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
public abstract class FieldStoreAPITest extends AbstractApiTest {

    public static class JSON extends FieldStoreAPITest {
        public JSON() throws TestContainerException {
            super(METHOD_JSON);
        }
    }

    public static class XML extends FieldStoreAPITest {
        public XML() throws TestContainerException {
            super(METHOD_XML);
        }
    }

    public FieldStoreAPITest(String method) throws TestContainerException {
        super("profile/field/store", method);
    }

    private static final String ACTION = "profile/field/store";

    @Test
    @ExpectedDataSet("/dbunit/web/rest/profile/field_store_success.xml")
    public void storeFieldSuccess() {


        MultivaluedMap<String, String> params = new MultivaluedMapImpl();
        JSONObject json;

        //store profile field into a not existing item
        params.add("tenantid", TENANT_ID);
        params.add("apikey", API_KEY);
        params.add("itemid", "PROFILE_TEST_ITEM_NOT_EXISTING");
        params.add("field", "/profile/name");
        params.add("value", "name_1");

        json = makeAPIRequest(params);

        assertThat(json, not(is(nullValue())));
        assertThat(json.getString("action"), is(ACTION));
        assertThat(json.getJSONObject("success").getString("@code"), is("315"));

        //store profile field into an existing profile field
        params = new MultivaluedMapImpl();
        params.add("tenantid", TENANT_ID);
        params.add("apikey", API_KEY);
        params.add("itemid", "PROFILE_TEST_ITEM_1");
        params.add("field", "/profile/name");
        params.add("value", "name_1");

        json = makeAPIRequest(params);

        assertThat(json, not(is(nullValue())));
        assertThat(json.getString("action"), is(ACTION));
        assertThat(json.getJSONObject("success").getString("@code"), is("315"));

        //store profile field into an existing item but not existing profile field
        params = new MultivaluedMapImpl();
        params.add("tenantid", TENANT_ID);
        params.add("apikey", API_KEY);
        params.add("itemid", "PROFILE_TEST_ITEM_1");
        params.add("field", "/profile/weight");
        params.add("value", "70");

        json = makeAPIRequest(params);

        assertThat(json, not(is(nullValue())));
        assertThat(json.getString("action"), is(ACTION));
        assertThat(json.getJSONObject("success").getString("@code"), is("315"));
    }

    @Test
    public void storeFieldWrongAPIKeyTenantCombination() {

        MultivaluedMap<String, String> params = new MultivaluedMapImpl();
        params.add("tenantid", TENANT_ID);
        params.add("apikey", API_KEY + "x");
        params.add("itemid", "PROFILE_TEST_ITEM_1");
        params.add("field", "/profile/weight");
        params.add("value", "70");

        JSONObject json = makeAPIRequest(params);

        assertThat(json, not(is(nullValue())));
        assertThat(json.getJSONObject("error").getString("@code"), is("299"));
    }

    @Test
    public void storeFieldNoAPIKey() {

        MultivaluedMap<String, String> params = new MultivaluedMapImpl();
        params.add("tenantid", TENANT_ID);
        params.add("itemid", "PROFILE_TEST_ITEM_1");
        params.add("field", "/profile/weight");
        params.add("value", "70");

        JSONObject json = makeAPIRequest(params);

        assertThat(json, not(is(nullValue())));
        assertThat(json.getJSONObject("error").getString("@code"), is("330"));
    }

    @Test
    public void storeFieldNoTenantID() {

        MultivaluedMap<String, String> params = new MultivaluedMapImpl();
        params.add("apikey", API_KEY);
        params.add("itemid", "PROFILE_TEST_ITEM_1");
        params.add("field", "/profile/weight");
        params.add("value", "70");

        JSONObject json = makeAPIRequest(params);

        assertThat(json, not(is(nullValue())));
        assertThat(json.getJSONObject("error").getString("@code"), is("331"));
    }

    @Test
    public void storeFieldNoItemID() {

        MultivaluedMap<String, String> params = new MultivaluedMapImpl();
        params.add("tenantid", TENANT_ID);
        params.add("apikey", API_KEY);
        params.add("field", "/profile/weight");
        params.add("value", "70");

        JSONObject json = makeAPIRequest(params);

        assertThat(json, not(is(nullValue())));
        assertThat(json.getJSONObject("error").getString("@code"), is("301"));
    }

    @Test
    public void storeFieldNoField() {

        MultivaluedMap<String, String> params = new MultivaluedMapImpl();
        params.add("tenantid", TENANT_ID);
        params.add("apikey", API_KEY);
        params.add("itemid", "PROFILE_TEST_ITEM_1");
        params.add("value", "70");

        JSONObject json = makeAPIRequest(params);

        assertThat(json, not(is(nullValue())));
        assertThat(json.getJSONObject("error").getString("@code"), is("335"));
    }

    @Test
    public void storeFieldNoValue() {

        MultivaluedMap<String, String> params = new MultivaluedMapImpl();
        params.add("tenantid", TENANT_ID);
        params.add("apikey", API_KEY);
        params.add("itemid", "PROFILE_TEST_ITEM_1");
        params.add("field", "/profile/weight");

        JSONObject json = makeAPIRequest(params);

        assertThat(json, not(is(nullValue())));
        assertThat(json.getJSONObject("error").getString("@code"), is("336"));
    }

    @Test
    public void storeFieldItemTypeNotFound() {

        MultivaluedMap<String, String> params = new MultivaluedMapImpl();
        params.add("tenantid", TENANT_ID);
        params.add("apikey", API_KEY);
        params.add("itemid", "PROFILE_TEST_ITEM_1");
        params.add("itemtype", "I_DO_NOT_EXIST");
        params.add("field", "/profile/weight");
        params.add("value", "70");

        JSONObject json = makeAPIRequest(params);

        assertThat(json, not(is(nullValue())));
        assertThat(json.getJSONObject("error").getString("@code"), is("912"));
    }

    @Test
    public void storeFieldIllegalXPath() {

        MultivaluedMap<String, String> params = new MultivaluedMapImpl();
        params.add("tenantid", TENANT_ID);
        params.add("apikey", API_KEY);
        params.add("itemid", "PROFILE_TEST_ITEM_1");
        params.add("field", "/profile/weight/");
        params.add("value", "70");

        JSONObject json = makeAPIRequest(params);

        assertThat(json, not(is(nullValue())));
        assertThat(json.getJSONObject("error").getString("@code"), is("912"));

        params = new MultivaluedMapImpl();
        params.add("tenantid", TENANT_ID);
        params.add("apikey", API_KEY);
        params.add("itemid", "PROFILE_TEST_ITEM_1");
        params.add("field", "/profile/weight/[");
        params.add("value", "70");

        json = makeAPIRequest(params);

        assertThat(json, not(is(nullValue())));
        assertThat(json.getJSONObject("error").getString("@code"), is("912"));

        // try to store a filed under a second root node
        params = new MultivaluedMapImpl();
        params.add("tenantid", TENANT_ID);
        params.add("apikey", API_KEY);
        params.add("itemid", "PROFILE_TEST_ITEM_1");
        params.add("field", "/anotherProfile/weight");
        params.add("value", "70");

        json = makeAPIRequest(params);

        assertThat(json, not(is(nullValue())));
        assertThat(json.getJSONObject("error").getString("@code"), is("912"));
    }

    @Test
    public void storeFieldMultipleNodes() {

        MultivaluedMap<String, String> params = new MultivaluedMapImpl();
        params.add("tenantid", TENANT_ID);
        params.add("apikey", API_KEY);
        params.add("itemid", "PROFILE_TEST_ITEM_2");
        params.add("field", "/profile/name");
        params.add("value", "70");

        JSONObject json = makeAPIRequest(params);

        assertThat(json, not(is(nullValue())));
        assertThat(json.getJSONObject("error").getString("@code"), is("337"));

    }
}
