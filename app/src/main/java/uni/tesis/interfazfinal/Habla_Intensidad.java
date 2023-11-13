package uni.tesis.interfazfinal;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Habla_Intensidad extends AppCompatActivity {

    private final String USERS_COLLECTION = "Usuarios";
    private final String HEAR_COLLECTION = "HEAR";
    private final String TALK_COLLECTION = "TALK";
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseUser user;
    private CollectionReference talkCollection;
    private DocumentReference talkIntensityDocument;

    private static final int AUDIO_PERMISSION_REQUEST_CODE = 1;
    String TAG = "TAG";

    private SurfaceView surfaceView;
    private SurfaceHolder surfaceHolder;
    private CircleDrawer circleDrawer;

    private AudioRecord audioRecord;
    private boolean isRecording = false;
    private int circleSizePRE = 0;

    private long successStartTime = 0;  // Tiempo en que se alcanzó el objetivo
    private long successDuration = 1000;
    private boolean isSuccessful = false;  // Indicador de si se alcanzó el objetivo durante el tiempo especificado
    //private static final long SUCCESS_DURATION = 1000;  // Duración en milisegundos para mostrar el éxito

    private Switch adminModeSwitch;
    private SeekBar ringSizeSeekBar, ringWidthSeekBar;
    private EditText inputTime;

    private Button backButton;

    private boolean isAdminMode = false;

    private long responseTime;
    private int currentIndex = 0, contDown = 0, contUp = 0;
    private int POINTS_TO_WIN = 10;
    private int[][] resultsLevel = new int[POINTS_TO_WIN][6]; //Col0: successDuration Col1: sizeRing Col2: widthRing Col3:durationIntento Col4: cantUP col5:cantDown

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_habla_intensidad);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        user = mAuth.getCurrentUser();
        talkCollection = db.collection(USERS_COLLECTION).document(user.getEmail()).collection(TALK_COLLECTION);
        talkIntensityDocument = talkCollection.document("Intensidad");

        surfaceView = findViewById(R.id.surfaceView);
        surfaceHolder = surfaceView.getHolder();
        circleDrawer = new CircleDrawer(surfaceHolder);

        adminModeSwitch = findViewById(R.id.switch1);
        ringSizeSeekBar = findViewById(R.id.seekBarRing1);
        ringWidthSeekBar = findViewById(R.id.seekBarRing2);
        inputTime = findViewById(R.id.editText);
        backButton = findViewById(R.id.backButton);

        surfaceView.setZOrderMediaOverlay(true);
        SurfaceHolder surfaceHolder = surfaceView.getHolder();
        surfaceHolder.setFormat(PixelFormat.TRANSPARENT);

        InputFilter noNewlinesFilter = (source, start, end, dest, dstart, dend) -> {
            // Evita que se ingresen caracteres de nueva línea o retorno de carro
            for (int i = start; i < end; i++) {
                if (source.charAt(i) == '\n' || source.charAt(i) == '\r') {
                    return "";
                }
            }
            return null; // Acepta la entrada tal como es
        };

        inputTime.setFilters(new InputFilter[]{noNewlinesFilter});

        backButton.setOnClickListener(v -> {
            Intent goHablaMenu = new Intent(this, Habla_Frame.class);
            startActivity(goHablaMenu);
        });

        adminMode();

        // Check and request audio permission
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, AUDIO_PERMISSION_REQUEST_CODE);
        } else {
            startAudioCapture();
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopAudioCapture();
    }

    private void adminMode(){
        // Configurar un listener para el switch del modo administrador
        adminModeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            isAdminMode = isChecked;
            currentIndex++;
            ringSizeSeekBar.setEnabled(isAdminMode); // Habilitar/deshabilitar el control deslizante según el modo admin
            ringWidthSeekBar.setEnabled(isAdminMode);
        });

        // Configurar un listener para el control deslizante de tamaño del anillo
        ringSizeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (isAdminMode) {
                    // Obtener el valor del control deslizante y ajustar el tamaño del anillo
                    int ringSize = progress;
                    circleDrawer.drawRingWithSize(ringSize);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        ringWidthSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (isAdminMode) {
                    // Obtener el valor del control deslizante y ajustar el tamaño del anillo
                    int ringWidth = progress;
                    circleDrawer.drawRingWithWidth(ringWidth);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        inputTime.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (isAdminMode) {
                    String durationText = inputTime.getText().toString();
                    try {
                        long newDuration = Long.parseLong(durationText);
                        successDuration = newDuration;
                        // Mensaje confirmacion
                        Toast.makeText(getApplicationContext(), "Duración actualizada: " + newDuration + " ms", Toast.LENGTH_SHORT).show();
                    } catch (NumberFormatException e) {
                        // Ingreso de valor no válido
                        Toast.makeText(getApplicationContext(), "Valor no válido", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

    }

    @SuppressLint("MissingPermission")
    private void startAudioCapture() {
        int sampleRate = 44100;  // Standard audio sample rate
        int channelConfig = AudioFormat.CHANNEL_IN_MONO;
        int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
        int bufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat);

        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, sampleRate, channelConfig, audioFormat, bufferSize);

        isRecording = true;
        audioRecord.startRecording();
        Log.d(TAG, "Entra");
        new Thread(new AudioCaptureRunnable()).start();
    }

    private void stopAudioCapture() {
        isRecording = false;
        if (audioRecord != null) {
            audioRecord.stop();
            audioRecord.release();
            audioRecord = null;
        }
    }

    private class AudioCaptureRunnable implements Runnable {
        private static final int MAX_INTENSITY = 32767;  // Maximum value for 16-bit audio

        @Override
        public void run() {
            short[] audioBuffer = new short[1024];
            while (isRecording) {
                int bytesRead = audioRecord.read(audioBuffer, 0, audioBuffer.length);
                if (bytesRead > 0) {
                    // Calculate intensity of the audio signal
                    double intensity = calculateIntensity(audioBuffer, bytesRead);

                    // Adjust the size of the circle based on intensity
                    adjustCircleSize(intensity);
                }
            }
        }

        private double calculateIntensity(short[] audioBuffer, int bytesRead) {
            long sum = 0;
            for (int i = 0; i < bytesRead; i++) {
                sum += Math.abs(audioBuffer[i]);
            }
            double avg = (double) sum / bytesRead;
            return avg / MAX_INTENSITY;
        }

        private void adjustCircleSize(double intensity) {
            final int factorCircleSize = 3000;  // Maximum circle size in pixels
            final int minCircleSize = 10;   // Minimum circle size in pixels
            final int difCircleSize = 20;
            int circleSizePOS = (int) (minCircleSize + intensity * factorCircleSize);

            if (Math.abs(circleSizePOS - circleSizePRE) > difCircleSize)
            {
                runOnUiThread(() -> {
                    circleDrawer.drawCircle(circleSizePOS);

                });
                circleSizePRE = circleSizePOS;
            }
        }
    }

    private class CircleDrawer {
        private SurfaceHolder surfaceHolder;
        private SuccessMessage successMessage;
        private int ringSize = 0;
        private int ringWidth = 0;

        CircleDrawer(SurfaceHolder holder) {
            surfaceHolder = holder;
            successMessage = new SuccessMessage(holder);
        }

        void drawCircle(int size){
            successMessage.drawSuccessMessage(size,ringSize,ringWidth);
        }

        void drawRingWithSize(int size){
            ringSize = size;
            successMessage.drawSuccessMessage(0,ringSize,ringWidth);
        }

        void drawRingWithWidth(int width){
            ringWidth = width;
            successMessage.drawSuccessMessage(0,ringSize,ringWidth);
        }
    }

    private void sendDataBase(DocumentReference doc, int[][] matrix, int level, int pointsWIN){
        Map<String, Object> mapa = new HashMap<>();
        // String tmp1, tmp2;
        List<Integer> duration = new ArrayList<>();
        List<String> size = new ArrayList<>();
        List<String> width = new ArrayList<>();

        for(int i = 0;i<pointsWIN;i++){
            duration.add(matrix[i][0]);
            size.add(String.valueOf(matrix[i][1]));
            width.add(String.valueOf(matrix[i][2]));
        }

        mapa.put("Duration", duration);
        mapa.put("Size Ring", size);
        mapa.put("Width Ring", width);

        doc.set(mapa, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Log.d(TAG, "Enviado");
            }
        });
    }

    private class SuccessMessage {

        //private static final int RING_WIDTH = 0;  // Ancho del anillo
        //private static final int SUCCESS_THRESHOLD = 600;  // Tamaño objetivo del círculo

        private SurfaceHolder surfaceHolder;

        SuccessMessage(SurfaceHolder holder) {
            surfaceHolder = holder;
        }

        void drawSuccessMessage(int circleSize, int ringSize, int ringWidth) {
            Canvas canvas = surfaceHolder.lockCanvas();
            if (canvas != null) {
                canvas.drawColor(Color.WHITE);
                int centerX = canvas.getWidth() / 2;
                int centerY = canvas.getHeight() / 2;

                // Dibujar círculo
                Paint circlePaint = new Paint();
                circlePaint.setColor(Color.argb(255,119,158,203));
                canvas.drawCircle(centerX, centerY, circleSize / 2, circlePaint);

                // Dibujar anillo
                if (ringSize > 0){
                    Paint ringPaint = new Paint();
                    ringPaint.setColor(Color.argb(100,189,236,182));
                    ringPaint.setStyle(Paint.Style.STROKE);
                    ringPaint.setStrokeWidth(ringWidth);
                    canvas.drawCircle(centerX, centerY, ringSize / 2, ringPaint);
                }

                if ((circleSize >= (ringSize - ringWidth/2)) &&((circleSize <= (ringSize + ringWidth/2)))) {
                    contDown = 0;
                    contUp = 0;
                    if (contDown + contUp == 0)
                        responseTime = System.currentTimeMillis();

                    if (!isSuccessful) {
                        successStartTime = System.currentTimeMillis();
                        isSuccessful = true;
                    } else if (System.currentTimeMillis() - successStartTime >= successDuration) {
                        // Cumplido, sigiuente nivel
                        drawSuccessTextOK(canvas, centerX, centerY);
                        responseTime = System.currentTimeMillis() - responseTime;

                        if (currentIndex>0 && currentIndex<=POINTS_TO_WIN){
                            resultsLevel[currentIndex-1][0] = (int) successDuration;
                            resultsLevel[currentIndex-1][1] = ringSize;
                            resultsLevel[currentIndex-1][2] = ringWidth;
                            sendDataBase(talkIntensityDocument, resultsLevel, 1, POINTS_TO_WIN);
                        }else {
                            currentIndex = 0;
                        }
                    }
                } else if (circleSize > (ringSize + ringWidth/2)) {
                    // abajo
                    drawSuccessTextDOWN(canvas, centerX, centerY);
                    contDown++;
                    isSuccessful = false;
                }else {
                    // arriba
                    contUp++;
                    drawSuccessTextUP(canvas, centerX, centerY);
                    isSuccessful = false;
                }

                surfaceHolder.unlockCanvasAndPost(canvas);
            }
        }

        private void drawSuccessTextOK(Canvas canvas, int centerX, int centerY) {
            Paint textPaint = new Paint();
            textPaint.setColor(Color.BLACK);
            textPaint.setTextSize(80);
            textPaint.setTextAlign(Paint.Align.CENTER);
            canvas.drawText("OK", centerX, centerY, textPaint);
        }

        private void drawSuccessTextUP(Canvas canvas, int centerX, int centerY) {
            Paint textPaint = new Paint();
            textPaint.setColor(Color.BLACK);
            textPaint.setTextSize(40);
            textPaint.setTextAlign(Paint.Align.CENTER);
            canvas.drawText("UP", centerX, centerY, textPaint);
        }

        private void drawSuccessTextDOWN(Canvas canvas, int centerX, int centerY) {
            Paint textPaint = new Paint();
            textPaint.setColor(Color.BLACK);
            textPaint.setTextSize(40);
            textPaint.setTextAlign(Paint.Align.CENTER);
            canvas.drawText("DOWN", centerX, centerY, textPaint);
        }
    }
}