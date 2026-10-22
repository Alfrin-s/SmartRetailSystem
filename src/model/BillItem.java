package model;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * BillItem representing an itemized line inside a sales bill.
 * Demonstrates Encapsulation and integrity validation.
 */
public class BillItem extends BaseEntity {
    private static final long serialVersionUID = 1L;

    private Integer billId;
    private Integer productId;
    private String productName;
    private BigDecimal unitPrice;
    private int quantity;
    private BigDecimal totalPrice;

    public BillItem() {
        super();
        this.unitPrice = BigDecimal.ZERO;
        this.quantity = 1;
        this.totalPrice = BigDecimal.ZERO;
    }

    public BillItem(Integer productId, String productName, BigDecimal unitPrice, int quantity) {
        super();
        this.productId = productId;
        this.productName = productName;
        this.unitPrice = (unitPrice != null) ? unitPrice.setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
        setQuantity(quantity);
    }

    public BillItem(Integer id, Integer billId, Integer productId, String productName, BigDecimal unitPrice, int quantity, BigDecimal totalPrice) {
        super(id);
        this.billId = billId;
        this.productId = productId;
        this.productName = productName;
        this.unitPrice = (unitPrice != null) ? unitPrice.setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
        this.quantity = quantity;
        this.totalPrice = (totalPrice != null) ? totalPrice.setScale(2, RoundingMode.HALF_UP) : calculateTotal();
    }

    public Integer getBillId() {
        return billId;
    }

    public void setBillId(Integer billId) {
        this.billId = billId;
    }

    public Integer getProductId() {
        return productId;
    }

    public void setProductId(Integer productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        if (unitPrice == null || unitPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Unit price cannot be negative.");
        }
        this.unitPrice = unitPrice.setScale(2, RoundingMode.HALF_UP);
        this.totalPrice = calculateTotal();
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be at least 1.");
        }
        this.quantity = quantity;
        this.totalPrice = calculateTotal();
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = (totalPrice != null) ? totalPrice.setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
    }

    private BigDecimal calculateTotal() {
        if (unitPrice == null) return BigDecimal.ZERO;
        return unitPrice.multiply(BigDecimal.valueOf(quantity)).setScale(2, RoundingMode.HALF_UP);
    }
}
