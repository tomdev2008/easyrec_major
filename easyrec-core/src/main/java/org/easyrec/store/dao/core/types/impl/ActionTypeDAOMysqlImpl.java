/*
 * Copyright 2010 Research Studios Austria Forschungsgesellschaft mBH
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
package org.easyrec.store.dao.core.types.impl;

import org.easyrec.store.dao.core.types.ActionTypeDAO;
import org.easyrec.utils.spring.cache.annotation.InvalidatesCache;
import org.easyrec.utils.spring.cache.annotation.LongCacheable;
import org.easyrec.utils.spring.store.dao.DaoUtils;
import org.easyrec.utils.spring.store.dao.annotation.DAO;
import org.easyrec.utils.spring.store.dao.impl.AbstractTableCreatingDAOImpl;
import org.easyrec.utils.spring.store.service.sqlscript.SqlScriptService;
import org.springframework.jdbc.core.ResultSetExtractor;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeSet;
import org.easyrec.model.core.ActionTypeVO;
import static org.easyrec.store.dao.core.types.ActionTypeDAO.DEFAULT_ID_COLUMN_NAME;
import static org.easyrec.store.dao.core.types.ActionTypeDAO.DEFAULT_NAME_COLUMN_NAME;

/**
 * This class provides a Mysql implementation of the {@link org.easyrec.store.dao.core.types.ActionTypeDAO} interface.
 * <p/>
 * <p><b>Company:&nbsp;</b>
 * SAT, Research Studios Austria</p>
 * <p/>
 * <p><b>Copyright:&nbsp;</b>
 * (c) 2007</p>
 * <p/>
 * <p><b>last modified:</b><br/>
 * $Author: dmann $<br/>
 * $Date: 2011-11-11 14:29:25 +0100 (Fr, 11 Nov 2011) $<br/>
 * $Revision: 18635 $</p>
 *
 * @author Roman Cerny
 */

@DAO
public class ActionTypeDAOMysqlImpl extends AbstractTableCreatingDAOImpl implements ActionTypeDAO {
    // constants
    private final static String TABLE_CREATING_SQL_SCRIPT_NAME = "classpath:sql/core/types/ActionType.sql";

    // constructor 
    public ActionTypeDAOMysqlImpl(DataSource dataSource, SqlScriptService sqlScriptService) {
        super(sqlScriptService);
        setDataSource(dataSource);

        // output connection information
        if (logger.isInfoEnabled()) {
            try {
                logger.info(DaoUtils.getDatabaseURLAndUserName(dataSource));
            } catch (Exception e) {
                logger.error(e);
            }
        }
    }

    // abstract template method of 'TableCreatingDAOImpl' implementation
    @Override
    public String getDefaultTableName() {
        return DEFAULT_TABLE_NAME;
    }

    @Override
    public String getTableCreatingSQLScriptName() {
        return TABLE_CREATING_SQL_SCRIPT_NAME;
    }

    // interface 'ActionTypeDAO' implementation
    @LongCacheable
    @Override
    public String getTypeById(Integer tenantId, final Integer id) {
        // validate
        if (tenantId == null) {
            throw new IllegalArgumentException("missing constraints: missing 'tenantId'");
        }
        if (id == null) {
            return null;
        }

        ResultSetExtractor<String> rse = new ResultSetExtractor<String>() {
            public String extractData(ResultSet rs) {
                try {
                    if (rs.next()) {
                        return DaoUtils.getStringIfPresent(rs, DEFAULT_NAME_COLUMN_NAME);
                    }
                } catch (SQLException e) {
                    logger.error("error occured", e);
                    throw new RuntimeException(e);
                }
                throw new IllegalArgumentException("unknown id: '" + id + "' for action type");
            }
        };

        StringBuilder sqlQuery = new StringBuilder("SELECT ");
        sqlQuery.append(DEFAULT_NAME_COLUMN_NAME);
        sqlQuery.append(" FROM ");
        sqlQuery.append(DEFAULT_TABLE_NAME);
        sqlQuery.append(" WHERE ");
        sqlQuery.append(DEFAULT_TENANT_COLUMN_NAME);
        sqlQuery.append("=? AND ");
        sqlQuery.append(DEFAULT_ID_COLUMN_NAME);
        sqlQuery.append("=?");

        Object[] args = {tenantId, id};
        int[] argTypes = {Types.INTEGER, Types.INTEGER};
        return getJdbcTemplate().query(sqlQuery.toString(), args, argTypes, rse);
    }

    @LongCacheable
    @Override
    public Integer getIdOfType(Integer tenantId, final String actionType) {
        // validate
        if (tenantId == null) {
            throw new IllegalArgumentException("missing constraints: missing 'tenantId'");
        }
        if (actionType == null) {
            return null;
        }
        ResultSetExtractor<Integer> rse = new ResultSetExtractor<Integer>() {
            public Integer extractData(ResultSet rs) {
                try {
                    if (rs.next()) {
                        return DaoUtils.getInteger(rs, DEFAULT_ID_COLUMN_NAME);
                    }
                } catch (SQLException e) {
                    logger.error("error occured", e);
                    throw new RuntimeException(e);
                }
                throw new IllegalArgumentException("unknown action type: '" + actionType + "'");
            }
        };

        StringBuilder sqlQuery = new StringBuilder("SELECT ");
        sqlQuery.append(DEFAULT_ID_COLUMN_NAME);
        sqlQuery.append(" FROM ");
        sqlQuery.append(DEFAULT_TABLE_NAME);
        sqlQuery.append(" WHERE ");
        sqlQuery.append(DEFAULT_TENANT_COLUMN_NAME);
        sqlQuery.append("=? AND ");
        sqlQuery.append(DEFAULT_NAME_COLUMN_NAME);
        sqlQuery.append(" LIKE ?");

        Object[] args = {tenantId, actionType};
        int[] argTypes = {Types.INTEGER, Types.VARCHAR};
        return getJdbcTemplate().query(sqlQuery.toString(), args, argTypes, rse);
    }

    @Override
    public int insertOrUpdate(Integer tenantId, String actionType, Integer id, boolean hasValue, Integer weight) {
        // validate
        if (tenantId == null) {
            throw new IllegalArgumentException("missing constraints: missing 'tenantId'");
        }
        if (actionType == null) {
            throw new IllegalArgumentException("missing constraints: missing 'actionType'");
        }
        if (id == null) {
            throw new IllegalArgumentException("missing constraints: missing 'id'");
        }
        StringBuilder query = new StringBuilder();
        boolean update;
        if (existsType(tenantId, actionType) == null) {
            query.append("INSERT INTO ");
            update = false;
        } else {
            query.append("UPDATE ");
            update = true;
        }
        query.append(DEFAULT_TABLE_NAME);
        query.append(" SET ");
        query.append(DEFAULT_TENANT_COLUMN_NAME);
        query.append("=?, ");
        query.append(DEFAULT_NAME_COLUMN_NAME);
        query.append("=?, ");
        query.append(DEFAULT_ID_COLUMN_NAME);
        query.append("=?, ");
        query.append(DEFAULT_HAS_VALUE_COLUMN_NAME);
        query.append("=?, ");
        query.append(DEFAULT_WEIGHT_COLUMN_NAME);
        query.append("=?");

        Object[] args;
        int[] argTypes;

        if (update) {
            query.append(" WHERE ");
            query.append(DEFAULT_TENANT_COLUMN_NAME);
            query.append("=? AND ");
            query.append(DEFAULT_NAME_COLUMN_NAME);
            query.append("=?");
            args = new Object[]{tenantId, actionType, id, hasValue, weight,  tenantId, actionType};
            argTypes = new int[]{Types.INTEGER, Types.VARCHAR, Types.INTEGER, Types.BOOLEAN, Types.INTEGER, Types.INTEGER, Types.VARCHAR};
        } else {
            args = new Object[]{tenantId, actionType, id, hasValue, weight};
            argTypes = new int[]{Types.INTEGER, Types.VARCHAR, Types.INTEGER, Types.BOOLEAN, Types.INTEGER};
        }
        return getJdbcTemplate().update(query.toString(), args, argTypes);
    }

    
    @Override
    public int insertOrUpdate(Integer tenantId, String actionType, Integer id, boolean hasValue) {
        return insertOrUpdate(tenantId, actionType, id, hasValue, 1);
    }
    
    @Override
    public int insertOrUpdate(Integer tenantId, String actionType, Integer id) {
        
        return insertOrUpdate(tenantId, actionType, id, false);
    }

    @Override
    public int insertOrUpdate(Integer tenantId, String actionType) {
        return insertOrUpdate(tenantId, actionType, (Boolean) null);
    }

    @Override
    public int insertOrUpdate(ActionTypeVO actionType) {
        return insertOrUpdate(actionType.getTenantId(), actionType.getName(), actionType.getId(), actionType.getHasValue(), actionType.getWeight());
    }

    @Override
    @InvalidatesCache
    public int insertOrUpdate(Integer tenantId, String actionType, Boolean visible) {
        return insertOrUpdate(tenantId, actionType, visible, 1);
    }
    
    @Override
    @InvalidatesCache
    public int insertOrUpdate(Integer tenantId, String actionType, Boolean visible, Integer weight) {
        Integer newId = existsType(tenantId, actionType);
        if (newId == null) {
            newId = getMaxActionTypeId(tenantId) + 1;
        }
        insertOrUpdate(tenantId, actionType, newId, visible, weight);
        return newId;
    }
    
    @LongCacheable
    @Override
    public HashMap<String, Integer> getMapping(Integer tenantId) {
        // validate
        if (tenantId == null) {
            throw new IllegalArgumentException("missing constraints: missing 'tenantId'");
        }

        ResultSetExtractor<HashMap<String, Integer>> rse = new ResultSetExtractor<HashMap<String, Integer>>() {
            public HashMap<String, Integer> extractData(ResultSet rs) {
                HashMap<String, Integer> mapping = new HashMap<String, Integer>();
                try {
                    while (rs.next()) {
                        mapping.put(DaoUtils.getStringIfPresent(rs, DEFAULT_NAME_COLUMN_NAME),
                                DaoUtils.getInteger(rs, DEFAULT_ID_COLUMN_NAME));
                    }
                    return mapping;
                } catch (SQLException e) {
                    logger.error("error occured", e);
                    throw new RuntimeException(e);
                }
            }
        };

        StringBuilder sqlQuery = new StringBuilder("SELECT ");
        sqlQuery.append(DEFAULT_NAME_COLUMN_NAME);
        sqlQuery.append(", ");
        sqlQuery.append(DEFAULT_ID_COLUMN_NAME);
        sqlQuery.append(" FROM ");
        sqlQuery.append(DEFAULT_TABLE_NAME);
        sqlQuery.append(" WHERE ");
        sqlQuery.append(DEFAULT_TENANT_COLUMN_NAME);
        sqlQuery.append("=?");

        Object[] args = {tenantId};
        int[] argTypes = {Types.INTEGER};
        return getJdbcTemplate().query(sqlQuery.toString(), args, argTypes, rse);
    }

    @LongCacheable
    @Override
    public Set<String> getTypes(Integer tenantId) {
        // validate
        if (tenantId == null) {
            throw new IllegalArgumentException("missing constraints: missing 'tenantId'");
        }
        ResultSetExtractor<Set<String>> rse = new ResultSetExtractor<Set<String>>() {
            public Set<String> extractData(ResultSet rs) {
                Set<String> types = new TreeSet<String>();
                try {
                    while (rs.next()) {
                        types.add(DaoUtils.getStringIfPresent(rs, DEFAULT_NAME_COLUMN_NAME));
                    }
                    return types;
                } catch (SQLException e) {
                    logger.error("error occured", e);
                    throw new RuntimeException(e);
                }
            }
        };

        StringBuilder sqlQuery = new StringBuilder("SELECT ");
        sqlQuery.append(DEFAULT_NAME_COLUMN_NAME);
        sqlQuery.append(" FROM ");
        sqlQuery.append(DEFAULT_TABLE_NAME);
        sqlQuery.append(" WHERE ");
        sqlQuery.append(DEFAULT_TENANT_COLUMN_NAME);
        sqlQuery.append("=?");
        sqlQuery.append(" ORDER BY ");
        sqlQuery.append(DEFAULT_NAME_COLUMN_NAME);

        Object[] args = {tenantId};
        int[] argTypes = {Types.INTEGER};
        return getJdbcTemplate().query(sqlQuery.toString(), args, argTypes, rse);
    }

    
    @LongCacheable
    @Override
    public Set<ActionTypeVO> getTypeVOs(Integer tenantId) {
        // validate
        if (tenantId == null) {
            throw new IllegalArgumentException("missing constraints: missing 'tenantId'");
        }
        ResultSetExtractor<Set<ActionTypeVO>> rse = new ResultSetExtractor<Set<ActionTypeVO>>() {
            public Set<ActionTypeVO> extractData(ResultSet rs) {
                Set<ActionTypeVO> types = new TreeSet<ActionTypeVO>();
                try {
                    while (rs.next()) {
                        types.add(new ActionTypeVO(
                                DaoUtils.getIntegerIfPresent(rs, DEFAULT_ID_COLUMN_NAME),
                                DaoUtils.getStringIfPresent(rs, DEFAULT_NAME_COLUMN_NAME),
                                DaoUtils.getIntegerIfPresent(rs, DEFAULT_TENANT_COLUMN_NAME),
                                DaoUtils.getBooleanIfPresent(rs, DEFAULT_HAS_VALUE_COLUMN_NAME),
                                DaoUtils.getIntegerIfPresent(rs, DEFAULT_WEIGHT_COLUMN_NAME)
                        ));
                    }
                    return types;
                } catch (SQLException e) {
                    logger.error("error occured", e);
                    throw new RuntimeException(e);
                }
            }
        };

        StringBuilder sqlQuery = new StringBuilder("SELECT * FROM ");
        sqlQuery.append(DEFAULT_TABLE_NAME);
        sqlQuery.append(" WHERE ");
        sqlQuery.append(DEFAULT_TENANT_COLUMN_NAME);
        sqlQuery.append("=?");
        sqlQuery.append(" ORDER BY ");
        sqlQuery.append(DEFAULT_NAME_COLUMN_NAME);

        Object[] args = {tenantId};
        int[] argTypes = {Types.INTEGER};
        return getJdbcTemplate().query(sqlQuery.toString(), args, argTypes, rse);
    }
    
    @Override
    @LongCacheable
    public Boolean hasValue(Integer tenantId, String actionType) {
        // validate
        if (tenantId == null) {
            throw new IllegalArgumentException("missing constraints: missing 'tenantId'");
        }
        if (actionType == null) {
            return null;
        }
        
        StringBuilder sqlQuery = new StringBuilder("SELECT ");
        sqlQuery.append(DEFAULT_HAS_VALUE_COLUMN_NAME).append(" FROM ").append(DEFAULT_TABLE_NAME);
        sqlQuery.append(" WHERE ").append(DEFAULT_TENANT_COLUMN_NAME).append("=? AND ");
        sqlQuery.append(DEFAULT_NAME_COLUMN_NAME).append("=?");

        Object[] args = {tenantId, actionType};
        int[] argt = {Types.INTEGER, Types.VARCHAR};

        return getJdbcTemplate().queryForObject(sqlQuery.toString(), args, argt, Boolean.class);
    }
    
    @Override
    @LongCacheable
    public Integer getWeight(Integer tenantId, String actionType) {
        // validate
        if (tenantId == null) {
            throw new IllegalArgumentException("missing constraints: missing 'tenantId'");
        }
        if (actionType == null) {
            return null;
        }
        
        StringBuilder sqlQuery = new StringBuilder("SELECT ");
        sqlQuery.append(DEFAULT_WEIGHT_COLUMN_NAME).append(" FROM ").append(DEFAULT_TABLE_NAME);
        sqlQuery.append(" WHERE ").append(DEFAULT_TENANT_COLUMN_NAME).append("=? AND ");
        sqlQuery.append(DEFAULT_NAME_COLUMN_NAME).append("=?");

        Object[] args = {tenantId, actionType};
        int[] argt = {Types.INTEGER, Types.VARCHAR};

        return getJdbcTemplate().queryForObject(sqlQuery.toString(), args, argt, Integer.class);
    }

    
    
    ///////////////////////////////////////////////////////////////////
    // private methods
    private Integer existsType(Integer tenantId, String actionType) {
        Integer id;
        try {
            id = getIdOfType(tenantId, actionType);
        } catch (IllegalArgumentException e) {
            logger.warn("An error occurred!", e);
            return null;
        }
        return id;
    }
    
    private int getMaxActionTypeId(Integer tenantId) {
        // validate
        if (tenantId == null) {
            throw new IllegalArgumentException("missing constraints: missing 'tenantId'");
        }

        ResultSetExtractor<Integer> rse = new ResultSetExtractor<Integer>() {
            public Integer extractData(ResultSet rs) {
                try {
                    if (rs.next()) {
                        return DaoUtils.getInteger(rs, DEFAULT_ID_COLUMN_NAME);
                    }
                } catch (SQLException e) {
                    logger.error("error occured", e);
                    throw new RuntimeException(e);
                }
                throw new IllegalArgumentException("unknown tenant");
            }
        };

        StringBuilder sqlQuery = new StringBuilder("SELECT MAX(");
        sqlQuery.append(DEFAULT_ID_COLUMN_NAME);
        sqlQuery.append(") AS ");
        sqlQuery.append(DEFAULT_ID_COLUMN_NAME);
        sqlQuery.append(" FROM ");
        sqlQuery.append(DEFAULT_TABLE_NAME);
        sqlQuery.append(" WHERE ");
        sqlQuery.append(DEFAULT_TENANT_COLUMN_NAME);
        sqlQuery.append("=?");

        Object[] args = {tenantId};
        int[] argTypes = {Types.INTEGER};

        Integer result = getJdbcTemplate().query(sqlQuery.toString(), args, argTypes, rse);

        return result == null ? 1 : result;
    }
}
