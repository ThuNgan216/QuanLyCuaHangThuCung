package gui.dialogs;

import entity.SanPham;
import java.awt.*;
import java.util.List;
import javax.swing.*;
import service.SanPhamService;
import utils.PetPlusTheme;

public class dlgSanPham extends JDialog {
    private final JTextField txtMaSP = new JTextField(15);
    private final JTextField txtTenSP = new JTextField(15);
    private final JTextField txtGiaBan = new JTextField(15);
    // Đã xóa txtSoLuongTon vì không dùng đến trên UI này

    private final SanPhamService service = new SanPhamService();
    private final SanPham sanPhamHienTai;
    private boolean daLuu = false;

    public dlgSanPham(Window owner, SanPham sp) {
        super(owner, sp == null ? "Thêm Sản Phẩm Mới" : "Cập Nhật Sản Phẩm", ModalityType.APPLICATION_MODAL);
        this.sanPhamHienTai = sp;
        khoiTaoGiaoDien();
    }

    private void khoiTaoGiaoDien() {
        setLayout(new BorderLayout(10, 10));

        JPanel pnlForm = new JPanel(new GridBagLayout());
        pnlForm.setBorder(BorderFactory.createEmptyBorder(15, 18, 10, 18));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6, 6, 6, 6);
        g.fill = GridBagConstraints.HORIZONTAL;
        g.weightx = 1;

        int y = 0;
        themDong(pnlForm, g, y++, "Mã sản phẩm (*):", txtMaSP);
        txtMaSP.setEditable(false);

        themDong(pnlForm, g, y++, "Tên sản phẩm (*):", txtTenSP);
        themDong(pnlForm, g, y++, "Giá bán (VNĐ) (*):", txtGiaBan);
        // Đã xóa dòng add Số lượng tồn ra khỏi giao diện form

        JButton btnLuu = new JButton("Lưu");
        JButton btnHuy = new JButton("Hủy");
        JPanel pnlButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        pnlButtons.add(btnLuu);
        pnlButtons.add(btnHuy);

        add(pnlForm, BorderLayout.CENTER);
        add(pnlButtons, BorderLayout.SOUTH);

        btnLuu.addActionListener(e -> xuLyLuu());
        btnHuy.addActionListener(e -> dispose());

        if (sanPhamHienTai != null) {
            // Chế độ Cập nhật: Chỉ sửa Tên và Giá bán
            txtMaSP.setText(sanPhamHienTai.getMaSP());
            txtTenSP.setText(sanPhamHienTai.getTenSP());
            txtGiaBan.setText(String.valueOf(sanPhamHienTai.getGiaBan()));
            txtGiaBan.setEditable(true);
        } else {
            // Chế độ Thêm mới: Tự sinh mã, giá bán khóa mặc định bằng 0
            tuDongSinhMaSP();
            txtGiaBan.setText("0");
            txtGiaBan.setEditable(false);
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

    private void tuDongSinhMaSP() {
        try {
            List<SanPham> ds = new dao.SanPhamDAO().layDanhSachSanPham();
            int maxId = 0;
            for (SanPham sp : ds) {
                String ma = sp.getMaSP();
                if (ma != null && ma.startsWith("SP")) {
                    try {
                        int num = Integer.parseInt(ma.substring(2));
                        if (num > maxId) maxId = num;
                    } catch (Exception ignored) {}
                }
            }
            txtMaSP.setText(String.format("SP%03d", maxId + 1));
        } catch (Exception ex) {
            txtMaSP.setText("SP001");
        }
    }

    private void xuLyLuu() {
        try {
            String ma = txtMaSP.getText().trim();
            String ten = txtTenSP.getText().trim();
            if (ten.isEmpty()) {
                throw new IllegalArgumentException("Tên sản phẩm không được để trống.");
            }
            float gia = txtGiaBan.getText().trim().isEmpty() ? 0 : Float.parseFloat(txtGiaBan.getText().trim());

            // Khi lưu giữ nguyên hoặc mặc định số lượng tồn cũ, không cho form ghi đè bừa bãi
            int tonCu = 0;
            if (sanPhamHienTai != null) {
                tonCu = sanPhamHienTai.getSoLuongTon();
            }

            SanPham sp = new SanPham(ma, ten, gia, tonCu);

            if (sanPhamHienTai == null) {
                service.them(sp);
                JOptionPane.showMessageDialog(this, "Thêm sản phẩm mới thành công (Trạng thái: Chờ nhập)!");
            } else {
                service.capNhat(sp);
                JOptionPane.showMessageDialog(this, "Cập nhật sản phẩm thành công!");
            }
            daLuu = true;
            dispose();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Giá bán phải là định dạng số hợp lệ.", "Lỗi định dạng", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi nghiệp vụ", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isDaLuu() {
        return daLuu;
    }
}