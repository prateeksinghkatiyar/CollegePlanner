package com.example.collegeplanner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class PostDetailActivity extends AppCompatActivity {

    ImageView uPictureIv, pImageIv;
    TextView uNameTv, pTimeTv, pDescriptionTv, pCommentsTv;
    ImageButton moreBtn;
    LinearLayout profileLayout;

    boolean mProcessComment = false;

    //progress bar
    ProgressDialog pd;

    //add comments views
    EditText commentEt;
    ImageButton sendBtn;
    ImageView cAvatarIv;
    RecyclerView recyclerView;

    List<ModelComment> commentList;
    AdapterComments adapterComments;

    String myUid, myEmail, myName, myDp, postId, hisDp, hisName, hisUid, pImage, pImage1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);


        //toolbar
        final Toolbar toolbar = findViewById(R.id.toolbar_5);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Post Detail");

        Intent intent = getIntent();
        postId = intent.getStringExtra("postId");
        //init views
        uPictureIv = findViewById(R.id.img_userPic2);
        pImageIv = findViewById(R.id.img_postImage2);
        uNameTv = findViewById(R.id.txt_userName2);
        pTimeTv = findViewById(R.id.txt_postTime2);
        pDescriptionTv = findViewById(R.id.txt_postDescription2);
        pCommentsTv = findViewById(R.id.pCommentsTv);
        moreBtn = findViewById(R.id.btn_more2);
        profileLayout =  findViewById(R.id.lyt_postDetail);
        commentEt =  findViewById(R.id.et_comment);
        sendBtn =  findViewById(R.id.sendBtn);
        cAvatarIv =  findViewById(R.id.cAvatarIv);
        recyclerView = findViewById(R.id.recyclerView);

        loadPostInfo();

        checkUserStatus();

        loadUserInfo();

        loadComments();

        toolbar.setSubtitle("SignedIn as "+ myEmail);

        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                postComment();
            }
        });

        moreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMoreOptions();
            }
        });

        pImageIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fullImageView();
            }
        });

    }

    private void fullImageView() {
        View view1 = LayoutInflater.from(PostDetailActivity.this).inflate(R.layout.dailog_post_image,null);
        final ImageView postIv = view1.findViewById(R.id.PostIv);

        AlertDialog.Builder builder = new AlertDialog.Builder(PostDetailActivity.this);
        builder.setView(view1);
        final AlertDialog dialog = builder.create();
        dialog.show();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts");
        Query query1 = reference.orderByChild("postId").equalTo(postId);
        query1.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot1: dataSnapshot.getChildren()){
                    pImage1 = ""+snapshot1.child("postImage").getValue();

                    //load full image
                    try {
                        Picasso.get().load(pImage).into(postIv);
                    }catch (Exception e){
                        Toast.makeText(PostDetailActivity.this, "Image loading failed", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(PostDetailActivity.this, ""+databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void loadComments() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);

        commentList = new ArrayList<>();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts").child(postId).child("Comments");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                commentList.clear();
                for(DataSnapshot ds: dataSnapshot.getChildren()){
                    ModelComment modelComment = ds.getValue(ModelComment.class);

                    commentList.add(modelComment);

                    adapterComments = new AdapterComments(getApplicationContext(), commentList);
                    recyclerView.setAdapter(adapterComments);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void showMoreOptions() {

        PopupMenu popupMenu = new PopupMenu(this, moreBtn, Gravity.END);
        if(hisUid.equals(myUid)){
            popupMenu.getMenu().add(Menu.NONE, 0, 0, "Delete");
            popupMenu.getMenu().add(Menu.NONE, 1, 0, "Edit");
        }
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();
                if(id==0){
                    beginDelete();
                }
                else if(id==1){
                    Intent intent = new Intent(PostDetailActivity.this, AddPost.class);
                    intent.putExtra("key","editPost");
                    intent.putExtra("editPostId",postId);
                    startActivity(intent);
                }
                return false;
            }
        });
        popupMenu.show();
    }

    private void beginDelete() {
        if(pImage.equals("noImage")){
            deleteWithoutImage();
            finish();
        }else {
            deleteWithImage();
            finish();
        }
    }

    private void deleteWithImage(){
        final ProgressDialog dialog = new ProgressDialog(this);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setMessage("Deleting Post...");

        StorageReference reference = FirebaseStorage.getInstance().getReferenceFromUrl(pImage);
        reference.delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Query query = FirebaseDatabase.getInstance().getReference("Posts").orderByChild("postId").equalTo(postId);
                        query.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                for (DataSnapshot snapshot: dataSnapshot.getChildren()){
                                    snapshot.getRef().removeValue();
                                }
                                dialog.dismiss();
                                Toast.makeText(PostDetailActivity.this, "Post deleted successfully", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                Toast.makeText(PostDetailActivity.this, ""+databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                                dialog.dismiss();

                            }
                        });

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                dialog.dismiss();
                Toast.makeText(PostDetailActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteWithoutImage() {
        final ProgressDialog dialog = new ProgressDialog(this);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setMessage("Deleting Post...");

        Query query = FirebaseDatabase.getInstance().getReference("Posts").orderByChild("postId").equalTo(postId);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot: dataSnapshot.getChildren()){
                    snapshot.getRef().removeValue();
                }
                dialog.dismiss();
                Toast.makeText(PostDetailActivity.this, "Post deleted successfully", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(PostDetailActivity.this, ""+databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                dialog.dismiss();

            }
        });
    }

    private void postComment() {
        pd = new ProgressDialog(this);
        pd.setMessage("Adding Comment...");

        String comment = commentEt.getText().toString().trim();

        if(TextUtils.isEmpty(comment)){
            Toast.makeText(this, "Comment is empty", Toast.LENGTH_SHORT).show();
            return;
        }

        String timeStamp = String.valueOf(System.currentTimeMillis());
        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        calendar.setTimeInMillis(Long.parseLong(timeStamp));
        String cTime = DateFormat.format("dd/MM/yyyy hh:mm aa", calendar).toString();

        String cId = timeStamp + postId;

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts").child(postId).child("Comments");
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("commentId",cId);
        hashMap.put("comment",comment);
        hashMap.put("commentTime",cTime);
        hashMap.put("userId",myUid);
        hashMap.put("userDp",myDp);
        hashMap.put("userName",myName);

        reference.child(cId).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                pd.dismiss();
                Toast.makeText(PostDetailActivity.this, "Comment added successfully", Toast.LENGTH_SHORT).show();
                commentEt.setText("");
                updateCommentCount();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                pd.dismiss();
                Toast.makeText(PostDetailActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateCommentCount() {
        mProcessComment = true;
        final DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts").child(postId);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(mProcessComment){
                    String comments = ""+ dataSnapshot.child("postComments").getValue();
                    int newCommentVal = Integer.parseInt(comments) +1;
                    reference.child("postComments").setValue(""+newCommentVal);
                    mProcessComment = false;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void loadUserInfo() {
        Query myref = FirebaseDatabase.getInstance().getReference("Students");
        myref.orderByChild("uid").equalTo(myUid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot ds: dataSnapshot.getChildren()){
                    myName =""+ds.child("name").getValue();
                    myDp =""+ds.child("profileImage").getValue();
                    try{
                        Picasso.get().load(myDp).placeholder(R.drawable.holder).into(cAvatarIv);
                    }catch (Exception e){
                        Picasso.get().load(R.drawable.holder).into(cAvatarIv);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void loadPostInfo() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts");
        Query query = reference.orderByChild("postId").equalTo(postId);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot: dataSnapshot.getChildren()){
                    //get data
                    String pDescription = ""+snapshot.child("postDescription").getValue();
                    String pTimestamp = ""+snapshot.child("postTime").getValue();
                    pImage = ""+snapshot.child("postImage").getValue();
                    hisDp = ""+snapshot.child("userDp").getValue();
                    hisUid = ""+snapshot.child("userId").getValue();
                    hisName = ""+snapshot.child("userName").getValue();
                    String commentCount = ""+snapshot.child("postComments").getValue();


                    //set data
                    pDescriptionTv.setText(pDescription);
                    pTimeTv.setText(pTimestamp);
                    pCommentsTv.setText(commentCount+" Comments");

                    uNameTv.setText(hisName);

                    //set image of the user who posted
                    if(pImage.equals("noImage")){
                        pImageIv.setVisibility(View.GONE);
                    }else{
                        pImageIv.setVisibility(View.VISIBLE);
                        try {
                            Picasso.get().load(pImage).into(pImageIv);
                        }catch (Exception e){

                        }
                    }

                    //set user image in comment part
                    try{
                        Picasso.get().load(hisDp).placeholder(R.drawable.holder).into(uPictureIv);
                    }catch (Exception e){
                        Picasso.get().load(R.drawable.holder).into(uPictureIv);
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void checkUserStatus(){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user!=null){
            //user is signed in
            myEmail = user.getEmail();
            myUid = user.getUid();
        }else{
            startActivity(new Intent(this, StudentLogin.class));
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }
}
