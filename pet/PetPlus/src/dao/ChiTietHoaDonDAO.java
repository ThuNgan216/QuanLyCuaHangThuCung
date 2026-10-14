package dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import database.ConnectDB;

public class ChiTietHoaDonDAO {

    public static class Dong {
        public String tenDV;
        public int soLuong;
        public BigDecimal donGia;
    }

    public static class ChiTietView {
        public String maHD, tenNV, tenKH, sdt, diaChi, maKhuyenMai, tenPTTT;
        public LocalDate ngayLap;
        public BigDecimal phanTramGiam = BigDecimal.ZERO;
        public BigDecimal tienKhachDua = BigDecimal.ZERO;
        public List<Dong> dong = new ArrayList<>();
    }

    // ===== SQL: chỉ cần sửa tên bảng/cột ở đây nếu khác =====
    private static final String SQL_HOADON = "SELECT hd.maHoaDon, hd.ngayLapHD, hd.tienKhachDua, "
            + "       nv.tenNV, kh.tenKH, kh.soDienThoai, kh.diaChi, "
            + "       km.maKhuyenMai, km.phanTramGiam, pt.tenPTTT "
            + "FROM HoaDon hd "
            + "JOIN NhanVien nv ON hd.maNV = nv.maNV "
            + "LEFT JOIN KhachHang kh ON hd.maKH = kh.maKH "
            + "LEFT JOIN KhuyenMai km ON hd.maKhuyenMai = km.maKhuyenMai "
            + "LEFT JOIN PhuongThucThanhToan pt ON hd.maPTTT = pt.maPTTT "
            + "WHERE hd.maHoaDon = ?";

    private static final String SQL_CHITIET = "SELECT dv.tenDichVu, ct.soLuong, ct.donGia "
            + "FROM ChiTietHoaDon ct JOIN DichVu dv ON ct.maDichVu = dv.maDichVu "
            + "WHERE ct.maHoaDon = ?";

    /* Trả về null nếu không tìm thấy hóa đơn */
    public ChiTietView getChiTiet(String maHD) throws SQLException {
        Connection con = ConnectDB.getConnection();
        ChiTietView v = null;

        try (PreparedStatement ps = con.prepareStatement(SQL_HOADON)) {
            ps.setString(1, maHD);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next())
                    return null;
                v = new ChiTietView();
                v.maHD = rs.getString("maHoaDon");
                java.sql.Date d = rs.getDate("ngayLapHD");
                v.ngayLap = d == null ? null : d.toLocalDate();
                v.tenNV = rs.getString("tenNV");
                v.tenKH = rs.getString("tenKH");
                v.sdt = rs.getString("soDienThoai");
                v.diaChi = rs.getString("diaChi");
                v.maKhuyenMai = rs.getString("maKhuyenMai");
                v.tenPTTT = rs.getString("tenPTTT");
                BigDecimal pt = rs.getBigDecimal("phanTramGiam");
                if (pt != null)
                    v.phanTramGiam = pt;
                BigDecimal kd = rs.getBigDecimal("tienKhachDua");
                if (kd != null)
                    v.tienKhachDua = kd;
            }
        }
        try (PreparedStatement ps = con.prepareStatement(SQL_CHITIET)) {
            ps.setString(1, maHD);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Dong d = new Dong();
                    d.tenDV = rs.getString("tenDichVu");
                    d.soLuong = rs.getInt("soLuong");
                    d.donGia = rs.getBigDecimal("donGia");
                    v.dong.add(d);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return v;
    }
}