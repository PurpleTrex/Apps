package com.purpletrex.aether.entities;

import com.purpletrex.aether.battle.Ability;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents an individual Ethereal creature
 */
public class Ethereal {
    private int speciesId;
    private String nickname;
    private String speciesName;
    private ElementType type;
    private int level;
    private int experience;
    private Stats stats;
    private BaseStats baseStats;
    private List<Ability> abilities;
    private StatusCondition status;
    private boolean isShiny;
    private int friendship;

    public Ethereal(int speciesId, String speciesName, ElementType type, BaseStats baseStats, int level) {
        this.speciesId = speciesId;
        this.speciesName = speciesName;
        this.nickname = speciesName;
        this.type = type;
        this.level = level;
        this.baseStats = baseStats;
        this.stats = Stats.calculateStats(baseStats, level);
        this.abilities = new ArrayList<>();
        this.status = StatusCondition.NONE;
        this.isShiny = Math.random() < 0.001; // 0.1% shiny rate
        this.friendship = 50;
        this.experience = calculateExperienceForLevel(level);
    }

    /**
     * Calculate damage this Ethereal deals to a defender
     */
    public int calculateDamage(Ability ability, Ethereal defender) {
        // Base damage calculation: ((2 * Level / 5 + 2) * Power * A/D) / 50 + 2
        float attackStat = ability.isPhysical() ? stats.getAttack() : stats.getSpecialAttack();
        float defenseStat = ability.isPhysical() ? defender.getStats().getDefense() : defender.getStats().getSpecialDefense();
        
        float baseDamage = ((2.0f * level / 5.0f + 2) * ability.getPower() * attackStat / defenseStat) / 50.0f + 2;
        
        // Type effectiveness
        float typeMultiplier = type.getEffectivenessAgainst(defender.getType());
        
        // STAB (Same Type Attack Bonus)
        float stab = (ability.getType() == type) ? 1.5f : 1.0f;
        
        // Random factor (0.85 to 1.0)
        float random = 0.85f + (float)(Math.random() * 0.15f);
        
        // Critical hit (6.25% chance for 2x damage)
        float critical = (Math.random() < 0.0625f) ? 2.0f : 1.0f;
        
        // Status condition modifier (Burn halves physical attack)
        float statusMod = 1.0f;
        if (status == StatusCondition.BURN && ability.isPhysical()) {
            statusMod = 0.5f;
        }
        
        int finalDamage = (int)(baseDamage * typeMultiplier * stab * random * critical * statusMod);
        return Math.max(1, finalDamage); // Minimum 1 damage
    }

    /**
     * Take damage from an attack
     */
    public void takeDamage(int damage) {
        stats.takeDamage(damage);
    }

    /**
     * Gain experience points
     */
    public void gainExperience(int exp) {
        experience += exp;
        checkLevelUp();
    }

    /**
     * Check if this Ethereal should level up
     */
    private void checkLevelUp() {
        int expNeeded = calculateExperienceForLevel(level + 1);
        while (experience >= expNeeded && level < 100) {
            level++;
            stats = Stats.calculateStats(baseStats, level);
            stats.fullHeal();
            expNeeded = calculateExperienceForLevel(level + 1);
        }
    }

    /**
     * Calculate total experience needed for a level
     */
    private int calculateExperienceForLevel(int lvl) {
        // Medium Fast growth rate: level^3
        return lvl * lvl * lvl;
    }

    /**
     * Heal this Ethereal
     */
    public void heal(int amount) {
        stats.heal(amount);
    }

    /**
     * Full heal
     */
    public void fullHeal() {
        stats.fullHeal();
        status = StatusCondition.NONE;
    }

    /**
     * Apply status condition
     */
    public void applyStatus(StatusCondition condition) {
        if (status == StatusCondition.NONE) {
            status = condition;
        }
    }

    /**
     * Process end of turn effects
     */
    public void endTurnEffects() {
        switch (status) {
            case BURN:
            case POISON:
                int damage = stats.getMaxHp() / 8;
                stats.takeDamage(damage);
                break;
            case FREEZE:
                // 20% chance to thaw
                if (Math.random() < 0.2) {
                    status = StatusCondition.NONE;
                }
                break;
        }
    }

    /**
     * Check if this Ethereal can act this turn
     */
    public boolean canAct() {
        switch (status) {
            case SLEEP:
                // 33% chance to wake up
                if (Math.random() < 0.33) {
                    status = StatusCondition.NONE;
                    return true;
                }
                return false;
            case FREEZE:
                return false;
            case PARALYZE:
                // 25% chance to be fully paralyzed
                return Math.random() >= 0.25;
            case CONFUSION:
                // 50% chance to hurt self
                if (Math.random() < 0.5) {
                    int selfDamage = stats.getMaxHp() / 8;
                    stats.takeDamage(selfDamage);
                    return false;
                }
                return true;
        }
        return true;
    }

    /**
     * Learn a new ability
     */
    public void learnAbility(Ability ability) {
        if (abilities.size() < 4) {
            abilities.add(ability);
        }
    }

    /**
     * Calculate capture rate
     */
    public float calculateCaptureRate(float glyphMultiplier) {
        float hpFactor = (3.0f * stats.getMaxHp() - 2.0f * stats.getCurrentHp()) / (3.0f * stats.getMaxHp());
        float statusBonus = 1.0f;
        
        switch (status) {
            case SLEEP:
            case FREEZE:
                statusBonus = 2.0f;
                break;
            case PARALYZE:
            case BURN:
            case POISON:
                statusBonus = 1.5f;
                break;
        }
        
        // Base capture rate depends on rarity (legendaries are harder)
        float baseCaptureRate = speciesId > 143 ? 3.0f : 45.0f;
        
        float captureValue = hpFactor * glyphMultiplier * baseCaptureRate * statusBonus;
        return Math.min(255.0f, captureValue) / 255.0f;
    }

    // Getters
    public int getSpeciesId() { return speciesId; }
    public String getNickname() { return nickname; }
    public String getSpeciesName() { return speciesName; }
    public ElementType getType() { return type; }
    public int getLevel() { return level; }
    public int getExperience() { return experience; }
    public Stats getStats() { return stats; }
    public List<Ability> getAbilities() { return abilities; }
    public StatusCondition getStatus() { return status; }
    public boolean isShiny() { return isShiny; }
    public int getFriendship() { return friendship; }
    public boolean isFainted() { return stats.isFainted(); }

    // Setters
    public void setNickname(String nickname) { this.nickname = nickname; }
    public void setStatus(StatusCondition status) { this.status = status; }
    public void increaseFriendship(int amount) { 
        friendship = Math.min(255, friendship + amount); 
    }
}
