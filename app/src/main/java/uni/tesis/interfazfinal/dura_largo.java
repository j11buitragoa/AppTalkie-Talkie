package uni.tesis.interfazfinal;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
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
import android.text.SpannableStringBuilder;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

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
import java.util.Locale;
import java.util.Map;

public class dura_largo extends AppCompatActivity {
    private Button btnStartRecognition;
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private SpannableStringBuilder accumulatedText = new SpannableStringBuilder();
    private Handler uiHandler = new Handler(Looper.getMainLooper());
    private TextView tvResult,tiempoPronunciacion,message;
    private static final float RMS_THRESHOLD = 8f; // Ajusta según sea necesario
    private boolean isSpeaking = false;
    private long startTimeSpeaking;
    private StringBuilder currentVowel = new StringBuilder();
    private HashMap<String, Long> vocalDurations = new HashMap<>();
    private Map<String, Long> startTimeVowelMap = new HashMap<>();
    private SpeechRecognizer speechRecognizer;
    private Runnable uiUpdateRunnable;
    private TextView textVocal,contadorSilencioTextView;
    private TextView timetext,tiempoDura,puntos;
    private long startTimeSelectedVocal;
    private boolean isListening = false;
    private long durationTimeInMillis;
    private long startTimeVocalDetection = 0;
    private int toquesPatoPelota = 0;
    private long elapsedTimeSinceVocalDetection;
    private int totalConteos = 0;

    private Interpolator interpolator = new AccelerateInterpolator();
    private static final float SCALE_FACTOR = 5.0f;
    private float initialPatoPositionX;
    private String duraTime = "5" ;
    private long startTime;
    private boolean isVowelMatch = false;
    private ImageView imagePoint;
    private float initialPositionX;
    private CountDownTimer countdownTimer;
    private  long silenceTime;
    private boolean conteoEnProgreso = false;
    private ProgressBar progressBar;
    private int silenceCount = 0;
    private static final int MAX_TOQUES_PATO_PELOTA = 1;
    private Handler handler = new Handler();
    private boolean isMovingToPelota = false;
    private CountDownTimer countDownTimer;
    private static final int INCREMENTO_CONTADOR = 100; // Ajusta el valor según sea necesario
    private int contadorSilencio = 0;
    private int puntos1=0;
    private CountDownTimer silenceCountdownTimer;
    private int maxSilence;
    private boolean isSilent = true;

    private boolean isPuntuacionContada = false;

    private int duration;
    private boolean isActive = false;
    private AudioRecord audioRecord;
    private AudioTrack audioTrack;
    private Thread thread;
    private int intMicBufferSize;
    private short[] micData, stereoData;
    short[] zeroVector ;
    private DocumentReference userDocRef;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseUser user;
    private int level;
    private final String USERS_COLLECTION = "User";
    private DocumentReference ejercicioDoc;
    private ArrayList<Long> elapsedTimes = new ArrayList<>();
    private  final String EJERCICIOS_COLLECTION="Ejercicios";

    private Points myApp;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dura_largo);
        btnStartRecognition = findViewById(R.id.btnStartRecognition);
        tvResult = findViewById(R.id.tvResult);
        textVocal = findViewById(R.id.textVocal);
        contadorSilencioTextView=findViewById(R.id.contadorSilencioTextView);
        tiempoPronunciacion = findViewById(R.id.tiempoPronunciacion);
        tiempoDura=findViewById(R.id.tiempoDura);
        puntos=findViewById(R.id.puntos);
        imagePoint=findViewById(R.id.imagePoint);
        progressBar = findViewById(R.id.progressBar);
        message = findViewById(R.id.message);
        imagePoint.setVisibility(View.INVISIBLE);
        silenceCountdownTimer = createSilenceCountdownTimer();

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        user = mAuth.getCurrentUser();
        userDocRef = db.collection(USERS_COLLECTION).document(user.getEmail());
        Intent intent = getIntent();
        ArrayList<String> nivel1 = intent.getStringArrayListExtra("Nivel 1");

        String nombreEjercicio = "Ejercicio_" + 15;
        ejercicioDoc = db.collection(EJERCICIOS_COLLECTION).document(nombreEjercicio);
        Log.d(TAG, "user " + user.getDisplayName() + "\nID " + user.getUid());
        myApp = (Points) getApplication();
        myApp.updateMainActivityUI();


        SharedPreferences preferences = getSharedPreferences("MyPreferences", Context.MODE_PRIVATE);
        String selectedVocal = preferences.getString("selectedVocal", "LA");
        String veces = preferences.getString("veces", "5");
        String silcTime = preferences.getString("silc_time", "5");
        Log.d("SharedPreferencesRecibo", "Silencio: " + silcTime);
        Log.d("SharedPreferencesRecibo", "Duración: " + duraTime);
        Log.d("SharedPreferencesRecibo", "Vocal seleccionada desde SharedPreferences: " + selectedVocal);
        duraTime = preferences.getString("dura_time", "5");
        Log.d(TAG, "silenceTime"+silenceTime);
        durationTimeInMillis=Long.parseLong(duraTime)*1000;
        duration=Integer.parseInt(duraTime);
        maxSilence = Integer.parseInt(silcTime);
        progressBar.setMax(maxSilence);

        Log.d("Envio", "veces: " + veces);
        //Log.d("Envio", "silc_time: " + silcTime);
        Log.d("Envio", "dura_time: " + duraTime);
        Log.d("Debug", "Vocal seleccionada desde SharedPreferences: " + selectedVocal);

        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                if (!isActive)
                    return;
                //threadLoop();
            }
        });

        switch (selectedVocal.toLowerCase()) {
            case "a":
                textVocal.setText("Pronuncia La");
                break;
            case "e":
                textVocal.setText("Pronuncia Me");
                break;
            case "i":
                textVocal.setText("Pronuncia Mi");
                break;
            case "o":
                textVocal.setText("Pronuncia No");
                break;
            case "u":
                textVocal.setText("Pronuncia Su");
                break;
        }
        timetext = findViewById(R.id.timetext);
        verificarPermisos();
        inicializarReconocedorVoz();
        btnStartRecognition.setOnClickListener(view -> toggleVoiceRecognition());

        uiUpdateRunnable = () -> {
            tvResult.setText(accumulatedText.toString());
        };
    }
    private DocumentReference getEjercicioDocument(int level) {
        String nombreEjercicio = "Ejercicio_" + 15;
        return db.collection(EJERCICIOS_COLLECTION).document(nombreEjercicio);
    }
    private void addPoints(int points) {
        String sectionName = "Habla_Duracion_Largo";
        ((Points) getApplication()).addPoints(sectionName, points);
    }

    private void sendDataBase(DocumentReference userdoc,int level){
        DocumentReference ejercicioDoc = getEjercicioDocument(level);
        Map<String, Object> mapa = new HashMap<>();
        SharedPreferences preferences = getSharedPreferences("MyPreferences", Context.MODE_PRIVATE);
        String selectedVocal = preferences.getString("selectedVocal", "LA");
        String veces = preferences.getString("veces", "5");
        String silcTime = preferences.getString("silc_time", "5");
        String duraTime = preferences.getString("dura_time", "5");

        int duration=Integer.parseInt(duraTime);
        int rep=Integer.parseInt(veces);
        int sil=Integer.parseInt(silcTime);
        int vez=rep*2;
        // String tmp1, tmp2;
        mapa.put("vocal de concurrencia", selectedVocal);
        mapa.put("puntos Posibles", vez);
        mapa.put("tiempo de mantener la voz ", duration);
        mapa.put("aciertos", puntos1);
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
    private void verificarPermisos() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO},
                    REQUEST_RECORD_AUDIO_PERMISSION);
            Log.d("O", "NO entro");

        } else {
            Log.d("O", "entro");
            inicializarReconocedorVoz();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                inicializarReconocedorVoz();
            } else {
                Log.d("O", "Permiso denegado");
            }
        }
    }

    private void toggleVoiceRecognition() {
        if (!isListening) {
            startVoiceRecognition();
            isActive = true;
            thread.start();
        } else {
            stopVoiceRecognition();
        }
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
            Log.d(TAG, "Entro al while is active");
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

    private void startVoiceRecognition() {
        runOnUiThread(() -> {

            startTimeSelectedVocal = System.currentTimeMillis();
            startTimeVocalDetection = System.currentTimeMillis();
            if (speechRecognizer == null) {
                try {
                    speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
                } catch (Exception e) {
                    Log.e("SpeechRecognizer", "Error al crear SpeechRecognizer: " + e.getMessage());
                    return;
                }
                inicializarReconocedorVoz();
            }

            Intent recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "es-ES");
            recognizerIntent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);

            try {
                speechRecognizer.startListening(recognizerIntent);
                isListening = true;
                Log.d("SpeechRecognizer", "Start listening");
            } catch (Exception e) {
                Log.e("SpeechRecognizer", "Error al iniciar el reconocimiento: " + e.getMessage());
            }
        });
    }

    private void inicializarReconocedorVoz() {
        SharedPreferences preferences = getSharedPreferences("MyPreferences", Context.MODE_PRIVATE);
        String selectedVocal = preferences.getString("selectedVocal", "LA");
        Log.d("SharedPreferencesRecibo", "Vocal seleccionada desde SharedPreferences: " + selectedVocal);
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        speechRecognizer.setRecognitionListener(new RecognitionListener() {

            @Override
            public void onReadyForSpeech(Bundle bundle) {
                Log.d("SpeechRecognizer", "Ready for speech");
                startTimeSpeaking = System.currentTimeMillis();
                startTimeVowelMap.clear();
                isListening = true;
                tiempoDura.setText("Tiempo que debes alcanzar:"+duraTime);
            }

            @Override
            public void onBeginningOfSpeech() {
                Log.d("SpeechRecognizer", "Beginning of speech");
                // Calcula el tiempo transcurrido desde el inicio hasta aquí
                long elapsedTime = System.currentTimeMillis() - startTimeSpeaking;
                Log.d("SpeechRecognizer", "Tiempo hasta comenzar a hablar: " + elapsedTime + " ms");
                verificarPermisos();
                elapsedTimes.add(elapsedTime);
                isSilent = false;

            }

            @Override
            public void onRmsChanged(float v) {
                if (v > RMS_THRESHOLD) {
                    if (!conteoEnProgreso) {
                        isSilent = false;
                    }
                    if (!currentVowel.toString().isEmpty()) {
                        long currentTime = System.currentTimeMillis();
                        long elapsedTime = currentTime - startTimeSpeaking;
                        vocalDurations.put(currentVowel.toString(), vocalDurations.getOrDefault(currentVowel.toString(), 0L) + elapsedTime);
                        actualizarDuracionVocales();

                    }
                    currentVowel = new StringBuilder();
                    startTimeVowelMap.clear();
                } else {
                    if (!currentVowel.toString().isEmpty()) {
                        actualizarDuracionVocales();
                    }
                    isSilent = true;

                }
                // Actualiza el progreso de la barra aquí cuando cambia el RMS
                if (isSpeaking && isVowelMatch) {
                    long currentTime = System.currentTimeMillis();
                    elapsedTimeSinceVocalDetection = currentTime - startTimeVocalDetection;
                    if (currentVowel.toString().toLowerCase().equals(selectedVocal.toLowerCase())) {
                        long tiempoTranscurrido = currentTime - startTimeSpeaking;
                        actualizarTiempoPronunciacion(tiempoTranscurrido);
                    }
                }
            }

            @Override
            public void onBufferReceived(byte[] buffer) {
                Log.d("SpeechRecognizer", "Buffer received: " + Arrays.toString(buffer));

            }

            @Override
            public void onEndOfSpeech() {
                Log.d("SpeechRecognizer", "End of speech");
                isListening = false;
                isSpeaking = false;
                startVoiceRecognition();
            }

            @Override
            public void onError(int error) {
                Log.e("SpeechRecognizer", "Error during recognition: " + error);
                onSpeechEnd();
                if (error == SpeechRecognizer.ERROR_SERVER) {
                    startVoiceRecognition();
                } else if (error == SpeechRecognizer.ERROR_NO_MATCH) {
                    startVoiceRecognition();
                } else {
                    Log.e("SpeechRecognizer", "Error durante el reconocimiento: " + error);
                    if (error == SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS) {
                        verificarPermisos();
                    }
                }

            }

            @Override
            public void onResults(Bundle results) {
                ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (matches != null && !matches.isEmpty()) {
                    String result = matches.get(0);
                    Log.d("SpeechRecognizer", "Recognition result: " + result);
                    startVoiceRecognition();
                }
            }

            @Override
            public void onPartialResults(Bundle bundle) {
                ArrayList<String> matches = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (matches != null && !matches.isEmpty()) {
                    String result = matches.get(0);
                    Log.d("SpeechRecognizer", "Partial result: " + result);
                    String vowels = getVowels(result);
                    updateTvResult(vowels);
                    if (!vowels.isEmpty()) {
                        currentVowel = new StringBuilder(vowels);
                        Log.d("SpeechRecognizer", "currentVowel: " + currentVowel.toString());
                        Log.d("SpeechRecognizer", "selectedVocal: " + selectedVocal);
                        if (currentVowel.toString().toLowerCase().equals(selectedVocal.toLowerCase())) {
                            long currentTime = System.currentTimeMillis();
                            long elapsedTimeSinceVocalDetection = currentTime - startTimeVocalDetection;

                            startTimeVocalDetection = currentTime;
                            Log.d("SpeechRecognizer", "La vocal coincide: " + selectedVocal);

                            if (!isSpeaking) {
                                // Inicia el tiempo de voz solo si no está hablando actualmente
                                startTimeSpeaking = currentTime;
                                isSpeaking = true;
                                //start...
                            }
                            isVowelMatch = true;
                            startTimeVocalDetection = System.currentTimeMillis();
                            startTimeSpeaking = startTimeVocalDetection;
                            startTimeVowelMap.put(currentVowel.toString(), startTimeSpeaking);
                        } else {
                            isSpeaking = false;
                            isVowelMatch = false;
                            Log.d("SpeechRecognizer", "La vocal no coincide. Vocal seleccionada: " + selectedVocal);
                        }
                    }
                }
            }

            @Override
            public void onEvent(int i, Bundle bundle) {

            }
        });
    }


    private void actualizarTiempoPronunciacion(long tiempoTranscurrido) {
        int segundos = (int) (tiempoTranscurrido / 1000);
        if (isSpeaking) {
            uiHandler.post(() -> {
                if (tiempoPronunciacion != null) {
                    tiempoPronunciacion.setText("Tiempo de pronunciación: " + segundos + " segundos");
                    if (segundos >= duration && !isPuntuacionContada && countDownTimer==null) {
                        puntos1++;
                        isPuntuacionContada = true;
                        if (imagePoint!= null) {
                            imagePoint.setVisibility(View.VISIBLE);
                            startSilenceCountdown();
                        }
                        Log.d("Puntos", "Puntos: " + puntos1);
                        contadorSilencio = 0;
                        // Actualizar la vista de puntos en el TextView
                        if (puntos != null) {
                            puntos.setText("Puntos: " + puntos1);
                        } else {
                            Log.e("TextViewPuntosError", "textViewPuntos es nulo");
                        }
                    } else if (segundos < duration) {
                        // Restablecer el indicador si el tiempo es inferior a la duración
                        isPuntuacionContada = false;
                        imagePoint.setVisibility(View.INVISIBLE);

                    }
                } else {
                    Log.e("TiempoPronunciacionError", "tiempoPronunciacion es nulo");
                }
            });
        }
    }
    private CountDownTimer createSilenceCountdownTimer() {
        SharedPreferences preferences = getSharedPreferences("MyPreferences", Context.MODE_PRIVATE);
        String silcTime = preferences.getString("silc_time", "5");

        Log.d("SharedPreferencesRecibo0", "Silencio: " + silcTime);

        return new CountDownTimer(Integer.parseInt(silcTime) * 1000, 1000) {

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
                    if (silenceCount == Integer.parseInt(silcTime)) {
                        puntos1++;
                        puntos.setText(Integer.toString(puntos1));
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
    private void startSilenceCountdown() {
        SharedPreferences preferences = getSharedPreferences("MyPreferences", Context.MODE_PRIVATE);
        String veces = preferences.getString("veces", "5");
        int veces1 = Integer.parseInt(veces);
        silenceCount = 0;
        totalConteos++;
        Log.d("Conteos: ", "Conteos: " + totalConteos);
        silenceCountdownTimer.start();
        if (totalConteos>=veces1){
            stopDetectionAndShowToast();
        }
    }
    private void stopDetectionAndShowToast() {
        addPoints(puntos1);
        sendDataBase(userDocRef,level);
        // Detener la detección y mostrar un Toast
        resetConteoYBarra();
        speechRecognizer.stopListening();
        ///Toast.makeText(this, "Se ha alcanzado el límite de conteos. ¡Se acabó!", Toast.LENGTH_SHORT).show();
        mostrarImagenEspecial();
        mostrarResultado();

    }
    private void resetConteoYBarra() {
        progressBar.setProgress(0);
        silenceCountdownTimer.cancel();
        silenceCount = 0;
        updateSilenceCountTextView(silenceCount);
        conteoEnProgreso = false;
    }
    // Obtén los resultados del reconocimiento de voz

    private void updateSilenceCountTextView(int count) {
        contadorSilencioTextView.setText("Silence Count: " + count);

    }
    private void updateTvResult(String text) {
        uiHandler.post(() -> {
            if (tvResult != null) {
                tvResult.setText(text);
            } else {
                Log.e("TvResultError", "tvResult es nulo");
            }
        });
    }

    @SuppressLint("SetTextI18n")
    private void onSpeechEnd() {
        Log.d("SpeechRecognizer", "End of speech");
        if (!currentVowel.toString().isEmpty()) {
            long elapsedTime = System.currentTimeMillis() - startTimeSpeaking;
            vocalDurations.put(currentVowel.toString(), vocalDurations.getOrDefault(currentVowel.toString(), 0L) + elapsedTime);
        }
        final String durationText = getDurationText();  // Almacenar el resultado en una variable
        uiHandler.post(() -> {
            if (timetext != null) {
                timetext.setText("Total: " + durationText + " ms");
            } else {
                Log.e("DuracionError", "timetext es nulo");
            }
        });
        Log.d("Duracion Total ", getDurationText());
        // Restablecer variables
        currentVowel = new StringBuilder();
        isSpeaking = false;
        isVowelMatch = false;
        startTimeSpeaking = 0;
    }

    @SuppressLint("SetTextI18n")
    private void actualizarDuracionVocales() {
        long totalDuration = 0;
        for (Map.Entry<String, Long> entry : startTimeVowelMap.entrySet()) {
            String vowel = entry.getKey();
            long startTime = entry.getValue();
            long duration = System.currentTimeMillis() - startTime;
            totalDuration += duration;
            vocalDurations.put(vowel, duration);
        }
        startTimeVowelMap.clear();
        //vocalDurations.clear();
    }
    private String getDurationText() {
        StringBuilder resultText = new StringBuilder();
        for (Map.Entry<String, Long> entry : vocalDurations.entrySet()) {
            resultText.append(entry.getKey())
                    .append("duración total : ")
                    .append(entry.getValue())
                    .append(" ms\n");
        }
        return resultText.toString();
    }
    private String getVowels(String input) {
        return input.replaceAll("[^aeiouAEIOU]", "");
    }

    private void mostrarResultado() {
        SharedPreferences preferences = getSharedPreferences("MyPreferences", Context.MODE_PRIVATE);
        String veces = preferences.getString("veces", "5");
        Integer repeticionest=Integer.parseInt(veces);
        Integer repeticionestt=repeticionest*2;
        // Inflar el layout personalizado
        View resultadoView = getLayoutInflater().inflate(R.layout.layout_result, null);

        // Obtener el TextView del layout
        TextView textResultado = resultadoView.findViewById(R.id.puntostt);
        TextView textPosibles = resultadoView.findViewById(R.id.puntospos);
        ImageView imageResultado = resultadoView.findViewById(R.id.imageView);

        // Establecer el número de puntos en el TextView (ajusta esto según tu lógica)
        textResultado.setText("Puntos: " + puntos1);
        textPosibles.setText("Posibles: " +repeticionestt);
        // Lógica para determinar la imagen según la cantidad de puntos
        if (puntos1 >= repeticionestt) {
            // Si obtuvo la mayor cantidad de puntos posibles
            imageResultado.setImageResource(R.drawable.star5);
        } else if (puntos1 >= repeticionestt / 2) {
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
                Intent intent = new Intent(dura_largo.this, Habla_Frame.class);
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
        imageView.setVisibility(View.VISIBLE);;

        // También puedes realizar otras acciones necesarias después de la última repetición
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
    private void stopVoiceRecognition() {
        if (speechRecognizer != null) {
            speechRecognizer.stopListening();
            isListening = false;
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (speechRecognizer != null) {
            speechRecognizer.destroy();
            stopVoiceAcquisitionPlayback();

        }
    }

}