package com.example.rajk.leasingmanagers.model;

/**
 * Created by SoumyaAgarwal on 8/6/2017.
 */

public class Phonebook {

    private String contact, name, designation, email;

    public Phonebook(String contact, String name, String designation, String email) {
        this.contact = contact;
        this.name = name;
        this.designation = designation;
        this.email = email;
    }

    public Phonebook() {
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDesignation() {
        return designation;
    }

    public void setDesignation(String designation) {
        this.designation = designation;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
