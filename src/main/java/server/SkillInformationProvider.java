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

import client.Character;
import client.Client;
import client.Job;
import client.Skill;
import client.SkillFactory;
import client.autoban.AutobanFactory;
import client.inventory.Equip;
import client.inventory.Inventory;
import client.inventory.InventoryType;
import client.inventory.Item;
import client.inventory.WeaponType;
import config.YamlConfig;
import constants.id.ItemId;
import constants.inventory.EquipSlot;
import constants.inventory.ItemConstants;
import constants.skills.Assassin;
import constants.skills.Gunslinger;
import constants.skills.NightWalker;
import net.server.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import provider.Data;
import provider.DataDirectoryEntry;
import provider.DataFileEntry;
import provider.DataProvider;
import provider.DataProviderFactory;
import provider.DataTool;
import provider.wz.WZFiles;
import server.MakerItemFactory.MakerItemCreateEntry;
import server.life.LifeFactory;
import server.life.MonsterInformationProvider;
import tools.DatabaseConnection;
import tools.PacketCreator;
import tools.Pair;
import tools.Randomizer;
import tools.StringUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * @author CPURules
 */
public class SkillInformationProvider {
    private static final Logger log = LoggerFactory.getLogger(SkillInformationProvider.class);
    private final static SkillInformationProvider instance = new SkillInformationProvider();

    public static SkillInformationProvider getInstance() {
        return instance;
    }

    protected DataProvider skillData;
    protected DataProvider stringData;
    protected Data skillStringData;
    protected Map<Integer, String> nameCache = new HashMap<>();
    protected List<Pair<Integer, String>> skillNameCache = new ArrayList<>();


    private SkillInformationProvider() {
        skillData = DataProviderFactory.getDataProvider(WZFiles.SKILL);
        stringData = DataProviderFactory.getDataProvider(WZFiles.STRING);
        skillStringData = stringData.getData("Skill.img");
    }

    public List<Pair<Integer, String>> getAllSkills() {
        if (!skillNameCache.isEmpty()) {
            return skillNameCache;
        }

        List<Pair<Integer, String>> itemPairs = new ArrayList<>();
        for (Data skillFolder: skillStringData.getChildren()) {
            itemPairs.add(new Pair<>(Integer.parseInt(skillFolder.getName()), DataTool.getString("name", skillFolder, "NO-NaME")));
        }

        return itemPairs;
    }

    private Data getStringData(int skillId) {
        return skillStringData.getChildByPath(String.valueOf(skillId));
    }

    public String getName(int skillId) {
        if (nameCache.containsKey(skillId)) {
            return nameCache.get(skillId);
        }
        Data strings = getStringData(skillId);
        if (strings == null) {
            return null;
        }
        String ret = DataTool.getString("name", strings, null);
        nameCache.put(skillId, ret);
        return ret;
    }
}
