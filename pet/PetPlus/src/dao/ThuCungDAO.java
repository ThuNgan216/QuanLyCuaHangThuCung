package dao;

import database.ConnectDB;
import entity.KhachHang;
import entity.ThuCung;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/** Truy vấn dữ liệu thú cưng trong SQL Server. */
public class ThuCungDAO {
    private static final String COLUMNS = "maThuCung, tenThuCung, loai, giong, gioiTinh, canNang, ngaySinh, tinhTrangSK, tiemChung, maKH";

    public List<ThuCung> layDanhSach() {
        List<ThuCung> ds = new ArrayList<>();
        String sql = "SELECT " + COLUMNS + " FROM ThuCung WHERE trangThai <> N'Đã xóa' "
                + "AND maKH IN (SELECT maKH FROM KhachHang WHERE trangThai <> N'Đã xóa') ORDER BY tenThuCung";
        try (Connection con = ConnectDB.getConnection();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next())
                ds.add(docThuCung(rs));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ds;
    }

    public ThuCung timTheoMa(String ma) {
        String sql = "SELECT " + COLUMNS + " FROM ThuCung WHERE maThuCung = ? AND trangThai <> N'Đã xóa'";
        try (Connection con = ConnectDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, ma);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next())
                    return docThuCung(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<ThuCung> timKiem(String tuKhoa) {
        List<ThuCung> ds = new ArrayList<>();
        String sql = "SELECT t." + COLUMNS.replace(", ", ", t.")
                + " FROM ThuCung t LEFT JOIN KhachHang k ON t.maKH=k.maKH WHERE t.trangThai <> N'Đã xóa' AND k.trangThai <> N'Đã xóa' AND k.sdt LIKE ? ORDER BY t.tenThuCung";
        try (Connection con = ConnectDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, "%" + (tuKhoa == null ? "" : tuKhoa.trim()) + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next())
                    ds.add(docThuCung(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Không thể tìm thú cưng theo số điện thoại khách hàng.", e);
        }
        return ds;
    }

    /** Số lần thử lại tối đa khi hai người thêm cùng lúc và sinh trùng mã. */
    private static final int SO_LAN_THU_TOI_DA = 3;

    /**
     * Phát sinh mã thú cưng theo số lớn nhất hiện có (TC01, TC02...), cùng cách với
     * KhachHangDAO.
     */
    public String taoMaTuDong() {
        try (Connection con = ConnectDB.getConnection()) {
            return taoMaTuDong(con);
        } catch (SQLException e) {
            throw new RuntimeException("Không thể tạo mã thú cưng.", e);
        }
    }

    // Tính số lớn nhất ngay trong SQL (kể cả thú cưng đã ẩn, để không dùng lại mã
    // cũ).
    private String taoMaTuDong(Connection con) throws SQLException {
        String sql = "SELECT MAX(TRY_CONVERT(INT, SUBSTRING(maThuCung, 3, 18))) "
                + "FROM ThuCung WHERE maThuCung LIKE 'TC[0-9]%'";
        try (PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            int max = rs.next() ? rs.getInt(1) : 0;
            return String.format(java.util.Locale.ROOT, "TC%02d", max + 1);
        }
    }

    public boolean them(ThuCung t) {
        String sql = """
                INSERT INTO ThuCung
                (maThuCung, tenThuCung, loai, giong, gioiTinh,
                 canNang, ngaySinh, tinhTrangSK, tiemChung, maKH)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection con = ConnectDB.getConnection()) {
            // Sinh mã rồi INSERT là hai bước, nên hai người thêm cùng lúc có thể ra cùng
            // mã.
            // Khóa chính trong database sẽ chặn bản ghi trùng; khi đó sinh mã mới và thử
            // lại.
            for (int lanThu = 1; lanThu <= SO_LAN_THU_TOI_DA; lanThu++) {
                t.setMaThuCung(taoMaTuDong(con));
                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    ganThamSo(ps, t, false);
                    return ps.executeUpdate() > 0;
                } catch (SQLException e) {
                    if (!laTrungKhoaChinh(e) || lanThu == SO_LAN_THU_TOI_DA) {
                        throw e;
                    }
                }
            }
            return false;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 2627: vi phạm PRIMARY KEY/UNIQUE constraint, 2601: trùng giá trị trong unique
     * index.
     */
    private boolean laTrungKhoaChinh(SQLException e) {
        return e.getErrorCode() == 2627 || e.getErrorCode() == 2601;
    }

    public boolean capNhat(ThuCung t) {
        String sql = "UPDATE ThuCung SET tenThuCung=?, loai=?, giong=?, gioiTinh=?, canNang=?, ngaySinh=?, tinhTrangSK=?, tiemChung=?, maKH=? WHERE maThuCung=? AND trangThai <> N'Đã xóa'";
        try (Connection con = ConnectDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, t.getTenThuCung());
            ps.setString(2, t.getLoai());
            ps.setString(3, t.getGiong());
            ps.setString(4, t.getGioiTinh());
            setCanNang(ps, 5, t);
            setNgaySinh(ps, 6, t);
            ps.setString(7, t.getTinhTrangSK());
            ps.setString(8, t.getTiemChung());
            ps.setString(9, t.getMaKH().getMaKH());
            ps.setString(10, t.getMaThuCung());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean xoa(String ma) {
        try (Connection con = ConnectDB.getConnection()) {
            return DeletePolicy.xoaHoacAn(con,
                    "SELECT 1 FROM ThuCung WITH (XLOCK, HOLDLOCK) WHERE maThuCung = ? AND trangThai <> N'Đã xóa'",
                    new String[] {
                            "SELECT 1 FROM LichHen WHERE maThuCung = ?",
                            "SELECT 1 FROM HoaDon WHERE maThuCung = ?"
                    },
                    "UPDATE ThuCung SET trangThai = N'Đã xóa' WHERE maThuCung = ? AND trangThai <> N'Đã xóa'",
                    "DELETE FROM ThuCung WHERE maThuCung = ? AND trangThai <> N'Đã xóa'",
                    ma);
        } catch (SQLException e) {
            throw new IllegalStateException("Không thể xóa hoặc ẩn thú cưng: " + e.getMessage(), e);
        }
    }

    private void ganThamSo(PreparedStatement ps, ThuCung t, boolean update) throws SQLException {
        ps.setString(1, t.getMaThuCung());
        ps.setString(2, t.getTenThuCung());
        ps.setString(3, t.getLoai());
        ps.setString(4, t.getGiong());
        ps.setString(5, t.getGioiTinh());
        setCanNang(ps, 6, t);
        setNgaySinh(ps, 7, t);
        ps.setString(8, t.getTinhTrangSK());
        ps.setString(9, t.getTiemChung());
        ps.setString(10, t.getMaKH().getMaKH());
    }

    private void setCanNang(PreparedStatement ps, int index, ThuCung t) throws SQLException {
        if (t.getCanNang() < 0)
            ps.setNull(index, Types.FLOAT);
        else
            ps.setFloat(index, t.getCanNang());
    }

    private void setNgaySinh(PreparedStatement ps, int index, ThuCung t) throws SQLException {
        if (t.getNgaySinh() == null)
            ps.setNull(index, Types.DATE);
        else
            ps.setDate(index, Date.valueOf(t.getNgaySinh()));
    }

    private ThuCung docThuCung(ResultSet rs) throws SQLException {
        ThuCung t = new ThuCung();
        t.setMaThuCung(rs.getString("maThuCung"));
        t.setTenThuCung(rs.getString("tenThuCung"));
        t.setLoai(rs.getString("loai"));
        t.setGiong(rs.getString("giong"));
        t.setGioiTinh(rs.getString("gioiTinh"));
        float canNang = rs.getFloat("canNang");
        t.setCanNang(rs.wasNull() ? -1 : canNang);
        Date ngay = rs.getDate("ngaySinh");
        t.setNgaySinh(ngay == null ? null : ngay.toLocalDate());
        t.setTinhTrangSK(rs.getString("tinhTrangSK"));
        t.setTiemChung(rs.getString("tiemChung"));
        t.setMaKH(new KhachHang(rs.getString("maKH")));
        return t;
    }
}
