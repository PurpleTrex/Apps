package com.purpletrex.aether.quest;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a quest in the game
 */
public class Quest {
    private int id;
    private String title;
    private String description;
    private QuestType type;
    private QuestStatus status;
    private List<QuestObjective> objectives;
    private List<QuestReward> rewards;
    private int requiredLevel;
    private List<Integer> prerequisiteQuests;

    public enum QuestType {
        MAIN,           // Main story quest
        SIDE,           // Optional side quest
        COLLECTION,     // Capture specific Ethereals
        CHALLENGE,      // Battle challenges
        REPEATABLE      // Daily/weekly quests
    }

    public enum QuestStatus {
        NOT_STARTED,
        AVAILABLE,
        IN_PROGRESS,
        COMPLETED,
        CLAIMED
    }

    public Quest(int id, String title, String description, QuestType type, int requiredLevel) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.type = type;
        this.status = QuestStatus.NOT_STARTED;
        this.objectives = new ArrayList<>();
        this.rewards = new ArrayList<>();
        this.requiredLevel = requiredLevel;
        this.prerequisiteQuests = new ArrayList<>();
    }

    /**
     * Add an objective to the quest
     */
    public void addObjective(QuestObjective objective) {
        objectives.add(objective);
    }

    /**
     * Add a reward
     */
    public void addReward(QuestReward reward) {
        rewards.add(reward);
    }

    /**
     * Add prerequisite quest
     */
    public void addPrerequisite(int questId) {
        prerequisiteQuests.add(questId);
    }

    /**
     * Start the quest
     */
    public void start() {
        if (status == QuestStatus.AVAILABLE) {
            status = QuestStatus.IN_PROGRESS;
        }
    }

    /**
     * Update objective progress
     */
    public void updateProgress(String objectiveId, int progress) {
        for (QuestObjective obj : objectives) {
            if (obj.getId().equals(objectiveId)) {
                obj.setCurrentProgress(obj.getCurrentProgress() + progress);
                break;
            }
        }
        checkCompletion();
    }

    /**
     * Check if all objectives are complete
     */
    private void checkCompletion() {
        boolean allComplete = true;
        for (QuestObjective obj : objectives) {
            if (!obj.isComplete()) {
                allComplete = false;
                break;
            }
        }
        if (allComplete && status == QuestStatus.IN_PROGRESS) {
            status = QuestStatus.COMPLETED;
        }
    }

    /**
     * Claim rewards
     */
    public List<QuestReward> claimRewards() {
        if (status == QuestStatus.COMPLETED) {
            status = QuestStatus.CLAIMED;
            return new ArrayList<>(rewards);
        }
        return new ArrayList<>();
    }

    /**
     * Get completion percentage
     */
    public float getCompletionPercentage() {
        if (objectives.isEmpty()) return 0;
        float total = 0;
        for (QuestObjective obj : objectives) {
            total += obj.getCompletionPercentage();
        }
        return total / objectives.size();
    }

    // Getters
    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public QuestType getType() { return type; }
    public QuestStatus getStatus() { return status; }
    public List<QuestObjective> getObjectives() { return objectives; }
    public List<QuestReward> getRewards() { return rewards; }
    public int getRequiredLevel() { return requiredLevel; }
    public List<Integer> getPrerequisiteQuests() { return prerequisiteQuests; }

    // Setters
    public void setStatus(QuestStatus status) { this.status = status; }
}
