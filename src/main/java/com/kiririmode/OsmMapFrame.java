package com.kiririmode;

import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.map.MapContent;
import org.geotools.map.MapViewport;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.swing.JMapFrame;
import org.geotools.tile.TileService;
import org.geotools.tile.impl.osm.OSMService;
import org.geotools.tile.util.TileLayer;

public class OsmMapFrame {
    public static void main(String args[])
    {
        String baseURL = "https://tile.openstreetmap.org/";
        TileService service = new OSMService("OSM", baseURL);

        MapContent map = new MapContent();
        map.addLayer(new TileLayer(service));

        // 東京駅の緯度と経度
        double lat = 35.681236;
        double lon = 139.767125;

        // 表示範囲を設定
        double span = 0.01;  // 緯度/経度での範囲
        DefaultGeographicCRS crs = DefaultGeographicCRS.WGS84;
        ReferencedEnvelope envelope = new ReferencedEnvelope(lon - span, lon + span, lat - span, lat + span, crs);

        // MapContentに表示範囲を設定
        MapViewport viewport = new MapViewport(envelope);
        map.setViewport(viewport);

        JMapFrame.showMap(map);
    }
}
