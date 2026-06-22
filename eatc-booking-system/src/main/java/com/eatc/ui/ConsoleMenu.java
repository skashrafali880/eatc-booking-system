package com.eatc.ui;

import com.eatc.exception.BookingException;
import com.eatc.exception.ValidationException;
import com.eatc.model.Booking;
import com.eatc.model.Lesson;
import com.eatc.model.Student;
import com.eatc.model.Subject;
import com.eatc.service.BookingService;
import com.eatc.service.ReportService;

import java.util.List;
import java.util.Scanner;

public final class ConsoleMenu {
    private final BookingService bookingService;
    private final ReportService reportService;
    private final Scanner scanner;

    public ConsoleMenu(BookingService bookingService, ReportService reportService) {
        if (bookingService == null || reportService == null) {
            throw new IllegalArgumentException("Services must not be null");
        }
        this.bookingService = bookingService;
        this.reportService = reportService;
        this.scanner = new Scanner(System.in);
    }

    public void start() {
        boolean running = true;
        System.out.println("Excel Academy Tuition Centre - EATC Booking System");

        while (running && scanner.hasNextLine()) {
            printMenu();
            int choice = readMenuChoice();
            try {
                switch (choice) {
                    case 1 -> printStudents();
                    case 2 -> printSubjects();
                    case 3 -> printLessons();
                    case 4 -> bookLesson();
                    case 5 -> changeBooking();
                    case 6 -> cancelBooking();
                    case 7 -> attendLesson();
                    case 8 -> addReview();
                    case 9 -> printBookings();
                    case 10 -> System.out.println(reportService.generateLessonAttendanceAndRatingReport());
                    case 11 -> System.out.println(reportService.generateHighestIncomeSubjectReport());
                    case 12 -> running = false;
                    default -> System.out.println("Please choose a number from 1 to 12.");
                }
            } catch (BookingException | ValidationException exception) {
                System.out.println("Error: " + exception.getMessage());
            } catch (RuntimeException exception) {
                System.out.println("Unable to complete the operation: " + exception.getMessage());
            }
        }
        System.out.println("EATC Booking System closed.");
    }

    private void printMenu() {
        System.out.println();
        System.out.println("1. View all students");
        System.out.println("2. View all subjects");
        System.out.println("3. View all lessons");
        System.out.println("4. Book lesson");
        System.out.println("5. Change booking");
        System.out.println("6. Cancel booking");
        System.out.println("7. Attend lesson");
        System.out.println("8. Add review");
        System.out.println("9. View all bookings");
        System.out.println("10. Print lesson attendance and rating report");
        System.out.println("11. Print highest income subject report");
        System.out.println("12. Exit");
    }

    private int readMenuChoice() {
        while (true) {
            System.out.print("Select an option: ");
            String input = scanner.nextLine().trim();
            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException exception) {
                System.out.println("Invalid input. Enter a whole number from 1 to 12.");
                if (!scanner.hasNextLine()) {
                    return 12;
                }
            }
        }
    }

    private void printStudents() {
        List<Student> students = bookingService.getAllStudents();
        System.out.println("Students:");
        students.forEach(student -> System.out.println("  " + student));
    }

    private void printSubjects() {
        List<Subject> subjects = bookingService.getAllSubjects();
        System.out.println("Subjects:");
        subjects.forEach(subject -> System.out.println("  " + subject));
    }

    private void printLessons() {
        List<Lesson> lessons = bookingService.getAllLessons();
        System.out.println("Lessons:");
        lessons.forEach(lesson -> System.out.println("  " + lesson));
    }

    private void printBookings() {
        List<Booking> bookings = bookingService.getAllBookings();
        System.out.println("Bookings:");
        bookings.forEach(booking -> System.out.println("  " + booking));
    }

    private void bookLesson() {
        printStudents();
        String studentId = readRequiredText("Student ID: ");
        printLessons();
        String lessonId = readRequiredText("Lesson ID: ");
        Booking booking = bookingService.bookLesson(studentId, lessonId);
        System.out.println("Booking created: " + booking);
    }

    private void changeBooking() {
        printBookings();
        String bookingId = readRequiredText("Booking ID: ");
        printLessons();
        String newLessonId = readRequiredText("New lesson ID: ");
        Booking booking = bookingService.changeBooking(bookingId, newLessonId);
        System.out.println("Booking changed: " + booking);
    }

    private void cancelBooking() {
        printBookings();
        String bookingId = readRequiredText("Booking ID: ");
        Booking booking = bookingService.cancelBooking(bookingId);
        System.out.println("Booking cancelled: " + booking);
    }

    private void attendLesson() {
        printBookings();
        String bookingId = readRequiredText("Booking ID: ");
        Booking booking = bookingService.attendLesson(bookingId);
        System.out.println("Attendance recorded: " + booking);
    }

    private void addReview() {
        printBookings();
        String bookingId = readRequiredText("Booking ID: ");
        int rating = readInteger("Rating (1-5): ");
        String comment = readRequiredText("Review comment: ");
        bookingService.addReview(bookingId, rating, comment);
        System.out.println("Review added to booking " + bookingId + '.');
    }

    private String readRequiredText(String prompt) {
        while (true) {
            System.out.print(prompt);
            if (!scanner.hasNextLine()) {
                throw new ValidationException("Input ended before the operation was completed");
            }
            String input = scanner.nextLine().trim();
            if (!input.isBlank()) {
                return input;
            }
            System.out.println("This value must not be blank.");
        }
    }

    private int readInteger(String prompt) {
        while (true) {
            String input = readRequiredText(prompt);
            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException exception) {
                System.out.println("Invalid input. Enter a whole number.");
            }
        }
    }
}
