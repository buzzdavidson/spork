/*
 * <LICENSEHEADER/>
 *
 */
package com.buzzdavidson.spork;

import com.buzzdavidson.spork.geroa.MinaLocalhostPopServer;
import com.buzzdavidson.spork.geroa.OWAMailResourceFactory;
import com.buzzdavidson.spork.util.ConfigHolder;
import com.ettrema.mail.Filter;

import com.ettrema.mail.pop.PopServer;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Main class for Spork server
 * 
 * @author steve
 */
public class SporkServer {

  /**
   * @param args the command line arguments
   */
  public static void main(String[] args) {
    if (args.length < 1) {
      showHelp();
    } else {
      Map<String, Object> configuration = ConfigHolder.getDefaultConfig();
      loadParams(configuration, args);
      ConfigHolder.initConfig(configuration);
      System.out.println("Current Configuration\n" + ConfigHolder.getConfigMap().toString());
      int port = ConfigHolder.getConfigValueAsInt(ConfigHolder.KEY_SERVERPORT, null);
      ArrayList filterList = new ArrayList<Filter>();
      PopServer popServer = new MinaLocalhostPopServer(port, new OWAMailResourceFactory(), filterList);
      System.out.println("Starting new POP3 server on port " + popServer.getPopPort());
      popServer.start();
    }
  }

  private static void loadParams(Map<String, Object> configuration, String[] args) {
    Map<String, String> argMap = new HashMap<String, String>();
    String currentKey = null;
    for (String arg : args) {
      if (arg.startsWith("-")) {
        if (currentKey != null) {
          // two contiguous switches: store in map with null value
          argMap.put(currentKey, null);
          currentKey = null;
        } else {
          // key becomes value after leading dash
          currentKey = arg.substring(1);
        }
      } else {
        if (currentKey != null) {
          argMap.put(currentKey, arg);
          currentKey = null;
        } else {
          // this is undefined - two contiguous entries without leading dash
        }
      }
    }
    // Pick up any trailing switch-only values
    if (currentKey != null) {
      argMap.put(currentKey, null);
    }

    boolean loadSwitches = true;

    // handle "special" params
    if (argMap.containsKey("help")) {
      showHelp();
    } else if (argMap.containsKey("writeConfig")) {
      String fileName = (String) argMap.get("writeConfig");
      if (fileName == null) {
        throw new IllegalArgumentException("-writeConfig switch requires configuration file path");
      } else {
        saveConfigToFile(fileName, configuration);
        loadSwitches = false;
      }
    } else if (argMap.containsKey("config")) {
      String fileName = (String) argMap.get("config");
      if (fileName == null) {
        throw new IllegalArgumentException("-config switch requires configuration file path");
      } else {
        loadConfigFromFile(fileName, configuration);
        loadSwitches = false;
      }
    }

    if (loadSwitches) {
      for (Map.Entry<String, Object> entry : configuration.entrySet()) {
        String key = entry.getKey();
        Object value = argMap.get(key);
        if (value != null) {
          configuration.put(key, value);
        }
      }
    }

  }

  private static void loadConfigFromFile(String fileName, Map<String, Object> configuration) {
    System.out.println("Loading configuration from file: " + fileName);
    Properties properties = new Properties();
    try {
      int i = 0;
      properties.load(new FileInputStream(fileName));
      for (Map.Entry<String, Object> entry : configuration.entrySet()) {
        String key = entry.getKey();
        if (properties.containsKey(key)) {
          entry.setValue(properties.getProperty(key));
          i++;
        }
      }
      if (i > 0) {
        System.out.println("Successfully loaded " + i + " properties from configuration file");
      }
    } catch (IOException ex) {
      ex.printStackTrace();
    }
  }

  private static void saveConfigToFile(String fileName, Map<String, Object> configuration) {
    File f = new File(fileName);
    if (f.exists()) {
      f.delete();
    }
    try {
      FileWriter outFile = new FileWriter(fileName);
      PrintWriter out = new PrintWriter(outFile);
      try {
        out.println("# SPORK configuration file");
        out.println("# Created by writeconfig " + DateFormat.getDateInstance(DateFormat.SHORT).format(new Date()));
        for (Map.Entry<String, String> entry : ConfigHolder.KEY_DOCUMENTATION.entrySet()) {
          if (entry.getValue() == null) {
            out.println(getHeader(entry.getKey()));
          } else {
            out.println("# " + entry.getValue());
            out.println(entry.getKey() + "=" + configuration.get(entry.getKey()).toString() + "\n");
          }
        }
        System.out.println("Successfully wrote file " + fileName);
        out.flush();
      } finally {
        out.close();
      }
    } catch (IOException ex) {
      System.err.println("Unable to create new file: " + ex);
    }
  }

  private static String getHeader(String val) {
    StringBuffer buf = new StringBuffer();
    int i;
    for (i = 0; i < val.length(); i++) {
      buf.append("-");
    }
    buf.append("\n");
    buf.append(val);
    buf.append("\n");
    for (i = 0; i < val.length(); i++) {
      buf.append("-");
    }
    return buf.toString();
  }

  private static void showHelp() {
    System.err.println("Usage: spork.jar [options]");
    System.err.println("\nWhere [options] are either:");
    System.err.println("\t-help                Application Usage");
    System.err.println("\t-writeConfig [file]  Write a new configuration file and exit");
    System.err.println("\t-config [file]       Load configuration from file");
    System.err.println("\nOr a combination of the following:");
    for (Map.Entry<String, String> entry : ConfigHolder.KEY_DOCUMENTATION.entrySet()) {
      if (entry.getValue() == null) {
        System.err.println(getHeader(entry.getKey()));
      } else {
        System.err.println("\t-" + fixspace(entry.getKey()) + entry.getValue());
      }
    }
  }

  private static String fixspace(String key) {
    int pad = 20 - key.length();
    StringBuffer buf = new StringBuffer();
    buf.append(key);
    buf.append(" [value]");
    for (int i = 0; i < pad; i++) {
      buf.append(" ");
    }
    return buf.toString();
  }
}
