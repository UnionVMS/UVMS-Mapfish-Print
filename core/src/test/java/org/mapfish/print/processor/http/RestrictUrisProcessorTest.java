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

import com.google.common.base.Predicate;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mapfish.print.TestHttpClientFactory;
import org.mapfish.print.http.MfClientHttpRequestFactory;
import org.mapfish.print.processor.http.matcher.AcceptAllMatcher;
import org.mapfish.print.processor.http.matcher.AddressHostMatcher;
import org.mapfish.print.processor.http.matcher.LocalHostMatcher;
import org.mapfish.print.processor.http.matcher.URIMatcher;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.mock.http.client.MockClientHttpRequest;

import javax.annotation.Nullable;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class RestrictUrisProcessorTest {
    static final TestHttpClientFactory requestFactory = new TestHttpClientFactory();

    @BeforeClass
    public static void setUp() throws Exception {
        requestFactory.registerHandler(new Predicate<URI>() {
            @Override
            public boolean apply(@Nullable URI input) {
                return true;
            }
        }, new TestHttpClientFactory.Handler() {
            @Override
            public MockClientHttpRequest handleRequest(URI uri, HttpMethod httpMethod) {
                assertEquals("localhost", uri.getHost());
                return null;
            }
        });
    }

    @Test
    public void testCreateFactoryWrapperLegalRequest() throws Exception {
        final RestrictUrisProcessor restrictUrisProcessor = createLocalhostOnly();
        ClientHttpFactoryProcessorParam params = new ClientHttpFactoryProcessorParam();
        final MfClientHttpRequestFactory factoryWrapper = restrictUrisProcessor.createFactoryWrapper(params, requestFactory);
        factoryWrapper.createRequest(new URI("http://localhost:8080/geoserver/wms"), HttpMethod.GET);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateFactoryWrapperIllegalRequest() throws Exception {
        final RestrictUrisProcessor restrictUrisProcessor = createLocalhostOnly();
        ClientHttpFactoryProcessorParam params = new ClientHttpFactoryProcessorParam();
        final ClientHttpRequestFactory factoryWrapper = restrictUrisProcessor.createFactoryWrapper(params, requestFactory);
        factoryWrapper.createRequest(new URI("http://www.google.com/q"), HttpMethod.GET);
    }

    @Test
    public void testRejectWithLegalRequest() throws Exception {
        final RestrictUrisProcessor restrictUrisProcessor = createDenyInternal();
        ClientHttpFactoryProcessorParam params = new ClientHttpFactoryProcessorParam();
        final ClientHttpRequestFactory factoryWrapper = restrictUrisProcessor.createFactoryWrapper(params, requestFactory);
        factoryWrapper.createRequest(new URI("http://localhost/q"), HttpMethod.GET);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRejectWithIllegalRequest() throws Exception {
        final RestrictUrisProcessor restrictUrisProcessor = createDenyInternal();
        ClientHttpFactoryProcessorParam params = new ClientHttpFactoryProcessorParam();
        final ClientHttpRequestFactory factoryWrapper = restrictUrisProcessor.createFactoryWrapper(params, requestFactory);
        factoryWrapper.createRequest(new URI("http://192.168.12.23/q"), HttpMethod.GET);
    }

    private RestrictUrisProcessor createLocalhostOnly() {
        final RestrictUrisProcessor processor = new RestrictUrisProcessor();
        processor.setMatchers(Collections.singletonList(new LocalHostMatcher()));
        return processor;
    }

    private RestrictUrisProcessor createDenyInternal() {
        final RestrictUrisProcessor processor = new RestrictUrisProcessor();
        List<URIMatcher> matchers = new ArrayList<URIMatcher>();
        AddressHostMatcher addressHostMatcher = new AddressHostMatcher();
        addressHostMatcher.setIp("192.168.12.0");
        addressHostMatcher.setMask("255.255.255.0");
        addressHostMatcher.setReject(true);
        matchers.add(addressHostMatcher);
        matchers.add(new AcceptAllMatcher());
        processor.setMatchers(matchers);
        return processor;
    }
}