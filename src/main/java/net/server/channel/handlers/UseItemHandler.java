/*
	This file is part of the OdinMS Maple Story Server
    Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc>
		       Matthias Butz <matze@odinms.de>
		       Jan Christian Meyer <vimes@odinms.de>

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
package net.server.channel.handlers;

import client.Character;
import client.Client;
import client.Disease;
import client.command.CommandsExecutor;
import client.inventory.InventoryType;
import client.inventory.Item;
import client.inventory.manipulator.InventoryManipulator;
import constants.id.ItemId;
import constants.inventory.ItemConstants;
import net.AbstractPacketHandler;
import net.packet.InPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import server.ItemInformationProvider;
import server.StatEffect;
import tools.PacketCreator;

import static constants.inventory.ItemConstants.USE_POTION_COOLDOWN;

import java.util.Map;

/**
 * @author Matze
 */
public final class UseItemHandler extends AbstractPacketHandler {
    // 2022529 - Meaning of Azaleas
    // 2022530 - Meaning of Forsythias // 2022531 - Meaning of Clovers
    // 2022536 - Underground Temple's Seal // 2022537 - Gladius' Strength
    private static final Map<Integer, Integer> MACRO_ITEMS_DICT = Map.of(2022529, 1, 2022530, 2, 2022531, 3, 2022536, 4, 2022537, 5);

    @Override
    public final void handlePacket(InPacket p, Client c) {
        Character chr = c.getPlayer();

        if (!chr.isAlive()) {
            c.sendPacket(PacketCreator.enableActions());
            return;
        }
        ItemInformationProvider ii = ItemInformationProvider.getInstance();
        p.readInt();
        short slot = p.readShort();
        int itemId = p.readInt();
        Item toUse = chr.getInventory(InventoryType.USE).getItem(slot);
        long curTime = System.currentTimeMillis();
        boolean macro = false;
        // if itemId in MACRO_ITEMS_DICT
        if (MACRO_ITEMS_DICT.containsKey(itemId)) {
            macro = true;
        }
        if (itemId != ItemId.HAPPY_BIRTHDAY && curTime - chr.getUsedPotionTime() < USE_POTION_COOLDOWN) {
            if (!macro){
                chr.yellowMessage("Potion is on cooldown!");
                return;
            }
        }
        chr.setUsedPotionTime(curTime); // set the time we used a potion (or any use item)
        if (toUse != null && toUse.getQuantity() > 0 && toUse.getItemId() == itemId) {
            if (itemId == ItemId.ALL_CURE_POTION) {
                chr.dispelDebuffs();
                remove(c, slot);
                return;
            } else if (itemId == ItemId.EYEDROP) {
                chr.dispelDebuff(Disease.DARKNESS);
                remove(c, slot);
                return;
            } else if (itemId == ItemId.TONIC) {
                chr.dispelDebuff(Disease.WEAKEN);
                chr.dispelDebuff(Disease.SLOW);
                remove(c, slot);
                return;
            } else if (itemId == ItemId.HOLY_WATER) {
                chr.dispelDebuff(Disease.SEAL);
                chr.dispelDebuff(Disease.CURSE);
                remove(c, slot);
                return;
            } else if (ItemConstants.isTownScroll(itemId)) {
                if (ii.getItemEffect(toUse.getItemId()).applyTo(chr)) {
                    remove(c, slot);
                }
                return;
            } else if(macro){
                String[] macros = chr.getUserMacros();
                int idx = MACRO_ITEMS_DICT.get(itemId) - 1;
                if (macros != null && macros.length > idx) {
                    String command = macros[idx];
                    CommandsExecutor.getInstance().handle(c, command);
                    }
            }

            if(!macro){
                remove(c, slot);
            }

            if (toUse.getItemId() == ItemId.HAPPY_BIRTHDAY) {
                chr.AddStrDexIntLuk(1);
            }

            if (toUse.getItemId() != ItemId.HAPPY_BIRTHDAY) {
                ii.getItemEffect(toUse.getItemId()).applyTo(chr);
            } else {
                StatEffect mse = ii.getItemEffect(toUse.getItemId());
                for (Character player : chr.getMap().getCharacters()) {
                    mse.applyTo(player);
                }
            }
        }
    }

    private void remove(Client c, short slot) {
        InventoryManipulator.removeFromSlot(c, InventoryType.USE, slot, (short) 1, false);
        c.sendPacket(PacketCreator.enableActions());
    }
}
