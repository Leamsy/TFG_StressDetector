package com.leamsy.stress_detector;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.empatica.empalink.ConnectionNotAllowedException;
import com.empatica.empalink.EmpaDeviceManager;
import com.empatica.empalink.EmpaticaDevice;
import com.empatica.empalink.config.EmpaSensorStatus;
import com.empatica.empalink.config.EmpaSensorType;
import com.empatica.empalink.config.EmpaStatus;
import com.empatica.empalink.delegate.EmpaDataDelegate;
import com.empatica.empalink.delegate.EmpaStatusDelegate;

public class EmpaticaSensors extends AppCompatActivity implements EmpaDataDelegate, EmpaStatusDelegate {

    private EmpaDeviceManager deviceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_empatica_sensors);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
        }

        deviceManager = new EmpaDeviceManager(getApplicationContext(), this, this);
        deviceManager.authenticateWithAPIKey("8830bd44000146d3bcf3e91bfad9931f");
    }

    @Override
    public void didDiscoverDevice(EmpaticaDevice bluetoothDevice, String deviceName, int rssi, boolean allowed) {
        // Check if the discovered device can be used with your API key. If allowed is always false,
        // the device is not linked with your API key. Please check your developer area at
        // https://www.empatica.com/connect/developer.php
        if (allowed) {
            // Stop scanning. The first allowed device will do.
            deviceManager.stopScanning();
            try {
                // Connect to the device
                deviceManager.connectDevice(bluetoothDevice);
            } catch (ConnectionNotAllowedException e) {
                // This should happen only if you try to connect when allowed == false.
            }
        }
    }

    @Override
    public void didFailedScanning(int errorCode) {

    }

    @Override
    public void didRequestEnableBluetooth() {

    }

    @Override
    public void bluetoothStateChanged() {

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
    }


    @Override
    public void didUpdateSensorStatus(@EmpaSensorStatus int status, EmpaSensorType type) {

        didUpdateOnWristStatus(status);
    }

    @Override
    public void didUpdateStatus(EmpaStatus status) {
        Log.d("aa", status.toString());
        // The device manager is ready for use
        if (status == EmpaStatus.READY) {

            deviceManager.startScanning();

        } else if (status == EmpaStatus.CONNECTED) {

        } else if (status == EmpaStatus.DISCONNECTED) {

        }
    }

    @Override
    public void didReceiveAcceleration(int x, int y, int z, double timestamp) {
        actualizarVista("acelerometro", "\nAcelerómetro: x: "+ x +" y: "+ y +" z: "+ z, timestamp);
    }

    @Override
    public void didReceiveBVP(float bvp, double timestamp) {
        //Blood Volume Pulse
        actualizarVista("bvp", "\nBlood Volume Pulse: " + bvp, timestamp);
    }

    @Override
    public void didReceiveBatteryLevel(float battery, double timestamp) {

    }

    @Override
    public void didReceiveGSR(float gsr, double timestamp) {
        //Electrodermal Activity Sensor
        actualizarVista("gsr", "\nElectrodermal Activity Sensor: " + gsr, timestamp);
    }

    @Override
    public void didReceiveIBI(float ibi, double timestamp) {
        //Inter-Beat Interval
        actualizarVista("ibi", "\nInter-Beat Interval: " + ibi, timestamp);
    }

    @Override
    public void didReceiveTemperature(float temp, double timestamp) {
        actualizarVista("temp", "\nTemperatura: " + temp, timestamp);
    }

    @Override
    public void didReceiveTag(double timestamp) {

    }

    @Override
    public void didEstablishConnection() {

    }

    @Override
    public void didUpdateOnWristStatus(@EmpaSensorStatus final int status) {

        if (status == EmpaSensorStatus.ON_WRIST) {
            ((TextView) findViewById(R.id.wrist_status_label)).setText("EN MUÑECA");
        }
        else {
            ((TextView) findViewById(R.id.wrist_status_label)).setText("NO EN MUÑECA");
        }
    }

    private void actualizarVista(String tipo, String texto, double time){

        runOnUiThread(() -> {
            TextView time_view = findViewById(R.id.time);
            time_view.setText("" + time);

            if(tipo == "acelerometro"){
                TextView acelerometro = findViewById(R.id.acelerometro);
                acelerometro.setText(texto);
            }
            if(tipo == "bvp"){
                TextView bvp = findViewById(R.id.bvp);
                bvp.setText(texto);
            }
            if(tipo == "gsr"){
                TextView gsr = findViewById(R.id.gsr);
                gsr.setText(texto);
            }
            if(tipo == "ibi"){
                TextView ibi = findViewById(R.id.ibi);
                ibi.setText(texto);
            }
            if(tipo == "temp"){
                TextView temp = findViewById(R.id.temp);
                temp.setText(texto);
            }
        });
    }
}
