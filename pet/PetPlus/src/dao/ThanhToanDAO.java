package dao;

import database.ConnectDB;

import java.sql.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.time.LocalDateTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ThanhToanDAO {

    /** Dữ liệu cần hiển thị và nạp nhanh vào hóa đơn từ phiếu tiếp nhận. */
    public static class PhieuChoThanhToanDTO {
        public final String maTiepNhan;
        public final LocalDateTime thoiGianTiepNhan;
        public final String maKH, tenKH, sdt, diaChi, maThuCung, tenThuCung;
        public final int hangThanhVien, diemTichLuy;
        public final String dichVuYeuCau;
        public final long phiDuKien;
        public final List<DichVuHoaDonDTO> danhSachDichVu = new ArrayList<>();

        public PhieuChoThanhToanDTO(String maTiepNhan, LocalDateTime thoiGianTiepNhan,
                String maKH, String tenKH, String sdt, String diaChi,
                int hangThanhVien, int diemTichLuy, String maThuCung, String tenThuCung,
                String dichVuYeuCau, long phiDuKien) {
            this.maTiepNhan = maTiepNhan;
            this.thoiGianTiepNhan = thoiGianTiepNhan;
            this.maKH = maKH;
            this.tenKH = tenKH;
            this.sdt = sdt;
            this.diaChi = diaChi;
            this.hangThanhVien = hangThanhVien;
            this.diemTichLuy = diemTichLuy;
            this.maThuCung = maThuCung;
            this.tenThuCung = tenThuCung;
            this.dichVuYeuCau = dichVuYeuCau;
            this.phiDuKien = phiDuKien;
        }

        public long tongPhiDichVu() {
            long tong = 0;
            for (DichVuHoaDonDTO dichVu : danhSachDichVu) tong = Math.addExact(tong, dichVu.donGia);
            return tong;
        }
    }

    public static class DichVuHoaDonDTO {
        public final String maDichVu, tenDichVu;
        public final long donGia;
        public DichVuHoaDonDTO(String maDichVu, String tenDichVu, long donGia) {
            this.maDichVu = maDichVu;
            this.tenDichVu = tenDichVu;
            this.donGia = donGia;
        }
    }

    public List<PhieuChoThanhToanDTO> layPhieuChoThanhToan() throws SQLException {
        String sql = "SELECT tn.maTiepNhan, tn.thoiGianTiepNhan, tn.maKH, kh.tenKH, kh.sdt, kh.diaChi, "
                + "kh.hangThanhVien, kh.diemTichLuy, tn.maThuCung, tc.tenThuCung, tn.dichVuYeuCau, tn.phiDuKien, "
                + "dv.maDichVu, dv.tenDichVu, dv.donGia AS giaDichVu "
                + "FROM TiepNhanThuCung tn "
                + "INNER JOIN KhachHang kh ON kh.maKH = tn.maKH "
                + "INNER JOIN ThuCung tc ON tc.maThuCung = tn.maThuCung "
                + "LEFT JOIN DichVu dv ON dv.maLichHen = tn.maLichHen "
                + "WHERE tn.trangThai = N'Chờ thanh toán' "
                + "ORDER BY tn.thoiGianTiepNhan, tn.maTiepNhan, dv.tenDichVu";
        Map<String, PhieuChoThanhToanDTO> theoMa = new LinkedHashMap<>();
        try (Connection con = ConnectDB.getConnection();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Timestamp ts = rs.getTimestamp("thoiGianTiepNhan");
                String maTiepNhan = rs.getString("maTiepNhan");
                PhieuChoThanhToanDTO phieu = theoMa.get(maTiepNhan);
                if (phieu == null) {
                    BigDecimal phiDuKien = rs.getBigDecimal("phiDuKien");
                    phieu = new PhieuChoThanhToanDTO(maTiepNhan, ts == null ? null : ts.toLocalDateTime(),
                            rs.getString("maKH"), rs.getString("tenKH"), rs.getString("sdt"),
                            rs.getString("diaChi"), rs.getInt("hangThanhVien"), rs.getInt("diemTichLuy"),
                            rs.getString("maThuCung"), rs.getString("tenThuCung"),
                            rs.getString("dichVuYeuCau"), phiDuKien == null ? 0L : phiDuKien.longValue());
                    theoMa.put(maTiepNhan, phieu);
                }
                String maDichVu = rs.getString("maDichVu");
                if (maDichVu != null) {
                    BigDecimal gia = rs.getBigDecimal("giaDichVu");
                    phieu.danhSachDichVu.add(new DichVuHoaDonDTO(maDichVu, rs.getString("tenDichVu"),
                            gia == null ? 0L : gia.longValue()));
                }
            }
        }
        return new ArrayList<>(theoMa.values());
    }

    // ================= DANH MỤC =================

    // Mỗi dòng: {tên, giá, mã}
    public List<Object[]> laySanPham() throws SQLException {
        return layDanhMuc(
                "SELECT maSP AS ma, tenSP AS ten, giaBan AS gia " +
                        "FROM SanPham ORDER BY tenSP");
    }

    public List<Object[]> layDichVu() throws SQLException {
        return layDanhMuc(
                "SELECT maDichVu AS ma, tenDichVu AS ten, donGia AS gia " +
                        "FROM DichVu ORDER BY tenDichVu");
    }

    private List<Object[]> layDanhMuc(String sql) throws SQLException {
        List<Object[]> ds = new ArrayList<>();

        try (Connection con = ConnectDB.getConnection();
                Statement st = con.createStatement();
                ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                BigDecimal gia = rs.getBigDecimal("gia");

                ds.add(new Object[] {
                        rs.getString("ten"),
                        gia == null ? 0L : gia.longValue(),
                        rs.getString("ma")
                });
            }
        }

        return ds;
    }

    // ================= KHÁCH HÀNG =================

    public static class KhachHangDTO {
        public String maKH;
        public String tenKH;
        public String sdt;
        public String diaChi;
        public int hangThanhVien;
        public int diemTichLuy;

        public KhachHangDTO(
                String maKH, String tenKH, String sdt,
                String diaChi, int hangThanhVien, int diemTichLuy) {
            this.maKH = maKH;
            this.tenKH = tenKH;
            this.sdt = sdt;
            this.diaChi = diaChi;
            this.hangThanhVien = hangThanhVien;
            this.diemTichLuy = diemTichLuy;
        }

        @Override
        public String toString() {
            return sdt + " - " + tenKH;
        }
    }

    public List<KhachHangDTO> timKhachHang(String sdt)
            throws SQLException {
        List<KhachHangDTO> ds = new ArrayList<>();

        String sql = "SELECT maKH, tenKH, sdt, diaChi, " +
                "hangThanhVien, diemTichLuy " +
                "FROM KhachHang WHERE sdt LIKE ? ORDER BY sdt";

        try (Connection con = ConnectDB.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, "%" + sdt + "%");

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ds.add(new KhachHangDTO(
                            rs.getString("maKH"),
                            rs.getString("tenKH"),
                            rs.getString("sdt"),
                            rs.getString("diaChi"),
                            rs.getInt("hangThanhVien"),
                            rs.getInt("diemTichLuy")));
                }
            }
        }

        return ds;
    }

    public boolean tonTaiSDT(Connection con, String sdt)
            throws SQLException {
        String sql = "SELECT 1 FROM KhachHang WHERE sdt = ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, sdt);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public String taoMaKhachHang(Connection con)
            throws SQLException {
        String sql = "SELECT TOP 1 maKH FROM KhachHang " +
                "ORDER BY LEN(maKH) DESC, maKH DESC";

        String last = null;

        try (Statement st = con.createStatement();
                ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) {
                last = rs.getString(1);
            }
        }

        if (last == null) {
            return "KH001";
        }

        Matcher m = Pattern.compile("^(\\D*)(\\d+)$")
                .matcher(last.trim());

        if (!m.matches()) {
            throw new SQLException(
                    "Không tạo được mã khách hàng từ: " + last);
        }

        String prefix = m.group(1);
        String digits = m.group(2);
        long next = Long.parseLong(digits) + 1;

        return prefix + String.format(
                "%0" + digits.length() + "d", next);
    }

    public void themKhachHang(
            Connection con, String maKH, String ten,
            String sdt, String diaChi) throws SQLException {

        String sql = "INSERT INTO KhachHang " +
                "(maKH, tenKH, sdt, diaChi, hangThanhVien, diemTichLuy) " +
                "VALUES (?, ?, ?, ?, 0, 0)";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maKH);
            ps.setString(2, ten);
            ps.setString(3, sdt);
            ps.setString(4, diaChi);
            ps.executeUpdate();
        }
    }

    // Sinh mã hóa đơn dạng HD001, HD002...
    public String taoMaHoaDon(Connection con) throws SQLException {
        String sql = "SELECT TOP 1 maHoaDon FROM HoaDon ORDER BY LEN(maHoaDon) DESC, maHoaDon DESC";
        String last = null;
        try (Statement st = con.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            if (rs.next())
                last = rs.getString(1);
        }
        if (last == null)
            return "HD001";
        Matcher m = Pattern.compile("^(\\D*)(\\d+)$").matcher(last.trim());
        if (!m.matches())
            throw new SQLException("Không tạo được mã hóa đơn từ: " + last);
        String digits = m.group(2);
        return m.group(1) + String.format("%0" + digits.length() + "d", Long.parseLong(digits) + 1);
    }

    // Tìm mã phương thức thanh toán theo tên ("Tiền mặt", "QR Code")
    public String layMaPTTT(Connection con, String tenPTTT) throws SQLException {
        String sql = "SELECT maPTTT FROM PhuongThucThanhToan WHERE tenPTTT = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, tenPTTT);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next())
                    return rs.getString(1);
            }
        }
        throw new SQLException("Không tìm thấy phương thức thanh toán: " + tenPTTT);
    }

    public void themHoaDon(Connection con, String maHD, String maNV, String maKH,
            String maPTTT) throws SQLException {
        String sql = "INSERT INTO HoaDon (maHoaDon, ngayLapHD, maNV, maKH, maKhuyenMai, maPTTT) "
                + "VALUES (?, ?, ?, ?, NULL, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maHD);
            ps.setDate(2, java.sql.Date.valueOf(java.time.LocalDate.now()));
            ps.setString(3, maNV);
            ps.setString(4, maKH);
            ps.setString(5, maPTTT);
            ps.executeUpdate();
        }
    }

    public void themChiTiet(Connection con, String maHD, List<Object[]> dong, long tienKhachDua)
            throws SQLException {
        String sql = "INSERT INTO ChiTietHoaDon (maHoaDon, maSP, maDichVu, soLuong, donGia, tienKhachDua) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            for (Object[] r : dong) {
                boolean laSP = "Sản phẩm".equals(r[0]);
                String ma = String.valueOf(r[1]);
                ps.setString(1, maHD);
                if (laSP) {
                    ps.setString(2, ma);
                    ps.setNull(3, Types.VARCHAR);
                } else {
                    ps.setNull(2, Types.VARCHAR);
                    ps.setString(3, ma);
                }
                ps.setInt(4, ((Number) r[3]).intValue());
                ps.setBigDecimal(5, BigDecimal.valueOf(((Number) r[4]).longValue()));
                ps.setBigDecimal(6, BigDecimal.valueOf(tienKhachDua));
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    // Trừ tồn kho sản phẩm (bỏ nếu SanPham không có cột soLuongTon)
    public void truTonKho(Connection con, List<Object[]> dong) throws SQLException {
        String sql = "UPDATE SanPham SET soLuongTon = soLuongTon - ? WHERE maSP = ? AND soLuongTon >= ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            for (Object[] r : dong) {
                if (!"Sản phẩm".equals(r[0]))
                    continue;
                int sl = ((Number) r[3]).intValue();
                ps.setInt(1, sl);
                ps.setString(2, String.valueOf(r[1]));
                ps.setInt(3, sl);
                if (ps.executeUpdate() == 0)
                    throw new SQLException("Sản phẩm " + r[2] + " không đủ tồn kho.");
            }
        }
    }

    // Cập nhật điểm: trừ điểm đã dùng, cộng điểm mới (ví dụ 1 điểm / 10.000 đ)
    public void capNhatDiem(Connection con, String maKH, int diemDung, long tongTien) throws SQLException {
        int diemCong = (int) (tongTien / 10000);
        String sql = "UPDATE KhachHang SET diemTichLuy = diemTichLuy - ? + ? WHERE maKH = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, diemDung);
            ps.setInt(2, diemCong);
            ps.setString(3, maKH);
            ps.executeUpdate();
        }
    }
}
