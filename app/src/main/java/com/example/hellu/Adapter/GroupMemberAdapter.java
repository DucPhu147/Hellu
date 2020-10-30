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
import com.example.hellu.Model.User;
import com.example.hellu.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class GroupMemberAdapter  extends RecyclerView.Adapter<GroupMemberAdapter.Viewholder> {

    private Context context;
    private List<String> list;

    public GroupMemberAdapter(Context context, List<String> obj) {
        this.context = context;
        list = obj;
    }

    @NonNull
    @Override
    public GroupMemberAdapter.Viewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.choose_user_item, parent, false);
        return new GroupMemberAdapter.Viewholder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final GroupMemberAdapter.Viewholder holder, final int position) {
        final String userID = list.get(position);
        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Users").child(userID);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    User myUser = snapshot.getValue(User.class);
                    holder.userName.setText(myUser.getUsername());

                    //holder.userMsg.setText(myUser.getEmail());
                    if (myUser.getImageURL().equals("default"))
                        holder.userImage.setImageResource(R.mipmap.ic_launcher_round);
                    else
                        Glide.with(context.getApplicationContext()).load(myUser.getImageURL()).into(holder.userImage);
                    holder.userSubText.setText(myUser.getEmail());
                    holder.usersCheckBox.setVisibility(View.GONE);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class Viewholder extends RecyclerView.ViewHolder {
        public TextView userSubText, userName;
        public CheckBox usersCheckBox;
        public CircleImageView userImage;

        public Viewholder(@NonNull View itemView) {
            super(itemView);
            userName = itemView.findViewById(R.id.selectUser_userName);
            userImage = itemView.findViewById(R.id.selectUser_userImage);
            userSubText = itemView.findViewById(R.id.selectUser_userSubText);
            usersCheckBox = itemView.findViewById(R.id.selectUser_userCheckBox);
        }
    }
}