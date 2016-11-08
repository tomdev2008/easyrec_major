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

import no.priv.garshol.duke.ConfigLoader;
import no.priv.garshol.duke.Property;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.List;

/**
 * @author Fabian Salcher
 */
public class ProfileDukeConfigurationValidator implements Validator {


    public boolean supports(final Class clazz) {
        return clazz.equals(ProfileDukeConfiguration.class);
    }

    public void validate(final Object target, final Errors errors) {
        ProfileDukeConfiguration configuration = (ProfileDukeConfiguration) target;

        /**
         * validates the duke XML configuration by loading the configuration with
         * the duke ConfigLoader and checking for exceptions
         */
        String dukeConfiguration = configuration.getDukeConfiguration();
        try {
            List<Property> propertyList = ConfigLoader.loadFromString(dukeConfiguration).getProperties();
            for (Property property : propertyList) {
                if (property.getName().equals("ID")) {
                    errors.rejectValue("dukeConfiguration", "error.readError",
                            "An error occurred while loading the configuration: \"ID\" is not allowed" +
                                    " as a property name.");
                }
                if (property.getName().equals("ItemID")) {
                    errors.rejectValue("dukeConfiguration", "error.readError",
                            "An error occurred while loading the configuration: \"ItemID\" is not allowed" +
                                    " as a property name.");
                }
            }
        } catch (IOException e) {
            errors.rejectValue("dukeConfiguration", "error.readError",
                    "An error occurred while loading the configuration: " + e.getMessage());
        } catch (SAXException e) {
            errors.rejectValue("dukeConfiguration", "error.xmlError",
                    "Configuration is not a valid XML document: " + e.getMessage());
        } catch (RuntimeException e) {
            errors.rejectValue("dukeConfiguration", "error.dukeConfigurationError",
                    "Configuration is not a valid duke XML configuration: " + e.getMessage());
        }
    }
}
