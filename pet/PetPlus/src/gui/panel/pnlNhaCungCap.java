package gui.panel;

import entity.NhaCungCap;
import gui.dialogs.dlgNhaCungCap;
import java.awt.*;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import service.NhaCungCapService;

public class pnlNhaCungCap extends JPanel {

    private final NhaCungCapService service = new NhaCungCapService();

    private JTextField txtTimKiem;
<<<<<<< HEAD
    private JTextField txtMaNCC;
    private JTextField txtTenNCC;
    private JTextField txtSoDienThoai;
    private JTextField txtDiaChi;
    private JTextField txtEmail;
=======
>>>>>>> 95e50a45891aa5ed4bac26dd4eec8ba51e1cb9d5

    private JButton btnTimKiem;
    private JButton btnLamMoi;
    private JButton btnThem;
    private JButton btnSua;
    private JButton btnXoa;

    private DefaultTableModel model;
    private JTable table;

    public pnlNhaCungCap() {
        setLayout(new BorderLayout(12, 12));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));

        khoiTaoGiaoDien();
        ganSuKien();
        taiDanhSach();

        utils.PetPlusTheme.apply(this);
    }

    private void khoiTaoGiaoDien() {
        // Tiêu đề
        JLabel title = new JLabel("QUẢN LÝ NHÀ CUNG CẤP");
        title.setFont(new Font("Arial", Font.BOLD, 22));
        title.setForeground(new Color(224, 25, 91));
        add(title, BorderLayout.NORTH);

<<<<<<< HEAD
        // Thanh tìm kiếm
=======
        // --- TẠO PANEL CHỨA THANH TÌM KIẾM (TRÁI) VÀ NÚT THAO TÁC (PHẢI) ---
        JPanel pnlTop = new JPanel(new BorderLayout());
        pnlTop.setOpaque(false);

        // 1. Cụm thanh tìm kiếm (Đẩy sang trái)
>>>>>>> 95e50a45891aa5ed4bac26dd4eec8ba51e1cb9d5
        JPanel pnlTimKiem = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        pnlTimKiem.setOpaque(false);

        txtTimKiem = new JTextField(20);
        btnTimKiem = new JButton("Tìm kiếm");
        btnLamMoi = new JButton("Làm mới");

        pnlTimKiem.add(new JLabel("Tìm theo mã, tên hoặc SĐT:"));
        pnlTimKiem.add(txtTimKiem);
        pnlTimKiem.add(btnTimKiem);
        pnlTimKiem.add(btnLamMoi);
<<<<<<< HEAD

        // Bảng danh sách
        model = new DefaultTableModel(new Object[]{"Mã NCC", "Tên nhà cung cấp", "Số điện thoại", "Địa chỉ", "Email"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        table = new JTable(model);
        table.setRowHeight(28);
        table.setFont(new Font("Arial", Font.PLAIN, 13));
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 13));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JPanel centerPanel = new JPanel(new BorderLayout(8, 8));
        centerPanel.setOpaque(false);
        centerPanel.add(pnlTimKiem, BorderLayout.NORTH);
        centerPanel.add(new JScrollPane(table), BorderLayout.CENTER);

        add(centerPanel, BorderLayout.CENTER);

        // Form thông tin & Nút thao tác ở phía dưới
        JPanel pnlForm = new JPanel(new GridBagLayout());
        pnlForm.setOpaque(false);
        pnlForm.setBorder(BorderFactory.createTitledBorder("Thông tin nhà cung cấp"));

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 8, 6, 8);
        c.fill = GridBagConstraints.HORIZONTAL;

        txtMaNCC = new JTextField(15);
        txtTenNCC = new JTextField(15);
        txtSoDienThoai = new JTextField(15);
        txtDiaChi = new JTextField(15);
        txtEmail = new JTextField(15);

        txtMaNCC.setEditable(false);
txtTenNCC.setEditable(false);
txtSoDienThoai.setEditable(false);
txtEmail.setEditable(false);
txtDiaChi.setEditable(false);

        // Hàng 1
        c.gridx = 0; c.gridy = 0; pnlForm.add(new JLabel("Mã NCC (*):"), c);
        c.gridx = 1; pnlForm.add(txtMaNCC, c);
        c.gridx = 2; pnlForm.add(new JLabel("Tên NCC (*):"), c);
        c.gridx = 3; pnlForm.add(txtTenNCC, c);

        // Hàng 2
        c.gridx = 0; c.gridy = 1; pnlForm.add(new JLabel("Số điện thoại:"), c);
        c.gridx = 1; pnlForm.add(txtSoDienThoai, c);
        c.gridx = 2; pnlForm.add(new JLabel("Email:"), c);
        c.gridx = 3; pnlForm.add(txtEmail, c);

        // Hàng 3
        c.gridx = 0; c.gridy = 2; pnlForm.add(new JLabel("Địa chỉ:"), c);
        c.gridx = 1; c.gridwidth = 3; pnlForm.add(txtDiaChi, c);
        c.gridwidth = 1;

        // Cụm nút thao tác
        JPanel pnlNut = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 10));
=======
        
        pnlTop.add(pnlTimKiem, BorderLayout.WEST);

        // 2. Cụm nút thao tác (Đẩy sang phải)
        JPanel pnlNut = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
>>>>>>> 95e50a45891aa5ed4bac26dd4eec8ba51e1cb9d5
        pnlNut.setOpaque(false);

        btnThem = new JButton("Thêm mới");
        btnSua = new JButton("Cập nhật");
        btnXoa = new JButton("Xóa");

        pnlNut.add(btnThem);
        pnlNut.add(btnSua);
        pnlNut.add(btnXoa);
<<<<<<< HEAD

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setOpaque(false);
        bottomPanel.add(pnlForm, BorderLayout.CENTER);
        bottomPanel.add(pnlNut, BorderLayout.SOUTH);

        add(bottomPanel, BorderLayout.SOUTH);
=======
        
        pnlTop.add(pnlNut, BorderLayout.EAST);

        // --- Bảng danh sách ---
        model = new DefaultTableModel(new Object[]{"Mã NCC", "Tên nhà cung cấp", "Số điện thoại", "Địa chỉ", "Email"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(model);
        table.setRowHeight(28);
        table.setFont(new Font("Arial", Font.PLAIN, 13));
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 13));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Khu vực trung tâm chứa pnlTop và bảng
        JPanel centerPanel = new JPanel(new BorderLayout(8, 8));
        centerPanel.setOpaque(false);
        centerPanel.add(pnlTop, BorderLayout.NORTH);
        centerPanel.add(new JScrollPane(table), BorderLayout.CENTER);

        add(centerPanel, BorderLayout.CENTER);
>>>>>>> 95e50a45891aa5ed4bac26dd4eec8ba51e1cb9d5
    }

    private void ganSuKien() {
        btnTimKiem.addActionListener(e -> timKiem());
        txtTimKiem.addActionListener(e -> timKiem());
        btnLamMoi.addActionListener(e -> {
            txtTimKiem.setText("");
<<<<<<< HEAD
            xoaTrangForm();
            taiDanhSach();
        });

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                hienThiLenForm();
            }
        });

        btnThem.addActionListener(e -> {
    dlgNhaCungCap dlg = new dlgNhaCungCap(SwingUtilities.getWindowAncestor(this), null);
    dlg.setVisible(true);
    if (dlg.isDaLuu()) {
        taiDanhSach(); // Tải lại bảng sau khi thêm thành công
    }
});
        btnSua.addActionListener(e -> {
    int selectedRow = table.getSelectedRow();
    if (selectedRow < 0) {
        JOptionPane.showMessageDialog(this, "Vui lòng chọn nhà cung cấp cần chỉnh sửa trên bảng.", "Thông báo", JOptionPane.WARNING_MESSAGE);
        return;
    }
    
    // Lấy mã NCC từ cột 0 của dòng đang chọn
    String maNCC = model.getValueAt(selectedRow, 0).toString();
    
    // Tìm đối tượng NhaCungCap từ service hoặc danh sách đang hiển thị
    NhaCungCap nccDangChon = service.layDanhSach().stream()
            .filter(x -> x.getMaNCC().equals(maNCC))
            .findFirst().orElse(null);

    if (nccDangChon != null) {
        dlgNhaCungCap dlg = new dlgNhaCungCap(SwingUtilities.getWindowAncestor(this), nccDangChon);
        dlg.setVisible(true);
        if (dlg.isDaLuu()) {
            taiDanhSach(); // Tải lại bảng sau khi cập nhật thành công
        }
    }
});
=======
            taiDanhSach();
        });

        // Nút Thêm mới
        btnThem.addActionListener(e -> {
            dlgNhaCungCap dlg = new dlgNhaCungCap(SwingUtilities.getWindowAncestor(this), null);
            dlg.setVisible(true);
            if (dlg.isDaLuu()) {
                taiDanhSach(); 
            }
        });

        // Nút Cập nhật
        btnSua.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow < 0) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn nhà cung cấp cần chỉnh sửa trên bảng.", "Thông báo", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String maNCC = model.getValueAt(selectedRow, 0).toString();

            NhaCungCap nccDangChon = service.layDanhSach().stream()
                    .filter(x -> x.getMaNCC().equals(maNCC))
                    .findFirst().orElse(null);

            if (nccDangChon != null) {
                dlgNhaCungCap dlg = new dlgNhaCungCap(SwingUtilities.getWindowAncestor(this), nccDangChon);
                dlg.setVisible(true);
                if (dlg.isDaLuu()) {
                    taiDanhSach(); 
                }
            }
        });

        // Nút Xóa
>>>>>>> 95e50a45891aa5ed4bac26dd4eec8ba51e1cb9d5
        btnXoa.addActionListener(e -> xoaNCC());
    }

    private void taiDanhSach() {
        try {
            hienThiTable(service.layDanhSach());
        } catch (Exception ex) {
            baoLoi("Không thể tải danh sách nhà cung cấp.", ex);
        }
    }

    private void timKiem() {
        try {
            hienThiTable(service.timKiem(txtTimKiem.getText()));
        } catch (Exception ex) {
            baoLoi("Tìm kiếm nhà cung cấp thất bại.", ex);
        }
    }

    private void hienThiTable(List<NhaCungCap> ds) {
        model.setRowCount(0);
        for (NhaCungCap ncc : ds) {
            model.addRow(new Object[]{
                    ncc.getMaNCC(),
                    ncc.getTenNCC(),
                    ncc.getSoDienThoai() == null ? "" : ncc.getSoDienThoai(),
                    ncc.getDiaChi() == null ? "" : ncc.getDiaChi(),
                    ncc.getEmail() == null ? "" : ncc.getEmail()
            });
        }
    }

<<<<<<< HEAD
    private void hienThiLenForm() {
        int r = table.getSelectedRow();
        if (r >= 0) {
            txtMaNCC.setText(model.getValueAt(r, 0).toString());
            txtMaNCC.setEditable(false);
            txtTenNCC.setText(model.getValueAt(r, 1).toString());
            txtSoDienThoai.setText(model.getValueAt(r, 2).toString());
            txtDiaChi.setText(model.getValueAt(r, 3).toString());
            txtEmail.setText(model.getValueAt(r, 4).toString());
        }
    }

    private void xoaTrangForm() {
        txtMaNCC.setText("");
        txtTenNCC.setText("");
        txtSoDienThoai.setText("");
        txtDiaChi.setText("");
        txtEmail.setText("");
        table.clearSelection();
    }

    private void themNCC() {
        try {
            NhaCungCap ncc = new NhaCungCap(
                    txtMaNCC.getText(),
                    txtTenNCC.getText(),
                    txtSoDienThoai.getText(),
                    txtDiaChi.getText(),
                    txtEmail.getText()
            );
            if (service.them(ncc)) {
                JOptionPane.showMessageDialog(this, "Thêm nhà cung cấp thành công!");
                xoaTrangForm();
                taiDanhSach();
            }
        } catch (Exception ex) {
            baoLoi("Thêm nhà cung cấp thất bại.", ex);
        }
    }

    private void capNhatNCC() {
        try {
            NhaCungCap ncc = new NhaCungCap(
                    txtMaNCC.getText(),
                    txtTenNCC.getText(),
                    txtSoDienThoai.getText(),
                    txtDiaChi.getText(),
                    txtEmail.getText()
            );
            if (service.capNhat(ncc)) {
                JOptionPane.showMessageDialog(this, "Cập nhật nhà cung cấp thành công!");
                taiDanhSach();
            }
        } catch (Exception ex) {
            baoLoi("Cập nhật thất bại.", ex);
        }
    }

    private void xoaNCC() {
        String ma = txtMaNCC.getText().trim();
        if (ma.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn nhà cung cấp cần xóa.", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

=======
    private void xoaNCC() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn nhà cung cấp cần xóa trên bảng.", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String ma = model.getValueAt(selectedRow, 0).toString();
>>>>>>> 95e50a45891aa5ed4bac26dd4eec8ba51e1cb9d5
        int confirm = JOptionPane.showConfirmDialog(this, "Bạn có chắc chắn muốn xóa nhà cung cấp " + ma + "?", "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                if (service.xoa(ma)) {
                    JOptionPane.showMessageDialog(this, "Xóa nhà cung cấp thành công!");
<<<<<<< HEAD
                    xoaTrangForm();
=======
>>>>>>> 95e50a45891aa5ed4bac26dd4eec8ba51e1cb9d5
                    taiDanhSach();
                }
            } catch (Exception ex) {
                baoLoi("Xóa nhà cung cấp thất bại.", ex);
            }
        }
    }

    private void baoLoi(String noiDung, Exception ex) {
        JOptionPane.showMessageDialog(this, noiDung + "\n" + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
    }
}