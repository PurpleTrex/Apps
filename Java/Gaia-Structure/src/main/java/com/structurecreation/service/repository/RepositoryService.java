package com.structurecreation.service.repository;

import com.structurecreation.model.Dependency;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Interface for package repository services that fetch latest versions
 */
public interface RepositoryService {
    /**
     * Get the latest stable version of a package
     * @param packageName The name of the package
     * @return CompletableFuture with the latest version string
     */
    CompletableFuture<String> getLatestVersion(String packageName);

    /**
     * Get available versions of a package
     * @param packageName The name of the package
     * @param limit Maximum number of versions to return
     * @return CompletableFuture with list of version strings
     */
    CompletableFuture<List<String>> getAvailableVersions(String packageName, int limit);

    /**
     * Search for packages by name
     * @param query The search query
     * @param limit Maximum number of results
     * @return CompletableFuture with list of package names
     */
    CompletableFuture<List<String>> searchPackages(String query, int limit);

    /**
     * Get information about a specific package version
     * @param packageName The package name
     * @param version The version
     * @return CompletableFuture with dependency information
     */
    CompletableFuture<Dependency> getPackageInfo(String packageName, String version);

    /**
     * Check if a package exists in the repository
     * @param packageName The package name
     * @return CompletableFuture with boolean result
     */
    CompletableFuture<Boolean> packageExists(String packageName);

    /**
     * Get the repository type (e.g., "Maven Central", "NPM", "PyPI")
     * @return The repository type
     */
    String getRepositoryType();
}