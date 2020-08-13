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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.instakillo.MainActivity;
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

import Model.Comments;
import Model.User;
import de.hdodenhof.circleimageview.CircleImageView;

public class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.holder> {
    private Context context;
    private ArrayList<Comments> commentsList;
    FirebaseUser firebaseUser;
    String postid;

    public CommentsAdapter(Context context, ArrayList<Comments> commentsList, String postid) {
        this.context = context;
        this.commentsList = commentsList;
        this.postid = postid;
    }

    @NonNull
    @Override
    public CommentsAdapter.holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.comments_item, parent, false);
        return new holder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentsAdapter.holder holder, int position) {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        final Comments comments = commentsList.get(position);
        holder.tcomments.setText(comments.getComment());
        holder.tusername.setText(comments.getPublisher());
        getUserInfo(holder.image_profile, holder.tusername, comments.getPublisher());
        holder.tcomments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, MainActivity.class);
                intent.putExtra("publisherid", comments.getPublisher());
                context.startActivity(intent);
            }
        });
        holder.image_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, MainActivity.class);
                intent.putExtra("publisherid", comments.getPublisher());
                context.startActivity(intent);
            }
        });
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (comments.getPublisher().equals(firebaseUser.getUid())) {
                    AlertDialog alertdialog = new AlertDialog.Builder(context).create();
                    alertdialog.setTitle("Do you want to delete");
                    alertdialog.setButton(AlertDialog.BUTTON_NEUTRAL, "No",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }

                            });
                    alertdialog.setButton(AlertDialog.BUTTON_POSITIVE, "Yes",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    FirebaseDatabase.getInstance().getReference("comments")
                                            .child(postid).child(comments.getCommentid())
                                            .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Toast.makeText(context, "Deleted", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                                    dialog.dismiss();
                                }
                            });
                    alertdialog.show();
                }
                return true;
            }
        });

    }

    @Override
    public int getItemCount() {
        return commentsList.size();
    }

    public class holder extends RecyclerView.ViewHolder {
        CircleImageView image_profile;
        TextView tusername, tcomments;

        public holder(@NonNull View itemView) {
            super(itemView);
            image_profile = itemView.findViewById(R.id.profiee);
            tcomments = itemView.findViewById(R.id.comments);
            tusername = itemView.findViewById(R.id.textusername);
        }
    }

    private void getUserInfo(final ImageView profile, final TextView username, String publisherid) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("users").child(publisherid);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                Glide.with(context).load(user.getImageurl()).into(profile);
                username.setText(user.getUsername());

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
