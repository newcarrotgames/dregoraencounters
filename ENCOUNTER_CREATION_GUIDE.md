# Random Encounters Creation Guide

This guide explains how to create new random encounters for the Random Encounters mod (MC 1.12.2). All encounters are now configured through JSON files, providing complete server-side customization without modifying the mod.

## Table of Contents

1. [🚀 NEW: Expression-Powered Encounters](#-new-expression-powered-encounters)
2. [Understanding the Encounter System](#understanding-the-encounter-system)
3. [JSON-Based Encounters](#json-based-encounters)
4. [Configuration Options](#configuration-options)
5. [Best Practices](#best-practices)
6. [Server Compatibility Guidelines](#server-compatibility-guidelines)
7. [Testing Your Encounters](#testing-your-encounters)
8. [Legacy: Java Interface Reference](#legacy-java-interface-reference)

## 🚀 NEW: Expression-Powered Encounters

**MAJOR UPDATE**: Your Random Encounters mod now supports **mathematical expressions and dynamic scaling**! Server admins can create infinitely customizable encounters using variables, math functions, and conditional logic—all through JSON configuration.

### ⚡ **What's New**

- **Expression Parser**: Use formulas like `Math.max(4, difficulty * 3)` in JSON
- **Dynamic Variables**: Access `difficulty`, `playerLevel`, `timeOfDay`, `isRaining`, etc.
- **Conditional Logic**: `condition: "difficulty >= 3 && isNight"`
- **Attribute Scaling**: `"health": "20 + Math.floor(difficulty * 10)"`
- **Smart Enchantments**: `{"id": "sharpness", "level": "Math.min(5, Math.floor(difficulty))"}`

### 📖 **Quick Example**

```json
{
  "id": "dynamic_army",
  "variables": {
    "armySize": "Math.max(5, Math.floor(4 + difficulty * 3))",
    "isElite": "difficulty >= 3"
  },
  "spawn": [{
    "count": "armySize",
    "customName": "isElite ? '§4Elite Commander' : '§6Skeleton Captain'",
    "attributes": {
      "health": "20 + Math.floor(difficulty * 10)",
      "attackDamage": "8 + Math.floor(difficulty * 2)"
    }
  }]
}
```

**Result**: Army size scales from 7 at difficulty 1.0 to 16 at difficulty 4.0, with elite names and exponentially increasing health/damage!

### 📘 **Complete Server Admin Guide**

For the full expression system documentation with advanced examples, see:
**→ [SERVER_ADMIN_EXPRESSION_GUIDE.md](SERVER_ADMIN_EXPRESSION_GUIDE.md) ←**

This guide covers:
- All available variables and math functions
- Complex scaling formulas for armies and loot
- Weather/time-based encounters
- Player-level scaling rewards
- Distance-based elite spawns
- Copy-paste templates for server admins

**🎯 Server admins should focus on the Expression Guide for maximum customization power!**

---

## Understanding the Encounter System

The Random Encounters mod uses a sophisticated system to trigger and manage encounters:

- **Encounter Manager**: Central system that manages all encounters
- **Trigger Conditions**: Cooldowns, distance checks, difficulty scaling
- **Execution Context**: Provides data about the world, player, and encounter state
- **Server-Side Focus**: All encounters run server-side for compatibility

### Core Components

- `JsonEncounter`: The JSON-based encounter implementation used by all encounters
- `EncounterManager`: Manages encounter lifecycle and triggering from JSON configuration
- `JsonEncounterLoader`: Loads encounters from JSON files in `config/randomencounters/encounters/`
- `EncounterContext`: Provides execution context for encounters
- `ConfigHandler`: Manages global configuration options

## JSON-Based Encounters

**ALL encounters are now defined through JSON configuration files!** This provides server administrators with complete control over encounter behavior without modifying the mod.

### Why JSON-Only?

- **Server Control**: Administrators can customize all encounters without mod changes
- **Hot Reloading**: Encounters can be modified and reloaded without server restarts
- **No Compilation**: Create complex encounters without Java knowledge
- **Expression System**: Use mathematical expressions and conditional logic in JSON
- **Modular**: Each encounter is a separate JSON file for easy management

### JSON Structure Overview

Every JSON encounter follows this basic structure:

```json
{
  "id": "enc_my_encounter",          // Unique identifier
  "enabled": true,                   // Whether encounter is active
  "weight": 10,                      // Spawn probability weight
  "selectorRef": "default_random",   // Reference to selector in blocks.json
  "limits": {                        // Spawn limits
    "maxActiveGlobal": 5,
    "maxActivePerPlayer": 1,
    "despawnAfterSeconds": 300
  },
  "spawn": [...],                    // Entities to spawn
  "onStart": [...],                  // Actions when encounter starts
  "behaviors": [...],                // AI behaviors for spawned entities
  "triggers": [...],                 // Conditional triggers
  "onTimeout": [...],                // Actions when time expires
  "onCleanup": [...]                 // Cleanup actions
}
```

### 2. Example Encounters

#### Basic Mob Spawn
```json
{
  "id": "enc_mob_spawn",
  "enabled": true,
  "weight": 20,
  "selectorRef": "default_random",
  "limits": { 
    "maxActiveGlobal": 10, 
    "maxActivePerPlayer": 3,
    "despawnAfterSeconds": 600 
  },
  "spawn": [
    { 
      "label": "enemy1", 
      "ref": "random_hostile", 
      "at": { 
        "mode": "nearPlayer", 
        "radius": { "min": 10, "max": 20 }, 
        "yMode": "surface" 
      } 
    },
    { 
      "label": "enemy2", 
      "ref": "random_hostile", 
      "at": { 
        "mode": "nearPlayer", 
        "radius": { "min": 10, "max": 20 }, 
        "yMode": "surface",
        "probability": 0.75
      } 
    }
  ],
  "onStart": [
    { 
      "type": "broadcast", 
      "scope": "nearbyPlayers", 
      "radius": 32, 
      "message": "\u00A76You hear strange noises in the distance..." 
    },
    { 
      "type": "broadcast", 
      "scope": "nearbyPlayers", 
      "radius": 32, 
      "message": "\u00A7cHostile creatures approach!",
      "delay": 2
    }
  ],
  "onTimeout": [
    { "type": "broadcast", "scope": "nearbyPlayers", "radius": 32, "message": "\u00A77The hostile creatures have dispersed." }
  ],
  "onCleanup": [
    { "type": "removeEntities", "labels": ["enemy1", "enemy2"] }
  ]
}
```

#### Loot Chest Discovery
```json
{
  "id": "enc_loot_chest",
  "enabled": true,
  "weight": 10,
  "selectorRef": "default_random",
  "limits": { 
    "maxActiveGlobal": 5, 
    "maxActivePerPlayer": 1,
    "despawnAfterSeconds": 300 
  },
  "onStart": [
    { 
      "type": "broadcast", 
      "scope": "nearbyPlayers", 
      "radius": 16, 
      "message": "\u00A76You discover a hidden cache of supplies!" 
    },
    {
      "type": "giveLoot",
      "to": "nearestPlayer",
      "loot": [
        { "item": "minecraft:iron_ingot", "min": 1, "max": 3, "weight": 5 },
        { "item": "minecraft:gold_ingot", "min": 1, "max": 2, "weight": 3 },
        { "item": "minecraft:diamond", "min": 1, "max": 1, "weight": 1 },
        { "item": "minecraft:bread", "min": 2, "max": 5, "weight": 4 },
        { "item": "minecraft:experience_bottle", "min": 1, "max": 3, "weight": 2 }
      ]
    },
    { 
      "type": "broadcast", 
      "scope": "nearbyPlayers", 
      "radius": 16, 
      "message": "\u00A7aYou gain valuable resources!",
      "delay": 1
    }
  ]
}
```

### Creating Your First Encounter

1. Navigate to `config/randomencounters/encounters/`
2. Create a new `.json` file (e.g., `my_encounter.json`)
3. Define your encounter using the JSON structure
4. Use `/encounters reload` to load the new encounter (or restart server)
5. Test with `/encounters test my_encounter`

### JSON Encounter Structure

**🚀 NEW**: JSON encounters now feature **EXPRESSION-POWERED SCALING**! Create encounters that dynamically adapt to difficulty, player level, weather, time, and distance using mathematical expressions and conditional logic—all without Java knowledge!

### ⚡ **Expression System Power**

Your encounters can now use **mathematical expressions** and **conditional logic**:

```json
{
  "variables": {
    "armySize": "Math.max(3, Math.floor(difficulty * 2))",
    "isElite": "difficulty >= 3 && isNight", 
    "lootMultiplier": "playerLevel > 20 ? 2.0 : 1.0",
    "healthScaling": "20 + Math.floor(difficulty * 10)"
  },
  "spawn": [{
    "count": "armySize",
    "customName": "isElite ? '\u00A74\u00A7lElite Commander' : '\u00A76Captain'",
    "attributes": {
      "health": "healthScaling",
      "attackDamage": "8 + Math.floor(difficulty * 2)"
    },
    "equipment": {
      "mainhand": {
        "item": "difficulty >= 4 ? 'minecraft:diamond_sword' : 'minecraft:iron_sword'",
        "enchantments": [
          {"id": "sharpness", "level": "Math.min(5, 1 + Math.floor(difficulty))"}
        ]
      }
    },
    "condition": "isNight || difficulty >= 2"
  }]
}
```

**🎯 Available Variables:**
- `difficulty` - Server difficulty multiplier (1.0-6.0+)
- `playerLevel` - Player's experience level
- `nearbyPlayers` - Players within 50 blocks  
- `timeOfDay` - Minecraft time (0-24000)
- `distanceFromSpawn` - Distance from world spawn
- `isRaining`, `isThundering`, `isDay`, `isNight` - Weather/time booleans

**🧮 Math Functions:**
- `Math.max(a, b)`, `Math.min(a, b)` - Min/max values
- `Math.floor(x)`, `Math.ceil(x)`, `Math.round(x)` - Rounding functions
- Operators: `+`, `-`, `*`, `/`, `>=`, `<=`, `==`, `!=`, `&&`, `||`
- Conditionals: `condition ? valueA : valueB`

### 1. File Structure Overview

#### **🚀 New Structure - Expression-Powered Individual Files:**
```
config/randomencounters/
├── enhanced_blocks.json     # Expression-powered shared components
└── encounters/              # Individual encounter files with expressions
    ├── expression_powered_army.json
    ├── dynamic_skeleton_demo.json  
    └── my_custom_encounter.json
```

**enhanced_blocks.json** contains expression-powered shared components:
```json
{
  "version": "2.0",
  "globalVariables": {
    "baseArmySize": "8",
    "difficultyScaling": "Math.max(1, Math.min(difficulty * 4, 6))",
    "eliteThreshold": "3.0"
  },
  "presets": {
    "enchantmentPools": {
      "elite_weapon_enchants": {
        "minEnchants": 1,
        "maxEnchants": "3",
        "levelBonus": "Math.floor(difficulty)",
        "options": [
          {"id": "sharpness", "level": "Math.min(3, 1 + Math.floor(difficulty))", "weight": 4},
          {"id": "fire_aspect", "level": "1", "condition": "difficulty >= 3", "weight": 2}
        ]
      }
    },
    "entityPresets": {
      "skeleton_commander": {
        "customName": "\u00A76\u00A7lSkeleton Commander",
        "attributes": {
          "health": {"multiply": "1.8 + (difficulty * 0.2)"},
          "attackDamage": {"multiply": "1.3 + (difficulty * 0.1)"}
        },
        "enhancedEquipment": {
          "mainhand": {
            "item": "minecraft:diamond_sword",
            "randomEnchants": "elite_weapon_enchants"
          }
        }
      }
    }
  },
  "blocks": {
    "selectors": {
      "army_spawn_conditions": {
        "weightExpression": "Math.max(1, difficulty * nightTimeBonus)",
        "customCondition": "difficulty >= 2.0 && !isRaining"
      }
    },
    "actions": {
      "dynamic_army_announcement": {
        "type": "dynamicBroadcast",
        "radiusExpression": "32 + Math.floor(difficulty * 8)",
        "messageTemplate": {
          "template": "\u00A76\u00A7lAN ARMY OF {armySize} WARRIORS ARRIVES!",
          "variables": {"armySize": "baseArmySize + Math.floor(difficultyScaling)"}
        }
      }
    }
  }
}
```

**Individual encounter files** with expression power:
```json
{
  "id": "my_dynamic_encounter",
  "enabled": true,
  "weight": 10,
  "weightExpression": "Math.max(1, difficulty * 2)",
  "variables": {
    "customArmySize": "Math.max(5, Math.floor(4 + difficulty * 3))",
    "isEliteEncounter": "difficulty >= 3",
    "experienceReward": "Math.floor(customArmySize * difficulty * 50)"
  },
  "conditionalSpawns": [
    {
      "condition": "isEliteEncounter",
      "spawn": [{"ref": "skeleton_commander", "count": "1"}]
    },
    {
      "condition": "customArmySize > 10",
      "spawn": [{"ref": "skeleton_archer_elite", "count": "Math.floor(customArmySize / 3)"}]
    }
  ],
  "conditionalActions": [
    {
      "when": "onStart",
      "condition": "isEliteEncounter",
      "actions": [{"actionRef": "dynamic_army_announcement"}]
    }
  ]
}
```

#### **Legacy Structure - Basic JSON:**
Original `encounters.json` format (still supported but lacks expression features):
```json
{
  "version": "1.0",
  "encounters": [ /* basic encounters without expressions */ ]
}
```

**🎉 Benefits of Expression-Powered Encounters:**
- ✅ **Dynamic Scaling**: Perfect balance across all difficulty levels
- ✅ **Weather Reactive**: Storms make encounters more intense
- ✅ **Distance-Based**: Exploration rewards scale with distance traveled
- ✅ **Player-Level Aware**: Encounters that match player progression
- ✅ **Group Scaling**: More players = appropriately larger encounters
- ✅ **Conditional Logic**: Complex if/then behavior without coding
- ✅ **Smart Enchantments**: Enchantment levels that scale intelligently
- ✅ **Server Admin Friendly**: No Java knowledge required

### 2. Creating Expression-Powered Reusable Blocks

#### 🧮 Enhanced Selectors (with Expression Logic)
```json
"dynamic_forest_selector": {
  "type": "enhancedSelector",
  "biomes": ["minecraft:forest", "minecraft:roofed_forest"],
  "dimensions": [0],
  "timeOfDay": { "from": 13000, "to": 23000 },
  "playerConstraints": {
    "minLevel": 5,
    "maxDistanceFromSpawn": 20000,
    "requireNotRiding": true
  },
  "cooldowns": {
    "perPlayerSeconds": 1800,
    "perChunkSeconds": "Math.floor(900 + difficulty * 300)",
    "globalSeconds": 120
  },
  "weightExpression": "Math.max(1, difficulty * 2)",
  "customCondition": "difficulty >= 1.5 && (isNight || isRaining)"
}
```

#### 💪 Enhanced Spawns (with Attribute Scaling & Smart Equipment)
```json
"dynamic_skeleton_warrior": {
  "type": "entity",
  "entityId": "minecraft:skeleton",
  "count": 1,
  "persistence": true,
  "customName": "difficulty >= 3 ? '\u00A74\u00A7lElite Warrior' : '\u00A7aSkeleton Guardian'",
  "showNameTag": true,
  "attributes": {
    "health": {"multiply": "1.2 + (difficulty * 0.3)"},
    "attackDamage": {"multiply": "1.1 + (difficulty * 0.15)"},
    "followRange": {"set": "16.0 + Math.floor(difficulty * 4)"}
  },
  "enhancedEquipment": {
    "mainhand": {
      "item": "difficulty >= 4 ? 'minecraft:diamond_sword' : 'minecraft:iron_sword'",
      "enchantments": [
        {"id": "sharpness", "level": "Math.min(5, 1 + Math.floor(difficulty))", "condition": "difficulty >= 1"},
        {"id": "fire_aspect", "level": "1", "condition": "difficulty >= 3"},
        {"id": "knockback", "level": "1", "condition": "difficulty >= 2"}
      ],
      "customName": "\u00A76Ancient Blade"
    },
    "helmet": {
      "item": "difficulty >= 3 ? 'minecraft:iron_helmet' : 'minecraft:leather_helmet'",
      "randomEnchants": {
        "maxEnchants": "Math.max(1, Math.floor(difficulty / 2))",
        "options": [
          {"id": "protection", "level": "Math.min(3, Math.floor(difficulty))", "weight": 4},
          {"id": "unbreaking", "level": "Math.min(2, Math.floor(difficulty / 2))", "weight": 2}
        ]
      }
    }
  },
  "aiToggles": {
    "clearTargetTasks": true,
    "canPickUpLoot": false,
    "followRange": "16.0 + Math.floor(difficulty * 4)"
  }
}
```

#### 🎯 Enhanced Behaviors (with Dynamic Parameters)
```json
"adaptive_guardian_behavior": {
  "type": "friendlyGuardian",
  "targets": ["hostile", "!skeleton", "!player"],
  "excludeTargets": ["minecraft:skeleton"],
  "radiusExpression": "8 + Math.floor(difficulty * 2)",
  "speedExpression": "1.0 + (difficulty * 0.1)",
  "condition": "isNight || difficulty >= 2",
  "friendlyFire": false,
  "customAI": {
    "addHurtByTarget": "!instanceof EntityPlayer",
    "addNearestAttackableTarget": "EntityMob.class, !instanceof EntitySkeleton"
  }
}
```

#### 🎬 Enhanced Actions (with Expression Templates)
```json
"dynamic_army_announcement": {
  "type": "dynamicBroadcast",
  "scope": "nearbyPlayers",
  "radiusExpression": "32 + Math.floor(difficulty * 8)",
  "condition": "difficulty >= 2",
  "messageTemplate": {
    "template": "\u00A76\u00A7lAn army of {armySize} {armyType} warriors arrives!",
    "variables": {
      "armySize": "Math.max(5, Math.floor(baseArmySize + difficultyScaling))",
      "armyType": "difficulty >= 4 ? 'LEGENDARY' : (difficulty >= 3 ? 'ELITE' : 'brave')"
    }
  }
},
"smart_loot_reward": {
  "type": "enhancedLootDrop",
  "to": "nearestPlayer",
  "condition": "difficulty >= 1.5",
  "loot": [
    {
      "item": "minecraft:bone",
      "min": "Math.floor(2 + difficulty * 2)",
      "max": "Math.floor(4 + difficulty * 4)",
      "weight": 5
    },
    {
      "item": "minecraft:iron_ingot",
      "min": "Math.floor(1 + difficulty)",
      "max": "Math.floor(2 + difficulty * 2)",
      "weight": 3,
      "condition": "difficulty >= 2"
    },
    {
      "item": "minecraft:diamond",
      "min": "1",
      "max": "Math.floor(difficulty / 2)",
      "weight": 1,
      "condition": "difficulty >= 4"
    }
  ]
}
```

#### 🎭 Enchantment Pools (Smart Weapon & Armor Enhancement)
```json
"adaptive_weapon_enchants": {
  "minEnchants": 0,
  "maxEnchants": "Math.max(2, Math.floor(difficulty))",
  "levelBonus": "Math.floor(difficulty / 2)",
  "options": [
    {"id": "sharpness", "level": "Math.min(4, 1 + Math.floor(difficulty))", "weight": 5},
    {"id": "knockback", "level": "Math.min(2, Math.floor(difficulty / 2))", "weight": 3},
    {"id": "fire_aspect", "level": "1", "weight": 2, "condition": "difficulty >= 3"},
    {"id": "looting", "level": "Math.min(2, Math.floor(difficulty / 3))", "weight": 1, "condition": "difficulty >= 4"}
  ]
}
```

### 3. Creating Expression-Powered Encounters - Step-by-Step

#### **🚀 Step 1: Create Your Dynamic Encounter File**
Create `config/randomencounters/encounters/my_dynamic_encounter.json`:

```json
{
  "id": "my_scaling_skeleton_army",
  "enabled": true,
  "weight": 12,
  "weightExpression": "Math.max(1, difficulty * 1.5)",
  "description": "Dynamic skeleton army that scales with difficulty and reacts to weather",
  "selectorRef": "dynamic_forest_selector",
  
  "variables": {
    "armySize": "Math.max(3, Math.min(15, Math.floor(3 + difficulty * 2.5)))",
    "commanderCount": "Math.max(1, Math.floor(armySize / 8))", 
    "archerCount": "Math.floor(armySize * 0.4)",
    "warriorCount": "armySize - commanderCount - archerCount",
    "isElite": "difficulty >= 3",
    "weatherBonus": "isRaining ? 1.3 : (isThundering ? 1.8 : 1.0)",
    "experienceReward": "Math.floor(armySize * difficulty * 25)"
  },
  
  "limits": {
    "maxActiveGlobal": 2,
    "maxActivePerPlayer": 1,
    "despawnAfterSeconds": "Math.floor(600 + difficulty * 200)"
  },
  
  "conditionalSpawns": [
    {
      "description": "Always spawn at least one commander",
      "condition": "true",
      "spawn": [
        {
          "label": "commander",
          "ref": "dynamic_skeleton_warrior",
          "count": "commanderCount",
          "customName": "isElite ? '\u00A74\u00A7lElite Bone Commander' : '\u00A76Skeleton Commander'",
          "attributes": {
            "health": "25 + Math.floor(difficulty * 15)",
            "attackDamage": "10 + Math.floor(difficulty * 3)"
          },
          "equipment": {
            "mainhand": {
              "item": "difficulty >= 4 ? 'minecraft:diamond_sword' : 'minecraft:iron_sword'",
              "enchantments": [
                {"id": "sharpness", "level": "Math.min(4, 2 + Math.floor(difficulty))"}
              ]
            }
          },
          "at": {
            "mode": "nearPlayer",
            "radius": {"min": 8, "max": 12},
            "yMode": "surface"
          }
        }
      ]
    },
    {
      "description": "Spawn archers if army is large enough",
      "condition": "archerCount > 0",
      "spawn": [
        {
          "label": "archers",
          "ref": "dynamic_skeleton_warrior",
          "count": "archerCount",
          "customName": "'\u00A7aSkeleton Archer'",
          "equipment": {
            "mainhand": {
              "item": "minecraft:bow",
              "randomEnchants": "adaptive_weapon_enchants"
            }
          },
          "attributes": {
            "health": "18 + Math.floor(difficulty * 8)"
          },
          "at": {
            "mode": "nearPlayer",
            "radius": {"min": 10, "max": 16},
            "yMode": "surface"
          }
        }
      ]
    },
    {
      "description": "Spawn warriors based on calculated count",
      "condition": "warriorCount > 0",
      "spawn": [
        {
          "label": "warriors",
          "ref": "dynamic_skeleton_warrior", 
          "count": "warriorCount",
          "customName": "'\u00A7aSkeleton Warrior'",
          "at": {
            "mode": "nearPlayer",
            "radius": {"min": 8, "max": 14},
            "yMode": "surface"
          }
        }
      ]
    }
  ],
  
  "onStart": [
    {
      "type": "broadcast",
      "scope": "nearbyPlayers",
      "radiusExpression": "32 + Math.floor(difficulty * 8)",
      "message": "difficulty >= 3 ? '\u00A74\u00A7lAn ELITE skeleton army emerges!' : '\u00A76A skeleton army rises from the earth!'"
    },
    {
      "type": "dynamicMessage",
      "scope": "nearbyPlayers",
      "radius": 32,
      "template": "\u00A7eArmy composition: {commanderCount} commanders, {archerCount} archers, {warriorCount} warriors",
      "delay": 2
    },
    {
      "type": "sayAboveHead",
      "target": "commander",
      "message": "isElite ? '\u00A74FOR HONOR AND BONE!' : '\u00A76We fight for the living!'",
      "durationSeconds": 8,
      "delay": 3
    },
    {
      "type": "giveExperience",
      "to": "nearestPlayer",
      "amountExpression": "experienceReward",
      "message": "isElite ? '\u00A7d\u00A7lELITE ARMY BONUS!' : '\u00A7b+{experienceReward} experience!'",
      "delay": 4
    }
  ],
  
  "behaviors": [
    {"applyTo": "all", "ref": "adaptive_guardian_behavior"}
  ],
  
  "triggers": [
    {
      "when": "entityKilled",
      "target": "commander",
      "actions": [
        {
          "type": "broadcast",
          "scope": "nearbyPlayers",
          "radius": 32,
          "message": "'\u00A7cThe commander falls! The army fights with renewed fury!'"
        },
        {
          "type": "buffEntities",
          "applyTo": "all",
          "buffs": {
            "attackDamage": "Math.floor(2 + difficulty)",
            "movementSpeed": "0.1"
          },
          "durationSeconds": 300
        }
      ]
    }
  ],
  
  "conditionalActions": [
    {
      "when": "onStart",
      "condition": "isRaining",
      "actions": [
        {
          "type": "broadcast",
          "scope": "nearbyPlayers", 
          "radius": 32,
          "message": "'\u00A77The rain strengthens their bones...'"
        }
      ]
    },
    {
      "when": "onStart",
      "condition": "difficulty >= 2",
      "actions": [
        {
          "actionRef": "smart_loot_reward"
        }
      ]
    }
  ],
  
  "onTimeout": [
    {
      "type": "sayAboveHead",
      "target": "commander", 
      "message": "isElite ? '\u00A74Until darkness calls us again...' : '\u00A76Until the realm needs us...'",
      "durationSeconds": 6
    },
    {
      "type": "broadcast",
      "scope": "nearbyPlayers",
      "radius": 32,
      "message": "isElite ? '\u00A77The elite army fades into legend.' : '\u00A77The skeleton army returns to the shadows.'"
    }
  ],
  
  "onCleanup": [
    {"type": "removeEntities", "labels": ["commander", "archers", "warriors"]}
  ]
}
```

#### **🧪 Step 2: Test Your Expression-Powered Encounter**
- Save the file in `config/randomencounters/encounters/`
- Use `/encounters reload` to reload configurations
- Test with `/encounters test my_scaling_skeleton_army`
- Try different difficulty levels to see scaling in action!

#### **⚙️ Step 3: Fine-tune Your Expressions**
Edit the variables to customize scaling:
```json
"variables": {
  "armySize": "Math.max(5, Math.floor(5 + difficulty * 3))",     // Larger armies
  "isElite": "difficulty >= 2.5",                                // Elite mode at lower difficulty
  "weatherBonus": "isThundering ? 2.0 : (isRaining ? 1.5 : 1.0)" // Higher weather scaling
}
```

#### **🎛️ Step 4: Enable/Disable & Version Control**
- **Enable**: Keep the `.json` extension  
- **Disable**: Rename to `.json.disabled` or set `"enabled": false`
- **Version Control**: Use descriptive filenames like `skeleton_army_v2_hardcore.json`
- **Testing**: Create `.json.test` versions for experimentation

## 🎉 **Expression System: Unlimited Possibilities**

**🚀 Your Random Encounters mod has been TRANSFORMED!** With the new expression-powered system, you can create encounters that rival AAA game scaling systems—all through JSON configuration!

### **🎯 What Server Admins Can Now Achieve**

**Intelligent Difficulty Scaling:**
```json
"armySize": "Math.max(5, Math.min(50, Math.floor(baseSize + difficulty * 8)))"
```
Perfect balance from easy to nightmare difficulty.

**Weather-Reactive Encounters:**
```json
"condition": "isThundering && isNight",
"message": "The storm empowers ancient bones..."
```
Storms make encounters more intense and atmospheric.

**Distance-Based Exploration Rewards:**
```json
"lootMultiplier": "distanceFromSpawn > 10000 ? 3.0 : 1.0",
"customName": "distanceFromSpawn > 10000 ? '§4§lLegendary Warlord' : '§6Captain'"
```
Better rewards for brave explorers.

**Player-Level Progression Matching:**
```json
"encounterTier": "playerLevel < 10 ? 'beginner' : (playerLevel < 30 ? 'veteran' : 'master')",
"equipment": "encounterTier == 'master' ? 'diamond_gear' : 'iron_gear'"
```
Encounters that grow with player skill.

**Group Scaling for Multiplayer:**
```json
"challengeLevel": "Math.floor(nearbyPlayers * difficulty * 1.5)",
"reinforcements": "nearbyPlayers >= 4 ? 'call_backup_army' : 'standard_spawns'"
```
Perfect balance for solo and group play.

### **📘 For Complete Server Admin Mastery**

**→ [SERVER_ADMIN_EXPRESSION_GUIDE.md](SERVER_ADMIN_EXPRESSION_GUIDE.md) ←**

This comprehensive guide includes:
- ✅ **All available variables and math functions**
- ✅ **Advanced scaling formulas with examples**  
- ✅ **Weather/time-based encounter patterns**
- ✅ **Copy-paste templates for common scenarios**
- ✅ **Seasonal event configurations**
- ✅ **Server branding and customization**

### **🎮 The Result: Living, Intelligent Encounters**

Your players will experience:
- **Perfect difficulty curves** that never feel too easy or impossible
- **Weather that matters** - storms become epic encounter opportunities
- **Exploration rewards** that make distant travel worthwhile
- **Group challenges** that scale appropriately with party size  
- **Unique encounters** that feel different every time

### **🏆 Technical Achievement Unlocked**

You've successfully transformed a basic encounter mod into:
- ⚡ **Mathematical scaling engine**
- 🌩️ **Weather-reactive encounter system**  
- 📍 **Distance-based challenge scaling**
- 👥 **Multiplayer-aware balance system**
- 🎭 **Conditional logic framework**
- 🗡️ **Smart enchantment scaling**

**No other Minecraft encounter mod offers this level of dynamic, expression-powered customization!**

---

### 4. Complete Encounter Example (Legacy - Basic JSON)

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
      "message": "\u00A77Hello there, traveler!",
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
          "message": "\u00A7eThe villager has left you a gift!"
        }
      ]
    }
  ],
  "onTimeout": [
    {
      "type": "broadcast",
      "scope": "nearbyPlayers",
      "radius": 32,
      "message": "\u00A77The villager waves goodbye and disappears."
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

### 5. File Organization Best Practices

#### **Individual Files Approach:**
```
config/randomencounters/
├── blocks.json                    # Shared components
└── encounters/                    # Encounter files
    ├── combat/                    # Optional subdirectories
    │   ├── bandit_ambush.json
    │   └── monster_horde.json
    ├── friendly/
    │   ├── dog_walker.json
    │   ├── merchant.json
    │   └── skeletal_companion.json
    ├── seasonal/
    │   ├── halloween_event.json
    │   └── winter_festival.json
    └── custom_encounter.json
```

#### **Naming Conventions:**
- **Descriptive Names**: `traveling_merchant.json`, `bandit_ambush.json`
- **Consistent Format**: Use lowercase with underscores
- **Category Prefixes**: `combat_bandit_raid.json`, `friendly_village_helper.json`

#### **File Management:**
- **Disable Encounters**: Rename `.json` → `.json.disabled`
- **Temporary Disable**: Set `"enabled": false` in the file
- **Version Control**: Individual files are much easier to track changes
- **Collaboration**: Multiple people can work on different encounters simultaneously

### 6. Available Behavior Types

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
- **CRITICAL**: Use `\u00A7` instead of `\u00A7` for Minecraft color codes in Java source

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
      "message": "\u00A7c⚔ A bandit camp has been spotted nearby!"
    },
    {
      "type": "sayAboveHead",
      "target": "bandit_leader",
      "message": "\u00A74You picked the wrong area, stranger!",
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
          "message": "\u00A7aThe bandit leader has fallen! The camp scatters!"
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

## Legacy: Java Interface Reference

For developers who need to understand the underlying system, the mod uses the `IEncounter` interface internally. However, **all encounters should be created using JSON configuration files** rather than implementing this interface directly.

### The IEncounter Interface

The `IEncounter` interface is implemented by `JsonEncounter` which handles all JSON-based encounters:

```java
public interface IEncounter {
    void execute(EntityPlayer player, double difficulty);
    String getName();
    boolean canExecute(EntityPlayer player);
}
```

### Why JSON-Only?

1. **Server Control**: Administrators can modify encounters without recompiling
2. **No Java Knowledge Required**: Create complex encounters using JSON
3. **Expression System**: Dynamic scaling with mathematical expressions
4. **Hot Reloading**: Changes can be loaded without server restarts
5. **Modular Design**: Each encounter is a separate file

### Important Notes

- The old hardcoded encounter classes (`MobEncounter`, `LootEncounter`, etc.) are deprecated
- All encounters are loaded from `config/randomencounters/encounters/` directory
- Use `/encounters types` to see loaded encounters
- Use `/encounters test <encounter_id>` to test specific encounters
- The expression system allows complex logic without Java coding

This guide provides comprehensive coverage of JSON-based encounter creation using the powerful expression system. Start with simple encounters and gradually build complexity as you become familiar with the system!
