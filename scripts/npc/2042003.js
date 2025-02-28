// Stage constants
var doomLobby = 980000000;
var stage1 = 980000100;
var stage2 = 980000200;
var stage3 = 980000300;
var stage4 = 980000400;
var stage5 = 980000500;
var stage6 = 980000600;

var curMap;
var status = -1;

// Configuration for each stage that requires handling
var stageConfig = {
    [stage1]: {
        prizeId: 4310000,
        prizeAmount: 1,
        nextStage: stage2,
        killAllText: "Dang, just kill the Mush mom, this is the first stage, go away!",
        spawn: function(map) {
            const LifeFactory = Java.type('server.life.LifeFactory');
            const Point = Java.type('java.awt.Point');
            var boss = LifeFactory.getMonster(8500001);
            var bossPos = new Point(201, 80);
            map.spawnMonsterOnGroundBelow(boss, bossPos);
        }
    },
    [stage3]: {
        nextStage: stage4,
        prizeId: 4310000,
        prizeAmount: 25,
        killAllText: "Looks like you have not killed all enemies here, while I understand why, I will not let you exit yet",
        spawn: function(map) {
            const LifeFactory = Java.type('server.life.LifeFactory');
            const Point = Java.type('java.awt.Point');
            var boss = LifeFactory.getMonster(8820001);
            var bossPos = new Point(201, 80);
            map.spawnMonsterOnGroundBelow(boss, bossPos);
        }
    },
    [stage5]: {
        nextStage: stage6,
        prizeId: 4310000,
        prizeAmount: 50,
        killAllText: "I'll be honest, I didn't think you would get this far, but kill all to exit!",
        spawn: function(map) {
            const LifeFactory = Java.type('server.life.LifeFactory');
            const Point = Java.type('java.awt.Point');
            var spawnPoint = new Point(190, 80);
            // Spawn a group of monsters via a loop
            for (var i = 9300291; i <= 9300294; i++) {
                map.spawnMonsterOnGroundBelow(LifeFactory.getMonster(i), spawnPoint);
            }
            // Spawn additional monsters at specific points
            map.spawnMonsterOnGroundBelow(LifeFactory.getMonster(8800000), new Point(150, 80));
            map.spawnMonsterOnGroundBelow(LifeFactory.getMonster(8820001), new Point(50, 80));
            map.spawnMonsterOnGroundBelow(LifeFactory.getMonster(9400121), new Point(50, 80));
            map.spawnMonsterOnGroundBelow(LifeFactory.getMonster(9400300), new Point(50, 80));
            map.spawnMonsterOnGroundBelow(LifeFactory.getMonster(9400014), new Point(50, 80));
        }
    }
};

// Helper function to get the map object by ID
function getMapById(mapId) {
    return cm.getClient().getChannelServer().getMapFactory().getMap(mapId);
}

function start() {
    status = -1;
    curMap = cm.getMapId();
    action(1, 0, 0);
}

function action(mode, type, selection) {
    // If the player cancels, dispose immediately
    if (mode < 1) {
        cm.dispose();
        return;
    }
    // Look up the configuration for the current stage
    // Retrieve the current map object
    var currentMap = getMapById(curMap);

    var config = stageConfig[curMap];
    if (!config) {
        // If no configuration exists for this map, simply dispose
        cm.dispose();
        return;
    }

    // Check if there are still monsters left; if so, prompt the player to finish them first
    if (currentMap.countMonsters() > 0) {
        cm.sendOk(config.killAllText);
        cm.dispose();
        return;
    }

    // Increment the conversation status
    status++;

    // At the initial status, ask the player if they're ready to start the next wave
    if (status === 0) {
        var doomChoice = makeChoices(config.prizeId, config.prizeAmount); // 4001129 is maple coin
        cm.sendSimple(doomChoice);
    }
    // On confirmation, spawn the next wave and move the party to the next map
    else if (status === 1) {
        if (selection === 0) {
            var nextMap = getMapById(config.nextStage);
            // Only spawn monsters if the next map is currently clear
            if (nextMap.countMonsters() === 0) {
                config.spawn(nextMap);
            }
            cm.warpParty(config.nextStage, 0);
        } else {
            if (cm.canHold(config.prizeId)) {
                cm.gainItem(config.prizeId, config.prizeAmount);
                cm.warpParty(doomLobby, 0);
            } else {
                cm.sendOk("Make sure you have a free spot in your ETC inventory.");
            }
        }
        cm.dispose();
    }
}

function makeChoices(itemIdToGive, numberOfItemToGive) {
    var result = "good job completing the doom stage, would you like to exit, or delve deeper into the DOOM\r\n\r\n";
    result += "#L" + 0 + "##l I am a manly man or a womanly woman with some chess hair take me deeper!\r\n";
    result += "Exit, you will get: " + numberOfItemToGive + " #L" + 1 + "\n ##v" + itemIdToGive + "##t" + itemIdToGive + "##l\r\n";
    return result;
}