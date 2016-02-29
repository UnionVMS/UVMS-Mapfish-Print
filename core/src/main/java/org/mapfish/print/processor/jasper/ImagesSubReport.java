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

package org.mapfish.print.processor.jasper;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.io.Files;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.Renderable;
import net.sf.jasperreports.engine.design.JRDesignBand;
import net.sf.jasperreports.engine.design.JRDesignElement;
import net.sf.jasperreports.engine.design.JRDesignExpression;
import net.sf.jasperreports.engine.design.JRDesignImage;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.type.HorizontalImageAlignEnum;
import net.sf.jasperreports.engine.type.ScaleImageEnum;
import net.sf.jasperreports.engine.type.WhenNoDataTypeEnum;

import java.awt.Dimension;
import java.io.File;
import java.net.URI;
import java.util.List;

/**
 * Creates a Jasper containing overlaid images, which is supposed to
 * be embedded into an another report as sub-report.
 *
 * @author tsauerwein
 */
public class ImagesSubReport {

    private final JasperDesign reportDesign;

    /**
     * Constructor.
     *
     * @param graphics A list of graphic files.
     * @param size The size of the report in pixel.
     * @param dpi the dpi of the report
     */
    public ImagesSubReport(final List<URI> graphics, final Dimension size, final double dpi) {
        this.reportDesign = createReport(graphics, size, dpi);
    }

    private JasperDesign createReport(final List<URI> graphics, final Dimension size, final double dpi) {
        final JasperDesign design = new JasperDesign();
        design.setName("map");

        // report size and margins
        design.setPageWidth(size.width);
        design.setPageHeight(size.height);
        design.setColumnWidth(size.width);
        design.setColumnSpacing(0);
        design.setLeftMargin(0);
        design.setRightMargin(0);
        design.setTopMargin(0);
        design.setBottomMargin(0);

        JRDesignBand band = new JRDesignBand();
        band.setHeight(size.height);

        // add layer graphics to report
        addGraphics(graphics, band, size, design);

        // note that the images are added to the "NoData" band, this ensures
        // that they are displayed even if no data connection is passed to the
        // sub-report
        design.setNoData(band);
        design.setWhenNoDataType(WhenNoDataTypeEnum.NO_DATA_SECTION);
        design.setProperty(Renderable.PROPERTY_IMAGE_DPI, String.valueOf(Math.round(dpi)));
        return design;
    }

    private void addGraphics(final List<URI> graphics, final JRDesignBand band,
            final Dimension size, final JasperDesign design) {
        for (URI graphicFile : graphics) {
            String imageExpression;

            final String fileName = new File(graphicFile).getAbsolutePath().replace('\\', '/');
            if (Files.getFileExtension(fileName).equals("svg")) {
                imageExpression = "net.sf.jasperreports.renderers.BatikRenderer.getInstance(new java.io.File(\""
                        + fileName + "\"))";
            } else {
                imageExpression = "\"" + fileName + "\"";
            }

            band.addElement(getImage(imageExpression, size, design));
        }
    }

    private JRDesignElement getImage(final String imageExpression, final Dimension mapSize,
            final JasperDesign design) {
        final JRDesignImage image = new JRDesignImage(design);

        image.setHorizontalImageAlign(HorizontalImageAlignEnum.CENTER);
        image.setX(0);
        image.setY(0);
        image.setWidth(mapSize.width);
        image.setHeight(mapSize.height);
        image.setScaleImage(ScaleImageEnum.RETAIN_SHAPE);

        final JRDesignExpression expression = new JRDesignExpression();
        expression.setText(imageExpression);
        image.setExpression(expression);

        return image;
    }

    /**
     * Compiles the report into a <code>*.jasper</code> file.
     *
     * @param compiledReportFile The destination file.
     * @throws JRException
     */
    public final void compile(final File compiledReportFile) throws JRException {
        JasperCompileManager.compileReportToFile(this.reportDesign, compiledReportFile.getAbsolutePath());
    }

    @VisibleForTesting
    protected final JasperDesign getReportDesign() {
        return this.reportDesign;
    }
}
