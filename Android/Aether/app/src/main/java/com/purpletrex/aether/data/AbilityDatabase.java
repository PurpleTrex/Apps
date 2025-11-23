package com.purpletrex.aether.data;

import com.purpletrex.aether.battle.Ability;
import com.purpletrex.aether.entities.ElementType;
import java.util.HashMap;
import java.util.Map;

/**
 * Database containing all abilities/moves that Ethereals can learn
 */
public class AbilityDatabase {
    private static final Map<Integer, Ability> abilities = new HashMap<>();

    static {
        initializeAbilities();
    }

    private static void initializeAbilities() {
        // BASIC MOVES (IDs 1-10)
        addAbility(1, "Tackle", ElementType.NATURE, 40, 100, true, "Basic physical attack");
        addAbility(2, "Scratch", ElementType.NATURE, 40, 100, true, "Rakes with claws");
        addAbility(3, "Quick Attack", ElementType.NATURE, 40, 100, true, "Fast priority move");
        addAbility(4, "Body Slam", ElementType.NATURE, 85, 100, true, "Heavy physical strike");
        addAbility(5, "Hyper Beam", ElementType.NATURE, 150, 90, false, "Powerful beam attack");
        addAbility(6, "Slash", ElementType.NATURE, 70, 100, true, "Sharp claw strike");
        addAbility(7, "Bite", ElementType.NATURE, 60, 100, true, "Dark bite attack");
        addAbility(8, "Crush", ElementType.NATURE, 80, 100, true, "Powerful crushing attack");
        addAbility(9, "Swift", ElementType.NATURE, 60, 999, false, "Never misses");
        addAbility(10, "Rage", ElementType.NATURE, 20, 100, true, "Increases attack");
        
        // NATURE MOVES (IDs 11-25)
        addAbility(11, "Vine Whip", ElementType.NATURE, 45, 100, true, "Strikes with vines");
        addAbility(12, "Razor Leaf", ElementType.NATURE, 55, 95, true, "Sharp leaf projectiles");
        addAbility(13, "Solar Beam", ElementType.NATURE, 120, 100, false, "Powerful solar energy");
        addAbility(14, "Petal Dance", ElementType.NATURE, 120, 100, false, "Rampaging petal storm");
        addAbility(15, "Seed Bomb", ElementType.NATURE, 80, 100, true, "Explosive seeds");
        addAbility(16, "Wood Hammer", ElementType.NATURE, 120, 100, true, "Powerful wooden strike");
        addAbility(17, "Leaf Blade", ElementType.NATURE, 90, 100, true, "Sharp leaf sword");
        addAbility(18, "Energy Ball", ElementType.NATURE, 90, 100, false, "Nature energy sphere");
        addAbility(19, "Thorn Strike", ElementType.NATURE, 70, 100, true, "Thorny attack");
        addAbility(20, "Forest Rage", ElementType.NATURE, 95, 95, false, "Forest fury");
        addAbility(21, "Growth Burst", ElementType.NATURE, 75, 100, false, "Sudden growth attack");
        addAbility(22, "Photosynthesis", ElementType.NATURE, 0, 100, false, "Heals HP");
        addAbility(23, "Spore Cloud", ElementType.NATURE, 0, 75, false, "Induces sleep");
        addAbility(24, "Poison Powder", ElementType.NATURE, 0, 75, false, "Poisons target");
        addAbility(25, "Leech Seed", ElementType.NATURE, 0, 90, false, "Drains HP over time");
        
        // FLAME MOVES (IDs 26-40)
        addAbility(26, "Ember", ElementType.FLAME, 40, 100, false, "Small flame");
        addAbility(27, "Flame Burst", ElementType.FLAME, 70, 100, false, "Bursting flames");
        addAbility(28, "Flamethrower", ElementType.FLAME, 90, 100, false, "Stream of fire");
        addAbility(29, "Fire Blast", ElementType.FLAME, 110, 85, false, "Intense fire blast");
        addAbility(30, "Inferno", ElementType.FLAME, 100, 50, false, "Overwhelming flames");
        addAbility(31, "Fire Fang", ElementType.FLAME, 65, 95, true, "Fiery bite");
        addAbility(32, "Blaze Kick", ElementType.FLAME, 85, 90, true, "Flaming kick");
        addAbility(33, "Fire Punch", ElementType.FLAME, 75, 100, true, "Fiery punch");
        addAbility(34, "Heat Wave", ElementType.FLAME, 95, 90, false, "Wave of heat");
        addAbility(35, "Lava Plume", ElementType.FLAME, 80, 100, false, "Erupting lava");
        addAbility(36, "Incinerate", ElementType.FLAME, 60, 100, false, "Burns completely");
        addAbility(37, "Sacred Fire", ElementType.FLAME, 100, 95, false, "Holy flames");
        addAbility(38, "Eruption", ElementType.FLAME, 150, 100, false, "Volcanic explosion");
        addAbility(39, "Will-O-Wisp", ElementType.FLAME, 0, 85, false, "Burns opponent");
        addAbility(40, "Sunny Day", ElementType.FLAME, 0, 100, false, "Boosts fire moves");
        
        // AQUA MOVES (IDs 41-55)
        addAbility(41, "Water Gun", ElementType.AQUA, 40, 100, false, "Water spray");
        addAbility(42, "Bubble Beam", ElementType.AQUA, 65, 100, false, "Bubble stream");
        addAbility(43, "Water Pulse", ElementType.AQUA, 60, 100, false, "Water wave");
        addAbility(44, "Surf", ElementType.AQUA, 90, 100, false, "Large wave");
        addAbility(45, "Hydro Pump", ElementType.AQUA, 110, 80, false, "Massive water blast");
        addAbility(46, "Aqua Tail", ElementType.AQUA, 90, 90, true, "Water tail strike");
        addAbility(47, "Waterfall", ElementType.AQUA, 80, 100, true, "Charging water strike");
        addAbility(48, "Ice Beam", ElementType.AQUA, 90, 100, false, "Freezing beam");
        addAbility(49, "Blizzard", ElementType.AQUA, 110, 70, false, "Freezing storm");
        addAbility(50, "Ice Shard", ElementType.AQUA, 40, 100, true, "Ice projectile");
        addAbility(51, "Icicle Crash", ElementType.AQUA, 85, 90, true, "Falling icicles");
        addAbility(52, "Whirlpool", ElementType.AQUA, 35, 85, false, "Traps opponent");
        addAbility(53, "Brine", ElementType.AQUA, 65, 100, false, "Extra damage if low HP");
        addAbility(54, "Aqua Ring", ElementType.AQUA, 0, 100, false, "Gradual healing");
        addAbility(55, "Rain Dance", ElementType.AQUA, 0, 100, false, "Boosts water moves");
        
        // STORM MOVES (IDs 56-70)
        addAbility(56, "Thunder Shock", ElementType.STORM, 40, 100, false, "Electric shock");
        addAbility(57, "Spark", ElementType.STORM, 65, 100, true, "Electric tackle");
        addAbility(58, "Thunderbolt", ElementType.STORM, 90, 100, false, "Strong electric blast");
        addAbility(59, "Thunder", ElementType.STORM, 110, 70, false, "Massive lightning");
        addAbility(60, "Thunder Fang", ElementType.STORM, 65, 95, true, "Electric bite");
        addAbility(61, "Thunder Punch", ElementType.STORM, 75, 100, true, "Electric punch");
        addAbility(62, "Volt Tackle", ElementType.STORM, 120, 100, true, "Powerful electric charge");
        addAbility(63, "Discharge", ElementType.STORM, 80, 100, false, "Electric discharge");
        addAbility(64, "Wild Charge", ElementType.STORM, 90, 100, true, "Reckless charge");
        addAbility(65, "Electro Ball", ElementType.STORM, 80, 100, false, "Speed-based electric ball");
        addAbility(66, "Gust", ElementType.STORM, 40, 100, false, "Wind gust");
        addAbility(67, "Air Slash", ElementType.STORM, 75, 95, false, "Blade of air");
        addAbility(68, "Hurricane", ElementType.STORM, 110, 70, false, "Fierce winds");
        addAbility(69, "Thunder Wave", ElementType.STORM, 0, 90, false, "Paralyzes target");
        addAbility(70, "Charge", ElementType.STORM, 0, 100, false, "Boosts electric power");
        
        // VOID MOVES (IDs 71-85)
        addAbility(71, "Shadow Ball", ElementType.VOID, 80, 100, false, "Shadow projectile");
        addAbility(72, "Dark Pulse", ElementType.VOID, 80, 100, false, "Dark energy wave");
        addAbility(73, "Night Slash", ElementType.VOID, 70, 100, true, "Dark blade");
        addAbility(74, "Crunch", ElementType.VOID, 80, 100, true, "Dark crushing bite");
        addAbility(75, "Foul Play", ElementType.VOID, 95, 100, true, "Uses foe's attack");
        addAbility(76, "Sucker Punch", ElementType.VOID, 70, 100, true, "Priority dark punch");
        addAbility(77, "Shadow Claw", ElementType.VOID, 70, 100, true, "Shadow slash");
        addAbility(78, "Dark Void", ElementType.VOID, 0, 50, false, "Puts to sleep");
        addAbility(79, "Nightmare", ElementType.VOID, 0, 100, false, "Damages sleeping foes");
        addAbility(80, "Void Storm", ElementType.VOID, 100, 90, false, "Shadow tempest");
        addAbility(81, "Darkness", ElementType.VOID, 90, 95, false, "Pure darkness");
        addAbility(82, "Abyss", ElementType.VOID, 110, 85, false, "Bottomless void");
        addAbility(83, "Shadow Sneak", ElementType.VOID, 40, 100, true, "Priority shadow");
        addAbility(84, "Black Hole", ElementType.VOID, 120, 80, false, "Gravity crush");
        addAbility(85, "Curse", ElementType.VOID, 0, 100, false, "Sacrifices HP");
        
        // RADIANT MOVES (IDs 86-100)
        addAbility(86, "Light Beam", ElementType.RADIANT, 90, 100, false, "Beam of light");
        addAbility(87, "Holy Ray", ElementType.RADIANT, 100, 95, false, "Sacred light");
        addAbility(88, "Flash Cannon", ElementType.RADIANT, 80, 100, false, "Light blast");
        addAbility(89, "Dazzling Gleam", ElementType.RADIANT, 80, 100, false, "Blinding light");
        addAbility(90, "Moonblast", ElementType.RADIANT, 95, 100, false, "Lunar power");
        addAbility(91, "Aura Sphere", ElementType.RADIANT, 80, 999, false, "Never misses");
        addAbility(92, "Radiance", ElementType.RADIANT, 110, 90, false, "Pure radiance");
        addAbility(93, "Light Screen", ElementType.RADIANT, 0, 100, false, "Raises special defense");
        addAbility(94, "Reflect", ElementType.RADIANT, 0, 100, false, "Raises defense");
        addAbility(95, "Heal Pulse", ElementType.RADIANT, 0, 100, false, "Heals target");
        addAbility(96, "Healing Wish", ElementType.RADIANT, 0, 100, false, "Sacrifice heal");
        addAbility(97, "Sacred Sword", ElementType.RADIANT, 90, 100, true, "Holy blade");
        addAbility(98, "Judgment", ElementType.RADIANT, 100, 100, false, "Divine judgment");
        addAbility(99, "Purify", ElementType.RADIANT, 70, 100, false, "Cleanses status");
        addAbility(100, "Blessing", ElementType.RADIANT, 0, 100, false, "Heals self");
        
        // MINERAL MOVES (IDs 101-115)
        addAbility(101, "Rock Throw", ElementType.MINERAL, 50, 90, true, "Throws rocks");
        addAbility(102, "Rock Slide", ElementType.MINERAL, 75, 90, true, "Sliding rocks");
        addAbility(103, "Stone Edge", ElementType.MINERAL, 100, 80, true, "Sharp stones");
        addAbility(104, "Rock Blast", ElementType.MINERAL, 25, 90, true, "Multiple rocks");
        addAbility(105, "Power Gem", ElementType.MINERAL, 80, 100, false, "Gem beam");
        addAbility(106, "Ancient Power", ElementType.MINERAL, 60, 100, false, "Prehistoric power");
        addAbility(107, "Earthquake", ElementType.MINERAL, 100, 100, true, "Ground shake");
        addAbility(108, "Earth Power", ElementType.MINERAL, 90, 100, false, "Earth energy");
        addAbility(109, "Dig", ElementType.MINERAL, 80, 100, true, "Underground attack");
        addAbility(110, "Bulldoze", ElementType.MINERAL, 60, 100, true, "Ground pound");
        addAbility(111, "Iron Head", ElementType.MINERAL, 80, 100, true, "Metal headbutt");
        addAbility(112, "Metal Claw", ElementType.MINERAL, 50, 95, true, "Steel claws");
        addAbility(113, "Steel Beam", ElementType.MINERAL, 140, 95, false, "Powerful metal beam");
        addAbility(114, "Harden", ElementType.MINERAL, 0, 100, false, "Raises defense");
        addAbility(115, "Iron Defense", ElementType.MINERAL, 0, 100, false, "Greatly raises defense");
        
        // MYSTIC MOVES (IDs 116-130)
        addAbility(116, "Confusion", ElementType.MYSTIC, 50, 100, false, "Psychic wave");
        addAbility(117, "Psybeam", ElementType.MYSTIC, 65, 100, false, "Psychic beam");
        addAbility(118, "Psychic", ElementType.MYSTIC, 90, 100, false, "Strong psychic power");
        addAbility(119, "Psyshock", ElementType.MYSTIC, 80, 100, false, "Psychic shock");
        addAbility(120, "Future Sight", ElementType.MYSTIC, 120, 100, false, "Delayed attack");
        addAbility(121, "Zen Headbutt", ElementType.MYSTIC, 80, 90, true, "Psychic headbutt");
        addAbility(122, "Psycho Cut", ElementType.MYSTIC, 70, 100, true, "Psychic blade");
        addAbility(123, "Mind Reader", ElementType.MYSTIC, 0, 100, false, "Next move hits");
        addAbility(124, "Telekinesis", ElementType.MYSTIC, 75, 100, false, "Lifts with mind");
        addAbility(125, "Dream Eater", ElementType.MYSTIC, 100, 100, false, "Drains sleeping foes");
        addAbility(126, "Hypnosis", ElementType.MYSTIC, 0, 60, false, "Puts to sleep");
        addAbility(127, "Cosmic Power", ElementType.MYSTIC, 0, 100, false, "Raises defenses");
        addAbility(128, "Calm Mind", ElementType.MYSTIC, 0, 100, false, "Raises special stats");
        addAbility(129, "Teleport", ElementType.MYSTIC, 0, 100, false, "Escape from battle");
        addAbility(130, "Miracle Eye", ElementType.MYSTIC, 0, 100, false, "Reveals weaknesses");
    }

    private static void addAbility(int id, String name, ElementType type, int power, 
                                    int accuracy, boolean isPhysical, String description) {
        abilities.put(id, new Ability(name, type, power, accuracy, isPhysical, description));
    }

    public static Ability getAbility(int id) {
        return abilities.get(id);
    }

    public static int getAbilityCount() {
        return abilities.size();
    }
}
