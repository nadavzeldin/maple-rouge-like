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
   @Author: Roey Zeldin - command to sell all slots
*/
package client.command.commands.gm0;

import client.Character;
import client.Client;
import client.command.Command;
import client.inventory.InventoryType;
import client.inventory.Item;
import server.Shop;
import server.ShopFactory;

import java.util.Random;
import java.util.Set;

import static constants.game.GameConstants.LOOT_LIZARD_UI_BANNER;

public class RollCommand extends Command {
    {
        setDescription("roll random int number from 1 to n");
    }
    @Override
    public void execute(Client c, String[] params) {
        Character player = c.getPlayer();
        if (params.length < 1 || params.length > 3) {
            player.yellowMessage("Syntax: @roll <n> OR @roll <min> <max>");
            return;
        }
        int n = Integer.parseInt(params[0]);
        if (params.length == 2) {
            // Roll random number from n to max
            int max = Integer.parseInt(params[1]);
            int result = random.nextInt(n, max);
            String message = String.format("The number you have rolled for %d to %d is %d", n, max, result);
            c.getPlayer().getMap().startMapEffect(message, 5121001); // Korean Soccer Chant
            return;
        }
        // Roll random number from 0 to n
        int result = random.nextInt(n);
        String message = String.format("The number you have rolled for 0 to %d is %d", n, result);
        c.getPlayer().getMap().startMapEffect(message, 5121001); // Korean Soccer Chant
    }
    private final Random random = new Random();
}