package com.github.ninurtax.consoleclient.listener;

import com.github.ninurtax.consoleclient.ConsoleClient;
import com.github.ninurtax.consoleclient.modules.ModuleManager;
import com.github.ninurtax.consoleclient.modules.impl.reconnect.ReconnectModule;
import com.github.ninurtax.consoleclient.modules.impl.script.StartupScriptModule;
import com.github.steveice10.mc.protocol.data.message.Message;
import com.github.steveice10.mc.protocol.packet.ingame.server.ServerChatPacket;
import com.github.steveice10.mc.protocol.packet.ingame.server.ServerJoinGamePacket;
import com.github.steveice10.packetlib.Session;
import com.github.steveice10.packetlib.event.session.DisconnectedEvent;
import com.github.steveice10.packetlib.event.session.DisconnectingEvent;
import com.github.steveice10.packetlib.event.session.PacketReceivedEvent;
import com.github.steveice10.packetlib.event.session.SessionAdapter;
import com.google.gson.JsonSyntaxException;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainComponentSerializer;
import org.bukkit.ChatColor;

public class ClientListener extends SessionAdapter {

    private ConsoleClient consoleClient;
    ModuleManager moduleManager;

    public ClientListener(ConsoleClient consoleClient) {
        this.consoleClient = consoleClient;
        this. moduleManager = consoleClient.getModuleManager();
    }

    @Override
    public void packetReceived(PacketReceivedEvent event) {
        if (event.getPacket() instanceof ServerJoinGamePacket) {
            onJoin(event.getSession());
        } else if (event.getPacket() instanceof ServerChatPacket) {
            if (!moduleManager.getChatModule().isEnabled()) return;
                moduleManager.getChatModule().onChat(((ServerChatPacket) event.getPacket()).getMessage());
        }
    }

    private void onJoin(Session session) {
        consoleClient.log("Server successfully joined");
        StartupScriptModule startupScriptModule = consoleClient.getModuleManager().getStartupScriptModule();
        if (startupScriptModule.isEnabled() && !startupScriptModule.isAlreadyExecuted())
            startupScriptModule.executeStartup(session);
    }

    @Override
    public void disconnected(DisconnectedEvent event) {
        consoleClient.log("Disconnected: " + event.getReason());
        if (event.getCause() != null) {
            event.getCause().printStackTrace();
        }
        tryReconnect(event.getReason());
    }

    private void tryReconnect(String reason) {
        ReconnectModule reconnectModule = consoleClient.getModuleManager().getReconnectModule();
        if (reconnectModule.isEnabled()) {
            reconnectModule.reconnect(reason);
        }
    }

}
