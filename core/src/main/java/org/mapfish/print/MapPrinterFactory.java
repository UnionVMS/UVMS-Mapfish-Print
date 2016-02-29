/*
 * Copyright (C) 2014  Camptocamp
 *
 * This file is part of MapFish Print
 *
 * MapFish Print is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MapFish Print is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with MapFish Print.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.mapfish.print;

import org.mapfish.print.servlet.NoSuchAppException;

import java.util.Set;

/**
 * Interface for a class that creates MapPrinters.
 * @author jesseeichar on 3/18/14.
 */
public interface MapPrinterFactory {
    /**
     * Creates the appropriate map printer.
     *
     * @param app an identifier that controls which configuration to use.
     */
    MapPrinter create(String app) throws NoSuchAppException;

    /**
     * Return the set of app ids that are available.
     */
    Set<String> getAppIds();
}
