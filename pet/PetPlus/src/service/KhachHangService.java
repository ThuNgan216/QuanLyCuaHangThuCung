
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
        kh.setHangThanhVien(1);
        kh.setDiemTichLuy(0);
        kiemTraDuLieu(kh);
        if (khachHangDAO.tonTaiSDT(kh.getSdt(), null)) {
            throw new IllegalArgumentException(
                    "Số điện thoại này đã được sử dụng cho khách hàng khác.");
        }
        return khachHangDAO.them(kh);
    }

    // 5. Cập nhật khách hàng
    public boolean capNhat(KhachHang kh) {
        kiemTraDuLieu(kh);
        KhachHang khCu = khachHangDAO.timTheoMa(kh.getMaKH().trim());
        if (khCu == null) {
            throw new IllegalArgumentException("Khách hàng không tồn tại.");
        }
        if (khachHangDAO.tonTaiSDT(kh.getSdt(), kh.getMaKH().trim())) {
            throw new IllegalArgumentException(
                    "Số điện thoại này đã được sử dụng cho khách hàng khác.");
        }
        // Không cho phép thay đổi hạng/điểm bằng cách gửi dữ liệu giả từ giao diện.
        kh.setHangThanhVien(khCu.getHangThanhVien());
        kh.setDiemTichLuy(khCu.getDiemTichLuy());
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

        if (!ten.matches("[\\p{L}]+(?:\\s+[\\p{L}]+)*")) {
            throw new IllegalArgumentException(
                "Tên khách hàng chỉ được chứa chữ cái và khoảng trắng, không có số hoặc ký tự đặc biệt."
            );
        }

        if (!sdt.matches("0\\d{9}")) {
            throw new IllegalArgumentException(
                "Số điện thoại phải bắt đầu bằng 0 và gồm đúng 10 chữ số."
            );
        }

        String diaChi = kh.getDiaChi() == null ? "" : kh.getDiaChi().trim();
        if (diaChi.isEmpty()) {
            throw new IllegalArgumentException("Địa chỉ không được để trống.");
        }
        if (!diaChi.matches("[\\p{L}]+(?:\\s+[\\p{L}]+)*")) {
            throw new IllegalArgumentException(
                "Địa chỉ chỉ được chứa chữ cái và khoảng trắng."
            );
        }

        if (diaChi.length() > 255) {
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
        kh.setDiaChi(diaChi);
    }
}
