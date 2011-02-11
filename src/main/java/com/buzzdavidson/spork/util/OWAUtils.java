/*
 * <LICENSEHEADER/>
 *
 */
package com.buzzdavidson.spork.util;

import com.buzzdavidson.spork.constant.OWAConstants;
import com.ettrema.mail.MailboxAddress;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import javax.mail.Address;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Node;

/**
 *
 * @author sdavidson
 */
public class OWAUtils {

  private static final Log logger = LogFactory.getLog(OWAUtils.class);
  private static Map<String, String> HEADER_MAP = null;
  private static Map<String, Pattern[]> PATTERN_MAP = null;

  static {
    Map<String, String> dmap = new HashMap<String, String>();
    dmap.put("return-path", "Return-Path");
    dmap.put("content-type", "Content-Type");
    dmap.put("mime-version", "MIME-Version");
    dmap.put("message-id", "Message-Id");
    dmap.put("received", "Received");
    dmap.put("x-mailer", "X-Mailer");
    dmap.put("from", "From");
    dmap.put("reply-to", "Reply-To");
    dmap.put("subject", "Subject");
    dmap.put("content-transfer-encoding", "Content-Transfer-Encoding");
    dmap.put("to", "To");
    dmap.put("cc", "Cc");
    dmap.put("bcc", "Bcc");
    dmap.put("date", "Date");
    dmap.put("sender", "Sender");
    HEADER_MAP = Collections.unmodifiableMap(dmap);
    Map<String, Pattern[]> pmap = new HashMap<String, Pattern[]>();
    for (Map.Entry<String, String> entry : HEADER_MAP.entrySet()) {
      String key = entry.getKey();
      Pattern p = Pattern.compile("^.*[:]" + key + "$");
      pmap.put(key, new Pattern[]{OWAConstants.OWA_PATTERN_PROPSTAT, OWAConstants.OWA_PATTERN_PROP, p});
    }
    PATTERN_MAP = Collections.unmodifiableMap(pmap);
  }

  public static MimeMessage toMimeMessage(Node mailNode) {
    MimeMessage retval = new MimeMessage((Session) null);
    try {
      populateHeader("return-path", mailNode, retval);
      populateHeader("content-type", mailNode, retval);
      populateHeader("mime-version", mailNode, retval);
      populateHeader("message-id", mailNode, retval);
      populateReceived(mailNode, retval);
      populateHeader("x-mailer", mailNode, retval);
      populateAddresses("from", mailNode, retval);
      populateAddresses("reply-to", mailNode, retval);
      populateHeader("subject", mailNode, retval);
      populateHeader("content-transfer-encoding", mailNode, retval);
      populateAddresses("to", mailNode, retval);
      populateAddresses("cc", mailNode, retval);
      populateAddresses("bcc", mailNode, retval);
      populateHeader("date", mailNode, retval);
      populateHeader("sender", mailNode, retval);
    } catch (MessagingException ex) {
      logger.error("Messaging Exception", ex);
    }
    return retval;
  }

  public static String getMessageURL(Node mailNode) {
    String url = null;
    Node urlNode = XmlUtils.getChild(mailNode, OWAConstants.OWA_FILTER_EMAIL_PATH);
    if (urlNode != null) {
      url = urlNode.getTextContent();
      // Exchange uses several characters in URL's which
      // httpclient (or standards) don't like so
      // we'll replace them with hex reference
      url = url.replaceAll("\\[", "%5B");
      url = url.replaceAll("\\]", "%5D");
      url = url.replaceAll("\\|", "%7C");
      url = url.replaceAll("\\^", "%5E");
      url = url.replaceAll("\\`", "%60");
      url = url.replaceAll("\\{", "%7B");
      url = url.replaceAll("\\}", "%7D");
    }
    return url;
  }

  protected static Address toAddress(String addr) {
    Address retval = null;
    MailboxAddress mba = MailboxAddress.parse(addr);
    if (mba != null) {
      retval = mba.toInternetAddress();
    }
    return retval;
  }

  private static String fetchText(Node mailNode, Pattern[] path, boolean decode) {
    String retval = null;
    Node node = XmlUtils.getChild(mailNode, path);
    if (node != null) {
      retval = node.getTextContent();
    }
    if (retval != null && decode) {
      try {
        logger.info("-->decoding: " + retval);
        retval = URLDecoder.decode(retval, "UTF-8"); // TODO: determine charset? Headers should be all UTF-8
      } catch (UnsupportedEncodingException ex) {
        logger.error("Unsupported Encoding Exception", ex);
      }
    }
    return retval;
  }

  private static void populateHeader(String key, Node mailNode, MimeMessage message) throws MessagingException {
    String data = fetchText(mailNode, PATTERN_MAP.get(key), false);
    if (data != null && data.length() > 0) {
      message.setHeader(HEADER_MAP.get(key), data);
    }
  }

  private static void populateReceived(Node mailNode, MimeMessage message) throws MessagingException {
    String data = fetchText(mailNode, PATTERN_MAP.get("received"), false);
    if (data != null && data.length() > 0) {
      for (String line : splitReceived(data)) {
        message.addHeader(HEADER_MAP.get("received"), line);
      }
    }
  }

  private static void populateAddresses(String key, Node mailNode, MimeMessage message) throws MessagingException {
    populateHeader(key, mailNode, message);
  }

  protected static List<String> splitReceived(String data) {
    List<String> retval = new ArrayList<String>();
    if (data != null) {
      String remainder = data.trim();
      String adder;
      int i = remainder.indexOf(OWAConstants.NEWLINE);
      while (i >= 0) {
        adder = remainder.substring(0, i).trim();
        if (adder.length() > 0) {
          retval.add(adder);
        }
        remainder = remainder.substring(i + 2).trim();
        i = remainder.indexOf(OWAConstants.NEWLINE);
      }
      adder = remainder.trim();
      if (adder.length() >= 1) {
        retval.add(adder);
      }
    }
    return retval;
  }

  public static void populateMetadata(MimeMessage message, Node props) {
    Long length = null;
    String lengthStr = fetchText(props, OWAConstants.OWA_FILTER_CONTENT_LENGTH, false);
    if (lengthStr != null) {
      try {
        length = Long.parseLong(lengthStr);
        logger.info("Got content length: " + length);
      } catch (NumberFormatException ex) {
        // do nothing
      }
    }
    //This appears to be the exchange specific message id,
    //looks like      "AQUAAAABXRM/AQAAAAA6N1oAAAAA"
    String idStr = fetchText(props, OWAConstants.OWA_FILTER_ID, false);
    if (idStr != null) {
      try {
        String headerId = "";
        String[] ids = message.getHeader("Message-Id");
        if (ids.length > 0) {
          headerId = ids[0];
        }
        if (headerId.length() > 0) {
          if (!headerId.equals(idStr)) {
            logger.warn(String.format("******** ID values differ!  header id [%s], message id [%s]", headerId, idStr));
          }
        } else {
          message.setHeader("Message-Id", idStr);
        }
      } catch (MessagingException ex) {
        logger.warn("Unable to handle message id header");
      }
    }
    //TODO: attachment flag? Is this needed at all? owabridge uses it to tweak content-length
    if (length != null) {
      try {
        message.setHeader("Content-Length", length.toString());
      } catch (MessagingException ex) {
        logger.warn("Unable to set content-length header", ex);
      }
    }
  }

  public static String getSingleHeader(MimeMessage message, String header) {
    String retval = "";
    String[] headers = null;
    try {
      headers = message.getHeader(header);
    } catch (MessagingException ex) {
      logger.warn("Got exception attempting to fetch header [" + header + "]");
    }
    if (headers != null && headers.length > 0) {
      retval = headers[0];
    }
    return retval;
  }
}
