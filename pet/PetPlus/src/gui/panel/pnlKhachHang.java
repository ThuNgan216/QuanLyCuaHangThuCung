package gui.panel;

import entity.HangThanhVien;
import entity.KhachHang;
import gui.dialogs.dlgKhachHang;
import service.KhachHangService;

import javax.swing.*;
import java.util.List;

/** Quản lý khách hàng. Khung màn hình dùng chung ở pnlQuanLyChung; luật nghiệp vụ ở KhachHangService. */
public class pnlKhachHang extends pnlQuanLyChung<KhachHang> {
    private static final String TAT_CA_HANG = "Tất cả hạng";

    private final KhachHangService service = new KhachHangService();
    private final JComboBox<String> cboHang = new JComboBox<>();

    public pnlKhachHang() {
        super("QUẢN LÝ KHÁCH HÀNG", "khách hàng", "Mã, tên hoặc số điện thoại",
                new String[] { "Mã khách hàng", "Tên khách hàng", "Số điện thoại", "Địa chỉ", "Hạng thành viên",
                        "Điểm tích lũy" });
        cboHang.addItem(TAT_CA_HANG);
        for (HangThanhVien h : HangThanhVien.values())
            cboHang.addItem(h.getTen());
        pnlBoLoc.add(new JLabel("Hạng thành viên:"));
        pnlBoLoc.add(cboHang);
        cboHang.addActionListener(e -> taiLai());
        khoiTao();
    }

    /** Hạng đang chọn trong bộ lọc; null nghĩa là tất cả các hạng. */
    private HangThanhVien hangDangChon() {
        String ten = (String) cboHang.getSelectedItem();
        for (HangThanhVien h : HangThanhVien.values())
            if (h.getTen().equals(ten))
                return h;
        return null;
    }

    @Override
    protected List<KhachHang> layDuLieu(String tuKhoa) {
        return service.timKiem(tuKhoa, hangDangChon());
    }

    @Override
    protected Object[] taoDong(KhachHang kh) {
        return new Object[] { kh.getMaKH(), kh.getTenKH(), kh.getSdt(), kh.getDiaChi(),
                HangThanhVien.tuMa(kh.getHangThanhVien()).getTen(), kh.getDiemTichLuy() };
    }

    @Override
    protected KhachHang timTheoMa(String ma) {
        return service.timTheoMa(ma);
    }

    @Override
    protected boolean moDialog(KhachHang cu) {
        dlgKhachHang dlg = new dlgKhachHang(cuaSoCha(), cu);
        dlg.setVisible(true);
        return dlg.isSaved();
    }

    @Override
    protected boolean xoaTheoMa(String ma) {
        return service.xoa(ma);
    }

    @Override
    protected boolean coQuyenCapNhat() {
        return true;
    }

    @Override
    protected String canhBaoKhiXoa() {
        return "(Không xóa được nếu khách còn thú cưng, còn lịch hẹn chưa kết thúc hoặc đã có hóa đơn.)";
    }

    @Override
    protected void datLaiBoLoc() {
        cboHang.setSelectedIndex(0);
    }
}
