var curMap
var lobby = 980000000
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
    const LifeFactory = Java.type('server.life.LifeFactory');
    const Point = Java.type('java.awt.Point');
    if (mode < 1) {
        cm.dispose();
    } else {
        if (curMap == stage2) {
            status++;
            var map = cm.getClient().getChannelServer().getMapFactory().getMap(stage2);
            var bossPos = new Point(201, 80);
            if (map.countMonsters() == 0) {
                if (status == 0) {
                    cm.sendYesNo("good job completing the doom, start next wave?");
                } else if (status == 1) {
                    var map = cm.getClient().getChannelServer().getMapFactory().getMap(stage3);
                    map.spawnHorntailOnGroundBelow(bossPos);
                    cm.warpParty(stage3, 0);
                    cm.dispose();
                }
            } else {
                cm.sendOk("kill all");
                cm.dispose();
            }
        }
    }
    if (curMap == stage4) {
        status++;
        var map = cm.getClient().getChannelServer().getMapFactory().getMap(stage4);
        const LifeFactory = Java.type('server.life.LifeFactory');
        const Point = Java.type('java.awt.Point');
        var bossPos = new Point(190, 80);
        if (map.countMonsters() == 0) {
            if (status == 0) {
                cm.sendYesNo("good job completing the doom, start next wave?");
            } else if (status == 1) {
                var map = cm.getClient().getChannelServer().getMapFactory().getMap(stage5);
                map.spawnMonsterOnGroundBelow(LifeFactory.getMonster(9420547), bossPos);
                map.spawnMonsterOnGroundBelow(LifeFactory.getMonster(9420548), bossPos);
                map.spawnMonsterOnGroundBelow(LifeFactory.getMonster(9420549), bossPos);
                map.spawnMonsterOnGroundBelow(LifeFactory.getMonster(9420542), bossPos);
                map.spawnMonsterOnGroundBelow(LifeFactory.getMonster(9420543), bossPos);
                map.spawnMonsterOnGroundBelow(LifeFactory.getMonster(9420544), bossPos);
                cm.warpParty(stage5, 0);
                cm.dispose();
            }
        } else {
            cm.sendOk("kill all");
            cm.dispose();
        }
    }
    if (curMap == stage6) {
        status++;
        var map = cm.getClient().getChannelServer().getMapFactory().getMap(stage6);
        if (map.countMonsters() == 0) {
            if (status == 0) {
                cm.sendYesNo("WOW, you have completed the doom, Here is your reward!");
            }
            if (status > 0) {
                cm.gainItem(4310000, 100);
                cm.warpParty(lobby, 0);
                cm.dispose();
            }
        } else {
            cm.sendOk("kill all");
            cm.dispose();
        }
    }
}

// if (cm.canHold(4310000, 1)) {
//     cm.gainItem(4310000, 1);
// } else {
//     cm.getPlayer().dropMessage(1, "Have a ETC slot available for this item."); // 4310000
// }