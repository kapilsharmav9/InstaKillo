package com.example.instakillo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Loginactivity extends AppCompatActivity {
    EditText eemail, epassword;
    Button btnlogin;
    FirebaseAuth firebaseAuth;
    DatabaseReference databaseReference;
    ProgressDialog pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loginactivity);
        initview();
        btnlogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pd = new ProgressDialog(Loginactivity.this);
                pd.setMessage("Please Wait...");
                pd.show();
                String email = eemail.getText().toString();
                String password = epassword.getText().toString();
                if (email.isEmpty() || password.isEmpty()) {
                    eemail.setError(" Enter Email");
                    eemail.requestFocus();
                    epassword.setError("Enter Password");
                    epassword.requestFocus();

                } else
                    firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(Loginactivity.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                databaseReference = FirebaseDatabase.getInstance().getReference().child("users").child(firebaseAuth.getUid());
                                databaseReference.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        pd.dismiss();
                                        Intent intent = new Intent(Loginactivity.this, MainActivity.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(intent);
                                        finish();
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });
                            } else {
                                Toast.makeText(Loginactivity.this, "Authentication Faild", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
            }
        });

    }

    private void initview() {
        eemail = findViewById(R.id.edtemailL);
        epassword = findViewById(R.id.editpasswordL);
        btnlogin = findViewById(R.id.btnLoginL);
        firebaseAuth = FirebaseAuth.getInstance();
    }
}