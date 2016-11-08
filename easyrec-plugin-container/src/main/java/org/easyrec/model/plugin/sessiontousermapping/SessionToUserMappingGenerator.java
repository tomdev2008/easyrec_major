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
package org.easyrec.model.plugin.sessiontousermapping;

import java.util.Iterator;
import java.util.List;
import org.easyrec.plugin.model.PluginId;
import org.easyrec.plugin.support.GeneratorPluginSupport;
import org.easyrec.store.dao.IDMappingDAO;
import org.easyrec.store.dao.core.ActionDAO;
import org.easyrec.utils.spring.store.ResultSetIteratorMysql;

/**
 *
 * @author Stephan
 */
public class SessionToUserMappingGenerator extends GeneratorPluginSupport<SessionToUserMappingConfiguration, SessionToUserMappingStatistics> {

    public static final String DISPLAY_NAME = "Session-to-User-mapping";
    public static final PluginId ID = new PluginId("http://www.easyrec.org/internal/SessionToUserMapping", "1.00");
    public static final int ASSOCTYPE = -1;

    private ActionDAO actionDAO;
    private IDMappingDAO iDMappingDAO;
    
    
    public SessionToUserMappingGenerator() {
        super(DISPLAY_NAME, ID.getUri(), ID.getVersion(), SessionToUserMappingConfiguration.class,
                SessionToUserMappingStatistics.class);
    }
    
    private void init() {
        install(false);
        initialize();
    }
    
    @Override
    protected void doExecute(ExecutionControl control, SessionToUserMappingStatistics stats) throws Exception {
        
        // get all sessions since lastRun where count distinct userid > 1;
        Iterator<String> sessions = actionDAO.getMultiUserSessions(getConfiguration().getTenantId(), getConfiguration().getLastRun(), true);
        int actionCount = 0;
        int sessionCount = 0;
        // For all sessionids, get the list of userIds for this session
        
        for (Iterator<String> it = sessions; it.hasNext();sessionCount++) {
            String session = it.next();
            List<Integer> userIds = actionDAO.getUserIdsOfSession(getConfiguration().getTenantId(), getConfiguration().getLastRun(), session);
            Integer newId = null;
            for (Integer userId : userIds) {
                String userSring = iDMappingDAO.lookup(userId);
                if (!session.equals(userSring)) { //it is a real userId
                    newId = userId;
                    break;
                }
            }
            if (newId != null) {
                actionCount += actionDAO.updateActionsOfSession(getConfiguration().getTenantId(), getConfiguration().getLastRun(), session, newId);
            } else {
                // if no userId matches the sessionid, we cannot map. To avoid an endless loop with the iterator we move the offset for the next iterator
                // bulk call up by one because the session we just found will remain in the result set and should then be ignored.
                ((ResultSetIteratorMysql<String>) sessions).incOffsetInResult();
            }
            
        }
        stats.setLastRun(getConfiguration().getLastRun());
        stats.setNumberOfSessionsFound(sessionCount);
        stats.setNumberOfActionsConsidered(actionCount);
    }

    @Override
    public String getPluginDescription() {
        return "easyrec internal session to user mapping";
    }

    public ActionDAO getActionDAO() {
        return actionDAO;
    }

    public void setActionDAO(ActionDAO actionDAO) {
        this.actionDAO = actionDAO;
    }

    public IDMappingDAO getiDMappingDAO() {
        return iDMappingDAO;
    }

    public void setiDMappingDAO(IDMappingDAO iDMappingDAO) {
        this.iDMappingDAO = iDMappingDAO;
    }
        
}
