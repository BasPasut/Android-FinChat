package com.example.finchat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import de.hdodenhof.circleimageview.CircleImageView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class FriendsActivity extends AppCompatActivity {

    private CircleImageView mFriendImg;
    private TextView mFriendname,mFriendStatus,mProfileFriendsCount;
    private Button mFriendRequest,mFriendCancel;

    private DatabaseReference mUsersDatabase;

    private ProgressDialog mProgresDialog;

    private DatabaseReference mFriendRequestDatabase;
    private DatabaseReference mFriendDatabase;
    private DatabaseReference mNotificationDatabase;
    private DatabaseReference mRootDatabase;
    private DatabaseReference mCurrentUserDatabase;


    private FirebaseUser mCurrent_user;

    String message_url;
    String sender_name;

    // 0 -> not friend
    // 1 -> request_sent
    // 2 -> request_received
    // 3 -> friends
    private int current_state;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);

        final String user_id;
        String data = getIntent().getStringExtra("user_id");
        if(data == null){
            user_id= getIntent().getStringExtra("from_user_id");
        }else{
            user_id = getIntent().getStringExtra("user_id");
        }

        mRootDatabase = FirebaseDatabase.getInstance().getReference();
        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(user_id);
        mUsersDatabase.keepSynced(true);
        mFriendRequestDatabase = FirebaseDatabase.getInstance().getReference().child("Friend_req");
        mFriendRequestDatabase.keepSynced(true);
        mFriendDatabase = FirebaseDatabase.getInstance().getReference().child("Friends");
        mFriendDatabase.keepSynced(true);
        mNotificationDatabase = FirebaseDatabase.getInstance().getReference().child("notifications");
        mNotificationDatabase.keepSynced(true);


        mCurrent_user = FirebaseAuth.getInstance().getCurrentUser();
        mCurrentUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(mCurrent_user.getUid());
        mCurrentUserDatabase.keepSynced(true);

        mFriendImg = findViewById(R.id.friend_img);
        mFriendname = findViewById(R.id.friend_name);
        mFriendStatus = findViewById(R.id.friend_status);
        mFriendRequest = findViewById(R.id.friend_request_btn);
        mFriendCancel = findViewById(R.id.friend_cancel_btn);

        mProgresDialog = new ProgressDialog(this);
        mProgresDialog.setTitle("Loading Friend Profile");
        mProgresDialog.setMessage("Please wait while we loading your friend data......");
        mProgresDialog.setCanceledOnTouchOutside(false);
        mProgresDialog.show();

        current_state = 0;

        mFriendCancel.setVisibility(View.INVISIBLE);
        mFriendCancel.setEnabled(false);

        mFriendImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FriendsActivity.this,MessageImageView.class);
                intent.putExtra("message_url",message_url);
                intent.putExtra("sender_name",sender_name);
                intent.putExtra("sender_time","");
                intent.putExtra("filename",getFileName(message_url));
                startActivity(intent);
            }
        });

        mUsersDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String display_name = dataSnapshot.child("name").getValue().toString();
                String status = dataSnapshot.child("status").getValue().toString();
                String image = dataSnapshot.child("image").getValue().toString();

                sender_name = display_name;
                message_url = image;

                mFriendname.setText(display_name);
                mFriendStatus.setText(status);

                Picasso.get().load(image).placeholder(R.drawable.default_icon_v2).into(mFriendImg);

                if(mCurrent_user.getUid().equals(user_id)){

                    mFriendCancel.setEnabled(false);
                    mFriendCancel.setVisibility(View.INVISIBLE);

                    mFriendRequest.setEnabled(false);
                    mFriendRequest.setVisibility(View.INVISIBLE);

                }

                mFriendRequestDatabase.child(mCurrent_user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if(dataSnapshot.hasChild(user_id)){
                            String request_type = dataSnapshot.child(user_id).child("request_type").getValue().toString();

                            if(request_type.equals("received")){
                                current_state = 2;
                                mFriendRequest.setText("Accept Friend Request");

                                mFriendCancel.setVisibility(View.VISIBLE);
                                mFriendCancel.setEnabled(true);
                            }
                            else if(request_type.equals("sent")){
                                current_state = 1;
                                mFriendRequest.setText("Cancel Friend Request");

                                mFriendCancel.setVisibility(View.INVISIBLE);
                                mFriendCancel.setEnabled(false);
                            }
                            mProgresDialog.dismiss();
                        }

                        else {
                            mFriendDatabase.child(mCurrent_user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                    if(dataSnapshot.hasChild(user_id)){

                                        current_state = 3;
                                        mFriendRequest.setText("Unfriend");

                                        mFriendCancel.setVisibility(View.INVISIBLE);
                                        mFriendCancel.setEnabled(false);
                                    }
                                    mProgresDialog.dismiss();
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                    mProgresDialog.dismiss();
                                }
                            });
                        }


                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        mFriendRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mFriendRequest.setEnabled(false);

                // not friend state
                if(current_state == 0){

                    DatabaseReference newNotificationRef = mRootDatabase.child("notifications").child(user_id).push();
                    String newNotificationId = newNotificationRef.getKey();

                    HashMap<String, String> notifications_data = new HashMap<>();
                    notifications_data.put("from", mCurrent_user.getUid());
                    notifications_data.put("type", "request");

                    Map requestMap = new HashMap<>();
                    requestMap.put("Friend_req/" + mCurrent_user.getUid() + "/" + user_id + "/request_type", "sent" );
                    requestMap.put("Friend_req/" + user_id+"/" + mCurrent_user.getUid() + "/request_type", "received");
                    requestMap.put("notifications/"+ user_id + "/" + newNotificationId, notifications_data);

                    mRootDatabase.updateChildren(requestMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {

                            if(databaseError != null){
                                Toast.makeText(FriendsActivity.this, "There was some error.",Toast.LENGTH_LONG).show();
                            }

                            else{
                                current_state = 1;
                                mFriendRequest.setText("Cancel Friend Request");
                            }

                             mFriendRequest.setEnabled(true);
                        }
                    });
                }

                // cancel request state
                if(current_state == 1){

                    mFriendRequestDatabase.child(mCurrent_user.getUid()).child(user_id).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                            mFriendRequestDatabase.child(user_id).child(mCurrent_user.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {

                                    mFriendRequest.setEnabled(true);
                                    current_state = 0;
                                    mFriendRequest.setText("Send Friend Request");

                                    mFriendCancel.setVisibility(View.INVISIBLE);
                                    mFriendCancel.setEnabled(false);
                                }
                            });
                        }
                    });
                }

                // request received state
                if(current_state == 2){

                    final String currentDate = DateFormat.getDateTimeInstance().format(new Date());

                    Map friendsMap = new HashMap();
                    friendsMap.put("Friends/" + mCurrent_user.getUid() + "/" + user_id + "/date", currentDate);
                    friendsMap.put("Friends/" + user_id + "/" + mCurrent_user.getUid() + "/date", currentDate);

                    Map deleteFriendReq = new HashMap();
                    deleteFriendReq.put("Friend_req/" + mCurrent_user.getUid() + "/" + user_id , null);
                    deleteFriendReq.put("Friend_req/" + user_id + "/" + mCurrent_user.getUid() , null);

                    mRootDatabase.updateChildren(friendsMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                            if(databaseError == null){
                                mFriendRequest.setEnabled(true);
                                current_state = 3;
                                mFriendRequest.setText("UnFriend " + mFriendname.getText().toString());

                                mFriendCancel.setVisibility(View.INVISIBLE);
                                mFriendCancel.setEnabled(false);
                            }
                            else{
                                String error = databaseError.getMessage();
                                Toast.makeText(FriendsActivity.this,error, Toast.LENGTH_LONG).show();
                            }

                        }
                    });

                    mRootDatabase.updateChildren(deleteFriendReq, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                            if(databaseError == null){
                            }
                            else{
                                String error = databaseError.getMessage();
                                Toast.makeText(FriendsActivity.this,error, Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                }

                // Unfriend state
                if(current_state == 3) {
                    Map unFriendMap = new HashMap();
                    unFriendMap.put("Friends/" + mCurrent_user.getUid() + "/" + user_id, null);
                    unFriendMap.put("Friends/" + user_id + "/" + mCurrent_user.getUid(), null);

                    Map hideChat = new HashMap();
                    hideChat.put("Chat/" + mCurrent_user.getUid() + "/" + user_id, null);
                    hideChat.put("Chat/" + user_id + "/" + mCurrent_user.getUid(), null);

                    mRootDatabase.updateChildren(hideChat);

                    mRootDatabase.updateChildren(unFriendMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                            if(databaseError == null){
                                current_state = 0;
                                mFriendRequest.setText("Send Friend Request");

                                mFriendCancel.setVisibility(View.INVISIBLE);
                                mFriendCancel.setEnabled(false);
                            }
                            else{
                                String error = databaseError.getMessage();
                                Toast.makeText(FriendsActivity.this,error, Toast.LENGTH_LONG).show();
                            }

                            mFriendRequest.setEnabled(true);
                        }
                    });
                }
            }
        });

        mFriendCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // decline friend request
                if(current_state == 2) {
                    Map declineFriendMap = new HashMap();
                    declineFriendMap.put("Friend_req/" + mCurrent_user.getUid() + "/" + user_id, null);
                    declineFriendMap.put("Friend_req/" + user_id + "/" + mCurrent_user.getUid(), null);

                    mRootDatabase.updateChildren(declineFriendMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                            if(databaseError == null){
                                mFriendRequest.setEnabled(true);
                                current_state = 0;
                                mFriendRequest.setText("Send Friend Request");

                                mFriendCancel.setVisibility(View.VISIBLE);
                                mFriendCancel.setEnabled(true);
                            }
                            else{
                                String error = databaseError.getMessage();
                                Toast.makeText(FriendsActivity.this,error, Toast.LENGTH_LONG).show();
                            }

                            mFriendRequest.setEnabled(true);
                        }
                    });
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        if(mCurrent_user != null){
            mCurrentUserDatabase.child("online").setValue(true);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(mCurrent_user != null){
            mCurrentUserDatabase.child("online").setValue(ServerValue.TIMESTAMP);
        }
    }

    public String getFileName(String message_url){
        String fileName = message_url.substring(message_url.lastIndexOf('/'));
        String[] fileNameSplit = fileName.split("\\?");
        String finalFileName = fileNameSplit[0].substring(15);
        return finalFileName;
    }
}
