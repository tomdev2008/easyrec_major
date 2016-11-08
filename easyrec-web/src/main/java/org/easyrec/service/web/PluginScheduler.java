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
package org.easyrec.service.web;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.easyrec.model.core.web.Operator;
import org.easyrec.model.core.web.RemoteTenant;
import org.easyrec.model.plugin.NamedConfiguration;
import org.easyrec.model.plugin.archive.ArchivePseudoConfiguration;
import org.easyrec.model.plugin.archive.ArchivePseudoGenerator;
import org.easyrec.model.web.EasyRecSettings;
import org.easyrec.plugin.configuration.GeneratorContainer;
import org.easyrec.service.core.TenantService;
import org.easyrec.store.dao.plugin.LogEntryDAO;
import org.easyrec.store.dao.web.OperatorDAO;
import org.easyrec.store.dao.web.RemoteTenantDAO;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.LinkedBlockingQueue;
import org.easyrec.model.plugin.LogEntry;
import org.easyrec.model.plugin.sessiontousermapping.SessionToUserMappingConfiguration;
import org.easyrec.model.plugin.sessiontousermapping.SessionToUserMappingGenerator;

/**
 * This class schedules plugins for each tenant.
 * <p/>
 * TODO update javadoc
 * All tenants, that have an active plugin scheduler flag are added
 * to the plugin TaskList. The PluginTaskList is a HashMap that contains
 * the tenant id as key and a PluginTask as value.
 * <p/>
 * A PluginTask adds a tenant to the execution queue at its execution time.
 * <p/>
 * The queue holds a list of all tenants that are waiting to be scheduled.
 * After a tenant is processed by the plugins, he is removed from the queue.
 *
 * @author phlavac
 */
public class PluginScheduler implements InitializingBean, DisposableBean {

 
    private final Log logger = LogFactory.getLog(getClass());

    private RemoteTenantDAO remoteTenantDAO;
    private OperatorDAO operatorDAO;
    private HashMap<String, PluginTimerTask> timerTaskPerMinute;
    private LogEntryDAO logEntryDAO;
    private LinkedBlockingQueue<RemoteTenant> queue;
    private TenantService tenantService;
    private RemoteTenantService remoteTenantService;
    private EasyRecSettings easyrecSettings;
    private GeneratorContainer generatorContainer;

    private Scheduler scheduler;

    public PluginScheduler() {
        
        queue = new LinkedBlockingQueue<>();
        timerTaskPerMinute = new HashMap<>();
    }

    /**
     * Init PluginScheduler timertasks for all Tenants.
     *
     * @throws java.lang.Exception
     */
    @Override
    public void afterPropertiesSet() throws Exception {

        if (easyrecSettings.isGenerator()) {
            initTasks();
            logEntryDAO.endAllEntries();
            scheduler = new Scheduler(queue);
            scheduler.start();
            logger.info("Plugin Scheduler started.");
        }
    }

    /**
     * Shut down scheduler
     *
     * @throws Exception
     */
    @Override
    public void destroy() throws Exception {
        
        for (PluginTimerTask task : timerTaskPerMinute.values()) {
            if (task != null) {
                task.destroy();
                task = null;
            }
        }
        timerTaskPerMinute.clear();
        logger.info("PluginScheduler shut down.");
    }

    /**
     * Add a PluginTimerTask to a Tenant
     *
     * @param remoteTenant RemoteTenant
     * @param previousTime
     */
    public void addTask(RemoteTenant remoteTenant, String previousTime) {
        
        if (previousTime != null) {
            PluginTimerTask ptt = timerTaskPerMinute.get(previousTime);
            if (ptt != null) { // a TimerTask for the given minute exists
                 // check if any other tenant uses this execution time
                List<RemoteTenant> tenants = remoteTenantDAO.getTenantsByExecutionTime(RemoteTenant.SCHEDULER_EXECUTION_TIME, previousTime);
                // if no other tenants needs the minute anymore, remove
                if ((tenants == null) || (tenants.isEmpty())) {
                    timerTaskPerMinute.remove(previousTime);
                    ptt.destroy();
                    logger.info("Removed TimerTaks for " + previousTime +"! No further tenants scheduled at this time.");
                }
            }
        }
        
        PluginTimerTask tt = timerTaskPerMinute.get(remoteTenant.getSchedulerExecutionTime());
        if (tt == null) { //if no other tenant uses this exectutionTime yet, add a new TimerTask for this minute
            tt = new PluginTimerTask(remoteTenantDAO, queue, remoteTenant.getSchedulerExecutionTime());
            timerTaskPerMinute.put(remoteTenant.getSchedulerExecutionTime(), tt);
            logger.info("Added TimerTask for " + remoteTenant.getSchedulerExecutionTime());
        }
    }


    public void initTasks() {


        List<Operator> operators = operatorDAO.getOperators("", 0, Integer.MAX_VALUE);
        for (Operator operator : operators) {

            List<RemoteTenant> tenants = remoteTenantDAO.getTenantsFromOperator(operator.getOperatorId());

            for (RemoteTenant r : tenants) {

                if (r.isSchedulerEnabled()) {
                    addTask(r, null);
                    //logger.info("'" + r.getOperatorId() + " - " + r.getStringId() + "' added to PluginTask List");
                }
            }
        }
    }

    private class Scheduler extends Thread {

        private final Log logger = LogFactory.getLog(getClass());

        LinkedBlockingQueue<RemoteTenant> queue;
        RemoteTenant remoteTenant;

        Scheduler(LinkedBlockingQueue<RemoteTenant> queue) {
            this.queue = queue;
        }

        // look if tenants are waiting in queue. If so process plugins for every tenant in queue.
        @Override
        public void run() {
            Thread thisThread = Thread.currentThread();

            while (!thisThread.isInterrupted() && scheduler == thisThread) {

                try {
                    remoteTenant = queue.take();

                    final Properties tenantConfig = tenantService.getTenantConfig(remoteTenant.getId());

                    if (tenantConfig == null) {
                        logger.warn("could not get tenant configuration, aborting");

                        return;
                    }

                    if ("true".equals(tenantConfig.getProperty(RemoteTenant.AUTO_ARCHIVER_ENABLED))) {
                        String daysString = tenantConfig.getProperty(RemoteTenant.AUTO_ARCHIVER_TIME_RANGE);
                        final int days = Integer.parseInt(daysString);
                        ArchivePseudoConfiguration configuration = new ArchivePseudoConfiguration(days);
                        configuration.setAssociationType("ARCHIVE");
                        NamedConfiguration namedConfiguration = new NamedConfiguration(remoteTenant.getId(), ArchivePseudoGenerator.ASSOCTYPE,
                                ArchivePseudoGenerator.ID, "Archive", configuration, true);

                        logger.info("Archiving actions older than " + days + " day(s)");

                        generatorContainer.runGenerator(namedConfiguration, true);
                    } else {
                        logger.info("Archiving disabled for tenant: "+ remoteTenant.getOperatorId() + ":" +
                                remoteTenant.getStringId());
                    }
                    
                    if ("true".equals(tenantConfig.getProperty(RemoteTenant.SESSION_TO_USER_MAPPING_ENABLED))) {
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        Date lastRun;
                        try {
                            lastRun = sdf.parse(remoteTenant.getCreationDate());
                        } catch (ParseException ex) {
                            logger.error("Error parsing Tenant creationDate! Using fallback.");
                            lastRun = new Date(System.currentTimeMillis() - (365 * 86400000l)); //fallback one year
                        }
                        List<LogEntry> lastRunEntry = logEntryDAO.getLogEntriesForTenant(remoteTenant.getId(), SessionToUserMappingGenerator.ASSOCTYPE, 0, 1);
                        
                        if ((lastRunEntry != null) && (!lastRunEntry.isEmpty())) {
                            LogEntry le = lastRunEntry.get(0);
                            lastRun = le.getStartDate();
                        }
                        
                        SessionToUserMappingConfiguration configuration = new SessionToUserMappingConfiguration(lastRun);
                        configuration.setAssociationType("SESSION_USER_MAPPING");
                        NamedConfiguration namedConfiguration = new NamedConfiguration(remoteTenant.getId(), SessionToUserMappingGenerator.ASSOCTYPE,
                                SessionToUserMappingGenerator.ID, "Session-to-User-mapping", configuration, true);

                        logger.info("Mapping Sessions to users since lastRun: " + lastRun);

                        generatorContainer.runGenerator(namedConfiguration, true);
                    } else {
                        logger.info("Session-to-User-mapping disabled for tenant: "+ remoteTenant.getOperatorId() + ":" +
                                remoteTenant.getStringId());
                    }

                    logger.info("starting generator plugin for tenant: " + remoteTenant.getOperatorId() + ":" +
                            remoteTenant.getStringId());
                    
                    generatorContainer.runGeneratorsForTenant(remoteTenant.getId(), false);

                    ///////////////////////////////////////
                    // TODO: insert logic here to trigger plugin generators
                    // TODO: send call to REST-API to mostview ALL Time to get results cached
                    // Problem: how to get ContextPath the needs to present to build backtracking URL?

                    remoteTenantService.updateTenantStatistics(remoteTenant.getId());
                } catch (InterruptedException ex) {
                    logger.debug("PluginScheduler stopped. ");
                }
                }
            }
        }
    

    public void setOperatorDAO(OperatorDAO operatorDAO) {
        this.operatorDAO = operatorDAO;
    }

    public void setRemoteTenantDAO(RemoteTenantDAO remoteTenantDAO) {
        this.remoteTenantDAO = remoteTenantDAO;
    }

    public void setLogEntryDAO(LogEntryDAO logEntryDAO) {
        this.logEntryDAO = logEntryDAO;
    }

    public void setTenantService(TenantService tenantService) {
        this.tenantService = tenantService;
    }

    public void setRemoteTenantService(RemoteTenantService remoteTenantService) {
        this.remoteTenantService = remoteTenantService;
    }

    public void setEasyrecSettings(EasyRecSettings easyrecSettings) {
        this.easyrecSettings = easyrecSettings;
    }

    public void setGeneratorContainer(GeneratorContainer generatorContainer) {
        this.generatorContainer = generatorContainer;
    }
}
