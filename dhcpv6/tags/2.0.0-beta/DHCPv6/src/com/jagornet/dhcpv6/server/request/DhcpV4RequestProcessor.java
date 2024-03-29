/*
 * Copyright 2009 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file DhcpV4RequestProcessor.java is part of DHCPv6.
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jagornet.dhcpv6.db.IdentityAssoc;
import com.jagornet.dhcpv6.message.DhcpV4Message;
import com.jagornet.dhcpv6.option.v4.DhcpV4RequestedIpAddressOption;
import com.jagornet.dhcpv6.option.v4.DhcpV4ServerIdOption;
import com.jagornet.dhcpv6.server.request.binding.Binding;
import com.jagornet.dhcpv6.server.request.binding.V4AddrBindingManager;
import com.jagornet.dhcpv6.util.DhcpConstants;
import com.jagornet.dhcpv6.util.Util;

/**
 * Title: DhcpV4RequestProcessor
 * Description: The main class for processing V4 REQUEST messages.
 * 
 * @author A. Gregory Rabil
 */

public class DhcpV4RequestProcessor extends BaseDhcpV4Processor
{
	
	/** The log. */
	private static Logger log = LoggerFactory.getLogger(DhcpV4RequestProcessor.class);
	
	protected DhcpV4RequestedIpAddressOption requestedIpAddrOption;
	protected enum RequestType {
		Request_Selecting, Request_Renewing, Request_Rebinding, Request_InitReboot;
	}
	protected RequestType type;
    
    /**
     * Construct an DhcpV4RequestProcessor processor.
     * 
     * @param requestMsg the Request message
     * @param clientLinkAddress the client link address
     */
    public DhcpV4RequestProcessor(DhcpV4Message requestMsg, InetAddress clientLinkAddress)
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

    	DhcpV4ServerIdOption requestedServerIdOption = requestMsg.getDhcpV4ServerIdOption();
        requestedIpAddrOption = (DhcpV4RequestedIpAddressOption)
        						requestMsg.getDhcpOption(DhcpConstants.V4OPTION_REQUESTED_IP);

        // first determine what KIND of request we are dealing with
    	if (requestMsg.getCiAddr().equals(DhcpConstants.ZEROADDR)) {
    		// the ciAddr MUST be 0.0.0.0 for Init-Reboot and Selecting
            if (requestedServerIdOption == null) {
            	// init-reboot MUST NOT have server-id option
            	type = RequestType.Request_InitReboot;
            }
            else {
            	// selecting MUST have server-id option
            	type = RequestType.Request_Selecting;                
            }    		
    	}
    	else {
    		// the ciAddr MUST NOT be 0.0.0.0 for Renew and Rebind
    		if (requestMsg.isUnicast()) {
    			// renew is unicast
    			// NOTE: this will not happen if the v4 broadcast interface used at startup,
    			//		 but handling of DHCPv4 renew/rebind is the same anyway
    			type = RequestType.Request_Renewing;
    		}
    		else {
    			// rebind is broadcast
    			type = RequestType.Request_Rebinding;
    		}
    	}
    	
    	if ((type == RequestType.Request_InitReboot) || (type == RequestType.Request_Selecting)) {
        	if (requestedIpAddrOption == null) {
        		log.warn("Ignoring " + type + " message: " +
        				"Requested IP option is null");
        		return false;
        	}
        	if (type == RequestType.Request_Selecting) {
                String requestedServerId = requestedServerIdOption.getIpAddressOption().getIpAddress();
                String myServerId = dhcpV4ServerIdOption.getIpAddressOption().getIpAddress();
                if (!myServerId.equals(requestedServerId)) {
                    log.warn("Ignoring " + type + " message: " +
                             "Requested ServerId: " + requestedServerIdOption +
                             " My ServerId: " + dhcpV4ServerIdOption);
                    return false;
                }
        	}
    	}
    	else {	// type == Renewing or Rebinding
    		if (requestedIpAddrOption != null) {
        		log.warn("Ignoring " + type + " message: " +
						"Requested IP option is not null");
				return false;    			
    		}
    	}

    	return true;
    }

    /* (non-Javadoc)
     * @see com.jagornet.dhcpv6.server.request.BaseDhcpProcessor#process()
     */
    @Override
    public boolean process()
    {
		boolean sendReply = true;
		byte chAddr[] = requestMsg.getChAddr();
		
		V4AddrBindingManager bindingMgr = dhcpServerConfig.getV4AddrBindingMgr();
		if (bindingMgr != null) {
			log.info("Processing " + type +
					 " from chAddr=" + Util.toHexString(chAddr) +
					 " ciAddr=" + requestMsg.getCiAddr().getHostAddress() +
					 " requestedIpAddrOption=" + requestedIpAddrOption);
// TODO
//    		if (!addrOnLink(requestedIpAddrOption, clientLink)) {
//    			//TODO - send NAK
//    		}
//    		else {
    			Binding binding = bindingMgr.findCurrentBinding(clientLink.getLink(), 
    															chAddr, requestMsg);
				if (binding != null) {
					binding = bindingMgr.updateBinding(binding, clientLink.getLink(), 
							chAddr, requestMsg, IdentityAssoc.COMMITTED);
					if (binding != null) {
						addBindingToReply(clientLink, binding);
						bindings.add(binding);
					}
					else {
						log.error("Failed to update binding for client: " + 
								Util.toHexString(chAddr));
					}
				}
				else {
					log.error("No Binding available for client: " + 
							Util.toHexString(chAddr));
				}
//			}
		}
		else {
			log.error("Unable to process V4 Request:" +
					" No V4AddrBindingManager available");
		}
    	
    	if (sendReply) {
            replyMsg.setMessageType((short)DhcpConstants.V4MESSAGE_TYPE_ACK);
            if (!bindings.isEmpty()) {
    			processDdnsUpdates();
            }
    	}
		return sendReply;    	
    }
}
