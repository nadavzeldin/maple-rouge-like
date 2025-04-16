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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.concurrent.TimeUnit.MINUTES;

public class AscensionCommand extends Command {

    // Define all ascension types in a single map with their corresponding indices
    private static final Map<Integer, String> ASCENSION_TYPES = new LinkedHashMap<>();

    static {
        // Populate the map with all available ascension types
        ASCENSION_TYPES.put(1, AscensionConstants.Names.HOARDER);
        ASCENSION_TYPES.put(2, AscensionConstants.Names.RESILIENT);
        ASCENSION_TYPES.put(3, AscensionConstants.Names.LUCKY);
        ASCENSION_TYPES.put(4, AscensionConstants.Names.BLACKSMITH);
        ASCENSION_TYPES.put(5, AscensionConstants.Names.EARLYBIRD);
        ASCENSION_TYPES.put(6, AscensionConstants.Names.INFINITE);
        ASCENSION_TYPES.put(7, AscensionConstants.Names.STYLIST);
        ASCENSION_TYPES.put(8, AscensionConstants.Names.RISKTAKER);
        ASCENSION_TYPES.put(9, AscensionConstants.Names.HEIRLOOM);
    }

    {
        setDescription("Show ascension status.");
    }

    @Override
    public void execute(Client c, String[] params) {
        int type;
        Character player = c.getPlayer();
        if (params.length < 1) {
            showSyntaxMessage(player);
            return;
        }

        try {
            type = Integer.parseInt(params[0]);
        } catch (NumberFormatException e) {
            showSyntaxMessage(player);
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

        if (type < 0 || type > ASCENSION_TYPES.size()) {
            showSyntaxMessage(player);
            return;
        }

        String ascensionType = ASCENSION_TYPES.get(type);

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

        if(!player.isGM())
        {
            player.addJailExpirationTime(MINUTES.toMillis(Long.MAX_VALUE));
            player.changeMap(MapId.JAIL);
        }

        // Determine the display name for broadcast message
        String displayName = ascensionType;
        if (ascensionType.equals(AscensionConstants.Names.INFINITE)) {
            displayName = getHighestInfiniteAscension(ascensions);
        }

        Server.getInstance().broadcastMessage(0,
                PacketCreator.serverNotice(6,
                        "Player " + player.getName() +
                                " was ascended [" + displayName + "] well done!")
        );
    }

    private void showSyntaxMessage(Character player) {
        StringBuilder syntax = new StringBuilder("Syntax: @ascend <0=ShowMyAscensions");

        for (Map.Entry<Integer, String> entry : ASCENSION_TYPES.entrySet()) {
            syntax.append(", ").append(entry.getKey()).append("=").append(entry.getValue());
        }

        syntax.append(">");
        player.yellowMessage(syntax.toString());
    }

    private String getHighestInfiniteAscension(List<String> ascensions) {
        // Pattern to match Infinite(#)
        Pattern pattern = Pattern.compile(AscensionConstants.Names.INFINITE + "\\((\\d+)\\)");
        int highestNum = 0;
        String highestInfinite = AscensionConstants.Names.INFINITE;

        for (String asc : ascensions) {
            Matcher matcher = pattern.matcher(asc);
            if (matcher.matches()) {
                int num = Integer.parseInt(matcher.group(1));
                if (num > highestNum) {
                    highestNum = num;
                    highestInfinite = asc;
                }
            }
        }

        return highestInfinite;
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