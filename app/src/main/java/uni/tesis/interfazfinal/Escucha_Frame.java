package uni.tesis.interfazfinal;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

public class Escucha_Frame extends AppCompatActivity {

    Button hearTimeButton, hearFreqButton, hearOrderButton, hearVowelsButton, backButton;
    Points myApp;
    long startTime;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_escucha_frame);
       // myApp = (Points) getApplication();

        // Inicia la sesión y registra la marca de tiempo al inicio
        //myApp.startSession();
        startTime = System.currentTimeMillis();
        hearTimeButton = findViewById(R.id.timeButton);
        hearFreqButton = findViewById(R.id.freqButton);
        hearOrderButton = findViewById(R.id.orderButton);
        //hearVowelsButton = findViewById(R.id.vowelsButton);
        backButton = findViewById(R.id.backButton);

        hearTimeButton.setOnClickListener(view -> {
            Intent goTime = new Intent(this, Ready_Tiempo.class);
            startActivity(goTime);
        });

        hearFreqButton.setOnClickListener(view -> {
            Intent goFreq = new Intent(this, Ready_frec1.class);
            startActivity(goFreq);
        });

        hearOrderButton.setOnClickListener(view -> {
            Intent goOrder = new Intent(this, Ready_Orden.class);
            startActivity(goOrder);
        });


        backButton.setOnClickListener(v -> {
            Intent goMenu = new Intent(this, MainActivity.class);
            startActivity(goMenu);
        });


    }
    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Finaliza la sesión y registra la marca de tiempo al cerrar la actividad
        //myApp.endSession();

        // Resto del código...
    }
}