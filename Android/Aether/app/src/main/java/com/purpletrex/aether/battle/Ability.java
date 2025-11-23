package com.purpletrex.aether.battle;

import com.purpletrex.aether.entities.ElementType;

/**
 * Represents an ability/move that an Ethereal can use in battle
 */
public class Ability {
    private String name;
    private ElementType type;
    private int power;
    private int accuracy;
    private boolean isPhysical;
    private String description;

    public Ability(String name, ElementType type, int power, int accuracy, boolean isPhysical, String description) {
        this.name = name;
        this.type = type;
        this.power = power;
        this.accuracy = accuracy;
        this.isPhysical = isPhysical;
        this.description = description;
    }

    public boolean attemptHit() {
        return Math.random() * 100 < accuracy;
    }

    public String getName() { return name; }
    public ElementType getType() { return type; }
    public int getPower() { return power; }
    public int getAccuracy() { return accuracy; }
    public boolean isPhysical() { return isPhysical; }
    public String getDescription() { return description; }
}
