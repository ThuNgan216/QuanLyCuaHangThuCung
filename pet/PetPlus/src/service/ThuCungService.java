package service;

import dao.KhachHangDAO;
import dao.ThuCungDAO;
import entity.KhachHang;
import entity.ThuCung;
import java.time.LocalDate;
import java.util.List;

/** Nghiệp vụ và kiểm tra dữ liệu thú cưng. */
public class ThuCungService {
    private final ThuCungDAO thuCungDAO = new ThuCungDAO();
    private final KhachHangDAO khachHangDAO = new KhachHangDAO();

    public List<ThuCung> layDanhSach() { return thuCungDAO.layDanhSach(); }
    public String taoMaThuCung() { return thuCungDAO.taoMaTuDong(); }
    public List<ThuCung> timKiem(String tuKhoa) { return tuKhoa == null || tuKhoa.trim().isEmpty() ? layDanhSach() : thuCungDAO.timKiem(tuKhoa.trim()); }
    public ThuCung timTheoMa(String ma) {
        if (ma == null || ma.trim().isEmpty()) throw new IllegalArgumentException("Mã thú cưng không được để trống.");
        return thuCungDAO.timTheoMa(ma.trim());
    }
    public boolean them(ThuCung t) {
        if (t == null) throw new IllegalArgumentException("Thông tin thú cưng không hợp lệ.");
        // Mã được phát sinh tự động trong DAO khi thêm mới.
        t.setMaThuCung("TC-TEMP");
        kiemTra(t, true);
        return thuCungDAO.them(t);
    }
    public boolean capNhat(ThuCung t) {
        kiemTra(t, false);
        if (thuCungDAO.timTheoMa(t.getMaThuCung()) == null) throw new IllegalArgumentException("Không tìm thấy thú cưng cần sửa.");
        return thuCungDAO.capNhat(t);
    }
    public boolean xoa(String ma) {
        if (ma == null || ma.trim().isEmpty()) throw new IllegalArgumentException("Mã thú cưng không được để trống.");
        if (thuCungDAO.timTheoMa(ma.trim()) == null) throw new IllegalArgumentException("Không tìm thấy thú cưng.");
        return thuCungDAO.xoa(ma.trim());
    }
    public List<KhachHang> layDanhSachKhachHang() { return khachHangDAO.layDanhSach(); }
    private void kiemTra(ThuCung t, boolean themMoi) {
        if (t == null) throw new IllegalArgumentException("Thông tin thú cưng không hợp lệ.");
        String ma = trim(t.getMaThuCung()), ten = trim(t.getTenThuCung()), loai = trim(t.getLoai());
        if (!themMoi && ma.isEmpty()) throw new IllegalArgumentException("Mã thú cưng không hợp lệ.");
        if (!themMoi && ma.length() > 20) throw new IllegalArgumentException("Mã thú cưng tối đa 20 ký tự.");
        if (ten.isEmpty() || loai.isEmpty() || trim(t.getGiong()).isEmpty()
                || trim(t.getGioiTinh()).isEmpty() || t.getNgaySinh() == null
                || t.getMaKH() == null || trim(t.getMaKH().getMaKH()).isEmpty()) {
            throw new IllegalArgumentException("Vui lòng nhập đầy đủ thông tin.");
        }
        if (ten.isEmpty()) throw new IllegalArgumentException("Vui lòng nhập tên thú cưng.");
        if (!ten.matches("^[\\p{L}]+(?:\\s+[\\p{L}]+)*$")) throw new IllegalArgumentException("Tên thú cưng chỉ được chứa chữ cái và khoảng trắng, không có số hoặc ký tự đặc biệt.");
        if (ten.length() > 100) throw new IllegalArgumentException("Tên thú cưng tối đa 100 ký tự.");
        if (loai.isEmpty()) throw new IllegalArgumentException("Vui lòng nhập loài thú cưng.");
        if (loai.length() > 50) throw new IllegalArgumentException("Loài tối đa 50 ký tự.");
        String sucKhoe = trim(t.getTinhTrangSK());
        if (!sucKhoe.isEmpty() && !sucKhoe.matches("^[\\p{L}]+(?:\\s+[\\p{L}]+)*$"))
            throw new IllegalArgumentException("Tình trạng sức khỏe chỉ được chứa chữ cái và khoảng trắng, không có số hoặc ký tự đặc biệt.");
        if (trim(t.getGiong()).length() > 100 || trim(t.getGioiTinh()).length() > 20 || sucKhoe.length() > 255 || trim(t.getTiemChung()).length() > 255)
            throw new IllegalArgumentException("Độ dài giống/giới tính/tình trạng sức khỏe/tiêm chủng vượt quá giới hạn.");
        if (t.getCanNang() < -1 || t.getCanNang() == 0) { /* 0 dùng làm giá trị trống từ form; chấp nhận */ }
        if (t.getCanNang() < -1) throw new IllegalArgumentException("Cân nặng không được âm.");
        if (t.getNgaySinh() != null && t.getNgaySinh().isAfter(LocalDate.now())) throw new IllegalArgumentException("Ngày sinh không được ở tương lai.");
        if (t.getMaKH() == null || trim(t.getMaKH().getMaKH()).isEmpty()) throw new IllegalArgumentException("Vui lòng chọn khách hàng sở hữu thú cưng.");
        if (khachHangDAO.timTheoMa(t.getMaKH().getMaKH()) == null) throw new IllegalArgumentException("Khách hàng không tồn tại.");
        if (!themMoi) t.setMaThuCung(ma);
        t.setTenThuCung(ten); t.setLoai(loai);
        t.setGiong(blankNull(t.getGiong())); t.setGioiTinh(blankNull(t.getGioiTinh()));
        t.setTinhTrangSK(blankNull(t.getTinhTrangSK())); t.setTiemChung(blankNull(t.getTiemChung()));
    }
    private String trim(String s) { return s == null ? "" : s.trim(); }
    private String blankNull(String s) { String v = trim(s); return v.isEmpty() ? null : v; }
}
