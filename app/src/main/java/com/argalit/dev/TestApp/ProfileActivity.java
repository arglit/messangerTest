package com.argalit.dev.TestApp;

import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.argalit.dev.TestApp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;


public class ProfileActivity extends AppCompatActivity
{

    private Button SendFriendRequestButton;
    private Button DeclineFriendRequestButton;
    private TextView ProfileName;
    private TextView ProfileStatus;
    private ImageView ProfileImage;

    private DatabaseReference UsersReference;
    private DatabaseReference FriendsReference;

    private String CURRENT_STATE;
    private DatabaseReference FriendRequestReference;
    private FirebaseAuth mAuth;

    String sender_user_id;
    String receiver_user_id;

    private DatabaseReference NotificationsReference;



    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        FriendRequestReference = FirebaseDatabase.getInstance().getReference().child("Friend_Requests");
        FriendRequestReference.keepSynced(true);


        mAuth = FirebaseAuth.getInstance();
        sender_user_id = mAuth.getCurrentUser().getUid();

        FriendsReference = FirebaseDatabase.getInstance().getReference().child("Friends");
        FriendsReference.keepSynced(true);

        NotificationsReference = FirebaseDatabase.getInstance().getReference().child("Notifications");
        NotificationsReference.keepSynced(true);



        receiver_user_id = getIntent().getExtras().get("visit_user_id").toString();

        SendFriendRequestButton = (Button)  findViewById(R.id.profile_visit_send_req_btn);
        DeclineFriendRequestButton = (Button) findViewById(R.id.profile_decline_friend_req_btn);
        ProfileName = (TextView) findViewById(R.id.profile_visit_username);
        ProfileStatus = (TextView) findViewById(R.id.profile_visit_user_status);
        ProfileImage = (ImageView) findViewById(R.id.profile_visit_user_image);


        CURRENT_STATE = "not_friends";

        UsersReference = FirebaseDatabase.getInstance().getReference().child("Users");


        UsersReference.child(receiver_user_id).addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                String name = dataSnapshot.child("user_name").getValue().toString();
                String status = dataSnapshot.child("user_status").getValue().toString();
                String image = dataSnapshot.child("user_image").getValue().toString();

                ProfileName.setText(name);
                ProfileStatus.setText(status);
                Picasso.get().load(image).placeholder(R.drawable.default_profile).into(ProfileImage);


                FriendRequestReference.child(sender_user_id)
                        .addListenerForSingleValueEvent(new ValueEventListener()
                        {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot)
                            {
                                if(dataSnapshot.exists())
                                {
                                    if(dataSnapshot.hasChild(receiver_user_id))
                                    {
                                        String req_type = dataSnapshot.child(receiver_user_id).child("request_type").getValue().toString();

                                        if(req_type.equals("sent"))
                                        {
                                            CURRENT_STATE = "request_sent";
                                            SendFriendRequestButton.setText("Cancel Friend Request");

                                            DeclineFriendRequestButton.setVisibility(View.INVISIBLE);
                                            DeclineFriendRequestButton.setEnabled(false);

                                        }
                                        else if(req_type.equals("receiver"))
                                        {
                                            CURRENT_STATE = "request_received";
                                            SendFriendRequestButton.setText("Accept Friend Request");

                                            DeclineFriendRequestButton.setVisibility(View.VISIBLE);
                                            DeclineFriendRequestButton.setEnabled(true);

                                            DeclineFriendRequestButton.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View view)
                                                {
                                                    DeclineFriendRequest();
                                                    
                                                }
                                            });



                                        }
                                    }
                                }
                                else
                                {
                                    FriendsReference.child(receiver_user_id)
                                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(DataSnapshot dataSnapshot)//проверка пользователей на нахождение в друзьях
                                                {
                                                    if(dataSnapshot.hasChild(sender_user_id))
                                                    {
                                                        CURRENT_STATE = "friends";
                                                        SendFriendRequestButton.setText("Unfriend this person");

                                                        DeclineFriendRequestButton.setVisibility(View.INVISIBLE);
                                                        DeclineFriendRequestButton.setEnabled(false);

                                                    }
                                                }

                                                @Override
                                                public void onCancelled(DatabaseError databaseError) {

                                                }
                                            });
                                }

                            }

                            private void DeclineFriendRequest()
                            {
                                FriendRequestReference.child(sender_user_id).child(receiver_user_id).removeValue()
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task)
                                            {
                                                if (task.isSuccessful())
                                                {
                                                    FriendRequestReference.child(receiver_user_id).child(sender_user_id).removeValue()
                                                            .addOnCompleteListener(new OnCompleteListener<Void>()
                                                            {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task)
                                                                {
                                                                    if(task.isSuccessful())
                                                                    {
                                                                        SendFriendRequestButton.setEnabled(true);
                                                                        CURRENT_STATE = "not_friends";
                                                                        SendFriendRequestButton.setText("Send friend request");

                                                                        DeclineFriendRequestButton.setVisibility(View.INVISIBLE);
                                                                        DeclineFriendRequestButton.setEnabled(false);

                                                                    }
                                                                }
                                                            });
                                                }
                                            }
                                        });
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });

            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        });

        DeclineFriendRequestButton.setVisibility(View.INVISIBLE);
        DeclineFriendRequestButton.setEnabled(false);

        if(sender_user_id.equals(receiver_user_id))
        {
            DeclineFriendRequestButton.setVisibility(View.INVISIBLE);
            SendFriendRequestButton.setVisibility(View.INVISIBLE);
        }



        SendFriendRequestButton.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view) {
                    SendFriendRequestButton.setEnabled(false);


                    if (CURRENT_STATE.equals("not_friends"))//отправка запроса  пользователя в друзья
                    {
                        SendFriendRequestToAPerson();
                    }
                    if (CURRENT_STATE.equals("request_sent"))//отмена запроса отправки пользователя в друзья
                    {
                        CancelFriendRequest();
                    }
                    if (CURRENT_STATE.equals("request_received")) {
                        AcceptFriendRequest();
                    }
                    if (CURRENT_STATE.equals("friends")) {
                        UnFriendaFriend();
                    }

                }
                private void CancelFriendRequest()//отмена запроса  отправки пользователя в друзья
                {
                    FriendRequestReference.child(sender_user_id).child(receiver_user_id).removeValue()
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task)
                                {
                                    if (task.isSuccessful())
                                    {
                                        FriendRequestReference.child(receiver_user_id).child(sender_user_id).removeValue()
                                                .addOnCompleteListener(new OnCompleteListener<Void>()
                                                {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task)
                                                    {
                                                        if(task.isSuccessful())
                                                        {
                                                            SendFriendRequestButton.setEnabled(true);
                                                            CURRENT_STATE = "not_friends";
                                                            SendFriendRequestButton.setText("Send friend request");

                                                            DeclineFriendRequestButton.setVisibility(View.INVISIBLE);
                                                            DeclineFriendRequestButton.setEnabled(false);

                                                        }
                                                    }
                                                });
                                    }
                                }
                            });


                }
                private void SendFriendRequestToAPerson()//отправка запроса  пользователя в друзья
                {
                    FriendRequestReference.child(sender_user_id).child(receiver_user_id)
                            .child("request_type").setValue("sent")
                            .addOnCompleteListener(new OnCompleteListener<Void>()
                            {
                                @Override
                                public void onComplete(@NonNull Task<Void> task)
                                {
                                    if(task.isSuccessful())
                                    {
                                        FriendRequestReference.child(receiver_user_id).child(sender_user_id)
                                                .child("request_type").setValue("receiver")
                                                .addOnCompleteListener(new OnCompleteListener<Void>()
                                                {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task)
                                                    {
                                                        if(task.isSuccessful())
                                                        {
                                                            HashMap<String, String> notificationsData = new HashMap<String, String>();
                                                            notificationsData.put("from",sender_user_id);
                                                            notificationsData.put("type","request");

                                                             NotificationsReference.child(receiver_user_id).push().setValue(notificationsData)
                                                                     .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                 @Override
                                                                 public void onComplete(@NonNull Task<Void> task)
                                                                 {
                                                                        if(task.isSuccessful())
                                                                        {
                                                                            SendFriendRequestButton.setEnabled(true);
                                                                            CURRENT_STATE = "request_sent";
                                                                            SendFriendRequestButton.setText("Cancel Friend Request");

                                                                            DeclineFriendRequestButton.setVisibility(View.INVISIBLE);
                                                                            DeclineFriendRequestButton.setEnabled(false);
                                                                        }
                                                                 }
                                                             });



                                                        }
                                                    }
                                                });
                                    }
                                }
                            });


                }
                private void AcceptFriendRequest()
                {
                    Calendar calFordATE = Calendar.getInstance();//это не ошибка
                    SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy");//это не ошибка
                    final String saveCurrentDate = currentDate.format(calFordATE.getTime());//это не ошибка

                    FriendRequestReference.child(sender_user_id).child(receiver_user_id).setValue(saveCurrentDate)
                            .addOnSuccessListener(new OnSuccessListener<Void>()
                            {
                                @Override
                                public void onSuccess(Void aVoid)
                                {
                                    FriendsReference.child(receiver_user_id).child(sender_user_id).setValue(saveCurrentDate)
                                            .addOnSuccessListener(new OnSuccessListener<Void>()
                                            {
                                                @Override
                                                public void onSuccess(Void aVoid)
                                                {
                                                    FriendRequestReference.child(sender_user_id).child(receiver_user_id).removeValue()
                                                            .addOnCompleteListener(new OnCompleteListener<Void>()
                                                            {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task)
                                                                {
                                                                    if (task.isSuccessful())
                                                                    {
                                                                        FriendRequestReference.child(receiver_user_id).child(sender_user_id).removeValue()
                                                                                .addOnCompleteListener(new OnCompleteListener<Void>()
                                                                                {
                                                                                    @Override
                                                                                    public void onComplete(@NonNull Task<Void> task)
                                                                                    {
                                                                                        if(task.isSuccessful())
                                                                                        {
                                                                                            SendFriendRequestButton.setEnabled(true);
                                                                                            CURRENT_STATE = "friends";
                                                                                            SendFriendRequestButton.setText("Unfriend this person");

                                                                                            DeclineFriendRequestButton.setVisibility(View.INVISIBLE);
                                                                                            DeclineFriendRequestButton.setEnabled(false);
                                                                                        }
                                                                                    }
                                                                                });
                                                                    }
                                                                }
                                                            });
                                                }
                                            });
                                }
                            });


                }
                private void UnFriendaFriend()
                {
                    FriendRequestReference.child(sender_user_id).child(receiver_user_id).removeValue()
                            .addOnCompleteListener(new OnCompleteListener<Void>()
                            {
                                @Override
                                public void onComplete(@NonNull Task<Void> task)
                                {
                                    if(task.isSuccessful())
                                    {
                                        FriendsReference.child(receiver_user_id).child(sender_user_id).removeValue()
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task)
                                                    {
                                                        if(task.isSuccessful())
                                                        {
                                                            SendFriendRequestButton.setEnabled(true);
                                                            CURRENT_STATE = "not_friends";
                                                            SendFriendRequestButton.setText("Send Friend Request");

                                                            DeclineFriendRequestButton.setVisibility(View.INVISIBLE);
                                                            DeclineFriendRequestButton.setEnabled(false);


                                                        }
                                                    }
                                                });
                                    }
                                }
                            });
                }

            }
            );
        }

    }





