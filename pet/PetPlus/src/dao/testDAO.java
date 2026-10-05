package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import database.ConnectDB;

public class testDAO {

    public static void main(java.lang.String[] args) {

        java.lang.String sql = "SELECT maTK, tenDangNhap, vaiTro, trangThai "
                + "FROM TaiKhoan";

        try (
            Connection connection = ConnectDB.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql);
            ResultSet resultSet = statement.executeQuery()
        ) {

            while (resultSet.next()) {
                System.out.println(
                    resultSet.getString("maTK")
                    + " | "
                    + resultSet.getString("tenDangNhap")
                    + " | "
                    + resultSet.getString("vaiTro")
                    + " | "
                    + resultSet.getBoolean("trangThai")
                );
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}