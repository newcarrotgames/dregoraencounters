# Random Encounters Mod

A Minecraft mod that adds dynamic random encounters to enhance gameplay in RLCraft Dregora servers.

## Features

- **Server-Side Compatible**: Clients without the mod can connect without issues
- **Configurable Encounters**: Multiple types of encounters with extensive configuration options
- **Mob Encounters**: Random hostile creatures that spawn near players
- **Loot Encounters**: Discover valuable items scattered throughout the world
- **Event Encounters**: Weather changes, beneficial/negative effects, and mysterious events
- **Smart Cooldowns**: Prevents encounter spam with configurable cooldowns and daily limits
- **Difficulty Scaling**: Encounters scale with player level and distance from spawn

## Installation

1. Download the mod JAR file
2. Place it in your server's `mods` folder
3. Start the server
4. Configuration file will be generated in `config/randomencounters.cfg`

## Configuration

The mod creates a configuration file with the following categories:

### General Settings
- `enableRandomEncounters`: Enable/disable the mod globally
- `baseEncounterChance`: Percentage chance per minute (1-100)
- `encounterCooldown`: Seconds between encounters per player
- `maxEncountersPerDay`: Maximum encounters per player per day

### Distance Settings
- `minDistanceFromSpawn`: Minimum distance from world spawn for encounters
- `minDistanceBetweenPlayers`: Minimum distance between players for individual encounters

### Encounter Types
- `enableMobEncounters`: Enable random mob spawning encounters
- `enableLootEncounters`: Enable random loot discovery encounters
- `enableEventEncounters`: Enable random world events
- `enableNPCEncounters`: Enable NPC encounters (disabled by default for compatibility)

### Difficulty Scaling
- `scaleWithPlayerLevel`: Scale difficulty with player experience level
- `scaleWithDistance`: Scale difficulty with distance from spawn
- `difficultyMultiplier`: Global difficulty multiplier

### Rewards
- `enableExperienceRewards`: Enable experience rewards for encounters
- `enableLootRewards`: Enable loot rewards for encounters
- `baseExperienceReward`: Base experience points awarded

## Commands

- `/encounters` - Show mod version and basic info
- `/encounters help` - Show all available commands
- `/encounters status` - Show your encounter status (encounters today, cooldown, etc.)
- `/encounters cooldown` - Show remaining cooldown time
- `/encounters trigger` - Force trigger an encounter (OP only)
- `/encounters config` - Show current configuration values
- `/encounters reload` - Reload configuration (OP only)

## Building

This mod is built with ForgeGradle for Minecraft 1.12.2 with Forge 14.23.5.2768.

Requirements:
- Java 8
- Gradle 4.10.3

To build:
```bash
./gradlew build
```

The compiled JAR will be in `build/libs/`

## Compatibility

- **Minecraft Version**: 1.12.2
- **Forge Version**: 14.23.5.2768
- **RLCraft Dregora**: Fully compatible
- **Server-Side Only**: Yes, clients don't need the mod installed

## Technical Details

The mod uses server-side only features to ensure maximum compatibility:
- No client-side rendering or GUIs
- No custom blocks, items, or entities
- No custom models or textures
- Uses vanilla Minecraft entities and items
- All encounters use server-side logic and vanilla mechanics

## License

This mod is created for RLCraft Dregora server use.
