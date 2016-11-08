/**Copyright 2015 Research Studios Austria Forschungsgesellschaft mBH
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
package org.easyrec.plugin.arm.model;

import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 *
 * @author szavrel
 */
public class ARMConfigurationValidator implements Validator {

    @Override
    public boolean supports(Class<?> type) {
        return type.equals(ARMConfiguration.class);
    }

    @Override
    public void validate(Object target, Errors errors) {
        ARMConfiguration configuration = (ARMConfiguration) target;

        if (configuration.getConfidencePrcnt() != null && configuration.getConfidencePrcnt() < 0.0 && configuration.getConfidencePrcnt() >100.0) {
            errors.rejectValue("confidencePrcnt", "error.outOfRange", "The confidence must be a valid percentage, thus between 0.0 and 100.0!");
        }

        if (configuration.getSupportPrcnt() != null && configuration.getSupportPrcnt() < 0.0 && configuration.getSupportPrcnt() > 100.0) {
            errors.rejectValue("supportPrcnt", "error.outOfRange", "The support percentage must be between 0.0 and 100.0!");
        }

        if (configuration.getSupportMinAbs() != null && configuration.getSupportMinAbs() < 1) {
            errors.rejectValue("supportMinAbs", "error.outOfRange", "The minimum absolute support must be a value greater than 1!");
        }

        if (configuration.getMaxRulesPerItem() != null && configuration.getMaxRulesPerItem() < 1) {
            errors.rejectValue("maxRulesPerItem", "error.outOfRange", "Valid values for maximum rules per item must be greater than 1!");
        }

    }

}
