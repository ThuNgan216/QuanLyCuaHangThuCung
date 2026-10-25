package dao;

import database.ConnectDB;
import entity.DichVu;
import entity.KhachHang;
import entity.LichHen;
import entity.NhanVien;
import entity.PhieuTiepNhanThuCung;
import entity.TaiKhoan;
import entity.ThuCung;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Tiếp nhận và theo dõi thú cưng bằng dữ liệu lưu trực tiếp trên hóa đơn. */
public class TiepNhanThuCungDAO {
    private static final String SELECT =
            "SELECT h.maHoaDon AS maTiepNhan, h.maLichHen, h.thoiGianTiepNhan, h.gioHenTra, "
            + "h.maNVTiepNhan AS maNV, nv.tenNV, h.maKH, kh.tenKH, kh.sdt, h.maThuCung, "
            + "tc.tenThuCung, tc.loai, h.dichVuYeuCau, h.tinhTrangBanGiao, h.ghiChuTiepNhan AS ghiChu, "
            + "h.phiDuKien, h.trangThaiTiepNhan AS trangThai "
            + "FROM HoaDon h "
            + "INNER JOIN NhanVien nv ON h.maNVTiepNhan = nv.maNV "
            + "INNER JOIN KhachHang kh ON h.maKH = kh.maKH "
            + "INNER JOIN ThuCung tc ON h.maThuCung = tc.maThuCung ";

    public List<PhieuTiepNhanThuCung> layDanhSach(String tuKhoa) {
        String sql = SELECT + "WHERE h.laTiepNhan = 1 AND h.trangThaiTiepNhan <> N'Chờ thanh toán' ";
        String key = tuKhoa == null ? "" : tuKhoa.trim();
        if (!key.isEmpty()) {
            sql += "AND (h.maHoaDon LIKE ? OR h.maLichHen LIKE ? OR kh.tenKH LIKE ? "
                    + "OR kh.sdt LIKE ? OR tc.tenThuCung LIKE ? OR h.dichVuYeuCau LIKE ? "
                    + "OR h.trangThaiTiepNhan LIKE ?) ";
        }
        sql += "ORDER BY h.thoiGianTiepNhan DESC";

        List<PhieuTiepNhanThuCung> danhSach = new ArrayList<>();
        try (Connection con = ConnectDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            if (!key.isEmpty()) {
                String like = "%" + key + "%";
                for (int i = 1; i <= 7; i++) ps.setString(i, like);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) danhSach.add(docPhieu(rs));
            }
        } catch (SQLException e) {
            throw loiSchema("Không thể tải danh sách tiếp nhận", e);
        }
        return danhSach;
    }

    /** Lịch đã xác nhận chưa có hóa đơn tiếp nhận. */
    public List<LichHen> layLichHenChoTiepNhan() {
        String sql = "SELECT l.maLichHen, l.thoiGianHen, l.trangThai, l.maNV, nv.tenNV, "
                + "l.maKH, kh.tenKH, l.maThuCung, tc.tenThuCung "
                + "FROM LichHen l "
                + "INNER JOIN NhanVien nv ON l.maNV = nv.maNV "
                + "INNER JOIN KhachHang kh ON l.maKH = kh.maKH "
                + "INNER JOIN ThuCung tc ON l.maThuCung = tc.maThuCung "
                + "WHERE l.trangThai = N'Đã xác nhận' "
                + "AND NOT EXISTS (SELECT 1 FROM HoaDon h WHERE h.laTiepNhan = 1 "
                + "AND h.maLichHen = l.maLichHen) ORDER BY l.thoiGianHen";
        List<LichHen> danhSach = new ArrayList<>();
        try (Connection con = ConnectDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                LichHen lichHen = new LichHen();
                lichHen.setMaLichHen(rs.getString("maLichHen"));
                Timestamp thoiGian = rs.getTimestamp("thoiGianHen");
                lichHen.setThoiGianHen(thoiGian == null ? null : thoiGian.toLocalDateTime());
                lichHen.setTrangThai(rs.getString("trangThai"));
                NhanVien nhanVien = new NhanVien(rs.getString("maNV"));
                nhanVien.setTenNV(rs.getString("tenNV"));
                lichHen.setMaNV(nhanVien);
                KhachHang khachHang = new KhachHang(rs.getString("maKH"));
                khachHang.setTenKH(rs.getString("tenKH"));
                lichHen.setMaKH(khachHang);
                ThuCung thuCung = new ThuCung(rs.getString("maThuCung"));
                thuCung.setTenThuCung(rs.getString("tenThuCung"));
                lichHen.setMaThuCung(thuCung);
                danhSach.add(lichHen);
            }
        } catch (SQLException e) {
            throw loiSchema("Không thể tải lịch hẹn để tiếp nhận", e);
        }
        return danhSach;
    }

    public List<DichVu> layDichVuTheoLichHen(String maLichHen) {
        List<DichVu> danhSach = new ArrayList<>();
        String sql = "SELECT maDichVu, tenDichVu, donGia, moTa, trangThai, maLichHen "
                + "FROM DichVu WHERE maLichHen = ? ORDER BY tenDichVu";
        try (Connection con = ConnectDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maLichHen);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) danhSach.add(docDichVu(rs));
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Không thể tải dịch vụ của lịch hẹn: " + e.getMessage(), e);
        }
        return danhSach;
    }

    /** Dịch vụ đang hoạt động, dùng làm danh sách chọn tại quầy. */
    public List<DichVu> layDichVuChoTiepNhan() {
        List<DichVu> danhSach = new ArrayList<>();
        String sql = "WITH DichVuMau AS ("
                + "SELECT d.maDichVu, d.tenDichVu, d.donGia, d.moTa, d.trangThai, d.maLichHen, "
                + "ROW_NUMBER() OVER (PARTITION BY d.tenDichVu ORDER BY lh.thoiGianHen DESC, d.maDichVu DESC) AS thuTu "
                + "FROM DichVu d LEFT JOIN LichHen lh ON lh.maLichHen = d.maLichHen "
                + "WHERE d.trangThai <> N'Ngừng hoạt động') "
                + "SELECT maDichVu, tenDichVu, donGia, moTa, trangThai, maLichHen "
                + "FROM DichVuMau WHERE thuTu = 1 ORDER BY tenDichVu";
        try (Connection con = ConnectDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) danhSach.add(docDichVu(rs));
        } catch (SQLException e) {
            throw new IllegalStateException("Không thể tải danh sách dịch vụ: " + e.getMessage(), e);
        }
        return danhSach;
    }

    /** Tra cứu hồ sơ khách hàng bằng một phần số điện thoại đã nhập. */
    public List<KhachHang> timKhachHangTheoSdt(String soDienThoai) {
        List<KhachHang> danhSach = new ArrayList<>();
        String sql = "SELECT maKH, tenKH, sdt, diaChi, hangThanhVien, diemTichLuy "
                + "FROM KhachHang WHERE REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(COALESCE(sdt, ''), ' ', ''), "
                + "'-', ''), '.', ''), '+', ''), '(', ''), ')', '') LIKE ? ORDER BY tenKH";
        try (Connection con = ConnectDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, "%" + soDienThoai + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    KhachHang khachHang = new KhachHang();
                    khachHang.setMaKH(rs.getString("maKH"));
                    khachHang.setTenKH(rs.getString("tenKH"));
                    khachHang.setSdt(rs.getString("sdt"));
                    khachHang.setDiaChi(rs.getString("diaChi"));
                    khachHang.setHangThanhVien(rs.getInt("hangThanhVien"));
                    khachHang.setDiemTichLuy(rs.getInt("diemTichLuy"));
                    danhSach.add(khachHang);
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Không thể tra cứu khách hàng theo số điện thoại: " + e.getMessage(), e);
        }
        return danhSach;
    }

    public List<ThuCung> layThuCung() {
        List<ThuCung> danhSach = new ArrayList<>();
        String sql = "SELECT maThuCung, tenThuCung, loai, giong, gioiTinh, canNang, ngaySinh, "
                + "tinhTrangSK, tiemChung, maKH FROM ThuCung ORDER BY tenThuCung";
        try (Connection con = ConnectDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                ThuCung thuCung = new ThuCung();
                thuCung.setMaThuCung(rs.getString("maThuCung"));
                thuCung.setTenThuCung(rs.getString("tenThuCung"));
                thuCung.setLoai(rs.getString("loai"));
                thuCung.setGiong(rs.getString("giong"));
                thuCung.setGioiTinh(rs.getString("gioiTinh"));
                float canNang = rs.getFloat("canNang");
                thuCung.setCanNang(rs.wasNull() ? -1 : canNang);
                Date ngaySinh = rs.getDate("ngaySinh");
                thuCung.setNgaySinh(ngaySinh == null ? null : ngaySinh.toLocalDate());
                thuCung.setTinhTrangSK(rs.getString("tinhTrangSK"));
                thuCung.setTiemChung(rs.getString("tiemChung"));
                thuCung.setMaKH(new KhachHang(rs.getString("maKH")));
                danhSach.add(thuCung);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Không thể tải thú cưng: " + e.getMessage(), e);
        }
        return danhSach;
    }

    public List<NhanVien> layNhanVienChamSocConTrong() {
        List<NhanVien> danhSach = new ArrayList<>();
        String sql = "SELECT nv.maNV, nv.tenNV, nv.sdt, nv.chucVu, tk.maTK, tk.tenDangNhap, "
                + "tk.vaiTro, tk.trangThai FROM NhanVien nv "
                + "INNER JOIN TaiKhoan tk ON nv.maTK = tk.maTK "
                + "WHERE tk.tenDangNhap <> N'admin' AND tk.trangThai = N'Hoạt động' "
                + "AND (UPPER(tk.vaiTro) IN (N'CHAM_SOC', N'CHĂM SÓC') OR nv.chucVu LIKE N'%chăm sóc%') "
                + "AND NOT EXISTS (SELECT 1 FROM HoaDon h WHERE h.laTiepNhan = 1 "
                + "AND h.maNVTiepNhan = nv.maNV AND h.trangThaiTiepNhan IN "
                + "(N'Đã tiếp nhận', N'Đang phục vụ', N'Chờ khách đón')) "
                + "AND NOT EXISTS (SELECT 1 FROM LichHen lh WHERE lh.maNV = nv.maNV "
                + "AND lh.trangThai IN (N'Chờ xác nhận', N'Đã xác nhận', N'Đã tiếp nhận', N'Đang thực hiện') "
                + "AND lh.thoiGianHen >= DATEADD(HOUR, -2, GETDATE()) "
                + "AND lh.thoiGianHen < DATEADD(HOUR, 2, GETDATE())) ORDER BY nv.tenNV";
        try (Connection con = ConnectDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                TaiKhoan taiKhoan = new TaiKhoan();
                taiKhoan.setMaTK(rs.getString("maTK"));
                taiKhoan.setTenDangNhap(rs.getString("tenDangNhap"));
                taiKhoan.setVaiTro(rs.getString("vaiTro"));
                taiKhoan.setTrangThai(rs.getString("trangThai"));
                danhSach.add(new NhanVien(rs.getString("maNV"), rs.getString("tenNV"), rs.getString("sdt"),
                        null, null, null, rs.getString("chucVu"), taiKhoan));
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Không thể tải nhân viên: " + e.getMessage(), e);
        }
        return danhSach;
    }

    public List<PhieuTiepNhanThuCung> timChiTietTiepNhan(String maTiepNhan) {
        String sql = SELECT + "WHERE h.laTiepNhan = 1 AND h.maHoaDon = ?";
        try (Connection con = ConnectDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maTiepNhan);
            try (ResultSet rs = ps.executeQuery()) {
                List<PhieuTiepNhanThuCung> danhSach = new ArrayList<>();
                while (rs.next()) danhSach.add(docPhieu(rs));
                return danhSach;
            }
        } catch (SQLException e) {
            throw loiSchema("Không thể đọc thông tin tiếp nhận", e);
        }
    }

    /** Tạo lịch hẹn nếu cần, rồi lưu tiếp nhận và các dịch vụ ngay trên hóa đơn. */
    public boolean them(PhieuTiepNhanThuCung phieu) {
        try (Connection con = ConnectDB.getConnection()) {
            boolean autoCommit = con.getAutoCommit();
            try {
                con.setAutoCommit(false);
                boolean taoLichHenMoi = phieu.getMaLichHen() == null || phieu.getMaLichHen().trim().isEmpty();
                if (taoLichHenMoi) {
                    phieu.setMaLichHen("LH" + UUID.randomUUID().toString().replace("-", "")
                            .substring(0, 18).toUpperCase());
                    try (PreparedStatement ps = con.prepareStatement(
                            "INSERT INTO LichHen (maLichHen, thoiGianHen, trangThai, maNV, maKH, maThuCung) "
                                    + "VALUES (?, ?, N'Đã tiếp nhận', ?, ?, ?)")) {
                        ps.setString(1, phieu.getMaLichHen());
                        ps.setTimestamp(2, Timestamp.valueOf(phieu.getThoiGianTiepNhan()));
                        ps.setString(3, phieu.getMaNV());
                        ps.setString(4, phieu.getMaKH());
                        ps.setString(5, phieu.getMaThuCung());
                        ps.executeUpdate();
                    }
                }

                List<DichVu> dichVuDaGanVaoLich = new ArrayList<>();
                String sqlDichVu = "INSERT INTO DichVu "
                        + "(maDichVu, tenDichVu, donGia, moTa, trangThai, maLichHen) "
                        + "VALUES (?, ?, ?, ?, N'Đã đặt', ?)";
                try (PreparedStatement ps = con.prepareStatement(sqlDichVu)) {
                    for (DichVu dichVu : phieu.getDanhSachDichVu()) {
                        String lichHenGoc = dichVu.getMaLichHen() == null
                                ? null : dichVu.getMaLichHen().getMaLichHen();
                        if (phieu.getMaLichHen().equals(lichHenGoc)) {
                            dichVuDaGanVaoLich.add(dichVu);
                            continue;
                        }
                        String maDichVuMoi = "DV" + UUID.randomUUID().toString().replace("-", "")
                                .substring(0, 18).toUpperCase();
                        ps.setString(1, maDichVuMoi);
                        ps.setString(2, dichVu.getTenDichVu());
                        ps.setBigDecimal(3, dichVu.getDonGia());
                        ps.setString(4, dichVu.getMoTa());
                        ps.setString(5, phieu.getMaLichHen());
                        ps.addBatch();
                        dichVuDaGanVaoLich.add(new DichVu(maDichVuMoi, dichVu.getTenDichVu(), dichVu.getDonGia(),
                                dichVu.getMoTa(), "Đã đặt", new LichHen(phieu.getMaLichHen())));
                    }
                    ps.executeBatch();
                }
                phieu.setDanhSachDichVu(dichVuDaGanVaoLich);

                String maHoaDon = new ThanhToanDAO().taoMaHoaDon(con);
                phieu.setMaTiepNhan(maHoaDon);
                String sqlHoaDon = "INSERT INTO HoaDon (maHoaDon, ngayLapHD, maNV, maKH, maKhuyenMai, maPTTT, "
                        + "laTiepNhan, maLichHen, maThuCung, maNVTiepNhan, thoiGianTiepNhan, gioHenTra, "
                        + "dichVuYeuCau, tinhTrangBanGiao, ghiChuTiepNhan, phiDuKien, trangThaiTiepNhan) "
                        + "VALUES (?, ?, ?, ?, NULL, NULL, 1, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
                try (PreparedStatement ps = con.prepareStatement(sqlHoaDon)) {
                    ps.setString(1, maHoaDon);
                    ps.setDate(2, Date.valueOf(phieu.getThoiGianTiepNhan().toLocalDate()));
                    ps.setString(3, phieu.getMaNV());
                    ps.setString(4, phieu.getMaKH());
                    ps.setString(5, phieu.getMaLichHen());
                    ps.setString(6, phieu.getMaThuCung());
                    ps.setString(7, phieu.getMaNV());
                    ps.setTimestamp(8, Timestamp.valueOf(phieu.getThoiGianTiepNhan()));
                    ps.setTimestamp(9, Timestamp.valueOf(phieu.getGioHenTra()));
                    ps.setString(10, phieu.getDichVuYeuCau());
                    ps.setString(11, phieu.getTinhTrangBanGiao());
                    ps.setString(12, phieu.getGhiChu());
                    ps.setBigDecimal(13, phieu.getPhiDuKien());
                    ps.setString(14, phieu.getTrangThai());
                    ps.executeUpdate();
                }

                String sqlChiTiet = "INSERT INTO ChiTietHoaDon "
                        + "(maHoaDon, maDichVu, maSP, soLuong, donGia, tienKhachDua) "
                        + "VALUES (?, ?, NULL, 1, ?, NULL)";
                try (PreparedStatement ps = con.prepareStatement(sqlChiTiet)) {
                    for (DichVu dichVu : phieu.getDanhSachDichVu()) {
                        ps.setString(1, maHoaDon);
                        ps.setString(2, dichVu.getMaDichVu());
                        ps.setBigDecimal(3, dichVu.getDonGia());
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }

                if (!taoLichHenMoi) {
                    try (PreparedStatement ps = con.prepareStatement(
                            "UPDATE LichHen SET trangThai = N'Đã tiếp nhận' "
                                    + "WHERE maLichHen = ? AND trangThai IN (N'Chờ xác nhận', N'Đã xác nhận')")) {
                        ps.setString(1, phieu.getMaLichHen());
                        if (ps.executeUpdate() != 1) {
                            throw new SQLException("Lịch hẹn đã được tiếp nhận hoặc không còn ở trạng thái chờ.");
                        }
                    }
                }
                con.commit();
                return true;
            } catch (SQLException | RuntimeException e) {
                con.rollback();
                throw e;
            } finally {
                con.setAutoCommit(autoCommit);
            }
        } catch (SQLException e) {
            throw loiSchema("Không thể tạo hóa đơn tiếp nhận", e);
        }
    }

    public boolean capNhatTrangThai(String maTiepNhan, String trangThai) {
        try (Connection con = ConnectDB.getConnection()) {
            boolean autoCommit = con.getAutoCommit();
            try {
                con.setAutoCommit(false);
                String maLichHen;
                try (PreparedStatement ps = con.prepareStatement(
                        "SELECT maLichHen FROM HoaDon WHERE maHoaDon = ? AND laTiepNhan = 1 AND maPTTT IS NULL")) {
                    ps.setString(1, maTiepNhan);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) throw new SQLException("Không tìm thấy hóa đơn tiếp nhận chưa thanh toán.");
                        maLichHen = rs.getString("maLichHen");
                    }
                }
                try (PreparedStatement ps = con.prepareStatement(
                        "UPDATE HoaDon SET trangThaiTiepNhan = ? "
                                + "WHERE maHoaDon = ? AND laTiepNhan = 1 AND maPTTT IS NULL")) {
                    ps.setString(1, trangThai);
                    ps.setString(2, maTiepNhan);
                    if (ps.executeUpdate() != 1) throw new SQLException("Không cập nhật được trạng thái tiếp nhận.");
                }
                String trangThaiHen = trangThaiLichHen(trangThai);
                if (maLichHen != null && trangThaiHen != null) {
                    try (PreparedStatement ps = con.prepareStatement(
                            "UPDATE LichHen SET trangThai = ? WHERE maLichHen = ?")) {
                        ps.setString(1, trangThaiHen);
                        ps.setString(2, maLichHen);
                        ps.executeUpdate();
                    }
                }
                con.commit();
                return true;
            } catch (SQLException | RuntimeException e) {
                con.rollback();
                throw e;
            } finally {
                con.setAutoCommit(autoCommit);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Không thể cập nhật trạng thái: " + e.getMessage(), e);
        }
    }

    /** Hoàn tất hóa đơn tiếp nhận trong cùng transaction với thao tác thanh toán. */
    public void danhDauDaThanhToan(Connection con, String maHoaDon) throws SQLException {
        String maLichHen;
        try (PreparedStatement ps = con.prepareStatement(
                "SELECT maLichHen FROM HoaDon WHERE maHoaDon = ? AND laTiepNhan = 1 "
                        + "AND trangThaiTiepNhan = N'Chờ thanh toán' AND maPTTT IS NOT NULL")) {
            ps.setString(1, maHoaDon);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) throw new SQLException("Hóa đơn " + maHoaDon
                        + " không còn ở trạng thái Chờ thanh toán hoặc chưa ghi nhận phương thức thanh toán.");
                maLichHen = rs.getString("maLichHen");
            }
        }
        try (PreparedStatement ps = con.prepareStatement(
                "UPDATE HoaDon SET trangThaiTiepNhan = N'Hoàn tất' "
                        + "WHERE maHoaDon = ? AND laTiepNhan = 1 AND trangThaiTiepNhan = N'Chờ thanh toán' "
                        + "AND maPTTT IS NOT NULL")) {
            ps.setString(1, maHoaDon);
            if (ps.executeUpdate() != 1) throw new SQLException("Không thể hoàn tất hóa đơn tiếp nhận " + maHoaDon);
        }
        if (maLichHen != null) {
            try (PreparedStatement ps = con.prepareStatement(
                    "UPDATE LichHen SET trangThai = N'Hoàn thành' WHERE maLichHen = ?")) {
                ps.setString(1, maLichHen);
                ps.executeUpdate();
            }
        }
    }

    private String trangThaiLichHen(String trangThaiTiepNhan) {
        if ("Đã tiếp nhận".equals(trangThaiTiepNhan)) return "Đã tiếp nhận";
        if ("Đang phục vụ".equals(trangThaiTiepNhan) || "Chờ khách đón".equals(trangThaiTiepNhan))
            return "Đang thực hiện";
        if ("Hoàn tất".equals(trangThaiTiepNhan)) return "Hoàn thành";
        if ("Đã hủy".equals(trangThaiTiepNhan)) return "Đã hủy";
        return null;
    }

    private PhieuTiepNhanThuCung docPhieu(ResultSet rs) throws SQLException {
        PhieuTiepNhanThuCung phieu = new PhieuTiepNhanThuCung();
        phieu.setMaTiepNhan(rs.getString("maTiepNhan"));
        phieu.setMaLichHen(rs.getString("maLichHen"));
        Timestamp tiepNhan = rs.getTimestamp("thoiGianTiepNhan");
        phieu.setThoiGianTiepNhan(tiepNhan == null ? null : tiepNhan.toLocalDateTime());
        Timestamp henTra = rs.getTimestamp("gioHenTra");
        phieu.setGioHenTra(henTra == null ? null : henTra.toLocalDateTime());
        phieu.setMaNV(rs.getString("maNV"));
        phieu.setTenNV(rs.getString("tenNV"));
        phieu.setMaKH(rs.getString("maKH"));
        phieu.setTenKH(rs.getString("tenKH"));
        phieu.setSdtKH(rs.getString("sdt"));
        phieu.setMaThuCung(rs.getString("maThuCung"));
        phieu.setTenThuCung(rs.getString("tenThuCung"));
        phieu.setLoaiThuCung(rs.getString("loai"));
        phieu.setDichVuYeuCau(rs.getString("dichVuYeuCau"));
        phieu.setTinhTrangBanGiao(rs.getString("tinhTrangBanGiao"));
        phieu.setGhiChu(rs.getString("ghiChu"));
        phieu.setPhiDuKien(rs.getBigDecimal("phiDuKien"));
        phieu.setTrangThai(rs.getString("trangThai"));
        return phieu;
    }

    private DichVu docDichVu(ResultSet rs) throws SQLException {
        DichVu dichVu = new DichVu();
        dichVu.setMaDichVu(rs.getString("maDichVu"));
        dichVu.setTenDichVu(rs.getString("tenDichVu"));
        dichVu.setDonGia(rs.getBigDecimal("donGia"));
        dichVu.setMoTa(rs.getString("moTa"));
        dichVu.setTrangThai(rs.getString("trangThai"));
        String maLichHen = rs.getString("maLichHen");
        if (maLichHen != null) dichVu.setMaLichHen(new LichHen(maLichHen));
        return dichVu;
    }

    private IllegalStateException loiSchema(String hanhDong, SQLException e) {
        String chiTiet = e.getMessage() == null ? "" : e.getMessage();
        if (chiTiet.contains("Invalid column name") || chiTiet.contains("laTiepNhan")
                || chiTiet.contains("maNVTiepNhan") || chiTiet.contains("trangThaiTiepNhan")) {
            return new IllegalStateException(hanhDong + ". Hãy chạy file data/Migration_HoaDon_TiepNhan.sql "
                    + "trong cơ sở dữ liệu PETPLUS. Chi tiết: " + chiTiet, e);
        }
        return new IllegalStateException(hanhDong + ": " + chiTiet, e);
    }
}
