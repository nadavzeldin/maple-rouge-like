package client.command.commands.gm0;

import client.Client;
import client.command.Command;

public class GamesFeatureNPCCommand extends Command {
    {
        setDescription("open merge NPC");
    }

    @Override
    public void execute(Client c, String[] params) {
        c.getAbstractPlayerInteraction().openNpc(2007, "GameFeatures");
    }
}