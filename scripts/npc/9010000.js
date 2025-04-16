/* 
 * NPC: Nana (Henesys)
 * ID: 9010000
 * Daily Scroll Quest
 */

var status = 0;
var requiredKills = 100;
var rewardItemId = 2049115; // Corruption Scroll

// Hardcoded monster name mapping
var monsterNames = {
    2230103: "Trixter",
    2230109: "Bubble Fish",
    3210202: "Jr. Grupin",
    4230112: "Master Robo",
    4230100: "Cold Eye",
    4230120: "Plateon",
    4230400: "Iron Boar",
    4230300: "Moon Bunny",
    5130107: "Coolie Zombie",
    5250001: "Stone Bug",
    6110300: "Homun",
    6130208: "Kru",
    7130601: "Green Hobi",
    8140101: "Black Kentaurus",
    8140002: "Blood Harp",
    8140600: "Bone Fish",
    8140703: "Brexton",
    8150101: "Cold Shark",
    8200000: "Eye of Time",
    3210450: "Scuba Pepe",
    4230104: "Clang",
    5100003: "Hodori",
    5100002: "Firebomb",
    5120506: "The Book Ghost",
    5130102: "Dark Stone Golem",
    5250002: "Primitive Boar",
    6130100: "Red Drake",
    5250002: "Boar",
    6130200: "Buffy",
    6130204: "Mr. Alli",
    6230200: "Dark Pepe",
    6230400: "Soul Teddy (Undead)",
    6400100: "Deep Buffoon",
    7130004: "Hankie",
    7130101: "Taurospear",
    7130500: "Rash",
    8140300: "Dark Klock (Undead)",
    8142100: "Risell Squid",
    8200010: "Oblivion Monk Trainee",
    7130500: "Rash",
    7130500: "Rash",
};

// take from monsterNames
var monsterList = Object.keys(monsterNames).map(function(key) { 
    return parseInt(key, 10);
}
);



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