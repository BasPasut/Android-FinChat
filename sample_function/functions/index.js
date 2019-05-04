mFriendRequestDatabase.child(mCurrent_user.getUid()).child(user_id).child("request_type")
                            .setValue("sent").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                mFriendRequestDatabase.child(user_id).child(mCurrent_user.getUid()).child("request_type")
                                        .setValue("received").addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {

                                        if(task.isSuccessful()) {
                                            HashMap<String, String> notifications_data = new HashMap<>();
                                            notifications_data.put("from", mCurrent_user.getUid());
                                            notifications_data.put("type", "request");

                                            mNotificationDatabase.child(user_id).push().setValue(notifications_data).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        current_state = 1;
                                                        mFriendRequest.setText("Cancel Friend Request");

                                                        mFriendCancel.setVisibility(View.INVISIBLE);
                                                        mFriendCancel.setEnabled(false);
                                                    } else {

                                                    }
                                                }
                                            });



                                            //Toast.makeText(FriendsActivity.this, "Success sending request", Toast.LENGTH_SHORT).show();
                                        }
                                        else{

                                        }
                                    }
                                });
                            }
                            else{
                                Toast.makeText(FriendsActivity.this, "Failed sending request", Toast.LENGTH_SHORT).show();
                            }

                            mFriendRequest.setEnabled(true);
                        }
                    });
