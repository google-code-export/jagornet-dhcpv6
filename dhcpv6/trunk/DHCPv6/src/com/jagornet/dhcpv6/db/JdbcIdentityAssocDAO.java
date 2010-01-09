/*
 * Copyright 2009 Jagornet Technologies, LLC.  All Rights Reserved.
 *
 * This software is the proprietary information of Jagornet Technologies, LLC. 
 * Use is subject to license terms.
 *
 */

/*
 *   This file JdbcIdentityAssocDAO.java is part of DHCPv6.
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
package com.jagornet.dhcpv6.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcDaoSupport;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;

// TODO: Auto-generated Javadoc
/**
 * The Class JdbcIdentityAssocDAO.
 * 
 * @author agrabil
 */
public class JdbcIdentityAssocDAO extends SimpleJdbcDaoSupport implements IdentityAssocDAO 
{

	/* (non-Javadoc)
	 * @see com.jagornet.dhcpv6.db.IdentityAssocDAO#create(com.jagornet.dhcpv6.db.IdentityAssoc)
	 */
	public void create(IdentityAssoc ia) 
	{
		SimpleJdbcInsert insertIa = new SimpleJdbcInsert(getDataSource())
                    							.withTableName("identityassoc")
                    							.usingGeneratedKeyColumns("id");
		
        Map<String, Object> parameters = new HashMap<String, Object>(4);
        parameters.put("duid", ia.getDuid());
        parameters.put("iatype", ia.getIatype());
        parameters.put("iaid", ia.getIaid());
        parameters.put("state", ia.getState());
//        Number newId = insertIa.executeAndReturnKey(parameters);
//        ia.setId(newId.longValue());
        insertIa.execute(parameters);
        IdentityAssoc newIa = getByKey(ia.getDuid(), ia.getIatype(), ia.getIaid());
        if (newIa != null) {
        	ia.setId(newIa.getId());
        }
	}

	/* (non-Javadoc)
	 * @see com.jagornet.dhcpv6.db.IdentityAssocDAO#update(com.jagornet.dhcpv6.db.IdentityAssoc)
	 */
	public void update(IdentityAssoc ia)
	{
		String updateQuery = "update identityassoc" +
							" set duid=?," +
							" iatype=?," +
							" iaid=?," +
							" state=?" +
							" where id=?";
		getJdbcTemplate().update(updateQuery, 
				new Object[] { ia.getDuid(), 
							   ia.getIatype(), 
							   ia.getIaid(), 
							   ia.getState(), 
							   ia.getId() });
	}

	/* (non-Javadoc)
	 * @see com.jagornet.dhcpv6.db.IdentityAssocDAO#deleteById(java.lang.Long)
	 */
	public void deleteById(Long id)
	{
        getSimpleJdbcTemplate().update(
        		"delete from identityassoc where id = ?", id);
    }

	/* (non-Javadoc)
	 * @see com.jagornet.dhcpv6.db.IdentityAssocDAO#getById()
	 */
	public IdentityAssoc getById(Long id) 
	{
		try {
		    return getSimpleJdbcTemplate().queryForObject(
		            "select * from identityassoc where id = ?", 
		            new IaRowMapper(), id);
		}
		catch (EmptyResultDataAccessException ex) {
			return null;
		}
	}

	/* (non-Javadoc)
	 * @see com.jagornet.dhcpv6.db.IdentityAssocDAO#getByKey(byte[], byte, long)
	 */
	public IdentityAssoc getByKey(byte[] duid, byte iatype, long iaid) 
	{
		try {
	        return getSimpleJdbcTemplate().queryForObject(
	                "select * from identityassoc where duid = ? and iatype = ? and iaid = ?", 
	                new IaRowMapper(), duid, iatype, iaid);
		}
		catch (EmptyResultDataAccessException ex) {
			// TODO: log message
			return null;
		}
	}

	/* (non-Javadoc)
	 * @see com.jagornet.dhcpv6.db.IdentityAssocDAO#findAllByDuid(byte[])
	 */
	public List<IdentityAssoc> findAllByDuid(byte[] duid) {
        return getSimpleJdbcTemplate().query(
                "select * from identityassoc where duid = ?", 
                new IaRowMapper(), duid);
	}

	/* (non-Javadoc)
	 * @see com.jagornet.dhcpv6.db.IdentityAssocDAO#findAllByIaid(long)
	 */
	public List<IdentityAssoc> findAllByIaid(long iaid) {
        return getSimpleJdbcTemplate().query(
                "select * from identityassoc where iaid = ?", 
                new IaRowMapper(), iaid);
	}

    /**
     * The Class IaRowMapper.
     */
    protected class IaRowMapper implements ParameterizedRowMapper<IdentityAssoc> 
    {	
        
        /* (non-Javadoc)
         * @see org.springframework.jdbc.core.simple.ParameterizedRowMapper#mapRow(java.sql.ResultSet, int)
         */
        public IdentityAssoc mapRow(ResultSet rs, int rowNum) throws SQLException {
        	IdentityAssoc ia = new IdentityAssoc();
        	ia.setId(rs.getLong("id"));
        	ia.setDuid(rs.getBytes("duid"));
        	ia.setIatype(rs.getByte("iatype"));
        	ia.setIaid(rs.getLong("iaid"));
        	ia.setState(rs.getByte("state"));
            return ia;
        }
    }
}
