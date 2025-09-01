package ai.torchlite.randomencounters.encounters.types;

import ai.torchlite.randomencounters.encounters.IEncounter;
import ai.torchlite.randomencounters.config.ConfigHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;

public class NPCEncounter implements IEncounter {
    
    @Override
    public void execute(EntityPlayer player, double difficulty) {
        // NPC encounters are disabled by default for server compatibility
        // This is a placeholder implementation that just sends a message
        
        player.sendMessage(new TextComponentString(
            TextFormatting.GRAY + "You sense a presence nearby, but see nothing..."));
        
        // Could implement server-side only NPC interactions here
        // Such as trading, quests, or dialogue through chat
        
        // Award experience if enabled
        if (ConfigHandler.enableExperienceRewards) {
            int experience = (int)(ConfigHandler.baseExperienceReward * difficulty * 0.4);
            player.addExperience(experience);
            player.sendMessage(new TextComponentString(
                TextFormatting.AQUA + "+" + experience + " experience"));
        }
    }
    
    @Override
    public String getName() {
        return "NPC Encounter";
    }
    
    @Override
    public boolean canExecute(EntityPlayer player) {
        // Only execute if NPC encounters are enabled in config
        // They are disabled by default for server compatibility
        return ConfigHandler.enableNPCEncounters;
    }
}
