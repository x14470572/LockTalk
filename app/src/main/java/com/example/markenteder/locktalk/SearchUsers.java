package com.example.markenteder.locktalk;

import android.content.Context;
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
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

//import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;


public class SearchUsers extends AppCompatActivity {

    private EditText uSearchField;
    private Button uSearchBtn;

    private RecyclerView uSearchResults;

    private DatabaseReference uDatabase;

    private LinearLayoutManager layoutManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_users);
        Toolbar toolBar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolBar);

        uDatabase = FirebaseDatabase.getInstance().getReference().child("Users");

        layoutManager = new LinearLayoutManager(this);

        /*uSearchField = (EditText) findViewById(R.id.searchUserTF);
        uSearchBtn = (Button) findViewById(R.id.searchBtn);*/

        uSearchResults = (RecyclerView) findViewById(R.id.resultsRV);
        uSearchResults.setHasFixedSize(true);
        uSearchResults.setLayoutManager(layoutManager);


    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Users> options =
                new FirebaseRecyclerOptions.Builder<Users>()
                .setQuery(uDatabase, Users.class)
                .build();

         FirebaseRecyclerAdapter adapter = new FirebaseRecyclerAdapter<Users, UserViewHolder>(options) {


            @Override
            public UserViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.user_list, parent, false);

                return new UserViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull UserViewHolder userViewHolder, int position, @NonNull Users users) {
                userViewHolder.setUName(users.getUsername());
                userViewHolder.setFName(users.getFullname());

                final String userId = getRef(position).getKey();

                Button connectBtn = (Button) userViewHolder.uView.findViewById(R.id.connectBtn);
                connectBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent pairRequest = new Intent(SearchUsers.this, PairRequest.class);
                        pairRequest.putExtra("userId", userId);
                        startActivity(pairRequest);
                    }
                });

            }
        };
         adapter.startListening();
         uSearchResults.setAdapter(adapter);
    }

    public class UserViewHolder extends RecyclerView.ViewHolder {

        View uView;

        public UserViewHolder(View itemView) {
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
