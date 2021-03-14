package com.github.ninurtax.consoleclient.minecraft;

import com.github.ninurtax.consoleclient.ConsoleClient;
import com.github.ninurtax.consoleclient.commands.ClientChatInputListener;
import com.github.ninurtax.consoleclient.listener.ClientListener;
import com.github.steveice10.mc.auth.exception.request.RequestException;
import com.github.steveice10.mc.protocol.MinecraftProtocol;
import com.github.steveice10.packetlib.Client;
import com.github.steveice10.packetlib.tcp.TcpSessionFactory;

public class Minecraft {

    private final String[] args;
    private final ConsoleClient consoleClient;
    private final CredentialsBuilder credentialsBuilder;
    private final String username;
    private final String password;
    private final String server;
    private int port = 25565;

    private Client client;


    public Minecraft(ConsoleClient consoleClient, String[] args) throws ArrayIndexOutOfBoundsException {
        this.consoleClient = consoleClient;
        this.args = args;
        this.credentialsBuilder = new CredentialsBuilder(args);

        this.username = credentialsBuilder.username;
        this.password = credentialsBuilder.password;
        this.server = credentialsBuilder.server;
    }

    public void login() {
        MinecraftProtocol protocol = null;
        try {
            protocol = new MinecraftProtocol(username, password);
            consoleClient.log("Successfully authenticated user.");
        } catch (RequestException e) {
            e.printStackTrace();
            return;
        }

        Client client = new Client(server, port, protocol, new TcpSessionFactory());
        client.getSession().setConnectTimeout(120);
        client.getSession().setReadTimeout(120);

        client.getSession().addListener(new ClientListener(consoleClient));
        client.getSession().connect(true);
        new ClientChatInputListener(client).start();
    }

    public Client getClient() {
        return client;
    }

    static class CredentialsBuilder {
        final String[] args;

        public String username = "";
        public String password = "";
        public String server = "";

        CredentialsBuilder(String[] args) {
            this.args = args;
            build();
        }

        private void build() throws ArrayIndexOutOfBoundsException {
            username = args[0];
            password = args[1];
            server = args[2];
        }


    }

}
