package com.rahuls.sharednotes.ui;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.rahuls.sharednotes.R;
import com.rahuls.sharednotes.auth.Login;
import com.rahuls.sharednotes.auth.Logout;
import com.rahuls.sharednotes.auth.Register;
import com.rahuls.sharednotes.group.ListGroups;
import com.rahuls.sharednotes.note.AddNote;
import com.rahuls.sharednotes.note.MainActivity;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EditProfile extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    public static final String TAG = "EditProfile";
    private static final int GALLERY_INTENT_CODE = 105;
    private static final int CAMERA_INTENT_CODE = 102;
    private static final int CAMERA_PERM_CODE = 101;
    EditText profileFullName,profileEmail,profilePhone;
    ImageView profileImageView;
    Button saveBtn;
    FirebaseAuth fAuth;
    FirebaseFirestore fStore;
    FirebaseUser user;
    StorageReference storageReference;
    DrawerLayout drawerLayout;
    ActionBarDrawerToggle toggle;
    NavigationView nav_view;
    String currentPhotoPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        Toolbar toolbar = findViewById(R.id.toolbar_fragment2);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer5);
        nav_view = findViewById(R.id.nav_view5);
        nav_view.setNavigationItemSelectedListener(this);
        toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(toggle);
        toggle.setDrawerIndicatorEnabled(true);
        toggle.syncState();


        View headerView = nav_view.getHeaderView(0);
        TextView userName = headerView.findViewById(R.id.userDisplayName);
        TextView userEmail = headerView.findViewById(R.id.userDisplayEmail);

        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        user = fAuth.getCurrentUser();
        storageReference = FirebaseStorage.getInstance().getReference();

        Intent data = getIntent();
        String fullName = data.getStringExtra("fullName");
        String email = data.getStringExtra("email");
        String phone = data.getStringExtra("phone");

        userName.setText(data.getStringExtra("userName"));
        userEmail.setText(data.getStringExtra("userEmail"));

        if (user.isAnonymous()) {
            userEmail.setVisibility(View.GONE);
        }

        profileFullName = findViewById(R.id.profileFullName);
        profileEmail = findViewById(R.id.profileEmailAddress);
        profilePhone = findViewById(R.id.profilePhoneNo);
        profileImageView = findViewById(R.id.profileImageView);
        saveBtn = findViewById(R.id.saveProfileInfo);

        StorageReference profileRef = storageReference.child("users/"+user.getUid()+"/profile.jpg");
        profileRef.getDownloadUrl().addOnSuccessListener(uri -> Picasso.get().load(uri).into(profileImageView));

        profileImageView.setOnClickListener(v -> {
            if(userEmail.getText().toString().equals("rahul1champ@gmail.com")) {
                Toast.makeText(EditProfile.this, "Picture can't be changed. Not registered to our database. Sign in first", Toast.LENGTH_SHORT).show();
            } else {
                startDialog();
            }
        });

        saveBtn.setOnClickListener(v -> {
            if(profileFullName.getText().toString().isEmpty() || profileEmail.getText().toString().isEmpty() || profilePhone.getText().toString().isEmpty()){
                Toast.makeText(EditProfile.this, "One or Many fields are empty.", Toast.LENGTH_SHORT).show();
                return;
            }

            if(!isValid(profileEmail.getText().toString())){
                Toast.makeText(EditProfile.this, "Email provided is not Valid", Toast.LENGTH_SHORT).show();
                return;
            }

            if(!isValidPh(profilePhone.getText().toString())){
                Toast.makeText(EditProfile.this, "Phone Number provided is not Valid, Make sure only number are included", Toast.LENGTH_SHORT).show();
                return;
            }

            final String email1 = profileEmail.getText().toString();
            if(userEmail.getText().toString().equals("rahul1champ@gmail.com")) {
                Toast.makeText(v.getContext(), "Email can't be changed. Not registered to our database. Sign in first", Toast.LENGTH_SHORT).show();
            } else {
                user.updateEmail(email1).addOnSuccessListener(aVoid -> {
                    DocumentReference docRef = fStore.collection("users").document(user.getUid());
                    Map<String, Object> edited = new HashMap<>();
                    edited.put("UserEmail", email1);
                    edited.put("UserName", profileFullName.getText().toString());
                    edited.put("UserPhone", profilePhone.getText().toString());
                    docRef.update(edited).addOnSuccessListener(aVoid1 -> {
                        Toast.makeText(v.getContext(), "Profile Updated", Toast.LENGTH_SHORT).show();
                        startNewActivity(v.getContext(), MainActivity.class);
                        finish();
                    });
                    Toast.makeText(v.getContext(), "Email is changed.", Toast.LENGTH_SHORT).show();
                }).addOnFailureListener(e -> Toast.makeText(v.getContext(), e.getMessage(), Toast.LENGTH_SHORT).show());
            }

        });

        profileEmail.setText(email);
        profileFullName.setText(fullName);
        profilePhone.setText(phone);

    }

    private void askCameraPermissions() {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.CAMERA}, CAMERA_PERM_CODE );
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

    private void startNewActivity(Context context, Class<?> actClass) {
        Intent intent = new Intent(context, actClass);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_up, R.anim.slide_down);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        drawerLayout.closeDrawer(GravityCompat.START);
        int itemId = item.getItemId();
        if (itemId == R.id.groups) {
            Intent intent = new Intent(this, ListGroups.class);
            intent.putExtra("userName", getIntent().getStringExtra("userName"));
            intent.putExtra("userEmail", getIntent().getStringExtra("userEmail"));
            startActivity(intent);
        } else if (itemId == R.id.addNote) {
            startNewActivity(this, AddNote.class);
        } else if (itemId == R.id.sync) {
            if (user.isAnonymous()) {
                startNewActivity(this, Login.class);
            } else {
                Toast.makeText(this, "You are Already Connected.", Toast.LENGTH_SHORT).show();
            }
        } else if (itemId == R.id.logout) {
            checkUser();
        } else {
            Toast.makeText(this, "Coming Soon", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    private void checkUser() {
        //if user is real or not
        if (user.isAnonymous()) {
            displayAlert();
        } else {
            fAuth.signOut();
            startNewActivity(this, Splash.class);
            finish();
        }
    }

    private void displayAlert() {
        androidx.appcompat.app.AlertDialog.Builder warning = new androidx.appcompat.app.AlertDialog.Builder(this).setTitle("Are you sure?")
                .setMessage("You are logged in with temp Account. Logging out will permanently delete your data.")
                .setPositiveButton("Sync Note", (dialog, which) -> {
                    startNewActivity(this, Register.class);
                    finish();
                }).setNegativeButton("Logout", (dialog, which) -> startNewActivity(this, Logout.class));
        warning.show();
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
                profileImageView.setImageURI(contentUri);
                uploadImageToFirebase(contentUri);
            }
        }

        if (requestCode == GALLERY_INTENT_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                Uri imageUri = data != null ? data.getData() : null;
                profileImageView.setImageURI(imageUri);
                uploadImageToFirebase(imageUri);
            }
        }
    }

    private void uploadImageToFirebase(Uri imageUri) {
        // upload image to firebase storage
        final StorageReference fileRef = storageReference.child("users/"+user.getUid()+"/profile.jpg");
        fileRef.putFile(imageUri).addOnSuccessListener(taskSnapshot -> fileRef.getDownloadUrl().addOnSuccessListener(uri -> Picasso.get().load(uri).into(profileImageView))).addOnFailureListener(e -> Toast.makeText(getApplicationContext(), "Failed.", Toast.LENGTH_SHORT).show());

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

    private boolean isValid(String email) {
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\." +
                "[a-zA-Z0-9_+&*-]+)*@" +
                "(?:[a-zA-Z0-9-]+\\.)+[a-z" +
                "A-Z]{2,7}$";

        Pattern pat = Pattern.compile(emailRegex);
        if (email == null)
            return false;
        return pat.matcher(email).matches();
    }

    public static boolean isValidPh(String s)
    {
        String allCountryRegex = "^(\\+\\d{1,3}( )?)?((\\(\\d{1,3}\\))|\\d{1,3})[- .]?\\d{3,4}[- .]?\\d{4}$";
        Pattern p = Pattern.compile(allCountryRegex);

        // Pattern class contains matcher() method to find matching between given number and regular expression
        Matcher m = p.matcher(s);
        return (m.find() && m.group().equals(s));
    }

    private void startDialog() {
        AlertDialog.Builder myAlertDialog = new AlertDialog.Builder(
                this);
        myAlertDialog.setTitle("Upload Pictures Option");
        myAlertDialog.setMessage("How do you want to set your picture?");

        myAlertDialog.setPositiveButton("Gallery",
                (arg0, arg1) -> {
                    Intent openGalleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(openGalleryIntent,GALLERY_INTENT_CODE);
                });

        myAlertDialog.setNegativeButton("Camera",
                (arg0, arg1) -> askCameraPermissions());
        myAlertDialog.show();
    }

}