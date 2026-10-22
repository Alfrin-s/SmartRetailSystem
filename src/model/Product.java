package model;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Product entity representing catalog items with pricing and stock levels.
 * Demonstrates Encapsulation and business rule enforcement.
 */
public class Product extends BaseEntity {
    private static final long serialVersionUID = 1L;

    public static final String STATUS_OK = "OK";
    public static final String STATUS_LOW = "LOW";
    public static final String STATUS_OUT_OF_STOCK = "OUT OF STOCK";

    private String barcode;
    private String name;
    private String category;
    private BigDecimal price;
    private int stock;
    private int minStock;
    private String unit;

    public Product() {
        super();
        this.price = BigDecimal.ZERO;
        this.stock = 0;
        this.minStock = 10;
        this.unit = "unit";
    }

    public Product(Integer id, String barcode, String name, String category, BigDecimal price, int stock, int minStock, String unit) {
        super(id);
        this.barcode = barcode;
        this.name = name;
        this.category = category;
        setPrice(price);
        setStock(stock);
        setMinStock(minStock);
        this.unit = (unit != null && !unit.trim().isEmpty()) ? unit.trim() : "unit";
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = (barcode != null) ? barcode.trim() : null;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = (name != null) ? name.trim() : null;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = (category != null) ? category.trim() : "General";
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        if (price == null || price.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Product price cannot be negative.");
        }
        this.price = price.setScale(2, RoundingMode.HALF_UP);
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        if (stock < 0) {
            throw new IllegalArgumentException("Stock quantity cannot be negative.");
        }
        this.stock = stock;
    }

    public int getMinStock() {
        return minStock;
    }

    public void setMinStock(int minStock) {
        if (minStock < 0) {
            minStock = 0;
        }
        this.minStock = minStock;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public boolean isLowStock() {
        return stock > 0 && stock <= minStock;
    }

    public boolean isOutOfStock() {
        return stock <= 0;
    }

    public String getStockStatus() {
        if (isOutOfStock()) {
            return STATUS_OUT_OF_STOCK;
        } else if (isLowStock()) {
            return STATUS_LOW;
        } else {
            return STATUS_OK;
        }
    }

    @Override
    public String toString() {
        return name + " - \u20B9" + price;
    }
}
