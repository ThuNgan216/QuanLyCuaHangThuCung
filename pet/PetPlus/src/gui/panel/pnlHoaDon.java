package gui.panel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Graphics;
import java.awt.Image;
import java.net.URL;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

import appColor.AppColors;
import dao.HoaDonDAO;
import entity.HoaDon;
import entity.KhachHang;

public class pnlHoaDon extends JPanel {

    private static final Color PINK_LIGHT = new Color(0xFFF0F5); // nền tiêu đề / header bảng
    private static final Color PINK_SELECT = new Color(0xFFE0EA); // nền ô đang chọn

    private static final String[] COLUMNS = { "STT", "Mã đơn hàng", "Tên khách hàng", "Số điện thoại", "Ngày lập",
            "Thành tiền" };

    private final HoaDonDAO hoaDonDAO = new HoaDonDAO();
    private DefaultTableModel model;
    private JTable table;
    private JTextField txtSearch;
    private JComboBox<String> cboNgay, cboThang, cboNam;
    private JRadioButton[] rdoPrice = new JRadioButton[4];
    private JPanel[] priceCards = new JPanel[4];
    private ButtonGroup priceGroup = new ButtonGroup();
    private JToggleButton btnTang, btnGiam;
    private ButtonGroup sortGroup = new ButtonGroup();

    public pnlHoaDon() {
        setLayout(new BorderLayout());
        setBackground(AppColors.BACKGROUND);

        add(createHeader(), BorderLayout.NORTH);

        JPanel body = new JPanel(new BorderLayout(15, 0));
        body.setBackground(AppColors.BACKGROUND);
        body.setBorder(new EmptyBorder(15, 15, 15, 15));
        body.add(createListPanel(), BorderLayout.CENTER);
        body.add(createActionPanel(), BorderLayout.EAST);
        add(body, BorderLayout.CENTER);

        applyFilter();
    }

    // ================= HEADER =================
    private JPanel createHeader() {
        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 12));
        header.setBackground(PINK_LIGHT);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, AppColors.BORDER));

        JLabel lblTitle = new JLabel("Quản lý đơn hàng");
        lblTitle.setFont(new Font("Arial", Font.BOLD, 24));
        lblTitle.setForeground(AppColors.PRIMARY_DARK);

        URL url = getClass().getResource("/icons/cart.png");
        if (url != null) {
            Image img = new ImageIcon(url).getImage().getScaledInstance(32, 32, Image.SCALE_SMOOTH);
            lblTitle.setIcon(new ImageIcon(img));
            lblTitle.setIconTextGap(12);
        }
        header.add(lblTitle);
        return header;
    }

    // ================= DANH SÁCH ĐƠN HÀNG (TRÁI) =================
    private JPanel createListPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(AppColors.WHITE);
        panel.setBorder(BorderFactory.createLineBorder(AppColors.BORDER));

        panel.add(createSectionTitle("Danh sách đơn hàng"), BorderLayout.NORTH);

        model = new DefaultTableModel(COLUMNS, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(model);
        table.setRowHeight(38);
        table.setFont(new Font("Arial", Font.PLAIN, 13));
        table.setForeground(AppColors.TEXT);
        table.setGridColor(AppColors.BORDER);
        table.setShowGrid(true);
        table.setSelectionBackground(PINK_SELECT);
        table.setSelectionForeground(AppColors.TEXT);
        table.setFillsViewportHeight(false);

        // Header bảng
        JTableHeader th = table.getTableHeader();
        th.setReorderingAllowed(false);
        th.setPreferredSize(new Dimension(0, 38));
        DefaultTableCellRenderer headerRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(t, v, s, f, r, c);
                lbl.setBackground(PINK_LIGHT);
                lbl.setForeground(AppColors.PRIMARY_DARK);
                lbl.setFont(new Font("Arial", Font.BOLD, 13));
                lbl.setHorizontalAlignment(SwingConstants.CENTER);
                lbl.setBorder(BorderFactory.createLineBorder(AppColors.BORDER));
                return lbl;
            }
        };
        for (int i = 0; i < COLUMNS.length; i++) {
            table.getColumnModel().getColumn(i).setHeaderRenderer(headerRenderer);
        }

        // Cell: căn giữa, cột Thành tiền đậm + màu hồng
        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(SwingConstants.CENTER);
        DefaultTableCellRenderer money = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(t, v, s, f, r, c);
                lbl.setText(formatMoney(((Number) v).longValue()));
                lbl.setFont(new Font("Arial", Font.BOLD, 13));
                lbl.setForeground(AppColors.PRIMARY_DARK);
                lbl.setHorizontalAlignment(SwingConstants.RIGHT);
                lbl.setBorder(new EmptyBorder(0, 0, 0, 12));
                return lbl;
            }
        };
        for (int i = 0; i < COLUMNS.length - 1; i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(center);
        }
        table.getColumnModel().getColumn(5).setCellRenderer(money);

        int[] widths = { 50, 100, 170, 130, 100, 120 };
        for (int i = 0; i < widths.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(new EmptyBorder(12, 12, 12, 12));
        scroll.getViewport().setBackground(AppColors.WHITE);
        scroll.setBackground(AppColors.WHITE);

        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    // ================= THAO TÁC (PHẢI) =================
    private JPanel createActionPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(AppColors.WHITE);
        panel.setBorder(BorderFactory.createLineBorder(AppColors.BORDER));
        panel.setPreferredSize(new Dimension(340, 0));

        panel.add(createSectionTitle("Thao tác"), BorderLayout.NORTH);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(AppColors.WHITE);
        content.setBorder(new EmptyBorder(12, 12, 12, 12));

        // --- Ô tìm kiếm ---
        JPanel searchRow = new JPanel(new BorderLayout(8, 0));
        searchRow.setBackground(AppColors.WHITE);
        searchRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        searchRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        txtSearch = new JTextField() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (getText().isEmpty()) { // placeholder
                    g.setColor(Color.GRAY);
                    g.setFont(getFont());
                    g.drawString("Tìm kiếm", getInsets().left, (getHeight() + g.getFontMetrics().getAscent()) / 2 - 2);
                }
            }
        };
        txtSearch.setFont(new Font("Arial", Font.PLAIN, 13));
        txtSearch.setBackground(PINK_LIGHT);
        txtSearch.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(AppColors.BORDER),
                new EmptyBorder(5, 8, 5, 8)));
        txtSearch.addActionListener(e -> applyFilter());

        JButton btnSearchSmall = createPinkButton("Tìm kiếm", false);
        btnSearchSmall.addActionListener(e -> applyFilter());

        searchRow.add(txtSearch, BorderLayout.CENTER);
        searchRow.add(btnSearchSmall, BorderLayout.EAST);
        content.add(searchRow);
        content.add(Box.createVerticalStrut(18));
        content.add(createDivider());

        // --- Ngày tháng năm ---
        content.add(createLabel("Ngày tháng năm"));
        JPanel dateRow = new JPanel(new GridLayout(1, 3, 8, 0));
        dateRow.setBackground(AppColors.WHITE);
        dateRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        dateRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));

        cboNgay = new JComboBox<>(buildRange("Ngày", 1, 31));
        cboThang = new JComboBox<>(buildRange("Tháng", 1, 12));
        cboNam = new JComboBox<>(buildRange("Năm", 2024, 2026));
        dateRow.add(wrapCombo(cboNgay));
        dateRow.add(wrapCombo(cboThang));
        dateRow.add(wrapCombo(cboNam));
        content.add(dateRow);
        content.add(Box.createVerticalStrut(15));
        content.add(createDivider());

        // --- Mức giá ---
        content.add(createLabel("Mức giá"));
        JPanel priceRow = new JPanel(new GridLayout(1, 4, 6, 0));
        priceRow.setBackground(AppColors.WHITE);
        priceRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        priceRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
        String[] priceTexts = { "0-500k", "500k-1500k", "1500k-3000k", "3000k-Hơn nữa" };
        for (int i = 0; i < 4; i++) {
            priceCards[i] = createPriceCard(priceTexts[i], i);
            priceRow.add(priceCards[i]);
        }
        content.add(priceRow);
        content.add(Box.createVerticalStrut(15));
        content.add(createDivider());

        // --- Sắp xếp giá ---
        content.add(createLabel("Sắp xếp giá"));
        JPanel sortRow = new JPanel(new GridLayout(1, 2, 10, 0));
        sortRow.setBackground(AppColors.WHITE);
        sortRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        sortRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 52));
        btnTang = createSortButton("Tăng dần", "Từ thấp đến cao");
        btnGiam = createSortButton("Giảm dần", "Từ cao đến thấp");
        sortRow.add(btnTang);
        sortRow.add(btnGiam);
        content.add(sortRow);
        content.add(Box.createVerticalStrut(25));

        // --- Nút Tìm kiếm lớn ---
        JButton btnSearch = createPinkButton("Tìm kiếm", true);
        btnSearch.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnSearch.addActionListener(e -> applyFilter());
        content.add(btnSearch);

        panel.add(content, BorderLayout.CENTER);
        return panel;
    }

    // ================= LỌC / SẮP XẾP DỮ LIỆU (TRUY VẤN SQL) =================
    private void applyFilter() {
        String keyword = txtSearch.getText().trim();
        Integer ngay = cboNgay.getSelectedIndex() > 0 ? Integer.valueOf((String) cboNgay.getSelectedItem()) : null;
        Integer thang = cboThang.getSelectedIndex() > 0 ? Integer.valueOf((String) cboThang.getSelectedItem()) : null;
        Integer nam = cboNam.getSelectedIndex() > 0 ? Integer.valueOf((String) cboNam.getSelectedItem()) : null;

        int priceIdx = -1;
        for (int i = 0; i < rdoPrice.length; i++) {
            if (rdoPrice[i].isSelected()) {
                priceIdx = i;
            }
        }
        int sort = btnTang.isSelected() ? 1 : (btnGiam.isSelected() ? -1 : 0);

        try {
            Map<String, Double> tongTien = new HashMap<>();
            List<HoaDon> ds = hoaDonDAO.timKiem(keyword, ngay, thang, nam, priceIdx, sort, tongTien);
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");

            model.setRowCount(0);
            int stt = 1;
            for (HoaDon hd : ds) {
                KhachHang kh = hd.getMaKH();
                model.addRow(new Object[] { String.format("%02d", stt++), hd.getMaHoaDon(), kh.getTenKH(),
                        kh.getSdt(), hd.getNgayLapHD() == null ? "" : hd.getNgayLapHD().format(fmt),
                        tongTien.get(hd.getMaHoaDon()) });
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi truy vấn dữ liệu: " + ex.getMessage(), "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    // ================= HÀM TIỆN ÍCH GIAO DIỆN =================
    private JLabel createSectionTitle(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setOpaque(true);
        lbl.setBackground(PINK_LIGHT);
        lbl.setForeground(AppColors.PRIMARY_DARK);
        lbl.setFont(new Font("Arial", Font.BOLD, 14));
        lbl.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, AppColors.BORDER), new EmptyBorder(10, 12, 10, 12)));
        return lbl;
    }

    private JLabel createLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Arial", Font.BOLD, 13));
        lbl.setForeground(AppColors.PRIMARY_DARK);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        lbl.setBorder(new EmptyBorder(12, 0, 8, 0));
        return lbl;
    }

    private JSeparator createDivider() {
        JSeparator sep = new JSeparator();
        sep.setForeground(AppColors.BORDER);
        sep.setAlignmentX(Component.LEFT_ALIGNMENT);
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 2));
        return sep;
    }

    private JButton createPinkButton(String text, boolean large) {
        JButton btn = new JButton(text);
        btn.setBackground(PINK_SELECT);
        btn.setForeground(AppColors.PRIMARY_DARK);
        btn.setFont(new Font("Arial", Font.BOLD, 13));
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createLineBorder(AppColors.BORDER));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(large ? new Dimension(150, 36) : new Dimension(80, 32));
        if (large) {
            btn.setMaximumSize(new Dimension(150, 36));
        }
        return btn;
    }

    private String[] buildRange(String first, int from, int to) {
        String[] arr = new String[to - from + 2];
        arr[0] = first;
        for (int i = from; i <= to; i++) {
            arr[i - from + 1] = String.valueOf(i);
        }
        return arr;
    }

    private JPanel wrapCombo(JComboBox<String> cbo) {
        cbo.setFont(new Font("Arial", Font.PLAIN, 13));
        cbo.setBackground(AppColors.WHITE);
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(AppColors.WHITE);
        p.add(cbo, BorderLayout.CENTER);
        return p;
    }

    // Ô chọn mức giá: chữ ở trên, radio ở dưới
    private JPanel createPriceCard(String text, int index) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(AppColors.WHITE);
        card.setBorder(BorderFactory.createLineBorder(AppColors.BORDER));
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JLabel lbl = new JLabel(text, SwingConstants.CENTER);
        lbl.setFont(new Font("Arial", Font.PLAIN, 11));
        lbl.setForeground(AppColors.TEXT);
        lbl.setBorder(new EmptyBorder(8, 2, 0, 2));
        lbl.setFont(new Font("Arial", Font.PLAIN, 11));
        lbl.setForeground(AppColors.TEXT);
        lbl.setBorder(new EmptyBorder(8, 2, 0, 2));

        JRadioButton rdo = new JRadioButton();
        rdo.setHorizontalAlignment(SwingConstants.CENTER);
        rdo.setOpaque(false);
        rdo.setFocusPainted(false);
        rdoPrice[index] = rdo;
        priceGroup.add(rdo);

        rdo.addItemListener(e -> {
            for (int i = 0; i < 4; i++) {
                if (priceCards[i] != null) {
                    boolean sel = rdoPrice[i].isSelected();
                    priceCards[i].setBackground(sel ? PINK_SELECT : AppColors.WHITE);
                    priceCards[i].setBorder(BorderFactory.createLineBorder(sel ? AppColors.PRIMARY : AppColors.BORDER,
                            sel ? 2 : 1));
                }
            }
        });

        card.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                rdo.setSelected(true);
            }
        });

        card.add(lbl, BorderLayout.CENTER);
        card.add(rdo, BorderLayout.SOUTH);
        return card;
    }

    // Nút sắp xếp 2 dòng chữ (tiêu đề + mô tả nhỏ)
    private JToggleButton createSortButton(String title, String sub) {
        JToggleButton btn = new JToggleButton();
        btn.setLayout(new GridLayout(2, 1));

        JLabel lblTitle = new JLabel(title, SwingConstants.CENTER);
        lblTitle.setFont(new Font("Arial", Font.BOLD, 13));
        lblTitle.setForeground(AppColors.TEXT);

        JLabel lblSub = new JLabel(sub, SwingConstants.CENTER);
        lblSub.setFont(new Font("Arial", Font.PLAIN, 10));
        lblSub.setForeground(Color.GRAY);

        btn.add(lblTitle);
        btn.add(lblSub);

        btn.setBackground(AppColors.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createLineBorder(AppColors.BORDER));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        sortGroup.add(btn);

        btn.addItemListener(e -> {
            for (JToggleButton b : new JToggleButton[] { btnTang, btnGiam }) {
                if (b != null) {
                    boolean sel = b.isSelected();
                    b.setBackground(sel ? PINK_SELECT : AppColors.WHITE);
                    b.setBorder(
                            BorderFactory.createLineBorder(sel ? AppColors.PRIMARY : AppColors.BORDER, sel ? 2 : 1));
                }
            }
        });
        return btn;
    }

    private String formatMoney(long value) {
        return String.format("%,d", value).replace(',', '.') + " đ";
    }
}