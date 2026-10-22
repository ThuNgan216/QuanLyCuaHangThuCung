
package service;

import dao.DichVuDAO;
import entity.DichVu;

import java.math.BigDecimal;
import java.util.List;

public class DichVuService {

    private final DichVuDAO dao = new DichVuDAO();

    public static final String DANG_HOAT_DONG = "Đang hoạt động";
    public static final String NGUNG_HOAT_DONG = "Ngừng hoạt động";

    public List<DichVu> layDanhSach() {
        return dao.layDanhSach();
    }

    public List<DichVu> timKiem(String tuKhoa) {
        return dao.timKiem(tuKhoa);
    }

    public String taoMaMoi() {
        return dao.taoMaMoi();
    }

    /**
     * maBoQua = null khi thêm mới.
     * Khi sửa, truyền mã của dịch vụ đang sửa.
     */
    public boolean kiemTraTrungTen(String ten, String maBoQua) {
        if (ten == null || ten.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Tên dịch vụ không được để trống.");
        }

        return dao.kiemTraTrungTen(chuanHoaTen(ten), maBoQua);
    }

    public boolean them(DichVu d) {
        kiemTra(d);

        if (dao.kiemTraTrungTen(d.getTenDichVu(), null)) {
            throw new IllegalArgumentException(
                    "Tên dịch vụ đã tồn tại. Vui lòng nhập tên khác.");
        }

        d.setMaDichVu(dao.taoMaMoi());
        return dao.them(d);
    }

    public boolean capNhat(DichVu d) {
        kiemTra(d);

        if (dao.kiemTraTrungTen(
                d.getTenDichVu(), d.getMaDichVu())) {
            throw new IllegalArgumentException(
                    "Tên dịch vụ đã tồn tại. Vui lòng nhập tên khác.");
        }

        return dao.capNhat(d);
    }

    public boolean xoa(String ma) {
        if (ma == null || ma.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Vui lòng chọn dịch vụ cần xóa.");
        }

        return dao.xoa(ma.trim());
    }

    private void kiemTra(DichVu d) {
        if (d == null) {
            throw new IllegalArgumentException(
                    "Thông tin dịch vụ không hợp lệ.");
        }

        String ten = chuanHoaTen(d.getTenDichVu());

        if (ten.isEmpty()) {
            throw new IllegalArgumentException(
                    "Tên dịch vụ không được để trống.");
        }

        // Chỉ cho phép chữ cái Unicode (bao gồm tiếng Việt)
        // và khoảng trắng; không cho số hoặc ký tự đặc biệt.
        if (!ten.matches("[\\p{L} ]+")) {
            throw new IllegalArgumentException(
                    "Tên dịch vụ chỉ được chứa chữ cái và khoảng trắng.");
        }

        d.setTenDichVu(ten);

        if (d.getDonGia() == null
                || d.getDonGia().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "Đơn giá phải lớn hơn 0.");
        }

        if (d.getMoTa() != null) {
            String moTa = d.getMoTa().trim();

            if (moTa.length() > 500) {
                throw new IllegalArgumentException(
                        "Mô tả tối đa 500 ký tự.");
            }

            d.setMoTa(moTa.isEmpty() ? null : moTa);
        }

        if (!DANG_HOAT_DONG.equals(d.getTrangThai())
                && !NGUNG_HOAT_DONG.equals(d.getTrangThai())) {
            throw new IllegalArgumentException(
                    "Vui lòng chọn trạng thái hợp lệ.");
        }
    }

    private String chuanHoaTen(String ten) {
        if (ten == null) {
            return "";
        }

        // Xóa khoảng trắng đầu/cuối và gộp nhiều khoảng trắng liên tiếp.
        return ten.trim().replaceAll("\\s+", " ");
    }
}
