package uni.tesis.interfazfinal;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class cofi_dur extends AppCompatActivity {

    private EditText ingresarTiempoEditText;
    private EditText ingresarTiempoSilencio;
    private Button salir;
    private EditText Cuantos;
    private String vocalT;
    private Spinner spinnerd;
    private  Button inicioButton;
    private String selectedOption;
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cofi_dur);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        inicioButton = findViewById(R.id.inicioButton);
        ingresarTiempoEditText = findViewById(R.id.ingresarTiempoEditText);
        ingresarTiempoSilencio = findViewById(R.id.ingresarTiempoSilencio);
        Cuantos = findViewById(R.id.Cuantos);
        salir = findViewById(R.id.salir);
        inicioButton.setEnabled(false);



        salir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(cofi_dur.this, duracion.class);
                startActivity(intent);
            }
        });
        ingresarTiempoEditText.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int i, int i1, int i2) {
                if (s.toString().trim().isEmpty()) {
                    inicioButton.setEnabled(false);
                } else {
                    try {
                        int valor = Integer.parseInt(s.toString());
                        if (valor >= 1 && valor <= 10) {
                            inicioButton.setEnabled(true);
                        } else {
                            inicioButton.setEnabled(false);
                        }
                    } catch (NumberFormatException e) {
                        inicioButton.setEnabled(false);
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        ingresarTiempoSilencio.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int i, int i1, int i2) {
                if (s.toString().trim().isEmpty()) {
                    inicioButton.setEnabled(false);
                } else {
                    try {
                        int valor = Integer.parseInt(s.toString());
                        if (valor >= 1 && valor <= 10) {
                            inicioButton.setEnabled(true);
                        } else {
                            inicioButton.setEnabled(false);
                        }
                    } catch (NumberFormatException e) {
                        inicioButton.setEnabled(false);
                    }
                }

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        Cuantos.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int i, int i1, int i2) {
                if (s.toString().trim().isEmpty()) {
                    inicioButton.setEnabled(false);
                } else {
                    try {
                        int valor = Integer.parseInt(s.toString());
                        if (valor >= 1 && valor <= 10) {
                            inicioButton.setEnabled(true);
                        } else {
                            inicioButton.setEnabled(false);
                        }
                    } catch (NumberFormatException e) {
                        inicioButton.setEnabled(false);
                    }
                }

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        inicioButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validarCampos();
                String tiempo_dur_voz = ingresarTiempoEditText.getText().toString();
                String cuantos = Cuantos.getText().toString();
                String tiempo_dur_sil = ingresarTiempoSilencio.getText().toString();



                SharedPreferences preferences = getSharedPreferences("mis_datos", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                try {
                    int sostenidoInt = Integer.parseInt(tiempo_dur_voz);
                    editor.putInt("tiempo_voz", sostenidoInt);
                } catch (NumberFormatException e) {
                }

                try {
                    int vecesInt = Integer.parseInt(cuantos);
                    editor.putInt("num", vecesInt);
                } catch (NumberFormatException e) {
                }

                try {
                    int tsilencioInt = Integer.parseInt(tiempo_dur_sil);
                    editor.putInt("tiempo_sil", tsilencioInt);
                } catch (NumberFormatException e) {
                }
                editor.apply();
            }
        });
    }
    void validarCampos() {
        String selectedItem = spinnerd.getSelectedItem().toString();
        String sostenidoValue = ingresarTiempoEditText.getText().toString();
        String vecesValue = Cuantos.getText().toString();
        String tsilencioValue = ingresarTiempoSilencio.getText().toString();

        boolean camposLlenos = !selectedItem.isEmpty() &&
                !sostenidoValue.trim().isEmpty() &&
                !vecesValue.trim().isEmpty() &&
                !tsilencioValue.trim().isEmpty();

        boolean sostenidoValido = false;
        boolean vecesValido = false;
        boolean tsilencioValido = false;

        if (camposLlenos) {
            try {
                int sostenidoInt = Integer.parseInt(sostenidoValue);
                sostenidoValido = (sostenidoInt >= 1 && sostenidoInt <= 10);

                int vecesInt = Integer.parseInt(vecesValue);
                vecesValido = (vecesInt >= 1 && vecesInt <= 10);

                int tsilencioInt = Integer.parseInt(tsilencioValue);
                tsilencioValido = (tsilencioInt >= 1 && tsilencioInt <= 10);
            } catch (NumberFormatException e) {
            }
        }
        boolean camposValidos = camposLlenos && sostenidoValido && vecesValido && tsilencioValido;
        inicioButton.setEnabled(camposValidos);
        if (!camposValidos) {
            Toast.makeText(getApplicationContext(), "Por favor, complete todos los campos correctamente", Toast.LENGTH_SHORT).show();
        }
        inicioButton.setEnabled(camposLlenos && sostenidoValido && vecesValido && tsilencioValido);
    }
}
