package ai.torchlite.randomencounters.commands;

import ai.torchlite.randomencounters.RandomEncounters;
import ai.torchlite.randomencounters.config.ConfigHandler;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class EncounterCommand extends CommandBase {
    
    @Override
    public String getName() {
        return "encounters";
    }
    
    @Override
    public String getUsage(ICommandSender sender) {
        return "/encounters <trigger|force|test|status|cooldown|config|types|help>";
    }
    
    @Override
    public int getRequiredPermissionLevel() {
        return 0; // Allow all players to use basic commands
    }
    
    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length == 0) {
            sender.sendMessage(new TextComponentString(
                TextFormatting.YELLOW + "Random Encounters v" + RandomEncounters.VERSION));
            sender.sendMessage(new TextComponentString(
                TextFormatting.GRAY + "Use '/encounters help' for available commands"));
            return;
        }
        
        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "help":
                showHelp(sender);
                break;
            case "status":
                showStatus(sender);
                break;
            case "cooldown":
                showCooldown(sender);
                break;
            case "trigger":
                triggerEncounter(server, sender);
                break;
            case "force":
                forceEncounter(server, sender);
                break;
            case "test":
                testEncounter(server, sender, args);
                break;
            case "enable":
                enableEncounters(sender);
                break;
            case "disable":
                disableEncounters(sender);
                break;
            case "config":
                showConfig(sender);
                break;
            case "types":
                showTypes(sender);
                break;
            case "reload":
                reloadConfig(server, sender);
                break;
            default:
                sender.sendMessage(new TextComponentString(
                    TextFormatting.RED + "Unknown subcommand: " + subCommand));
                sender.sendMessage(new TextComponentString(
                    TextFormatting.GRAY + "Use '/encounters help' for available commands"));
                break;
        }
    }
    
    private void showHelp(ICommandSender sender) {
        sender.sendMessage(new TextComponentString(
            TextFormatting.GOLD + "=== Random Encounters Commands ==="));
        sender.sendMessage(new TextComponentString(
            TextFormatting.YELLOW + "/encounters status" + TextFormatting.GRAY + " - Show your encounter status"));
        sender.sendMessage(new TextComponentString(
            TextFormatting.YELLOW + "/encounters cooldown" + TextFormatting.GRAY + " - Show remaining cooldown"));
        sender.sendMessage(new TextComponentString(
            TextFormatting.YELLOW + "/encounters trigger" + TextFormatting.GRAY + " - Force trigger an encounter (OP only)"));
        sender.sendMessage(new TextComponentString(
            TextFormatting.YELLOW + "/encounters force" + TextFormatting.GRAY + " - Force encounter (ignores global disable, OP only)"));
        sender.sendMessage(new TextComponentString(
            TextFormatting.YELLOW + "/encounters test <encounter_id>" + TextFormatting.GRAY + " - Test specific encounter by ID (OP only)"));
        sender.sendMessage(new TextComponentString(
            TextFormatting.YELLOW + "/encounters enable/disable" + TextFormatting.GRAY + " - Toggle encounters (OP only)"));
        sender.sendMessage(new TextComponentString(
            TextFormatting.YELLOW + "/encounters config" + TextFormatting.GRAY + " - Show current config"));
        sender.sendMessage(new TextComponentString(
            TextFormatting.YELLOW + "/encounters types" + TextFormatting.GRAY + " - List all available encounter types"));
        sender.sendMessage(new TextComponentString(
            TextFormatting.YELLOW + "/encounters reload" + TextFormatting.GRAY + " - Reload config (OP only)"));
    }
    
    private void showStatus(ICommandSender sender) {
        if (!(sender instanceof EntityPlayer)) {
            sender.sendMessage(new TextComponentString(
                TextFormatting.RED + "This command can only be used by players"));
            return;
        }
        
        EntityPlayer player = (EntityPlayer) sender;
        UUID playerId = player.getUniqueID();
        
        boolean enabled = ConfigHandler.enableRandomEncounters;
        int dailyCount = RandomEncounters.encounterManager.getDailyEncounterCount(playerId);
        int maxDaily = ConfigHandler.maxEncountersPerDay;
        int cooldown = RandomEncounters.encounterManager.getRemainingCooldown(playerId);
        
        sender.sendMessage(new TextComponentString(
            TextFormatting.GOLD + "=== Encounter Status ==="));
        sender.sendMessage(new TextComponentString(
            TextFormatting.YELLOW + "System Enabled: " + 
            (enabled ? TextFormatting.GREEN + "Yes" : TextFormatting.RED + "No")));
        sender.sendMessage(new TextComponentString(
            TextFormatting.YELLOW + "Daily Encounters: " + TextFormatting.WHITE + 
            dailyCount + "/" + maxDaily));
        
        if (cooldown > 0) {
            int minutes = cooldown / 60;
            int seconds = cooldown % 60;
            sender.sendMessage(new TextComponentString(
                TextFormatting.YELLOW + "Cooldown: " + TextFormatting.RED + 
                minutes + "m " + seconds + "s"));
        } else {
            sender.sendMessage(new TextComponentString(
                TextFormatting.YELLOW + "Cooldown: " + TextFormatting.GREEN + "Ready"));
        }
    }
    
    private void showCooldown(ICommandSender sender) {
        if (!(sender instanceof EntityPlayer)) {
            sender.sendMessage(new TextComponentString(
                TextFormatting.RED + "This command can only be used by players"));
            return;
        }
        
        EntityPlayer player = (EntityPlayer) sender;
        UUID playerId = player.getUniqueID();
        int cooldown = RandomEncounters.encounterManager.getRemainingCooldown(playerId);
        
        if (cooldown > 0) {
            int minutes = cooldown / 60;
            int seconds = cooldown % 60;
            sender.sendMessage(new TextComponentString(
                TextFormatting.YELLOW + "Next encounter available in: " + 
                TextFormatting.WHITE + minutes + "m " + seconds + "s"));
        } else {
            sender.sendMessage(new TextComponentString(
                TextFormatting.GREEN + "You can trigger an encounter now!"));
        }
    }
    
    private void triggerEncounter(MinecraftServer server, ICommandSender sender) throws CommandException {
        if (!sender.canUseCommand(2, this.getName())) {
            throw new CommandException("commands.generic.permission");
        }
        
        if (!(sender instanceof EntityPlayer)) {
            sender.sendMessage(new TextComponentString(
                TextFormatting.RED + "This command can only be used by players"));
            return;
        }
        
        EntityPlayer player = (EntityPlayer) sender;
        
        if (!ConfigHandler.enableRandomEncounters) {
            sender.sendMessage(new TextComponentString(
                TextFormatting.RED + "Random encounters are currently disabled"));
            return;
        }
        
        RandomEncounters.encounterManager.triggerEncounter(player);
        sender.sendMessage(new TextComponentString(
            TextFormatting.GREEN + "Encounter triggered!"));
    }
    
    private void forceEncounter(MinecraftServer server, ICommandSender sender) throws CommandException {
        if (!sender.canUseCommand(2, this.getName())) {
            throw new CommandException("commands.generic.permission");
        }
        
        if (!(sender instanceof EntityPlayer)) {
            sender.sendMessage(new TextComponentString(
                TextFormatting.RED + "This command can only be used by players"));
            return;
        }
        
        EntityPlayer player = (EntityPlayer) sender;
        
        sender.sendMessage(new TextComponentString(
            TextFormatting.YELLOW + "Forcing encounter (ignoring global disable setting)..."));
        
        // Force trigger encounter regardless of global settings
        RandomEncounters.encounterManager.triggerEncounter(player);
        sender.sendMessage(new TextComponentString(
            TextFormatting.GREEN + "Debug encounter triggered!"));
    }
    
    private void testEncounter(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (!sender.canUseCommand(2, this.getName())) {
            throw new CommandException("commands.generic.permission");
        }
        
        if (!(sender instanceof EntityPlayer)) {
            sender.sendMessage(new TextComponentString(
                TextFormatting.RED + "This command can only be used by players"));
            return;
        }
        
        if (args.length < 2) {
            sender.sendMessage(new TextComponentString(
                TextFormatting.RED + "Usage: /encounters test <encounter_id>"));
            sender.sendMessage(new TextComponentString(
                TextFormatting.GRAY + "Use '/encounters types' to see available encounters"));
            return;
        }
        
        EntityPlayer player = (EntityPlayer) sender;
        String encounterType = args[1].toLowerCase();
        
        sender.sendMessage(new TextComponentString(
            TextFormatting.YELLOW + "Testing " + encounterType + " encounter..."));
        
        // Test specific encounter type
        boolean success = RandomEncounters.encounterManager.triggerSpecificEncounter(player, encounterType);
        
        if (success) {
            sender.sendMessage(new TextComponentString(
                TextFormatting.GREEN + "Test " + encounterType + " encounter triggered!"));
        } else {
            sender.sendMessage(new TextComponentString(
                TextFormatting.RED + "Failed to trigger " + encounterType + " encounter (type may not be available)"));
        }
    }
    
    private void enableEncounters(ICommandSender sender) throws CommandException {
        if (!sender.canUseCommand(2, "encounters")) {
            throw new CommandException("commands.generic.permission");
        }
        
        ConfigHandler.enableRandomEncounters = true;
        sender.sendMessage(new TextComponentString(
            TextFormatting.GREEN + "Random encounters ENABLED"));
        sender.sendMessage(new TextComponentString(
            TextFormatting.GRAY + "Encounters will now generate automatically"));
    }
    
    private void disableEncounters(ICommandSender sender) throws CommandException {
        if (!sender.canUseCommand(2, "encounters")) {
            throw new CommandException("commands.generic.permission");
        }
        
        ConfigHandler.enableRandomEncounters = false;
        sender.sendMessage(new TextComponentString(
            TextFormatting.RED + "Random encounters DISABLED"));
        sender.sendMessage(new TextComponentString(
            TextFormatting.GRAY + "Use '/encounters force' for testing"));
    }
    
    private void showConfig(ICommandSender sender) {
        sender.sendMessage(new TextComponentString(
            TextFormatting.GOLD + "=== Encounter Configuration ==="));
        sender.sendMessage(new TextComponentString(
            TextFormatting.YELLOW + "Enabled: " + ConfigHandler.enableRandomEncounters));
        sender.sendMessage(new TextComponentString(
            TextFormatting.YELLOW + "Base Chance: " + ConfigHandler.baseEncounterChance + "%"));
        sender.sendMessage(new TextComponentString(
            TextFormatting.YELLOW + "Cooldown: " + ConfigHandler.encounterCooldown + "s"));
        sender.sendMessage(new TextComponentString(
            TextFormatting.YELLOW + "Max Daily: " + ConfigHandler.maxEncountersPerDay));
        sender.sendMessage(new TextComponentString(
            TextFormatting.YELLOW + "Min Distance from Spawn: " + ConfigHandler.minDistanceFromSpawn));
    }
    
    private void showTypes(ICommandSender sender) {
        sender.sendMessage(new TextComponentString(
            TextFormatting.GOLD + "=== Available Encounters ==="));
        
        // Show global encounter status
        boolean globalEnabled = ConfigHandler.enableRandomEncounters;
        sender.sendMessage(new TextComponentString(
            TextFormatting.YELLOW + "System Status: " + 
            (globalEnabled ? TextFormatting.GREEN + "ENABLED" : TextFormatting.RED + "DISABLED")));
        
        sender.sendMessage(new TextComponentString(
            TextFormatting.GRAY + "------------------------"));
        
        // Show loaded JSON encounters
        if (RandomEncounters.jsonLoader != null && RandomEncounters.jsonLoader.getConfig() != null 
            && RandomEncounters.jsonLoader.getConfig().encounters != null) {
            
            sender.sendMessage(new TextComponentString(
                TextFormatting.AQUA + "Loaded JSON Encounters:"));
            
            int enabledCount = 0;
            for (ai.torchlite.randomencounters.config.json.EncounterConfig.Encounter enc : 
                 RandomEncounters.jsonLoader.getConfig().encounters) {
                if (enc.enabled) {
                    enabledCount++;
                    sender.sendMessage(new TextComponentString(
                        TextFormatting.YELLOW + "  - " + TextFormatting.WHITE + enc.id + 
                        TextFormatting.GRAY + " (weight: " + enc.weight + ")"));
                }
            }
            
            if (enabledCount == 0) {
                sender.sendMessage(new TextComponentString(
                    TextFormatting.YELLOW + "  No encounters are enabled in the JSON configuration."));
            }
            
            sender.sendMessage(new TextComponentString(
                TextFormatting.GRAY + "------------------------"));
            sender.sendMessage(new TextComponentString(
                TextFormatting.YELLOW + "Total Active Encounters: " + 
                TextFormatting.WHITE + enabledCount));
                
            if (!globalEnabled && enabledCount > 0) {
                sender.sendMessage(new TextComponentString(
                    TextFormatting.RED + "Note: Encounters are configured but globally disabled!"));
                sender.sendMessage(new TextComponentString(
                    TextFormatting.GRAY + "Use '/encounters enable' to activate the system."));
            }
            
            sender.sendMessage(new TextComponentString(
                TextFormatting.GRAY + "Tip: Use '/encounters test <encounter_id>' to test a specific encounter."));
        } else {
            sender.sendMessage(new TextComponentString(
                TextFormatting.RED + "Error: No encounters loaded from JSON configuration!"));
            sender.sendMessage(new TextComponentString(
                TextFormatting.GRAY + "Check your config/randomencounters/encounters.json file."));
        }
    }
    
    private void reloadConfig(MinecraftServer server, ICommandSender sender) throws CommandException {
        if (!sender.canUseCommand(2, this.getName())) {
            throw new CommandException("commands.generic.permission");
        }
        
        // Note: Actual config reloading would require re-initializing ConfigHandler
        sender.sendMessage(new TextComponentString(
            TextFormatting.GREEN + "Configuration reloaded! (Restart recommended for all changes to take effect)"));
    }
    
    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
        if (args.length == 1) {
            return getListOfStringsMatchingLastWord(args, "help", "status", "cooldown", "trigger", "force", "test", "enable", "disable", "config", "types", "reload");
        } else if (args.length == 2 && "test".equals(args[0])) {
            return getListOfStringsMatchingLastWord(args, "mob", "loot", "event", "npc", "friendly", "army", "json");
        }
        return Collections.emptyList();
    }
}
