package com.eatc.service;

import com.eatc.model.Booking;
import com.eatc.model.DaySession;
import com.eatc.model.Gender;
import com.eatc.model.Lesson;
import com.eatc.model.Student;
import com.eatc.model.Subject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ReportServiceTest {
    private BookingService bookingService;
    private ReportService reportService;

    @BeforeEach
    void setUp() {
        TuitionCentre tuitionCentre = new TuitionCentre();
        Subject math = new Subject("SUB-MAT", "Math", new BigDecimal("25.00"));
        Subject english = new Subject("SUB-ENG", "English", new BigDecimal("20.00"));
        tuitionCentre.addSubject(math);
        tuitionCentre.addSubject(english);

        for (int index = 1; index <= 4; index++) {
            tuitionCentre.addStudent(new Student(
                    "S" + index,
                    "Student " + index,
                    index % 2 == 0 ? Gender.FEMALE : Gender.MALE,
                    LocalDate.of(2012, index, index),
                    index + " Test Street",
                    "0700000000" + index
            ));
        }

        tuitionCentre.addLesson(new Lesson("L-MAT", math, LocalDate.of(2026, 7, 4),
                DaySession.MORNING, LocalTime.of(9, 0), LocalTime.of(11, 0), "Mr Khan"));
        tuitionCentre.addLesson(new Lesson("L-ENG", english, LocalDate.of(2026, 7, 5),
                DaySession.MORNING, LocalTime.of(9, 0), LocalTime.of(11, 0), "Ms Green"));

        bookingService = new BookingService(tuitionCentre);
        reportService = new ReportService(tuitionCentre);
    }

    @Test
    void averageRatingIsCalculatedCorrectly() {
        Booking first = attend("S1", "L-MAT");
        Booking second = attend("S2", "L-MAT");
        bookingService.addReview(first.getBookingId(), 3, "Satisfactory");
        bookingService.addReview(second.getBookingId(), 5, "Excellent");

        String report = reportService.generateLessonAttendanceAndRatingReport();

        assertTrue(report.contains("Lesson ID: L-MAT"));
        assertTrue(report.contains("Average Rating: 4.00"));
    }

    @Test
    void averageRatingHandlesNoReviews() {
        attend("S1", "L-ENG");

        String report = reportService.generateLessonAttendanceAndRatingReport();

        assertTrue(report.contains("Lesson ID: L-ENG"));
        assertTrue(report.contains("Average Rating: No ratings yet"));
    }

    @Test
    void attendedCountIsCalculatedCorrectly() {
        attend("S1", "L-MAT");
        bookingService.bookLesson("S2", "L-MAT");

        String report = reportService.generateLessonAttendanceAndRatingReport();

        assertTrue(report.contains("Lesson ID: L-MAT"));
        assertTrue(report.contains("Booked Count: 2 | Attended Count: 1"));
    }

    @Test
    void highestIncomeSubjectIsCalculatedCorrectly() {
        attend("S1", "L-MAT");
        attend("S2", "L-MAT");
        attend("S3", "L-ENG");
        attend("S4", "L-ENG");

        String report = reportService.generateHighestIncomeSubjectReport();

        assertTrue(report.contains("Subject: Math | Price: 25.00 | Attended Booking Count: 2 | Total Income: 50.00"));
        assertTrue(report.contains("Subject: English | Price: 20.00 | Attended Booking Count: 2 | Total Income: 40.00"));
        assertTrue(report.contains("Highest Income Subject: Math | Total Income: 50.00"));
    }

    @Test
    void cancelledBookingsAreNotCountedAsIncome() {
        Booking booking = bookingService.bookLesson("S1", "L-MAT");
        bookingService.cancelBooking(booking.getBookingId());

        String report = reportService.generateHighestIncomeSubjectReport();

        assertTrue(report.contains("Subject: Math | Price: 25.00 | Attended Booking Count: 0 | Total Income: 0.00"));
    }

    @Test
    void bookedButNotAttendedBookingsAreNotCountedAsIncome() {
        bookingService.bookLesson("S1", "L-MAT");

        String report = reportService.generateHighestIncomeSubjectReport();

        assertTrue(report.contains("Subject: Math | Price: 25.00 | Attended Booking Count: 0 | Total Income: 0.00"));
    }

    private Booking attend(String studentId, String lessonId) {
        Booking booking = bookingService.bookLesson(studentId, lessonId);
        return bookingService.attendLesson(booking.getBookingId());
    }
}
