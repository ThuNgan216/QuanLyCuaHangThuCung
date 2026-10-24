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
                + "FROM dbo.SanPham ORDER BY maSP";

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
        String sql = "SELECT maNCC, tenNCC FROM dbo.NhaCungCap ORDER BY tenNCC, maNCC";
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
                + "WHERE pn.maNCC IS NOT NULL";
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
        String sql = "SELECT maSP, tenSP, giaBan, soLuongTon FROM dbo.SanPham WHERE maSP = ?";
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
        String sql = "UPDATE dbo.SanPham SET tenSP = ?, giaBan = ?, soLuongTon = ? WHERE maSP = ?";
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
        String sql = "DELETE FROM dbo.SanPham WHERE maSP = ?";
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, maSP);
            return ps.executeUpdate() > 0;
        }
    }

    // Lấy danh sách sản phẩm theo mã nhà cung cấp
public List<SanPham> laySanPhamTheoNhaCungCap(String maNCC) throws SQLException {
    List<SanPham> danhSach = new ArrayList<>();
    String sql = "SELECT sp.maSP, sp.tenSP, sp.giaBan, sp.soLuongTon "
               + "FROM dbo.SanPham sp "
               + "INNER JOIN dbo.SanPham_NhaCungCap sn ON sp.maSP = sn.maSP "
               + "WHERE sn.maNCC = ?";

    try (Connection con = ConnectDB.getConnection();
         PreparedStatement ps = con.prepareStatement(sql)) {
        
        ps.setString(1, maNCC);
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                SanPham sp = new SanPham(
                        rs.getString("maSP"),
                        rs.getString("tenSP"),
                        rs.getFloat("giaBan"),
                        rs.getInt("soLuongTon"));
                danhSach.add(sp);
            }
        }
    }
    return danhSach;
}

// Lấy giá nhập của 1 sản phẩm theo nhà cung cấp
public float layGiaNhap(String maSP, String maNCC) throws SQLException {
    String sql = "SELECT giaNhap FROM dbo.SanPham_NhaCungCap WHERE maSP = ? AND maNCC = ?";
    try (Connection con = ConnectDB.getConnection();
         PreparedStatement ps = con.prepareStatement(sql)) {
        ps.setString(1, maSP);
        ps.setString(2, maNCC);
        try (ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getFloat("giaNhap");
        }
    }
    return 0;
}

// Lưu hoặc cập nhật giá nhập mới
public void luuGiaNhap(String maSP, String maNCC, float giaNhap) throws SQLException {
    String sqlCheck = "SELECT 1 FROM dbo.SanPham_NhaCungCap WHERE maSP = ? AND maNCC = ?";
    boolean exists = false;
    try (Connection con = ConnectDB.getConnection(); PreparedStatement ps = con.prepareStatement(sqlCheck)) {
        ps.setString(1, maSP); ps.setString(2, maNCC);
        try (ResultSet rs = ps.executeQuery()) { exists = rs.next(); }
    }
    
    String sql = exists ? "UPDATE dbo.SanPham_NhaCungCap SET giaNhap = ? WHERE maSP = ? AND maNCC = ?" 
                        : "INSERT INTO dbo.SanPham_NhaCungCap (giaNhap, maSP, maNCC) VALUES (?, ?, ?)";
    try (Connection con = ConnectDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
        ps.setFloat(1, giaNhap);
        ps.setString(2, maSP);
        ps.setString(3, maNCC);
        ps.executeUpdate();
    }
}
// Lấy danh sách Sản phẩm kèm Giá nhập theo Mã NCC
    public List<Object[]> layDanhSachSanPhamVaGiaNhapTheoNCC(String maNCC) throws SQLException {
        List<Object[]> list = new ArrayList<>();
        String sql = "SELECT sp.maSP, sp.tenSP, sn.giaNhap, sp.soLuongTon " +
                     "FROM dbo.SanPham sp " +
                     "INNER JOIN dbo.SanPham_NhaCungCap sn ON sp.maSP = sn.maSP " +
                     "WHERE sn.maNCC = ?";
        try (Connection con = database.ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maNCC);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Object[]{
                        rs.getString("maSP"),
                        rs.getString("tenSP"),
                        rs.getFloat("giaNhap"),
                        rs.getInt("soLuongTon")
                    });
                }
            }
        }
        return list;
    }

}
