# JSON Conversion Roadmap
## Moving All Encounters to JSON Format

### 🎯 **Goal**: Make all encounters JSON-based for maximum server admin flexibility

---

## 📊 **Current Status**

### ✅ **Hardcoded Encounters (Java)**
- `MobEncounter` - Random creature spawning
- `LootEncounter` - Treasure chest drops  
- `EventEncounter` - Environmental events
- `NPCEncounter` - NPC interactions
- `FriendlySkeletonEncounter` - Skeletal companions
- `SkeletonArmyEncounter` - Epic army formation

### 📝 **JSON Encounters (Data-Driven)**
- `dog_walker.json` - Walking encounter example
- `traveling_merchant.json` - Trading encounter
- `simple_mob_encounter.json` - Converted MobEncounter
- `advanced_friendly_skeleton.json` - Enhanced skeleton encounter

---

## 🔧 **Required JSON Schema Extensions**

To convert all hardcoded encounters to JSON, we need these new features:

### **1. Variables & Expressions**
```json
"variables": {
  "difficultyScaling": true,
  "mobCount": "Math.max(1, Math.floor(difficulty * 2))",
  "healthMultiplier": "1.0 + (difficulty - 1.0) * 0.5"
}
```

### **2. Conditional Logic** 
```json
"conditionalSpawns": [
  {
    "condition": "difficulty < 1.5",
    "spawn": [{"ref": "harmless_animals"}]
  },
  {
    "condition": "difficulty >= 3.0", 
    "spawn": [{"ref": "elite_mobs"}]
  }
]
```

### **3. Equipment Enchantments**
```json
"equipment": {
  "mainhand": { 
    "item": "minecraft:bow", 
    "enchantments": [
      {"id": "power", "level": 2},
      {"id": "punch", "level": 1}
    ]
  }
}
```

### **4. Attribute Scaling**
```json
"attributes": {
  "health": { "multiply": 1.5, "add": 10 },
  "attackDamage": { "scale": "difficulty * 0.3" },
  "followRange": 20.0
}
```

### **5. Advanced AI Behaviors**
```json
"behaviors": [
  {
    "type": "friendlyGuardian",
    "targets": ["hostile", "!skeleton", "!player"],
    "radius": 12,
    "retaliate": true,
    "customAI": [
      {"add": "EntityAIHurtByTarget", "condition": "!instanceof EntityPlayer"},
      {"add": "EntityAINearestAttackableTarget<EntityMob>"},
      {"remove": "EntityAIAttackMelee"}
    ]
  }
]
```

### **6. Entity Mounting**
```json
"spawn": [
  {
    "label": "horse",
    "ref": "skeleton_horse",
    "at": {"mode": "nearPlayer", "radius": {"min": 10, "max": 15}}
  },
  {
    "label": "rider", 
    "ref": "skeleton_cavalry",
    "at": {"mode": "mountOn", "target": "horse"}
  }
]
```

### **7. Dynamic Text Generation**
```json
"messages": {
  "dynamic": true,
  "templates": [
    {"condition": "mobCount == 1", "text": "\u00A7aA mysterious visitor appears!"},
    {"condition": "mobCount > 1", "text": "\u00A7a{mobCount} unexpected guests arrive!"}
  ]
}
```

### **8. Probability & Random Choices**
```json
"randomActions": [
  {"weight": 30, "action": {"type": "sayAboveHead", "message": "Hello there!"}},
  {"weight": 20, "action": {"type": "giveLoot", "items": ["bone", "arrow"]}},
  {"weight": 10, "action": {"type": "spawn", "ref": "bonus_mob"}}
]
```

---

## 🛠️ **Implementation Strategy**

### **Phase 1: Schema Extensions**
1. **Add Expression Parser** - Support for `difficulty * 2`, `Math.max(1, x)`, etc.
2. **Conditional Blocks** - If/then logic for spawns, actions, messages
3. **Equipment Enhancement** - Enchantment support, durability, custom names
4. **Attribute System** - Health, damage, speed scaling
5. **Advanced AI Builder** - Custom AI task management

### **Phase 2: Encounter Migration** 
1. **Simple First**: `MobEncounter` → JSON (✅ Done as example)
2. **Medium Complexity**: `LootEncounter`, `EventEncounter` 
3. **Advanced**: `FriendlySkeletonEncounter` → JSON (✅ Partially done)
4. **Most Complex**: `SkeletonArmyEncounter` → JSON

### **Phase 3: Legacy Support**
1. **Backward Compatibility** - Keep hardcoded encounters as fallbacks
2. **Migration Tools** - Auto-convert simple encounters 
3. **Validation System** - Ensure JSON encounters work correctly

---

## 💡 **Benefits of Full JSON Conversion**

### **For Server Admins** 🎛️
- **Customize Everything**: Mob counts, equipment, messages, spawn conditions
- **Create Variants**: "Hard Mode Skeleton Army", "Peaceful Farm Animals"  
- **Share Encounters**: Community-driven encounter packs
- **No Recompilation**: Modify encounters without coding knowledge
- **Version Independent**: JSON encounters work across mod updates

### **For Developers** 👨‍💻
- **Easier Balancing**: Tweak numbers in JSON vs recompiling
- **Community Contributions**: Players can submit new encounters
- **A/B Testing**: Easy to test different encounter variants
- **Debugging**: JSON encounters are easier to troubleshoot

### **Example: Server Admin Customization**
```json
// Admin wants "Hardcore Skeleton Army" 
{
  "id": "enc_hardcore_skeleton_army",
  "weight": 2,
  "variables": {
    "armySize": "Math.min(50, difficulty * 8)",
    "commanderCount": 3,
    "cavalryPercent": 0.4
  },
  "spawn": [
    {"label": "commander1", "ref": "skeleton_commander_diamond"},
    {"label": "commander2", "ref": "skeleton_commander_diamond"},
    {"label": "commander3", "ref": "skeleton_commander_diamond"}
  ],
  "onStart": [
    {"type": "broadcast", "message": "\u00A7c\u00A7lTHE HARDCORE SKELETON LEGION ARRIVES!"}
  ]
}
```

---

## 🚀 **Getting Started**

### **Immediate Action Items**:
1. **Implement Expression Parser** for variables and scaling
2. **Add Enchantment Support** to equipment system  
3. **Create Conditional Logic** for spawn/action blocks
4. **Migrate Simple Encounters** as examples (MobEncounter ✅ done)

### **Testing Strategy**:
1. **JSON Schema Validation** - Ensure encounters parse correctly
2. **In-Game Testing** - Verify encounters work as expected  
3. **Performance Testing** - Ensure JSON parsing doesn't hurt server performance
4. **Admin Feedback** - Get real server admin input on flexibility needs

---

## 📝 **Next Steps**

Would you like me to:
1. **Implement the expression parser** for variables/scaling?
2. **Add enchantment support** to the equipment system?
3. **Convert more hardcoded encounters** to JSON format?
4. **Create admin tools** for encounter testing/validation?

The goal is to make your mod the most flexible and admin-friendly encounter system possible! 🎯

