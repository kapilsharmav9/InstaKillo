package com.example.instakillo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.MimeTypeMap;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;

import java.util.HashMap;

public class AddStoryActivity extends AppCompatActivity {
    private Uri imageuri;
    String myuri="";
    StorageTask uploadTask;
    StorageReference storageReference;
    private static final int Image_request = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_story);
        storageReference= FirebaseStorage.getInstance().getReference("story");

        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, Image_request);

    }
    private  String getImageExtention(Uri uri)
    {
        ContentResolver contentResolver=getContentResolver();
        MimeTypeMap mimeTypeMap=MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }
    private  void publishStory()
    {
     final ProgressDialog pd=new ProgressDialog(AddStoryActivity.this);
     pd.setMessage("Posting..");
     pd.show();
     if (imageuri!=null)
     {
        final StorageReference imagereference=storageReference.child(System.currentTimeMillis()+","+getImageExtention(imageuri));
        uploadTask=imagereference.putFile(imageuri);
        uploadTask.continueWithTask(new Continuation() {
            @Override
            public Task<Uri> then(@NonNull Task task) throws Exception {
            if(!task.isSuccessful())
            {
                throw task.getException();
            }
            return  imagereference.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful())
                {
                    Uri downloaduri=task.getResult();
                    myuri=downloaduri.toString();
                    String myid=FirebaseAuth.getInstance().getCurrentUser().getUid();
                    DatabaseReference databaseReference=FirebaseDatabase.getInstance().getReference("story")
                            .child(myid);
                    String storyid=databaseReference.push().getKey();
                    long timeend=System.currentTimeMillis()+86400000;
                    HashMap<String,Object>hashMap=new HashMap<>();
                    hashMap.put("imageuri",myuri);
                    hashMap.put("timestart", ServerValue.TIMESTAMP);
                    hashMap.put("timeend",timeend);
                    hashMap.put("storyid",storyid);
                    hashMap.put("userid",myid);
                    databaseReference.child(storyid).setValue(hashMap);
                    pd.dismiss();
                    finish();

                }
                else
                    {
                        Toast.makeText(AddStoryActivity.this, "Failed", Toast.LENGTH_SHORT).show();
                    }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(AddStoryActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
     }else
         {
             Toast.makeText(AddStoryActivity.this, "No Image Selected", Toast.LENGTH_SHORT).show();
         }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Image_request && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageuri = data.getData();
            publishStory();

        } else {

            Toast.makeText(AddStoryActivity.this, "something went wrong", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(AddStoryActivity.this, MainActivity.class));
            finish();
        }
    }
}