
package gui.panel;

import entity.DichVu;
import gui.dialogs.dlgDichVu;
import service.DichVuService;
import utils.Auth;

import java.text.DecimalFormat;
import java.util.List;

/**
 * Quản lý danh mục dịch vụ của cửa hàng: thêm dịch vụ mới, sửa giá, ngừng cung cấp, xóa.
 * Khung màn hình dùng chung ở pnlQuanLyChung; luật nghiệp vụ ở DichVuService.
 */
public class pnlDichVu extends pnlQuanLyChung<DichVu> {
    private static final DecimalFormat TIEN = new DecimalFormat("#,##0");

    private final DichVuService service = new DichVuService();

    public pnlDichVu() {
        super("QUẢN LÝ DỊCH VỤ", "dịch vụ", "Mã, tên, mô tả hoặc trạng thái dịch vụ",
                new String[] { "Mã dịch vụ", "Tên dịch vụ", "Đơn giá (đ)", "Mô tả", "Trạng thái" });
        khoiTao();
    }

    @Override
    protected List<DichVu> layDuLieu(String tuKhoa) {
        return service.timKiem(tuKhoa);
    }

    @Override
    protected Object[] taoDong(DichVu d) {
        return new Object[] { d.getMaDichVu(), d.getTenDichVu(), d.getDonGia() == null ? "" : TIEN.format(d.getDonGia()),
                d.getMoTa(), d.getTrangThai() };
    }

    @Override
    protected DichVu timTheoMa(String ma) {
        return service.timTheoMa(ma);
    }

    @Override
    protected boolean moDialog(DichVu cu) {
        dlgDichVu dlg = new dlgDichVu(cuaSoCha(), cu);
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
        return "(Nếu dịch vụ đã nằm trong hóa đơn, hệ thống chỉ ẩn nó đi để giữ lịch sử. "
                + "Muốn tạm ngừng cung cấp thì dùng Sửa và chọn \"Ngừng hoạt động\".)";
    }
}
