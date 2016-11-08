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
public abstract class FieldLoadAPITest extends AbstractApiTest {

    public static class JSON extends FieldLoadAPITest {
        public JSON() throws TestContainerException {
            super(METHOD_JSON);
        }
    }

    public static class XML extends FieldLoadAPITest {
        public XML() throws TestContainerException {
            super(METHOD_XML);
        }
    }

    public FieldLoadAPITest(String method) throws TestContainerException {
        super("profile/field/load", method);
    }

    private static final String ACTION = "profile/field/load";

    @Test
    public void loadFieldSuccess() {

        //store profile field into an existing profile field
        MultivaluedMap<String, String> params = new MultivaluedMapImpl();
        params.add("tenantid", TENANT_ID);
        params.add("apikey", API_KEY);
        params.add("itemid", "PROFILE_TEST_ITEM_1");
        params.add("field", "/profile/name");

        JSONObject json = makeAPIRequest(params);

        assertThat(json, not(is(nullValue())));
        assertThat(json.getString("action"), is(ACTION));
        assertThat(json.getString("tenantID"), is(TENANT_ID));
        assertThat(json.getString("itemID"), is("PROFILE_TEST_ITEM_1"));
        assertThat(json.getString("itemType"), is("ITEM"));
        assertThat(json.getString("field"), is("/profile/name"));
        assertThat(json.getString("value"), is("test profile"));

    }

    @Test
    public void loadFieldNotExistingField() {

        MultivaluedMap<String, String> params = new MultivaluedMapImpl();
        params.add("tenantid", TENANT_ID);
        params.add("apikey", API_KEY);
        params.add("itemid", "PROFILE_TEST_ITEM_1");
        params.add("field", "/profile/weight");

        JSONObject json = makeAPIRequest(params);

        assertThat(json, not(is(nullValue())));
        assertThat(json.getJSONObject("error").getString("@code"), is("316"));
    }

    @Test
    public void loadFieldNotExistingItem() {

        MultivaluedMap<String, String> params = new MultivaluedMapImpl();
        params.add("tenantid", TENANT_ID);
        params.add("apikey", API_KEY);
        params.add("itemid", "PROFILE_TEST_ITEM_NOT_EXISTING");
        params.add("field", "/profile/name");
        params.add("value", "name_1");

        JSONObject json = makeAPIRequest(params);

        assertThat(json, not(is(nullValue())));
        assertThat(json.getJSONObject("error").getString("@code"), is("316"));
    }

    @Test
    public void loadFieldWrongAPIKeyTenantCombination() {

        MultivaluedMap<String, String> params = new MultivaluedMapImpl();
        params.add("tenantid", TENANT_ID);
        params.add("apikey", API_KEY + "x");
        params.add("itemid", "PROFILE_TEST_ITEM_1");
        params.add("field", "/profile/weight");

        JSONObject json = makeAPIRequest(params);

        assertThat(json, not(is(nullValue())));
        assertThat(json.getJSONObject("error").getString("@code"), is("299"));
    }

    @Test
    public void loadFieldNoAPIKey() {

        MultivaluedMap<String, String> params = new MultivaluedMapImpl();
        params.add("tenantid", TENANT_ID);
        params.add("itemid", "PROFILE_TEST_ITEM_1");
        params.add("field", "/profile/weight");

        JSONObject json = makeAPIRequest(params);

        assertThat(json, not(is(nullValue())));
        assertThat(json.getJSONObject("error").getString("@code"), is("330"));
    }

    @Test
    public void loadFieldNoTenantID() {

        MultivaluedMap<String, String> params = new MultivaluedMapImpl();
        params.add("apikey", API_KEY);
        params.add("itemid", "PROFILE_TEST_ITEM_1");
        params.add("field", "/profile/weight");

        JSONObject json = makeAPIRequest(params);

        assertThat(json, not(is(nullValue())));
        assertThat(json.getJSONObject("error").getString("@code"), is("331"));
    }

    @Test
    public void loadFieldNoItemID() {

        MultivaluedMap<String, String> params = new MultivaluedMapImpl();
        params.add("tenantid", TENANT_ID);
        params.add("apikey", API_KEY);
        params.add("field", "/profile/weight");

        JSONObject json = makeAPIRequest(params);

        assertThat(json, not(is(nullValue())));
        assertThat(json.getJSONObject("error").getString("@code"), is("301"));
    }

    @Test
    public void loadFieldNoField() {

        MultivaluedMap<String, String> params = new MultivaluedMapImpl();
        params.add("tenantid", TENANT_ID);
        params.add("apikey", API_KEY);
        params.add("itemid", "PROFILE_TEST_ITEM_1");

        JSONObject json = makeAPIRequest(params);

        assertThat(json, not(is(nullValue())));
        assertThat(json.getJSONObject("error").getString("@code"), is("335"));
    }

    @Test
    public void loadFieldItemTypeNotFound() {

        MultivaluedMap<String, String> params = new MultivaluedMapImpl();
        params.add("tenantid", TENANT_ID);
        params.add("apikey", API_KEY);
        params.add("itemid", "PROFILE_TEST_ITEM_1");
        params.add("itemtype", "I_DO_NOT_EXIST");
        params.add("field", "/profile/weight");

        JSONObject json = makeAPIRequest(params);

        assertThat(json, not(is(nullValue())));
        assertThat(json.getJSONObject("error").getString("@code"), is("912"));
    }

    @Test
    public void loadFieldIllegalXPath() {

        MultivaluedMap<String, String> params = new MultivaluedMapImpl();
        params.add("tenantid", TENANT_ID);
        params.add("apikey", API_KEY);
        params.add("itemid", "PROFILE_TEST_ITEM_1");
        params.add("field", "/profile/weight/");

        JSONObject json = makeAPIRequest(params);

        assertThat(json, not(is(nullValue())));
        assertThat(json.getJSONObject("error").getString("@code"), is("912"));

        params = new MultivaluedMapImpl();
        params.add("tenantid", TENANT_ID);
        params.add("apikey", API_KEY);
        params.add("itemid", "PROFILE_TEST_ITEM_1");
        params.add("field", "/profile/weight/[");

        json = makeAPIRequest(params);

        assertThat(json, not(is(nullValue())));
        assertThat(json.getJSONObject("error").getString("@code"), is("912"));

    }

    @Test
    public void loadFieldMultipleNodes() {

        MultivaluedMap<String, String> params = new MultivaluedMapImpl();
        params.add("tenantid", TENANT_ID);
        params.add("apikey", API_KEY);
        params.add("itemid", "PROFILE_TEST_ITEM_2");
        params.add("field", "/profile/name");

        JSONObject json = makeAPIRequest(params);

        assertThat(json, not(is(nullValue())));
        assertThat(json.getJSONObject("error").getString("@code"), is("337"));

    }

}
