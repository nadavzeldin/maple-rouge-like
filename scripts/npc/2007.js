function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == -1) {
        cm.sendNext("Enjoy your trip.");
        cm.dispose();
    } else {
        if (status == 0 && mode == 0) {
            cm.sendNext("Enjoy your trip.");
            cm.dispose();
        }
        if (mode == 1) {
            status++;
        } else {
            status--;
        }
        if (status == 0) {
            cm.sendYesNo("#b[Welcome to ZeldaMS]#k\r\n\r\n" +
                "This server have few unique features:\r\n\r\n" +
                "#r1.#k If you die, you character sent to jail for #r10 years#k (delete/create new one)\r\n\r\n" +
                "#r2.#k You get #brandom job advancement#k each #b10 levels#k,\r\ncan be same job or any random explorer.\r\n" +
                "The job will be in same tier as you are (first job ..)\r\n\r\n" +
                "#r3.#k Your #bbeginner nimble feet skill#k has been modify and greatly enhance\r\n\r\n" +
                "#r4.#k A mysterious #bLoot Candle#k occasionally appears in maps,\r\ndropping #rvaluable scrolls#k and a #bunique potion#k that gives\r\n#rpermanent +1 to all stats#k for lucky adventurers\r\n\r\n" +
                "#r5.#k Visit #bFredrick in FM#k to merge your equipment using #rBlack Loud Machine#k\r\nmaking them significantly stronger!\r\n\r\n" +
                "#r6.#k Use #b@help#k to see other basic features\r\n\r\n" +
                "Have fun, would you like to skip the tutorials and head straight to #bLith Harbor#k?");
        } else if (status == 1) {
            cm.warp(104000000, 0);
            cm.dispose();
        }
    }
}