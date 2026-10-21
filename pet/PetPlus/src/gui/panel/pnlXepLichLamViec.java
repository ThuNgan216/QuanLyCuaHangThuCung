package gui.panel;

import dao.LichLamViecDAO;
import dao.LichLamViecDAO.LichItem;
import dao.LichLamViecDAO.NhanVienItem;
import entity.CaLamViec;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;


public class pnlXepLichLamViec extends JPanel {
    private static final Color PRIMARY = new Color(245, 70, 113);
    private static final Color DARK = new Color(220, 27, 86);
    private static final Color BG = new Color(255, 247, 250);
    private static final Color LIGHT = new Color(255, 236, 243);
    private static final Color TEXT = new Color(32, 44, 64);
    private static final Color MUTED = new Color(118, 126, 140);
    private static final Color BORDER = new Color(237, 217, 225);
    private static final DateTimeFormatter DATE = DateTimeFormatter
            .ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT);
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm");

    private final LichLamViecDAO dao = new LichLamViecDAO();
    private final DefaultTableModel model = new DefaultTableModel(
            new String[]{"Mã lịch", "Ngày", "Nhân viên", "Ca làm việc", "Bắt đầu", "Kết thúc"}, 0) {
        @Override public boolean isCellEditable(int row, int column) { return false; }
    };
    private final JTable tblLich = new JTable(model);
    private final JLabel lblTuan = new JLabel();
    private final JLabel lblTongLich = thongKe("0", "Lịch đã xếp");
    private final JLabel lblNhanVien = thongKe("0", "Nhân viên có lịch");
    private final JLabel lblCa = thongKe("0", "Loại ca sử dụng");
    private LocalDate dauTuan = LocalDate.now().with(DayOfWeek.MONDAY);
    private List<LichItem> duLieu = new ArrayList<LichItem>();
    private Runnable onChanged = new Runnable() { @Override public void run() {} };
    private int phienTai = 0;

    public pnlXepLichLamViec() {
        setLayout(new BorderLayout(0, 18));
        setBackground(BG);
        setBorder(new EmptyBorder(22, 24, 22, 24));

        JPanel heading = new JPanel(new BorderLayout(0, 13));
        heading.setOpaque(false);
        JLabel title = nhan("QUẢN LÝ LỊCH LÀM VIỆC", 24, TEXT, true);
        heading.add(title, BorderLayout.NORTH);
        JPanel thanhCongCu = new JPanel(new FlowLayout(FlowLayout.LEFT, 9, 3));
        thanhCongCu.setOpaque(false);
        JButton prev = nut("← Tuần trước", false);
        JButton next = nut("Tuần sau →", false);
        JButton current = nut("Tuần này", false);
        JButton refresh = nut("Làm mới", false);
        JButton add = nut("+ Thêm lịch", true);
        JButton edit = nut("Sửa lịch", false);
        JButton delete = nut("Xóa lịch", false);
        prev.addActionListener(e -> { dauTuan = dauTuan.minusWeeks(1); taiLich(); });
        next.addActionListener(e -> { dauTuan = dauTuan.plusWeeks(1); taiLich(); });
        current.addActionListener(e -> {
            dauTuan = LocalDate.now().with(DayOfWeek.MONDAY); taiLich();
        });
        refresh.addActionListener(e -> lamMoi());
        add.addActionListener(e -> moForm(null));
        edit.addActionListener(e -> {
            LichItem lich = lichDangChon();
            if (lich != null) moForm(lich);
        });
        delete.addActionListener(e -> xoaLich());
        thanhCongCu.add(prev);
        lblTuan.setFont(new Font("Arial", Font.BOLD, 13));
        lblTuan.setForeground(TEXT);
        thanhCongCu.add(lblTuan);
        thanhCongCu.add(next);
        thanhCongCu.add(current);
        thanhCongCu.add(refresh);
        thanhCongCu.add(add);
        thanhCongCu.add(edit);
        thanhCongCu.add(delete);
        heading.add(thanhCongCu, BorderLayout.CENTER);
        JPanel separator = new JPanel();
        separator.setBackground(PRIMARY);
        separator.setPreferredSize(new Dimension(0, 2));
        heading.add(separator, BorderLayout.SOUTH);
        add(heading, BorderLayout.NORTH);

        JPanel main = new JPanel(new BorderLayout(0, 17));
        main.setOpaque(false);
        JPanel stats = new JPanel(new GridLayout(1, 3, 14, 0));
        stats.setOpaque(false);
        stats.add(theThongKe(lblTongLich));
        stats.add(theThongKe(lblNhanVien));
        stats.add(theThongKe(lblCa));
        main.add(stats, BorderLayout.NORTH);

        JPanel tableBox = new JPanel(new BorderLayout(0, 12));
        tableBox.setBackground(Color.WHITE);
        tableBox.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER), new EmptyBorder(14, 14, 14, 14)));
        JLabel subTitle = nhan("Danh sách lịch làm việc trong tuần", 17, TEXT, true);
        tableBox.add(subTitle, BorderLayout.NORTH);
        chinhBang();
        JScrollPane scroll = new JScrollPane(tblLich);
        scroll.setBorder(BorderFactory.createLineBorder(BORDER));
        scroll.getViewport().setBackground(Color.WHITE);
        tableBox.add(scroll, BorderLayout.CENTER);
        main.add(tableBox, BorderLayout.CENTER);
        add(main, BorderLayout.CENTER);
        taiLich();
    }

    public void setOnChanged(Runnable callback) {
        onChanged = callback == null ? new Runnable() { @Override public void run() {} } : callback;
    }

    public void lamMoi() { taiLich(); }

    public LocalDate getDauTuan() { return dauTuan; }

    private static JLabel nhan(String title, int size, Color color, boolean bold) {
        JLabel label = new JLabel(title);
        label.setFont(new Font("Arial", bold ? Font.BOLD : Font.PLAIN, size));
        label.setForeground(color);
        return label;
    }

    private static JLabel thongKe(String number, String label) {
        JLabel text = nhan(number, 28, TEXT, true);
        text.putClientProperty("subtitle", label);
        return text;
    }

    private JPanel theThongKe(JLabel counter) {
        JPanel card = new JPanel(new BorderLayout(0, 8));
        card.setBackground(LIGHT);
        card.setBorder(new EmptyBorder(17, 22, 17, 16));
        JLabel title = nhan((String) counter.getClientProperty("subtitle"), 13, DARK, true);
        card.add(title, BorderLayout.NORTH);
        card.add(counter, BorderLayout.CENTER);
        return card;
    }

    private void chinhBang() {
        tblLich.setFont(new Font("Arial", Font.PLAIN, 13));
        tblLich.setForeground(TEXT);
        tblLich.setRowHeight(40);
        tblLich.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblLich.setSelectionBackground(new Color(255, 211, 227));
        tblLich.setGridColor(BORDER);
        tblLich.setShowVerticalLines(false);
        tblLich.setAutoCreateRowSorter(true);
        tblLich.getTableHeader().setPreferredSize(new Dimension(0, 43));
        tblLich.getTableHeader().setFont(new Font("Arial", Font.BOLD, 13));
        tblLich.getTableHeader().setBackground(LIGHT);
        tblLich.getTableHeader().setForeground(TEXT);
        DefaultTableCellRenderer align = new DefaultTableCellRenderer();
        align.setHorizontalAlignment(SwingConstants.CENTER);
        tblLich.getColumnModel().getColumn(0).setCellRenderer(align);
        tblLich.getColumnModel().getColumn(1).setCellRenderer(align);
        tblLich.getColumnModel().getColumn(4).setCellRenderer(align);
        tblLich.getColumnModel().getColumn(5).setCellRenderer(align);
        tblLich.getColumnModel().getColumn(2).setPreferredWidth(180);
        tblLich.getColumnModel().getColumn(3).setPreferredWidth(170);
    }

    private static JButton nut(String text, boolean chinh) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Arial", Font.BOLD, 12));
        btn.setBackground(chinh ? PRIMARY : Color.WHITE);
        btn.setForeground(chinh ? Color.WHITE : TEXT);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(chinh ? PRIMARY : BORDER),
                new EmptyBorder(10, 12, 10, 12)));
        return btn;
    }

    private static void trangTriInput(JComponent component) {
        component.setFont(new Font("Arial", Font.PLAIN, 13));
        component.setForeground(TEXT);
        component.setBackground(Color.WHITE);
        component.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER), new EmptyBorder(9, 10, 9, 10)));
        component.setPreferredSize(new Dimension(360, 42));
    }


    private void taiLich() {
        final LocalDate begin = dauTuan;
        final LocalDate end = begin.plusDays(6);
        lblTuan.setText("  " + begin.format(DATE) + " – " + end.format(DATE) + "  ");
        final int request = ++phienTai;
        new SwingWorker<List<LichItem>, Void>() {
            @Override protected List<LichItem> doInBackground() throws Exception {
                return dao.layLich(begin, end);
            }
            @Override protected void done() {
                if (request != phienTai) return;
                try {
                    duLieu = get();
                    model.setRowCount(0);
                    Set<String> nhanVien = new HashSet<String>();
                    Set<String> ca = new HashSet<String>();
                    for (LichItem l : duLieu) {
                        nhanVien.add(l.maNV);
                        ca.add(l.ca.getMaCa());
                        model.addRow(new Object[]{l.maLichLamViec,
                                l.ngayLamViec.format(DATE), l.tenNV, l.ca.getTenCa(),
                                l.ca.getGioBatDau().format(TIME),
                                l.ca.getGioKetThuc().format(TIME)});
                    }
                    lblTongLich.setText(String.valueOf(duLieu.size()));
                    lblNhanVien.setText(String.valueOf(nhanVien.size()));
                    lblCa.setText(String.valueOf(ca.size()));
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                } catch (ExecutionException ex) { baoLoi(ex.getCause()); }
            }
        }.execute();
    }

    private LichItem lichDangChon() {
        int index = tblLich.getSelectedRow();
        if (index < 0) {
            JOptionPane.showMessageDialog(this, "Hãy chọn một lịch trong bảng trước khi thao tác.");
            return null;
        }
        int modelIndex = tblLich.convertRowIndexToModel(index);
        String maLich = (String) model.getValueAt(modelIndex, 0);
        for (LichItem item : duLieu) {
            if (item.maLichLamViec.equals(maLich)) return item;
        }
        JOptionPane.showMessageDialog(this, "Lịch đã thay đổi. Hãy nhấn Làm mới.");
        return null;
    }

    private static final class FormData {
        final List<NhanVienItem> nhanVien;
        final List<CaLamViec> ca;
        FormData(List<NhanVienItem> nhanVien, List<CaLamViec> ca) {
            this.nhanVien = nhanVien; this.ca = ca;
        }
    }

    private void moForm(final LichItem cu) {
        new SwingWorker<FormData, Void>() {
            @Override protected FormData doInBackground() throws Exception {
                return new FormData(dao.layDanhSachNhanVien(), dao.layDanhSachCa());
            }
            @Override protected void done() {
                try {
                    FormData data = get();
                    if (data.nhanVien.isEmpty() || data.ca.isEmpty()) {
                        JOptionPane.showMessageDialog(pnlXepLichLamViec.this,
                                "Chưa có nhân viên đang hoạt động hoặc ca làm việc trong SQL Server.\n"
                              + "Hãy thêm nhân viên và ít nhất một bản ghi CaLamViec trước.",
                                "Thiếu dữ liệu", JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                    hienDialog(cu, data);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                } catch (ExecutionException ex) { baoLoi(ex.getCause()); }
            }
        }.execute();
    }

    private void hienDialog(final LichItem cu, FormData danhMuc) {
        Window owner = SwingUtilities.getWindowAncestor(this);
        final JDialog dialog = new JDialog(owner,
                cu == null ? "Thêm lịch làm việc" : "Cập nhật lịch làm việc",
                Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        dialog.setSize(580, 525);
        dialog.setLocationRelativeTo(this);
        dialog.getContentPane().setLayout(new BorderLayout());
        dialog.getContentPane().setBackground(Color.WHITE);

        JPanel banner = new JPanel(new BorderLayout());
        banner.setBackground(LIGHT);
        banner.setBorder(new EmptyBorder(19, 28, 19, 28));
        JLabel header = nhan(cu == null ? "THÊM LỊCH LÀM VIỆC" : "CẬP NHẬT LỊCH LÀM VIỆC",
                20, DARK, true);
        banner.add(header, BorderLayout.CENTER);
        dialog.add(banner, BorderLayout.NORTH);

        JPanel fields = new JPanel(new GridBagLayout());
        fields.setBackground(Color.WHITE);
        fields.setBorder(new EmptyBorder(21, 32, 20, 32));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 8, 0);

        final JComboBox<NhanVienItem> cboNV = new JComboBox<NhanVienItem>();
        for (NhanVienItem nv : danhMuc.nhanVien) cboNV.addItem(nv);
        trangTriInput(cboNV);
        final JTextField txtNgay = new JTextField(
                (cu == null ? LocalDate.now() : cu.ngayLamViec).format(DATE));
        trangTriInput(txtNgay);
        final JComboBox<CaLamViec> cboCa = new JComboBox<CaLamViec>();
        for (CaLamViec ca : danhMuc.ca) cboCa.addItem(ca);
        cboCa.setRenderer(new DefaultListCellRenderer() {
            @Override public Component getListCellRendererComponent(JList<?> list, Object value,
                    int index, boolean selected, boolean focus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(
                        list, value, index, selected, focus);
                if (value instanceof CaLamViec) {
                    CaLamViec ca = (CaLamViec) value;
                    label.setText(ca.getTenCa() + "  (" + ca.getGioBatDau().format(TIME)
                            + " - " + ca.getGioKetThuc().format(TIME) + ")");
                }
                return label;
            }
        });
        trangTriInput(cboCa);

        if (cu != null) {
            for (int i = 0; i < cboNV.getItemCount(); i++) {
                if (cboNV.getItemAt(i).maNV.equals(cu.maNV)) cboNV.setSelectedIndex(i);
            }
            for (int i = 0; i < cboCa.getItemCount(); i++) {
                if (cboCa.getItemAt(i).getMaCa().equals(cu.ca.getMaCa())) cboCa.setSelectedIndex(i);
            }
        }

        int row = 0;
        row = themTruong(fields, gbc, row, "Nhân viên", cboNV);
        row = themTruong(fields, gbc, row, "Ngày làm việc (dd/MM/yyyy)", txtNgay);
        row = themTruong(fields, gbc, row, "Ca làm việc", cboCa);
        gbc.gridy = row; gbc.insets = new Insets(0, 0, 5, 0);
    
        dialog.add(fields, BorderLayout.CENTER);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 18));
        footer.setBackground(Color.WHITE);
        footer.setBorder(new EmptyBorder(0, 22, 10, 22));
        JButton huy = nut("Hủy", false);
        JButton luu = nut(cu == null ? "Lưu lịch" : "Lưu thay đổi", true);
        huy.addActionListener(e -> dialog.dispose());
        luu.addActionListener(e -> {
            final LocalDate ngay;
            try { ngay = LocalDate.parse(txtNgay.getText().trim(), DATE); }
            catch (DateTimeParseException ex) {
                JOptionPane.showMessageDialog(dialog,
                        "Ngày không hợp lệ. Hãy dùng dd/MM/yyyy (ví dụ 22/09/2026).",
                        "Sai ngày", JOptionPane.WARNING_MESSAGE);
                return;
            }
            final NhanVienItem nhanVien = (NhanVienItem) cboNV.getSelectedItem();
            final CaLamViec ca = (CaLamViec) cboCa.getSelectedItem();
            if (nhanVien == null || ca == null) {
                JOptionPane.showMessageDialog(dialog, "Vui lòng chọn nhân viên và ca làm việc.");
                return;
            }
            dialog.dispose();
            luuDuLieu(cu, ngay, nhanVien, ca);
        });
        footer.add(huy);
        footer.add(luu);
        dialog.add(footer, BorderLayout.SOUTH);
        dialog.getRootPane().setDefaultButton(luu);
        dialog.setVisible(true);
    }

    private int themTruong(JPanel fields, GridBagConstraints gbc, int row,
                          String ten, JComponent input) {
        gbc.gridy = row++;
        gbc.insets = new Insets(0, 0, 5, 0);
        fields.add(nhan(ten, 13, TEXT, true), gbc);
        gbc.gridy = row++;
        gbc.insets = new Insets(0, 0, 16, 0);
        fields.add(input, gbc);
        return row;
    }

    private void luuDuLieu(final LichItem cu, final LocalDate ngay,
                          final NhanVienItem nhanVien, final CaLamViec ca) {
        new SwingWorker<Void, Void>() {
            @Override protected Void doInBackground() throws Exception {
                if (cu == null) dao.themLich(ngay, nhanVien.maNV, ca.getMaCa());
                else dao.capNhatLich(cu.maLichLamViec, ngay, nhanVien.maNV, ca.getMaCa());
                return null;
            }
            @Override protected void done() {
                try {
                    get();
                    dauTuan = ngay.with(DayOfWeek.MONDAY);
                    taiLich();
                    onChanged.run();
                    JOptionPane.showMessageDialog(pnlXepLichLamViec.this,
                            "Đã lưu lịch làm việc cho " + nhanVien.tenNV + ".");
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                } catch (ExecutionException ex) { baoLoi(ex.getCause()); }
            }
        }.execute();
    }

    private void xoaLich() {
        final LichItem lich = lichDangChon();
        if (lich == null) return;
        int answer = JOptionPane.showConfirmDialog(this,
                "Xóa lịch " + lich.ca.getTenCa() + " ngày " + lich.ngayLamViec.format(DATE)
                + " của " + lich.tenNV + "?", "Xác nhận xóa lịch",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (answer != JOptionPane.YES_OPTION) return;
        new SwingWorker<Void, Void>() {
            @Override protected Void doInBackground() throws Exception {
                dao.xoaLich(lich.maLichLamViec);
                return null;
            }
            @Override protected void done() {
                try {
                    get(); taiLich(); onChanged.run();
                    JOptionPane.showMessageDialog(pnlXepLichLamViec.this, "Đã xóa lịch làm việc.");
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                } catch (ExecutionException ex) { baoLoi(ex.getCause()); }
            }
        }.execute();
    }

    private void baoLoi(Throwable error) {
        Throwable root = error;
        while (root.getCause() != null && root.getCause() != root) root = root.getCause();
        String msg = root.getMessage() == null ? root.toString() : root.getMessage();
        if (root instanceof SQLException) {
            int code = ((SQLException) root).getErrorCode();
            if (code == 2601 || code == 2627) {
                msg = "Nhân viên đã được xếp ca này vào ngày đã chọn.";
            } else if (code == 547) {
                msg = "Không thể lưu vì nhân viên hoặc ca làm việc không còn tồn tại.";
            }
        }
        JOptionPane.showMessageDialog(this, msg, "Lỗi dữ liệu SQL Server",
                JOptionPane.ERROR_MESSAGE);
    }
}
