package com.abel.mkey;

import static android.content.ContentValues.TAG;

import android.app.ActivityOptions;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.squareup.picasso.Picasso;

public class KeyAccount extends AppCompatActivity {

    private TextView email;
    private ImageView emailImg;
    private Button bClose;
    TextView NombreActividad;
    private Button Generador;
    private Button KeyList;
    private Button Cuenta;
    GoogleSignInAccount gsa;
    private GoogleSignInClient gsc;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_key_account);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        gsc = GoogleSignIn.getClient(this, GoogleSignInOptions.DEFAULT_SIGN_IN);

        email = findViewById(R.id.textEmailUser);
        emailImg = findViewById(R.id.imageViewUser);
        bClose = findViewById(R.id.bSignOut);
        NombreActividad = findViewById(R.id.textView2);
        NombreActividad.setText(getString(R.string.nActivityAccount));
        Generador = findViewById(R.id.bGenerador);
        Generador.setText(getString(R.string.nActivityGeneratorN));
        KeyList = findViewById(R.id.bKeys);
        KeyList.setText(getString(R.string.nActivityKeyListN));
        Cuenta = findViewById(R.id.bCuenta);
        Cuenta.setText(getString(R.string.nActivityAccountN));
        bClose = findViewById(R.id.bSignOut);
        bClose.setText(getString(R.string.nActivityAccountB));
        gsa = GoogleSignIn.getLastSignedInAccount(this);

        if (gsa != null) {
            String correo = gsa.getEmail();
            email.setText(correo);
            String url = String.valueOf(gsa.getPhotoUrl());
            Log.d(TAG, "URL de la foto de perfil: " + url);
            Picasso.get().load(url).transform(new CircleTransformation()).into(emailImg);
        }
        Generador.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(KeyAccount.this, KeyGenerator.class);
                ActivityOptions options = ActivityOptions.makeCustomAnimation(KeyAccount.this, R.anim.slide_in, R.anim.slide_out);
                startActivity(intent, options.toBundle());
                finish();
            }
        });
        KeyList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
                overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
            }
        });
        emailImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeThemePreference();
            }
        });
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.blueActionBar));
        bClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signOut();
            }
        });
    }

    // Método para cerrar sesión
    private void signOut() {
        if (gsc != null) {
            gsc.signOut()
                    .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            // Acciones después de cerrar sesión
                            java.io.File localFile = new java.io.File("storage/emulated/0/Android/data/com.abel.mkey/files/mkeyDir/wFGec1A6Hc.txt.hidden");
                            localFile.delete();
                            Intent intent = new Intent(KeyAccount.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    });
        }
    }
    private void changeThemePreference() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        boolean isDarkMode = sharedPreferences.getBoolean("dark_mode", false);
        editor.putBoolean("dark_mode", !isDarkMode); // Cambia el valor del tema
        editor.apply();
    }

}