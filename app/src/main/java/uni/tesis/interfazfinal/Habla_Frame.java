package uni.tesis.interfazfinal;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

public class Habla_Frame extends AppCompatActivity {

    Button talkIntensity, backButton,HtiempoButton,HfrecuenciaButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_habla_frame);

        talkIntensity = findViewById(R.id.intensityButton);
        backButton = findViewById(R.id.backButton);
        HfrecuenciaButton=findViewById(R.id.HfrecuenciaButton);
        HtiempoButton=findViewById(R.id.HtiempoButton);

        talkIntensity.setOnClickListener(view -> {
            Intent goIntensity = new Intent(this,Ready_Int.class);
            startActivity(goIntensity);
        });

        backButton.setOnClickListener(v -> {
            Intent goMenu = new Intent(this, MainActivity.class);
            startActivity(goMenu);
        });

        HtiempoButton.setOnClickListener(v -> {
            Intent duracion =new Intent(this,repro_dur_largo.class);
            startActivity(duracion);

        });
        HfrecuenciaButton.setOnClickListener(v -> {
            Intent tono =new Intent(this,repro_staccato.class);
            startActivity(tono);
        });

    }
}