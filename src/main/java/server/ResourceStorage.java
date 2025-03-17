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
package server;

import client.Client;
import client.inventory.InventoryType;
import client.inventory.Item;
import client.inventory.ItemFactory;
import constants.game.GameConstants;
import constants.id.ItemId;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import provider.Data;
import provider.DataProvider;
import provider.DataProviderFactory;
import provider.DataTool;
import provider.wz.WZFiles;
import tools.DatabaseConnection;
import tools.PacketCreator;
import tools.Pair;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Matze
 */
public class ResourceStorage {
    public static final int ORE_OFFSET = 0;
    public static final int SCROLL_OFFSET = 1;
    public static final int MERGE_COIN_OFFSET = 2;
    
    private static final Logger log = LoggerFactory.getLogger(ResourceStorage.class);
    private static final Map<Integer, Integer> trunkGetCache = new HashMap<>();
    private static final Map<Integer, Integer> trunkPutCache = new HashMap<>();

    private final int accountId;
    private int currentNpcid;
    private final int storageTypeId;

    private final byte slots = Byte.MAX_VALUE;
    private final Map<InventoryType, List<Item>> typeItems = new HashMap<>();
    private List<Item> items = new LinkedList<>();
    private final Lock lock = new ReentrantLock(true);

    private ResourceStorage(int accountId, int storageTypeId) {
        this.accountId = accountId;
        this.storageTypeId = storageTypeId;
    }

    public static ResourceStorage loadFromDB(int accountId, int storageTypeId) {
        ResourceStorage ret;
        try (Connection con = DatabaseConnection.getConnection()) {
            ret = new ResourceStorage(accountId, storageTypeId);
            
            for (Pair<Item, InventoryType> item : ItemFactory.getFactoryByValue(storageTypeId).loadItems(ret.accountId, false)) {
                ret.items.add(item.getLeft());
            }

            return ret;
        } catch (SQLException ex) { // exceptions leading to deploy null storages found thanks to Jefe
            log.error("SQL error occurred when trying to load storage for accId {}", accountId, ex);
            throw new RuntimeException(ex);
        }
    }

    public byte getSlots() {
        return slots;
    }

    public boolean canGainSlots(int slots) {
        return false;
    }

    public boolean gainSlots(int slots) {
        return false;
    }

    public void saveToDB(Connection con) {
        try {
            List<Pair<Item, InventoryType>> itemsWithType = new ArrayList<>();

            List<Item> list = getItems();
            for (Item item : list) {
                itemsWithType.add(new Pair<>(item, item.getInventoryType()));
            }

            ItemFactory.getFactoryByValue(storageTypeId).saveItems(itemsWithType, accountId, con);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public Item getItem(byte slot) {
        lock.lock();
        try {
            return items.get(slot);
        } finally {
            lock.unlock();
        }
    }

    public Item getItemById(int itemId) {
        lock.lock();
        try {
            for (Item i : getItems()) {
                if (i.getItemId() == itemId) {
                    return i;
                }
            }
            return null;
        } finally {
            lock.unlock();
        }
    }

    // item is assumed to be internal (part of this.items)
    public boolean takeOut(Item item, short qty) {
        lock.lock();
        try {
            if (item.getQuantity() < qty || qty <= 0) {
                return false;
            }

            short newQty = (short) (item.getQuantity() - qty);
            if (newQty > 0) {
                item.setQuantity((short)(item.getQuantity() - qty));
            }
            else {
                items.remove(item);
                InventoryType type = item.getInventoryType();
                typeItems.put(type, new ArrayList<>(filterItems(type)));
            }

            return true;
        } finally {
            lock.unlock();
        }
    }

    // item is assumed to be external (inventory, loot, ...)
    public boolean store(Item item, short qty) {
        lock.lock();
        try {
            if (isFull() || item.getQuantity() < qty || qty < 0) { // thanks Optimist for noticing unrestricted amount of insertions here
                return false;
            }

            Item existing = getItemById(item.getItemId());
            if (existing == null) {
                items.add(new Item(item.getItemId(), (short) 0, qty));
                InventoryType type = item.getInventoryType();
                typeItems.put(type, new ArrayList<>(filterItems(type)));
            }
            else {
                existing.setQuantity((short) (existing.getQuantity() + qty));
            }

            return true;
        } finally {
            lock.unlock();
        }
    }

    public List<Item> getItems() {
        lock.lock();
        try {
            return Collections.unmodifiableList(items);
        } finally {
            lock.unlock();
        }
    }

    private List<Item> filterItems(InventoryType type) {
        List<Item> storageItems = getItems();
        List<Item> ret = new LinkedList<>();

        for (Item item : storageItems) {
            if (item.getInventoryType() == type) {
                ret.add(item);
            }
        }
        return ret;
    }

    public byte getSlot(InventoryType type, byte slot) {
        lock.lock();
        try {
            byte ret = 0;
            List<Item> storageItems = getItems();
            for (Item item : storageItems) {
                if (item == typeItems.get(type).get(slot)) {
                    return ret;
                }
                ret++;
            }
            return -1;
        } finally {
            lock.unlock();
        }
    }

    public void arrangeItems(Client c) {
        lock.lock();
        try {
            StorageInventory msi = new StorageInventory(c, items);
            msi.mergeItems();
            items = msi.sortItems();

            for (InventoryType type : InventoryType.values()) {
                typeItems.put(type, new ArrayList<>(items));
            }

            c.sendPacket(PacketCreator.arrangeStorage(slots, items));
        } finally {
            lock.unlock();
        }
    }

    public boolean isFull() {
        return false; // ???
        /*
        lock.lock();
        try {
            return items.size() >= ((int) slots & 0xFF); // makes the byte unsigned for 255 slots :)
        } finally {
            lock.unlock();
        }*/
    }
}


