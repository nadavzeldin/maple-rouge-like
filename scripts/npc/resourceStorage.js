/**
 * T-1337 Resource Manager
 * Manages account-wide storage of crafting items (ores, monster powder, ..), scrolls,
 * and merge coins
 *Reworked to be resource storage
 *@author CPURules
 */

 var status = 0;
 var sel;
 
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
             sel = selection;
 
             if (selection == 0) {
                 cm.sendOk("Ore storage!");
             }
             else if (selection == 1) {
                 cm.sendOk("Scroll storage!");
             }
             else if (selection == 2) {
                 cm.sendOk("Merge Coin storage!");
             }
             cm.dispose();
 
             /*
             if (selection == 0) {
                 if (cm.getPlayer().getGuildId() > 0) {
                     cm.sendOk("You may not create a new Guild while you are in one.");
                     cm.dispose();
                 } else {
                     cm.sendYesNo("Creating a Guild costs #b 1500000 mesos#k, are you sure you want to continue?");
                 }
             } else if (selection == 1) {
                 if (cm.getPlayer().getGuildId() < 1 || cm.getPlayer().getGuildRank() != 1) {
                     cm.sendOk("You can only disband a Guild if you are the leader of that Guild.");
                     cm.dispose();
                 } else {
                     cm.sendYesNo("Are you sure you want to disband your Guild? You will not be able to recover it afterward and all your GP will be gone.");
                 }
             } else if (selection == 2) {
                 if (cm.getPlayer().getGuildId() < 1 || cm.getPlayer().getGuildRank() != 1) {
                     cm.sendOk("You can only increase your Guild's capacity if you are the leader.");
                     cm.dispose();
                 } else {
                     var Guild = Java.type("net.server.guild.Guild");  // thanks Conrad for noticing an issue due to call on a static method here
                     cm.sendYesNo("Increasing your Guild capacity by #b5#k costs #b " + Guild.getIncreaseGuildCost(cm.getPlayer().getGuild().getCapacity()) + " mesos#k, are you sure you want to continue?");
                 }
             }*/
         }
         /*
         } else if (status == 2) {
             if (sel == 0 && cm.getPlayer().getGuildId() <= 0) {
                 cm.getPlayer().genericGuildMessage(1);
                 cm.dispose();
             } else if (cm.getPlayer().getGuildId() > 0 && cm.getPlayer().getGuildRank() == 1) {
                 if (sel == 1) {
                     cm.getPlayer().disbandGuild();
                     cm.dispose();
                 } else if (sel == 2) {
                     cm.getPlayer().increaseGuildCapacity();
                     cm.dispose();
                 }
             }
         }*/
     }
 }
 