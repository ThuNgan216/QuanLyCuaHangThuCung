package service;

import dao.LichHenDAO;
import entity.LichHen;
import java.util.List;
import java.time.LocalDateTime;
import java.time.Duration;

public class LichHenService {
    private final LichHenDAO dao=new LichHenDAO();
    public List<LichHen> layDanhSach(){return dao.layDanhSach();}
    public List<LichHen> timKiem(String key){return dao.timKiem(key);}
    public List<entity.NhanVien> layNhanVien(){return dao.layNhanVien();}
    public List<entity.KhachHang> timKhachHang(String key){return dao.timKhachHang(key);}
    public List<entity.ThuCung> layThuCungTheoKhach(String maKH,String key){return dao.layThuCungTheoKhach(maKH,key);}
    public String taoMaMoi(){return dao.taoMaMoi();}
    public boolean them(LichHen l){kiemTra(l,null);if(dao.tonTai(l.getMaLichHen().trim()))throw new IllegalArgumentException("Mã lịch hẹn đã tồn tại.");kiemTraTrungLich(l, null);return dao.them(l);}
    public boolean capNhat(LichHen l){LichHen cu=l==null||l.getMaLichHen()==null?null:dao.timTheoMa(l.getMaLichHen().trim());if(cu==null)throw new IllegalArgumentException("Không tìm thấy lịch hẹn.");kiemTra(l,cu);kiemTraTrungLich(l, l.getMaLichHen().trim());return dao.capNhat(l);}

    /** Một lịch chiếm [thời điểm bắt đầu, thời điểm bắt đầu + 30 phút). */
    private void kiemTraTrungLich(LichHen moi, String maLichDangSua) {
        LocalDateTime batDauMoi = moi.getThoiGianHen();
        LocalDateTime ketThucMoi = batDauMoi.plusMinutes(30);
        String maNVMoi = moi.getMaNV().getMaNV();
        for (LichHen cu : dao.layDanhSach()) {
            if (cu == null || cu.getThoiGianHen() == null || cu.getMaNV() == null || cu.getMaNV().getMaNV() == null) continue;
            if (maLichDangSua != null && maLichDangSua.equalsIgnoreCase(cu.getMaLichHen())) continue;
            if (!maNVMoi.equalsIgnoreCase(cu.getMaNV().getMaNV())) continue;
            if (!batDauMoi.toLocalDate().equals(cu.getThoiGianHen().toLocalDate())) continue;
            LocalDateTime batDauCu = cu.getThoiGianHen();
            LocalDateTime ketThucCu = batDauCu.plusMinutes(30);
            // Dùng khoảng nửa kín: lịch kết thúc đúng lúc lịch khác bắt đầu thì không trùng.
            boolean chongLan = batDauMoi.isBefore(ketThucCu) && ketThucMoi.isAfter(batDauCu);
            if (chongLan) {
                throw new IllegalArgumentException("Nhân viên " + maNVMoi + " đã có lịch " + cu.getMaLichHen()
                    + " lúc " + batDauCu.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                    + ". Mỗi ca kéo dài 30 phút nên không thể đặt lịch chồng lấn.");
            }
        }
    }
    public boolean xoa(String ma){if(ma==null||ma.trim().isEmpty())throw new IllegalArgumentException("Vui lòng chọn lịch hẹn cần xóa.");return dao.xoa(ma.trim());}
    /** @param cu lịch hiện có trong DB khi cập nhật; null khi thêm mới. */
    private void kiemTra(LichHen l, LichHen cu){if(l==null)throw new IllegalArgumentException("Thông tin lịch hẹn không hợp lệ.");if(l.getMaLichHen()==null||l.getMaLichHen().trim().isEmpty())throw new IllegalArgumentException("Mã lịch hẹn không được để trống.");if(l.getMaLichHen().trim().length()>20)throw new IllegalArgumentException("Mã lịch hẹn tối đa 20 ký tự.");if(l.getThoiGianHen()==null)throw new IllegalArgumentException("Vui lòng nhập thời gian hẹn.");
    boolean doiGio=cu==null||cu.getThoiGianHen()==null||!cu.getThoiGianHen().equals(l.getThoiGianHen());
    if(doiGio&&!l.getThoiGianHen().isAfter(LocalDateTime.now()))throw new IllegalArgumentException("Thời gian hẹn phải ở tương lai, không được chọn thời điểm hiện tại hoặc đã qua.");if(!entity.TrangThaiLichHen.laTrangThaiQuanLyHopLe(l.getTrangThai()))throw new IllegalArgumentException("Trạng thái lịch hẹn không hợp lệ.");if(l.getMaNV()==null||l.getMaNV().getMaNV()==null||l.getMaNV().getMaNV().trim().isEmpty())throw new IllegalArgumentException("Vui lòng chọn nhân viên.");
    if(l.getMaKH()==null||l.getMaKH().getMaKH()==null||l.getMaKH().getMaKH().trim().isEmpty())throw new IllegalArgumentException("Vui lòng chọn khách hàng.");if(l.getMaThuCung()==null||l.getMaThuCung().getMaThuCung()==null||l.getMaThuCung().getMaThuCung().trim().isEmpty())throw new IllegalArgumentException("Vui lòng chọn thú cưng.");l.setMaLichHen(l.getMaLichHen().trim());l.setTrangThai(l.getTrangThai().trim());}
}
