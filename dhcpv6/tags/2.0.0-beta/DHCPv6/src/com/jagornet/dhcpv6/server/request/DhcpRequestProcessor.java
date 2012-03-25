/*
 * Copyright 2009 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file DhcpRequestProcessor.java is part of DHCPv6.
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
package com.jagornet.dhcpv6.server.request;

import java.net.InetAddress;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jagornet.dhcpv6.db.IdentityAssoc;
import com.jagornet.dhcpv6.message.DhcpMessage;
import com.jagornet.dhcpv6.option.DhcpClientIdOption;
import com.jagornet.dhcpv6.option.DhcpIaNaOption;
import com.jagornet.dhcpv6.option.DhcpIaPdOption;
import com.jagornet.dhcpv6.option.DhcpIaTaOption;
import com.jagornet.dhcpv6.option.DhcpServerIdOption;
import com.jagornet.dhcpv6.server.request.binding.Binding;
import com.jagornet.dhcpv6.server.request.binding.NaAddrBindingManager;
import com.jagornet.dhcpv6.server.request.binding.PrefixBindingManager;
import com.jagornet.dhcpv6.server.request.binding.TaAddrBindingManager;
import com.jagornet.dhcpv6.util.DhcpConstants;

/**
 * Title: DhcpRequestProcessor
 * Description: The main class for processing REQUEST messages.
 * 
 * @author A. Gregory Rabil
 */

public class DhcpRequestProcessor extends BaseDhcpProcessor
{
	
	/** The log. */
	private static Logger log = LoggerFactory.getLogger(DhcpRequestProcessor.class);
    
    /**
     * Construct an DhcpRequestProcessor processor.
     * 
     * @param requestMsg the Request message
     * @param clientLinkAddress the client link address
     */
    public DhcpRequestProcessor(DhcpMessage requestMsg, InetAddress clientLinkAddress)
    {
        super(requestMsg, clientLinkAddress);
    }

    /*
     * FROM RFC 3315:
     * 
     * 15.4. Request Message
     * 
     * Servers MUST discard any received Request message that meet any of
     * the following conditions:
     * 
     * -  the message does not include a Server Identifier option.
     * 
     * -  the contents of the Server Identifier option do not match the
     *    server's DUID.
     * 
     * -  the message does not include a Client Identifier option.
     *  
     */
    /* (non-Javadoc)
     * @see com.jagornet.dhcpv6.server.request.BaseDhcpProcessor#preProcess()
     */
    @Override
    public boolean preProcess()
    {
    	if (!super.preProcess()) {
    		return false;
    	}

    	DhcpServerIdOption requestedServerIdOption = requestMsg.getDhcpServerIdOption();
        if (requestedServerIdOption == null) {
            log.warn("Ignoring Request message: " +
                    "Requested ServerId option is null");
           return false;
        }
        
        if (!dhcpServerIdOption.equals(requestedServerIdOption)) {
            log.warn("Ignoring Request message: " +
                     "Requested ServerId: " + requestedServerIdOption +
                     "My ServerId: " + dhcpServerIdOption);
            return false;
        }
    	
    	if (requestMsg.getDhcpClientIdOption() == null) {
    		log.warn("Ignoring Request message: " +
    				"ClientId option is null");
    		return false;
    	}
        
    	return true;
    }

    /* (non-Javadoc)
     * @see com.jagornet.dhcpv6.server.request.BaseDhcpProcessor#process()
     */
    @Override
    public boolean process()
    {
//      When the server receives a Request message via unicast from a client
//      to which the server has not sent a unicast option, the server
//      discards the Request message and responds with a Reply message
//      containing a Status Code option with the value UseMulticast, a Server
//      Identifier option containing the server's DUID, the Client Identifier
//      option from the client message, and no other options.

    	if (shouldMulticast()) {
    		replyMsg.setMessageType(DhcpConstants.REPLY);
    		setReplyStatus(DhcpConstants.STATUS_CODE_USEMULTICAST);
    		return true;
    	}

//		   If the server finds that the prefix on one or more IP addresses in
//		   any IA in the message from the client is not appropriate for the link
//		   to which the client is connected, the server MUST return the IA to
//		   the client with a Status Code option with the value NotOnLink.
//
//		   If the server cannot assign any addresses to an IA in the message
//		   from the client, the server MUST include the IA in the Reply message
//		   with no addresses in the IA and a Status Code option in the IA
//		   containing status code NoAddrsAvail.

		boolean sendReply = true;
		DhcpClientIdOption clientIdOption = requestMsg.getDhcpClientIdOption();
		
		List<DhcpIaNaOption> iaNaOptions = requestMsg.getIaNaOptions();
    	if ((iaNaOptions != null) && !iaNaOptions.isEmpty()) {
    		NaAddrBindingManager bindingMgr = dhcpServerConfig.getNaAddrBindingMgr();
    		if (bindingMgr != null) {
	    		for (DhcpIaNaOption dhcpIaNaOption : iaNaOptions) {
	    			log.info("Processing IA_NA Request: " + dhcpIaNaOption.toString());
		    		if (!allIaAddrsOnLink(dhcpIaNaOption, clientLink)) {
		    			addIaNaOptionStatusToReply(dhcpIaNaOption,
		    					DhcpConstants.STATUS_CODE_NOTONLINK);
		    		}
		    		else {
						Binding binding = bindingMgr.findCurrentBinding(clientLink.getLink(), 
								clientIdOption, dhcpIaNaOption, requestMsg);
						if (binding != null) {
							binding = bindingMgr.updateBinding(binding, clientLink.getLink(), 
									clientIdOption, dhcpIaNaOption, requestMsg, 
									IdentityAssoc.COMMITTED);
							if (binding != null) {
								addBindingToReply(clientLink, binding);
								bindings.add(binding);
							}
							else {
								addIaNaOptionStatusToReply(dhcpIaNaOption,
			    						DhcpConstants.STATUS_CODE_NOADDRSAVAIL);
							}
						}
						else {
							//TODO: what is the right thing to do here - we have
							//		a request, but the solicit failed somehow?
//							addIaNaOptionStatusToReply(dhcpIaNaOption,
//		    						DhcpConstants.STATUS_CODE_NOBINDING);
							// assume that if we have no binding, then there were
							// no addresses available to be given out on solicit
							addIaNaOptionStatusToReply(dhcpIaNaOption,
		    						DhcpConstants.STATUS_CODE_NOADDRSAVAIL);
						}
		    		}
				}
    		}
    		else {
    			log.error("Unable to process IA_NA Request:" +
    					" No NaAddrBindingManager available");
    		}
    	}
		
		List<DhcpIaTaOption> iaTaOptions = requestMsg.getIaTaOptions();
    	if ((iaTaOptions != null) && !iaTaOptions.isEmpty()) {
    		TaAddrBindingManager bindingMgr = dhcpServerConfig.getTaAddrBindingMgr();
    		if (bindingMgr != null) {
	    		for (DhcpIaTaOption dhcpIaTaOption : iaTaOptions) {
	    			log.info("Processing IA_TA Request: " + dhcpIaTaOption.toString());
		    		if (!allIaAddrsOnLink(dhcpIaTaOption, clientLink)) {
		    			addIaTaOptionStatusToReply(dhcpIaTaOption,
		    					DhcpConstants.STATUS_CODE_NOTONLINK);
		    		}
		    		else {
						Binding binding = bindingMgr.findCurrentBinding(clientLink.getLink(), 
								clientIdOption, dhcpIaTaOption, requestMsg);
						if (binding != null) {
							binding = bindingMgr.updateBinding(binding, clientLink.getLink(), 
									clientIdOption, dhcpIaTaOption, requestMsg, 
									IdentityAssoc.COMMITTED);
							if (binding != null) {
								addBindingToReply(clientLink, binding);
								bindings.add(binding);
							}
							else {
								addIaTaOptionStatusToReply(dhcpIaTaOption,
			    						DhcpConstants.STATUS_CODE_NOADDRSAVAIL);
							}
						}
						else {
							//TODO: what is the right thing to do here - we have
							//		a request, but the solicit failed somehow?
//							addIaTaOptionStatusToReply(dhcpIaTaOption,
//		    						DhcpConstants.STATUS_CODE_NOBINDING);
							// assume that if we have no binding, then there were
							// no addresses available to be given out on solicit
							addIaTaOptionStatusToReply(dhcpIaTaOption,
		    						DhcpConstants.STATUS_CODE_NOADDRSAVAIL);
						}
		    		}
				}
    		}
    		else {
    			log.error("Unable to process IA_TA Request:" +
    					" No TaAddrBindingManager available");
    		}
    	}
		
		List<DhcpIaPdOption> iaPdOptions = requestMsg.getIaPdOptions();
    	if ((iaPdOptions != null) && !iaPdOptions.isEmpty()) {
    		PrefixBindingManager bindingMgr = dhcpServerConfig.getPrefixBindingMgr();
    		if (bindingMgr != null) {
	    		for (DhcpIaPdOption dhcpIaPdOption : iaPdOptions) {
	    			log.info("Processing IA_PD Request: " + dhcpIaPdOption.toString());
		    		if (!allIaPrefixesOnLink(dhcpIaPdOption, clientLink)) {
		    			// for PD return NoPrefixAvail instead of NotOnLink
		    			addIaPdOptionStatusToReply(dhcpIaPdOption,
		    					DhcpConstants.STATUS_CODE_NOPREFIXAVAIL);
		    		}
		    		else {
						Binding binding = bindingMgr.findCurrentBinding(clientLink.getLink(), 
								clientIdOption, dhcpIaPdOption, requestMsg);
						if (binding != null) {
							binding = bindingMgr.updateBinding(binding, clientLink.getLink(), 
									clientIdOption, dhcpIaPdOption, requestMsg, 
									IdentityAssoc.COMMITTED);
							if (binding != null) {
								addBindingToReply(clientLink, binding);
								bindings.add(binding);
							}
							else {
				    			// for PD return NoPrefixAvail instead of NotOnLink
								addIaPdOptionStatusToReply(dhcpIaPdOption,
			    						DhcpConstants.STATUS_CODE_NOPREFIXAVAIL);
							}
						}
						else {
							//TODO: what is the right thing to do here - we have
							//		a request, but the solicit failed somehow?
//							addIaPdOptionStatusToReply(dhcpIaPdOption,
//		    						DhcpConstants.STATUS_CODE_NOBINDING);
							// assume that if we have no binding, then there were
							// no prefixes available to be given out on solicit
							addIaPdOptionStatusToReply(dhcpIaPdOption,
		    						DhcpConstants.STATUS_CODE_NOPREFIXAVAIL);
						}
		    		}
				}
    		}
    		else {
    			log.error("Unable to process IA_PD Request:" +
    					" No PrefixBindingManager available");
    		}
    	}
    	
    	if (sendReply) {
            replyMsg.setMessageType(DhcpConstants.REPLY);
            if (!bindings.isEmpty()) {
            	populateReplyMsgOptions(clientLink);
    			processDdnsUpdates();
            }
    	}
		return sendReply;    	
    }
}
