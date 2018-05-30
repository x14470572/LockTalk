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
import android.view.WindowManager;
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
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class PairRequest extends AppCompatActivity {

    private CircleImageView uProfilePic;
    private TextView uName, fName, uEmail, pNumber;
    private Button pairRequestBtn, decRequestBtn;

    private DatabaseReference uDatabase;
    private DatabaseReference uPairReqDatabase;
    private DatabaseReference uPairsDatabase;
    private DatabaseReference uNotificationsDB;
    private DatabaseReference rootReference;
    private DatabaseReference keysDatabase;
    private DatabaseReference userRef;

    private FirebaseUser currentUser;

    private String uPairReqState;
    private BigInteger g,p,x,y,A,B,s1,s2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pair_request);
        Toolbar toolBar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolBar);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE);

        final String userId = getIntent().getStringExtra("userId");

        uDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(userId);
        uPairReqDatabase = FirebaseDatabase.getInstance().getReference().child("PairRequest");
        uPairsDatabase = FirebaseDatabase.getInstance().getReference().child("Pairs");
        uNotificationsDB = FirebaseDatabase.getInstance().getReference().child("Notifications");
        keysDatabase = FirebaseDatabase.getInstance().getReference().child("PublicKeys");
        rootReference = FirebaseDatabase.getInstance().getReference();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUser.getUid());


        uProfilePic = (CircleImageView) findViewById(R.id.profilePic);
        uName = (TextView) findViewById(R.id.userTxt);
        fName = (TextView) findViewById(R.id.nameTxt);
        uEmail = (TextView) findViewById(R.id.emailTxt);
        pNumber = (TextView) findViewById(R.id.mobileTxt);
        pairRequestBtn = (Button) findViewById(R.id.connectReqBtn);
        decRequestBtn = (Button) findViewById(R.id.declineReqBtn);

        uPairReqState = "notPaired";

        decRequestBtn.setVisibility(View.INVISIBLE);
        decRequestBtn.setEnabled(false);

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

                if(currentUser.getUid().equals(userId)){
                    pairRequestBtn.setEnabled(false);
                    pairRequestBtn.setVisibility(View.INVISIBLE);
                }


                // Connections List

                uPairReqDatabase.child(currentUser.getUid()).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(dataSnapshot.hasChild(userId)){
                            String reqState = dataSnapshot.child(userId).child("pairReqState").getValue().toString();

                            if(reqState.equals("received")){
                                uPairReqState = "requestReceived";
                                pairRequestBtn.setText("Accept Friend Request");

                                decRequestBtn.setVisibility(View.VISIBLE);
                                decRequestBtn.setEnabled(true);
                            } else if(reqState.equals("sent")){
                                uPairReqState = "requestSent";
                                pairRequestBtn.setText("Rescind Request");

                                decRequestBtn.setVisibility(View.INVISIBLE);
                                decRequestBtn.setEnabled(false);
                            }
                        } else {
                            uPairsDatabase.child(currentUser.getUid()).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if(dataSnapshot.hasChild(userId)){
                                        uPairReqState = "paired";
                                        pairRequestBtn.setText("Remove Pairing");

                                        decRequestBtn.setVisibility(View.INVISIBLE);
                                        decRequestBtn.setEnabled(false);
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

                //Complete Strangers

                if(uPairReqState.equals("notPaired")){



                    uPairReqDatabase.child(currentUser.getUid()).child(userId).child("pairReqState")
                            .setValue("sent").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                uPairReqDatabase.child(userId).child(currentUser.getUid()).child("pairReqState")
                                        .setValue("received").addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {

                                        HashMap<String, String> nData = new HashMap<>();
                                        nData.put("from", currentUser.getUid());
                                        nData.put("type", "request");


                                        uNotificationsDB.child(userId).push().setValue(nData).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                pairRequestBtn.setEnabled(true);
                                                uPairReqState = "requestSent";
                                                pairRequestBtn.setText("Rescind Request");

                                                decRequestBtn.setVisibility(View.INVISIBLE);
                                                decRequestBtn.setEnabled(false);

                                                Toast.makeText(PairRequest.this, "Request Sent Successfully",Toast.LENGTH_LONG).show();
                                            }
                                        });

                                    }
                                });

                            }else {
                                Toast.makeText(PairRequest.this, "Failed Sending Request",Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                }

                // Cancelling Pair Request
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

                                    decRequestBtn.setVisibility(View.INVISIBLE);
                                    decRequestBtn.setEnabled(false);
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

                // Received Pair Request
                if(uPairReqState.equals("requestReceived")){

                    final String curDate = DateFormat.getDateTimeInstance().format(new Date());

                    SecretKey sK = new SecretKey();
                    final byte[] DHSecBytes = sK.GenDHSecret();
                    final String DHSec = toHexString(DHSecBytes);



                    uPairsDatabase.child(currentUser.getUid()).child(userId).child("date").setValue(curDate)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            uPairsDatabase.child(currentUser.getUid()).child(userId).child("secret").setValue(DHSec).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    uPairsDatabase.child(userId).child(currentUser.getUid()).child("date").setValue(curDate)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    uPairsDatabase.child(userId).child(currentUser.getUid()).child("secret").setValue(DHSec).addOnSuccessListener(new OnSuccessListener<Void>() {
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

                                                                            decRequestBtn.setVisibility(View.INVISIBLE);
                                                                            decRequestBtn.setEnabled(false);
                                                                        }
                                                                    });
                                                                }
                                                            });
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

    private static void byte2hex(byte b, StringBuffer buf) {
        char[] hexChars = { '0', '1', '2', '3', '4', '5', '6', '7', '8',
                '9', 'A', 'B', 'C', 'D', 'E', 'F' };
        int high = ((b & 0xf0) >> 4);
        int low = (b & 0x0f);
        buf.append(hexChars[high]);
        buf.append(hexChars[low]);
    }


    private static String toHexString(byte[] block) {
        StringBuffer buf = new StringBuffer();
        int len = block.length;
        for (int i = 0; i < len; i++) {
            byte2hex(block[i], buf);
            if (i < len-1) {
                //buf.append(":");
            }
        }
        return buf.toString();
    }

    @Override
    protected void onStart(){
        super.onStart();
        userRef.child("active").setValue(true);
    }

    @Override
    protected void onPause(){
        super.onPause();
        userRef.child("active").setValue(ServerValue.TIMESTAMP);
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
