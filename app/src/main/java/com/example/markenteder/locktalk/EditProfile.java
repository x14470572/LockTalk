package com.example.markenteder.locktalk;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import org.w3c.dom.Text;

import java.util.Random;

import de.hdodenhof.circleimageview.CircleImageView;

public class EditProfile extends AppCompatActivity {

    private DatabaseReference fData;
    private FirebaseUser fCurUser;
    private StorageReference fImageStorage;

    private CircleImageView uDisplayImage;
    private TextView uName;
    private TextView fName;
    private TextView uEmail;
    private TextView pNumber;
    private Button saveBtn;
    private Button saveImgBtn;
    private EditText editEmail;
    private EditText editFName;

    private static final int GALLERY_PICK = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        Toolbar toolBar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolBar);

        uDisplayImage = (CircleImageView) findViewById(R.id.displayImage);
        uName = (TextView) findViewById(R.id.uNameTV);
        fName = (TextView) findViewById(R.id.fNameTV);
        uEmail = (TextView) findViewById(R.id.emailTV);
        pNumber = (TextView) findViewById(R.id.mobileTV);

        fCurUser = FirebaseAuth.getInstance().getCurrentUser();
        fImageStorage = FirebaseStorage.getInstance().getReference();

        String current_uid = fCurUser.getUid();

        fData = FirebaseDatabase.getInstance().getReference().child("Users").child(current_uid);

        fData.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String username = dataSnapshot.child("username").getValue().toString();
                String image = dataSnapshot.child("image").getValue().toString();
                String pnumber = dataSnapshot.child("pnumber").getValue().toString();
                String small_image = dataSnapshot.child("small_image").getValue().toString();
                String email = dataSnapshot.child("email").getValue().toString();
                String fullname = dataSnapshot.child("fullname").getValue().toString();

                uName.setText(username);
                fName.setText(fullname);
                uEmail.setText(email);
                pNumber.setText(pnumber);

                Picasso.get().load(image).into(uDisplayImage);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        saveBtn = (Button) findViewById(R.id.saveBtn);
        editEmail = (EditText) findViewById(R.id.emailTF);
        editFName = (EditText) findViewById(R.id.nameTF);

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String newEmail = editEmail.getText().toString();
                String newFName = editFName.getText().toString();


                if(newEmail.isEmpty()){
                    fData.child("fullname").setValue(newFName);
                }else if(newFName.isEmpty()){
                    fData.child("email").setValue(newEmail);
                }else {
                    fData.child("email").setValue(newEmail);
                    fData.child("fullname").setValue(newFName);
                }
            }
        });

        saveImgBtn = (Button) findViewById(R.id.saveImgBtn);

        saveImgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                /*CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .start(EditProfile.this);*/

                Intent gallery = new Intent();
                gallery.setType("image/*");
                gallery.setAction(Intent.ACTION_GET_CONTENT);

                startActivityForResult(Intent.createChooser(gallery, "SELECT Image"), GALLERY_PICK);
            }
        });


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == GALLERY_PICK && resultCode == RESULT_OK){
            Uri imgUri = data.getData();

            CropImage.activity(imgUri)
                    .setAspectRatio(1, 1)
                    .start(this);
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();

                String curUserId = fCurUser.getUid();

                StorageReference path = fImageStorage.child("profile_images").child(curUserId + ".jpg");

                path.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if(task.isSuccessful()){
                            String img_url = task.getResult().getDownloadUrl().toString();

                            fData.child("image").setValue(img_url).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){
                                        Toast.makeText(EditProfile.this, "Image Successfully Uploaded", Toast.LENGTH_LONG).show();
                                    }
                                }
                            });

                        }else {
                            Toast.makeText(EditProfile.this, "Error in Uploading", Toast.LENGTH_LONG).show();
                        }
                    }
                });

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

    public static String rand(){
        Random gen = new Random();
        StringBuilder randStringBuild = new StringBuilder();
        int length = gen.nextInt(50);
        char tChar;
        for (int i = 0; i < length; i++){
            tChar = (char) (gen.nextInt(96) + 32);
            randStringBuild.append(tChar);
        }
        return randStringBuild.toString();
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
