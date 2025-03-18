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
package client.command.commands.gm6;

import client.AscensionConstants;
import client.Client;
import client.Character;
import client.Job;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static constants.id.ItemId.MERGE_COIN;


public class MergeCommand extends Command {
    public static final int BLACKSMITHJOBTIER = 10;

    private static final Logger log = LoggerFactory.getLogger(MergeCommand.class);

    {
        setDescription("Merges EQP items and makes them stronger");
    }

    @Override
    public void execute(Client c, String[] params) {
        ArrayList<Equip> equipmentItems = new ArrayList<>();
        // Retrieve all equipment (EQP) items from the player's inventory
        Character player = c.getPlayer();
        for (int i = 0; i < 101; i++) {
            Item tempItem = player.getInventory(InventoryType.EQUIP).getItem((byte) i);
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
        boolean foundItemToMerge = false;

        for (Map.Entry<Integer, ArrayList<Equip>> entry : itemsById.entrySet()) {
            ArrayList<Equip> equips = entry.getValue();

            // Skip if there's only one item (no merging needed)
            if (equips.size() <= 1) {
                continue;
            }
            if (equips.stream()
                    .map(Equip::getItemLevel)
                    .max(Short::compare)
                    .orElse((byte)1) >= 30)
            {
                player.yellowMessage("can't merge max level equip" + equips.getFirst().toString());
                continue;
            }
            foundItemToMerge = true;
            // Calculate the percentage boost to be added
            Equip primaryItem = mergeEquipStats(equips);

            short primaryPosition = primaryItem.getPosition();
            for (Equip equip : equips) {
                if (equip.getPosition() != primaryPosition) {
                    InventoryManipulator.removeFromSlot(c, InventoryType.EQUIP, (byte) equip.getPosition(), equip.getQuantity(), false, false);
                }
            }
            addItemSpecialEffect(primaryItem, player);
            c.sendPacket(PacketCreator.modifyInventory(true, Collections.singletonList(new ModifyInventory(0, primaryItem))));
        }

        // No item has been merge, no reason to remove item
        if (!foundItemToMerge) {
            player.yellowMessage("You don't have any equips to merge, will not use up the Black Loud Machine!");
        } else {
            short mergedCoinPosition = getMergeCoinSlot(player);
            InventoryManipulator.removeFromSlot(c, InventoryType.USE, (byte) mergedCoinPosition, (short)1, false, false);
        }
    }

    private static final Map<String, EffectBonus> EFFECT_BONUSES = new HashMap<>() {{
        put("HarryPotter", new EffectBonus("matk", "int"));
        put("Zoro", new EffectBonus("atk", "str"));
        put("Legolas", new EffectBonus("atk", "dex"));
        put("Ninja", new EffectBonus("atk", "luk"));
        put("Bolder", new EffectBonus("hp", "def"));
        put("Myst", new EffectBonus("mana", "mdef"));
    }};

    private static class EffectBonus {
        public final String weaponStat;
        public final String armorStat;

        public EffectBonus(String weaponStat, String armorStat) {
            this.weaponStat = weaponStat;
            this.armorStat = armorStat;
        }
    }

    private void addItemSpecialEffect(Equip primaryItem, Character player) {
        String previousEffect = primaryItem.getOwner();
        List<String> effects = Arrays.asList("Unkillable", "HarryPotter", "Zoro", "Legolas", "Ninja", "Bolder", "Myst");
        Random random = new Random();
        int jobTier = jobTierLevel(player.getJob());

        // Get random effect or null
        String selectedEffect = null;
        int roll = random.nextInt(100); // Generates a number between 0 and 99
        boolean isBlacksmith = player.accountExtraDetails.getAscension().contains(AscensionConstants.Names.BLACKSMITH);
        if (roll == 0) { // 1% chance for "Unkillable"
            selectedEffect = "Unkillable";
        } else if (roll < 50) { // 50% chance to pick a normal effect
            selectedEffect = effects.get(random.nextInt(effects.size()));
        }

        // Return if no effect or same effect
        if (selectedEffect == null || selectedEffect.equals(previousEffect)) {
            return;
        }

        // Return if no effect or same effect
        if (selectedEffect == null || previousEffect.equals(selectedEffect)) {
            return;
        }

        // Remove previous effect if it existed
        if (previousEffect != null && !previousEffect.isEmpty()) {
            EffectBonus prevBonus = EFFECT_BONUSES.get(previousEffect);
            if (prevBonus != null) {
                boolean isWeaponItem = isWeapon(primaryItem.getItemId());
                String statType = isWeaponItem ? prevBonus.weaponStat : prevBonus.armorStat;
                int boost = -getJobTierBoost(jobTier, statType); // Negative for removal
                updateStat(primaryItem, statType, boost);
            }
        }

        // Apply new effect
        boolean isBlacksmithBuffed = isBlacksmith && roll < 11 && !selectedEffect.equals("Unkillable");
        EffectBonus newBonus = EFFECT_BONUSES.get(selectedEffect);
        selectedEffect = isBlacksmithBuffed ? selectedEffect +"++" : selectedEffect;
        primaryItem.setOwner(selectedEffect);

        if (newBonus != null) {
            jobTier = isBlacksmithBuffed ? BLACKSMITHJOBTIER : jobTier;
            boolean isWeaponItem = isWeapon(primaryItem.getItemId());
            String statType = isWeaponItem ? newBonus.weaponStat : newBonus.armorStat;
            int boost = getJobTierBoost(jobTier, statType);
            updateStat(primaryItem, statType, boost);
        }
    }

    private short getMergeCoinSlot(Character player)
    {
        for (short i = 0; i < 101; i++) {
            Item tempItem = player.getInventory(InventoryType.USE).getItem((byte) i);
            if (tempItem == null) {
                continue;
            }
            if (tempItem.getItemId() == MERGE_COIN)
            {
                return i;
            }
        }
        return -1;
    }

    private void updateStat(Equip item, String statType, int boost) {
        switch (statType.toLowerCase()) {
            case "matk":
                item.setMatk((short)(item.getMatk() + boost));
                break;
            case "atk":
            case "watk":
                item.setWatk((short)(item.getWatk() + boost));
                break;
            case "str":
                item.setStr((short)(item.getStr() + boost));
                break;
            case "dex":
                item.setDex((short)(item.getDex() + boost));
                break;
            case "int":
                item.setInt((short)(item.getInt() + boost));
                break;
            case "luk":
                item.setLuk((short)(item.getLuk() + boost));
                break;
            case "hp":
                item.setHp((short)(item.getHp() + boost));
                break;
            case "mp":
            case "mana":
                item.setMp((short)(item.getMp() + boost));
                break;
            case "wdef":
            case "def":
                item.setWdef((short)(item.getWdef() + boost));
                break;
            case "mdef":
                item.setMdef((short)(item.getMdef() + boost));
                break;
        }
    }

    private int jobTierLevel(Job playerJob) {
        // Determine job tier/level
        int jobLevel = 0;
        int jobId = playerJob.getId();

        if (playerJob.isA(Job.BEGINNER) || playerJob.isA(Job.NOBLESSE)) {
            jobLevel = 0;
        } else if (jobId < 2000) {  // Cygnus Knights
            if (jobId % 10 == 0) {      // 1100, 1200, etc (1st job)
                jobLevel = 1;
            } else if (jobId % 100 == 10) { // 1110, 1210, etc (2nd job)
                jobLevel = 2;
            } else if (jobId % 100 == 11) { // 1111, 1211, etc (3rd job)
                jobLevel = 3;
            } else if (jobId % 100 == 12) { // 1112, 1212, etc (4th job)
                jobLevel = 4;
            }
        } else {  // Adventurers
            if (jobId % 100 == 0) {     // 100, 200, etc (1st job)
                jobLevel = 1;
            } else if (jobId % 10 == 0) { // 110, 120, etc (2nd job)
                jobLevel = 2;
            } else if (jobId % 10 == 1) { // 111, 121, etc (3rd job)
                jobLevel = 3;
            } else if (jobId % 10 == 2) { // 112, 122, etc (4th job)
                jobLevel = 4;
            }
        }
        return jobLevel;
    }

    private int getJobTierBoost(int jobTier, String statType) {

        switch (statType.toLowerCase()) {
            // Base Stats
            case "str":
            case "dex":
            case "int":
            case "luk":
                switch (jobTier) {
                    case 0: return 3;   // Beginner
                    case 1: return 5;   // 1st Job
                    case 2: return 8;   // 2nd Job
                    case 3: return 12;  // 3rd Job
                    case 4: return 15;  // 4th Job
                    case BLACKSMITHJOBTIER: return 100;
                    default: return 0;
                }

                // Attack Stats
            case "watk":
                switch (jobTier) {
                    case 0: return 5;
                    case 1: return 10;
                    case 2: return 15;
                    case 3: return 20;
                    case 4: return 25;
                    case BLACKSMITHJOBTIER: return 250;
                    default: return 0;
                }

            case "matk":
                switch (jobTier) {
                    case 0: return 5;
                    case 1: return 12;
                    case 2: return 18;
                    case 3: return 25;
                    case 4: return 30;
                    case BLACKSMITHJOBTIER: return 250;
                    default: return 0;
                }

                // Defense Stats
            case "wdef":
                switch (jobTier) {
                    case 0: return 20;
                    case 1: return 40;
                    case 2: return 60;
                    case 3: return 80;
                    case 4: return 100;
                    case BLACKSMITHJOBTIER: return 400;
                    default: return 0;
                }

            case "mdef":
                switch (jobTier) {
                    case 0: return 15;
                    case 1: return 30;
                    case 2: return 45;
                    case 3: return 60;
                    case 4: return 75;
                    case BLACKSMITHJOBTIER: return 400;
                    default: return 0;
                }

            case "hp":
                switch (jobTier) {
                    case 0: return 250;
                    case 1: return 500;
                    case 2: return 800;
                    case 3: return 1200;
                    case 4: return 1500;
                    case BLACKSMITHJOBTIER: return 4000;
                    default: return 0;
                }

            case "mp":
                switch (jobTier) {
                    case 0: return 200;
                    case 1: return 400;
                    case 2: return 650;
                    case 3: return 1000;
                    case 4: return 1200;
                    case BLACKSMITHJOBTIER: return 4000;
                    default: return 0;
                }

            default:
                return 0;
        }
    }

    private boolean isWeapon(int itemId) {
        int subCategory = (itemId / 10000) % 100;  // Get weapon type digits

        // Check if it falls into any weapon category
        return subCategory >= 30 && subCategory <= 49 && (
                subCategory == 30 || // One-Handed Sword
                        subCategory == 31 || // One-Handed Axe
                        subCategory == 32 || // One-Handed Mace
                        subCategory == 33 || // Dagger
                        subCategory == 37 || // Wand
                        subCategory == 38 || // Staff
                        subCategory == 40 || // Two-Handed Sword
                        subCategory == 41 || // Two-Handed Axe
                        subCategory == 42 || // Two-Handed Mace
                        subCategory == 43 || // Spear
                        subCategory == 44 || // Pole Arm
                        subCategory == 45 || // Bow
                        subCategory == 46 || // Crossbow
                        subCategory == 47 || // Claw
                        subCategory == 48 || // Knuckle
                        subCategory == 49    // Gun
        );
    }

    private Equip mergeEquipStats(ArrayList<Equip> equips) {
        Equip primaryItem = equips.getFirst();
        byte maxItemLevel = equips.stream()
                .map(Equip::getItemLevel)
                .max(Short::compare)
                .orElse((byte)1);

        maxItemLevel = maxItemLevel > 0 ? maxItemLevel : 1; // not to dived by zero

        double dampingScale = 5;
        double scalingFactor = (double) 1 / (dampingScale * (maxItemLevel * maxItemLevel));

        statGetters.forEach((statName, getter) -> {
            // Get the max stat for the equips array on the getter func
            short currentMaxStat = equips.stream()
                    .map(getter)
                    .max(Short::compare)
                    .orElse(getter.apply(primaryItem));

            short additionalStat = (short) (currentMaxStat * scalingFactor * (Math.sqrt(equips.size())));
            // check 16 bit overflow
            short newStatValue = (short) (currentMaxStat + additionalStat > currentMaxStat ? currentMaxStat + additionalStat : currentMaxStat);

            log.info("The new Item stat for {} is {}", statName, newStatValue);
            statUpdaters.get(statName).accept(primaryItem, newStatValue);
        });
        if (maxItemLevel != 30) {
            primaryItem.setItemLevel((byte) (maxItemLevel + 1)); // level up the Item
        }
        return primaryItem;
    }

    private final Map<String, java.util.function.BiConsumer<Equip, Short>> statUpdaters = Map.of(
            "Watk", Equip::setWatk,
            "Wdef", Equip::setWdef,
            "Matk", Equip::setMatk,
            "Mdef", Equip::setMdef,
            "HP", Equip::setHp,
            "MP", Equip::setMp,
            "Str", Equip::setStr,
            "Dex", Equip::setDex,
            "Luk", Equip::setLuk,
            "Int", Equip::setInt
    );

    private final Map<String, java.util.function.Function<Equip, Short>> statGetters = Map.of(
            "Watk", Equip::getWatk,
            "Wdef", Equip::getWdef,
            "Matk", Equip::getMatk,
            "Mdef", Equip::getMdef,
            "HP", Equip::getHp,
            "MP", Equip::getMp,
            "Str", Equip::getStr,
            "Dex", Equip::getDex,
            "Luk", Equip::getLuk,
            "Int", Equip::getInt
    );
}

// !item 1302000 1 sword
// !item 1082002 1 glove
// !item 2040806 10 glove dex
// !item 2280001 1 MERGE_COIN
