package com.example.hellu;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.hellu.Model.Calling;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class CallingActivity extends AppCompatActivity {
    ImageButton btnCancelCall, btnAcceptCall;
    FirebaseUser firebaseUser;
    CircleImageView callerImage;
    TextView txtCallingContent,txtAcceptCall,txtCancelCall;
    Calling call;
    boolean isCancelCall;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calling);
        btnCancelCall =findViewById(R.id.btnCancelCall);
        btnAcceptCall =findViewById(R.id.btnAcceptCall);
        callerImage=findViewById(R.id.callerImage);
        txtCallingContent=findViewById(R.id.txtCallingContent);
        txtAcceptCall=findViewById(R.id.txtAcceptCall);
        txtCancelCall=findViewById(R.id.txtCancelCall);
        isCancelCall=true;
        firebaseUser= FirebaseAuth.getInstance().getCurrentUser();
        call=(Calling)getIntent().getSerializableExtra("callModel");
        if(call.getReceiver().equals(firebaseUser.getUid())) {
            txtCallingContent.setText(call.getName() + " muốn video chat với bạn");
        }
        else {
            txtCallingContent.setText("Đang gọi cho " + call.getName());
            btnAcceptCall.setVisibility(View.GONE);
            txtAcceptCall.setVisibility(View.GONE);
        }
        if(!call.getImageURL().equals("default"))
            Glide.with(CallingActivity.this).load(call.getImageURL()).into(callerImage);
        else
            callerImage.setImageResource(R.mipmap.ic_launcher_round);
        btnCancelCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatabaseReference reference= FirebaseDatabase.getInstance().getReference("Users");
                reference.child(call.getReceiver()).child("Calling").removeValue();
                reference.child(call.getSender()).child("Calling").removeValue();
            }
        });
        btnAcceptCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatabaseReference reference= FirebaseDatabase.getInstance().getReference("Users");
                HashMap<String,Object> hashMap=new HashMap<>();
                hashMap.put("status","calling");
                reference.child(call.getReceiver()).child("Calling").updateChildren(hashMap);
                reference.child(call.getSender()).child("Calling").updateChildren(hashMap);
            }
        });
        DatabaseReference reference=FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(!snapshot.hasChild("Calling")) {
                    finish();
                }
                else{
                    Calling call=snapshot.child("Calling").getValue(Calling.class);
                    if(call.getStatus().equals("calling")){
                        isCancelCall=false;
                        Intent intent=new Intent(CallingActivity.this,VideoChatActivity.class);
                        intent.putExtra("callModel",call);
                        startActivity(intent);
                        finish();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    @Override
    protected void onDestroy() {
        if(isCancelCall) {
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
            reference.child(call.getReceiver()).child("Calling").removeValue();
            reference.child(call.getSender()).child("Calling").removeValue();
        }
        super.onDestroy();
    }


}