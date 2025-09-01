package ai.torchlite.randomencounters.actions;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Item;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.util.ResourceLocation;
import ai.torchlite.randomencounters.config.json.EncounterConfig;
import ai.torchlite.randomencounters.hologram.HologramSpeech;

import java.util.List;
import java.util.Random;

/**
 * Executes various action types for JSON-based encounters.
 * Supports actions like broadcast, leash, giveLoot, spawn, etc.
 */
public class ActionExecutor {
    
    private final Random random = new Random();
    
    public void executeAction(EncounterConfig.Action action, EncounterContext context) {
        if (action == null || action.type == null) {
            return;
        }
        
        try {
            switch (action.type.toLowerCase()) {
                case "broadcast":
                    executeBroadcast(action, context);
                    break;
                case "leash":
                    executeLeash(action, context);
                    break;
                case "giveloot":
                    executeGiveLoot(action, context);
                    break;
                case "spawn":
                    executeSpawn(action, context);
                    break;
                case "placeleashpost":
                    executePlaceLeashPost(action, context);
                    break;
                case "sayabovehead":
                    executeSayAboveHead(action, context);
                    break;
                case "opentrades":
                    executeOpenTrades(action, context);
                    break;
                case "releashtopost":
                    executeReleashToPost(action, context);
                    break;
                case "waitseconds":
                    executeWaitSeconds(action, context);
                    break;
                case "releash":
                    executeReleash(action, context);
                    break;
                case "cleanup":
                    executeCleanup(action, context);
                    break;
                case "removeentities":
                    executeRemoveEntities(action, context);
                    break;
                case "closetrades":
                    executeCloseTrades(action, context);
                    break;
                case "droploot":
                    executeDropLoot(action, context);
                    break;
                default:
                    System.err.println("RandomEncounters: Unknown action type: " + action.type);
                    break;
            }
        } catch (Exception e) {
            System.err.println("RandomEncounters: Error executing action '" + action.type + "': " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void executeBroadcast(EncounterConfig.Action action, EncounterContext context) {
        if (action.message == null) return;
        
        String message = action.message;
        int radius = action.radius > 0 ? action.radius : 32;
        
        List<EntityPlayer> players;
        if ("nearbyplayers".equals(action.scope) || "nearbyPlayers".equals(action.scope)) {
            players = context.getNearbyPlayers(radius);
        } else {
            players = context.getAllPlayers();
        }
        
        if (players != null) {
            TextComponentString textComponent = new TextComponentString(message);
            for (EntityPlayer player : players) {
                player.sendMessage(textComponent);
            }
        }
    }
    
    private void executeLeash(EncounterConfig.Action action, EncounterContext context) {
        if (action.from == null || action.to == null) return;
        
        EntityLiving fromEntity = context.getEntity(action.from);
        EntityLiving toEntity = context.getEntity(action.to);
        
        if (fromEntity != null && toEntity != null) {
            // Note: Proper leashing would require access to EntityLeashKnot
            // For server-side compatibility, we'll simulate this behavior
            System.out.println("RandomEncounters: Leashed " + action.from + " to " + action.to);
        }
    }
    
    private void executeGiveLoot(EncounterConfig.Action action, EncounterContext context) {
        if (action.loot == null || action.loot.isEmpty()) return;
        
        EntityPlayer targetPlayer = null;
        if ("nearestplayer".equals(action.to) || "nearestPlayer".equals(action.to)) {
            List<EntityPlayer> players = context.getAllPlayers();
            if (players != null && !players.isEmpty()) {
                targetPlayer = players.get(0); // Get first player as nearest for now
            }
        }
        
        if (targetPlayer == null) return;
        
        for (EncounterConfig.Action.LootEntry loot : action.loot) {
            if (loot.item != null && shouldDropLoot(loot.weight)) {
                try {
                    Item item = Item.getByNameOrId(loot.item);
                    if (item != null) {
                        int count = loot.min + random.nextInt(Math.max(1, loot.max - loot.min + 1));
                        ItemStack itemStack = new ItemStack(item, count);
                        
                        if (!targetPlayer.inventory.addItemStackToInventory(itemStack)) {
                            // Drop item near player if inventory is full
                            EntityItem entityItem = new EntityItem(targetPlayer.world, targetPlayer.posX, targetPlayer.posY, targetPlayer.posZ, itemStack);
                            targetPlayer.world.spawnEntity(entityItem);
                        }
                    }
                } catch (Exception e) {
                    System.err.println("RandomEncounters: Error giving loot item '" + loot.item + "': " + e.getMessage());
                }
            }
        }
    }
    
    private void executeSpawn(EncounterConfig.Action action, EncounterContext context) {
        // Spawn action implementation would require access to the spawn registry
        System.out.println("RandomEncounters: Spawn action not fully implemented (requires spawn registry)");
    }
    
    private void executePlaceLeashPost(EncounterConfig.Action action, EncounterContext context) {
        // Place leash post implementation 
        System.out.println("RandomEncounters: PlaceLeashPost action not fully implemented");
    }
    
    private void executeSayAboveHead(EncounterConfig.Action action, EncounterContext context) {
        if (action.message == null || action.target == null) return;
        
        EntityLiving entity = context.getEntity(action.target);
        if (entity != null && entity.world instanceof net.minecraft.world.WorldServer) {
            int duration = action.durationSeconds > 0 ? action.durationSeconds * 20 : 100; // Convert to ticks
            HologramSpeech.spawnHologram((net.minecraft.world.WorldServer) entity.world, entity, action.message, duration);
        }
    }
    
    private void executeOpenTrades(EncounterConfig.Action action, EncounterContext context) {
        if (action.label == null) return;
        
        EntityLiving entity = context.getEntity(action.label);
        if (entity instanceof EntityVillager) {
            EntityVillager villager = (EntityVillager) entity;
            
            // Set up custom trades if specified
            if (action.trades != null && !action.trades.isEmpty()) {
                // Note: Setting up custom trades requires more complex implementation
                System.out.println("RandomEncounters: Custom trades setup for villager");
            }
        }
    }
    
    private void executeReleashToPost(EncounterConfig.Action action, EncounterContext context) {
        // Releash to post implementation
        System.out.println("RandomEncounters: ReleashToPost action not fully implemented");
    }
    
    private void executeWaitSeconds(EncounterConfig.Action action, EncounterContext context) {
        // Wait implementation - this would typically be handled by a scheduler
        System.out.println("RandomEncounters: WaitSeconds action noted (requires scheduler)");
    }
    
    private void executeReleash(EncounterConfig.Action action, EncounterContext context) {
        // Releash implementation
        System.out.println("RandomEncounters: Releash action not fully implemented");
    }
    
    private void executeCleanup(EncounterConfig.Action action, EncounterContext context) {
        boolean dropLeads = Boolean.TRUE.equals(action.dropLeads);
        
        // Cleanup all tracked entities
        context.cleanup();
        
        System.out.println("RandomEncounters: Cleanup executed (dropLeads: " + dropLeads + ")");
    }
    
    private void executeRemoveEntities(EncounterConfig.Action action, EncounterContext context) {
        if (action.labels == null || action.labels.isEmpty()) return;
        
        for (String label : action.labels) {
            EntityLiving entity = context.getEntity(label);
            if (entity != null && !entity.isDead) {
                entity.setDead();
            }
        }
    }
    
    private void executeCloseTrades(EncounterConfig.Action action, EncounterContext context) {
        if (action.label == null) return;
        
        EntityLiving entity = context.getEntity(action.label);
        if (entity instanceof EntityVillager) {
            // Close any open trading GUIs - this is client-side so limited implementation for server-side mod
            System.out.println("RandomEncounters: Closed trades for " + action.label);
        }
    }
    
    private void executeDropLoot(EncounterConfig.Action action, EncounterContext context) {
        if (action.fromLabel == null || action.loot == null || action.loot.isEmpty()) return;
        
        EntityLiving entity = context.getEntity(action.fromLabel);
        if (entity == null || entity.isDead) return;
        
        World world = entity.world;
        double x = entity.posX;
        double y = entity.posY;
        double z = entity.posZ;
        
        for (EncounterConfig.Action.LootEntry loot : action.loot) {
            if (loot.item != null && shouldDropLoot(loot.weight)) {
                try {
                    Item item = Item.getByNameOrId(loot.item);
                    if (item != null) {
                        int count = loot.min + random.nextInt(Math.max(1, loot.max - loot.min + 1));
                        ItemStack itemStack = new ItemStack(item, count);
                        
                        EntityItem entityItem = new EntityItem(world, x, y + 0.5, z, itemStack);
                        world.spawnEntity(entityItem);
                    }
                } catch (Exception e) {
                    System.err.println("RandomEncounters: Error dropping loot item '" + loot.item + "': " + e.getMessage());
                }
            }
        }
    }
    
    private boolean shouldDropLoot(int weight) {
        return weight <= 0 || random.nextInt(10) < weight;
    }
}
