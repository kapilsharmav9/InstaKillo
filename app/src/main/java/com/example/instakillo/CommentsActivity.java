package com.example.instakillo;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import Adapter.CommentsAdapter;
import Model.Comments;
import Model.User;
import de.hdodenhof.circleimageview.CircleImageView;

public class CommentsActivity extends AppCompatActivity {

    EditText addcomment;
    TextView tpost;
    CircleImageView image_profile;
    String postid;
    String publisher;
    FirebaseUser firebaseUser;
    private RecyclerView recyclerView;
    CommentsAdapter commentsAdapter;
    ArrayList<Comments> commentsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);
        Toolbar toolbar = findViewById(R.id.toolbarc);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Comments");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Intent intent = getIntent();
        postid = intent.getStringExtra("postid");
        publisher = intent.getStringExtra("publisher");
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        recyclerView = findViewById(R.id.recycleComents);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

        commentsList = new ArrayList<>();
        commentsAdapter = new CommentsAdapter(CommentsActivity.this, commentsList,postid);
        recyclerView.setAdapter(commentsAdapter);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        addcomment = findViewById(R.id.edit_comment);
        image_profile = findViewById(R.id.Profile_com);
        tpost = findViewById(R.id.txt_post);

        tpost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (addcomment.getText().toString().equals("")) {
                    Toast.makeText(CommentsActivity.this, "You Can't Send Empty Comment", Toast.LENGTH_SHORT).show();
                } else {
                    addComment();
                }
            }
        });
        getImage();
        readComments();
    }

    private void addComment() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("comments").child(postid);
        String commentid=databaseReference.push().getKey();
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("comment", addcomment.getText().toString());
        hashMap.put("publisher", firebaseUser.getUid());
        hashMap.put("commentid",commentid);
        databaseReference.child(commentid).setValue(hashMap);
        addNotifications();
        addcomment.setText("");
    }
    private  void addNotifications()
    {
        DatabaseReference databaseReference=FirebaseDatabase.getInstance().getReference("Notifications").child(publisher);
        HashMap<String, Object>hashMap=new HashMap<>();
        hashMap.put("userid",firebaseUser.getUid());
        hashMap.put("text","Commented : "+addcomment.getText().toString());
        hashMap.put("postid",postid);
        hashMap.put("ispost",true);
        databaseReference.push().setValue(hashMap);
    }
    private void getImage() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("users").child(firebaseUser.getUid());
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                Glide.with(getApplicationContext()).load(user.getImageurl()).into(image_profile);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void readComments() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("comments").child(postid);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                commentsList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Comments comments = snapshot.getValue(Comments.class);
                    commentsList.add(comments);

                }
                commentsAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}