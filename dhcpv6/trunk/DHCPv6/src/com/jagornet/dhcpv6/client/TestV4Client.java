/*
 * Copyright 2011 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file TestV4Client.java is part of DHCPv6.
 *
 *   DHCPv6 is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   DHCPv6 is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with DHCPv6.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.jagornet.dhcpv6.client;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandler;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.channel.socket.DatagramChannel;
import org.jboss.netty.channel.socket.DatagramChannelFactory;
import org.jboss.netty.channel.socket.nio.NioDatagramChannelFactory;
import org.jboss.netty.handler.logging.LoggingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jagornet.dhcpv6.message.DhcpV4Message;
import com.jagornet.dhcpv6.option.v4.DhcpV4MsgTypeOption;
import com.jagornet.dhcpv6.option.v4.DhcpV4RequestedIpAddressOption;
import com.jagornet.dhcpv6.server.netty.DhcpV4ChannelDecoder;
import com.jagornet.dhcpv6.server.netty.DhcpV4ChannelEncoder;
import com.jagornet.dhcpv6.util.DhcpConstants;

/**
 * A test client that sends discover messages to a DHCPv4 server
 * via unicast.
 * 
 * @author A. Gregory Rabil
 */
@ChannelHandler.Sharable
public class TestV4Client extends SimpleChannelUpstreamHandler
{
	private static Logger log = LoggerFactory.getLogger(TestV4Client.class);

    protected Options options = new Options();
    protected CommandLineParser parser = new BasicParser();
    protected HelpFormatter formatter;
    
	protected InetAddress DEFAULT_ADDR;
    protected InetAddress serverAddr;
    protected int serverPort = DhcpConstants.V4_SERVER_PORT;
    protected InetAddress clientAddr;
    protected int clientPort = DhcpConstants.V4_SERVER_PORT;	// the test client acts as a relay
    protected boolean rapidCommit = false;
    protected int numRequests = 100;
    protected AtomicInteger discoversSent = new AtomicInteger();
    protected AtomicInteger offersReceived = new AtomicInteger();
    protected AtomicInteger requestsSent = new AtomicInteger();
    protected AtomicInteger acksReceived = new AtomicInteger();
    protected AtomicInteger releasesSent = new AtomicInteger();
    protected int successCnt = 0;
    protected long startTime = 0;    
    protected long endTime = 0;
    protected long timeout = 0;
    protected Object sync = new Object();

    protected InetSocketAddress server = null;
    protected InetSocketAddress client = null;
    
    protected DatagramChannel channel = null;	
	protected ExecutorService executor = Executors.newCachedThreadPool();
    
    protected Map<Long, DhcpV4Message> discoverMap =
    	Collections.synchronizedMap(new HashMap<Long, DhcpV4Message>());
    
    protected Map<Long, DhcpV4Message> requestMap =
    	Collections.synchronizedMap(new HashMap<Long, DhcpV4Message>());

    /**
     * Instantiates a new test client.
     *
     * @param args the args
     * @throws Exception the exception
     */
    public TestV4Client(String[] args) throws Exception 
    {
    	DEFAULT_ADDR = InetAddress.getLocalHost();
    	
        setupOptions();

        if(!parseOptions(args)) {
            formatter = new HelpFormatter();
            String cliName = this.getClass().getName();
            formatter.printHelp(cliName, options);
//            PrintWriter stderr = new PrintWriter(System.err, true);	// auto-flush=true
//            formatter.printHelp(stderr, 80, cliName, null, options, 2, 2, null);
            System.exit(0);
        }
        
        try {
			start();

		} 
        catch (Exception ex) {
			ex.printStackTrace();
		}
    }
    
	/**
	 * Setup options.
	 */
	private void setupOptions()
    {
		Option numOption = new Option("n", "number", true,
										"Number of client requests to send" +
										" [" + numRequests + "]");
		options.addOption(numOption);
		
        Option caOption = new Option("ca", "clientaddress", true,
        								"Address of DHCPv4 Client (relay)" +
        								" [" + DEFAULT_ADDR + "]");		
        options.addOption(caOption);
		
        Option saOption = new Option("sa", "serveraddress", true,
        								"Address of DHCPv4 Server" +
        								" [" + DEFAULT_ADDR + "]");		
        options.addOption(saOption);

        Option cpOption = new Option("cp", "clientport", true,
        							  "Client Port Number" +
        							  " [" + clientPort + "]");
        options.addOption(cpOption);

        Option spOption = new Option("sp", "serverport", true,
        							  "Server Port Number" +
        							  " [" + serverPort + "]");
        options.addOption(spOption);
        
        Option rOption = new Option("r", "rapidcommit", false,
        							"Send rapid-commit Solicit requests");
        options.addOption(rOption);
        
        Option toOption = new Option("to", "timeout", true,
        							"Timeout");
        options.addOption(toOption);
        
        Option helpOption = new Option("?", "help", false, "Show this help page.");
        
        options.addOption(helpOption);
    }

    /**
     * Parses the options.
     * 
     * @param args the args
     * 
     * @return true, if successful
     */
    protected boolean parseOptions(String[] args)
    {
        try {
            CommandLine cmd = parser.parse(options, args);
            if (cmd.hasOption("?")) {
                return false;
            }
            if (cmd.hasOption("n")) {
            	String n = cmd.getOptionValue("n");
            	try {
            		numRequests = Integer.parseInt(n);
            	}
            	catch (NumberFormatException ex) {
            		numRequests = 100;
            		System.err.println("Invalid number of requests: '" + n +
            							"' using default: " + numRequests +
            							" Exception=" + ex);
            	}
            }
            clientAddr = DEFAULT_ADDR;
            if (cmd.hasOption("ca")) {
            	String a = cmd.getOptionValue("ca");
            	try {
            		clientAddr = InetAddress.getByName(a);
            	}
            	catch (UnknownHostException ex) {
            		clientAddr = DEFAULT_ADDR;
            		System.err.println("Invalid address: '" + a +
            							"' using default: " + serverAddr +
            							" Exception=" + ex);
            	}
            }
            serverAddr = DEFAULT_ADDR;
            if (cmd.hasOption("sa")) {
            	String a = cmd.getOptionValue("sa");
            	try {
            		serverAddr = InetAddress.getByName(a);
            	}
            	catch (UnknownHostException ex) {
            		serverAddr = DEFAULT_ADDR;
            		System.err.println("Invalid address: '" + a +
            							"' using default: " + serverAddr +
            							" Exception=" + ex);
            	}
            }
            if (cmd.hasOption("cp")) {
            	String p = cmd.getOptionValue("cp");
            	try {
            		clientPort = Integer.parseInt(p);
            	}
            	catch (NumberFormatException ex) {
            		clientPort = DhcpConstants.CLIENT_PORT;
            		System.err.println("Invalid client port number: '" + p +
            							"' using default: " + clientPort +
            							" Exception=" + ex);
            	}
            }
            if (cmd.hasOption("sp")) {
            	String p = cmd.getOptionValue("sp");
            	try {
            		serverPort = Integer.parseInt(p);
            	}
            	catch (NumberFormatException ex) {
            		serverPort = DhcpConstants.SERVER_PORT;
            		System.err.println("Invalid server port number: '" + p +
            							"' using default: " + serverPort +
            							" Exception=" + ex);
            	}
            }
            if (cmd.hasOption("r")) {
            	rapidCommit = true;
            }
            if (cmd.hasOption("to")) {
            	String to = cmd.getOptionValue("to");
            	try {
            		timeout = Integer.parseInt(to);
            	}
            	catch (NumberFormatException ex) {
            		System.err.println("Invalid timeout: '" + to +
            							"' using default: " + timeout +
            							" Exception=" + ex);
            	}
            }
        }
        catch (ParseException pe) {
            System.err.println("Command line option parsing failure: " + pe);
            return false;
		}
        return true;
    }
    
    /**
     * Start sending DHCPv4 DISCOVERs.
     */
    public void start()
    {
    	DatagramChannelFactory factory = 
    		new NioDatagramChannelFactory(Executors.newCachedThreadPool());
    	
    	server = new InetSocketAddress(serverAddr, serverPort);
    	client = new InetSocketAddress(clientPort);
    	
		ChannelPipeline pipeline = Channels.pipeline();
        pipeline.addLast("logger", new LoggingHandler());
        pipeline.addLast("encoder", new DhcpV4ChannelEncoder());
        pipeline.addLast("decoder", new DhcpV4ChannelDecoder(client, false));
        pipeline.addLast("handler", this);
    	
        channel = factory.newChannel(pipeline);
    	channel.bind(client);

    	List<DhcpV4Message> discoverMsgs = buildDiscoverMessages();
    	
    	for (DhcpV4Message msg : discoverMsgs) {
    		executor.execute(new RequestSender(msg, server));
    	}

    	synchronized (sync) {
    		long ms = timeout * 1000;
        	try {
        		log.info("Waiting total of " + timeout + " milliseconds for completion");
        		sync.wait(ms);
        	}
        	catch (InterruptedException ex) {
        		log.error("Interrupted", ex);
        	}
		}

		log.info("Complete: discoversSent=" + discoversSent +
				" offersReceived=" + offersReceived +
				" requestsSent=" + requestsSent +
				" acksReceived=" + acksReceived +
				" releasesSent=" + releasesSent +
				" elapsedTime = " + (endTime - startTime) + " milliseconds");

    	log.info("Shutting down executor...");
    	executor.shutdownNow();
    	log.info("Closing channel...");
    	channel.close();
    	log.info("Done.");
    	System.exit(0);
    }

    /**
     * The Class RequestSender.
     */
    class RequestSender implements Runnable, ChannelFutureListener
    {
    	
	    /** The msg. */
	    DhcpV4Message msg;
    	
	    /** The server. */
	    InetSocketAddress server;
    	
    	/**
	     * Instantiates a new request sender.
	     *
	     * @param msg the msg
	     * @param server the server
	     */
	    public RequestSender(DhcpV4Message msg, InetSocketAddress server)
    	{
    		this.msg = msg;
    		this.server = server;
    	}
		
		/* (non-Javadoc)
		 * @see java.lang.Runnable#run()
		 */
		@Override
		public void run()
		{
			ChannelFuture future = channel.write(msg, server);
			future.addListener(this);
		}
		
		/* (non-Javadoc)
		 * @see org.jboss.netty.channel.ChannelFutureListener#operationComplete(org.jboss.netty.channel.ChannelFuture)
		 */
		@Override
		public void operationComplete(ChannelFuture future) throws Exception
		{
			long id = msg.getTransactionId();
			if (future.isSuccess()) {
				if (startTime == 0) {
					startTime = System.currentTimeMillis();
				}
				if (msg.getMessageType() == DhcpConstants.V4MESSAGE_TYPE_DISCOVER) {
					discoversSent.getAndIncrement();
					log.info("Succesfully sent discover message id=" + id);
					discoverMap.put(id, msg);
				}
				else if (msg.getMessageType() == DhcpConstants.V4MESSAGE_TYPE_REQUEST) {
					requestsSent.getAndIncrement();
					log.info("Succesfully sent request message id=" + id);
					requestMap.put(id, msg);
				}
				else if (msg.getMessageType() == DhcpConstants.V4MESSAGE_TYPE_RELEASE) {
					releasesSent.getAndIncrement();
					log.info("Succesfully sent release message id=" + id);
					if (releasesSent.get() == numRequests) {
						synchronized (sync) {
							sync.notifyAll();
						}
					}
				}
			}
			else {
				log.warn("Failed to send message id=" + msg.getTransactionId());
			}
		}
    }
    
    /**
     * Builds the discover messages.
     * 
     * @return the list< dhcp message>
     */
    private List<DhcpV4Message> buildDiscoverMessages()
    {
    	List<DhcpV4Message> requests = new ArrayList<DhcpV4Message>();   	
        for (int id=0; id<numRequests; id++) {
            DhcpV4Message msg = 
            	new DhcpV4Message(null, new InetSocketAddress(serverAddr, serverPort));

            msg.setOp((short)DhcpConstants.OP_REQUEST);
            msg.setTransactionId(id);
            msg.setHtype((short)1);	// ethernet
            msg.setHlen((byte)6);
            msg.setChAddr(buildChAddr(id));
            msg.setGiAddr(clientAddr);	// look like a relay to the DHCP server
            
            DhcpV4MsgTypeOption msgTypeOption = new DhcpV4MsgTypeOption();
            msgTypeOption.setUnsignedByte(
            		(short)DhcpConstants.V4MESSAGE_TYPE_DISCOVER);
            
            msg.putDhcpOption(msgTypeOption);
            requests.add(msg);
        }
        return requests;
    }

    private byte[] buildChAddr(long id) {
        byte[] bid = BigInteger.valueOf(id).toByteArray();
        byte[] chAddr = new byte[6];
        chAddr[0] = (byte)0xde;
        chAddr[1] = (byte)0xb1;
        if (bid.length == 4) {
            chAddr[2] = bid[0];
            chAddr[3] = bid[1];
            chAddr[4] = bid[2];
            chAddr[5] = bid[3];
        }
        else if (bid.length == 3) {
	        chAddr[2] = 0;
	        chAddr[3] = bid[0];
	        chAddr[4] = bid[1];
	        chAddr[5] = bid[2];
        }
        else if (bid.length == 2) {
	        chAddr[2] = 0;
	        chAddr[3] = 0;
	        chAddr[4] = bid[0];
	        chAddr[5] = bid[1];
        }
        else if (bid.length == 1) {
	        chAddr[2] = 0;
	        chAddr[3] = 0;
	        chAddr[4] = 0;
	        chAddr[5] = bid[0];
        }
        return chAddr;
    }
    
    private DhcpV4Message buildRequestMessage(DhcpV4Message offer) {
    	
        DhcpV4Message msg = 
            	new DhcpV4Message(null, new InetSocketAddress(serverAddr, serverPort));

        msg.setOp((short)DhcpConstants.OP_REQUEST);
        long xid = offer.getTransactionId();
        msg.setTransactionId(xid);
        msg.setHtype((short)1);	// ethernet
        msg.setHlen((byte)6);
        msg.setChAddr(buildChAddr(xid));
        msg.setGiAddr(clientAddr);	// look like a relay to the DHCP server
        
        DhcpV4MsgTypeOption msgTypeOption = new DhcpV4MsgTypeOption();
        msgTypeOption.setUnsignedByte(
        		(short)DhcpConstants.V4MESSAGE_TYPE_REQUEST);
        
        msg.putDhcpOption(msgTypeOption);
        
        DhcpV4RequestedIpAddressOption reqIpOption = new DhcpV4RequestedIpAddressOption();
        reqIpOption.setIpAddress(offer.getYiAddr().getHostAddress());
        msg.putDhcpOption(reqIpOption);
        
        return msg;
    }
    
    private DhcpV4Message buildReleaseMessage(DhcpV4Message ack) {
    	
        DhcpV4Message msg = 
            	new DhcpV4Message(null, new InetSocketAddress(serverAddr, serverPort));

        msg.setOp((short)DhcpConstants.OP_REQUEST);
        long xid = ack.getTransactionId();
        msg.setTransactionId(xid);
        msg.setHtype((short)1);	// ethernet
        msg.setHlen((byte)6);
        msg.setChAddr(buildChAddr(xid));
        msg.setGiAddr(clientAddr);	// look like a relay to the DHCP server
        msg.setCiAddr(ack.getYiAddr());
        
        DhcpV4MsgTypeOption msgTypeOption = new DhcpV4MsgTypeOption();
        msgTypeOption.setUnsignedByte(
        		(short)DhcpConstants.V4MESSAGE_TYPE_RELEASE);
        
        msg.putDhcpOption(msgTypeOption);
        
        return msg;
    }

	/*
	 * (non-Javadoc)
	 * @see org.jboss.netty.channel.SimpleChannelHandler#messageReceived(org.jboss.netty.channel.ChannelHandlerContext, org.jboss.netty.channel.MessageEvent)
	 */
	@Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception
    {
    	Object message = e.getMessage();
        if (message instanceof DhcpV4Message) {
            
            DhcpV4Message dhcpMessage = (DhcpV4Message) message;
            if (log.isDebugEnabled())
            	log.debug("Received: " + dhcpMessage.toStringWithOptions());
            else
            	log.info("Received: " + dhcpMessage.toString());
            
            if (dhcpMessage.getMessageType() == DhcpConstants.V4MESSAGE_TYPE_OFFER) {
	            DhcpV4Message discoverMessage = discoverMap.remove(dhcpMessage.getTransactionId());
	            if (discoverMessage != null) {
	            	offersReceived.getAndIncrement();
	            	log.info("Removed message from discover map: cnt=" + offersReceived);
	            	synchronized (discoverMap) {
	            		if (discoverMap.isEmpty()) {
	            			discoverMap.notify();
	            		}
	            		else {
	            			log.debug("Discover map size: " + discoverMap.size());
	            		}
	            	}
	            	// queue the request message now
	            	executor.execute(new RequestSender(buildRequestMessage(dhcpMessage), server));
	            }
	            else {
	            	log.error("Message not found in discover map: xid=" + dhcpMessage.getTransactionId());
	            }
            }
            else if (dhcpMessage.getMessageType() == DhcpConstants.V4MESSAGE_TYPE_ACK) {
	            DhcpV4Message requestMessage = requestMap.remove(dhcpMessage.getTransactionId());
	            if (requestMessage != null) {
	            	acksReceived.getAndIncrement();
	            	log.info("Removed message from request map: cnt=" + acksReceived);
	            	endTime = System.currentTimeMillis();
	            	synchronized (requestMap) {
	            		if (requestMap.isEmpty()) {
	            			requestMap.notify();
	            		}
	            		else {
	            			log.debug("Request map size: " + requestMap.size());
	            		}
	            	}
	            	// queue the release message now
	            	executor.execute(new RequestSender(buildReleaseMessage(dhcpMessage), server));
	            }
	            else {
	            	log.error("Message not found in request map: xid=" + dhcpMessage.getTransactionId());
	            }
            }
            else {
            	log.warn("Received unhandled message type: " + dhcpMessage.getMessageType());
            }
        }
        else {
            // Note: in theory, we can't get here, because the
            // codec would have thrown an exception beforehand
            log.error("Received unknown message object: " + message.getClass());
        }
    }
	 
	/* (non-Javadoc)
	 * @see org.jboss.netty.channel.SimpleChannelUpstreamHandler#exceptionCaught(org.jboss.netty.channel.ChannelHandlerContext, org.jboss.netty.channel.ExceptionEvent)
	 */
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception
	{
    	log.error("Exception caught: ", e.getCause());
    	e.getChannel().close();
	}
    
    /**
     * The main method.
     * 
     * @param args the arguments
     */
    public static void main(String[] args) {
        try {
			new TestV4Client(args);
		} catch (Exception e) {
			e.printStackTrace();
		}
    }

}
