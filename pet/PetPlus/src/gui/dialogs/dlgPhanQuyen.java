package gui.dialogs;

import entity.NhanVien;
import gui.dialogs.dlgNhanVien.PetPlusUI;
import gui.dialogs.dlgNhanVien.VaiTroHelper;
import gui.dialogs.dlgNhanVien.PetPlusDialogs;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class dlgPhanQuyen extends JDialog {
    private boolean accepted;
    private final String maVaiTroCu;
    private final JComboBox<String> cbo = new JComboBox<>(VaiTroHelper.TEN_VAI_TRO);

    public dlgPhanQuyen(Window owner, NhanVien nv) {
        super(owner, "Phân quyền nhân viên", ModalityType.APPLICATION_MODAL);
        if (nv.getMaTK() == null) throw new IllegalArgumentException("Nhân viên chưa có tài khoản");
        maVaiTroCu = nv.getMaTK().getVaiTro();
        setLayout(new BorderLayout());
        add(PetPlusDialogs.header("PHÂN QUYỀN",
                "Thay đổi vai trò đồng thời cập nhật chức vụ nhân viên"), BorderLayout.NORTH);
        JPanel main = new JPanel(new GridLayout(4, 1, 7, 8));
        main.setBorder(new EmptyBorder(18, 25, 22, 25));
        main.setBackground(Color.WHITE);
        main.add(PetPlusUI.label("Nhân viên: " + nv.getTenNV() + " (" + nv.getMaNV() + ")"));
        main.add(PetPlusUI.label("Tên đăng nhập: " + nv.getMaTK().getTenDangNhap()));
        main.add(PetPlusUI.label("Vai trò / Chức vụ được cấp:"));
        PetPlusUI.decorateInput(cbo);
        cbo.setFont(new Font("Arial", Font.PLAIN, 15));
        cbo.setSelectedItem(VaiTroHelper.tenVaiTro(maVaiTroCu));
        JPanel pnlCombo = new JPanel(new BorderLayout());
        pnlCombo.setBackground(Color.WHITE);
        cbo.setPreferredSize(new Dimension(400, 42));
        cbo.setMinimumSize(new Dimension(200, 42));
        pnlCombo.add(cbo, BorderLayout.CENTER);
        main.add(pnlCombo);
        add(main, BorderLayout.CENTER);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 13));
        footer.setBackground(PetPlusUI.BACKGROUND);
        JButton cancel = PetPlusUI.button("Hủy", false);
        JButton save = PetPlusUI.button("Lưu quyền", true);
        cancel.addActionListener(e -> dispose());
        save.addActionListener(e -> {
            if (cbo.getSelectedIndex() < 0) {
                PetPlusDialogs.error(this, "Vai trò", "Hãy chọn 1 trong 4 vai trò.");
                return;
            }
            accepted = true;
            dispose();
        });
        footer.add(cancel);
        footer.add(save);
        add(footer, BorderLayout.SOUTH);
        getRootPane().setDefaultButton(save);
        setSize(570, 360);
        setMinimumSize(new Dimension(520, 330));
        setLocationRelativeTo(owner);
    }

    public boolean open() { setVisible(true); return accepted; }
    public String getVaiTro() {
        return VaiTroHelper.giuMaNeuKhongDoi(maVaiTroCu, (String) cbo.getSelectedItem());
    }
}
