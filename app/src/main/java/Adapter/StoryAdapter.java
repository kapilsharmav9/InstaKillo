package Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.instakillo.AddStoryActivity;
import com.example.instakillo.R;
import com.example.instakillo.StoryActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import Model.Story;
import Model.User;
import de.hdodenhof.circleimageview.CircleImageView;

public class StoryAdapter extends RecyclerView.Adapter<StoryAdapter.Holder> {
    private ArrayList<Story> storyList;
    Context context;

    public StoryAdapter(ArrayList<Story> storyList, Context context) {
        this.storyList = storyList;
        this.context = context;
    }

    @NonNull
    @Override
    public StoryAdapter.Holder onCreateViewHolder(@NonNull ViewGroup parent, int position) {
        if (position == 0) {
            View view = LayoutInflater.from(context).inflate(R.layout.add_story_item, parent, false);
            return new Holder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.stories_item, parent, false);
            return new Holder(view);
        }

    }

    @Override
    public void onBindViewHolder(@NonNull final StoryAdapter.Holder holder, int position) {
        final Story story = storyList.get(position);
        userInfo(holder, story.getUserid(), position);
        if (holder.getAdapterPosition() != 0) {
            storySeen(holder, story.getUserid());
        }
        if (holder.getAdapterPosition() == 0) {
            myStory(holder.text_add_story, holder.image_plus, false);
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (holder.getAdapterPosition() == 0) {
                    myStory(holder.text_add_story, holder.image_plus, true);
                } else {
                    Intent intent = new Intent(context, StoryActivity.class);
                    intent.putExtra("userid", story.getUserid());
                    context.startActivity(intent);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return storyList.size();
    }

    public class Holder extends RecyclerView.ViewHolder {
        TextView username, text_add_story;
        CircleImageView story_image, story_seen, image_plus, iamge_add_story;

        public Holder(@NonNull View itemView) {
            super(itemView);
            story_image = itemView.findViewById(R.id.story_photo);
            iamge_add_story = itemView.findViewById(R.id.story_photo);
            story_seen = itemView.findViewById(R.id.stories_seen);
            image_plus = itemView.findViewById(R.id.story_plus);
            username = itemView.findViewById(R.id.story_username);
            text_add_story = itemView.findViewById(R.id.add_story_text);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return 0;
        }
        return 1;
    }

    private void userInfo(final Holder holder, String userid, final int pos) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("users").child(userid);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                Glide.with(context).load(user.getImageurl()).into(holder.story_image);
                if (pos != 0) {
                    Glide.with(context).load(user.getImageurl()).into(holder.story_seen);
                    holder.username.setText(user.getUsername());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void myStory(final TextView textView, final ImageView imageView, final boolean click) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("story")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int count = 0;
                long timeCurrent = System.currentTimeMillis();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Story story = snapshot.getValue(Story.class);
                    if (timeCurrent > story.getTimeStart() && timeCurrent < story.getTimeEnd()) {
                        count++;
                    }
                }
                if (click) {
                    if (count > 0) {
                        AlertDialog alertDialog = new AlertDialog.Builder(context).create();
                        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "View Story", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(context, StoryActivity.class);
                                intent.putExtra("userid", FirebaseAuth.getInstance().getCurrentUser().getUid());
                                context.startActivity(intent);
                                dialog.dismiss();
                            }
                        });
                        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Add Story", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(context, AddStoryActivity.class);
                                context.startActivity(intent);
                                dialog.dismiss();
                            }
                        });
                        alertDialog.show();
                    } else {
                        Intent intent = new Intent(context, AddStoryActivity.class);
                        context.startActivity(intent);
                    }

                } else if (count > 0) {
                    textView.setText("My Story");
                    imageView.setVisibility(View.GONE);
                } else {
                    textView.setText("Add Story");
                    imageView.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void storySeen(final Holder holder, String userid) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("story")
                .child(userid);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int i = 0;

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    if (!snapshot.child("views")
                            .child(FirebaseAuth.getInstance().getCurrentUser().getUid()).exists() && System.currentTimeMillis() < snapshot.getValue(Story.class).getTimeEnd()) {
                        i++;
                    }
                }
                if (i > 0) {
                    holder.story_image.setVisibility(View.VISIBLE);
                    holder.story_seen.setVisibility(View.GONE);
                } else {
                    holder.story_image.setVisibility(View.GONE);
                    holder.story_seen.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }
}
