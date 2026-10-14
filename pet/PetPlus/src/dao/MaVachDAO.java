
package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class MaVachDAO {

    // Dữ liệu sản phẩm sau khi quét mã vạch
    public static class SanPhamQuet {

        private final String maSP;
        private final String tenSP;
        private final double giaBan;
        private final int soLuongTon;

        public SanPhamQuet(
                String maSP,
                String tenSP,
                double giaBan,
                int soLuongTon) {

            this.maSP = maSP;
            this.tenSP = tenSP;
            this.giaBan = giaBan;
            this.soLuongTon = soLuongTon;
        }

        public String getMaSP() {
            return maSP;
        }

        public String getTenSP() {
            return tenSP;
        }

        public double getGiaBan() {
            return giaBan;
        }

        public int getSoLuongTon() {
            return soLuongTon;
        }
    }

    // Tìm sản phẩm theo mã vạch
    public SanPhamQuet timSanPhamTheoMaVach(
            Connection conn,
            String maVach) throws SQLException {

        if (maVach == null || maVach.trim().isEmpty()) {
            return null;
        }

        String sql =
            "SELECT sp.maSP, sp.tenSP, " +
            "sp.giaBan, sp.soLuongTon " +
            "FROM dbo.MaVach mv " +
            "INNER JOIN dbo.SanPham sp " +
            "ON mv.maSP = sp.maSP " +
            "WHERE mv.maVach = ?";

        try (PreparedStatement ps =
                conn.prepareStatement(sql)) {

            ps.setString(1, maVach.trim());

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {
                    return new SanPhamQuet(
                        rs.getString("maSP"),
                        rs.getString("tenSP"),
                        rs.getDouble("giaBan"),
                        rs.getInt("soLuongTon")
                    );
                }
            }
        }

        return null;
    }
}
