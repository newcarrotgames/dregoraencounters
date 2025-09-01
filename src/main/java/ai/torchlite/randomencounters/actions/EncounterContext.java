package ai.torchlite.randomencounters.actions;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.*;

public class EncounterContext {
    
    private final World world;
    private final BlockPos originPos;
    private final Map<String, EntityLiving> entities = new HashMap<>();
    private final List<EntityPlayer> involvedPlayers = new ArrayList<>();
    private int delayTicks = 0;
    
    public EncounterContext(World world, BlockPos originPos) {
        this.world = world;
        this.originPos = originPos;
    }
    
    public void addEntity(String label, EntityLiving entity) {
        entities.put(label, entity);
    }
    
    public EntityLiving getEntity(String label) {
        return entities.get(label);
    }
    
    public Collection<EntityLiving> getAllEntities() {
        return entities.values();
    }
    
    public void addPlayer(EntityPlayer player) {
        if (!involvedPlayers.contains(player)) {
            involvedPlayers.add(player);
        }
    }
    
    public List<EntityPlayer> getAllPlayers() {
        return new ArrayList<>(involvedPlayers);
    }
    
    public List<EntityPlayer> getNearbyPlayers(int radius) {
        List<EntityPlayer> nearby = new ArrayList<>();
        double radiusSq = radius * radius;
        
        for (EntityPlayer player : world.playerEntities) {
            if (player.getDistanceSqToCenter(originPos) <= radiusSq) {
                nearby.add(player);
            }
        }
        
        return nearby;
    }
    
    public World getWorld() {
        return world;
    }
    
    public BlockPos getOriginPos() {
        return originPos;
    }
    
    public void addDelay(int seconds) {
        delayTicks += seconds * 20;
    }
    
    public int getDelayTicks() {
        return delayTicks;
    }
    
    public void reduceDelay(int ticks) {
        delayTicks = Math.max(0, delayTicks - ticks);
    }
    
    public boolean hasDelay() {
        return delayTicks > 0;
    }
    
    public void cleanup() {
        // Clean up any remaining entities
        for (EntityLiving entity : entities.values()) {
            if (!entity.isDead) {
                entity.setDead();
            }
        }
        entities.clear();
        involvedPlayers.clear();
    }
}
