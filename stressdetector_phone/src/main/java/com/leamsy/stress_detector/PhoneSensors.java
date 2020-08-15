package com.leamsy.stress_detector;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import java.util.List;

public class PhoneSensors extends AppCompatActivity {


    private SensorManager manager;
    SensorEventListener sel = new SensorEventListener(){
        public void onAccuracyChanged(Sensor sensor, int accuracy) {}
        public void onSensorChanged(SensorEvent event) {

            if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
                float[] values = event.values;
                TextView giroscopio = findViewById(R.id.giroscopio);

                giroscopio.setText("GIROSCOPIO\n\nx: "+values[0]+"\ny: "+values[1]+"\nz: "+values[2]);
            }

            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                float[] values = event.values;
                TextView acelerometro = findViewById(R.id.acelerometro);

                acelerometro.setText("ACELERÓMETRO\n\nx: "+values[0]+"\ny: "+values[1]+"\nz: "+values[2]);
            }

            if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
                float[] values = event.values;
                TextView magnetico = findViewById(R.id.magnetico);

                magnetico.setText("MAGNETÓMETRO\n\nx: "+values[0]+"\ny: "+values[1]+"\nz: "+values[2]);
            }

            if (event.sensor.getType() == Sensor.TYPE_LIGHT) {
                float[] values = event.values;
                TextView luz = findViewById(R.id.luz);

                luz.setText("LUZ\n\n"+values[0]);
            }

            if (event.sensor.getType() == Sensor.TYPE_PROXIMITY) {
                float[] values = event.values;
                TextView proximidad = findViewById(R.id.proximidad);

                proximidad.setText("PROXIMIDAD\n\n"+values[0]);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reading);

        manager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        List<Sensor> deviceSensors = manager.getSensorList(Sensor.TYPE_ALL);

        Log.d("aa", "" + deviceSensors.isEmpty());

        TextView lista = findViewById(R.id.lista);

        StringBuilder strBuilder = new StringBuilder();
        for(Sensor s: deviceSensors){
            strBuilder.append(s.getName()+"\n");
        }

        lista.setText(strBuilder);

        List giroscopio = manager.getSensorList(Sensor.TYPE_GYROSCOPE);
        if(giroscopio.size()>0) {
            manager.registerListener(sel, (Sensor) giroscopio.get(0), SensorManager.SENSOR_DELAY_NORMAL);
        }

        List acelerometro = manager.getSensorList(Sensor.TYPE_ACCELEROMETER);
        if(acelerometro.size()>0) {
            manager.registerListener(sel, (Sensor) acelerometro.get(0), SensorManager.SENSOR_DELAY_NORMAL);
        }

        List magnetico = manager.getSensorList(Sensor.TYPE_MAGNETIC_FIELD);
        if(magnetico.size()>0) {
            manager.registerListener(sel, (Sensor) magnetico.get(0), SensorManager.SENSOR_DELAY_NORMAL);
        }

        List luz = manager.getSensorList(Sensor.TYPE_LIGHT);
        if(luz.size()>0) {
            manager.registerListener(sel, (Sensor) luz.get(0), SensorManager.SENSOR_DELAY_NORMAL);
        }

        List proximidad = manager.getSensorList(Sensor.TYPE_PROXIMITY);
        if(proximidad.size()>0) {
            manager.registerListener(sel, (Sensor) proximidad.get(0), SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

}