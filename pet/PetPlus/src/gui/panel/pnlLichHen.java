package gui.panel;

import entity.LichHen;
import gui.dialogs.dlgLichHen;
import service.LichHenService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.DefaultTableCellRenderer;

import java.awt.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.DayOfWeek;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import utils.Auth;

public class pnlLichHen extends JPanel {

    private final LichHenService service = new LichHenService();

    private final JTextField txtTim = new JTextField(18);
    private final JButton btnLoc = new JButton("Lọc");
    private final JTextField txtNgay = new JTextField(9);
    private final JButton btnNgay = new JButton("Chọn");
    private final JComboBox<String> cboKhungGio = new JComboBox<>();

    private LocalDate ngayChon = LocalDate.now();

    private final DefaultTableModel model = new DefaultTableModel(
            new Object[] {
                    "Mã lịch hẹn",
                    "Thời gian",
                    "Trạng thái",
                    "Nhân viên",
                    "Mã KH",
                    "Khách hàng",
                    "Mã thú cưng",
                    "Tên thú cưng"
            }, 0) {
        @Override
        public boolean isCellEditable(int r, int c) {
            return false;
        }
    };

    private final JTable table = new JTable(model);

    private static final DateTimeFormatter F = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private static final DateTimeFormatter DATE_F = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public pnlLichHen() {

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JPanel top = new JPanel(
                new FlowLayout(FlowLayout.LEFT, 8, 8));

        top.setBackground(Color.WHITE);

        txtTim.setFont(new Font("Arial", Font.PLAIN, 14));

        txtNgay.setEditable(false);
        txtNgay.setText(ngayChon.format(DATE_F));
        txtNgay.setFont(new Font("Arial", Font.PLAIN, 13));
        txtNgay.setCursor(
                Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        btnNgay.setToolTipText("Chọn ngày trên lịch");
        cboKhungGio.setFont(new Font("Arial", Font.PLAIN, 13));

        // Các khung giờ lọc
        cboKhungGio.addItem("Tất cả giờ");

        for (int h = 0; h < 24; h++) {
            cboKhungGio.addItem(
                    String.format(
                            Locale.ROOT, "%02d:00–%02d:00",
                            h, (h + 1) % 24));

            cboKhungGio.addItem(
                    String.format(
                            Locale.ROOT, "%02d:00–%02d:30",
                            h, h));

            cboKhungGio.addItem(
                    String.format(
                            Locale.ROOT, "%02d:30–%02d:00",
                            h, (h + 1) % 24));
        }

        btnLoc.setBackground(Color.WHITE);

        JButton bTim = new JButton("Tìm");
        JButton lamMoi = new JButton("Làm mới");
        JButton bThem = new JButton("Thêm lịch hẹn");
        JButton bSua = new JButton("Sửa");
        JButton bXoa = new JButton("Xóa");
        boolean coQuyenCapNhat = Auth.hasFullAccess() || Auth.hasRole("BAN_HANG");
        bThem.setVisible(coQuyenCapNhat);
        bSua.setVisible(coQuyenCapNhat);
        bXoa.setVisible(coQuyenCapNhat);

        top.add(new JLabel("Tên khách hàng:"));
        top.add(txtTim);
        top.add(bTim);
        top.add(lamMoi);
        top.add(new JLabel("Khung giờ:"));
        top.add(cboKhungGio);

        top.add(new JLabel("Ngày:"));
        top.add(txtNgay);
        top.add(btnNgay);

        top.add(btnLoc);

        add(top, BorderLayout.NORTH);

        // Định dạng bảng
        table.setFont(new Font("Arial", Font.PLAIN, 13));
        table.setForeground(new Color(32, 44, 64));
        table.setBackground(Color.WHITE);
        table.setRowHeight(36);
        table.setGridColor(new Color(237, 217, 225));
        table.setShowGrid(true);
        table.setIntercellSpacing(new Dimension(1, 1));
        table.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION);
        table.setAutoCreateRowSorter(true);
        table.setFillsViewportHeight(true);
        table.setSelectionBackground(new Color(255, 236, 243));
        table.setSelectionForeground(new Color(32, 44, 64));

        JTableHeader th = table.getTableHeader();
        th.setFont(new Font("Arial", Font.BOLD, 13));
        th.setForeground(new Color(32, 44, 64));
        th.setBackground(new Color(255, 236, 243));
        th.setPreferredSize(new Dimension(0, 42));
        th.setReorderingAllowed(false);

        DefaultTableCellRenderer rr = new DefaultTableCellRenderer();

        rr.setBackground(Color.WHITE);
        rr.setForeground(new Color(32, 44, 64));
        rr.setBorder(
                BorderFactory.createMatteBorder(
                        0, 0, 1, 1, new Color(237, 217, 225)));
        rr.setVerticalAlignment(SwingConstants.CENTER);

        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel()
                    .getColumn(i)
                    .setCellRenderer(rr);
        }

        JScrollPane sp = new JScrollPane(table);
        sp.getViewport().setBackground(Color.WHITE);

        add(sp, BorderLayout.CENTER);
        add(sp, BorderLayout.CENTER);

        // Panel chứa các nút Thêm, Sửa, Xóa ở góc dưới bên phải
        JPanel pnlChucNang = new JPanel(
                new FlowLayout(FlowLayout.RIGHT, 10, 8));

        pnlChucNang.setBackground(Color.WHITE);

        pnlChucNang.add(bThem);
        pnlChucNang.add(bSua);
        pnlChucNang.add(bXoa);

        add(pnlChucNang, BorderLayout.SOUTH);
        // Nút Lọc: lọc theo ngày và khung giờ
        btnLoc.addActionListener(e -> tim());

        // Nút Tìm
        bTim.addActionListener(e -> tim());
        txtTim.addActionListener(e -> tim());

        // Chọn ngày
        btnNgay.addActionListener(e -> moLichChonNgay());
        txtNgay.addActionListener(e -> moLichChonNgay());

        txtNgay.addMouseListener(
                new java.awt.event.MouseAdapter() {
                    @Override
                    public void mouseClicked(
                            java.awt.event.MouseEvent e) {
                        moLichChonNgay();
                    }
                });

        // Đổi khung giờ không tự lọc.
        // Người dùng bấm nút Lọc để áp dụng điều kiện mới.

        // Tải lại
        lamMoi.addActionListener(e -> tai());

        // Tìm khách hàng khi nhập tên
        txtTim.getDocument().addDocumentListener(
                new javax.swing.event.DocumentListener() {

                    private void capNhat() {
                        SwingUtilities.invokeLater(() -> tim());
                    }

                    @Override
                    public void insertUpdate(
                            javax.swing.event.DocumentEvent e) {
                        capNhat();
                    }

                    @Override
                    public void removeUpdate(
                            javax.swing.event.DocumentEvent e) {
                        capNhat();
                    }

                    @Override
                    public void changedUpdate(
                            javax.swing.event.DocumentEvent e) {
                        capNhat();
                    }
                });

        // Thêm, sửa, xóa lịch hẹn
        bThem.addActionListener(e -> moDialog(null));

        bSua.addActionListener(e -> {
            LichHen l = chonDong();

            if (l != null) {
                moDialog(l);
            }
        });

        bXoa.addActionListener(e -> xoa());

        tai();

        utils.PetPlusTheme.apply(this);
    }

    /**
     * Lọc theo tên khách hàng, ngày và khung giờ.
     */
    private void tim() {

        try {
            List<LichHen> ds = service.timKiem(txtTim.getText());

            String khung = (String) cboKhungGio.getSelectedItem();

            LocalDate date = ngayChon;

            LocalDateTime tu = null;
            LocalDateTime den = null;

            if (khung != null
                    && !"Tất cả giờ".equals(khung)) {

                String[] parts = khung.split("[–-]");

                tu = date.atTime(
                        Integer.parseInt(
                                parts[0].substring(0, 2)),
                        Integer.parseInt(
                                parts[0].substring(3, 5)));

                den = date.atTime(
                        Integer.parseInt(
                                parts[1].substring(0, 2)),
                        Integer.parseInt(
                                parts[1].substring(3, 5)));

                // Trường hợp khung giờ đi qua nửa đêm
                if (!den.isAfter(tu)) {
                    den = den.plusDays(1);
                }
            }

            final LocalDateTime start = tu;
            final LocalDateTime end = den;

            ds.removeIf(l -> {

                // Phần quản lý lịch chỉ hiển thị ba trạng thái nghiệp vụ.
                // Các lịch đã tiếp nhận được theo dõi ở màn hình tiếp nhận.
                if (!entity.TrangThaiLichHen.laTrangThaiQuanLyHopLe(l.getTrangThai())) {
                    return true;
                }

                if (l.getThoiGianHen() == null) {
                    return true;
                }

                LocalDateTime thoiGian = l.getThoiGianHen();

                // Chỉ lấy lịch thuộc ngày đã chọn
                if (!thoiGian.toLocalDate().equals(date)) {
                    return true;
                }

                // Nếu chọn một khung giờ cụ thể
                // thì lấy thời gian từ đầu khung đến trước cuối khung
                return start != null
                        && (thoiGian.isBefore(start)
                                || !thoiGian.isBefore(end));
            });

            hienThi(ds);

        } catch (RuntimeException e) {
            baoLoi(e);
        }
    }

    private void moLichChonNgay() {

        JDialog dlg = new JDialog(
                SwingUtilities.getWindowAncestor(this),
                "Chọn ngày lọc lịch hẹn",
                Dialog.ModalityType.APPLICATION_MODAL);

        dlg.setLayout(new BorderLayout(8, 8));

        JPanel head = new JPanel(
                new BorderLayout(8, 0));

        JButton prev = new JButton("◀");
        JButton next = new JButton("▶");

        JLabel monthLabel = new JLabel(
                "",
                SwingConstants.CENTER);

        monthLabel.setFont(
                monthLabel.getFont().deriveFont(
                        Font.BOLD, 15f));

        head.add(prev, BorderLayout.WEST);
        head.add(monthLabel, BorderLayout.CENTER);
        head.add(next, BorderLayout.EAST);

        JPanel days = new JPanel(
                new GridLayout(0, 7, 3, 3));

        JPanel body = new JPanel(
                new BorderLayout(5, 5));

        body.setBorder(
                BorderFactory.createEmptyBorder(
                        10, 10, 10, 10));

        body.add(head, BorderLayout.NORTH);
        body.add(days, BorderLayout.CENTER);

        dlg.add(body, BorderLayout.CENTER);

        YearMonth[] shown = {
                YearMonth.from(ngayChon)
        };

        LocalDate[] selected = {
                ngayChon
        };

        Runnable[] render = new Runnable[1];

        render[0] = () -> {

            monthLabel.setText(
                    shown[0].getMonth().getDisplayName(
                            TextStyle.FULL,
                            new Locale("vi", "VN")) + " " + shown[0].getYear());

            days.removeAll();

            String[] weekdays = {
                    "T2", "T3", "T4",
                    "T5", "T6", "T7", "CN"
            };

            for (String w : weekdays) {
                days.add(
                        new JLabel(w, SwingConstants.CENTER));
            }

            int offset = shown[0].atDay(1)
                    .getDayOfWeek()
                    .getValue()
                    - DayOfWeek.MONDAY.getValue();

            for (int i = 0; i < offset; i++) {
                days.add(new JLabel(""));
            }

            for (int d = 1; d <= shown[0].lengthOfMonth(); d++) {

                LocalDate date = shown[0].atDay(d);

                JButton b = new JButton(
                        Integer.toString(d));

                b.setMargin(
                        new Insets(4, 4, 4, 4));

                if (date.equals(selected[0])) {
                    b.setBackground(
                            utils.PetPlusTheme.PRIMARY);

                    b.setForeground(Color.WHITE);
                }

                b.addActionListener(e -> {
                    selected[0] = date;
                    render[0].run();
                });

                days.add(b);
            }

            days.revalidate();
            days.repaint();
        };

        prev.addActionListener(e -> {
            shown[0] = shown[0].minusMonths(1);
            render[0].run();
        });

        next.addActionListener(e -> {
            shown[0] = shown[0].plusMonths(1);
            render[0].run();
        });

        JButton ok = new JButton("Chọn ngày");
        JButton cancel = new JButton("Hủy");

        JPanel actions = new JPanel(
                new FlowLayout(FlowLayout.RIGHT));

        actions.add(ok);
        actions.add(cancel);

        dlg.add(actions, BorderLayout.SOUTH);

        ok.addActionListener(e -> {
            ngayChon = selected[0];
            txtNgay.setText(
                    ngayChon.format(DATE_F));

            dlg.dispose();

            tim();
        });

        cancel.addActionListener(e -> dlg.dispose());

        render[0].run();

        dlg.pack();

        dlg.setMinimumSize(
                new Dimension(310, 300));

        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);

    }

    /**
     * Tải lại danh sách và đặt lại bộ lọc.
     */
    private void tai() {

        try {
            txtTim.setText("");

            ngayChon = LocalDate.now();

            txtNgay.setText(
                    ngayChon.format(DATE_F));

            cboKhungGio.setSelectedIndex(0);

            List<LichHen> ds = service.layDanhSach();
            ds.removeIf(l -> !entity.TrangThaiLichHen.laTrangThaiQuanLyHopLe(l.getTrangThai()));
            hienThi(ds);

        } catch (RuntimeException e) {
            baoLoi(e);
        }
    }

    /**
     * Hiển thị danh sách lịch hẹn lên bảng.
     */
    private void hienThi(List<LichHen> ds) {

        model.setRowCount(0);

        for (LichHen l : ds) {

            model.addRow(new Object[] {
                    l.getMaLichHen(),

                    l.getThoiGianHen() == null
                            ? ""
                            : l.getThoiGianHen().format(F),

                    l.getTrangThai(),

                    l.getMaNV() == null
                            ? ""
                            : l.getMaNV().getTenNV(),

                    l.getMaKH() == null
                            ? ""
                            : l.getMaKH().getMaKH(),

                    l.getMaKH() == null
                            ? ""
                            : l.getMaKH().getTenKH(),

                    l.getMaThuCung() == null
                            ? ""
                            : l.getMaThuCung().getMaThuCung(),

                    l.getMaThuCung() == null
                            ? ""
                            : l.getMaThuCung().getTenThuCung()
            });
        }
    }

    /**
     * Lấy lịch hẹn đang được chọn.
     */
    private LichHen chonDong() {

        int row = table.getSelectedRow();

        if (row < 0) {
            JOptionPane.showMessageDialog(
                    this,
                    "Vui lòng chọn một lịch hẹn.");

            return null;
        }

        String ma = model.getValueAt(
                table.convertRowIndexToModel(row), 0).toString();

        try {
            for (LichHen l : service.layDanhSach()) {

                if (l.getMaLichHen().equals(ma)) {
                    return l;
                }
            }

        } catch (RuntimeException e) {
            baoLoi(e);
        }

        return null;
    }

    /**
     * Mở dialog thêm hoặc sửa lịch hẹn.
     */
    private void moDialog(LichHen l) {

        Window w = SwingUtilities.getWindowAncestor(this);

        dlgLichHen d = new dlgLichHen(w, l);
        d.setVisible(true);

        // Chỉ tải lại khi thêm hoặc sửa thành công
        if (d.isSaved()) {
            tai();
        }
    }

    /**
     * Xóa lịch hẹn.
     */
    private void xoa() {

        LichHen l = chonDong();

        if (l == null) {
            return;
        }

        if (JOptionPane.showConfirmDialog(
                this,
                "Bạn có chắc muốn xóa lịch hẹn "
                        + l.getMaLichHen() + "?",
                "Xác nhận",
                JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            service.xoa(l.getMaLichHen());
            tim();

        } catch (RuntimeException e) {
            baoLoi(e);
        }
    }

    private void baoLoi(Exception e) {

        JOptionPane.showMessageDialog(
                this,
                e.getMessage(),
                "Lỗi",
                JOptionPane.ERROR_MESSAGE);
    }
}
