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

package org.easyrec.plugin.aggregator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.easyrec.plugin.aggregator.model.AggregatorConfiguration;
import org.easyrec.plugin.aggregator.model.AggregatorConfigurationInt;
import org.easyrec.plugin.aggregator.model.AggregatorStatistics;
import org.easyrec.plugin.model.Version;
import org.easyrec.plugin.support.GeneratorPluginSupport;

import java.net.URI;
import java.util.Date;
import java.util.List;
import org.easyrec.model.core.ActionVO;
import org.easyrec.plugin.generator.RunConditionEnabled;
import org.easyrec.utils.io.MySQL;

/**
 *
 * @author szavrel
 */
public class AggregatorGenerator extends GeneratorPluginSupport<AggregatorConfiguration, AggregatorStatistics> implements RunConditionEnabled {

    public static final String DISPLAY_NAME = "User Profile Aggregator";
    public static final Version VERSION = new Version("1.00");
    public static final URI ID = URI.create("http://www.easyrec.org/plugins/UPA");
    public static final String ITEMTYPE_USER = "USER";

    private static final Log logger = LogFactory.getLog(AggregatorGenerator.class);

    private AggregatorService aggregatorService;

    public AggregatorGenerator() {
        super(DISPLAY_NAME, ID, VERSION, AggregatorConfiguration.class, AggregatorStatistics.class);
    }

    @Override
    public AggregatorConfiguration newConfiguration() {
        return new AggregatorConfiguration();
    }

    @Override
    public boolean evaluateRuncondition(Date lastRun) {
        
        if (lastRun == null) return true; // never run before so execute anyway+
        AggregatorConfigurationInt intConfiguration;
        try {
            intConfiguration = aggregatorService.mapTypesToConfiguration(getConfiguration());
        } catch (Exception ex) {
            logger.info("error in mapping configuration during evaluation of runcondition! Execution cancelled!");
            intConfiguration = null;
        }
        if (intConfiguration == null) return false; // if configuration cannot be mapped, no execution necessary
        int actionsSinceLastRun = aggregatorService.getNumberOfActions(intConfiguration, lastRun);
        return actionsSinceLastRun >= 1;
    }

    @Override
    protected void doExecute(ExecutionControl control, AggregatorStatistics stats) throws Exception {

        AggregatorConfiguration configuration = getConfiguration();
        AggregatorConfigurationInt intConfiguration;

        Date start = MySQL.sanitzeForMysql56(new Date());
        stats.setStartDate(start);

        try {
            intConfiguration = aggregatorService.mapTypesToConfiguration(configuration);
            logger.info("TenantId:" + intConfiguration.getTenantId());
        } catch (Exception e) {
            stats.setException(e.getMessage());
            intConfiguration = null;
        }
        if (intConfiguration != null) {
            if (control.isAbortRequested()) throw new Exception("UPA was manually aborted!");
            control.updateProgress(1, 3, "Getting users with actions.");
            List<Integer> users = aggregatorService.getUsersWithActions(intConfiguration);
            stats.setNrUsers(users.size());

            if (control.isAbortRequested()) throw new Exception("UPA was manually aborted!");
            int i=1;
            for (Integer user : users) {
                control.updateProgress(2, 3, "Aggregating user profiles " + i + "/" + stats.getNrUsers());
                List<ActionVO<Integer, Integer>> actions = aggregatorService.getActionsForUser(user, intConfiguration);
                aggregatorService.aggregateUserProfile(user, actions, intConfiguration);
                if (control.isAbortRequested()) throw new Exception("UPA was manually aborted!");
                i++;
            }
            stats.setNumberOfRulesCreated(i-1);
            stats.setNumberOfActionsConsidered(aggregatorService.getNumberOfActions(intConfiguration, null));
            // remove old Rules
            //aggregatorService.removeOldRules(intConfiguration, stats);
            //assocRuleMiningService.perform(configuration.getTenantId());

            control.updateProgress(3, 3, "Finished");
        } // TODO: else write logoutput 
        stats.setEndDate(MySQL.sanitzeForMysql56(new Date()));
        stats.setDuration((stats.getEndDate().getTime() - stats.getStartDate().getTime())/1000);
    }

    @Override
    public String getPluginDescription() {
        return "This plugin allows the aggregation of item and action metadata into simple user/item profiles. "
                + "It does not generate rules, just enriches the metadata in profiles. The generated data can then be used for matching techniques";
    }


    // ----------------------------- GETTER / SETTER METHODS -------------------------------------

    public void setAggregatorService(AggregatorService aggregatorService) {
        this.aggregatorService = aggregatorService;
    }

}
