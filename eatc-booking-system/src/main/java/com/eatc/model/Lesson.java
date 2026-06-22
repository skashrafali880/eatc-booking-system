package com.eatc.model;

import com.eatc.exception.BookingException;
import com.eatc.exception.ValidationException;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.OptionalDouble;

public final class Lesson {
    private static final int MAX_CAPACITY = 4;

    private final String lessonId;
    private final Subject subject;
    private final LocalDate date;
    private final DayOfWeek dayOfWeek;
    private final DaySession session;
    private final LocalTime startTime;
    private final LocalTime endTime;
    private final String tutorName;
    private final List<Booking> bookings;

    public Lesson(String lessonId, Subject subject, LocalDate date, DaySession session,
                  LocalTime startTime, LocalTime endTime, String tutorName) {
        this.lessonId = requireText(lessonId, "Lesson ID");
        if (subject == null) {
            throw new ValidationException("Subject must not be null");
        }
        if (date == null) {
            throw new ValidationException("Lesson date must not be null");
        }
        if (session == null) {
            throw new ValidationException("Day session must not be null");
        }
        if (startTime == null || endTime == null) {
            throw new ValidationException("Lesson start and end times must not be null");
        }
        if (!endTime.isAfter(startTime)) {
            throw new ValidationException("Lesson end time must be after its start time");
        }
        DayOfWeek lessonDay = date.getDayOfWeek();
        if (lessonDay != DayOfWeek.SATURDAY && lessonDay != DayOfWeek.SUNDAY) {
            throw new ValidationException("Lessons must run on Saturday or Sunday");
        }
        this.subject = subject;
        this.date = date;
        this.dayOfWeek = lessonDay;
        this.session = session;
        this.startTime = startTime;
        this.endTime = endTime;
        this.tutorName = requireText(tutorName, "Tutor name");
        this.bookings = new ArrayList<>();
    }

    private static String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new ValidationException(fieldName + " must not be blank");
        }
        return value.trim();
    }

    public String getLessonId() {
        return lessonId;
    }

    public Subject getSubject() {
        return subject;
    }

    public LocalDate getDate() {
        return date;
    }

    public DayOfWeek getDayOfWeek() {
        return dayOfWeek;
    }

    public DaySession getSession() {
        return session;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public String getTutorName() {
        return tutorName;
    }

    public static int getMaxCapacity() {
        return MAX_CAPACITY;
    }

    public List<Booking> getBookings() {
        return List.copyOf(bookings);
    }

    public boolean hasAvailableSpace() {
        return getCurrentBookedCount() < MAX_CAPACITY;
    }

    public boolean isFull() {
        return !hasAvailableSpace();
    }

    public void addBooking(Booking booking) {
        if (booking == null) {
            throw new ValidationException("Booking must not be null");
        }
        if (!booking.getLesson().getLessonId().equals(lessonId)) {
            throw new BookingException("Booking " + booking.getBookingId() + " belongs to a different lesson");
        }
        boolean duplicateId = bookings.stream()
                .anyMatch(existing -> existing.getBookingId().equals(booking.getBookingId()));
        if (duplicateId) {
            throw new BookingException("Booking " + booking.getBookingId() + " is already assigned to lesson " + lessonId);
        }
        if (booking.isActive() && isFull()) {
            throw new BookingException("Lesson " + lessonId + " is full");
        }
        bookings.add(booking);
    }

    public void removeBooking(Booking booking) {
        if (booking == null || !bookings.remove(booking)) {
            throw new BookingException("Booking is not assigned to lesson " + lessonId);
        }
    }

    public int getCurrentBookedCount() {
        return (int) bookings.stream()
                .filter(Booking::isActive)
                .count();
    }

    public int getAttendedCount() {
        return (int) bookings.stream()
                .filter(booking -> booking.getStatus() == BookingStatus.ATTENDED)
                .count();
    }

    public OptionalDouble getAverageRating() {
        return bookings.stream()
                .filter(booking -> booking.getStatus() == BookingStatus.ATTENDED)
                .map(Booking::getReview)
                .flatMap(java.util.Optional::stream)
                .mapToInt(Review::getRating)
                .average();
    }

    public List<Booking> getActiveBookings() {
        return bookings.stream()
                .filter(Booking::isActive)
                .toList();
    }

    @Override
    public String toString() {
        return "Lesson{" +
                "lessonId='" + lessonId + '\'' +
                ", subject=" + subject.getName() +
                ", date=" + date +
                ", dayOfWeek=" + dayOfWeek +
                ", session=" + session +
                ", time=" + startTime + "-" + endTime +
                ", tutorName='" + tutorName + '\'' +
                ", activeBookings=" + getCurrentBookedCount() + "/" + MAX_CAPACITY +
                '}';
    }
}
