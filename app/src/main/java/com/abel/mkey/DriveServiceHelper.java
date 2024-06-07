package com.abel.mkey;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.GooglePlayServicesAvailabilityException;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.api.services.drive.model.Permission;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;


public class DriveServiceHelper {

    private static final String TAG = "DriveServiceHelper";

    private final Drive mDriveService;

    public DriveServiceHelper(Context context) {
        GoogleSignInAccount signInAccount = GoogleSignIn.getLastSignedInAccount(context);
        if (signInAccount != null) {
            GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(
                    context, Collections.singleton(DriveScopes.DRIVE_FILE));
            credential.setSelectedAccountName(signInAccount.getAccount().name);
            mDriveService = new Drive.Builder(
                    AndroidHttp.newCompatibleTransport(),
                    JacksonFactory.getDefaultInstance(),
                    credential)
                    .setApplicationName("Mkey")
                    .build();
        } else {
            // Handle no signed in account error
            mDriveService = null;
        }
    }

    public void createDriveFolder(String folderName, String parentFolderId) {
        if (mDriveService == null) {
            // Handle no signed in account error
            return;
        }

        com.google.api.services.drive.model.File gFolder = new com.google.api.services.drive.model.File();
        gFolder.setName(folderName);
        gFolder.setMimeType("application/vnd.google-apps.folder");

        if (parentFolderId != null) {
            List<String> parents = new ArrayList<>();
            parents.add(parentFolderId);
            gFolder.setParents(parents);
        }

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                try {
                    mDriveService.files().create(gFolder).setFields("id").execute();
                } catch (IOException e) {
                    e.printStackTrace();
                    // Handle create folder error
                }
                return null;
            }
        }.execute();
    }

    public void uploadFileToFolder(String filePath, String folderId) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                try {
                    File fileMetadata = new File();
                    fileMetadata.setName(new java.io.File(filePath).getName()); // Obtiene el nombre del archivo
                    fileMetadata.setParents(Collections.singletonList(folderId));

                    java.io.File file = new java.io.File(filePath);
                    FileContent mediaContent = new FileContent("text/plain", file);

                    File uploadedFile = mDriveService.files().create(fileMetadata, mediaContent)
                            .setFields("id")
                            .execute();

                    Log.d(TAG, "File ID: " + uploadedFile.getId());
                } catch (IOException e) {
                    Log.e(TAG, "Error uploading file: " + e.getMessage());
                }
                return null;
            }
        }.execute();
    }


    public void getFolderIdByName(String folderName, FolderIdCallback callback) {
        if (mDriveService == null) {
            // Handle no signed in account error
            return;
        }

        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... voids) {
                try {
                    // Definir los parámetros de búsqueda para la carpeta
                    String query = "mimeType='application/vnd.google-apps.folder' and name='" + folderName + "'";

                    // Hacer la solicitud a la API de Google Drive para buscar la carpeta por su nombre
                    FileList result = mDriveService.files().list()
                            .setQ(query)
                            .setSpaces("drive")
                            .setFields("files(id)")
                            .execute();

                    // Obtener el ID de la carpeta si se encontró
                    if (result.getFiles() != null && !result.getFiles().isEmpty()) {
                        return result.getFiles().get(0).getId();
                    } else {
                        // Si no se encuentra la carpeta, devolver null
                        return null;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }
            }

            @Override
            protected void onPostExecute(String folderId) {
                super.onPostExecute(folderId);
                if (callback != null) {
                    callback.onFolderIdReceived(folderId);
                }
            }
        }.execute();
    }

    public interface FolderIdCallback {
        void onFolderIdReceived(String folderId);
    }
    public Task<Boolean> isFileExist(String fileName) {
        return Tasks.call(Executors.newSingleThreadExecutor(), () -> {
            FileList result = mDriveService.files().list()
                    .setQ("name = '" + fileName + "' and trashed = false")
                    .setSpaces("drive")
                    .setFields("files(id, name)")
                    .execute();
            List<File> files = result.getFiles();
            return files != null && !files.isEmpty();
        });
    }
    public Task<Long> getFileLastModifiedDate(String fileName) {
        return Tasks.call(Executors.newSingleThreadExecutor(), () -> {
            FileList result = mDriveService.files().list()
                    .setQ("name = '" + fileName + "' and trashed = false")
                    .setSpaces("drive")
                    .setFields("files(id, name, modifiedTime)")
                    .execute();
            List<File> files = result.getFiles();
            if (files != null && !files.isEmpty()) {
                return files.get(0).getModifiedTime().getValue();
            } else {
                throw new IOException("File not found.");
            }
        });
    }

    public Task<String> getFileIdByName(String fileName) {
        return Tasks.call(Executors.newSingleThreadExecutor(), () -> {
            FileList result = mDriveService.files().list()
                    .setQ("name = '" + fileName + "' and trashed = false")
                    .setSpaces("drive")
                    .setFields("files(id, name)")
                    .execute();
            List<File> files = result.getFiles();
            if (files != null && !files.isEmpty()) {
                return files.get(0).getId();
            } else {
                return null;
            }
        });
    }

    public Task<Void> deleteFile(String fileId) {
        return Tasks.call(Executors.newSingleThreadExecutor(), () -> {
            try {
                mDriveService.files().delete(fileId).execute();
                return null;
            } catch (IOException e) {
                throw new RuntimeException("Failed to delete file: " + e.getMessage(), e);
            }
        });
    }

    public Task<Void> downloadFile(String fileId, java.io.File destinationFile) {
        return Tasks.call(Executors.newSingleThreadExecutor(), () -> {
            try (OutputStream outputStream = new FileOutputStream(destinationFile)) {
                mDriveService.files().get(fileId)
                        .executeMediaAndDownloadTo(outputStream);
                return null;
            } catch (IOException e) {
                throw new RuntimeException("Failed to download file: " + e.getMessage(), e);
            }
        });
    }

    public Task<Void> shareFileWithUser(String fileId, String userEmail) {
        return Tasks.call(() -> {
            Permission userPermission = new Permission()
                    .setType("user")
                    .setRole("reader")
                    .setEmailAddress(userEmail);

            mDriveService.permissions().create(fileId, userPermission).execute();
            return null;
        });
    }



}

