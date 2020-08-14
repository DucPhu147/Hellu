package com.example.hellu.MessageNotification;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

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

        //RemoteMessage.Notification notification=remoteMessage.getNotification();
        final int id=Integer.parseInt(sender.replaceAll("[\\D]",""));
        Intent intent=new Intent(this, MessageActivity.class);
        intent.putExtra("id",sender);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        PendingIntent pendingIntent=PendingIntent.getActivity(this,id,intent,PendingIntent.FLAG_ONE_SHOT);
        final NotificationCompat.Builder builder=new NotificationCompat.Builder(this,"my_channel_01")
                .setSmallIcon(R.drawable.ic_action_send_now)
                .setContentTitle(title)
                .setContentText(body)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setColor(getResources().getColor(R.color.colorPrimary));

        final NotificationManager notificationManager= (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        Glide.with(this)
                .asBitmap()
                .load(largeIcon)
                .into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        int j=0;
                        if(id>0)
                            j=id;
                        builder.setLargeIcon(resource);
                        notificationManager.notify(j,builder.build());
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {
                    }
                });

    }
}
