package org.geotools.geojson.feature;

import org.geotools.feature.FeatureIterator;
import org.geotools.geojson.GeoJSONUtil;
import org.geotools.geojson.feature.FeatureCollectionHandler;
import org.geotools.geojson.feature.FeatureJSON;
import org.json.simple.parser.JSONParser;
import org.opengis.feature.simple.SimpleFeature;

import java.io.*;


/**
 * Created by georgige on 5/19/2016.
 */
public class PFeatureJSON extends FeatureJSON {


    @Override
    public FeatureIterator<SimpleFeature> streamFeatureCollection(Object input) throws IOException {
        return new FeatureCollectionIterator(input);
    }

    class FeatureCollectionIterator extends FeatureJSON.FeatureCollectionIterator {


        FeatureCollectionIterator(Object input) {
            super(input);
            try {
                this.reader = toReader(input);
            }
            catch (IOException e) {
                throw new RuntimeException(e);
            }
            this.parser = new JSONParser();
        }


        Reader toReader(Object input) throws IOException {
            if (input instanceof BufferedReader) {
                return (BufferedReader) input;
            }

            if (input instanceof Reader) {
                return new BufferedReader((Reader)input);
            }

            if (input instanceof InputStream) {
                return new BufferedReader(new InputStreamReader((InputStream)input, "UTF-8"));
            }

            if (input instanceof File) {
                return new BufferedReader(new FileReader((File)input));
            }

            if (input instanceof String) {
                return new StringReader((String)input);
            }

            throw new IllegalArgumentException("Unable to turn " + input + " into a reader");
        }
    }
}
