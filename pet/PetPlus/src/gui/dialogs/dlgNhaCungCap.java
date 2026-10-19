package gui.dialogs;

import entity.NhaCungCap;
import service.NhaCungCapService;
import utils.PetPlusTheme;

import javax.swing.*;
import java.awt.*;

public class dlgNhaCungCap extends JDialog {
    private final JTextField txtMaNCC = new JTextField(15);
    private final JTextField txtTenNCC = new JTextField(15);
    private final JTextField txtSoDienThoai = new JTextField(15);
    private final JTextField txtEmail = new JTextField(15);
    private final JTextField txtDiaChi = new JTextField(15);

    private final NhaCungCapService service = new NhaCungCapService();
    private final NhaCungCap nccHienTai;
    private boolean daLuu = false;

    public dlgNhaCungCap(Window owner, NhaCungCap ncc) {
        super(owner, ncc == null ? "Thêm Nhà Cung Cấp Mới" : "Cập Nhật Thông Tin Nhà Cung Cấp", ModalityType.APPLICATION_MODAL);
        this.nccHienTai = ncc;
        khoiTaoGiaoDien();
    }

    private void khoiTaoGiaoDien() {
        setLayout(new BorderLayout(10, 10));

        JPanel pnlForm = new JPanel(new GridBagLayout());
        pnlForm.setBorder(BorderFactory.createEmptyBorder(18, 20, 12, 20));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(8, 6, 8, 6);
        g.fill = GridBagConstraints.HORIZONTAL;
        g.weightx = 1;

        int y = 0;
        themDong(pnlForm, g, y++, "Mã Nhà Cung Cấp (*):", txtMaNCC);
        themDong(pnlForm, g, y++, "Tên Nhà Cung Cấp (*):", txtTenNCC);
        themDong(pnlForm, g, y++, "Số Điện Thoại:", txtSoDienThoai);
        themDong(pnlForm, g, y++, "Email:", txtEmail);
        themDong(pnlForm, g, y++, "Địa Chỉ:", txtDiaChi);

        // Nút thao tác
        JButton btnLuu = new JButton("Lưu");
        JButton btnHuy = new JButton("Hủy");
        JPanel pnlButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        pnlButtons.add(btnLuu);
        pnlButtons.add(btnHuy);

        add(pnlForm, BorderLayout.CENTER);
        add(pnlButtons, BorderLayout.SOUTH);

        // Sự kiện nút bấm
        btnLuu.addActionListener(e -> xuLyLuu());
        btnHuy.addActionListener(e -> dispose());

        // Nếu là chế độ cập nhật, đổ dữ liệu lên form và khóa mã NCC
        if (nccHienTai != null) {
            txtMaNCC.setText(nccHienTai.getMaNCC());
            txtMaNCC.setEditable(false); // Khóa cứng mã NCC không cho sửa khi cập nhật
            txtTenNCC.setText(nccHienTai.getTenNCC());
            txtSoDienThoai.setText(nccHienTai.getSoDienThoai());
            txtEmail.setText(nccHienTai.getEmail());
            txtDiaChi.setText(nccHienTai.getDiaChi());
        }

        PetPlusTheme.apply(getContentPane());
        pack();
        setLocationRelativeTo(getOwner());
    }

    private void themDong(JPanel p, GridBagConstraints g, int y, String label, JComponent c) {
        g.gridx = 0; g.gridy = y; g.weightx = 0;
        p.add(new JLabel(label), g);
        g.gridx = 1; g.weightx = 1;
        p.add(c, g);
    }

    private void xuLyLuu() {
        try {
            String ma = txtMaNCC.getText().trim();
            String ten = txtTenNCC.getText().trim();
            String sdt = txtSoDienThoai.getText().trim();
            String email = txtEmail.getText().trim();
            String diaChi = txtDiaChi.getText().trim();

            if (ma.isEmpty() || ten.isEmpty()) {
                throw new IllegalArgumentException("Mã và tên nhà cung cấp không được để trống.");
            }

            NhaCungCap ncc = new NhaCungCap(ma, ten, sdt, diaChi, email);

            if (nccHienTai == null) {
                service.them(ncc);
                JOptionPane.showMessageDialog(this, "Thêm nhà cung cấp thành công!");
            } else {
                service.capNhat(ncc);
                JOptionPane.showMessageDialog(this, "Cập nhật thông tin nhà cung cấp thành công!");
            }
            daLuu = true;
            dispose();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi nghiệp vụ", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isDaLuu() {
        return daLuu;
    }
}