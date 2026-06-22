package com.eatc.model;

import com.eatc.exception.ValidationException;

import java.time.LocalDate;

public final class Student {
    private final String studentId;
    private final String name;
    private final Gender gender;
    private final LocalDate dateOfBirth;
    private final String address;
    private final String emergencyContactNumber;

    public Student(String studentId, String name, Gender gender, LocalDate dateOfBirth,
                   String address, String emergencyContactNumber) {
        this.studentId = requireText(studentId, "Student ID");
        this.name = requireText(name, "Student name");
        if (gender == null) {
            throw new ValidationException("Gender must not be null");
        }
        if (dateOfBirth == null) {
            throw new ValidationException("Date of birth must not be null");
        }
        this.gender = gender;
        this.dateOfBirth = dateOfBirth;
        if (dateOfBirth.isAfter(LocalDate.now())) {
            throw new ValidationException("Date of birth must not be in the future");
        }
        this.address = requireText(address, "Address");
        this.emergencyContactNumber = requireText(emergencyContactNumber, "Emergency contact number");
    }

    private static String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new ValidationException(fieldName + " must not be blank");
        }
        return value.trim();
    }

    public String getStudentId() {
        return studentId;
    }

    public String getName() {
        return name;
    }

    public Gender getGender() {
        return gender;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public String getAddress() {
        return address;
    }

    public String getEmergencyContactNumber() {
        return emergencyContactNumber;
    }

    @Override
    public String toString() {
        return "Student{" +
                "studentId='" + studentId + '\'' +
                ", name='" + name + '\'' +
                ", gender=" + gender +
                ", dateOfBirth=" + dateOfBirth +
                ", address='" + address + '\'' +
                ", emergencyContactNumber='" + emergencyContactNumber + '\'' +
                '}';
    }
}
