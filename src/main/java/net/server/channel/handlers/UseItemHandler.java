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
import server.ItemInformationProvider;
import server.StatEffect;
import tools.PacketCreator;

import static constants.inventory.ItemConstants.USE_POTION_COOLDOWN;

/**
 * @author Matze
 */
public final class UseItemHandler extends AbstractPacketHandler {
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
        // if itemId in 2022529,2022530,2022531,2022536,2022537
        if (itemId == 2022529 || itemId == 2022530 || itemId == 2022531 || itemId == 2022536 || itemId == 2022537) {
            macro = true;
        }
        if (curTime - chr.getUsedPotionTime() < USE_POTION_COOLDOWN) {
            if (!macro){
                // use item is on cooldown
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
            
            } else if(itemId == 2022529){
                String[] macros = chr.getUserMacros();
                if (macros != null && macros.length > 0) {
                    String command = macros[0];
                    CommandsExecutor.getInstance().handle(c, command);
                }
            } else if(itemId == 2022530){
                String[] macros = chr.getUserMacros();
                if (macros != null && macros.length > 1) {
                    String command = macros[1];
                    CommandsExecutor.getInstance().handle(c, command);
                }
            } else if(itemId == 2022531){
                String[] macros = chr.getUserMacros();
                if (macros != null && macros.length > 2) {
                    String command = macros[2];
                    CommandsExecutor.getInstance().handle(c, command);
                }
            } else if(itemId == 2022536){
                String[] macros = chr.getUserMacros();
                if (macros != null && macros.length > 3) {
                    String command = macros[3];
                    CommandsExecutor.getInstance().handle(c, command);
                }
            } else if(itemId == 2022537){
                String[] macros = chr.getUserMacros();
                if (macros != null && macros.length > 4) {
                    String command = macros[4];
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
