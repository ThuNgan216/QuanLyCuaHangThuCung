package service;

import dao.KhachHangDAO;
import dao.ThuCungDAO;
import entity.KhachHang;
import entity.ThuCung;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

/** Nghiệp vụ và kiểm tra dữ liệu thú cưng. */
public class ThuCungService {
    /** Các loài được quản lý; giao diện (dlgThuCung) chọn trong danh sách này. */
    public static final List<String> LOAI_HOP_LE = Arrays.asList("Chó", "Mèo", "Chim", "Thỏ", "Hamster", "Khác");
    public static final List<String> GIOI_TINH_HOP_LE = Arrays.asList("Đực", "Cái");
    /** Tuổi tối đa hợp lý của một thú cưng (năm); ngày sinh xa hơn coi là nhập sai. */
    private static final int TUOI_TOI_DA = 40;

    private final ThuCungDAO thuCungDAO = new ThuCungDAO();
    private final KhachHangDAO khachHangDAO = new KhachHangDAO();

    public List<ThuCung> layDanhSach() {
        return thuCungDAO.layDanhSach();
    }

    public String taoMaMoi() {
        return thuCungDAO.taoMaMoi();
    }

    /** Tìm theo mã, tên, loài, giống thú cưng hoặc tên / số điện thoại chủ nuôi. */
    public List<ThuCung> timKiem(String tuKhoa) {
        String key = KiemTra.trim(tuKhoa);
        return key.isEmpty() ? layDanhSach() : thuCungDAO.timKiem(key);
    }

    public ThuCung timTheoMa(String ma) {
        return thuCungDAO.timTheoMa(KiemTra.maBatBuoc(ma, "thú cưng"));
    }

    public boolean them(ThuCung t) {
        if (t == null)
            throw new IllegalArgumentException("Thông tin thú cưng không hợp lệ.");
        // Mã được phát sinh tự động trong DAO khi thêm mới.
        t.setMaThuCung("TC-TEMP");
        kiemTra(t, true);
        kiemTraKhongTrung(t, null);
        return thuCungDAO.them(t);
    }

    public boolean capNhat(ThuCung t) {
        kiemTra(t, false);
        ThuCung cu = KiemTra.phaiTonTai(thuCungDAO.timTheoMa(t.getMaThuCung()), "thú cưng");

        // Đổi chủ nuôi khi còn lịch hẹn chưa kết thúc sẽ làm lịch hẹn lệch chủ (LichHen.maKH khác chủ thú cưng).
        boolean doiChu = cu.getMaKH() != null
                && !cu.getMaKH().getMaKH().equalsIgnoreCase(t.getMaKH().getMaKH());
        if (doiChu && thuCungDAO.coLichHenChuaKetThuc(t.getMaThuCung())) {
            throw new IllegalArgumentException("Thú cưng này còn lịch hẹn chưa kết thúc nên chưa thể đổi chủ nuôi. "
                    + "Vui lòng hoàn thành hoặc hủy các lịch hẹn trước.");
        }
        kiemTraKhongTrung(t, t.getMaThuCung());
        return thuCungDAO.capNhat(t);
    }

    public boolean xoa(String ma) {
        ma = KiemTra.maBatBuoc(ma, "thú cưng");
        KiemTra.phaiTonTai(thuCungDAO.timTheoMa(ma), "thú cưng");
        if (thuCungDAO.coLichHenChuaKetThuc(ma)) {
            throw new IllegalArgumentException("Thú cưng này còn lịch hẹn chưa kết thúc. "
                    + "Vui lòng hoàn thành hoặc hủy các lịch hẹn trước khi xóa thú cưng.");
        }
        return thuCungDAO.xoa(ma);
    }

    public List<KhachHang> layDanhSachKhachHang() {
        return khachHangDAO.layDanhSach();
    }

    /** Một chủ nuôi không có hai thú cưng cùng tên và cùng loài. */
    private void kiemTraKhongTrung(ThuCung t, String maBoQua) {
        if (thuCungDAO.tonTaiTrung(t.getMaKH().getMaKH().trim(), t.getTenThuCung(), t.getLoai(), maBoQua))
            throw new IllegalArgumentException("Khách hàng này đã có thú cưng cùng tên \"" + t.getTenThuCung()
                    + "\" và cùng loài " + t.getLoai() + ".");
    }

    /** Cân nặng tối đa hợp lý (kg) theo loài, để bắt lỗi nhập sai như 450 thay vì 4.5. */
    private static float canNangToiDa(String loai) {
        switch (loai) {
            case "Chó":
                return 90f;
            case "Mèo":
                return 15f;
            case "Chim":
                return 5f;
            case "Thỏ":
                return 10f;
            case "Hamster":
                return 1f;
            default:
                return 100f;
        }
    }

    private void kiemTra(ThuCung t, boolean themMoi) {
        if (t == null)
            throw new IllegalArgumentException("Thông tin thú cưng không hợp lệ.");
        String ma = KiemTra.trim(t.getMaThuCung());
        if (!themMoi && ma.isEmpty())
            throw new IllegalArgumentException("Mã thú cưng không hợp lệ.");
        KiemTra.doDaiToiDa(ma, 20, "Mã thú cưng");
        if (KiemTra.trim(t.getLoai()).isEmpty() || KiemTra.trim(t.getGiong()).isEmpty()
                || KiemTra.trim(t.getGioiTinh()).isEmpty() || t.getNgaySinh() == null) {
            throw new IllegalArgumentException("Vui lòng nhập đầy đủ thông tin.");
        }

        String ten = KiemTra.tenChiChuCai(t.getTenThuCung(), "Tên thú cưng", 100);

        // Loài, giới tính: chỉ nhận giá trị trong danh sách (không tin hoàn toàn vào combobox ở giao diện)
        String loai = chonTrongDanhSach(LOAI_HOP_LE, KiemTra.trim(t.getLoai()));
        if (loai == null)
            throw new IllegalArgumentException("Loài thú cưng phải là một trong: " + String.join(", ", LOAI_HOP_LE) + ".");
        String gioiTinh = chonTrongDanhSach(GIOI_TINH_HOP_LE, KiemTra.trim(t.getGioiTinh()));
        if (gioiTinh == null)
            throw new IllegalArgumentException("Giới tính phải là Đực hoặc Cái.");

        String sucKhoe = KiemTra.chiChuCaiTuyChon(t.getTinhTrangSK(), "Tình trạng sức khỏe", 255);
        KiemTra.doDaiToiDa(t.getGiong(), 100, "Giống");
        KiemTra.doDaiToiDa(t.getTiemChung(), 255, "Thông tin tiêm chủng");

        // Cân nặng: CAN_NANG_TRONG nghĩa là chưa nhập; nếu có nhập thì phải > 0 và hợp lý theo loài.
        float canNang = t.getCanNang();
        if (canNang != ThuCung.CAN_NANG_TRONG) {
            if (Float.isNaN(canNang) || canNang <= 0)
                throw new IllegalArgumentException("Cân nặng phải lớn hơn 0 (để trống nếu chưa biết).");
            float toiDa = canNangToiDa(loai);
            if (canNang > toiDa)
                throw new IllegalArgumentException("Cân nặng " + canNang + " kg vượt mức hợp lý của loài " + loai
                        + " (tối đa " + toiDa + " kg). Vui lòng kiểm tra lại.");
        }

        // Ngày sinh
        LocalDate homNay = LocalDate.now();
        if (t.getNgaySinh().isAfter(homNay))
            throw new IllegalArgumentException("Ngày sinh không được ở tương lai.");
        if (t.getNgaySinh().isBefore(homNay.minusYears(TUOI_TOI_DA)))
            throw new IllegalArgumentException("Ngày sinh quá xa (thú cưng không thể hơn " + TUOI_TOI_DA
                    + " tuổi). Vui lòng kiểm tra lại.");

        // Chủ nuôi
        if (t.getMaKH() == null || KiemTra.trim(t.getMaKH().getMaKH()).isEmpty())
            throw new IllegalArgumentException("Vui lòng chọn khách hàng sở hữu thú cưng.");
        if (khachHangDAO.timTheoMa(KiemTra.trim(t.getMaKH().getMaKH())) == null)
            throw new IllegalArgumentException("Khách hàng không tồn tại hoặc đã bị xóa.");

        // Chuẩn hóa trước khi chuyển cho DAO
        if (!themMoi)
            t.setMaThuCung(ma);
        t.setTenThuCung(ten);
        t.setLoai(loai);
        t.setGioiTinh(gioiTinh);
        t.setGiong(KiemTra.blankNull(t.getGiong()));
        t.setTinhTrangSK(sucKhoe);
        t.setTiemChung(KiemTra.blankNull(t.getTiemChung()));
        t.getMaKH().setMaKH(KiemTra.trim(t.getMaKH().getMaKH()));
    }

    private String chonTrongDanhSach(List<String> ds, String giaTri) {
        for (String s : ds)
            if (s.equalsIgnoreCase(giaTri))
                return s;
        return null;
    }
}
