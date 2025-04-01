/*
 * Command to launch resource storage manager NPC (T-1337)
 * @Author: CPURules
*/
package client.command.commands.gm0;

import client.Character;
import client.Client;
import client.command.Command;
import constants.id.MapId;

import static client.AscensionConstants.Names.HEIRLOOM;

public class StorageCommand extends Command {
    {
        setDescription("storage");
    }

    @Override
    public void execute(Client c, String[] params) {
        Character player = c.getPlayer();
        if (player.getMap().getId() == MapId.JAIL)
        {
            if (!player.accountExtraDetails.getAscension().contains(HEIRLOOM))
                player.yellowMessage("can't use this command in jail.");
        }

        c.getAbstractPlayerInteraction().openNpc(9030100);
    }
}
