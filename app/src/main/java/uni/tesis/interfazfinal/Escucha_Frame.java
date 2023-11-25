package uni.tesis.interfazfinal;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

public class Escucha_Frame extends AppCompatActivity {

    Button hearTimeButton, hearFreqButton, hearOrderButton, hearVowelsButton, backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_escucha_frame);

        hearTimeButton = findViewById(R.id.timeButton);
        hearFreqButton = findViewById(R.id.freqButton);
        hearOrderButton = findViewById(R.id.orderButton);
        hearVowelsButton = findViewById(R.id.vowelsButton);
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

        hearVowelsButton.setOnClickListener(view -> {
            Intent goVowels = new Intent(this, Escucha_Vocales.class);
            startActivity(goVowels);
        });

        backButton.setOnClickListener(v -> {
            Intent goMenu = new Intent(this, MainActivity.class);
            startActivity(goMenu);
        });


    }
}