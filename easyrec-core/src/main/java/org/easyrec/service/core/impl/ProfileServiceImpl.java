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
package org.easyrec.service.core.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.easyrec.model.core.ItemVO;
import org.easyrec.model.core.web.Item;
import org.easyrec.service.core.ProfileService;
import org.easyrec.service.core.exception.FieldNotFoundException;
import org.easyrec.service.core.exception.MultipleProfileFieldsFoundException;
import org.easyrec.service.domain.TypeMappingService;
import org.easyrec.store.dao.IDMappingDAO;
import org.easyrec.store.dao.core.ProfileDAO;
import org.w3c.dom.*;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.validation.SchemaFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author szavrel
 */
public class ProfileServiceImpl implements ProfileService {

    private ProfileDAO profileDAO;
    private IDMappingDAO idMappingDAO;
    private TypeMappingService typeMappingService;
    private SchemaFactory sf;
    private DocumentBuilderFactory dbf;

    private Transformer trans;

    // logging
    private final Log logger = LogFactory.getLog(this.getClass());

    public ProfileServiceImpl(ProfileDAO profileDAO, IDMappingDAO idMappingDAO, TypeMappingService typeMappingService) {
        this(profileDAO, null, idMappingDAO, typeMappingService);
    }


    public ProfileServiceImpl(ProfileDAO profileDAO,
                              String docBuilderFactory, IDMappingDAO idMappingDAO, TypeMappingService typeMappingService) {

        this.profileDAO = profileDAO;
        this.idMappingDAO = idMappingDAO;
        this.typeMappingService = typeMappingService;
        if (docBuilderFactory != null)
            System.setProperty("javax.xml.parsers.DocumentBuilderFactory", docBuilderFactory);
        dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        if (logger.isDebugEnabled()) {
            logger.debug("DocumentBuilderFactory: " + dbf.getClass().getName());
            ClassLoader cl = Thread.currentThread().getContextClassLoader().getSystemClassLoader();
            URL url = cl.getResource("org/apache/xerces/jaxp/DocumentBuilderFactoryImpl.class");
            logger.debug("Parser loaded from: " + url);
        }

        TransformerFactory tf = TransformerFactory.newInstance();
        try {
            trans = tf.newTransformer();
            trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        } catch (Exception e) {
            logger.warn("An error occurred!", e);
        }
    }

    public boolean storeProfile(Integer tenantId, Integer itemId, String itemTypeId, String profileXML) {
        return profileDAO.storeProfile(tenantId, itemId,
                typeMappingService.getIdOfItemType(tenantId, itemTypeId), profileXML) != 0;
    }

    /**
     *  Used
     * @param tenantId
     * @param itemId
     * @param itemType
     * @param profileXML
     *
     */
    @Override
    public boolean storeProfile(Integer tenantId, String itemId, String itemType, String profileXML) {
        return profileDAO.storeProfile(tenantId, idMappingDAO.lookup(itemId),
                typeMappingService.getIdOfItemType(tenantId, itemType), profileXML) != 0;
    }

    /**
     * Used
     * @param tenantId
     * @param itemId
     * @param itemType
     *
     */
    @Override
    public boolean deleteProfile(Integer tenantId, String itemId, String itemType) {
        return profileDAO.deleteProfile(tenantId, idMappingDAO.lookupOnly(itemId),
                typeMappingService.getIdOfItemType(tenantId, itemType));
    }


    public String getProfile(Integer tenantId, Integer itemId, Integer itemTypeId) {
        return profileDAO.getProfile(tenantId, itemId, itemTypeId);
    }

    public String getProfile(Integer tenantId, Integer itemId, String itemTypeId) {
        return profileDAO.getProfile(tenantId, itemId, typeMappingService.getIdOfItemType(tenantId, itemTypeId));
    }

    @Override
    public String getProfile(Item item) {
        return getProfile(item.getTenantId(), item.getItemId(), item.getItemType());
    }

    @Override
    public String getProfile(ItemVO<Integer, Integer> item) {
        return getProfile(item.getTenant(), item.getItem(), item.getType());
    }

    /**
     * Used
     * @param tenantId
     * @param itemId
     * @param itemTypeId
     *
     */
    @Override
    public String getProfile(Integer tenantId, String itemId, String itemTypeId) {
        Integer mappedItemId = idMappingDAO.lookupOnly(itemId);
        return getProfile(tenantId, mappedItemId, itemTypeId);
    }

    /**
     * Used
     * @param tenantId
     * @param itemId
     * @param itemType
     * @param dimensionXPath
     *
     * @throws Exception
     */
    @Override
    public Set<String> loadProfileField(Integer tenantId, String itemId, String itemType,
                                        String dimensionXPath)
            throws Exception {

        Set<String> result = new HashSet<>();

        int itemIntID = idMappingDAO.lookupOnly(itemId);

        XPathFactory xpf = XPathFactory.newInstance();

        Document doc = getProfileXMLDocument(tenantId, itemIntID, itemType);

        XPath xp = xpf.newXPath();
        NodeList nodeList = (NodeList) xp.evaluate(dimensionXPath, doc, XPathConstants.NODESET);

        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            result.add(node.getTextContent());
        }

        return result;
    }

    /**
     * Used
     * @param tenantId
     * @param itemId
     * @param itemTypeId
     * @param dimensionXPath
     * @param value
     *
     */
    @Override
    public synchronized boolean storeProfileField(Integer tenantId, String itemId, String itemTypeId,
                                                  String dimensionXPath, String value) throws Exception {

        int itemIntID = idMappingDAO.lookup(itemId);

        XPathFactory xpf = XPathFactory.newInstance();

        // load and parse the profile
        Document doc = getProfileXMLDocument(tenantId, itemIntID, itemTypeId);

        // follow the XPath from bottom to top until you find the first existing path element
        XPath xp = xpf.newXPath();
        String tmpPath = dimensionXPath;
        NodeList nodeList = (NodeList) xp.evaluate(tmpPath, doc, XPathConstants.NODESET);
        if (nodeList.getLength() > 1)
            throw new MultipleProfileFieldsFoundException(nodeList.getLength() + " nodes found.");

        Node node = null;
        if (nodeList.getLength() == 1)
            nodeList.item(0).setTextContent(value);
        else {
            while (node == null) {
                tmpPath = dimensionXPath.substring(0, tmpPath.lastIndexOf("/"));
                if ("".equals(tmpPath))
                    tmpPath = "/";
                node = (Node) xp.evaluate(tmpPath, doc, XPathConstants.NODE);
            }
            insertElement(doc, node,
                    dimensionXPath.substring(tmpPath.length()), value);
        }

        StringWriter writer = new StringWriter();
        Result result = new StreamResult(writer);
        trans.transform(new DOMSource(doc), result);
        writer.close();
        String xml = writer.toString();
        logger.debug(xml);
        storeProfile(tenantId, itemId, itemTypeId, xml);

        return true;
    }

/**
 * Used
 * @param tenantId
 * @param itemId
 * @param itemType
 * @param deleteXPath
 * @throws Exception
 *
 */
    @Override
    public boolean deleteProfileField(Integer tenantId, String itemId, String itemType, String deleteXPath)
            throws Exception {

        XPathFactory xpf = XPathFactory.newInstance();

        // load and parse the profile
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(new InputSource(new StringReader(
                getProfile(tenantId, itemId, itemType))));

        // check if the element exists
        XPath xp = xpf.newXPath();
        NodeList nodeList = (NodeList) xp.evaluate(deleteXPath, doc, XPathConstants.NODESET);

        if (nodeList.getLength() == 0)
            throw new FieldNotFoundException("Field does not exist in this profile!");
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            node.getParentNode().removeChild(node);
        }

        StringWriter writer = new StringWriter();
        Result result = new StreamResult(writer);
        trans.transform(new DOMSource(doc), result);
        writer.close();
        String xml = writer.toString();
        logger.debug(xml);
        storeProfile(tenantId, itemId, itemType, xml);

        return true;
    }

    @Override
    public List<ItemVO<Integer, Integer>> getItemsByItemType(Integer tenantId, String itemType, int count) {
        return profileDAO.getItemsByItemType(tenantId, typeMappingService.getIdOfItemType(tenantId, itemType), count);
    }

    /**
     * Inserts a new element and value into an XML Document at the position given in xPathExpression
     * relative to the Node given in startNode.
     *
     * @param doc             the Document in which the Element is inserted
     * @param startNode       the Node in the Document used as start point for the XPath Expression
     * @param xPathExpression the XPath from the startNode to the new Element
     * @param value           the value of the new Element
     */
    private Node insertElement(Document doc, Node startNode, String xPathExpression, String value) {

        if (!"".equals(xPathExpression)) {
            String[] xPathTokens = xPathExpression.split("/");
            for (String tag : xPathTokens) {
                if (!"".equals(tag)) {
                    Element el = doc.createElement(tag);
                    startNode.appendChild(el);
                    startNode = startNode.getLastChild();
                }
            }
            if (value != null) startNode.setTextContent(value);
        }
        return startNode;
    }

    private Document getProfileXMLDocument(Integer tenantId, Integer itemId, String itemTypeId)
            throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilder db = dbf.newDocumentBuilder();
        String profile = getProfile(tenantId, itemId, itemTypeId);
        if (profile == null || profile.equals(""))
            return db.newDocument();
        else
            return db.parse(new InputSource(new StringReader(profile)));
    }

}
