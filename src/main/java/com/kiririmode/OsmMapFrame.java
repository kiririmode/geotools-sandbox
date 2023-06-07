package com.kiririmode;

import org.geotools.data.DataUtilities;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.feature.SchemaException;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.map.FeatureLayer;
import org.geotools.map.MapContent;
import org.geotools.map.MapViewport;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.styling.*;
import org.geotools.swing.JMapFrame;
import org.geotools.tile.TileService;
import org.geotools.tile.impl.osm.OSMService;
import org.geotools.tile.util.TileLayer;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.FilterFactory2;
import org.opengis.geometry.Envelope;

import java.awt.*;

public class OsmMapFrame {
    public static void main(String args[]) throws SchemaException {
        String baseURL = "https://tile.openstreetmap.org/";
        TileService service = new OSMService("OSM", baseURL);

        MapContent map = new MapContent();
        map.addLayer(new TileLayer(service));

        // 札幌市役所の緯度と経度
        double lat = 43.062083;
        double lon = 141.354389;

        // 表示範囲を設定
        double span = 0.001;  // 緯度/経度での範囲
        ReferencedEnvelope envelope = new ReferencedEnvelope(lon - span, lon + span, lat - span, lat + span, DefaultGeographicCRS.WGS84);

        // MapContentに表示範囲を設定
        map.getViewport().setBounds(envelope);

        // 点のスタイル作成
        StyleBuilder styleBuilder = new StyleBuilder();
        PointSymbolizer pointSymbolizer = styleBuilder.createPointSymbolizer();
        Mark mark = styleBuilder.createMark(StyleBuilder.MARK_SQUARE, Color.RED, Color.RED, 12);
        Graphic graphic = styleBuilder.createGraphic(null, mark, null, 1.0f, 1.0f, 0);
        pointSymbolizer.setGraphic(graphic);

        // 市役所の座標を作成
        GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();
        Coordinate coordinate = new Coordinate(lon, lat);
        org.locationtech.jts.geom.Point point = geometryFactory.createPoint(coordinate);

        // 市役所を示すフィーチャーを作成
        SimpleFeatureType featureType = DataUtilities.createType("Location", "geometry:Point");
        SimpleFeature feature = DataUtilities.template(featureType);
        feature.setDefaultGeometry(point);

        // 市役所のレイヤーを作成してマップに追加
        // Style s = SLD.createSimpleStyle(featureType, Color.RED);
        Style style = styleBuilder.createStyle();
        style.featureTypeStyles().add(styleBuilder.createFeatureTypeStyle(pointSymbolizer));
        FeatureLayer layer = new FeatureLayer(DataUtilities.collection(feature), style);
        map.addLayer(layer);

        JMapFrame.showMap(map);
    }
}
