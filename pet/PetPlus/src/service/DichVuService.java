package service;

import dao.DichVuDAO;
import entity.DichVu;
import java.math.BigDecimal;
import java.util.List;

public class DichVuService {
    private final DichVuDAO dao = new DichVuDAO();
    public List<DichVu> layDanhSach() { return dao.layDanhSach(); }
    public List<DichVu> timKiem(String tuKhoa) { return dao.timKiem(tuKhoa); }
    public String taoMaMoi() { return dao.taoMaMoi(); }
    public boolean them(DichVu d) { kiemTra(d, true); if (tonTaiMa(d.getMaDichVu())) throw new IllegalArgumentException("Mã dịch vụ đã tồn tại."); return dao.them(d); }
    public boolean capNhat(DichVu d) { kiemTra(d, false); if (!tonTaiMa(d.getMaDichVu())) throw new IllegalArgumentException("Không tìm thấy dịch vụ cần sửa."); return dao.capNhat(d); }
    public boolean xoa(String ma) { if (ma == null || ma.trim().isEmpty()) throw new IllegalArgumentException("Vui lòng chọn dịch vụ cần xóa."); return dao.xoa(ma.trim()); }
    private boolean tonTaiMa(String ma) { for (DichVu d : dao.layDanhSach()) if (d.getMaDichVu().equalsIgnoreCase(ma.trim())) return true; return false; }
    private void kiemTra(DichVu d, boolean themMoi) {
        if (d == null) throw new IllegalArgumentException("Thông tin dịch vụ không hợp lệ.");
        if (d.getMaDichVu() == null || d.getMaDichVu().trim().isEmpty()) throw new IllegalArgumentException("Mã dịch vụ không được để trống.");
        if (d.getMaDichVu().trim().length() > 20) throw new IllegalArgumentException("Mã dịch vụ tối đa 20 ký tự.");
        if (d.getTenDichVu() == null || d.getTenDichVu().trim().isEmpty()) throw new IllegalArgumentException("Tên dịch vụ không được để trống.");
        if (d.getTenDichVu().trim().length() > 150) throw new IllegalArgumentException("Tên dịch vụ tối đa 150 ký tự.");
        BigDecimal gia = d.getDonGia();
        if (gia == null || gia.compareTo(BigDecimal.ZERO) < 0) throw new IllegalArgumentException("Đơn giá phải lớn hơn hoặc bằng 0.");
        if (d.getMoTa() != null && d.getMoTa().length() > 500) throw new IllegalArgumentException("Mô tả tối đa 500 ký tự.");
        if (d.getTrangThai() == null || d.getTrangThai().trim().isEmpty()) throw new IllegalArgumentException("Vui lòng chọn trạng thái.");
        d.setMaDichVu(d.getMaDichVu().trim()); d.setTenDichVu(d.getTenDichVu().trim());
        if (d.getMoTa() != null) d.setMoTa(d.getMoTa().trim()); d.setTrangThai(d.getTrangThai().trim());
    }
}
