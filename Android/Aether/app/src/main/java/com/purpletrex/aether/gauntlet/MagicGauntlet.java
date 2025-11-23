package com.purpletrex.aether.gauntlet;

import com.purpletrex.aether.entities.Ethereal;
import com.purpletrex.aether.inventory.Inventory;
import com.purpletrex.aether.data.ItemDatabase;

/**
 * Magic Gauntlet system for capturing Ethereals
 */
public class MagicGauntlet {
    private GauntletTier tier;
    private Inventory inventory;

    public enum GauntletTier {
        NOVICE(1, "Novice Gauntlet", 2),
        ADEPT(2, "Adept Gauntlet", 4),
        MASTER(3, "Master Gauntlet", 6),
        GRANDMASTER(4, "Grandmaster Gauntlet", 8);

        private final int level;
        private final String name;
        private final int itemSlots;

        GauntletTier(int level, String name, int itemSlots) {
            this.level = level;
            this.name = name;
            this.itemSlots = itemSlots;
        }

        public int getLevel() { return level; }
        public String getName() { return name; }
        public int getItemSlots() { return itemSlots; }
    }

    public MagicGauntlet(Inventory inventory) {
        this.tier = GauntletTier.NOVICE;
        this.inventory = inventory;
    }

    /**
     * Attempt to capture an Ethereal
     */
    public CaptureResult attemptCapture(Ethereal target, int glyphItemId) {
        // Check if have the glyph
        if (!inventory.hasItem(glyphItemId)) {
            return new CaptureResult(false, "You don't have that glyph!");
        }

        // Get glyph multiplier based on item
        float glyphMultiplier = getGlyphMultiplier(glyphItemId, target);

        // Calculate capture rate
        float captureRate = target.calculateCaptureRate(glyphMultiplier);

        // Consume glyph if it's consumable
        if (glyphItemId != 1) { // Standard Glyph is not consumable
            inventory.removeItem(glyphItemId, 1);
        }

        // Attempt capture
        boolean success = Math.random() < captureRate;

        if (success) {
            return new CaptureResult(true, "Gotcha! " + target.getNickname() + " was captured!");
        } else {
            // Different messages based on how close
            if (captureRate > 0.75f) {
                return new CaptureResult(false, "Argh! Almost had it!");
            } else if (captureRate > 0.5f) {
                return new CaptureResult(false, "Oh no! It broke free!");
            } else if (captureRate > 0.25f) {
                return new CaptureResult(false, "Aww! It appeared to be caught!");
            } else {
                return new CaptureResult(false, "Shoot! It was so close too!");
            }
        }
    }

    /**
     * Get capture multiplier for a glyph
     */
    private float getGlyphMultiplier(int glyphItemId, Ethereal target) {
        switch (glyphItemId) {
            case 1: return 1.0f; // Standard Glyph
            case 2: return 1.5f; // Enhanced Glyph
            case 3: return 2.0f; // Superior Glyph
            case 4: // Nature Glyph
                return target.getType().getName().equals("Nature") ? 3.0f : 1.0f;
            case 5: // Flame Glyph
                return target.getType().getName().equals("Flame") ? 3.0f : 1.0f;
            case 6: // Aqua Glyph
                return target.getType().getName().equals("Aqua") ? 3.0f : 1.0f;
            case 7: // Storm Glyph
                return target.getType().getName().equals("Storm") ? 3.0f : 1.0f;
            case 8: // Void Glyph
                return target.getType().getName().equals("Void") ? 3.0f : 1.0f;
            case 9: return 3.5f; // Master Glyph
            case 10: return 4.0f; // Legendary Glyph
            default: return 1.0f;
        }
    }

    /**
     * Upgrade the gauntlet
     */
    public boolean upgrade() {
        switch (tier) {
            case NOVICE:
                tier = GauntletTier.ADEPT;
                return true;
            case ADEPT:
                tier = GauntletTier.MASTER;
                return true;
            case MASTER:
                tier = GauntletTier.GRANDMASTER;
                return true;
            default:
                return false;
        }
    }

    /**
     * Check if can show capture rate
     */
    public boolean canShowCaptureRate() {
        return tier.getLevel() >= 3; // Master and above
    }

    /**
     * Check if can show type effectiveness
     */
    public boolean canShowEffectiveness() {
        return tier.getLevel() >= 3; // Master and above
    }

    // Getters
    public GauntletTier getTier() { return tier; }
    public int getItemSlots() { return tier.getItemSlots(); }

    /**
     * Result of a capture attempt
     */
    public static class CaptureResult {
        private boolean success;
        private String message;

        public CaptureResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
    }
}
