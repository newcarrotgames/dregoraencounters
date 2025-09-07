# 🚀 **Server Admin Expression Guide**
## **Create Infinitely Customizable Encounters with Math & Logic!**

Your Random Encounters mod now has **EXPRESSION PARSING POWER**! Server admins can create encounters that dynamically scale with difficulty, player levels, time of day, weather, and more—all through JSON configuration.

## 🎯 **What This Means for Server Admins**

**Before:** Limited to hardcoded encounter behavior  
**Now:** Complete control over encounter scaling, rewards, and conditions using mathematical expressions!

### **Real Examples from Your Mod:**

```json
"armySize": "Math.max(4, Math.min(20, Math.floor(4 + difficulty * 3)))"
"isElite": "difficulty >= 3"
"bonusLoot": "Math.floor(difficulty * 5)"
"experienceMultiplier": "isElite ? 2.0 : 1.0"
```

## 📊 **Available Variables**

Your encounters can access these dynamic variables:

| Variable | Description | Example Values |
|----------|-------------|----------------|
| `difficulty` | Server difficulty multiplier | 1.0, 2.5, 4.0 |
| `playerLevel` | Player's experience level | 1, 15, 50 |
| `nearbyPlayers` | Players within 50 blocks | 1, 3, 8 |
| `timeOfDay` | Minecraft time (0-24000) | 12000 (noon), 18000 (midnight) |
| `distanceFromSpawn` | Distance from world spawn | 100, 5000, 15000 |
| `isRaining` | Is it raining? | 0 (false) or 1 (true) |
| `isThundering` | Is it storming? | 0 (false) or 1 (true) |
| `isDay` | Daytime? (1000-13000) | 0 (false) or 1 (true) |
| `isNight` | Nighttime? | 0 (false) or 1 (true) |

## 🧮 **Math Functions**

Create complex scaling with these functions:

```json
"Math.max(a, b)"     // Maximum of two values
"Math.min(a, b)"     // Minimum of two values  
"Math.floor(x)"      // Round down
"Math.ceil(x)"       // Round up
"Math.round(x)"      // Round to nearest
```

## 🎮 **Real Server Admin Examples**

### **1. Difficulty-Scaling Army**
```json
{
  "variables": {
    "armySize": "Math.max(6, Math.min(30, Math.floor(6 + difficulty * 4)))",
    "eliteChance": "difficulty >= 3 ? 0.4 : 0.1",
    "commanderHealth": "30 + Math.floor(difficulty * 15)"
  }
}
```

**Result:** 
- Easy difficulty (1.0): 10 soldiers, 10% elite, commander has 45 HP
- Hard difficulty (3.0): 18 soldiers, 40% elite, commander has 75 HP
- Nightmare difficulty (5.0): 26 soldiers, 40% elite, commander has 105 HP

### **2. Time & Weather Based Encounters**
```json
{
  "spawn": [{
    "count": "isNight && isRaining ? Math.floor(difficulty * 6) : Math.floor(difficulty * 3)",
    "condition": "isNight || (isRaining && difficulty >= 2)"
  }]
}
```

**Result:** Double spawns during stormy nights, rain spawns only on higher difficulties!

### **3. Player-Level Scaling Rewards**
```json
{
  "rewards": [{
    "item": "minecraft:diamond",
    "min": "Math.max(1, Math.floor(playerLevel / 10))",
    "max": "Math.max(2, Math.floor(playerLevel / 5))",
    "condition": "playerLevel >= 20 && difficulty >= 2"
  }]
}
```

**Result:** Level 50 players get 5-10 diamonds, but only if level 20+ on difficulty 2+!

### **4. Distance-Based Elite Spawns**
```json
{
  "customName": "distanceFromSpawn > 10000 ? '§4§lLegendary Warlord' : '§6Skeleton Captain'",
  "equipment": {
    "mainhand": {
      "item": "distanceFromSpawn > 10000 ? 'minecraft:diamond_sword' : 'minecraft:iron_sword'",
      "enchantments": [
        {"id": "sharpness", "level": "Math.min(5, 1 + Math.floor(distanceFromSpawn / 5000))"}
      ]
    }
  }
}
```

**Result:** Skeletons near spawn get iron swords, far explorers face diamond-wielding legends!

## 🏆 **Advanced Server Scenarios**

### **Apocalypse Mode** (Difficulty 6+)
```json
{
  "condition": "difficulty >= 6 && isThundering",
  "variables": {
    "apocalypseMultiplier": "Math.min(10, difficulty * 2)",
    "hordeSize": "Math.floor(50 * apocalypseMultiplier)"
  },
  "message": "§4§l>>> APOCALYPSE MODE ACTIVATED <<<"
}
```

### **Newbie Protection** (Low levels get help)
```json
{
  "condition": "playerLevel <= 10",
  "variables": {
    "helpfulSkeletons": "Math.max(3, 8 - playerLevel)",
    "protectionBonus": "playerLevel <= 5 ? 2.0 : 1.5"
  }
}
```

### **Group Raid Scaling** (More players = bigger challenges)
```json
{
  "variables": {
    "raidSize": "Math.floor(nearbyPlayers * difficulty * 8)",
    "leaderCount": "Math.max(1, Math.floor(nearbyPlayers / 2))",
    "groupBonus": "nearbyPlayers >= 4 ? 'epic_loot_table' : 'standard_loot_table'"
  }
}
```

## 🎨 **Creative Conditional Logic**

### **Seasonal Events**
```json
{
  "condition": "timeOfDay >= 18000 && timeOfDay <= 6000 && !isRaining",
  "message": "§d§lMIDNIGHT SKELETON BALL - They're dancing instead of fighting!"
}
```

### **Weather-Reactive Behavior**
```json
{
  "attributes": {
    "movementSpeed": "isRaining ? 1.3 : 1.0",
    "attackDamage": "isThundering ? Math.floor(8 + difficulty * 4) : Math.floor(6 + difficulty * 2)"
  },
  "message": "isThundering ? '§c§lThe storm empowers them!' : '§aFriendly skeletons approach calmly.'"
}
```

## 📁 **Your Custom Encounter Template**

Copy this template and customize it for your server:

```json
{
  "id": "enc_my_server_custom",
  "enabled": true,
  "weight": 10,
  "description": "My server's custom encounter - edit this!",
  
  "variables": {
    "myArmySize": "Math.max(5, Math.floor(difficulty * 6))",
    "myDifficulty": "difficulty >= 3 ? 'HARD' : 'NORMAL'",
    "myRewards": "Math.floor(playerLevel * difficulty * 10)",
    "customCondition": "isNight && difficulty >= 2"
  },

  "spawn": [{
    "count": "myArmySize",
    "customName": "§a[YOUR SERVER] Skeleton Guardian",
    "condition": "customCondition",
    "attributes": {
      "health": "20 + Math.floor(difficulty * 10)",
      "attackDamage": "8 + Math.floor(difficulty * 3)"
    },
    "equipment": {
      "mainhand": {
        "item": "difficulty >= 4 ? 'minecraft:diamond_sword' : 'minecraft:iron_sword'",
        "enchantments": [
          {"id": "sharpness", "level": "Math.min(5, Math.floor(difficulty * 1.5))"}
        ]
      }
    }
  }],

  "onStart": [
    {
      "type": "broadcast",
      "message": "§6[YOUR SERVER] Custom encounter! Difficulty: {myDifficulty} | Army: {myArmySize}"
    }
  ]
}
```

## 🎯 **Pro Tips for Server Admins**

### **1. Test Your Expressions**
Use the in-game command: `/encounters test my_custom_encounter`

### **2. Balance Difficulty Curves**
```json
// Gentle scaling
"gentleArmy": "Math.floor(3 + difficulty * 2)"

// Exponential scaling  
"exponentialArmy": "Math.floor(3 * Math.pow(difficulty, 1.5))"

// Capped scaling
"cappedArmy": "Math.min(50, Math.floor(5 + difficulty * 10))"
```

### **3. Create Server Identity**
```json
"customName": "§b[MYSERVER] §aFriendly Guardian",
"message": "§6Welcome to MyServer! These skeletons will protect you!"
```

### **4. Seasonal/Event Configurations**
Create multiple encounter files and enable/disable them for special events:
- `halloween_spooky_army.json`
- `christmas_friendly_helpers.json`  
- `summer_beach_skeletons.json`

## 🚀 **The Result**

Your server now has **unlimited encounter customization power**! Players will experience:

✅ **Dynamic scaling** that matches their skill level  
✅ **Weather-reactive** encounters that feel alive  
✅ **Distance-based** challenges that reward exploration  
✅ **Group scaling** for multiplayer balance  
✅ **Custom server branding** in all encounters  
✅ **Seasonal variety** with easy config swaps  

## 🎉 **You Are Now an Encounter Wizard!**

Your Random Encounters mod has transformed from a simple skeleton spawner into a **full expression-powered encounter engine**. Every server admin can now create unique, balanced, and engaging experiences that scale perfectly with their server's difficulty and player base.

**No more hardcoded limits. No more static encounters. Welcome to dynamic, expression-powered adventure!**
