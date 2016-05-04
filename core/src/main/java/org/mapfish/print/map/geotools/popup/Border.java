package org.mapfish.print.map.geotools.popup;

import org.mapfish.print.parser.HasDefaultValue;

/**
 * Created by georgige on 5/4/2016.
 */
public final class Border {
    public String color;//something like #a3a3a3

    @HasDefaultValue
    public int width = 8; //in pixels
}