package dao;

import database.ConnectDB;
import entity.DichVu;
import entity.LichHen;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** Truy vấn dữ liệu dịch vụ trong SQL Server. */
public class DichVuDAO {
    private static final String SELECT = "SELECT d.maDichVu, d.tenDichVu, d.donGia, d.moTa, d.trangThai, d.maLichHen "
            + "FROM DichVu d WHERE d.trangThai <> N'Đã xóa' ";

    public List<DichVu> layDanhSach() {
        List<DichVu> ds = new ArrayList<>();
        String sql = SELECT + "ORDER BY d.tenDichVu";
        try (Connection con = ConnectDB.getConnection();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next())
                ds.add(doc(rs));
        } catch (SQLException e) {
            throw new IllegalStateException("Không thể tải danh sách dịch vụ: " + e.getMessage(), e);
        }
        return ds;
    }

    public List<DichVu> timKiem(String tuKhoa) {
        if (tuKhoa == null || tuKhoa.trim().isEmpty())
            return layDanhSach();
        List<DichVu> ds = new ArrayList<>();
        String sql = SELECT
                + "AND (d.maDichVu LIKE ? OR d.tenDichVu LIKE ? OR d.moTa LIKE ? OR d.trangThai LIKE ?) ORDER BY d.tenDichVu";
        String key = "%" + tuKhoa.trim() + "%";
        try (Connection con = ConnectDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            for (int i = 1; i <= 4; i++)
                ps.setString(i, key);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next())
                    ds.add(doc(rs));
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Không thể tìm dịch vụ: " + e.getMessage(), e);
        }
        return ds;
    }

    /** Mã gợi ý để hiển thị; mã thật được sinh lại khi lưu (xem {@link #them}). */
    public String taoMaMoi() {
        try (Connection con = ConnectDB.getConnection()) {
            return taoMaMoi(con);
        } catch (SQLException e) {
            throw new IllegalStateException("Không thể tự sinh mã dịch vụ: " + e.getMessage(), e);
        }
    }

    private String taoMaMoi(Connection con) throws SQLException {
        return TienIchDAO.sinhMa(con, "DichVu", "maDichVu", "DV");
    }

    /** Thêm dịch vụ; mã được sinh tại đây và sinh lại nếu hai người thêm cùng lúc bị trùng mã. */
    public boolean them(DichVu d) {
        String sql = "INSERT INTO DichVu(maDichVu, tenDichVu, donGia, moTa, trangThai, maLichHen) VALUES(?,?,?,?,?,?)";
        try (Connection con = ConnectDB.getConnection()) {
            for (int lanThu = 1; lanThu <= TienIchDAO.SO_LAN_THU_TOI_DA; lanThu++) {
                d.setMaDichVu(taoMaMoi(con));
                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    gan(ps, d);
                    return ps.executeUpdate() > 0;
                } catch (SQLException e) {
                    if (!TienIchDAO.laTrungRangBuoc(e, "PK_DichVu") || lanThu == TienIchDAO.SO_LAN_THU_TOI_DA)
                        throw e;
                }
            }
            return false;
        } catch (SQLException e) {
            throw new IllegalStateException("Không thể thêm dịch vụ: " + e.getMessage(), e);
        }
    }

    /** Tìm một dịch vụ (chưa bị xóa) theo mã; null nếu không có. */
    public DichVu timTheoMa(String ma) {
        String sql = SELECT + "AND d.maDichVu = ?";
        try (Connection con = ConnectDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, ma);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? doc(rs) : null;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Không thể đọc dịch vụ: " + e.getMessage(), e);
        }
    }

    /** Danh mục dịch vụ (không gắn lịch hẹn) đã có dịch vụ cùng tên hay chưa. */
    public boolean tonTaiTenDanhMuc(String tenDichVu, String maBoQua) {
        String sql = "SELECT 1 FROM DichVu WHERE maLichHen IS NULL AND trangThai <> N'Đã xóa' AND tenDichVu = ?"
                + (maBoQua == null ? "" : " AND maDichVu <> ?");
        try (Connection con = ConnectDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, tenDichVu);
            if (maBoQua != null)
                ps.setString(2, maBoQua);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Không thể kiểm tra trùng tên dịch vụ: " + e.getMessage(), e);
        }
    }

    /** Dịch vụ đã nằm trong chi tiết hóa đơn hay chưa (khi đó không được đổi tên / đơn giá). */
    public boolean daDungTrongHoaDon(String maDichVu) {
        String sql = "SELECT 1 FROM ChiTietHoaDon WHERE maDichVu = ?";
        try (Connection con = ConnectDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maDichVu);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Không thể kiểm tra hóa đơn của dịch vụ: " + e.getMessage(), e);
        }
    }

    /** Dịch vụ gắn với lịch hẹn chưa kết thúc: trả về mã lịch hẹn đó, hoặc null nếu không có. */
    public String layLichHenChuaKetThuc(String maDichVu) {
        String sql = "SELECT l.maLichHen FROM DichVu d INNER JOIN LichHen l ON l.maLichHen = d.maLichHen "
                + "WHERE d.maDichVu = ? AND l.trangThai NOT IN " + TienIchDAO.LICH_HEN_KHONG_CON_HIEU_LUC;
        try (Connection con = ConnectDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maDichVu);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getString(1) : null;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Không thể kiểm tra lịch hẹn của dịch vụ: " + e.getMessage(), e);
        }
    }

    public boolean capNhat(DichVu d) {
        String sql = "UPDATE DichVu SET tenDichVu=?, donGia=?, moTa=?, trangThai=?, maLichHen=? WHERE maDichVu=? AND trangThai <> N'Đã xóa'";
        try (Connection con = ConnectDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, d.getTenDichVu());
            ps.setBigDecimal(2, d.getDonGia());
            ps.setString(3, d.getMoTa());
            ps.setString(4, d.getTrangThai());
            ps.setString(5, d.getMaLichHen() == null ? null : d.getMaLichHen().getMaLichHen());
            ps.setString(6, d.getMaDichVu());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new IllegalStateException("Không thể cập nhật dịch vụ: " + e.getMessage(), e);
        }
    }

    public boolean xoa(String maDichVu) {
        try (Connection con = ConnectDB.getConnection()) {
            return DeletePolicy.xoaHoacAn(con,
                    "SELECT 1 FROM DichVu WITH (XLOCK, HOLDLOCK) WHERE maDichVu = ? AND trangThai <> N'Đã xóa'",
                    new String[] { "SELECT 1 FROM ChiTietHoaDon WHERE maDichVu = ?" },
                    "UPDATE DichVu SET trangThai = N'Đã xóa' WHERE maDichVu = ? AND trangThai <> N'Đã xóa'",
                    "DELETE FROM DichVu WHERE maDichVu = ? AND trangThai <> N'Đã xóa'",
                    maDichVu);
        } catch (SQLException e) {
            throw new IllegalStateException("Không thể xóa hoặc ẩn dịch vụ: " + e.getMessage(), e);
        }
    }

    private void gan(PreparedStatement ps, DichVu d) throws SQLException {
        ps.setString(1, d.getMaDichVu());
        ps.setString(2, d.getTenDichVu());
        ps.setBigDecimal(3, d.getDonGia());
        ps.setString(4, d.getMoTa());
        ps.setString(5, d.getTrangThai());
        ps.setString(6, d.getMaLichHen() == null ? null : d.getMaLichHen().getMaLichHen());
    }

    private DichVu doc(ResultSet rs) throws SQLException {
        DichVu d = new DichVu();
        d.setMaDichVu(rs.getString("maDichVu"));
        d.setTenDichVu(rs.getString("tenDichVu"));
        d.setDonGia(rs.getBigDecimal("donGia"));
        d.setMoTa(rs.getString("moTa"));
        d.setTrangThai(rs.getString("trangThai"));
        String maLichHen = rs.getString("maLichHen");
        d.setMaLichHen(maLichHen == null ? null : new LichHen(maLichHen));
        return d;
    }
}
