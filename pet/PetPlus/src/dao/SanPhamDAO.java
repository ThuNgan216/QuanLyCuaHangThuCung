package dao;

import database.ConnectDB;
import entity.SanPham;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SanPhamDAO {

    // Lấy danh sách sản phẩm từ database
    public List<SanPham> layDanhSachSanPham() throws SQLException {
        List<SanPham> danhSach = new ArrayList<>();

        String sql = "SELECT maSP, tenSP, giaBan, soLuongTon "
                + "FROM dbo.SanPham WHERE trangThai <> N'Đã xóa' ORDER BY maSP";

        // Lấy dữ liệu và tự đóng kết nối
        try (Connection con = ConnectDB.getConnection();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                SanPham sp = new SanPham(
                        rs.getString("maSP"),
                        rs.getString("tenSP"),
                        rs.getFloat("giaBan"),
                        rs.getInt("soLuongTon"));
                danhSach.add(sp);
            }
        }

        return danhSach;
    }

    // Lấy Top 5 sản phẩm bán chạy từ database
    public List<Object[]> layTop5SanPhamBanChay() throws SQLException {
        String sql = "SELECT TOP (5) " +
                "sp.maSP, sp.tenSP, " +
                "SUM(ct.soLuong) AS soLuongDaBan " +
                "FROM dbo.ChiTietHoaDon ct " +
                "INNER JOIN dbo.SanPham sp " +
                "ON ct.maSP = sp.maSP " +
                "GROUP BY sp.maSP, sp.tenSP " +
                "ORDER BY soLuongDaBan DESC, sp.maSP";

        List<Object[]> danhSach = new ArrayList<>();

        // Lấy dữ liệu và tự đóng kết nối
        try (Connection con = ConnectDB.getConnection();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                danhSach.add(new Object[] {
                        rs.getString("tenSP"),
                        rs.getInt("soLuongDaBan")
                });
            }
        }

        return danhSach;
    }

    // Lấy danh sách nhà cung cấp để hiển thị bộ lọc
    public List<String[]> layDanhSachNhaCungCap() throws SQLException {
        List<String[]> danhSach = new ArrayList<>();
        String sql = "SELECT maNCC, tenNCC FROM dbo.NhaCungCap WHERE trangThai <> N'Đã xóa' ORDER BY tenNCC, maNCC";
        try (Connection con = ConnectDB.getConnection();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                danhSach.add(new String[] { rs.getString("maNCC"), rs.getString("tenNCC") });
            }
        }
        return danhSach;
    }

    // Lấy các quan hệ nhà cung cấp - sản phẩm từ lịch sử nhập hàng, không lặp
    public List<String[]> layNhaCungCapCuaSanPham() throws SQLException {
        List<String[]> danhSach = new ArrayList<>();
        String sql = "SELECT DISTINCT ct.maSP, pn.maNCC "
                + "FROM dbo.ChiTietPhieuNhap ct "
                + "INNER JOIN dbo.PhieuNhap pn ON ct.maPN = pn.maPN "
                + "INNER JOIN dbo.NhaCungCap ncc ON ncc.maNCC = pn.maNCC "
                + "WHERE pn.maNCC IS NOT NULL AND ncc.trangThai <> N'Đã xóa'";
        try (Connection con = ConnectDB.getConnection();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                danhSach.add(new String[] { rs.getString("maSP"), rs.getString("maNCC") });
            }
        }
        return danhSach;
    }

    public SanPham timTheoMa(String maSP) throws SQLException {
        String sql = "SELECT maSP, tenSP, giaBan, soLuongTon FROM dbo.SanPham WHERE maSP = ? AND trangThai <> N'Đã xóa'";
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
             
            ps.setString(1, maSP);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new SanPham(
                            rs.getString("maSP"),
                            rs.getString("tenSP"),
                            rs.getFloat("giaBan"),
                            rs.getInt("soLuongTon")
                    );
                }
            }
        }
        return null;
    }

    public boolean them(SanPham sp) throws SQLException {
        String sql = "INSERT INTO dbo.SanPham (maSP, tenSP, giaBan, soLuongTon) VALUES (?, ?, ?, ?)";
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, sp.getMaSP());
            ps.setString(2, sp.getTenSP());
            ps.setFloat(3, sp.getGiaBan());
            ps.setInt(4, sp.getSoLuongTon());

            return ps.executeUpdate() > 0;
        }
    }

    public boolean capNhat(SanPham sp) throws SQLException {
        String sql = "UPDATE dbo.SanPham SET tenSP = ?, giaBan = ?, soLuongTon = ? WHERE maSP = ? AND trangThai <> N'Đã xóa'";
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, sp.getTenSP());
            ps.setFloat(2, sp.getGiaBan());
            ps.setInt(3, sp.getSoLuongTon());
            ps.setString(4, sp.getMaSP());

            return ps.executeUpdate() > 0;
        }
    }

    public boolean xoa(String maSP) throws SQLException {
        try (Connection con = ConnectDB.getConnection()) {
            return DeletePolicy.xoaHoacAn(con,
                    "SELECT 1 FROM dbo.SanPham WITH (XLOCK, HOLDLOCK) WHERE maSP = ? AND trangThai <> N'Đã xóa'",
                    new String[] {
                            "SELECT 1 FROM dbo.MaVach WHERE maSP = ?",
                            "SELECT 1 FROM dbo.ChiTietPhieuNhap WHERE maSP = ?",
                            "SELECT 1 FROM dbo.ChiTietHoaDon WHERE maSP = ?",
                            "SELECT 1 FROM dbo.KhuyenMaiSanPham WHERE maSP = ?"
                    },
                    "UPDATE dbo.SanPham SET trangThai = N'Đã xóa' WHERE maSP = ? AND trangThai <> N'Đã xóa'",
                    "DELETE FROM dbo.SanPham WHERE maSP = ? AND trangThai <> N'Đã xóa'",
                    maSP);
        }
    }

    // Lấy sản phẩm từng được nhập từ nhà cung cấp theo lịch sử phiếu nhập.
    public List<SanPham> laySanPhamTheoNhaCungCap(String maNCC) throws SQLException {
        List<SanPham> danhSach = new ArrayList<>();
        String sql = "SELECT DISTINCT sp.maSP, sp.tenSP, sp.giaBan, sp.soLuongTon "
                + "FROM dbo.SanPham sp "
                + "INNER JOIN dbo.ChiTietPhieuNhap ct ON ct.maSP = sp.maSP "
                + "INNER JOIN dbo.PhieuNhap pn ON pn.maPN = ct.maPN "
                + "WHERE pn.maNCC = ? AND sp.trangThai <> N'Đã xóa' ORDER BY sp.tenSP, sp.maSP";

        try (Connection con = ConnectDB.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maNCC);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    danhSach.add(new SanPham(
                            rs.getString("maSP"),
                            rs.getString("tenSP"),
                            rs.getFloat("giaBan"),
                            rs.getInt("soLuongTon")));
                }
            }
        }
        return danhSach;
    }

    // Giá nhập gần nhất của sản phẩm từ nhà cung cấp, lấy từ chi tiết phiếu nhập.
    public float layGiaNhap(String maSP, String maNCC) throws SQLException {
        return layGiaNhapGanNhat(maSP, maNCC);
    }

    // Danh sách sản phẩm và giá nhập gần nhất đã ghi nhận từ nhà cung cấp.
    public List<Object[]> layDanhSachSanPhamVaGiaNhapTheoNCC(String maNCC) throws SQLException {
        List<Object[]> danhSach = new ArrayList<>();
        String sql = "WITH GiaNhapGanNhat AS ("
                + "SELECT ct.maSP, ct.donGiaNhap, "
                + "ROW_NUMBER() OVER (PARTITION BY ct.maSP ORDER BY pn.ngayNhap DESC, pn.maPN DESC) AS thuTu "
                + "FROM dbo.ChiTietPhieuNhap ct "
                + "INNER JOIN dbo.PhieuNhap pn ON pn.maPN = ct.maPN "
                + "WHERE pn.maNCC = ?) "
                + "SELECT sp.maSP, sp.tenSP, g.donGiaNhap AS giaNhap, sp.soLuongTon "
                + "FROM GiaNhapGanNhat g "
                + "INNER JOIN dbo.SanPham sp ON sp.maSP = g.maSP "
                + "WHERE g.thuTu = 1 AND sp.trangThai <> N'Đã xóa' ORDER BY sp.tenSP, sp.maSP";

        try (Connection con = ConnectDB.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maNCC);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    danhSach.add(new Object[] {
                            rs.getString("maSP"),
                            rs.getString("tenSP"),
                            rs.getFloat("giaNhap"),
                            rs.getInt("soLuongTon")
                    });
                }
            }
        }
        return danhSach;
    }

    // Lấy danh sách sản phẩm theo NCC (Bao gồm SP từng nhập từ NCC đó + SP tự do chưa từng nhập ở trạng thái Chờ nhập)
    public List<SanPham> laySanPhamTheoNhaCungCapTuLichSu(String maNCC) throws SQLException {
        List<SanPham> danhSach = new ArrayList<>();
        String sql = "SELECT DISTINCT sp.maSP, sp.tenSP, sp.giaBan, sp.soLuongTon " +
                     "FROM dbo.SanPham sp " +
                     "INNER JOIN dbo.ChiTietPhieuNhap ct ON sp.maSP = ct.maSP " +
                     "INNER JOIN dbo.PhieuNhap pn ON ct.maPN = pn.maPN " +
                     "WHERE pn.maNCC = ? AND sp.trangThai <> N'Đã xóa' " +
                     "UNION " +
                     "SELECT sp.maSP, sp.tenSP, sp.giaBan, sp.soLuongTon " +
                     "FROM dbo.SanPham sp " +
                     "WHERE sp.trangThai <> N'Đã xóa' AND sp.maSP NOT IN (SELECT maSP FROM dbo.ChiTietPhieuNhap)";

        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maNCC);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    danhSach.add(new SanPham(
                        rs.getString("maSP"),
                        rs.getString("tenSP"),
                        rs.getFloat("giaBan"),
                        rs.getInt("soLuongTon")
                    ));
                }
            }
        }
        return danhSach;
    }

    // Lấy giá nhập gần nhất của sản phẩm từ NCC đó để gợi ý tự động
    public float layGiaNhapGanNhat(String maSP, String maNCC) throws SQLException {
        String sql = "SELECT TOP 1 ct.donGiaNhap " +
                     "FROM dbo.ChiTietPhieuNhap ct " +
                     "INNER JOIN dbo.PhieuNhap pn ON ct.maPN = pn.maPN " +
                     "WHERE ct.maSP = ? AND pn.maNCC = ? " +
                     "ORDER BY pn.ngayNhap DESC, pn.maPN DESC";
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maSP);
            ps.setString(2, maNCC);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getFloat("donGiaNhap");
            }
        }
        return 0;
    }

    // Lấy giá nhập gần nhất của sản phẩm (không cần phân biệt NCC)
    public float layGiaNhapGanNhatTongQuat(String maSP) throws SQLException {
        String sql = "SELECT TOP 1 ct.donGiaNhap " +
                     "FROM dbo.ChiTietPhieuNhap ct " +
                     "INNER JOIN dbo.PhieuNhap pn ON ct.maPN = pn.maPN " +
                     "WHERE ct.maSP = ? " +
                     "ORDER BY pn.ngayNhap DESC, pn.maPN DESC";
        try (Connection con = database.ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maSP);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getFloat("donGiaNhap");
            }
        }
        return 0;
    }

    // Lấy danh sách gợi ý nhập hàng (Tồn kho dưới 10) kèm theo gợi ý NCC và Giá từ lịch sử
    public List<Object[]> layDanhSachGoiYNhapHang(int nguongTonKho) throws SQLException {
        List<Object[]> list = new ArrayList<>();
        String sql = 
            "SELECT sp.maSP, sp.tenSP, sp.soLuongTon, " +
            "       ISNULL(lastImport.tenNCC, N'Chưa từng nhập (Hàng tự do)') AS nccGoiY, " +
            "       ISNULL(lastImport.donGiaNhap, 0) AS giaGoiY " +
            "FROM dbo.SanPham sp " +
            "OUTER APPLY ( " +
            "    SELECT TOP 1 ncc.tenNCC, ct.donGiaNhap " +
            "    FROM dbo.ChiTietPhieuNhap ct " +
            "    INNER JOIN dbo.PhieuNhap pn ON ct.maPN = pn.maPN " +
            "    INNER JOIN dbo.NhaCungCap ncc ON pn.maNCC = ncc.maNCC " +
            "    WHERE ct.maSP = sp.maSP AND ncc.trangThai <> N'Đã xóa' " +
            "    ORDER BY pn.ngayNhap DESC, pn.maPN DESC " +
            ") AS lastImport " +
            "WHERE sp.trangThai <> N'Đã xóa' AND sp.soLuongTon <= ? " +
            "ORDER BY sp.soLuongTon ASC";

        try (Connection con = database.ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, nguongTonKho);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Object[]{
                        rs.getString("maSP"), rs.getString("tenSP"), rs.getInt("soLuongTon"),
                        rs.getString("nccGoiY"), rs.getFloat("giaGoiY")
                    });
                }
            }
        }
        return list;
    }

    // Lấy dữ liệu tổng hợp phục vụ Gợi ý nhập hàng (Tồn kho, Tốc độ bán 30 ngày, Lịch sử NCC)
    public List<Object[]> layDuLieuGoiYNhapHang() throws SQLException {
        List<Object[]> list = new ArrayList<>();
        String sql = 
            "SELECT sp.maSP, sp.tenSP, sp.soLuongTon, " +
            "       ISNULL(sales.TongBan30Ngay, 0) AS TongBan30Ngay, " +
            "       lastImport.maNCC, lastImport.tenNCC, lastImport.donGiaNhap " +
            "FROM dbo.SanPham sp " +
            "LEFT JOIN ( " +
            "    SELECT ct.maSP, SUM(ct.soLuong) AS TongBan30Ngay " +
            "    FROM dbo.ChiTietHoaDon ct " +
            "    INNER JOIN dbo.HoaDon hd ON ct.maHoaDon = hd.maHoaDon " +
            "    WHERE hd.ngayLapHD >= DATEADD(day, -30, GETDATE()) " +
            "    GROUP BY ct.maSP " +
            ") sales ON sp.maSP = sales.maSP " +
            "OUTER APPLY ( " +
            "    SELECT TOP 1 pn.maNCC, ncc.tenNCC, ct.donGiaNhap " +
            "    FROM dbo.ChiTietPhieuNhap ct " +
            "    INNER JOIN dbo.PhieuNhap pn ON ct.maPN = pn.maPN " +
            "    INNER JOIN dbo.NhaCungCap ncc ON pn.maNCC = ncc.maNCC " +
            "    WHERE ct.maSP = sp.maSP AND ncc.trangThai <> N'Đã xóa' " +
            "    ORDER BY pn.ngayNhap DESC, pn.maPN DESC " +
            ") AS lastImport " +
            "WHERE sp.trangThai <> N'Đã xóa'";

        try (Connection con = database.ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new Object[]{
                    rs.getString("maSP"), 
                    rs.getString("tenSP"), 
                    rs.getInt("soLuongTon"),
                    rs.getInt("TongBan30Ngay"),
                    rs.getString("maNCC"), 
                    rs.getString("tenNCC") != null ? rs.getString("tenNCC") : "Chưa có",
                    rs.getFloat("donGiaNhap")
                });
            }
        }
        return list;
    }
}
