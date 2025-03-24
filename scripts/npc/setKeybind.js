/**
 * Pio
 * Set previous skills to keybinds
 * @author CPURules
 */

const GameConstants = Java.type('constants.game.GameConstants');
const JobTypes = Java.type('client.Job');
const KeyBinding = Java.type('client.keybind.KeyBinding');
const SkillInfo = Java.type('server.SkillInformationProvider');

var status;
var selectedBranch;
var skillId;

const keyMapStr = new Map(
    [["`", 41],  ["1", 2],  ["2", 3],  ["3", 4],  ["4", 5], 
    ["5", 6],  ["6", 7],  ["7", 8],  ["8", 9],  ["9", 10], 
    ["0", 11],  ["-", 12],  ["=", 13],  ["q", 16],  ["w", 17], 
    ["e", 18],  ["r", 19],  ["t", 20],  ["y", 21],  ["u", 22], 
    ["i", 23],  ["o", 24],  ["p", 25],  ["[", 26],  ["]", 27], 
    ["a", 30],  ["s", 31],  ["d", 32],  ["f", 33], 
    ["g", 34],  ["h", 35],  ["j", 36],  ["k", 37],  ["l", 38], 
    [";", 39],  ["'", 40],  ["shift", 42],  ["z", 44],  ["x", 45], 
    ["c", 46],  ["v", 47],  ["b", 48],  ["n", 49],  ["m", 50], 
    [",", 51],  [".", 52],  ["\\", 43],  ["ctrl", 29],  ["alt", 56], 
    ["space", 57],  ["f1", 59],  ["f2", 60],  ["f3", 61],  ["f4", 62], 
    ["f5", 63],  ["f6", 64],  ["f7", 65],  ["f8", 66],  ["f9", 67], 
    ["f10", 68],  ["f11", 87],  ["f12", 88],  ["ins", 82],  ["home", 71], 
    ["pup", 73],  ["del", 83],  ["end", 79],  ["pdn", 81]]
);

const keyMapInt = new Map(Array.from(keyMapStr, entry => [[entry[1], entry[0]]]));

function start() {
    status = -1;
    action(1, 0, 0);
}

function getName(id) {
    return SkillInfo.getInstance().getName(id);
}

function getJobName(id) {
    return GameConstants.JOB_NAMES.get(Math.floor(id / 10000)) ?? "N/A";
}

function getJobNameByJobId(id) {
    return getJobName(id * 10000); // hacky but oh well
}

function action(mode, type, selection) {
    if (mode == -1 || (mode == 0 && status == 0)) {
        cm.dispose();
        return;
    } else if (mode == 0) {
        status--;
    } else {
        status++;
    }

    var textList = [];
    // skillBranches: Class Category -> [{ First Advance -> Last Advance }]
    var skillBranches = new Map([
        ["Warrior", new Map([[JobTypes.SPEARMAN, JobTypes.DARKKNIGHT], [JobTypes.FIGHTER, JobTypes.HERO], [JobTypes.PAGE, JobTypes.PALADIN], [JobTypes.DAWNWARRIOR1, JobTypes.DAWNWARRIOR4]])],
        ["Magician", new Map([[JobTypes.IL_WIZARD, JobTypes.IL_ARCHMAGE], [JobTypes.FP_WIZARD, JobTypes.FP_ARCHMAGE], [JobTypes.CLERIC, JobTypes.BISHOP], [JobTypes.BLAZEWIZARD1, JobTypes.BLAZEWIZARD4]])],
        ["Bowman", new Map([[JobTypes.HUNTER, JobTypes.BOWMASTER], [JobTypes.CROSSBOWMAN, JobTypes.MARKSMAN], [JobTypes.WINDARCHER1, JobTypes.WINDARCHER4]])],
        ["Thief", new Map([[JobTypes.ASSASSIN, JobTypes.NIGHTLORD], [JobTypes.BANDIT, JobTypes.SHADOWER], [JobTypes.NIGHTWALKER1, JobTypes.NIGHTWALKER4]])],
        ["Pirate", new Map([[JobTypes.BRAWLER, JobTypes.BUCCANEER], [JobTypes.GUNSLINGER, JobTypes.CORSAIR], [JobTypes.THUNDERBREAKER1, JobTypes.THUNDERBREAKER4]])]
    ]);
    var skillBranchKeys = Array.from(skillBranches.keys());

    var jobBranches = [
        [JobTypes.HERO, JobTypes.PALADIN, JobTypes.DARKKNIGHT, JobTypes.DAWNWARRIOR4],
        [JobTypes.FP_ARCHMAGE, JobTypes.IL_ARCHMAGE, JobTypes.BISHOP, JobTypes.BLAZEWIZARD4],
        [JobTypes.BOWMASTER, JobTypes.MARKSMAN, JobTypes.WINDARCHER4],
        [JobTypes.NIGHTLORD, JobTypes.SHADOWER, JobTypes.NIGHTWALKER4],
        [JobTypes.BUCCANEER, JobTypes.CORSAIR, JobTypes.THUNDERBREAKER4],
        [JobTypes.ARAN4]
    ];

    if (status == 0) {
        textList.push("Which job tree do you want to view skills for?\r\n");
        for (var i = 0; i < skillBranchKeys.length; i++) {
            textList.push("#L" + i + "# " + skillBranchKeys[i] + "#l\r\n");
        }

        cm.sendSimple(textList.join(""));
    }
    else if (status == 1) {
        if (mode == 1) {
            selectedBranch = skillBranchKeys[selection];
        }

        textList.push("Which " + selectedBranch + " sub-tree do you want to view skills for?\r\n");
        var subBranchKeys = Array.from(skillBranches.get(selectedBranch).keys())
        
        for (var i = 0; i < subBranchKeys.length; i++) {
            textList.push("#L" + i + "# " + getJobNameByJobId(subBranchKeys[i].getId()) + "#l\r\n");
        }

        cm.sendSimple(textList.join(""));
    }
    else if (status == 2) {
        var subBranch = Array.from(skillBranches.get(selectedBranch).keys())[selection];
        var jobBranch = skillBranches.get(selectedBranch).get(subBranch);
        var allSkills = Array.from(cm.getPlayer().getSkills().keySet());
        var displaySkills = allSkills.filter((skill) => !GameConstants.isGMSkills(skill.getId()) && !skill.isBeginnerSkill()
                                                        && GameConstants.isInJobTree(skill.getId(),  jobBranch.getId()));
        // filter out passive skills:
        // thousands place is 0
        displaySkills = displaySkills.filter((skill) => (Math.floor(skill.getId() / 1000) % 10 > 0));

        // Sort by name, and then by skill ID
        displaySkills.sort((s1, s2) => getName(s1.getId()).localeCompare(getName(s2.getId())) || (s1.getId() - s2.getId()));

        if (displaySkills.length == 0) {
            textList.push("It looks like you don't have any skills in this category...");
            cm.sendOk(textList.join("\r\n"));
            cm.dispose();
            return;
        }
        
        textList.push("Which " + getJobNameByJobId(subBranch.getId()) + " skill would you like to bind?\r\n\r\n");

        for (var i = 0; i < displaySkills.length; i++) {
            var skill = displaySkills[i];
            textList.push("#L" + skill.getId() + "##s" + skill.getId() + "# #q" + skill.getId() + "##l\r\n");
        }

        cm.sendSimple(textList.join(""));
    }
    else if (status == 3) {
        skillId = selection;

        cm.sendGetText("What key do you want to bind #s" + skillId + "# #q" + skillId + "# to?\r\n");
    }
    else if (status == 4) {
        const PacketCreator = Java.type('tools.PacketCreator');
        var key = cm.getText().toLowerCase();
        var player = cm.getPlayer();
        var res = keyMapStr.get(key);
        player.changeKeybinding(keyMapStr.get(key), new KeyBinding(1, skillId));
        player.getClient().sendPacket(PacketCreator.getKeymap(player.getKeymap()));
        player.yellowMessage(getName(skillId) + " has been assigned to to the '" + key + "' key!");
        textList.push("All set, " + getName(skillId) + " has been assigned!");
        cm.sendOk(textList.join(""));
    }
    else {
        cm.dispose();
    }
}