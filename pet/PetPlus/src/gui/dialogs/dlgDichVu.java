
package gui.dialogs;

import entity.DichVu;
import service.DichVuService;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;

public class dlgDichVu extends JDialog {

    private final JTextField txtMa = new JTextField(20);
    private final JTextField txtTen = new JTextField(20);
    private final JTextField txtGia = new JTextField(20);
    private final JTextArea txtMoTa = new JTextArea(4, 20);

    private final JComboBox<String> cboTrangThai =
            new JComboBox<>(new String[] {
                    DichVuService.DANG_HOAT_DONG,
                    DichVuService.NGUNG_HOAT_DONG
            });

    private final DichVuService service = new DichVuService();

    private boolean confirmed = false;
    private final DichVu dichVuCu;

    public dlgDichVu(Window owner, DichVu dichVu) {
        super(
                owner,
                dichVu == null ? "Thêm dịch vụ" : "Cập nhật dịch vụ",
                ModalityType.APPLICATION_MODAL
        );

        this.dichVuCu = dichVu;

        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(new Color(255, 247, 250));

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        form.setBorder(BorderFactory.createEmptyBorder(18, 20, 12, 20));

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(7, 6, 7, 6);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.NORTHWEST;

        txtMa.setEditable(false);
        txtMa.setBackground(new Color(245, 245, 245));

        txtMoTa.setLineWrap(true);
        txtMoTa.setWrapStyleWord(true);

        JScrollPane scrollMoTa = new JScrollPane(txtMoTa);

        themDong(form, c, 0, "Mã dịch vụ:", txtMa);
        themDong(form, c, 1, "Tên dịch vụ *:", txtTen);
        themDong(form, c, 2, "Đơn giá (VNĐ) *:", txtGia);
        themDong(form, c, 3, "Mô tả:", scrollMoTa);
        themDong(form, c, 4, "Trạng thái *:", cboTrangThai);

        add(form, BorderLayout.CENTER);

        JButton btnLuu = taoNut("Lưu", new Color(245, 70, 113));
        JButton btnHuy = taoNut("Hủy", new Color(100, 116, 139));

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        buttons.setBackground(Color.WHITE);
        buttons.add(btnLuu);
        buttons.add(btnHuy);

        add(buttons, BorderLayout.SOUTH);

        napDuLieu();

        btnLuu.addActionListener(e -> kiemTraVaLuu());
        btnHuy.addActionListener(e -> dispose());

        getRootPane().setDefaultButton(btnLuu);

        utils.PetPlusTheme.apply(getContentPane());

        pack();
        setMinimumSize(new Dimension(480, getHeight()));
        setResizable(false);
        setLocationRelativeTo(owner);
    }

    private void napDuLieu() {
        if (dichVuCu == null) {
            try {
                txtMa.setText(service.taoMaMoi());
            } catch (RuntimeException ex) {
                JOptionPane.showMessageDialog(
                        this,
                        "Không thể tạo mã dịch vụ: " + ex.getMessage(),
                        "Lỗi",
                        JOptionPane.ERROR_MESSAGE
                );
                txtMa.setText("");
            }

            cboTrangThai.setSelectedItem(
                    DichVuService.DANG_HOAT_DONG
            );
        } else {
            txtMa.setText(dichVuCu.getMaDichVu());
            txtTen.setText(dichVuCu.getTenDichVu());

            txtGia.setText(
                    dichVuCu.getDonGia() == null
                            ? ""
                            : dichVuCu.getDonGia().stripTrailingZeros().toPlainString()
            );

            txtMoTa.setText(
                    dichVuCu.getMoTa() == null
                            ? ""
                            : dichVuCu.getMoTa()
            );

            cboTrangThai.setSelectedItem(dichVuCu.getTrangThai());
        }
    }

    private void themDong(
            JPanel panel,
            GridBagConstraints c,
            int row,
            String ten,
            Component input
    ) {
        c.gridx = 0;
        c.gridy = row;
        c.weightx = 0;

        JLabel label = new JLabel(ten);
        label.setFont(new Font("Arial", Font.BOLD, 13));
        panel.add(label, c);

        c.gridx = 1;
        c.weightx = 1;

        panel.add(input, c);
    }

    private JButton taoNut(String text, Color mau) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 12));
        button.setForeground(Color.WHITE);
        button.setBackground(mau);
        button.setFocusPainted(false);
        button.setOpaque(true);
        button.setBorder(BorderFactory.createEmptyBorder(9, 18, 9, 18));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return button;
    }

    private void kiemTraVaLuu() {
        String ten = txtTen.getText().trim();
        String giaText = txtGia.getText().trim();

        if (ten.isEmpty()) {
            baoLoi("Vui lòng nhập tên dịch vụ.");
            txtTen.requestFocus();
            return;
        }

        if (!ten.matches("[\\p{L} ]+")) {
            baoLoi(
                    "Tên dịch vụ chỉ được chứa chữ cái và khoảng trắng. "
                    + "Không nhập số hoặc ký tự đặc biệt."
            );
            txtTen.requestFocus();
            txtTen.selectAll();
            return;
        }

        if (giaText.isEmpty()) {
            baoLoi("Vui lòng nhập đơn giá.");
            txtGia.requestFocus();
            return;
        }

        BigDecimal gia;

        try {
            gia = new BigDecimal(giaText.replace(",", "."));

            if (gia.compareTo(BigDecimal.ZERO) <= 0) {
                baoLoi("Đơn giá phải lớn hơn 0.");
                txtGia.requestFocus();
                txtGia.selectAll();
                return;
            }
        } catch (NumberFormatException ex) {
            baoLoi("Đơn giá không hợp lệ. Vui lòng chỉ nhập số, ví dụ 120000.");
            txtGia.requestFocus();
            txtGia.selectAll();
            return;
        }

        if (cboTrangThai.getSelectedItem() == null) {
            baoLoi("Vui lòng chọn trạng thái.");
            return;
        }

        try {
            String maBoQua = dichVuCu == null
                    ? null
                    : dichVuCu.getMaDichVu();

            if (service.kiemTraTrungTen(ten, maBoQua)) {
                baoLoi(
                        "Tên dịch vụ đã tồn tại. "
                        + "Vui lòng nhập tên dịch vụ khác."
                );
                txtTen.requestFocus();
                txtTen.selectAll();
                return;
            }

            confirmed = true;
            dispose();

        } catch (Exception ex) {
            baoLoi("Không thể kiểm tra tên dịch vụ: " + ex.getMessage());
        }
    }

    private void baoLoi(String noiDung) {
        JOptionPane.showMessageDialog(
                this,
                noiDung,
                "Dữ liệu chưa hợp lệ",
                JOptionPane.WARNING_MESSAGE
        );
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public DichVu getDichVu() {
        if (!confirmed) {
            throw new IllegalStateException(
                    "Chưa xác nhận thông tin dịch vụ."
            );
        }

        BigDecimal gia = new BigDecimal(
                txtGia.getText().trim().replace(",", ".")
        );

        return new DichVu(
                txtMa.getText().trim(),
                txtTen.getText().trim().replaceAll("\\s+", " "),
                gia,
                txtMoTa.getText().trim().isEmpty()
                        ? null
                        : txtMoTa.getText().trim(),
                (String) cboTrangThai.getSelectedItem()
        );
    }
}
