package com.example.instakillo;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class StartActivity extends AppCompatActivity  {
    Button btnlogin,btnReg;
    FirebaseUser firebaseUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        initview();
        firebaseUser= FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser!=null)
        {
            startActivity(new Intent(StartActivity.this,MainActivity.class));
            finish();
        }
        btnlogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(StartActivity.this,Loginactivity.class);
                startActivity(intent);
            }
        });
        btnReg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(StartActivity.this,RegisterActivity.class);
                startActivity(intent);
            }
        });
    }
    private  void initview()
    {
        btnlogin=findViewById(R.id.btnloginS);
        btnReg=findViewById(R.id.btnregS);
    }


}