/*
 * <LICENSEHEADER/>
 *
 */
package com.buzzdavidson.spork.client;

import com.buzzdavidson.spork.util.OWAUtils;
import com.buzzdavidson.spork.util.OWAMessageFilter;
import com.buzzdavidson.spork.util.XmlUtils;
import com.buzzdavidson.spork.webdav.WebDavMethod;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import org.apache.commons.httpclient.HttpClient;
import static com.buzzdavidson.spork.constant.OWAConstants.*;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;import org.apache.commons.codec.binary.Base64;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NTCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * Simple OWA Client implementation.
 *
 * NOTE that this is NOT a thread-safe implementation, as it stores state!
 *
 * @author steve
 */
@EqualsAndHashCode
@ToString(includeFieldNames = true, exclude = "owaRootUrl")
public class OWAClient {

  /**
   * FQDN of OWA host
   */
  @Setter @NonNull
  private String serverHost;
  /**
   * Path on host to OWA login page - view page source on login form for "action"
   * typically "exchweb/bin/auth/owaauth.dll"
   */
  @Setter @NonNull
  private String loginPath;
  /**
   * URI Scheme (http or https, defaults to https)
   */
  @Setter @NonNull
  private String scheme;
  /**
   * Base path to mailbox, typically just "exchange"
   */
  @Setter @NonNull
  private String docBasePath;
  /**
   * Host to use for NT domain authentication.
   * Defaults to "localhost" - probably best to leave this alone.
   */
  @Setter @NonNull
  private String authHost;
  /**
   * Authentication domain. NT logins are often referred to via [authDomain]\[userName]
   */
  @Setter @NonNull
  private String authDomain;
  /**
   * Is this a "public computer"?  Defaults to false.  Affects server-side session length
   * and timeout
   */
  @Setter @NonNull
  private boolean publicComputer = false;
  /**
   * Controls maximum number of items retrieved from server in one session
   */
  @Setter @NonNull
  private int itemFetchLimit;
  /**
   * if true, will mark each item read when it is retrieved
   */
  @Setter @NonNull
  private boolean markReadAfterFetch = true;

  /**
   * If true, will include diagnostic headers in messages
   */
  @Setter
  private boolean wantDiagnostics = false;

  // Dynamic items (configured via code)
  /**
   * Base uri for OWA server.  Defined at runtime from scheme + serverHost
   */
  private transient URI baseUri = null;
  /**
   * HTTP Client for communicating with WebDAV
   */
  private transient HttpClient client;
  /**
   * Full address to logged in user's inbox - determined at runtime after login
   * (OWA redirects to this page upon successful login)
   */
  private transient String inboxAddress = null;
  /**
   * Root URL for WebDAV.  Determined at runtime.
   */
  private transient String owaRootUrl = null;
  private transient final Log logger = LogFactory.getLog(this.getClass());

  /**
   * Default constructor
   */
  public OWAClient() {
    client = new HttpClient();
  }

  /**
   * Assembles the base uri from scheme + serverHost
   * @return base URI
   */
  private URI getBaseUri() {
    if (baseUri == null) {
      try {
        baseUri = new URI(scheme, serverHost, "/", null);
      } catch (URISyntaxException ex) {
        throw new RuntimeException("Unable to create base URI", ex);
      }
    }
    return baseUri;
  }

  /**
   * Retrieve string representation of base uri
   * @return base URI as string
   */
  private String getBaseUrl() {
    return getBaseUri().toString();
  }

  /**
   * Perform login to OWA server given configuration parameters and supplied user/password
   *
   * @param userName  user name for login
   * @param password password for login
   * @return true if successfully authenticated
   */
  public boolean login(String userName, String password) {
    /**
     * Set NT credentials on client
     */
    Credentials cred;
    cred = new NTCredentials(userName, password, authHost, authDomain);
    client.getState().setCredentials(new AuthScope(authHost, AuthScope.ANY_PORT), cred);

    String authUrl = getBaseUrl() + loginPath;
    boolean retval = false;
    logger.info(String.format("Logging in to OWA using username [%s] at URL [%s] ", userName, authUrl));
    PostMethod post = new PostMethod(authUrl);
    post.setRequestHeader(PARAM_CONTENT_TYPE, PostMethod.FORM_URL_ENCODED_CONTENT_TYPE);
    post.addParameter(PARAM_DEST, getBaseUrl() + docBasePath);
    post.addParameter(PARAM_UNAME, userName);
    post.addParameter(PARAM_PWD, password);
    post.addParameter(PARAM_FLAGS, publicComputer ? OWA_FLAG_VALUE_PUBLIC.toString() : OWA_FLAG_VALUE_PRIVATE.toString());

    int status = 0;
    try {
      status = client.executeMethod(post);
      if (logger.isDebugEnabled()) {
        logger.debug("Post returned status code " + status);
      }
    } catch (Exception ex) {
      logger.error("Got error posting to login url", ex);
    }

    if (status == HttpStatus.SC_OK) {
      // We shouldn't see this in normal operation... Evaluate whether this actually ever occurs.
      logger.info("Login succeeded (no redirect specified)");
      retval = true;
    } else if (status == HttpStatus.SC_MOVED_TEMPORARILY) {
      // We typically receive a redirect upon successful authentication; the target url is the user's inbox
      Header locationHeader = post.getResponseHeader(HEADER_LOCATION);
      if (locationHeader != null) {
        String mbxRoot = getMailboxRoot(locationHeader.getValue());
        if (mbxRoot == null || mbxRoot.length() < 1) {
          logger.info("Login failed - Check configuration and credentials.");
          retval = false;
        } else {
          owaRootUrl = mbxRoot;
          inboxAddress = owaRootUrl + "/" + FOLDER_NAME_INBOX; // TODO is this sufficient?
          logger.info("Login succeeded, redirect specified (redirecting to " + owaRootUrl + ")");
          retval = true;
        }
      }
    } else {
      logger.error("Login failed with error code " + status);
    }
    return retval;
  }

  /**
   * Determine the mailbox root by fetching the page at the supplied URL and examining
   * the subsequent page for the <Base tag - this value becomes the mailbox root.
   *
   * @param url URL to fetch page from
   * @return mailbox root or null if not found
   */
  private String getMailboxRoot(String url) {
    String retval = null;
    GetMethod get = new GetMethod(url);
    get.setFollowRedirects(true);
    get.setDoAuthentication(true);
    try {
      logger.info("Retrieving mailbox root via url " + url);
      int status = client.executeMethod(get);
      if (logger.isDebugEnabled()) {
        logger.info("Mailbox root retrieval returned status code " + status);
      }
      if (status == HttpStatus.SC_OK) {
        String line;
        BufferedReader bodyReader = new BufferedReader(
                new InputStreamReader(
                get.getResponseBodyAsStream()));

        while ((line = bodyReader.readLine()) != null) {
          int pos = (line.toUpperCase().indexOf(BASE_TAG));
          if (pos >= 0) {
            retval = snagMailboxRoot(line);
            break;
          }
        }
      }
    } catch (HttpException ex) {
      logger.error("Received HttpException retrieving mailbox root", ex);
    } catch (UnsupportedEncodingException ex) {
      logger.error("Received UnsupportedEncodingException retrieving mailbox root", ex);
    } catch (IOException ex) {
      logger.error("Received IOException retrieving mailbox root", ex);
    }
    if (retval != null && retval.length() > 0) {
      logger.info("Found mailbox root [" + retval + "]");
    }
    return retval;
  }

  /**
   * Retrieve mailbox root from supplied value.  Expects the line containing "<BASE" tag,
   * looks for a quoted string for the value.
   *
   * @param value value to parse
   * @return mailbox root
   */
  private String snagMailboxRoot(String value) {
    int i1 = value.indexOf('"');
    int i2 = value.lastIndexOf('"');
    if (i1 > 0 && i2 > 0 && i2 > i1) {
      return value.substring(i1 + 1, i2 - 1);
    } else {
      return null;
    }
  }

  /**
   * Retrieve list of messages via OWA/WebDAV
   *
   * @param filter type of list to retrieve (new or all messages)
   * @return list of mime messages (never null)
   */
  public List<MimeMessage> fetchMail(OWAMessageFilter filter) {
    List<MimeMessage> retval = new ArrayList<MimeMessage>();
    try {
      WebDavMethod search = new WebDavMethod(inboxAddress, OWA_METHOD_SEARCH, OWA_HEADERS_BRIEF);
      String msg = "";
      if (OWAMessageFilter.ALL_MESSAGES.equals(filter)) {
        msg = OWA_QUERY_GET_LIST_MAIL_MSG_ALL;
      } else {
        msg = OWA_QUERY_GET_LIST_MAIL_MSG;
      }
      logger.info(String.format("Requesting %s mail messages", OWAMessageFilter.ALL_MESSAGES.equals(filter) ? "all" : "new"));
      search.setRequestEntity(new StringRequestEntity(msg, null, null));
      int status = client.executeMethod(search);
      if (logger.isDebugEnabled()) {
        logger.info("Message request returned status code [" + status + "]");
      }
      if (status == HttpStatus.SC_MULTI_STATUS) {
        Document doc = XmlUtils.readResponseDocument(search);
        Node email = XmlUtils.getChild(doc, OWA_FILTER_FIRST_MESSAGE_PATH);
        int msgCount;
        for (msgCount = 0; email != null; email = email.getNextSibling()) {
          msgCount++;
          if (msgCount > itemFetchLimit) {
            break;
          } else {
            String nodeName = email.getNodeName();
            if (nodeName.endsWith(OWA_MSG_RESPONSE_SUFFIX)) {
              MimeMessage newMessage = OWAUtils.toMimeMessage(email);
              if (newMessage != null) {
                String messageUrl = OWAUtils.getMessageURL(email);
                populateMessage(newMessage, messageUrl);
                if (markReadAfterFetch) {
                  markMessageRead(messageUrl);
                }
                retval.add(newMessage);
              }
            }
          }
        }
      }
      return retval;
    } catch (HttpException ex) {
      logger.error("Received HttpException fetching mail", ex);
    } catch (UnsupportedEncodingException ex) {
      logger.error("Received UnsupportedEncodingException fetching mail", ex);
    } catch (IOException ex) {
      logger.error("Received IOException fetching mail", ex);
    }
    return retval;
  }

  public void markMessageRead(String messageUrl) {
    String filename = messageUrl.substring(messageUrl.lastIndexOf("/") + 1);
    String query = String.format(OWA_QUERY_MARK_AS_READ, filename);
    WebDavMethod setRead = new WebDavMethod(inboxAddress + "/", OWA_METHOD_PROP_PATCH, OWA_HEADERS_MATCH);
    try {
      setRead.setRequestEntity(new StringRequestEntity(query, null, null));
      logger.info("Marking message as read [" + filename + "]");
      int status = client.executeMethod(setRead);
      if (logger.isDebugEnabled()) {
        logger.debug("Message request returned status code [" + status + "]");
      }
    } catch (HttpException ex) {
      logger.error("Received HttpException fetching mail", ex);
    } catch (UnsupportedEncodingException ex) {
      logger.error("Received UnsupportedEncodingException fetching mail", ex);
    } catch (IOException ex) {
      logger.error("Received IOException fetching mail", ex);
    }
  }

  public boolean removeMessage(String messageUrl) {
    boolean retval = false;
    String filename = messageUrl.substring(messageUrl.lastIndexOf("/") + 1);
    String query = String.format(OWA_QUERY_DELETE_MESSAGE, filename);
    WebDavMethod del = new WebDavMethod(inboxAddress + "/", OWA_METHOD_DELETE, OWA_HEADERS_MATCH);
    try {
      del.setRequestEntity(new StringRequestEntity(query, null, null));
      logger.info("Deleting message [" + filename + "]");
      int status = client.executeMethod(del);
      retval = (status == HttpStatus.SC_OK);
      if (logger.isDebugEnabled()){
        logger.debug("Message request returned status code [" + status + "]");
      }
    } catch (HttpException ex) {
      logger.error("Received HttpException fetching mail", ex);
    } catch (UnsupportedEncodingException ex) {
      logger.error("Received UnsupportedEncodingException fetching mail", ex);
    } catch (IOException ex) {
      logger.error("Received IOException fetching mail", ex);
    }
    return retval;
  }

  /**
   * Fetch the body of the email message given a message URL.
   *
   * @param message Mime message to add body to
   * @param messageUrl URL of message (OWA WebDav)
   */
  private void populateMessage(MimeMessage message, String messageUrl) {
    GetMethod get = new GetMethod(messageUrl);
    get.setRequestHeader("Translate", "F");
    try {
      logger.info("Requesting message body via url [" + messageUrl + "]");
      int status = client.executeMethod(get);
      if (logger.isDebugEnabled()) {
        logger.debug("Message request returned status code [" + status + "]");
      }
      if (status == HttpStatus.SC_OK) {
        BufferedReader in = new BufferedReader(new InputStreamReader(get.getResponseBodyAsStream()));
        String contentType = OWAUtils.getSingleHeader(message, PARAM_CONTENT_TYPE);
        String contentEncoding = OWAUtils.getSingleHeader(message, PARAM_CONTENT_ENCODING);
        boolean needContentType = (contentType == null || contentType.length() < 1);

        // first set of entries are headers, followed by empty line.  skip to first empty line.
        for (String line; (line = in.readLine()) != null;) {
          if (line != null && line.trim().length() == 0) {
            break;
          }
        }

        StringBuffer buf = new StringBuffer();
        for (String line; (line = in.readLine()) != null;) {
          buf.append(line);
          buf.append(NEWLINE);
        }

        if (needContentType || contentType.contains(CONTENT_TYPE_TEXT_PLAIN)) {
          // OWA sometimes reports XML or HTML content as "text/plain"; check for this.
          
          if (logger.isDebugEnabled()) {
            logger.debug("checking content encoding");
          }
          byte[] blob = buf.toString().getBytes();
          if (logger.isDebugEnabled()) {
            logger.debug("blob length is " + blob.length + " bytes");
          }
          if (contentEncoding.equals(BASE_64_ENCODING)) {
            if (logger.isDebugEnabled()) {
              logger.debug("blob is base64 encoded, decoding");
            }
            byte[] newblob = Base64.decodeBase64(blob);
            if (logger.isDebugEnabled()) {
              logger.debug("decoded blob length is " + newblob.length + " bytes");
            }
            String data = new String(newblob);
            buf = new StringBuffer(data);
          } else {
            if (logger.isDebugEnabled()) {
              logger.debug("blob is encoded with [" + contentEncoding + "], not decoding");
            }
          }
          contentType = determineContentType(buf, message);
        }

        if (wantDiagnostics) {
          buf.append(NEWLINE);
          buf.append("-------------------------------------");
          buf.append(NEWLINE);
          buf.append("SPORK Data");
          buf.append(NEWLINE);
          buf.append("content-encoding: " + contentEncoding);
          buf.append(NEWLINE);
          buf.append("content-type: " + contentType);
          if (needContentType) {
            buf.append(" (calculated) " + contentType);
            buf.append(" Message processed " + DateFormat.getDateInstance(DateFormat.SHORT).format(new Date()));
          }
        }

        if (needContentType) {
          buf = cleanupBuffer(buf);
        }

        buf.append(NEWLINE);
        message.setText(buf.toString());
        int len = buf.length();
        message.setHeader(PARAM_CONTENT_LENGTH, Integer.valueOf(len).toString()); // close enough :)
        if (contentType != null && contentType.length() > 0) {
          message.setHeader(PARAM_CONTENT_TYPE, contentType);
        }
        message.saveChanges();
      }
    } catch (HttpException ex) {
      logger.error("Received HttpException fetching message body", ex);
    } catch (UnsupportedEncodingException ex) {
      logger.error("Received UnsupportedEncodingException fetching message body", ex);
    } catch (IOException ex) {
      logger.error("Received IOException fetching message body", ex);
    } catch (MessagingException ex) {
      logger.error("Received MessagingException fetching message body", ex);
    }
  }

  private String determineContentType(StringBuffer buffer, MimeMessage message) {
    if (buffer.indexOf("_=_NextPart_") >= 0) {
      return CONTENT_TYPE_MULTIPART;
    }
    if (buffer.indexOf("This is a multi-part message in MIME format") >= 0) {
      return CONTENT_TYPE_MULTIPART;
    }
    if (buffer.indexOf("<html") >= 0 || buffer.indexOf("<HTML") >= 0 || buffer.indexOf("<!DOCTYPE HTML") >= 0) {
      return CONTENT_TYPE_HTML + "; charset=UTF-8";
    }
    if (buffer.indexOf("<?xml") >= 0) {
      return CONTENT_TYPE_XML + "; charset=UTF-8";
    }
    return CONTENT_TYPE_DEFAULT;
  }

  private StringBuffer cleanupBuffer(StringBuffer buffer) {
    // we seem to get odd characters in messages with no specific content-type
    // TODO: how can we handle these properly?
    String[] STRIP_SEQ = { "=20=", "=20", "=0A=", "=0A", "=3D", " = ", " = ", "=" };
    String outbuf = buffer.toString();
    for (String seq: STRIP_SEQ) {
      outbuf = outbuf.replaceAll(seq, "");
    }
    return new StringBuffer(outbuf);
  }

}
