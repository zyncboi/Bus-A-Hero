package com.busahero.busahero;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.*;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Authentication extends AppCompatActivity {
    FirebaseAuth fAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.authentication);

        fAuth = FirebaseAuth.getInstance();

        final EditText password = findViewById(R.id.password);
        final EditText emailAddress = findViewById(R.id.emailaddress);
        final Button loginbtn = findViewById(R.id.loginbtn);
        final TextView registernowbtn = findViewById(R.id.registernowbtn);

        if (fAuth.getCurrentUser() != null) {
            checkUserTypeAndRedirect();
        }

        loginbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String emailtxt = emailAddress.getText().toString();
                final String passwordtxt = password.getText().toString();

                if (passwordtxt.isEmpty()) {
                    Toast.makeText(Authentication.this, "Enter Password", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (emailtxt.isEmpty()) {
                    Toast.makeText(Authentication.this, "Enter Email Address", Toast.LENGTH_SHORT).show();
                    return;
                }

                fAuth.signInWithEmailAndPassword(emailtxt, passwordtxt).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            checkUserTypeAndRedirect();
                        } else {
                            // Handle authentication failure
                            if (task.getException() instanceof FirebaseAuthInvalidUserException) {
                                // Invalid user (email not registered)
                                Toast.makeText(Authentication.this, "Invalid email address. Please register.", Toast.LENGTH_SHORT).show();
                            } else if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                // Invalid password
                                Toast.makeText(Authentication.this, "Invalid password. Please try again.", Toast.LENGTH_SHORT).show();
                            } else {
                                // Other authentication failure
                                Toast.makeText(Authentication.this, "Authentication failed. Please try again.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
            }
        });

        registernowbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Authentication.this, Registration.class));
            }
        });
    }

    private void checkUserTypeAndRedirect() {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");

        Log.d("Authentication", "Checking user type for UserID: " + currentUserId);

        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(currentUserId)) {
                    // User exists in the "users" category, so they are a commuter.
                    Log.d("Authentication", "User is a commuter. Redirecting to CommuterView.class");
                    startActivity(new Intent(getApplicationContext(), CommuterView.class));
                } else {
                    DatabaseReference driversRef = FirebaseDatabase.getInstance().getReference("drivers");
                    driversRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.hasChild(currentUserId)) {
                                // User exists in the "drivers" category, so they are a driver.
                                Log.d("Authentication", "User is a driver. Redirecting to DriverView.class");
                                startActivity(new Intent(getApplicationContext(), DriverView.class));
                            } else {
                                // User data not found, redirect to registration.
                                Log.d("Authentication", "User data not found. Redirecting to Registration.class");
                                startActivity(new Intent(getApplicationContext(), Registration.class));
                            }
                            finish();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Log.e("Authentication", "Something Went Wrong");
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("Authentication", "Something Went Wrong");
            }
        });
    }
}
