package com.structurecreation.service.generator;

import com.structurecreation.model.ProjectNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive regression tests for FlaskProjectGenerator.
 * Tests all project types and validates complete project structure generation.
 */
class FlaskProjectGeneratorTest {

    private FlaskProjectGenerator generator;

    private static final String TEST_PROJECT_NAME = "TestFlaskProject";

    @BeforeEach
    void setUp() {
        generator = new FlaskProjectGenerator();
        assertNotNull(generator, "FlaskProjectGenerator should be initialized");
    }

    @ParameterizedTest
    @EnumSource(FlaskProjectGenerator.FlaskProjectType.class)
    @DisplayName("Should generate complete project structure for all Flask project types")
    void testGenerateProjectForAllTypes(FlaskProjectGenerator.FlaskProjectType type) {
        // Generate project
        ProjectNode root = generator.generateFlaskProject(TEST_PROJECT_NAME, type);

        // Validate root
        assertNotNull(root, "Root project node should not be null");
        assertEquals(TEST_PROJECT_NAME, root.getName(), "Project name should match");
        assertTrue(root.isDirectory(), "Root should be a directory");
        assertFalse(root.getChildren().isEmpty(), "Project should have children");
    }

    @Test
    @DisplayName("Should generate REST API project with all required components")
    void testRESTAPIProjectStructure() {
        ProjectNode root = generator.generateFlaskProject(TEST_PROJECT_NAME, 
            FlaskProjectGenerator.FlaskProjectType.REST_API);

        // Validate main directories
        assertHasChild(root, "app", true);
        assertHasChild(root, "tests", true);
        assertHasChild(root, "scripts", true);
        assertHasChild(root, "docs", true);
        assertHasChild(root, "static", true);
        assertHasChild(root, "templates", true);

        // Validate app structure
        ProjectNode appDir = findChild(root, "app");
        assertNotNull(appDir);
        assertHasChild(appDir, "__init__.py", false);
        assertHasChild(appDir, "config.py", false);
        assertHasChild(appDir, "extensions.py", false);
        assertHasChild(appDir, "models", true);
        assertHasChild(appDir, "api", true);
        assertHasChild(appDir, "utils", true);

        // Validate API structure
        ProjectNode apiDir = findChild(appDir, "api");
        assertNotNull(apiDir);
        ProjectNode v1Dir = findChild(apiDir, "v1");
        assertNotNull(v1Dir);
        assertHasChild(v1Dir, "resources", true);
        assertHasChild(v1Dir, "schemas", true);
        assertHasChild(v1Dir, "routes.py", false);

        // Validate resources
        ProjectNode resourcesDir = findChild(v1Dir, "resources");
        assertNotNull(resourcesDir);
        assertHasChild(resourcesDir, "user.py", false);
        assertHasChild(resourcesDir, "auth.py", false);
    }

    @Test
    @DisplayName("Should generate Web App project with views and forms")
    void testWebAppProjectStructure() {
        ProjectNode root = generator.generateFlaskProject(TEST_PROJECT_NAME, 
            FlaskProjectGenerator.FlaskProjectType.WEB_APP);

        ProjectNode appDir = findChild(root, "app");
        assertNotNull(appDir);

        // Validate web app specific structure
        assertHasChild(appDir, "views", true);
        assertHasChild(appDir, "forms", true);

        // Validate views
        ProjectNode viewsDir = findChild(appDir, "views");
        assertNotNull(viewsDir);
        assertHasChild(viewsDir, "main.py", false);
        assertHasChild(viewsDir, "auth.py", false);
        assertHasChild(viewsDir, "admin.py", false);

        // Validate forms
        ProjectNode formsDir = findChild(appDir, "forms");
        assertNotNull(formsDir);
        assertHasChild(formsDir, "auth_forms.py", false);
        assertHasChild(formsDir, "user_forms.py", false);
    }

    @Test
    @DisplayName("Should generate Microservice project with services")
    void testMicroserviceProjectStructure() {
        ProjectNode root = generator.generateFlaskProject(TEST_PROJECT_NAME, 
            FlaskProjectGenerator.FlaskProjectType.MICROSERVICE);

        ProjectNode appDir = findChild(root, "app");
        assertNotNull(appDir);

        // Validate microservice specific structure
        assertHasChild(appDir, "services", true);
        assertHasChild(appDir, "handlers", true);

        // Validate services
        ProjectNode servicesDir = findChild(appDir, "services");
        assertNotNull(servicesDir);
        assertHasChild(servicesDir, "data_service.py", false);
        assertHasChild(servicesDir, "cache_service.py", false);
        assertHasChild(servicesDir, "message_service.py", false);

        // Validate handlers
        ProjectNode handlersDir = findChild(appDir, "handlers");
        assertNotNull(handlersDir);
        assertHasChild(handlersDir, "event_handlers.py", false);
        assertHasChild(handlersDir, "error_handlers.py", false);
    }

    @Test
    @DisplayName("Should generate Data API project with ML components")
    void testDataAPIProjectStructure() {
        ProjectNode root = generator.generateFlaskProject(TEST_PROJECT_NAME, 
            FlaskProjectGenerator.FlaskProjectType.DATA_API);

        ProjectNode appDir = findChild(root, "app");
        assertNotNull(appDir);

        // Validate data API specific structure
        assertHasChild(appDir, "data", true);
        assertHasChild(appDir, "ml", true);

        // Validate data processing
        ProjectNode dataDir = findChild(appDir, "data");
        assertNotNull(dataDir);
        assertHasChild(dataDir, "processors.py", false);
        assertHasChild(dataDir, "transformers.py", false);
        assertHasChild(dataDir, "analyzers.py", false);

        // Validate ML components
        ProjectNode mlDir = findChild(appDir, "ml");
        assertNotNull(mlDir);
        assertHasChild(mlDir, "models.py", false);
        assertHasChild(mlDir, "predictors.py", false);
    }

    @Test
    @DisplayName("Should generate Async App project with async handlers")
    void testAsyncAppProjectStructure() {
        ProjectNode root = generator.generateFlaskProject(TEST_PROJECT_NAME, 
            FlaskProjectGenerator.FlaskProjectType.ASYNC_APP);

        ProjectNode appDir = findChild(root, "app");
        assertNotNull(appDir);

        // Validate async specific structure
        assertHasChild(appDir, "async_handlers", true);

        ProjectNode asyncDir = findChild(appDir, "async_handlers");
        assertNotNull(asyncDir);
        assertHasChild(asyncDir, "views.py", false);
        assertHasChild(asyncDir, "tasks.py", false);
        assertHasChild(asyncDir, "workers.py", false);
    }

    @Test
    @DisplayName("Should generate GraphQL API project")
    void testGraphQLAPIProjectStructure() {
        ProjectNode root = generator.generateFlaskProject(TEST_PROJECT_NAME, 
            FlaskProjectGenerator.FlaskProjectType.GRAPHQL_API);

        ProjectNode appDir = findChild(root, "app");
        assertNotNull(appDir);

        // Validate GraphQL structure
        assertHasChild(appDir, "graphql", true);

        ProjectNode graphqlDir = findChild(appDir, "graphql");
        assertNotNull(graphqlDir);
        assertHasChild(graphqlDir, "schema.py", false);
        assertHasChild(graphqlDir, "queries.py", false);
        assertHasChild(graphqlDir, "mutations.py", false);
        assertHasChild(graphqlDir, "resolvers.py", false);
    }

    @Test
    @DisplayName("Should generate WebSocket App project")
    void testWebSocketAppProjectStructure() {
        ProjectNode root = generator.generateFlaskProject(TEST_PROJECT_NAME, 
            FlaskProjectGenerator.FlaskProjectType.WEBSOCKET_APP);

        ProjectNode appDir = findChild(root, "app");
        assertNotNull(appDir);

        // Validate WebSocket structure
        assertHasChild(appDir, "websocket", true);

        ProjectNode wsDir = findChild(appDir, "websocket");
        assertNotNull(wsDir);
        assertHasChild(wsDir, "events.py", false);
        assertHasChild(wsDir, "handlers.py", false);
        assertHasChild(wsDir, "rooms.py", false);
    }

    @Test
    @DisplayName("Should generate all configuration files")
    void testConfigurationFiles() {
        ProjectNode root = generator.generateFlaskProject(TEST_PROJECT_NAME, 
            FlaskProjectGenerator.FlaskProjectType.REST_API);

        // Validate configuration files
        assertHasChild(root, "requirements.txt", false);
        assertHasChild(root, "requirements-dev.txt", false);
        assertHasChild(root, ".env.example", false);
        assertHasChild(root, ".gitignore", false);
        assertHasChild(root, "pyproject.toml", false);
        assertHasChild(root, "setup.cfg", false);
        assertHasChild(root, "setup.py", false);
        assertHasChild(root, "pytest.ini", false);
        assertHasChild(root, ".flaskenv", false);
        assertHasChild(root, "README.md", false);
        assertHasChild(root, "Makefile", false);

        // Validate content is not empty
        ProjectNode requirements = findChild(root, "requirements.txt");
        assertNotNull(requirements);
        assertNotNull(requirements.getContent());
        assertFalse(requirements.getContent().isEmpty());
        assertTrue(requirements.getContent().contains("Flask"));
    }

    @Test
    @DisplayName("Should generate Docker support files")
    void testDockerSupport() {
        ProjectNode root = generator.generateFlaskProject(TEST_PROJECT_NAME, 
            FlaskProjectGenerator.FlaskProjectType.REST_API);

        // Validate Docker files
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
        assertNotNull(dockerfile.getContent());
        assertTrue(dockerfile.getContent().contains("FROM python"));
        assertTrue(dockerfile.getContent().contains("WORKDIR /app"));
    }

    @Test
    @DisplayName("Should generate CI/CD workflows")
    void testCICDPipeline() {
        ProjectNode root = generator.generateFlaskProject(TEST_PROJECT_NAME, 
            FlaskProjectGenerator.FlaskProjectType.REST_API);

        // Validate GitHub Actions structure
        assertHasChild(root, ".github", true);
        ProjectNode githubDir = findChild(root, ".github");
        assertNotNull(githubDir);

        assertHasChild(githubDir, "workflows", true);
        ProjectNode workflowsDir = findChild(githubDir, "workflows");
        assertNotNull(workflowsDir);

        assertHasChild(workflowsDir, "ci.yml", false);
        assertHasChild(workflowsDir, "deploy.yml", false);

        // Validate CI workflow content
        ProjectNode ciWorkflow = findChild(workflowsDir, "ci.yml");
        assertNotNull(ciWorkflow);
        assertNotNull(ciWorkflow.getContent());
        assertTrue(ciWorkflow.getContent().contains("name: CI"));
        assertTrue(ciWorkflow.getContent().contains("pytest"));
    }

    @Test
    @DisplayName("Should generate test infrastructure")
    void testTestInfrastructure() {
        ProjectNode root = generator.generateFlaskProject(TEST_PROJECT_NAME, 
            FlaskProjectGenerator.FlaskProjectType.REST_API);

        ProjectNode testsDir = findChild(root, "tests");
        assertNotNull(testsDir);

        assertHasChild(testsDir, "__init__.py", false);
        assertHasChild(testsDir, "conftest.py", false);
        assertHasChild(testsDir, "test_models.py", false);
        assertHasChild(testsDir, "test_api.py", false);

        // Validate conftest content
        ProjectNode conftest = findChild(testsDir, "conftest.py");
        assertNotNull(conftest);
        assertNotNull(conftest.getContent());
        assertTrue(conftest.getContent().contains("pytest.fixture"));
        assertTrue(conftest.getContent().contains("test_client"));
    }

    @Test
    @DisplayName("Should generate utility files with proper content")
    void testUtilityFiles() {
        ProjectNode root = generator.generateFlaskProject(TEST_PROJECT_NAME, 
            FlaskProjectGenerator.FlaskProjectType.REST_API);

        ProjectNode appDir = findChild(root, "app");
        ProjectNode utilsDir = findChild(appDir, "utils");
        assertNotNull(utilsDir);

        // Validate validators
        ProjectNode validators = findChild(utilsDir, "validators.py");
        assertNotNull(validators);
        assertNotNull(validators.getContent());
        assertTrue(validators.getContent().contains("validate_username"));
        assertTrue(validators.getContent().contains("PasswordStrength"));

        // Validate decorators
        ProjectNode decorators = findChild(utilsDir, "decorators.py");
        assertNotNull(decorators);
        assertNotNull(decorators.getContent());
        assertTrue(decorators.getContent().contains("admin_required"));
        assertTrue(decorators.getContent().contains("role_required"));

        // Validate helpers
        ProjectNode helpers = findChild(utilsDir, "helpers.py");
        assertNotNull(helpers);
        assertNotNull(helpers.getContent());
        assertTrue(helpers.getContent().contains("generate_token"));
        assertTrue(helpers.getContent().contains("paginate_query"));
    }

    @Test
    @DisplayName("Should generate static assets")
    void testStaticAssets() {
        ProjectNode root = generator.generateFlaskProject(TEST_PROJECT_NAME, 
            FlaskProjectGenerator.FlaskProjectType.WEB_APP);

        ProjectNode staticDir = findChild(root, "static");
        assertNotNull(staticDir);

        // Validate CSS
        ProjectNode cssDir = findChild(staticDir, "css");
        assertNotNull(cssDir);
        ProjectNode mainCss = findChild(cssDir, "style.css");
        assertNotNull(mainCss);
        assertNotNull(mainCss.getContent());
        assertTrue(mainCss.getContent().contains(":root"));
        assertTrue(mainCss.getContent().contains("--primary-color"));

        // Validate JS
        ProjectNode jsDir = findChild(staticDir, "js");
        assertNotNull(jsDir);
        ProjectNode mainJs = findChild(jsDir, "app.js");
        assertNotNull(mainJs);
        assertNotNull(mainJs.getContent());
        assertTrue(mainJs.getContent().contains("DOMContentLoaded"));
        assertTrue(mainJs.getContent().contains("validateForm"));
    }

    @Test
    @DisplayName("Should generate templates with proper structure")
    void testTemplates() {
        ProjectNode root = generator.generateFlaskProject(TEST_PROJECT_NAME, 
            FlaskProjectGenerator.FlaskProjectType.WEB_APP);

        ProjectNode templatesDir = findChild(root, "templates");
        assertNotNull(templatesDir);

        // Validate base template
        ProjectNode baseTemplate = findChild(templatesDir, "base.html");
        assertNotNull(baseTemplate);
        assertNotNull(baseTemplate.getContent());
        assertTrue(baseTemplate.getContent().contains("<!DOCTYPE html>"));
        assertTrue(baseTemplate.getContent().contains("{% block content %}"));

        // Validate index template
        ProjectNode indexTemplate = findChild(templatesDir, "index.html");
        assertNotNull(indexTemplate);
        assertNotNull(indexTemplate.getContent());
        assertTrue(indexTemplate.getContent().contains("{% extends"));

        // Validate error templates
        ProjectNode errorDir = findChild(templatesDir, "errors");
        assertNotNull(errorDir);
        assertHasChild(errorDir, "404.html", false);
        assertHasChild(errorDir, "500.html", false);
    }

    @Test
    @DisplayName("Should generate database scripts")
    void testDatabaseScripts() {
        ProjectNode root = generator.generateFlaskProject(TEST_PROJECT_NAME, 
            FlaskProjectGenerator.FlaskProjectType.REST_API);

        ProjectNode scriptsDir = findChild(root, "scripts");
        assertNotNull(scriptsDir);

        // Validate init script
        ProjectNode initDb = findChild(scriptsDir, "init_db.py");
        assertNotNull(initDb);
        assertNotNull(initDb.getContent());
        assertTrue(initDb.getContent().contains("def init_db()"));
        assertTrue(initDb.getContent().contains("db.create_all()"));

        // Validate seed script
        ProjectNode seedDb = findChild(scriptsDir, "seed_db.py");
        assertNotNull(seedDb);
        assertNotNull(seedDb.getContent());
        assertTrue(seedDb.getContent().contains("def seed()"));
    }

    @Test
    @DisplayName("Should generate API documentation")
    void testAPIDocumentation() {
        ProjectNode root = generator.generateFlaskProject(TEST_PROJECT_NAME, 
            FlaskProjectGenerator.FlaskProjectType.REST_API);

        ProjectNode docsDir = findChild(root, "docs");
        assertNotNull(docsDir);

        ProjectNode apiDocs = findChild(docsDir, "api.md");
        assertNotNull(apiDocs);
        assertNotNull(apiDocs.getContent());
        assertTrue(apiDocs.getContent().contains("# API Documentation"));
        assertTrue(apiDocs.getContent().contains("POST /api/v1/auth/login"));
    }

    @Test
    @DisplayName("Should generate models with proper structure")
    void testModels() {
        ProjectNode root = generator.generateFlaskProject(TEST_PROJECT_NAME, 
            FlaskProjectGenerator.FlaskProjectType.REST_API);

        ProjectNode appDir = findChild(root, "app");
        ProjectNode modelsDir = findChild(appDir, "models");
        assertNotNull(modelsDir);

        // Validate base model
        ProjectNode baseModel = findChild(modelsDir, "base.py");
        assertNotNull(baseModel);
        assertNotNull(baseModel.getContent());
        assertTrue(baseModel.getContent().contains("class BaseModel"));
        assertTrue(baseModel.getContent().contains("db.Model"));

        // Validate user model
        ProjectNode userModel = findChild(modelsDir, "user.py");
        assertNotNull(userModel);
        assertNotNull(userModel.getContent());
        assertTrue(userModel.getContent().contains("class User"));
        assertTrue(userModel.getContent().contains("set_password"));
        assertTrue(userModel.getContent().contains("check_password"));
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

    private List<ProjectNode> getAllFiles(ProjectNode node) {
        return node.getChildren().stream()
            .flatMap(child -> {
                if (child.isDirectory()) {
                    return getAllFiles(child).stream();
                } else {
                    return java.util.stream.Stream.of(child);
                }
            })
            .collect(Collectors.toList());
    }
}
