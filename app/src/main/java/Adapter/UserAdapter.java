package Adapter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.instakillo.MainActivity;
import com.example.instakillo.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

import Fragments.ProfileFragment;
import Model.User;
import de.hdodenhof.circleimageview.CircleImageView;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.Holder> {
    private Context context;
    ArrayList<User> userList;
    FirebaseUser firebaseUser;
    private  boolean isFragment;

    public UserAdapter(Context context, ArrayList<User> userList, boolean isFragment) {
        this.context = context;
        this.userList = userList;
        this.isFragment = isFragment;
    }

    @NonNull
    @Override
    public UserAdapter.Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.user_item, parent, false);
        return new Holder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final UserAdapter.Holder holder, int position) {
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        final User user = userList.get(position);
        holder.btnfollow.setVisibility(View.VISIBLE);
        holder.txtusername.setText(user.getUsername());
        holder.txtfullname.setText(user.getFullname());
        isFollowing(user.getId(), holder.btnfollow);
        Glide.with(context).load(user.getImageurl()).into(holder.profiepic);
        if (user.getId().equals(firebaseUser.getUid())) {
            holder.btnfollow.setVisibility(View.GONE);

        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isFragment) {
                    SharedPreferences.Editor editor = context.getSharedPreferences("PRESS", Context.MODE_PRIVATE).edit();
                    editor.putString("profile", user.getId());
                    editor.apply();
                    ((FragmentActivity) context).getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new ProfileFragment()).commit();
                }
                else
                    {
                        Intent intent=new Intent(context, MainActivity.class);
                        intent.putExtra("publisherid",user.getId());
                        context.startActivity(intent);
                    }
            }
        });
        holder.btnfollow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (holder.btnfollow.getText().toString().equals("follow")) {
                    FirebaseDatabase.getInstance().getReference().child("Follow").child(firebaseUser.getUid())
                            .child("following").child(user.getId()).setValue(true);
                    FirebaseDatabase.getInstance().getReference().child("Follow").child(firebaseUser.getUid())
                            .child("followers").child(firebaseUser.getUid()).setValue(true);

                    addNotifications(user.getId());
                } else {
                    FirebaseDatabase.getInstance().getReference().child("Follow").child(firebaseUser.getUid())
                            .child("following").child(user.getId()).removeValue();
                    FirebaseDatabase.getInstance().getReference().child("Follow").child(firebaseUser.getUid())
                            .child("followers").child(firebaseUser.getUid()).removeValue();

                }
            }
        });
    }

    private void addNotifications(String userid) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Notifications").child(userid);
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("userid", firebaseUser.getUid());
        hashMap.put("text", "started following you");
        hashMap.put("postid", "");
        hashMap.put("ispost", false);
        databaseReference.push().setValue(hashMap);
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public class Holder extends RecyclerView.ViewHolder {
        public CircleImageView profiepic;
        public Button btnfollow;
        public TextView txtusername, txtfullname;

        public Holder(@NonNull View itemView) {
            super(itemView);
            profiepic = itemView.findViewById(R.id.profile_pic);
            btnfollow = itemView.findViewById(R.id.btn_follow);
            txtfullname = itemView.findViewById(R.id.fullname);
            txtusername = itemView.findViewById(R.id.userName);
        }
    }

    private void isFollowing(final String userid, final Button button) {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                .child("Follow").child(firebaseUser.getUid()).child("following");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.child(userid).exists()) {
                    button.setText(" following ");
                } else {
                    button.setText("follow");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
