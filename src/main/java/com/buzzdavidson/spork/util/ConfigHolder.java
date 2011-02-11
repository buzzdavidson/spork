/*
 * <LICENSEHEADER/>
 * 
 */

package com.buzzdavidson.spork.util;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 *
 * @author sdavidson
 */
public class ConfigHolder {
  public static final String DEFAULT_AUTH_DOMAIN = "MYNTDOMAIN";
  public static final String DEFAULT_AUTH_HOST = "localhost";
  public static final String DEFAULT_BASE_URL = "exchange";
  public static final Integer DEFAULT_MAX_ITEMS = Integer.valueOf(25);
  public static final String DEFAULT_HOSTNAME = "myoutlookserver.com";
  public static final String DEFAULT_LOGIN_URL = "exchweb/bin/auth/owaauth.dll";
  public static final String DEFAULT_SCHEME = "https";
  public static final int DEFAULT_SERVERPORT = 8110;

  public static final String KEY_SERVERPORT="port";
  public static final String KEY_HOSTNAME="serverHost";
  public static final String KEY_LOGINURL="loginPath";
  public static final String KEY_SCHEME="scheme";
  public static final String KEY_BASEPATH="docBasePath";
  public static final String KEY_AUTHHOST="authHost";
  public static final String KEY_AUTHDOMAIN="authDomain";
  public static final String KEY_PUBLICCOMPUTER="publicComputer";
  public static final String KEY_FETCHLIMIT="itemFetchLimit";
  public static final String KEY_MARKREAD="markReadAfterFetch";
  public static final String KEY_DIAGNOSTICS="diagnostics";

  public static final Map<String,String>KEY_DOCUMENTATION = new LinkedHashMap<String, String>();

  static {
    KEY_DOCUMENTATION.put("General Configuration Entries - these MUST be configured", null);
    KEY_DOCUMENTATION.put(KEY_HOSTNAME, "Fully qualified hostname of OWA server");
    KEY_DOCUMENTATION.put(KEY_AUTHDOMAIN, "NT Domain name for authentication");
    KEY_DOCUMENTATION.put("Miscellaneous Configuration Entries - you probably don't need to change these", null);
    KEY_DOCUMENTATION.put(KEY_SERVERPORT, "Port for local POP server (Default " + DEFAULT_SERVERPORT + ")");
    KEY_DOCUMENTATION.put(KEY_PUBLICCOMPUTER, "Should be 'true' if public computer (controls OWA session timeout) (Default false)");
    KEY_DOCUMENTATION.put(KEY_FETCHLIMIT, "Maximum number of entries to fetch at one time (Default " + DEFAULT_MAX_ITEMS + ")");
    KEY_DOCUMENTATION.put(KEY_DIAGNOSTICS, "Include diagnostic footer in each message (Default false)");
    KEY_DOCUMENTATION.put("Advanced Configuration Entries - Don't change these unless you know what you're doing", null);
    KEY_DOCUMENTATION.put(KEY_SCHEME, "URL Scheme (http or https) (Default " + DEFAULT_SCHEME + ")");
    KEY_DOCUMENTATION.put(KEY_LOGINURL, "Path to login URL - check form 'action' parameter on outlook web login page source (Default " + DEFAULT_LOGIN_URL + ")");
    KEY_DOCUMENTATION.put(KEY_BASEPATH, "OWA base path (Default " + DEFAULT_BASE_URL + ")");
    KEY_DOCUMENTATION.put(KEY_AUTHHOST, "NT Authentication Host (Default " + DEFAULT_AUTH_HOST + ")");
    KEY_DOCUMENTATION.put(KEY_MARKREAD, "Mark messages read when retrieved (Default true) - leave this alone unless debugging");
  }

  private static Map<String,Object>entryMap = null;
  public static void initConfig(Map<String,Object>configMap) {
    if (entryMap != null) {
      throw new IllegalStateException("Configuration Holder Already Configured");
    }
    entryMap = Collections.unmodifiableMap(new HashMap<String, Object>(configMap));
  }

  public static Map<String,Object>getConfig() {
    return entryMap;
  }

  public static Object getConfigValue(String key, Object defaultValue) {
    Object retval = entryMap.get(key);
    if (key == null) {
      retval = defaultValue;
    }
    return retval;
  }

  public static Integer getConfigValueAsInt(String key, Integer defaultValue) {
    Integer retval = null;
    Object ob = getConfigValue(key, defaultValue);
    if (ob != null) {
      retval = Integer.parseInt(ob.toString());
    }
    return retval;
  }

  public static Boolean getConfigValueAsBoolean(String key, Boolean defaultValue) {
    Boolean retval = null;
    Object ob = getConfigValue(key, defaultValue);
    if (ob != null) {
      retval = Boolean.parseBoolean(ob.toString());
    }
    return retval;
  }

  public static String getConfigValueAsString(String key, String defaultValue) {
    return(String)getConfigValue(key, defaultValue);
  }

  public static Boolean containsKey(String key) {
    return entryMap.containsKey(key);
  }

  public static Object getConfigMap() {
    return entryMap;
  }

  public static Map<String, Object> getDefaultConfig() {
    Map<String, Object> retval = new HashMap<String, Object>();
    retval.put(KEY_SERVERPORT, DEFAULT_SERVERPORT);
    retval.put(KEY_HOSTNAME, DEFAULT_HOSTNAME);
    retval.put(KEY_LOGINURL, DEFAULT_LOGIN_URL);
    retval.put(KEY_SCHEME, DEFAULT_SCHEME);
    retval.put(KEY_BASEPATH, DEFAULT_BASE_URL);
    retval.put(KEY_AUTHHOST, DEFAULT_AUTH_HOST);
    retval.put(KEY_AUTHDOMAIN, DEFAULT_AUTH_DOMAIN);
    retval.put(KEY_PUBLICCOMPUTER, Boolean.FALSE);
    retval.put(KEY_FETCHLIMIT, DEFAULT_MAX_ITEMS);
    retval.put(KEY_MARKREAD, Boolean.TRUE);
    retval.put(KEY_DIAGNOSTICS, Boolean.FALSE);
    return retval;
  }

}
