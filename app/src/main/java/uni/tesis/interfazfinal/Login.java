package uni.tesis.interfazfinal;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class Login extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String TAG = "TAG";

    private final String USERS_COLLECTION = "User";
    private final String AGE = "Edad";
    private final String NAME = "Nombre";
    private final String USERNAME = "Username";

    private Button loginButtonReg, loginButton;
    private EditText username, password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        FirebaseApp.initializeApp(this);
        username = findViewById(R.id.username);
        password = findViewById(R.id.password);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        loginButtonReg = findViewById(R.id.loginButtonReg);
        loginButton = findViewById(R.id.loginButtonLog);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loginUser();
                // Log.d(TAG, "user " + mAuth.getCurrentUser().getDisplayName() + "\nID " + mAuth.getCurrentUser().getUid());
            }
        });

        loginButtonReg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRegisterDialog();
            }
        });
    }

    private void loginUser(){
        String usuario = username.getText().toString();
        String contraseña = password.getText().toString();
        Log.d(TAG, "Usuario " + usuario);

        if (TextUtils.isEmpty(usuario) || TextUtils.isEmpty(contraseña)) {
            Toast.makeText(Login.this, "Por favor, ingrese su usuario y contraseña", Toast.LENGTH_SHORT).show();
            return;
        }
        mAuth.signInWithEmailAndPassword(usuario+"@app.com",contraseña).addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                Log.d(TAG, "user " + mAuth.getCurrentUser().getDisplayName() + "\nID " + mAuth.getCurrentUser().getUid());
                Intent goMenu = new Intent(Login.this, MainActivity.class);
                startActivity(goMenu);
                finish();
            }else {
                // Error en el inicio de sesión
                Toast.makeText(Login.this, "No existe usuario o su contraseña es incorrecta . Regístrese por favor", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void showRegisterDialog(){
        AlertDialog.Builder builder=new AlertDialog.Builder(this, R.style.CustomAlertDialogStyle);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.registro,null);
        dialogView.setBackgroundColor(getResources().getColor(R.color.white));
        dialogView.setBackgroundResource(R.drawable.rounded_white_background);

        builder.setView(dialogView);

        EditText usuarioN, contraseñaN, edadN, nameN;

        usuarioN = dialogView.findViewById(R.id.usuarioN);
        contraseñaN = dialogView.findViewById(R.id.contraseñaN);
        edadN = dialogView.findViewById(R.id.edadN);
        nameN = dialogView.findViewById(R.id.nameN);


        builder.setPositiveButton("Registrar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                FirebaseUser user = mAuth.getCurrentUser();

                String usuario = usuarioN.getText().toString().trim();
                String contraseña = contraseñaN.getText().toString().trim();
                String edad = edadN.getText().toString();
                String nombre = nameN.getText().toString();

                if (TextUtils.isEmpty(usuario) || TextUtils.isEmpty(contraseña) || TextUtils.isEmpty(edad)) {
                    Toast.makeText(Login.this, "Por favor, complete todos los campos", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (contraseña.length() < 6) {
                    Toast.makeText(Login.this, "Ingrese una contraseña válida (al menos 6 caracteres)", Toast.LENGTH_SHORT).show();
                    return;

                }
                // Using Database
                /*
                DatabaseReference usuariosRef = FirebaseDatabase.getInstance().getReference("usuarios");

                usuariosRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()){
                            Toast.makeText(Login.this, "El usuario ya está registrado.Inicie sesión", Toast.LENGTH_SHORT).show();
                            dialog.cancel();
                        }else{
                            mAuth.createUserWithEmailAndPassword(userId+"@app.com",contraseña).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("usuarios");
                                        FirebaseUser user = mAuth.getCurrentUser();
                                        String userId = user.getUid();
                                        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                                .setDisplayName(nombre).build();
                                        user.updateProfile(profileUpdates);
                                        databaseReference.child(userId).child("edad").setValue(edad);
                                        Toast.makeText(Login.this, "Registro exitoso", Toast.LENGTH_SHORT).show();
                                    }else {
                                        // Fallo en el registro
                                        Toast.makeText(Login.this, "El usuario ya existe. Inicie sesion o digite otro usuario ", Toast.LENGTH_SHORT).show();
                                    }

                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

                 */

                // Agrega info del usuario
                Map<String, Object> userInfo = new HashMap<>();
                userInfo.put(NAME, nombre);
                userInfo.put(AGE, edad);
                userInfo.put(USERNAME,usuario);

                String mail = usuario + "@app.com";

                db.collection(USERS_COLLECTION).document(mail).get()
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                          @Override
                          public void onSuccess(DocumentSnapshot documentSnapshot) {
                              if (documentSnapshot.exists()){
                                  Toast.makeText(Login.this, "El usuario ya está registrado.Inicie sesión", Toast.LENGTH_SHORT).show();
                                  dialog.cancel();
                              }else {
                                  mAuth.createUserWithEmailAndPassword(mail,contraseña).addOnCompleteListener(task -> {
                                      if (task.isSuccessful()) {
                                          UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                                  .setDisplayName(nombre).build();
                                          user.updateProfile(profileUpdates);
                                          db.collection(USERS_COLLECTION).document(mail).set(userInfo);
                                          Toast.makeText(Login.this, "Registro exitoso", Toast.LENGTH_SHORT).show();
                                      }else {
                                          // Fallo en el registro
                                          Toast.makeText(Login.this, "El usuario ya existe. Inicie sesion o digite otro usuario ", Toast.LENGTH_SHORT).show();
                                      }
                                  });
                              }
                          }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                        }
                    });
            }
        });
        builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel(); // Cierra el diálogo
            }
        });

        // Muestra el diálogo
        AlertDialog dialog = builder.create();
        dialog.show();
        Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        Button negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
        positiveButton.setTextColor(Color.WHITE);
        positiveButton.setBackgroundResource(R.drawable.custom_edittext);
        negativeButton.setTextColor(Color.WHITE);
        negativeButton.setBackgroundResource(R.drawable.custom_edittext);

    }
}