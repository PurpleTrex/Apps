package com.structurecreation.service;

import com.structurecreation.model.Dependency;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * Service for resolving and managing dependencies from various package repositories
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
    
    public DependencyResolverService() {
        this.httpClient = HttpClient.newHttpClient();
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
            // This is a simplified parser - in a real implementation, you'd use a JSON library
            // For now, we'll return mock results for demonstration
            if (responseBody.contains("\"numFound\":0") || responseBody.contains("\"total\":0")) {
                return results; // No results found
            }
            
            // Mock results for demonstration
            results.add(new DependencySearchResult("junit", "JUnit 5", "5.10.0", "Testing framework"));
            results.add(new DependencySearchResult("spring-boot", "Spring Boot", "3.1.4", "Spring Boot framework"));
            
        } catch (Exception e) {
            logger.error("Failed to parse search results", e);
        }
        
        return results;
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
}