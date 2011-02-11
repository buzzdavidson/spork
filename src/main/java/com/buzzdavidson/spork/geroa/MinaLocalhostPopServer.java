/*
 * <LICENSEHEADER/>
 * 
 */
package com.buzzdavidson.spork.geroa;

import com.ettrema.mail.Filter;
import com.ettrema.mail.MailResourceFactory;
import com.ettrema.mail.pop.MinaPopServer;
import com.ettrema.mail.pop.PopIOHandlerAdapter;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.util.List;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.buffer.SimpleBufferAllocator;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.textline.TextLineCodecFactory;
import org.apache.mina.filter.logging.LoggingFilter;
import org.apache.mina.filter.stream.StreamWriteFilter;
import org.apache.mina.transport.socket.SocketAcceptor;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;

/**
 * Override MinaPopServer to only allow connections from localhost
 * 
 * @author sdavidson
 */
public class MinaLocalhostPopServer extends MinaPopServer {

  private SocketAcceptor acceptor;

  public MinaLocalhostPopServer(int popPort, MailResourceFactory resourceFactory, List<Filter> filters) {
    super(popPort, resourceFactory, filters);
  }

  @Override
  public void start() {
    IoBuffer.setUseDirectBuffer(false);
    IoBuffer.setAllocator(new SimpleBufferAllocator());

    acceptor = new NioSocketAcceptor();

    acceptor.getFilterChain().addLast("logger", new LoggingFilter());
    acceptor.getFilterChain().addLast("codec", new ProtocolCodecFilter(new TextLineCodecFactory(Charset.forName("US-ASCII"))));
    acceptor.getFilterChain().addLast("stream", new StreamWriteFilter());
    acceptor.setHandler(new PopIOHandlerAdapter(this));
    try {
      acceptor.bind(new InetSocketAddress("127.0.0.1", getPopPort()));
    } catch (IOException ex) {
      throw new RuntimeException("Couldnt bind to port: " + getPopPort(), ex);
    }

  }

  @Override
  public void stop() {
    acceptor.unbind();
    acceptor = null;
  }
}
