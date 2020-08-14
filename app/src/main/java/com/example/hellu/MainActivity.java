package com.example.hellu;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.bumptech.glide.Glide;
import com.example.hellu.Fragment.ChatFragment;
import com.example.hellu.Fragment.SearchFragment;
import com.example.hellu.MessageNotification.Token;
import com.example.hellu.Model.Calling;
import com.example.hellu.Model.User;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

import java.io.Serializable;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {
    DrawerLayout drawer;
    CircleImageView navUserImage;
    TextView navUserName,navUserEmail;

    FirebaseUser firebaseUser;
    DatabaseReference reference;
    FloatingActionButton fab;
    int IS_DESTROY=0;
    TabLayout tabLayout;
    User currentUser;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setElevation(3);
        drawer = findViewById(R.id.drawer_layout);
        final NavigationView navigationView = findViewById(R.id.nav_view);
        ActionBarDrawerToggle toggle=new ActionBarDrawerToggle(this, drawer,toolbar,R.string.nav_drawer_open,R.string.nav_drawer_close);
        drawer.addDrawerListener(toggle); //hiện icon menu góc trái màn hình
        toggle.syncState(); //hiện icon menu góc trái màn hình
        //các item của nav drawer
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId())
                {
                    case R.id.nav_account:
                        Intent intent=new Intent(MainActivity.this, UserProfileActivity.class);
                        intent.putExtra("id",firebaseUser.getUid());
                        startActivity(intent);
                        break;
                    case R.id.nav_setting:
                        break;
                    case R.id.nav_signout: //Đăng xuất account khỏi server
                        FirebaseAuth.getInstance().signOut();
                        startActivity(new Intent(MainActivity.this,LoginActivity.class));
                        finish();
                        break;
                }
                return false;
            }
        });
        //
        FragmentManager fragmentManager=getSupportFragmentManager();
        FragmentTransaction fragmentTransaction=fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.fragment_container, SearchFragment.getInstance());
        fragmentTransaction.hide(SearchFragment.getInstance());
        fragmentTransaction.commit();

        //lấy id từ naview
        View headerView = navigationView.getHeaderView(0);
        navUserImage=headerView.findViewById(R.id.user_image);
        navUserName=headerView.findViewById(R.id.user_name);
        navUserEmail=headerView.findViewById(R.id.user_email);
        //
        firebaseUser= FirebaseAuth.getInstance().getCurrentUser();
        reference= FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (IS_DESTROY == 0) {
                    currentUser = dataSnapshot.getValue(User.class);
                    navUserName.setText(currentUser.getUsername());
                    navUserEmail.setText(currentUser.getEmail());
                    if (currentUser.getImageURL().equals("default")) {
                        navUserImage.setImageResource(R.mipmap.ic_launcher_round);
                    } else {
                        Glide.with(MainActivity.this).load(currentUser.getImageURL()).into(navUserImage);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        fab=findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(MainActivity.this, CreateGroupActivity.class);
                intent.putExtra("currentUser", (Serializable) currentUser);
                startActivity(intent);
            }
        });
        tabLayout=findViewById(R.id.tab_layout);
        updateToken(FirebaseInstanceId.getInstance().getToken());
    }
    private void updateToken(String token){
        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Tokens");
        Token token1=new Token(token);
        ref.child(firebaseUser.getUid()).setValue(token1);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater=getMenuInflater();
        inflater.inflate(R.menu.main,menu);
        MenuItem searchMenuItem = menu.findItem(R.id.search);
        SearchView searchView=(SearchView)searchMenuItem.getActionView();
       // ImageView searchIcon = searchView.findViewById(androidx.appcompat.R.id.search_button);
        View underLine = searchView.findViewById(androidx.appcompat.R.id.search_plate); //đổi màu thanh underline của searchview
        underLine.setBackgroundColor(Color.TRANSPARENT);
        //Vì mặc định ô search sẽ margin trái khá nhiều nên phải chỉnh lại cho cân với toolbar title
        LinearLayout searchEditFrame = searchView.findViewById(androidx.appcompat.R.id.search_edit_frame);
        ((LinearLayout.LayoutParams) searchEditFrame.getLayoutParams()).leftMargin = -20;
      //  searchIcon.setImageDrawable(ContextCompat.getDrawable(MainActivity.this,R.drawable.ic_account_circle_black_24dp));
        SearchView.SearchAutoComplete searchAutoComplete = searchView.findViewById(androidx.appcompat.R.id.search_src_text);
        searchAutoComplete.setTextColor(getResources().getColor(R.color.colorBlack));
        searchAutoComplete.setHint("Tìm kiếm...");
        searchAutoComplete.setHintTextColor(getResources().getColor(R.color.colorBlackTransparent));
        //searchAutoComplete.(getResources().getColor(R.color.colorPrimary));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                SearchFragment.getInstance().searchUser(newText.toLowerCase());
                return false;
            }
        });
        searchMenuItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                replaceFragment(SearchFragment.getInstance(), ChatFragment.getInstance());
                fab.hide();
                //tabLayout.setVisibility(View.GONE);
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                replaceFragment(ChatFragment.getInstance(), SearchFragment.getInstance());
                fab.show();
                //tabLayout.setVisibility(View.VISIBLE);
                return true;
            }
        });
        return true;
    }
    private void replaceFragment(Fragment fragmentShow,Fragment fragmentHide)
    {
        FragmentManager fragmentManager=getSupportFragmentManager();
        FragmentTransaction fragmentTransaction=fragmentManager.beginTransaction();
        fragmentTransaction.hide(fragmentHide).show(fragmentShow);
        fragmentTransaction.commit();
    }
    @Override
    public void onBackPressed() {
        if(drawer.isDrawerOpen(GravityCompat.START))
            drawer.closeDrawer(GravityCompat.START);
        else
            super.onBackPressed();
    }
    private void setStatus(String status){
        reference=FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
        HashMap<String,Object> hashMap=new HashMap<>();
        hashMap.put("status",status);
        if(status.equals("offline"))
            hashMap.put("lastonline", ServerValue.TIMESTAMP);
        reference.updateChildren(hashMap);
    }
    boolean isStartCalling=false;
    @Override
    protected void onStart() {
        super.onStart();
        DatabaseReference reference=FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.hasChild("Calling")){
                    //thêm isStartCalling vào để ngăn việc start activity lần nữa khi update child Calling
                    if(!isStartCalling) {
                        Calling call = snapshot.child("Calling").getValue(Calling.class);
                        Intent intent = new Intent(MainActivity.this, CallingActivity.class);
                        intent.putExtra("callModel", call);
                        startActivity(intent);
                        isStartCalling = true;
                    }
                }else
                    isStartCalling=false;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        IS_DESTROY=1;
        setStatus("offline");
    }

    @Override
    protected void onResume() {
        super.onResume();
        setStatus("online");
    }
}
