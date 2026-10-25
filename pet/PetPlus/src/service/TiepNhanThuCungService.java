package service;

import dao.TiepNhanThuCungDAO;
import entity.DichVu;
import entity.KhachHang;
import entity.LichHen;
import entity.NhanVien;
import entity.PhieuTiepNhanThuCung;
import entity.ThuCung;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Nghiệp vụ check-in và theo dõi thú cưng tại cửa hàng. */
public class TiepNhanThuCungService {
    public static final String DA_TIEP_NHAN = "Đã tiếp nhận";
    public static final String DANG_PHUC_VU = "Đang phục vụ";
    public static final String CHO_KHACH_DON = "Chờ khách đón";
    public static final String CHO_THANH_TOAN = "Chờ thanh toán";
    public static final String HOAN_TAT = "Hoàn tất";
    public static final String DA_HUY = "Đã hủy";
    public static final List<String> TRANG_THAI = Arrays.asList(
            DA_TIEP_NHAN, DANG_PHUC_VU, CHO_KHACH_DON, CHO_THANH_TOAN, DA_HUY);

    private final TiepNhanThuCungDAO dao = new TiepNhanThuCungDAO();

    public List<PhieuTiepNhanThuCung> layDanhSach(String tuKhoa) { return dao.layDanhSach(tuKhoa); }
    public List<LichHen> layLichHenChoTiepNhan() { return dao.layLichHenChoTiepNhan(); }
    public List<DichVu> layDichVuTheoLichHen(String maLichHen) { return dao.layDichVuTheoLichHen(maLichHen); }
    public List<DichVu> layDichVuChoTiepNhan() { return dao.layDichVuChoTiepNhan(); }
    public List<KhachHang> timKhachHangTheoSdt(String soDienThoai) {
        if (soDienThoai == null || soDienThoai.replaceAll("\\D", "").length() < 3)
            throw new IllegalArgumentException("Vui lòng nhập ít nhất 3 chữ số điện thoại để tra cứu.");
        return dao.timKhachHangTheoSdt(soDienThoai.replaceAll("\\D", ""));
    }
    public List<ThuCung> layThuCung() { return dao.layThuCung(); }
    public List<NhanVien> layNhanVienChamSocConTrong() { return dao.layNhanVienChamSocConTrong(); }

    public boolean tiepNhan(PhieuTiepNhanThuCung phieu) {
        kiemTra(phieu);
        // DAO cấp mã hóa đơn và lưu thông tin tiếp nhận trực tiếp trên HoaDon.
        phieu.setTrangThai(DA_TIEP_NHAN);
        return dao.them(phieu);
    }

    public boolean capNhatTrangThai(String maTiepNhan, String trangThai) {
        if (maTiepNhan == null || maTiepNhan.trim().isEmpty())
            throw new IllegalArgumentException("Không tìm thấy mã phiếu tiếp nhận.");
        if (!TRANG_THAI.contains(trangThai))
            throw new IllegalArgumentException("Trạng thái tiếp nhận không hợp lệ.");
        return dao.capNhatTrangThai(maTiepNhan.trim(), trangThai);
    }

    private void kiemTra(PhieuTiepNhanThuCung phieu) {
        if (phieu == null) throw new IllegalArgumentException("Thông tin tiếp nhận không hợp lệ.");
        batBuoc(phieu.getMaNV(), "Vui lòng chọn nhân viên tiếp nhận.");
        batBuoc(phieu.getMaKH(), "Vui lòng chọn khách hàng.");
        batBuoc(phieu.getMaThuCung(), "Vui lòng chọn thú cưng.");
        if (phieu.getDanhSachDichVu() == null || phieu.getDanhSachDichVu().isEmpty())
            throw new IllegalArgumentException("Vui lòng thêm ít nhất một dịch vụ vào phiếu tiếp nhận.");
        if (phieu.getMaLichHen() != null && phieu.getMaLichHen().trim().isEmpty()) phieu.setMaLichHen(null);
        if (phieu.getThoiGianTiepNhan() == null) phieu.setThoiGianTiepNhan(LocalDateTime.now());
        if (phieu.getGioHenTra() == null || !phieu.getGioHenTra().isAfter(phieu.getThoiGianTiepNhan()))
            throw new IllegalArgumentException("Giờ hẹn trả phải sau giờ tiếp nhận.");
        gioiHan(phieu.getTinhTrangBanGiao(), 1000, "Tình trạng bàn giao");
        gioiHan(phieu.getGhiChu(), 1000, "Ghi chú");
        Set<String> maDichVuDaChon = new HashSet<>();
        StringBuilder tenDichVu = new StringBuilder();
        BigDecimal tongPhi = BigDecimal.ZERO;
        for (DichVu dichVu : phieu.getDanhSachDichVu()) {
            if (dichVu == null) throw new IllegalArgumentException("Danh sách dịch vụ có dòng không hợp lệ.");
            batBuoc(dichVu.getMaDichVu(), "Có dịch vụ chưa có mã trong database.");
            batBuoc(dichVu.getTenDichVu(), "Có dịch vụ chưa có tên.");
            if (!maDichVuDaChon.add(dichVu.getMaDichVu()))
                throw new IllegalArgumentException("Không thêm trùng một dịch vụ vào cùng phiếu.");
            BigDecimal donGia = dichVu.getDonGia();
            if (donGia == null || donGia.signum() < 0)
                throw new IllegalArgumentException("Đơn giá dịch vụ không hợp lệ: " + dichVu.getTenDichVu());
            if (tenDichVu.length() > 0) tenDichVu.append("; ");
            tenDichVu.append(dichVu.getTenDichVu().trim());
            tongPhi = tongPhi.add(donGia);
        }
        gioiHan(tenDichVu.toString(), 500, "Danh sách dịch vụ");
        phieu.setDichVuYeuCau(tenDichVu.toString());
        phieu.setPhiDuKien(tongPhi);
        phieu.setMaNV(phieu.getMaNV().trim());
        phieu.setMaKH(phieu.getMaKH().trim());
        phieu.setMaThuCung(phieu.getMaThuCung().trim());
        phieu.setDichVuYeuCau(phieu.getDichVuYeuCau().trim());
    }

    private void batBuoc(String giaTri, String loi) {
        if (giaTri == null || giaTri.trim().isEmpty()) throw new IllegalArgumentException(loi);
    }

    private void gioiHan(String giaTri, int toiDa, String ten) {
        if (giaTri != null && giaTri.length() > toiDa)
            throw new IllegalArgumentException(ten + " tối đa " + toiDa + " ký tự.");
    }
}
