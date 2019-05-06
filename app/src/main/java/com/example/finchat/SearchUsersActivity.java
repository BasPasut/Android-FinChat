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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
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
import com.google.firebase.database.Query;
import com.squareup.picasso.Picasso;

public class SearchUsersActivity extends AppCompatActivity {

    private Toolbar mToolbar;

    private EditText search_text;
    private ImageButton search_btn;
    private RecyclerView search_users_list;

    private DatabaseReference mUsersDatabase;
    private FirebaseUser mCurrent_user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_users);

        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        mCurrent_user = FirebaseAuth.getInstance().getCurrentUser();

        mToolbar = findViewById(R.id.search_bar_layout);
        setSupportActionBar(mToolbar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View action_bar_view = inflater.inflate(R.layout.search_custom_bar, null);
        actionBar.setCustomView(action_bar_view);

        search_text = findViewById(R.id.search_box);
        search_btn = findViewById(R.id.search_btn);
        search_users_list = findViewById(R.id.search_users_list);

        search_users_list.setHasFixedSize(true);
        search_users_list.setLayoutManager(new LinearLayoutManager(this));

        search_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String searchText = search_text.getText().toString();
                Log.d("search",searchText);
                usersSearch(searchText);
            }
        });

    }

    private void usersSearch(String searchText) {

        Query searchQuery = mUsersDatabase.orderByChild("name").startAt(searchText).endAt(searchText + "\uf8ff");

        FirebaseRecyclerOptions<Users> options = new FirebaseRecyclerOptions.Builder<Users>().setQuery(searchQuery, new SnapshotParser<Users>() {
            @NonNull
            @Override
            public Users parseSnapshot(@NonNull DataSnapshot snapshot) {
                return new Users(snapshot.child("name").getValue().toString(), snapshot.child("image").getValue().toString(),snapshot.child("status").getValue().toString(), snapshot.child("thumb_image").getValue().toString());
            }
        }).build();

        FirebaseRecyclerAdapter<Users, SearchUsersViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Users, SearchUsersViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull SearchUsersViewHolder usersViewHolder, int i, @NonNull Users users) {
                usersViewHolder.setName(users.getUser_name());
                usersViewHolder.setStatus(users.getUser_status());
                usersViewHolder.setImageProfile(users.getUser_thumb_image());

                final String user_id = getRef(i).getKey();

                usersViewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent profile_intent;

                        if(mCurrent_user.getUid() == user_id){
                            profile_intent = new Intent(SearchUsersActivity.this, ProfilesActivity.class);
                        }
                        else{
                            profile_intent = new Intent(SearchUsersActivity.this, FriendsActivity.class);
                        }

                        profile_intent.putExtra("user_id",user_id);
                        startActivity(profile_intent);
                    }
                });
            }

            @NonNull
            @Override
            public SearchUsersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.user_single_view,parent,false);
                return new SearchUsersViewHolder(view);
            }
        };

        firebaseRecyclerAdapter.startListening();
        search_users_list.setAdapter(firebaseRecyclerAdapter);
    }

    public class SearchUsersViewHolder extends RecyclerView.ViewHolder {

        View mView;

        public SearchUsersViewHolder(View itemView){
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
}
