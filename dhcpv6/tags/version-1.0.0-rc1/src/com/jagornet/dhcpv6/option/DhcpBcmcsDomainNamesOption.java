/*
 * Copyright 2009 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file DhcpBcmcsDomainNamesOption.java is part of DHCPv6.
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
package com.jagornet.dhcpv6.option;

import com.jagornet.dhcpv6.option.base.BaseDomainNameListOption;
import com.jagornet.dhcpv6.xml.BcmcsDomainNamesOption;

/**
 * <p>Title: DhcpBcmcsDomainNamesOption </p>
 * <p>Description: </p>.
 * 
 * @author A. Gregory Rabil
 */
public class DhcpBcmcsDomainNamesOption extends BaseDomainNameListOption
{
	
	/**
	 * Instantiates a new dhcp bcmcs domain names option.
	 */
	public DhcpBcmcsDomainNamesOption()
	{
		this(null);
	}
	
	/**
	 * Instantiates a new dhcp bcmcs domain names option.
	 * 
	 * @param bcmcsDomainNamesOption the bcmcs domain names option
	 */
	public DhcpBcmcsDomainNamesOption(BcmcsDomainNamesOption bcmcsDomainNamesOption)
	{
		if (bcmcsDomainNamesOption != null)
			this.domainNameListOption = bcmcsDomainNamesOption;
		else
			this.domainNameListOption = BcmcsDomainNamesOption.Factory.newInstance();
	}

	/* (non-Javadoc)
     * @see com.jagornet.dhcpv6.option.DhcpOption#getCode()
     */
    public int getCode()
    {
        return ((BcmcsDomainNamesOption)domainNameListOption).getCode();
    }
}
