
package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import database.ConnectDB;
import entity.HoaDon;
import entity.KhachHang;

public class HoaDonDAO {

    /*
     * Tính tổng tiền hóa đơn:
     * Tổng gốc = SUM(soLuong * donGia)
     * Tổng tiền = Tổng gốc * (1 - phanTramGiam / 100)
     *
     * Các bảng sử dụng:
     * HoaDon
     * KhachHang
     * KhuyenMai
     * ChiTietHoaDon
     */

    private static final String JOIN_TONG_GOC = " LEFT JOIN ( " +
            "     SELECT maHoaDon, " +
            "            SUM(soLuong * donGia) AS TongGoc " +
            "     FROM ChiTietHoaDon " +
            "     GROUP BY maHoaDon " +
            " ) ct ON ct.maHoaDon = h.maHoaDon ";

    private static final String TONG_TIEN = "(ISNULL(ct.TongGoc, 0) * " +
            "(1 - ISNULL(km.phanTramGiam, 0) / 100.0))";

    /*
     * Khoảng giá:
     * 0: Dưới 500.000
     * 1: Từ 500.000 đến dưới 1.500.000
     * 2: Từ 1.500.000 đến dưới 3.000.000
     * 3: Từ 3.000.000 trở lên
     * -1: Không lọc giá
     */
    private static final double[] PRICE_MIN = {
            0,
            500000,
            1500000,
            3000000
    };

    private static final Double[] PRICE_MAX = {
            500000.0,
            1500000.0,
            3000000.0,
            null
    };

    /**
     * Tìm kiếm, lọc và sắp xếp hóa đơn.
     *
     * @param keyword     Tìm theo mã hóa đơn, tên khách hàng hoặc SĐT.
     *                    Null hoặc chuỗi rỗng nghĩa là không tìm theo từ khóa.
     * @param ngay        Ngày cần lọc; null nghĩa là bỏ qua.
     * @param thang       Tháng cần lọc; null nghĩa là bỏ qua.
     * @param nam         Năm cần lọc; null nghĩa là bỏ qua.
     * @param priceIdx    Chỉ số khoảng giá từ 0 đến 3;
     *                    -1 nghĩa là không lọc giá.
     * @param sort        1: Tổng tiền tăng dần.
     *                    -1: Tổng tiền giảm dần.
     *                    0: Ngày lập mới nhất trước.
     * @param outTongTien Map lưu mã hóa đơn và tổng tiền tương ứng.
     * @return Danh sách hóa đơn tìm được.
     * @throws SQLException Nếu xảy ra lỗi truy vấn SQL Server.
     */
    public List<HoaDon> timKiem(
            String keyword,
            Integer ngay,
            Integer thang,
            Integer nam,
            int priceIdx,
            int sort,
            Map<String, Double> outTongTien) throws SQLException {

        StringBuilder sql = new StringBuilder(
                "SELECT " +
                        "    h.maHoaDon AS MaHoaDon, " +
                        "    h.ngayLapHD AS NgayLapHD, " +
                        "    h.maKH AS MaKH, " +
                        "    k.tenKH AS TenKH, " +
                        "    k.sdt AS SoDienThoai, " +
                        TONG_TIEN + " AS TongTien " +
                        "FROM HoaDon h " +
                        "INNER JOIN KhachHang k " +
                        "    ON h.maKH = k.maKH " +
                        "LEFT JOIN KhuyenMai km " +
                        "    ON h.maKhuyenMai = km.maKhuyenMai " +
                        JOIN_TONG_GOC +
                        "WHERE 1 = 1 ");

        List<Object> params = new ArrayList<>();

        // Xóa kết quả cũ nếu sử dụng lại Map
        if (outTongTien != null) {
            outTongTien.clear();
        }

        // 1. Tìm kiếm theo mã hóa đơn, tên khách hàng hoặc số điện thoại
        String tuKhoa = keyword == null ? "" : keyword.trim();

        if (!tuKhoa.isEmpty()) {
            sql.append(
                    " AND ( " +
                            "     h.maHoaDon LIKE ? " +
                            "     OR k.tenKH LIKE ? " +
                            "     OR REPLACE(k.sdt, ' ', '') LIKE ? " +
                            " ) ");

            params.add("%" + tuKhoa + "%");
            params.add("%" + tuKhoa + "%");
            params.add("%" + tuKhoa.replace(" ", "") + "%");
        }

        // 2. Lọc theo ngày
        if (ngay != null) {
            sql.append(" AND DAY(h.ngayLapHD) = ? ");
            params.add(ngay);
        }

        // 3. Lọc theo tháng
        if (thang != null) {
            sql.append(" AND MONTH(h.ngayLapHD) = ? ");
            params.add(thang);
        }

        // 4. Lọc theo năm
        if (nam != null) {
            sql.append(" AND YEAR(h.ngayLapHD) = ? ");
            params.add(nam);
        }

        // 5. Lọc theo khoảng tổng tiền
        if (priceIdx >= 0 && priceIdx < PRICE_MIN.length) {

            sql.append(" AND ")
                    .append(TONG_TIEN)
                    .append(" >= ? ");

            params.add(PRICE_MIN[priceIdx]);

            if (PRICE_MAX[priceIdx] != null) {
                sql.append(" AND ")
                        .append(TONG_TIEN)
                        .append(" < ? ");

                params.add(PRICE_MAX[priceIdx]);
            }
        }

        // 6. Sắp xếp kết quả
        if (sort > 0) {
            sql.append(" ORDER BY TongTien ASC ");
        } else if (sort < 0) {
            sql.append(" ORDER BY TongTien DESC ");
        } else {
            sql.append(
                    " ORDER BY h.ngayLapHD DESC, h.maHoaDon ASC ");
        }

        List<HoaDon> ds = new ArrayList<>();

        // Lấy kết nối dùng chung, không đóng Connection tại DAO này
        Connection con = ConnectDB.getConnection();

        if (con == null) {
            throw new SQLException(
                    "Không lấy được kết nối SQL Server. " +
                            "Vui lòng kiểm tra ConnectDB và database PETPLUS.");
        }

        try (PreparedStatement ps = con.prepareStatement(sql.toString())) {

            // Gán các tham số truy vấn
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {

                    // Tạo hóa đơn
                    HoaDon hd = new HoaDon(
                            rs.getString("MaHoaDon"));

                    // Gán ngày lập hóa đơn
                    java.sql.Date ngayLap = rs.getDate("NgayLapHD");

                    if (ngayLap != null) {
                        hd.setNgayLapHD(
                                ngayLap.toLocalDate());
                    }

                    // Gán thông tin khách hàng vào hóa đơn
                    KhachHang kh = taoKhachHang(rs);
                    hd.setMaKH(kh);

                    // Thêm hóa đơn vào danh sách
                    ds.add(hd);

                    // Lưu tổng tiền theo mã hóa đơn
                    if (outTongTien != null) {
                        outTongTien.put(
                                hd.getMaHoaDon(),
                                rs.getDouble("TongTien"));
                    }
                }
            }
        }

        return ds;
    }

    /**
     * Tạo đối tượng KhachHang từ dữ liệu ResultSet.
     *
     * SQL đã đặt bí danh:
     * k.tenKH AS TenKH
     * k.sdt AS SoDienThoai
     */
    private KhachHang taoKhachHang(ResultSet rs)
            throws SQLException {

        KhachHang kh = new KhachHang();

        kh.setMaKH(rs.getString("MaKH"));
        kh.setTenKH(rs.getString("TenKH"));
        kh.setSdt(rs.getString("SoDienThoai"));

        return kh;
    }
}
