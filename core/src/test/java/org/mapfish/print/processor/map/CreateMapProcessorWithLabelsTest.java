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
import org.json.JSONException;
import org.junit.Test;
import org.mapfish.print.AbstractMapfishSpringTest;
import org.mapfish.print.TestHttpClientFactory;
import org.mapfish.print.URIUtils;
import org.mapfish.print.config.Configuration;
import org.mapfish.print.config.ConfigurationFactory;
import org.mapfish.print.config.Template;
import org.mapfish.print.http.MfClientHttpRequestFactoryImpl;
import org.mapfish.print.output.Values;
import org.mapfish.print.parser.MapfishParser;
import org.mapfish.print.test.util.ImageSimilarity;
import org.mapfish.print.wrapper.json.PJsonObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.client.MockClientHttpRequest;
import org.springframework.mock.http.client.MockClientHttpResponse;

import javax.annotation.Nullable;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Basic test of the Map processor.
 * <p></p>
 * Created by Jesse on 3/26/14.
 */
public class CreateMapProcessorWithLabelsTest extends AbstractMapfishSpringTest {
    public static final String BASE_DIR = "default/";

    @Autowired
    private ConfigurationFactory configurationFactory;
    @Autowired
    private MapfishParser parser;
    @Autowired
    private TestHttpClientFactory httpRequestFactory;


    @Test
    public void testExecute() throws Exception {
        PJsonObject requestData = loadJsonRequestData();
        doTest(requestData);
    }

    private void doTest(PJsonObject requestData) throws IOException, JSONException {
        httpRequestFactory.registerHandler(new Predicate<URI>() {
            @Override
            public boolean apply(@Nullable URI input) {
                return true;
            }
        }, new TestHttpClientFactory.Handler() {
            @Override
            public MockClientHttpRequest handleRequest(URI uri, HttpMethod httpMethod)  {
                final MockClientHttpRequest request = new MockClientHttpRequest();
                request.setResponse(new MockClientHttpResponse(new byte[0], HttpStatus.OK));
                return request;
            }
        });

        final Configuration config = configurationFactory.getConfig(getFile(BASE_DIR + "config.yaml"));
        final Template template = config.getTemplate("A4 landscape");
        Values values = new Values(requestData, template, parser, getTaskDirectory(), this.httpRequestFactory, new File("."));
        template.getProcessorGraph().createTask(values).invoke();

        @SuppressWarnings("unchecked")
        List<URI> layerGraphics = (List<URI>) values.getObject("layerGraphics", List.class);
        assertEquals(4, layerGraphics.size());

        final BufferedImage img = ImageIO.read(new File(layerGraphics.get(0)));
        ImageIO.write(img, "tiff", new File("c:/temp/"+getClass().getSimpleName()+".tiff"));
     //   new ImageSimilarity(img, 2).assertSimilarity(getFile(BASE_DIR + "expectedSimpleImage.tiff"), 30);
    }

    public static PJsonObject loadJsonRequestData() throws IOException {
        return parseJSONObjectFromFile(CreateMapProcessorWithLabelsTest.class, BASE_DIR + "requestData-A4-landscape-with-grid.json");
    }
}
