package com.structurecreation.service.generator;

import com.structurecreation.model.ProjectNode;
import com.structurecreation.service.repository.NpmRepository;
import com.structurecreation.service.repository.PyPiRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Service that manages all project generators and provides a unified interface
 * for generating different types of projects
 */
@Service
public class GeneratorService {
    private static final Logger logger = LoggerFactory.getLogger(GeneratorService.class);

    private final SpringBootProjectGenerator springBootGenerator;
    private final ReactProjectGenerator reactGenerator;
    private final NodeExpressProjectGenerator nodeExpressGenerator;
    private final DjangoProjectGenerator djangoGenerator;
    private final FlaskProjectGenerator flaskGenerator;

    public GeneratorService() {
        this.springBootGenerator = new SpringBootProjectGenerator();
        this.reactGenerator = new ReactProjectGenerator();
        this.nodeExpressGenerator = new NodeExpressProjectGenerator();
        this.djangoGenerator = new DjangoProjectGenerator();
        this.flaskGenerator = new FlaskProjectGenerator();
    }

    /**
     * Available project types that can be generated
     */
    public enum ProjectType {
        // Java Projects
        SPRING_BOOT_BASIC("Spring Boot - Basic REST API", "Java"),
        SPRING_BOOT_WEB("Spring Boot - Web Application", "Java"),
        SPRING_BOOT_MICROSERVICE("Spring Boot - Microservice", "Java"),
        SPRING_BOOT_FULL_STACK("Spring Boot - Full Stack", "Java"),

        // JavaScript/TypeScript Projects
        REACT_BASIC("React - Basic Application", "JavaScript"),
        REACT_REDUX("React - With Redux", "JavaScript"),
        REACT_CONTEXT("React - With Context API", "JavaScript"),
        REACT_TYPESCRIPT("React - TypeScript", "TypeScript"),
        REACT_FULL_STACK("React - Full Stack", "JavaScript"),

        NODE_EXPRESS_BASIC("Node.js - Express API", "JavaScript"),
        NODE_EXPRESS_TYPESCRIPT("Node.js - TypeScript Express", "TypeScript"),
        NODE_EXPRESS_MVC("Node.js - MVC Pattern", "JavaScript"),
        NODE_EXPRESS_GRAPHQL("Node.js - GraphQL API", "JavaScript"),
        NODE_EXPRESS_REALTIME("Node.js - Real-time with WebSockets", "JavaScript"),
        NODE_EXPRESS_MICROSERVICE("Node.js - Microservice", "JavaScript"),
        NODE_EXPRESS_FULL_STACK("Node.js - Full Stack", "JavaScript"),

        // Python Projects
        DJANGO_REST_API("Django - REST API", "Python"),
        DJANGO_FULL_STACK("Django - Full Stack Web", "Python"),
        DJANGO_MICROSERVICE("Django - Microservice", "Python"),
        DJANGO_CMS("Django - CMS", "Python"),
        DJANGO_ECOMMERCE("Django - E-commerce", "Python"),
        DJANGO_ANALYTICS("Django - Analytics Dashboard", "Python"),
        DJANGO_ML("Django - Machine Learning", "Python"),

        FLASK_BASIC("Flask - Basic API", "Python"),
        FLASK_REST_API("Flask - RESTful API", "Python"),
        FLASK_MICROSERVICE("Flask - Microservice", "Python"),
        FLASK_WEB("Flask - Web Application", "Python"),
        FLASK_ML("Flask - ML API", "Python"),
        FLASK_FULL_STACK("Flask - Full Stack", "Python");

        private final String displayName;
        private final String language;

        ProjectType(String displayName, String language) {
            this.displayName = displayName;
            this.language = language;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getLanguage() {
            return language;
        }
    }

    /**
     * Generate a project based on the selected type
     *
     * @param projectName The name of the project
     * @param projectType The type of project to generate
     * @param options Additional options for project generation
     * @return The generated project structure
     */
    public ProjectNode generateProject(String projectName, ProjectType projectType, Map<String, Object> options) {
        logger.info("Generating {} project: {}", projectType.getDisplayName(), projectName);

        try {
            ProjectNode generatedProject = null;

            switch (projectType) {
                // Spring Boot Projects
                case SPRING_BOOT_BASIC:
                    generatedProject = springBootGenerator.generateSpringBootProject(
                        projectName,
                        getOption(options, "groupId", "com.example"),
                        SpringBootProjectGenerator.SpringBootProjectType.BASIC
                    );
                    break;
                case SPRING_BOOT_WEB:
                    generatedProject = springBootGenerator.generateSpringBootProject(
                        projectName,
                        getOption(options, "groupId", "com.example"),
                        SpringBootProjectGenerator.SpringBootProjectType.WEB
                    );
                    break;
                case SPRING_BOOT_MICROSERVICE:
                    generatedProject = springBootGenerator.generateSpringBootProject(
                        projectName,
                        getOption(options, "groupId", "com.example"),
                        SpringBootProjectGenerator.SpringBootProjectType.MICROSERVICE
                    );
                    break;
                case SPRING_BOOT_FULL_STACK:
                    generatedProject = springBootGenerator.generateSpringBootProject(
                        projectName,
                        getOption(options, "groupId", "com.example"),
                        SpringBootProjectGenerator.SpringBootProjectType.FULL_STACK
                    );
                    break;

                // React Projects
                case REACT_BASIC:
                    generatedProject = reactGenerator.generateReactProject(
                        projectName,
                        ReactProjectGenerator.ReactProjectType.BASIC
                    );
                    break;
                case REACT_REDUX:
                    generatedProject = reactGenerator.generateReactProject(
                        projectName,
                        ReactProjectGenerator.ReactProjectType.WITH_REDUX
                    );
                    break;
                case REACT_CONTEXT:
                    generatedProject = reactGenerator.generateReactProject(
                        projectName,
                        ReactProjectGenerator.ReactProjectType.WITH_CONTEXT
                    );
                    break;
                case REACT_TYPESCRIPT:
                    generatedProject = reactGenerator.generateReactProject(
                        projectName,
                        ReactProjectGenerator.ReactProjectType.WITH_TYPESCRIPT
                    );
                    break;
                case REACT_FULL_STACK:
                    generatedProject = reactGenerator.generateReactProject(
                        projectName,
                        ReactProjectGenerator.ReactProjectType.FULL_STACK
                    );
                    break;

                // Node Express Projects
                case NODE_EXPRESS_BASIC:
                    generatedProject = nodeExpressGenerator.generateNodeExpressProject(
                        projectName,
                        NodeExpressProjectGenerator.NodeProjectType.BASIC
                    );
                    break;
                case NODE_EXPRESS_TYPESCRIPT:
                    generatedProject = nodeExpressGenerator.generateNodeExpressProject(
                        projectName,
                        NodeExpressProjectGenerator.NodeProjectType.TYPESCRIPT
                    );
                    break;
                case NODE_EXPRESS_MVC:
                    generatedProject = nodeExpressGenerator.generateNodeExpressProject(
                        projectName,
                        NodeExpressProjectGenerator.NodeProjectType.MVC
                    );
                    break;
                case NODE_EXPRESS_GRAPHQL:
                    generatedProject = nodeExpressGenerator.generateNodeExpressProject(
                        projectName,
                        NodeExpressProjectGenerator.NodeProjectType.GRAPHQL
                    );
                    break;
                case NODE_EXPRESS_REALTIME:
                    generatedProject = nodeExpressGenerator.generateNodeExpressProject(
                        projectName,
                        NodeExpressProjectGenerator.NodeProjectType.REAL_TIME
                    );
                    break;
                case NODE_EXPRESS_MICROSERVICE:
                    generatedProject = nodeExpressGenerator.generateNodeExpressProject(
                        projectName,
                        NodeExpressProjectGenerator.NodeProjectType.MICROSERVICE
                    );
                    break;
                case NODE_EXPRESS_FULL_STACK:
                    generatedProject = nodeExpressGenerator.generateNodeExpressProject(
                        projectName,
                        NodeExpressProjectGenerator.NodeProjectType.FULL_STACK
                    );
                    break;

                // Django Projects
                case DJANGO_REST_API:
                    generatedProject = djangoGenerator.generateDjangoProject(
                        projectName,
                        DjangoProjectGenerator.DjangoProjectType.REST_API
                    );
                    break;
                case DJANGO_FULL_STACK:
                    generatedProject = djangoGenerator.generateDjangoProject(
                        projectName,
                        DjangoProjectGenerator.DjangoProjectType.FULL_STACK
                    );
                    break;
                case DJANGO_MICROSERVICE:
                    generatedProject = djangoGenerator.generateDjangoProject(
                        projectName,
                        DjangoProjectGenerator.DjangoProjectType.MICROSERVICE
                    );
                    break;
                case DJANGO_CMS:
                    generatedProject = djangoGenerator.generateDjangoProject(
                        projectName,
                        DjangoProjectGenerator.DjangoProjectType.CMS
                    );
                    break;
                case DJANGO_ECOMMERCE:
                    generatedProject = djangoGenerator.generateDjangoProject(
                        projectName,
                        DjangoProjectGenerator.DjangoProjectType.ECOMMERCE
                    );
                    break;
                case DJANGO_ANALYTICS:
                    generatedProject = djangoGenerator.generateDjangoProject(
                        projectName,
                        DjangoProjectGenerator.DjangoProjectType.ANALYTICS
                    );
                    break;
                case DJANGO_ML:
                    generatedProject = djangoGenerator.generateDjangoProject(
                        projectName,
                        DjangoProjectGenerator.DjangoProjectType.MACHINE_LEARNING
                    );
                    break;

                // Flask Projects
                case FLASK_BASIC:
                    generatedProject = flaskGenerator.generateFlaskProject(
                        projectName,
                        FlaskProjectGenerator.FlaskProjectType.BASIC
                    );
                    break;
                case FLASK_REST_API:
                    generatedProject = flaskGenerator.generateFlaskProject(
                        projectName,
                        FlaskProjectGenerator.FlaskProjectType.REST_API
                    );
                    break;
                case FLASK_MICROSERVICE:
                    generatedProject = flaskGenerator.generateFlaskProject(
                        projectName,
                        FlaskProjectGenerator.FlaskProjectType.MICROSERVICE
                    );
                    break;
                case FLASK_WEB:
                    generatedProject = flaskGenerator.generateFlaskProject(
                        projectName,
                        FlaskProjectGenerator.FlaskProjectType.WEB_APP
                    );
                    break;
                case FLASK_ML:
                    generatedProject = flaskGenerator.generateFlaskProject(
                        projectName,
                        FlaskProjectGenerator.FlaskProjectType.ML_API
                    );
                    break;
                case FLASK_FULL_STACK:
                    generatedProject = flaskGenerator.generateFlaskProject(
                        projectName,
                        FlaskProjectGenerator.FlaskProjectType.FULL_STACK
                    );
                    break;

                default:
                    throw new IllegalArgumentException("Unsupported project type: " + projectType);
            }

            logger.info("Successfully generated {} project", projectType.getDisplayName());
            return generatedProject;

        } catch (Exception e) {
            logger.error("Error generating project: ", e);
            throw new RuntimeException("Failed to generate project: " + e.getMessage(), e);
        }
    }

    /**
     * Get available project types grouped by language
     *
     * @return Map of language to list of project types
     */
    public Map<String, List<ProjectType>> getProjectTypesByLanguage() {
        Map<String, List<ProjectType>> typesByLanguage = new LinkedHashMap<>();

        for (ProjectType type : ProjectType.values()) {
            typesByLanguage
                .computeIfAbsent(type.getLanguage(), k -> new ArrayList<>())
                .add(type);
        }

        return typesByLanguage;
    }

    /**
     * Get all available project types
     *
     * @return List of all project types
     */
    public List<ProjectType> getAllProjectTypes() {
        return Arrays.asList(ProjectType.values());
    }

    /**
     * Get project types for a specific language
     *
     * @param language The programming language
     * @return List of project types for the language
     */
    public List<ProjectType> getProjectTypesForLanguage(String language) {
        List<ProjectType> types = new ArrayList<>();
        for (ProjectType type : ProjectType.values()) {
            if (type.getLanguage().equalsIgnoreCase(language)) {
                types.add(type);
            }
        }
        return types;
    }

    /**
     * Get recommended project type based on requirements
     *
     * @param requirements Map of requirements
     * @return Recommended project type
     */
    public ProjectType recommendProjectType(Map<String, Object> requirements) {
        String language = getOption(requirements, "language", "Java");
        String purpose = getOption(requirements, "purpose", "api");
        boolean needsDatabase = getOption(requirements, "database", false);
        boolean needsAuth = getOption(requirements, "authentication", false);
        boolean needsRealtime = getOption(requirements, "realtime", false);

        // Basic recommendation logic
        if (language.equalsIgnoreCase("Java")) {
            if (purpose.contains("microservice")) {
                return ProjectType.SPRING_BOOT_MICROSERVICE;
            } else if (purpose.contains("web")) {
                return ProjectType.SPRING_BOOT_WEB;
            } else if (needsDatabase && needsAuth) {
                return ProjectType.SPRING_BOOT_FULL_STACK;
            } else {
                return ProjectType.SPRING_BOOT_BASIC;
            }
        } else if (language.equalsIgnoreCase("JavaScript") || language.equalsIgnoreCase("TypeScript")) {
            if (purpose.contains("frontend") || purpose.contains("ui")) {
                if (needsAuth) {
                    return ProjectType.REACT_FULL_STACK;
                } else {
                    return ProjectType.REACT_BASIC;
                }
            } else if (needsRealtime) {
                return ProjectType.NODE_EXPRESS_REALTIME;
            } else if (purpose.contains("graphql")) {
                return ProjectType.NODE_EXPRESS_GRAPHQL;
            } else if (purpose.contains("microservice")) {
                return ProjectType.NODE_EXPRESS_MICROSERVICE;
            } else {
                return ProjectType.NODE_EXPRESS_BASIC;
            }
        } else if (language.equalsIgnoreCase("Python")) {
            if (purpose.contains("ml") || purpose.contains("machine learning")) {
                return needsDatabase ? ProjectType.DJANGO_ML : ProjectType.FLASK_ML;
            } else if (purpose.contains("cms")) {
                return ProjectType.DJANGO_CMS;
            } else if (purpose.contains("ecommerce")) {
                return ProjectType.DJANGO_ECOMMERCE;
            } else if (purpose.contains("microservice")) {
                return ProjectType.FLASK_MICROSERVICE;
            } else if (needsDatabase && needsAuth) {
                return ProjectType.DJANGO_FULL_STACK;
            } else {
                return ProjectType.FLASK_BASIC;
            }
        }

        // Default fallback
        return ProjectType.SPRING_BOOT_BASIC;
    }

    /**
     * Validate project name
     *
     * @param projectName The project name to validate
     * @return true if valid, false otherwise
     */
    public boolean validateProjectName(String projectName) {
        if (projectName == null || projectName.trim().isEmpty()) {
            return false;
        }

        // Check for valid characters (alphanumeric, dash, underscore)
        if (!projectName.matches("^[a-zA-Z][a-zA-Z0-9_-]*$")) {
            return false;
        }

        // Check length
        if (projectName.length() < 2 || projectName.length() > 50) {
            return false;
        }

        return true;
    }

    /**
     * Get default options for a project type
     *
     * @param projectType The project type
     * @return Map of default options
     */
    public Map<String, Object> getDefaultOptions(ProjectType projectType) {
        Map<String, Object> defaults = new HashMap<>();

        // Add language-specific defaults
        switch (projectType.getLanguage()) {
            case "Java":
                defaults.put("groupId", "com.example");
                defaults.put("artifactId", "demo");
                defaults.put("version", "1.0.0-SNAPSHOT");
                defaults.put("javaVersion", "17");
                break;
            case "JavaScript":
            case "TypeScript":
                defaults.put("version", "1.0.0");
                defaults.put("nodeVersion", "18");
                defaults.put("packageManager", "npm");
                break;
            case "Python":
                defaults.put("pythonVersion", "3.11");
                defaults.put("virtualEnv", true);
                break;
        }

        // Add type-specific defaults
        if (projectType.name().contains("MICROSERVICE")) {
            defaults.put("docker", true);
            defaults.put("kubernetes", true);
        }

        if (projectType.name().contains("FULL_STACK")) {
            defaults.put("database", "postgresql");
            defaults.put("cache", "redis");
        }

        return defaults;
    }

    /**
     * Helper method to get option value with default
     */
    @SuppressWarnings("unchecked")
    private <T> T getOption(Map<String, Object> options, String key, T defaultValue) {
        if (options == null || !options.containsKey(key)) {
            return defaultValue;
        }
        try {
            return (T) options.get(key);
        } catch (ClassCastException e) {
            logger.warn("Invalid type for option {}, using default", key);
            return defaultValue;
        }
    }
}