package com.kiririmode;

import org.geotools.data.DataUtilities;
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.feature.SchemaException;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.map.FeatureLayer;
import org.geotools.map.MapContent;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.styling.Stroke;
import org.geotools.styling.*;
import org.geotools.swing.JMapFrame;
import org.geotools.tile.TileService;
import org.geotools.tile.impl.osm.OSMService;
import org.geotools.tile.util.TileLayer;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import java.awt.*;
import java.util.Arrays;

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

        FeatureLayer lineLayer = createLineLayer(coordinates, 2.0);
        map.addLayer(lineLayer);

        FeatureLayer pointLayer = createPointLayer(coordinates);
        map.addLayer(pointLayer);

        setBound(map, coordinates);
        JMapFrame.showMap(map);
    }

    private static void setBound(MapContent map, Coordinate[] coordinates) {
        double minLat = Arrays.stream(coordinates).mapToDouble(c -> c.y).min().orElseThrow(IllegalStateException::new);
        double maxLat = Arrays.stream(coordinates).mapToDouble(c -> c.y).max().orElseThrow(IllegalStateException::new);
        double minLon = Arrays.stream(coordinates).mapToDouble(c -> c.x).min().orElseThrow(IllegalStateException::new);
        double maxLon = Arrays.stream(coordinates).mapToDouble(c -> c.x).max().orElseThrow(IllegalStateException::new);

        // 表示範囲を設定
        double offset = 0.001;
        ReferencedEnvelope envelope = new ReferencedEnvelope(minLon - offset, maxLon + offset, minLat - offset, maxLat + offset, DefaultGeographicCRS.WGS84);

        // MapContentに表示範囲を設定
        map.getViewport().setBounds(envelope);
    }

    private static FeatureLayer createLineLayer(Coordinate[] coordinates, double lineWidth) throws SchemaException {
        GeometryFactory geometryFactory = new GeometryFactory();

        // Lineという名前で「線」のFeatureType、Featureを作成
        SimpleFeatureType lineStringType = DataUtilities.createType("Line", "geometry:LineString");
        SimpleFeature lineStringFeature = DataUtilities.template(lineStringType);

        // getmetryの指定
        LineString lineString = geometryFactory.createLineString(coordinates);
        lineStringFeature.setDefaultGeometry(lineString);

        StyleBuilder styleBuilder = new StyleBuilder();
        // 線のスタイル作成
        Stroke stroke = styleBuilder.createStroke(Color.BLUE, lineWidth);
        LineSymbolizer symbolizer = styleBuilder.createLineSymbolizer(stroke);

        Rule rule = styleBuilder.createRule(symbolizer);
        FeatureTypeStyle fts = styleBuilder.createFeatureTypeStyle("Feature", new Rule[]{rule});

        Style style = styleBuilder.createStyle();
        style.featureTypeStyles().add(fts);

        return new FeatureLayer(DataUtilities.collection(lineStringFeature), style);
    }

    private static FeatureLayer createPointLayer(Coordinate[] coordinates) throws SchemaException {
        GeometryFactory geometryFactory = new GeometryFactory();

        SimpleFeatureType pointType = DataUtilities.createType("Point", "geometry:Point");
        DefaultFeatureCollection points = new DefaultFeatureCollection();

        for (Coordinate c : coordinates) {
            Point point = geometryFactory.createPoint(c);
            SimpleFeature pointFeature = DataUtilities.template(pointType);
            pointFeature.setDefaultGeometry(point);
            points.add(pointFeature);
        }

        StyleBuilder styleBuilder = new StyleBuilder();

        Mark mark = styleBuilder.createMark(StyleBuilder.MARK_CIRCLE, new Color(0,0,0,0), Color.BLUE, 1.0);
        Graphic graphic = styleBuilder.createGraphic(null, mark, null, 1,10, 0);
        PointSymbolizer pointSymbolizer = styleBuilder.createPointSymbolizer(graphic);

        Rule rule = styleBuilder.createRule(pointSymbolizer);
        FeatureTypeStyle fts = styleBuilder.createFeatureTypeStyle("Feature", new Rule[]{rule});
        Style style = styleBuilder.createStyle();
        style.featureTypeStyles().add(fts);

        return new FeatureLayer(points, style);
    }
}
