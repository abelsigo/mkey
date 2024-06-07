package com.abel.mkey;

import android.app.ActivityOptions;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;

import org.passay.CharacterRule;
import org.passay.EnglishCharacterData;
import org.passay.PasswordGenerator;

import java.util.Random;


public class KeyGenerator extends AppCompatActivity {

    private Button bGenerarKey;
    private TextView showKey;
    TextView NombreActividad;
    private Button KeyList;
    private Button Cuenta;
    private Button Generador;
    private EditText Contrasena;
    private Button Copiar;
    private MaterialButtonToggleGroup toggleButtonGroup;
    private MaterialButton buttonRandomPassword;
    private MaterialButton buttonWordPassword;
    String word;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_key_generator);
        EdgeToEdge.enable(this);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        NombreActividad = findViewById(R.id.textView2);
        NombreActividad.setText(getString(R.string.nActivityGenerator));
        bGenerarKey = findViewById(R.id.bGenerateKey);
        toggleButtonGroup = findViewById(R.id.toggleButtonGroup);
        buttonRandomPassword = findViewById(R.id.buttonRandomPassword);
        buttonWordPassword = findViewById(R.id.buttonWordPassword);
        //bGenerarKey.setText(getString(R.string.nActivityGeneratorB));
        showKey = findViewById(R.id.textKeyShow);
        KeyList = findViewById(R.id.bKeys);
        KeyList.setText(getString(R.string.nActivityKeyListN));
        Generador = findViewById(R.id.bGenerador);
        Generador.setText(getString(R.string.nActivityGeneratorN));
        Cuenta = findViewById(R.id.bCuenta);
        Cuenta.setText(getString(R.string.nActivityAccountN));
        Contrasena = findViewById(R.id.textEditContrasenaGenerator);
        Copiar = findViewById(R.id.bCopy);
        buttonWordPassword.setChecked(true);
        toggleButtonGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                if (checkedId == R.id.buttonWordPassword) {
                    Contrasena.setVisibility(View.VISIBLE);
                } else {
                    Contrasena.setVisibility(View.GONE);
                }
            }
        });
        bGenerarKey.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (buttonRandomPassword.isChecked()) {
                    word = Contrasena.getText().toString();
                    String password = generatePasswordFromWord(word);
                    showKey.setText(password);
                    Copiar.setVisibility(View.VISIBLE);
                } else {
                    if (!Contrasena.getText().toString().isEmpty()) {
                        word = Contrasena.getText().toString();
                        String password = generatePasswordFromWord(word);
                        showKey.setText(password);
                        Copiar.setVisibility(View.VISIBLE);
                    } else if (Contrasena.getText().toString().isEmpty()) {
                        Toast.makeText(KeyGenerator.this, getString(R.string.nKeyGeneratorTextField), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
        KeyList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
                overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
            }
        });
        Cuenta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(KeyGenerator.this, KeyAccount.class);
                ActivityOptions options = ActivityOptions.makeCustomAnimation(KeyGenerator.this, R.anim.slide_in, R.anim.slide_out);
                startActivity(intent, options.toBundle());
                finish();
            }
        });
        Copiar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String textToCopy = showKey.getText().toString();
                copyToClipboard(textToCopy);
            }
        });
    getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.blueActionBar));
    }
    private String generateSecurePassword() {
        // Create character rule for lower case
        CharacterRule LCR = new CharacterRule(EnglishCharacterData.LowerCase);
        LCR.setNumberOfCharacters(2);

        // Create character rule for upper case
        CharacterRule UCR = new CharacterRule(EnglishCharacterData.UpperCase);
        UCR.setNumberOfCharacters(2);

        // Create character rule for digit
        CharacterRule DR = new CharacterRule(EnglishCharacterData.Digit);
        DR.setNumberOfCharacters(2);

        // Create character rule for special characters
        CharacterRule SR = new CharacterRule(EnglishCharacterData.Special);
        SR.setNumberOfCharacters(2);

        // Create instance of the PasswordGenerator class
        PasswordGenerator passGen = new PasswordGenerator();

        // Generate the password using Passay library
        return passGen.generatePassword(8, SR, LCR, UCR, DR);
    }

    private String generatePasswordFromWord(String word) {
        // Define la longitud total de la contraseña, incluyendo la longitud de la palabra proporcionada
        int totalLength = 12; // Por ejemplo, una longitud total de 8 caracteres

        // Calcula la longitud de la palabra proporcionada
        int wordLength = word.length();

        // Calcula la longitud de la parte aleatoria de la contraseña
        int randomPartLength = totalLength - wordLength;

        // Si la palabra proporcionada es más larga que la longitud total, utiliza solo los primeros caracteres
        if (wordLength >= totalLength) {
            return word.substring(0, totalLength);
        }

        // Calcula la longitud de la primera parte de la palabra
        int firstPartLength = wordLength / 2;

        // Calcula la longitud de la segunda parte de la palabra
        int secondPartLength = wordLength - firstPartLength;

        // Obtiene la primera parte de la palabra
        String firstPart = word.substring(0, firstPartLength);

        // Obtiene la segunda parte de la palabra
        String secondPart = word.substring(firstPartLength);

        // Completa la primera parte con caracteres aleatorios
        firstPart += generateRandomString(randomPartLength / 2);

        // Completa la segunda parte con caracteres aleatorios
        secondPart = generateRandomString(randomPartLength / 2) + secondPart;

        // Combina las dos partes y la parte aleatoria para formar la contraseña completa
        return firstPart + secondPart;
    }

    private String generateRandomString(int length) {
        // Define la lista de caracteres posibles que se pueden incluir en la parte aleatoria de la contraseña
        String possibleCharacters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()_+";

        // Crea una instancia del generador de números aleatorios
        Random random = new Random();

        // Crea un StringBuilder para construir la parte aleatoria de la contraseña
        StringBuilder stringBuilder = new StringBuilder();

        // Genera caracteres aleatorios y los agrega al StringBuilder
        for (int i = 0; i < length; i++) {
            int randomIndex = random.nextInt(possibleCharacters.length());
            stringBuilder.append(possibleCharacters.charAt(randomIndex));
        }

        // Devuelve la parte aleatoria de la contraseña como una cadena
        return stringBuilder.toString();
    }

    private void copyToClipboard(String text) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("simple text", text);
        clipboard.setPrimaryClip(clip);

        Toast.makeText(this, "Key copiada", Toast.LENGTH_SHORT).show();
    }


}