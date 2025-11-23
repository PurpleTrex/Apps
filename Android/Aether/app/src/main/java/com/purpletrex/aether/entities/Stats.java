package com.purpletrex.aether.entities;

/**
 * Stats for an Ethereal
 */
public class Stats {
    private int maxHp;
    private int currentHp;
    private int attack;
    private int defense;
    private int specialAttack;
    private int specialDefense;
    private int speed;

    public Stats(int maxHp, int attack, int defense, int specialAttack, int specialDefense, int speed) {
        this.maxHp = maxHp;
        this.currentHp = maxHp;
        this.attack = attack;
        this.defense = defense;
        this.specialAttack = specialAttack;
        this.specialDefense = specialDefense;
        this.speed = speed;
    }

    // Calculate stats based on level and base stats
    public static Stats calculateStats(BaseStats base, int level) {
        int hp = ((base.getHp() * 2) * level / 100) + level + 10;
        int atk = ((base.getAttack() * 2) * level / 100) + 5;
        int def = ((base.getDefense() * 2) * level / 100) + 5;
        int spAtk = ((base.getSpecialAttack() * 2) * level / 100) + 5;
        int spDef = ((base.getSpecialDefense() * 2) * level / 100) + 5;
        int spd = ((base.getSpeed() * 2) * level / 100) + 5;
        
        return new Stats(hp, atk, def, spAtk, spDef, spd);
    }

    public void takeDamage(int damage) {
        currentHp = Math.max(0, currentHp - damage);
    }

    public void heal(int amount) {
        currentHp = Math.min(maxHp, currentHp + amount);
    }

    public void fullHeal() {
        currentHp = maxHp;
    }

    public boolean isFainted() {
        return currentHp <= 0;
    }

    public float getHpPercentage() {
        return (float) currentHp / maxHp;
    }

    // Getters
    public int getMaxHp() { return maxHp; }
    public int getCurrentHp() { return currentHp; }
    public int getAttack() { return attack; }
    public int getDefense() { return defense; }
    public int getSpecialAttack() { return specialAttack; }
    public int getSpecialDefense() { return specialDefense; }
    public int getSpeed() { return speed; }

    // Setters
    public void setCurrentHp(int hp) { this.currentHp = Math.max(0, Math.min(maxHp, hp)); }
}
