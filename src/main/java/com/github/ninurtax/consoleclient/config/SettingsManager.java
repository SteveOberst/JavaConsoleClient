package com.github.ninurtax.consoleclient.config;

import com.github.ninurtax.consoleclient.ConsoleClient;
import com.github.ninurtax.consoleclient.modules.Module;
import org.ini4j.Ini;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

public class SettingsManager {

    public static final String SETTINGS_FILE_NAME = "ConsoleClient.ini";
    private static final List<? extends Class<? extends Serializable>> allowedTypes = Arrays.asList(Integer.class, int.class,
            Boolean.class, boolean.class, String.class);
    private final ConsoleClient consoleClient;
    private Ini ini = null;

    public SettingsManager(ConsoleClient consoleClient) {
        this.consoleClient = consoleClient;
        try {
            loadIni();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        try {
            start();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private void loadIni() throws IOException {
        consoleClient.saveResource(SETTINGS_FILE_NAME, false);
        File file = new File(SETTINGS_FILE_NAME);
        ini = new Ini(file);
        ini.load();
    }

    private void start() throws IllegalAccessException {
        for (Module module : consoleClient.getModuleManager().getModuleList()) {
            Field[] declaredFields = module.getClass().getDeclaredFields();
            Field[] declaredSuperClassFields = module.getClass().getSuperclass().getDeclaredFields();
            setFieldsForModule(module, declaredSuperClassFields);
            setFieldsForModule(module, declaredFields);
        }
    }

    private void setFieldsForModule(Module module, Field[] declaredFields) throws IllegalAccessException {
        for (Field declaredField : declaredFields) {
            if (declaredField.isAccessible()) {
                consoleClient.log(Level.WARNING, "Couldn't read field "
                        + declaredField.getName() + " in Module: " + module.getName()
                        + " \n Reason: Not accessible (Must be public)");
                continue;
            }

            boolean hasAnnotation = declaredField.getDeclaredAnnotation(LoadByIni.class) != null;
            if (!hasAnnotation) continue;
            if (!allowedTypes.contains(declaredField.getType())) {
                consoleClient.log(Level.WARNING, "Couldn't read field "
                        + declaredField.getName() + " in Module: " + module.getName()
                        + " \n Reason: Unacceptable field type: " +declaredField.getType());
                continue;
            }

            Object s = ini.get(module.getName(), declaredField.getName(), declaredField.getType());
            if (s != null)
                declaredField.set(module, s);
        }
    }
}