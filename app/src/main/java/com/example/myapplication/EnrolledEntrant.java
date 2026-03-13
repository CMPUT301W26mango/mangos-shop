package com.example.myapplication;

/**
 * US 02.06.05 — Data class representing an enrolled entrant.
 *
 * Used by CsvExportHelper to generate CSV output.
 * Fields match acceptance criteria #2: name, email, phone, enrolment date.
 *
 */
public class EnrolledEntrant {

    private String name;
    private String email;
    private String phone;       // Optional — may be null
    private String enrolmentDate;

    public EnrolledEntrant(String name, String email, String phone, String enrolmentDate) {
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.enrolmentDate = enrolmentDate;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public String getEnrolmentDate() {
        return enrolmentDate;
    }
}
