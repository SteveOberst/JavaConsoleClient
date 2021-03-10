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

    private static final boolean SPAWN_SERVER = false;
    private static final boolean VERIFY_USERS = true;
    private static final int PORT = 25565;
    private static final ProxyInfo PROXY = null;
    private static final Proxy AUTH_PROXY = Proxy.NO_PROXY;
    private static String USERNAME = "";
    private static String PASSWORD = "";
    private static String SERVER = "";

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
        client.getSession().setFlag(MinecraftConstants.AUTOMATIC_KEEP_ALIVE_MANAGEMENT, true);

        client.getSession().addListener(new SessionAdapter() {
            @Override
            public void packetReceived(PacketReceivedEvent event) {
                if (event.getPacket() instanceof ServerJoinGamePacket) {
                    System.out.println("Server successfully joined");
                    if (!ScriptReader.alreadyExecuted)
                        tryStartupScript(client);
                } else if (event.getPacket() instanceof ServerChatPacket) {
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
            ScriptReader scriptReader = new ScriptReader("startup.txt", client);
            scriptReader.execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static class ScriptReader extends FileReader {

        public static boolean alreadyExecuted = false;
        Client client;

        public ScriptReader(@NotNull String fileName, Client client) throws FileNotFoundException {
            super(fileName);
            this.client = client;
        }

        void execute () {
            alreadyExecuted = true;
            Scanner scanner = new Scanner(this);
            while (scanner.hasNextLine()) {
                String[] args = scanner.nextLine().split(" ");
                new SkriptHandler(args).execute();
            }
        }

        private class SkriptHandler {
            String[] args;
            public SkriptHandler(String[] args) {
                this.args = args;
            }

            void execute() throws ArrayIndexOutOfBoundsException {
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
                        //TODO:
                        break;
                    default:
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

    private static CompoundTag getDimensionTag() {
        CompoundTag tag = new CompoundTag("");

        CompoundTag dimensionTypes = new CompoundTag("minecraft:dimension_type");
        dimensionTypes.put(new StringTag("type", "minecraft:dimension_type"));
        ListTag dimensionTag = new ListTag("value");
        CompoundTag overworldTag = convertToValue("minecraft:overworld", 0, getOverworldTag().getValue());
        dimensionTag.add(overworldTag);
        dimensionTypes.put(dimensionTag);
        tag.put(dimensionTypes);

        CompoundTag biomeTypes = new CompoundTag("minecraft:worldgen/biome");
        biomeTypes.put(new StringTag("type", "minecraft:worldgen/biome"));
        ListTag biomeTag = new ListTag("value");
        CompoundTag plainsTag = convertToValue("minecraft:plains", 0, getPlainsTag().getValue());
        biomeTag.add(plainsTag);
        biomeTypes.put(biomeTag);
        tag.put(biomeTypes);

        return tag;
    }

    private static CompoundTag getOverworldTag() {
        CompoundTag overworldTag = new CompoundTag("");
        overworldTag.put(new StringTag("name", "minecraft:overworld"));
        overworldTag.put(new ByteTag("piglin_safe", (byte) 0));
        overworldTag.put(new ByteTag("natural", (byte) 1));
        overworldTag.put(new FloatTag("ambient_light", 0f));
        overworldTag.put(new StringTag("infiniburn", "minecraft:infiniburn_overworld"));
        overworldTag.put(new ByteTag("respawn_anchor_works", (byte) 0));
        overworldTag.put(new ByteTag("has_skylight", (byte) 1));
        overworldTag.put(new ByteTag("bed_works", (byte) 1));
        overworldTag.put(new StringTag("effects", "minecraft:overworld"));
        overworldTag.put(new ByteTag("has_raids", (byte) 1));
        overworldTag.put(new IntTag("logical_height", 256));
        overworldTag.put(new FloatTag("coordinate_scale", 1f));
        overworldTag.put(new ByteTag("ultrawarm", (byte) 0));
        overworldTag.put(new ByteTag("has_ceiling", (byte) 0));
        return overworldTag;
    }

    private static CompoundTag getPlainsTag() {
        CompoundTag plainsTag = new CompoundTag("");
        plainsTag.put(new StringTag("name", "minecraft:plains"));
        plainsTag.put(new StringTag("precipitation", "rain"));
        plainsTag.put(new FloatTag("depth", 0.125f));
        plainsTag.put(new FloatTag("temperature", 0.8f));
        plainsTag.put(new FloatTag("scale", 0.05f));
        plainsTag.put(new FloatTag("downfall", 0.4f));
        plainsTag.put(new StringTag("category", "plains"));

        CompoundTag effects = new CompoundTag("effects");
        effects.put(new LongTag("sky_color", 7907327));
        effects.put(new LongTag("water_fog_color", 329011));
        effects.put(new LongTag("fog_color", 12638463));
        effects.put(new LongTag("water_color", 4159204));

        CompoundTag moodSound = new CompoundTag("mood_sound");
        moodSound.put(new IntTag("tick_delay", 6000));
        moodSound.put(new FloatTag("offset", 2.0f));
        moodSound.put(new StringTag("sound", "minecraft:ambient.cave"));
        moodSound.put(new IntTag("block_search_extent", 8));

        effects.put(moodSound);

        plainsTag.put(effects);

        return plainsTag;
    }

    private static CompoundTag convertToValue(String name, int id, Map<String, Tag> values) {
        CompoundTag tag = new CompoundTag(name);
        tag.put(new StringTag("name", name));
        tag.put(new IntTag("id", id));
        CompoundTag element = new CompoundTag("element");
        element.setValue(values);
        tag.put(element);

        return tag;
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
