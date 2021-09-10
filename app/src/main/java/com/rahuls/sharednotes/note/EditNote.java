package com.rahuls.sharednotes.note;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.rahuls.sharednotes.R;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class EditNote extends AppCompatActivity {

    private static final int GALLERY_INTENT_CODE = 105;
    private static final int CAMERA_INTENT_CODE = 102;
    private static final int CAMERA_PERM_CODE = 101;
    Intent data;
    EditText editNoteTitle, editNoteContent;
    FirebaseFirestore fStore;
    ProgressBar spinner;
    FirebaseUser user;
    String currentPhotoPath;
    Button editPhotoButton;
    StorageReference storageReference;
    ImageView noteImageView;
    Uri photoURI = null;
    StorageReference fileRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_note);
        Toolbar toolbar = findViewById(R.id.toolbar2);
        setSupportActionBar(toolbar);

        fStore = FirebaseFirestore.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();
        storageReference = FirebaseStorage.getInstance().getReference();
        spinner = findViewById(R.id.progressBar2);

        data = getIntent();

        editNoteTitle = findViewById(R.id.editNoteTitle);
        editNoteContent = findViewById(R.id.editNoteContent);

        editPhotoButton = findViewById(R.id.edit_photo);
        noteImageView = findViewById(R.id.editNoteImage);

        editNoteTitle.setText(data.getStringExtra("title"));
        editNoteContent.setText(data.getStringExtra("content"));

        fileRef = storageReference.child("users/" + user.getUid() + "/myNotes/" + data.getStringExtra("noteId") + "/imageNote.jpg");
        fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
            Picasso.get().load(uri).into(noteImageView);
            noteImageView.setVisibility(View.VISIBLE);
        });

        FloatingActionButton fab = findViewById(R.id.saveEditedNote);
        fab.setOnClickListener(v -> {

            uploadImageToFirebase(photoURI);

            String nTitle = editNoteTitle.getText().toString();
            String nContent = editNoteContent.getText().toString();

            if (nTitle.isEmpty() || nContent.isEmpty()) {
                Toast.makeText(EditNote.this, "Title or Content cant be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            spinner.setVisibility(View.VISIBLE);

            //save note
            DocumentReference documentReference = fStore.collection("users").document(user.getUid())
                    .collection("myNotes").document(data.getStringExtra("noteId"));
            Map<String, Object> note = new HashMap<>();
            note.put("title", nTitle);
            note.put("content", nContent);
            note.put("createdOn", FieldValue.serverTimestamp());

            documentReference.update(note).addOnSuccessListener(aVoid -> {
                Toast.makeText(EditNote.this, "Note updated", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
            }).addOnFailureListener(e -> {
                Toast.makeText(EditNote.this, "Error, try again: " + e, Toast.LENGTH_SHORT).show();
                spinner.setVisibility(View.INVISIBLE);
            });
        });
        editPhotoButton.setOnClickListener(v -> startDialog());
    }

    private void startDialog() {
        AlertDialog.Builder myAlertDialog = new AlertDialog.Builder(
                this);
        myAlertDialog.setTitle("Upload Pictures Option");
        myAlertDialog.setMessage("How do you want to set your picture?");

        myAlertDialog.setPositiveButton("Gallery",
                (arg0, arg1) -> {
                    Intent openGalleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(openGalleryIntent, GALLERY_INTENT_CODE);
                });

        myAlertDialog.setNegativeButton("Camera",
                (arg0, arg1) -> askCameraPermissions());
        myAlertDialog.show();
    }

    private void askCameraPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERM_CODE);
        } else {
            dispatchTakePictureIntent();
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull @NotNull String[] permissions, @NonNull @NotNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERM_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                dispatchTakePictureIntent();
            } else {
                Toast.makeText(this, "Camera Permission is Required to use Camera", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @androidx.annotation.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if (requestCode == CAMERA_INTENT_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                File f = new File(currentPhotoPath);
                Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                Uri contentUri = Uri.fromFile(f);
                mediaScanIntent.setData(contentUri);
                this.sendBroadcast(mediaScanIntent);
                noteImageView.setImageURI(contentUri);
                noteImageView.setVisibility(View.VISIBLE);
                photoURI = contentUri;
            }
        }

        if (requestCode == GALLERY_INTENT_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                Uri imageUri = data != null ? data.getData() : null;
                noteImageView.setImageURI(imageUri);
                noteImageView.setVisibility(View.VISIBLE);
                photoURI = imageUri;
            }
        }
    }

    private void uploadImageToFirebase(@Nullable Uri imageUri) {
        // upload image to firebase storage
        if (imageUri != null) {
            fileRef.putFile(imageUri).addOnSuccessListener(taskSnapshot -> fileRef.getDownloadUrl().addOnSuccessListener(uri -> Picasso.get().load(uri).into(noteImageView))).addOnFailureListener(e -> Toast.makeText(getApplicationContext(), "Failed.", Toast.LENGTH_SHORT).show());
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
//        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }


    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ignored) {

            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.rahuls.android.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, CAMERA_INTENT_CODE);
            }
        }
    }
}