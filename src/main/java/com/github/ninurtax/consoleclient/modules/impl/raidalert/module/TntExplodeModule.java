package com.github.ninurtax.consoleclient.modules.impl.raidalert.module;

import com.github.ninurtax.consoleclient.ConsoleClient;
import com.github.ninurtax.consoleclient.config.LoadByIni;
import com.github.ninurtax.consoleclient.modules.Module;
import com.github.ninurtax.consoleclient.modules.impl.raidalert.RaidAlertSettings;
import com.github.ninurtax.consoleclient.phoneservice.PhoneService;
import com.github.ninurtax.consoleclient.util.Util;
import com.github.steveice10.mc.protocol.packet.ingame.server.world.ServerExplosionPacket;

public class TntExplodeModule extends Module {
    private final ConsoleClient consoleClient;
    private final RaidAlertSettings raidAlertSettings;
    private final PhoneService phoneService;
    private long lastTimeTriggered = System.currentTimeMillis();


    public TntExplodeModule(String name, ConsoleClient consoleClient) {
        super(name);
        this.consoleClient = consoleClient;
        this.raidAlertSettings = consoleClient.getRaidAlertSettings();
        this.phoneService = consoleClient.getPhoneService();
    }

    //TODO: cleanup code
    public void onExplosionDetected(ServerExplosionPacket packet) {
        if(!consoleClient.getRaidAlertSettings().isDetectExplosions() ) {
            return;
        }

        if(Util.isOnDelay(lastTimeTriggered, 5000)) {
            return;
        }
        lastTimeTriggered = System.currentTimeMillis();

        if (!Util.isOnDelay(raidAlertSettings.getLastTimePhoneMessageTriggered(), raidAlertSettings.getPhoneMessageDelay())) {
            if(!raidAlertSettings.isPhoneMessages()) {
                return;
            }
            raidAlertSettings.setLastTimePhoneCallTriggered(System.currentTimeMillis());
            if(raidAlertSettings.getExplosionDetectedMessage() == null) {
                phoneService.getContacts().forEach(phoneService::sendMessage);
            }else {
                phoneService.getContacts().forEach(contact -> phoneService.sendMessage(contact,
                        raidAlertSettings.getExplosionDetectedMessage()
                                .replace("{x}", String.valueOf(packet.getX()))
                                .replace("{y}", String.valueOf(packet.getY()))
                                .replace("{z}", String.valueOf(packet.getZ())))
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
                    raidAlertSettings.getExplosionDetectedCall())
                );
            }
        }
    }

    @Override
    public boolean isEnabled() {
        return raidAlertSettings.isEnabled();
    }
}
