package ai.torchlite.randomencounters.encounters.types;

import ai.torchlite.randomencounters.encounters.IEncounter;
import ai.torchlite.randomencounters.config.ConfigHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import java.util.Random;

public class LootEncounter implements IEncounter {
    
    private final Random random = new Random();
    
    @Override
    public void execute(EntityPlayer player, double difficulty) {
        World world = player.world;
        BlockPos playerPos = player.getPosition();
        
        // Send discovery message
        player.sendMessage(new TextComponentString(
            TextFormatting.GREEN + "You discovered something interesting..."));
        
        // Calculate loot quality based on difficulty
        int lootCount = Math.max(1, (int)(difficulty * 1.5));
        lootCount = Math.min(lootCount, 4); // Cap at 4 items
        
        for (int i = 0; i < lootCount; i++) {
            spawnLootItem(world, playerPos, difficulty);
        }
        
        // Send loot message
        String message = lootCount == 1 ? 
            "You found a valuable item!" : 
            "You found " + lootCount + " valuable items!";
        player.sendMessage(new TextComponentString(
            TextFormatting.GOLD + message));
        
        // Award experience if enabled
        if (ConfigHandler.enableExperienceRewards) {
            int experience = (int)(ConfigHandler.baseExperienceReward * difficulty * 0.5);
            player.addExperience(experience);
            player.sendMessage(new TextComponentString(
                TextFormatting.AQUA + "+" + experience + " experience"));
        }
    }
    
    private void spawnLootItem(World world, BlockPos playerPos, double difficulty) {
        // Find a spawn location near the player
        BlockPos spawnPos = findLootLocation(world, playerPos);
        
        // Create loot item based on difficulty
        ItemStack lootItem = createLootItem(difficulty);
        
        // Spawn the item
        EntityItem entityItem = new EntityItem(world, 
            spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5, 
            lootItem);
        
        // Add some upward motion for visual effect
        entityItem.motionY = 0.2;
        
        world.spawnEntity(entityItem);
    }
    
    private BlockPos findLootLocation(World world, BlockPos playerPos) {
        // Try to find a valid location within 5 blocks of the player
        for (int attempts = 0; attempts < 10; attempts++) {
            int x = playerPos.getX() + (random.nextInt(10) - 5);
            int z = playerPos.getZ() + (random.nextInt(10) - 5);
            
            // Find the surface
            for (int y = playerPos.getY() + 3; y > playerPos.getY() - 3; y--) {
                BlockPos checkPos = new BlockPos(x, y, z);
                BlockPos belowPos = checkPos.down();
                
                // Check if it's a valid location
                if (!world.isAirBlock(belowPos) && world.isAirBlock(checkPos)) {
                    return checkPos;
                }
            }
        }
        
        // Fallback to player position
        return playerPos.up();
    }
    
    private ItemStack createLootItem(double difficulty) {
        ItemStack item;
        
        if (difficulty < 1.5) {
            // Basic loot
            switch (random.nextInt(5)) {
                case 0: item = new ItemStack(Items.IRON_INGOT, 1 + random.nextInt(3)); break;
                case 1: item = new ItemStack(Items.GOLD_INGOT, 1 + random.nextInt(2)); break;
                case 2: item = new ItemStack(Items.COAL, 3 + random.nextInt(5)); break;
                case 3: item = new ItemStack(Items.BREAD, 2 + random.nextInt(4)); break;
                default: item = new ItemStack(Items.ARROW, 5 + random.nextInt(10)); break;
            }
        } else if (difficulty < 3.0) {
            // Medium loot
            switch (random.nextInt(6)) {
                case 0: item = new ItemStack(Items.DIAMOND, 1); break;
                case 1: item = new ItemStack(Items.EMERALD, 1 + random.nextInt(2)); break;
                case 2: item = new ItemStack(Items.ENCHANTED_BOOK); break;
                case 3: item = new ItemStack(Items.ENDER_PEARL, 1 + random.nextInt(2)); break;
                case 4: item = new ItemStack(Items.BLAZE_ROD, 1 + random.nextInt(2)); break;
                default: item = new ItemStack(Items.GOLD_INGOT, 3 + random.nextInt(3)); break;
            }
        } else {
            // Rare loot
            switch (random.nextInt(5)) {
                case 0: item = new ItemStack(Items.DIAMOND, 2 + random.nextInt(2)); break;
                case 1: item = new ItemStack(Items.EMERALD, 3 + random.nextInt(3)); break;
                case 2: item = new ItemStack(Items.NETHER_STAR); break;
                case 3: item = new ItemStack(Items.DRAGON_BREATH, 1 + random.nextInt(2)); break;
                default: item = new ItemStack(Items.ENCHANTED_BOOK); break;
            }
        }
        
        return item;
    }
    
    @Override
    public String getName() {
        return "Loot Encounter";
    }
    
    @Override
    public boolean canExecute(EntityPlayer player) {
        // Can always execute loot encounters
        return true;
    }
}
