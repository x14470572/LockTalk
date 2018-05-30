package com.example.markenteder.locktalk;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainHub extends AppCompatActivity {

    private String mUsername;
    private String currentUserId;

    private RecyclerView convList;

    private DatabaseReference convDatabase;
    private DatabaseReference messagesDatabase;
    private DatabaseReference uDatabase;

    private FirebaseAuth fbAuth;
    private FirebaseUser fbUser;
    private DatabaseReference userRef;

    private LinearLayoutManager layoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_hub);
        Toolbar toolBar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolBar);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE);

        fbAuth = FirebaseAuth.getInstance();
        fbUser = fbAuth.getCurrentUser();
        currentUserId = fbUser.getUid();

        convDatabase = FirebaseDatabase.getInstance().getReference().child("Conversation").child(currentUserId);
        convDatabase.keepSynced(true);
        uDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        uDatabase.keepSynced(true);
        messagesDatabase = FirebaseDatabase.getInstance().getReference().child("Messages").child(currentUserId);

        layoutManager = new LinearLayoutManager(this);

        convList = (RecyclerView) findViewById(R.id.conversationsRV);
        convList.setHasFixedSize(true);
        convList.setLayoutManager(layoutManager);


        if(fbUser != null){
            userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(fbUser.getUid());
        }
    }

    @Override
    protected void onStart(){
        super.onStart();

        Query convQuery = convDatabase.orderByChild("timestamp");

        FirebaseRecyclerOptions<Conversation> options =
                new FirebaseRecyclerOptions.Builder<Conversation>()
                        .setQuery(convDatabase, Conversation.class)
                        .build();

        FirebaseRecyclerAdapter adapter = new FirebaseRecyclerAdapter<Conversation, ConvViewHolder>(options) {

            @Override
            public ConvViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.conversation_list, parent, false);
                return new ConvViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull final ConvViewHolder convViewHolder, int position, @NonNull final Conversation conversation) {
                final String listUserId = getRef(position).getKey();

                Query lastMessageQuery = messagesDatabase.child(listUserId).limitToLast(1);

                lastMessageQuery.addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        String data = dataSnapshot.child("message").getValue().toString();
                        convViewHolder.setMessage(data, conversation.isSeen());
                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

                uDatabase.child(listUserId).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        final String username = dataSnapshot.child("username").getValue().toString();
                        String smallImage = dataSnapshot.child("small_image").getValue().toString();

                        convViewHolder.setUName(username);
                        convViewHolder.setUserPImage(smallImage);

                        convViewHolder.cView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent messageIntent = new Intent(MainHub.this, Messaging.class);
                                messageIntent.putExtra("userId", listUserId);
                                messageIntent.putExtra("username", username);
                                startActivity(messageIntent);
                            }
                        });

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

                convViewHolder.cView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        CharSequence options[] = new CharSequence[]{"Delete Messages"};
                        Toast.makeText(MainHub.this, "listUserId = " + listUserId,Toast.LENGTH_LONG).show();

                        AlertDialog.Builder builder = new AlertDialog.Builder(MainHub.this);
                        builder.setTitle("Select Options");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int position) {
                                if(position == 0){
                                    convDatabase.child(currentUserId).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            messagesDatabase.child(currentUserId).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {

                                                }
                                            });
                                        }
                                    });
                                    convDatabase.child(listUserId).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            messagesDatabase.child(listUserId).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {

                                                }
                                            });
                                        }
                                    });

                                }
                            }
                        });
                        builder.show();
                        return false;
                    }
                });

            }
        };
        adapter.startListening();
        convList.setAdapter(adapter);



        if (fbUser == null) {
            // Not signed in, launch the Sign In activity
            startActivity(new Intent(this, CreateAccount.class));
            finish();
            return;
        } else {
            mUsername = fbUser.getDisplayName();
            userRef.child("active").setValue("true");
        }
    }

    public class ConvViewHolder extends RecyclerView.ViewHolder {
        View cView;

        public ConvViewHolder(View itemView){
            super(itemView);

            cView = itemView;
        }
        public void setMessage(String message, boolean isSeen){

            TextView lastMessage = (TextView) cView.findViewById(R.id.convLastMsgTV);
            lastMessage.setText(message);

            if(!isSeen){
                lastMessage.setTypeface(lastMessage.getTypeface(), Typeface.BOLD);
            } else {
                lastMessage.setTypeface(lastMessage.getTypeface(), Typeface.NORMAL);
            }
        }

        public void setUserPImage(String smallImage){
            CircleImageView userImgView = (CircleImageView) cView.findViewById(R.id.convProfileImg);
            Picasso.get().load(smallImage).placeholder(R.drawable.profile_pic_resized).into(userImgView);
        }

        public void setUName(String username){

            TextView userNameView = (TextView) cView.findViewById(R.id.convUNameTV);
            userNameView.setText(username);

        }


    }

    @Override
    protected void onPause(){
        super.onPause();
        if(fbUser != null){
            userRef.child("active").setValue(ServerValue.TIMESTAMP);
        }
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
            case R.id.sign_out_menu:
                fbAuth.signOut();
                startActivity(new Intent(this, CreateAccount.class));
                break;
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
