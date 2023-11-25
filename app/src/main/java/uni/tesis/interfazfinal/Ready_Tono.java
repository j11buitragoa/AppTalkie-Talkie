package uni.tesis.interfazfinal;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

public class Ready_Tono extends AppCompatActivity {
     private Button ok;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ready_tono);
        ok=findViewById(R.id.ok);
        ok.setOnClickListener(view -> {
            Intent goTime = new Intent(this,tono.class);
            startActivity(goTime);
        });

    }
}