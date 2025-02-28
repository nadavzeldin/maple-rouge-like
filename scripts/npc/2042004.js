// Map constants
var doomLobby= 980000000;
var stage1  = 980000100;
var stage2  = 980000200;
var stage3  = 980000300;
var stage4  = 980000400;
var stage5  = 980000500;
var stage6  = 980000600;

var curMap;
var status = -1;

var LifeFactory = Java.type('server.life.LifeFactory');
var Point = Java.type('java.awt.Point');

// Configuration for stages handled by this script
var stageConfig = {
    [stage2]: {
        prizeId: 4001129,
        prizeAmount: 5,
        nextStage: stage3,
        killAllText: "This is one of my fav bosses, you will kill it to exit.",
        bossPos: new Point(201, 80),
        spawn: function(nextMap) {
            // Only spawn if the next map is clear of monsters
            if (nextMap.countMonsters() === 0) {
                for (var i = 9500172; i < 9500179; i++) {
                    nextMap.spawnMonsterOnGroundBelow(LifeFactory.getMonster(i), this.bossPos);
                }
            }
        }
    },
    [stage4]: {
        nextStage: stage5,
        prizeId: 4001254,
        prizeAmount: 1,
        killAllText: "This is an easy stage, just kill pink bean, that easy!",
        bossPos: new Point(190, 80),
        spawn: function(nextMap) {
            if (nextMap.countMonsters() === 0) {
                var monsterIds = [9420547, 9420548, 9420549, 9420542, 9420543, 9420544];
                for (var i = 0; i < monsterIds.length; i++) {
                    nextMap.spawnMonsterOnGroundBelow(LifeFactory.getMonster(monsterIds[i]), this.bossPos);
                }
            }
        }
    },
    [stage6]: {
        prizeId: 4310000,
        prizeAmount: 100,
        killAllText: "Holy hell, this looks hard! Kill all, because even maple god can't help you here.",
        nextStage: doomLobby,
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

    // Look up the configuration for the current stage
    if (curMap === stage6) {
        if (status == 0) {
            cm.sendYesNo("WOW, you have completed the doom, Here is your reward!" + config.prizeAmount +" #i4310000# #t4310000##l");
        }
        if (status > 0) {
            cm.gainItem(config.prizeId, config.prizeAmount);
            cm.warpParty(doomLobby, 0);
            cm.dispose();
        }
    } else {
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
}

function makeChoices(itemIdToGive, numberOfItemToGive) {
    var result = "good job completing the doom stage, would you like to exit, or delve deeper into the DOOM\r\n\r\n";
    result += "#L" + 0 + "##l I am a manly man or a womanly woman with some chess hair take me deeper!\r\n";
    result += "Exit, you will get: " + numberOfItemToGive + " #L" + 1 + "\n ##v" + itemIdToGive + "##t" + itemIdToGive + "##l\r\n";
    return result;
}
