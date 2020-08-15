package com.leamsy.stressdetector;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.icu.util.Calendar;
import android.os.Bundle;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.gms.wearable.CapabilityClient;
import com.google.android.gms.wearable.CapabilityInfo;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;
import com.leamsy.stressdetector.R;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.view.View;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;

public class MainActivity extends FragmentActivity {

    private Context context = this;
    private String nodeId;
    public static final String MESSAGE_PATH = "/sensores";
    JSONObject sensores = new JSONObject();
    private SensorManager manager;
    DecimalFormat df = new DecimalFormat("#.####");
    DecimalFormat df_pulsaciones = new DecimalFormat("#.##");

    SensorEventListener sel = new SensorEventListener(){
        public void onAccuracyChanged(Sensor sensor, int accuracy) {}
        public void onSensorChanged(SensorEvent event) {

            try {
                sensores.put("time", Calendar.getInstance().getTimeInMillis());

                TextView time = findViewById(R.id.time);
                time.setText("\n" + Calendar.getInstance().getTimeInMillis());
            } catch (JSONException e) {
                e.printStackTrace();
            }

            if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
                float[] values = event.values;

                try {
                    sensores.put("giroscopio_x", values[0]);
                    sensores.put("giroscopio_y", values[1]);
                    sensores.put("giroscopio_z", values[2]);

                    TextView giroscopio = findViewById(R.id.giroscopio);
                    giroscopio.setText("Giroscopio\n\nx: "+df.format(values[0])+"\ny: "+df.format(values[1])+"\nz: "+df.format(values[2]));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                float[] values = event.values;

                try {
                    sensores.put("acelerometro_x", values[0]);
                    sensores.put("acelerometro_y", values[1]);
                    sensores.put("acelerometro_z", values[2]);

                    TextView acelerometro = findViewById(R.id.acelerometro);
                    acelerometro.setText("Acelerómetro\n\nx: "+df.format(values[0])+"\ny: "+df.format(values[1])+"\nz: "+df.format(values[2]));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
                float[] values = event.values;

                try {
                    sensores.put("magnetico_x", values[0]);
                    sensores.put("magnetico_y", values[1]);
                    sensores.put("magnetico_z", values[2]);

                    TextView magnetico = findViewById(R.id.magnetico);
                    magnetico.setText("Magnetómetro\n\nx: "+df.format(values[0])+"\ny: "+df.format(values[1])+"\nz: "+df.format(values[2]));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            if (event.sensor.getType() == Sensor.TYPE_LIGHT) {
                float[] values = event.values;

                try {
                    sensores.put("luz", values[0]);

                    TextView luz = findViewById(R.id.luz);
                    luz.setText("Luz\n\n" + values[0]);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            if (event.sensor.getType() == Sensor.TYPE_HEART_RATE) {
                float[] values = event.values;

                TextView pulsaciones = findViewById(R.id.pulsaciones);
                pulsaciones.setText("Pulsaciones\n\n" + df_pulsaciones.format(values[0]));

                try {
                    sensores.put("pulsaciones", values[0]);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            sensores.put("pulsaciones", "");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        df.setRoundingMode(RoundingMode.CEILING);
        df_pulsaciones.setRoundingMode(RoundingMode.CEILING);

        new Thread(new Runnable() {
            @Override
            public void run() {
                CapabilityInfo capabilityInfo = null;
                try {
                    capabilityInfo = Tasks.await(
                            Wearable.getCapabilityClient(context).getCapability(
                                    "sensor_reception", CapabilityClient.FILTER_REACHABLE));
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                // capabilityInfo has the reachable nodes with the transcription capability
                updateCapabilities(capabilityInfo);

            }
        }).start();

        manager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        List<Sensor> deviceSensors = manager.getSensorList(Sensor.TYPE_ALL);

        StringBuilder strBuilder = new StringBuilder();
        for(Sensor s: deviceSensors){
            strBuilder.append(s.getName()+"\n");
        }

        try {
            sensores.put("lista", strBuilder);
        } catch (JSONException e) {
            e.printStackTrace();
        }

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

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.BODY_SENSORS)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.BODY_SENSORS)) {
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.BODY_SENSORS},
                        0);

                List pulsaciones = manager.getSensorList(Sensor.TYPE_HEART_RATE);
                if(pulsaciones.size()>0) {
                    manager.registerListener(sel, (Sensor) pulsaciones.get(0), SensorManager.SENSOR_DELAY_FASTEST);
                }
            }
        } else {
            List pulsaciones = manager.getSensorList(Sensor.TYPE_HEART_RATE);
            if(pulsaciones.size()>0) {
                manager.registerListener(sel, (Sensor) pulsaciones.get(0), SensorManager.SENSOR_DELAY_FASTEST);
            }
        }


        class enviar extends TimerTask {
            public void run() {
                send();
            }
        };


        while(Calendar.getInstance().getTimeInMillis()%1000 != 0){
        }

        Timer timer = new Timer();
        timer.schedule(new enviar(), 0, 200);

    }

    private void updateCapabilities(CapabilityInfo capabilityInfo) {
        Set<Node> connectedNodes = capabilityInfo.getNodes();

        String bestNodeId = null;
        // Find a nearby node or pick one arbitrarily
        for (Node node : connectedNodes) {
            if (node.isNearby()) {
                nodeId = node.getId();
            }
            bestNodeId = node.getId();
        }
        nodeId = bestNodeId;
    }

    private void send() {

        if (nodeId != null) {
            byte[] data = sensores.toString().getBytes();
            Task<Integer> sendTask = Wearable.getMessageClient(context).sendMessage(nodeId, MESSAGE_PATH, data);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        manager.unregisterListener(sel);
    }


    public void salir(View view) {
        android.os.Process.killProcess(android.os.Process.myPid());
    }
}