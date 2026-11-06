package service;

/**
 * Các bước kiểm tra giống nhau của service khách hàng, thú cưng, lịch hẹn, dịch vụ.
 * Dùng chung để thông báo lỗi và cách chuẩn hóa dữ liệu giống hệt nhau ở cả 4 module.
 */
public final class KiemTra {
    private KiemTra() {
    }

    /** Chuỗi chữ cái (có dấu) cách nhau bằng khoảng trắng, không số, không ký tự đặc biệt. */
    private static final String CHI_CHU_CAI = "[\\p{L}]+(?:\\s+[\\p{L}]+)*";

    public static String trim(String s) {
        return s == null ? "" : s.trim();
    }

    /** Chuỗi rỗng đổi thành null để lưu NULL vào CSDL. */
    public static String blankNull(String s) {
        String v = trim(s);
        return v.isEmpty() ? null : v;
    }

    /** Mã bắt buộc có giá trị; trả về mã đã cắt khoảng trắng. */
    public static String maBatBuoc(String ma, String tenDoiTuong) {
        String v = trim(ma);
        if (v.isEmpty())
            throw new IllegalArgumentException("Mã " + tenDoiTuong + " không được để trống.");
        return v;
    }

    /** Không tìm thấy bản ghi (chưa có hoặc đã bị xóa). */
    public static <T> T phaiTonTai(T doiTuong, String tenDoiTuong) {
        if (doiTuong == null)
            throw new IllegalArgumentException("Không tìm thấy " + tenDoiTuong + ".");
        return doiTuong;
    }

    public static void doDaiToiDa(String giaTri, int max, String nhan) {
        if (trim(giaTri).length() > max)
            throw new IllegalArgumentException(nhan + " tối đa " + max + " ký tự.");
    }

    /** Tên người / thú cưng: bắt buộc, chỉ chữ cái và khoảng trắng, tối đa max ký tự. */
    public static String tenChiChuCai(String giaTri, String nhan, int max) {
        String v = trim(giaTri);
        if (v.isEmpty())
            throw new IllegalArgumentException(nhan + " không được để trống.");
        doDaiToiDa(v, max, nhan);
        if (!v.matches(CHI_CHU_CAI))
            throw new IllegalArgumentException(
                    nhan + " chỉ được chứa chữ cái và khoảng trắng, không có số hoặc ký tự đặc biệt.");
        return v;
    }

    /** Trường tùy chọn: để trống thì thôi, nếu có thì chỉ chữ cái và khoảng trắng. */
    public static String chiChuCaiTuyChon(String giaTri, String nhan, int max) {
        String v = trim(giaTri);
        if (v.isEmpty())
            return null;
        return tenChiChuCai(v, nhan, max);
    }
}
