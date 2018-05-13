package com.example.markenteder.locktalk;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.Date;

import de.hdodenhof.circleimageview.CircleImageView;

public class PairRequest extends AppCompatActivity {

    private CircleImageView uProfilePic;
    private TextView uName, fName, uEmail, pNumber;
    private Button pairRequestBtn;

    private DatabaseReference uDatabase;
    private DatabaseReference uPairReqDatabase;
    private DatabaseReference uPairsDatabase;
    private FirebaseUser currentUser;

    private String uPairReqState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pair_request);
        Toolbar toolBar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolBar);

        final String userId = getIntent().getStringExtra("userId");

        uDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(userId);
        uPairReqDatabase = FirebaseDatabase.getInstance().getReference().child("PairRequest");
        uPairsDatabase = FirebaseDatabase.getInstance().getReference().child("Pairs");
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        uProfilePic = (CircleImageView) findViewById(R.id.profilePic);
        uName = (TextView) findViewById(R.id.userTxt);
        fName = (TextView) findViewById(R.id.nameTxt);
        uEmail = (TextView) findViewById(R.id.emailTxt);
        pNumber = (TextView) findViewById(R.id.mobileTxt);
        pairRequestBtn = (Button) findViewById(R.id.connectReqBtn);

        uPairReqState = "notPaired";



        uDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String username = dataSnapshot.child("username").getValue().toString();
                String image = dataSnapshot.child("image").getValue().toString();
                String pnumber = dataSnapshot.child("pnumber").getValue().toString();
                String email = dataSnapshot.child("email").getValue().toString();
                String fullname = dataSnapshot.child("fullname").getValue().toString();

                uName.setText(username);
                fName.setText(fullname);
                uEmail.setText(email);
                pNumber.setText(pnumber);

                Picasso.get().load(image).into(uProfilePic);

                // Connections List

                uPairReqDatabase.child(currentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(dataSnapshot.hasChild(userId)){
                            String reqState = dataSnapshot.child(userId).child("pairReqState").getValue().toString();

                            if(reqState.equals("received")){
                                uPairReqState = "requestReceived";
                                pairRequestBtn.setText("Accept Friend Request");
                            } else if(reqState.equals("sent")){
                                uPairReqState = "requestSent";
                                pairRequestBtn.setText("Rescind Request");
                            }
                        } else {
                            uPairReqDatabase.child(currentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if(dataSnapshot.hasChild(userId)){
                                        uPairReqState = "paired";
                                        pairRequestBtn.setText("Remove Pairing");
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        pairRequestBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                pairRequestBtn.setEnabled(false);

                if(uPairReqState.equals("notPaired")){

                    //Complete Strangers

                    uPairReqDatabase.child(currentUser.getUid()).child(userId).child("pairReqState")
                            .setValue("sent").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                uPairReqDatabase.child(userId).child(currentUser.getUid()).child("pairReqState")
                                        .setValue("received").addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {


                                        uPairReqState = "requestSent";
                                        pairRequestBtn.setText("Rescind Request");

                                        Toast.makeText(PairRequest.this, "Request Sent Successfully",Toast.LENGTH_LONG).show();
                                    }
                                });

                            }else {
                                Toast.makeText(PairRequest.this, "Failed Sending Request",Toast.LENGTH_LONG).show();
                            }
                            pairRequestBtn.setEnabled(true);
                        }
                    });
                }

                // Cancel Request
                if(uPairReqState.equals("requestSent")){
                    uPairReqDatabase.child(currentUser.getUid()).child(userId).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            uPairReqDatabase.child(userId).child(currentUser.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    pairRequestBtn.setEnabled(true);
                                    uPairReqState = "notPaired";
                                    pairRequestBtn.setText("Connect");
                                }
                            });
                        }
                    });
                }

                //Cancel Pairing
                if(uPairReqState.equals("paired")){
                    uPairsDatabase.child(currentUser.getUid()).child(userId).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            uPairsDatabase.child(userId).child(currentUser.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    pairRequestBtn.setEnabled(true);
                                    uPairReqState = "notPaired";
                                    pairRequestBtn.setText("Connect");
                                }
                            });
                        }
                    });
                    uPairsDatabase.child(userId).child(currentUser.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            uPairsDatabase.child(currentUser.getUid()).child(userId).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    pairRequestBtn.setEnabled(true);
                                    uPairReqState = "notPaired";
                                    pairRequestBtn.setText("Connect");
                                }
                            });
                        }
                    });
                }

                // Received Request
                if(uPairReqState.equals("requestReceived")){

                    final String curDate = DateFormat.getDateTimeInstance().format(new Date());

                    uPairsDatabase.child(currentUser.getUid()).child(userId).setValue(curDate)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            uPairsDatabase.child(userId).child(currentUser.getUid()).setValue(curDate)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            uPairReqDatabase.child(currentUser.getUid()).child(userId).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    uPairReqDatabase.child(userId).child(currentUser.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                            pairRequestBtn.setEnabled(true);
                                                            uPairReqState = "paired";
                                                            pairRequestBtn.setText("Remove Pairing");
                                                        }
                                                    });
                                                }
                                            });
                                        }
                                    });
                        }
                    });
                }
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater mInflater = getMenuInflater();
        mInflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.hub_menu:
                startActivity(new Intent(this, MainHub.class));
                break;
            case R.id.connect_menu:
                startActivity(new Intent(this, SearchUsers.class));
                break;
            case R.id.connections_menu:
                startActivity(new Intent(this, Connections.class));
                break;
            case R.id.profile_menu:
                startActivity(new Intent(this, EditProfile.class));
                break;
        }
        return true;
    }
}
