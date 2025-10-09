package com.structurecreation.service.repository;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.structurecreation.model.Dependency;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Repository service for fetching dependency information from PyPI (Python Package Index)
 */
public class PyPiRepository implements RepositoryService {
    private static final Logger logger = LoggerFactory.getLogger(PyPiRepository.class);
    private static final String PYPI_API = "https://pypi.org/pypi";
    private static final String PYPI_SEARCH_API = "https://pypi.org/simple";

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final ConcurrentHashMap<String, CacheEntry> cache;
    private static final long CACHE_DURATION_MINUTES = 15;

    private static class CacheEntry {
        final Object data;
        final long timestamp;

        CacheEntry(Object data) {
            this.data = data;
            this.timestamp = System.currentTimeMillis();
        }

        boolean isExpired() {
            return System.currentTimeMillis() - timestamp > TimeUnit.MINUTES.toMillis(CACHE_DURATION_MINUTES);
        }
    }

    public PyPiRepository() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.objectMapper = new ObjectMapper();
        this.cache = new ConcurrentHashMap<>();
    }

    @Override
    public CompletableFuture<String> getLatestVersion(String packageName) {
        String cacheKey = "latest:" + packageName;
        CacheEntry cached = cache.get(cacheKey);
        if (cached != null && !cached.isExpired()) {
            return CompletableFuture.completedFuture((String) cached.data);
        }

        return CompletableFuture.supplyAsync(() -> {
            try {
                String url = PYPI_API + "/" + URLEncoder.encode(packageName, StandardCharsets.UTF_8) + "/json";

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .timeout(Duration.ofSeconds(10))
                        .GET()
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {
                    JsonNode root = objectMapper.readTree(response.body());
                    JsonNode info = root.path("info");
                    String latestVersion = info.path("version").asText();

                    if (!latestVersion.isEmpty()) {
                        cache.put(cacheKey, new CacheEntry(latestVersion));
                        logger.info("Found latest version for {}: {}", packageName, latestVersion);
                        return latestVersion;
                    }
                }
                logger.warn("Could not find latest version for: {}", packageName);
                return "latest";
            } catch (Exception e) {
                logger.error("Error fetching latest version for {}: {}", packageName, e.getMessage());
                return "latest";
            }
        });
    }

    @Override
    public CompletableFuture<List<String>> getAvailableVersions(String packageName, int limit) {
        String cacheKey = "versions:" + packageName + ":" + limit;
        CacheEntry cached = cache.get(cacheKey);
        if (cached != null && !cached.isExpired()) {
            return CompletableFuture.completedFuture((List<String>) cached.data);
        }

        return CompletableFuture.supplyAsync(() -> {
            try {
                String url = PYPI_API + "/" + URLEncoder.encode(packageName, StandardCharsets.UTF_8) + "/json";

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .timeout(Duration.ofSeconds(10))
                        .GET()
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {
                    JsonNode root = objectMapper.readTree(response.body());
                    JsonNode releases = root.path("releases");

                    List<String> versionList = new ArrayList<>();
                    if (releases.isObject()) {
                        releases.fieldNames().forEachRemaining(versionList::add);
                        // Sort versions newest first
                        versionList.sort(this::compareVersions);
                        Collections.reverse(versionList);

                        // Filter out pre-releases if we have enough stable versions
                        List<String> stableVersions = versionList.stream()
                                .filter(v -> !v.contains("a") && !v.contains("b") && !v.contains("rc"))
                                .collect(Collectors.toList());

                        if (stableVersions.size() >= limit) {
                            versionList = stableVersions;
                        }

                        // Limit the results
                        if (versionList.size() > limit) {
                            versionList = versionList.subList(0, limit);
                        }
                    }

                    cache.put(cacheKey, new CacheEntry(versionList));
                    logger.info("Found {} versions for {}", versionList.size(), packageName);
                    return versionList;
                }
            } catch (Exception e) {
                logger.error("Error fetching versions for {}: {}", packageName, e.getMessage());
            }
            return Collections.emptyList();
        });
    }

    @Override
    public CompletableFuture<List<String>> searchPackages(String query, int limit) {
        // PyPI doesn't have a simple search API anymore, so we'll use a workaround
        // This would ideally be replaced with a proper search implementation
        return CompletableFuture.supplyAsync(() -> {
            List<String> commonPackages = Arrays.asList(
                "requests", "numpy", "pandas", "matplotlib", "flask", "django",
                "pytest", "scipy", "scikit-learn", "tensorflow", "torch",
                "beautifulsoup4", "sqlalchemy", "pillow", "selenium", "scrapy"
            );

            return commonPackages.stream()
                    .filter(pkg -> pkg.toLowerCase().contains(query.toLowerCase()))
                    .limit(limit)
                    .collect(Collectors.toList());
        });
    }

    @Override
    public CompletableFuture<Dependency> getPackageInfo(String packageName, String version) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String url = PYPI_API + "/" + URLEncoder.encode(packageName, StandardCharsets.UTF_8);
                if (!version.equals("latest")) {
                    url += "/" + version;
                }
                url += "/json";

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .timeout(Duration.ofSeconds(10))
                        .GET()
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {
                    JsonNode root = objectMapper.readTree(response.body());
                    JsonNode info = root.path("info");

                    Dependency dep = new Dependency();
                    dep.setArtifactId(packageName);
                    dep.setVersion(info.path("version").asText(version));

                    String description = info.path("summary").asText("");
                    if (description.isEmpty()) {
                        description = info.path("description").asText("");
                        if (description.length() > 200) {
                            description = description.substring(0, 200) + "...";
                        }
                    }
                    dep.setDescription(description);

                    // Get requires_dist for dependencies
                    JsonNode requiresDist = info.path("requires_dist");
                    if (requiresDist.isArray() && requiresDist.size() > 0) {
                        List<String> deps = new ArrayList<>();
                        for (JsonNode req : requiresDist) {
                            String reqStr = req.asText();
                            // Parse out just the package name from requirements like "package>=1.0.0"
                            String pkgName = reqStr.split("[<>=!]")[0].trim();
                            if (!pkgName.isEmpty() && !pkgName.contains(";")) {
                                deps.add(pkgName);
                            }
                        }
                        if (!deps.isEmpty()) {
                            dep.setDescription(description + " | Dependencies: " + String.join(", ", deps));
                        }
                    }

                    return dep;
                }
            } catch (Exception e) {
                logger.error("Error fetching package info for {}@{}: {}", packageName, version, e.getMessage());
            }
            return null;
        });
    }

    @Override
    public CompletableFuture<Boolean> packageExists(String packageName) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String url = PYPI_API + "/" + URLEncoder.encode(packageName, StandardCharsets.UTF_8) + "/json";

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .timeout(Duration.ofSeconds(5))
                        .method("HEAD", HttpRequest.BodyPublishers.noBody())
                        .build();

                HttpResponse<Void> response = httpClient.send(request, HttpResponse.BodyHandlers.discarding());
                return response.statusCode() == 200;
            } catch (Exception e) {
                logger.error("Error checking if package exists {}: {}", packageName, e.getMessage());
                return false;
            }
        });
    }

    @Override
    public String getRepositoryType() {
        return "PyPI";
    }

    /**
     * Get all dependencies with their version constraints
     */
    public CompletableFuture<Map<String, String>> getDependenciesWithConstraints(String packageName, String version) {
        return CompletableFuture.supplyAsync(() -> {
            Map<String, String> dependencies = new HashMap<>();

            try {
                String url = PYPI_API + "/" + URLEncoder.encode(packageName, StandardCharsets.UTF_8);
                if (!version.equals("latest")) {
                    url += "/" + version;
                }
                url += "/json";

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .timeout(Duration.ofSeconds(10))
                        .GET()
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {
                    JsonNode root = objectMapper.readTree(response.body());
                    JsonNode info = root.path("info");
                    JsonNode requiresDist = info.path("requires_dist");

                    if (requiresDist.isArray()) {
                        for (JsonNode req : requiresDist) {
                            String reqStr = req.asText();
                            // Skip conditional dependencies (those with ; in them)
                            if (!reqStr.contains(";")) {
                                // Parse package name and version constraint
                                String pkgName = reqStr.split("[<>=!]")[0].trim();
                                String constraint = reqStr.substring(pkgName.length()).trim();
                                if (constraint.isEmpty()) {
                                    constraint = "*";
                                }
                                dependencies.put(pkgName, constraint);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                logger.error("Error fetching dependencies for {}@{}: {}", packageName, version, e.getMessage());
            }

            return dependencies;
        });
    }

    /**
     * Compare Python version strings
     */
    private int compareVersions(String v1, String v2) {
        // Handle PEP 440 version strings
        String[] parts1 = normalizeVersion(v1).split("\\.");
        String[] parts2 = normalizeVersion(v2).split("\\.");

        int length = Math.max(parts1.length, parts2.length);
        for (int i = 0; i < length; i++) {
            int num1 = i < parts1.length ? parseVersionPart(parts1[i]) : 0;
            int num2 = i < parts2.length ? parseVersionPart(parts2[i]) : 0;

            if (num1 != num2) {
                return Integer.compare(num1, num2);
            }
        }

        // Consider pre-release versions
        int preRelease1 = getPreReleaseWeight(v1);
        int preRelease2 = getPreReleaseWeight(v2);

        return Integer.compare(preRelease1, preRelease2);
    }

    private String normalizeVersion(String version) {
        // Remove pre-release identifiers for main comparison
        return version.replaceAll("[ab]\\d+", "")
                     .replaceAll("rc\\d+", "")
                     .replaceAll("\\.post\\d+", "")
                     .replaceAll("\\.dev\\d+", "");
    }

    private int getPreReleaseWeight(String version) {
        if (version.contains(".dev")) return 0;
        if (version.contains("a")) return 1;
        if (version.contains("b")) return 2;
        if (version.contains("rc")) return 3;
        return 4; // Stable release
    }

    private int parseVersionPart(String part) {
        try {
            return Integer.parseInt(part);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /**
     * Clear the cache
     */
    public void clearCache() {
        cache.clear();
    }

    /**
     * Clean expired entries from cache
     */
    public void cleanCache() {
        cache.entrySet().removeIf(entry -> entry.getValue().isExpired());
    }
}