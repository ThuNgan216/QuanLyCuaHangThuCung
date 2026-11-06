package dao;

import database.ConnectDB;
import entity.*;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/** Truy vấn lịch hẹn và thông tin liên quan trong SQL Server. */
public class LichHenDAO {
    private static final String SELECT = "SELECT l.maLichHen,l.thoiGianHen,l.trangThai,l.maNV,l.maKH,l.maThuCung, " +
            "nv.tenNV,kh.tenKH,tc.tenThuCung FROM LichHen l " +
            "LEFT JOIN NhanVien nv ON l.maNV=nv.maNV LEFT JOIN KhachHang kh ON l.maKH=kh.maKH " +
            "LEFT JOIN ThuCung tc ON l.maThuCung=tc.maThuCung WHERE l.trangThai <> N'Đã xóa' ";

    /** Nhân viên được nhận lịch hẹn: tài khoản đang hoạt động và là nhân viên chăm sóc. */
    private static final String DIEU_KIEN_NV_NHAN_LICH = "tk.trangThai = N'Hoạt động' "
            + "AND (UPPER(tk.vaiTro) IN (N'CHAM_SOC', N'CHĂM SÓC') OR nv.chucVu LIKE N'%chăm sóc%')";

    private static final DateTimeFormatter GIO_HIEN_THI = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public List<LichHen> layDanhSach() {
        return truyVan(SELECT + " ORDER BY l.thoiGianHen DESC", null);
    }

    /** Tìm theo mã lịch, tên / SĐT khách hàng, tên thú cưng, tên nhân viên hoặc trạng thái. */
    public List<LichHen> timKiem(String key) {
        if (key == null || key.trim().isEmpty())
            return layDanhSach();
        String sql = SELECT + " AND (l.maLichHen LIKE ? OR kh.tenKH LIKE ? OR kh.sdt LIKE ? "
                + "OR tc.tenThuCung LIKE ? OR nv.tenNV LIKE ? OR l.trangThai LIKE ?) ORDER BY l.thoiGianHen DESC";
        String k = "%" + key.trim() + "%";
        return truyVan(sql, Arrays.<Object>asList(k, k, k, k, k, k));
    }

    /** Tìm một lịch hẹn (chưa bị xóa) theo mã; trả về null nếu không có. */
    public LichHen timTheoMa(String ma) {
        List<LichHen> ds = truyVan(SELECT + " AND l.maLichHen=?", Collections.<Object>singletonList(ma));
        return ds.isEmpty() ? null : ds.get(0);
    }

    private List<LichHen> truyVan(String sql, List<Object> args) {
        List<LichHen> ds = new ArrayList<>();
        try (Connection c = ConnectDB.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            if (args != null)
                for (int i = 0; i < args.size(); i++)
                    ps.setObject(i + 1, args.get(i));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    LichHen l = new LichHen();
                    l.setMaLichHen(rs.getString("maLichHen"));
                    Timestamp ts = rs.getTimestamp("thoiGianHen");
                    l.setThoiGianHen(ts == null ? null : ts.toLocalDateTime());
                    l.setTrangThai(rs.getString("trangThai"));
                    l.setMaNV(new NhanVien(rs.getString("maNV")));
                    l.getMaNV().setTenNV(rs.getString("tenNV"));
                    l.setMaKH(new KhachHang(rs.getString("maKH")));
                    l.getMaKH().setTenKH(rs.getString("tenKH"));
                    l.setMaThuCung(new ThuCung());
                    l.getMaThuCung().setMaThuCung(rs.getString("maThuCung"));
                    l.getMaThuCung().setTenThuCung(rs.getString("tenThuCung"));
                    ds.add(l);
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Không thể tải lịch hẹn: " + e.getMessage(), e);
        }
        return ds;
    }

    public boolean tonTai(String ma) {
        try (Connection c = ConnectDB.getConnection();
                PreparedStatement p = c
                        .prepareStatement("SELECT 1 FROM LichHen WHERE maLichHen=? AND trangThai <> N'Đã xóa'")) {
            p.setString(1, ma);
            try (ResultSet r = p.executeQuery()) {
                return r.next();
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Không thể kiểm tra mã lịch hẹn: " + e.getMessage(), e);
        }
    }

    /** Nhân viên được chọn khi đặt lịch hẹn (đang hoạt động, thuộc bộ phận chăm sóc). */
    public List<NhanVien> layNhanVien() {
        List<NhanVien> ds = new ArrayList<>();
        String q = "SELECT nv.maNV,nv.tenNV FROM NhanVien nv INNER JOIN TaiKhoan tk ON tk.maTK=nv.maTK WHERE "
                + DIEU_KIEN_NV_NHAN_LICH + " ORDER BY nv.tenNV";
        try (Connection c = ConnectDB.getConnection();
                PreparedStatement p = c.prepareStatement(q);
                ResultSet r = p.executeQuery()) {
            while (r.next()) {
                NhanVien n = new NhanVien(r.getString("maNV"));
                n.setTenNV(r.getString("tenNV"));
                ds.add(n);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Không thể tải nhân viên: " + e.getMessage(), e);
        }
        return ds;
    }

    /** Nhân viên này có đang được phép nhận lịch hẹn hay không. */
    public boolean nhanVienNhanLichDuoc(String maNV) {
        String q = "SELECT 1 FROM NhanVien nv INNER JOIN TaiKhoan tk ON tk.maTK=nv.maTK WHERE nv.maNV=? AND "
                + DIEU_KIEN_NV_NHAN_LICH;
        try (Connection c = ConnectDB.getConnection(); PreparedStatement p = c.prepareStatement(q)) {
            p.setString(1, maNV);
            try (ResultSet r = p.executeQuery()) {
                return r.next();
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Không thể kiểm tra nhân viên: " + e.getMessage(), e);
        }
    }

    /** Các ca làm việc của nhân viên trong ngày, mỗi phần tử là {giờ bắt đầu, giờ kết thúc}, theo thứ tự giờ. */
    public List<LocalTime[]> layCaLamTrongNgay(String maNV, LocalDate ngay) {
        List<LocalTime[]> ds = new ArrayList<>();
        String q = "SELECT ca.gioBatDau, ca.gioKetThuc FROM LichLamViec llv INNER JOIN CaLamViec ca ON ca.maCa=llv.maCa "
                + "WHERE llv.maNV=? AND llv.ngayLamViec=? ORDER BY ca.gioBatDau";
        try (Connection c = ConnectDB.getConnection(); PreparedStatement p = c.prepareStatement(q)) {
            p.setString(1, maNV);
            p.setDate(2, java.sql.Date.valueOf(ngay));
            try (ResultSet r = p.executeQuery()) {
                while (r.next())
                    ds.add(new LocalTime[] { r.getTime(1).toLocalTime(), r.getTime(2).toLocalTime() });
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Không thể tải ca làm việc: " + e.getMessage(), e);
        }
        return ds;
    }

    /**
     * Các lịch còn hiệu lực (chưa Hoàn thành / Đã hủy / Đã xóa) của một nhân viên hoặc một thú cưng
     * chồng lấn khoảng [batDau, batDau + phut). Lịch kết thúc đúng lúc lịch khác bắt đầu thì không chồng.
     *
     * @param cot       "maNV" hoặc "maThuCung"
     * @param maBoQua   mã lịch bỏ qua (chính lịch đang sửa), có thể null
     */
    public List<LichHen> timLichChong(String cot, String ma, LocalDateTime batDau, int phut, String maBoQua) {
        if (!"maNV".equals(cot) && !"maThuCung".equals(cot))
            throw new IllegalArgumentException("Cột kiểm tra trùng lịch không hợp lệ.");
        String sql = SELECT + " AND l." + cot + " = ? AND l.trangThai NOT IN (N'Hoàn thành', N'Đã hủy') "
                + "AND l.thoiGianHen < ? AND DATEADD(MINUTE, ?, l.thoiGianHen) > ?"
                + (maBoQua == null ? "" : " AND l.maLichHen <> ?") + " ORDER BY l.thoiGianHen";
        List<Object> args = new ArrayList<>(Arrays.<Object>asList(ma, Timestamp.valueOf(batDau.plusMinutes(phut)),
                Integer.valueOf(phut), Timestamp.valueOf(batDau)));
        if (maBoQua != null)
            args.add(maBoQua);
        return truyVan(sql, args);
    }

    public List<KhachHang> timKhachHang(String key) {
        List<KhachHang> ds = new ArrayList<>();
        String q = "SELECT maKH,tenKH,sdt,diaChi,hangThanhVien,diemTichLuy FROM KhachHang WHERE trangThai <> N'Đã xóa' AND (tenKH LIKE ? OR sdt LIKE ?) ORDER BY tenKH";
        try (Connection c = ConnectDB.getConnection(); PreparedStatement p = c.prepareStatement(q)) {
            String k = "%" + (key == null ? "" : key.trim()) + "%";
            p.setString(1, k);
            p.setString(2, k);
            try (ResultSet r = p.executeQuery()) {
                while (r.next()) {
                    KhachHang kh = new KhachHang(r.getString("maKH"), r.getString("tenKH"), r.getString("sdt"),
                            r.getString("diaChi"), r.getInt("hangThanhVien"), r.getInt("diemTichLuy"));
                    ds.add(kh);
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Không thể tìm khách hàng: " + e.getMessage(), e);
        }
        return ds;
    }

    public List<ThuCung> layThuCungTheoKhach(String maKH, String key) {
        List<ThuCung> ds = new ArrayList<>();
        String q = "SELECT maThuCung,tenThuCung,loai,giong,maKH FROM ThuCung WHERE maKH=? AND trangThai <> N'Đã xóa' AND tenThuCung LIKE ? ORDER BY tenThuCung";
        try (Connection c = ConnectDB.getConnection(); PreparedStatement p = c.prepareStatement(q)) {
            p.setString(1, maKH);
            p.setString(2, "%" + (key == null ? "" : key.trim()) + "%");
            try (ResultSet r = p.executeQuery()) {
                while (r.next()) {
                    ThuCung t = new ThuCung();
                    t.setMaThuCung(r.getString("maThuCung"));
                    t.setTenThuCung(r.getString("tenThuCung"));
                    t.setLoai(r.getString("loai"));
                    t.setGiong(r.getString("giong"));
                    ds.add(t);
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Không thể tải thú cưng của khách hàng: " + e.getMessage(), e);
        }
        return ds;
    }

    /** Mã gợi ý để hiển thị; mã thật được sinh lại khi lưu (xem {@link #them}). */
    public String taoMaMoi() {
        try (Connection c = ConnectDB.getConnection()) {
            return taoMaMoi(c);
        } catch (SQLException e) {
            throw new IllegalStateException("Không thể phát sinh mã lịch hẹn: " + e.getMessage(), e);
        }
    }

    private String taoMaMoi(Connection c) throws SQLException {
        return TienIchDAO.sinhMa(c, "LichHen", "maLichHen", "LH");
    }

    /**
     * Khách hàng hoặc thú cưng còn lịch hẹn chưa kết thúc hay không. Câu truy vấn này dùng chung cho
     * KhachHangDAO và ThuCungDAO để hai module cùng một định nghĩa "chưa kết thúc".
     *
     * @param cot "maKH" hoặc "maThuCung"
     */
    public boolean coLichHenChuaKetThuc(String cot, String ma) {
        if (!"maKH".equals(cot) && !"maThuCung".equals(cot))
            throw new IllegalArgumentException("Cột kiểm tra lịch hẹn không hợp lệ.");
        String sql = "SELECT 1 FROM LichHen WHERE " + cot + " = ? AND trangThai NOT IN "
                + TienIchDAO.LICH_HEN_KHONG_CON_HIEU_LUC;
        try (Connection c = ConnectDB.getConnection(); PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, ma);
            try (ResultSet r = p.executeQuery()) {
                return r.next();
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Không thể kiểm tra lịch hẹn chưa kết thúc: " + e.getMessage(), e);
        }
    }

    /**
     * Thêm lịch hẹn trong một transaction: khóa khoảng giờ của nhân viên và của thú cưng, kiểm tra
     * trùng lại lần nữa rồi mới INSERT, để hai người đặt cùng lúc không cùng lọt qua phần kiểm tra
     * ở service. Mã lịch hẹn được sinh tại đây (và sinh lại nếu bị trùng).
     *
     * @throws IllegalArgumentException nếu khung giờ đã bị người khác đặt
     */
    public boolean them(LichHen l, int phutMotLich) {
        String sql = "INSERT INTO LichHen(maLichHen,thoiGianHen,trangThai,maNV,maKH,maThuCung) VALUES(?,?,?,?,?,?)";
        try (Connection c = ConnectDB.getConnection()) {
            boolean autoCommitCu = c.getAutoCommit();
            c.setAutoCommit(false);
            try {
                khoaVaKiemTraChong(c, l, phutMotLich, null);
                for (int lanThu = 1; lanThu <= TienIchDAO.SO_LAN_THU_TOI_DA; lanThu++) {
                    l.setMaLichHen(taoMaMoi(c));
                    try (PreparedStatement p = c.prepareStatement(sql)) {
                        gan(p, l);
                        boolean ok = p.executeUpdate() > 0;
                        c.commit();
                        return ok;
                    } catch (SQLException e) {
                        if (!TienIchDAO.laTrungRangBuoc(e, "PK_LichHen") || lanThu == TienIchDAO.SO_LAN_THU_TOI_DA)
                            throw e;
                    }
                }
                c.rollback();
                return false;
            } catch (SQLException | RuntimeException ex) {
                try {
                    c.rollback();
                } catch (SQLException rollbackError) {
                    ex.addSuppressed(rollbackError);
                }
                throw ex;
            } finally {
                c.setAutoCommit(autoCommitCu);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Không thể thêm lịch hẹn: " + e.getMessage(), e);
        }
    }

    /** Cập nhật lịch hẹn, cũng khóa và kiểm tra trùng khung giờ trong transaction (bỏ qua chính lịch này). */
    public boolean capNhat(LichHen l, int phutMotLich) {
        String sql = "UPDATE LichHen SET thoiGianHen=?,trangThai=?,maNV=?,maKH=?,maThuCung=? WHERE maLichHen=? AND trangThai <> N'Đã xóa'";
        try (Connection c = ConnectDB.getConnection()) {
            boolean autoCommitCu = c.getAutoCommit();
            c.setAutoCommit(false);
            try {
                khoaVaKiemTraChong(c, l, phutMotLich, l.getMaLichHen());
                boolean ok;
                try (PreparedStatement p = c.prepareStatement(sql)) {
                    p.setTimestamp(1, Timestamp.valueOf(l.getThoiGianHen()));
                    p.setString(2, l.getTrangThai());
                    p.setString(3, l.getMaNV().getMaNV());
                    p.setString(4, l.getMaKH().getMaKH());
                    p.setString(5, l.getMaThuCung().getMaThuCung());
                    p.setString(6, l.getMaLichHen());
                    ok = p.executeUpdate() > 0;
                }
                c.commit();
                return ok;
            } catch (SQLException | RuntimeException ex) {
                try {
                    c.rollback();
                } catch (SQLException rollbackError) {
                    ex.addSuppressed(rollbackError);
                }
                throw ex;
            } finally {
                c.setAutoCommit(autoCommitCu);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Không thể cập nhật lịch hẹn: " + e.getMessage(), e);
        }
    }

    /** Chỉ đổi trạng thái (xác nhận / hoàn thành / hủy); các luật chuyển trạng thái do service kiểm tra. */
    public boolean capNhatTrangThai(String ma, String trangThai) {
        String sql = "UPDATE LichHen SET trangThai=? WHERE maLichHen=? AND trangThai <> N'Đã xóa'";
        try (Connection c = ConnectDB.getConnection(); PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, trangThai);
            p.setString(2, ma);
            return p.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new IllegalStateException("Không thể đổi trạng thái lịch hẹn: " + e.getMessage(), e);
        }
    }

    /** Lịch Hoàn thành / Đã hủy không chiếm khung giờ nên không cần kiểm tra. */
    private void khoaVaKiemTraChong(Connection c, LichHen l, int phut, String maBoQua) throws SQLException {
        TrangThaiLichHen tt = TrangThaiLichHen.tuTen(l.getTrangThai());
        if (tt != null && !tt.giuKhungGio())
            return;
        kiemTraKhongChong(c, "maNV", l.getMaNV().getMaNV(), "Nhân viên " + l.getMaNV().getMaNV(), l, phut, maBoQua);
        kiemTraKhongChong(c, "maThuCung", l.getMaThuCung().getMaThuCung(),
                "Thú cưng " + l.getMaThuCung().getMaThuCung(), l, phut, maBoQua);
    }

    private void kiemTraKhongChong(Connection c, String cot, String ma, String doiTuong, LichHen l, int phut,
            String maBoQua) throws SQLException {
        String sql = "SELECT TOP 1 maLichHen, thoiGianHen FROM LichHen WITH (UPDLOCK, HOLDLOCK) WHERE " + cot
                + " = ? AND trangThai NOT IN " + TienIchDAO.LICH_HEN_KHONG_CON_HIEU_LUC
                + " AND thoiGianHen < ? AND DATEADD(MINUTE, ?, thoiGianHen) > ?"
                + (maBoQua == null ? "" : " AND maLichHen <> ?");
        try (PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, ma);
            p.setTimestamp(2, Timestamp.valueOf(l.getThoiGianHen().plusMinutes(phut)));
            p.setInt(3, phut);
            p.setTimestamp(4, Timestamp.valueOf(l.getThoiGianHen()));
            if (maBoQua != null)
                p.setString(5, maBoQua);
            try (ResultSet r = p.executeQuery()) {
                if (r.next())
                    throw new IllegalArgumentException(doiTuong + " vừa có lịch " + r.getString(1) + " lúc "
                            + r.getTimestamp(2).toLocalDateTime().format(GIO_HIEN_THI)
                            + " trùng khung giờ này. Vui lòng chọn thời gian khác.");
            }
        }
    }

    public boolean xoa(String ma) {
        try (Connection c = ConnectDB.getConnection()) {
            return DeletePolicy.xoaHoacAn(c,
                    "SELECT 1 FROM LichHen WITH (XLOCK, HOLDLOCK) WHERE maLichHen=? AND trangThai <> N'Đã xóa'",
                    new String[] { "SELECT 1 FROM DichVu WHERE maLichHen=?", "SELECT 1 FROM HoaDon WHERE maLichHen=?" },
                    "UPDATE LichHen SET trangThai=N'Đã xóa' WHERE maLichHen=? AND trangThai <> N'Đã xóa'",
                    "DELETE FROM LichHen WHERE maLichHen=? AND trangThai <> N'Đã xóa'", ma);
        } catch (SQLException e) {
            throw new IllegalStateException("Không thể xóa hoặc ẩn lịch hẹn: " + e.getMessage(), e);
        }
    }

    private void gan(PreparedStatement p, LichHen l) throws SQLException {
        p.setString(1, l.getMaLichHen());
        p.setTimestamp(2, Timestamp.valueOf(l.getThoiGianHen()));
        p.setString(3, l.getTrangThai());
        p.setString(4, l.getMaNV().getMaNV());
        p.setString(5, l.getMaKH().getMaKH());
        p.setString(6, l.getMaThuCung().getMaThuCung());
    }
}
