package service;

import dao.DichVuDAO;
import entity.DichVu;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

/**
 * Nghiệp vụ quản lý danh mục dịch vụ của cửa hàng (thêm, sửa giá, ngừng cung cấp, xóa).
 * Chỉ làm việc với dịch vụ không gắn lịch hẹn; các bản sao gắn lịch hẹn do chức năng Tiếp nhận quản lý.
 */
public class DichVuService {
    public static final String DANG_HOAT_DONG = "Đang hoạt động";
    public static final String NGUNG_HOAT_DONG = "Ngừng hoạt động";
    public static final List<String> TRANG_THAI_HOP_LE = Arrays.asList(DANG_HOAT_DONG, NGUNG_HOAT_DONG);

    private static final BigDecimal DON_GIA_TOI_DA = new BigDecimal("100000000");

    private final DichVuDAO dao = new DichVuDAO();

    public List<DichVu> layDanhSach() {
        return dao.layDanhSach();
    }

    public List<DichVu> timKiem(String tuKhoa) {
        String key = KiemTra.trim(tuKhoa);
        return key.isEmpty() ? layDanhSach() : dao.timKiem(key);
    }

    public DichVu timTheoMa(String ma) {
        return dao.timTheoMa(KiemTra.maBatBuoc(ma, "dịch vụ"));
    }

    /** Mã gợi ý hiển thị trên form; mã thật do DAO sinh khi lưu. */
    public String taoMaMoi() {
        return dao.taoMaMoi();
    }

    /** Thêm dịch vụ vào danh mục; không được trùng tên với dịch vụ đang có. */
    public boolean them(DichVu d) {
        if (d == null)
            throw new IllegalArgumentException("Thông tin dịch vụ không hợp lệ.");
        d.setMaLichHen(null);
        d.setMaDichVu("DV-TEMP"); // mã thật do DAO sinh
        kiemTra(d);
        if (dao.tonTaiTenDanhMuc(d.getTenDichVu(), null))
            throw new IllegalArgumentException("Đã có dịch vụ tên \"" + d.getTenDichVu() + "\".");
        return dao.them(d);
    }

    public boolean capNhat(DichVu d) {
        if (d == null || KiemTra.trim(d.getMaDichVu()).isEmpty())
            throw new IllegalArgumentException("Mã dịch vụ không được để trống.");
        DichVu cu = KiemTra.phaiTonTai(dao.timTheoMa(d.getMaDichVu().trim()), "dịch vụ");
        d.setMaLichHen(null);
        kiemTra(d);
        if (dao.tonTaiTenDanhMuc(d.getTenDichVu(), cu.getMaDichVu()))
            throw new IllegalArgumentException("Đã có dịch vụ tên \"" + d.getTenDichVu() + "\".");

        // Dịch vụ đã xuất hiện trên hóa đơn: hóa đơn cũ hiển thị tên lấy từ đây nên không được đổi tên / đơn giá.
        if (dao.daDungTrongHoaDon(cu.getMaDichVu())) {
            boolean doiTen = !cu.getTenDichVu().equalsIgnoreCase(d.getTenDichVu());
            boolean doiGia = cu.getDonGia().compareTo(d.getDonGia()) != 0;
            if (doiTen || doiGia)
                throw new IllegalArgumentException("Dịch vụ này đã có trong hóa đơn nên không được đổi tên hoặc đơn giá. "
                        + "Hãy chuyển sang \"Ngừng hoạt động\" và tạo dịch vụ mới.");
        }
        return dao.capNhat(d);
    }

    public boolean xoa(String ma) {
        ma = KiemTra.maBatBuoc(ma, "dịch vụ");
        KiemTra.phaiTonTai(dao.timTheoMa(ma), "dịch vụ");
        // DAO tự chuyển sang "ẩn" nếu dịch vụ đã nằm trong hóa đơn.
        return dao.xoa(ma);
    }

    private void kiemTra(DichVu d) {
        KiemTra.maBatBuoc(d.getMaDichVu(), "dịch vụ");
        KiemTra.doDaiToiDa(d.getMaDichVu(), 20, "Mã dịch vụ");
        if (KiemTra.trim(d.getTenDichVu()).isEmpty())
            throw new IllegalArgumentException("Tên dịch vụ không được để trống.");
        KiemTra.doDaiToiDa(d.getTenDichVu(), 150, "Tên dịch vụ");
        BigDecimal gia = d.getDonGia();
        if (gia == null || gia.signum() <= 0)
            throw new IllegalArgumentException("Đơn giá phải lớn hơn 0.");
        if (gia.stripTrailingZeros().scale() > 2)
            throw new IllegalArgumentException("Đơn giá tối đa 2 chữ số thập phân.");
        if (gia.compareTo(DON_GIA_TOI_DA) > 0)
            throw new IllegalArgumentException("Đơn giá tối đa " + DON_GIA_TOI_DA.toPlainString()
                    + " đồng. Vui lòng kiểm tra lại.");
        KiemTra.doDaiToiDa(d.getMoTa(), 500, "Mô tả");
        if (!TRANG_THAI_HOP_LE.contains(KiemTra.trim(d.getTrangThai())))
            throw new IllegalArgumentException("Trạng thái chỉ nhận: " + String.join(", ", TRANG_THAI_HOP_LE) + ".");

        d.setMaDichVu(KiemTra.trim(d.getMaDichVu()));
        d.setTenDichVu(KiemTra.trim(d.getTenDichVu()));
        d.setMoTa(KiemTra.blankNull(d.getMoTa()));
        d.setTrangThai(KiemTra.trim(d.getTrangThai()));
    }
}
