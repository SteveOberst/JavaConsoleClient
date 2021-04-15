package com.github.ninurtax.consoleclient.modules.impl.reconnect;

import com.github.ninurtax.consoleclient.ConsoleClient;
import com.github.ninurtax.consoleclient.config.LoadByIni;
import com.github.ninurtax.consoleclient.log.ClientLogger;
import com.github.ninurtax.consoleclient.modules.Module;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.logging.Level;

public class ReconnectModule extends Module {

    private final ConsoleClient consoleClient;
    @LoadByIni(section = "AutoReconnect")
    private String reconnectMessagesFile = "sample-reconnect.txt";
    @LoadByIni(section = "AutoReconnect")
    private int delayInMS = 5000;
    @LoadByIni(section = "AutoReconnect")
    private boolean enabled = true;
    /**
     * This List contains all messages, the client will reconnect on
     */
    private ArrayList<String> reconnectMessages = new ArrayList<>();

    public ReconnectModule(String name, ConsoleClient consoleClient) {
        super(name);
        this.consoleClient = consoleClient;
    }

    @Override
    public void start() {
        consoleClient.saveResource("sample-reconnect.txt", false);
        try {
            loadMessages();
        } catch (IOException e) {
            e.printStackTrace();
             enabled = false;
        }
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    private void loadMessages() throws IOException {
        reconnectMessages = new ArrayList<>();
        File file = new File(reconnectMessagesFile);
        if (!file.exists()) {
            consoleClient.log(Level.WARNING, "Didn't find '" + reconnectMessagesFile + "' creating empty");
            boolean newFile = file.createNewFile();
        }

        Scanner scanner = new Scanner(file);
        while (scanner.hasNextLine()) {
            reconnectMessages.add(scanner.nextLine().toLowerCase());
        }
    }

    public void reconnect(String reason) {
        if (checkReason(reason)) {
            performReconnect();
        } else {
            consoleClient.log(Level.INFO, "Reason is not suited for reconnecting (update '" + reconnectMessagesFile + "' if otherwise)");
        }
    }

    private void performReconnect() {
        new Thread(() -> {
            consoleClient.getLogger().log(ClientLogger.Prefix.SCRIPT, "Reconnecting client");
            if (delayInMS > 0) {
                consoleClient.getLogger().log(ClientLogger.Prefix.SCRIPT, "Waiting "+delayInMS+"ms");
                try {
                    Thread.sleep(delayInMS);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            clearConsole();
            // Let the startup script perfom again
            consoleClient.getModuleManager().getStartupScriptModule().setAlreadyExecuted(false);
            // Connecting
            consoleClient.getMinecraft().login();
        }).start();

    }

    void clearConsole() {
        //Clearing the console
        try {
            ClientLogger.CLS.main();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private boolean checkReason(String reconnectModule) {
        return (reconnectMessages.contains(reconnectModule.toLowerCase()));
    }
}
