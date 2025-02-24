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
import client.Achievement;
import client.AscensionConstants;
import client.Character;
import client.Client;
import client.command.Command;
import com.fasterxml.jackson.databind.ObjectMapper;
import constants.id.MapId;
import tools.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;

public class AscensionCommand extends Command {
    {
        setDescription("Change language settings.");
    }

    @Override
    public void execute(Client c, String[] params) {
        int type;
        type = Integer.parseInt(params[0]);
        Character player = c.getPlayer();
        if (params.length < 1) {
            player.yellowMessage("Syntax: @ascension <0=ShowMyAscensions, 1=Hoarder, 2=Resilient>");
            return;
        }
        AccountExtraDetails details = player.accountExtraDetails;
        if (details == null || details.getAchievements() == null) {
            player.yellowMessage("No achievements found.");
            return;
        }
        List<String> ascensions = details.getAscension();
        if (type==0)
        {
            player.yellowMessage("My ascensions are: " + ascensions);
            return;
        }

        if (player.getLevel()<200)
        {
            player.yellowMessage("require level 200 at least.");
            return;
        }

        try {
            if (type < 0 || type > 2) {
                player.yellowMessage("Invalid ascension type. Use 1 for Hoarder or 2 for Resilient.");
                return;
            }
        } catch (NumberFormatException e) {
            player.yellowMessage("Invalid number format. Use 1 for Hoarder or 2 for Resilient.");
            return;
        }


        if (ascensions == null) {
            ascensions = new ArrayList<>();
            details.setAscension(ascensions);
        }



        String ascensionType = type == 1 ? AscensionConstants.Names.HOARDER : AscensionConstants.Names.RESILIENT;
        if (!ascensions.contains(ascensionType)) {
            ascensions.add(ascensionType);
        }

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement("UPDATE accounts SET extra_details = ? WHERE id = ?")) {

            String updatedJson = new ObjectMapper().writeValueAsString(details);
            ps.setString(1, updatedJson);
            ps.setInt(2, player.getAccountID());
            ps.executeUpdate();

        } catch (Exception e) {
            player.yellowMessage("Error");
        }
        player.changeMap(MapId.JAIL);

    }
}
