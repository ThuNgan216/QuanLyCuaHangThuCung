package dao;

import database.ConnectDB;
import entity.NhanVien;
import entity.TaiKhoan;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class TaiKhoanDAO {

    public NhanVien dangNhap(String tenDangNhap, String matKhau)
            throws SQLException {

        String sql =
            "SELECT nv.maNV, nv.tenNV, nv.sdt, nv.cccd, " +
            "nv.diaChi, nv.email, nv.chucVu, " +
            "tk.maTK, tk.tenDangNhap, tk.matKhau, " +
            "tk.vaiTro, tk.trangThai " +
            "FROM TaiKhoan tk " +
            "INNER JOIN NhanVien nv ON nv.maTK = tk.maTK " +
            "WHERE tk.tenDangNhap = ? " +
            "AND tk.matKhau = ? " +
            "AND tk.trangThai = N'Hoạt động'";

        try (Connection conn = ConnectDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, tenDangNhap);
            ps.setString(2, matKhau);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {
                    TaiKhoan tk = new TaiKhoan(
                        rs.getString("maTK"),
                        rs.getString("tenDangNhap"),
                        rs.getString("matKhau"),
                        rs.getString("vaiTro"),
                        rs.getString("trangThai")
                    );

                    return new NhanVien(
                        rs.getString("maNV"),
                        rs.getString("tenNV"),
                        rs.getString("sdt"),
                        rs.getString("cccd"),
                        rs.getString("diaChi"),
                        rs.getString("email"),
                        rs.getString("chucVu"),
                        tk
                    );
                }
            }
        }

        return null;
    }
}