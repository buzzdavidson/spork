/*
 * <LICENSEHEADER/>
 *
 */
package com.buzzdavidson.spork.geroa;

import com.buzzdavidson.spork.client.OWAClient;
import com.buzzdavidson.spork.util.OWAMessageFilter;
import com.ettrema.mail.MessageFolder;
import com.ettrema.mail.MessageResource;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.mail.internet.MimeMessage;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * OWA Implementation of geroa MessageFolder
 * @author steve
 */
public class OWAMessageFolder implements MessageFolder {

  private final Log logger = LogFactory.getLog(this.getClass());
  private OWAClient client;
  private String folderName;
  private Collection<MessageResource> messages = null;

  public OWAMessageFolder(OWAClient owaClient, String folderName) {
    // TODO: handle non-Inbox folders
    this.client = owaClient;
    this.folderName = folderName;
  }

  @Override
  public Collection<MessageResource> getMessages() {
    refreshMessages();
    return messages;
  }

  @Override
  public int numMessages() {
    if (messages == null) {
      refreshMessages();
    }
    return messages.size();
  }

  @Override
  public int totalSize() {
    if (messages == null) {
      refreshMessages();
    }
    int retval = 0;
    for (MessageResource res : messages) {
      retval += res.getSize();
    }
    return retval;
  }

  void removeMessage(OWAMessageResource message) {
    logger.error("***************************************** Folder -> removeMessage()");
    // TODO: implement removeMessage
    // throw new UnsupportedOperationException("Not yet implemented");
  }

  private void refreshMessages() {
    logger.info("Refreshing message list");
    List<MimeMessage> mimes = client.fetchMail(OWAMessageFilter.NEW_MESSAGES);
    logger.info("Found " + mimes.size() + " new messages");
    messages = new ArrayList<MessageResource>();
    for (MimeMessage mime : mimes) {
      messages.add(new OWAMessageResource(this, mime, null));
    }
  }
}
