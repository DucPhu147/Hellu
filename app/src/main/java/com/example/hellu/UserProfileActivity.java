package com.example.hellu;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.EditTextPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.bumptech.glide.Glide;
import com.example.hellu.Model.User;
import com.google.android.gms.tasks.Continuation;
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
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserProfileActivity extends AppCompatActivity {
    FirebaseUser firebaseUser;
    DatabaseReference reference;
    CircleImageView userImage;
    ImageView imageGallery;
    private static final int IMAGE_REQUEST=1;
    private Uri imageUri;
    private StorageTask uploadTask;
    StorageReference storageReference;
    String userID,imageURL;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);
        Toolbar toolbar=findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");
        userImage = findViewById(R.id.userProfile_Image);
        imageGallery = findViewById(R.id.userProfile_Gallery);
        userID = getIntent().getStringExtra("id");
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference("Users").child(userID).child("imageURL");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (IS_DESTROY == 0) {
                    imageURL = dataSnapshot.getValue(String.class);
                    //userEmail.setText(user.getEmail());
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
        imageGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
            }


        });
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings, new SettingsFragment())
                .commit();
        userImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(UserProfileActivity.this, ImageActivity.class);
                intent.putExtra("imageURL",imageURL);
                startActivity(intent);
            }
        });
    }
    int IS_DESTROY=0;
    @Override
    protected void onDestroy() {
        super.onDestroy();
        IS_DESTROY=1;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        onBackPressed();
        return super.onOptionsItemSelected(item);
    }
    private void openGallery() {
        Intent intent=new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent,IMAGE_REQUEST);
    }
    //trả về đuôi jpg hoặc png...
    private String getFileExtension(Uri uri){
        ContentResolver contentResolver=getContentResolver();
        MimeTypeMap mimeTypeMap=MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }
    private void uploadImage(){
        final LoadingDialog loadingDialog=new LoadingDialog(UserProfileActivity.this,"Đang tải ảnh lên...");
        loadingDialog.startDialog();
        if(imageUri!=null){
            final StorageReference fileReference=storageReference.child(UUID.randomUUID()+""+System.currentTimeMillis()
                    +"."+getFileExtension(imageUri));
            uploadTask=fileReference.putFile(imageUri);
            uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if(!task.isSuccessful())
                    {
                        throw task.getException();
                    }
                    return fileReference.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    loadingDialog.dismissDialog();
                    if(task.isSuccessful()){
                        Uri downloadUri=task.getResult();
                        String mUri=downloadUri.toString();

                        reference=FirebaseDatabase.getInstance().getReference("Users").child(userID);
                        HashMap<String,Object> map=new HashMap<>();
                        map.put("imageURL",mUri);
                        reference.updateChildren(map);
                    }
                    else
                    {
                        Toast.makeText(UserProfileActivity.this,"Tải ảnh không thành công",Toast.LENGTH_SHORT).show();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(UserProfileActivity.this,e.getMessage(),Toast.LENGTH_SHORT).show();
                    loadingDialog.dismissDialog();
                }
            });
        }else
        {
            Toast.makeText(UserProfileActivity.this,"Không có ảnh nào được chọn",Toast.LENGTH_SHORT).show();
            loadingDialog.dismissDialog();
        }
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
