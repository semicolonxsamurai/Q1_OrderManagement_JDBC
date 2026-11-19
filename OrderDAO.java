package com.ecommerce.oms;

import java.sql.*;
import java.util.List;

public class OrderDAO {

    private static final List<String> VALID_STATUSES =
            List.of("Pending", "Shipped", "Delivered", "Cancelled");

    // ---------- (a) Insert with PreparedStatement, handle duplicate order IDs ----------
    public void insertOrder(Order o) {
        String sql = "INSERT INTO Orders (order_id, customer_name, product_name, quantity, unit_price, status) "
                   + "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, o.orderId());
            ps.setString(2, o.customerName());
            ps.setString(3, o.productName());
            ps.setInt(4, o.quantity());
            ps.setDouble(5, o.unitPrice());
            ps.setString(6, o.status());
            ps.executeUpdate();
            System.out.println("Inserted order " + o.orderId());

        } catch (SQLIntegrityConstraintViolationException e) {
            System.out.println("Order ID " + o.orderId() + " already exists. Skipped (duplicate).");
        } catch (SQLException e) {
            System.out.println("Insert failed: " + e.getMessage());
        }
    }

    public void insertSampleOrders() {
        insertOrder(new Order(101, "Aarav Sharma", "Laptop", 1, 55000.00, "Pending"));
        insertOrder(new Order(102, "Priya Singh", "Wireless Mouse", 5, 799.50, "Shipped"));
        insertOrder(new Order(103, "Rohan Verma", "USB-C Cable", 10, 349.00, "Delivered"));
        insertOrder(new Order(104, "Neha Gupta", "Keyboard", 3, 1499.00, "Cancelled"));
    }

    // ---------- (b) Orders by status with quantity > 2, sorted by unit_price DESC ----------
    public void showByStatus(String status) {
        String sql = "SELECT * FROM Orders WHERE status = ? AND quantity > 2 ORDER BY unit_price DESC";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, status);
            try (ResultSet rs = ps.executeQuery()) {
                printHeader();
                boolean found = false;
                while (rs.next()) {
                    printRow(rs);
                    found = true;
                }
                if (!found) {
                    System.out.println("No orders found with status '" + status + "' and quantity > 2.");
                }
            }
        } catch (SQLException e) {
            System.out.println("Query failed: " + e.getMessage());
        }
    }

    // ---------- (c) Increase quantity ----------
    public void increaseQuantity(int orderId, int extra) {
        String sql = "UPDATE Orders SET quantity = quantity + ? WHERE order_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, extra);
            ps.setInt(2, orderId);
            int rows = ps.executeUpdate();
            if (rows > 0) {
                System.out.println("Quantity increased by " + extra + " for order " + orderId + ".");
            } else {
                System.out.println("Order " + orderId + " does not exist.");
            }
        } catch (SQLException e) {
            System.out.println("Update failed: " + e.getMessage());
        }
    }

    // ---------- (d) Update status only if order exists and status is valid ----------
    public void updateStatus(int orderId, String newStatus) {
        String valid = VALID_STATUSES.stream()
                .filter(s -> s.equalsIgnoreCase(newStatus.trim()))
                .findFirst().orElse(null);

        if (valid == null) {
            System.out.println("Invalid status. Allowed values: " + VALID_STATUSES);
            return;
        }

        String select = "SELECT * FROM Orders WHERE order_id = ?";
        String update = "UPDATE Orders SET status = ? WHERE order_id = ?";
        try (Connection con = DBConnection.getConnection()) {

            // Retrieve order details first
            try (PreparedStatement ps = con.prepareStatement(select)) {
                ps.setInt(1, orderId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        System.out.println("Order " + orderId + " does not exist.");
                        return;
                    }
                    System.out.println("Before update:");
                    printHeader();
                    printRow(rs);
                }
            }

            try (PreparedStatement ps = con.prepareStatement(update)) {
                ps.setString(1, valid);
                ps.setInt(2, orderId);
                ps.executeUpdate();
            }

            // Display updated details
            try (PreparedStatement ps = con.prepareStatement(select)) {
                ps.setInt(1, orderId);
                try (ResultSet rs = ps.executeQuery()) {
                    System.out.println("After update:");
                    printHeader();
                    while (rs.next()) printRow(rs);
                }
            }
        } catch (SQLException e) {
            System.out.println("Status update failed: " + e.getMessage());
        }
    }

    // ---------- (e) Total order value ----------
    public void showTotalOrderValue() {
        String sql = "SELECT SUM(quantity * unit_price) AS total FROM Orders";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                System.out.printf("Total order value: %.2f%n", rs.getDouble("total"));
            }
        } catch (SQLException e) {
            System.out.println("Query failed: " + e.getMessage());
        }
    }

    // ---------- (e) Delete only if status is Cancelled ----------
    public void deleteIfCancelled(int orderId) {
        String select = "SELECT status FROM Orders WHERE order_id = ?";
        String delete = "DELETE FROM Orders WHERE order_id = ? AND status = 'Cancelled'";
        try (Connection con = DBConnection.getConnection()) {

            try (PreparedStatement ps = con.prepareStatement(select)) {
                ps.setInt(1, orderId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        System.out.println("Order " + orderId + " does not exist.");
                        return;
                    }
                    String status = rs.getString("status");
                    if (!"Cancelled".equalsIgnoreCase(status)) {
                        System.out.println("Cannot delete order " + orderId
                                + ": status is '" + status + "', only Cancelled orders can be deleted.");
                        return;
                    }
                }
            }

            try (PreparedStatement ps = con.prepareStatement(delete)) {
                ps.setInt(1, orderId);
                if (ps.executeUpdate() > 0) {
                    System.out.println("Order " + orderId + " deleted successfully.");
                }
            }
        } catch (SQLException e) {
            System.out.println("Delete failed: " + e.getMessage());
        }
    }

    // ---------- helper: show all ----------
    public void showAll() {
        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM Orders ORDER BY order_id")) {
            printHeader();
            while (rs.next()) printRow(rs);
        } catch (SQLException e) {
            System.out.println("Query failed: " + e.getMessage());
        }
    }

    private void printHeader() {
        System.out.printf("%-8s %-18s %-18s %-5s %-12s %-10s%n",
                "ID", "Customer", "Product", "Qty", "Unit Price", "Status");
        System.out.println("-".repeat(78));
    }

    private void printRow(ResultSet rs) throws SQLException {
        System.out.printf("%-8d %-18s %-18s %-5d %-12.2f %-10s%n",
                rs.getInt("order_id"), rs.getString("customer_name"), rs.getString("product_name"),
                rs.getInt("quantity"), rs.getDouble("unit_price"), rs.getString("status"));
    }
}
