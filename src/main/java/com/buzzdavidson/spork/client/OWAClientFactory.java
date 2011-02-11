/*
 * <LICENSEHEADER/>
 * 
 */

package com.buzzdavidson.spork.client;

import com.buzzdavidson.spork.util.ConfigHolder;


/**
 *
 * @author sdavidson
 */
public class OWAClientFactory {
  public static OWAClient getClient() {
    OWAClient retval = new OWAClient();
    retval.setAuthDomain(ConfigHolder.getConfigValueAsString(ConfigHolder.KEY_AUTHDOMAIN, null));
    retval.setAuthHost(ConfigHolder.getConfigValueAsString(ConfigHolder.KEY_AUTHHOST, null));
    retval.setDocBasePath(ConfigHolder.getConfigValueAsString(ConfigHolder.KEY_BASEPATH, null));
    retval.setItemFetchLimit(ConfigHolder.getConfigValueAsInt(ConfigHolder.KEY_FETCHLIMIT, null));
    retval.setLoginPath(ConfigHolder.getConfigValueAsString(ConfigHolder.KEY_LOGINURL, null));
    retval.setMarkReadAfterFetch(ConfigHolder.getConfigValueAsBoolean(ConfigHolder.KEY_MARKREAD, null));
    retval.setPublicComputer(ConfigHolder.getConfigValueAsBoolean(ConfigHolder.KEY_PUBLICCOMPUTER, null));
    retval.setScheme(ConfigHolder.getConfigValueAsString(ConfigHolder.KEY_SCHEME, null));
    retval.setWantDiagnostics(ConfigHolder.getConfigValueAsBoolean(ConfigHolder.KEY_DIAGNOSTICS, Boolean.TRUE));
    retval.setServerHost(ConfigHolder.getConfigValueAsString(ConfigHolder.KEY_HOSTNAME, null));
    return retval;
  }

}
