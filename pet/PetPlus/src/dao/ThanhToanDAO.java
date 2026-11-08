package dao;

import database.ConnectDB;
import entity.KhuyenMai;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ThanhToanDAO {

    private static final String LOAI_SAN_PHAM = "Sản phẩm";
    private static final Pattern MA_CO_SO = Pattern.compile("^(\\D*)(\\d+)$");

    /** Dữ liệu hóa đơn tiếp nhận đang chờ thanh toán. */
    public static class PhieuChoThanhToanDTO {
        public final String maHoaDon;
        public final LocalDateTime thoiGianTiepNhan;
        public final String maKH;
        public final String tenKH;
        public final String sdt;
        public final String diaChi;
        public final int hangThanhVien;
        public final int diemTichLuy;
        public final String maThuCung;
        public final String tenThuCung;
        public final String dichVuYeuCau;
        public final long phiDuKien;
        public final List<DichVuHoaDonDTO> danhSachDichVu = new ArrayList<>();

        public PhieuChoThanhToanDTO(
                String maHoaDon,
                LocalDateTime thoiGianTiepNhan,
                String maKH,
                String tenKH,
                String sdt,
                String diaChi,
                int hangThanhVien,
                int diemTichLuy,
                String maThuCung,
                String tenThuCung,
                String dichVuYeuCau,
                long phiDuKien) {
            this.maHoaDon = maHoaDon;
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
            long tong = 0L;
            for (DichVuHoaDonDTO dichVu : danhSachDichVu) {
                tong = Math.addExact(tong, dichVu.donGia);
            }
            return tong;
        }
    }

    /** Một dịch vụ nằm trong chi tiết phiếu tiếp nhận. */
    public static class DichVuHoaDonDTO {
        public final String maDichVu;
        public final String tenDichVu;
        public final long donGia;

        public DichVuHoaDonDTO(String maDichVu, String tenDichVu, long donGia) {
            this.maDichVu = maDichVu;
            this.tenDichVu = tenDichVu;
            this.donGia = donGia;
        }
    }

    /** Thông tin khách hàng dùng trong màn hình thanh toán. */
    public static class KhachHangDTO {
        // Giữ các trường public có thể thay đổi để không phá vỡ tương thích
        // với những màn hình khác đang sử dụng DTO này.
        public String maKH;
        public String tenKH;
        public String sdt;
        public String diaChi;
        public int hangThanhVien;
        public int diemTichLuy;

        public KhachHangDTO(
                String maKH,
                String tenKH,
                String sdt,
                String diaChi,
                int hangThanhVien,
                int diemTichLuy) {
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

    /**
     * Lấy danh sách phiếu tiếp nhận đang chờ thanh toán và gom các dịch vụ
     * thuộc cùng một hóa đơn thành một DTO.
     */
    public List<PhieuChoThanhToanDTO> layPhieuChoThanhToan() throws SQLException {
        final String sql = "SELECT h.maHoaDon, h.thoiGianTiepNhan, h.maKH, kh.tenKH, kh.sdt, kh.diaChi, "
                + "kh.hangThanhVien, kh.diemTichLuy, h.maThuCung, tc.tenThuCung, "
                + "h.dichVuYeuCau, h.phiDuKien, ct.maDichVu, dv.tenDichVu, "
                + "ct.donGia AS giaDichVu "
                + "FROM HoaDon h "
                + "INNER JOIN KhachHang kh ON kh.maKH = h.maKH "
                + "INNER JOIN ThuCung tc ON tc.maThuCung = h.maThuCung "
                + "LEFT JOIN ChiTietHoaDon ct ON ct.maHoaDon = h.maHoaDon "
                + "AND ct.maDichVu IS NOT NULL "
                + "LEFT JOIN DichVu dv ON dv.maDichVu = ct.maDichVu "
                + "WHERE h.laTiepNhan = 1 AND h.maPTTT IS NULL "
                + "AND h.trangThaiTiepNhan = N'Chờ thanh toán' "
                + "ORDER BY h.thoiGianTiepNhan, h.maHoaDon, dv.tenDichVu";

        Map<String, PhieuChoThanhToanDTO> theoMaHoaDon = new LinkedHashMap<>();

        try (Connection con = ConnectDB.getConnection();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                String maHoaDon = rs.getString("maHoaDon");
                PhieuChoThanhToanDTO phieu = theoMaHoaDon.get(maHoaDon);

                if (phieu == null) {
                    Timestamp thoiGian = rs.getTimestamp("thoiGianTiepNhan");
                    BigDecimal phiDuKien = rs.getBigDecimal("phiDuKien");

                    phieu = new PhieuChoThanhToanDTO(
                            maHoaDon,
                            thoiGian == null ? null : thoiGian.toLocalDateTime(),
                            rs.getString("maKH"),
                            rs.getString("tenKH"),
                            rs.getString("sdt"),
                            rs.getString("diaChi"),
                            rs.getInt("hangThanhVien"),
                            rs.getInt("diemTichLuy"),
                            rs.getString("maThuCung"),
                            rs.getString("tenThuCung"),
                            rs.getString("dichVuYeuCau"),
                            phiDuKien == null ? 0L : phiDuKien.longValue());

                    theoMaHoaDon.put(maHoaDon, phieu);
                }

                String maDichVu = rs.getString("maDichVu");
                if (maDichVu != null) {
                    BigDecimal giaDichVu = rs.getBigDecimal("giaDichVu");
                    phieu.danhSachDichVu.add(new DichVuHoaDonDTO(
                            maDichVu,
                            rs.getString("tenDichVu"),
                            giaDichVu == null ? 0L : giaDichVu.longValue()));
                }
            }
        }

        return new ArrayList<>(theoMaHoaDon.values());
    }

    /**
     * Lấy danh mục sản phẩm. Mỗi dòng có thứ tự {tên, giá, mã} để tương thích
     * với model JTable hiện tại của pnlThanhToan.
     */
    public List<Object[]> laySanPham() throws SQLException {
        final String sql = "SELECT maSP AS ma, tenSP AS ten, giaBan AS gia "
                + "FROM SanPham "
                + "WHERE trangThai <> N'Đã xóa' "
                + "ORDER BY tenSP";
        return layDanhMuc(sql);
    }

    private List<Object[]> layDanhMuc(String sql) throws SQLException {
        List<Object[]> danhSach = new ArrayList<>();

        try (Connection con = ConnectDB.getConnection();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                BigDecimal gia = rs.getBigDecimal("gia");
                danhSach.add(new Object[] {
                        rs.getString("ten"),
                        gia == null ? 0L : gia.longValue(),
                        rs.getString("ma")
                });
            }
        }

        return danhSach;
    }

    /** Tìm khách hàng theo một phần số điện thoại. */
    public List<KhachHangDTO> timKhachHang(String sdt) throws SQLException {
        final String sql = "SELECT maKH, tenKH, sdt, diaChi, hangThanhVien, diemTichLuy "
                + "FROM KhachHang "
                + "WHERE trangThai <> N'Đã xóa' AND sdt LIKE ? "
                + "ORDER BY sdt";

        List<KhachHangDTO> danhSach = new ArrayList<>();

        try (Connection con = ConnectDB.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, "%" + (sdt == null ? "" : sdt.trim()) + "%");

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    danhSach.add(new KhachHangDTO(
                            rs.getString("maKH"),
                            rs.getString("tenKH"),
                            rs.getString("sdt"),
                            rs.getString("diaChi"),
                            rs.getInt("hangThanhVien"),
                            rs.getInt("diemTichLuy")));
                }
            }
        }

        return danhSach;
    }

    /** Kiểm tra số điện thoại đã thuộc một khách hàng còn hoạt động hay chưa. */
    public boolean tonTaiSDT(Connection con, String sdt) throws SQLException {
        final String sql = "SELECT 1 FROM KhachHang "
                + "WHERE sdt = ? AND trangThai <> N'Đã xóa'";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, sdt);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    /** Sinh mã khách hàng tiếp theo theo định dạng hiện có (ví dụ KH001). */
    public String taoMaKhachHang(Connection con) throws SQLException {
        final String sql = "SELECT TOP 1 maKH FROM KhachHang "
                + "ORDER BY LEN(maKH) DESC, maKH DESC";

        String maCuoi = null;
        try (Statement st = con.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) {
                maCuoi = rs.getString(1);
            }
        }

        return taoMaTiepTheo(maCuoi, "KH");
    }

    /** Sinh mã hóa đơn theo định dạng HD001, HD002... */
    public String taoMaHoaDon(Connection con) throws SQLException {
        final String sql = "SELECT TOP 1 maHoaDon FROM HoaDon WITH (UPDLOCK, HOLDLOCK) "
                + "WHERE maHoaDon LIKE 'HD[0-9]%' "
                + "ORDER BY LEN(maHoaDon) DESC, maHoaDon DESC";

        String maCuoi = null;
        try (Statement st = con.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) {
                maCuoi = rs.getString(1);
            }
        }

        return taoMaTiepTheo(maCuoi, "HD");
    }

    private String taoMaTiepTheo(String maCuoi, String tienToMacDinh) throws SQLException {
        if (maCuoi == null || maCuoi.trim().isEmpty()) {
            return tienToMacDinh + "001";
        }

        Matcher matcher = MA_CO_SO.matcher(maCuoi.trim());
        if (!matcher.matches()) {
            throw new SQLException("Không tạo được mã tiếp theo từ: " + maCuoi);
        }

        String tienTo = matcher.group(1);
        String chuoiSo = matcher.group(2);
        try {
            long soTiepTheo = Math.addExact(Long.parseLong(chuoiSo), 1L);
            return tienTo + String.format("%0" + chuoiSo.length() + "d", soTiepTheo);
        } catch (NumberFormatException | ArithmeticException ex) {
            throw new SQLException("Phần số của mã đã vượt giới hạn: " + maCuoi, ex);
        }
    }

    /** Lưu khách hàng mới trong transaction do tầng gọi quản lý. */
    public void themKhachHang(
            Connection con,
            String maKH,
            String ten,
            String sdt,
            String diaChi) throws SQLException {
        final String sql = "INSERT INTO KhachHang "
                + "(maKH, tenKH, sdt, diaChi, hangThanhVien, diemTichLuy) "
                + "VALUES (?, ?, ?, ?, 0, 0)";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maKH);
            ps.setString(2, ten);
            ps.setString(3, sdt);
            ps.setString(4, diaChi);
            ps.executeUpdate();
        }
    }

    /** Tra mã phương thức thanh toán theo tên hiển thị. */
    public String layMaPTTT(Connection con, String tenPTTT) throws SQLException {
        final String sql = "SELECT maPTTT FROM PhuongThucThanhToan WHERE tenPTTT = ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, tenPTTT);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString(1);
                }
            }
        }

        throw new SQLException("Không tìm thấy phương thức thanh toán: " + tenPTTT);
    }

    /** Tạo hóa đơn mới. */
    public void themHoaDon(
            Connection con,
            String maHD,
            String maNV,
            String maKH,
            String maKhuyenMai,
            String maPTTT) throws SQLException {
        final String sql = "INSERT INTO HoaDon "
                + "(maHoaDon, ngayLapHD, maNV, maKH, maKhuyenMai, maPTTT) "
                + "VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maHD);
            ps.setDate(2, java.sql.Date.valueOf(LocalDate.now()));
            ps.setString(3, maNV);
            ps.setString(4, maKH);
            if (maKhuyenMai == null) {
                ps.setNull(5, Types.VARCHAR);
            } else {
                ps.setString(5, maKhuyenMai);
            }
            ps.setString(6, maPTTT);
            ps.executeUpdate();
        }
    }

    /** Kiểm tra mã giao dịch ngân hàng đã được lưu vào hóa đơn khác chưa. */
    public boolean daDungMaGiaoDich(String maGiaoDich) throws SQLException {
        final String sql = "SELECT 1 FROM HoaDon WHERE maGiaoDichNH = ?";

        try (Connection con = ConnectDB.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maGiaoDich);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    /** Gắn mã giao dịch vào hóa đơn trong cùng transaction lưu hóa đơn. */
    public void ganMaGiaoDichNH(Connection con, String maHD, String maGiaoDich) throws SQLException {
        final String sql = "UPDATE HoaDon SET maGiaoDichNH = ? WHERE maHoaDon = ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maGiaoDich);
            ps.setString(2, maHD);
            ps.executeUpdate();
        }
    }

    public void capNhatThanhToanHoaDon(
            Connection con,
            String maHD,
            String maNV,
            String maKhuyenMai,
            String maPTTT,
            String maGiaoDichNganHang) throws SQLException {

        String sql = "UPDATE HoaDon SET "
                + "maNV = ?, "
                + "maKhuyenMai = ?, "
                + "maPTTT = ?, "
                + "maGiaoDichNH = ?, "
                + "ngayLapHD = CAST(GETDATE() AS DATE) "
                + "WHERE maHoaDon = ? "
                + "AND laTiepNhan = 1 "
                + "AND maPTTT IS NULL "
                + "AND trangThaiTiepNhan = N'Chờ thanh toán'";

        try (PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, maNV);

            if (maKhuyenMai == null) {
                ps.setNull(2, Types.VARCHAR);
            } else {
                ps.setString(2, maKhuyenMai);
            }

            ps.setString(3, maPTTT);

            if (maGiaoDichNganHang == null) {
                ps.setNull(4, Types.VARCHAR);
            } else {
                ps.setString(4, maGiaoDichNganHang);
            }

            ps.setString(5, maHD);

            if (ps.executeUpdate() != 1) {
                throw new SQLException(
                        "Hóa đơn " + maHD
                                + " không còn chờ thanh toán hoặc đã được thanh toán.");
            }
        }
    }

    /** Xóa chi tiết hóa đơn; giữ public để tương thích nghiệp vụ khác. */
    public void xoaChiTietHoaDon(Connection con, String maHD) throws SQLException {
        final String sql = "DELETE FROM ChiTietHoaDon WHERE maHoaDon = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maHD);
            ps.executeUpdate();
        }
    }

    /** Lưu tất cả dòng chi tiết hóa đơn bằng JDBC batch. */
    public void themChiTiet(Connection con, String maHD, List<Object[]> dong, long tienKhachDua)
            throws SQLException {
        if (dong == null || dong.isEmpty()) {
            return;
        }

        final String sql = "INSERT INTO ChiTietHoaDon "
                + "(maHoaDon, maSP, maDichVu, soLuong, donGia, tienKhachDua) "
                + "VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            for (Object[] row : dong) {
                boolean laSanPham = LOAI_SAN_PHAM.equals(row[0]);
                String ma = String.valueOf(row[1]);

                ps.setString(1, maHD);
                if (laSanPham) {
                    ps.setString(2, ma);
                    ps.setNull(3, Types.VARCHAR);
                } else {
                    ps.setNull(2, Types.VARCHAR);
                    ps.setString(3, ma);
                }

                ps.setInt(4, ((Number) row[3]).intValue());
                ps.setBigDecimal(5, BigDecimal.valueOf(((Number) row[4]).longValue()));
                ps.setBigDecimal(6, BigDecimal.valueOf(tienKhachDua));
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    /** Lưu tiền giảm khuyến mãi sản phẩm theo từng dòng sản phẩm đủ điều kiện. */
    public void themKhuyenMaiSanPham(
            Connection con,
            String maHD,
            List<Object[]> dong,
            KhuyenMai khuyenMai) throws SQLException {
        if (khuyenMai == null
                || khuyenMai.getMaKhuyenMai() == null
                || khuyenMai.getPhanTramGiam() <= 0
                || dong == null
                || dong.isEmpty()) {
            return;
        }

        Set<String> maSanPhamApDung = new HashSet<>(khuyenMai.getMaSanPhamApDung());
        if (maSanPhamApDung.isEmpty()) {
            return;
        }

        Map<String, Long> soLuongTheoMa = new LinkedHashMap<>();
        for (Object[] row : dong) {
            if (!LOAI_SAN_PHAM.equals(row[0])) {
                continue;
            }
            String maSP = String.valueOf(row[1]);
            if (maSanPhamApDung.contains(maSP)) {
                long soLuong = ((Number) row[3]).longValue();
                soLuongTheoMa.put(maSP,
                        Math.addExact(soLuongTheoMa.getOrDefault(maSP, 0L), soLuong));
            }
        }

        Set<String> maSanPhamDatNguong = new HashSet<>();
        for (Map.Entry<String, Long> entry : soLuongTheoMa.entrySet()) {
            if (entry.getValue() >= khuyenMai.getSoLuongToiThieu()) {
                maSanPhamDatNguong.add(entry.getKey());
            }
        }
        if (maSanPhamDatNguong.isEmpty()) {
            return;
        }

        final String sql = "INSERT INTO KhuyenMaiSanPham "
                + "(maKhuyenMaiMaSanPham, mucGiam, maSP, maKhuyenMai, maHoaDon) "
                + "VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            for (Object[] row : dong) {
                if (!LOAI_SAN_PHAM.equals(row[0])) {
                    continue;
                }

                String maSP = String.valueOf(row[1]);
                if (!maSanPhamDatNguong.contains(maSP)) {
                    continue;
                }

                long soLuong = ((Number) row[3]).longValue();
                long donGia = ((Number) row[4]).longValue();
                BigDecimal tienGiam = BigDecimal.valueOf(soLuong)
                        .multiply(BigDecimal.valueOf(donGia))
                        .multiply(BigDecimal.valueOf(khuyenMai.getPhanTramGiam()))
                        .divide(BigDecimal.valueOf(100), 0, RoundingMode.HALF_UP);

                if (tienGiam.signum() <= 0) {
                    continue;
                }

                String maDongKhuyenMai = "KMSP" + UUID.randomUUID()
                        .toString().replace("-", "").toUpperCase(java.util.Locale.ROOT);
                ps.setString(1, maDongKhuyenMai);
                ps.setBigDecimal(2, tienGiam);
                ps.setString(3, maSP);
                ps.setString(4, khuyenMai.getMaKhuyenMai());
                ps.setString(5, maHD);
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    /** Trừ tồn kho; nếu một dòng không đủ số lượng thì ném SQLException. */
    public void truTonKho(Connection con, List<Object[]> dong) throws SQLException {
        if (dong == null || dong.isEmpty()) {
            return;
        }

        final String sql = "UPDATE SanPham "
                + "SET soLuongTon = soLuongTon - ? "
                + "WHERE maSP = ? AND soLuongTon >= ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            for (Object[] row : dong) {
                if (!LOAI_SAN_PHAM.equals(row[0])) {
                    continue;
                }

                int soLuong = ((Number) row[3]).intValue();
                ps.setInt(1, soLuong);
                ps.setString(2, String.valueOf(row[1]));
                ps.setInt(3, soLuong);

                if (ps.executeUpdate() != 1) {
                    throw new SQLException("Sản phẩm " + row[2] + " không đủ tồn kho.");
                }
            }
        }
    }

    /** Trừ điểm đã dùng và cộng điểm mới theo quy tắc 1 điểm / 10.000 đồng. */
    public void capNhatDiem(Connection con, String maKH, int diemDung, long tongTien)
            throws SQLException {
        int diemCong = (int) (tongTien / 10000L);
        final String sql = "UPDATE KhachHang "
                + "SET diemTichLuy = diemTichLuy - ? + ? "
                + "WHERE maKH = ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, diemDung);
            ps.setInt(2, diemCong);
            ps.setString(3, maKH);
            ps.executeUpdate();
        }
    }

    /** Lấy các khuyến mãi chưa xóa và còn hiệu lực ở ngày hiện tại. */
    public List<KhuyenMai> layKhuyenMaiHieuLuc() throws SQLException {
        final String sql = "SELECT maKhuyenMai, tenKhuyenMai, phanTramGiam, dieuKienApDung, "
                + "ngayBatDau, ngayKetThuc "
                + "FROM KhuyenMai "
                + "WHERE trangThai <> N'Đã xóa' "
                + "AND (ngayBatDau IS NULL OR ngayBatDau <= ?) "
                + "AND (ngayKetThuc IS NULL OR ngayKetThuc >= ?) "
                + "ORDER BY tenKhuyenMai";

        List<KhuyenMai> danhSach = new ArrayList<>();
        java.sql.Date homNay = java.sql.Date.valueOf(LocalDate.now());

        try (Connection con = ConnectDB.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDate(1, homNay);
            ps.setDate(2, homNay);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    java.sql.Date ngayBatDau = rs.getDate("ngayBatDau");
                    java.sql.Date ngayKetThuc = rs.getDate("ngayKetThuc");

                    danhSach.add(new KhuyenMai(
                            rs.getString("maKhuyenMai"),
                            rs.getString("tenKhuyenMai"),
                            rs.getFloat("phanTramGiam"),
                            rs.getString("dieuKienApDung"),
                            ngayBatDau == null ? null : ngayBatDau.toLocalDate(),
                            ngayKetThuc == null ? null : ngayKetThuc.toLocalDate()));
                }
            }
        }

        return danhSach;
    }
}
