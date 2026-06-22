package com.eatc.service;

import com.eatc.model.Booking;
import com.eatc.model.DaySession;
import com.eatc.model.Gender;
import com.eatc.model.Lesson;
import com.eatc.model.Student;
import com.eatc.model.Subject;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public final class DataSeeder {
    private DataSeeder() {
    }

    public static void seed(TuitionCentre tuitionCentre) {
        if (tuitionCentre == null) {
            throw new IllegalArgumentException("Tuition centre must not be null");
        }

        List<Subject> subjects = createSubjects(tuitionCentre);
        List<Student> students = createStudents(tuitionCentre);
        List<Lesson> lessons = createLessons(tuitionCentre, subjects);
        createBookings(tuitionCentre, students, lessons);
    }

    private static List<Subject> createSubjects(TuitionCentre tuitionCentre) {
        List<Subject> subjects = List.of(
                new Subject("SUB-ENG", "English", new BigDecimal("20.00")),
                new Subject("SUB-MAT", "Math", new BigDecimal("25.00")),
                new Subject("SUB-VR", "Verbal Reasoning", new BigDecimal("22.00")),
                new Subject("SUB-NVR", "Non-verbal Reasoning", new BigDecimal("22.00"))
        );
        subjects.forEach(tuitionCentre::addSubject);
        return subjects;
    }

    private static List<Student> createStudents(TuitionCentre tuitionCentre) {
        List<Student> students = List.of(
                new Student("S001", "Aarav Patel", Gender.MALE, LocalDate.of(2013, 2, 14),
                        "12 Oak Road, London", "07100000001"),
                new Student("S002", "Amelia Brown", Gender.FEMALE, LocalDate.of(2012, 6, 3),
                        "8 Maple Close, London", "07100000002"),
                new Student("S003", "Noah Williams", Gender.MALE, LocalDate.of(2013, 9, 21),
                        "31 King Street, London", "07100000003"),
                new Student("S004", "Isla Taylor", Gender.FEMALE, LocalDate.of(2012, 11, 8),
                        "20 Queen Avenue, London", "07100000004"),
                new Student("S005", "Leo Johnson", Gender.MALE, LocalDate.of(2014, 1, 17),
                        "4 Cedar Lane, London", "07100000005"),
                new Student("S006", "Mia Wilson", Gender.FEMALE, LocalDate.of(2013, 4, 25),
                        "17 Elm Drive, London", "07100000006"),
                new Student("S007", "Ethan Thomas", Gender.MALE, LocalDate.of(2012, 8, 12),
                        "45 Park View, London", "07100000007"),
                new Student("S008", "Sophia Martin", Gender.FEMALE, LocalDate.of(2014, 3, 30),
                        "6 Birch Way, London", "07100000008"),
                new Student("S009", "Alex Morgan", Gender.OTHER, LocalDate.of(2013, 7, 6),
                        "28 River Road, London", "07100000009"),
                new Student("S010", "Grace Lee", Gender.FEMALE, LocalDate.of(2012, 12, 19),
                        "9 Garden Court, London", "07100000010")
        );
        students.forEach(tuitionCentre::addStudent);
        return students;
    }

    private static List<Lesson> createLessons(TuitionCentre tuitionCentre, List<Subject> subjects) {
        List<Lesson> lessons = new ArrayList<>();
        LocalDate firstSaturday = LocalDate.of(2026, 1, 3);
        int lessonNumber = 1;

        for (int weekend = 0; weekend < 8; weekend++) {
            LocalDate saturday = firstSaturday.plusWeeks(weekend);
            LocalDate sunday = saturday.plusDays(1);
            LocalDate[] dates = {saturday, saturday, sunday, sunday};
            DaySession[] sessions = {
                    DaySession.MORNING, DaySession.AFTERNOON,
                    DaySession.MORNING, DaySession.AFTERNOON
            };

            for (int slot = 0; slot < 4; slot++) {
                Subject subject = subjects.get((weekend + slot) % subjects.size());
                DaySession session = sessions[slot];
                LocalTime startTime = session == DaySession.MORNING
                        ? LocalTime.of(9, 0) : LocalTime.of(13, 0);
                LocalTime endTime = session == DaySession.MORNING
                        ? LocalTime.of(11, 0) : LocalTime.of(15, 0);
                Lesson lesson = new Lesson(
                        String.format("L%03d", lessonNumber++),
                        subject,
                        dates[slot],
                        session,
                        startTime,
                        endTime,
                        tutorFor(subject)
                );
                tuitionCentre.addLesson(lesson);
                lessons.add(lesson);
            }
        }
        return List.copyOf(lessons);
    }

    private static String tutorFor(Subject subject) {
        return switch (subject.getSubjectId()) {
            case "SUB-ENG" -> "Ms Green";
            case "SUB-MAT" -> "Mr Khan";
            case "SUB-VR" -> "Dr Evans";
            case "SUB-NVR" -> "Ms Lewis";
            default -> "EATC Tutor";
        };
    }

    private static void createBookings(TuitionCentre tuitionCentre, List<Student> students,
                                       List<Lesson> lessons) {
        BookingService bookingService = new BookingService(tuitionCentre);
        for (int index = 0; index < lessons.size(); index++) {
            Lesson lesson = lessons.get(index);
            Student firstStudent = students.get(index % students.size());
            Student secondStudent = students.get((index + 3) % students.size());

            Booking firstBooking = bookingService.bookLesson(
                    firstStudent.getStudentId(), lesson.getLessonId());
            bookingService.attendLesson(firstBooking.getBookingId());
            bookingService.addReview(firstBooking.getBookingId(), 3 + (index % 3),
                    "Useful lesson with clear teaching.");

            Booking secondBooking = bookingService.bookLesson(
                    secondStudent.getStudentId(), lesson.getLessonId());
            if (index % 5 == 0) {
                bookingService.cancelBooking(secondBooking.getBookingId());
            } else if (index % 2 == 0) {
                bookingService.attendLesson(secondBooking.getBookingId());
                bookingService.addReview(secondBooking.getBookingId(), 4 + (index % 2),
                        "Good pace and helpful practice.");
            }
        }
    }
}
