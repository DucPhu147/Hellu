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

import com.example.hellu.Adapter.ChatAdapter;
import com.example.hellu.Model.ChatIDList;
import com.example.hellu.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ChatFragment extends Fragment {
    private ChatAdapter chatAdapter;
    private List<String> IDList;
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    FirebaseUser firebaseUser;
    DatabaseReference reference;

    private static final ChatFragment ourInstance = new ChatFragment();

    public static ChatFragment getInstance() {
        return ourInstance;
    }
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_chat,container,false);
        recyclerView=view.findViewById(R.id.chatUserRecycleView);
        recyclerView.setHasFixedSize(true);
        //int resId = R.anim.layout_animation_fall_down;
        //LayoutAnimationController animation = AnimationUtils.loadLayoutAnimation(getContext(), resId);
        //recyclerView.setLayoutAnimation(animation);
        layoutManager=new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        IDList=new ArrayList<>();
        chatAdapter =new ChatAdapter(getContext(),IDList);
        recyclerView.setAdapter(chatAdapter);
        readChatIDList();
        return view;
    }
    public void readChatIDList(){
        firebaseUser= FirebaseAuth.getInstance().getCurrentUser();
        reference= FirebaseDatabase.getInstance().getReference("ChatIDList").child(firebaseUser.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    IDList.clear();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        ChatIDList c = snapshot.getValue(ChatIDList.class);
                        IDList.add(c.getId());
                    }
                    chatAdapter.notifyDataSetChanged();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
