package com.structurecreation.service;

import com.structurecreation.service.EnvironmentManager.EnvironmentInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for EnvironmentManager
 */
class EnvironmentManagerTest {
    
    @BeforeEach
    void setUp() {
        // Clear cache before each test
        EnvironmentManager.clearCache();
    }
    
    @Test
    void testInitializeEnvironments() {
        EnvironmentManager.initializeEnvironments();
        Map<String, EnvironmentInfo> environments = EnvironmentManager.getAllEnvironments();
        
        assertNotNull(environments);
        assertFalse(environments.isEmpty());
        
        // Should detect at least Java (since we're running tests)
        assertTrue(environments.containsKey("Java"));
    }
    
    @Test
    void testGetEnvironment() {
        EnvironmentInfo javaInfo = EnvironmentManager.getEnvironment("Java");
        
        assertNotNull(javaInfo);
        assertEquals("Java", javaInfo.getName());
        // Java should be available since we're running tests with it
        assertTrue(javaInfo.isAvailable());
        assertNotNull(javaInfo.getVersion());
    }
    
    @Test
    void testIsAvailable() {
        // Java should be available
        assertTrue(EnvironmentManager.isAvailable("Java"));
        
        // Non-existent tool should not be available
        assertFalse(EnvironmentManager.isAvailable("NonExistentTool"));
    }
    
    @Test
    void testGetRequiredToolsForProjectType() {
        List<String> javaMavenTools = EnvironmentManager.getRequiredToolsForProjectType("Java Maven");
        assertEquals(2, javaMavenTools.size());
        assertTrue(javaMavenTools.contains("Java"));
        assertTrue(javaMavenTools.contains("Maven"));
        
        List<String> pythonTools = EnvironmentManager.getRequiredToolsForProjectType("Python");
        assertEquals(2, pythonTools.size());
        assertTrue(pythonTools.contains("Python"));
        assertTrue(pythonTools.contains("Pip"));
        
        List<String> nodeTools = EnvironmentManager.getRequiredToolsForProjectType("Node.js");
        assertEquals(2, nodeTools.size());
        assertTrue(nodeTools.contains("Node.js"));
        assertTrue(nodeTools.contains("NPM"));
    }
    
    @Test
    void testAreRequiredToolsAvailable() {
        // This test depends on the actual environment, so we just test the logic
        boolean result = EnvironmentManager.areRequiredToolsAvailable("Java Maven");
        
        // Result should be deterministic
        assertEquals(result, EnvironmentManager.areRequiredToolsAvailable("Java Maven"));
    }
    
    @Test
    void testGetMissingTools() {
        List<String> missingTools = EnvironmentManager.getMissingTools("Java Maven");
        
        assertNotNull(missingTools);
        
        // If Java is available, it should not be in missing tools
        if (EnvironmentManager.isAvailable("Java")) {
            assertFalse(missingTools.contains("Java"));
        }
    }
    
    @Test
    void testClearCache() {
        // Initialize first
        EnvironmentManager.initializeEnvironments();
        Map<String, EnvironmentInfo> environments1 = EnvironmentManager.getAllEnvironments();
        assertFalse(environments1.isEmpty());
        
        // Clear and reinitialize
        EnvironmentManager.clearCache();
        Map<String, EnvironmentInfo> environments2 = EnvironmentManager.getAllEnvironments();
        
        // Both should have the same size
        assertEquals(environments1.size(), environments2.size());
    }
    
    @Test
    void testEnvironmentInfo() {
        EnvironmentInfo info = new EnvironmentInfo("TestTool", "1.0.0", "/test/path", true);
        
        assertEquals("TestTool", info.getName());
        assertEquals("1.0.0", info.getVersion());
        assertEquals("/test/path", info.getPath());
        assertTrue(info.isAvailable());
        
        String toString = info.toString();
        assertTrue(toString.contains("TestTool"));
        assertTrue(toString.contains("1.0.0"));
    }
    
    @Test
    void testEnvironmentInfoNotAvailable() {
        EnvironmentInfo info = new EnvironmentInfo("TestTool", null, null, false);
        
        assertEquals("TestTool", info.getName());
        assertNull(info.getVersion());
        assertNull(info.getPath());
        assertFalse(info.isAvailable());
        
        String toString = info.toString();
        assertTrue(toString.contains("Not Available"));
    }
}
