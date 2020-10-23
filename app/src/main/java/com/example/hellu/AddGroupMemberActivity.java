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
import com.example.hellu.Model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
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
        readUser();
    }
    private void readUser(){
        final FirebaseUser firebaseUser= FirebaseAuth.getInstance().getCurrentUser();
        final DatabaseReference reference= FirebaseDatabase.getInstance().getReference("Users");
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    chooseUserList.clear();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        User user = snapshot.getValue(User.class);
                        if (!user.getId().equals(firebaseUser.getUid())) {
                            chooseUserList.add(user);
                        }
                    }
                    progressBar.setVisibility(View.GONE);
                    chooseMemberAdapter.notifyDataSetChanged();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
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

    public void addItemToList2(User myUser){
        selectedUserList.add(myUser);
        selectedMemberAdapter.notifyItemInserted(selectedUserList.size());
        if(recycle_selectedUser.getVisibility()==View.GONE) {
            recycle_selectedUser.setVisibility(View.VISIBLE);
        }
        if(selectedUserList.size()>=2)
        {
            continueMenuItem.setEnabled(true);
            continueMenuItem.setVisible(true);
        }
    }
    public void removeItemFromList2(User myUser){
        int position=selectedUserList.indexOf(myUser);
        selectedUserList.remove(myUser);
        if(selectedUserList.size()<2)
        {
            continueMenuItem.setEnabled(false);
            continueMenuItem.setVisible(false);
        }
        selectedMemberAdapter.notifyItemRemoved(position);
        selectedMemberAdapter.notifyItemRangeChanged(position, selectedUserList.size());
        //bỏ tích checkbox ở list user bên dưới khi xóa những user list bên trên
        int position2=chooseUserList.indexOf(myUser);
        ChooseMemberAdapter.Viewholder holder= (ChooseMemberAdapter.Viewholder) recycle_chooseUser.findViewHolderForAdapterPosition(position2);
        holder.usersCheckBox.setChecked(false);
        if(selectedUserList.size()==0) {
            selectedUserList=new ArrayList<>();
            //thêm mấy dòng dưới để khi thêm item lại sẽ không bị glitch ảnh
            selectedMemberAdapter =new SelectedMemberAdapter(AddGroupMemberActivity.this,selectedUserList,this);
            recycle_selectedUser.setAdapter(selectedMemberAdapter);
            recycle_selectedUser.setVisibility(View.GONE);
        }
    }
}