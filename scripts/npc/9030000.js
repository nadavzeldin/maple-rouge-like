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
/* Fredrick NPC (9030000)
 * @author kevintjuh93
 */

var status = -1;
var selectedType = -1;

function start() {
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == 1) {
        status++;
    } else {
        if (status >= 2) {
            status--;
        } else {
            cm.dispose();
            return;
        }
    }

    if (status == 0) {
        var selStr = "What would you like me to do for you?\r\n";
        selStr += "#L0#Check my merchant items#l\r\n";
        selStr += "#L1#Merge equipment#l";
        cm.sendSimple(selStr);
    } else if (status == 1) {
        selectedType = selection;
        if (selection == 0) {  // Original Fredrick functionality
            if (!cm.hasMerchant() && cm.hasMerchantItems()) {
                cm.showFredrick();
                cm.dispose();
            } else {
                if (cm.hasMerchant()) {
                    cm.sendOk("You have a Merchant open.");
                } else {
                    cm.sendOk("You don't have any items or mesos to be retrieved.");
                }
                cm.dispose();
            }
        } else if (selection == 1) {  // Forging functionality
            var selStr = "Bring me a #i2280001# #t2280001# and I'll merge your equipment to make them stronger.";
            items = [2280001];  // Black Loud Machine
            for (var i = 0; i < items.length; i++) {
                selStr += "\r\n#L" + i + "##t" + items[i] + "##l";
            }
            cm.sendSimple(selStr);
        }
    } else if (status == 2) {
        if (selectedType == 1) {  // Forging process
            if (!cm.haveItem(2280001, 1)) {
                cm.sendOk("You need a #i2280001# #t2280001# to merge equipment.");
                cm.dispose();
                return;
            }

            cm.gainItem(2280001, -1); // Remove the Black Loud Machine
            const MergeCommand = Java.type('client.command.commands.gm0.MergeCommand');
            const processor = new MergeCommand();
            processor.execute(cm.getClient(), ["@merge"]);
            cm.sendOk("Your equipment has been successfully merged and enhanced!");
            cm.dispose();
        }
    }
}