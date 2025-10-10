package com.structurecreation.service.generator;

import com.structurecreation.model.ProjectNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive regression tests for NodeExpressProjectGenerator.
 * Tests all project types and validates complete project structure generation.
 */
class NodeExpressProjectGeneratorTest {

    private NodeExpressProjectGenerator generator;

    private static final String TEST_PROJECT_NAME = "TestExpressProject";

    @BeforeEach
    void setUp() {
        generator = new NodeExpressProjectGenerator();
        assertNotNull(generator, "should be initialized");
    }

    @ParameterizedTest
    @EnumSource(NodeExpressProjectGenerator.NodeProjectType.class)
    @DisplayName("Should generate complete project structure for all Express project types")
    void testGenerateProjectForAllTypes(NodeExpressProjectGenerator.NodeProjectType type) {
        // Generate project
        ProjectNode root = generator.generateNodeExpressProject(TEST_PROJECT_NAME, type);

        // Validate root
        assertNotNull(root, "Root project node should not be null");
        assertEquals(TEST_PROJECT_NAME, root.getName(), "Project name should match");
        assertTrue(root.isDirectory(), "Root should be a directory");
        assertFalse(root.getChildren().isEmpty(), "Project should have children");
    }

    @Test
    @DisplayName("Should generate package.json with Express dependencies")
    void testPackageJson() {
        ProjectNode root = generator.generateNodeExpressProject(TEST_PROJECT_NAME, 
            NodeExpressProjectGenerator.NodeProjectType.REST_API);

        ProjectNode packageJson = findChild(root, "package.json");
        assertNotNull(packageJson);
        assertNotNull(packageJson.getContent());
        
        String content = packageJson.getContent();
        assertTrue(content.contains(TEST_PROJECT_NAME));
        assertTrue(content.contains("express"));
    }

    @Test
    @DisplayName("Should generate REST API structure")
    void testRESTAPIStructure() {
        ProjectNode root = generator.generateNodeExpressProject(TEST_PROJECT_NAME, 
            NodeExpressProjectGenerator.NodeProjectType.REST_API);

        ProjectNode src = findChild(root, "src");
        assertNotNull(src);
        
        assertHasChild(src, "routes", true);
        assertHasChild(src, "controllers", true);
        assertHasChild(src, "models", true);
        assertHasChild(src, "middleware", true);
        assertHasChild(src, "app.js", false);
        assertHasChild(src, "server.js", false);
    }

    @Test
    @DisplayName("Should generate GraphQL API structure")
    void testGraphQLAPIStructure() {
        ProjectNode root = generator.generateNodeExpressProject(TEST_PROJECT_NAME, 
            NodeExpressProjectGenerator.NodeProjectType.GRAPHQL);

        ProjectNode src = findChild(root, "src");
        assertNotNull(src);
        
        assertHasChild(src, "graphql", true);
        ProjectNode graphql = findChild(src, "graphql");
        assertHasChild(graphql, "schema.js", false);
        assertHasChild(graphql, "resolvers.js", false);
    }

    @Test
    @DisplayName("Should generate configuration files")
    void testConfigurationFiles() {
        ProjectNode root = generator.generateNodeExpressProject(TEST_PROJECT_NAME, 
            NodeExpressProjectGenerator.NodeProjectType.REST_API);

        assertHasChild(root, ".env.example", false);
        assertHasChild(root, ".gitignore", false);
        assertHasChild(root, "README.md", false);
        
        ProjectNode envExample = findChild(root, ".env.example");
        assertNotNull(envExample.getContent());
        assertTrue(envExample.getContent().contains("PORT="));
    }

    @Test
    @DisplayName("Should generate test structure")
    void testTestStructure() {
        ProjectNode root = generator.generateNodeExpressProject(TEST_PROJECT_NAME, 
            NodeExpressProjectGenerator.NodeProjectType.REST_API);

        ProjectNode tests = findChild(root, "tests");
        assertNotNull(tests);
        
        // Validate test files exist
        assertFalse(tests.getChildren().isEmpty());
    }

    @Test
    @DisplayName("Should generate Docker support")
    void testDockerSupport() {
        ProjectNode root = generator.generateNodeExpressProject(TEST_PROJECT_NAME, 
            NodeExpressProjectGenerator.NodeProjectType.REST_API);

        ProjectNode dockerfile = findChild(root, "Dockerfile");
        assertNotNull(dockerfile);
        assertNotNull(dockerfile.getContent());
        assertTrue(dockerfile.getContent().contains("FROM node"));
    }

    @Test
    @DisplayName("Should generate README with usage instructions")
    void testReadme() {
        ProjectNode root = generator.generateNodeExpressProject(TEST_PROJECT_NAME, 
            NodeExpressProjectGenerator.NodeProjectType.REST_API);

        ProjectNode readme = findChild(root, "README.md");
        assertNotNull(readme);
        assertNotNull(readme.getContent());
        assertTrue(readme.getContent().contains(TEST_PROJECT_NAME));
        assertTrue(readme.getContent().contains("npm install"));
        assertTrue(readme.getContent().contains("npm start"));
    }

    @Test
    @DisplayName("Should generate middleware files")
    void testMiddleware() {
        ProjectNode root = generator.generateNodeExpressProject(TEST_PROJECT_NAME, 
            NodeExpressProjectGenerator.NodeProjectType.REST_API);

        ProjectNode src = findChild(root, "src");
        ProjectNode middleware = findChild(src, "middleware");
        assertNotNull(middleware);
        
        // Validate middleware files exist
        assertFalse(middleware.getChildren().isEmpty());
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


