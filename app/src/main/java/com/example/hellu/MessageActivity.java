package com.example.hellu;

import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.hellu.Adapter.MessageAdapter;
import com.example.hellu.Class.UploadFileToFirebase;
import com.example.hellu.MessageNotification.APIService;
import com.example.hellu.MessageNotification.Client;
import com.example.hellu.MessageNotification.Data;
import com.example.hellu.MessageNotification.MyResponse;
import com.example.hellu.MessageNotification.Sender;
import com.example.hellu.MessageNotification.Token;
import com.example.hellu.Model.Group;
import com.example.hellu.Model.Message;
import com.example.hellu.Model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.r0adkll.slidr.Slidr;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import hani.momanii.supernova_emoji_library.Actions.EmojIconActions;
import hani.momanii.supernova_emoji_library.Helper.EmojiconEditText;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MessageActivity extends AppCompatActivity {
    CircleImageView imgViewUserImage;
    TextView txtUserName, txtSubText;
    ConstraintLayout fileWrapper;
    View rootView;
    ImageView imgFromGallery, removeMediaFromGallery;
    EmojiconEditText editTextMessage;
    ImageButton btnSend, btnOpenGallery, btnEmoji;
    DatabaseReference reference;
    FirebaseUser firebaseUser;
    MessageAdapter messageAdapter;
    List<Message> list;
    RecyclerView recyclerView;
    View viewUserStatus;
    ValueEventListener seenListener;
    int IS_DESTROY = 0;
    String chatID, chatUserName, path;
    boolean isCurrentTypeIsGroup = false;
    Uri fileUri;
    String fileUrl;
    User myCurrentUser;
    APIService apiService;
    boolean isNotify = false;
    Bitmap fileBitmap;
    boolean isPermGranted;
    User currentChatUser;
    Group currentChatGroup;
    VideoView videoFromGallery;
    private final static int IMAGE_REQUEST = 1, CAMERA_REQUEST = 2,VIDEO_REQUEST=3, PERM_REQUEST =123;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setElevation(3);

        //vuốt activity sang phải để đóng
        Slidr.attach(this);

        //
        recyclerView = findViewById(R.id.messageRecycleView);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        rootView = findViewById(R.id.rootView);
        imgViewUserImage = findViewById(R.id.userMessage_image);
        txtUserName = findViewById(R.id.userMessage_name);
        txtSubText = findViewById(R.id.userMessage_subText);
        editTextMessage = findViewById(R.id.editMessage);
        btnOpenGallery = findViewById(R.id.btnOpenGallery);
        fileWrapper = findViewById(R.id.imgFromGalleryWrapper);
        imgFromGallery = findViewById(R.id.imgFromGallery);
        videoFromGallery=findViewById(R.id.videoFromGallery);
        removeMediaFromGallery = findViewById(R.id.removeMediaFromGallery);
        btnSend = findViewById(R.id.btnSendMessage);
        btnEmoji = findViewById(R.id.btnEmoji);
        viewUserStatus = findViewById(R.id.userMessage_status);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference ref2 = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
        ref2.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                myCurrentUser = snapshot.getValue(User.class);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

        chatID = getIntent().getStringExtra("id");

        if (chatID.contains("Group")) {
            isCurrentTypeIsGroup = true;
            path = chatID;
        }
        else {
            isCurrentTypeIsGroup = false;
            //id của mình luôn nằm vế trái
            if (firebaseUser.getUid().compareTo(chatID) > 0) //nếu chuỗi đầu tiên lớn hơn chuỗi thứ 2
                path = firebaseUser.getUid() + "|" + chatID;
            else                                           //nếu chuỗi đầu tiên bằng hoặc nhỏ hơn chuỗi thứ 2
                path = chatID + "|" + firebaseUser.getUid();
        }

        list = new ArrayList<>();
        messageAdapter = new MessageAdapter(MessageActivity.this, list, path);
        recyclerView.setAdapter(messageAdapter);

        if (!isCurrentTypeIsGroup)
            reference = FirebaseDatabase.getInstance().getReference("Users").child(chatID);
        else
            reference = FirebaseDatabase.getInstance().getReference("Groups").child(chatID);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    if (!isCurrentTypeIsGroup) { //nếu type là user
                        currentChatUser = dataSnapshot.getValue(User.class);
                        txtUserName.setText(currentChatUser.getUsername());
                        if (currentChatUser.getStatus().equals("offline")) {
                            viewUserStatus.setVisibility(View.INVISIBLE);
                            long lastOnline = currentChatUser.getLastonline();
                            long timeOffline = System.currentTimeMillis() - lastOnline;
                            long minuteOffline = timeOffline / 1000 / 60;
                            if (minuteOffline < 60) {//Hoạt động vào 1-> 59 phút phút trước
                                if (minuteOffline == 0)
                                    minuteOffline += 1;
                                txtSubText.setText("Hoạt động " + minuteOffline + " phút trước");
                            } else if (minuteOffline >= 60 && minuteOffline < 1440)//Hoạt động vào 1-> 23 giờ trước
                                txtSubText.setText("Hoạt động " + minuteOffline / 60 + " giờ trước");
                            else if (minuteOffline >= 1440 && minuteOffline < 11520)//Hoạt động vào 1-> 7 ngày trước
                                txtSubText.setText("Hoạt động " + minuteOffline / 1440 + " ngày trước");
                            else {
                                Date thisItemDate = new Date(lastOnline);
                                SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM, yyyy");
                                txtSubText.setText("Hoạt động vào " + dateFormat.format(thisItemDate));
                            }
                        } else if (currentChatUser.getStatus().equals("online")) {
                            viewUserStatus.setVisibility(View.VISIBLE);
                            txtSubText.setText("Đang hoạt động");
                        }
                        if (IS_DESTROY == 0) {
                            if (currentChatUser.getImageURL().equals("default"))
                                imgViewUserImage.setImageResource(R.mipmap.ic_launcher_round);
                            else
                                Glide.with(MessageActivity.this).load(currentChatUser.getImageURL()).into(imgViewUserImage);
                        }
                    } else { //nếu type là group
                        currentChatGroup = dataSnapshot.getValue(Group.class);
                        txtUserName.setText(currentChatGroup.getName());
                        viewUserStatus.setVisibility(View.GONE);
                        txtSubText.setText(currentChatGroup.getMember().split(",").length + " thành viên");
                        if (IS_DESTROY == 0) {
                            if (currentChatGroup.getImageURL().equals("default"))
                                imgViewUserImage.setImageResource(R.mipmap.ic_launcher_round);
                            else
                                Glide.with(MessageActivity.this).load(currentChatGroup.getImageURL()).into(imgViewUserImage);
                        }
                    }
                }else{
                    finish();
                    //Toast.makeText(MessageActivity.this,"Nhóm không tồn tại",Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        readMessages();
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isNotify = true;
                String msg = editTextMessage.getText().toString();
                //sender , receiver, message
                sendMessage(firebaseUser.getUid(), chatID, msg.trim());
                editTextMessage.setText("");
            }
        });
        setSeenMessage();
        //xin permission từ hệ thống
        String[] permissions={Manifest.permission.CAMERA,Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE};
        if(EasyPermissions.hasPermissions(this,permissions))
            isPermGranted=true;
        else
            isPermGranted=false;
        //khi bấm vào nút mở file
        btnOpenGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //hiện menu
                PopupMenu popup = new PopupMenu(MessageActivity.this, view);
                popup.getMenuInflater().inflate(R.menu.get_file_menu, popup.getMenu());
                popup.show();
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        //nếu chọn ảnh thì mở thư viện ảnh
                        if (item.getItemId() == R.id.gallery) {
                            if(isPermGranted) {
                                Intent intent = new Intent();
                                intent.setType("image/*");
                                intent.setAction(Intent.ACTION_GET_CONTENT);
                                startActivityForResult(intent, IMAGE_REQUEST);
                            }
                            else
                                requestPermissions();
                            //nếu chọn ảnh chụp từ camera thì mở camera
                        } else if (item.getItemId() == R.id.camera) {
                            if(isPermGranted) {
                                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                                startActivityForResult(intent, CAMERA_REQUEST);
                            }else
                                requestPermissions();
                            //nếu chọn video thì mở thư viện video
                        } else if (item.getItemId() == R.id.video) {
                            if(isPermGranted) {
                                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                                intent.setType("video/*");
                                startActivityForResult(intent, VIDEO_REQUEST);
                            }else
                                requestPermissions();
                        }
                        return true;
                    }
                });
            }
        });
        //nếu bấm vào dấu X thì tắt giao diện ảnh, video đi và hiện lại edit text như bth
        removeMediaFromGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editTextMessage.setVisibility(View.VISIBLE);
                fileWrapper.setVisibility(View.GONE);
            }
        });
        //gán vàn phím emoji vào imgabe button btnEmoji (Gán phát khi bấm sẽ hiện bàn phím toàn emoji)
        EmojIconActions emojIcon = new EmojIconActions(this, rootView, editTextMessage, btnEmoji);
        emojIcon.setIconsIds(R.drawable.ic_round_keyboard_24, R.drawable.ic_baseline_insert_emoticon_24);
        emojIcon.ShowEmojIcon();

        //api dành cho notification
        apiService = Client.getClient("https://fcm.googleapis.com/").create(APIService.class);
    }

    //sự kiện khi mình click vào các nút menu trên thanh toolbar
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId()==R.id.call){
            //video chat
            //sendCallRequest();
        }else if(item.getItemId()==R.id.info){
            Intent intent;
            if(!isCurrentTypeIsGroup) {
                intent = new Intent(MessageActivity.this, UserProfileActivity.class);
                intent.putExtra("id", chatID);
            }
            else {
                intent = new Intent(MessageActivity.this, GroupDetailActivity.class);
                List<String> memberList= Arrays.asList(currentChatGroup.getMember().split(","));
                intent.putExtra("groupObjectForUpdate", (Serializable) currentChatGroup);
                //intent.putExtra("action", "update");
                intent.putExtra("listMember", (Serializable) memberList);
                intent.putExtra("currentUser",myCurrentUser);
            }
            startActivity(intent);
        }else
            onBackPressed();
        return super.onOptionsItemSelected(item);
    }

    //cập nhật trạng thái "đã xem" dòng chat
    private void setSeenMessage() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid()).child("username");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                chatUserName = dataSnapshot.getValue(String.class);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        reference = FirebaseDatabase.getInstance().getReference("Messages").child(path);
        seenListener = reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Message message = snapshot.getValue(Message.class);
                    if (!message.getSender().equals(firebaseUser.getUid())) {
                        //nếu dòng chat đó không phải mình gửi thì sẽ seen nó
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("seen", chatUserName + ",");
                        snapshot.getRef().updateChildren(hashMap);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    //đọc message từ firebase và gán lên recyclerview
    private void readMessages() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Messages").child(path);
        ref.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Message c = dataSnapshot.getValue(Message.class);
                list.add(c);
                recyclerView.smoothScrollToPosition(list.size());
                messageAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void sendMessage(final String sender, final String receiver, final String message) {
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender", sender);
        hashMap.put("seen", "no");
        hashMap.put("timestamp", ServerValue.TIMESTAMP); //lấy thời gian online bằng firebase
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Messages").child(path).push();
        hashMap.put("id", ref.getKey());
        if(fileWrapper.getVisibility()==View.VISIBLE) {
            if (videoFromGallery.getVisibility() == View.VISIBLE) { //nếu định gửi video
                hashMap.put("type", "video");
                fileWrapper.setVisibility(View.GONE);
                uploadImage(ref, hashMap);
            } else if (imgFromGallery.getVisibility() == View.VISIBLE) { //nếu định gửi video
                hashMap.put("type", "image");
                fileWrapper.setVisibility(View.GONE);
                uploadImage(ref, hashMap);
            }
        }else {
            if (!message.equals("")) {
                hashMap.put("message", message);
                hashMap.put("type", "text");
                ref.setValue(hashMap);
            }
        }

        //Tạo cuộc trò chuyện giữa 2 người
        if (!isCurrentTypeIsGroup) {
            DatabaseReference chatRef = FirebaseDatabase.getInstance().getReference("ChatIDList")
                    .child(receiver) //ID người nhận
                    .child(sender);   //ID của mình

            chatRef.child("id").setValue(sender);

            DatabaseReference chatRef2 = FirebaseDatabase.getInstance().getReference("ChatIDList")
                    .child(sender) //ID của mình
                    .child(receiver);   //ID người nhận
            chatRef2.child("id").setValue(receiver);
        }
        else{
            if(currentChatGroup!=null) {
                List<String> memberList = Arrays.asList(currentChatGroup.getMember().split(","));
                for (int i = 0; i < memberList.size(); i++) {
                    DatabaseReference chatRef = FirebaseDatabase.getInstance().getReference("ChatIDList")
                            .child(memberList.get(i)) //ID của member
                            .child(receiver);   //ID group
                    chatRef.child("id").setValue(receiver);
                }
            }
        }
        //gửi notify
        if (isNotify)
            sendNotification(receiver, message);
        isNotify = false;
    }
    //  Gửi notify
    private void sendNotification(final String receiver, final String msg) {
        DatabaseReference tokens = FirebaseDatabase.getInstance().getReference("Tokens");
        Query query = tokens.orderByKey().equalTo(receiver);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Token token = dataSnapshot.getValue(Token.class);
                    Data data;
                    if(!isCurrentTypeIsGroup)
                        data = new Data(myCurrentUser.getId(), msg, myCurrentUser.getUsername(), receiver, myCurrentUser.getImageURL());
                    else
                        data = new Data(currentChatGroup.getId(), msg, myCurrentUser.getUsername(), receiver, myCurrentUser.getImageURL());
                    Sender sender = new Sender(data, token.getToken());
                    apiService.sendNotification(sender)
                            .enqueue(new Callback<MyResponse>() {
                                @Override
                                public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                                    if (response.code() == 200) {
                                        if (response.body().success != 1) {
                                            Toast.makeText(MessageActivity.this, "Send notify failed!", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }

                                @Override
                                public void onFailure(Call<MyResponse> call, Throwable t) {
                                    Toast.makeText(MessageActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        reference.removeEventListener(seenListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        IS_DESTROY = 1;
    }
    boolean isFileUri =false;
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if ((requestCode == IMAGE_REQUEST||requestCode==VIDEO_REQUEST) && resultCode == RESULT_OK && data != null && data.getData() != null) {
            fileUri = data.getData();
            editTextMessage.setVisibility(View.GONE);
            fileWrapper.setVisibility(View.VISIBLE);
            if(requestCode==IMAGE_REQUEST) {
                imgFromGallery.setImageURI(fileUri);
                videoFromGallery.setVisibility(View.GONE);
            }else{
                imgFromGallery.setVisibility(View.GONE);
                try {
                    videoFromGallery.setVideoURI(fileUri);
                    //preview video vừa lấy
                    videoFromGallery.seekTo(1);
                }catch (Exception ex){
                    Toast.makeText(MessageActivity.this,ex.getMessage(),Toast.LENGTH_SHORT).show();
                }
            }
            isFileUri =true;
        } else if (requestCode == CAMERA_REQUEST && resultCode == RESULT_OK) {
            fileBitmap = (Bitmap) data.getExtras().get("data");
            editTextMessage.setVisibility(View.GONE);
            fileWrapper.setVisibility(View.VISIBLE);
            imgFromGallery.setImageBitmap(fileBitmap);
            isFileUri =false;
        }
    }

    //tải image lên firebase
    private void uploadImage(final DatabaseReference ref, final HashMap<String, Object> hashMap) {
        editTextMessage.setVisibility(View.VISIBLE);
        UploadFileToFirebase uploadFileToFirebase;
        if(isFileUri)//nếu ảnh được chọn từ thư viện (ảnh sẽ là 1 uri)
            uploadFileToFirebase=new UploadFileToFirebase(MessageActivity.this, fileUri);
        else//nếu ảnh vừa được chụp từ camera (ảnh sẽ là bitmap)
            uploadFileToFirebase=new UploadFileToFirebase(MessageActivity.this, fileBitmap);
        Toast.makeText(MessageActivity.this, "Đang gửi ảnh...", Toast.LENGTH_SHORT).show();
        Task<Uri> uploadTask=uploadFileToFirebase.uploadImage();
        uploadTask.addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    Uri downloadUri = task.getResult();
                    fileUrl = downloadUri.toString();
                    hashMap.put("message", fileUrl);
                    ref.setValue(hashMap);
                    Toast.makeText(MessageActivity.this, "Gửi tệp tin thành công!", Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(MessageActivity.this,task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if(!isCurrentTypeIsGroup) {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.user_chat_menu, menu);
        }else{
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.group_chat_menu, menu);
        }
        return super.onCreateOptionsMenu(menu);
    }
    //gửi yêu cầu video call
    /*void sendCallRequest(){
        final DatabaseReference reference=FirebaseDatabase.getInstance().getReference("Users");
        reference.child(chatID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //Kiểm tra xem đối phương có đang gọi hoặc nghe đt từ ai ko
                if(!snapshot.hasChild("Calling")){
                    //nếu ko thì tiến hành thêm cuộc gọi vào user của mình
                    HashMap<String, Object> hashMap = new HashMap<>();
                    hashMap.put("sender", firebaseUser.getUid());
                    hashMap.put("receiver", currentChatUser.getId());
                    hashMap.put("name", currentChatUser.getUsername());
                    hashMap.put("status", "ringing");
                    hashMap.put("imageURL", currentChatUser.getImageURL());
                    reference.child(firebaseUser.getUid()).child("Calling").updateChildren(hashMap);
                    reference.child(firebaseUser.getUid()).child("Calling").updateChildren(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()) {
                                reference.child(firebaseUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        //thêm calling vào user của đối phương
                                        HashMap<String, Object> hashMap2 = new HashMap<>();
                                        hashMap2.put("sender", firebaseUser.getUid());
                                        hashMap2.put("receiver", currentChatUser.getId());
                                        hashMap2.put("name", myCurrentUser.getUsername());
                                        hashMap2.put("status", "ringing");
                                        hashMap2.put("imageURL", myCurrentUser.getImageURL());
                                        reference.child(currentChatUser.getId()).child("Calling").updateChildren(hashMap2);
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                            }
                        }
                    });
                }else{
                    Toast.makeText(MessageActivity.this,"Người này hiện đang trò chuyện với người khác",Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }*/


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        //xin permission bằng easy permission
        EasyPermissions.onRequestPermissionsResult(requestCode,permissions,grantResults,MessageActivity.this);
    }

    //xin permission bằng easy permission
    @AfterPermissionGranted(PERM_REQUEST)
    private void requestPermissions(){
        String[] permissions={Manifest.permission.CAMERA,Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE};
        if(EasyPermissions.hasPermissions(this,permissions)){
            isPermGranted=true;
        }else{
            EasyPermissions.requestPermissions(this,"Read storage permission is needed...", PERM_REQUEST,permissions);
        }
    }
}
