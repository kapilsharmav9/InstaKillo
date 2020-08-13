package Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Image;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.Placeholder;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.instakillo.CommentsActivity;
import com.example.instakillo.FollowerActivity;
import com.example.instakillo.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

import Fragments.PostDetailFragment;
import Fragments.ProfileFragment;
import Model.Post;
import Model.User;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.Holder> {
    public Context context;
    ArrayList<Post> postlist;
    private FirebaseUser firebaseUser;
    FirebaseDatabase firebaseDatabase;


    public PostAdapter(Context context, ArrayList<Post> postlist) {
        this.context = context;
        this.postlist = postlist;
    }

    @NonNull
    @Override
    public PostAdapter.Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.post_item, parent, false);
        return new PostAdapter.Holder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final PostAdapter.Holder holder, int position) {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        final Post post = postlist.get(position);
       Glide.with(context).load(post.getPostimage()).apply(new RequestOptions().placeholder(R.drawable.placeholder)).into(holder.image_post);
        if (post.getDescription().equals("")) {
            holder.txt_description.setVisibility(View.GONE);
        } else {
            holder.txt_description.setVisibility(View.VISIBLE);
            holder.txt_description.setText(post.getDescription());
        }
        publisherInfo(holder.image_profile, holder.txt_username, holder.txt_publisher, post.getPublisher());
        isLike(post.getPostid(), holder.image_like);
        nLikes(holder.txt_likes, post.getPostid());
        getComment(post.getPostid(), holder.txt_comments);
        isSaved(post.getPostid(), holder.image_save);
        holder.image_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = context.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit();
                editor.putString("profiled", post.getPublisher());
                editor.apply();
                ((FragmentActivity) context).getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new ProfileFragment()).commit();

            }
        });
        holder.txt_publisher.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = context.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit();
                editor.putString("profiled", post.getPublisher());
                editor.apply();
                ((FragmentActivity) context).getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new PostDetailFragment()).commit();

            }
        });
        holder.image_post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = context.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit();
                editor.putString("postid", post.getPostid());
                editor.apply();
                ((FragmentActivity) context).getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new PostDetailFragment()).commit();

            }
        });
        holder.txt_username.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = context.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit();
                editor.putString("profiled", post.getPublisher());
                editor.apply();
                ((FragmentActivity) context).getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new ProfileFragment()).commit();

            }
        });
        holder.image_like.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (holder.image_like.getTag().equals("like")) {
                    FirebaseDatabase.getInstance().getReference().child("likes").child(post.getPostid()).child(firebaseUser.getUid()).setValue(true);
                    addNotifications(post.getPublisher(), post.getPostid());
                } else {
                    FirebaseDatabase.getInstance().getReference().child("likes").child(post.getPostid()).child(firebaseUser.getUid()).removeValue();
                }
            }
        });
        holder.image_comment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, CommentsActivity.class);
                intent.putExtra("postid", post.getPostid());
                intent.putExtra("publisher", post.getPublisher());
                context.startActivity(intent);
            }
        });
        holder.image_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (holder.image_save.getTag().equals("save")) {
                    FirebaseDatabase.getInstance().getReference().child("saves").child(firebaseUser.getUid())
                            .child(post.getPostid()).setValue(true);
                } else {
                    FirebaseDatabase.getInstance().getReference().child("saves").child(firebaseUser.getUid())
                            .child(post.getPostid()).removeValue();
                }
            }
        });
        holder.txt_likes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, FollowerActivity.class);
                intent.putExtra("id", post.getPostid());
                intent.putExtra("title", "likes");
                context.startActivity(intent);
            }
        });
        holder.more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popupMenu = new PopupMenu(context, v);
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.edit_menu:
                                editpost(post.getPostid());
                                return true;
                            case R.id.delete_menu:
                                FirebaseDatabase.getInstance().getReference("posts")
                                        .child(post.getPostid()).removeValue()
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    Toast.makeText(context, "Post deleted", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                                return true;
                            case R.id.report_menu:
                                Toast.makeText(context, "Reported", Toast.LENGTH_SHORT).show();
                                return true;
                            default:
                                return false;
                        }

                    }
                });
                popupMenu.inflate(R.menu.post_menu);
                if (!post.getPublisher().equals(firebaseUser.getUid())) {
                    popupMenu.getMenu().findItem(R.id.edit_menu).setVisible(false);

                    popupMenu.getMenu().findItem(R.id.delete_menu).setVisible(false);
                }
                popupMenu.show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return postlist.size();
    }

    public class Holder extends RecyclerView.ViewHolder {
        public ImageView image_like, image_comment, image_save, image_profile, image_post, more;
        public TextView txt_likes, txt_description, txt_comments, txt_username, txt_publisher;

        public Holder(@NonNull View itemView) {
            super(itemView);
            image_like = itemView.findViewById(R.id.image_like);
            image_comment = itemView.findViewById(R.id.image_comment);
            image_save = itemView.findViewById(R.id.image_save);
            image_profile = itemView.findViewById(R.id.iprofile_pic);
            image_post = itemView.findViewById(R.id.image_post);
            txt_likes = itemView.findViewById(R.id.txt_like);
            txt_description = itemView.findViewById(R.id.Tdescription);
            txt_comments = itemView.findViewById(R.id.Tcomments);
            txt_publisher = itemView.findViewById(R.id.publisher);
            txt_username = itemView.findViewById(R.id.usernamep);
            more = itemView.findViewById(R.id.image_more);

        }


    }

    private void getComment(String postid, final TextView comments) {

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("comments").child(postid);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                comments.setText("View All " + dataSnapshot.getChildrenCount() + "comments");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void publisherInfo(final ImageView image_profile, final TextView username, final TextView publisher, String userid) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("users").child(userid);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                Glide.with(context).load(user.getImageurl()).into(image_profile);
                username.setText(user.getUsername());
                publisher.setText(user.getUsername());

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void isLike(String postid, final ImageView imageView) {
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        final DatabaseReference databaseReference = FirebaseDatabase.getInstance()
                .getReference().child("likes").child(postid);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.child(firebaseUser.getUid()).exists()) {
                    imageView.setImageResource(R.drawable.ic_liked);
                    imageView.setTag("liked");
                } else {
                    imageView.setImageResource(R.drawable.ic_like);
                    imageView.setTag("like");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void addNotifications(String userid, String postid) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Notifications").child(userid);
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("userid", firebaseUser.getUid());
        hashMap.put("text", "liked your post");
        hashMap.put("postid", postid);
        hashMap.put("ispost", true);
        databaseReference.push().setValue(hashMap);
    }

    private void nLikes(final TextView likes, String postid) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("likes").child(postid);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                likes.setText(dataSnapshot.getChildrenCount() + " likes");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void isSaved(final String postid, final ImageView image_post) {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("saves").child(firebaseUser.getUid());
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.child(postid).exists()) {
                    image_post.setImageResource(R.drawable.ic_save_black);
                    image_post.setTag("saved");
                } else {
                    image_post.setImageResource(R.drawable.ic_save);
                    image_post.setTag("save");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void editpost(final String postid) {
        final AlertDialog.Builder alertdiolog = new AlertDialog.Builder(context);
        alertdiolog.setTitle("Edit Message");
        final EditText editText = new EditText(context);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        );
        editText.setLayoutParams(lp);
        alertdiolog.setView(editText);
        getText(postid, editText);
        alertdiolog.setPositiveButton("Edit", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put("description", editText.getText().toString());
                FirebaseDatabase.getInstance().getReference("posts")
                        .child(postid).updateChildren(hashMap);
            }
        });
        alertdiolog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }

        });
        alertdiolog.show();
    }

    private void getText(final String postid, final EditText editText) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("posts")
                .child(postid);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                editText.setText(dataSnapshot.getValue(Post.class).getDescription());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}

