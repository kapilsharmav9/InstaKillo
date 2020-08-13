package Fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.instakillo.EditProfileActivity;
import com.example.instakillo.FollowerActivity;
import com.example.instakillo.OptionActivity;
import com.example.instakillo.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import Adapter.FotoAdapter;
import Model.Post;
import Model.User;
import de.hdodenhof.circleimageview.CircleImageView;


public class ProfileFragment extends Fragment {
    CircleImageView image_profile;
    ImageView image_options;
    TextView txt_post, txt_followers, txt_following, txt_bio, txt_username, fullname;
    FirebaseUser firebaseUser;
    String profiled;
    Button editprofile;
    ImageButton my_post, save_post;
    RecyclerView recyclerViewMypic, recyclerViewsave;
    FotoAdapter fotoAdapter;
    ArrayList<Post> postsList;
    ArrayList<Post> postsList_save;
    List<String> mysave;
    FotoAdapter fotoAdapter_save;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        image_profile = view.findViewById(R.id.profile_f);
        image_options = view.findViewById(R.id.options);
        txt_post = view.findViewById(R.id.tpostsP);
        txt_followers = view.findViewById(R.id.tfollowersP);
        txt_following = view.findViewById(R.id.followingP);
        txt_bio = view.findViewById(R.id.bio);
        txt_username = view.findViewById(R.id.pusername);
        fullname = view.findViewById(R.id.fullnamep);
        my_post = view.findViewById(R.id.my_photos);
        save_post = view.findViewById(R.id.save_post);
        editprofile = view.findViewById(R.id.edit_profilen);
        recyclerViewsave = view.findViewById(R.id.recycle_save);
        recyclerViewMypic = view.findViewById(R.id.recycle_mypic);
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        SharedPreferences sp = getContext().getSharedPreferences("PREFS", Context.MODE_PRIVATE);
        profiled = sp.getString("profileid", "none");
        recyclerViewsave.setHasFixedSize(true);
        recyclerViewsave.setLayoutManager(new GridLayoutManager(getContext(), 2));
        postsList_save = new ArrayList<>();
        fotoAdapter_save = new FotoAdapter(getContext(), postsList_save);
        recyclerViewsave.setAdapter(fotoAdapter_save);
        recyclerViewMypic.setHasFixedSize(true);
        recyclerViewMypic.setLayoutManager(new GridLayoutManager(getContext(), 2));
        postsList = new ArrayList<>();
        fotoAdapter = new FotoAdapter(getContext(), postsList);
        recyclerViewMypic.setAdapter(fotoAdapter);

        userInfo();
        getFollowers();
        getPost();
        myFotos();
        mySavePost();
        if (profiled.equals(firebaseUser.getUid())) {
            editprofile.setText("Edit Profile");
        } else {
            checkFollow();
            save_post.setVisibility(View.GONE);

        }
        editprofile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String btn = editprofile.getText().toString();
                if (btn.equals("Edit Profile")) {
                    startActivity(new Intent(getContext(), EditProfileActivity.class));
                } else if (btn.equals("Follow")) {
                    FirebaseDatabase.getInstance().getReference().child("Follow").child(firebaseUser.getUid())
                            .child("following").child(profiled).setValue(true);
                    FirebaseDatabase.getInstance().getReference().child("Follow").child(firebaseUser.getUid())
                            .child("followers").child(firebaseUser.getUid()).setValue(true);
                    addNotifications();

                } else if (btn.equals("following")) {
                    FirebaseDatabase.getInstance().getReference().child("Follow").child(firebaseUser.getUid())
                            .child("following").child(profiled).removeValue();
                    FirebaseDatabase.getInstance().getReference().child("Follow").child(profiled)
                            .child("followers").child(profiled).removeValue();
                }
            }
        });
        my_post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recyclerViewMypic.setVisibility(View.VISIBLE);
                recyclerViewsave.setVisibility(View.GONE);
            }
        });
        save_post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recyclerViewMypic.setVisibility(View.GONE);
                recyclerViewsave.setVisibility(View.VISIBLE);
            }
        });
        txt_followers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(getContext(), FollowerActivity.class);
                intent.putExtra("id",profiled);
                intent.putExtra("title","followers");
                startActivity(intent);
            }
        });
        txt_following.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(getContext(), FollowerActivity.class);
                intent.putExtra("id",profiled);
                intent.putExtra("title","following");
                startActivity(intent);
            }
        });
        image_options.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(getContext(), OptionActivity.class);
                startActivity(intent);
            }
        });
        return view;
    }

    private void addNotifications() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Notifications").child(profiled);
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("userid", firebaseUser.getUid());
        hashMap.put("text", "started following you");
        hashMap.put("postid", "");
        hashMap.put("ispost", false);
        databaseReference.push().setValue(hashMap);
    }

    private void userInfo() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("users").child(profiled);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (getContext() == null) {
                    return;
                }
                User user = dataSnapshot.getValue(User.class);
//                   Glide.with(getContext()).load(user.getImageurl()).into(image_profile);
//                    txt_username.setText(user.getUsername());
//                    fullname.setText(user.getFullname());
//                    txt_bio.setText(user.getBio());

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void checkFollow() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Follow")
                .child(firebaseUser.getUid()).child("following");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.child(profiled).exists()) {
                    editprofile.setText("following");
                } else {
                    editprofile.setText("Follow");

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void getFollowers() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference()
                .child("Follow").child(profiled).child("following");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                txt_followers.setText("" + dataSnapshot.getChildrenCount());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        DatabaseReference databaseReference1 = FirebaseDatabase.getInstance().getReference()
                .child("Follow").child(profiled).child("following");
        databaseReference1.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                txt_following.setText("" + dataSnapshot.getChildrenCount());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getPost() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("posts");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int i = 0;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Post post = snapshot.getValue(Post.class);
                    if (post.getPublisher().equals(profiled)) {
                        i++;
                    }
                }
                txt_post.setText("" + i);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void myFotos() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("posts");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                postsList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Post post = snapshot.getValue(Post.class);
                    if (post.getPublisher().equals(profiled)) {
                        postsList.add(post);
                    }
                }
                Collections.reverse(postsList);
                fotoAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void mySavePost() {
        mysave = new ArrayList<>();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("saves").child(firebaseUser.getUid());
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    mysave.add(snapshot.getKey());
                }
                readSaves();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void readSaves() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("posts");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                postsList_save.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Post post = snapshot.getValue(Post.class);
                    for (String id : mysave) {
                        if (post.getPostid().equals(id)) {
                            postsList_save.add(post);
                        }
                    }
                }
                fotoAdapter_save.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}