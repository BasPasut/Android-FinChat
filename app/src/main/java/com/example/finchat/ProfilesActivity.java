package com.example.finchat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class ProfilesActivity extends AppCompatActivity {

    private DatabaseReference mUserDatabase;
    private FirebaseUser mUser;
    private StorageReference mImageStorage;

    //Profile layout
    private CircleImageView mDisplayImage;
    private TextView mName;
    private TextView mStatus;
    private Button mStatusButton,mImageButton;

    private ProgressDialog mProgressDialog;

    private static final int GALLERY_PICK = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profiles);

        mDisplayImage = findViewById(R.id.profile_img);
        mName = findViewById(R.id.profile_name);
        mStatus = findViewById(R.id.profile_status);
        mStatusButton = findViewById(R.id.profile_cStatus);
        mImageButton = findViewById(R.id.profile_cImage);


        mProgressDialog = new ProgressDialog(this);

        mUser = FirebaseAuth.getInstance().getCurrentUser();
        mImageStorage = FirebaseStorage.getInstance().getReference();

        String user_id = mUser.getUid();

        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(user_id);
        mUserDatabase.keepSynced(true);


        mUserDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

               String profile_name = dataSnapshot.child("name").getValue().toString();
               final String profile_image = dataSnapshot.child("image").getValue().toString();
               String profile_status = dataSnapshot.child("status").getValue().toString();
               String profile_thumb_image = dataSnapshot.child("thumb_image").getValue().toString();

               mName.setText(profile_name);
               mStatus.setText(profile_status);


               if(!profile_image.equals("default")) {
                   //Picasso.get().load(profile_image).placeholder(R.drawable.default_avatar).into(mDisplayImage);
                   Picasso.get().load(profile_image).networkPolicy(NetworkPolicy.OFFLINE)
                           .placeholder(R.drawable.default_icon_v2).into(mDisplayImage, new Callback() {
                       @Override
                       public void onSuccess() {

                       }

                       @Override
                       public void onError(Exception e) {
                           Picasso.get().load(profile_image).placeholder(R.drawable.default_icon_v2).into(mDisplayImage);
                       }
                   });
               }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        mStatusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String status_value = mStatus.getText().toString();

                Intent status_intent = new Intent(ProfilesActivity.this, StatusActivity.class);
                status_intent.putExtra("status_value",status_value);
                startActivity(status_intent);
            }
        });

        mImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

//                CropImage.activity()
//                        .setGuidelines(CropImageView.Guidelines.ON)
//                        .setAspectRatio(1,1)
//                        .start(ProfilesActivity.this);
                OpenGallery();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        // Check if user is signed in (non-null) and update UI accordingly.
        if(mUser != null){
            mUserDatabase.child("online").setValue("true");
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if(mUser != null) {

            mUserDatabase.child("online").setValue(ServerValue.TIMESTAMP);
        }
    }

    private void OpenGallery() {
        Intent gallery_intent = new Intent();
        gallery_intent.setAction(Intent.ACTION_PICK);
        gallery_intent.setType("image/*");
        startActivityForResult(gallery_intent,GALLERY_PICK);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == GALLERY_PICK && resultCode == RESULT_OK){

            Uri imageUri = data.getData();
            CropImage.activity(imageUri)
                    .setAspectRatio(1,1)
                    .start(this);

        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {

            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK) {

                mProgressDialog.setTitle("Uploading your profile image");
                mProgressDialog.setMessage("Please wait while we uploading your image profile.....");
                mProgressDialog.setCanceledOnTouchOutside(false);
                mProgressDialog.show();

                Uri resultUri = result.getUri();

                File thumb_filePath = new File(resultUri.getPath());

                String user_id = mUser.getUid();

                Bitmap thumb_bitmap = null;

                try {
                    thumb_bitmap = new Compressor(this)
                            .setMaxWidth(200)
                            .setMaxHeight(200)
                            .setQuality(75)
                            .compressToBitmap(thumb_filePath);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                thumb_bitmap.compress(Bitmap.CompressFormat.JPEG,100,baos);
                final byte[] thumb_byte = baos.toByteArray();


                Log.d("testbugUri",resultUri.toString());

                final StorageReference filepath = mImageStorage.child("profile_images").child(user_id +".jpg");
                final StorageReference thumb_filepath = mImageStorage.child("profile_images").child("thumbs").child(user_id + ".jpg");

                filepath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if(task.isSuccessful()) {
                            filepath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    final Uri download_uri = uri;
                                    UploadTask uploadTask = thumb_filepath.putBytes(thumb_byte);
                                    uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> thumb_task) {
                                            thumb_task.getResult().getStorage().getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                @Override
                                                public void onSuccess(Uri uri) {
                                                        Map update_HashMap = new HashMap();
                                                        update_HashMap.put("image",download_uri.toString());
                                                        update_HashMap.put("thumb_image", uri.toString());

                                                        mUserDatabase.updateChildren(update_HashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if(task.isSuccessful()){
                                                                    mProgressDialog.dismiss();
                                                                    Toast.makeText(ProfilesActivity.this, "Success", Toast.LENGTH_LONG).show();
                                                                }
                                                                else{
                                                                    Toast.makeText(ProfilesActivity.this, "Error, uploading thumbnail", Toast.LENGTH_LONG).show();
                                                                }
                                                            }
                                                        });
                                                }
                                            });

                                        }
                                    });


                                }
                            });
                        }
                        else{
                            Toast.makeText(ProfilesActivity.this, "Failed", Toast.LENGTH_LONG).show();
                            mProgressDialog.dismiss();
                        }
                    }
                });

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }
}
