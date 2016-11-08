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

package org.easyrec.plugin.profileduke.duke.comparators;

import no.priv.garshol.duke.Comparator;

/**
 * This comparator returns 1 if the numbers are the same and scales down linearly to 0 when
 * the difference equals the diffThreshold defined in the configuration.
 *
 * @author Fabian Salcher
 */


public class YearComparator implements Comparator {
    private int diffThreshold;

    public boolean isTokenized() {
        return false;
    }

    public void setDiffThreshold(int diffThreshold) {
        this.diffThreshold = diffThreshold;
    }

    public double compare(String v1, String v2) {
        int y1, y2;
        try {
            y1 = Integer.parseInt(v1);
            y2 = Integer.parseInt(v2);
        } catch (NumberFormatException e) {
            return 0.5;
        }

        final int diff = Math.abs(y1 - y2);
        double result;
        if (diff < diffThreshold)
            result =  1 - ((double)diff / (double)diffThreshold);
        else
            result = 0;

        return result;
    }
}