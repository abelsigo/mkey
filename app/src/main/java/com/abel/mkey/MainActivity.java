package com.abel.mkey;

import static android.content.ContentValues.TAG;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.api.services.drive.DriveScopes;
import com.google.auth.oauth2.GoogleCredentials;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.Executor;


public class MainActivity extends AppCompatActivity {

    GoogleSignInOptions gso;
    GoogleSignInClient gsc;
    Button bIniciarSesion;

    private Executor executor;
    private BiometricPrompt biometricPrompt;
    private BiometricPrompt.PromptInfo promptInfo;

    private DriveServiceHelper driveServiceHelper;
    protected String googleAccountId;

    private boolean isBiometricAuthSuccess = false;
    private boolean isPinAuthSuccess = false;
    private static final int REQUEST_CODE_PIN = 2000;
    private ProgressDialog progressDialog;
    private SharedPreferences sharedPreferences;
    LoadingActivity loadingActivity;

    private int failedAttempts = 0;
    private final int MAX_FAILED_ATTEMPTS = 5;
    private boolean isBlocked = false;
    private Handler handler = new Handler();
    private boolean isExiting = false;
    private boolean isUserAttemptingAuth = false;
    private long blockTimeMillis = 0;
    private boolean isFingerprintVerified = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
        setContentView(R.layout.activity_main);
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.blueActionBar));
        sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        boolean isFirstTime = sharedPreferences.getBoolean("isFirstTime", true);

        // Verificar si la aplicación está bloqueada al abrirse
        boolean isPreviouslyBlocked = sharedPreferences.getBoolean("isBlocked", false);
        long blockTimeMillis = sharedPreferences.getLong("blockTimeMillis", 0);
        long currentTimeMillis = System.currentTimeMillis();

        if (isPreviouslyBlocked && blockTimeMillis > 0) {
            long elapsedTimeMillis = currentTimeMillis - blockTimeMillis;
            if (elapsedTimeMillis < 30000) { // Si han pasado menos de 30 segundos y se bloqueó
                // Mostrar pantalla de bloqueo
                setContentView(R.layout.activity_blocked);
                return;
            } else {
                // Restablecer el estado de bloqueo después del tiempo de espera
                sharedPreferences.edit().remove("blockTimeMillis").apply(); // Eliminar el tiempo de bloqueo guardado
                sharedPreferences.edit().remove("isBlocked").apply(); // Eliminar el estado de bloqueo
            }
        }

        if (isFirstTime) {
            Intent intent = new Intent(MainActivity.this, TutorialActivity.class);
            startActivity(intent);
            finish();
            return; // Detener la ejecución del resto del código
        }

        bIniciarSesion = findViewById(R.id.bSignIn);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().requestScopes(new Scope(DriveScopes.DRIVE_FILE), new Scope(DriveScopes.DRIVE)).build();
        gsc = GoogleSignIn.getClient(this,gso);

        bIniciarSesion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isBiometricAuthSuccess || isPinAuthSuccess) {
                    signIn();
                } else {
                    Toast.makeText(MainActivity.this, getString(R.string.nActivityMainToastE1), Toast.LENGTH_SHORT).show();
                }
            }
        });

        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        boolean isPinAlreadyEntered = sharedPreferences.getBoolean("pin_entered", false);
        if (isPinAlreadyEntered) {
            // Si el PIN ya se ha introducido anteriormente, permitir el inicio de sesión sin solicitar el PIN nuevamente
            isPinAuthSuccess = true;
            enableSignInButton();
        } else {
            // Si el PIN aún no se ha introducido, continuar con el flujo normal y solicitar el PIN
            checkBiometricSupport();
            loadingActivity = new LoadingActivity(this);
        }

    }


    private void checkBiometricSupport() {
        BiometricManager biometricManager = BiometricManager.from(this);
        switch (biometricManager.canAuthenticate()) {
            case BiometricManager.BIOMETRIC_SUCCESS:
                initBiometricPrompt();
                break;
            case BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE:
            case BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE:
            case BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED:
                //No biometric features available or none enrolled
                navigateToPinAuthentication();
                break;
        }
    }

    private void initBiometricPrompt() {
        executor = ContextCompat.getMainExecutor(this);
        isUserAttemptingAuth = true;
        biometricPrompt = new BiometricPrompt(MainActivity.this, executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                Log.e(TAG, "Authentication error: " + errorCode + " - " + errString);
                if (errorCode != BiometricPrompt.ERROR_USER_CANCELED) {
                    // Bloquear la aplicación solo si no fue cancelado por el usuario
                    blockApp();
                }
                finish();
            }

            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                Log.d(TAG, "Authentication succeeded");
                isBiometricAuthSuccess = true;
                isFingerprintVerified = true;
                enableSignInButton();
                //bIniciarSesion.performClick();
                failedAttempts = 0;
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                failedAttempts++;
                if (failedAttempts >= MAX_FAILED_ATTEMPTS) {
                    // Informar al usuario y sugerir el uso de PIN
                    blockApp();
                    Toast.makeText(getApplicationContext(), "Demasiados intentos fallidos.", Toast.LENGTH_LONG).show();
                }
            }
        });

        promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle(getString(R.string.nMainActivityFprintTitle))
                .setSubtitle("")
                .setNegativeButtonText(getString(R.string.nMainActivityFprintNB))
                .build();

        biometricPrompt.authenticate(promptInfo);
    }

    private void navigateToPinAuthentication() {
        Intent intent = new Intent(this, PinAuth.class);
        startActivityForResult(intent, REQUEST_CODE_PIN);
    }


    protected void signIn(){
        if (isFingerprintVerified || isPinAuthSuccess) { // Verificar si la huella ya ha sido verificada
            Intent signInIntent = gsc.getSignInIntent();
            startActivityForResult(signInIntent, 1000);
        } else {
            Toast.makeText(MainActivity.this, getString(R.string.nActivityMainToastE1), Toast.LENGTH_SHORT).show();
        }
    }


    public void signOut(GoogleSignInClient googleSignInClient) {
        googleSignInClient.signOut()
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        // Acciones después de cerrar sesión
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        googleAccountId = null;
        if (account != null) {
            googleAccountId = account.getId();
            // Aquí puedes utilizar googleAccountId como desees, por ejemplo, pasarlo a la próxima actividad
            Log.d(TAG, "ID de cuenta de Google: " + googleAccountId);
        }

        if (requestCode == REQUEST_CODE_PIN && resultCode == Activity.RESULT_OK) {
            isPinAuthSuccess = true;
            enableSignInButton();
            //bIniciarSesion.performClick();
        } else if (requestCode == 1000 && resultCode == Activity.RESULT_OK) {
            //isPinAuthSuccess = true;
            enableSignInButton();
            //bIniciarSesion.performClick();

        }

        if(requestCode == 1000){
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                driveServiceHelper = new DriveServiceHelper(this);
                loadingActivity.ShowDialog(getString(R.string.cardLoading));
                //showLoading();
                checkIfFileExists("wFGec1A6Hc.txt.hidden");
                compareFileModificationDates("storage/emulated/0/Android/data/com.abel.mkey/files/mkeyDir/wFGec1A6Hc.txt.hidden", "wFGec1A6Hc.txt.hidden");
                task.getResult(ApiException.class);
            } catch (ApiException e) {
                Log.e(TAG, "Error al iniciar sesión con Google", e);
                Toast.makeText(this, getString(R.string.nMainActivityFprintSignInE), Toast.LENGTH_SHORT).show();
            }
        }
    }


    private void checkIfFileExists(String fileName) {
        if (driveServiceHelper != null) {
            driveServiceHelper.isFileExist(fileName)
                    .addOnSuccessListener(isExist -> {
                        if (isExist) {
                            Log.e(TAG,"El archivo existe.");
                        } else {
                            Log.e(TAG,"El archivo no existe.");
                            String filePath1 = "storage/emulated/0/Android/data/com.abel.mkey/files/mkeyDir/wFGec1A6Hc.txt";
                            String filePath2 = "storage/emulated/0/Android/data/com.abel.mkey/files/mkeyDir/wFGec1A6Hc.txt.hidden";
                            File file1 = new File(filePath1);
                            File file2 = new File(filePath2);
                            if(file1.exists()||file2.exists()){
                                System.out.println("El archivo existe en la ruta especificada.");
                            }else{
                                try {
                                    FileOutputStream fos = new FileOutputStream(file1);
                                    String content = "";
                                    // Escribir el contenido en el archivo
                                    fos.write(content.getBytes());

                                    // Cerrar el flujo de salida
                                    fos.close();
                                }catch (IOException e){
                                    e.printStackTrace();
                                }
                            }
                            updateFileFromDrive();

                            navigateToSecondActivity(googleAccountId);

                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e("MainActivity", "No se pudo verificar la existencia del archivo.", e);
                    });
        } else {
            Log.e(TAG,"DriveServiceHelper no está inicializado.");
        }
    }

    private void compareFileModificationDates(String localFilePath, String driveFileName) {
        // Obtener la fecha de modificación del archivo local
        java.io.File localFile = new java.io.File(localFilePath);
        long localFileLastModified = localFile.lastModified();

        if (driveServiceHelper != null) {
            // Obtener la fecha de modificación del archivo en Google Drive
            driveServiceHelper.getFileLastModifiedDate(driveFileName)
                    .addOnSuccessListener(driveFileLastModified -> {
                        // Comparar las fechas
                        if (localFileLastModified > driveFileLastModified) {
                            deleteFileFromDrive("wFGec1A6Hc.txt.hidden");
                            Log.e(TAG,"El archivo local es más reciente.");
                            DriveServiceHelper driveServiceHelper = new DriveServiceHelper(MainActivity.this);
                            driveServiceHelper.getFolderIdByName("mkey", new DriveServiceHelper.FolderIdCallback() {
                                @Override
                                public void onFolderIdReceived(String folderId) {
                                    if (folderId != null) {
                                        String filePath = "storage/emulated/0/Android/data/com.abel.mkey/files/mkeyDir/wFGec1A6Hc.txt.hidden";
                                        // Aquí puedes usar el folderId obtenido para subir un archivo a la carpeta
                                        driveServiceHelper.uploadFileToFolder(filePath, folderId);
                                        navigateToSecondActivity(googleAccountId);
                                    } else {
                                        // Maneja el caso en el que no se encuentre la carpeta
                                        Log.e(TAG, "No se encontró la carpeta con el nombre 'mkey'"+getString(R.string.welcome2));
                                    }
                                }
                            });
                        } else if (localFileLastModified < driveFileLastModified) {
                            Log.e(TAG,"El archivo en Google Drive es más reciente.");
                            //Descargar archivo de drive
                            File file = new File("storage/emulated/0/Android/data/com.abel.mkey/files/mkeyDir/wFGec1A6Hc.txt.hidden");
                            downloadAndDeleteLocalFile("wFGec1A6Hc.txt.hidden",file);

                        } else {
                            Log.e(TAG,"Ambos archivos tienen la misma fecha de modificación.");
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e("MainActivity", "Error al obtener la fecha de modificación del archivo en Google Drive.", e);
                    });
        } else {
            Log.e(TAG,"DriveServiceHelper no está inicializado.");
        }
    }

    private void deleteFileFromDrive(String fileName) {
        driveServiceHelper.getFileIdByName(fileName)
                .addOnSuccessListener(fileId -> {
                    if (fileId != null) {
                        driveServiceHelper.deleteFile(fileId)
                                .addOnSuccessListener(aVoid -> {
                                    Log.e(TAG,"Archivo eliminado correctamente.");
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("MainActivity", "Error al eliminar el archivo.", e);
                                    Log.e(TAG,"Error al eliminar el archivo.");
                                });
                    } else {
                        Log.e("MainActivity", "El archivo no se encontró en Google Drive.");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("MainActivity", "Error al obtener el ID del archivo.", e);
                });
    }

    private void downloadAndDeleteLocalFile(String fileName, java.io.File localFile) {
        // Obtener el ID del archivo de Google Drive
        driveServiceHelper.getFileIdByName(fileName)
                .addOnSuccessListener(fileId -> {
                    if (fileId != null) {
                        // Verificar y eliminar el archivo local si existe
                        if (localFile.exists()) {
                            if (localFile.delete()) {
                                Log.e("MainActivity", "Archivo local eliminado correctamente.");
                            } else {
                                Log.e("MainActivity", "No se pudo eliminar el archivo local.");
                                return; // Si no se puede eliminar el archivo local, detenemos el proceso.
                            }
                        }

                        // Crear el directorio de destino si no existe
                        File mkeyDir = new File(getExternalFilesDir(null), "mkeyDir");
                        if (!mkeyDir.exists()) {
                            boolean created = mkeyDir.mkdirs();
                            if (!created) {
                                Log.e("MainActivity", "No se pudo crear la carpeta mkeyDir");
                                return;
                            }
                        }

                        // Definir el archivo de destino
                        java.io.File destinationFile = new java.io.File(mkeyDir, fileName);

                        // Descargar el archivo desde Google Drive
                        driveServiceHelper.downloadFile(fileId, destinationFile)
                                .addOnSuccessListener(aVoid -> {
                                    Log.e("MainActivity", "Archivo descargado correctamente.");
                                    navigateToSecondActivity(googleAccountId);
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("MainActivity", "Error al descargar el archivo.", e);
                                });
                    } else {
                        Log.e("MainActivity", "El archivo no se encontró en Google Drive.");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("MainActivity", "Error al obtener el ID del archivo.", e);
                });
    }

    protected void updateFileFromDrive(){
        DriveServiceHelper driveServiceHelper = new DriveServiceHelper(MainActivity.this);

        // Llamar al método uploadFileToFolder para subir el archivo a Google Drive
        driveServiceHelper.getFolderIdByName("mkey", new DriveServiceHelper.FolderIdCallback() {
            @Override
            public void onFolderIdReceived(String folderId) {
                if (folderId != null) {
                    String filePath = "storage/emulated/0/Android/data/com.abel.mkey/files/mkeyDir/wFGec1A6Hc.txt.hidden";
                    // Aquí puedes usar el folderId obtenido para subir un archivo a la carpeta
                    driveServiceHelper.uploadFileToFolder(filePath, folderId);
                } else {
                    // Maneja el caso en el que no se encuentre la carpeta
                    Log.e(TAG, "No se encontró la carpeta con el nombre 'mkey'");
                }
            }
        });
    }

    protected void navigateToSecondActivity(String googleAccountId) {
        loadingActivity.HideDialog();
        Singleton.getInstance().setGoogleId(googleAccountId);
        Intent intent = new Intent(MainActivity.this, ActivityKeyList.class);
        startActivity(intent);
        finish();
    }

    private void enableSignInButton() {
        if (isBiometricAuthSuccess || isPinAuthSuccess) {
            bIniciarSesion.setEnabled(true);
        }
    }

    private boolean validatePin(String pin) {
        String savedPin = sharedPreferences.getString("user_pin", null);
        if (savedPin == null) {
            // Si no hay PIN guardado, guardamos el PIN ingresado
            sharedPreferences.edit().putString("user_pin", pin).apply();
            // Guardar un indicador para indicar que el PIN se ha introducido por primera vez
            sharedPreferences.edit().putBoolean("pin_entered", true).apply();
            return true;
        } else {
            // Si ya hay un PIN guardado, lo comparamos con el ingresado
            return savedPin.equals(pin);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isBlocked) {
            // Guardar el estado de bloqueo en SharedPreferences
            sharedPreferences.edit().putBoolean("isBlocked", true).apply();
            // Guardar la hora de bloqueo en SharedPreferences solo si se bloquea la aplicación
            long currentTimeMillis = System.currentTimeMillis();
            sharedPreferences.edit().putLong("blockTimeMillis", currentTimeMillis).apply();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        boolean isPreviouslyBlocked = sharedPreferences.getBoolean("isBlocked", false);
        long blockTimeMillis = sharedPreferences.getLong("blockTimeMillis", 0);
        long currentTimeMillis = System.currentTimeMillis();

        if (isPreviouslyBlocked && blockTimeMillis > 0) {
            long elapsedTimeMillis = currentTimeMillis - blockTimeMillis;
            if (elapsedTimeMillis < 30000) { // Si han pasado menos de 30 segundos y se bloqueó
                // Mostrar pantalla de bloqueo
                setContentView(R.layout.activity_blocked);
                return;
            } else {
                // Restablecer el estado de bloqueo después del tiempo de espera
                sharedPreferences.edit().remove("blockTimeMillis").apply(); // Eliminar el tiempo de bloqueo guardado
                sharedPreferences.edit().remove("isBlocked").apply(); // Eliminar el estado de bloqueo
            }
        }
        // Verificar si hay intentos fallidos al abrir la aplicación
        if (failedAttempts >= MAX_FAILED_ATTEMPTS) {
            // Si se superaron los intentos fallidos, bloquear la aplicación
            blockApp();
            return;
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        isExiting = true;
    }

    private void blockApp() {
        isBlocked = true;
        failedAttempts = 0;
        bIniciarSesion.setEnabled(false); // Deshabilitar el botón de inicio de sesión

        // Guardar el estado de bloqueo en SharedPreferences
        sharedPreferences.edit().putBoolean("isBlocked", true).apply();

        // Guardar la hora de bloqueo en SharedPreferences
        long currentTimeMillis = System.currentTimeMillis();
        sharedPreferences.edit().putLong("blockTimeMillis", currentTimeMillis).apply();

        // Mostrar pantalla de bloqueo
        setContentView(R.layout.activity_blocked);

        handler.postDelayed(() -> {
            if (!isExiting) { // Verificar si la aplicación no está saliendo
                isBlocked = false;
                setContentView(R.layout.activity_main); // Volver a la pantalla principal
                //checkBiometricSupport(); // Reiniciar la autenticación biométrica
                Toast.makeText(getApplicationContext(), "Puedes intentar nuevamente la autenticación biométrica.", Toast.LENGTH_LONG).show();

                // Restablecer el estado de bloqueo después del tiempo de espera
                sharedPreferences.edit().remove("blockTimeMillis").apply(); // Eliminar la hora de bloqueo guardada
            }
        }, 30000); // 30 segundos
    }



}