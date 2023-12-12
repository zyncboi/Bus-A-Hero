package com.busahero.busahero;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class DriverAdapter extends RecyclerView.Adapter<DriverAdapter.ViewHolder> {
    private List<Driver> driverList;

    public DriverAdapter(List<Driver> driverList) {
        this.driverList = driverList;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView driverNameTextView;
        public TextView driverPlateNumberTextView;
        public TextView driverRouteTextView;
        public TextView driverCapacityTextView;

        public ViewHolder(View itemView) {
            super(itemView);
            driverNameTextView = itemView.findViewById(R.id.driverFullName);
            driverPlateNumberTextView = itemView.findViewById(R.id.driverPlateNumber);
            driverRouteTextView = itemView.findViewById(R.id.driverRoute);
            driverCapacityTextView = itemView.findViewById(R.id.driverCapacity);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.availablebusses, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Driver driver = driverList.get(position);

        holder.driverNameTextView.setText("Driver: " + driver.getFirstName() + " " + driver.getLastName());
        holder.driverPlateNumberTextView.setText("Plate Number: " + driver.getLicensePlate());
        holder.driverRouteTextView.setText("Route: " + driver.getRoute());
        holder.driverCapacityTextView.setText("Capacity: " + driver.getCapacity());
    }

    @Override
    public int getItemCount() {
        return driverList.size();
    }
}