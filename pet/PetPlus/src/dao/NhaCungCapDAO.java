package dao;

import database.ConnectDB;
import entity.NhaCungCap;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class NhaCungCapDAO {

    public List<NhaCungCap> layDanhSach() {
        List<NhaCungCap> ds = new ArrayList<>();
        String sql = "SELECT maNCC, tenNCC, soDienThoai, diaChi, email FROM dbo.NhaCungCap WHERE trangThai <> N'Đã xóa'";

        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                NhaCungCap ncc = new NhaCungCap();
                ncc.setMaNCC(rs.getString("maNCC"));
                ncc.setTenNCC(rs.getString("tenNCC"));
                ncc.setSoDienThoai(rs.getString("soDienThoai"));
                ncc.setDiaChi(rs.getString("diaChi"));
                ncc.setEmail(rs.getString("email"));
                ds.add(ncc);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Không thể tải danh sách nhà cung cấp: " + e.getMessage(), e);
        }
        return ds;
    }

    public NhaCungCap timTheoMa(String maNCC) {
        String sql = "SELECT * FROM dbo.NhaCungCap WHERE maNCC = ? AND trangThai <> N'Đã xóa'";
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, maNCC);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    NhaCungCap ncc = new NhaCungCap();
                    ncc.setMaNCC(rs.getString("maNCC"));
                    ncc.setTenNCC(rs.getString("tenNCC"));
                    ncc.setSoDienThoai(rs.getString("soDienThoai"));
                    ncc.setDiaChi(rs.getString("diaChi"));
                    ncc.setEmail(rs.getString("email"));
                    return ncc;
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Không thể tìm nhà cung cấp: " + e.getMessage(), e);
        }
        return null;
    }

    // --- HÀM MỚI: PHÁT SINH MÃ TỰ ĐỘNG ---
    public String phatSinhMaNCCMoi() throws SQLException {
        String sql = "SELECT TOP 1 maNCC FROM dbo.NhaCungCap ORDER BY maNCC DESC";
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            if (rs.next()) {
                String maMax = rs.getString("maNCC"); // VD: NCC005
                try {
                    // Lấy phần số sau chữ NCC (từ vị trí thứ 3), ví dụ "005" -> 5
                    int soHienTai = Integer.parseInt(maMax.substring(3));
                    int soMoi = soHienTai + 1;
                    // Định dạng lại thành NCC + 3 chữ số (NCC006)
                    return String.format("NCC%03d", soMoi);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }
        }
        // Trả về mặc định nếu bảng đang rỗng hoặc lỗi phân tích chuỗi
        return "NCC001";
    }

    public boolean them(NhaCungCap ncc) {
        String sql = "INSERT INTO dbo.NhaCungCap (maNCC, tenNCC, soDienThoai, diaChi, email) VALUES (?, ?, ?, ?, ?)";
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, ncc.getMaNCC());
            ps.setString(2, ncc.getTenNCC());
            ps.setString(3, ncc.getSoDienThoai());
            ps.setString(4, ncc.getDiaChi());
            ps.setString(5, ncc.getEmail());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new IllegalStateException("Không thể thêm nhà cung cấp: " + e.getMessage(), e);
        }
    }

    public boolean capNhat(NhaCungCap ncc) {
        String sql = "UPDATE dbo.NhaCungCap SET tenNCC = ?, soDienThoai = ?, diaChi = ?, email = ? WHERE maNCC = ? AND trangThai <> N'Đã xóa'";
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, ncc.getTenNCC());
            ps.setString(2, ncc.getSoDienThoai());
            ps.setString(3, ncc.getDiaChi());
            ps.setString(4, ncc.getEmail());
            ps.setString(5, ncc.getMaNCC());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new IllegalStateException("Không thể cập nhật nhà cung cấp: " + e.getMessage(), e);
        }
    }

    public boolean xoa(String maNCC) {
        try (Connection con = ConnectDB.getConnection()) {
            return DeletePolicy.xoaHoacAn(con,
                    "SELECT 1 FROM dbo.NhaCungCap WITH (XLOCK, HOLDLOCK) WHERE maNCC = ? AND trangThai <> N'Đã xóa'",
                    new String[] { "SELECT 1 FROM dbo.PhieuNhap WHERE maNCC = ?" },
                    "UPDATE dbo.NhaCungCap SET trangThai = N'Đã xóa' WHERE maNCC = ? AND trangThai <> N'Đã xóa'",
                    "DELETE FROM dbo.NhaCungCap WHERE maNCC = ? AND trangThai <> N'Đã xóa'",
                    maNCC);
        } catch (SQLException e) {
            throw new IllegalStateException("Không thể xóa hoặc ẩn nhà cung cấp: " + e.getMessage(), e);
        }
    }
}
