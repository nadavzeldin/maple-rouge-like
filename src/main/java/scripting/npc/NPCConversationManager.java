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
package scripting.npc;

import client.Character;
import client.*;
import client.inventory.Item;
import client.inventory.ItemFactory;
import client.inventory.Pet;
import config.YamlConfig;
import constants.game.GameConstants;
import constants.id.MapId;
import constants.id.NpcId;
import constants.inventory.ItemConstants;
import constants.string.LanguageConstants;
import net.server.Server;
import net.server.channel.Channel;
import net.server.coordinator.matchchecker.MatchCheckerListenerFactory.MatchCheckerType;
import net.server.guild.Alliance;
import net.server.guild.Guild;
import net.server.guild.GuildPackets;
import net.server.world.Party;
import net.server.world.PartyCharacter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import provider.Data;
import provider.DataProviderFactory;
import provider.wz.WZFiles;
import scripting.AbstractPlayerInteraction;
import server.*;
import server.SkillbookInformationProvider.SkillBookEntry;
import server.events.gm.Event;
import server.expeditions.Expedition;
import server.expeditions.ExpeditionType;
import server.gachapon.Gachapon;
import server.gachapon.Gachapon.GachaponItem;
import server.life.LifeFactory;
import server.life.PlayerNPC;
import server.maps.MapManager;
import server.maps.MapObject;
import server.maps.MapObjectType;
import server.maps.MapleMap;
import server.partyquest.AriantColiseum;
import server.partyquest.MonsterCarnival;
import server.partyquest.Pyramid;
import server.partyquest.Pyramid.PyramidMode;
import tools.PacketCreator;
import tools.packets.WeddingPackets;

import java.awt.*;
import java.sql.SQLException;
import java.util.List;
import java.util.*;

import static java.util.concurrent.TimeUnit.MINUTES;

/**
 * @author Matze
 */
public class NPCConversationManager extends AbstractPlayerInteraction {
    private static final Logger log = LoggerFactory.getLogger(NPCConversationManager.class);
    private static final List<Integer> TAMING_ITEMS = new ArrayList<>();
    private static final List<Integer> WEAPON_SHIELD_ITEMS = new ArrayList<>();
    private static final List<Integer> ACCESSORIES_ITEMS = new ArrayList<>();
    private static final List<Integer> EQUIPMENT_ITEMS = new ArrayList<>();
    private static final List<Integer> USE_ITEMS = new ArrayList<>();
    private static final List<Integer> SET_UP = new ArrayList<>();
    private static final List<Integer> SCROLLS_ITEMS = new ArrayList<>();
    private static boolean poolsInitialized = false;

    private final int npc;
    private int npcOid;
    private String scriptName;
    private String getText;
    private boolean itemScript;
    private List<PartyCharacter> otherParty;

    private final Map<Integer, String> npcDefaultTalks = new HashMap<>();

    private String getDefaultTalk(int npcid) {
        String talk = npcDefaultTalks.get(npcid);
        if (talk == null) {
            talk = LifeFactory.getNPCDefaultTalk(npcid);
            npcDefaultTalks.put(npcid, talk);
        }

        return talk;
    }

    private static synchronized void initializeItemPools() {
        if (poolsInitialized) return;
        // Chair Setup
        SET_UP.add(3010000); // The Relaxer
        SET_UP.add(3010001); // Sky-blue Wooden Chair
        SET_UP.add(3010002); // Green Chair
        SET_UP.add(3010003); // Red Chair
        SET_UP.add(3010004); // The Yellow Relaxer
        SET_UP.add(3010005); // The Red Relaxer
        SET_UP.add(3010006); // Yellow Chair
        SET_UP.add(3010007); // Pink Seal Cushion
        SET_UP.add(3010008); // Blue Seal Cushion
        SET_UP.add(3010009); // Red Round Chair
        SET_UP.add(3010010); // White Seal Cushion
        SET_UP.add(3010011); // Amorian Relaxer
        SET_UP.add(3010012); // Warrior Throne
        SET_UP.add(3010013); // Beach Chair
        SET_UP.add(3010014); // Moon Star Chair
        SET_UP.add(3010015); // The Red Relaxer
        SET_UP.add(3010016); // Grey Seal Cushion
        SET_UP.add(3010017); // Gold Seal Cushion
        SET_UP.add(3010018); // Palm Tree Beach Chair
        SET_UP.add(3010019); // Kadomatsu
        SET_UP.add(3010025); // Under the Maple Tree...
        SET_UP.add(3011000); // Fishing Chair
        SET_UP.add(3010040); // The Stirge Seat
        SET_UP.add(3010041); // Skull Throne
        SET_UP.add(3010043); // Halloween Broomstick Chair
        //SET_UP.add(3010044); // Winter Red Chair --> Item missing from wz files, will crash
        SET_UP.add(3010045); // Ice Chair
        SET_UP.add(3010046); // Dragon Chair(Inferno)
        SET_UP.add(3010047); // Dragon Chair(Abyss)
        SET_UP.add(3010057); // BloodyRose
        SET_UP.add(3010058); // WorldEnd
        SET_UP.add(3010060); // Noblesse Chair
        SET_UP.add(3010061); // Underneath the Maple Tree
        SET_UP.add(3010062); // Bamboo Chair
        SET_UP.add(3010063); // Moon and Star Cushion
        SET_UP.add(3010064); // Male Desert Rabbit Cushion
        SET_UP.add(3010065); // Pink Beach Parasol
        SET_UP.add(3010066); // Navy Velvet Sofa
        SET_UP.add(3010067); // Red Designer Chair
        SET_UP.add(3010069); // Yellow Robot Chair
        SET_UP.add(3010071); // Mini Shinsoo Chair
        SET_UP.add(3010072); // Miwok Chief's Chair
        SET_UP.add(3010073); // Giant Pink Bean Cushion
        SET_UP.add(3010085); // Olivia's Chair
        SET_UP.add(3010098); // TV Recliner
        SET_UP.add(3010099); // Cuddly Polar Bear
        SET_UP.add(3010101); // Christmas Gift Box
        SET_UP.add(3010106); // Ryko Chair
        SET_UP.add(3010111); // Tiger Skin Chair
        SET_UP.add(3010116); // The Spirit of Rock Chair
        SET_UP.add(3012005); // Amorian Loveseat
        SET_UP.add(3012010); // Half-Heart Chocolate Cake Chair
        SET_UP.add(3012011); // Chocolate Fondue Chair


        // Taming items
        TAMING_ITEMS.add(1902000); // Hog
        TAMING_ITEMS.add(1902001); // Silver Mane
        TAMING_ITEMS.add(1902002); // Red Draco
        TAMING_ITEMS.add(1902005); // Mimiana
        TAMING_ITEMS.add(1902006); // Mimio
        TAMING_ITEMS.add(1902007); // Shinjou
        TAMING_ITEMS.add(1902008); // Frog
        TAMING_ITEMS.add(1902009); // Ostrich
        TAMING_ITEMS.add(1902010); // Frog (Level 70)
        TAMING_ITEMS.add(1902011); // Turtle
        TAMING_ITEMS.add(1902012); // Yeti
        TAMING_ITEMS.add(1902015); // Werewolf
        TAMING_ITEMS.add(1902016); // Werewolf
        TAMING_ITEMS.add(1902017); // Werewolf
        TAMING_ITEMS.add(1902018); // Ryko
        TAMING_ITEMS.add(1902020); // Hot Air Balloon
        TAMING_ITEMS.add(1902021); // Robot
        TAMING_ITEMS.add(1902036); // Maple Racing Car
        TAMING_ITEMS.add(1902038); // Pink Scooter
        TAMING_ITEMS.add(1902039); // Black Scooter

        TAMING_ITEMS.add(1912000); // Saddle
        TAMING_ITEMS.add(1912003); // Frog Cover
        TAMING_ITEMS.add(1912004); // Ostrich Cover
        TAMING_ITEMS.add(1912005); // Saddle
        TAMING_ITEMS.add(1912006); // Frog Cover (Level 70)
        TAMING_ITEMS.add(1912007); // Turtle Mount
        TAMING_ITEMS.add(1912008); // Yeti Cover
        TAMING_ITEMS.add(1912011); // Wolf Saddle
        TAMING_ITEMS.add(1912013); // Hot Air Balloon Cover
        TAMING_ITEMS.add(1912014); // Robot Cover
        TAMING_ITEMS.add(1912029); // Maple Racing Car Cover
        TAMING_ITEMS.add(1912031); // Pink Scooter Key
        TAMING_ITEMS.add(1912032); // Black Scooter Key

        // Shields
        WEAPON_SHIELD_ITEMS.add(1092052); // Black Phoenix Shield
        WEAPON_SHIELD_ITEMS.add(1092056); // Transparent Shield
        WEAPON_SHIELD_ITEMS.add(1092057); // Timeless Prelude
        WEAPON_SHIELD_ITEMS.add(1092058); // Timeless Kite Shield
        WEAPON_SHIELD_ITEMS.add(1092059); // Timeless List
        WEAPON_SHIELD_ITEMS.add(1092060); // Blue Dragon Shield
        WEAPON_SHIELD_ITEMS.add(1092061); // Crossheider

        // One-handed swords
        WEAPON_SHIELD_ITEMS.add(1302080); // Maplemas Lights
        WEAPON_SHIELD_ITEMS.add(1302081); // Timeless Executioners

        // One-handed axes
        WEAPON_SHIELD_ITEMS.add(1312037); // Timeless Bardiche

        // One-handed blunt weapons
        WEAPON_SHIELD_ITEMS.add(1322054); // Maple Havoc Hammer
        WEAPON_SHIELD_ITEMS.add(1322060); // Timeless Allargando

        // Daggers
        WEAPON_SHIELD_ITEMS.add(1332056); // Maple Asura Dagger
        WEAPON_SHIELD_ITEMS.add(1332064); // Nebula Dagger 1 (LUK)
        WEAPON_SHIELD_ITEMS.add(1332065); // Nebula Dagger 2 (STR)
        WEAPON_SHIELD_ITEMS.add(1332073); // Timeless Pescas

        // Wands
        WEAPON_SHIELD_ITEMS.add(1372014); // Evil Tale
        WEAPON_SHIELD_ITEMS.add(1372015); // Angel Wings
        WEAPON_SHIELD_ITEMS.add(1372016); // Phoenix Wand
        WEAPON_SHIELD_ITEMS.add(1372034); // Maple Shine Wand
        WEAPON_SHIELD_ITEMS.add(1372044); // Timeless Enreal Tear

        // Staves
        WEAPON_SHIELD_ITEMS.add(1382039); // Maple Wisdom Staff
        WEAPON_SHIELD_ITEMS.add(1382053); // Celestial Staff
        WEAPON_SHIELD_ITEMS.add(1382057); // Timeless Aeas Hand

        // Two-handed swords
        WEAPON_SHIELD_ITEMS.add(1402015); // Heaven's Gate
        WEAPON_SHIELD_ITEMS.add(1402016); // Devil's Sunrise
        WEAPON_SHIELD_ITEMS.add(1402039); // Maple Soul Rohen
        WEAPON_SHIELD_ITEMS.add(1402046); // Timeless Nibleheim

        // Two-handed axes
        WEAPON_SHIELD_ITEMS.add(1412033); // Timeless Tabarzin

        // Two-handed blunt weapons
        WEAPON_SHIELD_ITEMS.add(1422037); // Timeless Bellocce

        // Spears
        WEAPON_SHIELD_ITEMS.add(1432040); // Maple Soul Spear
        WEAPON_SHIELD_ITEMS.add(1432045); // Sunspear
        
        // Pole arms
        WEAPON_SHIELD_ITEMS.add(1442051); // Maple Karstan
        WEAPON_SHIELD_ITEMS.add(1442060); // Heavenly Messenger
        WEAPON_SHIELD_ITEMS.add(1442063); // Timeless Diesra

        // Bows
        WEAPON_SHIELD_ITEMS.add(1452045); // Maple Kandiva Bow
        WEAPON_SHIELD_ITEMS.add(1452052); // Andromeda Bow

        // Crossbows
        WEAPON_SHIELD_ITEMS.add(1462040); // Maple Nishada
        WEAPON_SHIELD_ITEMS.add(1462046); // Void Hunter

        // Claws
        WEAPON_SHIELD_ITEMS.add(1472023); // Blood Gigantic
        WEAPON_SHIELD_ITEMS.add(1472062); // Black Hole

        //others
        WEAPON_SHIELD_ITEMS.add(1322027); // frying pan
        WEAPON_SHIELD_ITEMS.add(1322027); // pink tube
        WEAPON_SHIELD_ITEMS.add(1322021); // Black Tube - (no description)
        WEAPON_SHIELD_ITEMS.add(1322022); // Red Flowery Tube - (no description)
        WEAPON_SHIELD_ITEMS.add(1322023); // Blue Flowery Tube - (no description)
        WEAPON_SHIELD_ITEMS.add(1322024); // Purple Tube - (no description)
        WEAPON_SHIELD_ITEMS.add(1322025); // Emergency Rescue Tube - (no description)
        WEAPON_SHIELD_ITEMS.add(1322026); // Colorful Tube - (no description)
        WEAPON_SHIELD_ITEMS.add(1442021); // Yellow Mop - (no description)
        WEAPON_SHIELD_ITEMS.add(1442022); // White Mop - (no description)
        WEAPON_SHIELD_ITEMS.add(1442023); // Maroon Mop - (no description)

        // Knucklers
        WEAPON_SHIELD_ITEMS.add(1482008); // Psycho Claw
        WEAPON_SHIELD_ITEMS.add(1482022); // Maple Golden Claw

        // Guns
        WEAPON_SHIELD_ITEMS.add(1492006); // Lunar Shooter
        WEAPON_SHIELD_ITEMS.add(1492010); // Infinity's Wrath

        // Stat-giving Rings
        ACCESSORIES_ITEMS.add(1112300); // Ring of Moon Stone 1Carats
        ACCESSORIES_ITEMS.add(1112301); // Ring of Moon Stone: 2 Carats
        ACCESSORIES_ITEMS.add(1112302); // Ring of Moon Stone 3Carats
        ACCESSORIES_ITEMS.add(1112303); // Ring of Shining Star 1Carats
        ACCESSORIES_ITEMS.add(1112304); // Ring of Shining Star 2Carats
        ACCESSORIES_ITEMS.add(1112305); // Ring of Shining Star 3Carats
        ACCESSORIES_ITEMS.add(1112306); // Gold Heart Ring 1Carats
        ACCESSORIES_ITEMS.add(1112307); // Gold Heart Ring: 2 Carats
        ACCESSORIES_ITEMS.add(1112308); // Gold Heart Ring: 3 Carats
        ACCESSORIES_ITEMS.add(1112309); // Ring of Silver Wing 1Carats
        ACCESSORIES_ITEMS.add(1112310); // Ring of Silver Wing: 2 Carats
        ACCESSORIES_ITEMS.add(1112311); // Ring of Silver Wing: 3 Carats
        ACCESSORIES_ITEMS.add(1112400); // Ring of Alchemist
        ACCESSORIES_ITEMS.add(1112401); // Spiegelmann's Ring
        ACCESSORIES_ITEMS.add(1112402); // Spiegelmann's Ring
        ACCESSORIES_ITEMS.add(1112405); // Lilin's Ring
        ACCESSORIES_ITEMS.add(1112407); // Circle of Ancient Thought
        ACCESSORIES_ITEMS.add(1112408); // Circle of Ancient Strength
        ACCESSORIES_ITEMS.add(1112413); // Lilin's Ring
        ACCESSORIES_ITEMS.add(1112414); // Lilin's Ring
        ACCESSORIES_ITEMS.add(1112908); // Aura Ring (explicitly mentions +1 to all stats)
        ACCESSORIES_ITEMS.add(1112916); // Solo Ring
        ACCESSORIES_ITEMS.add(1122003); // bow
        ACCESSORIES_ITEMS.add(1122000); // Horntail Necklace
        ACCESSORIES_ITEMS.add(1122001); // Bow-tie(Green)
        ACCESSORIES_ITEMS.add(1022071); //  Red Shutter Shades
        ACCESSORIES_ITEMS.add(1122002); // Bow-tie (Red) - (no description)
        ACCESSORIES_ITEMS.add(1122004); // Bow-tie (Pink) - (no description)
        ACCESSORIES_ITEMS.add(1122005); // Bow-tie (Black) - (no description)
        ACCESSORIES_ITEMS.add(1122006); // Bow-tie (Blue) - (no description)
        ACCESSORIES_ITEMS.add(1122007); // Spiegelmann's Necklace - (no description)
        ACCESSORIES_ITEMS.add(1122010); // Horus' Eye - (no description)
        ACCESSORIES_ITEMS.add(1122014); // Silver Deputy Star - A badge of honor given by Lita Lawless for vanquishing monsters in defense of NLC. You're part of the law now!
       
        ACCESSORIES_ITEMS.add(1022069); // Orange Shutter Shades - (no description)
        ACCESSORIES_ITEMS.add(1012134); // Tear Drop Face Tattoo - Express your soft side with this teary-eyed Tattoo!
        ACCESSORIES_ITEMS.add(1122059); // Mark of Naricain
       
        ACCESSORIES_ITEMS.add(1032060); // Altair Earrings - (no description)
        ACCESSORIES_ITEMS.add(1022073); // Broken Glasses - (no description)
        ACCESSORIES_ITEMS.add(1032061); // (no description)

        USE_ITEMS.add(2060004); // Diamond Arrow for Bow
        USE_ITEMS.add(2060003); // Red Arrow for Bow
        USE_ITEMS.add(2060005); // Snowball
        USE_ITEMS.add(2060006); // Big Snowball
        USE_ITEMS.add(2061004); // Diamond Arrow for Crossbow
        USE_ITEMS.add(2061003); // Blue Arrow for Crossbow 
        USE_ITEMS.add(2070001); // Wolbi Throwing-Stars
        USE_ITEMS.add(2070002); // Mokbi Throwing-Stars
        USE_ITEMS.add(2070003); // Kumbi Throwing-Stars
        USE_ITEMS.add(2070004); // Tobi Throwing-Stars
        USE_ITEMS.add(2070005); // Steely Throwing-Knives
        USE_ITEMS.add(2070006); // Ilbi Throwing-Stars
        USE_ITEMS.add(2070007); // Hwabi Throwing-Stars
        USE_ITEMS.add(2070008); // Snowball
        USE_ITEMS.add(2070009); // Wooden Top
        USE_ITEMS.add(2070010); // Icicle
        USE_ITEMS.add(2070011); // Maple Throwing-Stars
        USE_ITEMS.add(2070012); // Paper Fighter Plane
        USE_ITEMS.add(2070013); // Orange
        USE_ITEMS.add(2070016); // Crystal Ilbi Throwing-Stars
        USE_ITEMS.add(2070018); // Balanced Fury
        USE_ITEMS.add(2330005); // Eternal Bullet
        USE_ITEMS.add(2330004); // Shiny Bullet
        USE_ITEMS.add(2331000); // Blaze Capsule
        USE_ITEMS.add(2332000); // Glaze Capsule
        USE_ITEMS.add(2022282); // Naricain's Demon Elixir
        USE_ITEMS.add(2022273); // Ssiws Cheese
        USE_ITEMS.add(2022278); // Lump of Coal
        USE_ITEMS.add(2022546); // Energy Drink

        // Transformation potion, disabled until crash is fixed
        
        //USE_ITEMS.add(2210000); // Orange Mushroom Piece
        //USE_ITEMS.add(2210003); // Dragon Elixir
        //USE_ITEMS.add(2210004); // Blue Ribbon Pig Piece
        //USE_ITEMS.add(2210005); // Tigun Transformation Bundle.
        //USE_ITEMS.add(2210007); // Change to Ghost
        //USE_ITEMS.add(2210008); // Ghost Candy
        //USE_ITEMS.add(2210012); // Change to Mouse
        //USE_ITEMS.add(2210018); // Sweet Rice Cake
        //USE_ITEMS.add(2210021); // Gaga Transformation Potion
        //USE_ITEMS.add(2210032); // Cody's Picture
        //USE_ITEMS.add(2210033); // Cake Picture
        //USE_ITEMS.add(2210034); // Alien Gray Transformation
        //USE_ITEMS.add(2210035); // Penguin Transformation 1
        //USE_ITEMS.add(2210036); // Penguin Transformation 2
        //USE_ITEMS.add(2210037); // Penguin Transformation 3
        //USE_ITEMS.add(2210038); // Penguin Transformation 4
        //USE_ITEMS.add(2210039); // Penguin Transformation 5

        // Add all boots items to EQUIPMENT_ITEMS
        
        EQUIPMENT_ITEMS.add(1072344); // Facestompers

        // Add all cape items to equipment items
        EQUIPMENT_ITEMS.add(1102084); // Pink Gaia Cape
        EQUIPMENT_ITEMS.add(1102086); // Purple Gaia Cape

        // Stat-giving Gloves
        
        EQUIPMENT_ITEMS.add(1082223); // Stormcaster Gloves

        // Scrolls

        SCROLLS_ITEMS.add(2040760); // Scroll for Shoes for ATT 10%
        SCROLLS_ITEMS.add(2040727); // Scroll for Spikes on Shoes 10%
        SCROLLS_ITEMS.add(2040002); // Scroll for Overall Armor for INT 10% (Assuming this is the correct item ID)
        SCROLLS_ITEMS.add(2040502); // Scroll for Overall Armor for DEX 10%
        SCROLLS_ITEMS.add(2040517); // Scroll for Overall Armor for LUK 10%
        SCROLLS_ITEMS.add(2040816); // Scroll for Gloves for Magic Att. 10%
        SCROLLS_ITEMS.add(2040920); // Scroll for Shield for Magic Att 10%
        SCROLLS_ITEMS.add(2048010); // Scroll for Pet Equip. for STR 60%
        SCROLLS_ITEMS.add(2048011); // Scroll for Pet Equip. for INT 60%
        SCROLLS_ITEMS.add(2048012); // Scroll for Pet Equip. for DEX 60%
        SCROLLS_ITEMS.add(2048013); // Scroll for Pet Equip. for LUK 60%
        SCROLLS_ITEMS.add(2040915); // Scroll for Shield for Weapon Att 10%
        SCROLLS_ITEMS.add(2040323); // Scroll for Earring for LUK 10%
        SCROLLS_ITEMS.add(2040205); // Scroll for Eye Accessory for INT 10%
        SCROLLS_ITEMS.add(2040105); // Scroll for Face Accessory for Avoidability 10%
        SCROLLS_ITEMS.add(2049207); // Dark Scroll for Accessory for LUK 30%
        SCROLLS_ITEMS.add(2049201); // Dark Scroll for Accessory for STR 30%
        SCROLLS_ITEMS.add(2049203); // Dark Scroll for Accessory for DEX 30%
        SCROLLS_ITEMS.add(2049205); // Dark Scroll for Accessory for INT 30%

        poolsInitialized = true;
        System.out.println("Gachapon item pools initialized successfully!");
    }


    public NPCConversationManager(Client c, int npc, String scriptName) {
        this(c, npc, -1, scriptName, false);
    }

    public NPCConversationManager(Client c, int npc, List<PartyCharacter> otherParty, boolean test) {
        super(c);
        this.c = c;
        this.npc = npc;
        this.otherParty = otherParty;
    }

    public NPCConversationManager(Client c, int npc, int oid, String scriptName, boolean itemScript) {
        super(c);
        this.npc = npc;
        this.npcOid = oid;
        this.scriptName = scriptName;
        this.itemScript = itemScript;

        if ((npc >= 9100100 && npc <= 9100111) || npc == 9100117) // gachapons
        {
            initializeItemPools();
        }
    }

    public int getNpc() {
        return npc;
    }

    public int getNpcObjectId() {
        return npcOid;
    }

    public String getScriptName() {
        return scriptName;
    }

    public boolean isItemScript() {
        return itemScript;
    }

    public void resetItemScript() {
        this.itemScript = false;
    }

    public void dispose() {
        NPCScriptManager.getInstance().dispose(this);
        getClient().sendPacket(PacketCreator.enableActions());
    }

    public void sendNext(String text) {
        getClient().sendPacket(PacketCreator.getNPCTalk(npc, (byte) 0, text, "00 01", (byte) 0));
    }

    public void sendPrev(String text) {
        getClient().sendPacket(PacketCreator.getNPCTalk(npc, (byte) 0, text, "01 00", (byte) 0));
    }

    public void sendNextPrev(String text) {
        getClient().sendPacket(PacketCreator.getNPCTalk(npc, (byte) 0, text, "01 01", (byte) 0));
    }

    public void sendOk(String text) {
        getClient().sendPacket(PacketCreator.getNPCTalk(npc, (byte) 0, text, "00 00", (byte) 0));
    }

    public void sendDefault() {
        sendOk(getDefaultTalk(npc));
    }

    public void sendYesNo(String text) {
        getClient().sendPacket(PacketCreator.getNPCTalk(npc, (byte) 1, text, "", (byte) 0));
    }

    public void sendAcceptDecline(String text) {
        getClient().sendPacket(PacketCreator.getNPCTalk(npc, (byte) 0x0C, text, "", (byte) 0));
    }

    public void sendSimple(String text) {
        getClient().sendPacket(PacketCreator.getNPCTalk(npc, (byte) 4, text, "", (byte) 0));
    }

    public void sendNext(String text, byte speaker) {
        getClient().sendPacket(PacketCreator.getNPCTalk(npc, (byte) 0, text, "00 01", speaker));
    }

    public void sendPrev(String text, byte speaker) {
        getClient().sendPacket(PacketCreator.getNPCTalk(npc, (byte) 0, text, "01 00", speaker));
    }

    public void sendNextPrev(String text, byte speaker) {
        getClient().sendPacket(PacketCreator.getNPCTalk(npc, (byte) 0, text, "01 01", speaker));
    }

    public void sendOk(String text, byte speaker) {
        getClient().sendPacket(PacketCreator.getNPCTalk(npc, (byte) 0, text, "00 00", speaker));
    }

    public void sendYesNo(String text, byte speaker) {
        getClient().sendPacket(PacketCreator.getNPCTalk(npc, (byte) 1, text, "", speaker));
    }

    public void sendAcceptDecline(String text, byte speaker) {
        getClient().sendPacket(PacketCreator.getNPCTalk(npc, (byte) 0x0C, text, "", speaker));
    }

    public void sendSimple(String text, byte speaker) {
        getClient().sendPacket(PacketCreator.getNPCTalk(npc, (byte) 4, text, "", speaker));
    }

    public void sendStyle(String text, int[] styles) {
        if (styles.length > 0) {
            getClient().sendPacket(PacketCreator.getNPCTalkStyle(npc, text, styles));
        } else {    // thanks Conrad for noticing empty styles crashing players
            sendOk("Sorry, there are no options of cosmetics available for you here at the moment.");
            dispose();
        }
    }

    public void sendGetNumber(String text, int def, int min, int max) {
        getClient().sendPacket(PacketCreator.getNPCTalkNum(npc, text, def, min, max));
    }

    public void sendGetText(String text) {
        getClient().sendPacket(PacketCreator.getNPCTalkText(npc, text, ""));
    }

    /*
     * 0 = ariant colliseum
     * 1 = Dojo
     * 2 = Carnival 1
     * 3 = Carnival 2
     * 4 = Ghost Ship PQ?
     * 5 = Pyramid PQ
     * 6 = Kerning Subway
     */
    public void sendDimensionalMirror(String text) {
        getClient().sendPacket(PacketCreator.getDimensionalMirror(text));
    }

    public void setGetText(String text) {
        this.getText = text;
    }

    public String getText() {
        return this.getText;
    }

    @Override
    public boolean forceStartQuest(int id) {
        return forceStartQuest(id, npc);
    }

    @Override
    public boolean forceCompleteQuest(int id) {
        return forceCompleteQuest(id, npc);
    }

    @Override
    public boolean startQuest(short id) {
        return startQuest((int) id);
    }

    @Override
    public boolean completeQuest(short id) {
        return completeQuest((int) id);
    }

    @Override
    public boolean startQuest(int id) {
        return startQuest(id, npc);
    }

    @Override
    public boolean completeQuest(int id) {
        return completeQuest(id, npc);
    }

    public int getMeso() {
        return getPlayer().getMeso();
    }

    public void gainMeso(int gain) {
        getPlayer().gainMeso(gain);
    }

    public void gainMeso(Double gain) {
        getPlayer().gainMeso(gain.intValue());
    }

    public void gainExp(int gain) {
        getPlayer().gainExp(gain, true, true);
    }

    @Override
    public void showEffect(String effect) {
        getPlayer().getMap().broadcastMessage(PacketCreator.environmentChange(effect, 3));
    }

    public void setHair(int hair) {
        getPlayer().setHair(hair);
        getPlayer().updateSingleStat(Stat.HAIR, hair);
        getPlayer().equipChanged();
    }

    public void setFace(int face) {
        getPlayer().setFace(face);
        getPlayer().updateSingleStat(Stat.FACE, face);
        getPlayer().equipChanged();
    }

    public void setSkin(int color) {
        getPlayer().setSkinColor(SkinColor.getById(color));
        getPlayer().updateSingleStat(Stat.SKIN, color);
        getPlayer().equipChanged();
    }

    public int itemQuantity(int itemid) {
        return getPlayer().getInventory(ItemConstants.getInventoryType(itemid)).countById(itemid);
    }

    public void displayGuildRanks() {
        Guild.displayGuildRanks(getClient(), npc);
    }

    public boolean canSpawnPlayerNpc(int mapid) {
        Character chr = getPlayer();
        return !YamlConfig.config.server.PLAYERNPC_AUTODEPLOY && chr.getLevel() >= chr.getMaxClassLevel() && !chr.isGM() && PlayerNPC.canSpawnPlayerNpc(chr.getName(), mapid);
    }

    public PlayerNPC getPlayerNPCByScriptid(int scriptId) {
        for (MapObject pnpcObj : getPlayer().getMap().getMapObjectsInRange(new Point(0, 0), Double.POSITIVE_INFINITY, Arrays.asList(MapObjectType.PLAYER_NPC))) {
            PlayerNPC pn = (PlayerNPC) pnpcObj;

            if (pn.getScriptId() == scriptId) {
                return pn;
            }
        }

        return null;
    }

    @Override
    public Party getParty() {
        return getPlayer().getParty();
    }

    @Override
    public void resetMap(int mapid) {
        getClient().getChannelServer().getMapFactory().getMap(mapid).resetReactors();
    }

    public void gainTameness(int tameness) {
        for (Pet pet : getPlayer().getPets()) {
            if (pet != null) {
                pet.gainTamenessFullness(getPlayer(), tameness, 0, 0);
            }
        }
    }

    public String getName() {
        return getPlayer().getName();
    }

    public int getGender() {
        return getPlayer().getGender();
    }

    public void changeJobById(int a) {
        getPlayer().changeJob(Job.getById(a));
    }

    public void changeJob(Job job) {
        getPlayer().changeJob(job);
    }

    public String getJobName(int id) {
        return GameConstants.getJobName(id);
    }

    public StatEffect getItemEffect(int itemId) {
        return ItemInformationProvider.getInstance().getItemEffect(itemId);
    }

    public void resetStats() {
        getPlayer().resetStats();
    }

    public void openShopNPC(int id) {
        Shop shop = ShopFactory.getInstance().getShop(id);

        if (shop != null) {
            shop.sendShop(c);
        } else {    // check for missing shopids thanks to resinate
            log.warn("Shop ID: {} is missing from database.", id);
            ShopFactory.getInstance().getShop(11000).sendShop(c);
        }
    }

    public void maxMastery() {
        for (Data skill_ : DataProviderFactory.getDataProvider(WZFiles.STRING).getData("Skill.img").getChildren()) {
            try {
                Skill skill = SkillFactory.getSkill(Integer.parseInt(skill_.getName()));
                getPlayer().changeSkillLevel(skill, (byte) 0, skill.getMaxLevel(), -1);
            } catch (NumberFormatException nfe) {
                nfe.printStackTrace();
                break;
            } catch (NullPointerException npe) {
                npe.printStackTrace();
                continue;
            }
        }
    }

    public void doGachapon() {
        GachaponItem item = Gachapon.getInstance().process(npc);
        Item itemGained = gainItem(item.getId(), (short) (item.getId() / 10000 == 200 ? 100 : 1), true, true); // For normal potions, make it give 100.

        sendNext("You have obtained a #b#t" + item.getId() + "##k.");

        int[] maps = {MapId.HENESYS, MapId.ELLINIA, MapId.PERION, MapId.KERNING_CITY, MapId.SLEEPYWOOD, MapId.MUSHROOM_SHRINE,
                MapId.SHOWA_SPA_M, MapId.SHOWA_SPA_F, MapId.NEW_LEAF_CITY, MapId.NAUTILUS_HARBOR};
        final int mapId = maps[(getNpc() != NpcId.GACHAPON_NAUTILUS && getNpc() != NpcId.GACHAPON_NLC) ?
                (getNpc() - NpcId.GACHAPON_HENESYS) : getNpc() == NpcId.GACHAPON_NLC ? 8 : 9];
        String map = c.getChannelServer().getMapFactory().getMap(mapId).getMapName();

        Gachapon.log(getPlayer(), item.getId(), map);

        if (item.getTier() > 0) { //Uncommon and Rare
            Server.getInstance().broadcastMessage(c.getWorld(), PacketCreator.gachaponMessage(itemGained, map, getPlayer()));
        }
    }

    public void doGroupGachapon(int groupId) {
        // Define item ID ranges for each group
        List<Integer> itemPool = new ArrayList<>();

        switch (groupId) {
            case 0: // Taming
                itemPool = TAMING_ITEMS;
                break;

            case 1: // Weapons & Shields
                itemPool = WEAPON_SHIELD_ITEMS;
                break;

            case 2: // Rings, Capes, Gloves, Face & Accessories
                itemPool = ACCESSORIES_ITEMS;
                break;

            case 3: // Cap, Coat, Longcoat, Pants & Shoes
                itemPool = EQUIPMENT_ITEMS;
                break;

            case 4: // Uses
                itemPool = USE_ITEMS;
                break;
                
            case 5: // Setups
                itemPool = SET_UP;
                break;
                
            case 6: // Scrolls
                itemPool = SCROLLS_ITEMS;
                break;
        }

        // Get a random item from the pool
        if (itemPool.isEmpty()) {
            // Fallback to regular gachapon if pool is empty
            doGachapon();
            return;
        }

        // Get a random item from the pool
        int randomIndex = (int) (Math.random() * itemPool.size());
        int itemId = itemPool.get(randomIndex);
        log.info("Gachapon random item id: {}", itemId);
        // Create a GachaponItem object (you may need to adjust this based on your implementation)

        // Give the item to the player
        getPlayer().getAbstractPlayerInteraction().gainItem(itemId);
    }

    private void addSpecificItem(List<Integer> pool, int itemId) {
        // Only add the item if it exists in the database
        pool.add(itemId);
    }
    /**
     * Helper method to add item IDs to the pool within a range
     */
    private void addItemsToPool(List<Integer> pool, int startId, int endId) {
        // This is a simplified implementation
        // In a real implementation, you would:
        // 1. Check if items exist in the database
        // 2. Filter out unavailable items
        // 3. Consider item drop rates/rarity

        // For simplicity, we're just adding items within the range
        // You should customize this to match your server's item database
        for (int id = startId; id <= endId; id++) {
            // Only add items that exist and should be available
            // This is a placeholder - you'd check against your item database
            pool.add(id);
        }

        // Add specific notable items for each range if desired
        // pool.add(specificItemId);
    }


    public void upgradeAlliance() {
        Alliance alliance = Server.getInstance().getAlliance(c.getPlayer().getGuild().getAllianceId());
        alliance.increaseCapacity(1);

        Server.getInstance().allianceMessage(alliance.getId(), GuildPackets.getGuildAlliances(alliance, c.getWorld()), -1, -1);
        Server.getInstance().allianceMessage(alliance.getId(), GuildPackets.allianceNotice(alliance.getId(), alliance.getNotice()), -1, -1);

        c.sendPacket(GuildPackets.updateAllianceInfo(alliance, c.getWorld()));  // thanks Vcoc for finding an alliance update to leader issue
    }

    public void disbandAlliance(Client c, int allianceId) {
        Alliance.disbandAlliance(allianceId);
    }

    public boolean canBeUsedAllianceName(String name) {
        return Alliance.canBeUsedAllianceName(name);
    }

    public Alliance createAlliance(String name) {
        return Alliance.createAlliance(getParty(), name);
    }

    public int getAllianceCapacity() {
        return Server.getInstance().getAlliance(getPlayer().getGuild().getAllianceId()).getCapacity();
    }

    public boolean hasMerchant() {
        return getPlayer().hasMerchant();
    }

    public boolean hasMerchantItems() {
        try {
            if (!ItemFactory.MERCHANT.loadItems(getPlayer().getId(), false).isEmpty()) {
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        return getPlayer().getMerchantMeso() != 0;
    }

    public void showFredrick() {
        c.sendPacket(PacketCreator.getFredrick(getPlayer()));
    }

    public int partyMembersInMap() {
        int inMap = 0;
        for (Character char2 : getPlayer().getMap().getCharacters()) {
            if (char2.getParty() == getPlayer().getParty()) {
                inMap++;
            }
        }
        return inMap;
    }

    public Event getEvent() {
        return c.getChannelServer().getEvent();
    }

    public void divideTeams() {
        if (getEvent() != null) {
            getPlayer().setTeam(getEvent().getLimit() % 2); //muhaha :D
        }
    }

    public Character getMapleCharacter(String player) {
        Character target = Server.getInstance().getWorld(c.getWorld()).getChannel(c.getChannel()).getPlayerStorage().getCharacterByName(player);
        return target;
    }

    public void logLeaf(String prize) {
        MapleLeafLogger.log(getPlayer(), true, prize);
    }

    public boolean createPyramid(String mode, boolean party) {//lol
        PyramidMode mod = PyramidMode.valueOf(mode);

        Party partyz = getPlayer().getParty();
        MapManager mapManager = c.getChannelServer().getMapFactory();

        MapleMap map = null;
        int mapid = MapId.NETTS_PYRAMID_SOLO_BASE;
        if (party) {
            mapid += 10000;
        }
        mapid += (mod.getMode() * 1000);

        for (byte b = 0; b < 5; b++) {//They cannot warp to the next map before the timer ends (:
            map = mapManager.getMap(mapid + b);
            if (map.getCharacters().size() > 0) {
                continue;
            } else {
                break;
            }
        }

        if (map == null) {
            return false;
        }

        if (!party) {
            partyz = new Party(-1, new PartyCharacter(getPlayer()));
        }
        Pyramid py = new Pyramid(partyz, mod, map.getId());
        getPlayer().setPartyQuest(py);
        py.warp(mapid);
        dispose();
        return true;
    }

    public boolean itemExists(int itemid) {
        return ItemInformationProvider.getInstance().getName(itemid) != null;
    }

    public int getCosmeticItem(int itemid) {
        if (itemExists(itemid)) {
            return itemid;
        }

        int baseid;
        if (itemid < 30000) {
            baseid = (itemid / 1000) * 1000 + (itemid % 100);
        } else {
            baseid = (itemid / 10) * 10;
        }

        return itemid != baseid && itemExists(baseid) ? baseid : -1;
    }

    private int getEquippedCosmeticid(int itemid) {
        if (itemid < 30000) {
            return getPlayer().getFace();
        } else {
            return getPlayer().getHair();
        }
    }

    public boolean isCosmeticEquipped(int itemid) {
        return getEquippedCosmeticid(itemid) == itemid;
    }

    public boolean isUsingOldPqNpcStyle() {
        return YamlConfig.config.server.USE_OLD_GMS_STYLED_PQ_NPCS && this.getPlayer().getParty() != null;
    }

    public Object[] getAvailableMasteryBooks() {
        return ItemInformationProvider.getInstance().usableMasteryBooks(this.getPlayer()).toArray();
    }

    public Object[] getAvailableSkillBooks() {
        List<Integer> ret = ItemInformationProvider.getInstance().usableSkillBooks(this.getPlayer());
        ret.addAll(SkillbookInformationProvider.getTeachableSkills(this.getPlayer()));

        return ret.toArray();
    }

    public Object[] getNamesWhoDropsItem(Integer itemId) {
        return ItemInformationProvider.getInstance().getWhoDrops(itemId).toArray();
    }

    public String getSkillBookInfo(int itemid) {
        SkillBookEntry sbe = SkillbookInformationProvider.getSkillbookAvailability(itemid);
        switch (sbe) {
            case UNAVAILABLE:
                return "";

            case REACTOR:
                return "    Obtainable through #rexploring#k (loot boxes).";

            case SCRIPT:
                return "    Obtainable through #rexploring#k (field interaction).";

            case QUEST_BOOK:
                return "    Obtainable through #rquestline#k (collecting book).";

            case QUEST_REWARD:
                return "    Obtainable through #rquestline#k (quest reward).";

            default:
                return "    Obtainable through #rquestline#k.";
        }
    }

    // (CPQ + WED wishlist) by -- Drago (Dragohe4rt)
    public int cpqCalcAvgLvl(int map) {
        int num = 0;
        int avg = 0;
        for (MapObject mmo : c.getChannelServer().getMapFactory().getMap(map).getAllPlayer()) {
            avg += ((Character) mmo).getLevel();
            num++;
        }
        avg /= num;
        return avg;
    }

    public boolean sendCPQMapLists() {
        String msg = LanguageConstants.getMessage(getPlayer(), LanguageConstants.CPQPickRoom);
        int msgLen = msg.length();
        for (int i = 0; i < 6; i++) {
            if (fieldTaken(i)) {
                if (fieldLobbied(i)) {
                    msg += "#b#L" + i + "#Carnival Field " + (i + 1) + " (Level: "  // "Carnival field" GMS-like improvement thanks to Jayd (jaydenseah)
                            + cpqCalcAvgLvl(980000100 + i * 100) + " / "
                            + getPlayerCount(980000100 + i * 100) + "x"
                            + getPlayerCount(980000100 + i * 100) + ")  #l\r\n";
                }
            } else {
                if (i >= 0 && i <= 3) {
                    msg += "#b#L" + i + "#Carnival Field " + (i + 1) + " (2x2) #l\r\n";
                } else {
                    msg += "#b#L" + i + "#Carnival Field " + (i + 1) + " (3x3) #l\r\n";
                }
            }
        }

        if (msg.length() > msgLen) {
            sendSimple(msg);
            return true;
        } else {
            return false;
        }
    }

    public boolean fieldTaken(int field) {
        if (!c.getChannelServer().canInitMonsterCarnival(true, field)) {
            return true;
        }
        if (!c.getChannelServer().getMapFactory().getMap(980000100 + field * 100).getAllPlayer().isEmpty()) {
            return true;
        }
        if (!c.getChannelServer().getMapFactory().getMap(980000101 + field * 100).getAllPlayer().isEmpty()) {
            return true;
        }
        return !c.getChannelServer().getMapFactory().getMap(980000102 + field * 100).getAllPlayer().isEmpty();
    }

    public boolean fieldLobbied(int field) {
        return !c.getChannelServer().getMapFactory().getMap(980000100 + field * 100).getAllPlayer().isEmpty();
    }

    public void cpqLobby(int field) {
        try {
            final MapleMap map, mapExit;
            Channel cs = c.getChannelServer();

            map = cs.getMapFactory().getMap(980000100 + 100 * field);
            mapExit = cs.getMapFactory().getMap(980000000);
            for (PartyCharacter mpc : c.getPlayer().getParty().getMembers()) {
                final Character mc = mpc.getPlayer();
                if (mc != null) {
                    mc.setChallenged(false);
                    mc.changeMap(map, map.getPortal(0));
                    mc.sendPacket(PacketCreator.serverNotice(6, LanguageConstants.getMessage(mc, LanguageConstants.CPQEntryLobby)));
                    TimerManager tMan = TimerManager.getInstance();
                    tMan.schedule(() -> mapClock((int) MINUTES.toSeconds(3)), 1500);

                    mc.setCpqTimer(TimerManager.getInstance().schedule(() -> mc.changeMap(mapExit, mapExit.getPortal(0)), MINUTES.toMillis(3)));
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public Character getChrById(int id) {
        return c.getChannelServer().getPlayerStorage().getCharacterById(id);
    }

    public void cancelCPQLobby() {
        for (PartyCharacter mpc : c.getPlayer().getParty().getMembers()) {
            Character mc = mpc.getPlayer();
            if (mc != null) {
                mc.clearCpqTimer();
            }
        }
    }

    private void warpoutCPQLobby(MapleMap lobbyMap) {
        MapleMap out = lobbyMap.getChannelServer().getMapFactory().getMap((lobbyMap.getId() < 980030000) ? 980000000 : 980030000);
        for (Character mc : lobbyMap.getAllPlayers()) {
            mc.resetCP();
            mc.setTeam(-1);
            mc.setMonsterCarnival(null);
            mc.changeMap(out, out.getPortal(0));
        }
    }

    private int isCPQParty(MapleMap lobby, Party party) {
        int cpqMinLvl, cpqMaxLvl;

        if (lobby.isCPQLobby()) {
            cpqMinLvl = 30;
            cpqMaxLvl = 50;
        } else {
            cpqMinLvl = 51;
            cpqMaxLvl = 70;
        }

        List<PartyCharacter> partyMembers = party.getPartyMembers();
        for (PartyCharacter pchr : partyMembers) {
            if (pchr.getLevel() >= cpqMinLvl && pchr.getLevel() <= cpqMaxLvl) {
                if (lobby.getCharacterById(pchr.getId()) == null) {
                    return 1;  // party member detected out of area
                }
            } else {
                return 2;  // party member doesn't fit requirements
            }
        }

        return 0;
    }

    private int canStartCPQ(MapleMap lobby, Party party, Party challenger) {
        int ret = isCPQParty(lobby, party);
        if (ret != 0) {
            return ret;
        }

        ret = isCPQParty(lobby, challenger);
        if (ret != 0) {
            return -ret;
        }

        return 0;
    }

    public void startCPQ(final Character challenger, final int field) {
        try {
            cancelCPQLobby();

            final MapleMap lobbyMap = getPlayer().getMap();
            if (challenger != null) {
                if (challenger.getParty() == null) {
                    throw new RuntimeException("No opponent found!");
                }

                for (PartyCharacter mpc : challenger.getParty().getMembers()) {
                    Character mc = mpc.getPlayer();
                    if (mc != null) {
                        mc.changeMap(lobbyMap, lobbyMap.getPortal(0));
                        TimerManager tMan = TimerManager.getInstance();
                        tMan.schedule(() -> mapClock(10), 1500);
                    }
                }
                for (PartyCharacter mpc : getPlayer().getParty().getMembers()) {
                    Character mc = mpc.getPlayer();
                    if (mc != null) {
                        TimerManager tMan = TimerManager.getInstance();
                        tMan.schedule(() -> mapClock(10), 1500);
                    }
                }
            }
            final int mapid = c.getPlayer().getMapId() + 1;
            TimerManager tMan = TimerManager.getInstance();
            tMan.schedule(() -> {
                try {
                    for (PartyCharacter mpc : getPlayer().getParty().getMembers()) {
                        Character mc = mpc.getPlayer();
                        if (mc != null) {
                            mc.setMonsterCarnival(null);
                        }
                    }
                    for (PartyCharacter mpc : challenger.getParty().getMembers()) {
                        Character mc = mpc.getPlayer();
                        if (mc != null) {
                            mc.setMonsterCarnival(null);
                        }
                    }
                } catch (NullPointerException npe) {
                    warpoutCPQLobby(lobbyMap);
                    return;
                }

                Party lobbyParty = getPlayer().getParty(), challengerParty = challenger.getParty();
                int status = canStartCPQ(lobbyMap, lobbyParty, challengerParty);
                if (status == 0) {
                    new MonsterCarnival(lobbyParty, challengerParty, mapid, true, (field / 100) % 10);
                } else {
                    warpoutCPQLobby(lobbyMap);
                }
            }, 11000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void startCPQ2(final Character challenger, final int field) {
        try {
            cancelCPQLobby();

            final MapleMap lobbyMap = getPlayer().getMap();
            if (challenger != null) {
                if (challenger.getParty() == null) {
                    throw new RuntimeException("No opponent found!");
                }

                for (PartyCharacter mpc : challenger.getParty().getMembers()) {
                    Character mc = mpc.getPlayer();
                    if (mc != null) {
                        mc.changeMap(lobbyMap, lobbyMap.getPortal(0));
                        mapClock(10);
                    }
                }
            }
            final int mapid = c.getPlayer().getMapId() + 100;
            TimerManager tMan = TimerManager.getInstance();
            tMan.schedule(() -> {
                try {
                    for (PartyCharacter mpc : getPlayer().getParty().getMembers()) {
                        Character mc = mpc.getPlayer();
                        if (mc != null) {
                            mc.setMonsterCarnival(null);
                        }
                    }
                    for (PartyCharacter mpc : challenger.getParty().getMembers()) {
                        Character mc = mpc.getPlayer();
                        if (mc != null) {
                            mc.setMonsterCarnival(null);
                        }
                    }
                } catch (NullPointerException npe) {
                    warpoutCPQLobby(lobbyMap);
                    return;
                }

                Party lobbyParty = getPlayer().getParty(), challengerParty = challenger.getParty();
                int status = canStartCPQ(lobbyMap, lobbyParty, challengerParty);
                if (status == 0) {
                    new MonsterCarnival(lobbyParty, challengerParty, mapid, false, (field / 1000) % 10);
                } else {
                    warpoutCPQLobby(lobbyMap);
                }
            }, 10000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean sendCPQMapLists2() {
        String msg = LanguageConstants.getMessage(getPlayer(), LanguageConstants.CPQPickRoom);
        int msgLen = msg.length();
        for (int i = 0; i < 3; i++) {
            if (fieldTaken2(i)) {
                if (fieldLobbied2(i)) {
                    msg += "#b#L" + i + "#Carnival Field " + (i + 1) + " (Level: "  // "Carnival field" GMS-like improvement thanks to Jayd
                            + cpqCalcAvgLvl(980031000 + i * 1000) + " / "
                            + getPlayerCount(980031000 + i * 1000) + "x"
                            + getPlayerCount(980031000 + i * 1000) + ")  #l\r\n";
                }
            } else {
                if (i == 0 || i == 1) {
                    msg += "#b#L" + i + "#Carnival Field " + (i + 1) + " (2x2) #l\r\n";
                } else {
                    msg += "#b#L" + i + "#Carnival Field " + (i + 1) + " (3x3) #l\r\n";
                }
            }
        }

        if (msg.length() > msgLen) {
            sendSimple(msg);
            return true;
        } else {
            return false;
        }
    }

    public boolean fieldTaken2(int field) {
        if (!c.getChannelServer().canInitMonsterCarnival(false, field)) {
            return true;
        }
        if (!c.getChannelServer().getMapFactory().getMap(980031000 + field * 1000).getAllPlayer().isEmpty()) {
            return true;
        }
        if (!c.getChannelServer().getMapFactory().getMap(980031100 + field * 1000).getAllPlayer().isEmpty()) {
            return true;
        }
        return !c.getChannelServer().getMapFactory().getMap(980031200 + field * 1000).getAllPlayer().isEmpty();
    }

    public boolean fieldLobbied2(int field) {
        return !c.getChannelServer().getMapFactory().getMap(980031000 + field * 1000).getAllPlayer().isEmpty();
    }

    public void cpqLobby2(int field) {
        try {
            final MapleMap map, mapExit;
            Channel cs = c.getChannelServer();

            mapExit = cs.getMapFactory().getMap(980030000);
            map = cs.getMapFactory().getMap(980031000 + 1000 * field);
            for (PartyCharacter mpc : c.getPlayer().getParty().getMembers()) {
                final Character mc = mpc.getPlayer();
                if (mc != null) {
                    mc.setChallenged(false);
                    mc.changeMap(map, map.getPortal(0));
                    mc.sendPacket(PacketCreator.serverNotice(6, LanguageConstants.getMessage(mc, LanguageConstants.CPQEntryLobby)));
                    TimerManager tMan = TimerManager.getInstance();
                    tMan.schedule(() -> mapClock((int) MINUTES.toSeconds(3)), 1500);

                    mc.setCpqTimer(TimerManager.getInstance().schedule(() -> mc.changeMap(mapExit, mapExit.getPortal(0)), MINUTES.toMillis(3)));
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void mapClock(int time) {
        getPlayer().getMap().broadcastMessage(PacketCreator.getClock(time));
    }

    private boolean sendCPQChallenge(String cpqType, int leaderid) {
        Set<Integer> cpqLeaders = new HashSet<>();
        cpqLeaders.add(leaderid);
        cpqLeaders.add(getPlayer().getId());

        return c.getWorldServer().getMatchCheckerCoordinator().createMatchConfirmation(MatchCheckerType.CPQ_CHALLENGE, c.getWorld(), getPlayer().getId(), cpqLeaders, cpqType);
    }

    public void answerCPQChallenge(boolean accept) {
        c.getWorldServer().getMatchCheckerCoordinator().answerMatchConfirmation(getPlayer().getId(), accept);
    }

    public void challengeParty2(int field) {
        Character leader = null;
        MapleMap map = c.getChannelServer().getMapFactory().getMap(980031000 + 1000 * field);
        for (MapObject mmo : map.getAllPlayer()) {
            Character mc = (Character) mmo;
            if (mc.getParty() == null) {
                sendOk(LanguageConstants.getMessage(mc, LanguageConstants.CPQFindError));
                return;
            }
            if (mc.getParty().getLeader().getId() == mc.getId()) {
                leader = mc;
                break;
            }
        }
        if (leader != null) {
            if (!leader.isChallenged()) {
                if (!sendCPQChallenge("cpq2", leader.getId())) {
                    sendOk(LanguageConstants.getMessage(leader, LanguageConstants.CPQChallengeRoomAnswer));
                }
            } else {
                sendOk(LanguageConstants.getMessage(leader, LanguageConstants.CPQChallengeRoomAnswer));
            }
        } else {
            sendOk(LanguageConstants.getMessage(leader, LanguageConstants.CPQLeaderNotFound));
        }
    }

    public void challengeParty(int field) {
        Character leader = null;
        MapleMap map = c.getChannelServer().getMapFactory().getMap(980000100 + 100 * field);
        if (map.getAllPlayer().size() != getPlayer().getParty().getMembers().size()) {
            sendOk("An unexpected error regarding the other party has occurred.");
            return;
        }
        for (MapObject mmo : map.getAllPlayer()) {
            Character mc = (Character) mmo;
            if (mc.getParty() == null) {
                sendOk(LanguageConstants.getMessage(mc, LanguageConstants.CPQFindError));
                return;
            }
            if (mc.getParty().getLeader().getId() == mc.getId()) {
                leader = mc;
                break;
            }
        }
        if (leader != null) {
            if (!leader.isChallenged()) {
                if (!sendCPQChallenge("cpq1", leader.getId())) {
                    sendOk(LanguageConstants.getMessage(leader, LanguageConstants.CPQChallengeRoomAnswer));
                }
            } else {
                sendOk(LanguageConstants.getMessage(leader, LanguageConstants.CPQChallengeRoomAnswer));
            }
        } else {
            sendOk(LanguageConstants.getMessage(leader, LanguageConstants.CPQLeaderNotFound));
        }
    }

    private synchronized boolean setupAriantBattle(Expedition exped, int mapid) {
        MapleMap arenaMap = this.getMap().getChannelServer().getMapFactory().getMap(mapid + 1);
        if (!arenaMap.getAllPlayers().isEmpty()) {
            return false;
        }

        new AriantColiseum(arenaMap, exped);
        return true;
    }

    public String startAriantBattle(ExpeditionType expedType, int mapid) {
        if (!GameConstants.isAriantColiseumLobby(mapid)) {
            return "You cannot start an Ariant tournament from outside the Battle Arena Entrance.";
        }

        Expedition exped = this.getMap().getChannelServer().getExpedition(expedType);
        if (exped == null) {
            return "Please register on an expedition before attempting to start an Ariant tournament.";
        }

        List<Character> players = exped.getActiveMembers();

        int playersSize = players.size();
        if (!(playersSize >= exped.getMinSize() && playersSize <= exped.getMaxSize())) {
            return "Make sure there are between #r" + exped.getMinSize() + " ~ " + exped.getMaxSize() + " players#k in this room to start the battle.";
        }

        MapleMap leaderMap = this.getMap();
        for (Character mc : players) {
            if (mc.getMap() != leaderMap) {
                return "All competing players should be on this area to start the battle.";
            }

            if (mc.getParty() != null) {
                return "All competing players must not be on a party to start the battle.";
            }

            int level = mc.getLevel();
            if (!(level >= expedType.getMinLevel() && level <= expedType.getMaxLevel())) {
                return "There are competing players outside of the acceptable level range in this room. All players must be on #blevel between 20~30#k to start the battle.";
            }
        }

        if (setupAriantBattle(exped, mapid)) {
            return "";
        } else {
            return "Other players are already competing on the Ariant tournament in this room. Please wait a while until the arena becomes available again.";
        }
    }

    public void sendMarriageWishlist(boolean groom) {
        Character player = this.getPlayer();
        Marriage marriage = player.getMarriageInstance();
        if (marriage != null) {
            int cid = marriage.getIntProperty(groom ? "groomId" : "brideId");
            Character chr = marriage.getPlayerById(cid);
            if (chr != null) {
                if (chr.getId() == player.getId()) {
                    player.sendPacket(WeddingPackets.onWeddingGiftResult((byte) 0xA, marriage.getWishlistItems(groom), marriage.getGiftItems(player.getClient(), groom)));
                } else {
                    marriage.setIntProperty("wishlistSelection", groom ? 0 : 1);
                    player.sendPacket(WeddingPackets.onWeddingGiftResult((byte) 0x09, marriage.getWishlistItems(groom), marriage.getGiftItems(player.getClient(), groom)));
                }
            }
        }
    }

    public void sendMarriageGifts(List<Item> gifts) {
        this.getPlayer().sendPacket(WeddingPackets.onWeddingGiftResult((byte) 0xA, Collections.singletonList(""), gifts));
    }

    public boolean createMarriageWishlist() {
        Marriage marriage = this.getPlayer().getMarriageInstance();
        if (marriage != null) {
            Boolean groom = marriage.isMarriageGroom(this.getPlayer());
            if (groom != null) {
                String wlKey;
                if (groom) {
                    wlKey = "groomWishlist";
                } else {
                    wlKey = "brideWishlist";
                }

                if (marriage.getProperty(wlKey).contentEquals("")) {
                    getClient().sendPacket(WeddingPackets.sendWishList());
                    return true;
                }
            }
        }

        return false;
    }
}
