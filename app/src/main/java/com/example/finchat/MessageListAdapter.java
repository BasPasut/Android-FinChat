package com.example.finchat;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;

public class MessageListAdapter extends RecyclerView.Adapter {
    private static final int VIEW_TYPE_MESSAGE_SENT = 1;
    private static final int VIEW_TYPE_MESSAGE_RECEIVED = 2;
    private static final int VIEW_TYPE_IMAGE_SENT = 3;
    private static final int VIEW_TYPE_IMAGE_RECEIVED = 4;
    private static final int VIEW_TYPE_PDF_SENT = 5;
    private static final int VIEW_TYPE_PDF_RECEIVED = 6;


    private Context mContext;
    private List<Messages> mMessageList;

    private FirebaseUser mCurrentUser;
    private DatabaseReference mDatabase;
    FirebaseStorage firebaseStorage;


    public MessageListAdapter(Context context, List<Messages> messageList) {
        mContext = context;
        mMessageList = messageList;

        firebaseStorage = FirebaseStorage.getInstance();
    }

    @Override
    public int getItemCount() {
        return mMessageList.size();
    }

    // Determines the appropriate ViewType according to the sender of the message.
    @Override
    public int getItemViewType(int position) {
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        Messages message = mMessageList.get(position);

        String from_user = message.getFrom();
        String type = message.getType();

        if (from_user.equals(mCurrentUser.getUid()) && type.contains("text")) {
            // If the current user is the sender of the message
            Log.d("view_type", VIEW_TYPE_MESSAGE_SENT+"");
            return VIEW_TYPE_MESSAGE_SENT;
        }
        if (!from_user.equals(mCurrentUser.getUid()) && type.contains("text")) {
            Log.d("view_type", VIEW_TYPE_MESSAGE_RECEIVED+"");
            return VIEW_TYPE_MESSAGE_RECEIVED;
        }
        if (from_user.equals(mCurrentUser.getUid()) && type.contains("image")) {
            Log.d("view_type", VIEW_TYPE_IMAGE_SENT+"");
            return VIEW_TYPE_IMAGE_SENT;
        }

        if(!from_user.contains(mCurrentUser.getUid()) && type.contains("image")) {
            Log.d("view_type", VIEW_TYPE_IMAGE_RECEIVED+"");
            return VIEW_TYPE_IMAGE_RECEIVED;
        }

        if (from_user.equals(mCurrentUser.getUid()) && type.contains("pdf")) {
            Log.d("view_type", VIEW_TYPE_PDF_SENT+"");
            return VIEW_TYPE_PDF_SENT;
        }

        if (!from_user.equals(mCurrentUser.getUid()) && type.contains("pdf")) {
            Log.d("view_type", VIEW_TYPE_PDF_RECEIVED+"");
            return VIEW_TYPE_PDF_RECEIVED;
        }
        else{
            return 0;
        }
    }

    // Inflates the appropriate layout according to the ViewType.
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;

        if (viewType == VIEW_TYPE_MESSAGE_SENT) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.message_single_sent, parent, false);
            return new SentMessageHolder(view);
        } else if (viewType == VIEW_TYPE_MESSAGE_RECEIVED) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.message_single_receive, parent, false);
            return new ReceivedMessageHolder(view);
        } else if (viewType == VIEW_TYPE_IMAGE_SENT) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.message_image_sent, parent, false);
            return new ImageSentHolder(view);
        } else if (viewType == VIEW_TYPE_IMAGE_RECEIVED) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.message_image_receive, parent, false);
            return new ImageReceivedHolder(view);
        } else if (viewType == VIEW_TYPE_PDF_SENT) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.message_pdf_sent, parent, false);
            return new PDFSentHolder(view);
        } else if (viewType == VIEW_TYPE_PDF_RECEIVED) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.message_pdf_receive, parent, false);
            return new PDFReceivedHolder(view);
        }

        return null;
    }

    // Passes the message object to a ViewHolder so that the contents can be bound to UI.
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        Messages message = mMessageList.get(position);

        switch (holder.getItemViewType()) {
            case VIEW_TYPE_MESSAGE_SENT :
                ((SentMessageHolder) holder).bind(message);
                break;
            case VIEW_TYPE_MESSAGE_RECEIVED :
                ((ReceivedMessageHolder) holder).bind(message);
                break;
            case VIEW_TYPE_IMAGE_SENT :
                ((ImageSentHolder) holder).bind(message);
                break;
            case VIEW_TYPE_IMAGE_RECEIVED :
                ((ImageReceivedHolder) holder).bind(message);
                break;
            case VIEW_TYPE_PDF_SENT :
                ((PDFSentHolder) holder).bind(message);
                break;
            case VIEW_TYPE_PDF_RECEIVED :
                ((PDFReceivedHolder) holder).bind(message);
                break;
            default :
                break;
        }
    }

    private class SentMessageHolder extends RecyclerView.ViewHolder {
        TextView messageText, timeText, nameText;
        CircleImageView profileImage;

        SentMessageHolder(View itemView) {
            super(itemView);

            itemView.setTag(0);
            messageText = itemView.findViewById(R.id.message_text_sent);
            timeText = itemView.findViewById(R.id.message_seen_sent);
            nameText = itemView.findViewById(R.id.message_name_sent);
            profileImage = itemView.findViewById(R.id.message_profile_sent);
        }

        void bind(Messages message) {

            Long message_time = message.getTime();
            String from_user = message.getFrom();

            messageText.setText(message.getMessage());

            // Format the stored timestamp into a readable String using method.
            Calendar cal = Calendar.getInstance(Locale.ENGLISH);
            cal.setTimeInMillis(message_time);
            String date = DateFormat.format("dd-MM-yyyy hh:mm:ss", cal).toString();
            timeText.setText(date);

            DatabaseReference mChatUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(from_user);

            mChatUserDatabase.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    String name = dataSnapshot.child("name").getValue().toString();
                    String image = dataSnapshot.child("thumb_image").getValue().toString();

                    nameText.setText(name);
                    Picasso.get().load(image).placeholder(R.drawable.default_icon_v2).into(profileImage);
                    Picasso.get().setIndicatorsEnabled(false);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

            messageText.setBackgroundColor(R.drawable.message_text_background_sent);
            messageText.setTextColor(Color.WHITE);
        }
    }

    private class ReceivedMessageHolder extends RecyclerView.ViewHolder {
        TextView messageText, timeText, nameText;
        CircleImageView profileImage;

        ReceivedMessageHolder(View itemView) {
            super(itemView);
            itemView.setTag(1);

            messageText = itemView.findViewById(R.id.message_text_receive);
            timeText = itemView.findViewById(R.id.message_seen_receive);
            nameText = itemView.findViewById(R.id.message_name_receive);
            profileImage = itemView.findViewById(R.id.message_profile_receive);
        }

        void bind(Messages message) {

            Long message_time = message.getTime();
            String from_user = message.getFrom();

            messageText.setText(message.getMessage());

            // Format the stored timestamp into a readable String using method.
            Calendar cal = Calendar.getInstance(Locale.ENGLISH);
            cal.setTimeInMillis(message_time);
            String date = DateFormat.format("dd-MM-yyyy hh:mm:ss", cal).toString();
            timeText.setText(date);

            DatabaseReference mChatUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(from_user);

            mChatUserDatabase.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    String name = dataSnapshot.child("name").getValue().toString();
                    String image = dataSnapshot.child("thumb_image").getValue().toString();

                    nameText.setText(name);
                    Picasso.get().load(image).placeholder(R.drawable.default_icon_v2).into(profileImage);
                    Picasso.get().setIndicatorsEnabled(false);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

            messageText.setBackgroundColor(Color.WHITE);
            messageText.setTextColor(Color.BLACK);

        }
    }

    private class ImageSentHolder extends RecyclerView.ViewHolder {
        View imageSentView;
        TextView timeText, nameText;
        CircleImageView profileImage;
        ImageView messageImage;
        boolean mBooleanIsPressed = false;

        ImageSentHolder(View itemView) {
            super(itemView);
            imageSentView = itemView;
            itemView.setTag(2);

            timeText = itemView.findViewById(R.id.message_image_seen_sent);
            nameText = itemView.findViewById(R.id.message_image_name_sent);
            profileImage = itemView.findViewById(R.id.message_image_profile_sent);
            messageImage = itemView.findViewById(R.id.message_image_text_sent);
        }

        void bind(Messages message) {

            Long message_time = message.getTime();
            String from_user = message.getFrom();
            final String[] sender_name = {""};

            // Format the stored timestamp into a readable String using method.
            Calendar cal = Calendar.getInstance(Locale.ENGLISH);
            cal.setTimeInMillis(message_time);
            String date = DateFormat.format("dd-MM-yyyy hh:mm:ss", cal).toString();
            timeText.setText(date);

            DatabaseReference mChatUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(from_user);

            mChatUserDatabase.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    String name = dataSnapshot.child("name").getValue().toString();
                    String image = dataSnapshot.child("thumb_image").getValue().toString();

                    nameText.setText(name);
                    sender_name[0] = name;
                    Picasso.get().load(image).placeholder(R.drawable.default_icon_v2).into(profileImage);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

            String message_url = message.getMessage();
            String finalFileName = getFileName(message_url);


            Picasso.get().load(message_url).resize(700, 0).onlyScaleDown()
                    .placeholder(R.drawable.waiting).into(messageImage);
            Picasso.get().setIndicatorsEnabled(false);

            messageImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(mContext,MessageImageView.class);
                    intent.putExtra("message_url",message_url);
                    intent.putExtra("sender_name",sender_name[0]);
                    intent.putExtra("sender_time",date);
                    intent.putExtra("filename",finalFileName);
                    mContext.startActivity(intent);
                }
            });

            messageImage.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    Toast.makeText(mContext,"Start Downloading. Please wait", Toast.LENGTH_SHORT).show();
                    new DownloadFileFromURL(mContext).execute(message_url,"/Images/",finalFileName);
                    return true;
                }
            });
        }

    }

    private class ImageReceivedHolder extends RecyclerView.ViewHolder {
        TextView timeText, nameText;
        CircleImageView profileImage;
        ImageView messageImage;
        boolean mBooleanIsPressed = false;

        ImageReceivedHolder(View itemView) {
            super(itemView);
            itemView.setTag(3);

            timeText = itemView.findViewById(R.id.message_image_seen_receive);
            nameText = itemView.findViewById(R.id.message_image_name_receive);
            profileImage = itemView.findViewById(R.id.message_image_profile_receive);
            messageImage = itemView.findViewById(R.id.message_image_text_receive);
        }

        void bind(Messages message) {

            Long message_time = message.getTime();
            String from_user = message.getFrom();
            final String[] sender_name = {""};

            // Format the stored timestamp into a readable String using method.
            Calendar cal = Calendar.getInstance(Locale.ENGLISH);
            cal.setTimeInMillis(message_time);
            String date = DateFormat.format("dd-MM-yyyy hh:mm:ss", cal).toString();
            timeText.setText(date);

            DatabaseReference mChatUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(from_user);

            mChatUserDatabase.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    String name = dataSnapshot.child("name").getValue().toString();
                    String image = dataSnapshot.child("thumb_image").getValue().toString();

                    nameText.setText(name);
                    sender_name[0] = name;
                    Picasso.get().load(image).placeholder(R.drawable.default_icon_v2).into(profileImage);

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

            String message_url = message.getMessage();
            String finalFileName = getFileName(message_url);

            Picasso.get().load(message_url).resize(700, 0).onlyScaleDown()
                    .placeholder(R.drawable.waiting).into(messageImage);
            Picasso.get().setIndicatorsEnabled(false);

            messageImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(mContext,MessageImageView.class);
                    intent.putExtra("message_url",message_url);
                    intent.putExtra("sender_name",sender_name[0]);
                    intent.putExtra("sender_time",date);
                    intent.putExtra("filename",finalFileName);
                    mContext.startActivity(intent);
                }
            });

            messageImage.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    Toast.makeText(mContext,"Start Downloading. Please wait", Toast.LENGTH_SHORT).show();
                    new DownloadFileFromURL(mContext).execute(message_url,"/Images/",finalFileName);
                    return true;
                }
            });
        }
    }

    private class PDFSentHolder extends RecyclerView.ViewHolder {

        TextView timeText, nameText, pdfname, pdfsize;
        CircleImageView profileImage;
        Button downloadBtn;

        public PDFSentHolder(View view) {
            super(view);
            itemView.setTag(4);

            timeText = view.findViewById(R.id.message_pdf_seen_sent);
            nameText = view.findViewById(R.id.message_pdf_name_sent);
            pdfname = view.findViewById(R.id.pdf_name_sent);
            pdfsize = view.findViewById(R.id.pdf_size_sent);
            profileImage = view.findViewById(R.id.message_pdf_profile_sent);
            downloadBtn = view.findViewById(R.id.message_pdf_btn_sent);
        }

        void bind(Messages message) {

            Long message_time = message.getTime();
            String from_user = message.getFrom();

            // Format the stored timestamp into a readable String using method.
            Calendar cal = Calendar.getInstance(Locale.ENGLISH);
            cal.setTimeInMillis(message_time);
            String date = DateFormat.format("dd-MM-yyyy hh:mm:ss", cal).toString();
            timeText.setText(date);

            DatabaseReference mChatUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(from_user);

            mChatUserDatabase.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    String name = dataSnapshot.child("name").getValue().toString();
                    String image = dataSnapshot.child("thumb_image").getValue().toString();

                    nameText.setText(name);
                    Picasso.get().load(image).placeholder(R.drawable.default_icon_v2).into(profileImage);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

            String message_url = message.getMessage();
            String finalFileName = getFileName(message_url);

            StorageReference storageReference = firebaseStorage.getReferenceFromUrl(message_url);
            storageReference.getMetadata().addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
                @Override
                public void onSuccess(StorageMetadata storageMetadata) {
                    long filesize = storageMetadata.getSizeBytes();
                    pdfsize.setText(format(filesize,2));
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d("Hello","not work");
                }
            });

            pdfname.setText(finalFileName);
            downloadBtn.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    Toast.makeText(mContext, "Start Downloading. Pls wait...", Toast.LENGTH_LONG).show();
                    new DownloadFileFromURL(mContext).execute(message_url,"/Documents/",finalFileName);
                    return true;
                }
            });

        }
    }

    private class PDFReceivedHolder extends RecyclerView.ViewHolder {
        TextView timeText, nameText, pdfname, pdfsize;
        CircleImageView profileImage;
        Button downloadBtn;

        public PDFReceivedHolder(View view) {
            super(view);
            itemView.setTag(5);

            timeText = view.findViewById(R.id.message_pdf_seen_receive);
            nameText = view.findViewById(R.id.message_pdf_name_receive);
            pdfname = view.findViewById(R.id.pdf_name_receive);
            pdfsize = view.findViewById(R.id.pdf_size_receive);
            profileImage = view.findViewById(R.id.message_pdf_profile_receive);
            downloadBtn = view.findViewById(R.id.message_pdf_btn_receive);
        }

        void bind(Messages message) {

            Long message_time = message.getTime();
            String from_user = message.getFrom();

            // Format the stored timestamp into a readable String using method.
            Calendar cal = Calendar.getInstance(Locale.ENGLISH);
            cal.setTimeInMillis(message_time);
            String date = DateFormat.format("dd-MM-yyyy hh:mm:ss", cal).toString();
            timeText.setText(date);

            DatabaseReference mChatUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(from_user);

            mChatUserDatabase.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    String name = dataSnapshot.child("name").getValue().toString();
                    String image = dataSnapshot.child("thumb_image").getValue().toString();

                    nameText.setText(name);
                    Picasso.get().load(image).placeholder(R.drawable.default_icon_v2).into(profileImage);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

            String message_url = message.getMessage();
            String finalFileName = getFileName(message_url);

            Log.d("url",message_url);

            pdfname.setText(finalFileName);


            StorageReference storageReference = firebaseStorage.getReferenceFromUrl(message_url);
            storageReference.getMetadata().addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
                @Override
                public void onSuccess(StorageMetadata storageMetadata) {
                    long filesize = storageMetadata.getSizeBytes();
                    pdfsize.setText(format(filesize,2));
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d("Hello","not work");
                }
            });

            downloadBtn.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    Toast.makeText(mContext, "Start Downloading. Pls wait...", Toast.LENGTH_LONG).show();
                    new DownloadFileFromURL(mContext).execute(message_url,"/Documents/",finalFileName);
                    return true;
                }
            });

        }
    }


    public String getFileName(String message_url){
        String fileName = message_url.substring(message_url.lastIndexOf('/'));
        String[] fileNameSplit = fileName.split("\\?");
        String finalFileName = fileNameSplit[0].substring(15);
        return finalFileName;
    }


    public static String format(double bytes, int digits) {
        String[] dictionary = { "bytes", "KB", "MB", "GB", "TB", "PB", "EB", "ZB", "YB" };
        int index = 0;
        for (index = 0; index < dictionary.length; index++) {
            if (bytes < 1024) {
                break;
            }
            bytes = bytes / 1024;
        }
        return String.format("%." + digits + "f", bytes) + " " + dictionary[index];
    }
}
