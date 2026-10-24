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

    private JButton btnTimKiem;
    private JButton btnLamMoi;
    private JButton btnThem;
    private JButton btnSua;
    private JButton btnXoa;
    private JButton btnXemSP;

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

        // --- TẠO PANEL CHỨA THANH TÌM KIẾM (TRÁI) VÀ NÚT THAO TÁC (PHẢI) ---
        JPanel pnlTop = new JPanel(new BorderLayout());
        pnlTop.setOpaque(false);

        // 1. Cụm thanh tìm kiếm (Đẩy sang trái)
        JPanel pnlTimKiem = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        pnlTimKiem.setOpaque(false);

        txtTimKiem = new JTextField(20);
        btnTimKiem = new JButton("Tìm kiếm");
        btnLamMoi = new JButton("Làm mới");

        pnlTimKiem.add(new JLabel("Tìm theo mã, tên hoặc SĐT:"));
        pnlTimKiem.add(txtTimKiem);
        pnlTimKiem.add(btnTimKiem);
        pnlTimKiem.add(btnLamMoi);
        
        pnlTop.add(pnlTimKiem, BorderLayout.WEST);

        // 2. Cụm nút thao tác (Đẩy sang phải)
        JPanel pnlNut = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        pnlNut.setOpaque(false);

        btnThem = new JButton("Thêm mới");
        btnSua = new JButton("Cập nhật");
        btnXoa = new JButton("Xóa");
        btnXemSP = new JButton("Xem SP");

        pnlNut.add(btnThem);
        pnlNut.add(btnSua);
        pnlNut.add(btnXoa);
        pnlNut.add(btnXemSP);
        
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
    }

    private void ganSuKien() {
        btnTimKiem.addActionListener(e -> timKiem());
        txtTimKiem.addActionListener(e -> timKiem());
        btnLamMoi.addActionListener(e -> {
            txtTimKiem.setText("");
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

        // Sự kiện click nút Xem SP
        btnXemSP.addActionListener(e -> xemSanPhamCungCap());

        // Nút Xóa
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

    private void xoaNCC() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn nhà cung cấp cần xóa trên bảng.", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String ma = model.getValueAt(selectedRow, 0).toString();
        int confirm = JOptionPane.showConfirmDialog(this, "Bạn có chắc chắn muốn xóa nhà cung cấp " + ma + "?", "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                if (service.xoa(ma)) {
                    JOptionPane.showMessageDialog(this, "Xóa nhà cung cấp thành công!");
                    taiDanhSach();
                }
            } catch (Exception ex) {
                baoLoi("Xóa nhà cung cấp thất bại.", ex);
            }
        }
    }

    private void xemSanPhamCungCap() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một nhà cung cấp trên bảng để xem sản phẩm.", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String maNCC = model.getValueAt(selectedRow, 0).toString();
        String tenNCC = model.getValueAt(selectedRow, 1).toString();

        try {
            // Lấy data từ hàm DAO vừa tạo ở Bước 1
            List<Object[]> dsSP = new dao.SanPhamDAO().layDanhSachSanPhamVaGiaNhapTheoNCC(maNCC);
            if (dsSP.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Nhà cung cấp này hiện chưa cung cấp sản phẩm nào cho hệ thống.", "Thông tin", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            // Tạo bảng popup
            String[] cols = {"Mã SP", "Tên Sản Phẩm", "Giá Nhập Thực Tế", "Tồn Kho"};
            DefaultTableModel modelSP = new DefaultTableModel(cols, 0) {
                @Override
                public boolean isCellEditable(int row, int column) { return false; }
            };

            for (Object[] row : dsSP) {
                float giaNhap = (float) row[2];
                row[2] = String.format("%,.0f đ", giaNhap); // Format tiền VNĐ
                modelSP.addRow(row);
            }

            JTable tblSP = new JTable(modelSP);
            tblSP.setRowHeight(28);
            tblSP.getTableHeader().setFont(new Font("Arial", Font.BOLD, 13));

            JScrollPane scroll = new JScrollPane(tblSP);
            scroll.setPreferredSize(new Dimension(600, 300)); // Kích thước popup

            JOptionPane.showMessageDialog(this, scroll, "Sản phẩm cung cấp bởi: " + tenNCC, JOptionPane.PLAIN_MESSAGE);

        } catch (Exception ex) {
            baoLoi("Không thể tải danh sách sản phẩm của nhà cung cấp.", ex);
        }
    }

    private void baoLoi(String noiDung, Exception ex) {
        JOptionPane.showMessageDialog(this, noiDung + "\n" + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
    }
}