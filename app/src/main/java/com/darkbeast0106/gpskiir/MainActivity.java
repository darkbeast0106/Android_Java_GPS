package com.darkbeast0106.gpskiir;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {
    private TextView textView;
    private Timer timer;
    private double longitude;
    private double latitude;

    private LocationManager locationManager;
    private LocationListener locationListener;
    private boolean writePermission;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();

        // Vizságlat, hogy van-e engedély a helyeadatok lekérdezéséhez.
        // Android Studio alt+enter segítségével legenerálja, lásd lentebb.
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            textView.setText(R.string.no_gps_permission);

            // Engedély kérés ablak megnyitása.
            String[] permissions =
                new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            };
            ActivityCompat.requestPermissions(this, permissions, 0);
            return;
        }
        // LocationManagernél a Listener hozzáadásakor azt is meg kell adni,
        // hogy mely szolgáltató adja a helyadatokat, illetve azt is,
        // hogy milyen gyakran és mekkora táv megtétele után történjen esemény.
        // 0, 0 megadásával folyamatos a helyadat frissítése.
        // Kötelező előtte ellenőrizni, hogy meg van-e a szükséges engedély.
        // Az ellenőrző blokk alt+enter-rel legenerálható.
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                0, 0, locationListener);
    }

    private void init() {
        textView = findViewById(R.id.gps_text);

        //LocationManager
        //Ez felelős a hely lekérdezésért.
        locationManager =
                (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        //LocationListener:
        //LocationManager eseménykezelője, megmondja, hogy mi történjen, amikor helyadatot kér le.
        //Létrehozásakor lambdát alkalmazható:
        locationListener = location -> {
            longitude = location.getLongitude();
            latitude = location.getLatitude();
        };
        //Lambda nélküli szintaxis:
        /*
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                longitude = location.getLongitude();
                latitude = location.getLatitude();
            }
        };
         */

        // Vizsgálat, hogy van-e endegély fájlt olvasni és fájlba írni a telefon tárhelyén
        // External Strorage ne tévesszen meg, ez vonatkozik a beépített tárhelyre is
        // Azért számít külső tárhelynek, mert az alkalmazásnak is van külön belső tárhelye
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {

            writePermission = false;
            // Engedély kérés ablak megnyitása.
            String[] permissions =
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE};
            ActivityCompat.requestPermissions(this, permissions, 1);
        }else{
            writePermission = true;
        }
    }

    // onResume akkor hívódik meg, amikor az alkalmazás előtérbe kerül.
    // Mehívódik az alkalmazás létrejöttekor is, illetve ha újra megnyitjuk a háttérből,
    // vagy bezárjuk a rajta lévő felugró ablakokat.
    // Timert legtöbb esetben itt érdemes létrehozni és onPauseban leállítani.
    @Override
    protected void onResume() {
        // Javas Timer leállítás után megszűnik és nem lehet újra indítani,
        // új példányt kell csinálni belőle.
        timer = new Timer();
        // TimerTask az esemény amit a timer végez,
        // csak egy Timerhez rendelhető hozzá, így új timer esetény új task is kell.
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                TimerMethod();
            }
        };
        timer.schedule(task, 1000, 5000);
        super.onResume();
    }

    // Megmondja, hogy mi történjen miután a felhasználó megadta vagy
    // megtagadta a különböző engedélyeket.
    // A requestCode az engedélykéréskor átadott requestCode,
    // a permissions tömb tartalmazza a kérésben átadott összes permissiont,
    // a grantResults tartalmazza, hogy az előbbi tömbből az azonos indexű engedély meg lett-e adva.
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                   @NonNull int[] grantResults) {
        if (requestCode == 0) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED)
            {
                // Mivel a requestLocationUpdates nem hívható meg a fenti ellenőrző block nélkül
                // Így itt nem használhatjuk a grantResults tömböt
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                        0, 0, locationListener);
            }
        }
        if (requestCode == 0) {
            writePermission =
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            == PackageManager.PERMISSION_GRANTED;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    // onPause akkor hívódik meg, amikor az alkalmazás már nincs az előtérben
    // pl.: lerakjuk, vagy felugró ablak nyílik meg rajta.
    // Ilyenkor érdemes lehet leállítani a timert, hogy a háttérben ne fusson tovább.
    @Override
    protected void onPause() {
        timer.cancel();
        super.onPause();
    }

    private void TimerMethod() {
        this.runOnUiThread(TimerTick);
    }

    // A textViewot nem tudja külső szál módosítani, ezért,
    // hogy Timerrel tudjuk módosítani a szöveget szükség van 1 Runnable típúsú objektumra.
    private final Runnable TimerTick = new Runnable(){
        @Override
        public void run() {
            //String locationText = String.format("Lat: %f\r\nLong:%f",latitude,longitude);
            // getString alkmas formátum stringbe a változók behelyezésére is.
            String locationText = getString(R.string.location_format, latitude,longitude);
            textView.setText(locationText);
            if (writePermission){
                try {
                    Naplozas.kiir(longitude,latitude);
                } catch (IOException e) {
                    Log.d("Kiiras", e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    };

}