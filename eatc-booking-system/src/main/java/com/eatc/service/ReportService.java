package com.eatc.service;

import com.eatc.model.Booking;
import com.eatc.model.BookingStatus;
import com.eatc.model.Lesson;
import com.eatc.model.Subject;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.OptionalDouble;

public final class ReportService {
    private final TuitionCentre tuitionCentre;

    public ReportService(TuitionCentre tuitionCentre) {
        if (tuitionCentre == null) {
            throw new IllegalArgumentException("Tuition centre must not be null");
        }
        this.tuitionCentre = tuitionCentre;
    }

    public String generateLessonAttendanceAndRatingReport() {
        StringBuilder report = new StringBuilder("Lesson Attendance and Rating Report")
                .append(System.lineSeparator());

        List<Lesson> lessons = tuitionCentre.getAllLessons();
        if (lessons.isEmpty()) {
            return report.append("No lessons available").append(System.lineSeparator()).toString();
        }

        for (Lesson lesson : lessons) {
            OptionalDouble averageRating = lesson.getAverageRating();
            String ratingText = averageRating.isPresent()
                    ? String.format(Locale.ROOT, "%.2f", averageRating.getAsDouble())
                    : "No ratings yet";
            report.append("Lesson ID: ").append(lesson.getLessonId())
                    .append(" | Date: ").append(lesson.getDate())
                    .append(" | Day: ").append(lesson.getDayOfWeek())
                    .append(" | Session: ").append(lesson.getSession())
                    .append(" | Subject: ").append(lesson.getSubject().getName())
                    .append(" | Tutor: ").append(lesson.getTutorName())
                    .append(" | Booked Count: ").append(lesson.getCurrentBookedCount())
                    .append(" | Attended Count: ").append(lesson.getAttendedCount())
                    .append(" | Average Rating: ").append(ratingText)
                    .append(System.lineSeparator());
        }
        return report.toString();
    }

    public String generateHighestIncomeSubjectReport() {
        StringBuilder report = new StringBuilder("Subject Income Report")
                .append(System.lineSeparator());
        List<Subject> subjects = tuitionCentre.getAllSubjects();
        if (subjects.isEmpty()) {
            return report.append("No subjects available").append(System.lineSeparator()).toString();
        }

        Map<String, Integer> attendedCounts = new HashMap<>();
        for (Booking booking : tuitionCentre.getAllBookings()) {
            if (booking.getStatus() == BookingStatus.ATTENDED) {
                attendedCounts.merge(booking.getLesson().getSubject().getSubjectId(), 1, Integer::sum);
            }
        }

        Subject highestIncomeSubject = null;
        BigDecimal highestIncome = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        for (Subject subject : subjects) {
            int attendedCount = attendedCounts.getOrDefault(subject.getSubjectId(), 0);
            BigDecimal totalIncome = subject.getPrice()
                    .multiply(BigDecimal.valueOf(attendedCount))
                    .setScale(2, RoundingMode.HALF_UP);
            report.append("Subject: ").append(subject.getName())
                    .append(" | Price: ").append(subject.getPrice().toPlainString())
                    .append(" | Attended Booking Count: ").append(attendedCount)
                    .append(" | Total Income: ").append(totalIncome.toPlainString())
                    .append(System.lineSeparator());
            if (highestIncomeSubject == null || totalIncome.compareTo(highestIncome) > 0) {
                highestIncomeSubject = subject;
                highestIncome = totalIncome;
            }
        }

        report.append("Highest Income Subject: ").append(highestIncomeSubject.getName())
                .append(" | Total Income: ").append(highestIncome.toPlainString())
                .append(System.lineSeparator());
        return report.toString();
    }
}
