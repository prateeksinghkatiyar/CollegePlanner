package com.example.collegeplanner;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.List;

public class AdapterComments  extends RecyclerView.Adapter<AdapterComments.MyHolder>{

    Context context;
    List<ModelComment> commentList;

    public AdapterComments(Context context, List<ModelComment> commentList) {
        this.context = context;
        this.commentList = commentList;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_comment, parent, false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {

        String uid = commentList.get(position).getUserId();
        String name = commentList.get(position).getUserName();
        String image = commentList.get(position).getUserDp();
        String cid = commentList.get(position).getCommentId();
        String comment = commentList.get(position).getComment();
        String time = commentList.get(position).getCommentTime();

        //set data
        holder.nameTv.setText(name);
        holder.commentTv.setText(comment);
        holder.timeTv.setText(time);

        //user profile image
        try{
            Picasso.get().load(image).placeholder(R.drawable.holder).into(holder.avatarIv);
        }
        catch (Exception e){
            Picasso.get().load(R.drawable.holder).into(holder.avatarIv);
        }


    }

    @Override
    public int getItemCount() {
        return commentList.size();
    }

    class MyHolder extends RecyclerView.ViewHolder{

        ImageView avatarIv;
        TextView nameTv, commentTv, timeTv;


        public MyHolder(@NonNull View itemView) {
            super(itemView);

            avatarIv = itemView.findViewById(R.id.avatarIv3);
            nameTv = itemView.findViewById(R.id.nameTv3);
            commentTv = itemView.findViewById(R.id.commentTv3);
            timeTv = itemView.findViewById(R.id.timeTv3);
        }
    }
}
