package com.github.ninurtax.consoleclient.modules.impl.script;

import com.github.ninurtax.consoleclient.ConsoleClient;
import com.github.ninurtax.consoleclient.config.LoadByIni;
import com.github.ninurtax.consoleclient.modules.Module;
import com.github.steveice10.packetlib.Client;
import com.github.steveice10.packetlib.Session;

import java.io.File;
import java.util.logging.Level;

public class StartupScriptModule extends Module {

    @LoadByIni
    public String startupFileName = "startup.txt";

    private ConsoleClient consoleClient;
    private boolean isAlreadyExecuted = false;

    public StartupScriptModule(String name, ConsoleClient consoleClient) {
        super(name);
        this.consoleClient = consoleClient;
    }

    @Override
    public void start() {
        consoleClient.saveResource("sample-startup.txt", false);

    }

    public void executeStartup(Session session) {
        setAlreadyExecuted(true);
        File file = new File(startupFileName);
        if (!file.exists()) {
            consoleClient.log(Level.WARNING, "Didn't find '"+startupFileName+"' stopping module");
            this.enabled = false;
            return;
        } else {
            consoleClient.log("Found '"+startupFileName+"', reading and executing now!");
        }
        try {
            ScriptCommander scriptCommander = new ScriptCommander(startupFileName, consoleClient, session);
            scriptCommander.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setAlreadyExecuted(boolean alreadyExecuted) {
        isAlreadyExecuted = alreadyExecuted;
    }

    public boolean isAlreadyExecuted() {
        return isAlreadyExecuted;
    }


}
