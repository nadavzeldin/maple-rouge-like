package client.command.commands.gm0;

import client.AscensionConstants;
import client.Character;
import client.Client;
import client.command.Command;
import client.inventory.manipulator.InventoryManipulator;

import static constants.id.MapId.GM_MAP;


public class StylistCommand extends Command {
    {
        setDescription("enable to teleport");
    }
    @Override
    public void execute(Client c, String[] params) {
        Character player = c.getPlayer();
        if (player.accountExtraDetails.getAscension().contains(AscensionConstants.Names.STYLIST))
            player.changeMap(GM_MAP);
        else
        {
            player.yellowMessage("Only " + AscensionConstants.Names.STYLIST + " is can go to this map");
        }
    }
    
}
