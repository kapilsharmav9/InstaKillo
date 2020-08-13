package com.example.instakillo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

import Model.User;

public class RegisterActivity extends AppCompatActivity {
    EditText eusername, efullname, eemail, epassword;
    TextView txtlogin;
    Button Register;
    FirebaseAuth fAuth;
    DatabaseReference dreference;
    ProgressDialog pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        initview();
        txtlogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RegisterActivity.this, Loginactivity.class);
                startActivity(intent);
            }
        });
        Register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pd = new ProgressDialog(RegisterActivity.this);
                pd.setMessage("Please Wait...");
                pd.show();
                String fullName = efullname.getText().toString();
                String email = eemail.getText().toString();
                String username = eusername.getText().toString();
                String password = epassword.getText().toString();
                if (fullName.isEmpty() || email.isEmpty() || username.isEmpty() || password.isEmpty()) {
                    efullname.setError("Please Enter FullName");
                    efullname.requestFocus();
                    eemail.setError("Please Enter Email");
                    eemail.requestFocus();
                    eusername.setError("Please Enter UserName");
                    eusername.requestFocus();
                    epassword.setError("Please Enter Password");
                    efullname.requestFocus();
                } else if (password.length() < 6) {
                    Toast.makeText(RegisterActivity.this, "Set Passord At least 6 character", Toast.LENGTH_SHORT).show();
                }
                else
                    {
                        register(fullName,username,email,password);
                    }
            }

        });
    }

    private void register(String fullname, final String username, final String email, final String password) {

        fAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                            String userid = firebaseUser.getUid();
                            dreference = FirebaseDatabase.getInstance().getReference().child("users").child(userid);
                            HashMap<String, Object> hashMap = new HashMap<>();
                            hashMap.put("id", userid);
                            hashMap.put("username", username.toLowerCase());
                            hashMap.put("bio", "null");
                            hashMap.put("imageurl", "https://firebasestorage.googleapis.com/v0/b/instakillo-5ec60.appspot.com/o/profile.jpg?alt=media&token=e62d33e3-0aef-4cc9-8fad-03d7767d9496");
                            User user=new User();

                            dreference.setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        pd.dismiss();
                                        Intent intent=new Intent(RegisterActivity.this,MainActivity.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(intent);

                                    }
                                }
                            });

                        }else {
                            pd.dismiss();
                            Toast.makeText(RegisterActivity.this, "You Can't Register with this email", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void initview() {
        eusername = findViewById(R.id.edituserNameR);
        efullname = findViewById(R.id.editfullNameR);
        eemail = findViewById(R.id.editemailR);
        epassword = findViewById(R.id.editpasswordR);
        txtlogin = findViewById(R.id.textlogin);
        fAuth=FirebaseAuth.getInstance();
        Register=findViewById(R.id.btnregisterR);

    }

}