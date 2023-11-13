package uni.tesis.interfazfinal;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.media.tv.TvContract;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {

    private String TAG = "TAG";
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private final String USERS_COLLECTION = "Usuarios";

    private Button escuchaButton, hablaButton, vibrometriaButton, addButton;
    private TextView nameTittle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        escuchaButton = findViewById(R.id.sensButton);
        hablaButton = findViewById(R.id.talkButton);
        vibrometriaButton = findViewById(R.id.eqButton);
        addButton = findViewById(R.id.addButton);
        nameTittle = findViewById(R.id.mainTittle);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        if(mAuth.getCurrentUser().getEmail().equals("admin@app.com")){
        }

        db.collection(USERS_COLLECTION).document(mAuth.getCurrentUser().getEmail()).get().addOnSuccessListener(documentSnapshot ->
                nameTittle.setText("Hola " + documentSnapshot.getString("Nombre")));

        escuchaButton.setOnClickListener(view -> {
            Intent goEscucha = new Intent(this, Escucha_Frame.class);
            startActivity(goEscucha);
        });

        hablaButton.setOnClickListener(view -> {
            Intent goHabla = new Intent(this, Habla_Frame.class);
            startActivity(goHabla);
        });

        vibrometriaButton.setOnClickListener(view -> {
            Intent goVibrometria = new Intent(this, Vibrometria.class);
            startActivity(goVibrometria);
        });

        addButton.setOnClickListener(view -> {
            Intent goAdd = new Intent(this, Add_Audios_Frame.class);
            startActivity(goAdd);
        });

    }


}