package com.purpletrex.aether.world;

/**
 * Represents a tile in the game world
 */
public class Tile {
    private TileType type;
    private boolean walkable;
    private boolean hasEncounter;
    private int encounterRate;

    public enum TileType {
        GRASS(true, true, 10),
        TALL_GRASS(true, true, 30),
        WATER(false, true, 20),
        PATH(true, false, 0),
        TREE(false, false, 0),
        ROCK(false, false, 0),
        BUILDING(false, false, 0),
        DOOR(true, false, 0),
        LEDGE(true, false, 0),
        CAVE_FLOOR(true, true, 15),
        SAND(true, false, 0),
        BRIDGE(true, false, 0);

        private final boolean walkable;
        private final boolean hasEncounter;
        private final int encounterRate;

        TileType(boolean walkable, boolean hasEncounter, int encounterRate) {
            this.walkable = walkable;
            this.hasEncounter = hasEncounter;
            this.encounterRate = encounterRate;
        }

        public boolean isWalkable() { return walkable; }
        public boolean hasEncounter() { return hasEncounter; }
        public int getEncounterRate() { return encounterRate; }
    }

    public Tile(TileType type) {
        this.type = type;
        this.walkable = type.isWalkable();
        this.hasEncounter = type.hasEncounter();
        this.encounterRate = type.getEncounterRate();
    }

    public TileType getType() { return type; }
    public boolean isWalkable() { return walkable; }
    public boolean hasEncounter() { return hasEncounter; }
    public int getEncounterRate() { return encounterRate; }
}
