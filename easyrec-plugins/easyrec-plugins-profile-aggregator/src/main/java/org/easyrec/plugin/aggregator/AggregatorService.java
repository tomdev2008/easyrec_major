/**Copyright 2010 Research Studios Austria Forschungsgesellschaft mBH
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

import java.util.Date;
import java.util.List;
import org.easyrec.model.core.ActionVO;
import org.easyrec.plugin.aggregator.model.AggregatorStatistics;
import org.easyrec.plugin.aggregator.model.AggregatorConfiguration;
import org.easyrec.plugin.aggregator.model.AggregatorConfigurationInt;

/**
 * <DESCRIPTION>
 * <p/>
 * <p><b>Company:&nbsp;</b>
 * SAT, Research Studios Austria</p>
 * <p/>
 * <p><b>Copyright:&nbsp;</b>
 * (c) 2007</p>
 * <p/>
 * <p><b>last modified:</b><br/>
 * $Author: szavrel $<br/>
 * $Date: 2011-02-11 11:04:49 +0100 (Fr, 11 Feb 2011) $<br/>
 * $Revision: 17656 $</p>
 *
 * @author Stephan Zavrel
 */
public interface AggregatorService {
    public static final String PARAM_SUPPORT_PRCT = "supportPrcnt";
    public static final String PARAM_SUPPORT_ABS = "supportMinAbs";
    public static final String PARAM_CONFIDENCE_PRCT = "confidencePrcnt";
    public static final String PARAM_METRIC = "metricType";
    public static final String PARAM_EXCL_SINGLE_ITEM_BASKETS = "excludeSingleItemBaskets";
    // Added PH: dynamic L1
    public static final String PARAM_MAX_SIZE_L1 = "maxSizeL1";
    public static final String GENERATOR_ID = "ProfileAggregator";
    public static final String GENERATOR_VERSION = "0.98";



    public AggregatorConfigurationInt mapTypesToConfiguration(AggregatorConfiguration configuration) throws Exception;
    
    public List<Integer> getUsersWithActions(AggregatorConfigurationInt configurationInt);
    
    public List<ActionVO<Integer,Integer>> getActionsForUser(Integer userId, AggregatorConfigurationInt configurationInt);
    
    public void aggregateUserProfile(Integer userId, List<ActionVO<Integer,Integer>> actions, AggregatorConfigurationInt configurationInt);
    
    public void removeOldRules(AggregatorConfigurationInt configuration, AggregatorStatistics stats);
    
    public Integer getNumberOfActions(AggregatorConfigurationInt intConfiguration, Date lastRun);

}
