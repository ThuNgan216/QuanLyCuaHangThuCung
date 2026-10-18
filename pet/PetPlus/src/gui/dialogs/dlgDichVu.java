package gui.dialogs;

import entity.DichVu;
import entity.LichHen;
import service.DichVuService;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.util.List;

public class dlgDichVu extends JDialog {
    private final JTextField txtMa = new JTextField(18);
    private final JTextField txtTen = new JTextField(18);
    private final JTextField txtGia = new JTextField(18);
    private final JTextField txtMoTa = new JTextField(18);
    private final JComboBox<String> cboTrangThai = new JComboBox<>(new String[]{"Đang hoạt động", "Ngừng hoạt động"});
    private final JComboBox<String> cboLichHen = new JComboBox<>();
    private final DichVuService service = new DichVuService();
    private boolean confirmed;

    public dlgDichVu(Window owner, DichVu dichVu) {
        super(owner, dichVu == null ? "Thêm dịch vụ" : "Cập nhật dịch vụ", ModalityType.APPLICATION_MODAL);
        setLayout(new BorderLayout(10, 10));
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(15, 18, 8, 18));
        GridBagConstraints c = new GridBagConstraints(); c.insets = new Insets(5, 5, 5, 5); c.fill = GridBagConstraints.HORIZONTAL;
        addRow(form, c, 0, "Mã dịch vụ:", txtMa); addRow(form, c, 1, "Tên dịch vụ:", txtTen);
        addRow(form, c, 2, "Đơn giá:", txtGia); addRow(form, c, 3, "Mô tả:", txtMoTa);
        addRow(form, c, 4, "Trạng thái:", cboTrangThai); addRow(form, c, 5, "Mã lịch hẹn:", cboLichHen);
        add(form, BorderLayout.CENTER);
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnLuu = new JButton("Lưu"); JButton btnHuy = new JButton("Hủy"); buttons.add(btnLuu); buttons.add(btnHuy); add(buttons, BorderLayout.SOUTH);
        try { List<String> maLichHen = service.layDanhSachMaLichHen(); for (String ma : maLichHen) cboLichHen.addItem(ma); }
        catch (Exception ex) { JOptionPane.showMessageDialog(owner, ex.getMessage(), "Lỗi tải lịch hẹn", JOptionPane.ERROR_MESSAGE); }
        if (dichVu != null) {
            txtMa.setText(dichVu.getMaDichVu()); txtMa.setEditable(false); txtTen.setText(dichVu.getTenDichVu());
            txtGia.setText(dichVu.getDonGia() == null ? "" : dichVu.getDonGia().toPlainString()); txtMoTa.setText(dichVu.getMoTa());
            cboTrangThai.setSelectedItem(dichVu.getTrangThai());
            if (dichVu.getMaLichHen() != null) cboLichHen.setSelectedItem(dichVu.getMaLichHen().getMaLichHen());
        }
        btnLuu.addActionListener(e -> { if (kiemTraVaLuu()) { confirmed = true; dispose(); } });
        btnHuy.addActionListener(e -> dispose());
        utils.PetPlusTheme.apply(getContentPane());
        pack(); setResizable(false); setLocationRelativeTo(owner);
    }

    private void addRow(JPanel p, GridBagConstraints c, int row, String label, Component input) {
        c.gridx = 0; c.gridy = row; c.weightx = 0; p.add(new JLabel(label), c);
        c.gridx = 1; c.weightx = 1; p.add(input, c);
    }

    private boolean kiemTraVaLuu() {
        try {
            if (cboLichHen.getSelectedItem() == null) throw new IllegalArgumentException("Cần có ít nhất một lịch hẹn trước khi thêm dịch vụ.");
            new BigDecimal(txtGia.getText().trim());
        } catch (NumberFormatException ex) { JOptionPane.showMessageDialog(this, "Đơn giá phải là số hợp lệ.", "Dữ liệu chưa đúng", JOptionPane.WARNING_MESSAGE); return false; }
        catch (IllegalArgumentException ex) { JOptionPane.showMessageDialog(this, ex.getMessage(), "Dữ liệu chưa đúng", JOptionPane.WARNING_MESSAGE); return false; }
        return true;
    }

    public boolean isConfirmed() { return confirmed; }
    public DichVu getDichVu() {
        return new DichVu(txtMa.getText().trim(), txtTen.getText().trim(), new BigDecimal(txtGia.getText().trim()),
                txtMoTa.getText().trim(), (String) cboTrangThai.getSelectedItem(), new LichHen((String) cboLichHen.getSelectedItem()));
    }
}
