package com.github.ninurtax.consoleclient;

import com.github.ninurtax.consoleclient.config.LoadByIni;
import com.github.ninurtax.consoleclient.config.SettingsManager;
import com.github.ninurtax.consoleclient.log.ClientLogger;
import com.github.ninurtax.consoleclient.minecraft.Minecraft;
import com.github.ninurtax.consoleclient.modules.ModuleManager;
import com.github.ninurtax.consoleclient.modules.impl.raidalert.RaidAlertSettings;
import com.github.ninurtax.consoleclient.phoneservice.PhoneService;

import java.util.logging.Level;

public class ConsoleClient extends ResourcePlugin {
    private static final String DISCORD_BOT_TOKEN = "<>";

    public static String[] args;
    /**
     * Program entry point
     * Will cancel out if not enough parameters given.
     *
     * @param args
     */
    public static void main(String[] args)  {
        ConsoleClient.args = args;
        if (args.length == 0) {
            System.out.println("Not enough parameters given. Exiting");
            return;
        }
        new ConsoleClient(args);
    }

    private final RaidAlertSettings raidAlertSettings = new RaidAlertSettings(args);
    private final PhoneService phoneService = new PhoneService(this);
    private final ClientLogger logger;
    private final ModuleManager moduleManager;
    private final SettingsManager settingsManager;
    private Minecraft minecraft;

    public ConsoleClient(String[] args)  {;
        this.logger = new ClientLogger();
        log("Starting Client...");
        this.moduleManager = new ModuleManager(this);
        this.settingsManager = new SettingsManager(this);
        saveResource("messages.cfg", false);
        moduleManager.callStart();
        try {
            minecraft = new Minecraft(this, args);
            minecraft.login();
        } catch (ArrayIndexOutOfBoundsException e) {
            log(Level.SEVERE, "Not enough parameters given");
            System.exit(0);
        }
        settingsManager.registerIniable(raidAlertSettings);
        settingsManager.registerIniable(phoneService);
        try {
            settingsManager.refreshValues();
        }catch (IllegalAccessException exception) {
            exception.printStackTrace();
            log(Level.SEVERE, "Unable to parse values from ini file. Try resetting it to default (deleting it)");
            System.exit(0);
        }
        phoneService.init();
    }

    /**
     * @return an instance of the logger
     */
    public ClientLogger getLogger() {
        return logger;
    }

    public PhoneService getPhoneService() {
        return phoneService;
    }

    public RaidAlertSettings getRaidAlertSettings() { return raidAlertSettings; }

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
