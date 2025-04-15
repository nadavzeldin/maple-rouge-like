/* 
 * NPC: Nana (Henesys)
 * ID: 9010000
 * Daily Scroll Quest
 */

var status = 0;
var monsterList = [100100, 100101, 130101, 210100, 1210100, 9300002]; // Snail, Blue Snail, Shroom, Stump, Red Snail, Green Mushroom
var requiredKills = 100;
var rewardItemId = 2049115; // Chaos Scroll

// Hardcoded monster name mapping
var monsterNames = {
    100100: "Snail",
    100101: "Blue Snail",
    130101: "Shroom",
    210100: "Stump",
    1210100: "Red Snail",
};

function getTodayDate() {
    var date = new Date();
    var year = date.getFullYear();
    var month = (date.getMonth() + 1).toString().padStart(2, '0');
    var day = date.getDate().toString().padStart(2, '0');
    return year + "-" + month + "-" + day;
}

function getRandomMonsterId() {
    return monsterList[Math.floor(Math.random() * monsterList.length)];
}

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == -1) {
        cm.dispose();
        return;
    }
    if (mode == 0 && status == 0) {
        cm.dispose();
        return;
    }
    if (mode == 1) {
        status++;
    } else {
        status--;
    }

    var player = cm.getPlayer();
    var extraDetails = player.getAccountExtraDetails();
    if (extraDetails == null) {
        cm.sendOk("Error: Unable to load account details. Please try again later.");
        cm.dispose();
        return;
    }
    var dq = extraDetails.getDailyQuest();
    var today = getTodayDate();

    if (status == 0) {
        // Initialize or reset quest if needed
        if (dq == null || dq.getDate() !== today) {
            dq = new Packages.client.DailyQuest();
            dq.setDate(today);
            dq.setMonsterId(getRandomMonsterId());
            dq.setKillCount(0);
            dq.setCompleted(false);
            extraDetails.setDailyQuest(dq);
            player.writeExtraDetails();
        }
        var monsterName = monsterNames[dq.getMonsterId()] || "Monster ID " + dq.getMonsterId();
        if (dq.isCompleted()) {
            cm.sendOk("You've already completed today's quest. Come back tomorrow!");
            cm.dispose();
        } else if (dq.getKillCount() >= requiredKills) {
            cm.sendNext("Wow, you've hunted " + dq.getKillCount() + " " + monsterName + "! Let's get your reward.");
        } else {
            cm.sendNext("Your daily quest is to hunt " + requiredKills + " " + monsterName + ". Current progress: " + dq.getKillCount() + "/" + requiredKills + ".");
        }
    } else if (status == 1) {
        if (dq.isCompleted()) {
            cm.dispose();
        } else if (dq.getKillCount() >= requiredKills) {
            // Check inventory space
            if (cm.canHold(rewardItemId, 1)) {
                cm.gainItem(rewardItemId, 1);
                dq.setCompleted(true);
                extraDetails.setDailyQuest(dq);
                player.writeExtraDetails();
                cm.sendOk("Here's your Corruption Scroll! Come back tomorrow for a new quest.");
            } else {
                cm.sendOk("Please make space in your USE inventory.");
            }
            cm.dispose();
        } else {
            var monsterName = monsterNames[dq.getMonsterId()] || "Monster ID " + dq.getMonsterId();
            cm.sendOk("Keep hunting! You need " + (requiredKills - dq.getKillCount()) + " more " + monsterName + ".");
            cm.dispose();
        }
    }
}