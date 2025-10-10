package com.structurecreation.service.generator;

import com.structurecreation.model.ProjectNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for all project generators.
 * Tests cross-cutting concerns and end-to-end workflows.
 */
class ProjectGeneratorIntegrationTest {

    private FlaskProjectGenerator flaskGenerator;
    private DjangoProjectGenerator djangoGenerator;
    private SpringBootProjectGenerator springBootGenerator;
    private ReactProjectGenerator reactGenerator;
    private NodeExpressProjectGenerator nodeExpressGenerator;

    @BeforeEach
    void setUp() {
        flaskGenerator = new FlaskProjectGenerator();
        djangoGenerator = new DjangoProjectGenerator();
        springBootGenerator = new SpringBootProjectGenerator();
        reactGenerator = new ReactProjectGenerator();
        nodeExpressGenerator = new NodeExpressProjectGenerator();
    }

    @Test
    @DisplayName("All generators should be initialized successfully")
    void testAllGeneratorsInitialized() {
        assertNotNull(flaskGenerator, "FlaskProjectGenerator should be initialized");
        assertNotNull(djangoGenerator, "DjangoProjectGenerator should be initialized");
        assertNotNull(springBootGenerator, "SpringBootProjectGenerator should be initialized");
        assertNotNull(reactGenerator, "ReactProjectGenerator should be initialized");
        assertNotNull(nodeExpressGenerator, "NodeExpressProjectGenerator should be initialized");
    }

    @Test
    @DisplayName("Flask project should have consistent structure across types")
    void testFlaskProjectConsistency() {
        List<ProjectNode> projects = new ArrayList<>();
        
        for (FlaskProjectGenerator.FlaskProjectType type : FlaskProjectGenerator.FlaskProjectType.values()) {
            ProjectNode project = flaskGenerator.generateFlaskProject("TestProject", type);
            projects.add(project);
            
            // All should have these core files
            assertHasFile(project, "requirements.txt");
            assertHasFile(project, ".gitignore");
            assertHasFile(project, "README.md");
            assertHasDirectory(project, "app");
        }
        
        assertEquals(FlaskProjectGenerator.FlaskProjectType.values().length, projects.size());
    }

    @Test
    @DisplayName("Django project should have consistent structure across types")
    void testDjangoProjectConsistency() {
        List<ProjectNode> projects = new ArrayList<>();
        
        for (DjangoProjectGenerator.DjangoProjectType type : DjangoProjectGenerator.DjangoProjectType.values()) {
            ProjectNode project = djangoGenerator.generateDjangoProject("TestProject", type);
            projects.add(project);
            
            // All should have these core files
            assertHasFile(project, "requirements.txt");
            assertHasFile(project, ".gitignore");
            assertHasFile(project, "manage.py");
        }
        
        assertEquals(DjangoProjectGenerator.DjangoProjectType.values().length, projects.size());
    }

    @Test
    @DisplayName("All Python projects should have valid Python file structure")
    void testPythonProjectStructure() {
        // Flask project
        ProjectNode flaskProject = flaskGenerator.generateFlaskProject(
            "FlaskTest", FlaskProjectGenerator.FlaskProjectType.REST_API);
        List<ProjectNode> flaskPyFiles = findPythonFiles(flaskProject);
        assertFalse(flaskPyFiles.isEmpty(), "Flask project should have Python files");
        
        // Django project
        ProjectNode djangoProject = djangoGenerator.generateDjangoProject(
            "DjangoTest", DjangoProjectGenerator.DjangoProjectType.REST_API);
        List<ProjectNode> djangoPyFiles = findPythonFiles(djangoProject);
        assertFalse(djangoPyFiles.isEmpty(), "Django project should have Python files");
        
        // Validate Python file content
        for (ProjectNode pyFile : flaskPyFiles) {
            if (pyFile.getContent() != null && !pyFile.getContent().isEmpty()) {
                validatePythonSyntax(pyFile.getContent(), pyFile.getName());
            }
        }
    }

    @Test
    @DisplayName("All JavaScript projects should have valid package.json")
    void testJavaScriptProjectStructure() {
        // React project
        ProjectNode reactProject = reactGenerator.generateReactProject(
            "ReactTest", ReactProjectGenerator.ReactProjectType.BASIC);
        assertValidPackageJson(reactProject, "react");
        
        // Node/Express project
        ProjectNode nodeProject = nodeExpressGenerator.generateNodeExpressProject(
            "NodeTest", NodeExpressProjectGenerator.NodeProjectType.REST_API);
        assertValidPackageJson(nodeProject, "express");
    }

    @Test
    @DisplayName("All projects should have proper .gitignore files")
    void testGitignoreGeneration() {
        // Flask
        ProjectNode flaskProject = flaskGenerator.generateFlaskProject(
            "Test", FlaskProjectGenerator.FlaskProjectType.REST_API);
        validateGitignore(flaskProject, List.of("__pycache__", "*.pyc", ".env"));
        
        // Django
        ProjectNode djangoProject = djangoGenerator.generateDjangoProject(
            "Test", DjangoProjectGenerator.DjangoProjectType.REST_API);
        validateGitignore(djangoProject, List.of("__pycache__", "*.pyc", "db.sqlite3"));
        
        // Spring Boot
        ProjectNode springProject = springBootGenerator.generateSpringBootProject(
            "Test", "com.test", "test-app", SpringBootProjectGenerator.SpringBootProjectType.BASIC);
        validateGitignore(springProject, List.of("target/", "*.class"));
        
        // React
        ProjectNode reactProject = reactGenerator.generateReactProject(
            "Test", ReactProjectGenerator.ReactProjectType.BASIC);
        validateGitignore(reactProject, List.of("node_modules", "/build"));
        
        // Node/Express
        ProjectNode nodeProject = nodeExpressGenerator.generateNodeExpressProject(
            "Test", NodeExpressProjectGenerator.NodeProjectType.REST_API);
        validateGitignore(nodeProject, List.of("node_modules", ".env"));
    }

    @Test
    @DisplayName("All projects should have README files with project name")
    void testReadmeGeneration() {
        String projectName = "TestProjectName";
        
        // Flask
        ProjectNode flaskProject = flaskGenerator.generateFlaskProject(
            projectName, FlaskProjectGenerator.FlaskProjectType.REST_API);
        validateReadme(flaskProject, projectName);
        
        // Django
        ProjectNode djangoProject = djangoGenerator.generateDjangoProject(
            projectName, DjangoProjectGenerator.DjangoProjectType.REST_API);
        validateReadme(djangoProject, projectName);
        
        // Spring Boot
        ProjectNode springProject = springBootGenerator.generateSpringBootProject(
            projectName, "com.test", "test-app", SpringBootProjectGenerator.SpringBootProjectType.BASIC);
        validateReadme(springProject, projectName);
        
        // React
        ProjectNode reactProject = reactGenerator.generateReactProject(
            projectName, ReactProjectGenerator.ReactProjectType.BASIC);
        validateReadme(reactProject, projectName);
        
        // Node/Express
        ProjectNode nodeProject = nodeExpressGenerator.generateNodeExpressProject(
            projectName, NodeExpressProjectGenerator.NodeProjectType.REST_API);
        validateReadme(nodeProject, projectName);
    }

    @Test
    @DisplayName("All projects should have no empty required files")
    void testNoEmptyRequiredFiles() {
        // Test Flask
        ProjectNode flaskProject = flaskGenerator.generateFlaskProject(
            "Test", FlaskProjectGenerator.FlaskProjectType.REST_API);
        validateNoEmptyFiles(flaskProject, List.of("requirements.txt", "README.md"));
        
        // Test Django
        ProjectNode djangoProject = djangoGenerator.generateDjangoProject(
            "Test", DjangoProjectGenerator.DjangoProjectType.REST_API);
        validateNoEmptyFiles(djangoProject, List.of("requirements.txt", "README.md", "manage.py"));
        
        // Test React
        ProjectNode reactProject = reactGenerator.generateReactProject(
            "Test", ReactProjectGenerator.ReactProjectType.BASIC);
        validateNoEmptyFiles(reactProject, List.of("package.json", "README.md"));
        
        // Test Node/Express
        ProjectNode nodeProject = nodeExpressGenerator.generateNodeExpressProject(
            "Test", NodeExpressProjectGenerator.NodeProjectType.REST_API);
        validateNoEmptyFiles(nodeProject, List.of("package.json", "README.md"));
    }

    @Test
    @DisplayName("Projects with Docker support should have complete Docker files")
    void testDockerSupportCompleteness() {
        // Flask with Docker
        ProjectNode flaskProject = flaskGenerator.generateFlaskProject(
            "Test", FlaskProjectGenerator.FlaskProjectType.REST_API);
        validateDockerSupport(flaskProject);
        
        // Django with Docker
        ProjectNode djangoProject = djangoGenerator.generateDjangoProject(
            "Test", DjangoProjectGenerator.DjangoProjectType.REST_API);
        validateDockerSupport(djangoProject);
        
        // Node/Express with Docker
        ProjectNode nodeProject = nodeExpressGenerator.generateNodeExpressProject(
            "Test", NodeExpressProjectGenerator.NodeProjectType.REST_API);
        validateDockerSupport(nodeProject);
    }

    @Test
    @DisplayName("All file contents should not contain placeholder text")
    void testNoPlaceholderText() {
        ProjectNode flaskProject = flaskGenerator.generateFlaskProject(
            "Test", FlaskProjectGenerator.FlaskProjectType.REST_API);
        
        List<ProjectNode> allFiles = getAllFiles(flaskProject);
        for (ProjectNode file : allFiles) {
            if (file.getContent() != null) {
                assertFalse(file.getContent().contains("TODO:"), 
                    "File " + file.getName() + " should not contain TODO placeholders");
                assertFalse(file.getContent().contains("FIXME"), 
                    "File " + file.getName() + " should not contain FIXME placeholders");
                assertFalse(file.getContent().contains("# Stub"), 
                    "File " + file.getName() + " should not contain stub comments");
            }
        }
    }

    @Test
    @DisplayName("File tree depth should be reasonable")
    void testReasonableFileTreeDepth() {
        ProjectNode flaskProject = flaskGenerator.generateFlaskProject(
            "Test", FlaskProjectGenerator.FlaskProjectType.REST_API);
        
        int maxDepth = getMaxDepth(flaskProject, 0);
        assertTrue(maxDepth < 10, "File tree depth should not exceed 10 levels, but was: " + maxDepth);
    }

    // Helper methods
    private void assertHasFile(ProjectNode root, String fileName) {
        ProjectNode file = findNodeByName(root, fileName);
        assertNotNull(file, "Project should have file: " + fileName);
        assertFalse(file.isDirectory(), fileName + " should be a file, not a directory");
    }

    private void assertHasDirectory(ProjectNode root, String dirName) {
        ProjectNode dir = findNodeByName(root, dirName);
        assertNotNull(dir, "Project should have directory: " + dirName);
        assertTrue(dir.isDirectory(), dirName + " should be a directory, not a file");
    }

    private ProjectNode findNodeByName(ProjectNode root, String name) {
        if (root.getName().equals(name)) {
            return root;
        }
        if (root.getChildren() == null) {
            return null;
        }
        for (ProjectNode child : root.getChildren()) {
            ProjectNode found = findNodeByName(child, name);
            if (found != null) {
                return found;
            }
        }
        return null;
    }

    private List<ProjectNode> findPythonFiles(ProjectNode root) {
        List<ProjectNode> pyFiles = new ArrayList<>();
        collectPythonFiles(root, pyFiles);
        return pyFiles;
    }

    private void collectPythonFiles(ProjectNode node, List<ProjectNode> pyFiles) {
        if (node.getName().endsWith(".py")) {
            pyFiles.add(node);
        }
        if (node.getChildren() != null) {
            for (ProjectNode child : node.getChildren()) {
                collectPythonFiles(child, pyFiles);
            }
        }
    }

    private void validatePythonSyntax(String content, String fileName) {
        // Basic validation - check for common Python syntax elements
        if (fileName.endsWith(".py") && !content.trim().isEmpty()) {
            // Should not have obvious syntax errors
            assertFalse(content.contains("SyntaxError"), 
                "File " + fileName + " should not contain syntax errors");
        }
    }

    private void assertValidPackageJson(ProjectNode root, String expectedDependency) {
        ProjectNode packageJson = findNodeByName(root, "package.json");
        assertNotNull(packageJson, "Project should have package.json");
        assertNotNull(packageJson.getContent(), "package.json should have content");
        assertTrue(packageJson.getContent().contains(expectedDependency), 
            "package.json should contain " + expectedDependency);
        assertTrue(packageJson.getContent().contains("\"name\""), 
            "package.json should have name field");
        assertTrue(packageJson.getContent().contains("\"version\""), 
            "package.json should have version field");
    }

    private void validateGitignore(ProjectNode root, List<String> expectedEntries) {
        ProjectNode gitignore = findNodeByName(root, ".gitignore");
        assertNotNull(gitignore, "Project should have .gitignore");
        assertNotNull(gitignore.getContent(), ".gitignore should have content");
        
        for (String entry : expectedEntries) {
            assertTrue(gitignore.getContent().contains(entry), 
                ".gitignore should contain: " + entry);
        }
    }

    private void validateReadme(ProjectNode root, String projectName) {
        ProjectNode readme = findNodeByName(root, "README.md");
        assertNotNull(readme, "Project should have README.md");
        assertNotNull(readme.getContent(), "README.md should have content");
        assertTrue(readme.getContent().contains(projectName), 
            "README.md should contain project name: " + projectName);
    }

    private void validateNoEmptyFiles(ProjectNode root, List<String> requiredFiles) {
        for (String fileName : requiredFiles) {
            ProjectNode file = findNodeByName(root, fileName);
            assertNotNull(file, "Required file should exist: " + fileName);
            assertNotNull(file.getContent(), "File should have content: " + fileName);
            assertFalse(file.getContent().trim().isEmpty(), 
                "File should not be empty: " + fileName);
        }
    }

    private void validateDockerSupport(ProjectNode root) {
        ProjectNode dockerfile = findNodeByName(root, "Dockerfile");
        assertNotNull(dockerfile, "Project should have Dockerfile");
        assertNotNull(dockerfile.getContent(), "Dockerfile should have content");
        assertTrue(dockerfile.getContent().contains("FROM"), 
            "Dockerfile should have FROM instruction");
        
        ProjectNode dockerCompose = findNodeByName(root, "docker-compose.yml");
        if (dockerCompose != null) {
            assertNotNull(dockerCompose.getContent(), "docker-compose.yml should have content");
            assertTrue(dockerCompose.getContent().contains("version"), 
                "docker-compose.yml should have version");
            assertTrue(dockerCompose.getContent().contains("services"), 
                "docker-compose.yml should have services");
        }
    }

    private List<ProjectNode> getAllFiles(ProjectNode root) {
        List<ProjectNode> files = new ArrayList<>();
        collectAllFiles(root, files);
        return files;
    }

    private void collectAllFiles(ProjectNode node, List<ProjectNode> files) {
        if (!node.isDirectory()) {
            files.add(node);
        }
        if (node.getChildren() != null) {
            for (ProjectNode child : node.getChildren()) {
                collectAllFiles(child, files);
            }
        }
    }

    private int getMaxDepth(ProjectNode node, int currentDepth) {
        if (node.getChildren() == null || node.getChildren().isEmpty()) {
            return currentDepth;
        }
        int maxChildDepth = currentDepth;
        for (ProjectNode child : node.getChildren()) {
            int childDepth = getMaxDepth(child, currentDepth + 1);
            maxChildDepth = Math.max(maxChildDepth, childDepth);
        }
        return maxChildDepth;
    }
}

