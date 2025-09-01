package ai.torchlite.randomencounters.encounters.types;

import ai.torchlite.randomencounters.encounters.IEncounter;
import ai.torchlite.randomencounters.config.ConfigHandler;
import ai.torchlite.randomencounters.hologram.HologramSpeech;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import java.util.Random;

public class FriendlySkeletonEncounter implements IEncounter {
    
    private final Random random = new Random();
    
    // Friendly messages the skeleton can say
    private final String[] friendlyMessages = {
        "§7Hello there, living friend!",
        "§7Don't be afraid, I mean no harm.",
        "§7Would you like to trade?",
        "§7I've been walking these lands peacefully.",
        "§7Take this gift from a friendly skeleton!",
        "§7The night doesn't have to be scary.",
        "§7I remember when I was like you...",
        "§7Peace between the living and undead!"
    };
    
    @Override
    public void execute(EntityPlayer player, double difficulty) {
        World world = player.world;
        BlockPos playerPos = player.getPosition();
        
        // Send atmospheric message
        player.sendMessage(new TextComponentString(
            TextFormatting.AQUA + "You hear the gentle rattle of bones nearby..."));
        
        // Calculate number of friendly skeletons based on difficulty
        int skeletonCount = Math.max(1, (int)(difficulty * 1.5));
        skeletonCount = Math.min(skeletonCount, 3); // Cap at 3 skeletons
        
        for (int i = 0; i < skeletonCount; i++) {
            spawnFriendlySkeleton(world, playerPos, player, i == 0); // First skeleton gets special treatment
        }
        
        // Send encounter message
        String message;
        if (skeletonCount == 1) {
            message = "A peaceful skeleton approaches you!";
        } else {
            message = skeletonCount + " friendly skeletons have gathered nearby!";
        }
        
        player.sendMessage(new TextComponentString(
            TextFormatting.GREEN + message));
        
        // Award experience if enabled
        if (ConfigHandler.enableExperienceRewards) {
            int experience = (int)(ConfigHandler.baseExperienceReward * difficulty * 0.3);
            player.addExperience(experience);
            player.sendMessage(new TextComponentString(
                TextFormatting.AQUA + "+" + experience + " experience (Peaceful encounter)"));
        }
    }
    
    private void spawnFriendlySkeleton(World world, BlockPos playerPos, EntityPlayer player, boolean isLeader) {
        // Find a safe spawn location
        BlockPos spawnPos = findSpawnLocation(world, playerPos);
        if (spawnPos == null) {
            return;
        }
        
        // Create the skeleton
        EntitySkeleton skeleton = new EntitySkeleton(world);
        skeleton.setPosition(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5);
        
        // Make skeleton friendly
        makeFriendly(skeleton);
        
        // Configure skeleton based on difficulty and role
        if (isLeader) {
            configureLeaderSkeleton(skeleton, player);
        } else {
            configureFollowerSkeleton(skeleton);
        }
        
        // Spawn the skeleton
        world.spawnEntity(skeleton);
        
        // Show a friendly message if it's the leader
        if (isLeader && world instanceof WorldServer) {
            showFriendlyMessage(skeleton, (WorldServer) world);
        }
        
        // Give the skeleton some items to drop as gifts immediately
        if (random.nextFloat() < 0.7f) { // 70% chance to drop gift
            dropGift(world, spawnPos, isLeader);
        }
    }
    
    private void makeFriendly(EntitySkeleton skeleton) {
        // Clear all target tasks to make skeleton non-hostile
        skeleton.targetTasks.taskEntries.clear();
        
        // Remove attack AI tasks
        skeleton.tasks.taskEntries.removeIf(entry -> 
            entry.action.getClass().getSimpleName().contains("Attack") ||
            entry.action.getClass().getSimpleName().contains("Bow"));
        
        // Make skeleton persistent so it doesn't despawn immediately
        skeleton.enablePersistence();
        
        // Set health to be a bit higher since it's a special encounter
        skeleton.setHealth(skeleton.getMaxHealth() * 1.2f);
    }
    
    private void configureLeaderSkeleton(EntitySkeleton skeleton, EntityPlayer player) {
        // Give leader skeleton some basic equipment
        skeleton.setItemStackToSlot(net.minecraft.inventory.EntityEquipmentSlot.MAINHAND, 
            new ItemStack(Items.STICK)); // Peaceful staff instead of bow
        
        // Leader skeleton gets better equipment drop chances
        skeleton.setDropChance(net.minecraft.inventory.EntityEquipmentSlot.MAINHAND, 0.1f);
        
        // Set custom name
        skeleton.setCustomNameTag("§aPeaceful Skeleton");
        skeleton.setAlwaysRenderNameTag(true);
    }
    
    private void configureFollowerSkeleton(EntitySkeleton skeleton) {
        // Followers get no weapons
        skeleton.setItemStackToSlot(net.minecraft.inventory.EntityEquipmentSlot.MAINHAND, 
            ItemStack.EMPTY);
        
        // Give followers a small chance to have flowers
        if (random.nextFloat() < 0.3f) {
            skeleton.setItemStackToSlot(net.minecraft.inventory.EntityEquipmentSlot.OFFHAND, 
                new ItemStack(Items.YELLOW_FLOWER));
        }
    }
    
    private void showFriendlyMessage(EntitySkeleton skeleton, WorldServer world) {
        // Use hologram speech system
        String message = friendlyMessages[random.nextInt(friendlyMessages.length)];
        HologramSpeech.spawnHologram(world, skeleton, message, 160); // 8 seconds (160 ticks)
    }
    
    private void dropGift(World world, BlockPos pos, boolean isLeader) {
        // Drop gift immediately near the skeleton
        ItemStack gift = createGift(isLeader);
        EntityItem giftItem = new EntityItem(world, 
            pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5, gift);
        giftItem.motionY = 0.3; // Nice upward motion
        giftItem.setDefaultPickupDelay(); // Normal pickup delay
        world.spawnEntity(giftItem);
    }
    
    private ItemStack createGift(boolean isLeader) {
        if (isLeader) {
            // Leader gives better gifts
            switch (random.nextInt(5)) {
                case 0: return new ItemStack(Items.BONE, 2 + random.nextInt(3));
                case 1: return new ItemStack(Items.ARROW, 8 + random.nextInt(8));
                case 2: return new ItemStack(Items.COAL, 3 + random.nextInt(4));
                case 3: return new ItemStack(Items.IRON_INGOT, 1 + random.nextInt(2));
                default: return new ItemStack(Items.BREAD, 2 + random.nextInt(3));
            }
        } else {
            // Followers give simpler gifts
            switch (random.nextInt(4)) {
                case 0: return new ItemStack(Items.BONE, 1 + random.nextInt(2));
                case 1: return new ItemStack(Items.ARROW, 3 + random.nextInt(5));
                case 2: return new ItemStack(Items.COAL, 1 + random.nextInt(3));
                default: return new ItemStack(Items.YELLOW_FLOWER, 1);
            }
        }
    }
    
    private BlockPos findSpawnLocation(World world, BlockPos playerPos) {
        // Try to find a valid spawn location within 8-16 blocks of the player
        for (int attempts = 0; attempts < 25; attempts++) {
            int x = playerPos.getX() + (random.nextInt(16) - 8);
            int z = playerPos.getZ() + (random.nextInt(16) - 8);
            
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
        
        // Fallback to near player position
        return playerPos.add(8, 0, 8);
    }
    
    @Override
    public String getName() {
        return "Friendly Skeleton Encounter";
    }
    
    @Override
    public boolean canExecute(EntityPlayer player) {
        // Can execute if friendly skeletons are enabled
        return ConfigHandler.enableFriendlySkeletonEncounters;
    }
}
