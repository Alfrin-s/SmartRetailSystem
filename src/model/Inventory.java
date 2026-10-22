package model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Inventory aggregate model representing stock status summary and batch evaluations.
 * Demonstrates Business Logic segregation in Model layer.
 */
public class Inventory implements Serializable {
    private static final long serialVersionUID = 1L;

    private int totalProducts;
    private int lowStockCount;
    private int outOfStockCount;
    private int totalUnitsInStock;
    private List<Product> lowStockProducts;

    public Inventory() {
        this.lowStockProducts = new ArrayList<>();
    }

    public static Inventory calculate(List<Product> products) {
        Inventory inv = new Inventory();
        if (products == null) return inv;

        inv.totalProducts = products.size();
        for (Product p : products) {
            inv.totalUnitsInStock += p.getStock();
            if (p.isOutOfStock()) {
                inv.outOfStockCount++;
                inv.lowStockProducts.add(p);
            } else if (p.isLowStock()) {
                inv.lowStockCount++;
                inv.lowStockProducts.add(p);
            }
        }
        return inv;
    }

    public int getTotalProducts() {
        return totalProducts;
    }

    public int getLowStockCount() {
        return lowStockCount;
    }

    public int getOutOfStockCount() {
        return outOfStockCount;
    }

    public int getTotalAlertCount() {
        return lowStockCount + outOfStockCount;
    }

    public int getTotalUnitsInStock() {
        return totalUnitsInStock;
    }

    public List<Product> getLowStockProducts() {
        return lowStockProducts;
    }
}
