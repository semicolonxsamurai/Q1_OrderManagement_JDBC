package com.ecommerce.oms;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/** Part (a): establishes the connection with MySQL. Change USER / PASSWORD to match your setup. */
public class DBConnection {

    private static final String URL = "jdbc:mysql://localhost:3306/ecommerce_db";
    private static final String USER = "root";
    private static final String PASSWORD = "root";   // <-- change this

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
