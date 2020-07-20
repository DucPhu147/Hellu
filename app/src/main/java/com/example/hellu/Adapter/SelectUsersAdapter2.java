package com.example.hellu.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.hellu.CreateGroupActivity;
import com.example.hellu.Model.User;
import com.example.hellu.R;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class SelectUsersAdapter2 extends RecyclerView.Adapter<SelectUsersAdapter2.Viewholder> {

    private Context context;
    private List<User> list;
    private CreateGroupActivity activity;
    public SelectUsersAdapter2(Context context, List<User> obj, CreateGroupActivity activity) {
        this.context=context;
        list=obj;
        this.activity=activity;
    }

    @NonNull
    @Override
    public Viewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(context).inflate(R.layout.user_item_selectuser2,parent,false);
        return new Viewholder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final Viewholder holder, final int position) {
        final User myUser=list.get(position);
        holder.userName.setText(myUser.getUsername());
        if(myUser.getImageURL().equals("default"))
            holder.userImage.setImageResource(R.mipmap.ic_launcher_round);
        else
            Glide.with(context).load(list.get(position).getImageURL()).into(holder.userImage);
        holder.removeUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                activity.removeItemFromList2(myUser);
            }
        });
    }
    @Override
    public int getItemCount() {
        return list.size();
    }
    public static class Viewholder extends RecyclerView.ViewHolder{
        public TextView userName;
        public CircleImageView userImage;
        public ImageView removeUser;
        public Viewholder(@NonNull View itemView) {
            super(itemView);
            userName=itemView.findViewById(R.id.selectUser2_userName);
            userImage=itemView.findViewById(R.id.selectUser2_userImage);
            removeUser=itemView.findViewById(R.id.selectUser2_removeUser);
        }
    }
}
