package com.example.finchat;

import android.app.ProgressDialog;
import androidx.annotation.NonNull;
import com.google.android.material.textfield.TextInputLayout;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class StatusActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private ProgressDialog mProgressDialog;

    private TextInputLayout mStatus;
    private Button mSavebtn;

    private DatabaseReference mDatabase;
    private FirebaseUser mUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);

        mUser = FirebaseAuth.getInstance().getCurrentUser();
        String user_id = mUser.getUid();

        mDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(user_id);

        mToolbar = findViewById(R.id.status_bar);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Account Status");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        String status_value = getIntent().getStringExtra("status_value");

        //Progress Dialog
        mProgressDialog = new ProgressDialog(this);

        mStatus = findViewById(R.id.status_input);
        mSavebtn = findViewById(R.id.status_save_btn);

        mStatus.getEditText().setText(status_value);

        mSavebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mProgressDialog.setTitle("Updating Status");
                mProgressDialog.setMessage("Please wait while we updating your mood TD......");
                mProgressDialog.show();

                String status = mStatus.getEditText().getText().toString();
                mDatabase.child("status").setValue(status).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            mProgressDialog.dismiss();
                        }
                        else{
                            Toast.makeText(getApplicationContext(), "Error, pls check your input",Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        });
    }
}
