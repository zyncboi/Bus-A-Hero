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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class Registration extends AppCompatActivity {
    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReferenceFromUrl("https://bus-a-hero-default-rtdb.firebaseio.com/");
    FirebaseAuth fAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register);

        // Initialize Firebase Authentication
        fAuth = FirebaseAuth.getInstance();

        RadioGroup accountTypeRadioGroup = findViewById(R.id.accountType);
        final EditText firstName = findViewById(R.id.firstname);
        final EditText lastName = findViewById(R.id.lastname);
        final EditText password = findViewById(R.id.password);
        final EditText emailAddress = findViewById(R.id.emailaddress);
        final Button registerbtn = findViewById(R.id.registerbtn);
        final TextView loginnowbtn = findViewById(R.id.loginnowbtn);

        if (fAuth.getCurrentUser() != null ){
            startActivity(new Intent(getApplicationContext(), CommuterView.class));
            finish();
        }

        registerbtn.setOnClickListener(v -> {
            Log.d("Registration", String.valueOf(emailAddress));
            final String firstNameTxt = firstName.getText().toString();
            final String lastNameTxt = lastName.getText().toString();
            final String emailtxt = emailAddress.getText().toString();
            final String passwordtxt = password.getText().toString();

            if (passwordtxt.isEmpty()){
                Toast.makeText(Registration.this, "Enter Password", Toast.LENGTH_SHORT).show();
            }
            if (firstNameTxt.isEmpty()){
                Toast.makeText(Registration.this, "Enter First Name", Toast.LENGTH_SHORT).show();
            }
            if (lastNameTxt.isEmpty()){
                Toast.makeText(Registration.this, "Enter Last Name", Toast.LENGTH_SHORT).show();
            }
            if (emailtxt.isEmpty()){
                Toast.makeText(Registration.this, "Enter Email Address", Toast.LENGTH_SHORT).show();
            }

            fAuth.createUserWithEmailAndPassword(emailtxt,passwordtxt).addOnCompleteListener(task -> {

                int selectedRadioButtonId = accountTypeRadioGroup.getCheckedRadioButtonId();

                if (task.isSuccessful()){
                    Toast.makeText(Registration.this, "Registered Successfully", Toast.LENGTH_SHORT).show();

                    FirebaseUser user = fAuth.getCurrentUser();
                    String userId = user.getUid();

                    // Create a User object with user information
                    User newUser = new User(firstNameTxt, lastNameTxt, "commuter");
                    Driver newDriver = new Driver(firstNameTxt, lastNameTxt, "Test Plate", "Route", false, 0, 0, "Available","driver");

                    if (selectedRadioButtonId == R.id.userlogin) {
                        databaseReference = FirebaseDatabase.getInstance().getReference().child("users");
                        databaseReference.child(userId).setValue(newUser);

                        startActivity(new Intent(getApplicationContext(), CommuterView.class));
                    } else if (selectedRadioButtonId == R.id.driverlogin) {
                        databaseReference = FirebaseDatabase.getInstance().getReference().child("drivers");
                        databaseReference.child(userId).setValue(newDriver);

                        startActivity(new Intent(getApplicationContext(), DriverView.class));
                    }


                }else {
                    Toast.makeText(Registration.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });

        loginnowbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Registration.this, Authentication.class));
            }
        });

    }

}
