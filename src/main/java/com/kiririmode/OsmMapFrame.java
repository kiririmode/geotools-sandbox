package com.kiririmode;

import org.geotools.map.MapContent;
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
        JMapFrame.showMap(map);
    }
}
