package com.structurecreation.service.generator;

import com.structurecreation.model.ProjectNode;
import com.structurecreation.service.DependencyResolverService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class FlaskProjectGenerator {

    @Autowired
    private DependencyResolverService dependencyResolver;

    public enum FlaskProjectType {
        REST_API("RESTful API with Flask-RESTful"),
        WEB_APP("Traditional web application"),
        MICROSERVICE("Lightweight microservice"),
        DATA_API("Data science API"),
        ASYNC_APP("Async application with Quart"),
        GRAPHQL_API("GraphQL API"),
        WEBSOCKET_APP("Real-time WebSocket application");

        private final String description;

        FlaskProjectType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    public ProjectNode generateFlaskProject(String projectName, FlaskProjectType type) {
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

    private void createProjectStructure(ProjectNode root, String projectName, FlaskProjectType type) {
        // Application package
        ProjectNode appDir = new ProjectNode("app", true, root);
        root.addChild(appDir);

        // __init__.py with app factory
        ProjectNode appInit = new ProjectNode("__init__.py", false, appDir);
        appInit.setContent(generateAppFactory(type));
        appDir.addChild(appInit);

        // Config module
        ProjectNode config = new ProjectNode("config.py", false, appDir);
        config.setContent(generateConfig());
        appDir.addChild(config);

        // Models directory
        ProjectNode modelsDir = new ProjectNode("models", true, appDir);
        appDir.addChild(modelsDir);

        ProjectNode modelsInit = new ProjectNode("__init__.py", false, modelsDir);
        modelsInit.setContent("from .base import db\nfrom .user import User\nfrom .role import Role");
        modelsDir.addChild(modelsInit);

        ProjectNode baseModel = new ProjectNode("base.py", false, modelsDir);
        baseModel.setContent(generateBaseModel());
        modelsDir.addChild(baseModel);

        ProjectNode userModel = new ProjectNode("user.py", false, modelsDir);
        userModel.setContent(generateUserModel());
        modelsDir.addChild(userModel);

        ProjectNode roleModel = new ProjectNode("role.py", false, modelsDir);
        roleModel.setContent(generateRoleModel());
        modelsDir.addChild(roleModel);

        // Create type-specific structure
        switch (type) {
            case REST_API:
                createRESTAPIStructure(appDir);
                break;
            case WEB_APP:
                createWebAppStructure(appDir);
                break;
            case MICROSERVICE:
                createMicroserviceStructure(appDir);
                break;
            case DATA_API:
                createDataAPIStructure(appDir);
                break;
            case ASYNC_APP:
                createAsyncStructure(appDir);
                break;
            case GRAPHQL_API:
                createGraphQLStructure(appDir);
                break;
            case WEBSOCKET_APP:
                createWebSocketStructure(appDir);
                break;
        }

        // Common directories
        ProjectNode utilsDir = new ProjectNode("utils", true, appDir);
        appDir.addChild(utilsDir);

        ProjectNode utilsInit = new ProjectNode("__init__.py", false, utilsDir);
        utilsInit.setContent("");
        utilsDir.addChild(utilsInit);

        ProjectNode validators = new ProjectNode("validators.py", false, utilsDir);
        validators.setContent(generateValidators());
        utilsDir.addChild(validators);

        ProjectNode decorators = new ProjectNode("decorators.py", false, utilsDir);
        decorators.setContent(generateDecorators());
        utilsDir.addChild(decorators);

        ProjectNode helpers = new ProjectNode("helpers.py", false, utilsDir);
        helpers.setContent(generateHelpers());
        utilsDir.addChild(helpers);

        // Extensions
        ProjectNode extensions = new ProjectNode("extensions.py", false, appDir);
        extensions.setContent(generateExtensions(type));
        appDir.addChild(extensions);

        // Static files
        ProjectNode staticDir = new ProjectNode("static", true, root);
        root.addChild(staticDir);

        ProjectNode cssDir = new ProjectNode("css", true, staticDir);
        staticDir.addChild(cssDir);
        ProjectNode mainCss = new ProjectNode("style.css", false, cssDir);
        mainCss.setContent(generateMainCSS());
        cssDir.addChild(mainCss);

        ProjectNode jsDir = new ProjectNode("js", true, staticDir);
        staticDir.addChild(jsDir);
        ProjectNode mainJs = new ProjectNode("app.js", false, jsDir);
        mainJs.setContent(generateMainJS(type));
        jsDir.addChild(mainJs);

        ProjectNode imgDir = new ProjectNode("img", true, staticDir);
        staticDir.addChild(imgDir);

        // Templates
        ProjectNode templatesDir = new ProjectNode("templates", true, root);
        root.addChild(templatesDir);

        ProjectNode baseTemplate = new ProjectNode("base.html", false, templatesDir);
        baseTemplate.setContent(generateBaseTemplate());
        templatesDir.addChild(baseTemplate);

        ProjectNode indexTemplate = new ProjectNode("index.html", false, templatesDir);
        indexTemplate.setContent(generateIndexTemplate());
        templatesDir.addChild(indexTemplate);

        ProjectNode errorDir = new ProjectNode("errors", true, templatesDir);
        templatesDir.addChild(errorDir);

        ProjectNode error404 = new ProjectNode("404.html", false, errorDir);
        error404.setContent(generate404Template());
        errorDir.addChild(error404);

        ProjectNode error500 = new ProjectNode("500.html", false, errorDir);
        error500.setContent(generate500Template());
        errorDir.addChild(error500);

        // Tests directory
        ProjectNode testsDir = new ProjectNode("tests", true, root);
        root.addChild(testsDir);

        ProjectNode testsInit = new ProjectNode("__init__.py", false, testsDir);
        testsInit.setContent("");
        testsDir.addChild(testsInit);

        ProjectNode conftest = new ProjectNode("conftest.py", false, testsDir);
        conftest.setContent(generateConftest());
        testsDir.addChild(conftest);

        ProjectNode testModels = new ProjectNode("test_models.py", false, testsDir);
        testModels.setContent(generateTestModels());
        testsDir.addChild(testModels);

        ProjectNode testAPI = new ProjectNode("test_api.py", false, testsDir);
        testAPI.setContent(generateTestAPI());
        testsDir.addChild(testAPI);

        // Migrations directory
        ProjectNode migrationsDir = new ProjectNode("migrations", true, root);
        root.addChild(migrationsDir);

        // Instance directory for config
        ProjectNode instanceDir = new ProjectNode("instance", true, root);
        root.addChild(instanceDir);

        // Logs directory
        ProjectNode logsDir = new ProjectNode("logs", true, root);
        root.addChild(logsDir);

        // Scripts directory
        ProjectNode scriptsDir = new ProjectNode("scripts", true, root);
        root.addChild(scriptsDir);

        ProjectNode initDb = new ProjectNode("init_db.py", false, scriptsDir);
        initDb.setContent(generateInitDbScript());
        scriptsDir.addChild(initDb);

        ProjectNode seedDb = new ProjectNode("seed_db.py", false, scriptsDir);
        seedDb.setContent(generateSeedDbScript());
        scriptsDir.addChild(seedDb);

        // Documentation
        ProjectNode docsDir = new ProjectNode("docs", true, root);
        root.addChild(docsDir);

        ProjectNode apiDocs = new ProjectNode("api.md", false, docsDir);
        apiDocs.setContent(generateAPIDocs(type));
        docsDir.addChild(apiDocs);

        // Main application entry point
        ProjectNode wsgi = new ProjectNode("wsgi.py", false, root);
        wsgi.setContent(generateWSGI());
        root.addChild(wsgi);

        ProjectNode runApp = new ProjectNode("run.py", false, root);
        runApp.setContent(generateRunApp());
        root.addChild(runApp);
    }

    private void createRESTAPIStructure(ProjectNode appDir) {
        // API package
        ProjectNode apiDir = new ProjectNode("api", true, appDir);
        appDir.addChild(apiDir);

        ProjectNode apiInit = new ProjectNode("__init__.py", false, apiDir);
        apiInit.setContent("");
        apiDir.addChild(apiInit);

        // API v1
        ProjectNode v1Dir = new ProjectNode("v1", true, apiDir);
        apiDir.addChild(v1Dir);

        ProjectNode v1Init = new ProjectNode("__init__.py", false, v1Dir);
        v1Init.setContent("");
        v1Dir.addChild(v1Init);

        // Resources
        ProjectNode resourcesDir = new ProjectNode("resources", true, v1Dir);
        v1Dir.addChild(resourcesDir);

        ProjectNode resourcesInit = new ProjectNode("__init__.py", false, resourcesDir);
        resourcesInit.setContent("");
        resourcesDir.addChild(resourcesInit);

        ProjectNode userResource = new ProjectNode("user.py", false, resourcesDir);
        userResource.setContent(generateUserResource());
        resourcesDir.addChild(userResource);

        ProjectNode authResource = new ProjectNode("auth.py", false, resourcesDir);
        authResource.setContent(generateAuthResource());
        resourcesDir.addChild(authResource);

        // Schemas
        ProjectNode schemasDir = new ProjectNode("schemas", true, v1Dir);
        v1Dir.addChild(schemasDir);

        ProjectNode schemasInit = new ProjectNode("__init__.py", false, schemasDir);
        schemasInit.setContent("");
        schemasDir.addChild(schemasInit);

        ProjectNode userSchema = new ProjectNode("user_schema.py", false, schemasDir);
        userSchema.setContent(generateUserSchema());
        schemasDir.addChild(userSchema);

        ProjectNode authSchema = new ProjectNode("auth_schema.py", false, schemasDir);
        authSchema.setContent(generateAuthSchema());
        schemasDir.addChild(authSchema);

        // Routes
        ProjectNode routes = new ProjectNode("routes.py", false, v1Dir);
        routes.setContent(generateAPIRoutes());
        v1Dir.addChild(routes);

        // Middleware
        ProjectNode middleware = new ProjectNode("middleware.py", false, apiDir);
        middleware.setContent(generateMiddleware());
        apiDir.addChild(middleware);
    }

    private void createWebAppStructure(ProjectNode appDir) {
        // Views package
        ProjectNode viewsDir = new ProjectNode("views", true, appDir);
        appDir.addChild(viewsDir);

        ProjectNode viewsInit = new ProjectNode("__init__.py", false, viewsDir);
        viewsInit.setContent("");
        viewsDir.addChild(viewsInit);

        ProjectNode mainViews = new ProjectNode("main.py", false, viewsDir);
        mainViews.setContent(generateMainViews());
        viewsDir.addChild(mainViews);

        ProjectNode authViews = new ProjectNode("auth.py", false, viewsDir);
        authViews.setContent(generateAuthViews());
        viewsDir.addChild(authViews);

        ProjectNode adminViews = new ProjectNode("admin.py", false, viewsDir);
        adminViews.setContent(generateAdminViews());
        viewsDir.addChild(adminViews);

        // Forms package
        ProjectNode formsDir = new ProjectNode("forms", true, appDir);
        appDir.addChild(formsDir);

        ProjectNode formsInit = new ProjectNode("__init__.py", false, formsDir);
        formsInit.setContent("");
        formsDir.addChild(formsInit);

        ProjectNode loginForm = new ProjectNode("auth_forms.py", false, formsDir);
        loginForm.setContent(generateAuthForms());
        formsDir.addChild(loginForm);

        ProjectNode userForm = new ProjectNode("user_forms.py", false, formsDir);
        userForm.setContent(generateUserForms());
        formsDir.addChild(userForm);
    }

    private void createMicroserviceStructure(ProjectNode appDir) {
        // Services package
        ProjectNode servicesDir = new ProjectNode("services", true, appDir);
        appDir.addChild(servicesDir);

        ProjectNode servicesInit = new ProjectNode("__init__.py", false, servicesDir);
        servicesInit.setContent("");
        servicesDir.addChild(servicesInit);

        ProjectNode dataService = new ProjectNode("data_service.py", false, servicesDir);
        dataService.setContent(generateDataService());
        servicesDir.addChild(dataService);

        ProjectNode cacheService = new ProjectNode("cache_service.py", false, servicesDir);
        cacheService.setContent(generateCacheService());
        servicesDir.addChild(cacheService);

        ProjectNode messageService = new ProjectNode("message_service.py", false, servicesDir);
        messageService.setContent(generateMessageService());
        servicesDir.addChild(messageService);

        // Handlers
        ProjectNode handlersDir = new ProjectNode("handlers", true, appDir);
        appDir.addChild(handlersDir);

        ProjectNode handlersInit = new ProjectNode("__init__.py", false, handlersDir);
        handlersInit.setContent("");
        handlersDir.addChild(handlersInit);

        ProjectNode eventHandlers = new ProjectNode("event_handlers.py", false, handlersDir);
        eventHandlers.setContent(generateEventHandlers());
        handlersDir.addChild(eventHandlers);

        ProjectNode errorHandlers = new ProjectNode("error_handlers.py", false, handlersDir);
        errorHandlers.setContent(generateErrorHandlers());
        handlersDir.addChild(errorHandlers);
    }

    private void createDataAPIStructure(ProjectNode appDir) {
        // Data processing package
        ProjectNode dataDir = new ProjectNode("data", true, appDir);
        appDir.addChild(dataDir);

        ProjectNode dataInit = new ProjectNode("__init__.py", false, dataDir);
        dataInit.setContent("");
        dataDir.addChild(dataInit);

        ProjectNode processors = new ProjectNode("processors.py", false, dataDir);
        processors.setContent(generateDataProcessors());
        dataDir.addChild(processors);

        ProjectNode transformers = new ProjectNode("transformers.py", false, dataDir);
        transformers.setContent(generateDataTransformers());
        dataDir.addChild(transformers);

        ProjectNode analyzers = new ProjectNode("analyzers.py", false, dataDir);
        analyzers.setContent(generateDataAnalyzers());
        dataDir.addChild(analyzers);

        // ML package
        ProjectNode mlDir = new ProjectNode("ml", true, appDir);
        appDir.addChild(mlDir);

        ProjectNode mlInit = new ProjectNode("__init__.py", false, mlDir);
        mlInit.setContent("");
        mlDir.addChild(mlInit);

        ProjectNode models = new ProjectNode("models.py", false, mlDir);
        models.setContent(generateMLModels());
        mlDir.addChild(models);

        ProjectNode predictors = new ProjectNode("predictors.py", false, mlDir);
        predictors.setContent(generatePredictors());
        mlDir.addChild(predictors);
    }

    private void createAsyncStructure(ProjectNode appDir) {
        // Async handlers
        ProjectNode asyncDir = new ProjectNode("async_handlers", true, appDir);
        appDir.addChild(asyncDir);

        ProjectNode asyncInit = new ProjectNode("__init__.py", false, asyncDir);
        asyncInit.setContent("");
        asyncDir.addChild(asyncInit);

        ProjectNode asyncViews = new ProjectNode("views.py", false, asyncDir);
        asyncViews.setContent(generateAsyncViews());
        asyncDir.addChild(asyncViews);

        ProjectNode asyncTasks = new ProjectNode("tasks.py", false, asyncDir);
        asyncTasks.setContent(generateAsyncTasks());
        asyncDir.addChild(asyncTasks);

        ProjectNode asyncWorkers = new ProjectNode("workers.py", false, asyncDir);
        asyncWorkers.setContent(generateAsyncWorkers());
        asyncDir.addChild(asyncWorkers);
    }

    private void createGraphQLStructure(ProjectNode appDir) {
        // GraphQL package
        ProjectNode graphqlDir = new ProjectNode("graphql", true, appDir);
        appDir.addChild(graphqlDir);

        ProjectNode graphqlInit = new ProjectNode("__init__.py", false, graphqlDir);
        graphqlInit.setContent("");
        graphqlDir.addChild(graphqlInit);

        ProjectNode schema = new ProjectNode("schema.py", false, graphqlDir);
        schema.setContent(generateGraphQLSchema());
        graphqlDir.addChild(schema);

        ProjectNode queries = new ProjectNode("queries.py", false, graphqlDir);
        queries.setContent(generateGraphQLQueries());
        graphqlDir.addChild(queries);

        ProjectNode mutations = new ProjectNode("mutations.py", false, graphqlDir);
        mutations.setContent(generateGraphQLMutations());
        graphqlDir.addChild(mutations);

        ProjectNode resolvers = new ProjectNode("resolvers.py", false, graphqlDir);
        resolvers.setContent(generateGraphQLResolvers());
        graphqlDir.addChild(resolvers);
    }

    private void createWebSocketStructure(ProjectNode appDir) {
        // WebSocket package
        ProjectNode wsDir = new ProjectNode("websocket", true, appDir);
        appDir.addChild(wsDir);

        ProjectNode wsInit = new ProjectNode("__init__.py", false, wsDir);
        wsInit.setContent("");
        wsDir.addChild(wsInit);

        ProjectNode events = new ProjectNode("events.py", false, wsDir);
        events.setContent(generateWebSocketEvents());
        wsDir.addChild(events);

        ProjectNode handlers = new ProjectNode("handlers.py", false, wsDir);
        handlers.setContent(generateWebSocketHandlers());
        wsDir.addChild(handlers);

        ProjectNode rooms = new ProjectNode("rooms.py", false, wsDir);
        rooms.setContent(generateWebSocketRooms());
        wsDir.addChild(rooms);
    }

    private void addConfigurationFiles(ProjectNode root, String projectName, FlaskProjectType type) {
        // requirements.txt
        ProjectNode requirements = new ProjectNode("requirements.txt", false, root);
        requirements.setContent(generateRequirements(type));
        root.addChild(requirements);

        // requirements-dev.txt
        ProjectNode requirementsDev = new ProjectNode("requirements-dev.txt", false, root);
        requirementsDev.setContent(generateRequirementsDev());
        root.addChild(requirementsDev);

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

        // setup.py
        ProjectNode setupPy = new ProjectNode("setup.py", false, root);
        setupPy.setContent(generateSetupPy(projectName));
        root.addChild(setupPy);

        // pytest.ini
        ProjectNode pytest = new ProjectNode("pytest.ini", false, root);
        pytest.setContent(generatePytestIni());
        root.addChild(pytest);

        // .flaskenv
        ProjectNode flaskenv = new ProjectNode(".flaskenv", false, root);
        flaskenv.setContent(generateFlaskenv());
        root.addChild(flaskenv);

        // README.md
        ProjectNode readme = new ProjectNode("README.md", false, root);
        readme.setContent(generateReadme(projectName, type));
        root.addChild(readme);

        // Makefile
        ProjectNode makefile = new ProjectNode("Makefile", false, root);
        makefile.setContent(generateMakefile());
        root.addChild(makefile);
    }

    private void addDockerSupport(ProjectNode root, String projectName, FlaskProjectType type) {
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
        ProjectNode nginxConf = new ProjectNode("nginx.conf", false, dockerDir);
        nginxConf.setContent(generateNginxConfig(projectName));
        dockerDir.addChild(nginxConf);

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
    }

    private String generateAppFactory(FlaskProjectType type) {
        return "\"\"\"Application factory for Flask app.\"\"\"\n\n" +
               "from flask import Flask\n" +
               "from flask_cors import CORS\n" +
               "from flask_migrate import Migrate\n" +
               "from flask_jwt_extended import JWTManager\n" +
               "from flask_limiter import Limiter\n" +
               "from flask_limiter.util import get_remote_address\n" +
               "from flask_caching import Cache\n" +
               "from flask_mail import Mail\n" +
               "from flask_marshmallow import Marshmallow\n" +
               "from flask_socketio import SocketIO\n\n" +
               "from app.config import Config\n" +
               "from app.extensions import db, bcrypt, login_manager\n\n" +
               "migrate = Migrate()\n" +
               "jwt = JWTManager()\n" +
               "cors = CORS()\n" +
               "limiter = Limiter(key_func=get_remote_address)\n" +
               "cache = Cache()\n" +
               "mail = Mail()\n" +
               "ma = Marshmallow()\n" +
               "socketio = SocketIO()\n\n" +
               "def create_app(config_class=Config):\n" +
               "    \"\"\"Create and configure the Flask application.\"\"\"\n" +
               "    app = Flask(__name__)\n" +
               "    app.config.from_object(config_class)\n\n" +
               "    # Initialize extensions\n" +
               "    db.init_app(app)\n" +
               "    migrate.init_app(app, db)\n" +
               "    bcrypt.init_app(app)\n" +
               "    login_manager.init_app(app)\n" +
               "    jwt.init_app(app)\n" +
               "    cors.init_app(app)\n" +
               "    limiter.init_app(app)\n" +
               "    cache.init_app(app)\n" +
               "    mail.init_app(app)\n" +
               "    ma.init_app(app)\n" +
               (type == FlaskProjectType.WEBSOCKET_APP ?
                "    socketio.init_app(app, cors_allowed_origins='*')\n" : "") +
               "\n" +
               "    # Register blueprints\n" +
               "    register_blueprints(app)\n\n" +
               "    # Register error handlers\n" +
               "    register_error_handlers(app)\n\n" +
               "    # Register CLI commands\n" +
               "    register_commands(app)\n\n" +
               "    return app\n\n" +
               "def register_blueprints(app):\n" +
               "    \"\"\"Register Flask blueprints.\"\"\"\n" +
               generateBlueprintRegistration(type) +
               "\n\n" +
               "def register_error_handlers(app):\n" +
               "    \"\"\"Register error handlers.\"\"\"\n" +
               "    @app.errorhandler(404)\n" +
               "    def not_found_error(error):\n" +
               "        return {'error': 'Resource not found'}, 404\n\n" +
               "    @app.errorhandler(500)\n" +
               "    def internal_error(error):\n" +
               "        db.session.rollback()\n" +
               "        return {'error': 'Internal server error'}, 500\n\n" +
               "def register_commands(app):\n" +
               "    \"\"\"Register CLI commands.\"\"\"\n" +
               "    @app.cli.command()\n" +
               "    def init_db():\n" +
               "        \"\"\"Initialize the database.\"\"\"\n" +
               "        db.create_all()\n" +
               "        print('Initialized the database.')\n\n" +
               "    @app.cli.command()\n" +
               "    def seed_db():\n" +
               "        \"\"\"Seed the database.\"\"\"\n" +
               "        from scripts.seed_db import seed\n" +
               "        seed()\n" +
               "        print('Seeded the database.')\n";
    }

    private String generateBlueprintRegistration(FlaskProjectType type) {
        switch (type) {
            case REST_API:
                return "    from app.api.v1.routes import api_bp\n" +
                       "    app.register_blueprint(api_bp, url_prefix='/api/v1')";
            case WEB_APP:
                return "    from app.views.main import main_bp\n" +
                       "    from app.views.auth import auth_bp\n" +
                       "    from app.views.admin import admin_bp\n" +
                       "    app.register_blueprint(main_bp)\n" +
                       "    app.register_blueprint(auth_bp, url_prefix='/auth')\n" +
                       "    app.register_blueprint(admin_bp, url_prefix='/admin')";
            case GRAPHQL_API:
                return "    from app.graphql import graphql_bp\n" +
                       "    app.register_blueprint(graphql_bp, url_prefix='/graphql')";
            default:
                return "    from app.views.main import main_bp\n" +
                       "    app.register_blueprint(main_bp)";
        }
    }

    private String generateConfig() {
        return "\"\"\"Application configuration.\"\"\"\n\n" +
               "import os\n" +
               "from datetime import timedelta\n\n" +
               "basedir = os.path.abspath(os.path.dirname(__file__))\n\n" +
               "class Config:\n" +
               "    \"\"\"Base configuration.\"\"\"\n" +
               "    SECRET_KEY = os.environ.get('SECRET_KEY') or 'dev-secret-key-change-in-production'\n" +
               "    SQLALCHEMY_DATABASE_URI = os.environ.get('DATABASE_URL') or \\\n" +
               "        'sqlite:///' + os.path.join(basedir, '..', 'app.db')\n" +
               "    SQLALCHEMY_TRACK_MODIFICATIONS = False\n" +
               "    SQLALCHEMY_ECHO = False\n\n" +
               "    # JWT Configuration\n" +
               "    JWT_SECRET_KEY = os.environ.get('JWT_SECRET_KEY') or SECRET_KEY\n" +
               "    JWT_ACCESS_TOKEN_EXPIRES = timedelta(hours=1)\n" +
               "    JWT_REFRESH_TOKEN_EXPIRES = timedelta(days=30)\n" +
               "    JWT_BLACKLIST_ENABLED = True\n" +
               "    JWT_BLACKLIST_TOKEN_CHECKS = ['access', 'refresh']\n\n" +
               "    # Redis Configuration\n" +
               "    REDIS_URL = os.environ.get('REDIS_URL') or 'redis://localhost:6379/0'\n\n" +
               "    # Cache Configuration\n" +
               "    CACHE_TYPE = 'redis'\n" +
               "    CACHE_REDIS_URL = REDIS_URL\n" +
               "    CACHE_DEFAULT_TIMEOUT = 300\n\n" +
               "    # Mail Configuration\n" +
               "    MAIL_SERVER = os.environ.get('MAIL_SERVER')\n" +
               "    MAIL_PORT = int(os.environ.get('MAIL_PORT') or 587)\n" +
               "    MAIL_USE_TLS = os.environ.get('MAIL_USE_TLS', 'true').lower() in ['true', 'on', '1']\n" +
               "    MAIL_USERNAME = os.environ.get('MAIL_USERNAME')\n" +
               "    MAIL_PASSWORD = os.environ.get('MAIL_PASSWORD')\n" +
               "    MAIL_DEFAULT_SENDER = os.environ.get('MAIL_DEFAULT_SENDER')\n\n" +
               "    # Celery Configuration\n" +
               "    CELERY_BROKER_URL = REDIS_URL\n" +
               "    CELERY_RESULT_BACKEND = REDIS_URL\n\n" +
               "    # Rate Limiting\n" +
               "    RATELIMIT_STORAGE_URL = REDIS_URL\n\n" +
               "    # Pagination\n" +
               "    ITEMS_PER_PAGE = 20\n\n" +
               "    # File Upload\n" +
               "    MAX_CONTENT_LENGTH = 16 * 1024 * 1024  # 16MB\n" +
               "    UPLOAD_FOLDER = os.path.join(basedir, '..', 'uploads')\n" +
               "    ALLOWED_EXTENSIONS = {'txt', 'pdf', 'png', 'jpg', 'jpeg', 'gif'}\n\n" +
               "class DevelopmentConfig(Config):\n" +
               "    \"\"\"Development configuration.\"\"\"\n" +
               "    DEBUG = True\n" +
               "    SQLALCHEMY_ECHO = True\n\n" +
               "class TestingConfig(Config):\n" +
               "    \"\"\"Testing configuration.\"\"\"\n" +
               "    TESTING = True\n" +
               "    SQLALCHEMY_DATABASE_URI = 'sqlite:///:memory:'\n" +
               "    WTF_CSRF_ENABLED = False\n\n" +
               "class ProductionConfig(Config):\n" +
               "    \"\"\"Production configuration.\"\"\"\n" +
               "    DEBUG = False\n\n" +
               "config = {\n" +
               "    'development': DevelopmentConfig,\n" +
               "    'testing': TestingConfig,\n" +
               "    'production': ProductionConfig,\n" +
               "    'default': DevelopmentConfig\n" +
               "}\n";
    }

    private String generateBaseModel() {
        return "\"\"\"Base model configuration.\"\"\"\n\n" +
               "from flask_sqlalchemy import SQLAlchemy\n" +
               "from datetime import datetime\n\n" +
               "db = SQLAlchemy()\n\n" +
               "class BaseModel(db.Model):\n" +
               "    \"\"\"Abstract base model with common fields.\"\"\"\n" +
               "    __abstract__ = True\n\n" +
               "    id = db.Column(db.Integer, primary_key=True)\n" +
               "    created_at = db.Column(db.DateTime, default=datetime.utcnow, nullable=False)\n" +
               "    updated_at = db.Column(db.DateTime, default=datetime.utcnow, onupdate=datetime.utcnow, nullable=False)\n\n" +
               "    def save(self):\n" +
               "        \"\"\"Save the model to the database.\"\"\"\n" +
               "        db.session.add(self)\n" +
               "        db.session.commit()\n\n" +
               "    def delete(self):\n" +
               "        \"\"\"Delete the model from the database.\"\"\"\n" +
               "        db.session.delete(self)\n" +
               "        db.session.commit()\n\n" +
               "    def to_dict(self):\n" +
               "        \"\"\"Convert model to dictionary.\"\"\"\n" +
               "        return {c.name: getattr(self, c.name) for c in self.__table__.columns}\n";
    }

    private String generateUserModel() {
        return "\"\"\"User model.\"\"\"\n\n" +
               "from werkzeug.security import generate_password_hash, check_password_hash\n" +
               "from flask_login import UserMixin\n" +
               "from app.models.base import db, BaseModel\n\n" +
               "class User(BaseModel, UserMixin):\n" +
               "    \"\"\"User model.\"\"\"\n" +
               "    __tablename__ = 'users'\n\n" +
               "    username = db.Column(db.String(80), unique=True, nullable=False)\n" +
               "    email = db.Column(db.String(120), unique=True, nullable=False)\n" +
               "    password_hash = db.Column(db.String(255), nullable=False)\n" +
               "    first_name = db.Column(db.String(50))\n" +
               "    last_name = db.Column(db.String(50))\n" +
               "    is_active = db.Column(db.Boolean, default=True)\n" +
               "    is_verified = db.Column(db.Boolean, default=False)\n" +
               "    role_id = db.Column(db.Integer, db.ForeignKey('roles.id'))\n\n" +
               "    # Relationships\n" +
               "    role = db.relationship('Role', backref='users')\n\n" +
               "    def set_password(self, password):\n" +
               "        \"\"\"Set password hash.\"\"\"\n" +
               "        self.password_hash = generate_password_hash(password)\n\n" +
               "    def check_password(self, password):\n" +
               "        \"\"\"Check password against hash.\"\"\"\n" +
               "        return check_password_hash(self.password_hash, password)\n\n" +
               "    def __repr__(self):\n" +
               "        return f'<User {self.username}>'\n";
    }

    private String generateRoleModel() {
        return "\"\"\"Role model for user permissions.\"\"\"\n\n" +
               "from app.models.base import db, BaseModel\n\n" +
               "class Role(BaseModel):\n" +
               "    \"\"\"Role model.\"\"\"\n" +
               "    __tablename__ = 'roles'\n\n" +
               "    name = db.Column(db.String(50), unique=True, nullable=False)\n" +
               "    description = db.Column(db.String(200))\n" +
               "    permissions = db.Column(db.JSON, default=list)\n\n" +
               "    def __repr__(self):\n" +
               "        return f'<Role {self.name}>'\n";
    }

    private String generateExtensions(FlaskProjectType type) {
        return "\"\"\"Flask extensions initialization.\"\"\"\n\n" +
               "from flask_sqlalchemy import SQLAlchemy\n" +
               "from flask_bcrypt import Bcrypt\n" +
               "from flask_login import LoginManager\n\n" +
               "db = SQLAlchemy()\n" +
               "bcrypt = Bcrypt()\n" +
               "login_manager = LoginManager()\n\n" +
               "@login_manager.user_loader\n" +
               "def load_user(user_id):\n" +
               "    from app.models.user import User\n" +
               "    return User.query.get(int(user_id))\n";
    }

    private String generateRequirements(FlaskProjectType type) {
        List<String> deps = new ArrayList<>();

        // Core Flask dependencies
        deps.add("Flask==3.0.0");
        deps.add("Flask-SQLAlchemy==3.1.1");
        deps.add("Flask-Migrate==4.0.5");
        deps.add("Flask-Login==0.6.3");
        deps.add("Flask-WTF==1.2.1");
        deps.add("Flask-Bcrypt==1.0.1");
        deps.add("Flask-JWT-Extended==4.5.3");
        deps.add("Flask-CORS==4.0.0");
        deps.add("Flask-Limiter==3.5.0");
        deps.add("Flask-Caching==2.1.0");
        deps.add("Flask-Mail==0.9.1");
        deps.add("Flask-Marshmallow==0.15.0");

        // Database
        deps.add("SQLAlchemy==2.0.23");
        deps.add("psycopg2-binary==2.9.9");
        deps.add("alembic==1.13.1");

        // Serialization
        deps.add("marshmallow==3.20.1");
        deps.add("marshmallow-sqlalchemy==0.29.0");

        // Forms
        deps.add("WTForms==3.1.1");
        deps.add("email-validator==2.1.0");

        // Caching and background tasks
        deps.add("redis==5.0.1");
        deps.add("celery==5.3.4");

        // Utilities
        deps.add("python-dotenv==1.0.0");
        deps.add("requests==2.31.0");
        deps.add("python-dateutil==2.8.2");

        // Add type-specific dependencies
        switch (type) {
            case REST_API:
                deps.add("Flask-RESTful==0.3.10");
                deps.add("flasgger==0.9.7.1");
                deps.add("apispec==6.3.1");
                break;
            case ASYNC_APP:
                deps.add("Quart==0.19.4");
                deps.add("hypercorn==0.15.0");
                deps.add("aioredis==2.0.1");
                break;
            case GRAPHQL_API:
                deps.add("graphene==3.3");
                deps.add("graphene-sqlalchemy==3.0.0b4");
                deps.add("Flask-GraphQL==2.0.1");
                break;
            case WEBSOCKET_APP:
                deps.add("Flask-SocketIO==5.3.5");
                deps.add("python-socketio==5.10.0");
                deps.add("eventlet==0.33.3");
                break;
            case DATA_API:
                deps.add("pandas==2.1.4");
                deps.add("numpy==1.26.2");
                deps.add("scikit-learn==1.3.2");
                deps.add("matplotlib==3.8.2");
                break;
        }

        // Server
        deps.add("gunicorn==21.2.0");
        deps.add("gevent==23.9.1");

        // Testing
        deps.add("pytest==7.4.3");
        deps.add("pytest-flask==1.3.0");
        deps.add("pytest-cov==4.1.0");
        deps.add("factory-boy==3.3.0");
        deps.add("faker==20.1.0");

        return String.join("\n", deps);
    }

    private String generateWSGI() {
        return "\"\"\"WSGI entry point.\"\"\"\n\n" +
               "from app import create_app\n\n" +
               "app = create_app()\n\n" +
               "if __name__ == '__main__':\n" +
               "    app.run()\n";
    }

    private String generateRunApp() {
        return "\"\"\"Development server entry point.\"\"\"\n\n" +
               "import os\n" +
               "from app import create_app\n\n" +
               "app = create_app()\n\n" +
               "if __name__ == '__main__':\n" +
               "    port = int(os.environ.get('PORT', 5000))\n" +
               "    app.run(host='0.0.0.0', port=port, debug=True)\n";
    }

    // Stub implementations for remaining helper methods
    private String generateValidators() { return "# Custom validators"; }
    private String generateDecorators() { return "# Custom decorators"; }
    private String generateHelpers() { return "# Helper functions"; }
    private String generateMainCSS() { return "/* Main CSS styles */"; }
    private String generateMainJS(FlaskProjectType type) { return "// Main JavaScript"; }
    private String generateBaseTemplate() { return "<!-- Base HTML template -->"; }
    private String generateIndexTemplate() { return "<!-- Index HTML template -->"; }
    private String generate404Template() { return "<!-- 404 error template -->"; }
    private String generate500Template() { return "<!-- 500 error template -->"; }
    private String generateConftest() { return "# Pytest configuration"; }
    private String generateTestModels() { return "# Model tests"; }
    private String generateTestAPI() { return "# API tests"; }
    private String generateInitDbScript() { return "# Database initialization script"; }
    private String generateSeedDbScript() { return "# Database seeding script"; }
    private String generateAPIDocs(FlaskProjectType type) { return "# API Documentation"; }
    private String generateUserResource() { return "# User resource implementation"; }
    private String generateAuthResource() { return "# Auth resource implementation"; }
    private String generateUserSchema() { return "# User schema"; }
    private String generateAuthSchema() { return "# Auth schema"; }
    private String generateAPIRoutes() { return "# API routes"; }
    private String generateMiddleware() { return "# Middleware implementation"; }
    private String generateMainViews() { return "# Main views"; }
    private String generateAuthViews() { return "# Auth views"; }
    private String generateAdminViews() { return "# Admin views"; }
    private String generateAuthForms() { return "# Auth forms"; }
    private String generateUserForms() { return "# User forms"; }
    private String generateDataService() { return "# Data service"; }
    private String generateCacheService() { return "# Cache service"; }
    private String generateMessageService() { return "# Message service"; }
    private String generateEventHandlers() { return "# Event handlers"; }
    private String generateErrorHandlers() { return "# Error handlers"; }
    private String generateDataProcessors() { return "# Data processors"; }
    private String generateDataTransformers() { return "# Data transformers"; }
    private String generateDataAnalyzers() { return "# Data analyzers"; }
    private String generateMLModels() { return "# ML models"; }
    private String generatePredictors() { return "# Predictors"; }
    private String generateAsyncViews() { return "# Async views"; }
    private String generateAsyncTasks() { return "# Async tasks"; }
    private String generateAsyncWorkers() { return "# Async workers"; }
    private String generateGraphQLSchema() { return "# GraphQL schema"; }
    private String generateGraphQLQueries() { return "# GraphQL queries"; }
    private String generateGraphQLMutations() { return "# GraphQL mutations"; }
    private String generateGraphQLResolvers() { return "# GraphQL resolvers"; }
    private String generateWebSocketEvents() { return "# WebSocket events"; }
    private String generateWebSocketHandlers() { return "# WebSocket handlers"; }
    private String generateWebSocketRooms() { return "# WebSocket rooms"; }
    private String generateRequirementsDev() { return "# Development requirements"; }
    private String generateEnvExample(FlaskProjectType type) { return "# Environment variables"; }
    private String generateGitignore() { return "*.pyc\n__pycache__/\ninstance/\n.env"; }
    private String generatePyprojectToml(String projectName) { return "[tool.black]\nline-length = 88"; }
    private String generateSetupCfg() { return "[flake8]\nmax-line-length = 88"; }
    private String generateSetupPy(String projectName) { return "from setuptools import setup\n\nsetup(name='" + projectName + "')"; }
    private String generatePytestIni() { return "[pytest]\ntestpaths = tests"; }
    private String generateFlaskenv() { return "FLASK_APP=wsgi.py\nFLASK_ENV=development"; }
    private String generateReadme(String projectName, FlaskProjectType type) { return "# " + projectName; }
    private String generateMakefile() { return "run:\n\tflask run"; }
    private String generateDockerfile(FlaskProjectType type) { return "FROM python:3.11-slim\nWORKDIR /app"; }
    private String generateDockerCompose(String projectName, FlaskProjectType type) { return "version: '3.9'"; }
    private String generateDockerignore() { return "*.pyc\n__pycache__/\n.git/"; }
    private String generateNginxConfig(String projectName) { return "# Nginx configuration"; }
    private String generateEntrypoint() { return "#!/bin/bash\n# Docker entrypoint"; }
    private String generateCIWorkflow(String projectName) { return "# CI workflow"; }
    private String generateDeployWorkflow(String projectName) { return "# Deploy workflow"; }
}