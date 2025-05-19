package chat.auth;

import chat.controller.DatabaseConnection;

import java.sql.*;

public class AccountManager {

    public AccountManager() {
        initDatabase();
    }


    private void initDatabase() {
        String createExtensionSQL = "CREATE EXTENSION IF NOT EXISTS pgcrypto;";
        String createUserTableSQL = "CREATE TABLE IF NOT EXISTS users (" +
                "username VARCHAR(50) PRIMARY KEY, " +
                "password_hash VARCHAR(100) NOT NULL);";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            System.out.println("AccountManager: Attempting to enable pgcrypto extension..."); //logging
            stmt.execute(createExtensionSQL); //execution
            System.out.println("AccountManager: pgcrypto extension enabled or already exists.");

            System.out.println("AccountManager: Attempting to create users table..."); //logging
            stmt.execute(createUserTableSQL); //execution
            System.out.println("AccountManager: Users table created or already exists.");
        } catch (SQLException e) {
            System.err.println("Error initializing database for AccountManager: " + e.getMessage());
        }
    }

    public boolean addAccount(String username, String password) {
        if (username == null || username.trim().isEmpty() || password == null || password.isEmpty()) {
            System.err.println("Username or password cannot be empty.");
            return false;
        }
        String sql = "INSERT INTO users (username, password_hash) VALUES (?, crypt(?, gen_salt('bf', 8))) ON CONFLICT (username) DO NOTHING;";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Error adding account for user '" + username + "': " + e.getMessage());
            return false;
        }
    }

    public boolean validateCredentials(String username, String password) {
        if (username == null || password == null) {
            return false;
        }
        String sql = "SELECT (password_hash) = crypt(?, password_hash) AS password_matches "
                + "FROM users WHERE username = ?;";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, password);
            pstmt.setString(2, username);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getBoolean("password_matches");
            }
            return false;
        } catch (SQLException e) {
            System.err.println("Error validating credentials for user '" + username + "': " + e.getMessage());
            return false;
        }
    }

    public boolean accountExists(String username) {
        if (username == null) {
            return false;
        }
        String sql = "SELECT 1 FROM users WHERE username = ?;";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            return rs.next(); // True if a row is found

        } catch (SQLException e) {
            System.err.println("Error checking if account exists for user '" + username + "': " + e.getMessage());
            return false;
        }
    }
}