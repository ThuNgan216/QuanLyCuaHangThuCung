package gui.dialogs;

import entity.SanPham;
import java.awt.*;
import javax.swing.*;
import service.SanPhamService;
import utils.PetPlusTheme;

public class dlgSanPham extends JDialog {
    private final JTextField txtMaSP = new JTextField(15);
    private final JTextField txtTenSP = new JTextField(15);
    private final JTextField txtGiaBan = new JTextField(15);
    private final JTextField txtSoLuongTon = new JTextField(15);
    private final JComboBox<entity.NhaCungCap> cboNhaCungCap = new JComboBox<>();
private final JTextField txtGiaNhap = new JTextField(15);

    private final SanPhamService service = new SanPhamService();
    private final SanPham sanPhamHienTai;
    private boolean daLuu = false;

    public dlgSanPham(Window owner, SanPham sp) {
        super(owner, sp == null ? "Thêm Sản Phẩm Mới" : "Cập Nhật Sản Phẩm", ModalityType.APPLICATION_MODAL);
        this.sanPhamHienTai = sp;
        khoiTaoGiaoDien();
        taiDuLieuBanDau();
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
        themDong(pnlForm, g, y++, "Mã sản phẩm (*):", txtMaSP); // Thêm lại dòng này
        txtMaSP.setEditable(false);
        themDong(pnlForm, g, y++, "Tên sản phẩm (*):", txtTenSP);
themDong(pnlForm, g, y++, "Nhà cung cấp:", cboNhaCungCap);
themDong(pnlForm, g, y++, "Giá nhập (VNĐ):", txtGiaNhap);
themDong(pnlForm, g, y++, "Giá bán (VNĐ) (*):", txtGiaBan);
themDong(pnlForm, g, y++, "Số lượng tồn:", txtSoLuongTon);

cboNhaCungCap.setRenderer(new DefaultListCellRenderer() {
    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean hasFocus) {
        super.getListCellRendererComponent(list, value, index, isSelected, hasFocus);
        if (value instanceof entity.NhaCungCap) {
            entity.NhaCungCap ncc = (entity.NhaCungCap) value;
            setText(ncc.getMaNCC() + " - " + ncc.getTenNCC());
        }
        return this;
    }
});

cboNhaCungCap.addActionListener(e -> {
    if (sanPhamHienTai != null) {
        entity.NhaCungCap ncc = (entity.NhaCungCap) cboNhaCungCap.getSelectedItem();
        if (ncc != null) {
            try {
                float gia = new dao.SanPhamDAO().layGiaNhap(sanPhamHienTai.getMaSP(), ncc.getMaNCC());
                txtGiaNhap.setText(gia > 0 ? String.format("%.0f", gia) : "");
            } catch (Exception ex) {}
        }
    }
});

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

        if (sanPhamHienTai != null) {
            txtMaSP.setText(sanPhamHienTai.getMaSP());
            txtMaSP.setEditable(false); // Không cho sửa mã khi cập nhật
            txtTenSP.setText(sanPhamHienTai.getTenSP());
            txtGiaBan.setText(String.valueOf(sanPhamHienTai.getGiaBan()));
            txtSoLuongTon.setText(String.valueOf(sanPhamHienTai.getSoLuongTon()));
            txtSoLuongTon.setEditable(false); // Số lượng tồn thay đổi qua phiếu nhập
        } else {
            tuDongSinhMaSP(); // Tự động sinh mã nếu là Thêm Mới
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

    private void taiDuLieuBanDau() {
        try {
            // Xóa đoạn code cũ của cboLoai đi và thay bằng code tải danh sách NCC
            java.util.List<entity.NhaCungCap> ds = new dao.NhaCungCapDAO().layDanhSach();
            for (entity.NhaCungCap ncc : ds) {
                cboNhaCungCap.addItem(ncc);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi tải nhà cung cấp: " + ex.getMessage());
        }
    }

    private void xuLyLuu() {
        try {
            String ma = txtMaSP.getText().trim();
            String ten = txtTenSP.getText().trim();
            if (ma.isEmpty() || ten.isEmpty()) {
                throw new IllegalArgumentException("Mã và tên sản phẩm không được để trống.");
            }
            float gia = Float.parseFloat(txtGiaBan.getText().trim());
            int ton = txtSoLuongTon.getText().trim().isEmpty() ? 0 : Integer.parseInt(txtSoLuongTon.getText().trim());

            SanPham sp = new SanPham(ma, ten, gia, ton);

            if (sanPhamHienTai == null) {
                service.them(sp);
                JOptionPane.showMessageDialog(this, "Thêm sản phẩm thành công!");
            } else {
                service.capNhat(sp);
                JOptionPane.showMessageDialog(this, "Cập nhật sản phẩm thành công!");
            }

            float giaNhap = Float.parseFloat(txtGiaNhap.getText().trim());
entity.NhaCungCap ncc = (entity.NhaCungCap) cboNhaCungCap.getSelectedItem();
new dao.SanPhamDAO().luuGiaNhap(sp.getMaSP(), ncc.getMaNCC(), giaNhap);

            daLuu = true;
            dispose();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Giá bán và số lượng tồn phải là định dạng số hợp lệ.", "Lỗi định dạng", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi nghiệp vụ", JOptionPane.ERROR_MESSAGE);
        }
    }
    

    public boolean isDaLuu() {
        return daLuu;
    }

    private void tuDongSinhMaSP() {
        try {
            java.util.List<entity.SanPham> ds = new dao.SanPhamDAO().layDanhSachSanPham();
            int maxId = 0;
            for (entity.SanPham sp : ds) {
                String ma = sp.getMaSP();
                if (ma != null && ma.startsWith("SP")) {
                    try {
                        int num = Integer.parseInt(ma.substring(2));
                        if (num > maxId) maxId = num;
                    } catch (Exception e) {}
                }
            }
            txtMaSP.setText(String.format("SP%03d", maxId + 1));
        } catch (Exception ex) {
            txtMaSP.setText("SP001");
        }
    }
    
}