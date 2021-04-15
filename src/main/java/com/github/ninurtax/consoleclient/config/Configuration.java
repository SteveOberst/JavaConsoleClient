package com.github.ninurtax.consoleclient.config;

import lombok.Getter;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Configuration {
  @Getter
  private final String fileName;
  @Getter
  private final File file;

  public Configuration(String fileName) {
    this.fileName = fileName;
    file = new File(fileName);
  }

  public String getString(String property) {
    Properties properties = new Properties();

    try {
      InputStream in = new FileInputStream(file);
      properties.load(in);
      in.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return properties.getProperty(property);
  }

  public int getInt(String property) {
    Properties properties = new Properties();

    try {
      InputStream in = new FileInputStream(file);
      properties.load(in);
      in.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return Integer.valueOf(properties.getProperty(property));
  }

  public boolean getBoolean(String property) {
    Properties properties = new Properties();

    try {
      InputStream in = new FileInputStream(file);
      properties.load(in);
      in.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return Boolean.valueOf(properties.getProperty(property));
  }

  public long getLong(String property) {
    Properties properties = new Properties();

    try {
      InputStream in = new FileInputStream(file);
      properties.load(in);
      in.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return Long.valueOf(properties.getProperty(property));
  }
}
