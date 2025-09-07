package ai.torchlite.randomencounters.encounters.types;

import ai.torchlite.randomencounters.encounters.IEncounter;
import ai.torchlite.randomencounters.config.ConfigHandler;
import ai.torchlite.randomencounters.hologram.HologramSpeech;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.monster.EntitySlime;
import net.minecraft.entity.passive.EntitySkeletonHorse;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.ai.EntityAIAttackRangedBow;
import net.minecraft.entity.ai.EntityAIAttackMelee;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.init.Items;
import net.minecraft.init.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import java.util.Random;
import java.util.ArrayList;
import java.util.List;

public class SkeletonArmyEncounter implements IEncounter {
    
    private final Random random = new Random();
    
    // Epic army arrival messages
    private final String[] armyMessages = {
        TextFormatting.WHITE + "Behold! The Skeleton Legion arrives to aid you!",
        TextFormatting.WHITE + "An ancient army awakens to fight by your side!",
        TextFormatting.WHITE + "The Bone Guard has come to your assistance!",
        TextFormatting.WHITE + "Skeletal warriors emerge from the shadows to help!",
        TextFormatting.WHITE + "The Undead Alliance marches to your defense!"
    };
    
    // Commander speech
    private final String[] commanderMessages = {
        "\u00A76FOR THE LIVING! CHARGE!",
        "\u00A76Stand firm, warriors! Protect our ally!",
        "\u00A76Today we fight as one - bone and flesh united!",
        "\u00A76Let our enemies tremble before our might!"
    };
    
    @Override
    public void execute(EntityPlayer player, double difficulty) {
        World world = player.world;
        BlockPos playerPos = player.getPosition();
        
        // Epic announcement
        player.sendMessage(new TextComponentString(
            TextFormatting.GOLD + "The ground trembles beneath an approaching army..."));
        
        // Calculate army size based on difficulty (bigger army for higher difficulty)
        int baseUnits = Math.max(8, (int)(difficulty * 4)); // 8-20+ units
        int maxUnits = Math.min(baseUnits, 25); // Cap at 25 for performance
        
        // Army composition
        int commanderCount = 1;
        int cavalryCount = Math.max(2, maxUnits / 6); // ~15% cavalry
        int archerCount = Math.max(3, maxUnits / 3); // ~33% archers  
        int warriorCount = Math.max(2, maxUnits / 4); // ~25% warriors
        int infantryCount = maxUnits - commanderCount - cavalryCount - archerCount - warriorCount;
        
        List<EntitySkeleton> army = new ArrayList<>();
        
        // Spawn the army
        try {
            // 1. Spawn Commander (most important)
            EntitySkeleton commander = spawnCommander(world, playerPos);
            if (commander != null) {
                army.add(commander);
                if (world instanceof WorldServer) {
                    String message = commanderMessages[random.nextInt(commanderMessages.length)];
                    HologramSpeech.spawnHologram((WorldServer) world, commander, message, 200); // 10 seconds
                }
            }
            
            // 2. Spawn Cavalry (mounted units)
            for (int i = 0; i < cavalryCount; i++) {
                EntitySkeleton cavalry = spawnCavalry(world, playerPos);
                if (cavalry != null) {
                    army.add(cavalry);
                }
            }
            
            // 3. Spawn Archers (ranged support)
            for (int i = 0; i < archerCount; i++) {
                EntitySkeleton archer = spawnArcher(world, playerPos);
                if (archer != null) {
                    army.add(archer);
                }
            }
            
            // 4. Spawn Warriors (melee fighters)
            for (int i = 0; i < warriorCount; i++) {
                EntitySkeleton warrior = spawnWarrior(world, playerPos);
                if (warrior != null) {
                    army.add(warrior);
                }
            }
            
            // 5. Spawn Infantry (basic units)
            for (int i = 0; i < infantryCount; i++) {
                EntitySkeleton infantry = spawnInfantry(world, playerPos);
                if (infantry != null) {
                    army.add(infantry);
                }
            }
            
        } catch (Exception e) {
            System.err.println("RandomEncounters: Error spawning skeleton army: " + e.getMessage());
            e.printStackTrace();
        }
        
        // Epic arrival message
        String arrivalMessage = armyMessages[random.nextInt(armyMessages.length)];
        player.sendMessage(new TextComponentString(TextFormatting.GOLD + arrivalMessage));
        player.sendMessage(new TextComponentString(
            TextFormatting.GREEN + "A mighty skeleton army of " + army.size() + " warriors has arrived!"));
        
        // Award substantial experience for this epic encounter
        if (ConfigHandler.enableExperienceRewards) {
            int experience = (int)(ConfigHandler.baseExperienceReward * difficulty * 2.0); // Double XP!
            player.addExperience(experience);
            player.sendMessage(new TextComponentString(
                TextFormatting.AQUA + "+" + experience + " experience (Epic Skeleton Army)"));
        }
        
        // Drop some epic loot supplies
        if (random.nextFloat() < 0.8f) { // 80% chance for army supplies
            dropArmySupplies(world, playerPos);
        }
    }
    
    private EntitySkeleton spawnCommander(World world, BlockPos playerPos) {
        BlockPos spawnPos = findSpawnLocation(world, playerPos, 5, 8);
        if (spawnPos == null) return null;
        
        EntitySkeleton commander = new EntitySkeleton(world);
        commander.setPosition(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5);
        
        // Make friendly and configure AI
        makeFriendlyWarrior(commander);
        
        // Commander gets the best equipment
        equipCommander(commander);
        
        // Set custom name
        commander.setCustomNameTag("\u00A76\u00A7lSkeleton Commander");
        commander.setAlwaysRenderNameTag(true);
        
        world.spawnEntity(commander);
        return commander;
    }
    
    private EntitySkeleton spawnCavalry(World world, BlockPos playerPos) {
        BlockPos spawnPos = findSpawnLocation(world, playerPos, 8, 15);
        if (spawnPos == null) return null;
        
        // First spawn the skeleton horse
        EntitySkeletonHorse horse = new EntitySkeletonHorse(world);
        horse.setPosition(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5);
        horse.setHorseTamed(true);
        horse.setHorseSaddled(true);
        
        // Make horse friendly (remove targeting)
        horse.targetTasks.taskEntries.clear();
        
        world.spawnEntity(horse);
        
        // Then spawn the skeleton rider
        EntitySkeleton rider = new EntitySkeleton(world);
        rider.setPosition(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5);
        
        // Make friendly and configure AI
        makeFriendlyArcher(rider); // Cavalry uses bows primarily
        
        // Equip cavalry
        equipCavalry(rider);
        
        // Set custom name
        rider.setCustomNameTag("\u00A7aSkeleton Cavalry");
        rider.setAlwaysRenderNameTag(true);
        
        world.spawnEntity(rider);
        
        // Mount the skeleton on the horse
        rider.startRiding(horse);
        
        return rider;
    }
    
    private EntitySkeleton spawnArcher(World world, BlockPos playerPos) {
        BlockPos spawnPos = findSpawnLocation(world, playerPos, 6, 12);
        if (spawnPos == null) return null;
        
        EntitySkeleton archer = new EntitySkeleton(world);
        archer.setPosition(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5);
        
        // Make friendly and configure AI
        makeFriendlyArcher(archer);
        
        // Equip archer
        equipArcher(archer);
        
        // Set custom name
        archer.setCustomNameTag("\u00A7aSkeleton Archer");
        archer.setAlwaysRenderNameTag(true);
        
        world.spawnEntity(archer);
        return archer;
    }
    
    private EntitySkeleton spawnWarrior(World world, BlockPos playerPos) {
        BlockPos spawnPos = findSpawnLocation(world, playerPos, 6, 12);
        if (spawnPos == null) return null;
        
        EntitySkeleton warrior = new EntitySkeleton(world);
        warrior.setPosition(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5);
        
        // Make friendly and configure AI for melee combat
        makeFriendlyWarrior(warrior);
        
        // Equip warrior
        equipWarrior(warrior);
        
        // Set custom name
        warrior.setCustomNameTag("\u00A7aSkeleton Warrior");
        warrior.setAlwaysRenderNameTag(true);
        
        world.spawnEntity(warrior);
        return warrior;
    }
    
    private EntitySkeleton spawnInfantry(World world, BlockPos playerPos) {
        BlockPos spawnPos = findSpawnLocation(world, playerPos, 6, 12);
        if (spawnPos == null) return null;
        
        EntitySkeleton infantry = new EntitySkeleton(world);
        infantry.setPosition(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5);
        
        // Make friendly - mix of archers and warriors
        if (random.nextBoolean()) {
            makeFriendlyArcher(infantry);
            equipBasicArcher(infantry);
        } else {
            makeFriendlyWarrior(infantry);
            equipBasicWarrior(infantry);
        }
        
        // Set custom name
        infantry.setCustomNameTag("\u00A7aSkeleton Soldier");
        infantry.setAlwaysRenderNameTag(true);
        
        world.spawnEntity(infantry);
        return infantry;
    }
    
    private void makeFriendlyArcher(EntitySkeleton skeleton) {
        // Clear existing AI
        skeleton.targetTasks.taskEntries.clear();
        
        // Make persistent
        skeleton.enablePersistence();
        
        // Enhanced health for army units
        skeleton.setHealth(skeleton.getMaxHealth() * 1.5f);
        
        // Add defensive AI
        skeleton.targetTasks.addTask(1, new EntityAIHurtByTarget(skeleton, false) {
            @Override
            public boolean shouldExecute() {
                return super.shouldExecute() && !(skeleton.getRevengeTarget() instanceof EntityPlayer);
            }
        });
        
        // Target hostile mobs
        skeleton.targetTasks.addTask(2, new EntityAINearestAttackableTarget<EntityMob>(skeleton, EntityMob.class, true) {
            @Override
            public boolean shouldExecute() {
                return super.shouldExecute() && !(this.targetEntity instanceof EntitySkeleton);
            }
        });
        
        skeleton.targetTasks.addTask(3, new EntityAINearestAttackableTarget<EntitySlime>(skeleton, EntitySlime.class, true));
        
        // Ensure ranged combat AI
        skeleton.tasks.addTask(4, new EntityAIAttackRangedBow<EntitySkeleton>(skeleton, 1.0D, 20, 20.0F));
        skeleton.tasks.addTask(8, new EntityAIWander(skeleton, 0.6D));
    }
    
    private void makeFriendlyWarrior(EntitySkeleton skeleton) {
        // Clear existing AI  
        skeleton.targetTasks.taskEntries.clear();
        skeleton.tasks.taskEntries.clear(); // Clear all tasks for warriors
        
        // Make persistent
        skeleton.enablePersistence();
        
        // Enhanced health for army units
        skeleton.setHealth(skeleton.getMaxHealth() * 1.8f); // Warriors get more health
        
        // Add defensive AI
        skeleton.targetTasks.addTask(1, new EntityAIHurtByTarget(skeleton, false) {
            @Override
            public boolean shouldExecute() {
                return super.shouldExecute() && !(skeleton.getRevengeTarget() instanceof EntityPlayer);
            }
        });
        
        // Target hostile mobs
        skeleton.targetTasks.addTask(2, new EntityAINearestAttackableTarget<EntityMob>(skeleton, EntityMob.class, true) {
            @Override
            public boolean shouldExecute() {
                return super.shouldExecute() && !(this.targetEntity instanceof EntitySkeleton);
            }
        });
        
        skeleton.targetTasks.addTask(3, new EntityAINearestAttackableTarget<EntitySlime>(skeleton, EntitySlime.class, true));
        
        // Melee combat AI - skeletons CAN use swords for melee!
        skeleton.tasks.addTask(2, new EntityAIAttackMelee(skeleton, 1.2D, false));
        skeleton.tasks.addTask(8, new EntityAIWander(skeleton, 0.8D));
    }
    
    private void equipCommander(EntitySkeleton commander) {
        // Diamond armor set
        commander.setItemStackToSlot(net.minecraft.inventory.EntityEquipmentSlot.HEAD, 
            createEnchantedArmor(Items.DIAMOND_HELMET));
        commander.setItemStackToSlot(net.minecraft.inventory.EntityEquipmentSlot.CHEST, 
            createEnchantedArmor(Items.DIAMOND_CHESTPLATE));
        commander.setItemStackToSlot(net.minecraft.inventory.EntityEquipmentSlot.LEGS, 
            createEnchantedArmor(Items.DIAMOND_LEGGINGS));
        commander.setItemStackToSlot(net.minecraft.inventory.EntityEquipmentSlot.FEET, 
            createEnchantedArmor(Items.DIAMOND_BOOTS));
        
        // Enchanted diamond sword
        commander.setItemStackToSlot(net.minecraft.inventory.EntityEquipmentSlot.MAINHAND, 
            createEnchantedSword(Items.DIAMOND_SWORD));
        
        // Set drop chances (low but possible)
        commander.setDropChance(net.minecraft.inventory.EntityEquipmentSlot.MAINHAND, 0.05f);
        commander.setDropChance(net.minecraft.inventory.EntityEquipmentSlot.HEAD, 0.02f);
    }
    
    private void equipCavalry(EntitySkeleton cavalry) {
        // Iron armor
        cavalry.setItemStackToSlot(net.minecraft.inventory.EntityEquipmentSlot.HEAD, 
            new ItemStack(Items.IRON_HELMET));
        cavalry.setItemStackToSlot(net.minecraft.inventory.EntityEquipmentSlot.CHEST, 
            new ItemStack(Items.IRON_CHESTPLATE));
        cavalry.setItemStackToSlot(net.minecraft.inventory.EntityEquipmentSlot.LEGS, 
            new ItemStack(Items.IRON_LEGGINGS));
        cavalry.setItemStackToSlot(net.minecraft.inventory.EntityEquipmentSlot.FEET, 
            new ItemStack(Items.IRON_BOOTS));
        
        // Enchanted bow for mounted archery
        cavalry.setItemStackToSlot(net.minecraft.inventory.EntityEquipmentSlot.MAINHAND, 
            createEnchantedBow());
        
        cavalry.setDropChance(net.minecraft.inventory.EntityEquipmentSlot.MAINHAND, 0.05f);
    }
    
    private void equipArcher(EntitySkeleton archer) {
        // Leather armor (mobility for archers)
        archer.setItemStackToSlot(net.minecraft.inventory.EntityEquipmentSlot.HEAD, 
            new ItemStack(Items.LEATHER_HELMET));
        archer.setItemStackToSlot(net.minecraft.inventory.EntityEquipmentSlot.CHEST, 
            new ItemStack(Items.LEATHER_CHESTPLATE));
        
        // Enchanted bow
        archer.setItemStackToSlot(net.minecraft.inventory.EntityEquipmentSlot.MAINHAND, 
            createEnchantedBow());
        
        // Arrows in offhand
        archer.setItemStackToSlot(net.minecraft.inventory.EntityEquipmentSlot.OFFHAND, 
            new ItemStack(Items.ARROW, 32));
        
        archer.setDropChance(net.minecraft.inventory.EntityEquipmentSlot.MAINHAND, 0.08f);
    }
    
    private void equipWarrior(EntitySkeleton warrior) {
        // Iron armor
        warrior.setItemStackToSlot(net.minecraft.inventory.EntityEquipmentSlot.HEAD, 
            new ItemStack(Items.IRON_HELMET));
        warrior.setItemStackToSlot(net.minecraft.inventory.EntityEquipmentSlot.CHEST, 
            new ItemStack(Items.IRON_CHESTPLATE));
        warrior.setItemStackToSlot(net.minecraft.inventory.EntityEquipmentSlot.LEGS, 
            new ItemStack(Items.IRON_LEGGINGS));
        
        // Enchanted iron sword
        warrior.setItemStackToSlot(net.minecraft.inventory.EntityEquipmentSlot.MAINHAND, 
            createEnchantedSword(Items.IRON_SWORD));
        
        // Shield in offhand
        warrior.setItemStackToSlot(net.minecraft.inventory.EntityEquipmentSlot.OFFHAND, 
            new ItemStack(Items.SHIELD));
        
        warrior.setDropChance(net.minecraft.inventory.EntityEquipmentSlot.MAINHAND, 0.08f);
    }
    
    private void equipBasicArcher(EntitySkeleton archer) {
        // Chain armor
        archer.setItemStackToSlot(net.minecraft.inventory.EntityEquipmentSlot.CHEST, 
            new ItemStack(Items.CHAINMAIL_CHESTPLATE));
        
        // Regular bow
        archer.setItemStackToSlot(net.minecraft.inventory.EntityEquipmentSlot.MAINHAND, 
            new ItemStack(Items.BOW));
        
        archer.setDropChance(net.minecraft.inventory.EntityEquipmentSlot.MAINHAND, 0.1f);
    }
    
    private void equipBasicWarrior(EntitySkeleton warrior) {
        // Chain armor
        warrior.setItemStackToSlot(net.minecraft.inventory.EntityEquipmentSlot.CHEST, 
            new ItemStack(Items.CHAINMAIL_CHESTPLATE));
        
        // Iron sword
        warrior.setItemStackToSlot(net.minecraft.inventory.EntityEquipmentSlot.MAINHAND, 
            new ItemStack(Items.IRON_SWORD));
        
        warrior.setDropChance(net.minecraft.inventory.EntityEquipmentSlot.MAINHAND, 0.1f);
    }
    
    private ItemStack createEnchantedBow() {
        ItemStack bow = new ItemStack(Items.BOW);
        
        // Add random enchantments
        switch (random.nextInt(3)) {
            case 0:
                EnchantmentHelper.setEnchantments(
                    com.google.common.collect.ImmutableMap.of(Enchantments.POWER, 2), bow);
                break;
            case 1:
                EnchantmentHelper.setEnchantments(
                    com.google.common.collect.ImmutableMap.of(Enchantments.PUNCH, 1), bow);
                break;
            case 2:
                EnchantmentHelper.setEnchantments(
                    com.google.common.collect.ImmutableMap.of(Enchantments.FLAME, 1), bow);
                break;
        }
        
        return bow;
    }
    
    private ItemStack createEnchantedSword(net.minecraft.item.Item swordType) {
        ItemStack sword = new ItemStack(swordType);
        
        // Add random combat enchantments
        switch (random.nextInt(3)) {
            case 0:
                EnchantmentHelper.setEnchantments(
                    com.google.common.collect.ImmutableMap.of(Enchantments.SHARPNESS, 2), sword);
                break;
            case 1:
                EnchantmentHelper.setEnchantments(
                    com.google.common.collect.ImmutableMap.of(Enchantments.KNOCKBACK, 1), sword);
                break;
            case 2:
                EnchantmentHelper.setEnchantments(
                    com.google.common.collect.ImmutableMap.of(Enchantments.FIRE_ASPECT, 1), sword);
                break;
        }
        
        return sword;
    }
    
    private ItemStack createEnchantedArmor(net.minecraft.item.Item armorType) {
        ItemStack armor = new ItemStack(armorType);
        
        // Add protection enchantment
        EnchantmentHelper.setEnchantments(
            com.google.common.collect.ImmutableMap.of(Enchantments.PROTECTION, 2), armor);
        
        return armor;
    }
    
    private void dropArmySupplies(World world, BlockPos pos) {
        // Epic loot drops for army encounter
        List<ItemStack> supplies = new ArrayList<>();
        
        supplies.add(new ItemStack(Items.ARROW, 32 + random.nextInt(32)));
        supplies.add(new ItemStack(Items.BONE, 8 + random.nextInt(12)));
        supplies.add(new ItemStack(Items.IRON_INGOT, 5 + random.nextInt(10)));
        supplies.add(new ItemStack(Items.GOLD_INGOT, 3 + random.nextInt(6)));
        
        if (random.nextFloat() < 0.3f) {
            supplies.add(new ItemStack(Items.DIAMOND, 1 + random.nextInt(3)));
        }
        
        if (random.nextFloat() < 0.4f) {
            supplies.add(new ItemStack(Items.ENCHANTED_BOOK));
        }
        
        // Drop supplies around the area
        for (ItemStack supply : supplies) {
            BlockPos dropPos = pos.add(
                random.nextInt(10) - 5, 
                1, 
                random.nextInt(10) - 5
            );
            
            EntityItem item = new EntityItem(world, 
                dropPos.getX() + 0.5, dropPos.getY(), dropPos.getZ() + 0.5, 
                supply);
            item.motionY = 0.3;
            world.spawnEntity(item);
        }
    }
    
    private BlockPos findSpawnLocation(World world, BlockPos playerPos, int minRange, int maxRange) {
        // Try to find a valid spawn location
        for (int attempts = 0; attempts < 30; attempts++) {
            int range = minRange + random.nextInt(maxRange - minRange + 1);
            double angle = random.nextDouble() * 2 * Math.PI;
            
            int x = playerPos.getX() + (int)(Math.cos(angle) * range);
            int z = playerPos.getZ() + (int)(Math.sin(angle) * range);
            
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
        
        // Fallback to near player
        return playerPos.add(minRange + random.nextInt(5), 0, minRange + random.nextInt(5));
    }
    
    @Override
    public String getName() {
        return "Epic Skeleton Army Encounter";
    }
    
    @Override
    public boolean canExecute(EntityPlayer player) {
        return ConfigHandler.enableSkeletonArmyEncounters;
    }
}
