/*
 * Copyright 2009 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file DhcpSolicitProcessor.java is part of DHCPv6.
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
import com.jagornet.dhcpv6.server.config.DhcpServerPolicies;
import com.jagornet.dhcpv6.server.config.DhcpServerPolicies.Property;
import com.jagornet.dhcpv6.server.request.binding.Binding;
import com.jagornet.dhcpv6.server.request.binding.NaAddrBindingManagerInterface;
import com.jagornet.dhcpv6.server.request.binding.PrefixBindingManagerInterface;
import com.jagornet.dhcpv6.server.request.binding.TaAddrBindingManagerInterface;
import com.jagornet.dhcpv6.util.DhcpConstants;
import com.jagornet.dhcpv6.xml.Link;

// TODO: Auto-generated Javadoc
/**
 * Title: DhcpSolicitProcessor
 * Description: The main class for processing SOLICIT messages.
 * 
 * @author A. Gregory Rabil
 */

public class DhcpSolicitProcessor extends BaseDhcpProcessor
{
	
	/** The log. */
	private static Logger log = LoggerFactory.getLogger(DhcpSolicitProcessor.class);
    
    /**
     * Construct an DhcpSolicitProcessor processor.
     * 
     * @param requestMsg the Solicit message
     * @param clientLinkAddress the client link address
     */
    public DhcpSolicitProcessor(DhcpMessage requestMsg, InetAddress clientLinkAddress)
    {
        super(requestMsg, clientLinkAddress);
    }

    /*
     * FROM RFC 3315:
     * 
     * 15.2. Solicit Message
     * 
     *    Servers MUST discard any Solicit messages that do not include a
     *    Client Identifier option or that do include a Server Identifier
     *    option.
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
    	
    	if (requestMsg.getDhcpClientIdOption() == null) {
    		log.warn("Ignoring Solicit message: " +
    				"ClientId option is null");
    		return false;
    	}
    	
    	if (requestMsg.getDhcpServerIdOption() != null) {
    		log.warn("Ignoring Solicit message: " +
					 "ServerId option is not null");
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
//		   If the Solicit message from the client included one or more IA
//		   options, the server MUST include IA options in the Advertise message
//		   containing any addresses that would be assigned to IAs contained in
//		   the Solicit message from the client.  If the client has included
//		   addresses in the IAs in the Solicit message, the server uses those
//		   addresses as hints about the addresses the client would like to
//		   receive.
//
//		   If the server will not assign any addresses to any IAs in a
//		   subsequent Request from the client, the server MUST send an Advertise
//		   message to the client that includes only a Status Code option with
//		   code NoAddrsAvail and a status message for the user, a Server
//		   Identifier option with the server's DUID, and a Client Identifier
//		   option with the client's DUID.

		boolean sendReply = true;
		boolean rapidCommit = isRapidCommit(requestMsg, clientLink.getLink());
		DhcpClientIdOption clientIdOption = requestMsg.getDhcpClientIdOption();
		
		List<DhcpIaNaOption> iaNaOptions = requestMsg.getIaNaOptions();
    	if ((iaNaOptions != null) && !iaNaOptions.isEmpty()) {
    		NaAddrBindingManagerInterface bindingMgr = dhcpServerConfig.getNaAddrBindingMgr();
    		if (bindingMgr != null) {
	    		for (DhcpIaNaOption dhcpIaNaOption : iaNaOptions) {
	    			log.info("Processing IA_NA Solicit: " + dhcpIaNaOption.toString());
					Binding binding = bindingMgr.findCurrentBinding(clientLink.getLink(), 
							clientIdOption, dhcpIaNaOption, requestMsg);
					if (binding == null) {
						// no current binding for this IA_NA, create a new one
						binding = bindingMgr.createSolicitBinding(clientLink.getLink(), 
								clientIdOption, dhcpIaNaOption, requestMsg, rapidCommit);
					}
					else {
						binding = bindingMgr.updateBinding(binding, clientLink.getLink(), 
								clientIdOption, dhcpIaNaOption, requestMsg, rapidCommit ? 
										IdentityAssoc.COMMITTED : IdentityAssoc.ADVERTISED);
					}
					if (binding != null) {
						// have a good binding, put it in the reply with options
						addBindingToReply(clientLink.getLink(), binding);
						bindings.add(binding);
					}
					else {
						// something went wrong, report NoAddrsAvail status for IA_NA
	    				addIaNaOptionStatusToReply(dhcpIaNaOption, 
	    						DhcpConstants.STATUS_CODE_NOADDRSAVAIL);
					}
	    		}
    		}
    		else {
    			log.error("Unable to process IA_NA Solicit:" +
    					" No NaAddrBindingManager available");
    		}
    	}
		
		List<DhcpIaTaOption> iaTaOptions = requestMsg.getIaTaOptions();
    	if ((iaTaOptions != null) && !iaTaOptions.isEmpty()) {
    		TaAddrBindingManagerInterface bindingMgr = dhcpServerConfig.getTaAddrBindingMgr();
    		if (bindingMgr != null) {
	    		for (DhcpIaTaOption dhcpIaTaOption : iaTaOptions) {
	    			log.info("Processing IA_TA Solicit: " + dhcpIaTaOption.toString());
					Binding binding = bindingMgr.findCurrentBinding(clientLink.getLink(), 
							clientIdOption, dhcpIaTaOption, requestMsg);
					if (binding == null) {
						// no current binding for this IA_TA, create a new one
						binding = bindingMgr.createSolicitBinding(clientLink.getLink(), 
								clientIdOption, dhcpIaTaOption, requestMsg, rapidCommit);
					}
					else {
						binding = bindingMgr.updateBinding(binding, clientLink.getLink(), 
								clientIdOption, dhcpIaTaOption, requestMsg, rapidCommit ? 
										IdentityAssoc.COMMITTED : IdentityAssoc.ADVERTISED);
					}
					if (binding != null) {
						// have a good binding, put it in the reply with options
						addBindingToReply(clientLink.getLink(), binding);
						bindings.add(binding);
					}
					else {
						// something went wrong, report NoAddrsAvail status for IA_TA
	    				addIaTaOptionStatusToReply(dhcpIaTaOption, 
	    						DhcpConstants.STATUS_CODE_NOADDRSAVAIL);
					}
	    		}
    		}
    		else {
    			log.error("Unable to process IA_TA Solicit:" +
    					" No TaAddrBindingManager available");
    		}
    	}
		
		List<DhcpIaPdOption> iaPdOptions = requestMsg.getIaPdOptions();
    	if ((iaPdOptions != null) && !iaPdOptions.isEmpty()) {
    		PrefixBindingManagerInterface bindingMgr = dhcpServerConfig.getPrefixBindingMgr();
    		if (bindingMgr != null) {
	    		for (DhcpIaPdOption dhcpIaPdOption : iaPdOptions) {
	    			log.info("Processing IA_PD Solicit: " + dhcpIaPdOption.toString());
					Binding binding = bindingMgr.findCurrentBinding(clientLink.getLink(), 
							clientIdOption, dhcpIaPdOption, requestMsg);
					if (binding == null) {
						// no current binding for this IA_PD, create a new one
						binding = bindingMgr.createSolicitBinding(clientLink.getLink(), 
								clientIdOption, dhcpIaPdOption, requestMsg, rapidCommit);
					}
					else {
						binding = bindingMgr.updateBinding(binding, clientLink.getLink(), 
								clientIdOption, dhcpIaPdOption, requestMsg, rapidCommit ? 
										IdentityAssoc.COMMITTED : IdentityAssoc.ADVERTISED);
					}
					if (binding != null) {
						// have a good binding, put it in the reply with options
						addBindingToReply(clientLink.getLink(), binding);
						bindings.add(binding);
					}
					else {
						// something went wrong, report NoAddrsAvail status for IA_PD
	    				addIaPdOptionStatusToReply(dhcpIaPdOption, 
	    						DhcpConstants.STATUS_CODE_NOADDRSAVAIL);
					}
	    		}
    		}
    		else {
    			log.error("Unable to process IA_PD Solicit:" +
    					" No PrefixBindingManager available");
    		}
    	}
    	
    	if (sendReply) {
    		if (rapidCommit) {
    			replyMsg.setMessageType(DhcpConstants.REPLY);
    		}
    		else {
    	        replyMsg.setMessageType(DhcpConstants.ADVERTISE);
    		}
    		if (!bindings.isEmpty()) {
    			populateReplyMsgOptions(clientLink.getLink());
    			processDdnsUpdates();
    		}
    	}    	
		return sendReply;
    }

	/**
	 * Checks if is rapid commit.
	 * 
	 * @param requestMsg the request msg
	 * @param clientLink the client link
	 * 
	 * @return true, if is rapid commit
	 */
	private boolean isRapidCommit(DhcpMessage requestMsg, Link clientLink)
	{
		if (DhcpServerPolicies.effectivePolicyAsBoolean(clientLink, Property.SUPPORT_RAPID_COMMIT)
				&& requestMsg.hasOption(DhcpConstants.OPTION_RAPID_COMMIT)) {
			return true;
		}
		return false;
	}
}
