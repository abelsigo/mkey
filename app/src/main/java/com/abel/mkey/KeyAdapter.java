package com.abel.mkey;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class KeyAdapter extends RecyclerView.Adapter<ViewHolder> {

    private List<String> dataList;
    private Context context;

    String filename = "wFGec1A6Hc.txt";
    String filepath = "mkeyDir";
    private DriveServiceHelper driveServiceHelper;

    private static final int EDIT_KEY_REQUEST_CODE = 1001;

    public KeyAdapter(Context context, List<String> dataList) {
        this.context = context;
        this.dataList = dataList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_layout, parent, false);
        return new ViewHolder(view);
    }
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        String item = dataList.get(position);
        String[] parts = item.split(";");
        holder.textViewItem.setText(item);

        holder.bEditarKey.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popupMenu = new PopupMenu(context, v);
                popupMenu.inflate(R.menu.item_options_menu); // Aquí puedes definir el menú de opciones en XML
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        int itemId = item.getItemId();
                        if (itemId == R.id.menu_option_edit) {
                            handleEditKey(holder.getAdapterPosition());
                            return true;
                        } else if (itemId == R.id.menu_option_delete) {
                            handleDeleteKey(holder.getAdapterPosition());
                            return true;
                        } else if (itemId == R.id.menu_option_view) {
                            handleViewKey(holder.getAdapterPosition());
                            return true;
                        }
                        return false;
                    }
                });
                popupMenu.show();
            }
        });
    }

    private void handleEditKey(int position) {
        File decryptFile = new File("storage/emulated/0/Android/data/com.abel.mkey/files/mkeyDir/wFGec1A6Hc.txt.hidden");
        String decryptPassword = Singleton.getInstance().getGoogleId();
        FolderEncryptor.decryptFile(decryptFile, decryptPassword);

        File myFile = new File(context.getExternalFilesDir(filepath), filename);
        try (BufferedReader br = new BufferedReader(new FileReader(myFile))) {
            String line;
            int counter = 0;
            while ((line = br.readLine()) != null) {
                if (counter == position) {
                    String[] fileParts = line.split(";");
                    if (fileParts.length >= 3) {
                        Intent intent = new Intent(context, Activity_EditKey.class);
                        intent.putExtra("position", position);
                        intent.putExtra("platform", fileParts[0]);
                        intent.putExtra("user", fileParts[1]);
                        intent.putExtra("password", fileParts[2]);
                        ((Activity) context).startActivityForResult(intent, EDIT_KEY_REQUEST_CODE);
                        break;
                    } else {
                        String msg = context.getString(R.string.nKeyAdapterNoValidData);
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
                    }
                }
                counter++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleDeleteKey(int position) {
        File decryptFile = new File("storage/emulated/0/Android/data/com.abel.mkey/files/mkeyDir/wFGec1A6Hc.txt.hidden");
        String decryptPassword = Singleton.getInstance().getGoogleId();
        FolderEncryptor.decryptFile(decryptFile, decryptPassword);

        if (position != RecyclerView.NO_POSITION) {
            File myFile = new File(context.getExternalFilesDir(filepath), filename);
            List<String> lines = new ArrayList<>();
            try (BufferedReader br = new BufferedReader(new FileReader(myFile))) {
                String line;
                while ((line = br.readLine()) != null) {
                    lines.add(line);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            lines.remove(position);

            try (BufferedWriter bw = new BufferedWriter(new FileWriter(myFile))) {
                for (String line : lines) {
                    bw.write(line);
                    bw.newLine();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            dataList.remove(position);
            notifyItemRemoved(position);
            File fileToEncrypt = new File("storage/emulated/0/Android/data/com.abel.mkey/files/mkeyDir/wFGec1A6Hc.txt");
            String encryptedPassword = Singleton.getInstance().getGoogleId();
            FolderEncryptor.encryptFile(fileToEncrypt, encryptedPassword);
        }
    }

    private void handleViewKey(int position) {
        File decryptFile = new File("storage/emulated/0/Android/data/com.abel.mkey/files/mkeyDir/wFGec1A6Hc.txt.hidden");
        String decryptPassword = Singleton.getInstance().getGoogleId();
        FolderEncryptor.decryptFile(decryptFile, decryptPassword);

        File myFile = new File(context.getExternalFilesDir(filepath), filename);
        try (BufferedReader br = new BufferedReader(new FileReader(myFile))) {
            String line;
            int counter = 0;
            while ((line = br.readLine()) != null) {
                if (counter == position) {
                    String[] fileParts = line.split(";");
                    if (fileParts.length >= 3) {
                        Intent intent = new Intent(context, Activity_ViewKey.class);
                        intent.putExtra("position", position);
                        intent.putExtra("platform", fileParts[0]);
                        intent.putExtra("user", fileParts[1]);
                        intent.putExtra("password", fileParts[2]);
                        ((Activity) context).startActivity(intent);
                        break;
                    } else {
                        String msg = context.getString(R.string.nKeyAdapterNoValidData);
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
                    }
                }
                counter++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void updateFile() {
        File myFile = new File(context.getExternalFilesDir(filepath), filename);
        try {
            FileWriter writer = new FileWriter(myFile);
            for (String item : dataList) {
                writer.write(item + "\n");
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

/*
    private void shareKey(String password) {
        String shareFilename = "passwordToShare.txt";
        String shareFilepath = "mkeyDir";
        File myFile = new File(context.getExternalFilesDir(shareFilepath), shareFilename);

        try (FileWriter writer = new FileWriter(myFile)) {
            writer.write(password);
        } catch (IOException e) {
            e.printStackTrace();
        }

        String encryptedPassword = Singleton.getInstance().getGoogleId();
        FolderEncryptor.encryptFile(myFile, encryptedPassword);

        subirYCompartirArchivo(myFile);
    }

 */
/*
    private void subirYCompartirArchivo(File fileToEncrypt) {
        driveServiceHelper = new DriveServiceHelper((Activity) context);

        // Llamar al método uploadFileToFolder para subir el archivo a Google Drive
        driveServiceHelper.getFolderIdByName("mkey", new DriveServiceHelper.FolderIdCallback() {
            @Override
            public void onFolderIdReceived(String folderId) {
                if (folderId != null) {
                    String filePath = "storage/emulated/0/Android/data/com.abel.mkey/files/mkeyDir/passwordToShare.txt.hidden";
                    // Aquí puedes usar el folderId obtenido para subir un archivo a la carpeta
                    driveServiceHelper.uploadFileToFolder(filePath, folderId);

                    // Esperar 5 segundos antes de verificar si el archivo existe
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            // Verificar si el archivo existe
                            driveServiceHelper.isFileExist("passwordToShare.txt.hidden").addOnSuccessListener(exists -> {
                                if (exists) {
                                    // El archivo existe, compartirlo
                                    compartirArchivoGoogleDrive("passwordToShare.txt.hidden", "xusialdeano@gmail.com");
                                } else {
                                    // El archivo no existe, mostrar un mensaje de error
                                    Toast.makeText(context, "El archivo aún no está disponible en Google Drive", Toast.LENGTH_SHORT).show();
                                }
                            }).addOnFailureListener(e -> {
                                // Manejar el caso en el que ocurra un error al verificar la existencia del archivo
                                Toast.makeText(context, "Error al verificar la existencia del archivo en Google Drive", Toast.LENGTH_SHORT).show();
                            });
                        }
                    }, 5000); // Espera 5 segundos antes de verificar
                } else {
                    // Maneja el caso en el que no se encuentre la carpeta
                    Log.e("KeyAdapter", "No se encontró la carpeta con el nombre 'mkey'");
                    Toast.makeText(context, "No se encontró la carpeta con el nombre 'mkey'", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

 */

/*
    private void subirYCompartirArchivo(File fileToEncrypt) {
        driveServiceHelper = new DriveServiceHelper((Activity) context);

        // Llamar al método uploadFileToFolder para subir el archivo a Google Drive
        driveServiceHelper.getFolderIdByName("mkey", new DriveServiceHelper.FolderIdCallback() {
            @Override
            public void onFolderIdReceived(String folderId) {
                if (folderId != null) {
                    String filePath = "storage/emulated/0/Android/data/com.abel.mkey/files/mkeyDir/passwordToShare.txt.hidden";
                    // Aquí puedes usar el folderId obtenido para subir un archivo a la carpeta
                    driveServiceHelper.uploadFileToFolder(filePath, folderId);

                    // Compartir el archivo en un hilo secundario
                    new AsyncTask<Void, Void, Void>() {
                        @Override
                        protected Void doInBackground(Void... voids) {
                            try {
                                // Espera un tiempo para que el archivo se cargue correctamente
                                Thread.sleep(3000); // 3 segundos de espera (puedes ajustar este valor según sea necesario)

                                // Compartir el archivo
                                compartirArchivoGoogleDriveInBackground("passwordToShare.txt.hidden", "xusialdeano@gmail.com");
                            } catch (Exception e) {
                                Log.e("KeyAdapter", "Error al compartir el archivo: " + e.getMessage());
                            }
                            return null;
                        }
                    }.execute();
                } else {
                    // Maneja el caso en el que no se encuentre la carpeta
                    Log.e("KeyAdapter", "No se encontró la carpeta con el nombre 'mkey'");
                    Toast.makeText(context, "No se encontró la carpeta con el nombre 'mkey'", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

 */



/*
    private void subirYCompartirArchivo(File fileToEncrypt) {
        driveServiceHelper = new DriveServiceHelper((Activity) context);

        // Llamar al método uploadFileToFolder para subir el archivo a Google Drive
        driveServiceHelper.getFolderIdByName("mkey", new DriveServiceHelper.FolderIdCallback() {
            @Override
            public void onFolderIdReceived(String folderId) {
                if (folderId != null) {
                    String filePath = "storage/emulated/0/Android/data/com.abel.mkey/files/mkeyDir/passwordToShare.txt.hidden";
                    // Aquí puedes usar el folderId obtenido para subir un archivo a la carpeta
                    driveServiceHelper.uploadFileToFolder(filePath, folderId);
                    driveServiceHelper.isFileExist("passwordToShare.txt.hidden");
                    compartirArchivoGoogleDrive("passwordToShare.txt.hidden","xusialdeano@gmail.com");
                } else {
                    // Maneja el caso en el que no se encuentre la carpeta
                    Log.e("KeyAdapter", "No se encontró la carpeta con el nombre 'mkey'");
                    Toast.makeText(context, "No se encontró la carpeta con el nombre 'mkey'", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

 */


    private void compartirArchivoGoogleDriveInBackground(String fileName, String userEmail) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                DriveServiceHelper driveServiceHelper = new DriveServiceHelper((Activity) context);

                driveServiceHelper.getFileIdByName(fileName)
                        .addOnSuccessListener(fileId -> {
                            if (fileId != null) {
                                driveServiceHelper.shareFileWithUser(fileId, userEmail)
                                        .addOnSuccessListener(aVoid -> {
                                            Toast.makeText(context, "Archivo compartido con: " + userEmail, Toast.LENGTH_LONG).show();
                                        })
                                        .addOnFailureListener(e -> {
                                            Log.e("KeyAdapter", "Error al compartir el archivo.", e);
                                            Toast.makeText(context, "Error al compartir el archivo.", Toast.LENGTH_SHORT).show();
                                        });
                            } else {
                                Toast.makeText(context, "El archivo no se encontró en Google Drive.", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnFailureListener(e -> {
                            Log.e("KeyAdapter", "Error al obtener el ID del archivo.", e);
                            Toast.makeText(context, "Error al obtener el ID del archivo.", Toast.LENGTH_SHORT).show();
                        });
                return null;
            }
        }.execute();
    }




/*
    @Override
    //Modificar esta parte para que siga mostrando plat y user, pero pudiendo rescatar cada dato por separado para poder editarlo.
    public void onBindViewHolder(ViewHolder holder, int position) {
        String item = dataList.get(position);
        String[] parts = item.split(";"); // Dividir la cadena en partes

        holder.textViewItem.setText(item);

            //Aqui quiero leer el archivo wFGec1A6Hc para poder recuperar los datos y ponerlos en el intent.putExtra.

        holder.bEditarKey.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FileReader fr = null;
                File myFile = new File(context.getExternalFilesDir(filepath), filename);
                try {
                    fr = new FileReader(myFile);
                    BufferedReader br = new BufferedReader(fr);
                    String line;
                    int counter = 0;
                    while ((line = br.readLine()) != null) {
                        if (counter == position) {
                            String[] fileParts = line.split(";");
                            if (fileParts.length >= 3) {
                                Intent intent = new Intent(context, Activity_EditKey.class);
                                intent.putExtra("position", position);
                                intent.putExtra("platform", fileParts[0]);
                                intent.putExtra("user", fileParts[1]);
                                intent.putExtra("password", fileParts[2]);
                                ((Activity) context).startActivityForResult(intent, 1);
                                break;
                            }
                        }
                        counter++;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

 */


    @Override
    public int getItemCount() {
        return dataList.size();
    }
}
