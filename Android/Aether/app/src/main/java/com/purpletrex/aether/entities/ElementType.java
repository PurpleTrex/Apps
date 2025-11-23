package com.purpletrex.aether.entities;

/**
 * Enum representing the 8 elemental types in Aether
 */
public enum ElementType {
    NATURE("Nature", 0xFF2D5016),
    FLAME("Flame", 0xFFDC143C),
    AQUA("Aqua", 0xFF4682B4),
    STORM("Storm", 0xFFFFD700),
    VOID("Void", 0xFF4B0082),
    RADIANT("Radiant", 0xFFFFFFFF),
    MINERAL("Mineral", 0xFF696969),
    MYSTIC("Mystic", 0xFFFF69B4);

    private final String name;
    private final int color;

    ElementType(String name, int color) {
        this.name = name;
        this.color = color;
    }

    public String getName() {
        return name;
    }

    public int getColor() {
        return color;
    }

    /**
     * Get type effectiveness multiplier
     * @param defendingType The type being attacked
     * @return Damage multiplier (0.5, 1.0, or 2.0)
     */
    public float getEffectivenessAgainst(ElementType defendingType) {
        switch (this) {
            case NATURE:
                if (defendingType == AQUA || defendingType == MINERAL) return 2.0f;
                if (defendingType == FLAME || defendingType == RADIANT) return 0.5f;
                break;
            case FLAME:
                if (defendingType == NATURE || defendingType == MINERAL) return 2.0f;
                if (defendingType == AQUA || defendingType == RADIANT) return 0.5f;
                break;
            case AQUA:
                if (defendingType == FLAME || defendingType == MINERAL) return 2.0f;
                if (defendingType == NATURE || defendingType == STORM) return 0.5f;
                break;
            case STORM:
                if (defendingType == AQUA || defendingType == MINERAL) return 2.0f;
                if (defendingType == STORM) return 0.5f;
                break;
            case VOID:
                if (defendingType == VOID || defendingType == MYSTIC) return 2.0f;
                if (defendingType == RADIANT) return 0.5f;
                break;
            case RADIANT:
                if (defendingType == VOID) return 2.0f;
                if (defendingType == RADIANT) return 0.5f;
                break;
            case MINERAL:
                if (defendingType == MYSTIC) return 2.0f;
                if (defendingType == NATURE || defendingType == AQUA || defendingType == FLAME) return 0.5f;
                break;
            case MYSTIC:
                if (defendingType == VOID) return 0.5f;
                if (defendingType == MYSTIC) return 0.5f;
                break;
        }
        return 1.0f;
    }
}
