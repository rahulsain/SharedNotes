package com.rahuls.sharednotes.drive;

import androidx.annotation.Nullable;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.api.client.http.FileContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * A utility for performing read/write operations on Drive files via the REST API and opening a
 * file picker UI via Storage Access Framework.
 */
public class DriveServiceHelper {

    private final Executor mExecutor = Executors.newSingleThreadExecutor();
    private final Drive mDriveService;
    private final String TAG = "DRIVE_TAG";
    public static String TYPE_AUDIO = "application/vnd.google-apps.audio";
    public static String TYPE_GOOGLE_DOCS = "application/vnd.google-apps.document";
    public static String TYPE_GOOGLE_DRAWING = "application/vnd.google-apps.drawing";
    public static String TYPE_GOOGLE_DRIVE_FILE = "application/vnd.google-apps.file";
    public static String TYPE_GOOGLE_FORMS = "application/vnd.google-apps.form";
    public static String TYPE_GOOGLE_FUSION_TABLES = "application/vnd.google-apps.fusiontable";
    public static String TYPE_GOOGLE_MY_MAPS = "application/vnd.google-apps.map";
    public static String TYPE_PHOTO = "application/vnd.google-apps.photo";
    public static String TYPE_GOOGLE_SLIDES = "application/vnd.google-apps.presentation";
    public static String TYPE_GOOGLE_APPS_SCRIPTS = "application/vnd.google-apps.script";
    public static String TYPE_GOOGLE_SITES = "application/vnd.google-apps.site";
    public static String TYPE_GOOGLE_SHEETS = "application/vnd.google-apps.spreadsheet";
    public static String TYPE_UNKNOWN = "application/vnd.google-apps.unknown";
    public static String TYPE_VIDEO = "application/vnd.google-apps.video";
    public static String TYPE_3_RD_PARTY_SHORTCUT = "application/vnd.google-apps.drive-sdk";


    public DriveServiceHelper(Drive driveService) {

        mDriveService = driveService;
    }

    public Task<GoogleDriveFileHolder> searchFile(String fileName, String mimeType) {
        return Tasks.call(mExecutor, () -> {

            FileList result = mDriveService.files().list()
                    .setQ("name = '" + fileName + "' and mimeType ='" + mimeType + "'")
                    .setSpaces("drive")
                    .setFields("files(id, name,size,createdTime,modifiedTime,starred)")
                    .execute();
            GoogleDriveFileHolder googleDriveFileHolder = new GoogleDriveFileHolder();
            if (result.getFiles().size() > 0) {

                googleDriveFileHolder.setId(result.getFiles().get(0).getId());
                googleDriveFileHolder.setName(result.getFiles().get(0).getName());
                googleDriveFileHolder.setModifiedTime(result.getFiles().get(0).getModifiedTime());
                googleDriveFileHolder.setSize(result.getFiles().get(0).getSize());
            }


            return googleDriveFileHolder;
        });
    }

    public Task<GoogleDriveFileHolder> searchFolder(String folderName) {
        return Tasks.call(mExecutor, () -> {

            // Retrieve the metadata as a File object.
            FileList result = mDriveService.files().list()
                    .setQ("mimeType = '" + "application/vnd.google-apps.folder" + "' and name = '" + folderName + "' ")
                    .setSpaces("drive")
                    .execute();
            GoogleDriveFileHolder googleDriveFileHolder = new GoogleDriveFileHolder();
            if (result.getFiles().size() > 0) {
                googleDriveFileHolder.setId(result.getFiles().get(0).getId());
                googleDriveFileHolder.setName(result.getFiles().get(0).getName());

            }
            return googleDriveFileHolder;
        });
    }
// view file in folder
    public Task<List<GoogleDriveFileHolder>> queryFiles(@Nullable final String folderId) {
        return Tasks.call(mExecutor, () -> {
            List<GoogleDriveFileHolder> googleDriveFileHolderList = new ArrayList<>();
            String parent = "root";
            if (folderId != null) {
                parent = folderId;
            }

            FileList result = mDriveService.files().list().setQ("'" + parent + "' in parents").setFields("files(id, name,size,createdTime,modifiedTime,starred)").setSpaces("drive").execute();

            for (int i = 0; i < result.getFiles().size(); i++) {

                GoogleDriveFileHolder googleDriveFileHolder = new GoogleDriveFileHolder();
                googleDriveFileHolder.setId(result.getFiles().get(i).getId());
                googleDriveFileHolder.setName(result.getFiles().get(i).getName());
                if (result.getFiles().get(i).getSize() != null) {
                    googleDriveFileHolder.setSize(result.getFiles().get(i).getSize());
                }

                if (result.getFiles().get(i).getModifiedTime() != null) {
                    googleDriveFileHolder.setModifiedTime(result.getFiles().get(i).getModifiedTime());
                }

                if (result.getFiles().get(i).getCreatedTime() != null) {
                    googleDriveFileHolder.setCreatedTime(result.getFiles().get(i).getCreatedTime());
                }

                if (result.getFiles().get(i).getStarred() != null) {
                    googleDriveFileHolder.setStarred(result.getFiles().get(i).getStarred());
                }

                googleDriveFileHolderList.add(googleDriveFileHolder);

            }


            return googleDriveFileHolderList;


        }
        );
    }

    /**
     * Creates a text file in the user's My Drive folder and returns its file ID.
     */
    public Task<GoogleDriveFileHolder> createFile(String folderId, String filename) {
        return Tasks.call(mExecutor, () -> {
            GoogleDriveFileHolder googleDriveFileHolder = new GoogleDriveFileHolder();

            List<String> root;
            if (folderId == null) {

                root = Collections.singletonList("root");

            } else {

                root = Collections.singletonList(folderId);
            }
            File metadata = new File()
                    .setParents(root)
                    .setMimeType("text/plain")
                    .setName(filename);

            File googleFile = mDriveService.files().create(metadata).execute();
            if (googleFile == null) {

                throw new IOException("Null result when requesting file creation.");
            }
            googleDriveFileHolder.setId(googleFile.getId());
            return googleDriveFileHolder;
        });
    }


// TO CREATE A FOLDER

    public Task<GoogleDriveFileHolder> createFolder(String folderName, @Nullable String folderId) {
        return Tasks.call(mExecutor, () -> {

            GoogleDriveFileHolder googleDriveFileHolder = new GoogleDriveFileHolder();

            List<String> root;
            if (folderId == null) {

                root = Collections.singletonList("root");

            } else {
                root = Collections.singletonList(folderId);
            }
            File metadata = new File()
                    .setParents(root)
                    .setMimeType("application/vnd.google-apps.folder")
                    .setName(folderName);

            File googleFile = mDriveService.files().create(metadata).execute();
            if (googleFile == null) {
                throw new IOException("Null result when requesting file creation.");
            }
            googleDriveFileHolder.setId(googleFile.getId());
            return googleDriveFileHolder;
        });
    }


    public Task<Void> downloadFile(java.io.File targetFile, String fileId) {
        return Tasks.call(mExecutor, () -> {

            // Retrieve the metadata as a File object.
            OutputStream outputStream = new FileOutputStream(targetFile);
            mDriveService.files().get(fileId).executeMediaAndDownloadTo(outputStream);
            return null;
        });
    }

    public Task<Void> deleteFolderFile(String fileId) {

        return Tasks.call(mExecutor, () -> {

            // Retrieve the metadata as a File object.
            if (fileId != null) {
                mDriveService.files().delete(fileId).execute();
            }

            return null;

        });
    }

// TO LIST FILES

    public List<File> listDriveImageFiles() throws IOException{

        FileList result;
        String pageToken = null;
        do {
            result = mDriveService.files().list()
/*.setQ("mimeType='image/png' or mimeType='text/plain'")This si to list both image and text files. Mind the type of image(png or jpeg).setQ("mimeType='image/png' or mimeType='text/plain'") */
                    .setSpaces("drive")
                    .setFields("nextPageToken, files(id, name)")
                    .setPageToken(pageToken)
                    .execute();

            pageToken = result.getNextPageToken();
        } while (pageToken != null);

        return result.getFiles();
    }

// TO UPLOAD A FILE ONTO DRIVE

    public Task<GoogleDriveFileHolder> uploadFile(final java.io.File localFile,
                                                  final String mimeType, @Nullable final String folderId) {
        return Tasks.call(mExecutor, () -> {
            // Retrieve the metadata as a File object.

            List<String> root;
            if (folderId == null) {
                root = Collections.singletonList("root");
            } else {

                root = Collections.singletonList(folderId);
            }

            File metadata = new File()
                    .setParents(root)
                    .setMimeType(mimeType)
                    .setName(localFile.getName());

            FileContent fileContent = new FileContent(mimeType, localFile);

            File fileMeta = mDriveService.files().create(metadata,
                    fileContent).execute();
            GoogleDriveFileHolder googleDriveFileHolder = new
                    GoogleDriveFileHolder();
            googleDriveFileHolder.setId(fileMeta.getId());
            googleDriveFileHolder.setName(fileMeta.getName());
            return googleDriveFileHolder;
        });
    }
}