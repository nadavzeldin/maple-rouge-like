/*
    This file is part of the HeavenMS MapleStory Server, commands OdinMS-based
    Copyleft (L) 2016 - 2019 RonanLana

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation version 3 as published by
    the Free Software Foundation. You may not use, modify or distribute
    this program under any other version of the GNU Affero General Public
    License.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

/*
   @Author: Arthur L - Refactored command content into modules
*/
package client.command.commands.gm0;

import client.AccountExtraDetails;
import client.AscensionConstants;
import client.Character;
import client.Client;
import client.command.Command;
import com.fasterxml.jackson.databind.ObjectMapper;
import constants.id.MapId;
import net.server.Server;
import tools.DatabaseConnection;
import tools.PacketCreator;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;

import static java.util.concurrent.TimeUnit.MINUTES;

public class WorldChatCommand extends Command {
    {
        setDescription("@world + your message");
    }

    @Override
    public void execute(Client c, String[] params) {
        Character player = c.getPlayer();
        if (params.length < 1) {
            player.yellowMessage("@world + your message");
            return;
        }

        try {
            StringBuilder messageBuilder = new StringBuilder();
            for (String param : params) {
                messageBuilder.append(param).append(" ");
            }
            String message = messageBuilder.toString().trim();

            // Added space between "Player" and player.getName()
            Server.getInstance().broadcastMessage(0,
                    PacketCreator.serverNotice(6,
                            "Scania | " + player.getName() + " - " + message)
            );
        } catch (Exception e) { // Changed to catch all exceptions
            player.yellowMessage("Failed to send message: " + e.getMessage());
            e.printStackTrace(); // Added to log the full exception
        }
    }
}