package dao;

import database.ConnectDB;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Báo cáo lợi nhuận gộp sản phẩm, ước tính theo giá nhập bình quân đến ngày bán. */
public class BaoCaoLoiNhuanDAO {

    public static class DongTheoNgay {
        public final LocalDate ngay;
        public final int soHoaDon;
        public final BigDecimal doanhThuDaCoGiaVon;
        public final BigDecimal giaVonUocTinh;
        public final BigDecimal loiNhuanGopUocTinh;

        public DongTheoNgay(LocalDate ngay, int soHoaDon, BigDecimal doanhThuDaCoGiaVon,
                BigDecimal giaVonUocTinh, BigDecimal loiNhuanGopUocTinh) {
            this.ngay = ngay;
            this.soHoaDon = soHoaDon;
            this.doanhThuDaCoGiaVon = doanhThuDaCoGiaVon;
            this.giaVonUocTinh = giaVonUocTinh;
            this.loiNhuanGopUocTinh = loiNhuanGopUocTinh;
        }
    }

    private static final String SQL_BAO_CAO =
            "SELECT CAST(hd.ngayLapHD AS DATE) AS ngay, "
            + "COUNT(DISTINCT hd.maHoaDon) AS soHoaDon, "
            + "SUM(CASE WHEN gv.donGiaVon IS NOT NULL THEN "
            + "    CAST(ct.soLuong AS DECIMAL(18,4)) * ct.donGia * giam.heSo "
            + "    ELSE CAST(0 AS DECIMAL(18,2)) END) AS doanhThuDaCoGiaVon, "
            + "SUM(CASE WHEN gv.donGiaVon IS NOT NULL THEN "
            + "    CAST(ct.soLuong AS DECIMAL(18,4)) * gv.donGiaVon "
            + "    ELSE CAST(0 AS DECIMAL(18,2)) END) AS giaVonUocTinh, "
            + "SUM(CASE WHEN gv.donGiaVon IS NOT NULL THEN "
            + "    CAST(ct.soLuong AS DECIMAL(18,4)) * (ct.donGia * giam.heSo - gv.donGiaVon) "
            + "    ELSE CAST(0 AS DECIMAL(18,2)) END) AS loiNhuanGopUocTinh "
            + "FROM dbo.HoaDon hd "
            + "JOIN dbo.ChiTietHoaDon ct ON ct.maHoaDon = hd.maHoaDon "
            + "LEFT JOIN dbo.KhuyenMai km ON km.maKhuyenMai = hd.maKhuyenMai "
            + "CROSS APPLY (SELECT CAST((100.0 - COALESCE(km.phanTramGiam, 0)) / 100.0 AS DECIMAL(9,6)) AS heSo) giam "
            + "OUTER APPLY ( "
            + "    SELECT CAST(SUM(CAST(ctpn.soLuongNhap AS DECIMAL(18,4)) * ctpn.donGiaNhap) "
            + "        / NULLIF(SUM(ctpn.soLuongNhap), 0) AS DECIMAL(18,4)) AS donGiaVon "
            + "    FROM dbo.ChiTietPhieuNhap ctpn "
            + "    JOIN dbo.PhieuNhap pn ON pn.maPN = ctpn.maPN "
            + "    WHERE ctpn.maSP = ct.maSP AND pn.ngayNhap <= hd.ngayLapHD "
            + ") gv "
            + "WHERE hd.maPTTT IS NOT NULL AND ct.maSP IS NOT NULL "
            + "  AND hd.ngayLapHD >= ? AND hd.ngayLapHD <= ? "
            + "GROUP BY CAST(hd.ngayLapHD AS DATE) "
            + "ORDER BY CAST(hd.ngayLapHD AS DATE) DESC";

    public List<DongTheoNgay> layBaoCao(LocalDate tuNgay, LocalDate denNgay) throws SQLException {
        List<DongTheoNgay> ketQua = new ArrayList<>();
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(SQL_BAO_CAO)) {
            ps.setDate(1, Date.valueOf(tuNgay));
            ps.setDate(2, Date.valueOf(denNgay));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Date ngay = rs.getDate("ngay");
                    ketQua.add(new DongTheoNgay(
                            ngay == null ? null : ngay.toLocalDate(),
                            rs.getInt("soHoaDon"),
                            giaTri(rs.getBigDecimal("doanhThuDaCoGiaVon")),
                            giaTri(rs.getBigDecimal("giaVonUocTinh")),
                            giaTri(rs.getBigDecimal("loiNhuanGopUocTinh"))));
                }
            }
        }
        return ketQua;
    }

    private static BigDecimal giaTri(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
