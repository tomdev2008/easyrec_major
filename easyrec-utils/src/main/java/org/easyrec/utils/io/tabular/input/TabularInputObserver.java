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
package org.easyrec.utils.io.tabular.input;

import java.util.List;

/**
 * Interface for clients of TabularInput implementations.
 * <p/>
 * <p><b>Company:&nbsp;</b>
 * SAT, Research Studios Austria</p>
 * <p/>
 * <p><b>Copyright:&nbsp;</b>
 * (c) 2007</p>
 * <p/>
 * <p><b>last modified:</b><br/>
 * $Author: szavrel $<br/>
 * $Date: 2011-03-22 15:26:04 +0100 (Di, 22 Mär 2011) $<br/>
 * $Revision: 17973 $</p>
 *
 * @author Florian Kleedorfer
 */

public interface TabularInputObserver {

    public void onStart(int columnCount, List<String> columnNames);

    public void onDataRow(int rowNum, List<String> values);

    public void onFinish(int rowCount);

    /**
     * Called when processing fails. No more calls are issued for the current data source.
     */
    public void onAbort(int rowNum);
}
