package net.lomeli.grandmoon;

import org.apache.logging.log4j.Level;

import net.minecraftforge.fml.common.FMLLog;

public class Logger {

    public static void log(Level logLevel, Object message) {
        FMLLog.log(GrandMoon.MOD_ID, logLevel, String.valueOf(message));
    }

    public static void logWarning(Object message) {
        log(Level.WARN, message);
    }

    public static void logInfo(Object message) {
        log(Level.INFO, message);
    }

    public static void logError(Object message) {
        log(Level.ERROR, message);
    }
}
