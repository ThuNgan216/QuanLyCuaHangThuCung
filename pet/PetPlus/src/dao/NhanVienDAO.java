package dao;

import database.ConnectDB;
import entity.NhanVien;
import entity.TaiKhoan;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class NhanVienDAO {

    // 1. Lấy danh sách nhân viên, không hiển thị tài khoản admin
    public List<NhanVien> layDanhSach() throws SQLException {
        List<NhanVien> danhSach = new ArrayList<>();

        String sql = "SELECT nv.maNV, nv.tenNV, nv.sdt, nv.cccd, " +
                "nv.diaChi, nv.email, nv.chucVu, " +
                "tk.maTK, tk.tenDangNhap, tk.matKhau, " +
                "tk.vaiTro, tk.trangThai " +
                "FROM NhanVien nv " +
                "INNER JOIN TaiKhoan tk ON nv.maTK = tk.maTK " +
                "WHERE tk.tenDangNhap <> N'admin' " +
                "ORDER BY nv.maNV";

        try (Connection conn = ConnectDB.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                danhSach.add(docNhanVien(rs));
            }
        }

        return danhSach;
    }

    // 2. Tìm kiếm nhân viên, loại trừ tài khoản admin
    public List<NhanVien> timKiem(String tuKhoa)
            throws SQLException {

        List<NhanVien> danhSach = new ArrayList<>();

        String sql = "SELECT nv.maNV, nv.tenNV, nv.sdt, nv.cccd, " +
                "nv.diaChi, nv.email, nv.chucVu, " +
                "tk.maTK, tk.tenDangNhap, tk.matKhau, " +
                "tk.vaiTro, tk.trangThai " +
                "FROM NhanVien nv " +
                "INNER JOIN TaiKhoan tk ON nv.maTK = tk.maTK " +
                "WHERE tk.tenDangNhap <> N'admin' " +
                "AND (nv.maNV LIKE ? " +
                "OR nv.tenNV LIKE ? " +
                "OR nv.sdt LIKE ?) " +
                "ORDER BY nv.maNV";

        try (Connection conn = ConnectDB.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            String keyword = "%" +
                    (tuKhoa == null ? "" : tuKhoa.trim()) + "%";

            ps.setString(1, keyword);
            ps.setString(2, keyword);
            ps.setString(3, keyword);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    danhSach.add(docNhanVien(rs));
                }
            }
        }

        return danhSach;
    }

    // 3. Chuyển dữ liệu SQL thành đối tượng NhanVien
    private NhanVien docNhanVien(ResultSet rs)
            throws SQLException {

        TaiKhoan tk = new TaiKhoan(
                rs.getString("maTK"),
                rs.getString("tenDangNhap"),
                rs.getString("matKhau"),
                rs.getString("vaiTro"),
                rs.getString("trangThai"));

        return new NhanVien(
                rs.getString("maNV"),
                rs.getString("tenNV"),
                rs.getString("sdt"),
                rs.getString("cccd"),
                rs.getString("diaChi"),
                rs.getString("email"),
                rs.getString("chucVu"),
                tk);
    }

    // 4. Thêm nhân viên và tài khoản trong cùng giao dịch
    public boolean them(NhanVien nv) throws SQLException {

        if (nv == null || nv.getMaTK() == null) {
            throw new IllegalArgumentException(
                    "Nhân viên và tài khoản không được null.");
        }

        TaiKhoan tk = nv.getMaTK();

        String sqlTK = "INSERT INTO TaiKhoan " +
                "(maTK, tenDangNhap, matKhau, vaiTro, trangThai) " +
                "VALUES (?, ?, ?, ?, ?)";

        String sqlNV = "INSERT INTO NhanVien " +
                "(maNV, tenNV, sdt, cccd, diaChi, email, chucVu, maTK) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = ConnectDB.getConnection()) {
            boolean autoCommitCu = conn.getAutoCommit();

            try {
                conn.setAutoCommit(false);

                // Thêm tài khoản
                try (PreparedStatement ps = conn.prepareStatement(sqlTK)) {

                    ps.setString(1, tk.getMaTK());
                    ps.setString(2, tk.getTenDangNhap());
                    ps.setString(3, tk.getMatKhau());
                    ps.setString(4, tk.getVaiTro());
                    ps.setString(5, tk.getTrangThai());

                    ps.executeUpdate();
                }

                // Thêm thông tin nhân viên
                try (PreparedStatement ps = conn.prepareStatement(sqlNV)) {

                    ps.setString(1, nv.getMaNV());
                    ps.setString(2, nv.getTenNV());
                    ps.setString(3, nv.getSdt());
                    ps.setString(4, nv.getCccd());
                    ps.setString(5, nv.getDiaChi());
                    ps.setString(6, nv.getEmail());
                    ps.setString(7, nv.getChucVu());
                    ps.setString(8, tk.getMaTK());

                    ps.executeUpdate();
                }

                conn.commit();
                return true;

            } catch (SQLException e) {
                conn.rollback();
                throw e;

            } finally {
                conn.setAutoCommit(autoCommitCu);
            }
        }
    }

    // 5. Cập nhật thông tin nhân viên và trạng thái tài khoản
    public boolean capNhat(NhanVien nv) throws SQLException {

        if (nv == null || nv.getMaTK() == null) {
            throw new IllegalArgumentException(
                    "Nhân viên và tài khoản không được null.");
        }

        String sqlNV = "UPDATE NhanVien SET " +
                "tenNV = ?, sdt = ?, cccd = ?, " +
                "diaChi = ?, email = ?, chucVu = ? " +
                "WHERE maNV = ?";

        String sqlTK = "UPDATE TaiKhoan SET trangThai = ? " +
                "WHERE maTK = ?";

        try (Connection conn = ConnectDB.getConnection()) {
            boolean autoCommitCu = conn.getAutoCommit();

            try {
                conn.setAutoCommit(false);

                int soDong;

                // Cập nhật thông tin nhân viên
                try (PreparedStatement ps = conn.prepareStatement(sqlNV)) {

                    ps.setString(1, nv.getTenNV());
                    ps.setString(2, nv.getSdt());
                    ps.setString(3, nv.getCccd());
                    ps.setString(4, nv.getDiaChi());
                    ps.setString(5, nv.getEmail());
                    ps.setString(6, nv.getChucVu());
                    ps.setString(7, nv.getMaNV());

                    soDong = ps.executeUpdate();
                }

                if (soDong == 0) {
                    conn.rollback();
                    return false;
                }

                // Cập nhật trạng thái tài khoản
                try (PreparedStatement ps = conn.prepareStatement(sqlTK)) {

                    ps.setString(
                            1, nv.getMaTK().getTrangThai());

                    ps.setString(
                            2, nv.getMaTK().getMaTK());

                    ps.executeUpdate();
                }

                conn.commit();
                return true;

            } catch (SQLException e) {
                conn.rollback();
                throw e;

            } finally {
                conn.setAutoCommit(autoCommitCu);
            }
        }
    }

    // 6. Ngừng hoạt động tài khoản, không xóa dữ liệu nhân viên
    public boolean xoa(String maNV) throws SQLException {

        String sql = "UPDATE TaiKhoan " +
                "SET trangThai = N'Ngừng hoạt động' " +
                "WHERE maTK = (" +
                "SELECT maTK FROM NhanVien WHERE maNV = ?) " +
                "AND tenDangNhap <> N'admin'";

        try (Connection conn = ConnectDB.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, maNV);

            return ps.executeUpdate() > 0;
        }
    }
}
