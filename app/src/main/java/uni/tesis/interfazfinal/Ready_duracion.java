package uni.tesis.interfazfinal;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

public class Ready_duracion extends AppCompatActivity {
    Button ok;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ready_duracion);
        ok=findViewById(R.id.ok);
        ok.setOnClickListener(view -> {
            Intent goTime = new Intent(this,duracion.class);
            startActivity(goTime);
        });
    }
}