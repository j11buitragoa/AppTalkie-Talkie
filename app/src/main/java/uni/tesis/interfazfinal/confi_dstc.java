package uni.tesis.interfazfinal;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class confi_dstc extends AppCompatActivity {
    private Button start;
    private EditText nfonema,Rep,silencio_time;
    private AudioTrack audioTrack;
    private Spinner listagrab;
    private String nombreGrabacionSeleccionada;
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confi_dstc);
        start = findViewById(R.id.start);
        nfonema = findViewById(R.id.nfonema);
        Rep =  findViewById(R.id.rep);
        silencio_time =  findViewById(R.id.silencio);
        listagrab =  findViewById(R.id.listagrab);
        cargarListaGrabaciones();
        start.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Log.d("Nombre grab", "Valor de nombreGrabacionSeleccionada en onClick: " + nombreGrabacionSeleccionada);
                if (nombreGrabacionSeleccionada != null && !nombreGrabacionSeleccionada.isEmpty()) {
                    SharedPreferences preferences = getSharedPreferences("Preferences_new", Context.MODE_PRIVATE);
                    String fonema = nfonema.getText().toString();
                    String repeticiones = Rep.getText().toString();
                    String silencio = silencio_time.getText().toString();

                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString("repeticiones", repeticiones);
                    editor.putString("silencio", silencio);
                    editor.putString("fonema", fonema);

                    editor.putString("nombreGrabacion", nombreGrabacionSeleccionada);
                    editor.apply();

                    Log.d(TAG, "silencio" + silencio);
                    Log.d(TAG, "repeticiones" + repeticiones);
                    Log.d(TAG, "fonema" + fonema);
                    Log.d(TAG, "nombreGrabacion" + nombreGrabacionSeleccionada);
                    Intent intent = new Intent(confi_dstc.this, repro_staccato.class);
                    startActivity(intent);
                } else {
                    // Si no se ha seleccionado una grabación, mostrar un mensaje al usuario
                    Toast.makeText(confi_dstc.this, "Por favor, seleccione una grabación", Toast.LENGTH_SHORT).show();
                }
            }
        });
        listagrab.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parentView, View view, int i, long l) {
                nombreGrabacionSeleccionada = parentView.getItemAtPosition(i).toString();
                Log.d(TAG, "Nombre de grabación seleccionada: " + nombreGrabacionSeleccionada);
                reproducirGrabacion(nombreGrabacionSeleccionada);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }
    private void cargarListaGrabaciones() {
        SharedPreferences preferences = getSharedPreferences("Grabaciones", Context.MODE_PRIVATE);
        Set<String> grabacionesSet = preferences.getStringSet("grabaciones", new HashSet<>());
        List<String> grabacionesList = new ArrayList<>(grabacionesSet);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, grabacionesList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        listagrab.setAdapter(adapter);
    }
    private void reproducirGrabacion(String nombreGrabacion) {
        if (audioTrack != null) {
            audioTrack.stop();
            audioTrack.release();
            audioTrack = null;
        }
        final int bufferSize = AudioTrack.getMinBufferSize(
                44100,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT);

        audioTrack = new AudioTrack(
                AudioManager.STREAM_MUSIC,
                44100,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                bufferSize,
                AudioTrack.MODE_STREAM);

        try {
            // Simplificamos la construcción de la ruta del archivo
            File file = new File(nombreGrabacion);

            if (!file.exists()) {
                Log.e(TAG, "El archivo no existe: " + file.getAbsolutePath());
                return;
            }

            FileInputStream fileInputStream = new FileInputStream(file);
            BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
            DataInputStream dataInputStream = new DataInputStream(bufferedInputStream);

            audioTrack.play();

            byte[] buffer = new byte[bufferSize];
            int bytesRead;

            while ((bytesRead = dataInputStream.read(buffer)) > 0) {
                audioTrack.write(buffer, 0, bytesRead);
            }

            dataInputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}