package com.structurecreation.service.generator;

import com.structurecreation.model.ProjectNode;
import com.structurecreation.service.DependencyResolverService;

import java.util.ArrayList;
import java.util.List;

public class FlaskProjectGenerator {

    private final DependencyResolverService dependencyResolver;

    public FlaskProjectGenerator(DependencyResolverService dependencyResolver) {
        this.dependencyResolver = dependencyResolver;
    }

    public FlaskProjectGenerator() {
        this.dependencyResolver = new DependencyResolverService();
    }

    public enum FlaskProjectType {
        BASIC("Basic Flask Application"),
        REST_API("RESTful API with Flask-RESTful"),
        WEB_APP("Traditional web application"),
        MICROSERVICE("Lightweight microservice"),
        DATA_API("Data science API"),
        ML_API("Machine Learning API"),
        ASYNC_APP("Async application with Quart"),
        GRAPHQL_API("GraphQL API"),
        WEBSOCKET_APP("Real-time WebSocket application"),
        FULL_STACK("Full-Stack Flask Application");

        private final String description;

        FlaskProjectType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    public ProjectNode generateFlaskProject(String projectName, FlaskProjectType type) {
        ProjectNode root = new ProjectNode(projectName, ProjectNode.NodeType.FOLDER);

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
        ProjectNode appDir = new ProjectNode("app", ProjectNode.NodeType.FOLDER);
        root.addChild(appDir);

        // __init__.py with app factory
        ProjectNode appInit = new ProjectNode("__init__.py", ProjectNode.NodeType.FILE);
        appInit.setContent(generateAppFactory(type));
        appDir.addChild(appInit);

        // Config module
        ProjectNode config = new ProjectNode("config.py", ProjectNode.NodeType.FILE);
        config.setContent(generateConfig());
        appDir.addChild(config);

        // Models directory
        ProjectNode modelsDir = new ProjectNode("models", ProjectNode.NodeType.FOLDER);
        appDir.addChild(modelsDir);

        ProjectNode modelsInit = new ProjectNode("__init__.py", ProjectNode.NodeType.FILE);
        modelsInit.setContent("from .base import db\nfrom .user import User\nfrom .role import Role");
        modelsDir.addChild(modelsInit);

        ProjectNode baseModel = new ProjectNode("base.py", ProjectNode.NodeType.FILE);
        baseModel.setContent(generateBaseModel());
        modelsDir.addChild(baseModel);

        ProjectNode userModel = new ProjectNode("user.py", ProjectNode.NodeType.FILE);
        userModel.setContent(generateUserModel());
        modelsDir.addChild(userModel);

        ProjectNode roleModel = new ProjectNode("role.py", ProjectNode.NodeType.FILE);
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
        ProjectNode utilsDir = new ProjectNode("utils", ProjectNode.NodeType.FOLDER);
        appDir.addChild(utilsDir);

        ProjectNode utilsInit = new ProjectNode("__init__.py", ProjectNode.NodeType.FILE);
        utilsInit.setContent("");
        utilsDir.addChild(utilsInit);

        ProjectNode validators = new ProjectNode("validators.py", ProjectNode.NodeType.FILE);
        validators.setContent(generateValidators());
        utilsDir.addChild(validators);

        ProjectNode decorators = new ProjectNode("decorators.py", ProjectNode.NodeType.FILE);
        decorators.setContent(generateDecorators());
        utilsDir.addChild(decorators);

        ProjectNode helpers = new ProjectNode("helpers.py", ProjectNode.NodeType.FILE);
        helpers.setContent(generateHelpers());
        utilsDir.addChild(helpers);

        // Extensions
        ProjectNode extensions = new ProjectNode("extensions.py", ProjectNode.NodeType.FILE);
        extensions.setContent(generateExtensions(type));
        appDir.addChild(extensions);

        // Static files
        ProjectNode staticDir = new ProjectNode("static", ProjectNode.NodeType.FOLDER);
        root.addChild(staticDir);

        ProjectNode cssDir = new ProjectNode("css", ProjectNode.NodeType.FOLDER);
        staticDir.addChild(cssDir);
        ProjectNode mainCss = new ProjectNode("style.css", ProjectNode.NodeType.FILE);
        mainCss.setContent(generateMainCSS());
        cssDir.addChild(mainCss);

        ProjectNode jsDir = new ProjectNode("js", ProjectNode.NodeType.FOLDER);
        staticDir.addChild(jsDir);
        ProjectNode mainJs = new ProjectNode("app.js", ProjectNode.NodeType.FILE);
        mainJs.setContent(generateMainJS(type));
        jsDir.addChild(mainJs);

        ProjectNode imgDir = new ProjectNode("img", ProjectNode.NodeType.FOLDER);
        staticDir.addChild(imgDir);

        // Templates
        ProjectNode templatesDir = new ProjectNode("templates", ProjectNode.NodeType.FOLDER);
        root.addChild(templatesDir);

        ProjectNode baseTemplate = new ProjectNode("base.html", ProjectNode.NodeType.FILE);
        baseTemplate.setContent(generateBaseTemplate());
        templatesDir.addChild(baseTemplate);

        ProjectNode indexTemplate = new ProjectNode("index.html", ProjectNode.NodeType.FILE);
        indexTemplate.setContent(generateIndexTemplate());
        templatesDir.addChild(indexTemplate);

        ProjectNode errorDir = new ProjectNode("errors", ProjectNode.NodeType.FOLDER);
        templatesDir.addChild(errorDir);

        ProjectNode error404 = new ProjectNode("404.html", ProjectNode.NodeType.FILE);
        error404.setContent(generate404Template());
        errorDir.addChild(error404);

        ProjectNode error500 = new ProjectNode("500.html", ProjectNode.NodeType.FILE);
        error500.setContent(generate500Template());
        errorDir.addChild(error500);

        // Tests directory
        ProjectNode testsDir = new ProjectNode("tests", ProjectNode.NodeType.FOLDER);
        root.addChild(testsDir);

        ProjectNode testsInit = new ProjectNode("__init__.py", ProjectNode.NodeType.FILE);
        testsInit.setContent("");
        testsDir.addChild(testsInit);

        ProjectNode conftest = new ProjectNode("conftest.py", ProjectNode.NodeType.FILE);
        conftest.setContent(generateConftest());
        testsDir.addChild(conftest);

        ProjectNode testModels = new ProjectNode("test_models.py", ProjectNode.NodeType.FILE);
        testModels.setContent(generateTestModels());
        testsDir.addChild(testModels);

        ProjectNode testAPI = new ProjectNode("test_api.py", ProjectNode.NodeType.FILE);
        testAPI.setContent(generateTestAPI());
        testsDir.addChild(testAPI);

        // Migrations directory
        ProjectNode migrationsDir = new ProjectNode("migrations", ProjectNode.NodeType.FOLDER);
        root.addChild(migrationsDir);

        // Instance directory for config
        ProjectNode instanceDir = new ProjectNode("instance", ProjectNode.NodeType.FOLDER);
        root.addChild(instanceDir);

        // Logs directory
        ProjectNode logsDir = new ProjectNode("logs", ProjectNode.NodeType.FOLDER);
        root.addChild(logsDir);

        // Scripts directory
        ProjectNode scriptsDir = new ProjectNode("scripts", ProjectNode.NodeType.FOLDER);
        root.addChild(scriptsDir);

        ProjectNode initDb = new ProjectNode("init_db.py", ProjectNode.NodeType.FILE);
        initDb.setContent(generateInitDbScript());
        scriptsDir.addChild(initDb);

        ProjectNode seedDb = new ProjectNode("seed_db.py", ProjectNode.NodeType.FILE);
        seedDb.setContent(generateSeedDbScript());
        scriptsDir.addChild(seedDb);

        // Documentation
        ProjectNode docsDir = new ProjectNode("docs", ProjectNode.NodeType.FOLDER);
        root.addChild(docsDir);

        ProjectNode apiDocs = new ProjectNode("api.md", ProjectNode.NodeType.FILE);
        apiDocs.setContent(generateAPIDocs(type));
        docsDir.addChild(apiDocs);

        // Main application entry point
        ProjectNode wsgi = new ProjectNode("wsgi.py", ProjectNode.NodeType.FILE);
        wsgi.setContent(generateWSGI());
        root.addChild(wsgi);

        ProjectNode runApp = new ProjectNode("run.py", ProjectNode.NodeType.FILE);
        runApp.setContent(generateRunApp());
        root.addChild(runApp);
    }

    private void createRESTAPIStructure(ProjectNode appDir) {
        // API package
        ProjectNode apiDir = new ProjectNode("api", ProjectNode.NodeType.FOLDER);
        appDir.addChild(apiDir);

        ProjectNode apiInit = new ProjectNode("__init__.py", ProjectNode.NodeType.FILE);
        apiInit.setContent("");
        apiDir.addChild(apiInit);

        // API v1
        ProjectNode v1Dir = new ProjectNode("v1", ProjectNode.NodeType.FOLDER);
        apiDir.addChild(v1Dir);

        ProjectNode v1Init = new ProjectNode("__init__.py", ProjectNode.NodeType.FILE);
        v1Init.setContent("");
        v1Dir.addChild(v1Init);

        // Resources
        ProjectNode resourcesDir = new ProjectNode("resources", ProjectNode.NodeType.FOLDER);
        v1Dir.addChild(resourcesDir);

        ProjectNode resourcesInit = new ProjectNode("__init__.py", ProjectNode.NodeType.FILE);
        resourcesInit.setContent("");
        resourcesDir.addChild(resourcesInit);

        ProjectNode userResource = new ProjectNode("user.py", ProjectNode.NodeType.FILE);
        userResource.setContent(generateUserResource());
        resourcesDir.addChild(userResource);

        ProjectNode authResource = new ProjectNode("auth.py", ProjectNode.NodeType.FILE);
        authResource.setContent(generateAuthResource());
        resourcesDir.addChild(authResource);

        // Schemas
        ProjectNode schemasDir = new ProjectNode("schemas", ProjectNode.NodeType.FOLDER);
        v1Dir.addChild(schemasDir);

        ProjectNode schemasInit = new ProjectNode("__init__.py", ProjectNode.NodeType.FILE);
        schemasInit.setContent("");
        schemasDir.addChild(schemasInit);

        ProjectNode userSchema = new ProjectNode("user_schema.py", ProjectNode.NodeType.FILE);
        userSchema.setContent(generateUserSchema());
        schemasDir.addChild(userSchema);

        ProjectNode authSchema = new ProjectNode("auth_schema.py", ProjectNode.NodeType.FILE);
        authSchema.setContent(generateAuthSchema());
        schemasDir.addChild(authSchema);

        // Routes
        ProjectNode routes = new ProjectNode("routes.py", ProjectNode.NodeType.FILE);
        routes.setContent(generateAPIRoutes());
        v1Dir.addChild(routes);

        // Middleware
        ProjectNode middleware = new ProjectNode("middleware.py", ProjectNode.NodeType.FILE);
        middleware.setContent(generateMiddleware());
        apiDir.addChild(middleware);
    }

    private void createWebAppStructure(ProjectNode appDir) {
        // Views package
        ProjectNode viewsDir = new ProjectNode("views", ProjectNode.NodeType.FOLDER);
        appDir.addChild(viewsDir);

        ProjectNode viewsInit = new ProjectNode("__init__.py", ProjectNode.NodeType.FILE);
        viewsInit.setContent("");
        viewsDir.addChild(viewsInit);

        ProjectNode mainViews = new ProjectNode("main.py", ProjectNode.NodeType.FILE);
        mainViews.setContent(generateMainViews());
        viewsDir.addChild(mainViews);

        ProjectNode authViews = new ProjectNode("auth.py", ProjectNode.NodeType.FILE);
        authViews.setContent(generateAuthViews());
        viewsDir.addChild(authViews);

        ProjectNode adminViews = new ProjectNode("admin.py", ProjectNode.NodeType.FILE);
        adminViews.setContent(generateAdminViews());
        viewsDir.addChild(adminViews);

        // Forms package
        ProjectNode formsDir = new ProjectNode("forms", ProjectNode.NodeType.FOLDER);
        appDir.addChild(formsDir);

        ProjectNode formsInit = new ProjectNode("__init__.py", ProjectNode.NodeType.FILE);
        formsInit.setContent("");
        formsDir.addChild(formsInit);

        ProjectNode loginForm = new ProjectNode("auth_forms.py", ProjectNode.NodeType.FILE);
        loginForm.setContent(generateAuthForms());
        formsDir.addChild(loginForm);

        ProjectNode userForm = new ProjectNode("user_forms.py", ProjectNode.NodeType.FILE);
        userForm.setContent(generateUserForms());
        formsDir.addChild(userForm);
    }

    private void createMicroserviceStructure(ProjectNode appDir) {
        // Services package
        ProjectNode servicesDir = new ProjectNode("services", ProjectNode.NodeType.FOLDER);
        appDir.addChild(servicesDir);

        ProjectNode servicesInit = new ProjectNode("__init__.py", ProjectNode.NodeType.FILE);
        servicesInit.setContent("");
        servicesDir.addChild(servicesInit);

        ProjectNode dataService = new ProjectNode("data_service.py", ProjectNode.NodeType.FILE);
        dataService.setContent(generateDataService());
        servicesDir.addChild(dataService);

        ProjectNode cacheService = new ProjectNode("cache_service.py", ProjectNode.NodeType.FILE);
        cacheService.setContent(generateCacheService());
        servicesDir.addChild(cacheService);

        ProjectNode messageService = new ProjectNode("message_service.py", ProjectNode.NodeType.FILE);
        messageService.setContent(generateMessageService());
        servicesDir.addChild(messageService);

        // Handlers
        ProjectNode handlersDir = new ProjectNode("handlers", ProjectNode.NodeType.FOLDER);
        appDir.addChild(handlersDir);

        ProjectNode handlersInit = new ProjectNode("__init__.py", ProjectNode.NodeType.FILE);
        handlersInit.setContent("");
        handlersDir.addChild(handlersInit);

        ProjectNode eventHandlers = new ProjectNode("event_handlers.py", ProjectNode.NodeType.FILE);
        eventHandlers.setContent(generateEventHandlers());
        handlersDir.addChild(eventHandlers);

        ProjectNode errorHandlers = new ProjectNode("error_handlers.py", ProjectNode.NodeType.FILE);
        errorHandlers.setContent(generateErrorHandlers());
        handlersDir.addChild(errorHandlers);
    }

    private void createDataAPIStructure(ProjectNode appDir) {
        // Data processing package
        ProjectNode dataDir = new ProjectNode("data", ProjectNode.NodeType.FOLDER);
        appDir.addChild(dataDir);

        ProjectNode dataInit = new ProjectNode("__init__.py", ProjectNode.NodeType.FILE);
        dataInit.setContent("");
        dataDir.addChild(dataInit);

        ProjectNode processors = new ProjectNode("processors.py", ProjectNode.NodeType.FILE);
        processors.setContent(generateDataProcessors());
        dataDir.addChild(processors);

        ProjectNode transformers = new ProjectNode("transformers.py", ProjectNode.NodeType.FILE);
        transformers.setContent(generateDataTransformers());
        dataDir.addChild(transformers);

        ProjectNode analyzers = new ProjectNode("analyzers.py", ProjectNode.NodeType.FILE);
        analyzers.setContent(generateDataAnalyzers());
        dataDir.addChild(analyzers);

        // ML package
        ProjectNode mlDir = new ProjectNode("ml", ProjectNode.NodeType.FOLDER);
        appDir.addChild(mlDir);

        ProjectNode mlInit = new ProjectNode("__init__.py", ProjectNode.NodeType.FILE);
        mlInit.setContent("");
        mlDir.addChild(mlInit);

        ProjectNode models = new ProjectNode("models.py", ProjectNode.NodeType.FILE);
        models.setContent(generateMLModels());
        mlDir.addChild(models);

        ProjectNode predictors = new ProjectNode("predictors.py", ProjectNode.NodeType.FILE);
        predictors.setContent(generatePredictors());
        mlDir.addChild(predictors);
    }

    private void createAsyncStructure(ProjectNode appDir) {
        // Async handlers
        ProjectNode asyncDir = new ProjectNode("async_handlers", ProjectNode.NodeType.FOLDER);
        appDir.addChild(asyncDir);

        ProjectNode asyncInit = new ProjectNode("__init__.py", ProjectNode.NodeType.FILE);
        asyncInit.setContent("");
        asyncDir.addChild(asyncInit);

        ProjectNode asyncViews = new ProjectNode("views.py", ProjectNode.NodeType.FILE);
        asyncViews.setContent(generateAsyncViews());
        asyncDir.addChild(asyncViews);

        ProjectNode asyncTasks = new ProjectNode("tasks.py", ProjectNode.NodeType.FILE);
        asyncTasks.setContent(generateAsyncTasks());
        asyncDir.addChild(asyncTasks);

        ProjectNode asyncWorkers = new ProjectNode("workers.py", ProjectNode.NodeType.FILE);
        asyncWorkers.setContent(generateAsyncWorkers());
        asyncDir.addChild(asyncWorkers);
    }

    private void createGraphQLStructure(ProjectNode appDir) {
        // GraphQL package
        ProjectNode graphqlDir = new ProjectNode("graphql", ProjectNode.NodeType.FOLDER);
        appDir.addChild(graphqlDir);

        ProjectNode graphqlInit = new ProjectNode("__init__.py", ProjectNode.NodeType.FILE);
        graphqlInit.setContent("");
        graphqlDir.addChild(graphqlInit);

        ProjectNode schema = new ProjectNode("schema.py", ProjectNode.NodeType.FILE);
        schema.setContent(generateGraphQLSchema());
        graphqlDir.addChild(schema);

        ProjectNode queries = new ProjectNode("queries.py", ProjectNode.NodeType.FILE);
        queries.setContent(generateGraphQLQueries());
        graphqlDir.addChild(queries);

        ProjectNode mutations = new ProjectNode("mutations.py", ProjectNode.NodeType.FILE);
        mutations.setContent(generateGraphQLMutations());
        graphqlDir.addChild(mutations);

        ProjectNode resolvers = new ProjectNode("resolvers.py", ProjectNode.NodeType.FILE);
        resolvers.setContent(generateGraphQLResolvers());
        graphqlDir.addChild(resolvers);
    }

    private void createWebSocketStructure(ProjectNode appDir) {
        // WebSocket package
        ProjectNode wsDir = new ProjectNode("websocket", ProjectNode.NodeType.FOLDER);
        appDir.addChild(wsDir);

        ProjectNode wsInit = new ProjectNode("__init__.py", ProjectNode.NodeType.FILE);
        wsInit.setContent("");
        wsDir.addChild(wsInit);

        ProjectNode events = new ProjectNode("events.py", ProjectNode.NodeType.FILE);
        events.setContent(generateWebSocketEvents());
        wsDir.addChild(events);

        ProjectNode handlers = new ProjectNode("handlers.py", ProjectNode.NodeType.FILE);
        handlers.setContent(generateWebSocketHandlers());
        wsDir.addChild(handlers);

        ProjectNode rooms = new ProjectNode("rooms.py", ProjectNode.NodeType.FILE);
        rooms.setContent(generateWebSocketRooms());
        wsDir.addChild(rooms);
    }

    private void addConfigurationFiles(ProjectNode root, String projectName, FlaskProjectType type) {
        // requirements.txt
        ProjectNode requirements = new ProjectNode("requirements.txt", ProjectNode.NodeType.FILE);
        requirements.setContent(generateRequirements(type));
        root.addChild(requirements);

        // requirements-dev.txt
        ProjectNode requirementsDev = new ProjectNode("requirements-dev.txt", ProjectNode.NodeType.FILE);
        requirementsDev.setContent(generateRequirementsDev());
        root.addChild(requirementsDev);

        // .env.example
        ProjectNode envExample = new ProjectNode(".env.example", ProjectNode.NodeType.FILE);
        envExample.setContent(generateEnvExample(type));
        root.addChild(envExample);

        // .gitignore
        ProjectNode gitignore = new ProjectNode(".gitignore", ProjectNode.NodeType.FILE);
        gitignore.setContent(generateGitignore());
        root.addChild(gitignore);

        // pyproject.toml
        ProjectNode pyproject = new ProjectNode("pyproject.toml", ProjectNode.NodeType.FILE);
        pyproject.setContent(generatePyprojectToml(projectName));
        root.addChild(pyproject);

        // setup.cfg
        ProjectNode setupCfg = new ProjectNode("setup.cfg", ProjectNode.NodeType.FILE);
        setupCfg.setContent(generateSetupCfg());
        root.addChild(setupCfg);

        // setup.py
        ProjectNode setupPy = new ProjectNode("setup.py", ProjectNode.NodeType.FILE);
        setupPy.setContent(generateSetupPy(projectName));
        root.addChild(setupPy);

        // pytest.ini
        ProjectNode pytest = new ProjectNode("pytest.ini", ProjectNode.NodeType.FILE);
        pytest.setContent(generatePytestIni());
        root.addChild(pytest);

        // .flaskenv
        ProjectNode flaskenv = new ProjectNode(".flaskenv", ProjectNode.NodeType.FILE);
        flaskenv.setContent(generateFlaskenv());
        root.addChild(flaskenv);

        // README.md
        ProjectNode readme = new ProjectNode("README.md", ProjectNode.NodeType.FILE);
        readme.setContent(generateReadme(projectName, type));
        root.addChild(readme);

        // Makefile
        ProjectNode makefile = new ProjectNode("Makefile", ProjectNode.NodeType.FILE);
        makefile.setContent(generateMakefile());
        root.addChild(makefile);
    }

    private void addDockerSupport(ProjectNode root, String projectName, FlaskProjectType type) {
        // Dockerfile
        ProjectNode dockerfile = new ProjectNode("Dockerfile", ProjectNode.NodeType.FILE);
        dockerfile.setContent(generateDockerfile(type));
        root.addChild(dockerfile);

        // docker-compose.yml
        ProjectNode dockerCompose = new ProjectNode("docker-compose.yml", ProjectNode.NodeType.FILE);
        dockerCompose.setContent(generateDockerCompose(projectName, type));
        root.addChild(dockerCompose);

        // .dockerignore
        ProjectNode dockerignore = new ProjectNode(".dockerignore", ProjectNode.NodeType.FILE);
        dockerignore.setContent(generateDockerignore());
        root.addChild(dockerignore);

        // docker directory
        ProjectNode dockerDir = new ProjectNode("docker", ProjectNode.NodeType.FOLDER);
        root.addChild(dockerDir);

        // nginx config
        ProjectNode nginxConf = new ProjectNode("nginx.conf", ProjectNode.NodeType.FILE);
        nginxConf.setContent(generateNginxConfig(projectName));
        dockerDir.addChild(nginxConf);

        // entrypoint script
        ProjectNode entrypoint = new ProjectNode("entrypoint.sh", ProjectNode.NodeType.FILE);
        entrypoint.setContent(generateEntrypoint());
        dockerDir.addChild(entrypoint);
    }

    private void addCICDPipeline(ProjectNode root, String projectName) {
        // .github directory
        ProjectNode githubDir = new ProjectNode(".github", ProjectNode.NodeType.FOLDER);
        root.addChild(githubDir);

        // workflows directory
        ProjectNode workflowsDir = new ProjectNode("workflows", ProjectNode.NodeType.FOLDER);
        githubDir.addChild(workflowsDir);

        // CI workflow
        ProjectNode ciWorkflow = new ProjectNode("ci.yml", ProjectNode.NodeType.FILE);
        ciWorkflow.setContent(generateCIWorkflow(projectName));
        workflowsDir.addChild(ciWorkflow);

        // Deploy workflow
        ProjectNode deployWorkflow = new ProjectNode("deploy.yml", ProjectNode.NodeType.FILE);
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

    private String generateValidators() {
        return "\"\"\"Custom validators for Flask forms and data.\"\"\"\n\n" +
               "import re\n" +
               "from wtforms.validators import ValidationError\n\n" +
               "def validate_username(form, field):\n" +
               "    \"\"\"Validate username format.\"\"\"\n" +
               "    if not re.match(r'^[a-zA-Z0-9_]{3,20}$', field.data):\n" +
               "        raise ValidationError(\n" +
               "            'Username must be 3-20 characters and contain only letters, numbers, and underscores.'\n" +
               "        )\n\n" +
               "def validate_phone(form, field):\n" +
               "    \"\"\"Validate phone number format.\"\"\"\n" +
               "    phone_regex = re.compile(r'^\\+?1?\\d{9,15}$')\n" +
               "    if field.data and not phone_regex.match(field.data):\n" +
               "        raise ValidationError('Invalid phone number format.')\n\n" +
               "def validate_file_size(max_size_mb=5):\n" +
               "    \"\"\"Validate uploaded file size.\"\"\"\n" +
               "    def _validate(form, field):\n" +
               "        if field.data:\n" +
               "            file_size = len(field.data.read())\n" +
               "            field.data.seek(0)\n" +
               "            max_bytes = max_size_mb * 1024 * 1024\n" +
               "            if file_size > max_bytes:\n" +
               "                raise ValidationError(f'File size must not exceed {max_size_mb}MB.')\n" +
               "    return _validate\n\n" +
               "def validate_allowed_file(allowed_extensions):\n" +
               "    \"\"\"Validate file extension.\"\"\"\n" +
               "    def _validate(form, field):\n" +
               "        if field.data:\n" +
               "            filename = field.data.filename\n" +
               "            if '.' not in filename or \\\n" +
               "               filename.rsplit('.', 1)[1].lower() not in allowed_extensions:\n" +
               "                raise ValidationError(\n" +
               "                    f'File type not allowed. Allowed types: {', '.join(allowed_extensions)}'\n" +
               "                )\n" +
               "    return _validate\n\n" +
               "class PasswordStrength:\n" +
               "    \"\"\"Validator for password strength.\"\"\"\n" +
               "    def __init__(self, min_length=8, require_uppercase=True, require_lowercase=True,\n" +
               "                 require_numbers=True, require_special=True):\n" +
               "        self.min_length = min_length\n" +
               "        self.require_uppercase = require_uppercase\n" +
               "        self.require_lowercase = require_lowercase\n" +
               "        self.require_numbers = require_numbers\n" +
               "        self.require_special = require_special\n\n" +
               "    def __call__(self, form, field):\n" +
               "        password = field.data\n" +
               "        if len(password) < self.min_length:\n" +
               "            raise ValidationError(f'Password must be at least {self.min_length} characters.')\n" +
               "        if self.require_uppercase and not re.search(r'[A-Z]', password):\n" +
               "            raise ValidationError('Password must contain at least one uppercase letter.')\n" +
               "        if self.require_lowercase and not re.search(r'[a-z]', password):\n" +
               "            raise ValidationError('Password must contain at least one lowercase letter.')\n" +
               "        if self.require_numbers and not re.search(r'\\d', password):\n" +
               "            raise ValidationError('Password must contain at least one number.')\n" +
               "        if self.require_special and not re.search(r'[!@#$%^&*(),.?\":{}|<>]', password):\n" +
               "            raise ValidationError('Password must contain at least one special character.')\n";
    }

    private String generateDecorators() {
        return "\"\"\"Custom decorators for Flask routes.\"\"\"\n\n" +
               "from functools import wraps\n" +
               "from flask import jsonify, request, current_app\n" +
               "from flask_jwt_extended import verify_jwt_in_request, get_jwt_identity, get_jwt\n" +
               "from app.models.user import User\n\n" +
               "def admin_required(fn):\n" +
               "    \"\"\"Require admin role to access route.\"\"\"\n" +
               "    @wraps(fn)\n" +
               "    def wrapper(*args, **kwargs):\n" +
               "        verify_jwt_in_request()\n" +
               "        user_id = get_jwt_identity()\n" +
               "        user = User.query.get(user_id)\n" +
               "        if not user or user.role.name != 'admin':\n" +
               "            return jsonify({'error': 'Admin access required'}), 403\n" +
               "        return fn(*args, **kwargs)\n" +
               "    return wrapper\n\n" +
               "def role_required(roles):\n" +
               "    \"\"\"Require specific role(s) to access route.\"\"\"\n" +
               "    def decorator(fn):\n" +
               "        @wraps(fn)\n" +
               "        def wrapper(*args, **kwargs):\n" +
               "            verify_jwt_in_request()\n" +
               "            user_id = get_jwt_identity()\n" +
               "            user = User.query.get(user_id)\n" +
               "            if not user or user.role.name not in roles:\n" +
               "                return jsonify({'error': f'Required role: {roles}'}), 403\n" +
               "            return fn(*args, **kwargs)\n" +
               "        return wrapper\n" +
               "    return decorator\n\n" +
               "def permission_required(permission):\n" +
               "    \"\"\"Require specific permission to access route.\"\"\"\n" +
               "    def decorator(fn):\n" +
               "        @wraps(fn)\n" +
               "        def wrapper(*args, **kwargs):\n" +
               "            verify_jwt_in_request()\n" +
               "            user_id = get_jwt_identity()\n" +
               "            user = User.query.get(user_id)\n" +
               "            if not user or permission not in user.role.permissions:\n" +
               "                return jsonify({'error': f'Permission required: {permission}'}), 403\n" +
               "            return fn(*args, **kwargs)\n" +
               "        return wrapper\n" +
               "    return decorator\n\n" +
               "def json_required(fn):\n" +
               "    \"\"\"Require JSON content type.\"\"\"\n" +
               "    @wraps(fn)\n" +
               "    def wrapper(*args, **kwargs):\n" +
               "        if not request.is_json:\n" +
               "            return jsonify({'error': 'Content-Type must be application/json'}), 400\n" +
               "        return fn(*args, **kwargs)\n" +
               "    return wrapper\n\n" +
               "def validate_schema(schema_class):\n" +
               "    \"\"\"Validate request data against marshmallow schema.\"\"\"\n" +
               "    def decorator(fn):\n" +
               "        @wraps(fn)\n" +
               "        def wrapper(*args, **kwargs):\n" +
               "            schema = schema_class()\n" +
               "            errors = schema.validate(request.get_json())\n" +
               "            if errors:\n" +
               "                return jsonify({'errors': errors}), 400\n" +
               "            return fn(*args, **kwargs)\n" +
               "        return wrapper\n" +
               "    return decorator\n\n" +
               "def cache_response(timeout=300):\n" +
               "    \"\"\"Cache route response.\"\"\"\n" +
               "    def decorator(fn):\n" +
               "        @wraps(fn)\n" +
               "        def wrapper(*args, **kwargs):\n" +
               "            from app import cache\n" +
               "            cache_key = f'{fn.__name__}:{request.full_path}'\n" +
               "            cached_response = cache.get(cache_key)\n" +
               "            if cached_response:\n" +
               "                return cached_response\n" +
               "            response = fn(*args, **kwargs)\n" +
               "            cache.set(cache_key, response, timeout=timeout)\n" +
               "            return response\n" +
               "        return wrapper\n" +
               "    return decorator\n";
    }

    private String generateHelpers() {
        return "\"\"\"Helper utility functions.\"\"\"\n\n" +
               "import os\n" +
               "import hashlib\n" +
               "import secrets\n" +
               "from datetime import datetime, timedelta\n" +
               "from werkzeug.utils import secure_filename\n" +
               "from flask import current_app, url_for\n\n" +
               "def generate_token(length=32):\n" +
               "    \"\"\"Generate a random token.\"\"\"\n" +
               "    return secrets.token_urlsafe(length)\n\n" +
               "def generate_file_hash(file_data):\n" +
               "    \"\"\"Generate SHA256 hash of file data.\"\"\"\n" +
               "    return hashlib.sha256(file_data).hexdigest()\n\n" +
               "def allowed_file(filename, allowed_extensions=None):\n" +
               "    \"\"\"Check if file extension is allowed.\"\"\"\n" +
               "    if allowed_extensions is None:\n" +
               "        allowed_extensions = current_app.config.get('ALLOWED_EXTENSIONS', set())\n" +
               "    return '.' in filename and \\\n" +
               "           filename.rsplit('.', 1)[1].lower() in allowed_extensions\n\n" +
               "def save_uploaded_file(file, folder='uploads', custom_filename=None):\n" +
               "    \"\"\"Save uploaded file securely.\"\"\"\n" +
               "    if not file:\n" +
               "        return None\n" +
               "    \n" +
               "    filename = custom_filename or secure_filename(file.filename)\n" +
               "    timestamp = datetime.utcnow().strftime('%Y%m%d_%H%M%S')\n" +
               "    name, ext = os.path.splitext(filename)\n" +
               "    unique_filename = f'{name}_{timestamp}{ext}'\n" +
               "    \n" +
               "    upload_folder = os.path.join(current_app.config['UPLOAD_FOLDER'], folder)\n" +
               "    os.makedirs(upload_folder, exist_ok=True)\n" +
               "    \n" +
               "    filepath = os.path.join(upload_folder, unique_filename)\n" +
               "    file.save(filepath)\n" +
               "    \n" +
               "    return filepath\n\n" +
               "def format_datetime(dt, format='%Y-%m-%d %H:%M:%S'):\n" +
               "    \"\"\"Format datetime object.\"\"\"\n" +
               "    if not dt:\n" +
               "        return None\n" +
               "    return dt.strftime(format)\n\n" +
               "def parse_datetime(date_string, format='%Y-%m-%d %H:%M:%S'):\n" +
               "    \"\"\"Parse datetime string.\"\"\"\n" +
               "    try:\n" +
               "        return datetime.strptime(date_string, format)\n" +
               "    except (ValueError, TypeError):\n" +
               "        return None\n\n" +
               "def time_ago(dt):\n" +
               "    \"\"\"Get human-readable time ago string.\"\"\"\n" +
               "    if not dt:\n" +
               "        return ''\n" +
               "    \n" +
               "    diff = datetime.utcnow() - dt\n" +
               "    seconds = diff.total_seconds()\n" +
               "    \n" +
               "    if seconds < 60:\n" +
               "        return 'just now'\n" +
               "    elif seconds < 3600:\n" +
               "        minutes = int(seconds / 60)\n" +
               "        return f'{minutes} minute{'s' if minutes != 1 else ''} ago'\n" +
               "    elif seconds < 86400:\n" +
               "        hours = int(seconds / 3600)\n" +
               "        return f'{hours} hour{'s' if hours != 1 else ''} ago'\n" +
               "    elif seconds < 604800:\n" +
               "        days = int(seconds / 86400)\n" +
               "        return f'{days} day{'s' if days != 1 else ''} ago'\n" +
               "    else:\n" +
               "        return format_datetime(dt, '%Y-%m-%d')\n\n" +
               "def paginate_query(query, page=1, per_page=20):\n" +
               "    \"\"\"Paginate SQLAlchemy query.\"\"\"\n" +
               "    pagination = query.paginate(\n" +
               "        page=page,\n" +
               "        per_page=per_page,\n" +
               "        error_out=False\n" +
               "    )\n" +
               "    return {\n" +
               "        'items': pagination.items,\n" +
               "        'total': pagination.total,\n" +
               "        'pages': pagination.pages,\n" +
               "        'current_page': pagination.page,\n" +
               "        'per_page': pagination.per_page,\n" +
               "        'has_next': pagination.has_next,\n" +
               "        'has_prev': pagination.has_prev\n" +
               "    }\n\n" +
               "def build_pagination_links(pagination, endpoint, **kwargs):\n" +
               "    \"\"\"Build pagination links.\"\"\"\n" +
               "    links = {}\n" +
               "    if pagination.has_prev:\n" +
               "        links['prev'] = url_for(endpoint, page=pagination.prev_num, **kwargs)\n" +
               "    if pagination.has_next:\n" +
               "        links['next'] = url_for(endpoint, page=pagination.next_num, **kwargs)\n" +
               "    links['first'] = url_for(endpoint, page=1, **kwargs)\n" +
               "    links['last'] = url_for(endpoint, page=pagination.pages, **kwargs)\n" +
               "    return links\n";
    }
    private String generateMainCSS() {
        return "/* Main application styles */\n\n" +
               ":root {\n" +
               "    --primary-color: #3498db;\n" +
               "    --secondary-color: #2ecc71;\n" +
               "    --danger-color: #e74c3c;\n" +
               "    --warning-color: #f39c12;\n" +
               "    --dark-color: #34495e;\n" +
               "    --light-color: #ecf0f1;\n" +
               "    --border-radius: 4px;\n" +
               "    --box-shadow: 0 2px 4px rgba(0,0,0,0.1);\n" +
               "}\n\n" +
               "* {\n" +
               "    margin: 0;\n" +
               "    padding: 0;\n" +
               "    box-sizing: border-box;\n" +
               "}\n\n" +
               "body {\n" +
               "    font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;\n" +
               "    line-height: 1.6;\n" +
               "    color: #333;\n" +
               "    background-color: #f5f5f5;\n" +
               "}\n\n" +
               ".container {\n" +
               "    max-width: 1200px;\n" +
               "    margin: 0 auto;\n" +
               "    padding: 0 20px;\n" +
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
               "    gap: 20px;\n" +
               "}\n\n" +
               "nav a {\n" +
               "    color: white;\n" +
               "    text-decoration: none;\n" +
               "    padding: 0.5rem 1rem;\n" +
               "    border-radius: var(--border-radius);\n" +
               "    transition: background-color 0.3s;\n" +
               "}\n\n" +
               "nav a:hover {\n" +
               "    background-color: rgba(255,255,255,0.1);\n" +
               "}\n\n" +
               ".btn {\n" +
               "    display: inline-block;\n" +
               "    padding: 0.6rem 1.2rem;\n" +
               "    border: none;\n" +
               "    border-radius: var(--border-radius);\n" +
               "    cursor: pointer;\n" +
               "    font-size: 1rem;\n" +
               "    text-decoration: none;\n" +
               "    transition: all 0.3s;\n" +
               "}\n\n" +
               ".btn-primary {\n" +
               "    background-color: var(--primary-color);\n" +
               "    color: white;\n" +
               "}\n\n" +
               ".btn-primary:hover {\n" +
               "    background-color: #2980b9;\n" +
               "}\n\n" +
               ".btn-success {\n" +
               "    background-color: var(--secondary-color);\n" +
               "    color: white;\n" +
               "}\n\n" +
               ".btn-danger {\n" +
               "    background-color: var(--danger-color);\n" +
               "    color: white;\n" +
               "}\n\n" +
               ".card {\n" +
               "    background-color: white;\n" +
               "    border-radius: var(--border-radius);\n" +
               "    box-shadow: var(--box-shadow);\n" +
               "    padding: 1.5rem;\n" +
               "    margin-bottom: 1.5rem;\n" +
               "}\n\n" +
               ".form-group {\n" +
               "    margin-bottom: 1rem;\n" +
               "}\n\n" +
               ".form-group label {\n" +
               "    display: block;\n" +
               "    margin-bottom: 0.5rem;\n" +
               "    font-weight: 500;\n" +
               "}\n\n" +
               ".form-control {\n" +
               "    width: 100%;\n" +
               "    padding: 0.6rem;\n" +
               "    border: 1px solid #ddd;\n" +
               "    border-radius: var(--border-radius);\n" +
               "    font-size: 1rem;\n" +
               "}\n\n" +
               ".form-control:focus {\n" +
               "    outline: none;\n" +
               "    border-color: var(--primary-color);\n" +
               "}\n\n" +
               ".alert {\n" +
               "    padding: 1rem;\n" +
               "    border-radius: var(--border-radius);\n" +
               "    margin-bottom: 1rem;\n" +
               "}\n\n" +
               ".alert-success {\n" +
               "    background-color: #d4edda;\n" +
               "    border: 1px solid #c3e6cb;\n" +
               "    color: #155724;\n" +
               "}\n\n" +
               ".alert-error {\n" +
               "    background-color: #f8d7da;\n" +
               "    border: 1px solid #f5c6cb;\n" +
               "    color: #721c24;\n" +
               "}\n\n" +
               "table {\n" +
               "    width: 100%;\n" +
               "    border-collapse: collapse;\n" +
               "}\n\n" +
               "table th, table td {\n" +
               "    padding: 0.75rem;\n" +
               "    text-align: left;\n" +
               "    border-bottom: 1px solid #ddd;\n" +
               "}\n\n" +
               "table th {\n" +
               "    background-color: var(--light-color);\n" +
               "    font-weight: 600;\n" +
               "}\n\n" +
               "@media (max-width: 768px) {\n" +
               "    nav ul {\n" +
               "        flex-direction: column;\n" +
               "        gap: 10px;\n" +
               "    }\n" +
               "}\n";
    }

    private String generateMainJS(FlaskProjectType type) {
        return "// Main application JavaScript\n\n" +
               "document.addEventListener('DOMContentLoaded', function() {\n" +
               "    console.log('Application initialized');\n\n" +
               "    // Flash message auto-dismiss\n" +
               "    const flashMessages = document.querySelectorAll('.alert');\n" +
               "    flashMessages.forEach(msg => {\n" +
               "        setTimeout(() => {\n" +
               "            msg.style.opacity = '0';\n" +
               "            setTimeout(() => msg.remove(), 300);\n" +
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
               "function validateForm(form) {\n" +
               "    let isValid = true;\n" +
               "    const requiredFields = form.querySelectorAll('[required]');\n" +
               "    \n" +
               "    requiredFields.forEach(field => {\n" +
               "        if (!field.value.trim()) {\n" +
               "            showError(field, 'This field is required');\n" +
               "            isValid = false;\n" +
               "        } else {\n" +
               "            clearError(field);\n" +
               "        }\n" +
               "    });\n" +
               "    \n" +
               "    return isValid;\n" +
               "}\n\n" +
               "function showError(field, message) {\n" +
               "    clearError(field);\n" +
               "    field.classList.add('error');\n" +
               "    const errorDiv = document.createElement('div');\n" +
               "    errorDiv.className = 'field-error';\n" +
               "    errorDiv.textContent = message;\n" +
               "    field.parentNode.appendChild(errorDiv);\n" +
               "}\n\n" +
               "function clearError(field) {\n" +
               "    field.classList.remove('error');\n" +
               "    const errorDiv = field.parentNode.querySelector('.field-error');\n" +
               "    if (errorDiv) {\n" +
               "        errorDiv.remove();\n" +
               "    }\n" +
               "}\n\n" +
               (type == FlaskProjectType.REST_API || type == FlaskProjectType.WEBSOCKET_APP ? 
               "// API Helper functions\n" +
               "async function apiRequest(url, method = 'GET', data = null) {\n" +
               "    const options = {\n" +
               "        method: method,\n" +
               "        headers: {\n" +
               "            'Content-Type': 'application/json',\n" +
               "        }\n" +
               "    };\n\n" +
               "    const token = localStorage.getItem('access_token');\n" +
               "    if (token) {\n" +
               "        options.headers['Authorization'] = `Bearer ${token}`;\n" +
               "    }\n\n" +
               "    if (data) {\n" +
               "        options.body = JSON.stringify(data);\n" +
               "    }\n\n" +
               "    try {\n" +
               "        const response = await fetch(url, options);\n" +
               "        const result = await response.json();\n" +
               "        \n" +
               "        if (!response.ok) {\n" +
               "            throw new Error(result.error || 'Request failed');\n" +
               "        }\n" +
               "        \n" +
               "        return result;\n" +
               "    } catch (error) {\n" +
               "        console.error('API Error:', error);\n" +
               "        throw error;\n" +
               "    }\n" +
               "}\n\n" +
               "function setAuthToken(token) {\n" +
               "    localStorage.setItem('access_token', token);\n" +
               "}\n\n" +
               "function clearAuthToken() {\n" +
               "    localStorage.removeItem('access_token');\n" +
               "}\n" : "") +
               (type == FlaskProjectType.WEBSOCKET_APP ?
               "\n// WebSocket connection\n" +
               "const socket = io();\n\n" +
               "socket.on('connect', function() {\n" +
               "    console.log('WebSocket connected');\n" +
               "});\n\n" +
               "socket.on('disconnect', function() {\n" +
               "    console.log('WebSocket disconnected');\n" +
               "});\n\n" +
               "socket.on('message', function(data) {\n" +
               "    console.log('Received message:', data);\n" +
               "});\n" : "");
    }
    private String generateBaseTemplate() {
        return "<!DOCTYPE html>\n" +
               "<html lang=\"en\">\n" +
               "<head>\n" +
               "    <meta charset=\"UTF-8\">\n" +
               "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
               "    <meta http-equiv=\"X-UA-Compatible\" content=\"ie=edge\">\n" +
               "    <title>{% block title %}Flask Application{% endblock %}</title>\n" +
               "    \n" +
               "    <!-- CSS -->\n" +
               "    <link rel=\"stylesheet\" href=\"{{ url_for('static', filename='css/style.css') }}\">\n" +
               "    {% block extra_css %}{% endblock %}\n" +
               "</head>\n" +
               "<body>\n" +
               "    <header>\n" +
               "        <nav class=\"container\">\n" +
               "            <div class=\"logo\">\n" +
               "                <a href=\"{{ url_for('main.index') }}\">Flask App</a>\n" +
               "            </div>\n" +
               "            <ul>\n" +
               "                <li><a href=\"{{ url_for('main.index') }}\">Home</a></li>\n" +
               "                {% if current_user.is_authenticated %}\n" +
               "                    <li><a href=\"{{ url_for('main.dashboard') }}\">Dashboard</a></li>\n" +
               "                    <li><a href=\"{{ url_for('auth.logout') }}\">Logout</a></li>\n" +
               "                {% else %}\n" +
               "                    <li><a href=\"{{ url_for('auth.login') }}\">Login</a></li>\n" +
               "                    <li><a href=\"{{ url_for('auth.register') }}\">Register</a></li>\n" +
               "                {% endif %}\n" +
               "            </ul>\n" +
               "        </nav>\n" +
               "    </header>\n\n" +
               "    <main class=\"container\">\n" +
               "        <!-- Flash messages -->\n" +
               "        {% with messages = get_flashed_messages(with_categories=true) %}\n" +
               "            {% if messages %}\n" +
               "                {% for category, message in messages %}\n" +
               "                    <div class=\"alert alert-{{ category }}\">\n" +
               "                        {{ message }}\n" +
               "                    </div>\n" +
               "                {% endfor %}\n" +
               "            {% endif %}\n" +
               "        {% endwith %}\n\n" +
               "        <!-- Page content -->\n" +
               "        {% block content %}{% endblock %}\n" +
               "    </main>\n\n" +
               "    <footer class=\"container\">\n" +
               "        <p>&copy; {{ current_year }} Flask Application. All rights reserved.</p>\n" +
               "    </footer>\n\n" +
               "    <!-- JavaScript -->\n" +
               "    <script src=\"{{ url_for('static', filename='js/app.js') }}\"></script>\n" +
               "    {% block extra_js %}{% endblock %}\n" +
               "</body>\n" +
               "</html>\n";
    }

    private String generateIndexTemplate() {
        return "{% extends \"base.html\" %}\n\n" +
               "{% block title %}Home - Flask Application{% endblock %}\n\n" +
               "{% block content %}\n" +
               "<div class=\"hero\">\n" +
               "    <h1>Welcome to Flask Application</h1>\n" +
               "    <p>A modern web application built with Flask</p>\n" +
               "    \n" +
               "    {% if not current_user.is_authenticated %}\n" +
               "        <div class=\"cta-buttons\">\n" +
               "            <a href=\"{{ url_for('auth.register') }}\" class=\"btn btn-primary\">Get Started</a>\n" +
               "            <a href=\"{{ url_for('auth.login') }}\" class=\"btn btn-secondary\">Sign In</a>\n" +
               "        </div>\n" +
               "    {% endif %}\n" +
               "</div>\n\n" +
               "<div class=\"features\">\n" +
               "    <div class=\"card\">\n" +
               "        <h3>🚀 Fast & Efficient</h3>\n" +
               "        <p>Built with Flask for optimal performance and scalability</p>\n" +
               "    </div>\n" +
               "    \n" +
               "    <div class=\"card\">\n" +
               "        <h3>🔒 Secure</h3>\n" +
               "        <p>Industry-standard security practices and authentication</p>\n" +
               "    </div>\n" +
               "    \n" +
               "    <div class=\"card\">\n" +
               "        <h3>📱 Responsive</h3>\n" +
               "        <p>Works seamlessly across all devices and screen sizes</p>\n" +
               "    </div>\n" +
               "</div>\n\n" +
               "{% if current_user.is_authenticated %}\n" +
               "<div class=\"card\">\n" +
               "    <h2>Welcome back, {{ current_user.username }}!</h2>\n" +
               "    <p>You're logged in and ready to go.</p>\n" +
               "    <a href=\"{{ url_for('main.dashboard') }}\" class=\"btn btn-primary\">Go to Dashboard</a>\n" +
               "</div>\n" +
               "{% endif %}\n" +
               "{% endblock %}\n";
    }

    private String generate404Template() {
        return "{% extends \"base.html\" %}\n\n" +
               "{% block title %}404 - Page Not Found{% endblock %}\n\n" +
               "{% block content %}\n" +
               "<div class=\"error-page\">\n" +
               "    <div class=\"card\" style=\"text-align: center; padding: 3rem;\">\n" +
               "        <h1 style=\"font-size: 5rem; color: #e74c3c; margin-bottom: 1rem;\">404</h1>\n" +
               "        <h2>Page Not Found</h2>\n" +
               "        <p>Sorry, the page you're looking for doesn't exist.</p>\n" +
               "        <div style=\"margin-top: 2rem;\">\n" +
               "            <a href=\"{{ url_for('main.index') }}\" class=\"btn btn-primary\">Go Home</a>\n" +
               "            <a href=\"javascript:history.back()\" class=\"btn btn-secondary\">Go Back</a>\n" +
               "        </div>\n" +
               "    </div>\n" +
               "</div>\n" +
               "{% endblock %}\n";
    }

    private String generate500Template() {
        return "{% extends \"base.html\" %}\n\n" +
               "{% block title %}500 - Server Error{% endblock %}\n\n" +
               "{% block content %}\n" +
               "<div class=\"error-page\">\n" +
               "    <div class=\"card\" style=\"text-align: center; padding: 3rem;\">\n" +
               "        <h1 style=\"font-size: 5rem; color: #e74c3c; margin-bottom: 1rem;\">500</h1>\n" +
               "        <h2>Internal Server Error</h2>\n" +
               "        <p>Sorry, something went wrong on our end. We're working to fix it.</p>\n" +
               "        <p>Please try again later.</p>\n" +
               "        <div style=\"margin-top: 2rem;\">\n" +
               "            <a href=\"{{ url_for('main.index') }}\" class=\"btn btn-primary\">Go Home</a>\n" +
               "            <a href=\"javascript:location.reload()\" class=\"btn btn-secondary\">Retry</a>\n" +
               "        </div>\n" +
               "    </div>\n" +
               "</div>\n" +
               "{% endblock %}\n";
    }
    private String generateConftest() {
        return "\"\"\"Pytest configuration and fixtures.\"\"\"\n\n" +
               "import pytest\n" +
               "from app import create_app, db\n" +
               "from app.models.user import User\n" +
               "from app.models.role import Role\n" +
               "from app.config import config\n\n" +
               "@pytest.fixture(scope='session')\n" +
               "def app():\n" +
               "    \"\"\"Create application for testing.\"\"\"\n" +
               "    app = create_app(config['testing'])\n" +
               "    return app\n\n" +
               "@pytest.fixture(scope='function')\n" +
               "def client(app):\n" +
               "    \"\"\"Create test client.\"\"\"\n" +
               "    return app.test_client()\n\n" +
               "@pytest.fixture(scope='function')\n" +
               "def init_database(app):\n" +
               "    \"\"\"Initialize database for testing.\"\"\"\n" +
               "    with app.app_context():\n" +
               "        db.create_all()\n" +
               "        \n" +
               "        # Create test roles\n" +
               "        admin_role = Role(name='admin', description='Administrator')\n" +
               "        user_role = Role(name='user', description='Regular user')\n" +
               "        db.session.add(admin_role)\n" +
               "        db.session.add(user_role)\n" +
               "        db.session.commit()\n" +
               "        \n" +
               "        # Create test users\n" +
               "        admin_user = User(username='admin', email='admin@test.com', role_id=admin_role.id)\n" +
               "        admin_user.set_password('admin123')\n" +
               "        admin_user.is_verified = True\n" +
               "        \n" +
               "        test_user = User(username='testuser', email='test@test.com', role_id=user_role.id)\n" +
               "        test_user.set_password('test123')\n" +
               "        test_user.is_verified = True\n" +
               "        \n" +
               "        db.session.add(admin_user)\n" +
               "        db.session.add(test_user)\n" +
               "        db.session.commit()\n" +
               "        \n" +
               "        yield db\n" +
               "        \n" +
               "        db.drop_all()\n\n" +
               "@pytest.fixture\n" +
               "def auth_token(client, init_database):\n" +
               "    \"\"\"Get authentication token for testing.\"\"\"\n" +
               "    response = client.post('/api/v1/auth/login', json={\n" +
               "        'username': 'testuser',\n" +
               "        'password': 'test123'\n" +
               "    })\n" +
               "    return response.json['access_token']\n\n" +
               "@pytest.fixture\n" +
               "def admin_token(client, init_database):\n" +
               "    \"\"\"Get admin authentication token for testing.\"\"\"\n" +
               "    response = client.post('/api/v1/auth/login', json={\n" +
               "        'username': 'admin',\n" +
               "        'password': 'admin123'\n" +
               "    })\n" +
               "    return response.json['access_token']\n";
    }

    private String generateTestModels() {
        return "\"\"\"Tests for database models.\"\"\"\n\n" +
               "import pytest\n" +
               "from app.models.user import User\n" +
               "from app.models.role import Role\n\n" +
               "def test_user_creation(init_database):\n" +
               "    \"\"\"Test user model creation.\"\"\"\n" +
               "    user = User.query.filter_by(username='testuser').first()\n" +
               "    assert user is not None\n" +
               "    assert user.username == 'testuser'\n" +
               "    assert user.email == 'test@test.com'\n" +
               "    assert user.is_active is True\n\n" +
               "def test_user_password_hashing(init_database):\n" +
               "    \"\"\"Test password hashing.\"\"\"\n" +
               "    user = User.query.filter_by(username='testuser').first()\n" +
               "    assert user.password_hash != 'test123'\n" +
               "    assert user.check_password('test123') is True\n" +
               "    assert user.check_password('wrongpassword') is False\n\n" +
               "def test_user_role_relationship(init_database):\n" +
               "    \"\"\"Test user-role relationship.\"\"\"\n" +
               "    user = User.query.filter_by(username='testuser').first()\n" +
               "    assert user.role is not None\n" +
               "    assert user.role.name == 'user'\n\n" +
               "def test_role_creation(init_database):\n" +
               "    \"\"\"Test role model creation.\"\"\"\n" +
               "    role = Role.query.filter_by(name='admin').first()\n" +
               "    assert role is not None\n" +
               "    assert role.name == 'admin'\n" +
               "    assert role.description == 'Administrator'\n\n" +
               "def test_user_to_dict(init_database):\n" +
               "    \"\"\"Test user to_dict method.\"\"\"\n" +
               "    user = User.query.filter_by(username='testuser').first()\n" +
               "    user_dict = user.to_dict()\n" +
               "    assert 'id' in user_dict\n" +
               "    assert 'username' in user_dict\n" +
               "    assert 'email' in user_dict\n" +
               "    assert 'created_at' in user_dict\n\n" +
               "def test_user_save_method(app, init_database):\n" +
               "    \"\"\"Test user save method.\"\"\"\n" +
               "    with app.app_context():\n" +
               "        role = Role.query.filter_by(name='user').first()\n" +
               "        new_user = User(username='newuser', email='new@test.com', role_id=role.id)\n" +
               "        new_user.set_password('password123')\n" +
               "        new_user.save()\n" +
               "        \n" +
               "        saved_user = User.query.filter_by(username='newuser').first()\n" +
               "        assert saved_user is not None\n" +
               "        assert saved_user.email == 'new@test.com'\n";
    }

    private String generateTestAPI() {
        return "\"\"\"Tests for API endpoints.\"\"\"\n\n" +
               "import pytest\n" +
               "import json\n\n" +
               "def test_index_route(client):\n" +
               "    \"\"\"Test index route.\"\"\"\n" +
               "    response = client.get('/')\n" +
               "    assert response.status_code == 200\n\n" +
               "def test_login_success(client, init_database):\n" +
               "    \"\"\"Test successful login.\"\"\"\n" +
               "    response = client.post('/api/v1/auth/login', json={\n" +
               "        'username': 'testuser',\n" +
               "        'password': 'test123'\n" +
               "    })\n" +
               "    assert response.status_code == 200\n" +
               "    data = response.json\n" +
               "    assert 'access_token' in data\n" +
               "    assert 'refresh_token' in data\n\n" +
               "def test_login_invalid_credentials(client, init_database):\n" +
               "    \"\"\"Test login with invalid credentials.\"\"\"\n" +
               "    response = client.post('/api/v1/auth/login', json={\n" +
               "        'username': 'testuser',\n" +
               "        'password': 'wrongpassword'\n" +
               "    })\n" +
               "    assert response.status_code == 401\n\n" +
               "def test_register_new_user(client, init_database):\n" +
               "    \"\"\"Test user registration.\"\"\"\n" +
               "    response = client.post('/api/v1/auth/register', json={\n" +
               "        'username': 'newuser',\n" +
               "        'email': 'newuser@test.com',\n" +
               "        'password': 'password123'\n" +
               "    })\n" +
               "    assert response.status_code == 201\n" +
               "    data = response.json\n" +
               "    assert data['username'] == 'newuser'\n\n" +
               "def test_register_duplicate_username(client, init_database):\n" +
               "    \"\"\"Test registration with existing username.\"\"\"\n" +
               "    response = client.post('/api/v1/auth/register', json={\n" +
               "        'username': 'testuser',\n" +
               "        'email': 'another@test.com',\n" +
               "        'password': 'password123'\n" +
               "    })\n" +
               "    assert response.status_code == 400\n\n" +
               "def test_protected_route_without_token(client, init_database):\n" +
               "    \"\"\"Test accessing protected route without token.\"\"\"\n" +
               "    response = client.get('/api/v1/users/profile')\n" +
               "    assert response.status_code == 401\n\n" +
               "def test_protected_route_with_token(client, init_database, auth_token):\n" +
               "    \"\"\"Test accessing protected route with valid token.\"\"\"\n" +
               "    headers = {'Authorization': f'Bearer {auth_token}'}\n" +
               "    response = client.get('/api/v1/users/profile', headers=headers)\n" +
               "    assert response.status_code == 200\n\n" +
               "def test_admin_route_without_permission(client, init_database, auth_token):\n" +
               "    \"\"\"Test accessing admin route without permission.\"\"\"\n" +
               "    headers = {'Authorization': f'Bearer {auth_token}'}\n" +
               "    response = client.get('/api/v1/admin/users', headers=headers)\n" +
               "    assert response.status_code == 403\n\n" +
               "def test_admin_route_with_permission(client, init_database, admin_token):\n" +
               "    \"\"\"Test accessing admin route with permission.\"\"\"\n" +
               "    headers = {'Authorization': f'Bearer {admin_token}'}\n" +
               "    response = client.get('/api/v1/admin/users', headers=headers)\n" +
               "    assert response.status_code == 200\n\n" +
               "def test_pagination(client, init_database, auth_token):\n" +
               "    \"\"\"Test API pagination.\"\"\"\n" +
               "    headers = {'Authorization': f'Bearer {auth_token}'}\n" +
               "    response = client.get('/api/v1/users?page=1&per_page=10', headers=headers)\n" +
               "    assert response.status_code == 200\n" +
               "    data = response.json\n" +
               "    assert 'items' in data\n" +
               "    assert 'total' in data\n" +
               "    assert 'pages' in data\n";
    }
    private String generateInitDbScript() {
        return "#!/usr/bin/env python\n" +
               "\"\"\"Database initialization script.\"\"\"\n\n" +
               "import sys\n" +
               "import os\n" +
               "sys.path.insert(0, os.path.dirname(os.path.dirname(os.path.abspath(__file__))))\n\n" +
               "from app import create_app, db\n" +
               "from app.models.user import User\n" +
               "from app.models.role import Role\n\n" +
               "def init_db():\n" +
               "    \"\"\"Initialize the database with tables and default data.\"\"\"\n" +
               "    app = create_app()\n" +
               "    \n" +
               "    with app.app_context():\n" +
               "        print('Creating database tables...')\n" +
               "        db.create_all()\n" +
               "        \n" +
               "        # Create default roles\n" +
               "        print('Creating default roles...')\n" +
               "        roles = [\n" +
               "            Role(name='admin', description='Administrator with full access',\n" +
               "                 permissions=['read', 'write', 'delete', 'admin']),\n" +
               "            Role(name='user', description='Regular user',\n" +
               "                 permissions=['read', 'write']),\n" +
               "            Role(name='guest', description='Guest user with limited access',\n" +
               "                 permissions=['read'])\n" +
               "        ]\n" +
               "        \n" +
               "        for role in roles:\n" +
               "            existing = Role.query.filter_by(name=role.name).first()\n" +
               "            if not existing:\n" +
               "                db.session.add(role)\n" +
               "        \n" +
               "        db.session.commit()\n" +
               "        print('Default roles created successfully.')\n" +
               "        \n" +
               "        # Create admin user if doesn't exist\n" +
               "        admin_role = Role.query.filter_by(name='admin').first()\n" +
               "        admin_user = User.query.filter_by(username='admin').first()\n" +
               "        \n" +
               "        if not admin_user:\n" +
               "            print('Creating admin user...')\n" +
               "            admin_user = User(\n" +
               "                username='admin',\n" +
               "                email='admin@example.com',\n" +
               "                first_name='Admin',\n" +
               "                last_name='User',\n" +
               "                role_id=admin_role.id,\n" +
               "                is_verified=True\n" +
               "            )\n" +
               "            admin_user.set_password('admin123')\n" +
               "            db.session.add(admin_user)\n" +
               "            db.session.commit()\n" +
               "            print('Admin user created. Username: admin, Password: admin123')\n" +
               "            print('*** Please change the admin password after first login! ***')\n" +
               "        \n" +
               "        print('Database initialization completed successfully!')\n\n" +
               "if __name__ == '__main__':\n" +
               "    init_db()\n";
    }

    private String generateSeedDbScript() {
        return "#!/usr/bin/env python\n" +
               "\"\"\"Database seeding script with sample data.\"\"\"\n\n" +
               "import sys\n" +
               "import os\n" +
               "from datetime import datetime, timedelta\n" +
               "import random\n" +
               "sys.path.insert(0, os.path.dirname(os.path.dirname(os.path.abspath(__file__))))\n\n" +
               "from app import create_app, db\n" +
               "from app.models.user import User\n" +
               "from app.models.role import Role\n\n" +
               "def seed():\n" +
               "    \"\"\"Seed the database with sample data.\"\"\"\n" +
               "    app = create_app()\n" +
               "    \n" +
               "    with app.app_context():\n" +
               "        print('Seeding database with sample data...')\n" +
               "        \n" +
               "        # Get roles\n" +
               "        user_role = Role.query.filter_by(name='user').first()\n" +
               "        \n" +
               "        if not user_role:\n" +
               "            print('Error: Roles not found. Please run init_db.py first.')\n" +
               "            return\n" +
               "        \n" +
               "        # Create sample users\n" +
               "        sample_users = [\n" +
               "            {'username': 'john_doe', 'email': 'john@example.com',\n" +
               "             'first_name': 'John', 'last_name': 'Doe'},\n" +
               "            {'username': 'jane_smith', 'email': 'jane@example.com',\n" +
               "             'first_name': 'Jane', 'last_name': 'Smith'},\n" +
               "            {'username': 'bob_wilson', 'email': 'bob@example.com',\n" +
               "             'first_name': 'Bob', 'last_name': 'Wilson'},\n" +
               "            {'username': 'alice_brown', 'email': 'alice@example.com',\n" +
               "             'first_name': 'Alice', 'last_name': 'Brown'},\n" +
               "            {'username': 'charlie_davis', 'email': 'charlie@example.com',\n" +
               "             'first_name': 'Charlie', 'last_name': 'Davis'}\n" +
               "        ]\n" +
               "        \n" +
               "        for user_data in sample_users:\n" +
               "            existing = User.query.filter_by(username=user_data['username']).first()\n" +
               "            if not existing:\n" +
               "                user = User(\n" +
               "                    username=user_data['username'],\n" +
               "                    email=user_data['email'],\n" +
               "                    first_name=user_data['first_name'],\n" +
               "                    last_name=user_data['last_name'],\n" +
               "                    role_id=user_role.id,\n" +
               "                    is_verified=True\n" +
               "                )\n" +
               "                user.set_password('password123')\n" +
               "                db.session.add(user)\n" +
               "                print(f\"Created user: {user_data['username']}\")\n" +
               "        \n" +
               "        db.session.commit()\n" +
               "        print('Database seeding completed successfully!')\n" +
               "        print('All sample users have password: password123')\n\n" +
               "if __name__ == '__main__':\n" +
               "    seed()\n";
    }

    private String generateAPIDocs(FlaskProjectType type) {
        return "# API Documentation\n\n" +
               "## Overview\n\n" +
               "This document describes the REST API endpoints available in this Flask application.\n\n" +
               "## Base URL\n\n" +
               "```\n" +
               "http://localhost:5000/api/v1\n" +
               "```\n\n" +
               "## Authentication\n\n" +
               "Most endpoints require authentication using JWT tokens. Include the token in the Authorization header:\n\n" +
               "```\n" +
               "Authorization: Bearer <your_token>\n" +
               "```\n\n" +
               "## Endpoints\n\n" +
               "### Authentication\n\n" +
               "#### Register User\n" +
               "```\n" +
               "POST /api/v1/auth/register\n" +
               "```\n\n" +
               "**Request Body:**\n" +
               "```json\n" +
               "{\n" +
               "  \"username\": \"string\",\n" +
               "  \"email\": \"string\",\n" +
               "  \"password\": \"string\"\n" +
               "}\n" +
               "```\n\n" +
               "**Response:** 201 Created\n" +
               "```json\n" +
               "{\n" +
               "  \"id\": 1,\n" +
               "  \"username\": \"string\",\n" +
               "  \"email\": \"string\",\n" +
               "  \"created_at\": \"2024-01-01T00:00:00\"\n" +
               "}\n" +
               "```\n\n" +
               "#### Login\n" +
               "```\n" +
               "POST /api/v1/auth/login\n" +
               "```\n\n" +
               "**Request Body:**\n" +
               "```json\n" +
               "{\n" +
               "  \"username\": \"string\",\n" +
               "  \"password\": \"string\"\n" +
               "}\n" +
               "```\n\n" +
               "**Response:** 200 OK\n" +
               "```json\n" +
               "{\n" +
               "  \"access_token\": \"string\",\n" +
               "  \"refresh_token\": \"string\",\n" +
               "  \"token_type\": \"Bearer\"\n" +
               "}\n" +
               "```\n\n" +
               "#### Refresh Token\n" +
               "```\n" +
               "POST /api/v1/auth/refresh\n" +
               "```\n\n" +
               "**Headers:**\n" +
               "```\n" +
               "Authorization: Bearer <refresh_token>\n" +
               "```\n\n" +
               "**Response:** 200 OK\n" +
               "```json\n" +
               "{\n" +
               "  \"access_token\": \"string\"\n" +
               "}\n" +
               "```\n\n" +
               "### Users\n\n" +
               "#### Get Current User Profile\n" +
               "```\n" +
               "GET /api/v1/users/profile\n" +
               "```\n\n" +
               "**Response:** 200 OK\n" +
               "```json\n" +
               "{\n" +
               "  \"id\": 1,\n" +
               "  \"username\": \"string\",\n" +
               "  \"email\": \"string\",\n" +
               "  \"first_name\": \"string\",\n" +
               "  \"last_name\": \"string\",\n" +
               "  \"role\": \"string\",\n" +
               "  \"created_at\": \"2024-01-01T00:00:00\"\n" +
               "}\n" +
               "```\n\n" +
               "#### Update User Profile\n" +
               "```\n" +
               "PUT /api/v1/users/profile\n" +
               "```\n\n" +
               "**Request Body:**\n" +
               "```json\n" +
               "{\n" +
               "  \"first_name\": \"string\",\n" +
               "  \"last_name\": \"string\",\n" +
               "  \"email\": \"string\"\n" +
               "}\n" +
               "```\n\n" +
               "#### List Users (Admin only)\n" +
               "```\n" +
               "GET /api/v1/admin/users?page=1&per_page=20\n" +
               "```\n\n" +
               "**Response:** 200 OK\n" +
               "```json\n" +
               "{\n" +
               "  \"items\": [],\n" +
               "  \"total\": 0,\n" +
               "  \"pages\": 0,\n" +
               "  \"current_page\": 1,\n" +
               "  \"per_page\": 20\n" +
               "}\n" +
               "```\n\n" +
               "## Error Responses\n\n" +
               "All endpoints may return the following error responses:\n\n" +
               "**400 Bad Request**\n" +
               "```json\n" +
               "{\n" +
               "  \"error\": \"Error message\"\n" +
               "}\n" +
               "```\n\n" +
               "**401 Unauthorized**\n" +
               "```json\n" +
               "{\n" +
               "  \"error\": \"Authentication required\"\n" +
               "}\n" +
               "```\n\n" +
               "**403 Forbidden**\n" +
               "```json\n" +
               "{\n" +
               "  \"error\": \"Permission denied\"\n" +
               "}\n" +
               "```\n\n" +
               "**404 Not Found**\n" +
               "```json\n" +
               "{\n" +
               "  \"error\": \"Resource not found\"\n" +
               "}\n" +
               "```\n\n" +
               "**500 Internal Server Error**\n" +
               "```json\n" +
               "{\n" +
               "  \"error\": \"Internal server error\"\n" +
               "}\n" +
               "```\n";
    }
    private String generateUserResource() {
        return "\"\"\"User resource for Flask-RESTful.\"\"\"\n\n" +
               "from flask import request\n" +
               "from flask_restful import Resource\n" +
               "from flask_jwt_extended import jwt_required, get_jwt_identity\n" +
               "from app.models.user import User\n" +
               "from app.models.base import db\n" +
               "from app.api.v1.schemas.user_schema import UserSchema, UserUpdateSchema\n" +
               "from marshmallow import ValidationError\n\n" +
               "user_schema = UserSchema()\n" +
               "users_schema = UserSchema(many=True)\n" +
               "update_schema = UserUpdateSchema()\n\n" +
               "class UserProfileResource(Resource):\n" +
               "    @jwt_required()\n" +
               "    def get(self):\n" +
               "        \"\"\"Get current user profile.\"\"\"\n" +
               "        user_id = get_jwt_identity()\n" +
               "        user = User.query.get_or_404(user_id)\n" +
               "        return user_schema.dump(user), 200\n" +
               "    \n" +
               "    @jwt_required()\n" +
               "    def put(self):\n" +
               "        \"\"\"Update current user profile.\"\"\"\n" +
               "        user_id = get_jwt_identity()\n" +
               "        user = User.query.get_or_404(user_id)\n" +
               "        \n" +
               "        try:\n" +
               "            data = update_schema.load(request.get_json())\n" +
               "        except ValidationError as err:\n" +
               "            return {'errors': err.messages}, 400\n" +
               "        \n" +
               "        for key, value in data.items():\n" +
               "            setattr(user, key, value)\n" +
               "        \n" +
               "        db.session.commit()\n" +
               "        return user_schema.dump(user), 200\n\n" +
               "class UserListResource(Resource):\n" +
               "    @jwt_required()\n" +
               "    def get(self):\n" +
               "        \"\"\"Get list of users with pagination.\"\"\"\n" +
               "        page = request.args.get('page', 1, type=int)\n" +
               "        per_page = request.args.get('per_page', 20, type=int)\n" +
               "        \n" +
               "        pagination = User.query.paginate(\n" +
               "            page=page,\n" +
               "            per_page=per_page,\n" +
               "            error_out=False\n" +
               "        )\n" +
               "        \n" +
               "        return {\n" +
               "            'items': users_schema.dump(pagination.items),\n" +
               "            'total': pagination.total,\n" +
               "            'pages': pagination.pages,\n" +
               "            'current_page': pagination.page,\n" +
               "            'per_page': pagination.per_page\n" +
               "        }, 200\n\n" +
               "class UserDetailResource(Resource):\n" +
               "    @jwt_required()\n" +
               "    def get(self, user_id):\n" +
               "        \"\"\"Get user by ID.\"\"\"\n" +
               "        user = User.query.get_or_404(user_id)\n" +
               "        return user_schema.dump(user), 200\n" +
               "    \n" +
               "    @jwt_required()\n" +
               "    def delete(self, user_id):\n" +
               "        \"\"\"Delete user by ID.\"\"\"\n" +
               "        user = User.query.get_or_404(user_id)\n" +
               "        db.session.delete(user)\n" +
               "        db.session.commit()\n" +
               "        return {'message': 'User deleted successfully'}, 200\n";
    }

    private String generateAuthResource() {
        return "\"\"\"Authentication resource for Flask-RESTful.\"\"\"\n\n" +
               "from flask import request\n" +
               "from flask_restful import Resource\n" +
               "from flask_jwt_extended import (\n" +
               "    create_access_token, create_refresh_token,\n" +
               "    jwt_required, get_jwt_identity\n" +
               ")\n" +
               "from app.models.user import User\n" +
               "from app.models.role import Role\n" +
               "from app.models.base import db\n" +
               "from app.api.v1.schemas.auth_schema import LoginSchema, RegisterSchema\n" +
               "from marshmallow import ValidationError\n\n" +
               "login_schema = LoginSchema()\n" +
               "register_schema = RegisterSchema()\n\n" +
               "class RegisterResource(Resource):\n" +
               "    def post(self):\n" +
               "        \"\"\"Register a new user.\"\"\"\n" +
               "        try:\n" +
               "            data = register_schema.load(request.get_json())\n" +
               "        except ValidationError as err:\n" +
               "            return {'errors': err.messages}, 400\n" +
               "        \n" +
               "        # Check if username already exists\n" +
               "        if User.query.filter_by(username=data['username']).first():\n" +
               "            return {'error': 'Username already exists'}, 400\n" +
               "        \n" +
               "        # Check if email already exists\n" +
               "        if User.query.filter_by(email=data['email']).first():\n" +
               "            return {'error': 'Email already exists'}, 400\n" +
               "        \n" +
               "        # Get default user role\n" +
               "        user_role = Role.query.filter_by(name='user').first()\n" +
               "        if not user_role:\n" +
               "            return {'error': 'Default role not found'}, 500\n" +
               "        \n" +
               "        # Create new user\n" +
               "        user = User(\n" +
               "            username=data['username'],\n" +
               "            email=data['email'],\n" +
               "            role_id=user_role.id\n" +
               "        )\n" +
               "        user.set_password(data['password'])\n" +
               "        \n" +
               "        db.session.add(user)\n" +
               "        db.session.commit()\n" +
               "        \n" +
               "        return {\n" +
               "            'id': user.id,\n" +
               "            'username': user.username,\n" +
               "            'email': user.email,\n" +
               "            'created_at': user.created_at.isoformat()\n" +
               "        }, 201\n\n" +
               "class LoginResource(Resource):\n" +
               "    def post(self):\n" +
               "        \"\"\"Authenticate user and return tokens.\"\"\"\n" +
               "        try:\n" +
               "            data = login_schema.load(request.get_json())\n" +
               "        except ValidationError as err:\n" +
               "            return {'errors': err.messages}, 400\n" +
               "        \n" +
               "        # Find user\n" +
               "        user = User.query.filter_by(username=data['username']).first()\n" +
               "        \n" +
               "        if not user or not user.check_password(data['password']):\n" +
               "            return {'error': 'Invalid username or password'}, 401\n" +
               "        \n" +
               "        if not user.is_active:\n" +
               "            return {'error': 'Account is deactivated'}, 401\n" +
               "        \n" +
               "        # Create tokens\n" +
               "        access_token = create_access_token(identity=user.id)\n" +
               "        refresh_token = create_refresh_token(identity=user.id)\n" +
               "        \n" +
               "        return {\n" +
               "            'access_token': access_token,\n" +
               "            'refresh_token': refresh_token,\n" +
               "            'token_type': 'Bearer'\n" +
               "        }, 200\n\n" +
               "class RefreshResource(Resource):\n" +
               "    @jwt_required(refresh=True)\n" +
               "    def post(self):\n" +
               "        \"\"\"Refresh access token.\"\"\"\n" +
               "        current_user = get_jwt_identity()\n" +
               "        access_token = create_access_token(identity=current_user)\n" +
               "        return {'access_token': access_token}, 200\n\n" +
               "class LogoutResource(Resource):\n" +
               "    @jwt_required()\n" +
               "    def post(self):\n" +
               "        \"\"\"Logout user (token blacklisting would be implemented here).\"\"\"\n" +
               "        # In production, add token to blacklist\n" +
               "        return {'message': 'Successfully logged out'}, 200\n";
    }

    private String generateUserSchema() {
        return "\"\"\"User marshmallow schemas.\"\"\"\n\n" +
               "from marshmallow import Schema, fields, validate, validates, ValidationError\n" +
               "from app.models.user import User\n\n" +
               "class UserSchema(Schema):\n" +
               "    \"\"\"User serialization schema.\"\"\"\n" +
               "    id = fields.Int(dump_only=True)\n" +
               "    username = fields.Str(required=True, validate=validate.Length(min=3, max=80))\n" +
               "    email = fields.Email(required=True)\n" +
               "    first_name = fields.Str(validate=validate.Length(max=50))\n" +
               "    last_name = fields.Str(validate=validate.Length(max=50))\n" +
               "    is_active = fields.Bool(dump_only=True)\n" +
               "    is_verified = fields.Bool(dump_only=True)\n" +
               "    created_at = fields.DateTime(dump_only=True)\n" +
               "    updated_at = fields.DateTime(dump_only=True)\n" +
               "    role = fields.Nested('RoleSchema', dump_only=True)\n\n" +
               "class UserUpdateSchema(Schema):\n" +
               "    \"\"\"User update schema.\"\"\"\n" +
               "    email = fields.Email()\n" +
               "    first_name = fields.Str(validate=validate.Length(max=50))\n" +
               "    last_name = fields.Str(validate=validate.Length(max=50))\n\n" +
               "class RoleSchema(Schema):\n" +
               "    \"\"\"Role serialization schema.\"\"\"\n" +
               "    id = fields.Int(dump_only=True)\n" +
               "    name = fields.Str(required=True)\n" +
               "    description = fields.Str()\n" +
               "    permissions = fields.List(fields.Str())\n";
    }

    private String generateAuthSchema() {
        return "\"\"\"Authentication marshmallow schemas.\"\"\"\n\n" +
               "from marshmallow import Schema, fields, validate, validates, ValidationError\n" +
               "import re\n\n" +
               "class RegisterSchema(Schema):\n" +
               "    \"\"\"User registration schema.\"\"\"\n" +
               "    username = fields.Str(\n" +
               "        required=True,\n" +
               "        validate=validate.Length(min=3, max=80)\n" +
               "    )\n" +
               "    email = fields.Email(required=True)\n" +
               "    password = fields.Str(\n" +
               "        required=True,\n" +
               "        validate=validate.Length(min=8)\n" +
               "    )\n" +
               "    \n" +
               "    @validates('username')\n" +
               "    def validate_username(self, value):\n" +
               "        if not re.match(r'^[a-zA-Z0-9_]+$', value):\n" +
               "            raise ValidationError(\n" +
               "                'Username can only contain letters, numbers, and underscores.'\n" +
               "            )\n" +
               "    \n" +
               "    @validates('password')\n" +
               "    def validate_password(self, value):\n" +
               "        if not re.search(r'[A-Z]', value):\n" +
               "            raise ValidationError('Password must contain at least one uppercase letter.')\n" +
               "        if not re.search(r'[a-z]', value):\n" +
               "            raise ValidationError('Password must contain at least one lowercase letter.')\n" +
               "        if not re.search(r'\\d', value):\n" +
               "            raise ValidationError('Password must contain at least one number.')\n\n" +
               "class LoginSchema(Schema):\n" +
               "    \"\"\"User login schema.\"\"\"\n" +
               "    username = fields.Str(required=True)\n" +
               "    password = fields.Str(required=True)\n";
    }

    private String generateAPIRoutes() {
        return "\"\"\"API routes configuration.\"\"\"\n\n" +
               "from flask import Blueprint\n" +
               "from flask_restful import Api\n" +
               "from app.api.v1.resources.user import (\n" +
               "    UserProfileResource, UserListResource, UserDetailResource\n" +
               ")\n" +
               "from app.api.v1.resources.auth import (\n" +
               "    RegisterResource, LoginResource, RefreshResource, LogoutResource\n" +
               ")\n\n" +
               "api_bp = Blueprint('api', __name__)\n" +
               "api = Api(api_bp)\n\n" +
               "# Auth routes\n" +
               "api.add_resource(RegisterResource, '/auth/register')\n" +
               "api.add_resource(LoginResource, '/auth/login')\n" +
               "api.add_resource(RefreshResource, '/auth/refresh')\n" +
               "api.add_resource(LogoutResource, '/auth/logout')\n\n" +
               "# User routes\n" +
               "api.add_resource(UserProfileResource, '/users/profile')\n" +
               "api.add_resource(UserListResource, '/users')\n" +
               "api.add_resource(UserDetailResource, '/users/<int:user_id>')\n";
    }

    private String generateMiddleware() {
        return "\"\"\"API middleware for request processing.\"\"\"\n\n" +
               "from flask import request, jsonify\n" +
               "from functools import wraps\n" +
               "import time\n" +
               "import logging\n\n" +
               "logger = logging.getLogger(__name__)\n\n" +
               "def log_request(f):\n" +
               "    \"\"\"Log all API requests.\"\"\"\n" +
               "    @wraps(f)\n" +
               "    def decorated_function(*args, **kwargs):\n" +
               "        start_time = time.time()\n" +
               "        \n" +
               "        # Log request\n" +
               "        logger.info(f'{request.method} {request.path} - {request.remote_addr}')\n" +
               "        \n" +
               "        # Execute request\n" +
               "        response = f(*args, **kwargs)\n" +
               "        \n" +
               "        # Log response time\n" +
               "        duration = time.time() - start_time\n" +
               "        logger.info(f'{request.method} {request.path} - {duration:.3f}s')\n" +
               "        \n" +
               "        return response\n" +
               "    return decorated_function\n\n" +
               "def validate_content_type(f):\n" +
               "    \"\"\"Validate JSON content type for POST/PUT requests.\"\"\"\n" +
               "    @wraps(f)\n" +
               "    def decorated_function(*args, **kwargs):\n" +
               "        if request.method in ['POST', 'PUT', 'PATCH']:\n" +
               "            if not request.is_json:\n" +
               "                return jsonify({'error': 'Content-Type must be application/json'}), 400\n" +
               "        return f(*args, **kwargs)\n" +
               "    return decorated_function\n\n" +
               "def add_cors_headers(response):\n" +
               "    \"\"\"Add CORS headers to response.\"\"\"\n" +
               "    response.headers['Access-Control-Allow-Origin'] = '*'\n" +
               "    response.headers['Access-Control-Allow-Methods'] = 'GET, POST, PUT, DELETE, OPTIONS'\n" +
               "    response.headers['Access-Control-Allow-Headers'] = 'Content-Type, Authorization'\n" +
               "    return response\n\n" +
               "def rate_limit_exceeded(e):\n" +
               "    \"\"\"Handle rate limit exceeded.\"\"\"\n" +
               "    return jsonify({'error': 'Rate limit exceeded. Please try again later.'}), 429\n";
    }
    private String generateMainViews() {
        return "\"\"\"Main application views.\"\"\"\n\n" +
               "from flask import Blueprint, render_template, redirect, url_for, flash, request\n" +
               "from flask_login import login_required, current_user\n" +
               "from app.models.user import User\n" +
               "from app.models.base import db\n\n" +
               "main_bp = Blueprint('main', __name__)\n\n" +
               "@main_bp.route('/')\n" +
               "def index():\n" +
               "    \"\"\"Home page.\"\"\"\n" +
               "    return render_template('index.html')\n\n" +
               "@main_bp.route('/dashboard')\n" +
               "@login_required\n" +
               "def dashboard():\n" +
               "    \"\"\"User dashboard.\"\"\"\n" +
               "    return render_template('dashboard.html', user=current_user)\n\n" +
               "@main_bp.route('/profile')\n" +
               "@login_required\n" +
               "def profile():\n" +
               "    \"\"\"User profile page.\"\"\"\n" +
               "    return render_template('profile.html', user=current_user)\n\n" +
               "@main_bp.route('/profile/edit', methods=['GET', 'POST'])\n" +
               "@login_required\n" +
               "def edit_profile():\n" +
               "    \"\"\"Edit user profile.\"\"\"\n" +
               "    if request.method == 'POST':\n" +
               "        current_user.first_name = request.form.get('first_name')\n" +
               "        current_user.last_name = request.form.get('last_name')\n" +
               "        current_user.email = request.form.get('email')\n" +
               "        \n" +
               "        db.session.commit()\n" +
               "        flash('Profile updated successfully!', 'success')\n" +
               "        return redirect(url_for('main.profile'))\n" +
               "    \n" +
               "    return render_template('edit_profile.html', user=current_user)\n\n" +
               "@main_bp.route('/about')\n" +
               "def about():\n" +
               "    \"\"\"About page.\"\"\"\n" +
               "    return render_template('about.html')\n\n" +
               "@main_bp.route('/contact')\n" +
               "def contact():\n" +
               "    \"\"\"Contact page.\"\"\"\n" +
               "    return render_template('contact.html')\n";
    }

    private String generateAuthViews() {
        return "\"\"\"Authentication views.\"\"\"\n\n" +
               "from flask import Blueprint, render_template, redirect, url_for, flash, request\n" +
               "from flask_login import login_user, logout_user, login_required, current_user\n" +
               "from app.models.user import User\n" +
               "from app.models.role import Role\n" +
               "from app.models.base import db\n" +
               "from app.forms.auth_forms import LoginForm, RegisterForm\n\n" +
               "auth_bp = Blueprint('auth', __name__)\n\n" +
               "@auth_bp.route('/login', methods=['GET', 'POST'])\n" +
               "def login():\n" +
               "    \"\"\"User login.\"\"\"\n" +
               "    if current_user.is_authenticated:\n" +
               "        return redirect(url_for('main.dashboard'))\n" +
               "    \n" +
               "    form = LoginForm()\n" +
               "    if form.validate_on_submit():\n" +
               "        user = User.query.filter_by(username=form.username.data).first()\n" +
               "        \n" +
               "        if user and user.check_password(form.password.data):\n" +
               "            if not user.is_active:\n" +
               "                flash('Your account is deactivated.', 'error')\n" +
               "                return redirect(url_for('auth.login'))\n" +
               "            \n" +
               "            login_user(user, remember=form.remember_me.data)\n" +
               "            next_page = request.args.get('next')\n" +
               "            flash('Logged in successfully!', 'success')\n" +
               "            return redirect(next_page or url_for('main.dashboard'))\n" +
               "        else:\n" +
               "            flash('Invalid username or password.', 'error')\n" +
               "    \n" +
               "    return render_template('auth/login.html', form=form)\n\n" +
               "@auth_bp.route('/register', methods=['GET', 'POST'])\n" +
               "def register():\n" +
               "    \"\"\"User registration.\"\"\"\n" +
               "    if current_user.is_authenticated:\n" +
               "        return redirect(url_for('main.dashboard'))\n" +
               "    \n" +
               "    form = RegisterForm()\n" +
               "    if form.validate_on_submit():\n" +
               "        # Check if username exists\n" +
               "        if User.query.filter_by(username=form.username.data).first():\n" +
               "            flash('Username already exists.', 'error')\n" +
               "            return redirect(url_for('auth.register'))\n" +
               "        \n" +
               "        # Check if email exists\n" +
               "        if User.query.filter_by(email=form.email.data).first():\n" +
               "            flash('Email already registered.', 'error')\n" +
               "            return redirect(url_for('auth.register'))\n" +
               "        \n" +
               "        # Get default user role\n" +
               "        user_role = Role.query.filter_by(name='user').first()\n" +
               "        \n" +
               "        # Create new user\n" +
               "        user = User(\n" +
               "            username=form.username.data,\n" +
               "            email=form.email.data,\n" +
               "            role_id=user_role.id\n" +
               "        )\n" +
               "        user.set_password(form.password.data)\n" +
               "        \n" +
               "        db.session.add(user)\n" +
               "        db.session.commit()\n" +
               "        \n" +
               "        flash('Registration successful! Please log in.', 'success')\n" +
               "        return redirect(url_for('auth.login'))\n" +
               "    \n" +
               "    return render_template('auth/register.html', form=form)\n\n" +
               "@auth_bp.route('/logout')\n" +
               "@login_required\n" +
               "def logout():\n" +
               "    \"\"\"User logout.\"\"\"\n" +
               "    logout_user()\n" +
               "    flash('Logged out successfully.', 'success')\n" +
               "    return redirect(url_for('main.index'))\n";
    }

    private String generateAdminViews() {
        return "\"\"\"Admin views.\"\"\"\n\n" +
               "from flask import Blueprint, render_template, redirect, url_for, flash, request\n" +
               "from flask_login import login_required, current_user\n" +
               "from app.models.user import User\n" +
               "from app.models.role import Role\n" +
               "from app.models.base import db\n" +
               "from app.utils.decorators import admin_required\n\n" +
               "admin_bp = Blueprint('admin', __name__)\n\n" +
               "@admin_bp.route('/')\n" +
               "@login_required\n" +
               "@admin_required\n" +
               "def index():\n" +
               "    \"\"\"Admin dashboard.\"\"\"\n" +
               "    total_users = User.query.count()\n" +
               "    active_users = User.query.filter_by(is_active=True).count()\n" +
               "    total_roles = Role.query.count()\n" +
               "    \n" +
               "    stats = {\n" +
               "        'total_users': total_users,\n" +
               "        'active_users': active_users,\n" +
               "        'total_roles': total_roles\n" +
               "    }\n" +
               "    \n" +
               "    return render_template('admin/dashboard.html', stats=stats)\n\n" +
               "@admin_bp.route('/users')\n" +
               "@login_required\n" +
               "@admin_required\n" +
               "def users():\n" +
               "    \"\"\"List all users.\"\"\"\n" +
               "    page = request.args.get('page', 1, type=int)\n" +
               "    per_page = 20\n" +
               "    \n" +
               "    pagination = User.query.order_by(User.created_at.desc()).paginate(\n" +
               "        page=page,\n" +
               "        per_page=per_page,\n" +
               "        error_out=False\n" +
               "    )\n" +
               "    \n" +
               "    return render_template('admin/users.html', pagination=pagination)\n\n" +
               "@admin_bp.route('/users/<int:user_id>/toggle-active', methods=['POST'])\n" +
               "@login_required\n" +
               "@admin_required\n" +
               "def toggle_user_active(user_id):\n" +
               "    \"\"\"Toggle user active status.\"\"\"\n" +
               "    user = User.query.get_or_404(user_id)\n" +
               "    \n" +
               "    if user.id == current_user.id:\n" +
               "        flash('You cannot deactivate your own account.', 'error')\n" +
               "        return redirect(url_for('admin.users'))\n" +
               "    \n" +
               "    user.is_active = not user.is_active\n" +
               "    db.session.commit()\n" +
               "    \n" +
               "    status = 'activated' if user.is_active else 'deactivated'\n" +
               "    flash(f'User {user.username} has been {status}.', 'success')\n" +
               "    \n" +
               "    return redirect(url_for('admin.users'))\n\n" +
               "@admin_bp.route('/users/<int:user_id>/delete', methods=['POST'])\n" +
               "@login_required\n" +
               "@admin_required\n" +
               "def delete_user(user_id):\n" +
               "    \"\"\"Delete user.\"\"\"\n" +
               "    user = User.query.get_or_404(user_id)\n" +
               "    \n" +
               "    if user.id == current_user.id:\n" +
               "        flash('You cannot delete your own account.', 'error')\n" +
               "        return redirect(url_for('admin.users'))\n" +
               "    \n" +
               "    username = user.username\n" +
               "    db.session.delete(user)\n" +
               "    db.session.commit()\n" +
               "    \n" +
               "    flash(f'User {username} has been deleted.', 'success')\n" +
               "    return redirect(url_for('admin.users'))\n\n" +
               "@admin_bp.route('/roles')\n" +
               "@login_required\n" +
               "@admin_required\n" +
               "def roles():\n" +
               "    \"\"\"List all roles.\"\"\"\n" +
               "    all_roles = Role.query.all()\n" +
               "    return render_template('admin/roles.html', roles=all_roles)\n";
    }

    private String generateAuthForms() {
        return "\"\"\"Authentication forms.\"\"\"\n\n" +
               "from flask_wtf import FlaskForm\n" +
               "from wtforms import StringField, PasswordField, BooleanField, SubmitField\n" +
               "from wtforms.validators import DataRequired, Email, Length, EqualTo, ValidationError\n" +
               "from app.utils.validators import validate_username, PasswordStrength\n" +
               "import re\n\n" +
               "class LoginForm(FlaskForm):\n" +
               "    \"\"\"User login form.\"\"\"\n" +
               "    username = StringField('Username', validators=[\n" +
               "        DataRequired(),\n" +
               "        Length(min=3, max=80)\n" +
               "    ])\n" +
               "    password = PasswordField('Password', validators=[\n" +
               "        DataRequired()\n" +
               "    ])\n" +
               "    remember_me = BooleanField('Remember Me')\n" +
               "    submit = SubmitField('Login')\n\n" +
               "class RegisterForm(FlaskForm):\n" +
               "    \"\"\"User registration form.\"\"\"\n" +
               "    username = StringField('Username', validators=[\n" +
               "        DataRequired(),\n" +
               "        Length(min=3, max=80),\n" +
               "        validate_username\n" +
               "    ])\n" +
               "    email = StringField('Email', validators=[\n" +
               "        DataRequired(),\n" +
               "        Email()\n" +
               "    ])\n" +
               "    password = PasswordField('Password', validators=[\n" +
               "        DataRequired(),\n" +
               "        Length(min=8),\n" +
               "        PasswordStrength()\n" +
               "    ])\n" +
               "    confirm_password = PasswordField('Confirm Password', validators=[\n" +
               "        DataRequired(),\n" +
               "        EqualTo('password', message='Passwords must match')\n" +
               "    ])\n" +
               "    submit = SubmitField('Register')\n\n" +
               "class ForgotPasswordForm(FlaskForm):\n" +
               "    \"\"\"Forgot password form.\"\"\"\n" +
               "    email = StringField('Email', validators=[\n" +
               "        DataRequired(),\n" +
               "        Email()\n" +
               "    ])\n" +
               "    submit = SubmitField('Reset Password')\n\n" +
               "class ResetPasswordForm(FlaskForm):\n" +
               "    \"\"\"Reset password form.\"\"\"\n" +
               "    password = PasswordField('New Password', validators=[\n" +
               "        DataRequired(),\n" +
               "        Length(min=8),\n" +
               "        PasswordStrength()\n" +
               "    ])\n" +
               "    confirm_password = PasswordField('Confirm Password', validators=[\n" +
               "        DataRequired(),\n" +
               "        EqualTo('password', message='Passwords must match')\n" +
               "    ])\n" +
               "    submit = SubmitField('Set New Password')\n";
    }

    private String generateUserForms() {
        return "\"\"\"User forms.\"\"\"\n\n" +
               "from flask_wtf import FlaskForm\n" +
               "from flask_wtf.file import FileField, FileAllowed\n" +
               "from wtforms import StringField, TextAreaField, SelectField, SubmitField\n" +
               "from wtforms.validators import DataRequired, Email, Length, Optional\n\n" +
               "class ProfileForm(FlaskForm):\n" +
               "    \"\"\"User profile form.\"\"\"\n" +
               "    first_name = StringField('First Name', validators=[\n" +
               "        Optional(),\n" +
               "        Length(max=50)\n" +
               "    ])\n" +
               "    last_name = StringField('Last Name', validators=[\n" +
               "        Optional(),\n" +
               "        Length(max=50)\n" +
               "    ])\n" +
               "    email = StringField('Email', validators=[\n" +
               "        DataRequired(),\n" +
               "        Email()\n" +
               "    ])\n" +
               "    bio = TextAreaField('Bio', validators=[\n" +
               "        Optional(),\n" +
               "        Length(max=500)\n" +
               "    ])\n" +
               "    avatar = FileField('Profile Picture', validators=[\n" +
               "        Optional(),\n" +
               "        FileAllowed(['jpg', 'jpeg', 'png', 'gif'], 'Images only!')\n" +
               "    ])\n" +
               "    submit = SubmitField('Update Profile')\n\n" +
               "class ChangePasswordForm(FlaskForm):\n" +
               "    \"\"\"Change password form.\"\"\"\n" +
               "    current_password = PasswordField('Current Password', validators=[\n" +
               "        DataRequired()\n" +
               "    ])\n" +
               "    new_password = PasswordField('New Password', validators=[\n" +
               "        DataRequired(),\n" +
               "        Length(min=8)\n" +
               "    ])\n" +
               "    confirm_password = PasswordField('Confirm New Password', validators=[\n" +
               "        DataRequired(),\n" +
               "        EqualTo('new_password', message='Passwords must match')\n" +
               "    ])\n" +
               "    submit = SubmitField('Change Password')\n\n" +
               "class UserAdminForm(FlaskForm):\n" +
               "    \"\"\"Admin user management form.\"\"\"\n" +
               "    username = StringField('Username', validators=[\n" +
               "        DataRequired(),\n" +
               "        Length(min=3, max=80)\n" +
               "    ])\n" +
               "    email = StringField('Email', validators=[\n" +
               "        DataRequired(),\n" +
               "        Email()\n" +
               "    ])\n" +
               "    role = SelectField('Role', coerce=int, validators=[\n" +
               "        DataRequired()\n" +
               "    ])\n" +
               "    is_active = SelectField('Status', choices=[\n" +
               "        (1, 'Active'),\n" +
               "        (0, 'Inactive')\n" +
               "    ], coerce=int)\n" +
               "    submit = SubmitField('Save')\n";
    }
    private String generateDataService() {
        return "\"\"\"Data service for microservice operations.\"\"\"\n\n" +
               "from typing import List, Dict, Any, Optional\n" +
               "import json\n" +
               "from app.models.base import db\n\n" +
               "class DataService:\n" +
               "    \"\"\"Service for data operations.\"\"\"\n" +
               "    \n" +
               "    @staticmethod\n" +
               "    def fetch_data(filters: Dict[str, Any] = None) -> List[Dict]:\n" +
               "        \"\"\"Fetch data with optional filters.\"\"\"\n" +
               "        # Implement data fetching logic\n" +
               "        pass\n" +
               "    \n" +
               "    @staticmethod\n" +
               "    def process_batch(data: List[Dict]) -> Dict[str, Any]:\n" +
               "        \"\"\"Process data in batch.\"\"\"\n" +
               "        results = []\n" +
               "        errors = []\n" +
               "        \n" +
               "        for item in data:\n" +
               "            try:\n" +
               "                processed = DataService._process_item(item)\n" +
               "                results.append(processed)\n" +
               "            except Exception as e:\n" +
               "                errors.append({'item': item, 'error': str(e)})\n" +
               "        \n" +
               "        return {\n" +
               "            'success': len(results),\n" +
               "            'failed': len(errors),\n" +
               "            'results': results,\n" +
               "            'errors': errors\n" +
               "        }\n" +
               "    \n" +
               "    @staticmethod\n" +
               "    def _process_item(item: Dict) -> Dict:\n" +
               "        \"\"\"Process individual item.\"\"\"\n" +
               "        # Add processing logic\n" +
               "        return item\n" +
               "    \n" +
               "    @staticmethod\n" +
               "    def validate_data(data: Dict, schema: Dict) -> tuple:\n" +
               "        \"\"\"Validate data against schema.\"\"\"\n" +
               "        errors = []\n" +
               "        \n" +
               "        for field, rules in schema.items():\n" +
               "            if rules.get('required') and field not in data:\n" +
               "                errors.append(f'{field} is required')\n" +
               "            elif field in data:\n" +
               "                value = data[field]\n" +
               "                if 'type' in rules and not isinstance(value, rules['type']):\n" +
               "                    errors.append(f'{field} must be of type {rules[\"type\"].__name__}')\n" +
               "        \n" +
               "        return (len(errors) == 0, errors)\n";
    }

    private String generateCacheService() {
        return "\"\"\"Cache service for data caching.\"\"\"\n\n" +
               "from flask import current_app\n" +
               "from app import cache\n" +
               "import json\n" +
               "import hashlib\n\n" +
               "class CacheService:\n" +
               "    \"\"\"Service for caching operations.\"\"\"\n" +
               "    \n" +
               "    @staticmethod\n" +
               "    def get(key: str):\n" +
               "        \"\"\"Get value from cache.\"\"\"\n" +
               "        return cache.get(key)\n" +
               "    \n" +
               "    @staticmethod\n" +
               "    def set(key: str, value, timeout=300):\n" +
               "        \"\"\"Set value in cache.\"\"\"\n" +
               "        return cache.set(key, value, timeout=timeout)\n" +
               "    \n" +
               "    @staticmethod\n" +
               "    def delete(key: str):\n" +
               "        \"\"\"Delete value from cache.\"\"\"\n" +
               "        return cache.delete(key)\n" +
               "    \n" +
               "    @staticmethod\n" +
               "    def clear_all():\n" +
               "        \"\"\"Clear all cache.\"\"\"\n" +
               "        return cache.clear()\n" +
               "    \n" +
               "    @staticmethod\n" +
               "    def generate_cache_key(*args, **kwargs):\n" +
               "        \"\"\"Generate cache key from arguments.\"\"\"\n" +
               "        key_data = f\"{args}:{sorted(kwargs.items())}\"\n" +
               "        return hashlib.md5(key_data.encode()).hexdigest()\n" +
               "    \n" +
               "    @staticmethod\n" +
               "    def cache_result(key_prefix: str, timeout=300):\n" +
               "        \"\"\"Decorator to cache function results.\"\"\"\n" +
               "        def decorator(func):\n" +
               "            def wrapper(*args, **kwargs):\n" +
               "                cache_key = f\"{key_prefix}:{CacheService.generate_cache_key(*args, **kwargs)}\"\n" +
               "                result = CacheService.get(cache_key)\n" +
               "                \n" +
               "                if result is None:\n" +
               "                    result = func(*args, **kwargs)\n" +
               "                    CacheService.set(cache_key, result, timeout=timeout)\n" +
               "                \n" +
               "                return result\n" +
               "            return wrapper\n" +
               "        return decorator\n";
    }

    private String generateMessageService() {
        return "\"\"\"Message queue service.\"\"\"\n\n" +
               "from celery import Celery\n" +
               "from typing import Dict, Any\n" +
               "import json\n\n" +
               "class MessageService:\n" +
               "    \"\"\"Service for message queue operations.\"\"\"\n" +
               "    \n" +
               "    @staticmethod\n" +
               "    def publish(queue: str, message: Dict[str, Any]):\n" +
               "        \"\"\"Publish message to queue.\"\"\"\n" +
               "        # Implement message publishing\n" +
               "        from app.celery_tasks import process_message\n" +
               "        return process_message.delay(queue, message)\n" +
               "    \n" +
               "    @staticmethod\n" +
               "    def send_email(to: str, subject: str, body: str):\n" +
               "        \"\"\"Send email asynchronously.\"\"\"\n" +
               "        from app.celery_tasks import send_email_task\n" +
               "        return send_email_task.delay(to, subject, body)\n" +
               "    \n" +
               "    @staticmethod\n" +
               "    def send_notification(user_id: int, message: str, notification_type: str = 'info'):\n" +
               "        \"\"\"Send notification to user.\"\"\"\n" +
               "        from app.celery_tasks import send_notification_task\n" +
               "        return send_notification_task.delay(user_id, message, notification_type)\n" +
               "    \n" +
               "    @staticmethod\n" +
               "    def schedule_task(task_name: str, eta=None, **kwargs):\n" +
               "        \"\"\"Schedule task for future execution.\"\"\"\n" +
               "        # Schedule task with eta\n" +
               "        pass\n";
    }

    private String generateEventHandlers() {
        return "\"\"\"Event handlers for microservice.\"\"\"\n\n" +
               "from flask import current_app\n" +
               "import logging\n\n" +
               "logger = logging.getLogger(__name__)\n\n" +
               "class EventHandler:\n" +
               "    \"\"\"Handle application events.\"\"\"\n" +
               "    \n" +
               "    @staticmethod\n" +
               "    def on_user_created(user):\n" +
               "        \"\"\"Handle user creation event.\"\"\"\n" +
               "        logger.info(f'User created: {user.username}')\n" +
               "        # Send welcome email\n" +
               "        # Create user profile\n" +
               "        # Log analytics event\n" +
               "    \n" +
               "    @staticmethod\n" +
               "    def on_user_login(user):\n" +
               "        \"\"\"Handle user login event.\"\"\"\n" +
               "        logger.info(f'User logged in: {user.username}')\n" +
               "        # Update last login timestamp\n" +
               "        # Log analytics event\n" +
               "    \n" +
               "    @staticmethod\n" +
               "    def on_data_processed(data_id):\n" +
               "        \"\"\"Handle data processing completion.\"\"\"\n" +
               "        logger.info(f'Data processed: {data_id}')\n" +
               "        # Notify relevant parties\n" +
               "        # Update status\n" +
               "    \n" +
               "    @staticmethod\n" +
               "    def on_error(error, context=None):\n" +
               "        \"\"\"Handle application errors.\"\"\"\n" +
               "        logger.error(f'Error occurred: {error}', extra=context or {})\n" +
               "        # Send error notification\n" +
               "        # Log to error tracking service\n";
    }

    private String generateErrorHandlers() {
        return "\"\"\"Error handlers for microservice.\"\"\"\n\n" +
               "from flask import jsonify, request\n" +
               "from werkzeug.exceptions import HTTPException\n" +
               "import logging\n" +
               "import traceback\n\n" +
               "logger = logging.getLogger(__name__)\n\n" +
               "def register_error_handlers(app):\n" +
               "    \"\"\"Register error handlers with Flask app.\"\"\"\n" +
               "    \n" +
               "    @app.errorhandler(400)\n" +
               "    def bad_request(error):\n" +
               "        \"\"\"Handle bad request errors.\"\"\"\n" +
               "        return jsonify({\n" +
               "            'error': 'Bad Request',\n" +
               "            'message': str(error)\n" +
               "        }), 400\n" +
               "    \n" +
               "    @app.errorhandler(401)\n" +
               "    def unauthorized(error):\n" +
               "        \"\"\"Handle unauthorized errors.\"\"\"\n" +
               "        return jsonify({\n" +
               "            'error': 'Unauthorized',\n" +
               "            'message': 'Authentication required'\n" +
               "        }), 401\n" +
               "    \n" +
               "    @app.errorhandler(403)\n" +
               "    def forbidden(error):\n" +
               "        \"\"\"Handle forbidden errors.\"\"\"\n" +
               "        return jsonify({\n" +
               "            'error': 'Forbidden',\n" +
               "            'message': 'You do not have permission to access this resource'\n" +
               "        }), 403\n" +
               "    \n" +
               "    @app.errorhandler(404)\n" +
               "    def not_found(error):\n" +
               "        \"\"\"Handle not found errors.\"\"\"\n" +
               "        return jsonify({\n" +
               "            'error': 'Not Found',\n" +
               "            'message': 'The requested resource was not found'\n" +
               "        }), 404\n" +
               "    \n" +
               "    @app.errorhandler(500)\n" +
               "    def internal_error(error):\n" +
               "        \"\"\"Handle internal server errors.\"\"\"\n" +
               "        logger.error(f'Internal error: {error}')\n" +
               "        logger.error(traceback.format_exc())\n" +
               "        return jsonify({\n" +
               "            'error': 'Internal Server Error',\n" +
               "            'message': 'An unexpected error occurred'\n" +
               "        }), 500\n" +
               "    \n" +
               "    @app.errorhandler(Exception)\n" +
               "    def handle_exception(error):\n" +
               "        \"\"\"Handle all unhandled exceptions.\"\"\"\n" +
               "        if isinstance(error, HTTPException):\n" +
               "            return error\n" +
               "        \n" +
               "        logger.error(f'Unhandled exception: {error}')\n" +
               "        logger.error(traceback.format_exc())\n" +
               "        \n" +
               "        return jsonify({\n" +
               "            'error': 'Internal Server Error',\n" +
               "            'message': 'An unexpected error occurred'\n" +
               "        }), 500\n";
    }

    private String generateDataProcessors() {
        return "\"\"\"Data processors for data API.\"\"\"\n\n" +
               "import pandas as pd\n" +
               "import numpy as np\n" +
               "from typing import List, Dict, Any\n\n" +
               "class DataProcessor:\n" +
               "    \"\"\"Process and transform data.\"\"\"\n" +
               "    \n" +
               "    @staticmethod\n" +
               "    def process_csv(file_path: str) -> pd.DataFrame:\n" +
               "        \"\"\"Process CSV file.\"\"\"\n" +
               "        df = pd.read_csv(file_path)\n" +
               "        return DataProcessor.clean_dataframe(df)\n" +
               "    \n" +
               "    @staticmethod\n" +
               "    def clean_dataframe(df: pd.DataFrame) -> pd.DataFrame:\n" +
               "        \"\"\"Clean DataFrame.\"\"\"\n" +
               "        # Remove duplicates\n" +
               "        df = df.drop_duplicates()\n" +
               "        \n" +
               "        # Handle missing values\n" +
               "        numeric_columns = df.select_dtypes(include=[np.number]).columns\n" +
               "        df[numeric_columns] = df[numeric_columns].fillna(df[numeric_columns].median())\n" +
               "        \n" +
               "        # Handle categorical missing values\n" +
               "        categorical_columns = df.select_dtypes(include=['object']).columns\n" +
               "        df[categorical_columns] = df[categorical_columns].fillna('Unknown')\n" +
               "        \n" +
               "        return df\n" +
               "    \n" +
               "    @staticmethod\n" +
               "    def normalize_data(df: pd.DataFrame, columns: List[str] = None) -> pd.DataFrame:\n" +
               "        \"\"\"Normalize numeric columns.\"\"\"\n" +
               "        if columns is None:\n" +
               "            columns = df.select_dtypes(include=[np.number]).columns\n" +
               "        \n" +
               "        for col in columns:\n" +
               "            df[col] = (df[col] - df[col].min()) / (df[col].max() - df[col].min())\n" +
               "        \n" +
               "        return df\n" +
               "    \n" +
               "    @staticmethod\n" +
               "    def aggregate_data(df: pd.DataFrame, group_by: str, agg_funcs: Dict) -> pd.DataFrame:\n" +
               "        \"\"\"Aggregate data by group.\"\"\"\n" +
               "        return df.groupby(group_by).agg(agg_funcs).reset_index()\n";
    }

    private String generateDataTransformers() {
        return "\"\"\"Data transformers for data API.\"\"\"\n\n" +
               "import pandas as pd\n" +
               "import numpy as np\n" +
               "from typing import List, Dict, Any, Callable\n\n" +
               "class DataTransformer:\n" +
               "    \"\"\"Transform data into different formats.\"\"\"\n" +
               "    \n" +
               "    @staticmethod\n" +
               "    def to_json(df: pd.DataFrame) -> List[Dict]:\n" +
               "        \"\"\"Convert DataFrame to JSON.\"\"\"\n" +
               "        return df.to_dict('records')\n" +
               "    \n" +
               "    @staticmethod\n" +
               "    def to_csv(df: pd.DataFrame, file_path: str):\n" +
               "        \"\"\"Save DataFrame to CSV.\"\"\"\n" +
               "        df.to_csv(file_path, index=False)\n" +
               "    \n" +
               "    @staticmethod\n" +
               "    def pivot_data(df: pd.DataFrame, index: str, columns: str, values: str) -> pd.DataFrame:\n" +
               "        \"\"\"Create pivot table.\"\"\"\n" +
               "        return df.pivot_table(index=index, columns=columns, values=values, aggfunc='mean')\n" +
               "    \n" +
               "    @staticmethod\n" +
               "    def encode_categorical(df: pd.DataFrame, columns: List[str]) -> pd.DataFrame:\n" +
               "        \"\"\"Encode categorical variables.\"\"\"\n" +
               "        for col in columns:\n" +
               "            df[col] = pd.Categorical(df[col]).codes\n" +
               "        return df\n" +
               "    \n" +
               "    @staticmethod\n" +
               "    def apply_transformation(df: pd.DataFrame, column: str, func: Callable) -> pd.DataFrame:\n" +
               "        \"\"\"Apply custom transformation to column.\"\"\"\n" +
               "        df[column] = df[column].apply(func)\n" +
               "        return df\n" +
               "    \n" +
               "    @staticmethod\n" +
               "    def merge_datasets(df1: pd.DataFrame, df2: pd.DataFrame, on: str, how: str = 'inner') -> pd.DataFrame:\n" +
               "        \"\"\"Merge two datasets.\"\"\"\n" +
               "        return pd.merge(df1, df2, on=on, how=how)\n";
    }

    private String generateDataAnalyzers() {
        return "\"\"\"Data analyzers for data API.\"\"\"\n\n" +
               "import pandas as pd\n" +
               "import numpy as np\n" +
               "from typing import Dict, Any, List\n\n" +
               "class DataAnalyzer:\n" +
               "    \"\"\"Analyze and generate insights from data.\"\"\"\n" +
               "    \n" +
               "    @staticmethod\n" +
               "    def get_summary_statistics(df: pd.DataFrame) -> Dict[str, Any]:\n" +
               "        \"\"\"Get summary statistics.\"\"\"\n" +
               "        return {\n" +
               "            'shape': df.shape,\n" +
               "            'columns': list(df.columns),\n" +
               "            'dtypes': df.dtypes.to_dict(),\n" +
               "            'missing_values': df.isnull().sum().to_dict(),\n" +
               "            'numeric_summary': df.describe().to_dict()\n" +
               "        }\n" +
               "    \n" +
               "    @staticmethod\n" +
               "    def detect_outliers(df: pd.DataFrame, column: str) -> pd.Series:\n" +
               "        \"\"\"Detect outliers using IQR method.\"\"\"\n" +
               "        Q1 = df[column].quantile(0.25)\n" +
               "        Q3 = df[column].quantile(0.75)\n" +
               "        IQR = Q3 - Q1\n" +
               "        \n" +
               "        lower_bound = Q1 - 1.5 * IQR\n" +
               "        upper_bound = Q3 + 1.5 * IQR\n" +
               "        \n" +
               "        return (df[column] < lower_bound) | (df[column] > upper_bound)\n" +
               "    \n" +
               "    @staticmethod\n" +
               "    def correlation_analysis(df: pd.DataFrame) -> pd.DataFrame:\n" +
               "        \"\"\"Perform correlation analysis.\"\"\"\n" +
               "        numeric_df = df.select_dtypes(include=[np.number])\n" +
               "        return numeric_df.corr()\n" +
               "    \n" +
               "    @staticmethod\n" +
               "    def trend_analysis(df: pd.DataFrame, date_column: str, value_column: str) -> Dict:\n" +
               "        \"\"\"Analyze trends over time.\"\"\"\n" +
               "        df[date_column] = pd.to_datetime(df[date_column])\n" +
               "        df = df.sort_values(date_column)\n" +
               "        \n" +
               "        return {\n" +
               "            'mean': df[value_column].mean(),\n" +
               "            'median': df[value_column].median(),\n" +
               "            'std': df[value_column].std(),\n" +
               "            'min': df[value_column].min(),\n" +
               "            'max': df[value_column].max(),\n" +
               "            'trend': 'increasing' if df[value_column].iloc[-1] > df[value_column].iloc[0] else 'decreasing'\n" +
               "        }\n";
    }

    private String generateMLModels() {
        return "\"\"\"Machine learning models.\"\"\"\n\n" +
               "import numpy as np\n" +
               "import pandas as pd\n" +
               "from sklearn.model_selection import train_test_split\n" +
               "from sklearn.preprocessing import StandardScaler\n" +
               "from sklearn.ensemble import RandomForestClassifier, RandomForestRegressor\n" +
               "from sklearn.metrics import accuracy_score, mean_squared_error\n" +
               "import joblib\n\n" +
               "class MLModel:\n" +
               "    \"\"\"Base class for ML models.\"\"\"\n" +
               "    \n" +
               "    def __init__(self):\n" +
               "        self.model = None\n" +
               "        self.scaler = StandardScaler()\n" +
               "    \n" +
               "    def preprocess(self, X):\n" +
               "        \"\"\"Preprocess features.\"\"\"\n" +
               "        return self.scaler.fit_transform(X)\n" +
               "    \n" +
               "    def train(self, X, y):\n" +
               "        \"\"\"Train the model.\"\"\"\n" +
               "        X_scaled = self.preprocess(X)\n" +
               "        self.model.fit(X_scaled, y)\n" +
               "    \n" +
               "    def predict(self, X):\n" +
               "        \"\"\"Make predictions.\"\"\"\n" +
               "        X_scaled = self.scaler.transform(X)\n" +
               "        return self.model.predict(X_scaled)\n" +
               "    \n" +
               "    def save(self, filepath):\n" +
               "        \"\"\"Save model to file.\"\"\"\n" +
               "        joblib.dump({'model': self.model, 'scaler': self.scaler}, filepath)\n" +
               "    \n" +
               "    def load(self, filepath):\n" +
               "        \"\"\"Load model from file.\"\"\"\n" +
               "        data = joblib.load(filepath)\n" +
               "        self.model = data['model']\n" +
               "        self.scaler = data['scaler']\n\n" +
               "class ClassificationModel(MLModel):\n" +
               "    \"\"\"Classification model.\"\"\"\n" +
               "    \n" +
               "    def __init__(self, n_estimators=100):\n" +
               "        super().__init__()\n" +
               "        self.model = RandomForestClassifier(n_estimators=n_estimators, random_state=42)\n" +
               "    \n" +
               "    def evaluate(self, X_test, y_test):\n" +
               "        \"\"\"Evaluate model.\"\"\"\n" +
               "        predictions = self.predict(X_test)\n" +
               "        return accuracy_score(y_test, predictions)\n\n" +
               "class RegressionModel(MLModel):\n" +
               "    \"\"\"Regression model.\"\"\"\n" +
               "    \n" +
               "    def __init__(self, n_estimators=100):\n" +
               "        super().__init__()\n" +
               "        self.model = RandomForestRegressor(n_estimators=n_estimators, random_state=42)\n" +
               "    \n" +
               "    def evaluate(self, X_test, y_test):\n" +
               "        \"\"\"Evaluate model.\"\"\"\n" +
               "        predictions = self.predict(X_test)\n" +
               "        return mean_squared_error(y_test, predictions, squared=False)\n";
    }

    private String generatePredictors() {
        return "\"\"\"Prediction services.\"\"\"\n\n" +
               "import numpy as np\n" +
               "import pandas as pd\n" +
               "from typing import Dict, Any, List\n" +
               "from app.ml.models import ClassificationModel, RegressionModel\n\n" +
               "class PredictionService:\n" +
               "    \"\"\"Service for making predictions.\"\"\"\n" +
               "    \n" +
               "    def __init__(self, model_path: str = None):\n" +
               "        self.model = None\n" +
               "        if model_path:\n" +
               "            self.load_model(model_path)\n" +
               "    \n" +
               "    def load_model(self, model_path: str):\n" +
               "        \"\"\"Load trained model.\"\"\"\n" +
               "        self.model = ClassificationModel()\n" +
               "        self.model.load(model_path)\n" +
               "    \n" +
               "    def predict_single(self, features: Dict[str, Any]) -> Dict[str, Any]:\n" +
               "        \"\"\"Make prediction for single instance.\"\"\"\n" +
               "        if not self.model:\n" +
               "            raise ValueError('Model not loaded')\n" +
               "        \n" +
               "        # Convert features dict to array\n" +
               "        feature_array = np.array([list(features.values())])\n" +
               "        \n" +
               "        # Make prediction\n" +
               "        prediction = self.model.predict(feature_array)[0]\n" +
               "        \n" +
               "        return {\n" +
               "            'prediction': prediction,\n" +
               "            'features': features\n" +
               "        }\n" +
               "    \n" +
               "    def predict_batch(self, features_list: List[Dict[str, Any]]) -> List[Dict[str, Any]]:\n" +
               "        \"\"\"Make predictions for multiple instances.\"\"\"\n" +
               "        if not self.model:\n" +
               "            raise ValueError('Model not loaded')\n" +
               "        \n" +
               "        # Convert to DataFrame\n" +
               "        df = pd.DataFrame(features_list)\n" +
               "        \n" +
               "        # Make predictions\n" +
               "        predictions = self.model.predict(df.values)\n" +
               "        \n" +
               "        # Format results\n" +
               "        results = []\n" +
               "        for i, pred in enumerate(predictions):\n" +
               "            results.append({\n" +
               "                'prediction': pred,\n" +
               "                'features': features_list[i]\n" +
               "            })\n" +
               "        \n" +
               "        return results\n" +
               "    \n" +
               "    def predict_with_confidence(self, features: Dict[str, Any]) -> Dict[str, Any]:\n" +
               "        \"\"\"Make prediction with confidence score.\"\"\"\n" +
               "        if not self.model:\n" +
               "            raise ValueError('Model not loaded')\n" +
               "        \n" +
               "        feature_array = np.array([list(features.values())])\n" +
               "        prediction = self.model.predict(feature_array)[0]\n" +
               "        \n" +
               "        # Get probability if available\n" +
               "        confidence = 0.0\n" +
               "        if hasattr(self.model.model, 'predict_proba'):\n" +
               "            probabilities = self.model.model.predict_proba(\n" +
               "                self.model.scaler.transform(feature_array)\n" +
               "            )\n" +
               "            confidence = float(np.max(probabilities))\n" +
               "        \n" +
               "        return {\n" +
               "            'prediction': prediction,\n" +
               "            'confidence': confidence,\n" +
               "            'features': features\n" +
               "        }\n";
    }
    private String generateAsyncViews() {
        return "\"\"\"Async views for Quart application.\"\"\"\n\n" +
               "from quart import Blueprint, jsonify, request\n" +
               "import asyncio\n\n" +
               "async_bp = Blueprint('async_handlers', __name__)\n\n" +
               "@async_bp.route('/async/data')\n" +
               "async def get_data():\n" +
               "    \"\"\"Get data asynchronously.\"\"\"\n" +
               "    # Simulate async operation\n" +
               "    await asyncio.sleep(0.1)\n" +
               "    \n" +
               "    data = {\n" +
               "        'message': 'Data fetched asynchronously',\n" +
               "        'items': [1, 2, 3, 4, 5]\n" +
               "    }\n" +
               "    return jsonify(data)\n\n" +
               "@async_bp.route('/async/process', methods=['POST'])\n" +
               "async def process_data():\n" +
               "    \"\"\"Process data asynchronously.\"\"\"\n" +
               "    data = await request.get_json()\n" +
               "    \n" +
               "    # Simulate async processing\n" +
               "    await asyncio.sleep(0.5)\n" +
               "    \n" +
               "    result = {\n" +
               "        'status': 'processed',\n" +
               "        'data': data\n" +
               "    }\n" +
               "    return jsonify(result)\n\n" +
               "@async_bp.route('/async/parallel')\n" +
               "async def parallel_operations():\n" +
               "    \"\"\"Execute multiple operations in parallel.\"\"\"\n" +
               "    async def operation1():\n" +
               "        await asyncio.sleep(0.1)\n" +
               "        return {'op': 1, 'result': 'done'}\n" +
               "    \n" +
               "    async def operation2():\n" +
               "        await asyncio.sleep(0.1)\n" +
               "        return {'op': 2, 'result': 'done'}\n" +
               "    \n" +
               "    async def operation3():\n" +
               "        await asyncio.sleep(0.1)\n" +
               "        return {'op': 3, 'result': 'done'}\n" +
               "    \n" +
               "    # Execute all operations in parallel\n" +
               "    results = await asyncio.gather(\n" +
               "        operation1(),\n" +
               "        operation2(),\n" +
               "        operation3()\n" +
               "    )\n" +
               "    \n" +
               "    return jsonify({'results': results})\n";
    }

    private String generateAsyncTasks() {
        return "\"\"\"Async background tasks.\"\"\"\n\n" +
               "import asyncio\n" +
               "from typing import Any, Callable\n" +
               "import logging\n\n" +
               "logger = logging.getLogger(__name__)\n\n" +
               "class AsyncTaskManager:\n" +
               "    \"\"\"Manage async background tasks.\"\"\"\n" +
               "    \n" +
               "    def __init__(self):\n" +
               "        self.tasks = {}\n" +
               "    \n" +
               "    async def create_task(self, name: str, coro: Callable, *args, **kwargs):\n" +
               "        \"\"\"Create and track an async task.\"\"\"\n" +
               "        task = asyncio.create_task(coro(*args, **kwargs))\n" +
               "        self.tasks[name] = task\n" +
               "        logger.info(f'Created task: {name}')\n" +
               "        return task\n" +
               "    \n" +
               "    async def wait_for_task(self, name: str, timeout: float = None):\n" +
               "        \"\"\"Wait for a specific task to complete.\"\"\"\n" +
               "        if name not in self.tasks:\n" +
               "            raise ValueError(f'Task {name} not found')\n" +
               "        \n" +
               "        try:\n" +
               "            result = await asyncio.wait_for(self.tasks[name], timeout=timeout)\n" +
               "            logger.info(f'Task {name} completed')\n" +
               "            return result\n" +
               "        except asyncio.TimeoutError:\n" +
               "            logger.error(f'Task {name} timed out')\n" +
               "            raise\n" +
               "    \n" +
               "    async def cancel_task(self, name: str):\n" +
               "        \"\"\"Cancel a running task.\"\"\"\n" +
               "        if name in self.tasks:\n" +
               "            self.tasks[name].cancel()\n" +
               "            logger.info(f'Task {name} cancelled')\n" +
               "    \n" +
               "    async def wait_all(self):\n" +
               "        \"\"\"Wait for all tasks to complete.\"\"\"\n" +
               "        if self.tasks:\n" +
               "            await asyncio.gather(*self.tasks.values())\n" +
               "            logger.info('All tasks completed')\n\n" +
               "async def send_email_async(to: str, subject: str, body: str):\n" +
               "    \"\"\"Send email asynchronously.\"\"\"\n" +
               "    await asyncio.sleep(0.1)  # Simulate email sending\n" +
               "    logger.info(f'Email sent to {to}')\n\n" +
               "async def process_file_async(file_path: str):\n" +
               "    \"\"\"Process file asynchronously.\"\"\"\n" +
               "    await asyncio.sleep(0.5)  # Simulate file processing\n" +
               "    logger.info(f'File processed: {file_path}')\n" +
               "    return {'status': 'processed', 'file': file_path}\n";
    }

    private String generateAsyncWorkers() {
        return "\"\"\"Async worker processes.\"\"\"\n\n" +
               "import asyncio\n" +
               "import logging\n" +
               "from typing import Callable, Any\n" +
               "from queue import Queue\n\n" +
               "logger = logging.getLogger(__name__)\n\n" +
               "class AsyncWorker:\n" +
               "    \"\"\"Async worker for processing jobs.\"\"\"\n" +
               "    \n" +
               "    def __init__(self, name: str, worker_func: Callable):\n" +
               "        self.name = name\n" +
               "        self.worker_func = worker_func\n" +
               "        self.queue = asyncio.Queue()\n" +
               "        self.is_running = False\n" +
               "    \n" +
               "    async def start(self):\n" +
               "        \"\"\"Start the worker.\"\"\"\n" +
               "        self.is_running = True\n" +
               "        logger.info(f'Worker {self.name} started')\n" +
               "        \n" +
               "        while self.is_running:\n" +
               "            try:\n" +
               "                job = await asyncio.wait_for(self.queue.get(), timeout=1.0)\n" +
               "                await self.process_job(job)\n" +
               "                self.queue.task_done()\n" +
               "            except asyncio.TimeoutError:\n" +
               "                continue\n" +
               "            except Exception as e:\n" +
               "                logger.error(f'Worker {self.name} error: {e}')\n" +
               "    \n" +
               "    async def process_job(self, job: Any):\n" +
               "        \"\"\"Process a single job.\"\"\"\n" +
               "        try:\n" +
               "            result = await self.worker_func(job)\n" +
               "            logger.info(f'Worker {self.name} processed job: {result}')\n" +
               "        except Exception as e:\n" +
               "            logger.error(f'Error processing job: {e}')\n" +
               "    \n" +
               "    async def add_job(self, job: Any):\n" +
               "        \"\"\"Add job to queue.\"\"\"\n" +
               "        await self.queue.put(job)\n" +
               "        logger.debug(f'Job added to {self.name} queue')\n" +
               "    \n" +
               "    async def stop(self):\n" +
               "        \"\"\"Stop the worker.\"\"\"\n" +
               "        self.is_running = False\n" +
               "        await self.queue.join()\n" +
               "        logger.info(f'Worker {self.name} stopped')\n\n" +
               "class AsyncWorkerPool:\n" +
               "    \"\"\"Pool of async workers.\"\"\"\n" +
               "    \n" +
               "    def __init__(self, num_workers: int, worker_func: Callable):\n" +
               "        self.workers = [\n" +
               "            AsyncWorker(f'worker-{i}', worker_func)\n" +
               "            for i in range(num_workers)\n" +
               "        ]\n" +
               "    \n" +
               "    async def start(self):\n" +
               "        \"\"\"Start all workers.\"\"\"\n" +
               "        tasks = [worker.start() for worker in self.workers]\n" +
               "        await asyncio.gather(*tasks)\n" +
               "    \n" +
               "    async def submit_job(self, job: Any):\n" +
               "        \"\"\"Submit job to least busy worker.\"\"\"\n" +
               "        # Simple round-robin distribution\n" +
               "        worker = min(self.workers, key=lambda w: w.queue.qsize())\n" +
               "        await worker.add_job(job)\n" +
               "    \n" +
               "    async def stop(self):\n" +
               "        \"\"\"Stop all workers.\"\"\"\n" +
               "        for worker in self.workers:\n" +
               "            await worker.stop()\n";
    }

    private String generateGraphQLSchema() {
        return "\"\"\"GraphQL schema definition.\"\"\"\n\n" +
               "import graphene\n" +
               "from graphene_sqlalchemy import SQLAlchemyObjectType\n" +
               "from app.models.user import User as UserModel\n" +
               "from app.models.role import Role as RoleModel\n\n" +
               "class User(SQLAlchemyObjectType):\n" +
               "    \"\"\"User GraphQL type.\"\"\"\n" +
               "    class Meta:\n" +
               "        model = UserModel\n" +
               "        exclude_fields = ('password_hash',)\n\n" +
               "class Role(SQLAlchemyObjectType):\n" +
               "    \"\"\"Role GraphQL type.\"\"\"\n" +
               "    class Meta:\n" +
               "        model = RoleModel\n\n" +
               "class Query(graphene.ObjectType):\n" +
               "    \"\"\"GraphQL queries.\"\"\"\n" +
               "    users = graphene.List(User)\n" +
               "    user = graphene.Field(User, id=graphene.Int())\n" +
               "    roles = graphene.List(Role)\n" +
               "    \n" +
               "    def resolve_users(self, info):\n" +
               "        return UserModel.query.all()\n" +
               "    \n" +
               "    def resolve_user(self, info, id):\n" +
               "        return UserModel.query.get(id)\n" +
               "    \n" +
               "    def resolve_roles(self, info):\n" +
               "        return RoleModel.query.all()\n\n" +
               "class CreateUser(graphene.Mutation):\n" +
               "    \"\"\"Create user mutation.\"\"\"\n" +
               "    class Arguments:\n" +
               "        username = graphene.String(required=True)\n" +
               "        email = graphene.String(required=True)\n" +
               "        password = graphene.String(required=True)\n" +
               "    \n" +
               "    user = graphene.Field(User)\n" +
               "    \n" +
               "    def mutate(self, info, username, email, password):\n" +
               "        from app.models.base import db\n" +
               "        \n" +
               "        user_role = RoleModel.query.filter_by(name='user').first()\n" +
               "        user = UserModel(username=username, email=email, role_id=user_role.id)\n" +
               "        user.set_password(password)\n" +
               "        \n" +
               "        db.session.add(user)\n" +
               "        db.session.commit()\n" +
               "        \n" +
               "        return CreateUser(user=user)\n\n" +
               "class Mutation(graphene.ObjectType):\n" +
               "    \"\"\"GraphQL mutations.\"\"\"\n" +
               "    create_user = CreateUser.Field()\n\n" +
               "schema = graphene.Schema(query=Query, mutation=Mutation)\n";
    }

    private String generateGraphQLQueries() {
        return "\"\"\"GraphQL query implementations.\"\"\"\n\n" +
               "import graphene\n" +
               "from graphene import relay\n" +
               "from graphene_sqlalchemy import SQLAlchemyConnectionField\n" +
               "from app.graphql.schema import User, Role\n" +
               "from app.models.user import User as UserModel\n\n" +
               "class UserQuery(graphene.ObjectType):\n" +
               "    \"\"\"User-related queries.\"\"\"\n" +
               "    node = relay.Node.Field()\n" +
               "    all_users = SQLAlchemyConnectionField(User.connection)\n" +
               "    \n" +
               "    user_by_username = graphene.Field(\n" +
               "        User,\n" +
               "        username=graphene.String(required=True)\n" +
               "    )\n" +
               "    \n" +
               "    active_users = graphene.List(User)\n" +
               "    \n" +
               "    def resolve_user_by_username(self, info, username):\n" +
               "        \"\"\"Get user by username.\"\"\"\n" +
               "        return UserModel.query.filter_by(username=username).first()\n" +
               "    \n" +
               "    def resolve_active_users(self, info):\n" +
               "        \"\"\"Get all active users.\"\"\"\n" +
               "        return UserModel.query.filter_by(is_active=True).all()\n\n" +
               "class SearchQuery(graphene.ObjectType):\n" +
               "    \"\"\"Search queries.\"\"\"\n" +
               "    search_users = graphene.List(\n" +
               "        User,\n" +
               "        query=graphene.String(required=True)\n" +
               "    )\n" +
               "    \n" +
               "    def resolve_search_users(self, info, query):\n" +
               "        \"\"\"Search users by username or email.\"\"\"\n" +
               "        return UserModel.query.filter(\n" +
               "            (UserModel.username.contains(query)) |\n" +
               "            (UserModel.email.contains(query))\n" +
               "        ).all()\n";
    }

    private String generateGraphQLMutations() {
        return "\"\"\"GraphQL mutation implementations.\"\"\"\n\n" +
               "import graphene\n" +
               "from app.graphql.schema import User\n" +
               "from app.models.user import User as UserModel\n" +
               "from app.models.base import db\n\n" +
               "class UpdateUser(graphene.Mutation):\n" +
               "    \"\"\"Update user mutation.\"\"\"\n" +
               "    class Arguments:\n" +
               "        id = graphene.Int(required=True)\n" +
               "        first_name = graphene.String()\n" +
               "        last_name = graphene.String()\n" +
               "        email = graphene.String()\n" +
               "    \n" +
               "    user = graphene.Field(User)\n" +
               "    success = graphene.Boolean()\n" +
               "    \n" +
               "    def mutate(self, info, id, **kwargs):\n" +
               "        user = UserModel.query.get(id)\n" +
               "        if not user:\n" +
               "            return UpdateUser(success=False, user=None)\n" +
               "        \n" +
               "        for key, value in kwargs.items():\n" +
               "            if value is not None:\n" +
               "                setattr(user, key, value)\n" +
               "        \n" +
               "        db.session.commit()\n" +
               "        return UpdateUser(success=True, user=user)\n\n" +
               "class DeleteUser(graphene.Mutation):\n" +
               "    \"\"\"Delete user mutation.\"\"\"\n" +
               "    class Arguments:\n" +
               "        id = graphene.Int(required=True)\n" +
               "    \n" +
               "    success = graphene.Boolean()\n" +
               "    message = graphene.String()\n" +
               "    \n" +
               "    def mutate(self, info, id):\n" +
               "        user = UserModel.query.get(id)\n" +
               "        if not user:\n" +
               "            return DeleteUser(success=False, message='User not found')\n" +
               "        \n" +
               "        db.session.delete(user)\n" +
               "        db.session.commit()\n" +
               "        return DeleteUser(success=True, message='User deleted successfully')\n\n" +
               "class ToggleUserActive(graphene.Mutation):\n" +
               "    \"\"\"Toggle user active status.\"\"\"\n" +
               "    class Arguments:\n" +
               "        id = graphene.Int(required=True)\n" +
               "    \n" +
               "    user = graphene.Field(User)\n" +
               "    success = graphene.Boolean()\n" +
               "    \n" +
               "    def mutate(self, info, id):\n" +
               "        user = UserModel.query.get(id)\n" +
               "        if not user:\n" +
               "            return ToggleUserActive(success=False, user=None)\n" +
               "        \n" +
               "        user.is_active = not user.is_active\n" +
               "        db.session.commit()\n" +
               "        return ToggleUserActive(success=True, user=user)\n";
    }

    private String generateGraphQLResolvers() {
        return "\"\"\"GraphQL field resolvers.\"\"\"\n\n" +
               "from graphene import ObjectType\n" +
               "from app.models.user import User as UserModel\n" +
               "from app.models.role import Role as RoleModel\n\n" +
               "class UserResolvers:\n" +
               "    \"\"\"Resolvers for User type.\"\"\"\n" +
               "    \n" +
               "    @staticmethod\n" +
               "    def resolve_full_name(user, info):\n" +
               "        \"\"\"Resolve full name.\"\"\"\n" +
               "        if user.first_name and user.last_name:\n" +
               "            return f\"{user.first_name} {user.last_name}\"\n" +
               "        return user.username\n" +
               "    \n" +
               "    @staticmethod\n" +
               "    def resolve_role_name(user, info):\n" +
               "        \"\"\"Resolve role name.\"\"\"\n" +
               "        return user.role.name if user.role else None\n" +
               "    \n" +
               "    @staticmethod\n" +
               "    def resolve_permissions(user, info):\n" +
               "        \"\"\"Resolve user permissions.\"\"\"\n" +
               "        return user.role.permissions if user.role else []\n\n" +
               "class RoleResolvers:\n" +
               "    \"\"\"Resolvers for Role type.\"\"\"\n" +
               "    \n" +
               "    @staticmethod\n" +
               "    def resolve_user_count(role, info):\n" +
               "        \"\"\"Resolve user count for role.\"\"\"\n" +
               "        return len(role.users) if role.users else 0\n\n" +
               "def get_user_by_context(info):\n" +
               "    \"\"\"Get user from context (for authenticated requests).\"\"\"\n" +
               "    context = info.context\n" +
               "    user_id = context.get('user_id')\n" +
               "    if user_id:\n" +
               "        return UserModel.query.get(user_id)\n" +
               "    return None\n\n" +
               "def require_authentication(resolver):\n" +
               "    \"\"\"Decorator to require authentication for resolver.\"\"\"\n" +
               "    def wrapper(root, info, **kwargs):\n" +
               "        user = get_user_by_context(info)\n" +
               "        if not user:\n" +
               "            raise Exception('Authentication required')\n" +
               "        return resolver(root, info, **kwargs)\n" +
               "    return wrapper\n\n" +
               "def require_role(role_name):\n" +
               "    \"\"\"Decorator to require specific role for resolver.\"\"\"\n" +
               "    def decorator(resolver):\n" +
               "        def wrapper(root, info, **kwargs):\n" +
               "            user = get_user_by_context(info)\n" +
               "            if not user or user.role.name != role_name:\n" +
               "                raise Exception(f'Role {role_name} required')\n" +
               "            return resolver(root, info, **kwargs)\n" +
               "        return wrapper\n" +
               "    return decorator\n";
    }

    private String generateWebSocketEvents() {
        return "\"\"\"WebSocket event definitions.\"\"\"\n\n" +
               "from flask_socketio import emit, join_room, leave_room\n" +
               "from flask import request\n" +
               "import logging\n\n" +
               "logger = logging.getLogger(__name__)\n\n" +
               "class WebSocketEvents:\n" +
               "    \"\"\"WebSocket event handlers.\"\"\"\n" +
               "    \n" +
               "    @staticmethod\n" +
               "    def on_connect():\n" +
               "        \"\"\"Handle client connection.\"\"\"\n" +
               "        logger.info(f'Client connected: {request.sid}')\n" +
               "        emit('connected', {'message': 'Connected to server'})\n" +
               "    \n" +
               "    @staticmethod\n" +
               "    def on_disconnect():\n" +
               "        \"\"\"Handle client disconnection.\"\"\"\n" +
               "        logger.info(f'Client disconnected: {request.sid}')\n" +
               "    \n" +
               "    @staticmethod\n" +
               "    def on_join(data):\n" +
               "        \"\"\"Handle room join event.\"\"\"\n" +
               "        room = data.get('room')\n" +
               "        if room:\n" +
               "            join_room(room)\n" +
               "            logger.info(f'Client {request.sid} joined room: {room}')\n" +
               "            emit('joined', {'room': room}, room=room)\n" +
               "    \n" +
               "    @staticmethod\n" +
               "    def on_leave(data):\n" +
               "        \"\"\"Handle room leave event.\"\"\"\n" +
               "        room = data.get('room')\n" +
               "        if room:\n" +
               "            leave_room(room)\n" +
               "            logger.info(f'Client {request.sid} left room: {room}')\n" +
               "            emit('left', {'room': room}, room=room)\n" +
               "    \n" +
               "    @staticmethod\n" +
               "    def on_message(data):\n" +
               "        \"\"\"Handle message event.\"\"\"\n" +
               "        message = data.get('message')\n" +
               "        room = data.get('room')\n" +
               "        \n" +
               "        logger.info(f'Message from {request.sid}: {message}')\n" +
               "        \n" +
               "        if room:\n" +
               "            emit('message', data, room=room, include_self=False)\n" +
               "        else:\n" +
               "            emit('message', data, broadcast=True, include_self=False)\n" +
               "    \n" +
               "    @staticmethod\n" +
               "    def on_broadcast(data):\n" +
               "        \"\"\"Handle broadcast event.\"\"\"\n" +
               "        emit('broadcast', data, broadcast=True)\n" +
               "    \n" +
               "    @staticmethod\n" +
               "    def on_error(error):\n" +
               "        \"\"\"Handle error event.\"\"\"\n" +
               "        logger.error(f'WebSocket error: {error}')\n" +
               "        emit('error', {'message': str(error)})\n";
    }

    private String generateWebSocketHandlers() {
        return "\"\"\"WebSocket handler implementations.\"\"\"\n\n" +
               "from flask_socketio import SocketIO, emit, join_room, leave_room\n" +
               "from flask import request\n" +
               "from typing import Dict, Any\n" +
               "import logging\n\n" +
               "logger = logging.getLogger(__name__)\n\n" +
               "def register_websocket_handlers(socketio: SocketIO):\n" +
               "    \"\"\"Register WebSocket event handlers.\"\"\"\n" +
               "    \n" +
               "    @socketio.on('connect')\n" +
               "    def handle_connect():\n" +
               "        \"\"\"Handle new connection.\"\"\"\n" +
               "        logger.info(f'New connection: {request.sid}')\n" +
               "        emit('connected', {\n" +
               "            'sid': request.sid,\n" +
               "            'message': 'Successfully connected'\n" +
               "        })\n" +
               "    \n" +
               "    @socketio.on('disconnect')\n" +
               "    def handle_disconnect():\n" +
               "        \"\"\"Handle disconnection.\"\"\"\n" +
               "        logger.info(f'Client disconnected: {request.sid}')\n" +
               "    \n" +
               "    @socketio.on('join')\n" +
               "    def handle_join(data: Dict[str, Any]):\n" +
               "        \"\"\"Handle join room request.\"\"\"\n" +
               "        room = data.get('room')\n" +
               "        if room:\n" +
               "            join_room(room)\n" +
               "            emit('user_joined', {\n" +
               "                'sid': request.sid,\n" +
               "                'room': room\n" +
               "            }, room=room, include_self=False)\n" +
               "            \n" +
               "            emit('joined', {'room': room})\n" +
               "            logger.info(f'{request.sid} joined room {room}')\n" +
               "    \n" +
               "    @socketio.on('leave')\n" +
               "    def handle_leave(data: Dict[str, Any]):\n" +
               "        \"\"\"Handle leave room request.\"\"\"\n" +
               "        room = data.get('room')\n" +
               "        if room:\n" +
               "            leave_room(room)\n" +
               "            emit('user_left', {\n" +
               "                'sid': request.sid,\n" +
               "                'room': room\n" +
               "            }, room=room)\n" +
               "            \n" +
               "            emit('left', {'room': room})\n" +
               "            logger.info(f'{request.sid} left room {room}')\n" +
               "    \n" +
               "    @socketio.on('message')\n" +
               "    def handle_message(data: Dict[str, Any]):\n" +
               "        \"\"\"Handle message.\"\"\"\n" +
               "        room = data.get('room')\n" +
               "        message = data.get('message')\n" +
               "        \n" +
               "        payload = {\n" +
               "            'sid': request.sid,\n" +
               "            'message': message\n" +
               "        }\n" +
               "        \n" +
               "        if room:\n" +
               "            emit('message', payload, room=room, include_self=False)\n" +
               "        else:\n" +
               "            emit('message', payload, broadcast=True, include_self=False)\n" +
               "    \n" +
               "    @socketio.on('ping')\n" +
               "    def handle_ping():\n" +
               "        \"\"\"Handle ping request.\"\"\"\n" +
               "        emit('pong', {'timestamp': socketio.server.get_time()})\n";
    }

    private String generateWebSocketRooms() {
        return "\"\"\"WebSocket room management.\"\"\"\n\n" +
               "from typing import Set, Dict, List\n" +
               "import logging\n\n" +
               "logger = logging.getLogger(__name__)\n\n" +
               "class RoomManager:\n" +
               "    \"\"\"Manage WebSocket rooms and participants.\"\"\"\n" +
               "    \n" +
               "    def __init__(self):\n" +
               "        self.rooms: Dict[str, Set[str]] = {}\n" +
               "        self.user_rooms: Dict[str, Set[str]] = {}\n" +
               "    \n" +
               "    def create_room(self, room_name: str):\n" +
               "        \"\"\"Create a new room.\"\"\"\n" +
               "        if room_name not in self.rooms:\n" +
               "            self.rooms[room_name] = set()\n" +
               "            logger.info(f'Room created: {room_name}')\n" +
               "    \n" +
               "    def join_room(self, room_name: str, sid: str):\n" +
               "        \"\"\"Add user to room.\"\"\"\n" +
               "        if room_name not in self.rooms:\n" +
               "            self.create_room(room_name)\n" +
               "        \n" +
               "        self.rooms[room_name].add(sid)\n" +
               "        \n" +
               "        if sid not in self.user_rooms:\n" +
               "            self.user_rooms[sid] = set()\n" +
               "        self.user_rooms[sid].add(room_name)\n" +
               "        \n" +
               "        logger.info(f'User {sid} joined room {room_name}')\n" +
               "    \n" +
               "    def leave_room(self, room_name: str, sid: str):\n" +
               "        \"\"\"Remove user from room.\"\"\"\n" +
               "        if room_name in self.rooms:\n" +
               "            self.rooms[room_name].discard(sid)\n" +
               "            \n" +
               "            # Remove empty rooms\n" +
               "            if not self.rooms[room_name]:\n" +
               "                del self.rooms[room_name]\n" +
               "                logger.info(f'Room deleted: {room_name}')\n" +
               "        \n" +
               "        if sid in self.user_rooms:\n" +
               "            self.user_rooms[sid].discard(room_name)\n" +
               "            \n" +
               "            if not self.user_rooms[sid]:\n" +
               "                del self.user_rooms[sid]\n" +
               "        \n" +
               "        logger.info(f'User {sid} left room {room_name}')\n" +
               "    \n" +
               "    def leave_all_rooms(self, sid: str):\n" +
               "        \"\"\"Remove user from all rooms.\"\"\"\n" +
               "        if sid in self.user_rooms:\n" +
               "            rooms = list(self.user_rooms[sid])\n" +
               "            for room in rooms:\n" +
               "                self.leave_room(room, sid)\n" +
               "    \n" +
               "    def get_room_members(self, room_name: str) -> List[str]:\n" +
               "        \"\"\"Get list of room members.\"\"\"\n" +
               "        return list(self.rooms.get(room_name, set()))\n" +
               "    \n" +
               "    def get_user_rooms(self, sid: str) -> List[str]:\n" +
               "        \"\"\"Get list of rooms user is in.\"\"\"\n" +
               "        return list(self.user_rooms.get(sid, set()))\n" +
               "    \n" +
               "    def get_room_count(self, room_name: str) -> int:\n" +
               "        \"\"\"Get number of users in room.\"\"\"\n" +
               "        return len(self.rooms.get(room_name, set()))\n" +
               "    \n" +
               "    def get_all_rooms(self) -> List[str]:\n" +
               "        \"\"\"Get list of all rooms.\"\"\"\n" +
               "        return list(self.rooms.keys())\n\n" +
               "# Global room manager instance\n" +
               "room_manager = RoomManager()\n";
    }
    private String generateRequirementsDev() {
        return "# Development dependencies\n\n" +
               "# Testing\n" +
               "pytest==7.4.3\n" +
               "pytest-flask==1.3.0\n" +
               "pytest-cov==4.1.0\n" +
               "pytest-asyncio==0.21.1\n" +
               "factory-boy==3.3.0\n" +
               "faker==20.1.0\n" +
               "coverage==7.3.2\n\n" +
               "# Code quality\n" +
               "black==23.11.0\n" +
               "flake8==6.1.0\n" +
               "pylint==3.0.2\n" +
               "mypy==1.7.1\n" +
               "isort==5.12.0\n\n" +
               "# Documentation\n" +
               "sphinx==7.2.6\n" +
               "sphinx-rtd-theme==2.0.0\n\n" +
               "# Development tools\n" +
               "ipython==8.18.1\n" +
               "ipdb==0.13.13\n" +
               "watchdog==3.0.0\n" +
               "python-dotenv==1.0.0\n\n" +
               "# Pre-commit hooks\n" +
               "pre-commit==3.5.0\n";
    }

    private String generateEnvExample(FlaskProjectType type) {
        return "# Flask Configuration\n" +
               "FLASK_APP=wsgi.py\n" +
               "FLASK_ENV=development\n" +
               "SECRET_KEY=your-secret-key-here\n\n" +
               "# Database\n" +
               "DATABASE_URL=sqlite:///app.db\n" +
               "# DATABASE_URL=postgresql://user:password@localhost/dbname\n\n" +
               "# JWT\n" +
               "JWT_SECRET_KEY=your-jwt-secret-key-here\n\n" +
               "# Redis\n" +
               "REDIS_URL=redis://localhost:6379/0\n\n" +
               "# Email Configuration\n" +
               "MAIL_SERVER=smtp.gmail.com\n" +
               "MAIL_PORT=587\n" +
               "MAIL_USE_TLS=true\n" +
               "MAIL_USERNAME=your-email@gmail.com\n" +
               "MAIL_PASSWORD=your-app-password\n" +
               "MAIL_DEFAULT_SENDER=noreply@example.com\n\n" +
               "# API Keys\n" +
               "API_KEY=your-api-key-here\n\n" +
               "# Application Settings\n" +
               "DEBUG=True\n" +
               "PORT=5000\n" +
               "HOST=0.0.0.0\n" +
               "ITEMS_PER_PAGE=20\n" +
               "MAX_CONTENT_LENGTH=16777216\n\n" +
               (type == FlaskProjectType.DATA_API ? 
               "# ML Model Settings\n" +
               "MODEL_PATH=models/\n" +
               "MODEL_VERSION=1.0.0\n\n" : "") +
               (type == FlaskProjectType.WEBSOCKET_APP ?
               "# WebSocket Settings\n" +
               "SOCKETIO_MESSAGE_QUEUE=redis://localhost:6379/0\n\n" : "");
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
               "# Virtual Environment\n" +
               "venv/\n" +
               "ENV/\n" +
               "env/\n\n" +
               "# Flask\n" +
               "instance/\n" +
               ".webassets-cache\n\n" +
               "# Database\n" +
               "*.db\n" +
               "*.sqlite\n" +
               "*.sqlite3\n\n" +
               "# Environment variables\n" +
               ".env\n" +
               ".env.local\n" +
               ".flaskenv\n\n" +
               "# IDEs\n" +
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
               "# Logs\n" +
               "*.log\n" +
               "logs/\n\n" +
               "# OS\n" +
               ".DS_Store\n" +
               "Thumbs.db\n\n" +
               "# Uploads\n" +
               "uploads/\n" +
               "media/\n";
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
               "  | build\n" +
               "  | dist\n" +
               ")/\n" +
               "'''\n\n" +
               "[tool.isort]\n" +
               "profile = \"black\"\n" +
               "line_length = 88\n" +
               "multi_line_output = 3\n" +
               "include_trailing_comma = true\n\n" +
               "[tool.pytest.ini_options]\n" +
               "testpaths = [\"tests\"]\n" +
               "python_files = \"test_*.py\"\n" +
               "python_classes = \"Test*\"\n" +
               "python_functions = \"test_*\"\n" +
               "addopts = \"-v --cov=app --cov-report=html --cov-report=term\"\n\n" +
               "[tool.mypy]\n" +
               "python_version = \"3.11\"\n" +
               "warn_return_any = true\n" +
               "warn_unused_configs = true\n" +
               "disallow_untyped_defs = false\n\n" +
               "[build-system]\n" +
               "requires = [\"setuptools>=61.0\", \"wheel\"]\n" +
               "build-backend = \"setuptools.build_meta\"\n\n" +
               "[project]\n" +
               "name = \"" + projectName.toLowerCase().replace(" ", "-") + "\"\n" +
               "version = \"0.1.0\"\n" +
               "description = \"Flask application\"\n" +
               "requires-python = \">=3.11\"\n";
    }

    private String generateSetupCfg() {
        return "[flake8]\n" +
               "max-line-length = 88\n" +
               "extend-ignore = E203, E501, W503\n" +
               "exclude =\n" +
               "    .git,\n" +
               "    __pycache__,\n" +
               "    build,\n" +
               "    dist,\n" +
               "    venv,\n" +
               "    .venv\n\n" +
               "[pylint]\n" +
               "max-line-length = 88\n" +
               "disable =\n" +
               "    C0111,\n" +
               "    C0103,\n" +
               "    R0903\n\n" +
               "[coverage:run]\n" +
               "source = app\n" +
               "omit =\n" +
               "    */tests/*\n" +
               "    */venv/*\n" +
               "    */__pycache__/*\n\n" +
               "[coverage:report]\n" +
               "precision = 2\n" +
               "show_missing = true\n";
    }

    private String generateSetupPy(String projectName) {
        return "\"\"\"Setup script for " + projectName + ".\"\"\"\n\n" +
               "from setuptools import setup, find_packages\n\n" +
               "with open('README.md', 'r', encoding='utf-8') as f:\n" +
               "    long_description = f.read()\n\n" +
               "with open('requirements.txt', 'r', encoding='utf-8') as f:\n" +
               "    requirements = [line.strip() for line in f if line.strip() and not line.startswith('#')]\n\n" +
               "setup(\n" +
               "    name='" + projectName.toLowerCase().replace(" ", "-") + "',\n" +
               "    version='0.1.0',\n" +
               "    author='Your Name',\n" +
               "    author_email='your.email@example.com',\n" +
               "    description='Flask application',\n" +
               "    long_description=long_description,\n" +
               "    long_description_content_type='text/markdown',\n" +
               "    url='https://github.com/yourusername/" + projectName.toLowerCase().replace(" ", "-") + "',\n" +
               "    packages=find_packages(),\n" +
               "    classifiers=[\n" +
               "        'Development Status :: 3 - Alpha',\n" +
               "        'Intended Audience :: Developers',\n" +
               "        'License :: OSI Approved :: MIT License',\n" +
               "        'Programming Language :: Python :: 3.11',\n" +
               "    ],\n" +
               "    python_requires='>=3.11',\n" +
               "    install_requires=requirements,\n" +
               ")\n";
    }

    private String generatePytestIni() {
        return "[pytest]\n" +
               "testpaths = tests\n" +
               "python_files = test_*.py\n" +
               "python_classes = Test*\n" +
               "python_functions = test_*\n" +
               "addopts =\n" +
               "    -v\n" +
               "    --strict-markers\n" +
               "    --cov=app\n" +
               "    --cov-report=html\n" +
               "    --cov-report=term-missing:skip-covered\n" +
               "    --cov-fail-under=80\n" +
               "markers =\n" +
               "    unit: Unit tests\n" +
               "    integration: Integration tests\n" +
               "    slow: Slow running tests\n";
    }

    private String generateFlaskenv() {
        return "FLASK_APP=wsgi.py\n" +
               "FLASK_ENV=development\n" +
               "FLASK_DEBUG=1\n";
    }

    private String generateReadme(String projectName, FlaskProjectType type) {
        return "# " + projectName + "\n\n" +
               "A " + type.getDescription() + " built with Flask.\n\n" +
               "## Features\n\n" +
               "- RESTful API with JWT authentication\n" +
               "- User management and role-based access control\n" +
               "- Database integration with SQLAlchemy\n" +
               "- Redis caching and session management\n" +
               "- Celery for background tasks\n" +
               "- Comprehensive test suite\n" +
               "- Docker support\n" +
               "- CI/CD with GitHub Actions\n\n" +
               "## Requirements\n\n" +
               "- Python 3.11+\n" +
               "- PostgreSQL (or SQLite for development)\n" +
               "- Redis\n\n" +
               "## Installation\n\n" +
               "1. Clone the repository:\n" +
               "```bash\n" +
               "git clone <repository-url>\n" +
               "cd " + projectName.toLowerCase().replace(" ", "-") + "\n" +
               "```\n\n" +
               "2. Create and activate virtual environment:\n" +
               "```bash\n" +
               "python -m venv venv\n" +
               "source venv/bin/activate  # On Windows: venv\\Scripts\\activate\n" +
               "```\n\n" +
               "3. Install dependencies:\n" +
               "```bash\n" +
               "pip install -r requirements.txt\n" +
               "pip install -r requirements-dev.txt  # For development\n" +
               "```\n\n" +
               "4. Set up environment variables:\n" +
               "```bash\n" +
               "cp .env.example .env\n" +
               "# Edit .env with your configuration\n" +
               "```\n\n" +
               "5. Initialize the database:\n" +
               "```bash\n" +
               "python scripts/init_db.py\n" +
               "python scripts/seed_db.py  # Optional: add sample data\n" +
               "```\n\n" +
               "## Running the Application\n\n" +
               "### Development\n" +
               "```bash\n" +
               "flask run\n" +
               "# Or\n" +
               "python run.py\n" +
               "```\n\n" +
               "### Production\n" +
               "```bash\n" +
               "gunicorn -w 4 -b 0.0.0.0:5000 wsgi:app\n" +
               "```\n\n" +
               "### Using Docker\n" +
               "```bash\n" +
               "docker-compose up\n" +
               "```\n\n" +
               "## Testing\n\n" +
               "Run tests:\n" +
               "```bash\n" +
               "pytest\n" +
               "```\n\n" +
               "Run tests with coverage:\n" +
               "```bash\n" +
               "pytest --cov=app --cov-report=html\n" +
               "```\n\n" +
               "## API Documentation\n\n" +
               "API documentation is available at `/docs` when running the application.\n\n" +
               "See [docs/api.md](docs/api.md) for detailed API documentation.\n\n" +
               "## Project Structure\n\n" +
               "```\n" +
               projectName + "/\n" +
               "├── app/                    # Application package\n" +
               "│   ├── __init__.py        # App factory\n" +
               "│   ├── config.py          # Configuration\n" +
               "│   ├── models/            # Database models\n" +
               "│   ├── api/               # API resources\n" +
               "│   ├── views/             # Web views\n" +
               "│   └── utils/             # Utilities\n" +
               "├── tests/                 # Test suite\n" +
               "├── scripts/               # Utility scripts\n" +
               "├── docs/                  # Documentation\n" +
               "├── docker/                # Docker configuration\n" +
               "├── .github/               # GitHub Actions\n" +
               "├── requirements.txt       # Dependencies\n" +
               "├── wsgi.py                # WSGI entry point\n" +
               "└── README.md\n" +
               "```\n\n" +
               "## Contributing\n\n" +
               "1. Fork the repository\n" +
               "2. Create a feature branch\n" +
               "3. Make your changes\n" +
               "4. Run tests\n" +
               "5. Submit a pull request\n\n" +
               "## License\n\n" +
               "MIT License\n";
    }

    private String generateMakefile() {
        return ".PHONY: help install run test clean\n\n" +
               "help:\n" +
               "\t@echo \"Available commands:\"\n" +
               "\t@echo \"  make install    - Install dependencies\"\n" +
               "\t@echo \"  make run        - Run development server\"\n" +
               "\t@echo \"  make test       - Run tests\"\n" +
               "\t@echo \"  make lint       - Run linters\"\n" +
               "\t@echo \"  make format     - Format code\"\n" +
               "\t@echo \"  make clean      - Clean up generated files\"\n" +
               "\t@echo \"  make docker     - Build and run with Docker\"\n\n" +
               "install:\n" +
               "\tpip install -r requirements.txt\n" +
               "\tpip install -r requirements-dev.txt\n\n" +
               "run:\n" +
               "\tflask run\n\n" +
               "test:\n" +
               "\tpytest\n\n" +
               "coverage:\n" +
               "\tpytest --cov=app --cov-report=html\n\n" +
               "lint:\n" +
               "\tflake8 app tests\n" +
               "\tpylint app\n" +
               "\tmypy app\n\n" +
               "format:\n" +
               "\tblack app tests\n" +
               "\tisort app tests\n\n" +
               "clean:\n" +
               "\tfind . -type f -name '*.pyc' -delete\n" +
               "\tfind . -type d -name '__pycache__' -delete\n" +
               "\trm -rf .pytest_cache\n" +
               "\trm -rf htmlcov\n" +
               "\trm -rf .coverage\n" +
               "\trm -rf build dist *.egg-info\n\n" +
               "init-db:\n" +
               "\tpython scripts/init_db.py\n\n" +
               "seed-db:\n" +
               "\tpython scripts/seed_db.py\n\n" +
               "docker:\n" +
               "\tdocker-compose up --build\n\n" +
               "docker-down:\n" +
               "\tdocker-compose down\n";
    }

    private String generateDockerfile(FlaskProjectType type) {
        return "FROM python:3.11-slim\n\n" +
               "# Set working directory\n" +
               "WORKDIR /app\n\n" +
               "# Install system dependencies\n" +
               "RUN apt-get update && apt-get install -y \\\n" +
               "    gcc \\\n" +
               "    postgresql-client \\\n" +
               "    && rm -rf /var/lib/apt/lists/*\n\n" +
               "# Copy requirements first for better caching\n" +
               "COPY requirements.txt .\n" +
               "RUN pip install --no-cache-dir -r requirements.txt\n\n" +
               "# Copy application code\n" +
               "COPY . .\n\n" +
               "# Create necessary directories\n" +
               "RUN mkdir -p logs uploads instance\n\n" +
               "# Set environment variables\n" +
               "ENV FLASK_APP=wsgi.py\n" +
               "ENV PYTHONUNBUFFERED=1\n\n" +
               "# Expose port\n" +
               "EXPOSE 5000\n\n" +
               "# Copy and set entrypoint\n" +
               "COPY docker/entrypoint.sh /entrypoint.sh\n" +
               "RUN chmod +x /entrypoint.sh\n\n" +
               "ENTRYPOINT [\"/entrypoint.sh\"]\n" +
               "CMD [\"gunicorn\", \"-w\", \"4\", \"-b\", \"0.0.0.0:5000\", \"wsgi:app\"]\n";
    }

    private String generateDockerCompose(String projectName, FlaskProjectType type) {
        return "version: '3.9'\n\n" +
               "services:\n" +
               "  web:\n" +
               "    build: .\n" +
               "    container_name: " + projectName.toLowerCase().replace(" ", "_") + "_web\n" +
               "    ports:\n" +
               "      - \"5000:5000\"\n" +
               "    environment:\n" +
               "      - FLASK_ENV=production\n" +
               "      - DATABASE_URL=postgresql://postgres:postgres@db:5432/" + projectName.toLowerCase().replace(" ", "_") + "\n" +
               "      - REDIS_URL=redis://redis:6379/0\n" +
               "    depends_on:\n" +
               "      - db\n" +
               "      - redis\n" +
               "    volumes:\n" +
               "      - ./uploads:/app/uploads\n" +
               "      - ./logs:/app/logs\n" +
               "    restart: unless-stopped\n\n" +
               "  db:\n" +
               "    image: postgres:15-alpine\n" +
               "    container_name: " + projectName.toLowerCase().replace(" ", "_") + "_db\n" +
               "    environment:\n" +
               "      - POSTGRES_USER=postgres\n" +
               "      - POSTGRES_PASSWORD=postgres\n" +
               "      - POSTGRES_DB=" + projectName.toLowerCase().replace(" ", "_") + "\n" +
               "    volumes:\n" +
               "      - postgres_data:/var/lib/postgresql/data\n" +
               "    ports:\n" +
               "      - \"5432:5432\"\n" +
               "    restart: unless-stopped\n\n" +
               "  redis:\n" +
               "    image: redis:7-alpine\n" +
               "    container_name: " + projectName.toLowerCase().replace(" ", "_") + "_redis\n" +
               "    ports:\n" +
               "      - \"6379:6379\"\n" +
               "    restart: unless-stopped\n\n" +
               "  nginx:\n" +
               "    image: nginx:alpine\n" +
               "    container_name: " + projectName.toLowerCase().replace(" ", "_") + "_nginx\n" +
               "    ports:\n" +
               "      - \"80:80\"\n" +
               "    volumes:\n" +
               "      - ./docker/nginx.conf:/etc/nginx/nginx.conf:ro\n" +
               "    depends_on:\n" +
               "      - web\n" +
               "    restart: unless-stopped\n\n" +
               "volumes:\n" +
               "  postgres_data:\n";
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
               ".flaskenv\n" +
               "venv/\n" +
               "ENV/\n" +
               "env/\n" +
               ".venv/\n" +
               ".pytest_cache/\n" +
               ".coverage\n" +
               "htmlcov/\n" +
               "*.db\n" +
               "*.sqlite\n" +
               "*.sqlite3\n" +
               "logs/\n" +
               "*.log\n" +
               ".vscode/\n" +
               ".idea/\n" +
               "README.md\n" +
               "docker-compose.yml\n";
    }

    private String generateNginxConfig(String projectName) {
        return "events {\n" +
               "    worker_connections 1024;\n" +
               "}\n\n" +
               "http {\n" +
               "    upstream flask_app {\n" +
               "        server web:5000;\n" +
               "    }\n\n" +
               "    server {\n" +
               "        listen 80;\n" +
               "        server_name localhost;\n" +
               "        client_max_body_size 16M;\n\n" +
               "        location / {\n" +
               "            proxy_pass http://flask_app;\n" +
               "            proxy_set_header Host $host;\n" +
               "            proxy_set_header X-Real-IP $remote_addr;\n" +
               "            proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;\n" +
               "            proxy_set_header X-Forwarded-Proto $scheme;\n" +
               "            proxy_redirect off;\n" +
               "            proxy_buffering off;\n" +
               "        }\n\n" +
               "        location /static {\n" +
               "            alias /app/static;\n" +
               "            expires 30d;\n" +
               "            add_header Cache-Control \"public, immutable\";\n" +
               "        }\n\n" +
               "        location /health {\n" +
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
               "flask db upgrade || echo \"No migrations to run\"\n\n" +
               "echo \"Initializing database...\"\n" +
               "python scripts/init_db.py || echo \"Database already initialized\"\n\n" +
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
               "    runs-on: ubuntu-latest\n" +
               "    \n" +
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
               "          - 5432:5432\n" +
               "      \n" +
               "      redis:\n" +
               "        image: redis:7-alpine\n" +
               "        options: >-\n" +
               "          --health-cmd \"redis-cli ping\"\n" +
               "          --health-interval 10s\n" +
               "          --health-timeout 5s\n" +
               "          --health-retries 5\n" +
               "        ports:\n" +
               "          - 6379:6379\n" +
               "    \n" +
               "    steps:\n" +
               "    - uses: actions/checkout@v3\n" +
               "    \n" +
               "    - name: Set up Python\n" +
               "      uses: actions/setup-python@v4\n" +
               "      with:\n" +
               "        python-version: '3.11'\n" +
               "    \n" +
               "    - name: Cache dependencies\n" +
               "      uses: actions/cache@v3\n" +
               "      with:\n" +
               "        path: ~/.cache/pip\n" +
               "        key: ${{ runner.os }}-pip-${{ hashFiles('**/requirements.txt') }}\n" +
               "    \n" +
               "    - name: Install dependencies\n" +
               "      run: |\n" +
               "        python -m pip install --upgrade pip\n" +
               "        pip install -r requirements.txt\n" +
               "        pip install -r requirements-dev.txt\n" +
               "    \n" +
               "    - name: Lint with flake8\n" +
               "      run: |\n" +
               "        flake8 app tests\n" +
               "    \n" +
               "    - name: Check code formatting\n" +
               "      run: |\n" +
               "        black --check app tests\n" +
               "    \n" +
               "    - name: Run tests\n" +
               "      env:\n" +
               "        DATABASE_URL: postgresql://postgres:postgres@localhost:5432/test_db\n" +
               "        REDIS_URL: redis://localhost:6379/0\n" +
               "      run: |\n" +
               "        pytest --cov=app --cov-report=xml\n" +
               "    \n" +
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
               "    runs-on: ubuntu-latest\n" +
               "    \n" +
               "    steps:\n" +
               "    - uses: actions/checkout@v3\n" +
               "    \n" +
               "    - name: Set up Docker Buildx\n" +
               "      uses: docker/setup-buildx-action@v2\n" +
               "    \n" +
               "    - name: Login to Docker Hub\n" +
               "      uses: docker/login-action@v2\n" +
               "      with:\n" +
               "        username: ${{ secrets.DOCKER_USERNAME }}\n" +
               "        password: ${{ secrets.DOCKER_PASSWORD }}\n" +
               "    \n" +
               "    - name: Build and push\n" +
               "      uses: docker/build-push-action@v4\n" +
               "      with:\n" +
               "        context: .\n" +
               "        push: true\n" +
               "        tags: ${{ secrets.DOCKER_USERNAME }}/" + projectName.toLowerCase().replace(" ", "-") + ":latest\n" +
               "        cache-from: type=registry,ref=${{ secrets.DOCKER_USERNAME }}/" + projectName.toLowerCase().replace(" ", "-") + ":buildcache\n" +
               "        cache-to: type=registry,ref=${{ secrets.DOCKER_USERNAME }}/" + projectName.toLowerCase().replace(" ", "-") + ":buildcache,mode=max\n" +
               "    \n" +
               "    - name: Deploy to production\n" +
               "      run: |\n" +
               "        echo \"Add deployment commands here\"\n" +
               "        # Example: SSH to server and pull new image\n" +
               "        # ssh user@server 'docker-compose pull && docker-compose up -d'\n";
    }
}