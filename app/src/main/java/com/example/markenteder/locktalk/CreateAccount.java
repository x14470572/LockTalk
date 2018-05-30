package com.example.markenteder.locktalk;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class CreateAccount extends AppCompatActivity {

    private EditText mobileTF, codeTF, usernameTF;
    private Button sendVerBtn, confirmBtn;

    private FirebaseAuth fAuth;
    private DatabaseReference fData;

    String verCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);

        /*getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE);*/

        fAuth = FirebaseAuth.getInstance();

        sendVerBtn = (Button) findViewById(R.id.sendVerBtn);
        confirmBtn = (Button) findViewById(R.id.confirmBtn);
        mobileTF = findViewById(R.id.mobileTF);
        codeTF = findViewById(R.id.codeTF);
        usernameTF = findViewById(R.id.usernameTF);


        sendVerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendVerCode();
            }
        });

        confirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                verifyCode();
                Intent intent = new Intent(view.getContext(), MainHub.class);
                startActivityForResult(intent, 0);
            }
        });
    }


    private void verifyCode() {
        String userCode = codeTF.getText().toString();

        String userName = usernameTF.getText().toString();
        String phoneNum = mobileTF.getText().toString();

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String uid = currentUser.getUid();

        fData = FirebaseDatabase.getInstance().getReference().child("Users").child(uid);


        HashMap<String, String> userMap = new HashMap<>();
        userMap.put("username", userName);
        userMap.put("pnumber", phoneNum);
        userMap.put("fullname", "default");
        userMap.put("email", "default");
        userMap.put("image", "default");
        userMap.put("small_image", "default");

        fData.setValue(userMap);

        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verCode, userCode);
        signInWithPhoneAuthCredential(credential);
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {

        fAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            String dToken = FirebaseInstanceId.getInstance().getToken();
                            String currentUID = fAuth.getCurrentUser().getUid();

                            fData.child(currentUID).child("deviceToken").setValue(dToken).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(getApplicationContext(), "Verification Successful", Toast.LENGTH_LONG).show();
                                }
                            });

                        } else {
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException){
                                Toast.makeText(getApplicationContext(), "Incorrect Verification Code", Toast.LENGTH_LONG).show();

                            }
                        }
                    }
                });
    }

    private void sendVerCode() {

        String pNum = mobileTF.getText().toString();

        if (pNum.isEmpty()) {
            mobileTF.setError("Please enter a phone number");
            mobileTF.requestFocus();
            return;
        }

        if (pNum.length() != 13) {
            mobileTF.setError("Please enter a valid phone number");
            mobileTF.requestFocus();
            return;
        }

        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                pNum,               // Phone number to verify
                60,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                this,               // Activity (for callback binding)
                mCallbacks);        // OnVerificationStateChangedCallbacks


    }


    PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        @Override
        public void onVerificationCompleted(PhoneAuthCredential credential) {
            signInWithPhoneAuthCredential(credential);
        }

        @Override
        public void onVerificationFailed(FirebaseException e) {

        }

        @Override
        public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            super.onCodeSent(s, forceResendingToken);
            verCode = s;
        }
    };
}
