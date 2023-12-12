package com.busahero.busahero;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.core.view.GravityCompat;
import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.security.cert.PolicyNode;
import java.util.ArrayList;
import java.util.List;


public class CommuterView extends AppCompatActivity implements LocationListener, OnMapReadyCallback, NavigationView.OnNavigationItemSelectedListener {
    private static final int REQUEST_LOCATION_PERMISSION = 1;
    private LocationManager locationManager;
    private GoogleMap mMap;
    private final List<LatLng> locationList = new ArrayList<>();
    private boolean initialLocationUpdate = true;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private Handler locationUpdateHandler = new Handler();
    private TextView commuterNameTextView;
    private TextView commuterUUIDTextView;
    private Marker userMarker;
    private static final int MAX_ACCURACY_THRESHOLD = 50; // Adjust this threshold as needed
    private Marker nearestDriverMarker; // Store the nearest driver marker
    private TextView nearestBusLabel;
    private TextView driverFullName;
    private TextView nearestBusRoute;
    private TextView nearestBusCapacity;
    private LatLng nearestBusMarkerPosition; // Added to store the LatLng position of the nearest bus marker


    @SuppressLint("MissingInflatedId")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.commuterview);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.mapFragment);
        mapFragment.getMapAsync(this);

        if (checkLocationPermission()) {
            requestLocationUpdates();
        } else {
            ActivityCompat.requestPermissions(CommuterView.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
        }

        // Initialize components
        NavigationView navigationView = findViewById(R.id.NavigationViewCommuter);
        View headerView = navigationView.getHeaderView(0); // Get the header view
        commuterNameTextView = headerView.findViewById(R.id.CommuterName);
        commuterUUIDTextView = headerView.findViewById(R.id.CommuterUUID);
        nearestBusLabel = findViewById(R.id.nearestBusLabel);
        driverFullName = findViewById(R.id.driverFullName);
        nearestBusRoute = findViewById(R.id.nearestBusRoute);
        nearestBusCapacity = findViewById(R.id.nearestBusCapacity);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("users").child(currentUser.getUid());

            userRef.addValueEventListener(new ValueEventListener() {
                @SuppressLint("SetTextI18n")
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        String firstName = dataSnapshot.child("firstName").getValue(String.class);
                        String lastName = dataSnapshot.child("lastName").getValue(String.class);
                        String newUserID = currentUser.getUid();

                        if (firstName != null && lastName != null) {
                            // Combine first name and last name and update the UI
                            String newName = firstName + " " + lastName;
                            commuterNameTextView.setText(newName);
                            commuterUUIDTextView.setText("ID: " + newUserID);
                        } else {
                            Log.e("CommuterView", "First name or last name is null.");
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.e("CommuterView", "Error reading user data: " + databaseError.getMessage());
                }
            });
        }

        // Initialize the toolbar
        Toolbar toolbar = findViewById(R.id.ActionBar);
        setSupportActionBar(toolbar);

        // Initialize the drawer layout and toggle button
        drawerLayout = findViewById(R.id.drawer_layout);
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);

        navigationView.setNavigationItemSelectedListener(this);

        // Initialize the toggle button
        ImageView toggleImageView = findViewById(R.id.toggleNavigationDrawer);
        toggleImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                } else {
                    drawerLayout.openDrawer(GravityCompat.START);
                }
            }
        });

        locationUpdateHandler.postDelayed(locationUpdateRunnable, 1000);

        Button zoomLocationButton = findViewById(R.id.zoomlocation);
        zoomLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Check if mMap is not null
                if (mMap != null) {
                    // Check if the last known location is available
                    Location lastKnownLocation = getLastKnownLocation();
                    if (lastKnownLocation != null) {
                        double latitude = lastKnownLocation.getLatitude();
                        double longitude = lastKnownLocation.getLongitude();
                        LatLng currentLocation = new LatLng(latitude, longitude);

                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15f));
                    } else {
                        Toast.makeText(CommuterView.this, "Location not available", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        Button zoomBusLocationButton = findViewById(R.id.zoomBusLocation);
        zoomBusLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Check if mMap is not null and nearestBusMarkerPosition is not null
                if (mMap != null && nearestBusMarkerPosition != null) {
                    // Move the camera to the nearest bus driver's location with an appropriate zoom level
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(nearestBusMarkerPosition, 15f));
                } else {
                    Toast.makeText(CommuterView.this, "Nearest bus location not available", Toast.LENGTH_SHORT).show();
                }
            }
        });

        Log.d("CommuterView", "onCreate: Location permission enabled: " + checkLocationPermission());
        Log.d("CommuterView", "onCreate: Location services enabled: " + isLocationEnabled());
    }

    private void requestLocationUpdates() {
        try {
            if (isLocationEnabled()) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 10, this);
            } else {
                Toast.makeText(this, "Please enable location services.", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        } catch (SecurityException e) {
            Log.e("CommuterView", "SecurityException: " + e.getMessage());
        }
    }

    private final Runnable locationUpdateRunnable = new Runnable() {
        @Override
        public void run() {
            requestLocationUpdates();
            locationUpdateHandler.postDelayed(this, 1000); // 1 second in milliseconds
        }
    };

    private boolean isLocationEnabled() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    private boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
            return false;
        }
        return true;
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        // Set a marker click listener to handle marker clicks
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                // Handle marker click here, if needed
                return false;
            }
        });
    }

    @Override
    public void onLocationChanged(Location location) {
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        Log.d("CommuterView", "Latitude: " + latitude + "\nLongitude: " + longitude);

        LatLng newLocation = new LatLng(latitude, longitude);

        if (userMarker != null) {
            userMarker.remove();
        }

        listenToDriverLocations();
        userMarker = mMap.addMarker(new MarkerOptions().position(newLocation).title("You"));

        if (initialLocationUpdate) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(newLocation, 18.9f));
            initialLocationUpdate = false;
        }
        Log.d("CommuterView", "onLocationChanged: Location changed. Marker updated.");
    }

    public void signOut() {
        FirebaseAuth.getInstance().signOut();
        startActivity(new Intent(this, Authentication.class));
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.nav_busses) {
            startActivity(new Intent(this, AvailableBusses.class));
        } else if (id == R.id.nav_logout) {
            signOut();
        }

        // Close the navigation drawer
        drawerLayout.closeDrawer(GravityCompat.START);

        return true;
    }

    private void listenToDriverLocations() {
        DatabaseReference driversRef = FirebaseDatabase.getInstance().getReference().child("drivers");

        // Get the user's location
        Location userLocation = getLastKnownLocation();

        if (userLocation == null) {
            // Handle the case where user location is not available
            Log.e("CommuterView", "User location is not available");
            return;
        }

        driversRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                double nearestDistance = Double.MAX_VALUE;
                DataSnapshot nearestDriverSnapshot = null;

                // Remove the existing nearest bus marker from the map if it exists
                if (nearestDriverMarker != null) {
                    nearestDriverMarker.remove();
                }

                // Clear all existing markers from the map (except the user marker)
                mMap.clear();

                // Add the user marker back to the map
                if (userMarker != null) {
                    userMarker = mMap.addMarker(new MarkerOptions()
                            .position(userMarker.getPosition())
                            .title("You")
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))); // Customize the user marker as needed
                }

                for (DataSnapshot driverSnapshot : dataSnapshot.getChildren()) {
                    Double latitude = driverSnapshot.child("latitude").getValue(Double.class);
                    Double longitude = driverSnapshot.child("longitude").getValue(Double.class);

                    if (latitude != null && longitude != null) {
                        LatLng driverLocation = new LatLng(latitude, longitude);

                        // Add marker for this driver
                        BitmapDescriptor busIcon = BitmapDescriptorFactory.fromResource(R.drawable.bus_icon);
                        String capacity = driverSnapshot.child("capacity").getValue(String.class);
                        String route = driverSnapshot.child("route").getValue(String.class);
                        String firstName = driverSnapshot.child("firstName").getValue(String.class);

                        String snippet = "Capacity: " + capacity;

                        MarkerOptions markerOptions = new MarkerOptions()
                                .position(driverLocation)
                                .icon(busIcon)
                                .title(route)
                                .snippet(snippet);

                        mMap.addMarker(markerOptions);

                        // Calculate the distance between user and driver locations
                        Location driverLocationObj = new Location("DriverLocation");
                        driverLocationObj.setLatitude(latitude);
                        driverLocationObj.setLongitude(longitude);

                        float distance = userLocation.distanceTo(driverLocationObj);

                        if (distance < nearestDistance) {
                            nearestDistance = distance;
                            nearestDriverSnapshot = driverSnapshot; // Update nearestDriverSnapshot
                        }
                    }
                }

                // Update the UI with the nearest driver's information
                if (nearestDriverSnapshot != null) {
                    nearestBusMarkerPosition = new LatLng(
                            nearestDriverSnapshot.child("latitude").getValue(Double.class),
                            nearestDriverSnapshot.child("longitude").getValue(Double.class)
                    );



                    // You can update other UI elements here as needed
                    String firstName = nearestDriverSnapshot.child("firstName").getValue(String.class);
                    String capacity = nearestDriverSnapshot.child("capacity").getValue(String.class);

                    nearestBusLabel.setText("Nearest Bus");
                    driverFullName.setText("Name: " + firstName);
                    nearestBusRoute.setText("Route: " + nearestDriverSnapshot.child("route").getValue(String.class));
                    nearestBusCapacity.setText("Capacity: " + capacity);
                } else {
                    // No nearest driver found
                    nearestBusLabel.setText("No buses nearby");
                    driverFullName.setText("");
                    nearestBusRoute.setText("");
                    nearestBusCapacity.setText("");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("CommuterView", "Error reading driver locations: " + databaseError.getMessage());
            }
        });
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        locationUpdateHandler.removeCallbacks(locationUpdateRunnable);

        if (nearestDriverMarker != null) {
            nearestDriverMarker.remove();
        }
    }

    private Location getLastKnownLocation() {
        try {
            if (isLocationEnabled() && checkLocationPermission()) {
                return locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            }
        } catch (SecurityException e) {
            Log.e("CommuterView", "SecurityException: " + e.getMessage());
        }
        return null;
    }
}
