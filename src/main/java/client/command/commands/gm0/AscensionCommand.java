package client.command.commands.gm0;

import client.AccountExtraDetails;
import client.Achievement;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.util.concurrent.TimeUnit.MINUTES;

public class AscensionCommand extends Command {
    {
        setDescription("Show ascension status.");
    }

    @Override
    public void execute(Client c, String[] params) {
        int type;
        Character player = c.getPlayer();
        if (params.length < 1) {
            player.yellowMessage("Syntax: @ascend <0=ShowMyAscensions, 1=Hoarder, 2=Resilient, 3=Lucky, 4=Blacksmith, 5=EarlyBird, 6=Infinite>");
            return;
        }

        try {
            type = Integer.parseInt(params[0]);
        } catch (NumberFormatException e) {
            player.yellowMessage("Invalid number format. Use 0 to show ascensions, 1 for Hoarder, 2 for Resilient, 3 for Lucky, 4 for Blacksmith, 5 for EarlyBird or 6 for Infinite");
            return;
        }

        AccountExtraDetails details = player.accountExtraDetails;
        if (details == null || details.getAchievements() == null) {
            player.yellowMessage("No achievements found.");
            return;
        }

        List<String> ascensions = details.getAscension();
        if (ascensions == null) {
            ascensions = new ArrayList<>();
            details.setAscension(ascensions);
        }

        if (type == 0) {
            player.yellowMessage("My ascensions are: " + ascensions);
            return;
        }

        if (player.getLevel() < 200) {
            player.yellowMessage("Require level 200 at least.");
            return;
        }

        if (type < 0 || type > 6) {
            player.yellowMessage("Invalid ascension type. Use 1 for Hoarder, 2 for Resilient, 3 for Lucky, 4 for Blacksmith, 5 for EarlyBird or 6 for Infinite.");
            return;
        }

        String ascensionType;
        if (type == 1) {
            ascensionType = AscensionConstants.Names.HOARDER;
        } else if (type == 2) {
            ascensionType = AscensionConstants.Names.RESILIENT;
        } else if (type == 3) {
            ascensionType = AscensionConstants.Names.LUCKY;
        } else if (type == 4) {
            ascensionType = AscensionConstants.Names.BLACKSMITH;
        } else if (type == 5) {
            ascensionType = AscensionConstants.Names.EARLYBIRD;
        } else {
            ascensionType = AscensionConstants.Names.INFINITE;
        }

        // Special handling for INFINITE ascension type
        if (ascensionType.equals(AscensionConstants.Names.INFINITE)) {
            handleInfiniteAscension(player, ascensions);
        } else {
            // Normal ascension handling
            if (ascensions.contains(ascensionType)) {
                player.yellowMessage("Only " + AscensionConstants.Names.INFINITE + " is stackable");
                return;
            }

            ascensions.add(ascensionType);
        }

        saveAscensions(player, details);

        player.addJailExpirationTime(MINUTES.toMillis(Long.MAX_VALUE));
        player.changeMap(MapId.JAIL);

        // Determine the display name for broadcast message
        String displayName = ascensionType;
        if (ascensionType.equals(AscensionConstants.Names.INFINITE)) {
            // Find the Infinite with the highest number
            Pattern pattern = Pattern.compile(AscensionConstants.Names.INFINITE + "\\((\\d+)\\)");
            int highestNum = 0;

            for (String asc : ascensions) {
                Matcher matcher = pattern.matcher(asc);
                if (matcher.matches()) {
                    int num = Integer.parseInt(matcher.group(1));
                    if (num > highestNum) {
                        highestNum = num;
                        displayName = asc;
                    }
                }
            }
        }

        Server.getInstance().broadcastMessage(0,
                PacketCreator.serverNotice(6,
                        "Player " + player.getName() +
                                " was ascended [" + displayName + "] well done!")
        );
    }

    private void handleInfiniteAscension(Character player, List<String> ascensions) {
        // Pattern to match Infinite(#)
        Pattern pattern = Pattern.compile(AscensionConstants.Names.INFINITE + "\\((\\d+)\\)");

        // Find the highest Infinite number
        int highestNum = 0;

        // Remove previous Infinite ascensions while finding the highest number
        List<String> toRemove = new ArrayList<>();

        for (String asc : ascensions) {
            Matcher matcher = pattern.matcher(asc);
            if (matcher.matches()) {
                toRemove.add(asc);
                int num = Integer.parseInt(matcher.group(1));
                if (num > highestNum) {
                    highestNum = num;
                }
            }
        }

        // Remove all previous Infinite entries
        ascensions.removeAll(toRemove);

        // Add the new Infinite with incremented number
        String newInfinite = AscensionConstants.Names.INFINITE + "(" + (highestNum + 1) + ")";
        ascensions.add(newInfinite);

        player.yellowMessage("Ascended to " + newInfinite);
    }

    private void saveAscensions(Character player, AccountExtraDetails details) {
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement("UPDATE accounts SET extra_details = ? WHERE id = ?")) {

            String updatedJson = new ObjectMapper().writeValueAsString(details);
            ps.setString(1, updatedJson);
            ps.setInt(2, player.getAccountID());
            ps.executeUpdate();

        } catch (Exception e) {
            player.yellowMessage("Error saving ascension: " + e.getMessage());
        }
    }
}