package com.github.ninurtax.consoleclient;

import com.github.steveice10.mc.auth.exception.request.RequestException;
import com.github.steveice10.mc.auth.service.AuthenticationService;
import com.github.steveice10.mc.auth.service.SessionService;
import com.github.steveice10.mc.protocol.MinecraftConstants;
import com.github.steveice10.mc.protocol.MinecraftProtocol;
import com.github.steveice10.mc.protocol.data.message.Message;
import com.github.steveice10.mc.protocol.packet.ingame.client.ClientChatPacket;
import com.github.steveice10.mc.protocol.packet.ingame.server.ServerChatPacket;
import com.github.steveice10.mc.protocol.packet.ingame.server.ServerJoinGamePacket;
import com.github.steveice10.opennbt.tag.builtin.*;
import com.github.steveice10.packetlib.Client;
import com.github.steveice10.packetlib.ProxyInfo;
import com.github.steveice10.packetlib.event.session.DisconnectedEvent;
import com.github.steveice10.packetlib.event.session.PacketReceivedEvent;
import com.github.steveice10.packetlib.event.session.SessionAdapter;
import com.github.steveice10.packetlib.tcp.TcpSessionFactory;
import com.google.gson.JsonSyntaxException;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainComponentSerializer;
import org.bukkit.ChatColor;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.Proxy;
import java.util.*;

public class ConsoleClient {

    private static final boolean VERIFY_USERS = true;
    private static final int PORT = 25565;
    private static final ProxyInfo PROXY = null;
    private static final Proxy AUTH_PROXY = Proxy.NO_PROXY;
    private static String USERNAME = "";
    private static String PASSWORD = "";
    private static String SERVER = "";
    private static final boolean IS_READING_MESSAGES = false;

    public static void main(String[] args) {
        //status();
        McArgsFormatter argsFormatted = null;
        try {
             argsFormatted = new McArgsFormatter(args);
        }catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("Not enough parameters given!");
            System.out.println("Exiting console client");
            return;
        }
        if (argsFormatted == null) return;

        USERNAME = argsFormatted.username;
        PASSWORD = argsFormatted.password;
        SERVER = argsFormatted.server;

        if (USERNAME.isEmpty() || PASSWORD.isEmpty() || SERVER.isEmpty()) return;

        login();

    }

    private static void login() {
        MinecraftProtocol protocol = null;
        if (VERIFY_USERS) {
            try {
                AuthenticationService authService = new AuthenticationService();
                authService.setUsername(USERNAME);
                authService.setPassword(PASSWORD);
                authService.setProxy(AUTH_PROXY);
                authService.login();

                // Can also use "new MinecraftProtocol(USERNAME, PASSWORD)"
                // if you don't need a proxy or any other customizations.
                protocol = new MinecraftProtocol(authService);
                System.out.println("Successfully authenticated user.");
            } catch (RequestException e) {
                e.printStackTrace();
                return;
            }
        } else {
            protocol = new MinecraftProtocol(USERNAME);
        }

        SessionService sessionService = new SessionService();
        sessionService.setProxy(AUTH_PROXY);

        Client client = new Client(SERVER, PORT, protocol, new TcpSessionFactory(PROXY));
        client.getSession().setFlag(MinecraftConstants.SESSION_SERVICE_KEY, sessionService);
        client.getSession().setConnectTimeout(120);
        client.getSession().setReadTimeout(120);

        client.getSession().addListener(new SessionAdapter() {
            @Override
            public void packetReceived(PacketReceivedEvent event) {
                if (event.getPacket() instanceof ServerJoinGamePacket) {
                    System.out.println("Server successfully joined");
                    if (!ScriptCommander.alreadyExecuted)
                        tryStartupScript(client);
                } else if (event.getPacket() instanceof ServerChatPacket) {
                    if (!IS_READING_MESSAGES) return;
                    Message message = event.<ServerChatPacket>getPacket().getMessage();
                    final Component component;
                    try {
                        component = GsonComponentSerializer.gson().deserialize(message.toString());
                    } catch (JsonSyntaxException e) {
                        System.out.println("[ConsoleClient] >> Couldn't output because of unknown JSON object.");
                        return;
                    }
                    final String plain = PlainComponentSerializer.plain().serialize(component);
                    String s = ChatColor.stripColor(plain);
                    System.out.println(s);
                }
            }

            @Override
            public void disconnected(DisconnectedEvent event) {
                System.out.println("Disconnected: " + event.getReason());
                if (event.getCause() != null) {
                    event.getCause().printStackTrace();
                }
            }
        });
        client.getSession().connect(true);
        //client.getSession().getPacketProtocol().registerOutgoing(323, ClientChatPacket.class);
        new CommandListener(client).start();
    }

    private static void tryStartupScript(Client client) {
        File file = new File("startup.txt");
        if (!file.exists()) {
            System.out.println("Didn't find 'startup.txt' creating an empty one");
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
        }else {
            System.out.println("Found 'startup.txt', reading and executing now!");
        }

        try {
            ScriptCommander scriptCommander = new ScriptCommander("startup.txt", client);
            scriptCommander.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static class ScriptCommander extends Thread {

        public static boolean alreadyExecuted = false;
        private static final int TIMEOUT_IN_MILLIS = 60000;
        private Client client;
        private ScriptReader scriptReader;


        public ScriptCommander(@NotNull String fileName, Client client) throws FileNotFoundException {
            this.client = client;
            this.scriptReader = new ScriptReader(fileName);
        }

        @Override
        public void run() {
            alreadyExecuted = true;
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


        private class ScriptReader extends FileReader {

            private Scanner scanner;

            public ScriptReader(@NotNull String fileName) throws FileNotFoundException {
                super(fileName);
                this.scanner = new Scanner(this);
            }

            public String getNextLine() {
                return scanner.nextLine();
            }

            public boolean hasNextLine() {
                return scanner.hasNextLine();
            }


        }

        private class ScriptHandler extends Thread {

            @Override
            public void run() {
                try {
                    this.execute();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            String[] args;
            ScriptCommander executor;

            public ScriptHandler(String[] args, ScriptCommander executor) {
                this.args = args;
                this.executor = executor;
            }

            void execute() throws ArrayIndexOutOfBoundsException, InterruptedException, NumberFormatException {
                switch (Commands.valueOf(args[0].toUpperCase())) {
                    case SEND:
                        StringJoiner stringBuilder = new StringJoiner(" ");
                        for (int i = 1; i < args.length; i++) {
                            stringBuilder.add(args[i]);
                        }

                        String message = stringBuilder.toString();
                        System.out.println(" >> [Console Cient] Sending: "+ message);
                        client.getSession().send(new ClientChatPacket(message));
                        break;
                    case WAIT:
                        Integer timeout = Integer.parseInt(args[1]);
                        System.out.println(" >> [Console Cient] Waiting: "+ timeout + "ms");
                        sleep(timeout);
                        break;
                    default:
                        System.out.println(" >> [Console Cient] Unkown Command: "+args[0]);
                        return;
                }
            }

        }
        private enum Commands {
            /**
             * Sends a command to the server
             */
            SEND,
            /**
             * Waits the given amount of time
             */
            WAIT;
        }
    }

    static class McArgsFormatter {
        final String[] args;

        public String username = "";
        public String password = "";
        public String server = "";

        McArgsFormatter(String[] args) {
            this.args = args;
            setup();
        }

        private void setup() throws ArrayIndexOutOfBoundsException {
            username = args[0];
            password = args[1];
            server = args[2];
        }


    }

    public static class CommandListener extends Thread {
        final Client client;

        CommandListener(Client client) {
            this.client = client;
        }

        @Override
        public void run() {
            try (Scanner scanner = new Scanner(System.in)) {
                while (client.getSession().isConnected()) {
                    if (!scanner.hasNext()) continue;
                    String input = scanner.nextLine();
                    client.getSession().send(new ClientChatPacket(input));
                }
            }
        }
    }

}
