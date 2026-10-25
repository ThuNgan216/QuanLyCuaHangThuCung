package dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import database.ConnectDB;

/**
 * DAO phục vụ màn hình xuất hóa đơn.
 * Chỉnh tên bảng / tên cột trong 2 hằng SQL bên dưới nếu CSDL của bạn khác.
 */
public class XuatHoaDonDAO {

    /** Một dòng hóa đơn (đã JOIN sẵn tên NV, KH, PTTT, KM và tổng tiền). */
    public static class HoaDonRow {
        public String maHoaDon;
        public LocalDate ngayLap;
        public String tenNV;
        public String tenKH;
        public String tenPTTT;
        public String tenKhuyenMai;
        public BigDecimal phanTramGiam = BigDecimal.ZERO; // 0 - 100
        public BigDecimal tongTien = BigDecimal.ZERO; // trước giảm giá
        public BigDecimal tienKhachDua = BigDecimal.ZERO;

        public BigDecimal tienGiam() {
            return tongTien.multiply(phanTramGiam).divide(BigDecimal.valueOf(100), 0, java.math.RoundingMode.HALF_UP);
        }

        public BigDecimal thanhToan() {
            return tongTien.subtract(tienGiam());
        }

        public BigDecimal tienThua() {
            return tienKhachDua.subtract(thanhToan());
        }
    }

    /** Một dòng chi tiết hóa đơn (sản phẩm hoặc dịch vụ). */
    public static class ChiTietRow {
        public String loai;
        public String ten;
        public int soLuong;
        public BigDecimal donGia;

        public BigDecimal thanhTien() {
            return donGia.multiply(BigDecimal.valueOf(soLuong));
        }
    }

    private static final String SQL_HOADON = "SELECT hd.maHoaDon, hd.ngayLapHD, nv.tenNV, kh.tenKH, pt.tenPTTT, "
            + "       km.tenKhuyenMai, km.phanTramGiam, "
            + "       COALESCE(SUM(ct.soLuong * ct.donGia), 0) AS tongTien, "
            + "       COALESCE(MAX(ct.tienKhachDua), 0) AS tienKhachDua "
            + "FROM HoaDon hd "
            + "LEFT JOIN NhanVien nv ON hd.maNV = nv.maNV "
            + "LEFT JOIN KhachHang kh ON hd.maKH = kh.maKH "
            + "LEFT JOIN KhuyenMai km ON hd.maKhuyenMai = km.maKhuyenMai "
            + "LEFT JOIN PhuongThucThanhToan pt ON hd.maPTTT = pt.maPTTT "
            + "LEFT JOIN ChiTietHoaDon ct ON hd.maHoaDon = ct.maHoaDon "
            + "WHERE (hd.maHoaDon LIKE ? OR kh.tenKH LIKE ?) "
            + "  AND hd.maPTTT IS NOT NULL AND hd.ngayLapHD BETWEEN ? AND ? "
            + "GROUP BY hd.maHoaDon, hd.ngayLapHD, nv.tenNV, kh.tenKH, pt.tenPTTT, "
            + "         km.tenKhuyenMai, km.phanTramGiam "
            + "ORDER BY hd.ngayLapHD DESC, hd.maHoaDon DESC";

    private static final String SQL_CHITIET = "SELECT CASE WHEN ct.maSP IS NOT NULL THEN N'Sản phẩm' ELSE N'Dịch vụ' END AS loai, "
            + "       COALESCE(sp.tenSP, dv.tenDichVu) AS ten, ct.soLuong, ct.donGia "
            + "FROM ChiTietHoaDon ct "
            + "LEFT JOIN SanPham sp ON ct.maSP = sp.maSP "
            + "LEFT JOIN DichVu dv ON ct.maDichVu = dv.maDichVu "
            + "WHERE ct.maHoaDon = ?";

    /** Lấy tất cả hóa đơn. */
    public List<HoaDonRow> getAllHoaDon() {
        return timHoaDon("", LocalDate.of(2000, 1, 1), LocalDate.of(2999, 12, 31));
    }

    /**
     * Tìm theo mã hóa đơn hoặc tên khách hàng, trong khoảng ngày [tuNgay, denNgay].
     */
    public List<HoaDonRow> timHoaDon(String tuKhoa, LocalDate tuNgay, LocalDate denNgay) {
        List<HoaDonRow> ds = new ArrayList<>();
        String kw = "%" + (tuKhoa == null ? "" : tuKhoa.trim()) + "%";
        try {
            try (Connection con = ConnectDB.getConnection();
                    PreparedStatement ps = con.prepareStatement(SQL_HOADON)) {
                ps.setString(1, kw);
                ps.setString(2, kw);
                ps.setDate(3, Date.valueOf(tuNgay));
                ps.setDate(4, Date.valueOf(denNgay));
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        HoaDonRow r = new HoaDonRow();
                        r.maHoaDon = rs.getString("maHoaDon");
                        Date d = rs.getDate("ngayLapHD");
                        r.ngayLap = d == null ? null : d.toLocalDate();
                        r.tenNV = rs.getString("tenNV");
                        r.tenKH = rs.getString("tenKH");
                        r.tenPTTT = rs.getString("tenPTTT");
                        r.tenKhuyenMai = rs.getString("tenKhuyenMai");
                        BigDecimal pg = rs.getBigDecimal("phanTramGiam");
                        r.phanTramGiam = pg == null ? BigDecimal.ZERO : pg;
                        r.tongTien = rs.getBigDecimal("tongTien");
                        r.tienKhachDua = rs.getBigDecimal("tienKhachDua");
                        ds.add(r);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ds;
    }

    /** Lấy chi tiết của một hóa đơn. */
    public List<ChiTietRow> getChiTiet(String maHoaDon) {
        List<ChiTietRow> ds = new ArrayList<>();
        try {
            try (Connection con = ConnectDB.getConnection();
                    PreparedStatement ps = con.prepareStatement(SQL_CHITIET)) {
                ps.setString(1, maHoaDon);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        ChiTietRow c = new ChiTietRow();
                        c.loai = rs.getString("loai");
                        c.ten = rs.getString("ten");
                        c.soLuong = rs.getInt("soLuong");
                        c.donGia = rs.getBigDecimal("donGia");
                        ds.add(c);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ds;
    }
}
