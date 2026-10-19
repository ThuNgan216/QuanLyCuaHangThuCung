package gui.panel;

import dao.MaVachDAO;
import dao.SanPhamDAO;
import entity.SanPham;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.*;
import utils.BarcodeUtils;

// Giao diện danh sách sản phẩm lấy dữ liệu từ SQL Server
public class pnlSanPham extends JPanel {
    private static final Color PINK = new Color(250, 48, 115);
    private static final Color DARK = new Color(31, 45, 70);
    private static final Color BG = new Color(252, 250, 251);
    private static final Color BORDER = new Color(237, 225, 231);
    private static final Font FONT = new Font("Times New Roman", Font.PLAIN, 14);
    private static final String[] COLUMNS = { "STT", "Tên sản phẩm", "Giá bán", "Tồn kho", "Trạng thái" };
    private final List<SanPham> danhSachSanPham = new ArrayList<>();
    private final List<SanPham> sanPhamDangHienThi = new ArrayList<>();
    private final NumberFormat tienVietNam = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
    private final JLabel lblTableTitle = new JLabel("Danh sách sản phẩm");
    private boolean dangTai = false;
    private int phienChon = 0;
    private final JButton btnCreateBarcode = button("Tạo mã vạch", true);

    private final DefaultTableModel model = new DefaultTableModel(COLUMNS, 0) {
        @Override
        public boolean isCellEditable(int row, int col) {
            return false;
        }
    };
    private final JTable table = new JTable(model);
    private final JTextField txtSearch = new JTextField();
    private final JTextField txtBarcode = new JTextField();
    private final JTextField txtName = new JTextField();
    private final JTextField txtPrice = new JTextField();
    private final JTextField txtStock = new JTextField();
    private final JTextField txtSupplier = new JTextField();
    private final JTextArea txtNote = new JTextArea(2, 16);
    private final JComboBox<NhaCungCapItem> cboCategory = new JComboBox<>();
    private final Map<String, Set<String>> nhaCungCapTheoSanPham = new HashMap<>();
    private final Map<String, String> tenNhaCungCap = new HashMap<>();
    private final JComboBox<String> cboStatus = new JComboBox<>(new String[] { "Tất cả", "Còn hàng", "Hết hàng" });
    private final JComboBox<String> cboType = new JComboBox<>(new String[] { "Sản phẩm", "Dịch vụ" });
    private final JComboBox<String> cboDetailStatus = new JComboBox<>(new String[] { "Còn hàng", "Hết hàng" });
    private final JLabel lblBarcodePreview = new JLabel("Chưa có hình mã vạch", SwingConstants.CENTER);
    private final JLabel lblCount = new JLabel();

    public pnlSanPham() {
        setLayout(new BorderLayout(0, 12));
        setBackground(BG);
        setBorder(new EmptyBorder(16, 18, 18, 18));
        add(createHeaderAndFilters(), BorderLayout.NORTH);
        add(createContent(), BorderLayout.CENTER);
        cboCategory.addItem(new NhaCungCapItem("all", "Tất cả nhà cung cấp"));
        cboCategory.addActionListener(e -> filterTable());
        cboType.setEnabled(false);
        cboDetailStatus.setEnabled(false);
        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                filterTable();
            }

            public void removeUpdate(DocumentEvent e) {
                filterTable();
            }

            public void changedUpdate(DocumentEvent e) {
                filterTable();
            }
        });
        cboStatus.addActionListener(e -> filterTable());
        loadSanPhamFromDB();
    }

    // Tiêu đề và khu vực lọc sản phẩm
    private JPanel createHeaderAndFilters() {
        JPanel wrapper = new JPanel(new BorderLayout(0, 12));
        wrapper.setOpaque(false);
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.add(label("Danh sách sản phẩm", 25, PINK, true), BorderLayout.WEST);
       JButton btnAdd = button("+ Thêm sản phẩm", true);
btnAdd.setPreferredSize(new Dimension(145, 30));
btnAdd.setFont(new Font("Times New Roman", Font.BOLD, 14));
btnAdd.addActionListener(e -> {
    gui.dialogs.dlgSanPham dlg = new gui.dialogs.dlgSanPham(SwingUtilities.getWindowAncestor(this), null);
    dlg.setVisible(true);
    if (dlg.isDaLuu()) {
        loadSanPhamFromDB(); // Tải lại bảng sau khi thêm thành công
    }});
        JPanel buttonWrapper = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        buttonWrapper.setOpaque(false);
        buttonWrapper.add(btnAdd);
        header.add(buttonWrapper, BorderLayout.EAST);
        wrapper.add(header, BorderLayout.NORTH);

        JPanel filters = panel();
        filters.setLayout(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(3, 4, 3, 4);
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.gridy = 0;
        gc.gridx = 0;
        gc.weightx = 1;
        filters.add(txtSearch, gc);
        txtSearch.setToolTipText("Tìm theo tên hoặc mã sản phẩm");
        cboCategory.setToolTipText("Lọc theo nhà cung cấp từng nhập sản phẩm");
        txtSearch.setText("");
        gc.weightx = 0;
        gc.gridx++;
        filters.add(cboCategory, gc);
        gc.gridx++;
        filters.add(cboStatus, gc);
        JButton btnFilter = button("Lọc", false);
        btnFilter.addActionListener(e -> filterTable());
        gc.gridx++;
        filters.add(btnFilter, gc);
        JButton btnRefresh = button("Làm mới", false);
        btnRefresh.addActionListener(e -> {
            txtSearch.setText("");
            cboCategory.setSelectedIndex(0);
            cboStatus.setSelectedIndex(0);
            loadSanPhamFromDB();
        });
        gc.gridx++;
        filters.add(btnRefresh, gc);
        wrapper.add(filters, BorderLayout.SOUTH);
        return wrapper;
    }

    // Danh sách bên trái và chi tiết bên phải
    private JSplitPane createContent() {
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, createTablePanel(), createDetailPanel());
        split.setResizeWeight(0.57);
        split.setDividerSize(0); // Ẩn thanh chia giữa danh sách và thông tin sản phẩm.
        split.setBorder(null);
        SwingUtilities.invokeLater(() -> split.setDividerLocation(0.57));
        return split;
    }

    private JPanel createTablePanel() {
        JPanel panel = panel();
        panel.setLayout(new BorderLayout(0, 10));
        lblTableTitle.setFont(new Font("Times New Roman", Font.BOLD, 19));
        lblTableTitle.setForeground(PINK);
        panel.add(lblTableTitle, BorderLayout.NORTH);
        table.setFont(FONT);
        table.setRowHeight(36);
        table.getTableHeader().setFont(new Font("Times New Roman", Font.BOLD, 13));
        table.getTableHeader().setPreferredSize(new Dimension(0, 36));
        table.setSelectionBackground(new Color(255, 226, 236));
        table.setSelectionForeground(DARK);
        table.setGridColor(new Color(238, 238, 238));
        table.setShowVerticalLines(false);
        table.setFillsViewportHeight(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getColumnModel().getColumn(0).setPreferredWidth(40);
        table.getColumnModel().getColumn(1).setPreferredWidth(260);
        table.getColumnModel().getColumn(2).setPreferredWidth(95);
        table.getColumnModel().getColumn(3).setPreferredWidth(65);
        table.getColumnModel().getColumn(4).setPreferredWidth(90);
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() >= 0)
                showSelected();
        });
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(BORDER));
        panel.add(scroll, BorderLayout.CENTER);
        lblCount.setFont(FONT);
        panel.add(lblCount, BorderLayout.SOUTH);
        return panel;
    }

    // Form thông tin sản phẩm và khung xem mã vạch
    private JPanel createDetailPanel() {
        JPanel panel = panel();
        panel.setLayout(new BorderLayout(0, 10));
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(label("Thông tin sản phẩm", 19, PINK, true), BorderLayout.WEST);
       JButton btnEdit = button("Chỉnh sửa", false);
btnEdit.addActionListener(e -> {
    int selectedRow = table.getSelectedRow();
    if (selectedRow < 0) {
        JOptionPane.showMessageDialog(this, "Vui lòng chọn sản phẩm cần chỉnh sửa trên bảng.", "Thông báo", JOptionPane.WARNING_MESSAGE);
        return;
    }
    SanPham spDangChon = sanPhamDangHienThi.get(selectedRow);
    gui.dialogs.dlgSanPham dlg = new gui.dialogs.dlgSanPham(SwingUtilities.getWindowAncestor(this), spDangChon);
    dlg.setVisible(true);
    if (dlg.isDaLuu()) {
        loadSanPhamFromDB(); // Tải lại bảng sau khi cập nhật
    }
});
        top.add(btnEdit, BorderLayout.EAST);
        panel.add(top, BorderLayout.NORTH);

        JPanel fields = new JPanel(new GridBagLayout());
        fields.setOpaque(false);
        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = 0;
        gc.weightx = 1;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.insets = new Insets(4, 0, 4, 0);
        int y = 0;
        addField(fields, gc, y++, "Mã vạch", txtBarcode);
        addField(fields, gc, y++, "Tên sản phẩm", txtName);
        addField(fields, gc, y++, "Danh mục", cboCategoryCopy());
        addField(fields, gc, y++, "Loại", cboType);
        addField(fields, gc, y++, "Đơn giá bán", txtPrice);
        addField(fields, gc, y++, "Số lượng tồn", txtStock);
        addField(fields, gc, y++, "Trạng thái", cboDetailStatus);
        addField(fields, gc, y++, "Nhà cung cấp", txtSupplier);
        txtNote.setLineWrap(true);
        txtNote.setWrapStyleWord(true);
        addField(fields, gc, y++, "Mô tả / Ghi chú", new JScrollPane(txtNote));
        for (JTextField field : new JTextField[] { txtBarcode, txtName, txtPrice, txtStock, txtSupplier }) {
            field.setEditable(false);
        }
        txtNote.setEditable(false);
        txtSupplier.setText("Chưa có dữ liệu");
        txtNote.setText("Chưa có dữ liệu mô tả.");
        panel.add(new JScrollPane(fields, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER), BorderLayout.CENTER);

        JPanel bottom = new JPanel(new BorderLayout(0, 8));
        bottom.setOpaque(false);
        lblBarcodePreview.setPreferredSize(new Dimension(0, 74));
        lblBarcodePreview.setBorder(BorderFactory.createDashedBorder(BORDER));
        bottom.add(lblBarcodePreview, BorderLayout.NORTH);
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);
        JButton btnView = button("Xem mã vạch", false);
        JButton btnPrint = button("In tem mã vạch", false);
        btnCreateBarcode.addActionListener(e -> taoMaVachChoSanPham());
        btnView.addActionListener(e -> xemMaVach());
        btnPrint.addActionListener(e -> notice("In tem mã vạch"));
        btnCreateBarcode.setEnabled(false);
        actions.add(btnCreateBarcode);
        actions.add(btnView);
        actions.add(btnPrint);
        bottom.add(actions, BorderLayout.SOUTH);
        panel.add(bottom, BorderLayout.SOUTH);
        return panel;
    }

    private JComboBox<String> cboCategoryCopy() {
        return new JComboBox<>(new String[] { "Thức ăn cho chó", "Thức ăn cho mèo", "Phụ kiện", "Vệ sinh" });
    }

    private void addField(JPanel parent, GridBagConstraints gc, int index, String title, JComponent field) {
        JPanel row = new JPanel(new BorderLayout(0, 3));
        row.setOpaque(false);
        row.add(label(title, 13, DARK, false), BorderLayout.NORTH);
        field.setFont(FONT);
        if (!(field instanceof JScrollPane))
            field.setPreferredSize(new Dimension(200, 29));
        row.add(field, BorderLayout.CENTER);
        gc.gridy = index;
        parent.add(row, gc);
    }

    // Kết quả tải sản phẩm và nhà cung cấp trên cùng luồng nền
    private static class DuLieuSanPham {
        List<SanPham> sanPham;
        List<String[]> nhaCungCap;
        List<String[]> lienKet;
    }

    // Dữ liệu cho combobox nhà cung cấp
    private static class NhaCungCapItem {
        final String maNCC;
        final String tenNCC;

        NhaCungCapItem(String maNCC, String tenNCC) {
            this.maNCC = maNCC;
            this.tenNCC = tenNCC;
        }

        @Override
        public String toString() {
            return tenNCC;
        }
    }

    // Tải sản phẩm và nhà cung cấp từ SQL Server, không chặn giao diện
    private void loadSanPhamFromDB() {
        if (dangTai)
            return;
        dangTai = true;
        lblCount.setText("Đang tải danh sách sản phẩm...");
        new SwingWorker<DuLieuSanPham, Void>() {
            @Override
            protected DuLieuSanPham doInBackground() throws Exception {
                SanPhamDAO dao = new SanPhamDAO();
                DuLieuSanPham data = new DuLieuSanPham();
                data.sanPham = dao.layDanhSachSanPham();
                data.nhaCungCap = dao.layDanhSachNhaCungCap();
                data.lienKet = dao.layNhaCungCapCuaSanPham();
                return data;
            }

            @Override
            protected void done() {
                dangTai = false;
                try {
                    DuLieuSanPham data = get();
                    danhSachSanPham.clear();
                    danhSachSanPham.addAll(data.sanPham);
                    nhaCungCapTheoSanPham.clear();
                    tenNhaCungCap.clear();
                    String maDangChon = "all";
                    NhaCungCapItem selected = (NhaCungCapItem) cboCategory.getSelectedItem();
                    if (selected != null)
                        maDangChon = selected.maNCC;
                    cboCategory.removeAllItems();
                    cboCategory.addItem(new NhaCungCapItem("all", "Tất cả nhà cung cấp"));
                    for (String[] ncc : data.nhaCungCap) {
                        tenNhaCungCap.put(ncc[0], ncc[1]);
                        cboCategory.addItem(new NhaCungCapItem(ncc[0], ncc[1]));
                    }
                    for (String[] pair : data.lienKet) {
                        nhaCungCapTheoSanPham.computeIfAbsent(pair[0], k -> new HashSet<>()).add(pair[1]);
                    }
                    for (int i = 0; i < cboCategory.getItemCount(); i++) {
                        if (cboCategory.getItemAt(i).maNCC.equals(maDangChon)) {
                            cboCategory.setSelectedIndex(i);
                            break;
                        }
                    }
                    filterTable();
                } catch (Exception ex) {
                    lblCount.setText("Không tải được dữ liệu từ SQL Server");
                    JOptionPane.showMessageDialog(pnlSanPham.this,
                            "Không tải được danh sách sản phẩm/NCC: " + ex.getMessage(),
                            "Lỗi dữ liệu", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    // Lọc theo tên, mã sản phẩm và số lượng tồn
    private void filterTable() {
        String keyword = txtSearch.getText().trim().toLowerCase(Locale.ROOT);
        String status = String.valueOf(cboStatus.getSelectedItem());
        NhaCungCapItem selectedNcc = (NhaCungCapItem) cboCategory.getSelectedItem();
        String maNCC = selectedNcc == null ? "all" : selectedNcc.maNCC;
        model.setRowCount(0);
        sanPhamDangHienThi.clear();
        for (SanPham sp : danhSachSanPham) {
            String ma = sp.getMaSP() == null ? "" : sp.getMaSP();
            String ten = sp.getTenSP() == null ? "" : sp.getTenSP();
            if (!ten.toLowerCase(Locale.ROOT).contains(keyword)
                    && !ma.toLowerCase(Locale.ROOT).contains(keyword))
                continue;
            String trangThai = sp.getSoLuongTon() > 0 ? "Còn hàng" : "Hết hàng";
            if (!"Tất cả".equals(status) && !status.equals(trangThai))
                continue;
            if (!"all".equals(maNCC) && !nhaCungCapTheoSanPham
                    .getOrDefault(ma, java.util.Collections.emptySet()).contains(maNCC))
                continue;
            sanPhamDangHienThi.add(sp);
            model.addRow(new Object[] { sanPhamDangHienThi.size(), ten,
                    tienVietNam.format(sp.getGiaBan()) + " đ",
                    sp.getSoLuongTon(), trangThai });
        }
        int count = model.getRowCount();
        lblTableTitle.setText("Danh sách sản phẩm (" + count + ")");
        lblCount.setText("Hiển thị " + count + " / " + danhSachSanPham.size() + " sản phẩm");
        if (count > 0)
            table.setRowSelectionInterval(0, 0);
        else
            clearSelected();
    }

    // Hiển thị thông tin sản phẩm được chọn
    private void showSelected() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0)
            return;
        int row = table.convertRowIndexToModel(viewRow);
        if (row < 0 || row >= sanPhamDangHienThi.size())
            return;
        SanPham sp = sanPhamDangHienThi.get(row);
        txtName.setText(sp.getTenSP());
        txtPrice.setText(tienVietNam.format(sp.getGiaBan()) + " đ");
        txtStock.setText(String.valueOf(sp.getSoLuongTon()));
        cboDetailStatus.setSelectedItem(sp.getSoLuongTon() > 0 ? "Còn hàng" : "Hết hàng");
        txtBarcode.setText("");
        Set<String> codes = nhaCungCapTheoSanPham.getOrDefault(sp.getMaSP(), java.util.Collections.emptySet());
        List<String> names = new ArrayList<>();
        for (String code : codes)
            names.add(tenNhaCungCap.getOrDefault(code, code));
        java.util.Collections.sort(names);
        txtSupplier.setText(names.isEmpty() ? "Chưa có phiếu nhập" : String.join(", ", names));
        txtSupplier.setToolTipText(txtSupplier.getText());
        txtNote.setText("Chưa có dữ liệu mô tả.");
        lblBarcodePreview.setIcon(null);
        lblBarcodePreview.setText("Đang kiểm tra mã vạch...");
        btnCreateBarcode.setEnabled(false);
        final int phien = ++phienChon;
        final String maSP = sp.getMaSP();
        new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() throws Exception {
                return new MaVachDAO().layMaVachTheoSanPham(maSP);
            }

            @Override
            protected void done() {
                if (phien != phienChon)
                    return;
                try {
                    String ma = get();
                    txtBarcode.setText(ma == null ? "" : ma);
                    if (ma == null) {
                        lblBarcodePreview.setText("Chưa có mã vạch - Nhấn Tạo mã vạch");
                        btnCreateBarcode.setEnabled(true);
                    } else {
                        hienThiHinhMaVach(ma);
                    }
                } catch (Exception ex) {
                    lblBarcodePreview.setText("Không tải được mã vạch");
                    JOptionPane.showMessageDialog(pnlSanPham.this,
                            "Không tải được mã vạch: " + ex.getMessage(),
                            "Lỗi dữ liệu", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private void clearSelected() {
        phienChon++;
        btnCreateBarcode.setEnabled(false);
        lblBarcodePreview.setIcon(null);
        txtBarcode.setText("");
        txtName.setText("");
        txtPrice.setText("");
        txtStock.setText("");
        txtSupplier.setText("");
        txtNote.setText("");
        lblBarcodePreview.setText("Chưa chọn sản phẩm");
    }

    // Tạo mã nội bộ Code 128 và lưu khi sản phẩm chưa có mã vạch
    private void taoMaVachChoSanPham() {
        int row = table.getSelectedRow();
        if (row < 0)
            return;
        int index = table.convertRowIndexToModel(row);
        if (index < 0 || index >= sanPhamDangHienThi.size())
            return;
        final String maSP = sanPhamDangHienThi.get(index).getMaSP();
        final int phien = phienChon;
        btnCreateBarcode.setEnabled(false);
        lblBarcodePreview.setText("Đang tạo mã vạch...");
        new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() throws Exception {
                return new MaVachDAO().taoMaVachNeuChuaCo(maSP);
            }

            @Override
            protected void done() {
                if (phien != phienChon)
                    return;
                try {
                    String ma = get();
                    txtBarcode.setText(ma);
                    hienThiHinhMaVach(ma);
                } catch (Exception ex) {
                    btnCreateBarcode.setEnabled(true);
                    lblBarcodePreview.setText("Tạo mã vạch thất bại");
                    JOptionPane.showMessageDialog(pnlSanPham.this,
                            "Không tạo được mã vạch: " + ex.getMessage(),
                            "Lỗi dữ liệu", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    // Xem mã vạch của sản phẩm đang chọn
    private void xemMaVach() {
        String ma = txtBarcode.getText().trim();
        if (ma.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Sản phẩm chưa có mã vạch. Hãy nhấn Tạo mã vạch.");
            return;
        }
        try {
            BufferedImage image = BarcodeUtils.taoMaVach(ma);
            ImageIcon icon = new ImageIcon(image);
            JOptionPane.showMessageDialog(this, new JLabel(icon),
                    "Mã vạch: " + ma, JOptionPane.PLAIN_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Không thể vẽ mã vạch: " + ex.getMessage());
        }
    }

    // Hiển thị ảnh mã vạch Code 128 ở phần thông tin sản phẩm
    private void hienThiHinhMaVach(String ma) {
        try {
            BufferedImage image = BarcodeUtils.taoMaVach(ma);
            int width = Math.min(300, Math.max(160, lblBarcodePreview.getWidth() - 20));
            Image scaled = image.getScaledInstance(width, 63, Image.SCALE_SMOOTH);
            lblBarcodePreview.setIcon(new ImageIcon(scaled));
            lblBarcodePreview.setText("");
            lblBarcodePreview.setHorizontalAlignment(SwingConstants.CENTER);
        } catch (Exception ex) {
            lblBarcodePreview.setIcon(null);
            lblBarcodePreview.setText("Không vẽ được mã vạch");
        }
    }

    private JPanel panel() {
        JPanel panel = new JPanel();
        panel.setBackground(Color.WHITE);
        panel.setBorder(new CompoundBorder(new LineBorder(BORDER, 1, true), new EmptyBorder(12, 12, 12, 12)));
        return panel;
    }

    private JLabel label(String text, int size, Color color, boolean bold) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Times New Roman", bold ? Font.BOLD : Font.PLAIN, size));
        label.setForeground(color);
        return label;
    }

    private JButton button(String text, boolean primary) {
        JButton b = new JButton(text);
        b.setFont(new Font("Times New Roman", Font.BOLD, 13));
        b.setBackground(primary ? PINK : Color.WHITE);
        b.setForeground(primary ? Color.WHITE : PINK);
        b.setFocusPainted(false);
        b.setBorder(new CompoundBorder(new LineBorder(PINK, 1, true), new EmptyBorder(8, 12, 8, 12)));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    private void notice(String action) {
        JOptionPane.showMessageDialog(this, "Chức năng " + action + " sẽ được tích hợp sau khi hoàn thiện giao diện.",
                "PetPlus", JOptionPane.INFORMATION_MESSAGE);
    }

    // Chạy thử giao diện độc lập (cần SQL Server)
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("PetPlus - Danh sách sản phẩm");
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.setContentPane(new pnlSanPham());
            frame.setSize(1200, 760);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
