package gui.panel;

import java.awt.*;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.Locale;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

import appColor.AppColors;

public class pnlChiTietHoaDon extends JPanel {

    private static final Color PINK_LIGHT = new Color(0xFD, 0xEC, 0xF1);
    private static final Color FIELD_BG = new Color(0xF3, 0xF4, 0xF6);
    private static final Color RED = new Color(0xD3, 0x2F, 0x2F);
    private static final String[] COLUMNS = { "STT", "Tên sản phẩm", "Số lượng", "Đơn giá", "Thành tiền" };
    private static final DecimalFormat MONEY = new DecimalFormat("#,###", DecimalFormatSymbols.getInstance(Locale.US));

    // Thông tin đơn hàng / khách hàng
    private final JLabel lblMaDon = valueLabel(), lblNgayTao = valueLabel(), lblNhanVien = valueLabel();
    private final JLabel lblTenKH = valueLabel(), lblSdt = valueLabel(), lblDiaChi = valueLabel();

    // Thanh toán
    private final JLabel lblTamTinh = new JLabel(), lblGiamGia = new JLabel(), lblMaGiamGia = new JLabel(),
            lblTong = new JLabel();

    // Phương thức + tiền khách đưa
    private final JRadioButton rdTienMat = new JRadioButton("Tiền mặt");
    private final JRadioButton rdQR = new JRadioButton("QR Code");
    private final JLabel lblKhachDua = valueLabel(), lblTienThua = new JLabel();
    private JPanel pnlKhachDua;

    private DefaultTableModel model;
    private JTable table;
    private final JButton btnQuayLai = new JButton("Quay lại");

    public pnlChiTietHoaDon() {
        setLayout(new BorderLayout(0, 15));
        setBackground(AppColors.BACKGROUND);
        setBorder(new EmptyBorder(15, 20, 20, 20));

        add(createHeader(), BorderLayout.NORTH);
        add(createBody(), BorderLayout.CENTER);
    }

    // ================= HEADER =================
    private JPanel createHeader() {
        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        header.setBackground(AppColors.BACKGROUND);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 2, 0, AppColors.PRIMARY), new EmptyBorder(0, 0, 8, 0)));

        JLabel title = new JLabel("Chi tiết đơn hàng");
        title.setFont(new Font("Arial", Font.BOLD, 24));
        title.setForeground(AppColors.PRIMARY_DARK);

        // Icon (tuỳ chọn): đặt receipt.png vào /icons, không có thì bỏ qua
        java.net.URL url = getClass().getResource("/icons/receipt.png");
        if (url != null) {
            title.setIcon(new ImageIcon(url));
            title.setIconTextGap(12);
        }
        header.add(title);
        return header;
    }

    // ================= BODY (2 CỘT) =================
    private JPanel createBody() {
        JPanel body = new JPanel(new GridBagLayout());
        body.setBackground(AppColors.BACKGROUND);
        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.BOTH;
        gc.weighty = 1;

        gc.gridx = 0;
        gc.weightx = 0.64;
        gc.insets = new Insets(0, 0, 0, 15);
        body.add(createLeft(), gc);

        gc.gridx = 1;
        gc.weightx = 0.36;
        gc.insets = new Insets(0, 0, 0, 0);
        body.add(createRight(), gc);
        return body;
    }

    // ---------- CỘT TRÁI ----------
    private JPanel createLeft() {
        JPanel left = new JPanel(new BorderLayout(0, 15));
        left.setBackground(AppColors.BACKGROUND);

        JPanel top = new JPanel(new GridLayout(1, 2, 15, 0));
        top.setBackground(AppColors.BACKGROUND);
        top.add(createCard("Thông tin đơn hàng",
                infoRows(new String[] { "Mã đơn hàng:", "Ngày tạo:", "Nhân viên:" },
                        new JLabel[] { lblMaDon, lblNgayTao, lblNhanVien })));
        top.add(createCard("Thông tin khách hàng",
                infoRows(new String[] { "Họ và tên:", "Số điện thoại:", "Địa chỉ:" },
                        new JLabel[] { lblTenKH, lblSdt, lblDiaChi })));

        left.add(top, BorderLayout.NORTH);
        left.add(createCard("Danh sách sản phẩm", createTable()), BorderLayout.CENTER);
        return left;
    }

    private JPanel infoRows(String[] labels, JLabel[] values) {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(AppColors.WHITE);
        p.setBorder(new EmptyBorder(12, 14, 12, 14));
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(5, 0, 5, 8);
        gc.anchor = GridBagConstraints.WEST;
        for (int i = 0; i < labels.length; i++) {
            gc.gridy = i;
            gc.gridx = 0;
            gc.weightx = 0;
            gc.fill = GridBagConstraints.NONE;
            JLabel l = new JLabel(labels[i]);
            l.setFont(new Font("Arial", Font.PLAIN, 13));
            l.setForeground(AppColors.TEXT);
            p.add(l, gc);

            gc.gridx = 1;
            gc.weightx = 1;
            gc.fill = GridBagConstraints.HORIZONTAL;
            gc.insets = new Insets(5, 0, 5, 0);
            p.add(values[i], gc);
            gc.insets = new Insets(5, 0, 5, 8);
        }
        return p;
    }

    private JComponent createTable() {
        model = new DefaultTableModel(COLUMNS, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        table = new JTable(model);
        table.setRowHeight(36);
        table.setFont(new Font("Arial", Font.PLAIN, 13));
        table.setForeground(AppColors.TEXT);
        table.setGridColor(AppColors.BORDER);
        table.setShowGrid(true);
        table.setFillsViewportHeight(true);
        table.setSelectionBackground(PINK_LIGHT);
        table.setSelectionForeground(AppColors.TEXT);

        JTableHeader th = table.getTableHeader();
        th.setReorderingAllowed(false);
        th.setPreferredSize(new Dimension(0, 36));
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
        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(SwingConstants.CENTER);
        DefaultTableCellRenderer left = new DefaultTableCellRenderer();
        left.setBorder(new EmptyBorder(0, 10, 0, 0));
        DefaultTableCellRenderer right = new DefaultTableCellRenderer();
        right.setHorizontalAlignment(SwingConstants.RIGHT);
        right.setBorder(new EmptyBorder(0, 0, 0, 10));

        DefaultTableCellRenderer[] cell = { center, left, center, right, right };
        int[] widths = { 50, 260, 80, 110, 120 };
        for (int i = 0; i < COLUMNS.length; i++) {
            table.getColumnModel().getColumn(i).setHeaderRenderer(headerRenderer);
            table.getColumnModel().getColumn(i).setCellRenderer(cell[i]);
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(new EmptyBorder(12, 12, 12, 12));
        scroll.getViewport().setBackground(AppColors.WHITE);
        scroll.setBackground(AppColors.WHITE);
        return scroll;
    }

    // ---------- CỘT PHẢI ----------
    private JPanel createRight() {
        JPanel right = new JPanel();
        right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));
        right.setBackground(AppColors.BACKGROUND);

        // Thanh toán
        JPanel pay = new JPanel(new GridBagLayout());
        pay.setBackground(AppColors.WHITE);
        pay.setBorder(new EmptyBorder(12, 14, 12, 14));
        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.insets = new Insets(6, 0, 6, 0);

        styleRight(lblTamTinh, Font.PLAIN, 13, AppColors.TEXT);
        styleRight(lblGiamGia, Font.PLAIN, 13, RED);
        styleRight(lblMaGiamGia, Font.PLAIN, 13, AppColors.PRIMARY_DARK);
        styleRight(lblTong, Font.BOLD, 20, AppColors.PRIMARY_DARK);

        addPayRow(pay, gc, 0, "Tạm tính:", lblTamTinh, false);
        addPayRow(pay, gc, 1, "Giảm giá:", lblGiamGia, false);
        addPayRow(pay, gc, 2, "Mã giảm giá:", lblMaGiamGia, false);

        gc.gridy = 3;
        gc.gridx = 0;
        gc.gridwidth = 2;
        pay.add(new JSeparator(), gc);
        gc.gridwidth = 1;
        addPayRow(pay, gc, 4, "Tổng thanh toán:", lblTong, true);

        right.add(createCard("Thanh toán", pay));
        right.add(Box.createVerticalStrut(15));

        // Phương thức thanh toán
        JPanel method = new JPanel(new GridLayout(1, 2, 10, 0));
        method.setBackground(AppColors.WHITE);
        method.setBorder(new EmptyBorder(12, 20, 12, 20));
        ButtonGroup bg = new ButtonGroup();
        for (JRadioButton rd : new JRadioButton[] { rdTienMat, rdQR }) {
            rd.setBackground(AppColors.WHITE);
            rd.setFont(new Font("Arial", Font.PLAIN, 13));
            rd.setForeground(AppColors.TEXT);
            rd.setFocusPainted(false);
            rd.setEnabled(false); // chỉ xem
            bg.add(rd);
            method.add(rd);
        }
        right.add(createCard("Phương thức thanh toán", method));
        right.add(Box.createVerticalStrut(15));

        // Khách hàng đưa tiền
        pnlKhachDua = new JPanel(new GridBagLayout());
        pnlKhachDua.setBackground(AppColors.WHITE);
        pnlKhachDua.setBorder(new EmptyBorder(12, 14, 12, 14));
        styleRight(lblTienThua, Font.BOLD, 18, AppColors.PRIMARY_DARK);
        lblKhachDua.setHorizontalAlignment(SwingConstants.RIGHT);
        lblKhachDua.setPreferredSize(new Dimension(130, 30));

        GridBagConstraints g2 = new GridBagConstraints();
        g2.fill = GridBagConstraints.HORIZONTAL;
        g2.insets = new Insets(6, 0, 6, 0);
        addPayRow(pnlKhachDua, g2, 0, "Số tiền khách đưa:", lblKhachDua, false);
        g2.gridy = 1;
        g2.gridx = 0;
        g2.gridwidth = 2;
        pnlKhachDua.add(new JSeparator(), g2);
        g2.gridwidth = 1;
        addPayRow(pnlKhachDua, g2, 2, "Tiền thừa trả khách:", lblTienThua, true);
        right.add(createCard("Khách hàng đưa tiền", pnlKhachDua));

        right.add(Box.createVerticalGlue());

        // Nút Quay lại
        btnQuayLai.setFont(new Font("Arial", Font.BOLD, 16));
        btnQuayLai.setBackground(AppColors.PRIMARY);
        btnQuayLai.setForeground(AppColors.WHITE);
        btnQuayLai.setFocusPainted(false);
        btnQuayLai.setBorderPainted(false);
        btnQuayLai.setOpaque(true);
        btnQuayLai.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnQuayLai.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        btnQuayLai.setPreferredSize(new Dimension(0, 46));
        btnQuayLai.setAlignmentX(Component.LEFT_ALIGNMENT);
        right.add(Box.createVerticalStrut(15));
        right.add(btnQuayLai);
        return right;
    }

    private void addPayRow(JPanel p, GridBagConstraints gc, int row, String label, JLabel value, boolean big) {
        gc.gridy = row;
        gc.gridx = 0;
        gc.weightx = 0;
        JLabel l = new JLabel(label);
        l.setFont(new Font("Arial", big ? Font.BOLD : Font.PLAIN, 13));
        l.setForeground(AppColors.TEXT);
        p.add(l, gc);
        gc.gridx = 1;
        gc.weightx = 1;
        p.add(value, gc);
    }

    private void styleRight(JLabel l, int style, int size, Color c) {
        l.setFont(new Font("Arial", style, size));
        l.setForeground(c);
        l.setHorizontalAlignment(SwingConstants.RIGHT);
    }

    // UI Vip
    private JPanel createCard(String title, JComponent content) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(AppColors.WHITE);
        card.setBorder(BorderFactory.createLineBorder(AppColors.BORDER));

        JLabel lbl = new JLabel(title);
        lbl.setOpaque(true);
        lbl.setBackground(PINK_LIGHT);
        lbl.setForeground(AppColors.PRIMARY_DARK);
        lbl.setFont(new Font("Arial", Font.BOLD, 13));
        lbl.setBorder(new EmptyBorder(8, 14, 8, 14));

        card.add(lbl, BorderLayout.NORTH);
        card.add(content, BorderLayout.CENTER);
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        return card;
    }

    private static JLabel valueLabel() {
        JLabel l = new JLabel(" ");
        l.setOpaque(true);
        l.setBackground(FIELD_BG);
        l.setFont(new Font("Arial", Font.PLAIN, 13));
        l.setForeground(AppColors.TEXT);
        l.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(AppColors.BORDER),
                new EmptyBorder(5, 8, 5, 8)));
        return l;
    }

    private static String money(long v) {
        return MONEY.format(v) + " đ";
    }

    // Function cho quay lại
    public void setOnBack(Runnable r) {
        for (var al : btnQuayLai.getActionListeners()) {
            btnQuayLai.removeActionListener(al);
        }
        btnQuayLai.addActionListener(e -> r.run());
    }

    public void setThongTin(String maDon, String ngayTao, String nhanVien, String tenKH, String sdt, String diaChi) {
        lblMaDon.setText(maDon);
        lblNgayTao.setText(ngayTao);
        lblNhanVien.setText(nhanVien);
        lblTenKH.setText(tenKH);
        lblSdt.setText(sdt);
        lblDiaChi.setText(diaChi);

    }

    /** Mỗi dòng: { tenSP (String), soLuong (Number), donGia (Number) } */
    public void setSanPham(List<Object[]> rows) {
        model.setRowCount(0);
        int stt = 1;
        for (Object[] r : rows) {
            long sl = ((Number) r[1]).longValue();
            long gia = ((Number) r[2]).longValue();
            model.addRow(new Object[] { stt++, r[0], sl, money(gia), money(sl * gia) });
        }
    }

    public void setThanhToan(long tamTinh, long giamGia, String maGiamGia, boolean tienMat, long tienKhachDua) {
        long tong = tamTinh - giamGia;
        lblTamTinh.setText(money(tamTinh));
        lblGiamGia.setText(giamGia > 0 ? "-" + money(giamGia) : money(0));
        lblMaGiamGia.setText(maGiamGia == null || maGiamGia.isEmpty() ? "—" : maGiamGia);
        lblTong.setText(money(tong));

        rdTienMat.setSelected(tienMat);
        rdQR.setSelected(!tienMat);

        if (tienMat) {
            lblKhachDua.setText(MONEY.format(tienKhachDua) + "  đ");
            lblTienThua.setText(money(Math.max(0, tienKhachDua - tong)));
        } else { // QR: không có tiền thừa
            lblKhachDua.setText(MONEY.format(tong) + "  đ");
            lblTienThua.setText(money(0));
        }
    }

    public void hienThi(dao.ChiTietHoaDonDAO.ChiTietView v) {
        java.time.format.DateTimeFormatter f = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy");
        setThongTin(v.maHD, v.ngayLap == null ? "" : v.ngayLap.format(f), v.tenNV,
                v.tenKH == null ? "Khách lẻ" : v.tenKH, v.sdt == null ? "" : v.sdt, v.diaChi == null ? "" : v.diaChi);

        java.math.BigDecimal tamTinh = java.math.BigDecimal.ZERO;
        List<Object[]> rows = new java.util.ArrayList<>();
        for (dao.ChiTietHoaDonDAO.Dong d : v.dong) {
            rows.add(new Object[] { d.tenDV, d.soLuong, d.donGia.longValue() });
            tamTinh = tamTinh.add(d.donGia.multiply(java.math.BigDecimal.valueOf(d.soLuong)));
        }
        setSanPham(rows);

        long giamGia = tamTinh.multiply(v.phanTramGiam).divide(java.math.BigDecimal.valueOf(100))
                .setScale(0, java.math.RoundingMode.HALF_UP).longValue();
        boolean tienMat = v.tenPTTT != null && v.tenPTTT.toLowerCase().contains("tiền mặt");
        setThanhToan(tamTinh.longValue(), giamGia, v.maKhuyenMai, tienMat, v.tienKhachDua.longValue());
    }
}