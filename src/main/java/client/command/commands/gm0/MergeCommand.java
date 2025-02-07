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
   @Author: Roey Zeldin - command to merge items and make them stronger

   TODO: add Fredrick in fm as a way to merge without commands. His ID is
*/
package client.command.commands.gm0;

import client.Client;
import client.command.Command;
import client.inventory.Equip;
import client.inventory.InventoryType;
import client.inventory.Item;
import client.inventory.ModifyInventory;
import client.inventory.manipulator.InventoryManipulator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.PacketCreator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class MergeCommand extends Command {
    private static final Logger log = LoggerFactory.getLogger(MergeCommand.class);

    {
        setDescription("Merges EQP items and makes them stronger");
    }

    @Override
    public void execute(Client c, String[] params) {
        ArrayList<Equip> equipmentItems = new ArrayList<>();
        // Retrieve all equipment (EQP) items from the player's inventory
        for (int i = 0; i < 101; i++) {
            Item tempItem = c.getPlayer().getInventory(InventoryType.EQUIP).getItem((byte) i);
            if (tempItem == null) {
                continue;
            }
            equipmentItems.add((Equip) tempItem);
        }
        // A map to group items by their ID
        Map<Integer, ArrayList<Equip>> itemsById = new HashMap<>();

        // Group items by their ID
        for (Equip equip : equipmentItems) {
            itemsById.computeIfAbsent(equip.getItemId(), k -> new ArrayList<>()).add(equip);
        }
        // Process each group of items with the same ID
        for (Map.Entry<Integer, ArrayList<Equip>> entry : itemsById.entrySet()) {
            ArrayList<Equip> equips = entry.getValue();
            
            // Skip if there's only one item (no merging needed)
            if (equips.size() <= 1) {
                continue;
            }

            // Calculate the percentage boost to be added
            Equip primaryItem = mergeEquipStats(equips);

            short primaryPosition = primaryItem.getPosition();
            for (Equip equip : equips) {
                if (equip.getPosition() != primaryPosition) {
                    InventoryManipulator.removeFromSlot(c, InventoryType.EQUIP, (byte) equip.getPosition(), equip.getQuantity(), false, false);
                }
            }
            c.sendPacket(PacketCreator.modifyInventory(true, Collections.singletonList(new ModifyInventory(0, primaryItem))));
        }
    }

    private Equip mergeEquipStats(ArrayList<Equip> equips) {
        Equip primaryItem = equips.getFirst();
        statGetters.forEach((statName, getter) -> {
            // Get the max stat for the equips array on the getter func
            short currentMaxStat = equips.stream()
                    .map(getter)
                    .max(Short::compare)
                    .orElse(getter.apply(primaryItem));

            short additionalStat = (short) (currentMaxStat * statsMultiplier * (equips.size() - 1));
            short newStatValue = (short) (currentMaxStat + additionalStat);

            log.info("The new Item stat for {} is {}", statName, newStatValue);
            statUpdaters.get(statName).accept(primaryItem, newStatValue);
        });

        return primaryItem;
    }

    private final double statsMultiplier = 0.1;
    private final Map<String, java.util.function.BiConsumer<Equip, Short>> statUpdaters = Map.of(
            "Watk", Equip::setWatk,
            "Wdef", Equip::setWdef,
            "Str", Equip::setStr,
            "Dex", Equip::setDex,
            "Luk", Equip::setLuk,
            "Int", Equip::setInt
    );

    private final Map<String, java.util.function.Function<Equip, Short>> statGetters = Map.of(
            "Watk", Equip::getWatk,
            "Wdef", Equip::getWdef,
            "Str", Equip::getStr,
            "Dex", Equip::getDex,
            "Luk", Equip::getLuk,
            "Int", Equip::getInt
    );
}

// !item 1302000 1 sword
// !item 1082002 1 glove
// !item 2040806 10 glove dex