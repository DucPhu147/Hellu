package com.example.hellu;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.EditTextPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.bumptech.glide.Glide;
import com.example.hellu.Class.LoadingDialog;
import com.example.hellu.Class.UploadFileToFirebase;
import com.example.hellu.Model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserProfileActivity extends AppCompatActivity {
    FirebaseUser firebaseUser;
    DatabaseReference reference;
    CircleImageView userImage;
    ImageView openGallery;
    TextView userName, userEmail;
    private final static int IMAGE_REQUEST = 1, CAMERA_REQUEST = 2;
    private Uri imageUri;
    private StorageTask uploadTask;
    StorageReference storageReference;
    String userID, imageURL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");

        userImage = findViewById(R.id.userProfile_Image);
        userName = findViewById(R.id.userProfile_UserName);
        userEmail = findViewById(R.id.userProfile_Email);
        openGallery = findViewById(R.id.openGallery);

        userID = getIntent().getStringExtra("id");
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (!userID.equals(firebaseUser.getUid())) {
            openGallery.setVisibility(View.GONE);
        } else {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings, new SettingsFragment())
                    .commit();
        }
        reference = FirebaseDatabase.getInstance().getReference("Users").child(userID);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    User user = dataSnapshot.getValue(User.class);
                    userName.setText(user.getUsername());
                    userEmail.setText(user.getEmail());
                    if (IS_DESTROY == 0) {
                        imageURL = user.getImageURL();
                        if (imageURL.equals("default")) {
                            userImage.setImageResource(R.mipmap.ic_launcher_round);
                        } else {
                            Glide.with(UserProfileActivity.this).load(imageURL).into(userImage);
                        }
                    }
                }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        storageReference = FirebaseStorage.getInstance().getReference("UserProfileImages");
        openGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(intent, IMAGE_REQUEST);
            }
        });
        userImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(UserProfileActivity.this, ImageActivity.class);
                intent.putExtra("imageURL", imageURL);
                startActivity(intent);
            }
        });
    }

    int IS_DESTROY = 0;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        IS_DESTROY = 1;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        onBackPressed();
        return super.onOptionsItemSelected(item);
    }
    private void uploadImage() {
        final LoadingDialog loadingDialog = new LoadingDialog(UserProfileActivity.this, "Đang tải ảnh lên...");
        loadingDialog.startDialog();
        UploadFileToFirebase uploadFileToFirebase = new UploadFileToFirebase(UserProfileActivity.this, true, imageUri);
        uploadFileToFirebase.uploadImage().addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                loadingDialog.dismissDialog();
                if (task.isSuccessful()) {
                    Uri downloadUri = task.getResult();
                    String mUri = downloadUri.toString();

                    reference = FirebaseDatabase.getInstance().getReference("Users").child(userID);
                    HashMap<String, Object> map = new HashMap<>();
                    map.put("imageURL", mUri);
                    reference.updateChildren(map);
                } else {
                    Toast.makeText(UserProfileActivity.this, "Tải ảnh không thành công", Toast.LENGTH_SHORT).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(UserProfileActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                loadingDialog.dismissDialog();
            }
        });
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==IMAGE_REQUEST&&resultCode==RESULT_OK&&data!=null&&data.getData()!=null)
        {
            imageUri=data.getData();
            if(uploadTask!=null&&uploadTask.isInProgress())
            {
                Toast.makeText(UserProfileActivity.this,"Đang tải ảnh lên...",Toast.LENGTH_SHORT).show();
            }else
                uploadImage();
        }
    }


    public static class SettingsFragment extends PreferenceFragmentCompat {
        EditTextPreference emailPref;
        EditTextPreference usernamePref;
        FirebaseUser firebaseUser;
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.user_profile_setting, rootKey);
            emailPref=findPreference("useremail");
            usernamePref=findPreference("username");
            firebaseUser=FirebaseAuth.getInstance().getCurrentUser();
            DatabaseReference reference=FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
            reference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    User user=snapshot.getValue(User.class);
                    emailPref.setText(user.getEmail());
                    usernamePref.setText(user.getUsername());
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
            usernamePref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    if(!newValue.equals("")) {
                        DatabaseReference ref = FirebaseDatabase.getInstance()
                                .getReference("Users")
                                .child(firebaseUser.getUid());
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("username", newValue.toString().trim());
                        hashMap.put("search", newValue.toString().toLowerCase().trim());
                        ref.updateChildren(hashMap);
                    }
                    return false;
                }
            });
        }
    }
}
