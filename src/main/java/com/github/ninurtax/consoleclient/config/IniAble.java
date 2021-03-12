package com.github.ninurtax.consoleclient.config;

public interface IniAble {

    /**
     * Example: Test
     * will output:
     * [Test]
     * key1=val1
     * key2=val2
     *
     * @return the head line of a ini section
     */
    String getHeadLine();

}
