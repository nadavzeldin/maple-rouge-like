/**
 * Talking Snow Man
 * Merge coin storage system
 * @author CPURules
 */
const ResourceStorageType = Java.type('server.ResourceStorage');
var header = "#r#eMerge Coin Storage#k#n\r\n\r\n";

var status;

var actionType; // 0 -> Withdraw, 1 -> Deposit
var itemId = 2022280;

function start() {
    status = -1;
    action(1, 0, 0);
}

function itemStr(id) {
    return ("#v" + id + "# #t" + id + "#");
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
    textList.push(header);
    if (status == 0) { // main menu (select transaction type)
        textList.push("This system can be used to store merge coins (#b" + itemStr(itemId) + "#k).\r\n\r\n");
        textList.push("What would you like to do?\r\n");
        textList.push("#b");
        textList.push("#L0#Withdraw merge coins\r\n");
        textList.push("#L1#Deposit merge coins#l");

        cm.sendSimple(textList.join(""));
    } else if (status == 1) { // show current balance and prompt for quantity
        if (mode == -1) { // if we end chat from the final screen, we exit the npc
            cm.dispose();
        } else if (mode == 1) {
            actionType = selection;
        }

        if (actionType == 0) { // withdraw
            var resourceStorage = cm.getPlayer().getResourceStorage()[ResourceStorageType.MERGE_COIN_OFFSET];
            var items = resourceStorage.getItems();

            if (items.size() == 0) {
                textList.push("It looks like you don't have any merge coins stored right now...\r\n\r\n");
                cm.sendOk(textList.join(""));
                cm.dispose();
                return;
            }

            var qty = items[0].getQuantity();
            textList.push("You currently have #b#e" + qty + "#n " + itemStr(itemId) + "#k stored.\r\n\r\n");
            textList.push("How many would you like to withdraw?\r\n");
            cm.sendGetNumber(textList.join(""), qty, 1, qty);
        }
        else if (actionType == 1) { // deposit
            const InventoryType = Java.type('client.inventory.InventoryType');
            var qty = cm.getPlayer().getInventory(InventoryType.USE).getMergeCoins();
            if (qty == 0) {
                textList.push("It looks like you don't have any merge coins on you right now...\r\n\r\n");
                cm.sendOk(textList.join(""));
                cm.dispose();
                return;
            }

            textList.push("You currently have #b#e" + qty + "#n " + itemStr(itemId) + "#k in your inventory.\r\n\r\n");
            textList.push("How many would you like to deposit?\r\n");
            cm.sendGetNumber(textList.join(""), qty, 1, qty);
        }
        else { // safeguard
            cm.dispose();
        }
    } else if (status == 2) { // process transaction
        var qty = selection;
        var resourceStorage = cm.getPlayer().getResourceStorage()[ResourceStorageType.MERGE_COIN_OFFSET];
        var item = resourceStorage.getItemById(itemId);

        if (actionType == 0) { // withdraw
            if (item == null || item.getQuantity() < qty) {
                textList.push("It looks like you don't have enough #b" + itemStr(item) + "#k to withdraw!");
                cm.sendOk(textList.join(""));
                cm.dispose();
                return;
            }

            var newInvItem = cm.gainItem(itemId, qty, false, true);
            if (newInvItem == null) { // no room in inventory, client will display a prompt
                cm.dispose();
                return;
            }

            if (resourceStorage.takeOut(item, qty)) {
                textList.push("Withdrew " + qty + " #b" + itemStr(itemId) + "#k!");
                cm.sendOk(textList.join(""));
                cm.dispose();
            }
        }
        else if (actionType == 1) { // deposit
            const InventoryType = Java.type('client.inventory.InventoryType');
            var inv = cm.getPlayer().getInventory(InventoryType.USE);
            var item = inv.findById(itemId);

            if (item == null || item.getQuantity() < qty) {
                cm.sendOk("It looks like you don't have enough #b" + itemStr(itemId) + "#k to deposit!");
                cm.dispose();
                return;
            }

            if (resourceStorage.store(item, qty)) {
                cm.gainItem(itemId, -1 * qty, false, true);
                textList.push("Deposited " + qty + " #b" + itemStr(itemId) + "#k!");
                cm.sendOk(textList.join(""));
                cm.dispose();
            }
            else {
                cm.sendOk("It looks like your storage might be full!");
                cm.dispose();
            }
        }
    }
    else {
        cm.dispose();
    }
}