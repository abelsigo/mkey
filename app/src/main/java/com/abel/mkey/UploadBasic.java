package com.abel.mkey;

import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.FileContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;

import java.io.IOException;
import java.util.Arrays;

public class UploadBasic {

    /**
     * Upload new file.
     *
     * @param service   Authorized Drive API service instance.
     * @param fileName  Name of the file to upload.
     * @param mimeType  MIME type of the file to upload.
     * @param filePath  Path to the file to upload.
     * @return Inserted file metadata if successful, {@code null} otherwise.
     * @throws IOException if service account credentials file not found.
     */
    public static String uploadBasic(Drive service, String fileName, String mimeType, String filePath) throws IOException {
        // Create file metadata
        File fileMetadata = new File();
        fileMetadata.setName(fileName);

        // File's content
        java.io.File file = new java.io.File(filePath);
        FileContent mediaContent = new FileContent(mimeType, file);

        try {
            // Upload file to Google Drive
            File uploadedFile = service.files().create(fileMetadata, mediaContent)
                    .setFields("id")
                    .execute();

            System.out.println("File ID: " + uploadedFile.getId());
            return uploadedFile.getId();
        } catch (GoogleJsonResponseException e) {
            // Handle error appropriately
            System.err.println("Unable to upload file: " + e.getDetails());
            throw e;
        }
    }
}
