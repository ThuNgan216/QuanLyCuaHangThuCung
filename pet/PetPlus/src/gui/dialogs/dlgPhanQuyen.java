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
    private final JComboBox<String> cbo = new JComboBox<>(VaiTroHelper.TEN_VAI_TRO);

    public dlgPhanQuyen(Window owner, NhanVien nv) {
        super(owner, "Phân quyền nhân viên", ModalityType.APPLICATION_MODAL);
        setLayout(new BorderLayout());
        add(PetPlusDialogs.header("PHÂN QUYỀN", "Thay đổi quyền truy cập hệ thống PetPlus"), BorderLayout.NORTH);
        JPanel main = new JPanel(new GridLayout(4, 1, 7, 8));
        main.setBorder(new EmptyBorder(18, 25, 22, 25));
        main.setBackground(Color.WHITE);
        main.add(PetPlusUI.label("Nhân viên: " + nv.getTenNV() + " (" + nv.getMaNV() + ")"));
        main.add(PetPlusUI.label("Tên đăng nhập: " + nv.getMaTK().getTenDangNhap()));
        main.add(PetPlusUI.label("Vai trò được cấp:"));
        PetPlusUI.decorateInput(cbo);

        cbo.setFont(new Font("Arial", Font.PLAIN, 15));
        cbo.setSelectedItem(
                VaiTroHelper.tenVaiTro(nv.getMaTK().getVaiTro()));

      
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
            accepted = true;
            dispose();
        });
        footer.add(cancel);
        footer.add(save);
        add(footer, BorderLayout.SOUTH);
        setSize(570, 360);
        setMinimumSize(new Dimension(520, 330));
        setLocationRelativeTo(owner);
    }

    public boolean open() {
        setVisible(true);
        return accepted;
    }

    public String getVaiTro() {
        return VaiTroHelper.maVaiTro((String) cbo.getSelectedItem());
    }
}
