package service;

import dao.SanPhamDAO;
import entity.SanPham;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SanPhamService {
    private final SanPhamDAO sanPhamDAO = new SanPhamDAO();

    public List<SanPham> layDanhSach() {
        try {
            return sanPhamDAO.layDanhSachSanPham();
        } catch (SQLException e) {
            e.printStackTrace();
            return new ArrayList<>(); // Lỗi thì trả về mảng rỗng để UI không bị văng
        }
    }

    public boolean them(SanPham sp) {
        kiemTraDuLieu(sp);
        try {
            if (sanPhamDAO.timTheoMa(sp.getMaSP()) != null) {
                throw new IllegalArgumentException("Mã sản phẩm đã tồn tại.");
            }
            return sanPhamDAO.them(sp);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean capNhat(SanPham sp) {
        kiemTraDuLieu(sp);
        try {
            if (sanPhamDAO.timTheoMa(sp.getMaSP()) == null) {
                throw new IllegalArgumentException("Sản phẩm không tồn tại.");
            }
            return sanPhamDAO.capNhat(sp);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean xoa(String maSP) {
        if (maSP == null || maSP.trim().isEmpty()) {
            throw new IllegalArgumentException("Mã sản phẩm không được rỗng.");
        }
        try {
            return sanPhamDAO.xoa(maSP);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void kiemTraDuLieu(SanPham sp) {
        if (sp.getMaSP() == null || sp.getMaSP().trim().isEmpty()) {
            throw new IllegalArgumentException("Mã sản phẩm không được rỗng");
        }
        if (sp.getTenSP() == null || sp.getTenSP().trim().isEmpty()) {
            throw new IllegalArgumentException("Tên sản phẩm không được rỗng");
        }
        if (sp.getGiaBan() < 0) {
            throw new IllegalArgumentException("Giá bán không được nhỏ hơn 0");
        }
        if (sp.getSoLuongTon() < 0) {
            throw new IllegalArgumentException("Số lượng tồn không được nhỏ hơn 0");
        }
    }
}