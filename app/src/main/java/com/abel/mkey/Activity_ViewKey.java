package com.abel.mkey;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;

public class Activity_ViewKey extends AppCompatActivity {
    protected TextView plataforma;
    protected TextView usuario;
    protected TextView contrasena;
    protected int position;
    protected FloatingActionButton verKey;
    boolean isPasswordVisible = false;

    private Handler handler = new Handler();
    private Runnable hidePasswordRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_view_key);
        plataforma = findViewById(R.id.textViewPlatform);
        usuario = findViewById(R.id.textViewMail);
        contrasena = findViewById(R.id.textViewPassword);
        verKey = findViewById(R.id.showKey);
        position = getIntent().getIntExtra("position",-1);
        String platform= getIntent().getStringExtra("platform");
        String user= getIntent().getStringExtra("user");
        String password= getIntent().getStringExtra("password");

        plataforma.setText(platform);
        usuario.setText(user);
        contrasena.setText("********");

        verKey.setImageResource(R.drawable.visibility_off); // Ícono de ojo cerrado inicialmente
        verKey.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isPasswordVisible) {
                    contrasena.setText("********"); // Ocultar la contraseña
                    verKey.setImageResource(R.drawable.visibility_off); // Cambia a ícono de ojo cerrado
                } else {
                    contrasena.setText(password); // Mostrar la contraseña
                    verKey.setImageResource(R.drawable.baseline_visibility_24); // Cambia a ícono de ojo abierto
                    scheduleHidePassword(); // Programar ocultar contraseña después de 15 segundos
                }
                isPasswordVisible = !isPasswordVisible;
            }
        });

        File fileToEncrypt = new File("storage/emulated/0/Android/data/com.abel.mkey/files/mkeyDir/wFGec1A6Hc.txt");
        String encryptedPassword = Singleton.getInstance().getGoogleId();;
        FolderEncryptor.encryptFile(fileToEncrypt,encryptedPassword);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.blueActionBar));
    }

    private void scheduleHidePassword() {
        if (hidePasswordRunnable != null) {
            handler.removeCallbacks(hidePasswordRunnable);
        }

        hidePasswordRunnable = new Runnable() {
            @Override
            public void run() {
                if (isPasswordVisible) {
                    contrasena.setText("********"); // Ocultar la contraseña
                    verKey.setImageResource(R.drawable.visibility_off); // Cambia a ícono de ojo cerrado
                    isPasswordVisible = false;
                }
            }
        };

        handler.postDelayed(hidePasswordRunnable, 10000); // Ocultar después de 15 segundos
    }
}