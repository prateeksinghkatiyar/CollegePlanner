package com.example.collegeplanner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;

public class AddPost extends AppCompatActivity {

    private ImageView PostImage;
    private Button PostButton;
    private EditText PostDescription;

    private Uri ImageUri;

    private ProgressDialog progressDialog;

    private static final int STORAGE_REQUEST_CODE = 100;
    private static final int IMAGE_PICK_GALLERY_CODE = 200;
    private String[] storagePermissions;

    private FirebaseAuth firebaseAuth;

    String studentName, Email, Uid, studentDp;

    String editDescription, editImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_post);

        //toolbar
        final Toolbar toolbar = findViewById(R.id.toolbar_post);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Add New Post");

        //firebase
        firebaseAuth = FirebaseAuth.getInstance();
        checkUserStatus();

        //find view by id
        PostImage = findViewById(R.id.img_post);
        PostDescription = findViewById(R.id.txt_post);
        PostButton = findViewById(R.id.btn_post);

        //get data through intent
        Intent intent = getIntent();
        final String isUpdateKey = ""+intent.getStringExtra("key");
        final String editPostId = ""+intent.getStringExtra("editPostId");
        //validate if we came here to update post i.e. came from AdapterPost
        if (isUpdateKey.equals("editPost")){
            toolbar.setTitle("Update Post");
            loadPostData(editPostId);
        }else{
            // nothing
        }

        //import data
        DatabaseReference studentRef = FirebaseDatabase.getInstance().getReference().child("Students");
        Query query = studentRef.orderByChild("uid").equalTo(Uid);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot: dataSnapshot.getChildren()){
                    studentName = ""+ Objects.requireNonNull(snapshot.child("name").getValue()).toString();
                    studentDp = ""+ Objects.requireNonNull(snapshot.child("profileImage").getValue()).toString();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        //progress Dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setCanceledOnTouchOutside(true);

        //permission
        storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        //on click listener
        PostImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage();
            }
        });

        PostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String description = PostDescription.getText().toString().trim();

                if(TextUtils.isEmpty(description)){
                    Toast.makeText(AddPost.this, "Enter Post Description", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(isUpdateKey.equals("editPost")){
                    beginUpdate(description, editPostId);
                }
                else if(ImageUri==null){
                    //post without image
                    uploadWithoutImage(description);
                }else{
                    //post with image
                    BitmapDrawable bitmapDrawable = (BitmapDrawable)PostImage.getDrawable();
                    Bitmap bitmap = bitmapDrawable.getBitmap();
                    uploadWithImage(description, bitmap);
                }
            }
        });

    }

    private void uploadWithImage(final String description, Bitmap bitmap) {
        progressDialog.setMessage("Uploading post...");
        progressDialog.show();

        String timeStamp = String.valueOf(System.currentTimeMillis());

        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        calendar.setTimeInMillis(Long.parseLong(timeStamp));
        final String pTime = DateFormat.format("dd/MM/yyyy hh:mm aa",calendar).toString();

        String filePathandName = "Posts/" + "post_" + timeStamp;
        final String pId = Uid + timeStamp;

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG,30,baos);
        byte[] data = baos.toByteArray();

        //post with image
        StorageReference reference = FirebaseStorage.getInstance().getReference().child(filePathandName);
        reference.putBytes(data)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while (!uriTask.isSuccessful());

                        String downloadUri = uriTask.getResult().toString();

                        if (uriTask.isSuccessful()){

                            HashMap<String, Object> hashMap = new HashMap<>();
                            hashMap.put("userId",Uid);
                            hashMap.put("userName",studentName);
                            hashMap.put("userDp",studentDp);
                            hashMap.put("postDescription",description);
                            hashMap.put("postImage",downloadUri);
                            hashMap.put("postTime",pTime);
                            hashMap.put("postId", pId);
                            hashMap.put("postComments","0");

                            DatabaseReference reference1 = FirebaseDatabase.getInstance().getReference("Posts");
                            reference1.child(pId).setValue(hashMap)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            progressDialog.dismiss();
                                            sendUserToMainActivity();
                                            Toast.makeText(AddPost.this, "Post uploaded successfully", Toast.LENGTH_SHORT).show();
                                            finish();
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            progressDialog.dismiss();
                                            Toast.makeText(AddPost.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(AddPost.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void uploadWithoutImage(String description) {
        progressDialog.setMessage("Uploading post...");
        progressDialog.show();

        String timeStamp = String.valueOf(System.currentTimeMillis());

        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        calendar.setTimeInMillis(Long.parseLong(timeStamp));
        final String pTime = DateFormat.format("dd/MM/yyyy hh:mm aa",calendar).toString();

        final String pId = Uid + timeStamp;

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("userId",Uid);
        hashMap.put("userName",studentName);
        hashMap.put("userDp",studentDp);
        hashMap.put("postDescription",description);
        hashMap.put("postImage","noImage");
        hashMap.put("postTime",pTime);
        hashMap.put("postId", pId);
        hashMap.put("postComments","0");

        DatabaseReference reference1 = FirebaseDatabase.getInstance().getReference("Posts");
        reference1.child(pId).setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        progressDialog.dismiss();
                        sendUserToMainActivity();
                        Toast.makeText(AddPost.this, "Post uploaded successfully", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(AddPost.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });


    }

    private void beginUpdate(String description, String editPostId) {
        progressDialog.setMessage("Updating Post...");
        progressDialog.show();

        if(!editImage.equals("noImage")){
            updateWithImage(description,editPostId);
        }else if (PostImage.getDrawable()!=null){
            updateWithNowImage(description,editPostId);
        }else{
            updateWithoutImage(description,editPostId);
        }
    }

    private void updateWithoutImage(String description, String editPostId) {
        HashMap<String, Object> hashMap1 = new HashMap<>();
        hashMap1.put("userId",Uid);
        hashMap1.put("userName",studentName);
        hashMap1.put("userDp",studentDp);
        hashMap1.put("postDescription",description);
        hashMap1.put("postImage","noImage");

        DatabaseReference reference1 = FirebaseDatabase.getInstance().getReference("Posts");
        reference1.child(editPostId).updateChildren(hashMap1)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        progressDialog.dismiss();
                        Toast.makeText(AddPost.this, "Post updated successfully", Toast.LENGTH_SHORT).show();
                        sendUserToMainActivity();
                        finish();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(AddPost.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void updateWithImage(final String description, final String editPostId) {
        StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(editImage);
        storageReference.delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        String timeStamp = String.valueOf(System.currentTimeMillis());
                        String filePathAndName = "Posts/"+"post_"+timeStamp;

                        //get image
                        Bitmap bitmap = ((BitmapDrawable)PostImage.getDrawable()).getBitmap();
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.JPEG,30,baos);
                        byte[] data = baos.toByteArray();

                        StorageReference reference = FirebaseStorage.getInstance().getReference().child(filePathAndName);
                        reference.putBytes(data)
                                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                                        while (!uriTask.isSuccessful());

                                        String downloadUri = uriTask.getResult().toString();
                                        if(uriTask.isSuccessful()){
                                            HashMap<String, Object> hashMap1 = new HashMap<>();
                                            hashMap1.put("userId",Uid);
                                            hashMap1.put("userName",studentName);
                                            hashMap1.put("userDp",studentDp);
                                            hashMap1.put("postDescription",description);
                                            hashMap1.put("postImage",downloadUri);

                                            DatabaseReference reference1 = FirebaseDatabase.getInstance().getReference("Posts");
                                            reference1.child(editPostId).updateChildren(hashMap1)
                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                            progressDialog.dismiss();
                                                            Toast.makeText(AddPost.this, "Post updated successfully", Toast.LENGTH_SHORT).show();
                                                            sendUserToMainActivity();
                                                            finish();
                                                        }
                                                    }).addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    progressDialog.dismiss();
                                                    Toast.makeText(AddPost.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                                }
                                            });

                                        }
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                progressDialog.dismiss();
                                Toast.makeText(AddPost.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(AddPost.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateWithNowImage(final String description, final String editPostId) {
        String timeStamp = String.valueOf(System.currentTimeMillis());
        String filePathAndName = "Posts/"+"post_"+timeStamp;

        //get image
        Bitmap bitmap = ((BitmapDrawable)PostImage.getDrawable()).getBitmap();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG,30,baos);
        byte[] data = baos.toByteArray();

        StorageReference reference = FirebaseStorage.getInstance().getReference().child(filePathAndName);
        reference.putBytes(data)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while (!uriTask.isSuccessful());

                        String downloadUri = uriTask.getResult().toString();
                        if(uriTask.isSuccessful()){
                            HashMap<String, Object> hashMap1 = new HashMap<>();
                            hashMap1.put("userId",Uid);
                            hashMap1.put("userName",studentName);
                            hashMap1.put("userDp",studentDp);
                            hashMap1.put("postDescription",description);
                            hashMap1.put("postImage",downloadUri);

                            DatabaseReference reference1 = FirebaseDatabase.getInstance().getReference("Posts");
                            reference1.child(editPostId).updateChildren(hashMap1)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            progressDialog.dismiss();
                                            Toast.makeText(AddPost.this, "Post updated Successfully", Toast.LENGTH_SHORT).show();
                                            sendUserToMainActivity();
                                            finish();
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    progressDialog.dismiss();
                                    Toast.makeText(AddPost.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });

                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(AddPost.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void loadPostData(String editPostId) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts");

        Query query1 = reference.orderByChild("postId").equalTo(editPostId);
        query1.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot: dataSnapshot.getChildren()){
                    //get data
                    editDescription = ""+snapshot.child("postDescription").getValue();
                    editImage = ""+snapshot.child("postImage").getValue();

                    //set data to views
                    PostDescription.setText(editDescription);

                    //set image
                    if(!editImage.equals("noImage")){
                        try{
                            Picasso.get().load(editImage).into(PostImage);
                        }catch (Exception e){

                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        checkUserStatus();
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkUserStatus();
    }

    private void selectImage() {
        if(!checkStoragePermission()){
            requestStoragePermission();
        }
        else {
            selectFromGallery();
        }
    }

    private void selectFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, IMAGE_PICK_GALLERY_CODE);
    }

    private boolean checkStoragePermission() {
        return ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                (PackageManager.PERMISSION_GRANTED);
    }

    private void requestStoragePermission() {
        ActivityCompat.requestPermissions(this, storagePermissions, STORAGE_REQUEST_CODE);
    }

    private void checkUserStatus(){
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if(user != null){
            // stay here
            Email = user.getEmail();
            Uid = user.getUid();
        } else {
            startActivity(new Intent(this, StudentLogin.class));
            finish();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case STORAGE_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    boolean storageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (storageAccepted) {
                        selectFromGallery();
                    } else {
                        Toast.makeText(this, "Storage Permission is necessary", Toast.LENGTH_SHORT).show();
                    }
                }
            }break;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == IMAGE_PICK_GALLERY_CODE && resultCode == RESULT_OK && data != null) {
            ImageUri = data.getData();
            PostImage.setImageURI(ImageUri);
        }
    }

    // on back button
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed(); //goto previous activity
        return super.onSupportNavigateUp();
    }

    private void sendUserToMainActivity() {
        Intent mainIntent = new Intent(this, StudentHome.class);
        startActivity(mainIntent);
    }

}
