/*
 * <LICENSEHEADER/>
 *
 */
package com.buzzdavidson.spork.geroa;

import com.buzzdavidson.spork.constant.OWAConstants;
import com.buzzdavidson.spork.util.OWAUtils;
import com.ettrema.mail.MessageResource;
import com.ettrema.mail.StandardMessageFactory;
import java.io.IOException;
import java.io.OutputStream;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * OWA Implementation of geroa MessageResource
 *
 * @author steve
 */
public class OWAMessageResource implements MessageResource {

  private final Log logger = LogFactory.getLog(this.getClass());
  private OWAMessageFolder folder;
  private MimeMessage message;

  public OWAMessageResource(OWAMessageFolder folder, MimeMessage mimeMessage, StandardMessageFactory factory) {
    super();
    this.folder = folder;
    this.message = mimeMessage;
  }

  @Override
  public void delete() {
    folder.removeMessage(this);
  }

  @Override
  public int getSize() {
    int retval = 0;
    String sizeHeader = OWAUtils.getSingleHeader(message, OWAConstants.PARAM_CONTENT_LENGTH);
    if (sizeHeader != null && sizeHeader.length() > 0) {
      try {
        retval = Integer.parseInt(sizeHeader);
      } catch (NumberFormatException ex) {
        retval = 0;
      }
    }
    return retval;
  }

  @Override
  public void writeTo(OutputStream out) {
    try {
      message.writeTo(out);
    } catch (IOException ex) {
      throw new RuntimeException(ex);
    } catch (MessagingException ex) {
      throw new RuntimeException(ex);
    }
  }
}
