package com.github.ninurtax.consoleclient.modules.impl.script;

import com.github.ninurtax.consoleclient.ConsoleClient;
import com.github.ninurtax.consoleclient.log.ClientLogger;
import com.github.steveice10.mc.protocol.packet.ingame.client.ClientChatPacket;
import com.github.steveice10.packetlib.Session;

import java.util.StringJoiner;
import java.util.logging.Level;

public class ScriptHandler extends Thread {

    String[] args;
    ScriptCommander executor;
    ConsoleClient consoleClient;

    public ScriptHandler(String[] args, ScriptCommander executor) {
        this.args = args;
        this.executor = executor;
        consoleClient = executor.getConsoleClient();
    }

    @Override
    public void run() {
        try {
            this.execute();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    void execute() throws ArrayIndexOutOfBoundsException, InterruptedException, NumberFormatException {
        CommandExecutor command = CommandExecutor.valueOf(args[0].toUpperCase());
        if (command != null) {
            command.execute(consoleClient.getLogger(), executor.getSession(), args);
        } else {
            consoleClient.getLogger().log(ClientLogger.Prefix.SCRIPT, Level.WARNING, "Unknown Command: " + args[0]);
        }


    }

    public enum CommandExecutor implements ICommandExecutor {

        /**
         * Sends a command to the server
         */
        SEND((logger, session, args) -> {
            StringJoiner stringBuilder = new StringJoiner(" ");
            for (int i = 1; i < args.length; i++) {
                stringBuilder.add(args[i]);
            }
            String message = stringBuilder.toString();
            logger.log(ClientLogger.Prefix.SCRIPT, "Sending: " + message);
            session.send(new ClientChatPacket(message));
        }),
        /**
         * Waits the given amount of time
         */
        WAIT((logger, session, args) -> {
            int timeout = Integer.parseInt(args[1]);
            logger.log(ClientLogger.Prefix.SCRIPT, "Waiting: " + timeout + "ms");
            sleep(timeout);
        }),

        ;

        public ICommandExecutor command;

        CommandExecutor(ICommandExecutor command) {
            this.command = command;
        }

        @Override
        public void execute(ClientLogger logger, Session session, String[] args) throws InterruptedException {
            command.execute(logger, session, args);
        }
    }

    @FunctionalInterface
    public interface ICommandExecutor {
        void execute(ClientLogger logger, Session session, String[] args) throws InterruptedException;
    }

}