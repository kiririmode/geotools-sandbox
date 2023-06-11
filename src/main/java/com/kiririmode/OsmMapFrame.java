package com.kiririmode;

import org.geotools.data.DataUtilities;
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.feature.SchemaException;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.map.FeatureLayer;
import org.geotools.map.MapContent;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.styling.*;
import org.geotools.swing.JMapFrame;
import org.geotools.tile.TileService;
import org.geotools.tile.impl.osm.OSMService;
import org.geotools.tile.util.TileLayer;
import org.locationtech.jts.geom.*;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import java.awt.*;
import java.util.Arrays;
import java.util.function.Function;

public class OsmMapFrame {
    public static void main(String args[]) throws SchemaException {
        String baseURL = "https://tile.openstreetmap.org/";
        TileService service = new OSMService("OSM", baseURL);

        MapContent map = new MapContent();
        map.addLayer(new TileLayer(service));

        Coordinate[] coordinates = new Coordinate[] {
                new Coordinate(141.354496, 43.062096),    // 札幌時計台
                new Coordinate(141.356246, 43.055527),    // 大通公園
                new Coordinate(141.356882, 43.068624)     // さっぽろテレビ塔
        };

        FeatureLayer lineLayer = createLineLayer("Line", "geometry:LineString", coordinates,
                styleBuilder -> styleBuilder.createLineSymbolizer(styleBuilder.createStroke(Color.BLUE, 2.0)));
        map.addLayer(lineLayer);

        FeatureLayer pointLayer = createPointLayer("Point", "geometry:Point", coordinates,
                styleBuilder -> styleBuilder.createPointSymbolizer(
                        styleBuilder.createGraphic(null,
                                styleBuilder.createMark(StyleBuilder.MARK_CIRCLE, new Color(0,0,0,0), Color.BLUE, 1.0), null, 1,10, 0)));
        map.addLayer(pointLayer);

        setBound(map, coordinates);
        JMapFrame.showMap(map);
    }

    private static void setBound(MapContent map, Coordinate[] coordinates) {
        double minLat = Arrays.stream(coordinates).mapToDouble(c -> c.y).min().orElseThrow(IllegalStateException::new);
        double maxLat = Arrays.stream(coordinates).mapToDouble(c -> c.y).max().orElseThrow(IllegalStateException::new);
        double minLon = Arrays.stream(coordinates).mapToDouble(c -> c.x).min().orElseThrow(IllegalStateException::new);
        double maxLon = Arrays.stream(coordinates).mapToDouble(c -> c.x).max().orElseThrow(IllegalStateException::new);

        double offset = 0.001;
        map.getViewport().setBounds(new ReferencedEnvelope(minLon - offset, maxLon + offset, minLat - offset, maxLat + offset, DefaultGeographicCRS.WGS84));
    }

    private static FeatureLayer createLineLayer(String typeName, String typeSpec, Coordinate[] coordinates, Function<StyleBuilder, Symbolizer> symbolizerCreator) throws SchemaException {
        SimpleFeatureType type = DataUtilities.createType(typeName, typeSpec);
        DefaultFeatureCollection featureCollection = new DefaultFeatureCollection();

        SimpleFeature feature = DataUtilities.template(type);
        feature.setDefaultGeometry(new GeometryFactory().createLineString(coordinates));
        featureCollection.add(feature);
        return getFeatureLayer(typeName, symbolizerCreator, featureCollection);
    }


    private static FeatureLayer createPointLayer(String typeName, String typeSpec, Coordinate[] coordinates, Function<StyleBuilder, Symbolizer> symbolizerCreator) throws SchemaException {
        SimpleFeatureType type = DataUtilities.createType(typeName, typeSpec);
        DefaultFeatureCollection featureCollection = new DefaultFeatureCollection();

        for (Coordinate coordinate : coordinates) {
            SimpleFeature feature = DataUtilities.template(type);
            feature.setDefaultGeometry(new GeometryFactory().createPoint(coordinate));
            featureCollection.add(feature);
        }

        return getFeatureLayer(typeName, symbolizerCreator, featureCollection);
    }

    private static FeatureLayer getFeatureLayer(String typeName, Function<StyleBuilder, Symbolizer> symbolizerCreator, DefaultFeatureCollection featureCollection) {
        StyleBuilder styleBuilder = new StyleBuilder();
        Rule rule = styleBuilder.createRule(symbolizerCreator.apply(styleBuilder));
        FeatureTypeStyle fts = styleBuilder.createFeatureTypeStyle(typeName, rule);
        Style style = styleBuilder.createStyle();
        style.featureTypeStyles().add(fts);

        return new FeatureLayer(featureCollection, style);
    }
}
