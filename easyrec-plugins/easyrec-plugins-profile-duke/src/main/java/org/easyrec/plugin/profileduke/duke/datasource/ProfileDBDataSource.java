/*
 * Copyright 2013 Research Studios Austria Forschungsgesellschaft mBH
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

package org.easyrec.plugin.profileduke.duke.datasource;

import no.priv.garshol.duke.DataSource;
import no.priv.garshol.duke.RecordIterator;
import org.apache.log4j.Logger;
import org.easyrec.model.core.ItemVO;
import org.easyrec.service.core.ProfileService;

import java.util.List;

/**
 * User: fsalcher
 * Date: 06.02.13
 */
public class ProfileDBDataSource implements DataSource {

    private List<ItemVO<Integer, Integer>> items;
    private ProfileService profileService;
    private org.apache.log4j.Logger logger;

    public ProfileDBDataSource(List<ItemVO<Integer, Integer>> items,
                               ProfileService profileService,
                               Logger logger) {
        this.items = items;
        this.profileService = profileService;
        this.logger = logger;
    }

    public RecordIterator getRecords() {
        return new ProfileDBRecordIterator(items, profileService, logger);
    }

    public void setLogger(no.priv.garshol.duke.Logger logger) {

    }


}
