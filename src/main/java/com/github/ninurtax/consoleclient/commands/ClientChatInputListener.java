package com.github.ninurtax.consoleclient.commands;

import com.github.steveice10.mc.protocol.packet.ingame.client.ClientChatPacket;
import com.github.steveice10.packetlib.Client;

import java.util.Scanner;

public class ClientChatInputListener {

    final Client client;

    public ClientChatInputListener(Client client) {
        this.client = client;
    }

    public void start() {
        new Thread("Client Chatter") {
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
        }.start();
    }

}

