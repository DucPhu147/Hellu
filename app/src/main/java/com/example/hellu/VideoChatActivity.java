package com.example.hellu;

import android.Manifest;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.hellu.Model.Calling;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.opentok.android.OpentokError;
import com.opentok.android.Publisher;
import com.opentok.android.PublisherKit;
import com.opentok.android.Session;
import com.opentok.android.Stream;
import com.opentok.android.Subscriber;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class VideoChatActivity extends AppCompatActivity implements Session.SessionListener, PublisherKit.PublisherListener {
    private static String API_Key="46882114";
    private static String SESSION_ID="1_MX40Njg4MjExNH5-MTU5NzIzMTEyOTY1MH5GYlJKaGtoVGpzcDQ0YmFtbGNrSTZwb3J-fg";
    private static String TOKEN="T1==cGFydG5lcl9pZD00Njg4MjExNCZzaWc9NDlmOGViZmViMjcwYjBlZjA5Njk4NGUxYWJkYWU4NjVjZjIwMGQxZjpzZXNzaW9uX2lkPTFfTVg0ME5qZzRNakV4Tkg1LU1UVTVOekl6TVRFeU9UWTFNSDVHWWxKS2FHdG9WR3B6Y0RRMFltRnRiR05yU1Rad2IzSi1mZyZjcmVhdGVfdGltZT0xNTk3MjMxMTY4Jm5vbmNlPTAuNzE0NDU4MDQzODUxMDcyNiZyb2xlPXB1Ymxpc2hlciZleHBpcmVfdGltZT0xNTk5ODIzMTU5JmluaXRpYWxfbGF5b3V0X2NsYXNzX2xpc3Q9";
    private static final String LOG_TAG=VideoChatActivity.class.getSimpleName();
    private static final int RC_VIDEO_APP_PERM=124;
    Calling call;
    ImageButton btnCancelCall;
    FirebaseUser firebaseUser;
    FrameLayout mainFrame,subFrame;
    Session session;
    Publisher publisher;
    Subscriber subscriber;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_chat);
        btnCancelCall=findViewById(R.id.btnCancelCall);
        call=(Calling)getIntent().getSerializableExtra("callModel");
        firebaseUser= FirebaseAuth.getInstance().getCurrentUser();
        btnCancelCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatabaseReference reference= FirebaseDatabase.getInstance().getReference("Users");
                reference.child(call.getReceiver()).child("Calling").removeValue();
                reference.child(call.getSender()).child("Calling").removeValue();
            }
        }); DatabaseReference reference=FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(!snapshot.hasChild("Calling")) {
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        requestPermissions();
    }
    @Override
    protected void onStop() {
        DatabaseReference reference= FirebaseDatabase.getInstance().getReference("Users");
        reference.child(call.getReceiver()).child("Calling").removeValue();
        reference.child(call.getSender()).child("Calling").removeValue();
        if(publisher!=null)
            publisher.destroy();
        if(subscriber!=null)
            subscriber.destroy();
        super.onStop();
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode,permissions,grantResults,VideoChatActivity.this);

    }

    @AfterPermissionGranted(RC_VIDEO_APP_PERM)
    private void requestPermissions(){
        String[] permissions={Manifest.permission.INTERNET,Manifest.permission.CAMERA,Manifest.permission.RECORD_AUDIO};
        if(EasyPermissions.hasPermissions(this,permissions)){
            mainFrame=findViewById(R.id.main_container);
            subFrame=findViewById(R.id.sub_container);
            session=new Session.Builder(this,API_Key,SESSION_ID).build();
            session.setSessionListener(VideoChatActivity.this);

            session.connect(TOKEN);
        }else{
            EasyPermissions.requestPermissions(this,"Camera and mic permission needed...",RC_VIDEO_APP_PERM,permissions);
        }
    }

    @Override
    public void onStreamCreated(PublisherKit publisherKit, Stream stream) {

    }

    @Override
    public void onStreamDestroyed(PublisherKit publisherKit, Stream stream) {

    }

    @Override
    public void onError(PublisherKit publisherKit, OpentokError opentokError) {

    }

    @Override
    public void onConnected(Session session) {
        Log.i(LOG_TAG,"Session connected");
        publisher=new Publisher.Builder(this).build();
        publisher.setPublisherListener(VideoChatActivity.this);
        subFrame.addView(publisher.getView());
        if(publisher.getView() instanceof GLSurfaceView){
            ((GLSurfaceView) publisher.getView()).setZOrderOnTop(true);
        }

        session.publish(publisher);
    }

    @Override
    public void onDisconnected(Session session) {
        Log.i(LOG_TAG,"Stream disconnected");
    }

    @Override
    public void onStreamReceived(Session session, Stream stream) {
        Log.i(LOG_TAG,"Stream Received");
        if(subscriber==null){
            subscriber=new Subscriber.Builder(this,stream).build();
            session.subscribe(subscriber);
            mainFrame.addView(subscriber.getView());
        }
    }

    @Override
    public void onStreamDropped(Session session, Stream stream) {

        Log.i(LOG_TAG,"Stream dropped");
        if(subscriber!=null){
            subscriber=null;
            mainFrame.removeAllViews();
        }
    }

    @Override
    public void onError(Session session, OpentokError opentokError) {

        Log.i(LOG_TAG,"Stream error");
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }
}