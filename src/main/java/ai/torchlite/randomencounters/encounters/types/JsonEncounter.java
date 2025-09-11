package ai.torchlite.randomencounters.encounters.types;

import ai.torchlite.randomencounters.encounters.IEncounter;
import ai.torchlite.randomencounters.config.json.EncounterConfig;
import ai.torchlite.randomencounters.config.json.JsonEncounterLoader;
import ai.torchlite.randomencounters.actions.ActionExecutor;
import ai.torchlite.randomencounters.actions.EncounterContext;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.passive.EntityWolf;
import net.minecraft.entity.passive.EntityChicken;
import net.minecraft.entity.passive.EntityCow;
import net.minecraft.entity.passive.EntityPig;
import net.minecraft.entity.passive.EntitySheep;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import java.util.Random;

/**
 * JSON-based encounter implementation that loads encounter definitions from JSON files.
 * This is the primary encounter type used by the mod.
 */
public class JsonEncounter implements IEncounter {
    
    private final EncounterConfig.Encounter encounterDef;
    private final JsonEncounterLoader loader;
    private final ActionExecutor actionExecutor;
    private final Random random = new Random();
    
    public JsonEncounter(EncounterConfig.Encounter encounterDef, JsonEncounterLoader loader) {
        this.encounterDef = encounterDef;
        this.loader = loader;
        this.actionExecutor = new ActionExecutor();
    }
    
    public String getEncounterId() {
        return encounterDef.id;
    }
    
    @Override
    public void execute(EntityPlayer player, double difficulty) {
        if (!encounterDef.enabled) {
            return;
        }
        
        World world = player.world;
        if (!(world instanceof WorldServer)) {
            return;
        }
        
        BlockPos playerPos = player.getPosition();
        EncounterContext context = new EncounterContext(world, playerPos);
        context.addPlayer(player);
        
        try {
            // Execute onStart actions
            if (encounterDef.onStart != null) {
                for (EncounterConfig.Encounter.ActionRef actionRef : encounterDef.onStart) {
                    executeAction(actionRef, context, player);
                }
            }
            
            // Handle spawns
            if (encounterDef.spawn != null) {
                for (EncounterConfig.Encounter.SpawnEntry spawnEntry : encounterDef.spawn) {
                    executeSpawn(spawnEntry, world, playerPos, context);
                }
            }
            
            // Note: behaviors, triggers, onTimeout, and onCleanup would be handled by
            // the full encounter system, but for now we're just doing basic spawning
            
        } catch (Exception e) {
            System.err.println("RandomEncounters: Error executing encounter " + encounterDef.id + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void executeAction(EncounterConfig.Encounter.ActionRef actionRef, EncounterContext context, EntityPlayer player) {
        // Simple action execution for broadcast messages
        if ("broadcast".equals(actionRef.type)) {
            if (actionRef.message != null) {
                // For now, just use the message directly (expression evaluation would be added later)
                String message = actionRef.message;
                player.sendMessage(new TextComponentString(message));
            }
        }
        // Additional action types would be implemented here
    }
    
    private void executeSpawn(EncounterConfig.Encounter.SpawnEntry spawnEntry, World world, BlockPos playerPos, EncounterContext context) {
        // Get spawn configuration from blocks
        EncounterConfig config = loader.getConfig();
        if (config == null || config.blocks == null || config.blocks.spawns == null) {
            return;
        }
        
        EncounterConfig.Spawn spawnDef = config.blocks.spawns.get(spawnEntry.ref);
        if (spawnDef == null) {
            System.err.println("RandomEncounters: Spawn reference not found: " + spawnEntry.ref);
            return;
        }
        
        // Calculate spawn position
        BlockPos spawnPos = calculateSpawnPosition(playerPos, spawnEntry.at);
        
        // Create and spawn entities
        int count = spawnDef.count > 0 ? spawnDef.count : 1;
        for (int i = 0; i < count; i++) {
            EntityLiving entity = createEntity(spawnDef.entityId, world);
            if (entity == null) {
                continue;
            }
            
            // Position the entity
            entity.setPosition(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5);
            
            // Apply persistence if needed
            if (spawnDef.persistence) {
                entity.enablePersistence();
            }
            
            // Apply AI toggles
            if (spawnDef.aiToggles != null) {
                applyAiToggles(entity, spawnDef.aiToggles);
            }
            
            // Spawn the entity
            world.spawnEntity(entity);
            // Add to context with the spawn label if available
            if (spawnEntry.label != null) {
                context.addEntity(spawnEntry.label, entity);
            }
        }
    }
    
    private EntityLiving createEntity(String entityId, World world) {
        if (entityId == null) {
            return null;
        }
        
        // Handle random types
        if ("random_farm_animal".equals(entityId)) {
            switch (random.nextInt(5)) {
                case 0: return new EntityChicken(world);
                case 1: return new EntityCow(world);
                case 2: return new EntityPig(world);
                case 3: return new EntitySheep(world);
                default: return new EntityChicken(world);
            }
        }
        
        // Handle specific entity types
        switch (entityId) {
            case "minecraft:villager":
                return new EntityVillager(world);
            case "minecraft:wolf":
                return new EntityWolf(world);
            case "minecraft:skeleton":
                return new EntitySkeleton(world);
            case "minecraft:zombie":
                return new EntityZombie(world);
            case "minecraft:chicken":
                return new EntityChicken(world);
            case "minecraft:cow":
                return new EntityCow(world);
            case "minecraft:pig":
                return new EntityPig(world);
            case "minecraft:sheep":
                return new EntitySheep(world);
            default:
                System.err.println("RandomEncounters: Unknown entity ID: " + entityId);
                return null;
        }
    }
    
    private void applyAiToggles(EntityLiving entity, EncounterConfig.Spawn.AiToggles aiToggles) {
        if (aiToggles.clearTargetTasks != null && aiToggles.clearTargetTasks) {
            entity.targetTasks.taskEntries.clear();
        }
        
        if (aiToggles.removeDefaultWander != null && aiToggles.removeDefaultWander) {
            entity.tasks.taskEntries.removeIf(entry -> 
                entry.action.getClass().getSimpleName().contains("Wander"));
        }
        
        if (aiToggles.canPickUpLoot != null) {
            entity.setCanPickUpLoot(aiToggles.canPickUpLoot);
        }
        
        if (aiToggles.followRange != null) {
            // Would need to apply follow range attribute here
        }
    }
    
    private BlockPos calculateSpawnPosition(BlockPos playerPos, EncounterConfig.Encounter.SpawnLocation location) {
        if (location == null) {
            // Default: spawn near player
            return playerPos.add(
                random.nextInt(10) - 5,
                0,
                random.nextInt(10) - 5
            );
        }
        
        if ("nearPlayer".equals(location.mode)) {
            int minRadius = location.radius != null ? location.radius.min : 5;
            int maxRadius = location.radius != null ? location.radius.max : 10;
            int radius = minRadius + random.nextInt(maxRadius - minRadius + 1);
            
            double angle = random.nextDouble() * 2 * Math.PI;
            int x = (int)(Math.cos(angle) * radius);
            int z = (int)(Math.sin(angle) * radius);
            
            return playerPos.add(x, 0, z);
        } else if ("absolute".equals(location.mode)) {
            if (location.x != null && location.y != null && location.z != null) {
                return new BlockPos(location.x, location.y, location.z);
            }
        }
        
        // Default fallback
        return playerPos.add(
            random.nextInt(10) - 5,
            0,
            random.nextInt(10) - 5
        );
    }
    
    @Override
    public String getName() {
        return encounterDef.id != null ? encounterDef.id : "JSON Encounter";
    }
    
    @Override
    public boolean canExecute(EntityPlayer player) {
        return encounterDef.enabled;
    }
}