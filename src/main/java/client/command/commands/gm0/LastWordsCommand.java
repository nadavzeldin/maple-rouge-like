package client.command.commands.gm0;

import client.Character;
import client.Client;
import client.command.Command;
import net.server.world.World;

public class LastWordsCommand extends Command {
    {
        setDescription("Set your last words before dying.");
    }

    @Override
    public void execute(Client c, String[] params) {
        Character player = c.getPlayer();

        if (params.length < 1) {
            player.yellowMessage("Usage: !lastwords <message>");
            return;
        }

        String lastWords = String.join(" ", params);
        //player.setLastWords(lastWords); // Store message in player object

        player.yellowMessage("Your last words have been set to: \"" + lastWords + "\".");
    }
}
