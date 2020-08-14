package com.example.hellu;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {
    TextInputLayout username, password,email;
    Button btnRegister;
    FirebaseAuth auth;
    DatabaseReference reference;
    LoadingDialog loadingDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        Toolbar toolbar=findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");


        username=findViewById(R.id.username);
        password=findViewById(R.id.password);
        email=findViewById(R.id.email);
        btnRegister=findViewById(R.id.btnRegister);

        auth=FirebaseAuth.getInstance();

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String txtuser=username.getEditText().getText().toString().trim();
                String txtpw=password.getEditText().getText().toString();
                String txtemail=email.getEditText().getText().toString().trim();
                if(!validateEmail(txtemail)|!validatePassword(txtpw)|!validateUserName(txtuser)) {
                    return;
                }
                else{
                    loadingDialog=new LoadingDialog(RegisterActivity.this,"Đang đăng ký...");
                    loadingDialog.startDialog();
                    register(txtuser, txtpw, txtemail);
                }
            }
        });
    }
    boolean validatePassword(String pw){
        if(pw.trim().equals(""))
        {
            password.setError("Trường này không được để trống");
            return false;
        }
        else if(pw.length()<6)
        {
            password.setError("Mật khẩu tối thiểu là 6 kí tự");
            return false;
        }else{
            password.setError(null);
        }
        return true;
    }
    boolean validateUserName(String name){
        if(name.trim().equals(""))
        {
            username.setError("Trường này không được để trống");
            return false;
        }else
        {
            username.setError(null);
        }
        return true;
    }
    boolean validateEmail(String e){
        if(e.trim().equals(""))
        {
            email.setError("Trường này không được để trống");
            return false;
        }else{
            email.setError(null);
        }
        return true;
    }
    private void register(final String name, String pw, final String email){
        auth.createUserWithEmailAndPassword(email,pw).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                loadingDialog.dismissDialog();
                if(task.isSuccessful())
                {
                    FirebaseUser f=auth.getCurrentUser();
                    assert f!=null; //gần giống if
                    String userID=f.getUid();
                    reference= FirebaseDatabase.getInstance().getReference("Users").child(userID);

                    HashMap<String,String> hashMap=new HashMap<>();
                    hashMap.put("id",userID);
                    hashMap.put("username",name.trim());
                    hashMap.put("email",email);
                    hashMap.put("imageURL","default");
                    hashMap.put("status","offline");
                    hashMap.put("search",name.toLowerCase().trim()+" "+email+" "+name.trim());
                    hashMap.put("lastonline","0");
                    reference.setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Intent intent=new Intent(RegisterActivity.this,MainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            finish();
                        }
                    });
                }else{
                    Toast.makeText(RegisterActivity.this,"Bạn không thể đăng ký với email này",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        onBackPressed();
        return super.onOptionsItemSelected(item);
    }
}
