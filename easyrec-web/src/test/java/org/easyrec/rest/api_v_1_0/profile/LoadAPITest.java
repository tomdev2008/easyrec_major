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
 * along with easyrec.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.easyrec.rest.api_v_1_0.profile;

import com.sun.jersey.core.util.MultivaluedMapImpl;
import com.sun.jersey.test.framework.spi.container.TestContainerException;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.easyrec.rest.api_v_1_0.AbstractApiTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.unitils.UnitilsJUnit4TestClassRunner;
import org.unitils.dbunit.annotation.DataSet;
import org.unitils.dbunit.annotation.ExpectedDataSet;

import javax.ws.rs.core.MultivaluedMap;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

/**
 *
 * @author Fabian Salcher
 */
@RunWith(UnitilsJUnit4TestClassRunner.class)
@DataSet(AbstractApiTest.DATASET_BASE)
public abstract class LoadAPITest extends AbstractApiTest {

    public static class JSON extends LoadAPITest {
        public JSON() throws TestContainerException { super(METHOD_JSON); }
    }

    public static class XML extends LoadAPITest {
        public XML() throws TestContainerException { super(METHOD_XML); }
    }

    public LoadAPITest(String method) throws TestContainerException {
        super("profile/load", method);
    }

    private static final String ACTION = "profile/load";

    @Test
    public void loadSuccess() {

        MultivaluedMap<String, String> params = new MultivaluedMapImpl();
        params.add("tenantid", TENANT_ID);
        params.add("apikey", API_KEY);
        params.add("itemid", "PROFILE_TEST_ITEM_1");

        JSONObject json = makeAPIRequest(params);

        assertThat(json, not(is(nullValue())));
        assertThat(json.getString("action"), is(ACTION));
        assertThat(json.getString("tenantID"), is(TENANT_ID));
        assertThat(json.getString("itemID"), is("PROFILE_TEST_ITEM_1"));
        assertThat(json.getString("itemType"), is("ITEM"));
        assertThat(json.getString("profile"), is("<profile><name>test profile</name></profile>"));
    }

    @Test
    public void loadNotExistingItemProfile() {

        MultivaluedMap<String, String> params = new MultivaluedMapImpl();
        params.add("tenantid", TENANT_ID);
        params.add("apikey", API_KEY);
        params.add("itemid", "PROFILE_TEST_ITEM_X");

        JSONObject json = makeAPIRequest(params);

        assertThat(json, not(is(nullValue())));
        assertThat(json.getJSONObject("error").getString("@code"), is("314"));
    }

    @Test
    public void loadWrongAPIKeyTenantCombination() {

        MultivaluedMap<String, String> params = new MultivaluedMapImpl();
        params.add("tenantid", TENANT_ID);
        params.add("apikey", API_KEY + "x");
        params.add("itemid", "PROFILE_TEST_ITEM_1");

        JSONObject json = makeAPIRequest(params);

        assertThat(json, not(is(nullValue())));
        assertThat(json.getJSONObject("error").getString("@code"), is("299"));
    }

    @Test
    public void loadNoAPIKey() {

        MultivaluedMap<String, String> params = new MultivaluedMapImpl();
        params.add("tenantid", TENANT_ID);
        params.add("itemid", "PROFILE_TEST_ITEM_1");

        JSONObject json = makeAPIRequest(params);

        assertThat(json, not(is(nullValue())));
        assertThat(json.getJSONObject("error").getString("@code"), is("330"));
    }

    @Test
    public void loadNoTenantID() {

        MultivaluedMap<String, String> params = new MultivaluedMapImpl();
        params.add("apikey", API_KEY);
        params.add("itemid", "PROFILE_TEST_ITEM_1");

        JSONObject json = makeAPIRequest(params);

        assertThat(json, not(is(nullValue())));
        assertThat(json.getJSONObject("error").getString("@code"), is("331"));
    }

    @Test
    public void loadNoItemID() {

        MultivaluedMap<String, String> params = new MultivaluedMapImpl();
        params.add("tenantid", TENANT_ID);
        params.add("apikey", API_KEY);

        JSONObject json = makeAPIRequest(params);

        assertThat(json, not(is(nullValue())));
        assertThat(json.getJSONObject("error").getString("@code"), is("301"));
    }

    @Test
    public void loadItemTypeNotFound() {

        MultivaluedMap<String, String> params = new MultivaluedMapImpl();
        params.add("tenantid", TENANT_ID);
        params.add("apikey", API_KEY);
        params.add("itemid", "PROFILE_TEST_ITEM_1");
        params.add("itemtype", "I_DO_NOT_EXIST");

        JSONObject json = makeAPIRequest(params);

        assertThat(json, not(is(nullValue())));
        assertThat(json.getJSONObject("error").getString("@code"), is("912"));
    }

}
