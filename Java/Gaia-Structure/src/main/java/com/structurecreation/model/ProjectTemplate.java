package com.structurecreation.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import javafx.collections.ObservableList;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents a project template that can be saved and loaded
 */
public class ProjectTemplate {
    
    private String templateName;
    private String templateDescription;
    private String projectType;
    private ProjectNode templateStructure;
    private ObservableList<Dependency> templateDependencies;
    private String author;
    private String version;
    private LocalDateTime createdDate;
    private LocalDateTime lastModified;
    private String category;
    private String[] tags;
    
    public ProjectTemplate() {
        this.templateName = "";
        this.templateDescription = "";
        this.projectType = "Custom";
        this.templateStructure = new ProjectNode("Template Root", ProjectNode.NodeType.FOLDER);
        this.author = System.getProperty("user.name", "Unknown");
        this.version = "1.0.0";
        this.createdDate = LocalDateTime.now();
        this.lastModified = LocalDateTime.now();
        this.category = "Custom";
        this.tags = new String[0];
    }
    
    @JsonCreator
    public ProjectTemplate(
            @JsonProperty("templateName") String templateName,
            @JsonProperty("templateDescription") String templateDescription,
            @JsonProperty("projectType") String projectType,
            @JsonProperty("templateStructure") ProjectNode templateStructure,
            @JsonProperty("author") String author,
            @JsonProperty("version") String version,
            @JsonProperty("category") String category) {
        this.templateName = templateName != null ? templateName : "";
        this.templateDescription = templateDescription != null ? templateDescription : "";
        this.projectType = projectType != null ? projectType : "Custom";
        this.templateStructure = templateStructure != null ? templateStructure : new ProjectNode("Template Root", ProjectNode.NodeType.FOLDER);
        this.author = author != null ? author : System.getProperty("user.name", "Unknown");
        this.version = version != null ? version : "1.0.0";
        this.category = category != null ? category : "Custom";
        this.createdDate = LocalDateTime.now();
        this.lastModified = LocalDateTime.now();
        this.tags = new String[0];
    }
    
    /**
     * Creates a template from a ProjectStructure
     */
    public static ProjectTemplate fromProjectStructure(ProjectStructure projectStructure, String templateName, String templateDescription) {
        ProjectTemplate template = new ProjectTemplate();
        template.setTemplateName(templateName);
        template.setTemplateDescription(templateDescription);
        template.setProjectType(projectStructure.getProjectType());
        template.setTemplateStructure(cloneProjectNode(projectStructure.getRootNode()));
        template.setTemplateDependencies(projectStructure.getDependencies());
        template.setAuthor(projectStructure.getAuthor());
        template.setVersion(projectStructure.getVersion());
        template.setLastModified(LocalDateTime.now());
        return template;
    }
    
    /**
     * Converts template to ProjectStructure
     */
    public ProjectStructure toProjectStructure(String projectName, String projectLocation) {
        ProjectStructure projectStructure = new ProjectStructure();
        projectStructure.setProjectName(projectName);
        projectStructure.setProjectLocation(projectLocation);
        projectStructure.setProjectType(this.projectType);
        projectStructure.setRootNode(cloneProjectNode(this.templateStructure));
        projectStructure.setDependencies(this.templateDependencies);
        projectStructure.setAuthor(this.author);
        projectStructure.setVersion(this.version);
        return projectStructure;
    }
    
    private static ProjectNode cloneProjectNode(ProjectNode original) {
        if (original == null) return null;
        
        ProjectNode clone = new ProjectNode(original.getName(), original.getType(), original.getContent());
        
        for (ProjectNode child : original.getChildren()) {
            clone.addChild(cloneProjectNode(child));
        }
        
        return clone;
    }
    
    // Getters and Setters
    public String getTemplateName() {
        return templateName;
    }
    
    public void setTemplateName(String templateName) {
        this.templateName = templateName != null ? templateName : "";
        this.lastModified = LocalDateTime.now();
    }
    
    public String getTemplateDescription() {
        return templateDescription;
    }
    
    public void setTemplateDescription(String templateDescription) {
        this.templateDescription = templateDescription != null ? templateDescription : "";
        this.lastModified = LocalDateTime.now();
    }
    
    public String getProjectType() {
        return projectType;
    }
    
    public void setProjectType(String projectType) {
        this.projectType = projectType != null ? projectType : "Custom";
        this.lastModified = LocalDateTime.now();
    }
    
    public ProjectNode getTemplateStructure() {
        return templateStructure;
    }
    
    public void setTemplateStructure(ProjectNode templateStructure) {
        this.templateStructure = templateStructure != null ? templateStructure : new ProjectNode("Template Root", ProjectNode.NodeType.FOLDER);
        this.lastModified = LocalDateTime.now();
    }
    
    public ObservableList<Dependency> getTemplateDependencies() {
        return templateDependencies;
    }
    
    public void setTemplateDependencies(ObservableList<Dependency> templateDependencies) {
        this.templateDependencies = templateDependencies;
        this.lastModified = LocalDateTime.now();
    }
    
    public String getAuthor() {
        return author;
    }
    
    public void setAuthor(String author) {
        this.author = author != null ? author : "Unknown";
    }
    
    public String getVersion() {
        return version;
    }
    
    public void setVersion(String version) {
        this.version = version != null ? version : "1.0.0";
        this.lastModified = LocalDateTime.now();
    }
    
    public LocalDateTime getCreatedDate() {
        return createdDate;
    }
    
    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate != null ? createdDate : LocalDateTime.now();
    }
    
    public LocalDateTime getLastModified() {
        return lastModified;
    }
    
    public void setLastModified(LocalDateTime lastModified) {
        this.lastModified = lastModified != null ? lastModified : LocalDateTime.now();
    }
    
    public String getCategory() {
        return category;
    }
    
    public void setCategory(String category) {
        this.category = category != null ? category : "Custom";
        this.lastModified = LocalDateTime.now();
    }
    
    public String[] getTags() {
        return tags != null ? tags.clone() : new String[0];
    }
    
    public void setTags(String[] tags) {
        this.tags = tags != null ? tags.clone() : new String[0];
        this.lastModified = LocalDateTime.now();
    }
    
    // Utility methods
    public boolean isValidTemplate() {
        return !templateName.trim().isEmpty() && 
               !projectType.trim().isEmpty() && 
               templateStructure != null;
    }
    
    public String getDisplayName() {
        return templateName + " (" + projectType + ")";
    }
    
    public String getSummary() {
        return String.format("%s - %s files, %s folders", 
                templateName, 
                countFiles(templateStructure), 
                countFolders(templateStructure));
    }
    
    private int countFiles(ProjectNode node) {
        if (node == null) return 0;
        int count = node.isFile() ? 1 : 0;
        for (ProjectNode child : node.getChildren()) {
            count += countFiles(child);
        }
        return count;
    }
    
    private int countFolders(ProjectNode node) {
        if (node == null) return 0;
        int count = node.isFolder() ? 1 : 0;
        for (ProjectNode child : node.getChildren()) {
            count += countFolders(child);
        }
        return count;
    }
    
    @Override
    public String toString() {
        return getDisplayName();
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProjectTemplate that = (ProjectTemplate) o;
        return Objects.equals(templateName, that.templateName) &&
                Objects.equals(projectType, that.projectType) &&
                Objects.equals(author, that.author);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(templateName, projectType, author);
    }
}