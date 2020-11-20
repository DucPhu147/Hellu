package com.example.hellu;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.SearchView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hellu.Adapter.ChooseMemberAdapter;
import com.example.hellu.Adapter.SelectedMemberAdapter;
import com.example.hellu.Model.ChatIDList;
import com.example.hellu.Model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class AddGroupMemberActivity extends AppCompatActivity {
    RecyclerView recycle_chooseUser,recycle_selectedUser;
    private List<User> chooseUserList,selectedUserList;
    private ChooseMemberAdapter chooseMemberAdapter;
    private SelectedMemberAdapter selectedMemberAdapter;
    RecyclerView.LayoutManager layoutManager;
    LinearLayoutManager layoutManager2;
    SearchView searchView;
    MenuItem continueMenuItem;
    User currentUser;
    ProgressBar progressBar;
    FirebaseUser firebaseUser;
    public static Activity thisActivity;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        thisActivity=this;
        setContentView(R.layout.activity_add_group_member);
        Toolbar toolbar=findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Thêm thành viên");
        getSupportActionBar().setElevation(3);
        searchView=findViewById(R.id.selectUser_searchUser);
        progressBar=findViewById(R.id.progressBar);
        recycle_chooseUser=findViewById(R.id.recycle_chooseUser);
        recycle_selectedUser=findViewById(R.id.recycle_selectedUser);

        recycle_selectedUser.setVisibility(View.GONE);

        firebaseUser= FirebaseAuth.getInstance().getCurrentUser();

        recycle_chooseUser.setHasFixedSize(true);
        recycle_selectedUser.setHasFixedSize(true);

        layoutManager=new LinearLayoutManager(AddGroupMemberActivity.this);
        layoutManager2=new LinearLayoutManager((AddGroupMemberActivity.this));
        layoutManager2.setOrientation(RecyclerView.HORIZONTAL);

        recycle_chooseUser.setLayoutManager(layoutManager);
        recycle_selectedUser.setLayoutManager((layoutManager2));

        chooseUserList=new ArrayList<>();
        selectedUserList=new ArrayList<>();

        chooseMemberAdapter =new ChooseMemberAdapter(AddGroupMemberActivity.this,chooseUserList,this);
        recycle_chooseUser.setAdapter(chooseMemberAdapter);
        selectedMemberAdapter =new SelectedMemberAdapter(AddGroupMemberActivity.this,selectedUserList,this);
        recycle_selectedUser.setAdapter(selectedMemberAdapter);
        currentUser=(User)getIntent().getSerializableExtra("currentUser");
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                    searchUser(s.trim());
                return false;
            }
        });
        readUser();
    }
    List<String> chatList;
    private void readUser(){
        //đọc những id user chat với mình
        chatList=new ArrayList<>();
        final DatabaseReference ref= FirebaseDatabase.getInstance().getReference("ChatIDList").child(firebaseUser.getUid());
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    chatList.clear();
                    for (DataSnapshot s : snapshot.getChildren()) {
                        ChatIDList c = s.getValue(ChatIDList.class);
                        chatList.add(c.getId());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        final Query reference= FirebaseDatabase.getInstance().getReference("Users");
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    progressBar.setVisibility(View.GONE);
                    chooseUserList.clear();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        User user = snapshot.getValue(User.class);
                        if (!user.getId().equals(firebaseUser.getUid())) {
                            chooseUserList.add(user);
                        }
                    }
                    chooseMemberAdapter.notifyDataSetChanged();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    public void searchUser(final String s) {
        if(s.equals("")){
            readUser();
        }else {
            Query query = FirebaseDatabase.getInstance().getReference("Users").orderByChild("search")
                    .startAt(s)
                    .endAt(s + "\uf8ff");
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        chooseUserList.clear();
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            User user = snapshot.getValue(User.class);
                            if (!user.getId().equals(firebaseUser.getUid())) {
                                chooseUserList.add(user);
                                /*if (selectedUserList.contains(user)) {
                                    ChooseMemberAdapter.Viewholder holder =
                                            (ChooseMemberAdapter.Viewholder)
                                                    recycle_chooseUser.findViewHolderForAdapterPosition(chooseUserList.indexOf(user));
                                    holder.usersCheckBox.setChecked(true);
                                }*/
                            }
                        }
                        chooseMemberAdapter.notifyDataSetChanged();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id=item.getItemId();
        if(id==R.id.action_confirm) {
            List<String> memberList=new ArrayList<>();
            memberList.add(0,currentUser.getId());
            for (User u:selectedUserList){
                memberList.add(u.getId());
            }
            Intent intent=new Intent(AddGroupMemberActivity.this, GroupDetailActivity.class);
            intent.putExtra("listMember", (Serializable) memberList);
            intent.putExtra("currentUser",currentUser);
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

    public void addItemToSelectedUserList(User myUser){
        if(!selectedUserList.contains(myUser)) {
            selectedUserList.add(myUser);
            selectedMemberAdapter.notifyItemInserted(selectedUserList.indexOf(myUser));
        }
        if(recycle_selectedUser.getVisibility()==View.GONE) {
            recycle_selectedUser.setVisibility(View.VISIBLE);
        }
        if(selectedUserList.size()>=2)
        {
            continueMenuItem.setEnabled(true);
            continueMenuItem.setVisible(true);
        }
    }
    public void removeItemFromSelectedUserList(User myUser){
        selectedMemberAdapter.notifyItemRemoved(selectedUserList.indexOf(myUser));
        selectedUserList.remove(myUser);
        if(selectedUserList.size()<2)
        {
            continueMenuItem.setEnabled(false);
            continueMenuItem.setVisible(false);
        }
        //bỏ tích checkbox ở list user bên dưới khi xóa những user list bên trên
        /*try {
            int position2 = chooseUserList.indexOf(myUser);
            ChooseMemberAdapter.Viewholder holder = (ChooseMemberAdapter.Viewholder) recycle_chooseUser.findViewHolderForAdapterPosition(position2);
            holder.usersCheckBox.setChecked(false);
        }catch(Exception ex){
            Log.d("selected_list",ex.getMessage());
        }*/
        if(selectedUserList.size()==0) {
            selectedUserList=new ArrayList<>();
            //thêm mấy dòng dưới để khi thêm item lại sẽ không bị glitch ảnh
            selectedMemberAdapter =new SelectedMemberAdapter(AddGroupMemberActivity.this,selectedUserList,this);
            recycle_selectedUser.setAdapter(selectedMemberAdapter);
            recycle_selectedUser.setVisibility(View.GONE);
        }
    }
}