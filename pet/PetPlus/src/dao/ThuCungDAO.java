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
        String sql = "SELECT " + COLUMNS + " FROM ThuCung ORDER BY tenThuCung";
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
        String sql = "SELECT " + COLUMNS + " FROM ThuCung WHERE maThuCung = ?";
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
        String sql = "SELECT t." + COLUMNS.replace(", ", ", t.") + " FROM ThuCung t LEFT JOIN KhachHang k ON t.maKH=k.maKH WHERE k.sdt LIKE ? ORDER BY t.tenThuCung";
        try (Connection con=ConnectDB.getConnection(); PreparedStatement ps=con.prepareStatement(sql)) {
            ps.setString(1, "%" + (tuKhoa==null ? "" : tuKhoa.trim()) + "%");
            try(ResultSet rs=ps.executeQuery()){while(rs.next()) ds.add(docThuCung(rs));}
        } catch(SQLException e){throw new RuntimeException("Không thể tìm thú cưng theo số điện thoại khách hàng.",e);}
        return ds;
    }

    public String taoMaTuDong() {
        String sql = "SELECT maThuCung FROM ThuCung";

        try (Connection con = ConnectDB.getConnection();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            java.util.Set<Long> cacSoDaDung = new java.util.HashSet<>();

            while (rs.next()) {
                String ma = rs.getString("maThuCung");

                if (ma != null && ma.matches("^TC[0-9]+$")) {
                    try {
                        long so = Long.parseLong(ma.substring(2));
                        cacSoDaDung.add(so);
                    } catch (NumberFormatException ignored) {
                        // Bỏ qua mã cũ có phần số vượt giới hạn long.
                    }
                }
            }

            long soTiepTheo = 1;

            while (cacSoDaDung.contains(soTiepTheo)) {
                soTiepTheo++;
            }

            return String.format(
                    java.util.Locale.ROOT, "TC%02d", soTiepTheo);

        } catch (SQLException e) {
            throw new RuntimeException("Không thể tạo mã thú cưng.", e);
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
            // Tạo mã và thêm dữ liệu trên cùng một kết nối.
            // Khóa chính trong database vẫn ngăn mã bị trùng.
            String maMoi = taoMaTuDong();
            t.setMaThuCung(maMoi);

            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ganThamSo(ps, t, false);
                return ps.executeUpdate() > 0;
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean capNhat(ThuCung t) {
        String sql = "UPDATE ThuCung SET tenThuCung=?, loai=?, giong=?, gioiTinh=?, canNang=?, ngaySinh=?, tinhTrangSK=?, tiemChung=?, maKH=? WHERE maThuCung=?";
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
        String sql = "DELETE FROM ThuCung WHERE maThuCung = ?";
        try (Connection con = ConnectDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, ma);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
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
