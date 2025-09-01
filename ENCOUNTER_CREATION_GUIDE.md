# Random Encounters Creation Guide

This guide explains how to create new random encounters for the Random Encounters mod (MC 1.12.2). There are two main approaches: creating hardcoded Java encounters and JSON-based encounters.

## Table of Contents

1. [Understanding the Encounter System](#understanding-the-encounter-system)
2. [Hardcoded Java Encounters](#hardcoded-java-encounters)
3. [JSON-Based Encounters](#json-based-encounters)
4. [Configuration Options](#configuration-options)
5. [Best Practices](#best-practices)
6. [Server Compatibility Guidelines](#server-compatibility-guidelines)
7. [Testing Your Encounters](#testing-your-encounters)

## Understanding the Encounter System

The Random Encounters mod uses a sophisticated system to trigger and manage encounters:

- **Encounter Manager**: Central system that manages all encounters
- **Trigger Conditions**: Cooldowns, distance checks, difficulty scaling
- **Execution Context**: Provides data about the world, player, and encounter state
- **Server-Side Focus**: All encounters run server-side for compatibility

### Core Components

- `IEncounter`: Interface that all encounters must implement
- `EncounterManager`: Manages encounter lifecycle and triggering
- `ConfigHandler`: Manages configuration options
- `EncounterContext`: Provides execution context for encounters

## Hardcoded Java Encounters

For complex behavior or when you need full Java capabilities, create hardcoded encounters.

### 1. Implementing the IEncounter Interface

Create a new class implementing `IEncounter`:

```java
package ai.torchlite.randomencounters.encounters.types;

import ai.torchlite.randomencounters.encounters.IEncounter;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;

public class MyCustomEncounter implements IEncounter {
    
    @Override
    public void execute(EntityPlayer player, double difficulty) {
        // Your encounter logic here
        player.sendMessage(new TextComponentString(
            TextFormatting.GOLD + "A custom encounter has triggered!"));
    }
    
    @Override
    public String getName() {
        return "My Custom Encounter";
    }
    
    @Override
    public boolean canExecute(EntityPlayer player) {
        // Add any preconditions here
        return true;
    }
}
```

### 2. Encounter Structure Examples

#### Mob Encounter Pattern
```java
public class MyMobEncounter implements IEncounter {
    
    private final Random random = new Random();
    
    @Override
    public void execute(EntityPlayer player, double difficulty) {
        World world = player.world;
        BlockPos playerPos = player.getPosition();
        
        // 1. Send warning message
        player.sendMessage(new TextComponentString(
            TextFormatting.YELLOW + "Something approaches..."));
        
        // 2. Calculate spawn count based on difficulty
        int mobCount = Math.max(1, (int)(difficulty * 2));
        mobCount = Math.min(mobCount, 5); // Cap at 5
        
        // 3. Spawn mobs
        for (int i = 0; i < mobCount; i++) {
            spawnCustomMob(world, playerPos, difficulty);
        }
        
        // 4. Send encounter message
        player.sendMessage(new TextComponentString(
            TextFormatting.GREEN + "Encounter spawned!"));
    }
    
    private void spawnCustomMob(World world, BlockPos playerPos, double difficulty) {
        // Find safe spawn location
        BlockPos spawnPos = findSpawnLocation(world, playerPos);
        if (spawnPos == null) return;
        
        // Create and configure mob
        EntityZombie mob = new EntityZombie(world);
        mob.setPosition(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5);
        
        // Scale mob based on difficulty
        float healthMultiplier = (float)(1.0 + (difficulty - 1.0) * 0.5);
        mob.setHealth(mob.getMaxHealth() * healthMultiplier);
        
        // Spawn in world
        world.spawnEntity(mob);
    }
    
    private BlockPos findSpawnLocation(World world, BlockPos playerPos) {
        // Try to find valid spawn location within 10-20 blocks
        for (int attempts = 0; attempts < 20; attempts++) {
            int x = playerPos.getX() + (random.nextInt(20) - 10);
            int z = playerPos.getZ() + (random.nextInt(20) - 10);
            
            for (int y = playerPos.getY() + 5; y > playerPos.getY() - 10; y--) {
                BlockPos checkPos = new BlockPos(x, y, z);
                if (!world.isAirBlock(checkPos.down()) && 
                    world.isAirBlock(checkPos) && 
                    world.isAirBlock(checkPos.up())) {
                    return checkPos;
                }
            }
        }
        return playerPos.add(5, 0, 5); // Fallback
    }
}
```

#### Loot Encounter Pattern
```java
public class MyLootEncounter implements IEncounter {
    
    @Override
    public void execute(EntityPlayer player, double difficulty) {
        World world = player.world;
        BlockPos playerPos = player.getPosition();
        
        // Send discovery message
        player.sendMessage(new TextComponentString(
            TextFormatting.GREEN + "You found something valuable!"));
        
        // Calculate loot quality based on difficulty
        int lootCount = Math.max(1, (int)(difficulty * 1.5));
        
        for (int i = 0; i < lootCount; i++) {
            spawnLoot(world, playerPos, difficulty);
        }
        
        // Award experience if enabled
        if (ConfigHandler.enableExperienceRewards) {
            int exp = (int)(ConfigHandler.baseExperienceReward * difficulty * 0.5);
            player.addExperience(exp);
        }
    }
    
    private void spawnLoot(World world, BlockPos playerPos, double difficulty) {
        ItemStack loot = createLootItem(difficulty);
        EntityItem item = new EntityItem(world, 
            playerPos.getX() + 0.5, playerPos.getY() + 1, playerPos.getZ() + 0.5, 
            loot);
        item.motionY = 0.2; // Add upward motion
        world.spawnEntity(item);
    }
    
    private ItemStack createLootItem(double difficulty) {
        if (difficulty < 2.0) {
            return new ItemStack(Items.IRON_INGOT, 1 + random.nextInt(3));
        } else {
            return new ItemStack(Items.DIAMOND, 1);
        }
    }
}
```

### 3. Registering Hardcoded Encounters

Add your encounter to `EncounterManager.initializeEncounters()`:

```java
// In EncounterManager.initializeEncounters()
if (ConfigHandler.enableMyCustomEncounters) {
    availableEncounters.add(new MyCustomEncounter());
}
```

Add a configuration option in `ConfigHandler`:

```java
// In ConfigHandler
public static boolean enableMyCustomEncounters = true;

// In init() method
prop = configuration.get("encounters", "enableMyCustomEncounters", enableMyCustomEncounters);
prop.setComment("Enable my custom encounters");
enableMyCustomEncounters = prop.getBoolean();
```

## JSON-Based Encounters

For easier configuration and server admin customization, use JSON encounters.

### 1. JSON Structure Overview

JSON encounters are defined in `config/randomencounters/encounters.json`:

```json
{
  "version": "1.0",
  "defaults": {
    "encounterCooldownSeconds": 900,
    "maxActivePerChunk": 1,
    "maxActiveGlobal": 32
  },
  "blocks": {
    "selectors": { /* trigger conditions */ },
    "routes": { /* movement paths */ },
    "spawns": { /* entity definitions */ },
    "behaviors": { /* AI behaviors */ },
    "actions": { /* encounter actions */ }
  },
  "encounters": [ /* encounter definitions */ ]
}
```

### 2. Creating Reusable Blocks

#### Selectors (Trigger Conditions)
```json
"my_forest_selector": {
  "type": "selector",
  "biomes": ["minecraft:forest", "minecraft:roofed_forest"],
  "dimensions": [0],
  "weather": ["clear", "rain"],
  "timeOfDay": { "from": 13000, "to": 23000 },
  "playerConstraints": {
    "minLevel": 5,
    "maxDistanceFromSpawn": 10000,
    "requireNotRiding": true
  },
  "cooldowns": {
    "perPlayerSeconds": 1200,
    "perChunkSeconds": 900,
    "globalSeconds": 60
  },
  "weights": { "base": 10 }
}
```

#### Spawns (Entity Definitions)
```json
"my_custom_villager": {
  "type": "entity",
  "entityId": "minecraft:villager",
  "count": 1,
  "persistence": true,
  "equipment": {
    "mainhand": { "item": "minecraft:stick", "count": 1 },
    "chest": { "item": "minecraft:leather_chestplate", "count": 1 }
  },
  "nbt": { "Profession": 1 },
  "aiToggles": {
    "canPickUpLoot": false,
    "followRange": 32.0,
    "removeDefaultWander": true
  }
}
```

#### Routes (Movement Paths)
```json
"patrol_route": {
  "type": "route",
  "mode": "loop",
  "arriveRadius": 3.0,
  "waypoints": [
    { "dx": 0, "dy": 0, "dz": 0, "yMode": "surface" },
    { "dx": 20, "dy": 0, "dz": 10, "yMode": "surface" },
    { "dx": -10, "dy": 0, "dz": 20, "yMode": "surface" }
  ]
}
```

#### Behaviors (AI Actions)
```json
"patrol_behavior": {
  "type": "moveToRoute",
  "routeRef": "patrol_route",
  "speed": 1.0,
  "timeoutSeconds": 180
}
```

#### Actions (Encounter Events)
```json
"greeting_message": {
  "type": "broadcast",
  "scope": "nearbyPlayers",
  "radius": 32,
  "message": "§aA friendly villager appears!"
},
"give_reward": {
  "type": "giveLoot",
  "to": "nearestPlayer",
  "loot": [
    { "item": "minecraft:bread", "min": 2, "max": 4, "weight": 3 },
    { "item": "minecraft:emerald", "min": 1, "max": 1, "weight": 1 }
  ]
}
```

### 3. Complete Encounter Example

```json
{
  "id": "my_friendly_villager",
  "enabled": true,
  "weight": 15,
  "selectorRef": "my_forest_selector",
  "limits": {
    "maxActiveGlobal": 3,
    "maxActivePerPlayer": 1,
    "despawnAfterSeconds": 300
  },
  "spawn": [
    {
      "label": "villager",
      "ref": "my_custom_villager",
      "at": {
        "mode": "nearPlayer",
        "radius": { "min": 8, "max": 15 },
        "yMode": "surface"
      }
    }
  ],
  "onStart": [
    { "ref": "greeting_message" },
    {
      "type": "sayAboveHead",
      "target": "villager",
      "message": "§7Hello there, traveler!",
      "durationSeconds": 5
    }
  ],
  "behaviors": [
    { "applyTo": "villager", "ref": "patrol_behavior" }
  ],
  "triggers": [
    {
      "when": "arrivedAtRouteEnd",
      "actions": [
        { "ref": "give_reward" },
        {
          "type": "broadcast",
          "scope": "nearbyPlayers",
          "radius": 16,
          "message": "§eThe villager has left you a gift!"
        }
      ]
    }
  ],
  "onTimeout": [
    {
      "type": "broadcast",
      "scope": "nearbyPlayers",
      "radius": 32,
      "message": "§7The villager waves goodbye and disappears."
    }
  ],
  "onCleanup": [
    { "type": "removeEntities", "labels": ["villager"] }
  ]
}
```

### 4. Available Action Types

| Action Type | Description | Example Use |
|-------------|-------------|-------------|
| `broadcast` | Send message to players | Announcements |
| `sayAboveHead` | Display hologram text | NPC speech |
| `spawn` | Spawn additional entities | Reinforcements |
| `giveLoot` | Give items to players | Rewards |
| `leash` | Leash entities together | Pet following |
| `openTrades` | Open trading interface | Merchant |
| `placeLeashPost` | Place fence+knot | Tying animals |
| `cleanup` | Remove encounter entities | Cleanup |

### 5. Available Behavior Types

| Behavior Type | Description | Configuration |
|---------------|-------------|---------------|
| `moveToRoute` | Follow waypoint path | `routeRef`, `speed`, `timeoutSeconds` |
| `leashFollow` | Follow another entity | `holder`, `snapDistance`, `calm` |
| `guard` | Protect an area | `radius`, `targets`, `speed` |

## Configuration Options

### Global Settings (ConfigHandler)
```java
// Core settings
enableRandomEncounters = false  // Master enable/disable
baseEncounterChance = 5         // % chance per minute
encounterCooldown = 300         // Seconds between encounters
maxEncountersPerDay = 10        // Daily limit per player

// Distance settings
minDistanceFromSpawn = 500      // Minimum distance from spawn
minDistanceBetweenPlayers = 100 // Player separation

// Encounter types
enableMobEncounters = true      // Mob spawning encounters
enableLootEncounters = true     // Item reward encounters
enableEventEncounters = true    // Weather/effect encounters
enableNPCEncounters = false     // NPC interaction encounters

// Difficulty scaling
scaleWithPlayerLevel = true     // Scale with XP level
scaleWithDistance = true        // Scale with distance from spawn
difficultyMultiplier = 1.0      // Global difficulty modifier

// Rewards
enableExperienceRewards = true  // Give XP rewards
enableLootRewards = true        // Give item rewards
baseExperienceReward = 50       // Base XP amount
```

### Difficulty Calculation

The difficulty value passed to encounters is calculated as:
```java
double difficulty = ConfigHandler.difficultyMultiplier;

if (ConfigHandler.scaleWithPlayerLevel) {
    difficulty *= (1.0 + (player.experienceLevel * 0.1)); // +10% per level
}

if (ConfigHandler.scaleWithDistance) {
    double extraDistance = Math.max(0, distanceFromSpawn - ConfigHandler.minDistanceFromSpawn);
    difficulty *= (1.0 + (extraDistance * 0.001)); // +0.1% per block
}
```

## Best Practices

### 1. Server Performance
- Limit entity count (max 5-10 entities per encounter)
- Use entity persistence carefully (only when necessary)
- Implement proper cleanup in `onTimeout` and `onCleanup`
- Avoid complex AI that runs every tick

### 2. Player Experience
- Send clear messages about what's happening
- Provide meaningful rewards scaled to difficulty
- Use appropriate cooldowns to prevent spam
- Test encounters at different difficulty levels

### 3. Code Quality
- Always check for null values (world, player, entities)
- Handle exceptions gracefully
- Use the Random instance consistently
- Follow Minecraft's coordinate system conventions

### 4. JSON Organization
- Use descriptive IDs for all blocks and encounters
- Group related blocks logically
- Comment complex configurations
- Test JSON syntax before deployment

## Server Compatibility Guidelines

⚠️ **CRITICAL**: This mod must maintain server-side compatibility!

### ✅ Allowed Operations
- Spawn existing Minecraft entities
- Modify entity NBT data
- Send chat messages to players
- Create item entities (loot drops)
- Use existing AI tasks
- Modify entity equipment
- Use server-side events

### ❌ Forbidden Operations
- Import from `net.minecraft.client.*` packages
- Create custom entities, blocks, or items
- Use custom renderers or GUIs
- Direct OpenGL calls
- Client-side packet handling
- Custom world generation

### Entity Compatibility
Use only vanilla entity IDs:
- `minecraft:villager`
- `minecraft:zombie`
- `minecraft:skeleton`
- `minecraft:wolf`
- `minecraft:cow`
- `minecraft:pig`
- `minecraft:chicken`
- And other vanilla entities

## Testing Your Encounters

### 1. Configuration Testing
```bash
# Enable debug mode
/encounters config
/encounters enable

# Test specific encounter types
/encounters test mob
/encounters test loot
/encounters test json

# Force trigger for immediate testing
/encounters force
```

### 2. Check Available Types
```bash
/encounters types
```

### 3. Monitor Performance
- Watch server TPS during encounters
- Check for memory leaks with large encounters
- Test with multiple players simultaneously
- Verify cleanup occurs properly

### 4. Compatibility Testing
- Test on dedicated server
- Verify clients without mod can connect
- Test with RLCraft Dregora modpack
- Check console for errors

### 5. JSON Validation
- Use JSON validators to check syntax
- Test incremental changes
- Backup working configurations
- Use the `/encounters reload` command for config changes

## Advanced Examples

### Complex Group Encounter
```json
{
  "id": "bandit_camp_raid",
  "enabled": true,
  "weight": 5,
  "selectorRef": "dangerous_night_selector",
  "spawn": [
    {
      "label": "bandit_leader",
      "ref": "villager_bandit_leader",
      "at": { "mode": "nearPlayer", "radius": { "min": 15, "max": 20 }, "yMode": "surface" }
    },
    {
      "label": "guard1",
      "ref": "skeleton_archer",
      "at": { "mode": "nearLabel", "label": "bandit_leader", "radius": { "min": 3, "max": 5 } }
    },
    {
      "label": "guard2",
      "ref": "zombie_warrior",
      "at": { "mode": "nearLabel", "label": "bandit_leader", "radius": { "min": 3, "max": 5 } }
    }
  ],
  "onStart": [
    {
      "type": "broadcast",
      "scope": "nearbyPlayers",
      "radius": 32,
      "message": "§c⚔ A bandit camp has been spotted nearby!"
    },
    {
      "type": "sayAboveHead",
      "target": "bandit_leader",
      "message": "§4You picked the wrong area, stranger!",
      "durationSeconds": 8
    }
  ],
  "behaviors": [
    { "applyTo": "guard1", "ref": "guard_leader_behavior" },
    { "applyTo": "guard2", "ref": "guard_leader_behavior" }
  ],
  "triggers": [
    {
      "when": "leaderKilled",
      "actions": [
        {
          "type": "broadcast",
          "scope": "nearbyPlayers",
          "radius": 32,
          "message": "§aThe bandit leader has fallen! The camp scatters!"
        },
        {
          "type": "giveLoot",
          "to": "nearestPlayer",
          "loot": [
            { "item": "minecraft:emerald", "min": 3, "max": 5, "weight": 1 },
            { "item": "minecraft:gold_ingot", "min": 2, "max": 3, "weight": 1 }
          ]
        }
      ]
    }
  ]
}
```

This guide provides comprehensive coverage of both hardcoded and JSON-based encounter creation. Start with simple encounters and gradually build complexity as you become familiar with the system!
