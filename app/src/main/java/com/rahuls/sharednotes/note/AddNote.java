package com.rahuls.sharednotes.note;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.Scope;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;
import com.rahuls.sharednotes.R;
import com.rahuls.sharednotes.drive.DriveServiceHelper;
import com.rahuls.sharednotes.drive.GoogleDriveFileHolder;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class AddNote extends AppCompatActivity {

    private static final String TAG = "AddNote";
    private static final int RESULT_LOAD_IMAGE = 1003;
    private static final int REQUEST_PICK_IMAGE = 1002;
    FirebaseFirestore fStore;
    EditText noteTitle,noteContent;
    Button driveActionButton;
    ProgressBar progressBarSave;
    FirebaseUser user;
    DriveServiceHelper mDriveServiceHelper;
    GoogleDriveFileHolder fileId;
    String noteId;
    private static final int RC_AUTHORIZE_DRIVE = 1004;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_note);
        Toolbar toolbar = findViewById(R.id.toolbar3);
        setSupportActionBar(toolbar);

        noteId = UUID.randomUUID().toString();


        fStore = FirebaseFirestore.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();
        noteTitle = findViewById(R.id.addNoteTitle);
        noteContent = findViewById(R.id.addNoteContent);

        progressBarSave = findViewById(R.id.progressBar1);
        driveActionButton = findViewById(R.id.create_drive_folder);

        FloatingActionButton fab = findViewById(R.id.fab1);
        fab.setOnClickListener(view -> {

            String nTitle = noteTitle.getText().toString();
            String nContent = noteContent.getText().toString();

            if(nTitle.isEmpty() || nContent.isEmpty()){
                Toast.makeText(AddNote.this,"Title or Content cant be empty",Toast.LENGTH_SHORT).show();
                return;
            }

            progressBarSave.setVisibility(View.VISIBLE);

            //save note
            DocumentReference documentReference = fStore.collection("users").document(user.getUid())
                    .collection("myNotes").document(noteId);

            Map<String,Object> note = new HashMap<>();
            note.put("title",nTitle);
            note.put("content",nContent);
            note.put("createdOn", FieldValue.serverTimestamp());

            documentReference.set(note).addOnSuccessListener(aVoid -> {
                Toast.makeText(AddNote.this, "Note added", Toast.LENGTH_SHORT).show();
                onBackPressed();
            }).addOnFailureListener(e -> {
                Toast.makeText(AddNote.this, "Error, try again: " + e, Toast.LENGTH_SHORT).show();
                progressBarSave.setVisibility(View.INVISIBLE);
            });
        });

        checkForGooglePermissions();
        createFolderInDrive("My Notes",null);

        driveActionButton.setOnClickListener(v ->{
            deleteDriveFileFolder(fileId.getId());
            listFilesInDrive();
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.close_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.close){
            Toast.makeText(this, "Note is not saved",Toast.LENGTH_SHORT).show();
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    private void checkForGooglePermissions() {

        Scope ACCESS_DRIVE_SCOPE = new Scope(Scopes.DRIVE_FILE);
        Scope SCOPE_EMAIL = new Scope(Scopes.EMAIL);

        if (!GoogleSignIn.hasPermissions(
                GoogleSignIn.getLastSignedInAccount(getApplicationContext()),
                ACCESS_DRIVE_SCOPE,
                SCOPE_EMAIL)) {
            GoogleSignIn.requestPermissions(
                    this,
                    RC_AUTHORIZE_DRIVE,
                    GoogleSignIn.getLastSignedInAccount(getApplicationContext()),
                    ACCESS_DRIVE_SCOPE,
                    SCOPE_EMAIL);
        } else {
            Toast.makeText(this, "Permission to access Drive and Email has been granted", Toast.LENGTH_SHORT).show();
            driveSetUp();

        }

    }

    private void driveSetUp() {

        GoogleSignInAccount mAccount = GoogleSignIn.getLastSignedInAccount(this);

        GoogleAccountCredential credential =
                GoogleAccountCredential.usingOAuth2(
                        getApplicationContext(), Collections.singleton(Scopes.DRIVE_FILE));
        credential.setSelectedAccount(mAccount.getAccount());
        Drive googleDriveService =
                new Drive.Builder(
                        AndroidHttp.newCompatibleTransport(),
                        new GsonFactory(),
                        credential)
                        .setApplicationName("Shared Notes")
                        .build();
        mDriveServiceHelper = new DriveServiceHelper(googleDriveService);
    }

    public void createFolderInDrive(String folderName, @Nullable String folderId) {

        Log.i(TAG, "Creating a Folder...");
        mDriveServiceHelper.createFolder(folderName, folderId)
                .addOnSuccessListener(googleDriveFileHolder -> {

                    Gson gson = new Gson();
                    Log.i(TAG, "onSuccess of Folder creation: " + gson.toJson(googleDriveFileHolder));
                    fileId = googleDriveFileHolder;
                })
                .addOnFailureListener(e -> Log.i(TAG, "onFailure of Folder creation: " + e.getMessage()));
    }

    public void listFilesInDrive() {

        Log.i(TAG, "Listing Files...");
        new MyAsyncTask().execute();

    }

    public void uploadFile() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_PICK_IMAGE);

        } else {
            Intent i = new Intent(
                    Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

            startActivityForResult(i, RESULT_LOAD_IMAGE);
        }
    }

    private void uploadImageIntoDrive(Bitmap bitmap) {

        try {

            if (bitmap == null) {

                Log.i(TAG, "Bitmap is null");
                return;
            }
            java.io.File file = new java.io.File(getApplicationContext().getFilesDir(), "FirstFile");
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 0 /*ignored for PNG*/, bos);
            byte[] bitmapData = bos.toByteArray();

            //write the bytes in file
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(bitmapData);
            fos.flush();
            fos.close();

            mDriveServiceHelper.uploadFile(file, "image/jpeg", null)
                    .addOnSuccessListener(googleDriveFileHolder -> Log.i(TAG, "Successfully Uploaded. File Id :" + googleDriveFileHolder.getId()))
                    .addOnFailureListener(e -> Log.i(TAG, "Failed to Upload. File Id :" + e.getMessage()));
        } catch (Exception e) {

            Log.i(TAG, "Exception : " + e.getMessage());
        }

    }

    public void downloadFile(java.io.File file,String fileID) {

//        java.io.File file = new java.io.File(getExternalFilesDir(null), "/certificate.jpg");
        mDriveServiceHelper.downloadFile(file, fileID)
                .addOnSuccessListener(aVoid -> {

                    Log.i(TAG, "Downloaded the file");
                    long file_size = file.length() / 1024;
                    Log.i(TAG, "file Size :" + file_size);
                    Log.i(TAG, "file Path :" + file.getAbsolutePath());
//                    Toast.makeText(this,"Downloaded: " + file.getAbsolutePath(),Toast.LENGTH_LONG).show();
                })
                .addOnFailureListener(e -> Log.i(TAG, "Failed to Download the file: " + fileID + ", Exception : " + e.getMessage()));
    }

    public void deleteDriveFileFolder(String fileId) {

        mDriveServiceHelper.deleteFolderFile(fileId)
                .addOnSuccessListener(aVoid -> Log.i(TAG, "onSuccess of Deleting File "))
                .addOnFailureListener(e -> Log.i(TAG, "onFailure on Deleting File Exception : " + e.getMessage()));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RESULT_LOAD_IMAGE) {

            if (resultCode == RESULT_OK) {

                Uri selectedImage = data.getData();
                String[] filePathColumn = {MediaStore.Images.Media.DATA};

                Cursor cursor = getContentResolver().query(selectedImage,
                        filePathColumn, null, null, null);
                cursor.moveToFirst();

                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                String picturePath = cursor.getString(columnIndex);
                cursor.close();

                uploadImageIntoDrive(BitmapFactory.decodeFile(picturePath));

            } else {

                Toast.makeText(this, "Did not select any image", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public class MyAsyncTask extends AsyncTask<Void, Void, List<File>> {

        List<File> fileList;

        @Override
        protected List<File> doInBackground(Void... voids) {

            try {

                fileList = mDriveServiceHelper.listDriveImageFiles();

            } catch (IOException e) {

                Log.i(TAG, "IO Exception while fetching file list");
            }

            return fileList;

        }

        @Override
        protected void onPostExecute(List<File> files) {
            super.onPostExecute(files);

            if (files.size() == 0){

                Log.i(TAG, "No Files");
            }
            for (File file : files) {

                Log.i(TAG, "\nFound file: File Name :" +
                        file.getName() + " File Id :" + file.getId());
            }
        }
    }

}