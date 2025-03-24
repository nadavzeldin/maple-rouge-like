function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == -1) {
        cm.sendNext("Enjoy your adventure in ZeldaMS! Return if you need guidance.");
        cm.dispose();
        return;
    } else {
        if (status == 0 && mode == 0) {
            cm.sendNext("Enjoy your adventure in ZeldaMS! Return if you need guidance.");
            cm.dispose();
            return;
        }
        if (mode == 1) {
            status++;
        } else {
            status--;
        }

        if (status == 0) {
            // Create simplified formatted sections with only #b and #k
            var message = "#b[Welcome to ZeldaMS]#k\r\n\r\n" +
                "This server have few unique features and theme is rouge-like\r\n" +
                "if you die your character is locked in jail.\r\n" +
                "You get #brandom job advancement#k each 10 levels. The job will be in same tier as you are (first job ..)\r\n" +
                "There are ascension once reaching level 200 and much more, here is the indepth features:\r\n\r\n" +

                // Client Features
                "#bClient Improvements:#k\r\n" +
                "* No class restrictions on equipment\r\n" +
                "* Cast skills while moving\r\n" +
                "* PIC with keyboard support\r\n" +
                "* Fast rope climbing\r\n" +
                "* Mages can cast while jumping\r\n\r\n" +

                // Server Rates
                "#bServer Rates:#k\r\n" +
                "* EXP: 50x\r\n" +
                "* Meso: 10x\r\n" +
                "* Drop: 50x\r\n" +
                "* Boss Drop: 10x\r\n" +
                "* Quest: 5x\r\n" +
                "* PQ Bonus: 2x\r\n" +
                "* Spawn: 2x (excluding boss maps)\r\n" +
                "* 3 Channels (channel 3 is buffed, mobs x5 HP and EXP)\r\n\r\n" +

                // Unique Gameplay
                "#bUnique Gameplay:#k\r\n" +
                "* Random Job System: Every 10 levels you'll get a random job\r\n" +
                "* Permadeath: If you die, you're locked in jail forever\r\n" +
                "* Loot Candles: Spawn randomly for special drops\r\n" +
                "* Skill Changes: Enhanced beginner skills\r\n" +
                "* Doom Dungeon: Special dungeon where permadeath doesn't apply\r\n\r\n" +

                // Commands Section
                "#bUseful Commands:#k\r\n" +
                "* @goto - Enhanced teleportation (@goto fm, bosses, etc)\r\n" +
                "* @randommap - Teleport to a random (potentially dangerous) map\r\n" +
                "* @resetap - Reset your AP points\r\n" +
                "* @buffme - Apply all available buffs\r\n" +
                "* @doom - Enter the Doom dungeon\r\n" +
                "* @boosted - Find and teleport to boosted maps\r\n" +
                "* @resources - Manage scrolls, ores and merge coins\r\n" +
                "* @skillbind - Bind previously learned skills\r\n" +
                "* @help - See all ~50 available commands\r\n\r\n" +

                // Special Systems
                "#bSpecial Systems:#k\r\n" +
                "* Merge System: Combine duplicate items for enhanced stats\r\n" +
                "* Ascension: Level 200 rebirth with powerful benefits\r\n" +
                "* Premium Gacha: 10M for unique rewards\r\n\r\n" +

                // Other Features
                "#bOther Features:#k\r\n" +
                "* Everlasting buffs\r\n" +
                "* Death notifications\r\n" +
                "* Increased item slots\r\n" +
                "* Achievements with rewards\r\n" +
                "* Level milestone gifts\r\n" +
                "* Solo expeditions\r\n" +
                "* HD client support\r\n" +

                "\r\n#bAre you ready to begin your adventure? yes will teleport to lith harbor#k";

            cm.sendYesNo(message);
        } else if (status == 1) {
            cm.warp(104000000, 0); // Warp to Henesys
            cm.dispose();
        }
    }
}