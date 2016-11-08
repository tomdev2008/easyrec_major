/*
 * Copyright 2012 Research Studios Austria Forschungsgesellschaft mBH
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
 * along with easyrec. If not, see <http://www.gnu.org/licenses/>.
 */

package org.easyrec.rest.api_v_1_0.profile;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * This class is a container class for all the Profile API tests.
 *
 * @author Fabian Salcher
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({ProfileAPITestSuite.XML.class, ProfileAPITestSuite.JSON.class})
public class ProfileAPITestSuite {
    @RunWith(Suite.class)
    @Suite.SuiteClasses({StoreAPITest.XML.class, LoadAPITest.XML.class, DeleteAPITest.XML.class,
            FieldStoreAPITest.XML.class, FieldLoadAPITest.XML.class, FieldDeleteAPITest.XML.class})
    public static class XML {
    }

    @RunWith(Suite.class)
    @Suite.SuiteClasses({StoreAPITest.JSON.class, LoadAPITest.JSON.class, DeleteAPITest.JSON.class,
            FieldStoreAPITest.JSON.class, FieldLoadAPITest.JSON.class, FieldDeleteAPITest.JSON.class})
    public static class JSON {
    }
}
