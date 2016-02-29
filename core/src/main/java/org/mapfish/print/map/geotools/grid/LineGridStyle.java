package org.mapfish.print.map.geotools.grid;

import org.geotools.styling.LineSymbolizer;
import org.geotools.styling.Style;
import org.geotools.styling.StyleBuilder;
import org.mapfish.print.map.style.json.ColorParser;

import java.awt.Color;

/**
 * Creates the Named LineGridStyle.
 *
 * @author Jesse on 6/29/2015.
 */
public final class LineGridStyle {
    private LineGridStyle() {
        // do nothing
    }

    /**
     * Gets the line grid style.
     */
    static Style get(final GridParam params) {
        return createGridStyle(params, new StyleBuilder());
    }

    private static Style createGridStyle(final GridParam params, final StyleBuilder builder) {
        final LineSymbolizer lineSymbolizer = builder.createLineSymbolizer();
        final Color strokeColor = ColorParser.toColor(params.gridColor);

        //CSOFF:MagicNumber
        lineSymbolizer.setStroke(builder.createStroke(strokeColor, 1, new float[]{4f, 4f}));
        //CSON:MagicNumber

        return builder.createStyle(lineSymbolizer);
    }

}
