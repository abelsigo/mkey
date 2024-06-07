package com.abel.mkey;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
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

import java.io.File;

public class Activity_EditKey extends AppCompatActivity {

    protected EditText plataforma;
    protected EditText usuario;
    protected EditText contrasena;
    public Button bEditar;
    TextView NombreActividad;
    protected int position;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_key);
        plataforma = findViewById(R.id.textEditPlataforma);
        usuario = findViewById(R.id.textEditUsuario);
        contrasena = findViewById(R.id.textEditContrasena);
        bEditar = findViewById(R.id.bGuardarKeyEdit);
        bEditar.setText(getString(R.string.nActivityEditB));
        NombreActividad = findViewById(R.id.textView2);
        NombreActividad.setText(getString(R.string.nActivityEdit));
        position = getIntent().getIntExtra("position",-1);
        String platform= getIntent().getStringExtra("platform");
        String user= getIntent().getStringExtra("user");
        String password= getIntent().getStringExtra("password");
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.blueActionBar));
        plataforma.setText(platform);
        usuario.setText(user);
        contrasena.setText(password);

        bEditar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String plat = String.valueOf(plataforma.getText());
                String us = String.valueOf(usuario.getText());
                String pas = String.valueOf(contrasena.getText());
                Intent intent = new Intent();
                intent.putExtra("position",position);
                intent.putExtra("newPlatform",plataforma.getText().toString());
                intent.putExtra("newUser",usuario.getText().toString());
                intent.putExtra("newPassword",contrasena.getText().toString());

                Log.d(TAG, "\nIntent: "+plat+us+pas);
                setResult(RESULT_OK,intent);
                finish();
            }
        });
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                setResult(RESULT_CANCELED);
                finish();
            }
        };
        getOnBackPressedDispatcher().addCallback(this, callback);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}