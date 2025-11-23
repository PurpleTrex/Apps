package com.purpletrex.aether.data;

import com.purpletrex.aether.entities.BaseStats;
import com.purpletrex.aether.entities.ElementType;

/**
 * Represents a species of Ethereal (the blueprint for creating individual Ethereals)
 */
public class EtherealSpecies {
    private int id;
    private String name;
    private ElementType type;
    private BaseStats baseStats;

    public EtherealSpecies(int id, String name, ElementType type, BaseStats baseStats) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.baseStats = baseStats;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public ElementType getType() { return type; }
    public BaseStats getBaseStats() { return baseStats; }
}
