/*
 * Command to launch resource storage manager NPC (T-1337)
 * @Author: CPURules
*/
package client.command.commands.gm0;

import client.Client;
import client.command.Command;
import constants.id.NpcId;

public class SkillBindCommand extends Command {
    {
        setDescription("zzzzzz");
    }

    @Override
    public void execute(Client c, String[] params) {
        c.getAbstractPlayerInteraction().openNpc(10000, "setKeybind");
    }
}
