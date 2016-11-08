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


import no.priv.garshol.duke.*;
import no.priv.garshol.duke.datasources.ColumnarDataSource;
import no.priv.garshol.duke.utils.DefaultRecordIterator;
import no.priv.garshol.duke.utils.NTriplesParser;
import org.easyrec.model.core.ItemVO;
import org.easyrec.plugin.profileduke.duke.datasource.utils.EasyrecXMLFormatParser;
import org.easyrec.service.core.ProfileService;

import java.io.*;
import java.util.*;

/**
 *
 * @author Soheil Khosravipour
 * @author Fabian Salcher
 */
public class EasyrecXMLFormatDataSource extends ColumnarDataSource {

    private Collection<String> types;

    private List<ItemVO<Integer, Integer>> profileItems;
    private ProfileService profileService;
    private List<Property> props;

    public void setProps(List<Property> props) {
        this.props = props;
    }

    public void setItems(List<ItemVO<Integer, Integer>> items) {
            this.profileItems = items;
    }

    public void setProfileService(ProfileService profileService)
    {
          this.profileService = profileService;
    }


    public EasyrecXMLFormatDataSource() {
        super();
        this.types = new HashSet();
    }


    public RecordIterator getRecords() {

            RecordBuilder builder = new RecordBuilder(types);
            EasyrecXMLFormatParser.parse(builder, profileService, profileItems, props);
            builder.filterByTypes();
            Iterator it = builder.getRecords().values().iterator();
            return new DefaultRecordIterator(it);

    }

    protected String getSourceName() {
        return "EasyrecXMLFormat";
    }

    // common utility method for adding a statement to a record
    private void addStatement(RecordImpl record,
                              String subject,
                              String property,
                              String object) {
        Collection<Column> cols = columns.get(property);
        if (cols == null) {
            if (property.equals(RDF_TYPE) && !types.isEmpty())
                addValue(record, subject, property, object);
            return;
        }

        for (Column col : cols) {
            String cleaned = object;
            if (col.getCleaner() != null)
                cleaned = col.getCleaner().clean(object);
            if (cleaned != null && !cleaned.equals(""))
                addValue(record, subject, col.getProperty(), cleaned);
        }
    }

    private void addValue(RecordImpl record, String subject,
                          String property, String object) {
        if (record.isEmpty())
            for (Column idcol : columns.get("?uri"))
                record.addValue(idcol.getProperty(), subject);

        record.addValue(property, object);
    }

    private boolean filterbytype(Record record) {
        if (types.isEmpty()) // there is no filtering
            return true;

        boolean found = false;
        for (String value : record.getValues(RDF_TYPE))
            if (types.contains(value))
                return true;
        return false;
    }

    // ----- non-incremental handler

    private static final String RDF_TYPE =
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type";

    class RecordBuilder implements StatementHandler {

        private List<ItemVO<Integer, Integer>> items;
        private Integer confTanantId;
        private Integer sourceType;
        private Integer viewType;
        private Integer assocType;
        private Integer itemType;
        private Map<String, RecordImpl> records;
        private Collection<String> types;

        public RecordBuilder(Collection<String> types) {
            this.records = new HashMap();
            this.types = types;
        }

        //    TODO:CAN BE DELETED   ? DO WE WANT TO USE FILTERING?
        public void filterByTypes() {
            // this is fairly ugly. if types has values we add an extra property
            // RDF_TYPE to records during build, then filter out records of the
            // wrong types here. finally, we strip away the RDF_TYPE property here.

            if (types.isEmpty())
                return;

            for (String uri : new ArrayList<String>(records.keySet())) {
                RecordImpl r = records.get(uri);
                if (!filterbytype(r))
                    records.remove(uri);
                else
                    r.remove(RDF_TYPE);
            }
        }

        public Map<String, RecordImpl> getRecords() {
            return records;
        }

        // FIXME: refactor this so that we share code with addStatement()
        public void statement(String subject, String property, String object,
                              boolean literal) {
            RecordImpl record = records.get(subject);
            if (record == null) {
                record = new RecordImpl();
                records.put(subject, record);
            }
            addStatement(record, subject, property, object);
        }
    }


//    TODO:CAN BE DELETED
    // --- incremental mode

    class IncrementalRecordIterator extends RecordIterator
            implements StatementHandler {

        private BufferedReader reader;
        private NTriplesParser parser;
        private Record nextrecord;
        private String subject;
        private String property;
        private String object;

        public IncrementalRecordIterator(Reader input) {
            this.reader = new BufferedReader(input);
            this.parser = new NTriplesParser(this);
            parseNextLine();
            findNextRecord();
        }

        public boolean hasNext() {
            return nextrecord != null;
        }

        public Record next() {
            Record record = nextrecord;
            findNextRecord();
            return record;
        }

        // find the next record that's of an acceptable type
        private void findNextRecord() {
            do {
                nextrecord = parseRecord();
            } while (nextrecord != null && !filterbytype(nextrecord));
        }

        private void parseNextLine() {
            // blanking out, so we can see whether we receive anything in
            // each line we parse.
            subject = null;

            String nextline;
            while (subject == null) {
                try {
                    nextline = reader.readLine();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                if (nextline == null)
                    return; // we're finished, and there is no next record

                parser.parseLine(nextline);
                // we've now received a callback setting 'subject' if there
                // was a statement in this line.
            }
        }

        // this finds the next record in the data stream
        private Record parseRecord() {
            RecordImpl record = new RecordImpl();
            String current = subject;

            // we've stored the first statement about the next resource, so we
            // need to process that before we move on to read anything

            while (current != null && current.equals(subject)) {
                addStatement(record, subject, property, object);
                parseNextLine();
                // we have now received a callback to statement() with the
                // next statement
            }

            // ok, subject is now either null (we're finished), or different from
            // current, because we've started on the next resource
            if (current == null)
                return null;
            return record;
        }

        public void statement(String subject, String property, String object,
                              boolean literal) {
            this.subject = subject;
            this.property = property;
            this.object = object;
        }
    }
}
