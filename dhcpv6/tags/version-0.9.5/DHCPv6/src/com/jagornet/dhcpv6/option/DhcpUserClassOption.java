/*
 * Copyright 2009 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file DhcpUserClassOption.java is part of DHCPv6.
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

import com.jagornet.dhcpv6.option.base.BaseOpaqueDataListOption;
import com.jagornet.dhcpv6.xml.UserClassOption;

/**
 * <p>Title: DhcpClientIdOption </p>
 * <p>Description: </p>.
 * 
 * @author A. Gregory Rabil
 */
public class DhcpUserClassOption extends BaseOpaqueDataListOption
{
	public DhcpUserClassOption()
	{
		this(null);
	}
	
	public DhcpUserClassOption(UserClassOption userClassOption)
	{
		if (userClassOption != null)
			this.opaqueDataListOption = userClassOption;
		else
			this.opaqueDataListOption = UserClassOption.Factory.newInstance();
	}
	
	/* (non-Javadoc)
     * @see com.jagornet.dhcpv6.option.DhcpOption#getCode()
     */
    public int getCode()
    {
        return ((UserClassOption)opaqueDataListOption).getCode();
    }
}
