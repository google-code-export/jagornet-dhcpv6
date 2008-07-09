package com.agr.dhcpv6.option;

import java.io.IOException;
import java.util.List;

import org.apache.mina.common.IoBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.agr.dhcpv6.server.config.xml.OpaqueData;
import com.agr.dhcpv6.server.config.xml.OptionExpression;
import com.agr.dhcpv6.server.config.xml.UserClassOption;

/**
 * <p>Title: DhcpClientIdOption </p>
 * <p>Description: </p>
 * 
 * @author A. Gregory Rabil
 * @version $Revision: $
 */

public class DhcpUserClassOption extends BaseDhcpOption implements DhcpComparableOption
{
	private static Logger log = LoggerFactory.getLogger(DhcpUserClassOption.class);

    private UserClassOption userClassOption;

    public DhcpUserClassOption()
    {
        super();
        userClassOption = new UserClassOption();
    }
    public DhcpUserClassOption(UserClassOption userClassOption)
    {
        super();
        if (userClassOption != null)
            this.userClassOption = userClassOption;
        else
            this.userClassOption = new UserClassOption();
    }

    public UserClassOption getUserClassOption()
    {
        return userClassOption;
    }

    public void setUserClassOption(UserClassOption userClassOption)
    {
        if (userClassOption != null)
            this.userClassOption = userClassOption;
    }

    public int getLength()
    {
        int len = 0;
        List<OpaqueData> userClasses = userClassOption.getUserClasses();
        if (userClasses != null) {
            for (OpaqueData opaque : userClasses) {
                len += OpaqueDataUtil.getLength(opaque);
            }
        }
        return len;
    }
    
    public IoBuffer encode() throws IOException
    {
        IoBuffer iobuf = super.encodeCodeAndLength();
        List<OpaqueData> userClasses = userClassOption.getUserClasses();
        if (userClasses != null) {
            for (OpaqueData opaque : userClasses) {
                OpaqueDataUtil.encode(iobuf, opaque);
            }
        }
        return iobuf.flip();
    }

    public void decode(IoBuffer iobuf) throws IOException
    {
    	int len = super.decodeLength(iobuf);
    	if ((len > 0) && (len <= iobuf.remaining())) {
    		int eof = iobuf.position() + len;
            while (iobuf.position() < eof) {
                OpaqueData opaque = OpaqueDataUtil.decode(iobuf);
                this.addUserClass(opaque);
            }
        }
    }

    public boolean matches(OptionExpression expression)
    {
        if (expression == null)
            return false;
        if (expression.getCode() != this.getCode())
            return false;

        List<OpaqueData> userClasses = userClassOption.getUserClasses();
        if (userClasses != null) {
            for (OpaqueData opaque : userClasses) {
                if (OpaqueDataUtil.matches(expression, opaque)) {
                    // if one of the user classes matches the
                    // expression, then consider it a match
                    return true;
                }
            }
        }
        return false;
    }

    public void addUserClass(String userclass)
    {
        if (userclass != null) {
            OpaqueData opaque = new OpaqueData();
            opaque.setAsciiValue(userclass);
            this.addUserClass(opaque);
        }
    }

    public void addUserClass(byte[] userclass)
    {
        if (userclass != null) {
            OpaqueData opaque = new OpaqueData();
            opaque.setHexValue(userclass);
            this.addUserClass(opaque);
        }
    }
    
    public void addUserClass(OpaqueData opaque)
    {
        userClassOption.getUserClasses().add(opaque);
    }

    public int getCode()
    {
        return userClassOption.getCode();
    }

    public String getName()
    {
        return userClassOption.getName();
    }
    
    public String toString()
    {
        if (userClassOption == null)
            return null;
        
        StringBuilder sb = new StringBuilder(userClassOption.getName());
        sb.append(": ");
        List<OpaqueData> userClasses = userClassOption.getUserClasses();
        if (userClasses != null) {
            for (OpaqueData opaque : userClasses) {
                sb.append(OpaqueDataUtil.toString(opaque));
                sb.append(",");
            }
            sb.setLength(sb.length()-1);
        }
        return sb.toString();
    }

}
