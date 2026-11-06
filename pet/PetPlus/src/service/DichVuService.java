package service;

import dao.DichVuDAO;
import entity.DichVu;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

/**
 * Nghiệp vụ dịch vụ. Bảng DichVu chứa hai loại bản ghi:
 * <ul>
 * <li><b>Danh mục</b> (maLichHen = NULL): dịch vụ cửa hàng cung cấp, trạng thái "Đang hoạt động" / "Ngừng hoạt động".
 * Màn hình tiếp nhận lấy các dịch vụ này làm mẫu.</li>
 * <li><b>Theo lịch hẹn</b> (maLichHen khác NULL): dịch vụ đã đặt cho một lịch hẹn, trạng thái "Đã đặt" / "Hoàn thành".</li>
 * </ul>
 */
public class DichVuService {
    public static final String DANG_HOAT_DONG = "Đang hoạt động";
    public static final String NGUNG_HOAT_DONG = "Ngừng hoạt động";
    public static final String DA_DAT = "Đã đặt";
    public static final String HOAN_THANH = "Hoàn thành";

    public static final List<String> TRANG_THAI_DANH_MUC = Arrays.asList(DANG_HOAT_DONG, NGUNG_HOAT_DONG);
    public static final List<String> TRANG_THAI_THEO_LICH = Arrays.asList(DA_DAT, HOAN_THANH);

    private static final BigDecimal DON_GIA_TOI_DA = new BigDecimal("100000000");

    private final DichVuDAO dao = new DichVuDAO();

    public static boolean laDanhMuc(DichVu d) {
        return d.getMaLichHen() == null || d.getMaLichHen().getMaLichHen() == null
                || d.getMaLichHen().getMaLichHen().trim().isEmpty();
    }

    /** Các trạng thái hợp lệ tùy loại dịch vụ. */
    public static List<String> trangThaiHopLe(boolean danhMuc) {
        return danhMuc ? TRANG_THAI_DANH_MUC : TRANG_THAI_THEO_LICH;
    }

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

    /** Thêm dịch vụ vào danh mục: luôn là dịch vụ không gắn lịch hẹn, không được trùng tên với dịch vụ khác. */
    public boolean them(DichVu d) {
        if (d == null)
            throw new IllegalArgumentException("Thông tin dịch vụ không hợp lệ.");
        d.setMaLichHen(null);
        d.setMaDichVu("DV-TEMP"); // mã thật do DAO sinh
        kiemTra(d);
        kiemTraTrangThai(d);
        if (dao.tonTaiTenDanhMuc(d.getTenDichVu(), null))
            throw new IllegalArgumentException("Danh mục đã có dịch vụ tên \"" + d.getTenDichVu() + "\".");
        return dao.them(d);
    }

    public boolean capNhat(DichVu d) {
        if (d == null || d.getMaDichVu() == null || d.getMaDichVu().trim().isEmpty())
            throw new IllegalArgumentException("Mã dịch vụ không được để trống.");
        DichVu cu = KiemTra.phaiTonTai(dao.timTheoMa(d.getMaDichVu().trim()), "dịch vụ");

        // Không cho đổi loại dịch vụ (danh mục <-> theo lịch hẹn) bằng dữ liệu gửi từ giao diện.
        d.setMaLichHen(cu.getMaLichHen());
        kiemTra(d);
        kiemTraTrangThai(d);

        if (laDanhMuc(cu)) {
            if (dao.tonTaiTenDanhMuc(d.getTenDichVu(), cu.getMaDichVu()))
                throw new IllegalArgumentException("Danh mục đã có dịch vụ tên \"" + d.getTenDichVu() + "\".");
        } else {
            if (HOAN_THANH.equals(cu.getTrangThai()))
                throw new IllegalArgumentException("Dịch vụ đã hoàn thành nên không thể chỉnh sửa.");
        }

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
        DichVu cu = KiemTra.phaiTonTai(dao.timTheoMa(ma), "dịch vụ");
        if (!laDanhMuc(cu)) {
            String lichHen = dao.layLichHenChuaKetThuc(cu.getMaDichVu());
            if (lichHen != null)
                throw new IllegalArgumentException("Dịch vụ đang gắn với lịch hẹn " + lichHen
                        + " chưa kết thúc. Vui lòng hoàn thành hoặc hủy lịch hẹn trước khi xóa dịch vụ.");
        }
        // DAO tự chuyển sang "ẩn" nếu dịch vụ đã nằm trong hóa đơn.
        return dao.xoa(ma);
    }

    private void kiemTraTrangThai(DichVu d) {
        List<String> hopLe = trangThaiHopLe(laDanhMuc(d));
        for (String tt : hopLe)
            if (tt.equals(d.getTrangThai()))
                return;
        throw new IllegalArgumentException("Trạng thái không hợp lệ. Dịch vụ "
                + (laDanhMuc(d) ? "trong danh mục" : "gắn lịch hẹn") + " chỉ nhận: " + String.join(", ", hopLe) + ".");
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
        if (d.getTrangThai() == null || d.getTrangThai().trim().isEmpty())
            throw new IllegalArgumentException("Vui lòng chọn trạng thái.");

        d.setMaDichVu(KiemTra.trim(d.getMaDichVu()));
        d.setTenDichVu(KiemTra.trim(d.getTenDichVu()));
        d.setMoTa(KiemTra.blankNull(d.getMoTa()));
        d.setTrangThai(d.getTrangThai().trim());
    }
}
