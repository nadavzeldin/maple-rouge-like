package client.command.commands.gm0;

import client.Client;
import client.command.Command;

public class MergeNPCCommand extends Command {
    {
        setDescription("open merge NPC");
    }

    @Override
    public void execute(Client c, String[] params) {
        c.getAbstractPlayerInteraction().openNpc(9030000, "mergeScript");
    }
}