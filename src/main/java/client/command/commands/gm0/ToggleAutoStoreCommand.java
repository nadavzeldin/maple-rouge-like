package client.command.commands.gm0;

import client.Client;
import client.command.Command;
import client.inventory.manipulator.InventoryManipulator;
import client.Character;


public class ToggleAutoStoreCommand extends Command {
    {
        setDescription("Toggles auto storage of resource items on loot.");
    }
    @Override
    public void execute(Client c, String[] params) {
        Character player = c.getPlayer();
        
        try {
            player.toggleAutoStoreOnLoot();
            player.dropMessage(5, "Toggled resource auto storage.  Currently: "
                                        + (player.accountExtraDetails.shouldAutoStoreOnLoot() ? "ON" : "OFF"));
        } catch (Exception e) {
            player.dropMessage(5, "Unable to toggle resource auto storage setting.");
        }
        
    }
    
}
