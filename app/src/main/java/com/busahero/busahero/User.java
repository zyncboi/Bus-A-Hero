package com.busahero.busahero; // Update with your actual package name

public class User {
    private String userId;
    private String firstName;
    private String lastName;
    private String type;
    public User() {
    }
    public User(String firstName, String lastName, String type) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.type = type;
    }
    public String getUserId() {
        return userId;
    }
    public void setUserId(String userId) {
        this.userId = userId;
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
    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }
}

