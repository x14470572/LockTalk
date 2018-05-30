package com.example.markenteder.locktalk;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class Connections extends AppCompatActivity {

    private RecyclerView uPairList;

    private FirebaseAuth uAuth;
    private DatabaseReference uPairsDatabase;
    private DatabaseReference uDatabase;
    private DatabaseReference userRef;

    private String currentUserId;

    private LinearLayoutManager layoutManager;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connections);
        Toolbar toolBar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolBar);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE);

        layoutManager = new LinearLayoutManager(this);

        uPairList = (RecyclerView) findViewById(R.id.pairsRV);
        uPairList.setHasFixedSize(true);
        uPairList.setLayoutManager(layoutManager);

        uAuth = FirebaseAuth.getInstance();

        currentUserId = uAuth.getCurrentUser().getUid();

        uPairsDatabase = FirebaseDatabase.getInstance().getReference().child("Pairs").child(currentUserId);
        uPairsDatabase.keepSynced(true);
        uDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        uDatabase.keepSynced(true);
        userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserId);


    }

    @Override
    public void onStart() {
        super.onStart();

        userRef.child("active").setValue(true);

        FirebaseRecyclerOptions<Pairs> options =
                new FirebaseRecyclerOptions.Builder<Pairs>()
                .setQuery(uPairsDatabase, Pairs.class)
                .build();

        FirebaseRecyclerAdapter adapter = new FirebaseRecyclerAdapter<Pairs, PairViewHolder>(options) {

            @Override
            public PairViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.pair_list, parent, false);
                return new PairViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull final PairViewHolder pairViewHolder, int position, @NonNull Pairs pairs) {

                pairViewHolder.setDate(pairs.getDate());

                final String listUserId = getRef(position).getKey();

                uDatabase.child(listUserId).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        final String username = dataSnapshot.child("username").getValue().toString();
                        String userImage = dataSnapshot.child("small_image").getValue().toString();

                        if(dataSnapshot.hasChild("active")){
                            String userOnline = dataSnapshot.child("active").getValue().toString();
                            pairViewHolder.setUserActive(userOnline);
                        }
                        pairViewHolder.setUName(username);
                        pairViewHolder.setUserImage(userImage);

                        pairViewHolder.pView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                CharSequence options[] = new CharSequence[]{"View Profile", "Send Message"};

                                AlertDialog.Builder builder = new AlertDialog.Builder(Connections.this);
                                builder.setTitle("Select Options");
                                builder.setItems(options, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int position) {
                                        if(position == 0){
                                            Intent pairProfile = new Intent(Connections.this, PairRequest.class);
                                            pairProfile.putExtra("userId", listUserId);
                                            startActivity(pairProfile);
                                        }
                                        if(position == 1){
                                            Intent messageIntent = new Intent(Connections.this, Messaging.class);
                                            messageIntent.putExtra("userId", listUserId);
                                            messageIntent.putExtra("username", username);
                                            startActivity(messageIntent);
                                        }
                                    }
                                });
                                builder.show();
                            }
                        });
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        };
        adapter.startListening();
        uPairList.setAdapter(adapter);
    }

    public class PairViewHolder extends RecyclerView.ViewHolder {
        View pView;

        public PairViewHolder(View itemView){
            super(itemView);

            pView = itemView;
        }
        public void setDate(String date){
            TextView uNameTV = (TextView) pView.findViewById(R.id.fNameTV);
            uNameTV.setText("Friends Since : " + date);
        }

        public void setUName(String username){
            TextView uNameView = (TextView) pView.findViewById(R.id.unameTV);
            uNameView.setText(username);
        }

        public void setUserImage(String smallImage){
            CircleImageView userImgView = (CircleImageView) pView.findViewById(R.id.profilePic);
            Picasso.get().load(smallImage).placeholder(R.drawable.profile_pic_resized).into(userImgView);
        }

        public void setUserActive(String onlineStatus){
            TextView activeView = (TextView) pView.findViewById(R.id.onlineTV);
            if(onlineStatus.equals("true")){
                activeView.setVisibility(View.VISIBLE);
            } else {
                activeView.setVisibility(View.INVISIBLE);
            }
        }
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
