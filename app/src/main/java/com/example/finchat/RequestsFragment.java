package com.example.finchat;


import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;

import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


/**
 * A simple {@link Fragment} subclass.
 */
public class RequestsFragment extends Fragment {

    private View RequestFragmentView;
    private RecyclerView request_list;

    private DatabaseReference friendReqRef, UsersRef, mRootDatabase;
    private FirebaseAuth mAuth;
    private String mCurrentuser_id;


    public RequestsFragment() {
        // Required empty public constructor

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        RequestFragmentView = inflater.inflate(R.layout.fragment_requests, container, false);

        mAuth = FirebaseAuth.getInstance();
        mCurrentuser_id = mAuth.getCurrentUser().getUid();
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        friendReqRef = FirebaseDatabase.getInstance().getReference().child("Friend_req");
        mRootDatabase = FirebaseDatabase.getInstance().getReference();


        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);

        request_list = (RecyclerView) RequestFragmentView.findViewById(R.id.request_list);
        request_list.setHasFixedSize(true);
        request_list.setLayoutManager(linearLayoutManager);


        // Inflate the layout for this fragment
        return RequestFragmentView;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Request> options = new FirebaseRecyclerOptions.Builder<Request>()
                .setQuery(friendReqRef.child(mCurrentuser_id), Request.class).build();

        FirebaseRecyclerAdapter<Request, RequestViewHolder> adapter = new FirebaseRecyclerAdapter<Request, RequestViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull RequestViewHolder requestViewHolder, int i, @NonNull Request request) {
                requestViewHolder.itemView.findViewById(R.id.request_accept_btn).setVisibility(View.VISIBLE);
                requestViewHolder.itemView.findViewById(R.id.request_decline_btn).setVisibility(View.VISIBLE);

                String list_user_id = getRef(i).getKey();

                DatabaseReference getTypeRef = getRef(i).child("request_type").getRef();
                getTypeRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()){
                            String type = dataSnapshot.getValue().toString();

                            if(type.contains("received")){
                                Button request_accept_btn = requestViewHolder.itemView.findViewById(R.id.request_accept_btn);
                                request_accept_btn.setText("Accept");

                                UsersRef.child(list_user_id).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        if(dataSnapshot.hasChild("thumb_image")){

                                            String from_user_image = dataSnapshot.child("thumb_image").getValue().toString();

                                            requestViewHolder.setImage(from_user_image);
                                        }

                                        String from_user_name = dataSnapshot.child("name").getValue().toString();

                                        requestViewHolder.setName(from_user_name);
                                        requestViewHolder.setStatus(from_user_name + " want to add you as a friend.");

                                        requestViewHolder.itemView.findViewById(R.id.request_accept_btn).setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                final String currentDate = DateFormat.getDateTimeInstance().format(new Date());

                                                Map friendsMap = new HashMap();
                                                friendsMap.put("Friends/" + mCurrentuser_id + "/" + list_user_id + "/date", currentDate);
                                                friendsMap.put("Friends/" + list_user_id + "/" + mCurrentuser_id + "/date", currentDate);

                                                Map deleteFriendReq = new HashMap();
                                                deleteFriendReq.put("Friend_req/" + mCurrentuser_id + "/" + list_user_id,null);
                                                deleteFriendReq.put("Friend_req/" + list_user_id + "/" + mCurrentuser_id, null);

                                                mRootDatabase.updateChildren(friendsMap, new DatabaseReference.CompletionListener() {
                                                    @Override
                                                    public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                                                        if(databaseError == null){
                                                            Toast.makeText(getContext(),from_user_name + " will be added to your friends list.", Toast.LENGTH_LONG).show();
                                                        }
                                                        else{
                                                            String error = databaseError.getMessage();
                                                            Toast.makeText(getContext(),error, Toast.LENGTH_LONG).show();
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
                                                            Toast.makeText(getContext(),error, Toast.LENGTH_LONG).show();
                                                        }
                                                    }
                                                });
                                            }
                                        });

                                        requestViewHolder.itemView.findViewById(R.id.request_decline_btn).setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                Map declineFriendMap = new HashMap();
                                                declineFriendMap.put("Friend_req/" + mCurrentuser_id + "/" + list_user_id, null);
                                                declineFriendMap.put("Friend_req/" + list_user_id + "/" + mCurrentuser_id, null);

                                                mRootDatabase.updateChildren(declineFriendMap, new DatabaseReference.CompletionListener() {
                                                    @Override
                                                    public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                                                        if(databaseError == null){

                                                        }
                                                        else{
                                                            String error = databaseError.getMessage();
                                                            Toast.makeText(getContext(),error, Toast.LENGTH_LONG).show();
                                                        }

                                                    }
                                                });
                                            }
                                        });
                                }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });
                            }

                            else if (type.contains("sent")){
                                Button request_sent_btn = requestViewHolder.itemView.findViewById(R.id.request_accept_btn);
                                request_sent_btn.setText("Cancel");
                                request_sent_btn.setBackgroundColor(Color.RED);
                                request_sent_btn.setTextColor(Color.WHITE);

                                requestViewHolder.itemView.findViewById(R.id.request_decline_btn).setVisibility(View.INVISIBLE);

                                UsersRef.child(list_user_id).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        if(dataSnapshot.hasChild("thumb_image")){

                                            final String from_user_image = dataSnapshot.child("thumb_image").getValue().toString();

                                            requestViewHolder.setImage(from_user_image);
                                        }

                                        final String from_user_name = dataSnapshot.child("name").getValue().toString();

                                        requestViewHolder.setName(from_user_name);
                                        requestViewHolder.setStatus("You have sent a request to " + from_user_name);

                                        request_sent_btn.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                Map cancelFriendMap = new HashMap();
                                                cancelFriendMap.put("Friend_req/" + mCurrentuser_id + "/" + list_user_id, null);
                                                cancelFriendMap.put("Friend_req/" + list_user_id + "/" + mCurrentuser_id, null);

                                                mRootDatabase.updateChildren(cancelFriendMap, new DatabaseReference.CompletionListener() {
                                                    @Override
                                                    public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                                                        if(databaseError == null){
                                                            Toast.makeText(getContext(),"Cancel friend request success.", Toast.LENGTH_LONG).show();
                                                        }
                                                        else{
                                                            String error = databaseError.getMessage();
                                                            Toast.makeText(getContext(),error, Toast.LENGTH_LONG).show();
                                                        }
                                                    }
                                                });
                                            }
                                        });
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });

                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

            }

            @NonNull
            @Override
            public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.request_single_view,parent,false);
                return new RequestViewHolder(view);
            }
        };

        adapter.startListening();
        request_list.setAdapter(adapter);
    }

    public static class RequestViewHolder extends RecyclerView.ViewHolder {

        View mView;

        public RequestViewHolder(View viewItem){
            super(viewItem);

            mView = itemView;
        }

        public void setName(String name){
            TextView from_user_name = (TextView) mView.findViewById(R.id.request_single_name);
            from_user_name.setText(name);
        }

        public void setStatus (String status){
            TextView request_status = (TextView) mView.findViewById(R.id.request_single_status);
            request_status.setText(status);
        }

        public void setImage(String image){
            CircleImageView from_user_img = (CircleImageView) mView.findViewById(R.id.request_single_img);
            Picasso.get().load(image).into(from_user_img);
        }

    }
}
