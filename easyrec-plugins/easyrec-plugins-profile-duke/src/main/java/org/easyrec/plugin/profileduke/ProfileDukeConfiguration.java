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

package org.easyrec.plugin.profileduke;

import org.easyrec.plugin.configuration.PluginConfigurationValidator;
import org.easyrec.plugin.configuration.PluginParameter;
import org.easyrec.plugin.configuration.PluginParameterPropertyEditor;
import org.easyrec.plugin.generator.GeneratorConfiguration;
import org.springframework.beans.propertyeditors.CustomBooleanEditor;
import org.springframework.beans.propertyeditors.CustomNumberEditor;

/**
 * Configuration object for the demo plugin. <p/> This class contains all parameters that can be configured and are
 * needed for the plugin to work correctly. <p/> <p><b>Company:&nbsp;</b> SAT, Research Studios Austria</p>
 * <p><b>Copyright:&nbsp;</b> (c) 2007</p> <p><b>last modified:</b><br/> $Author$<br/> $Date$<br/> $Revision$</p>
 *
 * @author Soheil Khosravipour
 * @author Fabian Salcher
 */

@PluginConfigurationValidator(validatorClass = ProfileDukeConfigurationValidator.class)
public class ProfileDukeConfiguration extends GeneratorConfiguration {

    public static class IntegerPropertyEditor extends CustomNumberEditor {
        public IntegerPropertyEditor() throws IllegalArgumentException {
            super(Integer.class, true);
        }
    }

    public static class BooleanPropertyEditor extends CustomBooleanEditor {
        public BooleanPropertyEditor() throws IllegalArgumentException {
            super(false);
        }
    }

    // ------------------------------ FIELDS ------------------------------

    // each configuration value needs to be annotaed with @PluginParameter.
    // displayName      - is the string that will be displayed for the value in the administration tool.
    // shortDescription - will be the first paragrah of the description when the help button is pressed in the admin tool.
    // description      - is the second paragraph displayed in the admin tool.
    //
    // each config value should be initialized with a default value. when a new configuration object is created
    // all config values are initialized with the default values and the configuration is named "Default Configuration" in
    // the superclass (GeneratorConfiguration.)

    @PluginParameter(description = "ItemType of the items which are to be matched.",
            displayName = "Item Type:",
            shortDescription = "", displayOrder = 2)
    // TODO: check if item type exists
    private String itemType = "ITEM";

    @PluginParameter(description = "<b> Allowed Values: true / false </b> <br> " +
            "When this mode is active the items are assigned randomly to N blocks and " +
            "then the similarity is calculated only within these blocks. You can configure " +
            "the size of the blocks in the \"[Block Calculation] block size\" setting.",
            displayName = "[Block Calculation] enabled:",
            shortDescription = "", displayOrder = 10)
    @PluginParameterPropertyEditor(propertyEditorClass = BooleanPropertyEditor.class)
    private Boolean blockCalculationMode = false;

    @PluginParameter(description = "Defines the size (in # of items) of the blocks used in Block Calculation Mode.",
            displayName = "[Block Calculation] block size:",
            shortDescription = "", displayOrder = 11)
    @PluginParameterPropertyEditor(propertyEditorClass =  IntegerPropertyEditor.class)
    private Integer blockCalculationBlockSize = 10;

    @PluginParameter(description = "Duke Configuration",
            displayName = "Duke Configuration",
            shortDescription = "", displayOrder = 12,
            asTextArea = true)
    private String dukeConfiguration =
            "<duke>\n" +
            "<param name=\"database-implementation\" value=\"in-memory\"/>\n" +
            "\n" +
            "\n" +
            "     <object class=\"org.easyrec.plugin.profileduke.duke.comparators.YearComparator\"\n" +
            "            name=\"YearComparator\">\n" +
            "        <param name=\"diffThreshold\" value=\"10\"/>\n" +
            "    </object>\n" +
            "\n" +
            "    <schema>\n" +
            "        <threshold>0.5</threshold>\n" +
            "\n" +
            "        <property>\n" +
            "            <name>title</name>\n" +
            "            <comparator>no.priv.garshol.duke.comparators.Levenshtein</comparator>\n" +
            "            <low>0.5</low>\n" +
            "            <high>0.9</high>\n" +
            "        </property>\n" +
            "\n" +
            "        <property>\n" +
            "            <name>year</name>\n" +
            "            <comparator>YearComparator</comparator>\n" +
            "            <low>0.5</low>\n" +
            "            <high>0.9</high>\n" +
            "        </property>\n" +
            "\n" +
            "        <property>\n" +
            "            <name>director</name>\n" +
            "            <comparator>no.priv.garshol.duke.comparators.PersonNameComparator</comparator>\n" +
            "            <low>0.5</low>\n" +
            "            <high>0.9</high>\n" +
            "        </property>\n" +
            "\n" +
            "        <property type=\"concatenateMultiValues\">\n" +
            "            <name>actor</name>\n" +
            "            <comparator>no.priv.garshol.duke.comparators.DiceCoefficientComparator</comparator>\n" +
            "            <low>0.5</low>\n" +
            "            <high>0.9</high>\n" +
            "        </property>\n" +
            "\n" +
            "        <property type=\"concatenateMultiValues\">\n" +
            "            <name>category</name>\n" +
            "            <comparator>no.priv.garshol.duke.comparators.DiceCoefficientComparator</comparator>\n" +
            "            <low>0.5</low>\n" +
            "            <high>0.9</high>\n" +
            "        </property>\n" +
            "    </schema>\n" +
            "</duke>";

    private String viewType = "SYSTEM";

    // --------------------- GETTER / SETTER METHODS ---------------------

    public String getItemType() {
        return itemType;
    }

    public void setItemType(String itemType) {
        this.itemType = itemType;
    }

    public String getViewType() {
        return viewType;
    }

    public void setViewType(final String viewType) {
        this.viewType = viewType;
    }

    public Boolean getBlockCalculationMode() {
        return blockCalculationMode;
    }

    public void setBlockCalculationMode(Boolean blockCalculationMode) {
        this.blockCalculationMode = blockCalculationMode;
    }

    public Integer getBlockCalculationBlockSize() {
        return blockCalculationBlockSize;
    }

    public void setBlockCalculationBlockSize(Integer blockCalculationBlockSize) {
        this.blockCalculationBlockSize = blockCalculationBlockSize;
    }

    public String getDukeConfiguration() {
        return dukeConfiguration;
    }

    public void setDukeConfiguration(String dukeConfiguration) {
        this.dukeConfiguration = dukeConfiguration;
    }
}
