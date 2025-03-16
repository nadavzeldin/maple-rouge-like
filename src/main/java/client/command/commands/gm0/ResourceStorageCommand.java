/*
 * Command to launch resource storage manager NPC (T-1337)
 * @Author: CPURules
*/
package client.command.commands.gm0;

import client.Client;
import client.command.Command;
import constants.id.NpcId;

public class ResourceStorageCommand extends Command {
    {
        setDescription("Open resource storage (scrolls, ores, merge coins)");
    }

    @Override
    public void execute(Client c, String[] params) {
        c.getAbstractPlayerInteraction().openNpc(NpcId.PLAYER_RESOURCE_STORAGE, "resourceStorage");
    }
}
