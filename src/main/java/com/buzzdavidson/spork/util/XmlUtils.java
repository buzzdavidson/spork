/*
 * <LICENSEHEADER/>
 *
 */
package com.buzzdavidson.spork.util;

import java.io.File;
import java.io.InputStream;
import java.util.regex.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * XML Helper Utility Methods
 *
 * @author steve
 */
public class XmlUtils {

  private static final DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
  private static final Log logger = LogFactory.getLog(XmlUtils.class);

  /**
   * Descends from the given parent looking for the first node to match path.
   * @param parent The DOM parent from which to start.
   * @param path The set of regular expressions for each level of the descent.
   * @return The child node at the end of the path or null if no match.
   */
  public static Node getChild(Node parent, Pattern[] path) {
    Node c = parent;
    int i;

    for (i = 0; c != null && i < path.length; ++i) {
      logger.debug("Looking for node matching [" + path[i].pattern() + "]");
      for (c = c.getFirstChild(); c != null; c = c.getNextSibling()) {
        String nn = c.getNodeName();
        Matcher m = path[i].matcher(nn);
        //logger.info("Checking node: " + nn);
        if (m != null && m.matches()) {
          logger.debug("Found matching node: " + nn);
          break;
        }
      }
    }

    return (i == path.length) ? c : null;
  }

  /**
   * Reads an XML Document from the given InputStream.
   * @param input InputStream to read from
   * @return XML Document
   */
  public static Document readDocument(InputStream input) {
    try {
      DocumentBuilder builder = docFactory.newDocumentBuilder();

      return builder.parse(input);
    } catch (Exception ex) {
      throw new RuntimeException("Failed to read XML document", ex);
    }
  }

  /**
   * Reads an XML Document from the given HttpMethodBase's response stream.
   * @param method method from which to read document
   * @return XML Document
   */
  public static Document readResponseDocument(HttpMethodBase method) {
    try {
      return readDocument(method.getResponseBodyAsStream());
    } catch (Exception ex) {
      throw new RuntimeException("Failed to read XML document", ex);
    }
  }

  /**
   * Write a XML document to file
   * @param doc document to write
   * @param filename name of file to create
   */
  public static void writeXmlFile(Document doc, String filename) {
    try {
      // Prepare the DOM document for writing
      Source source = new DOMSource(doc);

      // Prepare the output file
      File file = new File(filename);
      Result result = new StreamResult(file);

      // Write the DOM document to the file
      Transformer xformer = TransformerFactory.newInstance().newTransformer();
      xformer.transform(source, result);
    } catch (TransformerConfigurationException e) {
    } catch (TransformerException e) {
    }
  }
}
