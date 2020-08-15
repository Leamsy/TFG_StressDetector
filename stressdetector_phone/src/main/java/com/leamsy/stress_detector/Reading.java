package com.leamsy.stress_detector;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.empatica.empalink.ConnectionNotAllowedException;
import com.empatica.empalink.EmpaDeviceManager;
import com.empatica.empalink.EmpaticaDevice;
import com.empatica.empalink.config.EmpaSensorStatus;
import com.empatica.empalink.config.EmpaSensorType;
import com.empatica.empalink.config.EmpaStatus;
import com.empatica.empalink.delegate.EmpaDataDelegate;
import com.empatica.empalink.delegate.EmpaStatusDelegate;
import com.google.android.gms.wearable.MessageClient;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Wearable;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class Reading extends AppCompatActivity implements MessageClient.OnMessageReceivedListener, EmpaDataDelegate, EmpaStatusDelegate {

    //////////////////////     MQTT     ///////////////////////
    MqttAndroidClient mqttAndroidClient;

    String serverUri = "tcp://broker.hivemq.com";

    String clientId = MqttClient.generateClientId();
    String subscriptionTopic = "stress_data";
    String publishTopic = "stress_results";
    ////////////////////////////////////////////////////////////

    Unificado uni = new Unificado();
    Boolean watch_connected = false;
    Boolean empatica_connected = false;
    Boolean server_connected = false;

    String csv;
    int csv_cont=0;

    long time_phone=0;

    int modo=0;

    //Sensores teléfono

    float giros_phone_x;
    float giros_phone_y;
    float giros_phone_z;

    float acele_phone_x;
    float acele_phone_y;
    float acele_phone_z;

    float magne_phone_x;
    float magne_phone_y;
    float magne_phone_z;

    float luz_phone;
    float proxi_phone;

    //Sensores reloj

    long time_watch=0;

    float giros_watch_x;
    float giros_watch_y;
    float giros_watch_z;

    float acele_watch_x;
    float acele_watch_y;
    float acele_watch_z;

    float magne_watch_x;
    float magne_watch_y;
    float magne_watch_z;

    float luz_watch;
    float pulsa_watch;

    //Sensores Empatica

    float acele_empa_x;
    float acele_empa_y;
    float acele_empa_z;

    float bvp_empa;
    float gsr_empa;
    float ibi_empa;
    float temp_empa;

    //Sensores unificados

    class Unificado{
        public long time;

        public float giroscopio_x;
        public float giroscopio_y;
        public float giroscopio_z;

        public float acelerometro_x;
        public float acelerometro_y;
        public float acelerometro_z;

        public float magnetometro_x;
        public float magnetometro_y;
        public float magnetometro_z;

        public float luz;
        public float pulsaciones;

        public float bvp_empa;
        public float gsr_empa;
        public float temp_empa;
    };



    private EmpaDeviceManager deviceManager;
    private SensorManager manager;
    SensorEventListener sel = new SensorEventListener(){
        public void onAccuracyChanged(Sensor sensor, int accuracy) {}
        public void onSensorChanged(SensorEvent event) {

            time_phone = Calendar.getInstance().getTimeInMillis();

            if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
                float[] values = event.values;
                giros_phone_x = values[0];
                giros_phone_y = values[1];
                giros_phone_z = values[2];
            }

            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                float[] values = event.values;
                acele_phone_x = values[0];
                acele_phone_y = values[1];
                acele_phone_z = values[2];
            }

            if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
                float[] values = event.values;
                magne_phone_x = values[0];
                magne_phone_y = values[1];
                magne_phone_z = values[2];
            }

            if (event.sensor.getType() == Sensor.TYPE_LIGHT) {
                float[] values = event.values;
                luz_phone = values[0];
            }

            if (event.sensor.getType() == Sensor.TYPE_PROXIMITY) {
                float[] values = event.values;
                proxi_phone = values[0];
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reading);

        mqttAndroidClient = new MqttAndroidClient(this, serverUri, clientId);

        try {
            mqttAndroidClient.connect().setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    subscribeToTopic();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.d("aa", "Failed to connect to: " + serverUri);
                    Log.d("aa", "" + exception);
                }
            });
        } catch (MqttException ex){
            ex.printStackTrace();
        }

        /////////////////////////////////
        ///PHONE SENSORS
        /////////////////////////////////

        manager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

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



        /////////////////////////////////
        ///WATCH SENSORS
        /////////////////////////////////

        Wearable.getMessageClient(this).addListener(this);


        /////////////////////////////////
        ///EMPATICA SENSORS
        /////////////////////////////////

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
        }

        deviceManager = new EmpaDeviceManager(getApplicationContext(), this, this);
        deviceManager.authenticateWithAPIKey("8830bd44000146d3bcf3e91bfad9931f");



        class enviar extends TimerTask {
            public void run() {
                runOnUiThread(() -> {
                    unificarLecturas();
                    updateText();
                });
            }
        };

        while(Calendar.getInstance().getTimeInMillis()%1000 != 0){
        }

        Timer timer = new Timer();
        timer.schedule(new enviar(), 0, 200);
    }


    /////////////////////////////////
    ///WATCH
    /////////////////////////////////

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {

        if (messageEvent.getPath().equals("/sensores")) {
            try {
                JSONObject respuesta = new JSONObject(new String(messageEvent.getData()));
                watch_connected=true;

                time_watch = respuesta.getLong("time");

                giros_watch_x = (float) respuesta.getDouble("giroscopio_x");
                giros_watch_y = (float) respuesta.getDouble("giroscopio_y");
                giros_watch_z = (float) respuesta.getDouble("giroscopio_z");

                acele_watch_x = (float) respuesta.getDouble("acelerometro_x");
                acele_watch_y = (float) respuesta.getDouble("acelerometro_y");
                acele_watch_z = (float) respuesta.getDouble("acelerometro_z");

                magne_watch_x = (float) respuesta.getDouble("magnetico_x");
                magne_watch_y = (float) respuesta.getDouble("magnetico_y");
                magne_watch_z = (float) respuesta.getDouble("magnetico_z");

                luz_watch = (float) respuesta.getDouble("luz");

                pulsa_watch = (float) respuesta.getDouble("pulsaciones");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    /////////////////////////////////
    ///EMPATICA
    /////////////////////////////////

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
        acele_empa_x = (float) (x*9.80665/64);
        acele_empa_y = (float) (y*9.80665/64);
        acele_empa_z = (float) (z*9.80665/64);
    }

    @Override
    public void didReceiveBVP(float bvp, double timestamp) {
        //Blood Volume Pulse
        bvp_empa = bvp;
    }

    @Override
    public void didReceiveGSR(float gsr, double timestamp) {
        //Electrodermal Activity Sensor
        gsr_empa = gsr;
    }

    @Override
    public void didReceiveIBI(float ibi, double timestamp) {
        //Inter-Beat Interval
        ibi_empa = 60/ibi;
    }

    @Override
    public void didReceiveTemperature(float temp, double timestamp) {
        temp_empa = temp;
    }

    @Override
    public void didReceiveBatteryLevel(float battery, double timestamp) {

    }

    @Override
    public void didReceiveTag(double timestamp) {

    }

    @Override
    public void didEstablishConnection() {

        empatica_connected=true;
    }

    @Override
    public void didUpdateOnWristStatus(@EmpaSensorStatus final int status) {

        runOnUiThread(() -> {
            if (status == EmpaSensorStatus.ON_WRIST) {
                ((TextView) findViewById(R.id.wrist_status_label_empatica)).setText("EN MUÑECA");
            }
            else {
                ((TextView) findViewById(R.id.wrist_status_label_empatica)).setText("NO EN MUÑECA");
            }
        });
    }

    private void unificarLecturas(){

        if(!watch_connected && !empatica_connected){

            uni.giroscopio_x = giros_phone_x;
            uni.giroscopio_y = giros_phone_y;
            uni.giroscopio_z = giros_phone_z;

            uni.acelerometro_x = acele_phone_x;
            uni.acelerometro_y = acele_phone_y;
            uni.acelerometro_z = acele_phone_z;

            uni.magnetometro_x = magne_phone_x;
            uni.magnetometro_y = magne_phone_y;
            uni.magnetometro_z = magne_phone_z;

            uni.luz = luz_phone;
            uni.pulsaciones = 0;

            uni.bvp_empa = 0;
            uni.gsr_empa = 0;
            uni.temp_empa = 0;
        }

        else if(watch_connected && !empatica_connected){

            uni.giroscopio_x = (giros_phone_x + giros_watch_x)/2;
            uni.giroscopio_y = (giros_phone_y + giros_watch_y)/2;
            uni.giroscopio_z = (giros_phone_z + giros_watch_z)/2;

            uni.acelerometro_x = (acele_phone_x + acele_watch_x)/2;
            uni.acelerometro_y = (acele_phone_y + acele_watch_y)/2;
            uni.acelerometro_z = (acele_phone_z + acele_watch_z)/2;

            uni.magnetometro_x = (magne_phone_x + magne_watch_x)/2;
            uni.magnetometro_y = (magne_phone_y + magne_watch_y)/2;
            uni.magnetometro_z = (magne_phone_z + magne_watch_z)/2;

            uni.luz = (luz_phone + luz_watch)/2;
            uni.pulsaciones = pulsa_watch;

            uni.bvp_empa = 0;
            uni.gsr_empa = 0;
            uni.temp_empa = 0;

        }

        else if(!watch_connected && empatica_connected){

            uni.giroscopio_x = giros_phone_x;
            uni.giroscopio_y = giros_phone_y;
            uni.giroscopio_z = giros_phone_z;

            uni.acelerometro_x = (acele_phone_x + acele_empa_x)/2;
            uni.acelerometro_y = (acele_phone_y + acele_empa_y)/2;
            uni.acelerometro_z = (acele_phone_z + acele_empa_z)/2;

            uni.magnetometro_x = magne_phone_x;
            uni.magnetometro_y = magne_phone_y;
            uni.magnetometro_z = magne_phone_z;

            uni.luz = luz_phone;
            uni.pulsaciones = ibi_empa;

            uni.bvp_empa = bvp_empa;
            uni.gsr_empa = gsr_empa;
            uni.temp_empa = temp_empa;
        }

        else if(watch_connected && empatica_connected){

            uni.giroscopio_x = giros_watch_x;
            uni.giroscopio_y = giros_watch_y;
            uni.giroscopio_z = giros_watch_z;

            uni.acelerometro_x = (acele_watch_x + acele_empa_x)/2;
            uni.acelerometro_y = (acele_watch_y + acele_empa_y)/2;
            uni.acelerometro_z = (acele_watch_z + acele_empa_z)/2;

            uni.magnetometro_x = magne_watch_x;
            uni.magnetometro_y = magne_watch_y;
            uni.magnetometro_z = magne_watch_z;

            uni.luz = (luz_phone + luz_watch)/2;
            uni.pulsaciones = pulsa_watch;

            uni.bvp_empa = bvp_empa;
            uni.gsr_empa = gsr_empa;
            uni.temp_empa = temp_empa;
        }

        if(server_connected && watch_connected && empatica_connected){
            publishMessage();
        }
    }

    private void updateText(){

        double mil =  time_phone%1000;
        int seconds = (int) (time_phone / 1000) % 60;
        int minutes = (int) ((time_phone / (1000*60)) % 60);
        int hours   = (int) ((time_phone / (1000*60*60)+2) % 24);


        DecimalFormat df = new DecimalFormat("#####.######");

        TextView time_p = findViewById(R.id.time_devices);
        time_p.setText(String.format("%02d",hours) + ":" + String.format("%02d",minutes) + ":" + String.format("%02d",seconds) + ":" + String.format("%03d",(int)mil));

        TextView giroscopio_p = findViewById(R.id.giroscopio_phone);
        giroscopio_p.setText("x: "+df.format(giros_phone_x)+"\ny: "+df.format(giros_phone_y)+"\nz: "+df.format(giros_phone_z));

        TextView acelerometro_p = findViewById(R.id.acelerometro_phone);
        acelerometro_p.setText("x: "+df.format(acele_phone_x)+"\ny: "+df.format(acele_phone_y)+"\nz: "+df.format(acele_phone_z));

        TextView magnetico_p = findViewById(R.id.magnetico_phone);
        magnetico_p.setText("x: "+df.format(magne_phone_x)+"\ny: "+df.format(magne_phone_y)+"\nz: "+df.format(magne_phone_z));

        TextView luz_p = findViewById(R.id.luz_phone);
        luz_p.setText("" + luz_phone);

        TextView proximidad_p = findViewById(R.id.proximidad_phone);
        proximidad_p.setText("" + proxi_phone);

        ///////////////////////////////////////////////////

        if(watch_connected){
            TextView giroscopio_w = findViewById(R.id.giroscopio_watch);
            giroscopio_w.setText("x: "+df.format(giros_watch_x)+"\ny: "+df.format(giros_watch_y)+"\nz: "+df.format(giros_watch_z));

            TextView acelerometro_w = findViewById(R.id.acelerometro_watch);
            acelerometro_w.setText("x: "+df.format(acele_watch_x)+"\ny: "+df.format(acele_watch_y)+"\nz: "+df.format(acele_watch_z));

            TextView magnetometro_w = findViewById(R.id.magnetico_watch);
            magnetometro_w.setText("x: "+df.format(magne_watch_x)+"\ny: "+df.format(magne_watch_y)+"\nz: "+df.format(magne_watch_z));

            TextView luz_w = findViewById(R.id.luz_watch);
            luz_w.setText("" + luz_watch);

            TextView pulsaciones_w = findViewById(R.id.pulsaciones_watch);
            pulsaciones_w.setText("" + pulsa_watch);
        }

        ///////////////////////////////////////////////////

        if(empatica_connected){
            TextView acelerometro_e = findViewById(R.id.acelerometro_empatica);
            acelerometro_e.setText("x: "+ acele_empa_x +"\ny: "+ acele_empa_y +"\nz: "+ acele_empa_z);

            TextView bvp_e = findViewById(R.id.bvp_empatica);
            bvp_e.setText("" + bvp_empa);

            TextView gsr_e = findViewById(R.id.gsr_empatica);
            gsr_e.setText("" + gsr_empa);

            TextView ibi_e = findViewById(R.id.ibi_empatica);
            ibi_e.setText("" + ibi_empa);

            TextView temp_e = findViewById(R.id.temp_empatica);
            temp_e.setText("" + temp_empa);
        }

        ///////////////////////////////////////////////////

        TextView giroscopio_u = findViewById(R.id.giroscopio_uni);
        giroscopio_u.setText("x: "+ df.format(uni.giroscopio_x) +"\ny: "+ df.format(uni.giroscopio_y) +"\nz: "+ df.format(uni.giroscopio_z));

        TextView acelerometro_u = findViewById(R.id.acelerometro_uni);
        acelerometro_u.setText("x: "+ df.format(uni.acelerometro_x) +"\ny: "+ df.format(uni.acelerometro_y) +"\nz: "+ df.format(uni.acelerometro_z));

        TextView magnetometro_u = findViewById(R.id.magnetometro_uni);
        magnetometro_u.setText("x: "+ df.format(uni.magnetometro_x) +"\ny: "+ df.format(uni.magnetometro_y) +"\nz: "+ df.format(uni.magnetometro_z));

        TextView luz_u = findViewById(R.id.luz_uni);
        luz_u.setText("" + uni.luz);

        TextView pulsaciones_u = findViewById(R.id.pulsaciones_uni);
        pulsaciones_u.setText("" + uni.pulsaciones);

        TextView bvp_u = findViewById(R.id.bvp_uni);
        bvp_u.setText("" + uni.bvp_empa);

        TextView gsr_u = findViewById(R.id.gsr_uni);
        gsr_u.setText("" + uni.gsr_empa);

        TextView temp_u = findViewById(R.id.temp_uni);
        temp_u.setText("" + uni.temp_empa);

    }

    public void subscribeToTopic(){
        try {
            mqttAndroidClient.subscribe(publishTopic, 0, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.d("aa", "Subscribed!");
                    server_connected=true;
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.d("aa", "Failed to subscribe");
                }
            });

            mqttAndroidClient.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {

                }

                @Override
                public void messageArrived(String topic, MqttMessage message) throws Exception {
                    Log.d("tag","message>>" + new String(message.getPayload()));
                    Log.d("tag","topic>>" + topic);

                    findViewById(R.id.gif).setVisibility(View.GONE);
                    if(new String(message.getPayload()).equals("0")){
                        ImageView img = findViewById(R.id.results);
                        img.setVisibility(View.VISIBLE);
                        img.setImageResource(R.drawable.relajado);
                    }
                    if(new String(message.getPayload()).equals("1")){
                        ImageView img = findViewById(R.id.results);
                        img.setVisibility(View.VISIBLE);
                        img.setImageResource(R.drawable.estresado);
                    }
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {

                }
            });


        } catch (MqttException ex){
            System.err.println("Exception whilst subscribing");
            ex.printStackTrace();
        }
    }

    public void publishMessage(){
        Log.d("aa", "" + csv_cont);
        if(csv_cont==0){
            csv = "pulsaciones, gsr, temperatura";
            csv_cont++;
        }
        else{
            csv += System.lineSeparator() + (double)uni.pulsaciones + "," + (double)uni.gsr_empa + "," + (double)uni.temp_empa + "," + modo;
            csv_cont++;
        }

        if(csv_cont==26){
            csv_cont=0;
            Log.d("aa", "" + csv);
            try {
                MqttMessage message = new MqttMessage();

                message.setPayload(csv.getBytes());
                mqttAndroidClient.publish(subscriptionTopic, message);
                Log.d("aa", "Message Published");
                csv = "";
                if(!mqttAndroidClient.isConnected()){
                    Log.d("aa", mqttAndroidClient.getBufferedMessageCount() + " messages in buffer.");
                }
            } catch (MqttException e) {
                System.err.println("Error Publishing: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}