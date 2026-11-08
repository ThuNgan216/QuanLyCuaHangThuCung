package gui.panel;

import entity.ThuCung;
import gui.dialogs.dlgThuCung;
import service.ThuCungService;
import utils.Auth;

import java.util.List;

/** Quản lý thú cưng. Khung màn hình dùng chung ở pnlQuanLyChung; luật nghiệp vụ ở ThuCungService. */
public class pnlThuCung extends pnlQuanLyChung<ThuCung> {
    private final ThuCungService service = new ThuCungService();

    public pnlThuCung() {
        super("QUẢN LÝ THÚ CƯNG", "thú cưng", "Mã, tên, loài, giống thú cưng hoặc tên / SĐT chủ nuôi",
                new String[] { "Mã thú cưng", "Tên", "Loài", "Giống", "Giới tính", "Cân nặng (kg)", "Ngày sinh",
                        "Sức khỏe", "Tiêm chủng", "Mã khách hàng" });
        khoiTao();
    }

    @Override
    protected List<ThuCung> layDuLieu(String tuKhoa) {
        return service.timKiem(tuKhoa);
    }

    @Override
    protected Object[] taoDong(ThuCung t) {
        return new Object[] { t.getMaThuCung(), t.getTenThuCung(), t.getLoai(), t.getGiong(), t.getGioiTinh(),
                t.getCanNang() < 0 ? "" : t.getCanNang(), t.getNgaySinh(), t.getTinhTrangSK(), t.getTiemChung(),
                t.getMaKH() == null ? "" : t.getMaKH().getMaKH() };
    }

    @Override
    protected ThuCung timTheoMa(String ma) {
        return service.timTheoMa(ma);
    }

    @Override
    protected boolean moDialog(ThuCung cu) {
        dlgThuCung dlg = new dlgThuCung(cuaSoCha(), cu);
        dlg.setVisible(true);
        return dlg.isSaved();
    }

    @Override
    protected boolean xoaTheoMa(String ma) {
        return service.xoa(ma);
    }

    @Override
    protected boolean coQuyenCapNhat() {
        return Auth.hasFullAccess() || Auth.hasRole("CHAM_SOC");
    }

    @Override
    protected String canhBaoKhiXoa() {
        return "(Không xóa được nếu còn lịch hẹn chưa kết thúc; nếu đã có lịch sử lịch hẹn/hóa đơn, "
                + "thú cưng chỉ bị ẩn khỏi danh sách.)";
    }
}
