package com.purpletrex.aether.core;

import com.purpletrex.aether.entities.Ethereal;
import com.purpletrex.aether.inventory.Inventory;
import com.purpletrex.aether.quest.QuestManager;
import com.purpletrex.aether.gauntlet.MagicGauntlet;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents the player's game data
 */
public class PlayerData {
    private String name;
    private int playerX;
    private int playerY;
    private String currentRegion;
    private List<Ethereal> team;
    private List<Ethereal> storage;
    private Inventory inventory;
    private QuestManager questManager;
    private MagicGauntlet gauntlet;
    private int playTime; // in seconds
    private List<Integer> capturedSpecies; // Aetherdex
    private int[] badges; // Champion badges

    public PlayerData(String name) {
        this.name = name;
        this.playerX = 10;
        this.playerY = 10;
        this.currentRegion = "Verdant Hollow";
        this.team = new ArrayList<>();
        this.storage = new ArrayList<>();
        this.inventory = new Inventory();
        this.questManager = new QuestManager();
        this.gauntlet = new MagicGauntlet(inventory);
        this.playTime = 0;
        this.capturedSpecies = new ArrayList<>();
        this.badges = new int[5]; // 5 badges
        
        // Give starting items
        inventory.addItem(1, 1); // Standard Glyph (infinite)
        inventory.addItem(11, 5); // 5x Minor Elixir
        inventory.addItem(51, 1); // Ethereal Gauntlet
        inventory.addItem(52, 1); // Aetherdex
        inventory.addItem(53, 1); // Verdant Hollow Map
    }

    /**
     * Add Ethereal to team
     */
    public boolean addToTeam(Ethereal ethereal) {
        if (team.size() < 6) {
            team.add(ethereal);
            addToCaptured(ethereal.getSpeciesId());
            return true;
        }
        return false;
    }

    /**
     * Add Ethereal to storage
     */
    public void addToStorage(Ethereal ethereal) {
        storage.add(ethereal);
        addToCaptured(ethereal.getSpeciesId());
    }

    /**
     * Add species to Aetherdex
     */
    private void addToCaptured(int speciesId) {
        if (!capturedSpecies.contains(speciesId)) {
            capturedSpecies.add(speciesId);
        }
    }

    /**
     * Award a badge
     */
    public void awardBadge(int badgeIndex) {
        if (badgeIndex >= 0 && badgeIndex < badges.length) {
            badges[badgeIndex] = 1;
        }
    }

    /**
     * Check if has badge
     */
    public boolean hasBadge(int badgeIndex) {
        return badgeIndex >= 0 && badgeIndex < badges.length && badges[badgeIndex] == 1;
    }

    /**
     * Get number of badges
     */
    public int getBadgeCount() {
        int count = 0;
        for (int badge : badges) {
            if (badge == 1) count++;
        }
        return count;
    }

    /**
     * Get Aetherdex completion percentage
     */
    public float getAetherdexCompletion() {
        return (float) capturedSpecies.size() / 151.0f * 100.0f;
    }

    /**
     * Increment play time
     */
    public void incrementPlayTime(int seconds) {
        playTime += seconds;
    }

    /**
     * Get formatted play time
     */
    public String getFormattedPlayTime() {
        int hours = playTime / 3600;
        int minutes = (playTime % 3600) / 60;
        return String.format("%02d:%02d", hours, minutes);
    }

    // Getters and setters
    public String getName() { return name; }
    public int getPlayerX() { return playerX; }
    public int getPlayerY() { return playerY; }
    public String getCurrentRegion() { return currentRegion; }
    public List<Ethereal> getTeam() { return team; }
    public List<Ethereal> getStorage() { return storage; }
    public Inventory getInventory() { return inventory; }
    public QuestManager getQuestManager() { return questManager; }
    public MagicGauntlet getGauntlet() { return gauntlet; }
    public int getPlayTime() { return playTime; }
    public List<Integer> getCapturedSpecies() { return capturedSpecies; }
    
    public void setPlayerX(int x) { this.playerX = x; }
    public void setPlayerY(int y) { this.playerY = y; }
    public void setCurrentRegion(String region) { this.currentRegion = region; }
}
