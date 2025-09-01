package ai.torchlite.randomencounters.proxy;

import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class ClientProxy extends CommonProxy {
    
    @Override
    public void preInit(FMLPreInitializationEvent event) {
        super.preInit(event);
        // Client-specific initialization (if any)
    }
    
    @Override
    public void init(FMLInitializationEvent event) {
        super.init(event);
        // Client-specific initialization (if any)
    }
    
    @Override
    public void postInit(FMLPostInitializationEvent event) {
        super.postInit(event);
        // Client-specific initialization (if any)
    }
}
