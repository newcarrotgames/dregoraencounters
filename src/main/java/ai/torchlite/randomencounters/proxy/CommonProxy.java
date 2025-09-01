package ai.torchlite.randomencounters.proxy;

import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import ai.torchlite.randomencounters.events.EncounterEventHandler;
import net.minecraftforge.common.MinecraftForge;

public class CommonProxy {
    
    public void preInit(FMLPreInitializationEvent event) {
        // Register event handlers
        MinecraftForge.EVENT_BUS.register(new EncounterEventHandler());
    }
    
    public void init(FMLInitializationEvent event) {
        // Common initialization logic
    }
    
    public void postInit(FMLPostInitializationEvent event) {
        // Post initialization logic
    }
}
