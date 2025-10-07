package com.structurecreation.model;

import java.util.Objects;

/**
 * Represents a project dependency (Maven, NPM, Pip, etc.)
 */
public class Dependency {
    
    private String type;
    private String name;
    private String version;
    private String description;
    private boolean optional;
    private String scope; // compile, test, runtime, etc.
    
    public Dependency() {
        this("Maven", "", "latest");
    }
    
    public Dependency(String type, String name, String version) {
        this.type = type != null ? type : "Maven";
        this.name = name != null ? name : "";
        this.version = version != null ? version : "latest";
        this.description = "";
        this.optional = false;
        this.scope = "compile";
    }
    
    public Dependency(String type, String name, String version, String description, boolean optional, String scope) {
        this.type = type != null ? type : "Maven";
        this.name = name != null ? name : "";
        this.version = version != null ? version : "latest";
        this.description = description != null ? description : "";
        this.optional = optional;
        this.scope = scope != null ? scope : "compile";
    }
    
    // Getters and Setters
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type != null ? type : "Maven";
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name != null ? name : "";
    }
    
    public String getVersion() {
        return version;
    }
    
    public void setVersion(String version) {
        this.version = version != null ? version : "latest";
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description != null ? description : "";
    }
    
    public boolean isOptional() {
        return optional;
    }
    
    public void setOptional(boolean optional) {
        this.optional = optional;
    }
    
    public String getScope() {
        return scope;
    }
    
    public void setScope(String scope) {
        this.scope = scope != null ? scope : "compile";
    }
    
    // Utility methods
    public String getFullName() {
        return type + ":" + name + ":" + version;
    }
    
    public boolean isValidDependency() {
        return !name.trim().isEmpty() && !type.trim().isEmpty();
    }
    
    /**
     * Formats the dependency for different package managers
     */
    public String getFormattedDependency() {
        switch (type.toLowerCase()) {
            case "maven":
                return formatMavenDependency();
            case "gradle":
                return formatGradleDependency();
            case "npm":
            case "yarn":
                return formatNpmDependency();
            case "pip":
                return formatPipDependency();
            case "nuget":
                return formatNuGetDependency();
            case "composer":
                return formatComposerDependency();
            case "gem":
                return formatGemDependency();
            case "go modules":
                return formatGoModulesDependency();
            default:
                return name + " " + version;
        }
    }
    
    private String formatMavenDependency() {
        StringBuilder sb = new StringBuilder();
        sb.append("<dependency>\n");
        sb.append("    <groupId>").append(getGroupId()).append("</groupId>\n");
        sb.append("    <artifactId>").append(getArtifactId()).append("</artifactId>\n");
        sb.append("    <version>").append(version).append("</version>\n");
        if (!scope.equals("compile")) {
            sb.append("    <scope>").append(scope).append("</scope>\n");
        }
        if (optional) {
            sb.append("    <optional>true</optional>\n");
        }
        sb.append("</dependency>");
        return sb.toString();
    }
    
    private String formatGradleDependency() {
        String configuration = scope.equals("test") ? "testImplementation" : "implementation";
        return configuration + " '" + name + ":" + version + "'";
    }
    
    private String formatNpmDependency() {
        return "\"" + name + "\": \"" + version + "\"";
    }
    
    private String formatPipDependency() {
        return name + "==" + version;
    }
    
    private String formatNuGetDependency() {
        return "<PackageReference Include=\"" + name + "\" Version=\"" + version + "\" />";
    }
    
    private String formatComposerDependency() {
        return "\"" + name + "\": \"" + version + "\"";
    }
    
    private String formatGemDependency() {
        return "gem '" + name + "', '" + version + "'";
    }
    
    private String formatGoModulesDependency() {
        return name + " " + version;
    }
    
    private String getGroupId() {
        if (name.contains(":")) {
            return name.split(":")[0];
        }
        // Default group ID patterns
        if (name.startsWith("org.") || name.startsWith("com.") || name.startsWith("io.")) {
            int lastDot = name.lastIndexOf('.');
            return lastDot > 0 ? name.substring(0, lastDot) : name;
        }
        return "org.example"; // fallback
    }
    
    private String getArtifactId() {
        if (name.contains(":")) {
            String[] parts = name.split(":");
            return parts.length > 1 ? parts[1] : parts[0];
        }
        int lastDot = name.lastIndexOf('.');
        return lastDot > 0 ? name.substring(lastDot + 1) : name;
    }
    
    @Override
    public String toString() {
        return type + ": " + name + " (" + version + ")";
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Dependency that = (Dependency) o;
        return Objects.equals(type, that.type) &&
                Objects.equals(name, that.name) &&
                Objects.equals(version, that.version);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(type, name, version);
    }
}