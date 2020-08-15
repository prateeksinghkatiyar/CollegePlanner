package com.example.collegeplanner;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

public class AdapterPosts extends RecyclerView.Adapter<AdapterPosts.MyHolder>{

    Context context;
    List<Posts> postList;

    String myUid;
    String editDescription, editImage;

    public AdapterPosts(Context context, List<Posts> postList) {
        this.context = context;
        this.postList = postList;
        myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.content_home, parent, false) ;
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyHolder holder, int position) {

        final String uid = postList.get(position).getUserId();
        String uName = postList.get(position).getUserName();
        String uImage = postList.get(position).getUserDp();
        final String pId = postList.get(position).getPostId();
        final String pDescription = postList.get(position).getPostDescription();
        final String pImage = postList.get(position).getPostImage();
        String pTime = postList.get(position).getPostTime();
        String pComments = postList.get(position).getPostComments();

        //user profile image
        try{
            Picasso.get().load(uImage).placeholder(R.drawable.holder).into(holder.userPic);
        }
        catch (Exception e){
            Picasso.get().load(R.drawable.holder).into(holder.userPic);
        }

        //post image
        if(pImage.equals("noImage")){
            holder.postPic.setVisibility(View.GONE);
        }else{
            holder.postPic.setVisibility(View.VISIBLE);
            try {
                Picasso.get().load(pImage).into(holder.postPic);
            }catch (Exception e){

            }
        }

        //set data
        holder.userName.setText(uName);
        holder.postTime.setText(pTime);
        holder.postDescription.setText(pDescription);
        holder.pCommentsTv.setText(pComments + " Comments");

        //button click
        holder.moreBtn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMoreOptions(holder.moreBtn1, uid, myUid, pId, pImage);
            }
        });

        holder.commentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // start postdetailactivity
                Intent intent = new Intent(context, PostDetailActivity.class);
                intent.putExtra("postId",pId); // will get detail of post using this id, its id of the post clicked
                context.startActivity(intent);
            }
        });

        holder.shareBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BitmapDrawable bitmapDrawable = (BitmapDrawable)holder.postPic.getDrawable();
                if(bitmapDrawable==null){
                    shareTextOnly(pDescription);
                }else{
                    Bitmap bitmap = bitmapDrawable.getBitmap();
                    shareTextandImage(pDescription, bitmap);
                }
            }
        });

        holder.postPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // start postdetailactivity
                Intent intent = new Intent(context, PostDetailActivity.class);
                intent.putExtra("postId",pId); // will get detail of post using this id, its id of the post clicked
                context.startActivity(intent);
            }
        });

    }

    private void shareTextandImage(String pDescription, Bitmap bitmap) {
        String shareBody = pDescription;
        Uri uri = saveImageToShare(bitmap);

        Intent sIntent = new Intent(Intent.ACTION_SEND);
        sIntent.setType("image/jpeg");
        sIntent.putExtra(Intent.EXTRA_TEXT, shareBody);
        sIntent.putExtra(Intent.EXTRA_STREAM, uri);//text to share
        sIntent.putExtra(Intent.EXTRA_SUBJECT, "Subject Here"); // via email
        context.startActivity(Intent.createChooser(sIntent, "ShareVia"));
    }

    private Uri saveImageToShare(Bitmap bitmap) {
        File imageFolder = new File(context.getCacheDir(), "images");
        Uri uri = null;
        try {
            imageFolder.mkdirs();
            File file = new File(imageFolder, "shared_image.png");

            FileOutputStream stream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 30, stream);
            stream.flush();
            stream.close();
            uri = FileProvider.getUriForFile(context, "com.example.collegeplanner.fileprovider", file);
        } catch (Exception e) {
            Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        return uri;
    }

    private void shareTextOnly(String pDescription) {
        String shareBody = pDescription;

        Intent sIntent = new Intent(Intent.ACTION_SEND);
        sIntent.setType("text/plain");
        sIntent.putExtra(Intent.EXTRA_TEXT, shareBody); //text to share
        sIntent.putExtra(Intent.EXTRA_SUBJECT, "Subject Here"); // via email
        context.startActivity(Intent.createChooser(sIntent, "ShareVia"));
    }

    private void showMoreOptions(ImageButton moreBtn1, String uid, String myUid, final String pId, final String pImage) {
        PopupMenu popupMenu = new PopupMenu(context, moreBtn1, Gravity.END);

        if(uid.equals(myUid)){
            popupMenu.getMenu().add(Menu.NONE, 0 ,0, "Delete");
            popupMenu.getMenu().add(Menu.NONE, 1, 0, "Edit");
        }
        popupMenu.getMenu().add(Menu.NONE, 2, 0,"Detail");
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();
                if(id==0){
                    //delete is clicked
                    beginDelete(pId, pImage);
                }
                else if(id==1){
                    Intent intent = new Intent(context, AddPost.class);
                    intent.putExtra("key","editPost");
                    intent.putExtra("editPostId",pId);
                    context.startActivity(intent);
                }else if(id==2){
                    Intent intent = new Intent(context, PostDetailActivity.class);
                    intent.putExtra("postId",pId); // will get detail of post using this id, its id of the post clicked
                    context.startActivity(intent);
                }
                return false;
            }
        });
        popupMenu.show();
    }

    private void beginDelete(String pId, String pImage) {
        if(pImage.equals("noImage")){
            deleteWithoutImage(pId);
        }else {
            deleteWithImage(pId,pImage);
        }
    }

    private void deleteWithImage(final String pId, String pImage) {
        final ProgressDialog dialog = new ProgressDialog(context);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setMessage("Deleting Post...");

        StorageReference reference = FirebaseStorage.getInstance().getReferenceFromUrl(pImage);
        reference.delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Query query = FirebaseDatabase.getInstance().getReference("Posts").orderByChild("postId").equalTo(pId);
                        query.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                for (DataSnapshot snapshot: dataSnapshot.getChildren()){
                                    snapshot.getRef().removeValue();
                                }
                                dialog.dismiss();
                                Toast.makeText(context, "Post deleted successfully", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                Toast.makeText(context, ""+databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                                dialog.dismiss();

                            }
                        });

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                dialog.dismiss();
                Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteWithoutImage(String pId) {
        final ProgressDialog dialog = new ProgressDialog(context);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setMessage("Deleting Post...");

        Query query = FirebaseDatabase.getInstance().getReference("Posts").orderByChild("postId").equalTo(pId);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot: dataSnapshot.getChildren()){
                    snapshot.getRef().removeValue();
                }
                dialog.dismiss();
                Toast.makeText(context, "Post deleted successfully", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(context, ""+databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                dialog.dismiss();

            }
        });
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    class MyHolder extends RecyclerView.ViewHolder{

        ImageView userPic, postPic;
        TextView userName, postTime, postDescription, pCommentsTv;
        ImageButton moreBtn1;
        Button commentBtn, shareBtn;

        public MyHolder(@NonNull View itemView) {
            super(itemView);

            userPic = itemView.findViewById(R.id.img_userPic1);
            postPic = itemView.findViewById(R.id.img_postImage1);
            userName = itemView.findViewById(R.id.txt_userName1);
            postTime = itemView.findViewById(R.id.txt_postTime1);
            postDescription = itemView.findViewById(R.id.txt_postDescription1);
            pCommentsTv = itemView.findViewById(R.id.pCommentsTv);
            moreBtn1 = itemView.findViewById(R.id.btn_more1);
            commentBtn = itemView.findViewById(R.id.btn_comment1);
            shareBtn = itemView.findViewById(R.id.btn_share1);
        }
    }
}
