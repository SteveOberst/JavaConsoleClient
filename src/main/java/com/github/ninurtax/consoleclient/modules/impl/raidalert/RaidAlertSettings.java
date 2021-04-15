package com.github.ninurtax.consoleclient.modules.impl.raidalert;

import com.github.ninurtax.consoleclient.config.Configuration;
import com.github.ninurtax.consoleclient.config.IniAble;
import com.github.ninurtax.consoleclient.config.LoadByIni;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

public class RaidAlertSettings implements IniAble {
    //Boolean values
    @LoadByIni(section = "RaidAlert")
    @Getter
    private boolean detectExplosions;

    @LoadByIni(section = "RaidAlert")
    @Getter
    private boolean detectRunningRaid;

    @LoadByIni(section = "RaidAlert")
    @Getter
    private boolean phoneMessages;

    @LoadByIni(section = "RaidAlert")
    @Getter
    private boolean phoneCalls;

    //Other values to work with
    @LoadByIni(section = "RaidAlert")
    @Getter
    private long discordAlertDelay;

    @LoadByIni(section = "RaidAlert")
    @Getter
    private long phoneMessageDelay;

    @LoadByIni(section = "RaidAlert")
    @Getter
    private int phoneCallDelay;

    @Getter
    private boolean enabled = false;

    @Getter
    @Setter
    private long lastTimePhoneMessageTriggered = System.currentTimeMillis();

    @Getter
    @Setter
    private long lastTimePhoneCallTriggered = System.currentTimeMillis();

    public RaidAlertSettings(String[] args) {
        Configuration configuration = new Configuration("messages.cfg");
        explosionDetectedMessage = configuration.getString("tnt-detected-phone-message");
        explosionDetectedCall = configuration.getString("tnt-detected-phone-call");
        raidStartedMessage = configuration.getString("raid-started-phone-message");
        raidStartedCall = configuration.getString("raid-started-phone-call");
        if(args.length == 4) {
            enabled = Boolean.valueOf(args[3]);
        }
    }

    @Getter
    private final String explosionDetectedMessage;

    @Getter
    private final String explosionDetectedCall;

    @Getter
    private final String raidStartedMessage;

    @Getter
    private final String raidStartedCall;
}
