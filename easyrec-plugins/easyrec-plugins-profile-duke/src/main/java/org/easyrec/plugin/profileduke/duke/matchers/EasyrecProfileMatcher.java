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

package org.easyrec.plugin.profileduke.duke.matchers;

import no.priv.garshol.duke.Record;
import no.priv.garshol.duke.matchers.AbstractMatchListener;
import org.easyrec.model.core.ItemAssocVO;
import org.easyrec.model.core.ItemVO;
import org.easyrec.plugin.profileduke.ProfileDukeGenerator;
import org.easyrec.plugin.profileduke.store.dao.ProfileSimilarityItemAssocDAO;

import java.util.*;

/**
 * ProfileDuke Plugin. <p/> <p><b>Company:&nbsp;</b> SAT,
 * Research Studios Austria</p> <p><b>Copyright:&nbsp;</b> (c) 2012</p> <p><b>last modified:</b><br/> $Author$<br/>
 * $Date$<br/> $Revision$</p>
 *
 * @author Soheil Khosravipour
 * @author Fabian Salcher
 */

public class EasyrecProfileMatcher extends AbstractMatchListener {
    private int numberOfCreatedAssociations = 0;
    private int numberOfMatches = 0;
    private int numberOfMaybeMatches = 0;
    private int records = 0;
    private int nonmatches; // only counted in record linkage mode
    private boolean createAssociationsFromMaybeMatches;
    private boolean linkage; // means there's a separate indexing step


    private List<ItemVO<Integer, Integer>> items;
    private static Integer confTanantId;
    private Integer sourceType;
    private Integer viewType;
    private static Integer assocType;
    private ArrayList<ItemAssocVO<Integer, Integer>> itemAssocArrayList;
    private ProfileSimilarityItemAssocDAO itemAssocDAO;

    private ProfileDukeGenerator profileDukeGenerator;

    private HashMap<Integer, HashMap<Integer, Double>> userToUsers =
            new HashMap<Integer, HashMap<Integer, Double>>();

    public void setItemAssocHashMap(HashMap<Integer, HashMap<Integer, Double>> userToUsers) {
        this.userToUsers = userToUsers;
    }

    public HashMap<Integer, HashMap<Integer, Double>> getItemAssocHashMap() {
        return userToUsers;
    }

    public void setItemAssocDAO(ProfileSimilarityItemAssocDAO itemAssocDAO) {
        this.itemAssocDAO = itemAssocDAO;
    }

    public void setConfTanantId(Integer confTanantId) {
        this.confTanantId = confTanantId;
    }

    public void setAssocType(Integer assocType) {
        this.assocType = assocType;
    }

    public void setSourceType(Integer sourceType) {
        this.sourceType = sourceType;
    }

    public void setViewType(Integer viewType) {
        this.viewType = viewType;
    }

    public void setItemAssocArrayList(ArrayList<ItemAssocVO<Integer, Integer>> itemAssocArrayList) {
        this.itemAssocArrayList = itemAssocArrayList;
    }

    public EasyrecProfileMatcher(boolean createAssociationsFromMaybeMatches,
                                 boolean linkage,
                                 ProfileDukeGenerator profileDukeGenerator) {

        this.createAssociationsFromMaybeMatches = createAssociationsFromMaybeMatches;
        this.linkage = linkage;
        this.profileDukeGenerator = profileDukeGenerator;
    }

    public int getMatchCount() {
        return numberOfMatches;
    }

    public void batchReady(int size) {
        if (linkage)
            records += size; // no endRecord() call in linkage mode
        ProfileDukeGenerator.logger.info("Records: " + records);
    }

    public void matches(Record r1, Record r2, double confidence) {
        numberOfMatches++;
        createAssociationBetweenRecords(r1, r2, confidence);
    }

    public void matchesPerhaps(Record r1, Record r2, double confidence) {
        numberOfMaybeMatches++;
        if (createAssociationsFromMaybeMatches)
            createAssociationBetweenRecords(r1, r2, confidence);
    }

    public void endRecord() {
        records++;
    }

    public void endProcessing() {
        ProfileDukeGenerator.logger.info("");
        ProfileDukeGenerator.logger.info("Total records: " + records);
        ProfileDukeGenerator.logger.info("Total matches: " + numberOfMatches);
        ProfileDukeGenerator.logger.info("Maybe matches: " + numberOfMaybeMatches);
        if (nonmatches > 0) // FIXME: this ain't right. we should know the mode
            ProfileDukeGenerator.logger.info("Total non-matches: " + nonmatches);

        profileDukeGenerator.setNumberOfAssociationsCreated(numberOfCreatedAssociations);
    }

    public void noMatchFor(Record record) {
        nonmatches++;
    }

    /**
     * Creates an association and saves it in the DB.
     *
     * @param r1 first record which represents an item
     * @param r2 second record which represents an item
     * @param confidence associationValue
     */

    public void createAssociationBetweenRecords(Record r1, Record r2, double confidence) {

        Date execution = new Date();

        ItemAssocVO<Integer, Integer> itemAssoc = new ItemAssocVO<Integer, Integer>(
                confTanantId,
                new ItemVO(profileDukeGenerator.getTenantId(),
                        r1.getValue("ItemID"),
                        profileDukeGenerator.getItemType()),
                assocType, confidence,
                new ItemVO(profileDukeGenerator.getTenantId(),
                        r2.getValue("ItemID"),
                        profileDukeGenerator.getItemType()),
                sourceType, "ProfileDuke Plugin", viewType, null, execution);

        numberOfCreatedAssociations++;
        itemAssocDAO.insertOrUpdateItemAssoc(itemAssoc);
    }

    public static String toString(Record r) {
        StringBuffer buf = new StringBuffer();
        for (String p : r.getProperties()) {
            Collection<String> vs = r.getValues(p);
            if (vs == null)
                continue;

            buf.append(p + ": ");
            for (String v : vs)
                buf.append("'" + v + "', ");
        }

        //buf.append(";;; " + r);
        return buf.toString();
    }
}


