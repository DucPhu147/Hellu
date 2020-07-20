package com.example.hellu;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.SearchView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hellu.Adapter.SelectUsersAdapter;
import com.example.hellu.Adapter.SelectUsersAdapter2;
import com.example.hellu.Model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class CreateGroupActivity extends AppCompatActivity {
    RecyclerView recyclerView1,recyclerView2;
    private List<User> list1,list2;
    private SelectUsersAdapter selectUsersAdapter1;
    private SelectUsersAdapter2 selectUsersAdapter2;
    RecyclerView.LayoutManager layoutManager;
    EditText editGroupName;
    LinearLayoutManager layoutManager2;
    SearchView searchView;
    boolean isUserRead=false;
    DatabaseReference groupRef;
    MenuItem continueMenuItem;
    User currentUser;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);
        Toolbar toolbar=findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Tạo nhóm mới");
        searchView=findViewById(R.id.selectUser_searchUser);
        editGroupName=findViewById(R.id.selectUser_groupName);
        recyclerView1=findViewById(R.id.selectUser_userList);
        recyclerView2=findViewById(R.id.selectUser_userList2);
        recyclerView2.setVisibility(View.GONE);
        recyclerView1.setHasFixedSize(true);
        recyclerView2.setHasFixedSize(true);

        layoutManager=new LinearLayoutManager(CreateGroupActivity.this);
        layoutManager2=new LinearLayoutManager((CreateGroupActivity.this));
        layoutManager2.setOrientation(RecyclerView.HORIZONTAL);

        recyclerView1.setLayoutManager(layoutManager);
        recyclerView2.setLayoutManager((layoutManager2));

        list1=new ArrayList<>();
        list2=new ArrayList<>();

        selectUsersAdapter1=new SelectUsersAdapter(CreateGroupActivity.this,list1,this);
        recyclerView1.setAdapter(selectUsersAdapter1);
        selectUsersAdapter2=new SelectUsersAdapter2(CreateGroupActivity.this,list2,this);
        recyclerView2.setAdapter(selectUsersAdapter2);
        currentUser=(User)getIntent().getSerializableExtra("currentUser");
        readUser();
    }
    private void readUser(){
        final FirebaseUser firebaseUser= FirebaseAuth.getInstance().getCurrentUser();
        final DatabaseReference reference= FirebaseDatabase.getInstance().getReference("Users");
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(isUserRead==false) {
                    isUserRead=true;
                    list1.clear();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        User user = snapshot.getValue(User.class);
                        assert user != null;
                        assert firebaseUser != null;
                        if (!user.getId().equals(firebaseUser.getUid())) {
                            list1.add(user);
                        }
                    }
                    selectUsersAdapter1.notifyDataSetChanged();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id=item.getItemId();
        if(id==R.id.action_continue) {
            //Lấy ID tự tạo của firebase
            HashMap<String, Object> hashMap=new HashMap<>();
            hashMap.put("owner",currentUser.getId());
            String groupName;
            if(editGroupName.getText().toString().trim().equals("")) {
                groupName="Nhóm của "+currentUser.getUsername();
            }
            else {
                groupName=editGroupName.getText().toString();
            }
            hashMap.put("name", groupName.trim());
            hashMap.put("search", groupName.toLowerCase().trim()+" "+groupName.trim());
            hashMap.put("imageURL","default");
            String key="Group_"+ UUID.randomUUID()+System.currentTimeMillis();//tự tạo id riêng với khả năng bị trùng thấp nhất
            hashMap.put("id",key);

            groupRef = FirebaseDatabase.getInstance().getReference("Groups").child(key);

            DatabaseReference chatRef =FirebaseDatabase.getInstance().getReference("ChatIDList")
                    .child(currentUser.getId()) //ID của mình
                    .child(key);   //ID của group
            chatRef.child("id").setValue(key);
            String member="";
            member+=currentUser.getId()+",";
            for(int i=0;i<list2.size();i++){
                member+=list2.get(i).getId()+",";
                chatRef =FirebaseDatabase.getInstance().getReference("ChatIDList")
                        .child(list2.get(i).getId()) //ID của user khác
                        .child(key);   //ID của group
                chatRef.child("id").setValue(key);
            }
            hashMap.put("member",member);
            groupRef.setValue(hashMap);
            finish();
            Intent intent=new Intent(CreateGroupActivity.this, MessageActivity.class);
            intent.putExtra("id",key);
            startActivity(intent);
        }else
            onBackPressed();
        return super.onOptionsItemSelected(item);
    }


    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuInflater menuInflater=getMenuInflater();
        menuInflater.inflate(R.menu.activity_create_group_menu,menu);
        continueMenuItem=menu.getItem(0).setEnabled(false); //lấy item "Tiếp tục" của menu này
        continueMenuItem.setEnabled(false);
        continueMenuItem.setVisible(false);
        return super.onPrepareOptionsMenu(menu);
    }

    public void addItemToList2(User myUser, int positionOfItemInList1){
        list2.add(myUser);
        selectUsersAdapter2.notifyItemInserted(list2.size());
        if(recyclerView2.getVisibility()==View.GONE) {
            recyclerView2.setVisibility(View.VISIBLE);
        }
        if(list2.size()>=2)
        {
            continueMenuItem.setEnabled(true);
            continueMenuItem.setVisible(true);
        }
    }
    public void removeItemFromList2(User myUser){
        int position=list2.indexOf(myUser);
        list2.remove(myUser);
        if(list2.size()<2)
        {
            continueMenuItem.setEnabled(false);
            continueMenuItem.setVisible(false);
        }
        selectUsersAdapter2.notifyItemRemoved(position);
        selectUsersAdapter2.notifyItemRangeChanged(position, list2.size());
        //bỏ tích checkbox ở list user bên dưới khi xóa những user list bên trên
        int position2=list1.indexOf(myUser);
        SelectUsersAdapter.Viewholder holder= (SelectUsersAdapter.Viewholder) recyclerView1.findViewHolderForAdapterPosition(position2);
        holder.usersCheckBox.setChecked(false);
        if(list2.size()==0) {
            list2=new ArrayList<>();
            //thêm mấy dòng dưới để khi thêm item lại sẽ không bị glitch ảnh
            selectUsersAdapter2=new SelectUsersAdapter2(CreateGroupActivity.this,list2,this);
            recyclerView2.setAdapter(selectUsersAdapter2);
            recyclerView2.setVisibility(View.GONE);
        }
    }
}