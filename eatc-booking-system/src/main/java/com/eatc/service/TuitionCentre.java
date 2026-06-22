package com.eatc.service;

import com.eatc.exception.ValidationException;
import com.eatc.model.Booking;
import com.eatc.model.Lesson;
import com.eatc.model.Student;
import com.eatc.model.Subject;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class TuitionCentre {
    private final Map<String, Student> students;
    private final Map<String, Subject> subjects;
    private final Map<String, Lesson> lessons;
    private final Map<String, Booking> bookings;

    public TuitionCentre() {
        this.students = new HashMap<>();
        this.subjects = new HashMap<>();
        this.lessons = new HashMap<>();
        this.bookings = new HashMap<>();
    }

    public void addStudent(Student student) {
        if (student == null) {
            throw new ValidationException("Student must not be null");
        }
        if (students.putIfAbsent(student.getStudentId(), student) != null) {
            throw new ValidationException("Student ID already exists: " + student.getStudentId());
        }
    }

    public void addSubject(Subject subject) {
        if (subject == null) {
            throw new ValidationException("Subject must not be null");
        }
        if (subjects.putIfAbsent(subject.getSubjectId(), subject) != null) {
            throw new ValidationException("Subject ID already exists: " + subject.getSubjectId());
        }
    }

    public void addLesson(Lesson lesson) {
        if (lesson == null) {
            throw new ValidationException("Lesson must not be null");
        }
        Subject registeredSubject = subjects.get(lesson.getSubject().getSubjectId());
        if (registeredSubject == null) {
            throw new ValidationException("Lesson subject is not registered: " + lesson.getSubject().getSubjectId());
        }
        if (registeredSubject != lesson.getSubject()) {
            throw new ValidationException("Lesson must use the registered subject instance so its price remains fixed");
        }
        if (lessons.putIfAbsent(lesson.getLessonId(), lesson) != null) {
            throw new ValidationException("Lesson ID already exists: " + lesson.getLessonId());
        }
    }

    public void addBooking(Booking booking) {
        if (booking == null) {
            throw new ValidationException("Booking must not be null");
        }
        if (!students.containsKey(booking.getStudent().getStudentId())) {
            throw new ValidationException("Booking student is not registered: " + booking.getStudent().getStudentId());
        }
        if (!lessons.containsKey(booking.getLesson().getLessonId())) {
            throw new ValidationException("Booking lesson is not registered: " + booking.getLesson().getLessonId());
        }
        if (bookings.putIfAbsent(booking.getBookingId(), booking) != null) {
            throw new ValidationException("Booking ID already exists: " + booking.getBookingId());
        }
    }

    public Optional<Student> findStudentById(String id) {
        return id == null ? Optional.empty() : Optional.ofNullable(students.get(id.trim()));
    }

    public Optional<Subject> findSubjectById(String id) {
        return id == null ? Optional.empty() : Optional.ofNullable(subjects.get(id.trim()));
    }

    public Optional<Lesson> findLessonById(String id) {
        return id == null ? Optional.empty() : Optional.ofNullable(lessons.get(id.trim()));
    }

    public Optional<Booking> findBookingById(String id) {
        return id == null ? Optional.empty() : Optional.ofNullable(bookings.get(id.trim()));
    }

    public List<Student> getAllStudents() {
        List<Student> result = new ArrayList<>(students.values());
        result.sort(Comparator.comparing(Student::getStudentId));
        return List.copyOf(result);
    }

    public List<Subject> getAllSubjects() {
        List<Subject> result = new ArrayList<>(subjects.values());
        result.sort(Comparator.comparing(Subject::getSubjectId));
        return List.copyOf(result);
    }

    public List<Lesson> getAllLessons() {
        List<Lesson> result = new ArrayList<>(lessons.values());
        result.sort(Comparator.comparing(Lesson::getDate)
                .thenComparing(Lesson::getStartTime)
                .thenComparing(Lesson::getLessonId));
        return List.copyOf(result);
    }

    public List<Booking> getAllBookings() {
        List<Booking> result = new ArrayList<>(bookings.values());
        result.sort(Comparator.comparing(Booking::getBookingId));
        return List.copyOf(result);
    }
}
