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
import server.maps.MapleMap;

import java.awt.*;
import java.util.Objects;

import static constants.game.GameConstants.LOOT_LIZARD_UI_BANNER;

public class DoomCommand extends Command {
    {
        setDescription("Start the Doom");
    }

    @Override
    public void execute(Client c, String[] params) {
        Character player = c.getPlayer();
        player.getMap().startMapEffect("THE DOOM HAS STARTED, DO NOT DIE!", 5120011); // 5120011 is a test so far
        final Point targetPoint = player.getPosition();
        final MapleMap targetMap = player.getMap();

        for (int stage = 1; stage <= 3; stage++)
        {
//            try{
//                Thread.sleep(10000); // sleep for 10 secs
//            } catch (InterruptedException e) {
//            }
            String stringText = String.format("Starting stage number : %d goodLuck, you have 1 minute!", stage);
            player.getMap().startMapEffect(stringText, 5120011); // 5120011 is a test so far
            spawnDoomMonster(stage, targetPoint, targetMap);

//            try {
//                Thread.sleep(600000); // sleep for 1 minute
//            } catch (InterruptedException e) {
//            }
        }
    }

    private void spawnDoomMonster(int stage, Point targetPoint, MapleMap targetMap)
    {
        switch (stage)
        {
            case 1:
                targetMap.spawnMonsterOnGroundBelow(Objects.requireNonNull(LifeFactory.getMonster(MobId.ZOMBIE_MUSHROOM)), targetPoint);
                return;
            case 2:
                for (int i = 0; i < 10; i++) {
                    targetMap.spawnMonsterOnGroundBelow(Objects.requireNonNull(LifeFactory.getMonster(MobId.MUSH_MOM)), targetPoint);
                }
                return;
            case 3:
                return;
            case 4:
                targetMap.spawnMonsterOnGroundBelow(Objects.requireNonNull(LifeFactory.getMonster(MobId.PAPULATUS_CLOCK)), targetPoint);
                return;
            case 5:
                // Spawn Zakum
                targetMap.spawnFakeMonsterOnGroundBelow(Objects.requireNonNull(LifeFactory.getMonster(MobId.ZAKUM_1)), targetPoint);
                for (int mobId = MobId.ZAKUM_ARM_1; mobId <= MobId.ZAKUM_ARM_8; mobId++) {
                    targetMap.spawnMonsterOnGroundBelow(Objects.requireNonNull(LifeFactory.getMonster(mobId)), targetPoint);
                }
                return;
            case 6:
                targetMap.spawnHorntailOnGroundBelow(targetPoint);
                return;
        }
    }
}
