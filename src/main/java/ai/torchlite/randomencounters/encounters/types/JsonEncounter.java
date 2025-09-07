package ai.torchlite.randomencounters.encounters.types;

import ai.torchlite.randomencounters.encounters.IEncounter;
import ai.torchlite.randomencounters.config.json.EncounterConfig;
import ai.torchlite.randomencounters.config.json.JsonEncounterLoader;
import ai.torchlite.randomencounters.actions.ActionExecutor;
import ai.torchlite.randomencounters.actions.EncounterContext;
import ai.torchlite.randomencounters.ai.AIMoveToRoute;
import ai.torchlite.randomencounters.hologram.HologramSpeech;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.passive.EntityWolf;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

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
            // Spawn entities
            if (encounterDef.spawn != null) {
                for (EncounterConfig.Encounter.SpawnEntry spawnEntry : encounterDef.spawn) {
                    EntityLiving entity = spawnEntity(spawnEntry, context, difficulty);
                    if (entity != null) {
                        context.addEntity(spawnEntry.label, entity);
                    }
                }
            }
            
            // Execute onStart actions
            if (encounterDef.onStart != null) {
                for (EncounterConfig.Encounter.ActionRef actionRef : encounterDef.onStart) {
                    executeActionRef(actionRef, context);
                }
            }
            
            // Apply behaviors
            if (encounterDef.behaviors != null) {
                for (EncounterConfig.Encounter.BehaviorRef behaviorRef : encounterDef.behaviors) {
                    applyBehavior(behaviorRef, context);
                }
            }
            
        } catch (Exception e) {
            System.err.println("RandomEncounters: Error executing JSON encounter '" + encounterDef.id + "': " + e.getMessage());
            e.printStackTrace();
            context.cleanup();
        }
    }
    
    private EntityLiving spawnEntity(EncounterConfig.Encounter.SpawnEntry spawnEntry, EncounterContext context, double difficulty) {
        EncounterConfig.Spawn spawnDef = loader.getSpawn(spawnEntry.ref);
        if (spawnDef == null) {
            System.err.println("RandomEncounters: Unknown spawn reference: " + spawnEntry.ref);
            return null;
        }
        
        BlockPos spawnPos = calculateSpawnPosition(spawnEntry.at, context);
        if (spawnPos == null) {
            return null;
        }
        
        EntityLiving entity = createEntity(spawnDef, context.getWorld());
        if (entity == null) {
            return null;
        }
        
        // Set position
        entity.setPosition(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5);
        
        // Apply spawn configuration
        if (spawnDef.persistence) {
            entity.enablePersistence();
        }
        
        // Apply AI toggles
        if (spawnDef.aiToggles != null) {
            applyAiToggles(entity, spawnDef.aiToggles);
        }
        
        // Spawn in world
        context.getWorld().spawnEntity(entity);
        
        return entity;
    }
    
    private EntityLiving createEntity(EncounterConfig.Spawn spawnDef, World world) {
        if (!"entity".equals(spawnDef.type)) {
            System.err.println("RandomEncounters: Only 'entity' spawn type is currently supported");
            return null;
        }
        
        switch (spawnDef.entityId) {
            case "minecraft:villager":
                return new EntityVillager(world);
            case "minecraft:wolf":
                return new EntityWolf(world);
            case "minecraft:skeleton":
                return new EntitySkeleton(world);
            case "minecraft:zombie":
                return new EntityZombie(world);
            default:
                System.err.println("RandomEncounters: Unknown entity ID: " + spawnDef.entityId);
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
    }
    
    private BlockPos calculateSpawnPosition(EncounterConfig.Encounter.SpawnLocation spawnLocation, EncounterContext context) {
        if (spawnLocation == null) {
            return context.getOriginPos();
        }
        
        BlockPos basePos = null;
        
        switch (spawnLocation.mode) {
            case "nearPlayer":
                basePos = context.getOriginPos();
                break;
            case "nearLabel":
                if (spawnLocation.label != null) {
                    EntityLiving entity = context.getEntity(spawnLocation.label);
                    if (entity != null) {
                        basePos = entity.getPosition();
                    }
                }
                break;
            case "absolute":
                if (spawnLocation.x != null && spawnLocation.y != null && spawnLocation.z != null) {
                    basePos = new BlockPos(spawnLocation.x, spawnLocation.y, spawnLocation.z);
                }
                break;
        }
        
        if (basePos == null) {
            return context.getOriginPos();
        }
        
        // Apply radius offset
        if (spawnLocation.radius != null) {
            int minRadius = spawnLocation.radius.min;
            int maxRadius = spawnLocation.radius.max;
            int radius = minRadius + random.nextInt(Math.max(1, maxRadius - minRadius + 1));
            
            double angle = random.nextDouble() * 2 * Math.PI;
            int xOffset = (int) (Math.cos(angle) * radius);
            int zOffset = (int) (Math.sin(angle) * radius);
            
            basePos = basePos.add(xOffset, 0, zOffset);
        }
        
        // Handle Y mode
        if ("surface".equals(spawnLocation.yMode)) {
            basePos = context.getWorld().getTopSolidOrLiquidBlock(basePos);
        }
        
        return basePos;
    }
    
    private void executeActionRef(EncounterConfig.Encounter.ActionRef actionRef, EncounterContext context) {
        EncounterConfig.Action action = null;
        
        if (actionRef.ref != null) {
            // Reference to a predefined action
            action = loader.getAction(actionRef.ref);
        } else if (actionRef.type != null) {
            // Inline action definition
            action = new EncounterConfig.Action();
            action.type = actionRef.type;
            action.scope = actionRef.scope;
            action.radius = actionRef.radius;
            action.message = actionRef.message;
            action.from = actionRef.from;
            action.to = actionRef.to;
            action.target = actionRef.target;
            action.label = actionRef.label;
            action.fromLabel = actionRef.fromLabel;
            action.spawnRef = actionRef.spawnRef;
            action.anchor = actionRef.anchor;
            action.offset = actionRef.offset;
            action.durationSeconds = actionRef.durationSeconds;
            action.seconds = actionRef.seconds;
            action.dropLeads = actionRef.dropLeads;
            action.labels = actionRef.labels;
            action.loot = actionRef.loot;
            action.trades = actionRef.trades;
        }
        
        if (action != null) {
            actionExecutor.executeAction(action, context);
        }
    }
    
    private void applyBehavior(EncounterConfig.Encounter.BehaviorRef behaviorRef, EncounterContext context) {
        EntityLiving entity = context.getEntity(behaviorRef.applyTo);
        if (entity == null) {
            return;
        }
        
        EncounterConfig.Behavior behavior = loader.getBehavior(behaviorRef.ref);
        if (behavior == null) {
            System.err.println("RandomEncounters: Unknown behavior reference: " + behaviorRef.ref);
            return;
        }
        
        switch (behavior.type) {
            case "moveToRoute":
                applyMoveToRouteBehavior(entity, behavior, context);
                break;
            case "leashFollow":
                applyLeashFollowBehavior(entity, behavior, context);
                break;
            case "guard":
                applyGuardBehavior(entity, behavior, context);
                break;
            default:
                System.err.println("RandomEncounters: Unknown behavior type: " + behavior.type);
                break;
        }
    }
    
    private void applyMoveToRouteBehavior(EntityLiving entity, EncounterConfig.Behavior behavior, EncounterContext context) {
        EncounterConfig.Route route = loader.getRoute(behavior.routeRef);
        if (route == null) {
            System.err.println("RandomEncounters: Unknown route reference: " + behavior.routeRef);
            return;
        }
        
        AIMoveToRoute moveAI = new AIMoveToRoute(
            (net.minecraft.entity.EntityCreature) entity, 
            route, 
            behavior.speed, 
            behavior.timeoutSeconds * 20, 
            context.getOriginPos()
        );
        
        entity.tasks.addTask(1, moveAI);
    }
    
    private void applyLeashFollowBehavior(EntityLiving entity, EncounterConfig.Behavior behavior, EncounterContext context) {
        EntityLiving holder = context.getEntity(behavior.holder);
        if (holder != null) {
            entity.setLeashedToEntity(holder, true);
        }
    }
    
    private void applyGuardBehavior(EntityLiving entity, EncounterConfig.Behavior behavior, EncounterContext context) {
        // Basic guard behavior - could be expanded
        // For now, just ensure the entity doesn't wander too far
        // Guard behavior - keep entity in specified radius (1.12.2 doesn't have setHomePositionAndDistance)
        // This would need custom AI implementation
    }
    
    @Override
    public String getName() {
        return encounterDef.id;
    }
    
    @Override
    public boolean canExecute(EntityPlayer player) {
        return encounterDef.enabled;
    }
}
