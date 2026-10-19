package dao;

import database.ConnectDB;

import java.sql.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ThanhToanDAO {

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
}
