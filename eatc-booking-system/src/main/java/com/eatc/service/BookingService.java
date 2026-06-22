package com.eatc.service;

import com.eatc.exception.BookingException;
import com.eatc.model.Booking;
import com.eatc.model.BookingStatus;
import com.eatc.model.Lesson;
import com.eatc.model.Review;
import com.eatc.model.Student;
import com.eatc.model.Subject;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public final class BookingService {
    private final TuitionCentre tuitionCentre;
    private int nextBookingNumber;

    public BookingService(TuitionCentre tuitionCentre) {
        if (tuitionCentre == null) {
            throw new IllegalArgumentException("Tuition centre must not be null");
        }
        this.tuitionCentre = tuitionCentre;
        this.nextBookingNumber = 1;
    }

    public synchronized Booking bookLesson(String studentId, String lessonId) {
        Student student = requireStudent(studentId);
        Lesson lesson = requireLesson(lessonId);

        if (lesson.isFull()) {
            throw new BookingException("Lesson " + lesson.getLessonId() + " is full");
        }
        if (hasActiveBookingForLesson(student, lesson, null)) {
            throw new BookingException("Student " + student.getStudentId()
                    + " already has an active booking for lesson " + lesson.getLessonId());
        }
        if (hasTimeClash(student, lesson, null)) {
            throw new BookingException("Student " + student.getStudentId()
                    + " already has an active booking at this time");
        }

        Booking booking = new Booking(generateBookingId(), student, lesson);
        lesson.addBooking(booking);
        try {
            tuitionCentre.addBooking(booking);
        } catch (RuntimeException exception) {
            lesson.removeBooking(booking);
            throw exception;
        }
        return booking;
    }

    public synchronized Booking changeBooking(String bookingId, String newLessonId) {
        Booking booking = requireBooking(bookingId);
        if (booking.getStatus() != BookingStatus.BOOKED) {
            throw new BookingException("Only a BOOKED booking can be changed");
        }

        Lesson oldLesson = booking.getLesson();
        Lesson newLesson = requireLesson(newLessonId);
        if (oldLesson.getLessonId().equals(newLesson.getLessonId())) {
            throw new BookingException("Booking is already assigned to lesson " + newLesson.getLessonId());
        }
        if (newLesson.isFull()) {
            throw new BookingException("Lesson " + newLesson.getLessonId() + " is full");
        }
        if (!oldLesson.getSubject().getSubjectId().equals(newLesson.getSubject().getSubjectId())) {
            throw new BookingException("A booking can only be changed to a lesson of the same subject");
        }
        if (hasActiveBookingForLesson(booking.getStudent(), newLesson, booking)) {
            throw new BookingException("Student already has an active booking for lesson " + newLesson.getLessonId());
        }
        if (hasTimeClash(booking.getStudent(), newLesson, booking)) {
            throw new BookingException("Student already has an active booking at the new lesson time");
        }

        oldLesson.removeBooking(booking);
        booking.changeLesson(newLesson);
        try {
            newLesson.addBooking(booking);
        } catch (RuntimeException exception) {
            booking.changeLesson(oldLesson);
            oldLesson.addBooking(booking);
            throw new BookingException("Unable to change booking: " + exception.getMessage(), exception);
        }
        return booking;
    }

    public synchronized Booking cancelBooking(String bookingId) {
        Booking booking = requireBooking(bookingId);
        booking.cancel();
        return booking;
    }

    public synchronized Booking attendLesson(String bookingId) {
        Booking booking = requireBooking(bookingId);
        booking.attend();
        return booking;
    }

    public synchronized Review addReview(String bookingId, int rating, String comment) {
        Booking booking = requireBooking(bookingId);
        if (booking.getStatus() != BookingStatus.ATTENDED) {
            throw new BookingException("A review can only be added to an ATTENDED booking");
        }
        Review review = new Review(rating, comment);
        booking.addReview(review);
        return review;
    }

    public List<Student> getAllStudents() {
        return tuitionCentre.getAllStudents();
    }

    public List<Subject> getAllSubjects() {
        return tuitionCentre.getAllSubjects();
    }

    public List<Lesson> getAllLessons() {
        return tuitionCentre.getAllLessons();
    }

    public List<Booking> getAllBookings() {
        return tuitionCentre.getAllBookings();
    }

    public Optional<Student> findStudentById(String id) {
        return tuitionCentre.findStudentById(id);
    }

    public Optional<Lesson> findLessonById(String id) {
        return tuitionCentre.findLessonById(id);
    }

    public Optional<Booking> findBookingById(String id) {
        return tuitionCentre.findBookingById(id);
    }

    private Student requireStudent(String studentId) {
        return tuitionCentre.findStudentById(studentId)
                .orElseThrow(() -> new BookingException("Student not found: " + studentId));
    }

    private Lesson requireLesson(String lessonId) {
        return tuitionCentre.findLessonById(lessonId)
                .orElseThrow(() -> new BookingException("Lesson not found: " + lessonId));
    }

    private Booking requireBooking(String bookingId) {
        return tuitionCentre.findBookingById(bookingId)
                .orElseThrow(() -> new BookingException("Booking not found: " + bookingId));
    }

    private boolean hasActiveBookingForLesson(Student student, Lesson lesson, Booking excludedBooking) {
        return tuitionCentre.getAllBookings().stream()
                .filter(Booking::isActive)
                .filter(existing -> existing != excludedBooking)
                .anyMatch(existing -> existing.getStudent().getStudentId().equals(student.getStudentId())
                        && existing.getLesson().getLessonId().equals(lesson.getLessonId()));
    }

    private boolean hasTimeClash(Student student, Lesson lesson, Booking excludedBooking) {
        return tuitionCentre.getAllBookings().stream()
                .filter(Booking::isActive)
                .filter(existing -> existing != excludedBooking)
                .filter(existing -> existing.getStudent().getStudentId().equals(student.getStudentId()))
                .map(Booking::getLesson)
                .anyMatch(existingLesson -> existingLesson.getDate().equals(lesson.getDate())
                        && timesOverlap(existingLesson.getStartTime(), existingLesson.getEndTime(),
                        lesson.getStartTime(), lesson.getEndTime()));
    }

    private boolean timesOverlap(LocalTime firstStart, LocalTime firstEnd,
                                 LocalTime secondStart, LocalTime secondEnd) {
        return firstStart.isBefore(secondEnd) && secondStart.isBefore(firstEnd);
    }

    private String generateBookingId() {
        String bookingId;
        do {
            bookingId = "B" + String.format("%04d", nextBookingNumber++);
        } while (tuitionCentre.findBookingById(bookingId).isPresent());
        return bookingId;
    }
}
