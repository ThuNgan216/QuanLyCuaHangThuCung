
package dao;

import database.ConnectDB;
import entity.DichVu;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Truy vấn danh mục dịch vụ và bảng giá dịch vụ.
 */
public class DichVuDAO {

    private static final String SELECT = "SELECT maDichVu, tenDichVu, donGia, moTa, trangThai "
            + "FROM dbo.DichVu ";

    public List<DichVu> layDanhSach() {
        return truyVanDanhSach(
                SELECT + " ORDER BY tenDichVu");
    }

    public List<DichVu> timKiem(String tuKhoa) {
        if (tuKhoa == null || tuKhoa.trim().isEmpty()) {
            return layDanhSach();
        }

        String sql = SELECT + " WHERE tenDichVu LIKE ? ORDER BY tenDichVu";
        String key = "%" + tuKhoa.trim() + "%";
        try (Connection con = ConnectDB.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, key);

            try (ResultSet rs = ps.executeQuery()) {
                List<DichVu> ds = new ArrayList<>();

                while (rs.next()) {
                    ds.add(doc(rs));
                }

                return ds;
            }

        } catch (SQLException e) {
            throw new IllegalStateException(
                    "Không thể tìm dịch vụ: " + e.getMessage(), e);
        }
    }

    private List<DichVu> truyVanDanhSach(String sql) {
        List<DichVu> ds = new ArrayList<>();

        try (Connection con = ConnectDB.getConnection();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                ds.add(doc(rs));
            }

        } catch (SQLException e) {
            throw new IllegalStateException(
                    "Không thể tải danh sách dịch vụ: "
                            + e.getMessage(),
                    e);
        }

        return ds;
    }

    /**
     * Tạo mã DV01, DV02, DV03...
     *
     * Lưu ý: mã cũ dạng DV_CATLONG, DV_TAMCHO không được
     * tính là mã số. Nếu còn DV005 thì mã tiếp theo là DV06.
     */
    public String taoMaMoi() {
        String sql = """
                SELECT ISNULL(
                    MAX(TRY_CONVERT(INT, SUBSTRING(maDichVu, 3, 18))),
                    0
                ) + 1 AS soMoi
                FROM dbo.DichVu
                WHERE maDichVu LIKE 'DV%'
                  AND TRY_CONVERT(
                      INT, SUBSTRING(maDichVu, 3, 18)
                  ) IS NOT NULL
                """;

        try (Connection con = ConnectDB.getConnection();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return String.format(
                        java.util.Locale.ROOT,
                        "DV%02d",
                        rs.getInt("soMoi"));
            }

            return "DV01";

        } catch (SQLException e) {
            throw new IllegalStateException(
                    "Không thể tạo mã dịch vụ: " + e.getMessage(), e);
        }
    }

    public boolean them(DichVu d) {
        String sql = """
                INSERT INTO dbo.DichVu
                    (maDichVu, tenDichVu, donGia, moTa, trangThai)
                VALUES (?, ?, ?, ?, ?)
                """;

        try (Connection con = ConnectDB.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {

            gan(ps, d);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new IllegalStateException(
                    "Không thể thêm dịch vụ: " + e.getMessage(), e);
        }
    }

    public boolean capNhat(DichVu d) {
        String sql = """
                UPDATE dbo.DichVu
                SET tenDichVu = ?,
                    donGia = ?,
                    moTa = ?,
                    trangThai = ?
                WHERE maDichVu = ?
                """;

        try (Connection con = ConnectDB.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, d.getTenDichVu());
            ps.setBigDecimal(2, d.getDonGia());
            ps.setString(3, d.getMoTa());
            ps.setString(4, d.getTrangThai());
            ps.setString(5, d.getMaDichVu());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new IllegalStateException(
                    "Không thể cập nhật dịch vụ: " + e.getMessage(), e);
        }
    }

    public boolean xoa(String ma) {
        String sqlKiemTra = """
                SELECT COUNT(*)
                FROM dbo.ChiTietHoaDon
                WHERE maDichVu = ?
                """;

        try (Connection con = ConnectDB.getConnection()) {

            // Kiểm tra dịch vụ đã phát sinh hóa đơn chưa
            try (PreparedStatement ps = con.prepareStatement(sqlKiemTra)) {
                ps.setString(1, ma);

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {

                        String sqlNgung = """
                                UPDATE dbo.DichVu
                                SET trangThai = N'Ngừng hoạt động'
                                WHERE maDichVu = ?
                                """;

                        try (PreparedStatement psNgung = con.prepareStatement(sqlNgung)) {
                            psNgung.setString(1, ma);
                            return psNgung.executeUpdate() > 0;
                        }
                    }
                }
            }

            // Chưa được sử dụng thì xóa
            String sqlXoa = """
                    DELETE FROM dbo.DichVu
                    WHERE maDichVu = ?
                    """;

            try (PreparedStatement ps = con.prepareStatement(sqlXoa)) {
                ps.setString(1, ma);
                return ps.executeUpdate() > 0;
            }

        } catch (SQLException e) {
            throw new IllegalStateException(
                    "Không thể xử lý dịch vụ: " + e.getMessage(), e);
        }
    }

    /**
     * Tra giá theo tên dịch vụ, loài, cân nặng và độ dài lông.
     * Trả về null khi chưa có mức giá phù hợp.
     */

    public BigDecimal layGia(
            String maDichVu,
            String loaiThuCung,
            BigDecimal canNang,
            String doDaiLong) {

        if (maDichVu == null || maDichVu.isBlank()
                || loaiThuCung == null || loaiThuCung.isBlank()
                || canNang == null
                || doDaiLong == null || doDaiLong.isBlank()
                || canNang.compareTo(BigDecimal.ZERO) < 0) {
            return null;
        }

        String sql = """
                SELECT TOP 1 b.donGia
                FROM dbo.BangGiaDichVu b
                WHERE b.maDichVu = ?
                  AND LTRIM(RTRIM(b.loaiThuCung)) = ?
                  AND ? >= b.canNangTu
                  AND ? < b.canNangDen
                  AND (
                        LTRIM(RTRIM(b.doDaiLong)) = ?
                        OR LTRIM(RTRIM(b.doDaiLong)) = N'Không áp dụng'
                  )
                ORDER BY
                  CASE
                    WHEN LTRIM(RTRIM(b.doDaiLong)) = ? THEN 0
                    ELSE 1
                  END,
                  b.maBangGia
                """;

        try (Connection con = ConnectDB.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, maDichVu.trim());
            ps.setString(2, loaiThuCung.trim());
            ps.setBigDecimal(3, canNang);
            ps.setBigDecimal(4, canNang);
            ps.setString(5, doDaiLong.trim());
            ps.setString(6, doDaiLong.trim());

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getBigDecimal("donGia") : null;
            }

        } catch (SQLException e) {
            throw new IllegalStateException(
                    "Lỗi truy vấn bảng giá dịch vụ: " + e.getMessage(), e);
        }
    }

    private void gan(PreparedStatement ps, DichVu d)
            throws SQLException {

        ps.setString(1, d.getMaDichVu());
        ps.setString(2, d.getTenDichVu());
        ps.setBigDecimal(3, d.getDonGia());
        ps.setString(4, d.getMoTa());
        ps.setString(5, d.getTrangThai());
    }

    private DichVu doc(ResultSet rs) throws SQLException {
        DichVu d = new DichVu();

        d.setMaDichVu(rs.getString("maDichVu"));
        d.setTenDichVu(rs.getString("tenDichVu"));
        d.setDonGia(rs.getBigDecimal("donGia"));
        d.setMoTa(rs.getString("moTa"));
        d.setTrangThai(rs.getString("trangThai"));

        return d;
    }

public boolean kiemTraTrungTen(String tenDichVu, String maBoQua) {
    String sql;

    if (maBoQua == null || maBoQua.trim().isEmpty()) {
        sql = """
                SELECT COUNT(*)
                FROM dbo.DichVu
                WHERE LOWER(LTRIM(RTRIM(tenDichVu))) =
                      LOWER(LTRIM(RTRIM(?)))
                """;
    } else {
        sql = """
                SELECT COUNT(*)
                FROM dbo.DichVu
                WHERE LOWER(LTRIM(RTRIM(tenDichVu))) =
                      LOWER(LTRIM(RTRIM(?)))
                  AND maDichVu <> ?
                """;
    }

    try (Connection con = ConnectDB.getConnection();
         PreparedStatement ps = con.prepareStatement(sql)) {

        ps.setString(1, tenDichVu.trim());

        if (maBoQua != null && !maBoQua.trim().isEmpty()) {
            ps.setString(2, maBoQua.trim());
        }

        try (ResultSet rs = ps.executeQuery()) {
            return rs.next() && rs.getInt(1) > 0;
        }

    } catch (SQLException e) {
        throw new IllegalStateException(
                "Không thể kiểm tra tên dịch vụ: " + e.getMessage(), e);
    }
}

}
