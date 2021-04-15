package com.github.ninurtax.consoleclient.listener;

import com.github.ninurtax.consoleclient.ConsoleClient;
import com.github.ninurtax.consoleclient.modules.ModuleManager;
import com.github.ninurtax.consoleclient.modules.impl.reconnect.ReconnectModule;
import com.github.ninurtax.consoleclient.modules.impl.script.StartupScriptModule;
import com.github.steveice10.mc.protocol.packet.ingame.server.ServerChatPacket;
import com.github.steveice10.mc.protocol.packet.ingame.server.ServerJoinGamePacket;
import com.github.steveice10.mc.protocol.packet.ingame.server.ServerTitlePacket;
import com.github.steveice10.mc.protocol.packet.ingame.server.world.ServerExplosionPacket;
import com.github.steveice10.packetlib.Session;
import com.github.steveice10.packetlib.event.session.DisconnectedEvent;
import com.github.steveice10.packetlib.event.session.PacketReceivedEvent;
import com.github.steveice10.packetlib.event.session.SessionAdapter;

public class ClientListener extends SessionAdapter {

    private ConsoleClient consoleClient;
    ModuleManager moduleManager;

    public ClientListener(ConsoleClient consoleClient) {
        this.consoleClient = consoleClient;
        this.moduleManager = consoleClient.getModuleManager();
    }

    //TODO: implement proper management instead of a ton of if statements
    @Override
    public void packetReceived(PacketReceivedEvent event) {
        if (event.getPacket() instanceof ServerJoinGamePacket) {
            onJoin(event.getSession());
        } else if (event.getPacket() instanceof ServerChatPacket) {
            if (moduleManager.getChatModule().isEnabled()) {
                moduleManager.getChatModule().onChat(((ServerChatPacket) event.getPacket()).getMessage());
            }
            if(moduleManager.getRaidTitleModule().isEnabled()) {
                moduleManager.getRaidTitleModule().onChatPacketRecieved(event.getPacket());
            }
        } else if (event.getPacket() instanceof ServerExplosionPacket) {
            if(moduleManager.getTntExplodeModule().isEnabled()) {
                moduleManager.getTntExplodeModule().onExplosionDetected(event.getPacket());
            }
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
