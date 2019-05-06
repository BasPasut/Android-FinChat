package com.example.finchat;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;


public class SplashGIFActivity extends AppCompatActivity {

    ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_gif);

        imageView = (ImageView) findViewById(R.id.imageView);
        //Typeface custom_font = Typeface.createFromAsset(getAssets(),  "/fonts/ATL.ttf");

        //welcome_txt.setTypeface(custom_font);

        Glide.with(getApplicationContext()).load(R.drawable.gif_splash).into(imageView);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent main_intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(main_intent);
                finish();
            }
        },1500);

    }
}
