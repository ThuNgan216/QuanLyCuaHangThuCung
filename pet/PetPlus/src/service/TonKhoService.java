package service;

import dao.SanPhamDAO;
import entity.SanPham;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TonKhoService {

    private final SanPhamDAO sanPhamDAO = new SanPhamDAO();

    // UC022 - Xem cảnh báo tồn kho
    public List<SanPham> layCanhBaoTonKho(int mucToiThieu) {
        try {
            List<SanPham> ds = sanPhamDAO.layDanhSachSanPham();
            // Lọc ra các sản phẩm có số lượng tồn <= mức tối thiểu
            return ds.stream()
                     .filter(sp -> sp.getSoLuongTon() <= mucToiThieu)
                     .collect(Collectors.toList());
        } catch (SQLException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
}