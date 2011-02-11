/*
 * <LICENSEHEADER/>
 *
 */
package com.buzzdavidson.spork.util;

import java.io.IOException;
import java.io.InputStream;

/**
 * This class extends InputStream and delegates to (wraps) another InputStream.
 * The purpose of this class is to facilitate extending behavior of InputStream
 * objects.
 *
 * @author sdavidson
 */
public class MessageInputStream extends InputStream {

  public MessageInputStream(InputStream stream) {
    this.stream = stream;
  }

  @Override
  public int available() throws IOException {
    return stream.available();
  }

  @Override
  public void close() throws IOException {
    stream.close();
  }

  @Override
  public void mark(int readlimit) {
    stream.mark(readlimit);
  }

  @Override
  public boolean markSupported() {
    return stream.markSupported();
  }

  @Override
  public int read() throws IOException {
    return stream.read();
  }

  @Override
  public int read(byte b[], int off, int len) throws IOException {
    return stream.read(b, off, len);
  }

  @Override
  public void reset() throws IOException {
    stream.reset();
  }

  @Override
  public long skip(long n) throws IOException {
    return stream.skip(n);
  }
  private final InputStream stream;
}
