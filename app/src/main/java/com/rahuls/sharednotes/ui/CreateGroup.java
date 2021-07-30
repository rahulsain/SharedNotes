package com.rahuls.sharednotes.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.rahuls.sharednotes.R;
import com.rahuls.sharednotes.auth.Login;
import com.rahuls.sharednotes.auth.Register;
import com.rahuls.sharednotes.group.AddGroup;
import com.rahuls.sharednotes.group.AddGroupNote;
import com.rahuls.sharednotes.group.SharedNote;
import com.rahuls.sharednotes.model.Group;

import java.util.Objects;

public class CreateGroup extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    DrawerLayout drawerLayout;
    ActionBarDrawerToggle toggle;
    NavigationView nav_view;
    RecyclerView groupLists;
    FirebaseFirestore fStore;
    FirestoreRecyclerAdapter<Group, NoteViewHolder> groupAdapter;
    FirebaseUser user;
    FirebaseAuth fAuth;
    Group group;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        fStore = FirebaseFirestore.getInstance();
        fAuth = FirebaseAuth.getInstance();
        user = fAuth.getCurrentUser();

        final String[] UserName = {""};
//        final String[] EmailId = {""};

        Query query = fStore.collection("groups").whereArrayContains("GroupMembers", Objects.requireNonNull(user.getEmail()));
//        Toast.makeText(getApplicationContext(),user.getEmail() + " " + user.getDisplayName(),Toast.LENGTH_SHORT).show();
//        Intent data = getIntent();

        FirestoreRecyclerOptions<Group> allNotes = new FirestoreRecyclerOptions.Builder<Group>()
                .setQuery(query, Group.class).build();

        groupAdapter = new FirestoreRecyclerAdapter<Group, NoteViewHolder>(allNotes) {
            @Override
            protected void onBindViewHolder(@NonNull NoteViewHolder holder, int position, @NonNull Group model) {
                holder.groupName.setText(model.getGroupName());
                final String groupId = groupAdapter.getSnapshots().getSnapshot(position).getId();

                holder.view.setOnClickListener(view -> {
                    Intent intent = new Intent(CreateGroup.this, SharedNote.class).putExtra("groupId", groupId);
                    intent.putExtra("UserName", UserName[0]);
                    startActivity(intent);
                });
            }

            @NonNull
            @Override
            public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.group_view_layout, parent, false);
                return new NoteViewHolder(view);
            }
        };

        groupLists = findViewById(R.id.grouplist);

        drawerLayout = findViewById(R.id.drawer3);
        nav_view = findViewById(R.id.nav_view3);
        nav_view.setNavigationItemSelectedListener(this);
        toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(toggle);
        toggle.setDrawerIndicatorEnabled(true);
        toggle.syncState();

        groupLists.setLayoutManager(new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL));
        groupLists.setAdapter(groupAdapter);

        View headerView = nav_view.getHeaderView(0);
        TextView userName = headerView.findViewById(R.id.userDisplayName);
        TextView userEmail = headerView.findViewById(R.id.userDisplayEmail);
        nav_view.getMenu().findItem(R.id.groups).setTitle("Personal Notes");

        if (user.isAnonymous()) {
            userEmail.setVisibility(View.GONE);
            userName.setText(R.string.temp_user);
        } else {
            DocumentReference docRef = fStore.collection("users").document(user.getUid());
            docRef.get().addOnSuccessListener(documentSnapshot -> {
                UserName[0] = documentSnapshot.getString("UserName");
//                EmailId[0] = documentSnapshot.getString("UserEmail");
                userName.setText(UserName[0]);
            });

            userEmail.setText(user.getEmail());

        }

        FloatingActionButton fab = findViewById(R.id.addGroupFloat);
        fab.setOnClickListener(view ->
                startActivity(new Intent(view.getContext(), AddGroup.class)));
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        drawerLayout.closeDrawer(GravityCompat.START);
        switch (item.getItemId()) {
            case R.id.groups:
                startActivity(new Intent(this, MainActivity.class));
                overridePendingTransition(R.anim.slide_up, R.anim.slide_down);
                break;

            case R.id.addNote:
                Intent intent = new Intent(this, AddGroupNote.class);
                intent.putExtra("groupId", group.getGroupId());
                startActivity(intent);
                overridePendingTransition(R.anim.slide_up, R.anim.slide_down);
                break;

            case R.id.sync:
                if (user.isAnonymous()) {
                    startActivity(new Intent(this, Login.class));
                    overridePendingTransition(R.anim.slide_up, R.anim.slide_down);
                } else {
                    Toast.makeText(this, "You are Already Connected.", Toast.LENGTH_SHORT).show();
                }
                break;

            case R.id.logout:
                checkUser();
                break;

            default:
                Toast.makeText(this, "Coming Soon", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    private void checkUser() {
        //if user is real or not
        if (user.isAnonymous()) {
            displayAlert();
        } else {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(getApplicationContext(), Splash.class));
            overridePendingTransition(R.anim.slide_up, R.anim.slide_down);
            finish();
        }
    }

    private void displayAlert() {
        AlertDialog.Builder warning = new AlertDialog.Builder(this).setTitle("Are you sure?")
                .setMessage("You are logged in with temp Account. Logging out will permanently delete your data.")
                .setPositiveButton("Sync Note", (dialog, which) -> {
                    startActivity(new Intent(getApplicationContext(), Register.class));
                    overridePendingTransition(R.anim.slide_up, R.anim.slide_down);
                    finish();
                }).setNegativeButton("Logout", (dialog, which) -> {
                    //ToDo: delete data created by the Temp User

                    //TODO: delete the temp user

                    user.delete().addOnSuccessListener(aVoid -> {
                        startActivity(new Intent(getApplicationContext(), Splash.class));
                        overridePendingTransition(R.anim.slide_up, R.anim.slide_down);
                        finish();
                    });
                });
        warning.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.option_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.settings) {
            Toast.makeText(this, "Setting Menu is Clicked", Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();
        groupAdapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (groupAdapter != null) {
            groupAdapter.stopListening();
        }
    }

    public static class NoteViewHolder extends RecyclerView.ViewHolder {

        TextView groupName;
        View view;
        CardView mCardView;

        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);

            groupName = itemView.findViewById(R.id.groupName);
            view = itemView;
            mCardView = itemView.findViewById(R.id.groupCard);
        }
    }
}