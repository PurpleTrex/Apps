package com.structurecreation.service;

import java.util.*;

/**
 * Service for managing predefined dependencies for different project types
 */
public class DependencyPresetService {
    
    private static final Map<String, List<DependencyPreset>> PROJECT_DEPENDENCIES = new HashMap<>();
    
    static {
        initializePresets();
    }
    
    public static class DependencyPreset {
        private final String name;
        private final String description;
        private final String type;
        private final String artifact;
        private final String version;
        private final List<String> compatibleVersions;
        private final boolean isRequired;
        
        public DependencyPreset(String name, String description, String type, String artifact, 
                               String version, boolean isRequired, String... compatibleVersions) {
            this.name = name;
            this.description = description;
            this.type = type;
            this.artifact = artifact;
            this.version = version;
            this.isRequired = isRequired;
            this.compatibleVersions = Arrays.asList(compatibleVersions);
        }
        
        // Getters
        public String getName() { return name; }
        public String getDescription() { return description; }
        public String getType() { return type; }
        public String getArtifact() { return artifact; }
        public String getVersion() { return version; }
        public List<String> getCompatibleVersions() { return compatibleVersions; }
        public boolean isRequired() { return isRequired; }
        
        @Override
        public String toString() {
            return name + (isRequired ? " (Required)" : " (Optional)");
        }
    }
    
    private static void initializePresets() {
        // Java Maven dependencies
        PROJECT_DEPENDENCIES.put("Java Maven", Arrays.asList(
            new DependencyPreset("JUnit 5", "Modern testing framework", "Maven", 
                "org.junit.jupiter:junit-jupiter", "5.10.0", false, "5.9.0", "5.10.0", "5.11.0"),
            new DependencyPreset("Spring Boot Starter", "Spring Boot web starter", "Maven", 
                "org.springframework.boot:spring-boot-starter-web", "3.1.4", false, "3.0.0", "3.1.4", "3.2.0"),
            new DependencyPreset("Jackson Core", "JSON processing library", "Maven", 
                "com.fasterxml.jackson.core:jackson-databind", "2.15.2", false, "2.14.0", "2.15.2", "2.16.0"),
            new DependencyPreset("Logback", "Logging framework", "Maven", 
                "ch.qos.logback:logback-classic", "1.4.11", false, "1.4.8", "1.4.11", "1.5.0"),
            new DependencyPreset("Apache Commons Lang", "Common utilities", "Maven", 
                "org.apache.commons:commons-lang3", "3.13.0", false, "3.12.0", "3.13.0", "3.14.0")
        ));
        
        // Java Gradle dependencies
        PROJECT_DEPENDENCIES.put("Java Gradle", Arrays.asList(
            new DependencyPreset("JUnit 5", "Modern testing framework", "Gradle", 
                "org.junit.jupiter:junit-jupiter", "5.10.0", false, "5.9.0", "5.10.0", "5.11.0"),
            new DependencyPreset("Spring Boot Starter", "Spring Boot web starter", "Gradle", 
                "org.springframework.boot:spring-boot-starter-web", "3.1.4", false, "3.0.0", "3.1.4", "3.2.0"),
            new DependencyPreset("Gson", "Google JSON library", "Gradle", 
                "com.google.code.gson:gson", "2.10.1", false, "2.9.0", "2.10.1", "2.11.0")
        ));
        
        // Spring Boot dependencies
        PROJECT_DEPENDENCIES.put("Spring Boot", Arrays.asList(
            new DependencyPreset("Spring Boot Starter Web", "Web development starter", "Maven", 
                "org.springframework.boot:spring-boot-starter-web", "3.1.4", true, "3.0.0", "3.1.4", "3.2.0"),
            new DependencyPreset("Spring Boot Starter Data JPA", "JPA data access", "Maven", 
                "org.springframework.boot:spring-boot-starter-data-jpa", "3.1.4", false, "3.0.0", "3.1.4", "3.2.0"),
            new DependencyPreset("Spring Boot Starter Security", "Security framework", "Maven", 
                "org.springframework.boot:spring-boot-starter-security", "3.1.4", false, "3.0.0", "3.1.4", "3.2.0"),
            new DependencyPreset("Spring Boot DevTools", "Development tools", "Maven", 
                "org.springframework.boot:spring-boot-devtools", "3.1.4", false, "3.0.0", "3.1.4", "3.2.0"),
            new DependencyPreset("H2 Database", "In-memory database", "Maven", 
                "com.h2database:h2", "2.2.224", false, "2.1.214", "2.2.224", "2.3.0"),
            new DependencyPreset("MySQL Connector", "MySQL database driver", "Maven", 
                "mysql:mysql-connector-java", "8.0.33", false, "8.0.30", "8.0.33", "9.0.0")
        ));
        
        // Python dependencies
        PROJECT_DEPENDENCIES.put("Python", Arrays.asList(
            new DependencyPreset("Requests", "HTTP library", "Pip", 
                "requests", "2.31.0", false, "2.28.0", "2.31.0", "2.32.0"),
            new DependencyPreset("NumPy", "Numerical computing", "Pip", 
                "numpy", "1.24.3", false, "1.21.0", "1.24.3", "1.26.0"),
            new DependencyPreset("Pandas", "Data manipulation", "Pip", 
                "pandas", "2.0.3", false, "1.5.0", "2.0.3", "2.1.0"),
            new DependencyPreset("Flask", "Web framework", "Pip", 
                "flask", "2.3.3", false, "2.2.0", "2.3.3", "3.0.0"),
            new DependencyPreset("Django", "Full-featured web framework", "Pip", 
                "django", "4.2.5", false, "4.1.0", "4.2.5", "5.0.0"),
            new DependencyPreset("PyTest", "Testing framework", "Pip", 
                "pytest", "7.4.2", false, "7.1.0", "7.4.2", "8.0.0")
        ));
        
        // Node.js dependencies
        PROJECT_DEPENDENCIES.put("Node.js", Arrays.asList(
            new DependencyPreset("Express", "Web framework", "NPM", 
                "express", "4.18.2", false, "4.17.0", "4.18.2", "5.0.0"),
            new DependencyPreset("Lodash", "Utility library", "NPM", 
                "lodash", "4.17.21", false, "4.17.20", "4.17.21", "5.0.0"),
            new DependencyPreset("Axios", "HTTP client", "NPM", 
                "axios", "1.5.0", false, "1.3.0", "1.5.0", "2.0.0"),
            new DependencyPreset("Moment", "Date manipulation", "NPM", 
                "moment", "2.29.4", false, "2.29.0", "2.29.4", "3.0.0"),
            new DependencyPreset("Jest", "Testing framework", "NPM", 
                "jest", "29.7.0", false, "29.5.0", "29.7.0", "30.0.0")
        ));
        
        // React dependencies
        PROJECT_DEPENDENCIES.put("React", Arrays.asList(
            new DependencyPreset("React", "React library", "NPM", 
                "react", "18.2.0", true, "18.0.0", "18.2.0", "19.0.0"),
            new DependencyPreset("React DOM", "React DOM renderer", "NPM", 
                "react-dom", "18.2.0", true, "18.0.0", "18.2.0", "19.0.0"),
            new DependencyPreset("React Router", "Client-side routing", "NPM", 
                "react-router-dom", "6.15.0", false, "6.8.0", "6.15.0", "7.0.0"),
            new DependencyPreset("Styled Components", "CSS-in-JS styling", "NPM", 
                "styled-components", "6.0.7", false, "5.3.0", "6.0.7", "7.0.0"),
            new DependencyPreset("Material-UI", "React UI framework", "NPM", 
                "@mui/material", "5.14.5", false, "5.10.0", "5.14.5", "6.0.0"),
            new DependencyPreset("React Testing Library", "Testing utilities", "NPM", 
                "@testing-library/react", "13.4.0", false, "13.0.0", "13.4.0", "14.0.0")
        ));
        
        // Django dependencies
        PROJECT_DEPENDENCIES.put("Django", Arrays.asList(
            new DependencyPreset("Django", "Django framework", "Pip", 
                "django", "4.2.5", true, "4.1.0", "4.2.5", "5.0.0"),
            new DependencyPreset("Django REST Framework", "API framework", "Pip", 
                "djangorestframework", "3.14.0", false, "3.12.0", "3.14.0", "4.0.0"),
            new DependencyPreset("Django CORS Headers", "CORS handling", "Pip", 
                "django-cors-headers", "4.3.0", false, "4.0.0", "4.3.0", "5.0.0"),
            new DependencyPreset("Pillow", "Image processing", "Pip", 
                "pillow", "10.0.0", false, "9.5.0", "10.0.0", "11.0.0"),
            new DependencyPreset("psycopg2", "PostgreSQL adapter", "Pip", 
                "psycopg2-binary", "2.9.7", false, "2.9.5", "2.9.7", "3.0.0")
        ));
        
        // Flask dependencies
        PROJECT_DEPENDENCIES.put("Flask", Arrays.asList(
            new DependencyPreset("Flask", "Flask framework", "Pip", 
                "flask", "2.3.3", true, "2.2.0", "2.3.3", "3.0.0"),
            new DependencyPreset("Flask-SQLAlchemy", "SQLAlchemy integration", "Pip", 
                "flask-sqlalchemy", "3.0.5", false, "3.0.0", "3.0.5", "4.0.0"),
            new DependencyPreset("Flask-Login", "User session management", "Pip", 
                "flask-login", "0.6.2", false, "0.6.0", "0.6.2", "0.7.0"),
            new DependencyPreset("Flask-WTF", "Form handling", "Pip", 
                "flask-wtf", "1.1.1", false, "1.0.0", "1.1.1", "2.0.0")
        ));
    }
    
    /**
     * Get predefined dependencies for a project type
     */
    public static List<DependencyPreset> getPresetsForProjectType(String projectType) {
        return PROJECT_DEPENDENCIES.getOrDefault(projectType, new ArrayList<>());
    }
    
    /**
     * Check if dependencies are compatible with each other
     */
    public static boolean areCompatible(List<DependencyPreset> selectedDependencies) {
        // Basic compatibility check - can be enhanced
        Set<String> types = new HashSet<>();
        for (DependencyPreset preset : selectedDependencies) {
            types.add(preset.getType());
        }
        
        // For now, just check if all dependencies use the same package manager
        return types.size() <= 1;
    }
    
    /**
     * Get recommended version for a dependency
     */
    public static String getRecommendedVersion(String projectType, String dependencyName) {
        List<DependencyPreset> presets = getPresetsForProjectType(projectType);
        return presets.stream()
            .filter(preset -> preset.getName().equalsIgnoreCase(dependencyName))
            .findFirst()
            .map(DependencyPreset::getVersion)
            .orElse("latest");
    }
    
    /**
     * Validate if a custom dependency is compatible with project type
     */
    public static boolean isCompatibleWithProjectType(String projectType, String dependencyType) {
        switch (projectType.toLowerCase()) {
            case "java maven":
            case "spring boot":
                return "Maven".equalsIgnoreCase(dependencyType);
            case "java gradle":
                return "Gradle".equalsIgnoreCase(dependencyType);
            case "python":
            case "django":
            case "flask":
                return "Pip".equalsIgnoreCase(dependencyType);
            case "node.js":
            case "react":
            case "vue.js":
            case "angular":
                return "NPM".equalsIgnoreCase(dependencyType) || "Yarn".equalsIgnoreCase(dependencyType);
            default:
                return true; // Custom projects allow any dependency type
        }
    }
}