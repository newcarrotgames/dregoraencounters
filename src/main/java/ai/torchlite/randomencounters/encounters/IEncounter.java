package ai.torchlite.randomencounters.encounters;

import net.minecraft.entity.player.EntityPlayer;

public interface IEncounter {
    
    /**
     * Execute the encounter for the given player
     * @param player The player experiencing the encounter
     * @param difficulty The calculated difficulty multiplier for this encounter
     */
    void execute(EntityPlayer player, double difficulty);
    
    /**
     * Get the name/type of this encounter
     * @return The encounter name
     */
    String getName();
    
    /**
     * Check if this encounter can be executed for the given player
     * @param player The player to check
     * @return true if the encounter can be executed
     */
    boolean canExecute(EntityPlayer player);
}
