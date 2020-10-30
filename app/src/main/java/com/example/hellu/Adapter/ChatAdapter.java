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
import com.example.hellu.Model.Message;
import com.example.hellu.Model.User;
import com.example.hellu.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import hani.momanii.supernova_emoji_library.Helper.EmojiconTextView;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.Viewholder> {

    private Context context;
    private List<String> list;
    String lastMsg,senderID;
    long timeStamp;
    public int unreadCount=0;
    private boolean isTypeGroup;
    public ChatAdapter(Context context, List<String> obj) {
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
                Intent intent=new Intent(context, MessageActivity.class);
                intent.putExtra("id",id);
                context.startActivity(intent);
            }
        });
        DatabaseReference ref;
        if(!isTypeGroup) {
            ref = FirebaseDatabase.getInstance().getReference("Users").child(id);
            ref.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        User user = dataSnapshot.getValue(User.class);
                        holder.userName.setText(user.getUsername());
                        if (user.getImageURL().equals("default"))
                            holder.userImage.setImageResource(R.mipmap.ic_launcher_round);
                        else
                            Glide.with(context.getApplicationContext()).load(user.getImageURL()).into(holder.userImage);
                        if (user.getStatus().equals("offline"))
                            holder.userStatus.setVisibility(View.INVISIBLE);
                        else if (user.getStatus().equals("online"))
                            holder.userStatus.setVisibility(View.VISIBLE);
                    }
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
                    //nếu tồn tại group thì sẽ lấy thông tin
                    if (dataSnapshot.exists()) {
                        Group group = dataSnapshot.getValue(Group.class);
                        holder.userName.setText(group.getName());
                        if (group.getImageURL().equals("default"))
                            holder.userImage.setImageResource(R.mipmap.ic_launcher_round);
                        else
                            Glide.with(context.getApplicationContext()).load(group.getImageURL()).into(holder.userImage);
                    }//nếu group ko tồn tại sẽ tự động xóa khỏi chat ID list
                    else{
                        FirebaseUser firebaseUser=FirebaseAuth.getInstance().getCurrentUser();
                        DatabaseReference chatIDRef=FirebaseDatabase.getInstance().getReference("ChatIDList").child(firebaseUser.getUid());
                        chatIDRef.child(id).removeValue();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

        displayLastMessage(id,holder.userMsg,holder.userUnreadCount,holder.userTimeStamp);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class Viewholder extends RecyclerView.ViewHolder{
        public TextView userName,userUnreadCount,userTimeStamp;
        public EmojiconTextView userMsg;
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
            userTimeStamp=itemView.findViewById(R.id.userItem_timeStamp);
        }
    }
    String type;
    private void displayLastMessage(final String id, final TextView txtMsg, final TextView userUnreadCount,final TextView userTimeStamp){
        lastMsg="default";
        timeStamp=0;
        final FirebaseUser firebaseUser= FirebaseAuth.getInstance().getCurrentUser();
        //id của mình phải luôn luôn ở vế trái
        String path;
        if(!isTypeGroup) {
            if (firebaseUser.getUid().compareTo(id) > 0) //nếu chuỗi đầu tiên lớn hơn chuỗi thứ 2
                path = firebaseUser.getUid() + "|" + id;
            else                            //nếu chuỗi đầu tiên bằng hoặc nhỏ hơn chuỗi thứ 2
                path = id + "|" + firebaseUser.getUid();
        }else
            path=id;
        DatabaseReference reference= FirebaseDatabase.getInstance().getReference("Messages").child(path);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot:dataSnapshot.getChildren()) {
                    Message message = snapshot.getValue(Message.class);
                    type=message.getType();
                    lastMsg = message.getMessage();
                    timeStamp=message.getTimestamp();
                    senderID = message.getSender();
                    if (!message.getSender().equals(firebaseUser.getUid()))
                    {
                        if (message.getSeen().equals("no"))
                            unreadCount++;
                    }
                }
                if(!lastMsg.equals("default")) {
                    long time= timeStamp;
                    Date thisItemDate=new Date(time);
                    SimpleDateFormat hourFormat;
                    SimpleDateFormat dateFormat=new SimpleDateFormat("dd:MM:yyyy");
                    SimpleDateFormat yearFormat=new SimpleDateFormat("yyyy");
                    //Nếu message này được gửi trong cùng ngày
                    if(dateFormat.format(System.currentTimeMillis()).equals(dateFormat.format(time)))
                        hourFormat=new SimpleDateFormat("'Hôm nay' HH:mm");//17:05
                    //Nếu message gửi trong cùng năm
                    else if(yearFormat.format(System.currentTimeMillis()).equals(yearFormat.format(time)))
                        hourFormat=new SimpleDateFormat("dd MMM");//04 thg 7
                    //nếu message gửi khác năm
                    else
                        hourFormat=new SimpleDateFormat("dd MMM, yyyy");//04 thg 7, 2020
                    userTimeStamp.setText(hourFormat.format(thisItemDate));
                    userTimeStamp.setVisibility(View.VISIBLE);
                    if (senderID.equals(firebaseUser.getUid())) {
                        if(type.equals("text"))
                            txtMsg.setText("Bạn: " + lastMsg);
                        else if(type.equals("image"))
                            txtMsg.setText("Bạn đã gửi 1 ảnh");
                        else if(type.equals("video"))
                            txtMsg.setText("Bạn đã gửi 1 video");
                    }
                    else {
                        if (type.equals("text"))
                            txtMsg.setText(lastMsg);
                        else if (type.equals("image"))
                            txtMsg.setText("Đã gửi 1 ảnh");
                        else if (type.equals("video"))
                            txtMsg.setText("Đã gửi 1 video");
                    }
                }
                else{
                    txtMsg.setText("Các bạn đã được kết nối");
                }
                lastMsg="default";
                if(unreadCount==0){
                    userUnreadCount.setVisibility(View.INVISIBLE);
                }
                else{
                    userUnreadCount.setVisibility(View.VISIBLE);
                    userUnreadCount.setText(unreadCount+"");
                }
                unreadCount=0;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
