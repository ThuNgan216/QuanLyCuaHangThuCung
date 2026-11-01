
package dao;

import database.ConnectDB;
import entity.KhachHang;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class KhachHangDAO {

    // 1. Lấy danh sách khách hàng từ SQL Server
    public List<KhachHang> layDanhSach() {
        List<KhachHang> ds = new ArrayList<>();

        String sql = "SELECT maKH, tenKH, sdt, diaChi, hangThanhVien, diemTichLuy "
                   + "FROM KhachHang WHERE trangThai <> N'Đã xóa'";

        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                KhachHang kh = new KhachHang();

                kh.setMaKH(rs.getString("maKH"));
                kh.setTenKH(rs.getString("tenKH"));
                kh.setSdt(rs.getString("sdt"));
                kh.setDiaChi(rs.getString("diaChi"));
                kh.setHangThanhVien(rs.getInt("hangThanhVien"));
                kh.setDiemTichLuy(rs.getInt("diemTichLuy"));

                ds.add(kh);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return ds;
    }

    // 2. Tìm khách hàng theo mã
    public KhachHang timTheoMa(String maKH) {
        String sql = "SELECT maKH, tenKH, sdt, diaChi, hangThanhVien, diemTichLuy "
                   + "FROM KhachHang WHERE maKH = ? AND trangThai <> N'Đã xóa'";

        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, maKH);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    KhachHang kh = new KhachHang();

                    kh.setMaKH(rs.getString("maKH"));
                    kh.setTenKH(rs.getString("tenKH"));
                    kh.setSdt(rs.getString("sdt"));
                    kh.setDiaChi(rs.getString("diaChi"));
                    kh.setHangThanhVien(rs.getInt("hangThanhVien"));
                    kh.setDiemTichLuy(rs.getInt("diemTichLuy"));

                    return kh;
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    // 3. Tìm theo mã, tên hoặc số điện thoại
    public List<KhachHang> timKiem(String tuKhoa) {
        List<KhachHang> ds = new ArrayList<>();

        String sql = "SELECT maKH, tenKH, sdt, diaChi, hangThanhVien, diemTichLuy "
                   + "FROM KhachHang "
                   + "WHERE trangThai <> N'Đã xóa' "
                   + "AND (maKH LIKE ? OR tenKH LIKE ? OR sdt LIKE ?)";

        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            String key = "%" + tuKhoa + "%";

            ps.setString(1, key);
            ps.setString(2, key);
            ps.setString(3, key);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    KhachHang kh = new KhachHang();

                    kh.setMaKH(rs.getString("maKH"));
                    kh.setTenKH(rs.getString("tenKH"));
                    kh.setSdt(rs.getString("sdt"));
                    kh.setDiaChi(rs.getString("diaChi"));
                    kh.setHangThanhVien(rs.getInt("hangThanhVien"));
                    kh.setDiemTichLuy(rs.getInt("diemTichLuy"));

                    ds.add(kh);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return ds;
    }


    /** Phát sinh mã KH theo số thứ tự lớn nhất hiện có, dạng KH01, KH02... */
    public String taoMaMoi() {
        String sql = "SELECT MAX(TRY_CONVERT(INT, SUBSTRING(maKH, 3, 18))) FROM KhachHang WHERE maKH LIKE 'KH[0-9]%'";
        try (Connection con = ConnectDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            int max = 0; if (rs.next()) max = rs.getInt(1);
            return String.format(java.util.Locale.ROOT, "KH%02d", max + 1);
        } catch (SQLException e) { throw new IllegalStateException("Không thể phát sinh mã khách hàng: " + e.getMessage(), e); }
    }

    // 4. Thêm khách hàng vào SQL Server
    public boolean them(KhachHang kh) {
        String sql = "INSERT INTO KhachHang "
                   + "(maKH, tenKH, sdt, diaChi, hangThanhVien, diemTichLuy) "
                   + "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, kh.getMaKH());
            ps.setString(2, kh.getTenKH());
            ps.setString(3, kh.getSdt());
            ps.setString(4, kh.getDiaChi());

            if (kh.getHangThanhVien() == 0) {
                ps.setNull(5, Types.INTEGER);
            } else {
                ps.setInt(5, kh.getHangThanhVien());
            }

            ps.setInt(6, kh.getDiemTichLuy());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 5. Cập nhật khách hàng trong SQL Server
    public boolean capNhat(KhachHang kh) {
        String sql = "UPDATE KhachHang "
                   + "SET tenKH = ?, sdt = ?, diaChi = ?, "
                   + "hangThanhVien = ?, diemTichLuy = ? "
                   + "WHERE maKH = ? AND trangThai <> N'Đã xóa'";

        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, kh.getTenKH());
            ps.setString(2, kh.getSdt());
            ps.setString(3, kh.getDiaChi());

            if (kh.getHangThanhVien() == 0) {
                ps.setNull(4, Types.INTEGER);
            } else {
                ps.setInt(4, kh.getHangThanhVien());
            }

            ps.setInt(5, kh.getDiemTichLuy());
            ps.setString(6, kh.getMaKH());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 6. Xóa khách hàng nếu chưa được dùng; nếu có lịch sử thì ẩn để giữ khóa ngoại.
    public boolean xoa(String maKH) {
        try (Connection con = ConnectDB.getConnection()) {
            return DeletePolicy.xoaHoacAn(con,
                    "SELECT 1 FROM KhachHang WITH (XLOCK, HOLDLOCK) WHERE maKH = ? AND trangThai <> N'Đã xóa'",
                    new String[] {
                            "SELECT 1 FROM ThuCung WHERE maKH = ?",
                            "SELECT 1 FROM LichHen WHERE maKH = ?",
                            "SELECT 1 FROM HoaDon WHERE maKH = ?",
                            "SELECT 1 FROM TichDiem WHERE maKH = ?"
                    },
                    "UPDATE KhachHang SET trangThai = N'Đã xóa' WHERE maKH = ? AND trangThai <> N'Đã xóa'",
                    "DELETE FROM KhachHang WHERE maKH = ? AND trangThai <> N'Đã xóa'",
                    maKH);

        } catch (SQLException e) {
            throw new IllegalStateException("Không thể xóa hoặc ẩn khách hàng: " + e.getMessage(), e);
        }
    }
}
