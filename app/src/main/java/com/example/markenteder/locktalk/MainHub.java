package com.example.markenteder.locktalk;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainHub extends AppCompatActivity {

    private String mUsername;

    //Firebase Instance Variables
    private FirebaseAuth fbAuth;
    private FirebaseUser fbUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_hub);
        Toolbar toolBar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolBar);

        // Initialize Firebase Auth
        fbAuth = FirebaseAuth.getInstance();
        fbUser = fbAuth.getCurrentUser();
        if (fbUser == null) {
            // Not signed in, launch the Sign In activity
            startActivity(new Intent(this, CreateAccount.class));
            finish();
            return;
        } else {
            mUsername = fbUser.getDisplayName();
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
