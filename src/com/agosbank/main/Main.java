package com.agosbank.main;

import com.agosbank.database.DBConnection;
import java.sql.Connection;

public class Main {
    public static void main(String[] args) {
        System.out.println("=== AgosBank System Starting ===");

        // Jar Check & Connection Test
        Connection conn = DBConnection.getConnection();

        if (conn != null) {
            System.out.println("✅ SUCCESS: Kayang-kaya na nating mag-save ng data!");
        } else {
            System.out.println("❌ ERROR: May problema sa connection. Check XAMPP or MySQL Driver.");
        }
    }
}