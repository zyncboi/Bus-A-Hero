package com.busahero.busahero;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class AvailableBusses extends AppCompatActivity {
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.availablebusses);

        Toolbar toolbar = findViewById(R.id.availablebussestoolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle("Available Buses");
        }

        // Initialize Firebase
        databaseReference = FirebaseDatabase.getInstance().getReference("drivers");

        // Retrieve and display enroute drivers
        retrieveEnrouteDrivers();
    }

    private void retrieveEnrouteDrivers() {
        final LinearLayout cardViewContainer = findViewById(R.id.cardViewContainer);

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Clear the existing CardViews
                cardViewContainer.removeAllViews();

                int enrouteDriversCount = 0; // Counter for enroute drivers

                for (DataSnapshot driverSnapshot : dataSnapshot.getChildren()) {
                    Driver driver = driverSnapshot.getValue(Driver.class);

                    if (driver != null && driver.isEnroute()) {
                        // Create a CardView for each enroute driver
                        CardView cardView = createCardView(driver);

                        // Add the dynamically created CardView to the LinearLayout
                        cardViewContainer.addView(cardView);

                        enrouteDriversCount++; // Increment the counter
                    }
                }

                Log.d("AvailableBusses", "Enroute drivers count: " + enrouteDriversCount);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle database error if needed
            }
        });
    }

    private CardView createCardView(Driver driver) {
        CardView cardView = new CardView(this);
        CardView.LayoutParams layoutParams = new CardView.LayoutParams(
                CardView.LayoutParams.MATCH_PARENT,
                CardView.LayoutParams.WRAP_CONTENT
        );
        layoutParams.setMargins(16, 16, 16, 16);
        cardView.setLayoutParams(layoutParams);
        cardView.setCardElevation(getResources().getDimension(R.dimen.card_elevation));
        cardView.setRadius(getResources().getDimension(R.dimen.card_corner_radius));

        // Create a vertical LinearLayout to hold TextViews one below the other
        LinearLayout verticalLayout = new LinearLayout(this);
        verticalLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        verticalLayout.setOrientation(LinearLayout.VERTICAL);

        // Create TextViews for displaying driver information
        TextView nameTextView = new TextView(this);
        nameTextView.setText("Driver: " + driver.getFirstName() + " " + driver.getLastName());
        nameTextView.setTextSize(16);
        nameTextView.setPadding(16, 16, 16, 8);

        TextView plateNumberTextView = new TextView(this);
        plateNumberTextView.setText("Plate Number: " + driver.getLicensePlate());
        plateNumberTextView.setTextSize(16);
        plateNumberTextView.setPadding(16, 8, 16, 8);

        TextView routeTextView = new TextView(this);
        routeTextView.setText("Route: " + driver.getRoute());
        routeTextView.setTextSize(16);
        routeTextView.setPadding(16, 8, 16, 8);

        TextView capacityTextView = new TextView(this);
        capacityTextView.setText("Capacity: " + driver.getCapacity());
        capacityTextView.setTextSize(16);
        capacityTextView.setPadding(16, 8, 16, 16);

        // Add TextViews to the vertical LinearLayout
        verticalLayout.addView(nameTextView);
        verticalLayout.addView(plateNumberTextView);
        verticalLayout.addView(routeTextView);
        verticalLayout.addView(capacityTextView);

        // Add the vertical LinearLayout to the CardView
        cardView.addView(verticalLayout);

        // Customize CardView appearance here as needed
        cardView.setCardBackgroundColor(ContextCompat.getColor(this, R.color.card_background_color));

        return cardView;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}