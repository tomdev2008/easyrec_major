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
 *
 * @author Fabian Salcher
 */


public class DebugComparator implements Comparator {

    public boolean isTokenized() {
        return false;
    }

    public double compare(String v1, String v2) {

        System.out.println("comparing: \"" + v1 + "\" vs \"" + v2 + "\"");
        return 0.5;
    }
}