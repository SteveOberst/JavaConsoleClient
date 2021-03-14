package com.github.ninurtax.consoleclient.modules.impl.reconnect;

import com.github.ninurtax.consoleclient.ConsoleClient;
import com.github.ninurtax.consoleclient.config.LoadByIni;
import com.github.ninurtax.consoleclient.modules.Module;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.logging.Level;

public class ReconnectModule extends Module {

    @LoadByIni
    public String reconnectMessagesFile = "sample-reconnect.txt";

    /**
     * This List contains all messages, the client will reconnect on
     */
    ArrayList<String> reconnectMessages = new ArrayList<>();

    private final ConsoleClient consoleClient;

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
            this.enabled = false;
        }
    }

    private void loadMessages() throws IOException {
        reconnectMessages = new ArrayList<>();
        File file = new File(reconnectMessagesFile);
        if (!file.exists()) {
            consoleClient.log(Level.WARNING, "Didn't find '"+reconnectMessagesFile+"' creating empty");
            file.createNewFile();
        }

        Scanner scanner = new Scanner(file);
        while (scanner.hasNextLine()) {
            reconnectMessages.add(scanner.nextLine());
        }
    }

    public void reconnect(String reason) {
        if (checkReason(reason));
            ConsoleClient.main(ConsoleClient.args);
    }

    private boolean checkReason(String reconnectModule) {
        return  (reconnectMessages.contains(reconnectModule));
    }
}
