/*
 * <LICENSEHEADER/>
 *
 */
package com.buzzdavidson.spork.constant;

import java.util.regex.Pattern;

/**
 * Constants for OWA implementation
 *
 * @author steve
 */
public interface OWAConstants {

  static final String BASE_TAG = "<BASE";
  static final String CONTENT_TYPE_TEXT_PLAIN = "text/plain";
  static final String CONTENT_TYPE_DEFAULT = CONTENT_TYPE_TEXT_PLAIN;
  static final String CONTENT_TYPE_HTML = "text/html";
  static final String CONTENT_TYPE_MULTIPART = "multipart/mixed";
  static final String CONTENT_TYPE_XML = "text/xml";
  static final String FOLDER_NAME_INBOX = "Inbox";
  static final String HEADER_LOCATION = "location";
  static final String NEWLINE = "\r\n";
  static final Integer OWA_FLAG_VALUE_PRIVATE = Integer.valueOf(4);
  static final Integer OWA_FLAG_VALUE_PUBLIC = Integer.valueOf(11);
  static final String[] OWA_HEADERS_BRIEF = new String[]{
    "Brief:t"
  };
  static final String[] OWA_HEADERS_MATCH = new String[]{
    "Brief:t",
    "If-Match:*"
  };
  static final String OWA_METHOD_DELETE = "BDELETE";
  static final String OWA_METHOD_PROP_FIND = "PROPFIND";
  static final String OWA_METHOD_PROP_PATCH = "BPROPPATCH";
  static final String OWA_METHOD_SEARCH = "SEARCH";
  static final String OWA_MSG_RESPONSE_SUFFIX = ":response";
  static final Pattern OWA_PATTERN_INBOX = Pattern.compile("^.*[:]inbox$");
  static final Pattern OWA_PATTERN_MULTISTAT = Pattern.compile("^.*[:]multistatus$");
  static final Pattern OWA_PATTERN_PROP = Pattern.compile("^.*[:]prop$");
  static final Pattern OWA_PATTERN_PROPSTAT = Pattern.compile("^.*[:]propstat$");
  static final Pattern OWA_PATTERN_RESPONSE = Pattern.compile("^.*[:]response$");
  static final String OWA_QUERY_DELETE_MESSAGE =
          "<?xml version=\"1.0\" encoding=\"utf-8\" ?>"
          + "<D:delete xmlns:D=\"DAV:\" "
          + "xmlns:a=\"urn:schemas:httpmail:\" "
          + "xmlns:T=\"urn:uuid:c2f41010-65b3-11d1-a29f-00aa00c14882/\">"
          + "<D:target>"
          + "<D:href>"
          + "%s"
          + "</D:href>"
          + "</D:target>"
          + "</D:delete>";
  static final String OWA_QUERY_GET_LIST_MAIL_MSG_ALL =
          "<?xml version=\"1.0\" encoding=\"utf-8\" ?>"
          + "<searchrequest xmlns=\"DAV:\">"
          + "<sql>\r\n"
          + "SELECT \"urn:schemas:httpmail:fromemail\", \"urn:schemas:httpmail:read\" \r\n"
          + "FROM \"\"\r\n"
          + "WHERE &quot;DAV:iscollection&quot; = False AND &quot;DAV:ishidden&quot; = False\r\n"
          + "ORDER BY \"DAV:creationdate\"\r\n"
          + "</sql>"
          + "</searchrequest>";
  static final String OWA_QUERY_GET_LIST_MAIL_MSG =
          "<?xml version=\"1.0\" encoding=\"utf-8\" ?>"
          + "<searchrequest xmlns=\"DAV:\">"
          + "<sql>\r\n"
          + "SELECT "
          + "\"urn:schemas:mailheader:return-path\", "
          + "\"urn:schemas:mailheader:content-type\", "
          + "\"urn:schemas:mailheader:mime-version\", "
          + "\"urn:schemas:mailheader:message-id\", "
          + "\"urn:schemas:mailheader:received\", "
          + "\"urn:schemas:mailheader:x-mailer\", "
          + "\"urn:schemas:mailheader:from\", "
          + "\"urn:schemas:mailheader:reply-to\", "
          + "\"urn:schemas:mailheader:subject\", "
          + "\"urn:schemas:mailheader:content-transfer-encoding\", "
          + "\"urn:schemas:mailheader:to\", "
          + "\"urn:schemas:mailheader:cc\", "
          + "\"urn:schemas:mailheader:bcc\", "
          + "\"urn:schemas:mailheader:date\", "
          + "\"urn:schemas:mailheader:sender\", "
          + "\"urn:schemas:httpmail:read\""
          + " \r\n"
          + "FROM \"\"\r\n"
          + "WHERE &quot;DAV:iscollection&quot; = False AND &quot;DAV:ishidden&quot; = False"
          + " AND \"urn:schemas:httpmail:read\"= False\r\n"
          + "ORDER BY \"DAV:creationdate\"\r\n"
          + "</sql>"
          + "</searchrequest>";
  static final String OWA_QUERY_MARK_AS_READ =
          "<?xml version=\"1.0\" encoding=\"utf-8\" ?>"
          + "<D:propertyupdate xmlns:D=\"DAV:\" "
          + "xmlns:a=\"urn:schemas:httpmail:\" "
          + "xmlns:T=\"urn:uuid:c2f41010-65b3-11d1-a29f-00aa00c14882/\">"
          + "<D:target>"
          + "<D:href>"
          + "%s"
          + "</D:href>"
          + "</D:target>"
          + "<D:set><D:prop>"
          + "<a:read>1</a:read>"
          + "</D:prop></D:set>"
          + "</D:propertyupdate>";
  static final String PARAM_CONTENT_LENGTH = "Content-Length";
  static final String PARAM_CONTENT_TYPE = "Content-Type";
  static final String PARAM_CONTENT_ENCODING = "Content-Transfer-Encoding";
  static final String PARAM_DEST = "destination";
  static final String PARAM_FLAGS = "flags";
  static final String PARAM_PWD = "password";
  static final String PARAM_UNAME = "username";
  static final String BASE_64_ENCODING = "base64";
  static final Pattern[] OWA_FILTER_CONTENT_LENGTH = new Pattern[]{
    Pattern.compile("^.*[:]getcontentlength$")
  };
  static final Pattern[] OWA_FILTER_EMAIL_PATH = new Pattern[]{
    Pattern.compile("^.*[:]href$")
  };
  static final Pattern[] OWA_FILTER_FIRST_MESSAGE_PATH = new Pattern[]{
    OWA_PATTERN_MULTISTAT,
    OWA_PATTERN_RESPONSE
  };
  static final Pattern[] OWA_FILTER_ID = new Pattern[]{
    Pattern.compile("^.*[:]id$")
  };
  static final Pattern[] OWA_FILTER_INBOX_PATH = new Pattern[]{
    OWA_PATTERN_MULTISTAT,
    OWA_PATTERN_RESPONSE,
    OWA_PATTERN_PROPSTAT,
    OWA_PATTERN_PROP,
    OWA_PATTERN_INBOX
  };
}
