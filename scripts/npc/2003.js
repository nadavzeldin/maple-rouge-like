var status = -1;
var selection = -1;

function start() {
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == -1) {
        cm.dispose();
        return;
    }

    if (mode == 0) {
        cm.dispose();
        return;
    }

    status++;

    if (status == 0) {
        cm.sendSimple("Welcome, brave Mapler! What would you like to know about?\r\n\r\n#L0##b1. Legends#k#l\r\n#L1##b2. Ascension#k#l");
    } else if (status == 1) {
        if (selection == 0) {
            cm.sendOk("#eLegends Never Die#k#n\r\n\r\nPlay testing achievers which manage to reach level 200 first are:\r\n\r\n#b1. #eChrollo#n#k\r\n#b2. #eKeen#n#k\r\n#b3. #eMiguel#n#k\r\n\r\n#d#eCongrads you legends!#k#n");
            cm.dispose();
        } else if (selection == 1) {
            cm.sendOk("#eAscension Powers#k#n\r\n\r\n#dUpon reaching level 200, you have unlocked new command:#k\r\n#r@ascension <option>#k\r\n\r\n#eHere are the options will be soon added#n");
            cm.dispose();
        }
    } else {
        cm.dispose();
    }
}