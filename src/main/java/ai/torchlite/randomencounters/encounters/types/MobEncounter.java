package ai.torchlite.randomencounters.encounters.types;

import ai.torchlite.randomencounters.encounters.IEncounter;
import ai.torchlite.randomencounters.config.ConfigHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.monster.*;
import net.minecraft.entity.passive.*;
import net.minecraft.entity.EntityLiving;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import java.util.Random;

public class MobEncounter implements IEncounter {
    
    private final Random random = new Random();
    
    @Override
    public void execute(EntityPlayer player, double difficulty) {
        World world = player.world;
        BlockPos playerPos = player.getPosition();
        
        // Calculate number of mobs based on difficulty
        int mobCount = Math.max(1, (int)(difficulty * 2));
        mobCount = Math.min(mobCount, 5); // Cap at 5 mobs
        
        // Send comedic warning message to player
        player.sendMessage(new TextComponentString(
            TextFormatting.GOLD + "You hear strange noises in the distance..."));
        
        for (int i = 0; i < mobCount; i++) {
            spawnRandomMob(world, playerPos, difficulty);
        }
        
        // Send encounter message
        String message = mobCount == 1 ? 
            "A mysterious visitor has appeared!" : 
            mobCount + " unexpected guests have arrived!";
        player.sendMessage(new TextComponentString(
            TextFormatting.GREEN + message));
    }
    
    private void spawnRandomMob(World world, BlockPos playerPos, double difficulty) {
        // Find a safe spawn location near the player
        BlockPos spawnPos = findSpawnLocation(world, playerPos);
        if (spawnPos == null) {
            return;
        }
        
        // Select random mob type
        EntityLiving mob = createRandomMob(world, difficulty);
        if (mob == null) {
            return;
        }
        
        // Set spawn position
        mob.setPosition(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5);
        
        // Scale mob based on difficulty
        scaleMob(mob, difficulty);
        
        // Spawn the mob
        world.spawnEntity(mob);
    }
    
    private BlockPos findSpawnLocation(World world, BlockPos playerPos) {
        // Try to find a valid spawn location within 10-20 blocks of the player
        for (int attempts = 0; attempts < 20; attempts++) {
            int x = playerPos.getX() + (random.nextInt(20) - 10);
            int z = playerPos.getZ() + (random.nextInt(20) - 10);
            
            // Find the surface
            for (int y = playerPos.getY() + 5; y > playerPos.getY() - 10; y--) {
                BlockPos checkPos = new BlockPos(x, y, z);
                BlockPos belowPos = checkPos.down();
                
                // Check if it's a valid spawn location
                if (!world.isAirBlock(belowPos) && 
                    world.isAirBlock(checkPos) && 
                    world.isAirBlock(checkPos.up())) {
                    return checkPos;
                }
            }
        }
        
        // Fallback to player position if no good location found
        return playerPos.add(5, 0, 5);
    }
    
    private EntityLiving createRandomMob(World world, double difficulty) {
        // Select mob type for comedic encounters
        EntityLiving mob;
        
        if (difficulty < 1.5) {
            // Harmless farm animals for comic relief
            switch (random.nextInt(5)) {
                case 0: mob = new EntityChicken(world); break;
                case 1: mob = new EntityPig(world); break;
                case 2: mob = new EntityCow(world); break;
                case 3: mob = new EntitySheep(world); break;
                default: mob = new EntityBat(world); break;
            }
        } else if (difficulty < 3.0) {
            // Mix of harmless animals and mild threats
            switch (random.nextInt(6)) {
                case 0: mob = new EntityChicken(world); break;
                case 1: mob = new EntityPig(world); break;
                case 2: mob = new EntityZombie(world); break;
                case 3: mob = new EntitySkeleton(world); break;
                case 4: mob = new EntityHorse(world); break;
                default: mob = new EntityRabbit(world); break;
            }
        } else {
            // "Elite" comedy encounters - more farm animals!
            switch (random.nextInt(5)) {
                case 0: mob = new EntityCow(world); break;
                case 1: mob = new EntitySheep(world); break;
                case 2: mob = new EntityHorse(world); break;
                case 3: mob = new EntityLlama(world); break;
                default: mob = new EntityZombie(world); break; // Keep one mild threat
            }
        }
        
        return mob;
    }
    
    private void scaleMob(EntityLiving mob, double difficulty) {
        // Increase mob health based on difficulty
        float healthMultiplier = (float)(1.0 + (difficulty - 1.0) * 0.5);
        mob.setHealth(mob.getMaxHealth() * healthMultiplier);
        
        // Add equipment for humanoid mobs based on difficulty
        if (mob instanceof EntityZombie || mob instanceof EntitySkeleton) {
            // Could add armor/weapons here based on difficulty
        }
    }
    
    @Override
    public String getName() {
        return "Surprise Visitor Encounter";
    }
    
    @Override
    public boolean canExecute(EntityPlayer player) {
        // Can always execute mob encounters
        return true;
    }
}
