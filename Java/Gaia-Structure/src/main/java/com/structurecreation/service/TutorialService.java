package com.structurecreation.service;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service for displaying tutorials and help dialogs
 */
public class TutorialService {
    
    private static final Logger logger = LoggerFactory.getLogger(TutorialService.class);
    
    /**
     * Show the Quick Start tutorial
     */
    public void showQuickStartTutorial() {
        Alert tutorial = new Alert(Alert.AlertType.INFORMATION);
        tutorial.setTitle("Quick Start Tutorial");
        tutorial.setHeaderText("Welcome to Gaia!");
        
        VBox content = new VBox(15);
        content.setPadding(new Insets(10));
        
        // Step-by-step instructions
        String[] steps = {
            "1. Project Information",
            "   • Enter your project name",
            "   • Choose a location folder", 
            "   • Select project type (Java Maven, Spring Boot, Python, etc.)",
            "",
            "2. Build Project Structure", 
            "   • Use the tree view on the left to add folders and files",
            "   • Right-click or use toolbar buttons to add/edit/delete items",
            "",
            "3. Add Dependencies",
            "   • Quick Add: Select from preset dependencies for your project type",
            "   • Custom Add: Manually enter any dependency name and version",
            "",
            "4. Create Project",
            "   • Click 'Create Project' to generate your complete project structure",
            "   • Dependencies will be automatically configured and installed"
        };
        
        Label instructionsLabel = new Label(String.join("\n", steps));
        instructionsLabel.setFont(Font.font("Consolas", 12));
        instructionsLabel.setWrapText(true);
        
        Label tipLabel = new Label("💡 Tip: Hover over any UI element to see helpful tooltips!");
        tipLabel.setFont(Font.font(null, FontWeight.BOLD, 12));
        tipLabel.setStyle("-fx-text-fill: #0078d4;");
        
        content.getChildren().addAll(instructionsLabel, new Separator(), tipLabel);
        
        tutorial.getDialogPane().setContent(content);
        tutorial.getDialogPane().setPrefWidth(600);
        tutorial.getDialogPane().setPrefHeight(500);
        
        tutorial.showAndWait();
        logger.info("Displayed Quick Start tutorial");
    }
    
    /**
     * Show dependency management guide
     */
    public void showDependencyGuide() {
        Alert guide = new Alert(Alert.AlertType.INFORMATION);
        guide.setTitle("Dependency Management Guide");
        guide.setHeaderText("How to Manage Dependencies");
        
        VBox content = new VBox(15);
        content.setPadding(new Insets(10));
        
        Label presetLabel = new Label("📦 Preset Dependencies");
        presetLabel.setFont(Font.font(null, FontWeight.BOLD, 14));
        
        Label presetText = new Label(
            "• Select your project type first (Java Maven, Spring Boot, etc.)\n" +
            "• Preset dropdown will populate with popular, tested dependencies\n" +
            "• Click 'Add Preset' to add the selected dependency\n" +
            "• These are curated versions that work well together"
        );
        presetText.setWrapText(true);
        
        Label customLabel = new Label("⚙️ Custom Dependencies");
        customLabel.setFont(Font.font(null, FontWeight.BOLD, 14));
        
        Label customText = new Label(
            "• Choose dependency type (Maven, NPM, Pip, etc.)\n" +
            "• Enter exact dependency name (e.g., 'spring-boot-starter-web')\n" +
            "• Specify version or leave empty for latest\n" +
            "• Click 'Add Dependency' - it will be validated against the repository\n" +
            "• Supports all major package managers: Maven Central, NPM, PyPI, NuGet"
        );
        customText.setWrapText(true);
        
        Label examplesLabel = new Label("📝 Examples");
        examplesLabel.setFont(Font.font(null, FontWeight.BOLD, 14));
        
        Label examplesText = new Label(
            "Java Maven: org.springframework.boot:spring-boot-starter-web, version 3.1.4\n" +
            "NPM: react, version 18.2.0\n" +
            "Python: django, version 4.2.5\n" +
            "NuGet: Newtonsoft.Json, version 13.0.3"
        );
        examplesText.setFont(Font.font("Consolas", 11));
        examplesText.setWrapText(true);
        
        content.getChildren().addAll(
            presetLabel, presetText, new Separator(),
            customLabel, customText, new Separator(), 
            examplesLabel, examplesText
        );
        
        guide.getDialogPane().setContent(content);
        guide.getDialogPane().setPrefWidth(650);
        guide.getDialogPane().setPrefHeight(550);
        
        guide.showAndWait();
        logger.info("Displayed Dependency Management guide");
    }
    
    /**
     * Show project types overview
     */
    public void showProjectTypesOverview() {
        Alert overview = new Alert(Alert.AlertType.INFORMATION);
        overview.setTitle("Project Types Overview");
        overview.setHeaderText("Available Project Types and Their Features");
        
        VBox content = new VBox(10);
        content.setPadding(new Insets(10));
        
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(8);
        
        String[][] projectTypes = {
            {"Java Maven", "Standard Maven project structure, pom.xml, Maven dependencies"},
            {"Java Gradle", "Gradle build system, build.gradle, Gradle dependencies"},
            {"Spring Boot", "Spring Boot application, embedded server, auto-configuration"},
            {"Python", "Python project, requirements.txt, pip dependencies"},
            {"Node.js", "Node.js application, package.json, NPM dependencies"},
            {"React", "React frontend application, JSX components, NPM packages"},
            {"Django", "Django web framework, Python dependencies, project structure"},
            {"Flask", "Flask micro-framework, lightweight Python web apps"},
            {"Custom", "Generic project structure, manual dependency management"}
        };
        
        Label headerType = new Label("Project Type");
        headerType.setFont(Font.font(null, FontWeight.BOLD, 12));
        Label headerDesc = new Label("Description & Features");
        headerDesc.setFont(Font.font(null, FontWeight.BOLD, 12));
        
        grid.add(headerType, 0, 0);
        grid.add(headerDesc, 1, 0);
        
        for (int i = 0; i < projectTypes.length; i++) {
            Label typeLabel = new Label(projectTypes[i][0]);
            typeLabel.setFont(Font.font(null, FontWeight.BOLD, 11));
            typeLabel.setStyle("-fx-text-fill: #0078d4;");
            
            Label descLabel = new Label(projectTypes[i][1]);
            descLabel.setWrapText(true);
            descLabel.setPrefWidth(400);
            
            grid.add(typeLabel, 0, i + 1);
            grid.add(descLabel, 1, i + 1);
        }
        
        content.getChildren().addAll(grid);
        
        overview.getDialogPane().setContent(content);
        overview.getDialogPane().setPrefWidth(700);
        overview.getDialogPane().setPrefHeight(500);
        
        overview.showAndWait();
        logger.info("Displayed Project Types overview");
    }
    
    /**
     * Show keyboard shortcuts
     */
    public void showKeyboardShortcuts() {
        Alert shortcuts = new Alert(Alert.AlertType.INFORMATION);
        shortcuts.setTitle("Keyboard Shortcuts");
        shortcuts.setHeaderText("Available Keyboard Shortcuts");
        
        VBox content = new VBox(10);
        content.setPadding(new Insets(10));
        
        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(5);
        
        String[][] shortcutList = {
            {"F1", "Quick Start Tutorial"},
            {"Ctrl+N", "New Project"},
            {"Ctrl+O", "Open Project"},
            {"Ctrl+S", "Save as Template"},
            {"Ctrl+L", "Load Template"},
            {"Delete", "Delete selected tree item"},
            {"F2", "Rename selected tree item"},
            {"Ctrl+Enter", "Create Project"},
            {"Escape", "Cancel current operation"},
            {"Alt+F4", "Exit application"}
        };
        
        for (int i = 0; i < shortcutList.length; i++) {
            Label keyLabel = new Label(shortcutList[i][0]);
            keyLabel.setFont(Font.font("Consolas", FontWeight.BOLD, 11));
            keyLabel.setStyle("-fx-background-color: #f0f0f0; -fx-padding: 2 6 2 6; -fx-background-radius: 3;");
            
            Label actionLabel = new Label(shortcutList[i][1]);
            
            grid.add(keyLabel, 0, i);
            grid.add(actionLabel, 1, i);
        }
        
        content.getChildren().add(grid);
        
        shortcuts.getDialogPane().setContent(content);
        shortcuts.getDialogPane().setPrefWidth(400);
        shortcuts.getDialogPane().setPrefHeight(350);
        
        shortcuts.showAndWait();
        logger.info("Displayed keyboard shortcuts");
    }
    
    /**
     * Show troubleshooting guide
     */
    public void showTroubleshooting() {
        Alert troubleshooting = new Alert(Alert.AlertType.INFORMATION);
        troubleshooting.setTitle("Troubleshooting");
        troubleshooting.setHeaderText("Common Issues and Solutions");
        
        VBox content = new VBox(15);
        content.setPadding(new Insets(10));
        
        String[] issues = {
            "❌ \"Dependency not found\" error:",
            "   • Check spelling of dependency name",
            "   • Verify the dependency exists in the repository",
            "   • Try without version number to use latest",
            "",
            "❌ Preset dropdown is empty:",
            "   • Make sure you've selected a project type (not 'Custom')",
            "   • Try selecting a different project type like 'Java Maven' or 'Spring Boot'",
            "",
            "❌ Project creation fails:",
            "   • Ensure the project location exists and is writable",
            "   • Check that project name contains valid characters",
            "   • Verify you have internet connection for dependency downloads",
            "",
            "❌ Dependencies not installing:",
            "   • Check your internet connection",
            "   • Verify you have the required package manager installed (Maven, NPM, etc.)",
            "   • Some dependencies may require manual installation",
            "",
            "💡 Still having issues?",
            "   • Check the application logs in the console",
            "   • Try creating a simple project first to test functionality",
            "   • Restart the application if needed"
        };
        
        Label issuesLabel = new Label(String.join("\n", issues));
        issuesLabel.setFont(Font.font("Consolas", 11));
        issuesLabel.setWrapText(true);
        
        content.getChildren().add(issuesLabel);
        
        troubleshooting.getDialogPane().setContent(content);
        troubleshooting.getDialogPane().setPrefWidth(650);
        troubleshooting.getDialogPane().setPrefHeight(500);
        
        troubleshooting.showAndWait();
        logger.info("Displayed troubleshooting guide");
    }
    
    /**
     * Show about dialog
     */
    public void showAbout() {
        Alert about = new Alert(Alert.AlertType.INFORMATION);
        about.setTitle("About");
        about.setHeaderText("Gaia - Project Structure Creator v1.0.0");
        
        VBox content = new VBox(15);
        content.setPadding(new Insets(10));
        
        Label descLabel = new Label(
            "Gaia is a powerful project structure creator with automated dependency management.\n\n" +
            "Features:\n" +
            "• Multiple project type templates\n" +
            "• Visual project structure builder\n" +
            "• Smart dependency management with presets\n" +
            "• Integration with Maven Central, NPM, PyPI, and more\n" +
            "• Template saving and loading\n" +
            "• Cross-platform compatibility"
        );
        descLabel.setWrapText(true);
        
        Label techLabel = new Label(
            "Built with:\n" +
            "• JavaFX 19 for the user interface\n" +
            "• Java 17 for core functionality\n" +
            "• Maven for dependency management\n" +
            "• SLF4J for logging"
        );
        techLabel.setWrapText(true);
        techLabel.setFont(Font.font("Consolas", 10));
        
        content.getChildren().addAll(descLabel, new Separator(), techLabel);
        
        about.getDialogPane().setContent(content);
        about.getDialogPane().setPrefWidth(500);
        about.getDialogPane().setPrefHeight(400);
        
        about.showAndWait();
        logger.info("Displayed about dialog");
    }
}