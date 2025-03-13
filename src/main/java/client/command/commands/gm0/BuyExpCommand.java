package client.command.commands.gm0;

import client.Client;
import client.command.Command;
import client.inventory.manipulator.InventoryManipulator;
import client.Character;


public class BuyExpCommand extends Command {
    {
        setDescription("Buy 2 hour 3x EXP for 4000 nx.");
    }
    @Override
    public void execute(Client c, String[] params) {
        Character player = c.getPlayer();
        int cash =  player.getCashShop().getCash(1);
        if (cash < 4000) {
            player.dropMessage(5, "You do not have enough NX to buy EXP.");
            return;
        }
        else{
            player.getCashShop().gainCash(1, -4000);
            InventoryManipulator.addById(c, 5211060, (short) 1, player.getName(), -1, (short) 0, -1);
            player.dropMessage(5, "You have bought 2 hour of 3x EXP.");
        }
    }
    
}
