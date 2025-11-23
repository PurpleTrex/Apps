package com.purpletrex.aether.quest;

/**
 * Represents a quest objective
 */
public class QuestObjective {
    private String id;
    private String description;
    private ObjectiveType type;
    private int targetProgress;
    private int currentProgress;
    private String targetData; // Species ID, item ID, etc.

    public enum ObjectiveType {
        CAPTURE_SPECIES,    // Capture specific Ethereal
        CAPTURE_TYPE,       // Capture X of element type
        DEFEAT_TRAINER,     // Defeat specific trainer
        DEFEAT_COUNT,       // Defeat X Ethereals
        COLLECT_ITEM,       // Collect specific item
        REACH_LOCATION,     // Go to specific place
        TALK_TO_NPC,        // Talk to someone
        LEVEL_UP,           // Level up Ethereal
        EVOLVE,             // Evolve an Ethereal
        WIN_BATTLES        // Win X battles
    }

    public QuestObjective(String id, String description, ObjectiveType type, int targetProgress, String targetData) {
        this.id = id;
        this.description = description;
        this.type = type;
        this.targetProgress = targetProgress;
        this.currentProgress = 0;
        this.targetData = targetData;
    }

    /**
     * Check if objective is complete
     */
    public boolean isComplete() {
        return currentProgress >= targetProgress;
    }

    /**
     * Get completion percentage
     */
    public float getCompletionPercentage() {
        return (float) currentProgress / targetProgress;
    }

    // Getters and setters
    public String getId() { return id; }
    public String getDescription() { return description; }
    public ObjectiveType getType() { return type; }
    public int getTargetProgress() { return targetProgress; }
    public int getCurrentProgress() { return currentProgress; }
    public String getTargetData() { return targetData; }
    public void setCurrentProgress(int progress) { 
        this.currentProgress = Math.min(progress, targetProgress); 
    }
}
