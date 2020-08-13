package com.example.instakillo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import Model.Story;
import Model.User;
import de.hdodenhof.circleimageview.CircleImageView;
import jp.shts.android.storiesprogressview.StoriesProgressView;

public class StoryActivity extends AppCompatActivity implements StoriesProgressView.StoriesListener {
    int counter = 0;
    long pressTime = 0L;
    long limit = 500L;
    StoriesProgressView storiesProgressView;
    CircleImageView image_circle;
    ImageView story_image;
    TextView story_username;
    List<String> imagesList;
    List<String> storiid;
    String userid;
    LinearLayout r_seen;
    TextView seen_number;
    ImageView delete_story;
    private TextView.OnTouchListener onTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    pressTime = System.currentTimeMillis();
                    storiesProgressView.pause();
                    return false;
                case MotionEvent.ACTION_UP:
                    long now = System.currentTimeMillis();
                    storiesProgressView.resume();
                    return limit < now - pressTime;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_story);
        seen_number = findViewById(R.id.seen_number);
        delete_story = findViewById(R.id.story_delete);
        r_seen = findViewById(R.id.r_seen);
        storiesProgressView = findViewById(R.id.stories_progress);
        image_circle = findViewById(R.id.stroryphoto);
        story_image = findViewById(R.id.Story_images);
        story_username = findViewById(R.id.story_username);
        r_seen.setVisibility(View.GONE);
        delete_story.setVisibility(View.GONE);
        userid = getIntent().getStringExtra("userid");
        if (userid.equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
            r_seen.setVisibility(View.VISIBLE);
            delete_story.setVisibility(View.VISIBLE);
        }
        getStories(userid);
        getUserinfo(userid);
        View reverse = findViewById(R.id.reverse_view);
        reverse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                storiesProgressView.reverse();
            }
        });
        reverse.setOnTouchListener(onTouchListener);
        View skip = findViewById(R.id.skip_view);
        skip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                storiesProgressView.skip();
            }
        });
        skip.setOnTouchListener(onTouchListener);
        r_seen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), FollowerActivity.class);
                intent.putExtra("id", userid);
                intent.putExtra("storyis", storiid.get(counter));
                intent.putExtra("title", "views");
                startActivity(intent);
            }
        });
        delete_story.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("story")
                        .child(userid).child(storiid.get(counter));
                databaseReference.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(getApplicationContext(), "deleted", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }
                });
            }
        });
    }

    @Override
    public void onNext() {
        Glide.with(getApplicationContext()).load(imagesList.get(++counter)).into(story_image);
        addViews(storiid.get(counter));
        seenNumber(storiid.get(counter));

    }

    @Override
    public void onPrev() {
        if ((counter - 1) < 0) return;
        Glide.with(getApplicationContext()).load(imagesList.get(--counter)).into(story_image);
        seenNumber(storiid.get(counter));
    }

    @Override
    public void onComplete() {
        finish();
    }

    @Override
    protected void onDestroy() {
        storiesProgressView.destroy();
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        storiesProgressView.resume();
        super.onResume();
    }


    @Override
    protected void onPause() {
        storiesProgressView.pause();
        super.onPause();
    }

    private void getStories(String userid) {
        imagesList = new ArrayList<>();
        storiid = new ArrayList<>();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("story").child(userid);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                imagesList.clear();
                storiid.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Story story = snapshot.getValue(Story.class);
                    long timecurrent = System.currentTimeMillis();
                    if (timecurrent > story.getTimeStart() && timecurrent < story.getTimeEnd()) {
                        imagesList.add(story.getImageurl());
                        storiid.add(story.getStoryid());
                    }
                }
                storiesProgressView.setStoriesCount(imagesList.size());
                storiesProgressView.setStoryDuration(5000L);
                storiesProgressView.setStoriesListener(StoryActivity.this);
                storiesProgressView.startStories(counter);
                Glide.with(getApplicationContext()).load(imagesList.get(counter)).into(story_image);
                addViews(storiid.get(counter));
                seenNumber(storiid.get(counter));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getUserinfo(String userid) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("users").child(userid);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                Glide.with(getApplicationContext()).load(user.getImageurl()).
                        into(image_circle);
                story_username.setText(user.getUsername());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void addViews(String storyid) {
        FirebaseDatabase.getInstance().getReference("story").child(userid)
                .child(storyid).child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(true);


    }

    private void seenNumber(String storyid) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("story").child(userid)
                .child(storyid).child("views");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                seen_number.setText("" + dataSnapshot.getChildrenCount());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

}