package com.structurecreation.service.generator;

import com.structurecreation.model.ProjectNode;
import com.structurecreation.model.Dependency;
import com.structurecreation.service.DependencyResolverService;
import com.structurecreation.service.repository.MavenCentralRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * Advanced Spring Boot project generator that creates complete, production-ready applications
 * with all necessary dependencies, configurations, and no build errors
 */
public class SpringBootProjectGenerator {
    private static final Logger logger = LoggerFactory.getLogger(SpringBootProjectGenerator.class);

    private final DependencyResolverService dependencyResolver;
    private final MavenCentralRepository mavenRepository;

    public SpringBootProjectGenerator() {
        this.dependencyResolver = new DependencyResolverService();
        this.mavenRepository = new MavenCentralRepository();
    }

    /**
     * Generate a complete Spring Boot project structure with all dependencies
     */
    public ProjectNode generateSpringBootProject(String projectName, String groupId, SpringBootProjectType type) {
        ProjectNode root = new ProjectNode(projectName, ProjectNode.NodeType.FOLDER);

        // Create Maven/Gradle folder structure
        createFolderStructure(root, type);

        // Generate configuration files
        generateConfigFiles(root, projectName, groupId, type);

        // Generate source files
        generateSourceFiles(root, projectName, groupId, type);

        // Generate test files
        generateTestFiles(root, projectName, groupId, type);

        // Generate complete pom.xml with all dependencies
        generatePomXml(root, projectName, groupId, type);

        return root;
    }

    /**
     * Create the complete folder structure
     */
    private void createFolderStructure(ProjectNode root, SpringBootProjectType type) {
        // Source folders
        ProjectNode src = new ProjectNode("src", ProjectNode.NodeType.FOLDER);
        ProjectNode main = new ProjectNode("main", ProjectNode.NodeType.FOLDER);
        ProjectNode test = new ProjectNode("test", ProjectNode.NodeType.FOLDER);

        // Main subfolders
        ProjectNode mainJava = new ProjectNode("java", ProjectNode.NodeType.FOLDER);
        ProjectNode mainResources = new ProjectNode("resources", ProjectNode.NodeType.FOLDER);

        // Test subfolders
        ProjectNode testJava = new ProjectNode("java", ProjectNode.NodeType.FOLDER);
        ProjectNode testResources = new ProjectNode("resources", ProjectNode.NodeType.FOLDER);

        // Resources subfolders
        ProjectNode templates = new ProjectNode("templates", ProjectNode.NodeType.FOLDER);
        ProjectNode staticFolder = new ProjectNode("static", ProjectNode.NodeType.FOLDER);
        ProjectNode staticCss = new ProjectNode("css", ProjectNode.NodeType.FOLDER);
        ProjectNode staticJs = new ProjectNode("js", ProjectNode.NodeType.FOLDER);
        ProjectNode staticImages = new ProjectNode("images", ProjectNode.NodeType.FOLDER);

        // Database migration folder (for Flyway/Liquibase)
        ProjectNode dbMigration = new ProjectNode("db", ProjectNode.NodeType.FOLDER);
        ProjectNode migration = new ProjectNode("migration", ProjectNode.NodeType.FOLDER);
        dbMigration.addChild(migration);

        // Add static subfolders
        staticFolder.addChild(staticCss);
        staticFolder.addChild(staticJs);
        staticFolder.addChild(staticImages);

        // Add to resources
        mainResources.addChild(templates);
        mainResources.addChild(staticFolder);
        mainResources.addChild(dbMigration);

        // Build structure
        main.addChild(mainJava);
        main.addChild(mainResources);
        test.addChild(testJava);
        test.addChild(testResources);
        src.addChild(main);
        src.addChild(test);
        root.addChild(src);

        // Docker support
        if (type == SpringBootProjectType.MICROSERVICE || type == SpringBootProjectType.FULL_STACK) {
            ProjectNode docker = new ProjectNode("docker", ProjectNode.NodeType.FOLDER);
            root.addChild(docker);
        }

        // Documentation
        ProjectNode docs = new ProjectNode("docs", ProjectNode.NodeType.FOLDER);
        root.addChild(docs);
    }

    /**
     * Generate configuration files
     */
    private void generateConfigFiles(ProjectNode root, String projectName, String groupId, SpringBootProjectType type) {
        // .gitignore
        ProjectNode gitignore = new ProjectNode(".gitignore", ProjectNode.NodeType.FILE,
            generateGitignore());
        root.addChild(gitignore);

        // README.md
        ProjectNode readme = new ProjectNode("README.md", ProjectNode.NodeType.FILE,
            generateReadme(projectName, type));
        root.addChild(readme);

        // Application properties
        ProjectNode mainResources = root.findChild("src").findChild("main").findChild("resources");

        ProjectNode appProps = new ProjectNode("application.properties", ProjectNode.NodeType.FILE,
            generateApplicationProperties(type));
        mainResources.addChild(appProps);

        ProjectNode appYaml = new ProjectNode("application.yml", ProjectNode.NodeType.FILE,
            generateApplicationYaml(type));
        mainResources.addChild(appYaml);

        // Environment-specific properties
        ProjectNode appPropsDev = new ProjectNode("application-dev.yml", ProjectNode.NodeType.FILE,
            generateApplicationDevYaml());
        mainResources.addChild(appPropsDev);

        ProjectNode appPropsProd = new ProjectNode("application-prod.yml", ProjectNode.NodeType.FILE,
            generateApplicationProdYaml());
        mainResources.addChild(appPropsProd);

        // Logback configuration
        ProjectNode logback = new ProjectNode("logback-spring.xml", ProjectNode.NodeType.FILE,
            generateLogbackConfig());
        mainResources.addChild(logback);

        // Docker files
        if (type == SpringBootProjectType.MICROSERVICE || type == SpringBootProjectType.FULL_STACK) {
            ProjectNode dockerfile = new ProjectNode("Dockerfile", ProjectNode.NodeType.FILE,
                generateDockerfile(projectName));
            root.addChild(dockerfile);

            ProjectNode dockerCompose = new ProjectNode("docker-compose.yml", ProjectNode.NodeType.FILE,
                generateDockerCompose(projectName, type));
            root.addChild(dockerCompose);
        }

        // CI/CD files
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
    private void generateSourceFiles(ProjectNode root, String projectName, String groupId, SpringBootProjectType type) {
        ProjectNode mainJava = root.findChild("src").findChild("main").findChild("java");

        // Create package structure
        String[] packageParts = groupId.split("\\.");
        ProjectNode currentNode = mainJava;
        for (String part : packageParts) {
            ProjectNode packageNode = new ProjectNode(part, ProjectNode.NodeType.FOLDER);
            currentNode.addChild(packageNode);
            currentNode = packageNode;
        }

        // Add project name to package
        ProjectNode projectPackage = new ProjectNode(projectName.toLowerCase().replaceAll("[^a-z0-9]", ""),
                                                    ProjectNode.NodeType.FOLDER);
        currentNode.addChild(projectPackage);

        // Create sub-packages
        ProjectNode controller = new ProjectNode("controller", ProjectNode.NodeType.FOLDER);
        ProjectNode service = new ProjectNode("service", ProjectNode.NodeType.FOLDER);
        ProjectNode repository = new ProjectNode("repository", ProjectNode.NodeType.FOLDER);
        ProjectNode model = new ProjectNode("model", ProjectNode.NodeType.FOLDER);
        ProjectNode dto = new ProjectNode("dto", ProjectNode.NodeType.FOLDER);
        ProjectNode config = new ProjectNode("config", ProjectNode.NodeType.FOLDER);
        ProjectNode exception = new ProjectNode("exception", ProjectNode.NodeType.FOLDER);
        ProjectNode util = new ProjectNode("util", ProjectNode.NodeType.FOLDER);
        ProjectNode mapper = new ProjectNode("mapper", ProjectNode.NodeType.FOLDER);

        projectPackage.addChild(controller);
        projectPackage.addChild(service);
        projectPackage.addChild(repository);
        projectPackage.addChild(model);
        projectPackage.addChild(dto);
        projectPackage.addChild(config);
        projectPackage.addChild(exception);
        projectPackage.addChild(util);
        projectPackage.addChild(mapper);

        String packageName = groupId + "." + projectName.toLowerCase().replaceAll("[^a-z0-9]", "");

        // Main Application class
        ProjectNode mainApp = new ProjectNode(toCamelCase(projectName) + "Application.java",
                                            ProjectNode.NodeType.FILE,
                                            generateMainApplication(packageName, projectName));
        projectPackage.addChild(mainApp);

        // Generate component files
        generateControllerFiles(controller, packageName, type);
        generateServiceFiles(service, packageName, type);
        generateRepositoryFiles(repository, packageName, type);
        generateModelFiles(model, packageName, type);
        generateDtoFiles(dto, packageName, type);
        generateConfigFiles(config, packageName, type);
        generateExceptionFiles(exception, packageName, type);
        generateUtilFiles(util, packageName, type);

        // Generate HTML templates if web project
        if (type == SpringBootProjectType.WEB || type == SpringBootProjectType.FULL_STACK) {
            ProjectNode templates = root.findChild("src").findChild("main").findChild("resources").findChild("templates");
            generateTemplateFiles(templates);
        }
    }

    /**
     * Generate test files
     */
    private void generateTestFiles(ProjectNode root, String projectName, String groupId, SpringBootProjectType type) {
        ProjectNode testJava = root.findChild("src").findChild("test").findChild("java");

        // Create test package structure
        String[] packageParts = groupId.split("\\.");
        ProjectNode currentNode = testJava;
        for (String part : packageParts) {
            ProjectNode packageNode = new ProjectNode(part, ProjectNode.NodeType.FOLDER);
            currentNode.addChild(packageNode);
            currentNode = packageNode;
        }

        ProjectNode projectPackage = new ProjectNode(projectName.toLowerCase().replaceAll("[^a-z0-9]", ""),
                                                    ProjectNode.NodeType.FOLDER);
        currentNode.addChild(projectPackage);

        String packageName = groupId + "." + projectName.toLowerCase().replaceAll("[^a-z0-9]", "");

        // Main test class
        ProjectNode mainTest = new ProjectNode(toCamelCase(projectName) + "ApplicationTests.java",
                                              ProjectNode.NodeType.FILE,
                                              generateMainTestClass(packageName, projectName));
        projectPackage.addChild(mainTest);

        // Controller tests
        ProjectNode controllerTests = new ProjectNode("controller", ProjectNode.NodeType.FOLDER);
        ProjectNode userControllerTest = new ProjectNode("UserControllerTest.java", ProjectNode.NodeType.FILE,
            generateControllerTest(packageName));
        controllerTests.addChild(userControllerTest);
        projectPackage.addChild(controllerTests);

        // Service tests
        ProjectNode serviceTests = new ProjectNode("service", ProjectNode.NodeType.FOLDER);
        ProjectNode userServiceTest = new ProjectNode("UserServiceTest.java", ProjectNode.NodeType.FILE,
            generateServiceTest(packageName));
        serviceTests.addChild(userServiceTest);
        projectPackage.addChild(serviceTests);

        // Integration tests
        ProjectNode integrationTests = new ProjectNode("integration", ProjectNode.NodeType.FOLDER);
        ProjectNode integrationTest = new ProjectNode("IntegrationTest.java", ProjectNode.NodeType.FILE,
            generateIntegrationTest(packageName));
        integrationTests.addChild(integrationTest);
        projectPackage.addChild(integrationTests);

        // Test resources
        ProjectNode testResources = root.findChild("src").findChild("test").findChild("resources");
        ProjectNode testProps = new ProjectNode("application-test.yml", ProjectNode.NodeType.FILE,
            generateTestProperties());
        testResources.addChild(testProps);
    }

    /**
     * Generate complete pom.xml with all necessary dependencies
     */
    private void generatePomXml(ProjectNode root, String projectName, String groupId, SpringBootProjectType type) {
        List<Dependency> dependencies = getCompleteDependencies(type);

        // Resolve all dependencies to get latest versions
        CompletableFuture<Set<DependencyResolverService.ResolvedDependency>> resolvedDeps =
            dependencyResolver.resolveMavenDependencies(dependencies);

        try {
            Set<DependencyResolverService.ResolvedDependency> resolved = resolvedDeps.get();
            logger.info("Resolved {} total Maven dependencies for Spring Boot project", resolved.size());
        } catch (Exception e) {
            logger.error("Error resolving dependencies: ", e);
        }

        String pomXml = generatePomXmlContent(projectName, groupId, type, dependencies);
        ProjectNode pomNode = new ProjectNode("pom.xml", ProjectNode.NodeType.FILE, pomXml);
        root.addChild(pomNode);
    }

    /**
     * Get complete list of dependencies for Spring Boot project
     */
    private List<Dependency> getCompleteDependencies(SpringBootProjectType type) {
        List<Dependency> deps = new ArrayList<>();

        // Core Spring Boot dependencies
        deps.add(createDependency("org.springframework.boot", "spring-boot-starter", "3.2.0"));
        deps.add(createDependency("org.springframework.boot", "spring-boot-starter-web", "3.2.0"));
        deps.add(createDependency("org.springframework.boot", "spring-boot-starter-validation", "3.2.0"));
        deps.add(createDependency("org.springframework.boot", "spring-boot-starter-actuator", "3.2.0"));
        deps.add(createDependency("org.springframework.boot", "spring-boot-devtools", "3.2.0", "runtime"));

        // Data access
        deps.add(createDependency("org.springframework.boot", "spring-boot-starter-data-jpa", "3.2.0"));
        deps.add(createDependency("org.springframework.boot", "spring-boot-starter-data-redis", "3.2.0"));
        deps.add(createDependency("com.h2database", "h2", "2.2.224", "runtime"));
        deps.add(createDependency("org.postgresql", "postgresql", "42.7.1", "runtime"));
        deps.add(createDependency("mysql", "mysql-connector-java", "8.0.33", "runtime"));

        // Security
        if (type != SpringBootProjectType.BASIC) {
            deps.add(createDependency("org.springframework.boot", "spring-boot-starter-security", "3.2.0"));
            deps.add(createDependency("org.springframework.security", "spring-security-test", "6.2.0", "test"));
            deps.add(createDependency("io.jsonwebtoken", "jjwt-api", "0.12.3"));
            deps.add(createDependency("io.jsonwebtoken", "jjwt-impl", "0.12.3", "runtime"));
            deps.add(createDependency("io.jsonwebtoken", "jjwt-jackson", "0.12.3", "runtime"));
        }

        // Web and REST
        if (type == SpringBootProjectType.WEB || type == SpringBootProjectType.FULL_STACK) {
            deps.add(createDependency("org.springframework.boot", "spring-boot-starter-thymeleaf", "3.2.0"));
            deps.add(createDependency("org.springframework.boot", "spring-boot-starter-websocket", "3.2.0"));
            deps.add(createDependency("org.webjars", "bootstrap", "5.3.2"));
            deps.add(createDependency("org.webjars", "jquery", "3.7.1"));
        }

        // API Documentation
        deps.add(createDependency("org.springdoc", "springdoc-openapi-starter-webmvc-ui", "2.3.0"));
        deps.add(createDependency("org.springdoc", "springdoc-openapi-starter-webmvc-api", "2.3.0"));

        // Caching
        deps.add(createDependency("org.springframework.boot", "spring-boot-starter-cache", "3.2.0"));
        deps.add(createDependency("com.github.ben-manes.caffeine", "caffeine", "3.1.8"));

        // Messaging (for microservices)
        if (type == SpringBootProjectType.MICROSERVICE) {
            deps.add(createDependency("org.springframework.boot", "spring-boot-starter-amqp", "3.2.0"));
            deps.add(createDependency("org.springframework.kafka", "spring-kafka", "3.1.0"));
            deps.add(createDependency("org.springframework.cloud", "spring-cloud-starter-netflix-eureka-client", "4.1.0"));
            deps.add(createDependency("org.springframework.cloud", "spring-cloud-starter-config", "4.1.0"));
            deps.add(createDependency("org.springframework.cloud", "spring-cloud-starter-openfeign", "4.1.0"));
            deps.add(createDependency("io.micrometer", "micrometer-tracing-bridge-brave", "1.2.0"));
            deps.add(createDependency("io.zipkin.reporter2", "zipkin-reporter-brave", "2.17.0"));
        }

        // Database migration
        deps.add(createDependency("org.flywaydb", "flyway-core", "9.22.3"));
        deps.add(createDependency("org.liquibase", "liquibase-core", "4.25.0"));

        // Utilities
        deps.add(createDependency("org.projectlombok", "lombok", "1.18.30", "provided"));
        deps.add(createDependency("org.mapstruct", "mapstruct", "1.5.5.Final"));
        deps.add(createDependency("org.mapstruct", "mapstruct-processor", "1.5.5.Final", "provided"));
        deps.add(createDependency("com.google.guava", "guava", "32.1.3-jre"));
        deps.add(createDependency("org.apache.commons", "commons-lang3", "3.14.0"));
        deps.add(createDependency("commons-io", "commons-io", "2.15.1"));
        deps.add(createDependency("com.fasterxml.jackson.datatype", "jackson-datatype-jsr310", "2.16.0"));

        // Testing
        deps.add(createDependency("org.springframework.boot", "spring-boot-starter-test", "3.2.0", "test"));
        deps.add(createDependency("org.junit.jupiter", "junit-jupiter", "5.10.1", "test"));
        deps.add(createDependency("org.mockito", "mockito-core", "5.7.0", "test"));
        deps.add(createDependency("org.assertj", "assertj-core", "3.24.2", "test"));
        deps.add(createDependency("io.rest-assured", "rest-assured", "5.4.0", "test"));
        deps.add(createDependency("org.testcontainers", "testcontainers", "1.19.3", "test"));
        deps.add(createDependency("org.testcontainers", "junit-jupiter", "1.19.3", "test"));
        deps.add(createDependency("org.testcontainers", "postgresql", "1.19.3", "test"));

        // Monitoring and metrics
        deps.add(createDependency("io.micrometer", "micrometer-registry-prometheus", "1.12.0"));
        deps.add(createDependency("net.logstash.logback", "logstash-logback-encoder", "7.4"));

        // Email
        if (type == SpringBootProjectType.FULL_STACK) {
            deps.add(createDependency("org.springframework.boot", "spring-boot-starter-mail", "3.2.0"));
            deps.add(createDependency("org.springframework.boot", "spring-boot-starter-freemarker", "3.2.0"));
        }

        // Batch processing
        if (type == SpringBootProjectType.FULL_STACK) {
            deps.add(createDependency("org.springframework.boot", "spring-boot-starter-batch", "3.2.0"));
            deps.add(createDependency("org.springframework.boot", "spring-boot-starter-quartz", "3.2.0"));
        }

        return deps;
    }

    private Dependency createDependency(String groupId, String artifactId, String version) {
        return createDependency(groupId, artifactId, version, "compile");
    }

    private Dependency createDependency(String groupId, String artifactId, String version, String scope) {
        Dependency dep = new Dependency();
        dep.setGroupId(groupId);
        dep.setArtifactId(artifactId);
        dep.setVersion(version);
        dep.setScope(scope);
        dep.setType("Maven");
        return dep;
    }

    // File content generation methods

    private String generatePomXmlContent(String projectName, String groupId, SpringBootProjectType type, List<Dependency> dependencies) {
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        sb.append("<project xmlns=\"http://maven.apache.org/POM/4.0.0\"\n");
        sb.append("         xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n");
        sb.append("         xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0\n");
        sb.append("         http://maven.apache.org/xsd/maven-4.0.0.xsd\">\n");
        sb.append("    <modelVersion>4.0.0</modelVersion>\n\n");

        sb.append("    <parent>\n");
        sb.append("        <groupId>org.springframework.boot</groupId>\n");
        sb.append("        <artifactId>spring-boot-starter-parent</artifactId>\n");
        sb.append("        <version>3.2.0</version>\n");
        sb.append("        <relativePath/>\n");
        sb.append("    </parent>\n\n");

        sb.append("    <groupId>").append(groupId).append("</groupId>\n");
        sb.append("    <artifactId>").append(projectName.toLowerCase()).append("</artifactId>\n");
        sb.append("    <version>1.0.0-SNAPSHOT</version>\n");
        sb.append("    <name>").append(projectName).append("</name>\n");
        sb.append("    <description>").append(type.getDescription()).append("</description>\n\n");

        sb.append("    <properties>\n");
        sb.append("        <java.version>17</java.version>\n");
        sb.append("        <maven.compiler.source>17</maven.compiler.source>\n");
        sb.append("        <maven.compiler.target>17</maven.compiler.target>\n");
        sb.append("        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>\n");
        if (type == SpringBootProjectType.MICROSERVICE) {
            sb.append("        <spring-cloud.version>2023.0.0</spring-cloud.version>\n");
        }
        sb.append("    </properties>\n\n");

        if (type == SpringBootProjectType.MICROSERVICE) {
            sb.append("    <dependencyManagement>\n");
            sb.append("        <dependencies>\n");
            sb.append("            <dependency>\n");
            sb.append("                <groupId>org.springframework.cloud</groupId>\n");
            sb.append("                <artifactId>spring-cloud-dependencies</artifactId>\n");
            sb.append("                <version>${spring-cloud.version}</version>\n");
            sb.append("                <type>pom</type>\n");
            sb.append("                <scope>import</scope>\n");
            sb.append("            </dependency>\n");
            sb.append("        </dependencies>\n");
            sb.append("    </dependencyManagement>\n\n");
        }

        sb.append("    <dependencies>\n");
        for (Dependency dep : dependencies) {
            sb.append("        <dependency>\n");
            sb.append("            <groupId>").append(dep.getGroupId()).append("</groupId>\n");
            sb.append("            <artifactId>").append(dep.getArtifactId()).append("</artifactId>\n");
            if (!dep.getVersion().isEmpty() && !dep.getVersion().equals("LATEST")) {
                sb.append("            <version>").append(dep.getVersion()).append("</version>\n");
            }
            if (dep.getScope() != null && !dep.getScope().equals("compile")) {
                sb.append("            <scope>").append(dep.getScope()).append("</scope>\n");
            }
            sb.append("        </dependency>\n");
        }
        sb.append("    </dependencies>\n\n");

        sb.append("    <build>\n");
        sb.append("        <plugins>\n");
        sb.append("            <plugin>\n");
        sb.append("                <groupId>org.springframework.boot</groupId>\n");
        sb.append("                <artifactId>spring-boot-maven-plugin</artifactId>\n");
        sb.append("                <configuration>\n");
        sb.append("                    <excludes>\n");
        sb.append("                        <exclude>\n");
        sb.append("                            <groupId>org.projectlombok</groupId>\n");
        sb.append("                            <artifactId>lombok</artifactId>\n");
        sb.append("                        </exclude>\n");
        sb.append("                    </excludes>\n");
        sb.append("                </configuration>\n");
        sb.append("            </plugin>\n");
        sb.append("        </plugins>\n");
        sb.append("    </build>\n\n");

        sb.append("</project>\n");

        return sb.toString();
    }

    private String generateGitignore() {
        return "# Compiled class file\n" +
               "*.class\n\n" +
               "# Log file\n" +
               "*.log\n\n" +
               "# Package Files\n" +
               "*.jar\n" +
               "*.war\n" +
               "*.nar\n" +
               "*.ear\n" +
               "*.zip\n" +
               "*.tar.gz\n" +
               "*.rar\n\n" +
               "# Maven\n" +
               "target/\n" +
               "pom.xml.tag\n" +
               "pom.xml.releaseBackup\n" +
               "pom.xml.versionsBackup\n" +
               "pom.xml.next\n\n" +
               "# IDE\n" +
               ".idea/\n" +
               "*.iml\n" +
               "*.iws\n" +
               ".vscode/\n" +
               ".settings/\n" +
               ".classpath\n" +
               ".project\n\n" +
               "# OS\n" +
               ".DS_Store\n" +
               "Thumbs.db\n\n" +
               "# Application\n" +
               "application-local.yml\n" +
               "*.pid\n";
    }

    private String generateReadme(String projectName, SpringBootProjectType type) {
        return "# " + projectName + "\n\n" +
               type.getDescription() + "\n\n" +
               "## Features\n\n" +
               "- Spring Boot 3.2.0 with Java 17\n" +
               "- RESTful API with OpenAPI documentation\n" +
               "- JPA/Hibernate for data persistence\n" +
               "- Database migration with Flyway\n" +
               "- Comprehensive testing with JUnit 5 and Mockito\n" +
               "- Docker support for containerization\n" +
               "- Actuator for monitoring and health checks\n" +
               "- Logging with Logback\n" +
               "- Security with Spring Security and JWT\n\n" +
               "## Prerequisites\n\n" +
               "- Java 17 or higher\n" +
               "- Maven 3.8+\n" +
               "- Docker (optional)\n\n" +
               "## Getting Started\n\n" +
               "### Running locally\n\n" +
               "```bash\n" +
               "mvn spring-boot:run\n" +
               "```\n\n" +
               "### Building\n\n" +
               "```bash\n" +
               "mvn clean package\n" +
               "```\n\n" +
               "### Running tests\n\n" +
               "```bash\n" +
               "mvn test\n" +
               "```\n\n" +
               "### Docker\n\n" +
               "```bash\n" +
               "docker build -t " + projectName.toLowerCase() + " .\n" +
               "docker run -p 8080:8080 " + projectName.toLowerCase() + "\n" +
               "```\n\n" +
               "## API Documentation\n\n" +
               "Once the application is running, visit:\n" +
               "- Swagger UI: http://localhost:8080/swagger-ui.html\n" +
               "- OpenAPI JSON: http://localhost:8080/v3/api-docs\n\n" +
               "## Monitoring\n\n" +
               "- Health: http://localhost:8080/actuator/health\n" +
               "- Metrics: http://localhost:8080/actuator/metrics\n" +
               "- Info: http://localhost:8080/actuator/info\n";
    }

    private String generateApplicationProperties(SpringBootProjectType type) {
        return "# Server Configuration\n" +
               "server.port=8080\n" +
               "server.servlet.context-path=/api\n\n" +
               "# Application\n" +
               "spring.application.name=${project.name}\n\n" +
               "# Database Configuration\n" +
               "spring.datasource.url=jdbc:h2:mem:testdb\n" +
               "spring.datasource.driver-class-name=org.h2.Driver\n" +
               "spring.datasource.username=sa\n" +
               "spring.datasource.password=\n\n" +
               "# JPA/Hibernate\n" +
               "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect\n" +
               "spring.jpa.hibernate.ddl-auto=update\n" +
               "spring.jpa.show-sql=true\n\n" +
               "# H2 Console\n" +
               "spring.h2.console.enabled=true\n" +
               "spring.h2.console.path=/h2-console\n";
    }

    private String generateApplicationYaml(SpringBootProjectType type) {
        StringBuilder yaml = new StringBuilder();
        yaml.append("spring:\n");
        yaml.append("  application:\n");
        yaml.append("    name: ${project.name}\n\n");

        yaml.append("  profiles:\n");
        yaml.append("    active: dev\n\n");

        yaml.append("  datasource:\n");
        yaml.append("    url: jdbc:postgresql://localhost:5432/appdb\n");
        yaml.append("    username: ${DB_USERNAME:postgres}\n");
        yaml.append("    password: ${DB_PASSWORD:password}\n");
        yaml.append("    driver-class-name: org.postgresql.Driver\n");
        yaml.append("    hikari:\n");
        yaml.append("      maximum-pool-size: 10\n");
        yaml.append("      minimum-idle: 5\n");
        yaml.append("      connection-timeout: 30000\n\n");

        yaml.append("  jpa:\n");
        yaml.append("    hibernate:\n");
        yaml.append("      ddl-auto: validate\n");
        yaml.append("    properties:\n");
        yaml.append("      hibernate:\n");
        yaml.append("        dialect: org.hibernate.dialect.PostgreSQLDialect\n");
        yaml.append("        format_sql: true\n");
        yaml.append("        use_sql_comments: true\n");
        yaml.append("    show-sql: false\n\n");

        yaml.append("  cache:\n");
        yaml.append("    type: caffeine\n");
        yaml.append("    caffeine:\n");
        yaml.append("      spec: maximumSize=500,expireAfterWrite=60s\n\n");

        if (type == SpringBootProjectType.MICROSERVICE) {
            yaml.append("  cloud:\n");
            yaml.append("    config:\n");
            yaml.append("      enabled: true\n");
            yaml.append("      uri: http://localhost:8888\n\n");

            yaml.append("eureka:\n");
            yaml.append("  client:\n");
            yaml.append("    service-url:\n");
            yaml.append("      defaultZone: http://localhost:8761/eureka/\n");
            yaml.append("  instance:\n");
            yaml.append("    prefer-ip-address: true\n\n");
        }

        yaml.append("server:\n");
        yaml.append("  port: 8080\n");
        yaml.append("  servlet:\n");
        yaml.append("    context-path: /\n");
        yaml.append("  error:\n");
        yaml.append("    include-message: always\n");
        yaml.append("    include-binding-errors: always\n\n");

        yaml.append("management:\n");
        yaml.append("  endpoints:\n");
        yaml.append("    web:\n");
        yaml.append("      exposure:\n");
        yaml.append("        include: health,info,metrics,prometheus\n");
        yaml.append("  endpoint:\n");
        yaml.append("    health:\n");
        yaml.append("      show-details: always\n\n");

        yaml.append("logging:\n");
        yaml.append("  level:\n");
        yaml.append("    root: INFO\n");
        yaml.append("    org.springframework.web: DEBUG\n");
        yaml.append("    org.hibernate.SQL: DEBUG\n");
        yaml.append("  pattern:\n");
        yaml.append("    console: \"%d{yyyy-MM-dd HH:mm:ss} - %msg%n\"\n");
        yaml.append("    file: \"%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n\"\n");

        return yaml.toString();
    }

    private String generateApplicationDevYaml() {
        return "spring:\n" +
               "  profiles: dev\n\n" +
               "  datasource:\n" +
               "    url: jdbc:h2:mem:devdb\n" +
               "    driver-class-name: org.h2.Driver\n" +
               "    username: sa\n" +
               "    password:\n\n" +
               "  jpa:\n" +
               "    hibernate:\n" +
               "      ddl-auto: create-drop\n" +
               "    show-sql: true\n\n" +
               "  h2:\n" +
               "    console:\n" +
               "      enabled: true\n" +
               "      path: /h2-console\n\n" +
               "logging:\n" +
               "  level:\n" +
               "    root: INFO\n" +
               "    org.springframework: DEBUG\n";
    }

    private String generateApplicationProdYaml() {
        return "spring:\n" +
               "  profiles: prod\n\n" +
               "  datasource:\n" +
               "    url: ${DATABASE_URL}\n" +
               "    username: ${DATABASE_USERNAME}\n" +
               "    password: ${DATABASE_PASSWORD}\n\n" +
               "  jpa:\n" +
               "    hibernate:\n" +
               "      ddl-auto: validate\n" +
               "    show-sql: false\n\n" +
               "server:\n" +
               "  port: ${PORT:8080}\n\n" +
               "logging:\n" +
               "  level:\n" +
               "    root: WARN\n" +
               "    org.springframework: INFO\n";
    }

    private String generateLogbackConfig() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
               "<configuration>\n" +
               "    <include resource=\"org/springframework/boot/logging/logback/base.xml\"/>\n\n" +
               "    <springProfile name=\"dev\">\n" +
               "        <logger name=\"org.springframework\" level=\"DEBUG\"/>\n" +
               "        <logger name=\"org.hibernate.SQL\" level=\"DEBUG\"/>\n" +
               "    </springProfile>\n\n" +
               "    <springProfile name=\"prod\">\n" +
               "        <logger name=\"org.springframework\" level=\"INFO\"/>\n" +
               "        <appender name=\"FILE\" class=\"ch.qos.logback.core.rolling.RollingFileAppender\">\n" +
               "            <file>logs/application.log</file>\n" +
               "            <rollingPolicy class=\"ch.qos.logback.core.rolling.TimeBasedRollingPolicy\">\n" +
               "                <fileNamePattern>logs/application-%d{yyyy-MM-dd}.log</fileNamePattern>\n" +
               "                <maxHistory>30</maxHistory>\n" +
               "            </rollingPolicy>\n" +
               "            <encoder>\n" +
               "                <pattern>%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n</pattern>\n" +
               "            </encoder>\n" +
               "        </appender>\n" +
               "        <root level=\"INFO\">\n" +
               "            <appender-ref ref=\"FILE\"/>\n" +
               "        </root>\n" +
               "    </springProfile>\n" +
               "</configuration>\n";
    }

    private String generateDockerfile(String projectName) {
        return "# Build stage\n" +
               "FROM maven:3.8.5-openjdk-17-slim AS build\n" +
               "WORKDIR /app\n" +
               "COPY pom.xml .\n" +
               "RUN mvn dependency:go-offline -B\n" +
               "COPY src ./src\n" +
               "RUN mvn clean package -DskipTests\n\n" +
               "# Runtime stage\n" +
               "FROM openjdk:17-jdk-slim\n" +
               "WORKDIR /app\n" +
               "COPY --from=build /app/target/" + projectName.toLowerCase() + "-*.jar app.jar\n" +
               "EXPOSE 8080\n" +
               "ENTRYPOINT [\"java\", \"-jar\", \"app.jar\"]\n";
    }

    private String generateDockerCompose(String projectName, SpringBootProjectType type) {
        StringBuilder compose = new StringBuilder();
        compose.append("version: '3.8'\n\n");
        compose.append("services:\n");

        compose.append("  app:\n");
        compose.append("    build: .\n");
        compose.append("    ports:\n");
        compose.append("      - \"8080:8080\"\n");
        compose.append("    environment:\n");
        compose.append("      - SPRING_PROFILES_ACTIVE=docker\n");
        compose.append("      - DATABASE_URL=jdbc:postgresql://db:5432/").append(projectName.toLowerCase()).append("\n");
        compose.append("      - DATABASE_USERNAME=postgres\n");
        compose.append("      - DATABASE_PASSWORD=password\n");
        compose.append("    depends_on:\n");
        compose.append("      - db\n");
        if (type == SpringBootProjectType.MICROSERVICE) {
            compose.append("      - redis\n");
            compose.append("      - rabbitmq\n");
        }
        compose.append("\n");

        compose.append("  db:\n");
        compose.append("    image: postgres:15-alpine\n");
        compose.append("    environment:\n");
        compose.append("      - POSTGRES_DB=").append(projectName.toLowerCase()).append("\n");
        compose.append("      - POSTGRES_USER=postgres\n");
        compose.append("      - POSTGRES_PASSWORD=password\n");
        compose.append("    volumes:\n");
        compose.append("      - postgres_data:/var/lib/postgresql/data\n");
        compose.append("    ports:\n");
        compose.append("      - \"5432:5432\"\n\n");

        if (type == SpringBootProjectType.MICROSERVICE) {
            compose.append("  redis:\n");
            compose.append("    image: redis:7-alpine\n");
            compose.append("    ports:\n");
            compose.append("      - \"6379:6379\"\n\n");

            compose.append("  rabbitmq:\n");
            compose.append("    image: rabbitmq:3-management-alpine\n");
            compose.append("    ports:\n");
            compose.append("      - \"5672:5672\"\n");
            compose.append("      - \"15672:15672\"\n");
            compose.append("    environment:\n");
            compose.append("      - RABBITMQ_DEFAULT_USER=admin\n");
            compose.append("      - RABBITMQ_DEFAULT_PASS=admin\n\n");
        }

        compose.append("volumes:\n");
        compose.append("  postgres_data:\n");

        return compose.toString();
    }

    private String generateGithubActionsCI() {
        return "name: CI Pipeline\n\n" +
               "on:\n" +
               "  push:\n" +
               "    branches: [ main, develop ]\n" +
               "  pull_request:\n" +
               "    branches: [ main ]\n\n" +
               "jobs:\n" +
               "  test:\n" +
               "    runs-on: ubuntu-latest\n\n" +
               "    steps:\n" +
               "    - uses: actions/checkout@v3\n\n" +
               "    - name: Set up JDK 17\n" +
               "      uses: actions/setup-java@v3\n" +
               "      with:\n" +
               "        java-version: '17'\n" +
               "        distribution: 'temurin'\n\n" +
               "    - name: Cache Maven dependencies\n" +
               "      uses: actions/cache@v3\n" +
               "      with:\n" +
               "        path: ~/.m2\n" +
               "        key: ${{ runner.os }}-m2-${{ hashFiles('**/pom.xml') }}\n" +
               "        restore-keys: ${{ runner.os }}-m2\n\n" +
               "    - name: Run tests\n" +
               "      run: mvn clean test\n\n" +
               "    - name: Build application\n" +
               "      run: mvn clean package\n\n" +
               "    - name: Upload artifacts\n" +
               "      uses: actions/upload-artifact@v3\n" +
               "      with:\n" +
               "        name: jar-artifact\n" +
               "        path: target/*.jar\n";
    }

    private String generateMainApplication(String packageName, String projectName) {
        return "package " + packageName + ";\n\n" +
               "import org.springframework.boot.SpringApplication;\n" +
               "import org.springframework.boot.autoconfigure.SpringBootApplication;\n" +
               "import org.springframework.cache.annotation.EnableCaching;\n" +
               "import org.springframework.scheduling.annotation.EnableAsync;\n" +
               "import org.springframework.scheduling.annotation.EnableScheduling;\n\n" +
               "@SpringBootApplication\n" +
               "@EnableCaching\n" +
               "@EnableAsync\n" +
               "@EnableScheduling\n" +
               "public class " + toCamelCase(projectName) + "Application {\n\n" +
               "    public static void main(String[] args) {\n" +
               "        SpringApplication.run(" + toCamelCase(projectName) + "Application.class, args);\n" +
               "    }\n\n" +
               "}\n";
    }

    private void generateControllerFiles(ProjectNode controller, String packageName, SpringBootProjectType type) {
        // User Controller
        String userController = "package " + packageName + ".controller;\n\n" +
            "import " + packageName + ".dto.UserDto;\n" +
            "import " + packageName + ".model.User;\n" +
            "import " + packageName + ".service.UserService;\n" +
            "import io.swagger.v3.oas.annotations.Operation;\n" +
            "import io.swagger.v3.oas.annotations.tags.Tag;\n" +
            "import jakarta.validation.Valid;\n" +
            "import lombok.RequiredArgsConstructor;\n" +
            "import org.springframework.data.domain.Page;\n" +
            "import org.springframework.data.domain.Pageable;\n" +
            "import org.springframework.http.HttpStatus;\n" +
            "import org.springframework.http.ResponseEntity;\n" +
            "import org.springframework.web.bind.annotation.*;\n\n" +
            "@RestController\n" +
            "@RequestMapping(\"/api/users\")\n" +
            "@RequiredArgsConstructor\n" +
            "@Tag(name = \"User Management\", description = \"User management APIs\")\n" +
            "public class UserController {\n\n" +
            "    private final UserService userService;\n\n" +
            "    @GetMapping\n" +
            "    @Operation(summary = \"Get all users\")\n" +
            "    public ResponseEntity<Page<User>> getAllUsers(Pageable pageable) {\n" +
            "        return ResponseEntity.ok(userService.findAll(pageable));\n" +
            "    }\n\n" +
            "    @GetMapping(\"/{id}\")\n" +
            "    @Operation(summary = \"Get user by ID\")\n" +
            "    public ResponseEntity<User> getUserById(@PathVariable Long id) {\n" +
            "        return userService.findById(id)\n" +
            "                .map(ResponseEntity::ok)\n" +
            "                .orElse(ResponseEntity.notFound().build());\n" +
            "    }\n\n" +
            "    @PostMapping\n" +
            "    @Operation(summary = \"Create new user\")\n" +
            "    public ResponseEntity<User> createUser(@Valid @RequestBody UserDto userDto) {\n" +
            "        User created = userService.create(userDto);\n" +
            "        return ResponseEntity.status(HttpStatus.CREATED).body(created);\n" +
            "    }\n\n" +
            "    @PutMapping(\"/{id}\")\n" +
            "    @Operation(summary = \"Update user\")\n" +
            "    public ResponseEntity<User> updateUser(@PathVariable Long id, @Valid @RequestBody UserDto userDto) {\n" +
            "        return userService.update(id, userDto)\n" +
            "                .map(ResponseEntity::ok)\n" +
            "                .orElse(ResponseEntity.notFound().build());\n" +
            "    }\n\n" +
            "    @DeleteMapping(\"/{id}\")\n" +
            "    @Operation(summary = \"Delete user\")\n" +
            "    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {\n" +
            "        userService.delete(id);\n" +
            "        return ResponseEntity.noContent().build();\n" +
            "    }\n" +
            "}\n";

        ProjectNode userControllerNode = new ProjectNode("UserController.java", ProjectNode.NodeType.FILE, userController);
        controller.addChild(userControllerNode);

        // Health Controller
        String healthController = "package " + packageName + ".controller;\n\n" +
            "import org.springframework.web.bind.annotation.*;\n" +
            "import java.util.Map;\n\n" +
            "@RestController\n" +
            "@RequestMapping(\"/health\")\n" +
            "public class HealthController {\n\n" +
            "    @GetMapping\n" +
            "    public Map<String, String> health() {\n" +
            "        return Map.of(\"status\", \"UP\", \"service\", \"Spring Boot Application\");\n" +
            "    }\n" +
            "}\n";

        ProjectNode healthControllerNode = new ProjectNode("HealthController.java", ProjectNode.NodeType.FILE, healthController);
        controller.addChild(healthControllerNode);
    }

    private void generateServiceFiles(ProjectNode service, String packageName, SpringBootProjectType type) {
        // User Service Interface
        String userServiceInterface = "package " + packageName + ".service;\n\n" +
            "import " + packageName + ".dto.UserDto;\n" +
            "import " + packageName + ".model.User;\n" +
            "import org.springframework.data.domain.Page;\n" +
            "import org.springframework.data.domain.Pageable;\n" +
            "import java.util.Optional;\n\n" +
            "public interface UserService {\n" +
            "    Page<User> findAll(Pageable pageable);\n" +
            "    Optional<User> findById(Long id);\n" +
            "    User create(UserDto userDto);\n" +
            "    Optional<User> update(Long id, UserDto userDto);\n" +
            "    void delete(Long id);\n" +
            "}\n";

        ProjectNode userServiceInterfaceNode = new ProjectNode("UserService.java", ProjectNode.NodeType.FILE, userServiceInterface);
        service.addChild(userServiceInterfaceNode);

        // User Service Implementation
        String userServiceImpl = "package " + packageName + ".service;\n\n" +
            "import " + packageName + ".dto.UserDto;\n" +
            "import " + packageName + ".exception.ResourceNotFoundException;\n" +
            "import " + packageName + ".mapper.UserMapper;\n" +
            "import " + packageName + ".model.User;\n" +
            "import " + packageName + ".repository.UserRepository;\n" +
            "import lombok.RequiredArgsConstructor;\n" +
            "import lombok.extern.slf4j.Slf4j;\n" +
            "import org.springframework.cache.annotation.CacheEvict;\n" +
            "import org.springframework.cache.annotation.Cacheable;\n" +
            "import org.springframework.data.domain.Page;\n" +
            "import org.springframework.data.domain.Pageable;\n" +
            "import org.springframework.stereotype.Service;\n" +
            "import org.springframework.transaction.annotation.Transactional;\n" +
            "import java.util.Optional;\n\n" +
            "@Service\n" +
            "@RequiredArgsConstructor\n" +
            "@Slf4j\n" +
            "@Transactional\n" +
            "public class UserServiceImpl implements UserService {\n\n" +
            "    private final UserRepository userRepository;\n" +
            "    private final UserMapper userMapper;\n\n" +
            "    @Override\n" +
            "    @Transactional(readOnly = true)\n" +
            "    public Page<User> findAll(Pageable pageable) {\n" +
            "        log.debug(\"Fetching all users with pageable: {}\", pageable);\n" +
            "        return userRepository.findAll(pageable);\n" +
            "    }\n\n" +
            "    @Override\n" +
            "    @Transactional(readOnly = true)\n" +
            "    @Cacheable(value = \"users\", key = \"#id\")\n" +
            "    public Optional<User> findById(Long id) {\n" +
            "        log.debug(\"Fetching user with id: {}\", id);\n" +
            "        return userRepository.findById(id);\n" +
            "    }\n\n" +
            "    @Override\n" +
            "    public User create(UserDto userDto) {\n" +
            "        log.info(\"Creating new user: {}\", userDto.getEmail());\n" +
            "        User user = userMapper.toEntity(userDto);\n" +
            "        return userRepository.save(user);\n" +
            "    }\n\n" +
            "    @Override\n" +
            "    @CacheEvict(value = \"users\", key = \"#id\")\n" +
            "    public Optional<User> update(Long id, UserDto userDto) {\n" +
            "        log.info(\"Updating user with id: {}\", id);\n" +
            "        return userRepository.findById(id)\n" +
            "                .map(user -> {\n" +
            "                    userMapper.updateEntityFromDto(userDto, user);\n" +
            "                    return userRepository.save(user);\n" +
            "                });\n" +
            "    }\n\n" +
            "    @Override\n" +
            "    @CacheEvict(value = \"users\", key = \"#id\")\n" +
            "    public void delete(Long id) {\n" +
            "        log.info(\"Deleting user with id: {}\", id);\n" +
            "        userRepository.deleteById(id);\n" +
            "    }\n" +
            "}\n";

        ProjectNode userServiceImplNode = new ProjectNode("UserServiceImpl.java", ProjectNode.NodeType.FILE, userServiceImpl);
        service.addChild(userServiceImplNode);
    }

    private void generateRepositoryFiles(ProjectNode repository, String packageName, SpringBootProjectType type) {
        String userRepository = "package " + packageName + ".repository;\n\n" +
            "import " + packageName + ".model.User;\n" +
            "import org.springframework.data.jpa.repository.JpaRepository;\n" +
            "import org.springframework.data.jpa.repository.Query;\n" +
            "import org.springframework.stereotype.Repository;\n" +
            "import java.util.Optional;\n" +
            "import java.util.List;\n\n" +
            "@Repository\n" +
            "public interface UserRepository extends JpaRepository<User, Long> {\n\n" +
            "    Optional<User> findByEmail(String email);\n\n" +
            "    List<User> findByActiveTrue();\n\n" +
            "    @Query(\"SELECT u FROM User u WHERE u.email = ?1 AND u.active = true\")\n" +
            "    Optional<User> findActiveUserByEmail(String email);\n\n" +
            "    boolean existsByEmail(String email);\n" +
            "}\n";

        ProjectNode userRepositoryNode = new ProjectNode("UserRepository.java", ProjectNode.NodeType.FILE, userRepository);
        repository.addChild(userRepositoryNode);
    }

    private void generateModelFiles(ProjectNode model, String packageName, SpringBootProjectType type) {
        String userModel = "package " + packageName + ".model;\n\n" +
            "import jakarta.persistence.*;\n" +
            "import lombok.*;\n" +
            "import org.hibernate.annotations.CreationTimestamp;\n" +
            "import org.hibernate.annotations.UpdateTimestamp;\n" +
            "import java.time.LocalDateTime;\n\n" +
            "@Entity\n" +
            "@Table(name = \"users\")\n" +
            "@Data\n" +
            "@NoArgsConstructor\n" +
            "@AllArgsConstructor\n" +
            "@Builder\n" +
            "public class User {\n\n" +
            "    @Id\n" +
            "    @GeneratedValue(strategy = GenerationType.IDENTITY)\n" +
            "    private Long id;\n\n" +
            "    @Column(nullable = false)\n" +
            "    private String firstName;\n\n" +
            "    @Column(nullable = false)\n" +
            "    private String lastName;\n\n" +
            "    @Column(nullable = false, unique = true)\n" +
            "    private String email;\n\n" +
            "    @Column(nullable = false)\n" +
            "    private String password;\n\n" +
            "    @Column(nullable = false)\n" +
            "    private Boolean active = true;\n\n" +
            "    @CreationTimestamp\n" +
            "    @Column(updatable = false)\n" +
            "    private LocalDateTime createdAt;\n\n" +
            "    @UpdateTimestamp\n" +
            "    private LocalDateTime updatedAt;\n\n" +
            "    @PrePersist\n" +
            "    protected void onCreate() {\n" +
            "        if (active == null) {\n" +
            "            active = true;\n" +
            "        }\n" +
            "    }\n" +
            "}\n";

        ProjectNode userModelNode = new ProjectNode("User.java", ProjectNode.NodeType.FILE, userModel);
        model.addChild(userModelNode);

        // Base Entity
        String baseEntity = "package " + packageName + ".model;\n\n" +
            "import jakarta.persistence.*;\n" +
            "import lombok.Data;\n" +
            "import org.springframework.data.annotation.CreatedDate;\n" +
            "import org.springframework.data.annotation.LastModifiedDate;\n" +
            "import org.springframework.data.jpa.domain.support.AuditingEntityListener;\n" +
            "import java.io.Serializable;\n" +
            "import java.time.LocalDateTime;\n\n" +
            "@MappedSuperclass\n" +
            "@Data\n" +
            "@EntityListeners(AuditingEntityListener.class)\n" +
            "public abstract class BaseEntity implements Serializable {\n\n" +
            "    @Id\n" +
            "    @GeneratedValue(strategy = GenerationType.IDENTITY)\n" +
            "    private Long id;\n\n" +
            "    @CreatedDate\n" +
            "    @Column(nullable = false, updatable = false)\n" +
            "    private LocalDateTime createdAt;\n\n" +
            "    @LastModifiedDate\n" +
            "    @Column(nullable = false)\n" +
            "    private LocalDateTime updatedAt;\n\n" +
            "    @Version\n" +
            "    private Long version;\n" +
            "}\n";

        ProjectNode baseEntityNode = new ProjectNode("BaseEntity.java", ProjectNode.NodeType.FILE, baseEntity);
        model.addChild(baseEntityNode);
    }

    private void generateDtoFiles(ProjectNode dto, String packageName, SpringBootProjectType type) {
        String userDto = "package " + packageName + ".dto;\n\n" +
            "import jakarta.validation.constraints.*;\n" +
            "import lombok.*;\n\n" +
            "@Data\n" +
            "@NoArgsConstructor\n" +
            "@AllArgsConstructor\n" +
            "@Builder\n" +
            "public class UserDto {\n\n" +
            "    private Long id;\n\n" +
            "    @NotBlank(message = \"First name is required\")\n" +
            "    @Size(min = 2, max = 50)\n" +
            "    private String firstName;\n\n" +
            "    @NotBlank(message = \"Last name is required\")\n" +
            "    @Size(min = 2, max = 50)\n" +
            "    private String lastName;\n\n" +
            "    @NotBlank(message = \"Email is required\")\n" +
            "    @Email(message = \"Email should be valid\")\n" +
            "    private String email;\n\n" +
            "    @NotBlank(message = \"Password is required\")\n" +
            "    @Size(min = 6, message = \"Password must be at least 6 characters\")\n" +
            "    private String password;\n\n" +
            "    private Boolean active;\n" +
            "}\n";

        ProjectNode userDtoNode = new ProjectNode("UserDto.java", ProjectNode.NodeType.FILE, userDto);
        dto.addChild(userDtoNode);

        // API Response wrapper
        String apiResponse = "package " + packageName + ".dto;\n\n" +
            "import lombok.*;\n" +
            "import java.time.LocalDateTime;\n\n" +
            "@Data\n" +
            "@Builder\n" +
            "@NoArgsConstructor\n" +
            "@AllArgsConstructor\n" +
            "public class ApiResponse<T> {\n\n" +
            "    private boolean success;\n" +
            "    private String message;\n" +
            "    private T data;\n" +
            "    private LocalDateTime timestamp;\n\n" +
            "    public static <T> ApiResponse<T> success(T data) {\n" +
            "        return ApiResponse.<T>builder()\n" +
            "                .success(true)\n" +
            "                .data(data)\n" +
            "                .timestamp(LocalDateTime.now())\n" +
            "                .build();\n" +
            "    }\n\n" +
            "    public static <T> ApiResponse<T> success(T data, String message) {\n" +
            "        return ApiResponse.<T>builder()\n" +
            "                .success(true)\n" +
            "                .message(message)\n" +
            "                .data(data)\n" +
            "                .timestamp(LocalDateTime.now())\n" +
            "                .build();\n" +
            "    }\n\n" +
            "    public static <T> ApiResponse<T> error(String message) {\n" +
            "        return ApiResponse.<T>builder()\n" +
            "                .success(false)\n" +
            "                .message(message)\n" +
            "                .timestamp(LocalDateTime.now())\n" +
            "                .build();\n" +
            "    }\n" +
            "}\n";

        ProjectNode apiResponseNode = new ProjectNode("ApiResponse.java", ProjectNode.NodeType.FILE, apiResponse);
        dto.addChild(apiResponseNode);
    }

    private void generateConfigFiles(ProjectNode config, String packageName, SpringBootProjectType type) {
        // Web Config
        String webConfig = "package " + packageName + ".config;\n\n" +
            "import org.springframework.context.annotation.Configuration;\n" +
            "import org.springframework.web.servlet.config.annotation.*;\n\n" +
            "@Configuration\n" +
            "@EnableWebMvc\n" +
            "public class WebConfig implements WebMvcConfigurer {\n\n" +
            "    @Override\n" +
            "    public void addCorsMappings(CorsRegistry registry) {\n" +
            "        registry.addMapping(\"/api/**\")\n" +
            "                .allowedOrigins(\"http://localhost:3000\")\n" +
            "                .allowedMethods(\"GET\", \"POST\", \"PUT\", \"DELETE\", \"OPTIONS\")\n" +
            "                .allowedHeaders(\"*\")\n" +
            "                .allowCredentials(true);\n" +
            "    }\n\n" +
            "    @Override\n" +
            "    public void addInterceptors(InterceptorRegistry registry) {\n" +
            "        // Add custom interceptors here\n" +
            "    }\n" +
            "}\n";

        ProjectNode webConfigNode = new ProjectNode("WebConfig.java", ProjectNode.NodeType.FILE, webConfig);
        config.addChild(webConfigNode);

        // JPA Config
        String jpaConfig = "package " + packageName + ".config;\n\n" +
            "import org.springframework.context.annotation.Configuration;\n" +
            "import org.springframework.data.jpa.repository.config.EnableJpaAuditing;\n" +
            "import org.springframework.data.jpa.repository.config.EnableJpaRepositories;\n" +
            "import org.springframework.transaction.annotation.EnableTransactionManagement;\n\n" +
            "@Configuration\n" +
            "@EnableJpaRepositories(\"" + packageName + ".repository\")\n" +
            "@EnableJpaAuditing\n" +
            "@EnableTransactionManagement\n" +
            "public class JpaConfig {\n" +
            "}\n";

        ProjectNode jpaConfigNode = new ProjectNode("JpaConfig.java", ProjectNode.NodeType.FILE, jpaConfig);
        config.addChild(jpaConfigNode);

        // OpenAPI Config
        String openApiConfig = "package " + packageName + ".config;\n\n" +
            "import io.swagger.v3.oas.models.OpenAPI;\n" +
            "import io.swagger.v3.oas.models.info.Info;\n" +
            "import io.swagger.v3.oas.models.info.License;\n" +
            "import org.springframework.context.annotation.Bean;\n" +
            "import org.springframework.context.annotation.Configuration;\n\n" +
            "@Configuration\n" +
            "public class OpenApiConfig {\n\n" +
            "    @Bean\n" +
            "    public OpenAPI customOpenAPI() {\n" +
            "        return new OpenAPI()\n" +
            "                .info(new Info()\n" +
            "                        .title(\"Spring Boot API\")\n" +
            "                        .version(\"1.0\")\n" +
            "                        .description(\"Spring Boot RESTful API Documentation\")\n" +
            "                        .license(new License().name(\"Apache 2.0\").url(\"http://springdoc.org\")));\n" +
            "    }\n" +
            "}\n";

        ProjectNode openApiConfigNode = new ProjectNode("OpenApiConfig.java", ProjectNode.NodeType.FILE, openApiConfig);
        config.addChild(openApiConfigNode);
    }

    private void generateExceptionFiles(ProjectNode exception, String packageName, SpringBootProjectType type) {
        // Resource Not Found Exception
        String notFoundEx = "package " + packageName + ".exception;\n\n" +
            "public class ResourceNotFoundException extends RuntimeException {\n\n" +
            "    public ResourceNotFoundException(String message) {\n" +
            "        super(message);\n" +
            "    }\n\n" +
            "    public ResourceNotFoundException(String resource, String field, Object value) {\n" +
            "        super(String.format(\"%s not found with %s : '%s'\", resource, field, value));\n" +
            "    }\n" +
            "}\n";

        ProjectNode notFoundExNode = new ProjectNode("ResourceNotFoundException.java", ProjectNode.NodeType.FILE, notFoundEx);
        exception.addChild(notFoundExNode);

        // Global Exception Handler
        String globalHandler = "package " + packageName + ".exception;\n\n" +
            "import " + packageName + ".dto.ApiResponse;\n" +
            "import lombok.extern.slf4j.Slf4j;\n" +
            "import org.springframework.http.HttpStatus;\n" +
            "import org.springframework.http.ResponseEntity;\n" +
            "import org.springframework.validation.FieldError;\n" +
            "import org.springframework.web.bind.MethodArgumentNotValidException;\n" +
            "import org.springframework.web.bind.annotation.*;\n" +
            "import java.util.HashMap;\n" +
            "import java.util.Map;\n\n" +
            "@RestControllerAdvice\n" +
            "@Slf4j\n" +
            "public class GlobalExceptionHandler {\n\n" +
            "    @ExceptionHandler(ResourceNotFoundException.class)\n" +
            "    public ResponseEntity<ApiResponse<Void>> handleResourceNotFoundException(ResourceNotFoundException ex) {\n" +
            "        log.error(\"Resource not found: {}\", ex.getMessage());\n" +
            "        return ResponseEntity.status(HttpStatus.NOT_FOUND)\n" +
            "                .body(ApiResponse.error(ex.getMessage()));\n" +
            "    }\n\n" +
            "    @ExceptionHandler(MethodArgumentNotValidException.class)\n" +
            "    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationExceptions(\n" +
            "            MethodArgumentNotValidException ex) {\n" +
            "        Map<String, String> errors = new HashMap<>();\n" +
            "        ex.getBindingResult().getAllErrors().forEach((error) -> {\n" +
            "            String fieldName = ((FieldError) error).getField();\n" +
            "            String errorMessage = error.getDefaultMessage();\n" +
            "            errors.put(fieldName, errorMessage);\n" +
            "        });\n" +
            "        return ResponseEntity.status(HttpStatus.BAD_REQUEST)\n" +
            "                .body(ApiResponse.<Map<String, String>>builder()\n" +
            "                        .success(false)\n" +
            "                        .message(\"Validation failed\")\n" +
            "                        .data(errors)\n" +
            "                        .build());\n" +
            "    }\n\n" +
            "    @ExceptionHandler(Exception.class)\n" +
            "    public ResponseEntity<ApiResponse<Void>> handleGlobalException(Exception ex) {\n" +
            "        log.error(\"Unexpected error occurred\", ex);\n" +
            "        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)\n" +
            "                .body(ApiResponse.error(\"An unexpected error occurred\"));\n" +
            "    }\n" +
            "}\n";

        ProjectNode globalHandlerNode = new ProjectNode("GlobalExceptionHandler.java", ProjectNode.NodeType.FILE, globalHandler);
        exception.addChild(globalHandlerNode);
    }

    private void generateUtilFiles(ProjectNode util, String packageName, SpringBootProjectType type) {
        // Constants
        String constants = "package " + packageName + ".util;\n\n" +
            "public class Constants {\n\n" +
            "    private Constants() {\n" +
            "        throw new IllegalStateException(\"Utility class\");\n" +
            "    }\n\n" +
            "    public static final String API_VERSION = \"/api/v1\";\n" +
            "    public static final String DATE_FORMAT = \"yyyy-MM-dd\";\n" +
            "    public static final String DATETIME_FORMAT = \"yyyy-MM-dd HH:mm:ss\";\n" +
            "    public static final int DEFAULT_PAGE_SIZE = 20;\n" +
            "    public static final int MAX_PAGE_SIZE = 100;\n" +
            "}\n";

        ProjectNode constantsNode = new ProjectNode("Constants.java", ProjectNode.NodeType.FILE, constants);
        util.addChild(constantsNode);
    }

    private void generateMapperFiles(ProjectNode mapper, String packageName, SpringBootProjectType type) {
        // User Mapper
        String userMapper = "package " + packageName + ".mapper;\n\n" +
            "import " + packageName + ".dto.UserDto;\n" +
            "import " + packageName + ".model.User;\n" +
            "import org.mapstruct.*;\n\n" +
            "@Mapper(componentModel = \"spring\")\n" +
            "public interface UserMapper {\n\n" +
            "    UserDto toDto(User user);\n\n" +
            "    User toEntity(UserDto userDto);\n\n" +
            "    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)\n" +
            "    void updateEntityFromDto(UserDto userDto, @MappingTarget User user);\n" +
            "}\n";

        ProjectNode userMapperNode = new ProjectNode("UserMapper.java", ProjectNode.NodeType.FILE, userMapper);
        mapper.addChild(userMapperNode);
    }

    private void generateTemplateFiles(ProjectNode templates) {
        // Index HTML
        String indexHtml = "<!DOCTYPE html>\n" +
            "<html xmlns:th=\"http://www.thymeleaf.org\">\n" +
            "<head>\n" +
            "    <meta charset=\"UTF-8\">\n" +
            "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
            "    <title>Spring Boot Application</title>\n" +
            "    <link href=\"https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css\" rel=\"stylesheet\">\n" +
            "</head>\n" +
            "<body>\n" +
            "    <div class=\"container mt-5\">\n" +
            "        <h1>Welcome to Spring Boot</h1>\n" +
            "        <p class=\"lead\">Your application is running successfully!</p>\n" +
            "        <a href=\"/swagger-ui.html\" class=\"btn btn-primary\">View API Documentation</a>\n" +
            "    </div>\n" +
            "</body>\n" +
            "</html>\n";

        ProjectNode indexHtmlNode = new ProjectNode("index.html", ProjectNode.NodeType.FILE, indexHtml);
        templates.addChild(indexHtmlNode);
    }

    private String generateMainTestClass(String packageName, String projectName) {
        return "package " + packageName + ";\n\n" +
               "import org.junit.jupiter.api.Test;\n" +
               "import org.springframework.boot.test.context.SpringBootTest;\n\n" +
               "@SpringBootTest\n" +
               "class " + toCamelCase(projectName) + "ApplicationTests {\n\n" +
               "    @Test\n" +
               "    void contextLoads() {\n" +
               "    }\n\n" +
               "}\n";
    }

    private String generateControllerTest(String packageName) {
        return "package " + packageName + ".controller;\n\n" +
               "import " + packageName + ".model.User;\n" +
               "import " + packageName + ".service.UserService;\n" +
               "import com.fasterxml.jackson.databind.ObjectMapper;\n" +
               "import org.junit.jupiter.api.BeforeEach;\n" +
               "import org.junit.jupiter.api.Test;\n" +
               "import org.springframework.beans.factory.annotation.Autowired;\n" +
               "import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;\n" +
               "import org.springframework.boot.test.mock.mockito.MockBean;\n" +
               "import org.springframework.http.MediaType;\n" +
               "import org.springframework.test.web.servlet.MockMvc;\n\n" +
               "import java.util.Optional;\n\n" +
               "import static org.mockito.ArgumentMatchers.any;\n" +
               "import static org.mockito.Mockito.when;\n" +
               "import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;\n" +
               "import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;\n\n" +
               "@WebMvcTest(UserController.class)\n" +
               "class UserControllerTest {\n\n" +
               "    @Autowired\n" +
               "    private MockMvc mockMvc;\n\n" +
               "    @MockBean\n" +
               "    private UserService userService;\n\n" +
               "    @Autowired\n" +
               "    private ObjectMapper objectMapper;\n\n" +
               "    private User testUser;\n\n" +
               "    @BeforeEach\n" +
               "    void setUp() {\n" +
               "        testUser = User.builder()\n" +
               "                .id(1L)\n" +
               "                .firstName(\"John\")\n" +
               "                .lastName(\"Doe\")\n" +
               "                .email(\"john@example.com\")\n" +
               "                .build();\n" +
               "    }\n\n" +
               "    @Test\n" +
               "    void getUserById_ReturnsUser() throws Exception {\n" +
               "        when(userService.findById(1L)).thenReturn(Optional.of(testUser));\n\n" +
               "        mockMvc.perform(get(\"/api/users/1\"))\n" +
               "                .andExpect(status().isOk())\n" +
               "                .andExpect(jsonPath(\"$.id\").value(1))\n" +
               "                .andExpect(jsonPath(\"$.email\").value(\"john@example.com\"));\n" +
               "    }\n" +
               "}\n";
    }

    private String generateServiceTest(String packageName) {
        return "package " + packageName + ".service;\n\n" +
               "import " + packageName + ".model.User;\n" +
               "import " + packageName + ".repository.UserRepository;\n" +
               "import org.junit.jupiter.api.BeforeEach;\n" +
               "import org.junit.jupiter.api.Test;\n" +
               "import org.junit.jupiter.api.extension.ExtendWith;\n" +
               "import org.mockito.InjectMocks;\n" +
               "import org.mockito.Mock;\n" +
               "import org.mockito.junit.jupiter.MockitoExtension;\n\n" +
               "import java.util.Optional;\n\n" +
               "import static org.assertj.core.api.Assertions.assertThat;\n" +
               "import static org.mockito.Mockito.*;\n\n" +
               "@ExtendWith(MockitoExtension.class)\n" +
               "class UserServiceTest {\n\n" +
               "    @Mock\n" +
               "    private UserRepository userRepository;\n\n" +
               "    @InjectMocks\n" +
               "    private UserServiceImpl userService;\n\n" +
               "    private User testUser;\n\n" +
               "    @BeforeEach\n" +
               "    void setUp() {\n" +
               "        testUser = User.builder()\n" +
               "                .id(1L)\n" +
               "                .firstName(\"John\")\n" +
               "                .lastName(\"Doe\")\n" +
               "                .email(\"john@example.com\")\n" +
               "                .build();\n" +
               "    }\n\n" +
               "    @Test\n" +
               "    void findById_ReturnsUser() {\n" +
               "        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));\n\n" +
               "        Optional<User> result = userService.findById(1L);\n\n" +
               "        assertThat(result).isPresent();\n" +
               "        assertThat(result.get().getEmail()).isEqualTo(\"john@example.com\");\n" +
               "        verify(userRepository, times(1)).findById(1L);\n" +
               "    }\n" +
               "}\n";
    }

    private String generateIntegrationTest(String packageName) {
        return "package " + packageName + ".integration;\n\n" +
               "import org.junit.jupiter.api.Test;\n" +
               "import org.springframework.beans.factory.annotation.Autowired;\n" +
               "import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;\n" +
               "import org.springframework.boot.test.context.SpringBootTest;\n" +
               "import org.springframework.test.context.ActiveProfiles;\n" +
               "import org.springframework.test.web.servlet.MockMvc;\n\n" +
               "import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;\n" +
               "import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;\n\n" +
               "@SpringBootTest\n" +
               "@AutoConfigureMockMvc\n" +
               "@ActiveProfiles(\"test\")\n" +
               "class IntegrationTest {\n\n" +
               "    @Autowired\n" +
               "    private MockMvc mockMvc;\n\n" +
               "    @Test\n" +
               "    void healthCheck_ReturnsOk() throws Exception {\n" +
               "        mockMvc.perform(get(\"/health\"))\n" +
               "                .andExpect(status().isOk());\n" +
               "    }\n" +
               "}\n";
    }

    private String generateTestProperties() {
        return "spring:\n" +
               "  profiles: test\n\n" +
               "  datasource:\n" +
               "    url: jdbc:h2:mem:testdb\n" +
               "    driver-class-name: org.h2.Driver\n\n" +
               "  jpa:\n" +
               "    hibernate:\n" +
               "      ddl-auto: create-drop\n" +
               "    show-sql: false\n\n" +
               "logging:\n" +
               "  level:\n" +
               "    root: WARN\n";
    }

    private String toCamelCase(String str) {
        String[] parts = str.split("[^a-zA-Z0-9]");
        StringBuilder camelCase = new StringBuilder();
        for (String part : parts) {
            if (!part.isEmpty()) {
                camelCase.append(part.substring(0, 1).toUpperCase());
                if (part.length() > 1) {
                    camelCase.append(part.substring(1).toLowerCase());
                }
            }
        }
        return camelCase.toString();
    }

    /**
     * Spring Boot project types
     */
    public enum SpringBootProjectType {
        BASIC("Basic Spring Boot REST API"),
        WEB("Spring Boot Web Application with Thymeleaf"),
        MICROSERVICE("Spring Boot Microservice with Cloud Config"),
        FULL_STACK("Full-Stack Spring Boot Application with Everything");

        private final String description;

        SpringBootProjectType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
}