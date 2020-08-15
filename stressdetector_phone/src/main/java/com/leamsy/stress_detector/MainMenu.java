package com.leamsy.stress_detector;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

public class MainMenu extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);
    }

    public void leer(android.view.View V){
        Intent intent = new Intent(this, Reading.class);
        startActivity(intent);
    }

    public void goPhone(android.view.View V){
        Intent intent = new Intent(this, PhoneSensors.class);
        startActivity(intent);
    }

    public void goWatch(android.view.View V){
        Intent intent = new Intent(this, WatchSensors.class);
        startActivity(intent);
    }

    public void goEmpatica(android.view.View V){
        Intent intent = new Intent(this, EmpaticaSensors.class);
        startActivity(intent);
    }


}
