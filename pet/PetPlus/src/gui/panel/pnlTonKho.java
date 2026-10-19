package gui.panel;

import entity.SanPham;
import java.awt.*;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import service.TonKhoService;
import utils.PetPlusTheme;

public class pnlTonKho extends JPanel {
    private DefaultTableModel model;
    private JTable table;
    private final TonKhoService tonKhoService = new TonKhoService();
    private JSpinner spnMucToiThieu;
    private JButton btnLoc;
    private JLabel lblTongCanhBao = new JLabel("0");

    public pnlTonKho() {
        setLayout(new BorderLayout(15, 15));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        khoiTaoGiaoDien();
        ganSuKien();
        taiDanhSach(10); // Mặc định cảnh báo tồn kho <= 10
        PetPlusTheme.apply(this);
    }

    private void khoiTaoGiaoDien() {
        // --- 1. PHẦN TIÊU ĐỀ VÀ BỘ LỌC (NORTH) ---
        JPanel pnlNorth = new JPanel(new BorderLayout(10, 10));
        pnlNorth.setOpaque(false);

        JLabel lblTitle = new JLabel("CẢNH BÁO TỒN KHO HÀNG HÓA");
        lblTitle.setFont(new Font("Arial", Font.BOLD, 22));
        lblTitle.setForeground(new Color(224, 25, 91));
        pnlNorth.add(lblTitle, BorderLayout.WEST);

        // Panel chứa thẻ card tổng quan và bộ lọc
        JPanel pnlFilterWrapper = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        pnlFilterWrapper.setOpaque(false);

        JPanel pnlCardSummary = createCardSummary("Sản phẩm cần chú ý:", lblTongCanhBao);
        pnlFilterWrapper.add(pnlCardSummary);

        JPanel pnlFilter = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        pnlFilter.setOpaque(false);
        pnlFilter.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(218, 225, 233)),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        pnlFilter.setBackground(new Color(248, 249, 250));

        spnMucToiThieu = new JSpinner(new SpinnerNumberModel(10, 0, 1000, 1));
        spnMucToiThieu.setPreferredSize(new Dimension(70, 30));
        spnMucToiThieu.setFont(new Font("Arial", Font.PLAIN, 13));
        
        btnLoc = new JButton("Lọc Cảnh Báo");
        btnLoc.setBackground(new Color(250, 48, 115));
        btnLoc.setForeground(Color.WHITE);
        btnLoc.setFocusPainted(false);
        btnLoc.setFont(new Font("Arial", Font.BOLD, 12));

        pnlFilter.add(new JLabel("Mức tồn tối thiểu:"));
        pnlFilter.add(spnMucToiThieu);
        pnlFilter.add(btnLoc);

        pnlFilterWrapper.add(pnlFilter);
        pnlNorth.add(pnlFilterWrapper, BorderLayout.EAST);

        add(pnlNorth, BorderLayout.NORTH);

        // --- 2. BẢNG DANH SÁCH CẢNH BÁO (CENTER) ---
        String[] columns = {"Mã SP", "Tên Sản Phẩm", "Đơn Giá Bán", "Số Lượng Tồn", "Trạng Thái Kho"};
        model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        table = new JTable(model);
        table.setRowHeight(32);
        table.setFont(new Font("Arial", Font.PLAIN, 13));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setGridColor(new Color(230, 235, 240));

        // Header table styling
        JTableHeader header = table.getTableHeader();
        header.setPreferredSize(new Dimension(0, 35));
        header.setFont(new Font("Arial", Font.BOLD, 13));

        // Custom căn giữa và màu sắc cột số lượng tồn & trạng thái
        table.getColumnModel().getColumn(3).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(t, v, s, f, r, c);
                lbl.setHorizontalAlignment(SwingConstants.CENTER);
                lbl.setFont(new Font("Arial", Font.BOLD, 13));
                return lbl;
            }
        });

        table.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(t, v, s, f, r, c);
                lbl.setHorizontalAlignment(SwingConstants.CENTER);
                String val = String.valueOf(v);
                if ("Hết hàng".equals(val)) {
                    lbl.setForeground(new Color(215, 48, 70));
                } else {
                    lbl.setForeground(new Color(215, 128, 0));
                }
                lbl.setFont(new Font("Arial", Font.BOLD, 13));
                return lbl;
            }
        });

        add(new JScrollPane(table), BorderLayout.CENTER);
    }

    private JPanel createCardSummary(String title, JLabel lblVal) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        p.setOpaque(false);
        JLabel t = new JLabel(title);
        t.setFont(new Font("Arial", Font.PLAIN, 13));
        t.setForeground(new Color(89, 111, 140));
        lblVal.setFont(new Font("Arial", Font.BOLD, 16));
        lblVal.setForeground(new Color(224, 25, 91));
        p.add(t);
        p.add(lblVal);
        return p;
    }

    private void ganSuKien() {
        btnLoc.addActionListener(e -> {
            int mucToiThieu = (int) spnMucToiThieu.getValue();
            taiDanhSach(mucToiThieu);
        });
    }

    private void taiDanhSach(int mucToiThieu) {
        try {
            model.setRowCount(0);
            List<SanPham> danhSach = tonKhoService.layCanhBaoTonKho(mucToiThieu);
            
            for (SanPham sp : danhSach) {
                int ton = sp.getSoLuongTon();
                String trangThai = ton == 0 ? "Hết hàng" : "Sắp hết hàng";
                model.addRow(new Object[]{
                        sp.getMaSP(),
                        sp.getTenSP(),
                        String.format("%,.0f đ", (double) sp.getGiaBan()),
                        ton,
                        trangThai
                });
            }
            lblTongCanhBao.setText(String.valueOf(danhSach.size()));
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Không thể tải dữ liệu tồn kho: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
}