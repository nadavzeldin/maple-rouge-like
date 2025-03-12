package client.command.commands.gm0;

import client.Client;
import client.command.Command;

public class GetMacroCommand extends Command {
    {
        setDescription("Get the macro/s command for the specified slot.");
    }

    @Override
    public void execute(Client client, String[] params) {
        // print the all the macros, if there is a number print only this slot
        // use the command !getmacro [<slot>] e.g. @getmacro 1
        if (params.length == 0) {
            String[] macros = client.getPlayer().getUserMacros();
            if (macros != null) {
                for (int i = 0; i < macros.length; i++) {
                    client.getPlayer().yellowMessage("Macro " + i+1 + ": " + macros[i] != null ? macros[i] : "Empty");
                }
            } else {
                client.getPlayer().yellowMessage("No macros found.");
            }
        } else {
            int slot = Integer.parseInt(params[0]);
            String[] macros = client.getPlayer().getUserMacros();
            if (macros != null && macros.length > slot) {
                client.getPlayer().yellowMessage("Macro " + slot + ": " + macros[slot]);
            } else {
                client.getPlayer().yellowMessage("No macro found for slot " + slot);
            }
        }
    }

    
    
}
