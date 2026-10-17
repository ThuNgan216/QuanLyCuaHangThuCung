package service;

import dao.LichHenDAO;
import entity.LichHen;
import java.time.LocalDateTime;
import java.util.List;

public class LichHenService {
    private final LichHenDAO dao=new LichHenDAO();
    public List<LichHen> layDanhSach(){return dao.layDanhSach();}
    public List<LichHen> timKiem(String key){return dao.timKiem(key);}
    public List<String> layMaNhanVien(){return dao.layMaNhanVien();}
    public List<String> layMaKhachHang(){return dao.layMaKhachHang();}
    public List<String> layMaThuCung(){return dao.layMaThuCung();}
    public boolean them(LichHen l){kiemTra(l);if(dao.tonTai(l.getMaLichHen().trim()))throw new IllegalArgumentException("Mã lịch hẹn đã tồn tại.");return dao.them(l);}
    public boolean capNhat(LichHen l){kiemTra(l);if(!dao.tonTai(l.getMaLichHen().trim()))throw new IllegalArgumentException("Không tìm thấy lịch hẹn.");return dao.capNhat(l);}
    public boolean xoa(String ma){if(ma==null||ma.trim().isEmpty())throw new IllegalArgumentException("Vui lòng chọn lịch hẹn cần xóa.");return dao.xoa(ma.trim());}
    private void kiemTra(LichHen l){if(l==null)throw new IllegalArgumentException("Thông tin lịch hẹn không hợp lệ.");if(l.getMaLichHen()==null||l.getMaLichHen().trim().isEmpty())throw new IllegalArgumentException("Mã lịch hẹn không được để trống.");if(l.getMaLichHen().trim().length()>20)throw new IllegalArgumentException("Mã lịch hẹn tối đa 20 ký tự.");if(l.getThoiGianHen()==null)throw new IllegalArgumentException("Vui lòng nhập thời gian hẹn.");if(l.getTrangThai()==null||l.getTrangThai().trim().isEmpty())throw new IllegalArgumentException("Vui lòng chọn trạng thái.");if(l.getMaNV()==null||l.getMaNV().getMaNV()==null||l.getMaNV().getMaNV().trim().isEmpty())throw new IllegalArgumentException("Vui lòng chọn nhân viên.");if(l.getMaKH()==null||l.getMaKH().getMaKH()==null||l.getMaKH().getMaKH().trim().isEmpty())throw new IllegalArgumentException("Vui lòng chọn khách hàng.");if(l.getMaThuCung()==null||l.getMaThuCung().getMaThuCung()==null||l.getMaThuCung().getMaThuCung().trim().isEmpty())throw new IllegalArgumentException("Vui lòng chọn thú cưng.");l.setMaLichHen(l.getMaLichHen().trim());l.setTrangThai(l.getTrangThai().trim());}
}
