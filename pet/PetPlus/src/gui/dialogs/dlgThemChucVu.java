
package gui.dialogs;

import gui.dialogs.dlgNhanVien.PetPlusUI;
import gui.dialogs.dlgNhanVien.PetPlusDialogs;
import gui.dialogs.dlgNhanVien.VaiTroHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.Locale;

public class dlgThemChucVu extends JDialog {

    private final JTextField txtMa = PetPlusUI.textField();
    private final JTextField txtTen = PetPlusUI.textField();

    private boolean daThem = false;

    public dlgThemChucVu(Window owner) {

        super(owner, "Thêm chức vụ / vai trò",
                ModalityType.APPLICATION_MODAL);

        setLayout(new BorderLayout());
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        getContentPane().setBackground(PetPlusUI.WHITE);
    
        add(PetPlusDialogs.header(
                "THÊM CHỨC VỤ / VAI TRÒ",
                ""
        ), BorderLayout.NORTH);

        
        JPanel form = new JPanel(new GridLayout(4, 1, 6, 8));
        form.setBackground(PetPlusUI.WHITE);

        form.setBorder(new EmptyBorder(25, 30, 25, 30));

        form.add(PetPlusUI.label("Mã vai trò *"));
        form.add(txtMa);

        form.add(PetPlusUI.label("Tên chức vụ *"));
        form.add(txtTen);

        txtMa.setToolTipText("Ví dụ: KY_THUAT");
        txtTen.setToolTipText("Ví dụ: Kỹ thuật viên");

        add(form, BorderLayout.CENTER);

        
        JPanel footer = new JPanel(
                new FlowLayout(FlowLayout.RIGHT, 12, 15)
        );

        footer.setBackground(PetPlusUI.BACKGROUND);

        JButton btnHuy = PetPlusUI.button("Hủy", false);
        JButton btnLuu = PetPlusUI.button("Lưu chức vụ", true);

        btnHuy.addActionListener(e -> dispose());

        btnLuu.addActionListener(e -> luuChucVu());

        footer.add(btnHuy);
        footer.add(btnLuu);

        add(footer, BorderLayout.SOUTH);

        getRootPane().setDefaultButton(btnLuu);

        setSize(520, 350);
        setMinimumSize(new Dimension(480, 320));
        setLocationRelativeTo(owner);
    }

    private void luuChucVu() {

        String ma = txtMa.getText()
                .trim()
                .toUpperCase(Locale.ROOT);

        String ten = txtTen.getText().trim();

        if (!ma.matches("[A-Z][A-Z0-9_]{2,49}")
                || "ADMIN".equals(ma)) {

            PetPlusDialogs.error(
                    this,
                    "Mã vai trò không hợp lệ",
                    "Mã cần 3–50 ký tự, bắt đầu bằng chữ cái, "
                    + "chỉ chứa A-Z, 0-9, dấu _ và không được là ADMIN."
            );
            return;
        }

        if (ten.isEmpty() || ten.length() > 100) {

            PetPlusDialogs.error(
                    this,
                    "Tên chức vụ",
                    "Tên chức vụ phải có từ 1 đến 100 ký tự."
            );
            return;
        }

        boolean ok = VaiTroHelper.themChucVu(ma, ten);

        if (!ok) {
            PetPlusDialogs.error(
                    this,
                    "Không thể thêm",
                    "Mã vai trò hoặc tên chức vụ đã tồn tại."
            );
            return;
        }

        daThem = true;
        dispose();
    }

    public boolean open() {
        setVisible(true);
        return daThem;
    }
}
