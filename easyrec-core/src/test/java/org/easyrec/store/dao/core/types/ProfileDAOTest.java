package org.easyrec.store.dao.core.types;

import org.easyrec.store.dao.core.ProfileDAO;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.unitils.UnitilsJUnit4TestClassRunner;
import org.unitils.dbunit.annotation.DataSet;
import org.unitils.spring.annotation.SpringApplicationContext;
import org.unitils.spring.annotation.SpringBeanByName;

/**Copyright 2012 Research Studios Austria Forschungsgesellschaft mBH
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

//@RunWith(UnitilsJUnit4TestClassRunner.class)
@SpringApplicationContext({
        "spring/web/common/dao/itemDAO.xml",
        "spring/easyrecDataSource.xml",
        "spring/web/common/rankingsCache.xml",
        "spring/utils/aop/Cache.xml"
})
public class ProfileDAOTest {

    @SpringBeanByName
    protected ProfileDAO profileDAO;

    public static final String testDbXml = "/dbunit/itemDaoTest.xml";

//    @Test
//    @DataSet(testDbXml)
//    public void testAdd() {
//
//    }

}
