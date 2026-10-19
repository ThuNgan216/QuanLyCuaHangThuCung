package dao;

import database.ConnectDB;
import entity.NhaCungCap;
import entity.NhanVien;
import entity.PhieuNhap;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PhieuNhapDAO {

    // Thêm Phiếu Nhập mới
    public boolean them(PhieuNhap pn, Connection con) throws SQLException {
        String sql = "INSERT INTO PhieuNhap (maPN, ngayNhap, maNV, maNCC) VALUES (?, ?, ?, ?)";
        
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, pn.getMaPN());
            ps.setDate(2, Date.valueOf(pn.getNgayNhap()));
            ps.setString(3, pn.getMaNV().getMaNV());
            ps.setString(4, pn.getMaNCC().getMaNCC());

            return ps.executeUpdate() > 0;
        }
    }

    // Lấy danh sách toàn bộ phiếu nhập JOIN với Nhân Viên và Nhà Cung Cấp để lấy đầy đủ Tên
    public List<PhieuNhap> layDanhSach() {
        List<PhieuNhap> ds = new ArrayList<>();
        String sql = "SELECT pn.maPN, pn.ngayNhap, pn.maNV, nv.tenNV, pn.maNCC, ncc.tenNCC " +
                     "FROM PhieuNhap pn " +
                     "LEFT JOIN NhanVien nv ON pn.maNV = nv.maNV " +
                     "LEFT JOIN NhaCungCap ncc ON pn.maNCC = ncc.maNCC " +
                     "ORDER BY pn.ngayNhap DESC, pn.maPN DESC";

        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                NhanVien nv = new NhanVien();
                nv.setMaNV(rs.getString("maNV"));
                nv.setTenNV(rs.getString("tenNV")); // Lấy tên nhân viên đầy đủ

                NhaCungCap ncc = new NhaCungCap();
                ncc.setMaNCC(rs.getString("maNCC"));
                ncc.setTenNCC(rs.getString("tenNCC")); // Lấy tên nhà cung cấp đầy đủ

                PhieuNhap pn = new PhieuNhap(
                        rs.getString("maPN"),
                        rs.getDate("ngayNhap").toLocalDate(),
                        nv,
                        ncc
                );
                ds.add(pn);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ds;
    }
    
    // Tìm kiếm phiếu nhập theo mã phiếu nhập
    public PhieuNhap timTheoMa(String maPN) {
        String sql = "SELECT pn.maPN, pn.ngayNhap, pn.maNV, nv.tenNV, pn.maNCC, ncc.tenNCC " +
                     "FROM PhieuNhap pn " +
                     "LEFT JOIN NhanVien nv ON pn.maNV = nv.maNV " +
                     "LEFT JOIN NhaCungCap ncc ON pn.maNCC = ncc.maNCC " +
                     "WHERE pn.maPN = ?";
        
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
             
            ps.setString(1, maPN);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    NhanVien nv = new NhanVien();
                    nv.setMaNV(rs.getString("maNV"));
                    nv.setTenNV(rs.getString("tenNV"));

                    NhaCungCap ncc = new NhaCungCap();
                    ncc.setMaNCC(rs.getString("maNCC"));
                    ncc.setTenNCC(rs.getString("tenNCC"));

                    return new PhieuNhap(
                            rs.getString("maPN"),
                            rs.getDate("ngayNhap").toLocalDate(),
                            nv,
                            ncc
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}