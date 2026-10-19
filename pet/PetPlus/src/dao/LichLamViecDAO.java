package dao;

import database.ConnectDB;
import entity.CaLamViec;
import entity.LichLamViec;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.security.SecureRandom;
import java.util.Locale;


public class LichLamViecDAO {


    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int SO_LUONG_MA = 1000;

   
    public static final class NhanVienItem {
        public final String maNV;
        public final String tenNV;

        public NhanVienItem(String maNV, String tenNV) {
            this.maNV = maNV;
            this.tenNV = tenNV;
        }

        @Override
        public String toString() {
            return tenNV + " (" + maNV + ")";
        }
    }


    public static final class LichItem {
        public final String maLichLamViec;
        public final LocalDate ngayLamViec;
        public final String maNV;
        public final String tenNV;
        public final CaLamViec ca;

        public LichItem(String maLichLamViec, LocalDate ngayLamViec,
                        String maNV, String tenNV, CaLamViec ca) {
            this.maLichLamViec = maLichLamViec;
            this.ngayLamViec = ngayLamViec;
            this.maNV = maNV;
            this.tenNV = tenNV;
            this.ca = ca;
        }
    }

    private Connection moKetNoi() throws SQLException {
        Connection conn = ConnectDB.getConnection();
        if (conn == null || conn.isClosed()) {
            throw new SQLException("Không kết nối được SQL Server. Hãy kiểm tra ConnectDB.");
        }
        return conn;
    }

  
    public List<NhanVienItem> layDanhSachNhanVien() throws SQLException {
        String sql = "SELECT nv.maNV, nv.tenNV "
                + "FROM NhanVien nv "
                + "INNER JOIN TaiKhoan tk ON nv.maTK = tk.maTK "
                + "WHERE tk.tenDangNhap <> N'admin' "
                + "AND (tk.trangThai IS NULL OR tk.trangThai <> N'Ngừng hoạt động') "
                + "ORDER BY nv.tenNV, nv.maNV";
        List<NhanVienItem> result = new ArrayList<NhanVienItem>();
        try (Connection c = moKetNoi();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                result.add(new NhanVienItem(rs.getString("maNV"), rs.getString("tenNV")));
            }
        }
        return result;
    }

   
    public List<CaLamViec> layDanhSachCa() throws SQLException {
        String sql = "SELECT maCa, tenCa, gioBatDau, gioKetThuc "
                + "FROM CaLamViec ORDER BY gioBatDau, maCa";
        List<CaLamViec> result = new ArrayList<CaLamViec>();
        try (Connection c = moKetNoi();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Time batDau = rs.getTime("gioBatDau");
                Time ketThuc = rs.getTime("gioKetThuc");
                result.add(new CaLamViec(rs.getString("maCa"), rs.getString("tenCa"),
                        batDau.toLocalTime(), ketThuc.toLocalTime()));
            }
        }
        return result;
    }

    public List<LichItem> layLich(LocalDate tuNgay, LocalDate denNgay) throws SQLException {
        if (tuNgay == null || denNgay == null || tuNgay.isAfter(denNgay)) {
            throw new IllegalArgumentException("Khoảng ngày tìm kiếm không hợp lệ.");
        }
        String sql = "SELECT l.maLichLamViec, l.ngayLamViec, nv.maNV, nv.tenNV, "
                + "c.maCa, c.tenCa, c.gioBatDau, c.gioKetThuc "
                + "FROM LichLamViec l "
                + "INNER JOIN NhanVien nv ON l.maNV = nv.maNV "
                + "INNER JOIN CaLamViec c ON l.maCa = c.maCa "
                + "WHERE l.ngayLamViec BETWEEN ? AND ? "
                + "ORDER BY l.ngayLamViec, c.gioBatDau, nv.tenNV";
        List<LichItem> result = new ArrayList<LichItem>();
        try (Connection conn = moKetNoi(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(tuNgay));
            ps.setDate(2, Date.valueOf(denNgay));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    CaLamViec ca = new CaLamViec(rs.getString("maCa"),
                            rs.getString("tenCa"), rs.getTime("gioBatDau").toLocalTime(),
                            rs.getTime("gioKetThuc").toLocalTime());
                    result.add(new LichItem(rs.getString("maLichLamViec"),
                            rs.getDate("ngayLamViec").toLocalDate(),
                            rs.getString("maNV"), rs.getString("tenNV"), ca));
                }
            }
        }
        return result;
    }

    
    private String taoMaLichKhongTrung(Connection c) throws SQLException {
        boolean[] daDung = new boolean[SO_LUONG_MA];
        String sql = "SELECT maLichLamViec FROM LichLamViec WITH (UPDLOCK, HOLDLOCK) "
                   + "WHERE maLichLamViec LIKE 'LLV%'";

        try (PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String ma = rs.getString(1);
                if (ma != null) {
                    ma = ma.trim().toUpperCase(Locale.ROOT);
                    if (ma.matches("LLV[0-9]{3}")) {
                        int so = Integer.parseInt(ma.substring(3));
                        daDung[so] = true;
                    }
                }
            }
        }

        List<Integer> conTrong = new ArrayList<Integer>();
        for (int so = 0; so < SO_LUONG_MA; so++) {
            if (!daDung[so]) conTrong.add(so);
        }
        if (conTrong.isEmpty()) {
            throw new SQLException("Đã sử dụng hết 1000 mã lịch (LLV000 - LLV999). "
                    + "Cần mở rộng định dạng mã nếu muốn thêm lịch.");
        }
        int soNgauNhien = conTrong.get(RANDOM.nextInt(conTrong.size()));
        return String.format(Locale.ROOT, "LLV%03d", soNgauNhien);
    }

   
    public String themLich(LocalDate ngay, String maNV, String maCa) throws SQLException {
        kiemTraThongTin(ngay, maNV, maCa);
       
        for (int lanThu = 0; lanThu < 10; lanThu++) {
            try (Connection c = moKetNoi()) {
                c.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
                c.setAutoCommit(false);
                try {
                    kiemTraTrungGio(c, ngay, maNV, maCa, null);
                    String maLich = taoMaLichKhongTrung(c);
                    String sql = "INSERT INTO LichLamViec "
                            + "(maLichLamViec, ngayLamViec, maNV, maCa) VALUES (?, ?, ?, ?)";
                    try (PreparedStatement ps = c.prepareStatement(sql)) {
                        ps.setString(1, maLich);
                        ps.setDate(2, Date.valueOf(ngay));
                        ps.setString(3, maNV);
                        ps.setString(4, maCa);
                        ps.executeUpdate();
                    }
                    c.commit();
                    return maLich;
                } catch (SQLException ex) {
                    try {
                        c.rollback();
                    } catch (SQLException rollbackError) {
                        ex.addSuppressed(rollbackError);
                    }
                    if (laLoiCoTheThuLai(ex)) continue;
                    throw ex;
                }
            }
        }
        throw new SQLException("Không thể sinh mã LLVxxx sau nhiều lần thử. Vui lòng thử lại.");
    }

    
    private boolean laLoiCoTheThuLai(SQLException ex) {
        if (ex.getErrorCode() == 1205) return true; // Deadlock SQL Server
        if (ex.getErrorCode() != 2627 && ex.getErrorCode() != 2601) return false;
        String thongBao = ex.getMessage();
        return thongBao != null && thongBao.toLowerCase(Locale.ROOT)
                .contains("pk_lichlamviec");
    }

    public String themLich(LichLamViec lich) throws SQLException {
        if (lich == null || lich.getMaNV() == null || lich.getMaCa() == null) {
            throw new IllegalArgumentException("Thiếu thông tin lịch/nhân viên/ca làm việc.");
        }
        return themLich(lich.getNgayLamViec(), lich.getMaNV().getMaNV(),
                lich.getMaCa().getMaCa());
    }

   
    public void capNhatLich(String maLich, LocalDate ngay, String maNV, String maCa)
            throws SQLException {
        kiemTraThongTin(ngay, maNV, maCa);
        if (maLich == null || maLich.trim().isEmpty()) {
            throw new IllegalArgumentException("Thiếu mã lịch cần cập nhật.");
        }
        try (Connection c = moKetNoi()) {
            c.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
            c.setAutoCommit(false);
            try {
                kiemTraTrungGio(c, ngay, maNV, maCa, maLich);
                String sql = "UPDATE LichLamViec SET ngayLamViec=?, maNV=?, maCa=? "
                        + "WHERE maLichLamViec=?";
                int count;
                try (PreparedStatement ps = c.prepareStatement(sql)) {
                    ps.setDate(1, Date.valueOf(ngay));
                    ps.setString(2, maNV);
                    ps.setString(3, maCa);
                    ps.setString(4, maLich);
                    count = ps.executeUpdate();
                }
                if (count == 0) {
                    throw new SQLException("Không tìm thấy lịch cần sửa. Hãy tải lại danh sách.");
                }
                c.commit();
            } catch (SQLException ex) {
                c.rollback();
                throw ex;
            }
        }
    }

    public void xoaLich(String maLich) throws SQLException {
        if (maLich == null || maLich.trim().isEmpty()) {
            throw new IllegalArgumentException("Chưa chọn lịch cần xóa.");
        }
        try (Connection c = moKetNoi();
             PreparedStatement ps = c.prepareStatement(
                     "DELETE FROM LichLamViec WHERE maLichLamViec=?")) {
            ps.setString(1, maLich);
            if (ps.executeUpdate() == 0) {
                throw new SQLException("Lịch này không còn tồn tại. Hãy tải lại danh sách.");
            }
        }
    }

    private void kiemTraThongTin(LocalDate ngay, String maNV, String maCa) {
        if (ngay == null || maNV == null || maNV.trim().isEmpty()
                || maCa == null || maCa.trim().isEmpty()) {
            throw new IllegalArgumentException("Vui lòng nhập ngày, chọn nhân viên và ca làm việc.");
        }
    }

    /** Không cho cùng nhân viên làm hai ca có giờ chồng lên nhau trong cùng ngày. */
    private void kiemTraTrungGio(Connection c, LocalDate ngay, String maNV,
                                String maCa, String maLichLoaiTru) throws SQLException {
        String sql = "SELECT COUNT(*) FROM LichLamViec l "
                + "INNER JOIN CaLamViec daXep ON daXep.maCa=l.maCa "
                + "INNER JOIN CaLamViec caMoi ON caMoi.maCa=? "
                + "WHERE l.maNV=? AND l.ngayLamViec=? "
                + "AND daXep.gioBatDau < caMoi.gioKetThuc "
                + "AND daXep.gioKetThuc > caMoi.gioBatDau "
                + "AND (? IS NULL OR l.maLichLamViec<>?)";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, maCa);
            ps.setString(2, maNV);
            ps.setDate(3, Date.valueOf(ngay));
            ps.setString(4, maLichLoaiTru);
            ps.setString(5, maLichLoaiTru);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    throw new SQLException("Nhân viên đã có ca trùng giờ trong ngày này. "
                            + "Hãy chọn ca hoặc ngày khác.");
                }
            }
        }
    }
}
