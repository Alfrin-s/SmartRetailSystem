package controller;

import dao.ReportDAO;
import model.SalesReport;

import java.sql.SQLException;

/**
 * Controller for sales reporting and analytics.
 * Follows MVC pattern.
 */
public class ReportController {

    private final ReportDAO reportDAO;

    public ReportController() {
        this.reportDAO = new ReportDAO();
    }

    public SalesReport getSalesReport(String period) throws SQLException {
        if (period == null || period.trim().isEmpty()) {
            period = "Daily";
        }
        return reportDAO.getSalesReport(period);
    }
}
