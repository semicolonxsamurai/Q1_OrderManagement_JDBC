package com.ecommerce.oms;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        // Part (a): verify the connection
        try (Connection con = DBConnection.getConnection()) {
            System.out.println("Connected to MySQL: " + con.getCatalog());
        } catch (SQLException e) {
            System.out.println("Could not connect to database: " + e.getMessage());
            return;
        }

        OrderDAO dao = new OrderDAO();
        Scanner sc = new Scanner(System.in);

        while (true) {
            System.out.println("\n===== ORDER MANAGEMENT SYSTEM =====");
            System.out.println("1. Insert sample orders (3+ records)");
            System.out.println("2. Show orders by status (quantity > 2, price DESC)");
            System.out.println("3. Increase order quantity");
            System.out.println("4. Update order status");
            System.out.println("5. Show total order value");
            System.out.println("6. Delete a Cancelled order");
            System.out.println("7. Show all orders");
            System.out.println("0. Exit");
            System.out.print("Choice: ");

            int choice = readInt(sc);
            switch (choice) {
                case 1 -> dao.insertSampleOrders();
                case 2 -> {
                    System.out.print("Enter status: ");
                    dao.showByStatus(sc.nextLine().trim());
                }
                case 3 -> {
                    System.out.print("Order ID: ");
                    int id = readInt(sc);
                    System.out.print("Additional quantity: ");
                    int extra = readInt(sc);
                    dao.increaseQuantity(id, extra);
                }
                case 4 -> {
                    System.out.print("Order ID: ");
                    int id = readInt(sc);
                    System.out.print("New status (Pending/Shipped/Delivered/Cancelled): ");
                    dao.updateStatus(id, sc.nextLine());
                }
                case 5 -> dao.showTotalOrderValue();
                case 6 -> {
                    System.out.print("Order ID to delete: ");
                    dao.deleteIfCancelled(readInt(sc));
                }
                case 7 -> dao.showAll();
                case 0 -> {
                    System.out.println("Goodbye!");
                    return;
                }
                default -> System.out.println("Invalid choice.");
            }
        }
    }

    private static int readInt(Scanner sc) {
        while (true) {
            try {
                return Integer.parseInt(sc.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.print("Enter a valid number: ");
            }
        }
    }
}
