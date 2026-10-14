
package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import database.ConnectDB;

public class SanPhamDAO {

    // Lấy Top 5 sản phẩm bán chạy từ database
    public List<Object[]> layTop5SanPhamBanChay()
            throws SQLException {

        String sql =
                "SELECT TOP (5) " +
                "sp.maSP, sp.tenSP, " +
                "SUM(ct.soLuong) AS soLuongDaBan " +
                "FROM dbo.ChiTietHoaDon ct " +
                "INNER JOIN dbo.SanPham sp " +
                "ON ct.maSP = sp.maSP " +
                "GROUP BY sp.maSP, sp.tenSP " +
                "ORDER BY soLuongDaBan DESC, sp.maSP";

        List<Object[]> danhSach = new ArrayList<>();

        // Lấy dữ liệu và tự đóng kết nối
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                danhSach.add(new Object[] {
                        rs.getString("tenSP"),
                        rs.getInt("soLuongDaBan")
                });
            }
        }

        return danhSach;
    }
}
