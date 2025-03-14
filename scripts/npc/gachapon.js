/*
	This file is part of the OdinMS Maple Story Server
    Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc>
		       Matthias Butz <matze@odinms.de>
		       Jan Christian Meyer <vimes@odinms.de>

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation version 3 as published by
    the Free Software Foundation. You may not use, modify or distribute
    this program under any other version of the GNU Affero General Public
    License.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
/* Modified Gachapon with Group Prize Selection */

var status;
var ticketId = 5220000;
var mapName = ["Henesys", "Ellinia", "Perion", "Kerning City", "Sleepywood", "Mushroom Shrine", "Showa Spa (M)", "Showa Spa (F)", "Ludibrium", "New Leaf City", "El Nath", "Nautilus"];
var curMapName = "";
var selection;
var amount = 0;
var selectedOption = -1;
var MESO_COST_PER_ROLL = 10000000; // 10 million mesos per roll

// Prize groups
var prizeGroups = [
    "Taming",
    "Weapons & Shields",
    "Accessories & Gloves",
    "Capes & Shoes",
    "Uses",
    "Setups"
];

function start() {
    status = -1;
    curMapName = mapName[(cm.getNpc() != 9100117 && cm.getNpc() != 9100109) ? (cm.getNpc() - 9100100) : cm.getNpc() == 9100109 ? 9 : 11];

    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode < 0) {
        cm.dispose();
    } else {
        if (mode == 1) {
            status++;
        } else {
            status--;
        }

        if (status == 0 && mode == 1) {
            if (cm.haveItem(ticketId)) {
                cm.sendSimple("Welcome to the " + curMapName + " Gachapon. How would you like to proceed?\r\n\r\n#L0#Use regular Gachapon ticket#l\r\n#L1#Use Premium Group Selection (1M mesos per roll)#l\r\n#L2#What is Gachapon?#l\r\n#L3#Where can you buy Gachapon tickets?#l");
            } else {
                cm.sendSimple("Welcome to the " + curMapName + " Gachapon. How may I help you?\r\n\r\n#L2#What is Gachapon?#l\r\n#L3#Where can you buy Gachapon tickets?#l");
            }
        } else if (status == 1) {
            if (selection == 0 && cm.haveItem(ticketId)) {
                // Original gachapon logic
                if (cm.canHold(1302000) && cm.canHold(2000000) && cm.canHold(3010001) && cm.canHold(4000000)) {
                    cm.gainItem(ticketId, -1);
                    cm.doGachapon();
                } else {
                    cm.sendOk("Please have at least one slot in your #rEQUIP, USE, SET-UP, #kand #rETC#k inventories free.");
                }
                cm.dispose();
            } else if (selection == 1) {
                // Group selection gachapon
                var selectionText = "Choose a prize group you're interested in:\r\n\r\n";
                for (var i = 0; i < prizeGroups.length; i++) {
                    selectionText += "#L" + i + "#" + prizeGroups[i] + "#l\r\n";
                }
                cm.sendSimple(selectionText);
                this.selection = selection;
            } else if (selection == 2) {
                cm.sendNext("Play Gachapon to earn rare scrolls, equipment, chairs, mastery books, and other cool items! All you need is a #bGachapon Ticket#k to be the winner of a random mix of items.");
                status = 10; // Skip to explanation flow
            } else if (selection == 3) {
                cm.sendNext("Gachapon Tickets are available in the #rCash Shop#k and can be purchased using NX or Maple Points. Click on the red SHOP at the lower right hand corner of the screen to visit the #rCash Shop#k where you can purchase tickets.");
                status = 10; // Skip to explanation flow
            }
        } else if (status == 2) {
            if (this.selection == 1) {
                // User selected a prize group
                selectedOption = selection;
                cm.sendGetNumber("How many times would you like to roll? (1-10)\r\nEach roll costs 1,000,000 mesos.", 1, 1, 10);
            } else {
                cm.sendNextPrev("You'll find a variety of items from the " + curMapName + " Gachapon, but you'll most likely find items and scrolls related to " + curMapName + ".");
            }
        } else if (status == 3) {
            if (this.selection == 1) {
                // User entered amount of rolls
                amount = selection;
                var totalCost = amount * MESO_COST_PER_ROLL;
                cm.sendYesNo("You're about to roll " + amount + " times for items in the #b" + prizeGroups[selectedOption] + "#k category.\r\n\r\nTotal cost: #r" + totalCost + " mesos#k\r\n\r\nWould you like to proceed?");
            } else {
                cm.dispose();
            }
        } else if (status == 4) {
            // Verify mesos and inventory space
            var totalCost = amount * MESO_COST_PER_ROLL;

            if (cm.getMeso() < totalCost) {
                cm.sendOk("You don't have enough mesos. You need #r" + totalCost + " mesos#k to roll " + amount + " times.");
                cm.dispose();
                return;
            }

            // Check inventory space based on selected option
            var hasSpace = false;

            switch (selectedOption) {
                case 0: // Taming
                    // For taming items, we'll just check if there's at least amount slots free
                    // in SETUP inventory since that's where monsters go (most important part)
                    hasSpace = cm.canHold(1902000, amount);
                    // Even if saddle check fails, allow it to proceed - they'll just get the monster
                    break;
                case 1: // Weapons & Shields
                    hasSpace = cm.canHold(1302000, amount) && cm.canHold(1092000, amount);
                    break;
                case 2: // Rings, Capes, Gloves, Face & Accessories
                    hasSpace = cm.canHold(1112000, amount) && cm.canHold(1102000, amount) &&
                        cm.canHold(1082000, amount) && cm.canHold(1012000, amount) &&
                        cm.canHold(1022000, amount);
                    break;
                case 3: // Cap, Coat, Longcoat, Pants & Shoes
                    hasSpace = cm.canHold(1002000, amount) && cm.canHold(1040000, amount) &&
                        cm.canHold(1050000, amount) && cm.canHold(1060000, amount) &&
                        cm.canHold(1072000, amount);
                    break;
                case 4: // Uses
                    hasSpace = cm.canHold(2000000, amount) && cm.canHold(2040000, amount) && cm.canHold(2070000, amount);
                    break;
            }

            // Take mesos and do gachapon rolls
            cm.gainMeso(-totalCost);

            // Do gachapon rolls with selected group
            // You'll need to implement the actual doGroupGachapon function in your server-side code
            for (var i = 0; i < amount; i++) {
                cm.doGroupGachapon(selectedOption);
            }

            cm.sendOk("You've successfully rolled " + amount + " times in the " + prizeGroups[selectedOption] + " category. Check your inventory for the new items!");
            cm.dispose();
        } else if (status == 11) {
            cm.sendNextPrev("You'll find a variety of items from the " + curMapName + " Gachapon, but you'll most likely find items and scrolls related to " + curMapName + ".");
        } else {
            cm.dispose();
        }
    }
}
