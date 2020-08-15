package com.leamsy.stress_detector;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

import com.google.android.gms.wearable.MessageClient;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Wearable;

import org.json.JSONException;
import org.json.JSONObject;

public class WatchSensors extends Activity implements MessageClient.OnMessageReceivedListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_watch_sensors);

        Wearable.getMessageClient(this).addListener(this);
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        if (messageEvent.getPath().equals("/sensores")) {
            try {
                JSONObject respuesta = new JSONObject(new String(messageEvent.getData()));

                TextView lista = findViewById(R.id.lista);
                lista.setText(respuesta.getString("lista"));

                TextView time = findViewById(R.id.time);
                time.setText(respuesta.getString("time"));


                TextView giroscopio = findViewById(R.id.giroscopio);
                giroscopio.setText("\nGiroscopio\n\nx: "+respuesta.getString("giroscopio_x")+"\ny: "+respuesta.getString("giroscopio_y")+"\nz: "+respuesta.getString("giroscopio_z"));

                TextView acelerometro = findViewById(R.id.acelerometro);
                acelerometro.setText("\nAcelerómetro\n\nx: "+respuesta.getString("acelerometro_x")+"\ny: "+respuesta.getString("acelerometro_y")+"\nz: "+respuesta.getString("acelerometro_z"));

                TextView magnetometro = findViewById(R.id.magnetico);
                magnetometro.setText("\nMagnetómetro\n\nx: "+respuesta.getString("magnetico_x")+"\ny: "+respuesta.getString("magnetico_y")+"\nz: "+respuesta.getString("magnetico_z"));

                TextView luz = findViewById(R.id.luz);
                luz.setText("\nLuz\n\n"+respuesta.getString("luz"));

                TextView pulsaciones = findViewById(R.id.pulsaciones);
                pulsaciones.setText("\nPulsaciones\n\n"+respuesta.getString("pulsaciones"));


            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }


}
