package gui.panel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

import appColor.AppColors;
import dao.XuatHoaDonDAO;
import dao.XuatHoaDonDAO.ChiTietRow;
import dao.XuatHoaDonDAO.HoaDonRow;

public class pnlXuatHoaDon extends JPanel {
    private static final long serialVersionUID = 1L;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DecimalFormat MONEY = new DecimalFormat("#,##0");

    private static final Font FONT = new Font("Segoe UI", Font.PLAIN, 14);
    private static final Font FONT_BOLD = new Font("Segoe UI", Font.BOLD, 14);
    private static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 26);
    private static final Font FONT_SECTION = new Font("Segoe UI", Font.BOLD, 16);
    private static final Color TEXT_MUTED = new Color(0x6B, 0x72, 0x80);
    private static final Color ROW_SELECTED = new Color(0xFB, 0xD0, 0xDD);

    private final XuatHoaDonDAO dao = new XuatHoaDonDAO();
    private final List<HoaDonRow> dsHienTai = new ArrayList<>();

    private JTextField txtTimKiem, txtTuNgay, txtDenNgay;
    private JButton btnTim, btnLamMoi, btnXuatFile, btnIn;
    private JTable tblHoaDon, tblChiTiet;
    private DefaultTableModel modelHoaDon, modelChiTiet;
    private JLabel lblSoHoaDon;
    private JLabel lblMaHD, lblNgay, lblNV, lblKH, lblPTTT, lblKM;
    private JLabel lblTong, lblGiam, lblThanhToan, lblKhachDua, lblTienThua;

    public pnlXuatHoaDon() {
        setLayout(new BorderLayout(0, 12));
        setBackground(AppColors.BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

        JPanel top = new JPanel();
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.setOpaque(false);
        top.add(taoTieuDe());
        top.add(Box.createVerticalStrut(12));
        top.add(taoPanelTimKiem());
        add(top, BorderLayout.NORTH);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, taoPanelDanhSach(), taoPanelChiTiet());
        split.setResizeWeight(0.5);
        split.setDividerSize(12);
        split.setBorder(null);
        split.setOpaque(false);
        split.setContinuousLayout(true);
        add(split, BorderLayout.CENTER);

        dangKySuKien();
        taiDanhSach(dao.timHoaDon("", LocalDate.now().minusMonths(1), LocalDate.now()));
    }

    // ================================================================ UI
    private JPanel taoTieuDe() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);
        p.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel title = new JLabel("XUẤT HÓA ĐƠN");
        title.setFont(FONT_TITLE);
        title.setForeground(AppColors.PRIMARY_DARK);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel sub = new JLabel("Tra cứu hóa đơn, xem chi tiết, xuất file hoặc in hóa đơn");
        sub.setFont(FONT);
        sub.setForeground(TEXT_MUTED);
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);

        p.add(title);
        p.add(Box.createVerticalStrut(2));
        p.add(sub);
        return p;
    }

    private JPanel taoPanelTimKiem() {
        RoundedPanel card = new RoundedPanel(new FlowLayout(FlowLayout.LEFT, 12, 12));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 66));

        txtTimKiem = taoTextField(18);
        txtTuNgay = taoTextField(9);
        txtDenNgay = taoTextField(9);
        txtTuNgay.setText(LocalDate.now().minusMonths(1).format(DATE_FMT));
        txtDenNgay.setText(LocalDate.now().format(DATE_FMT));
        txtTuNgay.setHorizontalAlignment(SwingConstants.CENTER);
        txtDenNgay.setHorizontalAlignment(SwingConstants.CENTER);

        btnTim = new RoundedButton("Tìm kiếm", true);
        btnLamMoi = new RoundedButton("Làm mới", false);

        card.add(taoNhan("Mã HĐ / Tên KH"));
        card.add(txtTimKiem);
        card.add(taoNhan("Từ ngày"));
        card.add(txtTuNgay);
        card.add(taoNhan("Đến ngày"));
        card.add(txtDenNgay);
        card.add(btnTim);
        card.add(btnLamMoi);
        return card;
    }

    private JPanel taoPanelDanhSach() {
        RoundedPanel card = new RoundedPanel(new BorderLayout(0, 8));
        card.setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));

        JPanel head = new JPanel(new BorderLayout());
        head.setOpaque(false);
        JLabel title = new JLabel("Danh sách hóa đơn");
        title.setFont(FONT_SECTION);
        title.setForeground(AppColors.PRIMARY_DARK);
        lblSoHoaDon = new JLabel("0 hóa đơn");
        lblSoHoaDon.setFont(FONT);
        lblSoHoaDon.setForeground(TEXT_MUTED);
        head.add(title, BorderLayout.WEST);
        head.add(lblSoHoaDon, BorderLayout.EAST);
        card.add(head, BorderLayout.NORTH);

        modelHoaDon = new DefaultTableModel(
                new String[] { "Mã HĐ", "Ngày lập", "Nhân viên", "Khách hàng", "PTTT", "Khuyến mãi", "Thanh toán" },
                0) {
            private static final long serialVersionUID = 1L;

            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        tblHoaDon = new JTable(modelHoaDon);
        tblHoaDon.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        styleTable(tblHoaDon, new int[] { 0, 1 }, new int[] { 6 });
        card.add(taoScroll(tblHoaDon), BorderLayout.CENTER);
        return card;
    }

    private JPanel taoPanelChiTiet() {
        RoundedPanel card = new RoundedPanel(new BorderLayout(0, 10));
        card.setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));

        // ---- phần đầu: tiêu đề + thông tin chung
        JPanel north = new JPanel(new BorderLayout(0, 8));
        north.setOpaque(false);
        JLabel title = new JLabel("Chi tiết hóa đơn");
        title.setFont(FONT_SECTION);
        title.setForeground(AppColors.PRIMARY_DARK);
        north.add(title, BorderLayout.NORTH);

        lblMaHD = taoGiaTri();
        lblNgay = taoGiaTri();
        lblNV = taoGiaTri();
        lblKH = taoGiaTri();
        lblPTTT = taoGiaTri();
        lblKM = taoGiaTri();

        JPanel info = new JPanel(new GridBagLayout());
        info.setBackground(AppColors.BACKGROUND);
        info.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppColors.BORDER),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)));
        themThongTin(info, 0, 0, "Mã hóa đơn", lblMaHD);
        themThongTin(info, 0, 1, "Ngày lập", lblNgay);
        themThongTin(info, 1, 0, "Nhân viên", lblNV);
        themThongTin(info, 1, 1, "Khách hàng", lblKH);
        themThongTin(info, 2, 0, "Thanh toán", lblPTTT);
        themThongTin(info, 2, 1, "Khuyến mãi", lblKM);
        north.add(info, BorderLayout.CENTER);
        card.add(north, BorderLayout.NORTH);

        // ---- bảng chi tiết
        modelChiTiet = new DefaultTableModel(
                new String[] { "STT", "Loại", "Tên sản phẩm / dịch vụ", "Số lượng", "Đơn giá", "Thành tiền" }, 0) {
            private static final long serialVersionUID = 1L;

            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        tblChiTiet = new JTable(modelChiTiet);
        styleTable(tblChiTiet, new int[] { 0, 1, 3 }, new int[] { 4, 5 });
        tblChiTiet.getColumnModel().getColumn(0).setMaxWidth(60);
        card.add(taoScroll(tblChiTiet), BorderLayout.CENTER);

        // ---- phần cuối: nút + tổng tiền
        JPanel south = new JPanel(new BorderLayout());
        south.setOpaque(false);

        btnXuatFile = new RoundedButton("Xuất file .txt", false);
        btnIn = new RoundedButton("In hóa đơn", true);
        JPanel nut = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        nut.setOpaque(false);
        nut.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        nut.add(btnXuatFile);
        nut.add(btnIn);
        south.add(nut, BorderLayout.SOUTH);

        lblTong = taoSo(FONT_BOLD, AppColors.TEXT);
        lblGiam = taoSo(FONT_BOLD, AppColors.TEXT);
        lblThanhToan = taoSo(new Font("Segoe UI", Font.BOLD, 20), AppColors.PRIMARY_DARK);
        lblKhachDua = taoSo(FONT_BOLD, AppColors.TEXT);
        lblTienThua = taoSo(FONT_BOLD, AppColors.TEXT);

        JPanel tong = new JPanel(new GridBagLayout());
        tong.setBackground(AppColors.SIDEBAR);
        tong.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppColors.PRIMARY, 1),
                BorderFactory.createEmptyBorder(10, 16, 10, 16)));
        themDongTong(tong, 0, "Tổng tiền", lblTong);
        themDongTong(tong, 1, "Giảm giá", lblGiam);
        themDongTong(tong, 2, "Khách cần trả", lblThanhToan);
        themDongTong(tong, 3, "Tiền khách đưa", lblKhachDua);
        themDongTong(tong, 4, "Tiền thừa", lblTienThua);
        south.add(tong, BorderLayout.CENTER);

        card.add(south, BorderLayout.SOUTH);
        return card;
    }

    // ------------------------------------------------------- UI helpers
    private JTextField taoTextField(int cols) {
        JTextField t = new JTextField(cols);
        t.setFont(FONT);
        t.setForeground(AppColors.TEXT);
        t.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppColors.BORDER),
                BorderFactory.createEmptyBorder(6, 8, 6, 8)));
        return t;
    }

    private JLabel taoNhan(String text) {
        JLabel l = new JLabel(text);
        l.setFont(FONT_BOLD);
        l.setForeground(AppColors.TEXT);
        return l;
    }

    private JLabel taoGiaTri() {
        JLabel l = new JLabel("-");
        l.setFont(FONT_BOLD);
        l.setForeground(AppColors.TEXT);
        return l;
    }

    private JLabel taoSo(Font f, Color c) {
        JLabel l = new JLabel("0 đ", SwingConstants.RIGHT);
        l.setFont(f);
        l.setForeground(c);
        return l;
    }

    private void themThongTin(JPanel p, int row, int col, String caption, JLabel value) {
        JLabel cap = new JLabel(caption);
        cap.setFont(FONT);
        cap.setForeground(TEXT_MUTED);

        GridBagConstraints g = new GridBagConstraints();
        g.gridy = row;
        g.insets = new Insets(4, 4, 4, 12);
        g.anchor = GridBagConstraints.WEST;

        g.gridx = col * 2;
        g.weightx = 0;
        g.ipadx = 0;
        p.add(cap, g);

        g.gridx = col * 2 + 1;
        g.weightx = 1;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(4, 0, 4, 24);
        p.add(value, g);
    }

    private void themDongTong(JPanel p, int row, String caption, JLabel value) {
        JLabel cap = new JLabel(caption);
        cap.setFont(FONT);
        cap.setForeground(TEXT_MUTED);

        GridBagConstraints g = new GridBagConstraints();
        g.gridy = row;
        g.insets = new Insets(3, 0, 3, 24);
        g.anchor = GridBagConstraints.WEST;
        g.gridx = 0;
        p.add(cap, g);

        g.gridx = 1;
        g.weightx = 1;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(3, 0, 3, 0);
        p.add(value, g);
    }

    private JScrollPane taoScroll(JTable t) {
        JScrollPane sp = new JScrollPane(t);
        sp.setBorder(BorderFactory.createLineBorder(AppColors.BORDER));
        sp.getViewport().setBackground(AppColors.WHITE);
        return sp;
    }

    /** Áp dụng style chung cho bảng: header hồng, xen kẽ dòng, canh lề. */
    private void styleTable(JTable t, int[] centerCols, int[] rightCols) {
        t.setFont(FONT);
        t.setForeground(AppColors.TEXT);
        t.setRowHeight(32);
        t.setShowGrid(false);
        t.setShowHorizontalLines(true);
        t.setGridColor(new Color(0xEE, 0xEE, 0xF0));
        t.setIntercellSpacing(new Dimension(0, 1));
        t.setSelectionBackground(ROW_SELECTED);
        t.setSelectionForeground(AppColors.TEXT);
        t.setFillsViewportHeight(true);

        CellRenderer cell = new CellRenderer(centerCols, rightCols);
        for (int i = 0; i < t.getColumnCount(); i++) {
            t.getColumnModel().getColumn(i).setCellRenderer(cell);
        }

        JTableHeader h = t.getTableHeader();
        h.setReorderingAllowed(false);
        h.setPreferredSize(new Dimension(0, 36));
        h.setDefaultRenderer(new HeaderRenderer(centerCols, rightCols));
    }

    // ----------------------------------------------- Custom components
    private static class RoundedPanel extends JPanel {
        private static final long serialVersionUID = 1L;

        RoundedPanel(java.awt.LayoutManager lm) {
            super(lm);
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(AppColors.WHITE);
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
            g2.setColor(AppColors.BORDER);
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    private static class RoundedButton extends JButton {
        private static final long serialVersionUID = 1L;
        private final boolean filled;
        private boolean hover;

        RoundedButton(String text, boolean filled) {
            super(text);
            this.filled = filled;
            setFont(FONT_BOLD);
            setForeground(filled ? AppColors.WHITE : AppColors.PRIMARY_DARK);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setBorder(BorderFactory.createEmptyBorder(9, 20, 9, 20));
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    hover = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    hover = false;
                    repaint();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight();
            if (filled) {
                g2.setColor(hover ? AppColors.PRIMARY_DARK : AppColors.PRIMARY);
                g2.fillRoundRect(0, 0, w - 1, h - 1, 12, 12);
            } else {
                g2.setColor(hover ? AppColors.SIDEBAR : AppColors.WHITE);
                g2.fillRoundRect(0, 0, w - 1, h - 1, 12, 12);
                g2.setColor(AppColors.PRIMARY);
                g2.drawRoundRect(0, 0, w - 1, h - 1, 12, 12);
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }

    private static int align(int col, int[] center, int[] right) {
        for (int c : right) {
            if (c == col) {
                return SwingConstants.RIGHT;
            }
        }
        for (int c : center) {
            if (c == col) {
                return SwingConstants.CENTER;
            }
        }
        return SwingConstants.LEFT;
    }

    private static class CellRenderer extends DefaultTableCellRenderer {
        private static final long serialVersionUID = 1L;
        private final int[] center, right;

        CellRenderer(int[] center, int[] right) {
            this.center = center;
            this.right = right;
        }

        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean focus, int row,
                int col) {
            super.getTableCellRendererComponent(t, v, sel, false, row, col);
            setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
            setHorizontalAlignment(align(col, center, right));
            setForeground(AppColors.TEXT);
            if (sel) {
                setBackground(ROW_SELECTED);
            } else {
                setBackground(row % 2 == 0 ? AppColors.WHITE : AppColors.BACKGROUND);
            }
            return this;
        }
    }

    private static class HeaderRenderer extends DefaultTableCellRenderer {
        private static final long serialVersionUID = 1L;
        private final int[] center, right;

        HeaderRenderer(int[] center, int[] right) {
            this.center = center;
            this.right = right;
        }

        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean focus, int row,
                int col) {
            super.getTableCellRendererComponent(t, v, false, false, row, col);
            setOpaque(true);
            setBackground(AppColors.TABLE_HEADER);
            setForeground(AppColors.PRIMARY_DARK);
            setFont(FONT_BOLD);
            setHorizontalAlignment(align(col, center, right));
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 2, 0, AppColors.PRIMARY),
                    BorderFactory.createEmptyBorder(0, 10, 0, 10)));
            return this;
        }
    }

    // ============================================================ Sự kiện
    private void dangKySuKien() {
        btnTim.addActionListener(e -> timKiem());
        txtTimKiem.addActionListener(e -> timKiem());
        btnLamMoi.addActionListener(e -> {
            txtTimKiem.setText("");
            txtTuNgay.setText(LocalDate.now().minusMonths(1).format(DATE_FMT));
            txtDenNgay.setText(LocalDate.now().format(DATE_FMT));
            taiDanhSach(dao.timHoaDon("", LocalDate.now().minusMonths(1), LocalDate.now()));
        });
        tblHoaDon.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                hienThiChiTiet(tblHoaDon.getSelectedRow());
            }
        });
        btnXuatFile.addActionListener(e -> xuatFile());
        btnIn.addActionListener(e -> inHoaDon());
    }

    private void timKiem() {
        try {
            LocalDate tu = LocalDate.parse(txtTuNgay.getText().trim(), DATE_FMT);
            LocalDate den = LocalDate.parse(txtDenNgay.getText().trim(), DATE_FMT);
            if (tu.isAfter(den)) {
                JOptionPane.showMessageDialog(this, "Từ ngày phải nhỏ hơn hoặc bằng Đến ngày.");
                return;
            }
            taiDanhSach(dao.timHoaDon(txtTimKiem.getText(), tu, den));
        } catch (java.time.format.DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this, "Ngày không hợp lệ, định dạng đúng: dd/MM/yyyy");
        }
    }

    // =========================================================== Hiển thị
    private void taiDanhSach(List<HoaDonRow> ds) {
        dsHienTai.clear();
        dsHienTai.addAll(ds);
        modelHoaDon.setRowCount(0);
        for (HoaDonRow r : ds) {
            modelHoaDon.addRow(new Object[] { r.maHoaDon, r.ngayLap == null ? "" : r.ngayLap.format(DATE_FMT),
                    r.tenNV, r.tenKH, r.tenPTTT, tenKhuyenMaiHienThi(r),
                    MONEY.format(r.thanhToan()) + " đ" });
        }
        lblSoHoaDon.setText(ds.size() + " hóa đơn");
        xoaChiTiet();
        if (!ds.isEmpty()) {
            tblHoaDon.setRowSelectionInterval(0, 0);
        }
    }

    private void xoaChiTiet() {
        modelChiTiet.setRowCount(0);
        for (JLabel l : new JLabel[] { lblMaHD, lblNgay, lblNV, lblKH, lblPTTT, lblKM }) {
            l.setText("-");
        }
        for (JLabel l : new JLabel[] { lblTong, lblGiam, lblThanhToan, lblKhachDua, lblTienThua }) {
            l.setText("0 đ");
        }
    }

    private void hienThiChiTiet(int row) {
        if (row < 0 || row >= dsHienTai.size()) {
            xoaChiTiet();
            return;
        }
        HoaDonRow hd = dsHienTai.get(row);
        lblMaHD.setText(hd.maHoaDon);
        lblNgay.setText(hd.ngayLap == null ? "" : hd.ngayLap.format(DATE_FMT));
        lblNV.setText(nz(hd.tenNV));
        lblKH.setText(nz(hd.tenKH));
        lblPTTT.setText(nz(hd.tenPTTT));
        lblKM.setText(tenKhuyenMaiHienThi(hd));

        modelChiTiet.setRowCount(0);
        int stt = 1;
        for (ChiTietRow c : dao.getChiTiet(hd.maHoaDon)) {
            modelChiTiet.addRow(new Object[] { stt++, c.loai, c.ten, c.soLuong, MONEY.format(c.donGia) + " đ",
                    MONEY.format(c.thanhTien()) + " đ" });
        }
        lblTong.setText(MONEY.format(hd.tongTien) + " đ");
        lblGiam.setText(MONEY.format(hd.tienGiam()) + " đ");
        lblThanhToan.setText(MONEY.format(hd.thanhToan()) + " đ");
        lblKhachDua.setText(MONEY.format(hd.tienKhachDua) + " đ");
        lblTienThua.setText(MONEY.format(hd.tienThua().max(java.math.BigDecimal.ZERO)) + " đ");
    }

    private String tenKhuyenMaiHienThi(HoaDonRow hd) {
        StringBuilder ten = new StringBuilder();
        if (hd.tenKhuyenMai != null) {
            ten.append(hd.tenKhuyenMai).append(" (").append(hd.phanTramGiam).append("%)");
        }
        if (hd.tenKhuyenMaiSanPham != null) {
            if (ten.length() > 0) ten.append(" / ");
            ten.append(hd.tenKhuyenMaiSanPham).append(" (")
                    .append(hd.phanTramGiamSanPham).append("% sản phẩm)");
        }
        return ten.length() == 0 ? "Không có" : ten.toString();
    }

    // ========================================================== Xuất / In
    private HoaDonRow hoaDonDangChon() {
        int row = tblHoaDon.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một hóa đơn.");
            return null;
        }
        return dsHienTai.get(row);
    }

    private String taoNoiDungHoaDon(HoaDonRow hd) {
        StringBuilder sb = new StringBuilder();
        String line = "=".repeat(60);
        sb.append(center("CỦA HÀNG THÚ CƯNG PETPLUS", 70)).append('\n');
        sb.append(center("Địa chỉ: 650 Lê Văn Thọ, Quận Gò Vấp, TP. HCM", 40)).append('\n');
        sb.append(line).append('\n');
        sb.append(center("HÓA ĐƠN BÁN HÀNG", 60)).append('\n');
        sb.append(line).append('\n');
        sb.append("Mã hóa đơn : ").append(hd.maHoaDon).append('\n');
        sb.append("Ngày lập   : ").append(hd.ngayLap == null ? "" : hd.ngayLap.format(DATE_FMT)).append('\n');
        sb.append("Nhân viên  : ").append(nz(hd.tenNV)).append('\n');
        sb.append("Khách hàng : ").append(nz(hd.tenKH)).append('\n');
        sb.append("PT thanh toán: ").append(nz(hd.tenPTTT)).append('\n');
        sb.append("-".repeat(60)).append('\n');
        sb.append(String.format("%-4s %-24s %5s %12s %12s%n", "STT", "Tên", "SL", "Đơn giá", "Thành tiền"));
        int stt = 1;
        for (ChiTietRow c : dao.getChiTiet(hd.maHoaDon)) {
            String ten = nz(c.ten);
            if (ten.length() > 24) {
                ten = ten.substring(0, 21) + "...";
            }
            sb.append(String.format("%-4d %-24s %5d %12s %12s%n", stt++, ten, c.soLuong, MONEY.format(c.donGia),
                    MONEY.format(c.thanhTien())));
        }
        sb.append("-".repeat(60)).append('\n');
        sb.append(String.format("%-40s %19s%n", "Tổng tiền:", MONEY.format(hd.tongTien)));
        sb.append(String.format("%-40s %19s%n", "Tổng giảm khuyến mãi:", MONEY.format(hd.tienGiam())));
        sb.append(String.format("%-40s %19s%n", "Khách cần trả:", MONEY.format(hd.thanhToan())));
        sb.append(String.format("%-40s %19s%n", "Tiền khách đưa:", MONEY.format(hd.tienKhachDua)));
        sb.append(String.format("%-40s %19s%n", "Tiền thừa:",
                MONEY.format(hd.tienThua().max(java.math.BigDecimal.ZERO))));
        sb.append(line).append('\n');
        sb.append(center("Cảm ơn quý khách!", 60)).append('\n');
        return sb.toString();
    }

    private void xuatFile() {
        HoaDonRow hd = hoaDonDangChon();
        if (hd == null) {
            return;
        }
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new File("HoaDon_" + hd.maHoaDon + ".txt"));
        if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                Files.write(fc.getSelectedFile().toPath(), taoNoiDungHoaDon(hd).getBytes(StandardCharsets.UTF_8));
                JOptionPane.showMessageDialog(this, "Đã xuất hóa đơn thành công.");
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Lỗi ghi file: " + ex.getMessage());
            }
        }
    }

    private void inHoaDon() {
        HoaDonRow hd = hoaDonDangChon();
        if (hd == null) {
            return;
        }
        JTextArea area = new JTextArea(taoNoiDungHoaDon(hd));
        area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 11));
        try {
            area.print();
        } catch (java.awt.print.PrinterException ex) {
            JOptionPane.showMessageDialog(this, "Lỗi in: " + ex.getMessage());
        }
    }

    // ============================================================ Tiện ích
    private static String nz(String s) {
        return s == null ? "" : s;
    }

    private static String center(String s, int width) {
        int pad = Math.max(0, (width - s.length()) / 2);
        return " ".repeat(pad) + s;
    }
}
