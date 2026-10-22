package gui.panel;

import entity.DichVu;
import gui.dialogs.dlgDichVu;
import service.DichVuService;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class pnlDichVu extends JPanel {
    private static final Color PRIMARY = new Color(245, 70, 113);
    private static final Color PRIMARY_DARK = new Color(220, 27, 86);
    private static final Color LIGHT_PINK = new Color(255, 236, 243);
    private static final Color BORDER = new Color(237, 217, 225);
    private final DichVuService service = new DichVuService();
    private final JTextField txtTim = new JTextField(22);
    private final DefaultTableModel model = new DefaultTableModel(
            new Object[] { "Mã dịch vụ", "Tên dịch vụ", "Đơn giá", "Mô tả", "Trạng thái" }, 0) {
        @Override
        public boolean isCellEditable(int row, int col) {
            return false;
        }
    };
    private final JTable table = new JTable(model);

    public pnlDichVu() {
        setLayout(new BorderLayout(10, 12));
        setBackground(new Color(255, 247, 250));
        setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));
        JLabel title = new JLabel("QUẢN LÝ DỊCH VỤ");
        title.setFont(new Font("Arial", Font.BOLD, 22));
        title.setForeground(PRIMARY_DARK);
        JPanel header = new JPanel(new BorderLayout(8, 8));
        header.setOpaque(false);
        header.add(title, BorderLayout.NORTH);
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        top.setOpaque(false);
        top.add(new JLabel("Tìm mã / tên / mô tả:"));
        top.add(txtTim);
        JButton btnTim = taoNut("Tìm kiếm", PRIMARY);
        JButton btnTai = taoNut("Làm mới", new Color(100, 116, 139));
        JButton btnThem = taoNut("+ Thêm dịch vụ", PRIMARY);
        JButton btnSua = taoNut("Sửa", new Color(52, 120, 190));
        JButton btnXoa = taoNut("Xóa", new Color(200, 60, 75));
        top.add(btnTim);
        top.add(btnTai);
        top.add(btnThem);
        top.add(btnSua);
        top.add(btnXoa);
        header.add(top, BorderLayout.CENTER);
        add(header, BorderLayout.NORTH);

        table.setFont(new Font("Arial", Font.PLAIN, 13));
        table.setRowHeight(34);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setFillsViewportHeight(true);
        table.setShowGrid(true);
        table.setGridColor(BORDER);
        table.setIntercellSpacing(new Dimension(0, 1));
        table.setBackground(Color.WHITE);
        table.setForeground(new Color(32, 44, 64));
        table.setSelectionBackground(LIGHT_PINK);
        table.setSelectionForeground(new Color(32, 44, 64));
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 13));
        table.getTableHeader().setBackground(new Color(255, 224, 235));
        table.getTableHeader().setForeground(new Color(100, 30, 55));
        table.getTableHeader().setPreferredSize(new Dimension(0, 38));
        table.getTableHeader().setReorderingAllowed(false);
        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(SwingConstants.CENTER);
        table.getColumnModel().getColumn(0).setCellRenderer(center);
        table.getColumnModel().getColumn(4).setCellRenderer(center);
        table.getColumnModel().getColumn(2).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            protected void setValue(Object value) {
                if (value instanceof Number)
                    setText(NumberFormat.getNumberInstance(new Locale("vi", "VN")).format(value) + " đ");
                else
                    setText(value == null ? "" : value.toString());
                setHorizontalAlignment(SwingConstants.RIGHT);
            }
        });
        table.getColumnModel().getColumn(0).setPreferredWidth(100);
        table.getColumnModel().getColumn(1).setPreferredWidth(190);
        table.getColumnModel().getColumn(2).setPreferredWidth(130);
        table.getColumnModel().getColumn(3).setPreferredWidth(260);
        table.getColumnModel().getColumn(4).setPreferredWidth(130);
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(BORDER));
        scroll.getViewport().setBackground(Color.WHITE);
        add(scroll, BorderLayout.CENTER);

        btnTim.addActionListener(e -> tim());
        txtTim.addActionListener(e -> tim());
        btnTai.addActionListener(e -> {
            txtTim.setText("");
            tai();
        });
        btnThem.addActionListener(e -> moDialog(null));
        btnSua.addActionListener(e -> {
            DichVu d = chon();
            if (d != null)
                moDialog(d);
        });
        btnXoa.addActionListener(e -> xoa());
        tai();
        utils.PetPlusTheme.apply(this);
    }

    private JButton taoNut(String text, Color color) {
        JButton b = new JButton(text);
        b.setFont(new Font("Arial", Font.BOLD, 12));
        b.setForeground(Color.WHITE);
        b.setBackground(color);
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createEmptyBorder(8, 13, 8, 13));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setOpaque(true);
        return b;
    }

    private void tai() {
        try {
            hienThi(service.layDanhSach());
        } catch (Exception ex) {
            loi("Không thể tải danh sách dịch vụ", ex);
        }
    }

    private void tim() {
        try {
            hienThi(service.timKiem(txtTim.getText()));
        } catch (Exception ex) {
            loi("Không thể tìm dịch vụ", ex);
        }
    }

    private void hienThi(List<DichVu> ds) {
        model.setRowCount(0);
        for (DichVu d : ds)
            model.addRow(new Object[] { d.getMaDichVu(), d.getTenDichVu(), d.getDonGia(),
                    d.getMoTa() == null ? "" : d.getMoTa(), d.getTrangThai() });
    }

    private DichVu chon() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một dịch vụ.");
            return null;
        }
        String ma = String.valueOf(model.getValueAt(table.convertRowIndexToModel(row), 0));
        for (DichVu d : service.layDanhSach())
            if (d.getMaDichVu().equals(ma))
                return d;
        return null;
    }

    private void moDialog(DichVu cu) {
        try {
            dlgDichVu dlg = new dlgDichVu(SwingUtilities.getWindowAncestor(this), cu);
            dlg.setVisible(true);
            if (dlg.isConfirmed()) {
                DichVu d = dlg.getDichVu();
                boolean ok = cu == null ? service.them(d) : service.capNhat(d);
                if (ok) {
                    JOptionPane.showMessageDialog(this, "Lưu dịch vụ thành công.");
                    tai();
                } else
                    JOptionPane.showMessageDialog(this, "Không có dữ liệu nào được thay đổi.");
            }
        } catch (Exception ex) {
            loi("Không thể lưu dịch vụ", ex);
        }
    }

    private void xoa() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(
                    this,
                    "Đã xử lý dịch vụ. Nếu dịch vụ đã phát sinh hóa đơn, "
                            + "trạng thái sẽ được chuyển thành Ngừng hoạt động.");
            ;
            return;
        }
        String ma = String.valueOf(model.getValueAt(table.convertRowIndexToModel(row), 0));
        if (JOptionPane.showConfirmDialog(this, "Bạn có chắc muốn xóa dịch vụ " + ma + "?", "Xác nhận",
                JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            try {
                if (service.xoa(ma)) {
                    JOptionPane.showMessageDialog(this, "Đã xóa dịch vụ.");
                    tai();
                } else
                    JOptionPane.showMessageDialog(this, "Không tìm thấy dịch vụ.");
            } catch (Exception ex) {
                loi("Không thể xóa dịch vụ. Có thể dữ liệu đang được tham chiếu.", ex);
            }
        }
    }

    private void loi(String msg, Exception ex) {
        JOptionPane.showMessageDialog(this, msg + ":\n" + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
    }
}
