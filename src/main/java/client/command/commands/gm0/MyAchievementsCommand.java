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
import client.Client;
import client.command.Command;

import java.util.List;

public class MyAchievementsCommand extends Command {
    {
        setDescription("Watch my achievements");
    }

    @Override
    public void execute(Client c, String[] params) {
        AccountExtraDetails details = c.getPlayer().accountExtraDetails;
        if (details == null || details.getAchievements() == null) {
            c.getPlayer().yellowMessage("No achievements found.");
            return;
        }

        List<Achievement> achievements = details.getAchievements();
        achievements.sort((a1, a2) -> {
            int statusCompare = a2.getStatus().compareTo(a1.getStatus());
            return statusCompare != 0 ? statusCompare : 0;
        });

        c.getPlayer().yellowMessage("=== Achievement Progress ===");
        for (Achievement achievement : achievements) {
            String status = achievement.getStatus().equals("done") ? "+" : "-";
            String bonus = achievement.getBonus() != null ? " - Reward: " + achievement.getBonus() : "";
            c.getPlayer().yellowMessage(status + " " + achievement.getName() + bonus);
        }
    }
}
