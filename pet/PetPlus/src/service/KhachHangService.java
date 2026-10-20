
package service;

import java.util.List;

import dao.KhachHangDAO;
import entity.KhachHang;

public class KhachHangService {

    private final KhachHangDAO khachHangDAO = new KhachHangDAO();

    // 1. Lấy danh sách khách hàng
    public List<KhachHang> layDanhSach() {
        return khachHangDAO.layDanhSach();
    }

    // 2. Tìm khách hàng theo mã
    public KhachHang timTheoMa(String maKH) {
        if (maKH == null || maKH.trim().isEmpty()) {
            throw new IllegalArgumentException(
                "Mã khách hàng không được để trống."
            );
        }

        return khachHangDAO.timTheoMa(maKH.trim());
    }

    // 3. Tìm kiếm khách hàng
    public List<KhachHang> timKiem(String tuKhoa) {
        if (tuKhoa == null || tuKhoa.trim().isEmpty()) {
            return layDanhSach();
        }

        return khachHangDAO.timKiem(tuKhoa.trim());
    }

    // 4. Thêm khách hàng
    public String taoMaMoi() { return khachHangDAO.taoMaMoi(); }

    public boolean them(KhachHang kh) {
        if (kh == null) throw new IllegalArgumentException("Thông tin khách hàng không hợp lệ.");
        // Mã do hệ thống phát sinh, không lấy mã người dùng nhập.
        kh.setMaKH(khachHangDAO.taoMaMoi());
        kiemTraDuLieu(kh);
        return khachHangDAO.them(kh);
    }

    // 5. Cập nhật khách hàng
    public boolean capNhat(KhachHang kh) {
        kiemTraDuLieu(kh);

        if (khachHangDAO.timTheoMa(kh.getMaKH().trim()) == null) {
            throw new IllegalArgumentException(
                "Khách hàng không tồn tại."
            );
        }

        return khachHangDAO.capNhat(kh);
    }

    // 6. Xóa khách hàng
    public boolean xoa(String maKH) {
        if (maKH == null || maKH.trim().isEmpty()) {
            throw new IllegalArgumentException(
                "Mã khách hàng không được để trống."
            );
        }

        if (khachHangDAO.timTheoMa(maKH.trim()) == null) {
            throw new IllegalArgumentException(
                "Khách hàng không tồn tại."
            );
        }

        return khachHangDAO.xoa(maKH.trim());
    }

    // 7. Kiểm tra dữ liệu đầu vào
    private void kiemTraDuLieu(KhachHang kh) {
        if (kh == null) {
            throw new IllegalArgumentException(
                "Thông tin khách hàng không hợp lệ."
            );
        }

        String ma = kh.getMaKH() == null
            ? "" : kh.getMaKH().trim();

        String ten = kh.getTenKH() == null
            ? "" : kh.getTenKH().trim();

        String sdt = kh.getSdt() == null
            ? "" : kh.getSdt().trim();

        if (ma.isEmpty() && kh.getMaKH() != null) {
            throw new IllegalArgumentException("Mã khách hàng không hợp lệ.");
        }

        if (ma.length() > 20) {
            throw new IllegalArgumentException(
                "Mã khách hàng tối đa 20 ký tự."
            );
        }

        if (ten.isEmpty()) {
            throw new IllegalArgumentException(
                "Tên khách hàng không được để trống."
            );
        }

        if (ten.length() > 100) {
            throw new IllegalArgumentException(
                "Tên khách hàng tối đa 100 ký tự."
            );
        }

        if (sdt.length() > 15) {
            throw new IllegalArgumentException(
                "Số điện thoại tối đa 15 ký tự."
            );
        }

        if (!sdt.isEmpty() && !sdt.matches("\\d{9,15}")) {
            throw new IllegalArgumentException(
                "Số điện thoại phải gồm 9 đến 15 chữ số."
            );
        }

        if (kh.getDiaChi() != null
                && kh.getDiaChi().length() > 255) {
            throw new IllegalArgumentException(
                "Địa chỉ tối đa 255 ký tự."
            );
        }

        if (kh.getHangThanhVien() < 0) {
            throw new IllegalArgumentException(
                "Hạng thành viên không được âm."
            );
        }

        if (kh.getDiemTichLuy() < 0) {
            throw new IllegalArgumentException(
                "Điểm tích lũy không được âm."
            );
        }

        // Chuẩn hóa dữ liệu trước khi chuyển cho DAO
        kh.setMaKH(ma);
        kh.setTenKH(ten);
        kh.setSdt(sdt.isEmpty() ? null : sdt);
        kh.setDiaChi(
            kh.getDiaChi() == null
                || kh.getDiaChi().trim().isEmpty()
                ? null : kh.getDiaChi().trim()
        );
    }
}
