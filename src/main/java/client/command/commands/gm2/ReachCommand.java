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
package client.command.commands.gm2;

import client.Character;
import client.Client;
import client.command.Command;
import server.maps.MapleMap;

import static constants.id.MapId.DOOM_MAPS;

public class ReachCommand  extends Command {
    {
        setDescription("Warp to a player.");
    }

    @Override
    public void execute(Client c, String[] params) {
        Character player = c.getPlayer();

        // Check command syntax
        if (params.length < 1) {
            player.yellowMessage("Syntax: !reach <playername>");
            return;
        }

        String targetName = params[0];
        Character target = c.getWorldServer().getPlayerStorage().getCharacterByName(targetName);

        if (!player.isGM() && DOOM_MAPS.contains(target.getMap().getId()))
        {
            player.yellowMessage("cant teleport into doom");
            return;
        }

        // Check if target exists and is logged in
        if (target == null || !target.isLoggedin()) {
            player.yellowMessage("Unknown player.");
            return;
        }

        // Check party constraint
        if (!player.isGM() && !player.isPartyMember(target)) {
            player.yellowMessage("You can only reach players in your party.");
            return;
        }

        // Check channel constraint
        if (player.getClient().getChannel() != target.getClient().getChannel()) {
            player.dropMessage(5, "Player '" + target.getName() + "' is at channel " +
                    target.getClient().getChannel() + ".");
            return;
        }

        // Teleport to target
        MapleMap targetMap = target.getMap();
        player.saveLocationOnWarp();
        player.forceChangeMap(targetMap, targetMap.findClosestPortal(target.getPosition()));
    }
}
