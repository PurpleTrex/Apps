package com.purpletrex.aether.quest;

/**
 * Represents a quest reward
 */
public class QuestReward {
    private RewardType type;
    private int value;
    private String description;

    public enum RewardType {
        MONEY,          // Aether Shards
        ITEM,           // Item ID
        ETHEREAL,       // Ethereal species ID
        EXPERIENCE,     // EXP for team
        BADGE,          // Champion badge
        GAUNTLET_UPGRADE // Upgrade gauntlet
    }

    public QuestReward(RewardType type, int value, String description) {
        this.type = type;
        this.value = value;
        this.description = description;
    }

    // Getters
    public RewardType getType() { return type; }
    public int getValue() { return value; }
    public String getDescription() { return description; }
}
