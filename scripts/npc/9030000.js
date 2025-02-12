var status = -1;
var selectedType = -1;

function start() {
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == 1) {
        status++;
    } else {
        if (status >= 2) {
            status--;
        } else {
            cm.dispose();
            return;
        }
    }

    if (status == 0) {
        var selStr = "What would you like me to do for you?\r\n";
        selStr += "#L0#Check my merchant items#l\r\n";
        selStr += "#L1#Merge equipment#l\r\n";
        selStr += "#L2#How does merging effect work?#l"; // Moved below "Merge equipment"
        cm.sendSimple(selStr);
    } else if (status == 1) {
        selectedType = selection;
        if (selection == 0) {  // Original Fredrick functionality
            if (!cm.hasMerchant() && cm.hasMerchantItems()) {
                cm.showFredrick();
                cm.dispose();
            } else {
                if (cm.hasMerchant()) {
                    cm.sendOk("You have a Merchant open.");
                } else {
                    cm.sendOk("You don't have any items or mesos to be retrieved.");
                }
                cm.dispose();
            }
        } else if (selection == 1) {  // Forging functionality
            var selStr = "Bring me a #i2280001# #t2280001# and I'll merge your equipment to make them stronger.";
            items = [2280001];  // Black Loud Machine
            for (var i = 0; i < items.length; i++) {
                selStr += "\r\n#L" + i + "##t" + items[i] + "##l";
            }
            cm.sendSimple(selStr);
        } else if (selection == 2) {  // Merging effect explanation
            var explainStr = "Each merge attempt has these possible outcomes:\r\n\r\n";
            explainStr += "#bCommon Effects (Medium Chance)#k\r\n\r\n";

            explainStr += "#dHarry Potter:#k\r\n";
            explainStr += "- Weapon: Increases #rMagic Attack#k\r\n";
            explainStr += "- Armor: Increases #rIntelligence#k\r\n\r\n";

            explainStr += "#dZoro:#k\r\n";
            explainStr += "- Weapon: Increases #rAttack Power#k\r\n";
            explainStr += "- Armor: Increases #rStrength#k\r\n\r\n";

            explainStr += "#dLegolas:#k\r\n";
            explainStr += "- Weapon: Increases #rAttack Power#k\r\n";
            explainStr += "- Armor: Increases #rDexterity#k\r\n\r\n";

            explainStr += "#dNinja:#k\r\n";
            explainStr += "- Weapon: Increases #rAttack Power#k\r\n";
            explainStr += "- Armor: Increases #rLuck#k\r\n\r\n";

            explainStr += "#dBolder:#k\r\n";
            explainStr += "- Weapon: Increases #rHP#k\r\n";
            explainStr += "- Armor: Increases #rDefense#k\r\n\r\n";

            explainStr += "#dMyst:#k\r\n";
            explainStr += "- Weapon: Increases #rMP#k\r\n";
            explainStr += "- Armor: Increases #rMagic Defense#k\r\n\r\n";

            explainStr += "#rLegendary Effect:#k\r\n";
            explainStr += "#rUnkillable#k (Extremely Rare)\r\n";
            explainStr += "- Special Effect: Upon death, you'll be teleported to Henesys.\r\n";
            explainStr += "  Your life is saved, but all equipment will be removed.\r\n\r\n";

            explainStr += "#rNote: All stat bonuses scale with job advancement level!#k";

            cm.sendOk(explainStr);
            cm.dispose();


        }
    } else if (status == 2) {
        if (selectedType == 1) {  // Forging process
            if (!cm.haveItem(2280001, 1)) {
                cm.sendOk("You need a #i2280001# #t2280001# to merge equipment.");
                cm.dispose();
                return;
            }

            cm.gainItem(2280001, -1); // Remove the Black Loud Machine
            const MergeCommand = Java.type('client.command.commands.gm6.MergeCommand');
            const processor = new MergeCommand();
            processor.execute(cm.getClient(), ["@merge"]);
            cm.sendOk("Your equipment has been successfully merged and enhanced!");
            cm.dispose();
        }
    }
}
