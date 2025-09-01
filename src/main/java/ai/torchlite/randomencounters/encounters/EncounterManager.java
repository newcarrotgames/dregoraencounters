package ai.torchlite.randomencounters.encounters;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import ai.torchlite.randomencounters.config.ConfigHandler;
import ai.torchlite.randomencounters.config.json.JsonEncounterLoader;
import ai.torchlite.randomencounters.config.json.EncounterConfig;
import ai.torchlite.randomencounters.encounters.types.*;
import ai.torchlite.randomencounters.hologram.HologramSpeech;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class EncounterManager {
    
    private final Map<UUID, Long> playerCooldowns = new ConcurrentHashMap<>();
    private final Map<UUID, Integer> dailyEncounterCounts = new ConcurrentHashMap<>();
    private final Map<UUID, Long> lastDayCheck = new ConcurrentHashMap<>();
    private final Random random = new Random();
    
    private final List<IEncounter> availableEncounters = new ArrayList<>();
    private JsonEncounterLoader jsonLoader;
    
    public EncounterManager() {
        initializeEncounters();
    }
    
    private void initializeEncounters() {
        // Get JSON loader from main class
        jsonLoader = ai.torchlite.randomencounters.RandomEncounters.jsonLoader;
        
        // Load JSON encounters if possible
        if (loadJsonEncounters()) {
            System.out.println("RandomEncounters: Using JSON-based encounters");
            return;
        }
        
        // Fallback to hardcoded encounters
        System.out.println("RandomEncounters: Falling back to hardcoded encounters");
        if (ConfigHandler.enableMobEncounters) {
            availableEncounters.add(new MobEncounter());
        }
        if (ConfigHandler.enableLootEncounters) {
            availableEncounters.add(new LootEncounter());
        }
        if (ConfigHandler.enableEventEncounters) {
            availableEncounters.add(new EventEncounter());
        }
        // NPC encounters disabled by default for server compatibility
        if (ConfigHandler.enableNPCEncounters) {
            availableEncounters.add(new NPCEncounter());
        }
        if (ConfigHandler.enableFriendlySkeletonEncounters) {
            availableEncounters.add(new ai.torchlite.randomencounters.encounters.types.FriendlySkeletonEncounter());
        }
    }
    
    private boolean loadJsonEncounters() {
        try {
            if (jsonLoader == null || jsonLoader.getConfig() == null) {
                return false;
            }
            
            EncounterConfig config = jsonLoader.getConfig();
            if (config.encounters == null || config.encounters.isEmpty()) {
                return false;
            }
            
            // Load JSON encounters
            for (EncounterConfig.Encounter encounterDef : config.encounters) {
                if (encounterDef.enabled) {
                    JsonEncounter jsonEncounter = new JsonEncounter(encounterDef, jsonLoader);
                    availableEncounters.add(jsonEncounter);
                }
            }
            
            return !availableEncounters.isEmpty();
            
        } catch (Exception e) {
            System.err.println("RandomEncounters: Failed to load JSON encounters: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    public boolean shouldTriggerEncounter(EntityPlayer player) {
        UUID playerId = player.getUniqueID();
        long currentTime = System.currentTimeMillis();
        
        // Check if encounters are globally enabled
        if (!ConfigHandler.enableRandomEncounters) {
            return false;
        }
        
        // Check cooldown
        if (isOnCooldown(playerId, currentTime)) {
            return false;
        }
        
        // Check daily limit
        if (hasReachedDailyLimit(playerId, currentTime)) {
            return false;
        }
        
        // Check distance from spawn
        if (!isValidDistance(player)) {
            return false;
        }
        
        // Check if player is above ground (not underground)
        if (!isAboveGround(player)) {
            return false;
        }
        
        // Check random chance
        return random.nextInt(100) < ConfigHandler.baseEncounterChance;
    }
    
    private boolean isOnCooldown(UUID playerId, long currentTime) {
        Long lastEncounter = playerCooldowns.get(playerId);
        if (lastEncounter == null) {
            return false;
        }
        return (currentTime - lastEncounter) < (ConfigHandler.encounterCooldown * 1000L);
    }
    
    private boolean hasReachedDailyLimit(UUID playerId, long currentTime) {
        // Check if it's a new day
        Long lastDay = lastDayCheck.get(playerId);
        long currentDay = currentTime / (24 * 60 * 60 * 1000L); // Days since epoch
        
        if (lastDay == null || lastDay < currentDay) {
            // Reset daily count for new day
            dailyEncounterCounts.put(playerId, 0);
            lastDayCheck.put(playerId, currentDay);
            return false;
        }
        
        Integer count = dailyEncounterCounts.get(playerId);
        return count != null && count >= ConfigHandler.maxEncountersPerDay;
    }
    
    private boolean isValidDistance(EntityPlayer player) {
        BlockPos playerPos = player.getPosition();
        BlockPos spawnPos = player.world.getSpawnPoint();
        
        double distance = playerPos.getDistance(spawnPos.getX(), spawnPos.getY(), spawnPos.getZ());
        return distance >= ConfigHandler.minDistanceFromSpawn;
    }
    
    private boolean isAboveGround(EntityPlayer player) {
        World world = player.world;
        BlockPos playerPos = player.getPosition();
        
        // Check if player can see the sky (not underground/in caves)
        if (world.canSeeSky(playerPos)) {
            return true;
        }
        
        // Alternative check: if player is near surface level
        // Get the top solid block at player's X,Z position
        BlockPos surfacePos = world.getTopSolidOrLiquidBlock(playerPos);
        int surfaceY = surfacePos.getY();
        int playerY = playerPos.getY();
        
        // Consider "above ground" if within 10 blocks of surface
        return (surfaceY - playerY) <= 10;
    }
    
    public void triggerEncounter(EntityPlayer player) {
        if (availableEncounters.isEmpty()) {
            return;
        }
        
        UUID playerId = player.getUniqueID();
        long currentTime = System.currentTimeMillis();
        
        // Update cooldown and daily count
        playerCooldowns.put(playerId, currentTime);
        dailyEncounterCounts.put(playerId, 
            dailyEncounterCounts.getOrDefault(playerId, 0) + 1);
        
        // Select random encounter
        IEncounter encounter = availableEncounters.get(random.nextInt(availableEncounters.size()));
        
        // Calculate difficulty
        double difficulty = calculateDifficulty(player);
        
        // Execute encounter
        encounter.execute(player, difficulty);
    }
    
    public boolean triggerSpecificEncounter(EntityPlayer player, String encounterType) {
        UUID playerId = player.getUniqueID();
        long currentTime = System.currentTimeMillis();
        
        // Update cooldown and daily count for testing
        playerCooldowns.put(playerId, currentTime);
        dailyEncounterCounts.put(playerId, 
            dailyEncounterCounts.getOrDefault(playerId, 0) + 1);
        
        IEncounter encounter = null;
        
        switch (encounterType.toLowerCase()) {
            case "mob":
                if (ConfigHandler.enableMobEncounters) {
                    encounter = new ai.torchlite.randomencounters.encounters.types.MobEncounter();
                }
                break;
            case "loot":
                if (ConfigHandler.enableLootEncounters) {
                    encounter = new ai.torchlite.randomencounters.encounters.types.LootEncounter();
                }
                break;
            case "event":
                if (ConfigHandler.enableEventEncounters) {
                    encounter = new ai.torchlite.randomencounters.encounters.types.EventEncounter();
                }
                break;
            case "npc":
                if (ConfigHandler.enableNPCEncounters) {
                    encounter = new ai.torchlite.randomencounters.encounters.types.NPCEncounter();
                }
                break;
            case "friendlyskeleton":
            case "friendly":
                if (ConfigHandler.enableFriendlySkeletonEncounters) {
                    encounter = new ai.torchlite.randomencounters.encounters.types.FriendlySkeletonEncounter();
                }
                break;
            case "json":
                // Try to get a random JSON encounter
                if (!availableEncounters.isEmpty()) {
                    encounter = availableEncounters.get(random.nextInt(availableEncounters.size()));
                }
                break;
        }
        
        if (encounter != null) {
            double difficulty = calculateDifficulty(player);
            encounter.execute(player, difficulty);
            return true;
        }
        
        return false;
    }
    
    private double calculateDifficulty(EntityPlayer player) {
        double difficulty = ConfigHandler.difficultyMultiplier;
        
        if (ConfigHandler.scaleWithPlayerLevel) {
            // Scale with player experience level
            int level = player.experienceLevel;
            difficulty *= (1.0 + (level * 0.1)); // 10% increase per level
        }
        
        if (ConfigHandler.scaleWithDistance) {
            // Scale with distance from spawn
            BlockPos playerPos = player.getPosition();
            BlockPos spawnPos = player.world.getSpawnPoint();
            double distance = playerPos.getDistance(spawnPos.getX(), spawnPos.getY(), spawnPos.getZ());
            
            // Increase difficulty by 0.1% per block beyond minimum distance
            double extraDistance = Math.max(0, distance - ConfigHandler.minDistanceFromSpawn);
            difficulty *= (1.0 + (extraDistance * 0.001));
        }
        
        return Math.max(0.1, difficulty); // Minimum difficulty of 0.1
    }
    
    public void cleanupOldData() {
        long currentTime = System.currentTimeMillis();
        long dayAgo = currentTime - (24 * 60 * 60 * 1000L);
        
        // Remove old cooldowns (older than 1 day)
        playerCooldowns.entrySet().removeIf(entry -> entry.getValue() < dayAgo);
    }
    
    public void tick() {
        // Tick hologram speech system
        HologramSpeech.tickHolograms();
        
        // Other per-tick operations could go here
    }
    
    public int getRemainingCooldown(UUID playerId) {
        Long lastEncounter = playerCooldowns.get(playerId);
        if (lastEncounter == null) {
            return 0;
        }
        
        long elapsed = (System.currentTimeMillis() - lastEncounter) / 1000L;
        return Math.max(0, ConfigHandler.encounterCooldown - (int)elapsed);
    }
    
    public int getDailyEncounterCount(UUID playerId) {
        return dailyEncounterCounts.getOrDefault(playerId, 0);
    }
}
