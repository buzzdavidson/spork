/*
 * <LICENSEHEADER/>
 *
 */
package com.buzzdavidson.spork.geroa;

import com.buzzdavidson.spork.client.OWAClient;
import com.buzzdavidson.spork.constant.OWAConstants;
import com.buzzdavidson.spork.client.OWAClientFactory;
import com.ettrema.mail.Mailbox;
import com.ettrema.mail.MailboxAddress;
import com.ettrema.mail.MessageFolder;
import javax.mail.internet.MimeMessage;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * OWA Implementation of geroa MailBox
 *
 * @author steve
 */
public class OWAMailBox implements Mailbox {

  private final Log logger = LogFactory.getLog(this.getClass());
  private MailboxAddress mailboxAddress;
  private OWAClient owaClient;

  public OWAMailBox(MailboxAddress add) {
    mailboxAddress = add;
    logger.info("Created new mail box with address " + add.toString());
    owaClient = OWAClientFactory.getClient();
    if (logger.isDebugEnabled()) {
      logger.debug("Created new client using properties: " + owaClient.toString());
    }
  }

  @Override
  public boolean authenticate(String password) {
    boolean retval = owaClient.login(mailboxAddress.user, password);
    if (retval) {
      if (logger.isDebugEnabled()) {
        logger.debug("Authentication succeeded.");
      }
    } else {
      logger.info(String.format("Authentication failed."));
    }
    return retval;
  }

  @Override
  public boolean authenticateMD5(byte[] passwordHash) {
    logger.info(String.format("authenticateMD5 called with hash value [%s]", passwordHash));
    throw new UnsupportedOperationException("authenticateMD5 Not supported.");
  }

  @Override
  public MessageFolder getInbox() {
    return getMailFolder(OWAConstants.FOLDER_NAME_INBOX);
  }

  @Override
  public MessageFolder getMailFolder(String name) {
    if (logger.isDebugEnabled()) {
      logger.debug("Got request for folder [" + name + "]");
    }
    return new OWAMessageFolder(owaClient, name);
  }

  @Override
  public boolean isEmailDisabled() {
    return false;
  }

  @Override
  public void storeMail(MimeMessage mm) {
    logger.error("************************Got request to store mail message");
    // TODO: store mail message (when is this called?)
    throw new UnsupportedOperationException("Not supported yet.");
  }
}
