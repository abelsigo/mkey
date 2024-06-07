// PinAuthActivity.java
package com.abel.mkey;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class PinAuth extends AppCompatActivity {
    private EditText pinEditText;
    public TextView infoPin;
    private Button submitButton;
    private SharedPreferences sharedPreferences;
    private static final int MIN_PIN_LENGTH = 6;
    private static final int MAX_PIN_LENGTH = 12;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pin_auth);

        sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);

        pinEditText = findViewById(R.id.pinEditText);
        infoPin = findViewById(R.id.textViewInfoPin);
        submitButton = findViewById(R.id.authenticateButton);

        boolean isFirstTime = sharedPreferences.getBoolean("isFirstTimePinAuth", true);
        if (isFirstTime) {
            // Show infoPin TextView
            infoPin.setVisibility(View.VISIBLE);
            // Update shared preferences to mark that the app has been opened before
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("isFirstTimePinAuth", false);
            editor.apply();
        } else {
            // Hide infoPin TextView
            infoPin.setVisibility(View.GONE);
        }
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String enteredPin = pinEditText.getText().toString();
                if(enteredPin.isEmpty()) {
                    Toast.makeText(PinAuth.this, getString(R.string.nActivityPinAuthToastE1), Toast.LENGTH_SHORT).show();
                }else {
                    if(enteredPin.length() < MIN_PIN_LENGTH || enteredPin.length() > MAX_PIN_LENGTH){
                        Toast.makeText(PinAuth.this, getString(R.string.nActivityPinAuthToastE2), Toast.LENGTH_SHORT).show();
                    }else {
                        if (validatePin(enteredPin)) {
                            Intent resultIntent = new Intent();
                            setResult(Activity.RESULT_OK, resultIntent);
                            finish();
                        } else {
                            Toast.makeText(PinAuth.this, getString(R.string.nActivityPinAuthToastE3), Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
        });
    }

    private boolean validatePin(String pin) {
        String savedPin = sharedPreferences.getString("user_pin", null);
        if (savedPin == null) {
            // Si no hay PIN guardado, guardamos el PIN ingresado
            sharedPreferences.edit().putString("user_pin", pin).apply();
            return true;
        } else {
            // Si ya hay un PIN guardado, lo comparamos con el ingresado
            return savedPin.equals(pin);
        }
    }
}
