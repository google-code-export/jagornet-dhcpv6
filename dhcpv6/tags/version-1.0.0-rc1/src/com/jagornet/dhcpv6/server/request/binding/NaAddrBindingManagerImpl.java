/*
 * Copyright 2009 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file NaAddrBindingManagerImpl.java is part of DHCPv6.
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
package com.jagornet.dhcpv6.server.request.binding;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import com.jagornet.dhcpv6.db.IdentityAssoc;
import com.jagornet.dhcpv6.message.DhcpMessage;
import com.jagornet.dhcpv6.option.DhcpClientIdOption;
import com.jagornet.dhcpv6.option.DhcpIaAddrOption;
import com.jagornet.dhcpv6.option.DhcpIaNaOption;
import com.jagornet.dhcpv6.server.config.DhcpServerConfigException;
import com.jagornet.dhcpv6.xml.AddressBindingsType;
import com.jagornet.dhcpv6.xml.AddressPoolsType;
import com.jagornet.dhcpv6.xml.Link;
import com.jagornet.dhcpv6.xml.LinkFilter;

/**
 * The Class NaAddrBindingManagerImpl.
 * 
 * @author A. Gregory Rabil
 */
public class NaAddrBindingManagerImpl 
		extends AddressBindingManager 
		implements NaAddrBindingManager
{
	
	/**
	 * Instantiates a new na addr binding manager impl.
	 * 
	 * @throws DhcpServerConfigException the dhcp server config exception
	 */
	public NaAddrBindingManagerImpl() throws DhcpServerConfigException
	{
		super();
	}
	
	/* (non-Javadoc)
	 * @see com.jagornet.dhcpv6.server.request.binding.AddressBindingManager#getAddressBindingsType(com.jagornet.dhcpv6.xml.Link)
	 */
	@Override
	protected AddressBindingsType getAddressBindingsType(Link link) {
		return link.getNaAddrBindings();
	}

	/* (non-Javadoc)
	 * @see com.jagornet.dhcpv6.server.request.binding.AddressBindingManager#getAddressPoolsType(com.jagornet.dhcpv6.xml.LinkFilter)
	 */
	@Override
	protected AddressPoolsType getAddressPoolsType(LinkFilter linkFilter) {
		return linkFilter.getNaAddrPools();
	}

	/* (non-Javadoc)
	 * @see com.jagornet.dhcpv6.server.request.binding.AddressBindingManager#getAddressPoolsType(com.jagornet.dhcpv6.xml.Link)
	 */
	@Override
	protected AddressPoolsType getAddressPoolsType(Link link) {
		return link.getNaAddrPools();
	}

	/* (non-Javadoc)
	 * @see com.jagornet.dhcpv6.server.request.binding.NaAddrBindingManager#findCurrentBinding(com.jagornet.dhcpv6.xml.Link, com.jagornet.dhcpv6.option.DhcpClientIdOption, com.jagornet.dhcpv6.option.DhcpIaNaOption, com.jagornet.dhcpv6.message.DhcpMessage)
	 */
	@Override
	public Binding findCurrentBinding(Link clientLink,
			DhcpClientIdOption clientIdOption, DhcpIaNaOption iaNaOption,
			DhcpMessage requestMsg) {
		
		byte[] duid = clientIdOption.getDuid();
		long iaid = iaNaOption.getIaNaOption().getIaId();
		
		return super.findCurrentBinding(clientLink, duid, IdentityAssoc.NA_TYPE, 
				iaid, requestMsg);
	}

	/* (non-Javadoc)
	 * @see com.jagornet.dhcpv6.server.request.binding.NaAddrBindingManager#createSolicitBinding(com.jagornet.dhcpv6.xml.Link, com.jagornet.dhcpv6.option.DhcpClientIdOption, com.jagornet.dhcpv6.option.DhcpIaNaOption, com.jagornet.dhcpv6.message.DhcpMessage, boolean)
	 */
	@Override
	public Binding createSolicitBinding(Link clientLink,
			DhcpClientIdOption clientIdOption, DhcpIaNaOption iaNaOption,
			DhcpMessage requestMsg, boolean rapidCommit) {
		
		byte[] duid = clientIdOption.getDuid();
		long iaid = iaNaOption.getIaNaOption().getIaId();
		
		List<InetAddress> requestAddrs = getInetAddrs(iaNaOption);
		
		return super.createSolicitBinding(clientLink, duid, IdentityAssoc.NA_TYPE, 
				iaid, requestAddrs, requestMsg, rapidCommit);
	}

	/* (non-Javadoc)
	 * @see com.jagornet.dhcpv6.server.request.binding.NaAddrBindingManager#updateBinding(com.jagornet.dhcpv6.server.request.binding.Binding, com.jagornet.dhcpv6.xml.Link, com.jagornet.dhcpv6.option.DhcpClientIdOption, com.jagornet.dhcpv6.option.DhcpIaNaOption, com.jagornet.dhcpv6.message.DhcpMessage, byte)
	 */
	@Override
	public Binding updateBinding(Binding binding, Link clientLink,
			DhcpClientIdOption clientIdOption, DhcpIaNaOption iaNaOption,
			DhcpMessage requestMsg, byte state) {
		
		byte[] duid = clientIdOption.getDuid();
		long iaid = iaNaOption.getIaNaOption().getIaId();
		
		List<InetAddress> requestAddrs = getInetAddrs(iaNaOption);
		
		return super.updateBinding(binding, clientLink, duid, IdentityAssoc.NA_TYPE,
				iaid, requestAddrs, requestMsg, state);
	}
	
	/**
	 * Extract the list of IP addresses from within the given IA_NA option.
	 * 
	 * @param iaNaOption the IA_NA option
	 * 
	 * @return the list of InetAddresses for the IPs in the IA_NA option
	 */
	private List<InetAddress> getInetAddrs(DhcpIaNaOption iaNaOption)
	{
		List<InetAddress> inetAddrs = null;
		List<DhcpIaAddrOption> iaAddrs = iaNaOption.getIaAddrOptions();
		if ((iaAddrs != null) && !iaAddrs.isEmpty()) {
			inetAddrs = new ArrayList<InetAddress>();
			for (DhcpIaAddrOption iaAddr : iaAddrs) {
				InetAddress inetAddr = iaAddr.getInetAddress();
				inetAddrs.add(inetAddr);
			}
		}
		return inetAddrs;
	}
}
