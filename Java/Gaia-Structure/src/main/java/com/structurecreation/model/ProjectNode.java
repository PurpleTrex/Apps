package com.structurecreation.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents a node in the project structure tree (file or folder)
 */
public class ProjectNode {
    
    public enum NodeType {
        FILE, FOLDER
    }
    
    private String name;
    private NodeType type;
    private List<ProjectNode> children;
    private String content; // For files that need specific content
    
    public ProjectNode(String name, NodeType type) {
        this.name = name;
        this.type = type;
        this.children = new ArrayList<>();
        this.content = "";
    }
    
    public ProjectNode(String name, NodeType type, String content) {
        this.name = name;
        this.type = type;
        this.children = new ArrayList<>();
        this.content = content != null ? content : "";
    }
    
    // Getters and Setters
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public NodeType getType() {
        return type;
    }
    
    public void setType(NodeType type) {
        this.type = type;
    }
    
    public List<ProjectNode> getChildren() {
        return children;
    }
    
    public void setChildren(List<ProjectNode> children) {
        this.children = children != null ? children : new ArrayList<>();
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content != null ? content : "";
    }
    
    // Utility methods
    public boolean isFolder() {
        return type == NodeType.FOLDER;
    }
    
    public boolean isFile() {
        return type == NodeType.FILE;
    }

    public boolean isDirectory() {
        return isFolder();
    }
    
    public void addChild(ProjectNode child) {
        if (this.isFolder()) {
            this.children.add(child);
        }
    }
    
    public void removeChild(ProjectNode child) {
        this.children.remove(child);
    }
    
    public boolean hasChildren() {
        return !children.isEmpty();
    }
    
    public ProjectNode findChild(String name) {
        return children.stream()
                .filter(child -> child.getName().equals(name))
                .findFirst()
                .orElse(null);
    }
    
    public String getFullPath() {
        return getFullPath(this, "");
    }
    
    private String getFullPath(ProjectNode node, String currentPath) {
        if (currentPath.isEmpty()) {
            return node.getName();
        }
        return currentPath + "/" + node.getName();
    }
    
    @Override
    public String toString() {
        return name;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProjectNode that = (ProjectNode) o;
        return Objects.equals(name, that.name) && type == that.type;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(name, type);
    }
}