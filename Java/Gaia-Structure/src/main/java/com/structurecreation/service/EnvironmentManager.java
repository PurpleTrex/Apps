package com.structurecreation.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Service for detecting and managing development environments
 * Detects installed tools like Java, Maven, Gradle, Node.js, Python, etc.
 */
public class EnvironmentManager {
    
    private static final Logger logger = LoggerFactory.getLogger(EnvironmentManager.class);
    
    private static final Map<String, EnvironmentInfo> cachedEnvironments = new HashMap<>();
    private static boolean cacheInitialized = false;
    
    /**
     * Represents information about a development environment
     */
    public static class EnvironmentInfo {
        private final String name;
        private final String version;
        private final String path;
        private final boolean available;
        
        public EnvironmentInfo(String name, String version, String path, boolean available) {
            this.name = name;
            this.version = version;
            this.path = path;
            this.available = available;
        }
        
        public String getName() { return name; }
        public String getVersion() { return version; }
        public String getPath() { return path; }
        public boolean isAvailable() { return available; }
        
        @Override
        public String toString() {
            if (!available) {
                return name + " (Not Available)";
            }
            return name + " " + version + (path != null ? " (" + path + ")" : "");
        }
    }
    
    /**
     * Initialize and cache all environment information
     */
    public static void initializeEnvironments() {
        if (cacheInitialized) {
            return;
        }
        
        logger.info("Initializing environment detection...");
        
        // Detect various environments
        cachedEnvironments.put("Java", detectJava());
        cachedEnvironments.put("Maven", detectMaven());
        cachedEnvironments.put("Gradle", detectGradle());
        cachedEnvironments.put("Node.js", detectNodeJs());
        cachedEnvironments.put("NPM", detectNpm());
        cachedEnvironments.put("Python", detectPython());
        cachedEnvironments.put("Pip", detectPip());
        cachedEnvironments.put("Git", detectGit());
        
        cacheInitialized = true;
        logger.info("Environment detection completed");
    }
    
    /**
     * Get environment information for a specific tool
     */
    public static EnvironmentInfo getEnvironment(String toolName) {
        if (!cacheInitialized) {
            initializeEnvironments();
        }
        return cachedEnvironments.getOrDefault(toolName, 
            new EnvironmentInfo(toolName, "Unknown", null, false));
    }
    
    /**
     * Get all detected environments
     */
    public static Map<String, EnvironmentInfo> getAllEnvironments() {
        if (!cacheInitialized) {
            initializeEnvironments();
        }
        return new HashMap<>(cachedEnvironments);
    }
    
    /**
     * Check if a specific tool is available
     */
    public static boolean isAvailable(String toolName) {
        return getEnvironment(toolName).isAvailable();
    }
    
    /**
     * Get recommended tools for a project type
     */
    public static List<String> getRequiredToolsForProjectType(String projectType) {
        List<String> tools = new ArrayList<>();
        
        switch (projectType.toLowerCase()) {
            case "java maven":
            case "spring boot":
                tools.add("Java");
                tools.add("Maven");
                break;
            case "java gradle":
                tools.add("Java");
                tools.add("Gradle");
                break;
            case "python":
            case "django":
            case "flask":
                tools.add("Python");
                tools.add("Pip");
                break;
            case "node.js":
            case "react":
            case "vue.js":
            case "angular":
                tools.add("Node.js");
                tools.add("NPM");
                break;
        }
        
        return tools;
    }
    
    /**
     * Check if all required tools are available for a project type
     */
    public static boolean areRequiredToolsAvailable(String projectType) {
        List<String> requiredTools = getRequiredToolsForProjectType(projectType);
        for (String tool : requiredTools) {
            if (!isAvailable(tool)) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Get missing tools for a project type
     */
    public static List<String> getMissingTools(String projectType) {
        List<String> missingTools = new ArrayList<>();
        List<String> requiredTools = getRequiredToolsForProjectType(projectType);
        
        for (String tool : requiredTools) {
            if (!isAvailable(tool)) {
                missingTools.add(tool);
            }
        }
        
        return missingTools;
    }
    
    // Detection methods for various tools
    
    private static EnvironmentInfo detectJava() {
        try {
            String version = executeCommand("java", "-version");
            if (version != null && version.contains("version")) {
                String javaVersion = extractVersion(version);
                String javaHome = System.getenv("JAVA_HOME");
                return new EnvironmentInfo("Java", javaVersion, javaHome, true);
            }
        } catch (Exception e) {
            logger.debug("Java not detected: {}", e.getMessage());
        }
        return new EnvironmentInfo("Java", null, null, false);
    }
    
    private static EnvironmentInfo detectMaven() {
        try {
            String version = executeCommand("mvn", "--version");
            if (version != null && version.contains("Maven")) {
                String mavenVersion = extractVersion(version);
                return new EnvironmentInfo("Maven", mavenVersion, null, true);
            }
        } catch (Exception e) {
            logger.debug("Maven not detected: {}", e.getMessage());
        }
        return new EnvironmentInfo("Maven", null, null, false);
    }
    
    private static EnvironmentInfo detectGradle() {
        try {
            String version = executeCommand("gradle", "--version");
            if (version != null && version.contains("Gradle")) {
                String gradleVersion = extractVersion(version);
                return new EnvironmentInfo("Gradle", gradleVersion, null, true);
            }
        } catch (Exception e) {
            logger.debug("Gradle not detected: {}", e.getMessage());
        }
        return new EnvironmentInfo("Gradle", null, null, false);
    }
    
    private static EnvironmentInfo detectNodeJs() {
        try {
            String version = executeCommand("node", "--version");
            if (version != null && version.startsWith("v")) {
                return new EnvironmentInfo("Node.js", version.trim(), null, true);
            }
        } catch (Exception e) {
            logger.debug("Node.js not detected: {}", e.getMessage());
        }
        return new EnvironmentInfo("Node.js", null, null, false);
    }
    
    private static EnvironmentInfo detectNpm() {
        try {
            String version = executeCommand("npm", "--version");
            if (version != null && !version.isEmpty()) {
                return new EnvironmentInfo("NPM", version.trim(), null, true);
            }
        } catch (Exception e) {
            logger.debug("NPM not detected: {}", e.getMessage());
        }
        return new EnvironmentInfo("NPM", null, null, false);
    }
    
    private static EnvironmentInfo detectPython() {
        try {
            // Try python3 first
            String version = executeCommand("python3", "--version");
            if (version != null && version.contains("Python")) {
                String pythonVersion = extractVersion(version);
                return new EnvironmentInfo("Python", pythonVersion, null, true);
            }
        } catch (Exception e) {
            logger.debug("Python3 not detected, trying python: {}", e.getMessage());
        }
        
        try {
            // Try python
            String version = executeCommand("python", "--version");
            if (version != null && version.contains("Python")) {
                String pythonVersion = extractVersion(version);
                return new EnvironmentInfo("Python", pythonVersion, null, true);
            }
        } catch (Exception e) {
            logger.debug("Python not detected: {}", e.getMessage());
        }
        
        return new EnvironmentInfo("Python", null, null, false);
    }
    
    private static EnvironmentInfo detectPip() {
        try {
            // Try pip3 first
            String version = executeCommand("pip3", "--version");
            if (version != null && version.contains("pip")) {
                String pipVersion = extractVersion(version);
                return new EnvironmentInfo("Pip", pipVersion, null, true);
            }
        } catch (Exception e) {
            logger.debug("Pip3 not detected, trying pip: {}", e.getMessage());
        }
        
        try {
            // Try pip
            String version = executeCommand("pip", "--version");
            if (version != null && version.contains("pip")) {
                String pipVersion = extractVersion(version);
                return new EnvironmentInfo("Pip", pipVersion, null, true);
            }
        } catch (Exception e) {
            logger.debug("Pip not detected: {}", e.getMessage());
        }
        
        return new EnvironmentInfo("Pip", null, null, false);
    }
    
    private static EnvironmentInfo detectGit() {
        try {
            String version = executeCommand("git", "--version");
            if (version != null && version.contains("git")) {
                String gitVersion = extractVersion(version);
                return new EnvironmentInfo("Git", gitVersion, null, true);
            }
        } catch (Exception e) {
            logger.debug("Git not detected: {}", e.getMessage());
        }
        return new EnvironmentInfo("Git", null, null, false);
    }
    
    /**
     * Execute a command and return its output
     */
    private static String executeCommand(String... command) {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder(command);
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();
            
            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
            }
            
            process.waitFor();
            return output.toString();
        } catch (Exception e) {
            logger.trace("Command execution failed: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * Extract version number from version string
     */
    private static String extractVersion(String versionString) {
        if (versionString == null) {
            return "Unknown";
        }
        
        // Extract version pattern like "1.2.3" or "v1.2.3"
        String[] patterns = {
            "\\d+\\.\\d+\\.\\d+",
            "v\\d+\\.\\d+\\.\\d+",
            "\\d+\\.\\d+"
        };
        
        for (String pattern : patterns) {
            java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
            java.util.regex.Matcher m = p.matcher(versionString);
            if (m.find()) {
                return m.group();
            }
        }
        
        // If no pattern matched, return the first line
        String[] lines = versionString.split("\n");
        if (lines.length > 0) {
            return lines[0].trim();
        }
        
        return "Unknown";
    }
    
    /**
     * Clear the environment cache (useful for testing or refreshing)
     */
    public static void clearCache() {
        cachedEnvironments.clear();
        cacheInitialized = false;
    }
}
