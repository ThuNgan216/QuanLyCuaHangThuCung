package gui.dialogs;

import entity.DichVu;
import entity.LichHen;
import service.DichVuService;
import utils.ThongBao;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;

/**
 * Thêm / sửa dịch vụ. Dialog tự gọi service để lưu: nếu vi phạm nghiệp vụ thì báo lỗi và giữ dialog mở,
 * người dùng không mất dữ liệu đã nhập.
 * Dịch vụ danh mục chọn "Đang hoạt động / Ngừng hoạt động"; dịch vụ gắn lịch hẹn chọn "Đã đặt / Hoàn thành".
 */
public class dlgDichVu extends JDialog {
    private final JTextField txtMa = new JTextField(18);
    private final JTextField txtTen = new JTextField(18);
    private final JTextField txtGia = new JTextField(18);
    private final JTextField txtMoTa = new JTextField(18);
    private final JComboBox<String> cboTrangThai = new JComboBox<>();
    private final DichVuService service = new DichVuService();
    private final DichVu dichVuCu;
    private final LichHen lichHenCu;
    private boolean saved;

    public dlgDichVu(Window owner, DichVu dichVu) {
        super(owner, dichVu == null ? "Thêm dịch vụ" : "Cập nhật dịch vụ", ModalityType.APPLICATION_MODAL);
        this.dichVuCu = dichVu;
        this.lichHenCu = dichVu == null ? null : dichVu.getMaLichHen();
        boolean danhMuc = dichVu == null || DichVuService.laDanhMuc(dichVu);
        for (String tt : DichVuService.trangThaiHopLe(danhMuc))
            cboTrangThai.addItem(tt);

        setLayout(new BorderLayout(10, 10));
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(15, 18, 8, 18));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5, 5, 5, 5);
        c.fill = GridBagConstraints.HORIZONTAL;
        int dong = 0;
        addRow(form, c, dong++, "Mã dịch vụ:", txtMa);
        if (!danhMuc) {
            JTextField txtLichHen = new JTextField(lichHenCu.getMaLichHen(), 18);
            txtLichHen.setEditable(false);
            addRow(form, c, dong++, "Gắn lịch hẹn:", txtLichHen);
        }
        addRow(form, c, dong++, "Tên dịch vụ:", txtTen);
        addRow(form, c, dong++, "Đơn giá (đ):", txtGia);
        addRow(form, c, dong++, "Mô tả:", txtMoTa);
        addRow(form, c, dong++, "Trạng thái:", cboTrangThai);
        add(form, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnLuu = new JButton("Lưu");
        JButton btnHuy = new JButton("Hủy");
        buttons.add(btnLuu);
        buttons.add(btnHuy);
        add(buttons, BorderLayout.SOUTH);

        txtMa.setEditable(false);
        if (dichVu != null) {
            txtMa.setText(dichVu.getMaDichVu());
            txtTen.setText(dichVu.getTenDichVu());
            txtGia.setText(dichVu.getDonGia() == null ? "" : dichVu.getDonGia().toPlainString());
            txtMoTa.setText(dichVu.getMoTa());
            cboTrangThai.setSelectedItem(dichVu.getTrangThai());
        } else {
            try {
                txtMa.setText(service.taoMaMoi());
                txtMa.setToolTipText("Mã gợi ý; hệ thống cấp mã chính thức khi lưu.");
            } catch (RuntimeException ex) {
                txtMa.setText("");
                txtMa.setToolTipText(ex.getMessage());
                btnLuu.setEnabled(false);
                ThongBao.loi(owner, "Không thể tạo mã dịch vụ", ex);
            }
        }
        btnLuu.addActionListener(e -> luu());
        btnHuy.addActionListener(e -> dispose());
        getRootPane().setDefaultButton(btnLuu);
        utils.PetPlusTheme.apply(getContentPane());
        pack();
        setResizable(false);
        setLocationRelativeTo(owner);
    }

    private void addRow(JPanel p, GridBagConstraints c, int row, String label, Component input) {
        c.gridx = 0; c.gridy = row; c.weightx = 0; p.add(new JLabel(label), c);
        c.gridx = 1; c.weightx = 1; p.add(input, c);
    }

    private void luu() {
        try {
            BigDecimal gia;
            try {
                gia = new BigDecimal(txtGia.getText().trim().replace(",", ""));
            } catch (NumberFormatException ex) {
                throw new IllegalArgumentException("Đơn giá phải là số hợp lệ, ví dụ 120000.");
            }
            DichVu d = new DichVu(txtMa.getText().trim(), txtTen.getText().trim(), gia, txtMoTa.getText().trim(),
                    (String) cboTrangThai.getSelectedItem(), lichHenCu);
            boolean ok = dichVuCu == null ? service.them(d) : service.capNhat(d);
            if (!ok) {
                ThongBao.khongThayDoi(this);
                return;
            }
            saved = true;
            if (dichVuCu == null)
                ThongBao.daThem(this, "dịch vụ", d.getMaDichVu());
            else
                ThongBao.daCapNhat(this, "dịch vụ");
            dispose();
        } catch (RuntimeException ex) {
            ThongBao.xuLy(this, "Không thể lưu dịch vụ", ex);
        }
    }

    public boolean isSaved() { return saved; }
}
