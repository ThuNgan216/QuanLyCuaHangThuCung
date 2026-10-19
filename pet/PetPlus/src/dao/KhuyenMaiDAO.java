package dao;

import database.ConnectDB;
import entity.KhuyenMai;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class KhuyenMaiDAO {

    public List<KhuyenMai> layDanhSach() {
        List<KhuyenMai> ds = new ArrayList<>();
        String sql = "SELECT maKhuyenMai, tenKhuyenMai, phanTramGiam, dieuKienApDung, ngayBatDau, ngayKetThuc FROM KhuyenMai";

        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                KhuyenMai km = new KhuyenMai();
                km.setMaKhuyenMai(rs.getString("maKhuyenMai"));
                km.setTenKhuyenMai(rs.getString("tenKhuyenMai"));
                km.setPhanTramGiam(rs.getFloat("phanTramGiam"));
                km.setDieuKienApDung(rs.getString("dieuKienApDung"));
                km.setNgayBatDau(rs.getDate("ngayBatDau").toLocalDate());
                km.setNgayKetThuc(rs.getDate("ngayKetThuc").toLocalDate());
                ds.add(km);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ds;
    }

    public KhuyenMai timTheoMa(String maKM) {
        String sql = "SELECT * FROM KhuyenMai WHERE maKhuyenMai = ?";
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, maKM);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    KhuyenMai km = new KhuyenMai();
                    km.setMaKhuyenMai(rs.getString("maKhuyenMai"));
                    km.setTenKhuyenMai(rs.getString("tenKhuyenMai"));
                    km.setPhanTramGiam(rs.getFloat("phanTramGiam"));
                    km.setDieuKienApDung(rs.getString("dieuKienApDung"));
                    km.setNgayBatDau(rs.getDate("ngayBatDau").toLocalDate());
                    km.setNgayKetThuc(rs.getDate("ngayKetThuc").toLocalDate());
                    return km;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean them(KhuyenMai km) {
        String sql = "INSERT INTO KhuyenMai (maKhuyenMai, tenKhuyenMai, phanTramGiam, dieuKienApDung, ngayBatDau, ngayKetThuc) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, km.getMaKhuyenMai());
            ps.setString(2, km.getTenKhuyenMai());
            ps.setDouble(3, km.getPhanTramGiam());
            ps.setString(4, km.getDieuKienApDung());
            ps.setDate(5, Date.valueOf(km.getNgayBatDau()));
            ps.setDate(6, Date.valueOf(km.getNgayKetThuc()));

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean capNhat(KhuyenMai km) {
        String sql = "UPDATE KhuyenMai SET tenKhuyenMai = ?, phanTramGiam = ?, dieuKienApDung = ?, ngayBatDau = ?, ngayKetThuc = ? WHERE maKhuyenMai = ?";
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, km.getTenKhuyenMai());
            ps.setDouble(2, km.getPhanTramGiam());
            ps.setString(3, km.getDieuKienApDung());
            ps.setDate(4, Date.valueOf(km.getNgayBatDau()));
            ps.setDate(5, Date.valueOf(km.getNgayKetThuc()));
            ps.setString(6, km.getMaKhuyenMai());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean xoa(String maKM) {
        String sql = "DELETE FROM KhuyenMai WHERE maKhuyenMai = ?";
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, maKM);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}