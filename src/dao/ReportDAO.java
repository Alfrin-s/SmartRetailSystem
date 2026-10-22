package dao;

import model.SalesReport;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Data Access Object for sales aggregation and analytics.
 */
public class ReportDAO extends BaseDAO {

    public SalesReport getSalesReport(String period) throws SQLException {
        SalesReport report = new SalesReport(period);
        Connection conn = null;
        Statement stmt = null;
        PreparedStatement groupStmt = null;
        ResultSet rsSummary = null;
        ResultSet rsItems = null;
        ResultSet rsGroup = null;

        try {
            conn = getConnection();

            // 1. Overall totals
            stmt = conn.createStatement();
            rsSummary = stmt.executeQuery("SELECT COUNT(*), COALESCE(SUM(grand_total), 0) FROM bills WHERE status = 'COMPLETED'");
            if (rsSummary.next()) {
                report.setTotalBills(rsSummary.getInt(1));
                report.setTotalSales(rsSummary.getBigDecimal(2));
            }

            // 2. Total items sold
            rsItems = stmt.executeQuery("SELECT COALESCE(SUM(quantity), 0) FROM bill_items");
            if (rsItems.next()) {
                report.setTotalItemsSold(rsItems.getInt(1));
            }

            // 3. Grouped breakdown based on period
            // Universal date string slicing: substr(created_at, 1, 10) for daily, substr(created_at, 1, 7) for monthly
            String groupSql;
            if ("Monthly".equalsIgnoreCase(period)) {
                groupSql = "SELECT substr(created_at, 1, 7) AS period_key, COUNT(*) AS bill_count, SUM(grand_total) AS total_val " +
                        "FROM bills WHERE status = 'COMPLETED' " +
                        "GROUP BY period_key ORDER BY period_key DESC LIMIT 12";
            } else if ("Weekly".equalsIgnoreCase(period)) {
                // Group by recent 7-day slices or dates
                groupSql = "SELECT substr(created_at, 1, 10) AS period_key, COUNT(*) AS bill_count, SUM(grand_total) AS total_val " +
                        "FROM bills WHERE status = 'COMPLETED' " +
                        "GROUP BY period_key ORDER BY period_key DESC LIMIT 7";
            } else {
                // Daily (default)
                groupSql = "SELECT substr(created_at, 1, 10) AS period_key, COUNT(*) AS bill_count, SUM(grand_total) AS total_val " +
                        "FROM bills WHERE status = 'COMPLETED' " +
                        "GROUP BY period_key ORDER BY period_key DESC LIMIT 15";
            }

            groupStmt = conn.prepareStatement(groupSql);
            rsGroup = groupStmt.executeQuery();
            while (rsGroup.next()) {
                String key = rsGroup.getString("period_key");
                int count = rsGroup.getInt("bill_count");
                BigDecimal amt = rsGroup.getBigDecimal("total_val");
                report.addEntry(new SalesReport.SalesEntry(key != null ? key : "Unknown", count, amt));
            }

            return report;
        } finally {
            close(rsGroup, groupStmt, rsItems, rsSummary, stmt, conn);
        }
    }
}
