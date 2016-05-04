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

package org.mapfish.print.map.geotools.popup;

import com.vividsolutions.jts.util.Assert;
import org.geotools.referencing.CRS;
import org.geotools.referencing.operation.transform.IdentityTransform;
import org.mapfish.print.map.AbstractLayerParams;
import org.mapfish.print.map.geotools.GeoJsonLayer;
import org.mapfish.print.parser.HasDefaultValue;
import org.mapfish.print.parser.OneOf;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;

/**
 * Parameters relevant to creating Grid layers.
 * CSOFF: VisibilityModifier
 */
public final class PopupParam{


    @HasDefaultValue
    public Boolean showAttrNames = false;

    @OneOf("dataFields")
    public DataFieldParam[] dataFields;

    public PopupStyle popupStyle;


    public void postConstruct() {
//        Assert.isTrue(this.showAttrNames && this.dataFields != null && this.dataFields.length > 0,
//                GeoJsonLayer.class.getSimpleName() + ".dataFields is missing when showAttrNames is set to 'true'");

    }




    /*"showAttrNames": true,
            "dataFields": [
    {
        "propName": "movementType",
            "displayName": "Type"
    },
    {
        "propName": "reportedSpeed",
            "displayName": "Speed"
    },
    {
        "propName": "connectionId",
            "displayName": "Connection ID"
    },
    {
        "propName": "overlayId",
            "displayName": "Overlay ID"
    }]*/


}
