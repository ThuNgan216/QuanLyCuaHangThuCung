package service;

import java.util.List;

import dao.KhachHangDAO;
import entity.HangThanhVien;
import entity.KhachHang;

public class KhachHangService {

    private final KhachHangDAO khachHangDAO = new KhachHangDAO();

    // 1. Lấy danh sách khách hàng
    public List<KhachHang> layDanhSach() {
        return khachHangDAO.layDanhSach();
    }

    // 2. Tìm khách hàng theo mã
    public KhachHang timTheoMa(String maKH) {
        return khachHangDAO.timTheoMa(KiemTra.maBatBuoc(maKH, "khách hàng"));
    }

    // 3. Tìm kiếm khách hàng
    public List<KhachHang> timKiem(String tuKhoa) {
        String key = KiemTra.trim(tuKhoa);
        return key.isEmpty() ? layDanhSach() : khachHangDAO.timKiem(key);
    }

    /** Tìm kiếm kết hợp lọc theo hạng thành viên (hang = null: không lọc). */
    public List<KhachHang> timKiem(String tuKhoa, HangThanhVien hang) {
        List<KhachHang> ds = timKiem(tuKhoa);
        if (hang == null)
            return ds;
        List<KhachHang> loc = new java.util.ArrayList<>();
        for (KhachHang kh : ds)
            if (HangThanhVien.tuMa(kh.getHangThanhVien()) == hang)
                loc.add(kh);
        return loc;
    }

    /** Mô tả hạng để hiển thị, ví dụ "Hạng 1 (còn 340 điểm để lên Hạng 2)". */
    public static String moTaHang(int maHang, int diem) {
        HangThanhVien hang = HangThanhVien.tuMa(maHang);
        HangThanhVien tiep = hang.hangTiepTheo();
        return tiep == null ? hang.getTen() + " (hạng cao nhất)"
                : hang.getTen() + " (còn " + hang.diemConThieu(diem) + " điểm để lên " + tiep.getTen() + ")";
    }

    // 4. Thêm khách hàng
    public String taoMaMoi() {
        return khachHangDAO.taoMaMoi();
    }

    public boolean them(KhachHang kh) {
        if (kh == null)
            throw new IllegalArgumentException("Thông tin khách hàng không hợp lệ.");
        // Mã do hệ thống phát sinh, không lấy mã người dùng nhập.
        kh.setMaKH(khachHangDAO.taoMaMoi());
        kh.setHangThanhVien(HangThanhVien.HANG_1.getMa());
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
        KhachHang khCu = KiemTra.phaiTonTai(khachHangDAO.timTheoMa(kh.getMaKH().trim()), "khách hàng");
        if (khachHangDAO.tonTaiSDT(kh.getSdt(), kh.getMaKH().trim())) {
            throw new IllegalArgumentException(
                    "Số điện thoại này đã được sử dụng cho khách hàng khác.");
        }
        // Không cho phép thay đổi hạng/điểm bằng cách gửi dữ liệu giả từ giao diện.
        // Hạng luôn khớp với điểm hiện có và không bị hạ so với hạng đã đạt.
        kh.setDiemTichLuy(khCu.getDiemTichLuy());
        kh.setHangThanhVien(Math.max(HangThanhVien.tuMa(khCu.getHangThanhVien()).getMa(),
                HangThanhVien.tuDiem(khCu.getDiemTichLuy()).getMa()));
        return khachHangDAO.capNhat(kh);
    }

    // 6. Xóa khách hàng
    public boolean xoa(String maKH) {
        maKH = KiemTra.maBatBuoc(maKH, "khách hàng");
        KiemTra.phaiTonTai(khachHangDAO.timTheoMa(maKH), "khách hàng");

        // Không xóa khách còn lịch hẹn đang hiệu lực, nếu không lịch của khách bị xóa theo mà
        // nhân viên không hay biết.
        if (khachHangDAO.coLichHenChuaKetThuc(maKH)) {
            throw new IllegalArgumentException(
                    "Khách hàng này còn lịch hẹn chưa kết thúc. "
                            + "Vui lòng hoàn thành hoặc hủy các lịch hẹn trước khi xóa khách hàng.");
        }

        // Chặn xóa khi còn thú cưng, nếu không khách hàng bị ẩn nhưng thú cưng vẫn hiện
        // với chủ nuôi đã ẩn.
        if (khachHangDAO.coThuCungDangQuanLy(maKH)) {
            throw new IllegalArgumentException(
                    "Khách hàng này còn thú cưng đang quản lý. "
                            + "Vui lòng xóa các thú cưng của khách hàng trước khi xóa khách hàng.");
        }

        return khachHangDAO.xoa(maKH);
    }

    /**
     * Địa chỉ thật thường có số nhà và dấu phẩy (vd: "650 Lê Văn Thọ, Gò Vấp"),
     * nên cho phép chữ, chữ số, khoảng trắng và các ký tự , . / -
     * Để public static để form nhập liệu dùng chung, tránh viết luật hai lần.
     */
    private static final java.util.regex.Pattern DIA_CHI_HOP_LE = java.util.regex.Pattern
            .compile("[\\p{L}\\p{N}\\s,./\\-]+");

    /** Luật kiểm tra tên (chữ cái và khoảng trắng, tối đa 100 ký tự), dùng chung với thú cưng qua KiemTra. */
    public static void kiemTraTen(String tenKH) {
        KiemTra.tenChiChuCai(tenKH, "Tên khách hàng", 100);
    }

    /** Luật kiểm tra số điện thoại, dùng chung cho dialog và service. */
    public static void kiemTraSdt(String soDienThoai) {
        String sdt = soDienThoai == null ? "" : soDienThoai.trim();
        if (!sdt.matches("0\\d{9}")) {
            throw new IllegalArgumentException("Số điện thoại phải bắt đầu bằng 0 và gồm đúng 10 chữ số.");
        }
    }

    public static void kiemTraDiaChi(String diaChi) {
        String dc = diaChi == null ? "" : diaChi.trim();
        if (dc.isEmpty()) {
            throw new IllegalArgumentException("Địa chỉ không được để trống.");
        }
        if (dc.length() > 255) {
            throw new IllegalArgumentException("Địa chỉ tối đa 255 ký tự.");
        }
        if (!DIA_CHI_HOP_LE.matcher(dc).matches()) {
            throw new IllegalArgumentException(
                    "Địa chỉ chỉ được chứa chữ cái, chữ số, khoảng trắng và các ký tự , . / -");
        }
    }

    // 7. Kiểm tra dữ liệu đầu vào
    private void kiemTraDuLieu(KhachHang kh) {
        if (kh == null) {
            throw new IllegalArgumentException(
                    "Thông tin khách hàng không hợp lệ.");
        }

        String ma = kh.getMaKH() == null
                ? ""
                : kh.getMaKH().trim();

        String ten = kh.getTenKH() == null
                ? ""
                : kh.getTenKH().trim();

        String sdt = kh.getSdt() == null
                ? ""
                : kh.getSdt().trim();

        if (ma.isEmpty() && kh.getMaKH() != null) {
            throw new IllegalArgumentException("Mã khách hàng không hợp lệ.");
        }

        if (ma.length() > 20) {
            throw new IllegalArgumentException(
                    "Mã khách hàng tối đa 20 ký tự.");
        }

        kiemTraTen(ten);
        kiemTraSdt(sdt);

        String diaChi = kh.getDiaChi() == null ? "" : kh.getDiaChi().trim();
        kiemTraDiaChi(diaChi);

        if (kh.getHangThanhVien() < 0) {
            throw new IllegalArgumentException(
                    "Hạng thành viên không được âm.");
        }

        if (kh.getDiemTichLuy() < 0) {
            throw new IllegalArgumentException(
                    "Điểm tích lũy không được âm.");
        }

        // Chuẩn hóa dữ liệu trước khi chuyển cho DAO
        kh.setMaKH(ma);
        kh.setTenKH(ten);
        kh.setSdt(sdt.isEmpty() ? null : sdt);
        kh.setDiaChi(diaChi);
    }
}