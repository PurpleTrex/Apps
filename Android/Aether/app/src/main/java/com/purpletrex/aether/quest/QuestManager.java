package com.purpletrex.aether.quest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manages all quests in the game
 */
public class QuestManager {
    private Map<Integer, Quest> allQuests;
    private List<Integer> activeQuests;
    private List<Integer> completedQuests;
    private static final int MAX_ACTIVE_QUESTS = 10;

    public QuestManager() {
        this.allQuests = new HashMap<>();
        this.activeQuests = new ArrayList<>();
        this.completedQuests = new ArrayList<>();
        initializeQuests();
    }

    /**
     * Initialize all quests in the game
     */
    private void initializeQuests() {
        // MAIN QUEST LINE
        Quest q1 = new Quest(1, "The First Champion", "Defeat Champion Sylva in Verdant Hollow", Quest.QuestType.MAIN, 1);
        q1.addObjective(new QuestObjective("defeat_sylva", "Defeat Champion Sylva", QuestObjective.ObjectiveType.DEFEAT_TRAINER, 1, "sylva"));
        q1.addReward(new QuestReward(QuestReward.RewardType.BADGE, 58, "Forest Badge"));
        q1.addReward(new QuestReward(QuestReward.RewardType.MONEY, 1000, "1000 Aether Shards"));
        q1.setStatus(Quest.QuestStatus.AVAILABLE);
        allQuests.put(1, q1);

        Quest q2 = new Quest(2, "The Flame Trial", "Defeat Champion Ignis in Crimson Wastes", Quest.QuestType.MAIN, 15);
        q2.addObjective(new QuestObjective("defeat_ignis", "Defeat Champion Ignis", QuestObjective.ObjectiveType.DEFEAT_TRAINER, 1, "ignis"));
        q2.addReward(new QuestReward(QuestReward.RewardType.BADGE, 59, "Flame Badge"));
        q2.addReward(new QuestReward(QuestReward.RewardType.MONEY, 2000, "2000 Aether Shards"));
        q2.addPrerequisite(1);
        allQuests.put(2, q2);

        Quest q3 = new Quest(3, "The Aqua Challenge", "Defeat Champion Marina in Azure Depths", Quest.QuestType.MAIN, 25);
        q3.addObjective(new QuestObjective("defeat_marina", "Defeat Champion Marina", QuestObjective.ObjectiveType.DEFEAT_TRAINER, 1, "marina"));
        q3.addReward(new QuestReward(QuestReward.RewardType.BADGE, 60, "Aqua Badge"));
        q3.addReward(new QuestReward(QuestReward.RewardType.MONEY, 3000, "3000 Aether Shards"));
        q3.addReward(new QuestReward(QuestReward.RewardType.GAUNTLET_UPGRADE, 1, "Gauntlet Upgrade"));
        q3.addPrerequisite(2);
        allQuests.put(3, q3);

        Quest q4 = new Quest(4, "The Storm Peak", "Defeat Champion Tempus in Tempest Peaks", Quest.QuestType.MAIN, 35);
        q4.addObjective(new QuestObjective("defeat_tempus", "Defeat Champion Tempus", QuestObjective.ObjectiveType.DEFEAT_TRAINER, 1, "tempus"));
        q4.addReward(new QuestReward(QuestReward.RewardType.BADGE, 61, "Storm Badge"));
        q4.addReward(new QuestReward(QuestReward.RewardType.MONEY, 4000, "4000 Aether Shards"));
        q4.addPrerequisite(3);
        allQuests.put(4, q4);

        Quest q5 = new Quest(5, "The Void Awaits", "Defeat Champion Nox in Obsidian Core", Quest.QuestType.MAIN, 45);
        q5.addObjective(new QuestObjective("defeat_nox", "Defeat Champion Nox", QuestObjective.ObjectiveType.DEFEAT_TRAINER, 1, "nox"));
        q5.addReward(new QuestReward(QuestReward.RewardType.BADGE, 62, "Void Badge"));
        q5.addReward(new QuestReward(QuestReward.RewardType.MONEY, 5000, "5000 Aether Shards"));
        q5.addReward(new QuestReward(QuestReward.RewardType.GAUNTLET_UPGRADE, 1, "Gauntlet Upgrade"));
        q5.addPrerequisite(4);
        allQuests.put(5, q5);

        // SIDE QUESTS
        Quest s1 = new Quest(6, "Lost in the Woods", "Find Finn's lost Ethereal in Whispering Woods", Quest.QuestType.SIDE, 5);
        s1.addObjective(new QuestObjective("find_sparkwing", "Find Sparkwing", QuestObjective.ObjectiveType.REACH_LOCATION, 1, "whispering_woods"));
        s1.addReward(new QuestReward(QuestReward.RewardType.MONEY, 500, "500 Aether Shards"));
        s1.addReward(new QuestReward(QuestReward.RewardType.ITEM, 2, "3x Enhanced Glyph"));
        s1.setStatus(Quest.QuestStatus.AVAILABLE);
        allQuests.put(6, s1);

        Quest s2 = new Quest(7, "Nature Scholar", "Capture 10 different Nature-type Ethereals", Quest.QuestType.COLLECTION, 10);
        s2.addObjective(new QuestObjective("capture_nature", "Capture 10 Nature types", QuestObjective.ObjectiveType.CAPTURE_TYPE, 10, "Nature"));
        s2.addReward(new QuestReward(QuestReward.RewardType.ITEM, 4, "5x Nature Glyph"));
        s2.addReward(new QuestReward(QuestReward.RewardType.ITEM, 47, "3x Rare Candy"));
        s2.setStatus(Quest.QuestStatus.AVAILABLE);
        allQuests.put(7, s2);

        Quest s3 = new Quest(8, "Type Master: Flame", "Defeat 3 trainers using only Flame-type Ethereals", Quest.QuestType.CHALLENGE, 20);
        s3.addObjective(new QuestObjective("flame_battles", "Win 3 battles with Flame types", QuestObjective.ObjectiveType.WIN_BATTLES, 3, "Flame"));
        s3.addReward(new QuestReward(QuestReward.RewardType.ITEM, 9, "1x Master Glyph"));
        s3.addReward(new QuestReward(QuestReward.RewardType.MONEY, 2000, "2000 Aether Shards"));
        allQuests.put(8, s3);

        Quest s4 = new Quest(9, "The Collector", "Complete 50% of the Aetherdex", Quest.QuestType.COLLECTION, 15);
        s4.addObjective(new QuestObjective("aetherdex", "Capture 76 different species", QuestObjective.ObjectiveType.CAPTURE_SPECIES, 76, "any"));
        s4.addReward(new QuestReward(QuestReward.RewardType.MONEY, 5000, "5000 Aether Shards"));
        s4.addReward(new QuestReward(QuestReward.RewardType.ITEM, 70, "Lucky Charm"));
        allQuests.put(9, s4);

        Quest s5 = new Quest(10, "Evolution Expert", "Evolve 5 different Ethereals", Quest.QuestType.COLLECTION, 12);
        s5.addObjective(new QuestObjective("evolve_ethereals", "Evolve 5 Ethereals", QuestObjective.ObjectiveType.EVOLVE, 5, "any"));
        s5.addReward(new QuestReward(QuestReward.RewardType.ITEM, 48, "3x Evolution Stone"));
        allQuests.put(10, s5);
    }

    /**
     * Activate a quest
     */
    public boolean activateQuest(int questId) {
        if (activeQuests.size() >= MAX_ACTIVE_QUESTS) {
            return false;
        }
        if (!allQuests.containsKey(questId)) {
            return false;
        }
        Quest quest = allQuests.get(questId);
        if (quest.getStatus() == Quest.QuestStatus.AVAILABLE) {
            quest.start();
            activeQuests.add(questId);
            return true;
        }
        return false;
    }

    /**
     * Update quest progress
     */
    public void updateQuestProgress(int questId, String objectiveId, int progress) {
        if (!allQuests.containsKey(questId)) return;
        Quest quest = allQuests.get(questId);
        if (quest.getStatus() == Quest.QuestStatus.IN_PROGRESS) {
            quest.updateProgress(objectiveId, progress);
            if (quest.getStatus() == Quest.QuestStatus.COMPLETED) {
                // Quest just completed
            }
        }
    }

    /**
     * Claim quest rewards
     */
    public List<QuestReward> claimQuest(int questId) {
        if (!allQuests.containsKey(questId)) return new ArrayList<>();
        Quest quest = allQuests.get(questId);
        List<QuestReward> rewards = quest.claimRewards();
        if (!rewards.isEmpty()) {
            activeQuests.remove(Integer.valueOf(questId));
            completedQuests.add(questId);
        }
        return rewards;
    }

    /**
     * Get all active quests
     */
    public List<Quest> getActiveQuests() {
        List<Quest> quests = new ArrayList<>();
        for (int id : activeQuests) {
            quests.add(allQuests.get(id));
        }
        return quests;
    }

    /**
     * Get all available quests
     */
    public List<Quest> getAvailableQuests() {
        List<Quest> quests = new ArrayList<>();
        for (Quest quest : allQuests.values()) {
            if (quest.getStatus() == Quest.QuestStatus.AVAILABLE) {
                quests.add(quest);
            }
        }
        return quests;
    }

    /**
     * Get quest by ID
     */
    public Quest getQuest(int id) {
        return allQuests.get(id);
    }

    public int getCompletedCount() {
        return completedQuests.size();
    }

    public int getTotalQuests() {
        return allQuests.size();
    }
}
