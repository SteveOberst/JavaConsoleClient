package com.github.ninurtax.consoleclient.modules.impl.script;

import com.github.ninurtax.consoleclient.ConsoleClient;
import com.github.steveice10.packetlib.Client;
import com.github.steveice10.packetlib.Session;
import org.jetbrains.annotations.NotNull;

import java.io.FileNotFoundException;

public class ScriptCommander extends Thread {

    private static final int TIMEOUT_IN_MILLIS = 60000;
    private final Session session;
    private final ScriptReader scriptReader;
    private ConsoleClient consoleClient;

    public ScriptCommander(@NotNull String fileName,ConsoleClient consoleClient, Session session) throws FileNotFoundException {
        this.session = session;
        this.consoleClient = consoleClient;
        this.scriptReader = new ScriptReader(fileName);
    }

    @Override
    public void run() {
        while (scriptReader.hasNextLine()) {
            String[] args = scriptReader.getNextLine().split(" ");
            ScriptHandler scriptHandler = new ScriptHandler(args, this);
            scriptHandler.start();
            try {
                scriptHandler.join(TIMEOUT_IN_MILLIS);
            } catch (InterruptedException e) {
                e.printStackTrace();
                return;
            }
        }
    }

    public Session getSession() {
        return session;
    }

    public ConsoleClient getConsoleClient() {
        return consoleClient;
    }
}