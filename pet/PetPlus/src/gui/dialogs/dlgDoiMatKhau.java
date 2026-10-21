package gui.dialogs;

import dao.TaiKhoanDAO;
import entity.NhanVien;
import gui.dialogs.dlgNhanVien.PetPlusDialogs;
import gui.dialogs.dlgNhanVien.PetPlusUI;
import utils.Auth;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;

public class dlgDoiMatKhau extends JDialog {
    private static final int DO_DAI_TOI_THIEU = 8;

    private final JPasswordField txtMatKhauCu = new JPasswordField();
    private final JPasswordField txtMatKhauMoi = new JPasswordField();
    private final JPasswordField txtNhapLaiMatKhau = new JPasswordField();
    private final JButton btnLuu = PetPlusDialogs.button("Đổi mật khẩu", true);
    private final JButton btnHuy = PetPlusDialogs.button("Hủy", false);
    private final String maTK;

    public dlgDoiMatKhau(Window owner) {
        super(owner, "Đổi mật khẩu tài khoản", ModalityType.APPLICATION_MODAL);

        NhanVien nguoiDung = Auth.user;
        if (nguoiDung == null || nguoiDung.getMaTK() == null) {
            throw new IllegalStateException("Cần đăng nhập trước khi đổi mật khẩu.");
        }
        maTK = nguoiDung.getMaTK().getMaTK();

        setLayout(new BorderLayout());
        add(PetPlusDialogs.header("ĐỔI MẬT KHẨU", "Tài khoản: " + nguoiDung.getMaTK().getTenDangNhap()),
                BorderLayout.NORTH);

        PetPlusDialogs.decorateInput(txtMatKhauCu);
        PetPlusDialogs.decorateInput(txtMatKhauMoi);
        PetPlusDialogs.decorateInput(txtNhapLaiMatKhau);

        JPanel body = new JPanel(new GridBagLayout());
        body.setBackground(Color.WHITE);
        body.setBorder(new EmptyBorder(22, 25, 12, 25));
        addRow(body, 0, "Mật khẩu hiện tại", txtMatKhauCu);
        addRow(body, 1, "Mật khẩu mới", txtMatKhauMoi);
        addRow(body, 2, "Nhập lại mật khẩu mới", txtNhapLaiMatKhau);

        JLabel note = new JLabel("Mật khẩu mới phải có ít nhất " + DO_DAI_TOI_THIEU + " ký tự.");
        note.setFont(PetPlusUI.font(Font.ITALIC, 12));
        note.setForeground(PetPlusUI.MUTED);
        GridBagConstraints noteConstraints = new GridBagConstraints();
        noteConstraints.gridx = 1;
        noteConstraints.gridy = 3;
        noteConstraints.anchor = GridBagConstraints.WEST;
        noteConstraints.insets = new Insets(-4, 0, 0, 0);
        body.add(note, noteConstraints);
        add(body, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 14));
        actions.setBackground(PetPlusDialogs.BG);
        btnHuy.addActionListener(e -> dispose());
        btnLuu.addActionListener(e -> doiMatKhau());
        actions.add(btnHuy);
        actions.add(btnLuu);
        add(actions, BorderLayout.SOUTH);

        getRootPane().setDefaultButton(btnLuu);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);
        setSize(560, 340);
        setMinimumSize(new Dimension(520, 320));
        setLocationRelativeTo(owner);
    }

    private void addRow(JPanel body, int row, String title, JPasswordField field) {
        GridBagConstraints labelConstraints = new GridBagConstraints();
        labelConstraints.gridx = 0;
        labelConstraints.gridy = row;
        labelConstraints.anchor = GridBagConstraints.WEST;
        labelConstraints.insets = new Insets(0, 0, 16, 16);
        body.add(PetPlusDialogs.label(title), labelConstraints);

        GridBagConstraints fieldConstraints = new GridBagConstraints();
        fieldConstraints.gridx = 1;
        fieldConstraints.gridy = row;
        fieldConstraints.fill = GridBagConstraints.HORIZONTAL;
        fieldConstraints.weightx = 1;
        fieldConstraints.insets = new Insets(0, 0, 16, 0);
        body.add(field, fieldConstraints);
    }

    private void doiMatKhau() {
        char[] matKhauCu = txtMatKhauCu.getPassword();
        char[] matKhauMoi = txtMatKhauMoi.getPassword();
        char[] nhapLai = txtNhapLaiMatKhau.getPassword();

        if (matKhauCu.length == 0 || matKhauMoi.length == 0 || nhapLai.length == 0) {
            xoa(matKhauCu, matKhauMoi, nhapLai);
            baoLoi("Vui lòng nhập đầy đủ ba ô mật khẩu.");
            return;
        }
        if (matKhauMoi.length < DO_DAI_TOI_THIEU) {
            xoa(matKhauCu, matKhauMoi, nhapLai);
            baoLoi("Mật khẩu mới phải có ít nhất " + DO_DAI_TOI_THIEU + " ký tự.");
            return;
        }
        if (!Arrays.equals(matKhauMoi, nhapLai)) {
            xoa(matKhauCu, matKhauMoi, nhapLai);
            baoLoi("Mật khẩu mới và phần nhập lại chưa khớp.");
            return;
        }
        if (Arrays.equals(matKhauCu, matKhauMoi)) {
            xoa(matKhauCu, matKhauMoi, nhapLai);
            baoLoi("Mật khẩu mới phải khác mật khẩu hiện tại.");
            return;
        }

        final char[] matKhauCuChoXuLy = matKhauCu.clone();
        final char[] matKhauMoiChoXuLy = matKhauMoi.clone();
        xoa(matKhauCu, matKhauMoi, nhapLai);

        btnLuu.setEnabled(false);
        btnHuy.setEnabled(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                return new TaiKhoanDAO().doiMatKhau(
                        maTK,
                        new String(matKhauCuChoXuLy),
                        new String(matKhauMoiChoXuLy));
            }

            @Override
            protected void done() {
                try {
                    if (get()) {
                        NhanVien nguoiDung = Auth.user;
                        if (nguoiDung != null && nguoiDung.getMaTK() != null
                                && maTK.equals(nguoiDung.getMaTK().getMaTK())) {
                            nguoiDung.getMaTK().setMatKhau(new String(matKhauMoiChoXuLy));
                        }
                        JOptionPane.showMessageDialog(
                                dlgDoiMatKhau.this,
                                "Đổi mật khẩu thành công.",
                                "Thành công",
                                JOptionPane.INFORMATION_MESSAGE);
                        dispose();
                    } else {
                        txtMatKhauCu.setText("");
                        txtMatKhauCu.requestFocusInWindow();
                        baoLoi("Mật khẩu hiện tại không đúng hoặc tài khoản không còn hoạt động.");
                    }
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    baoLoi("Không thể hoàn tất việc đổi mật khẩu. Vui lòng thử lại.");
                } catch (ExecutionException ex) {
                    Throwable cause = ex.getCause();
                    String message = cause == null || cause.getMessage() == null
                            ? "Không thể kết nối cơ sở dữ liệu."
                            : "Không thể đổi mật khẩu: " + cause.getMessage();
                    baoLoi(message);
                } finally {
                    xoa(matKhauCuChoXuLy, matKhauMoiChoXuLy);
                    if (isDisplayable()) {
                        btnLuu.setEnabled(true);
                        btnHuy.setEnabled(true);
                        setCursor(Cursor.getDefaultCursor());
                    }
                }
            }
        }.execute();
    }

    private void baoLoi(String message) {
        JOptionPane.showMessageDialog(
                this, message, "Đổi mật khẩu", JOptionPane.WARNING_MESSAGE);
    }

    private void xoa(char[]... matKhau) {
        for (char[] value : matKhau) {
            if (value != null) {
                Arrays.fill(value, '\0');
            }
        }
    }

    @Override
    public void dispose() {
        txtMatKhauCu.setText("");
        txtMatKhauMoi.setText("");
        txtNhapLaiMatKhau.setText("");
        super.dispose();
    }
}