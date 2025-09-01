package ai.torchlite.randomencounters.hologram;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.world.WorldServer;
import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;

public class HologramSpeech {
    
    private static final Map<Integer, HologramTracker> activeHolograms = new HashMap<>();
    
    public static EntityArmorStand spawnHologram(WorldServer world, EntityLiving npc, String text, int durationTicks) {
        EntityArmorStand armorStand = new EntityArmorStand(world);
        armorStand.setInvisible(true);
        armorStand.setNoGravity(true);
        // Use reflection or NBT to set these properties for 1.12.2 compatibility
        armorStand.getDataManager().set(armorStand.STATUS, (byte)(armorStand.getDataManager().get(armorStand.STATUS) | 1)); // Small
        armorStand.getDataManager().set(armorStand.STATUS, (byte)(armorStand.getDataManager().get(armorStand.STATUS) | 16)); // Marker
        armorStand.setCustomNameTag(text);
        armorStand.setAlwaysRenderNameTag(true); // show at all distances
        
        // Position above NPC's head
        positionHologram(armorStand, npc);
        
        world.spawnEntity(armorStand);
        
        // Track the hologram
        activeHolograms.put(armorStand.getEntityId(), new HologramTracker(armorStand, npc, 0.4D, durationTicks));
        
        return armorStand;
    }
    
    public static void spawnHologram(WorldServer world, EntityLiving npc, String text) {
        spawnHologram(world, npc, text, 200); // 10 seconds default
    }
    
    private static void positionHologram(EntityArmorStand armorStand, EntityLiving npc) {
        double y = npc.posY + npc.height + 0.4D;
        armorStand.setPosition(npc.posX, y, npc.posZ);
        armorStand.rotationYaw = npc.rotationYaw;
    }
    
    public static void tickHolograms() {
        Iterator<Map.Entry<Integer, HologramTracker>> iterator = activeHolograms.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Integer, HologramTracker> entry = iterator.next();
            HologramTracker tracker = entry.getValue();
            
            if (!tracker.tick()) {
                iterator.remove();
            }
        }
    }
    
    public static void removeHologram(EntityArmorStand armorStand) {
        activeHolograms.remove(armorStand.getEntityId());
        if (!armorStand.isDead) {
            armorStand.setDead();
        }
    }
    
    public static void removeAllHolograms() {
        for (HologramTracker tracker : activeHolograms.values()) {
            if (!tracker.armorStand.isDead) {
                tracker.armorStand.setDead();
            }
        }
        activeHolograms.clear();
    }
    
    private static class HologramTracker {
        final EntityArmorStand armorStand;
        final EntityLiving npc;
        final double yOffset;
        int remainingTicks;
        
        HologramTracker(EntityArmorStand armorStand, EntityLiving npc, double yOffset, int durationTicks) {
            this.armorStand = armorStand;
            this.npc = npc;
            this.yOffset = yOffset;
            this.remainingTicks = durationTicks;
        }
        
        boolean tick() {
            // Check if entities are still alive
            if (armorStand.isDead || npc.isDead) {
                if (!armorStand.isDead) {
                    armorStand.setDead();
                }
                return false;
            }
            
            // Check duration
            remainingTicks--;
            if (remainingTicks <= 0) {
                armorStand.setDead();
                return false;
            }
            
            // Update position to follow NPC
            armorStand.setPosition(npc.posX, npc.posY + npc.height + yOffset, npc.posZ);
            armorStand.rotationYaw = npc.rotationYaw;
            
            return true;
        }
    }
}
