package com.purpletrex.aether.inventory;

import java.util.HashMap;
import java.util.Map;

/**
 * Player's inventory system
 */
public class Inventory {
    private Map<Integer, Integer> items; // item ID -> quantity
    private int money;
    private static final int MAX_ITEM_STACK = 99;

    public Inventory() {
        this.items = new HashMap<>();
        this.money = 3000; // Starting money
    }

    /**
     * Add an item to inventory
     */
    public boolean addItem(int itemId, int quantity) {
        int currentAmount = items.getOrDefault(itemId, 0);
        if (currentAmount + quantity > MAX_ITEM_STACK) {
            return false;
        }
        items.put(itemId, currentAmount + quantity);
        return true;
    }

    /**
     * Remove an item from inventory
     */
    public boolean removeItem(int itemId, int quantity) {
        int currentAmount = items.getOrDefault(itemId, 0);
        if (currentAmount < quantity) {
            return false;
        }
        if (currentAmount == quantity) {
            items.remove(itemId);
        } else {
            items.put(itemId, currentAmount - quantity);
        }
        return true;
    }

    /**
     * Get quantity of an item
     */
    public int getItemQuantity(int itemId) {
        return items.getOrDefault(itemId, 0);
    }

    /**
     * Check if has item
     */
    public boolean hasItem(int itemId) {
        return items.containsKey(itemId) && items.get(itemId) > 0;
    }

    /**
     * Add money
     */
    public void addMoney(int amount) {
        money += amount;
    }

    /**
     * Remove money
     */
    public boolean removeMoney(int amount) {
        if (money < amount) {
            return false;
        }
        money -= amount;
        return true;
    }

    /**
     * Get all items
     */
    public Map<Integer, Integer> getAllItems() {
        return new HashMap<>(items);
    }

    // Getters
    public int getMoney() { return money; }
}
