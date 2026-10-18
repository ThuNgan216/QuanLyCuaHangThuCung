package gui.panel;

import java.awt.*;
import java.text.NumberFormat;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import database.ConnectDB;
import utils.Auth;
import java.sql.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;

// Giao diện thanh toán PetPlus - chưa lưu dữ liệu vào SQL Server
public class pnlThanhToan extends JPanel {
    private static final Color PINK = new Color(246, 65, 125);
    private static final Color DARK_PINK = new Color(217, 37, 96);
    private static final Color LIGHT_PINK = new Color(255, 241, 246);
    private static final Color BORDER = new Color(238, 222, 229);
    private static final Color TEXT = new Color(40, 46, 58);
    private static final Font FONT = new Font("Times New Roman", Font.PLAIN, 13);
    private static final NumberFormat MONEY = NumberFormat.getIntegerInstance(new Locale("vi", "VN"));
    private final DefaultTableModel model = new DefaultTableModel(
            new String[] { "STT", "Tên sản phẩm", "Số lượng", "Đơn giá", "Thành tiền" }, 0) {
        @Override
        public boolean isCellEditable(int r, int c) {
            return false;
        }
    };

    private final JTable table = new JTable(model);
    private final JLabel lblSubtotal = valueLabel("0 đ");
    private final JLabel lblDiscount = valueLabel("0 đ");
    private final JLabel lblTotal = valueLabel("0 đ");
    private final JLabel lblChange = valueLabel("0 đ");
    private final JTextField txtPaid = new JTextField("2.000.000");
    private final JTextField txtDiscount = new JTextField("10");
    private final JTextField txtOrderCode = field("#DH-20261017-001", false);
    private final JTextField txtOrderDate = field(LocalDateTime.now().format(
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")), false);

    private final JTextField txtEmployee = field("Đang tải nhân viên...", false);
    private final JTextField txtCustomer = field("", true);
    private final JTextField txtPhone = field("", true);
    private final JTextField txtAddress = field("", true);
    private final JTextField txtMemberType = field("", false);
    private final JTextField txtPoints = field("0", false);

    // Danh sách khách hàng tìm theo số điện thoại
    private final JPopupMenu popupKhachHang = new JPopupMenu();
    private final DefaultListModel<KhachHangItem> modelKhachHang = new DefaultListModel<>();
    private final JList<KhachHangItem> listKhachHang = new JList<>(modelKhachHang);
    private boolean dangChonKhachHang = false;
    private int lanTimKhachHang = 0;

    private static class KhachHangItem {
        final String ten;
        final String sdt;
        final String diaChi;
        final int hangThanhVien;
        final int diemTichLuy;

        KhachHangItem(String ten, String sdt, String diaChi, int hangThanhVien, int diemTichLuy) {
            this.ten = ten;
            this.sdt = sdt;
            this.diaChi = diaChi;
            this.hangThanhVien = hangThanhVien;
            this.diemTichLuy = diemTichLuy;
        }

        @Override
        public String toString() {
            return sdt + " - " + ten;
        }
    }

    private final JRadioButton rbCash = new JRadioButton("Tiền mặt", true);
    private final JRadioButton rbQR = new JRadioButton("QR Code");
    private long total = 0;

    public pnlThanhToan() {
        setLayout(new BorderLayout(0, 12));
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(12, 14, 14, 14));
        add(createTitle(), BorderLayout.NORTH);
        JPanel body = new JPanel(new BorderLayout(10, 10));
        body.setOpaque(false);
        body.add(createTop(), BorderLayout.NORTH);
        body.add(createBottom(), BorderLayout.CENTER);
        add(body, BorderLayout.CENTER);
        // Dữ liệu mẫu để xem giao diện; thay bằng dữ liệu giỏ hàng khi tích hợp POS
        addProduct("Thức ăn cho chó Royal Canin 5kg", 2, 450000);
        addProduct("Dây xích dắt chó cao cấp", 1, 250000);
        addProduct("Sữa tắm chó mèo Bioline 500ml", 3, 120000);
        addProduct("Đồ chơi bóng tennis cho chó", 2, 85000);
        txtPaid.getDocument().addDocumentListener(new SimpleDocumentListener(this::updateTotals));
        txtDiscount.getDocument().addDocumentListener(new SimpleDocumentListener(this::updateTotals));
        updateTotals();
        setupTimKhachHang();
        // Chỉ tra cứu nhân viên khi panel được hiển thị; cập nhật khi quay lại màn
        // hình.
        addHierarchyListener(e -> {
            if ((e.getChangeFlags() & java.awt.event.HierarchyEvent.SHOWING_CHANGED) != 0
                    && isShowing()) {
                loadEmployeeFromDatabase();
            }
        });
    }

    /**
     * Lấy tên nhân viên đang lập hóa đơn từ SQL Server theo phiên đăng nhập.
     * Không lấy nhân viên bất kỳ (ví dụ SELECT TOP 1), tránh ghi sai người lập.
     */

    private void loadEmployeeFromDatabase() {
        if (Auth.user == null) {
            txtEmployee.setText("Chưa đăng nhập");
            return;
        }
        final String employeeId = Auth.user.getMaNV();
        if (employeeId == null || employeeId.trim().isEmpty()) {
            txtEmployee.setText("Không xác định nhân viên");
            return;
        }
        txtEmployee.setText("Đang tải nhân viên...");
        new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() throws Exception {
                String sql = "SELECT tenNV FROM NhanVien WHERE maNV = ?";
                try (Connection con = ConnectDB.getConnection();
                        PreparedStatement stmt = con.prepareStatement(sql)) {
                    stmt.setString(1, employeeId);
                    try (ResultSet rs = stmt.executeQuery()) {
                        return rs.next() ? rs.getString("tenNV") : null;
                    }
                }
            }

            @Override
            protected void done() {
                // Tránh hiển thị dữ liệu của nhân viên cũ nếu phiên đăng nhập đã đổi.
                if (Auth.user == null || !employeeId.equals(Auth.user.getMaNV())) {
                    return;
                }
                try {
                    String employeeName = get();
                    txtEmployee.setText(employeeName == null || employeeName.trim().isEmpty()
                            ? "Không tìm thấy nhân viên"
                            : employeeName);
                } catch (Exception ex) {
                    txtEmployee.setText("Lỗi tải nhân viên");
                    JOptionPane.showMessageDialog(pnlThanhToan.this,
                            "Không thể lấy nhân viên từ SQL Server: " + ex.getMessage(),
                            "Lỗi cơ sở dữ liệu", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    // Tìm khách hàng theo số điện thoại và hiển thị danh sách gợi ý

    private void setupTimKhachHang() {
        listKhachHang.setFont(FONT);
        listKhachHang.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane scroll = new JScrollPane(listKhachHang);
        scroll.setPreferredSize(new Dimension(320, 140));
        popupKhachHang.add(scroll);

        javax.swing.Timer timer = new javax.swing.Timer(300, e -> timKhachHang(txtPhone.getText().trim()));
        timer.setRepeats(false);

        txtPhone.getDocument().addDocumentListener(new DocumentListener() {
            private void capNhat() {
                if (dangChonKhachHang)
                    return;

                lanTimKhachHang++;
                txtCustomer.setText("");
                txtAddress.setText("");
                popupKhachHang.setVisible(false);
                timer.restart();
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                capNhat();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                capNhat();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                capNhat();
            }
        });

        listKhachHang.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() >= 1)
                    chonKhachHang();
            }
        });

        listKhachHang.getInputMap().put(KeyStroke.getKeyStroke("ENTER"), "chonKhachHang");
        listKhachHang.getActionMap().put("chonKhachHang", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                chonKhachHang();
            }
        });
    }

    // Lấy thông tin khách hàng từ SQL Server, không chặn giao diện

    private void timKhachHang(String sdt) {
        final int lanTim = ++lanTimKhachHang;
        modelKhachHang.clear();

        if (sdt.length() < 2) {
            popupKhachHang.setVisible(false);
            return;
        }

        new SwingWorker<java.util.List<KhachHangItem>, Void>() {
            @Override
            protected java.util.List<KhachHangItem> doInBackground() throws Exception {
                java.util.List<KhachHangItem> ketQua = new java.util.ArrayList<>();
                String sql = "SELECT tenKH, sdt, diaChi, hangThanhVien, diemTichLuy "
                        + "FROM KhachHang WHERE sdt LIKE ? ORDER BY sdt";

                try (Connection con = ConnectDB.getConnection();
                        PreparedStatement stmt = con.prepareStatement(sql)) {
                    stmt.setString(1, "%" + sdt + "%");

                    try (ResultSet rs = stmt.executeQuery()) {
                        while (rs.next()) {
                            ketQua.add(new KhachHangItem(
                                    rs.getString("tenKH"),
                                    rs.getString("sdt"),
                                    rs.getString("diaChi"),
                                    rs.getInt("hangThanhVien"),
                                    rs.getInt("diemTichLuy")));

                        }
                    }
                }
                return ketQua;
            }

            @Override
            protected void done() {
                if (lanTim != lanTimKhachHang || !sdt.equals(txtPhone.getText().trim()))
                    return;

                try {
                    for (KhachHangItem kh : get())
                        modelKhachHang.addElement(kh);

                    if (!modelKhachHang.isEmpty() && txtPhone.isShowing()) {
                        popupKhachHang.show(txtPhone, 0, txtPhone.getHeight());
                    } else {
                        popupKhachHang.setVisible(false);
                    }
                } catch (Exception ex) {
                    popupKhachHang.setVisible(false);
                    JOptionPane.showMessageDialog(pnlThanhToan.this,
                            "Không thể tìm khách hàng từ SQL Server: " + ex.getMessage(),
                            "Lỗi cơ sở dữ liệu", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    // Chọn số điện thoại và tự điền thông tin khách hàng

    private void chonKhachHang() {
        KhachHangItem kh = listKhachHang.getSelectedValue();
        if (kh == null)
            return;

        dangChonKhachHang = true;
        lanTimKhachHang++;
        try {
            txtCustomer.setText(kh.ten == null ? "" : kh.ten);
            txtPhone.setText(kh.sdt == null ? "" : kh.sdt);
            txtAddress.setText(kh.diaChi == null ? "" : kh.diaChi);
            // Hiển thị loại thành viên và điểm tích lũy
            // Hiển thị loại thành viên và điểm tích lũy
            txtMemberType.setText(String.valueOf(kh.hangThanhVien));
            txtPoints.setText(String.valueOf(kh.diemTichLuy));
        } finally {
            dangChonKhachHang = false;
        }
        popupKhachHang.setVisible(false);
    }

    private JPanel createTitle() {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        JLabel title = label("Thanh toán", 22, DARK_PINK, true);
        p.add(title, BorderLayout.WEST);
        p.setBorder(new CompoundBorder(new MatteBorder(0, 0, 1, 0, PINK), new EmptyBorder(0, 6, 9, 0)));
        return p;
    }

    private JPanel createTop() {
        JPanel p = new JPanel(new GridLayout(1, 3, 12, 0));
        p.setOpaque(false);
        JPanel order = section("Thông tin đơn hàng");
        addRows(order, new String[] { "Mã đơn hàng:", "Ngày tạo:", "Nhân viên:" },
                new JComponent[] { txtOrderCode, txtOrderDate, txtEmployee });
        JPanel customer = section("Thông tin khách hàng");
        addRows(customer,
                new String[] { "Họ và tên:", "Số điện thoại:", "Địa chỉ:", "Loại thành viên:", "Điểm tích lũy:" },
                new JComponent[] { txtCustomer, txtPhone, txtAddress, txtMemberType, txtPoints });
        JPanel summary = section("Tạm tính");
        JPanel sumGrid = new JPanel(new GridLayout(3, 2, 6, 9));
        sumGrid.setOpaque(false);
        sumGrid.add(label("Tạm tính:", 13, TEXT, false));
        sumGrid.add(lblSubtotal);
        JPanel disc = new JPanel(new FlowLayout(FlowLayout.RIGHT, 2, 0));
        disc.setOpaque(false);
        txtDiscount.setHorizontalAlignment(JTextField.RIGHT);
        txtDiscount.setPreferredSize(new Dimension(34, 24));
        disc.add(txtDiscount);
        disc.add(label("%", 12, TEXT, false));
        sumGrid.add(disc);
        sumGrid.add(lblDiscount);
        JLabel caption = label("Tổng thanh toán:", 14, TEXT, true);
        sumGrid.add(caption);
        lblTotal.setForeground(DARK_PINK);
        lblTotal.setFont(new Font("Times New Roman", Font.BOLD, 18));
        sumGrid.add(lblTotal);
        summary.add(sumGrid, BorderLayout.CENTER);
        p.add(order);
        p.add(customer);
        p.add(summary);
        p.setPreferredSize(new Dimension(0, 210));
        return p;
    }

    private JPanel createBottom() {
        JPanel p = new JPanel(new BorderLayout(10, 0));
        p.setOpaque(false);
        JPanel items = section("Danh sách sản phẩm");
        configureTable();
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(new LineBorder(BORDER));
        items.add(scroll, BorderLayout.CENTER);
        JPanel actions = new JPanel(new GridLayout(1, 3, 12, 0));
        actions.setOpaque(false);
        actions.setBorder(new EmptyBorder(9, 0, 0, 0));
        JButton add = outlineButton("+  Thêm sản phẩm");
        JButton edit = outlineButton("Sửa sản phẩm");
        JButton remove = outlineButton("-   Xóa sản phẩm");
        add.addActionListener(e -> promptAdd());
        edit.addActionListener(e -> promptEdit());
        remove.addActionListener(e -> removeSelected());
        actions.add(add);
        actions.add(edit);
        actions.add(remove);
        items.add(actions, BorderLayout.SOUTH);
        JPanel right = new JPanel(new BorderLayout(0, 9));
        right.setOpaque(false);
        JPanel payMethod = section("Phương thức thanh toán");
        JPanel radio = new JPanel(new GridLayout(1, 2));
        radio.setOpaque(false);
        for (JRadioButton rb : new JRadioButton[] { rbCash, rbQR }) {
            rb.setOpaque(false);
            rb.setFont(FONT);
            rb.setForeground(TEXT);
            radio.add(rb);
        }
        ButtonGroup group = new ButtonGroup();
        group.add(rbCash);
        group.add(rbQR);
        payMethod.add(radio, BorderLayout.CENTER);
        payMethod.setPreferredSize(new Dimension(0, 84));
        JPanel payment = section("Khách hàng đưa tiền");
        JPanel paymentContent = new JPanel();
        paymentContent.setOpaque(false);
        paymentContent.setLayout(new BoxLayout(paymentContent, BoxLayout.Y_AXIS));
        JPanel line = new JPanel(new BorderLayout(8, 0));
        line.setOpaque(false);
        line.add(label("Số tiền khách đưa:", 13, TEXT, false), BorderLayout.WEST);
        txtPaid.setFont(FONT);
        txtPaid.setHorizontalAlignment(JTextField.RIGHT);
        line.add(txtPaid, BorderLayout.CENTER);
        line.add(label("đ", 13, TEXT, false), BorderLayout.EAST);
        paymentContent.add(line);
        paymentContent.add(Box.createVerticalStrut(12));
        JPanel amounts = new JPanel(new GridLayout(1, 3, 6, 0));
        amounts.setOpaque(false);
        for (long amount : new long[] { 1500000, 1600000, 2000000 }) {
            JButton b = outlineButton(MONEY.format(amount));
            b.addActionListener(e -> txtPaid.setText(MONEY.format(amount)));
            amounts.add(b);
        }
        paymentContent.add(amounts);
        paymentContent.add(Box.createVerticalStrut(15));
        paymentContent.add(new JSeparator());
        paymentContent.add(Box.createVerticalStrut(12));
        JPanel changeLine = new JPanel(new BorderLayout());
        changeLine.setOpaque(false);
        changeLine.add(label("Tiền thừa trả khách:", 13, TEXT, false), BorderLayout.WEST);
        lblChange.setForeground(DARK_PINK);
        changeLine.add(lblChange, BorderLayout.EAST);
        paymentContent.add(changeLine);
        payment.add(paymentContent, BorderLayout.CENTER);
        JPanel footer = new JPanel(new GridLayout(2, 1, 0, 7));
        footer.setOpaque(false);
        JButton confirm = primaryButton("Xác nhận thanh toán");
        JButton cancel = outlineButton("Hủy thanh toán");
        confirm.addActionListener(e -> confirmPayment());
        cancel.addActionListener(e -> cancelPayment());
        footer.add(confirm);
        footer.add(cancel);
        footer.setPreferredSize(new Dimension(0, 82));
        right.add(payMethod, BorderLayout.NORTH);
        right.add(payment, BorderLayout.CENTER);
        right.add(footer, BorderLayout.SOUTH);
        right.setPreferredSize(new Dimension(292, 0));
        p.add(items, BorderLayout.CENTER);
        p.add(right, BorderLayout.EAST);
        return p;
    }

    private JPanel section(String title) {
        JPanel p = new JPanel(new BorderLayout(0, 8));
        p.setBackground(Color.WHITE);
        p.setBorder(new LineBorder(BORDER));
        JPanel heading = new JPanel(new BorderLayout());
        heading.setBackground(LIGHT_PINK);
        heading.setBorder(new EmptyBorder(7, 10, 7, 10));
        heading.add(label(title, 14, DARK_PINK, true));
        p.add(heading, BorderLayout.NORTH);
        JPanel inner = new JPanel(new BorderLayout());
        inner.setOpaque(false);
        inner.setBorder(new EmptyBorder(4, 9, 9, 9));
        p.add(inner, BorderLayout.CENTER);
        return p;
    }
    // Gắn form 3 dòng vào vùng nội dung của panel

    private void addRows(JPanel section, String[] names, JComponent[] controls) {
        JPanel grid = new JPanel(new GridBagLayout());
        grid.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(3, 5, 3, 5);
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;
        for (int i = 0; i < names.length; i++) {
            c.gridy = i;
            c.gridx = 0;
            c.weightx = 0;
            grid.add(label(names[i], 12, TEXT, false), c);
            c.gridx = 1;
            c.weightx = 1;
            grid.add(controls[i], c);
        }
        section.add(grid, BorderLayout.CENTER);
    }

    private void configureTable() {
        table.setFont(FONT);
        table.setRowHeight(28);
        table.setSelectionBackground(new Color(255, 222, 233));
        table.setGridColor(BORDER);
        table.setShowGrid(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Times New Roman", Font.BOLD, 12));
        header.setBackground(LIGHT_PINK);
        header.setPreferredSize(new Dimension(0, 30));
        int[] widths = { 38, 230, 65, 95, 105 };
        for (int i = 0; i < widths.length; i++)
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        DefaultTableCellRenderer right = new DefaultTableCellRenderer();
        right.setHorizontalAlignment(SwingConstants.RIGHT);
        table.getColumnModel().getColumn(2).setCellRenderer(right);
        DefaultTableCellRenderer currency = new DefaultTableCellRenderer() {
            @Override
            protected void setValue(Object value) {
                setText(value instanceof Number ? MONEY.format(((Number) value).longValue()) + " đ" : "");
                setHorizontalAlignment(SwingConstants.RIGHT);
            }
        };
        table.getColumnModel().getColumn(3).setCellRenderer(currency);
        table.getColumnModel().getColumn(4).setCellRenderer(currency);
    }

    private void addProduct(String name, int qty, long price) {
        // Lưu số nguyên trong model để tính tiền chính xác
        model.addRow(new Object[] { model.getRowCount() + 1, name, qty, price, qty * price });
        updateTotals();
    }

    private void promptAdd() {
        JTextField name = new JTextField();
        JTextField qty = new JTextField("1");
        JTextField price = new JTextField();
        Object[] fields = { "Tên sản phẩm:", name, "Số lượng:", qty, "Đơn giá (đ):", price };
        if (JOptionPane.showConfirmDialog(this, fields, "Thêm sản phẩm",
                JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION)
            return;
        try {
            String title = name.getText().trim();
            int count = Integer.parseInt(qty.getText().trim());
            long amount = parseMoney(price.getText());
            if (title.isEmpty() || count <= 0 || amount < 0)
                throw new IllegalArgumentException();
            for (int r = 0; r < model.getRowCount(); r++) {
                if (title.equalsIgnoreCase(String.valueOf(model.getValueAt(r, 1)))) {
                    int existing = (int) model.getValueAt(r, 2);
                    model.setValueAt(Math.addExact(existing, count), r, 2);
                    updateTotals();
                    return;
                }
            }
            addProduct(title, count, amount);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Nhập tên, số lượng và đơn giá hợp lệ.");
        }
    }

    private void promptEdit() {
        int r = table.getSelectedRow();
        if (r < 0) {
            JOptionPane.showMessageDialog(this, "Hãy chọn một sản phẩm.");
            return;
        }
        int row = table.convertRowIndexToModel(r);
        JTextField qty = new JTextField(String.valueOf(model.getValueAt(row, 2)));
        JTextField price = new JTextField(MONEY.format((long) model.getValueAt(row, 3)));
        if (JOptionPane.showConfirmDialog(this, new Object[] { "Số lượng:", qty, "Đơn giá:", price },
                "Sửa sản phẩm", JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION)
            return;
        try {
            int count = Integer.parseInt(qty.getText().trim());
            long amount = parseMoney(price.getText());
            if (count <= 0 || amount < 0)
                throw new IllegalArgumentException();
            model.setValueAt(count, row, 2);
            model.setValueAt(amount, row, 3);
            updateTotals();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Số lượng hoặc đơn giá không hợp lệ.");
        }
    }

    private void removeSelected() {
        int r = table.getSelectedRow();
        if (r < 0) {
            JOptionPane.showMessageDialog(this, "Hãy chọn một sản phẩm cần xóa.");
            return;
        }
        model.removeRow(table.convertRowIndexToModel(r));
        for (int i = 0; i < model.getRowCount(); i++)
            model.setValueAt(i + 1, i, 0);
        updateTotals();
    }

    private void updateTotals() {
        long subtotal = 0;
        for (int i = 0; i < model.getRowCount(); i++) {
            int qty = (int) model.getValueAt(i, 2);
            long price = (long) model.getValueAt(i, 3);
            long line = qty * price;
            model.setValueAt(line, i, 4);
            subtotal += line;
        }
        double discountRate;
        try {
            discountRate = Double.parseDouble(txtDiscount.getText().replace(',', '.').trim());
        } catch (Exception e) {
            discountRate = 0;
        }
        discountRate = Math.max(0, Math.min(100, discountRate));
        long discount = Math.round(subtotal * discountRate / 100);
        total = subtotal - discount;
        lblSubtotal.setText(MONEY.format(subtotal) + " đ");
        lblDiscount.setText("-" + MONEY.format(discount) + " đ");
        lblTotal.setText(MONEY.format(total) + " đ");
        long paid;
        try {
            paid = parseMoney(txtPaid.getText());
        } catch (Exception e) {
            paid = 0;
        }
        lblChange.setText(MONEY.format(Math.max(0, paid - total)) + " đ");
    }

    private void confirmPayment() {
        if (model.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Chưa có sản phẩm trong đơn hàng.");
            return;
        }
        if (txtCustomer.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Hãy nhập tên khách hàng.");
            return;
        }
        if (rbCash.isSelected()) {
            try {
                if (parseMoney(txtPaid.getText()) < total) {
                    JOptionPane.showMessageDialog(this, "Số tiền khách đưa chưa đủ để thanh toán.");
                    return;
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Số tiền khách đưa không hợp lệ.");
                return;
            }
        }
        JOptionPane.showMessageDialog(this,
                "Giao diện đã kiểm tra thông tin thanh toán.\nChưa ghi hóa đơn hoặc trừ tồn kho trong SQL Server.",
                "PetPlus - Bản giao diện", JOptionPane.INFORMATION_MESSAGE);
    }

    private void cancelPayment() {
        if (JOptionPane.showConfirmDialog(this, "Xóa danh sách sản phẩm và thông tin đang nhập?",
                "Hủy thanh toán", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION)
            return;
        model.setRowCount(0);
        txtCustomer.setText("");
        txtPhone.setText("");
        txtAddress.setText("");
        txtPaid.setText("0");
        updateTotals();
    }

    private static long parseMoney(String value) {
        String cleaned = value.replace(".", "").replace(",", "").replace(" ", "").replace("đ", "").trim();
        if (cleaned.isEmpty())
            return 0;
        return Long.parseLong(cleaned);
    }

    private static JLabel label(String s, int size, Color color, boolean bold) {
        JLabel l = new JLabel(s);
        l.setForeground(color);
        l.setFont(new Font("Times New Roman", bold ? Font.BOLD : Font.PLAIN, size));
        return l;
    }

    private static JLabel valueLabel(String s) {
        JLabel l = label(s, 13, TEXT, false);
        l.setHorizontalAlignment(SwingConstants.RIGHT);
        return l;
    }

    private static JTextField field(String s, boolean editable) {
        JTextField t = new JTextField(s);
        t.setFont(FONT);
        t.setEditable(editable);
        t.setBackground(editable ? Color.WHITE : new Color(246, 247, 249));
        t.setBorder(new CompoundBorder(new LineBorder(new Color(225, 229, 234)), new EmptyBorder(4, 7, 4, 7)));
        return t;
    }

    private static JButton outlineButton(String name) {
        JButton b = new JButton(name);
        b.setFont(FONT);
        b.setForeground(DARK_PINK);
        b.setBackground(Color.WHITE);
        b.setFocusPainted(false);
        b.setBorder(new LineBorder(PINK, 1));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    private static JButton primaryButton(String name) {
        JButton b = outlineButton(name);
        b.setForeground(Color.WHITE);
        b.setBackground(PINK);
        b.setOpaque(true);
        return b;
    }

    private interface Callback {
        void run();
    }

    private static class SimpleDocumentListener implements javax.swing.event.DocumentListener {
        private final Callback callback;

        SimpleDocumentListener(Callback callback) {
            this.callback = callback;
        }

        public void insertUpdate(javax.swing.event.DocumentEvent e) {
            callback.run();
        }

        public void removeUpdate(javax.swing.event.DocumentEvent e) {
            callback.run();
        }

        public void changedUpdate(javax.swing.event.DocumentEvent e) {
            callback.run();
        }
    }
    // Chạy riêng để xem giao diện, không cần SQL Server

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("PetPlus - Thanh toán");
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.setContentPane(new pnlThanhToan());
            frame.setSize(1100, 680);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
