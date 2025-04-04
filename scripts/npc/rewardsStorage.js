/**
 * Level Rewards Collection System
 * Allows players to collect level-up rewards
 */

// Debug function - logs to server console
function log(message) {
    java.lang.System.out.println("[RewardsStorage Debug] " + message);
}

// Try to load required types
try {
    log("Loading required Java types...");
    const ResourceStorageType = Java.type('server.ResourceStorage');
    const InventoryType = Java.type('client.inventory.InventoryType');
    const InventoryManipulator = Java.type('server.InventoryManipulator');
    const ItemConstants = Java.type('client.inventory.Item.ItemConstants');
    const PacketCreator = Java.type('tools.PacketCreator');
    const ModifyInventory = Java.type('tools.ModifyInventory');
    const Collections = Java.type('java.util.Collections');
    log("Successfully loaded Java types");
} catch (e) {
    log("Error loading Java types: " + e);
}

var header = "#r#eLevel Rewards Collection#k#n\r\n\r\n";
var status = -1;
var selectedLevel = 0;

// Constants
const Level_Up_Banner = 5120000; // Banner ID

function start() {
    log("Script started");
    status = -1;
    action(1, 0, 0);
}

function itemStr(id) {
    return ("#v" + id + "# #t" + id + "#");
}

function getLevelRewards() {
    log("Getting level rewards mapping");
    var levelToItem = {};
    // level 20-80 rings, level 100 belt, level 120 eye, level 140 ear, level 160 pendent, level 180 forehead
    levelToItem[20] = 1112300;
    levelToItem[40] = 1112408;
    levelToItem[60] = 1112405;
    levelToItem[80] = 1112401;
    levelToItem[90] = 1132009;
    levelToItem[100] = 1102166;
    levelToItem[120] = 1022082;
    levelToItem[140] = 1032033;
    levelToItem[160] = 1122003;
    levelToItem[180] = 1012070;

    return levelToItem;
}

function applyItemStats(item, level) {
    log("Applying custom stats to item for level: " + level);
    try {
        item.setWatk(10);
        item.setMatk(10);
        item.setStr(10);
        item.setDex(10);
        item.setInt(10);
        item.setLuk(10);
        item.setSpeed(5);
        item.setJump(5);
        log("Successfully applied stats to item");
    } catch (e) {
        log("Error applying stats to item: " + e);
    }

    return item;
}

function action(mode, type, selection) {
    log("Action called: mode=" + mode + ", type=" + type + ", selection=" + selection + ", status=" + status);

    if (mode == -1) {
        log("Mode -1, disposing");
        cm.dispose();
        return;
    } else if (mode == 0) {
        status--;
        log("Mode 0, decreasing status to " + status);
    } else {
        status++;
        log("Mode 1, increasing status to " + status);
    }

    var textList = [];
    textList.push(header);

    try {
        log("Getting player");
        var player = cm.getPlayer();
        var level = player.getLevel();
        log("Player level: " + level);

        // Initialize reward status if needed
        log("Initializing reward status");
        try {
            player.initializeRewardStatus();
            log("Reward status initialized");
        } catch (e) {
            log("Error initializing reward status: " + e);
            cm.sendOk("There was an error initializing rewards. Please report this to an administrator.");
            cm.dispose();
            return;
        }

        if (status == 0) { // Main menu - show available rewards
            log("Status 0: Displaying main menu");
            var levelRewards = getLevelRewards();
            var hasEligibleRewards = false;

            textList.push("Welcome! As you level up, you unlock special equipment rewards.\r\n");
            textList.push("The following rewards are available for collection:\r\n\r\n");
            textList.push("#b");

            // Loop through all rewards and check eligibility
            for (var rewardLevel in levelRewards) {
                // Parse rewardLevel as integer since JavaScript object keys are strings
                var intLevel = parseInt(rewardLevel);

                log("Checking reward for level " + intLevel + ", player level: " + level);
                // Check if player level is sufficient and reward hasn't been claimed
                var canTake = false;
                try {
                    canTake = player.canTakeReward(intLevel);
                    log("Can take reward for level " + intLevel + ": " + canTake);
                } catch (e) {
                    log("Error checking if can take reward: " + e);
                }

                if (level >= intLevel && canTake) {
                    log("Adding eligible reward for level " + intLevel);
                    textList.push("#L" + intLevel + "#Level " + intLevel + " Reward: " + itemStr(levelRewards[intLevel]) + "#l\r\n");
                    hasEligibleRewards = true;
                }
            }

            if (!hasEligibleRewards) {
                log("No eligible rewards found");
                textList.push("#kYou don't have any rewards available for collection at this time.\r\n");
                textList.push("Keep leveling up to unlock more rewards!");
                cm.sendOk(textList.join(""));
                cm.dispose();
                return;
            }

            log("Sending menu with eligible rewards");
            cm.sendSimple(textList.join(""));
        } else if (status == 1) { // Process reward selection
            log("Status 1: Processing reward selection: " + selection);
            selectedLevel = selection;
            log("Selected level: " + selectedLevel);

            var levelRewards = getLevelRewards();
            var itemId = levelRewards[selectedLevel];
            log("Item ID for selected level: " + itemId);

            var canTake = false;
            try {
                canTake = player.canTakeReward(selectedLevel);
                log("Can take selected reward: " + canTake);
            } catch (e) {
                log("Error checking if can take selected reward: " + e);
            }

            if (!canTake) {
                log("Reward already claimed");
                textList.push("You have already claimed this reward!");
                cm.sendOk(textList.join(""));
                cm.dispose();
                return;
            }

            // Check if player has enough inventory space
            log("Checking inventory space");
            if (!cm.canHold(itemId)) {
                log("Not enough inventory space");
                textList.push("Please make room in your Equipment inventory to receive this reward!");
                cm.sendOk(textList.join(""));
                cm.dispose();
                return;
            }

            // Add the item to player's inventory
            log("Adding item to inventory: " + itemId);
            try {
                // Use cm's built-in method instead of direct InventoryManipulator access
                player.itemReward(selectedLevel, false);
                log("Item added successfully");
            } catch (e) {
                log("Error adding item to inventory: " + e);
                textList.push("There was an error giving you the item. Please report this to an administrator.");
                cm.sendOk(textList.join(""));
                cm.dispose();
                return;
            }

            // Mark reward as claimed
            log("Marking reward as claimed");
            try {
                player.setRewardStatus(selectedLevel);
                log("Reward marked as claimed");
            } catch (e) {
                log("Error marking reward as claimed: " + e);
            }

            // Display success message
            textList.push("You have successfully claimed your Level " + selectedLevel + " reward: " + itemStr(itemId) + "!");
            textList.push("\r\n\r\nThis powerful item has been customized based on your level.");

            // Display map effect
            log("Displaying map effect");
            try {
                var map_ = player.getMap();
                map_.startMapEffect("Congratulations! You've claimed your level " + selectedLevel + " reward!", Level_Up_Banner);
                log("Map effect displayed");
            } catch (e) {
                log("Error displaying map effect: " + e);
            }

            log("Sending final success message");
            cm.sendOk(textList.join(""));
            cm.dispose();
        } else {
            log("Invalid status: " + status + ", disposing");
            cm.dispose();
        }
    } catch (e) {
        log("Unexpected error in action: " + e);
        cm.sendOk("An unexpected error occurred. Please report this to an administrator.");
        cm.dispose();
    }
}