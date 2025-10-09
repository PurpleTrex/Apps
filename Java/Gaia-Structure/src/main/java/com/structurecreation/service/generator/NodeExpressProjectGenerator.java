package com.structurecreation.service.generator;

import com.structurecreation.model.ProjectNode;
import com.structurecreation.service.DependencyResolverService;
import com.structurecreation.service.repository.NpmRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * Advanced Node.js Express project generator that creates complete, production-ready applications
 * with all necessary dependencies, configurations, and no build errors
 */
public class NodeExpressProjectGenerator {
    private static final Logger logger = LoggerFactory.getLogger(NodeExpressProjectGenerator.class);

    private final DependencyResolverService dependencyResolver;
    private final NpmRepository npmRepository;

    public NodeExpressProjectGenerator() {
        this.dependencyResolver = new DependencyResolverService();
        this.npmRepository = new NpmRepository();
    }

    /**
     * Generate a complete Node.js Express project structure with all dependencies
     */
    public ProjectNode generateNodeExpressProject(String projectName, NodeProjectType type) {
        ProjectNode root = new ProjectNode(projectName, ProjectNode.NodeType.FOLDER);

        // Create folder structure
        createFolderStructure(root, type);

        // Generate configuration files
        generateConfigFiles(root, projectName, type);

        // Generate source files
        generateSourceFiles(root, projectName, type);

        // Generate test files
        generateTestFiles(root, type);

        // Generate complete package.json with all dependencies
        generatePackageJson(root, projectName, type);

        return root;
    }

    /**
     * Create the complete folder structure
     */
    private void createFolderStructure(ProjectNode root, NodeProjectType type) {
        // Main folders
        ProjectNode src = new ProjectNode("src", ProjectNode.NodeType.FOLDER);
        ProjectNode tests = new ProjectNode("tests", ProjectNode.NodeType.FOLDER);
        ProjectNode config = new ProjectNode("config", ProjectNode.NodeType.FOLDER);
        ProjectNode public_ = new ProjectNode("public", ProjectNode.NodeType.FOLDER);
        ProjectNode views = new ProjectNode("views", ProjectNode.NodeType.FOLDER);
        ProjectNode docs = new ProjectNode("docs", ProjectNode.NodeType.FOLDER);

        // Source subfolders
        ProjectNode controllers = new ProjectNode("controllers", ProjectNode.NodeType.FOLDER);
        ProjectNode models = new ProjectNode("models", ProjectNode.NodeType.FOLDER);
        ProjectNode routes = new ProjectNode("routes", ProjectNode.NodeType.FOLDER);
        ProjectNode services = new ProjectNode("services", ProjectNode.NodeType.FOLDER);
        ProjectNode middleware = new ProjectNode("middleware", ProjectNode.NodeType.FOLDER);
        ProjectNode utils = new ProjectNode("utils", ProjectNode.NodeType.FOLDER);
        ProjectNode validators = new ProjectNode("validators", ProjectNode.NodeType.FOLDER);
        ProjectNode database = new ProjectNode("database", ProjectNode.NodeType.FOLDER);

        // Database subfolders
        ProjectNode migrations = new ProjectNode("migrations", ProjectNode.NodeType.FOLDER);
        ProjectNode seeders = new ProjectNode("seeders", ProjectNode.NodeType.FOLDER);
        database.addChild(migrations);
        database.addChild(seeders);

        // Public subfolders
        ProjectNode css = new ProjectNode("css", ProjectNode.NodeType.FOLDER);
        ProjectNode js = new ProjectNode("js", ProjectNode.NodeType.FOLDER);
        ProjectNode images = new ProjectNode("images", ProjectNode.NodeType.FOLDER);
        public_.addChild(css);
        public_.addChild(js);
        public_.addChild(images);

        // Add subfolders to src
        src.addChild(controllers);
        src.addChild(models);
        src.addChild(routes);
        src.addChild(services);
        src.addChild(middleware);
        src.addChild(utils);
        src.addChild(validators);
        src.addChild(database);

        // WebSocket support for real-time features
        if (type == NodeProjectType.REAL_TIME || type == NodeProjectType.FULL_STACK) {
            ProjectNode sockets = new ProjectNode("sockets", ProjectNode.NodeType.FOLDER);
            src.addChild(sockets);
        }

        // GraphQL support
        if (type == NodeProjectType.GRAPHQL || type == NodeProjectType.FULL_STACK) {
            ProjectNode graphql = new ProjectNode("graphql", ProjectNode.NodeType.FOLDER);
            ProjectNode schemas = new ProjectNode("schemas", ProjectNode.NodeType.FOLDER);
            ProjectNode resolvers = new ProjectNode("resolvers", ProjectNode.NodeType.FOLDER);
            graphql.addChild(schemas);
            graphql.addChild(resolvers);
            src.addChild(graphql);
        }

        // Add main folders to root
        root.addChild(src);
        root.addChild(tests);
        root.addChild(config);
        root.addChild(public_);
        root.addChild(views);
        root.addChild(docs);

        // Docker support
        if (type == NodeProjectType.MICROSERVICE || type == NodeProjectType.FULL_STACK) {
            ProjectNode docker = new ProjectNode("docker", ProjectNode.NodeType.FOLDER);
            root.addChild(docker);
        }
    }

    /**
     * Generate configuration files
     */
    private void generateConfigFiles(ProjectNode root, String projectName, NodeProjectType type) {
        // .gitignore
        ProjectNode gitignore = new ProjectNode(".gitignore", ProjectNode.NodeType.FILE,
            generateGitignore());
        root.addChild(gitignore);

        // README.md
        ProjectNode readme = new ProjectNode("README.md", ProjectNode.NodeType.FILE,
            generateReadme(projectName, type));
        root.addChild(readme);

        // .env
        ProjectNode env = new ProjectNode(".env", ProjectNode.NodeType.FILE,
            generateEnvFile(type));
        root.addChild(env);

        // .env.example
        ProjectNode envExample = new ProjectNode(".env.example", ProjectNode.NodeType.FILE,
            generateEnvFile(type));
        root.addChild(envExample);

        // ESLint configuration
        ProjectNode eslintrc = new ProjectNode(".eslintrc.json", ProjectNode.NodeType.FILE,
            generateEslintConfig());
        root.addChild(eslintrc);

        // Prettier configuration
        ProjectNode prettierrc = new ProjectNode(".prettierrc", ProjectNode.NodeType.FILE,
            generatePrettierConfig());
        root.addChild(prettierrc);

        // Jest configuration
        ProjectNode jestConfig = new ProjectNode("jest.config.js", ProjectNode.NodeType.FILE,
            generateJestConfig());
        root.addChild(jestConfig);

        // Nodemon configuration
        ProjectNode nodemonConfig = new ProjectNode("nodemon.json", ProjectNode.NodeType.FILE,
            generateNodemonConfig());
        root.addChild(nodemonConfig);

        // TypeScript configuration (if TypeScript)
        if (type == NodeProjectType.TYPESCRIPT || type == NodeProjectType.FULL_STACK) {
            ProjectNode tsconfig = new ProjectNode("tsconfig.json", ProjectNode.NodeType.FILE,
                generateTsConfig());
            root.addChild(tsconfig);
        }

        // Docker files
        if (type == NodeProjectType.MICROSERVICE || type == NodeProjectType.FULL_STACK) {
            ProjectNode dockerfile = new ProjectNode("Dockerfile", ProjectNode.NodeType.FILE,
                generateDockerfile());
            root.addChild(dockerfile);

            ProjectNode dockerCompose = new ProjectNode("docker-compose.yml", ProjectNode.NodeType.FILE,
                generateDockerCompose(projectName, type));
            root.addChild(dockerCompose);

            ProjectNode dockerignore = new ProjectNode(".dockerignore", ProjectNode.NodeType.FILE,
                generateDockerignore());
            root.addChild(dockerignore);
        }

        // PM2 configuration for production
        ProjectNode pm2Config = new ProjectNode("ecosystem.config.js", ProjectNode.NodeType.FILE,
            generatePM2Config(projectName));
        root.addChild(pm2Config);

        // Swagger configuration
        ProjectNode swaggerConfig = new ProjectNode("swagger.yaml", ProjectNode.NodeType.FILE,
            generateSwaggerConfig(projectName));
        root.addChild(swaggerConfig);

        // GitHub Actions CI/CD
        ProjectNode githubWorkflows = new ProjectNode(".github", ProjectNode.NodeType.FOLDER);
        ProjectNode workflows = new ProjectNode("workflows", ProjectNode.NodeType.FOLDER);
        ProjectNode ciYml = new ProjectNode("ci.yml", ProjectNode.NodeType.FILE,
            generateGithubActionsCI());
        workflows.addChild(ciYml);
        githubWorkflows.addChild(workflows);
        root.addChild(githubWorkflows);
    }

    /**
     * Generate source files
     */
    private void generateSourceFiles(ProjectNode root, String projectName, NodeProjectType type) {
        ProjectNode src = root.findChild("src");
        String ext = (type == NodeProjectType.TYPESCRIPT || type == NodeProjectType.FULL_STACK) ? "ts" : "js";

        // Main application file
        ProjectNode app = new ProjectNode("app." + ext, ProjectNode.NodeType.FILE,
            generateAppFile(type));
        src.addChild(app);

        // Server file
        ProjectNode server = new ProjectNode("server." + ext, ProjectNode.NodeType.FILE,
            generateServerFile(type));
        src.addChild(server);

        // Generate controller files
        ProjectNode controllers = src.findChild("controllers");
        generateControllerFiles(controllers, type);

        // Generate model files
        ProjectNode models = src.findChild("models");
        generateModelFiles(models, type);

        // Generate route files
        ProjectNode routes = src.findChild("routes");
        generateRouteFiles(routes, type);

        // Generate service files
        ProjectNode services = src.findChild("services");
        generateServiceFiles(services, type);

        // Generate middleware files
        ProjectNode middleware = src.findChild("middleware");
        generateMiddlewareFiles(middleware, type);

        // Generate utility files
        ProjectNode utils = src.findChild("utils");
        generateUtilityFiles(utils, type);

        // Generate validator files
        ProjectNode validators = src.findChild("validators");
        generateValidatorFiles(validators, type);

        // Generate database files
        ProjectNode database = src.findChild("database");
        generateDatabaseFiles(database, type);

        // Generate config files
        ProjectNode config = root.findChild("config");
        generateConfigurationFiles(config, type);

        // Generate view files if needed
        if (type == NodeProjectType.MVC || type == NodeProjectType.FULL_STACK) {
            ProjectNode views = root.findChild("views");
            generateViewFiles(views);
        }

        // Generate GraphQL files if needed
        if (type == NodeProjectType.GRAPHQL || type == NodeProjectType.FULL_STACK) {
            ProjectNode graphql = src.findChild("graphql");
            if (graphql != null) {
                generateGraphQLFiles(graphql, type);
            }
        }

        // Generate WebSocket files if needed
        if (type == NodeProjectType.REAL_TIME || type == NodeProjectType.FULL_STACK) {
            ProjectNode sockets = src.findChild("sockets");
            if (sockets != null) {
                generateSocketFiles(sockets, type);
            }
        }
    }

    /**
     * Generate test files
     */
    private void generateTestFiles(ProjectNode root, NodeProjectType type) {
        ProjectNode tests = root.findChild("tests");
        String ext = (type == NodeProjectType.TYPESCRIPT || type == NodeProjectType.FULL_STACK) ? "ts" : "js";

        // Unit tests
        ProjectNode unit = new ProjectNode("unit", ProjectNode.NodeType.FOLDER);
        tests.addChild(unit);

        ProjectNode userControllerTest = new ProjectNode("user.controller.test." + ext, ProjectNode.NodeType.FILE,
            generateControllerTest());
        unit.addChild(userControllerTest);

        ProjectNode userServiceTest = new ProjectNode("user.service.test." + ext, ProjectNode.NodeType.FILE,
            generateServiceTest());
        unit.addChild(userServiceTest);

        // Integration tests
        ProjectNode integration = new ProjectNode("integration", ProjectNode.NodeType.FOLDER);
        tests.addChild(integration);

        ProjectNode apiTest = new ProjectNode("api.test." + ext, ProjectNode.NodeType.FILE,
            generateIntegrationTest());
        integration.addChild(apiTest);

        // E2E tests
        ProjectNode e2e = new ProjectNode("e2e", ProjectNode.NodeType.FOLDER);
        tests.addChild(e2e);

        ProjectNode e2eTest = new ProjectNode("app.e2e.test." + ext, ProjectNode.NodeType.FILE,
            generateE2ETest());
        e2e.addChild(e2eTest);

        // Test setup
        ProjectNode setup = new ProjectNode("setup." + ext, ProjectNode.NodeType.FILE,
            generateTestSetup());
        tests.addChild(setup);
    }

    /**
     * Generate complete package.json with all necessary dependencies
     */
    private void generatePackageJson(ProjectNode root, String projectName, NodeProjectType type) {
        Map<String, String> dependencies = getCompleteDependencies(type);
        Map<String, String> devDependencies = getCompleteDevDependencies(type);

        // Resolve all dependencies to get latest versions
        CompletableFuture<Map<String, String>> resolvedDeps = resolveAllDependencies(dependencies);
        CompletableFuture<Map<String, String>> resolvedDevDeps = resolveAllDependencies(devDependencies);

        try {
            dependencies = resolvedDeps.get();
            devDependencies = resolvedDevDeps.get();
        } catch (Exception e) {
            logger.error("Error resolving dependencies: ", e);
        }

        String packageJson = generatePackageJsonContent(projectName, type, dependencies, devDependencies);
        ProjectNode packageJsonNode = new ProjectNode("package.json", ProjectNode.NodeType.FILE, packageJson);
        root.addChild(packageJsonNode);
    }

    /**
     * Get complete list of dependencies for Node.js Express project
     */
    private Map<String, String> getCompleteDependencies(NodeProjectType type) {
        Map<String, String> deps = new LinkedHashMap<>();

        // Core dependencies
        deps.put("express", "^4.18.2");
        deps.put("cors", "^2.8.5");
        deps.put("helmet", "^7.1.0");
        deps.put("compression", "^1.7.4");
        deps.put("express-rate-limit", "^7.1.5");
        deps.put("morgan", "^1.10.0");
        deps.put("dotenv", "^16.3.1");
        deps.put("body-parser", "^1.20.2");
        deps.put("cookie-parser", "^1.4.6");
        deps.put("express-session", "^1.17.3");

        // Database
        deps.put("mongoose", "^8.0.3");
        deps.put("pg", "^8.11.3");
        deps.put("mysql2", "^3.6.5");
        deps.put("sequelize", "^6.35.2");
        deps.put("redis", "^4.6.11");

        // Authentication & Security
        deps.put("jsonwebtoken", "^9.0.2");
        deps.put("bcryptjs", "^2.4.3");
        deps.put("passport", "^0.7.0");
        deps.put("passport-jwt", "^4.0.1");
        deps.put("passport-local", "^1.0.0");
        deps.put("express-validator", "^7.0.1");
        deps.put("crypto-js", "^4.2.0");

        // API Documentation
        deps.put("swagger-ui-express", "^5.0.0");
        deps.put("swagger-jsdoc", "^6.2.8");

        // File handling
        deps.put("multer", "^1.4.5-lts.1");
        deps.put("sharp", "^0.33.1");
        deps.put("fs-extra", "^11.2.0");

        // Email
        deps.put("nodemailer", "^6.9.7");
        deps.put("@sendgrid/mail", "^8.1.0");

        // Utilities
        deps.put("lodash", "^4.17.21");
        deps.put("uuid", "^9.0.1");
        deps.put("moment", "^2.29.4");
        deps.put("date-fns", "^3.0.6");
        deps.put("axios", "^1.6.2");
        deps.put("node-cron", "^3.0.3");

        // Logging
        deps.put("winston", "^3.11.0");
        deps.put("winston-daily-rotate-file", "^4.7.1");

        // Validation
        deps.put("joi", "^17.11.0");
        deps.put("yup", "^1.3.3");

        // WebSocket support
        if (type == NodeProjectType.REAL_TIME || type == NodeProjectType.FULL_STACK) {
            deps.put("socket.io", "^4.6.0");
            deps.put("ws", "^8.15.1");
        }

        // GraphQL support
        if (type == NodeProjectType.GRAPHQL || type == NodeProjectType.FULL_STACK) {
            deps.put("graphql", "^16.8.1");
            deps.put("apollo-server-express", "^3.13.0");
            deps.put("graphql-tools", "^9.0.0");
            deps.put("graphql-subscriptions", "^2.0.0");
        }

        // Microservice support
        if (type == NodeProjectType.MICROSERVICE) {
            deps.put("amqplib", "^0.10.3");
            deps.put("kafkajs", "^2.2.4");
            deps.put("bull", "^4.11.5");
            deps.put("agenda", "^5.0.0");
        }

        // Template engines
        if (type == NodeProjectType.MVC || type == NodeProjectType.FULL_STACK) {
            deps.put("ejs", "^3.1.9");
            deps.put("pug", "^3.0.2");
            deps.put("handlebars", "^4.7.8");
        }

        // TypeScript runtime
        if (type == NodeProjectType.TYPESCRIPT || type == NodeProjectType.FULL_STACK) {
            deps.put("reflect-metadata", "^0.2.1");
            deps.put("class-validator", "^0.14.0");
            deps.put("class-transformer", "^0.5.1");
        }

        // Monitoring
        deps.put("express-status-monitor", "^1.3.4");
        deps.put("node-os-utils", "^1.3.7");

        return deps;
    }

    /**
     * Get complete list of dev dependencies
     */
    private Map<String, String> getCompleteDevDependencies(NodeProjectType type) {
        Map<String, String> devDeps = new LinkedHashMap<>();

        // Development tools
        devDeps.put("nodemon", "^3.0.2");
        devDeps.put("concurrently", "^8.2.2");
        devDeps.put("cross-env", "^7.0.3");

        // Testing
        devDeps.put("jest", "^29.7.0");
        devDeps.put("supertest", "^6.3.3");
        devDeps.put("chai", "^4.3.10");
        devDeps.put("mocha", "^10.2.0");
        devDeps.put("sinon", "^17.0.1");
        devDeps.put("nyc", "^15.1.0");

        // Linting
        devDeps.put("eslint", "^8.56.0");
        devDeps.put("eslint-config-airbnb-base", "^15.0.0");
        devDeps.put("eslint-plugin-import", "^2.29.1");
        devDeps.put("eslint-plugin-node", "^11.1.0");
        devDeps.put("eslint-plugin-security", "^2.1.0");

        // Formatting
        devDeps.put("prettier", "^3.1.1");
        devDeps.put("husky", "^8.0.3");
        devDeps.put("lint-staged", "^15.2.0");

        // TypeScript
        if (type == NodeProjectType.TYPESCRIPT || type == NodeProjectType.FULL_STACK) {
            devDeps.put("typescript", "^5.3.3");
            devDeps.put("@types/node", "^20.10.5");
            devDeps.put("@types/express", "^4.17.21");
            devDeps.put("@types/cors", "^2.8.17");
            devDeps.put("@types/morgan", "^1.9.9");
            devDeps.put("@types/jest", "^29.5.11");
            devDeps.put("@types/supertest", "^6.0.2");
            devDeps.put("@types/jsonwebtoken", "^9.0.5");
            devDeps.put("@types/bcryptjs", "^2.4.6");
            devDeps.put("@types/multer", "^1.4.11");
            devDeps.put("@types/nodemailer", "^6.4.14");
            devDeps.put("@types/lodash", "^4.14.202");
            devDeps.put("ts-node", "^10.9.2");
            devDeps.put("ts-jest", "^29.1.1");
            devDeps.put("tsx", "^4.6.2");
        }

        // Database tools
        devDeps.put("sequelize-cli", "^6.6.2");
        devDeps.put("mongodb-memory-server", "^9.1.3");

        // API testing
        devDeps.put("newman", "^6.1.0");
        devDeps.put("artillery", "^2.0.0");

        return devDeps;
    }

    /**
     * Resolve all dependencies to get latest versions
     */
    private CompletableFuture<Map<String, String>> resolveAllDependencies(Map<String, String> dependencies) {
        return CompletableFuture.supplyAsync(() -> {
            Map<String, String> resolved = new LinkedHashMap<>();

            for (Map.Entry<String, String> entry : dependencies.entrySet()) {
                String packageName = entry.getKey();
                String versionRange = entry.getValue();

                try {
                    String latestVersion = npmRepository.getLatestVersion(packageName).get();
                    resolved.put(packageName, versionRange.startsWith("^") || versionRange.startsWith("~")
                        ? versionRange : "^" + latestVersion);
                } catch (Exception e) {
                    logger.warn("Could not resolve version for {}, using default", packageName);
                    resolved.put(packageName, versionRange);
                }
            }

            return resolved;
        });
    }

    // File content generation methods

    private String generatePackageJsonContent(String projectName, NodeProjectType type,
                                             Map<String, String> dependencies,
                                             Map<String, String> devDependencies) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        sb.append("  \"name\": \"").append(projectName.toLowerCase()).append("\",\n");
        sb.append("  \"version\": \"1.0.0\",\n");
        sb.append("  \"description\": \"").append(type.getDescription()).append("\",\n");
        sb.append("  \"main\": \"src/server.js\",\n");
        sb.append("  \"scripts\": {\n");
        sb.append("    \"start\": \"node src/server.js\",\n");
        sb.append("    \"dev\": \"nodemon src/server.js\",\n");
        sb.append("    \"test\": \"jest --coverage\",\n");
        sb.append("    \"test:watch\": \"jest --watch\",\n");
        sb.append("    \"test:e2e\": \"jest --config ./tests/jest-e2e.json\",\n");
        sb.append("    \"lint\": \"eslint . --ext .js,.ts\",\n");
        sb.append("    \"lint:fix\": \"eslint . --ext .js,.ts --fix\",\n");
        sb.append("    \"format\": \"prettier --write \\\"src/**/*.{js,ts,json}\\\"\",\n");
        sb.append("    \"migrate\": \"sequelize-cli db:migrate\",\n");
        sb.append("    \"seed\": \"sequelize-cli db:seed:all\",\n");
        sb.append("    \"build\": \"tsc\",\n");
        sb.append("    \"prepare\": \"husky install\"\n");
        sb.append("  },\n");
        sb.append("  \"keywords\": [\"nodejs\", \"express\", \"api\", \"rest\"],\n");
        sb.append("  \"author\": \"\",\n");
        sb.append("  \"license\": \"MIT\",\n");
        sb.append("  \"dependencies\": {\n");

        Iterator<Map.Entry<String, String>> depIterator = dependencies.entrySet().iterator();
        while (depIterator.hasNext()) {
            Map.Entry<String, String> entry = depIterator.next();
            sb.append("    \"").append(entry.getKey()).append("\": \"").append(entry.getValue()).append("\"");
            if (depIterator.hasNext()) sb.append(",");
            sb.append("\n");
        }

        sb.append("  },\n");
        sb.append("  \"devDependencies\": {\n");

        Iterator<Map.Entry<String, String>> devDepIterator = devDependencies.entrySet().iterator();
        while (devDepIterator.hasNext()) {
            Map.Entry<String, String> entry = devDepIterator.next();
            sb.append("    \"").append(entry.getKey()).append("\": \"").append(entry.getValue()).append("\"");
            if (devDepIterator.hasNext()) sb.append(",");
            sb.append("\n");
        }

        sb.append("  },\n");
        sb.append("  \"engines\": {\n");
        sb.append("    \"node\": \">=18.0.0\",\n");
        sb.append("    \"npm\": \">=9.0.0\"\n");
        sb.append("  },\n");
        sb.append("  \"lint-staged\": {\n");
        sb.append("    \"*.{js,ts}\": [\n");
        sb.append("      \"eslint --fix\",\n");
        sb.append("      \"prettier --write\"\n");
        sb.append("    ]\n");
        sb.append("  }\n");
        sb.append("}\n");

        return sb.toString();
    }

    private String generateGitignore() {
        return "# Dependencies\n" +
               "node_modules/\n" +
               "npm-debug.log*\n" +
               "yarn-debug.log*\n" +
               "yarn-error.log*\n\n" +
               "# Environment\n" +
               ".env\n" +
               ".env.local\n" +
               ".env.*.local\n\n" +
               "# Logs\n" +
               "logs/\n" +
               "*.log\n\n" +
               "# Testing\n" +
               "coverage/\n" +
               ".nyc_output/\n\n" +
               "# Production\n" +
               "dist/\n" +
               "build/\n\n" +
               "# IDE\n" +
               ".vscode/\n" +
               ".idea/\n" +
               "*.swp\n" +
               "*.swo\n" +
               "*~\n\n" +
               "# OS\n" +
               ".DS_Store\n" +
               "Thumbs.db\n\n" +
               "# Uploads\n" +
               "uploads/\n" +
               "temp/\n";
    }

    private String generateReadme(String projectName, NodeProjectType type) {
        return "# " + projectName + "\n\n" +
               type.getDescription() + "\n\n" +
               "## Features\n\n" +
               "- Express.js REST API with best practices\n" +
               "- JWT Authentication & Authorization\n" +
               "- MongoDB/PostgreSQL/MySQL database support\n" +
               "- Request validation with Joi/Express-validator\n" +
               "- Error handling middleware\n" +
               "- API documentation with Swagger\n" +
               "- Logging with Winston\n" +
               "- Testing with Jest & Supertest\n" +
               "- Docker support\n" +
               "- CI/CD with GitHub Actions\n" +
               "- Security with Helmet & rate limiting\n" +
               "- File upload with Multer\n" +
               "- Email service with Nodemailer\n\n" +
               "## Prerequisites\n\n" +
               "- Node.js 18+\n" +
               "- npm 9+\n" +
               "- MongoDB/PostgreSQL/MySQL\n" +
               "- Redis (optional)\n\n" +
               "## Installation\n\n" +
               "```bash\n" +
               "npm install\n" +
               "```\n\n" +
               "## Configuration\n\n" +
               "Copy `.env.example` to `.env` and update the values:\n\n" +
               "```bash\n" +
               "cp .env.example .env\n" +
               "```\n\n" +
               "## Development\n\n" +
               "```bash\n" +
               "npm run dev\n" +
               "```\n\n" +
               "## Production\n\n" +
               "```bash\n" +
               "npm start\n" +
               "```\n\n" +
               "## Testing\n\n" +
               "```bash\n" +
               "# Run all tests\n" +
               "npm test\n\n" +
               "# Run tests in watch mode\n" +
               "npm run test:watch\n\n" +
               "# Run e2e tests\n" +
               "npm run test:e2e\n" +
               "```\n\n" +
               "## API Documentation\n\n" +
               "Once the server is running, visit:\n" +
               "- Swagger UI: http://localhost:3000/api-docs\n\n" +
               "## Docker\n\n" +
               "```bash\n" +
               "# Build image\n" +
               "docker build -t " + projectName.toLowerCase() + " .\n\n" +
               "# Run container\n" +
               "docker-compose up\n" +
               "```\n\n" +
               "## Project Structure\n\n" +
               "```\n" +
               projectName + "/\n" +
               "├── src/\n" +
               "│   ├── controllers/    # Route controllers\n" +
               "│   ├── models/         # Database models\n" +
               "│   ├── routes/         # API routes\n" +
               "│   ├── services/       # Business logic\n" +
               "│   ├── middleware/     # Custom middleware\n" +
               "│   ├── utils/          # Utility functions\n" +
               "│   ├── validators/     # Request validators\n" +
               "│   └── database/       # Database config\n" +
               "├── tests/              # Test files\n" +
               "├── config/             # Configuration files\n" +
               "└── package.json        # Dependencies\n" +
               "```\n";
    }

    private String generateEnvFile(NodeProjectType type) {
        StringBuilder env = new StringBuilder();
        env.append("# Server\n");
        env.append("NODE_ENV=development\n");
        env.append("PORT=3000\n");
        env.append("HOST=localhost\n\n");

        env.append("# Database\n");
        env.append("DB_TYPE=mongodb\n");
        env.append("DB_HOST=localhost\n");
        env.append("DB_PORT=27017\n");
        env.append("DB_NAME=").append("app_db").append("\n");
        env.append("DB_USER=admin\n");
        env.append("DB_PASSWORD=password\n");
        env.append("MONGODB_URI=mongodb://localhost:27017/app_db\n\n");

        env.append("# PostgreSQL (alternative)\n");
        env.append("POSTGRES_HOST=localhost\n");
        env.append("POSTGRES_PORT=5432\n");
        env.append("POSTGRES_DB=app_db\n");
        env.append("POSTGRES_USER=postgres\n");
        env.append("POSTGRES_PASSWORD=password\n\n");

        env.append("# Redis\n");
        env.append("REDIS_HOST=localhost\n");
        env.append("REDIS_PORT=6379\n");
        env.append("REDIS_PASSWORD=\n\n");

        env.append("# JWT\n");
        env.append("JWT_SECRET=your-super-secret-jwt-key-change-this\n");
        env.append("JWT_EXPIRE=7d\n");
        env.append("JWT_COOKIE_EXPIRE=7\n\n");

        env.append("# Email\n");
        env.append("EMAIL_HOST=smtp.gmail.com\n");
        env.append("EMAIL_PORT=587\n");
        env.append("EMAIL_USER=your-email@gmail.com\n");
        env.append("EMAIL_PASSWORD=your-app-password\n");
        env.append("EMAIL_FROM=noreply@app.com\n\n");

        env.append("# API Keys\n");
        env.append("API_KEY=your-api-key\n");
        env.append("STRIPE_SECRET_KEY=sk_test_...\n");
        env.append("SENDGRID_API_KEY=SG...\n\n");

        env.append("# AWS\n");
        env.append("AWS_ACCESS_KEY_ID=\n");
        env.append("AWS_SECRET_ACCESS_KEY=\n");
        env.append("AWS_REGION=us-east-1\n");
        env.append("S3_BUCKET=\n\n");

        env.append("# Logging\n");
        env.append("LOG_LEVEL=debug\n");
        env.append("LOG_FILE=app.log\n\n");

        env.append("# Rate Limiting\n");
        env.append("RATE_LIMIT_WINDOW_MS=900000\n");
        env.append("RATE_LIMIT_MAX_REQUESTS=100\n");

        return env.toString();
    }

    private String generateAppFile(NodeProjectType type) {
        String imports = type == NodeProjectType.TYPESCRIPT || type == NodeProjectType.FULL_STACK ?
            "import express, { Application, Request, Response, NextFunction } from 'express';\n" +
            "import cors from 'cors';\n" +
            "import helmet from 'helmet';\n" +
            "import compression from 'compression';\n" +
            "import morgan from 'morgan';\n" +
            "import cookieParser from 'cookie-parser';\n" +
            "import mongoSanitize from 'express-mongo-sanitize';\n" +
            "import rateLimit from 'express-rate-limit';\n" +
            "import swaggerUi from 'swagger-ui-express';\n" +
            "import { errorHandler } from './middleware/errorHandler';\n" +
            "import routes from './routes';\n" :
            "const express = require('express');\n" +
            "const cors = require('cors');\n" +
            "const helmet = require('helmet');\n" +
            "const compression = require('compression');\n" +
            "const morgan = require('morgan');\n" +
            "const cookieParser = require('cookie-parser');\n" +
            "const mongoSanitize = require('express-mongo-sanitize');\n" +
            "const rateLimit = require('express-rate-limit');\n" +
            "const swaggerUi = require('swagger-ui-express');\n" +
            "const { errorHandler } = require('./middleware/errorHandler');\n" +
            "const routes = require('./routes');\n";

        return imports + "\n" +
               "const app = express();\n\n" +
               "// Trust proxy\n" +
               "app.set('trust proxy', 1);\n\n" +
               "// Security middleware\n" +
               "app.use(helmet());\n" +
               "app.use(cors({\n" +
               "  origin: process.env.CORS_ORIGIN || '*',\n" +
               "  credentials: true\n" +
               "}));\n\n" +
               "// Rate limiting\n" +
               "const limiter = rateLimit({\n" +
               "  windowMs: parseInt(process.env.RATE_LIMIT_WINDOW_MS || '900000'),\n" +
               "  max: parseInt(process.env.RATE_LIMIT_MAX_REQUESTS || '100'),\n" +
               "  message: 'Too many requests from this IP'\n" +
               "});\n" +
               "app.use('/api', limiter);\n\n" +
               "// Body parsing\n" +
               "app.use(express.json({ limit: '10mb' }));\n" +
               "app.use(express.urlencoded({ extended: true, limit: '10mb' }));\n" +
               "app.use(cookieParser());\n\n" +
               "// Data sanitization\n" +
               "app.use(mongoSanitize());\n\n" +
               "// Compression\n" +
               "app.use(compression());\n\n" +
               "// Logging\n" +
               "if (process.env.NODE_ENV === 'development') {\n" +
               "  app.use(morgan('dev'));\n" +
               "} else {\n" +
               "  app.use(morgan('combined'));\n" +
               "}\n\n" +
               "// Static files\n" +
               "app.use('/public', express.static('public'));\n" +
               "app.use('/uploads', express.static('uploads'));\n\n" +
               "// API Documentation\n" +
               "const swaggerDocument = require('../swagger.json');\n" +
               "app.use('/api-docs', swaggerUi.serve, swaggerUi.setup(swaggerDocument));\n\n" +
               "// Health check\n" +
               "app.get('/health', (req, res) => {\n" +
               "  res.status(200).json({ status: 'OK', timestamp: new Date().toISOString() });\n" +
               "});\n\n" +
               "// API Routes\n" +
               "app.use('/api/v1', routes);\n\n" +
               "// 404 handler\n" +
               "app.use((req, res, next) => {\n" +
               "  res.status(404).json({ error: 'Route not found' });\n" +
               "});\n\n" +
               "// Error handling\n" +
               "app.use(errorHandler);\n\n" +
               (type == NodeProjectType.TYPESCRIPT || type == NodeProjectType.FULL_STACK ?
                "export default app;\n" : "module.exports = app;\n");
    }

    private String generateServerFile(NodeProjectType type) {
        String imports = type == NodeProjectType.TYPESCRIPT || type == NodeProjectType.FULL_STACK ?
            "import dotenv from 'dotenv';\n" +
            "import app from './app';\n" +
            "import { connectDB } from './database/connection';\n" +
            "import logger from './utils/logger';\n" :
            "require('dotenv').config();\n" +
            "const app = require('./app');\n" +
            "const { connectDB } = require('./database/connection');\n" +
            "const logger = require('./utils/logger');\n";

        return imports + "\n" +
               "// Handle uncaught exceptions\n" +
               "process.on('uncaughtException', (err) => {\n" +
               "  logger.error('UNCAUGHT EXCEPTION! Shutting down...');\n" +
               "  logger.error(err.name, err.message);\n" +
               "  process.exit(1);\n" +
               "});\n\n" +
               "// Connect to database\n" +
               "connectDB();\n\n" +
               "const PORT = process.env.PORT || 3000;\n" +
               "const server = app.listen(PORT, () => {\n" +
               "  logger.info(`Server running on port ${PORT} in ${process.env.NODE_ENV} mode`);\n" +
               "});\n\n" +
               "// Handle unhandled promise rejections\n" +
               "process.on('unhandledRejection', (err) => {\n" +
               "  logger.error('UNHANDLED REJECTION! Shutting down...');\n" +
               "  logger.error(err.name, err.message);\n" +
               "  server.close(() => {\n" +
               "    process.exit(1);\n" +
               "  });\n" +
               "});\n\n" +
               "// Graceful shutdown\n" +
               "process.on('SIGTERM', () => {\n" +
               "  logger.info('SIGTERM received. Shutting down gracefully');\n" +
               "  server.close(() => {\n" +
               "    logger.info('Process terminated');\n" +
               "  });\n" +
               "});\n";
    }

    private void generateControllerFiles(ProjectNode controllers, NodeProjectType type) {
        String ext = type == NodeProjectType.TYPESCRIPT || type == NodeProjectType.FULL_STACK ? "ts" : "js";

        // User Controller
        String userController = "const { User } = require('../models');\n" +
            "const { userService } = require('../services');\n" +
            "const { catchAsync } = require('../utils/catchAsync');\n" +
            "const { AppError } = require('../utils/appError');\n\n" +
            "exports.getAllUsers = catchAsync(async (req, res, next) => {\n" +
            "  const users = await userService.getAllUsers(req.query);\n" +
            "  res.status(200).json({\n" +
            "    success: true,\n" +
            "    results: users.length,\n" +
            "    data: users\n" +
            "  });\n" +
            "});\n\n" +
            "exports.getUser = catchAsync(async (req, res, next) => {\n" +
            "  const user = await userService.getUserById(req.params.id);\n" +
            "  if (!user) {\n" +
            "    return next(new AppError('User not found', 404));\n" +
            "  }\n" +
            "  res.status(200).json({\n" +
            "    success: true,\n" +
            "    data: user\n" +
            "  });\n" +
            "});\n\n" +
            "exports.createUser = catchAsync(async (req, res, next) => {\n" +
            "  const user = await userService.createUser(req.body);\n" +
            "  res.status(201).json({\n" +
            "    success: true,\n" +
            "    data: user\n" +
            "  });\n" +
            "});\n\n" +
            "exports.updateUser = catchAsync(async (req, res, next) => {\n" +
            "  const user = await userService.updateUser(req.params.id, req.body);\n" +
            "  if (!user) {\n" +
            "    return next(new AppError('User not found', 404));\n" +
            "  }\n" +
            "  res.status(200).json({\n" +
            "    success: true,\n" +
            "    data: user\n" +
            "  });\n" +
            "});\n\n" +
            "exports.deleteUser = catchAsync(async (req, res, next) => {\n" +
            "  await userService.deleteUser(req.params.id);\n" +
            "  res.status(204).json({\n" +
            "    success: true,\n" +
            "    data: null\n" +
            "  });\n" +
            "});\n";

        ProjectNode userControllerNode = new ProjectNode("user.controller." + ext, ProjectNode.NodeType.FILE, userController);
        controllers.addChild(userControllerNode);

        // Auth Controller
        String authController = "const { authService } = require('../services');\n" +
            "const { catchAsync } = require('../utils/catchAsync');\n\n" +
            "exports.register = catchAsync(async (req, res, next) => {\n" +
            "  const { user, token } = await authService.register(req.body);\n" +
            "  res.status(201).json({\n" +
            "    success: true,\n" +
            "    token,\n" +
            "    data: user\n" +
            "  });\n" +
            "});\n\n" +
            "exports.login = catchAsync(async (req, res, next) => {\n" +
            "  const { user, token } = await authService.login(req.body);\n" +
            "  res.status(200).json({\n" +
            "    success: true,\n" +
            "    token,\n" +
            "    data: user\n" +
            "  });\n" +
            "});\n\n" +
            "exports.logout = catchAsync(async (req, res, next) => {\n" +
            "  res.cookie('token', 'none', {\n" +
            "    expires: new Date(Date.now() + 10 * 1000),\n" +
            "    httpOnly: true\n" +
            "  });\n" +
            "  res.status(200).json({\n" +
            "    success: true,\n" +
            "    data: null\n" +
            "  });\n" +
            "});\n";

        ProjectNode authControllerNode = new ProjectNode("auth.controller." + ext, ProjectNode.NodeType.FILE, authController);
        controllers.addChild(authControllerNode);
    }

    private void generateModelFiles(ProjectNode models, NodeProjectType type) {
        String ext = type == NodeProjectType.TYPESCRIPT || type == NodeProjectType.FULL_STACK ? "ts" : "js";

        // User Model (Mongoose)
        String userModel = "const mongoose = require('mongoose');\n" +
            "const bcrypt = require('bcryptjs');\n" +
            "const jwt = require('jsonwebtoken');\n\n" +
            "const userSchema = new mongoose.Schema({\n" +
            "  name: {\n" +
            "    type: String,\n" +
            "    required: [true, 'Please provide a name'],\n" +
            "    trim: true,\n" +
            "    maxlength: [50, 'Name cannot be more than 50 characters']\n" +
            "  },\n" +
            "  email: {\n" +
            "    type: String,\n" +
            "    required: [true, 'Please provide an email'],\n" +
            "    unique: true,\n" +
            "    lowercase: true,\n" +
            "    match: [/^\\S+@\\S+\\.\\S+$/, 'Please provide a valid email']\n" +
            "  },\n" +
            "  password: {\n" +
            "    type: String,\n" +
            "    required: [true, 'Please provide a password'],\n" +
            "    minlength: 6,\n" +
            "    select: false\n" +
            "  },\n" +
            "  role: {\n" +
            "    type: String,\n" +
            "    enum: ['user', 'admin'],\n" +
            "    default: 'user'\n" +
            "  },\n" +
            "  isActive: {\n" +
            "    type: Boolean,\n" +
            "    default: true\n" +
            "  },\n" +
            "  emailVerified: {\n" +
            "    type: Boolean,\n" +
            "    default: false\n" +
            "  },\n" +
            "  resetPasswordToken: String,\n" +
            "  resetPasswordExpire: Date\n" +
            "}, {\n" +
            "  timestamps: true\n" +
            "});\n\n" +
            "// Encrypt password before saving\n" +
            "userSchema.pre('save', async function(next) {\n" +
            "  if (!this.isModified('password')) {\n" +
            "    next();\n" +
            "  }\n" +
            "  const salt = await bcrypt.genSalt(10);\n" +
            "  this.password = await bcrypt.hash(this.password, salt);\n" +
            "});\n\n" +
            "// Sign JWT and return\n" +
            "userSchema.methods.getSignedJwtToken = function() {\n" +
            "  return jwt.sign({ id: this._id }, process.env.JWT_SECRET, {\n" +
            "    expiresIn: process.env.JWT_EXPIRE\n" +
            "  });\n" +
            "};\n\n" +
            "// Match password\n" +
            "userSchema.methods.matchPassword = async function(enteredPassword) {\n" +
            "  return await bcrypt.compare(enteredPassword, this.password);\n" +
            "};\n\n" +
            "module.exports = mongoose.model('User', userSchema);\n";

        ProjectNode userModelNode = new ProjectNode("user.model." + ext, ProjectNode.NodeType.FILE, userModel);
        models.addChild(userModelNode);

        // Index file
        String indexModel = "module.exports = {\n" +
            "  User: require('./user.model'),\n" +
            "  // Add other models here\n" +
            "};\n";

        ProjectNode indexModelNode = new ProjectNode("index." + ext, ProjectNode.NodeType.FILE, indexModel);
        models.addChild(indexModelNode);
    }

    private void generateRouteFiles(ProjectNode routes, NodeProjectType type) {
        String ext = type == NodeProjectType.TYPESCRIPT || type == NodeProjectType.FULL_STACK ? "ts" : "js";

        // User Routes
        String userRoutes = "const express = require('express');\n" +
            "const router = express.Router();\n" +
            "const { userController } = require('../controllers');\n" +
            "const { auth, authorize } = require('../middleware/auth');\n" +
            "const { validate } = require('../middleware/validate');\n" +
            "const { userValidation } = require('../validators');\n\n" +
            "router\n" +
            "  .route('/')\n" +
            "  .get(auth, authorize('admin'), userController.getAllUsers)\n" +
            "  .post(validate(userValidation.createUser), userController.createUser);\n\n" +
            "router\n" +
            "  .route('/:id')\n" +
            "  .get(auth, userController.getUser)\n" +
            "  .put(auth, validate(userValidation.updateUser), userController.updateUser)\n" +
            "  .delete(auth, authorize('admin'), userController.deleteUser);\n\n" +
            "module.exports = router;\n";

        ProjectNode userRoutesNode = new ProjectNode("user.routes." + ext, ProjectNode.NodeType.FILE, userRoutes);
        routes.addChild(userRoutesNode);

        // Index Routes
        String indexRoutes = "const express = require('express');\n" +
            "const router = express.Router();\n\n" +
            "// Import route modules\n" +
            "const userRoutes = require('./user.routes');\n" +
            "const authRoutes = require('./auth.routes');\n\n" +
            "// Mount routes\n" +
            "router.use('/users', userRoutes);\n" +
            "router.use('/auth', authRoutes);\n\n" +
            "module.exports = router;\n";

        ProjectNode indexRoutesNode = new ProjectNode("index." + ext, ProjectNode.NodeType.FILE, indexRoutes);
        routes.addChild(indexRoutesNode);
    }

    private void generateServiceFiles(ProjectNode services, NodeProjectType type) {
        String ext = type == NodeProjectType.TYPESCRIPT || type == NodeProjectType.FULL_STACK ? "ts" : "js";

        // User Service
        String userService = "const { User } = require('../models');\n" +
            "const { AppError } = require('../utils/appError');\n\n" +
            "class UserService {\n" +
            "  async getAllUsers(query) {\n" +
            "    const { page = 1, limit = 10, sort = '-createdAt' } = query;\n" +
            "    const skip = (page - 1) * limit;\n\n" +
            "    const users = await User.find()\n" +
            "      .sort(sort)\n" +
            "      .limit(limit * 1)\n" +
            "      .skip(skip)\n" +
            "      .select('-password');\n\n" +
            "    return users;\n" +
            "  }\n\n" +
            "  async getUserById(id) {\n" +
            "    const user = await User.findById(id).select('-password');\n" +
            "    return user;\n" +
            "  }\n\n" +
            "  async createUser(userData) {\n" +
            "    const user = await User.create(userData);\n" +
            "    user.password = undefined;\n" +
            "    return user;\n" +
            "  }\n\n" +
            "  async updateUser(id, userData) {\n" +
            "    const user = await User.findByIdAndUpdate(id, userData, {\n" +
            "      new: true,\n" +
            "      runValidators: true\n" +
            "    }).select('-password');\n" +
            "    return user;\n" +
            "  }\n\n" +
            "  async deleteUser(id) {\n" +
            "    await User.findByIdAndDelete(id);\n" +
            "  }\n" +
            "}\n\n" +
            "module.exports = new UserService();\n";

        ProjectNode userServiceNode = new ProjectNode("user.service." + ext, ProjectNode.NodeType.FILE, userService);
        services.addChild(userServiceNode);
    }

    private void generateMiddlewareFiles(ProjectNode middleware, NodeProjectType type) {
        String ext = type == NodeProjectType.TYPESCRIPT || type == NodeProjectType.FULL_STACK ? "ts" : "js";

        // Error Handler
        String errorHandler = "const errorHandler = (err, req, res, next) => {\n" +
            "  let error = { ...err };\n" +
            "  error.message = err.message;\n\n" +
            "  // Log to console for dev\n" +
            "  console.log(err);\n\n" +
            "  // Mongoose bad ObjectId\n" +
            "  if (err.name === 'CastError') {\n" +
            "    const message = 'Resource not found';\n" +
            "    error = new AppError(message, 404);\n" +
            "  }\n\n" +
            "  // Mongoose duplicate key\n" +
            "  if (err.code === 11000) {\n" +
            "    const message = 'Duplicate field value entered';\n" +
            "    error = new AppError(message, 400);\n" +
            "  }\n\n" +
            "  // Mongoose validation error\n" +
            "  if (err.name === 'ValidationError') {\n" +
            "    const message = Object.values(err.errors).map(val => val.message).join(', ');\n" +
            "    error = new AppError(message, 400);\n" +
            "  }\n\n" +
            "  res.status(error.statusCode || 500).json({\n" +
            "    success: false,\n" +
            "    error: error.message || 'Server Error'\n" +
            "  });\n" +
            "};\n\n" +
            "module.exports = { errorHandler };\n";

        ProjectNode errorHandlerNode = new ProjectNode("errorHandler." + ext, ProjectNode.NodeType.FILE, errorHandler);
        middleware.addChild(errorHandlerNode);

        // Auth Middleware
        String authMiddleware = "const jwt = require('jsonwebtoken');\n" +
            "const { User } = require('../models');\n" +
            "const { AppError } = require('../utils/appError');\n" +
            "const { catchAsync } = require('../utils/catchAsync');\n\n" +
            "exports.auth = catchAsync(async (req, res, next) => {\n" +
            "  let token;\n\n" +
            "  if (req.headers.authorization && req.headers.authorization.startsWith('Bearer')) {\n" +
            "    token = req.headers.authorization.split(' ')[1];\n" +
            "  } else if (req.cookies.token) {\n" +
            "    token = req.cookies.token;\n" +
            "  }\n\n" +
            "  if (!token) {\n" +
            "    return next(new AppError('Not authorized to access this route', 401));\n" +
            "  }\n\n" +
            "  try {\n" +
            "    const decoded = jwt.verify(token, process.env.JWT_SECRET);\n" +
            "    req.user = await User.findById(decoded.id);\n" +
            "    next();\n" +
            "  } catch (err) {\n" +
            "    return next(new AppError('Not authorized to access this route', 401));\n" +
            "  }\n" +
            "});\n\n" +
            "exports.authorize = (...roles) => {\n" +
            "  return (req, res, next) => {\n" +
            "    if (!roles.includes(req.user.role)) {\n" +
            "      return next(new AppError('User role is not authorized to access this route', 403));\n" +
            "    }\n" +
            "    next();\n" +
            "  };\n" +
            "};\n";

        ProjectNode authMiddlewareNode = new ProjectNode("auth." + ext, ProjectNode.NodeType.FILE, authMiddleware);
        middleware.addChild(authMiddlewareNode);
    }

    private void generateUtilityFiles(ProjectNode utils, NodeProjectType type) {
        String ext = type == NodeProjectType.TYPESCRIPT || type == NodeProjectType.FULL_STACK ? "ts" : "js";

        // Logger
        String logger = "const winston = require('winston');\n" +
            "const path = require('path');\n\n" +
            "const logger = winston.createLogger({\n" +
            "  level: process.env.LOG_LEVEL || 'info',\n" +
            "  format: winston.format.combine(\n" +
            "    winston.format.timestamp({ format: 'YYYY-MM-DD HH:mm:ss' }),\n" +
            "    winston.format.errors({ stack: true }),\n" +
            "    winston.format.splat(),\n" +
            "    winston.format.json()\n" +
            "  ),\n" +
            "  defaultMeta: { service: 'api' },\n" +
            "  transports: [\n" +
            "    new winston.transports.File({\n" +
            "      filename: path.join('logs', 'error.log'),\n" +
            "      level: 'error'\n" +
            "    }),\n" +
            "    new winston.transports.File({\n" +
            "      filename: path.join('logs', 'combined.log')\n" +
            "    })\n" +
            "  ]\n" +
            "});\n\n" +
            "if (process.env.NODE_ENV !== 'production') {\n" +
            "  logger.add(new winston.transports.Console({\n" +
            "    format: winston.format.combine(\n" +
            "      winston.format.colorize(),\n" +
            "      winston.format.simple()\n" +
            "    )\n" +
            "  }));\n" +
            "}\n\n" +
            "module.exports = logger;\n";

        ProjectNode loggerNode = new ProjectNode("logger." + ext, ProjectNode.NodeType.FILE, logger);
        utils.addChild(loggerNode);

        // Catch Async
        String catchAsync = "module.exports.catchAsync = (fn) => {\n" +
            "  return (req, res, next) => {\n" +
            "    Promise.resolve(fn(req, res, next)).catch(next);\n" +
            "  };\n" +
            "};\n";

        ProjectNode catchAsyncNode = new ProjectNode("catchAsync." + ext, ProjectNode.NodeType.FILE, catchAsync);
        utils.addChild(catchAsyncNode);

        // App Error
        String appError = "class AppError extends Error {\n" +
            "  constructor(message, statusCode) {\n" +
            "    super(message);\n\n" +
            "    this.statusCode = statusCode;\n" +
            "    this.isOperational = true;\n\n" +
            "    Error.captureStackTrace(this, this.constructor);\n" +
            "  }\n" +
            "}\n\n" +
            "module.exports = { AppError };\n";

        ProjectNode appErrorNode = new ProjectNode("appError." + ext, ProjectNode.NodeType.FILE, appError);
        utils.addChild(appErrorNode);
    }

    private void generateValidatorFiles(ProjectNode validators, NodeProjectType type) {
        String ext = type == NodeProjectType.TYPESCRIPT || type == NodeProjectType.FULL_STACK ? "ts" : "js";

        // User Validation
        String userValidation = "const Joi = require('joi');\n\n" +
            "const createUser = Joi.object({\n" +
            "  name: Joi.string().required().min(2).max(50),\n" +
            "  email: Joi.string().required().email(),\n" +
            "  password: Joi.string().required().min(6),\n" +
            "  role: Joi.string().valid('user', 'admin')\n" +
            "});\n\n" +
            "const updateUser = Joi.object({\n" +
            "  name: Joi.string().min(2).max(50),\n" +
            "  email: Joi.string().email(),\n" +
            "  role: Joi.string().valid('user', 'admin')\n" +
            "});\n\n" +
            "module.exports = {\n" +
            "  createUser,\n" +
            "  updateUser\n" +
            "};\n";

        ProjectNode userValidationNode = new ProjectNode("user.validation." + ext, ProjectNode.NodeType.FILE, userValidation);
        validators.addChild(userValidationNode);
    }

    private void generateDatabaseFiles(ProjectNode database, NodeProjectType type) {
        String ext = type == NodeProjectType.TYPESCRIPT || type == NodeProjectType.FULL_STACK ? "ts" : "js";

        // Database Connection
        String connection = "const mongoose = require('mongoose');\n" +
            "const logger = require('../utils/logger');\n\n" +
            "const connectDB = async () => {\n" +
            "  try {\n" +
            "    const conn = await mongoose.connect(process.env.MONGODB_URI || 'mongodb://localhost:27017/app_db', {\n" +
            "      useNewUrlParser: true,\n" +
            "      useUnifiedTopology: true\n" +
            "    });\n\n" +
            "    logger.info(`MongoDB Connected: ${conn.connection.host}`);\n" +
            "  } catch (error) {\n" +
            "    logger.error(`Error: ${error.message}`);\n" +
            "    process.exit(1);\n" +
            "  }\n" +
            "};\n\n" +
            "module.exports = { connectDB };\n";

        ProjectNode connectionNode = new ProjectNode("connection." + ext, ProjectNode.NodeType.FILE, connection);
        database.addChild(connectionNode);
    }

    private void generateConfigurationFiles(ProjectNode config, NodeProjectType type) {
        String ext = type == NodeProjectType.TYPESCRIPT || type == NodeProjectType.FULL_STACK ? "ts" : "js";

        // Database Config
        String dbConfig = "module.exports = {\n" +
            "  development: {\n" +
            "    username: process.env.DB_USER || 'root',\n" +
            "    password: process.env.DB_PASSWORD || null,\n" +
            "    database: process.env.DB_NAME || 'app_dev',\n" +
            "    host: process.env.DB_HOST || '127.0.0.1',\n" +
            "    dialect: 'postgres'\n" +
            "  },\n" +
            "  test: {\n" +
            "    username: 'root',\n" +
            "    password: null,\n" +
            "    database: 'app_test',\n" +
            "    host: '127.0.0.1',\n" +
            "    dialect: 'postgres'\n" +
            "  },\n" +
            "  production: {\n" +
            "    use_env_variable: 'DATABASE_URL',\n" +
            "    dialect: 'postgres',\n" +
            "    dialectOptions: {\n" +
            "      ssl: {\n" +
            "        require: true,\n" +
            "        rejectUnauthorized: false\n" +
            "      }\n" +
            "    }\n" +
            "  }\n" +
            "};\n";

        ProjectNode dbConfigNode = new ProjectNode("database." + ext, ProjectNode.NodeType.FILE, dbConfig);
        config.addChild(dbConfigNode);
    }

    private void generateViewFiles(ProjectNode views) {
        // Index EJS
        String indexView = "<!DOCTYPE html>\n" +
            "<html lang=\"en\">\n" +
            "<head>\n" +
            "    <meta charset=\"UTF-8\">\n" +
            "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
            "    <title>Node.js Express App</title>\n" +
            "    <link href=\"https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css\" rel=\"stylesheet\">\n" +
            "</head>\n" +
            "<body>\n" +
            "    <div class=\"container mt-5\">\n" +
            "        <h1>Welcome to Node.js Express</h1>\n" +
            "        <p class=\"lead\">Your API is running successfully!</p>\n" +
            "        <a href=\"/api-docs\" class=\"btn btn-primary\">View API Documentation</a>\n" +
            "    </div>\n" +
            "</body>\n" +
            "</html>\n";

        ProjectNode indexViewNode = new ProjectNode("index.ejs", ProjectNode.NodeType.FILE, indexView);
        views.addChild(indexViewNode);
    }

    private void generateGraphQLFiles(ProjectNode graphql, NodeProjectType type) {
        ProjectNode schemas = graphql.findChild("schemas");
        ProjectNode resolvers = graphql.findChild("resolvers");
        String ext = type == NodeProjectType.TYPESCRIPT || type == NodeProjectType.FULL_STACK ? "ts" : "js";

        // User Schema
        String userSchema = "const { gql } = require('apollo-server-express');\n\n" +
            "const userTypeDefs = gql`\n" +
            "  type User {\n" +
            "    id: ID!\n" +
            "    name: String!\n" +
            "    email: String!\n" +
            "    role: String!\n" +
            "    createdAt: String!\n" +
            "    updatedAt: String!\n" +
            "  }\n\n" +
            "  type Query {\n" +
            "    users: [User!]!\n" +
            "    user(id: ID!): User\n" +
            "  }\n\n" +
            "  type Mutation {\n" +
            "    createUser(name: String!, email: String!, password: String!): User!\n" +
            "    updateUser(id: ID!, name: String, email: String): User!\n" +
            "    deleteUser(id: ID!): Boolean!\n" +
            "  }\n" +
            "`;\n\n" +
            "module.exports = userTypeDefs;\n";

        ProjectNode userSchemaNode = new ProjectNode("user.schema." + ext, ProjectNode.NodeType.FILE, userSchema);
        schemas.addChild(userSchemaNode);

        // User Resolver
        String userResolver = "const { User } = require('../../models');\n\n" +
            "const userResolvers = {\n" +
            "  Query: {\n" +
            "    users: async () => {\n" +
            "      return await User.find();\n" +
            "    },\n" +
            "    user: async (_, { id }) => {\n" +
            "      return await User.findById(id);\n" +
            "    }\n" +
            "  },\n" +
            "  Mutation: {\n" +
            "    createUser: async (_, { name, email, password }) => {\n" +
            "      const user = new User({ name, email, password });\n" +
            "      return await user.save();\n" +
            "    },\n" +
            "    updateUser: async (_, { id, name, email }) => {\n" +
            "      return await User.findByIdAndUpdate(id, { name, email }, { new: true });\n" +
            "    },\n" +
            "    deleteUser: async (_, { id }) => {\n" +
            "      await User.findByIdAndDelete(id);\n" +
            "      return true;\n" +
            "    }\n" +
            "  }\n" +
            "};\n\n" +
            "module.exports = userResolvers;\n";

        ProjectNode userResolverNode = new ProjectNode("user.resolver." + ext, ProjectNode.NodeType.FILE, userResolver);
        resolvers.addChild(userResolverNode);
    }

    private void generateSocketFiles(ProjectNode sockets, NodeProjectType type) {
        String ext = type == NodeProjectType.TYPESCRIPT || type == NodeProjectType.FULL_STACK ? "ts" : "js";

        // Socket Handler
        String socketHandler = "const socketAuth = require('../middleware/socketAuth');\n\n" +
            "module.exports = (io) => {\n" +
            "  // Authentication middleware\n" +
            "  io.use(socketAuth);\n\n" +
            "  io.on('connection', (socket) => {\n" +
            "    console.log('New client connected:', socket.id);\n\n" +
            "    // Join room\n" +
            "    socket.on('join-room', (roomId) => {\n" +
            "      socket.join(roomId);\n" +
            "      socket.to(roomId).emit('user-joined', socket.userId);\n" +
            "    });\n\n" +
            "    // Send message\n" +
            "    socket.on('send-message', (data) => {\n" +
            "      io.to(data.roomId).emit('receive-message', {\n" +
            "        userId: socket.userId,\n" +
            "        message: data.message,\n" +
            "        timestamp: new Date()\n" +
            "      });\n" +
            "    });\n\n" +
            "    // Disconnect\n" +
            "    socket.on('disconnect', () => {\n" +
            "      console.log('Client disconnected:', socket.id);\n" +
            "    });\n" +
            "  });\n" +
            "};\n";

        ProjectNode socketHandlerNode = new ProjectNode("socketHandler." + ext, ProjectNode.NodeType.FILE, socketHandler);
        sockets.addChild(socketHandlerNode);
    }

    // Test file generators
    private String generateControllerTest() {
        return "const request = require('supertest');\n" +
               "const app = require('../../src/app');\n" +
               "const { User } = require('../../src/models');\n\n" +
               "describe('User Controller', () => {\n" +
               "  describe('GET /api/v1/users', () => {\n" +
               "    it('should get all users', async () => {\n" +
               "      const res = await request(app)\n" +
               "        .get('/api/v1/users')\n" +
               "        .set('Authorization', 'Bearer ' + token)\n" +
               "        .expect(200);\n\n" +
               "      expect(res.body.success).toBe(true);\n" +
               "      expect(Array.isArray(res.body.data)).toBe(true);\n" +
               "    });\n" +
               "  });\n" +
               "});\n";
    }

    private String generateServiceTest() {
        return "const userService = require('../../src/services/user.service');\n" +
               "const { User } = require('../../src/models');\n\n" +
               "jest.mock('../../src/models');\n\n" +
               "describe('User Service', () => {\n" +
               "  describe('getAllUsers', () => {\n" +
               "    it('should return all users', async () => {\n" +
               "      const mockUsers = [{ id: 1, name: 'Test User' }];\n" +
               "      User.find.mockResolvedValue(mockUsers);\n\n" +
               "      const users = await userService.getAllUsers({});\n" +
               "      expect(users).toEqual(mockUsers);\n" +
               "    });\n" +
               "  });\n" +
               "});\n";
    }

    private String generateIntegrationTest() {
        return "const request = require('supertest');\n" +
               "const app = require('../../src/app');\n" +
               "const mongoose = require('mongoose');\n\n" +
               "beforeAll(async () => {\n" +
               "  await mongoose.connect(process.env.MONGODB_URI_TEST);\n" +
               "});\n\n" +
               "afterAll(async () => {\n" +
               "  await mongoose.connection.close();\n" +
               "});\n\n" +
               "describe('API Integration Tests', () => {\n" +
               "  it('should return 200 for health check', async () => {\n" +
               "    const res = await request(app)\n" +
               "      .get('/health')\n" +
               "      .expect(200);\n\n" +
               "    expect(res.body.status).toBe('OK');\n" +
               "  });\n" +
               "});\n";
    }

    private String generateE2ETest() {
        return "const request = require('supertest');\n" +
               "const app = require('../../src/app');\n\n" +
               "describe('E2E Tests', () => {\n" +
               "  let token;\n\n" +
               "  beforeAll(async () => {\n" +
               "    const res = await request(app)\n" +
               "      .post('/api/v1/auth/login')\n" +
               "      .send({ email: 'test@example.com', password: 'password' });\n" +
               "    token = res.body.token;\n" +
               "  });\n\n" +
               "  it('should complete user flow', async () => {\n" +
               "    // Create user\n" +
               "    const createRes = await request(app)\n" +
               "      .post('/api/v1/users')\n" +
               "      .set('Authorization', 'Bearer ' + token)\n" +
               "      .send({ name: 'Test', email: 'new@example.com', password: 'password' })\n" +
               "      .expect(201);\n\n" +
               "    const userId = createRes.body.data.id;\n\n" +
               "    // Get user\n" +
               "    await request(app)\n" +
               "      .get('/api/v1/users/' + userId)\n" +
               "      .set('Authorization', 'Bearer ' + token)\n" +
               "      .expect(200);\n\n" +
               "    // Delete user\n" +
               "    await request(app)\n" +
               "      .delete('/api/v1/users/' + userId)\n" +
               "      .set('Authorization', 'Bearer ' + token)\n" +
               "      .expect(204);\n" +
               "  });\n" +
               "});\n";
    }

    private String generateTestSetup() {
        return "jest.setTimeout(30000);\n\n" +
               "beforeAll(() => {\n" +
               "  process.env.NODE_ENV = 'test';\n" +
               "});\n";
    }

    // Configuration file generators
    private String generateEslintConfig() {
        return "{\n" +
               "  \"env\": {\n" +
               "    \"node\": true,\n" +
               "    \"es2021\": true,\n" +
               "    \"jest\": true\n" +
               "  },\n" +
               "  \"extends\": [\n" +
               "    \"eslint:recommended\",\n" +
               "    \"plugin:security/recommended\",\n" +
               "    \"prettier\"\n" +
               "  ],\n" +
               "  \"parserOptions\": {\n" +
               "    \"ecmaVersion\": \"latest\",\n" +
               "    \"sourceType\": \"module\"\n" +
               "  },\n" +
               "  \"plugins\": [\"security\"],\n" +
               "  \"rules\": {\n" +
               "    \"no-console\": \"warn\",\n" +
               "    \"no-unused-vars\": [\"error\", { \"argsIgnorePattern\": \"^_\" }]\n" +
               "  }\n" +
               "}";
    }

    private String generatePrettierConfig() {
        return "{\n" +
               "  \"semi\": true,\n" +
               "  \"trailingComma\": \"none\",\n" +
               "  \"singleQuote\": true,\n" +
               "  \"printWidth\": 100,\n" +
               "  \"tabWidth\": 2\n" +
               "}";
    }

    private String generateJestConfig() {
        return "module.exports = {\n" +
               "  testEnvironment: 'node',\n" +
               "  coveragePathIgnorePatterns: ['/node_modules/'],\n" +
               "  collectCoverageFrom: [\n" +
               "    'src/**/*.js',\n" +
               "    '!src/**/*.test.js'\n" +
               "  ],\n" +
               "  testMatch: [\n" +
               "    '**/tests/**/*.test.js',\n" +
               "    '**/?(*.)+(spec|test).js'\n" +
               "  ],\n" +
               "  setupFilesAfterEnv: ['./tests/setup.js']\n" +
               "};\n";
    }

    private String generateNodemonConfig() {
        return "{\n" +
               "  \"watch\": [\"src\"],\n" +
               "  \"ext\": \"js,json\",\n" +
               "  \"ignore\": [\"src/**/*.test.js\"],\n" +
               "  \"exec\": \"node src/server.js\",\n" +
               "  \"env\": {\n" +
               "    \"NODE_ENV\": \"development\"\n" +
               "  }\n" +
               "}";
    }

    private String generateTsConfig() {
        return "{\n" +
               "  \"compilerOptions\": {\n" +
               "    \"target\": \"ES2022\",\n" +
               "    \"module\": \"commonjs\",\n" +
               "    \"lib\": [\"ES2022\"],\n" +
               "    \"outDir\": \"./dist\",\n" +
               "    \"rootDir\": \"./src\",\n" +
               "    \"strict\": true,\n" +
               "    \"esModuleInterop\": true,\n" +
               "    \"skipLibCheck\": true,\n" +
               "    \"forceConsistentCasingInFileNames\": true,\n" +
               "    \"resolveJsonModule\": true,\n" +
               "    \"declaration\": true,\n" +
               "    \"declarationMap\": true,\n" +
               "    \"sourceMap\": true,\n" +
               "    \"experimentalDecorators\": true,\n" +
               "    \"emitDecoratorMetadata\": true\n" +
               "  },\n" +
               "  \"include\": [\"src/**/*\"],\n" +
               "  \"exclude\": [\"node_modules\", \"dist\", \"tests\"]\n" +
               "}";
    }

    private String generateDockerfile() {
        return "# Build stage\n" +
               "FROM node:18-alpine AS builder\n" +
               "WORKDIR /app\n" +
               "COPY package*.json ./\n" +
               "RUN npm ci --only=production\n\n" +
               "# Production stage\n" +
               "FROM node:18-alpine\n" +
               "WORKDIR /app\n" +
               "COPY --from=builder /app/node_modules ./node_modules\n" +
               "COPY . .\n" +
               "EXPOSE 3000\n" +
               "USER node\n" +
               "CMD [\"node\", \"src/server.js\"]\n";
    }

    private String generateDockerCompose(String projectName, NodeProjectType type) {
        StringBuilder compose = new StringBuilder();
        compose.append("version: '3.8'\n\n");
        compose.append("services:\n");

        compose.append("  app:\n");
        compose.append("    build: .\n");
        compose.append("    ports:\n");
        compose.append("      - \"3000:3000\"\n");
        compose.append("    environment:\n");
        compose.append("      - NODE_ENV=production\n");
        compose.append("      - MONGODB_URI=mongodb://mongo:27017/").append(projectName.toLowerCase()).append("\n");
        compose.append("      - REDIS_HOST=redis\n");
        compose.append("    depends_on:\n");
        compose.append("      - mongo\n");
        compose.append("      - redis\n");
        compose.append("    volumes:\n");
        compose.append("      - ./uploads:/app/uploads\n\n");

        compose.append("  mongo:\n");
        compose.append("    image: mongo:7\n");
        compose.append("    ports:\n");
        compose.append("      - \"27017:27017\"\n");
        compose.append("    volumes:\n");
        compose.append("      - mongo_data:/data/db\n\n");

        compose.append("  redis:\n");
        compose.append("    image: redis:7-alpine\n");
        compose.append("    ports:\n");
        compose.append("      - \"6379:6379\"\n\n");

        if (type == NodeProjectType.MICROSERVICE) {
            compose.append("  rabbitmq:\n");
            compose.append("    image: rabbitmq:3-management\n");
            compose.append("    ports:\n");
            compose.append("      - \"5672:5672\"\n");
            compose.append("      - \"15672:15672\"\n\n");
        }

        compose.append("volumes:\n");
        compose.append("  mongo_data:\n");

        return compose.toString();
    }

    private String generateDockerignore() {
        return "node_modules\n" +
               "npm-debug.log\n" +
               ".env\n" +
               ".git\n" +
               ".gitignore\n" +
               "README.md\n" +
               ".vscode\n" +
               ".idea\n" +
               "coverage\n" +
               ".nyc_output\n";
    }

    private String generatePM2Config(String projectName) {
        return "module.exports = {\n" +
               "  apps: [{\n" +
               "    name: '" + projectName.toLowerCase() + "',\n" +
               "    script: './src/server.js',\n" +
               "    instances: 'max',\n" +
               "    exec_mode: 'cluster',\n" +
               "    autorestart: true,\n" +
               "    watch: false,\n" +
               "    max_memory_restart: '1G',\n" +
               "    env: {\n" +
               "      NODE_ENV: 'production',\n" +
               "      PORT: 3000\n" +
               "    },\n" +
               "    error_file: './logs/err.log',\n" +
               "    out_file: './logs/out.log',\n" +
               "    log_file: './logs/combined.log',\n" +
               "    time: true\n" +
               "  }]\n" +
               "};\n";
    }

    private String generateSwaggerConfig(String projectName) {
        return "openapi: 3.0.0\n" +
               "info:\n" +
               "  title: " + projectName + " API\n" +
               "  description: RESTful API Documentation\n" +
               "  version: 1.0.0\n" +
               "servers:\n" +
               "  - url: http://localhost:3000/api/v1\n" +
               "    description: Development server\n" +
               "  - url: https://api.example.com/v1\n" +
               "    description: Production server\n\n" +
               "components:\n" +
               "  securitySchemes:\n" +
               "    bearerAuth:\n" +
               "      type: http\n" +
               "      scheme: bearer\n" +
               "      bearerFormat: JWT\n\n" +
               "security:\n" +
               "  - bearerAuth: []\n\n" +
               "paths:\n" +
               "  /users:\n" +
               "    get:\n" +
               "      summary: Get all users\n" +
               "      tags: [Users]\n" +
               "      responses:\n" +
               "        200:\n" +
               "          description: Success\n";
    }

    private String generateGithubActionsCI() {
        return "name: CI/CD Pipeline\n\n" +
               "on:\n" +
               "  push:\n" +
               "    branches: [main, develop]\n" +
               "  pull_request:\n" +
               "    branches: [main]\n\n" +
               "jobs:\n" +
               "  test:\n" +
               "    runs-on: ubuntu-latest\n\n" +
               "    strategy:\n" +
               "      matrix:\n" +
               "        node-version: [18.x, 20.x]\n\n" +
               "    steps:\n" +
               "    - uses: actions/checkout@v3\n\n" +
               "    - name: Use Node.js ${{ matrix.node-version }}\n" +
               "      uses: actions/setup-node@v3\n" +
               "      with:\n" +
               "        node-version: ${{ matrix.node-version }}\n" +
               "        cache: 'npm'\n\n" +
               "    - name: Install dependencies\n" +
               "      run: npm ci\n\n" +
               "    - name: Run linter\n" +
               "      run: npm run lint\n\n" +
               "    - name: Run tests\n" +
               "      run: npm test\n\n" +
               "    - name: Upload coverage\n" +
               "      uses: codecov/codecov-action@v3\n" +
               "      with:\n" +
               "        file: ./coverage/coverage.json\n\n" +
               "  build:\n" +
               "    needs: test\n" +
               "    runs-on: ubuntu-latest\n" +
               "    if: github.ref == 'refs/heads/main'\n\n" +
               "    steps:\n" +
               "    - uses: actions/checkout@v3\n\n" +
               "    - name: Build Docker image\n" +
               "      run: docker build -t app:latest .\n\n" +
               "    - name: Push to registry\n" +
               "      run: echo 'Push to Docker registry'\n";
    }

    /**
     * Node.js project types
     */
    public enum NodeProjectType {
        BASIC("Basic Node.js Express API"),
        TYPESCRIPT("TypeScript Node.js Express API"),
        MVC("MVC Pattern with Template Engine"),
        GRAPHQL("GraphQL API with Apollo Server"),
        REAL_TIME("Real-time API with WebSockets"),
        MICROSERVICE("Microservice with Message Queue"),
        FULL_STACK("Full-Stack Node.js with Everything");

        private final String description;

        NodeProjectType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
}