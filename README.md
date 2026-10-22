# Smart Retail Inventory & Billing System

A desktop Retail Management & Point-of-Sale (POS) application built with **Java Swing**, following the **Model-View-Controller (MVC)** architectural pattern, **Data Access Object (DAO)** pattern, **Role-Based Access Control (RBAC)**, and **ACID database transactions**.

---

## Architecture Overview (MVC Pattern)

```
                 USER / CASHIER / ADMIN
                           │
                           ▼
                 ┌───────────────────┐
                 │    VIEW LAYER     │
                 │    (Java Swing)   │
                 └─────────┬─────────┘
                           │ (Actions & Events)
                           ▼
                 ┌───────────────────┐
                 │ CONTROLLER LAYER  │
                 │ (Business Orchestr)
                 └─────────┬─────────┘
                           │ (State & Operations)
                           ▼
                 ┌───────────────────┐
                 │    MODEL LAYER    │
                 │  (Business Logic) │
                 └─────────┬─────────┘
                           │ (CRUD & Transactions)
                           ▼
                 ┌───────────────────┐
                 │     DAO LAYER     │
                 │   (BaseDAO/JDBC)  │
                 └─────────┬─────────┘
                           │ (PreparedStatements)
                           ▼
                 ┌───────────────────┐
                 │     DATABASE      │
                 │     MySQL 8.x     │
                 └───────────────────┘
```

1. **View Layer** (`src/view/`):
   - `LoginFrame`: Credential verification and role-based portal routing.
   - `AdminDashboard`: Back-office portal with sidebar navigation, real-time KPI metrics, and recent sales.
   - `CashierDashboard`: Dedicated checkout station for front-desk cashiers.
   - `ProductPanel`: Catalog management (search, add, edit, delete).
   - `InventoryPanel`: Stock health tracking with custom badge rendering (`OK`, `LOW STOCK`, `OUT OF STOCK`) and instant restocking.
   - `BillingPanel`: Two-column POS layout (live catalog search on left, current bill/cart with tax calculation on right).
   - `ReceiptDialog`: Thermal-styled receipt preview with PDF and TXT export.
   - `ReportsPanel`: Analytics breakdown (Daily, Weekly, Monthly) and interactive custom 2D Bar Chart.
   - `UserPanel`: Admin staff account management (RBAC).

2. **Controller Layer** (`src/controller/`):
   - Decouples View events from database operations.
   - `LoginController`, `ProductController`, `InventoryController`, `BillingController`, `ReportController`, `UserController`.

3. **Model Layer** (`src/model/`):
   - Pure domain models and business calculation logic (tax computation, cart item aggregation, stock health evaluation).
   - Zero Swing or SQL dependencies.
   - `BaseEntity`, `User`, `Product`, `Inventory`, `Bill`, `BillItem`, `SalesReport`.

4. **DAO Layer** (`src/dao/`):
   - JDBC persistence with `PreparedStatement` to prevent SQL injection.
   - `BaseDAO`: Abstract base class handling connection pooling and resource closing.
   - `UserDAO`, `ProductDAO`, `BillDAO`, `ReportDAO`.

---

## Viva & Examination Guide (OOP & Design Concepts)

When examiners ask about software engineering principles, here is how they are implemented:

| Concept | Implementation in Code | Where to Show in Presentation |
|---|---|---|
| **Encapsulation** | Private fields with strict validation in setters (e.g. `Product.setPrice`, `BillItem.setQuantity` preventing negative values). | `model.Product`, `model.BillItem` |
| **Inheritance** | `BaseEntity` inherited by `User`, `Product`, `Bill`, `BillItem`. `BaseDAO` inherited by all DAOs. | `model.BaseEntity`, `dao.BaseDAO` |
| **Polymorphism** | `StockStatusCellRenderer` rendering badges dynamically. `ReceiptExporter` interface with `TextExporter` and `PdfExporter` strategies. | `view.StockStatusCellRenderer`, `util.ReceiptExporter` |
| **Abstraction** | `BaseDAO` abstracts connection management and cleanup. `ReceiptExporter` defines contract for invoice generation. | `dao.BaseDAO`, `util.ReceiptExporter` |
| **MVC Separation** | Swing components contain **no SQL queries**. Views communicate exclusively with Controllers; Controllers interact with Models and DAOs. | Compare `view.BillingPanel` with `controller.BillingController` and `dao.BillDAO` |
| **ACID Transactions** | In `BillDAO.createBillWithItems()`, `conn.setAutoCommit(false)` is used. If any product has insufficient stock, `conn.rollback()` executes, guaranteeing stock consistency. | `dao.BillDAO.java` line 35 |
| **RBAC Security** | Role distinction (`ADMIN` vs `CASHIER`) routes users to distinct dashboards with restricted action sets. | `view.LoginFrame`, `model.User` |

---

## Database Configuration (MySQL)

The system uses standard **MySQL** via JDBC (`mysql-connector-j-8.3.0.jar`).

1. **Automatic Initialization**:
   - When the application starts, it connects to MySQL, automatically creates the `smart_retail` database (if it doesn't already exist), builds all tables (`users`, `products`, `bills`, `bill_items`), and seeds initial admin and cashier accounts and sample catalog products.

2. **Configuration (`resources/db.properties`)**:
   - Ensure MySQL is running (e.g. in XAMPP, WAMP, or MySQL Workbench).
   - Configure connection settings:
     ```properties
     db.host=localhost
     db.port=3306
     db.database=smart_retail
     db.user=root
     db.password=
     ```

3. **Manual SQL Script (Optional / For Examiners)**:
   - Full SQL DDL with constraints and seed data is available at:
     `resources/schema.sql`

---

## Default Login Credentials

| Role | Username | Password | Access Level |
|---|---|---|---|
| **Admin** | `admin` | `admin123` | Full back-office, products, inventory, users, reports, billing |
| **Cashier** | `cashier` | `cashier123` | POS checkout station, live search, billing, receipts |

---

## How to Build and Run

### Prerequisites
- Java Development Kit (JDK 17 or later).

### 1. Compile
```cmd
build.bat
```

### 2. Run Automated Verification Tests
```cmd
test.bat
```

### 3. Launch Application
```cmd
run.bat
```
