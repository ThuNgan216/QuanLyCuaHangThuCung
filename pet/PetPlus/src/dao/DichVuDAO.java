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
            + "FROM DichVu d ";

    public List<DichVu> layDanhSach() {
        List<DichVu> ds = new ArrayList<>();
        String sql = SELECT + "ORDER BY d.tenDichVu";
        try (Connection con = ConnectDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) ds.add(doc(rs));
        } catch (SQLException e) { throw new IllegalStateException("Không thể tải danh sách dịch vụ: " + e.getMessage(), e); }
        return ds;
    }

    public List<DichVu> timKiem(String tuKhoa) {
        if (tuKhoa == null || tuKhoa.trim().isEmpty()) return layDanhSach();
        List<DichVu> ds = new ArrayList<>();
        String sql = SELECT + "WHERE d.maDichVu LIKE ? OR d.tenDichVu LIKE ? OR d.moTa LIKE ? OR d.trangThai LIKE ? ORDER BY d.tenDichVu";
        String key = "%" + tuKhoa.trim() + "%";
        try (Connection con = ConnectDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            for (int i = 1; i <= 4; i++) ps.setString(i, key);
            try (ResultSet rs = ps.executeQuery()) { while (rs.next()) ds.add(doc(rs)); }
        } catch (SQLException e) { throw new IllegalStateException("Không thể tìm dịch vụ: " + e.getMessage(), e); }
        return ds;
    }

    public String taoMaMoi() {
        String sql = "SELECT ISNULL(MAX(TRY_CONVERT(INT, SUBSTRING(maDichVu, 3, 18))), 0) + 1 AS soMoi "
                + "FROM dbo.DichVu WHERE maDichVu LIKE 'DV[0-9]%'";
        try (Connection con = ConnectDB.getConnection();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            return rs.next() ? String.format(java.util.Locale.ROOT, "DV%03d", rs.getInt("soMoi")) : "DV001";
        } catch (SQLException e) {
            throw new IllegalStateException("Không thể tự sinh mã dịch vụ: " + e.getMessage(), e);
        }
    }

    public boolean them(DichVu d) {
        String sql = "INSERT INTO DichVu(maDichVu, tenDichVu, donGia, moTa, trangThai, maLichHen) VALUES(?,?,?,?,?,?)";
        try (Connection con = ConnectDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            gan(ps, d); return ps.executeUpdate() > 0;
        } catch (SQLException e) { throw new IllegalStateException("Không thể thêm dịch vụ: " + e.getMessage(), e); }
    }

    public boolean capNhat(DichVu d) {
        String sql = "UPDATE DichVu SET tenDichVu=?, donGia=?, moTa=?, trangThai=?, maLichHen=? WHERE maDichVu=?";
        try (Connection con = ConnectDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, d.getTenDichVu()); ps.setBigDecimal(2, d.getDonGia()); ps.setString(3, d.getMoTa());
            ps.setString(4, d.getTrangThai());
            ps.setString(5, d.getMaLichHen() == null ? null : d.getMaLichHen().getMaLichHen());
            ps.setString(6, d.getMaDichVu());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { throw new IllegalStateException("Không thể cập nhật dịch vụ: " + e.getMessage(), e); }
    }

    public boolean xoa(String maDichVu) {
        try (Connection con = ConnectDB.getConnection(); PreparedStatement ps = con.prepareStatement("DELETE FROM DichVu WHERE maDichVu=?")) {
            ps.setString(1, maDichVu); return ps.executeUpdate() > 0;
        } catch (SQLException e) { throw new IllegalStateException("Không thể xóa dịch vụ: " + e.getMessage(), e); }
    }

    private void gan(PreparedStatement ps, DichVu d) throws SQLException {
        ps.setString(1, d.getMaDichVu()); ps.setString(2, d.getTenDichVu()); ps.setBigDecimal(3, d.getDonGia());
        ps.setString(4, d.getMoTa()); ps.setString(5, d.getTrangThai());
        ps.setString(6, d.getMaLichHen() == null ? null : d.getMaLichHen().getMaLichHen());
    }

    private DichVu doc(ResultSet rs) throws SQLException {
        DichVu d = new DichVu(); d.setMaDichVu(rs.getString("maDichVu")); d.setTenDichVu(rs.getString("tenDichVu"));
        d.setDonGia(rs.getBigDecimal("donGia")); d.setMoTa(rs.getString("moTa")); d.setTrangThai(rs.getString("trangThai"));
        String maLichHen = rs.getString("maLichHen");
        d.setMaLichHen(maLichHen == null ? null : new LichHen(maLichHen)); return d;
    }
}
