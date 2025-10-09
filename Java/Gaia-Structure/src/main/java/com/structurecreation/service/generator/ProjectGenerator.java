package com.structurecreation.service.generator;

import com.structurecreation.model.ProjectNode;
import java.util.Map;

/**
 * Base interface for all project generators
 * Provides a standard contract for generating different types of project structures
 */
public interface ProjectGenerator {

    /**
     * Generate a complete project structure with all necessary files and configurations
     *
     * @param projectName The name of the project to generate
     * @param options Additional options specific to the project type
     * @return The root ProjectNode containing the complete project structure
     */
    ProjectNode generateProject(String projectName, Map<String, Object> options);

    /**
     * Get the supported project types for this generator
     *
     * @return Array of supported project type names
     */
    String[] getSupportedTypes();

    /**
     * Get the generator name
     *
     * @return The name of this generator (e.g., "Spring Boot", "React", "Django")
     */
    String getGeneratorName();

    /**
     * Get the generator description
     *
     * @return A brief description of what this generator creates
     */
    String getDescription();

    /**
     * Validate the project options before generation
     *
     * @param options The options to validate
     * @return true if options are valid, false otherwise
     */
    boolean validateOptions(Map<String, Object> options);

    /**
     * Get default options for this generator
     *
     * @return Map of default option values
     */
    Map<String, Object> getDefaultOptions();
}