package com.github.ninurtax.consoleclient.phoneservice;

import com.github.ninurtax.consoleclient.ConsoleClient;
import com.github.ninurtax.consoleclient.config.IniAble;
import com.github.ninurtax.consoleclient.config.LoadByIni;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Call;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import com.twilio.type.Twiml;
import org.yaml.snakeyaml.tokens.ScalarToken;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

public class PhoneService implements IniAble {
    @LoadByIni(section = "PhoneService")
    private String sid;
    @LoadByIni(section = "PhoneService")
    private String authToken;
    @LoadByIni(section = "PhoneService")
    private String phoneNumber;

    private final ConsoleClient consoleClient;
    private static final String SETTINGS_FILE_NAME = "number-list.txt";
    private final List<String> contacts = new ArrayList<>();

    public PhoneService(ConsoleClient consoleClient) {
        this.consoleClient = consoleClient;
        consoleClient.saveResource(SETTINGS_FILE_NAME, false);
        registerNumbers();
    }

    //TODO: implement ExecutorService for machines with more than 3 cores
    public void sendMessage(String target, String content) {
        consoleClient.log("Sending message to \"" + target + "\" with content: " + content);
        Message message = Message.creator(
                new PhoneNumber(target),
                new PhoneNumber(phoneNumber),
                content
        ).create();
    }

    public void sendMessage(String target) {
        consoleClient.log("Sending message to \"" + target + "\" with default content.");
        Message message = Message.creator(
                new PhoneNumber(target),
                new PhoneNumber(phoneNumber),
                "WEEWOO! Your base is being raided. Get online!"
        ).create();
    }

    public void call(String target, String message) {
        Call call = Call.creator(
                new PhoneNumber(target),
                new PhoneNumber(phoneNumber),
                new Twiml("<Response><Say>" + message + "</Say></Response>")
        ).create();
    }

    public void call(String target) {
        Call call = Call.creator(
                new PhoneNumber(target),
                new PhoneNumber(phoneNumber),
                new Twiml("<Response><Say>You are being raided. Get on to check up on your base.</Say></Response>")
        ).create();
    }

    private void registerNumbers() {
        BufferedReader bufferedReader;
        try {
            bufferedReader = new BufferedReader(new FileReader(SETTINGS_FILE_NAME));
            String line = bufferedReader.readLine();
            while(line != null) {
                contacts.add(line);
                line = bufferedReader.readLine();
            }
        } catch (Exception exception) {
            consoleClient.getLogger().log(Level.WARNING, "unable to read number-list.txt");
        }
    }

    public void init() {
        Twilio.init(sid, authToken);
    }

    public List<String> getContacts() {
        return contacts;
    }
}
