package com.purpletrex.aether.inventory;

/**
 * Represents an item in the game
 */
public class Item {
    private int id;
    private String name;
    private String description;
    private ItemType type;
    private int price;
    private boolean consumable;
    private int effectValue;

    public enum ItemType {
        GLYPH,          // Capture glyphs
        MEDICINE,       // HP/status healing
        BATTLE_ITEM,    // Stat boosters
        KEY_ITEM,       // Story items
        TREASURE        // Sellable items
    }

    public Item(int id, String name, String description, ItemType type, int price, boolean consumable, int effectValue) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.type = type;
        this.price = price;
        this.consumable = consumable;
        this.effectValue = effectValue;
    }

    // Getters
    public int getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public ItemType getType() { return type; }
    public int getPrice() { return price; }
    public boolean isConsumable() { return consumable; }
    public int getEffectValue() { return effectValue; }
}
