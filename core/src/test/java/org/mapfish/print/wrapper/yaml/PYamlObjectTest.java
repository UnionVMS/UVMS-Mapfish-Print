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

package org.mapfish.print.wrapper.yaml;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.junit.Test;
import org.mapfish.print.wrapper.json.PJsonArray;
import org.mapfish.print.wrapper.json.PJsonObject;

import java.util.Map;

import static org.junit.Assert.assertEquals;

public class PYamlObjectTest {
    @Test
    public void testToJson() throws Exception {
        Map<String, Object> map = Maps.newHashMap();
        map.put("att1", 1);
        map.put("att2", new Object[]{1,2});

        Map<String, Object> embedded = Maps.newHashMap();
        embedded.put("embeddedAtt1", true);
        Map<String, Object> embeddedEmbedded = Maps.newHashMap();
        embeddedEmbedded.put("ee1", 1);
        embedded.put("embeddedAtt2", embeddedEmbedded);
        embedded.put("embeddedAtt3", Lists.newArrayList("one", "two", "three"));
        map.put("att3", embedded);
        final PJsonObject test = new PYamlObject(map, "test").toJSON();

        assertEquals(3, test.size());
        assertEquals(1, test.getInt("att1"));

        PJsonArray array1 = test.getJSONArray("att2");
        assertEquals(2, array1.size());
        assertEquals(1, array1.get(0));
        assertEquals(2, array1.get(1));

        PJsonObject embeddedJson = test.getJSONObject("att3");
        assertEquals(3, embeddedJson.size());
        assertEquals(true, embeddedJson.has("embeddedAtt1"));

        PJsonObject embeddedEmbeddedJson = embeddedJson.getJSONObject("embeddedAtt2");
        assertEquals(1, embeddedEmbeddedJson.size());
        assertEquals(1, embeddedEmbeddedJson.getInt("ee1"));

        PJsonArray array2 = embeddedJson.getJSONArray("embeddedAtt3");
        assertEquals(3, array2.size());
        assertEquals("one", array2.getString(0));
        assertEquals("two", array2.getString(1));
        assertEquals("three", array2.getString(2));
    }
}