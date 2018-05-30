package com.example.markenteder.locktalk;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.AlgorithmParameters;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.content.ContentValues.TAG;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private List<Messages> messageList;
    private String eMessage = null;
    private String dMessageHex = null;
    private String dMessage = null;
    private byte[] eMessageByte, dMessageByte = new byte[0];
    private byte[] aParams = new byte[0];
    private SecretKeySpec dAesKey;
    Cipher decrypt = null;

    private FirebaseAuth uAuth;
    private DatabaseReference uDatabase;

    public MessageAdapter(List<Messages> messageList){
        this.messageList = messageList;
    }

    @Override
    public MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.message_list, parent, false);
        return new MessageViewHolder(v);
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder {
        public TextView messageText, messageTime, messageUName;
        public CircleImageView messageUserImg;

        public MessageViewHolder(View view) {
            super(view);

            messageText = (TextView) view.findViewById(R.id.messageTV);
            messageUserImg = (CircleImageView) view.findViewById(R.id.messageProfileImg);
            messageTime = (TextView) view.findViewById(R.id.messageTimeTV);
            messageUName = (TextView) view.findViewById(R.id.messageUNameTV);
        }
    }

    @Override
    public void onBindViewHolder(final MessageViewHolder viewHolder, int i) {

        uAuth = FirebaseAuth.getInstance();

        String currentUserId = uAuth.getCurrentUser().getUid();

        Messages c = messageList.get(i);
        String fromUser = c.getFrom();

        uDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(fromUser);

        uDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String uName = dataSnapshot.child("username").getValue().toString();
                String image = dataSnapshot.child("small_image").getValue().toString();

                viewHolder.messageUName.setText(uName);

                Picasso.get().load(image)
                        .placeholder(R.drawable.profile_pic_resized).into(viewHolder.messageUserImg);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        if(fromUser.equals(currentUserId)){

            viewHolder.messageText.setBackgroundResource(R.drawable.message_background);
            viewHolder.messageText.setTextColor(Color.WHITE);

        }else{

            viewHolder.messageText.setBackgroundResource(R.drawable.message_background);
            viewHolder.messageText.setTextColor(Color.WHITE);

        }
        /*try {
            Messaging m = new Messaging();
            aParams = m.encodedParams;
            dAesKey = m.sAESKey;
            eMessage = c.getMessage();
            eMessageByte = hexToByteArray(eMessage);


            AlgorithmParameters AESParams = AlgorithmParameters.getInstance("AES");
            AESParams.init(aParams);
            decrypt = Cipher.getInstance("AES/CBC/PKCS5Padding");
            decrypt.init(Cipher.DECRYPT_MODE, dAesKey, AESParams);
            dMessageByte = decrypt.doFinal(eMessageByte);
            dMessage = new String(dMessageByte, "UTF-8");



        } catch (NoSuchAlgorithmException | InvalidAlgorithmParameterException | IOException | NoSuchPaddingException | InvalidKeyException | BadPaddingException | IllegalBlockSizeException e) {
            e.printStackTrace();
        }*/

        viewHolder.messageUName.setText(c.getFrom());
        viewHolder.messageText.setText(c.getMessage());

    }

    /*private static byte[] hexToByteArray(String s){

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
     //
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
    }*/

    @Override
    public int getItemCount(){
        return messageList.size();
    }
}
