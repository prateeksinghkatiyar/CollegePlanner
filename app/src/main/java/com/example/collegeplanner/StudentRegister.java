package com.example.collegeplanner;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class StudentRegister extends AppCompatActivity {

    EditText name, email, password, confirmPassword;
    Button register;
    CircleImageView profileImage;
    // Progress bar
    ProgressDialog progressDialog;

    private FirebaseAuth firebaseAuth;
    FirebaseUser user;
    Uri image_uri;

    String nameRG, emailRG, passwordRG, passwordCRG;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_register);

        name = findViewById(R.id.txt_Rname);
        email = findViewById(R.id.txt_Remail);
        password = findViewById(R.id.txt_Rpassword);
        confirmPassword = findViewById(R.id.txt_RCpassword);
        register = findViewById(R.id.btn_register);
        profileImage = findViewById(R.id.profile_image);
        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();
        //init progress bar
        progressDialog = new ProgressDialog(this);
        progressDialog.setCanceledOnTouchOutside(false);

        //profile Image
        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showImagePick();
            }
        });
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                nameRG = name.getText().toString().trim();
                emailRG = email.getText().toString().trim();
                passwordRG = password.getText().toString().trim();
                passwordCRG = confirmPassword.getText().toString().trim(); 

                if (TextUtils.isEmpty(nameRG)) {
                    Toast.makeText(StudentRegister.this, "Enter name", Toast.LENGTH_SHORT).show();
                } else if (TextUtils.isEmpty(emailRG)) {
                    Toast.makeText(StudentRegister.this, "Enter email", Toast.LENGTH_SHORT).show();
                } else if (TextUtils.isEmpty(passwordRG)) {
                    Toast.makeText(StudentRegister.this, "Enter password", Toast.LENGTH_SHORT).show();
                } else if (TextUtils.isEmpty(passwordCRG)) {
                    Toast.makeText(StudentRegister.this, "Enter password again", Toast.LENGTH_SHORT).show();
                } else if (passwordRG.length() < 8) {
                    Toast.makeText(StudentRegister.this, "Password length should be more than 8", Toast.LENGTH_SHORT).show();
                } else if (!passwordRG.equals(passwordCRG)) {
                    Toast.makeText(StudentRegister.this, "Password doesn't match", Toast.LENGTH_SHORT).show();
                } else
                    registerUser();
            }
        });


    }

    private void showImagePick() {
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(1, 1)
                .setOutputCompressFormat(Bitmap.CompressFormat.JPEG)
                .setOutputCompressQuality(30)
                .start(StudentRegister.this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {

            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                assert result != null;
                image_uri = result.getUri();
            }
            profileImage.setImageURI(image_uri);

        } else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
            Toast.makeText(this, "Image crop failed...", Toast.LENGTH_SHORT).show();

        }
    }


    private void registerUser() {

        progressDialog.setMessage("Registering...");
        progressDialog.show();
        firebaseAuth.createUserWithEmailAndPassword(emailRG, passwordRG)
                .addOnCompleteListener(StudentRegister.this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    progressDialog.dismiss();
                    savingUserInfo();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(StudentRegister.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void savingUserInfo() {

        progressDialog.setMessage("Saving user info...");
        progressDialog.show();
        if (image_uri == null){
            HashMap<String,Object> hashMap = new HashMap<>();
            hashMap.put("uid", "" + firebaseAuth.getUid());
            hashMap.put("name", "" + nameRG);
            hashMap.put("email", "" + emailRG);
            hashMap.put("password", "" + passwordRG);
            hashMap.put("batch", "");
            hashMap.put("branch", "");
            hashMap.put("rollNo", "");
            hashMap.put("phone", "");
            hashMap.put("profileImage", "");

            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Students");
            databaseReference.child(Objects.requireNonNull(firebaseAuth.getUid())).setValue(hashMap)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            progressDialog.dismiss();
                            startActivity(new Intent(StudentRegister.this,StudentHome.class));
                            finish();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progressDialog.dismiss();
                    Toast.makeText(StudentRegister.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

        }else {
            String filePathAndName = firebaseAuth.getUid();
            StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("ProfileImages").child(filePathAndName);
            storageReference.putFile(image_uri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                            while (!uriTask.isSuccessful());
                            Uri downloadUri = uriTask.getResult();

                            if(uriTask.isSuccessful()){
                                HashMap<String,Object> hashMap = new HashMap<>();
                                hashMap.put("uid", "" + firebaseAuth.getUid());
                                hashMap.put("name", "" + nameRG);
                                hashMap.put("email", "" + emailRG);
                                hashMap.put("password", "" + passwordRG);
                                hashMap.put("batch", "");
                                hashMap.put("branch", "");
                                hashMap.put("rollNo", "");
                                hashMap.put("phone", "");
                                hashMap.put("profileImage", ""+downloadUri);

                                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Students");
                                databaseReference.child(Objects.requireNonNull(firebaseAuth.getUid())).setValue(hashMap)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                progressDialog.dismiss();
                                                startActivity(new Intent(StudentRegister.this,StudentHome.class));
                                                finish();
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        progressDialog.dismiss();
                                        Toast.makeText(StudentRegister.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progressDialog.dismiss();
                    Toast.makeText(StudentRegister.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    public void login(View view) {
        startActivity(new Intent(getApplicationContext(), StudentLogin.class));
        finish();
    }
}







