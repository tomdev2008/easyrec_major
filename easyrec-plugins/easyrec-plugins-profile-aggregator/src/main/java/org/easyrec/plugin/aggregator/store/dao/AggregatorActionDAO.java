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
package org.easyrec.plugin.aggregator.store.dao;


import java.util.Date;
import java.util.List;
import org.easyrec.model.core.ActionVO;
import org.easyrec.plugin.aggregator.model.AggregatorConfigurationInt;

/**
 * This interface provides methods to access data in a rulemining database.
 * <p/>
 * <p><b>Company:&nbsp;</b>
 * SAT, Research Studios Austria</p>
 * <p/>
 * <p><b>Copyright:&nbsp;</b>
 * (c) 2006</p>
 * <p/>
 * <p><b>last modified:</b><br/>
 * $Author: szavrel $<br/>
 * $Date: 2011-02-11 11:04:49 +0100 (Fr, 11 Feb 2011) $<br/>
 * $Revision: 17656 $</p>
 *
 * @author Stephan Zavrel
 */
public interface AggregatorActionDAO {
    
    public List<Integer> getUsersWithActions(AggregatorConfigurationInt configuration);
    
    public List<ActionVO<Integer,Integer>> getActionsForUsers(Integer userId, AggregatorConfigurationInt configuration);
    
    public int getNumberOfActions(Integer tenantId, Integer actionType, Date lastRun);
    
}
