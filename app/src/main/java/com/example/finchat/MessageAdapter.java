package com.example.finchat;

import android.graphics.Color;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder>{
        private List<Messages> mMessageList;
        private DatabaseReference mUserDatabase;
        private FirebaseAuth mAuth;

        public MessageAdapter(List<Messages> mMessageList) {

            this.mMessageList = mMessageList;

        }

        @Override
        public MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.message_single_receive,parent, false);

            return new MessageViewHolder(v);

        }

        public class MessageViewHolder extends RecyclerView.ViewHolder {

            public TextView messageText;
            public CircleImageView profileImage;
            public TextView displayName;
            public ImageView messageImage;
            public TextView timestampText;

            View mView;

            public MessageViewHolder(View view) {
                super(view);

                messageText = view.findViewById(R.id.message_text_receive);
                profileImage = view.findViewById(R.id.message_profile_receive);
                displayName = view.findViewById(R.id.message_name_receive);
                messageImage = view.findViewById(R.id.message_image_receive);
                timestampText = view.findViewById(R.id.message_seen_receive);

            }
        }

        @Override
        public void onBindViewHolder(final MessageViewHolder viewHolder, int i) {

            mAuth = FirebaseAuth.getInstance();

            String current_user_id = mAuth.getCurrentUser().getUid();

            Messages c = mMessageList.get(i);

            Long message_time = c.getTime();
            String from_user = c.getFrom();
            String message_type = c.getType();


            Calendar cal = Calendar.getInstance(Locale.ENGLISH);
            cal.setTimeInMillis(message_time);
            String date = DateFormat.format("dd-MM-yyyy hh:mm:ss", cal).toString();

            viewHolder.timestampText.setText(date);

            mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(from_user);

            mUserDatabase.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    String name = dataSnapshot.child("name").getValue().toString();
                    String image = dataSnapshot.child("thumb_image").getValue().toString();

                    viewHolder.displayName.setText(name);

                    Picasso.get().load(image).placeholder(R.drawable.default_avatar).into(viewHolder.profileImage);

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });


            if(message_type.equals("text")) {

                viewHolder.messageText.setText(c.getMessage());
                viewHolder.messageImage.setVisibility(View.INVISIBLE);

                if(from_user.equals(current_user_id)){
                    viewHolder.messageText.setBackgroundColor(R.drawable.message_text_background_sent);
                    viewHolder.messageText.setTextColor(Color.WHITE);
                }
                else {
                    viewHolder.messageText.setBackgroundColor(Color.WHITE);
                    viewHolder.messageText.setTextColor(Color.BLACK);
                }


            } else {

                viewHolder.messageText.setVisibility(View.INVISIBLE);
                Picasso.get().load(c.getMessage()).resize(700,0).onlyScaleDown()
                        .placeholder(R.drawable.default_avatar).into(viewHolder.messageImage);

            }

        }

        @Override
        public int getItemCount() {
            return mMessageList.size();
        }

}
