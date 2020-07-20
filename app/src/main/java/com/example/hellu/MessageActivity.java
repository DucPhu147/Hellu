package com.example.hellu;

import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.hellu.Adapter.MessageAdapter;
import com.example.hellu.Model.Group;
import com.example.hellu.Model.Message;
import com.example.hellu.Model.User;
import com.example.hellu.Notification.APIService;
import com.example.hellu.Notification.Client;
import com.example.hellu.Notification.Data;
import com.example.hellu.Notification.MyResponse;
import com.example.hellu.Notification.Sender;
import com.example.hellu.Notification.Token;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.r0adkll.slidr.Slidr;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;
import hani.momanii.supernova_emoji_library.Actions.EmojIconActions;
import hani.momanii.supernova_emoji_library.Helper.EmojiconEditText;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MessageActivity extends AppCompatActivity {
    CircleImageView imgViewUserImage;
    TextView txtUserName, txtSubText;
    ConstraintLayout imgFromGalleryWrapper;
    View rootView;
    ImageView imgFromGallery, removeImgFromGallery;
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
    String userID, chatUserName, path;
    boolean isCurrentTypeIsGroup = false;
    Uri imageUri;
    private StorageTask<UploadTask.TaskSnapshot> uploadTask;
    StorageReference storageReference;
    String imgURL;
    String currentID;
    APIService apiService;
    boolean isNotify = false;
    Bitmap imageBitmap;
    byte[] imageData;
    private final static int IMAGE_REQUEST = 1, CAMERA_REQUEST = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setElevation(8);

        Slidr.attach(this);
        //getWindow().setSharedElementEnterTransition(TransitionInflater.from(this).inflateTransition(R.transition.shared_element_transition));
        //overridePendingTransition(R.anim.enter_anim,R.anim.exit_anim);

        apiService = Client.getClient("https://fcm.googleapis.com/").create(APIService.class);

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
        imgFromGalleryWrapper = findViewById(R.id.imgFromGalleryWrapper);
        imgFromGallery = findViewById(R.id.imgFromGallery);
        removeImgFromGallery = findViewById(R.id.removeImgFromGallery);
        btnSend = findViewById(R.id.btnSendMessage);
        btnEmoji = findViewById(R.id.btnEmoji);
        viewUserStatus = findViewById(R.id.userMessage_status);


        storageReference = FirebaseStorage.getInstance().getReference("MessageImages");

        userID = getIntent().getStringExtra("id");
        if (userID.contains("Group"))
            isCurrentTypeIsGroup = true;
        else
            isCurrentTypeIsGroup = false;
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (!isCurrentTypeIsGroup) {
            //id của mình luôn nằm vế trái
            if (firebaseUser.getUid().compareTo(userID) > 0) //nếu chuỗi đầu tiên lớn hơn chuỗi thứ 2
                path = firebaseUser.getUid() + "|" + userID;
            else                                           //nếu chuỗi đầu tiên bằng hoặc nhỏ hơn chuỗi thứ 2
                path = userID + "|" + firebaseUser.getUid();
        } else
            path = userID;
        list = new ArrayList<>();
        messageAdapter = new MessageAdapter(MessageActivity.this, list, path);
        recyclerView.setAdapter(messageAdapter);

        if (!isCurrentTypeIsGroup)
            reference = FirebaseDatabase.getInstance().getReference("Users").child(userID);
        else
            reference = FirebaseDatabase.getInstance().getReference("Groups").child(userID);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!isCurrentTypeIsGroup) { //nếu type là user
                    User user = dataSnapshot.getValue(User.class);
                    txtUserName.setText(user.getUsername());
                    if (user.getStatus().equals("offline")) {
                        viewUserStatus.setVisibility(View.INVISIBLE);
                        long time = user.getLastseen();
                        if (time > 0) {
                            long time2 = System.currentTimeMillis();
                            Date thisItemDate = new Date(time);
                            SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd/MM/yyyy");//thứ, ngày/tháng/năm
                            SimpleDateFormat hourFormat = new SimpleDateFormat("HH:mm");
                            if (dateFormat.format(time2).equals(dateFormat.format(time)))
                                txtSubText.setText("Hoạt động vào " + hourFormat.format(thisItemDate));//chỉ hiện giờ nếu lần hoạt động cuối trong cùng ngày
                            else
                                txtSubText.setText("Hoạt động vào " + dateFormat.format(thisItemDate));//hiện ngày
                        } else
                            txtSubText.setText(user.getEmail());
                    } else if (user.getStatus().equals("online")) {
                        viewUserStatus.setVisibility(View.VISIBLE);
                        txtSubText.setText("Đang hoạt động");
                    }
                    if (IS_DESTROY == 0) {
                        if (user.getImageURL().equals("default"))
                            imgViewUserImage.setImageResource(R.mipmap.ic_launcher_round);
                        else
                            Glide.with(MessageActivity.this).load(user.getImageURL()).into(imgViewUserImage);
                    }
                    currentID = user.getId();
                } else { //nếu type là group
                    Group myGroup = dataSnapshot.getValue(Group.class);
                    txtUserName.setText(myGroup.getName());
                    viewUserStatus.setVisibility(View.GONE);
                    txtSubText.setText(myGroup.getMember().split(",").length + " thành viên");
                    if (IS_DESTROY == 0) {
                        if (myGroup.getImageURL().equals("default"))
                            imgViewUserImage.setImageResource(R.mipmap.ic_launcher_round);
                        else
                            Glide.with(MessageActivity.this).load(myGroup.getImageURL()).into(imgViewUserImage);
                    }
                    currentID = myGroup.getId();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        readMessages(currentID);
        // seenMessage(userID);
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isNotify = true;
                String msg = editTextMessage.getText().toString();
                sendMessage(firebaseUser.getUid(), userID, msg.trim());
                editTextMessage.setText("");
            }
        });
        setSeenMessage();
        btnOpenGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu popup = new PopupMenu(MessageActivity.this, view);
                popup.getMenuInflater().inflate(R.menu.get_file_menu, popup.getMenu());
                popup.show();
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        if (item.getItemId() == R.id.gallery) {
                            Intent intent = new Intent();
                            intent.setType("image/*");
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            startActivityForResult(intent, IMAGE_REQUEST);
                        } else if (item.getItemId() == R.id.camera) {
                            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                            startActivityForResult(intent, CAMERA_REQUEST);
                        }
                        return true;
                    }
                });
            }
        });
        removeImgFromGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editTextMessage.setVisibility(View.VISIBLE);
                imgFromGalleryWrapper.setVisibility(View.GONE);
            }
        });
        EmojIconActions emojIcon = new EmojIconActions(this, rootView, editTextMessage, btnEmoji);
        emojIcon.setIconsIds(R.drawable.ic_round_keyboard_24, R.drawable.ic_baseline_insert_emoticon_24);
        emojIcon.ShowEmojIcon();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        onBackPressed();
        return super.onOptionsItemSelected(item);
    }

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
                    if (!message.getSender().equals(firebaseUser.getUid())) { //nếu dòng chat đó không phải mình gửi thì sẽ seen nó
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

    private void readMessages(final String userID) {
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
        if (imgFromGalleryWrapper.getVisibility() == View.VISIBLE) { //nếu định gửi ảnh
            hashMap.put("type", "image");
            imgFromGalleryWrapper.setVisibility(View.GONE);
            uploadImage(ref, hashMap);
        } else {
            if (!message.equals("")) {
                hashMap.put("message", message);
                hashMap.put("type", "text");
                ref.setValue(hashMap);
            }
        }

        //Tạo cuộc trò chuyện giữa 2 người
        DatabaseReference chatRef = FirebaseDatabase.getInstance().getReference("ChatIDList")
                .child(sender) //ID của mình
                .child(receiver);   //ID của người mình nhắn tới
        chatRef.child("id").setValue(receiver);
        if (!isCurrentTypeIsGroup) {
            chatRef = FirebaseDatabase.getInstance().getReference("ChatIDList")
                    .child(receiver) //ID của người mình nhắn tới
                    .child(sender);   //ID của mình
            chatRef.child("id").setValue(sender);
        }
        DatabaseReference ref2 = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
        ref2.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User myUser = snapshot.getValue(User.class);
                if (isNotify) {
                    sendNotification(receiver, myUser, message);
                }
                isNotify = false;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void sendNotification(String receiver, final User myUser, final String msg) {
        DatabaseReference tokens = FirebaseDatabase.getInstance().getReference("Tokens");
        Query query = tokens.orderByKey().equalTo(receiver);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Token token = dataSnapshot.getValue(Token.class);
                    Data data = new Data(myUser.getId(), msg, myUser.getUsername(), userID, myUser.getImageURL());

                    Sender sender = new Sender(data, token.getToken());
                    apiService.sendNotification(sender)
                            .enqueue(new Callback<MyResponse>() {
                                @Override
                                public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                                    if (response.code() == 200) {
                                        if (response.body().success != 1) {
                                            Toast.makeText(MessageActivity.this, "Failed", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }

                                @Override
                                public void onFailure(Call<MyResponse> call, Throwable t) {

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
    boolean isImageFromGallery=false;
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            editTextMessage.setVisibility(View.GONE);
            imgFromGalleryWrapper.setVisibility(View.VISIBLE);
            imgFromGallery.setImageURI(imageUri);
            isImageFromGallery=true;
        } else if (requestCode == CAMERA_REQUEST && resultCode == RESULT_OK) {
            imageBitmap = (Bitmap) data.getExtras().get("data");
            editTextMessage.setVisibility(View.GONE);
            imgFromGalleryWrapper.setVisibility(View.VISIBLE);
            imgFromGallery.setImageBitmap(imageBitmap);
            imageData=getByteDataFromBitmap(imageBitmap);
            isImageFromGallery=false;
        }
    }

    //trả về đuôi jpg hoặc png...
    private String getFileExtension(Uri uri) {
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private byte[] getByteDataFromBitmap(Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        //String path2 = MediaStore.Images.Media.insertImage(getContentResolver(), inImage, "Title", null);
        byte[] data = bytes.toByteArray();
        return data;
    }

    private void uploadImage(final DatabaseReference ref, final HashMap<String, Object> hashMap) {
        final StorageReference fileReference;
        Toast.makeText(MessageActivity.this, "Đang gửi ảnh...", Toast.LENGTH_SHORT).show();
        if(isImageFromGallery) {
            fileReference = storageReference.child(UUID.randomUUID() + "" + System.currentTimeMillis()
                    + "." + getFileExtension(imageUri));
            uploadTask = fileReference.putFile(imageUri);
        }
        else {
            fileReference = storageReference.child(UUID.randomUUID() + "" + System.currentTimeMillis()+".jpeg");
            uploadTask = fileReference.putBytes(imageData);
        }
        uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }
                return fileReference.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    Uri downloadUri = task.getResult();
                    imgURL = downloadUri.toString();
                    hashMap.put("message", imgURL);
                    ref.setValue(hashMap);
                    Toast.makeText(MessageActivity.this, "Gửi ảnh thành công ", Toast.LENGTH_SHORT).show();
                    editTextMessage.setVisibility(View.VISIBLE);
                } else {
                    Toast.makeText(MessageActivity.this, "Gửi ảnh không thành công " + task.getException(), Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MessageActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
