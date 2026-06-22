package com.eatc.model;

import com.eatc.exception.BookingException;
import com.eatc.exception.ValidationException;

import java.time.LocalDateTime;
import java.util.Optional;

public final class Booking {
    private final String bookingId;
    private final Student student;
    private Lesson lesson;
    private BookingStatus status;
    private Review review;
    private final LocalDateTime createdAt;

    public Booking(String bookingId, Student student, Lesson lesson) {
        if (bookingId == null || bookingId.isBlank()) {
            throw new ValidationException("Booking ID must not be blank");
        }
        if (student == null) {
            throw new ValidationException("Student must not be null");
        }
        if (lesson == null) {
            throw new ValidationException("Lesson must not be null");
        }
        this.bookingId = bookingId.trim();
        this.student = student;
        this.lesson = lesson;
        this.status = BookingStatus.BOOKED;
        this.createdAt = LocalDateTime.now();
    }

    public String getBookingId() {
        return bookingId;
    }

    public Student getStudent() {
        return student;
    }

    public Lesson getLesson() {
        return lesson;
    }

    public BookingStatus getStatus() {
        return status;
    }

    public Optional<Review> getReview() {
        return Optional.ofNullable(review);
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public boolean isActive() {
        return status == BookingStatus.BOOKED || status == BookingStatus.ATTENDED;
    }
    
    public void changeLesson(Lesson lesson) {
        if (status != BookingStatus.BOOKED) {
            throw new BookingException("Only a BOOKED booking can be changed");
        }
        if (lesson == null) {
            throw new ValidationException("New lesson must not be null");
        }
        this.lesson = lesson;
    }

    public void cancel() {
        if (status == BookingStatus.CANCELLED) {
            throw new BookingException("Booking " + bookingId + " is already cancelled");
        }
        status = BookingStatus.CANCELLED;
    }

    public void attend() {
        if (status != BookingStatus.BOOKED) {
            throw new BookingException("Only a BOOKED booking can be marked as attended");
        }
        status = BookingStatus.ATTENDED;
    }

    public void addReview(Review review) {
        if (status != BookingStatus.ATTENDED) {
            throw new BookingException("A review can only be added after attendance");
        }
        if (review == null) {
            throw new ValidationException("Review must not be null");
        }
        if (this.review != null) {
            throw new BookingException("Booking " + bookingId + " already has a review");
        }
        this.review = review;
    }

    @Override
    public String toString() {
        return "Booking{" +
                "bookingId='" + bookingId + '\'' +
                ", studentId='" + student.getStudentId() + '\'' +
                ", studentName='" + student.getName() + '\'' +
                ", lessonId='" + lesson.getLessonId() + '\'' +
                ", subject='" + lesson.getSubject().getName() + '\'' +
                ", date=" + lesson.getDate() +
                ", session=" + lesson.getSession() +
                ", status=" + status +
                ", review=" + (review == null ? "None" : review) +
                ", createdAt=" + createdAt +
                '}';
    }
}
