package dao;

import database.ConnectDB;
import entity.HoaDon;
import entity.KhachHang;
import entity.TichDiem;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** Truy vấn lịch sử điểm được ghi nhận theo từng hóa đơn. */
public class TichDiemDAO {
    private static final String SELECT_SQL =
            "SELECT td.maTichDiem, td.diemTichLuy, td.ngayCapNhat, " +
            "td.maKH, kh.tenKH, td.maHoaDon " +
            "FROM TichDiem td " +
            "INNER JOIN KhachHang kh ON kh.maKH = td.maKH ";

    public List<TichDiem> layDanhSach() {
        List<TichDiem> ds = new ArrayList<>();
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(SELECT_SQL + " ORDER BY td.ngayCapNhat DESC, td.maTichDiem");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) ds.add(docTichDiem(rs));
        } catch (SQLException e) {
            throw new IllegalStateException("Không thể tải lịch sử tích điểm: " + e.getMessage(), e);
        }
        return ds;
    }

    public List<TichDiem> timKiem(String tuKhoa) {
        if (tuKhoa == null || tuKhoa.trim().isEmpty()) return layDanhSach();
        List<TichDiem> ds = new ArrayList<>();
        String sql = SELECT_SQL +
                "WHERE td.maTichDiem LIKE ? OR td.maKH LIKE ? OR kh.tenKH LIKE ? OR td.maHoaDon LIKE ? " +
                "ORDER BY td.ngayCapNhat DESC, td.maTichDiem";
        String key = "%" + tuKhoa.trim() + "%";
        try (Connection con = ConnectDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            for (int i = 1; i <= 4; i++) ps.setString(i, key);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) ds.add(docTichDiem(rs));
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Không thể tìm lịch sử tích điểm: " + e.getMessage(), e);
        }
        return ds;
    }

    private TichDiem docTichDiem(ResultSet rs) throws SQLException {
        TichDiem td = new TichDiem();
        td.setMaTichDiem(rs.getString("maTichDiem"));
        td.setDiemTichLuy(rs.getInt("diemTichLuy"));
        Date ngay = rs.getDate("ngayCapNhat");
        td.setNgayCapNhat(ngay == null ? null : ngay.toLocalDate());

        KhachHang kh = new KhachHang();
        kh.setMaKH(rs.getString("maKH"));
        kh.setTenKH(rs.getString("tenKH"));
        td.setMaKH(kh);

        td.setMaHoaDon(new HoaDon(rs.getString("maHoaDon")));
        return td;
    }
}
