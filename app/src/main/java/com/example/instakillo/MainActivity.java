package com.example.instakillo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.StorageReference;

import Fragments.HomeFragment;
import Fragments.NotificationFragment;
import Fragments.ProfileFragment;
import Fragments.SearchFragment;

public class MainActivity extends AppCompatActivity {
    BottomNavigationView bottomNavigationView;
    Fragment selectedfragment = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(navigationitemSelectedlistner);


        Bundle bundle=getIntent().getExtras(); getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new HomeFragment()).commit();
        if(bundle!=null)
        {
            String publiherid=bundle.getString("publisherid");
            SharedPreferences.Editor editer=getSharedPreferences("PREFS",MODE_PRIVATE).edit();
            editer.putString("profileid",publiherid);
            editer.apply();
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,new ProfileFragment()).commit();
        }else
            {
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,new HomeFragment()).commit();
            }

    }

    private BottomNavigationView.OnNavigationItemSelectedListener navigationitemSelectedlistner = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.nav_home:
                    selectedfragment = new HomeFragment();
                    break;
                case R.id.nav_search:
                    selectedfragment = new SearchFragment();
                    break;
                case R.id.nav_add:
                    selectedfragment = null;
                    startActivity(new Intent(MainActivity.this, PostActivity.class));
                    break;
                case R.id.nav_heart:
                    selectedfragment = new NotificationFragment();
                    break;
                case R.id.nav_profile:
                    SharedPreferences.Editor editor = getSharedPreferences("PRESS", MODE_PRIVATE).edit();
                    editor.putString("profileid", FirebaseAuth.getInstance().getCurrentUser().getUid());
                    editor.apply();
                    selectedfragment = new ProfileFragment();
                    break;
            }
            if (selectedfragment != null) {
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, selectedfragment).commit();
            }
            return true;
        }
    };

}
