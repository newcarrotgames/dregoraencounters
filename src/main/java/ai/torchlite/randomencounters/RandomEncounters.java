package ai.torchlite.randomencounters;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import ai.torchlite.randomencounters.proxy.CommonProxy;
import ai.torchlite.randomencounters.config.ConfigHandler;
import ai.torchlite.randomencounters.config.json.JsonEncounterLoader;
import ai.torchlite.randomencounters.commands.EncounterCommand;
import ai.torchlite.randomencounters.encounters.EncounterManager;

import java.io.File;

@Mod(modid = RandomEncounters.MODID, 
     name = RandomEncounters.NAME, 
     version = RandomEncounters.VERSION,
     acceptableRemoteVersions = "*",
     acceptedMinecraftVersions = "[1.12.2]")
public class RandomEncounters {
    
    public static final String MODID = "randomencounters";
    public static final String NAME = "Random Encounters";
    public static final String VERSION = "@VERSION@";
    
    @Instance(MODID)
    public static RandomEncounters instance;
    
    @SidedProxy(clientSide = "ai.torchlite.randomencounters.proxy.ClientProxy", 
                serverSide = "ai.torchlite.randomencounters.proxy.CommonProxy")
    public static CommonProxy proxy;
    
    public static EncounterManager encounterManager;
    public static JsonEncounterLoader jsonLoader;
    
    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        ConfigHandler.init(event.getSuggestedConfigurationFile());
        
        // Initialize JSON loader with config directory
        jsonLoader = new JsonEncounterLoader();
        File configDir = event.getModConfigurationDirectory();
        File modConfigDir = new File(configDir, MODID);
        if (!modConfigDir.exists()) {
            modConfigDir.mkdirs();
        }
        jsonLoader.loadConfig(modConfigDir);
        
        encounterManager = new EncounterManager();
        
        // Log mod status
        if (ConfigHandler.enableRandomEncounters) {
            System.out.println("RandomEncounters: Mod is ENABLED - encounters will generate");
        } else {
            System.out.println("RandomEncounters: Mod is DISABLED - no encounters will generate");
            System.out.println("RandomEncounters: To enable, set 'enableRandomEncounters=true' in config/randomencounters.cfg");
        }
        
        proxy.preInit(event);
    }
    
    @EventHandler
    public void init(FMLInitializationEvent event) {
        proxy.init(event);
    }
    
    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        proxy.postInit(event);
    }
    
    @EventHandler
    public void serverStarting(FMLServerStartingEvent event) {
        event.registerServerCommand(new EncounterCommand());
    }
}
