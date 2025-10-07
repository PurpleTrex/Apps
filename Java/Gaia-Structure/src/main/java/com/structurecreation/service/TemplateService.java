package com.structurecreation.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.structurecreation.model.ProjectTemplate;
import com.structurecreation.model.ProjectStructure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing project templates - saving, loading, and organizing templates
 */
public class TemplateService {
    
    private static final Logger logger = LoggerFactory.getLogger(TemplateService.class);
    private static final String TEMPLATES_DIR = "project-templates";
    private static final String TEMPLATE_EXTENSION = ".json";
    
    private final ObjectMapper objectMapper;
    private final Path templatesPath;
    
    public TemplateService() {
        this.objectMapper = createObjectMapper();
        this.templatesPath = Paths.get(System.getProperty("user.home"), TEMPLATES_DIR);
        initializeTemplatesDirectory();
    }
    
    private ObjectMapper createObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        return mapper;
    }
    
    private void initializeTemplatesDirectory() {
        try {
            if (!Files.exists(templatesPath)) {
                Files.createDirectories(templatesPath);
                logger.info("Created templates directory: {}", templatesPath);
                
                // Create built-in templates
                createBuiltInTemplates();
            }
        } catch (IOException e) {
            logger.error("Failed to create templates directory", e);
        }
    }
    
    /**
     * Saves a template to the filesystem
     */
    public void saveTemplate(ProjectTemplate template) throws IOException {
        if (!template.isValidTemplate()) {
            throw new IllegalArgumentException("Invalid template: " + template.getTemplateName());
        }
        
        String fileName = sanitizeFileName(template.getTemplateName()) + TEMPLATE_EXTENSION;
        Path templateFile = templatesPath.resolve(fileName);
        
        objectMapper.writeValue(templateFile.toFile(), template);
        logger.info("Saved template: {} to {}", template.getTemplateName(), templateFile);
    }
    
    /**
     * Loads a specific template by name
     */
    public ProjectTemplate loadTemplate(String templateName) throws IOException {
        String fileName = sanitizeFileName(templateName) + TEMPLATE_EXTENSION;
        Path templateFile = templatesPath.resolve(fileName);
        
        if (!Files.exists(templateFile)) {
            throw new IOException("Template not found: " + templateName);
        }
        
        ProjectTemplate template = objectMapper.readValue(templateFile.toFile(), ProjectTemplate.class);
        logger.info("Loaded template: {}", template.getTemplateName());
        return template;
    }
    
    /**
     * Lists all available templates
     */
    public List<ProjectTemplate> listTemplates() throws IOException {
        List<ProjectTemplate> templates = new ArrayList<>();
        
        if (!Files.exists(templatesPath)) {
            return templates;
        }
        
        List<Path> templateFiles = Files.list(templatesPath)
                .filter(path -> path.toString().endsWith(TEMPLATE_EXTENSION))
                .collect(Collectors.toList());
        
        for (Path templateFile : templateFiles) {
            try {
                ProjectTemplate template = objectMapper.readValue(templateFile.toFile(), ProjectTemplate.class);
                templates.add(template);
            } catch (IOException e) {
                logger.warn("Failed to load template from {}: {}", templateFile, e.getMessage());
            }
        }
        
        // Sort by name
        templates.sort((t1, t2) -> t1.getTemplateName().compareToIgnoreCase(t2.getTemplateName()));
        
        logger.info("Listed {} templates", templates.size());
        return templates;
    }
    
    /**
     * Lists templates by category
     */
    public List<ProjectTemplate> listTemplatesByCategory(String category) throws IOException {
        return listTemplates().stream()
                .filter(template -> template.getCategory().equalsIgnoreCase(category))
                .collect(Collectors.toList());
    }
    
    /**
     * Lists templates by project type
     */
    public List<ProjectTemplate> listTemplatesByProjectType(String projectType) throws IOException {
        return listTemplates().stream()
                .filter(template -> template.getProjectType().equalsIgnoreCase(projectType))
                .collect(Collectors.toList());
    }
    
    /**
     * Deletes a template
     */
    public boolean deleteTemplate(String templateName) throws IOException {
        String fileName = sanitizeFileName(templateName) + TEMPLATE_EXTENSION;
        Path templateFile = templatesPath.resolve(fileName);
        
        if (Files.exists(templateFile)) {
            Files.delete(templateFile);
            logger.info("Deleted template: {}", templateName);
            return true;
        }
        
        return false;
    }
    
    /**
     * Creates a template from current project structure
     */
    public ProjectTemplate createTemplateFromProject(ProjectStructure projectStructure, String templateName, String templateDescription) {
        ProjectTemplate template = ProjectTemplate.fromProjectStructure(projectStructure, templateName, templateDescription);
        
        // Set category based on project type
        template.setCategory(determineCategory(projectStructure.getProjectType()));
        
        // Set tags based on project type and dependencies
        template.setTags(generateTags(projectStructure));
        
        return template;
    }
    
    /**
     * Applies a template to create a new project structure
     */
    public ProjectStructure applyTemplate(ProjectTemplate template, String projectName, String projectLocation) {
        ProjectStructure projectStructure = template.toProjectStructure(projectName, projectLocation);
        logger.info("Applied template '{}' to create project '{}'", template.getTemplateName(), projectName);
        return projectStructure;
    }
    
    /**
     * Checks if a template with the given name exists
     */
    public boolean templateExists(String templateName) {
        String fileName = sanitizeFileName(templateName) + TEMPLATE_EXTENSION;
        Path templateFile = templatesPath.resolve(fileName);
        return Files.exists(templateFile);
    }
    
    /**
     * Gets the templates directory path
     */
    public Path getTemplatesPath() {
        return templatesPath;
    }
    
    private void createBuiltInTemplates() {
        try {
            // Create basic Java Maven template
            createJavaMavenTemplate();
            
            // Create basic Python template
            createPythonTemplate();
            
            // Create basic Node.js template
            createNodeJsTemplate();
            
            // Create basic React template
            createReactTemplate();
            
        } catch (IOException e) {
            logger.warn("Failed to create built-in templates", e);
        }
    }
    
    private void createJavaMavenTemplate() throws IOException {
        ProjectTemplate template = new ProjectTemplate();
        template.setTemplateName("Java Maven Project");
        template.setTemplateDescription("Standard Java Maven project structure");
        template.setProjectType("Java Maven");
        template.setCategory("Java");
        template.setAuthor("System");
        template.setTags(new String[]{"java", "maven", "backend", "enterprise"});
        
        // Create structure
        var root = template.getTemplateStructure();
        root.setName("Project Root");
        
        var src = new com.structurecreation.model.ProjectNode("src", com.structurecreation.model.ProjectNode.NodeType.FOLDER);
        var main = new com.structurecreation.model.ProjectNode("main", com.structurecreation.model.ProjectNode.NodeType.FOLDER);
        var test = new com.structurecreation.model.ProjectNode("test", com.structurecreation.model.ProjectNode.NodeType.FOLDER);
        
        var mainJava = new com.structurecreation.model.ProjectNode("java", com.structurecreation.model.ProjectNode.NodeType.FOLDER);
        var mainResources = new com.structurecreation.model.ProjectNode("resources", com.structurecreation.model.ProjectNode.NodeType.FOLDER);
        var testJava = new com.structurecreation.model.ProjectNode("java", com.structurecreation.model.ProjectNode.NodeType.FOLDER);
        var testResources = new com.structurecreation.model.ProjectNode("resources", com.structurecreation.model.ProjectNode.NodeType.FOLDER);
        
        main.addChild(mainJava);
        main.addChild(mainResources);
        test.addChild(testJava);
        test.addChild(testResources);
        
        src.addChild(main);
        src.addChild(test);
        root.addChild(src);
        
        saveTemplate(template);
    }
    
    private void createPythonTemplate() throws IOException {
        ProjectTemplate template = new ProjectTemplate();
        template.setTemplateName("Python Project");
        template.setTemplateDescription("Standard Python project structure");
        template.setProjectType("Python");
        template.setCategory("Python");
        template.setAuthor("System");
        template.setTags(new String[]{"python", "scripting", "data-science", "ai"});
        
        var root = template.getTemplateStructure();
        root.setName("Project Root");
        
        var src = new com.structurecreation.model.ProjectNode("src", com.structurecreation.model.ProjectNode.NodeType.FOLDER);
        var tests = new com.structurecreation.model.ProjectNode("tests", com.structurecreation.model.ProjectNode.NodeType.FOLDER);
        var docs = new com.structurecreation.model.ProjectNode("docs", com.structurecreation.model.ProjectNode.NodeType.FOLDER);
        
        var initPy = new com.structurecreation.model.ProjectNode("__init__.py", com.structurecreation.model.ProjectNode.NodeType.FILE);
        var mainPy = new com.structurecreation.model.ProjectNode("main.py", com.structurecreation.model.ProjectNode.NodeType.FILE);
        var testInit = new com.structurecreation.model.ProjectNode("__init__.py", com.structurecreation.model.ProjectNode.NodeType.FILE);
        
        src.addChild(initPy);
        src.addChild(mainPy);
        tests.addChild(testInit);
        
        root.addChild(src);
        root.addChild(tests);
        root.addChild(docs);
        
        saveTemplate(template);
    }
    
    private void createNodeJsTemplate() throws IOException {
        ProjectTemplate template = new ProjectTemplate();
        template.setTemplateName("Node.js Project");
        template.setTemplateDescription("Standard Node.js project structure");
        template.setProjectType("Node.js");
        template.setCategory("JavaScript");
        template.setAuthor("System");
        template.setTags(new String[]{"nodejs", "javascript", "backend", "api"});
        
        var root = template.getTemplateStructure();
        root.setName("Project Root");
        
        var src = new com.structurecreation.model.ProjectNode("src", com.structurecreation.model.ProjectNode.NodeType.FOLDER);
        var test = new com.structurecreation.model.ProjectNode("test", com.structurecreation.model.ProjectNode.NodeType.FOLDER);
        var public_ = new com.structurecreation.model.ProjectNode("public", com.structurecreation.model.ProjectNode.NodeType.FOLDER);
        
        var indexJs = new com.structurecreation.model.ProjectNode("index.js", com.structurecreation.model.ProjectNode.NodeType.FILE);
        var appJs = new com.structurecreation.model.ProjectNode("app.js", com.structurecreation.model.ProjectNode.NodeType.FILE);
        
        src.addChild(indexJs);
        src.addChild(appJs);
        
        root.addChild(src);
        root.addChild(test);
        root.addChild(public_);
        
        saveTemplate(template);
    }
    
    private void createReactTemplate() throws IOException {
        ProjectTemplate template = new ProjectTemplate();
        template.setTemplateName("React Application");
        template.setTemplateDescription("Modern React application structure");
        template.setProjectType("React");
        template.setCategory("Frontend");
        template.setAuthor("System");
        template.setTags(new String[]{"react", "javascript", "frontend", "ui", "spa"});
        
        var root = template.getTemplateStructure();
        root.setName("Project Root");
        
        var src = new com.structurecreation.model.ProjectNode("src", com.structurecreation.model.ProjectNode.NodeType.FOLDER);
        var public_ = new com.structurecreation.model.ProjectNode("public", com.structurecreation.model.ProjectNode.NodeType.FOLDER);
        var components = new com.structurecreation.model.ProjectNode("components", com.structurecreation.model.ProjectNode.NodeType.FOLDER);
        var hooks = new com.structurecreation.model.ProjectNode("hooks", com.structurecreation.model.ProjectNode.NodeType.FOLDER);
        var utils = new com.structurecreation.model.ProjectNode("utils", com.structurecreation.model.ProjectNode.NodeType.FOLDER);
        
        var appJs = new com.structurecreation.model.ProjectNode("App.js", com.structurecreation.model.ProjectNode.NodeType.FILE);
        var indexJs = new com.structurecreation.model.ProjectNode("index.js", com.structurecreation.model.ProjectNode.NodeType.FILE);
        var appCss = new com.structurecreation.model.ProjectNode("App.css", com.structurecreation.model.ProjectNode.NodeType.FILE);
        var indexHtml = new com.structurecreation.model.ProjectNode("index.html", com.structurecreation.model.ProjectNode.NodeType.FILE);
        
        src.addChild(appJs);
        src.addChild(indexJs);
        src.addChild(appCss);
        src.addChild(components);
        src.addChild(hooks);
        src.addChild(utils);
        
        public_.addChild(indexHtml);
        
        root.addChild(src);
        root.addChild(public_);
        
        saveTemplate(template);
    }
    
    private String determineCategory(String projectType) {
        String lowerType = projectType.toLowerCase();
        if (lowerType.contains("java") || lowerType.contains("spring")) {
            return "Java";
        } else if (lowerType.contains("python")) {
            return "Python";
        } else if (lowerType.contains("node") || lowerType.contains("javascript")) {
            return "JavaScript";
        } else if (lowerType.contains("react") || lowerType.contains("vue") || lowerType.contains("angular")) {
            return "Frontend";
        } else if (lowerType.contains("c#") || lowerType.contains("dotnet")) {
            return ".NET";
        }
        return "Custom";
    }
    
    private String[] generateTags(ProjectStructure projectStructure) {
        List<String> tags = new ArrayList<>();
        
        String projectType = projectStructure.getProjectType().toLowerCase();
        
        // Add project type tags
        if (projectType.contains("java")) tags.add("java");
        if (projectType.contains("maven")) tags.add("maven");
        if (projectType.contains("gradle")) tags.add("gradle");
        if (projectType.contains("spring")) tags.add("spring");
        if (projectType.contains("python")) tags.add("python");
        if (projectType.contains("node")) tags.add("nodejs");
        if (projectType.contains("react")) tags.add("react");
        if (projectType.contains("vue")) tags.add("vue");
        if (projectType.contains("angular")) tags.add("angular");
        
        // Add dependency-based tags
        if (projectStructure.getDependencies() != null) {
            for (var dep : projectStructure.getDependencies()) {
                String depName = dep.getName().toLowerCase();
                if (depName.contains("spring")) tags.add("spring");
                if (depName.contains("junit")) tags.add("testing");
                if (depName.contains("react")) tags.add("react");
                if (depName.contains("express")) tags.add("express");
                if (depName.contains("flask") || depName.contains("django")) tags.add("web-framework");
            }
        }
        
        return tags.toArray(new String[0]);
    }
    
    private String sanitizeFileName(String fileName) {
        return fileName.replaceAll("[^a-zA-Z0-9._-]", "_");
    }
}