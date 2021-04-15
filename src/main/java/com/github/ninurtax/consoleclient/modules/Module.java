package com.github.ninurtax.consoleclient.modules;

import com.github.ninurtax.consoleclient.config.IniAble;
import com.github.ninurtax.consoleclient.config.LoadByIni;
import lombok.Setter;

public abstract class Module implements IniAble {
    /**
     * Name of the module
     * *Can be the headline as well*
     */
    protected final String name;

    public Module(String name) {
        this.name = name;
    }

    /**
     * called when starting this module
     */
    public void start() {}

    public abstract boolean isEnabled();

    public String getName() {
        return name;
    }
}
