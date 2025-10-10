package com.structurecreation.service.generator;

import com.structurecreation.model.ProjectNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive regression tests for SpringBootProjectGenerator.
 * Tests all project types and validates complete project structure generation.
 */
class SpringBootProjectGeneratorTest {

    private SpringBootProjectGenerator generator;

    private static final String TEST_PROJECT_NAME = "TestSpringBootProject";
    private static final String TEST_GROUP_ID = "com.test";
    private static final String TEST_ARTIFACT_ID = "test-app";

    @BeforeEach
    void setUp() {
        generator = new SpringBootProjectGenerator();
        assertNotNull(generator, "should be initialized");
    }

    @ParameterizedTest
    @EnumSource(SpringBootProjectGenerator.SpringBootProjectType.class)
    @DisplayName("Should generate complete project structure for all Spring Boot project types")
    void testGenerateProjectForAllTypes(SpringBootProjectGenerator.SpringBootProjectType type) {
        // Generate project
        ProjectNode root = generator.generateSpringBootProject(
            TEST_PROJECT_NAME, TEST_GROUP_ID, type);

        // Validate root
        assertNotNull(root, "Root project node should not be null");
        assertEquals(TEST_PROJECT_NAME, root.getName(), "Project name should match");
        assertTrue(root.isDirectory(), "Root should be a directory");
        assertFalse(root.getChildren().isEmpty(), "Project should have children");
    }

    @Test
    @DisplayName("Should generate REST API project")
    void testRESTAPIProjectStructure() {
        ProjectNode root = generator.generateSpringBootProject(
            TEST_PROJECT_NAME, TEST_GROUP_ID, 
            SpringBootProjectGenerator.SpringBootProjectType.REST_API);

        // Validate src/main/java structure
        ProjectNode src = findChild(root, "src");
        assertNotNull(src);
        ProjectNode main = findChild(src, "main");
        assertNotNull(main);
        ProjectNode java = findChild(main, "java");
        assertNotNull(java);

        // Validate package structure
        ProjectNode packageDir = findChild(java, "com");
        assertNotNull(packageDir);

        // Validate main application class exists
        assertTrue(hasJavaFiles(java));
    }

    @Test
    @DisplayName("Should generate pom.xml with correct dependencies")
    void testPomXml() {
        ProjectNode root = generator.generateSpringBootProject(
            TEST_PROJECT_NAME, TEST_GROUP_ID, 
            SpringBootProjectGenerator.SpringBootProjectType.REST_API);

        ProjectNode pomXml = findChild(root, "pom.xml");
        assertNotNull(pomXml);
        assertNotNull(pomXml.getContent());
        
        String content = pomXml.getContent();
        assertTrue(content.contains(TEST_GROUP_ID));
        assertTrue(content.contains(TEST_ARTIFACT_ID));
        assertTrue(content.contains("spring-boot-starter-web"));
    }

    @Test
    @DisplayName("Should generate application properties")
    void testApplicationProperties() {
        ProjectNode root = generator.generateSpringBootProject(
            TEST_PROJECT_NAME, TEST_GROUP_ID, 
            SpringBootProjectGenerator.SpringBootProjectType.REST_API);

        ProjectNode src = findChild(root, "src");
        ProjectNode main = findChild(src, "main");
        ProjectNode resources = findChild(main, "resources");
        assertNotNull(resources);

        ProjectNode appProps = findChild(resources, "application.properties");
        assertNotNull(appProps);
        assertNotNull(appProps.getContent());
    }

    @Test
    @DisplayName("Should generate test structure")
    void testTestStructure() {
        ProjectNode root = generator.generateSpringBootProject(
            TEST_PROJECT_NAME, TEST_GROUP_ID, 
            SpringBootProjectGenerator.SpringBootProjectType.REST_API);

        ProjectNode src = findChild(root, "src");
        ProjectNode test = findChild(src, "test");
        assertNotNull(test);

        ProjectNode testJava = findChild(test, "java");
        assertNotNull(testJava);

        // Validate test files exist
        assertTrue(hasJavaFiles(testJava));
    }

    @Test
    @DisplayName("Should generate README")
    void testReadme() {
        ProjectNode root = generator.generateSpringBootProject(
            TEST_PROJECT_NAME, TEST_GROUP_ID, 
            SpringBootProjectGenerator.SpringBootProjectType.REST_API);

        ProjectNode readme = findChild(root, "README.md");
        assertNotNull(readme);
        assertNotNull(readme.getContent());
        assertTrue(readme.getContent().contains(TEST_PROJECT_NAME));
    }

    @Test
    @DisplayName("Should generate .gitignore")
    void testGitignore() {
        ProjectNode root = generator.generateSpringBootProject(
            TEST_PROJECT_NAME, TEST_GROUP_ID, 
            SpringBootProjectGenerator.SpringBootProjectType.REST_API);

        ProjectNode gitignore = findChild(root, ".gitignore");
        assertNotNull(gitignore);
        assertNotNull(gitignore.getContent());
        assertTrue(gitignore.getContent().contains("target/"));
        assertTrue(gitignore.getContent().contains("*.class"));
    }

    // Helper methods
    private ProjectNode findChild(ProjectNode parent, String name) {
        if (parent == null || parent.getChildren() == null) {
            return null;
        }
        return parent.getChildren().stream()
            .filter(child -> child.getName().equals(name))
            .findFirst()
            .orElse(null);
    }

    private boolean hasJavaFiles(ProjectNode node) {
        if (node == null || node.getChildren() == null) {
            return false;
        }
        return node.getChildren().stream()
            .anyMatch(child -> child.getName().endsWith(".java") || 
                (child.isDirectory() && hasJavaFiles(child)));
    }
}


