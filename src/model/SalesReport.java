package model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * SalesReport entity representing aggregated analytics for daily, weekly, or monthly periods.
 */
public class SalesReport implements Serializable {
    private static final long serialVersionUID = 1L;

    public static class SalesEntry implements Serializable {
        private static final long serialVersionUID = 1L;
        private String datePeriod;
        private int billCount;
        private BigDecimal totalAmount;

        public SalesEntry(String datePeriod, int billCount, BigDecimal totalAmount) {
            this.datePeriod = datePeriod;
            this.billCount = billCount;
            this.totalAmount = (totalAmount != null) ? totalAmount : BigDecimal.ZERO;
        }

        public String getDatePeriod() {
            return datePeriod;
        }

        public int getBillCount() {
            return billCount;
        }

        public BigDecimal getTotalAmount() {
            return totalAmount;
        }
    }

    private String period; // "Daily", "Weekly", "Monthly"
    private BigDecimal totalSales;
    private int totalBills;
    private int totalItemsSold;
    private List<SalesEntry> entries;

    public SalesReport() {
        this.totalSales = BigDecimal.ZERO;
        this.totalBills = 0;
        this.totalItemsSold = 0;
        this.entries = new ArrayList<>();
    }

    public SalesReport(String period) {
        this();
        this.period = period;
    }

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    public BigDecimal getTotalSales() {
        return totalSales;
    }

    public void setTotalSales(BigDecimal totalSales) {
        this.totalSales = (totalSales != null) ? totalSales : BigDecimal.ZERO;
    }

    public int getTotalBills() {
        return totalBills;
    }

    public void setTotalBills(int totalBills) {
        this.totalBills = totalBills;
    }

    public int getTotalItemsSold() {
        return totalItemsSold;
    }

    public void setTotalItemsSold(int totalItemsSold) {
        this.totalItemsSold = totalItemsSold;
    }

    public List<SalesEntry> getEntries() {
        return entries;
    }

    public void setEntries(List<SalesEntry> entries) {
        this.entries = (entries != null) ? entries : new ArrayList<SalesEntry>();
    }

    public void addEntry(SalesEntry entry) {
        if (entry != null) {
            entries.add(entry);
        }
    }
}
