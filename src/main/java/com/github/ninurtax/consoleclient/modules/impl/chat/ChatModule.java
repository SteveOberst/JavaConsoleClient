package com.github.ninurtax.consoleclient.modules.impl.chat;

import com.github.ninurtax.consoleclient.ConsoleClient;
import com.github.ninurtax.consoleclient.log.ClientLogger;
import com.github.ninurtax.consoleclient.modules.Module;
import com.github.steveice10.mc.protocol.data.message.Message;
import com.google.gson.JsonSyntaxException;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainComponentSerializer;
import org.bukkit.ChatColor;

import java.util.logging.Level;

public class ChatModule extends Module {

    final ConsoleClient consoleClient;

    public ChatModule(String name, ConsoleClient consoleClient) {
        super(name);
        this.consoleClient = consoleClient;
    }

    public void onChat(Message message) {
        consoleClient.getLogger().log(ClientLogger.Prefix.CHAT, Level.INFO, getPlainMessage(message));
    }

    private String getPlainMessage(Message message) {
        return ChatColor.stripColor(message.toString());

    }

}
