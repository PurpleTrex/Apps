package com.structurecreation.service;

import com.structurecreation.model.Dependency;
import com.structurecreation.service.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * Advanced dependency resolver that handles transitive dependencies,
 * version conflicts, and ensures compatibility
 */
public class DependencyResolverService {

    private static final Logger logger = LoggerFactory.getLogger(DependencyResolverService.class);

    // Repository URLs for different package managers
    private static final Map<String, String> REPOSITORY_URLS = new HashMap<>();
    private static final Map<String, String> SEARCH_URLS = new HashMap<>();

    static {
        // Maven Central
        REPOSITORY_URLS.put("Maven", "https://repo1.maven.org/maven2/");
        SEARCH_URLS.put("Maven", "https://search.maven.org/solrsearch/select?q={query}&rows=20&wt=json");

        // NPM Registry
        REPOSITORY_URLS.put("NPM", "https://registry.npmjs.org/");
        SEARCH_URLS.put("NPM", "https://registry.npmjs.org/-/v1/search?text={query}&size=20");

        // PyPI
        REPOSITORY_URLS.put("Pip", "https://pypi.org/simple/");
        SEARCH_URLS.put("Pip", "https://pypi.org/pypi/{query}/json");

        // NuGet
        REPOSITORY_URLS.put("NuGet", "https://api.nuget.org/v3-flatcontainer/");
        SEARCH_URLS.put("NuGet", "https://azuresearch-usnc.nuget.org/query?q={query}&take=20");

        // Gradle (uses Maven Central by default)
        REPOSITORY_URLS.put("Gradle", "https://repo1.maven.org/maven2/");
        SEARCH_URLS.put("Gradle", "https://search.maven.org/solrsearch/select?q={query}&rows=20&wt=json");
    }

    private final HttpClient httpClient;
    private final MavenCentralRepository mavenRepository;
    private final NpmRepository npmRepository;
    private final PyPiRepository pypiRepository;
    private final ExecutorService executorService;
    private final Map<String, ResolvedDependency> resolvedCache = new ConcurrentHashMap<>();

    public DependencyResolverService() {
        this.httpClient = HttpClient.newHttpClient();
        this.mavenRepository = new MavenCentralRepository();
        this.npmRepository = new NpmRepository();
        this.pypiRepository = new PyPiRepository();
        this.executorService = Executors.newFixedThreadPool(10);
    }
    
    /**
     * Get repository URL for a dependency type
     */
    public String getRepositoryUrl(String dependencyType) {
        return REPOSITORY_URLS.getOrDefault(dependencyType, "");
    }
    
    /**
     * Search for dependencies in the repository
     */
    public CompletableFuture<List<DependencySearchResult>> searchDependencies(String dependencyType, String query) {
        String searchUrl = SEARCH_URLS.get(dependencyType);
        if (searchUrl == null) {
            return CompletableFuture.completedFuture(new ArrayList<>());
        }
        
        String finalUrl = searchUrl.replace("{query}", query);
        
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(finalUrl))
            .header("Accept", "application/json")
            .build();
            
        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
            .thenApply(response -> parseDependencySearchResults(dependencyType, response.body()))
            .exceptionally(throwable -> {
                logger.error("Failed to search dependencies", throwable);
                return new ArrayList<>();
            });
    }
    
    /**
     * Get latest version of a dependency
     */
    public CompletableFuture<String> getLatestVersion(String dependencyType, String dependencyName) {
        return searchDependencies(dependencyType, dependencyName)
            .thenApply(results -> {
                if (!results.isEmpty()) {
                    return results.get(0).getLatestVersion();
                }
                return "latest";
            });
    }
    
    /**
     * Validate if a dependency exists in the repository
     */
    public CompletableFuture<Boolean> validateDependency(Dependency dependency) {
        return searchDependencies(dependency.getType(), dependency.getName())
            .thenApply(results -> !results.isEmpty());
    }
    
    /**
     * Generate installation command for a dependency
     */
    public String generateInstallCommand(Dependency dependency, String projectType) {
        String type = dependency.getType();
        String name = dependency.getName();
        String version = dependency.getVersion();
        
        switch (type.toLowerCase()) {
            case "maven":
                return generateMavenDependency(name, version);
            case "gradle":
                return generateGradleDependency(name, version);
            case "npm":
                return "npm install " + name + (version.equals("latest") ? "" : "@" + version);
            case "yarn":
                return "yarn add " + name + (version.equals("latest") ? "" : "@" + version);
            case "pip":
                return "pip install " + name + (version.equals("latest") ? "" : "==" + version);
            case "nuget":
                return "dotnet add package " + name + (version.equals("latest") ? "" : " --version " + version);
            default:
                return "# Install " + name + " " + version;
        }
    }
    
    /**
     * Generate build file entries for dependencies
     */
    public String generateBuildFileEntry(List<Dependency> dependencies, String projectType) {
        StringBuilder sb = new StringBuilder();
        
        switch (projectType.toLowerCase()) {
            case "java maven":
                sb.append("<!-- Dependencies -->\n<dependencies>\n");
                for (Dependency dep : dependencies) {
                    if ("Maven".equals(dep.getType())) {
                        sb.append(generateMavenDependency(dep.getName(), dep.getVersion())).append("\n");
                    }
                }
                sb.append("</dependencies>\n");
                break;
                
            case "java gradle":
            case "spring boot":
                sb.append("dependencies {\n");
                for (Dependency dep : dependencies) {
                    if ("Gradle".equals(dep.getType()) || "Maven".equals(dep.getType())) {
                        sb.append(generateGradleDependency(dep.getName(), dep.getVersion())).append("\n");
                    }
                }
                sb.append("}\n");
                break;
                
            case "node.js":
            case "react":
                sb.append("\"dependencies\": {\n");
                for (int i = 0; i < dependencies.size(); i++) {
                    Dependency dep = dependencies.get(i);
                    if ("NPM".equals(dep.getType())) {
                        sb.append("  \"").append(dep.getName()).append("\": \"")
                          .append(dep.getVersion()).append("\"");
                        if (i < dependencies.size() - 1) sb.append(",");
                        sb.append("\n");
                    }
                }
                sb.append("}\n");
                break;
                
            case "python":
            case "django":
            case "flask":
                for (Dependency dep : dependencies) {
                    if ("Pip".equals(dep.getType())) {
                        sb.append(dep.getName());
                        if (!dep.getVersion().equals("latest")) {
                            sb.append("==").append(dep.getVersion());
                        }
                        sb.append("\n");
                    }
                }
                break;
        }
        
        return sb.toString();
    }
    
    private String generateMavenDependency(String name, String version) {
        String[] parts = name.split(":");
        if (parts.length >= 2) {
            return String.format(
                "    <dependency>\n" +
                "        <groupId>%s</groupId>\n" +
                "        <artifactId>%s</artifactId>\n" +
                "        <version>%s</version>\n" +
                "    </dependency>",
                parts[0], parts[1], version
            );
        }
        return "    <!-- Invalid Maven dependency format: " + name + " -->";
    }
    
    private String generateGradleDependency(String name, String version) {
        return "    implementation '" + name + ":" + version + "'";
    }
    
    private List<DependencySearchResult> parseDependencySearchResults(String dependencyType, String responseBody) {
        List<DependencySearchResult> results = new ArrayList<>();

        try {
            // Check for empty results
            if (responseBody == null || responseBody.isEmpty() ||
                responseBody.contains("\"numFound\":0") || responseBody.contains("\"total\":0")) {
                return results;
            }

            switch (dependencyType) {
                case "Maven":
                case "Gradle":
                    parseMavenSearchResults(responseBody, results);
                    break;
                case "NPM":
                    parseNpmSearchResults(responseBody, results);
                    break;
                case "Pip":
                    parsePyPiSearchResults(responseBody, results);
                    break;
                case "NuGet":
                    parseNuGetSearchResults(responseBody, results);
                    break;
                default:
                    logger.warn("Unknown dependency type: {}", dependencyType);
            }

        } catch (Exception e) {
            logger.error("Failed to parse search results for {}: {}", dependencyType, e.getMessage());
        }

        return results;
    }

    private void parseMavenSearchResults(String responseBody, List<DependencySearchResult> results) {
        // Parse Maven Central search response
        // Format: {"response":{"numFound":X,"docs":[{"id":"...","g":"groupId","a":"artifactId","latestVersion":"...","p":"jar","text":["..."]}]}}

        int docsStart = responseBody.indexOf("\"docs\":[");
        if (docsStart == -1) return;

        int docsEnd = responseBody.indexOf("]", docsStart);
        if (docsEnd == -1) return;

        String docsSection = responseBody.substring(docsStart + 8, docsEnd);
        String[] artifacts = docsSection.split("\\},\\{");

        for (String artifact : artifacts) {
            String groupId = extractJsonValue(artifact, "\"g\":\"", "\"");
            String artifactId = extractJsonValue(artifact, "\"a\":\"", "\"");
            String version = extractJsonValue(artifact, "\"latestVersion\":\"", "\"");
            String packaging = extractJsonValue(artifact, "\"p\":\"", "\"");

            if (groupId != null && artifactId != null) {
                String name = groupId + ":" + artifactId;
                String displayName = artifactId;
                String description = String.format("%s (%s)", groupId, packaging != null ? packaging : "jar");

                if (version == null || version.isEmpty()) {
                    version = extractJsonValue(artifact, "\"v\":\"", "\"");
                }

                results.add(new DependencySearchResult(name, displayName,
                    version != null ? version : "latest", description));
            }
        }
    }

    private void parseNpmSearchResults(String responseBody, List<DependencySearchResult> results) {
        // Parse NPM search response
        // Format: {"objects":[{"package":{"name":"...","version":"...","description":"..."}}]}

        int objectsStart = responseBody.indexOf("\"objects\":[");
        if (objectsStart == -1) return;

        String[] packages = responseBody.split("\"package\":\\{");
        for (int i = 1; i < packages.length && i <= 20; i++) {
            String pkg = packages[i];
            String name = extractJsonValue(pkg, "\"name\":\"", "\"");
            String version = extractJsonValue(pkg, "\"version\":\"", "\"");
            String description = extractJsonValue(pkg, "\"description\":\"", "\"");

            if (name != null) {
                results.add(new DependencySearchResult(name, name,
                    version != null ? version : "latest",
                    description != null ? description : ""));
            }
        }
    }

    private void parsePyPiSearchResults(String responseBody, List<DependencySearchResult> results) {
        // Parse PyPI response - single package info
        // Format: {"info":{"name":"...","version":"...","summary":"..."}}

        String name = extractJsonValue(responseBody, "\"name\":\"", "\"");
        String version = extractJsonValue(responseBody, "\"version\":\"", "\"");
        String summary = extractJsonValue(responseBody, "\"summary\":\"", "\"");

        if (name != null) {
            results.add(new DependencySearchResult(name, name,
                version != null ? version : "latest",
                summary != null ? summary : ""));
        }
    }

    private void parseNuGetSearchResults(String responseBody, List<DependencySearchResult> results) {
        // Parse NuGet search response
        // Format: {"data":[{"id":"...","version":"...","description":"..."}]}

        int dataStart = responseBody.indexOf("\"data\":[");
        if (dataStart == -1) return;

        String[] packages = responseBody.split("\\{\"id\":\"");
        for (int i = 1; i < packages.length && i <= 20; i++) {
            String pkg = packages[i];
            String id = pkg.substring(0, pkg.indexOf("\""));
            String version = extractJsonValue(pkg, "\"version\":\"", "\"");
            String description = extractJsonValue(pkg, "\"description\":\"", "\"");

            if (id != null) {
                results.add(new DependencySearchResult(id, id,
                    version != null ? version : "latest",
                    description != null ? description : ""));
            }
        }
    }

    private String extractJsonValue(String json, String startMarker, String endMarker) {
        int start = json.indexOf(startMarker);
        if (start == -1) return null;

        start += startMarker.length();
        int end = json.indexOf(endMarker, start);
        if (end == -1) return null;

        String value = json.substring(start, end);
        // Unescape JSON strings
        return value.replace("\\\"", "\"")
                   .replace("\\\\", "\\")
                   .replace("\\n", "\n")
                   .replace("\\r", "\r")
                   .replace("\\t", "\t");
    }
    
    /**
     * Data class for dependency search results
     */
    public static class DependencySearchResult {
        private final String name;
        private final String displayName;
        private final String latestVersion;
        private final String description;
        
        public DependencySearchResult(String name, String displayName, String latestVersion, String description) {
            this.name = name;
            this.displayName = displayName;
            this.latestVersion = latestVersion;
            this.description = description;
        }
        
        public String getName() { return name; }
        public String getDisplayName() { return displayName; }
        public String getLatestVersion() { return latestVersion; }
        public String getDescription() { return description; }
        
        @Override
        public String toString() {
            return displayName + " (" + latestVersion + ")";
        }
    }

    /**
     * Resolved dependency with all transitive dependencies
     */
    public static class ResolvedDependency {
        public final String name;
        public final String version;
        public final String type;
        public final Set<ResolvedDependency> dependencies;
        public final Map<String, String> metadata;

        public ResolvedDependency(String name, String version, String type) {
            this.name = name;
            this.version = version;
            this.type = type;
            this.dependencies = new HashSet<>();
            this.metadata = new HashMap<>();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ResolvedDependency that = (ResolvedDependency) o;
            return Objects.equals(name, that.name) && Objects.equals(version, that.version);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, version);
        }

        @Override
        public String toString() {
            return name + "@" + version;
        }
    }

    /**
     * Dependency conflict resolution result
     */
    public static class ConflictResolution {
        public final String dependency;
        public final String selectedVersion;
        public final List<String> requestedVersions;
        public final String reason;

        public ConflictResolution(String dependency, String selectedVersion,
                                 List<String> requestedVersions, String reason) {
            this.dependency = dependency;
            this.selectedVersion = selectedVersion;
            this.requestedVersions = requestedVersions;
            this.reason = reason;
        }
    }

    /**
     * Resolve all dependencies for a Maven project with transitive dependencies
     */
    public CompletableFuture<Set<ResolvedDependency>> resolveMavenDependencies(List<Dependency> dependencies) {
        return CompletableFuture.supplyAsync(() -> {
            Set<ResolvedDependency> resolved = new HashSet<>();
            Map<String, String> versionConstraints = new HashMap<>();

            for (Dependency dep : dependencies) {
                String key = dep.getGroupId() != null ?
                    dep.getGroupId() + ":" + dep.getArtifactId() : dep.getName();
                ResolvedDependency resolvedDep = resolveMavenDependency(key, dep.getVersion(),
                                                                       versionConstraints, new HashSet<>());
                if (resolvedDep != null) {
                    resolved.add(resolvedDep);
                }
            }

            // Resolve conflicts
            resolved = resolveVersionConflicts(resolved, "maven");

            logger.info("Resolved {} Maven dependencies with {} total artifacts",
                       dependencies.size(), resolved.size());
            return resolved;
        }, executorService);
    }

    /**
     * Resolve a single Maven dependency with transitive dependencies
     */
    private ResolvedDependency resolveMavenDependency(String packageName, String version,
                                                      Map<String, String> versionConstraints,
                                                      Set<String> visited) {
        String cacheKey = "maven:" + packageName + ":" + version;

        // Check cache
        if (resolvedCache.containsKey(cacheKey)) {
            return resolvedCache.get(cacheKey);
        }

        // Prevent circular dependencies
        if (visited.contains(packageName)) {
            logger.debug("Circular dependency detected for: {}", packageName);
            return null;
        }
        visited.add(packageName);

        try {
            // Get latest version if needed
            if (version == null || version.equals("LATEST") || version.isEmpty() || version.equals("latest")) {
                version = mavenRepository.getLatestVersion(packageName).get();
            }

            ResolvedDependency resolved = new ResolvedDependency(packageName, version, "maven");

            // Get transitive dependencies
            String[] parts = packageName.split(":");
            if (parts.length == 2) {
                Map<String, List<String>> deps = mavenRepository.fetchDependencies(
                    parts[0], parts[1], version);

                if (deps.containsKey("compile")) {
                    for (String dep : deps.get("compile")) {
                        String[] depParts = dep.split(":");
                        if (depParts.length >= 2) {
                            String depKey = depParts[0] + ":" + depParts[1];
                            String depVersion = depParts.length > 2 ? depParts[2] : "LATEST";

                            ResolvedDependency transitive = resolveMavenDependency(
                                depKey, depVersion, versionConstraints, new HashSet<>(visited));

                            if (transitive != null) {
                                resolved.dependencies.add(transitive);
                            }
                        }
                    }
                }
            }

            resolvedCache.put(cacheKey, resolved);
            return resolved;

        } catch (Exception e) {
            logger.error("Error resolving Maven dependency {}: {}", packageName, e.getMessage());
            return null;
        }
    }

    /**
     * Resolve all dependencies for an NPM project with transitive dependencies
     */
    public CompletableFuture<Set<ResolvedDependency>> resolveNpmDependencies(Map<String, String> dependencies) {
        return CompletableFuture.supplyAsync(() -> {
            Set<ResolvedDependency> resolved = new HashSet<>();
            Map<String, String> versionConstraints = new HashMap<>(dependencies);

            for (Map.Entry<String, String> entry : dependencies.entrySet()) {
                ResolvedDependency resolvedDep = resolveNpmDependency(entry.getKey(), entry.getValue(),
                                                                     versionConstraints, new HashSet<>(), 0);
                if (resolvedDep != null) {
                    resolved.add(resolvedDep);
                }
            }

            // Resolve conflicts
            resolved = resolveVersionConflicts(resolved, "npm");

            logger.info("Resolved {} NPM dependencies with {} total packages",
                       dependencies.size(), resolved.size());
            return resolved;
        }, executorService);
    }

    /**
     * Resolve a single NPM dependency with transitive dependencies
     */
    private ResolvedDependency resolveNpmDependency(String packageName, String versionRange,
                                                    Map<String, String> versionConstraints,
                                                    Set<String> visited, int depth) {
        // Limit depth to prevent infinite recursion
        if (depth > 5) {
            logger.debug("Max depth reached for: {}", packageName);
            return null;
        }

        String cacheKey = "npm:" + packageName + ":" + versionRange;

        // Check cache
        if (resolvedCache.containsKey(cacheKey)) {
            return resolvedCache.get(cacheKey);
        }

        // Prevent circular dependencies
        if (visited.contains(packageName)) {
            logger.debug("Circular dependency detected for: {}", packageName);
            return null;
        }
        visited.add(packageName);

        try {
            // Resolve version from range
            String version = resolveNpmVersion(packageName, versionRange);
            if (version == null) {
                version = npmRepository.getLatestVersion(packageName).get();
            }

            ResolvedDependency resolved = new ResolvedDependency(packageName, version, "npm");

            // Get transitive dependencies
            Map<String, Map<String, String>> allDeps = npmRepository.getAllDependencies(packageName, version).get();

            if (allDeps.containsKey("dependencies")) {
                Map<String, String> deps = allDeps.get("dependencies");
                for (Map.Entry<String, String> dep : deps.entrySet()) {
                    ResolvedDependency transitive = resolveNpmDependency(
                        dep.getKey(), dep.getValue(), versionConstraints,
                        new HashSet<>(visited), depth + 1);

                    if (transitive != null) {
                        resolved.dependencies.add(transitive);
                    }
                }
            }

            resolvedCache.put(cacheKey, resolved);
            return resolved;

        } catch (Exception e) {
            logger.error("Error resolving NPM dependency {}: {}", packageName, e.getMessage());
            return null;
        }
    }

    /**
     * Resolve NPM version from version range
     */
    private String resolveNpmVersion(String packageName, String versionRange) {
        try {
            // Handle common version ranges
            if (versionRange.equals("*") || versionRange.equals("latest")) {
                return npmRepository.getLatestVersion(packageName).get();
            }

            // Remove common prefixes
            String cleanVersion = versionRange.replaceAll("^[~^>=<]", "");

            // If it's a specific version, use it
            if (cleanVersion.matches("\\d+\\.\\d+\\.\\d+")) {
                return cleanVersion;
            }

            // Otherwise get latest matching version
            List<String> versions = npmRepository.getAvailableVersions(packageName, 20).get();
            for (String version : versions) {
                if (matchesVersionRange(version, versionRange)) {
                    return version;
                }
            }

            // Fallback to latest
            return npmRepository.getLatestVersion(packageName).get();

        } catch (Exception e) {
            logger.error("Error resolving NPM version for {} with range {}: {}",
                        packageName, versionRange, e.getMessage());
            return null;
        }
    }

    /**
     * Check if a version matches a version range
     */
    private boolean matchesVersionRange(String version, String range) {
        // Simplified version matching
        if (range.startsWith("^")) {
            // Caret range - compatible with version
            String baseVersion = range.substring(1);
            return version.startsWith(baseVersion.split("\\.")[0]);
        } else if (range.startsWith("~")) {
            // Tilde range - approximately equivalent to version
            String baseVersion = range.substring(1);
            String[] baseParts = baseVersion.split("\\.");
            String[] versionParts = version.split("\\.");
            return baseParts.length >= 2 && versionParts.length >= 2 &&
                   baseParts[0].equals(versionParts[0]) &&
                   baseParts[1].equals(versionParts[1]);
        } else if (range.startsWith(">=")) {
            return compareVersions(version, range.substring(2)) >= 0;
        } else if (range.startsWith(">")) {
            return compareVersions(version, range.substring(1)) > 0;
        }

        return version.equals(range);
    }

    /**
     * Compare two semantic versions
     */
    private int compareVersions(String v1, String v2) {
        String[] parts1 = v1.split("\\.");
        String[] parts2 = v2.split("\\.");

        for (int i = 0; i < Math.min(parts1.length, parts2.length); i++) {
            int p1 = Integer.parseInt(parts1[i].replaceAll("[^0-9]", ""));
            int p2 = Integer.parseInt(parts2[i].replaceAll("[^0-9]", ""));
            if (p1 != p2) {
                return Integer.compare(p1, p2);
            }
        }

        return Integer.compare(parts1.length, parts2.length);
    }

    /**
     * Resolve version conflicts in dependency tree
     */
    private Set<ResolvedDependency> resolveVersionConflicts(Set<ResolvedDependency> dependencies, String type) {
        Map<String, List<ResolvedDependency>> groupedDeps = new HashMap<>();

        // Group dependencies by name
        flattenDependencies(dependencies).forEach(dep ->
            groupedDeps.computeIfAbsent(dep.name, k -> new ArrayList<>()).add(dep)
        );

        Set<ResolvedDependency> resolved = new HashSet<>();
        List<ConflictResolution> conflicts = new ArrayList<>();

        for (Map.Entry<String, List<ResolvedDependency>> entry : groupedDeps.entrySet()) {
            List<ResolvedDependency> versions = entry.getValue();

            if (versions.size() == 1) {
                resolved.add(versions.get(0));
            } else {
                // Resolve conflict - pick the newest compatible version
                ResolvedDependency selected = selectBestVersion(versions, type);
                resolved.add(selected);

                conflicts.add(new ConflictResolution(
                    entry.getKey(),
                    selected.version,
                    versions.stream().map(d -> d.version).collect(Collectors.toList()),
                    "Selected newest compatible version"
                ));
            }
        }

        if (!conflicts.isEmpty()) {
            logger.info("Resolved {} version conflicts:", conflicts.size());
            conflicts.forEach(c -> logger.info("  {} -> {} (requested: {})",
                                              c.dependency, c.selectedVersion, c.requestedVersions));
        }

        return resolved;
    }

    /**
     * Flatten dependency tree to a set
     */
    private Set<ResolvedDependency> flattenDependencies(Set<ResolvedDependency> dependencies) {
        Set<ResolvedDependency> flattened = new HashSet<>();
        Queue<ResolvedDependency> queue = new LinkedList<>(dependencies);

        while (!queue.isEmpty()) {
            ResolvedDependency dep = queue.poll();
            if (flattened.add(dep)) {
                queue.addAll(dep.dependencies);
            }
        }

        return flattened;
    }

    /**
     * Select the best version from conflicting dependencies
     */
    private ResolvedDependency selectBestVersion(List<ResolvedDependency> versions, String type) {
        // Sort by version (newest first)
        versions.sort((a, b) -> compareVersions(b.version, a.version));

        // Return the newest version
        return versions.get(0);
    }

    /**
     * Get a complete dependency tree as a formatted string
     */
    public String getDependencyTree(Set<ResolvedDependency> dependencies) {
        StringBuilder sb = new StringBuilder();
        for (ResolvedDependency dep : dependencies) {
            appendDependencyTree(sb, dep, "", new HashSet<>());
        }
        return sb.toString();
    }

    private void appendDependencyTree(StringBuilder sb, ResolvedDependency dep,
                                     String prefix, Set<String> visited) {
        String key = dep.name + "@" + dep.version;
        if (visited.contains(key)) {
            sb.append(prefix).append(dep.name).append("@").append(dep.version)
              .append(" (circular reference)\n");
            return;
        }
        visited.add(key);

        sb.append(prefix).append(dep.name).append("@").append(dep.version).append("\n");

        List<ResolvedDependency> sortedDeps = new ArrayList<>(dep.dependencies);
        sortedDeps.sort(Comparator.comparing(d -> d.name));

        for (int i = 0; i < sortedDeps.size(); i++) {
            boolean isLast = (i == sortedDeps.size() - 1);
            String childPrefix = prefix + (isLast ? "└── " : "├── ");
            String grandchildPrefix = prefix + (isLast ? "    " : "│   ");
            appendDependencyTree(sb, sortedDeps.get(i), childPrefix, new HashSet<>(visited));
        }
    }

    /**
     * Clear all caches
     */
    public void clearCaches() {
        resolvedCache.clear();
        mavenRepository.clearCache();
        npmRepository.clearCache();
        pypiRepository.clearCache();
    }

    /**
     * Shutdown the executor service
     */
    public void shutdown() {
        executorService.shutdown();
    }
}