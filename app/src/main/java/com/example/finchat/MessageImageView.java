package com.example.finchat;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

public class MessageImageView extends AppCompatActivity {

    private ImageView imageShow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_image_view);

        imageShow = (ImageView) findViewById(R.id.message_image_show);
        String message_url = getIntent().getStringExtra("message_url");

        Picasso.get().load(message_url).into(imageShow);
    }
}
