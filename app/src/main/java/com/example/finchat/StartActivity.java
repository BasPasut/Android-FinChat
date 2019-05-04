package com.example.finchat;

import android.content.Intent;
import android.graphics.Typeface;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class StartActivity extends AppCompatActivity {

    private Button mRegBtn,mLogBtn;
    private TextView start_text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        mRegBtn = findViewById(R.id.register_btn);
        mLogBtn = findViewById(R.id.login_btn);
        start_text = findViewById(R.id.start_text);

        Typeface custom_font = Typeface.createFromAsset(getAssets(),  "fonts/ATL.ttf");

        start_text.setTypeface(custom_font);
        mLogBtn.setTypeface(custom_font);
        mRegBtn.setTypeface(custom_font);

        mRegBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent register_intent = new Intent(StartActivity.this, RegisterActivity.class);
                startActivity(register_intent);
            }
        });

        mLogBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent login_intent = new Intent(StartActivity.this, LoginActivity.class);
                startActivity(login_intent);
            }
        });
    }
}
