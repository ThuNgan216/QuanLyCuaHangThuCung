package gui.panel;

import entity.ThuCung;
import gui.dialogs.dlgThuCung;
import service.ThuCungService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class pnlThuCung extends JPanel {
    private final ThuCungService service = new ThuCungService();
    private final JTextField txtTimKiem = new JTextField(22);
    private final DefaultTableModel model = new DefaultTableModel(new Object[]{"Mã thú cưng", "Tên", "Loài", "Giống", "Giới tính", "Cân nặng (kg)", "Ngày sinh", "Sức khỏe", "Tiêm chủng", "Mã khách hàng"}, 0) {
        @Override public boolean isCellEditable(int row, int column) { return false; }
    };
    private final JTable table = new JTable(model);

    public pnlThuCung() {
        setLayout(new BorderLayout(10, 10)); setBackground(Color.WHITE); setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        JLabel title = new JLabel("QUẢN LÝ THÚ CƯNG"); title.setFont(new Font("Arial", Font.BOLD, 23)); title.setForeground(new Color(224, 25, 91));
        JPanel top = new JPanel(new BorderLayout(8, 8)); top.setBackground(Color.WHITE); top.add(title, BorderLayout.NORTH);
        JPanel search = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8)); search.setBackground(Color.WHITE);
        JButton btnTim = new JButton("Tìm kiếm"), btnLamMoi = new JButton("Làm mới"); search.add(new JLabel("Mã / Tên / Loài / Giống / Khách hàng:")); search.add(txtTimKiem); search.add(btnTim); search.add(btnLamMoi); top.add(search, BorderLayout.CENTER); add(top, BorderLayout.NORTH);
        table.setRowHeight(26); table.setAutoCreateRowSorter(true); table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12)); add(new JScrollPane(table), BorderLayout.CENTER);
        JButton btnThem = new JButton("Thêm thú cưng"), btnSua = new JButton("Sửa"), btnXoa = new JButton("Xóa"), btnTai = new JButton("Tải danh sách");
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4)); bottom.setBackground(Color.WHITE); bottom.add(btnTai); bottom.add(btnThem); bottom.add(btnSua); bottom.add(btnXoa); add(bottom, BorderLayout.SOUTH);
        btnTim.addActionListener(e -> timKiem()); txtTimKiem.addActionListener(e -> timKiem()); btnLamMoi.addActionListener(e -> { txtTimKiem.setText(""); taiDanhSach(); }); btnTai.addActionListener(e -> taiDanhSach());
        btnThem.addActionListener(e -> them()); btnSua.addActionListener(e -> sua()); btnXoa.addActionListener(e -> xoa()); taiDanhSach();
        utils.PetPlusTheme.apply(this);
    }
    private void taiDanhSach() { try { hienThi(service.layDanhSach()); } catch (Exception ex) { loi("Không thể tải danh sách thú cưng.", ex); } }
    private void timKiem() { try { hienThi(service.timKiem(txtTimKiem.getText())); } catch (Exception ex) { loi("Tìm kiếm thất bại.", ex); } }
    private void hienThi(List<ThuCung> ds) {
        model.setRowCount(0);
        for (ThuCung t : ds) model.addRow(new Object[]{t.getMaThuCung(), t.getTenThuCung(), t.getLoai(), t.getGiong(), t.getGioiTinh(), t.getCanNang() < 0 ? "" : t.getCanNang(), t.getNgaySinh(), t.getTinhTrangSK(), t.getTiemChung(), t.getMaKH() == null ? "" : t.getMaKH().getMaKH()});
    }
    private void them() {
        dlgThuCung dlg = new dlgThuCung(SwingUtilities.getWindowAncestor(this), null); dlg.setVisible(true);
        if (dlg.isSaved()) try { if (service.them(dlg.getThuCung())) { thongBao("Thêm thú cưng thành công."); taiDanhSach(); } else thongBao("Không thêm được thú cưng. Hãy kiểm tra kết nối/database."); } catch (Exception ex) { loi("Thêm thú cưng thất bại.", ex); }
    }
    private void sua() {
        ThuCung selected = layDongChon(); if (selected == null) { thongBao("Vui lòng chọn thú cưng cần sửa."); return; }
        try { ThuCung full = service.timTheoMa(selected.getMaThuCung()); if (full == null) { thongBao("Không tìm thấy thú cưng."); taiDanhSach(); return; }
            dlgThuCung dlg = new dlgThuCung(SwingUtilities.getWindowAncestor(this), full); dlg.setVisible(true);
            if (dlg.isSaved()) try { if (service.capNhat(dlg.getThuCung())) { thongBao("Cập nhật thú cưng thành công."); taiDanhSach(); } else thongBao("Không cập nhật được thú cưng."); } catch (Exception ex) { loi("Cập nhật thất bại.", ex); }
        } catch (Exception ex) { loi("Không đọc được thông tin thú cưng.", ex); }
    }
    private void xoa() {
        ThuCung selected = layDongChon(); if (selected == null) { thongBao("Vui lòng chọn thú cưng cần xóa."); return; }
        if (JOptionPane.showConfirmDialog(this, "Xóa thú cưng " + selected.getMaThuCung() + " - " + selected.getTenThuCung() + "?", "Xác nhận", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
        try { if (service.xoa(selected.getMaThuCung())) { thongBao("Đã xóa thú cưng."); taiDanhSach(); } else thongBao("Không xóa được thú cưng."); } catch (Exception ex) { loi("Xóa thú cưng thất bại.", ex); }
    }
    private ThuCung layDongChon() {
        int row = table.getSelectedRow(); if (row < 0) return null; row = table.convertRowIndexToModel(row);
        ThuCung t = new ThuCung(); t.setMaThuCung(String.valueOf(model.getValueAt(row, 0))); return t;
    }
    private void thongBao(String msg) { JOptionPane.showMessageDialog(this, msg); }
    private void loi(String msg, Exception ex) { JOptionPane.showMessageDialog(this, msg + "\n" + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE); }
}
