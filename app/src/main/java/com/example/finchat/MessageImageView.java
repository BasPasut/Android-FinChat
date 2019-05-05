package com.example.finchat;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

public class MessageImageView extends AppCompatActivity {

    private ImageView imageShow;
    private ImageButton shared_btn,download_btn;
    private TextView sender_name,sender_time;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_image_view);
        Picasso.get().setIndicatorsEnabled(false);

        imageShow = (ImageView) findViewById(R.id.message_image_show);
        shared_btn = (ImageButton) findViewById(R.id.shared_btn);
        download_btn = (ImageButton) findViewById(R.id.download_btn);
        sender_name = (TextView) findViewById(R.id.sender_name);
        sender_time = (TextView) findViewById(R.id.sender_time);

        String message_url = getIntent().getStringExtra("message_url");
        String senderName = getIntent().getStringExtra("sender_name");
        String senderTime = getIntent().getStringExtra("sender_time");
        String finalFileName = getIntent().getStringExtra("filename");

        sender_name.setText(senderName);
        sender_time.setText(senderTime);
        Picasso.get().load(message_url).into(imageShow);

        download_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MessageImageView.this,"Start Downloading. Please wait", Toast.LENGTH_SHORT).show();
                new DownloadFileFromURL(MessageImageView.this).execute(message_url,"/Images/",finalFileName);
            }
        });

        shared_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent share = new Intent(Intent.ACTION_SEND);
                share.setType("text/plain");
                share.putExtra(Intent.EXTRA_TEXT, message_url);

                startActivity(Intent.createChooser(share, "Choose share destination"));
            }
        });
    }
}
