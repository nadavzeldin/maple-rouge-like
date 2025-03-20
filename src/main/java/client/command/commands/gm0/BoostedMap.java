package client.command.commands.gm0;

import client.Character;
import client.Client;
import client.command.Command;
import server.maps.MapleMap;

public class BoostedMap extends Command {
    {
        setDescription("Warp to boosted map.");
    }

    // Change to static field
    private static int boostedMapId = 1;

    // Change to static method
    public static void setBoostedMap(int mapId) {
        boostedMapId = mapId;
    }

    @Override
    public void execute(Client c, String[] params) {
        Character player = c.getPlayer();

        if (!player.isAlive()) {
            player.dropMessage(1, "You cannot use this command while dead.");
            return;
        }
        MapleMap targetMap = c.getChannelServer().getMapFactory().getMap(boostedMapId);

        if (targetMap == null) {
            player.dropMessage(1, "Error: Could not load the selected random map.");
            return;
        }

        // Warp to the map
        player.saveLocationOnWarp();
        player.changeMap(targetMap, targetMap.getRandomPlayerSpawnpoint());

        player.yellowMessage("You have been warped to boosted map: " + boostedMapId);
    }
}