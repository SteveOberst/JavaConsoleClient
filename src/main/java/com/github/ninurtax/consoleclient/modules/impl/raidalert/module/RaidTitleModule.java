package com.github.ninurtax.consoleclient.modules.impl.raidalert.module;

import com.github.ninurtax.consoleclient.ConsoleClient;
import com.github.ninurtax.consoleclient.config.LoadByIni;
import com.github.ninurtax.consoleclient.modules.Module;
import com.github.ninurtax.consoleclient.modules.impl.raidalert.RaidAlertSettings;
import com.github.ninurtax.consoleclient.phoneservice.PhoneService;
import com.github.ninurtax.consoleclient.util.Util;
import com.github.steveice10.mc.protocol.packet.ingame.server.ServerChatPacket;
import com.github.steveice10.mc.protocol.packet.ingame.server.ServerTitlePacket;
import com.github.steveice10.mc.protocol.packet.ingame.server.world.ServerExplosionPacket;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

public class RaidTitleModule extends Module {
    private final ConsoleClient consoleClient;
    private final RaidAlertSettings raidAlertSettings;
    private final PhoneService phoneService;

    public RaidTitleModule(String name, ConsoleClient consoleClient) {
        super(name);
        this.consoleClient = consoleClient;
        this.raidAlertSettings = consoleClient.getRaidAlertSettings();
        this.phoneService = consoleClient.getPhoneService();
    }
    //TODO: cleanup code
    public void onChatPacketRecieved(ServerChatPacket packet) {
        if(!raidAlertSettings.isDetectRunningRaid()) {
            return;
        }
        String message = ChatColor.stripColor(packet.getMessage().toString());
        if(!checkMessage(message)) {
            return;
        }
        if (!Util.isOnDelay(raidAlertSettings.getLastTimePhoneMessageTriggered(), raidAlertSettings.getPhoneMessageDelay())) {
            if(!raidAlertSettings.isPhoneMessages()) {
                return;
            }
            raidAlertSettings.setLastTimePhoneMessageTriggered(System.currentTimeMillis());
            if(raidAlertSettings.getExplosionDetectedMessage() == null) {
                phoneService.getContacts().forEach(phoneService::sendMessage);
            }else {
                phoneService.getContacts().forEach(contact -> phoneService.sendMessage(contact,
                        raidAlertSettings.getRaidStartedMessage().replace("{raiders_faction}",
                                packet.getMessage().toString().split(" ")[2].replace(":", "")))
                );
            }
        }
        if(!Util.isOnDelay(raidAlertSettings.getLastTimePhoneCallTriggered(), raidAlertSettings.getPhoneCallDelay())) {
            if(!raidAlertSettings.isPhoneCalls()) {
                return;
            }
            raidAlertSettings.setLastTimePhoneCallTriggered(System.currentTimeMillis());
            if(raidAlertSettings.getExplosionDetectedCall() == null) {
                phoneService.getContacts().forEach(phoneService::call);
            }else {
                phoneService.getContacts().forEach(contact -> phoneService.call(contact,
                        raidAlertSettings.getRaidStartedCall())
                );
            }
        }
    }

    private boolean checkMessage(String message) {
        return message.contains("Raid by") && !message.startsWith("[");
    }

    @Override
    public boolean isEnabled() {
        return raidAlertSettings.isEnabled();
    }
}
