package dao;

import database.ConnectDB;
import entity.KhachHang;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Truy vấn khách hàng trong SQL Server.
 * Mọi lỗi CSDL được ném ra dưới dạng IllegalStateException (không còn nuốt lỗi và trả
 * về danh sách rỗng / false), để giao diện báo đúng nguyên nhân cho người dùng.
 */
public class KhachHangDAO {

    private static final String COT = "maKH, tenKH, sdt, diaChi, hangThanhVien, diemTichLuy";

    private KhachHang doc(ResultSet rs) throws SQLException {
        KhachHang kh = new KhachHang();
        kh.setMaKH(rs.getString("maKH"));
        kh.setTenKH(rs.getString("tenKH"));
        kh.setSdt(rs.getString("sdt"));
        kh.setDiaChi(rs.getString("diaChi"));
        kh.setHangThanhVien(rs.getInt("hangThanhVien")); // NULL -> 0
        kh.setDiemTichLuy(rs.getInt("diemTichLuy"));
        return kh;
    }

    // 1. Lấy danh sách khách hàng
    public List<KhachHang> layDanhSach() {
        List<KhachHang> ds = new ArrayList<>();
        String sql = "SELECT " + COT + " FROM KhachHang WHERE trangThai <> N'Đã xóa' ORDER BY maKH";
        try (Connection con = ConnectDB.getConnection();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next())
                ds.add(doc(rs));
        } catch (SQLException e) {
            throw new IllegalStateException("Không thể tải danh sách khách hàng: " + e.getMessage(), e);
        }
        return ds;
    }

    // 2. Tìm khách hàng theo mã
    public KhachHang timTheoMa(String maKH) {
        String sql = "SELECT " + COT + " FROM KhachHang WHERE maKH = ? AND trangThai <> N'Đã xóa'";
        try (Connection con = ConnectDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maKH);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? doc(rs) : null;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Không thể đọc khách hàng: " + e.getMessage(), e);
        }
    }

    // 3. Tìm theo mã, tên hoặc số điện thoại
    public List<KhachHang> timKiem(String tuKhoa) {
        List<KhachHang> ds = new ArrayList<>();
        String sql = "SELECT " + COT + " FROM KhachHang WHERE trangThai <> N'Đã xóa' "
                + "AND (maKH LIKE ? OR tenKH LIKE ? OR sdt LIKE ?) ORDER BY maKH";
        try (Connection con = ConnectDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            String key = "%" + tuKhoa + "%";
            for (int i = 1; i <= 3; i++)
                ps.setString(i, key);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next())
                    ds.add(doc(rs));
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Không thể tìm khách hàng: " + e.getMessage(), e);
        }
        return ds;
    }

    /** Phát sinh mã KH theo số thứ tự lớn nhất hiện có (kể cả đã ẩn): KH001, KH002... */
    public String taoMaMoi() {
        try (Connection con = ConnectDB.getConnection()) {
            return taoMaMoi(con);
        } catch (SQLException e) {
            throw new IllegalStateException("Không thể phát sinh mã khách hàng: " + e.getMessage(), e);
        }
    }

    private String taoMaMoi(Connection con) throws SQLException {
        return TienIchDAO.sinhMa(con, "KhachHang", "maKH", "KH");
    }

    /** Kiểm tra số điện thoại đã thuộc khách hàng khác hay chưa. */
    public boolean tonTaiSDT(String sdt, String maKHBoQua) {
        String sql = maKHBoQua == null
                ? "SELECT 1 FROM KhachHang WHERE sdt = ? AND trangThai <> N'Đã xóa'"
                : "SELECT 1 FROM KhachHang WHERE sdt = ? AND maKH <> ? AND trangThai <> N'Đã xóa'";
        try (Connection con = ConnectDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, sdt);
            if (maKHBoQua != null)
                ps.setString(2, maKHBoQua);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Không thể kiểm tra số điện thoại khách hàng: " + e.getMessage(), e);
        }
    }

    // 4. Thêm khách hàng. Mã được sinh lại trong DAO nếu hai người thêm cùng lúc bị trùng mã.
    public boolean them(KhachHang kh) {
        String sql = "INSERT INTO KhachHang (maKH, tenKH, sdt, diaChi, hangThanhVien, diemTichLuy) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection con = ConnectDB.getConnection()) {
            for (int lanThu = 1; lanThu <= TienIchDAO.SO_LAN_THU_TOI_DA; lanThu++) {
                kh.setMaKH(taoMaMoi(con));
                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setString(1, kh.getMaKH());
                    ps.setString(2, kh.getTenKH());
                    ps.setString(3, kh.getSdt());
                    ps.setString(4, kh.getDiaChi());
                    ganHang(ps, 5, kh);
                    ps.setInt(6, kh.getDiemTichLuy());
                    return ps.executeUpdate() > 0;
                } catch (SQLException e) {
                    if (TienIchDAO.laTrungRangBuoc(e, "UQ_KhachHang_SDT"))
                        throw new IllegalArgumentException("Số điện thoại này đã được sử dụng cho khách hàng khác.", e);
                    if (!TienIchDAO.laTrungRangBuoc(e, "PK_KhachHang") || lanThu == TienIchDAO.SO_LAN_THU_TOI_DA)
                        throw e;
                }
            }
            return false;
        } catch (SQLException e) {
            throw new IllegalStateException("Không thể thêm khách hàng: " + e.getMessage(), e);
        }
    }

    // 5. Cập nhật khách hàng
    public boolean capNhat(KhachHang kh) {
        String sql = "UPDATE KhachHang SET tenKH = ?, sdt = ?, diaChi = ?, hangThanhVien = ?, diemTichLuy = ? "
                + "WHERE maKH = ? AND trangThai <> N'Đã xóa'";
        try (Connection con = ConnectDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, kh.getTenKH());
            ps.setString(2, kh.getSdt());
            ps.setString(3, kh.getDiaChi());
            ganHang(ps, 4, kh);
            ps.setInt(5, kh.getDiemTichLuy());
            ps.setString(6, kh.getMaKH());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            if (TienIchDAO.laTrungRangBuoc(e, "UQ_KhachHang_SDT"))
                throw new IllegalArgumentException("Số điện thoại này đã được sử dụng cho khách hàng khác.", e);
            throw new IllegalStateException("Không thể cập nhật khách hàng: " + e.getMessage(), e);
        }
    }

    private void ganHang(PreparedStatement ps, int index, KhachHang kh) throws SQLException {
        if (kh.getHangThanhVien() == 0)
            ps.setNull(index, Types.INTEGER);
        else
            ps.setInt(index, kh.getHangThanhVien());
    }

    /** Khách hàng còn thú cưng đang quản lý (chưa bị xóa/ẩn) hay không. */
    public boolean coThuCungDangQuanLy(String maKH) {
        String sql = "SELECT 1 FROM ThuCung WHERE maKH = ? AND trangThai <> N'Đã xóa'";
        try (Connection con = ConnectDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maKH);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Không thể kiểm tra thú cưng của khách hàng: " + e.getMessage(), e);
        }
    }

    /** Khách hàng còn lịch hẹn chưa kết thúc hay không (dùng chung câu truy vấn của LichHenDAO). */
    public boolean coLichHenChuaKetThuc(String maKH) {
        return new LichHenDAO().coLichHenChuaKetThuc("maKH", maKH);
    }

    // 6. Xóa hẳn khách hàng khỏi CSDL (không còn ẩn), giải phóng số điện thoại.
    // Xóa kèm dữ liệu phụ thuộc của khách: dịch vụ gắn lịch hẹn, lịch hẹn, thú cưng
    // đã ẩn. Nếu khách đã có hóa đơn thì từ chối để không mất lịch sử bán hàng.
    public boolean xoa(String maKH) {
        final String coHoaDon = "SELECT 1 FROM HoaDon WHERE maKH = ? "
                + "OR maLichHen IN (SELECT maLichHen FROM LichHen WHERE maKH = ?) "
                + "OR maThuCung IN (SELECT maThuCung FROM ThuCung WHERE maKH = ?) "
                + "UNION ALL SELECT 1 FROM ChiTietHoaDon WHERE maDichVu IN "
                + "(SELECT maDichVu FROM DichVu WHERE maLichHen IN "
                + "(SELECT maLichHen FROM LichHen WHERE maKH = ?))";
        final String[] sqlXoa = {
                "DELETE FROM DichVu WHERE maLichHen IN (SELECT maLichHen FROM LichHen WHERE maKH = ?)",
                "DELETE FROM LichHen WHERE maKH = ?",
                "DELETE FROM ThuCung WHERE maKH = ?"
        };

        try (Connection con = ConnectDB.getConnection()) {
            boolean autoCommitCu = con.getAutoCommit();
            con.setAutoCommit(false);
            try {
                // Khóa dòng khách hàng để không ai thêm dữ liệu mới trong lúc xóa.
                try (PreparedStatement ps = con.prepareStatement(
                        "SELECT 1 FROM KhachHang WITH (XLOCK, HOLDLOCK) WHERE maKH = ?")) {
                    ps.setString(1, maKH);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            con.commit();
                            return false;
                        }
                    }
                }

                try (PreparedStatement ps = con.prepareStatement(coHoaDon)) {
                    for (int i = 1; i <= 4; i++) {
                        ps.setString(i, maKH);
                    }
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            throw new IllegalArgumentException(
                                    "Khách hàng này đã có hóa đơn nên không thể xóa hẳn khỏi hệ thống "
                                            + "để tránh mất lịch sử bán hàng.");
                        }
                    }
                }

                for (String sql : sqlXoa) {
                    try (PreparedStatement ps = con.prepareStatement(sql)) {
                        ps.setString(1, maKH);
                        ps.executeUpdate();
                    }
                }

                boolean daXoa;
                try (PreparedStatement ps = con.prepareStatement(
                        "DELETE FROM KhachHang WHERE maKH = ?")) {
                    ps.setString(1, maKH);
                    daXoa = ps.executeUpdate() > 0;
                }
                con.commit();
                return daXoa;
            } catch (SQLException | RuntimeException ex) {
                try {
                    con.rollback();
                } catch (SQLException rollbackError) {
                    ex.addSuppressed(rollbackError);
                }
                throw ex;
            } finally {
                con.setAutoCommit(autoCommitCu);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Không thể xóa khách hàng: " + e.getMessage(), e);
        }
    }
}