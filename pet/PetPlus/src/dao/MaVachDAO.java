package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import database.ConnectDB;

public class MaVachDAO {

    // Dữ liệu sản phẩm sau khi quét mã vạch
    public static class SanPhamQuet {
        private final String maSP;
        private final String tenSP;
        private final double giaBan;
        private final int soLuongTon;

        public SanPhamQuet(String maSP, String tenSP, double giaBan, int soLuongTon) {
            this.maSP = maSP;
            this.tenSP = tenSP;
            this.giaBan = giaBan;
            this.soLuongTon = soLuongTon;
        }
        public String getMaSP() { return maSP; }
        public String getTenSP() { return tenSP; }
        public double getGiaBan() { return giaBan; }
        public int getSoLuongTon() { return soLuongTon; }
    }

    // Tìm sản phẩm theo mã vạch, giữ nguyên hàm dùng cho webcam
    public SanPhamQuet timSanPhamTheoMaVach(Connection conn, String maVach) throws SQLException {
        if (maVach == null || maVach.trim().isEmpty()) return null;
        String sql = "SELECT sp.maSP, sp.tenSP, sp.giaBan, sp.soLuongTon "
                + "FROM dbo.MaVach mv INNER JOIN dbo.SanPham sp ON mv.maSP = sp.maSP "
                + "WHERE mv.maVach = ? AND sp.trangThai <> N'Đã xóa'";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maVach.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new SanPhamQuet(rs.getString("maSP"), rs.getString("tenSP"),
                            rs.getDouble("giaBan"), rs.getInt("soLuongTon"));
                }
            }
        }
        return null;
    }

    // Lấy mã vạch đã lưu theo mã sản phẩm
    public String layMaVachTheoSanPham(String maSP) throws SQLException {
        String sql = "SELECT TOP (1) maVach FROM dbo.MaVach WHERE maSP = ? ORDER BY maVach";
        try (Connection conn = ConnectDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maSP);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getString("maVach") : null;
            }
        }
    }

    // Chỉ tạo mã mới khi sản phẩm chưa có mã; dùng mã nội bộ Code 128
    public String taoMaVachNeuChuaCo(String maSP) throws SQLException {
        if (maSP == null || maSP.trim().isEmpty())
            throw new IllegalArgumentException("Chưa chọn sản phẩm");
        String ma = "PP-" + maSP.trim();
        if (ma.length() > 50) throw new IllegalArgumentException("Mã vạch vượt quá 50 ký tự");
        try (Connection conn = ConnectDB.getConnection()) {
            boolean autoCommit = conn.getAutoCommit();
            int isolation = conn.getTransactionIsolation();
            try {
                conn.setAutoCommit(false);
                conn.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
                String sqlSP = "SELECT maSP FROM dbo.SanPham WITH (UPDLOCK, HOLDLOCK) WHERE maSP = ? AND trangThai <> N'Đã xóa'";
                try (PreparedStatement ps = conn.prepareStatement(sqlSP)) {
                    ps.setString(1, maSP.trim());
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) throw new SQLException("Sản phẩm không tồn tại: " + maSP);
                    }
                }
                String sqlDaCo = "SELECT TOP (1) maVach FROM dbo.MaVach WITH (UPDLOCK, HOLDLOCK) WHERE maSP = ? ORDER BY maVach";
                try (PreparedStatement ps = conn.prepareStatement(sqlDaCo)) {
                    ps.setString(1, maSP.trim());
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            String existing = rs.getString("maVach");
                            conn.commit();
                            return existing;
                        }
                    }
                }
                String sqlTrung = "SELECT maSP FROM dbo.MaVach WITH (UPDLOCK, HOLDLOCK) WHERE maVach = ?";
                try (PreparedStatement ps = conn.prepareStatement(sqlTrung)) {
                    ps.setString(1, ma);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) throw new SQLException("Mã vạch đã được dùng: " + ma);
                    }
                }
                String sqlThem = "INSERT INTO dbo.MaVach (maVach, maSP) VALUES (?, ?)";
                try (PreparedStatement ps = conn.prepareStatement(sqlThem)) {
                    ps.setString(1, ma);
                    ps.setString(2, maSP.trim());
                    ps.executeUpdate();
                }
                conn.commit();
                return ma;
            } catch (SQLException | RuntimeException ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setTransactionIsolation(isolation);
                conn.setAutoCommit(autoCommit);
            }
        }
    }

    /** Gán mã vạch đã quét cho sản phẩm; thay mã hiện có nếu sản phẩm đã có mã. */
    public SanPhamQuet ganMaVachChoSanPham(String maVach, String maSP) throws SQLException {
        if (maVach == null || maVach.trim().isEmpty())
            throw new IllegalArgumentException("Mã vạch không được để trống.");
        if (maVach.trim().length() > 50)
            throw new IllegalArgumentException("Mã vạch vượt quá 50 ký tự.");
        if (maSP == null || maSP.trim().isEmpty())
            throw new IllegalArgumentException("Chưa chọn sản phẩm.");

        String maVachSach = maVach.trim();
        String maSPSach = maSP.trim();
        try (Connection conn = ConnectDB.getConnection()) {
            boolean autoCommit = conn.getAutoCommit();
            int isolation = conn.getTransactionIsolation();
            try {
                conn.setAutoCommit(false);
                conn.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);

                try (PreparedStatement ps = conn.prepareStatement(
                        "SELECT maSP FROM dbo.SanPham WITH (UPDLOCK, HOLDLOCK) WHERE maSP = ? AND trangThai <> N'Đã xóa'")) {
                    ps.setString(1, maSPSach);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) throw new SQLException("Sản phẩm không tồn tại: " + maSPSach);
                    }
                }

                try (PreparedStatement ps = conn.prepareStatement(
                        "SELECT maSP FROM dbo.MaVach WITH (UPDLOCK, HOLDLOCK) WHERE maVach = ?")) {
                    ps.setString(1, maVachSach);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next() && !maSPSach.equals(rs.getString("maSP"))) {
                            throw new SQLException("Mã vạch " + maVachSach + " đã được gán cho sản phẩm "
                                    + rs.getString("maSP") + ".");
                        }
                    }
                }

                String maVachCu = null;
                try (PreparedStatement ps = conn.prepareStatement(
                        "SELECT maVach FROM dbo.MaVach WITH (UPDLOCK, HOLDLOCK) WHERE maSP = ?")) {
                    ps.setString(1, maSPSach);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) maVachCu = rs.getString("maVach");
                    }
                }

                if (maVachCu == null) {
                    try (PreparedStatement ps = conn.prepareStatement(
                            "INSERT INTO dbo.MaVach (maVach, maSP) VALUES (?, ?)")) {
                        ps.setString(1, maVachSach);
                        ps.setString(2, maSPSach);
                        ps.executeUpdate();
                    }
                } else if (!maVachSach.equals(maVachCu)) {
                    try (PreparedStatement ps = conn.prepareStatement(
                            "UPDATE dbo.MaVach SET maVach = ? WHERE maSP = ?")) {
                        ps.setString(1, maVachSach);
                        ps.setString(2, maSPSach);
                        ps.executeUpdate();
                    }
                }

                SanPhamQuet sanPham;
                try (PreparedStatement ps = conn.prepareStatement(
                        "SELECT maSP, tenSP, giaBan, soLuongTon FROM dbo.SanPham WHERE maSP = ? AND trangThai <> N'Đã xóa'")) {
                    ps.setString(1, maSPSach);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) throw new SQLException("Không tải lại được sản phẩm " + maSPSach + ".");
                        sanPham = new SanPhamQuet(rs.getString("maSP"), rs.getString("tenSP"),
                                rs.getDouble("giaBan"), rs.getInt("soLuongTon"));
                    }
                }
                conn.commit();
                return sanPham;
            } catch (SQLException | RuntimeException ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setTransactionIsolation(isolation);
                conn.setAutoCommit(autoCommit);
            }
        }
    }
}
