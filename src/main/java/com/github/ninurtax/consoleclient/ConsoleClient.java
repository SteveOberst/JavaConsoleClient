package com.github.ninurtax.consoleclient;

import com.github.ninurtax.consoleclient.config.SettingsManager;
import com.github.ninurtax.consoleclient.log.ClientLogger;
import com.github.ninurtax.consoleclient.minecraft.Minecraft;
import com.github.ninurtax.consoleclient.modules.ModuleManager;

import java.util.logging.Level;

public class ConsoleClient extends ResourcePlugin {

    public static String[] args;
    /**
     * Program entry point
     *
     * @param args
     */
    public static void main(String[] args) {
        ConsoleClient.args = args;
        if (args.length == 0) {
            System.out.println("Not enough parameters given. Exiting");
            return;
        }
        new ConsoleClient(args);
    }

    private final ClientLogger logger;
    private final ModuleManager moduleManager;
    private final SettingsManager settingsManager;
    private Minecraft minecraft;

    public ConsoleClient(String[] args) {
        this.logger = new ClientLogger();
        log("Starting Client...");
        this.moduleManager = new ModuleManager(this);
        this.settingsManager = new SettingsManager(this);
        moduleManager.callStart();
        try {
            minecraft = new Minecraft(this, args);
            minecraft.login();
        } catch (ArrayIndexOutOfBoundsException e) {
            log(Level.SEVERE, "Not enough parameters given");
            System.exit(0);
        }
    }

    /**
     * @return an instance of the logger
     */
    public ClientLogger getLogger() {
        return logger;
    }

    public void log(String msg) {
        log(Level.INFO, msg);
    }

    public void log(Level level, String msg) {
        getLogger().log(level, msg);
    }

    public ModuleManager getModuleManager() {
        return moduleManager;
    }

    public SettingsManager getSettingsManager() {
        return settingsManager;
    }

    public Minecraft getMinecraft() {
        return minecraft;
    }
}
