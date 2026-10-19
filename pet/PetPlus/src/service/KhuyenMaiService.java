package service;

import dao.KhuyenMaiDAO;
import entity.KhuyenMai;

import java.util.List;

public class KhuyenMaiService {
    private final KhuyenMaiDAO khuyenMaiDAO = new KhuyenMaiDAO();

    public List<KhuyenMai> layDanhSach() {
        return khuyenMaiDAO.layDanhSach();
    }

    public boolean them(KhuyenMai km) {
        kiemTraDuLieu(km);
        if (khuyenMaiDAO.timTheoMa(km.getMaKhuyenMai().trim()) != null) {
            throw new IllegalArgumentException("Mã khuyến mãi đã tồn tại.");
        }
        return khuyenMaiDAO.them(km);
    }

    public boolean capNhat(KhuyenMai km) {
        kiemTraDuLieu(km);
        if (khuyenMaiDAO.timTheoMa(km.getMaKhuyenMai().trim()) == null) {
            throw new IllegalArgumentException("Khuyến mãi không tồn tại.");
        }
        return khuyenMaiDAO.capNhat(km);
    }

    public boolean xoa(String maKM) {
        if (maKM == null || maKM.trim().isEmpty()) {
            throw new IllegalArgumentException("Mã khuyến mãi không được trống.");
        }
        return khuyenMaiDAO.xoa(maKM.trim());
    }

    private void kiemTraDuLieu(KhuyenMai km) {
        if (km == null) throw new IllegalArgumentException("Thông tin khuyến mãi không hợp lệ.");
        if (km.getMaKhuyenMai() == null || km.getMaKhuyenMai().trim().isEmpty()) {
            throw new IllegalArgumentException("Mã khuyến mãi không được để trống.");
        }
        if (km.getTenKhuyenMai() == null || km.getTenKhuyenMai().trim().isEmpty()) {
            throw new IllegalArgumentException("Tên khuyến mãi không được để trống.");
        }
        // Ràng buộc check % giảm
        if (km.getPhanTramGiam() < 0 || km.getPhanTramGiam() > 100) {
            throw new IllegalArgumentException("Phần trăm giảm giá phải từ 0 đến 100.");
        }
        // Ràng buộc ngày kết thúc >= ngày bắt đầu
        if (km.getNgayKetThuc().isBefore(km.getNgayBatDau())) {
            throw new IllegalArgumentException("Ngày kết thúc không được trước ngày bắt đầu.");
        }
    }
}