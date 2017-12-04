package com.example.markenteder.locktalk;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

public class SearchUsers extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_users);

        Button pairBtn = (Button) findViewById(R.id.pairBtn);
        pairBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), PairRequest.class);
                startActivityForResult(intent, 0);
            }
        });

        Button profileBtn = (Button) findViewById(R.id.profileBtn);
        profileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), EditProfile.class);
                startActivityForResult(intent, 0);
            }
        });
        Button connectBtn = (Button) findViewById(R.id.connectBtn);
        connectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), SearchUsers.class);
                startActivityForResult(intent, 0);
            }
        });
        Button hubBtn = (Button) findViewById(R.id.hubBtn);
        hubBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), MainHub.class);
                startActivityForResult(intent, 0);
            }
        });
        Button connectionsBtn = (Button) findViewById(R.id.connectionsBtn);
        connectionsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), Connections.class);
                startActivityForResult(intent, 0);
            }
        });
        Button archivedBtn = (Button) findViewById(R.id.archivedBtn);
        archivedBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), MainHub.class);
                startActivityForResult(intent, 0);
            }
        });
    }
}
