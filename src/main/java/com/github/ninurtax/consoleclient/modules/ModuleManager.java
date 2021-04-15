package com.github.ninurtax.consoleclient.modules;

import com.github.ninurtax.consoleclient.ConsoleClient;
import com.github.ninurtax.consoleclient.modules.impl.chat.ChatModule;
import com.github.ninurtax.consoleclient.modules.impl.raidalert.module.RaidTitleModule;
import com.github.ninurtax.consoleclient.modules.impl.raidalert.module.TntExplodeModule;
import com.github.ninurtax.consoleclient.modules.impl.reconnect.ReconnectModule;
import com.github.ninurtax.consoleclient.modules.impl.script.StartupScriptModule;

import java.util.ArrayList;
import java.util.List;

public class ModuleManager {

    private final ConsoleClient consoleClient;
    private final List<Module> moduleList = new ArrayList<>();
    /*
       Modules
     */
    private ChatModule chatModule;
    private StartupScriptModule startupScriptModule;
    private ReconnectModule reconnectModule;
    private TntExplodeModule tntExplodeModule;
    private RaidTitleModule raidTitleModule;

    public ModuleManager(ConsoleClient consoleClient) {
        this.consoleClient = consoleClient;
        initModules();
    }

    private void initModules() {
        /*
         * Init
         */
        chatModule = new ChatModule("Chat", consoleClient);
        startupScriptModule = new StartupScriptModule("StartupScript", consoleClient);
        reconnectModule = new ReconnectModule("AutoReconnect", consoleClient);
        tntExplodeModule = new TntExplodeModule("TntExplodeDetection", consoleClient);
        raidTitleModule = new RaidTitleModule("RunningRaidDetection", consoleClient);

        /*
         * Registering
         */
        moduleList.add(chatModule);
        moduleList.add(startupScriptModule);
        moduleList.add(reconnectModule);
        moduleList.add(tntExplodeModule);
        moduleList.add(raidTitleModule);
    }

    /**
     * Call for every module that it should start working!
     */
    public void callStart() {
        moduleList.forEach(Module::start);
    }

    public List<Module> getModuleList() {
        return moduleList;
    }

    public ChatModule getChatModule() {
        return chatModule;
    }

    public TntExplodeModule getTntExplodeModule() { return tntExplodeModule; }

    public RaidTitleModule getRaidTitleModule() { return raidTitleModule; }

    public StartupScriptModule getStartupScriptModule() {
        return startupScriptModule;
    }

    public ReconnectModule getReconnectModule() {
        return reconnectModule;
    }
}
