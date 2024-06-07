package com.abel.mkey;

import static org.apache.commons.codec.binary.Base64.decodeBase64;
import static org.apache.commons.codec.binary.Base64.encodeBase64;

import android.app.ActivityOptions;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.FileObserver;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import java.time.LocalDate;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.auth.oauth2.GoogleCredentials;

import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.CipherInputStream;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class ActivityKeyList extends AppCompatActivity {

    private static final String TAG = "ActivityKeyList";
    private RecyclerView recyclerView;
    private KeyAdapter adapter;
    private List<String> dataList;
    private
    GoogleSignInOptions gso;
    GoogleSignInAccount gsa;
    TextView NombreUsuario;
    Button GenerarArchivo;
    String filename = "wFGec1A6Hc.txt";
    String filepath = "mkeyDir";

    String newPlatform;
    String newUser;
    String newPassword;
    int size = 3;

    private Button Generador;
    private Button Cuenta;
    private DriveServiceHelper driveServiceHelper;

    private static final String FILE_PATH_DECRYPT = "storage/emulated/0/Android/data/com.abel.mkey/files/mkeyDir/wFGec1A6Hc.txt.hidden";
    private static final String FILE_PATH_ENCRYPT = "storage/emulated/0/Android/data/com.abel.mkey/files/mkeyDir/wFGec1A6Hc.txt";

    private static final int EDIT_KEY_REQUEST_CODE = 1001;

    private static final int NEW_KEY_REQUEST_CODE = 1002;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_key_list);
        File decryptFile = new File(FILE_PATH_DECRYPT);
        String decryptPassword = Singleton.getInstance().getGoogleId();;
        FolderEncryptor.decryptFile(decryptFile, decryptPassword);
        Generador = findViewById(R.id.bGenerador);
        Generador.setText(getString(R.string.nActivityGeneratorN));
        Cuenta = findViewById(R.id.bCuenta);
        Cuenta.setText(getString(R.string.nActivityAccountN));
        Generador.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ActivityKeyList.this, KeyGenerator.class);
                ActivityOptions options = ActivityOptions.makeCustomAnimation(ActivityKeyList.this, R.anim.slide_in, R.anim.slide_out);
                startActivity(intent, options.toBundle());
            }
        });
        Cuenta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ActivityKeyList.this, KeyAccount.class);
                ActivityOptions options = ActivityOptions.makeCustomAnimation(ActivityKeyList.this, R.anim.slide_in, R.anim.slide_out);
                startActivity(intent, options.toBundle());
            }
        });

        NombreUsuario = findViewById(R.id.textViewName);
        GenerarArchivo = findViewById(R.id.bGenerarArchivo);
        recyclerView = findViewById(R.id.recyclerView);


        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        GoogleSignInClient gsc = GoogleSignIn.getClient(this, gso);

        gsa = GoogleSignIn.getLastSignedInAccount(this);
        if (gsa != null) {
            String name = gsa.getDisplayName();
            String url = String.valueOf(gsa.getPhotoUrl());
            int nLength = name.length();
            if(nLength>=10){
                name = trimToNineCharacters(name);
            }
            NombreUsuario.setText(obtenerSaludoSegunHora()+ name+"!");
            size = size/3;
            NombreUsuario.setTextSize(size);
            //
        }

        GenerarArchivo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                generarKeys();
            }
        });

        FileReader fr = null;
        File myFile = new File(getExternalFilesDir(filepath), filename);
        dataList = new ArrayList<>();
        try{
            fr = new FileReader(myFile);
            BufferedReader br = new BufferedReader(fr);
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(";");
                if(parts.length>=3) {
                    String formattedLine = getString(R.string.nActivityKeyListPlatform)+": " + parts[0] + "\n"+getString(R.string.nActivityKeyListUser)+": " + parts[1];
                    dataList.add(formattedLine);
                }
            }
        }catch (IOException e){
            e.printStackTrace();
        }finally {

            // Inicializar adaptador
            adapter = new KeyAdapter(this, dataList);

            // Establecer LayoutManager
            recyclerView.setLayoutManager(new LinearLayoutManager(this));

            // Establecer adaptador
            recyclerView.setAdapter(adapter);
        }


// Crear una instancia de DriveServiceHelper
                /*b
                DriveServiceHelper driveServiceHelper = new DriveServiceHelper(ActivityKeyList.this);
// Llamar al método createDriveFolder en la instancia creada
                driveServiceHelper.getFolderIdByName("mkey", new DriveServiceHelper.FolderIdCallback() {
                    @Override
                    public void onFolderIdReceived(String folderId) {
                        if (folderId != null) {
                            String filePath = "storage/emulated/0/Android/data/com.abel.mkey/files/mkeyDir/wFGec1A6Hc.txt.hidden";
                            // Aquí puedes usar el folderId obtenido para subir un archivo a la carpeta
                            driveServiceHelper.uploadFileToFolder(filePath, folderId);
                        } else {
                            // Maneja el caso en el que no se encuentre la carpeta
                            Log.e(TAG, "No se encontró la carpeta con el nombre 'mkey'"+getString(R.string.welcome));
                        }
                    }
                });
                File decryptFile = new File(FILE_PATH_DECRYPT);
                String decryptPassword = Singleton.getInstance().getGoogleId();
                FolderEncryptor.decryptFile(decryptFile, decryptPassword);
 new DriveOperationTask().execute();
                 */


        File fileToEncrypt = new File(FILE_PATH_ENCRYPT);
        String encryptedPassword = Singleton.getInstance().getGoogleId();;
        FolderEncryptor.encryptFile(fileToEncrypt,encryptedPassword);
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.blueActionBar));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        File decryptFile = new File(FILE_PATH_DECRYPT);
        String decryptPassword = Singleton.getInstance().getGoogleId();;
        FolderEncryptor.decryptFile(decryptFile, decryptPassword);

        if (requestCode == EDIT_KEY_REQUEST_CODE && resultCode == RESULT_OK) {
            int position = data.getIntExtra("position", 0);
            newPlatform = data.getStringExtra("newPlatform");
            newUser = data.getStringExtra("newUser");
            newPassword = data.getStringExtra("newPassword");
            if (position != -1) {
                guardarEnArchivo(position,newPlatform,newUser,newPassword);
                dataList.set(position, newPlatform + ";" + newUser + ";" + newPassword);
                adapter.notifyItemChanged(position);
                mostrarNotificacion();
            }
        }else if (requestCode == EDIT_KEY_REQUEST_CODE && resultCode == RESULT_CANCELED) {
            File fileToEncrypt = new File("storage/emulated/0/Android/data/com.abel.mkey/files/mkeyDir/wFGec1A6Hc.txt");
            String encryptedPassword = Singleton.getInstance().getGoogleId();;
            FolderEncryptor.encryptFile(fileToEncrypt,encryptedPassword);
        }
         else if (requestCode == NEW_KEY_REQUEST_CODE) {
            File fileToEncrypt = new File("storage/emulated/0/Android/data/com.abel.mkey/files/mkeyDir/wFGec1A6Hc.txt");
            String encryptedPassword = Singleton.getInstance().getGoogleId();;
            FolderEncryptor.encryptFile(fileToEncrypt,encryptedPassword);
         }else if (requestCode == NEW_KEY_REQUEST_CODE && resultCode == RESULT_CANCELED) {
            File fileToEncrypt = new File("storage/emulated/0/Android/data/com.abel.mkey/files/mkeyDir/wFGec1A6Hc.txt");
            String encryptedPassword = Singleton.getInstance().getGoogleId();;
            FolderEncryptor.encryptFile(fileToEncrypt,encryptedPassword);
        }

    }

    private void guardarEnArchivo(int positionEdit, String nPlatform, String nUser, String nPassword) {

        File myFile = new File(getExternalFilesDir(filepath), filename);

        try {
            BufferedReader reader = new BufferedReader(new FileReader(myFile));
            StringBuilder newContent = new StringBuilder();
            String line;

            // Construir el nuevo contenido reemplazando la línea en la posición especificada
            int lineNumber = 0;
            while ((line = reader.readLine()) != null) {
                if (lineNumber == positionEdit) {
                    newContent.append(nPlatform).append(";").append(nUser).append(";").append(nPassword).append(System.lineSeparator());
                } else if (!line.trim().isEmpty()) {  // Omitir líneas vacías
                    newContent.append(line).append(System.lineSeparator());
                }
                lineNumber++;
            }
            reader.close();

            // Escribir el nuevo contenido en el archivo
            try (FileWriter writer = new FileWriter(myFile)) {
                writer.write(newContent.toString());
            }

            dataList.set(positionEdit, nPlatform + ";" + nUser + ";" + nPassword);
            adapter.notifyItemChanged(positionEdit);

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, getString(R.string.nActivityKeyListAE), Toast.LENGTH_SHORT).show();
        }
        Intent intent = new Intent(ActivityKeyList.this,ActivityKeyList.class);
        startActivity(intent);
        new DriveOperationTask().execute();
    }

    protected void generarKeys() {
        Intent intent = new Intent(ActivityKeyList.this, Activity_Key.class);
        ActivityOptions options = ActivityOptions.makeCustomAnimation(ActivityKeyList.this, R.anim.slide_in, R.anim.slide_out);
        startActivityForResult(intent, NEW_KEY_REQUEST_CODE);
        mostrarNotificacion();
    }

    private void deleteFileFromDrive(String fileName) {
        driveServiceHelper.getFileIdByName(fileName)
                .addOnSuccessListener(fileId -> {
                    if (fileId != null) {
                        driveServiceHelper.deleteFile(fileId)
                                .addOnSuccessListener(aVoid -> {
                                    Log.e("MainActivity", "Archivo eliminado correctamente.");
                                    mostrarNotificacion();
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("MainActivity", "Error al eliminar el archivo.", e);
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
        driveServiceHelper = new DriveServiceHelper(ActivityKeyList.this);

        // Llamar al método uploadFileToFolder para subir el archivo a Google Drive
        driveServiceHelper.getFolderIdByName("mkey", new DriveServiceHelper.FolderIdCallback() {
            @Override
            public void onFolderIdReceived(String folderId) {
                if (folderId != null) {
                    String filePath = FILE_PATH_DECRYPT;
                    // Aquí puedes usar el folderId obtenido para subir un archivo a la carpeta
                    driveServiceHelper.uploadFileToFolder(filePath, folderId);
                } else {
                    // Maneja el caso en el que no se encuentre la carpeta
                    Log.e(TAG, "No se encontró la carpeta con el nombre 'mkey'");
                }
            }
        });
    }

    private class DriveOperationTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            // Aquí puedes llamar a tus funciones de actualización y eliminación en segundo plano
            updateFileFromDrive();
            deleteFileFromDrive("wFGec1A6Hc.txt.hidden");
            return null;

        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            // Aquí puedes realizar cualquier acción que necesites después de que se completen las operaciones en segundo plano

        }
    }

    @Override
    public void onBackPressed(){
        super.onBackPressed();
        File fileToEncrypt = new File("storage/emulated/0/Android/data/com.abel.mkey/files/mkeyDir/wFGec1A6Hc.txt");
        String encryptedPassword = Singleton.getInstance().getGoogleId();;
        FolderEncryptor.encryptFile(fileToEncrypt,encryptedPassword);
    }

    private String obtenerSaludoSegunHora() {
        int hora = 0;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            hora = java.time.LocalTime.now().getHour();
        }
        if (hora >= 6 && hora < 13) {
            return getString(R.string.welcomeDay);
        } else if (hora >= 13 && hora < 21) {
            return getString(R.string.welcomeAfternoon);
        } else {
            return getString(R.string.welcomeNight);
        }
    }

    protected void mostrarNotificacion() {
        // Crear un NotificationManager
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Crear un NotificationChannel (solo para Android Oreo y versiones posteriores)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("mi_canal_id", "Nombre del canal", NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        // Configurar la notificación usando NotificationCompat.Builder
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "mi_canal_id")
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(getString(R.string.nActivityKeyListNotificationTitle))
                .setContentText(getString(R.string.nActivityKeyListNotificationText))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        // Mostrar la notificación llamando al método notify()
        notificationManager.notify(1, builder.build());
    }
    private String trimToNineCharacters(String name) {
        // Definir las vocales
        String vowels = "aeiouAEIOU";
        int maxLength = 9;

        // Iterar sobre los primeros 9 caracteres del nombre
        for (int i = maxLength - 1; i >= 0; i--) {
            // Si el carácter actual no es una vocal, recortar el nombre hasta este punto
            if (vowels.indexOf(name.charAt(i)) == -1) {
                return name.substring(0, i + 1)+"."; // Devolver hasta la consonante encontrada
            }
        }
        return name.substring(0, maxLength); // Si no se encuentra una consonante, recortar hasta el límite de 9 caracteres
    }

}
