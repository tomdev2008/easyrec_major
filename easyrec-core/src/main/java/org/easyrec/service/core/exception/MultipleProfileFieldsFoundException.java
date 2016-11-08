/**Copyright 2012 Research Studios Austria Forschungsgesellschaft mBH
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
package org.easyrec.service.core.exception;

/**
 * Exception thrown at the ProfileService if an XPath expression points
 * unexpectedly to more than one nodes.
 *
 * @author Fabian Salcher
 */
public class MultipleProfileFieldsFoundException extends Exception {
    private static final long serialVersionUID = 7078318277243304886L;

    public MultipleProfileFieldsFoundException(String message) {
        super(message);
    }
}
