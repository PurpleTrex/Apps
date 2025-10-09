package com.structurecreation.service.repository;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.structurecreation.model.Dependency;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Service for interacting with Maven Central Repository
 * Fetches latest versions and dependency information
 */
public class MavenCentralRepository implements RepositoryService {

    private static final Logger logger = LoggerFactory.getLogger(MavenCentralRepository.class);
    private static final String MAVEN_CENTRAL_API = "https://search.maven.org/solrsearch/select";
    private static final String MAVEN_CENTRAL_REPO = "https://repo1.maven.org/maven2";

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Map<String, CachedVersion> versionCache = new ConcurrentHashMap<>();
    private static final long CACHE_DURATION_MS = TimeUnit.HOURS.toMillis(1);

    /**
     * Cached version information
     */
    private static class CachedVersion {
        final String version;
        final List<String> availableVersions;
        final long timestamp;
        final Map<String, List<String>> dependencies;

        CachedVersion(String version, List<String> availableVersions, Map<String, List<String>> dependencies) {
            this.version = version;
            this.availableVersions = availableVersions;
            this.dependencies = dependencies;
            this.timestamp = System.currentTimeMillis();
        }

        boolean isExpired() {
            return System.currentTimeMillis() - timestamp > CACHE_DURATION_MS;
        }
    }

    /**
     * Get the latest version of a Maven artifact
     * @param groupId Group ID (e.g., "org.springframework.boot")
     * @param artifactId Artifact ID (e.g., "spring-boot-starter-web")
     * @return Latest version or null if not found
     */
    public String getLatestVersion(String groupId, String artifactId) {
        String cacheKey = groupId + ":" + artifactId;

        // Check cache
        CachedVersion cached = versionCache.get(cacheKey);
        if (cached != null && !cached.isExpired()) {
            logger.debug("Using cached version for {}", cacheKey);
            return cached.version;
        }

        try {
            // Build query URL
            String query = String.format("q=g:\"%s\"+AND+a:\"%s\"&rows=1&wt=json",
                URLEncoder.encode(groupId, StandardCharsets.UTF_8),
                URLEncoder.encode(artifactId, StandardCharsets.UTF_8));

            String urlString = MAVEN_CENTRAL_API + "?" + query;
            logger.debug("Fetching latest version from: {}", urlString);

            // Make HTTP request
            String response = makeHttpRequest(urlString);
            if (response == null) {
                return null;
            }

            // Parse JSON response
            JsonNode root = objectMapper.readTree(response);
            JsonNode docs = root.path("response").path("docs");

            if (docs.isArray() && docs.size() > 0) {
                String latestVersion = docs.get(0).path("latestVersion").asText();
                if (latestVersion.isEmpty()) {
                    latestVersion = docs.get(0).path("v").asText();
                }

                // Get all versions
                List<String> allVersions = getAllVersions(groupId, artifactId);

                // Get dependencies for this version
                Map<String, List<String>> dependencies = fetchDependencies(groupId, artifactId, latestVersion);

                // Cache the result
                versionCache.put(cacheKey, new CachedVersion(latestVersion, allVersions, dependencies));

                logger.info("Found latest version for {}: {}", cacheKey, latestVersion);
                return latestVersion;
            }
        } catch (Exception e) {
            logger.error("Error fetching latest version for {}:{}", groupId, artifactId, e);
        }

        return null;
    }

    /**
     * Get all available versions of a Maven artifact
     * @param groupId Group ID
     * @param artifactId Artifact ID
     * @return List of versions sorted from newest to oldest
     */
    public List<String> getAllVersions(String groupId, String artifactId) {
        String cacheKey = groupId + ":" + artifactId;

        // Check cache
        CachedVersion cached = versionCache.get(cacheKey);
        if (cached != null && !cached.isExpired() && cached.availableVersions != null) {
            return cached.availableVersions;
        }

        List<String> versions = new ArrayList<>();

        try {
            // Build query URL for all versions
            String query = String.format("q=g:\"%s\"+AND+a:\"%s\"&rows=50&wt=json&core=gav",
                URLEncoder.encode(groupId, StandardCharsets.UTF_8),
                URLEncoder.encode(artifactId, StandardCharsets.UTF_8));

            String urlString = MAVEN_CENTRAL_API + "?" + query;
            String response = makeHttpRequest(urlString);

            if (response != null) {
                JsonNode root = objectMapper.readTree(response);
                JsonNode docs = root.path("response").path("docs");

                if (docs.isArray()) {
                    for (JsonNode doc : docs) {
                        String version = doc.path("v").asText();
                        if (!version.isEmpty() && !versions.contains(version)) {
                            versions.add(version);
                        }
                    }
                }
            }

            // Sort versions (newest first)
            versions.sort(this::compareVersions);
            Collections.reverse(versions);

            logger.debug("Found {} versions for {}", versions.size(), cacheKey);
        } catch (Exception e) {
            logger.error("Error fetching versions for {}:{}", groupId, artifactId, e);
        }

        return versions;
    }

    /**
     * Search for artifacts by name
     * @param searchTerm Search term
     * @return List of matching artifacts with their latest versions
     */
    public List<MavenArtifact> searchArtifacts(String searchTerm) {
        List<MavenArtifact> artifacts = new ArrayList<>();

        try {
            String query = String.format("q=%s&rows=20&wt=json",
                URLEncoder.encode(searchTerm, StandardCharsets.UTF_8));

            String urlString = MAVEN_CENTRAL_API + "?" + query;
            String response = makeHttpRequest(urlString);

            if (response != null) {
                JsonNode root = objectMapper.readTree(response);
                JsonNode docs = root.path("response").path("docs");

                if (docs.isArray()) {
                    for (JsonNode doc : docs) {
                        MavenArtifact artifact = new MavenArtifact(
                            doc.path("g").asText(),
                            doc.path("a").asText(),
                            doc.path("latestVersion").asText(),
                            doc.path("p").asText()
                        );
                        artifacts.add(artifact);
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Error searching for artifacts: {}", searchTerm, e);
        }

        return artifacts;
    }

    /**
     * Fetch dependencies for a specific artifact version
     * @param groupId Group ID
     * @param artifactId Artifact ID
     * @param version Version
     * @return Map of dependency scopes to list of dependencies
     */
    public Map<String, List<String>> fetchDependencies(String groupId, String artifactId, String version) {
        Map<String, List<String>> dependencies = new HashMap<>();

        try {
            // Build POM URL
            String pomPath = groupId.replace('.', '/') + "/" + artifactId + "/" + version + "/" + artifactId + "-" + version + ".pom";
            String pomUrl = MAVEN_CENTRAL_REPO + "/" + pomPath;

            logger.debug("Fetching POM from: {}", pomUrl);
            String pomContent = makeHttpRequest(pomUrl);

            if (pomContent != null) {
                // Parse POM XML (simplified parsing)
                dependencies = parsePomDependencies(pomContent);
            }
        } catch (Exception e) {
            logger.error("Error fetching dependencies for {}:{}:{}", groupId, artifactId, version, e);
        }

        return dependencies;
    }

    /**
     * Parse dependencies from POM XML content
     */
    private Map<String, List<String>> parsePomDependencies(String pomContent) {
        Map<String, List<String>> dependencies = new HashMap<>();
        List<String> compile = new ArrayList<>();
        List<String> test = new ArrayList<>();
        List<String> provided = new ArrayList<>();

        try {
            // Simple XML parsing for dependencies
            String[] lines = pomContent.split("\n");
            boolean inDependencies = false;
            boolean inDependency = false;
            String currentGroupId = null;
            String currentArtifactId = null;
            String currentVersion = null;
            String currentScope = "compile";

            for (String line : lines) {
                line = line.trim();

                if (line.contains("<dependencies>")) {
                    inDependencies = true;
                } else if (line.contains("</dependencies>")) {
                    inDependencies = false;
                } else if (inDependencies) {
                    if (line.contains("<dependency>")) {
                        inDependency = true;
                        currentScope = "compile"; // default scope
                    } else if (line.contains("</dependency>")) {
                        if (currentGroupId != null && currentArtifactId != null) {
                            String dep = currentGroupId + ":" + currentArtifactId;
                            if (currentVersion != null) {
                                dep += ":" + currentVersion;
                            }

                            switch (currentScope) {
                                case "test":
                                    test.add(dep);
                                    break;
                                case "provided":
                                    provided.add(dep);
                                    break;
                                default:
                                    compile.add(dep);
                                    break;
                            }
                        }
                        inDependency = false;
                        currentGroupId = null;
                        currentArtifactId = null;
                        currentVersion = null;
                    } else if (inDependency) {
                        if (line.contains("<groupId>")) {
                            currentGroupId = extractXmlValue(line, "groupId");
                        } else if (line.contains("<artifactId>")) {
                            currentArtifactId = extractXmlValue(line, "artifactId");
                        } else if (line.contains("<version>")) {
                            currentVersion = extractXmlValue(line, "version");
                        } else if (line.contains("<scope>")) {
                            currentScope = extractXmlValue(line, "scope");
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Error parsing POM dependencies", e);
        }

        if (!compile.isEmpty()) dependencies.put("compile", compile);
        if (!test.isEmpty()) dependencies.put("test", test);
        if (!provided.isEmpty()) dependencies.put("provided", provided);

        return dependencies;
    }

    /**
     * Extract value from simple XML element
     */
    private String extractXmlValue(String line, String tagName) {
        String startTag = "<" + tagName + ">";
        String endTag = "</" + tagName + ">";
        int start = line.indexOf(startTag);
        int end = line.indexOf(endTag);
        if (start >= 0 && end > start) {
            return line.substring(start + startTag.length(), end);
        }
        return null;
    }

    /**
     * Check for version updates for multiple artifacts
     * @param artifacts List of artifacts to check
     * @return Map of artifact to update info
     */
    public CompletableFuture<Map<String, VersionUpdate>> checkForUpdates(List<MavenArtifact> artifacts) {
        return CompletableFuture.supplyAsync(() -> {
            Map<String, VersionUpdate> updates = new HashMap<>();

            for (MavenArtifact artifact : artifacts) {
                String latestVersion = getLatestVersion(artifact.groupId, artifact.artifactId);
                if (latestVersion != null && !latestVersion.equals(artifact.version)) {
                    List<String> allVersions = getAllVersions(artifact.groupId, artifact.artifactId);
                    updates.put(
                        artifact.getCoordinates(),
                        new VersionUpdate(artifact.version, latestVersion, allVersions)
                    );
                }
            }

            return updates;
        });
    }

    /**
     * Make HTTP GET request
     */
    private String makeHttpRequest(String urlString) {
        try {
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            conn.setRequestProperty("User-Agent", "Gaia-Structure/1.0");

            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                    return reader.lines().collect(Collectors.joining("\n"));
                }
            } else {
                logger.warn("HTTP request failed with code {}: {}", responseCode, urlString);
            }
        } catch (IOException e) {
            logger.error("Error making HTTP request to: {}", urlString, e);
        }

        return null;
    }

    /**
     * Compare version strings
     */
    private int compareVersions(String v1, String v2) {
        String[] parts1 = v1.split("[.-]");
        String[] parts2 = v2.split("[.-]");

        int length = Math.max(parts1.length, parts2.length);
        for (int i = 0; i < length; i++) {
            String part1 = i < parts1.length ? parts1[i] : "0";
            String part2 = i < parts2.length ? parts2[i] : "0";

            // Try numeric comparison first
            try {
                int num1 = Integer.parseInt(part1);
                int num2 = Integer.parseInt(part2);
                int result = Integer.compare(num1, num2);
                if (result != 0) return result;
            } catch (NumberFormatException e) {
                // Fall back to string comparison
                int result = part1.compareTo(part2);
                if (result != 0) return result;
            }
        }

        return 0;
    }

    /**
     * Clear the version cache
     */
    public void clearCache() {
        versionCache.clear();
        logger.info("Maven repository cache cleared");
    }

    /**
     * Maven artifact representation
     */
    public static class MavenArtifact {
        public final String groupId;
        public final String artifactId;
        public final String version;
        public final String packaging;

        public MavenArtifact(String groupId, String artifactId, String version, String packaging) {
            this.groupId = groupId;
            this.artifactId = artifactId;
            this.version = version;
            this.packaging = packaging != null ? packaging : "jar";
        }

        public String getCoordinates() {
            return groupId + ":" + artifactId;
        }

        @Override
        public String toString() {
            return groupId + ":" + artifactId + ":" + version;
        }
    }

    /**
     * Version update information
     */
    public static class VersionUpdate {
        public final String currentVersion;
        public final String latestVersion;
        public final List<String> availableVersions;

        public VersionUpdate(String currentVersion, String latestVersion, List<String> availableVersions) {
            this.currentVersion = currentVersion;
            this.latestVersion = latestVersion;
            this.availableVersions = availableVersions;
        }

        public boolean hasUpdate() {
            return !currentVersion.equals(latestVersion);
        }
    }

    // RepositoryService interface implementations
    @Override
    public CompletableFuture<String> getLatestVersion(String packageName) {
        return CompletableFuture.supplyAsync(() -> {
            String[] parts = packageName.split(":");
            if (parts.length != 2) {
                logger.warn("Invalid Maven package format: {}. Expected groupId:artifactId", packageName);
                return "LATEST";
            }
            String result = getLatestVersion(parts[0], parts[1]);
            return result != null ? result : "LATEST";
        });
    }

    @Override
    public CompletableFuture<List<String>> getAvailableVersions(String packageName, int limit) {
        return CompletableFuture.supplyAsync(() -> {
            String[] parts = packageName.split(":");
            if (parts.length != 2) {
                return Collections.emptyList();
            }
            List<String> versions = getAllVersions(parts[0], parts[1]);
            return versions.size() > limit ? versions.subList(0, limit) : versions;
        });
    }

    @Override
    public CompletableFuture<List<String>> searchPackages(String query, int limit) {
        return CompletableFuture.supplyAsync(() -> {
            List<MavenArtifact> artifacts = searchArtifacts(query);
            return artifacts.stream()
                .limit(limit)
                .map(a -> a.groupId + ":" + a.artifactId)
                .collect(Collectors.toList());
        });
    }

    @Override
    public CompletableFuture<Dependency> getPackageInfo(String packageName, String version) {
        return CompletableFuture.supplyAsync(() -> {
            String[] parts = packageName.split(":");
            if (parts.length != 2) {
                return null;
            }

            Dependency dep = new Dependency();
            dep.setGroupId(parts[0]);
            dep.setArtifactId(parts[1]);
            dep.setVersion(version);
            dep.setScope("compile");

            // Get dependencies for this version
            Map<String, List<String>> deps = fetchDependencies(parts[0], parts[1], version);
            if (deps.containsKey("compile")) {
                dep.setDescription("Dependencies: " + String.join(", ", deps.get("compile")));
            }

            return dep;
        });
    }

    @Override
    public CompletableFuture<Boolean> packageExists(String packageName) {
        return CompletableFuture.supplyAsync(() -> {
            String[] parts = packageName.split(":");
            if (parts.length != 2) {
                return false;
            }
            String version = getLatestVersion(parts[0], parts[1]);
            return version != null;
        });
    }

    @Override
    public String getRepositoryType() {
        return "Maven Central";
    }
}