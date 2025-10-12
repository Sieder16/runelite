package net.runelite.client.plugins.skillingoutfit;

import java.util.LinkedHashMap;
import java.util.Map;

public class SkillingOutfitData
{
    public static final Map<String, SkillingOutfitDataEntry> OUTFITS_DATA = new LinkedHashMap<>();

    static
    {
        // Graceful Outfit
        Map<Integer, SkillingOutfitItem> graceful = new LinkedHashMap<>();
        graceful.put(11850, new SkillingOutfitItem(11850, "Graceful Hood", 35, "Marks of Grace", 11849));
        graceful.put(11854, new SkillingOutfitItem(11854, "Graceful Top", 55, "Marks of Grace", 11849));
        graceful.put(11856, new SkillingOutfitItem(11856, "Graceful Legs", 60, "Marks of Grace", 11849));
        graceful.put(11858, new SkillingOutfitItem(11858, "Graceful Gloves", 30, "Marks of Grace", 11849));
        graceful.put(11860, new SkillingOutfitItem(11860, "Graceful Boots", 40, "Marks of Grace", 11849));
        graceful.put(11852, new SkillingOutfitItem(11852, "Graceful Cape", 40, "Marks of Grace", 11849));
        OUTFITS_DATA.put("Agility - Graceful Outfit",
                new SkillingOutfitDataEntry("displayAgility", graceful, "https://oldschool.runescape.wiki/w/Graceful_outfit", "Agility"));

        // Carpenter's Outfit
        Map<Integer, SkillingOutfitItem> carpenter = new LinkedHashMap<>();
        carpenter.put(24872, new SkillingOutfitItem(24872, "Carpenter Helmet", 400, "Carpenter Points", 0));
        carpenter.put(24874, new SkillingOutfitItem(24874, "Carpenter Shirt", 800, "Carpenter Points", 0));
        carpenter.put(24876, new SkillingOutfitItem(24876, "Carpenter Trousers", 600, "Carpenter Points", 0));
        carpenter.put(24878, new SkillingOutfitItem(24878, "Carpenter Boots", 200, "Carpenter Points", 0));
        OUTFITS_DATA.put("Construction - Carpenter Outfit",
                new SkillingOutfitDataEntry("displayConstruction", carpenter, "https://oldschool.runescape.wiki/w/Carpenter's_outfit", "Construction"));

        // Farmer's Outfit A MALE
        Map<Integer, SkillingOutfitItem> farmera = new LinkedHashMap<>();
        farmera.put(13646, new SkillingOutfitItem(13646, "Farmer's Strawhat", 75, "Farming Points", 0));
        farmera.put(13642, new SkillingOutfitItem(13642, "Farmer's Jacket", 150, "Farming Points", 0));
        farmera.put(13640, new SkillingOutfitItem(13640, "Farmer's Boro Trousers", 125, "Farming Points", 0));
        farmera.put(13644, new SkillingOutfitItem(13644, "Farmer's Boots", 50, "Farming Points", 0));
        OUTFITS_DATA.put("Farming - Farmer's Outfit Male",
                new SkillingOutfitDataEntry("displayFarming", farmera, "https://oldschool.runescape.wiki/w/Farmer's_outfit#Body_Type_A", "Farming"));

        // Farmer's Outfit B FEMALE
        Map<Integer, SkillingOutfitItem> farmerb = new LinkedHashMap<>();
        farmerb.put(13647, new SkillingOutfitItem(13647, "Farmer's Strawhat", 75, "Farming Points", 0));
        farmerb.put(13643, new SkillingOutfitItem(13643, "Farmer's Shirt", 150, "Farming Points", 0));
        farmerb.put(13641, new SkillingOutfitItem(13641, "Farmer's Boro Trousers", 125, "Farming Points", 0));
        farmerb.put(13645, new SkillingOutfitItem(13645, "Farmer's Boots", 50, "Farming Points", 0));
        OUTFITS_DATA.put("Farming - Farmer's Outfit Female",
                new SkillingOutfitDataEntry("displayFarming", farmerb, "https://oldschool.runescape.wiki/w/Farmer's_outfit#Body_Type_B", "Farming"));

        // Pyromancer Outfit
        Map<Integer, SkillingOutfitItem> pyromancer = new LinkedHashMap<>();
        pyromancer.put(20708, new SkillingOutfitItem(20708, "Pyromancer Hood", 0, "Wintertodt - Reward Cart", 0));
        pyromancer.put(20704, new SkillingOutfitItem(20704, "Pyromancer Garb", 0, "Wintertodt - Reward Cart", 0));
        pyromancer.put(20706, new SkillingOutfitItem(20706, "Pyromancer Robe", 0, "Wintertodt - Reward Cart", 0));
        pyromancer.put(20710, new SkillingOutfitItem(20710, "Pyromancer Boots", 0, "Wintertodt - Reward Cart", 0));
        OUTFITS_DATA.put("Firemaking - Pyromancer Outfit",
                new SkillingOutfitDataEntry("displayFiremaking", pyromancer, "https://oldschool.runescape.wiki/w/Pyromancer_outfit", "Firemaking"));

        // Angler's Outfit
        Map<Integer, SkillingOutfitItem> angler = new LinkedHashMap<>();
        angler.put(13258, new SkillingOutfitItem(13258, "Angler Hat", 0, "Fishing Trawler - Reward Net", 0));
        angler.put(13259, new SkillingOutfitItem(13259, "Angler Top", 0, "Fishing Trawler - Reward Net", 0));
        angler.put(13260, new SkillingOutfitItem(13260, "Angler Waders", 0, "Fishing Trawler - Reward Net", 0));
        angler.put(13261, new SkillingOutfitItem(13261, "Angler Boots", 0, "Fishing Trawler - Reward Net", 0));
        OUTFITS_DATA.put("Fishing - Angler's Outfit",
                new SkillingOutfitDataEntry("displayFishing", angler, "https://oldschool.runescape.wiki/w/Angler's_outfit", "Fishing"));

        // Guild Hunter Outfit
        Map<Integer, SkillingOutfitItem> hunter = new LinkedHashMap<>();
        hunter.put(29263, new SkillingOutfitItem(29263, "Guild Hunter Headwear", 0, "Hunter Rumors", 0));
        hunter.put(29265, new SkillingOutfitItem(29265, "Guild Hunter Top", 0, "Hunter Rumors", 0));
        hunter.put(29267, new SkillingOutfitItem(29267, "Guild Hunter Legs", 0, "Hunter Rumors", 0));
        hunter.put(29269, new SkillingOutfitItem(29269, "Guild Hunter Boots", 0, "Hunter Rumors", 0));
        OUTFITS_DATA.put("Hunter - Guild Hunter Outfit",
                new SkillingOutfitDataEntry("displayHunter", hunter, "https://oldschool.runescape.wiki/w/Guild_hunter_outfit", "Hunter"));

        // Prospector Kit
        Map<Integer, SkillingOutfitItem> prospector = new LinkedHashMap<>();
        prospector.put(12013, new SkillingOutfitItem(12013, "Prospector Helmet", 40, "Gold Nuggets", 12012));
        prospector.put(12014, new SkillingOutfitItem(12014, "Prospector Jacket", 60, "Gold Nuggets", 12012));
        prospector.put(12015, new SkillingOutfitItem(12015, "Prospector Legs", 50, "Gold Nuggets", 12012));
        prospector.put(12010, new SkillingOutfitItem(12010, "Prospector Boots", 30, "Gold Nuggets", 12012)); // CHANGE ID BACK TO 12016, SET AS 12010 FOR TESTING AS I HAVE FULL SET
        OUTFITS_DATA.put("Mining - Prospector Kit",
                new SkillingOutfitDataEntry("displayMining", prospector, "https://oldschool.runescape.wiki/w/Prospector_kit", "Mining"));

        // Prayer Robes
        Map<Integer, SkillingOutfitItem> prayer = new LinkedHashMap<>();
        prayer.put(25438, new SkillingOutfitItem(25438, "Zealot's Helmet", 0, "Shade Catacombs - Gold Chest", 0));
        prayer.put(25434, new SkillingOutfitItem(25434, "Zealot's Robe Top", 0, "Shade Catacombs - Gold Chest", 0));
        prayer.put(25436, new SkillingOutfitItem(25436, "Zealot's Robe Bottom", 0, "Shade Catacombs - Gold Chest", 0));
        prayer.put(25440, new SkillingOutfitItem(25440, "Zealot's  Boots", 0, "Shade Catacombs - Gold Chest", 0));
        OUTFITS_DATA.put("Prayer - Zealot's Robes",
                new SkillingOutfitDataEntry("displayPrayer", prayer, "https://oldschool.runescape.wiki/w/Zealot's_robes", "Prayer"));

        // Runecraft Robes
        Map<Integer, SkillingOutfitItem> runecraft = new LinkedHashMap<>();
        runecraft.put(26850, new SkillingOutfitItem(26850, "Hat Of The Eye", 400, "Abyssal Pearls", 26792));
        runecraft.put(26854, new SkillingOutfitItem(26854, "Robe Top Of The Eye", 350, "Abyssal Pearls", 26792));
        runecraft.put(26852, new SkillingOutfitItem(26852, "Robe Bottoms Of The Eye", 350, "Abyssal Pearls", 26792));
        runecraft.put(26856, new SkillingOutfitItem(26856, "Boots Of The Eye", 250, "Abyssal Pearls", 26792));
        OUTFITS_DATA.put("Runecraft - Raiments Of The Eye",
                new SkillingOutfitDataEntry("displayRunecraft", runecraft, "https://oldschool.runescape.wiki/w/Raiments_of_the_Eye", "Runecraft"));

        // Smith's Uniform
        Map<Integer, SkillingOutfitItem> smithing = new LinkedHashMap<>();
        smithing.put(27023, new SkillingOutfitItem(27023, "Smiths Tunic", 4000, "Foundry Reputation", 0));
        smithing.put(27025, new SkillingOutfitItem(27025, "Smiths Trousers", 4000, "Foundry Reputation", 0));
        smithing.put(27029, new SkillingOutfitItem(27029, "Smiths Gloves", 3500, "Foundry Reputation", 0));
        smithing.put(27027, new SkillingOutfitItem(27027, "Smiths Boots", 3500, "Foundry Reputation", 0));
        OUTFITS_DATA.put("Smithing - Smith's Uniform",
                new SkillingOutfitDataEntry("displaySmithing", smithing, "https://oldschool.runescape.wiki/w/Smiths'_Uniform", "Smithing"));

        // Rogue Equipment
        Map<Integer, SkillingOutfitItem> Thieving = new LinkedHashMap<>();
        Thieving.put(5554, new SkillingOutfitItem(5554, "Rogue Mask", 0, "Rogues' Den - Wall Safes", 0));
        Thieving.put(5553, new SkillingOutfitItem(5553, "Rogue Top", 0, "Rogues' Den - Wall Safes", 0));
        Thieving.put(5556, new SkillingOutfitItem(5556, "Rogue Gloves", 0, "Rogues' Den - Wall Safes", 0));
        Thieving.put(5555, new SkillingOutfitItem(5555, "Rogue Trousers", 0, "Rogues' Den - Wall Safes", 0));
        Thieving.put(5557, new SkillingOutfitItem(5557, "Rogue Boots", 0, "Rogues' Den - Wall Safes", 0));
        OUTFITS_DATA.put("Thieving - Rogue Equipment",
                new SkillingOutfitDataEntry("displayThieving", Thieving, "https://oldschool.runescape.wiki/w/Rogue_equipment", "Thieving"));

        // Lumberjack Outfit
        Map<Integer, SkillingOutfitItem> woodcutting = new LinkedHashMap<>();
        woodcutting.put(10941, new SkillingOutfitItem(10941, "Lumberjack Hat", 1200, "Anima-Infused Bark", 28134));
        woodcutting.put(10939, new SkillingOutfitItem(10939, "Lumberjack Top", 1500, "Anima-Infused Bark", 28134));
        woodcutting.put(10940, new SkillingOutfitItem(10940, "Lumberjack Legs", 1300, "Anima-Infused Bark", 28134));
        woodcutting.put(10933, new SkillingOutfitItem(10933, "Lumberjack Boots", 1000, "Anima-Infused Bark", 28134));
        OUTFITS_DATA.put("Woodcutting - Lumberjack Outfit",
                new SkillingOutfitDataEntry("displayWoodcutting", woodcutting, "https://oldschool.runescape.wiki/w/Lumberjack_outfit", "Woodcutting"));

    }

    // Inner class
    public static class SkillingOutfitDataEntry
    {
        public final String configKey;
        public final Map<Integer, SkillingOutfitItem> items;
        public final String wikiUrl;
        public final String primarySkill;

        public SkillingOutfitDataEntry(String configKey, Map<Integer, SkillingOutfitItem> items, String wikiUrl, String primarySkill)
        {
            this.configKey = configKey;
            this.items = items;
            this.wikiUrl = wikiUrl;
            this.primarySkill = primarySkill;
        }
    }
}
