package com.example.gps;


import android.Manifest;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.content.Intent;
import android.widget.Toast;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button button_write_gps;
    private Button button_stop_writing;
    private Button button_show_map;

    private LocationManager locationManager;

    private boolean bool_record_in_db;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        button_write_gps =  findViewById(R.id.button_write_to_database);
        button_stop_writing = findViewById(R.id.button_stop_writing);
        button_show_map = findViewById(R.id.button_show_map);
        Button button_clear_db = findViewById(R.id.button_crear_db);


        button_show_map.setOnClickListener(this);
        button_stop_writing.setOnClickListener(this);
        button_write_gps.setOnClickListener(this);

        button_clear_db.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (permission_req()) {
                    showAlertClearDB(v);
                }
            }
        });


        button_stop_writing.setClickable(false);

        bool_record_in_db = false;


        String filePath = getString(R.string.filePath);
        File folder = new File(filePath);
        if (!folder.exists())
            folder.mkdirs();


        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        button_show_map.setClickable(true);
        button_write_gps.setClickable(true);
        button_stop_writing.setClickable(false);
    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    @SuppressLint("MissingPermission")
    @Override
    protected void onResume() {
        super.onResume();


        if (permission_req())
        {
            int time_update_gps = 10;
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    (100 * time_update_gps), 0, locationListener);
        }

        CheckGPS();
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        locationManager.removeUpdates(locationListener);
    }

    private LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            if (bool_record_in_db) {
                Write_to_db(location);
            }
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            CheckGPS();
        }

        @Override
        public void onProviderEnabled(String provider) {CheckGPS(); }

        @Override
        public void onProviderDisabled(String provider) { CheckGPS();  }
    };

    private void CheckGPS()
    {
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) && bool_record_in_db)
        {
            StopRec();
        }
    }


    @Override
    public void onClick(View v) {
        if (!permission_req()) return;
        switch (v.getId())
        {
            case R.id.button_show_map: ShowMap(v); break;
            case R.id.button_write_to_database: StartRec(); break;
            case R.id.button_stop_writing: StopRec(); break;
        }
    }

    private void ShowMap(View v)
    {

        Intent show_map = new Intent(v.getContext(), MapsActivity.class);
        startActivity(show_map);
    }

    private void StartRec()
    {

        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
        {
            Toast toast = Toast.makeText(getApplicationContext(),"GPS не включен",Toast.LENGTH_SHORT);
            toast.show();
            return;
        }

        Toast toast = Toast.makeText(getApplicationContext(),"Запись включена",Toast.LENGTH_SHORT);
        toast.show();

        bool_record_in_db = true;
 
        button_show_map.setClickable(false);
        button_write_gps.setClickable(false);
        button_stop_writing.setClickable(true);
    }

    private void StopRec()
    {
        Toast toast = Toast.makeText(getApplicationContext(),"Запись выключена",Toast.LENGTH_SHORT);
        toast.show();

        bool_record_in_db = false;

        button_show_map.setClickable(true);
        button_write_gps.setClickable(true);
        button_stop_writing.setClickable(false);
    }

    boolean permission_req()
    {
        boolean test = true;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
        {
            Toast.makeText(getApplicationContext(),"Не получены все права",Toast.LENGTH_SHORT).show();
            test = false;
        }
        return test;
    }


    ////////////////////////////////////////// Egor //////////////////////////////////////////
    
    private void Write_to_db(Location location)
    {
        double lat = location.getLatitude();
        double lon = location.getLongitude();

        String txtData = lat + " " + lon + "\n";

        String fileName = getString(R.string.fileName);
        String filePath = getString(R.string.filePath);

        try {

            FileWriter writer = new FileWriter((filePath + "/" + fileName), true);
            BufferedWriter bufferWriter = new BufferedWriter(writer);
            bufferWriter.write(txtData);
            bufferWriter.close();

        } catch (Exception e) {
            Toast toast = Toast.makeText(getApplicationContext(),"Невозможно создать файл",Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    public void showAlertClearDB(View v){

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);

        alertDialog.setTitle("Внимание");

        alertDialog.setMessage("Вы уверены, что хотите очистить базу?");

        alertDialog.setPositiveButton("Да", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int which) {
                ClearDB();
                Toast.makeText(getApplicationContext(), "База очищена", Toast.LENGTH_SHORT).show();
            }
        });

        alertDialog.setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        alertDialog.show();
    }


    private void ClearDB()
    {
        String fileName = getString(R.string.fileName);
        String filePath = getString(R.string.filePath);
        File file = new File(filePath + "/" + fileName);
        file.delete();
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////


}
