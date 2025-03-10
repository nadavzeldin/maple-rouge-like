package client.command.commands.gm0;

import client.Client;
import client.command.Command;
import server.maps.MapleMap;

public class SetMacroCommand extends Command {
    {
        setDescription("Set up to 5 macros to use with facial expression.");
    }

    @Override
    public void execute(Client c, String[] params) {
        // use the command !macro<slot> <command/s> e.g. @macro1 @loot @sell equip 5
        // take the command and save it to the macro table in the sql
        System.out.println("The params are: " + params);
        // first check if the command is valid
        if (params.length < 2) {
            c.getPlayer().yellowMessage("Syntax: !setmacro <slot> <-><command/s>");
            return;
        }

        // get the slot
        int slot = Integer.parseInt(params[0]);
        if (slot < 0 || slot > 5) {
            c.getPlayer().yellowMessage("Invalid slot.");
            return;
        }

        // save the command to the macro table in the sql
        String command = "";
        for (int i = 1; i < params.length; i++) {
            command += params[i] + " ";
        }
        // replace - with @
        command = command.replace("-", "@");
        System.out.println("The command is: " + command);
        c.getPlayer().updateMacro(slot, command);
    }
    
}
