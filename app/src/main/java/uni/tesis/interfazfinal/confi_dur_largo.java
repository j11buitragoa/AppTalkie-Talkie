package uni.tesis.interfazfinal;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class confi_dur_largo extends AppCompatActivity {
    private Handler handler = new Handler();
    private Button buttonstart,listagrab,tono;
    private Spinner spinner_voc;
    private EditText sil_time,dur_time,veces;
    private long tiempoInicio;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confi_dur_largo);
        tiempoInicio = System.currentTimeMillis();

        spinner_voc=findViewById(R.id.spinner_voc);
        buttonstart=findViewById(R.id.buttonstart);
        sil_time=findViewById(R.id.sil_time);
        listagrab=findViewById(R.id.listagrab);
        tono=findViewById(R.id.tonos);
        dur_time=findViewById(R.id.dur_time);
        veces=findViewById(R.id.veces);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.vocales_array,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_voc.setAdapter(adapter);
        spinner_voc.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String selectedVocal = (String) adapterView.getSelectedItem();
                Toast.makeText(confi_dur_largo.this, "Vocal seleccionada: " + selectedVocal, Toast.LENGTH_SHORT).show();
                SharedPreferences preferences = getSharedPreferences("MyPreferences", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                String fileName = preferences.getString(selectedVocal, "");
                editor.putString("selectedVocal", selectedVocal);
                editor.putString("file_name", fileName);

                editor.apply();



                if (!TextUtils.isEmpty(fileName)) {
                    // Aquí puedes utilizar el nombre de la grabación asociado a la vocal seleccionada
                    Toast.makeText(confi_dur_largo.this, "Nombre de la grabación asociado a " + selectedVocal + ": " + fileName, Toast.LENGTH_SHORT).show();

                } else {
                    Toast.makeText(confi_dur_largo.this, "No hay grabación asociada a " + selectedVocal, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                Toast.makeText(confi_dur_largo.this, "No selecciono ninguna vocal  " , Toast.LENGTH_SHORT).show();

            }
        });

        sil_time.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence s, int i, int i1, int i2) {
                if (s.toString().trim().isEmpty()) {
                    buttonstart.setEnabled(false);
                } else {
                    try {
                        int valor = Integer.parseInt(s.toString());
                        if (valor >= 1 && valor <= 10) {
                            buttonstart.setEnabled(true);
                        } else {
                            buttonstart.setEnabled(false);
                        }
                    } catch (NumberFormatException e) {
                        buttonstart.setEnabled(false);
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        dur_time.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence s, int i, int i1, int i2) {
                if (s.toString().trim().isEmpty()) {
                    buttonstart.setEnabled(false);
                } else {
                    try {
                        int valor = Integer.parseInt(s.toString());
                        if (valor >= 1 && valor <= 10) {
                            buttonstart.setEnabled(true);
                        } else {
                            buttonstart.setEnabled(false);
                        }
                    } catch (NumberFormatException e) {
                        buttonstart.setEnabled(false);
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        veces.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence s, int i, int i1, int i2) {
                if (s.toString().trim().isEmpty()) {
                    buttonstart.setEnabled(false);
                } else {
                    try {
                        int valor = Integer.parseInt(s.toString());
                        if (valor >= 1 && valor <= 10) {
                            buttonstart.setEnabled(true);
                        } else {
                            buttonstart.setEnabled(false);
                        }
                    } catch (NumberFormatException e) {
                        buttonstart.setEnabled(false);
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        buttonstart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences preferences = getSharedPreferences("MyPreferences", Context.MODE_PRIVATE);
                String selectedVocal = preferences.getString("selectedVocal", "LA");
                String veces_s=veces.getText().toString();
                String silc_time=sil_time.getText().toString();
                String dura_time=dur_time.getText().toString();
                Log.d("Envio",selectedVocal);

                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("selectedVocal", selectedVocal);
                editor.putString("veces", veces_s);
                editor.putString("silc_time", silc_time);
                Log.d("ENVIO",silc_time);
                editor.putString("dura_time", dura_time);
                editor.apply();

                Intent intent = new Intent(confi_dur_largo.this, repro_dur_largo.class);
                startActivity(intent);
                finish();
            }
        });

        listagrab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mostrarListaGrabaciones();

            }
        });

    }
    private void mostrarListaGrabaciones() {
        // Obtener la lista de grabaciones (de alguna manera, por ejemplo, desde el sistema de archivos)
        List<GrabacionModel> grabaciones = obtenerListaDeGrabaciones();

        // Crear un adaptador para el ListView
        GrabacionAdapter adapter = new GrabacionAdapter(this, grabaciones);

        // Crear y configurar el diálogo
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Lista de Grabaciones");
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_grabaciones, null);
        builder.setView(dialogView);

        ListView listViewGrabaciones = dialogView.findViewById(R.id.listViewGrabacionesDialog);
        listViewGrabaciones.setAdapter(adapter);

        // Asignar un OnItemClickListener al ListView
        listViewGrabaciones.setOnItemClickListener((parent, view, position, id) -> {
            // Manejar la selección del elemento de la lista
            GrabacionModel grabacionSeleccionada = adapter.getItem(position);
            if (grabacionSeleccionada != null) {
                // Aquí puedes cargar y reproducir la grabación seleccionada
                String nombreGrabacion = grabacionSeleccionada.getNombre();

            }
        });

        builder.setPositiveButton("Cerrar", (dialog, which) -> dialog.dismiss());

        // Mostrar el diálogo
        builder.create().show();
    }
    private List<GrabacionModel> obtenerListaDeGrabaciones() {
        List<GrabacionModel> grabaciones = new ArrayList<>();
        String directorioGrabaciones = getExternalCacheDir().getAbsolutePath();

        // Aquí deberías obtener la lista de archivos de grabaciones en tu directorio específico
        File directorio = new File(directorioGrabaciones);

        if (!directorio.exists() || !directorio.isDirectory()) {
            // Crea el directorio si no existe
            if (directorio.mkdirs()) {
                Log.d("Archivo", "Directorio creado con éxito");
            } else {
                Log.e("Archivo", "Error al crear el directorio");
                return grabaciones;
            }
        }

        File[] archivos = directorio.listFiles();
        if (archivos != null) {
            for (File archivo : archivos) {
                // Obtener solo el nombre del archivo sin la extensión
                String nombreGrabacion = archivo.getName().replaceFirst("[.][^.]+$", "");

                // Agregar log
                Log.d("Archivo", "Nombre de grabación encontrado: " + nombreGrabacion);

                // Agregar cada archivo como una grabación al modelo
                grabaciones.add(new GrabacionModel(nombreGrabacion));
            }
        } else {
            Log.d("Archivo", "No se encontraron archivos en el directorio");
        }

        return grabaciones;
    }
    @Override
    protected void onDestroy() {

        super.onDestroy();

    }
    @Override
    protected void onStop(){
        super.onStop();
        Log.d("confi_dur_largo", "onDestroy - Llamado");
        long tiempoSesionActual = System.currentTimeMillis() - tiempoInicio;
        TimeT.guardarTiempoAcumulado(this, tiempoSesionActual);
        Log.d("confi_dur_largo ", "onDestroy - Tiempo acumulado: " + tiempoSesionActual);
    }

}