package org.example.billing.model;

public class Customer {

    private final int id;
    private final String email;
    private final String fullName;

    public Customer(int id, String email, String fullName) {
        this.id = id;
        this.email = email;
        this.fullName = fullName;
    }

    public int getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getFullName() {
        return fullName;
    }
}