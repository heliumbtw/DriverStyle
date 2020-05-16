package com.example.drivestyle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import static com.example.drivestyle.App.CHANNEL_ID;
import static com.example.drivestyle.Utils.checkFolder;
import static com.example.drivestyle.Utils.df;
import static com.example.drivestyle.Utils.df_int;
import static com.example.drivestyle.Utils.getDate;

public class MainActivity extends AppCompatActivity implements LocationListener {

    SensorManager sensorManager;
    Sensor sensor_a;
    Sensor sensor_m;

    private boolean mLastAccelerometerSet = false;
    private boolean mLastMagnetometerSet = false;
    private float[] mLastAccelerometer = new float[3];
    private float[] mLastMagnetometer = new float[3];
    private float[] mR = new float[9];
    private float[] mOrientation = new float[3];
    private float[] mOrientation_d = new float[3];
    private float[] linear_acceleration = new float[3];

    private String[] acc_values = new String[3];

    private boolean isPlay = true;
    private boolean finishTrip = false;
    // TODO: ML, чарты, характеристики в конце поездки, БД, проверить csv после всего
    private float lastSpeed = 0;
    private float currentSpeed;
    private float gpsAcceleration;

    int PERMISSION_ALL = 1;
    String[] PERMISSIONS = {
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        assert sensorManager != null;
        sensor_a = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensor_m = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        Intent serviceIntent = new Intent(this, DriveStyleService.class);
        ContextCompat.startForegroundService(this, serviceIntent);
        if (!hasPermissions(this, PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
        }
        statusCheck();
        checkFolder();
        final ImageView playButton = findViewById(R.id.playbtn);
        playButton.setOnClickListener(new View.OnClickListener() { public void onClick(View v) {
            if(isPlay) {
                playButton.setImageResource(R.drawable.stopicon);
                doStuff();
            }
            else {
                if (buildAlertMessageFinishTrip()) {
                    stopStuff();
                    playButton.setImageResource(R.drawable.playicon);
                }
            }
            isPlay = !isPlay;
        }
        });
    }

    public static boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context,
                        permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }
    @Override
    protected void onResume() {
        super.onResume();
        mLastAccelerometerSet = false;
        mLastMagnetometerSet = false;
        sensorManager.registerListener(SensorListener, sensor_a, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(SensorListener, sensor_m, SensorManager.SENSOR_DELAY_NORMAL);
        //1000000,1000000
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        finish();
        Intent serviceIntent = new Intent(this, DriveStyleService.class);
        stopService(serviceIntent);
    }

    @Override
    public void onBackPressed() {android.os.Process.killProcess(android.os.Process.myPid());}

    @Override
    public void onLocationChanged(Location location) {
        TextView txt = this.findViewById(R.id.current_speed);
        if (location != null) {
            currentSpeed = location.getSpeed();
            gpsAcceleration = currentSpeed - lastSpeed;
            lastSpeed = currentSpeed;
            int speed = (int) ((currentSpeed * 3600) / 1000);
            txt.setText(String.valueOf(speed));
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();
            updateNotification(String.valueOf(speed));
            WriteCsv.writeDataByLine(getDate(), String.valueOf(speed),
                    String.valueOf(latitude), String.valueOf(longitude),
                    String.valueOf(acc_values[0]), String.valueOf(acc_values[1]),
                    String.valueOf(acc_values[2]));
        }
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }

    void updateNotification(String new_speed){
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
        assert mNotificationManager != null;
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

    public void statusCheck() {
        final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        assert manager != null;
        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            buildAlertMessageNoGps();
        }
    }

    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AlertDialogStyle);
        builder.setMessage("GPS в данный момент выключен, хотите включить его ?")
                .setCancelable(false)
                .setPositiveButton("Да", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("Нет", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    private boolean buildAlertMessageFinishTrip() {
        final AlertDialog.Builder dialog = new AlertDialog.Builder(this, R.style.AlertDialogStyle);
        dialog.setCancelable(false);
        dialog.setMessage("Закончить поездку ?");
        dialog.setPositiveButton("Да", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                finishTrip = true;
                Intent intent = new Intent(MainActivity.this, FinishActivity.class);
                startActivity(intent);
            }
        })
                .setNegativeButton("Нет", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = dialog.create();
        alert.show();
        return finishTrip;
    }

    @SuppressLint("MissingPermission")
    void doStuff(){
        LocationManager lm = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        if (lm != null) {
            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER,1000,0, this);
        }
    }

    void stopStuff(){
        LocationManager lm = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        if (lm != null) {
            lm.removeUpdates(this);
        }
    }

    public final SensorEventListener SensorListener = new SensorEventListener() {

        public void onAccuracyChanged(Sensor sensor, int acc) {
        }

        @SuppressLint("SetTextI18n")
        public void onSensorChanged(SensorEvent sensorEvent) {
            if (sensorEvent.sensor == sensor_a) {
                System.arraycopy(sensorEvent.values, 0, mLastAccelerometer, 0, sensorEvent.values.length);
                mLastAccelerometerSet = true;

                final float alpha = (float) 0.8;
                float[] gravity = new float[3];

                gravity[0] = alpha * gravity[0] + (1 - alpha) * sensorEvent.values[0];
                gravity[1] = alpha * gravity[1] + (1 - alpha) * sensorEvent.values[1];
                gravity[2] = alpha * gravity[2] + (1 - alpha) * sensorEvent.values[2];

                linear_acceleration[0] = sensorEvent.values[0] - gravity[0];
                linear_acceleration[1] = sensorEvent.values[1] - gravity[1];
                linear_acceleration[2] = sensorEvent.values[2] - gravity[2];

                acc_values[0] = df.format(linear_acceleration[0]);
                acc_values[1] = df.format(linear_acceleration[1]);
                acc_values[2] = df.format(linear_acceleration[2]);

                TextView acc_x = findViewById(R.id.acc_x_value);
                TextView acc_y = findViewById(R.id.acc_y_value);
                TextView acc_z = findViewById(R.id.acc_z_value);

                acc_x.setText(acc_values[0]);
                acc_y.setText(acc_values[1]);
                acc_z.setText(acc_values[2]);

            } else if (sensorEvent.sensor == sensor_m) {
                System.arraycopy(sensorEvent.values, 0, mLastMagnetometer, 0, sensorEvent.values.length);
                mLastMagnetometerSet = true;
            }
            if (mLastAccelerometerSet && mLastMagnetometerSet) {
                SensorManager.getRotationMatrix(mR, null, mLastAccelerometer, mLastMagnetometer);
                SensorManager.getOrientation(mR, mOrientation);

                mOrientation_d[0] = (int) (mOrientation[0]*180/3.14);
                mOrientation_d[1] = (int) (mOrientation[1]*180/3.14);
                mOrientation_d[2] = (int) (mOrientation[2]*180/3.14);

                TextView rot_roll = findViewById(R.id.roll_value);
                TextView rot_pitch = findViewById(R.id.pitch_value);
                TextView rot_yaw = findViewById(R.id.yaw_value);

                rot_roll.setText(df_int.format(mOrientation_d[0]));
                rot_pitch.setText(df_int.format(mOrientation_d[1]));
                rot_yaw.setText(df_int.format(mOrientation_d[2]));
            }
        }
    };

}