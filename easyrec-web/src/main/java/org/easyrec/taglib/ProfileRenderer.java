/*
 * Copyright 2011 Research Studios Austria Forschungsgesellschaft mBH
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
package org.easyrec.taglib;

import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.owasp.esapi.ESAPI;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.Tag;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;

/**
 * JSP tag to convert XML profiles into HTML code which can be viewed within a browser.
 * <p/>
 * <p><b>Company:&nbsp;</b>
 * SAT, Research Studios Austria</p>
 * <p/>
 * <p><b>Copyright:&nbsp;</b>
 * (c) 2007</p>
 * <p/>
 * <p><b>last modified:</b><br/>
 * $Author: dmann $<br/>
 * $Date: 2011-04-20 10:45:07 +0200 (Mi, 20 Apr 2011) $<br/>
 * $Revision: 18166 $</p>
 *
 * @author David Mann
 */
public class ProfileRenderer implements Tag {

    private static final Log logger = LogFactory.getLog(ProfileRenderer.class);

    private PageContext pageContext;
    private Tag parent;
    private String profile = "";
    private ObjectMapper mapper;
    private ObjectWriter writer;

    public void setProfile(String profile) {
        this.profile = profile;
    }

    public ProfileRenderer() {
        super();
        this.mapper = new ObjectMapper();
        this.writer = mapper.writer(new DefaultPrettyPrinter());
    }

    @Override
    public int doStartTag() throws JspTagException {
        return SKIP_BODY;
    }

    @Override
    public int doEndTag() throws JspTagException {
        try {
            String tagId = "profile" + Long.toString(System.currentTimeMillis());
            StringBuilder fullHTML = new StringBuilder();
            fullHTML.append("<div class=\"profile\">");
            String profileContent = getSourceViewJSON(profile); //since JSON is now the primary format, we try this first
            if (profileContent == null) {
                profileContent = getListViewHTML(profile); 
            }
            if (profileContent == null) {
                profileContent = getSourceViewHTML(profile); 
            }
            fullHTML.append("   <div id=\"profileHTML-").append(tagId).append("\">");
            fullHTML.append(profileContent);
            fullHTML.append("   </div>");
            fullHTML.append("</div>");

            pageContext.getOut().write(fullHTML.toString());
        } catch (java.io.IOException e) {
            throw new JspTagException("IO Error: " + e.getMessage());
        }
        return EVAL_PAGE;
    }

    /**
     * This function sanitizes the profileXML so the user can see the profile XML in his web browser.
     *
     * @param profileXML the ProfileXML you want to convert to HTML
     * @return the HTML representation of the given XML file
     */
    public String getSourceViewHTML(String profileXML) {
        String returnString = "<pre class=\"prettyprint\">";
        returnString += formatXml(profileXML).replaceAll("<", "&lt;").replaceAll(">", "&gt;");
        returnString += "</pre>";

        return returnString;
    }
    
    public String getSourceViewJSON(String profileJSON) {
        
        String returnString = null;
        try {
            returnString = "<pre class=\"prettyprint\">";
            returnString += writer.writeValueAsString(mapper.readValue(profileJSON, Object.class));
            returnString += "</pre>";
        } catch (Exception e) {
            returnString = null;
        }
        return returnString;
    }

    /**
     * This function converts a XML profile to a HTML List representation of the given XML string
     *
     * @param profileXML the XML Profile you want to convert
     * @return the HTML representation of the given XML object
     */
    public String getListViewHTML(String profileXML) {

        if (profileXML == null || profileXML.equals("")) {
            return "";
        }

        StringBuilder profileHTML = new StringBuilder();
        NodeList childNodes = getNodeListFromProfileXML(profileXML);
        if (childNodes != null)
            convertNodeListToHTML(childNodes, profileHTML);
        else
            return null;
            //return "<p>The profile contains no valid XML.<br />Click \"View Profile Source\" to display the content of the profile.</p>";
        return profileHTML.toString();
    }

    /**
     * This function appends HTML text based on the given nodeList to the given StringBuilder
     * The function will build the HTML recursively until the whole tree is converted to HTML.
     *
     * @param nodeList    the nodeList which you want to convert to HTML
     * @param profileHTML the StringBuilder which is used to write the HTML
     */
    private void convertNodeListToHTML(NodeList nodeList, StringBuilder profileHTML) {
        if (nodeList == null) {
            profileHTML.append("<p>could not parse the XML profile of this item. </p>");
            return;
        }

        int elementCounter = countElements(nodeList);
        if (elementCounter == 0)
            return;

        profileHTML.append("<dl>");
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node item = nodeList.item(i);
            if (item.getNodeType() == Node.ELEMENT_NODE) {
                profileHTML.append("<dt class=\"profileView\">" + ESAPI.encoder().encodeForHTML(item.getNodeName()) + ":</dt>");

                profileHTML.append("<dd class=\"profileView\">");
                convertNodeListToHTML(item.getChildNodes(), profileHTML);
                String textNodeValue = ESAPI.encoder().encodeForHTML(item.getFirstChild().getNodeValue());
                if (textNodeValue != null)
                    profileHTML.append(textNodeValue);

                profileHTML.append("</dd>");
            }
        }
        profileHTML.append("</dl>");
    }

    /**
     * This function will count all elements in the given NodeList.
     *
     * @param nodeList the nodeList you want to count
     * @return the number of ELEMENT_NODE in the first hierarchy of the given nodeList
     */
    private int countElements(NodeList nodeList) {
        int elementCounter = 0;
        for (int i = 0; i < nodeList.getLength(); i++) {
            if (nodeList.item(i).getNodeType() == Node.ELEMENT_NODE)
                elementCounter++;
        }
        return elementCounter;
    }

    /**
     * This function will return a NodeList Object which holds all XML Nodes from the given
     * XML String in the same hierarchy as in the XML String. It is an exact representation
     * of the given profileXML as Java Object.
     *
     * @param profileXML the string you want to convert to a XML NodeList
     * @return the converted NodeList
     */
    private NodeList getNodeListFromProfileXML(String profileXML) {
        try {
            Document doc = generateXmlDocument(profileXML);
            doc.getDocumentElement().normalize();
            return doc.getDocumentElement().getChildNodes();
        } catch (SAXException e) {
            logger.debug("An error occurred!", e);
            return null;
        }
    }

    /**
     * This function generates a XML document of the given XML String
     *
     * @param profileXML
     * @return The XmlDocument of the given XML string
     */
    private Document generateXmlDocument(String profileXML)
            throws SAXException {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            InputSource is = new InputSource();
            is.setCharacterStream(new StringReader(profileXML));
            return db.parse(is);
        } catch (ParserConfigurationException e) {
            logger.warn("An error occurred!", e);
        } catch (IOException e) {
            logger.warn("An error occurred!", e);
            e.printStackTrace();
        }
        return null;
    }


    /**
     * This function will format the XML profile with intends and new lines.
     *
     * @param xmlToFormat the xml String you want to format
     * @return the formated version of teh given XML string
     */
    private String formatXml(String xmlToFormat) {
        try {
            final Document document = generateXmlDocument(xmlToFormat);

            OutputFormat format = new OutputFormat(document);
            format.setLineWidth(65);
            format.setIndenting(true);
            format.setIndent(2);
            format.setOmitXMLDeclaration(true);
            Writer out = new StringWriter();
            XMLSerializer serializer = new XMLSerializer(out, format);
            serializer.serialize(document);

            return out.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (SAXException e) {
            return xmlToFormat;
        }
    }


    @Override
    public void release() {
    }


    @Override
    public void setPageContext(final PageContext pageContext) {
        this.pageContext = pageContext;
    }


    @Override
    public void setParent(final Tag parent) {
        this.parent = parent;
    }

    @Override
    public Tag getParent() {
        return parent;
    }


}

