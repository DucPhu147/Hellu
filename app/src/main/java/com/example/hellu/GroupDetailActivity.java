package com.example.hellu;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.hellu.Adapter.GroupMemberAdapter;
import com.example.hellu.Class.UploadFileToFirebase;
import com.example.hellu.Model.Group;
import com.example.hellu.Model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class GroupDetailActivity extends AppCompatActivity {
    RecyclerView.LayoutManager layoutManager;
    RecyclerView recyclerView;
    private List<String> memberList;
    TextInputLayout inputLayoutGroupName;
    private GroupMemberAdapter groupMemberAdapter;
    TextView memberCount;
    ImageView openGalery,groupImage;
    User currentUser;
    String groupID;
    Uri imageUri;
    Group groupObjectForUpdate;
    private final static int IMAGE_REQUEST=1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_detail);
        Toolbar toolbar=findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Thông tin nhóm");
        getSupportActionBar().setElevation(3);

        recyclerView=findViewById(R.id.groupDetail_MemberList);
        memberCount=findViewById(R.id.groupDetail_GroupMemberCount);
        openGalery=findViewById(R.id.openGallery);
        groupImage=findViewById(R.id.groupDetail_Image);
        inputLayoutGroupName=findViewById(R.id.groupDetail_Name);

        layoutManager=new LinearLayoutManager(GroupDetailActivity.this);

        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);

        memberList=new ArrayList<>();
        memberList= (List<String>) getIntent().getSerializableExtra("listMember");
        if(getIntent().getSerializableExtra("groupObjectForUpdate")!=null){
            groupObjectForUpdate=(Group)getIntent().getSerializableExtra("groupObjectForUpdate");
            if(!groupObjectForUpdate.getImageURL().equals("default")) {
                Glide.with(GroupDetailActivity.this)
                        .load(groupObjectForUpdate.getImageURL())
                        .into(groupImage);
            }
            inputLayoutGroupName.getEditText().setText(groupObjectForUpdate.getName());
            groupID=groupObjectForUpdate.getId();
            Log.w("groupImage","loaded");
        }
        //groupImage.image
        memberCount.setText("Thành viên: "+memberList.size());
        currentUser=(User)getIntent().getSerializableExtra("currentUser");
        groupMemberAdapter =new GroupMemberAdapter(GroupDetailActivity.this,memberList);

        recyclerView.setAdapter(groupMemberAdapter);
        groupMemberAdapter.notifyDataSetChanged();
        openGalery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(intent, IMAGE_REQUEST);
            }
        });
    }
    private void createGroup() {
        //Lấy ID tự tạo của firebase
        HashMap<String, Object> hashMap = new HashMap<>();
        //nếu groupObjectForUpdate là null thì là tạo group mới, còn ko là update group
        if(getIntent().getSerializableExtra("groupObjectForUpdate")==null) {
            hashMap.put("owner", memberList.get(0));
            String groupName;
            if (inputLayoutGroupName.getEditText().getText().toString().trim().equals("")) {
                groupName = "Nhóm của " + currentUser.getUsername();
            } else {
                groupName = inputLayoutGroupName.getEditText().getText().toString().trim();
            }
            hashMap.put("name", groupName.trim());
            hashMap.put("search", groupName.toLowerCase().trim() + " " + groupName.trim());
            hashMap.put("imageURL", "default");
            groupID = "Group_" + UUID.randomUUID() + System.currentTimeMillis();//tự tạo id riêng với khả năng bị trùng thấp nhất
            hashMap.put("id", groupID);

            String member = "";
            DatabaseReference groupRef = FirebaseDatabase.getInstance().getReference("Groups").child(groupID);
            for (int i = 0; i < memberList.size(); i++) {
                member += memberList.get(i) + ",";
                //Tạo chat user ID cho các member
                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("ChatIDList")
                        .child(memberList.get(i))//ID của mình
                        .child(groupID);//ID của group
                ref.child("id").setValue(groupID);
            }
            hashMap.put("member", member);
            //sau khi thêm thành công group vào database thì sẽ up ảnh group hoặc chuyển luôn sang message activity
            groupRef.setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        startMessageActivity();
                        if (imageUri != null)
                            uploadImage();
                    } else {
                        Toast.makeText(GroupDetailActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }else{
            hashMap.put("name",inputLayoutGroupName.getEditText().getText().toString());
            if(imageUri!=null)
                uploadImage();
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Groups").child(groupID);
            reference.updateChildren(hashMap);
            finish();
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if(getIntent().getSerializableExtra("groupObjectForUpdate")!=null) {
            if(groupObjectForUpdate.getOwner().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                MenuInflater menuInflater = getMenuInflater();
                menuInflater.inflate(R.menu.activity_create_group_menu, menu);
            }else{//nếu ko phải chủ group thì sẽ không chỉnh ảnh hay chỉnh tên group đươc
                openGalery.setVisibility(View.GONE);
                inputLayoutGroupName.getEditText().setEnabled(false);
            }
        }else{
            MenuInflater menuInflater = getMenuInflater();
            menuInflater.inflate(R.menu.activity_create_group_menu, menu);
        }
        return super.onCreateOptionsMenu(menu);
    }
    private void startMessageActivity(){
        //finish activity add member
        AddGroupMemberActivity.thisActivity.finish();
        //
        finish();
        Intent intent=new Intent(GroupDetailActivity.this,MessageActivity.class);
        intent.putExtra("id",groupID);
        startActivity(intent);
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id=item.getItemId();
        if(id==R.id.action_confirm) {
            createGroup();
        }else
            onBackPressed();
        return super.onOptionsItemSelected(item);
    }
    private void uploadImage() {
        UploadFileToFirebase uploadFileToFirebase = new UploadFileToFirebase(GroupDetailActivity.this, imageUri);
        uploadFileToFirebase.uploadImage().addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    Uri downloadUri = task.getResult();
                    String mUri = downloadUri.toString();
                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Groups").child(groupID);
                    HashMap<String, Object> map = new HashMap<>();
                    map.put("imageURL", mUri);
                    reference.updateChildren(map);
                }
                else{
                    Toast.makeText(GroupDetailActivity.this,task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==IMAGE_REQUEST&&resultCode==RESULT_OK&&data!=null&&data.getData()!=null)
        {
            imageUri=data.getData();
            groupImage.setImageURI(imageUri);
        }
    }
}