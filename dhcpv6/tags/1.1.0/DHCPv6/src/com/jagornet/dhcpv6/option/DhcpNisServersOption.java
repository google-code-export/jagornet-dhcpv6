/*
 * Copyright 2009 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file DhcpNisServersOption.java is part of DHCPv6.
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

import com.jagornet.dhcpv6.option.base.BaseIpAddressListOption;
import com.jagornet.dhcpv6.xml.NisServersOption;

/**
 * <p>Title: DhcpNisServersOption </p>
 * <p>Description: </p>.
 * 
 * @author A. Gregory Rabil
 */
public class DhcpNisServersOption extends BaseIpAddressListOption
{
	
	/**
	 * Instantiates a new dhcp nis servers option.
	 */
	public DhcpNisServersOption()
	{
		this(null);
	}
	
	/**
	 * Instantiates a new dhcp nis servers option.
	 * 
	 * @param nisServersOption the nis servers option
	 */
	public DhcpNisServersOption(NisServersOption nisServersOption)
	{
		if (nisServersOption != null)
			this.ipAddressListOption = nisServersOption;
		else
			this.ipAddressListOption = NisServersOption.Factory.newInstance();
	}
	
    /* (non-Javadoc)
     * @see com.jagornet.dhcpv6.option.DhcpOption#getCode()
     */
    public int getCode()
    {
        return ((NisServersOption)ipAddressListOption).getCode();
    }
}
