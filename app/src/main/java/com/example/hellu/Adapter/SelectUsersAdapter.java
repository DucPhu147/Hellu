package com.example.hellu.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.hellu.CreateGroupActivity;
import com.example.hellu.Model.User;
import com.example.hellu.R;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class SelectUsersAdapter extends RecyclerView.Adapter<SelectUsersAdapter.Viewholder> {

    private Context context;
    private List<User> list;
    private CreateGroupActivity activity;
    private CheckBox checkBox;
    public SelectUsersAdapter(Context context, List<User> obj, CreateGroupActivity activity) {
        this.context=context;
        list=obj;
        this.activity=activity;
    }

    @NonNull
    @Override
    public Viewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.user_item_selectuser, parent, false);
        return new Viewholder(view);
    }
    @Override
    public void onBindViewHolder(@NonNull final Viewholder holder, final int position) {
        final User myUser = list.get(position);
        holder.userName.setText(myUser.getUsername());
        checkBox = holder.usersCheckBox;

        //holder.userMsg.setText(myUser.getEmail());
        if (myUser.getImageURL().equals("default"))
            holder.userImage.setImageResource(R.mipmap.ic_launcher_round);
        else
            Glide.with(context).load(list.get(position).getImageURL()).into(holder.userImage);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (holder.usersCheckBox.isChecked()) {
                    holder.usersCheckBox.setChecked(false);
                    activity.removeItemFromList2(myUser);
                } else {
                    holder.usersCheckBox.setChecked(true);
                    activity.addItemToList2(myUser, position);
                }
            }
        });
        holder.userSubText.setText(myUser.getEmail());
    }
    @Override
    public int getItemCount() {
        return list.size();
    }
    public static class Viewholder extends RecyclerView.ViewHolder{
        public TextView userSubText,userName;
        public CheckBox usersCheckBox;
        public CircleImageView userImage;
        public Viewholder(@NonNull View itemView) {
            super(itemView);
            userName=itemView.findViewById(R.id.selectUser_userName);
            userImage=itemView.findViewById(R.id.selectUser_userImage);
            userSubText=itemView.findViewById(R.id.selectUser_userSubText);
            usersCheckBox=itemView.findViewById(R.id.selectUser_userCheckBox);
        }
    }
}
