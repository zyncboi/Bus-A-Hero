package com.busahero.busahero;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class DriverView extends AppCompatActivity {

    private Switch statusSwitch;
    private LocationManager locationManager;
    private DatabaseReference databaseReference;
    private static final int REQUEST_LOCATION_PERMISSION = 1;
    private TextView driverStatusTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.driverview);

        FirebaseApp.initializeApp(this);
        databaseReference = FirebaseDatabase.getInstance().getReference().child("drivers");
        FirebaseAuth fAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = fAuth.getCurrentUser();
        driverStatusTextView = findViewById(R.id.driverstatustext);

        if (currentUser != null) {
            String userId = currentUser.getUid();
            DatabaseReference userNodeRef = databaseReference.child(userId);
            Button seatsAvailableButton = findViewById(R.id.seatsavailable);
            Button standingOnlyButton = findViewById(R.id.standingonly);
            Button fullButton = findViewById(R.id.full);

            userNodeRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        String firstName = dataSnapshot.child("firstName").getValue(String.class);
                        String capacityText = dataSnapshot.child("capacity").getValue(String.class);
                        Boolean enrouteText = dataSnapshot.child("enroute").getValue(Boolean.class);

                        TextView driverTextView = findViewById(R.id.drivertextview);
                        if (firstName != null) {
                            driverTextView.setText("Welcome, " + firstName.substring(0, 1).toUpperCase() + firstName.substring(1));
                        } else {
                            driverTextView.setText("Welcome, Driver! ");
                        }
                        TextView capacityTextView = findViewById(R.id.drivercapacitytext);
                        capacityTextView.setText("Capacity: " + capacityText);

                        if (enrouteText != null && enrouteText.booleanValue()) {
                            driverStatusTextView.setText("Status: In Transit");
                            statusSwitch.setChecked(true);
                        } else {
                            DatabaseReference userNodeRef = databaseReference.child(userId);
                            userNodeRef.child("latitude").setValue(0);
                            userNodeRef.child("longitude").setValue(0);
                            driverStatusTextView.setText("Status: Idle");
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.e("FirebaseError", "Error retrieving user data: " + databaseError.getMessage());
                }
            });

            seatsAvailableButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    updateCapacity("Available");
                }
            });

            standingOnlyButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    updateCapacity("Standing Only");
                }
            });

            fullButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    updateCapacity("No More Space");
                }
            });
        } else {
            startActivity(new Intent(this, Authentication.class));
        }
        TextView logoutTextView = findViewById(R.id.logoutdriver);
        logoutTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signOut();
            }
        });

        statusSwitch = findViewById(R.id.statusswitch);
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        statusSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    if (ActivityCompat.checkSelfPermission(DriverView.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, locationListener);
                    } else {
                        ActivityCompat.requestPermissions(DriverView.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
                    }

                    FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                    if (currentUser != null) {
                        String userId = currentUser.getUid();
                        DatabaseReference userNodeRef = databaseReference.child(userId);
                        userNodeRef.child("enroute").setValue(true);
                        driverStatusTextView.setText("Status: In Transit");
                    }
                } else {
                    locationManager.removeUpdates(locationListener);

                    FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                    if (currentUser != null) {
                        String userId = currentUser.getUid();
                        DatabaseReference userNodeRef = databaseReference.child(userId);
                        userNodeRef.child("enroute").setValue(false);
                        driverStatusTextView.setText("Status: Idle");
                    }
                }
            }
        });
    }

    private final LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(@NonNull Location location) {
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();

            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser != null) {
                String userId = currentUser.getUid();
                DatabaseReference userNodeRef = databaseReference.child(userId);

                userNodeRef.child("latitude").setValue(latitude);
                userNodeRef.child("longitude").setValue(longitude);
            }
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onProviderDisabled(String provider) {
        }
    };

    private void updateCapacity(String newCapacity) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            DatabaseReference userNodeRef = databaseReference.child(userId);
            userNodeRef.child("capacity").setValue(newCapacity);

            TextView capacityTextView = findViewById(R.id.drivercapacitytext);
            capacityTextView.setText("Capacity: " + newCapacity);
        }
    }

    public void signOut() {
        FirebaseAuth.getInstance().signOut();
        startActivity(new Intent(this, Authentication.class));
        finish();
    }
}
