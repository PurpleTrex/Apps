package com.structurecreation.controller;

import com.structurecreation.model.ProjectNode;
import com.structurecreation.model.ProjectStructure;
import com.structurecreation.model.Dependency;
import com.structurecreation.service.ProjectCreationService;
import com.structurecreation.service.FileTreeGenerator;
import com.structurecreation.service.TemplateService;
import com.structurecreation.service.DependencyPresetService;
import com.structurecreation.service.DependencyPresetService.DependencyPreset;
import com.structurecreation.service.DependencyResolverService;
import com.structurecreation.service.TutorialService;
import com.structurecreation.service.EnvironmentManager;
import com.structurecreation.service.EnvironmentManager.EnvironmentInfo;
import com.structurecreation.service.generator.ReactProjectGenerator;
import com.structurecreation.model.ProjectTemplate;
import com.structurecreation.util.AlertUtils;
import com.structurecreation.util.TreeViewDragAndDrop;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTreeCell;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.geometry.Insets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Main Controller for the Project Structure Creator GUI
 */
public class MainController implements Initializable {

    private static final Logger logger = LoggerFactory.getLogger(MainController.class);

    // Tree View for project structure
    @FXML private TreeView<ProjectNode> projectTreeView;
    @FXML private Button addFolderButton;
    @FXML private Button addFileButton;
    @FXML private Button deleteButton;
    @FXML private Button renameButton;

    // Project info
    @FXML private TextField projectNameField;
    @FXML private TextField projectLocationField;
    @FXML private Button browseLocationButton;
    @FXML private ComboBox<String> projectTypeComboBox;

    // Dependencies table
    @FXML private TableView<Dependency> dependenciesTable;
    @FXML private TableColumn<Dependency, String> dependencyTypeColumn;
    @FXML private TableColumn<Dependency, String> dependencyNameColumn;
    @FXML private TableColumn<Dependency, String> dependencyVersionColumn;
    @FXML private Button addDependencyButton;
    @FXML private Button removeDependencyButton;

    // Dependency input fields
    @FXML private ComboBox<String> dependencyTypeComboBox;
    @FXML private TextField dependencyNameField;
    @FXML private TextField dependencyVersionField;
    @FXML private ComboBox<com.structurecreation.service.DependencyPresetService.DependencyPreset> dependencyPresetComboBox;
    @FXML private Button addPresetButton;

    // Actions
    @FXML private Button createProjectButton;
    @FXML private Button clearAllButton;
    @FXML private Button loadTemplateButton;
    @FXML private Button saveTemplateButton;

    // Menu items
    @FXML private MenuItem quickStartMenuItem;
    @FXML private MenuItem dependencyHelpMenuItem;
    @FXML private MenuItem projectTypesHelpMenuItem;
    @FXML private MenuItem keyboardShortcutsMenuItem;
    @FXML private MenuItem troubleshootingMenuItem;
    @FXML private MenuItem aboutMenuItem;
    @FXML private MenuItem environmentInfoMenuItem;
    @FXML private Button helpButton;

    // Status and progress
    @FXML private Label statusLabel;
    @FXML private ProgressBar progressBar;
    @FXML private VBox progressContainer;

    // Services
    private ProjectCreationService projectCreationService;
    private FileTreeGenerator fileTreeGenerator;
    private com.structurecreation.service.TemplateService templateService;
    private DependencyResolverService dependencyResolverService;
    private TutorialService tutorialService;
    private ReactProjectGenerator reactProjectGenerator;
    
    // Data
    private ProjectStructure currentProject;
    private ObservableList<Dependency> dependencies;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        logger.info("Initializing MainController");
        
        // Initialize environment detection in background
        Task<Void> envDetectionTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                EnvironmentManager.initializeEnvironments();
                return null;
            }
        };
        new Thread(envDetectionTask).start();
        
        // Initialize services
        projectCreationService = new ProjectCreationService();
        fileTreeGenerator = new FileTreeGenerator();
        templateService = new TemplateService();
        dependencyResolverService = new DependencyResolverService();
        tutorialService = new TutorialService();
        reactProjectGenerator = new ReactProjectGenerator();
        
        // Initialize data
        currentProject = new ProjectStructure();
        dependencies = FXCollections.observableArrayList();
        
        // Setup UI components
        setupProjectTreeView();
        setupProjectTypeComboBox();
        setupDependenciesTable();
        setupDependencyTypeComboBox();
        setupDependencyPresetComboBox();
        setupEventHandlers();
        
        // Initial UI state
        updateUIState();
        progressContainer.setVisible(false);
        
        logger.info("MainController initialized successfully");
    }

    private void setupProjectTreeView() {
        // Create root node
        ProjectNode rootNode = new ProjectNode("Project Root", ProjectNode.NodeType.FOLDER);
        TreeItem<ProjectNode> rootItem = new TreeItem<>(rootNode);
        rootItem.setExpanded(true);
        projectTreeView.setRoot(rootItem);
        
        // Enable editing
        projectTreeView.setEditable(true);

        // Enable drag and drop functionality
        TreeViewDragAndDrop.enableDragAndDrop(projectTreeView);

        // Setup selection listener
        projectTreeView.getSelectionModel().selectedItemProperty().addListener(
            (observable, oldValue, newValue) -> updateUIState()
        );
    }

    private void setupProjectTypeComboBox() {
        projectTypeComboBox.setItems(FXCollections.observableArrayList(
            "Java Maven", "Java Gradle", "Python", "Node.js", "React", "Vue.js", 
            "Angular", "Spring Boot", "Django", "Flask", "Custom"
        ));
        projectTypeComboBox.setValue("Custom");
    }

    private void setupDependenciesTable() {
        dependencyTypeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        dependencyNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        dependencyVersionColumn.setCellValueFactory(new PropertyValueFactory<>("version"));
        
        dependenciesTable.setItems(dependencies);
        
        // Enable row selection
        dependenciesTable.getSelectionModel().selectedItemProperty().addListener(
            (observable, oldValue, newValue) -> updateUIState()
        );
    }

    private void setupDependencyTypeComboBox() {
        dependencyTypeComboBox.setItems(FXCollections.observableArrayList(
            "Maven", "Gradle", "NPM", "Pip", "Yarn", "NuGet", "Composer", "Gem", "Go Modules"
        ));
        dependencyTypeComboBox.setValue("Maven");
    }
    
    private void setupDependencyPresetComboBox() {
        // Initial empty list - will be populated when project type changes
        dependencyPresetComboBox.setItems(FXCollections.observableArrayList());
        
        // Custom cell factory to show dependency description
        dependencyPresetComboBox.setCellFactory(listView -> new ListCell<DependencyPresetService.DependencyPreset>() {
            @Override
            protected void updateItem(DependencyPresetService.DependencyPreset preset, boolean empty) {
                super.updateItem(preset, empty);
                if (empty || preset == null) {
                    setText(null);
                    setTooltip(null);
                } else {
                    setText(preset.toString());
                    setTooltip(new Tooltip(preset.getDescription()));
                }
            }
        });
        
        // Update presets when project type changes
        projectTypeComboBox.getSelectionModel().selectedItemProperty().addListener(
            (observable, oldValue, newValue) -> updateDependencyPresets()
        );
    }

    private void setupEventHandlers() {
        // Project tree actions
        addFolderButton.setOnAction(e -> addFolder());
        addFileButton.setOnAction(e -> addFile());
        deleteButton.setOnAction(e -> deleteSelectedNode());
        renameButton.setOnAction(e -> renameSelectedNode());
        
        // Project info
        browseLocationButton.setOnAction(e -> browseProjectLocation());
        
        // Dependencies
        addDependencyButton.setOnAction(e -> addDependency());
        removeDependencyButton.setOnAction(e -> removeDependency());
        addPresetButton.setOnAction(e -> addPresetDependency());
        
        // Main actions
        createProjectButton.setOnAction(e -> createProject());
        clearAllButton.setOnAction(e -> clearAll());
        loadTemplateButton.setOnAction(e -> loadTemplate());
        saveTemplateButton.setOnAction(e -> saveTemplate());
        
        // Help and tutorial actions
        helpButton.setOnAction(e -> tutorialService.showQuickStartTutorial());
        quickStartMenuItem.setOnAction(e -> tutorialService.showQuickStartTutorial());
        dependencyHelpMenuItem.setOnAction(e -> tutorialService.showDependencyGuide());
        projectTypesHelpMenuItem.setOnAction(e -> tutorialService.showProjectTypesOverview());
        keyboardShortcutsMenuItem.setOnAction(e -> tutorialService.showKeyboardShortcuts());
        troubleshootingMenuItem.setOnAction(e -> tutorialService.showTroubleshooting());
        aboutMenuItem.setOnAction(e -> tutorialService.showAbout());
        
        // Tools menu actions
        environmentInfoMenuItem.setOnAction(e -> showEnvironmentInformation());
    }

    @FXML
    private void addFolder() {
        TreeItem<ProjectNode> selectedItem = projectTreeView.getSelectionModel().getSelectedItem();
        if (selectedItem == null) {
            selectedItem = projectTreeView.getRoot();
        }
        
        // Only allow adding to folders
        if (selectedItem.getValue().getType() != ProjectNode.NodeType.FOLDER) {
            selectedItem = selectedItem.getParent();
        }
        
        String folderName = showInputDialog("Add Folder", "Enter folder name:", "New Folder");
        if (folderName != null && !folderName.trim().isEmpty()) {
            ProjectNode folderNode = new ProjectNode(folderName.trim(), ProjectNode.NodeType.FOLDER);
            TreeItem<ProjectNode> folderItem = new TreeItem<>(folderNode);
            selectedItem.getChildren().add(folderItem);
            selectedItem.setExpanded(true);
            
            // Select the new item
            projectTreeView.getSelectionModel().select(folderItem);
            logger.info("Added folder: {}", folderName);
        }
    }

    @FXML
    private void addFile() {
        TreeItem<ProjectNode> selectedItem = projectTreeView.getSelectionModel().getSelectedItem();
        if (selectedItem == null) {
            selectedItem = projectTreeView.getRoot();
        }
        
        // Only allow adding to folders
        if (selectedItem.getValue().getType() != ProjectNode.NodeType.FOLDER) {
            selectedItem = selectedItem.getParent();
        }
        
        String fileName = showInputDialog("Add File", "Enter file name (with extension):", "example.txt");
        if (fileName != null && !fileName.trim().isEmpty()) {
            ProjectNode fileNode = new ProjectNode(fileName.trim(), ProjectNode.NodeType.FILE);
            TreeItem<ProjectNode> fileItem = new TreeItem<>(fileNode);
            selectedItem.getChildren().add(fileItem);
            selectedItem.setExpanded(true);
            
            // Select the new item
            projectTreeView.getSelectionModel().select(fileItem);
            logger.info("Added file: {}", fileName);
        }
    }

    @FXML
    private void deleteSelectedNode() {
        TreeItem<ProjectNode> selectedItem = projectTreeView.getSelectionModel().getSelectedItem();
        if (selectedItem == null || selectedItem == projectTreeView.getRoot()) {
            AlertUtils.showWarning("Cannot Delete", "Please select a valid item to delete (not the root).");
            return;
        }
        
        boolean confirmed = AlertUtils.showConfirmation(
            "Delete Item", 
            "Are you sure you want to delete '" + selectedItem.getValue().getName() + "' and all its contents?"
        );
        
        if (confirmed) {
            selectedItem.getParent().getChildren().remove(selectedItem);
            logger.info("Deleted node: {}", selectedItem.getValue().getName());
        }
    }

    @FXML
    private void renameSelectedNode() {
        TreeItem<ProjectNode> selectedItem = projectTreeView.getSelectionModel().getSelectedItem();
        if (selectedItem == null || selectedItem == projectTreeView.getRoot()) {
            AlertUtils.showWarning("Cannot Rename", "Please select a valid item to rename (not the root).");
            return;
        }
        
        String currentName = selectedItem.getValue().getName();
        String newName = showInputDialog("Rename Item", "Enter new name:", currentName);
        
        if (newName != null && !newName.trim().isEmpty() && !newName.equals(currentName)) {
            selectedItem.getValue().setName(newName.trim());
            projectTreeView.refresh();
            logger.info("Renamed '{}' to '{}'", currentName, newName);
        }
    }

    @FXML
    private void browseProjectLocation() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select Project Location");
        
        // Set initial directory
        String currentLocation = projectLocationField.getText();
        if (!currentLocation.isEmpty()) {
            File currentDir = new File(currentLocation);
            if (currentDir.exists() && currentDir.isDirectory()) {
                directoryChooser.setInitialDirectory(currentDir);
            }
        }
        
        Stage stage = (Stage) browseLocationButton.getScene().getWindow();
        File selectedDirectory = directoryChooser.showDialog(stage);
        
        if (selectedDirectory != null) {
            projectLocationField.setText(selectedDirectory.getAbsolutePath());
        }
    }

    @FXML
    private void addDependency() {
        String type = dependencyTypeComboBox.getValue();
        String name = dependencyNameField.getText().trim();
        String version = dependencyVersionField.getText().trim();
        
        if (name.isEmpty()) {
            AlertUtils.showWarning("Invalid Dependency", "Please enter a dependency name.");
            return;
        }
        
        // Validate compatibility with project type
        String projectType = projectTypeComboBox.getValue();
        if (!DependencyPresetService.isCompatibleWithProjectType(projectType, type)) {
            AlertUtils.showWarning("Incompatible Dependency", 
                "The dependency type '" + type + "' is not compatible with project type '" + projectType + "'.");
            return;
        }
        
        Dependency dependency = new Dependency(type, name, version.isEmpty() ? "latest" : version);
        
        // Validate dependency asynchronously using the resolver service
        dependencyResolverService.validateDependency(dependency)
            .thenAccept(isValid -> {
                Platform.runLater(() -> {
                    if (isValid) {
                        dependencies.add(dependency);
                        logger.info("Added validated dependency: {} {} {}", type, name, version);
                        
                        // Show repository URL for reference
                        String repoUrl = dependencyResolverService.getRepositoryUrl(type);
                        logger.info("Repository: {}", repoUrl);
                        
                        // Clear input fields
                        dependencyNameField.clear();
                        dependencyVersionField.clear();
                    } else {
                        AlertUtils.showWarning("Dependency Not Found", 
                            "The dependency '" + name + "' could not be found in the " + type + " repository.");
                    }
                });
            })
            .exceptionally(throwable -> {
                Platform.runLater(() -> {
                    logger.warn("Could not validate dependency, adding anyway: {}", throwable.getMessage());
                    dependencies.add(dependency);
                    
                    // Clear input fields
                    dependencyNameField.clear();
                    dependencyVersionField.clear();
                });
                return null;
            });
    }
    


    @FXML
    private void removeDependency() {
        Dependency selected = dependenciesTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            dependencies.remove(selected);
            logger.info("Removed dependency: {}", selected.getName());
        }
    }

    @FXML
    private void createProject() {
        if (!validateInput()) {
            return;
        }

        String projectType = projectTypeComboBox.getValue();
        String projectName = projectNameField.getText().trim();
        String projectLocation = projectLocationField.getText().trim();

        // For React projects, use the advanced React generator
        if ("React".equals(projectType)) {
            createReactProjectWithFullDependencies(projectName, projectLocation);
            return;
        }

        // Update project structure for other project types
        currentProject.setProjectName(projectName);
        currentProject.setProjectLocation(projectLocation);
        currentProject.setProjectType(projectType);
        currentProject.setRootNode(convertTreeToProjectNode(projectTreeView.getRoot()));
        currentProject.setDependencies(dependencies);

        // Show progress
        showProgress(true);
        updateStatus("Creating project structure...");

        // Create project in background thread
        Task<Void> createProjectTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                try {
                    // Create directory structure and files
                    updateMessage("Creating directories and files...");
                    updateProgress(0.2, 1.0);
                    projectCreationService.createProjectStructure(currentProject);
                    
                    // Generate dependency configuration
                    updateMessage("Generating dependency configuration...");
                    updateProgress(0.5, 1.0);
                    generateDependencyConfiguration(currentProject);
                    
                    // Install dependencies
                    updateMessage("Installing dependencies...");
                    updateProgress(0.7, 1.0);
                    projectCreationService.installDependencies(currentProject);
                    
                    // Generate file tree documentation
                    updateMessage("Generating documentation...");
                    updateProgress(0.9, 1.0);
                    fileTreeGenerator.generateFileTree(currentProject);
                    
                    updateProgress(1.0, 1.0);
                    updateMessage("Project created successfully!");
                    
                    return null;
                } catch (Exception e) {
                    logger.error("Failed to create project", e);
                    throw e;
                }
            }
        };
        
        createProjectTask.setOnSucceeded(e -> {
            Platform.runLater(() -> {
                showProgress(false);
                updateStatus("Project created successfully!");
                AlertUtils.showInformation("Success", 
                    "Project '" + currentProject.getProjectName() + "' has been created successfully!\n\n" +
                    "Location: " + currentProject.getProjectLocation());
            });
        });
        
        createProjectTask.setOnFailed(e -> {
            Platform.runLater(() -> {
                showProgress(false);
                updateStatus("Failed to create project.");
                Throwable exception = createProjectTask.getException();
                AlertUtils.showError("Error", "Failed to create project: " + exception.getMessage());
            });
        });
        
        // Bind progress
        progressBar.progressProperty().bind(createProjectTask.progressProperty());
        statusLabel.textProperty().bind(createProjectTask.messageProperty());
        
        // Run task
        Thread thread = new Thread(createProjectTask);
        thread.setDaemon(true);
        thread.start();
    }

    @FXML
    private void clearAll() {
        boolean confirmed = AlertUtils.showConfirmation(
            "Clear All", 
            "Are you sure you want to clear all project data? This action cannot be undone."
        );
        
        if (confirmed) {
            // Clear project info
            projectNameField.clear();
            projectLocationField.clear();
            projectTypeComboBox.setValue("Custom");
            
            // Clear tree
            projectTreeView.getRoot().getChildren().clear();
            
            // Clear dependencies
            dependencies.clear();
            
            updateStatus("All data cleared.");
            logger.info("All project data cleared");
        }
    }

    @FXML
    private void loadTemplate() {
        try {
            List<ProjectTemplate> templates = templateService.listTemplates();
            
            if (templates.isEmpty()) {
                AlertUtils.showInformation("No Templates", "No templates found. Create some templates first by saving your current project structure.");
                return;
            }
            
            // Create template selection dialog
            ChoiceDialog<ProjectTemplate> dialog = new ChoiceDialog<>(templates.get(0), templates);
            dialog.setTitle("Load Template");
            dialog.setHeaderText("Select a template to load");
            dialog.setContentText("Choose template:");
            
            // Customize the dialog
            dialog.getDialogPane().setPrefWidth(400);
            
            Optional<ProjectTemplate> result = dialog.showAndWait();
            if (result.isPresent()) {
                ProjectTemplate selectedTemplate = result.get();
                
                // Confirm loading
                boolean confirmed = AlertUtils.showConfirmation(
                    "Load Template", 
                    "Loading template '" + selectedTemplate.getTemplateName() + "' will replace your current project structure.\n\n" +
                    "Template Details:\n" +
                    "• Type: " + selectedTemplate.getProjectType() + "\n" +
                    "• Author: " + selectedTemplate.getAuthor() + "\n" +
                    "• Description: " + selectedTemplate.getTemplateDescription() + "\n\n" +
                    "Do you want to continue?"
                );
                
                if (confirmed) {
                    loadTemplateIntoProject(selectedTemplate);
                    AlertUtils.showSuccess("Template Loaded", 
                        "Template '" + selectedTemplate.getTemplateName() + "' has been loaded successfully!");
                }
            }
            
        } catch (Exception e) {
            logger.error("Failed to load template", e);
            AlertUtils.showError("Load Template Error", "Failed to load template: " + e.getMessage());
        }
    }

    @FXML
    private void saveTemplate() {
        try {
            // Validate current project has content
            if (projectTreeView.getRoot().getChildren().isEmpty()) {
                AlertUtils.showWarning("Empty Project", "Please create some folders or files before saving as a template.");
                return;
            }
            
            // Create template input dialog
            Dialog<ProjectTemplate> dialog = createSaveTemplateDialog();
            
            Optional<ProjectTemplate> result = dialog.showAndWait();
            if (result.isPresent()) {
                ProjectTemplate template = result.get();
                
                // Check if template already exists
                if (templateService.templateExists(template.getTemplateName())) {
                    boolean overwrite = AlertUtils.showConfirmation(
                        "Template Exists", 
                        "A template named '" + template.getTemplateName() + "' already exists.\n\n" +
                        "Do you want to overwrite it?"
                    );
                    
                    if (!overwrite) {
                        return;
                    }
                }
                
                // Create template from current project
                currentProject.setProjectName(projectNameField.getText().trim());
                currentProject.setProjectType(projectTypeComboBox.getValue());
                currentProject.setRootNode(convertTreeToProjectNode(projectTreeView.getRoot()));
                currentProject.setDependencies(dependencies);
                
                ProjectTemplate finalTemplate = templateService.createTemplateFromProject(
                    currentProject, 
                    template.getTemplateName(), 
                    template.getTemplateDescription()
                );
                
                // Set additional properties
                finalTemplate.setCategory(template.getCategory());
                
                // Save template
                templateService.saveTemplate(finalTemplate);
                
                AlertUtils.showSuccess("Template Saved", 
                    "Template '" + template.getTemplateName() + "' has been saved successfully!\n\n" +
                    "Location: " + templateService.getTemplatesPath());
                
                logger.info("Saved template: {}", template.getTemplateName());
            }
            
        } catch (Exception e) {
            logger.error("Failed to save template", e);
            AlertUtils.showError("Save Template Error", "Failed to save template: " + e.getMessage());
        }
    }

    private boolean validateInput() {
        String projectName = projectNameField.getText().trim();
        String projectLocation = projectLocationField.getText().trim();
        String projectType = projectTypeComboBox.getValue();
        
        if (projectName.isEmpty()) {
            AlertUtils.showWarning("Invalid Input", "Please enter a project name.");
            return false;
        }
        
        if (projectLocation.isEmpty()) {
            AlertUtils.showWarning("Invalid Input", "Please select a project location.");
            return false;
        }
        
        File locationDir = new File(projectLocation);
        if (!locationDir.exists() || !locationDir.isDirectory()) {
            AlertUtils.showWarning("Invalid Location", "The selected project location does not exist or is not a directory.");
            return false;
        }
        
        // Check if required development tools are available
        if (projectType != null && !projectType.equals("Custom")) {
            List<String> missingTools = EnvironmentManager.getMissingTools(projectType);
            if (!missingTools.isEmpty()) {
                boolean proceed = AlertUtils.showConfirmation(
                    "Missing Development Tools",
                    "The following required tools are not detected:\n" +
                    String.join("\n", missingTools) + "\n\n" +
                    "The project will be created, but you may need to install these tools to build and run it.\n\n" +
                    "Do you want to proceed anyway?"
                );
                if (!proceed) {
                    return false;
                }
            }
        }
        
        // Check for dependency conflicts
        if (!dependencies.isEmpty()) {
            List<DependencyPreset> presets = new java.util.ArrayList<>();
            for (Dependency dep : dependencies) {
                // Create a preset-like object for conflict checking
                DependencyPreset preset = new DependencyPreset(
                    dep.getName(), "", dep.getType(), dep.getName(), dep.getVersion(), false
                );
                presets.add(preset);
            }
            
            List<String> conflicts = DependencyPresetService.getDependencyConflicts(presets);
            if (!conflicts.isEmpty()) {
                boolean proceed = AlertUtils.showConfirmation(
                    "Dependency Conflicts Detected",
                    "The following dependency conflicts were detected:\n" +
                    String.join("\n", conflicts) + "\n\n" +
                    "Do you want to proceed anyway?"
                );
                if (!proceed) {
                    return false;
                }
            }
        }
        
        return true;
    }

    private ProjectNode convertTreeToProjectNode(TreeItem<ProjectNode> treeItem) {
        ProjectNode node = new ProjectNode(treeItem.getValue().getName(), treeItem.getValue().getType());
        
        for (TreeItem<ProjectNode> child : treeItem.getChildren()) {
            node.getChildren().add(convertTreeToProjectNode(child));
        }
        
        return node;
    }

    private void updateUIState() {
        TreeItem<ProjectNode> selectedItem = projectTreeView.getSelectionModel().getSelectedItem();
        boolean hasSelection = selectedItem != null && selectedItem != projectTreeView.getRoot();
        
        deleteButton.setDisable(!hasSelection);
        renameButton.setDisable(!hasSelection);
        
        Dependency selectedDependency = dependenciesTable.getSelectionModel().getSelectedItem();
        removeDependencyButton.setDisable(selectedDependency == null);
    }

    private void showProgress(boolean show) {
        progressContainer.setVisible(show);
        createProjectButton.setDisable(show);
    }

    private void updateStatus(String message) {
        statusLabel.setText(message);
        logger.info("Status: {}", message);
    }

    private String showInputDialog(String title, String headerText, String defaultValue) {
        TextInputDialog dialog = new TextInputDialog(defaultValue);
        dialog.setTitle(title);
        dialog.setHeaderText(headerText);
        dialog.setContentText("Name:");
        
        Optional<String> result = dialog.showAndWait();
        return result.orElse(null);
    }
    
    /**
     * Loads a template into the current project structure
     */
    private void loadTemplateIntoProject(ProjectTemplate template) {
        // Clear current project
        projectNameField.clear();
        projectTypeComboBox.setValue(template.getProjectType());
        projectTreeView.getRoot().getChildren().clear();
        dependencies.clear();
        
        // Load template structure
        loadProjectNodeIntoTree(template.getTemplateStructure(), projectTreeView.getRoot());
        
        // Load template dependencies
        if (template.getTemplateDependencies() != null) {
            dependencies.addAll(template.getTemplateDependencies());
        }
        
        // Expand tree
        expandTreeView(projectTreeView.getRoot());
        
        updateStatus("Template '" + template.getTemplateName() + "' loaded successfully.");
        logger.info("Loaded template '{}' into project", template.getTemplateName());
    }
    
    /**
     * Creates the save template dialog
     */
    private Dialog<ProjectTemplate> createSaveTemplateDialog() {
        Dialog<ProjectTemplate> dialog = new Dialog<>();
        dialog.setTitle("Save Template");
        dialog.setHeaderText("Create a new project template");
        
        // Set the button types
        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);
        
        // Create the form
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        
        TextField templateName = new TextField();
        templateName.setPromptText("Template name");
        templateName.setPrefWidth(300);
        
        TextArea templateDescription = new TextArea();
        templateDescription.setPromptText("Template description");
        templateDescription.setPrefRowCount(3);
        templateDescription.setPrefWidth(300);
        templateDescription.setWrapText(true);
        
        ComboBox<String> categoryCombo = new ComboBox<>();
        categoryCombo.getItems().addAll("Java", "Python", "JavaScript", "Frontend", ".NET", "Mobile", "Custom");
        categoryCombo.setValue("Custom");
        categoryCombo.setPrefWidth(300);
        
        grid.add(new Label("Template Name:"), 0, 0);
        grid.add(templateName, 1, 0);
        grid.add(new Label("Description:"), 0, 1);
        grid.add(templateDescription, 1, 1);
        grid.add(new Label("Category:"), 0, 2);
        grid.add(categoryCombo, 1, 2);
        
        dialog.getDialogPane().setContent(grid);
        
        // Enable/Disable save button depending on whether a name was entered
        Button saveButton = (Button) dialog.getDialogPane().lookupButton(saveButtonType);
        saveButton.setDisable(true);
        
        // Do some validation
        templateName.textProperty().addListener((observable, oldValue, newValue) -> {
            saveButton.setDisable(newValue.trim().isEmpty());
        });
        
        // Request focus on the template name field by default
        Platform.runLater(() -> templateName.requestFocus());
        
        // Convert the result to a ProjectTemplate when the save button is clicked
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                ProjectTemplate template = new ProjectTemplate();
                template.setTemplateName(templateName.getText().trim());
                template.setTemplateDescription(templateDescription.getText().trim());
                template.setCategory(categoryCombo.getValue());
                return template;
            }
            return null;
        });
        
        return dialog;
    }
    
    /**
     * Loads a ProjectNode structure into the tree view
     */
    private void loadProjectNodeIntoTree(ProjectNode sourceNode, TreeItem<ProjectNode> targetParent) {
        for (ProjectNode child : sourceNode.getChildren()) {
            ProjectNode newNode = new ProjectNode(child.getName(), child.getType(), child.getContent());
            TreeItem<ProjectNode> newTreeItem = new TreeItem<>(newNode);
            targetParent.getChildren().add(newTreeItem);
            
            // Recursively load children
            if (child.hasChildren()) {
                loadProjectNodeIntoTree(child, newTreeItem);
            }
        }
    }
    
    /**
     * Expands all nodes in the tree view
     */
    private void expandTreeView(TreeItem<ProjectNode> item) {
        if (item != null && !item.isLeaf()) {
            item.setExpanded(true);
            for (TreeItem<ProjectNode> child : item.getChildren()) {
                expandTreeView(child);
            }
        }
    }
    
    /**
     * Updates dependency presets based on current project type
     */
    private void updateDependencyPresets() {
        String selectedType = projectTypeComboBox.getValue();
        logger.info("Updating dependency presets for project type: {}", selectedType);
        
        if (selectedType != null) {
            List<DependencyPreset> presets = DependencyPresetService.getPresetsForProjectType(selectedType);
            logger.info("Found {} presets for project type: {}", presets.size(), selectedType);
            
            // Log repository information for the project type
            String defaultDepType = getDefaultDependencyType(selectedType);
            String repositoryUrl = dependencyResolverService.getRepositoryUrl(defaultDepType);
            logger.info("Repository URL for {} ({}): {}", selectedType, defaultDepType, repositoryUrl);
            
            ObservableList<DependencyPreset> presetList = FXCollections.observableArrayList();
            
            // Add a null preset as the first item to represent "Select a preset..."
            presetList.add(null);
            presetList.addAll(presets);
            
            dependencyPresetComboBox.setItems(presetList);
            dependencyPresetComboBox.setCellFactory(param -> new ListCell<DependencyPreset>() {
                @Override
                protected void updateItem(DependencyPreset item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText("Select a preset...");
                    } else {
                        setText(item.getName());
                    }
                }
            });
            dependencyPresetComboBox.setButtonCell(new ListCell<DependencyPreset>() {
                @Override
                protected void updateItem(DependencyPreset item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText("Select a preset...");
                    } else {
                        setText(item.getName());
                    }
                }
            });
            dependencyPresetComboBox.setValue(null);
            
            logger.info("Successfully updated dependency preset dropdown with {} items", presetList.size());
        } else {
            logger.warn("Selected project type is null, clearing presets");
            dependencyPresetComboBox.setItems(FXCollections.observableArrayList());
        }
    }
    
    /**
     * Adds a preset dependency to the dependency list
     */
    @FXML
    private void addPresetDependency() {
        DependencyPreset selectedPreset = dependencyPresetComboBox.getValue();
        
        if (selectedPreset == null) {
            AlertUtils.showWarning("No Preset Selected", "Please select a preset dependency.");
            return;
        }
        
        // Check if dependency already exists
        boolean exists = dependencies.stream()
            .anyMatch(dep -> dep.getName().equals(selectedPreset.getArtifact()));
            
        if (!exists) {
            Dependency dependency = new Dependency(
                selectedPreset.getType(),
                selectedPreset.getArtifact(),
                selectedPreset.getVersion()
            );
            
            dependencies.add(dependency);
            logger.info("Added preset dependency: {}", dependency.getName());
            
            // Reset preset selection
            dependencyPresetComboBox.setValue(null);
        } else {
            AlertUtils.showWarning("Duplicate Dependency", 
                "This dependency is already in the list.");
        }
    }
    
    /**
     * Get the default dependency type for a project type
     */
    private String getDefaultDependencyType(String projectType) {
        switch (projectType.toLowerCase()) {
            case "java maven":
            case "spring boot":
                return "Maven";
            case "java gradle":
                return "Gradle";
            case "node.js":
            case "react":
            case "vue.js":
            case "angular":
                return "NPM";
            case "python":
            case "django":
            case "flask":
                return "Pip";
            default:
                return "Maven";
        }
    }
    
    /**
     * Generate dependency configuration using the resolver service
     */
    private void generateDependencyConfiguration(ProjectStructure projectStructure) {
        if (projectStructure.getDependencies() == null || projectStructure.getDependencies().isEmpty()) {
            logger.info("No dependencies to configure");
            return;
        }
        
        logger.info("Generating dependency configuration for {} dependencies", projectStructure.getDependencies().size());
        
        // Generate build file content
        String buildFileContent = dependencyResolverService.generateBuildFileEntry(
            projectStructure.getDependencies(), 
            projectStructure.getProjectType()
        );
        
        if (!buildFileContent.isEmpty()) {
            logger.info("Generated build file content:\n{}", buildFileContent);
            // In a real implementation, you might write this to a build file
        }
        
        // Generate individual install commands for logging/documentation
        for (Dependency dependency : projectStructure.getDependencies()) {
            String installCommand = dependencyResolverService.generateInstallCommand(
                dependency, 
                projectStructure.getProjectType()
            );
            logger.info("Install command for {}: {}", dependency.getName(), installCommand);
        }
    }
    
    /**
     * Show environment information dialog
     */
    private void showEnvironmentInformation() {
        // Create dialog
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Environment Information");
        dialog.setHeaderText("Detected Development Environments");
        
        // Create content
        VBox content = new VBox(10);
        content.setPadding(new Insets(20));
        
        // Get all environments
        Map<String, EnvironmentInfo> environments = EnvironmentManager.getAllEnvironments();
        
        // Add environment information
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(10);
        grid.setPadding(new Insets(10));
        
        int row = 0;
        for (Map.Entry<String, EnvironmentInfo> entry : environments.entrySet()) {
            EnvironmentInfo info = entry.getValue();
            
            Label nameLabel = new Label(info.getName() + ":");
            nameLabel.setStyle("-fx-font-weight: bold;");
            grid.add(nameLabel, 0, row);
            
            String status = info.isAvailable() ? 
                "✓ " + info.getVersion() : 
                "✗ Not Available";
            Label statusLabel = new Label(status);
            statusLabel.setStyle(info.isAvailable() ? 
                "-fx-text-fill: green;" : 
                "-fx-text-fill: red;");
            grid.add(statusLabel, 1, row);
            
            row++;
        }
        
        content.getChildren().add(grid);
        
        // Add current project type requirements if selected
        String projectType = projectTypeComboBox.getValue();
        if (projectType != null && !projectType.equals("Custom")) {
            Separator separator = new Separator();
            content.getChildren().add(separator);
            
            Label reqLabel = new Label("Required for " + projectType + ":");
            reqLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
            content.getChildren().add(reqLabel);
            
            List<String> required = EnvironmentManager.getRequiredToolsForProjectType(projectType);
            List<String> missing = EnvironmentManager.getMissingTools(projectType);
            
            VBox reqBox = new VBox(5);
            for (String tool : required) {
                boolean isMissing = missing.contains(tool);
                Label toolLabel = new Label((isMissing ? "✗ " : "✓ ") + tool);
                toolLabel.setStyle(isMissing ? 
                    "-fx-text-fill: red;" : 
                    "-fx-text-fill: green;");
                reqBox.getChildren().add(toolLabel);
            }
            content.getChildren().add(reqBox);
            
            if (!missing.isEmpty()) {
                Label warningLabel = new Label("\nWarning: Some required tools are missing!");
                warningLabel.setStyle("-fx-text-fill: orange; -fx-font-weight: bold;");
                content.getChildren().add(warningLabel);
            }
        }
        
        // Add refresh button
        Button refreshButton = new Button("Refresh");
        refreshButton.setOnAction(e -> {
            EnvironmentManager.clearCache();
            dialog.close();
            showEnvironmentInformation();
        });
        content.getChildren().add(refreshButton);
        
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        
        dialog.showAndWait();
    }

    /**
     * Create a React project with full dependency resolution
     */
    private void createReactProjectWithFullDependencies(String projectName, String projectLocation) {
        showProgress(true);
        updateStatus("Creating advanced React project...");

        Task<Void> createReactTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                try {
                    // Generate React project structure with all dependencies
                    updateMessage("Generating React project structure...");
                    updateProgress(0.1, 1.0);

                    ProjectNode reactProject = reactProjectGenerator.generateReactProject(
                        projectName,
                        ReactProjectGenerator.ReactProjectType.FULL_STACK
                    );

                    // Create project structure object
                    ProjectStructure structure = new ProjectStructure();
                    structure.setProjectName(projectName);
                    structure.setProjectLocation(projectLocation);
                    structure.setProjectType("React");
                    structure.setRootNode(reactProject);

                    // Create physical directories and files
                    updateMessage("Creating directories and files...");
                    updateProgress(0.3, 1.0);
                    projectCreationService.createProjectStructure(structure);

                    // Resolve all NPM dependencies with transitive dependencies
                    updateMessage("Resolving all dependencies with compatibility checks...");
                    updateProgress(0.5, 1.0);

                    // Get package.json content and extract dependencies
                    ProjectNode packageJson = reactProject.findChild("package.json");
                    if (packageJson != null) {
                        Map<String, String> npmDeps = parsePackageJsonDependencies(packageJson.getContent());

                        // Resolve all dependencies including transitive ones
                        var resolvedDeps = dependencyResolverService.resolveNpmDependencies(npmDeps).get();

                        updateMessage(String.format("Resolved %d total dependencies (including transitive)...",
                                                  resolvedDeps.size()));
                        updateProgress(0.7, 1.0);

                        // Log dependency tree
                        String depTree = dependencyResolverService.getDependencyTree(resolvedDeps);
                        logger.info("Dependency tree:\n{}", depTree);
                    }

                    // Install dependencies
                    updateMessage("Installing dependencies (this may take several minutes)...");
                    updateProgress(0.8, 1.0);

                    // Run npm install
                    File projectDir = new File(projectLocation, projectName);
                    Process npmInstall = new ProcessBuilder("npm", "install")
                            .directory(projectDir)
                            .start();

                    boolean finished = npmInstall.waitFor(5, java.util.concurrent.TimeUnit.MINUTES);
                    if (!finished) {
                        npmInstall.destroyForcibly();
                        throw new RuntimeException("NPM install took too long");
                    }

                    if (npmInstall.exitValue() != 0) {
                        logger.warn("NPM install completed with warnings/errors");
                    }

                    updateMessage("Project created successfully!");
                    updateProgress(1.0, 1.0);

                } catch (Exception e) {
                    logger.error("Failed to create React project", e);
                    throw new RuntimeException("Failed to create React project: " + e.getMessage(), e);
                }
                return null;
            }
        };

        createReactTask.setOnSucceeded(e -> {
            Platform.runLater(() -> {
                showProgress(false);
                updateStatus("React project created successfully!");

                String message = String.format(
                    "React project '%s' has been created successfully!\n\n" +
                    "Location: %s\n\n" +
                    "Features included:\n" +
                    "• React 18 with Hooks\n" +
                    "• React Router for navigation\n" +
                    "• Material-UI components\n" +
                    "• Redux Toolkit for state management\n" +
                    "• Axios for API calls\n" +
                    "• Form handling with React Hook Form\n" +
                    "• Testing with Jest & React Testing Library\n" +
                    "• ESLint & Prettier configured\n" +
                    "• Tailwind CSS for styling\n" +
                    "• All dependencies resolved and compatible\n\n" +
                    "To start development:\n" +
                    "1. cd %s/%s\n" +
                    "2. npm run dev",
                    projectName, projectLocation, projectLocation, projectName
                );

                AlertUtils.showInformation("Success", message);
            });
        });

        createReactTask.setOnFailed(e -> {
            Platform.runLater(() -> {
                showProgress(false);
                updateStatus("Failed to create React project.");
                Throwable exception = createReactTask.getException();
                AlertUtils.showError("Error", "Failed to create React project: " + exception.getMessage());
            });
        });

        // Bind progress
        progressBar.progressProperty().bind(createReactTask.progressProperty());
        statusLabel.textProperty().bind(createReactTask.messageProperty());

        // Run task
        Thread thread = new Thread(createReactTask);
        thread.setDaemon(true);
        thread.start();
    }

    /**
     * Parse dependencies from package.json content
     */
    private Map<String, String> parsePackageJsonDependencies(String packageJsonContent) {
        Map<String, String> dependencies = new LinkedHashMap<>();

        try {
            // Simple JSON parsing for dependencies
            String[] lines = packageJsonContent.split("\n");
            boolean inDependencies = false;

            for (String line : lines) {
                line = line.trim();

                if (line.contains("\"dependencies\"")) {
                    inDependencies = true;
                } else if (line.contains("\"devDependencies\"") || (inDependencies && line.equals("},"))) {
                    inDependencies = false;
                } else if (inDependencies && line.contains(":")) {
                    // Parse dependency line
                    String[] parts = line.split(":");
                    if (parts.length == 2) {
                        String name = parts[0].trim().replace("\"", "").replace(",", "");
                        String version = parts[1].trim().replace("\"", "").replace(",", "");
                        dependencies.put(name, version);
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Error parsing package.json dependencies", e);
        }

        return dependencies;
    }
}
