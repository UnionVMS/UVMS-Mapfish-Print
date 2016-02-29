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

package org.mapfish.print.processor.map;

import com.google.common.base.Predicate;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.io.Files;
import jsr166y.ForkJoinPool;
import org.junit.Test;
import org.mapfish.print.AbstractMapfishSpringTest;
import org.mapfish.print.TestHttpClientFactory;
import org.mapfish.print.URIUtils;
import org.mapfish.print.config.Configuration;
import org.mapfish.print.config.ConfigurationFactory;
import org.mapfish.print.config.Template;
import org.mapfish.print.output.Values;
import org.mapfish.print.parser.MapfishParser;
import org.mapfish.print.test.util.ImageSimilarity;
import org.mapfish.print.wrapper.json.PJsonObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.mock.http.client.MockClientHttpRequest;
import org.springframework.test.annotation.DirtiesContext;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Test for native rotation on WMS layers.
 */
public class CreateMapProcessorScaleBBoxNativeRotationWms1_3_0Test extends AbstractMapfishSpringTest {
    public static final String BASE_DIR = "bbox_native_rotation_wms1_3_0_scale/";

    @Autowired
    private ConfigurationFactory configurationFactory;
    @Autowired
    private TestHttpClientFactory requestFactory;
    @Autowired
    private MapfishParser parser;
    
    @Autowired
    private ForkJoinPool forkJoinPool;

    @Test
    @DirtiesContext
    public void testExecute() throws Exception {
        final String host = "bbox_native_rotation_wms1_3_0_scale";
        requestFactory.registerHandler(
                new Predicate<URI>() {
                    @Override
                    public boolean apply(URI input) {
                        return (("" + input.getHost()).contains(host + ".wms")) || input.getAuthority().contains(host + ".wms");
                    }
                }, new TestHttpClientFactory.Handler() {
                    @Override
                    public MockClientHttpRequest handleRequest(URI uri, HttpMethod httpMethod) throws Exception {

                        final Multimap<String, String> uppercaseParams = HashMultimap.create();
                        for (Map.Entry<String, String> entry : URIUtils.getParameters(uri).entries()) {
                            uppercaseParams.put(entry.getKey().toUpperCase(), entry.getValue().toUpperCase());
                        }

                        assertTrue("SERVICE != WMS: " + uppercaseParams.get("WMS"), uppercaseParams.containsEntry("SERVICE", "WMS"));
                        assertTrue("FORMAT != IMAGE/TIFF: " + uppercaseParams.get("FORMAT"), uppercaseParams.containsEntry("FORMAT",
                                "IMAGE/TIFF"));
                        assertTrue("REQUEST != GETMAP: " + uppercaseParams.get("REQUEST"), uppercaseParams.containsEntry("REQUEST", "GETMAP"));
                        assertTrue("VERSION != 1.3.0: " + uppercaseParams.get("VERSION"), uppercaseParams.containsEntry("VERSION",
                                "1.3.0"));
                        assertTrue("LAYERS != TOPP:STATES: " + uppercaseParams.get("LAYERS"), uppercaseParams.containsEntry("LAYERS",
                                "TOPP:STATES"));
                        assertTrue("ANGLE != 90", uppercaseParams.containsEntry("ANGLE", "90"));
                        assertTrue("BBOX is missing", uppercaseParams.containsKey("BBOX"));
                        assertTrue("mapSize is not rotated (width)",
                                uppercaseParams.containsEntry("WIDTH", "780"));
                        assertTrue("mapSize is not rotated (height)",
                                uppercaseParams.containsEntry("HEIGHT", "330"));
                        
                        try {
                            byte[] bytes = Files.toByteArray(getFile("/map-data/states-native-rotation.tiff"));
                            return ok(uri, bytes, httpMethod);
                        } catch (AssertionError e) {
                            return error404(uri, httpMethod);
                        }
                    }
                }
        );
        final Configuration config = configurationFactory.getConfig(getFile(BASE_DIR + "config.yaml"));
        final Template template = config.getTemplate("main");
        PJsonObject requestData = loadJsonRequestData();
        Values values = new Values(requestData, template, this.parser, getTaskDirectory(), this.requestFactory, new File("."));
        forkJoinPool.invoke(template.getProcessorGraph().createTask(values));

        @SuppressWarnings("unchecked")
        List<URI> layerGraphics = (List<URI>) values.getObject("layerGraphics", List.class);
        assertEquals(1, layerGraphics.size());

        //Files.copy(new File(layerGraphics.get(0)), new File("/tmp/0_"+getClass().getSimpleName()+".tiff"));

        new ImageSimilarity(new File(layerGraphics.get(0)), 2)
                .assertSimilarity(getFile(BASE_DIR + "expectedSimpleImage.tiff"), 10);

    }

    private static PJsonObject loadJsonRequestData() throws IOException {
        return parseJSONObjectFromFile(CreateMapProcessorScaleBBoxNativeRotationWms1_3_0Test.class, BASE_DIR + "requestData.json");
    }
}
