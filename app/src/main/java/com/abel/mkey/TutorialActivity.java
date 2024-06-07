package com.abel.mkey;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;
import androidx.core.content.ContextCompat;

public class TutorialActivity extends AppCompatActivity {


    private TextView[] tutorialTexts;
    private ImageView imageAdd;
    private int currentIndex = 0;
    private Handler handler = new Handler();
    private BiometricManager biometricManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutorial);
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.blueActionBar));
        imageAdd = findViewById(R.id.imageViewAdd);
        tutorialTexts = new TextView[]{
                findViewById(R.id.tutorialText),
                findViewById(R.id.tutorialText2),
                findViewById(R.id.tutorialText3),
                findViewById(R.id.tutorialText4),
                findViewById(R.id.tutorialText5)
        };

        Button continueButton = findViewById(R.id.continueButton);

        biometricManager = BiometricManager.from(this);

        continueButton.setOnClickListener(v -> {
            // Guardar en SharedPreferences que la aplicación ya ha sido abierta
            SharedPreferences preferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean("isFirstTime", false);
            editor.apply();

            // Mostrar el pop-up si no hay biometría disponible
            if (biometricManager.canAuthenticate() != BiometricManager.BIOMETRIC_SUCCESS) {
                showPopupFor5Seconds();
            } else {
                // Ir a la MainActivity directamente si hay biometría
                navigateToMainActivity();
            }
        });
    }

    private void navigateToMainActivity() {
        Intent intent = new Intent(TutorialActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private Runnable showNextTextView = new Runnable() {
        @Override
        public void run() {
            if (currentIndex < tutorialTexts.length) {
                tutorialTexts[currentIndex].setVisibility(View.VISIBLE);
                currentIndex++;
                handler.postDelayed(this, 2000); // Cambia el tiempo de retardo según tus necesidades
            } else {
                // Ya se han mostrado todos los TextView
                handler.removeCallbacks(this);
            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        // Comienza a mostrar los TextView cuando la actividad se reanuda
        int delay = 0;
        for (int i = 0; i < tutorialTexts.length; i++) {
            animateTextView(tutorialTexts[i], delay);
            delay += 3000; // Puedes ajustar este valor para cambiar la velocidad de la animación
        }
        handler.postDelayed(() -> imageAdd.setVisibility(View.VISIBLE), 3600);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Detiene la ejecución del Handler cuando la actividad está en pausa
        handler.removeCallbacks(showNextTextView);
    }

    private void animateTextView(final TextView textView, final int delay) {
        AnimationSet set = new AnimationSet(true);
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.write_tutorial);
        animation.setStartOffset(delay);
        set.addAnimation(animation);
        textView.startAnimation(set);
        textView.setVisibility(View.VISIBLE);
    }

    private void showPopupFor5Seconds() {
        // Inflar el layout del pop-up
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.popup_pin, null);

        // Construir el diálogo
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);

        // Crear y mostrar el diálogo
        AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();

        // Cerrar el diálogo después de 5 segundos y navegar a la MainActivity
        new Handler().postDelayed(() -> {
            dialog.dismiss();
            navigateToMainActivity();
        }, 5000); // 5000 milisegundos = 5 segundos
    }
}
