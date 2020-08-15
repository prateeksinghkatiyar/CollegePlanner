package com.example.collegeplanner;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.app.Activity.RESULT_OK;
import static com.google.firebase.storage.FirebaseStorage.getInstance;


/**
 * A simple {@link Fragment} subclass.
 */
public class ProfileFragment extends Fragment {

    //firebase
    private FirebaseAuth firebaseAuth;
    private FirebaseUser user;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;
    private StorageReference storageReference;
    private ProgressDialog progressDialog;
    private String uid, uMail;

    //views
    private CircleImageView userDp;
    private TextView uName, uEmail, uPhone, uBatch, uBranch, uRollNo;
    private ImageButton btnName, btnEmail, btnPhone, btnBatch, btnBranch, btnRollNo;


    public ProfileFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View profile = inflater.inflate(R.layout.fragment_profile, container, false);

        //init firebase
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("Students");
        storageReference = getInstance().getReference();
        user = firebaseAuth.getCurrentUser();

        //progress dailog
        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setMessage("Updating...");
        progressDialog.setCanceledOnTouchOutside(false);

        //init views
        userDp = profile.findViewById(R.id.img_userProfilePic);
        uName = profile.findViewById(R.id.txt_userName);
        uEmail = profile.findViewById(R.id.txt_userEmail);
        uPhone = profile.findViewById(R.id.txt_userPhone);
        uBatch = profile.findViewById(R.id.txt_userBatch);
        uBranch = profile.findViewById(R.id.txt_userBranch);
        uRollNo = profile.findViewById(R.id.txt_userRollNo);
        btnName = profile.findViewById(R.id.btn_editName);
        btnEmail = profile.findViewById(R.id.btn_editEmail);
        btnPhone = profile.findViewById(R.id.btn_editPhone);
        btnBatch = profile.findViewById(R.id.btn_editBatch);
        btnBranch = profile.findViewById(R.id.btn_editBranch);
        btnRollNo = profile.findViewById(R.id.btn_editRollNo);


        Query query = databaseReference.orderByChild("uid").equalTo(user.getUid());
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    String dp = "" + ds.child("profileImage").getValue();
                    String name = "" + ds.child("name").getValue();
                    String email = "" + ds.child("email").getValue();
                    String phone = "" + ds.child("phone").getValue();
                    String batch = "" + ds.child("batch").getValue();
                    String branch = "" + ds.child("branch").getValue();
                    String rollNo = "" + ds.child("rollNo").getValue();

                    //set data
                    uName.setText(name);
                    uEmail.setText(email);
                    uPhone.setText(phone);
                    uBatch.setText(batch);
                    uBranch.setText(branch);
                    uRollNo.setText(rollNo);
                    try {
                        Picasso.get().load(dp).into(userDp);
                    } catch (Exception e) {
                        Picasso.get().load(R.drawable.holder).into(userDp);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        userDp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateUserDp();
            }
        });
        btnName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateUserData("name");
            }
        });
        btnEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateUserData("email");
            }
        });
        btnPhone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateUserData("phone");
            }
        });
        btnBatch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateUserData("batch");
            }
        });
        btnBranch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateUserData("branch");
            }
        });
        btnRollNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateUserData("rollNo");
            }
        });

        checkUserStatus();

        return profile;
    }

    private void checkUserStatus() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if(user != null){
            uid = user.getUid();
            uMail = user.getEmail();
            // stay here
        } else {
            startActivity(new Intent(getActivity(), StudentLogin.class));
        }
    }


    private void updateUserDp() {
        startActivity(new Intent(getActivity(), ProfilePicActivity.class));
    }

    private void updateUserData(final String key) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Update " + key);
        LinearLayout linearLayout = new LinearLayout(getActivity());
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setPadding(10, 10, 10, 10);

        final EditText editText = new EditText(getActivity());
        editText.setHint("Enter " + key);
        linearLayout.addView(editText);

        builder.setView(linearLayout);

        builder.setPositiveButton("Update ", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                final String value = editText.getText().toString().trim();
                if (!TextUtils.isEmpty(value)) {
                    progressDialog.show();
                    HashMap<String, Object> hashMap = new HashMap<>();
                    hashMap.put(key, value);

                    databaseReference.child(user.getUid()).updateChildren(hashMap)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    progressDialog.dismiss();
                                    Toast.makeText(getActivity(), key + " updated successfully", Toast.LENGTH_SHORT).show();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(getActivity(), "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

                    //if user edit his name,also change it from his post
                    if (key.equals("name")) {
                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts");
                        Query query = reference.orderByChild("userId").equalTo(uid);
                        query.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                    String child = ds.getKey();
                                    dataSnapshot.getRef().child(child).child("userName").setValue(value);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });

                        //update name in current user comments on posts
                        reference.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                    String child = ds.getKey();
                                    if (dataSnapshot.child(child).hasChild("Comments")) {
                                        String child1 = "" + dataSnapshot.child(child).getKey();
                                        Query child2 = FirebaseDatabase.getInstance().getReference("Posts").child(child1).child("Comments")
                                                .orderByChild("userId").equalTo(uid);
                                        child2.addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                                    String child = ds.getKey();
                                                    dataSnapshot.getRef().child(child).child("userName").setValue(value);
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {

                                            }
                                        });
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }

                } else {
                    Toast.makeText(getActivity(), "Please enter " + key, Toast.LENGTH_SHORT).show();

                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.create().show();
    }

}
