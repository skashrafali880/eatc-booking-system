package com.eatc;

import com.eatc.service.BookingService;
import com.eatc.service.DataSeeder;
import com.eatc.service.ReportService;
import com.eatc.service.TuitionCentre;
import com.eatc.ui.EatcGui;

import javax.swing.SwingUtilities;

public final class Main {
    private Main() {
    }

    public static void main(String[] args) {
        TuitionCentre tuitionCentre = new TuitionCentre();
        DataSeeder.seed(tuitionCentre);
        BookingService bookingService = new BookingService(tuitionCentre);
        ReportService reportService = new ReportService(tuitionCentre);
        SwingUtilities.invokeLater(() -> new EatcGui(bookingService, reportService).show());
    }
}
