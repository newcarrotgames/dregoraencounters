package ai.torchlite.randomencounters.encounters.types;

import ai.torchlite.randomencounters.encounters.IEncounter;
import ai.torchlite.randomencounters.config.ConfigHandler;
import ai.torchlite.randomencounters.hologram.HologramSpeech;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.monster.EntitySlime;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.ai.EntityAIAttackRangedBow;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAIWander;
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
        TextFormatting.WHITE + "7Hello there, living friend!",
        TextFormatting.WHITE + "7Don't be afraid, I'll protect you.",
        TextFormatting.WHITE + "7I'll follow and guard you on your journey.",
        TextFormatting.WHITE + "7Together we can face any danger!",
        TextFormatting.WHITE + "7Take this gift from your skeletal companion!",
        TextFormatting.WHITE + "7I'll watch your back against monsters.",
        TextFormatting.WHITE + "7Lead the way, I'll follow and defend!",
        TextFormatting.WHITE + "7Your guardian is here to stay!"
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
            message = "A friendly guardian skeleton offers to accompany and protect you!";
        } else {
            message = skeletonCount + " guardian skeletons arrive to follow and defend you!";
        }
        
        player.sendMessage(new TextComponentString(
            TextFormatting.GREEN + message));
        
        // Award experience if enabled
        if (ConfigHandler.enableExperienceRewards) {
            int experience = (int)(ConfigHandler.baseExperienceReward * difficulty * 0.3);
            player.addExperience(experience);
            player.sendMessage(new TextComponentString(
                TextFormatting.AQUA + "+" + experience + " experience (Guardian companion)"));
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
        // Clear all existing target tasks first
        skeleton.targetTasks.taskEntries.clear();
        
        // Make skeleton persistent so it doesn't despawn immediately
        skeleton.enablePersistence();
        
        // Set health to be a bit higher since it's a special encounter
        skeleton.setHealth(skeleton.getMaxHealth() * 1.2f);
        
        // Add defensive AI - hurt by target (but not players)
        skeleton.targetTasks.addTask(1, new EntityAIHurtByTarget(skeleton, false) {
            @Override
            public boolean shouldExecute() {
                // Only retaliate if hurt by non-player entities
                return super.shouldExecute() && !(skeleton.getRevengeTarget() instanceof EntityPlayer);
            }
        });
        
        // Add AI to target hostile mobs
        skeleton.targetTasks.addTask(2, new EntityAINearestAttackableTarget<EntityMob>(skeleton, EntityMob.class, true) {
            @Override
            public boolean shouldExecute() {
                // Don't target other skeletons or friendly entities
                return super.shouldExecute() && !(this.targetEntity instanceof EntitySkeleton);
            }
        });
        
        // Add AI to target slimes (they don't extend EntityMob)
        skeleton.targetTasks.addTask(3, new EntityAINearestAttackableTarget<EntitySlime>(skeleton, EntitySlime.class, true));
        
        // Ensure skeleton can still attack with bow (re-add if removed)
        boolean hasAttackAI = false;
        for (net.minecraft.entity.ai.EntityAITasks.EntityAITaskEntry entry : skeleton.tasks.taskEntries) {
            if (entry.action instanceof EntityAIAttackRangedBow) {
                hasAttackAI = true;
                break;
            }
        }
        
        if (!hasAttackAI) {
            skeleton.tasks.addTask(4, new EntityAIAttackRangedBow<EntitySkeleton>(skeleton, 1.0D, 20, 15.0F));
        }
        
        // Add following behavior - TODO: Implement player following AI
        // skeleton.tasks.addTask(5, new EntityAIFollowPlayer(skeleton, 1.0D, 6.0F, 20.0F));
        
        // Add wandering when no player is nearby (lower priority)
        skeleton.tasks.addTask(8, new EntityAIWander(skeleton, 0.8D));
    }
    
    private void configureLeaderSkeleton(EntitySkeleton skeleton, EntityPlayer player) {
        // Give leader skeleton a bow so it can defend itself and others
        skeleton.setItemStackToSlot(net.minecraft.inventory.EntityEquipmentSlot.MAINHAND, 
            new ItemStack(Items.BOW)); // Bow for defending against hostile mobs
        
        // Leader skeleton gets better equipment drop chances
        skeleton.setDropChance(net.minecraft.inventory.EntityEquipmentSlot.MAINHAND, 0.1f);
        
        // Set custom name
        skeleton.setCustomNameTag(TextFormatting.WHITE + "aSkeletal Companion");
        skeleton.setAlwaysRenderNameTag(true);
    }
    
    private void configureFollowerSkeleton(EntitySkeleton skeleton) {
        // Give followers bows so they can also defend against hostile mobs
        skeleton.setItemStackToSlot(net.minecraft.inventory.EntityEquipmentSlot.MAINHAND, 
            new ItemStack(Items.BOW));
        
        // Give followers arrows in offhand for better combat
        skeleton.setItemStackToSlot(net.minecraft.inventory.EntityEquipmentSlot.OFFHAND, 
            new ItemStack(Items.ARROW, 16));
        
        // Set modest drop chance for their equipment
        skeleton.setDropChance(net.minecraft.inventory.EntityEquipmentSlot.MAINHAND, 0.05f);
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
            // Leader gives better combat-related gifts
            switch (random.nextInt(6)) {
                case 0: return new ItemStack(Items.BONE, 2 + random.nextInt(3)); // Still bones (skeleton theme)
                case 1: return new ItemStack(Items.ARROW, 16 + random.nextInt(16)); // More arrows for combat
                case 2: return new ItemStack(Items.BOW, 1); // Spare bow
                case 3: return new ItemStack(Items.IRON_INGOT, 2 + random.nextInt(2)); // Iron for armor/weapons
                case 4: return new ItemStack(Items.LEATHER, 2 + random.nextInt(3)); // Leather for armor
                default: return new ItemStack(Items.BREAD, 3 + random.nextInt(3)); // Food for health
            }
        } else {
            // Followers give modest combat supplies
            switch (random.nextInt(5)) {
                case 0: return new ItemStack(Items.BONE, 1 + random.nextInt(2));
                case 1: return new ItemStack(Items.ARROW, 8 + random.nextInt(8)); // More arrows
                case 2: return new ItemStack(Items.STRING, 2 + random.nextInt(3)); // String for bows
                case 3: return new ItemStack(Items.COAL, 2 + random.nextInt(3)); // Coal for torches
                default: return new ItemStack(Items.BREAD, 2 + random.nextInt(2)); // Food
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
        return "Skeletal Companion Encounter";
    }
    
    @Override
    public boolean canExecute(EntityPlayer player) {
        // Can execute if friendly skeletons are enabled
        return ConfigHandler.enableFriendlySkeletonEncounters;
    }
    
    // TODO: Custom AI task for skeletons to follow the nearest player
    // Implementation temporarily disabled for compatibility
    /*
    private static class EntityAIFollowPlayer extends EntityAIBase {
        // Following AI implementation goes here
        // Will be implemented in future version
    }
    */
}
