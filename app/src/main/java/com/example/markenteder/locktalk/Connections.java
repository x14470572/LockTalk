package com.example.markenteder.locktalk;

import android.content.Intent;
import android.support.annotation.NonNull;
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
import android.widget.Button;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class Connections extends AppCompatActivity {

    private RecyclerView uPairList;

    private DatabaseReference uPairsDatabase;
    private DatabaseReference uDatabase;

    private FirebaseAuth uAuth;

    private String currentUser;

    private View mMainView;

    private LinearLayoutManager layoutManager;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connections);
        Toolbar toolBar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolBar);

        layoutManager = new LinearLayoutManager(this);

        uPairList = (RecyclerView) findViewById(R.id.pairsRV);
        uPairList.setHasFixedSize(true);
        uPairList.setLayoutManager(layoutManager);
        uAuth = FirebaseAuth.getInstance();

        currentUser = uAuth.getCurrentUser().getUid();

        uPairsDatabase = FirebaseDatabase.getInstance().getReference().child("Pairs").child(currentUser);
        uDatabase = FirebaseDatabase.getInstance().getReference().child("Users");

    }

    protected void onStart() {
        super.onStart();

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
            protected void onBindViewHolder(@NonNull Connections.PairViewHolder pairViewHolder, int position, @NonNull Pairs pairs) {
                pairViewHolder.setUName(pairs.getUsername());
                pairViewHolder.setFName(pairs.getFullname());

                final String userId = getRef(position).getKey();

                /*Button connectBtn = (Button) pairViewHolder.uView.findViewById(R.id.connectBtn);
                connectBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent chatRequest = new Intent(Connections.this, Messaging.class);
                        chatRequest.putExtra("userId", userId);
                        startActivity(chatRequest);
                    }
                });*/

            }
        };
        adapter.startListening();
        uPairList.setAdapter(adapter);
    }

    public class PairViewHolder extends RecyclerView.ViewHolder {

        View uView;

        public PairViewHolder(View itemView) {
            super(itemView);

            uView = itemView;
        }
        public void setUName(String username){
            TextView uNameTV = (TextView) uView.findViewById(R.id.unameTV);
            uNameTV.setText(username);
        }
        public void setFName(String fullname){
            TextView fNameTV = (TextView) uView.findViewById(R.id.fNameTV);
            fNameTV.setText(fullname);
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
