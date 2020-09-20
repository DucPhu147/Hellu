package com.example.hellu;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.hellu.Class.LoadingDialog;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {
    //TextInputEditText email,password;
    TextInputLayout email,password;
    Button btnLogin;
    TextView txtRegister,txtReset;
    FirebaseAuth auth;
    FirebaseUser firebaseUser;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        firebaseUser= FirebaseAuth.getInstance().getCurrentUser();
        txtReset=findViewById(R.id.txtForgotPW);
        txtRegister=findViewById(R.id.txtRegister);
        password=findViewById(R.id.password);
        email=findViewById(R.id.email);
        btnLogin=findViewById(R.id.btnLogin);
        auth=FirebaseAuth.getInstance();
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String em = email.getEditText().getText().toString().trim();
                String pw = password.getEditText().getText().toString();
                if(em.isEmpty()||pw.isEmpty())
                    Toast.makeText(LoginActivity.this,"Hãy điền đầy đủ thông tin!",Toast.LENGTH_SHORT).show();
                else{
                    final LoadingDialog loadingDialog=new LoadingDialog(LoginActivity.this,"Đang đăng nhập...");
                    loadingDialog.startDialog();
                    auth.signInWithEmailAndPassword(em,pw).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            loadingDialog.dismissDialog();
                            if(task.isSuccessful())
                            {
                                Intent intent=new Intent(LoginActivity.this,MainActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                finish();
                            }else
                            {
                                Toast.makeText(LoginActivity.this,"Đăng nhập thất bại!",Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });
        txtRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(LoginActivity.this,RegisterActivity.class);
                startActivity(intent);
            }
        });
        txtReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(LoginActivity.this,ResetPasswordActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onStart() {
        //tự động đăng nhập
        firebaseUser= FirebaseAuth.getInstance().getCurrentUser();
        if(firebaseUser!=null)
        {
            Intent intent=new Intent(LoginActivity.this,MainActivity.class);
            startActivity(intent);
            finish();
        }
        super.onStart();
    }
}
