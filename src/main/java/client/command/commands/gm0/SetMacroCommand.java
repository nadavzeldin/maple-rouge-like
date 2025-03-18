package client.command.commands.gm0;

import client.Client;
import client.command.Command;


public class SetMacroCommand extends Command {
    {
        setDescription("Set 5 macros to use with special potions found in the shop. Set and active when consuming the potion.");
    }

    @Override
    public void execute(Client c, String[] params) {
        System.out.println("The params are: " + params);
        if (params.length < 2) {
            c.getPlayer().yellowMessage("Syntax: !setmacro <slot> <-><command/s>\n e.g. @setmacro 1 -loot -sell equip 5");
            return;
        }

        int slot = Integer.parseInt(params[0]);
        if (slot < 0 || slot > 5) {
            c.getPlayer().yellowMessage("Invalid slot.");
            return;
        }

        String command = "";
        for (int i = 1; i < params.length; i++) {
            command += params[i] + " ";
        }
        command = command.replace("-", "@");
        c.getPlayer().updateMacro(slot, command);
    }
    
}
