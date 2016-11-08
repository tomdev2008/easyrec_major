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
package org.easyrec.service.web;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.easyrec.model.core.web.RemoteTenant;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.LinkedBlockingQueue;
import org.easyrec.store.dao.web.RemoteTenantDAO;

/**
 * This Class adds a tenant to the plugin queue at its execution time
 * every 24 hours.
 * <p/>
 * <p><b>Company:&nbsp;</b>
 * SAT, Research Studios Austria</p>
 * <p/>
 * <p><b>Copyright:&nbsp;</b>
 * (c) 2007</p>
 * <p/>
 * <p><b>last modified:</b><br/>
 * $Author: fsalcher $<br/>
 * $Date: 2012-03-19 14:22:17 +0100 (Mo, 19 Mär 2012) $<br/>
 * $Revision: 18781 $</p>
 *
 * @author phlavac
 * @version <CURRENT PROJECT VERSION>
 * @since <PROJECT VERSION ON FILE CREATION>
 */
public class PluginTimerTask {

    // TODO: move to vocabulary?
    private static final long FIXED_RATE = 1000 * 60 * 60 * 24; // schedule every 24hours
    private final Log logger = LogFactory.getLog(getClass());

    private Timer pluginTimer = null;
    private final RemoteTenantDAO remoteTenantDAO;
    private final String exectutionTime;

    private class PluginInnerTimerTask extends TimerTask {

        private final RemoteTenantDAO remoteTenantDAO;
        private final LinkedBlockingQueue<RemoteTenant> queue;
        private final String executionTime;
        private final Log logger = LogFactory.getLog(getClass());

        public PluginInnerTimerTask(RemoteTenantDAO remoteTenantDAO, LinkedBlockingQueue<RemoteTenant> queue, String executionTime) {
            this.remoteTenantDAO = remoteTenantDAO;
            this.queue = queue;
            this.executionTime = executionTime;
        }

        @Override
        public void run() {
            
            logger.info("Getting tenants scheduled for plugin execution at " + executionTime);
            
            List<RemoteTenant> tenants = remoteTenantDAO.getTenantsByExecutionTime(RemoteTenant.SCHEDULER_EXECUTION_TIME, executionTime);

            queue.addAll(tenants);
            
            logger.info("Added " + tenants.size() + " tenants to queue!");
        }
    };

    public PluginTimerTask(RemoteTenantDAO remoteTenantDAO, LinkedBlockingQueue<RemoteTenant> queue, String executionTime) {

        logger.debug(
                "Init PluginTimerTask for " + executionTime);

        this.remoteTenantDAO = remoteTenantDAO;
        this.exectutionTime = executionTime;

        pluginTimer = new Timer();
        pluginTimer.scheduleAtFixedRate(new PluginInnerTimerTask(remoteTenantDAO, queue, executionTime),
                getExecutionTime(executionTime), FIXED_RATE);
    }

    /**
     * Cancels this task
     */
    public void destroy() {
        pluginTimer.cancel();
        pluginTimer = null;
        logger.debug(
                "cancel Timertask for '" + exectutionTime);
    }


    /**
     * This functions returns the time when the plugins have to be executed for the first time for a given time String.
     *
     * @param exeTime: e.g. 23:45
     *
     */
    private Date getExecutionTime(String exeTime) {
        try {
            if (exeTime == null) {
                exeTime = "02:00";
            }
            Date now = new Date();
            DateFormat exeTimeFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm");
            DateFormat todayDateFormat = new SimpleDateFormat("dd.MM.yyyy");
            Date execDate = (Date) exeTimeFormat.parse(todayDateFormat.format(now) + " " + exeTime);
            if (now.after(execDate)) {
                Calendar cal = Calendar.getInstance();
                cal.setTime(execDate);
                cal.add(Calendar.DATE, 1);
                execDate = cal.getTime();
            }
            return execDate;
        } catch (ParseException ex) {
            logger.info(ex.getMessage());
        }
        return null;
    }
}