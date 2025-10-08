package com.structurecreation.util;

import com.structurecreation.service.EnvironmentManager;
import com.structurecreation.service.EnvironmentManager.EnvironmentInfo;

import java.util.Map;

/**
 * Simple utility to test environment detection from command line
 */
public class EnvironmentDetectionTest {
    
    public static void main(String[] args) {
        System.out.println("=== Gaia Environment Detection Test ===\n");
        
        // Initialize environment detection
        System.out.println("Initializing environment detection...");
        EnvironmentManager.initializeEnvironments();
        
        // Get all environments
        Map<String, EnvironmentInfo> environments = EnvironmentManager.getAllEnvironments();
        
        System.out.println("\nDetected Environments:\n");
        System.out.println(String.format("%-15s %-15s %s", "Tool", "Status", "Version/Info"));
        System.out.println("-".repeat(60));
        
        for (Map.Entry<String, EnvironmentInfo> entry : environments.entrySet()) {
            EnvironmentInfo info = entry.getValue();
            String status = info.isAvailable() ? "✓ Available" : "✗ Not Found";
            String version = info.isAvailable() ? info.getVersion() : "-";
            
            System.out.println(String.format("%-15s %-15s %s", 
                info.getName(), 
                status, 
                version));
        }
        
        // Test project type requirements
        System.out.println("\n\nProject Type Requirements:\n");
        
        String[] projectTypes = {"Java Maven", "Spring Boot", "Python", "Node.js", "React"};
        
        for (String projectType : projectTypes) {
            System.out.println("\n" + projectType + ":");
            
            var required = EnvironmentManager.getRequiredToolsForProjectType(projectType);
            System.out.println("  Required tools: " + String.join(", ", required));
            
            var missing = EnvironmentManager.getMissingTools(projectType);
            if (missing.isEmpty()) {
                System.out.println("  Status: ✓ All tools available");
            } else {
                System.out.println("  Status: ✗ Missing: " + String.join(", ", missing));
            }
        }
        
        System.out.println("\n=== Test Complete ===");
    }
}
