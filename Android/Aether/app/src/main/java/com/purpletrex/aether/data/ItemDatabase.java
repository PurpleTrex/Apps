package com.purpletrex.aether.data;

import com.purpletrex.aether.inventory.Item;
import com.purpletrex.aether.inventory.Item.ItemType;
import java.util.HashMap;
import java.util.Map;

/**
 * Database of all items in the game
 */
public class ItemDatabase {
    private static final Map<Integer, Item> items = new HashMap<>();

    static {
        initializeItems();
    }

    private static void initializeItems() {
        // GLYPHS (IDs 1-10)
        addItem(1, "Standard Glyph", "Basic capture glyph. Unlimited use.", ItemType.GLYPH, 0, false, 100);
        addItem(2, "Enhanced Glyph", "Improved capture rate (1.5x)", ItemType.GLYPH, 200, true, 150);
        addItem(3, "Superior Glyph", "High capture rate (2.0x)", ItemType.GLYPH, 600, true, 200);
        addItem(4, "Nature Glyph", "3x for Nature types", ItemType.GLYPH, 1000, true, 300);
        addItem(5, "Flame Glyph", "3x for Flame types", ItemType.GLYPH, 1000, true, 300);
        addItem(6, "Aqua Glyph", "3x for Aqua types", ItemType.GLYPH, 1000, true, 300);
        addItem(7, "Storm Glyph", "3x for Storm types", ItemType.GLYPH, 1000, true, 300);
        addItem(8, "Void Glyph", "3x for Void types", ItemType.GLYPH, 1000, true, 300);
        addItem(9, "Master Glyph", "Near-guaranteed capture (3.5x)", ItemType.GLYPH, 5000, true, 350);
        addItem(10, "Legendary Glyph", "For legendaries only (4.0x)", ItemType.GLYPH, 0, true, 400);
        
        // MEDICINE - HP RESTORATION (IDs 11-20)
        addItem(11, "Minor Elixir", "Restores 20 HP", ItemType.MEDICINE, 50, true, 20);
        addItem(12, "Greater Elixir", "Restores 50 HP", ItemType.MEDICINE, 200, true, 50);
        addItem(13, "Super Elixir", "Restores 100 HP", ItemType.MEDICINE, 500, true, 100);
        addItem(14, "Max Elixir", "Restores 200 HP", ItemType.MEDICINE, 1000, true, 200);
        addItem(15, "Full Restore", "Restores all HP", ItemType.MEDICINE, 1500, true, 9999);
        addItem(16, "Aether Crystal", "Restores all HP and cures status", ItemType.MEDICINE, 2000, true, 9999);
        addItem(17, "Revival Shard", "Revives fainted Ethereal with 50% HP", ItemType.MEDICINE, 1500, true, 50);
        addItem(18, "Revival Crystal", "Revives fainted Ethereal with full HP", ItemType.MEDICINE, 3000, true, 100);
        addItem(19, "Energy Root", "Restores 120 HP (bitter)", ItemType.MEDICINE, 800, true, 120);
        addItem(20, "Sacred Ash", "Revives all fainted Ethereals", ItemType.MEDICINE, 10000, true, 100);
        
        // MEDICINE - STATUS CURES (IDs 21-30)
        addItem(21, "Antidote", "Cures poison", ItemType.MEDICINE, 100, true, 1);
        addItem(22, "Freeze Remedy", "Cures freeze", ItemType.MEDICINE, 100, true, 2);
        addItem(23, "Wake Essence", "Cures sleep", ItemType.MEDICINE, 100, true, 3);
        addItem(24, "Paralyze Cure", "Cures paralysis", ItemType.MEDICINE, 100, true, 4);
        addItem(25, "Burn Heal", "Cures burn", ItemType.MEDICINE, 100, true, 5);
        addItem(26, "Full Heal", "Cures all status effects", ItemType.MEDICINE, 400, true, 0);
        addItem(27, "Lava Cookie", "Cures all status effects", ItemType.MEDICINE, 200, true, 0);
        addItem(28, "Healing Water", "Cures any status", ItemType.MEDICINE, 300, true, 0);
        addItem(29, "Mental Herb", "Cures confusion", ItemType.MEDICINE, 150, true, 6);
        addItem(30, "Purity Shard", "Prevents status for one battle", ItemType.MEDICINE, 500, true, 10);
        
        // BATTLE ITEMS - STAT BOOSTERS (IDs 31-45)
        addItem(31, "Power Dust", "Raises ATK in battle", ItemType.BATTLE_ITEM, 300, true, 1);
        addItem(32, "Guard Dust", "Raises DEF in battle", ItemType.BATTLE_ITEM, 300, true, 2);
        addItem(33, "Speed Dust", "Raises SPD in battle", ItemType.BATTLE_ITEM, 300, true, 3);
        addItem(34, "Focus Dust", "Raises accuracy", ItemType.BATTLE_ITEM, 300, true, 4);
        addItem(35, "Special Dust", "Raises SP.ATK", ItemType.BATTLE_ITEM, 300, true, 5);
        addItem(36, "Aegis Dust", "Raises SP.DEF", ItemType.BATTLE_ITEM, 300, true, 6);
        addItem(37, "Critical Dust", "Raises critical hit ratio", ItemType.BATTLE_ITEM, 400, true, 7);
        addItem(38, "Evasion Dust", "Raises evasion", ItemType.BATTLE_ITEM, 350, true, 8);
        addItem(39, "HP Boost", "Temporarily raises max HP", ItemType.BATTLE_ITEM, 500, true, 9);
        addItem(40, "Rage Powder", "Forces foe to target user", ItemType.BATTLE_ITEM, 450, true, 10);
        
        // PERMANENT STAT ITEMS (IDs 41-50)
        addItem(41, "HP Shard", "Permanently increases HP", ItemType.BATTLE_ITEM, 5000, true, 100);
        addItem(42, "Power Core", "Permanently increases ATK", ItemType.BATTLE_ITEM, 5000, true, 101);
        addItem(43, "Shield Core", "Permanently increases DEF", ItemType.BATTLE_ITEM, 5000, true, 102);
        addItem(44, "Energy Core", "Permanently increases SP.ATK", ItemType.BATTLE_ITEM, 5000, true, 103);
        addItem(45, "Ward Core", "Permanently increases SP.DEF", ItemType.BATTLE_ITEM, 5000, true, 104);
        addItem(46, "Swift Core", "Permanently increases SPD", ItemType.BATTLE_ITEM, 5000, true, 105);
        addItem(47, "Rare Candy", "Increases level by 1", ItemType.BATTLE_ITEM, 10000, true, 999);
        addItem(48, "Evolution Stone", "Forces evolution", ItemType.BATTLE_ITEM, 3000, true, 998);
        addItem(49, "Friendship Berry", "Increases friendship", ItemType.BATTLE_ITEM, 200, true, 997);
        addItem(50, "Reset Herb", "Resets all stat changes", ItemType.BATTLE_ITEM, 1000, true, 996);
        
        // KEY ITEMS (IDs 51-70)
        addItem(51, "Ethereal Gauntlet", "Your magic gauntlet for capturing", ItemType.KEY_ITEM, 0, false, 0);
        addItem(52, "Aetherdex", "Records all Ethereals you've seen", ItemType.KEY_ITEM, 0, false, 0);
        addItem(53, "Region Map - Verdant", "Map of Verdant Hollow", ItemType.KEY_ITEM, 0, false, 1);
        addItem(54, "Region Map - Crimson", "Map of Crimson Wastes", ItemType.KEY_ITEM, 0, false, 2);
        addItem(55, "Region Map - Azure", "Map of Azure Depths", ItemType.KEY_ITEM, 0, false, 3);
        addItem(56, "Region Map - Tempest", "Map of Tempest Peaks", ItemType.KEY_ITEM, 0, false, 4);
        addItem(57, "Region Map - Obsidian", "Map of Obsidian Core", ItemType.KEY_ITEM, 0, false, 5);
        addItem(58, "Forest Badge", "Champion Sylva's badge", ItemType.KEY_ITEM, 0, false, 10);
        addItem(59, "Flame Badge", "Champion Ignis's badge", ItemType.KEY_ITEM, 0, false, 11);
        addItem(60, "Aqua Badge", "Champion Marina's badge", ItemType.KEY_ITEM, 0, false, 12);
        addItem(61, "Storm Badge", "Champion Tempus's badge", ItemType.KEY_ITEM, 0, false, 13);
        addItem(62, "Void Badge", "Champion Nox's badge", ItemType.KEY_ITEM, 0, false, 14);
        addItem(63, "Master Key", "Opens special doors", ItemType.KEY_ITEM, 0, false, 20);
        addItem(64, "Void Keystone", "Activates the Nexus", ItemType.KEY_ITEM, 0, false, 21);
        addItem(65, "Ancient Relic", "Mysterious artifact", ItemType.KEY_ITEM, 0, false, 22);
        addItem(66, "Bicycle", "Travel faster", ItemType.KEY_ITEM, 0, false, 30);
        addItem(67, "Fishing Rod", "Catch water Ethereals", ItemType.KEY_ITEM, 0, false, 31);
        addItem(68, "Dowsing Rod", "Find hidden items", ItemType.KEY_ITEM, 0, false, 32);
        addItem(69, "Exp Share", "Shares EXP with all team", ItemType.KEY_ITEM, 0, false, 40);
        addItem(70, "Lucky Charm", "Increases shiny rate", ItemType.KEY_ITEM, 0, false, 41);
        
        // TREASURES (IDs 71-80)
        addItem(71, "Pearl", "Pretty pearl. Sell for 700.", ItemType.TREASURE, 700, false, 0);
        addItem(72, "Big Pearl", "Large pearl. Sell for 3000.", ItemType.TREASURE, 3000, false, 0);
        addItem(73, "Star Piece", "Red gem. Sell for 5000.", ItemType.TREASURE, 5000, false, 0);
        addItem(74, "Gold Nugget", "Pure gold. Sell for 10000.", ItemType.TREASURE, 10000, false, 0);
        addItem(75, "Silver Leaf", "Silver leaf. Sell for 500.", ItemType.TREASURE, 500, false, 0);
        addItem(76, "Gold Leaf", "Golden leaf. Sell for 2000.", ItemType.TREASURE, 2000, false, 0);
        addItem(77, "Comet Shard", "Meteor fragment. Sell for 15000.", ItemType.TREASURE, 15000, false, 0);
        addItem(78, "Relic Coin", "Ancient coin. Sell for 1000.", ItemType.TREASURE, 1000, false, 0);
        addItem(79, "Relic Vase", "Ancient vase. Sell for 5000.", ItemType.TREASURE, 5000, false, 0);
        addItem(80, "Relic Crown", "Ancient crown. Sell for 20000.", ItemType.TREASURE, 20000, false, 0);
    }

    private static void addItem(int id, String name, String description, ItemType type, int price, boolean consumable, int effectValue) {
        items.put(id, new Item(id, name, description, type, price, consumable, effectValue));
    }

    public static Item getItem(int id) {
        return items.get(id);
    }

    public static int getItemCount() {
        return items.size();
    }
}
