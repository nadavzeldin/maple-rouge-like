/**
 -- Version Info -----------------------------------------------------------------------------------
 1.0 - First Version by Drago (MapleStorySA)
 2.0 - Second Version by Ronan (HeavenMS)
 3.0 - Third Version by Jayd - translated CPQ contents to English and added Pirate items
 Special thanks to 頼晏 (ryantpayton) for also stepping in to translate CPQ scripts.
 ---------------------------------------------------------------------------------------------------
 **/

var status = 0;

var cpqMap = 980000000;

function start() {
    status = -1;


    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == -1) {
        cm.dispose();
    } else {
        if (status >= 0 && mode == 0) {
            cm.dispose();
            return;
        }
        if (mode == 1) {
            status++;
        } else {
            status--;
        }
        if (status == 0) {
            cm.sendYesNo("Would you like to doom?");
        } else if (status == 1) {
            var mapId = 980000100; // Example Map ID, replace with actual Map ID
            var map = cm.getClient().getChannelServer().getMapFactory().getMap(mapId);
            const LifeFactory = Java.type('server.life.LifeFactory');
            const Point = Java.type('java.awt.Point');
            var bossMobid = 2230101; // Zombie
            var boss = LifeFactory.getMonster(bossMobid);
            var bossPos = new Point(201, 80);
            map.spawnMonsterOnGroundBelow(boss, bossPos);
            cm.warpParty(980000100, 0);
            cm.dispose();
        }
    }
}