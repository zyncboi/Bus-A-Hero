package com.busahero.busahero;

public class Driver {
    private String firstName;
    private String lastName;
    private String licensePlate;
    private String route;
    private boolean enroute;
    private double latitude;
    private double longitude;
    private String capacity;
    private String type;


    public Driver() {
        // Default constructor required for Firebase
    }

    public Driver(String firstName, String lastName, String licensePlate, String route, boolean enroute, double latitude, double longitude, String capacity, String type) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.licensePlate = licensePlate;
        this.route = route;
        this.enroute = enroute;
        this.latitude = latitude;
        this.longitude = longitude;
        this.capacity = capacity;
        this.type = type;

    }

    // Getter and Setter methods for the "enroute" field
    public boolean isEnroute() {
        return enroute;
    }
    public void setEnroute(boolean enroute) {
        this.enroute = enroute;
    }
    public String getFirstName() {
        return firstName;
    }
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    public String getLastName() {
        return lastName;
    }
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    public String getLicensePlate() {
        return licensePlate;
    }
    public void setLicensePlate(String licensePlate) {
        this.licensePlate = licensePlate;
    }
    public String getRoute() {
        return route;
    }
    public void setRoute(String route) {
        this.route = route;
    }
    public double getLatitude() {
        return latitude;
    }
    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }
    public double getLongitude() {
        return longitude;
    }
    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }
    public String getCapacity() {
        return capacity;
    }
    public void setCapacity(String capacity) {
        this.capacity = capacity;
    }
}

