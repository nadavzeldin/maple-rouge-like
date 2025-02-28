// Map constants
var lobby   = 980000000;
var stage1  = 980000100;
var stage2  = 980000200;
var stage3  = 980000300;
var stage4  = 980000400;
var stage5  = 980000500;
var stage6  = 980000600;

var curMap;
var status = -1;

// Retrieve map object by ID
function getMapById(mapId) {
    return cm.getClient().getChannelServer().getMapFactory().getMap(mapId);
}

var LifeFactory = Java.type('server.life.LifeFactory');
var Point = Java.type('java.awt.Point');

// Configuration for stages handled by this script
var stageConfig = {
    [stage2]: {
        nextStage: stage3,
        bossPos: new Point(201, 80),
        message: "good job completing the doom, start next wave?",
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
        bossPos: new Point(190, 80),
        message: "good job completing the doom, start next wave?",
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
        // For stage6, the "nextStage" is the lobby, and instead of spawning, we give a reward.
        nextStage: lobby,
        message: "WOW, you have completed the doom, Here is your reward!",
        reward: function() {
            cm.gainItem(4310000, 100);
        }
    }
};

function start() {
    status = -1;
    curMap = cm.getMapId();
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode < 1) {
        cm.dispose();
        return;
    }

    // Check if this stage is configured for handling
    var config = stageConfig[curMap];
    if (!config) {
        cm.dispose();
        return;
    }

    // Ensure the current map is clear before proceeding
    var currentMap = getMapById(curMap);
    if (currentMap.countMonsters() > 0) {
        cm.sendOk("kill all");
        cm.dispose();
        return;
    }

    status++;

    // At first prompt, ask the player to confirm starting the next wave or reward
    if (status === 0) {
        cm.sendYesNo(config.message);
        return;
    }
    // Upon confirmation, process the next stage or reward logic
    else if (status === 1) {
        // If a spawn function is provided, execute it on the next map
        if (config.spawn) {
            var nextMap = getMapById(config.nextStage);
            config.spawn(nextMap);
        }
        // If a reward function is provided, execute it (applies for stage6)
        if (config.reward) {
            config.reward();
        }
        // Warp the party to the next stage (or lobby)
        cm.warpParty(config.nextStage, 0);
        cm.dispose();
        return;
    }
}
