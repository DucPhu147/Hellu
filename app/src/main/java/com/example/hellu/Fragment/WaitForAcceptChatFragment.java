package com.example.hellu.Fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hellu.Model.ChatIDList;
import com.example.hellu.Model.Group;
import com.example.hellu.R;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;
import java.util.List;

public class WaitForAcceptChatFragment extends Fragment {
    //private GroupsAdapter groupsAdapter;
    private List<ChatIDList> groupIDList;
    private List<Group> chatGroup;
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    FirebaseUser firebaseUser;
    DatabaseReference reference;

    private static final WaitForAcceptChatFragment ourInstance = new WaitForAcceptChatFragment();

    public static WaitForAcceptChatFragment getInstance() {
        return ourInstance;
    }
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_chat_group,container,false);
        recyclerView=view.findViewById(R.id.chatGroupRecycleView);
        recyclerView.setHasFixedSize(true);
        //int resId = R.anim.layout_animation_fall_down;
        //LayoutAnimationController animation = AnimationUtils.loadLayoutAnimation(getContext(), resId);
        //recyclerView.setLayoutAnimation(animation);
        layoutManager=new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        groupIDList=new ArrayList<>();
        chatGroup=new ArrayList<>();
       // groupsAdapter=new GroupsAdapter(getContext(),chatGroup);
       // recyclerView.setAdapter(groupsAdapter);
        //readUserID();
        return view;
    }

   /* public void readUserID(){
        firebaseUser= FirebaseAuth.getInstance().getCurrentUser();
        reference= FirebaseDatabase.getInstance().getReference("ChatUserList").child(firebaseUser.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                groupIDList.clear();
                for(DataSnapshot snapshot:dataSnapshot.getChildren()){
                    ChatIDList c=snapshot.getValue(ChatIDList.class);
                    groupIDList.add(c);
                }
                readChatGroup();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    public void readChatGroup(){
        reference= FirebaseDatabase.getInstance().getReference("Groups");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                chatGroup.clear();
                for(DataSnapshot snapshot:dataSnapshot.getChildren()){
                    Group myGroup=snapshot.getValue(Group.class);
                    for(ChatIDList i:groupIDList){
                        if(i.getId().equals(myGroup.getId()))
                        {
                            chatGroup.add(myGroup);
                        }
                    }
                }
                groupsAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }*/

}
