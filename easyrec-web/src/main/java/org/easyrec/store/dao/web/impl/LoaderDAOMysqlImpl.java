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
package org.easyrec.store.dao.web.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.easyrec.model.core.web.Operator;
import org.easyrec.store.dao.web.LoaderDAO;
import org.easyrec.store.dao.web.OperatorDAO;
import org.easyrec.utils.spring.store.service.sqlscript.impl.SqlScriptServiceImpl;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.jdbc.support.DatabaseMetaDataCallback;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.jdbc.support.MetaDataAccessException;
import org.springframework.web.context.ConfigurableWebApplicationContext;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.*;

/**
 * @author szavrel
 */
public class LoaderDAOMysqlImpl extends JdbcDaoSupport
        implements LoaderDAO, ApplicationListener, ApplicationContextAware {

    private final Log logger = LogFactory.getLog(LoaderDAOMysqlImpl.class);

    private SqlScriptServiceImpl sqlScriptService;
    private Properties properties;
    private Resource overrideFolder;
    private Resource dbCreationFile;
    private Resource dbMigrateFolder;
    private List<String> migrateFiles;
    private ApplicationContext applicationContext;
    private HashMap<String, String> configLocations;
    private String currentVersion;

    private static final String SQL_ADD_OPERATOR;

    static {
        SQL_ADD_OPERATOR = new StringBuilder().append(" INSERT INTO ").append(OperatorDAO.DEFAULT_TABLE_NAME)
                .append("    (").append(OperatorDAO.DEFAULT_TABLE_KEY).append(", PASSWORD, FIRSTNAME, LASTNAME, ")
                .append("     EMAIL, PHONE, COMPANY, ADDRESS, APIKEY, IP, CREATIONDATE, ACTIVE, ACCESSLEVEL) VALUES ")
                .append("    (?,PASSWORD(?),?,?,?,?,?,?,?,?,?,?,?) ").toString();
    }

    public LoaderDAOMysqlImpl(DataSource dataSource, SqlScriptServiceImpl sqlScriptService, Resource dbCreationFile,
                              Resource dbMigrateFolder) {
        setDataSource(dataSource);
        HikariDataSource bds =  (HikariDataSource) dataSource;
        //bds.setInitializationFailFast(false);
        logger.info("Installer trying to connect with user: " + bds.getUsername() + "/" + bds.getPassword());
        this.sqlScriptService = sqlScriptService;
        this.dbCreationFile = dbCreationFile;
        this.dbMigrateFolder = dbMigrateFolder;
    }

    @Override
    public void testConnection(String url, String username, String password) throws Exception {

        HikariConfig config = new HikariConfig();
        config.setDataSourceClassName("com.mysql.jdbc.jdbc2.optional.MysqlDataSource");
        config.setJdbcUrl(url);
        config.setUsername(username);
        config.setPassword(password);
        config.setPoolName("easyrecPool");
        config.addDataSourceProperty("url", url);
        HikariDataSource ds = new HikariDataSource(config);

        setDataSource(ds);
        sqlScriptService.setDataSource(ds);
        
        boolean tablesOk = false;

        DatabaseMetaDataCallback callback = new DatabaseMetaDataCallback() {
            public Object processMetaData(DatabaseMetaData dbmd) throws SQLException, MetaDataAccessException {
                ResultSet rs = dbmd.getTables(null, null, "operator", null);
                return rs.next();
            }
        };

        tablesOk = (Boolean) JdbcUtils.extractDatabaseMetaData(ds, callback);
    }

    @Override
    public void createDB() throws Exception {

        HikariDataSource bds = (HikariDataSource) getDataSource();
        boolean tablesOk = false;

        DatabaseMetaDataCallback callback = new DatabaseMetaDataCallback() {
            public Object processMetaData(DatabaseMetaData dbmd) throws SQLException, MetaDataAccessException {
                ResultSet rs = dbmd.getTables(null, null, "operator", null);
                return rs.next();
            }
        };

        tablesOk = (Boolean) JdbcUtils.extractDatabaseMetaData(bds, callback);
        sqlScriptService.executeSqlScript(dbCreationFile.getInputStream());
    }

    @Override
    public void migrateDB() throws Exception {

        HikariDataSource bds = (HikariDataSource) getDataSource();
        boolean tablesOk = false;

        DatabaseMetaDataCallback callback = new DatabaseMetaDataCallback() {
            public Object processMetaData(DatabaseMetaData dbmd) throws SQLException, MetaDataAccessException {
                ResultSet rs = dbmd.getTables(null, null, "operator", null);
                return rs.next();
            }
        };

        tablesOk = (Boolean) JdbcUtils.extractDatabaseMetaData(bds, callback);
        Float installedVersion = checkVersion();

        for (String migrateFile : migrateFiles) {
            logger.info("migrate File: " + migrateFile);
            Float scriptVersion = Float.parseFloat(migrateFile.substring(migrateFile.lastIndexOf("_") + 1));
            logger.info("scriptVersion: " + scriptVersion);
            if (installedVersion < scriptVersion) {
                File f = new File(dbMigrateFolder.getFile(), migrateFile + ".sql");
                if (f.exists()) {
                    logger.info("Executing migrate script: " + f.getName());
                    sqlScriptService.executeSqlScript(new FileSystemResource(f).getInputStream());
                }
                if (scriptVersion == 0.96) {
                    update_0_96f();
                }
                if (scriptVersion == 0.98) {
                    update_0_98();
                }
                if (scriptVersion == 1.00) {
                    update_1_00();
                }
            }
        }

//        if (installedVersion < 0.96f) {
//            update_0_96f();
//            // logs are not converted from ruleminerlog -> plugin_log
//        }
//
//        if (installedVersion < 0.98) {
//            update_0_98();
//        }

        //updateVersion(); // done in migrate script!
    }

    private void update_1_00() {
        
        logger.info("executing 'update_1_00()'...");
        
        
        logger.info("checking action table colums...");
        final HashMap<String, Integer> actionColumnList = new HashMap<>();
        getJdbcTemplate().query("SHOW COLUMNS FROM action", new RowCallbackHandler() {
            public void processRow(ResultSet resultSet) throws SQLException {
                String column = resultSet.getString("Field");
                actionColumnList.put(column, 1);
            }
        });
        
        if (actionColumnList.containsKey("searchSucceeded")) {
            getJdbcTemplate().execute("ALTER TABLE action DROP COLUMN searchSucceeded");
        }
                
        if (actionColumnList.containsKey("numberOfFoundItems")) {
            getJdbcTemplate().execute("ALTER TABLE action DROP COLUMN numberOfFoundItems");
        }
        if (actionColumnList.containsKey("description")) {
            getJdbcTemplate().execute("ALTER TABLE action CHANGE COLUMN description actionInfo VARCHAR(500) CHARACTER SET utf8");
        }
        
        logger.info("checking actionType table colums...");
        final HashMap<String, Integer> aTcolumnList = new HashMap<>();
        getJdbcTemplate().query("SHOW COLUMNS FROM actiontype", new RowCallbackHandler() {
            public void processRow(ResultSet resultSet) throws SQLException {
                String column = resultSet.getString("Field");
                aTcolumnList.put(column, 1);
            }
        });
        
        if (!aTcolumnList.containsKey("weight")) {
            getJdbcTemplate().execute("ALTER TABLE actiontype ADD COLUMN weight INT(11) NOT NULL DEFAULT 1");
        }
        
        logger.info("checking backtracking table colums...");
        final HashMap<String, Integer> bTcolumnList = new HashMap<>();
        getJdbcTemplate().query("SHOW COLUMNS FROM backtracking", new RowCallbackHandler() {
            public void processRow(ResultSet resultSet) throws SQLException {
                String column = resultSet.getString("Field");
                bTcolumnList.put(column, 1);
            }
        });
        
        if (!bTcolumnList.containsKey("itemFromTypeId")) {
            getJdbcTemplate().execute("ALTER TABLE backtracking ADD COLUMN itemFromTypeId int(11) NOT NULL AFTER itemFromId");
        }
        
        if (!bTcolumnList.containsKey("itemFromTypeId")) {
            getJdbcTemplate().execute("ALTER TABLE backtracking ADD COLUMN itemToTypeId int(11) NOT NULL AFTER itemToId");
        }
        
        if (bTcolumnList.containsKey("assocType")) {
            getJdbcTemplate().execute("ALTER TABLE backtracking CHANGE COLUMN assocType recType INT(11) UNSIGNED NOT NULL");
        }
        
        if (bTcolumnList.containsKey("timestamp")) {
            getJdbcTemplate().execute("ALTER TABLE backtracking CHANGE COLUMN timestamp actionTime TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP");
        }

        getJdbcTemplate().execute("ALTER TABLE backtracking DROP INDEX assoc ");
        getJdbcTemplate().execute("ALTER TABLE backtracking ADD INDEX assoc(tenantId, itemFromId, itemFromTypeId, recType, itemToId, itemToTypeId)");
    }
    
    /*
     * Items now have a profile field so the profile table moved to the item table
     * this function will move the items from the profile table to the items table and
     * remove the profile table from the database.
     */
    private void update_0_98() {

        logger.info("executing 'update_0_98()'...");

        final String profileTableName = "profile";
        final String itemTableName = "item";
        final String itemTypeTableName = "itemtype";
        final String assocTypeTableName = "assoctype";
        final String sourceTypeTableName = "sourcetype";

        final String itemColumnTenantID = "tenantId";
        final String itemColumnItemID = "itemid";
        final String itemColumnItemType = "itemtype";
        final String itemColumnProfileData = "profileData";
        final String itemColumnChangeDate = "changedate";
        final String itemColumnActive = "active";

        final String profileColumnTenantID = "tenantId";
        final String profileColumnItemID = "itemId";
        final String profileColumnItemTypeID = "itemTypeId";
        final String profileColumnProfileData = "profileData";
        final String profileColumnChangeData = "changeDate";
        final String profileColumnActive = "active";

        final String itemTypeColumnTenantID = "tenantId";
        final String itemTypeColumnName = "name";
        final String itemTypeColumnID = "id";

        final String assocTypeTenantID = "tenantId";
        final String assocTypeName = "name";
        final String assocTypeID = "id";
        final String assocTypeVisible = "visible";

        final String sourceTypeColumnId = "id";
        final String sourceTypeColumnTenantId = "tenantId";
        final String sourceTypeColumnName = "name";



        /* Create a hashmap with the itemTypeNames as values and the
         * tenantId and itemTypeId as key.
         */
        logger.info("creating a hashmap with itemTypeNames...");
        final HashMap<String, String> itemTypeList = new HashMap<String, String>();
        getJdbcTemplate().query("SELECT * FROM " + itemTypeTableName, new RowCallbackHandler() {
            public void processRow(ResultSet resultSet) throws SQLException {
                int tenantId = resultSet.getInt(itemTypeColumnTenantID);
                int itemTypeId = resultSet.getInt(itemTypeColumnID);
                String itemTypeName = resultSet.getString(itemTypeColumnName);
                itemTypeList.put(String.valueOf(tenantId) + "," + String.valueOf(itemTypeId), itemTypeName);
            }
        });

        /*
         * For each entry in the profile table look for an entry with the same key in the
         * item table. If the item table has no entry, create it else update the entry with
         * the values from the profile table.
         */
        logger.info("executing 'update_0_98()'...");
        getJdbcTemplate().query("SELECT * FROM " + profileTableName, new RowCallbackHandler() {
            public void processRow(ResultSet resultSet) throws SQLException {

                final int tenantId = resultSet.getInt(profileColumnTenantID);
                final int itemId = resultSet.getInt(profileColumnItemID);
                int itemTypeId = resultSet.getInt(profileColumnItemTypeID);
                String tempProfileData = resultSet.getString(profileColumnProfileData);
                if (tempProfileData == null) {
                    tempProfileData = "null";
                } else {
                    tempProfileData = "'" + tempProfileData + "'";
                }
                final String profileData = tempProfileData;
                final String itemTypeName = "'" + itemTypeList.get(
                        String.valueOf(tenantId) + "," + String.valueOf(itemTypeId)) + "'";

                /*
                 * A "0000-00-00 00:00:00" value in the table is returned as null and
                 * must therefore a null value must be changed back to "0000-00-00 00:00:00"
                 * before updating the table.
                 */

                String tempChangeDate = resultSet.getString(profileColumnChangeData);
                if (tempChangeDate == null) {
                    tempChangeDate = "0000-00-00 00:00:00";
                }
                final String changeDate = "'" + tempChangeDate + "'";
                final boolean active = resultSet.getBoolean(profileColumnActive);

                final String sqlItemUpdate = " WHERE " +
                        itemColumnItemID + " = " + itemId + " AND " +
                        itemColumnTenantID + " = " + tenantId + " AND " +
                        itemColumnItemType + " like " + itemTypeName;

                final String sqlItemToUpdate = "FROM " + itemTableName + sqlItemUpdate;
                getJdbcTemplate().query("SELECT COUNT(*) " + sqlItemToUpdate, new RowCallbackHandler() {
                    public void processRow(ResultSet resultSet) throws SQLException {
                        if (resultSet.getInt(1) == 0) { // The profile is not in the item table -> create one.
                            String sqlInsertStatement = "INSERT INTO " + itemTableName + " SET " +
                                    itemColumnItemID + " = " + String.valueOf(itemId) + ", " +
                                    itemColumnTenantID + " = " + String.valueOf(tenantId) + ", " +
                                    itemColumnItemType + " = " + itemTypeName + ", " +
                                    itemColumnProfileData + " = " + profileData + ", " +
                                    itemColumnChangeDate + " = " + changeDate + ", " +
                                    itemColumnActive + " = " + String.valueOf(active);
                            logger.info("executing: " + sqlInsertStatement);
                            getJdbcTemplate().execute(sqlInsertStatement);
                        } else { // The profile has already an entry in the item table -> just updating.
                            getJdbcTemplate().query("SELECT * " + sqlItemToUpdate, new RowCallbackHandler() {
                                public void processRow(ResultSet resultSet) throws SQLException {

                                    logger.info("updating item: " + resultSet.getString(itemColumnItemID));
                                    getJdbcTemplate().execute("UPDATE " + itemTableName + " SET " +
                                            itemColumnProfileData + " = " + profileData + ", " +
                                            itemColumnChangeDate + " = " + changeDate + " " +
                                            sqlItemUpdate);
                                }
                            }
                            );
                        }
                    }
                }
                );
            }
        });

        getJdbcTemplate().execute("DROP TABLE " + profileTableName);

        /**
         * create association type PROFILE_SIMILARITY for the profileDuke plugin
         */

        // get list of tenantIDs in the assoctype table
        logger.info("inserting PROFILE_SIMILARITY assoc Type");
        String sql = "SELECT DISTINCT " + assocTypeTenantID + " FROM " + assocTypeTableName;
        logger.info("fireing: " + sql);
        final List<Integer> tenantIDList = new ArrayList<Integer>();
        getJdbcTemplate().query(sql,
                new RowCallbackHandler() {
                    public void processRow(ResultSet resultSet) throws SQLException {
                        int tenantIdx = resultSet.getInt(assocTypeTenantID);
                        logger.info("found tenant " + tenantIdx);
                        logger.info("found " + resultSet.getFetchSize() + " results");
                        int tenantId = resultSet.getInt(assocTypeTenantID);
                        logger.info("found tenant " + tenantId);
                        tenantIDList.add(tenantId);
                    }
                });

        // get the max id of each tenant and add a PROFILE_SIMILARITY association type and
        // a sourceType for the plugins with the new versions.
        Integer maxAssocTypeId;
        Integer maxSourceTypeId;
        for (final Integer tenantId : tenantIDList) {

            // association type
            maxAssocTypeId = getJdbcTemplate().queryForInt("SELECT MAX(id) FROM " + assocTypeTableName +
                    " WHERE " + assocTypeTableName + "." + assocTypeTenantID + " = " + tenantId);

            sql = "INSERT INTO " + assocTypeTableName + " (" +
                    assocTypeTenantID + ", " + assocTypeName + ", " + assocTypeID + ", " + assocTypeVisible +
                    ") VALUES (" + tenantId + ", \"PROFILE_SIMILARITY\", " + (maxAssocTypeId + 1) + ", 1)";

            getJdbcTemplate().execute(sql);

            // sourceType
            maxSourceTypeId = getJdbcTemplate().queryForInt("SELECT MAX(" + sourceTypeColumnId + ") FROM " +
                    sourceTypeTableName +
                    " WHERE " + sourceTypeTableName + "." + sourceTypeColumnTenantId + " = " + tenantId);

            sql = "INSERT INTO " + sourceTypeTableName + " (" +
                    sourceTypeColumnTenantId + ", " + sourceTypeColumnName + ", " + sourceTypeColumnId +
                    ") VALUES (" + tenantId + ", \"http://www.easyrec.org/plugins/slopeone/0.98\", " + (maxSourceTypeId + 1) + ")";
            getJdbcTemplate().execute(sql);
            sql = "INSERT INTO " + sourceTypeTableName + " (" +
                    sourceTypeColumnTenantId + ", " + sourceTypeColumnName + ", " + sourceTypeColumnId +
                    ") VALUES (" + tenantId + ", \"http://www.easyrec.org/plugins/ARM/0.98\", " + (maxSourceTypeId + 2) + ")";
            getJdbcTemplate().execute(sql);
        }

        // update the pluginVersion on the plugin_configuration table
        sql = "UPDATE plugin_configuration SET pluginVersion = '0.98'\n" +
                "WHERE pluginId = 'http://www.easyrec.org/plugins/ARM' OR pluginId = 'http://www.easyrec.org/plugins/slopeone'";
        getJdbcTemplate().execute(sql);
    }


    /**
     * easyrec pre 0.96 stored settings for plugins in the tenantConfig column of the tenantsTable from 0.96
     * onwards they are stored as XML serialized GeneratorConfigurations in the plugin_configuration  table
     * this snippet converts the existing ARM configurations (and only ARM configurations) to the new XML
     * version.
     *
     * @throws IOException
     */
    private void update_0_96f() throws IOException {
        //

        final String RENAME_SOURCETYPE_QUERY =
                "UPDATE sourcetype SET name=? WHERE name=?";
        getJdbcTemplate().update(RENAME_SOURCETYPE_QUERY, "http://www.easyrec.org/plugins/ARM/0.96", "ARM");
        getJdbcTemplate().update(RENAME_SOURCETYPE_QUERY, "http://www.easyrec.org/plugins/slopeone/0.96", "http://www.easyrec.org/plugins/slopeone/0.95");

        ResultSetExtractor<Map<Integer, String>> rse = new ResultSetExtractor<Map<Integer, String>>() {
            public Map<Integer, String> extractData(ResultSet rs) throws SQLException, DataAccessException {
                Map<Integer, String> result = Maps.newHashMap();

                while (rs.next()) {
                    int id = rs.getInt("id");
                    String config = rs.getString("tenantConfig");
                    result.put(id, config);
                }

                return result;
            }
        };

        Map<Integer, String> tenantConfigs = getJdbcTemplate().query("SELECT id, tenantConfig FROM tenant", rse);

        for (Map.Entry<Integer, String> tenantConfig : tenantConfigs.entrySet()) {
            String tenantConfigString = tenantConfig.getValue();

            if (tenantConfigString == null)
                tenantConfigString = "";

            int tenantId = tenantConfig.getKey();
            StringReader reader = new StringReader(tenantConfigString);

            Properties propertiesConfig = new Properties();
            propertiesConfig.load(reader);

            String configViewedTogether =
                    generateXmlConfigurationFromProperties(propertiesConfig, "VIEWED_TOGETHER", "VIEW");
            String configGoodRatedTogether =
                    generateXmlConfigurationFromProperties(propertiesConfig, "GOOD_RATED_TOGETHER", "RATE");
            String configBoughtTogether =
                    generateXmlConfigurationFromProperties(propertiesConfig, "BOUGHT_TOGETHER", "BUY");

            // write back config with arm settings removed
//                getJdbcTemplate().update("UPDATE tenant SET tenantConfig = ? WHERE id = ?",
//                        propertiesConfig.toString(), tenantConfig.getKey());

            final String CONFIG_QUERY =
                    "INSERT INTO plugin_configuration(tenantId, assocTypeId, pluginId, pluginVersion, name, configuration, active) VALUES " +
                            "(?, ?, 'http://www.easyrec.org/plugins/ARM', ?, 'Default Configuration', ?, b'1')";

            // generate configuration entries
            getJdbcTemplate().update(CONFIG_QUERY, tenantId, 1, currentVersion, configViewedTogether);
            getJdbcTemplate().update(CONFIG_QUERY, tenantId, 2, currentVersion, configGoodRatedTogether);
            getJdbcTemplate().update(CONFIG_QUERY, tenantId, 3, currentVersion, configBoughtTogether);

            final String slopeOneXmlConfig =
                    "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" +
                            "<slopeOneConfiguration>" +
                            "<configurationName>Default Configuration</configurationName>" +
                            "<associationType>IS_RELATED</associationType>" +
                            "<maxRecsPerItem>10</maxRecsPerItem>" +
                            "<minRatedCount xsi:nil=\"true\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"/>" +
                            "<nonPersonalizedSourceInfo>slopeone-nonpersonalized</nonPersonalizedSourceInfo>" +
                            "<actionType>RATE</actionType><itemTypes/>" +
                            "<viewType>COMMUNITY</viewType>" +
                            "</slopeOneConfiguration>";

            final String GET_ISRELATED_ASSOCTYPE_QUERY =
                    "SELECT id FROM assoctype WHERE name = 'IS_RELATED' AND tenantId = ?";
            int isRelatedAssocType = 0;
            // for tenants without a type 'IS_RELATED' this throws an exception - so catch!!
            try {
                isRelatedAssocType = getJdbcTemplate().queryForInt(GET_ISRELATED_ASSOCTYPE_QUERY, tenantId);
            } catch (EmptyResultDataAccessException erdar) {
                isRelatedAssocType = 0;
            }

            if (isRelatedAssocType == 0) {
                final String GET_MAX_ASSOCTYPE_QUERY =
                        "SELECT MAX(id) FROM assoctype WHERE tenantId = ?";
                isRelatedAssocType = getJdbcTemplate().queryForInt(GET_MAX_ASSOCTYPE_QUERY, tenantId) + 1;

                final String INSERT_ASSOCTYPE_QUERY =
                        "INSERT INTO assoctype(tenantId, name, id, visible) VALUES (?, 'IS_RELATED', ?, b'1')";
                getJdbcTemplate().update(INSERT_ASSOCTYPE_QUERY, tenantId, isRelatedAssocType);
            }
            // add sourcetype for slopeone where missing
            final String GET_SLOPEONE_SOURCETYPE_QUERY =
                    "SELECT id FROM sourcetype WHERE name = 'http://www.easyrec.org/plugins/slopeone/0.96' AND tenantId = ?";
            int slopeOneSourceType = 0;
            // for tenants without a type 'http://www.easyrec.org/plugins/slopeone/0.96' this throws an exception - so catch!!
            try {
                slopeOneSourceType = getJdbcTemplate().queryForInt(GET_SLOPEONE_SOURCETYPE_QUERY, tenantId);
            } catch (EmptyResultDataAccessException erdar) {
                slopeOneSourceType = 0;
            }

            if (slopeOneSourceType == 0) { // this means sourcetype not found, so update
                final String GET_MAX_SOURCETYPE_QUERY =
                        "SELECT MAX(id) FROM sourcetype WHERE tenantId = ?";
                slopeOneSourceType = getJdbcTemplate().queryForInt(GET_MAX_SOURCETYPE_QUERY, tenantId) + 1;

                final String INSERT_SOURCETYPE_QUERY =
                        "INSERT INTO sourcetype(tenantId, name, id) VALUES (?, 'http://www.easyrec.org/plugins/slopeone/0.96', ?)";
                getJdbcTemplate().update(INSERT_SOURCETYPE_QUERY, tenantId, slopeOneSourceType);
            }

            final String SLOPEONE_CONFIG_QUERY =
                    "INSERT INTO plugin_configuration(tenantId, assocTypeId, pluginId, pluginVersion, name, configuration, active) VALUES " +
                            "(?, ?, 'http://www.easyrec.org/plugins/slopeone', ?, 'Default Configuration', ?, b'1')";
            getJdbcTemplate().update(SLOPEONE_CONFIG_QUERY, tenantId, isRelatedAssocType, currentVersion,
                    slopeOneXmlConfig);

        }
    }

    private String generateXmlConfigurationFromProperties(final Properties propertiesConfig, final String assocType,
                                                          final String actionType) {
        String supportMinAbs = propertiesConfig.getProperty(assocType + ".ARM.supportMinAbs", "2");
        String supportPercnt = propertiesConfig.getProperty(assocType + ".ARM.supportPrcnt", "0.0");
        String confidencePercnt = propertiesConfig.getProperty(assocType + ".ARM.confidencePrcnt", "0.0");

        // no harm to keep the old values - maybe useful as fallback
//        propertiesConfig.remove(assocType + ".ARM.supportMinAbs");
//        propertiesConfig.remove(assocType + ".ARM.supportPrcnt");
//        propertiesConfig.remove(assocType + ".ARM.confidencePrcnt");

        final String xmlConfigurationTemplate =
                "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" +
                        "<armConfiguration>" +
                        "<configurationName>Default Configuration</configurationName>" +
                        "<associationType>%s</associationType>" +
                        "<actionType>%s</actionType>" +
                        "<confidencePrcnt>%s</confidencePrcnt>" +
                        "<doDeltaUpdate>false</doDeltaUpdate>" +
                        "<excludeSingleItemBaskests>false</excludeSingleItemBaskests>" +
                        "<itemTypes>ITEM</itemTypes>" +
                        "<maxRulesPerItem>50</maxRulesPerItem>" +
                        "<maxSizeL1>5000</maxSizeL1>" +
                        "<metricType>CONFIDENCE</metricType>" +
                        "<ratingNeutral>5.5</ratingNeutral>" +
                        "<supportMinAbs>%s</supportMinAbs>" +
                        "<supportPrcnt>%s</supportPrcnt>" +
                        "</armConfiguration>";

        return String.format(xmlConfigurationTemplate, assocType, actionType, confidencePercnt, supportMinAbs,
                supportPercnt);
    }

    /**
     * This function returns the current version of easyrec,
     * depending on the presence of a version table. If
     * no version table is present return the inital version
     *
     *
     */
    @Override
    public Float checkVersion() throws Exception {

        HikariDataSource bds = (HikariDataSource) getDataSource();
        float tableCount;

        DatabaseMetaDataCallback callback = new DatabaseMetaDataCallback() {
            public Object processMetaData(DatabaseMetaData dbmd) throws SQLException, MetaDataAccessException {
                ResultSet rs = dbmd.getTables(null, null, null, null);
                float f = 0;
                while (rs.next()) {
                    f++;
                }
                return f;
            }
        };

        tableCount = (Float) JdbcUtils.extractDatabaseMetaData(bds, callback);

        if (tableCount != 0) {
            try {
                return getJdbcTemplate().queryForObject("SELECT MAX(VERSION) FROM easyrec ", Float.class);
            } catch (Exception e) {
                // else return initial version 0.9
                return INITIAL_VERSION;
            }
        } else {
            return tableCount;
        }
    }

    public void updateVersion() {
        try {
            getJdbcTemplate().update("INSERT INTO easyrec(version) VALUES (?)", currentVersion);
        } catch (Exception e) {
            logger.warn("unable to update version", e);
        }
    }

    @Override
    public Operator addOperator(String id, String password, String firstName, String lastName, String email,
                                String phone, String company, String address, String apiKey, String ip) {

        Object[] args = {id, password, firstName, lastName, email, phone, company, address, apiKey, ip, new Date(),
                true, 1};

        int[] argTypes = {Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.VARCHAR,
                Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.TIMESTAMP, Types.BOOLEAN,
                Types.INTEGER};

        try {
            getJdbcTemplate().update(SQL_ADD_OPERATOR, args, argTypes);

            return new Operator(id, password, firstName, lastName, email, phone, company, address, apiKey, ip, true,
                    // active
                    new Date().toString(), // creation date
                    1,                     // acceslevel
                    0,                     // login count
                    null                   // last login date
            );
        } catch (Exception e) {
            logger.debug(e);
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        if (event instanceof ContextRefreshedEvent) {
            // after context start, check if properties file in folder /override exists
            try {
                File f = new File(overrideFolder.getFile(), "easyrec.database.properties");
                // if file exists, config was already written, so boot application as normal
                if (f.exists()) {
                    reloadContext();
                }
            } catch (IOException ex) {
                logger.error("Error looking for config file! Running in installer mode as fallback!");
            }
        }
    }

    @Override
    public void reloadContext() {
        reloadBackend();
        reloadFrontend();
    }

    @Override
    public void reloadBackend() {
        List<String> configLocs = Lists.newArrayList("classpath:spring/web/commonContext.xml");

        if ("on".equals(properties.getProperty("easyrec.rest"))) {
            configLocs.add(configLocations.get("easyrec.rest"));
        }

        // if no config found use default
        if (configLocs.size() == 1) {
            configLocs.add(configLocations.get("easyrec.rest"));
        }

        if ("on".equals(properties.getProperty("easyrec.dev"))) {
            configLocs.add(configLocations.get("easyrec.dev"));
        }

        ApplicationContext webctx = applicationContext;

        ApplicationContext parent = webctx.getParent();

        if (parent instanceof ConfigurableWebApplicationContext) {

            ((ConfigurableWebApplicationContext) parent).setConfigLocations(
                    configLocs.toArray(new String[configLocs.size()]));
            ((ConfigurableWebApplicationContext) parent).refresh();

        }
        
        setDataSource(parent.getBean("easyrecDataSource", com.zaxxer.hikari.HikariDataSource.class));
    }

    @Override
    public void reloadFrontend() {

        ApplicationContext webctx = applicationContext;

        if (webctx instanceof ConfigurableWebApplicationContext) {

            ((ConfigurableWebApplicationContext) webctx)
                    .setConfigLocation("classpath:spring/web/easyrecContext.xml");
            ((ConfigurableWebApplicationContext) webctx).refresh();
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    public void setDriver(String driver) {
        HikariDataSource bds = (HikariDataSource) getDataSource();
        bds.setDriverClassName(driver);
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    public Properties getProperties() {
        return properties;
    }

    public void setOverrideFolder(Resource resource) {
        this.overrideFolder = resource;
    }

    public void setConfigLocations(HashMap<String, String> configLocations) {
        this.configLocations = configLocations;
    }

    public List<String> getMigrateFiles() {
        return migrateFiles;
    }

    public void setMigrateFiles(List<String> migrateFiles) {
        this.migrateFiles = migrateFiles;
    }

    public void setCurrentVersion(String currentVersion) {
        this.currentVersion = currentVersion;
    }
}
