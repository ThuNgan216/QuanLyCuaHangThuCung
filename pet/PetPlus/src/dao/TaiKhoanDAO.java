package dao;

import database.ConnectDB;
import entity.NhanVien;
import entity.TaiKhoan;

import java.sql.*;

public class TaiKhoanDAO {
    /** Giữ API đăng nhập cũ để các màn hình hiện hữu vẫn hoạt động. */
    public NhanVien dangNhap(String tenDangNhap, String matKhau) throws SQLException {
        String sql = "SELECT nv.maNV,nv.tenNV,nv.sdt,nv.cccd,nv.diaChi,nv.email,nv.chucVu," +
                     "tk.maTK,tk.tenDangNhap,tk.matKhau,tk.vaiTro,tk.trangThai " +
                     "FROM TaiKhoan tk INNER JOIN NhanVien nv ON nv.maTK=tk.maTK " +
                     "WHERE tk.tenDangNhap=? AND tk.matKhau=? AND tk.trangThai=N'Hoạt động'";
        try (Connection conn = ConnectDB.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tenDangNhap);
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

    /** Đổi mật khẩu của tài khoản đang đăng nhập sau khi xác minh mật khẩu hiện tại. */
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
    /** Sửa tài khoản. matKhauMoi null => giữ nguyên; tuyệt đối không tải mật khẩu vào form. */
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
            if (doiMatKhau) ps.setString(i++, matKhauMoi);
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
        if (!"QUAN_LY".equals(vaiTro) && !"BAN_HANG".equals(vaiTro)
            && !"KHO".equals(vaiTro) && !"CHAM_SOC".equals(vaiTro))
            throw new IllegalArgumentException("Mã vai trò không hợp lệ.");
        String sql = "UPDATE TaiKhoan SET vaiTro=? WHERE maTK=? AND tenDangNhap<>N'admin'";
        try (Connection conn = ConnectDB.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, vaiTro);
            ps.setString(2, maTK);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Xóa vĩnh viễn một TÀI KHOẢN CHƯA ĐƯỢC GÁN NHÂN VIÊN.
     * Không xóa/cascade nhân viên. Với giao diện hiện tại, các tài khoản đều có
     * nhân viên nên lệnh sẽ trả false, tránh xóa hồ sơ và hóa đơn liên quan.
     */
    public boolean xoaTaiKhoanNeuChuaGanNhanVien(String maTK) throws SQLException {
        String sql = "DELETE FROM TaiKhoan WHERE maTK=? AND tenDangNhap<>N'admin' " +
                     "AND NOT EXISTS (SELECT 1 FROM NhanVien WHERE maTK=?)";
        try (Connection conn = ConnectDB.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maTK);
            ps.setString(2, maTK);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Xóa tài khoản KÈM hồ sơ nhân viên (do nv.maTK là khóa ngoại).
     * CHỈ gọi sau khi người dùng xác nhận 2 lần. Nếu DB có FK NO ACTION tới
     * NhanVien, SQL Server báo lỗi và transaction sẽ rollback an toàn.
     * Cần kiểm tra quy tắc ON DELETE CASCADE của các bảng nghiệp vụ trước khi bật ở production.
     */
    
public boolean xoa(String maNV) throws SQLException {

    String sql = "UPDATE TaiKhoan "
        + "SET trangThai = N'Ngừng hoạt động' "
        + "WHERE maTK = ("
        + "SELECT maTK FROM NhanVien WHERE maNV = ?"
        + ") "
        + "AND tenDangNhap <> N'admin' "
        + "AND trangThai <> N'Ngừng hoạt động'";

    try (Connection conn = ConnectDB.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {

        ps.setString(1, maNV);

        return ps.executeUpdate() > 0;
    }
}

}