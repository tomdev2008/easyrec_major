/**Copyright 2015 Research Studios Austria Forschungsgesellschaft mBH
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
package org.easyrec.plugin.profilesolr.store.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;
import javax.sql.DataSource;
import org.easyrec.model.core.web.Item;
import org.easyrec.plugin.profilesolr.SolrSimilarityGenerator;
import org.easyrec.plugin.profilesolr.model.SolrSimilarityConfigurationInt;
import org.easyrec.plugin.profilesolr.store.dao.SolrItemDAO;
import org.easyrec.service.core.ClusterService;
import org.easyrec.store.dao.core.ItemDAO;
import static org.easyrec.store.dao.core.ItemDAO.DEFAULT_ACTIVE_COLUMN_NAME;
import static org.easyrec.store.dao.core.ItemDAO.DEFAULT_CREATION_DATE_COLUMN_NAME;
import static org.easyrec.store.dao.core.ItemDAO.DEFAULT_DESCRIPTION_COLUMN_NAME;
import static org.easyrec.store.dao.core.ItemDAO.DEFAULT_ID_COLUMN_NAME;
import static org.easyrec.store.dao.core.ItemDAO.DEFAULT_ITEMID_COLUMN_NAME;
import static org.easyrec.store.dao.core.ItemDAO.DEFAULT_ITEMTYPE_COLUMN_NAME;
import static org.easyrec.store.dao.core.ItemDAO.DEFAULT_PROFILEDATA_COLUMN_NAME;
import static org.easyrec.store.dao.core.ItemDAO.DEFAULT_TENANTID_COLUMN_NAME;
import org.easyrec.utils.spring.store.dao.DaoUtils;
import org.easyrec.utils.spring.store.dao.annotation.DAO;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.support.JdbcDaoSupport;

/**
 *
 * @author Stephan
 */
@DAO
public class SolrItemDAOImpl extends JdbcDaoSupport implements SolrItemDAO {

    private final SolrItemRowMapper itemRowMapper = new SolrItemRowMapper();
    
    public SolrItemDAOImpl(DataSource datasource) {
        setDataSource(datasource);
    }
    
    @Override
    public List<Item> getNonUserItems(SolrSimilarityConfigurationInt configuration) {
        
        Object[] args = {configuration.getTenantId(), SolrSimilarityGenerator.ITEMTYPE_USER, ClusterService.ITEMTYPE_CLUSTER};
        int[] argTypes = {Types.INTEGER, Types.VARCHAR, Types.VARCHAR};

        StringBuilder query  = new StringBuilder();
        query.append(" SELECT ID, TENANTID, ITEMID, ITEMTYPE, DESCRIPTION, PROFILEDATA, ACTIVE, CREATIONDATE ")
                .append(" FROM ").append(ItemDAO.DEFAULT_TABLE_NAME).append(" WHERE ").append(" TENANTID = ? ")
                .append(" AND ITEMTYPE NOT IN (?,?) AND ACTIVE=TRUE");

        return getJdbcTemplate().query(query.toString(), args, argTypes, itemRowMapper);

    }
    
    @Override
    public List<Item> getUserItems(SolrSimilarityConfigurationInt configuration) {
        
        Object[] args = {configuration.getTenantId(), SolrSimilarityGenerator.ITEMTYPE_USER};
        int[] argTypes = {Types.INTEGER, Types.VARCHAR};

        StringBuilder query  = new StringBuilder();
        query.append(" SELECT ID, TENANTID, ITEMID, ITEMTYPE, DESCRIPTION, PROFILEDATA, ACTIVE, CREATIONDATE ")
                .append(" FROM ").append(ItemDAO.DEFAULT_TABLE_NAME).append(" WHERE ").append(" TENANTID = ? ")
                .append(" AND ITEMTYPE=? AND ACTIVE=TRUE");

        return getJdbcTemplate().query(query.toString(), args, argTypes, itemRowMapper);

    }
    
    private static class SolrItemRowMapper implements RowMapper<Item> {
        @Override
        public Item mapRow(ResultSet rs, int rowNum) throws SQLException {
             Item i = new Item(DaoUtils.getStringIfPresent(rs, DEFAULT_ID_COLUMN_NAME),
                    DaoUtils.getIntegerIfPresent(rs, DEFAULT_TENANTID_COLUMN_NAME),
                    DaoUtils.getStringIfPresent(rs, DEFAULT_ITEMID_COLUMN_NAME),
                    DaoUtils.getStringIfPresent(rs, DEFAULT_ITEMTYPE_COLUMN_NAME),
                    DaoUtils.getStringIfPresent(rs, DEFAULT_DESCRIPTION_COLUMN_NAME),
                    null,
                    null,
                    null,
                    DaoUtils.getBoolean(rs, DEFAULT_ACTIVE_COLUMN_NAME),
                    DaoUtils.getStringIfPresent(rs, DEFAULT_CREATION_DATE_COLUMN_NAME));
             i.setProfileData(DaoUtils.getStringIfPresent(rs, DEFAULT_PROFILEDATA_COLUMN_NAME));
             return i;
        }
    }

    
}
