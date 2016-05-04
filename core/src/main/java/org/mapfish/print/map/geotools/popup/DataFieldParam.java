package org.mapfish.print.map.geotools.popup;

import com.vividsolutions.jts.util.Assert;
import org.apache.commons.lang.StringUtils;
import org.mapfish.print.map.AbstractLayerParams;
import org.mapfish.print.parser.Requires;

/**
 * Created by georgige on 3/24/2016.
 */
public class DataFieldParam  {

    public String propName;

    @Requires("propName")
    public String displayName;


    public void postConstruct() {
        Assert.isTrue(StringUtils.isNotBlank(this.propName), "propName must not be blank");
    }

    /*
    *    "popupProperties" : {
                        "showAttrNames": true,
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
                            }
                        ]
                    }*/

}
