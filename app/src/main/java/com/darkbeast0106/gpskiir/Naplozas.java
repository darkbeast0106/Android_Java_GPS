package com.darkbeast0106.gpskiir;

import android.os.Environment;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Naplozas {

    //Fájlbaírás, IOExceptiont dobhat ám ezt a MainActivityn tervezzük lekezelni.
    public static void kiir(double longitude, double latitude) throws IOException {
        //Dátum-idő lekérdezése
        Date datum = Calendar.getInstance().getTime();

        // Dátum formázására használt objektum. A formázásokat lásd:
        // https://docs.oracle.com/javase/7/docs/api/java/text/SimpleDateFormat.html
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String formazottDatum = df.format(datum);

        //CSV fájl egy sora. Formátum: longitude,latitude,dátum
        String sor = String.format("%f,%f,%s",longitude,latitude,formazottDatum);

        //Megnézi a belső tárhely állapotát.
        String state = Environment.getExternalStorageState();
        if (state.equals(Environment.MEDIA_MOUNTED)){
            //Filebaírás FileWriter+Buffered writer segítségével, felülírás helyett hozzáfűzéssel
            File file = new File(Environment.getExternalStorageDirectory(),"gps_adatok.csv");
            BufferedWriter bw = new BufferedWriter(new FileWriter(file, true));
            bw.append(sor);
            // Nincs olyan függvény ami alapból sortörést is tenne a sorok végére,
            // Ezt nekünk kell megtenni. \r\n illetve sima \n helyett érdemesebb
            // az operációs rendszer alap elválasztó karakterét odaírni.
            bw.append(System.lineSeparator());
            bw.close();
        }

    }
}
