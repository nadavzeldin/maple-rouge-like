var curMap
var stage1 = 980000100
var stage2= 980000200
var stage3= 980000300
var stage4= 980000400
var stage5= 980000500
var stage6= 980000600

function start() {
    status = -1;
    curMap = cm.getMapId();
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode < 1) {
        cm.dispose();
    } else {
        if (curMap == stage1) {
            status++;
            var map = cm.getClient().getChannelServer().getMapFactory().getMap(stage1);
            const LifeFactory = Java.type('server.life.LifeFactory');
            const Point = Java.type('java.awt.Point');
            var bossMobid = 8500001; // 8500001
            var boss = LifeFactory.getMonster(bossMobid);
            var bossPos = new Point(201, 80);
            if (map.countMonsters() == 0) { //Center tower
                if (status == 0) {
                    cm.sendYesNo("good job completing the doom, start next wave?");
                } else if (status == 1) {
                    var map = cm.getClient().getChannelServer().getMapFactory().getMap(stage2);
                    map.spawnMonsterOnGroundBelow(boss, bossPos);
                    cm.warpParty(stage2, 0);
                    cm.dispose();
                }
            } else {
                cm.sendOk("kill all");
                cm.dispose();
            }
        }
        if (curMap == stage3) {
            status++;
            var map = cm.getClient().getChannelServer().getMapFactory().getMap(stage3);
            const LifeFactory = Java.type('server.life.LifeFactory');
            const Point = Java.type('java.awt.Point');
            var bossMobid = 8820001; // pinkbean
            var boss = LifeFactory.getMonster(bossMobid);
            var bossPos = new Point(201, 80);
            if (map.countMonsters() == 0) {
                if (status == 0) {
                    cm.sendYesNo("good job completing the doom, start next wave?");
                } else if (status == 1) {
                    var map = cm.getClient().getChannelServer().getMapFactory().getMap(stage4);
                    map.spawnMonsterOnGroundBelow(boss, bossPos);
                    cm.warpParty(stage4, 0);
                    cm.dispose();
                }
            } else {
                cm.sendOk("kill all");
                cm.dispose();
            }
        }
        if (curMap == stage5) {
            status++;
            var map = cm.getClient().getChannelServer().getMapFactory().getMap(stage5);
            if (map.countMonsters() == 0) { //Center tower
                if (status == 0) {
                    cm.sendYesNo("good job completing the doom, start next wave?");
                } else if (status == 1) {
                    var map = cm.getClient().getChannelServer().getMapFactory().getMap(stage6);
                    const LifeFactory = Java.type('server.life.LifeFactory');
                    const Point = Java.type('java.awt.Point');
                    var bossPos = new Point(190, 80);
                    for (var i = 9300291; i <= 9300294; i++) {
                        map.spawnMonsterOnGroundBelow(LifeFactory.getMonster(i), bossPos);
                    }
                    map.spawnMonsterOnGroundBelow(LifeFactory.getMonster(8800000), new Point(150, 80)); // zakum num 1
                    map.spawnMonsterOnGroundBelow(LifeFactory.getMonster(8820001), new Point(50, 80)); // pink bean
                    map.spawnMonsterOnGroundBelow(LifeFactory.getMonster(9400121), new Point(50, 80)); // female boss
                    map.spawnMonsterOnGroundBelow(LifeFactory.getMonster(9400300), new Point(50, 80)); // the boss
                    map.spawnMonsterOnGroundBelow(LifeFactory.getMonster(9400014), new Point(50, 80)); // Black Crow


                    cm.warpParty(stage6, 0);
                    cm.dispose();
                }
            } else {
                cm.sendOk("kill all");
                cm.dispose();
            }
        }
    }
}