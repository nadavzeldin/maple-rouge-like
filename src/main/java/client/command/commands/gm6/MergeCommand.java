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
import client.inventory.manipulator.KarmaManipulator;
import constants.inventory.ItemConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import server.ItemInformationProvider;
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
        Character player = c.getPlayer();

        // First, check if the player has the merge coin
        short mergedCoinPosition = getMergeCoinSlot(player);
        if (mergedCoinPosition == -1) {
            player.yellowMessage("You need a merge coin to merge equipment!");
            return;
        }

        ArrayList<Equip> equipmentItems = new ArrayList<>();
        // Retrieve all equipment (EQP) items from the player's inventory
        for (int i = 0; i < 101; i++) {
            Item tempItem = player.getInventory(InventoryType.EQUIP).getItem((byte) i);
            if (tempItem == null) {
                continue;
            }
            equipmentItems.add((Equip) tempItem);
        }

        // First check for level 25 items for special merge
        boolean performedSpecialMerge = checkForLevel25SpecialMerge(c, player, equipmentItems, mergedCoinPosition);
        if (performedSpecialMerge) {
            return; // End command if special merge was performed
        }

        // A map to group items by their ID
        Map<Integer, ArrayList<Equip>> itemsById = new HashMap<>();

        // Group items by their ID
        for (Equip equip : equipmentItems) {
            itemsById.computeIfAbsent(equip.getItemId(), k -> new ArrayList<>()).add(equip);
        }

        // Process each group of items with the same ID
        boolean foundItemToMerge = false;
        int totalMergesDone = 0;

        for (Map.Entry<Integer, ArrayList<Equip>> entry : itemsById.entrySet()) {
            ArrayList<Equip> equips = entry.getValue();

            // Skip if there's only one item (no merging needed)
            if (equips.size() <= 1) {
                continue; // Skip silently for items that can't be merged
            }

            // Sort equips by level in descending order
            Collections.sort(equips, (a, b) -> Short.compare(b.getItemLevel(), a.getItemLevel()));

            // Continue merging as long as there are enough items and coins
            while (equips.size() >= 2) {
                // Check if we still have merge coins
                mergedCoinPosition = getMergeCoinSlot(player);
                if (mergedCoinPosition == -1) {
                    player.yellowMessage("No more merge coins available for merging.");
                    break;
                }

                // Check if highest level is already at maximum
                if (equips.getFirst().getItemLevel() >= 30) {
                    player.yellowMessage("Cannot merge " + equips.getFirst().toString() + ": Item level is already at maximum (30).");
                    break;
                }

                // Separate level 1 items from higher level items
                ArrayList<Equip> level1Items = new ArrayList<>();
                ArrayList<Equip> higherLevelItems = new ArrayList<>();

                // Primary item (highest level) goes into higher level items
                higherLevelItems.add(equips.get(0));

                // Check the rest of the items
                for (int i = 1; i < equips.size(); i++) {
                    if (equips.get(i).getItemLevel() == 1) {
                        level1Items.add(equips.get(i));
                    } else {
                        higherLevelItems.add(equips.get(i));
                    }
                }

                // If no level 1 items, we can't merge anything
                if (level1Items.isEmpty()) {
                    player.yellowMessage("No level 1 items available to merge with " + higherLevelItems.getFirst().toString());
                    break;
                }

                // Process up to 4 level 1 items at a time (plus the primary item)
                int maxItemsToMerge = Math.min(level1Items.size(), 4);
                ArrayList<Equip> itemsToMerge = new ArrayList<>();

                // Add the primary item
                itemsToMerge.add(higherLevelItems.get(0));

                // Add level 1 items
                for (int i = 0; i < maxItemsToMerge; i++) {
                    itemsToMerge.add(level1Items.get(i));
                }

                // Perform the merge operation
                Equip primaryItem = mergeEquipStats(itemsToMerge, c.getPlayer());

                if (primaryItem == null) {
                    // Merge failed (likely due to level check)
                    break;
                }
                makeItemTradeable(primaryItem);
                foundItemToMerge = true;
                totalMergesDone++;

                // Remove all merged items except the primary from inventory
                short primaryPosition = primaryItem.getPosition();
                for (int i = 1; i < itemsToMerge.size(); i++) {
                    Equip equip = itemsToMerge.get(i);
                    InventoryManipulator.removeFromSlot(c, InventoryType.EQUIP, (byte) equip.getPosition(), equip.getQuantity(), false, false);
                }

                // Use up one merge coin
                InventoryManipulator.removeFromSlot(c, InventoryType.USE, (byte) mergedCoinPosition, (short)1, false, false);

                // Apply special effects
                addItemSpecialEffect(primaryItem, player);

                // Update the client
                c.sendPacket(PacketCreator.modifyInventory(true, Collections.singletonList(new ModifyInventory(0, primaryItem))));

                // Update the equips list for the next potential merge
                // Remove all the level 1 items we just used
                equips.removeAll(itemsToMerge.subList(1, itemsToMerge.size()));
            }
        }

        // Final message
        if (!foundItemToMerge) {
            player.yellowMessage("No items were merged. No merge coins were consumed.");
        } else {
            player.yellowMessage("Merge operations complete! " + totalMergesDone + " merges performed.");
        }
    }
    private void makeItemTradeable(Equip item) {
        short flag = item.getFlag();
        if ((flag & ItemConstants.UNTRADEABLE) == ItemConstants.UNTRADEABLE) {
            flag &= (0xFFFFFFFF ^ ItemConstants.UNTRADEABLE);
            item.setFlag(flag);
        }

        // If the item is drop restricted, we need to add the karma flag to make it tradeable
        if (ItemInformationProvider.getInstance().isDropRestricted(item.getItemId())) {
            // Add karma flag to the item
            KarmaManipulator.setKarmaFlag(item);
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

    private Equip mergeEquipStats(ArrayList<Equip> equips, Character player) {
        // Sort equips by level in descending order
        Collections.sort(equips, (a, b) -> Short.compare(b.getItemLevel(), a.getItemLevel()));

        // Get the primary item (highest level)
        Equip primaryItem = equips.getFirst();
        byte primaryLevel = primaryItem.getItemLevel();

        // Rule #4: Check if any items have levels higher than 1
        boolean hasHigherLevelItems = false;
        for (int i = 1; i < equips.size(); i++) {
            if (equips.get(i).getItemLevel() > 1) {
                hasHigherLevelItems = true;
                break;
            }
        }

        if (hasHigherLevelItems) {
            player.yellowMessage("Cannot merge: You can only merge level 1 items into your primary item.");
            return null;
        }

        // Rule #3: Check if the level increase would exceed 30
        // Each additional item adds 6 levels
        int levelIncrease = 6 * (Math.min(equips.size(), 5) - 1);
        int newLevel = primaryLevel + levelIncrease;

        if (newLevel > 30) {
            player.yellowMessage("Cannot merge: Combined level would exceed the maximum (30).");
            return null;
        }

        // Rule #2: Limit to max 5 items per merge (primary + 4 others)
        int mergeCount = Math.min(equips.size(), 5);
        List<Equip> equipsToMerge = equips.subList(0, mergeCount);

        // Apply the new merging algorithm
        applyNewStatsMergeLogic(primaryItem, equipsToMerge);

        // Set the new item level based on rule #3
        primaryItem.setItemLevel((byte)newLevel);
        log.info("Item level increased from {} to {} (+{} levels)",
                primaryLevel, newLevel, levelIncrease);

        return primaryItem;
    }

    /**
     * Special merge for level 25 items - doubles stats and sets to level 30
     * @return true if special merge was performed, false otherwise
     */
    private boolean checkForLevel25SpecialMerge(Client c, Character player, ArrayList<Equip> equipmentItems, short mergedCoinPosition) {
        // Find all level 25 items
        boolean isBlacksmith = player.accountExtraDetails.getAscension().contains(AscensionConstants.Names.BLACKSMITH);

        if (!isBlacksmith)
        {
            player.yellowMessage("Only blacksmith (Ascension) can reach level 30");
            return false;
        }

        List<Equip> level25Items = new ArrayList<>();

        for (Equip equip : equipmentItems) {
            if (equip.getItemLevel() == 25) {
                level25Items.add(equip);
            }
        }

        if (level25Items.isEmpty()) {
            return false; // No level 25 items found
        }

        // Ask player which level 25 item to merge if multiple exist
        Equip selectedItem;

        if (level25Items.size() == 1) {
            selectedItem = level25Items.getFirst();
        } else {
            // If multiple items, choose the first one for now
            // In a real implementation, you'd prompt the user to choose
            selectedItem = level25Items.getFirst();
            player.yellowMessage("Multiple level 25 items found. Processing the first one: " + selectedItem.toString());
        }

        // Apply the special merge - double all stats and set to level 30
        multiplyItemStats(selectedItem);
        selectedItem.setItemLevel((byte)30);

        // Use up one merge coin
        InventoryManipulator.removeFromSlot(c, InventoryType.USE, (byte) mergedCoinPosition, (short)1, false, false);

        // Apply special effects (as with normal merges)
        addItemSpecialEffect(selectedItem, player);

        // Update the client
        c.sendPacket(PacketCreator.modifyInventory(true, Collections.singletonList(new ModifyInventory(0, selectedItem))));

        // Success message
        player.yellowMessage("Special level 25 merge complete! Your item stats have been doubled and level set to 30.");
        return true;
    }

    /**
     * Multiplies all stats on an equip item by 1.5
     */
    private void multiplyItemStats(Equip item) {
        // Multiply all stats by 1.5
        item.setWatk((short)(item.getWatk() * 1.5));
        item.setWdef((short)(item.getWdef() * 1.5));
        item.setMatk((short)(item.getMatk() * 1.5));
        item.setMdef((short)(item.getMdef() * 1.5));
        item.setHp((short)(item.getHp() * 1.5));
        item.setMp((short)(item.getMp() * 1.5));
        item.setStr((short)(item.getStr() * 1.5));
        item.setDex((short)(item.getDex() * 1.5));
        item.setLuk((short)(item.getLuk() * 1.5));
        item.setInt((short)(item.getInt() * 1.5));

        // Log the stat changes
        log.info("Special Level 25 merge - multiplied all stats by 1.5 for item: {}", item.toString());
    }

    /**
     * Calculates and applies the stats from merging equipment
     * New algorithm: Sum all stats, subtract primary stats to get bonus,
     * apply tax rate (0% default under 2000 atk, 40% if over 2000 atk)
     */
    private void applyNewStatsMergeLogic(Equip primaryItem, List<Equip> equipsToMerge) {
        // Calculate the total stats from all items
        Map<String, Short> totalStats = new HashMap<>();

        // Initialize with zeros
        statGetters.keySet().forEach(stat -> totalStats.put(stat, (short)0));

        // Sum up all stats from all items to merge
        for (Equip equip : equipsToMerge) {
            statGetters.forEach((statName, getter) -> {
                short currentStat = totalStats.get(statName);
                short equipStat = getter.apply(equip);
                totalStats.put(statName, (short)(currentStat + equipStat));
            });
        }

        // Calculate the bonus stats (total - primary)
        Map<String, Short> bonusStats = new HashMap<>();
        statGetters.forEach((statName, getter) -> {
            short primaryStat = getter.apply(primaryItem);
            short totalStat = totalStats.get(statName);
            short bonusStat = (short)(totalStat - primaryStat);
            bonusStats.put(statName, bonusStat);
        });

        // Rule #1: Apply tax rate based on the new rules
        // Default no tax under 2000 attack, 40% above
        double taxRate; // Default 0% tax

        // Check if watk or matk bonus exceeds 2000, apply 40% tax
        if (bonusStats.get("Watk") > 2000 || bonusStats.get("Matk") > 2000) {
            taxRate = 0.4; // 40% tax for high attack bonuses
        } else {
            taxRate = 0.0;
        }

        // Apply the stats to the primary item
        statGetters.forEach((statName, getter) -> {
            short primaryStat = getter.apply(primaryItem);
            short bonusStat = bonusStats.get(statName);

            // Apply tax and round up
            double taxedBonusDouble = bonusStat * (1 - taxRate);
            // Math.ceil to round up to nearest whole number, then cast to short
            short taxedBonus = (short)Math.ceil(taxedBonusDouble);

            // Calculate new stat value
            short newStatValue = (short)(primaryStat + taxedBonus);

            // Update the primary item
            statUpdaters.get(statName).accept(primaryItem, newStatValue);

            log.info("Merged {} stat: Base={}, Bonus={}, Tax={}%, Taxed bonus={}, Final={}",
                    statName, primaryStat, bonusStat, (int)(taxRate * 100), taxedBonus, newStatValue);
        });
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
// !item 2022280 1 MERGE_COIN
