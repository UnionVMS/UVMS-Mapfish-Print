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

package org.mapfish.print.processor.http;

import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.test.context.ContextConfiguration;

import java.net.URI;
import javax.annotation.Nullable;

import static org.junit.Assert.assertEquals;

@ContextConfiguration(locations = {
        "classpath:org/mapfish/print/processor/http/map-uri/add-custom-processor-application-context.xml"
})
public class MapUriProcessorTest extends AbstractHttpProcessorTest {

    @Override
    protected String baseDir() {
        return "map-uri";
    }

    @Override
    protected Class<TestProcessor> testProcessorClass() {
        return TestProcessor.class;
    }

    @Override
    protected Class<? extends AbstractClientHttpRequestFactoryProcessor> classUnderTest() {
        return MapUriProcessor.class;
    }

    public static class TestProcessor extends AbstractTestProcessor {
        @Nullable
        @Override
        public Void execute(TestParam values, ExecutionContext context) throws Exception {
            final URI uri = new URI("http://localhost:8080/path?query#fragment");
            final ClientHttpRequest request = values.clientHttpRequestFactory.createRequest(uri, HttpMethod.GET);
            final URI finalUri = request.getURI();

            assertEquals("http", finalUri.getScheme());
            assertEquals("127.0.0.1", finalUri.getHost());
            assertEquals("/path", finalUri.getPath());
            assertEquals(8080, finalUri.getPort());
            assertEquals("query", finalUri.getQuery());
            assertEquals("fragment", finalUri.getFragment());

            return null;
        }
    }
}