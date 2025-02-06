/*
    This file is part of the HeavenMS MapleStory Server, commands OdinMS-based
    Copyleft (L) 2016 - 2019 RonanLana

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

/*
   @Author: Arthur L - Refactored command content into modules
*/
package client.command.commands.gm0;

import client.Character;
import client.Client;
import client.Stat;
import client.command.Command;
import client.inventory.Equip;
import client.inventory.InventoryType;
import client.inventory.Item;
import client.inventory.ModifyInventory;
import client.inventory.manipulator.InventoryManipulator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.PacketCreator;
import tools.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ResetStatsCommand extends Command {
    {
        setDescription("Reset all your stats back to default");
    }

    @Override
    public void execute(Client c, String[] params) {
        Character chr = c.getPlayer();

        // Calculate total AP to return
        int str = chr.getStr() - 4;  // 4 is base stat
        int dex = chr.getDex() - 4;
        int int_ = chr.getInt() - 4;
        int luk = chr.getLuk() - 4;

        // Add up total AP used
        int totalAP = str + dex + int_ + luk;

        // Create stat update list
        List<Pair<Stat, Integer>> stats = new ArrayList<>();
        stats.add(new Pair<>(Stat.STR, 4));
        stats.add(new Pair<>(Stat.DEX, 4));
        stats.add(new Pair<>(Stat.INT, 4));
        stats.add(new Pair<>(Stat.LUK, 4));

        // Update character stats
        chr.setStr(4);
        chr.setDex(4);
        chr.setInt(4);
        chr.setLuk(4);

        // Return AP
        chr.setRemainingAp(chr.getRemainingAp() + totalAP);
        stats.add(new Pair<>(Stat.AVAILABLEAP, chr.getRemainingAp()));

        // Send the packet to update stats
        c.sendPacket(PacketCreator.updatePlayerStats(stats, true, chr));

        chr.message("Stats have been reset. AP Returned: " + totalAP);
    }
}