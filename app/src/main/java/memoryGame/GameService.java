package memoryGame;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Binder;
import android.support.v4.app.ActivityCompat;

public class GameService extends Service implements SensorEventListener, LocationListener, IConstants {

    //binder
    private final IBinder       mBinder = new MyLocalBinder();

    //location
    private Location            lastLocation;
    private LocationManager     locationManager;

    //sensors
    private GameSensorListener  sensorListener;
    private SensorManager       sensorManager;
    private Sensor              accelerometer;
    private Sensor              magnetometer;
    private int                 counterSamples = 0;
    private boolean             isSensorExist = false;
    private float[]             lastAccelerometer = new float[3];
    private float[]             lastMagnetometer = new float[3];
    private float[][]           lastOrientationArr = new float[SENSOR_COUNTER][3];
    private boolean             lastAccelerometerSet = false;
    private boolean             lastMagnetometerSet = false;
    private float[]             mR = new float[9];
    private float[]             firstOrientation = new float[3];
    private float[]             orientation = new float[3];


    public GameService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        locationManager.removeUpdates(this);
        return super.onUnbind(intent);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        initSensors();
        setLocation();
    }

    private void initSensors(){
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        }
        if (accelerometer != null && magnetometer != null) { //first orientation
            isSensorExist = true;
            SensorManager.getRotationMatrix(mR, null, lastAccelerometer, lastMagnetometer);
            SensorManager.getOrientation(mR, firstOrientation);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        if (event.sensor == accelerometer) {
            System.arraycopy(event.values, 0, lastAccelerometer, 0, event.values.length);
            lastAccelerometerSet = true;
        } else if (event.sensor == magnetometer) {
            System.arraycopy(event.values, 0, lastMagnetometer, 0, event.values.length);
            lastMagnetometerSet = true;
        }

        if (lastAccelerometerSet && lastMagnetometerSet) {
            SensorManager.getRotationMatrix(mR, null, lastAccelerometer, lastMagnetometer);
            SensorManager.getOrientation(mR, orientation);
        }
        System.arraycopy(orientation, 0, lastOrientationArr[counterSamples++], 0, orientation.length);

        if (counterSamples == SENSOR_COUNTER) {
            checkMoves(lastOrientationArr);
            counterSamples = 0;
        }
    }

    public class MyLocalBinder extends Binder {
        GameService getService() {
            // returns the local service
            return GameService.this;
        }

        void registerSensorListener(GameSensorListener listener) {
            if (isSensorExist) {
                lastAccelerometerSet = false;
                lastMagnetometerSet = false;
                sensorManager.registerListener(GameService.this, accelerometer, sensorManager.SENSOR_DELAY_NORMAL);
                sensorManager.registerListener(GameService.this, magnetometer, sensorManager.SENSOR_DELAY_NORMAL);
                sensorListener = listener;
            }
        }

        void DeleteSensorListener() {
            if (isSensorExist) {
                sensorManager.unregisterListener(GameService.this, accelerometer);
                sensorManager.unregisterListener(GameService.this, magnetometer);
                sensorListener = null;
            }
        }
    }

    public interface GameSensorListener {
        void onOrientationChanged();
    }

    private void checkMoves(float[][] mLastOrientationArr) { // check if there is exceptional move
        float[] vector = avgMoves(mLastOrientationArr);
        for (int i = 0; i < vector.length; i++) {
            if (Math.abs(vector[i] - firstOrientation[i]) < SENSITIVE_OF_CHECKING)
                return;
        }
        sensorListener.onOrientationChanged();
    }

    private float[] avgMoves(float[][] matMoves) { //calculate average of last moves
        float avgArr[] = new float[matMoves[0].length], sum = 0;
        for (int i = 0; i < matMoves[0].length; i++) {
            for (int j = 0; j < matMoves.length; j++) {
                sum += matMoves[j][i];
            }
            avgArr[i] = sum / matMoves.length;
            sum = 0;
        }
        return avgArr;
    }

    private void setLocation() {
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        boolean gps_enabled = false;
        boolean network_enabled = false;
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            lastLocation = setDummy();
            return;
        }
        try {
            gps_enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch(Exception ex) {}
        try {
            network_enabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch(Exception ex) {}

        if(!gps_enabled && !network_enabled) {
            lastLocation = setDummy();
        }
        else{
            Criteria crit = new Criteria();
            crit.setAccuracy(Criteria.ACCURACY_FINE);
            crit.setPowerRequirement(Criteria.NO_REQUIREMENT);
            lastLocation = locationManager.getLastKnownLocation(locationManager.getBestProvider(crit, true));
            if (gps_enabled){
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
            }
            else if (network_enabled){
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
            }
        }
    }

    public Location setDummy (){
        Location lastLocation = new Location("dummy");
        lastLocation.setLatitude(32.113086);
        lastLocation.setLongitude(34.818021);
        return lastLocation;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    @Override
    public void onLocationChanged(Location location) {
        if (location!=null)
            lastLocation = location;
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

    public Location getLastLocation() {
        return lastLocation;
    }
}
