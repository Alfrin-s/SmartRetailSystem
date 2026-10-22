package model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * Bill entity representing a customer sales receipt transaction.
 * Demonstrates Model Business Logic, Encapsulation, and Aggregate Roots.
 */
public class Bill extends BaseEntity {
    private static final long serialVersionUID = 1L;

    private String billNumber;
    private Integer cashierId;
    private String cashierName;
    private List<BillItem> items;
    private BigDecimal subtotal;
    private BigDecimal taxRate;    // e.g. 0.00 for 0%, 5.00 for 5%
    private BigDecimal taxAmount;
    private BigDecimal grandTotal;
    private String paymentMethod;
    private String status;

    public Bill() {
        super();
        this.items = new ArrayList<>();
        this.subtotal = BigDecimal.ZERO;
        this.taxRate = BigDecimal.ZERO;
        this.taxAmount = BigDecimal.ZERO;
        this.grandTotal = BigDecimal.ZERO;
        this.paymentMethod = "CASH";
        this.status = "COMPLETED";
    }

    public Bill(String billNumber, Integer cashierId, String cashierName) {
        this();
        this.billNumber = billNumber;
        this.cashierId = cashierId;
        this.cashierName = cashierName;
    }

    public void addItem(BillItem newItem) {
        if (newItem == null) return;

        // Check if item already exists in cart, then increment quantity
        boolean exists = false;
        for (BillItem item : items) {
            if (item.getProductId() != null && item.getProductId().equals(newItem.getProductId())) {
                item.setQuantity(item.getQuantity() + newItem.getQuantity());
                exists = true;
                break;
            }
        }
        if (!exists) {
            items.add(newItem);
        }
        recalculateTotals();
    }

    public void removeItem(int index) {
        if (index >= 0 && index < items.size()) {
            items.remove(index);
            recalculateTotals();
        }
    }

    public void updateItemQuantity(int index, int quantity) {
        if (index >= 0 && index < items.size()) {
            if (quantity <= 0) {
                items.remove(index);
            } else {
                items.get(index).setQuantity(quantity);
            }
            recalculateTotals();
        }
    }

    public void clear() {
        items.clear();
        recalculateTotals();
    }

    public void recalculateTotals() {
        BigDecimal sum = BigDecimal.ZERO;
        for (BillItem item : items) {
            if (item.getTotalPrice() != null) {
                sum = sum.add(item.getTotalPrice());
            }
        }
        this.subtotal = sum.setScale(2, RoundingMode.HALF_UP);
        if (taxRate != null && taxRate.compareTo(BigDecimal.ZERO) > 0) {
            this.taxAmount = this.subtotal.multiply(taxRate)
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        } else {
            this.taxAmount = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        this.grandTotal = this.subtotal.add(this.taxAmount).setScale(2, RoundingMode.HALF_UP);
    }

    public int getTotalUnitsCount() {
        int count = 0;
        for (BillItem item : items) {
            count += item.getQuantity();
        }
        return count;
    }

    public String getBillNumber() {
        return billNumber;
    }

    public void setBillNumber(String billNumber) {
        this.billNumber = billNumber;
    }

    public Integer getCashierId() {
        return cashierId;
    }

    public void setCashierId(Integer cashierId) {
        this.cashierId = cashierId;
    }

    public String getCashierName() {
        return cashierName;
    }

    public void setCashierName(String cashierName) {
        this.cashierName = cashierName;
    }

    public List<BillItem> getItems() {
        return items;
    }

    public void setItems(List<BillItem> items) {
        this.items = (items != null) ? items : new ArrayList<BillItem>();
        recalculateTotals();
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = (subtotal != null) ? subtotal.setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
    }

    public BigDecimal getTaxRate() {
        return taxRate;
    }

    public void setTaxRate(BigDecimal taxRate) {
        this.taxRate = (taxRate != null) ? taxRate : BigDecimal.ZERO;
        recalculateTotals();
    }

    public BigDecimal getTaxAmount() {
        return taxAmount;
    }

    public void setTaxAmount(BigDecimal taxAmount) {
        this.taxAmount = (taxAmount != null) ? taxAmount.setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
    }

    public BigDecimal getGrandTotal() {
        return grandTotal;
    }

    public void setGrandTotal(BigDecimal grandTotal) {
        this.grandTotal = (grandTotal != null) ? grandTotal.setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
