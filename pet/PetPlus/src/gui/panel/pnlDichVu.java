package gui.panel;

import entity.DichVu;
import gui.dialogs.dlgDichVu;
import service.DichVuService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class pnlDichVu extends JPanel {
    private final DichVuService service = new DichVuService();
    private final JTextField txtTim = new JTextField(22);
    private final DefaultTableModel model = new DefaultTableModel(new Object[]{"Mã dịch vụ", "Tên dịch vụ", "Đơn giá", "Mô tả", "Trạng thái", "Mã lịch hẹn"}, 0) {
        @Override public boolean isCellEditable(int row, int col) { return false; }
    };
    private final JTable table = new JTable(model);

    public pnlDichVu() {
        setLayout(new BorderLayout(10, 10)); setBackground(Color.WHITE); setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));
        JLabel title = new JLabel("QUẢN LÝ DỊCH VỤ"); title.setFont(new Font("Arial", Font.BOLD, 22)); title.setForeground(new Color(224, 25, 91)); add(title, BorderLayout.NORTH);
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8)); top.setOpaque(false);
        top.add(new JLabel("Tìm kiếm:")); top.add(txtTim);
        JButton btnTim = new JButton("Tìm"); JButton btnTai = new JButton("Tải lại"); JButton btnThem = new JButton("Thêm"); JButton btnSua = new JButton("Sửa"); JButton btnXoa = new JButton("Xóa");
        top.add(btnTim); top.add(btnTai); top.add(btnThem); top.add(btnSua); top.add(btnXoa); add(top, BorderLayout.BEFORE_FIRST_LINE);
        table.setRowHeight(27); table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12)); add(new JScrollPane(table), BorderLayout.CENTER);
        JLabel note = new JLabel("Theo cấu trúc database hiện tại, mỗi dịch vụ phải gắn với một mã lịch hẹn."); note.setForeground(new Color(89, 111, 140)); add(note, BorderLayout.SOUTH);
        btnTim.addActionListener(e -> tim()); txtTim.addActionListener(e -> tim()); btnTai.addActionListener(e -> { txtTim.setText(""); tai(); });
        btnThem.addActionListener(e -> moDialog(null)); btnSua.addActionListener(e -> { DichVu d = chon(); if (d != null) moDialog(d); }); btnXoa.addActionListener(e -> xoa());
        tai();
        utils.PetPlusTheme.apply(this);
    }

    private void tai() { try { hienThi(service.layDanhSach()); } catch (Exception ex) { loi("Không thể tải danh sách dịch vụ", ex); } }
    private void tim() { try { hienThi(service.timKiem(txtTim.getText())); } catch (Exception ex) { loi("Không thể tìm dịch vụ", ex); } }
    private void hienThi(List<DichVu> ds) {
        model.setRowCount(0);
        for (DichVu d : ds) model.addRow(new Object[]{d.getMaDichVu(), d.getTenDichVu(), d.getDonGia(), d.getMoTa(), d.getTrangThai(), d.getMaLichHen() == null ? "" : d.getMaLichHen().getMaLichHen()});
    }
    private DichVu chon() {
        int row = table.getSelectedRow(); if (row < 0) { JOptionPane.showMessageDialog(this, "Vui lòng chọn một dịch vụ."); return null; }
        int modelRow = table.convertRowIndexToModel(row); String ma = String.valueOf(model.getValueAt(modelRow, 0));
        for (DichVu d : service.layDanhSach()) if (d.getMaDichVu().equals(ma)) return d; return null;
    }
    private void moDialog(DichVu cu) {
        try {
            dlgDichVu dlg = new dlgDichVu(SwingUtilities.getWindowAncestor(this), cu); dlg.setVisible(true);
            if (dlg.isConfirmed()) {
                DichVu d = dlg.getDichVu(); boolean ok = cu == null ? service.them(d) : service.capNhat(d);
                if (ok) { JOptionPane.showMessageDialog(this, "Lưu dịch vụ thành công."); tai(); }
                else JOptionPane.showMessageDialog(this, "Không có dữ liệu nào được thay đổi.");
            }
        } catch (Exception ex) { loi("Không thể lưu dịch vụ", ex); }
    }
    private void xoa() {
        int row = table.getSelectedRow(); if (row < 0) { JOptionPane.showMessageDialog(this, "Vui lòng chọn dịch vụ cần xóa."); return; }
        String ma = String.valueOf(model.getValueAt(table.convertRowIndexToModel(row), 0));
        if (JOptionPane.showConfirmDialog(this, "Bạn có chắc muốn xóa dịch vụ " + ma + "?", "Xác nhận", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            try { if (service.xoa(ma)) { JOptionPane.showMessageDialog(this, "Đã xóa dịch vụ."); tai(); } else JOptionPane.showMessageDialog(this, "Không tìm thấy dịch vụ."); }
            catch (Exception ex) { loi("Không thể xóa dịch vụ. Có thể dữ liệu đang được tham chiếu.", ex); }
        }
    }
    private void loi(String msg, Exception ex) { JOptionPane.showMessageDialog(this, msg + ":\n" + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE); }
}
