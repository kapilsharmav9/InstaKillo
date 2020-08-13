package Fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.instakillo.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import Adapter.PostAdapter;
import Model.Post;


public class PostDetailFragment extends Fragment {
String postid;
RecyclerView recyclerView;
PostAdapter postAdapter;
ArrayList<Post>postList;
         @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
         View view=inflater.inflate(R.layout.fragment_post_detail, container, false);
         SharedPreferences sp=getContext().getSharedPreferences("PREFS", Context.MODE_PRIVATE);
         postid=sp.getString("postid","none");
         recyclerView=view.findViewById(R.id.recycle_photo_detail);
         recyclerView.setHasFixedSize(true);
         recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
         postList =new ArrayList<>();
         postAdapter=new PostAdapter(getContext(),postList);
         recyclerView.setAdapter(postAdapter);
         redPosts();

             return view;
         }
         private  void redPosts()
         {
             DatabaseReference databaseReference= FirebaseDatabase.getInstance().getReference().child("posts").child(postid);
             databaseReference.addValueEventListener(new ValueEventListener() {
                 @Override
                 public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                     postList.clear();
                     Post post=dataSnapshot.getValue(Post.class);
                     postList.add(post);
                     postAdapter.notifyDataSetChanged();
                 }

                 @Override
                 public void onCancelled(@NonNull DatabaseError databaseError) {

                 }
             });
         }
}