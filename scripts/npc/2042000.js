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
            cm.sendYesNo("#b[Welcome to DoomMS]#k\r\n\r\n" +
                "Prepare yourself for the ultimate survival challenge with our unique DOOM wave battle system:\r\n\r\n" +
                "#r1.#k Face off against #b5 waves of relentless enemies#k, ranging from #reasy fodder to nearly impossible demons#k.\r\n\r\n" +
                "#r2.#k Survive without the fear of losing your character! Death here means a respawn, not the end!\r\n\r\n" +
                "#r3.#k Conquer all five waves to earn #bMusical Coins#k, which you can use to unlock exclusive rewards and power-ups.\r\n\r\n" +
                "#r4.#k GOOD LUCK, I created something I think is impoessible to beat!");

        } else if (status == 1) {
            var mapId = 980000100; // Example Map ID, replace with actual Map ID
            var map = cm.getClient().getChannelServer().getMapFactory().getMap(mapId);
            const LifeFactory = Java.type('server.life.LifeFactory');
            const Point = Java.type('java.awt.Point');
            var bossMobid = 6130101; // Mushmom
            var boss = LifeFactory.getMonster(bossMobid);
            var bossPos = new Point(201, 80);
            map.spawnMonsterOnGroundBelow(boss, bossPos);
            cm.warpParty(980000100, 0);
            cm.dispose();
        }
    }
}