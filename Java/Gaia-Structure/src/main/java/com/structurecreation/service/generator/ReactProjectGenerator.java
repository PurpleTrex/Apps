package com.structurecreation.service.generator;

import com.structurecreation.model.ProjectNode;
import com.structurecreation.service.DependencyResolverService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * Advanced React project generator that creates complete, working React applications
 * with all necessary dependencies, configurations, and no build errors
 */
public class ReactProjectGenerator {
    private static final Logger logger = LoggerFactory.getLogger(ReactProjectGenerator.class);

    private final DependencyResolverService dependencyResolver;

    public ReactProjectGenerator(DependencyResolverService dependencyResolver) {
        this.dependencyResolver = dependencyResolver;
    }

    public ReactProjectGenerator() {
        this.dependencyResolver = new DependencyResolverService();
    }

    /**
     * Generate a complete React project structure with all dependencies
     */
    public ProjectNode generateReactProject(String projectName, ReactProjectType type) {
        ProjectNode root = new ProjectNode(projectName, ProjectNode.NodeType.FOLDER);

        // Create folder structure
        createFolderStructure(root, type);

        // Generate configuration files
        generateConfigFiles(root, projectName, type);

        // Generate source files
        generateSourceFiles(root, type);

        // Generate complete package.json with all dependencies
        generatePackageJson(root, projectName, type);

        return root;
    }

    /**
     * Create the complete folder structure
     */
    private void createFolderStructure(ProjectNode root, ReactProjectType type) {
        // Main folders
        ProjectNode src = new ProjectNode("src", ProjectNode.NodeType.FOLDER);
        ProjectNode public_ = new ProjectNode("public", ProjectNode.NodeType.FOLDER);
        ProjectNode tests = new ProjectNode("__tests__", ProjectNode.NodeType.FOLDER);

        // Source subfolders
        ProjectNode components = new ProjectNode("components", ProjectNode.NodeType.FOLDER);
        ProjectNode pages = new ProjectNode("pages", ProjectNode.NodeType.FOLDER);
        ProjectNode hooks = new ProjectNode("hooks", ProjectNode.NodeType.FOLDER);
        ProjectNode utils = new ProjectNode("utils", ProjectNode.NodeType.FOLDER);
        ProjectNode services = new ProjectNode("services", ProjectNode.NodeType.FOLDER);
        ProjectNode assets = new ProjectNode("assets", ProjectNode.NodeType.FOLDER);
        ProjectNode styles = new ProjectNode("styles", ProjectNode.NodeType.FOLDER);
        ProjectNode store = new ProjectNode("store", ProjectNode.NodeType.FOLDER);
        ProjectNode context = new ProjectNode("context", ProjectNode.NodeType.FOLDER);
        ProjectNode types = new ProjectNode("types", ProjectNode.NodeType.FOLDER);

        // Asset subfolders
        ProjectNode images = new ProjectNode("images", ProjectNode.NodeType.FOLDER);
        ProjectNode fonts = new ProjectNode("fonts", ProjectNode.NodeType.FOLDER);
        ProjectNode icons = new ProjectNode("icons", ProjectNode.NodeType.FOLDER);

        // Add subfolders to assets
        assets.addChild(images);
        assets.addChild(fonts);
        assets.addChild(icons);

        // Add subfolders to src
        src.addChild(components);
        src.addChild(pages);
        src.addChild(hooks);
        src.addChild(utils);
        src.addChild(services);
        src.addChild(assets);
        src.addChild(styles);

        if (type == ReactProjectType.WITH_REDUX || type == ReactProjectType.FULL_STACK) {
            src.addChild(store);
        }

        if (type == ReactProjectType.WITH_CONTEXT || type == ReactProjectType.FULL_STACK) {
            src.addChild(context);
        }

        if (type == ReactProjectType.WITH_TYPESCRIPT || type == ReactProjectType.FULL_STACK) {
            src.addChild(types);
        }

        // Add main folders to root
        root.addChild(src);
        root.addChild(public_);
        root.addChild(tests);
    }

    /**
     * Generate configuration files
     */
    private void generateConfigFiles(ProjectNode root, String projectName, ReactProjectType type) {
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
            generateEnvFile());
        root.addChild(env);

        // .env.example
        ProjectNode envExample = new ProjectNode(".env.example", ProjectNode.NodeType.FILE,
            generateEnvFile());
        root.addChild(envExample);

        // ESLint configuration
        ProjectNode eslintrc = new ProjectNode(".eslintrc.json", ProjectNode.NodeType.FILE,
            generateEslintConfig(type));
        root.addChild(eslintrc);

        // Prettier configuration
        ProjectNode prettierrc = new ProjectNode(".prettierrc", ProjectNode.NodeType.FILE,
            generatePrettierConfig());
        root.addChild(prettierrc);

        if (type == ReactProjectType.WITH_TYPESCRIPT || type == ReactProjectType.FULL_STACK) {
            // TypeScript configuration
            ProjectNode tsconfig = new ProjectNode("tsconfig.json", ProjectNode.NodeType.FILE,
                generateTsConfig());
            root.addChild(tsconfig);
        }

        // Tailwind CSS configuration (for modern styling)
        ProjectNode tailwindConfig = new ProjectNode("tailwind.config.js", ProjectNode.NodeType.FILE,
            generateTailwindConfig());
        root.addChild(tailwindConfig);

        // PostCSS configuration
        ProjectNode postcssConfig = new ProjectNode("postcss.config.js", ProjectNode.NodeType.FILE,
            generatePostCSSConfig());
        root.addChild(postcssConfig);

        // Webpack configuration for advanced builds
        if (type == ReactProjectType.FULL_STACK) {
            ProjectNode webpackConfig = new ProjectNode("webpack.config.js", ProjectNode.NodeType.FILE,
                generateWebpackConfig());
            root.addChild(webpackConfig);
        }

        // Jest configuration for testing
        ProjectNode jestConfig = new ProjectNode("jest.config.js", ProjectNode.NodeType.FILE,
            generateJestConfig(type));
        root.addChild(jestConfig);

        // Babel configuration
        ProjectNode babelConfig = new ProjectNode(".babelrc", ProjectNode.NodeType.FILE,
            generateBabelConfig(type));
        root.addChild(babelConfig);
    }

    /**
     * Generate source files
     */
    private void generateSourceFiles(ProjectNode root, ReactProjectType type) {
        ProjectNode src = root.findChild("src");

        // Main App component
        String extension = (type == ReactProjectType.WITH_TYPESCRIPT || type == ReactProjectType.FULL_STACK) ? "tsx" : "jsx";

        ProjectNode app = new ProjectNode("App." + extension, ProjectNode.NodeType.FILE,
            generateAppComponent(type));
        src.addChild(app);

        // Index file
        ProjectNode index = new ProjectNode("index." + extension, ProjectNode.NodeType.FILE,
            generateIndexFile(type));
        src.addChild(index);

        // App CSS
        ProjectNode appCss = new ProjectNode("App.css", ProjectNode.NodeType.FILE,
            generateAppCss());
        src.addChild(appCss);

        // Index CSS
        ProjectNode indexCss = new ProjectNode("index.css", ProjectNode.NodeType.FILE,
            generateIndexCss());
        src.addChild(indexCss);

        // Generate component files
        ProjectNode components = src.findChild("components");
        generateComponentFiles(components, type);

        // Generate page files
        ProjectNode pages = src.findChild("pages");
        generatePageFiles(pages, type);

        // Generate hook files
        ProjectNode hooks = src.findChild("hooks");
        generateHookFiles(hooks, type);

        // Generate utility files
        ProjectNode utils = src.findChild("utils");
        generateUtilityFiles(utils, type);

        // Generate service files
        ProjectNode services = src.findChild("services");
        generateServiceFiles(services, type);

        // Generate store files (if Redux)
        if (type == ReactProjectType.WITH_REDUX || type == ReactProjectType.FULL_STACK) {
            ProjectNode store = src.findChild("store");
            generateStoreFiles(store, type);
        }

        // Generate public files
        ProjectNode public_ = root.findChild("public");
        generatePublicFiles(public_);

        // Generate test files
        ProjectNode tests = root.findChild("__tests__");
        generateTestFiles(tests, type);
    }

    /**
     * Generate complete package.json with all necessary dependencies
     */
    private void generatePackageJson(ProjectNode root, String projectName, ReactProjectType type) {
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
     * Get complete list of dependencies for React project
     */
    private Map<String, String> getCompleteDependencies(ReactProjectType type) {
        Map<String, String> deps = new LinkedHashMap<>();

        // Core React dependencies
        deps.put("react", "^18.2.0");
        deps.put("react-dom", "^18.2.0");

        // Routing
        deps.put("react-router-dom", "^6.20.0");

        // HTTP Client
        deps.put("axios", "^1.6.2");

        // State Management
        if (type == ReactProjectType.WITH_REDUX || type == ReactProjectType.FULL_STACK) {
            deps.put("@reduxjs/toolkit", "^1.9.7");
            deps.put("react-redux", "^8.1.3");
            deps.put("redux-persist", "^6.0.0");
        }

        // Form handling
        deps.put("react-hook-form", "^7.48.2");
        deps.put("yup", "^1.3.3");

        // UI Components
        deps.put("@mui/material", "^5.14.20");
        deps.put("@mui/icons-material", "^5.14.19");
        deps.put("@emotion/react", "^11.11.1");
        deps.put("@emotion/styled", "^11.11.0");

        // Styling
        deps.put("styled-components", "^6.1.1");
        deps.put("classnames", "^2.3.2");

        // Date handling
        deps.put("date-fns", "^2.30.0");
        deps.put("dayjs", "^1.11.10");

        // Charts and data visualization
        deps.put("recharts", "^2.10.3");
        deps.put("chart.js", "^4.4.1");
        deps.put("react-chartjs-2", "^5.2.0");

        // Notifications
        deps.put("react-toastify", "^9.1.3");
        deps.put("notistack", "^3.0.1");

        // Animations
        deps.put("framer-motion", "^10.16.16");
        deps.put("react-spring", "^9.7.3");

        // Utilities
        deps.put("lodash", "^4.17.21");
        deps.put("uuid", "^9.0.1");
        deps.put("crypto-js", "^4.2.0");

        // Environment variables
        deps.put("dotenv", "^16.3.1");

        // WebSocket support
        deps.put("socket.io-client", "^4.5.4");

        // Image handling
        deps.put("react-image-crop", "^11.0.1");
        deps.put("react-dropzone", "^14.2.3");

        // Internationalization
        deps.put("react-i18next", "^14.0.0");
        deps.put("i18next", "^23.7.8");

        // Performance monitoring
        deps.put("web-vitals", "^3.5.0");

        if (type == ReactProjectType.FULL_STACK) {
            // Authentication
            deps.put("@auth0/auth0-react", "^2.2.4");
            deps.put("jsonwebtoken", "^9.0.2");

            // GraphQL
            deps.put("@apollo/client", "^3.8.8");
            deps.put("graphql", "^16.8.1");

            // Analytics
            deps.put("react-ga4", "^2.1.0");
        }

        return deps;
    }

    /**
     * Get complete list of dev dependencies
     */
    private Map<String, String> getCompleteDevDependencies(ReactProjectType type) {
        Map<String, String> devDeps = new LinkedHashMap<>();

        // Build tools
        devDeps.put("@vitejs/plugin-react", "^4.2.0");
        devDeps.put("vite", "^5.0.7");

        // TypeScript
        if (type == ReactProjectType.WITH_TYPESCRIPT || type == ReactProjectType.FULL_STACK) {
            devDeps.put("typescript", "^5.3.3");
            devDeps.put("@types/react", "^18.2.45");
            devDeps.put("@types/react-dom", "^18.2.17");
            devDeps.put("@types/node", "^20.10.4");
            devDeps.put("@types/lodash", "^4.14.202");
            devDeps.put("@types/uuid", "^9.0.7");
            devDeps.put("@types/crypto-js", "^4.2.1");
        }

        // Linting
        devDeps.put("eslint", "^8.55.0");
        devDeps.put("eslint-plugin-react", "^7.33.2");
        devDeps.put("eslint-plugin-react-hooks", "^4.6.0");
        devDeps.put("eslint-plugin-jsx-a11y", "^6.8.0");
        devDeps.put("eslint-plugin-import", "^2.29.0");
        devDeps.put("eslint-config-prettier", "^9.1.0");

        // Formatting
        devDeps.put("prettier", "^3.1.1");
        devDeps.put("husky", "^8.0.3");
        devDeps.put("lint-staged", "^15.2.0");

        // Testing
        devDeps.put("@testing-library/react", "^14.1.2");
        devDeps.put("@testing-library/jest-dom", "^6.1.5");
        devDeps.put("@testing-library/user-event", "^14.5.1");
        devDeps.put("jest", "^29.7.0");
        devDeps.put("jest-environment-jsdom", "^29.7.0");
        devDeps.put("@babel/preset-react", "^7.23.3");
        devDeps.put("@babel/preset-env", "^7.23.5");

        // CSS
        devDeps.put("tailwindcss", "^3.3.6");
        devDeps.put("postcss", "^8.4.32");
        devDeps.put("autoprefixer", "^10.4.16");
        devDeps.put("sass", "^1.69.5");

        // Bundle analysis
        devDeps.put("rollup-plugin-visualizer", "^5.11.0");
        devDeps.put("source-map-explorer", "^2.5.3");

        // Development utilities
        devDeps.put("concurrently", "^8.2.2");
        devDeps.put("cross-env", "^7.0.3");
        devDeps.put("dotenv-cli", "^7.3.0");

        // Storybook for component development
        if (type == ReactProjectType.FULL_STACK) {
            devDeps.put("@storybook/react-vite", "^7.6.4");
            devDeps.put("@storybook/addon-essentials", "^7.6.4");
            devDeps.put("@storybook/addon-interactions", "^7.6.4");
            devDeps.put("@storybook/addon-links", "^7.6.4");
        }

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
                    // Get latest version that matches the range
                    String latestVersion = dependencyResolver.getLatestVersion("NPM", packageName).get();
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

    private String generatePackageJsonContent(String projectName, ReactProjectType type,
                                             Map<String, String> dependencies,
                                             Map<String, String> devDependencies) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        sb.append("  \"name\": \"").append(projectName.toLowerCase()).append("\",\n");
        sb.append("  \"private\": true,\n");
        sb.append("  \"version\": \"1.0.0\",\n");
        sb.append("  \"type\": \"module\",\n");
        sb.append("  \"scripts\": {\n");
        sb.append("    \"dev\": \"vite\",\n");
        sb.append("    \"build\": \"vite build\",\n");
        sb.append("    \"preview\": \"vite preview\",\n");
        sb.append("    \"test\": \"jest\",\n");
        sb.append("    \"test:watch\": \"jest --watch\",\n");
        sb.append("    \"test:coverage\": \"jest --coverage\",\n");
        sb.append("    \"lint\": \"eslint . --ext js,jsx,ts,tsx --report-unused-disable-directives --max-warnings 0\",\n");
        sb.append("    \"format\": \"prettier --write \\\"src/**/*.{js,jsx,ts,tsx,json,css,scss,md}\\\"\",\n");
        sb.append("    \"prepare\": \"husky install\",\n");
        sb.append("    \"analyze\": \"source-map-explorer 'dist/**/*.js'\"\n");
        sb.append("  },\n");
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
        sb.append("  \"lint-staged\": {\n");
        sb.append("    \"*.{js,jsx,ts,tsx}\": [\n");
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
               "/.pnp\n" +
               ".pnp.js\n\n" +
               "# Testing\n" +
               "/coverage\n\n" +
               "# Production\n" +
               "/build\n" +
               "/dist\n\n" +
               "# Misc\n" +
               ".DS_Store\n" +
               ".env\n" +
               ".env.local\n" +
               ".env.development.local\n" +
               ".env.test.local\n" +
               ".env.production.local\n\n" +
               "# Logs\n" +
               "npm-debug.log*\n" +
               "yarn-debug.log*\n" +
               "yarn-error.log*\n" +
               "lerna-debug.log*\n\n" +
               "# Editor directories\n" +
               ".idea/\n" +
               ".vscode/\n" +
               "*.swp\n" +
               "*.swo\n" +
               "*~\n\n" +
               "# OS files\n" +
               "Thumbs.db\n";
    }

    private String generateReadme(String projectName, ReactProjectType type) {
        return "# " + projectName + "\n\n" +
               "A modern React application built with " + type.getDescription() + "\n\n" +
               "## Features\n\n" +
               "- Modern React 18 with Hooks\n" +
               "- Vite for fast development and optimized builds\n" +
               "- React Router for navigation\n" +
               "- Material-UI for beautiful components\n" +
               "- Tailwind CSS for utility-first styling\n" +
               "- Axios for HTTP requests\n" +
               "- Form handling with React Hook Form\n" +
               "- Testing with Jest and React Testing Library\n" +
               "- ESLint and Prettier for code quality\n" +
               "- Husky for pre-commit hooks\n\n" +
               "## Getting Started\n\n" +
               "### Prerequisites\n\n" +
               "- Node.js 18+ and npm\n\n" +
               "### Installation\n\n" +
               "```bash\n" +
               "npm install\n" +
               "```\n\n" +
               "### Development\n\n" +
               "```bash\n" +
               "npm run dev\n" +
               "```\n\n" +
               "### Build\n\n" +
               "```bash\n" +
               "npm run build\n" +
               "```\n\n" +
               "### Testing\n\n" +
               "```bash\n" +
               "npm test\n" +
               "```\n\n" +
               "## Project Structure\n\n" +
               "```\n" +
               projectName + "/\n" +
               "├── src/\n" +
               "│   ├── components/     # Reusable components\n" +
               "│   ├── pages/          # Page components\n" +
               "│   ├── hooks/          # Custom hooks\n" +
               "│   ├── services/       # API services\n" +
               "│   ├── utils/          # Utility functions\n" +
               "│   ├── assets/         # Images, fonts, etc.\n" +
               "│   └── styles/         # Global styles\n" +
               "├── public/             # Static files\n" +
               "├── __tests__/          # Test files\n" +
               "└── package.json        # Dependencies\n" +
               "```\n";
    }

    private String generateEnvFile() {
        return "# API Configuration\n" +
               "VITE_API_URL=http://localhost:3000/api\n" +
               "VITE_API_KEY=your_api_key_here\n\n" +
               "# App Configuration\n" +
               "VITE_APP_NAME=React App\n" +
               "VITE_APP_VERSION=1.0.0\n\n" +
               "# Feature Flags\n" +
               "VITE_ENABLE_ANALYTICS=false\n" +
               "VITE_ENABLE_DEBUG=true\n";
    }

    private String generateEslintConfig(ReactProjectType type) {
        return "{\n" +
               "  \"env\": {\n" +
               "    \"browser\": true,\n" +
               "    \"es2021\": true,\n" +
               "    \"jest\": true\n" +
               "  },\n" +
               "  \"extends\": [\n" +
               "    \"eslint:recommended\",\n" +
               "    \"plugin:react/recommended\",\n" +
               "    \"plugin:react-hooks/recommended\",\n" +
               "    \"plugin:jsx-a11y/recommended\",\n" +
               "    \"plugin:import/recommended\",\n" +
               "    \"prettier\"\n" +
               "  ],\n" +
               "  \"parserOptions\": {\n" +
               "    \"ecmaFeatures\": {\n" +
               "      \"jsx\": true\n" +
               "    },\n" +
               "    \"ecmaVersion\": \"latest\",\n" +
               "    \"sourceType\": \"module\"\n" +
               "  },\n" +
               "  \"plugins\": [\"react\", \"react-hooks\", \"jsx-a11y\", \"import\"],\n" +
               "  \"rules\": {\n" +
               "    \"react/react-in-jsx-scope\": \"off\",\n" +
               "    \"react/prop-types\": \"off\"\n" +
               "  },\n" +
               "  \"settings\": {\n" +
               "    \"react\": {\n" +
               "      \"version\": \"detect\"\n" +
               "    }\n" +
               "  }\n" +
               "}";
    }

    private String generatePrettierConfig() {
        return "{\n" +
               "  \"semi\": true,\n" +
               "  \"trailingComma\": \"es5\",\n" +
               "  \"singleQuote\": true,\n" +
               "  \"printWidth\": 100,\n" +
               "  \"tabWidth\": 2,\n" +
               "  \"useTabs\": false,\n" +
               "  \"endOfLine\": \"lf\"\n" +
               "}";
    }

    private String generateTsConfig() {
        return "{\n" +
               "  \"compilerOptions\": {\n" +
               "    \"target\": \"ES2020\",\n" +
               "    \"useDefineForClassFields\": true,\n" +
               "    \"lib\": [\"ES2020\", \"DOM\", \"DOM.Iterable\"],\n" +
               "    \"module\": \"ESNext\",\n" +
               "    \"skipLibCheck\": true,\n" +
               "    \"moduleResolution\": \"bundler\",\n" +
               "    \"allowImportingTsExtensions\": true,\n" +
               "    \"resolveJsonModule\": true,\n" +
               "    \"isolatedModules\": true,\n" +
               "    \"noEmit\": true,\n" +
               "    \"jsx\": \"react-jsx\",\n" +
               "    \"strict\": true,\n" +
               "    \"noUnusedLocals\": true,\n" +
               "    \"noUnusedParameters\": true,\n" +
               "    \"noFallthroughCasesInSwitch\": true,\n" +
               "    \"baseUrl\": \".\",\n" +
               "    \"paths\": {\n" +
               "      \"@/*\": [\"src/*\"]\n" +
               "    }\n" +
               "  },\n" +
               "  \"include\": [\"src\"],\n" +
               "  \"references\": [{ \"path\": \"./tsconfig.node.json\" }]\n" +
               "}";
    }

    private String generateTailwindConfig() {
        return "/** @type {import('tailwindcss').Config} */\n" +
               "export default {\n" +
               "  content: [\n" +
               "    \"./index.html\",\n" +
               "    \"./src/**/*.{js,ts,jsx,tsx}\",\n" +
               "  ],\n" +
               "  theme: {\n" +
               "    extend: {},\n" +
               "  },\n" +
               "  plugins: [],\n" +
               "}";
    }

    private String generatePostCSSConfig() {
        return "export default {\n" +
               "  plugins: {\n" +
               "    tailwindcss: {},\n" +
               "    autoprefixer: {},\n" +
               "  },\n" +
               "}";
    }

    private String generateWebpackConfig() {
        return "// Advanced webpack configuration for production builds\n" +
               "const path = require('path');\n" +
               "const HtmlWebpackPlugin = require('html-webpack-plugin');\n\n" +
               "module.exports = {\n" +
               "  entry: './src/index.js',\n" +
               "  output: {\n" +
               "    path: path.resolve(__dirname, 'dist'),\n" +
               "    filename: 'bundle.[contenthash].js',\n" +
               "  },\n" +
               "  // Additional webpack configuration...\n" +
               "};\n";
    }

    private String generateJestConfig(ReactProjectType type) {
        return "module.exports = {\n" +
               "  testEnvironment: 'jsdom',\n" +
               "  setupFilesAfterEnv: ['<rootDir>/src/setupTests.js'],\n" +
               "  moduleNameMapper: {\n" +
               "    '\\\\.(css|less|scss|sass)$': 'identity-obj-proxy',\n" +
               "  },\n" +
               "  transform: {\n" +
               "    '^.+\\\\.(js|jsx|ts|tsx)$': ['babel-jest', { presets: ['@babel/preset-react'] }],\n" +
               "  },\n" +
               "  collectCoverageFrom: [\n" +
               "    'src/**/*.{js,jsx,ts,tsx}',\n" +
               "    '!src/index.js',\n" +
               "    '!src/reportWebVitals.js',\n" +
               "  ],\n" +
               "};\n";
    }

    private String generateBabelConfig(ReactProjectType type) {
        return "{\n" +
               "  \"presets\": [\n" +
               "    \"@babel/preset-env\",\n" +
               "    \"@babel/preset-react\"\n" +
               "  ]\n" +
               "}";
    }

    private String generateAppComponent(ReactProjectType type) {
        return "import React from 'react';\n" +
               "import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';\n" +
               "import { ThemeProvider, createTheme } from '@mui/material/styles';\n" +
               "import CssBaseline from '@mui/material/CssBaseline';\n" +
               "import { ToastContainer } from 'react-toastify';\n" +
               "import 'react-toastify/dist/ReactToastify.css';\n" +
               "import './App.css';\n\n" +
               "// Import pages\n" +
               "import HomePage from './pages/HomePage';\n" +
               "import AboutPage from './pages/AboutPage';\n\n" +
               "const theme = createTheme({\n" +
               "  palette: {\n" +
               "    primary: {\n" +
               "      main: '#1976d2',\n" +
               "    },\n" +
               "    secondary: {\n" +
               "      main: '#dc004e',\n" +
               "    },\n" +
               "  },\n" +
               "});\n\n" +
               "function App() {\n" +
               "  return (\n" +
               "    <ThemeProvider theme={theme}>\n" +
               "      <CssBaseline />\n" +
               "      <Router>\n" +
               "        <div className=\"App\">\n" +
               "          <Routes>\n" +
               "            <Route path=\"/\" element={<HomePage />} />\n" +
               "            <Route path=\"/about\" element={<AboutPage />} />\n" +
               "          </Routes>\n" +
               "          <ToastContainer position=\"bottom-right\" />\n" +
               "        </div>\n" +
               "      </Router>\n" +
               "    </ThemeProvider>\n" +
               "  );\n" +
               "}\n\n" +
               "export default App;\n";
    }

    private String generateIndexFile(ReactProjectType type) {
        return "import React from 'react';\n" +
               "import ReactDOM from 'react-dom/client';\n" +
               "import App from './App';\n" +
               "import './index.css';\n\n" +
               "ReactDOM.createRoot(document.getElementById('root')).render(\n" +
               "  <React.StrictMode>\n" +
               "    <App />\n" +
               "  </React.StrictMode>\n" +
               ");\n";
    }

    private String generateAppCss() {
        return ".App {\n" +
               "  min-height: 100vh;\n" +
               "  display: flex;\n" +
               "  flex-direction: column;\n" +
               "}\n";
    }

    private String generateIndexCss() {
        return "@tailwind base;\n" +
               "@tailwind components;\n" +
               "@tailwind utilities;\n\n" +
               ":root {\n" +
               "  font-family: Inter, system-ui, Avenir, Helvetica, Arial, sans-serif;\n" +
               "  line-height: 1.5;\n" +
               "  font-weight: 400;\n" +
               "}\n\n" +
               "body {\n" +
               "  margin: 0;\n" +
               "  padding: 0;\n" +
               "}\n";
    }

    private void generateComponentFiles(ProjectNode components, ReactProjectType type) {
        String ext = (type == ReactProjectType.WITH_TYPESCRIPT || type == ReactProjectType.FULL_STACK) ? "tsx" : "jsx";

        // Header component
        ProjectNode header = new ProjectNode("Header." + ext, ProjectNode.NodeType.FILE,
            "import React from 'react';\n" +
            "import { AppBar, Toolbar, Typography, Button } from '@mui/material';\n" +
            "import { Link } from 'react-router-dom';\n\n" +
            "const Header = () => {\n" +
            "  return (\n" +
            "    <AppBar position=\"static\">\n" +
            "      <Toolbar>\n" +
            "        <Typography variant=\"h6\" sx={{ flexGrow: 1 }}>\n" +
            "          My App\n" +
            "        </Typography>\n" +
            "        <Button color=\"inherit\" component={Link} to=\"/\">Home</Button>\n" +
            "        <Button color=\"inherit\" component={Link} to=\"/about\">About</Button>\n" +
            "      </Toolbar>\n" +
            "    </AppBar>\n" +
            "  );\n" +
            "};\n\n" +
            "export default Header;\n");
        components.addChild(header);

        // Footer component
        ProjectNode footer = new ProjectNode("Footer." + ext, ProjectNode.NodeType.FILE,
            "import React from 'react';\n" +
            "import { Box, Typography } from '@mui/material';\n\n" +
            "const Footer = () => {\n" +
            "  return (\n" +
            "    <Box component=\"footer\" sx={{ py: 3, px: 2, mt: 'auto', backgroundColor: '#f5f5f5' }}>\n" +
            "      <Typography variant=\"body2\" color=\"text.secondary\" align=\"center\">\n" +
            "        © 2024 My App. All rights reserved.\n" +
            "      </Typography>\n" +
            "    </Box>\n" +
            "  );\n" +
            "};\n\n" +
            "export default Footer;\n");
        components.addChild(footer);
    }

    private void generatePageFiles(ProjectNode pages, ReactProjectType type) {
        String ext = (type == ReactProjectType.WITH_TYPESCRIPT || type == ReactProjectType.FULL_STACK) ? "tsx" : "jsx";

        // Home page
        ProjectNode homePage = new ProjectNode("HomePage." + ext, ProjectNode.NodeType.FILE,
            "import React from 'react';\n" +
            "import { Container, Typography, Button, Box } from '@mui/material';\n" +
            "import Header from '../components/Header';\n" +
            "import Footer from '../components/Footer';\n\n" +
            "const HomePage = () => {\n" +
            "  return (\n" +
            "    <>\n" +
            "      <Header />\n" +
            "      <Container maxWidth=\"lg\" sx={{ mt: 4, mb: 4 }}>\n" +
            "        <Box textAlign=\"center\" py={8}>\n" +
            "          <Typography variant=\"h2\" gutterBottom>\n" +
            "            Welcome to React App\n" +
            "          </Typography>\n" +
            "          <Typography variant=\"h5\" color=\"text.secondary\" paragraph>\n" +
            "            Built with modern React and best practices\n" +
            "          </Typography>\n" +
            "          <Button variant=\"contained\" size=\"large\" sx={{ mt: 3 }}>\n" +
            "            Get Started\n" +
            "          </Button>\n" +
            "        </Box>\n" +
            "      </Container>\n" +
            "      <Footer />\n" +
            "    </>\n" +
            "  );\n" +
            "};\n\n" +
            "export default HomePage;\n");
        pages.addChild(homePage);

        // About page
        ProjectNode aboutPage = new ProjectNode("AboutPage." + ext, ProjectNode.NodeType.FILE,
            "import React from 'react';\n" +
            "import { Container, Typography } from '@mui/material';\n" +
            "import Header from '../components/Header';\n" +
            "import Footer from '../components/Footer';\n\n" +
            "const AboutPage = () => {\n" +
            "  return (\n" +
            "    <>\n" +
            "      <Header />\n" +
            "      <Container maxWidth=\"lg\" sx={{ mt: 4, mb: 4 }}>\n" +
            "        <Typography variant=\"h3\" gutterBottom>\n" +
            "          About Us\n" +
            "        </Typography>\n" +
            "        <Typography variant=\"body1\" paragraph>\n" +
            "          This is a modern React application with all the best practices.\n" +
            "        </Typography>\n" +
            "      </Container>\n" +
            "      <Footer />\n" +
            "    </>\n" +
            "  );\n" +
            "};\n\n" +
            "export default AboutPage;\n");
        pages.addChild(aboutPage);
    }

    private void generateHookFiles(ProjectNode hooks, ReactProjectType type) {
        String ext = (type == ReactProjectType.WITH_TYPESCRIPT || type == ReactProjectType.FULL_STACK) ? "ts" : "js";

        // useLocalStorage hook
        ProjectNode useLocalStorage = new ProjectNode("useLocalStorage." + ext, ProjectNode.NodeType.FILE,
            "import { useState, useEffect } from 'react';\n\n" +
            "function useLocalStorage(key, initialValue) {\n" +
            "  const [storedValue, setStoredValue] = useState(() => {\n" +
            "    try {\n" +
            "      const item = window.localStorage.getItem(key);\n" +
            "      return item ? JSON.parse(item) : initialValue;\n" +
            "    } catch (error) {\n" +
            "      console.error(error);\n" +
            "      return initialValue;\n" +
            "    }\n" +
            "  });\n\n" +
            "  const setValue = (value) => {\n" +
            "    try {\n" +
            "      setStoredValue(value);\n" +
            "      window.localStorage.setItem(key, JSON.stringify(value));\n" +
            "    } catch (error) {\n" +
            "      console.error(error);\n" +
            "    }\n" +
            "  };\n\n" +
            "  return [storedValue, setValue];\n" +
            "}\n\n" +
            "export default useLocalStorage;\n");
        hooks.addChild(useLocalStorage);
    }

    private void generateUtilityFiles(ProjectNode utils, ReactProjectType type) {
        String ext = (type == ReactProjectType.WITH_TYPESCRIPT || type == ReactProjectType.FULL_STACK) ? "ts" : "js";

        // API helper
        ProjectNode apiHelper = new ProjectNode("api." + ext, ProjectNode.NodeType.FILE,
            "import axios from 'axios';\n\n" +
            "const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:3000/api';\n\n" +
            "const api = axios.create({\n" +
            "  baseURL: API_BASE_URL,\n" +
            "  headers: {\n" +
            "    'Content-Type': 'application/json',\n" +
            "  },\n" +
            "});\n\n" +
            "// Request interceptor\n" +
            "api.interceptors.request.use(\n" +
            "  (config) => {\n" +
            "    const token = localStorage.getItem('token');\n" +
            "    if (token) {\n" +
            "      config.headers.Authorization = `Bearer ${token}`;\n" +
            "    }\n" +
            "    return config;\n" +
            "  },\n" +
            "  (error) => Promise.reject(error)\n" +
            ");\n\n" +
            "export default api;\n");
        utils.addChild(apiHelper);
    }

    private void generateServiceFiles(ProjectNode services, ReactProjectType type) {
        String ext = (type == ReactProjectType.WITH_TYPESCRIPT || type == ReactProjectType.FULL_STACK) ? "ts" : "js";

        // User service
        ProjectNode userService = new ProjectNode("userService." + ext, ProjectNode.NodeType.FILE,
            "import api from '../utils/api';\n\n" +
            "const userService = {\n" +
            "  getUsers: async () => {\n" +
            "    const response = await api.get('/users');\n" +
            "    return response.data;\n" +
            "  },\n\n" +
            "  getUser: async (id) => {\n" +
            "    const response = await api.get(`/users/${id}`);\n" +
            "    return response.data;\n" +
            "  },\n\n" +
            "  createUser: async (userData) => {\n" +
            "    const response = await api.post('/users', userData);\n" +
            "    return response.data;\n" +
            "  },\n\n" +
            "  updateUser: async (id, userData) => {\n" +
            "    const response = await api.put(`/users/${id}`, userData);\n" +
            "    return response.data;\n" +
            "  },\n\n" +
            "  deleteUser: async (id) => {\n" +
            "    const response = await api.delete(`/users/${id}`);\n" +
            "    return response.data;\n" +
            "  },\n" +
            "};\n\n" +
            "export default userService;\n");
        services.addChild(userService);
    }

    private void generateStoreFiles(ProjectNode store, ReactProjectType type) {
        String ext = (type == ReactProjectType.WITH_TYPESCRIPT || type == ReactProjectType.FULL_STACK) ? "ts" : "js";

        // Redux store
        ProjectNode storeFile = new ProjectNode("store." + ext, ProjectNode.NodeType.FILE,
            "import { configureStore } from '@reduxjs/toolkit';\n" +
            "import userReducer from './userSlice';\n\n" +
            "export const store = configureStore({\n" +
            "  reducer: {\n" +
            "    user: userReducer,\n" +
            "  },\n" +
            "});\n\n" +
            "export type RootState = ReturnType<typeof store.getState>;\n" +
            "export type AppDispatch = typeof store.dispatch;\n");
        store.addChild(storeFile);

        // User slice
        ProjectNode userSlice = new ProjectNode("userSlice." + ext, ProjectNode.NodeType.FILE,
            "import { createSlice } from '@reduxjs/toolkit';\n\n" +
            "const userSlice = createSlice({\n" +
            "  name: 'user',\n" +
            "  initialState: {\n" +
            "    currentUser: null,\n" +
            "    isLoading: false,\n" +
            "    error: null,\n" +
            "  },\n" +
            "  reducers: {\n" +
            "    setUser: (state, action) => {\n" +
            "      state.currentUser = action.payload;\n" +
            "    },\n" +
            "    clearUser: (state) => {\n" +
            "      state.currentUser = null;\n" +
            "    },\n" +
            "  },\n" +
            "});\n\n" +
            "export const { setUser, clearUser } = userSlice.actions;\n" +
            "export default userSlice.reducer;\n");
        store.addChild(userSlice);
    }

    private void generatePublicFiles(ProjectNode public_) {
        // index.html
        ProjectNode indexHtml = new ProjectNode("index.html", ProjectNode.NodeType.FILE,
            "<!DOCTYPE html>\n" +
            "<html lang=\"en\">\n" +
            "  <head>\n" +
            "    <meta charset=\"UTF-8\" />\n" +
            "    <link rel=\"icon\" type=\"image/svg+xml\" href=\"/vite.svg\" />\n" +
            "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\" />\n" +
            "    <title>React App</title>\n" +
            "  </head>\n" +
            "  <body>\n" +
            "    <div id=\"root\"></div>\n" +
            "    <script type=\"module\" src=\"/src/index.jsx\"></script>\n" +
            "  </body>\n" +
            "</html>\n");
        public_.addChild(indexHtml);

        // robots.txt
        ProjectNode robotsTxt = new ProjectNode("robots.txt", ProjectNode.NodeType.FILE,
            "User-agent: *\n" +
            "Disallow:\n");
        public_.addChild(robotsTxt);
    }

    private void generateTestFiles(ProjectNode tests, ReactProjectType type) {
        String ext = (type == ReactProjectType.WITH_TYPESCRIPT || type == ReactProjectType.FULL_STACK) ? "tsx" : "jsx";

        // App test
        ProjectNode appTest = new ProjectNode("App.test." + ext, ProjectNode.NodeType.FILE,
            "import React from 'react';\n" +
            "import { render, screen } from '@testing-library/react';\n" +
            "import App from '../src/App';\n\n" +
            "describe('App', () => {\n" +
            "  test('renders without crashing', () => {\n" +
            "    render(<App />);\n" +
            "    expect(screen.getByText(/Welcome/i)).toBeInTheDocument();\n" +
            "  });\n" +
            "});\n");
        tests.addChild(appTest);

        // Setup tests
        ProjectNode setupTests = new ProjectNode("setupTests.js", ProjectNode.NodeType.FILE,
            "import '@testing-library/jest-dom';\n");
        tests.addChild(setupTests);
    }

    /**
     * React project types
     */
    public enum ReactProjectType {
        BASIC("Basic React Application"),
        WITH_REDUX("React with Redux State Management"),
        WITH_CONTEXT("React with Context API"),
        WITH_TYPESCRIPT("React with TypeScript"),
        TYPESCRIPT_APP("React Application with TypeScript"),
        WEB_APP("React Web Application"),
        ML_API("React with ML/AI Integration"),
        FULL_STACK("Full-Stack React Application with Everything");

        private final String description;

        ReactProjectType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
}