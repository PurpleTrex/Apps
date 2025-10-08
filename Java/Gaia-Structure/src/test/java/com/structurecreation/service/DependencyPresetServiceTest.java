package com.structurecreation.service;

import com.structurecreation.service.DependencyPresetService.DependencyPreset;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for DependencyPresetService enhancements
 */
class DependencyPresetServiceTest {
    
    @Test
    void testGetPresetsForProjectType() {
        List<DependencyPreset> presets = DependencyPresetService.getPresetsForProjectType("Java Maven");
        
        assertNotNull(presets);
        assertFalse(presets.isEmpty());
        
        // All presets should be Maven type
        assertTrue(presets.stream().allMatch(p -> "Maven".equals(p.getType())));
    }
    
    @Test
    void testAreCompatible() {
        // Create compatible dependencies (same type)
        DependencyPreset maven1 = new DependencyPreset(
            "JUnit", "Testing", "Maven", "org.junit.jupiter:junit-jupiter", "5.10.0", false
        );
        DependencyPreset maven2 = new DependencyPreset(
            "Logback", "Logging", "Maven", "ch.qos.logback:logback-classic", "1.4.11", false
        );
        
        List<DependencyPreset> compatible = Arrays.asList(maven1, maven2);
        assertTrue(DependencyPresetService.areCompatible(compatible));
        
        // Create incompatible dependencies (different types)
        DependencyPreset npm = new DependencyPreset(
            "React", "UI Library", "NPM", "react", "18.2.0", false
        );
        
        List<DependencyPreset> incompatible = Arrays.asList(maven1, npm);
        assertFalse(DependencyPresetService.areCompatible(incompatible));
    }
    
    @Test
    void testCheckVersionConflicts() {
        // Dependencies with different versions of the same package
        DependencyPreset junit1 = new DependencyPreset(
            "JUnit 5.9", "Testing", "Maven", "org.junit.jupiter:junit-jupiter", "5.9.0", false
        );
        DependencyPreset junit2 = new DependencyPreset(
            "JUnit 5.10", "Testing", "Maven", "org.junit.jupiter:junit-jupiter", "5.10.0", false
        );
        
        List<DependencyPreset> conflicting = Arrays.asList(junit1, junit2);
        List<String> conflicts = DependencyPresetService.checkVersionConflicts(conflicting);
        
        assertNotNull(conflicts);
        assertFalse(conflicts.isEmpty());
    }
    
    @Test
    void testIsVersionCompatible() {
        // Test with JUnit preset from Java Maven
        boolean compatible = DependencyPresetService.isVersionCompatible("Java Maven", "JUnit 5", "5.10.0");
        
        // Should return true (version is in compatible list or presets allow it)
        assertTrue(compatible);
        
        // Test with non-existent dependency
        boolean nonExistent = DependencyPresetService.isVersionCompatible("Java Maven", "NonExistentLib", "1.0.0");
        
        // Should return true (not in presets, assume compatible)
        assertTrue(nonExistent);
    }
    
    @Test
    void testGetDependencyConflicts() {
        // Create compatible dependencies
        DependencyPreset maven1 = new DependencyPreset(
            "JUnit", "Testing", "Maven", "org.junit.jupiter:junit-jupiter", "5.10.0", false
        );
        DependencyPreset maven2 = new DependencyPreset(
            "Logback", "Logging", "Maven", "ch.qos.logback:logback-classic", "1.4.11", false
        );
        
        List<DependencyPreset> noConflicts = Arrays.asList(maven1, maven2);
        List<String> conflicts1 = DependencyPresetService.getDependencyConflicts(noConflicts);
        
        assertTrue(conflicts1.isEmpty());
        
        // Create conflicting dependencies (Django and Flask)
        DependencyPreset django = new DependencyPreset(
            "Django", "Web Framework", "Pip", "django", "4.2.5", false
        );
        DependencyPreset flask = new DependencyPreset(
            "Flask", "Web Framework", "Pip", "flask", "2.3.3", false
        );
        
        List<DependencyPreset> withConflicts = Arrays.asList(django, flask);
        List<String> conflicts2 = DependencyPresetService.getDependencyConflicts(withConflicts);
        
        assertFalse(conflicts2.isEmpty());
        assertTrue(conflicts2.stream().anyMatch(c -> c.contains("incompatible")));
    }
    
    @Test
    void testIsCompatibleWithProjectType() {
        // Test Java Maven
        assertTrue(DependencyPresetService.isCompatibleWithProjectType("Java Maven", "Maven"));
        assertFalse(DependencyPresetService.isCompatibleWithProjectType("Java Maven", "NPM"));
        
        // Test Python
        assertTrue(DependencyPresetService.isCompatibleWithProjectType("Python", "Pip"));
        assertFalse(DependencyPresetService.isCompatibleWithProjectType("Python", "Maven"));
        
        // Test Node.js
        assertTrue(DependencyPresetService.isCompatibleWithProjectType("Node.js", "NPM"));
        assertTrue(DependencyPresetService.isCompatibleWithProjectType("Node.js", "Yarn"));
        assertFalse(DependencyPresetService.isCompatibleWithProjectType("Node.js", "Pip"));
        
        // Test Custom (should accept any)
        assertTrue(DependencyPresetService.isCompatibleWithProjectType("Custom", "Maven"));
        assertTrue(DependencyPresetService.isCompatibleWithProjectType("Custom", "NPM"));
    }
    
    @Test
    void testGetRecommendedVersion() {
        String version = DependencyPresetService.getRecommendedVersion("Java Maven", "JUnit 5");
        
        assertNotNull(version);
        assertFalse(version.isEmpty());
        
        // Non-existent dependency should return "latest"
        String nonExistent = DependencyPresetService.getRecommendedVersion("Java Maven", "NonExistentLib");
        assertEquals("latest", nonExistent);
    }
    
    @Test
    void testDependencyPreset() {
        DependencyPreset preset = new DependencyPreset(
            "JUnit", "Testing Framework", "Maven", "org.junit.jupiter:junit-jupiter", 
            "5.10.0", true, "5.9.0", "5.10.0", "5.11.0"
        );
        
        assertEquals("JUnit", preset.getName());
        assertEquals("Testing Framework", preset.getDescription());
        assertEquals("Maven", preset.getType());
        assertEquals("org.junit.jupiter:junit-jupiter", preset.getArtifact());
        assertEquals("5.10.0", preset.getVersion());
        assertTrue(preset.isRequired());
        assertEquals(3, preset.getCompatibleVersions().size());
        
        String toString = preset.toString();
        assertTrue(toString.contains("JUnit"));
        assertTrue(toString.contains("Required"));
    }
}
