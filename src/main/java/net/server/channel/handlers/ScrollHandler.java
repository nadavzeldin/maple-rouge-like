package net.server.channel.handlers;

import client.AscensionConstants;
import client.Character;
import client.Client;
import client.Skill;
import client.SkillFactory;
import client.inventory.Equip;
import client.inventory.Equip.ScrollResult;
import client.inventory.Inventory;
import client.inventory.InventoryType;
import client.inventory.Item;
import client.inventory.ModifyInventory;
import client.inventory.manipulator.InventoryManipulator;
import constants.id.ItemId;
import constants.inventory.ItemConstants;
import net.AbstractPacketHandler;
import net.packet.InPacket;
import server.ItemInformationProvider;
import tools.PacketCreator;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Matze
 * @author Frz
 */
public final class ScrollHandler extends AbstractPacketHandler {

    @Override
    public final void handlePacket(InPacket p, Client c) {
        if (c.tryacquireClient()) {
            try {
                p.readInt(); // whatever...
                short scrollSlot = p.readShort();
                short equipSlot = p.readShort();
                byte ws = (byte) p.readShort();
                boolean whiteScroll = (ws & 2) == 2;
                boolean legendarySpirit = false;

                ItemInformationProvider ii = ItemInformationProvider.getInstance();
                Character chr = c.getPlayer();
                Equip toScroll = (Equip) chr.getInventory(InventoryType.EQUIPPED).getItem(equipSlot);
                Skill LegendarySpirit = SkillFactory.getSkill(1003);
                if (chr.getSkillLevel(LegendarySpirit) > 0 && equipSlot >= 0) {
                    legendarySpirit = true;
                    toScroll = (Equip) chr.getInventory(InventoryType.EQUIP).getItem(equipSlot);
                }

                Inventory useInventory = chr.getInventory(InventoryType.USE);
                Item scroll = useInventory.getItem(scrollSlot);
                Item wscroll = whiteScroll ? useInventory.findById(ItemId.WHITE_SCROLL) : null;

                // Validation checks
                if (scroll == null || scroll.getQuantity() < 1) {
                    announceCannotScroll(c, legendarySpirit);
                    return;
                }
                if (ItemConstants.isCleanSlate(scroll.getItemId()) && !ii.canUseCleanSlate(toScroll)) {
                    announceCannotScroll(c, legendarySpirit);
                    return;
                }
                if (!ItemConstants.isModifierScroll(scroll.getItemId()) && toScroll.getUpgradeSlots() < 1) {
                    announceCannotScroll(c, legendarySpirit);
                    return;
                }
                List<Integer> scrollReqs = ii.getScrollReqs(scroll.getItemId());
                if (scrollReqs.size() > 0 && !scrollReqs.contains(toScroll.getItemId())) {
                    announceCannotScroll(c, legendarySpirit);
                    return;
                }
                if (whiteScroll && (wscroll == null || wscroll.getQuantity() < 1)) {
                    announceCannotScroll(c, legendarySpirit);
                    return;
                }
                if (!ItemConstants.isChaosScroll(scroll.getItemId()) && !ItemConstants.isCleanSlate(scroll.getItemId())) {
                    if (!canScroll(scroll.getItemId(), toScroll.getItemId())) {
                        announceCannotScroll(c, legendarySpirit);
                        return;
                    }
                }

                byte initialSlots = toScroll.getUpgradeSlots();
                List<ModifyInventory> mods = new ArrayList<>();
                Equip currentEquip = toScroll;
                boolean equipChanged = equipSlot < 0;
                int successfulScrolls = 0;
                int scrollsUsed = 0;

                useInventory.lockInventory();
                try {
                    if (legendarySpirit) {
                        // For Legendary Spirit, keep scrolling based on whiteScroll condition
                        while (currentEquip != null && 
                               (whiteScroll ? successfulScrolls < initialSlots : scrollsUsed < initialSlots) && 
                               scroll.getQuantity() > scrollsUsed && 
                               (!whiteScroll || wscroll.getQuantity() > scrollsUsed)
                               && !ItemConstants.isCleanSlate(scrollsUsed)) {
                            byte oldLevel = currentEquip.getLevel();
                            byte oldSlots = currentEquip.getUpgradeSlots();

                            Equip scrolled = (Equip) ii.scrollEquipWithId(currentEquip, scroll.getItemId(), whiteScroll, 0, chr.isGM(), chr.accountExtraDetails.getAscension().contains(AscensionConstants.Names.LUCKY));
                            ScrollResult scrollSuccess = Equip.ScrollResult.FAIL;

                            if (scrolled == null) {
                                scrollSuccess = Equip.ScrollResult.CURSE;
                            } else if (scrolled.getLevel() > oldLevel || 
                                     (ItemConstants.isCleanSlate(scroll.getItemId()) && scrolled.getUpgradeSlots() == oldSlots + 1) || 
                                     ItemConstants.isFlagModifier(scroll.getItemId(), scrolled.getFlag())) {
                                scrollSuccess = Equip.ScrollResult.SUCCESS;
                                successfulScrolls++;
                            }

                            scrollsUsed++;

                            if (scrollSuccess == Equip.ScrollResult.CURSE && !ItemId.isWeddingRing(currentEquip.getItemId())) {
                                handleCurseResult(c, currentEquip, equipSlot, mods);
                                currentEquip = null;
                            } else {
                                currentEquip = scrolled;
                                mods.add(new ModifyInventory(3, currentEquip));
                                mods.add(new ModifyInventory(0, currentEquip));
                            }

                            // Broadcast scroll effect for each attempt
                            chr.getMap().broadcastMessage(PacketCreator.getScrollEffect(chr.getId(), scrollSuccess, legendarySpirit, whiteScroll));

                            // Add 100ms delay between scrolls
                            if (currentEquip != null && (whiteScroll ? successfulScrolls < initialSlots : scrollsUsed < initialSlots)) {
                                try {
                                    Thread.sleep(100); // 100ms delay
                                } catch (InterruptedException e) {
                                    Thread.currentThread().interrupt();
                                    break;
                                }
                            }
                        }
                    } else {
                        // Regular scrolling: only 1 scroll
                        byte oldLevel = currentEquip.getLevel();
                        byte oldSlots = currentEquip.getUpgradeSlots();

                        Equip scrolled = (Equip) ii.scrollEquipWithId(currentEquip, scroll.getItemId(), whiteScroll, 0, chr.isGM(), chr.accountExtraDetails.getAscension().contains(AscensionConstants.Names.LUCKY));
                        ScrollResult scrollSuccess = Equip.ScrollResult.FAIL;

                        if (scrolled == null) {
                            scrollSuccess = Equip.ScrollResult.CURSE;
                        } else if (scrolled.getLevel() > oldLevel || 
                                 (ItemConstants.isCleanSlate(scroll.getItemId()) && scrolled.getUpgradeSlots() == oldSlots + 1) || 
                                 ItemConstants.isFlagModifier(scroll.getItemId(), scrolled.getFlag())) {
                            scrollSuccess = Equip.ScrollResult.SUCCESS;
                            successfulScrolls++;
                        }

                        scrollsUsed = 1;

                        if (scrollSuccess == Equip.ScrollResult.CURSE && !ItemId.isWeddingRing(currentEquip.getItemId())) {
                            handleCurseResult(c, currentEquip, equipSlot, mods);
                            currentEquip = null;
                        } else {
                            currentEquip = scrolled;
                            mods.add(new ModifyInventory(3, currentEquip));
                            mods.add(new ModifyInventory(0, currentEquip));
                        }

                        chr.getMap().broadcastMessage(PacketCreator.getScrollEffect(chr.getId(), scrollSuccess, legendarySpirit, whiteScroll));
                    }

                    // Remove used items from inventory
                    InventoryManipulator.removeFromSlot(c, InventoryType.USE, scroll.getPosition(), (short) scrollsUsed, false);
                    if (whiteScroll && !ItemConstants.isCleanSlate(scroll.getItemId())) {
                        InventoryManipulator.removeFromSlot(c, InventoryType.USE, wscroll.getPosition(), (short) scrollsUsed, false, false);
                    }
                } finally {
                    useInventory.unlockInventory();
                }

                // Send inventory update
                c.sendPacket(PacketCreator.modifyInventory(true, mods));

                // Update equipped item if necessary
                if (equipChanged && (successfulScrolls > 0 || currentEquip == null)) {
                    chr.equipChanged();
                }

            } finally {
                c.releaseClient();
            }
        }
    }

    private static void handleCurseResult(Client c, Equip toScroll, short equipSlot, List<ModifyInventory> mods) {
        Character chr = c.getPlayer();
        mods.add(new ModifyInventory(3, toScroll));
        Inventory inv = equipSlot < 0 ? chr.getInventory(InventoryType.EQUIPPED) : chr.getInventory(InventoryType.EQUIP);

        inv.lockInventory();
        try {
            if (equipSlot < 0) {
                chr.unequippedItem(toScroll);
            }
            inv.removeItem(toScroll.getPosition());
        } finally {
            inv.unlockInventory();
        }
    }

    private static void announceCannotScroll(Client c, boolean legendarySpirit) {
        if (legendarySpirit) {
            c.sendPacket(PacketCreator.getScrollEffect(c.getPlayer().getId(), ScrollResult.FAIL, false, false));
        } else {
            c.sendPacket(PacketCreator.getInventoryFull());
        }
    }

    private static boolean canScroll(int scrollid, int itemid) {
        int sid = scrollid / 100;

        switch (sid) {
            case 20492: // scroll for accessory (pendant, belt, ring)
                return canScroll(ItemId.RING_STR_100_SCROLL, itemid) || canScroll(ItemId.DRAGON_STONE_SCROLL, itemid) ||
                        canScroll(ItemId.BELT_STR_100_SCROLL, itemid);
            default:
                return (scrollid / 100) % 100 == (itemid / 10000) % 100;
        }
    }
}