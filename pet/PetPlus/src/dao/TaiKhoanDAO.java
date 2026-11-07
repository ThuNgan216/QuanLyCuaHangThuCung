package dao;

import database.ConnectDB;
import entity.NhanVien;
import entity.TaiKhoan;

import java.sql.*;
import java.util.LinkedHashMap;
import java.util.Map;

public class TaiKhoanDAO {
    public Map<String, String> layDanhSachVaiTro()
            throws SQLException {

        Map<String, String> ds = new LinkedHashMap<>();

        ds.put("BAN_HANG", "Bán hàng / Thu ngân");
        ds.put("KHO", "Kho");
        ds.put("CHAM_SOC", "Chăm sóc thú cưng");
        ds.put("QUAN_LY", "Quản lý");

        String sql = "SELECT DISTINCT tk.vaiTro, nv.chucVu " +
                "FROM TaiKhoan tk " +
                "JOIN NhanVien nv ON tk.maTK = nv.maTK " +
                "WHERE tk.tenDangNhap <> N'admin' " +
                "AND nv.chucVu IS NOT NULL";

        try (Connection con = ConnectDB.getConnection();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                ds.put(
                        rs.getString("vaiTro"),
                        rs.getString("chucVu"));
            }
        }

        return ds;
    }

    public NhanVien dangNhap(String maNV, String matKhau) throws SQLException {
        String sql = "SELECT nv.maNV,nv.tenNV,nv.sdt,nv.cccd,nv.diaChi,nv.email,nv.chucVu," +
                "tk.maTK,tk.tenDangNhap,tk.matKhau,tk.vaiTro,tk.trangThai " +
                "FROM TaiKhoan tk INNER JOIN NhanVien nv ON nv.maTK=tk.maTK " +
                "WHERE nv.maNV=? AND tk.matKhau=? AND tk.trangThai=N'Hoạt động'";
        try (Connection conn = ConnectDB.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maNV);
            ps.setString(2, matKhau);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    TaiKhoan tk = new TaiKhoan(rs.getString("maTK"), rs.getString("tenDangNhap"),
                            rs.getString("matKhau"), rs.getString("vaiTro"), rs.getString("trangThai"));
                    return new NhanVien(rs.getString("maNV"), rs.getString("tenNV"), rs.getString("sdt"),
                            rs.getString("cccd"), rs.getString("diaChi"), rs.getString("email"),
                            rs.getString("chucVu"), tk);
                }
            }
        }
        return null;
    }

    public boolean doiMatKhau(String maTK, String matKhauCu, String matKhauMoi) throws SQLException {
        if (maTK == null || matKhauCu == null || matKhauMoi == null)
            throw new IllegalArgumentException("Thông tin đổi mật khẩu không hợp lệ.");

        String sql = "UPDATE TaiKhoan SET matKhau=? " +
                "WHERE maTK=? AND matKhau=? AND trangThai=N'Hoạt động'";
        try (Connection conn = ConnectDB.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, matKhauMoi);
            ps.setString(2, maTK);
            ps.setString(3, matKhauCu);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean capNhatTaiKhoan(String maTK, String tenDangNhap, String matKhauMoi,
            String vaiTro, String trangThai) throws SQLException {
        if ("admin".equalsIgnoreCase(tenDangNhap))
            throw new IllegalArgumentException("Tên đăng nhập admin được dành riêng.");
        boolean doiMatKhau = matKhauMoi != null && !matKhauMoi.isEmpty();
        String sql = "UPDATE TaiKhoan SET tenDangNhap=?,vaiTro=?,trangThai=?" +
                (doiMatKhau ? ",matKhau=? " : " ") + "WHERE maTK=? AND tenDangNhap<>N'admin'";
        try (Connection conn = ConnectDB.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tenDangNhap);
            ps.setString(2, vaiTro);
            ps.setString(3, trangThai);
            int i = 4;
            if (doiMatKhau)
                ps.setString(i++, matKhauMoi);
            ps.setString(i, maTK);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean doiTrangThai(String maTK, String trangThai) throws SQLException {
        if (!"Hoạt động".equals(trangThai) && !"Ngừng hoạt động".equals(trangThai))
            throw new IllegalArgumentException("Trạng thái không hợp lệ.");
        String sql = "UPDATE TaiKhoan SET trangThai=? WHERE maTK=? AND tenDangNhap<>N'admin'";
        try (Connection conn = ConnectDB.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, trangThai);
            ps.setString(2, maTK);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean doiVaiTro(String maTK, String vaiTro) throws SQLException {
        if (vaiTro == null
                || !vaiTro.matches("[A-Z][A-Z0-9_]{2,49}")
                || "ADMIN".equals(vaiTro)) {
            throw new IllegalArgumentException("Mã vai trò không hợp lệ.");
        }
        String sql = "UPDATE TaiKhoan SET vaiTro=? WHERE maTK=? AND tenDangNhap<>N'admin'";
        try (Connection conn = ConnectDB.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, vaiTro);
            ps.setString(2, maTK);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean xoaTaiKhoanNeuChuaGanNhanVien(String maTK) throws SQLException {
        String sql = "DELETE FROM TaiKhoan WHERE maTK=? AND tenDangNhap<>N'admin' " +
                "AND NOT EXISTS (SELECT 1 FROM NhanVien WHERE maTK=?)";
        try (Connection conn = ConnectDB.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maTK);
            ps.setString(2, maTK);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean xoa(String maTK) throws SQLException {
        try (Connection conn = ConnectDB.getConnection()) {
            boolean autoCommitCu = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try {
                if (!tonTai(conn,
                        "SELECT 1 FROM TaiKhoan WITH (XLOCK, HOLDLOCK) WHERE maTK = ? AND tenDangNhap <> N'admin'",
                        maTK)) {
                    conn.commit();
                    return false;
                }

                String maNV = null;
                try (PreparedStatement ps = conn.prepareStatement(
                        "SELECT maNV FROM NhanVien WITH (XLOCK, HOLDLOCK) WHERE maTK = ?")) {
                    ps.setString(1, maTK);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next())
                            maNV = rs.getString("maNV");
                    }
                }

                boolean coLichSu = maNV != null && (tonTai(conn, "SELECT 1 FROM LichLamViec WHERE maNV = ?", maNV)
                        || tonTai(conn, "SELECT 1 FROM PhieuNhap WHERE maNV = ?", maNV)
                        || tonTai(conn, "SELECT 1 FROM LichHen WHERE maNV = ?", maNV)
                        || tonTai(conn, "SELECT 1 FROM HoaDon WHERE maNV = ?", maNV)
                        || tonTai(conn, "SELECT 1 FROM HoaDon WHERE maNVTiepNhan = ?", maNV));

                if (coLichSu) {
                    try (PreparedStatement ps = conn.prepareStatement(
                            "UPDATE TaiKhoan SET trangThai = N'Ngừng hoạt động' WHERE maTK = ? AND tenDangNhap <> N'admin'")) {
                        ps.setString(1, maTK);
                        ps.executeUpdate();
                    }
                } else {
                    if (maNV != null) {
                        try (PreparedStatement ps = conn.prepareStatement(
                                "DELETE FROM NhanVien WHERE maTK = ?")) {
                            ps.setString(1, maTK);
                            ps.executeUpdate();
                        }
                    }
                    try (PreparedStatement ps = conn.prepareStatement(
                            "DELETE FROM TaiKhoan WHERE maTK = ? AND tenDangNhap <> N'admin'")) {
                        ps.setString(1, maTK);
                        ps.executeUpdate();
                    }
                }

                conn.commit();
                return true;
            } catch (SQLException | RuntimeException ex) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackError) {
                    ex.addSuppressed(rollbackError);
                }
                throw ex;
            } finally {
                conn.setAutoCommit(autoCommitCu);
            }
        }
    }

    public boolean xoaNhanVien(String maNV) throws SQLException {
        String sql = "SELECT maTK FROM NhanVien WHERE maNV = ?";
        String maTK = null;
        try (Connection conn = ConnectDB.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maNV);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next())
                    maTK = rs.getString("maTK");
            }
        }
        return maTK != null && xoa(maTK);
    }

    private boolean tonTai(Connection conn, String sql, String ma) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, ma);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

}
