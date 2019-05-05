package com.example.finchat;

import android.app.ProgressDialog;
import android.content.Intent;
import androidx.annotation.NonNull;
import com.google.android.material.textfield.TextInputLayout;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.widget.Toolbar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class LoginActivity extends AppCompatActivity {

    private ProgressDialog mLoginProgress;
    
    private Toolbar mToolbar;

    private TextInputLayout mLoginEmail;
    private TextInputLayout mLoginPassword;

    private Button login_btn;

    private FirebaseAuth mAuth;

    private DatabaseReference mUserDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        mLoginProgress = new ProgressDialog(this);

        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users");

        mToolbar = findViewById(R.id.login_toolbar);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Login");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mLoginEmail = findViewById(R.id.login_email);
        mLoginPassword = findViewById(R.id.login_password);
        login_btn = findViewById(R.id.login_btn);

        login_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String email = mLoginEmail.getEditText().getText().toString();
                String password = mLoginPassword.getEditText().getText().toString();
    
                if(!TextUtils.isEmpty(email) || !TextUtils.isEmpty(password)){
                    
                    mLoginProgress.setTitle("Login");
                    mLoginProgress.setMessage("Please wait while we check your information.....");
                    mLoginProgress.setCanceledOnTouchOutside(false);
                    mLoginProgress.show();
                    
                    loginUser(email,password);
                    
                }

            }
        });
    }

    private void loginUser(String email, String password) {
            mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {

                    if (task.isSuccessful()) {
                        mLoginProgress.dismiss();

                        String current_user_id = mAuth.getCurrentUser().getUid();

                        String deviceToken = FirebaseInstanceId.getInstance().getToken();

                        mUserDatabase.child(current_user_id).child("device_token").setValue(deviceToken).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Intent main_intent = new Intent(LoginActivity.this, MainActivity.class);
                                    main_intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(main_intent);
                                    finish();
                                }
                            }
                        });


                    } else {
                        task.getException().printStackTrace();
                        mLoginProgress.hide();
                        Toast.makeText(LoginActivity.this, "Cannot Sign in. Please check your email, or password.", Toast.LENGTH_SHORT).show();
                    }
                }
            });

    }
}
