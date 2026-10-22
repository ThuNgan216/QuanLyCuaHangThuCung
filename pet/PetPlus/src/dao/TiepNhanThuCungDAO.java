package dao;

import database.ConnectDB;
import entity.DichVu;
import entity.KhachHang;
import entity.LichHen;
import entity.LichHen;
import entity.NhanVien;
import entity.PhieuTiepNhanThuCung;
import entity.TaiKhoan;
import entity.ThuCung;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Đọc và ghi các phiếu tiếp nhận thú cưng. */
public class TiepNhanThuCungDAO {
    private static final String SELECT =
            "SELECT tn.maTiepNhan, tn.maLichHen, tn.thoiGianTiepNhan, tn.gioHenTra, "
            + "tn.maNV, nv.tenNV, tn.maKH, kh.tenKH, kh.sdt, tn.maThuCung, tc.tenThuCung, tc.loai, "
            + "tn.dichVuYeuCau, tn.tinhTrangBanGiao, tn.ghiChu, tn.phiDuKien, tn.trangThai "
            + "FROM TiepNhanThuCung tn "
            + "INNER JOIN NhanVien nv ON tn.maNV = nv.maNV "
            + "INNER JOIN KhachHang kh ON tn.maKH = kh.maKH "
            + "INNER JOIN ThuCung tc ON tn.maThuCung = tc.maThuCung ";

    public List<PhieuTiepNhanThuCung> layDanhSach(String tuKhoa) {
        String sql = SELECT;
        String key = tuKhoa == null ? "" : tuKhoa.trim();
        if (!key.isEmpty()) {
            sql += "WHERE tn.trangThai <> N'Chờ thanh toán' AND (tn.maTiepNhan LIKE ? OR tn.maLichHen LIKE ? OR kh.tenKH LIKE ? "
                    + "OR kh.sdt LIKE ? OR tc.tenThuCung LIKE ? OR tn.dichVuYeuCau LIKE ? "
                    + "OR tn.trangThai LIKE ?) ";
        } else {
            sql += "WHERE tn.trangThai <> N'Chờ thanh toán' ";
        }
        sql += "ORDER BY tn.thoiGianTiepNhan DESC";

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

    /** Lịch đang chờ/đã xác nhận chưa được check-in và chưa có phiếu tiếp nhận. */
    public List<LichHen> layLichHenChoTiepNhan() {
        String sql = "SELECT l.maLichHen, l.thoiGianHen, l.trangThai, l.maNV, nv.tenNV, "
                + "l.maKH, kh.tenKH, l.maThuCung, tc.tenThuCung "
                + "FROM LichHen l "
                + "INNER JOIN NhanVien nv ON l.maNV = nv.maNV "
                + "INNER JOIN KhachHang kh ON l.maKH = kh.maKH "
                + "INNER JOIN ThuCung tc ON l.maThuCung = tc.maThuCung "
                + "WHERE l.trangThai = N'Đã xác nhận' "
                + "AND NOT EXISTS (SELECT 1 FROM TiepNhanThuCung tn WHERE tn.maLichHen = l.maLichHen) "
                + "ORDER BY l.thoiGianHen";
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
        String sql = "SELECT d.maDichVu, d.tenDichVu, c.donGia, d.moTa, d.trangThai "
                + "FROM ChiTietLichHenDichVu c JOIN DichVu d ON d.maDichVu = c.maDichVu "
                + "WHERE c.maLichHen = ? ORDER BY d.tenDichVu";
        try (Connection con = ConnectDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maLichHen);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    danhSach.add(docDichVu(rs));
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Không thể tải dịch vụ của lịch hẹn: " + e.getMessage(), e);
        }
        return danhSach;
    }

    /** Danh mục dịch vụ đang hoạt động để nhân viên chọn khi tiếp nhận. */
    public List<DichVu> layDichVuChoTiepNhan() {
        List<DichVu> danhSach = new ArrayList<>();
        String sql = "SELECT maDichVu, tenDichVu, donGia, moTa, trangThai FROM DichVu "
                + "WHERE trangThai = N'Đang hoạt động' ORDER BY tenDichVu";
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
                + "FROM KhachHang "
                + "WHERE REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(COALESCE(sdt, ''), ' ', ''), "
                + "'-', ''), '.', ''), '+', ''), '(', ''), ')', '') LIKE ? "
                + "ORDER BY tenKH";
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
                java.sql.Date ngaySinh = rs.getDate("ngaySinh");
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
                + "AND (UPPER(tk.vaiTro) IN (N'CHAM_SOC', N'CHĂM SÓC') "
                + "OR nv.chucVu LIKE N'%chăm sóc%') "
                + "AND NOT EXISTS (SELECT 1 FROM TiepNhanThuCung tn "
                + "WHERE tn.maNV = nv.maNV AND tn.trangThai IN "
                + "(N'Đã tiếp nhận', N'Đang phục vụ', N'Chờ khách đón')) "
                + "AND NOT EXISTS (SELECT 1 FROM LichHen lh WHERE lh.maNV = nv.maNV "
                + "AND lh.trangThai IN (N'Chờ xác nhận', N'Đã xác nhận', N'Đã tiếp nhận', N'Đang thực hiện') "
                + "AND lh.thoiGianHen >= DATEADD(HOUR, -2, GETDATE()) "
                + "AND lh.thoiGianHen < DATEADD(HOUR, 2, GETDATE())) "
                + "ORDER BY nv.tenNV";
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
        String sql = SELECT + "WHERE tn.maTiepNhan = ?";
        try (Connection con = ConnectDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maTiepNhan);
            try (ResultSet rs = ps.executeQuery()) {
                List<PhieuTiepNhanThuCung> ds = new ArrayList<>();
                while (rs.next()) ds.add(docPhieu(rs));
                return ds;
            }
        } catch (SQLException e) {
            throw loiSchema("Không thể đọc phiếu tiếp nhận", e);
        }
    }

    public boolean them(PhieuTiepNhanThuCung phieu) {
        String sql = "INSERT INTO TiepNhanThuCung "
                + "(maTiepNhan, maLichHen, thoiGianTiepNhan, gioHenTra, maNV, maKH, maThuCung, "
                + "dichVuYeuCau, tinhTrangBanGiao, ghiChu, phiDuKien, trangThai) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
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
                String sqlDichVu = "IF NOT EXISTS (SELECT 1 FROM ChiTietLichHenDichVu WHERE maLichHen=? AND maDichVu=?) "
                        + "INSERT INTO ChiTietLichHenDichVu (maLichHen, maDichVu, donGia) VALUES (?, ?, ?)";
                for (DichVu dichVu : phieu.getDanhSachDichVu()) {
                    try (PreparedStatement ps = con.prepareStatement(sqlDichVu)) {
                        ps.setString(1, phieu.getMaLichHen());
                        ps.setString(2, dichVu.getMaDichVu());
                        ps.setString(3, phieu.getMaLichHen());
                        ps.setString(4, dichVu.getMaDichVu());
                        ps.setBigDecimal(5, dichVu.getDonGia());
                        ps.executeUpdate();
                    }
                    dichVuDaGanVaoLich.add(dichVu);
                }
                phieu.setDanhSachDichVu(dichVuDaGanVaoLich);

                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setString(1, phieu.getMaTiepNhan());
                    ps.setString(2, phieu.getMaLichHen());
                    ps.setTimestamp(3, Timestamp.valueOf(phieu.getThoiGianTiepNhan()));
                    ps.setTimestamp(4, Timestamp.valueOf(phieu.getGioHenTra()));
                    ps.setString(5, phieu.getMaNV());
                    ps.setString(6, phieu.getMaKH());
                    ps.setString(7, phieu.getMaThuCung());
                    ps.setString(8, phieu.getDichVuYeuCau());
                    ps.setString(9, phieu.getTinhTrangBanGiao());
                    ps.setString(10, phieu.getGhiChu());
                    ps.setBigDecimal(11, phieu.getPhiDuKien());
                    ps.setString(12, phieu.getTrangThai());
                    ps.executeUpdate();
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
            throw new IllegalStateException("Không thể tạo phiếu tiếp nhận: " + e.getMessage(), e);
        }
    }

    public boolean capNhatTrangThai(String maTiepNhan, String trangThai) {
        String sql = "UPDATE TiepNhanThuCung SET trangThai = ? WHERE maTiepNhan = ?";
        try (Connection con = ConnectDB.getConnection()) {
            boolean autoCommit = con.getAutoCommit();
            try {
                con.setAutoCommit(false);
                String maLichHen;
                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setString(1, trangThai);
                    ps.setString(2, maTiepNhan);
                    if (ps.executeUpdate() != 1) throw new SQLException("Không tìm thấy phiếu tiếp nhận.");
                }
                try (PreparedStatement ps = con.prepareStatement(
                        "SELECT maLichHen FROM TiepNhanThuCung WHERE maTiepNhan = ?")) {
                    ps.setString(1, maTiepNhan);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) throw new SQLException("Không đọc được phiếu tiếp nhận vừa cập nhật.");
                        maLichHen = rs.getString("maLichHen");
                    }
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

    /** Chốt trạng thái phiếu trong cùng transaction tạo hóa đơn. */
    public void danhDauDaThanhToan(Connection con, String maTiepNhan) throws SQLException {
        String sql = "UPDATE TiepNhanThuCung SET trangThai = N'Hoàn tất' "
                + "WHERE maTiepNhan = ? AND trangThai = N'Chờ thanh toán'";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maTiepNhan);
            if (ps.executeUpdate() != 1) {
                throw new SQLException("Phiếu " + maTiepNhan
                        + " không còn ở trạng thái Chờ thanh toán hoặc đã được thanh toán.");
            }
        }
        String maLichHen = null;
        try (PreparedStatement ps = con.prepareStatement(
                "SELECT maLichHen FROM TiepNhanThuCung WHERE maTiepNhan = ?")) {
            ps.setString(1, maTiepNhan);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) maLichHen = rs.getString("maLichHen");
            }
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
        return dichVu;
    }

    private IllegalStateException loiSchema(String hanhDong, SQLException e) {
        if (e.getMessage() != null && e.getMessage().contains("TiepNhanThuCung")) {
            return new IllegalStateException(hanhDong + ". Hãy chạy file data/Migration_TiepNhanThuCung.sql "
                    + "trong cơ sở dữ liệu PETPLUS. Chi tiết: " + e.getMessage(), e);
        }
        return new IllegalStateException(hanhDong + ": " + e.getMessage(), e);
    }
}
