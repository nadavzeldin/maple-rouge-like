/**
 * T-1337 Resource Manager
 * Main menu for account-wide resource storage (ores; scrolls; merge coins)
 * @author CPURules
 */

var status;

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == -1 || (mode == 0 && status == 0)) {
        cm.dispose();
        return;
    } else if (mode == 0) {
        status--;
    } else {
        status++;
    }

    var textList = [];

    if (status == 0) {
        textList.push("#r#eResource Storage Menu#k#n\r\n\r\n");
        textList.push("Welcome to the account-wide resource storage system!  This storage acts as a safe place to store various materials you may find throughout your journey.  ");
        textList.push("This includes ores, scrolls, and merge coins.\r\n\r\n");
        textList.push("Any eligible resources you loot will automatically be transferred to this storage system from your inventory.  Use #r@togglestore#k to change this setting.\r\n\r\n");
        textList.push("Which system would you like to access?\r\n");
        textList.push("#b");
        textList.push("#L0#Crafting material storage#l\r\n");
        textList.push("#L1#Scroll storage#l\r\n");
        textList.push("#L2#Merge Coin storage#l\r\n");

        cm.sendSimple(textList.join(""));
    } else if (status == 1) {
        var npcIds = [2083002, 2041016, 9310083];
        var scriptIds = ["oreStorage", "scrollStorage", "coinStorage"];
        cm.dispose();
        cm.openNpc(npcIds[selection], scriptIds[selection]);
    }
    else {
        cm.dispose();
    }
}