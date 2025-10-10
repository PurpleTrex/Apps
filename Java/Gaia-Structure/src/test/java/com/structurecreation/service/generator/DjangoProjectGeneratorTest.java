package com.structurecreation.service.generator;

import com.structurecreation.model.ProjectNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive regression tests for DjangoProjectGenerator.
 * Tests all project types and validates complete project structure generation.
 */
class DjangoProjectGeneratorTest {

    private DjangoProjectGenerator generator;

    private static final String TEST_PROJECT_NAME = "TestDjangoProject";

    @BeforeEach
    void setUp() {
        generator = new DjangoProjectGenerator();
        assertNotNull(generator, "DjangoProjectGenerator should be initialized");
    }

    @ParameterizedTest
    @EnumSource(DjangoProjectGenerator.DjangoProjectType.class)
    @DisplayName("Should generate complete project structure for all Django project types")
    void testGenerateProjectForAllTypes(DjangoProjectGenerator.DjangoProjectType type) {
        // Generate project
        ProjectNode root = generator.generateDjangoProject(TEST_PROJECT_NAME, type);

        // Validate root
        assertNotNull(root, "Root project node should not be null");
        assertEquals(TEST_PROJECT_NAME, root.getName(), "Project name should match");
        assertTrue(root.isDirectory(), "Root should be a directory");
        assertFalse(root.getChildren().isEmpty(), "Project should have children");
    }

    @Test
    @DisplayName("Should generate REST API project with DRF components")
    void testRESTAPIProjectStructure() {
        ProjectNode root = generator.generateDjangoProject(TEST_PROJECT_NAME, 
            DjangoProjectGenerator.DjangoProjectType.REST_API);

        // Validate main project directory
        ProjectNode projectDir = findChild(root, TEST_PROJECT_NAME);
        assertNotNull(projectDir);
        
        // Validate settings structure
        assertHasChild(projectDir, "settings", true);
        ProjectNode settingsDir = findChild(projectDir, "settings");
        assertHasChild(settingsDir, "base.py", false);
        assertHasChild(settingsDir, "local.py", false);
        assertHasChild(settingsDir, "production.py", false);
        assertHasChild(settingsDir, "test.py", false);

        // Validate main app
        assertHasChild(root, "apps", true);
        ProjectNode appsDir = findChild(root, "apps");
        assertHasChild(appsDir, "users", true);
        assertHasChild(appsDir, "api", true);
    }

    @Test
    @DisplayName("Should generate Full Stack project with templates")
    void testFullStackProjectStructure() {
        ProjectNode root = generator.generateDjangoProject(TEST_PROJECT_NAME, 
            DjangoProjectGenerator.DjangoProjectType.FULL_STACK);

        // Validate static files
        assertHasChild(root, "static", true);
        ProjectNode staticDir = findChild(root, "static");
        assertHasChild(staticDir, "css", true);
        assertHasChild(staticDir, "js", true);
        assertHasChild(staticDir, "img", true);

        // Validate templates
        assertHasChild(root, "templates", true);
        ProjectNode templatesDir = findChild(root, "templates");
        assertHasChild(templatesDir, "base.html", false);
        assertHasChild(templatesDir, "index.html", false);
    }

    @Test
    @DisplayName("Should generate all configuration files")
    void testConfigurationFiles() {
        ProjectNode root = generator.generateDjangoProject(TEST_PROJECT_NAME, 
            DjangoProjectGenerator.DjangoProjectType.REST_API);

        // Validate Python configuration files
        assertHasChild(root, "requirements.txt", false);
        assertHasChild(root, "requirements-dev.txt", false);
        assertHasChild(root, ".env.example", false);
        assertHasChild(root, ".gitignore", false);
        assertHasChild(root, "pyproject.toml", false);
        assertHasChild(root, "setup.cfg", false);
        assertHasChild(root, "pytest.ini", false);
        assertHasChild(root, ".pre-commit-config.yaml", false);
        assertHasChild(root, ".editorconfig", false);
        assertHasChild(root, "manage.py", false);

        // Validate content
        ProjectNode requirements = findChild(root, "requirements.txt");
        assertNotNull(requirements);
        assertNotNull(requirements.getContent());
        assertTrue(requirements.getContent().contains("Django"));
    }

    @Test
    @DisplayName("Should generate test infrastructure")
    void testTestInfrastructure() {
        ProjectNode root = generator.generateDjangoProject(TEST_PROJECT_NAME, 
            DjangoProjectGenerator.DjangoProjectType.REST_API);

        assertHasChild(root, "tests", true);
        ProjectNode testsDir = findChild(root, "tests");
        assertNotNull(testsDir);
        
        assertHasChild(testsDir, "conftest.py", false);
        assertHasChild(testsDir, "test_base.py", false);

        // Validate conftest content
        ProjectNode conftest = findChild(testsDir, "conftest.py");
        assertNotNull(conftest);
        assertNotNull(conftest.getContent());
        assertTrue(conftest.getContent().contains("pytest.fixture"));
    }

    @Test
    @DisplayName("Should generate validators and permissions")
    void testUtilityFiles() {
        ProjectNode root = generator.generateDjangoProject(TEST_PROJECT_NAME, 
            DjangoProjectGenerator.DjangoProjectType.REST_API);

        ProjectNode appsDir = findChild(root, "apps");
        ProjectNode coreDir = findChild(appsDir, "core");
        assertNotNull(coreDir);

        // Validate validators
        ProjectNode validators = findChild(coreDir, "validators.py");
        assertNotNull(validators);
        assertNotNull(validators.getContent());
        assertTrue(validators.getContent().contains("validate_username"));

        // Validate permissions
        ProjectNode permissions = findChild(coreDir, "permissions.py");
        assertNotNull(permissions);
        assertNotNull(permissions.getContent());
        assertTrue(permissions.getContent().contains("IsOwnerOrReadOnly"));

        // Validate mixins
        ProjectNode mixins = findChild(coreDir, "mixins.py");
        assertNotNull(mixins);
        assertNotNull(mixins.getContent());
        assertTrue(mixins.getContent().contains("SuperUserRequiredMixin"));
    }

    @Test
    @DisplayName("Should generate Docker support")
    void testDockerSupport() {
        ProjectNode root = generator.generateDjangoProject(TEST_PROJECT_NAME, 
            DjangoProjectGenerator.DjangoProjectType.REST_API);

        assertHasChild(root, "Dockerfile", false);
        assertHasChild(root, "docker-compose.yml", false);
        assertHasChild(root, ".dockerignore", false);
        assertHasChild(root, "docker", true);

        ProjectNode dockerDir = findChild(root, "docker");
        assertNotNull(dockerDir);
        assertHasChild(dockerDir, "nginx.conf", false);
        assertHasChild(dockerDir, "entrypoint.sh", false);

        // Validate Dockerfile content
        ProjectNode dockerfile = findChild(root, "Dockerfile");
        assertNotNull(dockerfile);
        assertTrue(dockerfile.getContent().contains("FROM python"));
        assertTrue(dockerfile.getContent().contains("Django"));
    }

    @Test
    @DisplayName("Should generate CI/CD workflows")
    void testCICDPipeline() {
        ProjectNode root = generator.generateDjangoProject(TEST_PROJECT_NAME, 
            DjangoProjectGenerator.DjangoProjectType.REST_API);

        assertHasChild(root, ".github", true);
        ProjectNode githubDir = findChild(root, ".github");
        assertNotNull(githubDir);

        assertHasChild(githubDir, "workflows", true);
        ProjectNode workflowsDir = findChild(githubDir, "workflows");
        assertHasChild(workflowsDir, "ci.yml", false);
        assertHasChild(workflowsDir, "deploy.yml", false);
        assertHasChild(workflowsDir, "dependabot.yml", false);

        // Validate CI workflow
        ProjectNode ciWorkflow = findChild(workflowsDir, "ci.yml");
        assertNotNull(ciWorkflow);
        assertTrue(ciWorkflow.getContent().contains("pytest"));
    }

    @Test
    @DisplayName("Should generate scripts")
    void testScripts() {
        ProjectNode root = generator.generateDjangoProject(TEST_PROJECT_NAME, 
            DjangoProjectGenerator.DjangoProjectType.REST_API);

        assertHasChild(root, "scripts", true);
        ProjectNode scriptsDir = findChild(root, "scripts");
        assertNotNull(scriptsDir);

        assertHasChild(scriptsDir, "run_dev.sh", false);
        assertHasChild(scriptsDir, "migrate.sh", false);

        // Validate script content
        ProjectNode runDev = findChild(scriptsDir, "run_dev.sh");
        assertNotNull(runDev);
        assertTrue(runDev.getContent().contains("#!/bin/bash"));
        assertTrue(runDev.getContent().contains("python manage.py runserver"));
    }

    @Test
    @DisplayName("Should generate static assets")
    void testStaticAssets() {
        ProjectNode root = generator.generateDjangoProject(TEST_PROJECT_NAME, 
            DjangoProjectGenerator.DjangoProjectType.FULL_STACK);

        ProjectNode staticDir = findChild(root, "static");
        assertNotNull(staticDir);

        // Validate CSS
        ProjectNode cssDir = findChild(staticDir, "css");
        assertNotNull(cssDir);
        ProjectNode mainCss = findChild(cssDir, "main.css");
        assertNotNull(mainCss);
        assertTrue(mainCss.getContent().contains(":root"));
        assertTrue(mainCss.getContent().contains("--primary-color"));

        // Validate JS
        ProjectNode jsDir = findChild(staticDir, "js");
        assertNotNull(jsDir);
        ProjectNode mainJs = findChild(jsDir, "main.js");
        assertNotNull(mainJs);
        assertTrue(mainJs.getContent().contains("getCookie"));
        assertTrue(mainJs.getContent().contains("csrftoken"));
    }

    @Test
    @DisplayName("Should generate templates")
    void testTemplates() {
        ProjectNode root = generator.generateDjangoProject(TEST_PROJECT_NAME, 
            DjangoProjectGenerator.DjangoProjectType.FULL_STACK);

        ProjectNode templatesDir = findChild(root, "templates");
        assertNotNull(templatesDir);

        // Validate base template
        ProjectNode baseTemplate = findChild(templatesDir, "base.html");
        assertNotNull(baseTemplate);
        assertTrue(baseTemplate.getContent().contains("{% load static %}"));
        assertTrue(baseTemplate.getContent().contains("{% block content %}"));

        // Validate index template
        ProjectNode indexTemplate = findChild(templatesDir, "index.html");
        assertNotNull(indexTemplate);
        assertTrue(indexTemplate.getContent().contains("{% extends"));
    }

    @Test
    @DisplayName("Should generate API documentation")
    void testAPIDocumentation() {
        ProjectNode root = generator.generateDjangoProject(TEST_PROJECT_NAME, 
            DjangoProjectGenerator.DjangoProjectType.REST_API);

        assertHasChild(root, "docs", true);
        ProjectNode docsDir = findChild(root, "docs");
        assertNotNull(docsDir);

        ProjectNode apiDocs = findChild(docsDir, "api.md");
        assertNotNull(apiDocs);
        assertTrue(apiDocs.getContent().contains("# API Documentation"));
        assertTrue(apiDocs.getContent().contains("/api/v1/"));
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

