/*
 * <LICENSEHEADER/>
 *
 */
package com.buzzdavidson.spork.webdav;

import com.buzzdavidson.spork.constant.OWAConstants;
import java.io.IOException;
import lombok.Data;
import lombok.ToString;
import org.apache.commons.httpclient.HttpConnection;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.methods.EntityEnclosingMethod;

/**
 * This class implements a basic WebDAV method object for HttpClient.
 *
 * @author steve
 */
@Data
@ToString(includeFieldNames = true)
public class WebDavMethod extends EntityEnclosingMethod {

  private String[] headerNames;
  private String[] headerValues;
  private String name;

  public WebDavMethod(String path, String name, String[] headers) {
    super(path);

    this.name = name;

    headerNames = new String[headers.length];
    headerValues = new String[headers.length];

    for (int i = 0; i < headers.length; ++i) {
      int colon = headers[i].indexOf(':');
      headerNames[i] = headers[i].substring(0, colon);
      headerValues[i] = headers[i].substring(colon + 1);
    }
  }

  public WebDavMethod(String path, String name) {
    this(path, name, null);
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public void addRequestHeaders(HttpState st, HttpConnection cn)
          throws IOException, HttpException {
    if (getRequestHeader(OWAConstants.PARAM_CONTENT_TYPE) == null) {
      setRequestHeader(OWAConstants.PARAM_CONTENT_TYPE, OWAConstants.CONTENT_TYPE_XML);
    }

    super.addRequestHeaders(st, cn);

    for (int i = 0; i < headerNames.length; ++i) {
      setRequestHeader(headerNames[i], headerValues[i]);
    }
  }
}
