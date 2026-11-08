package dao;

import database.ConnectDB;
import entity.KhachHang;
import entity.ThuCung;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Truy vấn dữ liệu thú cưng trong SQL Server.
 * Lỗi CSDL được ném ra (IllegalStateException) thay vì nuốt lỗi và trả về false / danh sách rỗng.
 */
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
            throw new IllegalStateException("Không thể tải danh sách thú cưng: " + e.getMessage(), e);
        }
        return ds;
    }

    public ThuCung timTheoMa(String ma) {
        String sql = "SELECT " + COLUMNS + " FROM ThuCung WHERE maThuCung = ? AND trangThai <> N'Đã xóa'";
        try (Connection con = ConnectDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, ma);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? docThuCung(rs) : null;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Không thể đọc thú cưng: " + e.getMessage(), e);
        }
    }

    /** Tìm theo mã, tên, loài, giống thú cưng hoặc tên / số điện thoại của chủ nuôi. */
    public List<ThuCung> timKiem(String tuKhoa) {
        List<ThuCung> ds = new ArrayList<>();
        String sql = "SELECT t." + COLUMNS.replace(", ", ", t.")
                + " FROM ThuCung t INNER JOIN KhachHang k ON t.maKH = k.maKH "
                + "WHERE t.trangThai <> N'Đã xóa' AND k.trangThai <> N'Đã xóa' "
                + "AND (t.maThuCung LIKE ? OR t.tenThuCung LIKE ? OR t.loai LIKE ? OR t.giong LIKE ? "
                + "OR k.tenKH LIKE ? OR k.sdt LIKE ?) ORDER BY t.tenThuCung";
        try (Connection con = ConnectDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            String key = "%" + (tuKhoa == null ? "" : tuKhoa.trim()) + "%";
            for (int i = 1; i <= 6; i++)
                ps.setString(i, key);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next())
                    ds.add(docThuCung(rs));
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Không thể tìm thú cưng: " + e.getMessage(), e);
        }
        return ds;
    }

    /** Phát sinh mã thú cưng theo số lớn nhất hiện có (TC001, TC002...), cùng cách với các DAO khác. */
    public String taoMaMoi() {
        try (Connection con = ConnectDB.getConnection()) {
            return taoMaMoi(con);
        } catch (SQLException e) {
            throw new IllegalStateException("Không thể tạo mã thú cưng: " + e.getMessage(), e);
        }
    }

    private String taoMaMoi(Connection con) throws SQLException {
        return TienIchDAO.sinhMa(con, "ThuCung", "maThuCung", "TC");
    }

    public boolean them(ThuCung t) {
        String sql = """
                INSERT INTO ThuCung
                (maThuCung, tenThuCung, loai, giong, gioiTinh,
                 canNang, ngaySinh, tinhTrangSK, tiemChung, maKH)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
        try (Connection con = ConnectDB.getConnection()) {
            // Sinh mã rồi INSERT là hai bước nên hai người thêm cùng lúc có thể ra cùng mã.
            // Khóa chính sẽ chặn bản ghi trùng; khi đó sinh mã mới và thử lại.
            for (int lanThu = 1; lanThu <= TienIchDAO.SO_LAN_THU_TOI_DA; lanThu++) {
                t.setMaThuCung(taoMaMoi(con));
                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    ganThamSo(ps, t);
                    return ps.executeUpdate() > 0;
                } catch (SQLException e) {
                    if (!TienIchDAO.laTrungRangBuoc(e, "PK_ThuCung") || lanThu == TienIchDAO.SO_LAN_THU_TOI_DA)
                        throw e;
                }
            }
            return false;
        } catch (SQLException e) {
            throw new IllegalStateException("Không thể thêm thú cưng: " + e.getMessage(), e);
        }
    }

    public boolean capNhat(ThuCung t) {
        String sql = "UPDATE ThuCung SET tenThuCung=?, loai=?, giong=?, gioiTinh=?, canNang=?, ngaySinh=?, "
                + "tinhTrangSK=?, tiemChung=?, maKH=? WHERE maThuCung=? AND trangThai <> N'Đã xóa'";
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
            throw new IllegalStateException("Không thể cập nhật thú cưng: " + e.getMessage(), e);
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

    /** Thú cưng còn lịch hẹn chưa kết thúc hay không (dùng chung câu truy vấn của LichHenDAO). */
    public boolean coLichHenChuaKetThuc(String maThuCung) {
        return new LichHenDAO().coLichHenChuaKetThuc("maThuCung", maThuCung);
    }

    /** Chủ nuôi đã có thú cưng cùng tên và cùng loài (chưa bị xóa) hay chưa. */
    public boolean tonTaiTrung(String maKH, String tenThuCung, String loai, String maThuCungBoQua) {
        String sql = "SELECT 1 FROM ThuCung WHERE maKH = ? AND tenThuCung = ? AND loai = ? "
                + "AND trangThai <> N'Đã xóa'" + (maThuCungBoQua == null ? "" : " AND maThuCung <> ?");
        try (Connection con = ConnectDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maKH);
            ps.setString(2, tenThuCung);
            ps.setString(3, loai);
            if (maThuCungBoQua != null)
                ps.setString(4, maThuCungBoQua);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Không thể kiểm tra trùng thú cưng: " + e.getMessage(), e);
        }
    }

    private void ganThamSo(PreparedStatement ps, ThuCung t) throws SQLException {
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
        t.setCanNang(rs.wasNull() ? ThuCung.CAN_NANG_TRONG : canNang);
        Date ngay = rs.getDate("ngaySinh");
        t.setNgaySinh(ngay == null ? null : ngay.toLocalDate());
        t.setTinhTrangSK(rs.getString("tinhTrangSK"));
        t.setTiemChung(rs.getString("tiemChung"));
        t.setMaKH(new KhachHang(rs.getString("maKH")));
        return t;
    }
}
