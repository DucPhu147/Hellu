package com.example.hellu.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.hellu.MessageActivity;
import com.example.hellu.Model.Group;
import com.example.hellu.Model.User;
import com.example.hellu.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.Viewholder> {

    private Context context;
    private List<String> list;
    String lastMsg,senderID;
    public int unreadCount=0;
    private boolean isTypeGroup;
    public SearchAdapter(Context context, List<String> obj) {
        this.context=context;
        list=obj;
        setHasStableIds(true);
    }

    @NonNull
    @Override
    public Viewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(context).inflate(R.layout.user_item,parent,false);
        return new Viewholder(view);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        if(list.get(position).contains("Group"))
            isTypeGroup=true;
        else
            isTypeGroup=false;
        return position;
    }

    @Override
    public void onBindViewHolder(@NonNull final Viewholder holder, int position) {
        final String id=list.get(position);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Intent intent=new Intent(context, UserProfileActivity.class);
                Intent intent=new Intent(context, MessageActivity.class);
                intent.putExtra("id",id);
                context.startActivity(intent);
            }
        });
        DatabaseReference ref;
        if(!isTypeGroup) {
            ref = FirebaseDatabase.getInstance().getReference("Users").child(id);
            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    User user=dataSnapshot.getValue(User.class);
                    holder.userName.setText(user.getUsername());
                    if(user.getImageURL().equals("default"))
                        holder.userImage.setImageResource(R.mipmap.ic_launcher_round);
                    else
                        Glide.with(context.getApplicationContext()).load(user.getImageURL()).into(holder.userImage);
                    holder.userMsg.setText(user.getEmail());
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
        else {
            ref = FirebaseDatabase.getInstance().getReference("Groups").child(id);
            ref.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.exists()) {
                        Group group = dataSnapshot.getValue(Group.class);
                        holder.userName.setText(group.getName());
                        if (group.getImageURL().equals("default"))
                            holder.userImage.setImageResource(R.mipmap.ic_launcher_round);
                        else
                            Glide.with(context.getApplicationContext()).load(group.getImageURL()).into(holder.userImage);
                        holder.userMsg.setText(group.getMember().split(",").length + " thành viên");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class Viewholder extends RecyclerView.ViewHolder{
        public TextView userMsg,userName,userUnreadCount;
        public CircleImageView userImage;
        public View userStatus;
        public CardView cardView;
        public Viewholder(@NonNull View itemView) {
            super(itemView);
            userName=itemView.findViewById(R.id.userItem_name);
            userImage=itemView.findViewById(R.id.userItem_image);
            userMsg=itemView.findViewById(R.id.userItem_msg);
            cardView=itemView.findViewById(R.id.cardView);
            userStatus=itemView.findViewById(R.id.userItem_status);
            userUnreadCount=itemView.findViewById(R.id.userItem_unread);
        }
    }
}
