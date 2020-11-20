package com.example.hellu.MessageNotification;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.RemoteInput;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.hellu.MessageActivity;
import com.example.hellu.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MyFirebaseMessaging extends FirebaseMessagingService {
    private static final String KEY_TEXT_REPLY="reply_action_key";
    @Override
    public void onNewToken(String s) {
        super.onNewToken(s);
        String refreshToken = FirebaseInstanceId.getInstance().getInstanceId().getResult().getToken();
        if (refreshToken != null){
            updateToken(refreshToken);
        }
    }

    private void updateToken(String refreshToken) {
        if (FirebaseAuth.getInstance().getCurrentUser() != null){
            FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Tokens");
            Token token = new Token(refreshToken);
            reference.child(firebaseUser.getUid()).setValue(token);
        }
    }
    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        String idReceiver=remoteMessage.getData().get("receiver");//id người nhận
        FirebaseUser firebaseUser= FirebaseAuth.getInstance().getCurrentUser();
        if(firebaseUser!=null && idReceiver.equals(firebaseUser.getUid()))
        {
            sendNotification(remoteMessage);
        }
    }
    void sendNotification(RemoteMessage remoteMessage){
        String sender=remoteMessage.getData().get("sender"); //id người gửi tin nhắn
        String largeIcon=remoteMessage.getData().get("largeicon");
        String title=remoteMessage.getData().get("title");
        String body=remoteMessage.getData().get("body");


        final NotificationManager notificationManager= (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        //khi bấm vào notification sẽ chạy đến activity
        final int id=Integer.parseInt(sender.replaceAll("[\\D]",""));
        Intent intent=new Intent(this, MessageActivity.class);
        intent.putExtra("id",sender);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent=PendingIntent.getActivity(this,id,intent,PendingIntent.FLAG_ONE_SHOT);
        //

        //tạo nút trả lời trong notification
        RemoteInput remoteInput = new RemoteInput.Builder(KEY_TEXT_REPLY)
                .setLabel("Nội dung")
                .build();

        PendingIntent replyPendingIntent =
                PendingIntent.getBroadcast(getApplicationContext(),
                        id, intent,PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Action replyAction = new NotificationCompat.Action.Builder(R.drawable.ic_action_send_now,
                        "Trả lời", replyPendingIntent)
                        .addRemoteInput(remoteInput)
                        .build();
        //
        final NotificationCompat.Builder builder=new NotificationCompat.Builder(this,"my_channel_01")
                .setSmallIcon(R.drawable.ic_action_send_now)
                .setContentTitle(title)
                .setContentText(body)
                .setVibrate(new long[]{100, 250})
                .setPriority(Notification.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .setAutoCancel(true)
                .addAction(replyAction)
                .setContentIntent(pendingIntent)
                .setColor(getResources().getColor(R.color.colorPrimary));

        String channelId="my_channel01";
        String channelName = "my_channel_name";

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel mChannel = new NotificationChannel(
                    channelId, channelName, NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(mChannel);
        }
        else{
            builder.setPriority(Notification.PRIORITY_MAX);
        }
        int j=0;
        if(id>0)
            j=id;

        //nếu ảnh đại diện không là ảnh default
        if(!largeIcon.equals("default")) {
            final int finalJ = j;
            Glide.with(this)
                    .asBitmap()
                    .load(largeIcon)
                    .into(new CustomTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                            builder.setLargeIcon(resource);
                            notificationManager.notify(finalJ,builder.build());
                        }
                        @Override
                        public void onLoadCleared(@Nullable Drawable placeholder) {
                        }
                    });
        }
        //nếu là ảnh default
        else{
            builder.setLargeIcon(BitmapFactory.decodeResource(getResources(),R.mipmap.ic_launcher));
            notificationManager.notify(id,builder.build());
        }
    }
    //lấy câu trả lời từ replyAction
    private CharSequence getMessageText(Intent intent) {
        Bundle remoteInput = RemoteInput.getResultsFromIntent(intent);
        if (remoteInput != null) {
            return remoteInput.getCharSequence(KEY_TEXT_REPLY);
        }
        return null;
    }
}
