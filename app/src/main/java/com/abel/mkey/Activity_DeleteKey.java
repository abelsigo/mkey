package com.abel.mkey;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class Activity_DeleteKey extends AppCompatActivity {

    protected EditText plataforma;
    protected EditText usuario;
    protected EditText contrasena;
    public Button bEditar;

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
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.blueActionBar));
        position = getIntent().getIntExtra("position",-1);
        String platform= getIntent().getStringExtra("platform");
        String user= getIntent().getStringExtra("user");
        String password= getIntent().getStringExtra("password");

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

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}