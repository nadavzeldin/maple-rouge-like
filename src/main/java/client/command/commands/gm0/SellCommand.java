package client.command.commands.gm0;

import client.Character;
import client.Client;
import client.command.Command;
import client.inventory.Inventory;
import client.inventory.InventoryType;
import client.inventory.Item;
import server.ItemInformationProvider;
import server.Shop;
import server.ShopFactory;

import java.util.Arrays;
import java.util.Set;

public class SellCommand extends Command {
    {
        setDescription("Sells all items in an inventory tab, starting from a specific slot or excluding a specific item.");
    }

    @Override
    public void execute(Client c, String[] params) {
        Character player = c.getPlayer();

        if (params.length < 1) {
            player.yellowMessage("Syntax: @sell <tab> [slot number] | @sell exclude <tab> <item_name> | @sell all");
            return;
        }

        String type = params[0].toLowerCase();
        Shop shop = ShopFactory.getInstance().getShop(1337); // GM Shop

        // Handling "exclude" functionality
        if (params.length >= 3 && params[0].equalsIgnoreCase("exclude")) {
            String tab = params[1].toLowerCase();
            String excludeItemName = String.join(" ", Arrays.copyOfRange(params, 2, params.length)).toLowerCase();
            sellExcludingItem(c, shop, player, tab, excludeItemName);
            return;
        }

        // Handling selling everything
        if (type.equals("all")) {
            for (InventoryType inventoryType : InventoryType.values()) {
                if (inventoryType == InventoryType.CASH) continue; // Skip Cash inventory
                sellFromInventory(c, shop, player, inventoryType, (short) 0);
            }
            player.yellowMessage("Sold all applicable items in all tabs.");
            return;
        }

        // Handling selling from a specific slot onwards
        int startSlot = 0;
        if (params.length >= 2) {
            try {
                startSlot = Integer.parseInt(params[1]);
                if (startSlot < 0) {
                    player.yellowMessage("Invalid slot number.");
                    return;
                }
            } catch (NumberFormatException e) {
                player.yellowMessage("Invalid number format for slot.");
                return;
            }
        }

        if (!allTypesAsString.contains(type)) {
            player.yellowMessage("Error: The specified inventory type '" + type + "' does not exist.");
            return;
        }

        InventoryType inventoryType = InventoryType.valueOf(type.toUpperCase());
        sellFromInventory(c, shop, player, inventoryType, (short) startSlot);
    }

    private void sellFromInventory(Client c, Shop shop, Character player, InventoryType inventoryType, short startSlot) {
        Inventory inventory = player.getInventory(inventoryType);
        for (short i = startSlot; i < inventory.getSlotLimit(); i++) {
            Item tempItem = inventory.getItem((byte) i);
            if (tempItem != null) {
                player.yellowMessage("Selling item: " + tempItem.getItemId() + " from slot " + i);
                shop.sell(c, inventoryType, i, tempItem.getQuantity());
            }
        }
        player.yellowMessage("Sold all applicable items in " + inventoryType.name().toLowerCase() + " starting from slot " + startSlot + ".");
    }

    private void sellExcludingItem(Client c, Shop shop, Character player, String type, String excludeItemName) {
        if (!allTypesAsString.contains(type)) {
            player.yellowMessage("Error: The specified inventory type '" + type + "' does not exist.");
            return;
        }

        InventoryType inventoryType = InventoryType.valueOf(type.toUpperCase());
        Inventory inventory = player.getInventory(inventoryType);
        ItemInformationProvider itemInfoProvider = ItemInformationProvider.getInstance();

        for (short i = 0; i < inventory.getSlotLimit(); i++) {
            Item tempItem = inventory.getItem((byte) i);
            if (tempItem != null) {
                String tempItemName = itemInfoProvider.getName(tempItem.getItemId()).toLowerCase();
                if (tempItemName.equals(excludeItemName)) {
                    player.yellowMessage("Skipping item: " + tempItemName);
                    continue;
                }
                player.yellowMessage("Selling item: " + tempItemName + " from slot " + i);
                shop.sell(c, inventoryType, i, tempItem.getQuantity());
            }
        }
        player.yellowMessage("Sold all items in " + type + " except " + excludeItemName + ".");
    }

    private final Set<String> allTypesAsString = Set.of("equip", "use", "setup", "etc", "cash", "all");
}
