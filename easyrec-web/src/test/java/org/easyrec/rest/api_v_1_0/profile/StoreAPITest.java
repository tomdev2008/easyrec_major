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
 * @author Fabian Salcher
 */

@RunWith(UnitilsJUnit4TestClassRunner.class)
@DataSet(AbstractApiTest.DATASET_BASE)
public abstract class StoreAPITest extends AbstractApiTest {

    public static class JSON extends StoreAPITest {
        public JSON() throws TestContainerException {
            super(METHOD_JSON);
        }
    }

    public static class XML extends StoreAPITest {
        public XML() throws TestContainerException {
            super(METHOD_XML);
        }
    }

    public StoreAPITest(String method) throws TestContainerException {
        super("profile/store", method);
    }

    private static final String ACTION = "profile/store";

    @Test
    @ExpectedDataSet("/dbunit/web/rest/profile/store_success.xml")
    public void storeSuccess() {


        MultivaluedMap<String, String> params = new MultivaluedMapImpl();
        JSONObject json;

        //store profile for a not existing item
        params.add("tenantid", TENANT_ID);
        params.add("apikey", API_KEY);
        params.add("itemid", "ITEM_PROFILE_STORE_1");
        params.add("profile", "<profile><name>test profile</name></profile>");

        json = makeAPIRequest(params);

        assertThat(json, not(is(nullValue())));
        assertThat(json.getString("action"), is(ACTION));
        assertThat(json.getJSONObject("success").getString("@code"), is("310"));

        //store profile for an existing item
        params = new MultivaluedMapImpl();
        params.add("tenantid", TENANT_ID);
        params.add("apikey", API_KEY);
        params.add("itemid", "ITEM_A");
        params.add("profile", "<profile><name>test profile</name></profile>");

        json = makeAPIRequest(params);

        assertThat(json, not(is(nullValue())));
        assertThat(json.getString("action"), is(ACTION));
        assertThat(json.getJSONObject("success").getString("@code"), is("310"));
    }

    @Test
    public void storeWrongAPIKeyTenantCombination() {

        MultivaluedMap<String, String> params = new MultivaluedMapImpl();
        params.add("tenantid", TENANT_ID);
        params.add("apikey", API_KEY + "x");
        params.add("itemid", "ITEM_PROFILE_STORE_2");
        params.add("profile", "<profile><name>test profile</name></profile>");

        JSONObject json = makeAPIRequest(params);

        assertThat(json, not(is(nullValue())));
        assertThat(json.getJSONObject("error").getString("@code"), is("299"));
    }

    @Test
    public void storeNoAPIKey() {

        MultivaluedMap<String, String> params = new MultivaluedMapImpl();
        params.add("tenantid", TENANT_ID);
        params.add("itemid", "ITEM_PROFILE_STORE_2");
        params.add("profile", "<profile><name>test profile</name></profile>");

        JSONObject json = makeAPIRequest(params);

        assertThat(json, not(is(nullValue())));
        assertThat(json.getJSONObject("error").getString("@code"), is("330"));
    }

    @Test
    public void storeNoTenantID() {

        MultivaluedMap<String, String> params = new MultivaluedMapImpl();
        params.add("apikey", API_KEY);
        params.add("itemid", "ITEM_PROFILE_STORE_2");
        params.add("profile", "<profile><name>test profile</name></profile>");

        JSONObject json = makeAPIRequest(params);

        assertThat(json, not(is(nullValue())));
        assertThat(json.getJSONObject("error").getString("@code"), is("331"));
    }

    @Test
    public void storeNoItemID() {

        MultivaluedMap<String, String> params = new MultivaluedMapImpl();
        params.add("tenantid", TENANT_ID);
        params.add("apikey", API_KEY);
        params.add("profile", "<profile><name>test profile</name></profile>");

        JSONObject json = makeAPIRequest(params);

        assertThat(json, not(is(nullValue())));
        assertThat(json.getJSONObject("error").getString("@code"), is("301"));
    }

    @Test
    public void storeNoProfile() {

        MultivaluedMap<String, String> params = new MultivaluedMapImpl();
        params.add("tenantid", TENANT_ID);
        params.add("apikey", API_KEY);
        params.add("itemid", "ITEM_PROFILE_STORE_2");

        JSONObject json = makeAPIRequest(params);

        assertThat(json, not(is(nullValue())));
        assertThat(json.getJSONObject("error").getString("@code"), is("334"));
    }

    @Test
    public void storeItemTypeNotFound() {

        MultivaluedMap<String, String> params = new MultivaluedMapImpl();
        params.add("tenantid", TENANT_ID);
        params.add("apikey", API_KEY);
        params.add("itemid", "ITEM_PROFILE_STORE_2");
        params.add("itemtype", "I_DO_NOT_EXIST");
        params.add("profile", "<profile><name>test profile</name></profile>");

        JSONObject json = makeAPIRequest(params);

        assertThat(json, not(is(nullValue())));
        assertThat(json.getJSONObject("error").getString("@code"), is("912"));
    }

}
