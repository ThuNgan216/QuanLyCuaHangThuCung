package dao;

import database.ConnectDB;
import entity.ChiTietPhieuNhap;
import entity.PhieuNhap;
import entity.SanPham;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ChiTietPhieuNhapDAO {

    // Lấy danh sách chi tiết sản phẩm dựa theo Mã Phiếu Nhập
    public List<ChiTietPhieuNhap> layChiTietTheoMaPN(String maPN) throws SQLException {
        List<ChiTietPhieuNhap> danhSach = new ArrayList<>();
        
        // Kiểm tra lại tên các cột trong bảng ChiTietPhieuNhap ở SQL Server của nhóm (thường là maPN hoặc maPhieuNhap)
        String sql = "SELECT ct.maPN, ct.maSP, sp.tenSP, ct.soLuongNhap, ct.donGiaNhap, ct.hanSuDung " +
                     "FROM ChiTietPhieuNhap ct " +
                     "INNER JOIN SanPham sp ON ct.maSP = sp.maSP " +
                     "WHERE ct.maPN = ?";

        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            ps.setString(1, maPN);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    SanPham sp = new SanPham();
                    sp.setMaSP(rs.getString("maSP"));
                    sp.setTenSP(rs.getString("tenSP"));

                    PhieuNhap pn = new PhieuNhap(rs.getString("maPN"));

                    ChiTietPhieuNhap ct = new ChiTietPhieuNhap();
                    ct.setMaSP(sp);
                    ct.setMaPN(pn);
                    ct.setSoLuongNhap(rs.getInt("soLuongNhap"));
                    
                    // Lấy đơn giá nhập dạng BigDecimal an toàn
                    java.math.BigDecimal donGia = rs.getBigDecimal("donGiaNhap");
                    ct.setDonGiaNhap(donGia != null ? donGia : java.math.BigDecimal.ZERO);
                    
                    if (rs.getDate("hanSuDung") != null) {
                        ct.setHanSuDung(rs.getDate("hanSuDung").toLocalDate());
                    }

                    danhSach.add(ct);
                }
            }
        }
        return danhSach;
    }

    public boolean them(ChiTietPhieuNhap ct, Connection con) throws SQLException {
        String sql = "INSERT INTO ChiTietPhieuNhap (maPN, maSP, soLuongNhap, donGiaNhap, hanSuDung) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, ct.getMaPN().getMaPN());
            ps.setString(2, ct.getMaSP().getMaSP());
            ps.setInt(3, ct.getSoLuongNhap());
            ps.setBigDecimal(4, ct.getDonGiaNhap());
            
            if (ct.getHanSuDung() != null) {
                ps.setDate(5, java.sql.Date.valueOf(ct.getHanSuDung()));
            } else {
                ps.setNull(5, java.sql.Types.DATE);
            }

            return ps.executeUpdate() > 0;
        }
    }
}