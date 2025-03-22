package server.life;

import client.command.commands.gm0.BoostedMap;
import constants.id.MapId;
import net.server.Server;
import net.server.channel.Channel;
import server.TimerManager;
import net.server.world.World;
import server.maps.MapFactory;
import server.maps.MapObject;
import server.maps.MapObjectType;
import server.maps.MapleMap;
import tools.PacketCreator;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static client.command.commands.gm0.WarpRandomMap.validMaps;

/**
 * Manages the hourly spawn rate boost for a random map
 */
public class HourlySpawnBoostManager {
    private static final int DEFAULT_SPAWN_RATE = 2;  // Default spawn rate multiplier (x2)
    private static final int BOOSTED_SPAWN_RATE = 10; // Boosted spawn rate multiplier (x10)
    private static final long BOOST_DURATION = TimeUnit.HOURS.toMillis(1); // 1 hour

    private static HourlySpawnBoostManager instance;
    private ScheduledFuture<?> boostTask;
    private int currentBoostedMapId = -1;
    private final List<Integer> eligibleMapIds;
    private final Random random = new Random();

    private HourlySpawnBoostManager() {
        // List of maps eligible for spawn rate boost
        // You can customize this list with maps you want to include
        eligibleMapIds = new ArrayList<>();
        for (int mapId : validMaps) {
            eligibleMapIds.add(mapId);
        }

        // Filter out any map IDs that might be in BOSS_MAPS
        eligibleMapIds.removeAll(MapId.BOSS_MAPS);
        eligibleMapIds.removeIf(mapId -> (mapId >= 925020000 && mapId <= 925039999));    } // dojo range maps

    public static HourlySpawnBoostManager getInstance() {
        if (instance == null) {
            instance = new HourlySpawnBoostManager();
        }
        return instance;
    }

    /**
     * Start the hourly spawn boost scheduler
     */
    public void startHourlyBoostScheduler() {
        if (boostTask != null && !boostTask.isCancelled()) {
            boostTask.cancel(false);
        }

        // Start immediately
        scheduleNextBoost(0);
    }

    /**
     * Schedule the next boost with the given delay
     */
    private void scheduleNextBoost(long delay) {
        System.out.println("[DEBUG] Scheduling next boost in " + delay + " ms");

        boostTask = TimerManager.getInstance().schedule(() -> {
            System.out.println("[DEBUG] Executing scheduled boost task");

            // If there's a currently boosted map, reset it
            if (currentBoostedMapId != -1) {
                System.out.println("[DEBUG] Resetting previous boosted map: " + currentBoostedMapId);
                resetPreviousBoostedMap();
            }

            // Select a new random map and boost it
            System.out.println("[DEBUG] Boosting a random map");
            boostRandomMap();

            // Schedule the next boost after BOOST_DURATION
            System.out.println("[DEBUG] Scheduling next boost after " + BOOST_DURATION + " ms");
            scheduleNextBoost(BOOST_DURATION);
        }, delay);
    }

    /**
     * Reset the previously boosted map back to default spawn rate
     */
    private void resetPreviousBoostedMap() {
        for (World world : Server.getInstance().getWorlds()) {
            for (Channel channel : world.getChannels()) {
                MapleMap map = channel.getMapFactory().getMap(currentBoostedMapId);
                if (map != null) {
                    resetMapSpawnRate(map);
                }
            }
        }

        currentBoostedMapId = -1;
    }
    /**
     * Finds a random map from the eligible list that contains monsters
     */
    private MapleMap findEligibleMapWithMonsters(Channel channel, List<Integer> eligibleMapIds, Random random) {
        MapleMap map;
        do {
            int mapIndex = random.nextInt(eligibleMapIds.size() - 1);
            int mapId = eligibleMapIds.get(mapIndex);
            map = channel.getMapFactory().getMap(mapId);

            // Check if the map has monsters by using getMapObjectsInRange which gets all monsters within range
            // Using Double.POSITIVE_INFINITY ensures we check the entire map
            List<MapObject> monsters = map.getMapObjectsInRange(
                    new Point(0, 0),
                    Double.POSITIVE_INFINITY,
                    Arrays.asList(MapObjectType.MONSTER)
            );

            if (!monsters.isEmpty() && !map.isTown()) {
                break; // Found a map with monsters
            }

        } while (true); // Keep trying until we find a map with monsters
        return map;
    }
    /**
     * Boost a randomly selected map
     */
    private void boostRandomMap() {
        // Choose a random map from eligible maps
        Channel firstChannel = Server.getInstance().getWorlds().getFirst().getChannels().getFirst();
        MapleMap eligibleMap = findEligibleMapWithMonsters(firstChannel, eligibleMapIds, random);
        currentBoostedMapId = eligibleMap.getId();
        boostMapSpawnRate(eligibleMap);


        // Announce the new boosted map
        Server.getInstance().broadcastMessage(0,
                PacketCreator.serverNotice(6,
                        "[Spawn Boost] The spawn rate for " + getMapName(currentBoostedMapId) +
                                " has been increased to x" + BOOSTED_SPAWN_RATE + " for the next hour!" +
                                "use @boosted for fast travel")
        );
        BoostedMap.setBoostedMap(currentBoostedMapId);
    }

    /**
     * Boost the spawn rate for the given map
     */
    private void boostMapSpawnRate(MapleMap map) {
        // Make a backup of the current spawn points
        map.backupSpawnPoints();

        // Apply the boosted spawn rate
        map.multiplySpawnRate(BOOSTED_SPAWN_RATE / DEFAULT_SPAWN_RATE);

        // Reset the map to apply the new spawn rate
        map.respawn();
    }

    /**
     * Reset the spawn rate for the given map
     */
    private void resetMapSpawnRate(MapleMap map) {
        // Restore the original spawn points
        map.restoreSpawnPoints();

        // Reset the map to apply the new spawn rate
        map.respawn();
    }

    /**
     * Get a readable name for the map
     */
    private String getMapName(int mapId) {
        // This is a simplistic approach - you might want to use the actual map name data
        String mapName = MapFactory.loadPlaceName(mapId);
        if (mapName == null || mapName.isEmpty()) {
            return "Map " + mapId;
        }
        return mapName;
    }

    /**
     * Get the currently boosted map ID
     */
    public int getCurrentBoostedMapId() {
        return currentBoostedMapId;
    }

    /**
     * Check if a map is currently boosted
     */
    public boolean isMapBoosted(int mapId) {
        return mapId == currentBoostedMapId;
    }

    /**
     * Get the current spawn rate for a map
     */
    public int getSpawnRateForMap(int mapId) {
        return isMapBoosted(mapId) ? BOOSTED_SPAWN_RATE : DEFAULT_SPAWN_RATE;
    }
}