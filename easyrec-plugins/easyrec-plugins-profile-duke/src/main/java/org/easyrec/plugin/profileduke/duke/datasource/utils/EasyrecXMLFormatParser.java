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

package org.easyrec.plugin.profileduke.duke.datasource.utils;

import no.priv.garshol.duke.Property;
import no.priv.garshol.duke.StatementHandler;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.easyrec.model.core.ItemVO;
import org.easyrec.plugin.profileduke.ProfileDukeGenerator;
import org.easyrec.service.core.ProfileService;
import org.springframework.util.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Profile Based Matching Plugin. <p/> <p><b>Company:&nbsp;</b> SAT,
 * Research Studios Austria</p> <p><b>Copyright:&nbsp;</b> (c) 2012</p> <p><b>last modified:</b><br/> $Author$<br/>
 * $Date$<br/> $Revision$</p>
 *
 * @author Soheil Khosravipour
 * @author Fabian Salcher
 *
 */
public class EasyrecXMLFormatParser {

    private static Log logger = LogFactory.getLog(EasyrecXMLFormatParser.class);

    private StatementHandler handler;
    private static List<Property> props;
    private static List<ItemVO<Integer, Integer>> profileItems;
    private static ProfileService profileService;

    /**
     * Reads the NTriples file from the reader, pushing statements into
     * the handler.
     */
    public static void parse(StatementHandler handler, ProfileService profileSrv, List<ItemVO<Integer, Integer>> items, List<Property> properties) {
        profileItems = items;
        profileService = profileSrv;
        props = properties;
        new EasyrecXMLFormatParser(handler).parse();
    }

    private EasyrecXMLFormatParser(StatementHandler handler) {
        this.handler = handler;
    }

    private void parse() {

        //    Parse The XML Fileand add the records to handler    \\This is a directory to test
        ProfileDukeGenerator.logger.info("ProfileDuke Plugin. Profile Items Number: " + profileItems.size());

        for (ItemVO<Integer, Integer> item : profileItems) {
            //  String xmlProfile = profileService.getProfile(item);

            String xmlProfile = profileService.getProfile(item);
            if (xmlProfile == null)
                continue;

            int profileTenant = item.getTenant();
            int ProfileItem = item.getItem();
            int profileType = item.getType();
            ProfileDukeGenerator.logger.debug("Profile Item: " + ProfileItem + "profileTenant" +
                    profileTenant + "profileType" + profileType);

            ProfileDukeGenerator.logger.debug("XMLPROFILE: " + xmlProfile);

            xmlParser(xmlProfile, profileTenant, ProfileItem, profileType);
        }
    }

    /**
     * Takes an XML string with the profile and creates statements out
     * of the properties for the StatementHandler
     *
     * @param xmlString string with the profile XML
     * @param tenantId tenantId of the actual tenant
     * @param itemId itemId of the item with the profile
     * @param itemType itemType of the item with the profile
     */

    private void xmlParser(String xmlString, int tenantId, int itemId, int itemType) {

        String idString = Integer.toString(tenantId) + Integer.toString(itemId) + Integer.toString(itemType);

        try {

            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            InputSource is = new InputSource();
            is.setCharacterStream(new StringReader(xmlString));


            String subject = idString;
            handler.statement(subject, "ID", idString, true);
            handler.statement(subject, "ItemID", Integer.toString(itemId), true);

            Document doc = dBuilder.parse(is);
            doc.getDocumentElement().normalize();

            ProfileDukeGenerator.logger.debug("File: " + idString + " Root element: " +
                    doc.getDocumentElement().getNodeName());
            NodeList nList = doc.getElementsByTagName("profile");


            // create a map of props to check later if the properties from the profile are
            // also in the duke configuration
            HashMap<String, Property> propertyList = new HashMap<String, Property>(props.size());
            for (Property property: props)
                propertyList.put(property.getName(), property);

            for (int i = 0; i < nList.getLength(); i++) {

                Node node = nList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) node;

                    NodeList propertyNodes = element.getElementsByTagName("*");
                    for (int j = 0; j < propertyNodes.getLength(); j++) {
                        String propertyName = propertyNodes.item(j).getNodeName();
                        NodeList childNodes = propertyNodes.item(j).getChildNodes();
                        String propertyValue;
                        if (childNodes.getLength() > 0) {
                            propertyValue = childNodes.item(0).getNodeValue();
                        } else {
                            continue;
                        }
                        if (propertyList.containsKey(propertyName)) {
                            if (propertyList.get(propertyName).isConcatenateMultiValues())
                                propertyValue = StringUtils.replace(propertyValue, " ", "~");
                            handler.statement(subject, propertyName, propertyValue, true);
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.warn("An error occurred!", e);
        }
    }

}
