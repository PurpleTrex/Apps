package com.purpletrex.aether.world;

import com.purpletrex.aether.entities.ElementType;
import java.util.Random;

/**
 * Represents a map/region in the game world
 */
public class GameMap {
    private String name;
    private int width;
    private int height;
    private Tile[][] tiles;
    private ElementType primaryType;
    private int minLevel;
    private int maxLevel;
    private Random random;

    public GameMap(String name, int width, int height, ElementType primaryType, int minLevel, int maxLevel) {
        this.name = name;
        this.width = width;
        this.height = height;
        this.primaryType = primaryType;
        this.minLevel = minLevel;
        this.maxLevel = maxLevel;
        this.random = new Random();
        this.tiles = new Tile[height][width];
        generateMap();
    }

    /**
     * Generate a simple map
     */
    private void generateMap() {
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                // Simple generation: grass with some obstacles
                if (x == 0 || x == width - 1 || y == 0 || y == height - 1) {
                    // Borders are trees
                    tiles[y][x] = new Tile(Tile.TileType.TREE);
                } else if (random.nextFloat() < 0.1) {
                    // 10% chance of obstacle
                    tiles[y][x] = new Tile(random.nextBoolean() ? Tile.TileType.TREE : Tile.TileType.ROCK);
                } else if (random.nextFloat() < 0.3) {
                    // 30% tall grass
                    tiles[y][x] = new Tile(Tile.TileType.TALL_GRASS);
                } else {
                    // Regular grass
                    tiles[y][x] = new Tile(Tile.TileType.GRASS);
                }
            }
        }
        
        // Create a path in the middle
        int midY = height / 2;
        for (int x = 1; x < width - 1; x++) {
            tiles[midY][x] = new Tile(Tile.TileType.PATH);
            if (midY > 0) tiles[midY - 1][x] = new Tile(Tile.TileType.PATH);
        }
    }

    /**
     * Get tile at position
     */
    public Tile getTile(int x, int y) {
        if (x < 0 || x >= width || y < 0 || y >= height) {
            return null;
        }
        return tiles[y][x];
    }

    /**
     * Check if position is walkable
     */
    public boolean isWalkable(int x, int y) {
        Tile tile = getTile(x, y);
        return tile != null && tile.isWalkable();
    }

    /**
     * Check for random encounter
     */
    public boolean checkEncounter(int x, int y) {
        Tile tile = getTile(x, y);
        if (tile == null || !tile.hasEncounter()) {
            return false;
        }
        
        // Random encounter based on tile's encounter rate
        return random.nextInt(100) < tile.getEncounterRate();
    }

    /**
     * Get random wild Ethereal level for this area
     */
    public int getWildEtherealLevel() {
        return minLevel + random.nextInt(maxLevel - minLevel + 1);
    }

    /**
     * Get random wild Ethereal species for this area
     */
    public int getWildEtherealSpecies() {
        // Bias towards primary type for this region
        if (random.nextFloat() < 0.5f) {
            // Return species of primary type
            return getSpeciesOfType(primaryType);
        } else {
            // Return any common species (IDs 10-89)
            return 10 + random.nextInt(80);
        }
    }

    /**
     * Get a random species of a specific type
     */
    private int getSpeciesOfType(ElementType type) {
        // Simplified: return random from type range
        // In full game, this would properly filter by type
        switch (type) {
            case NATURE: return 10 + random.nextInt(15); // Nature species
            case FLAME: return 25 + random.nextInt(15);  // Flame species
            case AQUA: return 40 + random.nextInt(15);   // Aqua species
            case STORM: return 55 + random.nextInt(15);  // Storm species
            case VOID: return 70 + random.nextInt(15);   // Void species
            case RADIANT: return 85 + random.nextInt(15); // Radiant species
            case MINERAL: return 100 + random.nextInt(15); // Mineral species
            case MYSTIC: return 115 + random.nextInt(15);  // Mystic species
            default: return 10 + random.nextInt(80);
        }
    }

    // Getters
    public String getName() { return name; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public ElementType getPrimaryType() { return primaryType; }
    public int getMinLevel() { return minLevel; }
    public int getMaxLevel() { return maxLevel; }
}
