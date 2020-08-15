package com.example.collegeplanner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class StudentHome extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private CircleImageView userImage;
    private TextView userName, userEmail;
    FirebaseAuth firebaseAuth;
    DatabaseReference reference;
    String currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_home);

        //firebase setup
        firebaseAuth = FirebaseAuth.getInstance();
        currentUser = firebaseAuth.getCurrentUser().getUid();
        reference = FirebaseDatabase.getInstance().getReference().child("Students");
        //toolbar
        Toolbar toolbar = findViewById(R.id.toolbar1);
        setSupportActionBar(toolbar);
        //drawer layout
        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(onNavigationItemSelectedListener);
        // set nav view user data
        View navHeader = navigationView.getHeaderView(0);
        userImage = navHeader.findViewById(R.id.img_userImage);
        userName = navHeader.findViewById(R.id.txt_userName);
        userEmail = navHeader.findViewById(R.id.txt_userEmail);

        reference.child(currentUser).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    String name = dataSnapshot.child("name").getValue().toString();
                    String email = dataSnapshot.child("email").getValue().toString();
                    String image = dataSnapshot.child("profileImage").getValue().toString();

                    userName.setText(name);
                    userEmail.setText(email);
                    try{
                        Picasso.get().load(image).into(userImage);
                    }catch (Exception e){
                        Picasso.get().load(R.drawable.holder).into(userImage);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();

        if (savedInstanceState == null) {
            Objects.requireNonNull(getSupportActionBar()).setTitle("Home");
            HomeFragment homeFragment = new HomeFragment();
            FragmentTransaction fthome = getSupportFragmentManager().beginTransaction();
            fthome.replace(R.id.fragment_container_student,homeFragment,"");
            fthome.commit();
        }
    }

    private NavigationView.OnNavigationItemSelectedListener onNavigationItemSelectedListener = new NavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            Fragment fragments = null;
            switch (item.getItemId()) {
                case R.id.nav_home_S:
                    Objects.requireNonNull(getSupportActionBar()).setTitle("Home");
                    fragments = new HomeFragment();
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container_student,
                            fragments).commit();
                    break;
                case R.id.nav_profile:
                    Objects.requireNonNull(getSupportActionBar()).setTitle("Profile");
                    fragments = new ProfileFragment();
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container_student,
                            fragments).commit();
                    break;
                case R.id.nav_faculty:
                    Objects.requireNonNull(getSupportActionBar()).setTitle("Faculty");
                    fragments = new FacltyFragment();
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container_student,
                            fragments).commit();
                    break;
                case R.id.nav_aboutus:
                    Objects.requireNonNull(getSupportActionBar()).setTitle("About Us");
                    fragments = new AboutFragment();
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container_student,
                            fragments).commit();
                    break;
                case R.id.nav_changepassword:
                    changePassword();
                    break;
                case R.id.nav_logout:
                    logoutDialog();
                    break;
            }
            assert fragments != null;
            drawerLayout.closeDrawer(GravityCompat.START);

            return true;
        }
    };

    private void logoutDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Are you sure to logout?");
        builder.setPositiveButton("Logout", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                firebaseAuth.signOut();
                checkUserStatus();
            }
        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        }else
            super.onBackPressed();
    }

    private void changePassword() {

        View view = LayoutInflater.from(StudentHome.this).inflate(R.layout.dailog_change_password,null);
        final EditText passwordEt = view.findViewById(R.id.passwordEt);
        final EditText newPasswordEt = view.findViewById(R.id.newPasswordEt);
        final EditText cnfPasswordEt = view.findViewById(R.id.cnfPasswordEt);
        Button updatePasswordBtn = view.findViewById(R.id.updatePasswordBtn);

        AlertDialog.Builder builder = new AlertDialog.Builder(StudentHome.this);
        builder.setView(view);

        final AlertDialog dialog = builder.create();
        dialog.show();

        updatePasswordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String oldPassword = passwordEt.getText().toString().trim();
                String newPassword = newPasswordEt.getText().toString().trim();
                String cnfPassword = cnfPasswordEt.getText().toString().trim();

                if (TextUtils.isEmpty(oldPassword)) {
                    Toast.makeText(StudentHome.this, "Enter old password", Toast.LENGTH_SHORT).show();
                } else if (TextUtils.isEmpty(newPassword)) {
                    Toast.makeText(StudentHome.this, "Enter new password", Toast.LENGTH_SHORT).show();
                } else if (TextUtils.isEmpty(cnfPassword)) {
                    Toast.makeText(StudentHome.this, "Enter new password again", Toast.LENGTH_SHORT).show();
                } else if (newPassword.length() < 8 || cnfPassword.length() < 8) {
                    Toast.makeText(StudentHome.this, "Password length should be more than 8", Toast.LENGTH_SHORT).show();
                } else if (!newPassword.equals(cnfPassword)) {
                    Toast.makeText(StudentHome.this, "New password doesn't match", Toast.LENGTH_SHORT).show();
                }else {
                    dialog.dismiss();
                    updatePassword(oldPassword, newPassword);
                }
            }
        });
    }

    private void updatePassword(String oldPassword, final String newPassword) {
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Updating Password...");
        progressDialog.show();

        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        //authenticate the user
        AuthCredential authCredential = EmailAuthProvider.getCredential(user.getEmail(), oldPassword);
        user.reauthenticate(authCredential)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        user.updatePassword(newPassword)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        progressDialog.dismiss();
                                        Toast.makeText(StudentHome.this, "Password Updated Successfully", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        progressDialog.dismiss();
                                        Toast.makeText(StudentHome.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(StudentHome.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private void checkUserStatus() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if(user != null){
            Toast.makeText(this, "User Logging in", Toast.LENGTH_SHORT).show();
        } else {
            startActivity(new Intent(this, StudentLogin.class));
            Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
            finish();
        }
    }
}
