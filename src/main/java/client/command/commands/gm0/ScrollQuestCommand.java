package client.command.commands.gm0;

import client.Character;
import client.Client;
import client.command.Command;
import constants.id.NpcId;

public class ScrollQuestCommand extends Command {
    {
        setDescription("Start the daily scroll quest");
    }

    @Override
    public void execute(Client c, String[] params) {
        Character chr = c.getPlayer();
        chr.getAbstractPlayerInteraction().openNpc(9010000, "dailyScrollQuest");
    }
}