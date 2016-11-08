/*
 * Copyright 2015 Research Studios Austria Forschungsgesellschaft mBH
 *
 * This file is part of easyrec.
 *
 * easyrec is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * easyrec is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with easyrec.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.easyrec.plugin.aggregator.store.dao.impl;

import java.net.URI;
import java.sql.Types;
import java.util.Date;
import javax.sql.DataSource;
import org.easyrec.plugin.aggregator.store.dao.AggregatorLogEntryDAO;
import org.easyrec.plugin.model.Version;
import org.springframework.jdbc.core.support.JdbcDaoSupport;

/**
 *
 * @author Stephan
 */
public class AggregatorLogEntryDAOImpl extends JdbcDaoSupport implements AggregatorLogEntryDAO {

    public AggregatorLogEntryDAOImpl(DataSource dataSource) {
        setDataSource(dataSource);
    }
    
    
    @Override
    public Date getLastLogEntryForTenant(int tenantId, int assocTypeId, URI pluginID, Version version) {
        
        StringBuilder query = new StringBuilder();
        query.append("SELECT MAX(startDate) FROM plugin_log WHERE tenantId=? AND pluginId=? AND pluginVersion=? AND assocTypeId=? AND endDate IS NOT NULL");
        Object[] args = {tenantId, pluginID.toString(), version.toString(), assocTypeId};
        int[] argt = {Types.INTEGER, Types.VARCHAR, Types.VARCHAR, Types.INTEGER};

        return getJdbcTemplate().queryForObject(query.toString(), args, argt, Date.class);
    }
    
    
    
    
}
