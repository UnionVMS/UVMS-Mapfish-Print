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

package org.mapfish.print.attribute.map;

import com.google.common.collect.Lists;

import org.geotools.referencing.CRS;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.mapfish.print.AbstractMapfishSpringTest;
import org.mapfish.print.TestHttpClientFactory;
import org.mapfish.print.attribute.ReflectiveAttribute;
import org.mapfish.print.attribute.map.ZoomToFeatures.ZoomType;
import org.mapfish.print.config.Configuration;
import org.mapfish.print.config.ConfigurationFactory;
import org.mapfish.print.config.Template;
import org.mapfish.print.output.Values;
import org.mapfish.print.parser.MapfishParser;
import org.mapfish.print.processor.map.CreateMapProcessorFlexibleScaleBBoxGeoJsonTest;
import org.mapfish.print.wrapper.json.PJsonObject;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.util.List;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mapfish.print.processor.map.CreateMapProcessorFlexibleScaleBBoxGeoJsonTest.BASE_DIR;
import static org.mapfish.print.processor.map.CreateMapProcessorFlexibleScaleBBoxGeoJsonTest.loadJsonRequestData;

/**
 * @author Jesse on 4/3/14.
 */
public class MapAttributeTest extends AbstractMapfishSpringTest {

    @Autowired
    private ConfigurationFactory configurationFactory;
    @Autowired
    private MapfishParser parser;
    @Autowired
    private TestHttpClientFactory httpRequestFactory;

    @Before
    public void setUp() throws Exception {
        this.configurationFactory.setDoValidation(false);

    }

    @SuppressWarnings("unchecked")
    @Test(expected = IllegalArgumentException.class)
    public void testMaxDpi() throws Exception {
        final File configFile = getFile(CreateMapProcessorFlexibleScaleBBoxGeoJsonTest.class, BASE_DIR + "config.yaml");
        final Configuration config = configurationFactory.getConfig(configFile);
        final Template template = config.getTemplate("main");

        final PJsonObject pJsonObject = loadJsonRequestData();
        final PJsonObject attributesJson = pJsonObject.getJSONObject("attributes");
        final JSONObject mapDef = attributesJson.getJSONObject("mapDef").getInternalObj();
        mapDef.remove("dpi");
        mapDef.accumulate("dpi", 1000);

        final ReflectiveAttribute<MapAttribute.MapAttributeValues> mapAttribute = (ReflectiveAttribute<MapAttribute
                        .MapAttributeValues>) template.getAttributes().get("mapDef");

        final MapAttribute.MapAttributeValues value = mapAttribute.createValue(template);
        parser.parse(true, attributesJson.getJSONObject("mapDef"), value);
    }

    @Test
    public void testDpiSuggestions() throws Exception {
        final File configFile = getFile(MapAttributeTest.class, "map_attributes/config-json-dpi.yaml");
        final Configuration config = configurationFactory.getConfig(configFile);
        final Template template = config.getTemplate("main");

        final MapAttribute mapAttribute = (MapAttribute) template.getAttributes().get("mapDef");
        List<Throwable> errors = Lists.newArrayList();
        mapAttribute.validate(errors, config);

        assertFalse(errors.isEmpty());
    }

    @Test
    public void testAttributesFromJson() throws Exception {
        final File configFile = getFile(MapAttributeTest.class, "map_attributes/config-json.yaml");
        final Configuration config = configurationFactory.getConfig(configFile);
        final Template template = config.getTemplate("main");
        final PJsonObject pJsonObject = parseJSONObjectFromFile(MapAttributeTest.class, "map_attributes/requestData-json.json");
        final Values values = new Values(pJsonObject, template, new MapfishParser(), getTaskDirectory(), this.httpRequestFactory,
                new File("."));
        final MapAttribute.MapAttributeValues value = values.getObject("mapDef", MapAttribute.MapAttributeValues.class);

        assertEquals(90.0, value.getDpi(), 0.1);
        assertNotNull(value.getLayers());
        Object proj = value.getMapBounds().getProjection();
        CoordinateReferenceSystem expected = CRS.decode("CRS:84");
        assertTrue(CRS.equalsIgnoreMetadata(expected, proj));
        assertEquals(0.0, value.rotation, 0.1);
        assertArrayEquals(new double[]{90, 200, 300, 400}, value.getDpiSuggestions(), 0.1);
    }


    @Test
    public void testAttributesFromYaml() throws Exception {
        final File configFile = getFile(MapAttributeTest.class, "map_attributes/config-yaml.yaml");
        final Configuration config = configurationFactory.getConfig(configFile);
        final Template template = config.getTemplate("main");
        final PJsonObject pJsonObject = parseJSONObjectFromFile(MapAttributeTest.class, "map_attributes/requestData-yaml.json");
        final Values values = new Values(pJsonObject, template, new MapfishParser(), getTaskDirectory(), this.httpRequestFactory, new File("."));
        final MapAttribute.MapAttributeValues value = values.getObject("mapDef", MapAttribute.MapAttributeValues.class);

        assertEquals(80.0, value.getDpi(), 0.1);
        assertNotNull(value.getLayers());

        Object proj = value.getMapBounds().getProjection();
        CoordinateReferenceSystem expected = CRS.decode("CRS:84");
        assertTrue(CRS.equalsIgnoreMetadata(expected, proj));
        assertEquals(10.0, value.rotation, 0.1);
        assertArrayEquals(new double[]{90, 200, 300, 400}, value.getDpiSuggestions(), 0.1);
    }

    @Test
    public void testAttributesFromBoth() throws Exception {
        final File configFile = getFile(MapAttributeTest.class, "map_attributes/config-yaml.yaml");
        final Configuration config = configurationFactory.getConfig(configFile);
        final Template template = config.getTemplate("main");
        final PJsonObject pJsonObject = parseJSONObjectFromFile(MapAttributeTest.class, "map_attributes/requestData-json.json");
        final Values values = new Values(pJsonObject, template, new MapfishParser(), getTaskDirectory(), this.httpRequestFactory, new File("."));
        final MapAttribute.MapAttributeValues value = values.getObject("mapDef", MapAttribute.MapAttributeValues.class);

        assertEquals(90.0, value.getDpi(), 0.1);
        assertNotNull(value.getLayers());

        Object proj = value.getMapBounds().getProjection();
        CoordinateReferenceSystem expected = CRS.decode("CRS:84");
        assertTrue(CRS.equalsIgnoreMetadata(expected, proj));
        assertEquals(0.0, value.rotation, 0.1);
        assertArrayEquals(new double[]{90, 200, 300, 400}, value.getDpiSuggestions(), 0.1);
    }

    @Test
    public void testZoomToFeatures() throws Exception {
        final File configFile = getFile(MapAttributeTest.class, "map_attributes/config-zoomTo.yaml");
        final Configuration config = configurationFactory.getConfig(configFile);
        final Template template = config.getTemplate("main");
        final PJsonObject pJsonObject = parseJSONObjectFromFile(MapAttributeTest.class, "map_attributes/requestData-zoomTo.json");
        final Values values = new Values(pJsonObject, template, new MapfishParser(), getTaskDirectory(), this.httpRequestFactory,
                new File("."));
        final MapAttribute.MapAttributeValues value = values.getObject("mapDef", MapAttribute.MapAttributeValues.class);

        assertEquals(null, value.zoomToFeatures);
    }

    @Test
    public void testZoomToFeaturesCenter() throws Exception {
        final File configFile = getFile(MapAttributeTest.class, "map_attributes/config-zoomToCenter.yaml");
        final Configuration config = configurationFactory.getConfig(configFile);
        final Template template = config.getTemplate("main");
        final PJsonObject pJsonObject = parseJSONObjectFromFile(MapAttributeTest.class, "map_attributes/requestData-zoomToCenter.json");
        final Values values = new Values(pJsonObject, template, new MapfishParser(), getTaskDirectory(), this.httpRequestFactory,
                new File("."));
        final MapAttribute.MapAttributeValues value = values.getObject("mapDef", MapAttribute.MapAttributeValues.class);

        assertNotNull(value.zoomToFeatures);
        assertEquals(ZoomType.CENTER, value.zoomToFeatures.zoomType);
    }
}
