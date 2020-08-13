package Adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.instakillo.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.util.ArrayList;

import Fragments.PostDetailFragment;
import Model.Notifications;
import Model.Post;
import Model.User;
import de.hdodenhof.circleimageview.CircleImageView;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.Holder> {
    private Context context;
    private ArrayList<Notifications> notfilist;

    public NotificationAdapter(Context context, ArrayList<Notifications> notfilist) {
        this.context = context;
        this.notfilist = notfilist;
    }

    @NonNull
    @Override
    public NotificationAdapter.Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.notification_item, parent, false);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationAdapter.Holder holder, int position) {
        final Notifications notifications = notfilist.get(position);
        holder.txt_comments.setText(notifications.getText());
        userInfo(holder.image_profile, holder.username, notifications.getUserid());
        if (notifications.isPost()) {
            holder.image_post.setVisibility(View.VISIBLE);
            getPostimage(holder.image_post, notifications.getPostid());
        } else {
            holder.image_post.setVisibility(View.GONE);
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (notifications.isPost()) {
                    SharedPreferences.Editor editor = context.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit();
                    editor.putString("postid", notifications.getPostid());
                    editor.apply();
                    ((FragmentActivity) context).getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new PostDetailFragment()).commit();

                }
                else
                    {
                        SharedPreferences.Editor editor = context.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit();
                        editor.putString("profiled", notifications.getUserid());
                        editor.apply();
                        ((FragmentActivity) context).getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new PostDetailFragment()).commit();

                    }
            }
        });
    }

    @Override
    public int getItemCount() {
        return notfilist.size();
    }

    public class Holder extends RecyclerView.ViewHolder {
        CircleImageView image_profile;
        ImageView image_post;
        public TextView txt_comments, username;

        public Holder(@NonNull View itemView) {
            super(itemView);
            image_profile = itemView.findViewById(R.id.profil_n);
            image_post = itemView.findViewById(R.id.postimage_n);
            txt_comments = itemView.findViewById(R.id.comments_n);
            username = itemView.findViewById(R.id.username_n);
        }
    }

    private void userInfo(final ImageView imageView, final TextView username, String publisherid) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("users").child(publisherid);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                Glide.with(context).load(user.getImageurl()).into(imageView);
                username.setText(user.getUsername());

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getPostimage(final ImageView imageView, String postid) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("posts").child(postid);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Post post = dataSnapshot.getValue(Post.class);
                Glide.with(context).load(post.getPostimage()).into(imageView);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
