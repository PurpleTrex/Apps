package com.purpletrex.aether.data;

import com.purpletrex.aether.entities.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Database containing all 151 Ethereal species
 */
public class EtherealDatabase {
    private static final Map<Integer, EtherealSpecies> species = new HashMap<>();

    static {
        initializeDatabase();
    }

    private static void initializeDatabase() {
        // STARTERS (IDs 1-3)
        addSpecies(1, "Sproutling", ElementType.NATURE, new BaseStats(45, 49, 49, 65, 65, 45));
        addSpecies(2, "Emberlynx", ElementType.FLAME, new BaseStats(39, 52, 43, 60, 50, 65));
        addSpecies(3, "Tidepup", ElementType.AQUA, new BaseStats(44, 48, 65, 50, 64, 43));
        
        // STARTER EVOLUTIONS (IDs 4-9)
        addSpecies(4, "Vinewarden", ElementType.NATURE, new BaseStats(60, 62, 63, 80, 80, 60));
        addSpecies(5, "Floramancer", ElementType.NATURE, new BaseStats(80, 82, 83, 100, 100, 80));
        addSpecies(6, "Blazeclaw", ElementType.FLAME, new BaseStats(58, 64, 58, 80, 65, 80));
        addSpecies(7, "Infernotiger", ElementType.FLAME, new BaseStats(78, 84, 78, 109, 85, 100));
        addSpecies(8, "Wavehound", ElementType.AQUA, new BaseStats(59, 63, 80, 65, 80, 58));
        addSpecies(9, "Tsunamicanine", ElementType.AQUA, new BaseStats(79, 83, 100, 85, 105, 78));
        
        // COMMON ETHEREALS - ALL 151 SPECIES
        // Nature types (10-24)
        addSpecies(10, "Leafbug", ElementType.NATURE, new BaseStats(40, 30, 35, 20, 20, 50));
        addSpecies(11, "Vinecrawler", ElementType.NATURE, new BaseStats(50, 45, 55, 25, 25, 30));
        addSpecies(12, "Mossmoth", ElementType.NATURE, new BaseStats(60, 45, 50, 90, 80, 70));
        addSpecies(13, "Seedling", ElementType.NATURE, new BaseStats(50, 45, 55, 55, 65, 30));
        addSpecies(14, "Bloombeast", ElementType.NATURE, new BaseStats(75, 70, 85, 85, 95, 45));
        addSpecies(15, "Thornguard", ElementType.NATURE, new BaseStats(65, 90, 140, 45, 60, 35));
        addSpecies(16, "Fernfox", ElementType.NATURE, new BaseStats(55, 55, 40, 50, 40, 75));
        addSpecies(17, "Forestlord", ElementType.NATURE, new BaseStats(85, 85, 70, 80, 70, 105));
        addSpecies(18, "Petalwing", ElementType.NATURE, new BaseStats(45, 35, 40, 65, 55, 85));
        addSpecies(19, "Vinewhip", ElementType.NATURE, new BaseStats(70, 95, 60, 55, 50, 90));
        addSpecies(20, "Treebeast", ElementType.NATURE, new BaseStats(80, 90, 95, 75, 80, 60));
        addSpecies(21, "Grasswolf", ElementType.NATURE, new BaseStats(70, 85, 70, 70, 65, 95));
        addSpecies(22, "Floracat", ElementType.NATURE, new BaseStats(65, 75, 65, 80, 70, 90));
        addSpecies(23, "Plantdrake", ElementType.NATURE, new BaseStats(85, 95, 90, 90, 85, 75));
        addSpecies(24, "Mossgolem", ElementType.NATURE, new BaseStats(90, 85, 120, 60, 90, 40));
        
        // Flame types (25-39)
        addSpecies(25, "Emberant", ElementType.FLAME, new BaseStats(35, 40, 30, 50, 30, 55));
        addSpecies(26, "Flamecrawler", ElementType.FLAME, new BaseStats(50, 60, 50, 70, 50, 70));
        addSpecies(27, "Cinderpup", ElementType.FLAME, new BaseStats(45, 60, 40, 50, 40, 65));
        addSpecies(28, "Blazehound", ElementType.FLAME, new BaseStats(70, 95, 70, 75, 70, 95));
        addSpecies(29, "Flamewing", ElementType.FLAME, new BaseStats(50, 55, 45, 75, 55, 90));
        addSpecies(30, "Infernowl", ElementType.FLAME, new BaseStats(80, 75, 70, 115, 85, 105));
        addSpecies(31, "Lavafox", ElementType.FLAME, new BaseStats(55, 70, 55, 75, 60, 85));
        addSpecies(32, "Pyrodrake", ElementType.FLAME, new BaseStats(80, 100, 80, 95, 80, 90));
        addSpecies(33, "Scorchbeetle", ElementType.FLAME, new BaseStats(60, 85, 100, 55, 70, 40));
        addSpecies(34, "Magmabeast", ElementType.FLAME, new BaseStats(90, 110, 90, 85, 80, 65));
        addSpecies(35, "Ashwolf", ElementType.FLAME, new BaseStats(75, 95, 75, 80, 70, 90));
        addSpecies(36, "Burningcat", ElementType.FLAME, new BaseStats(70, 90, 70, 85, 75, 95));
        addSpecies(37, "Firedrake", ElementType.FLAME, new BaseStats(85, 105, 85, 95, 85, 85));
        addSpecies(38, "Volcanogolem", ElementType.FLAME, new BaseStats(95, 110, 110, 75, 85, 50));
        addSpecies(39, "Infernotitan", ElementType.FLAME, new BaseStats(100, 120, 95, 90, 85, 70));
        
        // Aqua types (40-54)
        addSpecies(40, "Puddlefrog", ElementType.AQUA, new BaseStats(40, 40, 40, 50, 40, 50));
        addSpecies(41, "Tidalfrog", ElementType.AQUA, new BaseStats(65, 65, 65, 85, 70, 75));
        addSpecies(42, "Streamfish", ElementType.AQUA, new BaseStats(35, 45, 40, 50, 40, 70));
        addSpecies(43, "Rapidcarp", ElementType.AQUA, new BaseStats(50, 70, 60, 70, 60, 105));
        addSpecies(44, "Coralsnake", ElementType.AQUA, new BaseStats(55, 60, 90, 65, 85, 45));
        addSpecies(45, "Reefdragon", ElementType.AQUA, new BaseStats(85, 90, 120, 95, 115, 65));
        addSpecies(46, "Bubbleshrimp", ElementType.AQUA, new BaseStats(45, 50, 70, 60, 65, 60));
        addSpecies(47, "Foamcrab", ElementType.AQUA, new BaseStats(70, 80, 120, 75, 90, 45));
        addSpecies(48, "Mistwhale", ElementType.AQUA, new BaseStats(110, 75, 95, 85, 100, 55));
        addSpecies(49, "Crystalfish", ElementType.AQUA, new BaseStats(50, 45, 60, 95, 80, 90));
        addSpecies(50, "Wavewolf", ElementType.AQUA, new BaseStats(75, 85, 85, 80, 85, 80));
        addSpecies(51, "Watercat", ElementType.AQUA, new BaseStats(70, 80, 80, 90, 90, 85));
        addSpecies(52, "Oceandrake", ElementType.AQUA, new BaseStats(90, 95, 105, 100, 105, 75));
        addSpecies(53, "Icegolem", ElementType.AQUA, new BaseStats(95, 90, 115, 80, 100, 55));
        addSpecies(54, "Glacialtitan", ElementType.AQUA, new BaseStats(105, 95, 120, 95, 110, 65));
        
        // Storm types (55-69)
        addSpecies(55, "Sparkwing", ElementType.STORM, new BaseStats(40, 45, 40, 50, 40, 75));
        addSpecies(56, "Thunderhawk", ElementType.STORM, new BaseStats(70, 75, 70, 90, 70, 115));
        addSpecies(57, "Buzzbird", ElementType.STORM, new BaseStats(45, 50, 45, 55, 50, 85));
        addSpecies(58, "Voltfalcon", ElementType.STORM, new BaseStats(75, 85, 75, 95, 85, 125));
        addSpecies(59, "Cloudcat", ElementType.STORM, new BaseStats(55, 60, 50, 70, 60, 95));
        addSpecies(60, "Stormtiger", ElementType.STORM, new BaseStats(85, 95, 75, 105, 85, 125));
        addSpecies(61, "Staticmouse", ElementType.STORM, new BaseStats(35, 30, 25, 45, 35, 80));
        addSpecies(62, "Voltrat", ElementType.STORM, new BaseStats(55, 50, 45, 75, 60, 115));
        addSpecies(63, "Windrider", ElementType.STORM, new BaseStats(65, 70, 60, 80, 70, 110));
        addSpecies(64, "Galewing", ElementType.STORM, new BaseStats(95, 100, 85, 115, 95, 140));
        addSpecies(65, "Lightwolf", ElementType.STORM, new BaseStats(80, 90, 75, 95, 80, 120));
        addSpecies(66, "Thundercat", ElementType.STORM, new BaseStats(75, 85, 70, 100, 85, 125));
        addSpecies(67, "Stormdrake", ElementType.STORM, new BaseStats(90, 100, 85, 110, 90, 130));
        addSpecies(68, "Cloudgolem", ElementType.STORM, new BaseStats(85, 95, 95, 105, 95, 105));
        addSpecies(69, "Tempesttitan", ElementType.STORM, new BaseStats(95, 105, 90, 120, 100, 135));
        
        // Void types (70-84)
        addSpecies(70, "Shadowpup", ElementType.VOID, new BaseStats(45, 50, 45, 60, 50, 60));
        addSpecies(71, "Umbrawolf", ElementType.VOID, new BaseStats(75, 85, 75, 100, 85, 95));
        addSpecies(72, "Darkwing", ElementType.VOID, new BaseStats(50, 55, 50, 70, 60, 85));
        addSpecies(73, "Nightraven", ElementType.VOID, new BaseStats(80, 85, 80, 110, 95, 115));
        addSpecies(74, "Shadowclaw", ElementType.VOID, new BaseStats(60, 75, 60, 65, 60, 90));
        addSpecies(75, "Voidfang", ElementType.VOID, new BaseStats(90, 110, 90, 95, 90, 120));
        addSpecies(76, "Duskbat", ElementType.VOID, new BaseStats(40, 45, 35, 55, 45, 80));
        addSpecies(77, "Eclipsewing", ElementType.VOID, new BaseStats(70, 75, 65, 95, 80, 115));
        addSpecies(78, "Gloomspider", ElementType.VOID, new BaseStats(55, 70, 80, 60, 75, 45));
        addSpecies(79, "Abyssarachnid", ElementType.VOID, new BaseStats(85, 105, 115, 90, 105, 70));
        addSpecies(80, "Darkwolf", ElementType.VOID, new BaseStats(80, 100, 85, 90, 85, 105));
        addSpecies(81, "Shadowcat", ElementType.VOID, new BaseStats(75, 95, 80, 100, 90, 110));
        addSpecies(82, "Voiddrake", ElementType.VOID, new BaseStats(90, 110, 95, 105, 95, 115));
        addSpecies(83, "Darkgolem", ElementType.VOID, new BaseStats(95, 115, 110, 85, 95, 75));
        addSpecies(84, "Abysstitin", ElementType.VOID, new BaseStats(100, 125, 105, 100, 100, 95));
        
        // Radiant types (85-99)
        addSpecies(85, "Glimmerfly", ElementType.RADIANT, new BaseStats(40, 35, 35, 55, 45, 75));
        addSpecies(86, "Lightweaver", ElementType.RADIANT, new BaseStats(70, 60, 60, 95, 85, 110));
        addSpecies(87, "Glowpup", ElementType.RADIANT, new BaseStats(50, 45, 50, 65, 60, 65));
        addSpecies(88, "Radiantwolf", ElementType.RADIANT, new BaseStats(80, 75, 85, 105, 100, 95));
        addSpecies(89, "Shinewing", ElementType.RADIANT, new BaseStats(45, 40, 45, 70, 60, 85));
        addSpecies(90, "Dawnbird", ElementType.RADIANT, new BaseStats(75, 70, 75, 115, 100, 120));
        addSpecies(91, "Prismfly", ElementType.RADIANT, new BaseStats(50, 50, 50, 80, 75, 90));
        addSpecies(92, "Auracat", ElementType.RADIANT, new BaseStats(60, 60, 65, 85, 80, 90));
        addSpecies(93, "Lumifox", ElementType.RADIANT, new BaseStats(70, 65, 70, 95, 90, 100));
        addSpecies(94, "Holyhound", ElementType.RADIANT, new BaseStats(95, 85, 95, 115, 110, 105));
        addSpecies(95, "Lightwolf", ElementType.RADIANT, new BaseStats(85, 80, 90, 110, 105, 100));
        addSpecies(96, "Shinecat", ElementType.RADIANT, new BaseStats(80, 75, 85, 115, 110, 105));
        addSpecies(97, "Radiancedrake", ElementType.RADIANT, new BaseStats(95, 90, 100, 125, 120, 110));
        addSpecies(98, "Crystalgolem", ElementType.RADIANT, new BaseStats(100, 95, 110, 105, 115, 85));
        addSpecies(99, "Celestialtitan", ElementType.RADIANT, new BaseStats(110, 100, 115, 130, 125, 100));
        
        // Mineral types (100-114)
        addSpecies(100, "Rockshell", ElementType.MINERAL, new BaseStats(55, 45, 90, 30, 50, 20));
        addSpecies(101, "Boulderfortress", ElementType.MINERAL, new BaseStats(90, 75, 150, 50, 80, 30));
        addSpecies(102, "Stonerat", ElementType.MINERAL, new BaseStats(40, 55, 50, 35, 40, 60));
        addSpecies(103, "Granitemole", ElementType.MINERAL, new BaseStats(65, 85, 80, 50, 60, 80));
        addSpecies(104, "Crystalspike", ElementType.MINERAL, new BaseStats(50, 60, 85, 60, 75, 40));
        addSpecies(105, "Diamondbeast", ElementType.MINERAL, new BaseStats(80, 95, 125, 90, 115, 60));
        addSpecies(106, "Ironwing", ElementType.MINERAL, new BaseStats(55, 70, 90, 50, 60, 75));
        addSpecies(107, "Steelfalcon", ElementType.MINERAL, new BaseStats(85, 105, 130, 75, 90, 105));
        addSpecies(108, "Sandfox", ElementType.MINERAL, new BaseStats(50, 65, 60, 55, 55, 85));
        addSpecies(109, "Quartzwolf", ElementType.MINERAL, new BaseStats(80, 95, 90, 85, 85, 115));
        addSpecies(110, "Stonewolf", ElementType.MINERAL, new BaseStats(85, 100, 110, 70, 80, 85));
        addSpecies(111, "Ironcat", ElementType.MINERAL, new BaseStats(80, 95, 105, 75, 85, 90));
        addSpecies(112, "Mineraldrake", ElementType.MINERAL, new BaseStats(95, 115, 135, 85, 100, 80));
        addSpecies(113, "Steelgolem", ElementType.MINERAL, new BaseStats(105, 120, 155, 70, 95, 55));
        addSpecies(114, "Titantitan", ElementType.MINERAL, new BaseStats(115, 135, 170, 80, 105, 50));
        
        // Mystic types (115-129)
        addSpecies(115, "Mindmouse", ElementType.MYSTIC, new BaseStats(35, 25, 30, 60, 50, 70));
        addSpecies(116, "Psycherat", ElementType.MYSTIC, new BaseStats(60, 45, 55, 100, 85, 105));
        addSpecies(117, "Dreamwing", ElementType.MYSTIC, new BaseStats(50, 40, 45, 75, 65, 90));
        addSpecies(118, "Visionhawk", ElementType.MYSTIC, new BaseStats(80, 65, 75, 115, 105, 125));
        addSpecies(119, "Astralcat", ElementType.MYSTIC, new BaseStats(55, 50, 55, 85, 75, 95));
        addSpecies(120, "Cosmictiger", ElementType.MYSTIC, new BaseStats(85, 80, 85, 125, 115, 130));
        addSpecies(121, "Soulfox", ElementType.MYSTIC, new BaseStats(60, 55, 60, 90, 80, 100));
        addSpecies(122, "Spiritdrake", ElementType.MYSTIC, new BaseStats(90, 85, 90, 130, 120, 135));
        addSpecies(123, "Telekineticpup", ElementType.MYSTIC, new BaseStats(45, 40, 45, 70, 60, 80));
        addSpecies(124, "Mindhound", ElementType.MYSTIC, new BaseStats(75, 70, 75, 110, 100, 115));
        addSpecies(125, "Psychicwolf", ElementType.MYSTIC, new BaseStats(85, 80, 85, 120, 110, 120));
        addSpecies(126, "Astralcat", ElementType.MYSTIC, new BaseStats(80, 75, 80, 125, 115, 125));
        addSpecies(127, "Minddrake", ElementType.MYSTIC, new BaseStats(95, 90, 95, 140, 130, 140));
        addSpecies(128, "Psychicgolem", ElementType.MYSTIC, new BaseStats(90, 85, 90, 135, 125, 125));
        addSpecies(129, "Cosmictitan", ElementType.MYSTIC, new BaseStats(100, 95, 100, 150, 140, 145));
        
        // RARE ETHEREALS (130-143)
        addSpecies(130, "Ancienttree", ElementType.NATURE, new BaseStats(95, 100, 100, 95, 100, 70));
        addSpecies(131, "Phoenixflame", ElementType.FLAME, new BaseStats(90, 110, 90, 105, 90, 100));
        addSpecies(132, "Leviathan", ElementType.AQUA, new BaseStats(100, 95, 110, 105, 115, 75));
        addSpecies(133, "Thunderlord", ElementType.STORM, new BaseStats(90, 100, 90, 110, 95, 125));
        addSpecies(134, "Voidlord", ElementType.VOID, new BaseStats(95, 115, 100, 105, 100, 110));
        addSpecies(135, "Angelicbeast", ElementType.RADIANT, new BaseStats(95, 95, 100, 115, 110, 110));
        addSpecies(136, "Titangolem", ElementType.MINERAL, new BaseStats(105, 115, 145, 80, 100, 55));
        addSpecies(137, "Oraclebeast", ElementType.MYSTIC, new BaseStats(90, 85, 90, 130, 120, 130));
        addSpecies(138, "Primalwolf", ElementType.NATURE, new BaseStats(90, 105, 95, 95, 95, 105));
        addSpecies(139, "Primalbeast", ElementType.FLAME, new BaseStats(95, 120, 95, 100, 95, 95));
        addSpecies(140, "Alphadragon", ElementType.AQUA, new BaseStats(100, 105, 115, 110, 120, 85));
        addSpecies(141, "Megabeast", ElementType.STORM, new BaseStats(95, 110, 95, 115, 100, 130));
        addSpecies(142, "Ultrabeast", ElementType.VOID, new BaseStats(100, 125, 105, 110, 105, 115));
        addSpecies(143, "Omegabeast", ElementType.RADIANT, new BaseStats(100, 100, 105, 120, 115, 115));
        
        // LEGENDARY ETHEREALS (144-151)
        addSpecies(144, "Sylvanus", ElementType.NATURE, new BaseStats(106, 110, 110, 110, 110, 90));
        addSpecies(145, "Pyrothorn", ElementType.FLAME, new BaseStats(106, 130, 90, 125, 90, 100));
        addSpecies(146, "Glacialis", ElementType.AQUA, new BaseStats(106, 100, 120, 110, 125, 85));
        addSpecies(147, "Stormrend", ElementType.STORM, new BaseStats(106, 110, 90, 125, 100, 115));
        addSpecies(148, "Voidheart", ElementType.VOID, new BaseStats(106, 125, 105, 120, 105, 95));
        addSpecies(149, "Luxendor", ElementType.RADIANT, new BaseStats(106, 100, 110, 130, 120, 100));
        addSpecies(150, "Terragol", ElementType.MINERAL, new BaseStats(106, 135, 160, 80, 110, 50));
        addSpecies(151, "Mindweaver", ElementType.MYSTIC, new BaseStats(106, 90, 100, 145, 135, 120));
    }

    private static void addSpecies(int id, String name, ElementType type, BaseStats baseStats) {
        species.put(id, new EtherealSpecies(id, name, type, baseStats));
    }

    public static EtherealSpecies getSpecies(int id) {
        return species.get(id);
    }

    public static Ethereal createEthereal(int speciesId, int level) {
        EtherealSpecies spec = getSpecies(speciesId);
        if (spec == null) return null;
        return new Ethereal(spec.getId(), spec.getName(), spec.getType(), spec.getBaseStats(), level);
    }

    public static int getSpeciesCount() {
        return species.size();
    }
}
