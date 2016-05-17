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

package org.mapfish.print.map.geotools;

import com.google.common.collect.Maps;
import com.vividsolutions.jts.geom.Geometry;
import org.apache.commons.lang.StringEscapeUtils;
import org.eclipse.emf.ecore.util.EContentsEList;
import org.geotools.data.FeatureSource;
import org.geotools.data.collection.CollectionFeatureSource;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.simple.SimpleFeatureImpl;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.map.Layer;
import org.geotools.map.MapContent;
import org.geotools.renderer.lite.RendererUtilities;
import org.geotools.renderer.lite.StreamingRenderer;
import org.mapfish.print.ExceptionUtils;
import org.mapfish.print.attribute.map.MapBounds;
import org.mapfish.print.attribute.map.MapfishMapContext;
import org.mapfish.print.config.Template;
import org.mapfish.print.http.MfClientHttpRequestFactory;
import org.mapfish.print.map.AbstractLayerParams;
import org.mapfish.print.map.geotools.popup.DataFieldParam;
import org.mapfish.print.map.geotools.popup.PopupParam;
import org.mapfish.print.map.geotools.popup.PopupStyle;
import org.mapfish.print.parser.HasDefaultValue;
import org.mapfish.print.parser.OneOf;
import org.opengis.feature.Feature;
import org.opengis.feature.GeometryAttribute;
import org.opengis.feature.Property;
import org.opengis.feature.simple.SimpleFeature;

import java.awt.*;
import java.awt.geom.*;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.concurrent.ExecutorService;
import javax.annotation.Nonnull;

/**
 * <p>Parses GeoJSON from the request data.</p>
 * <p>Type: <code>geojson</code></p>
 *
 * @author Jesse on 3/26/14.
 */
public final class GeoJsonLayer extends AbstractFeatureSourceLayer {

    /**
     * Constructor.
     *
     * @param executorService       the thread pool for doing the rendering.
     * @param featureSourceSupplier a function that creates the feature source.  This will only be called once.
     * @param styleSupplier         a function that creates the style for styling the features. This will only be called once.
     * @param renderAsSvg           is the layer rendered as SVG?
     * @param params the parameters for this layer
     */
    public GeoJsonLayer(final ExecutorService executorService,
                        final FeatureSourceSupplier featureSourceSupplier,
                        final StyleSupplier<FeatureSource> styleSupplier,
                        final boolean renderAsSvg,
                        final AbstractLayerParams params) {
        super(executorService, featureSourceSupplier, styleSupplier, renderAsSvg, params);
    }

        @Override
        public void render(final Graphics2D graphics2D,
                   final MfClientHttpRequestFactory clientHttpRequestFactory,
                   final MapfishMapContext transformer,
                   final boolean isFirstLayer) {
        super.render(graphics2D, clientHttpRequestFactory, transformer, isFirstLayer);

        Rectangle paintArea = new Rectangle(transformer.getMapSize());
        MapBounds bounds = transformer.getBounds();

        MapfishMapContext layerTransformer = transformer;
        if (transformer.getRotation() != 0.0 && !this.supportsNativeRotation()) {
            // if a rotation is set and the rotation can not be handled natively
            // by the layer, we have to adjust the bounds and map size
            paintArea = new Rectangle(transformer.getRotatedMapSize());
            bounds = transformer.getRotatedBounds();
            graphics2D.setTransform(transformer.getTransform());
            Dimension mapSize = new Dimension(paintArea.width, paintArea.height);
            layerTransformer = new MapfishMapContext(transformer, bounds, mapSize, transformer.getRotation(), transformer.getDPI(),
                    transformer.getRequestorDPI(), transformer.isForceLongitudeFirst(), transformer.isDpiSensitiveStyle());
        }


        MapContent content = new MapContent();
        try {
            java.util.List<? extends Layer> layers = getLayers(clientHttpRequestFactory, layerTransformer, isFirstLayer);
//            applyTransparency(layers);

            content.addLayers(layers);

            StreamingRenderer renderer = new StreamingRenderer();

            RenderingHints hints = new RenderingHints(Collections.<RenderingHints.Key, Object>emptyMap());
            hints.add(new RenderingHints(RenderingHints.KEY_ALPHA_INTERPOLATION,
                    RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY));
            hints.add(new RenderingHints(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON));
            hints.add(new RenderingHints(RenderingHints.KEY_COLOR_RENDERING,
                    RenderingHints.VALUE_COLOR_RENDER_QUALITY));
            hints.add(new RenderingHints(RenderingHints.KEY_DITHERING,
                    RenderingHints.VALUE_DITHER_ENABLE));
            hints.add(new RenderingHints(RenderingHints.KEY_FRACTIONALMETRICS,
                    RenderingHints.VALUE_FRACTIONALMETRICS_ON));
            hints.add(new RenderingHints(RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_BICUBIC));
            hints.add(new RenderingHints(RenderingHints.KEY_RENDERING,
                    RenderingHints.VALUE_RENDER_QUALITY));
            hints.add(new RenderingHints(RenderingHints.KEY_STROKE_CONTROL,
                    RenderingHints.VALUE_STROKE_PURE));
            hints.add(new RenderingHints(RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_ON));

            graphics2D.addRenderingHints(hints);
            renderer.setJava2DHints(hints);
            Map<String, Object> renderHints = Maps.newHashMap();
            if (transformer.isForceLongitudeFirst() != null) {
                renderHints.put(StreamingRenderer.FORCE_EPSG_AXIS_ORDER_KEY, transformer.isForceLongitudeFirst());
            }
            renderer.setRendererHints(renderHints);

            renderer.setMapContent(content);

            final ReferencedEnvelope mapArea = bounds.toReferencedEnvelope(paintArea, transformer.getDPI());

            renderer.paint(graphics2D, paintArea, mapArea);

            //            renderer.setThreadPool(this.executorService);
            FeatureIterator<? extends Feature> featuresIter= this.featureSource.getFeatures().features();
            AffineTransform pointTransformer = RendererUtilities.worldToScreenTransform(mapArea, paintArea);

            FontMetrics fontMetrics = graphics2D.getFontMetrics();

            while (featuresIter.hasNext()) {
                Feature feature = featuresIter.next();
                Property showPopupProperty = feature.getProperty("overlayHidden");
                Property popupX = feature.getProperty("popupX");
                Property popupY = feature.getProperty("popupY");

                if (showPopupProperty.getValue() instanceof Boolean && !((Boolean)showPopupProperty.getValue())) {
                    GeoJsonParam geoJsonParam = (GeoJsonParam)this.params;
                    List<String> popupLines = new ArrayList<>();
                    StringBuilder buffer = new StringBuilder();

                    /*
                    GeometryAttribute geomAttr = (GeometryAttribute)feature.getProperty("geometry");
                    System.out.println(geomAttr.getBounds());

                    Point2D featurePoint = new Point2D.Double(geomAttr.getBounds().getMinX(), geomAttr.getBounds().getMinY()); */
                    Point2D featurePoint = new Point2D.Double((Double)popupX.getValue(), (Double)popupY.getValue());
                    Point2D featurePointInPixelPlain = pointTransformer.transform(featurePoint, null);

                    int lineWidth = geoJsonParam.popupProperties.popupStyle.width; //in pixels
                    int lineHeight = graphics2D.getFontMetrics().getHeight(); //in pixels
                    double startXPixel = featurePointInPixelPlain.getX();
                    double startYPixel = featurePointInPixelPlain.getY();

                    //draw the first horizontal line of the popup --------------
                    /*graphics2D.setColor(Color.BLACK);
                    graphics2D.draw(new Line2D.Double(startXPixel, startYPixel, startXPixel + lineWidth, startYPixel));*/

                    Map<String, String> propsToDisplay = new LinkedHashMap<>();

                    for(DataFieldParam dataField:geoJsonParam.popupProperties.dataFields) {
                        if (feature.getProperty(dataField.propName) != null && feature.getProperty(dataField.propName).getValue() != null) {
                            propsToDisplay.put(dataField.displayName, feature.getProperty(dataField.propName).getValue().toString());
                        }
                    }

                    RoundRectangle2D.Double background = new RoundRectangle2D.Double(startXPixel,
                            startYPixel,
                            lineWidth,
                            lineHeight*propsToDisplay.size(),
                            geoJsonParam.popupProperties.popupStyle.radius,
                            geoJsonParam.popupProperties.popupStyle.radius);

                    Rectangle2D blueStripe = new Rectangle2D.Double(startXPixel,
                            startYPixel,
                            geoJsonParam.popupProperties.popupStyle.border.width,
                            lineHeight*propsToDisplay.size());
                    Area blueStripeArea = new Area(blueStripe);
                    blueStripeArea.intersect(new Area(background));

                    graphics2D.setColor(Color.white);
                    graphics2D.fill(background);
                    graphics2D.setColor(Color.decode(geoJsonParam.popupProperties.popupStyle.border.color));
                    graphics2D.fill(blueStripeArea);

                    graphics2D.setColor(Color.black);

                    lineWidth -=  blueStripeArea.getBounds().width; //remove the blue stripe on the left
                    startXPixel += blueStripeArea.getBounds().width;
                    Font font = graphics2D.getFont();

                    for(Map.Entry<String, String> prop: propsToDisplay.entrySet()) {
                        if (geoJsonParam.popupProperties.showAttrNames) {
                            font.deriveFont(Font.BOLD);
                            graphics2D.setFont(font.deriveFont(Font.BOLD));
                            fontMetrics = graphics2D.getFontMetrics();

                            buffer.append(prop.getKey()).append(": ");

                            while (fontMetrics.stringWidth(buffer.toString()) > lineWidth) {
                                buffer.deleteCharAt(buffer.length()-1);
                            }
                            graphics2D.drawString(StringEscapeUtils.escapeJava(buffer.toString()), (float) startXPixel, (float) startYPixel + 11);
                        }

                        int propLabelWidth = fontMetrics.stringWidth(buffer.toString());
                        graphics2D.setFont(font.deriveFont(Font.PLAIN));
                        fontMetrics = graphics2D.getFontMetrics();

                        if (propLabelWidth < lineWidth) {
                            buffer = new StringBuilder(prop.getValue());

                            while (fontMetrics.stringWidth(buffer.toString()) + propLabelWidth > lineWidth) {
                                buffer.deleteCharAt(buffer.length()-1);
                            }

                            graphics2D.drawString(buffer.toString(), (float) startXPixel + propLabelWidth, (float) startYPixel + 11);
                        }
                        buffer = new StringBuilder();
                        startYPixel += lineHeight;

                    }


                }
            }

        } catch (Exception e) {
            throw ExceptionUtils.getRuntimeException(e);
        } finally {
            content.dispose();
        }
    }

    /**
      * <p>Renders GeoJSON layers.</p>
      * <p>Type: <code>geojson</code></p>
      * [[examples=json_styling,datasource_multiple_maps,printwms_tyger_ny_EPSG_900913]]
      */
    public static final class Plugin extends AbstractFeatureSourceLayerPlugin<GeoJsonParam> {

        private static final String TYPE = "geojson";
        private static final String COMPATIBILITY_TYPE = "vector";

        /**
         * Constructor.
         */
        public Plugin() {
            super(TYPE, COMPATIBILITY_TYPE);
        }

        @Override
        public GeoJsonParam createParameter() {
            return new GeoJsonParam();
        }

        @Nonnull
        @Override
        public GeoJsonLayer parse(@Nonnull final Template template,
                                  @Nonnull final GeoJsonParam param) throws IOException {
            return new GeoJsonLayer(
                    this.forkJoinPool,
                    createFeatureSourceSupplier(template, param.geoJson),
                    createStyleFunction(template, param.style),
                    template.getConfiguration().renderAsSvg(param.renderAsSvg),
                    param);
        }

        private FeatureSourceSupplier createFeatureSourceSupplier(final Template template,
                                                                    final String geoJsonString) {
            return new FeatureSourceSupplier() {
                @Nonnull
                @Override
                public FeatureSource load(@Nonnull final MfClientHttpRequestFactory requestFactory,
                                          @Nonnull final MapfishMapContext mapContext) {
                    final FeaturesParser parser = new FeaturesParser(requestFactory, mapContext.isForceLongitudeFirst());
                    SimpleFeatureCollection featureCollection;
                    try {
                        featureCollection = parser.autoTreat(template, geoJsonString);
                        return new CollectionFeatureSource(featureCollection);
                    } catch (IOException e) {
                        throw ExceptionUtils.getRuntimeException(e);
                    }
                }
            };
        }
    }

    /**
     * The parameters for creating a layer that renders GeoJSON formatted data.
     */
    public static class GeoJsonParam extends AbstractVectorLayerParam {
        /**
         * A geojson formatted string or url to the geoJson or the raw GeoJSON data.
         * <p></p>
         * The url can be a file url, however if it is it must be relative to the configuration directory.
         */
        public String geoJson;

        public PopupParam popupProperties;
    }
}
