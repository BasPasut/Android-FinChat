package com.example.finchat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.firebase.ui.database.SnapshotParser;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.squareup.picasso.Picasso;

public class UsersActivity extends AppCompatActivity {

    private Toolbar mToolbar;

    private RecyclerView mUsersList;

    private DatabaseReference mUsersDatabase;
    private FirebaseUser mCurrent_user;

    private ImageButton user_search_btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);


        mToolbar = findViewById(R.id.friends_app_bar);
        setSupportActionBar(mToolbar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Users List");
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        user_search_btn = findViewById(R.id.user_search_btn);

        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        mUsersDatabase.keepSynced(true);
        mCurrent_user = FirebaseAuth.getInstance().getCurrentUser();

        mUsersList = findViewById(R.id.users_list);
        mUsersList.setHasFixedSize(true);
        mUsersList.setLayoutManager(new LinearLayoutManager(this));

        user_search_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent search_intent = new Intent(UsersActivity.this,SearchUsersActivity.class);
                startActivity(search_intent);
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();

        if(mCurrent_user != null){
            mUsersDatabase.child(mCurrent_user.getUid()).child("online").setValue("true");
        }

        FirebaseRecyclerOptions<Users> options = new FirebaseRecyclerOptions.Builder<Users>().setQuery(mUsersDatabase, new SnapshotParser<Users>() {
            @NonNull
            @Override
            public Users parseSnapshot(@NonNull DataSnapshot snapshot) {
                 return new Users(snapshot.child("name").getValue().toString(), snapshot.child("image").getValue().toString(),snapshot.child("status").getValue().toString(), snapshot.child("thumb_image").getValue().toString());
            }
        }).build();

        FirebaseRecyclerAdapter<Users, UsersViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Users, UsersViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull UsersViewHolder usersViewHolder, int i, @NonNull Users users) {
                usersViewHolder.setName(users.getUser_name());
                usersViewHolder.setStatus(users.getUser_status());
                usersViewHolder.setImageProfile(users.getUser_thumb_image());

                final String user_id = getRef(i).getKey();

                usersViewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Intent profile_intent;

                        if(mCurrent_user.getUid() == user_id){
                            profile_intent = new Intent(UsersActivity.this, ProfilesActivity.class);
                        }
                        else{
                            profile_intent = new Intent(UsersActivity.this, FriendsActivity.class);
                        }
                        profile_intent.putExtra("user_id",user_id);
                        startActivity(profile_intent);
                    }
                });
            }

            @NonNull
            @Override
            public UsersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.user_single_view,parent,false);
                return new UsersViewHolder(view);
            }
        };

        firebaseRecyclerAdapter.startListening();
        mUsersList.setAdapter(firebaseRecyclerAdapter);
    }

    public static class UsersViewHolder extends RecyclerView.ViewHolder {

        View mView;

        public UsersViewHolder(@NonNull View itemView) {
            super(itemView);

            mView = itemView;
        }

        public void setName(String name){
            TextView userNameView = mView.findViewById(R.id.user_single_name);
            userNameView.setText(name);
        }

        public void setStatus(String status){
            TextView userStatusView = mView.findViewById(R.id.user_single_status);
            userStatusView.setText(status);
        }

        public void setImageProfile(String user_thumb_image) {

            CircleImageView userImageView = mView.findViewById(R.id.user_single_img);
            Picasso.get().load(user_thumb_image).placeholder(R.drawable.default_icon_v2).into(userImageView);
        }

    }

    @Override
    protected void onPause() {
        super.onPause();

        if(mCurrent_user != null) {

            mUsersDatabase.child(mCurrent_user.getUid()).child("online").setValue(ServerValue.TIMESTAMP);
        }
    }
}
