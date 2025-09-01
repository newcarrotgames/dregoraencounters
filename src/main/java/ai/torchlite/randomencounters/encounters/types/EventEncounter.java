package ai.torchlite.randomencounters.encounters.types;

import ai.torchlite.randomencounters.encounters.IEncounter;
import ai.torchlite.randomencounters.config.ConfigHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraft.world.storage.WorldInfo;
import java.util.Random;

public class EventEncounter implements IEncounter {
    
    private final Random random = new Random();
    
    @Override
    public void execute(EntityPlayer player, double difficulty) {
        // Select random event type
        EventType eventType = EventType.values()[random.nextInt(EventType.values().length)];
        
        // Execute the event
        switch (eventType) {
            case WEATHER_CHANGE:
                executeWeatherEvent(player, difficulty);
                break;
            case BENEFICIAL_EFFECT:
                executeBeneficialEffect(player, difficulty);
                break;
            case TEMPORARY_CHALLENGE:
                executeTemporaryChallenge(player, difficulty);
                break;
            case MYSTERIOUS_EVENT:
                executeMysteriousEvent(player, difficulty);
                break;
        }
        
        // Award experience if enabled
        if (ConfigHandler.enableExperienceRewards) {
            int experience = (int)(ConfigHandler.baseExperienceReward * difficulty * 0.3);
            player.addExperience(experience);
            player.sendMessage(new TextComponentString(
                TextFormatting.AQUA + "+" + experience + " experience"));
        }
    }
    
    private void executeWeatherEvent(EntityPlayer player, double difficulty) {
        World world = player.world;
        WorldInfo worldInfo = world.getWorldInfo();
        
        if (random.nextBoolean()) {
            // Clear weather
            worldInfo.setRaining(false);
            worldInfo.setThundering(false);
            player.sendMessage(new TextComponentString(
                TextFormatting.YELLOW + "The skies clear as if by magic..."));
        } else {
            // Storm
            worldInfo.setRaining(true);
            if (difficulty > 2.0) {
                worldInfo.setThundering(true);
                player.sendMessage(new TextComponentString(
                    TextFormatting.DARK_GRAY + "Dark storm clouds gather overhead..."));
            } else {
                player.sendMessage(new TextComponentString(
                    TextFormatting.GRAY + "Rain begins to fall..."));
            }
        }
    }
    
    private void executeBeneficialEffect(EntityPlayer player, double difficulty) {
        // Duration based on difficulty (longer for higher difficulty)
        int duration = (int)(60 + (difficulty * 30)); // 60-150 seconds
        
        switch (random.nextInt(5)) {
            case 0:
                player.addPotionEffect(new PotionEffect(MobEffects.SPEED, duration * 20, 0));
                player.sendMessage(new TextComponentString(
                    TextFormatting.GREEN + "You feel swift as the wind!"));
                break;
            case 1:
                player.addPotionEffect(new PotionEffect(MobEffects.STRENGTH, duration * 20, 0));
                player.sendMessage(new TextComponentString(
                    TextFormatting.RED + "You feel strength coursing through your veins!"));
                break;
            case 2:
                player.addPotionEffect(new PotionEffect(MobEffects.REGENERATION, duration * 20, 0));
                player.sendMessage(new TextComponentString(
                    TextFormatting.LIGHT_PURPLE + "You feel your wounds healing!"));
                break;
            case 3:
                player.addPotionEffect(new PotionEffect(MobEffects.LUCK, duration * 20, 0));
                player.sendMessage(new TextComponentString(
                    TextFormatting.GOLD + "You feel unusually fortunate!"));
                break;
            default:
                player.addPotionEffect(new PotionEffect(MobEffects.NIGHT_VISION, duration * 20, 0));
                player.sendMessage(new TextComponentString(
                    TextFormatting.BLUE + "Your vision becomes crystal clear!"));
                break;
        }
    }
    
    private void executeTemporaryChallenge(EntityPlayer player, double difficulty) {
        // Shorter duration for negative effects
        int duration = (int)(30 + (difficulty * 15)); // 30-75 seconds
        
        switch (random.nextInt(4)) {
            case 0:
                player.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, duration * 20, 0));
                player.sendMessage(new TextComponentString(
                    TextFormatting.GRAY + "You feel sluggish..."));
                break;
            case 1:
                player.addPotionEffect(new PotionEffect(MobEffects.HUNGER, duration * 20, 0));
                player.sendMessage(new TextComponentString(
                    TextFormatting.DARK_RED + "You suddenly feel very hungry..."));
                break;
            case 2:
                player.addPotionEffect(new PotionEffect(MobEffects.BLINDNESS, (duration / 2) * 20, 0));
                player.sendMessage(new TextComponentString(
                    TextFormatting.BLACK + "Darkness clouds your vision..."));
                break;
            default:
                player.addPotionEffect(new PotionEffect(MobEffects.WEAKNESS, duration * 20, 0));
                player.sendMessage(new TextComponentString(
                    TextFormatting.DARK_GRAY + "You feel weakened..."));
                break;
        }
    }
    
    private void executeMysteriousEvent(EntityPlayer player, double difficulty) {
        switch (random.nextInt(4)) {
            case 0:
                // Heal player
                player.heal(player.getMaxHealth() * 0.5f);
                player.sendMessage(new TextComponentString(
                    TextFormatting.LIGHT_PURPLE + "A warm light envelops you, healing your wounds..."));
                break;
            case 1:
                // Restore hunger
                player.getFoodStats().addStats(10, 1.0f);
                player.sendMessage(new TextComponentString(
                    TextFormatting.GREEN + "You feel mysteriously nourished..."));
                break;
            case 2:
                // Small experience boost
                int experience = (int)(20 * difficulty);
                player.addExperience(experience);
                player.sendMessage(new TextComponentString(
                    TextFormatting.AQUA + "Ancient knowledge flows into your mind... (+" + experience + " XP)"));
                break;
            default:
                // Brief invulnerability
                player.addPotionEffect(new PotionEffect(MobEffects.ABSORPTION, 300, 1));
                player.sendMessage(new TextComponentString(
                    TextFormatting.YELLOW + "A protective aura surrounds you..."));
                break;
        }
    }
    
    @Override
    public String getName() {
        return "Event Encounter";
    }
    
    @Override
    public boolean canExecute(EntityPlayer player) {
        // Can always execute event encounters
        return true;
    }
    
    private enum EventType {
        WEATHER_CHANGE,
        BENEFICIAL_EFFECT,
        TEMPORARY_CHALLENGE,
        MYSTERIOUS_EVENT
    }
}
