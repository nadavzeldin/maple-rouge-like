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
import client.command.Command;
import constants.id.MobId;
import server.ShopFactory;
import server.life.LifeFactory;
import server.maps.FieldLimit;
import server.maps.MapleMap;
import server.maps.MiniDungeonInfo;

import java.awt.*;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static constants.game.GameConstants.LOOT_LIZARD_UI_BANNER;
import static constants.id.MapId.DOOM_MAPS;
import static constants.id.MapId.FM_ENTRANCE;

public class DoomCommand extends Command {
    {
        setDescription("Start the Doom");
    }
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    @Override
    public void execute(Client c, String[] params) {
        Character player = c.getPlayer();
        // if player is in doom map already, i.e. where there are monsters, disable tp
        if (DOOM_MAPS.contains(c.getPlayer().getMapId())){
            MapleMap target = c.getChannelServer().getMapFactory().getMap(FM_ENTRANCE);
            MapleMap currentMap = player.getMap();
            currentMap.killAllMonsters();
            currentMap.clearDrops();
            player.saveLocationOnWarp();
            player.changeMap(target, target.getRandomPlayerSpawnpoint());
            return;
        }
        try {
            MapleMap target = c.getChannelServer().getMapFactory().getMap(DOOM_MAP);
            if (target == null) {
                player.yellowMessage("Map ID " + params[0] + " is invalid.");
                return;
            }

            if (!player.isAlive()) {
                player.dropMessage(1, "This command cannot be used when you're dead.");
                return;
            }

            player.saveLocationOnWarp();
            player.changeMap(target, target.getRandomPlayerSpawnpoint());
        } catch (Exception ex) {
            player.yellowMessage("Map ID " + params[0] + " is invalid.");
        }
    }

    private final int DOOM_MAP = 980000000;
}
