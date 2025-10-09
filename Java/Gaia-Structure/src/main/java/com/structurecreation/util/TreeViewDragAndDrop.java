package com.structurecreation.util;

import com.structurecreation.model.ProjectNode;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.*;
import javafx.util.Callback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Utility class for enabling drag and drop functionality in TreeView
 * Allows users to reorganize project structure by dragging nodes
 */
public class TreeViewDragAndDrop {

    private static final Logger logger = LoggerFactory.getLogger(TreeViewDragAndDrop.class);
    private static final DataFormat SERIALIZED_MIME_TYPE = new DataFormat("application/x-java-serialized-object");

    private TreeItem<ProjectNode> draggedItem;
    private TreeCell<ProjectNode> dropTarget;

    /**
     * Enable drag and drop for the given TreeView
     */
    public static void enableDragAndDrop(TreeView<ProjectNode> treeView) {
        TreeViewDragAndDrop handler = new TreeViewDragAndDrop();
        treeView.setCellFactory(handler.createCellFactory());
    }

    /**
     * Create a cell factory with drag and drop support
     */
    private Callback<TreeView<ProjectNode>, TreeCell<ProjectNode>> createCellFactory() {
        return (TreeView<ProjectNode> tree) -> {
            TreeCell<ProjectNode> cell = new TreeCell<ProjectNode>() {
                @Override
                protected void updateItem(ProjectNode item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        setGraphic(null);
                    } else {
                        setText(item.toString());

                        // Add style based on node type
                        if (item.isDirectory()) {
                            setStyle("-fx-font-weight: bold;");
                        } else {
                            setStyle("-fx-font-weight: normal;");
                        }
                    }
                }
            };

            // Setup drag detection
            cell.setOnDragDetected((MouseEvent event) -> onDragDetected(event, cell, tree));

            // Setup drag over
            cell.setOnDragOver((DragEvent event) -> onDragOver(event, cell));

            // Setup drag entered
            cell.setOnDragEntered((DragEvent event) -> onDragEntered(event, cell));

            // Setup drag exited
            cell.setOnDragExited((DragEvent event) -> onDragExited(event, cell));

            // Setup drag dropped
            cell.setOnDragDropped((DragEvent event) -> onDragDropped(event, cell, tree));

            // Setup drag done
            cell.setOnDragDone((DragEvent event) -> onDragDone(event));

            return cell;
        };
    }

    /**
     * Handle drag detection
     */
    private void onDragDetected(MouseEvent event, TreeCell<ProjectNode> cell, TreeView<ProjectNode> tree) {
        draggedItem = cell.getTreeItem();

        // Don't allow dragging the root
        if (draggedItem == null || draggedItem.getParent() == null) {
            return;
        }

        Dragboard dragboard = cell.startDragAndDrop(TransferMode.MOVE);
        ClipboardContent content = new ClipboardContent();
        content.put(SERIALIZED_MIME_TYPE, draggedItem.getValue().getName());
        content.putString(draggedItem.getValue().getName());
        dragboard.setContent(content);
        dragboard.setDragView(cell.snapshot(null, null));

        logger.debug("Started dragging: {}", draggedItem.getValue().getName());
        event.consume();
    }

    /**
     * Handle drag over
     */
    private void onDragOver(DragEvent event, TreeCell<ProjectNode> cell) {
        if (event.getDragboard().hasContent(SERIALIZED_MIME_TYPE)) {
            TreeItem<ProjectNode> targetItem = cell.getTreeItem();

            // Check if the drop is valid
            if (isValidDropTarget(targetItem)) {
                event.acceptTransferModes(TransferMode.MOVE);

                // Visual feedback - highlight valid drop targets
                if (targetItem != null && targetItem.getValue().isDirectory()) {
                    if (!cell.getStyleClass().contains("drop-target")) {
                        cell.getStyleClass().add("drop-target");
                    }
                }
            }
        }
        event.consume();
    }

    /**
     * Handle drag entered
     */
    private void onDragEntered(DragEvent event, TreeCell<ProjectNode> cell) {
        if (event.getDragboard().hasContent(SERIALIZED_MIME_TYPE)) {
            TreeItem<ProjectNode> targetItem = cell.getTreeItem();
            if (isValidDropTarget(targetItem)) {
                dropTarget = cell;
                cell.setStyle("-fx-background-color: #e0e0e0; -fx-border-color: #2196F3; -fx-border-width: 2px;");
            }
        }
        event.consume();
    }

    /**
     * Handle drag exited
     */
    private void onDragExited(DragEvent event, TreeCell<ProjectNode> cell) {
        if (cell == dropTarget) {
            cell.setStyle("");
            cell.getStyleClass().remove("drop-target");
            dropTarget = null;
        }
        event.consume();
    }

    /**
     * Handle drag dropped
     */
    private void onDragDropped(DragEvent event, TreeCell<ProjectNode> cell, TreeView<ProjectNode> tree) {
        Dragboard dragboard = event.getDragboard();
        boolean success = false;

        if (dragboard.hasContent(SERIALIZED_MIME_TYPE)) {
            TreeItem<ProjectNode> targetItem = cell.getTreeItem();

            if (isValidDropTarget(targetItem)) {
                // Remove from old location
                TreeItem<ProjectNode> parent = draggedItem.getParent();
                parent.getChildren().remove(draggedItem);

                // Add to new location
                if (targetItem.getValue().isDirectory()) {
                    // Drop into folder
                    targetItem.getChildren().add(draggedItem);
                    targetItem.setExpanded(true);
                } else {
                    // Drop next to file
                    TreeItem<ProjectNode> targetParent = targetItem.getParent();
                    int index = targetParent.getChildren().indexOf(targetItem);
                    targetParent.getChildren().add(index + 1, draggedItem);
                }

                // Update the model
                updateModel(draggedItem, targetItem);

                // Select the moved item
                tree.getSelectionModel().select(draggedItem);

                logger.info("Moved {} to new location", draggedItem.getValue().getName());
                success = true;
            }
        }

        event.setDropCompleted(success);
        event.consume();
    }

    /**
     * Handle drag done
     */
    private void onDragDone(DragEvent event) {
        draggedItem = null;
        if (dropTarget != null) {
            dropTarget.setStyle("");
            dropTarget.getStyleClass().remove("drop-target");
            dropTarget = null;
        }
        event.consume();
    }

    /**
     * Check if the target is a valid drop location
     */
    private boolean isValidDropTarget(TreeItem<ProjectNode> targetItem) {
        // Can't drop on null
        if (targetItem == null || draggedItem == null) {
            return false;
        }

        // Can't drop on itself
        if (targetItem == draggedItem) {
            return false;
        }

        // Can't drop on its own descendant
        if (isDescendant(targetItem, draggedItem)) {
            return false;
        }

        // Can only drop on folders or next to files
        return true;
    }

    /**
     * Check if one item is a descendant of another
     */
    private boolean isDescendant(TreeItem<ProjectNode> potential, TreeItem<ProjectNode> ancestor) {
        TreeItem<ProjectNode> parent = potential.getParent();
        while (parent != null) {
            if (parent == ancestor) {
                return true;
            }
            parent = parent.getParent();
        }
        return false;
    }

    /**
     * Update the underlying model after drag and drop
     */
    private void updateModel(TreeItem<ProjectNode> movedItem, TreeItem<ProjectNode> targetItem) {
        ProjectNode movedNode = movedItem.getValue();
        ProjectNode targetNode = targetItem.getValue();

        // Remove from old parent in model
        TreeItem<ProjectNode> oldParent = movedItem.getParent();
        if (oldParent != null) {
            ProjectNode oldParentNode = oldParent.getValue();
            oldParentNode.getChildren().remove(movedNode);
        }

        // Add to new parent in model
        if (targetNode.isDirectory()) {
            targetNode.getChildren().add(movedNode);
        } else {
            TreeItem<ProjectNode> newParent = targetItem.getParent();
            if (newParent != null) {
                ProjectNode newParentNode = newParent.getValue();
                int index = newParentNode.getChildren().indexOf(targetNode);
                newParentNode.getChildren().add(index + 1, movedNode);
            }
        }
    }

    /**
     * Get all items in the tree as a flat list
     */
    public static List<TreeItem<ProjectNode>> getAllItems(TreeItem<ProjectNode> root) {
        List<TreeItem<ProjectNode>> items = new ArrayList<>();
        addItemsRecursively(root, items);
        return items;
    }

    private static void addItemsRecursively(TreeItem<ProjectNode> item, List<TreeItem<ProjectNode>> list) {
        list.add(item);
        for (TreeItem<ProjectNode> child : item.getChildren()) {
            addItemsRecursively(child, list);
        }
    }
}