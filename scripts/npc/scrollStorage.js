/**
 * Vega
 * Scroll storage system
 * @author CPURules
 */
const ResourceStorageType = Java.type('server.ResourceStorage');
const ItemInfo = Java.type('server.ItemInformationProvider')
var header = "#r#eScroll Storage#k#n\r\n\r\n";

var status;

var actionType; // 0 -> Withdraw, 1 -> Deposit
var selectedItem; // Item ID to withdraw / deposit
var filterText = ""; // string to filter items by
var setFilter = false;

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
    } else if (mode == 0 && type != 1) {
        status--;
    } else {
        status++;
    }

    var textList = [];
    textList.push(header);
    if (status == 0) { // main menu (select transaction type)
        textList.push("This system can be used to store any of the following types of items:\r\n");
        textList.push("\t#i2041000# Equipment enhancing scrolls\r\n");
        textList.push("\t#i2049100# Chaos scrolls\r\n");
        textList.push("\t#i2340000# White scrolls\r\n");
        textList.push("\t#i2049000# Clean slate scrolls\r\n");
        textList.push("\t#i2022153# All-stat cakes\r\n")
        textList.push("What would you like to do?\r\n");
        textList.push("#b");
        textList.push("#L0#Withdraw scrolls#l\r\n");
        textList.push("#L1#Deposit scrolls#l");

        cm.sendSimple(textList.join(""));
    } else if (status == 1) { // list items available for deposit/withdraw
        if (mode == 1) { // if we chose an action, set the action type (allows for backing up)
            actionType = selection;
        }

        if (actionType == 0) { // withdraw
            var resourceStorage = cm.getPlayer().getResourceStorage()[ResourceStorageType.SCROLL_OFFSET];
            var items = resourceStorage.getItems(filterText);

            if (items.size() == 0) {
                if (filterText != "") {
                    textList.push("It looks like you don't have any stored scrolls matching your filter: #r" + filterText + "#k...");
                }
                else {
                    textList.push("It looks like you don't have any scrolls stored right now...\r\n\r\n");
                }
                textList.push("\r\n\r\n");
                cm.sendOk(textList.join(""));
                cm.dispose();
                return;
            }

            textList.push("Below are the scrolls you currently have stored.\r\n");
            textList.push("#L0#If you'd like, click this line to filter this list#l\r\n\r\n");
            if (filterText != "") {
                textList.push("Current filter: #r" + filterText + "#k\r\n");
            }
            textList.push("\r\n");
            textList.push("What would you like to withdraw?\r\n");
            for (var i = 0; i < items.size(); i++) {
                var item = items[i];
                var id = item.getItemId();
                textList.push("#b#L" + id + "#" + itemStr(id) + "#k (" + item.getQuantity() + ")#l\r\n");
            }
            cm.sendSimple(textList.join(""));
        }
        else if (actionType == 1) { // deposit
            const InventoryType = Java.type('client.inventory.InventoryType');
            var inv = cm.getPlayer().getInventory(InventoryType.USE).getScrolls();
            if (inv.size() == 0) {
                textList.push("It looks like you don't have any scrolls on you right now...\r\n\r\n");
                cm.sendOk(textList.join(""));
                cm.dispose();
                return;
            }

            textList.push("Below are the scrolls currently in your inventory.\r\n\r\n");
            textList.push("What would you like to deposit?\r\n");
            for (var i = 0; i < inv.size(); i++) {
                var item = inv[i];
                var id = item.getItemId();
                textList.push("#b#L" + id + "# " + itemStr(id) + "#k (" + item.getQuantity() + ")#l\r\n");
            }
            cm.sendSimple(textList.join(""));
        }
        else { // safeguard
            cm.dispose();
        }
    } else if (status == 2) { // prompt for quantity
        if (mode == 1) { // we came from the previous screen
            if (selection == 0) { // filter
                setFilter = true;
                textList.push("Enter the text you'd like to use for filtering your stored scrolls:\r\n\r\n");
                cm.sendGetText(textList.join(""));
                return;
            }
            selectedItem = selection;
        }

        if (actionType == 0) { // withdraw
            var resourceStorage = cm.getPlayer().getResourceStorage()[ResourceStorageType.SCROLL_OFFSET];
            var item = resourceStorage.getItemById(selectedItem);
            
            textList.push("How many #b" + itemStr(selectedItem) + "#k would you like to withdraw?\r\n");
            cm.sendGetNumber(textList.join(""), item.getQuantity(), 1, item.getQuantity());
        }
        else if (actionType == 1) { // deposit
            const InventoryType = Java.type('client.inventory.InventoryType');
            var inv = cm.getPlayer().getInventory(InventoryType.USE);
            var item = inv.findById(selectedItem);

            textList.push("How many #b" + itemStr(selectedItem) + "#k would you like to deposit?\r\n");
            cm.sendGetNumber(textList.join(""), item.getQuantity(), 1, item.getQuantity());
        }
        else { // safeguard
            cm.dispose();
        }
    } else if (status == 3) { // process transaction
        if (setFilter) { // If we came from the set filter screen, update the filter text and backtrack to the item list screen
            filterText = cm.getText();
            setFilter = false;
            status = 2;
            action(0, 0, 0);
            return;
        }
        var qty = selection;
        var resourceStorage = cm.getPlayer().getResourceStorage()[ResourceStorageType.SCROLL_OFFSET];
        var item = resourceStorage.getItemById(selectedItem);

        if (actionType == 0) { // withdraw
            if (item == null || item.getQuantity() < qty) {
                textList.push("It looks like you don't have enough #b" + itemStr(selectedItem) + "#k to withdraw!");
                cm.sendOk(textList.join(""));
                cm.dispose();
                return;
            }

            var newInvItem = cm.gainItem(selectedItem, qty, false, true);
            if (newInvItem == null) { // inventory is full, client will display message, just quit
                cm.dispose();
                return;
            }

            if (resourceStorage.takeOut(item, selection)) {
                textList.push("Withdrew " + qty + " #b" + itemStr(selectedItem) + "#k!\r\n\r\n");
                textList.push("Would you like to withdraw more scrolls?\r\n");
                cm.sendYesNo(textList.join(""));
            }
        }
        else if (actionType == 1) { // deposit
            const InventoryType = Java.type('client.inventory.InventoryType');
            var inv = cm.getPlayer().getInventory(InventoryType.USE);
            var item = inv.findById(selectedItem);

            if (item == null || item.getQuantity() < qty) {
                cm.sendOk("It looks like you don't have enough #b" + itemStr(selectedItem) + "#k to deposit!");
                cm.dispose();
                return;
            }

            if (resourceStorage.store(item, selection)) {
                cm.gainItem(selectedItem, -1 * qty, false, true);
                textList.push("Deposited " + qty + " #b" + itemStr(selectedItem) + "#k!\r\n\r\n");
                textList.push("Would you like to deposit more crafting materials?\r\n");
                cm.sendYesNo(textList.join(""));
            }
            else {
                cm.sendOk("It looks like your storage might be full! You can only hold up to #b32,767#k of any one item.");
                cm.dispose();
            }
        }
    }
    else if (status == 4) { // more transactions?
        if (mode == 0) { // no - dispose and return
            cm.dispose();
            return;
        }
        else { // yes - update status and call action again
            status = 2;
            action(0, 0, null);
        }
    }
    else {
        cm.dispose();
    }
}