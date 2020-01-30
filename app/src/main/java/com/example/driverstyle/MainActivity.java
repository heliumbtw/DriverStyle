package com.example.driverstyle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import static com.example.driverstyle.App.CHANNEL_ID;


public class MainActivity extends AppCompatActivity implements LocationListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Intent serviceIntent = new Intent(this, DriveStyleService.class);
        ContextCompat.startForegroundService(this, serviceIntent);
        //check permission
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1000);
        } else {
            doStuff();

        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        finish();
        Intent serviceIntent = new Intent(this, DriveStyleService.class);
        stopService(serviceIntent);
    }

    @Override
    public void onBackPressed() {
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    @Override
    public void onLocationChanged(Location location) {

        TextView txt = this.findViewById(R.id.current_speed);
        if (location != null) { // if coords are changing
            Log.d("myTAG", "speed " + location.getSpeed());
            //float nCurrentSpeed = location.getSpeed() * 3.6f;
            //txt.setText(String.format("%.2f", nCurrentSpeed)+ " km/h" );
            int speed=(int) ((location.getSpeed()*3600)/1000);
            txt.setText(String.valueOf(speed));
            updateNotification(String.valueOf(speed));
        } else {
            //txt.setText("-.- km/h");
            //cuz 0
        }
    }

    private void updateNotification(String new_speed){
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, 0);

        Notification notification= new Notification.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_directions_car_black_24dp)
                .setContentTitle("Стиль ")
                .setContentText("Текущая скорость " + new_speed + " км/ч")
                .setColor(ContextCompat.getColor(this, R.color.colorNotification))
                .setAutoCancel(false)
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .setContentIntent(pendingIntent)
                .build();
        NotificationManager mNotificationManager=(NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(DriveStyleService.notificationId,notification);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 1000) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                doStuff();
            } else {
                finish();
            }
        }
    }

    @SuppressLint("MissingPermission")
    private void doStuff(){
        LocationManager lm = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        if (lm != null) {
            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER,1000,0,this);
            // commented, this is from the old version
            // this.onLocationChanged(null);
        }
        //Toast.makeText(this,"Waiting for GPS connection!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}
