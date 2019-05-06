package com.example.finchat;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public class MessageImageView extends AppCompatActivity {

    private ImageView imageShow;
    private ImageButton shared_btn,download_btn;
    private TextView sender_name,sender_time;

    private final int STORAGE_EXTERNAL_PERMISSION = 1;
    String mPermission = Manifest.permission.WRITE_EXTERNAL_STORAGE;
    final int REQUEST_CODE_PERMISSION = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_image_view);
        Picasso.get().setIndicatorsEnabled(false);

        imageShow = findViewById(R.id.message_image_show);
        shared_btn = findViewById(R.id.shared_btn);
        download_btn = findViewById(R.id.download_btn);
        sender_name = findViewById(R.id.sender_name);
        sender_time = findViewById(R.id.sender_time);

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
                try{
                    if(ActivityCompat.checkSelfPermission(MessageImageView.this, mPermission) != PERMISSION_GRANTED){
                        ActivityCompat.requestPermissions(MessageImageView.this, new String[]{mPermission},REQUEST_CODE_PERMISSION);
                    }
                    else{
                        Toast.makeText(MessageImageView.this, "Start Downloading. Please wait", Toast.LENGTH_SHORT).show();
                        new DownloadFileFromURL(MessageImageView.this).execute(message_url, "/Images/", finalFileName);
                    }
                }
                catch (Exception e){
                    e.printStackTrace();
                }

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
