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

package org.easyrec.plugin.profileduke.duke.datasource;

import no.priv.garshol.duke.Record;
import no.priv.garshol.duke.RecordImpl;
import no.priv.garshol.duke.RecordIterator;
import org.apache.log4j.Logger;
import org.easyrec.model.core.ItemVO;
import org.easyrec.service.core.ProfileService;

import java.util.*;

/**
 * User: fsalcher
 * Date: 06.02.13
 */
public class ProfileDBRecordIterator extends RecordIterator {

    private Iterator<ItemVO<Integer, Integer>> iterator;
    private ProfileService profileService;
    private Logger logger;


    public ProfileDBRecordIterator(List<ItemVO<Integer, Integer>> items,
                                   ProfileService profileService,
                                   Logger logger) {
        iterator = items.iterator();
        this.profileService = profileService;
        this.logger = logger;
    }

    @Override
    public boolean hasNext() {
        return iterator.hasNext();
    }

    @Override
    public Record next() {
        if (iterator.hasNext()) {
            ItemVO<Integer, Integer> itemVO = iterator.next();

            Map<String, Collection<String>> values = new HashMap<String, Collection<String>>();

            // TODO: fill the record

            String profile = profileService.getProfile(itemVO);





            //Record record = new ProfileDBRecord();
            Record record = new RecordImpl(values);
            return record;
        } else
            return null;
    }

    private Map<String, Collection<String>> parseXML(String xmlString){
        // TODO: adapt
        /*
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        InputSource is = new InputSource();
        is.setCharacterStream(new StringReader(xmlString));

        Document doc = dBuilder.parse(is);
        doc.getDocumentElement().normalize();

        ProfileDukeGenerator.logger.debug("File: " + idString + " Root element: " + doc.getDocumentElement().getNodeName());
        NodeList nList = doc.getElementsByTagName("profile");

        String subject = idString;
        Boolean literal = true;
        Boolean numeric = false;
        handler.statement(subject, "ID", idString, literal);
        handler.statement(subject, "profiletenant", Integer.toString(proTannent), literal);
        handler.statement(subject, "profileitem", Integer.toString(profItem), literal);
        handler.statement(subject, "profiletype", Integer.toString(proType), literal);

//            System.out.println("PROPERTY: " + props.get(0).getName() + " Literal?: " + literal.toString());
//            System.out.println("PROPERTY: " + props.get(2).getName() + " Literal?: " + literal.toString());
//            System.out.println("PROPERTY: " + props.get(5).getName() + " Literal?: " + literal.toString());

        for (int temp = 0; temp < nList.getLength(); temp++) {

            Node nNode = nList.item(temp);
            if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                Element eElement = (Element) nNode;


                for (int propIndex = 0; propIndex < props.size(); propIndex++) {
                    if (!(props.get(propIndex).getName().equals("ID") || props.get(propIndex).getName().equals("profiletenant") ||
                            props.get(propIndex).getName().equals("profileitem") || props.get(propIndex).getName().equals("profiletype"))) {
                        if (isNumeric(props.get(propIndex).getName())) {
//                                System.out.println("PROPERTY: " + props.get(propIndex).getName() + "   Literal?: " + numeric.toString());
                            handler.statement(subject, props.get(propIndex).getName(), getTagValue(props.get(propIndex).getName(), eElement), numeric);
                        } else {
//                                System.out.println("PROPERTY: " + props.get(propIndex).getName() + "   Literal?: " + literal.toString());
                            handler.statement(subject, props.get(propIndex).getName(), getTagValue(props.get(propIndex).getName(), eElement), literal);
                        }
                    }
                }
            }
        }
        */
        return null;
    }

}
