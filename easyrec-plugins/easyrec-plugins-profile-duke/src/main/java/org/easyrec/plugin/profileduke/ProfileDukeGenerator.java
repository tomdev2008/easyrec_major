/*
 * Copyright 2013 Research Studios Austria Forschungsgesellschaft mBH
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

package org.easyrec.plugin.profileduke;

import no.priv.garshol.duke.Column;
import no.priv.garshol.duke.Configuration;
import no.priv.garshol.duke.Processor;
import no.priv.garshol.duke.Property;
import no.priv.garshol.duke.ConfigLoader;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.easyrec.model.core.ItemVO;
import org.easyrec.plugin.model.Version;
import org.easyrec.plugin.profileduke.duke.datasource.EasyrecXMLFormatDataSource;
import org.easyrec.plugin.profileduke.duke.matchers.EasyrecProfileMatcher;
import org.easyrec.plugin.profileduke.store.dao.ProfileSimilarityItemAssocDAO;
import org.easyrec.plugin.support.GeneratorPluginSupport;
import org.easyrec.service.core.ProfileService;
import org.easyrec.service.domain.TypeMappingService;
import org.easyrec.store.dao.core.ItemAssocDAO;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.net.URI;
import java.util.*;


// TODO: Description

/**
 * @author Soheil Khosravipour
 * @author Fabian Salcher
 */
public class ProfileDukeGenerator extends GeneratorPluginSupport<ProfileDukeConfiguration, ProfileDukeStats> {
    // ------------------------------ FIELDS ------------------------------

    // the display name is the name of the generator that will show up in the admin tool when the plugin has been loaded.
    public static final String DISPLAY_NAME = "Profile Similarity Calculator (Duke)";
    // version of the generator, should be ascending for each new release
    // you might increment the versioning components (major,minor,misc) like this:
    //   major - complete reworks of your generator, major new features
    //   minor - small feature improvements
    //   misc  - bugfix releases or anything else
    public static final Version VERSION = new Version("1.00");

    // The URI that uniquely identifies the plugin. While any valid URI is technically ok here, implementors
    // should choose their URIs wisely, ideally the URI should be 'cool'
    // (@see <a href="http://www.dfki.uni-kl.de/~sauermann/2006/11/cooluris/#cooluris">Cool URIs for the
    // Semantic Web</a>) If unsure, use an all-lowercase http URI pointing to a host/path that you control,
    // ending with '#[plugin-name]'.
    public static final URI ID = URI.create("http://www.easyrec.org/plugins/profileSimilarity");

    public static final Log logger = LogFactory.getLog(org.easyrec.plugin.profileduke.ProfileDukeGenerator.class);

    // the service will be auto-wired when the plugin is loaded, see {@link #setActionService(ActionService)}.
    private ProfileService profileService;

    private ProfileSimilarityItemAssocDAO profileSimilarityItemAssocDAO;

    private int numberOfAssociationsCreated = 0;
    private double lastThreshold = 0;

    private int tenantId;
    private int itemType;


    // --------------------------- CONSTRUCTORS ---------------------------

    public ProfileDukeGenerator() {
        // we need to call the constructor of GeneratorPluginSupport to provide the name, id and version
        //additionally, we have to pass the class objects of config and stats classes.
        super(DISPLAY_NAME, ID, VERSION, ProfileDukeConfiguration.class, ProfileDukeStats.class);
    }

    // --------------------- GETTER / SETTER METHODS ---------------------

    // this method will be called when the plugin is being loaded and Spring injects the service, you need to make sure
    // that everything after the "set" part of the method name is named exactly like the Spring-bean.
    // For all beans that can be injected look in the wiki.

    public void setProfileService(ProfileService profileService) {
        this.profileService = profileService;
    }

    public void setNumberOfAssociationsCreated(int numberOfAssociationsCreated) {
        this.numberOfAssociationsCreated = numberOfAssociationsCreated;
    }

    public int getItemType(){
        return itemType;
    }

    public int getTenantId() {
        return tenantId;
    }

    // ------------------------ INTERFACE METHODS ------------------------

    @Override
    public String getPluginDescription() {
        return "<p>Generator plugin that generates rules based on the item profiles.<p/>\n" +
                "<p>The record linkage engine Duke is used to calculate the similarities between the item profiles.</p>";
    }

    // -------------------------- MAIN METHODS --------------------------

    @Override
    protected void doCleanup() throws Exception {
        logger.info("The plugin is now being uninstalled.");
        // remove all tables/files/resources you created in {@link #doInitialize()}.
        // optional - you don't have to implement this method
    }

    @Override
    protected void doExecute(ExecutionControl executionControl, ProfileDukeStats stats) throws Exception {

        // when doExecute() is called, the generator has been initialized with the configuration we should use
        ProfileDukeConfiguration config = getConfiguration();

        TypeMappingService typeMappingService = (TypeMappingService) super.getTypeMappingService();
        int sourceType = typeMappingService.getIdOfSourceType(config.getTenantId(), this.getId().toString());
        itemType = typeMappingService.getIdOfItemType(config.getTenantId(), config.getItemType());
        tenantId = config.getTenantId();
        ItemAssocDAO itemAssocDAO = getItemAssocDAO();

        // the generator needs to check periodically if abort was requested and stop operation in a clean manner
        if (executionControl.isAbortRequested()) return;

        List<ItemVO<Integer, Integer>> itemList = profileService.getItemsByItemType(
                config.getTenantId(), config.getItemType(), 0);

        stats.setNumberOfItems(itemList.size());

        logger.info("BlockMode: " + config.getBlockCalculationMode());
        logger.info("BlockModeBlockSize: " + config.getBlockCalculationBlockSize());
        logger.info("ItemListSize: " + itemList.size());
        logger.info("Duke Configuration: \n" + config.getDukeConfiguration());

        if ("true".equals(config.getBlockCalculationMode())) {

            LinkedList<ItemVO<Integer, Integer>> itemPot = new LinkedList<ItemVO<Integer, Integer>>();
            for (ItemVO<Integer, Integer> item : itemList) {
                itemPot.add(item);
            }

            Random random = new Random();
            int blockSize = config.getBlockCalculationBlockSize();
            List<ItemVO<Integer, Integer>> itemTempList = new Vector<ItemVO<Integer, Integer>>();
            while (itemPot.size() > 0) {
                itemTempList.add(itemPot.remove(random.nextInt(itemPot.size())));
                if (itemPot.size() == 0 || (itemTempList.size() >= blockSize &&
                        itemPot.size() > ((float) blockSize * 0.1))) {
                    executionControl.updateProgress(itemList.size() - itemPot.size(),
                            itemList.size() * 2,
                            "calculating item similarity");
                    if (!prepareAndStartDuke(itemTempList)) {
                        executionControl.updateProgress(1, 1, "Due to an error execution has been aborted!");
                        return;
                    }
                    itemTempList.clear();
                }
            }
        } else {
            executionControl.updateProgress(1, 2, "calculating item similarity");
            if (!prepareAndStartDuke(itemList)) {
                executionControl.updateProgress(1, 1, "Due to an error execution has been aborted!");
                return;
            }
        }

        stats.setNumberOfRulesCreated(numberOfAssociationsCreated);

        // delete associations below the threshold

        int associationType = typeMappingService.getIdOfAssocType(tenantId, config.getAssociationType());
        itemAssocDAO.removeItemAssocByTenant(config.getTenantId(), associationType,
                sourceType,
                stats.getStartDate());

    }

    @Override
    protected void doInitialize() throws Exception {
        // This method will be run each time easyrec starts-up and you can do some preinitialization of your plugin here.
        logger.info("The plugin is now being initialized by easyrec.");
        // optional - you don't have to implement this method
    }

    @Override
    protected void doInstall() throws Exception {
        // This method will only be called once when the plugin is uploaded to easyrec.
        // You can set-up your database here or do some other run-once tasks.
        logger.info("The plugin is now being installed.");
        // optional - you don't have to implement this method
    }

    @Override
    protected void doUninstall() throws Exception {
        // This method will only be called once when the plugin is deleted from easyrec.
        // You need to remove all resources created by your plugin (including entries in easyrec database tables.)
        logger.info("The plugin is now being uninstalled.");
        // optional - you don't have to implement this method
    }


    /**
     * This procedure calculates loads the duke configuration,
     * reads the item profiles, starts duke to calculate the
     * similarities and writes them as associations in the DB.
     *
     * @param items a list of items which should contain all users of your tenant
     * @return <code>true</code> if the calculation succeeds and <code>false</code> if an
     *         error occurs
     * @throws IOException
     */
    private boolean prepareAndStartDuke(List<ItemVO<Integer, Integer>> items) throws IOException {

        ProfileDukeConfiguration config = getConfiguration();

        String dukeConfigurationString = config.getDukeConfiguration();

        TypeMappingService typeMappingService = (TypeMappingService) super.getTypeMappingService();

        Integer associationType = typeMappingService.getIdOfAssocType(
                config.getTenantId(),
                config.getAssociationType());
        Integer sourceType = typeMappingService.getIdOfSourceType(
                config.getTenantId(),
                this.getId().toString());
        Integer viewType = typeMappingService.getIdOfViewType(
                config.getTenantId(),
                config.getViewType());


        Configuration dukeConfig = null;
        try {
            dukeConfig = ConfigLoader.loadFromString(dukeConfigurationString);
        } catch (SAXException e) {
            logger.error("An error occurred while parsing Duke configuration: " + e.getMessage());
            logger.debug(e.getStackTrace());
            return false;
        } catch (IOException e) {
            logger.error("An error occurred while parsing Duke configuration: " + e.getMessage());
            logger.debug(e.getStackTrace());
            return false;
        }

        List<Property> propertyList = dukeConfig.getProperties();
        propertyList.add(new Property("ID"));
        Property itemIDProperty = new Property("ItemID");
        itemIDProperty.setIgnoreProperty(true);
        propertyList.add(itemIDProperty);
        dukeConfig.setProperties(propertyList);

        lastThreshold = dukeConfig.getThreshold();

        //read properties
        List<Property> props = dukeConfig.getProperties();
        EasyrecXMLFormatDataSource dataSource = new EasyrecXMLFormatDataSource();
        dataSource.setItems(items);
        dataSource.setProfileService(profileService);

        for (Property prop : props) {
            if (prop.getName().equals("ID")) {
                dataSource.addColumn(new Column("?uri", "ID", null, null));
            } else {
                dataSource.addColumn(new Column(prop.getName(), prop.getName(), null, null));
            }
        }
        dataSource.setProps(props);
        dukeConfig.addDataSource(0, dataSource);

        Processor proc = new Processor(dukeConfig);
        EasyrecProfileMatcher easyrecProfileMatcher = new EasyrecProfileMatcher(false, true, this);
        easyrecProfileMatcher.setItemAssocDAO(profileSimilarityItemAssocDAO);
        easyrecProfileMatcher.setAssocType(associationType);
        easyrecProfileMatcher.setConfTanantId(config.getTenantId());

        easyrecProfileMatcher.setSourceType(sourceType);
        easyrecProfileMatcher.setViewType(viewType);

        proc.addMatchListener(easyrecProfileMatcher);
        proc.deduplicate();
        proc.close();

        return true;
    }

    public void setProfileSimilarityItemAssocDAO(ProfileSimilarityItemAssocDAO profileSimilarityItemAssocDAO) {
        this.profileSimilarityItemAssocDAO = profileSimilarityItemAssocDAO;
    }
}
