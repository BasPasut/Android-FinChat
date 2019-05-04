package com.example.finchat;


import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.firebase.ui.database.SnapshotParser;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;


/**
 * A simple {@link Fragment} subclass.
 */
public class FriendsFragment extends Fragment {

    private RecyclerView mFriendList;

    private DatabaseReference mFriendDatabase;
    private DatabaseReference mUserDatabase;

    private FirebaseAuth mAuth;

    private String mCurrent_user_id;

    private View mMainView;


    public FriendsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mMainView = inflater.inflate(R.layout.fragment_friends, container, false);

        mFriendList = mMainView.findViewById(R.id.friends_list);
        mAuth = FirebaseAuth.getInstance();

        mCurrent_user_id = mAuth.getCurrentUser().getUid();

        mFriendDatabase = FirebaseDatabase.getInstance().getReference().child("Friends").child(mCurrent_user_id);
        mFriendDatabase.keepSynced(true);
        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        mUserDatabase.keepSynced(true);

        mFriendList.setHasFixedSize(true);
        mFriendList.setLayoutManager(new LinearLayoutManager(getContext()));


        // Inflate the layout for this fragment
        return mMainView;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Friends> options = new FirebaseRecyclerOptions.Builder<Friends>().setQuery(mFriendDatabase, new SnapshotParser<Friends>() {
            @NonNull
            @Override
            public Friends parseSnapshot(@NonNull DataSnapshot snapshot) {
                return new Friends(snapshot.child("date").getValue().toString());
            }
        }).build();

        FirebaseRecyclerAdapter<Friends, FriendsViewHolder> friendsRecycleViewAdapter = new FirebaseRecyclerAdapter<Friends, FriendsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull FriendsViewHolder friendsViewHolder, int i, @NonNull Friends friends) {


                String list_user_id = getRef(i).getKey();

                mUserDatabase.child(list_user_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        String name = dataSnapshot.child("name").getValue().toString();
                        String thumb_image = dataSnapshot.child("thumb_image").getValue().toString();
                        String online_status = dataSnapshot.child("online").getValue().toString();

                        friendsViewHolder.setName(name);
                        friendsViewHolder.setThumbImage(thumb_image);

                        if(dataSnapshot.hasChild("online")) {
                            friendsViewHolder.setStatus(online_status);

                            if(online_status.contains("true")){
                                friendsViewHolder.setStatusText("Online");
                            }
                            else{
                                GetTimeAgo getTimeAgo = new GetTimeAgo();

                                long lastOnline = Long.parseLong(online_status);

                                String lastOnlineTime = GetTimeAgo.getTimeAgo(lastOnline, getContext());

                                try {
                                    friendsViewHolder.setStatusText(lastOnlineTime);
                                }
                                catch (NullPointerException e){
                                    friendsViewHolder.setStatusText("Online");
                                }



                            }

                        }

                        friendsViewHolder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                CharSequence[] options = new CharSequence[]{"Open Profile", "Send message"};

                                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

                                builder.setTitle("Select Options");
                                builder.setIcon(R.drawable.icon_round);
                                builder.setItems(options, (dialog, which) -> {
                                    switch(which){
                                        case 0 :
                                            Intent profile_intent = new Intent(getContext(), FriendsActivity.class);
                                            profile_intent.putExtra("user_id",list_user_id);
                                            startActivity(profile_intent);
                                            break;

                                        case 1:
                                            Intent chat_intent = new Intent(getContext(), ChatActivity.class);
                                            chat_intent.putExtra("user_id",list_user_id);
                                            chat_intent.putExtra("user_name",name);
                                            startActivity(chat_intent);
                                            break;

                                        default:
                                            break;

                                    }
                                });

                                builder.create().show();

                            }
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

            }

            @NonNull
            @Override
            public FriendsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.user_single_view,parent,false);
                return new FriendsViewHolder(view);
            }
        };

        friendsRecycleViewAdapter.startListening();
        mFriendList.setAdapter(friendsRecycleViewAdapter);
    }

    public static class FriendsViewHolder extends RecyclerView.ViewHolder {

        View mView;

        public FriendsViewHolder(@NonNull View itemView) {
            super(itemView);

            mView = itemView;
        }

        public void setStatusText(String status){
            TextView userStatusView = mView.findViewById(R.id.user_single_status);
            userStatusView.setText(status);
        }

        public void setName (String name){
            TextView userNameView = mView.findViewById(R.id.user_single_name);
            userNameView.setText(name);
        }

        public void setThumbImage(String thumb_img){
            CircleImageView userImageView = mView.findViewById(R.id.user_single_img);
            Picasso.get().load(thumb_img).placeholder(R.drawable.default_icon_v2).into(userImageView);
        }

        public void setStatus (String status){
            ImageView img_status = mView.findViewById(R.id.user_single_online_status);

            if(status.contains("true")){
                img_status.setVisibility(View.VISIBLE);
            }
            else{
                img_status.setVisibility(View.INVISIBLE);
            }
        }
    }

}


