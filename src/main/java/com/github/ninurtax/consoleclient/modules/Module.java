package com.github.ninurtax.consoleclient.modules;

import com.github.ninurtax.consoleclient.config.IniAble;
import com.github.ninurtax.consoleclient.config.LoadByIni;

public abstract class Module implements IniAble {

    /**
     * Name of the module
     * *Can be the headline as well*
     */
    protected final String name;

    @LoadByIni
    public boolean enabled = false;

    public Module(String name) {
        this.name = name;
    }

    /**
     * called when starting this module
     */
    public void start() {}

    public boolean isEnabled() {
        return enabled;
    }

    public String getName() {
        return name;
    }

    /**
     * Using {@link #name}
     * Can be overwritten
     * @return head line name
     */
    @Override
    public String getHeadLine() {
        return name;
    }
}
