package ai.torchlite.randomencounters.events;

import ai.torchlite.randomencounters.RandomEncounters;
import ai.torchlite.randomencounters.config.ConfigHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import java.util.Random;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EncounterEventHandler {
    
    private final Random random = new Random();
    private final Map<UUID, Integer> playerTickCounters = new HashMap<>();
    private int cleanupCounter = 0;
    
    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.world.isRemote) {
            return; // Only process on server side
        }
        
        EntityPlayer player = event.player;
        UUID playerId = player.getUniqueID();
        
        // Check encounters roughly every minute (1200 ticks = 60 seconds)
        int tickCount = playerTickCounters.getOrDefault(playerId, 0);
        tickCount++;
        
        if (tickCount >= 1200) { // 1 minute
            tickCount = 0;
            
            // Check if encounters are globally enabled and should trigger
            if (ConfigHandler.enableRandomEncounters && RandomEncounters.encounterManager.shouldTriggerEncounter(player)) {
                RandomEncounters.encounterManager.triggerEncounter(player);
            }
        }
        
        playerTickCounters.put(playerId, tickCount);
        
        // Tick encounter manager systems every tick
        RandomEncounters.encounterManager.tick();
        
        // Clean up old data periodically (every 5 minutes)
        cleanupCounter++;
        if (cleanupCounter >= 6000) { // 5 minutes
            cleanupCounter = 0;
            RandomEncounters.encounterManager.cleanupOldData();
            
            // Clean up disconnected players from tick counters
            playerTickCounters.entrySet().removeIf(entry -> {
                UUID uuid = entry.getKey();
                return player.world.getMinecraftServer().getPlayerList().getPlayerByUUID(uuid) == null;
            });
        }
    }
}
