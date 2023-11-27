package uni.tesis.interfazfinal;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Ready_Orden extends AppCompatActivity {
    Button okButton, adminButton;
    String selectLevel;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ready_orden);

        okButton = findViewById(R.id.okButton);
        adminButton = findViewById(R.id.adminButton);

        okButton.setOnClickListener(view -> {
            Intent goTime = new Intent(this,Escucha_Orden.class);
            startActivity(goTime);
        });

        adminButton.setOnClickListener(v -> {
            showAdminOrderDialog();
        });
    }

    private void showAdminOrderDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.customAlertDialogTalkie);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.activity_admin_escucha_orden,null);
        dialogView.setBackgroundResource(R.drawable.rounded_white_background);

        builder.setView(dialogView);

        Spinner spinnerLevel;
        Button saveButton, cancelButton;
        EditText editDuracion,editIntentos;
        String[] level = new String[]{
                "Selecciona un nivel",
                "Nivel 1",
                "Nivel 2",
                "Nivel 3",
                "Nivel 4"
        };

        // Muestra el diálogo
        AlertDialog dialog = builder.create();
        dialog.show();

        spinnerLevel = dialogView.findViewById(R.id.spinnerLevel);
        editDuracion = dialogView.findViewById(R.id.editDuracion);
        editIntentos = dialogView.findViewById(R.id.editIntentos);
        saveButton = dialogView.findViewById(R.id.saveButton);
        cancelButton = dialogView.findViewById(R.id.cancelButton);

        final List<String> devicesList = new ArrayList<>(Arrays.asList(level));
        final ArrayAdapter<String> adapterDevice = new ArrayAdapter<String>(this,R.layout.spinner_item,devicesList){
            @Override
            public boolean isEnabled(int position){
                if(position == 0)
                {
                    return false;
                }
                else
                {
                    return true;
                }
            }
            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView tv = (TextView) view;
                if(position == 0){
                    // Set the hint text color gray
                    tv.setTextColor(Color.GRAY);
                }
                else {
                    tv.setTextColor(Color.BLACK);
                }
                return view;
            }
        };
        adapterDevice.setDropDownViewResource(R.layout.spinner_item);
        spinnerLevel.setAdapter(adapterDevice);

        spinnerLevel.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    //Toast.makeText(Login.this, "Por favor, seleccione una opción", Toast.LENGTH_SHORT).show();
                }else {
                    selectLevel = parent.getItemAtPosition(position).toString();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        saveButton.setOnClickListener(v -> {

        });

        cancelButton.setOnClickListener(v -> {
            dialog.cancel();
        });
    }
}