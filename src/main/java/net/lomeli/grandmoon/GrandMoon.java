package net.lomeli.grandmoon;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

@Mod(modid = GrandMoon.MOD_ID, name = GrandMoon.MOD_NAME, version = GrandMoon.VERSION)
public class GrandMoon {
    public static final String MOD_ID = "grandmoon";
    public static final String MOD_NAME = "Grand Moon";
    public static final String VERSION = "1.0.0";

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        if (!event.getSide().isClient())
            Logger.logWarning("WTF ARE YOU DOING?! THIS IS A CLIENT SIDE ONLY MOD!!");
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        if (event.getSide().isClient()) {
            EventHandler handler = new EventHandler();
            FMLCommonHandler.instance().bus().register(handler);
            MinecraftForge.EVENT_BUS.register(handler);
        }
    }
}
