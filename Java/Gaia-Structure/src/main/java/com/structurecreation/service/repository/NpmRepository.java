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
 * Repository service for fetching dependency information from NPM Registry
 */
public class NpmRepository implements RepositoryService {
    private static final Logger logger = LoggerFactory.getLogger(NpmRepository.class);
    private static final String NPM_REGISTRY_API = "https://registry.npmjs.org";
    private static final String NPM_SEARCH_API = "https://api.npms.io/v2/search";

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

    public NpmRepository() {
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
                String url = NPM_REGISTRY_API + "/" + URLEncoder.encode(packageName, StandardCharsets.UTF_8);

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .timeout(Duration.ofSeconds(10))
                        .GET()
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {
                    JsonNode root = objectMapper.readTree(response.body());

                    // Get the latest version from dist-tags
                    JsonNode distTags = root.path("dist-tags");
                    String latestVersion = distTags.path("latest").asText();

                    if (latestVersion.isEmpty()) {
                        // Fallback to the newest version from versions object
                        JsonNode versions = root.path("versions");
                        if (versions.isObject()) {
                            List<String> versionList = new ArrayList<>();
                            versions.fieldNames().forEachRemaining(versionList::add);
                            versionList.sort(this::compareVersions);
                            if (!versionList.isEmpty()) {
                                latestVersion = versionList.get(versionList.size() - 1);
                            }
                        }
                    }

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
                String url = NPM_REGISTRY_API + "/" + URLEncoder.encode(packageName, StandardCharsets.UTF_8);

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .timeout(Duration.ofSeconds(10))
                        .GET()
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {
                    JsonNode root = objectMapper.readTree(response.body());
                    JsonNode versions = root.path("versions");

                    List<String> versionList = new ArrayList<>();
                    if (versions.isObject()) {
                        versions.fieldNames().forEachRemaining(versionList::add);
                        // Sort versions newest first
                        versionList.sort(this::compareVersions);
                        Collections.reverse(versionList);

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
        return CompletableFuture.supplyAsync(() -> {
            try {
                String url = NPM_SEARCH_API + "?q=" + URLEncoder.encode(query, StandardCharsets.UTF_8)
                           + "&size=" + limit;

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .timeout(Duration.ofSeconds(10))
                        .GET()
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {
                    JsonNode root = objectMapper.readTree(response.body());
                    JsonNode results = root.path("results");

                    List<String> packages = new ArrayList<>();
                    if (results.isArray()) {
                        for (JsonNode result : results) {
                            String packageName = result.path("package").path("name").asText();
                            if (!packageName.isEmpty()) {
                                packages.add(packageName);
                            }
                        }
                    }

                    logger.info("Found {} packages for query: {}", packages.size(), query);
                    return packages;
                }
            } catch (Exception e) {
                logger.error("Error searching packages with query {}: {}", query, e.getMessage());
            }
            return Collections.emptyList();
        });
    }

    @Override
    public CompletableFuture<Dependency> getPackageInfo(String packageName, String version) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String url = NPM_REGISTRY_API + "/" + URLEncoder.encode(packageName, StandardCharsets.UTF_8);
                if (!version.equals("latest")) {
                    url += "/" + version;
                }

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .timeout(Duration.ofSeconds(10))
                        .GET()
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {
                    JsonNode root = objectMapper.readTree(response.body());

                    // For full package info, get specific version
                    JsonNode versionInfo = root;
                    if (version.equals("latest") && root.has("versions")) {
                        String latestVersion = root.path("dist-tags").path("latest").asText();
                        versionInfo = root.path("versions").path(latestVersion);
                    }

                    Dependency dep = new Dependency();
                    dep.setArtifactId(packageName);
                    dep.setVersion(versionInfo.path("version").asText(version));
                    dep.setDescription(versionInfo.path("description").asText(""));

                    // Get dependencies
                    JsonNode dependencies = versionInfo.path("dependencies");
                    if (dependencies.isObject()) {
                        List<String> depList = new ArrayList<>();
                        dependencies.fieldNames().forEachRemaining(depName -> {
                            String depVersion = dependencies.path(depName).asText();
                            depList.add(depName + "@" + depVersion);
                        });
                        if (!depList.isEmpty()) {
                            dep.setDescription(dep.getDescription() + " | Dependencies: " + String.join(", ", depList));
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
                String url = NPM_REGISTRY_API + "/" + URLEncoder.encode(packageName, StandardCharsets.UTF_8);

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
        return "NPM Registry";
    }

    /**
     * Get all dependencies (including dev, peer, and optional) for a package
     */
    public CompletableFuture<Map<String, Map<String, String>>> getAllDependencies(String packageName, String version) {
        return CompletableFuture.supplyAsync(() -> {
            Map<String, Map<String, String>> allDeps = new HashMap<>();

            try {
                String url = NPM_REGISTRY_API + "/" + URLEncoder.encode(packageName, StandardCharsets.UTF_8);

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .timeout(Duration.ofSeconds(10))
                        .GET()
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {
                    JsonNode root = objectMapper.readTree(response.body());

                    // Get specific version info
                    JsonNode versionInfo;
                    if (version.equals("latest")) {
                        String latestVersion = root.path("dist-tags").path("latest").asText();
                        versionInfo = root.path("versions").path(latestVersion);
                    } else {
                        versionInfo = root.path("versions").path(version);
                    }

                    // Extract different types of dependencies
                    extractDependencyMap(versionInfo, "dependencies", allDeps);
                    extractDependencyMap(versionInfo, "devDependencies", allDeps);
                    extractDependencyMap(versionInfo, "peerDependencies", allDeps);
                    extractDependencyMap(versionInfo, "optionalDependencies", allDeps);
                }
            } catch (Exception e) {
                logger.error("Error fetching all dependencies for {}@{}: {}", packageName, version, e.getMessage());
            }

            return allDeps;
        });
    }

    private void extractDependencyMap(JsonNode versionInfo, String depType, Map<String, Map<String, String>> allDeps) {
        JsonNode deps = versionInfo.path(depType);
        if (deps.isObject() && deps.size() > 0) {
            Map<String, String> depMap = new HashMap<>();
            deps.fieldNames().forEachRemaining(depName -> {
                depMap.put(depName, deps.path(depName).asText());
            });
            allDeps.put(depType, depMap);
        }
    }

    /**
     * Compare semantic versions
     */
    private int compareVersions(String v1, String v2) {
        // Remove any pre-release tags for comparison
        String clean1 = v1.split("-")[0];
        String clean2 = v2.split("-")[0];

        String[] parts1 = clean1.split("\\.");
        String[] parts2 = clean2.split("\\.");

        int length = Math.max(parts1.length, parts2.length);
        for (int i = 0; i < length; i++) {
            int num1 = i < parts1.length ? parseVersionPart(parts1[i]) : 0;
            int num2 = i < parts2.length ? parseVersionPart(parts2[i]) : 0;

            if (num1 != num2) {
                return Integer.compare(num1, num2);
            }
        }

        // If base versions are equal, consider pre-release tags
        boolean hasPreRelease1 = v1.contains("-");
        boolean hasPreRelease2 = v2.contains("-");

        if (hasPreRelease1 && !hasPreRelease2) return -1;
        if (!hasPreRelease1 && hasPreRelease2) return 1;
        if (hasPreRelease1 && hasPreRelease2) {
            return v1.compareTo(v2);
        }

        return 0;
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