package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class ConnectDB {
    private static final String URL = "jdbc:sqlserver://localhost:1433;"
            + "databaseName=PETPLUS;"
            + "encrypt=true;"
            + "trustServerCertificate=true;";

    private static final String USERNAME = "sa";
<<<<<<< HEAD
    private static final String PASSWORD_ENV = "PETPLUS_DB_PASSWORD";
=======
    private static final String PASSWORD = "sapassword";
>>>>>>> 95e50a45891aa5ed4bac26dd4eec8ba51e1cb9d5

    private ConnectDB() {
    }

    public static Connection getConnection() throws SQLException {
<<<<<<< HEAD
        String password = System.getenv(PASSWORD_ENV);
        if (password == null || password.trim().isEmpty()) {
            throw new SQLException("Chua cau hinh bien moi truong " + PASSWORD_ENV + ".");
        }

        return DriverManager.getConnection(
                URL,
                USERNAME,
                password);
=======
        return DriverManager.getConnection(
                URL,
                USERNAME,
                PASSWORD);
>>>>>>> 95e50a45891aa5ed4bac26dd4eec8ba51e1cb9d5
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
<<<<<<< HEAD
}
=======
}
>>>>>>> 95e50a45891aa5ed4bac26dd4eec8ba51e1cb9d5
