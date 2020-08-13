package Adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.instakillo.R;

import java.util.ArrayList;

import Fragments.ProfileFragment;
import Model.Post;

public class FotoAdapter extends RecyclerView.Adapter<FotoAdapter.Holder> {
    private Context context;
    ArrayList<Post> postsList;

    public FotoAdapter(Context context, ArrayList<Post> postsList) {
        this.context = context;
        this.postsList = postsList;
    }

    @NonNull
    @Override
    public FotoAdapter.Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.fotos_item, parent, false);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FotoAdapter.Holder holder, int position) {
        final Post post = postsList.get(position);
        Glide.with(context).load(post.getPostimage()).into(holder.post_images);
        holder.post_images.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor=context.getSharedPreferences("PREFS",Context.MODE_PRIVATE).edit();
                editor.putString("postid",post.getPostid());
                editor.apply();
                ((FragmentActivity)context).getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,new ProfileFragment()).commit();

            }
        });
    }

    @Override
    public int getItemCount() {
        return postsList.size();
    }

    public class Holder extends RecyclerView.ViewHolder {
        ImageView post_images;

        public Holder(@NonNull View itemView) {
            super(itemView);
            post_images = itemView.findViewById(R.id.post_images);
        }
    }
}
