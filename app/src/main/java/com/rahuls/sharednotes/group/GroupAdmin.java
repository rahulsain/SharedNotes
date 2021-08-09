package com.rahuls.sharednotes.group;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.rahuls.sharednotes.R;
import com.rahuls.sharednotes.model.Group;
import com.rahuls.sharednotes.roomdb.Email;
import com.rahuls.sharednotes.roomdb.EmailRVAdapter;
import com.rahuls.sharednotes.roomdb.INotesRVAdapter;
import com.rahuls.sharednotes.roomdb.NotesViewModel;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class GroupAdmin extends AppCompatActivity implements INotesRVAdapter {

    private static final String TAG = "GroupAdmin";
    FirebaseFirestore fStore;
    EditText groupName, groupMember;
    ProgressBar progressBarSave;
    FirebaseUser user;
    Group group;
    NotesViewModel viewModel;
    DocumentReference documentReference;
    List<String> groupMembers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_group);
        Toolbar toolbar = findViewById(R.id.toolbar5);
        setSupportActionBar(toolbar);

        fStore = FirebaseFirestore.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();
        groupName = findViewById(R.id.addGroupName);
        groupMember = findViewById(R.id.emailGroupMember);

        progressBarSave = findViewById(R.id.progressBar);

        groupMembers = new ArrayList<>();
        group = new Group();

        RecyclerView memberList = findViewById(R.id.memberList);
        memberList.setLayoutManager(new LinearLayoutManager(this));
        EmailRVAdapter adapter = new EmailRVAdapter(this, this);
        memberList.setNestedScrollingEnabled(false);
        memberList.setAdapter(adapter);

        viewModel = new ViewModelProvider(this,
                ViewModelProvider.AndroidViewModelFactory.getInstance(getApplication())).get(NotesViewModel.class);
        viewModel.deleteAllNotes();
        viewModel.getAllEmail().observe(this, demo -> {
            if (demo != null) {
                adapter.updateList(demo);
            }
        });

        String groupId = getIntent().getStringExtra("groupId");
        documentReference = fStore.collection("groups").document(groupId);

        documentReference.get().addOnSuccessListener(documentSnapshot -> {
            group = documentSnapshot.toObject(Group.class);

            groupName.setText(group.getGroupName());

            List<String> tem = group.getGroupMembers();

                for (String mem : tem) {
                    groupMembers.add(mem);
                    viewModel.insertNotes(new Email(mem));
                }
        });

        FloatingActionButton fab1 = findViewById(R.id.addGroupMember);
        fab1.setOnClickListener(view -> {
            String groupMemberEmail = groupMember.getText().toString();
            if (isValid(groupMemberEmail)) {
                groupMembers.add(groupMemberEmail);
                groupMember.setText("");
                viewModel.insertNotes(new Email(groupMemberEmail));
            } else {
                Toast.makeText(getApplicationContext(), "Not a Valid Email", Toast.LENGTH_SHORT).show();
            }
        });

        FloatingActionButton fab2 = findViewById(R.id.fab3);
        fab2.setOnClickListener(view -> {
            group.setGroupName(groupName.getText().toString());
            group.setGroupMembers(groupMembers);

            if (group.getGroupName().isEmpty() && group.getGroupMembers().isEmpty()) {
                Toast.makeText(this, "Name and Member cant be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            progressBarSave.setVisibility(View.VISIBLE);

            //save group
            Map<String, Object> groupDetails = new HashMap<>();
            groupDetails.put("GroupName", group.getGroupName());
            groupDetails.put("GroupMembers", group.getGroupMembers());

            documentReference.update(groupDetails).addOnSuccessListener(aVoid -> {
                Toast.makeText(view.getContext(), "Group Updated", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(view.getContext(), ListGroups.class);
                intent.putExtra("userName", getIntent().getStringExtra("userName"));
                intent.putExtra("userEmail", getIntent().getStringExtra("userEmail"));
                viewModel.deleteAllNotes();
                startActivity(intent);
            }).addOnFailureListener(e -> {
                Toast.makeText(view.getContext(), "Error, try again", Toast.LENGTH_SHORT).show();
                progressBarSave.setVisibility(View.INVISIBLE);
            });
        });

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.close_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.close) {
            Toast.makeText(this, "Group not updated", Toast.LENGTH_SHORT).show();
            viewModel.deleteAllNotes();
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemClicked(@NotNull Email email) {
        viewModel.deleteNotes(email);
        documentReference.update("GroupMembers", FieldValue.arrayRemove(email.getText()));
        groupMembers.remove(email.getText());
    }

}
