package gui.panel;

import entity.KhuyenMai;
import gui.dialogs.dlgKhuyenMai;
import service.KhuyenMaiService;
import utils.PetPlusTheme;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class pnlKhuyenMai extends JPanel {
    private DefaultTableModel model;
    private JTable table;
    private final KhuyenMaiService service = new KhuyenMaiService();

    private JButton btnThem, btnSua, btnXoa, btnLamMoi;

    public pnlKhuyenMai() {
        setLayout(new BorderLayout(12, 12));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));

        khoiTaoGiaoDien();
        ganSuKien();
        taiDanhSach();
        PetPlusTheme.apply(this);
    }

    private void khoiTaoGiaoDien() {
        // Tiêu đề và nút chức năng phía trên
        JPanel pnlTop = new JPanel(new BorderLayout());
        pnlTop.setOpaque(false);

        JLabel lblTitle = new JLabel("QUẢN LÝ KHUYẾN MÃI & ƯU ĐÃI");
        lblTitle.setFont(new Font("Arial", Font.BOLD, 22));
        lblTitle.setForeground(new Color(224, 25, 91));

        JPanel pnlButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        pnlButtons.setOpaque(false);

        btnThem = new JButton("+ Thêm Khuyến Mãi");
        btnSua = new JButton("Chỉnh Sửa");
        btnXoa = new JButton("Xóa");
        btnLamMoi = new JButton("Làm Mới");

        pnlButtons.add(btnThem);
        pnlButtons.add(btnSua);
        pnlButtons.add(btnXoa);
        pnlButtons.add(btnLamMoi);

        pnlTop.add(lblTitle, BorderLayout.WEST);
        pnlTop.add(pnlButtons, BorderLayout.EAST);
        add(pnlTop, BorderLayout.NORTH);

        // Bảng hiển thị danh sách khuyến mãi
        String[] columns = {"Mã KM", "Tên Chương Trình", "Phần Trăm Giảm (%)", "Điều Kiện Áp Dụng", "Ngày Bắt Đầu", "Ngày Kết Thúc"};
        model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        table = new JTable(model);
        table.setRowHeight(28);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        add(new JScrollPane(table), BorderLayout.CENTER);
    }

    private void ganSuKien() {
        btnThem.addActionListener(e -> {
            dlgKhuyenMai dlg = new dlgKhuyenMai(SwingUtilities.getWindowAncestor(this), null);
            dlg.setVisible(true);
            if (dlg.isDaLuu()) {
                taiDanhSach();
            }
        });

        btnSua.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow < 0) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn dòng khuyến mãi cần chỉnh sửa trên bảng.", "Thông báo", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String maKM = model.getValueAt(selectedRow, 0).toString();
            KhuyenMai km = service.layDanhSach().stream()
                    .filter(x -> x.getMaKhuyenMai().equals(maKM))
                    .findFirst().orElse(null);

            if (km != null) {
                dlgKhuyenMai dlg = new dlgKhuyenMai(SwingUtilities.getWindowAncestor(this), km);
                dlg.setVisible(true);
                if (dlg.isDaLuu()) {
                    taiDanhSach();
                }
            }
        });

        btnXoa.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow < 0) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn chương trình khuyến mãi cần xóa.", "Thông báo", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String maKM = model.getValueAt(selectedRow, 0).toString();
            int confirm = JOptionPane.showConfirmDialog(this, "Bạn có chắc chắn muốn xóa khuyến mãi [" + maKM + "]?", "Xác nhận xóa", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    service.xoa(maKM);
                    JOptionPane.showMessageDialog(this, "Xóa khuyến mãi thành công!");
                    taiDanhSach();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Không thể xóa khuyến mãi: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        btnLamMoi.addActionListener(e -> taiDanhSach());
    }

    private void taiDanhSach() {
        try {
            model.setRowCount(0);
            List<KhuyenMai> danhSach = service.layDanhSach();
            for (KhuyenMai km : danhSach) {
                model.addRow(new Object[]{
                        km.getMaKhuyenMai(),
                        km.getTenKhuyenMai(),
                        km.getPhanTramGiam() + "%",
                        km.getDieuKienApDung(),
                        km.getNgayBatDau(),
                        km.getNgayKetThuc()
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Không thể tải danh sách khuyến mãi: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
}