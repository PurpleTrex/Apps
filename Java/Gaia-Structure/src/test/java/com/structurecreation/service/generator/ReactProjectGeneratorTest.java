package com.structurecreation.service.generator;

import com.structurecreation.model.ProjectNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive regression tests for ReactProjectGenerator.
 * Tests all project types and validates complete project structure generation.
 */
class ReactProjectGeneratorTest {

    private ReactProjectGenerator generator;

    private static final String TEST_PROJECT_NAME = "TestReactProject";

    @BeforeEach
    void setUp() {
        generator = new ReactProjectGenerator();
        assertNotNull(generator, "should be initialized");
    }

    @ParameterizedTest
    @EnumSource(ReactProjectGenerator.ReactProjectType.class)
    @DisplayName("Should generate complete project structure for all React project types")
    void testGenerateProjectForAllTypes(ReactProjectGenerator.ReactProjectType type) {
        // Generate project
        ProjectNode root = generator.generateReactProject(TEST_PROJECT_NAME, type);

        // Validate root
        assertNotNull(root, "Root project node should not be null");
        assertEquals(TEST_PROJECT_NAME, root.getName(), "Project name should match");
        assertTrue(root.isDirectory(), "Root should be a directory");
        assertFalse(root.getChildren().isEmpty(), "Project should have children");
    }

    @Test
    @DisplayName("Should generate package.json")
    void testPackageJson() {
        ProjectNode root = generator.generateReactProject(TEST_PROJECT_NAME, 
            ReactProjectGenerator.ReactProjectType.BASIC);

        ProjectNode packageJson = findChild(root, "package.json");
        assertNotNull(packageJson);
        assertNotNull(packageJson.getContent());
        
        String content = packageJson.getContent();
        assertTrue(content.contains(TEST_PROJECT_NAME));
        assertTrue(content.contains("react"));
        assertTrue(content.contains("react-dom"));
    }

    @Test
    @DisplayName("Should generate src directory structure")
    void testSrcStructure() {
        ProjectNode root = generator.generateReactProject(TEST_PROJECT_NAME, 
            ReactProjectGenerator.ReactProjectType.BASIC);

        ProjectNode src = findChild(root, "src");
        assertNotNull(src);
        
        assertHasChild(src, "components", true);
        assertHasChild(src, "App.js", false);
        assertHasChild(src, "index.js", false);
        assertHasChild(src, "App.css", false);
        assertHasChild(src, "index.css", false);
    }

    @Test
    @DisplayName("Should generate public directory")
    void testPublicDirectory() {
        ProjectNode root = generator.generateReactProject(TEST_PROJECT_NAME, 
            ReactProjectGenerator.ReactProjectType.BASIC);

        ProjectNode publicDir = findChild(root, "public");
        assertNotNull(publicDir);
        
        assertHasChild(publicDir, "index.html", false);
        
        ProjectNode indexHtml = findChild(publicDir, "index.html");
        assertNotNull(indexHtml.getContent());
        assertTrue(indexHtml.getContent().contains("<div id=\"root\">"));
    }

    @Test
    @DisplayName("Should generate configuration files")
    void testConfigurationFiles() {
        ProjectNode root = generator.generateReactProject(TEST_PROJECT_NAME, 
            ReactProjectGenerator.ReactProjectType.BASIC);

        assertHasChild(root, ".gitignore", false);
        assertHasChild(root, "README.md", false);
        
        ProjectNode gitignore = findChild(root, ".gitignore");
        assertNotNull(gitignore.getContent());
        assertTrue(gitignore.getContent().contains("node_modules"));
    }

    @Test
    @DisplayName("Should generate TypeScript configuration for TypeScript projects")
    void testTypeScriptConfiguration() {
        ProjectNode root = generator.generateReactProject(TEST_PROJECT_NAME, 
            ReactProjectGenerator.ReactProjectType.WITH_TYPESCRIPT);

        ProjectNode tsconfig = findChild(root, "tsconfig.json");
        assertNotNull(tsconfig);
        assertNotNull(tsconfig.getContent());
        assertTrue(tsconfig.getContent().contains("\"jsx\": \"react\""));
    }

    @Test
    @DisplayName("Should generate components")
    void testComponents() {
        ProjectNode root = generator.generateReactProject(TEST_PROJECT_NAME, 
            ReactProjectGenerator.ReactProjectType.BASIC);

        ProjectNode src = findChild(root, "src");
        ProjectNode components = findChild(src, "components");
        assertNotNull(components);
        
        // Validate at least one component exists
        assertFalse(components.getChildren().isEmpty());
    }

    @Test
    @DisplayName("Should generate README with project information")
    void testReadme() {
        ProjectNode root = generator.generateReactProject(TEST_PROJECT_NAME, 
            ReactProjectGenerator.ReactProjectType.BASIC);

        ProjectNode readme = findChild(root, "README.md");
        assertNotNull(readme);
        assertNotNull(readme.getContent());
        assertTrue(readme.getContent().contains(TEST_PROJECT_NAME));
        assertTrue(readme.getContent().contains("npm start"));
    }

    // Helper methods
    private void assertHasChild(ProjectNode parent, String childName, boolean isDirectory) {
        ProjectNode child = findChild(parent, childName);
        assertNotNull(child, "Child '" + childName + "' should exist in '" + parent.getName() + "'");
        assertEquals(isDirectory, child.isDirectory(), 
            "Child '" + childName + "' should " + (isDirectory ? "be" : "not be") + " a directory");
    }

    private ProjectNode findChild(ProjectNode parent, String name) {
        if (parent == null || parent.getChildren() == null) {
            return null;
        }
        return parent.getChildren().stream()
            .filter(child -> child.getName().equals(name))
            .findFirst()
            .orElse(null);
    }
}


