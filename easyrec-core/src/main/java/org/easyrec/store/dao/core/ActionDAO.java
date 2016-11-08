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
package org.easyrec.store.dao.core;

import java.util.Date;
import java.util.Iterator;
import org.easyrec.model.core.ActionVO;
import org.easyrec.model.core.ItemVO;
import org.easyrec.model.core.RankedItemVO;
import org.easyrec.model.core.RatingVO;
import org.easyrec.store.dao.BaseActionDAO;

import java.util.List;

/**
 * This interface provides methods to store data into and read <code>Action</code> entries from a SAT recommender database.
 * <p/>
 * <p><b>Company:&nbsp;</b>
 * SAT, Research Studios Austria</p>
 * <p/>
 * <p><b>Copyright:&nbsp;</b>
 * (c) 2007</p>
 * <p/>
 * <p><b>last modified:</b><br/>
 * $Author: dmann $<br/>
 * $Date: 2011-12-20 15:22:22 +0100 (Di, 20 Dez 2011) $<br/>
 * $Revision: 18685 $</p>
 *
 * @author Roman Cerny
 */
public interface ActionDAO extends
    BaseActionDAO<ActionVO<Integer, Integer>, RankedItemVO<Integer, Integer>, Integer, Integer, ItemVO<Integer, Integer>, RatingVO<Integer, Integer>, Integer, Integer> {

    public Iterator<String> getMultiUserSessions(Integer tenantId, Date lastRun, boolean staticOffset);
    
    public List<Integer> getUserIdsOfSession(Integer tenantId, Date lastRun, String sessionId);
    
    public int updateActionsOfSession(Integer tenantId, Date lastRun, String sessionId, Integer newUserId);
}
