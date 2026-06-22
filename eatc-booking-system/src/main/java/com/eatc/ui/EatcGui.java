package com.eatc.ui;

import com.eatc.exception.BookingException;
import com.eatc.exception.ValidationException;
import com.eatc.model.Booking;
import com.eatc.model.Lesson;
import com.eatc.model.Student;
import com.eatc.model.Subject;
import com.eatc.service.BookingService;
import com.eatc.service.ReportService;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.List;
import java.util.Locale;

public final class EatcGui {
    private final BookingService bookingService;
    private final ReportService reportService;
    private final JFrame frame;
    private final JTable studentsTable;
    private final JTable subjectsTable;
    private final JTable lessonsTable;
    private final JTable bookingsTable;
    private final DefaultTableModel studentsModel;
    private final DefaultTableModel subjectsModel;
    private final DefaultTableModel lessonsModel;
    private final DefaultTableModel bookingsModel;
    private final JTextArea attendanceReportArea;
    private final JTextArea incomeReportArea;
    private final JLabel statusLabel;

    public EatcGui(BookingService bookingService, ReportService reportService) {
        if (bookingService == null || reportService == null) {
            throw new IllegalArgumentException("Services must not be null");
        }
        this.bookingService = bookingService;
        this.reportService = reportService;

        this.studentsModel = createTableModel(
                "Student ID", "Name", "Gender", "Date of Birth", "Address", "Emergency Contact");
        this.subjectsModel = createTableModel("Subject ID", "Subject", "Lesson Price");
        this.lessonsModel = createTableModel(
                "Lesson ID", "Date", "Day", "Session", "Start", "End", "Subject",
                "Price", "Tutor", "Active Bookings", "Attended", "Average Rating");
        this.bookingsModel = createTableModel(
                "Booking ID", "Student ID", "Student", "Lesson ID", "Subject", "Date",
                "Session", "Time", "Status", "Rating", "Review");

        this.studentsTable = createTable(studentsModel);
        this.subjectsTable = createTable(subjectsModel);
        this.lessonsTable = createTable(lessonsModel);
        this.bookingsTable = createTable(bookingsModel);
        this.attendanceReportArea = createReportArea();
        this.incomeReportArea = createReportArea();
        this.statusLabel = new JLabel("Ready");

        this.frame = new JFrame("Excel Academy Tuition Centre - EATC Booking System");
        configureFrame();
    }

    public void show() {
        refreshAll();
        frame.setVisible(true);
    }

    private void configureFrame() {
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setMinimumSize(new Dimension(1100, 700));
        frame.setSize(1450, 820);
        frame.setLocationRelativeTo(null);
        frame.setJMenuBar(createMenuBar());

        JPanel content = new JPanel(new BorderLayout(8, 8));
        content.setBorder(new EmptyBorder(10, 10, 8, 10));
        content.add(createHeader(), BorderLayout.NORTH);
        content.add(createTabs(), BorderLayout.CENTER);

        statusLabel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEtchedBorder(), new EmptyBorder(5, 8, 5, 8)));
        content.add(statusLabel, BorderLayout.SOUTH);
        frame.setContentPane(content);
    }

    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        JMenuItem refreshItem = new JMenuItem("Refresh All");
        refreshItem.addActionListener(event -> refreshAll());
        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.addActionListener(event -> frame.dispose());
        fileMenu.add(refreshItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);

        JMenu bookingMenu = new JMenu("Booking Actions");
        JMenuItem bookItem = new JMenuItem("Book Lesson");
        bookItem.addActionListener(event -> bookLesson());
        JMenuItem changeItem = new JMenuItem("Change Selected Booking");
        changeItem.addActionListener(event -> changeBooking());
        JMenuItem cancelItem = new JMenuItem("Cancel Selected Booking");
        cancelItem.addActionListener(event -> cancelBooking());
        JMenuItem attendItem = new JMenuItem("Mark Selected Booking Attended");
        attendItem.addActionListener(event -> attendLesson());
        JMenuItem reviewItem = new JMenuItem("Review Selected Booking");
        reviewItem.addActionListener(event -> addReview());
        bookingMenu.add(bookItem);
        bookingMenu.add(changeItem);
        bookingMenu.add(cancelItem);
        bookingMenu.add(attendItem);
        bookingMenu.add(reviewItem);

        menuBar.add(fileMenu);
        menuBar.add(bookingMenu);
        return menuBar;
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEtchedBorder(), new EmptyBorder(10, 14, 10, 14)));
        JLabel title = new JLabel("Excel Academy Tuition Centre", SwingConstants.LEFT);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 22f));
        JLabel subtitle = new JLabel("Student lesson booking, attendance, reviews and reports");
        subtitle.setFont(subtitle.getFont().deriveFont(Font.PLAIN, 13f));
        JPanel labels = new JPanel(new GridLayout(2, 1));
        labels.add(title);
        labels.add(subtitle);
        header.add(labels, BorderLayout.WEST);

        JButton refreshButton = new JButton("Refresh All");
        refreshButton.addActionListener(event -> refreshAll());
        header.add(refreshButton, BorderLayout.EAST);
        return header;
    }

    private JTabbedPane createTabs() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Students", createTablePanel(studentsTable, null));
        tabs.addTab("Subjects", createTablePanel(subjectsTable, null));
        tabs.addTab("Lessons", createLessonsPanel());
        tabs.addTab("Bookings", createBookingsPanel());
        tabs.addTab("Reports", createReportsPanel());
        return tabs;
    }

    private JPanel createLessonsPanel() {
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton bookButton = new JButton("Book Selected Lesson");
        bookButton.addActionListener(event -> bookSelectedLesson());
        JButton refreshButton = new JButton("Refresh Lessons");
        refreshButton.addActionListener(event -> refreshLessons());
        buttons.add(bookButton);
        buttons.add(refreshButton);
        return createTablePanel(lessonsTable, buttons);
    }

    private JPanel createBookingsPanel() {
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton bookButton = new JButton("New Booking");
        JButton changeButton = new JButton("Change Lesson");
        JButton cancelButton = new JButton("Cancel Booking");
        JButton attendButton = new JButton("Mark Attended");
        JButton reviewButton = new JButton("Add Review");
        JButton refreshButton = new JButton("Refresh Bookings");

        bookButton.addActionListener(event -> bookLesson());
        changeButton.addActionListener(event -> changeBooking());
        cancelButton.addActionListener(event -> cancelBooking());
        attendButton.addActionListener(event -> attendLesson());
        reviewButton.addActionListener(event -> addReview());
        refreshButton.addActionListener(event -> refreshBookings());

        buttons.add(bookButton);
        buttons.add(changeButton);
        buttons.add(cancelButton);
        buttons.add(attendButton);
        buttons.add(reviewButton);
        buttons.add(refreshButton);
        return createTablePanel(bookingsTable, buttons);
    }

    private JPanel createReportsPanel() {
        JPanel attendancePanel = new JPanel(new BorderLayout(5, 5));
        JButton attendanceButton = new JButton("Generate Attendance and Rating Report");
        attendanceButton.addActionListener(event -> refreshAttendanceReport());
        attendancePanel.add(attendanceButton, BorderLayout.NORTH);
        attendancePanel.add(new JScrollPane(attendanceReportArea), BorderLayout.CENTER);

        JPanel incomePanel = new JPanel(new BorderLayout(5, 5));
        JButton incomeButton = new JButton("Generate Highest Income Subject Report");
        incomeButton.addActionListener(event -> refreshIncomeReport());
        incomePanel.add(incomeButton, BorderLayout.NORTH);
        incomePanel.add(new JScrollPane(incomeReportArea), BorderLayout.CENTER);

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, attendancePanel, incomePanel);
        splitPane.setResizeWeight(0.65);
        splitPane.setDividerLocation(430);

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(splitPane, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createTablePanel(JTable table, JPanel buttons) {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        if (buttons != null) {
            panel.add(buttons, BorderLayout.NORTH);
        }
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private DefaultTableModel createTableModel(String... columns) {
        return new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }

    private JTable createTable(DefaultTableModel model) {
        JTable table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoCreateRowSorter(true);
        table.setRowHeight(24);
        table.getTableHeader().setReorderingAllowed(false);
        table.setFillsViewportHeight(true);
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);
        return table;
    }

    private JTextArea createReportArea() {
        JTextArea area = new JTextArea();
        area.setEditable(false);
        area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        area.setLineWrap(false);
        area.setBorder(new EmptyBorder(6, 6, 6, 6));
        return area;
    }

    private void refreshAll() {
        refreshStudents();
        refreshSubjects();
        refreshLessons();
        refreshBookings();
        refreshAttendanceReport();
        refreshIncomeReport();
        setStatus("All data refreshed");
    }

    private void refreshStudents() {
        studentsModel.setRowCount(0);
        for (Student student : bookingService.getAllStudents()) {
            studentsModel.addRow(new Object[]{
                    student.getStudentId(), student.getName(), student.getGender(),
                    student.getDateOfBirth(), student.getAddress(), student.getEmergencyContactNumber()
            });
        }
    }

    private void refreshSubjects() {
        subjectsModel.setRowCount(0);
        for (Subject subject : bookingService.getAllSubjects()) {
            subjectsModel.addRow(new Object[]{
                    subject.getSubjectId(), subject.getName(), subject.getPrice().toPlainString()
            });
        }
    }

    private void refreshLessons() {
        lessonsModel.setRowCount(0);
        for (Lesson lesson : bookingService.getAllLessons()) {
            String averageRating = lesson.getAverageRating().isPresent()
                    ? String.format(Locale.ROOT, "%.2f", lesson.getAverageRating().getAsDouble())
                    : "No ratings yet";
            lessonsModel.addRow(new Object[]{
                    lesson.getLessonId(), lesson.getDate(), lesson.getDayOfWeek(), lesson.getSession(),
                    lesson.getStartTime(), lesson.getEndTime(), lesson.getSubject().getName(),
                    lesson.getSubject().getPrice().toPlainString(), lesson.getTutorName(),
                    lesson.getCurrentBookedCount() + "/" + Lesson.getMaxCapacity(),
                    lesson.getAttendedCount(), averageRating
            });
        }
    }

    private void refreshBookings() {
        bookingsModel.setRowCount(0);
        for (Booking booking : bookingService.getAllBookings()) {
            String rating = booking.getReview()
                    .map(review -> Integer.toString(review.getRating()))
                    .orElse("");
            String comment = booking.getReview()
                    .map(review -> review.getComment())
                    .orElse("");
            Lesson lesson = booking.getLesson();
            bookingsModel.addRow(new Object[]{
                    booking.getBookingId(), booking.getStudent().getStudentId(),
                    booking.getStudent().getName(), lesson.getLessonId(), lesson.getSubject().getName(),
                    lesson.getDate(), lesson.getSession(), lesson.getStartTime() + "-" + lesson.getEndTime(),
                    booking.getStatus(), rating, comment
            });
        }
    }

    private void refreshAttendanceReport() {
        attendanceReportArea.setText(reportService.generateLessonAttendanceAndRatingReport());
        attendanceReportArea.setCaretPosition(0);
    }

    private void refreshIncomeReport() {
        incomeReportArea.setText(reportService.generateHighestIncomeSubjectReport());
        incomeReportArea.setCaretPosition(0);
    }

    private void bookSelectedLesson() {
        String lessonId = getSelectedId(lessonsTable, lessonsModel, "Select a lesson first.");
        if (lessonId == null) {
            return;
        }
        SelectionOption student = chooseStudent();
        if (student == null) {
            return;
        }
        performAction("Booking created", () -> bookingService.bookLesson(student.getId(), lessonId));
    }

    private void bookLesson() {
        SelectionOption student = chooseStudent();
        if (student == null) {
            return;
        }
        SelectionOption lesson = chooseLesson("Select lesson", bookingService.getAllLessons());
        if (lesson == null) {
            return;
        }
        performAction("Booking created", () -> bookingService.bookLesson(student.getId(), lesson.getId()));
    }

    private void changeBooking() {
        String bookingId = getSelectedBookingId();
        if (bookingId == null) {
            return;
        }
        Booking booking = bookingService.findBookingById(bookingId).orElse(null);
        if (booking == null) {
            showError("Booking no longer exists.");
            return;
        }
        List<Lesson> matchingLessons = bookingService.getAllLessons().stream()
                .filter(lesson -> lesson.getSubject().getSubjectId()
                        .equals(booking.getLesson().getSubject().getSubjectId()))
                .filter(lesson -> !lesson.getLessonId().equals(booking.getLesson().getLessonId()))
                .toList();
        SelectionOption lesson = chooseLesson("Select replacement lesson", matchingLessons);
        if (lesson == null) {
            return;
        }
        performAction("Booking changed", () -> bookingService.changeBooking(bookingId, lesson.getId()));
    }

    private void cancelBooking() {
        String bookingId = getSelectedBookingId();
        if (bookingId == null) {
            return;
        }
        int answer = JOptionPane.showConfirmDialog(frame,
                "Cancel booking " + bookingId + "?", "Confirm cancellation",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (answer == JOptionPane.YES_OPTION) {
            performAction("Booking cancelled", () -> bookingService.cancelBooking(bookingId));
        }
    }

    private void attendLesson() {
        String bookingId = getSelectedBookingId();
        if (bookingId == null) {
            return;
        }
        performAction("Attendance recorded", () -> bookingService.attendLesson(bookingId));
    }

    private void addReview() {
        String bookingId = getSelectedBookingId();
        if (bookingId == null) {
            return;
        }

        JSpinner ratingSpinner = new JSpinner(new SpinnerNumberModel(5, 1, 5, 1));
        JTextArea commentArea = new JTextArea(5, 32);
        commentArea.setLineWrap(true);
        commentArea.setWrapStyleWord(true);
        JPanel form = new JPanel(new BorderLayout(6, 6));
        JPanel ratingPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        ratingPanel.add(new JLabel("Rating (1-5):"));
        ratingPanel.add(ratingSpinner);
        form.add(ratingPanel, BorderLayout.NORTH);
        form.add(new JScrollPane(commentArea), BorderLayout.CENTER);

        int result = JOptionPane.showConfirmDialog(frame, form,
                "Review booking " + bookingId, JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            int rating = (Integer) ratingSpinner.getValue();
            String comment = commentArea.getText();
            performAction("Review added", () -> bookingService.addReview(bookingId, rating, comment));
        }
    }

    private SelectionOption chooseStudent() {
        List<SelectionOption> options = bookingService.getAllStudents().stream()
                .map(student -> new SelectionOption(student.getStudentId(),
                        student.getStudentId() + " - " + student.getName()))
                .toList();
        return chooseOption("Select student", "Student:", options);
    }

    private SelectionOption chooseLesson(String title, List<Lesson> lessons) {
        List<SelectionOption> options = lessons.stream()
                .map(lesson -> new SelectionOption(lesson.getLessonId(),
                        lesson.getLessonId() + " - " + lesson.getDate() + " " + lesson.getSession()
                                + " - " + lesson.getSubject().getName() + " - "
                                + lesson.getCurrentBookedCount() + "/" + Lesson.getMaxCapacity()))
                .toList();
        return chooseOption(title, "Lesson:", options);
    }

    private SelectionOption chooseOption(String title, String message, List<SelectionOption> options) {
        if (options.isEmpty()) {
            showError("No matching records are available.");
            return null;
        }
        Object selected = JOptionPane.showInputDialog(frame, message, title,
                JOptionPane.PLAIN_MESSAGE, null, options.toArray(), options.get(0));
        return selected instanceof SelectionOption option ? option : null;
    }

    private String getSelectedBookingId() {
        return getSelectedId(bookingsTable, bookingsModel, "Select a booking first.");
    }

    private String getSelectedId(JTable table, DefaultTableModel model, String errorMessage) {
        int selectedViewRow = table.getSelectedRow();
        if (selectedViewRow < 0) {
            showError(errorMessage);
            return null;
        }
        int selectedModelRow = table.convertRowIndexToModel(selectedViewRow);
        return model.getValueAt(selectedModelRow, 0).toString();
    }

    private void performAction(String successMessage, GuiAction action) {
        try {
            action.run();
            refreshAll();
            setStatus(successMessage);
            JOptionPane.showMessageDialog(frame, successMessage + '.', "Success",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (BookingException | ValidationException exception) {
            showError(exception.getMessage());
        } catch (RuntimeException exception) {
            showError("Unable to complete the operation: " + exception.getMessage());
        }
    }

    private void showError(String message) {
        setStatus("Error: " + message);
        JOptionPane.showMessageDialog(frame, message, "EATC Error", JOptionPane.ERROR_MESSAGE);
    }

    private void setStatus(String message) {
        statusLabel.setText(message);
    }

    @FunctionalInterface
    private interface GuiAction {
        void run();
    }

    private static final class SelectionOption {
        private final String id;
        private final String label;

        private SelectionOption(String id, String label) {
            this.id = id;
            this.label = label;
        }

        private String getId() {
            return id;
        }

        @Override
        public String toString() {
            return label;
        }
    }
}
