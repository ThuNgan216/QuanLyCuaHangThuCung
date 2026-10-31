package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class ConnectDB {
    private static final String URL = "jdbc:sqlserver://localhost:1433;"
            + "databaseName=PETPLUS;"
            + "encrypt=true;"
            + "trustServerCertificate=true;";

    private static final String USERNAME = System.getenv().getOrDefault("PETPLUS_DB_USERNAME", "sa");
    private static final String PASSWORD = System.getenv("PETPLUS_DB_PASSWORD");

    private ConnectDB() {
    }

    public static Connection getConnection() throws SQLException {
        if (PASSWORD == null || PASSWORD.isBlank()) {
            throw new SQLException("Missing PETPLUS_DB_PASSWORD environment variable.");
        }
        return DriverManager.getConnection(
                URL,
                USERNAME,
                PASSWORD);
    }

    public static void main(String[] args) {

        try (Connection connection = getConnection()) {

            System.out.println("================================");
            System.out.println("KET NOI THANH CONG");
            System.out.println("Database: " + connection.getCatalog());
            System.out.println("================================");

        } catch (SQLException e) {

            System.err.println("KET NOI THAT BAI");
            System.err.println("Message: " + e.getMessage());
            System.err.println("SQL State: " + e.getSQLState());
            System.err.println("Error Code: " + e.getErrorCode());

            e.printStackTrace();
        }
    }
}
