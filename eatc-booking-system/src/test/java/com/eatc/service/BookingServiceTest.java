package com.eatc.service;

import com.eatc.exception.BookingException;
import com.eatc.exception.ValidationException;
import com.eatc.model.Booking;
import com.eatc.model.BookingStatus;
import com.eatc.model.DaySession;
import com.eatc.model.Gender;
import com.eatc.model.Lesson;
import com.eatc.model.Review;
import com.eatc.model.Student;
import com.eatc.model.Subject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BookingServiceTest {
    private TuitionCentre tuitionCentre;
    private BookingService bookingService;
    private Lesson mathLessonOne;
    private Lesson mathLessonTwo;
    private Lesson mathFullLesson;
    private Lesson englishLesson;
    private Lesson clashingLesson;

    @BeforeEach
    void setUp() {
        tuitionCentre = new TuitionCentre();
        Subject math = new Subject("SUB-MAT", "Math", new BigDecimal("25.00"));
        Subject english = new Subject("SUB-ENG", "English", new BigDecimal("20.00"));
        tuitionCentre.addSubject(math);
        tuitionCentre.addSubject(english);

        for (int index = 1; index <= 6; index++) {
            tuitionCentre.addStudent(new Student(
                    "S" + index,
                    "Student " + index,
                    index % 2 == 0 ? Gender.FEMALE : Gender.MALE,
                    LocalDate.of(2012, index, index),
                    index + " Test Street",
                    "0700000000" + index
            ));
        }

        mathLessonOne = new Lesson("L-M1", math, LocalDate.of(2026, 7, 4),
                DaySession.MORNING, LocalTime.of(9, 0), LocalTime.of(11, 0), "Mr Khan");
        mathLessonTwo = new Lesson("L-M2", math, LocalDate.of(2026, 7, 11),
                DaySession.MORNING, LocalTime.of(9, 0), LocalTime.of(11, 0), "Mr Khan");
        mathFullLesson = new Lesson("L-M3", math, LocalDate.of(2026, 7, 18),
                DaySession.MORNING, LocalTime.of(9, 0), LocalTime.of(11, 0), "Mr Khan");
        englishLesson = new Lesson("L-E1", english, LocalDate.of(2026, 7, 11),
                DaySession.AFTERNOON, LocalTime.of(13, 0), LocalTime.of(15, 0), "Ms Green");
        clashingLesson = new Lesson("L-E2", english, LocalDate.of(2026, 7, 4),
                DaySession.MORNING, LocalTime.of(9, 0), LocalTime.of(11, 0), "Ms Green");

        tuitionCentre.addLesson(mathLessonOne);
        tuitionCentre.addLesson(mathLessonTwo);
        tuitionCentre.addLesson(mathFullLesson);
        tuitionCentre.addLesson(englishLesson);
        tuitionCentre.addLesson(clashingLesson);
        bookingService = new BookingService(tuitionCentre);
    }

    @Test
    void bookingSucceedsWhenLessonHasSpace() {
        Booking booking = bookingService.bookLesson("S1", "L-M1");

        assertEquals(BookingStatus.BOOKED, booking.getStatus());
        assertSame(mathLessonOne, booking.getLesson());
        assertEquals(1, mathLessonOne.getCurrentBookedCount());
        assertTrue(bookingService.findBookingById(booking.getBookingId()).isPresent());
    }

    @Test
    void bookingFailsWhenLessonIsFull() {
        fillLesson("L-M1");

        assertThrows(BookingException.class, () -> bookingService.bookLesson("S5", "L-M1"));
        assertEquals(4, mathLessonOne.getCurrentBookedCount());
    }

    @Test
    void bookingFailsWhenStudentHasTimeClash() {
        bookingService.bookLesson("S1", "L-M1");

        assertThrows(BookingException.class, () -> bookingService.bookLesson("S1", "L-E2"));
    }

    @Test
    void bookingFailsWhenStudentBooksSameLessonTwice() {
        bookingService.bookLesson("S1", "L-M1");

        assertThrows(BookingException.class, () -> bookingService.bookLesson("S1", "L-M1"));
    }

    @Test
    void changingBookingSucceedsForSameSubjectWithSpace() {
        Booking booking = bookingService.bookLesson("S1", "L-M1");

        Booking changed = bookingService.changeBooking(booking.getBookingId(), "L-M2");

        assertSame(booking, changed);
        assertSame(mathLessonTwo, changed.getLesson());
        assertEquals(0, mathLessonOne.getCurrentBookedCount());
        assertEquals(1, mathLessonTwo.getCurrentBookedCount());
    }

    @Test
    void changingBookingFailsForDifferentSubject() {
        Booking booking = bookingService.bookLesson("S1", "L-M1");

        assertThrows(BookingException.class,
                () -> bookingService.changeBooking(booking.getBookingId(), "L-E1"));
        assertSame(mathLessonOne, booking.getLesson());
    }

    @Test
    void changingBookingFailsWhenNewLessonIsFull() {
        Booking booking = bookingService.bookLesson("S1", "L-M1");
        bookingService.bookLesson("S2", "L-M3");
        bookingService.bookLesson("S3", "L-M3");
        bookingService.bookLesson("S4", "L-M3");
        bookingService.bookLesson("S5", "L-M3");

        assertThrows(BookingException.class,
                () -> bookingService.changeBooking(booking.getBookingId(), "L-M3"));
        assertSame(mathLessonOne, booking.getLesson());
    }

    @Test
    void cancellationChangesStatusToCancelled() {
        Booking booking = bookingService.bookLesson("S1", "L-M1");

        Booking cancelled = bookingService.cancelBooking(booking.getBookingId());

        assertEquals(BookingStatus.CANCELLED, cancelled.getStatus());
    }

    @Test
    void cancelledBookingNoLongerCountsTowardCapacity() {
        Booking booking = bookingService.bookLesson("S1", "L-M1");
        assertEquals(1, mathLessonOne.getCurrentBookedCount());

        bookingService.cancelBooking(booking.getBookingId());

        assertEquals(0, mathLessonOne.getCurrentBookedCount());
        assertTrue(mathLessonOne.hasAvailableSpace());
        assertFalse(mathLessonOne.getActiveBookings().contains(booking));
    }

    @Test
    void attendanceChangesStatusFromBookedToAttended() {
        Booking booking = bookingService.bookLesson("S1", "L-M1");

        Booking attended = bookingService.attendLesson(booking.getBookingId());

        assertEquals(BookingStatus.ATTENDED, attended.getStatus());
        assertEquals(1, mathLessonOne.getAttendedCount());
    }

    @Test
    void reviewSucceedsOnlyAfterAttendance() {
        Booking booking = bookingService.bookLesson("S1", "L-M1");
        bookingService.attendLesson(booking.getBookingId());

        Review review = bookingService.addReview(booking.getBookingId(), 5, "Excellent lesson");

        assertEquals(5, review.getRating());
        assertEquals("Excellent lesson", review.getComment());
        assertSame(review, booking.getReview().orElseThrow());
    }

    @Test
    void reviewFailsForBookedBooking() {
        Booking booking = bookingService.bookLesson("S1", "L-M1");

        assertThrows(BookingException.class,
                () -> bookingService.addReview(booking.getBookingId(), 4, "Good lesson"));
    }

    @Test
    void reviewFailsForCancelledBooking() {
        Booking booking = bookingService.bookLesson("S1", "L-M1");
        bookingService.cancelBooking(booking.getBookingId());

        assertThrows(BookingException.class,
                () -> bookingService.addReview(booking.getBookingId(), 4, "Good lesson"));
    }

    @Test
    void ratingBelowOneFails() {
        Booking booking = bookingService.bookLesson("S1", "L-M1");
        bookingService.attendLesson(booking.getBookingId());

        assertThrows(ValidationException.class,
                () -> bookingService.addReview(booking.getBookingId(), 0, "Invalid rating"));
    }

    @Test
    void ratingAboveFiveFails() {
        Booking booking = bookingService.bookLesson("S1", "L-M1");
        bookingService.attendLesson(booking.getBookingId());

        assertThrows(ValidationException.class,
                () -> bookingService.addReview(booking.getBookingId(), 6, "Invalid rating"));
    }

    private void fillLesson(String lessonId) {
        bookingService.bookLesson("S1", lessonId);
        bookingService.bookLesson("S2", lessonId);
        bookingService.bookLesson("S3", lessonId);
        bookingService.bookLesson("S4", lessonId);
    }
}
