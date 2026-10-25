package gui.dialogs;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.KeyEvent;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

import appColor.AppColors;
import dao.ChiTietHoaDonDAO;

/**
 * Dialog xem chi tiết hóa đơn (chỉ xem).
 * Mỗi lần mở tạo một dialog mới: new dlgChiTietHoaDon(owner,
 * view).setVisible(true);
 */
public class dlgChiTietHoaDon extends JDialog {

    private static final Color PINK_LIGHT = new Color(0xFD, 0xEC, 0xF1);
    private static final Color FIELD_BG = new Color(0xF3, 0xF4, 0xF6);
    private static final Color RED = new Color(0xD3, 0x2F, 0x2F);
    private static final String[] COLUMNS = { "STT", "Sản phẩm / Dịch vụ", "Số lượng", "Đơn giá", "Thành tiền" };
    private static final DecimalFormat MONEY = new DecimalFormat("#,###", DecimalFormatSymbols.getInstance(Locale.US));
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

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

    private final DefaultTableModel model = new DefaultTableModel(COLUMNS, 0) {
        @Override
        public boolean isCellEditable(int r, int c) {
            return false;
        }
    };
    private final JTable table = new JTable(model);
    private final JButton btnDong = new JButton("Đóng");

    public dlgChiTietHoaDon(Window owner, ChiTietHoaDonDAO.ChiTietView view) {
        super(owner, "Chi tiết hóa đơn", Dialog.ModalityType.APPLICATION_MODAL);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel root = new JPanel(new BorderLayout(0, 15));
        root.setBackground(AppColors.BACKGROUND);
        root.setBorder(new EmptyBorder(15, 20, 20, 20));
        root.add(createHeader(), BorderLayout.NORTH);
        root.add(createBody(), BorderLayout.CENTER);
        setContentPane(root);

        btnDong.addActionListener(e -> dispose());
        getRootPane().setDefaultButton(btnDong);
        getRootPane().registerKeyboardAction(e -> dispose(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);

        hienThi(view);

        setSize(1100, 650);
        setMinimumSize(new Dimension(900, 560));
        setLocationRelativeTo(owner);
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
        URL url = getClass().getResource("/icons/receipt.png");
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
        gc.anchor = GridBagConstraints.WEST;
        for (int i = 0; i < labels.length; i++) {
            gc.gridy = i;

            gc.gridx = 0;
            gc.weightx = 0;
            gc.fill = GridBagConstraints.NONE;
            gc.insets = new Insets(5, 0, 5, 8);
            JLabel l = new JLabel(labels[i]);
            l.setFont(new Font("Arial", Font.PLAIN, 13));
            l.setForeground(AppColors.TEXT);
            p.add(l, gc);

            gc.gridx = 1;
            gc.weightx = 1;
            gc.fill = GridBagConstraints.HORIZONTAL;
            gc.insets = new Insets(5, 0, 5, 0);
            p.add(values[i], gc);
        }
        return p;
    }

    private JComponent createTable() {
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
        th.setResizingAllowed(false);
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
        DefaultTableCellRenderer left = alignedRenderer(SwingConstants.LEFT, new EmptyBorder(0, 10, 0, 0));
        DefaultTableCellRenderer right = alignedRenderer(SwingConstants.RIGHT, new EmptyBorder(0, 0, 0, 16));

        DefaultTableCellRenderer[] cell = { center, left, center, right, right };
        int[] widths = { 50, 260, 80, 120, 130 };
        for (int i = 0; i < COLUMNS.length; i++) {
            var col = table.getColumnModel().getColumn(i);
            col.setHeaderRenderer(headerRenderer);
            col.setCellRenderer(cell[i]);
            col.setPreferredWidth(widths[i]);
            // Cố định mọi cột trừ cột "Sản phẩm / Dịch vụ"
            if (i != 1) {
                col.setMinWidth(widths[i]);
                col.setMaxWidth(widths[i]);
            }
        }

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(new EmptyBorder(12, 12, 12, 12));
        scroll.getViewport().setBackground(AppColors.WHITE);
        scroll.setBackground(AppColors.WHITE);
        return scroll;
    }

    private static DefaultTableCellRenderer alignedRenderer(int align, EmptyBorder border) {
        return new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(t, v, s, f, r, c);
                lbl.setHorizontalAlignment(align);
                lbl.setBorder(border); // đặt SAU super để không bị ghi đè
                return lbl;
            }
        };
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
        addSeparatorRow(pay, gc, 3);
        addPayRow(pay, gc, 4, "Tổng thanh toán:", lblTong, true);

        right.add(createCard("Thanh toán", pay));
        right.add(Box.createVerticalStrut(15));

        // Phương thức thanh toán (chỉ xem)
        JPanel method = new JPanel(new GridLayout(1, 2, 10, 0));
        method.setBackground(AppColors.WHITE);
        method.setBorder(new EmptyBorder(12, 20, 12, 20));
        ButtonGroup bg = new ButtonGroup();
        for (JRadioButton rd : new JRadioButton[] { rdTienMat, rdQR }) {
            rd.setBackground(AppColors.WHITE);
            rd.setFont(new Font("Arial", Font.PLAIN, 13));
            rd.setForeground(AppColors.TEXT);
            rd.setFocusPainted(false);
            rd.setEnabled(false);
            bg.add(rd);
            method.add(rd);
        }
        right.add(createCard("Phương thức thanh toán", method));
        right.add(Box.createVerticalStrut(15));

        // Khách hàng đưa tiền
        JPanel khachDua = new JPanel(new GridBagLayout());
        khachDua.setBackground(AppColors.WHITE);
        khachDua.setBorder(new EmptyBorder(12, 14, 12, 14));
        styleRight(lblTienThua, Font.BOLD, 18, AppColors.PRIMARY_DARK);
        lblKhachDua.setHorizontalAlignment(SwingConstants.RIGHT);
        lblKhachDua.setPreferredSize(new Dimension(130, 30));

        GridBagConstraints g2 = new GridBagConstraints();
        g2.fill = GridBagConstraints.HORIZONTAL;
        g2.insets = new Insets(6, 0, 6, 0);
        addPayRow(khachDua, g2, 0, "Số tiền khách đưa:", lblKhachDua, false);
        addSeparatorRow(khachDua, g2, 1);
        addPayRow(khachDua, g2, 2, "Tiền thừa trả khách:", lblTienThua, true);
        right.add(createCard("Khách hàng đưa tiền", khachDua));

        right.add(Box.createVerticalGlue());

        // Nút Đóng
        btnDong.setFont(new Font("Arial", Font.BOLD, 16));
        btnDong.setBackground(AppColors.PRIMARY);
        btnDong.setForeground(AppColors.WHITE);
        btnDong.setFocusPainted(false);
        btnDong.setBorderPainted(false);
        btnDong.setOpaque(true);
        btnDong.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnDong.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        btnDong.setPreferredSize(new Dimension(0, 46));
        btnDong.setAlignmentX(Component.LEFT_ALIGNMENT);
        right.add(Box.createVerticalStrut(15));
        right.add(btnDong);
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

    private void addSeparatorRow(JPanel p, GridBagConstraints gc, int row) {
        gc.gridy = row;
        gc.gridx = 0;
        gc.gridwidth = 2;
        p.add(new JSeparator(), gc);
        gc.gridwidth = 1;
    }

    private void styleRight(JLabel l, int style, int size, Color c) {
        l.setFont(new Font("Arial", style, size));
        l.setForeground(c);
        l.setHorizontalAlignment(SwingConstants.RIGHT);
    }

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

    // ================= ĐỔ DỮ LIỆU =================
    private void hienThi(ChiTietHoaDonDAO.ChiTietView v) {
        lblMaDon.setText(v.maHD);
        lblNgayTao.setText(v.ngayLap == null ? "" : v.ngayLap.format(DATE_FMT));
        lblNhanVien.setText(v.tenNV == null ? "" : v.tenNV);
        lblTenKH.setText(v.tenKH == null ? "Khách lẻ" : v.tenKH);
        lblSdt.setText(v.sdt == null ? "" : v.sdt);
        lblDiaChi.setText(v.diaChi == null ? "" : v.diaChi);

        // Danh sách sản phẩm + tạm tính
        model.setRowCount(0);
        BigDecimal tamTinh = BigDecimal.ZERO;
        int stt = 1;
        List<Object[]> rows = new ArrayList<>();
        for (ChiTietHoaDonDAO.Dong d : v.dong) {
            long sl = d.soLuong;
            long gia = d.donGia.longValue();
            model.addRow(new Object[] { stt++, d.tenMuc, sl, money(gia), money(sl * gia) });
            tamTinh = tamTinh.add(d.donGia.multiply(BigDecimal.valueOf(d.soLuong)));
        }

        // Giảm giá
        BigDecimal phanTram = v.phanTramGiam == null ? BigDecimal.ZERO : v.phanTramGiam;
        long giamGia = tamTinh.multiply(phanTram).divide(BigDecimal.valueOf(100))
                .setScale(0, RoundingMode.HALF_UP).longValue();
        long tamTinhLong = tamTinh.longValue();
        long tong = tamTinhLong - giamGia;

        lblTamTinh.setText(money(tamTinhLong));
        lblGiamGia.setText(giamGia > 0 ? "-" + money(giamGia) : money(0));
        lblMaGiamGia.setText(v.maKhuyenMai == null || v.maKhuyenMai.isEmpty() ? "—" : v.maKhuyenMai);
        lblTong.setText(money(tong));

        // Phương thức + tiền khách đưa
        boolean tienMat = v.tenPTTT != null && v.tenPTTT.toLowerCase().contains("tiền mặt");
        rdTienMat.setSelected(tienMat);
        rdQR.setSelected(!tienMat);

        if (tienMat) {
            long khachDua = v.tienKhachDua == null ? tong : v.tienKhachDua.longValue();
            lblKhachDua.setText(MONEY.format(khachDua) + "  đ");
            lblTienThua.setText(money(Math.max(0, khachDua - tong)));
        } else { // QR: không có tiền thừa
            lblKhachDua.setText(MONEY.format(tong) + "  đ");
            lblTienThua.setText(money(0));
        }
    }
}