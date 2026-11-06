package gui.panel;

import entity.DichVu;
import gui.dialogs.dlgDichVu;
import service.DichVuService;
import utils.Auth;

import javax.swing.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Quản lý dịch vụ. Bảng gồm hai loại: dịch vụ trong danh mục (cửa hàng cung cấp) và dịch vụ gắn với một
 * lịch hẹn cụ thể (sinh ra khi tiếp nhận). Hai loại có tập trạng thái khác nhau nên có cột "Loại" và bộ lọc.
 * Khung màn hình dùng chung ở pnlQuanLyChung; luật nghiệp vụ ở DichVuService.
 */
public class pnlDichVu extends pnlQuanLyChung<DichVu> {
    private static final String TAT_CA = "Tất cả";
    private static final String DANH_MUC = "Danh mục";
    private static final String THEO_LICH = "Theo lịch hẹn";
    private static final DecimalFormat TIEN = new DecimalFormat("#,##0");

    private final DichVuService service = new DichVuService();
    private final JComboBox<String> cboLoai = new JComboBox<>(new String[] { TAT_CA, DANH_MUC, THEO_LICH });

    public pnlDichVu() {
        super("QUẢN LÝ DỊCH VỤ", "dịch vụ", "Mã, tên, mô tả hoặc trạng thái dịch vụ",
                new String[] { "Mã dịch vụ", "Tên dịch vụ", "Đơn giá (đ)", "Mô tả", "Trạng thái", "Loại" });
        pnlBoLoc.add(new JLabel("Loại:"));
        pnlBoLoc.add(cboLoai);
        cboLoai.addActionListener(e -> taiLai());
        khoiTao();
    }

    /** Tìm theo từ khóa rồi lọc theo loại (danh mục / theo lịch hẹn). */
    @Override
    protected List<DichVu> layDuLieu(String tuKhoa) {
        String loai = (String) cboLoai.getSelectedItem();
        List<DichVu> loc = new ArrayList<>();
        for (DichVu d : service.timKiem(tuKhoa)) {
            boolean danhMuc = DichVuService.laDanhMuc(d);
            if (TAT_CA.equals(loai) || (DANH_MUC.equals(loai) && danhMuc) || (THEO_LICH.equals(loai) && !danhMuc))
                loc.add(d);
        }
        return loc;
    }

    @Override
    protected Object[] taoDong(DichVu d) {
        String loai = DichVuService.laDanhMuc(d) ? DANH_MUC : THEO_LICH + " " + d.getMaLichHen().getMaLichHen();
        return new Object[] { d.getMaDichVu(), d.getTenDichVu(), d.getDonGia() == null ? "" : TIEN.format(d.getDonGia()),
                d.getMoTa(), d.getTrangThai(), loai };
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
        return "(Nếu dịch vụ đã nằm trong hóa đơn, hệ thống chỉ ẩn nó đi để giữ lịch sử.)";
    }

    @Override
    protected void datLaiBoLoc() {
        cboLoai.setSelectedItem(TAT_CA);
    }
}
