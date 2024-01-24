package uni.tesis.interfazfinal;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.icu.util.Calendar;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class duracion_staccato extends AppCompatActivity {
    private TextView resultTextView, silenceCountTextView, message, puntos;
    private ImageView pato;
    private int contadorRepeticiones = 0;
    private LinearLayout patoContainer;
    private boolean conteoEnProgreso = false;
    private List<ImageView> listaPatitos = new ArrayList<>();
    private long startTimeSpeaking;
    private long lastSpeechEndTime;
    private SpeechRecognizer speechRecognizer;
    private boolean isListening = false;
    private int silenceCount = 0;
    private int puntosS = 0;
    private ProgressBar progressBar;

    private long lastVoiceActivityTime = 0;
    private int maxSilence;
    private int intMicBufferSize, intStereoBufferSize;
    private short[] micData, stereoData;
    private CountDownTimer silenceCountdownTimer;
    private static final double RMS_THRESHOLD = 0.5; // Umbral ajustable
    private boolean isSilent = true;
    short[] zeroVector ;
    private boolean isAudioThreadRunning = false;

    private boolean isActive = false;
    private Handler uiHandler = new Handler(Looper.getMainLooper());

    private AudioRecord audioRecord;
    private AudioTrack audioTrack;
    private Thread thread;
    private Thread audioThread;
    private DocumentReference userDocRef;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseUser user;
    private final String USERS_COLLECTION = "User";
    private DocumentReference ejercicioDoc;
    private ArrayList<Long> elapsedTimes = new ArrayList<>();
    private int level;
    private Points points;
    private  final String EJERCICIOS_COLLECTION="Ejercicios";
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_duracion_staccato);
        resultTextView = findViewById(R.id.resultTextView);
        Button startSpeechBtn = findViewById(R.id.startSpeechBtn);
        patoContainer = findViewById(R.id.patoContainer);
        listaPatitos = new ArrayList<>();
        silenceCountTextView = findViewById(R.id.silenceCountTextView);
        silenceCountdownTimer = createSilenceCountdownTimer();
        progressBar = findViewById(R.id.progressBar);
        message = findViewById(R.id.message);
        puntos = findViewById(R.id.puntos);


        SharedPreferences preferences = getSharedPreferences("Preferences_new", Context.MODE_PRIVATE);
        String fonema = preferences.getString("fonema", "LA");
        String repeticiones = preferences.getString("repeticiones", "5");
        String silencio = preferences.getString("silencio", "1");
        String nombreGrabacion = preferences.getString("nombreGrabacion", "");

        Log.d("Envio", "veces: " + repeticiones);
        Log.d("Envio", "fonema: " + fonema);
        Log.d("Envio", "silencio: " + silencio);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        user = mAuth.getCurrentUser();
        userDocRef = db.collection(USERS_COLLECTION).document(user.getEmail());
        String nombreEjercicio = "Ejercicio_" + 16;
        ejercicioDoc = db.collection(EJERCICIOS_COLLECTION).document(nombreEjercicio);
        Intent intent = getIntent();
        ArrayList<String> nivel1 = intent.getStringArrayListExtra("Nivel 1");
        Log.d(TAG, "user " + user.getDisplayName() + "\nID " + user.getUid());
        points = (Points) getApplication();
        points.updateMainActivityUI();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                maxSilence = Integer.parseInt(silencio);
                progressBar.setMax(maxSilence);
                speechRecognizer = SpeechRecognizer.createSpeechRecognizer(duracion_staccato.this);
                speechRecognizer.setRecognitionListener(new MyRecognitionListener());

            }
        });

        audioThread = new Thread(new Runnable() {
            @Override
            public void run() {
                if (!isActive)
                    return;
                threadLoop();
            }
        });


        startSpeechBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isListening) {
                    isActive = true;
                    //audioThread.start();
                    startSpeechRecognitionOnUiThread();

                } else {
                    stopSpeechRecognition();
                }
            }
        });
    }
    private void startSpeechRecognitionOnUiThread() {
        uiHandler.post(new Runnable() {
            @Override
            public void run() {
                startSpeechRecognition();
            }
        });
    }
    private DocumentReference getEjercicioDocument(int level) {
        String nombreEjercicio = "Ejercicio_" + 16;
        return db.collection(EJERCICIOS_COLLECTION).document(nombreEjercicio);
    }

    private void sendDataBase(DocumentReference userdoc,int level){
        DocumentReference ejercicioDoc = getEjercicioDocument(level);
        Map<String, Object> mapa = new HashMap<>();
        SharedPreferences preferences = getSharedPreferences("Preferences_new", Context.MODE_PRIVATE);
        String fonema = preferences.getString("fonema", "LA");
        String repeticiones = preferences.getString("repeticiones", "5");
        String silencio = preferences.getString("silencio", "1");


        int rep=Integer.parseInt(repeticiones);
        int sil=Integer.parseInt(silencio);
        int vez=rep*2;
        // String tmp1, tmp2;
        mapa.put("fonema", fonema);
        mapa.put("puntos Posibles", vez);
        mapa.put("aciertos", puntosS);
        mapa.put("duración de silencio",sil);
        mapa.put("tiempo de Respuesta:" , elapsedTimes);
        mapa.put("ejercicio",ejercicioDoc);

        DocumentReference userDocRef = db.collection(USERS_COLLECTION).document(user.getEmail());
        mapa.put("usuario",userDocRef);
        Date fechaActual = Calendar.getInstance().getTime();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String fechaHora = dateFormat.format(fechaActual);
        mapa.put("fecha",fechaHora);
        // Agrega el nuevo intento a la colección "Intentos"
        db.collection("Intentosf")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int numIntentos = queryDocumentSnapshots.size();
                    String intentoNombre = "Intento_" + (numIntentos + 1);

                    db.collection("Intentosf")
                            .document(intentoNombre)
                            .set(mapa)
                            .addOnSuccessListener(documentReference -> {
                                Log.d(TAG, "Datos del intento enviados correctamente");
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Error al enviar datos del intento", e);
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al obtener la cantidad de intentos", e);
                });
    }
    private void threadLoop() {
        // Configurar AudioRecord para la adquisición de voz
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        int intRecordSampleRate = 8000;
        intMicBufferSize = AudioRecord.getMinBufferSize(intRecordSampleRate, AudioFormat.CHANNEL_IN_MONO
                , AudioFormat.ENCODING_PCM_16BIT);
        micData = new short[intMicBufferSize];
        zeroVector = new short[intMicBufferSize];
        Arrays.fill(zeroVector, (short) 0);

        Log.d(TAG, "Entro al threadloop");
        Log.d("AudioBuffer", "Tamaño del búfer de audio: " + intMicBufferSize);
        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC,
                intRecordSampleRate,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                intMicBufferSize);
        audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC
                , intRecordSampleRate
                , AudioFormat.CHANNEL_OUT_STEREO
                , AudioFormat.ENCODING_PCM_16BIT
                , intMicBufferSize
                , AudioTrack.MODE_STREAM);

        audioRecord.startRecording();
        audioTrack.play();

        while (isActive) {
            // Log.d(TAG, "Entro al while is active");
            audioRecord.read(micData, 0, intMicBufferSize);
            for (int i = 0; i < intMicBufferSize; i++) {
                micData[i] = (short) Math.min(micData[i] , Short.MAX_VALUE);
            }
            // Crear señal estéreo solo con el canal izquierdo
            stereoData = stereoSound(micData, zeroVector, 2 * intMicBufferSize);
            audioTrack.write(stereoData, 0, stereoData.length);
        }
        // Detener grabación y reproducción al salir del bucle
        audioRecord.stop();
        audioTrack.stop();
    }
    private short[] stereoSound(short[] left, short[] right, int stereoArraySize){

        short[] stereoSoundArray = new short[stereoArraySize];

        for (int i = 0; i<stereoArraySize; i++){
            stereoSoundArray[i]=0;
        }
        // LEFT
        for (int i = 0; i<left.length; i++){
            stereoSoundArray[2*i] = left[i];
        }
        // RIGHT
        for (int i = 0; i<left.length; i++){
            stereoSoundArray[2*i+1] = right[i];
        }

        return stereoSoundArray;
    }
    private void startSpeechRecognition() {
        isAudioThreadRunning = true;

        isListening = true;
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "es"); // Cambia a tu idioma preferido


        speechRecognizer.startListening(intent);
    }

    private void stopSpeechRecognition() {
        isListening = false;
        resultTextView.setText("Detenido.");
        speechRecognizer.stopListening();
    }
    private class MyRecognitionListener implements RecognitionListener {
        SharedPreferences preferences=getSharedPreferences("Preferences_new", Context.MODE_PRIVATE);
        String fonema=preferences.getString("fonema","LA");

        @Override
        public void onReadyForSpeech(Bundle bundle) {
            Log.d("SpeechRecognizer", "Ready for speech");
            startTimeSpeaking = System.currentTimeMillis();
            message.setVisibility(View.VISIBLE);
            message.setText("Pronuncia "+fonema);

        }

        @Override
        public void onBeginningOfSpeech() {
            Log.d("SpeechRecognizer", "Beginning of speech");
            // Calcula el tiempo transcurrido desde el inicio hasta aquí
            long elapsedTime = System.currentTimeMillis() - startTimeSpeaking;
            Log.d("SpeechRecognizer", "Tiempo hasta comenzar a hablar: " + elapsedTime + " ms");
            lastSpeechEndTime = System.currentTimeMillis();
            elapsedTimes.add(elapsedTime);

            isSilent = false;
        }

        @Override
        public void onRmsChanged(float v) {
            // Verifica si el nivel de sonido es mayor que el umbral establecido
            if (v > RMS_THRESHOLD) {
                lastSpeechEndTime = System.currentTimeMillis(); // Actualiza el tiempo de la última actividad de voz
                // La persona está hablando, pero solo incrementa la barra si isSilent es falso
                if (!conteoEnProgreso) {
                    isSilent = false;
                }
            } else {
                // La persona está en silencio
                isSilent = true;
            }
        }

        @Override
        public void onBufferReceived(byte[] bytes) {

        }

        @Override
        public void onEndOfSpeech() {
            lastSpeechEndTime = System.currentTimeMillis();

        }

        @Override
        public void onError(int error) {
            resultTextView.setText("Error en el reconocimiento de voz. Código: " + error);
            uiHandler.post(new Runnable() {
                @Override
                public void run() {
                    startSpeechRecognition();
                }
            });

        }

        @Override
        public void onResults(Bundle results) {
            updateResultView(results);
            // Reinicia la escucha para la adquisición continua
            if (isListening) {
                startSpeechRecognition();

            }

        }

        @Override
        public void onPartialResults(Bundle bundle) {
            updateResultView(bundle);

        }


        @Override
        public void onEvent(int i, Bundle bundle) {

        }
    }
    private void updateResultView(Bundle results) {
        SharedPreferences preferences=getSharedPreferences("Preferences_new", Context.MODE_PRIVATE);
        String fonema=preferences.getString("fonema","LA");
        ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        if (matches != null && !matches.isEmpty()) {
            String result = matches.get(0);
            if (result.equalsIgnoreCase(fonema)) {
                resultTextView.setText("Texto reconocido: " + result);
                contadorRepeticiones++;
                puntosS++;
                puntos.setText(Integer.toString(puntosS));
                message.setVisibility(View.GONE);

                mostrarImagenesSegunRepeticiones();


            } else {
                resultTextView.setText("No coincidió con el fonema.");
            }
        }
    }
    private void mostrarImagenesSegunRepeticiones() {
        SharedPreferences preferences=getSharedPreferences("Preferences_new", Context.MODE_PRIVATE);
        String repeticiones=preferences.getString("repeticiones","5");
        String silencio = preferences.getString("silencio", "1");

        Log.d("mostrarImagenes", "Mostrando imagen para repeticiones: " + contadorRepeticiones);

        // Obtén el ID del recurso de la imagen dinámicamente
        int resourceId = getResources().getIdentifier("pato2", "drawable", getPackageName());
        // Verifica si aún puedes mostrar más patos
        if (contadorRepeticiones <= Integer.parseInt(repeticiones)) {

            // Crea un nuevo ImageView para cada pato
            ImageView nuevoPato = new ImageView(this);
            // Configura los parámetros de diseño con márgenes y dimensiones proporcionales a la pantalla
            DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
            int screenWidth = displayMetrics.widthPixels;
            int screenHeight = displayMetrics.heightPixels;
            int patoWidth = screenWidth / 10; // Ajusta según sea necesario
            int patoHeight = screenHeight / 10; // Ajusta según sea necesario

            // Configura los parámetros de diseño con márgenes (ajusta estos valores según tus necesidades)
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    patoWidth, // Ancho
                    patoHeight  // Altura
            );

            // Ajusta los márgenes para lograr la disposición en forma de escalera ascendente
            //int leftMargin = 50 + (contadorRepeticiones - 1) * 30;  // Ajusta el valor según tus necesidades
            int rightMargin = 100 - (contadorRepeticiones - 1) * 45;  // Ajusta el valor según tus necesidades
            int topMargin = 0 + (contadorRepeticiones - 1) * 10;   // Ajusta el valor según tus necesidades

            layoutParams.setMargins(rightMargin, topMargin, 150, 10);
            nuevoPato.setLayoutParams(layoutParams);

            // Establece la imagen en el ImageView
            nuevoPato.setImageResource(resourceId);

            // Agrega el nuevo pato al contenedor
            patoContainer.addView(nuevoPato);


            resetConteoYBarra();



            // Iniciar el temporizador de silencio solo si el contadorRepeticiones es menor o igual al número de silencio deseado
            if (contadorRepeticiones <= Integer.parseInt(silencio) && !conteoEnProgreso) {
                startSilenceCountdown();
                conteoEnProgreso = true;
            }

        }else{
            stopSpeechRecognition();
            mostrarImagenEspecial();
            mostrarResultado();
            sendDataBase(userDocRef,level);


        }
    }
    private void startSilenceCountdown() {
        silenceCount = 0;
        silenceCountdownTimer.start();
    }

    private void resetConteoYBarra() {
        progressBar.setProgress(0);
        silenceCountdownTimer.cancel();
        silenceCount = 0;
        updateSilenceCountTextView(silenceCount);
        conteoEnProgreso = false;

    }
    private void mostrarResultado() {

        SharedPreferences preferences=getSharedPreferences("Preferences_new", Context.MODE_PRIVATE);
        String repeticiones=preferences.getString("repeticiones","5");
        Integer repeticionest=Integer.parseInt(repeticiones);
        Integer repeticionestt=repeticionest*2;
        // Inflar el layout personalizado
        View resultadoView = getLayoutInflater().inflate(R.layout.layout_result, null);

        // Obtener el TextView del layout
        TextView textResultado = resultadoView.findViewById(R.id.puntostt);
        TextView textPosibles = resultadoView.findViewById(R.id.puntospos);
        ImageView imageResultado = resultadoView.findViewById(R.id.imageView);

        // Establecer el número de puntos en el TextView (ajusta esto según tu lógica)
        textResultado.setText("Puntos: " + puntosS);
        textPosibles.setText("Posibles: " +repeticionestt);
        // Lógica para determinar la imagen según la cantidad de puntos
        if (puntosS >= repeticionestt) {
            // Si obtuvo la mayor cantidad de puntos posibles
            imageResultado.setImageResource(R.drawable.star5);
        } else if (puntosS >= repeticionestt / 2) {
            // Si obtuvo al menos la mitad de los puntos posibles
            imageResultado.setImageResource(R.drawable.star3);
        } else {
            // Si obtuvo menos de la mitad de los puntos posibles
            imageResultado.setImageResource(R.drawable.star1);
        }


        // Mostrar el layout resultado en un AlertDialog o en cualquier otro contenedor deseado
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(resultadoView);
        builder.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(duracion_staccato.this, Habla_Frame.class);
                startActivity(intent);
                finish();
            }
        }); // Puedes agregar botones adicionales o acciones según sea necesario
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }


    private void mostrarImagenEspecial() {
        // Aquí puedes mostrar la imagen especial, por ejemplo, cambiar la imagen de un ImageView
        ImageView imageView = findViewById(R.id.trofeo);
        // Establecer las dimensiones deseadas (ajusta los valores según tus necesidades)
        int widthInPixels = 100;  // Ancho en píxeles
        int heightInPixels = 100; // Altura en píxeles

        // Configurar los parámetros de diseño
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                widthInPixels,
                heightInPixels
        );

        // Establecer los parámetros de diseño en el ImageView
        imageView.setLayoutParams(layoutParams);
        imageView.setImageResource(R.drawable.trofeo);
        addPoints(puntosS);
        imageView.setVisibility(View.VISIBLE);;

        // También puedes realizar otras acciones necesarias después de la última repetición
    }
    private void addPoints(int points) {
        String sectionName = "Habla_Staccato";
        ((Points) getApplication()).addPoints(sectionName, points);
    }
    private CountDownTimer createSilenceCountdownTimer() {
        SharedPreferences preferences=getSharedPreferences("Preferences_new", Context.MODE_PRIVATE);
        String silencio=preferences.getString("silencio","1");

        return new CountDownTimer(Integer.parseInt(silencio) * 1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                silenceCount++;
                updateSilenceCountTextView(silenceCount);
                // Verifica si la persona está en silencio
                if (isSilent) {
                    progressBar.incrementProgressBy(1);  // Incrementa la barra de progreso en 1
                    // Actualiza la barra de progreso
                    progressBar.setProgress(silenceCount);
                    message.setVisibility(View.GONE);
                    if (silenceCount == Integer.parseInt(silencio)) {
                        puntosS++;
                        puntos.setText(Integer.toString(puntosS));
                    }
                }else{
                    message.setVisibility(View.VISIBLE);
                    message.setText("¡Quédate en silencio para que la barra aumente!");

                }



            }

            @Override
            public void onFinish() {
                // Se ha alcanzado el valor de silencio, reiniciar el temporizador y mostrar nuevas imágenes
                silenceCount = 0;
                conteoEnProgreso = false;
                progressBar.setProgress(0);

            }
        };
    }
    // Obtén los resultados del reconocimiento de voz

    private void updateSilenceCountTextView(int count) {
        silenceCountTextView.setText("Silence Count: " + count);


    }
    private void stopVoiceAcquisitionPlayback() {
        isActive = false;
        if (audioRecord != null) {
            audioRecord.stop();
            audioRecord.release();
        }
        if (audioTrack != null) {
            audioTrack.stop();
            audioTrack.release();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        speechRecognizer.setRecognitionListener(null);
        speechRecognizer.destroy();
        stopVoiceAcquisitionPlayback();

    }
    @Override
    protected void onStop() {
        super.onStop();
        isActive = false; // Detener el hilo de audio en onStop

    }
}