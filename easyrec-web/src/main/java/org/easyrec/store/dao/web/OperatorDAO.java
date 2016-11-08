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
package org.easyrec.store.dao.web;

import org.easyrec.model.core.web.Operator;
import org.easyrec.store.dao.BasicDAO;

import java.util.List;

/**
 * An Operator is a registered User/Company at the easyrec portal.
 * An Operator can have one ore more tenants assigned to.
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
 * @author <AUTHOR>
 */
public interface OperatorDAO extends BasicDAO {

    public static final String DEFAULT_TABLE_NAME = "operator";
    public static final String DEFAULT_TABLE_KEY = "operatorid";

    public final static String DEFAULT_OPERATORID_COLUMN_NAME = "OPERATORID";
    public final static String DEFAULT_PASSWORD_COLUMN_NAME = "PASSWORD";
    public final static String DEFAULT_FIRSTNAME_COLUMN_NAME = "FIRSTNAME";
    public final static String DEFAULT_LASTNAME_COLUMN_NAME = "LASTNAME";
    public final static String DEFAULT_EMAIL_COLUMN_NAME = "EMAIL";
    public final static String DEFAULT_PHONE_COLUMN_NAME = "PHONE";
    public final static String DEFAULT_COMPANY_COLUMN_NAME = "COMPANY";
    public final static String DEFAULT_ADDRESS_COLUMN_NAME = "ADDRESS";
    public final static String DEFAULT_APIKEY_COLUMN_NAME = "APIKEY";
    public final static String DEFAULT_IP_COLUMN_NAME = "IP";
    public final static String DEFAULT_ACTIVE_COLUMN_NAME = "ACTIVE";
    public final static String DEFAULT_CREATIONDATE_COLUMN_NAME = "CREATIONDATE";
    public final static String DEFAULT_ACCESSLEVEL_COLUMN_NAME = "ACCESSLEVEL";
    public final static String DEFAULT_LOGIN_COUNT_COLUMN_NAME = "LOGINCOUNT";
    public final static String DEFAULT_LAST_LOGIN_DATE_COLUMN_NAME = "LASTLOGIN";


    /**
     * This function checks if the operator id, submitted by the client,
     * is allowed to communicate with the recommender.
     *
     * @param operatorId
     *
     */
    @Override
    public boolean exists(String operatorId);


    /**
     * This function adds an operator.
     *
     * @param operatorId
     * @param password
     * @param firstName
     * @param lastName
     * @param email
     * @param phone
     * @param company
     * @param address
     * @param apiKey
     * @param ip
     */
    public void add(String operatorId, String password, String firstName, String lastName, String email, String phone,
                    String company, String address, String apiKey, String ip);


    /**
     * This function updates an operator for a given operator id
     * and return true if operation was successfull.
     *
     * @param operatorId
     * @param firstName
     * @param lastName
     * @param email
     * @param phone
     * @param company
     * @param address
     * @param apiKey
     * @param ip
     *
     */
    public boolean update(String operatorId, String firstName, String lastName, String email, String phone,
                          String company, String address, String apiKey, String ip);

    /**
     * This function returns an <code>Operator</code> object for a given operator id.
     *
     * @param operatorId the operator id of the wanted <code>Operator</code> object
     * @return the <code>Operator</code> object with the given operator id
     */
    public Operator get(String operatorId);

    /**
     * This function checks if the apiKey/TenantId combination is valid and
     * returns in case of success the core <code>TenantId</code> and <code>null</code> otherwise.
     *
     * @param apiKey   the operators apiKey.
     * @param tenantId the tenantId which is assigned to the operator.
     * @return the <code>coreTenantID</code> if the apiKey/tenantId combination exists
     *         and <code>null</code> otherwise
     */
    public Integer getTenantId(String apiKey, String tenantId);

    /**
     * Gets an operator for a given security token.
     * A token is genrated if an operator signs in and stays valid until signed out.
     *
     * @param token
     *
     */
    public Operator getOperatorFromToken(String token);


    /**
     * Sets a security token for the given operator.
     *
     * @param token
     * @param operatorId
     */
    public void setTokenForOperator(String token, String operatorId);


    /**
     * Removes the security token from the given operator.
     * This method may be called, when a session invalidates.
     *
     * @param operatorId
     */
    public void removeTokenFromOperator(String operatorId);


    /**
     * This function removes an operator with a given operator id
     * from the database.
     *
     * @param operatorId
     */
    public void remove(String operatorId);

    /**
     * This function return true if the given operator has associated
     * tenants.
     *
     * @param operatorId
     *
     */
    public boolean hasTenants(String operatorId);

    /**
     * This function activates an Operator.
     * An operator is activated if the link in the
     * confirmation email is clicked.
     * The operator id and the apiKey must be submitted.
     * If they match with an existing operator, he is
     * activated and the method return true.
     * False in any other case.
     *
     * @param operatorId
     * @param apiKey
     *
     */
    public boolean activate(String operatorId, String apiKey);

    /**
     * This function deactivates an Operator.
     *
     * @param operatorId
     *
     */
    public boolean deactivate(String operatorId);

    /**
     * This function returns an operator, increases the login count and
     * set the current date to the last login date
     * if the right operator/password combination is passed.
     *
     * @param operatorId
     * @param password
     *
     */
    public Operator signIn(String operatorId, String password);

    /**
     * @param operatorId
     * @param offset
     * @param limit
     * @return Operators for a given offest and count.
     */
    public List<Operator> getOperators(String operatorId, int offset, int limit);

    /**
     * This function updates the password for an operator
     *
     * @param operatorId
     * @param password
     */
    public void updatePassword(String operatorId, String password);


    /**
     * This function returns true if the given passwort matches to the password
     * of the given operatorid
     *
     * @param operatorId
     * @param password
     *
     */
    public boolean correctPassword(String operatorId, String password);

    /**
     * gets a Token based on the number of actions.
     *
     *
     */
    public int getToken();

    /**
     * 
     * 
     * @param operatorId
     *
     */
    public int count(String operatorId);

}
