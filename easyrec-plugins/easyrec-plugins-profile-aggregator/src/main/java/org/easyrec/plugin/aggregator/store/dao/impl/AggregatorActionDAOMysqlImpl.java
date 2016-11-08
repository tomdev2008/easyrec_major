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
package org.easyrec.plugin.aggregator.store.dao.impl;

import com.google.common.collect.Lists;
import com.google.common.primitives.Ints;
import gnu.trove.map.hash.TObjectIntHashMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.easyrec.model.core.ItemVO;
import org.easyrec.store.dao.BaseActionDAO;
import org.easyrec.utils.spring.store.dao.annotation.DAO;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.support.JdbcDaoSupport;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Date;
import java.util.List;
import org.easyrec.model.core.ActionVO;
import org.easyrec.plugin.aggregator.model.AggregatorConfigurationInt;
import org.easyrec.plugin.aggregator.store.dao.AggregatorActionDAO;
import static org.easyrec.store.dao.BaseActionDAO.DEFAULT_ACTIONINFO_COLUMN_NAME;
import static org.easyrec.store.dao.BaseActionDAO.DEFAULT_ACTION_TIME_COLUMN_NAME;
import static org.easyrec.store.dao.BaseActionDAO.DEFAULT_ACTION_TYPE_COLUMN_NAME;
import static org.easyrec.store.dao.BaseActionDAO.DEFAULT_ID_COLUMN_NAME;
import static org.easyrec.store.dao.BaseActionDAO.DEFAULT_IP_COLUMN_NAME;
import static org.easyrec.store.dao.BaseActionDAO.DEFAULT_ITEM_COLUMN_NAME;
import static org.easyrec.store.dao.BaseActionDAO.DEFAULT_ITEM_TYPE_COLUMN_NAME;
import static org.easyrec.store.dao.BaseActionDAO.DEFAULT_RATING_VALUE_COLUMN_NAME;
import static org.easyrec.store.dao.BaseActionDAO.DEFAULT_SESSION_COLUMN_NAME;
import static org.easyrec.store.dao.BaseActionDAO.DEFAULT_TENANT_COLUMN_NAME;
import static org.easyrec.store.dao.BaseActionDAO.DEFAULT_USER_COLUMN_NAME;
import org.easyrec.utils.spring.store.dao.DaoUtils;
import org.springframework.jdbc.core.RowMapper;

/**
 * This class provides methods to access data in a datamining/rulemining database.
 * <p/>
 * <p><b>Company:&nbsp;</b>
 * SAT, Research Studios Austria</p>
 * <p/>
 * <p><b>Copyright:&nbsp;</b>
 * (c) 2006</p>
 * <p/>
 * <p><b>last modified:</b><br/>
 * $Author: szavrel $<br/>
 * $Date: 2011-02-11 18:35:47 +0100 (Fr, 11 Feb 2011) $<br/>
 * $Revision: 17681 $</p>
 *
 * @author Stephan Zavrel
 */
@DAO
public class AggregatorActionDAOMysqlImpl extends JdbcDaoSupport implements AggregatorActionDAO {

    private ActionVORowMapper actionVORowMapper = new ActionVORowMapper();
    


    //////////////////////////////////////////////////////////////////////////////
    // constructor
    public AggregatorActionDAOMysqlImpl(DataSource dataSource) {
        setDataSource(dataSource);
    }

   
    @Override
    public List<Integer> getUsersWithActions(AggregatorConfigurationInt configuration) {
        //Todo: only get non-anonymous users! -> done using idMappingDAO later 
        //Todo: move to iterator
        List<Object> args = Lists.newArrayList();
        List<Integer> argt = Lists.newArrayList();
        
        // get all Baskets
        StringBuilder query = new StringBuilder();
        query.append("SELECT DISTINCT(a.").append(BaseActionDAO.DEFAULT_USER_COLUMN_NAME);
        query.append(") FROM ").append(BaseActionDAO.DEFAULT_TABLE_NAME).append(" a, idmapping i");
        query.append(" WHERE ").append(BaseActionDAO.DEFAULT_TENANT_COLUMN_NAME).append("=")
                .append(configuration.getTenantId());
        if (configuration.getActionType() != null) {
        query.append(" AND ").append(BaseActionDAO.DEFAULT_ACTION_TYPE_COLUMN_NAME).append("=?");
                args.add(configuration.getActionType());
                argt.add(Types.INTEGER);
        }
        if (configuration.getLastRun() != null) {
        query.append(" AND ").append(BaseActionDAO.DEFAULT_ACTION_TIME_COLUMN_NAME).append(">=?");
                args.add(configuration.getLastRun());
                argt.add(Types.TIMESTAMP);
        }
        // filter anonymous users
        query.append(" AND a.userId=i.intId AND a.sessionId!=i.stringId");
        
        
        List<Integer> baskets = getJdbcTemplate().queryForList(query.toString(), args.toArray(),
                Ints.toArray(argt), Integer.class);
            
        return baskets;
        
    }
    
    @Override
    public List<ActionVO<Integer,Integer>> getActionsForUsers(Integer userId, AggregatorConfigurationInt configuration) {
        
        //sort by itemID, then check on itemID,typeID change look in profile -> this way only 1 query is needed 
        List<Object> args = Lists.newArrayList();
        List<Integer> argt = Lists.newArrayList();
        
        // get all Baskets
        StringBuilder query = new StringBuilder();
        query.append("SELECT * FROM ").append(BaseActionDAO.DEFAULT_TABLE_NAME);
        query.append(" WHERE ").append(BaseActionDAO.DEFAULT_TENANT_COLUMN_NAME).append("=")
                .append(configuration.getTenantId()).append(" AND ").append(BaseActionDAO.DEFAULT_USER_COLUMN_NAME)
                .append("=").append(userId);
        if (configuration.getActionType() != null) {
        query.append(" AND ").append(BaseActionDAO.DEFAULT_ACTION_TYPE_COLUMN_NAME).append("=?");
                args.add(configuration.getActionType());
                argt.add(Types.INTEGER);
        }
        
        // delta updates don't work since we only store Top x of every field
//        if (configuration.getLastRun() != null) {
//            query.append(" AND ").append(BaseActionDAO.DEFAULT_ACTION_TIME_COLUMN_NAME).append(">=?");
//                args.add(configuration.getLastRun());
//                argt.add(Types.TIMESTAMP);
//        }
        
        query.append(" ORDER BY ").append(BaseActionDAO.DEFAULT_ITEM_COLUMN_NAME).append(" ASC");
        
        
        return getJdbcTemplate().query(query.toString(), args.toArray(), Ints.toArray(argt), actionVORowMapper);
        
        
        // only if config for itemprofile, load the item profile
        
    }

    @Override
    public int getNumberOfActions(Integer tenantId, Integer actionType, Date lastRun) {
        List<Object> args = Lists.newArrayList();
        List<Integer> argt = Lists.newArrayList();
        
        StringBuilder query = new StringBuilder("SELECT count(1) as cnt FROM ");
        query.append(BaseActionDAO.DEFAULT_TABLE_NAME);
        query.append(" WHERE ").append(BaseActionDAO.DEFAULT_TENANT_COLUMN_NAME).append("=?");
        args.add(tenantId);
        argt.add(Types.INTEGER);
        if (actionType!=null) {
            query.append(" AND ").append(BaseActionDAO.DEFAULT_ACTION_TYPE_COLUMN_NAME).append("=?");
            args.add(actionType);
            argt.add(Types.INTEGER);
        }
        // we always need to consider all actions since we don't store the complete user profile
//        if (lastRun != null) {
//            query.append(" AND ").append(BaseActionDAO.DEFAULT_ACTION_TIME_COLUMN_NAME).append(">=?");
//            args.add(lastRun);
//            argt.add(Types.TIMESTAMP);
//        }

        return getJdbcTemplate().queryForInt(query.toString(), args.toArray(), Ints.toArray(argt));
    }
    
    private class ActionVORowMapper implements RowMapper<ActionVO<Integer, Integer>> {
        @Override
        public ActionVO<Integer, Integer> mapRow(ResultSet rs, int rowNum)
                throws SQLException {
            ActionVO<Integer, Integer> actionVO =
                    new ActionVO<>(
                            DaoUtils.getLong(rs, DEFAULT_ID_COLUMN_NAME),
                            DaoUtils.getInteger(rs, DEFAULT_TENANT_COLUMN_NAME),
                            DaoUtils.getInteger(rs, DEFAULT_USER_COLUMN_NAME),
                            DaoUtils.getStringIfPresent(rs, DEFAULT_SESSION_COLUMN_NAME),
                            DaoUtils.getStringIfPresent(rs, DEFAULT_IP_COLUMN_NAME),
                            new ItemVO<>(DaoUtils.getInteger(rs, DEFAULT_TENANT_COLUMN_NAME),
                                    DaoUtils.getInteger(rs, DEFAULT_ITEM_COLUMN_NAME),
                                    DaoUtils.getInteger(rs, DEFAULT_ITEM_TYPE_COLUMN_NAME)),
                            DaoUtils.getInteger(rs, DEFAULT_ACTION_TYPE_COLUMN_NAME),
                            DaoUtils.getInteger(rs, DEFAULT_RATING_VALUE_COLUMN_NAME),
                            DaoUtils.getStringIfPresent(rs, DEFAULT_ACTIONINFO_COLUMN_NAME),
                            DaoUtils.getDate(rs, DEFAULT_ACTION_TIME_COLUMN_NAME));
            return actionVO;
        }
    }
    
    private static class ActionResultSetExtractor
            implements ResultSetExtractor<TObjectIntHashMap<ItemVO<Integer, Integer>>> {

        private int minSupp;
        // logging
        private final Log logger = LogFactory.getLog(this.getClass());

        @Override
        public TObjectIntHashMap<ItemVO<Integer, Integer>> extractData(ResultSet rs) {
            TObjectIntHashMap<ItemVO<Integer, Integer>> map = new TObjectIntHashMap<>();
            int itemId, itemTypeId, tenantId, cnt = 0;

            try {
                while (rs.next()) {
                    itemId = rs.getInt(BaseActionDAO.DEFAULT_ITEM_COLUMN_NAME);
                    itemTypeId = rs.getInt(BaseActionDAO.DEFAULT_ITEM_TYPE_COLUMN_NAME);
                    tenantId = rs.getInt(BaseActionDAO.DEFAULT_TENANT_COLUMN_NAME);
                    cnt = rs.getInt("cnt");
                    map.put(new ItemVO<>(tenantId, itemId, itemTypeId), cnt);
                }
                // optimization: replaces former adjustSupport method
                minSupp = cnt;
            } catch (SQLException e) {
                logger.error("An error occured during ResultSet extraction", e);
                throw new RuntimeException(e);
            }
            return map;
        }

        public Integer getMinSupp() {
            return this.minSupp;
        }
    }
    
}
