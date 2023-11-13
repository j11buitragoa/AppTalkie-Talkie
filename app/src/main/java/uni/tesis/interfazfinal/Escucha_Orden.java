package uni.tesis.interfazfinal;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

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
import java.util.Random;

public class Escucha_Orden extends AppCompatActivity {

    private String TAG = "START";
    private final String USERS_COLLECTION = "Usuarios";
    private final String HEAR_COLLECTION = "HEAR";
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseUser user;
    private CollectionReference hearCollection;
    private DocumentReference hearOrderDocument;
    private int currentLevel = 1;
    private int currentPatternIndex = 0, currentPatternIndex2 = 0;
    private int pointsToPassLevel = 3, successCount = 0, faultCount = 0;
    private int[][] rangeMs = {
            {1000, 1500, 500}, // minMsL1, maxMsL1, waitTimeL1
            {500, 1000, 300},  // minMsL2, maxMsL2, waitTimeL1
            {200, 500, 200},
            {100, 200, 200},
            {100, 500, 200}// minMsL3, maxMsL3, waitTimeL1
    };
    private int maxLevel = 4;
    private int[][][] patterns = new int[maxLevel][pointsToPassLevel][2]; //Col 1: Canal, Col 2: Duration

    // UI Variables
    private Button leftButton, rightButton, backButton;
    private TextView levelTextView, scoreTextView;

    // Audio Variables
    private AudioTrack audioTrack;
    private int sampleRate = 8000, freqTone = 300, waitTime = 500, bufferSize;
    private short[] left, right, tone;
    private double gain = 1;

    private long responseTime;
    private int[][] resultsLevel = new int[pointsToPassLevel][2]; // Col0: Patron Col1: Resultados

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_escucha_orden);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        user = mAuth.getCurrentUser();
        hearCollection = db.collection(USERS_COLLECTION).document(user.getEmail()).collection(HEAR_COLLECTION);
        hearOrderDocument = hearCollection.document("Order");

        leftButton = findViewById(R.id.left_button);
        rightButton = findViewById(R.id.right_button);
        levelTextView = findViewById(R.id.level_text);
        scoreTextView = findViewById(R.id.score_text);
        backButton = findViewById(R.id.backButton);

        leftButton.setOnClickListener(v -> checkPattern(1));
        rightButton.setOnClickListener(v -> checkPattern(2));
        backButton.setOnClickListener(v -> {
            Intent goEscucha = new Intent(Escucha_Orden.this, Escucha_Frame.class);
            startActivity(goEscucha);
        });

        Toast.makeText(Escucha_Orden.this, "Comienza en 3 seg", Toast.LENGTH_SHORT).show();

        waitTime(3000);
        setPatterns();
        startLevel();

    }

    private void startLevel() {
        Log.d(TAG,"Inicia Level " + currentLevel);
        successCount = 0;
        faultCount = 0;
        currentPatternIndex = 0;

        PatternRunnable patternRunnable = new PatternRunnable();
        new Thread(patternRunnable).start();

        levelTextView.setText("Level " + currentLevel);
        scoreTextView.setText("Score " + successCount);
    }
    private void checkPattern(int channel){
        Log.d(TAG,"Inicia checkPattern");
        resultsLevel[currentPatternIndex2][1] = channel;
        if (channel == patterns[currentLevel-1][currentPatternIndex2][0]){
            successCount++;
        }else {
            faultCount++;
        }
        if (successCount+faultCount >= pointsToPassLevel){
            responseTime = System.currentTimeMillis() - responseTime;
            sendDataBase(hearOrderDocument, resultsLevel, currentLevel, pointsToPassLevel);
            if (successCount == pointsToPassLevel){
                currentLevel++;
                if (currentLevel <= maxLevel){
                    Toast.makeText(Escucha_Orden.this, "PASAS DE NIVEL", Toast.LENGTH_SHORT).show();
                    waitTime(2000);
                    setPatterns();
                    startLevel();
                }
                else {
                    Toast.makeText(Escucha_Orden.this, "Fin del juego", Toast.LENGTH_SHORT).show();
                    Intent goEscucha = new Intent(Escucha_Orden.this, Escucha_Frame.class);
                    startActivity(goEscucha);
                }
            }else {
                Toast.makeText(Escucha_Orden.this, "REPITES NIVEL", Toast.LENGTH_SHORT).show();
                waitTime(2000);
                setPatterns();
                startLevel();
            }
            // waitTime(3000);
        }
        currentPatternIndex2++;
    }
    private void playPattern(){
        boolean isRunning = true;
        int top = 0, played = 0;

        while (isRunning){
            if (currentPatternIndex < pointsToPassLevel){
                if (played == top) {
                    waitTime(1000);
                    if (patterns[currentLevel-1][currentPatternIndex][0] == 1) {
                        left = setTone(gain, patterns[currentLevel-1][currentPatternIndex][1], freqTone, sampleRate);
                        right = new short[left.length];
                    } else {
                        right = setTone(gain, patterns[currentLevel-1][currentPatternIndex][1], freqTone, sampleRate);
                        left = new short[right.length];
                    }
                    tone = stereoSound(left, right);
                    bufferSize = 2 * tone.length;
                    top = (int) (bufferSize * 0.25);

                    audioTrack = new AudioTrack(
                            AudioManager.STREAM_MUSIC,
                            sampleRate,
                            AudioFormat.CHANNEL_OUT_STEREO,
                            AudioFormat.ENCODING_PCM_16BIT,
                            bufferSize,
                            AudioTrack.MODE_STATIC);

                    audioTrack.write(tone, 0, tone.length);
                    waitTime(rangeMs[currentLevel-1][2]);
                    audioTrack.play();
                    currentPatternIndex++;
                }
            }else {
                if (played == top){
                    currentPatternIndex2 = 0;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            leftButton.setEnabled(true);
                            rightButton.setEnabled(true);
                            scoreTextView.setText("Score " + successCount);
                        }
                    });
                    isRunning = false;
                }
            }
            played = audioTrack.getPlaybackHeadPosition();
        }
        responseTime = System.currentTimeMillis();
    }
    private void setPatterns(){
        int minMs, maxMs, tmpRandom;
        Random random = new Random();

        leftButton.setEnabled(false);
        rightButton.setEnabled(false);


        for (int j = 0; j<pointsToPassLevel;j++){
            patterns[currentLevel-1][j][0] = random.nextInt(2)+1;
            minMs = (int)(rangeMs[currentLevel-1][0]/10);
            maxMs = (int)(rangeMs[currentLevel-1][1]/10);
            tmpRandom = (random.nextInt((maxMs-minMs)+1) + minMs) * 10;
            patterns[currentLevel-1][j][1] = tmpRandom;
            resultsLevel[j][0] = patterns[currentLevel-1][j][0];
        }
        // Imprimir
        Log.d("MATRIX", "Set Level " + String.valueOf(currentLevel));
        for (int j = 0; j < pointsToPassLevel; j++) {
            Log.d("MATRIX", "" + patterns[currentLevel-1][j][0] + "    " + patterns[currentLevel-1][j][1]);
        }
    }
    private short[] setTone(double gain, int durationTimeMs, int freq, int sampleRate) {
        int toneSize = (int)(sampleRate * durationTimeMs * 0.001);
        short[] tone = new short[toneSize];
        for (int i = 0; i < toneSize; i++) {
            double t = (double) i / sampleRate;
            tone[i] = (short) (gain * Math.sin(2 * Math.PI * freq * t) * Short.MAX_VALUE);
        }
        return tone;
    }
    private short[] stereoSound(short[] left, short[] right){

        int stereoSoundSize;
        //asignacion tamaño array estereo
        if (left.length > right.length){
            stereoSoundSize = 2*left.length;
        }else {
            stereoSoundSize = 2*right.length;
        }

        short[] stereoSoundArray = new short[stereoSoundSize];

        //inicializar array
        for (int i = 0; i<stereoSoundSize; i++){
            stereoSoundArray[i]=0;
        }
        //left
        for (int i = 0; i<left.length; i++){
            stereoSoundArray[2*i]=left[i];
        }
        //right
        for (int i = 0; i<right.length; i++){
            stereoSoundArray[2*i+1]=right[i];
        }

        return stereoSoundArray;
    }
    private void waitTime(int ms){
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
    class PatternRunnable implements Runnable {
        @Override
        public void run() {
            playPattern();
        }
    }

    private void sendDataBase(DocumentReference doc, int[][] matrix, int level, int pointsWIN){
        Map<String, Object> mapa = new HashMap<>();
        List<String> resultado = new ArrayList<>();
        List<String> patron = new ArrayList<>();

        for(int i = 0;i<pointsWIN;i++){
            if (matrix[i][0] == 1) patron.add("L");
            else patron.add("R");
            if (matrix[i][1] == 1) resultado.add("L");
            else resultado.add("R");

        }

        mapa.put("Result Level " + level, resultado);
        mapa.put("Time Level " + level, responseTime);
        mapa.put("Pattern Level " + level, patron);

        doc.set(mapa, SetOptions.merge()).addOnSuccessListener(unused -> Log.d(TAG, "Enviado"));
    }
}