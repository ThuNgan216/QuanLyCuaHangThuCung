package gui.dialogs;

import entity.DichVu;
import entity.LichHen;
import service.DichVuService;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;

public class dlgDichVu extends JDialog {
    private final JTextField txtMa = new JTextField(18);
    private final JTextField txtTen = new JTextField(18);
    private final JTextField txtGia = new JTextField(18);
    private final JTextField txtMoTa = new JTextField(18);
    private final JComboBox<String> cboTrangThai = new JComboBox<>(new String[]{"Đang hoạt động", "Ngừng hoạt động"});
    private final DichVuService service = new DichVuService();
    private final LichHen lichHenCu;
    private boolean confirmed;

    public dlgDichVu(Window owner, DichVu dichVu) {
        super(owner, dichVu == null ? "Thêm dịch vụ" : "Cập nhật dịch vụ", ModalityType.APPLICATION_MODAL);
        this.lichHenCu = dichVu == null ? null : dichVu.getMaLichHen();
        setLayout(new BorderLayout(10, 10));
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(15, 18, 8, 18));
        GridBagConstraints c = new GridBagConstraints(); c.insets = new Insets(5, 5, 5, 5); c.fill = GridBagConstraints.HORIZONTAL;
        addRow(form, c, 0, "Mã dịch vụ:", txtMa); addRow(form, c, 1, "Tên dịch vụ:", txtTen);
        addRow(form, c, 2, "Đơn giá:", txtGia); addRow(form, c, 3, "Mô tả:", txtMoTa);
        addRow(form, c, 4, "Trạng thái:", cboTrangThai);
        add(form, BorderLayout.CENTER);
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnLuu = new JButton("Lưu"); JButton btnHuy = new JButton("Hủy"); buttons.add(btnLuu); buttons.add(btnHuy); add(buttons, BorderLayout.SOUTH);
        txtMa.setEditable(false);
        if (dichVu != null) {
            txtMa.setText(dichVu.getMaDichVu()); txtMa.setEditable(false); txtTen.setText(dichVu.getTenDichVu());
            txtGia.setText(dichVu.getDonGia() == null ? "" : dichVu.getDonGia().toPlainString()); txtMoTa.setText(dichVu.getMoTa());
            cboTrangThai.setSelectedItem(dichVu.getTrangThai());
        } else {
            try {
                txtMa.setText(service.taoMaMoi());
                txtMa.setToolTipText("Mã dịch vụ được tự sinh từ database.");
            } catch (RuntimeException ex) {
                txtMa.setText("");
                txtMa.setToolTipText(ex.getMessage());
                btnLuu.setEnabled(false);
                JOptionPane.showMessageDialog(owner, ex.getMessage(), "Không thể tạo mã dịch vụ", JOptionPane.ERROR_MESSAGE);
            }
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
            if (txtTen.getText().trim().isEmpty()) throw new IllegalArgumentException("Vui lòng nhập tên dịch vụ.");
            new BigDecimal(txtGia.getText().trim());
        } catch (NumberFormatException ex) { JOptionPane.showMessageDialog(this, "Đơn giá phải là số hợp lệ.", "Dữ liệu chưa đúng", JOptionPane.WARNING_MESSAGE); return false; }
        catch (IllegalArgumentException ex) { JOptionPane.showMessageDialog(this, ex.getMessage(), "Dữ liệu chưa đúng", JOptionPane.WARNING_MESSAGE); return false; }
        return true;
    }

    public boolean isConfirmed() { return confirmed; }
    public DichVu getDichVu() {
        return new DichVu(txtMa.getText().trim(), txtTen.getText().trim(), new BigDecimal(txtGia.getText().trim()),
                txtMoTa.getText().trim(), (String) cboTrangThai.getSelectedItem(), lichHenCu);
    }
}
