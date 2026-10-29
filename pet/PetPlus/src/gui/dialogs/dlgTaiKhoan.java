package gui.dialogs;

import entity.NhanVien;
import entity.TaiKhoan;
import gui.dialogs.dlgNhanVien.PetPlusUI;
import gui.dialogs.dlgNhanVien.PetPlusDialogs;
import gui.dialogs.dlgNhanVien.VaiTroHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.Arrays;

public class dlgTaiKhoan extends JDialog {
    private boolean accepted;
    private String matKhauMoi;
    private final String maVaiTroCu;
    private final JTextField txtTenDN = PetPlusUI.textField();
    private final JPasswordField txtMatKhau = new JPasswordField();
    private final JPasswordField txtNhapLai = new JPasswordField();
    private final JComboBox<String> cboVaiTro = new JComboBox<>(VaiTroHelper.TEN_VAI_TRO);
    private final JComboBox<String> cboTrangThai =
            new JComboBox<>(new String[]{"Hoạt động", "Ngừng hoạt động"});

    public dlgTaiKhoan(Window owner, NhanVien nv) {
        super(owner, "Cập nhật tài khoản", ModalityType.APPLICATION_MODAL);
        TaiKhoan tk = nv.getMaTK();
        if (tk == null) throw new IllegalArgumentException("Nhân viên chưa có tài khoản");
        maVaiTroCu = tk.getVaiTro();
        setLayout(new BorderLayout());
        add(PetPlusDialogs.header("CẬP NHẬT TÀI KHOẢN",
                "Nhân viên: " + nv.getTenNV() + "  |  Mã TK: " + tk.getMaTK()), BorderLayout.NORTH);

        PetPlusUI.decorateInput(txtMatKhau);
        PetPlusUI.decorateInput(txtNhapLai);
        PetPlusUI.decorateInput(cboVaiTro);
        cboVaiTro.setPrototypeDisplayValue("Chăm sóc thú cưng");
        PetPlusUI.decorateInput(cboTrangThai);
        JPanel body = new JPanel(new GridBagLayout());
        body.setBackground(Color.WHITE);
        body.setBorder(new EmptyBorder(22, 25, 12, 25));
        addRow(body, 0, "Tên đăng nhập", txtTenDN);
        addRow(body, 1, "Vai trò / Chức vụ", cboVaiTro);
        addRow(body, 2, "Trạng thái", cboTrangThai);
        addRow(body, 3, "Mật khẩu mới", txtMatKhau);
        addRow(body, 4, "Nhập lại mật khẩu", txtNhapLai);
        JLabel note = new JLabel("Để trống 2 ô mật khẩu nếu không muốn đổi.");
        note.setFont(PetPlusUI.font(Font.ITALIC, 12));
        note.setForeground(PetPlusUI.MUTED);
        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = 1; gc.gridy = 5; gc.anchor = GridBagConstraints.WEST;
        body.add(note, gc);
        add(body, BorderLayout.CENTER);

        txtTenDN.setText(tk.getTenDangNhap());
        cboVaiTro.setSelectedItem(VaiTroHelper.tenVaiTro(tk.getVaiTro()));
        cboTrangThai.setSelectedItem(tk.getTrangThai());
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 14));
        actions.setBackground(PetPlusUI.BACKGROUND);
        JButton cancel = PetPlusUI.button("Hủy", false);
        JButton save = PetPlusUI.button("Lưu thay đổi", true);
        cancel.addActionListener(e -> dispose());
        save.addActionListener(e -> {
            String user = txtTenDN.getText().trim();
            if (user.isEmpty() || user.matches(".*\\s+.*")) {
                PetPlusDialogs.error(this, "Tên đăng nhập", "Tên đăng nhập không được trống hoặc chứa khoảng trắng.");
                return;
            }
            if (user.equalsIgnoreCase("admin")) {
                PetPlusDialogs.error(this, "Tên đăng nhập", "Không được đổi tên đăng nhập thành admin.");
                return;
            }
            char[] p1 = txtMatKhau.getPassword();
            char[] p2 = txtNhapLai.getPassword();
            if (p1.length > 0 && p1.length < 8) {
                Arrays.fill(p1, '\0'); Arrays.fill(p2, '\0');
                PetPlusDialogs.error(this, "Mật khẩu", "Mật khẩu mới phải có ít nhất 8 ký tự.");
                return;
            }
            if (!Arrays.equals(p1, p2)) {
                Arrays.fill(p1, '\0'); Arrays.fill(p2, '\0');
                PetPlusDialogs.error(this, "Mật khẩu", "Hai mật khẩu chưa khớp.");
                return;
            }
            if (cboVaiTro.getSelectedIndex() < 0) {
                Arrays.fill(p1, '\0'); Arrays.fill(p2, '\0');
                PetPlusDialogs.error(this, "Vai trò", "Hãy chọn một trong 4 vai trò.");
                return;
            }
            matKhauMoi = p1.length == 0 ? null : new String(p1);
            Arrays.fill(p1, '\0'); Arrays.fill(p2, '\0');
            accepted = true;
            dispose();
        });
        actions.add(cancel); actions.add(save);
        add(actions, BorderLayout.SOUTH);
        getRootPane().setDefaultButton(save);
        setSize(640, 475);
        setMinimumSize(new Dimension(570, 445));
        setLocationRelativeTo(owner);
    }

    private void addRow(JPanel body, int y, String label, JComponent field) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0; c.gridy = y; c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(0, 0, 16, 16);
        body.add(PetPlusUI.label(label), c);
        c = new GridBagConstraints();
        c.gridx = 1; c.gridy = y; c.fill = GridBagConstraints.HORIZONTAL; c.weightx = 1;
        c.insets = new Insets(0, 0, 16, 0);
        body.add(field, c);
    }

    public boolean open() { setVisible(true); return accepted; }
    public String getTenDangNhap() { return txtTenDN.getText().trim(); }
    public String getMatKhauMoi() { return matKhauMoi; }
    public String getVaiTro() {
        return VaiTroHelper.giuMaNeuKhongDoi(maVaiTroCu, (String) cboVaiTro.getSelectedItem());
    }
    public String getTrangThai() { return (String) cboTrangThai.getSelectedItem(); }
}
