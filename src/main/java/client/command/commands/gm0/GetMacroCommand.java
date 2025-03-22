package client.command.commands.gm0;

import client.Client;
import client.command.Command;

public class GetMacroCommand extends Command {
    {
        setDescription("Get the macro/s command for the specified slot.");
    }

    @Override
    public void execute(Client client, String[] params) {
        if (params.length == 0) {
            String[] macros = client.getPlayer().getUserMacros();
            if (macros != null) {
                for (int i = 0; i < macros.length; i++) {
                    if (macros[i] == null) {
                        client.getPlayer().yellowMessage("Macro " + String.valueOf(i+1) + ": Empty");
                        continue;
                    }
                    String macro = "Macro " + String.valueOf(i+1) + ": " + macros[i];
                    client.getPlayer().yellowMessage(macro);
                }
            } else {
                client.getPlayer().yellowMessage("No macros found.");
            }
        } else {
            int slot = Integer.parseInt(params[0]);
            String[] macros = client.getPlayer().getUserMacros();
            if (macros != null && macros.length > slot) {
                client.getPlayer().yellowMessage("Macro " + slot + ": " + macros[slot-1]);
            } else {
                client.getPlayer().yellowMessage("No macro found for slot " + slot);
            }
        }
    }

    
    
}
