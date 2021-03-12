package com.github.ninurtax.consoleclient.log;

import java.util.logging.*;

public class ClientLogger {

    Logger logger = Logger.getLogger(ClientLogger.class.getName());

    public ClientLogger() {
        logger.setUseParentHandlers(false);
        LogFormatter formatter = new LogFormatter();
        ConsoleHandler handler = new ConsoleHandler();
        handler.setFormatter(formatter);
        logger.addHandler(handler);
    }

    public void log(Level level, String msg) {
        logger.log(level, msg);
    }

    public void log(Prefix prefix, String msg) {
        this.log(prefix, Level.INFO, msg);
    }

    public void log(Prefix prefix, Level level, String msg) {
        logger.log(level, prefix.getName() + msg);
    }

    public static class Prefix {

        public static Prefix CHAT = new Prefix("Chat: ");
        public static Prefix SCRIPT = new Prefix("Script: ");

        private final String name;

        public Prefix(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    static class LogFormatter extends Formatter {

        public String format(LogRecord record) {
            return "[" + record.getLevel() + "] " +
                    formatMessage(record) +
                    "\n";
        }

        public String getHead(Handler h) {
            return super.getHead(h);
        }

        public String getTail(Handler h) {
            return super.getTail(h);
        }
    }
}
