/*
 * <LICENSEHEADER/>
 *
 */
package com.buzzdavidson.spork.geroa;

import com.ettrema.mail.MailResourceFactory;
import com.ettrema.mail.Mailbox;
import com.ettrema.mail.MailboxAddress;
/**
 * OWA Implementation of geroa Mail Resource Factory
 * @author steve
 */
public class OWAMailResourceFactory implements MailResourceFactory {

  @Override
  public Mailbox getMailbox(MailboxAddress add) {
    return new OWAMailBox(add);
  }
}
