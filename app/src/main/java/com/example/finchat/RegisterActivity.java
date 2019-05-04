package com.example.finchat;

import android.app.ProgressDialog;
import android.content.Intent;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import androidx.appcompat.widget.Toolbar;

import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthEmailException;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class RegisterActivity extends AppCompatActivity {

    private TextInputLayout mDisplayName;
    private TextInputLayout mEmail;
    private TextInputLayout mPassword;
    private Button mCreate_acc;
    private Toolbar mToolbar;

    List<String> user_list;


    //ProgressDialog
    private ProgressDialog mRegProgress;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private DatabaseReference mUserDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();

        mDisplayName = findViewById(R.id.reg_name);
        mEmail = findViewById(R.id.reg_email);
        mPassword = findViewById(R.id.reg_password);
        mCreate_acc = findViewById(R.id.create_account_btn);

        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users");

        mRegProgress = new ProgressDialog(this);

        user_list = new ArrayList<>();

        mToolbar = findViewById(R.id.register_toolbar);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Create Account");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mUserDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot d : dataSnapshot.getChildren()){
                    try {
                        String user_name = d.child("name").getValue().toString();
                        user_list.add(user_name);
                    }
                    catch (NullPointerException e){
                        e.printStackTrace();
                    }
                }
            }


            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        mCreate_acc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String display_name = mDisplayName.getEditText().getText().toString().trim();
                String email = mEmail.getEditText().getText().toString();
                String password = mPassword.getEditText().getText().toString();

                if (!TextUtils.isEmpty(display_name) && !TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)) {
                    mRegProgress.setTitle("Registering User");
                    mRegProgress.setMessage("Please wait while we create your account !");
                    mRegProgress.setCanceledOnTouchOutside(false);
                    mRegProgress.show();
                    //Log.d("test",display_name);
                    register_user(display_name, email, password);
                }
                else{
                    Toast.makeText(RegisterActivity.this, "Please fill all information for registration.", Toast.LENGTH_SHORT).show();
                }

            }
        });
    }

    private void register_user(String display_name, String email, String password) {

        if(user_list.contains(display_name)){
            Toast.makeText(RegisterActivity.this, "This username is already used. Please change.", Toast.LENGTH_SHORT).show();
            mRegProgress.dismiss();
        }
        else{
            Toast.makeText(RegisterActivity.this, "Correct.", Toast.LENGTH_SHORT).show();
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {

                                FirebaseUser current_user = FirebaseAuth.getInstance().getCurrentUser();
                                String user_id = "";
                                if (current_user != null) {
                                    user_id = current_user.getUid();
                                }

                                mDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(user_id);

                                FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(new OnSuccessListener<InstanceIdResult>() {
                                    @Override
                                    public void onSuccess(InstanceIdResult instanceIdResult) {
                                        String deviceToken = instanceIdResult.getToken();

                                        HashMap<String, String> userMap = new HashMap<>();
                                        userMap.put("name", display_name);
                                        userMap.put("status", "Hello world!!");
                                        userMap.put("image", "default");
                                        userMap.put("thumb_image", "default");
                                        userMap.put("device_token", deviceToken);

                                        mDatabase.setValue(userMap).addOnCompleteListener(task1 -> {
                                            if (task1.isSuccessful()) {
                                                Intent main_intent = new Intent(RegisterActivity.this, MainActivity.class);
                                                startActivity(main_intent);
                                                finish();
                                            }
                                        });
                                    }
                                });
                            } else {
                                if (task.getException() instanceof FirebaseAuthWeakPasswordException) {
                                    mRegProgress.dismiss();
                                    Toast.makeText(RegisterActivity.this, "Weak password !!", Toast.LENGTH_LONG).show();
                                } else if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                                    mRegProgress.dismiss();
                                    Toast.makeText(RegisterActivity.this, "Already Existing user with this email", Toast.LENGTH_LONG).show();
                                } else if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                    mRegProgress.dismiss();
                                    Toast.makeText(RegisterActivity.this, "Invalid Email", Toast.LENGTH_LONG).show();
                                } else {
                                    mRegProgress.dismiss();
                                    Toast.makeText(RegisterActivity.this, "Unknown Error", Toast.LENGTH_LONG).show();
                                    task.getException().printStackTrace();
                                }

                            }
                        }

                    });
        }


    }
}
