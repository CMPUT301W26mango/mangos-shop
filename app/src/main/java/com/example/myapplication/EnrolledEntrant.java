package com.example.myapplication;

/**
 * US 02.06.05 — Data class representing an enrolled entrant.
 *
 * Used by CsvExportHelper to generate CSV output.
 * Fields match acceptance criteria #2: name, email, phone, enrolment date.
 *
 * deviceId and status are set after construction via setters so the existing
 * 4-param constructor stays compatible with CsvExportHelperTest.
 */
public class EnrolledEntrant {

    private String name;
    private String email;
    private String phone;           // Optional — may be null
    private String enrolmentDate;
    private String deviceId;        // Firestore document ID — required for lottery batch writes
    private String status;          // "waiting", "selected", "accepted", "reject

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

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
