package com.structurecreation.service.generator;

import com.structurecreation.model.ProjectNode;
import com.structurecreation.service.DependencyResolverService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Abstract base class for project generators providing common functionality
 */
public abstract class AbstractProjectGenerator implements ProjectGenerator {
    protected static final Logger logger = LoggerFactory.getLogger(AbstractProjectGenerator.class);

    protected final DependencyResolverService dependencyResolver;

    protected AbstractProjectGenerator() {
        this.dependencyResolver = new DependencyResolverService();
    }

    /**
     * Common validation logic
     */
    @Override
    public boolean validateOptions(Map<String, Object> options) {
        if (options == null) {
            return false;
        }

        // Check for required options
        Map<String, Object> defaults = getDefaultOptions();
        for (String key : defaults.keySet()) {
            if (!options.containsKey(key) && isRequiredOption(key)) {
                logger.error("Missing required option: {}", key);
                return false;
            }
        }

        return validateSpecificOptions(options);
    }

    /**
     * Override this method to provide specific validation logic
     */
    protected abstract boolean validateSpecificOptions(Map<String, Object> options);

    /**
     * Override this method to specify which options are required
     */
    protected abstract boolean isRequiredOption(String optionKey);

    /**
     * Helper method to safely get option value with default fallback
     */
    protected <T> T getOption(Map<String, Object> options, String key, T defaultValue) {
        if (options == null || !options.containsKey(key)) {
            return defaultValue;
        }

        try {
            @SuppressWarnings("unchecked")
            T value = (T) options.get(key);
            return value;
        } catch (ClassCastException e) {
            logger.warn("Invalid type for option {}, using default", key);
            return defaultValue;
        }
    }

    /**
     * Create a standard .gitignore file
     */
    protected String generateGitignore(String... additionalPatterns) {
        StringBuilder sb = new StringBuilder();

        // Common patterns
        sb.append("# IDE files\n");
        sb.append(".idea/\n");
        sb.append(".vscode/\n");
        sb.append("*.iml\n");
        sb.append("*.iws\n");
        sb.append("*.ipr\n\n");

        sb.append("# OS files\n");
        sb.append(".DS_Store\n");
        sb.append("Thumbs.db\n");
        sb.append("*~\n\n");

        sb.append("# Logs\n");
        sb.append("*.log\n");
        sb.append("logs/\n\n");

        sb.append("# Environment files\n");
        sb.append(".env\n");
        sb.append(".env.local\n");
        sb.append(".env.*.local\n\n");

        // Add additional patterns
        if (additionalPatterns != null && additionalPatterns.length > 0) {
            sb.append("# Project specific\n");
            for (String pattern : additionalPatterns) {
                sb.append(pattern).append("\n");
            }
        }

        return sb.toString();
    }

    /**
     * Create a standard README.md file
     */
    protected String generateReadme(String projectName, String description,
                                  String[] features, String[] prerequisites,
                                  Map<String, String> commands) {
        StringBuilder sb = new StringBuilder();

        sb.append("# ").append(projectName).append("\n\n");
        sb.append(description).append("\n\n");

        if (features != null && features.length > 0) {
            sb.append("## Features\n\n");
            for (String feature : features) {
                sb.append("- ").append(feature).append("\n");
            }
            sb.append("\n");
        }

        if (prerequisites != null && prerequisites.length > 0) {
            sb.append("## Prerequisites\n\n");
            for (String prerequisite : prerequisites) {
                sb.append("- ").append(prerequisite).append("\n");
            }
            sb.append("\n");
        }

        sb.append("## Getting Started\n\n");

        sb.append("### Installation\n\n");
        sb.append("```bash\n");
        sb.append("# Clone the repository\n");
        sb.append("git clone <repository-url>\n");
        sb.append("cd ").append(projectName.toLowerCase()).append("\n\n");
        sb.append("# Install dependencies\n");
        String installCmd = commands.getOrDefault("install", "# See documentation for installation");
        sb.append(installCmd).append("\n");
        sb.append("```\n\n");

        if (commands != null && !commands.isEmpty()) {
            sb.append("### Available Commands\n\n");
            for (Map.Entry<String, String> entry : commands.entrySet()) {
                if (!"install".equals(entry.getKey())) {
                    sb.append("#### ").append(capitalize(entry.getKey())).append("\n\n");
                    sb.append("```bash\n");
                    sb.append(entry.getValue()).append("\n");
                    sb.append("```\n\n");
                }
            }
        }

        sb.append("## License\n\n");
        sb.append("This project is licensed under the MIT License.\n");

        return sb.toString();
    }

    /**
     * Helper to create a folder node
     */
    protected ProjectNode createFolder(String name) {
        return new ProjectNode(name, ProjectNode.NodeType.FOLDER);
    }

    /**
     * Helper to create a file node
     */
    protected ProjectNode createFile(String name, String content) {
        return new ProjectNode(name, ProjectNode.NodeType.FILE, content);
    }

    /**
     * Build nested folder structure
     */
    protected ProjectNode buildPath(ProjectNode root, String... folders) {
        ProjectNode current = root;
        for (String folder : folders) {
            ProjectNode child = current.findChild(folder);
            if (child == null) {
                child = createFolder(folder);
                current.addChild(child);
            }
            current = child;
        }
        return current;
    }

    /**
     * Capitalize first letter of string
     */
    protected String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    /**
     * Convert string to camelCase
     */
    protected String toCamelCase(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }

        String[] parts = str.split("[^a-zA-Z0-9]");
        StringBuilder camelCase = new StringBuilder();

        for (int i = 0; i < parts.length; i++) {
            String part = parts[i];
            if (!part.isEmpty()) {
                if (i == 0) {
                    camelCase.append(part.toLowerCase());
                } else {
                    camelCase.append(capitalize(part.toLowerCase()));
                }
            }
        }

        return camelCase.toString();
    }

    /**
     * Convert string to PascalCase
     */
    protected String toPascalCase(String str) {
        String camelCase = toCamelCase(str);
        if (camelCase == null || camelCase.isEmpty()) {
            return camelCase;
        }
        return capitalize(camelCase);
    }

    /**
     * Convert string to snake_case
     */
    protected String toSnakeCase(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }

        return str.replaceAll("([a-z])([A-Z])", "$1_$2")
                  .replaceAll("[^a-zA-Z0-9]", "_")
                  .toLowerCase()
                  .replaceAll("_+", "_")
                  .replaceAll("^_|_$", "");
    }

    /**
     * Convert string to kebab-case
     */
    protected String toKebabCase(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }

        return str.replaceAll("([a-z])([A-Z])", "$1-$2")
                  .replaceAll("[^a-zA-Z0-9]", "-")
                  .toLowerCase()
                  .replaceAll("-+", "-")
                  .replaceAll("^-|-$", "");
    }
}