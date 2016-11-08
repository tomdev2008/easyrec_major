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
package org.easyrec.service;

import org.easyrec.model.core.transfer.TimeConstraintVO;

import java.util.Iterator;
import java.util.List;

/**
 * Base interface for RecommendationHistoryServices, describes methods to retrieve stored recommendations (from the recommender engine).
 * <p/>
 * <p><b>Company:&nbsp;</b>
 * SAT, Research Studios Austria</p>
 * <p/>
 * <p><b>Copyright:&nbsp;</b>
 * (c) 2007</p>
 * <p/>
 * <p><b>last modified:</b><br/>
 * $Author: pmarschik $<br/>
 * $Date: 2011-02-11 11:04:49 +0100 (Fr, 11 Feb 2011) $<br/>
 * $Revision: 17656 $</p>
 *
 * @author Roman Cerny
 */
public interface BaseRecommendationHistoryService<R, RI> {
    ////////////////////////////////////////////////////////////////////////////
    // typed methods
    public int insertRecommendation(R recommendation);

    public Iterator<R> getRecommendationIterator(int bulkSize);

    public Iterator<R> getRecommendationIterator(int bulkSize, TimeConstraintVO timeConstraints);

    public Iterator<RI> getRecommendedItemIterator(int bulkSize);

    public List<RI> getRecommendedItems(TimeConstraintVO timeConstraints);

    public R loadRecommendation(Integer recommendationId);

    public List<RI> getRecommendedItemsOfRecommendation(Integer recommendationId);

    // HINT: add more convenience methods, byTenantId(), byQueriedItemType(), byAssociatedActionType(), getRecommendationsQBE(), ... (Mantis Issue: #722)
}
