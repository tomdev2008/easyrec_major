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

package org.easyrec.plugin.profilesolr;

import java.io.File;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.easyrec.plugin.model.Version;
import org.easyrec.plugin.support.GeneratorPluginSupport;

import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.easyrec.plugin.generator.RunConditionEnabled;
import org.easyrec.plugin.profilesolr.model.SolrSimilarityConfiguration;
import org.easyrec.plugin.profilesolr.model.SolrSimilarityConfigurationInt;
import org.easyrec.plugin.profilesolr.model.SolrSimilarityStatistics;
import org.easyrec.utils.io.MySQL;
import org.easyrec.utils.io.TreeCopy;
import org.easyrec.utils.io.TreeDelete;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.core.io.Resource;

/**
 *
 * @author szavrel
 */
public class SolrSimilarityGenerator extends GeneratorPluginSupport<SolrSimilarityConfiguration, SolrSimilarityStatistics> implements RunConditionEnabled, DisposableBean {

    public static final String DISPLAY_NAME = "Solr Profile Similarity";
    public static final Version VERSION = new Version("1.00");
    public static final URI ID = URI.create("http://www.easyrec.org/plugins/solr");
    public static final String ITEMTYPE_USER = "USER";

    private static final Log logger = LogFactory.getLog(SolrSimilarityGenerator.class);

    private Resource pluginFolder;
    private Resource solrFolder;
    
    private Path solrHomeFolder;
    
    private SolrSimilarityService solrSimilarityService;

    public SolrSimilarityGenerator() {
        super(DISPLAY_NAME, ID, VERSION, SolrSimilarityConfiguration.class, SolrSimilarityStatistics.class);
    }

    @Override
    public SolrSimilarityConfiguration newConfiguration() {
        return new SolrSimilarityConfiguration();
    }

    @Override
    public boolean evaluateRuncondition(Date lastRun) {
       return true; 
    }

    @Override
    protected void doInstall() throws Exception {


        logger.info("Using plugin folder: " + pluginFolder.getURL());
        logger.info("Using solr folder: " + solrFolder.getURL().toString());
        
        String t = solrFolder.getURI().getRawSchemeSpecificPart().substring(0, solrFolder.getURI().getRawSchemeSpecificPart().lastIndexOf("!"));
        
        File target = pluginFolder.getFile();
        logger.info(target.isDirectory());
        File source = null;
        for (File f : target.listFiles()) {
            logger.info(f.getName());
            if (t.contains(f.getName().replaceAll(" ", "%20"))) 
            {
                source = f;
                break;
            }
        }

        Path p = Paths.get(source.toURI());
        
        FileSystem fs = FileSystems.newFileSystem(p, null);
        Path solr = fs.getPath("/solr");
        
        solrHomeFolder = Paths.get(target.getPath(),"solr");
        TreeCopy tc = new TreeCopy(solr, solrHomeFolder);
        Files.walkFileTree(solr, tc);
        
//        EmbeddedSolrServer solrServer = new EmbeddedSolrServer(solrHomeFolder,"easyrec");

//        String urlString = "http://localhost:8983/solr/easyrec";
//        SolrClient solrClient = new HttpSolrClient(urlString);
//        if (solrServer == null) throw new Exception("Could not initialized Solr server!");
//        solrSimilarityService.setSolrClient(solrServer);
        
    }

    @Override
    protected void doInitialize() throws Exception {
        if (solrHomeFolder == null) {
            File target = pluginFolder.getFile();
            solrHomeFolder = Paths.get(target.getPath(),"solr");
        }
        if (Files.notExists(solrHomeFolder)) {
            doInstall();
        }
        
        EmbeddedSolrServer solrServer = new EmbeddedSolrServer(solrHomeFolder,"easyrec");

//        String urlString = "http://localhost:8983/solr/easyrec";
//        SolrClient solrClient = new HttpSolrClient(urlString);
        if (solrServer == null) throw new Exception("Could not initialize Solr server!");
        solrSimilarityService.setSolrClient(solrServer);
    }
    
    
    
    @Override
    protected void doExecute(ExecutionControl control, SolrSimilarityStatistics stats) throws Exception {

        SolrSimilarityConfiguration configuration = getConfiguration();
        SolrSimilarityConfigurationInt config;

        Date start = MySQL.sanitzeForMysql56(new Date());
        stats.setStartDate(start);
        try {
            config = solrSimilarityService.mapTypesToConfiguration(configuration);
            logger.info("TenantId:" + config.getTenantId());
        } catch (Exception e) {
            stats.setException(e.getMessage());
            config = null;
        }
        if (config != null) {
            control.updateProgress(1, 4, "Indexing items");
            solrSimilarityService.addItemsToIndex(config);
            control.updateProgress(2, 4, "matching profiles");
            solrSimilarityService.matchProfiles(config, stats);
            control.updateProgress(3, 4, "removing old rules");
            solrSimilarityService.removeOldRules(config, stats);
            control.updateProgress(4, 4, "Finished");
        }
        stats.setEndDate(MySQL.sanitzeForMysql56(new Date()));
        stats.setDuration((stats.getEndDate().getTime() - stats.getStartDate().getTime())/1000);
    }

    @Override
    public String getPluginDescription() {
        return "This plugin tries to find similar items based on item (user) profiles indexed by solr.";
    }

    @Override
    protected void doUninstall() throws Exception {
        SolrClient sc = solrSimilarityService.getSolrClient();
        if (sc != null) sc.close();
        Files.walkFileTree(solrHomeFolder, new TreeDelete());
    }

    @Override
    public void destroy() throws Exception {
        logger.info("Destroying solr plugin!");
        SolrClient sc = solrSimilarityService.getSolrClient();
        if (sc != null) sc.close();
    }
        

    // ----------------------------- GETTER / SETTER METHODS -------------------------------------

    public void setPluginFolder(Resource pluginFolder) {
        this.pluginFolder = pluginFolder;
    }

    public void setSolrFolder(Resource solrFolder) {
        this.solrFolder = solrFolder;
    }

    public void setSolrSimilarityService(SolrSimilarityService solrSimilarityService) {
        this.solrSimilarityService = solrSimilarityService;
    }
    
}
