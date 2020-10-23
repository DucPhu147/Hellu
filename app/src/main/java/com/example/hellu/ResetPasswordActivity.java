package com.example.hellu;

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
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;

public class ResetPasswordActivity extends AppCompatActivity {
    TextInputLayout email,password;
    Button btnReset;
    FirebaseAuth firebaseAuth;
    boolean isForgotPW;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);
        Toolbar toolbar=findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Reset mật khẩu");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        email=findViewById(R.id.email);
        btnReset=findViewById(R.id.btnReset);
        password=findViewById(R.id.password);
        String action=getIntent().getStringExtra("action");
        if(action.equals("changePW")) {
            isForgotPW = false;
            email.setVisibility(View.GONE);
        }
        else if(action.equals("resetPW")) {
            isForgotPW = true;
            password.setVisibility(View.GONE);
        }
        firebaseAuth=FirebaseAuth.getInstance();
        btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isForgotPW)
                    forgotPassword();
                else
                    changePW();
            }
        });
    }
    public void forgotPassword() {
        String mail = email.getEditText().getText().toString();
        if (mail.trim().isEmpty()) {
            Toast.makeText(ResetPasswordActivity.this, "Bạn phải nhập email", Toast.LENGTH_SHORT).show();
        } else {
            firebaseAuth.sendPasswordResetEmail(mail).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(ResetPasswordActivity.this, "Đã gửi liên kết đến email của bạn", Toast.LENGTH_SHORT).show();
                        onBackPressed();
                    } else {
                        Toast.makeText(ResetPasswordActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
    }
    public void changePW(){
        String pw = password.getEditText().getText().toString();
        if (pw.trim().isEmpty()) {
            Toast.makeText(ResetPasswordActivity.this, "Bạn phải nhập mật khẩu", Toast.LENGTH_SHORT).show();
        } else {
            AuthCredential credential= EmailAuthProvider.getCredential(firebaseAuth.getCurrentUser().getEmail(),pw);
            firebaseAuth.getCurrentUser().reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        firebaseAuth.sendPasswordResetEmail(firebaseAuth.getCurrentUser().getEmail()).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(ResetPasswordActivity.this, "Đã gửi liên kết đến email của bạn", Toast.LENGTH_SHORT).show();
                                    onBackPressed();
                                } else {
                                    Toast.makeText(ResetPasswordActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                }
                            }
                        });
                    }else{
                        Toast.makeText(ResetPasswordActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        onBackPressed();
        return super.onOptionsItemSelected(item);
    }
}
