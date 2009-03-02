/*
 * Copyright 2009 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file DhcpNisPlusDomainNameOption.java is part of DHCPv6.
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

import com.jagornet.dhcpv6.xml.NisPlusDomainNameOption;

/**
 * <p>Title: DhcpNisPlusDomainNameOption </p>
 * <p>Description: </p>.
 * 
 * @author A. Gregory Rabil
 */
public class DhcpNisPlusDomainNameOption extends BaseDomainNameOption
{
    /** The nis plus domain name option. */
    private NisPlusDomainNameOption nisPlusDomainNameOption;

    /**
     * Instantiates a new dhcp nis plus domain name option.
     */
    public DhcpNisPlusDomainNameOption()
    {
        this(null);
    }
    
    /**
     * Instantiates a new dhcp nis plus domain name option.
     * 
     * @param nisPlusDomainNameOption the nis plus domain name option
     */
    public DhcpNisPlusDomainNameOption(NisPlusDomainNameOption nisPlusDomainNameOption)
    {
        super();
        if (nisPlusDomainNameOption != null)
            this.nisPlusDomainNameOption = nisPlusDomainNameOption;
        else
            this.nisPlusDomainNameOption = NisPlusDomainNameOption.Factory.newInstance();
    }

    /**
     * Gets the nis plus domain name option.
     * 
     * @return the nis plus domain name option
     */
    public NisPlusDomainNameOption getNisPlusDomainNameOption()
    {
        return nisPlusDomainNameOption;
    }

    /**
     * Sets the nis plus domain name option.
     * 
     * @param nisPlusDomainNameOption the new nis plus domain name option
     */
    public void setNisPlusDomainNameOption(NisPlusDomainNameOption nisPlusDomainNameOption)
    {
        if (nisPlusDomainNameOption != null)
            this.nisPlusDomainNameOption = nisPlusDomainNameOption;
    }

    /* (non-Javadoc)
     * @see com.jagornet.dhcpv6.option.DhcpOption#getCode()
     */
    public int getCode()
    {
        return nisPlusDomainNameOption.getCode();
    }

    /* (non-Javadoc)
     * @see com.jagornet.dhcpv6.option.BaseDomainNameOption#getDomainName()
     */
    public String getDomainName()
    {
        return nisPlusDomainNameOption.getDomainName();
    }
    
    /* (non-Javadoc)
     * @see com.jagornet.dhcpv6.option.BaseDomainNameOption#setDomainName(java.lang.String)
     */
    public void setDomainName(String domainName)
    {
        nisPlusDomainNameOption.setDomainName(domainName);
    }
}
