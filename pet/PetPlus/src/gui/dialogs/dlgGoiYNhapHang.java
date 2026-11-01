package gui.dialogs;

import dao.SanPhamDAO;
import entity.SanPham;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import utils.PetPlusTheme;

public class dlgGoiYNhapHang extends JDialog {
    private JTable table;
    private DefaultTableModel model;
    private JRadioButton radROP, radCoBan;
    private JTextField txtSoNgayDuTru, txtNguongTon;
    private List<Object[]> duLieuGoc = new ArrayList<>(); // Lưu data từ SQL

    public dlgGoiYNhapHang(Window owner) {
        super(owner, "Thuật Toán Đề Xuất Nhập Hàng Tự Động", ModalityType.APPLICATION_MODAL);
        setSize(1000, 650);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(10, 10));

        khoiTaoGiaoDien();
        taiDuLieuTuDB();
        tinhToanVaLocData(); // Lần đầu hiển thị
        
        PetPlusTheme.apply(getContentPane());
    }

    private void khoiTaoGiaoDien() {
        // --- 1. KHU VỰC LỰA CHỌN TIÊU CHÍ LỌC ---
        JPanel pnlTop = new JPanel(new GridBagLayout());
        pnlTop.setBorder(BorderFactory.createTitledBorder("Tiêu chí gợi ý nhập hàng"));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(5, 10, 5, 10);
        g.anchor = GridBagConstraints.WEST;
        
        radROP = new JRadioButton("Theo tốc độ bán", true);
        radCoBan = new JRadioButton("Theo định mức tồn kho");
        ButtonGroup bg = new ButtonGroup();
        bg.add(radROP); bg.add(radCoBan);
        
        txtSoNgayDuTru = new JTextField("15", 5);
        txtNguongTon = new JTextField("10", 5);
        txtNguongTon.setEnabled(false); 

        radROP.addActionListener(e -> { txtSoNgayDuTru.setEnabled(true); txtNguongTon.setEnabled(false); });
        radCoBan.addActionListener(e -> { txtSoNgayDuTru.setEnabled(false); txtNguongTon.setEnabled(true); });

        JButton btnTinhToan = new JButton("Áp dụng");
        btnTinhToan.setBackground(new Color(255, 193, 7));
        btnTinhToan.addActionListener(e -> tinhToanVaLocData());

        g.gridx = 0; g.gridy = 0; pnlTop.add(radROP, g);
        g.gridx = 1; pnlTop.add(new JLabel("Số ngày dự trữ:"), g); 
        g.gridx = 2; pnlTop.add(txtSoNgayDuTru, g);
        
        g.gridx = 0; g.gridy = 1; pnlTop.add(radCoBan, g);
        g.gridx = 1; pnlTop.add(new JLabel("Tồn kho tối thiểu:"), g); 
        g.gridx = 2; pnlTop.add(txtNguongTon, g);
        
        g.gridx = 3; g.gridy = 0; g.gridheight = 2; 
        g.fill = GridBagConstraints.VERTICAL; pnlTop.add(btnTinhToan, g);
        
        add(pnlTop, BorderLayout.NORTH);

        // --- 2. BẢNG HIỂN THỊ ĐỀ XUẤT ---
        String[] cols = {"Chọn", "Mã SP", "Tên Sản Phẩm", "Tồn Hiện Tại", "Tốc độ bán", "ĐỀ XUẤT NHẬP", "Nhà Cung Cấp", "Giá Nhập Lần Cuối", "maNCC"};
        model = new DefaultTableModel(cols, 0) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return columnIndex == 0 ? Boolean.class : String.class; // Cột 0 là Checkbox
            }
            @Override
            public boolean isCellEditable(int row, int col) {
                return col == 0 || col == 5; // Chỉ cho tích chọn và sửa số lượng đề xuất
            }
        };
        table = new JTable(model);
        table.setRowHeight(28);
        table.getColumnModel().getColumn(0).setMaxWidth(50); // Cột checkbox
        table.getColumnModel().getColumn(8).setMinWidth(0); // Ẩn cột maNCC
        table.getColumnModel().getColumn(8).setMaxWidth(0);
        table.getColumnModel().getColumn(8).setWidth(0);
        add(new JScrollPane(table), BorderLayout.CENTER);

        // --- 3. NÚT CHUYỂN SANG PHIẾU NHẬP ---
        JPanel pnlBottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnTaoPhieu = new JButton("Tạo Phiếu Nhập Cho Các Mục Đã Chọn");
        btnTaoPhieu.setBackground(new Color(250, 48, 115));
        btnTaoPhieu.setForeground(Color.WHITE);
        btnTaoPhieu.addActionListener(e -> xuLyTaoPhieuNhap());
        pnlBottom.add(btnTaoPhieu);
        add(pnlBottom, BorderLayout.SOUTH);
    }

    private void taiDuLieuTuDB() {
        try {
            duLieuGoc = new SanPhamDAO().layDuLieuGoiYNhapHang();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi tải data: " + ex.getMessage());
        }
    }

    // Thuật toán ROP & Lọc cơ bản
    private void tinhToanVaLocData() {
        model.setRowCount(0);
        try {
            int leadTime = 3; // Thời gian hàng về (ngầm định 3 ngày)
            int soNgayDuTru = Integer.parseInt(txtSoNgayDuTru.getText().trim());
            int nguongTon = Integer.parseInt(txtNguongTon.getText().trim());
            boolean isROP = radROP.isSelected();

            for (Object[] row : duLieuGoc) {
                String maSP = (String) row[0];
                String tenSP = (String) row[1];
                int tonKho = (int) row[2];
                int tongBan30Ngay = (int) row[3];
                String maNCC = (String) row[4];
                String tenNCC = (String) row[5];
                float giaNhap = (float) row[6];

                boolean canNhap = false;
                int soLuongDeXuat = 0;
                String tocDoBanHienThi = "-";

                if (isROP) {
                    double velocity = tongBan30Ngay / 30.0;
                    if (velocity > 0) {
                        tocDoBanHienThi = String.format("%.1f cái/ngày", velocity);
                        double daysLeft = tonKho / velocity;
                        if (daysLeft <= leadTime) { // Hàng sẽ hết trước khi nhập về kịp
                            canNhap = true;
                            soLuongDeXuat = (int) Math.ceil((velocity * soNgayDuTru) - tonKho);
                            if (soLuongDeXuat < 1) soLuongDeXuat = 1; // Bèo nhất gọi 1 cái
                        }
                    }
                } else {
                    if (tonKho <= nguongTon) {
                        canNhap = true;
                        soLuongDeXuat = nguongTon - tonKho;
                        if (soLuongDeXuat < 1) soLuongDeXuat = 1;
                    }
                }

                if (canNhap) {
                    model.addRow(new Object[]{
                        false, // Checkbox mặc định chưa tick
                        maSP, tenSP, 
                        String.valueOf(tonKho), 
                        tocDoBanHienThi, 
                        String.valueOf(soLuongDeXuat), 
                        tenNCC, 
                        giaNhap > 0 ? String.format("%,.0f đ", giaNhap) : "0",
                        maNCC
                    });
                }
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập số hợp lệ vào các ô thông số!");
        }
    }

    private void xuLyTaoPhieuNhap() {
        List<Object[]> hangChon = new ArrayList<>();
        String chungMaNCC = null;

        for (int i = 0; i < model.getRowCount(); i++) {
            boolean isChecked = (Boolean) model.getValueAt(i, 0);
            if (isChecked) {
                String maNCC = (String) model.getValueAt(i, 8);
                if (maNCC == null || maNCC.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Có sản phẩm tự do chưa có NCC. Vui lòng bỏ tick hoặc nhập tay sau.", "Lỗi", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                if (chungMaNCC == null) {
                    chungMaNCC = maNCC; // Lấy NCC của món hàng đầu tiên làm mốc
                } else if (!chungMaNCC.equals(maNCC)) {
                    JOptionPane.showMessageDialog(this, "LỖI: 1 Phiếu nhập chỉ dành cho 1 Nhà cung cấp. Vui lòng chỉ tick chọn các mặt hàng có CÙNG Nhà Cung Cấp!", "Sai nghiệp vụ", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                String maSP = (String) model.getValueAt(i, 1);
                String tenSP = (String) model.getValueAt(i, 2);
                int sl = Integer.parseInt(model.getValueAt(i, 5).toString());
                float gia = Float.parseFloat(model.getValueAt(i, 7).toString().replaceAll("[^0-9]", ""));
                
                hangChon.add(new Object[]{maSP, tenSP, sl, gia});
            }
        }

        if (hangChon.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng tích chọn ít nhất 1 sản phẩm!");
            return;
        }

        // Đẩy sang form Phiếu Nhập
        dlgPhieuNhap dlg = new dlgPhieuNhap(SwingUtilities.getWindowAncestor(this));
        dlg.importHangTuGoiY(chungMaNCC, hangChon); // Gọi hàm điền tự động
        dlg.setVisible(true);
        if (dlg.isDaLuu()) {
            dispose(); // Nhập xong thì tắt popup này luôn
        }
    }
}