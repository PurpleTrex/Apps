package com.structurecreation.service;

import com.structurecreation.model.ProjectStructure;
import com.structurecreation.model.ProjectNode;
import com.structurecreation.model.Dependency;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * Service responsible for creating project structures and installing dependencies
 */
public class ProjectCreationService {

    private static final Logger logger = LoggerFactory.getLogger(ProjectCreationService.class);
    
    private final Map<String, ProjectTemplateGenerator> templateGenerators;
    
    public ProjectCreationService() {
        this.templateGenerators = new HashMap<>();
        initializeTemplateGenerators();
    }
    
    private void initializeTemplateGenerators() {
        templateGenerators.put("Java Maven", new MavenProjectGenerator());
        templateGenerators.put("Java Gradle", new GradleProjectGenerator());
        templateGenerators.put("Python", new PythonProjectGenerator());
        templateGenerators.put("Node.js", new NodeJsProjectGenerator());
        templateGenerators.put("React", new ReactProjectGenerator());
        templateGenerators.put("Spring Boot", new SpringBootProjectGenerator());
        templateGenerators.put("Custom", new CustomProjectGenerator());
    }
    
    /**
     * Creates the complete project structure including directories, files, and configuration
     */
    public void createProjectStructure(ProjectStructure projectStructure) throws IOException {
        logger.info("Creating project structure for: {}", projectStructure.getProjectName());
        
        // Create base project directory
        Path projectPath = createProjectDirectory(projectStructure);
        
        // Generate template-specific files first
        ProjectTemplateGenerator generator = templateGenerators.get(projectStructure.getProjectType());
        if (generator != null) {
            generator.generateTemplate(projectStructure, projectPath);
        }
        
        // Create custom directory structure
        createDirectoryStructure(projectStructure.getRootNode(), projectPath);
        
        logger.info("Project structure created successfully at: {}", projectPath);
    }
    
    /**
     * Installs all dependencies specified in the project structure
     */
    public void installDependencies(ProjectStructure projectStructure) throws IOException, InterruptedException {
        logger.info("Installing dependencies for project: {}", projectStructure.getProjectName());
        
        if (projectStructure.getDependencies() == null || projectStructure.getDependencies().isEmpty()) {
            logger.info("No dependencies to install");
            return;
        }
        
        Path projectPath = Paths.get(projectStructure.getFullProjectPath());
        
        // Group dependencies by type
        Map<String, java.util.List<Dependency>> dependenciesByType = new HashMap<>();
        for (Dependency dep : projectStructure.getDependencies()) {
            dependenciesByType.computeIfAbsent(dep.getType().toLowerCase(), k -> new java.util.ArrayList<>()).add(dep);
        }
        
        // Install each type of dependency
        for (Map.Entry<String, java.util.List<Dependency>> entry : dependenciesByType.entrySet()) {
            installDependenciesByType(entry.getKey(), entry.getValue(), projectPath);
        }
        
        logger.info("Dependencies installed successfully");
    }
    
    private Path createProjectDirectory(ProjectStructure projectStructure) throws IOException {
        Path projectPath = Paths.get(projectStructure.getFullProjectPath());
        
        if (Files.exists(projectPath)) {
            logger.warn("Project directory already exists: {}", projectPath);
            // Don't throw exception, just continue
        } else {
            Files.createDirectories(projectPath);
            logger.info("Created project directory: {}", projectPath);
        }
        
        return projectPath;
    }
    
    private void createDirectoryStructure(ProjectNode node, Path currentPath) throws IOException {
        for (ProjectNode child : node.getChildren()) {
            Path childPath = currentPath.resolve(child.getName());
            
            if (child.isFolder()) {
                // Create directory
                Files.createDirectories(childPath);
                logger.debug("Created directory: {}", childPath);
                
                // Recursively create children
                createDirectoryStructure(child, childPath);
            } else {
                // Create file
                createFile(childPath, child.getContent());
                logger.debug("Created file: {}", childPath);
            }
        }
    }
    
    private void createFile(Path filePath, String content) throws IOException {
        // Ensure parent directory exists
        Files.createDirectories(filePath.getParent());
        
        // Write file with content
        if (content != null && !content.trim().isEmpty()) {
            Files.write(filePath, content.getBytes());
        } else {
            // Create empty file
            Files.createFile(filePath);
        }
    }
    
    private void installDependenciesByType(String type, java.util.List<Dependency> dependencies, Path projectPath) 
            throws IOException, InterruptedException {
        
        logger.info("Installing {} dependencies: {}", type, dependencies.size());
        
        switch (type) {
            case "maven":
                installMavenDependencies(dependencies, projectPath);
                break;
            case "gradle":
                installGradleDependencies(dependencies, projectPath);
                break;
            case "npm":
                installNpmDependencies(dependencies, projectPath);
                break;
            case "yarn":
                installYarnDependencies(dependencies, projectPath);
                break;
            case "pip":
                installPipDependencies(dependencies, projectPath);
                break;
            case "nuget":
                installNuGetDependencies(dependencies, projectPath);
                break;
            case "composer":
                installComposerDependencies(dependencies, projectPath);
                break;
            case "gem":
                installGemDependencies(dependencies, projectPath);
                break;
            case "go modules":
                installGoModulesDependencies(dependencies, projectPath);
                break;
            default:
                logger.warn("Unknown dependency type: {}", type);
        }
    }
    
    private void installMavenDependencies(java.util.List<Dependency> dependencies, Path projectPath) 
            throws IOException, InterruptedException {
        
        // Maven dependencies are handled in pom.xml generation
        // We can run maven install if pom.xml exists
        Path pomPath = projectPath.resolve("pom.xml");
        if (Files.exists(pomPath)) {
            executeCommand(projectPath, "mvn", "clean", "install", "-DskipTests");
        }
    }
    
    private void installGradleDependencies(java.util.List<Dependency> dependencies, Path projectPath) 
            throws IOException, InterruptedException {
        
        // Gradle dependencies are handled in build.gradle generation
        Path buildGradlePath = projectPath.resolve("build.gradle");
        if (Files.exists(buildGradlePath)) {
            executeCommand(projectPath, "gradle", "build", "--exclude-task", "test");
        }
    }
    
    private void installNpmDependencies(java.util.List<Dependency> dependencies, Path projectPath) 
            throws IOException, InterruptedException {
        
        // Check if package.json exists
        Path packageJsonPath = projectPath.resolve("package.json");
        if (Files.exists(packageJsonPath)) {
            executeCommand(projectPath, "npm", "install");
        } else {
            // Install individual packages
            for (Dependency dep : dependencies) {
                String packageSpec = dep.getName();
                if (!dep.getVersion().equals("latest")) {
                    packageSpec += "@" + dep.getVersion();
                }
                executeCommand(projectPath, "npm", "install", packageSpec);
            }
        }
    }
    
    private void installYarnDependencies(java.util.List<Dependency> dependencies, Path projectPath) 
            throws IOException, InterruptedException {
        
        Path packageJsonPath = projectPath.resolve("package.json");
        if (Files.exists(packageJsonPath)) {
            executeCommand(projectPath, "yarn", "install");
        } else {
            for (Dependency dep : dependencies) {
                String packageSpec = dep.getName();
                if (!dep.getVersion().equals("latest")) {
                    packageSpec += "@" + dep.getVersion();
                }
                executeCommand(projectPath, "yarn", "add", packageSpec);
            }
        }
    }
    
    private void installPipDependencies(java.util.List<Dependency> dependencies, Path projectPath) 
            throws IOException, InterruptedException {
        
        // Check if requirements.txt exists
        Path requirementsPath = projectPath.resolve("requirements.txt");
        if (Files.exists(requirementsPath)) {
            executeCommand(projectPath, "pip", "install", "-r", "requirements.txt");
        } else {
            // Install individual packages
            for (Dependency dep : dependencies) {
                String packageSpec = dep.getFormattedDependency();
                executeCommand(projectPath, "pip", "install", packageSpec);
            }
        }
    }
    
    private void installNuGetDependencies(java.util.List<Dependency> dependencies, Path projectPath) 
            throws IOException, InterruptedException {
        
        for (Dependency dep : dependencies) {
            executeCommand(projectPath, "dotnet", "add", "package", dep.getName(), "--version", dep.getVersion());
        }
    }
    
    private void installComposerDependencies(java.util.List<Dependency> dependencies, Path projectPath) 
            throws IOException, InterruptedException {
        
        Path composerJsonPath = projectPath.resolve("composer.json");
        if (Files.exists(composerJsonPath)) {
            executeCommand(projectPath, "composer", "install");
        } else {
            for (Dependency dep : dependencies) {
                executeCommand(projectPath, "composer", "require", dep.getName() + ":" + dep.getVersion());
            }
        }
    }
    
    private void installGemDependencies(java.util.List<Dependency> dependencies, Path projectPath) 
            throws IOException, InterruptedException {
        
        Path gemfilePath = projectPath.resolve("Gemfile");
        if (Files.exists(gemfilePath)) {
            executeCommand(projectPath, "bundle", "install");
        } else {
            for (Dependency dep : dependencies) {
                executeCommand(projectPath, "gem", "install", dep.getName(), "-v", dep.getVersion());
            }
        }
    }
    
    private void installGoModulesDependencies(java.util.List<Dependency> dependencies, Path projectPath) 
            throws IOException, InterruptedException {
        
        // Initialize go.mod if it doesn't exist
        Path goModPath = projectPath.resolve("go.mod");
        if (!Files.exists(goModPath)) {
            executeCommand(projectPath, "go", "mod", "init", "example.com/project");
        }
        
        for (Dependency dep : dependencies) {
            executeCommand(projectPath, "go", "get", dep.getFormattedDependency());
        }
    }
    
    private void executeCommand(Path workingDirectory, String... command) throws IOException, InterruptedException {
        logger.info("Executing command: {} in directory: {}", String.join(" ", command), workingDirectory);
        
        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.directory(workingDirectory.toFile());
        processBuilder.redirectErrorStream(true);
        
        Process process = processBuilder.start();
        
        // Log output
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                logger.debug("Command output: {}", line);
            }
        }
        
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            logger.warn("Command exited with code: {}", exitCode);
        }
    }
    
    // Template generator interfaces and implementations
    private interface ProjectTemplateGenerator {
        void generateTemplate(ProjectStructure projectStructure, Path projectPath) throws IOException;
    }
    
    private class MavenProjectGenerator implements ProjectTemplateGenerator {
        @Override
        public void generateTemplate(ProjectStructure projectStructure, Path projectPath) throws IOException {
            createMavenPom(projectStructure, projectPath);
            createMavenDirectoryStructure(projectPath);
        }
        
        private void createMavenPom(ProjectStructure projectStructure, Path projectPath) throws IOException {
            StringBuilder pom = new StringBuilder();
            pom.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
            pom.append("<project xmlns=\"http://maven.apache.org/POM/4.0.0\"\n");
            pom.append("         xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n");
            pom.append("         xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\">\n");
            pom.append("    <modelVersion>4.0.0</modelVersion>\n\n");
            pom.append("    <groupId>com.example</groupId>\n");
            pom.append("    <artifactId>").append(projectStructure.getProjectName().toLowerCase().replace(" ", "-")).append("</artifactId>\n");
            pom.append("    <version>").append(projectStructure.getVersion()).append("</version>\n");
            pom.append("    <packaging>jar</packaging>\n\n");
            pom.append("    <name>").append(projectStructure.getProjectName()).append("</name>\n");
            if (!projectStructure.getDescription().isEmpty()) {
                pom.append("    <description>").append(projectStructure.getDescription()).append("</description>\n");
            }
            pom.append("\n");
            pom.append("    <properties>\n");
            pom.append("        <maven.compiler.source>17</maven.compiler.source>\n");
            pom.append("        <maven.compiler.target>17</maven.compiler.target>\n");
            pom.append("        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>\n");
            pom.append("    </properties>\n\n");
            
            // Add dependencies
            if (projectStructure.getDependencies() != null && !projectStructure.getDependencies().isEmpty()) {
                pom.append("    <dependencies>\n");
                for (Dependency dep : projectStructure.getDependencies()) {
                    if (dep.getType().equalsIgnoreCase("maven")) {
                        pom.append("        ").append(dep.getFormattedDependency().replace("\n", "\n        ")).append("\n");
                    }
                }
                pom.append("    </dependencies>\n\n");
            }
            
            pom.append("    <build>\n");
            pom.append("        <plugins>\n");
            pom.append("            <plugin>\n");
            pom.append("                <groupId>org.apache.maven.plugins</groupId>\n");
            pom.append("                <artifactId>maven-compiler-plugin</artifactId>\n");
            pom.append("                <version>3.11.0</version>\n");
            pom.append("                <configuration>\n");
            pom.append("                    <source>17</source>\n");
            pom.append("                    <target>17</target>\n");
            pom.append("                </configuration>\n");
            pom.append("            </plugin>\n");
            pom.append("        </plugins>\n");
            pom.append("    </build>\n");
            pom.append("</project>\n");
            
            Files.write(projectPath.resolve("pom.xml"), pom.toString().getBytes());
        }
        
        private void createMavenDirectoryStructure(Path projectPath) throws IOException {
            Files.createDirectories(projectPath.resolve("src/main/java"));
            Files.createDirectories(projectPath.resolve("src/main/resources"));
            Files.createDirectories(projectPath.resolve("src/test/java"));
            Files.createDirectories(projectPath.resolve("src/test/resources"));
        }
    }
    
    private class GradleProjectGenerator implements ProjectTemplateGenerator {
        @Override
        public void generateTemplate(ProjectStructure projectStructure, Path projectPath) throws IOException {
            createGradleBuild(projectStructure, projectPath);
            createGradleWrapper(projectPath);
            createGradleDirectoryStructure(projectPath);
        }
        
        private void createGradleBuild(ProjectStructure projectStructure, Path projectPath) throws IOException {
            StringBuilder build = new StringBuilder();
            build.append("plugins {\n");
            build.append("    id 'java'\n");
            build.append("    id 'application'\n");
            build.append("}\n\n");
            build.append("group = 'com.example'\n");
            build.append("version = '").append(projectStructure.getVersion()).append("'\n");
            build.append("java.sourceCompatibility = JavaVersion.VERSION_17\n\n");
            build.append("repositories {\n");
            build.append("    mavenCentral()\n");
            build.append("}\n\n");
            
            if (projectStructure.getDependencies() != null && !projectStructure.getDependencies().isEmpty()) {
                build.append("dependencies {\n");
                for (Dependency dep : projectStructure.getDependencies()) {
                    if (dep.getType().equalsIgnoreCase("gradle")) {
                        build.append("    ").append(dep.getFormattedDependency()).append("\n");
                    }
                }
                build.append("}\n\n");
            }
            
            build.append("application {\n");
            build.append("    mainClass = 'com.example.Main'\n");
            build.append("}\n");
            
            Files.write(projectPath.resolve("build.gradle"), build.toString().getBytes());
        }
        
        private void createGradleWrapper(Path projectPath) throws IOException {
            // Create gradle wrapper files (simplified)
            Files.createDirectories(projectPath.resolve("gradle/wrapper"));
            
            String gradleWrapperProperties = "distributionBase=GRADLE_USER_HOME\n" +
                    "distributionPath=wrapper/dists\n" +
                    "distributionUrl=https\\://services.gradle.org/distributions/gradle-8.4-bin.zip\n" +
                    "zipStoreBase=GRADLE_USER_HOME\n" +
                    "zipStorePath=wrapper/dists\n";
            
            Files.write(projectPath.resolve("gradle/wrapper/gradle-wrapper.properties"), 
                    gradleWrapperProperties.getBytes());
        }
        
        private void createGradleDirectoryStructure(Path projectPath) throws IOException {
            Files.createDirectories(projectPath.resolve("src/main/java/com/example"));
            Files.createDirectories(projectPath.resolve("src/main/resources"));
            Files.createDirectories(projectPath.resolve("src/test/java/com/example"));
            Files.createDirectories(projectPath.resolve("src/test/resources"));
        }
    }
    
    private class PythonProjectGenerator implements ProjectTemplateGenerator {
        @Override
        public void generateTemplate(ProjectStructure projectStructure, Path projectPath) throws IOException {
            createRequirementsTxt(projectStructure, projectPath);
            createSetupPy(projectStructure, projectPath);
            createPythonDirectoryStructure(projectPath);
        }
        
        private void createRequirementsTxt(ProjectStructure projectStructure, Path projectPath) throws IOException {
            if (projectStructure.getDependencies() != null && !projectStructure.getDependencies().isEmpty()) {
                StringBuilder requirements = new StringBuilder();
                for (Dependency dep : projectStructure.getDependencies()) {
                    if (dep.getType().equalsIgnoreCase("pip")) {
                        requirements.append(dep.getFormattedDependency()).append("\n");
                    }
                }
                Files.write(projectPath.resolve("requirements.txt"), requirements.toString().getBytes());
            }
        }
        
        private void createSetupPy(ProjectStructure projectStructure, Path projectPath) throws IOException {
            String setup = "from setuptools import setup, find_packages\n\n" +
                    "setup(\n" +
                    "    name='" + projectStructure.getProjectName().toLowerCase().replace(" ", "-") + "',\n" +
                    "    version='" + projectStructure.getVersion() + "',\n" +
                    "    author='" + projectStructure.getAuthor() + "',\n" +
                    "    description='" + projectStructure.getDescription() + "',\n" +
                    "    packages=find_packages(),\n" +
                    "    classifiers=[\n" +
                    "        'Development Status :: 3 - Alpha',\n" +
                    "        'Intended Audience :: Developers',\n" +
                    "        'Programming Language :: Python :: 3',\n" +
                    "        'Programming Language :: Python :: 3.8',\n" +
                    "        'Programming Language :: Python :: 3.9',\n" +
                    "        'Programming Language :: Python :: 3.10',\n" +
                    "        'Programming Language :: Python :: 3.11',\n" +
                    "    ],\n" +
                    "    python_requires='>=3.8',\n" +
                    ")\n";
            
            Files.write(projectPath.resolve("setup.py"), setup.getBytes());
        }
        
        private void createPythonDirectoryStructure(Path projectPath) throws IOException {
            Files.createDirectories(projectPath.resolve("src"));
            Files.createFile(projectPath.resolve("src/__init__.py"));
            Files.createDirectories(projectPath.resolve("tests"));
            Files.createFile(projectPath.resolve("tests/__init__.py"));
        }
    }
    
    private class NodeJsProjectGenerator implements ProjectTemplateGenerator {
        @Override
        public void generateTemplate(ProjectStructure projectStructure, Path projectPath) throws IOException {
            createPackageJson(projectStructure, projectPath);
            createNodeDirectoryStructure(projectPath);
        }
        
        private void createPackageJson(ProjectStructure projectStructure, Path projectPath) throws IOException {
            StringBuilder packageJson = new StringBuilder();
            packageJson.append("{\n");
            packageJson.append("  \"name\": \"").append(projectStructure.getProjectName().toLowerCase().replace(" ", "-")).append("\",\n");
            packageJson.append("  \"version\": \"").append(projectStructure.getVersion()).append("\",\n");
            packageJson.append("  \"description\": \"").append(projectStructure.getDescription()).append("\",\n");
            packageJson.append("  \"main\": \"index.js\",\n");
            packageJson.append("  \"scripts\": {\n");
            packageJson.append("    \"start\": \"node index.js\",\n");
            packageJson.append("    \"test\": \"echo \\\"Error: no test specified\\\" && exit 1\"\n");
            packageJson.append("  },\n");
            packageJson.append("  \"author\": \"").append(projectStructure.getAuthor()).append("\",\n");
            packageJson.append("  \"license\": \"MIT\"");
            
            if (projectStructure.getDependencies() != null && !projectStructure.getDependencies().isEmpty()) {
                packageJson.append(",\n  \"dependencies\": {\n");
                boolean first = true;
                for (Dependency dep : projectStructure.getDependencies()) {
                    if (dep.getType().equalsIgnoreCase("npm")) {
                        if (!first) packageJson.append(",\n");
                        packageJson.append("    ").append(dep.getFormattedDependency());
                        first = false;
                    }
                }
                packageJson.append("\n  }");
            }
            
            packageJson.append("\n}\n");
            
            Files.write(projectPath.resolve("package.json"), packageJson.toString().getBytes());
        }
        
        private void createNodeDirectoryStructure(Path projectPath) throws IOException {
            Files.createFile(projectPath.resolve("index.js"));
            Files.createDirectories(projectPath.resolve("src"));
            Files.createDirectories(projectPath.resolve("test"));
        }
    }
    
    private class ReactProjectGenerator implements ProjectTemplateGenerator {
        @Override
        public void generateTemplate(ProjectStructure projectStructure, Path projectPath) throws IOException {
            createPackageJson(projectStructure, projectPath);
            createReactDirectoryStructure(projectPath);
            createReactFiles(projectPath);
        }
        
        private void createPackageJson(ProjectStructure projectStructure, Path projectPath) throws IOException {
            // Similar to NodeJS but with React dependencies
            StringBuilder packageJson = new StringBuilder();
            packageJson.append("{\n");
            packageJson.append("  \"name\": \"").append(projectStructure.getProjectName().toLowerCase().replace(" ", "-")).append("\",\n");
            packageJson.append("  \"version\": \"").append(projectStructure.getVersion()).append("\",\n");
            packageJson.append("  \"private\": true,\n");
            packageJson.append("  \"dependencies\": {\n");
            packageJson.append("    \"react\": \"^18.2.0\",\n");
            packageJson.append("    \"react-dom\": \"^18.2.0\",\n");
            packageJson.append("    \"react-scripts\": \"5.0.1\"");
            
            if (projectStructure.getDependencies() != null && !projectStructure.getDependencies().isEmpty()) {
                for (Dependency dep : projectStructure.getDependencies()) {
                    if (dep.getType().equalsIgnoreCase("npm")) {
                        packageJson.append(",\n    ").append(dep.getFormattedDependency());
                    }
                }
            }
            
            packageJson.append("\n  },\n");
            packageJson.append("  \"scripts\": {\n");
            packageJson.append("    \"start\": \"react-scripts start\",\n");
            packageJson.append("    \"build\": \"react-scripts build\",\n");
            packageJson.append("    \"test\": \"react-scripts test\",\n");
            packageJson.append("    \"eject\": \"react-scripts eject\"\n");
            packageJson.append("  },\n");
            packageJson.append("  \"browserslist\": {\n");
            packageJson.append("    \"production\": [\n");
            packageJson.append("      \">0.2%\",\n");
            packageJson.append("      \"not dead\",\n");
            packageJson.append("      \"not op_mini all\"\n");
            packageJson.append("    ],\n");
            packageJson.append("    \"development\": [\n");
            packageJson.append("      \"last 1 chrome version\",\n");
            packageJson.append("      \"last 1 firefox version\",\n");
            packageJson.append("      \"last 1 safari version\"\n");
            packageJson.append("    ]\n");
            packageJson.append("  }\n");
            packageJson.append("}\n");
            
            Files.write(projectPath.resolve("package.json"), packageJson.toString().getBytes());
        }
        
        private void createReactDirectoryStructure(Path projectPath) throws IOException {
            Files.createDirectories(projectPath.resolve("public"));
            Files.createDirectories(projectPath.resolve("src"));
        }
        
        private void createReactFiles(Path projectPath) throws IOException {
            // Create basic React files
            String indexHtml = "<!DOCTYPE html>\n<html lang=\"en\">\n<head>\n  <meta charset=\"utf-8\" />\n  <title>React App</title>\n</head>\n<body>\n  <div id=\"root\"></div>\n</body>\n</html>";
            Files.write(projectPath.resolve("public/index.html"), indexHtml.getBytes());
            
            String appJs = "function App() {\n  return (\n    <div>\n      <h1>Hello World!</h1>\n    </div>\n  );\n}\n\nexport default App;";
            Files.write(projectPath.resolve("src/App.js"), appJs.getBytes());
            
            String indexJs = "import React from 'react';\nimport ReactDOM from 'react-dom/client';\nimport App from './App';\n\nconst root = ReactDOM.createRoot(document.getElementById('root'));\nroot.render(<App />);";
            Files.write(projectPath.resolve("src/index.js"), indexJs.getBytes());
        }
    }
    
    private class SpringBootProjectGenerator implements ProjectTemplateGenerator {
        @Override
        public void generateTemplate(ProjectStructure projectStructure, Path projectPath) throws IOException {
            createSpringBootPom(projectStructure, projectPath);
            createSpringBootDirectoryStructure(projectPath);
            createSpringBootMainClass(projectStructure, projectPath);
        }
        
        private void createSpringBootPom(ProjectStructure projectStructure, Path projectPath) throws IOException {
            // Spring Boot specific Maven POM
            StringBuilder pom = new StringBuilder();
            pom.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
            pom.append("<project xmlns=\"http://maven.apache.org/POM/4.0.0\"\n");
            pom.append("         xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n");
            pom.append("         xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\">\n");
            pom.append("    <modelVersion>4.0.0</modelVersion>\n\n");
            pom.append("    <parent>\n");
            pom.append("        <groupId>org.springframework.boot</groupId>\n");
            pom.append("        <artifactId>spring-boot-starter-parent</artifactId>\n");
            pom.append("        <version>3.1.5</version>\n");
            pom.append("        <relativePath/>\n");
            pom.append("    </parent>\n\n");
            pom.append("    <groupId>com.example</groupId>\n");
            pom.append("    <artifactId>").append(projectStructure.getProjectName().toLowerCase().replace(" ", "-")).append("</artifactId>\n");
            pom.append("    <version>").append(projectStructure.getVersion()).append("</version>\n");
            pom.append("    <packaging>jar</packaging>\n\n");
            pom.append("    <name>").append(projectStructure.getProjectName()).append("</name>\n");
            if (!projectStructure.getDescription().isEmpty()) {
                pom.append("    <description>").append(projectStructure.getDescription()).append("</description>\n");
            }
            pom.append("\n");
            pom.append("    <properties>\n");
            pom.append("        <java.version>17</java.version>\n");
            pom.append("    </properties>\n\n");
            pom.append("    <dependencies>\n");
            pom.append("        <dependency>\n");
            pom.append("            <groupId>org.springframework.boot</groupId>\n");
            pom.append("            <artifactId>spring-boot-starter-web</artifactId>\n");
            pom.append("        </dependency>\n");
            pom.append("        <dependency>\n");
            pom.append("            <groupId>org.springframework.boot</groupId>\n");
            pom.append("            <artifactId>spring-boot-starter-test</artifactId>\n");
            pom.append("            <scope>test</scope>\n");
            pom.append("        </dependency>\n");
            
            // Add custom dependencies
            if (projectStructure.getDependencies() != null && !projectStructure.getDependencies().isEmpty()) {
                for (Dependency dep : projectStructure.getDependencies()) {
                    if (dep.getType().equalsIgnoreCase("maven")) {
                        pom.append("        ").append(dep.getFormattedDependency().replace("\n", "\n        ")).append("\n");
                    }
                }
            }
            
            pom.append("    </dependencies>\n\n");
            pom.append("    <build>\n");
            pom.append("        <plugins>\n");
            pom.append("            <plugin>\n");
            pom.append("                <groupId>org.springframework.boot</groupId>\n");
            pom.append("                <artifactId>spring-boot-maven-plugin</artifactId>\n");
            pom.append("            </plugin>\n");
            pom.append("        </plugins>\n");
            pom.append("    </build>\n");
            pom.append("</project>\n");
            
            Files.write(projectPath.resolve("pom.xml"), pom.toString().getBytes());
        }
        
        private void createSpringBootDirectoryStructure(Path projectPath) throws IOException {
            Files.createDirectories(projectPath.resolve("src/main/java/com/example"));
            Files.createDirectories(projectPath.resolve("src/main/resources"));
            Files.createDirectories(projectPath.resolve("src/test/java/com/example"));
        }
        
        private void createSpringBootMainClass(ProjectStructure projectStructure, Path projectPath) throws IOException {
            String className = projectStructure.getProjectName().replaceAll("[^a-zA-Z0-9]", "") + "Application";
            String mainClass = "package com.example;\n\n" +
                    "import org.springframework.boot.SpringApplication;\n" +
                    "import org.springframework.boot.autoconfigure.SpringBootApplication;\n\n" +
                    "@SpringBootApplication\n" +
                    "public class " + className + " {\n\n" +
                    "    public static void main(String[] args) {\n" +
                    "        SpringApplication.run(" + className + ".class, args);\n" +
                    "    }\n\n" +
                    "}\n";
            
            Files.write(projectPath.resolve("src/main/java/com/example/" + className + ".java"), mainClass.getBytes());
        }
    }
    
    private class CustomProjectGenerator implements ProjectTemplateGenerator {
        @Override
        public void generateTemplate(ProjectStructure projectStructure, Path projectPath) throws IOException {
            // Custom projects don't generate template files, only user-defined structure
            logger.info("Custom project - no template files generated");
        }
    }
}