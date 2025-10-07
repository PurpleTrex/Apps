package com.structurecreation.model;

import javafx.collections.ObservableList;
import java.util.Objects;

/**
 * Represents the complete project structure configuration
 */
public class ProjectStructure {
    
    private String projectName;
    private String projectLocation;
    private String projectType;
    private ProjectNode rootNode;
    private ObservableList<Dependency> dependencies;
    private String description;
    private String author;
    private String version;
    
    public ProjectStructure() {
        this.projectName = "";
        this.projectLocation = "";
        this.projectType = "Custom";
        this.rootNode = new ProjectNode("Project Root", ProjectNode.NodeType.FOLDER);
        this.description = "";
        this.author = System.getProperty("user.name", "Unknown");
        this.version = "1.0.0";
    }
    
    // Getters and Setters
    public String getProjectName() {
        return projectName;
    }
    
    public void setProjectName(String projectName) {
        this.projectName = projectName != null ? projectName : "";
    }
    
    public String getProjectLocation() {
        return projectLocation;
    }
    
    public void setProjectLocation(String projectLocation) {
        this.projectLocation = projectLocation != null ? projectLocation : "";
    }
    
    public String getProjectType() {
        return projectType;
    }
    
    public void setProjectType(String projectType) {
        this.projectType = projectType != null ? projectType : "Custom";
    }
    
    public ProjectNode getRootNode() {
        return rootNode;
    }
    
    public void setRootNode(ProjectNode rootNode) {
        this.rootNode = rootNode != null ? rootNode : new ProjectNode("Project Root", ProjectNode.NodeType.FOLDER);
    }
    
    public ObservableList<Dependency> getDependencies() {
        return dependencies;
    }
    
    public void setDependencies(ObservableList<Dependency> dependencies) {
        this.dependencies = dependencies;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description != null ? description : "";
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
    }
    
    // Utility methods
    public String getFullProjectPath() {
        return projectLocation + "/" + projectName;
    }
    
    public boolean isValidProject() {
        return !projectName.trim().isEmpty() && 
               !projectLocation.trim().isEmpty() && 
               rootNode != null;
    }
    
    public int getTotalFileCount() {
        return countFiles(rootNode);
    }
    
    public int getTotalFolderCount() {
        return countFolders(rootNode);
    }
    
    private int countFiles(ProjectNode node) {
        int count = 0;
        if (node.isFile()) {
            count = 1;
        }
        for (ProjectNode child : node.getChildren()) {
            count += countFiles(child);
        }
        return count;
    }
    
    private int countFolders(ProjectNode node) {
        int count = 0;
        if (node.isFolder()) {
            count = 1;
        }
        for (ProjectNode child : node.getChildren()) {
            count += countFolders(child);
        }
        return count;
    }
    
    @Override
    public String toString() {
        return "ProjectStructure{" +
                "projectName='" + projectName + '\'' +
                ", projectType='" + projectType + '\'' +
                ", location='" + projectLocation + '\'' +
                ", files=" + getTotalFileCount() +
                ", folders=" + getTotalFolderCount() +
                ", dependencies=" + (dependencies != null ? dependencies.size() : 0) +
                '}';
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProjectStructure that = (ProjectStructure) o;
        return Objects.equals(projectName, that.projectName) &&
                Objects.equals(projectLocation, that.projectLocation) &&
                Objects.equals(projectType, that.projectType);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(projectName, projectLocation, projectType);
    }
}