package com.example.hellu.Adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.util.Pair;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.hellu.ImageActivity;
import com.example.hellu.Model.Message;
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

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.Viewholder> {
    private Context context;
    private boolean isViewRight;
    private FirebaseUser firebaseUser;
    private List<Message> list;
    private String pathToChats;
    public MessageAdapter(Context context, List<Message> list, String pathToChats){
        this.context=context;
        this.list=list;
        this.pathToChats=pathToChats;
        setHasStableIds(true);
    }
    @NonNull
    @Override
    public Viewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (!isViewRight) {
            view = LayoutInflater.from(context).inflate(R.layout.chat_item_left, parent, false);
        } else {
            view = LayoutInflater.from(context).inflate(R.layout.chat_item_right, parent, false);
        }
        return new Viewholder(view);
    }
    @Override
    public void onBindViewHolder(@NonNull final Viewholder holder, final int position) {

        final Message message =list.get(position);
        if(message.getType().equals("text")) {
            holder.txtMessage.setText(message.getMessage());
            holder.txtMessage.setVisibility(View.VISIBLE);
            holder.imgMsg.setVisibility(View.GONE);
        }else if(message.getType().equals("image")){
            Glide.with(context.getApplicationContext())
                    .load(message.getMessage())
                    .into(holder.imgMsg);
            holder.txtMessage.setVisibility(View.GONE);
            holder.imgMsg.setVisibility(View.VISIBLE);
            holder.msgWrapper.setBackgroundColor(Color.TRANSPARENT);
            holder.txtTimeStamp.setTextColor(context.getResources().getColor(R.color.colorBlackTransparent));
            holder.msgWrapper.setPadding(0,
                    holder.msgWrapper.getPaddingTop(),
                    0,
                    holder.msgWrapper.getPaddingBottom());
            holder.imgMsg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ActivityOptionsCompat optionsCompat;
                    optionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(
                            (Activity) context,
                            Pair.create((View) holder.imgMsg, ViewCompat.getTransitionName(holder.imgMsg)));
                    Intent intent=new Intent(context, ImageActivity.class);

                    intent.putExtra("imageURL",message.getMessage());
                    context.startActivity(intent,optionsCompat.toBundle());
                }
            });
        }
        //hiện thời gian
        long time= message.getTimestamp();
        Date thisItemDate=new Date(time);
        SimpleDateFormat dateFormat;
        SimpleDateFormat yearFormat=new SimpleDateFormat("yyyy");//năm
        if(yearFormat.format(System.currentTimeMillis()).equals(yearFormat.format(time)))
            dateFormat=new SimpleDateFormat("dd MMM");//thứ, ngày/tháng/năm
        else
            dateFormat=new SimpleDateFormat("dd MMM, yyyy");//thứ, ngày/tháng/năm
        holder.txtDate.setText(dateFormat.format(thisItemDate).toUpperCase());
        SimpleDateFormat hourFormat=new SimpleDateFormat("HH:mm");//17:05
        holder.txtTimeStamp.setText(hourFormat.format(thisItemDate));
        GradientDrawable drawable=new GradientDrawable();
        if(!message.getType().equals("image")) {
            drawable = (GradientDrawable) holder.msgWrapper.getBackground().mutate();
            if (isViewRight)//top-left,top-right,bottom-right,bottom-left
                drawable.setCornerRadii(new float[]{50, 50, 50, 50, 15, 15, 50, 50});
            else
                drawable.setCornerRadii(new float[]{50, 50, 50, 50, 50, 50, 15, 15});
        }
        if(position>0) {
            //Ẩn Hiện ngày của message
            Date previousItemDate = new Date(list.get(position - 1).getTimestamp());
            Date currentItemDate = new Date(list.get(position).getTimestamp());
            if (dateFormat.format(currentItemDate).equals(dateFormat.format(previousItemDate)))
                holder.dateWrapper.setVisibility(View.GONE);
            //tạo và xóa góc bo tròn của các item chat liền nhau
            if (!list.get(position).getType().equals("image")){
                if(list.get(position).getSender().equals(list.get(position-1).getSender())
                        &&holder.dateWrapper.getVisibility()==View.GONE) {
                    //mutate() để khi sửa background của 1 item thì các item dùng chung background đó không bị sửa theo
                    drawable = (GradientDrawable) holder.msgWrapper.getBackground().mutate();
                    if (isViewRight)//top-left,top-right,bottom-right,bottom-left
                        drawable.setCornerRadii(new float[]{50, 50, 15, 15, 15, 15, 50, 50});
                    else
                        drawable.setCornerRadii(new float[]{15, 15, 50, 50, 50, 50, 15, 15});
                }
                holder.msgWrapper.setBackground(drawable);
            }
            //tạo khoảng trắng giữa dòng chat của mình và dòng chat ng khác
            if (!list.get(position - 1).getSender().equals(list.get(position).getSender())) {
                //Nếu đã hiện ngày tháng bên trên message thì không tạo khoảng trắng nữa
                if (holder.dateWrapper.getVisibility() == View.GONE) {
                    RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) holder.msgWrapper.getLayoutParams();
                    if (isViewRight)
                        params.setMargins(200, 50, 5, 0);
                    else
                        params.setMargins(0, 50, 150, 0);
                    holder.msgWrapper.setLayoutParams(params);
                }
            }
        }
        //Ẩn hiện "Đã xem"
        if(position==list.size()-1) {
            if(!isViewRight){
                holder.txtSeen.setVisibility(View.GONE);
            }
            else{
                DatabaseReference ref=FirebaseDatabase.getInstance().getReference().child("Messages").child(pathToChats).child(message.getId()).child("seen");
                ref.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        String seen=dataSnapshot.getValue(String.class);
                        if(seen.equals("no")){
                            holder.txtSeen.setVisibility(View.VISIBLE);
                            holder.txtSeen.setText("Đã gửi");
                        }else{
                            String seen2="";
                            if(seen.split(",").length==1){
                                seen2=seen.split(",")[0]+" đã xem";
                            }else if(seen.split(",").length>1&&seen.split(",").length<3) {
                                seen2 += seen.split(",")[0]+", "+seen.split(",")[1]+" đã xem";
                            }else if(seen.split(",").length>=3){
                                seen2 +=seen.split(",")[0]+", "+seen.split(",")[1]+" và "+(seen.split(",").length-2)+" người khác đã xem";
                            }
                            if(position==list.size()-1) {
                                holder.txtSeen.setVisibility(View.VISIBLE);
                                holder.txtSeen.setText(seen2);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        }else{
            holder.txtSeen.setVisibility(View.GONE);
        }
         //Ẩn ảnh đại diện của họ khi các dòng chat của họ không bị xen ngang bởi dòng chat của mình
        if(!isViewRight) {
            if (position < getItemCount() - 1 && message.getSender().equals(list.get(position + 1).getSender())) {
                holder.guestImg.setVisibility(View.INVISIBLE);
                //holder.setIsRecyclable(false);
            } else {
                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users").child(message.getSender()).child("imageURL");
                ref.addValueEventListener(new ValueEventListener() {//chỉ đọc 1 lần (ko update đc như addValueEventListener)
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        String URL = dataSnapshot.getValue(String.class);
                        if (URL.equals("default"))
                            holder.guestImg.setImageResource(R.mipmap.ic_launcher_round);
                        else {
                            Glide.with(context.getApplicationContext()).load(URL).into(holder.guestImg);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        }

    }
    public class Viewholder extends RecyclerView.ViewHolder{

        public TextView txtSeen,txtTimeStamp,txtDate;
        public EmojiconTextView txtMessage;
        public LinearLayout dateWrapper,msgWrapper;
        public CircleImageView guestImg;
        public ImageView imgMsg;
        public Viewholder(@NonNull View itemView) {
            super(itemView);
            txtMessage =itemView.findViewById(R.id.userChat_Msg);
            imgMsg=itemView.findViewById(R.id.userChat_ImageMsg);
            guestImg=itemView.findViewById(R.id.userChat_Image);
            txtSeen=itemView.findViewById(R.id.userChat_isSeen);
            txtTimeStamp=itemView.findViewById(R.id.userChat_timeStamp);
            txtDate=itemView.findViewById(R.id.userChat_Date);
            dateWrapper=itemView.findViewById(R.id.dateWrapper);
            msgWrapper=itemView.findViewById(R.id.msgWrapper);
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        firebaseUser= FirebaseAuth.getInstance().getCurrentUser();
        //nếu id người gửi là id mình thì view sẽ là chat_item_right
        if(list.get(position).getSender().equals(firebaseUser.getUid()))
            isViewRight=true;
        else
            isViewRight=false;
        return position;
    }

    @Override
    public int getItemCount() {
        return list.size();
    }
}
