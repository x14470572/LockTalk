package com.example.markenteder.locktalk;

import android.content.Context;
import android.content.Intent;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

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

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.AlgorithmParameters;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

import de.hdodenhof.circleimageview.CircleImageView;

public class Messaging extends AppCompatActivity {

    private String convUser;
    private Toolbar toolBar;
    private String image;

    private DatabaseReference rootReference;
    private FirebaseAuth uAuth;
    private FirebaseUser fbUser;
    private DatabaseReference userRef;

    private TextView uNameView, lastSeenView;
    private CircleImageView uProfileImg;
    private String currentUserId;

    private EditText messageET;
    private ImageButton sendBtn;

    private RecyclerView messagesList;
    private SwipeRefreshLayout refreshLayout;

    private final List<Messages> messageList = new ArrayList<>();
    private LinearLayoutManager linearManager;
    private MessageAdapter mAdapter;

    private static final int TOTAL_MESSAGES_TO_LOAD = 10;
    private int currentPage = 1;

    private int messagePosition = 0;
    private String pageLastKey = "";
    private String pagePrevKey = "";

    private String secretHex;
    private byte[] secretByte;
    Cipher encrypt = null;
    public byte[] encodedParams = new byte[0];
    public SecretKeySpec sAESKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messaging);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE);

        toolBar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolBar);

        ActionBar aBar = getSupportActionBar();

        aBar.setDisplayHomeAsUpEnabled(true);
        aBar.setDisplayShowCustomEnabled(true);

        rootReference = FirebaseDatabase.getInstance().getReference();
        uAuth = FirebaseAuth.getInstance();
        fbUser = uAuth.getCurrentUser();
        currentUserId = uAuth.getCurrentUser().getUid();

        convUser = getIntent().getStringExtra("userId");
        String convUName = getIntent().getStringExtra("username");

        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View messageBarView = inflater.inflate(R.layout.messaging_bar, null);

        aBar.setCustomView(messageBarView);

        if(fbUser != null){
            userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(fbUser.getUid());
        }


        uNameView = (TextView) findViewById(R.id.uNameTV);
        lastSeenView = (TextView) findViewById(R.id.lastSeenTV);
        uProfileImg = (CircleImageView) findViewById(R.id.messageBarImg);
        messageET = (EditText) findViewById(R.id.messageET);
        sendBtn = (ImageButton) findViewById(R.id.sendBtn);

        mAdapter = new MessageAdapter(messageList);

        messagesList = (RecyclerView) findViewById(R.id.messagesRV);
        refreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        linearManager = new LinearLayoutManager(this);

        messagesList.setHasFixedSize(true);
        messagesList.setLayoutManager(linearManager);

        messagesList.setAdapter(mAdapter);

        displayMessages();

        uNameView.setText(convUName);

        DatabaseReference DHSecretRef = rootReference.child("Pairs").child(currentUserId).child(convUser);
        DHSecretRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                secretHex = dataSnapshot.child("secret").getValue().toString();
                //secretByte = hexToByteArray(secretHex);

                MessageDigest digest = null;
                try {
                    digest = MessageDigest.getInstance("SHA-256");
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                }
                secretByte = digest.digest(secretHex.getBytes(StandardCharsets.UTF_8));

                sAESKey = new SecretKeySpec(secretByte, 0, 16, "AES");
                try{
                    encrypt = Cipher.getInstance("AES/CBC/PKCS5Padding");
                    encrypt.init(Cipher.ENCRYPT_MODE, sAESKey);
                    encodedParams = encrypt.getParameters().getEncoded();

                }catch (NoSuchAlgorithmException | IOException | InvalidKeyException | NoSuchPaddingException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        rootReference.child("Users").child(convUser).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String active = dataSnapshot.child("active").getValue().toString();
                String image = dataSnapshot.child("small_image").getValue().toString();

                Picasso.get().load(image)
                        .placeholder(R.drawable.profile_pic_resized).into(uProfileImg);


                if(active.equals("true")){
                    lastSeenView.setText("Online");
                } else {
                    ConvertTime convertTime = new ConvertTime();

                    long lastActive = Long.parseLong(active);

                    String lastActiveTime = ConvertTime.ConvertTime(lastActive, getApplicationContext());

                    lastSeenView.setText(lastActiveTime);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        rootReference.child("Conversation").child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(!dataSnapshot.hasChild(convUser)){
                    Map convAddMap = new HashMap();
                    convAddMap.put("seen", false);
                    convAddMap.put("timestamp", ServerValue.TIMESTAMP);

                    Map convUserMap = new HashMap();
                    convUserMap.put("Conversation/" + currentUserId + "/" + convUser, convAddMap);
                    convUserMap.put("Conversation/" + convUser + "/" + currentUserId, convAddMap);

                    rootReference.updateChildren(convUserMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            if(databaseError != null){
                                Log.d("Chat_Log", databaseError.getMessage().toString());
                            }
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    sendBtn.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            try {
                sendMessage();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
    });

    refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
        @Override
        public void onRefresh() {
            currentPage++;

            messagePosition = 0;

            displayPastMessages();
        }
    });

    }

    @Override
    protected void onStart(){
        super.onStart();
        userRef.child("active").setValue("true");

    }

    @Override
    protected void onPause(){
        super.onPause();
        if(fbUser != null){
            userRef.child("active").setValue(ServerValue.TIMESTAMP);
        }
    }

    private void sendMessage() throws UnsupportedEncodingException {
        String message = messageET.getText().toString();
        //byte[] messageBytes = message.getBytes("UTF-8");

        if(!TextUtils.isEmpty(message)){
            final String currentUserRef = "Messages/" + currentUserId + "/" + convUser;
            final String convUserRef = "Messages/" + convUser + "/" + currentUserId;


            final DatabaseReference userMessagePush = rootReference.child("Messages").child(currentUserId).child(convUser).push();
            /*try {
                byte[] cText = encrypt.doFinal(messageBytes);
                String eMessage = toHexString(cText);*/

                String pushId = userMessagePush.getKey();

                Map messageMap = new HashMap();
                messageMap.put("message", message);
                messageMap.put("seen", false);
                messageMap.put("time", ServerValue.TIMESTAMP);
                messageMap.put("from", currentUserId);

                Map messageUserMap = new HashMap();
                messageUserMap.put(currentUserRef + "/" + pushId, messageMap);
                messageUserMap.put(convUserRef + "/" + pushId, messageMap);

                messageET.setText("");

                rootReference.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                        if(databaseError != null){
                            Log.d("Chat_Log", databaseError.getMessage().toString());
                            }
                    }
                });
            /*} catch (IllegalBlockSizeException | BadPaddingException e) {
                e.printStackTrace();
            }*/
        }
    }

    private void displayMessages() {

        DatabaseReference messageRef = rootReference.child("Messages").child(currentUserId).child(convUser);

        Query messageQuery = messageRef.limitToLast(currentPage * TOTAL_MESSAGES_TO_LOAD);

        messageQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Messages message = dataSnapshot.getValue(Messages.class);

                messagePosition++;

                if(messagePosition == 1){

                    String mKey = dataSnapshot.getKey();
                    pageLastKey = mKey;
                    pagePrevKey = mKey;

                }

                messageList.add(message);
                mAdapter.notifyDataSetChanged();

                messagesList.scrollToPosition(messageList.size() - 1);

                refreshLayout.setRefreshing(false);
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
    }

    private void displayPastMessages() {

        DatabaseReference messageRef = rootReference.child("Messages").child(currentUserId).child(convUser);

        Query messageQuery = messageRef.orderByKey().endAt(pageLastKey).limitToLast(10);

        messageQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                Messages message = dataSnapshot.getValue(Messages.class);

                String mKey = dataSnapshot.getKey();

                if(!pagePrevKey.equals(mKey)) {

                    messageList.add(messagePosition++, message);

                } else {

                    pagePrevKey = pageLastKey;
                }

                if(messagePosition == 1){

                    pageLastKey = mKey;

                }

                Log.d("TOTAL_KEYS", "Last Key : " + pageLastKey + " l Prev Key : " + pagePrevKey + " l Message Key : " + mKey);

                mAdapter.notifyDataSetChanged();

                refreshLayout.setRefreshing(false);

                linearManager.scrollToPositionWithOffset(8, 0);
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
    }

    private static byte[] hexToByteArray(String s){

        byte[] b = new byte[s.length() / 2];
        for (int i = 0; i < b.length; i++) {
            int index = i * 2;
            int v = Integer.parseInt(s.substring(index, index + 2), 16);
            b[i] = (byte) v;
        }
        return b;
    }

    private static void byte2hex(byte b, StringBuffer buf) {
        char[] hexChars = { '0', '1', '2', '3', '4', '5', '6', '7', '8',
                '9', 'A', 'B', 'C', 'D', 'E', 'F' };
        int high = ((b & 0xf0) >> 4);
        int low = (b & 0x0f);
        buf.append(hexChars[high]);
        buf.append(hexChars[low]);
    }

    /*
     * Converts a byte array to hex string
     */
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

}
