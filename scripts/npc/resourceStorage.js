/**
 * T-1337 Resource Manager
 * Manages account-wide storage of crafting items (ores, monster powder, ..), scrolls,
 * and merge coins
 *Reworked to be resource storage
 *@author CPURules
 */

 var status = 0;
 var sel;
 var storageType;
 var actionType;

 var headers = [
    "#r#eOre Storage#k#n",
    "#b#eScroll Storage#k#n",
    "#d#eMerge Coin Storage#k#n"
 ]

 function start() {
     cm.sendSimple("I'm the new storage manager!\r\n\r\n"
                    + "#b#L0#Ore Storage\r\n"
                    + "#b#L1#Scroll Storage\r\n"
                    + "#b#L2#Merge Coin Storage");
 }
 
 function action(mode, type, selection) {
     if (mode == -1) {
         cm.dispose();
     } else {
         if (mode == 0 && status == 0) {
             cm.dispose();
             return;
         }
         if (mode == 1) {
             status++;
         } else {
             status--;
         }
 
         if (status == 1) {
             storageType = selection;

             var listText = [];

             listText.push(headers[storageType]);
             if (selection == 0) {
                listText.push("This system can be used to store any of the following types of items:");
                listText.push("\tOres (including refined and strengthened plates/jewels)");
                listText.push("\tMonster Crystals");
                listText.push("\tMagic Powders");
                listText.push("\tCrafting Stimulators and Production Manuals");
                listText.push("\t#b#v4020009# #t4020009##k and #b#v4021010# #t4021010#");
             }
             else if (selection == 1) {
                listText.push("This system can be used to store any equipment enhancing scroll you find on your journey.");
             }
             else if (selection == 2) {
                listText.push("This system can be used to store any #b#v2022280##t2022280##k you find on your journey.");
            }
            listText.push("");
            
            listText.push("#kPlease select what you would like to do:");
            listText.push("#b#L0#Withdraw from storage#l");
            listText.push("#L1#Deposit into storage#l");
        
            cm.sendSimple(listText.join("\r\n"));
         }
         else if (status == 2) {
            actionType = selection;

            var listText = [];
            listText.push(headers[storageType])
            if (actionType == 0) {
                var resourceStorage = cm.getPlayer().getResourceStorage()[storageType];
                var items = resourceStorage.getItems();
                var c = items.size();

                listText.push("Below are the items you currently have in storage.  Select one to withdraw.");
                listText.push("You are currently using #e" + c + "#k slot" + (c == 1 ? "" : "s") + " out of " + resourceStorage.getSlots());
                listText.push("");
                for (var i = 0; i < items.size(); i++) {
                    listText.push("#b#L" + i + "##v" + items[i].getItemId() + "# #t" + items[i].getItemId() + "##l");
                }   
                
                cm.sendSimple(listText.join("\r\n"));
                cm.dispose();
            }
            else if (actionType == 1) {
                const InventoryType = Java.type('client.inventory.InventoryType');
                var inv;
                if (storageType == 0) {
                    inv = cm.getPlayer().getInventory(InventoryType.ETC);
                }
                else {
                    inv = cm.getPlayer().getInventory(InventoryType.USE);
                }

                if (storageType < 2) {
                    var canDeposit;
                    if (storageType == 0) canDeposit = inv.getOres();
                    else if (storageType == 1) canDeposit = inv.getScrolls();
                    listText.push("inv size: " + inv.list().size() + " - deposit size: " + canDeposit.size());
                    listText.push("Select the item you wish to deposit");
                    for (var i = 0; i < canDeposit.size(); i++) {
                        listText.push("#b#L" + i + "##v" + canDeposit[i].getItemId() + "# #t" + canDeposit[i].getItemId() + "##l");
                    }

                    cm.sendSimple(listText.join("\r\n"));
                    cm.dispose();
                }
                else {
                    var coins = cm.getPlayer().getInventory(InventoryType.USE).getMergeCoins();
                    listText.push("You currently have #e" + coins + "#k merge coins.");
                    listText.push("Enter the number you wish to deposit below.");

                    cm.sendSimple(listText.join("\r\n"));
                    cm.dispose();
                }
            }
         }
         else {
            cm.dispose();
         }
     }
 }
 