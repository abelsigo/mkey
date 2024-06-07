package com.abel.mkey;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;

public class Activity_Key extends AppCompatActivity {

    String filename = "";
    String filepath = "";
    String filecontent = "";
    Button guardar;

    EditText plataforma;
    EditText usuario;
    EditText contrasena;
    TextView NombreActividad;

    protected String GoogleId;
    private static final String FILE_PATH_DECRYPT = "storage/emulated/0/Android/data/com.abel.mkey/files/mkeyDir/wFGec1A6Hc.txt.hidden";
    private DriveServiceHelper driveServiceHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_key);
        guardar = findViewById(R.id.bGuardarKey);
        guardar.setText(getString(R.string.nActivityAddB));
        plataforma = findViewById(R.id.textPlataforma);
        usuario = findViewById(R.id.textUsuario);
        contrasena = findViewById(R.id.textContrasena);
        NombreActividad = findViewById(R.id.textView2);
        NombreActividad.setText(getString(R.string.nActivityAdd));
        System.out.println("\nActivityKey: "+Singleton.getInstance().getGoogleId());
        File decryptFile = new File("storage/emulated/0/Android/data/com.abel.mkey/files/mkeyDir/wFGec1A6Hc.txt.hidden");
        String decryptPassword = Singleton.getInstance().getGoogleId();
        FolderEncryptor.decryptFile(decryptFile, decryptPassword);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
            
        });

        filename = "wFGec1A6Hc.txt";
        filepath = "mkeyDir";
        if(!isExternalStorageAvailableForRW()){
            guardar.setEnabled(false);
        }
        guardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String emptyKey = ";;";
                String newKey = plataforma.getText().toString().trim() + ";" + usuario.getText().toString().trim() + ";" + contrasena.getText().toString().trim();
                System.out.println("\n"+newKey+"\n");
                if (!newKey.equals(emptyKey)) {
                    appendToFile(newKey);
                    plataforma.setText("");
                    usuario.setText("");
                    contrasena.setText("");
                    Toast.makeText(Activity_Key.this, getString(R.string.nActivityKeySS), Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(Activity_Key.this, ActivityKeyList.class);
                    startActivity(intent);
                    new DriveOperationTask().execute();
                }else if(newKey.equals(emptyKey)){
                    Toast.makeText(Activity_Key.this, getString(R.string.nActivityKeyND), Toast.LENGTH_SHORT).show();
                }
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

        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.blueActionBar));
    }

    private boolean isExternalStorageAvailableForRW() {
        String extStorageState = Environment.getExternalStorageState();
        if(extStorageState.equals(Environment.MEDIA_MOUNTED)){
            return true;
        }
        return false;
    }

    public void appendToFile(String newKey) {
        File myFile = new File(getExternalFilesDir(filepath), filename);
        try {
            // Leer el archivo y eliminar las líneas vacías
            BufferedReader reader = new BufferedReader(new FileReader(myFile));
            StringBuilder content = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    content.append(line).append(System.lineSeparator());
                }
            }
            reader.close();

            // Abrir el archivo en modo de adición
            FileOutputStream fos = new FileOutputStream(myFile, false); // Sobreescribir el archivo

            // Agregar la nueva clave
            newKey = newKey.trim();  // Trim para eliminar espacios en blanco al principio o al final
            if (content.length() > 0 && !newKey.isEmpty()) {
                content.append("");
            }
            content.append(newKey);

            // Escribir el nuevo contenido en el archivo
            fos.write(content.toString().getBytes());
            fos.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void deleteFileFromDrive(String fileName) {
        driveServiceHelper.getFileIdByName(fileName)
                .addOnSuccessListener(fileId -> {
                    if (fileId != null) {
                        driveServiceHelper.deleteFile(fileId)
                                .addOnSuccessListener(aVoid -> {

                                })
                                .addOnFailureListener(e -> {
                                    Log.e("MainActivity", "Error al eliminar el archivo.", e);
                                });
                    } else {
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("MainActivity", "Error al obtener el ID del archivo.", e);
                });
    }

    protected void updateFileFromDrive(){
        driveServiceHelper = new DriveServiceHelper(Activity_Key.this);

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


}