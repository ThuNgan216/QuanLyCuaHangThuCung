package service;

import dao.KhuyenMaiDAO;
import entity.KhuyenMai;

import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class KhuyenMaiService {
    private static final Pattern MA_KHUYEN_MAI_DANG_SO = Pattern.compile("^KM(\\d+)$", Pattern.CASE_INSENSITIVE);
    private final KhuyenMaiDAO khuyenMaiDAO = new KhuyenMaiDAO();

    public List<KhuyenMai> layDanhSach() {
        return khuyenMaiDAO.layDanhSach();
    }

    /** Sinh mã KM kế tiếp theo các mã đã có, không cần thay đổi cấu trúc database. */
    public String taoMaMoi() {
        long soLonNhat = 0;
        for (KhuyenMai km : khuyenMaiDAO.layDanhSach()) {
            if (km.getMaKhuyenMai() == null) continue;
            Matcher matcher = MA_KHUYEN_MAI_DANG_SO.matcher(km.getMaKhuyenMai().trim());
            if (!matcher.matches()) continue;
            try {
                soLonNhat = Math.max(soLonNhat, Long.parseLong(matcher.group(1)));
            } catch (NumberFormatException ignored) {
                // Bỏ qua mã số quá lớn; các mã hợp lệ khác vẫn dùng được để tìm số tiếp theo.
            }
        }
        if (soLonNhat == Long.MAX_VALUE) {
            throw new IllegalStateException("Không thể tạo thêm mã khuyến mãi.");
        }
        return "KM" + String.format(Locale.ROOT, "%03d", soLonNhat + 1);
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
        if (!KhuyenMai.PHAM_VI_HOA_DON.equals(km.getPhamViApDung())
                && !KhuyenMai.PHAM_VI_SAN_PHAM.equals(km.getPhamViApDung())) {
            throw new IllegalArgumentException("Hãy chọn loại khuyến mãi hợp lệ.");
        }
        if (km.getHangKhachHangApDung() < 0 || km.getHangKhachHangApDung() > 3) {
            throw new IllegalArgumentException("Hạng khách hàng áp dụng phải từ 1 đến 3 hoặc tất cả khách hàng.");
        }
        if (KhuyenMai.PHAM_VI_SAN_PHAM.equals(km.getPhamViApDung())) {
            if (km.getMaSanPhamApDung().isEmpty()) {
                throw new IllegalArgumentException("Hãy chọn ít nhất một sản phẩm áp dụng khuyến mãi.");
            }
            if (km.getSoLuongToiThieu() < 1) {
                throw new IllegalArgumentException("Số lượng tối thiểu phải từ 1 trở lên.");
            }
        }
        if (km.getDieuKienApDungLuu().length() > 500) {
            throw new IllegalArgumentException("Thông tin khuyến mãi vượt quá giới hạn lưu 500 ký tự.");
        }
        // Ràng buộc check % giảm
        if (km.getPhanTramGiam() < 0 || km.getPhanTramGiam() > 100) {
            throw new IllegalArgumentException("Phần trăm giảm giá phải từ 0 đến 100.");
        }
        // Ràng buộc ngày kết thúc >= ngày bắt đầu
        if (km.getNgayBatDau() == null || km.getNgayKetThuc() == null) {
            throw new IllegalArgumentException("Vui lòng chọn ngày bắt đầu và ngày kết thúc.");
        }
        if (km.getNgayKetThuc().isBefore(km.getNgayBatDau())) {
            throw new IllegalArgumentException("Ngày kết thúc không được trước ngày bắt đầu.");
        }
    }
}
