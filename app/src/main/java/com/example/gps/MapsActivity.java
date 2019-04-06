package com.example.gps;


import android.graphics.Color;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.maps.android.heatmaps.HeatmapTileProvider;


import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;



public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMapLongClickListener {

    private GoogleMap mMap;
    private ArrayList<LatLng> list = new ArrayList<LatLng>();
    private HeatmapTileProvider mProvider;
    private TileOverlay mOverlay;
    Circle circle;

    private int radius = 25; //radius 25 meter

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        try {
            setheat();
        } catch (IOException e) {
            Toast toast = Toast.makeText(getApplicationContext(),"База пуста",Toast.LENGTH_SHORT);
            toast.show();
            return;
        }

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(list.get(0),10));
        mMap.setOnMapLongClickListener(this);
    }


    private void setheat() throws IOException {

        String fileName = getString(R.string.fileName);
        String filePath = getString(R.string.filePath);

        BufferedReader br = new BufferedReader(new FileReader((filePath + "/" + fileName)));

        String sCurrentLine;

        while ((sCurrentLine = br.readLine()) != null)
        {
            String[] lat_lon = sCurrentLine.split(" ");
            double lat = Double.parseDouble(lat_lon[0]);
            double lon = Double.parseDouble(lat_lon[1]);
            list.add(new LatLng(lat, lon));
        }


        mProvider = new HeatmapTileProvider.Builder().data(list).build();
        mProvider.setRadius(radius);
        mOverlay = mMap.addTileOverlay(new TileOverlayOptions().tileProvider(mProvider));

    }

    ////////////////////////////////////////// Evgeniy //////////////////////////////////////////    
    @Override
    public void onMapLongClick(LatLng latLng) {

        int seconds = countSec(latLng);
        Toast toast = Toast.makeText(getApplicationContext(),"Кол-во секунд: " + seconds,Toast.LENGTH_SHORT);
        toast.show();

        if (circle != null)
            circle.remove();

        circle = mMap.addCircle(new CircleOptions()
                .center(latLng)
                .radius(radius)
                .strokeColor(Color.BLUE)
                .fillColor(Color.BLUE));
    }
    /////////////////////////////////////////////////////////////////////////////////////////////

    ////////////////////////////////////////// Nikita //////////////////////////////////////////
    private int countSec(LatLng latLng)
    {
        double lat = latLng.latitude;
        double lon = latLng.longitude;

        int count = 0;

        for (int i = 0; i < list.size(); i++)
        {
            double lat2 = list.get(i).latitude;
            double lon2 = list.get(i).longitude;

            double result = 111.2 * Math.sqrt( (lon - lon2)*(lon - lon2) + (lat - lat2)*Math.cos(Math.PI*lon/180)*(lat - lat2)*Math.cos(Math.PI*lon/180));

            if( result <= (double)(radius)/1000)  count++;
        }

        return count;
    }
    /////////////////////////////////////////////////////////////////////////////////////////////

}
