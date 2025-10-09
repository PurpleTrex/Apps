package com.structurecreation.service.generator;

import com.structurecreation.model.ProjectNode;
import com.structurecreation.service.DependencyResolverService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DjangoProjectGenerator {

    @Autowired
    private DependencyResolverService dependencyResolver;

    public enum DjangoProjectType {
        REST_API("REST API with Django REST Framework"),
        FULL_STACK("Full-stack web application"),
        MICROSERVICE("Microservice with async support"),
        CMS("Content Management System"),
        ECOMMERCE("E-commerce platform"),
        ANALYTICS("Analytics dashboard"),
        MACHINE_LEARNING("ML-powered application");

        private final String description;

        DjangoProjectType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    public ProjectNode generateDjangoProject(String projectName, DjangoProjectType type) {
        ProjectNode root = new ProjectNode(projectName, true, null);

        // Create project structure
        createProjectStructure(root, projectName, type);

        // Add configuration files
        addConfigurationFiles(root, projectName, type);

        // Add Docker support
        addDockerSupport(root, projectName, type);

        // Add CI/CD
        addCICDPipeline(root, projectName);

        return root;
    }

    private void createProjectStructure(ProjectNode root, String projectName, DjangoProjectType type) {
        // Main Django project directory
        ProjectNode projectDir = new ProjectNode(projectName, true, root);
        root.addChild(projectDir);

        // settings package
        ProjectNode settingsDir = new ProjectNode("settings", true, projectDir);
        projectDir.addChild(settingsDir);

        // Settings files
        ProjectNode initSettings = new ProjectNode("__init__.py", false, settingsDir);
        initSettings.setContent("from .base import *\n\ntry:\n    from .local import *\nexcept ImportError:\n    pass");
        settingsDir.addChild(initSettings);

        ProjectNode baseSettings = new ProjectNode("base.py", false, settingsDir);
        baseSettings.setContent(generateBaseSettings(projectName, type));
        settingsDir.addChild(baseSettings);

        ProjectNode localSettings = new ProjectNode("local.py", false, settingsDir);
        localSettings.setContent(generateLocalSettings());
        settingsDir.addChild(localSettings);

        ProjectNode prodSettings = new ProjectNode("production.py", false, settingsDir);
        prodSettings.setContent(generateProductionSettings());
        settingsDir.addChild(prodSettings);

        ProjectNode testSettings = new ProjectNode("test.py", false, settingsDir);
        testSettings.setContent(generateTestSettings());
        settingsDir.addChild(testSettings);

        // Main project files
        ProjectNode urls = new ProjectNode("urls.py", false, projectDir);
        urls.setContent(generateMainUrls(type));
        projectDir.addChild(urls);

        ProjectNode wsgi = new ProjectNode("wsgi.py", false, projectDir);
        wsgi.setContent(generateWSGI(projectName));
        projectDir.addChild(wsgi);

        ProjectNode asgi = new ProjectNode("asgi.py", false, projectDir);
        asgi.setContent(generateASGI(projectName));
        projectDir.addChild(asgi);

        ProjectNode celeryFile = new ProjectNode("celery.py", false, projectDir);
        celeryFile.setContent(generateCeleryConfig(projectName));
        projectDir.addChild(celeryFile);

        // Apps directory
        ProjectNode appsDir = new ProjectNode("apps", true, root);
        root.addChild(appsDir);

        ProjectNode appsInit = new ProjectNode("__init__.py", false, appsDir);
        appsInit.setContent("");
        appsDir.addChild(appsInit);

        // Create apps based on project type
        switch (type) {
            case REST_API:
                createAPIApp(appsDir);
                createAuthApp(appsDir);
                break;
            case FULL_STACK:
                createCoreApp(appsDir);
                createAuthApp(appsDir);
                createPagesApp(appsDir);
                break;
            case MICROSERVICE:
                createServiceApp(appsDir);
                createHealthApp(appsDir);
                break;
            case CMS:
                createContentApp(appsDir);
                createMediaApp(appsDir);
                createUsersApp(appsDir);
                break;
            case ECOMMERCE:
                createProductsApp(appsDir);
                createCartApp(appsDir);
                createOrdersApp(appsDir);
                createPaymentsApp(appsDir);
                break;
            case ANALYTICS:
                createDashboardApp(appsDir);
                createReportsApp(appsDir);
                createDataApp(appsDir);
                break;
            case MACHINE_LEARNING:
                createMLApp(appsDir);
                createPredictionsApp(appsDir);
                createDatasetApp(appsDir);
                break;
        }

        // Static files
        ProjectNode staticDir = new ProjectNode("static", true, root);
        root.addChild(staticDir);

        ProjectNode cssDir = new ProjectNode("css", true, staticDir);
        staticDir.addChild(cssDir);
        ProjectNode mainCss = new ProjectNode("main.css", false, cssDir);
        mainCss.setContent(generateMainCSS());
        cssDir.addChild(mainCss);

        ProjectNode jsDir = new ProjectNode("js", true, staticDir);
        staticDir.addChild(jsDir);
        ProjectNode mainJs = new ProjectNode("main.js", false, jsDir);
        mainJs.setContent(generateMainJS());
        jsDir.addChild(mainJs);

        ProjectNode imgDir = new ProjectNode("images", true, staticDir);
        staticDir.addChild(imgDir);

        // Media files
        ProjectNode mediaDir = new ProjectNode("media", true, root);
        root.addChild(mediaDir);

        // Templates
        ProjectNode templatesDir = new ProjectNode("templates", true, root);
        root.addChild(templatesDir);

        ProjectNode baseTemplate = new ProjectNode("base.html", false, templatesDir);
        baseTemplate.setContent(generateBaseTemplate());
        templatesDir.addChild(baseTemplate);

        ProjectNode indexTemplate = new ProjectNode("index.html", false, templatesDir);
        indexTemplate.setContent(generateIndexTemplate());
        templatesDir.addChild(indexTemplate);

        // Tests directory
        ProjectNode testsDir = new ProjectNode("tests", true, root);
        root.addChild(testsDir);

        ProjectNode testsInit = new ProjectNode("__init__.py", false, testsDir);
        testsInit.setContent("");
        testsDir.addChild(testsInit);

        ProjectNode testBase = new ProjectNode("test_base.py", false, testsDir);
        testBase.setContent(generateTestBase());
        testsDir.addChild(testBase);

        ProjectNode conftest = new ProjectNode("conftest.py", false, testsDir);
        conftest.setContent(generateConftest());
        testsDir.addChild(conftest);

        // Utils directory
        ProjectNode utilsDir = new ProjectNode("utils", true, root);
        root.addChild(utilsDir);

        ProjectNode utilsInit = new ProjectNode("__init__.py", false, utilsDir);
        utilsInit.setContent("");
        utilsDir.addChild(utilsInit);

        ProjectNode validators = new ProjectNode("validators.py", false, utilsDir);
        validators.setContent(generateValidators());
        utilsDir.addChild(validators);

        ProjectNode permissions = new ProjectNode("permissions.py", false, utilsDir);
        permissions.setContent(generatePermissions());
        utilsDir.addChild(permissions);

        ProjectNode mixins = new ProjectNode("mixins.py", false, utilsDir);
        mixins.setContent(generateMixins());
        utilsDir.addChild(mixins);

        // Locale directory for i18n
        ProjectNode localeDir = new ProjectNode("locale", true, root);
        root.addChild(localeDir);

        // Logs directory
        ProjectNode logsDir = new ProjectNode("logs", true, root);
        root.addChild(logsDir);

        // Scripts directory
        ProjectNode scriptsDir = new ProjectNode("scripts", true, root);
        root.addChild(scriptsDir);

        ProjectNode runDev = new ProjectNode("run_dev.sh", false, scriptsDir);
        runDev.setContent(generateRunDevScript());
        scriptsDir.addChild(runDev);

        ProjectNode migrate = new ProjectNode("migrate.sh", false, scriptsDir);
        migrate.setContent(generateMigrateScript());
        scriptsDir.addChild(migrate);

        // Documentation
        ProjectNode docsDir = new ProjectNode("docs", true, root);
        root.addChild(docsDir);

        ProjectNode apiDocs = new ProjectNode("api.md", false, docsDir);
        apiDocs.setContent(generateAPIDocs(type));
        docsDir.addChild(apiDocs);
    }

    private void createAPIApp(ProjectNode appsDir) {
        ProjectNode apiApp = new ProjectNode("api", true, appsDir);
        appsDir.addChild(apiApp);

        ProjectNode init = new ProjectNode("__init__.py", false, apiApp);
        init.setContent("");
        apiApp.addChild(init);

        ProjectNode v1Dir = new ProjectNode("v1", true, apiApp);
        apiApp.addChild(v1Dir);

        ProjectNode v1Init = new ProjectNode("__init__.py", false, v1Dir);
        v1Init.setContent("");
        v1Dir.addChild(v1Init);

        ProjectNode serializers = new ProjectNode("serializers.py", false, v1Dir);
        serializers.setContent(generateAPISerializers());
        v1Dir.addChild(serializers);

        ProjectNode views = new ProjectNode("views.py", false, v1Dir);
        views.setContent(generateAPIViews());
        v1Dir.addChild(views);

        ProjectNode urls = new ProjectNode("urls.py", false, v1Dir);
        urls.setContent(generateAPIUrls());
        v1Dir.addChild(urls);

        ProjectNode filters = new ProjectNode("filters.py", false, v1Dir);
        filters.setContent(generateAPIFilters());
        v1Dir.addChild(filters);

        ProjectNode pagination = new ProjectNode("pagination.py", false, v1Dir);
        pagination.setContent(generateAPIPagination());
        v1Dir.addChild(pagination);
    }

    private void createAuthApp(ProjectNode appsDir) {
        ProjectNode authApp = new ProjectNode("authentication", true, appsDir);
        appsDir.addChild(authApp);

        ProjectNode init = new ProjectNode("__init__.py", false, authApp);
        init.setContent("");
        authApp.addChild(init);

        ProjectNode models = new ProjectNode("models.py", false, authApp);
        models.setContent(generateAuthModels());
        authApp.addChild(models);

        ProjectNode views = new ProjectNode("views.py", false, authApp);
        views.setContent(generateAuthViews());
        authApp.addChild(views);

        ProjectNode serializers = new ProjectNode("serializers.py", false, authApp);
        serializers.setContent(generateAuthSerializers());
        authApp.addChild(serializers);

        ProjectNode urls = new ProjectNode("urls.py", false, authApp);
        urls.setContent(generateAuthUrls());
        authApp.addChild(urls);

        ProjectNode backends = new ProjectNode("backends.py", false, authApp);
        backends.setContent(generateAuthBackends());
        authApp.addChild(backends);

        ProjectNode tokens = new ProjectNode("tokens.py", false, authApp);
        tokens.setContent(generateTokens());
        authApp.addChild(tokens);
    }

    private void createCoreApp(ProjectNode appsDir) {
        ProjectNode coreApp = new ProjectNode("core", true, appsDir);
        appsDir.addChild(coreApp);

        ProjectNode init = new ProjectNode("__init__.py", false, coreApp);
        init.setContent("");
        coreApp.addChild(init);

        ProjectNode models = new ProjectNode("models.py", false, coreApp);
        models.setContent(generateCoreModels());
        coreApp.addChild(models);

        ProjectNode views = new ProjectNode("views.py", false, coreApp);
        views.setContent(generateCoreViews());
        coreApp.addChild(views);

        ProjectNode forms = new ProjectNode("forms.py", false, coreApp);
        forms.setContent(generateCoreForms());
        coreApp.addChild(forms);

        ProjectNode admin = new ProjectNode("admin.py", false, coreApp);
        admin.setContent(generateCoreAdmin());
        coreApp.addChild(admin);

        ProjectNode urls = new ProjectNode("urls.py", false, coreApp);
        urls.setContent(generateCoreUrls());
        coreApp.addChild(urls);
    }

    private void createPagesApp(ProjectNode appsDir) {
        ProjectNode pagesApp = new ProjectNode("pages", true, appsDir);
        appsDir.addChild(pagesApp);

        ProjectNode init = new ProjectNode("__init__.py", false, pagesApp);
        init.setContent("");
        pagesApp.addChild(init);

        ProjectNode views = new ProjectNode("views.py", false, pagesApp);
        views.setContent(generatePagesViews());
        pagesApp.addChild(views);

        ProjectNode urls = new ProjectNode("urls.py", false, pagesApp);
        urls.setContent(generatePagesUrls());
        pagesApp.addChild(urls);
    }

    private void createServiceApp(ProjectNode appsDir) {
        ProjectNode serviceApp = new ProjectNode("service", true, appsDir);
        appsDir.addChild(serviceApp);

        ProjectNode init = new ProjectNode("__init__.py", false, serviceApp);
        init.setContent("");
        serviceApp.addChild(init);

        ProjectNode handlers = new ProjectNode("handlers.py", false, serviceApp);
        handlers.setContent(generateServiceHandlers());
        serviceApp.addChild(handlers);

        ProjectNode tasks = new ProjectNode("tasks.py", false, serviceApp);
        tasks.setContent(generateServiceTasks());
        serviceApp.addChild(tasks);

        ProjectNode consumers = new ProjectNode("consumers.py", false, serviceApp);
        consumers.setContent(generateServiceConsumers());
        serviceApp.addChild(consumers);
    }

    private void createHealthApp(ProjectNode appsDir) {
        ProjectNode healthApp = new ProjectNode("health", true, appsDir);
        appsDir.addChild(healthApp);

        ProjectNode init = new ProjectNode("__init__.py", false, healthApp);
        init.setContent("");
        healthApp.addChild(init);

        ProjectNode views = new ProjectNode("views.py", false, healthApp);
        views.setContent(generateHealthViews());
        healthApp.addChild(views);

        ProjectNode urls = new ProjectNode("urls.py", false, healthApp);
        urls.setContent(generateHealthUrls());
        healthApp.addChild(urls);
    }

    private void createContentApp(ProjectNode appsDir) {
        ProjectNode contentApp = new ProjectNode("content", true, appsDir);
        appsDir.addChild(contentApp);

        ProjectNode init = new ProjectNode("__init__.py", false, contentApp);
        init.setContent("");
        contentApp.addChild(init);

        ProjectNode models = new ProjectNode("models.py", false, contentApp);
        models.setContent(generateContentModels());
        contentApp.addChild(models);

        ProjectNode admin = new ProjectNode("admin.py", false, contentApp);
        admin.setContent(generateContentAdmin());
        contentApp.addChild(admin);
    }

    private void createMediaApp(ProjectNode appsDir) {
        ProjectNode mediaApp = new ProjectNode("media_manager", true, appsDir);
        appsDir.addChild(mediaApp);

        ProjectNode init = new ProjectNode("__init__.py", false, mediaApp);
        init.setContent("");
        mediaApp.addChild(init);

        ProjectNode models = new ProjectNode("models.py", false, mediaApp);
        models.setContent(generateMediaModels());
        mediaApp.addChild(models);

        ProjectNode processors = new ProjectNode("processors.py", false, mediaApp);
        processors.setContent(generateMediaProcessors());
        mediaApp.addChild(processors);
    }

    private void createUsersApp(ProjectNode appsDir) {
        ProjectNode usersApp = new ProjectNode("users", true, appsDir);
        appsDir.addChild(usersApp);

        ProjectNode init = new ProjectNode("__init__.py", false, usersApp);
        init.setContent("");
        usersApp.addChild(init);

        ProjectNode models = new ProjectNode("models.py", false, usersApp);
        models.setContent(generateUserModels());
        usersApp.addChild(models);

        ProjectNode admin = new ProjectNode("admin.py", false, usersApp);
        admin.setContent(generateUserAdmin());
        usersApp.addChild(admin);
    }

    private void createProductsApp(ProjectNode appsDir) {
        ProjectNode productsApp = new ProjectNode("products", true, appsDir);
        appsDir.addChild(productsApp);

        ProjectNode init = new ProjectNode("__init__.py", false, productsApp);
        init.setContent("");
        productsApp.addChild(init);

        ProjectNode models = new ProjectNode("models.py", false, productsApp);
        models.setContent(generateProductModels());
        productsApp.addChild(models);

        ProjectNode views = new ProjectNode("views.py", false, productsApp);
        views.setContent(generateProductViews());
        productsApp.addChild(views);
    }

    private void createCartApp(ProjectNode appsDir) {
        ProjectNode cartApp = new ProjectNode("cart", true, appsDir);
        appsDir.addChild(cartApp);

        ProjectNode init = new ProjectNode("__init__.py", false, cartApp);
        init.setContent("");
        cartApp.addChild(init);

        ProjectNode cart = new ProjectNode("cart.py", false, cartApp);
        cart.setContent(generateCartClass());
        cartApp.addChild(cart);

        ProjectNode views = new ProjectNode("views.py", false, cartApp);
        views.setContent(generateCartViews());
        cartApp.addChild(views);
    }

    private void createOrdersApp(ProjectNode appsDir) {
        ProjectNode ordersApp = new ProjectNode("orders", true, appsDir);
        appsDir.addChild(ordersApp);

        ProjectNode init = new ProjectNode("__init__.py", false, ordersApp);
        init.setContent("");
        ordersApp.addChild(init);

        ProjectNode models = new ProjectNode("models.py", false, ordersApp);
        models.setContent(generateOrderModels());
        ordersApp.addChild(models);

        ProjectNode views = new ProjectNode("views.py", false, ordersApp);
        views.setContent(generateOrderViews());
        ordersApp.addChild(views);
    }

    private void createPaymentsApp(ProjectNode appsDir) {
        ProjectNode paymentsApp = new ProjectNode("payments", true, appsDir);
        appsDir.addChild(paymentsApp);

        ProjectNode init = new ProjectNode("__init__.py", false, paymentsApp);
        init.setContent("");
        paymentsApp.addChild(init);

        ProjectNode processors = new ProjectNode("processors.py", false, paymentsApp);
        processors.setContent(generatePaymentProcessors());
        paymentsApp.addChild(processors);

        ProjectNode views = new ProjectNode("views.py", false, paymentsApp);
        views.setContent(generatePaymentViews());
        paymentsApp.addChild(views);
    }

    private void createDashboardApp(ProjectNode appsDir) {
        ProjectNode dashboardApp = new ProjectNode("dashboard", true, appsDir);
        appsDir.addChild(dashboardApp);

        ProjectNode init = new ProjectNode("__init__.py", false, dashboardApp);
        init.setContent("");
        dashboardApp.addChild(init);

        ProjectNode views = new ProjectNode("views.py", false, dashboardApp);
        views.setContent(generateDashboardViews());
        dashboardApp.addChild(views);

        ProjectNode widgets = new ProjectNode("widgets.py", false, dashboardApp);
        widgets.setContent(generateDashboardWidgets());
        dashboardApp.addChild(widgets);
    }

    private void createReportsApp(ProjectNode appsDir) {
        ProjectNode reportsApp = new ProjectNode("reports", true, appsDir);
        appsDir.addChild(reportsApp);

        ProjectNode init = new ProjectNode("__init__.py", false, reportsApp);
        init.setContent("");
        reportsApp.addChild(init);

        ProjectNode generators = new ProjectNode("generators.py", false, reportsApp);
        generators.setContent(generateReportGenerators());
        reportsApp.addChild(generators);

        ProjectNode views = new ProjectNode("views.py", false, reportsApp);
        views.setContent(generateReportViews());
        reportsApp.addChild(views);
    }

    private void createDataApp(ProjectNode appsDir) {
        ProjectNode dataApp = new ProjectNode("data", true, appsDir);
        appsDir.addChild(dataApp);

        ProjectNode init = new ProjectNode("__init__.py", false, dataApp);
        init.setContent("");
        dataApp.addChild(init);

        ProjectNode collectors = new ProjectNode("collectors.py", false, dataApp);
        collectors.setContent(generateDataCollectors());
        dataApp.addChild(collectors);

        ProjectNode processors = new ProjectNode("processors.py", false, dataApp);
        processors.setContent(generateDataProcessors());
        dataApp.addChild(processors);
    }

    private void createMLApp(ProjectNode appsDir) {
        ProjectNode mlApp = new ProjectNode("ml", true, appsDir);
        appsDir.addChild(mlApp);

        ProjectNode init = new ProjectNode("__init__.py", false, mlApp);
        init.setContent("");
        mlApp.addChild(init);

        ProjectNode models = new ProjectNode("models.py", false, mlApp);
        models.setContent(generateMLModels());
        mlApp.addChild(models);

        ProjectNode trainers = new ProjectNode("trainers.py", false, mlApp);
        trainers.setContent(generateMLTrainers());
        mlApp.addChild(trainers);
    }

    private void createPredictionsApp(ProjectNode appsDir) {
        ProjectNode predictionsApp = new ProjectNode("predictions", true, appsDir);
        appsDir.addChild(predictionsApp);

        ProjectNode init = new ProjectNode("__init__.py", false, predictionsApp);
        init.setContent("");
        predictionsApp.addChild(init);

        ProjectNode predictors = new ProjectNode("predictors.py", false, predictionsApp);
        predictors.setContent(generatePredictors());
        predictionsApp.addChild(predictors);

        ProjectNode views = new ProjectNode("views.py", false, predictionsApp);
        views.setContent(generatePredictionViews());
        predictionsApp.addChild(views);
    }

    private void createDatasetApp(ProjectNode appsDir) {
        ProjectNode datasetApp = new ProjectNode("datasets", true, appsDir);
        appsDir.addChild(datasetApp);

        ProjectNode init = new ProjectNode("__init__.py", false, datasetApp);
        init.setContent("");
        datasetApp.addChild(init);

        ProjectNode models = new ProjectNode("models.py", false, datasetApp);
        models.setContent(generateDatasetModels());
        datasetApp.addChild(models);

        ProjectNode loaders = new ProjectNode("loaders.py", false, datasetApp);
        loaders.setContent(generateDatasetLoaders());
        datasetApp.addChild(loaders);
    }

    private void addConfigurationFiles(ProjectNode root, String projectName, DjangoProjectType type) {
        // requirements.txt
        ProjectNode requirements = new ProjectNode("requirements.txt", false, root);
        requirements.setContent(generateRequirements(type));
        root.addChild(requirements);

        // requirements-dev.txt
        ProjectNode requirementsDev = new ProjectNode("requirements-dev.txt", false, root);
        requirementsDev.setContent(generateRequirementsDev());
        root.addChild(requirementsDev);

        // manage.py
        ProjectNode manage = new ProjectNode("manage.py", false, root);
        manage.setContent(generateManagePy(projectName));
        root.addChild(manage);

        // .env.example
        ProjectNode envExample = new ProjectNode(".env.example", false, root);
        envExample.setContent(generateEnvExample(type));
        root.addChild(envExample);

        // .gitignore
        ProjectNode gitignore = new ProjectNode(".gitignore", false, root);
        gitignore.setContent(generateGitignore());
        root.addChild(gitignore);

        // pyproject.toml
        ProjectNode pyproject = new ProjectNode("pyproject.toml", false, root);
        pyproject.setContent(generatePyprojectToml(projectName));
        root.addChild(pyproject);

        // setup.cfg
        ProjectNode setupCfg = new ProjectNode("setup.cfg", false, root);
        setupCfg.setContent(generateSetupCfg());
        root.addChild(setupCfg);

        // pytest.ini
        ProjectNode pytest = new ProjectNode("pytest.ini", false, root);
        pytest.setContent(generatePytestIni());
        root.addChild(pytest);

        // .pre-commit-config.yaml
        ProjectNode precommit = new ProjectNode(".pre-commit-config.yaml", false, root);
        precommit.setContent(generatePrecommitConfig());
        root.addChild(precommit);

        // .editorconfig
        ProjectNode editorconfig = new ProjectNode(".editorconfig", false, root);
        editorconfig.setContent(generateEditorconfig());
        root.addChild(editorconfig);

        // README.md
        ProjectNode readme = new ProjectNode("README.md", false, root);
        readme.setContent(generateReadme(projectName, type));
        root.addChild(readme);
    }

    private void addDockerSupport(ProjectNode root, String projectName, DjangoProjectType type) {
        // Dockerfile
        ProjectNode dockerfile = new ProjectNode("Dockerfile", false, root);
        dockerfile.setContent(generateDockerfile(type));
        root.addChild(dockerfile);

        // docker-compose.yml
        ProjectNode dockerCompose = new ProjectNode("docker-compose.yml", false, root);
        dockerCompose.setContent(generateDockerCompose(projectName, type));
        root.addChild(dockerCompose);

        // .dockerignore
        ProjectNode dockerignore = new ProjectNode(".dockerignore", false, root);
        dockerignore.setContent(generateDockerignore());
        root.addChild(dockerignore);

        // docker directory
        ProjectNode dockerDir = new ProjectNode("docker", true, root);
        root.addChild(dockerDir);

        // nginx config
        ProjectNode nginxDir = new ProjectNode("nginx", true, dockerDir);
        dockerDir.addChild(nginxDir);

        ProjectNode nginxConf = new ProjectNode("nginx.conf", false, nginxDir);
        nginxConf.setContent(generateNginxConfig(projectName));
        nginxDir.addChild(nginxConf);

        // entrypoint script
        ProjectNode entrypoint = new ProjectNode("entrypoint.sh", false, dockerDir);
        entrypoint.setContent(generateEntrypoint());
        dockerDir.addChild(entrypoint);
    }

    private void addCICDPipeline(ProjectNode root, String projectName) {
        // .github directory
        ProjectNode githubDir = new ProjectNode(".github", true, root);
        root.addChild(githubDir);

        // workflows directory
        ProjectNode workflowsDir = new ProjectNode("workflows", true, githubDir);
        githubDir.addChild(workflowsDir);

        // CI workflow
        ProjectNode ciWorkflow = new ProjectNode("ci.yml", false, workflowsDir);
        ciWorkflow.setContent(generateCIWorkflow(projectName));
        workflowsDir.addChild(ciWorkflow);

        // Deploy workflow
        ProjectNode deployWorkflow = new ProjectNode("deploy.yml", false, workflowsDir);
        deployWorkflow.setContent(generateDeployWorkflow(projectName));
        workflowsDir.addChild(deployWorkflow);

        // Dependabot config
        ProjectNode dependabot = new ProjectNode("dependabot.yml", false, githubDir);
        dependabot.setContent(generateDependabotConfig());
        githubDir.addChild(dependabot);
    }

    private String generateBaseSettings(String projectName, DjangoProjectType type) {
        return "\"\"\"Django settings for " + projectName + " project.\"\"\"\n\n" +
               "import os\n" +
               "from pathlib import Path\n" +
               "from datetime import timedelta\n" +
               "import environ\n\n" +
               "env = environ.Env()\n\n" +
               "# Build paths inside the project\n" +
               "BASE_DIR = Path(__file__).resolve().parent.parent.parent\n\n" +
               "# Read environment variables\n" +
               "environ.Env.read_env(os.path.join(BASE_DIR, '.env'))\n\n" +
               "# SECURITY WARNING: keep the secret key used in production secret!\n" +
               "SECRET_KEY = env('SECRET_KEY', default='django-insecure-change-this-in-production')\n\n" +
               "# SECURITY WARNING: don't run with debug turned on in production!\n" +
               "DEBUG = env.bool('DEBUG', default=False)\n\n" +
               "ALLOWED_HOSTS = env.list('ALLOWED_HOSTS', default=['localhost', '127.0.0.1'])\n\n" +
               "# Application definition\n" +
               "DJANGO_APPS = [\n" +
               "    'django.contrib.admin',\n" +
               "    'django.contrib.auth',\n" +
               "    'django.contrib.contenttypes',\n" +
               "    'django.contrib.sessions',\n" +
               "    'django.contrib.messages',\n" +
               "    'django.contrib.staticfiles',\n" +
               "    'django.contrib.sites',\n" +
               "    'django.contrib.humanize',\n" +
               "]\n\n" +
               "THIRD_PARTY_APPS = [\n" +
               "    'rest_framework',\n" +
               "    'corsheaders',\n" +
               "    'django_filters',\n" +
               "    'drf_spectacular',\n" +
               "    'django_celery_beat',\n" +
               "    'django_celery_results',\n" +
               "    'storages',\n" +
               "    'crispy_forms',\n" +
               "    'crispy_bootstrap5',\n" +
               "    'django_extensions',\n" +
               "    'debug_toolbar',\n" +
               "    'silk',\n" +
               "    'django_redis',\n" +
               (type == DjangoProjectType.MACHINE_LEARNING ?
                "    'django_pandas',\n" : "") +
               "]\n\n" +
               "LOCAL_APPS = [\n" +
               generateLocalApps(type) +
               "]\n\n" +
               "INSTALLED_APPS = DJANGO_APPS + THIRD_PARTY_APPS + LOCAL_APPS\n\n" +
               "MIDDLEWARE = [\n" +
               "    'django.middleware.security.SecurityMiddleware',\n" +
               "    'whitenoise.middleware.WhiteNoiseMiddleware',\n" +
               "    'corsheaders.middleware.CorsMiddleware',\n" +
               "    'django.contrib.sessions.middleware.SessionMiddleware',\n" +
               "    'django.middleware.common.CommonMiddleware',\n" +
               "    'django.middleware.csrf.CsrfViewMiddleware',\n" +
               "    'django.contrib.auth.middleware.AuthenticationMiddleware',\n" +
               "    'django.contrib.messages.middleware.MessageMiddleware',\n" +
               "    'django.middleware.clickjacking.XFrameOptionsMiddleware',\n" +
               "    'debug_toolbar.middleware.DebugToolbarMiddleware',\n" +
               "    'silk.middleware.SilkyMiddleware',\n" +
               "]\n\n" +
               "ROOT_URLCONF = '" + projectName + ".urls'\n\n" +
               "TEMPLATES = [\n" +
               "    {\n" +
               "        'BACKEND': 'django.template.backends.django.DjangoTemplates',\n" +
               "        'DIRS': [BASE_DIR / 'templates'],\n" +
               "        'APP_DIRS': True,\n" +
               "        'OPTIONS': {\n" +
               "            'context_processors': [\n" +
               "                'django.template.context_processors.debug',\n" +
               "                'django.template.context_processors.request',\n" +
               "                'django.contrib.auth.context_processors.auth',\n" +
               "                'django.contrib.messages.context_processors.messages',\n" +
               "            ],\n" +
               "        },\n" +
               "    },\n" +
               "]\n\n" +
               "WSGI_APPLICATION = '" + projectName + ".wsgi.application'\n" +
               "ASGI_APPLICATION = '" + projectName + ".asgi.application'\n\n" +
               "# Database\n" +
               "DATABASES = {\n" +
               "    'default': {\n" +
               "        'ENGINE': 'django.db.backends.postgresql',\n" +
               "        'NAME': env('DB_NAME', default='" + projectName + "_db'),\n" +
               "        'USER': env('DB_USER', default='postgres'),\n" +
               "        'PASSWORD': env('DB_PASSWORD', default='postgres'),\n" +
               "        'HOST': env('DB_HOST', default='localhost'),\n" +
               "        'PORT': env('DB_PORT', default='5432'),\n" +
               "        'CONN_MAX_AGE': 600,\n" +
               "        'OPTIONS': {\n" +
               "            'connect_timeout': 10,\n" +
               "        },\n" +
               "    }\n" +
               "}\n\n" +
               "# Cache\n" +
               "CACHES = {\n" +
               "    'default': {\n" +
               "        'BACKEND': 'django_redis.cache.RedisCache',\n" +
               "        'LOCATION': env('REDIS_URL', default='redis://127.0.0.1:6379/1'),\n" +
               "        'OPTIONS': {\n" +
               "            'CLIENT_CLASS': 'django_redis.client.DefaultClient',\n" +
               "        }\n" +
               "    }\n" +
               "}\n\n" +
               "# Celery Configuration\n" +
               "CELERY_BROKER_URL = env('CELERY_BROKER_URL', default='redis://localhost:6379/0')\n" +
               "CELERY_RESULT_BACKEND = env('CELERY_RESULT_BACKEND', default='django-db')\n" +
               "CELERY_ACCEPT_CONTENT = ['json']\n" +
               "CELERY_TASK_SERIALIZER = 'json'\n" +
               "CELERY_RESULT_SERIALIZER = 'json'\n" +
               "CELERY_TIMEZONE = 'UTC'\n" +
               "CELERY_BEAT_SCHEDULER = 'django_celery_beat.schedulers:DatabaseScheduler'\n\n" +
               "# Password validation\n" +
               "AUTH_PASSWORD_VALIDATORS = [\n" +
               "    {'NAME': 'django.contrib.auth.password_validation.UserAttributeSimilarityValidator'},\n" +
               "    {'NAME': 'django.contrib.auth.password_validation.MinimumLengthValidator'},\n" +
               "    {'NAME': 'django.contrib.auth.password_validation.CommonPasswordValidator'},\n" +
               "    {'NAME': 'django.contrib.auth.password_validation.NumericPasswordValidator'},\n" +
               "]\n\n" +
               "# Internationalization\n" +
               "LANGUAGE_CODE = 'en-us'\n" +
               "TIME_ZONE = 'UTC'\n" +
               "USE_I18N = True\n" +
               "USE_TZ = True\n\n" +
               "# Static files (CSS, JavaScript, Images)\n" +
               "STATIC_URL = '/static/'\n" +
               "STATIC_ROOT = BASE_DIR / 'staticfiles'\n" +
               "STATICFILES_DIRS = [BASE_DIR / 'static']\n" +
               "STATICFILES_STORAGE = 'whitenoise.storage.CompressedManifestStaticFilesStorage'\n\n" +
               "# Media files\n" +
               "MEDIA_URL = '/media/'\n" +
               "MEDIA_ROOT = BASE_DIR / 'media'\n\n" +
               "# Default primary key field type\n" +
               "DEFAULT_AUTO_FIELD = 'django.db.models.BigAutoField'\n\n" +
               "# REST Framework\n" +
               "REST_FRAMEWORK = {\n" +
               "    'DEFAULT_AUTHENTICATION_CLASSES': [\n" +
               "        'rest_framework.authentication.SessionAuthentication',\n" +
               "        'rest_framework_simplejwt.authentication.JWTAuthentication',\n" +
               "    ],\n" +
               "    'DEFAULT_PERMISSION_CLASSES': [\n" +
               "        'rest_framework.permissions.IsAuthenticated',\n" +
               "    ],\n" +
               "    'DEFAULT_PAGINATION_CLASS': 'rest_framework.pagination.PageNumberPagination',\n" +
               "    'PAGE_SIZE': 20,\n" +
               "    'DEFAULT_FILTER_BACKENDS': [\n" +
               "        'django_filters.rest_framework.DjangoFilterBackend',\n" +
               "        'rest_framework.filters.SearchFilter',\n" +
               "        'rest_framework.filters.OrderingFilter',\n" +
               "    ],\n" +
               "    'DEFAULT_SCHEMA_CLASS': 'drf_spectacular.openapi.AutoSchema',\n" +
               "    'DEFAULT_RENDERER_CLASSES': [\n" +
               "        'rest_framework.renderers.JSONRenderer',\n" +
               "        'rest_framework.renderers.BrowsableAPIRenderer',\n" +
               "    ],\n" +
               "}\n\n" +
               "# JWT Settings\n" +
               "SIMPLE_JWT = {\n" +
               "    'ACCESS_TOKEN_LIFETIME': timedelta(minutes=60),\n" +
               "    'REFRESH_TOKEN_LIFETIME': timedelta(days=7),\n" +
               "    'ROTATE_REFRESH_TOKENS': True,\n" +
               "    'BLACKLIST_AFTER_ROTATION': True,\n" +
               "    'UPDATE_LAST_LOGIN': True,\n" +
               "    'ALGORITHM': 'HS256',\n" +
               "    'SIGNING_KEY': SECRET_KEY,\n" +
               "}\n\n" +
               "# CORS Settings\n" +
               "CORS_ALLOW_ALL_ORIGINS = env.bool('CORS_ALLOW_ALL_ORIGINS', default=False)\n" +
               "CORS_ALLOWED_ORIGINS = env.list('CORS_ALLOWED_ORIGINS', default=['http://localhost:3000'])\n\n" +
               "# Spectacular Settings\n" +
               "SPECTACULAR_SETTINGS = {\n" +
               "    'TITLE': '" + projectName + " API',\n" +
               "    'DESCRIPTION': 'API documentation for " + projectName + "',\n" +
               "    'VERSION': '1.0.0',\n" +
               "    'SERVE_INCLUDE_SCHEMA': False,\n" +
               "}\n\n" +
               "# Crispy Forms\n" +
               "CRISPY_ALLOWED_TEMPLATE_PACKS = 'bootstrap5'\n" +
               "CRISPY_TEMPLATE_PACK = 'bootstrap5'\n\n" +
               "# Logging\n" +
               "LOGGING = {\n" +
               "    'version': 1,\n" +
               "    'disable_existing_loggers': False,\n" +
               "    'formatters': {\n" +
               "        'verbose': {\n" +
               "            'format': '{levelname} {asctime} {module} {message}',\n" +
               "            'style': '{',\n" +
               "        },\n" +
               "    },\n" +
               "    'handlers': {\n" +
               "        'file': {\n" +
               "            'level': 'INFO',\n" +
               "            'class': 'logging.handlers.RotatingFileHandler',\n" +
               "            'filename': BASE_DIR / 'logs' / 'django.log',\n" +
               "            'maxBytes': 1024 * 1024 * 15,  # 15MB\n" +
               "            'backupCount': 10,\n" +
               "            'formatter': 'verbose',\n" +
               "        },\n" +
               "        'console': {\n" +
               "            'level': 'INFO',\n" +
               "            'class': 'logging.StreamHandler',\n" +
               "            'formatter': 'verbose',\n" +
               "        },\n" +
               "    },\n" +
               "    'root': {\n" +
               "        'handlers': ['console', 'file'],\n" +
               "        'level': 'INFO',\n" +
               "    },\n" +
               "}\n\n" +
               "# Email Configuration\n" +
               "EMAIL_BACKEND = env('EMAIL_BACKEND', default='django.core.mail.backends.console.EmailBackend')\n" +
               "EMAIL_HOST = env('EMAIL_HOST', default='smtp.gmail.com')\n" +
               "EMAIL_PORT = env.int('EMAIL_PORT', default=587)\n" +
               "EMAIL_USE_TLS = env.bool('EMAIL_USE_TLS', default=True)\n" +
               "EMAIL_HOST_USER = env('EMAIL_HOST_USER', default='')\n" +
               "EMAIL_HOST_PASSWORD = env('EMAIL_HOST_PASSWORD', default='')\n" +
               "DEFAULT_FROM_EMAIL = env('DEFAULT_FROM_EMAIL', default='noreply@example.com')\n\n" +
               "# Security Settings\n" +
               "SECURE_SSL_REDIRECT = env.bool('SECURE_SSL_REDIRECT', default=False)\n" +
               "SESSION_COOKIE_SECURE = env.bool('SESSION_COOKIE_SECURE', default=False)\n" +
               "CSRF_COOKIE_SECURE = env.bool('CSRF_COOKIE_SECURE', default=False)\n" +
               "SECURE_HSTS_SECONDS = env.int('SECURE_HSTS_SECONDS', default=0)\n" +
               "SECURE_HSTS_INCLUDE_SUBDOMAINS = env.bool('SECURE_HSTS_INCLUDE_SUBDOMAINS', default=False)\n" +
               "SECURE_HSTS_PRELOAD = env.bool('SECURE_HSTS_PRELOAD', default=False)\n\n" +
               "# Site Configuration\n" +
               "SITE_ID = 1\n";
    }

    private String generateLocalApps(DjangoProjectType type) {
        switch (type) {
            case REST_API:
                return "    'apps.api',\n    'apps.authentication',\n";
            case FULL_STACK:
                return "    'apps.core',\n    'apps.authentication',\n    'apps.pages',\n";
            case MICROSERVICE:
                return "    'apps.service',\n    'apps.health',\n";
            case CMS:
                return "    'apps.content',\n    'apps.media_manager',\n    'apps.users',\n";
            case ECOMMERCE:
                return "    'apps.products',\n    'apps.cart',\n    'apps.orders',\n    'apps.payments',\n";
            case ANALYTICS:
                return "    'apps.dashboard',\n    'apps.reports',\n    'apps.data',\n";
            case MACHINE_LEARNING:
                return "    'apps.ml',\n    'apps.predictions',\n    'apps.datasets',\n";
            default:
                return "    'apps.core',\n";
        }
    }

    private String generateLocalSettings() {
        return "\"\"\"Local development settings.\"\"\"\n\n" +
               "from .base import *\n\n" +
               "DEBUG = True\n\n" +
               "ALLOWED_HOSTS = ['*']\n\n" +
               "# Database\n" +
               "DATABASES = {\n" +
               "    'default': {\n" +
               "        'ENGINE': 'django.db.backends.sqlite3',\n" +
               "        'NAME': BASE_DIR / 'db.sqlite3',\n" +
               "    }\n" +
               "}\n\n" +
               "# Debug Toolbar\n" +
               "INTERNAL_IPS = ['127.0.0.1']\n\n" +
               "# Email\n" +
               "EMAIL_BACKEND = 'django.core.mail.backends.console.EmailBackend'\n\n" +
               "# CORS\n" +
               "CORS_ALLOW_ALL_ORIGINS = True\n";
    }

    private String generateProductionSettings() {
        return "\"\"\"Production settings.\"\"\"\n\n" +
               "from .base import *\n\n" +
               "DEBUG = False\n\n" +
               "# Security\n" +
               "SECURE_SSL_REDIRECT = True\n" +
               "SESSION_COOKIE_SECURE = True\n" +
               "CSRF_COOKIE_SECURE = True\n" +
               "SECURE_HSTS_SECONDS = 31536000\n" +
               "SECURE_HSTS_INCLUDE_SUBDOMAINS = True\n" +
               "SECURE_HSTS_PRELOAD = True\n\n" +
               "# Static files\n" +
               "AWS_ACCESS_KEY_ID = env('AWS_ACCESS_KEY_ID')\n" +
               "AWS_SECRET_ACCESS_KEY = env('AWS_SECRET_ACCESS_KEY')\n" +
               "AWS_STORAGE_BUCKET_NAME = env('AWS_STORAGE_BUCKET_NAME')\n" +
               "AWS_S3_CUSTOM_DOMAIN = f'{AWS_STORAGE_BUCKET_NAME}.s3.amazonaws.com'\n" +
               "AWS_S3_OBJECT_PARAMETERS = {'CacheControl': 'max-age=86400'}\n\n" +
               "# Static files\n" +
               "STATICFILES_STORAGE = 'storages.backends.s3boto3.S3Boto3Storage'\n" +
               "STATIC_URL = f'https://{AWS_S3_CUSTOM_DOMAIN}/static/'\n\n" +
               "# Media files\n" +
               "DEFAULT_FILE_STORAGE = 'storages.backends.s3boto3.S3Boto3Storage'\n" +
               "MEDIA_URL = f'https://{AWS_S3_CUSTOM_DOMAIN}/media/'\n";
    }

    private String generateTestSettings() {
        return "\"\"\"Test settings.\"\"\"\n\n" +
               "from .base import *\n\n" +
               "# Use in-memory database for tests\n" +
               "DATABASES = {\n" +
               "    'default': {\n" +
               "        'ENGINE': 'django.db.backends.sqlite3',\n" +
               "        'NAME': ':memory:',\n" +
               "    }\n" +
               "}\n\n" +
               "# Disable migrations for faster tests\n" +
               "class DisableMigrations:\n" +
               "    def __contains__(self, item):\n" +
               "        return True\n\n" +
               "    def __getitem__(self, item):\n" +
               "        return None\n\n" +
               "MIGRATION_MODULES = DisableMigrations()\n\n" +
               "# Use simple password hasher for faster tests\n" +
               "PASSWORD_HASHERS = [\n" +
               "    'django.contrib.auth.hashers.MD5PasswordHasher',\n" +
               "]\n\n" +
               "# Disable cache\n" +
               "CACHES = {\n" +
               "    'default': {\n" +
               "        'BACKEND': 'django.core.cache.backends.dummy.DummyCache',\n" +
               "    }\n" +
               "}\n\n" +
               "# Email\n" +
               "EMAIL_BACKEND = 'django.core.mail.backends.locmem.EmailBackend'\n";
    }

    private String generateMainUrls(DjangoProjectType type) {
        StringBuilder urls = new StringBuilder();
        urls.append("\"\"\"URL Configuration\"\"\"\n\n");
        urls.append("from django.contrib import admin\n");
        urls.append("from django.urls import path, include\n");
        urls.append("from django.conf import settings\n");
        urls.append("from django.conf.urls.static import static\n");
        urls.append("from drf_spectacular.views import SpectacularAPIView, SpectacularRedocView, SpectacularSwaggerView\n\n");

        urls.append("urlpatterns = [\n");
        urls.append("    path('admin/', admin.site.urls),\n");

        switch (type) {
            case REST_API:
                urls.append("    path('api/v1/', include('apps.api.v1.urls')),\n");
                urls.append("    path('api/auth/', include('apps.authentication.urls')),\n");
                break;
            case FULL_STACK:
                urls.append("    path('', include('apps.core.urls')),\n");
                urls.append("    path('auth/', include('apps.authentication.urls')),\n");
                urls.append("    path('pages/', include('apps.pages.urls')),\n");
                break;
            case MICROSERVICE:
                urls.append("    path('service/', include('apps.service.urls')),\n");
                urls.append("    path('health/', include('apps.health.urls')),\n");
                break;
            case CMS:
                urls.append("    path('content/', include('apps.content.urls')),\n");
                urls.append("    path('media/', include('apps.media_manager.urls')),\n");
                urls.append("    path('users/', include('apps.users.urls')),\n");
                break;
            case ECOMMERCE:
                urls.append("    path('products/', include('apps.products.urls')),\n");
                urls.append("    path('cart/', include('apps.cart.urls')),\n");
                urls.append("    path('orders/', include('apps.orders.urls')),\n");
                urls.append("    path('payments/', include('apps.payments.urls')),\n");
                break;
            case ANALYTICS:
                urls.append("    path('dashboard/', include('apps.dashboard.urls')),\n");
                urls.append("    path('reports/', include('apps.reports.urls')),\n");
                urls.append("    path('data/', include('apps.data.urls')),\n");
                break;
            case MACHINE_LEARNING:
                urls.append("    path('ml/', include('apps.ml.urls')),\n");
                urls.append("    path('predictions/', include('apps.predictions.urls')),\n");
                urls.append("    path('datasets/', include('apps.datasets.urls')),\n");
                break;
        }

        urls.append("    # API Documentation\n");
        urls.append("    path('api/schema/', SpectacularAPIView.as_view(), name='schema'),\n");
        urls.append("    path('api/docs/', SpectacularSwaggerView.as_view(url_name='schema'), name='swagger-ui'),\n");
        urls.append("    path('api/redoc/', SpectacularRedocView.as_view(url_name='schema'), name='redoc'),\n");
        urls.append("]\n\n");

        urls.append("if settings.DEBUG:\n");
        urls.append("    import debug_toolbar\n");
        urls.append("    urlpatterns += [\n");
        urls.append("        path('__debug__/', include(debug_toolbar.urls)),\n");
        urls.append("        path('silk/', include('silk.urls', namespace='silk')),\n");
        urls.append("    ]\n");
        urls.append("    urlpatterns += static(settings.MEDIA_URL, document_root=settings.MEDIA_ROOT)\n");
        urls.append("    urlpatterns += static(settings.STATIC_URL, document_root=settings.STATIC_ROOT)\n");

        return urls.toString();
    }

    private String generateWSGI(String projectName) {
        return "\"\"\"WSGI config for " + projectName + " project.\"\"\"\n\n" +
               "import os\n\n" +
               "from django.core.wsgi import get_wsgi_application\n\n" +
               "os.environ.setdefault('DJANGO_SETTINGS_MODULE', '" + projectName + ".settings')\n\n" +
               "application = get_wsgi_application()\n";
    }

    private String generateASGI(String projectName) {
        return "\"\"\"ASGI config for " + projectName + " project.\"\"\"\n\n" +
               "import os\n\n" +
               "from django.core.asgi import get_asgi_application\n" +
               "from channels.routing import ProtocolTypeRouter, URLRouter\n" +
               "from channels.auth import AuthMiddlewareStack\n\n" +
               "os.environ.setdefault('DJANGO_SETTINGS_MODULE', '" + projectName + ".settings')\n\n" +
               "django_asgi_app = get_asgi_application()\n\n" +
               "application = ProtocolTypeRouter({\n" +
               "    'http': django_asgi_app,\n" +
               "    # Add WebSocket routing here if needed\n" +
               "})\n";
    }

    private String generateCeleryConfig(String projectName) {
        return "\"\"\"Celery configuration for " + projectName + " project.\"\"\"\n\n" +
               "import os\n" +
               "from celery import Celery\n\n" +
               "os.environ.setdefault('DJANGO_SETTINGS_MODULE', '" + projectName + ".settings')\n\n" +
               "app = Celery('" + projectName + "')\n" +
               "app.config_from_object('django.conf:settings', namespace='CELERY')\n" +
               "app.autodiscover_tasks()\n\n" +
               "@app.task(bind=True)\n" +
               "def debug_task(self):\n" +
               "    print(f'Request: {self.request!r}')\n";
    }

    // Generate content methods for various app files
    private String generateAPISerializers() {
        return "from rest_framework import serializers\n" +
               "from django.contrib.auth import get_user_model\n\n" +
               "User = get_user_model()\n\n" +
               "class UserSerializer(serializers.ModelSerializer):\n" +
               "    class Meta:\n" +
               "        model = User\n" +
               "        fields = ['id', 'username', 'email', 'first_name', 'last_name']\n" +
               "        read_only_fields = ['id']\n\n" +
               "class PaginationSerializer(serializers.Serializer):\n" +
               "    count = serializers.IntegerField()\n" +
               "    next = serializers.URLField(allow_null=True)\n" +
               "    previous = serializers.URLField(allow_null=True)\n" +
               "    results = serializers.ListField()\n";
    }

    private String generateAPIViews() {
        return "from rest_framework import viewsets, status\n" +
               "from rest_framework.decorators import action\n" +
               "from rest_framework.response import Response\n" +
               "from rest_framework.permissions import IsAuthenticated\n" +
               "from django.contrib.auth import get_user_model\n" +
               "from .serializers import UserSerializer\n\n" +
               "User = get_user_model()\n\n" +
               "class UserViewSet(viewsets.ModelViewSet):\n" +
               "    queryset = User.objects.all()\n" +
               "    serializer_class = UserSerializer\n" +
               "    permission_classes = [IsAuthenticated]\n\n" +
               "    @action(detail=False, methods=['get'])\n" +
               "    def me(self, request):\n" +
               "        serializer = self.get_serializer(request.user)\n" +
               "        return Response(serializer.data)\n";
    }

    private String generateAPIUrls() {
        return "from django.urls import path, include\n" +
               "from rest_framework.routers import DefaultRouter\n" +
               "from .views import UserViewSet\n\n" +
               "router = DefaultRouter()\n" +
               "router.register('users', UserViewSet)\n\n" +
               "urlpatterns = [\n" +
               "    path('', include(router.urls)),\n" +
               "]\n";
    }

    private String generateAPIFilters() {
        return "import django_filters\n" +
               "from django.contrib.auth import get_user_model\n\n" +
               "User = get_user_model()\n\n" +
               "class UserFilter(django_filters.FilterSet):\n" +
               "    username = django_filters.CharFilter(lookup_expr='icontains')\n" +
               "    email = django_filters.CharFilter(lookup_expr='icontains')\n" +
               "    \n" +
               "    class Meta:\n" +
               "        model = User\n" +
               "        fields = ['username', 'email', 'is_active', 'is_staff']\n";
    }

    private String generateAPIPagination() {
        return "from rest_framework.pagination import PageNumberPagination, LimitOffsetPagination, CursorPagination\n\n" +
               "class StandardResultsSetPagination(PageNumberPagination):\n" +
               "    page_size = 20\n" +
               "    page_size_query_param = 'page_size'\n" +
               "    max_page_size = 100\n\n" +
               "class LargeResultsSetPagination(PageNumberPagination):\n" +
               "    page_size = 100\n" +
               "    page_size_query_param = 'page_size'\n" +
               "    max_page_size = 1000\n\n" +
               "class CustomLimitOffsetPagination(LimitOffsetPagination):\n" +
               "    default_limit = 20\n" +
               "    limit_query_param = 'limit'\n" +
               "    offset_query_param = 'offset'\n" +
               "    max_limit = 100\n\n" +
               "class CustomCursorPagination(CursorPagination):\n" +
               "    page_size = 20\n" +
               "    ordering = '-created_at'\n";
    }

    // Continue with more content generation methods...
    private String generateAuthModels() {
        return "from django.db import models\n" +
               "from django.contrib.auth.models import AbstractUser\n" +
               "from django.utils.translation import gettext_lazy as _\n\n" +
               "class CustomUser(AbstractUser):\n" +
               "    email = models.EmailField(_('email address'), unique=True)\n" +
               "    phone = models.CharField(max_length=20, blank=True)\n" +
               "    bio = models.TextField(blank=True)\n" +
               "    avatar = models.ImageField(upload_to='avatars/', blank=True, null=True)\n" +
               "    date_of_birth = models.DateField(blank=True, null=True)\n" +
               "    is_verified = models.BooleanField(default=False)\n" +
               "    created_at = models.DateTimeField(auto_now_add=True)\n" +
               "    updated_at = models.DateTimeField(auto_now=True)\n\n" +
               "    USERNAME_FIELD = 'email'\n" +
               "    REQUIRED_FIELDS = ['username']\n\n" +
               "    class Meta:\n" +
               "        verbose_name = _('User')\n" +
               "        verbose_name_plural = _('Users')\n";
    }

    private String generateRequirements(DjangoProjectType type) {
        List<String> deps = new ArrayList<>();

        // Core Django dependencies
        deps.add("Django==5.0.1");
        deps.add("djangorestframework==3.14.0");
        deps.add("django-cors-headers==4.3.1");
        deps.add("django-environ==0.11.2");
        deps.add("django-filter==23.5");
        deps.add("drf-spectacular==0.27.0");
        deps.add("djangorestframework-simplejwt==5.3.1");

        // Database
        deps.add("psycopg2-binary==2.9.9");
        deps.add("dj-database-url==2.1.0");

        // Caching and Celery
        deps.add("redis==5.0.1");
        deps.add("django-redis==5.4.0");
        deps.add("celery==5.3.4");
        deps.add("django-celery-beat==2.5.0");
        deps.add("django-celery-results==2.5.1");

        // Storage
        deps.add("boto3==1.34.14");
        deps.add("django-storages==1.14.2");
        deps.add("Pillow==10.2.0");
        deps.add("whitenoise==6.6.0");

        // Forms and templates
        deps.add("django-crispy-forms==2.1");
        deps.add("crispy-bootstrap5==2023.10");

        // Utilities
        deps.add("python-decouple==3.8");
        deps.add("pytz==2023.3.post1");
        deps.add("python-dateutil==2.8.2");
        deps.add("requests==2.31.0");
        deps.add("httpx==0.25.2");

        // Security
        deps.add("cryptography==41.0.7");
        deps.add("PyJWT==2.8.0");
        deps.add("django-ratelimit==4.1.0");

        // Monitoring and debugging
        deps.add("django-debug-toolbar==4.2.0");
        deps.add("django-silk==5.0.4");
        deps.add("django-extensions==3.2.3");
        deps.add("sentry-sdk==1.39.1");

        // Testing
        deps.add("pytest==7.4.3");
        deps.add("pytest-django==4.7.0");
        deps.add("pytest-cov==4.1.0");
        deps.add("factory-boy==3.3.0");
        deps.add("faker==20.1.0");

        // Add type-specific dependencies
        switch (type) {
            case MACHINE_LEARNING:
                deps.add("pandas==2.1.4");
                deps.add("numpy==1.26.2");
                deps.add("scikit-learn==1.3.2");
                deps.add("tensorflow==2.15.0");
                deps.add("torch==2.1.2");
                deps.add("django-pandas==0.6.6");
                deps.add("matplotlib==3.8.2");
                deps.add("seaborn==0.13.0");
                break;
            case ECOMMERCE:
                deps.add("stripe==7.9.0");
                deps.add("django-money==3.4.1");
                deps.add("django-countries==7.5.1");
                deps.add("django-phonenumber-field==7.3.0");
                break;
            case CMS:
                deps.add("django-ckeditor==6.7.0");
                deps.add("django-taggit==5.0.1");
                deps.add("django-mptt==0.15.0");
                break;
            case MICROSERVICE:
                deps.add("channels==4.0.0");
                deps.add("channels-redis==4.1.0");
                deps.add("daphne==4.0.0");
                deps.add("grpcio==1.60.0");
                break;
        }

        // Server
        deps.add("gunicorn==21.2.0");
        deps.add("uvicorn==0.25.0");

        return String.join("\n", deps);
    }

    private String generateManagePy(String projectName) {
        return "#!/usr/bin/env python\n" +
               "\"\"\"Django's command-line utility for administrative tasks.\"\"\"\n" +
               "import os\n" +
               "import sys\n\n" +
               "if __name__ == '__main__':\n" +
               "    os.environ.setdefault('DJANGO_SETTINGS_MODULE', '" + projectName + ".settings')\n" +
               "    try:\n" +
               "        from django.core.management import execute_from_command_line\n" +
               "    except ImportError as exc:\n" +
               "        raise ImportError(\n" +
               "            \"Couldn't import Django. Are you sure it's installed and \"\n" +
               "            \"available on your PYTHONPATH environment variable? Did you \"\n" +
               "            \"forget to activate a virtual environment?\"\n" +
               "        ) from exc\n" +
               "    execute_from_command_line(sys.argv)\n";
    }

    private String generateDockerfile(DjangoProjectType type) {
        return "FROM python:3.11-slim\n\n" +
               "# Set environment variables\n" +
               "ENV PYTHONDONTWRITEBYTECODE=1\n" +
               "ENV PYTHONUNBUFFERED=1\n\n" +
               "# Set work directory\n" +
               "WORKDIR /app\n\n" +
               "# Install system dependencies\n" +
               "RUN apt-get update && apt-get install -y \\\n" +
               "    build-essential \\\n" +
               "    libpq-dev \\\n" +
               "    netcat-traditional \\\n" +
               "    && rm -rf /var/lib/apt/lists/*\n\n" +
               "# Install Python dependencies\n" +
               "COPY requirements.txt .\n" +
               "RUN pip install --no-cache-dir -r requirements.txt\n\n" +
               "# Copy project\n" +
               "COPY . .\n\n" +
               "# Run collectstatic\n" +
               "RUN python manage.py collectstatic --noinput\n\n" +
               "# Add and run as non-root user\n" +
               "RUN useradd -m -u 1000 django && chown -R django:django /app\n" +
               "USER django\n\n" +
               "# Run the application\n" +
               "CMD [\"gunicorn\", \"--bind\", \"0.0.0.0:8000\", \"--workers\", \"4\", \"--threads\", \"4\", \"project.wsgi:application\"]\n";
    }

    // Add remaining helper methods...
    private String generateDockerCompose(String projectName, DjangoProjectType type) {
        return "version: '3.9'\n\n" +
               "services:\n" +
               "  db:\n" +
               "    image: postgres:16-alpine\n" +
               "    volumes:\n" +
               "      - postgres_data:/var/lib/postgresql/data\n" +
               "    environment:\n" +
               "      - POSTGRES_DB=" + projectName + "_db\n" +
               "      - POSTGRES_USER=postgres\n" +
               "      - POSTGRES_PASSWORD=postgres\n" +
               "    ports:\n" +
               "      - \"5432:5432\"\n\n" +
               "  redis:\n" +
               "    image: redis:7-alpine\n" +
               "    ports:\n" +
               "      - \"6379:6379\"\n\n" +
               "  web:\n" +
               "    build: .\n" +
               "    command: python manage.py runserver 0.0.0.0:8000\n" +
               "    volumes:\n" +
               "      - .:/app\n" +
               "    ports:\n" +
               "      - \"8000:8000\"\n" +
               "    environment:\n" +
               "      - DEBUG=True\n" +
               "      - DATABASE_URL=postgresql://postgres:postgres@db:5432/" + projectName + "_db\n" +
               "      - REDIS_URL=redis://redis:6379/0\n" +
               "    depends_on:\n" +
               "      - db\n" +
               "      - redis\n\n" +
               "  celery:\n" +
               "    build: .\n" +
               "    command: celery -A " + projectName + " worker -l info\n" +
               "    volumes:\n" +
               "      - .:/app\n" +
               "    environment:\n" +
               "      - DATABASE_URL=postgresql://postgres:postgres@db:5432/" + projectName + "_db\n" +
               "      - REDIS_URL=redis://redis:6379/0\n" +
               "    depends_on:\n" +
               "      - db\n" +
               "      - redis\n\n" +
               "  celery-beat:\n" +
               "    build: .\n" +
               "    command: celery -A " + projectName + " beat -l info\n" +
               "    volumes:\n" +
               "      - .:/app\n" +
               "    environment:\n" +
               "      - DATABASE_URL=postgresql://postgres:postgres@db:5432/" + projectName + "_db\n" +
               "      - REDIS_URL=redis://redis:6379/0\n" +
               "    depends_on:\n" +
               "      - db\n" +
               "      - redis\n\n" +
               "volumes:\n" +
               "  postgres_data:\n";
    }

    // Full implementations for authentication methods
    private String generateAuthViews() {
        return "from django.contrib.auth import authenticate, login, logout\n" +
               "from django.contrib.auth.decorators import login_required\n" +
               "from django.shortcuts import render, redirect\n" +
               "from django.views.generic import CreateView, UpdateView\n" +
               "from django.urls import reverse_lazy\n" +
               "from django.contrib import messages\n" +
               "from django.utils.decorators import method_decorator\n" +
               "from django.views.decorators.csrf import csrf_exempt\n" +
               "from django.http import JsonResponse\n" +
               "from rest_framework import status, generics\n" +
               "from rest_framework.decorators import api_view, permission_classes\n" +
               "from rest_framework.permissions import AllowAny, IsAuthenticated\n" +
               "from rest_framework.response import Response\n" +
               "from rest_framework.views import APIView\n" +
               "from rest_framework_simplejwt.views import TokenObtainPairView, TokenRefreshView\n" +
               "from rest_framework_simplejwt.tokens import RefreshToken\n" +
               "from .serializers import (\n" +
               "    UserRegistrationSerializer, UserSerializer, \n" +
               "    ChangePasswordSerializer, PasswordResetSerializer,\n" +
               "    EmailVerificationSerializer\n" +
               ")\n" +
               "from .models import CustomUser\n" +
               "from .tokens import account_activation_token\n" +
               "from django.contrib.sites.shortcuts import get_current_site\n" +
               "from django.template.loader import render_to_string\n" +
               "from django.utils.http import urlsafe_base64_encode, urlsafe_base64_decode\n" +
               "from django.utils.encoding import force_bytes, force_str\n" +
               "from django.core.mail import EmailMessage\n" +
               "import logging\n\n" +
               "logger = logging.getLogger(__name__)\n\n" +
               "class RegisterView(generics.CreateAPIView):\n" +
               "    \"\"\"User registration view.\"\"\"\n" +
               "    queryset = CustomUser.objects.all()\n" +
               "    serializer_class = UserRegistrationSerializer\n" +
               "    permission_classes = [AllowAny]\n\n" +
               "    def create(self, request, *args, **kwargs):\n" +
               "        serializer = self.get_serializer(data=request.data)\n" +
               "        serializer.is_valid(raise_exception=True)\n" +
               "        user = serializer.save()\n" +
               "        \n" +
               "        # Send activation email\n" +
               "        current_site = get_current_site(request)\n" +
               "        mail_subject = 'Activate your account'\n" +
               "        message = render_to_string('authentication/activation_email.html', {\n" +
               "            'user': user,\n" +
               "            'domain': current_site.domain,\n" +
               "            'uid': urlsafe_base64_encode(force_bytes(user.pk)),\n" +
               "            'token': account_activation_token.make_token(user),\n" +
               "        })\n" +
               "        email = EmailMessage(mail_subject, message, to=[user.email])\n" +
               "        email.send()\n" +
               "        \n" +
               "        return Response(\n" +
               "            {'message': 'User created successfully. Please check your email to activate your account.'},\n" +
               "            status=status.HTTP_201_CREATED\n" +
               "        )\n\n" +
               "class LoginView(TokenObtainPairView):\n" +
               "    \"\"\"Custom login view with JWT tokens.\"\"\"\n" +
               "    permission_classes = [AllowAny]\n\n" +
               "    def post(self, request, *args, **kwargs):\n" +
               "        response = super().post(request, *args, **kwargs)\n" +
               "        if response.status_code == 200:\n" +
               "            user = CustomUser.objects.get(email=request.data['email'])\n" +
               "            serializer = UserSerializer(user)\n" +
               "            response.data['user'] = serializer.data\n" +
               "        return response\n\n" +
               "class LogoutView(APIView):\n" +
               "    \"\"\"Logout view with token blacklisting.\"\"\"\n" +
               "    permission_classes = [IsAuthenticated]\n\n" +
               "    def post(self, request):\n" +
               "        try:\n" +
               "            refresh_token = request.data['refresh']\n" +
               "            token = RefreshToken(refresh_token)\n" +
               "            token.blacklist()\n" +
               "            return Response(\n" +
               "                {'message': 'Successfully logged out'},\n" +
               "                status=status.HTTP_200_OK\n" +
               "            )\n" +
               "        except Exception as e:\n" +
               "            logger.error(f'Logout error: {str(e)}')\n" +
               "            return Response(\n" +
               "                {'error': 'Invalid token'},\n" +
               "                status=status.HTTP_400_BAD_REQUEST\n" +
               "            )\n\n" +
               "class ProfileView(generics.RetrieveUpdateAPIView):\n" +
               "    \"\"\"User profile view.\"\"\"\n" +
               "    serializer_class = UserSerializer\n" +
               "    permission_classes = [IsAuthenticated]\n\n" +
               "    def get_object(self):\n" +
               "        return self.request.user\n\n" +
               "class ChangePasswordView(generics.UpdateAPIView):\n" +
               "    \"\"\"Change password view.\"\"\"\n" +
               "    serializer_class = ChangePasswordSerializer\n" +
               "    permission_classes = [IsAuthenticated]\n\n" +
               "    def get_object(self):\n" +
               "        return self.request.user\n\n" +
               "    def update(self, request, *args, **kwargs):\n" +
               "        serializer = self.get_serializer(data=request.data)\n" +
               "        serializer.is_valid(raise_exception=True)\n" +
               "        \n" +
               "        user = self.get_object()\n" +
               "        if not user.check_password(serializer.data.get('old_password')):\n" +
               "            return Response(\n" +
               "                {'error': 'Incorrect old password'},\n" +
               "                status=status.HTTP_400_BAD_REQUEST\n" +
               "            )\n" +
               "        \n" +
               "        user.set_password(serializer.data.get('new_password'))\n" +
               "        user.save()\n" +
               "        \n" +
               "        return Response(\n" +
               "            {'message': 'Password changed successfully'},\n" +
               "            status=status.HTTP_200_OK\n" +
               "        )\n\n" +
               "class PasswordResetRequestView(generics.GenericAPIView):\n" +
               "    \"\"\"Request password reset email.\"\"\"\n" +
               "    serializer_class = PasswordResetSerializer\n" +
               "    permission_classes = [AllowAny]\n\n" +
               "    def post(self, request):\n" +
               "        serializer = self.get_serializer(data=request.data)\n" +
               "        serializer.is_valid(raise_exception=True)\n" +
               "        \n" +
               "        email = serializer.data['email']\n" +
               "        try:\n" +
               "            user = CustomUser.objects.get(email=email)\n" +
               "            # Send password reset email\n" +
               "            current_site = get_current_site(request)\n" +
               "            mail_subject = 'Reset your password'\n" +
               "            message = render_to_string('authentication/password_reset_email.html', {\n" +
               "                'user': user,\n" +
               "                'domain': current_site.domain,\n" +
               "                'uid': urlsafe_base64_encode(force_bytes(user.pk)),\n" +
               "                'token': account_activation_token.make_token(user),\n" +
               "            })\n" +
               "            email_message = EmailMessage(mail_subject, message, to=[email])\n" +
               "            email_message.send()\n" +
               "        except CustomUser.DoesNotExist:\n" +
               "            pass  # Don't reveal if email exists\n" +
               "        \n" +
               "        return Response(\n" +
               "            {'message': 'If the email exists, a password reset link has been sent'},\n" +
               "            status=status.HTTP_200_OK\n" +
               "        )\n\n" +
               "class EmailVerificationView(APIView):\n" +
               "    \"\"\"Verify email with token.\"\"\"\n" +
               "    permission_classes = [AllowAny]\n\n" +
               "    def get(self, request, uidb64, token):\n" +
               "        try:\n" +
               "            uid = force_str(urlsafe_base64_decode(uidb64))\n" +
               "            user = CustomUser.objects.get(pk=uid)\n" +
               "        except (TypeError, ValueError, OverflowError, CustomUser.DoesNotExist):\n" +
               "            user = None\n" +
               "        \n" +
               "        if user is not None and account_activation_token.check_token(user, token):\n" +
               "            user.is_verified = True\n" +
               "            user.save()\n" +
               "            return Response(\n" +
               "                {'message': 'Email verified successfully'},\n" +
               "                status=status.HTTP_200_OK\n" +
               "            )\n" +
               "        \n" +
               "        return Response(\n" +
               "            {'error': 'Invalid verification link'},\n" +
               "            status=status.HTTP_400_BAD_REQUEST\n" +
               "        )\n\n" +
               "@api_view(['POST'])\n" +
               "@permission_classes([IsAuthenticated])\n" +
               "def resend_verification_email(request):\n" +
               "    \"\"\"Resend email verification.\"\"\"\n" +
               "    user = request.user\n" +
               "    if user.is_verified:\n" +
               "        return Response(\n" +
               "            {'message': 'Email already verified'},\n" +
               "            status=status.HTTP_400_BAD_REQUEST\n" +
               "        )\n" +
               "    \n" +
               "    # Send verification email\n" +
               "    current_site = get_current_site(request)\n" +
               "    mail_subject = 'Verify your email'\n" +
               "    message = render_to_string('authentication/activation_email.html', {\n" +
               "        'user': user,\n" +
               "        'domain': current_site.domain,\n" +
               "        'uid': urlsafe_base64_encode(force_bytes(user.pk)),\n" +
               "        'token': account_activation_token.make_token(user),\n" +
               "    })\n" +
               "    email = EmailMessage(mail_subject, message, to=[user.email])\n" +
               "    email.send()\n" +
               "    \n" +
               "    return Response(\n" +
               "        {'message': 'Verification email sent'},\n" +
               "        status=status.HTTP_200_OK\n" +
               "    )\n";
    }

    private String generateAuthSerializers() {
        return "from rest_framework import serializers\n" +
               "from django.contrib.auth import authenticate\n" +
               "from django.contrib.auth.password_validation import validate_password\n" +
               "from .models import CustomUser\n\n" +
               "class UserSerializer(serializers.ModelSerializer):\n" +
               "    \"\"\"Serializer for user details.\"\"\"\n" +
               "    class Meta:\n" +
               "        model = CustomUser\n" +
               "        fields = [\n" +
               "            'id', 'username', 'email', 'first_name', 'last_name',\n" +
               "            'phone', 'bio', 'avatar', 'date_of_birth', 'is_verified',\n" +
               "            'date_joined', 'last_login'\n" +
               "        ]\n" +
               "        read_only_fields = ['id', 'date_joined', 'last_login', 'is_verified']\n\n" +
               "class UserRegistrationSerializer(serializers.ModelSerializer):\n" +
               "    \"\"\"Serializer for user registration.\"\"\"\n" +
               "    password = serializers.CharField(\n" +
               "        write_only=True,\n" +
               "        required=True,\n" +
               "        validators=[validate_password]\n" +
               "    )\n" +
               "    password2 = serializers.CharField(write_only=True, required=True)\n" +
               "    \n" +
               "    class Meta:\n" +
               "        model = CustomUser\n" +
               "        fields = [\n" +
               "            'username', 'email', 'password', 'password2',\n" +
               "            'first_name', 'last_name', 'phone'\n" +
               "        ]\n" +
               "        extra_kwargs = {\n" +
               "            'first_name': {'required': True},\n" +
               "            'last_name': {'required': True},\n" +
               "            'email': {'required': True}\n" +
               "        }\n" +
               "    \n" +
               "    def validate(self, attrs):\n" +
               "        if attrs['password'] != attrs['password2']:\n" +
               "            raise serializers.ValidationError(\n" +
               "                {'password': 'Password fields didn\\'t match.'}\n" +
               "            )\n" +
               "        return attrs\n" +
               "    \n" +
               "    def validate_email(self, value):\n" +
               "        if CustomUser.objects.filter(email=value).exists():\n" +
               "            raise serializers.ValidationError('Email already registered.')\n" +
               "        return value\n" +
               "    \n" +
               "    def create(self, validated_data):\n" +
               "        validated_data.pop('password2')\n" +
               "        user = CustomUser.objects.create_user(**validated_data)\n" +
               "        return user\n\n" +
               "class ChangePasswordSerializer(serializers.Serializer):\n" +
               "    \"\"\"Serializer for password change.\"\"\"\n" +
               "    old_password = serializers.CharField(required=True)\n" +
               "    new_password = serializers.CharField(\n" +
               "        required=True,\n" +
               "        validators=[validate_password]\n" +
               "    )\n" +
               "    new_password2 = serializers.CharField(required=True)\n" +
               "    \n" +
               "    def validate(self, attrs):\n" +
               "        if attrs['new_password'] != attrs['new_password2']:\n" +
               "            raise serializers.ValidationError(\n" +
               "                {'new_password': 'Password fields didn\\'t match.'}\n" +
               "            )\n" +
               "        return attrs\n\n" +
               "class PasswordResetSerializer(serializers.Serializer):\n" +
               "    \"\"\"Serializer for password reset request.\"\"\"\n" +
               "    email = serializers.EmailField(required=True)\n\n" +
               "class PasswordResetConfirmSerializer(serializers.Serializer):\n" +
               "    \"\"\"Serializer for password reset confirmation.\"\"\"\n" +
               "    new_password = serializers.CharField(\n" +
               "        required=True,\n" +
               "        validators=[validate_password]\n" +
               "    )\n" +
               "    new_password2 = serializers.CharField(required=True)\n" +
               "    uid = serializers.CharField(required=True)\n" +
               "    token = serializers.CharField(required=True)\n" +
               "    \n" +
               "    def validate(self, attrs):\n" +
               "        if attrs['new_password'] != attrs['new_password2']:\n" +
               "            raise serializers.ValidationError(\n" +
               "                {'new_password': 'Password fields didn\\'t match.'}\n" +
               "            )\n" +
               "        return attrs\n\n" +
               "class EmailVerificationSerializer(serializers.Serializer):\n" +
               "    \"\"\"Serializer for email verification.\"\"\"\n" +
               "    uid = serializers.CharField(required=True)\n" +
               "    token = serializers.CharField(required=True)\n\n" +
               "class LoginSerializer(serializers.Serializer):\n" +
               "    \"\"\"Serializer for user login.\"\"\"\n" +
               "    email = serializers.EmailField(required=True)\n" +
               "    password = serializers.CharField(required=True, write_only=True)\n" +
               "    \n" +
               "    def validate(self, attrs):\n" +
               "        email = attrs.get('email')\n" +
               "        password = attrs.get('password')\n" +
               "        \n" +
               "        if email and password:\n" +
               "            user = authenticate(email=email, password=password)\n" +
               "            if not user:\n" +
               "                raise serializers.ValidationError('Invalid credentials.')\n" +
               "            if not user.is_active:\n" +
               "                raise serializers.ValidationError('User account is disabled.')\n" +
               "            attrs['user'] = user\n" +
               "        else:\n" +
               "            raise serializers.ValidationError('Email and password are required.')\n" +
               "        \n" +
               "        return attrs\n\n" +
               "class SocialAuthSerializer(serializers.Serializer):\n" +
               "    \"\"\"Serializer for social authentication.\"\"\"\n" +
               "    provider = serializers.CharField(required=True)\n" +
               "    access_token = serializers.CharField(required=True)\n";
    }

    private String generateAuthUrls() {
        return "from django.urls import path\n" +
               "from rest_framework_simplejwt.views import TokenRefreshView, TokenVerifyView\n" +
               "from .views import (\n" +
               "    RegisterView, LoginView, LogoutView, ProfileView,\n" +
               "    ChangePasswordView, PasswordResetRequestView,\n" +
               "    EmailVerificationView, resend_verification_email\n" +
               ")\n\n" +
               "app_name = 'authentication'\n\n" +
               "urlpatterns = [\n" +
               "    # Authentication endpoints\n" +
               "    path('register/', RegisterView.as_view(), name='register'),\n" +
               "    path('login/', LoginView.as_view(), name='login'),\n" +
               "    path('logout/', LogoutView.as_view(), name='logout'),\n" +
               "    \n" +
               "    # Token management\n" +
               "    path('token/refresh/', TokenRefreshView.as_view(), name='token_refresh'),\n" +
               "    path('token/verify/', TokenVerifyView.as_view(), name='token_verify'),\n" +
               "    \n" +
               "    # Profile management\n" +
               "    path('profile/', ProfileView.as_view(), name='profile'),\n" +
               "    path('change-password/', ChangePasswordView.as_view(), name='change_password'),\n" +
               "    \n" +
               "    # Password reset\n" +
               "    path('password-reset/', PasswordResetRequestView.as_view(), name='password_reset'),\n" +
               "    path('password-reset-confirm/<uidb64>/<token>/', \n" +
               "         PasswordResetRequestView.as_view(), name='password_reset_confirm'),\n" +
               "    \n" +
               "    # Email verification\n" +
               "    path('verify-email/<uidb64>/<token>/', \n" +
               "         EmailVerificationView.as_view(), name='verify_email'),\n" +
               "    path('resend-verification/', \n" +
               "         resend_verification_email, name='resend_verification'),\n" +
               "]\n";
    }

    private String generateAuthBackends() {
        return "from django.contrib.auth.backends import ModelBackend\n" +
               "from django.contrib.auth import get_user_model\n" +
               "from django.db.models import Q\n\n" +
               "User = get_user_model()\n\n" +
               "class EmailBackend(ModelBackend):\n" +
               "    \"\"\"Custom authentication backend that allows login with email.\"\"\"\n" +
               "    \n" +
               "    def authenticate(self, request, username=None, password=None, **kwargs):\n" +
               "        email = kwargs.get('email') or username\n" +
               "        \n" +
               "        if email is None or password is None:\n" +
               "            return None\n" +
               "        \n" +
               "        try:\n" +
               "            # Try to find user by email or username\n" +
               "            user = User.objects.get(\n" +
               "                Q(email__iexact=email) | Q(username__iexact=email)\n" +
               "            )\n" +
               "        except User.DoesNotExist:\n" +
               "            return None\n" +
               "        except User.MultipleObjectsReturned:\n" +
               "            # Return None if multiple users found\n" +
               "            return None\n" +
               "        \n" +
               "        # Check password\n" +
               "        if user.check_password(password) and self.user_can_authenticate(user):\n" +
               "            return user\n" +
               "        \n" +
               "        return None\n" +
               "    \n" +
               "    def get_user(self, user_id):\n" +
               "        try:\n" +
               "            return User.objects.get(pk=user_id)\n" +
               "        except User.DoesNotExist:\n" +
               "            return None\n\n" +
               "class SocialAuthBackend(ModelBackend):\n" +
               "    \"\"\"Backend for social authentication (Google, Facebook, etc.).\"\"\"\n" +
               "    \n" +
               "    def authenticate(self, request, provider=None, access_token=None, **kwargs):\n" +
               "        if not provider or not access_token:\n" +
               "            return None\n" +
               "        \n" +
               "        # Verify token with provider\n" +
               "        user_info = self.verify_social_token(provider, access_token)\n" +
               "        if not user_info:\n" +
               "            return None\n" +
               "        \n" +
               "        # Get or create user\n" +
               "        email = user_info.get('email')\n" +
               "        if not email:\n" +
               "            return None\n" +
               "        \n" +
               "        try:\n" +
               "            user = User.objects.get(email=email)\n" +
               "        except User.DoesNotExist:\n" +
               "            # Create new user from social login\n" +
               "            user = User.objects.create_user(\n" +
               "                email=email,\n" +
               "                username=email.split('@')[0],\n" +
               "                first_name=user_info.get('first_name', ''),\n" +
               "                last_name=user_info.get('last_name', ''),\n" +
               "                is_verified=True  # Social logins are pre-verified\n" +
               "            )\n" +
               "        \n" +
               "        return user if self.user_can_authenticate(user) else None\n" +
               "    \n" +
               "    def verify_social_token(self, provider, access_token):\n" +
               "        \"\"\"Verify token with social provider and return user info.\"\"\"\n" +
               "        # Implementation would vary by provider\n" +
               "        # This is a placeholder for actual provider verification\n" +
               "        if provider == 'google':\n" +
               "            return self.verify_google_token(access_token)\n" +
               "        elif provider == 'facebook':\n" +
               "            return self.verify_facebook_token(access_token)\n" +
               "        elif provider == 'github':\n" +
               "            return self.verify_github_token(access_token)\n" +
               "        return None\n" +
               "    \n" +
               "    def verify_google_token(self, access_token):\n" +
               "        \"\"\"Verify Google OAuth token.\"\"\"\n" +
               "        # In production, use Google API to verify token\n" +
               "        # import requests\n" +
               "        # response = requests.get(\n" +
               "        #     f'https://www.googleapis.com/oauth2/v3/tokeninfo?access_token={access_token}'\n" +
               "        # )\n" +
               "        # if response.status_code == 200:\n" +
               "        #     return response.json()\n" +
               "        return None\n" +
               "    \n" +
               "    def verify_facebook_token(self, access_token):\n" +
               "        \"\"\"Verify Facebook OAuth token.\"\"\"\n" +
               "        # In production, use Facebook Graph API\n" +
               "        return None\n" +
               "    \n" +
               "    def verify_github_token(self, access_token):\n" +
               "        \"\"\"Verify GitHub OAuth token.\"\"\"\n" +
               "        # In production, use GitHub API\n" +
               "        return None\n";
    }

    private String generateTokens() {
        return "from django.contrib.auth.tokens import PasswordResetTokenGenerator\n" +
               "from django.utils import six\n" +
               "import hashlib\n\n" +
               "class AccountActivationTokenGenerator(PasswordResetTokenGenerator):\n" +
               "    \"\"\"Token generator for email verification.\"\"\"\n" +
               "    \n" +
               "    def _make_hash_value(self, user, timestamp):\n" +
               "        # Include user's email verification status in the hash\n" +
               "        return (\n" +
               "            six.text_type(user.pk) + six.text_type(timestamp) +\n" +
               "            six.text_type(user.is_verified)\n" +
               "        )\n\n" +
               "class PasswordResetToken(PasswordResetTokenGenerator):\n" +
               "    \"\"\"Custom password reset token generator.\"\"\"\n" +
               "    \n" +
               "    def _make_hash_value(self, user, timestamp):\n" +
               "        # Include user's password hash to invalidate token after password change\n" +
               "        login_timestamp = '' if user.last_login is None else user.last_login.replace(\n" +
               "            microsecond=0, tzinfo=None\n" +
               "        )\n" +
               "        return (\n" +
               "            six.text_type(user.pk) + \n" +
               "            user.password +\n" +
               "            six.text_type(login_timestamp) + \n" +
               "            six.text_type(timestamp)\n" +
               "        )\n\n" +
               "# Token instances\n" +
               "account_activation_token = AccountActivationTokenGenerator()\n" +
               "password_reset_token = PasswordResetToken()\n\n" +
               "def generate_api_key(user):\n" +
               "    \"\"\"Generate a unique API key for a user.\"\"\"\n" +
               "    raw_key = f'{user.id}-{user.email}-{user.date_joined}'\n" +
               "    return hashlib.sha256(raw_key.encode()).hexdigest()\n\n" +
               "def generate_refresh_token_key(user):\n" +
               "    \"\"\"Generate a unique key for refresh token storage.\"\"\"\n" +
               "    import uuid\n" +
               "    from datetime import datetime\n" +
               "    \n" +
               "    unique_id = uuid.uuid4().hex\n" +
               "    timestamp = datetime.now().timestamp()\n" +
               "    raw_key = f'{user.id}-{unique_id}-{timestamp}'\n" +
               "    \n" +
               "    return hashlib.sha256(raw_key.encode()).hexdigest()\n\n" +
               "class TokenBlacklist:\n" +
               "    \"\"\"Manager for blacklisted tokens.\"\"\"\n" +
               "    \n" +
               "    @staticmethod\n" +
               "    def add_to_blacklist(token):\n" +
               "        \"\"\"Add a token to the blacklist.\"\"\"\n" +
               "        from django.core.cache import cache\n" +
               "        from datetime import datetime, timedelta\n" +
               "        \n" +
               "        # Store in cache with expiration\n" +
               "        expiration = timedelta(days=7)  # Match refresh token lifetime\n" +
               "        cache.set(\n" +
               "            f'blacklist_token_{token}',\n" +
               "            True,\n" +
               "            expiration.total_seconds()\n" +
               "        )\n" +
               "    \n" +
               "    @staticmethod\n" +
               "    def is_blacklisted(token):\n" +
               "        \"\"\"Check if a token is blacklisted.\"\"\"\n" +
               "        from django.core.cache import cache\n" +
               "        return cache.get(f'blacklist_token_{token}', False)\n";
    }
    private String generateCoreModels() {
        return "from django.db import models\n" +
               "from django.contrib.auth import get_user_model\n" +
               "from django.utils.translation import gettext_lazy as _\n" +
               "from django.utils import timezone\n" +
               "import uuid\n\n" +
               "User = get_user_model()\n\n" +
               "class TimeStampedModel(models.Model):\n" +
               "    \"\"\"Abstract base model with timestamps.\"\"\"\n" +
               "    created_at = models.DateTimeField(auto_now_add=True)\n" +
               "    updated_at = models.DateTimeField(auto_now=True)\n" +
               "    \n" +
               "    class Meta:\n" +
               "        abstract = True\n\n" +
               "class UUIDModel(models.Model):\n" +
               "    \"\"\"Abstract base model with UUID primary key.\"\"\"\n" +
               "    id = models.UUIDField(primary_key=True, default=uuid.uuid4, editable=False)\n" +
               "    \n" +
               "    class Meta:\n" +
               "        abstract = True\n\n" +
               "class Category(TimeStampedModel):\n" +
               "    \"\"\"Category model for organizing content.\"\"\"\n" +
               "    name = models.CharField(max_length=100, unique=True)\n" +
               "    slug = models.SlugField(unique=True)\n" +
               "    description = models.TextField(blank=True)\n" +
               "    parent = models.ForeignKey(\n" +
               "        'self', \n" +
               "        null=True, \n" +
               "        blank=True, \n" +
               "        on_delete=models.CASCADE,\n" +
               "        related_name='children'\n" +
               "    )\n" +
               "    is_active = models.BooleanField(default=True)\n" +
               "    order = models.IntegerField(default=0)\n" +
               "    \n" +
               "    class Meta:\n" +
               "        verbose_name_plural = 'Categories'\n" +
               "        ordering = ['order', 'name']\n" +
               "    \n" +
               "    def __str__(self):\n" +
               "        return self.name\n\n" +
               "class Tag(TimeStampedModel):\n" +
               "    \"\"\"Tag model for content tagging.\"\"\"\n" +
               "    name = models.CharField(max_length=50, unique=True)\n" +
               "    slug = models.SlugField(unique=True)\n" +
               "    \n" +
               "    def __str__(self):\n" +
               "        return self.name\n\n" +
               "class Article(TimeStampedModel, UUIDModel):\n" +
               "    \"\"\"Article model for blog/news content.\"\"\"\n" +
               "    title = models.CharField(max_length=200)\n" +
               "    slug = models.SlugField(unique=True)\n" +
               "    author = models.ForeignKey(User, on_delete=models.CASCADE, related_name='articles')\n" +
               "    content = models.TextField()\n" +
               "    excerpt = models.TextField(max_length=500, blank=True)\n" +
               "    featured_image = models.ImageField(upload_to='articles/', blank=True, null=True)\n" +
               "    category = models.ForeignKey(Category, on_delete=models.SET_NULL, null=True, blank=True)\n" +
               "    tags = models.ManyToManyField(Tag, blank=True)\n" +
               "    status = models.CharField(\n" +
               "        max_length=20,\n" +
               "        choices=[\n" +
               "            ('draft', 'Draft'),\n" +
               "            ('published', 'Published'),\n" +
               "            ('archived', 'Archived'),\n" +
               "        ],\n" +
               "        default='draft'\n" +
               "    )\n" +
               "    published_at = models.DateTimeField(null=True, blank=True)\n" +
               "    views_count = models.PositiveIntegerField(default=0)\n" +
               "    is_featured = models.BooleanField(default=False)\n" +
               "    \n" +
               "    class Meta:\n" +
               "        ordering = ['-published_at']\n" +
               "    \n" +
               "    def __str__(self):\n" +
               "        return self.title\n" +
               "    \n" +
               "    def save(self, *args, **kwargs):\n" +
               "        if self.status == 'published' and not self.published_at:\n" +
               "            self.published_at = timezone.now()\n" +
               "        super().save(*args, **kwargs)\n\n" +
               "class Comment(TimeStampedModel):\n" +
               "    \"\"\"Comment model for article comments.\"\"\"\n" +
               "    article = models.ForeignKey(Article, on_delete=models.CASCADE, related_name='comments')\n" +
               "    author = models.ForeignKey(User, on_delete=models.CASCADE)\n" +
               "    content = models.TextField()\n" +
               "    parent = models.ForeignKey(\n" +
               "        'self',\n" +
               "        null=True,\n" +
               "        blank=True,\n" +
               "        on_delete=models.CASCADE,\n" +
               "        related_name='replies'\n" +
               "    )\n" +
               "    is_approved = models.BooleanField(default=True)\n" +
               "    \n" +
               "    class Meta:\n" +
               "        ordering = ['-created_at']\n" +
               "    \n" +
               "    def __str__(self):\n" +
               "        return f'Comment by {self.author.username} on {self.article.title}'\n\n" +
               "class Notification(TimeStampedModel):\n" +
               "    \"\"\"Notification model for user notifications.\"\"\"\n" +
               "    recipient = models.ForeignKey(User, on_delete=models.CASCADE, related_name='notifications')\n" +
               "    title = models.CharField(max_length=200)\n" +
               "    message = models.TextField()\n" +
               "    notification_type = models.CharField(\n" +
               "        max_length=50,\n" +
               "        choices=[\n" +
               "            ('info', 'Information'),\n" +
               "            ('success', 'Success'),\n" +
               "            ('warning', 'Warning'),\n" +
               "            ('error', 'Error'),\n" +
               "        ],\n" +
               "        default='info'\n" +
               "    )\n" +
               "    is_read = models.BooleanField(default=False)\n" +
               "    link = models.URLField(blank=True, null=True)\n" +
               "    \n" +
               "    class Meta:\n" +
               "        ordering = ['-created_at']\n" +
               "    \n" +
               "    def __str__(self):\n" +
               "        return f'{self.title} - {self.recipient.username}'\n";
    }

    private String generateCoreViews() {
        return "from django.shortcuts import render, get_object_or_404, redirect\n" +
               "from django.views.generic import ListView, DetailView, CreateView, UpdateView, DeleteView\n" +
               "from django.contrib.auth.mixins import LoginRequiredMixin, UserPassesTestMixin\n" +
               "from django.contrib.auth.decorators import login_required\n" +
               "from django.contrib import messages\n" +
               "from django.urls import reverse_lazy\n" +
               "from django.db.models import Q, Count, F\n" +
               "from django.core.paginator import Paginator\n" +
               "from django.http import JsonResponse\n" +
               "from django.utils import timezone\n" +
               "from .models import Article, Category, Tag, Comment, Notification\n" +
               "from .forms import ArticleForm, CommentForm\n" +
               "import json\n\n" +
               "class ArticleListView(ListView):\n" +
               "    \"\"\"List view for articles.\"\"\"\n" +
               "    model = Article\n" +
               "    template_name = 'core/article_list.html'\n" +
               "    context_object_name = 'articles'\n" +
               "    paginate_by = 10\n" +
               "    \n" +
               "    def get_queryset(self):\n" +
               "        queryset = Article.objects.filter(status='published')\n" +
               "        \n" +
               "        # Search functionality\n" +
               "        search_query = self.request.GET.get('q')\n" +
               "        if search_query:\n" +
               "            queryset = queryset.filter(\n" +
               "                Q(title__icontains=search_query) |\n" +
               "                Q(content__icontains=search_query) |\n" +
               "                Q(tags__name__icontains=search_query)\n" +
               "            ).distinct()\n" +
               "        \n" +
               "        # Category filter\n" +
               "        category_slug = self.kwargs.get('category_slug')\n" +
               "        if category_slug:\n" +
               "            category = get_object_or_404(Category, slug=category_slug)\n" +
               "            queryset = queryset.filter(category=category)\n" +
               "        \n" +
               "        # Tag filter\n" +
               "        tag_slug = self.kwargs.get('tag_slug')\n" +
               "        if tag_slug:\n" +
               "            tag = get_object_or_404(Tag, slug=tag_slug)\n" +
               "            queryset = queryset.filter(tags=tag)\n" +
               "        \n" +
               "        return queryset.select_related('author', 'category').prefetch_related('tags')\n" +
               "    \n" +
               "    def get_context_data(self, **kwargs):\n" +
               "        context = super().get_context_data(**kwargs)\n" +
               "        context['categories'] = Category.objects.filter(is_active=True)\n" +
               "        context['popular_tags'] = Tag.objects.annotate(\n" +
               "            article_count=Count('article')\n" +
               "        ).order_by('-article_count')[:10]\n" +
               "        context['featured_articles'] = Article.objects.filter(\n" +
               "            status='published',\n" +
               "            is_featured=True\n" +
               "        )[:5]\n" +
               "        return context\n\n" +
               "class ArticleDetailView(DetailView):\n" +
               "    \"\"\"Detail view for a single article.\"\"\"\n" +
               "    model = Article\n" +
               "    template_name = 'core/article_detail.html'\n" +
               "    context_object_name = 'article'\n" +
               "    \n" +
               "    def get_object(self):\n" +
               "        article = super().get_object()\n" +
               "        # Increment view count\n" +
               "        Article.objects.filter(pk=article.pk).update(views_count=F('views_count') + 1)\n" +
               "        return article\n" +
               "    \n" +
               "    def get_context_data(self, **kwargs):\n" +
               "        context = super().get_context_data(**kwargs)\n" +
               "        article = self.object\n" +
               "        \n" +
               "        # Get approved comments\n" +
               "        context['comments'] = article.comments.filter(\n" +
               "            is_approved=True,\n" +
               "            parent=None\n" +
               "        ).select_related('author').prefetch_related('replies')\n" +
               "        \n" +
               "        # Get related articles\n" +
               "        context['related_articles'] = Article.objects.filter(\n" +
               "            status='published',\n" +
               "            category=article.category\n" +
               "        ).exclude(id=article.id)[:3]\n" +
               "        \n" +
               "        context['comment_form'] = CommentForm()\n" +
               "        return context\n\n" +
               "class ArticleCreateView(LoginRequiredMixin, CreateView):\n" +
               "    \"\"\"Create view for articles.\"\"\"\n" +
               "    model = Article\n" +
               "    form_class = ArticleForm\n" +
               "    template_name = 'core/article_form.html'\n" +
               "    success_url = reverse_lazy('core:article_list')\n" +
               "    \n" +
               "    def form_valid(self, form):\n" +
               "        form.instance.author = self.request.user\n" +
               "        messages.success(self.request, 'Article created successfully!')\n" +
               "        return super().form_valid(form)\n\n" +
               "class ArticleUpdateView(LoginRequiredMixin, UserPassesTestMixin, UpdateView):\n" +
               "    \"\"\"Update view for articles.\"\"\"\n" +
               "    model = Article\n" +
               "    form_class = ArticleForm\n" +
               "    template_name = 'core/article_form.html'\n" +
               "    \n" +
               "    def test_func(self):\n" +
               "        article = self.get_object()\n" +
               "        return self.request.user == article.author or self.request.user.is_staff\n" +
               "    \n" +
               "    def form_valid(self, form):\n" +
               "        messages.success(self.request, 'Article updated successfully!')\n" +
               "        return super().form_valid(form)\n" +
               "    \n" +
               "    def get_success_url(self):\n" +
               "        return reverse_lazy('core:article_detail', kwargs={'slug': self.object.slug})\n\n" +
               "class ArticleDeleteView(LoginRequiredMixin, UserPassesTestMixin, DeleteView):\n" +
               "    \"\"\"Delete view for articles.\"\"\"\n" +
               "    model = Article\n" +
               "    template_name = 'core/article_confirm_delete.html'\n" +
               "    success_url = reverse_lazy('core:article_list')\n" +
               "    \n" +
               "    def test_func(self):\n" +
               "        article = self.get_object()\n" +
               "        return self.request.user == article.author or self.request.user.is_staff\n" +
               "    \n" +
               "    def delete(self, request, *args, **kwargs):\n" +
               "        messages.success(request, 'Article deleted successfully!')\n" +
               "        return super().delete(request, *args, **kwargs)\n\n" +
               "@login_required\n" +
               "def add_comment(request, slug):\n" +
               "    \"\"\"Add comment to an article.\"\"\"\n" +
               "    article = get_object_or_404(Article, slug=slug)\n" +
               "    \n" +
               "    if request.method == 'POST':\n" +
               "        form = CommentForm(request.POST)\n" +
               "        if form.is_valid():\n" +
               "            comment = form.save(commit=False)\n" +
               "            comment.article = article\n" +
               "            comment.author = request.user\n" +
               "            \n" +
               "            # Handle reply to comment\n" +
               "            parent_id = request.POST.get('parent_id')\n" +
               "            if parent_id:\n" +
               "                parent_comment = get_object_or_404(Comment, id=parent_id)\n" +
               "                comment.parent = parent_comment\n" +
               "            \n" +
               "            comment.save()\n" +
               "            messages.success(request, 'Comment added successfully!')\n" +
               "            \n" +
               "            # Create notification for article author\n" +
               "            if article.author != request.user:\n" +
               "                Notification.objects.create(\n" +
               "                    recipient=article.author,\n" +
               "                    title='New comment on your article',\n" +
               "                    message=f'{request.user.username} commented on \"{article.title}\"',\n" +
               "                    notification_type='info',\n" +
               "                    link=f'/articles/{article.slug}/#comment-{comment.id}'\n" +
               "                )\n" +
               "    \n" +
               "    return redirect('core:article_detail', slug=slug)\n\n" +
               "@login_required\n" +
               "def mark_notification_read(request, notification_id):\n" +
               "    \"\"\"Mark notification as read.\"\"\"\n" +
               "    notification = get_object_or_404(\n" +
               "        Notification,\n" +
               "        id=notification_id,\n" +
               "        recipient=request.user\n" +
               "    )\n" +
               "    notification.is_read = True\n" +
               "    notification.save()\n" +
               "    \n" +
               "    if request.is_ajax():\n" +
               "        return JsonResponse({'status': 'success'})\n" +
               "    \n" +
               "    return redirect('core:notifications')\n\n" +
               "@login_required\n" +
               "def notifications_view(request):\n" +
               "    \"\"\"View all notifications for the current user.\"\"\"\n" +
               "    notifications = request.user.notifications.all()\n" +
               "    \n" +
               "    # Mark all as read if requested\n" +
               "    if request.method == 'POST' and 'mark_all_read' in request.POST:\n" +
               "        notifications.filter(is_read=False).update(is_read=True)\n" +
               "        messages.success(request, 'All notifications marked as read')\n" +
               "        return redirect('core:notifications')\n" +
               "    \n" +
               "    paginator = Paginator(notifications, 20)\n" +
               "    page_number = request.GET.get('page')\n" +
               "    page_obj = paginator.get_page(page_number)\n" +
               "    \n" +
               "    context = {\n" +
               "        'notifications': page_obj,\n" +
               "        'unread_count': notifications.filter(is_read=False).count()\n" +
               "    }\n" +
               "    \n" +
               "    return render(request, 'core/notifications.html', context)\n";
    }

    private String generateCoreForms() {
        return "from django import forms\n" +
               "from django.core.exceptions import ValidationError\n" +
               "from .models import Article, Comment, Category, Tag\n" +
               "from django.utils.text import slugify\n\n" +
               "class ArticleForm(forms.ModelForm):\n" +
               "    \"\"\"Form for creating and editing articles.\"\"\"\n" +
               "    \n" +
               "    tags = forms.CharField(\n" +
               "        required=False,\n" +
               "        widget=forms.TextInput(attrs={\n" +
               "            'class': 'form-control',\n" +
               "            'placeholder': 'Enter tags separated by commas'\n" +
               "        }),\n" +
               "        help_text='Enter tags separated by commas'\n" +
               "    )\n" +
               "    \n" +
               "    class Meta:\n" +
               "        model = Article\n" +
               "        fields = ['title', 'content', 'excerpt', 'category', 'featured_image', 'status', 'is_featured']\n" +
               "        widgets = {\n" +
               "            'title': forms.TextInput(attrs={'class': 'form-control'}),\n" +
               "            'content': forms.Textarea(attrs={'class': 'form-control', 'rows': 10}),\n" +
               "            'excerpt': forms.Textarea(attrs={'class': 'form-control', 'rows': 3}),\n" +
               "            'category': forms.Select(attrs={'class': 'form-control'}),\n" +
               "            'featured_image': forms.ClearableFileInput(attrs={'class': 'form-control'}),\n" +
               "            'status': forms.Select(attrs={'class': 'form-control'}),\n" +
               "            'is_featured': forms.CheckboxInput(attrs={'class': 'form-check-input'}),\n" +
               "        }\n" +
               "    \n" +
               "    def __init__(self, *args, **kwargs):\n" +
               "        super().__init__(*args, **kwargs)\n" +
               "        \n" +
               "        # Set initial tags if editing\n" +
               "        if self.instance.pk:\n" +
               "            self.fields['tags'].initial = ', '.join(\n" +
               "                self.instance.tags.values_list('name', flat=True)\n" +
               "            )\n" +
               "    \n" +
               "    def clean_title(self):\n" +
               "        title = self.cleaned_data['title']\n" +
               "        if len(title) < 5:\n" +
               "            raise ValidationError('Title must be at least 5 characters long')\n" +
               "        return title\n" +
               "    \n" +
               "    def save(self, commit=True):\n" +
               "        article = super().save(commit=False)\n" +
               "        \n" +
               "        # Generate slug from title if not set\n" +
               "        if not article.slug:\n" +
               "            article.slug = slugify(article.title)\n" +
               "            \n" +
               "            # Ensure slug is unique\n" +
               "            original_slug = article.slug\n" +
               "            counter = 1\n" +
               "            while Article.objects.filter(slug=article.slug).exists():\n" +
               "                article.slug = f'{original_slug}-{counter}'\n" +
               "                counter += 1\n" +
               "        \n" +
               "        if commit:\n" +
               "            article.save()\n" +
               "            \n" +
               "            # Handle tags\n" +
               "            tags_text = self.cleaned_data.get('tags', '')\n" +
               "            if tags_text:\n" +
               "                tag_names = [tag.strip() for tag in tags_text.split(',') if tag.strip()]\n" +
               "                tags = []\n" +
               "                for tag_name in tag_names:\n" +
               "                    tag, created = Tag.objects.get_or_create(\n" +
               "                        name=tag_name,\n" +
               "                        defaults={'slug': slugify(tag_name)}\n" +
               "                    )\n" +
               "                    tags.append(tag)\n" +
               "                article.tags.set(tags)\n" +
               "            else:\n" +
               "                article.tags.clear()\n" +
               "            \n" +
               "            self.save_m2m()\n" +
               "        \n" +
               "        return article\n\n" +
               "class CommentForm(forms.ModelForm):\n" +
               "    \"\"\"Form for adding comments.\"\"\"\n" +
               "    \n" +
               "    class Meta:\n" +
               "        model = Comment\n" +
               "        fields = ['content']\n" +
               "        widgets = {\n" +
               "            'content': forms.Textarea(attrs={\n" +
               "                'class': 'form-control',\n" +
               "                'rows': 3,\n" +
               "                'placeholder': 'Write your comment here...'\n" +
               "            })\n" +
               "        }\n" +
               "    \n" +
               "    def clean_content(self):\n" +
               "        content = self.cleaned_data['content']\n" +
               "        if len(content) < 10:\n" +
               "            raise ValidationError('Comment must be at least 10 characters long')\n" +
               "        if len(content) > 1000:\n" +
               "            raise ValidationError('Comment cannot exceed 1000 characters')\n" +
               "        return content\n\n" +
               "class CategoryForm(forms.ModelForm):\n" +
               "    \"\"\"Form for creating and editing categories.\"\"\"\n" +
               "    \n" +
               "    class Meta:\n" +
               "        model = Category\n" +
               "        fields = ['name', 'description', 'parent', 'is_active', 'order']\n" +
               "        widgets = {\n" +
               "            'name': forms.TextInput(attrs={'class': 'form-control'}),\n" +
               "            'description': forms.Textarea(attrs={'class': 'form-control', 'rows': 3}),\n" +
               "            'parent': forms.Select(attrs={'class': 'form-control'}),\n" +
               "            'is_active': forms.CheckboxInput(attrs={'class': 'form-check-input'}),\n" +
               "            'order': forms.NumberInput(attrs={'class': 'form-control'}),\n" +
               "        }\n" +
               "    \n" +
               "    def __init__(self, *args, **kwargs):\n" +
               "        super().__init__(*args, **kwargs)\n" +
               "        \n" +
               "        # Exclude self from parent choices when editing\n" +
               "        if self.instance.pk:\n" +
               "            self.fields['parent'].queryset = Category.objects.exclude(\n" +
               "                pk=self.instance.pk\n" +
               "            )\n" +
               "    \n" +
               "    def clean_name(self):\n" +
               "        name = self.cleaned_data['name']\n" +
               "        if Category.objects.filter(name__iexact=name).exclude(pk=self.instance.pk).exists():\n" +
               "            raise ValidationError('A category with this name already exists')\n" +
               "        return name\n" +
               "    \n" +
               "    def save(self, commit=True):\n" +
               "        category = super().save(commit=False)\n" +
               "        \n" +
               "        # Generate slug from name if not set\n" +
               "        if not category.slug:\n" +
               "            category.slug = slugify(category.name)\n" +
               "        \n" +
               "        if commit:\n" +
               "            category.save()\n" +
               "        \n" +
               "        return category\n\n" +
               "class SearchForm(forms.Form):\n" +
               "    \"\"\"Form for searching articles.\"\"\"\n" +
               "    \n" +
               "    q = forms.CharField(\n" +
               "        max_length=200,\n" +
               "        required=False,\n" +
               "        widget=forms.TextInput(attrs={\n" +
               "            'class': 'form-control',\n" +
               "            'placeholder': 'Search articles...'\n" +
               "        })\n" +
               "    )\n" +
               "    \n" +
               "    category = forms.ModelChoiceField(\n" +
               "        queryset=Category.objects.filter(is_active=True),\n" +
               "        required=False,\n" +
               "        empty_label='All Categories',\n" +
               "        widget=forms.Select(attrs={'class': 'form-control'})\n" +
               "    )\n" +
               "    \n" +
               "    status = forms.ChoiceField(\n" +
               "        choices=[('', 'All')] + Article._meta.get_field('status').choices,\n" +
               "        required=False,\n" +
               "        widget=forms.Select(attrs={'class': 'form-control'})\n" +
               "    )\n" +
               "    \n" +
               "    sort_by = forms.ChoiceField(\n" +
               "        choices=[\n" +
               "            ('-published_at', 'Newest First'),\n" +
               "            ('published_at', 'Oldest First'),\n" +
               "            ('-views_count', 'Most Viewed'),\n" +
               "            ('title', 'Title A-Z'),\n" +
               "            ('-title', 'Title Z-A'),\n" +
               "        ],\n" +
               "        required=False,\n" +
               "        widget=forms.Select(attrs={'class': 'form-control'})\n" +
               "    )\n";
    }

    private String generateCoreAdmin() {
        return "from django.contrib import admin\n" +
               "from django.utils.html import format_html\n" +
               "from django.urls import reverse\n" +
               "from django.utils.safestring import mark_safe\n" +
               "from .models import Category, Tag, Article, Comment, Notification\n\n" +
               "@admin.register(Category)\n" +
               "class CategoryAdmin(admin.ModelAdmin):\n" +
               "    list_display = ['name', 'slug', 'parent', 'is_active', 'order', 'created_at']\n" +
               "    list_filter = ['is_active', 'created_at', 'parent']\n" +
               "    search_fields = ['name', 'slug', 'description']\n" +
               "    prepopulated_fields = {'slug': ('name',)}\n" +
               "    ordering = ['order', 'name']\n" +
               "    list_editable = ['is_active', 'order']\n\n" +
               "@admin.register(Tag)\n" +
               "class TagAdmin(admin.ModelAdmin):\n" +
               "    list_display = ['name', 'slug', 'article_count', 'created_at']\n" +
               "    search_fields = ['name', 'slug']\n" +
               "    prepopulated_fields = {'slug': ('name',)}\n" +
               "    \n" +
               "    def article_count(self, obj):\n" +
               "        return obj.article_set.count()\n" +
               "    article_count.short_description = 'Articles'\n\n" +
               "@admin.register(Article)\n" +
               "class ArticleAdmin(admin.ModelAdmin):\n" +
               "    list_display = [\n" +
               "        'title', 'author', 'category', 'status', 'is_featured',\n" +
               "        'views_count', 'comment_count', 'published_at', 'created_at'\n" +
               "    ]\n" +
               "    list_filter = [\n" +
               "        'status', 'is_featured', 'created_at', 'published_at',\n" +
               "        'category', 'tags'\n" +
               "    ]\n" +
               "    search_fields = ['title', 'content', 'excerpt', 'author__username']\n" +
               "    prepopulated_fields = {'slug': ('title',)}\n" +
               "    raw_id_fields = ['author']\n" +
               "    date_hierarchy = 'created_at'\n" +
               "    ordering = ['-created_at']\n" +
               "    list_editable = ['status', 'is_featured']\n" +
               "    filter_horizontal = ['tags']\n" +
               "    \n" +
               "    fieldsets = (\n" +
               "        ('Basic Information', {\n" +
               "            'fields': ('title', 'slug', 'author', 'excerpt')\n" +
               "        }),\n" +
               "        ('Content', {\n" +
               "            'fields': ('content', 'featured_image')\n" +
               "        }),\n" +
               "        ('Categorization', {\n" +
               "            'fields': ('category', 'tags')\n" +
               "        }),\n" +
               "        ('Publishing', {\n" +
               "            'fields': ('status', 'is_featured', 'published_at')\n" +
               "        }),\n" +
               "        ('Statistics', {\n" +
               "            'fields': ('views_count',),\n" +
               "            'classes': ('collapse',)\n" +
               "        }),\n" +
               "    )\n" +
               "    \n" +
               "    readonly_fields = ['views_count', 'published_at']\n" +
               "    \n" +
               "    def comment_count(self, obj):\n" +
               "        return obj.comments.count()\n" +
               "    comment_count.short_description = 'Comments'\n" +
               "    \n" +
               "    def save_model(self, request, obj, form, change):\n" +
               "        if not change:  # Creating new article\n" +
               "            obj.author = request.user\n" +
               "        super().save_model(request, obj, form, change)\n" +
               "    \n" +
               "    actions = ['make_published', 'make_draft', 'make_featured']\n" +
               "    \n" +
               "    def make_published(self, request, queryset):\n" +
               "        updated = queryset.update(status='published')\n" +
               "        self.message_user(request, f'{updated} articles published.')\n" +
               "    make_published.short_description = 'Publish selected articles'\n" +
               "    \n" +
               "    def make_draft(self, request, queryset):\n" +
               "        updated = queryset.update(status='draft')\n" +
               "        self.message_user(request, f'{updated} articles marked as draft.')\n" +
               "    make_draft.short_description = 'Mark selected articles as draft'\n" +
               "    \n" +
               "    def make_featured(self, request, queryset):\n" +
               "        updated = queryset.update(is_featured=True)\n" +
               "        self.message_user(request, f'{updated} articles featured.')\n" +
               "    make_featured.short_description = 'Feature selected articles'\n\n" +
               "@admin.register(Comment)\n" +
               "class CommentAdmin(admin.ModelAdmin):\n" +
               "    list_display = [\n" +
               "        'id', 'author', 'article_link', 'content_preview',\n" +
               "        'is_approved', 'parent', 'created_at'\n" +
               "    ]\n" +
               "    list_filter = ['is_approved', 'created_at']\n" +
               "    search_fields = ['content', 'author__username', 'article__title']\n" +
               "    raw_id_fields = ['author', 'article', 'parent']\n" +
               "    date_hierarchy = 'created_at'\n" +
               "    ordering = ['-created_at']\n" +
               "    list_editable = ['is_approved']\n" +
               "    \n" +
               "    def content_preview(self, obj):\n" +
               "        return obj.content[:50] + '...' if len(obj.content) > 50 else obj.content\n" +
               "    content_preview.short_description = 'Content'\n" +
               "    \n" +
               "    def article_link(self, obj):\n" +
               "        url = reverse('admin:core_article_change', args=[obj.article.id])\n" +
               "        return format_html('<a href=\"{}\">{}</a>', url, obj.article.title)\n" +
               "    article_link.short_description = 'Article'\n" +
               "    \n" +
               "    actions = ['approve_comments', 'disapprove_comments']\n" +
               "    \n" +
               "    def approve_comments(self, request, queryset):\n" +
               "        updated = queryset.update(is_approved=True)\n" +
               "        self.message_user(request, f'{updated} comments approved.')\n" +
               "    approve_comments.short_description = 'Approve selected comments'\n" +
               "    \n" +
               "    def disapprove_comments(self, request, queryset):\n" +
               "        updated = queryset.update(is_approved=False)\n" +
               "        self.message_user(request, f'{updated} comments disapproved.')\n" +
               "    disapprove_comments.short_description = 'Disapprove selected comments'\n\n" +
               "@admin.register(Notification)\n" +
               "class NotificationAdmin(admin.ModelAdmin):\n" +
               "    list_display = [\n" +
               "        'title', 'recipient', 'notification_type',\n" +
               "        'is_read', 'created_at'\n" +
               "    ]\n" +
               "    list_filter = ['notification_type', 'is_read', 'created_at']\n" +
               "    search_fields = ['title', 'message', 'recipient__username']\n" +
               "    raw_id_fields = ['recipient']\n" +
               "    date_hierarchy = 'created_at'\n" +
               "    ordering = ['-created_at']\n" +
               "    list_editable = ['is_read']\n" +
               "    \n" +
               "    actions = ['mark_as_read', 'mark_as_unread']\n" +
               "    \n" +
               "    def mark_as_read(self, request, queryset):\n" +
               "        updated = queryset.update(is_read=True)\n" +
               "        self.message_user(request, f'{updated} notifications marked as read.')\n" +
               "    mark_as_read.short_description = 'Mark selected as read'\n" +
               "    \n" +
               "    def mark_as_unread(self, request, queryset):\n" +
               "        updated = queryset.update(is_read=False)\n" +
               "        self.message_user(request, f'{updated} notifications marked as unread.')\n" +
               "    mark_as_unread.short_description = 'Mark selected as unread'\n";
    }

    private String generateCoreUrls() {
        return "from django.urls import path\n" +
               "from .views import (\n" +
               "    ArticleListView, ArticleDetailView, ArticleCreateView,\n" +
               "    ArticleUpdateView, ArticleDeleteView,\n" +
               "    add_comment, notifications_view, mark_notification_read\n" +
               ")\n\n" +
               "app_name = 'core'\n\n" +
               "urlpatterns = [\n" +
               "    # Article URLs\n" +
               "    path('', ArticleListView.as_view(), name='article_list'),\n" +
               "    path('articles/', ArticleListView.as_view(), name='article_list'),\n" +
               "    path('articles/create/', ArticleCreateView.as_view(), name='article_create'),\n" +
               "    path('articles/<slug:slug>/', ArticleDetailView.as_view(), name='article_detail'),\n" +
               "    path('articles/<slug:slug>/edit/', ArticleUpdateView.as_view(), name='article_update'),\n" +
               "    path('articles/<slug:slug>/delete/', ArticleDeleteView.as_view(), name='article_delete'),\n" +
               "    \n" +
               "    # Category and Tag URLs\n" +
               "    path('category/<slug:category_slug>/', ArticleListView.as_view(), name='category_articles'),\n" +
               "    path('tag/<slug:tag_slug>/', ArticleListView.as_view(), name='tag_articles'),\n" +
               "    \n" +
               "    # Comment URLs\n" +
               "    path('articles/<slug:slug>/comment/', add_comment, name='add_comment'),\n" +
               "    \n" +
               "    # Notification URLs\n" +
               "    path('notifications/', notifications_view, name='notifications'),\n" +
               "    path('notifications/<int:notification_id>/read/', \n" +
               "         mark_notification_read, name='mark_notification_read'),\n" +
               "]\n";
    }

    private String generatePagesViews() {
        return "from django.shortcuts import render\n" +
               "from django.views.generic import TemplateView\n" +
               "from django.contrib.auth.mixins import LoginRequiredMixin\n" +
               "from django.db.models import Count, Q\n" +
               "from apps.core.models import Article, Category, Tag\n" +
               "from datetime import datetime, timedelta\n\n" +
               "class HomePageView(TemplateView):\n" +
               "    \"\"\"Home page view.\"\"\"\n" +
               "    template_name = 'pages/home.html'\n" +
               "    \n" +
               "    def get_context_data(self, **kwargs):\n" +
               "        context = super().get_context_data(**kwargs)\n" +
               "        \n" +
               "        # Featured articles\n" +
               "        context['featured_articles'] = Article.objects.filter(\n" +
               "            status='published',\n" +
               "            is_featured=True\n" +
               "        ).select_related('author', 'category')[:3]\n" +
               "        \n" +
               "        # Recent articles\n" +
               "        context['recent_articles'] = Article.objects.filter(\n" +
               "            status='published'\n" +
               "        ).select_related('author', 'category')[:6]\n" +
               "        \n" +
               "        # Popular articles (most viewed)\n" +
               "        context['popular_articles'] = Article.objects.filter(\n" +
               "            status='published'\n" +
               "        ).order_by('-views_count')[:5]\n" +
               "        \n" +
               "        # Categories with article count\n" +
               "        context['categories'] = Category.objects.filter(\n" +
               "            is_active=True\n" +
               "        ).annotate(\n" +
               "            article_count=Count('article')\n" +
               "        ).order_by('-article_count')[:8]\n" +
               "        \n" +
               "        # Popular tags\n" +
               "        context['popular_tags'] = Tag.objects.annotate(\n" +
               "            article_count=Count('article')\n" +
               "        ).filter(article_count__gt=0).order_by('-article_count')[:15]\n" +
               "        \n" +
               "        # Site statistics\n" +
               "        context['total_articles'] = Article.objects.filter(status='published').count()\n" +
               "        context['total_categories'] = Category.objects.filter(is_active=True).count()\n" +
               "        context['total_tags'] = Tag.objects.count()\n" +
               "        \n" +
               "        return context\n\n" +
               "class AboutPageView(TemplateView):\n" +
               "    \"\"\"About page view.\"\"\"\n" +
               "    template_name = 'pages/about.html'\n" +
               "    \n" +
               "    def get_context_data(self, **kwargs):\n" +
               "        context = super().get_context_data(**kwargs)\n" +
               "        context['team_members'] = [\n" +
               "            {\n" +
               "                'name': 'John Doe',\n" +
               "                'role': 'Founder & CEO',\n" +
               "                'bio': 'Passionate about technology and innovation.',\n" +
               "                'image': '/static/images/team/john.jpg'\n" +
               "            },\n" +
               "            {\n" +
               "                'name': 'Jane Smith',\n" +
               "                'role': 'CTO',\n" +
               "                'bio': 'Expert in software architecture and development.',\n" +
               "                'image': '/static/images/team/jane.jpg'\n" +
               "            },\n" +
               "            {\n" +
               "                'name': 'Mike Johnson',\n" +
               "                'role': 'Lead Developer',\n" +
               "                'bio': 'Full-stack developer with 10+ years experience.',\n" +
               "                'image': '/static/images/team/mike.jpg'\n" +
               "            },\n" +
               "        ]\n" +
               "        return context\n\n" +
               "class ContactPageView(TemplateView):\n" +
               "    \"\"\"Contact page view.\"\"\"\n" +
               "    template_name = 'pages/contact.html'\n" +
               "    \n" +
               "    def post(self, request, *args, **kwargs):\n" +
               "        # Handle contact form submission\n" +
               "        name = request.POST.get('name')\n" +
               "        email = request.POST.get('email')\n" +
               "        subject = request.POST.get('subject')\n" +
               "        message = request.POST.get('message')\n" +
               "        \n" +
               "        # Send email (implementation depends on email backend)\n" +
               "        from django.core.mail import send_mail\n" +
               "        from django.conf import settings\n" +
               "        \n" +
               "        try:\n" +
               "            send_mail(\n" +
               "                f'Contact Form: {subject}',\n" +
               "                f'From: {name} <{email}>\\n\\nMessage:\\n{message}',\n" +
               "                settings.DEFAULT_FROM_EMAIL,\n" +
               "                [settings.DEFAULT_FROM_EMAIL],\n" +
               "                fail_silently=False,\n" +
               "            )\n" +
               "            \n" +
               "            context = self.get_context_data(**kwargs)\n" +
               "            context['success'] = True\n" +
               "            context['message'] = 'Your message has been sent successfully!'\n" +
               "        except Exception as e:\n" +
               "            context = self.get_context_data(**kwargs)\n" +
               "            context['error'] = True\n" +
               "            context['message'] = 'Failed to send message. Please try again later.'\n" +
               "        \n" +
               "        return self.render_to_response(context)\n\n" +
               "class PrivacyPolicyView(TemplateView):\n" +
               "    \"\"\"Privacy policy page view.\"\"\"\n" +
               "    template_name = 'pages/privacy.html'\n\n" +
               "class TermsOfServiceView(TemplateView):\n" +
               "    \"\"\"Terms of service page view.\"\"\"\n" +
               "    template_name = 'pages/terms.html'\n\n" +
               "class DashboardView(LoginRequiredMixin, TemplateView):\n" +
               "    \"\"\"User dashboard view.\"\"\"\n" +
               "    template_name = 'pages/dashboard.html'\n" +
               "    \n" +
               "    def get_context_data(self, **kwargs):\n" +
               "        context = super().get_context_data(**kwargs)\n" +
               "        user = self.request.user\n" +
               "        \n" +
               "        # User's articles\n" +
               "        context['user_articles'] = Article.objects.filter(\n" +
               "            author=user\n" +
               "        ).order_by('-created_at')[:10]\n" +
               "        \n" +
               "        # Article statistics\n" +
               "        context['total_articles'] = Article.objects.filter(author=user).count()\n" +
               "        context['published_articles'] = Article.objects.filter(\n" +
               "            author=user,\n" +
               "            status='published'\n" +
               "        ).count()\n" +
               "        context['draft_articles'] = Article.objects.filter(\n" +
               "            author=user,\n" +
               "            status='draft'\n" +
               "        ).count()\n" +
               "        \n" +
               "        # Total views\n" +
               "        total_views = 0\n" +
               "        for article in Article.objects.filter(author=user):\n" +
               "            total_views += article.views_count\n" +
               "        context['total_views'] = total_views\n" +
               "        \n" +
               "        # Recent notifications\n" +
               "        context['recent_notifications'] = user.notifications.all()[:5]\n" +
               "        context['unread_notifications'] = user.notifications.filter(is_read=False).count()\n" +
               "        \n" +
               "        # Activity chart data (last 30 days)\n" +
               "        thirty_days_ago = datetime.now() - timedelta(days=30)\n" +
               "        articles_by_day = Article.objects.filter(\n" +
               "            author=user,\n" +
               "            created_at__gte=thirty_days_ago\n" +
               "        ).extra(\n" +
               "            select={'day': 'date(created_at)'},\n" +
               "        ).values('day').annotate(count=Count('id'))\n" +
               "        \n" +
               "        context['activity_data'] = list(articles_by_day)\n" +
               "        \n" +
               "        return context\n\n" +
               "def custom_404(request, exception):\n" +
               "    \"\"\"Custom 404 error page.\"\"\"\n" +
               "    return render(request, 'pages/404.html', status=404)\n\n" +
               "def custom_500(request):\n" +
               "    \"\"\"Custom 500 error page.\"\"\"\n" +
               "    return render(request, 'pages/500.html', status=500)\n";
    }

    private String generatePagesUrls() {
        return "from django.urls import path\n" +
               "from .views import (\n" +
               "    HomePageView, AboutPageView, ContactPageView,\n" +
               "    PrivacyPolicyView, TermsOfServiceView, DashboardView\n" +
               ")\n\n" +
               "app_name = 'pages'\n\n" +
               "urlpatterns = [\n" +
               "    path('', HomePageView.as_view(), name='home'),\n" +
               "    path('about/', AboutPageView.as_view(), name='about'),\n" +
               "    path('contact/', ContactPageView.as_view(), name='contact'),\n" +
               "    path('privacy/', PrivacyPolicyView.as_view(), name='privacy'),\n" +
               "    path('terms/', TermsOfServiceView.as_view(), name='terms'),\n" +
               "    path('dashboard/', DashboardView.as_view(), name='dashboard'),\n" +
               "]\n";
    }

    private String generateServiceHandlers() {
        return "from typing import Any, Dict, Optional\n" +
               "import json\n" +
               "import logging\n" +
               "from django.conf import settings\n" +
               "from django.core.cache import cache\n" +
               "import requests\n" +
               "from requests.adapters import HTTPAdapter\n" +
               "from requests.packages.urllib3.util.retry import Retry\n\n" +
               "logger = logging.getLogger(__name__)\n\n" +
               "class BaseServiceHandler:\n" +
               "    \"\"\"Base handler for external service integrations.\"\"\"\n" +
               "    \n" +
               "    def __init__(self, base_url: str, timeout: int = 30):\n" +
               "        self.base_url = base_url\n" +
               "        self.timeout = timeout\n" +
               "        self.session = self._create_session()\n" +
               "    \n" +
               "    def _create_session(self) -> requests.Session:\n" +
               "        \"\"\"Create a requests session with retry logic.\"\"\"\n" +
               "        session = requests.Session()\n" +
               "        \n" +
               "        # Configure retry strategy\n" +
               "        retry_strategy = Retry(\n" +
               "            total=3,\n" +
               "            backoff_factor=1,\n" +
               "            status_forcelist=[429, 500, 502, 503, 504],\n" +
               "            method_whitelist=['HEAD', 'GET', 'OPTIONS']\n" +
               "        )\n" +
               "        \n" +
               "        adapter = HTTPAdapter(max_retries=retry_strategy)\n" +
               "        session.mount('http://', adapter)\n" +
               "        session.mount('https://', adapter)\n" +
               "        \n" +
               "        return session\n" +
               "    \n" +
               "    def make_request(\n" +
               "        self,\n" +
               "        method: str,\n" +
               "        endpoint: str,\n" +
               "        data: Optional[Dict] = None,\n" +
               "        headers: Optional[Dict] = None\n" +
               "    ) -> Optional[Dict]:\n" +
               "        \"\"\"Make HTTP request to external service.\"\"\"\n" +
               "        url = f'{self.base_url}/{endpoint}'\n" +
               "        \n" +
               "        try:\n" +
               "            response = self.session.request(\n" +
               "                method=method,\n" +
               "                url=url,\n" +
               "                json=data,\n" +
               "                headers=headers,\n" +
               "                timeout=self.timeout\n" +
               "            )\n" +
               "            response.raise_for_status()\n" +
               "            return response.json()\n" +
               "        except requests.exceptions.RequestException as e:\n" +
               "            logger.error(f'Request failed: {e}')\n" +
               "            return None\n" +
               "    \n" +
               "    def get_cached_or_fetch(\n" +
               "        self,\n" +
               "        cache_key: str,\n" +
               "        fetch_func: callable,\n" +
               "        cache_timeout: int = 3600\n" +
               "    ) -> Any:\n" +
               "        \"\"\"Get data from cache or fetch if not available.\"\"\"\n" +
               "        cached_data = cache.get(cache_key)\n" +
               "        \n" +
               "        if cached_data is not None:\n" +
               "            logger.debug(f'Cache hit for key: {cache_key}')\n" +
               "            return cached_data\n" +
               "        \n" +
               "        logger.debug(f'Cache miss for key: {cache_key}')\n" +
               "        data = fetch_func()\n" +
               "        \n" +
               "        if data is not None:\n" +
               "            cache.set(cache_key, data, cache_timeout)\n" +
               "        \n" +
               "        return data\n\n" +
               "class PaymentServiceHandler(BaseServiceHandler):\n" +
               "    \"\"\"Handler for payment processing services.\"\"\"\n" +
               "    \n" +
               "    def __init__(self):\n" +
               "        super().__init__(\n" +
               "            base_url=settings.PAYMENT_SERVICE_URL,\n" +
               "            timeout=60\n" +
               "        )\n" +
               "        self.api_key = settings.PAYMENT_API_KEY\n" +
               "    \n" +
               "    def process_payment(\n" +
               "        self,\n" +
               "        amount: float,\n" +
               "        currency: str,\n" +
               "        payment_method: str,\n" +
               "        metadata: Dict\n" +
               "    ) -> Dict:\n" +
               "        \"\"\"Process a payment transaction.\"\"\"\n" +
               "        headers = {\n" +
               "            'Authorization': f'Bearer {self.api_key}',\n" +
               "            'Content-Type': 'application/json'\n" +
               "        }\n" +
               "        \n" +
               "        data = {\n" +
               "            'amount': amount,\n" +
               "            'currency': currency,\n" +
               "            'payment_method': payment_method,\n" +
               "            'metadata': metadata\n" +
               "        }\n" +
               "        \n" +
               "        result = self.make_request(\n" +
               "            method='POST',\n" +
               "            endpoint='payments',\n" +
               "            data=data,\n" +
               "            headers=headers\n" +
               "        )\n" +
               "        \n" +
               "        if result:\n" +
               "            logger.info(f'Payment processed: {result.get(\"transaction_id\")}')\n" +
               "        else:\n" +
               "            logger.error('Payment processing failed')\n" +
               "        \n" +
               "        return result or {'status': 'failed'}\n" +
               "    \n" +
               "    def refund_payment(self, transaction_id: str, amount: Optional[float] = None) -> Dict:\n" +
               "        \"\"\"Refund a payment transaction.\"\"\"\n" +
               "        headers = {\n" +
               "            'Authorization': f'Bearer {self.api_key}'\n" +
               "        }\n" +
               "        \n" +
               "        data = {'transaction_id': transaction_id}\n" +
               "        if amount:\n" +
               "            data['amount'] = amount\n" +
               "        \n" +
               "        return self.make_request(\n" +
               "            method='POST',\n" +
               "            endpoint='refunds',\n" +
               "            data=data,\n" +
               "            headers=headers\n" +
               "        )\n\n" +
               "class EmailServiceHandler(BaseServiceHandler):\n" +
               "    \"\"\"Handler for email service integrations.\"\"\"\n" +
               "    \n" +
               "    def __init__(self):\n" +
               "        super().__init__(\n" +
               "            base_url=settings.EMAIL_SERVICE_URL,\n" +
               "            timeout=30\n" +
               "        )\n" +
               "        self.api_key = settings.EMAIL_API_KEY\n" +
               "    \n" +
               "    def send_email(\n" +
               "        self,\n" +
               "        to: list,\n" +
               "        subject: str,\n" +
               "        html_content: str,\n" +
               "        text_content: Optional[str] = None,\n" +
               "        from_email: Optional[str] = None\n" +
               "    ) -> bool:\n" +
               "        \"\"\"Send email via external service.\"\"\"\n" +
               "        headers = {\n" +
               "            'Authorization': f'Bearer {self.api_key}',\n" +
               "            'Content-Type': 'application/json'\n" +
               "        }\n" +
               "        \n" +
               "        data = {\n" +
               "            'to': to,\n" +
               "            'subject': subject,\n" +
               "            'html': html_content,\n" +
               "            'text': text_content or html_content,\n" +
               "            'from': from_email or settings.DEFAULT_FROM_EMAIL\n" +
               "        }\n" +
               "        \n" +
               "        result = self.make_request(\n" +
               "            method='POST',\n" +
               "            endpoint='send',\n" +
               "            data=data,\n" +
               "            headers=headers\n" +
               "        )\n" +
               "        \n" +
               "        return result is not None and result.get('status') == 'sent'\n" +
               "    \n" +
               "    def send_bulk_email(\n" +
               "        self,\n" +
               "        recipients: list,\n" +
               "        template_id: str,\n" +
               "        variables: Dict\n" +
               "    ) -> Dict:\n" +
               "        \"\"\"Send bulk emails using template.\"\"\"\n" +
               "        headers = {\n" +
               "            'Authorization': f'Bearer {self.api_key}'\n" +
               "        }\n" +
               "        \n" +
               "        data = {\n" +
               "            'recipients': recipients,\n" +
               "            'template_id': template_id,\n" +
               "            'variables': variables\n" +
               "        }\n" +
               "        \n" +
               "        return self.make_request(\n" +
               "            method='POST',\n" +
               "            endpoint='bulk-send',\n" +
               "            data=data,\n" +
               "            headers=headers\n" +
               "        )\n\n" +
               "class NotificationServiceHandler:\n" +
               "    \"\"\"Handler for push notifications.\"\"\"\n" +
               "    \n" +
               "    def __init__(self):\n" +
               "        self.fcm_server_key = settings.FCM_SERVER_KEY\n" +
               "    \n" +
               "    def send_push_notification(\n" +
               "        self,\n" +
               "        device_tokens: list,\n" +
               "        title: str,\n" +
               "        body: str,\n" +
               "        data: Optional[Dict] = None\n" +
               "    ) -> bool:\n" +
               "        \"\"\"Send push notification via FCM.\"\"\"\n" +
               "        from pyfcm import FCMNotification\n" +
               "        \n" +
               "        push_service = FCMNotification(api_key=self.fcm_server_key)\n" +
               "        \n" +
               "        try:\n" +
               "            result = push_service.notify_multiple_devices(\n" +
               "                registration_ids=device_tokens,\n" +
               "                message_title=title,\n" +
               "                message_body=body,\n" +
               "                data_message=data\n" +
               "            )\n" +
               "            \n" +
               "            return result['success'] > 0\n" +
               "        except Exception as e:\n" +
               "            logger.error(f'Push notification failed: {e}')\n" +
               "            return False\n\n" +
               "class WebhookHandler:\n" +
               "    \"\"\"Handler for incoming webhooks.\"\"\"\n" +
               "    \n" +
               "    @staticmethod\n" +
               "    def verify_signature(payload: str, signature: str, secret: str) -> bool:\n" +
               "        \"\"\"Verify webhook signature.\"\"\"\n" +
               "        import hmac\n" +
               "        import hashlib\n" +
               "        \n" +
               "        expected_signature = hmac.new(\n" +
               "            secret.encode(),\n" +
               "            payload.encode(),\n" +
               "            hashlib.sha256\n" +
               "        ).hexdigest()\n" +
               "        \n" +
               "        return hmac.compare_digest(expected_signature, signature)\n" +
               "    \n" +
               "    @staticmethod\n" +
               "    def process_webhook(event_type: str, payload: Dict) -> bool:\n" +
               "        \"\"\"Process incoming webhook event.\"\"\"\n" +
               "        from .tasks import process_webhook_event\n" +
               "        \n" +
               "        # Queue webhook processing as async task\n" +
               "        process_webhook_event.delay(event_type, payload)\n" +
               "        \n" +
               "        return True\n";
    }

    private String generateServiceTasks() {
        return "from celery import shared_task\n" +
               "from celery.utils.log import get_task_logger\n" +
               "from django.core.cache import cache\n" +
               "from django.core.mail import send_mail\n" +
               "from django.conf import settings\n" +
               "from django.utils import timezone\n" +
               "from datetime import datetime, timedelta\n" +
               "import requests\n" +
               "import json\n" +
               "from typing import Dict, List, Optional\n\n" +
               "logger = get_task_logger(__name__)\n\n" +
               "@shared_task(bind=True, max_retries=3)\n" +
               "def send_email_task(self, to_email: str, subject: str, message: str) -> bool:\n" +
               "    \"\"\"Async task to send email.\"\"\"\n" +
               "    try:\n" +
               "        send_mail(\n" +
               "            subject,\n" +
               "            message,\n" +
               "            settings.DEFAULT_FROM_EMAIL,\n" +
               "            [to_email],\n" +
               "            fail_silently=False,\n" +
               "        )\n" +
               "        logger.info(f'Email sent to {to_email}')\n" +
               "        return True\n" +
               "    except Exception as exc:\n" +
               "        logger.error(f'Email sending failed: {exc}')\n" +
               "        # Retry with exponential backoff\n" +
               "        raise self.retry(exc=exc, countdown=60 * (2 ** self.request.retries))\n\n" +
               "@shared_task\n" +
               "def process_webhook_event(event_type: str, payload: Dict) -> None:\n" +
               "    \"\"\"Process webhook events asynchronously.\"\"\"\n" +
               "    logger.info(f'Processing webhook event: {event_type}')\n" +
               "    \n" +
               "    try:\n" +
               "        if event_type == 'payment.completed':\n" +
               "            handle_payment_completed(payload)\n" +
               "        elif event_type == 'payment.failed':\n" +
               "            handle_payment_failed(payload)\n" +
               "        elif event_type == 'subscription.created':\n" +
               "            handle_subscription_created(payload)\n" +
               "        elif event_type == 'subscription.cancelled':\n" +
               "            handle_subscription_cancelled(payload)\n" +
               "        else:\n" +
               "            logger.warning(f'Unknown webhook event type: {event_type}')\n" +
               "    except Exception as e:\n" +
               "        logger.error(f'Webhook processing failed: {e}')\n" +
               "        raise\n\n" +
               "@shared_task\n" +
               "def cleanup_old_data() -> Dict:\n" +
               "    \"\"\"Periodic task to clean up old data.\"\"\"\n" +
               "    from apps.core.models import Notification, Article\n" +
               "    \n" +
               "    # Delete old read notifications (older than 30 days)\n" +
               "    thirty_days_ago = timezone.now() - timedelta(days=30)\n" +
               "    deleted_notifications = Notification.objects.filter(\n" +
               "        is_read=True,\n" +
               "        created_at__lt=thirty_days_ago\n" +
               "    ).delete()\n" +
               "    \n" +
               "    # Archive old draft articles (older than 90 days)\n" +
               "    ninety_days_ago = timezone.now() - timedelta(days=90)\n" +
               "    archived_articles = Article.objects.filter(\n" +
               "        status='draft',\n" +
               "        created_at__lt=ninety_days_ago\n" +
               "    ).update(status='archived')\n" +
               "    \n" +
               "    # Clear old cache entries\n" +
               "    cache.clear()\n" +
               "    \n" +
               "    result = {\n" +
               "        'notifications_deleted': deleted_notifications[0],\n" +
               "        'articles_archived': archived_articles,\n" +
               "        'cache_cleared': True,\n" +
               "        'timestamp': timezone.now().isoformat()\n" +
               "    }\n" +
               "    \n" +
               "    logger.info(f'Cleanup completed: {result}')\n" +
               "    return result\n\n" +
               "@shared_task\n" +
               "def generate_reports() -> None:\n" +
               "    \"\"\"Generate periodic reports.\"\"\"\n" +
               "    from apps.reports.generators import (\n" +
               "        generate_daily_report,\n" +
               "        generate_weekly_report,\n" +
               "        generate_monthly_report\n" +
               "    )\n" +
               "    \n" +
               "    now = datetime.now()\n" +
               "    \n" +
               "    # Daily report\n" +
               "    generate_daily_report(now.date())\n" +
               "    \n" +
               "    # Weekly report (on Mondays)\n" +
               "    if now.weekday() == 0:\n" +
               "        generate_weekly_report(now.date())\n" +
               "    \n" +
               "    # Monthly report (on first day of month)\n" +
               "    if now.day == 1:\n" +
               "        generate_monthly_report(now.date())\n" +
               "    \n" +
               "    logger.info('Reports generated successfully')\n\n" +
               "@shared_task(bind=True)\n" +
               "def sync_external_data(self, source: str) -> Dict:\n" +
               "    \"\"\"Sync data from external sources.\"\"\"\n" +
               "    logger.info(f'Starting sync from {source}')\n" +
               "    \n" +
               "    try:\n" +
               "        if source == 'api':\n" +
               "            result = sync_from_api()\n" +
               "        elif source == 'database':\n" +
               "            result = sync_from_database()\n" +
               "        elif source == 'file':\n" +
               "            result = sync_from_file()\n" +
               "        else:\n" +
               "            raise ValueError(f'Unknown sync source: {source}')\n" +
               "        \n" +
               "        logger.info(f'Sync completed: {result}')\n" +
               "        return result\n" +
               "        \n" +
               "    except Exception as exc:\n" +
               "        logger.error(f'Sync failed: {exc}')\n" +
               "        raise self.retry(exc=exc, countdown=300)\n\n" +
               "@shared_task\n" +
               "def process_batch_operations(operation_type: str, items: List[int]) -> Dict:\n" +
               "    \"\"\"Process batch operations asynchronously.\"\"\"\n" +
               "    logger.info(f'Processing batch {operation_type} for {len(items)} items')\n" +
               "    \n" +
               "    results = {\n" +
               "        'success': [],\n" +
               "        'failed': [],\n" +
               "        'total': len(items)\n" +
               "    }\n" +
               "    \n" +
               "    for item_id in items:\n" +
               "        try:\n" +
               "            if operation_type == 'publish':\n" +
               "                publish_item(item_id)\n" +
               "            elif operation_type == 'archive':\n" +
               "                archive_item(item_id)\n" +
               "            elif operation_type == 'delete':\n" +
               "                delete_item(item_id)\n" +
               "            \n" +
               "            results['success'].append(item_id)\n" +
               "        except Exception as e:\n" +
               "            logger.error(f'Failed to process item {item_id}: {e}')\n" +
               "            results['failed'].append(item_id)\n" +
               "    \n" +
               "    results['success_count'] = len(results['success'])\n" +
               "    results['failed_count'] = len(results['failed'])\n" +
               "    \n" +
               "    return results\n\n" +
               "@shared_task\n" +
               "def monitor_system_health() -> Dict:\n" +
               "    \"\"\"Monitor system health and send alerts.\"\"\"\n" +
               "    import psutil\n" +
               "    from django.db import connection\n" +
               "    \n" +
               "    health_status = {\n" +
               "        'timestamp': timezone.now().isoformat(),\n" +
               "        'status': 'healthy',\n" +
               "        'alerts': []\n" +
               "    }\n" +
               "    \n" +
               "    # Check CPU usage\n" +
               "    cpu_percent = psutil.cpu_percent(interval=1)\n" +
               "    if cpu_percent > 80:\n" +
               "        health_status['alerts'].append(f'High CPU usage: {cpu_percent}%')\n" +
               "        health_status['status'] = 'warning'\n" +
               "    \n" +
               "    # Check memory usage\n" +
               "    memory = psutil.virtual_memory()\n" +
               "    if memory.percent > 85:\n" +
               "        health_status['alerts'].append(f'High memory usage: {memory.percent}%')\n" +
               "        health_status['status'] = 'warning'\n" +
               "    \n" +
               "    # Check disk usage\n" +
               "    disk = psutil.disk_usage('/')\n" +
               "    if disk.percent > 90:\n" +
               "        health_status['alerts'].append(f'High disk usage: {disk.percent}%')\n" +
               "        health_status['status'] = 'critical'\n" +
               "    \n" +
               "    # Check database connection\n" +
               "    try:\n" +
               "        with connection.cursor() as cursor:\n" +
               "            cursor.execute('SELECT 1')\n" +
               "    except Exception as e:\n" +
               "        health_status['alerts'].append(f'Database connection failed: {e}')\n" +
               "        health_status['status'] = 'critical'\n" +
               "    \n" +
               "    # Send alerts if needed\n" +
               "    if health_status['alerts']:\n" +
               "        send_health_alerts(health_status)\n" +
               "    \n" +
               "    # Store health status\n" +
               "    cache.set('system_health', health_status, 300)\n" +
               "    \n" +
               "    return health_status\n\n" +
               "# Helper functions\n" +
               "def handle_payment_completed(payload: Dict) -> None:\n" +
               "    \"\"\"Handle payment completed webhook.\"\"\"\n" +
               "    # Implementation specific to your payment processing\n" +
               "    pass\n\n" +
               "def handle_payment_failed(payload: Dict) -> None:\n" +
               "    \"\"\"Handle payment failed webhook.\"\"\"\n" +
               "    # Implementation specific to your payment processing\n" +
               "    pass\n\n" +
               "def handle_subscription_created(payload: Dict) -> None:\n" +
               "    \"\"\"Handle subscription created webhook.\"\"\"\n" +
               "    # Implementation specific to your subscription handling\n" +
               "    pass\n\n" +
               "def handle_subscription_cancelled(payload: Dict) -> None:\n" +
               "    \"\"\"Handle subscription cancelled webhook.\"\"\"\n" +
               "    # Implementation specific to your subscription handling\n" +
               "    pass\n\n" +
               "def sync_from_api() -> Dict:\n" +
               "    \"\"\"Sync data from external API.\"\"\"\n" +
               "    # Implementation specific to your API integration\n" +
               "    return {'synced': 0}\n\n" +
               "def sync_from_database() -> Dict:\n" +
               "    \"\"\"Sync data from external database.\"\"\"\n" +
               "    # Implementation specific to your database integration\n" +
               "    return {'synced': 0}\n\n" +
               "def sync_from_file() -> Dict:\n" +
               "    \"\"\"Sync data from file.\"\"\"\n" +
               "    # Implementation specific to your file integration\n" +
               "    return {'synced': 0}\n\n" +
               "def publish_item(item_id: int) -> None:\n" +
               "    \"\"\"Publish an item.\"\"\"\n" +
               "    # Implementation specific to your publishing logic\n" +
               "    pass\n\n" +
               "def archive_item(item_id: int) -> None:\n" +
               "    \"\"\"Archive an item.\"\"\"\n" +
               "    # Implementation specific to your archiving logic\n" +
               "    pass\n\n" +
               "def delete_item(item_id: int) -> None:\n" +
               "    \"\"\"Delete an item.\"\"\"\n" +
               "    # Implementation specific to your deletion logic\n" +
               "    pass\n\n" +
               "def send_health_alerts(health_status: Dict) -> None:\n" +
               "    \"\"\"Send health status alerts.\"\"\"\n" +
               "    # Send email or push notification about system health\n" +
               "    alert_message = '\\n'.join(health_status['alerts'])\n" +
               "    send_email_task.delay(\n" +
               "        settings.ADMIN_EMAIL,\n" +
               "        f'System Health Alert: {health_status[\"status\"]}',\n" +
               "        alert_message\n" +
               "    )\n";
    }

    private String generateServiceConsumers() {
        return "import json\n" +
               "import logging\n" +
               "from channels.generic.websocket import AsyncWebsocketConsumer\n" +
               "from channels.db import database_sync_to_async\n" +
               "from django.contrib.auth import get_user_model\n" +
               "from asgiref.sync import async_to_sync\n" +
               "from channels.layers import get_channel_layer\n\n" +
               "logger = logging.getLogger(__name__)\n" +
               "User = get_user_model()\n\n" +
               "class NotificationConsumer(AsyncWebsocketConsumer):\n" +
               "    \"\"\"WebSocket consumer for real-time notifications.\"\"\"\n" +
               "    \n" +
               "    async def connect(self):\n" +
               "        \"\"\"Handle WebSocket connection.\"\"\"\n" +
               "        self.user = self.scope['user']\n" +
               "        \n" +
               "        if self.user.is_anonymous:\n" +
               "            await self.close()\n" +
               "            return\n" +
               "        \n" +
               "        # Create room name based on user ID\n" +
               "        self.room_name = f'notifications_{self.user.id}'\n" +
               "        self.room_group_name = f'user_{self.user.id}'\n" +
               "        \n" +
               "        # Join room group\n" +
               "        await self.channel_layer.group_add(\n" +
               "            self.room_group_name,\n" +
               "            self.channel_name\n" +
               "        )\n" +
               "        \n" +
               "        await self.accept()\n" +
               "        \n" +
               "        # Send initial connection message\n" +
               "        await self.send(text_data=json.dumps({\n" +
               "            'type': 'connection_established',\n" +
               "            'message': 'Connected to notification service'\n" +
               "        }))\n" +
               "        \n" +
               "        # Send unread notification count\n" +
               "        unread_count = await self.get_unread_notifications_count()\n" +
               "        await self.send(text_data=json.dumps({\n" +
               "            'type': 'unread_count',\n" +
               "            'count': unread_count\n" +
               "        }))\n" +
               "        \n" +
               "        logger.info(f'WebSocket connected for user {self.user.username}')\n" +
               "    \n" +
               "    async def disconnect(self, close_code):\n" +
               "        \"\"\"Handle WebSocket disconnection.\"\"\"\n" +
               "        if hasattr(self, 'room_group_name'):\n" +
               "            # Leave room group\n" +
               "            await self.channel_layer.group_discard(\n" +
               "                self.room_group_name,\n" +
               "                self.channel_name\n" +
               "            )\n" +
               "        \n" +
               "        logger.info(f'WebSocket disconnected for user {getattr(self, \"user\", \"unknown\")}')\n" +
               "    \n" +
               "    async def receive(self, text_data):\n" +
               "        \"\"\"Handle incoming WebSocket messages.\"\"\"\n" +
               "        try:\n" +
               "            data = json.loads(text_data)\n" +
               "            message_type = data.get('type')\n" +
               "            \n" +
               "            if message_type == 'mark_read':\n" +
               "                notification_id = data.get('notification_id')\n" +
               "                await self.mark_notification_read(notification_id)\n" +
               "            \n" +
               "            elif message_type == 'mark_all_read':\n" +
               "                await self.mark_all_notifications_read()\n" +
               "            \n" +
               "            elif message_type == 'ping':\n" +
               "                await self.send(text_data=json.dumps({\n" +
               "                    'type': 'pong',\n" +
               "                    'timestamp': data.get('timestamp')\n" +
               "                }))\n" +
               "            \n" +
               "        except json.JSONDecodeError:\n" +
               "            logger.error(f'Invalid JSON received: {text_data}')\n" +
               "        except Exception as e:\n" +
               "            logger.error(f'Error processing message: {e}')\n" +
               "    \n" +
               "    async def notification_message(self, event):\n" +
               "        \"\"\"Send notification to WebSocket.\"\"\"\n" +
               "        await self.send(text_data=json.dumps({\n" +
               "            'type': 'notification',\n" +
               "            'notification': event['notification']\n" +
               "        }))\n" +
               "    \n" +
               "    @database_sync_to_async\n" +
               "    def get_unread_notifications_count(self):\n" +
               "        \"\"\"Get unread notifications count for user.\"\"\"\n" +
               "        return self.user.notifications.filter(is_read=False).count()\n" +
               "    \n" +
               "    @database_sync_to_async\n" +
               "    def mark_notification_read(self, notification_id):\n" +
               "        \"\"\"Mark a notification as read.\"\"\"\n" +
               "        try:\n" +
               "            notification = self.user.notifications.get(id=notification_id)\n" +
               "            notification.is_read = True\n" +
               "            notification.save()\n" +
               "            return True\n" +
               "        except Exception as e:\n" +
               "            logger.error(f'Error marking notification as read: {e}')\n" +
               "            return False\n" +
               "    \n" +
               "    @database_sync_to_async\n" +
               "    def mark_all_notifications_read(self):\n" +
               "        \"\"\"Mark all notifications as read.\"\"\"\n" +
               "        return self.user.notifications.filter(is_read=False).update(is_read=True)\n\n" +
               "class ChatConsumer(AsyncWebsocketConsumer):\n" +
               "    \"\"\"WebSocket consumer for real-time chat.\"\"\"\n" +
               "    \n" +
               "    async def connect(self):\n" +
               "        \"\"\"Handle chat connection.\"\"\"\n" +
               "        self.room_name = self.scope['url_route']['kwargs']['room_name']\n" +
               "        self.room_group_name = f'chat_{self.room_name}'\n" +
               "        self.user = self.scope['user']\n" +
               "        \n" +
               "        if self.user.is_anonymous:\n" +
               "            await self.close()\n" +
               "            return\n" +
               "        \n" +
               "        # Join room group\n" +
               "        await self.channel_layer.group_add(\n" +
               "            self.room_group_name,\n" +
               "            self.channel_name\n" +
               "        )\n" +
               "        \n" +
               "        await self.accept()\n" +
               "        \n" +
               "        # Send user joined message\n" +
               "        await self.channel_layer.group_send(\n" +
               "            self.room_group_name,\n" +
               "            {\n" +
               "                'type': 'user_joined',\n" +
               "                'username': self.user.username\n" +
               "            }\n" +
               "        )\n" +
               "    \n" +
               "    async def disconnect(self, close_code):\n" +
               "        \"\"\"Handle chat disconnection.\"\"\"\n" +
               "        # Send user left message\n" +
               "        await self.channel_layer.group_send(\n" +
               "            self.room_group_name,\n" +
               "            {\n" +
               "                'type': 'user_left',\n" +
               "                'username': self.user.username\n" +
               "            }\n" +
               "        )\n" +
               "        \n" +
               "        # Leave room group\n" +
               "        await self.channel_layer.group_discard(\n" +
               "            self.room_group_name,\n" +
               "            self.channel_name\n" +
               "        )\n" +
               "    \n" +
               "    async def receive(self, text_data):\n" +
               "        \"\"\"Handle incoming chat messages.\"\"\"\n" +
               "        try:\n" +
               "            data = json.loads(text_data)\n" +
               "            message = data['message']\n" +
               "            \n" +
               "            # Save message to database\n" +
               "            await self.save_message(message)\n" +
               "            \n" +
               "            # Send message to room group\n" +
               "            await self.channel_layer.group_send(\n" +
               "                self.room_group_name,\n" +
               "                {\n" +
               "                    'type': 'chat_message',\n" +
               "                    'message': message,\n" +
               "                    'username': self.user.username,\n" +
               "                    'timestamp': timezone.now().isoformat()\n" +
               "                }\n" +
               "            )\n" +
               "        except Exception as e:\n" +
               "            logger.error(f'Error processing chat message: {e}')\n" +
               "    \n" +
               "    async def chat_message(self, event):\n" +
               "        \"\"\"Send chat message to WebSocket.\"\"\"\n" +
               "        await self.send(text_data=json.dumps({\n" +
               "            'type': 'message',\n" +
               "            'message': event['message'],\n" +
               "            'username': event['username'],\n" +
               "            'timestamp': event['timestamp']\n" +
               "        }))\n" +
               "    \n" +
               "    async def user_joined(self, event):\n" +
               "        \"\"\"Send user joined notification.\"\"\"\n" +
               "        await self.send(text_data=json.dumps({\n" +
               "            'type': 'user_joined',\n" +
               "            'username': event['username']\n" +
               "        }))\n" +
               "    \n" +
               "    async def user_left(self, event):\n" +
               "        \"\"\"Send user left notification.\"\"\"\n" +
               "        await self.send(text_data=json.dumps({\n" +
               "            'type': 'user_left',\n" +
               "            'username': event['username']\n" +
               "        }))\n" +
               "    \n" +
               "    @database_sync_to_async\n" +
               "    def save_message(self, message):\n" +
               "        \"\"\"Save chat message to database.\"\"\"\n" +
               "        # Implementation depends on your chat model\n" +
               "        pass\n\n" +
               "def send_notification_to_user(user_id: int, notification_data: dict):\n" +
               "    \"\"\"Send notification to specific user via WebSocket.\"\"\"\n" +
               "    channel_layer = get_channel_layer()\n" +
               "    async_to_sync(channel_layer.group_send)(\n" +
               "        f'user_{user_id}',\n" +
               "        {\n" +
               "            'type': 'notification_message',\n" +
               "            'notification': notification_data\n" +
               "        }\n" +
               "    )\n\n" +
               "def broadcast_message(room_name: str, message_data: dict):\n" +
               "    \"\"\"Broadcast message to all users in a room.\"\"\"\n" +
               "    channel_layer = get_channel_layer()\n" +
               "    async_to_sync(channel_layer.group_send)(\n" +
               "        f'chat_{room_name}',\n" +
               "        {\n" +
               "            'type': 'chat_message',\n" +
               "            **message_data\n" +
               "        }\n" +
               "    )\n";
    }
    private String generateHealthViews() {
        return "from django.http import JsonResponse\n" +
               "from django.views import View\n" +
               "from django.db import connection\n" +
               "from django.core.cache import cache\n" +
               "from django.utils import timezone\n" +
               "import psutil\n" +
               "import os\n" +
               "import redis\n" +
               "from celery import current_app\n" +
               "import logging\n\n" +
               "logger = logging.getLogger(__name__)\n\n" +
               "class HealthCheckView(View):\n" +
               "    \"\"\"Basic health check endpoint\"\"\"\n" +
               "    \n" +
               "    def get(self, request):\n" +
               "        return JsonResponse({\n" +
               "            'status': 'healthy',\n" +
               "            'timestamp': timezone.now().isoformat(),\n" +
               "            'service': 'django-api',\n" +
               "            'version': os.environ.get('APP_VERSION', '1.0.0')\n" +
               "        })\n\n" +
               "class LivenessProbeView(View):\n" +
               "    \"\"\"Kubernetes liveness probe endpoint\"\"\"\n" +
               "    \n" +
               "    def get(self, request):\n" +
               "        try:\n" +
               "            # Basic application check\n" +
               "            import django\n" +
               "            django_version = django.get_version()\n" +
               "            \n" +
               "            return JsonResponse({\n" +
               "                'status': 'alive',\n" +
               "                'django_version': django_version,\n" +
               "                'timestamp': timezone.now().isoformat()\n" +
               "            })\n" +
               "        except Exception as e:\n" +
               "            logger.error(f'Liveness check failed: {e}')\n" +
               "            return JsonResponse({\n" +
               "                'status': 'error',\n" +
               "                'error': str(e)\n" +
               "            }, status=503)\n\n" +
               "class ReadinessProbeView(View):\n" +
               "    \"\"\"Kubernetes readiness probe endpoint\"\"\"\n" +
               "    \n" +
               "    def get(self, request):\n" +
               "        checks = {}\n" +
               "        is_ready = True\n" +
               "        \n" +
               "        # Database check\n" +
               "        try:\n" +
               "            with connection.cursor() as cursor:\n" +
               "                cursor.execute('SELECT 1')\n" +
               "            checks['database'] = {'status': 'healthy'}\n" +
               "        except Exception as e:\n" +
               "            checks['database'] = {'status': 'unhealthy', 'error': str(e)}\n" +
               "            is_ready = False\n" +
               "        \n" +
               "        # Cache check\n" +
               "        try:\n" +
               "            cache.set('health_check', 'ok', 1)\n" +
               "            if cache.get('health_check') == 'ok':\n" +
               "                checks['cache'] = {'status': 'healthy'}\n" +
               "            else:\n" +
               "                raise Exception('Cache read/write failed')\n" +
               "        except Exception as e:\n" +
               "            checks['cache'] = {'status': 'unhealthy', 'error': str(e)}\n" +
               "            is_ready = False\n" +
               "        \n" +
               "        # Celery check\n" +
               "        try:\n" +
               "            celery_status = current_app.control.inspect().active()\n" +
               "            if celery_status:\n" +
               "                checks['celery'] = {'status': 'healthy', 'workers': len(celery_status)}\n" +
               "            else:\n" +
               "                checks['celery'] = {'status': 'unhealthy', 'error': 'No active workers'}\n" +
               "                is_ready = False\n" +
               "        except Exception as e:\n" +
               "            checks['celery'] = {'status': 'unhealthy', 'error': str(e)}\n" +
               "            # Celery is optional, don't fail readiness\n" +
               "        \n" +
               "        status_code = 200 if is_ready else 503\n" +
               "        return JsonResponse({\n" +
               "            'status': 'ready' if is_ready else 'not_ready',\n" +
               "            'checks': checks,\n" +
               "            'timestamp': timezone.now().isoformat()\n" +
               "        }, status=status_code)\n\n" +
               "class DetailedHealthView(View):\n" +
               "    \"\"\"Detailed health status with system metrics\"\"\"\n" +
               "    \n" +
               "    def get(self, request):\n" +
               "        # Require authentication for detailed health info\n" +
               "        if not request.user.is_staff:\n" +
               "            return JsonResponse({'error': 'Unauthorized'}, status=401)\n" +
               "        \n" +
               "        health_data = {\n" +
               "            'timestamp': timezone.now().isoformat(),\n" +
               "            'system': self.get_system_metrics(),\n" +
               "            'database': self.check_database(),\n" +
               "            'cache': self.check_cache(),\n" +
               "            'celery': self.check_celery(),\n" +
               "            'storage': self.check_storage(),\n" +
               "            'external_services': self.check_external_services()\n" +
               "        }\n" +
               "        \n" +
               "        # Calculate overall health score\n" +
               "        health_score = self.calculate_health_score(health_data)\n" +
               "        health_data['health_score'] = health_score\n" +
               "        health_data['status'] = self.get_health_status(health_score)\n" +
               "        \n" +
               "        return JsonResponse(health_data)\n" +
               "    \n" +
               "    def get_system_metrics(self):\n" +
               "        \"\"\"Get system resource metrics\"\"\"\n" +
               "        try:\n" +
               "            cpu_percent = psutil.cpu_percent(interval=1)\n" +
               "            memory = psutil.virtual_memory()\n" +
               "            disk = psutil.disk_usage('/')\n" +
               "            \n" +
               "            return {\n" +
               "                'cpu': {\n" +
               "                    'usage_percent': cpu_percent,\n" +
               "                    'cores': psutil.cpu_count(),\n" +
               "                    'status': 'healthy' if cpu_percent < 80 else 'warning'\n" +
               "                },\n" +
               "                'memory': {\n" +
               "                    'usage_percent': memory.percent,\n" +
               "                    'total_gb': round(memory.total / (1024**3), 2),\n" +
               "                    'available_gb': round(memory.available / (1024**3), 2),\n" +
               "                    'status': 'healthy' if memory.percent < 85 else 'warning'\n" +
               "                },\n" +
               "                'disk': {\n" +
               "                    'usage_percent': disk.percent,\n" +
               "                    'total_gb': round(disk.total / (1024**3), 2),\n" +
               "                    'free_gb': round(disk.free / (1024**3), 2),\n" +
               "                    'status': 'healthy' if disk.percent < 90 else 'warning'\n" +
               "                }\n" +
               "            }\n" +
               "        except Exception as e:\n" +
               "            logger.error(f'Failed to get system metrics: {e}')\n" +
               "            return {'error': str(e), 'status': 'error'}\n" +
               "    \n" +
               "    def check_database(self):\n" +
               "        \"\"\"Check database connectivity and performance\"\"\"\n" +
               "        try:\n" +
               "            from django.db import connections\n" +
               "            import time\n" +
               "            \n" +
               "            results = {}\n" +
               "            for db_alias in connections:\n" +
               "                start_time = time.time()\n" +
               "                with connections[db_alias].cursor() as cursor:\n" +
               "                    cursor.execute('SELECT 1')\n" +
               "                    cursor.fetchone()\n" +
               "                \n" +
               "                response_time = (time.time() - start_time) * 1000\n" +
               "                \n" +
               "                results[db_alias] = {\n" +
               "                    'status': 'healthy' if response_time < 100 else 'slow',\n" +
               "                    'response_time_ms': round(response_time, 2),\n" +
               "                    'connection_count': len(connections[db_alias].queries)\n" +
               "                }\n" +
               "            \n" +
               "            return results\n" +
               "        except Exception as e:\n" +
               "            logger.error(f'Database check failed: {e}')\n" +
               "            return {'error': str(e), 'status': 'error'}\n" +
               "    \n" +
               "    def check_cache(self):\n" +
               "        \"\"\"Check cache connectivity and performance\"\"\"\n" +
               "        try:\n" +
               "            import time\n" +
               "            test_key = 'health_check_test'\n" +
               "            test_value = timezone.now().isoformat()\n" +
               "            \n" +
               "            # Write test\n" +
               "            start_time = time.time()\n" +
               "            cache.set(test_key, test_value, 10)\n" +
               "            write_time = (time.time() - start_time) * 1000\n" +
               "            \n" +
               "            # Read test\n" +
               "            start_time = time.time()\n" +
               "            retrieved = cache.get(test_key)\n" +
               "            read_time = (time.time() - start_time) * 1000\n" +
               "            \n" +
               "            # Clean up\n" +
               "            cache.delete(test_key)\n" +
               "            \n" +
               "            return {\n" +
               "                'status': 'healthy' if retrieved == test_value else 'error',\n" +
               "                'write_time_ms': round(write_time, 2),\n" +
               "                'read_time_ms': round(read_time, 2),\n" +
               "                'backend': cache.__class__.__name__\n" +
               "            }\n" +
               "        except Exception as e:\n" +
               "            logger.error(f'Cache check failed: {e}')\n" +
               "            return {'error': str(e), 'status': 'error'}\n" +
               "    \n" +
               "    def check_celery(self):\n" +
               "        \"\"\"Check Celery workers and queue status\"\"\"\n" +
               "        try:\n" +
               "            from kombu import Connection\n" +
               "            from django.conf import settings\n" +
               "            \n" +
               "            # Check worker status\n" +
               "            inspect = current_app.control.inspect()\n" +
               "            active_workers = inspect.active()\n" +
               "            stats = inspect.stats()\n" +
               "            \n" +
               "            # Check queue lengths\n" +
               "            broker_url = settings.CELERY_BROKER_URL\n" +
               "            with Connection(broker_url) as conn:\n" +
               "                channel = conn.channel()\n" +
               "                queue_info = {}\n" +
               "                \n" +
               "                for queue_name in ['celery', 'priority', 'scheduled']:\n" +
               "                    try:\n" +
               "                        queue = channel.queue_declare(queue_name, passive=True)\n" +
               "                        queue_info[queue_name] = queue.message_count\n" +
               "                    except:\n" +
               "                        queue_info[queue_name] = 0\n" +
               "            \n" +
               "            return {\n" +
               "                'status': 'healthy' if active_workers else 'no_workers',\n" +
               "                'workers': len(active_workers) if active_workers else 0,\n" +
               "                'queues': queue_info,\n" +
               "                'stats': stats\n" +
               "            }\n" +
               "        except Exception as e:\n" +
               "            logger.error(f'Celery check failed: {e}')\n" +
               "            return {'error': str(e), 'status': 'warning'}\n" +
               "    \n" +
               "    def check_storage(self):\n" +
               "        \"\"\"Check storage backends\"\"\"\n" +
               "        try:\n" +
               "            from django.core.files.storage import default_storage\n" +
               "            from django.conf import settings\n" +
               "            import tempfile\n" +
               "            \n" +
               "            # Test file operations\n" +
               "            test_content = b'health_check_test'\n" +
               "            test_name = f'health_check_{timezone.now().timestamp()}.txt'\n" +
               "            \n" +
               "            # Write test\n" +
               "            path = default_storage.save(test_name, tempfile.TemporaryFile())\n" +
               "            default_storage.delete(path)\n" +
               "            \n" +
               "            # Check media root\n" +
               "            media_disk = psutil.disk_usage(settings.MEDIA_ROOT)\n" +
               "            \n" +
               "            return {\n" +
               "                'status': 'healthy',\n" +
               "                'backend': default_storage.__class__.__name__,\n" +
               "                'media_disk': {\n" +
               "                    'usage_percent': media_disk.percent,\n" +
               "                    'free_gb': round(media_disk.free / (1024**3), 2)\n" +
               "                }\n" +
               "            }\n" +
               "        except Exception as e:\n" +
               "            logger.error(f'Storage check failed: {e}')\n" +
               "            return {'error': str(e), 'status': 'warning'}\n" +
               "    \n" +
               "    def check_external_services(self):\n" +
               "        \"\"\"Check connectivity to external services\"\"\"\n" +
               "        import requests\n" +
               "        from django.conf import settings\n" +
               "        \n" +
               "        services = {}\n" +
               "        \n" +
               "        # Check Redis if configured\n" +
               "        if hasattr(settings, 'REDIS_URL'):\n" +
               "            try:\n" +
               "                r = redis.from_url(settings.REDIS_URL)\n" +
               "                r.ping()\n" +
               "                services['redis'] = {'status': 'healthy'}\n" +
               "            except Exception as e:\n" +
               "                services['redis'] = {'status': 'error', 'error': str(e)}\n" +
               "        \n" +
               "        # Check external APIs\n" +
               "        external_apis = getattr(settings, 'HEALTH_CHECK_APIS', [])\n" +
               "        for api_config in external_apis:\n" +
               "            try:\n" +
               "                response = requests.get(\n" +
               "                    api_config['url'],\n" +
               "                    timeout=api_config.get('timeout', 5)\n" +
               "                )\n" +
               "                services[api_config['name']] = {\n" +
               "                    'status': 'healthy' if response.status_code == 200 else 'degraded',\n" +
               "                    'response_time_ms': round(response.elapsed.total_seconds() * 1000, 2)\n" +
               "                }\n" +
               "            except Exception as e:\n" +
               "                services[api_config['name']] = {\n" +
               "                    'status': 'error',\n" +
               "                    'error': str(e)\n" +
               "                }\n" +
               "        \n" +
               "        return services\n" +
               "    \n" +
               "    def calculate_health_score(self, health_data):\n" +
               "        \"\"\"Calculate overall health score (0-100)\"\"\"\n" +
               "        score = 100\n" +
               "        \n" +
               "        # System metrics impact\n" +
               "        if 'system' in health_data:\n" +
               "            system = health_data['system']\n" +
               "            if 'cpu' in system and system['cpu'].get('usage_percent', 0) > 80:\n" +
               "                score -= 10\n" +
               "            if 'memory' in system and system['memory'].get('usage_percent', 0) > 85:\n" +
               "                score -= 15\n" +
               "            if 'disk' in system and system['disk'].get('usage_percent', 0) > 90:\n" +
               "                score -= 20\n" +
               "        \n" +
               "        # Critical services impact\n" +
               "        if health_data.get('database', {}).get('status') == 'error':\n" +
               "            score -= 30\n" +
               "        if health_data.get('cache', {}).get('status') == 'error':\n" +
               "            score -= 20\n" +
               "        \n" +
               "        return max(0, score)\n" +
               "    \n" +
               "    def get_health_status(self, score):\n" +
               "        \"\"\"Get health status based on score\"\"\"\n" +
               "        if score >= 90:\n" +
               "            return 'healthy'\n" +
               "        elif score >= 70:\n" +
               "            return 'degraded'\n" +
               "        elif score >= 50:\n" +
               "            return 'warning'\n" +
               "        else:\n" +
               "            return 'critical'\n";
    private String generateHealthUrls() {
        return "from django.urls import path\n" +
               "from . import views\n\n" +
               "app_name = 'health'\n\n" +
               "urlpatterns = [\n" +
               "    # Basic health check\n" +
               "    path('', views.HealthCheckView.as_view(), name='health'),\n" +
               "    path('health/', views.HealthCheckView.as_view(), name='basic'),\n" +
               "    \n" +
               "    # Kubernetes probes\n" +
               "    path('liveness/', views.LivenessProbeView.as_view(), name='liveness'),\n" +
               "    path('readiness/', views.ReadinessProbeView.as_view(), name='readiness'),\n" +
               "    \n" +
               "    # Detailed health status (requires authentication)\n" +
               "    path('detailed/', views.DetailedHealthView.as_view(), name='detailed'),\n" +
               "    \n" +
               "    # Specific component checks\n" +
               "    path('db/', views.DatabaseHealthView.as_view(), name='database'),\n" +
               "    path('cache/', views.CacheHealthView.as_view(), name='cache'),\n" +
               "    path('celery/', views.CeleryHealthView.as_view(), name='celery'),\n" +
               "    path('storage/', views.StorageHealthView.as_view(), name='storage'),\n" +
               "]\n";
    private String generateContentModels() {
        return "from django.db import models\n" +
               "from django.contrib.auth import get_user_model\n" +
               "from django.utils.text import slugify\n" +
               "from django.utils import timezone\n" +
               "from django.core.validators import MinValueValidator, MaxValueValidator\n" +
               "from taggit.managers import TaggableManager\n" +
               "from django.contrib.contenttypes.fields import GenericForeignKey\n" +
               "from django.contrib.contenttypes.models import ContentType\n" +
               "import uuid\n\n" +
               "User = get_user_model()\n\n" +
               "class Category(models.Model):\n" +
               "    \"\"\"Content category model\"\"\"\n" +
               "    name = models.CharField(max_length=100, unique=True)\n" +
               "    slug = models.SlugField(max_length=100, unique=True, blank=True)\n" +
               "    description = models.TextField(blank=True)\n" +
               "    parent = models.ForeignKey(\n" +
               "        'self',\n" +
               "        null=True,\n" +
               "        blank=True,\n" +
               "        on_delete=models.CASCADE,\n" +
               "        related_name='children'\n" +
               "    )\n" +
               "    icon = models.CharField(max_length=50, blank=True)\n" +
               "    color = models.CharField(max_length=7, default='#000000')\n" +
               "    order = models.IntegerField(default=0)\n" +
               "    is_active = models.BooleanField(default=True)\n" +
               "    created_at = models.DateTimeField(auto_now_add=True)\n" +
               "    updated_at = models.DateTimeField(auto_now=True)\n\n" +
               "    class Meta:\n" +
               "        verbose_name_plural = 'Categories'\n" +
               "        ordering = ['order', 'name']\n\n" +
               "    def save(self, *args, **kwargs):\n" +
               "        if not self.slug:\n" +
               "            self.slug = slugify(self.name)\n" +
               "        super().save(*args, **kwargs)\n\n" +
               "    def __str__(self):\n" +
               "        return self.name\n\n" +
               "    @property\n" +
               "    def full_path(self):\n" +
               "        if self.parent:\n" +
               "            return f'{self.parent.full_path} > {self.name}'\n" +
               "        return self.name\n\n" +
               "class ContentStatus(models.TextChoices):\n" +
               "    DRAFT = 'draft', 'Draft'\n" +
               "    REVIEW = 'review', 'In Review'\n" +
               "    PUBLISHED = 'published', 'Published'\n" +
               "    ARCHIVED = 'archived', 'Archived'\n\n" +
               "class Article(models.Model):\n" +
               "    \"\"\"Article content model\"\"\"\n" +
               "    uuid = models.UUIDField(default=uuid.uuid4, editable=False, unique=True)\n" +
               "    title = models.CharField(max_length=200)\n" +
               "    slug = models.SlugField(max_length=200, unique=True, blank=True)\n" +
               "    subtitle = models.CharField(max_length=300, blank=True)\n" +
               "    \n" +
               "    # Content\n" +
               "    summary = models.TextField(max_length=500)\n" +
               "    content = models.TextField()\n" +
               "    markdown_content = models.TextField(blank=True)\n" +
               "    \n" +
               "    # Metadata\n" +
               "    author = models.ForeignKey(\n" +
               "        User,\n" +
               "        on_delete=models.SET_NULL,\n" +
               "        null=True,\n" +
               "        related_name='articles'\n" +
               "    )\n" +
               "    category = models.ForeignKey(\n" +
               "        Category,\n" +
               "        on_delete=models.SET_NULL,\n" +
               "        null=True,\n" +
               "        blank=True,\n" +
               "        related_name='articles'\n" +
               "    )\n" +
               "    tags = TaggableManager(blank=True)\n" +
               "    \n" +
               "    # Media\n" +
               "    featured_image = models.ImageField(\n" +
               "        upload_to='articles/featured/%Y/%m/',\n" +
               "        blank=True,\n" +
               "        null=True\n" +
               "    )\n" +
               "    thumbnail = models.ImageField(\n" +
               "        upload_to='articles/thumbnails/%Y/%m/',\n" +
               "        blank=True,\n" +
               "        null=True\n" +
               "    )\n" +
               "    \n" +
               "    # Status and visibility\n" +
               "    status = models.CharField(\n" +
               "        max_length=20,\n" +
               "        choices=ContentStatus.choices,\n" +
               "        default=ContentStatus.DRAFT\n" +
               "    )\n" +
               "    is_featured = models.BooleanField(default=False)\n" +
               "    is_premium = models.BooleanField(default=False)\n" +
               "    \n" +
               "    # Engagement\n" +
               "    view_count = models.PositiveIntegerField(default=0)\n" +
               "    like_count = models.PositiveIntegerField(default=0)\n" +
               "    share_count = models.PositiveIntegerField(default=0)\n" +
               "    comment_count = models.PositiveIntegerField(default=0)\n" +
               "    reading_time = models.PositiveIntegerField(default=0, help_text='Estimated reading time in minutes')\n" +
               "    \n" +
               "    # SEO\n" +
               "    meta_title = models.CharField(max_length=60, blank=True)\n" +
               "    meta_description = models.CharField(max_length=160, blank=True)\n" +
               "    meta_keywords = models.CharField(max_length=255, blank=True)\n" +
               "    \n" +
               "    # Timestamps\n" +
               "    published_at = models.DateTimeField(null=True, blank=True)\n" +
               "    created_at = models.DateTimeField(auto_now_add=True)\n" +
               "    updated_at = models.DateTimeField(auto_now=True)\n\n" +
               "    class Meta:\n" +
               "        ordering = ['-published_at', '-created_at']\n" +
               "        indexes = [\n" +
               "            models.Index(fields=['slug']),\n" +
               "            models.Index(fields=['status', 'published_at']),\n" +
               "            models.Index(fields=['author', 'status']),\n" +
               "        ]\n\n" +
               "    def save(self, *args, **kwargs):\n" +
               "        if not self.slug:\n" +
               "            self.slug = slugify(self.title)\n" +
               "        \n" +
               "        if self.status == ContentStatus.PUBLISHED and not self.published_at:\n" +
               "            self.published_at = timezone.now()\n" +
               "        \n" +
               "        # Calculate reading time (avg 200 words per minute)\n" +
               "        word_count = len(self.content.split())\n" +
               "        self.reading_time = max(1, word_count // 200)\n" +
               "        \n" +
               "        super().save(*args, **kwargs)\n\n" +
               "    def __str__(self):\n" +
               "        return self.title\n\n" +
               "    @property\n" +
               "    def is_published(self):\n" +
               "        return self.status == ContentStatus.PUBLISHED\n\n" +
               "class Page(models.Model):\n" +
               "    \"\"\"Static page model\"\"\"\n" +
               "    title = models.CharField(max_length=200)\n" +
               "    slug = models.SlugField(max_length=200, unique=True)\n" +
               "    content = models.TextField()\n" +
               "    template = models.CharField(\n" +
               "        max_length=100,\n" +
               "        default='pages/default.html',\n" +
               "        help_text='Template path'\n" +
               "    )\n" +
               "    \n" +
               "    # SEO\n" +
               "    meta_title = models.CharField(max_length=60, blank=True)\n" +
               "    meta_description = models.CharField(max_length=160, blank=True)\n" +
               "    \n" +
               "    # Status\n" +
               "    is_active = models.BooleanField(default=True)\n" +
               "    show_in_menu = models.BooleanField(default=False)\n" +
               "    menu_order = models.IntegerField(default=0)\n" +
               "    \n" +
               "    created_at = models.DateTimeField(auto_now_add=True)\n" +
               "    updated_at = models.DateTimeField(auto_now=True)\n\n" +
               "    class Meta:\n" +
               "        ordering = ['menu_order', 'title']\n\n" +
               "    def __str__(self):\n" +
               "        return self.title\n\n" +
               "class Comment(models.Model):\n" +
               "    \"\"\"Comment model for articles\"\"\"\n" +
               "    article = models.ForeignKey(\n" +
               "        Article,\n" +
               "        on_delete=models.CASCADE,\n" +
               "        related_name='comments'\n" +
               "    )\n" +
               "    author = models.ForeignKey(\n" +
               "        User,\n" +
               "        on_delete=models.CASCADE,\n" +
               "        related_name='comments'\n" +
               "    )\n" +
               "    parent = models.ForeignKey(\n" +
               "        'self',\n" +
               "        null=True,\n" +
               "        blank=True,\n" +
               "        on_delete=models.CASCADE,\n" +
               "        related_name='replies'\n" +
               "    )\n" +
               "    \n" +
               "    content = models.TextField()\n" +
               "    is_approved = models.BooleanField(default=False)\n" +
               "    is_featured = models.BooleanField(default=False)\n" +
               "    \n" +
               "    like_count = models.PositiveIntegerField(default=0)\n" +
               "    \n" +
               "    created_at = models.DateTimeField(auto_now_add=True)\n" +
               "    updated_at = models.DateTimeField(auto_now=True)\n\n" +
               "    class Meta:\n" +
               "        ordering = ['-created_at']\n\n" +
               "    def __str__(self):\n" +
               "        return f'Comment by {self.author.username} on {self.article.title}'\n\n" +
               "class Like(models.Model):\n" +
               "    \"\"\"Generic like model\"\"\"\n" +
               "    user = models.ForeignKey(User, on_delete=models.CASCADE)\n" +
               "    content_type = models.ForeignKey(ContentType, on_delete=models.CASCADE)\n" +
               "    object_id = models.PositiveIntegerField()\n" +
               "    content_object = GenericForeignKey('content_type', 'object_id')\n" +
               "    created_at = models.DateTimeField(auto_now_add=True)\n\n" +
               "    class Meta:\n" +
               "        unique_together = ['user', 'content_type', 'object_id']\n\n" +
               "class Bookmark(models.Model):\n" +
               "    \"\"\"User bookmark model\"\"\"\n" +
               "    user = models.ForeignKey(\n" +
               "        User,\n" +
               "        on_delete=models.CASCADE,\n" +
               "        related_name='bookmarks'\n" +
               "    )\n" +
               "    article = models.ForeignKey(\n" +
               "        Article,\n" +
               "        on_delete=models.CASCADE,\n" +
               "        related_name='bookmarks'\n" +
               "    )\n" +
               "    notes = models.TextField(blank=True)\n" +
               "    created_at = models.DateTimeField(auto_now_add=True)\n\n" +
               "    class Meta:\n" +
               "        unique_together = ['user', 'article']\n" +
               "        ordering = ['-created_at']\n\n" +
               "class ContentView(models.Model):\n" +
               "    \"\"\"Track content views\"\"\"\n" +
               "    article = models.ForeignKey(\n" +
               "        Article,\n" +
               "        on_delete=models.CASCADE,\n" +
               "        related_name='views'\n" +
               "    )\n" +
               "    user = models.ForeignKey(\n" +
               "        User,\n" +
               "        on_delete=models.SET_NULL,\n" +
               "        null=True,\n" +
               "        blank=True\n" +
               "    )\n" +
               "    ip_address = models.GenericIPAddressField()\n" +
               "    user_agent = models.TextField(blank=True)\n" +
               "    referrer = models.URLField(blank=True, null=True)\n" +
               "    session_key = models.CharField(max_length=40, blank=True)\n" +
               "    created_at = models.DateTimeField(auto_now_add=True)\n\n" +
               "    class Meta:\n" +
               "        ordering = ['-created_at']\n\n" +
               "class Newsletter(models.Model):\n" +
               "    \"\"\"Newsletter subscription\"\"\"\n" +
               "    email = models.EmailField(unique=True)\n" +
               "    name = models.CharField(max_length=100, blank=True)\n" +
               "    is_active = models.BooleanField(default=True)\n" +
               "    categories = models.ManyToManyField(\n" +
               "        Category,\n" +
               "        blank=True,\n" +
               "        related_name='subscribers'\n" +
               "    )\n" +
               "    token = models.UUIDField(default=uuid.uuid4, editable=False)\n" +
               "    confirmed = models.BooleanField(default=False)\n" +
               "    confirmed_at = models.DateTimeField(null=True, blank=True)\n" +
               "    created_at = models.DateTimeField(auto_now_add=True)\n" +
               "    updated_at = models.DateTimeField(auto_now=True)\n\n" +
               "    def __str__(self):\n" +
               "        return self.email\n\n" +
               "class ContentRating(models.Model):\n" +
               "    \"\"\"Content rating model\"\"\"\n" +
               "    article = models.ForeignKey(\n" +
               "        Article,\n" +
               "        on_delete=models.CASCADE,\n" +
               "        related_name='ratings'\n" +
               "    )\n" +
               "    user = models.ForeignKey(\n" +
               "        User,\n" +
               "        on_delete=models.CASCADE,\n" +
               "        related_name='content_ratings'\n" +
               "    )\n" +
               "    rating = models.IntegerField(\n" +
               "        validators=[MinValueValidator(1), MaxValueValidator(5)]\n" +
               "    )\n" +
               "    review = models.TextField(blank=True)\n" +
               "    created_at = models.DateTimeField(auto_now_add=True)\n" +
               "    updated_at = models.DateTimeField(auto_now=True)\n\n" +
               "    class Meta:\n" +
               "        unique_together = ['article', 'user']\n\n" +
               "    def __str__(self):\n" +
               "        return f'{self.user.username} - {self.article.title} - {self.rating}★'\n";
    private String generateContentAdmin() {
        return "from django.contrib import admin\n" +
               "from django.utils.html import format_html\n" +
               "from django.urls import reverse\n" +
               "from django.utils.safestring import mark_safe\n" +
               "from django.db.models import Count, Avg\n" +
               "from import_export import resources\n" +
               "from import_export.admin import ImportExportModelAdmin\n" +
               "from .models import (\n" +
               "    Category, Article, Page, Comment, Like,\n" +
               "    Bookmark, ContentView, Newsletter, ContentRating\n" +
               ")\n\n" +
               "# Resources for import/export\n" +
               "class ArticleResource(resources.ModelResource):\n" +
               "    class Meta:\n" +
               "        model = Article\n" +
               "        fields = (\n" +
               "            'id', 'title', 'slug', 'subtitle', 'summary',\n" +
               "            'author__username', 'category__name', 'status',\n" +
               "            'is_featured', 'is_premium', 'view_count',\n" +
               "            'published_at', 'created_at'\n" +
               "        )\n" +
               "        export_order = fields\n\n" +
               "# Inline admins\n" +
               "class CommentInline(admin.TabularInline):\n" +
               "    model = Comment\n" +
               "    extra = 0\n" +
               "    fields = ['author', 'content', 'is_approved', 'created_at']\n" +
               "    readonly_fields = ['created_at']\n" +
               "    can_delete = True\n\n" +
               "class ContentRatingInline(admin.TabularInline):\n" +
               "    model = ContentRating\n" +
               "    extra = 0\n" +
               "    fields = ['user', 'rating', 'review', 'created_at']\n" +
               "    readonly_fields = ['created_at']\n\n" +
               "# Admin filters\n" +
               "class PublishedFilter(admin.SimpleListFilter):\n" +
               "    title = 'publication status'\n" +
               "    parameter_name = 'published'\n\n" +
               "    def lookups(self, request, model_admin):\n" +
               "        return (\n" +
               "            ('published', 'Published'),\n" +
               "            ('draft', 'Draft'),\n" +
               "            ('scheduled', 'Scheduled'),\n" +
               "        )\n\n" +
               "    def queryset(self, request, queryset):\n" +
               "        from django.utils import timezone\n" +
               "        if self.value() == 'published':\n" +
               "            return queryset.filter(\n" +
               "                status='published',\n" +
               "                published_at__lte=timezone.now()\n" +
               "            )\n" +
               "        elif self.value() == 'draft':\n" +
               "            return queryset.filter(status='draft')\n" +
               "        elif self.value() == 'scheduled':\n" +
               "            return queryset.filter(\n" +
               "                status='published',\n" +
               "                published_at__gt=timezone.now()\n" +
               "            )\n\n" +
               "# Main admin classes\n" +
               "@admin.register(Category)\n" +
               "class CategoryAdmin(admin.ModelAdmin):\n" +
               "    list_display = [\n" +
               "        'name', 'slug', 'parent', 'article_count',\n" +
               "        'color_display', 'order', 'is_active'\n" +
               "    ]\n" +
               "    list_filter = ['is_active', 'parent']\n" +
               "    search_fields = ['name', 'description']\n" +
               "    prepopulated_fields = {'slug': ('name',)}\n" +
               "    ordering = ['order', 'name']\n" +
               "    list_editable = ['order', 'is_active']\n\n" +
               "    def article_count(self, obj):\n" +
               "        return obj.articles.count()\n" +
               "    article_count.short_description = 'Articles'\n\n" +
               "    def color_display(self, obj):\n" +
               "        return format_html(\n" +
               "            '<span style=\"color: {}; font-weight: bold;\">⬤</span> {}',\n" +
               "            obj.color, obj.color\n" +
               "        )\n" +
               "    color_display.short_description = 'Color'\n\n" +
               "@admin.register(Article)\n" +
               "class ArticleAdmin(ImportExportModelAdmin):\n" +
               "    resource_class = ArticleResource\n" +
               "    list_display = [\n" +
               "        'title', 'author', 'category', 'status_colored',\n" +
               "        'is_featured', 'is_premium', 'view_count',\n" +
               "        'avg_rating', 'published_at', 'thumbnail_preview'\n" +
               "    ]\n" +
               "    list_filter = [\n" +
               "        PublishedFilter, 'status', 'is_featured',\n" +
               "        'is_premium', 'category', 'author', 'created_at'\n" +
               "    ]\n" +
               "    search_fields = ['title', 'subtitle', 'summary', 'content']\n" +
               "    prepopulated_fields = {'slug': ('title',)}\n" +
               "    date_hierarchy = 'published_at'\n" +
               "    ordering = ['-created_at']\n" +
               "    \n" +
               "    fieldsets = (\n" +
               "        ('Basic Information', {\n" +
               "            'fields': (\n" +
               "                'title', 'slug', 'subtitle',\n" +
               "                'author', 'category', 'tags'\n" +
               "            )\n" +
               "        }),\n" +
               "        ('Content', {\n" +
               "            'fields': ('summary', 'content', 'markdown_content'),\n" +
               "            'classes': ('wide',)\n" +
               "        }),\n" +
               "        ('Media', {\n" +
               "            'fields': ('featured_image', 'thumbnail')\n" +
               "        }),\n" +
               "        ('Status & Visibility', {\n" +
               "            'fields': (\n" +
               "                'status', 'is_featured', 'is_premium',\n" +
               "                'published_at'\n" +
               "            )\n" +
               "        }),\n" +
               "        ('SEO', {\n" +
               "            'fields': (\n" +
               "                'meta_title', 'meta_description', 'meta_keywords'\n" +
               "            ),\n" +
               "            'classes': ('collapse',)\n" +
               "        }),\n" +
               "        ('Statistics', {\n" +
               "            'fields': (\n" +
               "                'view_count', 'like_count', 'share_count',\n" +
               "                'comment_count', 'reading_time'\n" +
               "            ),\n" +
               "            'classes': ('collapse',)\n" +
               "        })\n" +
               "    )\n" +
               "    \n" +
               "    readonly_fields = [\n" +
               "        'view_count', 'like_count', 'share_count',\n" +
               "        'comment_count', 'reading_time', 'uuid'\n" +
               "    ]\n" +
               "    \n" +
               "    inlines = [CommentInline, ContentRatingInline]\n" +
               "    \n" +
               "    actions = [\n" +
               "        'make_published', 'make_draft', 'make_featured',\n" +
               "        'remove_featured', 'export_as_markdown'\n" +
               "    ]\n\n" +
               "    def get_queryset(self, request):\n" +
               "        queryset = super().get_queryset(request)\n" +
               "        queryset = queryset.annotate(\n" +
               "            _avg_rating=Avg('ratings__rating'),\n" +
               "            _comment_count=Count('comments')\n" +
               "        )\n" +
               "        return queryset\n\n" +
               "    def status_colored(self, obj):\n" +
               "        colors = {\n" +
               "            'draft': '#999999',\n" +
               "            'review': '#FFA500',\n" +
               "            'published': '#00AA00',\n" +
               "            'archived': '#FF0000'\n" +
               "        }\n" +
               "        return format_html(\n" +
               "            '<span style=\"color: {}; font-weight: bold;\">{}</span>',\n" +
               "            colors.get(obj.status, '#000000'),\n" +
               "            obj.get_status_display()\n" +
               "        )\n" +
               "    status_colored.short_description = 'Status'\n" +
               "    status_colored.admin_order_field = 'status'\n\n" +
               "    def avg_rating(self, obj):\n" +
               "        if hasattr(obj, '_avg_rating') and obj._avg_rating:\n" +
               "            return format_html(\n" +
               "                '<span style=\"color: gold;\">{:.1f} ★</span>',\n" +
               "                obj._avg_rating\n" +
               "            )\n" +
               "        return '-'\n" +
               "    avg_rating.short_description = 'Rating'\n" +
               "    avg_rating.admin_order_field = '_avg_rating'\n\n" +
               "    def thumbnail_preview(self, obj):\n" +
               "        if obj.thumbnail:\n" +
               "            return format_html(\n" +
               "                '<img src=\"{}\" width=\"50\" height=\"50\" style=\"object-fit: cover;\"/>',\n" +
               "                obj.thumbnail.url\n" +
               "            )\n" +
               "        return '-'\n" +
               "    thumbnail_preview.short_description = 'Thumbnail'\n\n" +
               "    def make_published(self, request, queryset):\n" +
               "        from django.utils import timezone\n" +
               "        updated = queryset.update(\n" +
               "            status='published',\n" +
               "            published_at=timezone.now()\n" +
               "        )\n" +
               "        self.message_user(\n" +
               "            request,\n" +
               "            f'{updated} article(s) published successfully.'\n" +
               "        )\n" +
               "    make_published.short_description = 'Publish selected articles'\n\n" +
               "    def make_draft(self, request, queryset):\n" +
               "        updated = queryset.update(status='draft')\n" +
               "        self.message_user(\n" +
               "            request,\n" +
               "            f'{updated} article(s) moved to draft.'\n" +
               "        )\n" +
               "    make_draft.short_description = 'Move to draft'\n\n" +
               "    def make_featured(self, request, queryset):\n" +
               "        updated = queryset.update(is_featured=True)\n" +
               "        self.message_user(\n" +
               "            request,\n" +
               "            f'{updated} article(s) marked as featured.'\n" +
               "        )\n" +
               "    make_featured.short_description = 'Mark as featured'\n\n" +
               "    def remove_featured(self, request, queryset):\n" +
               "        updated = queryset.update(is_featured=False)\n" +
               "        self.message_user(\n" +
               "            request,\n" +
               "            f'{updated} article(s) removed from featured.'\n" +
               "        )\n" +
               "    remove_featured.short_description = 'Remove from featured'\n\n" +
               "    def export_as_markdown(self, request, queryset):\n" +
               "        import zipfile\n" +
               "        from django.http import HttpResponse\n" +
               "        from io import BytesIO\n" +
               "        \n" +
               "        zip_buffer = BytesIO()\n" +
               "        with zipfile.ZipFile(zip_buffer, 'w') as zip_file:\n" +
               "            for article in queryset:\n" +
               "                content = f'# {article.title}\\n\\n'\n" +
               "                if article.subtitle:\n" +
               "                    content += f'## {article.subtitle}\\n\\n'\n" +
               "                content += f'**Author:** {article.author}\\n'\n" +
               "                content += f'**Published:** {article.published_at}\\n\\n'\n" +
               "                content += article.markdown_content or article.content\n" +
               "                \n" +
               "                zip_file.writestr(\n" +
               "                    f'{article.slug}.md',\n" +
               "                    content.encode('utf-8')\n" +
               "                )\n" +
               "        \n" +
               "        response = HttpResponse(\n" +
               "            zip_buffer.getvalue(),\n" +
               "            content_type='application/zip'\n" +
               "        )\n" +
               "        response['Content-Disposition'] = 'attachment; filename=\"articles.zip\"'\n" +
               "        return response\n" +
               "    export_as_markdown.short_description = 'Export as Markdown'\n\n" +
               "@admin.register(Page)\n" +
               "class PageAdmin(admin.ModelAdmin):\n" +
               "    list_display = [\n" +
               "        'title', 'slug', 'template', 'is_active',\n" +
               "        'show_in_menu', 'menu_order', 'updated_at'\n" +
               "    ]\n" +
               "    list_filter = ['is_active', 'show_in_menu', 'template']\n" +
               "    search_fields = ['title', 'content']\n" +
               "    prepopulated_fields = {'slug': ('title',)}\n" +
               "    list_editable = ['is_active', 'show_in_menu', 'menu_order']\n" +
               "    ordering = ['menu_order', 'title']\n\n" +
               "@admin.register(Comment)\n" +
               "class CommentAdmin(admin.ModelAdmin):\n" +
               "    list_display = [\n" +
               "        'article', 'author', 'content_preview',\n" +
               "        'is_approved', 'is_featured', 'like_count',\n" +
               "        'created_at'\n" +
               "    ]\n" +
               "    list_filter = [\n" +
               "        'is_approved', 'is_featured',\n" +
               "        'created_at', 'article__category'\n" +
               "    ]\n" +
               "    search_fields = ['content', 'author__username', 'article__title']\n" +
               "    date_hierarchy = 'created_at'\n" +
               "    actions = ['approve_comments', 'reject_comments']\n" +
               "    list_editable = ['is_approved', 'is_featured']\n\n" +
               "    def content_preview(self, obj):\n" +
               "        return obj.content[:100] + '...' if len(obj.content) > 100 else obj.content\n" +
               "    content_preview.short_description = 'Content'\n\n" +
               "    def approve_comments(self, request, queryset):\n" +
               "        updated = queryset.update(is_approved=True)\n" +
               "        self.message_user(\n" +
               "            request,\n" +
               "            f'{updated} comment(s) approved.'\n" +
               "        )\n" +
               "    approve_comments.short_description = 'Approve selected comments'\n\n" +
               "    def reject_comments(self, request, queryset):\n" +
               "        updated = queryset.update(is_approved=False)\n" +
               "        self.message_user(\n" +
               "            request,\n" +
               "            f'{updated} comment(s) rejected.'\n" +
               "        )\n" +
               "    reject_comments.short_description = 'Reject selected comments'\n\n" +
               "@admin.register(Newsletter)\n" +
               "class NewsletterAdmin(admin.ModelAdmin):\n" +
               "    list_display = [\n" +
               "        'email', 'name', 'is_active', 'confirmed',\n" +
               "        'category_list', 'created_at'\n" +
               "    ]\n" +
               "    list_filter = ['is_active', 'confirmed', 'categories', 'created_at']\n" +
               "    search_fields = ['email', 'name']\n" +
               "    date_hierarchy = 'created_at'\n" +
               "    filter_horizontal = ['categories']\n" +
               "    actions = ['activate_subscriptions', 'deactivate_subscriptions']\n\n" +
               "    def category_list(self, obj):\n" +
               "        return ', '.join([c.name for c in obj.categories.all()])\n" +
               "    category_list.short_description = 'Categories'\n\n" +
               "    def activate_subscriptions(self, request, queryset):\n" +
               "        updated = queryset.update(is_active=True)\n" +
               "        self.message_user(\n" +
               "            request,\n" +
               "            f'{updated} subscription(s) activated.'\n" +
               "        )\n" +
               "    activate_subscriptions.short_description = 'Activate subscriptions'\n\n" +
               "    def deactivate_subscriptions(self, request, queryset):\n" +
               "        updated = queryset.update(is_active=False)\n" +
               "        self.message_user(\n" +
               "            request,\n" +
               "            f'{updated} subscription(s) deactivated.'\n" +
               "        )\n" +
               "    deactivate_subscriptions.short_description = 'Deactivate subscriptions'\n\n" +
               "@admin.register(ContentView)\n" +
               "class ContentViewAdmin(admin.ModelAdmin):\n" +
               "    list_display = [\n" +
               "        'article', 'user', 'ip_address',\n" +
               "        'referrer_domain', 'created_at'\n" +
               "    ]\n" +
               "    list_filter = ['created_at', 'article__category']\n" +
               "    search_fields = ['article__title', 'user__username', 'ip_address']\n" +
               "    date_hierarchy = 'created_at'\n" +
               "    readonly_fields = ['article', 'user', 'ip_address', 'user_agent', 'referrer']\n\n" +
               "    def referrer_domain(self, obj):\n" +
               "        if obj.referrer:\n" +
               "            from urllib.parse import urlparse\n" +
               "            return urlparse(obj.referrer).netloc\n" +
               "        return '-'\n" +
               "    referrer_domain.short_description = 'Referrer'\n\n" +
               "# Register remaining models\n" +
               "admin.site.register(Like)\n" +
               "admin.site.register(Bookmark)\n" +
               "admin.site.register(ContentRating)\n";
    private String generateMediaModels() {
        return "from django.db import models\n" +
               "from django.contrib.auth import get_user_model\n" +
               "from django.core.validators import FileExtensionValidator\n" +
               "from django.utils import timezone\n" +
               "import uuid\n" +
               "import os\n\n" +
               "User = get_user_model()\n\n" +
               "class MediaType(models.TextChoices):\n" +
               "    IMAGE = 'image', 'Image'\n" +
               "    VIDEO = 'video', 'Video'\n" +
               "    AUDIO = 'audio', 'Audio'\n" +
               "    DOCUMENT = 'document', 'Document'\n" +
               "    OTHER = 'other', 'Other'\n\n" +
               "class Media(models.Model):\n" +
               "    \"\"\"Generic media file model\"\"\"\n" +
               "    uuid = models.UUIDField(default=uuid.uuid4, editable=False, unique=True)\n" +
               "    name = models.CharField(max_length=255)\n" +
               "    file = models.FileField(\n" +
               "        upload_to='media/%Y/%m/%d/',\n" +
               "        validators=[FileExtensionValidator(\n" +
               "            allowed_extensions=['jpg', 'jpeg', 'png', 'gif', 'mp4', 'webm', 'mp3', 'wav', 'pdf', 'doc', 'docx']\n" +
               "        )]\n" +
               "    )\n" +
               "    media_type = models.CharField(max_length=20, choices=MediaType.choices)\n" +
               "    mime_type = models.CharField(max_length=100, blank=True)\n" +
               "    size = models.BigIntegerField(default=0)\n" +
               "    width = models.IntegerField(null=True, blank=True)\n" +
               "    height = models.IntegerField(null=True, blank=True)\n" +
               "    duration = models.FloatField(null=True, blank=True, help_text='Duration in seconds for audio/video')\n" +
               "    \n" +
               "    thumbnail = models.ImageField(upload_to='thumbnails/%Y/%m/%d/', null=True, blank=True)\n" +
               "    alt_text = models.CharField(max_length=255, blank=True)\n" +
               "    caption = models.TextField(blank=True)\n" +
               "    \n" +
               "    uploaded_by = models.ForeignKey(User, on_delete=models.SET_NULL, null=True, related_name='uploaded_media')\n" +
               "    is_public = models.BooleanField(default=False)\n" +
               "    tags = models.JSONField(default=list, blank=True)\n" +
               "    metadata = models.JSONField(default=dict, blank=True)\n" +
               "    \n" +
               "    created_at = models.DateTimeField(auto_now_add=True)\n" +
               "    updated_at = models.DateTimeField(auto_now=True)\n\n" +
               "    class Meta:\n" +
               "        ordering = ['-created_at']\n" +
               "        indexes = [\n" +
               "            models.Index(fields=['media_type', 'created_at']),\n" +
               "            models.Index(fields=['uploaded_by', 'is_public']),\n" +
               "        ]\n\n" +
               "    def save(self, *args, **kwargs):\n" +
               "        if self.file:\n" +
               "            self.size = self.file.size\n" +
               "            self.mime_type = self.get_mime_type()\n" +
               "        super().save(*args, **kwargs)\n\n" +
               "    def get_mime_type(self):\n" +
               "        import mimetypes\n" +
               "        mime_type, _ = mimetypes.guess_type(self.file.name)\n" +
               "        return mime_type or 'application/octet-stream'\n\n" +
               "    def __str__(self):\n" +
               "        return self.name\n\n" +
               "class ImageProcessingJob(models.Model):\n" +
               "    \"\"\"Track image processing jobs\"\"\"\n" +
               "    media = models.ForeignKey(Media, on_delete=models.CASCADE, related_name='processing_jobs')\n" +
               "    operation = models.CharField(max_length=50)\n" +
               "    parameters = models.JSONField(default=dict)\n" +
               "    status = models.CharField(\n" +
               "        max_length=20,\n" +
               "        choices=[\n" +
               "            ('pending', 'Pending'),\n" +
               "            ('processing', 'Processing'),\n" +
               "            ('completed', 'Completed'),\n" +
               "            ('failed', 'Failed')\n" +
               "        ],\n" +
               "        default='pending'\n" +
               "    )\n" +
               "    result_file = models.FileField(upload_to='processed/%Y/%m/%d/', null=True, blank=True)\n" +
               "    error_message = models.TextField(blank=True)\n" +
               "    started_at = models.DateTimeField(null=True, blank=True)\n" +
               "    completed_at = models.DateTimeField(null=True, blank=True)\n" +
               "    created_at = models.DateTimeField(auto_now_add=True)\n\n" +
               "    def __str__(self):\n" +
               "        return f'{self.operation} on {self.media.name}'\n";
    }

    private String generateMediaProcessors() {
        return "from PIL import Image, ImageOps\n" +
               "from django.core.files.base import ContentFile\n" +
               "from django.core.files.storage import default_storage\n" +
               "import io\n" +
               "import os\n" +
               "import logging\n" +
               "from celery import shared_task\n" +
               "import cv2\n" +
               "import numpy as np\n\n" +
               "logger = logging.getLogger(__name__)\n\n" +
               "class ImageProcessor:\n" +
               "    \"\"\"Image processing utilities\"\"\"\n" +
               "    \n" +
               "    @staticmethod\n" +
               "    def resize_image(image_file, width=None, height=None, maintain_aspect=True):\n" +
               "        \"\"\"Resize an image\"\"\"\n" +
               "        img = Image.open(image_file)\n" +
               "        \n" +
               "        if maintain_aspect:\n" +
               "            img.thumbnail((width or img.width, height or img.height), Image.LANCZOS)\n" +
               "        else:\n" +
               "            if width and height:\n" +
               "                img = img.resize((width, height), Image.LANCZOS)\n" +
               "        \n" +
               "        output = io.BytesIO()\n" +
               "        img.save(output, format=img.format or 'JPEG', quality=85, optimize=True)\n" +
               "        output.seek(0)\n" +
               "        return output\n" +
               "    \n" +
               "    @staticmethod\n" +
               "    def create_thumbnail(image_file, size=(300, 300)):\n" +
               "        \"\"\"Create a thumbnail from an image\"\"\"\n" +
               "        img = Image.open(image_file)\n" +
               "        img.thumbnail(size, Image.LANCZOS)\n" +
               "        \n" +
               "        # Convert to RGB if necessary\n" +
               "        if img.mode in ('RGBA', 'LA', 'P'):\n" +
               "            background = Image.new('RGB', img.size, (255, 255, 255))\n" +
               "            if img.mode == 'P':\n" +
               "                img = img.convert('RGBA')\n" +
               "            background.paste(img, mask=img.split()[-1] if img.mode == 'RGBA' else None)\n" +
               "            img = background\n" +
               "        \n" +
               "        output = io.BytesIO()\n" +
               "        img.save(output, format='JPEG', quality=85, optimize=True)\n" +
               "        output.seek(0)\n" +
               "        return output\n" +
               "    \n" +
               "    @staticmethod\n" +
               "    def optimize_image(image_file, max_width=1920, quality=85):\n" +
               "        \"\"\"Optimize image for web\"\"\"\n" +
               "        img = Image.open(image_file)\n" +
               "        \n" +
               "        # Resize if too large\n" +
               "        if img.width > max_width:\n" +
               "            ratio = max_width / img.width\n" +
               "            new_height = int(img.height * ratio)\n" +
               "            img = img.resize((max_width, new_height), Image.LANCZOS)\n" +
               "        \n" +
               "        # Convert to RGB if necessary\n" +
               "        if img.mode in ('RGBA', 'LA', 'P'):\n" +
               "            background = Image.new('RGB', img.size, (255, 255, 255))\n" +
               "            if img.mode == 'P':\n" +
               "                img = img.convert('RGBA')\n" +
               "            background.paste(img, mask=img.split()[-1] if img.mode == 'RGBA' else None)\n" +
               "            img = background\n" +
               "        \n" +
               "        output = io.BytesIO()\n" +
               "        img.save(output, format='JPEG', quality=quality, optimize=True)\n" +
               "        output.seek(0)\n" +
               "        return output\n" +
               "    \n" +
               "    @staticmethod\n" +
               "    def apply_watermark(image_file, watermark_file, position='bottom-right', opacity=0.5):\n" +
               "        \"\"\"Apply watermark to an image\"\"\"\n" +
               "        img = Image.open(image_file)\n" +
               "        watermark = Image.open(watermark_file)\n" +
               "        \n" +
               "        # Resize watermark to be 10% of image width\n" +
               "        watermark_width = int(img.width * 0.1)\n" +
               "        watermark_height = int(watermark.height * (watermark_width / watermark.width))\n" +
               "        watermark = watermark.resize((watermark_width, watermark_height), Image.LANCZOS)\n" +
               "        \n" +
               "        # Apply opacity\n" +
               "        if watermark.mode != 'RGBA':\n" +
               "            watermark = watermark.convert('RGBA')\n" +
               "        watermark.putalpha(int(255 * opacity))\n" +
               "        \n" +
               "        # Calculate position\n" +
               "        margin = 20\n" +
               "        if position == 'bottom-right':\n" +
               "            pos = (img.width - watermark_width - margin, img.height - watermark_height - margin)\n" +
               "        elif position == 'bottom-left':\n" +
               "            pos = (margin, img.height - watermark_height - margin)\n" +
               "        elif position == 'top-right':\n" +
               "            pos = (img.width - watermark_width - margin, margin)\n" +
               "        elif position == 'top-left':\n" +
               "            pos = (margin, margin)\n" +
               "        else:  # center\n" +
               "            pos = ((img.width - watermark_width) // 2, (img.height - watermark_height) // 2)\n" +
               "        \n" +
               "        img.paste(watermark, pos, watermark)\n" +
               "        \n" +
               "        output = io.BytesIO()\n" +
               "        img.save(output, format=img.format or 'JPEG', quality=85)\n" +
               "        output.seek(0)\n" +
               "        return output\n\n" +
               "@shared_task\n" +
               "def process_uploaded_image(media_id):\n" +
               "    \"\"\"Process uploaded image asynchronously\"\"\"\n" +
               "    from .models import Media, ImageProcessingJob\n" +
               "    \n" +
               "    try:\n" +
               "        media = Media.objects.get(id=media_id)\n" +
               "        processor = ImageProcessor()\n" +
               "        \n" +
               "        # Create thumbnail\n" +
               "        thumbnail = processor.create_thumbnail(media.file)\n" +
               "        media.thumbnail.save(\n" +
               "            f'thumb_{media.uuid}.jpg',\n" +
               "            ContentFile(thumbnail.read()),\n" +
               "            save=False\n" +
               "        )\n" +
               "        \n" +
               "        # Get image dimensions\n" +
               "        img = Image.open(media.file)\n" +
               "        media.width = img.width\n" +
               "        media.height = img.height\n" +
               "        \n" +
               "        # Extract metadata\n" +
               "        media.metadata = {\n" +
               "            'format': img.format,\n" +
               "            'mode': img.mode,\n" +
               "            'info': img.info\n" +
               "        }\n" +
               "        \n" +
               "        media.save()\n" +
               "        \n" +
               "        return {'status': 'success', 'media_id': media_id}\n" +
               "    \n" +
               "    except Exception as e:\n" +
               "        logger.error(f'Error processing image {media_id}: {e}')\n" +
               "        return {'status': 'error', 'message': str(e)}\n\n" +
               "class VideoProcessor:\n" +
               "    \"\"\"Video processing utilities\"\"\"\n" +
               "    \n" +
               "    @staticmethod\n" +
               "    def extract_thumbnail(video_file, time_offset=1):\n" +
               "        \"\"\"Extract thumbnail from video\"\"\"\n" +
               "        import tempfile\n" +
               "        \n" +
               "        with tempfile.NamedTemporaryFile(suffix='.mp4', delete=False) as tmp_file:\n" +
               "            tmp_file.write(video_file.read())\n" +
               "            tmp_path = tmp_file.name\n" +
               "        \n" +
               "        try:\n" +
               "            cap = cv2.VideoCapture(tmp_path)\n" +
               "            cap.set(cv2.CAP_PROP_POS_MSEC, time_offset * 1000)\n" +
               "            success, frame = cap.read()\n" +
               "            \n" +
               "            if success:\n" +
               "                # Convert BGR to RGB\n" +
               "                frame = cv2.cvtColor(frame, cv2.COLOR_BGR2RGB)\n" +
               "                img = Image.fromarray(frame)\n" +
               "                \n" +
               "                # Create thumbnail\n" +
               "                img.thumbnail((640, 360), Image.LANCZOS)\n" +
               "                \n" +
               "                output = io.BytesIO()\n" +
               "                img.save(output, format='JPEG', quality=85)\n" +
               "                output.seek(0)\n" +
               "                \n" +
               "                cap.release()\n" +
               "                return output\n" +
               "        finally:\n" +
               "            os.unlink(tmp_path)\n" +
               "        \n" +
               "        return None\n" +
               "    \n" +
               "    @staticmethod\n" +
               "    def get_video_info(video_file):\n" +
               "        \"\"\"Extract video information\"\"\"\n" +
               "        import tempfile\n" +
               "        \n" +
               "        with tempfile.NamedTemporaryFile(suffix='.mp4', delete=False) as tmp_file:\n" +
               "            tmp_file.write(video_file.read())\n" +
               "            tmp_path = tmp_file.name\n" +
               "        \n" +
               "        try:\n" +
               "            cap = cv2.VideoCapture(tmp_path)\n" +
               "            \n" +
               "            info = {\n" +
               "                'width': int(cap.get(cv2.CAP_PROP_FRAME_WIDTH)),\n" +
               "                'height': int(cap.get(cv2.CAP_PROP_FRAME_HEIGHT)),\n" +
               "                'fps': cap.get(cv2.CAP_PROP_FPS),\n" +
               "                'frame_count': int(cap.get(cv2.CAP_PROP_FRAME_COUNT)),\n" +
               "                'duration': cap.get(cv2.CAP_PROP_FRAME_COUNT) / cap.get(cv2.CAP_PROP_FPS)\n" +
               "            }\n" +
               "            \n" +
               "            cap.release()\n" +
               "            return info\n" +
               "        finally:\n" +
               "            os.unlink(tmp_path)\n\n" +
               "@shared_task\n" +
               "def process_uploaded_video(media_id):\n" +
               "    \"\"\"Process uploaded video asynchronously\"\"\"\n" +
               "    from .models import Media\n" +
               "    \n" +
               "    try:\n" +
               "        media = Media.objects.get(id=media_id)\n" +
               "        processor = VideoProcessor()\n" +
               "        \n" +
               "        # Extract thumbnail\n" +
               "        thumbnail = processor.extract_thumbnail(media.file)\n" +
               "        if thumbnail:\n" +
               "            media.thumbnail.save(\n" +
               "                f'thumb_{media.uuid}.jpg',\n" +
               "                ContentFile(thumbnail.read()),\n" +
               "                save=False\n" +
               "            )\n" +
               "        \n" +
               "        # Get video info\n" +
               "        media.file.seek(0)\n" +
               "        info = processor.get_video_info(media.file)\n" +
               "        \n" +
               "        media.width = info['width']\n" +
               "        media.height = info['height']\n" +
               "        media.duration = info['duration']\n" +
               "        media.metadata = info\n" +
               "        \n" +
               "        media.save()\n" +
               "        \n" +
               "        return {'status': 'success', 'media_id': media_id}\n" +
               "    \n" +
               "    except Exception as e:\n" +
               "        logger.error(f'Error processing video {media_id}: {e}')\n" +
               "        return {'status': 'error', 'message': str(e)}\n";
    }

    private String generateUserModels() {
        return "from django.contrib.auth.models import AbstractUser\n" +
               "from django.db import models\n" +
               "from django.utils import timezone\n" +
               "from django.core.validators import RegexValidator\n" +
               "import uuid\n\n" +
               "class CustomUser(AbstractUser):\n" +
               "    \"\"\"Extended user model with additional fields\"\"\"\n" +
               "    uuid = models.UUIDField(default=uuid.uuid4, editable=False, unique=True)\n" +
               "    \n" +
               "    # Profile information\n" +
               "    bio = models.TextField(max_length=500, blank=True)\n" +
               "    avatar = models.ImageField(upload_to='avatars/', null=True, blank=True)\n" +
               "    cover_image = models.ImageField(upload_to='covers/', null=True, blank=True)\n" +
               "    \n" +
               "    # Contact information\n" +
               "    phone_regex = RegexValidator(\n" +
               "        regex=r'^\\+?1?\\d{9,15}$',\n" +
               "        message=\"Phone number must be entered in the format: '+999999999'. Up to 15 digits allowed.\"\n" +
               "    )\n" +
               "    phone_number = models.CharField(validators=[phone_regex], max_length=17, blank=True)\n" +
               "    \n" +
               "    # Location\n" +
               "    country = models.CharField(max_length=100, blank=True)\n" +
               "    city = models.CharField(max_length=100, blank=True)\n" +
               "    timezone = models.CharField(max_length=50, default='UTC')\n" +
               "    \n" +
               "    # Preferences\n" +
               "    language = models.CharField(max_length=10, default='en')\n" +
               "    theme = models.CharField(\n" +
               "        max_length=20,\n" +
               "        choices=[('light', 'Light'), ('dark', 'Dark'), ('auto', 'Auto')],\n" +
               "        default='auto'\n" +
               "    )\n" +
               "    \n" +
               "    # Account status\n" +
               "    is_verified = models.BooleanField(default=False)\n" +
               "    is_premium = models.BooleanField(default=False)\n" +
               "    premium_until = models.DateTimeField(null=True, blank=True)\n" +
               "    \n" +
               "    # Social links\n" +
               "    website = models.URLField(blank=True)\n" +
               "    twitter = models.CharField(max_length=50, blank=True)\n" +
               "    linkedin = models.CharField(max_length=100, blank=True)\n" +
               "    github = models.CharField(max_length=50, blank=True)\n" +
               "    \n" +
               "    # Metadata\n" +
               "    last_seen = models.DateTimeField(null=True, blank=True)\n" +
               "    email_verified_at = models.DateTimeField(null=True, blank=True)\n" +
               "    \n" +
               "    class Meta:\n" +
               "        indexes = [\n" +
               "            models.Index(fields=['email']),\n" +
               "            models.Index(fields=['username']),\n" +
               "        ]\n\n" +
               "    @property\n" +
               "    def is_premium_active(self):\n" +
               "        if not self.is_premium:\n" +
               "            return False\n" +
               "        if self.premium_until:\n" +
               "            return self.premium_until > timezone.now()\n" +
               "        return True\n\n" +
               "    def get_full_name(self):\n" +
               "        return f'{self.first_name} {self.last_name}'.strip() or self.username\n\n" +
               "class UserProfile(models.Model):\n" +
               "    \"\"\"Additional user profile information\"\"\"\n" +
               "    user = models.OneToOneField(CustomUser, on_delete=models.CASCADE, related_name='profile')\n" +
               "    \n" +
               "    # Professional information\n" +
               "    job_title = models.CharField(max_length=100, blank=True)\n" +
               "    company = models.CharField(max_length=100, blank=True)\n" +
               "    industry = models.CharField(max_length=100, blank=True)\n" +
               "    years_experience = models.PositiveIntegerField(null=True, blank=True)\n" +
               "    \n" +
               "    # Statistics\n" +
               "    followers_count = models.PositiveIntegerField(default=0)\n" +
               "    following_count = models.PositiveIntegerField(default=0)\n" +
               "    posts_count = models.PositiveIntegerField(default=0)\n" +
               "    \n" +
               "    # Privacy settings\n" +
               "    is_public = models.BooleanField(default=True)\n" +
               "    show_email = models.BooleanField(default=False)\n" +
               "    show_phone = models.BooleanField(default=False)\n" +
               "    allow_messages = models.BooleanField(default=True)\n" +
               "    \n" +
               "    # Notification preferences\n" +
               "    email_notifications = models.BooleanField(default=True)\n" +
               "    push_notifications = models.BooleanField(default=True)\n" +
               "    newsletter_subscription = models.BooleanField(default=False)\n" +
               "    \n" +
               "    created_at = models.DateTimeField(auto_now_add=True)\n" +
               "    updated_at = models.DateTimeField(auto_now=True)\n\n" +
               "    def __str__(self):\n" +
               "        return f'{self.user.username} Profile'\n\n" +
               "class UserFollow(models.Model):\n" +
               "    \"\"\"User follow relationships\"\"\"\n" +
               "    follower = models.ForeignKey(\n" +
               "        CustomUser,\n" +
               "        on_delete=models.CASCADE,\n" +
               "        related_name='following'\n" +
               "    )\n" +
               "    following = models.ForeignKey(\n" +
               "        CustomUser,\n" +
               "        on_delete=models.CASCADE,\n" +
               "        related_name='followers'\n" +
               "    )\n" +
               "    created_at = models.DateTimeField(auto_now_add=True)\n\n" +
               "    class Meta:\n" +
               "        unique_together = ['follower', 'following']\n" +
               "        ordering = ['-created_at']\n\n" +
               "    def __str__(self):\n" +
               "        return f'{self.follower.username} follows {self.following.username}'\n";
    }

    private String generateUserAdmin() {
        return "from django.contrib import admin\n" +
               "from django.contrib.auth.admin import UserAdmin\n" +
               "from django.utils.html import format_html\n" +
               "from .models import CustomUser, UserProfile, UserFollow\n\n" +
               "class UserProfileInline(admin.StackedInline):\n" +
               "    model = UserProfile\n" +
               "    can_delete = False\n" +
               "    verbose_name_plural = 'Profile'\n\n" +
               "@admin.register(CustomUser)\n" +
               "class CustomUserAdmin(UserAdmin):\n" +
               "    list_display = [\n" +
               "        'username', 'email', 'get_full_name', 'is_verified',\n" +
               "        'is_premium', 'is_staff', 'date_joined', 'avatar_preview'\n" +
               "    ]\n" +
               "    list_filter = [\n" +
               "        'is_staff', 'is_superuser', 'is_active',\n" +
               "        'is_verified', 'is_premium', 'date_joined'\n" +
               "    ]\n" +
               "    search_fields = ['username', 'first_name', 'last_name', 'email']\n" +
               "    ordering = ['-date_joined']\n" +
               "    \n" +
               "    fieldsets = UserAdmin.fieldsets + (\n" +
               "        ('Profile', {\n" +
               "            'fields': (\n" +
               "                'bio', 'avatar', 'cover_image',\n" +
               "                'phone_number', 'country', 'city', 'timezone'\n" +
               "            )\n" +
               "        }),\n" +
               "        ('Account Status', {\n" +
               "            'fields': (\n" +
               "                'is_verified', 'is_premium', 'premium_until',\n" +
               "                'email_verified_at', 'last_seen'\n" +
               "            )\n" +
               "        }),\n" +
               "        ('Social Links', {\n" +
               "            'fields': ('website', 'twitter', 'linkedin', 'github')\n" +
               "        }),\n" +
               "        ('Preferences', {\n" +
               "            'fields': ('language', 'theme')\n" +
               "        }),\n" +
               "    )\n" +
               "    \n" +
               "    inlines = [UserProfileInline]\n" +
               "    \n" +
               "    def avatar_preview(self, obj):\n" +
               "        if obj.avatar:\n" +
               "            return format_html(\n" +
               "                '<img src=\"{}\" width=\"30\" height=\"30\" style=\"border-radius: 50%;\"/>',\n" +
               "                obj.avatar.url\n" +
               "            )\n" +
               "        return '-'\n" +
               "    avatar_preview.short_description = 'Avatar'\n\n" +
               "@admin.register(UserProfile)\n" +
               "class UserProfileAdmin(admin.ModelAdmin):\n" +
               "    list_display = [\n" +
               "        'user', 'job_title', 'company',\n" +
               "        'followers_count', 'following_count',\n" +
               "        'is_public', 'created_at'\n" +
               "    ]\n" +
               "    list_filter = ['is_public', 'newsletter_subscription', 'created_at']\n" +
               "    search_fields = ['user__username', 'user__email', 'job_title', 'company']\n\n" +
               "@admin.register(UserFollow)\n" +
               "class UserFollowAdmin(admin.ModelAdmin):\n" +
               "    list_display = ['follower', 'following', 'created_at']\n" +
               "    list_filter = ['created_at']\n" +
               "    search_fields = ['follower__username', 'following__username']\n" +
               "    date_hierarchy = 'created_at'\n";
    }

    private String generateProductModels() {
        return "from django.db import models\n" +
               "from django.contrib.auth import get_user_model\n" +
               "from django.utils.text import slugify\n" +
               "from django.core.validators import MinValueValidator, MaxValueValidator\n" +
               "from decimal import Decimal\n" +
               "import uuid\n\n" +
               "User = get_user_model()\n\n" +
               "class ProductCategory(models.Model):\n" +
               "    \"\"\"Product category model\"\"\"\n" +
               "    name = models.CharField(max_length=100, unique=True)\n" +
               "    slug = models.SlugField(max_length=100, unique=True, blank=True)\n" +
               "    description = models.TextField(blank=True)\n" +
               "    parent = models.ForeignKey(\n" +
               "        'self', null=True, blank=True,\n" +
               "        on_delete=models.CASCADE, related_name='children'\n" +
               "    )\n" +
               "    image = models.ImageField(upload_to='categories/', null=True, blank=True)\n" +
               "    is_active = models.BooleanField(default=True)\n" +
               "    order = models.IntegerField(default=0)\n" +
               "    created_at = models.DateTimeField(auto_now_add=True)\n" +
               "    updated_at = models.DateTimeField(auto_now=True)\n\n" +
               "    class Meta:\n" +
               "        verbose_name_plural = 'Product Categories'\n" +
               "        ordering = ['order', 'name']\n\n" +
               "    def save(self, *args, **kwargs):\n" +
               "        if not self.slug:\n" +
               "            self.slug = slugify(self.name)\n" +
               "        super().save(*args, **kwargs)\n\n" +
               "    def __str__(self):\n" +
               "        return self.name\n\n" +
               "class Product(models.Model):\n" +
               "    \"\"\"Product model\"\"\"\n" +
               "    uuid = models.UUIDField(default=uuid.uuid4, editable=False, unique=True)\n" +
               "    sku = models.CharField(max_length=50, unique=True)\n" +
               "    name = models.CharField(max_length=200)\n" +
               "    slug = models.SlugField(max_length=200, unique=True, blank=True)\n" +
               "    \n" +
               "    description = models.TextField()\n" +
               "    short_description = models.TextField(max_length=500, blank=True)\n" +
               "    \n" +
               "    category = models.ForeignKey(\n" +
               "        ProductCategory,\n" +
               "        on_delete=models.SET_NULL,\n" +
               "        null=True,\n" +
               "        related_name='products'\n" +
               "    )\n" +
               "    \n" +
               "    # Pricing\n" +
               "    price = models.DecimalField(\n" +
               "        max_digits=10,\n" +
               "        decimal_places=2,\n" +
               "        validators=[MinValueValidator(Decimal('0.01'))]\n" +
               "    )\n" +
               "    compare_price = models.DecimalField(\n" +
               "        max_digits=10,\n" +
               "        decimal_places=2,\n" +
               "        null=True,\n" +
               "        blank=True,\n" +
               "        help_text='Original price for showing discounts'\n" +
               "    )\n" +
               "    cost = models.DecimalField(\n" +
               "        max_digits=10,\n" +
               "        decimal_places=2,\n" +
               "        null=True,\n" +
               "        blank=True,\n" +
               "        help_text='Cost of goods sold'\n" +
               "    )\n" +
               "    \n" +
               "    # Inventory\n" +
               "    stock_quantity = models.IntegerField(default=0)\n" +
               "    track_inventory = models.BooleanField(default=True)\n" +
               "    allow_backorder = models.BooleanField(default=False)\n" +
               "    \n" +
               "    # Product details\n" +
               "    weight = models.DecimalField(\n" +
               "        max_digits=10,\n" +
               "        decimal_places=2,\n" +
               "        null=True,\n" +
               "        blank=True,\n" +
               "        help_text='Weight in kg'\n" +
               "    )\n" +
               "    dimensions = models.JSONField(default=dict, blank=True)  # {length, width, height}\n" +
               "    \n" +
               "    # Media\n" +
               "    featured_image = models.ImageField(upload_to='products/featured/', null=True, blank=True)\n" +
               "    \n" +
               "    # Status\n" +
               "    is_active = models.BooleanField(default=True)\n" +
               "    is_featured = models.BooleanField(default=False)\n" +
               "    is_digital = models.BooleanField(default=False)\n" +
               "    \n" +
               "    # SEO\n" +
               "    meta_title = models.CharField(max_length=60, blank=True)\n" +
               "    meta_description = models.CharField(max_length=160, blank=True)\n" +
               "    \n" +
               "    # Statistics\n" +
               "    view_count = models.PositiveIntegerField(default=0)\n" +
               "    sold_count = models.PositiveIntegerField(default=0)\n" +
               "    rating_average = models.DecimalField(\n" +
               "        max_digits=3,\n" +
               "        decimal_places=2,\n" +
               "        null=True,\n" +
               "        blank=True\n" +
               "    )\n" +
               "    rating_count = models.PositiveIntegerField(default=0)\n" +
               "    \n" +
               "    created_at = models.DateTimeField(auto_now_add=True)\n" +
               "    updated_at = models.DateTimeField(auto_now=True)\n\n" +
               "    class Meta:\n" +
               "        ordering = ['-created_at']\n" +
               "        indexes = [\n" +
               "            models.Index(fields=['slug']),\n" +
               "            models.Index(fields=['sku']),\n" +
               "            models.Index(fields=['is_active', 'is_featured']),\n" +
               "        ]\n\n" +
               "    def save(self, *args, **kwargs):\n" +
               "        if not self.slug:\n" +
               "            self.slug = slugify(self.name)\n" +
               "        super().save(*args, **kwargs)\n\n" +
               "    def __str__(self):\n" +
               "        return self.name\n\n" +
               "    @property\n" +
               "    def in_stock(self):\n" +
               "        return self.stock_quantity > 0 or not self.track_inventory or self.allow_backorder\n\n" +
               "    @property\n" +
               "    def discount_percentage(self):\n" +
               "        if self.compare_price and self.compare_price > self.price:\n" +
               "            return int(((self.compare_price - self.price) / self.compare_price) * 100)\n" +
               "        return 0\n\n" +
               "class ProductImage(models.Model):\n" +
               "    \"\"\"Product images\"\"\"\n" +
               "    product = models.ForeignKey(Product, on_delete=models.CASCADE, related_name='images')\n" +
               "    image = models.ImageField(upload_to='products/gallery/')\n" +
               "    alt_text = models.CharField(max_length=255, blank=True)\n" +
               "    is_primary = models.BooleanField(default=False)\n" +
               "    order = models.IntegerField(default=0)\n" +
               "    created_at = models.DateTimeField(auto_now_add=True)\n\n" +
               "    class Meta:\n" +
               "        ordering = ['order', 'created_at']\n\n" +
               "class ProductVariant(models.Model):\n" +
               "    \"\"\"Product variants (size, color, etc.)\"\"\"\n" +
               "    product = models.ForeignKey(Product, on_delete=models.CASCADE, related_name='variants')\n" +
               "    sku = models.CharField(max_length=50, unique=True)\n" +
               "    name = models.CharField(max_length=100)\n" +
               "    \n" +
               "    # Variant options\n" +
               "    options = models.JSONField(default=dict)  # {size: 'L', color: 'Blue'}\n" +
               "    \n" +
               "    # Pricing\n" +
               "    price = models.DecimalField(\n" +
               "        max_digits=10,\n" +
               "        decimal_places=2,\n" +
               "        null=True,\n" +
               "        blank=True,\n" +
               "        help_text='Leave blank to use product price'\n" +
               "    )\n" +
               "    \n" +
               "    # Inventory\n" +
               "    stock_quantity = models.IntegerField(default=0)\n" +
               "    \n" +
               "    image = models.ImageField(upload_to='products/variants/', null=True, blank=True)\n" +
               "    is_active = models.BooleanField(default=True)\n" +
               "    \n" +
               "    created_at = models.DateTimeField(auto_now_add=True)\n" +
               "    updated_at = models.DateTimeField(auto_now=True)\n\n" +
               "    def __str__(self):\n" +
               "        return f'{self.product.name} - {self.name}'\n\n" +
               "class ProductReview(models.Model):\n" +
               "    \"\"\"Product reviews\"\"\"\n" +
               "    product = models.ForeignKey(Product, on_delete=models.CASCADE, related_name='reviews')\n" +
               "    user = models.ForeignKey(User, on_delete=models.CASCADE, related_name='product_reviews')\n" +
               "    \n" +
               "    rating = models.IntegerField(\n" +
               "        validators=[MinValueValidator(1), MaxValueValidator(5)]\n" +
               "    )\n" +
               "    title = models.CharField(max_length=200)\n" +
               "    comment = models.TextField()\n" +
               "    \n" +
               "    is_verified_purchase = models.BooleanField(default=False)\n" +
               "    is_approved = models.BooleanField(default=False)\n" +
               "    \n" +
               "    helpful_count = models.PositiveIntegerField(default=0)\n" +
               "    \n" +
               "    created_at = models.DateTimeField(auto_now_add=True)\n" +
               "    updated_at = models.DateTimeField(auto_now=True)\n\n" +
               "    class Meta:\n" +
               "        unique_together = ['product', 'user']\n" +
               "        ordering = ['-created_at']\n\n" +
               "    def __str__(self):\n" +
               "        return f'{self.user.username} - {self.product.name} - {self.rating}★'\n";
    private String generateProductViews() {
        return "from django.views.generic import ListView, DetailView, View\n" +
               "from django.shortcuts import get_object_or_404, redirect\n" +
               "from django.http import JsonResponse\n" +
               "from django.db.models import Q, Avg, Count\n" +
               "from django.core.paginator import Paginator\n" +
               "from django.contrib.auth.decorators import login_required\n" +
               "from django.utils.decorators import method_decorator\n" +
               "from django.views.decorators.csrf import csrf_exempt\n" +
               "from .models import Product, ProductCategory, ProductReview\n" +
               "import json\n\n" +
               "class ProductListView(ListView):\n" +
               "    model = Product\n" +
               "    template_name = 'products/list.html'\n" +
               "    context_object_name = 'products'\n" +
               "    paginate_by = 12\n\n" +
               "    def get_queryset(self):\n" +
               "        queryset = Product.objects.filter(is_active=True)\n" +
               "        \n" +
               "        # Category filter\n" +
               "        category_slug = self.request.GET.get('category')\n" +
               "        if category_slug:\n" +
               "            category = get_object_or_404(ProductCategory, slug=category_slug)\n" +
               "            queryset = queryset.filter(category=category)\n" +
               "        \n" +
               "        # Search\n" +
               "        search_query = self.request.GET.get('q')\n" +
               "        if search_query:\n" +
               "            queryset = queryset.filter(\n" +
               "                Q(name__icontains=search_query) |\n" +
               "                Q(description__icontains=search_query) |\n" +
               "                Q(sku__icontains=search_query)\n" +
               "            )\n" +
               "        \n" +
               "        # Price range\n" +
               "        min_price = self.request.GET.get('min_price')\n" +
               "        max_price = self.request.GET.get('max_price')\n" +
               "        if min_price:\n" +
               "            queryset = queryset.filter(price__gte=min_price)\n" +
               "        if max_price:\n" +
               "            queryset = queryset.filter(price__lte=max_price)\n" +
               "        \n" +
               "        # Sorting\n" +
               "        sort_by = self.request.GET.get('sort', '-created_at')\n" +
               "        sort_options = {\n" +
               "            'price_asc': 'price',\n" +
               "            'price_desc': '-price',\n" +
               "            'name': 'name',\n" +
               "            'newest': '-created_at',\n" +
               "            'popular': '-view_count',\n" +
               "            'rating': '-rating_average'\n" +
               "        }\n" +
               "        queryset = queryset.order_by(sort_options.get(sort_by, '-created_at'))\n" +
               "        \n" +
               "        # Prefetch related\n" +
               "        queryset = queryset.select_related('category').prefetch_related('images', 'variants')\n" +
               "        \n" +
               "        return queryset\n\n" +
               "    def get_context_data(self, **kwargs):\n" +
               "        context = super().get_context_data(**kwargs)\n" +
               "        context['categories'] = ProductCategory.objects.filter(is_active=True)\n" +
               "        context['current_category'] = self.request.GET.get('category')\n" +
               "        context['search_query'] = self.request.GET.get('q', '')\n" +
               "        context['sort_by'] = self.request.GET.get('sort', 'newest')\n" +
               "        return context\n\n" +
               "class ProductDetailView(DetailView):\n" +
               "    model = Product\n" +
               "    template_name = 'products/detail.html'\n" +
               "    context_object_name = 'product'\n\n" +
               "    def get_object(self):\n" +
               "        product = get_object_or_404(\n" +
               "            Product.objects.select_related('category').prefetch_related(\n" +
               "                'images', 'variants', 'reviews__user'\n" +
               "            ),\n" +
               "            slug=self.kwargs['slug'],\n" +
               "            is_active=True\n" +
               "        )\n" +
               "        \n" +
               "        # Increment view count\n" +
               "        product.view_count += 1\n" +
               "        product.save(update_fields=['view_count'])\n" +
               "        \n" +
               "        return product\n\n" +
               "    def get_context_data(self, **kwargs):\n" +
               "        context = super().get_context_data(**kwargs)\n" +
               "        product = self.object\n" +
               "        \n" +
               "        # Related products\n" +
               "        context['related_products'] = Product.objects.filter(\n" +
               "            category=product.category,\n" +
               "            is_active=True\n" +
               "        ).exclude(id=product.id)[:4]\n" +
               "        \n" +
               "        # Reviews\n" +
               "        reviews = product.reviews.filter(is_approved=True)\n" +
               "        context['reviews'] = reviews[:10]\n" +
               "        context['review_stats'] = reviews.aggregate(\n" +
               "            avg_rating=Avg('rating'),\n" +
               "            total_reviews=Count('id')\n" +
               "        )\n" +
               "        \n" +
               "        # Rating distribution\n" +
               "        rating_dist = {}\n" +
               "        for i in range(1, 6):\n" +
               "            rating_dist[i] = reviews.filter(rating=i).count()\n" +
               "        context['rating_distribution'] = rating_dist\n" +
               "        \n" +
               "        # User review\n" +
               "        if self.request.user.is_authenticated:\n" +
               "            context['user_review'] = reviews.filter(user=self.request.user).first()\n" +
               "        \n" +
               "        return context\n\n" +
               "class ProductQuickView(View):\n" +
               "    \"\"\"AJAX endpoint for product quick view\"\"\"\n" +
               "    \n" +
               "    def get(self, request, pk):\n" +
               "        product = get_object_or_404(Product, pk=pk, is_active=True)\n" +
               "        \n" +
               "        data = {\n" +
               "            'id': product.id,\n" +
               "            'name': product.name,\n" +
               "            'price': str(product.price),\n" +
               "            'compare_price': str(product.compare_price) if product.compare_price else None,\n" +
               "            'description': product.short_description or product.description[:200],\n" +
               "            'in_stock': product.in_stock,\n" +
               "            'stock_quantity': product.stock_quantity,\n" +
               "            'images': [\n" +
               "                {'url': img.image.url, 'alt': img.alt_text}\n" +
               "                for img in product.images.all()[:3]\n" +
               "            ],\n" +
               "            'variants': [\n" +
               "                {\n" +
               "                    'id': v.id,\n" +
               "                    'name': v.name,\n" +
               "                    'price': str(v.price) if v.price else str(product.price),\n" +
               "                    'in_stock': v.stock_quantity > 0,\n" +
               "                    'options': v.options\n" +
               "                }\n" +
               "                for v in product.variants.filter(is_active=True)\n" +
               "            ],\n" +
               "            'rating': {\n" +
               "                'average': float(product.rating_average) if product.rating_average else 0,\n" +
               "                'count': product.rating_count\n" +
               "            }\n" +
               "        }\n" +
               "        \n" +
               "        return JsonResponse(data)\n\n" +
               "@method_decorator(login_required, name='dispatch')\n" +
               "class AddReviewView(View):\n" +
               "    \"\"\"Add product review\"\"\"\n" +
               "    \n" +
               "    def post(self, request, pk):\n" +
               "        product = get_object_or_404(Product, pk=pk)\n" +
               "        \n" +
               "        try:\n" +
               "            data = json.loads(request.body)\n" +
               "            \n" +
               "            review, created = ProductReview.objects.update_or_create(\n" +
               "                product=product,\n" +
               "                user=request.user,\n" +
               "                defaults={\n" +
               "                    'rating': data['rating'],\n" +
               "                    'title': data.get('title', ''),\n" +
               "                    'comment': data.get('comment', ''),\n" +
               "                    'is_approved': True  # Auto-approve for authenticated users\n" +
               "                }\n" +
               "            )\n" +
               "            \n" +
               "            # Update product rating\n" +
               "            reviews = product.reviews.filter(is_approved=True)\n" +
               "            product.rating_average = reviews.aggregate(Avg('rating'))['rating__avg']\n" +
               "            product.rating_count = reviews.count()\n" +
               "            product.save(update_fields=['rating_average', 'rating_count'])\n" +
               "            \n" +
               "            return JsonResponse({\n" +
               "                'success': True,\n" +
               "                'message': 'Review added successfully',\n" +
               "                'created': created\n" +
               "            })\n" +
               "        \n" +
               "        except Exception as e:\n" +
               "            return JsonResponse({\n" +
               "                'success': False,\n" +
               "                'message': str(e)\n" +
               "            }, status=400)\n\n" +
               "class ProductSearchAPI(View):\n" +
               "    \"\"\"Product search API endpoint\"\"\"\n" +
               "    \n" +
               "    def get(self, request):\n" +
               "        query = request.GET.get('q', '')\n" +
               "        limit = int(request.GET.get('limit', 10))\n" +
               "        \n" +
               "        if len(query) < 2:\n" +
               "            return JsonResponse({'results': []})\n" +
               "        \n" +
               "        products = Product.objects.filter(\n" +
               "            Q(name__icontains=query) |\n" +
               "            Q(sku__icontains=query),\n" +
               "            is_active=True\n" +
               "        )[:limit]\n" +
               "        \n" +
               "        results = [\n" +
               "            {\n" +
               "                'id': p.id,\n" +
               "                'name': p.name,\n" +
               "                'slug': p.slug,\n" +
               "                'price': str(p.price),\n" +
               "                'image': p.featured_image.url if p.featured_image else None,\n" +
               "                'category': p.category.name if p.category else None\n" +
               "            }\n" +
               "            for p in products\n" +
               "        ]\n" +
               "        \n" +
               "        return JsonResponse({'results': results})\n";
    }

    private String generateCartClass() {
        return "from decimal import Decimal\n" +
               "from django.conf import settings\n" +
               "from django.contrib.sessions.backends.base import SessionBase\n" +
               "from .models import Product, ProductVariant\n" +
               "import json\n\n" +
               "class Cart:\n" +
               "    \"\"\"Shopping cart implementation using sessions\"\"\"\n" +
               "    \n" +
               "    def __init__(self, request):\n" +
               "        \"\"\"Initialize the cart\"\"\"\n" +
               "        self.session = request.session\n" +
               "        cart = self.session.get(settings.CART_SESSION_ID)\n" +
               "        if not cart:\n" +
               "            cart = self.session[settings.CART_SESSION_ID] = {}\n" +
               "        self.cart = cart\n" +
               "    \n" +
               "    def add(self, product, variant=None, quantity=1, override_quantity=False):\n" +
               "        \"\"\"Add a product to the cart or update its quantity\"\"\"\n" +
               "        # Create unique cart key\n" +
               "        cart_key = self._get_cart_key(product, variant)\n" +
               "        \n" +
               "        if cart_key not in self.cart:\n" +
               "            # Determine price\n" +
               "            if variant and variant.price:\n" +
               "                price = str(variant.price)\n" +
               "            else:\n" +
               "                price = str(product.price)\n" +
               "            \n" +
               "            self.cart[cart_key] = {\n" +
               "                'product_id': product.id,\n" +
               "                'variant_id': variant.id if variant else None,\n" +
               "                'name': product.name,\n" +
               "                'variant_name': variant.name if variant else None,\n" +
               "                'price': price,\n" +
               "                'quantity': 0,\n" +
               "                'image': product.featured_image.url if product.featured_image else None,\n" +
               "                'slug': product.slug\n" +
               "            }\n" +
               "        \n" +
               "        if override_quantity:\n" +
               "            self.cart[cart_key]['quantity'] = quantity\n" +
               "        else:\n" +
               "            self.cart[cart_key]['quantity'] += quantity\n" +
               "        \n" +
               "        # Check stock\n" +
               "        if variant:\n" +
               "            available = variant.stock_quantity\n" +
               "        else:\n" +
               "            available = product.stock_quantity\n" +
               "        \n" +
               "        if product.track_inventory and not product.allow_backorder:\n" +
               "            if self.cart[cart_key]['quantity'] > available:\n" +
               "                self.cart[cart_key]['quantity'] = available\n" +
               "        \n" +
               "        self.save()\n" +
               "    \n" +
               "    def remove(self, product, variant=None):\n" +
               "        \"\"\"Remove a product from the cart\"\"\"\n" +
               "        cart_key = self._get_cart_key(product, variant)\n" +
               "        \n" +
               "        if cart_key in self.cart:\n" +
               "            del self.cart[cart_key]\n" +
               "            self.save()\n" +
               "    \n" +
               "    def update_quantity(self, product, variant=None, quantity=1):\n" +
               "        \"\"\"Update product quantity in cart\"\"\"\n" +
               "        cart_key = self._get_cart_key(product, variant)\n" +
               "        \n" +
               "        if cart_key in self.cart:\n" +
               "            if quantity <= 0:\n" +
               "                self.remove(product, variant)\n" +
               "            else:\n" +
               "                self.cart[cart_key]['quantity'] = quantity\n" +
               "                self.save()\n" +
               "    \n" +
               "    def save(self):\n" +
               "        \"\"\"Mark the session as modified\"\"\"\n" +
               "        self.session.modified = True\n" +
               "    \n" +
               "    def clear(self):\n" +
               "        \"\"\"Clear the cart\"\"\"\n" +
               "        del self.session[settings.CART_SESSION_ID]\n" +
               "        self.save()\n" +
               "    \n" +
               "    def get_items(self):\n" +
               "        \"\"\"Get all cart items with product objects\"\"\"\n" +
               "        items = []\n" +
               "        product_ids = [item['product_id'] for item in self.cart.values()]\n" +
               "        products = Product.objects.filter(id__in=product_ids)\n" +
               "        products_dict = {p.id: p for p in products}\n" +
               "        \n" +
               "        variant_ids = [item['variant_id'] for item in self.cart.values() if item['variant_id']]\n" +
               "        variants = ProductVariant.objects.filter(id__in=variant_ids)\n" +
               "        variants_dict = {v.id: v for v in variants}\n" +
               "        \n" +
               "        for cart_key, item in self.cart.items():\n" +
               "            product = products_dict.get(item['product_id'])\n" +
               "            variant = variants_dict.get(item['variant_id']) if item['variant_id'] else None\n" +
               "            \n" +
               "            if product:\n" +
               "                price = Decimal(item['price'])\n" +
               "                items.append({\n" +
               "                    'cart_key': cart_key,\n" +
               "                    'product': product,\n" +
               "                    'variant': variant,\n" +
               "                    'price': price,\n" +
               "                    'quantity': item['quantity'],\n" +
               "                    'total_price': price * item['quantity']\n" +
               "                })\n" +
               "        \n" +
               "        return items\n" +
               "    \n" +
               "    def get_total_price(self):\n" +
               "        \"\"\"Calculate total price of all items\"\"\"\n" +
               "        return sum(\n" +
               "            Decimal(item['price']) * item['quantity']\n" +
               "            for item in self.cart.values()\n" +
               "        )\n" +
               "    \n" +
               "    def get_total_items(self):\n" +
               "        \"\"\"Get total number of items\"\"\"\n" +
               "        return sum(item['quantity'] for item in self.cart.values())\n" +
               "    \n" +
               "    def get_subtotal(self):\n" +
               "        \"\"\"Get subtotal (before tax and shipping)\"\"\"\n" +
               "        return self.get_total_price()\n" +
               "    \n" +
               "    def get_tax(self, tax_rate=Decimal('0.10')):\n" +
               "        \"\"\"Calculate tax\"\"\"\n" +
               "        return self.get_subtotal() * tax_rate\n" +
               "    \n" +
               "    def get_shipping(self):\n" +
               "        \"\"\"Calculate shipping cost\"\"\"\n" +
               "        subtotal = self.get_subtotal()\n" +
               "        if subtotal >= Decimal('100.00'):\n" +
               "            return Decimal('0.00')  # Free shipping over $100\n" +
               "        elif subtotal >= Decimal('50.00'):\n" +
               "            return Decimal('5.00')\n" +
               "        else:\n" +
               "            return Decimal('10.00')\n" +
               "    \n" +
               "    def get_grand_total(self):\n" +
               "        \"\"\"Calculate grand total\"\"\"\n" +
               "        return self.get_subtotal() + self.get_tax() + self.get_shipping()\n" +
               "    \n" +
               "    def _get_cart_key(self, product, variant=None):\n" +
               "        \"\"\"Generate unique cart key\"\"\"\n" +
               "        if variant:\n" +
               "            return f'product_{product.id}_variant_{variant.id}'\n" +
               "        return f'product_{product.id}'\n" +
               "    \n" +
               "    def __iter__(self):\n" +
               "        \"\"\"Iterate over cart items\"\"\"\n" +
               "        for item in self.get_items():\n" +
               "            yield item\n" +
               "    \n" +
               "    def __len__(self):\n" +
               "        \"\"\"Count all items\"\"\"\n" +
               "        return self.get_total_items()\n" +
               "    \n" +
               "    def to_json(self):\n" +
               "        \"\"\"Convert cart to JSON\"\"\"\n" +
               "        return json.dumps({\n" +
               "            'items': [\n" +
               "                {\n" +
               "                    'product_id': item['product'].id,\n" +
               "                    'variant_id': item['variant'].id if item['variant'] else None,\n" +
               "                    'name': item['product'].name,\n" +
               "                    'variant_name': item['variant'].name if item['variant'] else None,\n" +
               "                    'price': str(item['price']),\n" +
               "                    'quantity': item['quantity'],\n" +
               "                    'total': str(item['total_price'])\n" +
               "                }\n" +
               "                for item in self.get_items()\n" +
               "            ],\n" +
               "            'subtotal': str(self.get_subtotal()),\n" +
               "            'tax': str(self.get_tax()),\n" +
               "            'shipping': str(self.get_shipping()),\n" +
               "            'total': str(self.get_grand_total()),\n" +
               "            'item_count': self.get_total_items()\n" +
               "        })\n";
    }

    private String generateCartViews() {
        return "from django.shortcuts import render, redirect, get_object_or_404\n" +
               "from django.views import View\n" +
               "from django.http import JsonResponse\n" +
               "from django.contrib import messages\n" +
               "from django.views.decorators.csrf import csrf_exempt\n" +
               "from django.utils.decorators import method_decorator\n" +
               "from .cart import Cart\n" +
               "from .models import Product, ProductVariant\n" +
               "import json\n\n" +
               "class CartDetailView(View):\n" +
               "    \"\"\"Display cart contents\"\"\"\n" +
               "    \n" +
               "    def get(self, request):\n" +
               "        cart = Cart(request)\n" +
               "        return render(request, 'cart/detail.html', {\n" +
               "            'cart': cart,\n" +
               "            'cart_items': cart.get_items()\n" +
               "        })\n\n" +
               "class AddToCartView(View):\n" +
               "    \"\"\"Add product to cart\"\"\"\n" +
               "    \n" +
               "    def post(self, request):\n" +
               "        cart = Cart(request)\n" +
               "        \n" +
               "        try:\n" +
               "            data = json.loads(request.body) if request.body else request.POST\n" +
               "            product_id = data.get('product_id')\n" +
               "            variant_id = data.get('variant_id')\n" +
               "            quantity = int(data.get('quantity', 1))\n" +
               "            \n" +
               "            product = get_object_or_404(Product, id=product_id, is_active=True)\n" +
               "            variant = None\n" +
               "            \n" +
               "            if variant_id:\n" +
               "                variant = get_object_or_404(\n" +
               "                    ProductVariant,\n" +
               "                    id=variant_id,\n" +
               "                    product=product,\n" +
               "                    is_active=True\n" +
               "                )\n" +
               "            \n" +
               "            # Check stock availability\n" +
               "            if product.track_inventory:\n" +
               "                available = variant.stock_quantity if variant else product.stock_quantity\n" +
               "                if available < quantity and not product.allow_backorder:\n" +
               "                    return JsonResponse({\n" +
               "                        'success': False,\n" +
               "                        'message': f'Only {available} items available'\n" +
               "                    }, status=400)\n" +
               "            \n" +
               "            cart.add(product, variant, quantity)\n" +
               "            \n" +
               "            if request.headers.get('X-Requested-With') == 'XMLHttpRequest':\n" +
               "                return JsonResponse({\n" +
               "                    'success': True,\n" +
               "                    'message': 'Product added to cart',\n" +
               "                    'cart_total_items': cart.get_total_items(),\n" +
               "                    'cart_total_price': str(cart.get_total_price())\n" +
               "                })\n" +
               "            \n" +
               "            messages.success(request, 'Product added to cart')\n" +
               "            return redirect('cart:detail')\n" +
               "        \n" +
               "        except Exception as e:\n" +
               "            if request.headers.get('X-Requested-With') == 'XMLHttpRequest':\n" +
               "                return JsonResponse({\n" +
               "                    'success': False,\n" +
               "                    'message': str(e)\n" +
               "                }, status=400)\n" +
               "            \n" +
               "            messages.error(request, 'Failed to add product to cart')\n" +
               "            return redirect('products:list')\n\n" +
               "class UpdateCartView(View):\n" +
               "    \"\"\"Update cart item quantity\"\"\"\n" +
               "    \n" +
               "    def post(self, request):\n" +
               "        cart = Cart(request)\n" +
               "        \n" +
               "        try:\n" +
               "            data = json.loads(request.body) if request.body else request.POST\n" +
               "            product_id = data.get('product_id')\n" +
               "            variant_id = data.get('variant_id')\n" +
               "            quantity = int(data.get('quantity', 1))\n" +
               "            \n" +
               "            product = get_object_or_404(Product, id=product_id)\n" +
               "            variant = None\n" +
               "            \n" +
               "            if variant_id:\n" +
               "                variant = get_object_or_404(ProductVariant, id=variant_id)\n" +
               "            \n" +
               "            if quantity <= 0:\n" +
               "                cart.remove(product, variant)\n" +
               "                message = 'Product removed from cart'\n" +
               "            else:\n" +
               "                cart.update_quantity(product, variant, quantity)\n" +
               "                message = 'Cart updated'\n" +
               "            \n" +
               "            if request.headers.get('X-Requested-With') == 'XMLHttpRequest':\n" +
               "                return JsonResponse({\n" +
               "                    'success': True,\n" +
               "                    'message': message,\n" +
               "                    'cart_total_items': cart.get_total_items(),\n" +
               "                    'cart_subtotal': str(cart.get_subtotal()),\n" +
               "                    'cart_total': str(cart.get_grand_total())\n" +
               "                })\n" +
               "            \n" +
               "            messages.success(request, message)\n" +
               "            return redirect('cart:detail')\n" +
               "        \n" +
               "        except Exception as e:\n" +
               "            if request.headers.get('X-Requested-With') == 'XMLHttpRequest':\n" +
               "                return JsonResponse({\n" +
               "                    'success': False,\n" +
               "                    'message': str(e)\n" +
               "                }, status=400)\n" +
               "            \n" +
               "            messages.error(request, 'Failed to update cart')\n" +
               "            return redirect('cart:detail')\n\n" +
               "class RemoveFromCartView(View):\n" +
               "    \"\"\"Remove item from cart\"\"\"\n" +
               "    \n" +
               "    def post(self, request):\n" +
               "        cart = Cart(request)\n" +
               "        \n" +
               "        try:\n" +
               "            data = json.loads(request.body) if request.body else request.POST\n" +
               "            product_id = data.get('product_id')\n" +
               "            variant_id = data.get('variant_id')\n" +
               "            \n" +
               "            product = get_object_or_404(Product, id=product_id)\n" +
               "            variant = None\n" +
               "            \n" +
               "            if variant_id:\n" +
               "                variant = get_object_or_404(ProductVariant, id=variant_id)\n" +
               "            \n" +
               "            cart.remove(product, variant)\n" +
               "            \n" +
               "            if request.headers.get('X-Requested-With') == 'XMLHttpRequest':\n" +
               "                return JsonResponse({\n" +
               "                    'success': True,\n" +
               "                    'message': 'Product removed from cart',\n" +
               "                    'cart_total_items': cart.get_total_items(),\n" +
               "                    'cart_total': str(cart.get_grand_total())\n" +
               "                })\n" +
               "            \n" +
               "            messages.success(request, 'Product removed from cart')\n" +
               "            return redirect('cart:detail')\n" +
               "        \n" +
               "        except Exception as e:\n" +
               "            if request.headers.get('X-Requested-With') == 'XMLHttpRequest':\n" +
               "                return JsonResponse({\n" +
               "                    'success': False,\n" +
               "                    'message': str(e)\n" +
               "                }, status=400)\n" +
               "            \n" +
               "            messages.error(request, 'Failed to remove product')\n" +
               "            return redirect('cart:detail')\n\n" +
               "class ClearCartView(View):\n" +
               "    \"\"\"Clear entire cart\"\"\"\n" +
               "    \n" +
               "    def post(self, request):\n" +
               "        cart = Cart(request)\n" +
               "        cart.clear()\n" +
               "        \n" +
               "        if request.headers.get('X-Requested-With') == 'XMLHttpRequest':\n" +
               "            return JsonResponse({\n" +
               "                'success': True,\n" +
               "                'message': 'Cart cleared'\n" +
               "            })\n" +
               "        \n" +
               "        messages.success(request, 'Cart cleared')\n" +
               "        return redirect('cart:detail')\n\n" +
               "class CartAPIView(View):\n" +
               "    \"\"\"API endpoint for cart operations\"\"\"\n" +
               "    \n" +
               "    def get(self, request):\n" +
               "        \"\"\"Get cart contents as JSON\"\"\"\n" +
               "        cart = Cart(request)\n" +
               "        return JsonResponse(json.loads(cart.to_json()))\n" +
               "    \n" +
               "    @method_decorator(csrf_exempt)\n" +
               "    def post(self, request):\n" +
               "        \"\"\"Handle cart operations via API\"\"\"\n" +
               "        try:\n" +
               "            data = json.loads(request.body)\n" +
               "            action = data.get('action')\n" +
               "            cart = Cart(request)\n" +
               "            \n" +
               "            if action == 'add':\n" +
               "                product = get_object_or_404(Product, id=data['product_id'])\n" +
               "                variant = None\n" +
               "                if data.get('variant_id'):\n" +
               "                    variant = get_object_or_404(ProductVariant, id=data['variant_id'])\n" +
               "                cart.add(product, variant, data.get('quantity', 1))\n" +
               "            \n" +
               "            elif action == 'update':\n" +
               "                product = get_object_or_404(Product, id=data['product_id'])\n" +
               "                variant = None\n" +
               "                if data.get('variant_id'):\n" +
               "                    variant = get_object_or_404(ProductVariant, id=data['variant_id'])\n" +
               "                cart.update_quantity(product, variant, data['quantity'])\n" +
               "            \n" +
               "            elif action == 'remove':\n" +
               "                product = get_object_or_404(Product, id=data['product_id'])\n" +
               "                variant = None\n" +
               "                if data.get('variant_id'):\n" +
               "                    variant = get_object_or_404(ProductVariant, id=data['variant_id'])\n" +
               "                cart.remove(product, variant)\n" +
               "            \n" +
               "            elif action == 'clear':\n" +
               "                cart.clear()\n" +
               "            \n" +
               "            else:\n" +
               "                return JsonResponse({\n" +
               "                    'success': False,\n" +
               "                    'message': 'Invalid action'\n" +
               "                }, status=400)\n" +
               "            \n" +
               "            return JsonResponse(json.loads(cart.to_json()))\n" +
               "        \n" +
               "        except Exception as e:\n" +
               "            return JsonResponse({\n" +
               "                'success': False,\n" +
               "                'message': str(e)\n" +
               "            }, status=400)\n";
    }

    private String generateOrderModels() {
        return "from django.db import models\n" +
               "from django.contrib.auth import get_user_model\n" +
               "from django.utils import timezone\n" +
               "from decimal import Decimal\n" +
               "import uuid\n\n" +
               "User = get_user_model()\n\n" +
               "class OrderStatus(models.TextChoices):\n" +
               "    PENDING = 'pending', 'Pending'\n" +
               "    PROCESSING = 'processing', 'Processing'\n" +
               "    PAID = 'paid', 'Paid'\n" +
               "    SHIPPED = 'shipped', 'Shipped'\n" +
               "    DELIVERED = 'delivered', 'Delivered'\n" +
               "    CANCELLED = 'cancelled', 'Cancelled'\n" +
               "    REFUNDED = 'refunded', 'Refunded'\n\n" +
               "class Order(models.Model):\n" +
               "    \"\"\"Order model\"\"\"\n" +
               "    uuid = models.UUIDField(default=uuid.uuid4, editable=False, unique=True)\n" +
               "    order_number = models.CharField(max_length=50, unique=True, editable=False)\n" +
               "    \n" +
               "    # Customer\n" +
               "    user = models.ForeignKey(\n" +
               "        User,\n" +
               "        on_delete=models.SET_NULL,\n" +
               "        null=True,\n" +
               "        blank=True,\n" +
               "        related_name='orders'\n" +
               "    )\n" +
               "    email = models.EmailField()\n" +
               "    phone = models.CharField(max_length=20)\n" +
               "    \n" +
               "    # Billing address\n" +
               "    billing_first_name = models.CharField(max_length=50)\n" +
               "    billing_last_name = models.CharField(max_length=50)\n" +
               "    billing_address = models.CharField(max_length=250)\n" +
               "    billing_address2 = models.CharField(max_length=250, blank=True)\n" +
               "    billing_city = models.CharField(max_length=100)\n" +
               "    billing_state = models.CharField(max_length=100)\n" +
               "    billing_postal_code = models.CharField(max_length=20)\n" +
               "    billing_country = models.CharField(max_length=100)\n" +
               "    \n" +
               "    # Shipping address\n" +
               "    shipping_first_name = models.CharField(max_length=50)\n" +
               "    shipping_last_name = models.CharField(max_length=50)\n" +
               "    shipping_address = models.CharField(max_length=250)\n" +
               "    shipping_address2 = models.CharField(max_length=250, blank=True)\n" +
               "    shipping_city = models.CharField(max_length=100)\n" +
               "    shipping_state = models.CharField(max_length=100)\n" +
               "    shipping_postal_code = models.CharField(max_length=20)\n" +
               "    shipping_country = models.CharField(max_length=100)\n" +
               "    \n" +
               "    # Order details\n" +
               "    status = models.CharField(\n" +
               "        max_length=20,\n" +
               "        choices=OrderStatus.choices,\n" +
               "        default=OrderStatus.PENDING\n" +
               "    )\n" +
               "    \n" +
               "    # Pricing\n" +
               "    subtotal = models.DecimalField(max_digits=10, decimal_places=2)\n" +
               "    tax_amount = models.DecimalField(max_digits=10, decimal_places=2, default=Decimal('0.00'))\n" +
               "    shipping_amount = models.DecimalField(max_digits=10, decimal_places=2, default=Decimal('0.00'))\n" +
               "    discount_amount = models.DecimalField(max_digits=10, decimal_places=2, default=Decimal('0.00'))\n" +
               "    total_amount = models.DecimalField(max_digits=10, decimal_places=2)\n" +
               "    \n" +
               "    # Payment\n" +
               "    payment_method = models.CharField(max_length=50, blank=True)\n" +
               "    payment_id = models.CharField(max_length=100, blank=True)\n" +
               "    paid = models.BooleanField(default=False)\n" +
               "    paid_at = models.DateTimeField(null=True, blank=True)\n" +
               "    \n" +
               "    # Shipping\n" +
               "    shipping_method = models.CharField(max_length=100, blank=True)\n" +
               "    tracking_number = models.CharField(max_length=100, blank=True)\n" +
               "    shipped_at = models.DateTimeField(null=True, blank=True)\n" +
               "    delivered_at = models.DateTimeField(null=True, blank=True)\n" +
               "    \n" +
               "    # Notes\n" +
               "    customer_notes = models.TextField(blank=True)\n" +
               "    admin_notes = models.TextField(blank=True)\n" +
               "    \n" +
               "    # Timestamps\n" +
               "    created_at = models.DateTimeField(auto_now_add=True)\n" +
               "    updated_at = models.DateTimeField(auto_now=True)\n\n" +
               "    class Meta:\n" +
               "        ordering = ['-created_at']\n" +
               "        indexes = [\n" +
               "            models.Index(fields=['order_number']),\n" +
               "            models.Index(fields=['user', 'created_at']),\n" +
               "            models.Index(fields=['status', 'created_at']),\n" +
               "        ]\n\n" +
               "    def save(self, *args, **kwargs):\n" +
               "        if not self.order_number:\n" +
               "            # Generate unique order number\n" +
               "            prefix = timezone.now().strftime('%Y%m%d')\n" +
               "            last_order = Order.objects.filter(\n" +
               "                order_number__startswith=prefix\n" +
               "            ).order_by('-order_number').first()\n" +
               "            \n" +
               "            if last_order:\n" +
               "                last_number = int(last_order.order_number[-4:])\n" +
               "                new_number = last_number + 1\n" +
               "            else:\n" +
               "                new_number = 1\n" +
               "            \n" +
               "            self.order_number = f'{prefix}{new_number:04d}'\n" +
               "        \n" +
               "        super().save(*args, **kwargs)\n\n" +
               "    def __str__(self):\n" +
               "        return f'Order {self.order_number}'\n\n" +
               "    @property\n" +
               "    def full_name(self):\n" +
               "        return f'{self.billing_first_name} {self.billing_last_name}'\n\n" +
               "    @property\n" +
               "    def full_billing_address(self):\n" +
               "        parts = [\n" +
               "            self.billing_address,\n" +
               "            self.billing_address2,\n" +
               "            f'{self.billing_city}, {self.billing_state} {self.billing_postal_code}',\n" +
               "            self.billing_country\n" +
               "        ]\n" +
               "        return '\\n'.join(filter(None, parts))\n\n" +
               "    @property\n" +
               "    def full_shipping_address(self):\n" +
               "        parts = [\n" +
               "            self.shipping_address,\n" +
               "            self.shipping_address2,\n" +
               "            f'{self.shipping_city}, {self.shipping_state} {self.shipping_postal_code}',\n" +
               "            self.shipping_country\n" +
               "        ]\n" +
               "        return '\\n'.join(filter(None, parts))\n\n" +
               "class OrderItem(models.Model):\n" +
               "    \"\"\"Order item model\"\"\"\n" +
               "    order = models.ForeignKey(Order, on_delete=models.CASCADE, related_name='items')\n" +
               "    product = models.ForeignKey(\n" +
               "        'Product',\n" +
               "        on_delete=models.SET_NULL,\n" +
               "        null=True\n" +
               "    )\n" +
               "    variant = models.ForeignKey(\n" +
               "        'ProductVariant',\n" +
               "        on_delete=models.SET_NULL,\n" +
               "        null=True,\n" +
               "        blank=True\n" +
               "    )\n" +
               "    \n" +
               "    # Product snapshot\n" +
               "    product_name = models.CharField(max_length=200)\n" +
               "    product_sku = models.CharField(max_length=50)\n" +
               "    variant_name = models.CharField(max_length=100, blank=True)\n" +
               "    \n" +
               "    # Pricing\n" +
               "    price = models.DecimalField(max_digits=10, decimal_places=2)\n" +
               "    quantity = models.PositiveIntegerField(default=1)\n" +
               "    total = models.DecimalField(max_digits=10, decimal_places=2)\n" +
               "    \n" +
               "    created_at = models.DateTimeField(auto_now_add=True)\n\n" +
               "    class Meta:\n" +
               "        ordering = ['created_at']\n\n" +
               "    def save(self, *args, **kwargs):\n" +
               "        self.total = self.price * self.quantity\n" +
               "        super().save(*args, **kwargs)\n\n" +
               "    def __str__(self):\n" +
               "        return f'{self.product_name} x {self.quantity}'\n\n" +
               "class ShippingRate(models.Model):\n" +
               "    \"\"\"Shipping rates\"\"\"\n" +
               "    name = models.CharField(max_length=100)\n" +
               "    description = models.TextField(blank=True)\n" +
               "    base_rate = models.DecimalField(max_digits=10, decimal_places=2)\n" +
               "    per_item_rate = models.DecimalField(\n" +
               "        max_digits=10,\n" +
               "        decimal_places=2,\n" +
               "        default=Decimal('0.00')\n" +
               "    )\n" +
               "    min_order = models.DecimalField(\n" +
               "        max_digits=10,\n" +
               "        decimal_places=2,\n" +
               "        null=True,\n" +
               "        blank=True\n" +
               "    )\n" +
               "    max_order = models.DecimalField(\n" +
               "        max_digits=10,\n" +
               "        decimal_places=2,\n" +
               "        null=True,\n" +
               "        blank=True\n" +
               "    )\n" +
               "    free_shipping_threshold = models.DecimalField(\n" +
               "        max_digits=10,\n" +
               "        decimal_places=2,\n" +
               "        null=True,\n" +
               "        blank=True\n" +
               "    )\n" +
               "    estimated_days = models.CharField(max_length=50)\n" +
               "    is_active = models.BooleanField(default=True)\n" +
               "    order = models.IntegerField(default=0)\n" +
               "    created_at = models.DateTimeField(auto_now_add=True)\n" +
               "    updated_at = models.DateTimeField(auto_now=True)\n\n" +
               "    class Meta:\n" +
               "        ordering = ['order', 'name']\n\n" +
               "    def __str__(self):\n" +
               "        return self.name\n\n" +
               "    def calculate_rate(self, subtotal, item_count=1):\n" +
               "        \"\"\"Calculate shipping rate for order\"\"\"\n" +
               "        if self.free_shipping_threshold and subtotal >= self.free_shipping_threshold:\n" +
               "            return Decimal('0.00')\n" +
               "        \n" +
               "        rate = self.base_rate + (self.per_item_rate * item_count)\n" +
               "        return rate\n";
    }

    private String generateOrderViews() {
        return "from django.shortcuts import render, redirect, get_object_or_404\n" +
               "from django.views import View\n" +
               "from django.contrib.auth.decorators import login_required\n" +
               "from django.utils.decorators import method_decorator\n" +
               "from django.contrib import messages\n" +
               "from django.db import transaction\n" +
               "from django.http import JsonResponse, HttpResponse\n" +
               "from django.core.mail import send_mail\n" +
               "from django.template.loader import render_to_string\n" +
               "from django.conf import settings\n" +
               "from .models import Order, OrderItem, ShippingRate\n" +
               "from .cart import Cart\n" +
               "from decimal import Decimal\n" +
               "import json\n\n" +
               "@method_decorator(login_required, name='dispatch')\n" +
               "class CheckoutView(View):\n" +
               "    \"\"\"Checkout process\"\"\"\n" +
               "    \n" +
               "    def get(self, request):\n" +
               "        cart = Cart(request)\n" +
               "        \n" +
               "        if not cart.get_items():\n" +
               "            messages.warning(request, 'Your cart is empty')\n" +
               "            return redirect('cart:detail')\n" +
               "        \n" +
               "        shipping_rates = ShippingRate.objects.filter(is_active=True)\n" +
               "        \n" +
               "        # Calculate shipping for each rate\n" +
               "        shipping_options = []\n" +
               "        for rate in shipping_rates:\n" +
               "            cost = rate.calculate_rate(\n" +
               "                cart.get_subtotal(),\n" +
               "                cart.get_total_items()\n" +
               "            )\n" +
               "            shipping_options.append({\n" +
               "                'id': rate.id,\n" +
               "                'name': rate.name,\n" +
               "                'description': rate.description,\n" +
               "                'cost': cost,\n" +
               "                'estimated_days': rate.estimated_days\n" +
               "            })\n" +
               "        \n" +
               "        context = {\n" +
               "            'cart': cart,\n" +
               "            'shipping_options': shipping_options,\n" +
               "            'user': request.user\n" +
               "        }\n" +
               "        \n" +
               "        return render(request, 'orders/checkout.html', context)\n" +
               "    \n" +
               "    def post(self, request):\n" +
               "        cart = Cart(request)\n" +
               "        \n" +
               "        if not cart.get_items():\n" +
               "            return JsonResponse({\n" +
               "                'success': False,\n" +
               "                'message': 'Cart is empty'\n" +
               "            }, status=400)\n" +
               "        \n" +
               "        try:\n" +
               "            with transaction.atomic():\n" +
               "                # Create order\n" +
               "                order = Order.objects.create(\n" +
               "                    user=request.user,\n" +
               "                    email=request.POST.get('email', request.user.email),\n" +
               "                    phone=request.POST.get('phone'),\n" +
               "                    \n" +
               "                    # Billing address\n" +
               "                    billing_first_name=request.POST.get('billing_first_name'),\n" +
               "                    billing_last_name=request.POST.get('billing_last_name'),\n" +
               "                    billing_address=request.POST.get('billing_address'),\n" +
               "                    billing_address2=request.POST.get('billing_address2', ''),\n" +
               "                    billing_city=request.POST.get('billing_city'),\n" +
               "                    billing_state=request.POST.get('billing_state'),\n" +
               "                    billing_postal_code=request.POST.get('billing_postal_code'),\n" +
               "                    billing_country=request.POST.get('billing_country'),\n" +
               "                    \n" +
               "                    # Shipping address\n" +
               "                    shipping_first_name=request.POST.get('shipping_first_name'),\n" +
               "                    shipping_last_name=request.POST.get('shipping_last_name'),\n" +
               "                    shipping_address=request.POST.get('shipping_address'),\n" +
               "                    shipping_address2=request.POST.get('shipping_address2', ''),\n" +
               "                    shipping_city=request.POST.get('shipping_city'),\n" +
               "                    shipping_state=request.POST.get('shipping_state'),\n" +
               "                    shipping_postal_code=request.POST.get('shipping_postal_code'),\n" +
               "                    shipping_country=request.POST.get('shipping_country'),\n" +
               "                    \n" +
               "                    # Pricing\n" +
               "                    subtotal=cart.get_subtotal(),\n" +
               "                    tax_amount=cart.get_tax(),\n" +
               "                    shipping_amount=Decimal(request.POST.get('shipping_amount', '0')),\n" +
               "                    total_amount=cart.get_grand_total(),\n" +
               "                    \n" +
               "                    # Other\n" +
               "                    shipping_method=request.POST.get('shipping_method', ''),\n" +
               "                    customer_notes=request.POST.get('customer_notes', '')\n" +
               "                )\n" +
               "                \n" +
               "                # Create order items\n" +
               "                for item in cart.get_items():\n" +
               "                    OrderItem.objects.create(\n" +
               "                        order=order,\n" +
               "                        product=item['product'],\n" +
               "                        variant=item['variant'],\n" +
               "                        product_name=item['product'].name,\n" +
               "                        product_sku=item['product'].sku,\n" +
               "                        variant_name=item['variant'].name if item['variant'] else '',\n" +
               "                        price=item['price'],\n" +
               "                        quantity=item['quantity']\n" +
               "                    )\n" +
               "                    \n" +
               "                    # Update product stock\n" +
               "                    if item['product'].track_inventory:\n" +
               "                        if item['variant']:\n" +
               "                            item['variant'].stock_quantity -= item['quantity']\n" +
               "                            item['variant'].save()\n" +
               "                        else:\n" +
               "                            item['product'].stock_quantity -= item['quantity']\n" +
               "                            item['product'].save()\n" +
               "                \n" +
               "                # Clear cart\n" +
               "                cart.clear()\n" +
               "                \n" +
               "                # Send confirmation email\n" +
               "                self.send_order_confirmation(order)\n" +
               "                \n" +
               "                # Store order ID in session for payment\n" +
               "                request.session['order_id'] = order.id\n" +
               "                \n" +
               "                return redirect('orders:payment', order_number=order.order_number)\n" +
               "        \n" +
               "        except Exception as e:\n" +
               "            messages.error(request, f'Error creating order: {e}')\n" +
               "            return redirect('orders:checkout')\n" +
               "    \n" +
               "    def send_order_confirmation(self, order):\n" +
               "        \"\"\"Send order confirmation email\"\"\"\n" +
               "        subject = f'Order Confirmation - {order.order_number}'\n" +
               "        html_message = render_to_string('orders/email/confirmation.html', {\n" +
               "            'order': order\n" +
               "        })\n" +
               "        \n" +
               "        send_mail(\n" +
               "            subject,\n" +
               "            '',\n" +
               "            settings.DEFAULT_FROM_EMAIL,\n" +
               "            [order.email],\n" +
               "            html_message=html_message\n" +
               "        )\n\n" +
               "@method_decorator(login_required, name='dispatch')\n" +
               "class OrderDetailView(View):\n" +
               "    \"\"\"Order detail view\"\"\"\n" +
               "    \n" +
               "    def get(self, request, order_number):\n" +
               "        order = get_object_or_404(\n" +
               "            Order,\n" +
               "            order_number=order_number,\n" +
               "            user=request.user\n" +
               "        )\n" +
               "        \n" +
               "        return render(request, 'orders/detail.html', {\n" +
               "            'order': order\n" +
               "        })\n\n" +
               "@method_decorator(login_required, name='dispatch')\n" +
               "class OrderListView(View):\n" +
               "    \"\"\"User order history\"\"\"\n" +
               "    \n" +
               "    def get(self, request):\n" +
               "        orders = Order.objects.filter(user=request.user)\n" +
               "        \n" +
               "        # Filter by status\n" +
               "        status = request.GET.get('status')\n" +
               "        if status:\n" +
               "            orders = orders.filter(status=status)\n" +
               "        \n" +
               "        return render(request, 'orders/list.html', {\n" +
               "            'orders': orders,\n" +
               "            'current_status': status\n" +
               "        })\n\n" +
               "class OrderInvoiceView(View):\n" +
               "    \"\"\"Generate order invoice\"\"\"\n" +
               "    \n" +
               "    @method_decorator(login_required)\n" +
               "    def get(self, request, order_number):\n" +
               "        order = get_object_or_404(\n" +
               "            Order,\n" +
               "            order_number=order_number,\n" +
               "            user=request.user\n" +
               "        )\n" +
               "        \n" +
               "        html = render_to_string('orders/invoice.html', {\n" +
               "            'order': order\n" +
               "        })\n" +
               "        \n" +
               "        response = HttpResponse(html)\n" +
               "        response['Content-Type'] = 'text/html'\n" +
               "        \n" +
               "        # Optional: Generate PDF\n" +
               "        if request.GET.get('format') == 'pdf':\n" +
               "            import pdfkit\n" +
               "            pdf = pdfkit.from_string(html, False)\n" +
               "            response = HttpResponse(pdf, content_type='application/pdf')\n" +
               "            response['Content-Disposition'] = f'attachment; filename=\"invoice_{order_number}.pdf\"'\n" +
               "        \n" +
               "        return response\n\n" +
               "class TrackOrderView(View):\n" +
               "    \"\"\"Track order status\"\"\"\n" +
               "    \n" +
               "    def get(self, request):\n" +
               "        return render(request, 'orders/track.html')\n" +
               "    \n" +
               "    def post(self, request):\n" +
               "        order_number = request.POST.get('order_number')\n" +
               "        email = request.POST.get('email')\n" +
               "        \n" +
               "        try:\n" +
               "            order = Order.objects.get(\n" +
               "                order_number=order_number,\n" +
               "                email=email\n" +
               "            )\n" +
               "            \n" +
               "            return render(request, 'orders/track_result.html', {\n" +
               "                'order': order\n" +
               "            })\n" +
               "        \n" +
               "        except Order.DoesNotExist:\n" +
               "            messages.error(request, 'Order not found')\n" +
               "            return redirect('orders:track')\n";
    }

    private String generatePaymentProcessors() {
        return "import stripe\n" +
               "import paypalrestsdk\n" +
               "from django.conf import settings\n" +
               "from django.utils import timezone\n" +
               "from decimal import Decimal\n" +
               "import logging\n" +
               "import hashlib\n" +
               "import hmac\n\n" +
               "logger = logging.getLogger(__name__)\n\n" +
               "class PaymentProcessor:\n" +
               "    \"\"\"Base payment processor class\"\"\"\n" +
               "    \n" +
               "    def process_payment(self, order, payment_data):\n" +
               "        raise NotImplementedError\n" +
               "    \n" +
               "    def refund_payment(self, order, amount=None):\n" +
               "        raise NotImplementedError\n" +
               "    \n" +
               "    def validate_webhook(self, request):\n" +
               "        raise NotImplementedError\n\n" +
               "class StripeProcessor(PaymentProcessor):\n" +
               "    \"\"\"Stripe payment processor\"\"\"\n" +
               "    \n" +
               "    def __init__(self):\n" +
               "        stripe.api_key = settings.STRIPE_SECRET_KEY\n" +
               "        self.webhook_secret = settings.STRIPE_WEBHOOK_SECRET\n" +
               "    \n" +
               "    def create_payment_intent(self, order):\n" +
               "        \"\"\"Create Stripe payment intent\"\"\"\n" +
               "        try:\n" +
               "            intent = stripe.PaymentIntent.create(\n" +
               "                amount=int(order.total_amount * 100),  # Convert to cents\n" +
               "                currency='usd',\n" +
               "                metadata={\n" +
               "                    'order_id': order.id,\n" +
               "                    'order_number': order.order_number\n" +
               "                },\n" +
               "                description=f'Order {order.order_number}'\n" +
               "            )\n" +
               "            return {\n" +
               "                'success': True,\n" +
               "                'client_secret': intent.client_secret,\n" +
               "                'payment_intent_id': intent.id\n" +
               "            }\n" +
               "        except stripe.error.StripeError as e:\n" +
               "            logger.error(f'Stripe error: {e}')\n" +
               "            return {\n" +
               "                'success': False,\n" +
               "                'error': str(e)\n" +
               "            }\n" +
               "    \n" +
               "    def process_payment(self, order, payment_data):\n" +
               "        \"\"\"Process payment with Stripe\"\"\"\n" +
               "        try:\n" +
               "            # Create charge\n" +
               "            charge = stripe.Charge.create(\n" +
               "                amount=int(order.total_amount * 100),\n" +
               "                currency='usd',\n" +
               "                source=payment_data['token'],\n" +
               "                description=f'Order {order.order_number}',\n" +
               "                metadata={\n" +
               "                    'order_id': order.id,\n" +
               "                    'order_number': order.order_number\n" +
               "                }\n" +
               "            )\n" +
               "            \n" +
               "            # Update order\n" +
               "            order.payment_id = charge.id\n" +
               "            order.payment_method = 'stripe'\n" +
               "            order.paid = True\n" +
               "            order.paid_at = timezone.now()\n" +
               "            order.status = 'processing'\n" +
               "            order.save()\n" +
               "            \n" +
               "            return {\n" +
               "                'success': True,\n" +
               "                'transaction_id': charge.id\n" +
               "            }\n" +
               "        \n" +
               "        except stripe.error.CardError as e:\n" +
               "            return {\n" +
               "                'success': False,\n" +
               "                'error': 'Card declined',\n" +
               "                'details': str(e)\n" +
               "            }\n" +
               "        \n" +
               "        except Exception as e:\n" +
               "            logger.error(f'Payment processing error: {e}')\n" +
               "            return {\n" +
               "                'success': False,\n" +
               "                'error': 'Payment processing failed'\n" +
               "            }\n" +
               "    \n" +
               "    def refund_payment(self, order, amount=None):\n" +
               "        \"\"\"Refund Stripe payment\"\"\"\n" +
               "        try:\n" +
               "            refund_amount = amount or order.total_amount\n" +
               "            \n" +
               "            refund = stripe.Refund.create(\n" +
               "                charge=order.payment_id,\n" +
               "                amount=int(refund_amount * 100) if amount else None\n" +
               "            )\n" +
               "            \n" +
               "            order.status = 'refunded'\n" +
               "            order.save()\n" +
               "            \n" +
               "            return {\n" +
               "                'success': True,\n" +
               "                'refund_id': refund.id\n" +
               "            }\n" +
               "        \n" +
               "        except Exception as e:\n" +
               "            logger.error(f'Refund error: {e}')\n" +
               "            return {\n" +
               "                'success': False,\n" +
               "                'error': str(e)\n" +
               "            }\n" +
               "    \n" +
               "    def validate_webhook(self, request):\n" +
               "        \"\"\"Validate Stripe webhook\"\"\"\n" +
               "        payload = request.body\n" +
               "        sig_header = request.META.get('HTTP_STRIPE_SIGNATURE')\n" +
               "        \n" +
               "        try:\n" +
               "            event = stripe.Webhook.construct_event(\n" +
               "                payload, sig_header, self.webhook_secret\n" +
               "            )\n" +
               "            return event\n" +
               "        except Exception as e:\n" +
               "            logger.error(f'Webhook validation error: {e}')\n" +
               "            return None\n\n" +
               "class PayPalProcessor(PaymentProcessor):\n" +
               "    \"\"\"PayPal payment processor\"\"\"\n" +
               "    \n" +
               "    def __init__(self):\n" +
               "        paypalrestsdk.configure({\n" +
               "            'mode': settings.PAYPAL_MODE,\n" +
               "            'client_id': settings.PAYPAL_CLIENT_ID,\n" +
               "            'client_secret': settings.PAYPAL_CLIENT_SECRET\n" +
               "        })\n" +
               "    \n" +
               "    def create_payment(self, order, return_url, cancel_url):\n" +
               "        \"\"\"Create PayPal payment\"\"\"\n" +
               "        payment = paypalrestsdk.Payment({\n" +
               "            'intent': 'sale',\n" +
               "            'payer': {\n" +
               "                'payment_method': 'paypal'\n" +
               "            },\n" +
               "            'redirect_urls': {\n" +
               "                'return_url': return_url,\n" +
               "                'cancel_url': cancel_url\n" +
               "            },\n" +
               "            'transactions': [{\n" +
               "                'amount': {\n" +
               "                    'total': str(order.total_amount),\n" +
               "                    'currency': 'USD',\n" +
               "                    'details': {\n" +
               "                        'subtotal': str(order.subtotal),\n" +
               "                        'tax': str(order.tax_amount),\n" +
               "                        'shipping': str(order.shipping_amount)\n" +
               "                    }\n" +
               "                },\n" +
               "                'description': f'Order {order.order_number}',\n" +
               "                'invoice_number': order.order_number,\n" +
               "                'item_list': {\n" +
               "                    'items': [\n" +
               "                        {\n" +
               "                            'name': item.product_name,\n" +
               "                            'sku': item.product_sku,\n" +
               "                            'price': str(item.price),\n" +
               "                            'currency': 'USD',\n" +
               "                            'quantity': item.quantity\n" +
               "                        }\n" +
               "                        for item in order.items.all()\n" +
               "                    ]\n" +
               "                }\n" +
               "            }]\n" +
               "        })\n" +
               "        \n" +
               "        if payment.create():\n" +
               "            # Find approval URL\n" +
               "            approval_url = None\n" +
               "            for link in payment.links:\n" +
               "                if link.rel == 'approval_url':\n" +
               "                    approval_url = link.href\n" +
               "                    break\n" +
               "            \n" +
               "            return {\n" +
               "                'success': True,\n" +
               "                'payment_id': payment.id,\n" +
               "                'approval_url': approval_url\n" +
               "            }\n" +
               "        else:\n" +
               "            return {\n" +
               "                'success': False,\n" +
               "                'error': payment.error\n" +
               "            }\n" +
               "    \n" +
               "    def execute_payment(self, payment_id, payer_id):\n" +
               "        \"\"\"Execute PayPal payment\"\"\"\n" +
               "        payment = paypalrestsdk.Payment.find(payment_id)\n" +
               "        \n" +
               "        if payment.execute({'payer_id': payer_id}):\n" +
               "            return {\n" +
               "                'success': True,\n" +
               "                'transaction_id': payment.id\n" +
               "            }\n" +
               "        else:\n" +
               "            return {\n" +
               "                'success': False,\n" +
               "                'error': payment.error\n" +
               "            }\n" +
               "    \n" +
               "    def process_payment(self, order, payment_data):\n" +
               "        \"\"\"Process PayPal payment\"\"\"\n" +
               "        result = self.execute_payment(\n" +
               "            payment_data['payment_id'],\n" +
               "            payment_data['payer_id']\n" +
               "        )\n" +
               "        \n" +
               "        if result['success']:\n" +
               "            order.payment_id = result['transaction_id']\n" +
               "            order.payment_method = 'paypal'\n" +
               "            order.paid = True\n" +
               "            order.paid_at = timezone.now()\n" +
               "            order.status = 'processing'\n" +
               "            order.save()\n" +
               "        \n" +
               "        return result\n" +
               "    \n" +
               "    def refund_payment(self, order, amount=None):\n" +
               "        \"\"\"Refund PayPal payment\"\"\"\n" +
               "        sale = paypalrestsdk.Sale.find(order.payment_id)\n" +
               "        \n" +
               "        refund_data = {}\n" +
               "        if amount:\n" +
               "            refund_data['amount'] = {\n" +
               "                'total': str(amount),\n" +
               "                'currency': 'USD'\n" +
               "            }\n" +
               "        \n" +
               "        refund = sale.refund(refund_data)\n" +
               "        \n" +
               "        if refund.success():\n" +
               "            order.status = 'refunded'\n" +
               "            order.save()\n" +
               "            return {\n" +
               "                'success': True,\n" +
               "                'refund_id': refund.id\n" +
               "            }\n" +
               "        else:\n" +
               "            return {\n" +
               "                'success': False,\n" +
               "                'error': refund.error\n" +
               "            }\n\n" +
               "class PaymentManager:\n" +
               "    \"\"\"Manage different payment processors\"\"\"\n" +
               "    \n" +
               "    def __init__(self):\n" +
               "        self.processors = {\n" +
               "            'stripe': StripeProcessor(),\n" +
               "            'paypal': PayPalProcessor()\n" +
               "        }\n" +
               "    \n" +
               "    def get_processor(self, method):\n" +
               "        return self.processors.get(method)\n" +
               "    \n" +
               "    def process_payment(self, order, method, payment_data):\n" +
               "        processor = self.get_processor(method)\n" +
               "        if processor:\n" +
               "            return processor.process_payment(order, payment_data)\n" +
               "        return {\n" +
               "            'success': False,\n" +
               "            'error': 'Invalid payment method'\n" +
               "        }\n";
    private String generatePaymentViews() {
        return "from django.shortcuts import render, redirect, get_object_or_404\n" +
               "from django.views import View\n" +
               "from django.http import JsonResponse\n" +
               "from django.contrib.auth.decorators import login_required\n" +
               "from django.utils.decorators import method_decorator\n" +
               "from django.views.decorators.csrf import csrf_exempt\n" +
               "from django.contrib import messages\n" +
               "from .models import Order\n" +
               "from .payment_processors import PaymentManager\n" +
               "import json\n\n" +
               "@method_decorator(login_required, name='dispatch')\n" +
               "class PaymentView(View):\n" +
               "    def get(self, request, order_number):\n" +
               "        order = get_object_or_404(Order, order_number=order_number, user=request.user)\n" +
               "        return render(request, 'payments/payment.html', {'order': order})\n" +
               "    \n" +
               "    def post(self, request, order_number):\n" +
               "        order = get_object_or_404(Order, order_number=order_number, user=request.user)\n" +
               "        payment_method = request.POST.get('payment_method')\n" +
               "        payment_data = json.loads(request.POST.get('payment_data', '{}'))\n" +
               "        \n" +
               "        manager = PaymentManager()\n" +
               "        result = manager.process_payment(order, payment_method, payment_data)\n" +
               "        \n" +
               "        if result['success']:\n" +
               "            messages.success(request, 'Payment successful')\n" +
               "            return redirect('orders:success', order_number=order.order_number)\n" +
               "        else:\n" +
               "            messages.error(request, result.get('error', 'Payment failed'))\n" +
               "            return redirect('orders:payment', order_number=order.order_number)\n";
    }

    private String generateDashboardViews() {
        return "from django.shortcuts import render\n" +
               "from django.views import View\n" +
               "from django.contrib.auth.decorators import login_required\n" +
               "from django.utils.decorators import method_decorator\n" +
               "from django.db.models import Count, Sum, Avg, Q\n" +
               "from django.utils import timezone\n" +
               "from datetime import timedelta\n" +
               "import json\n\n" +
               "@method_decorator(login_required, name='dispatch')\n" +
               "class DashboardView(View):\n" +
               "    def get(self, request):\n" +
               "        context = {\n" +
               "            'stats': self.get_stats(),\n" +
               "            'recent_orders': self.get_recent_orders(),\n" +
               "            'top_products': self.get_top_products(),\n" +
               "            'revenue_chart': self.get_revenue_chart_data(),\n" +
               "            'user_activity': self.get_user_activity()\n" +
               "        }\n" +
               "        return render(request, 'dashboard/index.html', context)\n" +
               "    \n" +
               "    def get_stats(self):\n" +
               "        from .models import Order, Product, CustomUser\n" +
               "        today = timezone.now().date()\n" +
               "        return {\n" +
               "            'total_orders': Order.objects.count(),\n" +
               "            'today_orders': Order.objects.filter(created_at__date=today).count(),\n" +
               "            'total_revenue': Order.objects.filter(paid=True).aggregate(Sum('total_amount'))['total_amount__sum'] or 0,\n" +
               "            'total_users': CustomUser.objects.count(),\n" +
               "            'total_products': Product.objects.filter(is_active=True).count()\n" +
               "        }\n" +
               "    \n" +
               "    def get_recent_orders(self):\n" +
               "        from .models import Order\n" +
               "        return Order.objects.select_related('user').prefetch_related('items')[:10]\n" +
               "    \n" +
               "    def get_top_products(self):\n" +
               "        from .models import Product\n" +
               "        return Product.objects.filter(is_active=True).order_by('-sold_count')[:5]\n" +
               "    \n" +
               "    def get_revenue_chart_data(self):\n" +
               "        from .models import Order\n" +
               "        end_date = timezone.now().date()\n" +
               "        start_date = end_date - timedelta(days=30)\n" +
               "        \n" +
               "        data = []\n" +
               "        current = start_date\n" +
               "        while current <= end_date:\n" +
               "            revenue = Order.objects.filter(\n" +
               "                created_at__date=current,\n" +
               "                paid=True\n" +
               "            ).aggregate(Sum('total_amount'))['total_amount__sum'] or 0\n" +
               "            data.append({'date': current.isoformat(), 'revenue': float(revenue)})\n" +
               "            current += timedelta(days=1)\n" +
               "        \n" +
               "        return json.dumps(data)\n" +
               "    \n" +
               "    def get_user_activity(self):\n" +
               "        from .models import CustomUser\n" +
               "        now = timezone.now()\n" +
               "        return {\n" +
               "            'active_today': CustomUser.objects.filter(last_seen__date=now.date()).count(),\n" +
               "            'active_week': CustomUser.objects.filter(last_seen__gte=now-timedelta(days=7)).count(),\n" +
               "            'new_users': CustomUser.objects.filter(date_joined__gte=now-timedelta(days=30)).count()\n" +
               "        }\n";
    }

    private String generateDashboardWidgets() {
        return "from django.template.loader import render_to_string\n" +
               "from django.utils.safestring import mark_safe\n" +
               "from django.db.models import Count, Sum, Avg\n" +
               "import json\n\n" +
               "class DashboardWidget:\n" +
               "    def __init__(self, title, template, context=None):\n" +
               "        self.title = title\n" +
               "        self.template = template\n" +
               "        self.context = context or {}\n" +
               "    \n" +
               "    def render(self):\n" +
               "        return mark_safe(render_to_string(self.template, self.context))\n\n" +
               "class StatsWidget(DashboardWidget):\n" +
               "    def __init__(self, model, field=None, aggregate='count'):\n" +
               "        self.model = model\n" +
               "        self.field = field\n" +
               "        self.aggregate = aggregate\n" +
               "        super().__init__('Stats', 'widgets/stats.html')\n" +
               "    \n" +
               "    def get_value(self):\n" +
               "        if self.aggregate == 'count':\n" +
               "            return self.model.objects.count()\n" +
               "        elif self.aggregate == 'sum' and self.field:\n" +
               "            return self.model.objects.aggregate(Sum(self.field))[f'{self.field}__sum']\n" +
               "        elif self.aggregate == 'avg' and self.field:\n" +
               "            return self.model.objects.aggregate(Avg(self.field))[f'{self.field}__avg']\n" +
               "        return 0\n\n" +
               "class ChartWidget(DashboardWidget):\n" +
               "    def __init__(self, title, chart_type='line', data=None):\n" +
               "        self.chart_type = chart_type\n" +
               "        self.data = data or []\n" +
               "        super().__init__(title, 'widgets/chart.html', {\n" +
               "            'chart_type': chart_type,\n" +
               "            'chart_data': json.dumps(data)\n" +
               "        })\n\n" +
               "class RecentItemsWidget(DashboardWidget):\n" +
               "    def __init__(self, title, model, limit=5):\n" +
               "        items = model.objects.all()[:limit]\n" +
               "        super().__init__(title, 'widgets/recent_items.html', {'items': items})\n\n" +
               "class ProgressWidget(DashboardWidget):\n" +
               "    def __init__(self, title, current, target):\n" +
               "        percentage = (current / target * 100) if target > 0 else 0\n" +
               "        super().__init__(title, 'widgets/progress.html', {\n" +
               "            'current': current,\n" +
               "            'target': target,\n" +
               "            'percentage': percentage\n" +
               "        })\n";
    }

    private String generateReportGenerators() {
        return "from django.http import HttpResponse\n" +
               "from django.template.loader import render_to_string\n" +
               "from reportlab.pdfgen import canvas\n" +
               "from reportlab.lib.pagesizes import letter, A4\n" +
               "from reportlab.lib import colors\n" +
               "from reportlab.platypus import SimpleDocTemplate, Table, TableStyle, Paragraph\n" +
               "from reportlab.lib.styles import getSampleStyleSheet\n" +
               "import csv\n" +
               "import xlsxwriter\n" +
               "from io import BytesIO\n" +
               "from datetime import datetime\n\n" +
               "class ReportGenerator:\n" +
               "    def __init__(self, title, data):\n" +
               "        self.title = title\n" +
               "        self.data = data\n" +
               "        self.timestamp = datetime.now()\n" +
               "    \n" +
               "    def generate_pdf(self):\n" +
               "        buffer = BytesIO()\n" +
               "        doc = SimpleDocTemplate(buffer, pagesize=letter)\n" +
               "        elements = []\n" +
               "        \n" +
               "        styles = getSampleStyleSheet()\n" +
               "        elements.append(Paragraph(self.title, styles['Title']))\n" +
               "        elements.append(Paragraph(f'Generated: {self.timestamp}', styles['Normal']))\n" +
               "        \n" +
               "        if self.data:\n" +
               "            table = Table(self.data)\n" +
               "            table.setStyle(TableStyle([\n" +
               "                ('BACKGROUND', (0, 0), (-1, 0), colors.grey),\n" +
               "                ('TEXTCOLOR', (0, 0), (-1, 0), colors.whitesmoke),\n" +
               "                ('ALIGN', (0, 0), (-1, -1), 'CENTER'),\n" +
               "                ('FONTNAME', (0, 0), (-1, 0), 'Helvetica-Bold'),\n" +
               "                ('FONTSIZE', (0, 0), (-1, 0), 14),\n" +
               "                ('BOTTOMPADDING', (0, 0), (-1, 0), 12),\n" +
               "                ('BACKGROUND', (0, 1), (-1, -1), colors.beige),\n" +
               "                ('GRID', (0, 0), (-1, -1), 1, colors.black)\n" +
               "            ]))\n" +
               "            elements.append(table)\n" +
               "        \n" +
               "        doc.build(elements)\n" +
               "        buffer.seek(0)\n" +
               "        return buffer\n" +
               "    \n" +
               "    def generate_csv(self):\n" +
               "        response = HttpResponse(content_type='text/csv')\n" +
               "        response['Content-Disposition'] = f'attachment; filename=\"{self.title}.csv\"'\n" +
               "        \n" +
               "        writer = csv.writer(response)\n" +
               "        for row in self.data:\n" +
               "            writer.writerow(row)\n" +
               "        \n" +
               "        return response\n" +
               "    \n" +
               "    def generate_excel(self):\n" +
               "        buffer = BytesIO()\n" +
               "        workbook = xlsxwriter.Workbook(buffer)\n" +
               "        worksheet = workbook.add_worksheet()\n" +
               "        \n" +
               "        # Add formats\n" +
               "        header_format = workbook.add_format({\n" +
               "            'bold': True,\n" +
               "            'bg_color': '#D7E4BD',\n" +
               "            'border': 1\n" +
               "        })\n" +
               "        \n" +
               "        # Write data\n" +
               "        for row_num, row_data in enumerate(self.data):\n" +
               "            for col_num, cell_data in enumerate(row_data):\n" +
               "                if row_num == 0:\n" +
               "                    worksheet.write(row_num, col_num, cell_data, header_format)\n" +
               "                else:\n" +
               "                    worksheet.write(row_num, col_num, cell_data)\n" +
               "        \n" +
               "        workbook.close()\n" +
               "        buffer.seek(0)\n" +
               "        return buffer\n\n" +
               "class SalesReport(ReportGenerator):\n" +
               "    def __init__(self, start_date, end_date):\n" +
               "        from .models import Order\n" +
               "        \n" +
               "        orders = Order.objects.filter(\n" +
               "            created_at__date__range=[start_date, end_date],\n" +
               "            paid=True\n" +
               "        )\n" +
               "        \n" +
               "        data = [['Order Number', 'Date', 'Customer', 'Total', 'Status']]\n" +
               "        for order in orders:\n" +
               "            data.append([\n" +
               "                order.order_number,\n" +
               "                order.created_at.strftime('%Y-%m-%d'),\n" +
               "                order.full_name,\n" +
               "                str(order.total_amount),\n" +
               "                order.status\n" +
               "            ])\n" +
               "        \n" +
               "        super().__init__(f'Sales Report {start_date} to {end_date}', data)\n";
    }

    private String generateReportViews() {
        return "from django.shortcuts import render\n" +
               "from django.views import View\n" +
               "from django.http import HttpResponse\n" +
               "from django.contrib.auth.decorators import login_required\n" +
               "from django.utils.decorators import method_decorator\n" +
               "from .report_generators import SalesReport, ReportGenerator\n" +
               "from datetime import datetime, timedelta\n\n" +
               "@method_decorator(login_required, name='dispatch')\n" +
               "class ReportView(View):\n" +
               "    def get(self, request):\n" +
               "        return render(request, 'reports/index.html')\n" +
               "    \n" +
               "    def post(self, request):\n" +
               "        report_type = request.POST.get('report_type')\n" +
               "        format_type = request.POST.get('format', 'pdf')\n" +
               "        start_date = request.POST.get('start_date')\n" +
               "        end_date = request.POST.get('end_date')\n" +
               "        \n" +
               "        if report_type == 'sales':\n" +
               "            report = SalesReport(start_date, end_date)\n" +
               "        else:\n" +
               "            return HttpResponse('Invalid report type', status=400)\n" +
               "        \n" +
               "        if format_type == 'pdf':\n" +
               "            buffer = report.generate_pdf()\n" +
               "            response = HttpResponse(buffer, content_type='application/pdf')\n" +
               "            response['Content-Disposition'] = f'attachment; filename=\"{report.title}.pdf\"'\n" +
               "        elif format_type == 'csv':\n" +
               "            response = report.generate_csv()\n" +
               "        elif format_type == 'excel':\n" +
               "            buffer = report.generate_excel()\n" +
               "            response = HttpResponse(\n" +
               "                buffer,\n" +
               "                content_type='application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'\n" +
               "            )\n" +
               "            response['Content-Disposition'] = f'attachment; filename=\"{report.title}.xlsx\"'\n" +
               "        else:\n" +
               "            return HttpResponse('Invalid format', status=400)\n" +
               "        \n" +
               "        return response\n";
    }

    private String generateDataCollectors() {
        return "from celery import shared_task\n" +
               "from django.utils import timezone\n" +
               "from django.db.models import Count, Sum, Avg\n" +
               "import logging\n\n" +
               "logger = logging.getLogger(__name__)\n\n" +
               "@shared_task\n" +
               "def collect_user_metrics():\n" +
               "    from .models import CustomUser, UserMetric\n" +
               "    \n" +
               "    for user in CustomUser.objects.filter(is_active=True):\n" +
               "        metric, created = UserMetric.objects.get_or_create(\n" +
               "            user=user,\n" +
               "            date=timezone.now().date()\n" +
               "        )\n" +
               "        \n" +
               "        metric.login_count = user.login_logs.filter(\n" +
               "            created_at__date=timezone.now().date()\n" +
               "        ).count()\n" +
               "        \n" +
               "        metric.page_views = user.page_views.filter(\n" +
               "            created_at__date=timezone.now().date()\n" +
               "        ).count()\n" +
               "        \n" +
               "        metric.save()\n" +
               "    \n" +
               "    logger.info('User metrics collected successfully')\n\n" +
               "@shared_task\n" +
               "def collect_sales_metrics():\n" +
               "    from .models import Order, SalesMetric\n" +
               "    \n" +
               "    today = timezone.now().date()\n" +
               "    \n" +
               "    orders = Order.objects.filter(created_at__date=today)\n" +
               "    \n" +
               "    metric, created = SalesMetric.objects.get_or_create(date=today)\n" +
               "    \n" +
               "    metric.total_orders = orders.count()\n" +
               "    metric.total_revenue = orders.filter(paid=True).aggregate(\n" +
               "        Sum('total_amount')\n" +
               "    )['total_amount__sum'] or 0\n" +
               "    metric.average_order_value = orders.filter(paid=True).aggregate(\n" +
               "        Avg('total_amount')\n" +
               "    )['total_amount__avg'] or 0\n" +
               "    \n" +
               "    metric.save()\n" +
               "    \n" +
               "    logger.info('Sales metrics collected successfully')\n";
    }

    private String generateDataProcessors() {
        return "import pandas as pd\n" +
               "import numpy as np\n" +
               "from sklearn.preprocessing import StandardScaler, LabelEncoder\n" +
               "from django.core.cache import cache\n" +
               "import logging\n\n" +
               "logger = logging.getLogger(__name__)\n\n" +
               "class DataProcessor:\n" +
               "    def __init__(self):\n" +
               "        self.scaler = StandardScaler()\n" +
               "        self.label_encoders = {}\n" +
               "    \n" +
               "    def process_dataframe(self, df):\n" +
               "        # Handle missing values\n" +
               "        df = self.handle_missing_values(df)\n" +
               "        \n" +
               "        # Encode categorical variables\n" +
               "        df = self.encode_categorical(df)\n" +
               "        \n" +
               "        # Scale numerical features\n" +
               "        df = self.scale_features(df)\n" +
               "        \n" +
               "        return df\n" +
               "    \n" +
               "    def handle_missing_values(self, df):\n" +
               "        # Fill numeric columns with median\n" +
               "        numeric_columns = df.select_dtypes(include=[np.number]).columns\n" +
               "        df[numeric_columns] = df[numeric_columns].fillna(df[numeric_columns].median())\n" +
               "        \n" +
               "        # Fill categorical columns with mode\n" +
               "        categorical_columns = df.select_dtypes(include=['object']).columns\n" +
               "        for col in categorical_columns:\n" +
               "            df[col] = df[col].fillna(df[col].mode()[0] if not df[col].mode().empty else 'unknown')\n" +
               "        \n" +
               "        return df\n" +
               "    \n" +
               "    def encode_categorical(self, df):\n" +
               "        categorical_columns = df.select_dtypes(include=['object']).columns\n" +
               "        \n" +
               "        for col in categorical_columns:\n" +
               "            if col not in self.label_encoders:\n" +
               "                self.label_encoders[col] = LabelEncoder()\n" +
               "                df[col] = self.label_encoders[col].fit_transform(df[col])\n" +
               "            else:\n" +
               "                df[col] = self.label_encoders[col].transform(df[col])\n" +
               "        \n" +
               "        return df\n" +
               "    \n" +
               "    def scale_features(self, df):\n" +
               "        numeric_columns = df.select_dtypes(include=[np.number]).columns\n" +
               "        df[numeric_columns] = self.scaler.fit_transform(df[numeric_columns])\n" +
               "        return df\n\n" +
               "class AggregationProcessor:\n" +
               "    @staticmethod\n" +
               "    def aggregate_time_series(data, freq='D', agg_func='sum'):\n" +
               "        df = pd.DataFrame(data)\n" +
               "        df['date'] = pd.to_datetime(df['date'])\n" +
               "        df.set_index('date', inplace=True)\n" +
               "        \n" +
               "        if agg_func == 'sum':\n" +
               "            return df.resample(freq).sum()\n" +
               "        elif agg_func == 'mean':\n" +
               "            return df.resample(freq).mean()\n" +
               "        elif agg_func == 'max':\n" +
               "            return df.resample(freq).max()\n" +
               "        elif agg_func == 'min':\n" +
               "            return df.resample(freq).min()\n" +
               "        else:\n" +
               "            return df.resample(freq).sum()\n";
    }

    private String generateMLModels() {
        return "from sklearn.ensemble import RandomForestClassifier, RandomForestRegressor\n" +
               "from sklearn.linear_model import LogisticRegression, LinearRegression\n" +
               "from sklearn.svm import SVC, SVR\n" +
               "from sklearn.model_selection import train_test_split, cross_val_score\n" +
               "from sklearn.metrics import accuracy_score, precision_score, recall_score, f1_score\n" +
               "import joblib\n" +
               "import os\n\n" +
               "class MLModel:\n" +
               "    def __init__(self, model_type='classification'):\n" +
               "        self.model_type = model_type\n" +
               "        self.model = None\n" +
               "        self.is_trained = False\n" +
               "    \n" +
               "    def create_model(self, algorithm='random_forest', **kwargs):\n" +
               "        if self.model_type == 'classification':\n" +
               "            if algorithm == 'random_forest':\n" +
               "                self.model = RandomForestClassifier(**kwargs)\n" +
               "            elif algorithm == 'logistic':\n" +
               "                self.model = LogisticRegression(**kwargs)\n" +
               "            elif algorithm == 'svm':\n" +
               "                self.model = SVC(**kwargs)\n" +
               "        else:\n" +
               "            if algorithm == 'random_forest':\n" +
               "                self.model = RandomForestRegressor(**kwargs)\n" +
               "            elif algorithm == 'linear':\n" +
               "                self.model = LinearRegression(**kwargs)\n" +
               "            elif algorithm == 'svm':\n" +
               "                self.model = SVR(**kwargs)\n" +
               "    \n" +
               "    def train(self, X, y, test_size=0.2):\n" +
               "        X_train, X_test, y_train, y_test = train_test_split(\n" +
               "            X, y, test_size=test_size, random_state=42\n" +
               "        )\n" +
               "        \n" +
               "        self.model.fit(X_train, y_train)\n" +
               "        self.is_trained = True\n" +
               "        \n" +
               "        # Evaluate\n" +
               "        predictions = self.model.predict(X_test)\n" +
               "        \n" +
               "        if self.model_type == 'classification':\n" +
               "            metrics = {\n" +
               "                'accuracy': accuracy_score(y_test, predictions),\n" +
               "                'precision': precision_score(y_test, predictions, average='weighted'),\n" +
               "                'recall': recall_score(y_test, predictions, average='weighted'),\n" +
               "                'f1': f1_score(y_test, predictions, average='weighted')\n" +
               "            }\n" +
               "        else:\n" +
               "            from sklearn.metrics import mean_squared_error, r2_score\n" +
               "            metrics = {\n" +
               "                'mse': mean_squared_error(y_test, predictions),\n" +
               "                'r2': r2_score(y_test, predictions)\n" +
               "            }\n" +
               "        \n" +
               "        return metrics\n" +
               "    \n" +
               "    def predict(self, X):\n" +
               "        if not self.is_trained:\n" +
               "            raise ValueError('Model must be trained before prediction')\n" +
               "        return self.model.predict(X)\n" +
               "    \n" +
               "    def save(self, filepath):\n" +
               "        joblib.dump(self.model, filepath)\n" +
               "    \n" +
               "    def load(self, filepath):\n" +
               "        self.model = joblib.load(filepath)\n" +
               "        self.is_trained = True\n";
    }

    private String generateMLTrainers() {
        return "from celery import shared_task\n" +
               "from .ml_models import MLModel\n" +
               "from .data_processors import DataProcessor\n" +
               "import pandas as pd\n" +
               "import logging\n\n" +
               "logger = logging.getLogger(__name__)\n\n" +
               "@shared_task\n" +
               "def train_recommendation_model():\n" +
               "    from .models import Order, OrderItem, Product\n" +
               "    \n" +
               "    # Prepare data\n" +
               "    order_items = OrderItem.objects.select_related('order', 'product').all()\n" +
               "    \n" +
               "    data = []\n" +
               "    for item in order_items:\n" +
               "        data.append({\n" +
               "            'user_id': item.order.user_id,\n" +
               "            'product_id': item.product_id,\n" +
               "            'quantity': item.quantity,\n" +
               "            'price': float(item.price),\n" +
               "            'category': item.product.category_id if item.product else None\n" +
               "        })\n" +
               "    \n" +
               "    df = pd.DataFrame(data)\n" +
               "    \n" +
               "    # Process data\n" +
               "    processor = DataProcessor()\n" +
               "    df = processor.process_dataframe(df)\n" +
               "    \n" +
               "    # Train model\n" +
               "    X = df[['user_id', 'category', 'price']]\n" +
               "    y = df['product_id']\n" +
               "    \n" +
               "    model = MLModel(model_type='classification')\n" +
               "    model.create_model(algorithm='random_forest', n_estimators=100)\n" +
               "    metrics = model.train(X, y)\n" +
               "    \n" +
               "    # Save model\n" +
               "    model.save('models/recommendation_model.pkl')\n" +
               "    \n" +
               "    logger.info(f'Recommendation model trained with metrics: {metrics}')\n" +
               "    return metrics\n\n" +
               "@shared_task\n" +
               "def train_churn_prediction_model():\n" +
               "    from .models import CustomUser\n" +
               "    from django.utils import timezone\n" +
               "    from datetime import timedelta\n" +
               "    \n" +
               "    # Prepare data\n" +
               "    users = CustomUser.objects.all()\n" +
               "    \n" +
               "    data = []\n" +
               "    for user in users:\n" +
               "        last_order = user.orders.order_by('-created_at').first()\n" +
               "        days_since_last_order = (\n" +
               "            (timezone.now() - last_order.created_at).days \n" +
               "            if last_order else 999\n" +
               "        )\n" +
               "        \n" +
               "        data.append({\n" +
               "            'user_id': user.id,\n" +
               "            'days_since_registration': (timezone.now() - user.date_joined).days,\n" +
               "            'order_count': user.orders.count(),\n" +
               "            'total_spent': float(user.orders.filter(paid=True).aggregate(\n" +
               "                Sum('total_amount')\n" +
               "            )['total_amount__sum'] or 0),\n" +
               "            'days_since_last_order': days_since_last_order,\n" +
               "            'is_churned': days_since_last_order > 90\n" +
               "        })\n" +
               "    \n" +
               "    df = pd.DataFrame(data)\n" +
               "    \n" +
               "    # Process data\n" +
               "    processor = DataProcessor()\n" +
               "    df = processor.process_dataframe(df)\n" +
               "    \n" +
               "    # Train model\n" +
               "    X = df.drop(['user_id', 'is_churned'], axis=1)\n" +
               "    y = df['is_churned']\n" +
               "    \n" +
               "    model = MLModel(model_type='classification')\n" +
               "    model.create_model(algorithm='logistic')\n" +
               "    metrics = model.train(X, y)\n" +
               "    \n" +
               "    # Save model\n" +
               "    model.save('models/churn_model.pkl')\n" +
               "    \n" +
               "    logger.info(f'Churn prediction model trained with metrics: {metrics}')\n" +
               "    return metrics\n";
    }

    private String generatePredictors() {
        return "from .ml_models import MLModel\n" +
               "from .data_processors import DataProcessor\n" +
               "import pandas as pd\n" +
               "import numpy as np\n\n" +
               "class RecommendationPredictor:\n" +
               "    def __init__(self):\n" +
               "        self.model = MLModel(model_type='classification')\n" +
               "        self.model.load('models/recommendation_model.pkl')\n" +
               "        self.processor = DataProcessor()\n" +
               "    \n" +
               "    def predict_for_user(self, user_id, n_recommendations=5):\n" +
               "        from .models import Product, Order\n" +
               "        \n" +
               "        # Get user's purchase history\n" +
               "        user_orders = Order.objects.filter(user_id=user_id)\n" +
               "        purchased_products = set()\n" +
               "        \n" +
               "        for order in user_orders:\n" +
               "            for item in order.items.all():\n" +
               "                purchased_products.add(item.product_id)\n" +
               "        \n" +
               "        # Get all products not purchased by user\n" +
               "        available_products = Product.objects.exclude(\n" +
               "            id__in=purchased_products\n" +
               "        ).filter(is_active=True)\n" +
               "        \n" +
               "        # Prepare data for prediction\n" +
               "        data = []\n" +
               "        for product in available_products:\n" +
               "            data.append({\n" +
               "                'user_id': user_id,\n" +
               "                'category': product.category_id,\n" +
               "                'price': float(product.price)\n" +
               "            })\n" +
               "        \n" +
               "        if not data:\n" +
               "            return []\n" +
               "        \n" +
               "        df = pd.DataFrame(data)\n" +
               "        df = self.processor.process_dataframe(df)\n" +
               "        \n" +
               "        # Get predictions\n" +
               "        predictions = self.model.predict_proba(df)[:, 1]\n" +
               "        \n" +
               "        # Get top N recommendations\n" +
               "        top_indices = np.argsort(predictions)[-n_recommendations:][::-1]\n" +
               "        recommended_products = [available_products[i] for i in top_indices]\n" +
               "        \n" +
               "        return recommended_products\n\n" +
               "class ChurnPredictor:\n" +
               "    def __init__(self):\n" +
               "        self.model = MLModel(model_type='classification')\n" +
               "        self.model.load('models/churn_model.pkl')\n" +
               "        self.processor = DataProcessor()\n" +
               "    \n" +
               "    def predict_churn_probability(self, user_id):\n" +
               "        from .models import CustomUser\n" +
               "        from django.utils import timezone\n" +
               "        \n" +
               "        user = CustomUser.objects.get(id=user_id)\n" +
               "        \n" +
               "        last_order = user.orders.order_by('-created_at').first()\n" +
               "        days_since_last_order = (\n" +
               "            (timezone.now() - last_order.created_at).days \n" +
               "            if last_order else 999\n" +
               "        )\n" +
               "        \n" +
               "        data = pd.DataFrame([{\n" +
               "            'days_since_registration': (timezone.now() - user.date_joined).days,\n" +
               "            'order_count': user.orders.count(),\n" +
               "            'total_spent': float(user.orders.filter(paid=True).aggregate(\n" +
               "                Sum('total_amount')\n" +
               "            )['total_amount__sum'] or 0),\n" +
               "            'days_since_last_order': days_since_last_order\n" +
               "        }])\n" +
               "        \n" +
               "        data = self.processor.process_dataframe(data)\n" +
               "        \n" +
               "        probability = self.model.predict_proba(data)[0, 1]\n" +
               "        return probability\n";
    }

    private String generatePredictionViews() {
        return "from django.shortcuts import render\n" +
               "from django.views import View\n" +
               "from django.http import JsonResponse\n" +
               "from django.contrib.auth.decorators import login_required\n" +
               "from django.utils.decorators import method_decorator\n" +
               "from .predictors import RecommendationPredictor, ChurnPredictor\n\n" +
               "@method_decorator(login_required, name='dispatch')\n" +
               "class RecommendationView(View):\n" +
               "    def get(self, request):\n" +
               "        predictor = RecommendationPredictor()\n" +
               "        recommendations = predictor.predict_for_user(\n" +
               "            request.user.id,\n" +
               "            n_recommendations=10\n" +
               "        )\n" +
               "        \n" +
               "        return render(request, 'predictions/recommendations.html', {\n" +
               "            'recommendations': recommendations\n" +
               "        })\n\n" +
               "@method_decorator(login_required, name='dispatch')\n" +
               "class ChurnPredictionView(View):\n" +
               "    def get(self, request):\n" +
               "        predictor = ChurnPredictor()\n" +
               "        probability = predictor.predict_churn_probability(request.user.id)\n" +
               "        \n" +
               "        return JsonResponse({\n" +
               "            'user_id': request.user.id,\n" +
               "            'churn_probability': probability,\n" +
               "            'risk_level': 'high' if probability > 0.7 else 'medium' if probability > 0.3 else 'low'\n" +
               "        })\n";
    }

    private String generateDatasetModels() {
        return "from django.db import models\n" +
               "from django.contrib.auth import get_user_model\n" +
               "import uuid\n\n" +
               "User = get_user_model()\n\n" +
               "class Dataset(models.Model):\n" +
               "    uuid = models.UUIDField(default=uuid.uuid4, editable=False, unique=True)\n" +
               "    name = models.CharField(max_length=200)\n" +
               "    description = models.TextField()\n" +
               "    \n" +
               "    file = models.FileField(upload_to='datasets/')\n" +
               "    file_type = models.CharField(max_length=10, choices=[\n" +
               "        ('csv', 'CSV'),\n" +
               "        ('json', 'JSON'),\n" +
               "        ('excel', 'Excel'),\n" +
               "        ('parquet', 'Parquet')\n" +
               "    ])\n" +
               "    \n" +
               "    columns = models.JSONField(default=dict)\n" +
               "    row_count = models.IntegerField(default=0)\n" +
               "    file_size = models.BigIntegerField(default=0)\n" +
               "    \n" +
               "    uploaded_by = models.ForeignKey(User, on_delete=models.SET_NULL, null=True)\n" +
               "    is_public = models.BooleanField(default=False)\n" +
               "    \n" +
               "    created_at = models.DateTimeField(auto_now_add=True)\n" +
               "    updated_at = models.DateTimeField(auto_now=True)\n\n" +
               "    class Meta:\n" +
               "        ordering = ['-created_at']\n\n" +
               "    def __str__(self):\n" +
               "        return self.name\n\n" +
               "class DatasetVersion(models.Model):\n" +
               "    dataset = models.ForeignKey(Dataset, on_delete=models.CASCADE, related_name='versions')\n" +
               "    version = models.CharField(max_length=20)\n" +
               "    file = models.FileField(upload_to='datasets/versions/')\n" +
               "    changes = models.TextField()\n" +
               "    created_at = models.DateTimeField(auto_now_add=True)\n\n" +
               "    class Meta:\n" +
               "        unique_together = ['dataset', 'version']\n" +
               "        ordering = ['-created_at']\n";
    }

    private String generateDatasetLoaders() {
        return "import pandas as pd\n" +
               "import json\n" +
               "import pyarrow.parquet as pq\n" +
               "from django.core.files.storage import default_storage\n" +
               "import logging\n\n" +
               "logger = logging.getLogger(__name__)\n\n" +
               "class DatasetLoader:\n" +
               "    @staticmethod\n" +
               "    def load_dataset(dataset):\n" +
               "        file_path = dataset.file.path\n" +
               "        \n" +
               "        if dataset.file_type == 'csv':\n" +
               "            return pd.read_csv(file_path)\n" +
               "        elif dataset.file_type == 'json':\n" +
               "            return pd.read_json(file_path)\n" +
               "        elif dataset.file_type == 'excel':\n" +
               "            return pd.read_excel(file_path)\n" +
               "        elif dataset.file_type == 'parquet':\n" +
               "            return pd.read_parquet(file_path)\n" +
               "        else:\n" +
               "            raise ValueError(f'Unsupported file type: {dataset.file_type}')\n" +
               "    \n" +
               "    @staticmethod\n" +
               "    def save_dataset(df, filepath, file_type='csv'):\n" +
               "        if file_type == 'csv':\n" +
               "            df.to_csv(filepath, index=False)\n" +
               "        elif file_type == 'json':\n" +
               "            df.to_json(filepath, orient='records')\n" +
               "        elif file_type == 'excel':\n" +
               "            df.to_excel(filepath, index=False)\n" +
               "        elif file_type == 'parquet':\n" +
               "            df.to_parquet(filepath, index=False)\n" +
               "        else:\n" +
               "            raise ValueError(f'Unsupported file type: {file_type}')\n" +
               "    \n" +
               "    @staticmethod\n" +
               "    def analyze_dataset(dataset):\n" +
               "        df = DatasetLoader.load_dataset(dataset)\n" +
               "        \n" +
               "        analysis = {\n" +
               "            'shape': df.shape,\n" +
               "            'columns': list(df.columns),\n" +
               "            'dtypes': df.dtypes.to_dict(),\n" +
               "            'missing_values': df.isnull().sum().to_dict(),\n" +
               "            'summary': df.describe().to_dict()\n" +
               "        }\n" +
               "        \n" +
               "        # Update dataset metadata\n" +
               "        dataset.columns = analysis['columns']\n" +
               "        dataset.row_count = analysis['shape'][0]\n" +
               "        dataset.save()\n" +
               "        \n" +
               "        return analysis\n";
    private String generateMainCSS() {
        return "/* Django Application Main Styles */\n\n" +
               ":root {\n" +
               "    --primary-color: #0C4B33;\n" +
               "    --secondary-color: #44B78B;\n" +
               "    --success-color: #28a745;\n" +
               "    --danger-color: #dc3545;\n" +
               "    --warning-color: #ffc107;\n" +
               "    --info-color: #17a2b8;\n" +
               "    --dark-color: #343a40;\n" +
               "    --light-color: #f8f9fa;\n" +
               "    --border-radius: 0.375rem;\n" +
               "    --box-shadow: 0 0.125rem 0.25rem rgba(0,0,0,0.075);\n" +
               "    --font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;\n" +
               "}\n\n" +
               "* {\n" +
               "    margin: 0;\n" +
               "    padding: 0;\n" +
               "    box-sizing: border-box;\n" +
               "}\n\n" +
               "body {\n" +
               "    font-family: var(--font-family);\n" +
               "    font-size: 1rem;\n" +
               "    line-height: 1.5;\n" +
               "    color: #212529;\n" +
               "    background-color: #fff;\n" +
               "}\n\n" +
               ".container {\n" +
               "    width: 100%;\n" +
               "    max-width: 1200px;\n" +
               "    margin: 0 auto;\n" +
               "    padding: 0 15px;\n" +
               "}\n\n" +
               "header {\n" +
               "    background-color: var(--primary-color);\n" +
               "    color: white;\n" +
               "    padding: 1rem 0;\n" +
               "    box-shadow: var(--box-shadow);\n" +
               "}\n\n" +
               "nav {\n" +
               "    display: flex;\n" +
               "    justify-content: space-between;\n" +
               "    align-items: center;\n" +
               "}\n\n" +
               "nav ul {\n" +
               "    list-style: none;\n" +
               "    display: flex;\n" +
               "    gap: 1.5rem;\n" +
               "}\n\n" +
               "nav a {\n" +
               "    color: white;\n" +
               "    text-decoration: none;\n" +
               "    padding: 0.5rem 1rem;\n" +
               "    border-radius: var(--border-radius);\n" +
               "    transition: background-color 0.3s ease;\n" +
               "}\n\n" +
               "nav a:hover {\n" +
               "    background-color: rgba(255,255,255,0.1);\n" +
               "}\n\n" +
               ".btn {\n" +
               "    display: inline-block;\n" +
               "    padding: 0.5rem 1rem;\n" +
               "    font-size: 1rem;\n" +
               "    font-weight: 400;\n" +
               "    line-height: 1.5;\n" +
               "    text-align: center;\n" +
               "    text-decoration: none;\n" +
               "    border: 1px solid transparent;\n" +
               "    border-radius: var(--border-radius);\n" +
               "    cursor: pointer;\n" +
               "    transition: all 0.15s ease-in-out;\n" +
               "}\n\n" +
               ".btn-primary {\n" +
               "    background-color: var(--primary-color);\n" +
               "    border-color: var(--primary-color);\n" +
               "    color: white;\n" +
               "}\n\n" +
               ".btn-primary:hover {\n" +
               "    background-color: #0a3d28;\n" +
               "}\n\n" +
               ".btn-success {\n" +
               "    background-color: var(--success-color);\n" +
               "    border-color: var(--success-color);\n" +
               "    color: white;\n" +
               "}\n\n" +
               ".btn-danger {\n" +
               "    background-color: var(--danger-color);\n" +
               "    border-color: var(--danger-color);\n" +
               "    color: white;\n" +
               "}\n\n" +
               ".card {\n" +
               "    background-color: white;\n" +
               "    border: 1px solid #dee2e6;\n" +
               "    border-radius: var(--border-radius);\n" +
               "    box-shadow: var(--box-shadow);\n" +
               "    padding: 1.5rem;\n" +
               "    margin-bottom: 1.5rem;\n" +
               "}\n\n" +
               ".form-group {\n" +
               "    margin-bottom: 1rem;\n" +
               "}\n\n" +
               ".form-label {\n" +
               "    display: block;\n" +
               "    margin-bottom: 0.5rem;\n" +
               "    font-weight: 500;\n" +
               "}\n\n" +
               ".form-control {\n" +
               "    display: block;\n" +
               "    width: 100%;\n" +
               "    padding: 0.5rem 0.75rem;\n" +
               "    font-size: 1rem;\n" +
               "    line-height: 1.5;\n" +
               "    color: #495057;\n" +
               "    background-color: #fff;\n" +
               "    border: 1px solid #ced4da;\n" +
               "    border-radius: var(--border-radius);\n" +
               "    transition: border-color 0.15s ease-in-out;\n" +
               "}\n\n" +
               ".form-control:focus {\n" +
               "    outline: none;\n" +
               "    border-color: var(--primary-color);\n" +
               "    box-shadow: 0 0 0 0.2rem rgba(12,75,51,0.25);\n" +
               "}\n\n" +
               ".alert {\n" +
               "    padding: 0.75rem 1.25rem;\n" +
               "    margin-bottom: 1rem;\n" +
               "    border: 1px solid transparent;\n" +
               "    border-radius: var(--border-radius);\n" +
               "}\n\n" +
               ".alert-success {\n" +
               "    background-color: #d4edda;\n" +
               "    border-color: #c3e6cb;\n" +
               "    color: #155724;\n" +
               "}\n\n" +
               ".alert-error, .alert-danger {\n" +
               "    background-color: #f8d7da;\n" +
               "    border-color: #f5c6cb;\n" +
               "    color: #721c24;\n" +
               "}\n\n" +
               ".alert-info {\n" +
               "    background-color: #d1ecf1;\n" +
               "    border-color: #bee5eb;\n" +
               "    color: #0c5460;\n" +
               "}\n\n" +
               ".table {\n" +
               "    width: 100%;\n" +
               "    border-collapse: collapse;\n" +
               "}\n\n" +
               ".table th,\n" +
               ".table td {\n" +
               "    padding: 0.75rem;\n" +
               "    text-align: left;\n" +
               "    border-bottom: 1px solid #dee2e6;\n" +
               "}\n\n" +
               ".table th {\n" +
               "    background-color: var(--light-color);\n" +
               "    font-weight: 600;\n" +
               "}\n\n" +
               "footer {\n" +
               "    margin-top: 3rem;\n" +
               "    padding: 2rem 0;\n" +
               "    background-color: var(--light-color);\n" +
               "    text-align: center;\n" +
               "}\n\n" +
               "@media (max-width: 768px) {\n" +
               "    nav ul {\n" +
               "        flex-direction: column;\n" +
               "        gap: 0.5rem;\n" +
               "    }\n" +
               "}\n";
    }

    private String generateMainJS() {
        return "// Django Application Main JavaScript\n\n" +
               "document.addEventListener('DOMContentLoaded', function() {\n" +
               "    console.log('Django application loaded');\n\n" +
               "    // CSRF Token handling for AJAX requests\n" +
               "    const csrftoken = getCookie('csrftoken');\n\n" +
               "    // Configure AJAX to include CSRF token\n" +
               "    if (typeof $ !== 'undefined') {\n" +
               "        $.ajaxSetup({\n" +
               "            beforeSend: function(xhr, settings) {\n" +
               "                if (!csrfSafeMethod(settings.type) && !this.crossDomain) {\n" +
               "                    xhr.setRequestHeader('X-CSRFToken', csrftoken);\n" +
               "                }\n" +
               "            }\n" +
               "        });\n" +
               "    }\n\n" +
               "    // Auto-dismiss alerts\n" +
               "    const alerts = document.querySelectorAll('.alert');\n" +
               "    alerts.forEach(alert => {\n" +
               "        setTimeout(() => {\n" +
               "            alert.style.opacity = '0';\n" +
               "            setTimeout(() => alert.remove(), 300);\n" +
               "        }, 5000);\n" +
               "    });\n\n" +
               "    // Form validation\n" +
               "    const forms = document.querySelectorAll('form[data-validate]');\n" +
               "    forms.forEach(form => {\n" +
               "        form.addEventListener('submit', function(e) {\n" +
               "            if (!validateForm(this)) {\n" +
               "                e.preventDefault();\n" +
               "            }\n" +
               "        });\n" +
               "    });\n" +
               "});\n\n" +
               "function getCookie(name) {\n" +
               "    let cookieValue = null;\n" +
               "    if (document.cookie && document.cookie !== '') {\n" +
               "        const cookies = document.cookie.split(';');\n" +
               "        for (let i = 0; i < cookies.length; i++) {\n" +
               "            const cookie = cookies[i].trim();\n" +
               "            if (cookie.substring(0, name.length + 1) === (name + '=')) {\n" +
               "                cookieValue = decodeURIComponent(cookie.substring(name.length + 1));\n" +
               "                break;\n" +
               "            }\n" +
               "        }\n" +
               "    }\n" +
               "    return cookieValue;\n" +
               "}\n\n" +
               "function csrfSafeMethod(method) {\n" +
               "    return (/^(GET|HEAD|OPTIONS|TRACE)$/.test(method));\n" +
               "}\n\n" +
               "function validateForm(form) {\n" +
               "    let isValid = true;\n" +
               "    const requiredFields = form.querySelectorAll('[required]');\n\n" +
               "    requiredFields.forEach(field => {\n" +
               "        if (!field.value.trim()) {\n" +
               "            showError(field, 'This field is required');\n" +
               "            isValid = false;\n" +
               "        } else {\n" +
               "            clearError(field);\n" +
               "        }\n" +
               "    });\n\n" +
               "    return isValid;\n" +
               "}\n\n" +
               "function showError(field, message) {\n" +
               "    clearError(field);\n" +
               "    field.classList.add('is-invalid');\n" +
               "    const errorDiv = document.createElement('div');\n" +
               "    errorDiv.className = 'invalid-feedback';\n" +
               "    errorDiv.textContent = message;\n" +
               "    field.parentNode.appendChild(errorDiv);\n" +
               "}\n\n" +
               "function clearError(field) {\n" +
               "    field.classList.remove('is-invalid');\n" +
               "    const errorDiv = field.parentNode.querySelector('.invalid-feedback');\n" +
               "    if (errorDiv) {\n" +
               "        errorDiv.remove();\n" +
               "    }\n" +
               "}\n\n" +
               "// API Helper for making authenticated requests\n" +
               "async function apiRequest(url, options = {}) {\n" +
               "    const csrftoken = getCookie('csrftoken');\n" +
               "    const defaults = {\n" +
               "        headers: {\n" +
               "            'Content-Type': 'application/json',\n" +
               "            'X-CSRFToken': csrftoken\n" +
               "        }\n" +
               "    };\n\n" +
               "    const config = { ...defaults, ...options };\n" +
               "    config.headers = { ...defaults.headers, ...options.headers };\n\n" +
               "    try {\n" +
               "        const response = await fetch(url, config);\n" +
               "        const data = await response.json();\n\n" +
               "        if (!response.ok) {\n" +
               "            throw new Error(data.error || 'Request failed');\n" +
               "        }\n\n" +
               "        return data;\n" +
               "    } catch (error) {\n" +
               "        console.error('API Error:', error);\n" +
               "        throw error;\n" +
               "    }\n" +
               "}\n";
    }

    private String generateBaseTemplate() {
        return "{% load static %}\n" +
               "<!DOCTYPE html>\n" +
               "<html lang=\"en\">\n" +
               "<head>\n" +
               "    <meta charset=\"UTF-8\">\n" +
               "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
               "    <meta http-equiv=\"X-UA-Compatible\" content=\"ie=edge\">\n" +
               "    <title>{% block title %}Django Application{% endblock %}</title>\n\n" +
               "    <!-- CSS -->\n" +
               "    <link rel=\"stylesheet\" href=\"{% static 'css/main.css' %}\">\n" +
               "    {% block extra_css %}{% endblock %}\n" +
               "</head>\n" +
               "<body>\n" +
               "    <!-- Navigation -->\n" +
               "    <header>\n" +
               "        <nav class=\"container\">\n" +
               "            <div class=\"logo\">\n" +
               "                <a href=\"{% url 'home' %}\">Django App</a>\n" +
               "            </div>\n" +
               "            <ul>\n" +
               "                <li><a href=\"{% url 'home' %}\">Home</a></li>\n" +
               "                {% if user.is_authenticated %}\n" +
               "                    <li><a href=\"{% url 'dashboard' %}\">Dashboard</a></li>\n" +
               "                    <li><a href=\"{% url 'profile' %}\">Profile</a></li>\n" +
               "                    {% if user.is_staff %}\n" +
               "                        <li><a href=\"{% url 'admin:index' %}\">Admin</a></li>\n" +
               "                    {% endif %}\n" +
               "                    <li><a href=\"{% url 'logout' %}\">Logout</a></li>\n" +
               "                {% else %}\n" +
               "                    <li><a href=\"{% url 'login' %}\">Login</a></li>\n" +
               "                    <li><a href=\"{% url 'register' %}\">Register</a></li>\n" +
               "                {% endif %}\n" +
               "            </ul>\n" +
               "        </nav>\n" +
               "    </header>\n\n" +
               "    <!-- Main Content -->\n" +
               "    <main class=\"container\" style=\"margin-top: 2rem; min-height: calc(100vh - 200px);\">\n" +
               "        <!-- Messages -->\n" +
               "        {% if messages %}\n" +
               "            {% for message in messages %}\n" +
               "                <div class=\"alert alert-{{ message.tags }}\" role=\"alert\">\n" +
               "                    {{ message }}\n" +
               "                </div>\n" +
               "            {% endfor %}\n" +
               "        {% endif %}\n\n" +
               "        <!-- Page Content -->\n" +
               "        {% block content %}{% endblock %}\n" +
               "    </main>\n\n" +
               "    <!-- Footer -->\n" +
               "    <footer>\n" +
               "        <div class=\"container\">\n" +
               "            <p>&copy; {% now 'Y' %} Django Application. All rights reserved.</p>\n" +
               "        </div>\n" +
               "    </footer>\n\n" +
               "    <!-- JavaScript -->\n" +
               "    <script src=\"{% static 'js/main.js' %}\"></script>\n" +
               "    {% block extra_js %}{% endblock %}\n" +
               "</body>\n" +
               "</html>\n";
    }

    private String generateIndexTemplate() {
        return "{% extends 'base.html' %}\n" +
               "{% load static %}\n\n" +
               "{% block title %}Home - Django Application{% endblock %}\n\n" +
               "{% block content %}\n" +
               "<div class=\"hero\" style=\"text-align: center; padding: 3rem 0;\">\n" +
               "    <h1>Welcome to Django Application</h1>\n" +
               "    <p style=\"font-size: 1.2rem; color: #666; margin: 1rem 0;\">A powerful web application built with Django</p>\n\n" +
               "    {% if not user.is_authenticated %}\n" +
               "        <div style=\"margin-top: 2rem;\">\n" +
               "            <a href=\"{% url 'register' %}\" class=\"btn btn-primary\" style=\"margin: 0 0.5rem;\">Get Started</a>\n" +
               "            <a href=\"{% url 'login' %}\" class=\"btn btn-success\" style=\"margin: 0 0.5rem;\">Sign In</a>\n" +
               "        </div>\n" +
               "    {% endif %}\n" +
               "</div>\n\n" +
               "<div style=\"display: grid; grid-template-columns: repeat(auto-fit, minmax(300px, 1fr)); gap: 1.5rem; margin-top: 3rem;\">\n" +
               "    <div class=\"card\">\n" +
               "        <h3>🚀 High Performance</h3>\n" +
               "        <p>Built with Django for robust and scalable applications</p>\n" +
               "    </div>\n\n" +
               "    <div class=\"card\">\n" +
               "        <h3>🔒 Secure</h3>\n" +
               "        <p>Enterprise-level security with Django's built-in protection</p>\n" +
               "    </div>\n\n" +
               "    <div class=\"card\">\n" +
               "        <h3>📱 Modern UI</h3>\n" +
               "        <p>Responsive design that works on all devices</p>\n" +
               "    </div>\n" +
               "</div>\n\n" +
               "{% if user.is_authenticated %}\n" +
               "<div class=\"card\" style=\"margin-top: 2rem;\">\n" +
               "    <h2>Welcome back, {{ user.get_full_name|default:user.username }}!</h2>\n" +
               "    <p>You're logged in and ready to use the application.</p>\n" +
               "    <a href=\"{% url 'dashboard' %}\" class=\"btn btn-primary\">Go to Dashboard</a>\n" +
               "</div>\n" +
               "{% endif %}\n" +
               "{% endblock %}\n";
    }
    private String generateTestBase() {
        return "\"\"\"Base test classes and utilities.\"\"\"\n\n" +
               "from django.test import TestCase, Client\n" +
               "from django.contrib.auth import get_user_model\n" +
               "from rest_framework.test import APITestCase, APIClient\n" +
               "from rest_framework_simplejwt.tokens import RefreshToken\n\n" +
               "User = get_user_model()\n\n" +
               "class BaseTestCase(TestCase):\n" +
               "    \"\"\"Base test case with common setup.\"\"\"\n\n" +
               "    def setUp(self):\n" +
               "        \"\"\"Set up test data.\"\"\"\n" +
               "        self.client = Client()\n" +
               "        self.create_test_users()\n\n" +
               "    def create_test_users(self):\n" +
               "        \"\"\"Create test users.\"\"\"\n" +
               "        self.user = User.objects.create_user(\n" +
               "            username='testuser',\n" +
               "            email='test@example.com',\n" +
               "            password='testpass123'\n" +
               "        )\n\n" +
               "        self.admin = User.objects.create_superuser(\n" +
               "            username='admin',\n" +
               "            email='admin@example.com',\n" +
               "            password='adminpass123'\n" +
               "        )\n\n" +
               "    def login_user(self, user=None):\n" +
               "        \"\"\"Log in a user.\"\"\"\n" +
               "        if user is None:\n" +
               "            user = self.user\n" +
               "        self.client.login(username=user.username, password='testpass123')\n\n" +
               "class BaseAPITestCase(APITestCase):\n" +
               "    \"\"\"Base API test case with authentication.\"\"\"\n\n" +
               "    def setUp(self):\n" +
               "        \"\"\"Set up test data.\"\"\"\n" +
               "        self.client = APIClient()\n" +
               "        self.create_test_users()\n\n" +
               "    def create_test_users(self):\n" +
               "        \"\"\"Create test users.\"\"\"\n" +
               "        self.user = User.objects.create_user(\n" +
               "            username='testuser',\n" +
               "            email='test@example.com',\n" +
               "            password='testpass123'\n" +
               "        )\n\n" +
               "        self.admin = User.objects.create_superuser(\n" +
               "            username='admin',\n" +
               "            email='admin@example.com',\n" +
               "            password='adminpass123'\n" +
               "        )\n\n" +
               "    def get_token(self, user=None):\n" +
               "        \"\"\"Get JWT token for user.\"\"\"\n" +
               "        if user is None:\n" +
               "            user = self.user\n" +
               "        refresh = RefreshToken.for_user(user)\n" +
               "        return str(refresh.access_token)\n\n" +
               "    def authenticate(self, user=None):\n" +
               "        \"\"\"Authenticate client with JWT token.\"\"\"\n" +
               "        token = self.get_token(user)\n" +
               "        self.client.credentials(HTTP_AUTHORIZATION=f'Bearer {token}')\n";
    }

    private String generateConftest() {
        return "\"\"\"Pytest configuration and fixtures.\"\"\"\n\n" +
               "import pytest\n" +
               "from django.contrib.auth import get_user_model\n" +
               "from rest_framework.test import APIClient\n\n" +
               "User = get_user_model()\n\n" +
               "@pytest.fixture\n" +
               "def api_client():\n" +
               "    \"\"\"Return API client.\"\"\"\n" +
               "    return APIClient()\n\n" +
               "@pytest.fixture\n" +
               "@pytest.mark.django_db\n" +
               "def test_user():\n" +
               "    \"\"\"Create and return test user.\"\"\"\n" +
               "    return User.objects.create_user(\n" +
               "        username='testuser',\n" +
               "        email='test@example.com',\n" +
               "        password='testpass123'\n" +
               "    )\n\n" +
               "@pytest.fixture\n" +
               "@pytest.mark.django_db\n" +
               "def admin_user():\n" +
               "    \"\"\"Create and return admin user.\"\"\"\n" +
               "    return User.objects.create_superuser(\n" +
               "        username='admin',\n" +
               "        email='admin@example.com',\n" +
               "        password='adminpass123'\n" +
               "    )\n\n" +
               "@pytest.fixture\n" +
               "def authenticated_client(api_client, test_user):\n" +
               "    \"\"\"Return authenticated API client.\"\"\"\n" +
               "    api_client.force_authenticate(user=test_user)\n" +
               "    return api_client\n\n" +
               "@pytest.fixture\n" +
               "def admin_client(api_client, admin_user):\n" +
               "    \"\"\"Return authenticated admin API client.\"\"\"\n" +
               "    api_client.force_authenticate(user=admin_user)\n" +
               "    return api_client\n";
    }

    private String generateValidators() {
        return "\"\"\"Custom validators for Django models and forms.\"\"\"\n\n" +
               "from django.core.exceptions import ValidationError\n" +
               "from django.utils.translation import gettext_lazy as _\n" +
               "import re\n\n" +
               "def validate_username(value):\n" +
               "    \"\"\"Validate username format.\"\"\"\n" +
               "    if not re.match(r'^[a-zA-Z0-9_]{3,30}$', value):\n" +
               "        raise ValidationError(\n" +
               "            _('Username must be 3-30 characters and contain only letters, numbers, and underscores.'),\n" +
               "            code='invalid_username'\n" +
               "        )\n\n" +
               "def validate_phone_number(value):\n" +
               "    \"\"\"Validate phone number format.\"\"\"\n" +
               "    phone_regex = re.compile(r'^\\+?1?\\d{9,15}$')\n" +
               "    if not phone_regex.match(value):\n" +
               "        raise ValidationError(\n" +
               "            _('Invalid phone number format.'),\n" +
               "            code='invalid_phone'\n" +
               "        )\n\n" +
               "def validate_file_size(value, max_size_mb=5):\n" +
               "    \"\"\"Validate uploaded file size.\"\"\"\n" +
               "    max_size = max_size_mb * 1024 * 1024\n" +
               "    if value.size > max_size:\n" +
               "        raise ValidationError(\n" +
               "            _(f'File size must not exceed {max_size_mb}MB.'),\n" +
               "            code='file_too_large'\n" +
               "        )\n\n" +
               "def validate_image_extension(value):\n" +
               "    \"\"\"Validate image file extension.\"\"\"\n" +
               "    import os\n" +
               "    ext = os.path.splitext(value.name)[1]\n" +
               "    valid_extensions = ['.jpg', '.jpeg', '.png', '.gif', '.webp']\n" +
               "    if ext.lower() not in valid_extensions:\n" +
               "        raise ValidationError(\n" +
               "            _(f'Unsupported file extension. Allowed: {', '.join(valid_extensions)}'),\n" +
               "            code='invalid_extension'\n" +
               "        )\n\n" +
               "class PasswordStrengthValidator:\n" +
               "    \"\"\"Validate password strength.\"\"\"\n\n" +
               "    def __init__(self, min_length=8, require_uppercase=True, require_lowercase=True,\n" +
               "                 require_numbers=True, require_special=True):\n" +
               "        self.min_length = min_length\n" +
               "        self.require_uppercase = require_uppercase\n" +
               "        self.require_lowercase = require_lowercase\n" +
               "        self.require_numbers = require_numbers\n" +
               "        self.require_special = require_special\n\n" +
               "    def validate(self, password, user=None):\n" +
               "        if len(password) < self.min_length:\n" +
               "            raise ValidationError(\n" +
               "                _(f'Password must be at least {self.min_length} characters long.'),\n" +
               "                code='password_too_short'\n" +
               "            )\n\n" +
               "        if self.require_uppercase and not re.search(r'[A-Z]', password):\n" +
               "            raise ValidationError(\n" +
               "                _('Password must contain at least one uppercase letter.'),\n" +
               "                code='password_no_upper'\n" +
               "            )\n\n" +
               "        if self.require_lowercase and not re.search(r'[a-z]', password):\n" +
               "            raise ValidationError(\n" +
               "                _('Password must contain at least one lowercase letter.'),\n" +
               "                code='password_no_lower'\n" +
               "            )\n\n" +
               "        if self.require_numbers and not re.search(r'\\d', password):\n" +
               "            raise ValidationError(\n" +
               "                _('Password must contain at least one number.'),\n" +
               "                code='password_no_number'\n" +
               "            )\n\n" +
               "        if self.require_special and not re.search(r'[!@#$%^&*(),.?\":{}|<>]', password):\n" +
               "            raise ValidationError(\n" +
               "                _('Password must contain at least one special character.'),\n" +
               "                code='password_no_special'\n" +
               "            )\n\n" +
               "    def get_help_text(self):\n" +
               "        return _('Your password must meet the strength requirements.')\n";
    }

    private String generatePermissions() {
        return "\"\"\"Custom permission classes for Django REST Framework.\"\"\"\n\n" +
               "from rest_framework import permissions\n\n" +
               "class IsOwnerOrReadOnly(permissions.BasePermission):\n" +
               "    \"\"\"Object-level permission to only allow owners to edit.\"\"\"\n\n" +
               "    def has_object_permission(self, request, view, obj):\n" +
               "        if request.method in permissions.SAFE_METHODS:\n" +
               "            return True\n" +
               "        return obj.owner == request.user or obj.user == request.user\n\n" +
               "class IsAdminOrReadOnly(permissions.BasePermission):\n" +
               "    \"\"\"Only admins can modify, others can only read.\"\"\"\n\n" +
               "    def has_permission(self, request, view):\n" +
               "        if request.method in permissions.SAFE_METHODS:\n" +
               "            return True\n" +
               "        return request.user and request.user.is_staff\n\n" +
               "class IsOwner(permissions.BasePermission):\n" +
               "    \"\"\"Only the owner can access the resource.\"\"\"\n\n" +
               "    def has_object_permission(self, request, view, obj):\n" +
               "        return obj.owner == request.user or obj.user == request.user\n\n" +
               "class IsSuperUser(permissions.BasePermission):\n" +
               "    \"\"\"Only superusers have permission.\"\"\"\n\n" +
               "    def has_permission(self, request, view):\n" +
               "        return request.user and request.user.is_superuser\n\n" +
               "class HasGroupPermission(permissions.BasePermission):\n" +
               "    \"\"\"Check if user is in required group.\"\"\"\n\n" +
               "    def has_permission(self, request, view):\n" +
               "        required_groups = getattr(view, 'required_groups', [])\n" +
               "        if not required_groups:\n" +
               "            return True\n" +
               "        return request.user.groups.filter(name__in=required_groups).exists()\n\n" +
               "class CanCreateOnly(permissions.BasePermission):\n" +
               "    \"\"\"Can only create, not read/update/delete.\"\"\"\n\n" +
               "    def has_permission(self, request, view):\n" +
               "        return request.method == 'POST'\n";
    }

    private String generateMixins() {
        return "\"\"\"Custom mixins for Django views.\"\"\"\n\n" +
               "from django.contrib import messages\n" +
               "from django.shortcuts import redirect\n" +
               "from django.contrib.auth.mixins import LoginRequiredMixin\n" +
               "from django.core.exceptions import PermissionDenied\n\n" +
               "class SuperUserRequiredMixin(LoginRequiredMixin):\n" +
               "    \"\"\"Verify that the current user is a superuser.\"\"\"\n\n" +
               "    def dispatch(self, request, *args, **kwargs):\n" +
               "        if not request.user.is_superuser:\n" +
               "            raise PermissionDenied\n" +
               "        return super().dispatch(request, *args, **kwargs)\n\n" +
               "class StaffRequiredMixin(LoginRequiredMixin):\n" +
               "    \"\"\"Verify that the current user is staff.\"\"\"\n\n" +
               "    def dispatch(self, request, *args, **kwargs):\n" +
               "        if not request.user.is_staff:\n" +
               "            raise PermissionDenied\n" +
               "        return super().dispatch(request, *args, **kwargs)\n\n" +
               "class GroupRequiredMixin(LoginRequiredMixin):\n" +
               "    \"\"\"Verify that the current user is in required group.\"\"\"\n" +
               "    required_groups = []\n\n" +
               "    def dispatch(self, request, *args, **kwargs):\n" +
               "        if not request.user.groups.filter(name__in=self.required_groups).exists():\n" +
               "            raise PermissionDenied\n" +
               "        return super().dispatch(request, *args, **kwargs)\n\n" +
               "class MessageMixin:\n" +
               "    \"\"\"Add success message after form submission.\"\"\"\n" +
               "    success_message = ''\n\n" +
               "    def form_valid(self, form):\n" +
               "        response = super().form_valid(form)\n" +
               "        if self.success_message:\n" +
               "            messages.success(self.request, self.success_message)\n" +
               "        return response\n\n" +
               "class AjaxableResponseMixin:\n" +
               "    \"\"\"Mixin to add AJAX support to a form.\"\"\"\n\n" +
               "    def form_invalid(self, form):\n" +
               "        response = super().form_invalid(form)\n" +
               "        if self.request.is_ajax():\n" +
               "            return JsonResponse(form.errors, status=400)\n" +
               "        return response\n\n" +
               "    def form_valid(self, form):\n" +
               "        response = super().form_valid(form)\n" +
               "        if self.request.is_ajax():\n" +
               "            data = {'pk': self.object.pk}\n" +
               "            return JsonResponse(data)\n" +
               "        return response\n";
    }
    private String generateRunDevScript() {
        return "#!/bin/bash\n" +
               "# Development server startup script\n\n" +
               "echo \"Starting Django development server...\"\n\n" +
               "# Load environment variables\n" +
               "if [ -f .env ]; then\n" +
               "    export $(cat .env | xargs)\n" +
               "fi\n\n" +
               "# Run migrations\n" +
               "echo \"Running migrations...\"\n" +
               "python manage.py migrate\n\n" +
               "# Collect static files\n" +
               "echo \"Collecting static files...\"\n" +
               "python manage.py collectstatic --noinput\n\n" +
               "# Start development server\n" +
               "echo \"Starting server on port 8000...\"\n" +
               "python manage.py runserver 0.0.0.0:8000\n";
    }

    private String generateMigrateScript() {
        return "#!/bin/bash\n" +
               "# Database migration script\n\n" +
               "echo \"Running Django migrations...\"\n\n" +
               "# Load environment variables\n" +
               "if [ -f .env ]; then\n" +
               "    export $(cat .env | xargs)\n" +
               "fi\n\n" +
               "# Make migrations\n" +
               "echo \"Making migrations...\"\n" +
               "python manage.py makemigrations\n\n" +
               "# Show migration plan\n" +
               "echo \"Migration plan:\"\n" +
               "python manage.py showmigrations\n\n" +
               "# Apply migrations\n" +
               "echo \"Applying migrations...\"\n" +
               "python manage.py migrate\n\n" +
               "echo \"Migrations completed successfully!\"\n";
    }

    private String generateAPIDocs(DjangoProjectType type) {
        return "# API Documentation\n\n" +
               "## Overview\n\n" +
               "This document describes the REST API endpoints for this Django application.\n\n" +
               "## Base URL\n\n" +
               "```\n" +
               "http://localhost:8000/api/v1/\n" +
               "```\n\n" +
               "## Authentication\n\n" +
               "The API uses JWT (JSON Web Token) authentication. Include the token in the Authorization header:\n\n" +
               "```\n" +
               "Authorization: Bearer <your_token>\n" +
               "```\n\n" +
               "## Endpoints\n\n" +
               "### Authentication\n\n" +
               "#### Register\n" +
               "```\n" +
               "POST /api/v1/auth/register/\n" +
               "```\n\n" +
               "**Request:**\n" +
               "```json\n" +
               "{\n" +
               "  \"username\": \"string\",\n" +
               "  \"email\": \"string\",\n" +
               "  \"password\": \"string\"\n" +
               "}\n" +
               "```\n\n" +
               "#### Login\n" +
               "```\n" +
               "POST /api/v1/auth/login/\n" +
               "```\n\n" +
               "**Request:**\n" +
               "```json\n" +
               "{\n" +
               "  \"username\": \"string\",\n" +
               "  \"password\": \"string\"\n" +
               "}\n" +
               "```\n\n" +
               "**Response:**\n" +
               "```json\n" +
               "{\n" +
               "  \"access\": \"string\",\n" +
               "  \"refresh\": \"string\"\n" +
               "}\n" +
               "```\n\n" +
               "### Users\n\n" +
               "#### List Users\n" +
               "```\n" +
               "GET /api/v1/users/\n" +
               "```\n\n" +
               "#### Get User\n" +
               "```\n" +
               "GET /api/v1/users/{id}/\n" +
               "```\n\n" +
               "#### Update User\n" +
               "```\n" +
               "PUT /api/v1/users/{id}/\n" +
               "PATCH /api/v1/users/{id}/\n" +
               "```\n\n" +
               "#### Delete User\n" +
               "```\n" +
               "DELETE /api/v1/users/{id}/\n" +
               "```\n\n" +
               "## Error Codes\n\n" +
               "- `400` - Bad Request\n" +
               "- `401` - Unauthorized\n" +
               "- `403` - Forbidden\n" +
               "- `404` - Not Found\n" +
               "- `500` - Internal Server Error\n";
    }

    private String generateRequirementsDev() {
        return "# Development Dependencies\n\n" +
               "# Testing\n" +
               "pytest==7.4.3\n" +
               "pytest-django==4.7.0\n" +
               "pytest-cov==4.1.0\n" +
               "factory-boy==3.3.0\n" +
               "faker==20.1.0\n" +
               "coverage==7.3.2\n\n" +
               "# Code Quality\n" +
               "black==23.11.0\n" +
               "flake8==6.1.0\n" +
               "pylint==3.0.2\n" +
               "pylint-django==2.5.5\n" +
               "mypy==1.7.1\n" +
               "django-stubs==4.2.6\n" +
               "isort==5.12.0\n\n" +
               "# Debugging\n" +
               "django-debug-toolbar==4.2.0\n" +
               "django-extensions==3.2.3\n" +
               "ipython==8.18.1\n" +
               "ipdb==0.13.13\n\n" +
               "# Documentation\n" +
               "sphinx==7.2.6\n" +
               "sphinx-rtd-theme==2.0.0\n\n" +
               "# Development Tools\n" +
               "watchdog==3.0.0\n" +
               "pre-commit==3.5.0\n";
    }

    private String generateEnvExample(DjangoProjectType type) {
        return "# Django Settings\n" +
               "DEBUG=True\n" +
               "SECRET_KEY=your-secret-key-here-change-in-production\n" +
               "ALLOWED_HOSTS=localhost,127.0.0.1\n\n" +
               "# Database\n" +
               "DATABASE_URL=sqlite:///db.sqlite3\n" +
               "# DATABASE_URL=postgresql://user:password@localhost:5432/dbname\n\n" +
               "# Redis\n" +
               "REDIS_URL=redis://localhost:6379/0\n" +
               "CELERY_BROKER_URL=redis://localhost:6379/0\n" +
               "CELERY_RESULT_BACKEND=redis://localhost:6379/0\n\n" +
               "# Email Configuration\n" +
               "EMAIL_BACKEND=django.core.mail.backends.console.EmailBackend\n" +
               "EMAIL_HOST=smtp.gmail.com\n" +
               "EMAIL_PORT=587\n" +
               "EMAIL_USE_TLS=True\n" +
               "EMAIL_HOST_USER=your-email@gmail.com\n" +
               "EMAIL_HOST_PASSWORD=your-app-password\n" +
               "DEFAULT_FROM_EMAIL=noreply@example.com\n\n" +
               "# AWS S3 (if using)\n" +
               "AWS_ACCESS_KEY_ID=\n" +
               "AWS_SECRET_ACCESS_KEY=\n" +
               "AWS_STORAGE_BUCKET_NAME=\n" +
               "AWS_S3_REGION_NAME=us-east-1\n\n" +
               "# API Keys\n" +
               "API_KEY=your-api-key\n\n" +
               "# Application Settings\n" +
               "SITE_URL=http://localhost:8000\n" +
               "CORS_ALLOWED_ORIGINS=http://localhost:3000,http://localhost:8080\n";
    }

    private String generateGitignore() {
        return "# Python\n" +
               "__pycache__/\n" +
               "*.py[cod]\n" +
               "*$py.class\n" +
               "*.so\n" +
               ".Python\n" +
               "build/\n" +
               "develop-eggs/\n" +
               "dist/\n" +
               "downloads/\n" +
               "eggs/\n" +
               ".eggs/\n" +
               "lib/\n" +
               "lib64/\n" +
               "parts/\n" +
               "sdist/\n" +
               "var/\n" +
               "wheels/\n" +
               "*.egg-info/\n" +
               ".installed.cfg\n" +
               "*.egg\n\n" +
               "# Django\n" +
               "*.log\n" +
               "local_settings.py\n" +
               "db.sqlite3\n" +
               "db.sqlite3-journal\n" +
               "/media\n" +
               "/static\n" +
               "staticfiles/\n\n" +
               "# Environment\n" +
               ".env\n" +
               ".env.local\n" +
               ".env.*.local\n" +
               "venv/\n" +
               "ENV/\n" +
               "env/\n\n" +
               "# IDE\n" +
               ".vscode/\n" +
               ".idea/\n" +
               "*.swp\n" +
               "*.swo\n" +
               "*~\n\n" +
               "# Testing\n" +
               ".pytest_cache/\n" +
               ".coverage\n" +
               "htmlcov/\n" +
               ".tox/\n\n" +
               "# OS\n" +
               ".DS_Store\n" +
               "Thumbs.db\n\n" +
               "# Celery\n" +
               "celerybeat-schedule\n" +
               "celerybeat.pid\n";
    }

    private String generatePyprojectToml(String projectName) {
        return "[tool.black]\n" +
               "line-length = 88\n" +
               "target-version = ['py311']\n" +
               "include = '\\.pyi?$'\n" +
               "exclude = '''\n" +
               "/(\n" +
               "    \\.git\n" +
               "  | \\.venv\n" +
               "  | migrations\n" +
               "  | build\n" +
               "  | dist\n" +
               ")/\n" +
               "'''\n\n" +
               "[tool.isort]\n" +
               "profile = \"black\"\n" +
               "line_length = 88\n" +
               "multi_line_output = 3\n" +
               "include_trailing_comma = true\n" +
               "skip_glob = [\"*/migrations/*\"]\n\n" +
               "[tool.pytest.ini_options]\n" +
               "DJANGO_SETTINGS_MODULE = \"" + projectName + ".settings.test\"\n" +
               "python_files = [\"test_*.py\", \"*_test.py\", \"tests.py\"]\n" +
               "addopts = \"-v --cov=. --cov-report=html --cov-report=term\"\n\n" +
               "[tool.mypy]\n" +
               "python_version = \"3.11\"\n" +
               "warn_return_any = true\n" +
               "warn_unused_configs = true\n" +
               "plugins = [\"mypy_django_plugin.main\"]\n\n" +
               "[tool.django-stubs]\n" +
               "django_settings_module = \"" + projectName + ".settings\"\n\n" +
               "[build-system]\n" +
               "requires = [\"setuptools>=61.0\"]\n" +
               "build-backend = \"setuptools.build_meta\"\n\n" +
               "[project]\n" +
               "name = \"" + projectName.toLowerCase().replace(" ", "-") + "\"\n" +
               "version = \"0.1.0\"\n" +
               "description = \"Django application\"\n" +
               "requires-python = \">=3.11\"\n";
    }

    private String generateSetupCfg() {
        return "[flake8]\n" +
               "max-line-length = 88\n" +
               "extend-ignore = E203, E501, W503\n" +
               "exclude =\n" +
               "    .git,\n" +
               "    __pycache__,\n" +
               "    */migrations/*,\n" +
               "    build,\n" +
               "    dist,\n" +
               "    venv,\n" +
               "    .venv\n\n" +
               "[pylint]\n" +
               "load-plugins = pylint_django\n" +
               "django-settings-module = project.settings\n" +
               "max-line-length = 88\n" +
               "disable =\n" +
               "    C0111,\n" +
               "    C0103,\n" +
               "    R0903,\n" +
               "    W0212\n\n" +
               "[coverage:run]\n" +
               "source = .\n" +
               "omit =\n" +
               "    */tests/*\n" +
               "    */migrations/*\n" +
               "    */venv/*\n" +
               "    */__pycache__/*\n" +
               "    manage.py\n" +
               "    */settings/*\n\n" +
               "[coverage:report]\n" +
               "precision = 2\n" +
               "show_missing = true\n" +
               "skip_covered = false\n";
    }

    private String generatePytestIni() {
        return "[pytest]\n" +
               "DJANGO_SETTINGS_MODULE = project.settings.test\n" +
               "python_files = test_*.py *_test.py tests.py\n" +
               "python_classes = Test*\n" +
               "python_functions = test_*\n" +
               "addopts =\n" +
               "    -v\n" +
               "    --strict-markers\n" +
               "    --tb=short\n" +
               "    --cov=.\n" +
               "    --cov-report=html\n" +
               "    --cov-report=term-missing:skip-covered\n" +
               "    --cov-fail-under=80\n" +
               "    --reuse-db\n" +
               "markers =\n" +
               "    unit: Unit tests\n" +
               "    integration: Integration tests\n" +
               "    slow: Slow running tests\n" +
               "    django_db: Tests that require database access\n";
    }

    private String generatePrecommitConfig() {
        return "# Pre-commit hooks configuration\n" +
               "repos:\n" +
               "  - repo: https://github.com/pre-commit/pre-commit-hooks\n" +
               "    rev: v4.5.0\n" +
               "    hooks:\n" +
               "      - id: trailing-whitespace\n" +
               "      - id: end-of-file-fixer\n" +
               "      - id: check-yaml\n" +
               "      - id: check-added-large-files\n" +
               "      - id: check-json\n" +
               "      - id: check-merge-conflict\n" +
               "      - id: detect-private-key\n\n" +
               "  - repo: https://github.com/psf/black\n" +
               "    rev: 23.11.0\n" +
               "    hooks:\n" +
               "      - id: black\n" +
               "        language_version: python3.11\n\n" +
               "  - repo: https://github.com/pycqa/isort\n" +
               "    rev: 5.12.0\n" +
               "    hooks:\n" +
               "      - id: isort\n" +
               "        args: [\"--profile\", \"black\"]\n\n" +
               "  - repo: https://github.com/pycqa/flake8\n" +
               "    rev: 6.1.0\n" +
               "    hooks:\n" +
               "      - id: flake8\n" +
               "        args: [\"--max-line-length=88\", \"--extend-ignore=E203,W503\"]\n\n" +
               "  - repo: https://github.com/pre-commit/mirrors-mypy\n" +
               "    rev: v1.7.1\n" +
               "    hooks:\n" +
               "      - id: mypy\n" +
               "        additional_dependencies: [django-stubs]\n";
    }

    private String generateEditorconfig() {
        return "# EditorConfig is awesome: https://EditorConfig.org\n\n" +
               "root = true\n\n" +
               "[*]\n" +
               "charset = utf-8\n" +
               "end_of_line = lf\n" +
               "insert_final_newline = true\n" +
               "trim_trailing_whitespace = true\n\n" +
               "[*.py]\n" +
               "indent_style = space\n" +
               "indent_size = 4\n" +
               "max_line_length = 88\n\n" +
               "[*.{js,jsx,ts,tsx,json,css,scss,html}]\n" +
               "indent_style = space\n" +
               "indent_size = 2\n\n" +
               "[*.md]\n" +
               "trim_trailing_whitespace = false\n\n" +
               "[Makefile]\n" +
               "indent_style = tab\n";
    }

    private String generateDockerignore() {
        return "**/__pycache__\n" +
               "**/*.pyc\n" +
               "**/*.pyo\n" +
               "**/*.pyd\n" +
               ".Python\n" +
               "*.so\n" +
               "*.egg\n" +
               "*.egg-info\n" +
               "dist\n" +
               "build\n" +
               ".git\n" +
               ".gitignore\n" +
               ".env\n" +
               ".env.local\n" +
               "venv/\n" +
               "ENV/\n" +
               "env/\n" +
               ".venv/\n" +
               ".pytest_cache/\n" +
               ".coverage\n" +
               "htmlcov/\n" +
               "db.sqlite3\n" +
               "*.log\n" +
               ".vscode/\n" +
               ".idea/\n" +
               "README.md\n" +
               "docker-compose.yml\n" +
               "node_modules/\n";
    }

    private String generateNginxConfig(String projectName) {
        return "events {\n" +
               "    worker_connections 1024;\n" +
               "}\n\n" +
               "http {\n" +
               "    upstream django_app {\n" +
               "        server web:8000;\n" +
               "    }\n\n" +
               "    server {\n" +
               "        listen 80;\n" +
               "        server_name localhost;\n" +
               "        client_max_body_size 20M;\n\n" +
               "        location / {\n" +
               "            proxy_pass http://django_app;\n" +
               "            proxy_set_header Host $host;\n" +
               "            proxy_set_header X-Real-IP $remote_addr;\n" +
               "            proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;\n" +
               "            proxy_set_header X-Forwarded-Proto $scheme;\n" +
               "            proxy_redirect off;\n" +
               "        }\n\n" +
               "        location /static/ {\n" +
               "            alias /app/staticfiles/;\n" +
               "            expires 30d;\n" +
               "            add_header Cache-Control \"public, immutable\";\n" +
               "        }\n\n" +
               "        location /media/ {\n" +
               "            alias /app/media/;\n" +
               "            expires 7d;\n" +
               "        }\n\n" +
               "        location /health/ {\n" +
               "            access_log off;\n" +
               "            return 200 \"healthy\\n\";\n" +
               "            add_header Content-Type text/plain;\n" +
               "        }\n" +
               "    }\n" +
               "}\n";
    }

    private String generateEntrypoint() {
        return "#!/bin/bash\n" +
               "set -e\n\n" +
               "echo \"Waiting for database...\"\n" +
               "while ! nc -z db 5432; do\n" +
               "  sleep 0.1\n" +
               "done\n" +
               "echo \"Database started\"\n\n" +
               "echo \"Waiting for Redis...\"\n" +
               "while ! nc -z redis 6379; do\n" +
               "  sleep 0.1\n" +
               "done\n" +
               "echo \"Redis started\"\n\n" +
               "echo \"Running database migrations...\"\n" +
               "python manage.py migrate --noinput\n\n" +
               "echo \"Collecting static files...\"\n" +
               "python manage.py collectstatic --noinput\n\n" +
               "echo \"Creating superuser if doesn't exist...\"\n" +
               "python manage.py shell -c \"from django.contrib.auth import get_user_model; User = get_user_model(); User.objects.filter(username='admin').exists() or User.objects.create_superuser('admin', 'admin@example.com', 'admin123')\" || true\n\n" +
               "echo \"Starting application...\"\n" +
               "exec \"$@\"\n";
    }

    private String generateCIWorkflow(String projectName) {
        return "name: CI\n\n" +
               "on:\n" +
               "  push:\n" +
               "    branches: [ main, develop ]\n" +
               "  pull_request:\n" +
               "    branches: [ main, develop ]\n\n" +
               "jobs:\n" +
               "  test:\n" +
               "    runs-on: ubuntu-latest\n\n" +
               "    services:\n" +
               "      postgres:\n" +
               "        image: postgres:15\n" +
               "        env:\n" +
               "          POSTGRES_PASSWORD: postgres\n" +
               "          POSTGRES_DB: test_db\n" +
               "        options: >-\n" +
               "          --health-cmd pg_isready\n" +
               "          --health-interval 10s\n" +
               "          --health-timeout 5s\n" +
               "          --health-retries 5\n" +
               "        ports:\n" +
               "          - 5432:5432\n\n" +
               "      redis:\n" +
               "        image: redis:7-alpine\n" +
               "        options: >-\n" +
               "          --health-cmd \"redis-cli ping\"\n" +
               "          --health-interval 10s\n" +
               "          --health-timeout 5s\n" +
               "          --health-retries 5\n" +
               "        ports:\n" +
               "          - 6379:6379\n\n" +
               "    steps:\n" +
               "    - uses: actions/checkout@v3\n\n" +
               "    - name: Set up Python\n" +
               "      uses: actions/setup-python@v4\n" +
               "      with:\n" +
               "        python-version: '3.11'\n\n" +
               "    - name: Cache dependencies\n" +
               "      uses: actions/cache@v3\n" +
               "      with:\n" +
               "        path: ~/.cache/pip\n" +
               "        key: ${{ runner.os }}-pip-${{ hashFiles('**/requirements.txt') }}\n\n" +
               "    - name: Install dependencies\n" +
               "      run: |\n" +
               "        python -m pip install --upgrade pip\n" +
               "        pip install -r requirements.txt\n" +
               "        pip install -r requirements-dev.txt\n\n" +
               "    - name: Lint with flake8\n" +
               "      run: |\n" +
               "        flake8 .\n\n" +
               "    - name: Check code formatting\n" +
               "      run: |\n" +
               "        black --check .\n\n" +
               "    - name: Run tests\n" +
               "      env:\n" +
               "        DATABASE_URL: postgresql://postgres:postgres@localhost:5432/test_db\n" +
               "        REDIS_URL: redis://localhost:6379/0\n" +
               "      run: |\n" +
               "        pytest --cov=. --cov-report=xml\n\n" +
               "    - name: Upload coverage\n" +
               "      uses: codecov/codecov-action@v3\n" +
               "      with:\n" +
               "        files: ./coverage.xml\n";
    }

    private String generateDeployWorkflow(String projectName) {
        return "name: Deploy\n\n" +
               "on:\n" +
               "  push:\n" +
               "    branches: [ main ]\n" +
               "  workflow_dispatch:\n\n" +
               "jobs:\n" +
               "  deploy:\n" +
               "    runs-on: ubuntu-latest\n\n" +
               "    steps:\n" +
               "    - uses: actions/checkout@v3\n\n" +
               "    - name: Set up Docker Buildx\n" +
               "      uses: docker/setup-buildx-action@v2\n\n" +
               "    - name: Login to Docker Hub\n" +
               "      uses: docker/login-action@v2\n" +
               "      with:\n" +
               "        username: ${{ secrets.DOCKER_USERNAME }}\n" +
               "        password: ${{ secrets.DOCKER_PASSWORD }}\n\n" +
               "    - name: Build and push\n" +
               "      uses: docker/build-push-action@v4\n" +
               "      with:\n" +
               "        context: .\n" +
               "        push: true\n" +
               "        tags: ${{ secrets.DOCKER_USERNAME }}/" + projectName.toLowerCase().replace(" ", "-") + ":latest\n" +
               "        cache-from: type=registry,ref=${{ secrets.DOCKER_USERNAME }}/" + projectName.toLowerCase().replace(" ", "-") + ":buildcache\n" +
               "        cache-to: type=registry,ref=${{ secrets.DOCKER_USERNAME }}/" + projectName.toLowerCase().replace(" ", "-") + ":buildcache,mode=max\n\n" +
               "    - name: Deploy to production\n" +
               "      run: |\n" +
               "        echo \"Add deployment commands here\"\n" +
               "        # Example: Deploy to cloud provider\n";
    }

    private String generateDependabotConfig() {
        return "version: 2\n" +
               "updates:\n" +
               "  - package-ecosystem: \"pip\"\n" +
               "    directory: \"/\"\n" +
               "    schedule:\n" +
               "      interval: \"weekly\"\n" +
               "      day: \"monday\"\n" +
               "    open-pull-requests-limit: 10\n" +
               "    reviewers:\n" +
               "      - \"your-github-username\"\n" +
               "    assignees:\n" +
               "      - \"your-github-username\"\n" +
               "    labels:\n" +
               "      - \"dependencies\"\n" +
               "      - \"python\"\n\n" +
               "  - package-ecosystem: \"docker\"\n" +
               "    directory: \"/\"\n" +
               "    schedule:\n" +
               "      interval: \"weekly\"\n" +
               "    labels:\n" +
               "      - \"dependencies\"\n" +
               "      - \"docker\"\n\n" +
               "  - package-ecosystem: \"github-actions\"\n" +
               "    directory: \"/\"\n" +
               "    schedule:\n" +
               "      interval: \"weekly\"\n" +
               "    labels:\n" +
               "      - \"dependencies\"\n" +
               "      - \"github-actions\"\n";
    }
}