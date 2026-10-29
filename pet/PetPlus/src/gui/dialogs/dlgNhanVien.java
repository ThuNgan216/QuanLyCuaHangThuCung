package gui.dialogs;

import dao.NhanVienDAO;
import entity.NhanVien;
import entity.TaiKhoan;

import javax.swing.*;
import javax.swing.table.JTableHeader;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import java.awt.*;
import java.sql.SQLException;
import java.util.Arrays;

public class dlgNhanVien extends JDialog {
    private final NhanVien nhanVienCu;
    private NhanVien result;
    private final JTextField txtMa = PetPlusUI.textField();
    private final JTextField txtTen = PetPlusUI.textField();
    private final JTextField txtSDT = PetPlusUI.textField();
    private final JTextField txtCCCD = PetPlusUI.textField();
    private final JTextField txtDiaChi = PetPlusUI.textField();
    private final JTextField txtEmail = PetPlusUI.textField();
    private final JPasswordField txtMatKhau = new JPasswordField();
    private final JComboBox<String> cboChucVu = new JComboBox<>(VaiTroHelper.CHUC_VU);
    private final JComboBox<String> cboTrangThai = new JComboBox<>(new String[] { "Hoạt động", "Ngừng hoạt động" });
    private boolean maNVTaoThatBai;
    private String loiTaoMa;

    public dlgNhanVien(Window owner, NhanVien nhanVienCu) {
        super(owner, nhanVienCu == null ? "Thêm nhân viên" : "Sửa nhân viên", ModalityType.APPLICATION_MODAL);
        this.nhanVienCu = nhanVienCu;
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());
        getContentPane().setBackground(PetPlusUI.WHITE);
        add(PetPlusDialogs.header(nhanVienCu == null ? "THÊM NHÂN VIÊN" : "CẬP NHẬT NHÂN VIÊN",
                nhanVienCu == null ? "Tạo hồ sơ và tài khoản đăng nhập cùng lúc"
                        : "Chỉnh sửa hồ sơ; thông tin đăng nhập quản lý ở menu Tài khoản"),
                BorderLayout.NORTH);

        PetPlusUI.decorateInput(txtMatKhau);
        PetPlusUI.decorateInput(cboChucVu);
        cboChucVu.setPrototypeDisplayValue("Chăm sóc thú cưng");
        PetPlusUI.decorateInput(cboTrangThai);
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(PetPlusUI.WHITE);
        form.setBorder(new EmptyBorder(16, 28, 16, 28));
        int r = 0;
        addRow(form, r++, "Mã nhân viên *", txtMa);
        addRow(form, r++, "Họ và tên *", txtTen);
        addRow(form, r++, "Số điện thoại *", txtSDT);
        addRow(form, r++, "CCCD", txtCCCD);
        addRow(form, r++, "Địa chỉ", txtDiaChi);
        addRow(form, r++, "Email", txtEmail);
        addRow(form, r++, "Chức vụ", cboChucVu);
        addRow(form, r++, "Trạng thái", cboTrangThai);
        if (nhanVienCu == null) {
            addRow(form, r++, "Mật khẩu mới *", txtMatKhau);
            JCheckBox show = new JCheckBox("Hiện mật khẩu");
            show.setBackground(PetPlusUI.WHITE);
            char echo = txtMatKhau.getEchoChar();
            show.addActionListener(e -> txtMatKhau.setEchoChar(show.isSelected() ? (char) 0 : echo));
            GridBagConstraints gc = new GridBagConstraints();
            gc.gridx = 1;
            gc.gridy = r;
            gc.anchor = GridBagConstraints.WEST;
            form.add(show, gc);
        }
        JScrollPane scroll = new JScrollPane(form);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(14);
        add(scroll, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 14));
        bottom.setBackground(PetPlusUI.BACKGROUND);
        bottom.setBorder(new EmptyBorder(0, 20, 0, 20));
        JButton btnHuy = PetPlusUI.button("Hủy", false);
        JButton btnLuu = PetPlusUI.button(nhanVienCu == null ? "Thêm nhân viên" : "Lưu thay đổi", true);
        btnHuy.addActionListener(e -> dispose());
        btnLuu.addActionListener(e -> luu());
        bottom.add(btnHuy);
        bottom.add(btnLuu);
        add(bottom, BorderLayout.SOUTH);
        getRootPane().setDefaultButton(btnLuu);

        if (nhanVienCu != null)
            napDuLieuCu();
        else
            caiDatMaNVMoi();
        setSize(700, 680);
        setMinimumSize(new Dimension(580, 560));
        setLocationRelativeTo(owner);
    }

    private void addRow(JPanel panel, int y, String label, JComponent field) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = y;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(0, 0, 12, 20);
        panel.add(PetPlusUI.label(label), c);
        c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = y;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(0, 0, 12, 0);
        panel.add(field, c);
    }

    private void napDuLieuCu() {
        TaiKhoan tk = nhanVienCu.getMaTK();
        txtMa.setText(nhanVienCu.getMaNV());
        txtMa.setEditable(false);
        txtTen.setText(value(nhanVienCu.getTenNV()));
        txtSDT.setText(value(nhanVienCu.getSdt()));
        txtCCCD.setText(value(nhanVienCu.getCccd()));
        txtDiaChi.setText(value(nhanVienCu.getDiaChi()));
        txtEmail.setText(value(nhanVienCu.getEmail()));
        // Chức vụ cũ có thể là "Thu ngân", "Thủ kho"...; đưa về 4 nhóm mới.
        String cv = VaiTroHelper.chuanHoaChucVu(nhanVienCu.getChucVu());
        if (cv == null && nhanVienCu.getMaTK() != null) {
            cv = VaiTroHelper.chuanHoaChucVu(nhanVienCu.getMaTK().getVaiTro());
        }
        cboChucVu.setSelectedItem(cv);
        if (cv == null)
            cboChucVu.setSelectedIndex(-1);
        if (tk != null) {
            cboTrangThai.setSelectedItem(tk.getTrangThai());
        }
        txtMa.setToolTipText("Mã nhân viên đã được tạo và không thể sửa.");
    }

    private void caiDatMaNVMoi() {
        txtMa.setEditable(false);
        txtMa.setToolTipText("Mã nhân viên được tự sinh từ dữ liệu hiện có.");
        try {
            txtMa.setText(new NhanVienDAO().taoMaNVMoi());
        } catch (SQLException ex) {
            maNVTaoThatBai = true;
            loiTaoMa = ex.getMessage();
            txtMa.setText("");
            txtMa.setToolTipText("Không thể tự sinh mã. Kiểm tra kết nối database.");
        }
    }

    private void luu() {
        if (maNVTaoThatBai) {
            PetPlusDialogs.error(this, "Không thể tạo mã nhân viên",
                    "Không đọc được mã hiện có từ database. Hãy kiểm tra kết nối rồi mở lại form."
                            + (loiTaoMa == null ? "" : "\nChi tiết: " + loiTaoMa));
            return;
        }
        String ma = txtMa.getText().trim();
        String ten = txtTen.getText().trim();
        String sdt = txtSDT.getText().trim();
        String cccd = txtCCCD.getText().trim();
        String email = txtEmail.getText().trim();
        if (ma.isEmpty() || ten.isEmpty() || sdt.isEmpty()) {
            PetPlusDialogs.error(this, "Dữ liệu chưa đủ", "Vui lòng nhập họ tên và số điện thoại.");
            return;
        }
        if (!ma.matches("[a-zA-Z0-9_-]{2,25}")) {
            PetPlusDialogs.error(this, "Mã nhân viên", "Mã phải dài 2–25 ký tự, chỉ gồm chữ không dấu, số, _ hoặc -.");
            return;
        }
        if (!sdt.matches("0[0-9]{9}")) {
            PetPlusDialogs.error(this, "Số điện thoại", "Số điện thoại phải bắt đầu bằng 0 và gồm đúng 10 chữ số.");
            return;
        }
        if (!ten.matches("[\\p{L}]+(?:\\s+[\\p{L}]+)*")) {
            PetPlusDialogs.error(this, "Họ và tên", "Họ tên chỉ được chứa chữ cái và khoảng trắng.");
            return;
        }
        if (!cccd.isEmpty() && !cccd.matches("[0-9]{12}")) {
            PetPlusDialogs.error(this, "CCCD", "CCCD gồm 12 chữ số hoặc để trống.");
            return;
        }
        if (!email.isEmpty() && !email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            PetPlusDialogs.error(this, "Email", "Email chưa đúng định dạng.");
            return;
        }
        String cv = (String) cboChucVu.getSelectedItem();
        if (cv == null || VaiTroHelper.chuanHoaChucVu(cv) == null) {
            PetPlusDialogs.error(this, "Chức vụ", "Hãy chọn 1 trong 4 chức vụ được hỗ trợ.");
            return;
        }
        String tt = (String) cboTrangThai.getSelectedItem();
        TaiKhoan tk;
        if (nhanVienCu == null) {
            char[] pass = txtMatKhau.getPassword();
            if (pass.length < 8) {
                Arrays.fill(pass, '\0');
                PetPlusDialogs.error(this, "Mật khẩu",
                        "Mật khẩu tạo mới cần ít nhất 8 ký tự (không dùng mật khẩu 123).");
                return;
            }
            tk = new TaiKhoan("TK" + ma, ma, new String(pass), VaiTroHelper.tuChucVu(cv), tt);
            Arrays.fill(pass, '\0');
        } else {
            TaiKhoan old = nhanVienCu.getMaTK();
            String quyen = VaiTroHelper.giuMaNeuKhongDoi(old.getVaiTro(), cv);
            tk = new TaiKhoan(old.getMaTK(), old.getTenDangNhap(), old.getMatKhau(), quyen, tt);
        }
        result = new NhanVien(ma, ten, sdt, nullable(cccd), nullable(txtDiaChi.getText()),
                nullable(email), cv, tk);
        dispose();
    }

    private String value(String s) {
        return s == null ? "" : s;
    }

    private String nullable(String s) {
        return s == null || s.trim().isEmpty() ? null : s.trim();
    }

    public NhanVien open() {
        setVisible(true);
        return result;
    }

    public static final class PetPlusUI {
        public static final Color PRIMARY = new Color(245, 70, 113);
        public static final Color PRIMARY_DARK = new Color(220, 27, 86);
        public static final Color BACKGROUND = new Color(255, 247, 250);
        public static final Color WHITE = Color.WHITE;
        public static final Color TEXT = new Color(32, 44, 64);
        public static final Color MUTED = new Color(118, 126, 140);
        public static final Color BORDER = new Color(237, 217, 225);
        public static final Color LIGHT_PINK = new Color(255, 236, 243);

        private PetPlusUI() {
        }

        public static Font font(int style, int size) {
            return new Font("Arial", style, size);
        }

        public static JPanel panel(LayoutManager manager, Color color) {
            JPanel p = new JPanel(manager);
            p.setBackground(color);
            return p;
        }

        public static JButton button(String text, boolean primary) {
            JButton b = new JButton(text) {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    Color bg = primary ? (getModel().isRollover() ? PRIMARY_DARK : PRIMARY)
                            : (getModel().isRollover() ? LIGHT_PINK : WHITE);
                    if (!isEnabled())
                        bg = new Color(230, 230, 230);
                    g2.setColor(bg);
                    g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                    if (!primary) {
                        g2.setColor(BORDER);
                        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                    }
                    g2.dispose();
                    super.paintComponent(g);
                }
            };
            b.setFont(font(Font.BOLD, 13));
            b.setForeground(primary ? WHITE : TEXT);
            b.setOpaque(false);
            b.setContentAreaFilled(false);
            b.setBorderPainted(false);
            b.setFocusPainted(false);
            b.setRolloverEnabled(true);
            b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            b.setPreferredSize(new Dimension(158, 39));
            return b;
        }

        public static JTextField textField() {
            JTextField field = new JTextField();
            decorateInput(field);
            return field;
        }

        public static void decorateInput(JComponent field) {
            field.setFont(font(Font.PLAIN, 13));
            field.setBackground(WHITE);
            field.setForeground(TEXT);
            field.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(BORDER), new EmptyBorder(8, 11, 8, 11)));
            field.setPreferredSize(new Dimension(270, 38));
        }

        public static JLabel label(String text) {
            JLabel label = new JLabel(text);
            label.setFont(font(Font.BOLD, 13));
            label.setForeground(TEXT);
            return label;
        }

        public static void styleTable(JTable table) {
            table.setFont(font(Font.PLAIN, 13));
            table.setForeground(TEXT);
            table.setBackground(WHITE);
            table.setGridColor(BORDER);
            table.setSelectionBackground(new Color(254, 209, 224));
            table.setSelectionForeground(TEXT);
            table.setRowHeight(37);
            table.setShowVerticalLines(false);
            table.setFillsViewportHeight(true);
            table.setAutoCreateRowSorter(true);
            table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            JTableHeader header = table.getTableHeader();
            header.setFont(font(Font.BOLD, 13));
            header.setForeground(TEXT);
            header.setBackground(LIGHT_PINK);
            header.setPreferredSize(new Dimension(0, 42));
            ((DefaultTableCellRenderer) header.getDefaultRenderer()).setHorizontalAlignment(SwingConstants.LEFT);
        }

        public static JScrollPane scrollTable(JTable table) {
            JScrollPane scroll = new JScrollPane(table);
            scroll.setBorder(BorderFactory.createLineBorder(BORDER));
            scroll.getViewport().setBackground(WHITE);
            return scroll;
        }
    }

    public static final class VaiTroHelper {
        public static final String[] TEN_VAI_TRO = {
                "Bán hàng / Thu ngân", "Kho", "Chăm sóc thú cưng", "Quản lý"
        };
        public static final String[] CHUC_VU = TEN_VAI_TRO.clone();
        public static final String[] MA_VAI_TRO = {
                "BAN_HANG", "KHO", "CHAM_SOC", "QUAN_LY"
        };

        private VaiTroHelper() {
        }

        public static String chuanHoaChucVu(String giaTri) {
            if (giaTri == null)
                return null;

            String s = giaTri.trim();

            for (int i = 0; i < TEN_VAI_TRO.length; i++) {
                if (s.equalsIgnoreCase(TEN_VAI_TRO[i])
                        || s.equalsIgnoreCase(MA_VAI_TRO[i])) {
                    return TEN_VAI_TRO[i];
                }
            }

            return null;
        }

        public static String tuChucVu(String chucVu) {
            String ten = chuanHoaChucVu(chucVu);
            if (ten == null)
                throw new IllegalArgumentException("Chức vụ không hợp lệ: " + chucVu);
            for (int i = 0; i < TEN_VAI_TRO.length; i++)
                if (TEN_VAI_TRO[i].equals(ten))
                    return MA_VAI_TRO[i];
            throw new IllegalArgumentException("Không tìm thấy mã vai trò: " + chucVu);
        }

        public static String tenVaiTro(String ma) {
            String ten = chuanHoaChucVu(ma);
            return ten == null ? (ma == null ? "" : ma) : ten;
        }

        public static String maVaiTro(String ten) {
            if ("ADMIN".equalsIgnoreCase(ten))
                return "ADMIN";
            return tuChucVu(ten);
        }

        public static String giuMaNeuKhongDoi(String maCu, String tenMoi) {
            String tenCu = chuanHoaChucVu(maCu);
            String tenChon = chuanHoaChucVu(tenMoi);
            if (tenCu != null && tenCu.equals(tenChon))
                return maCu;
            return tuChucVu(tenMoi);
        }
    }

    public static final class PetPlusDialogs {
        public static final Color PINK = new Color(245, 70, 113);
        public static final Color PINK_DARK = new Color(217, 25, 85);
        public static final Color TEXT = new Color(34, 44, 61);
        public static final Color BG = new Color(255, 247, 250);
        public static final Color BORDER = new Color(234, 216, 224);

        private PetPlusDialogs() {
        }

        public static JButton button(String title, boolean primary) {
            JButton btn = new JButton(title) {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D gg = (Graphics2D) g.create();
                    gg.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    gg.setColor(isEnabled() ? (primary ? PINK : Color.WHITE) : new Color(210, 210, 210));
                    gg.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                    if (!primary) {
                        gg.setColor(BORDER);
                        gg.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                    }
                    gg.dispose();
                    super.paintComponent(g);
                }
            };
            btn.setOpaque(false);
            btn.setContentAreaFilled(false);
            btn.setBorderPainted(false);
            btn.setFocusPainted(false);
            btn.setForeground(primary ? Color.WHITE : TEXT);
            btn.setFont(new Font("Arial", Font.BOLD, 13));
            btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            btn.setPreferredSize(new Dimension(145, 39));
            return btn;
        }

        public static JTextField field() {
            JTextField f = new JTextField();
            decorateInput(f);
            return f;
        }

        public static void decorateInput(JComponent f) {
            f.setFont(new Font("Arial", Font.PLAIN, 13));
            f.setForeground(TEXT);
            f.setBackground(Color.WHITE);
            f.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(BORDER), new EmptyBorder(8, 10, 8, 10)));
            f.setPreferredSize(new Dimension(260, 38));
        }

        public static JLabel label(String s) {
            JLabel label = new JLabel(s);
            label.setFont(new Font("Arial", Font.BOLD, 13));
            label.setForeground(TEXT);
            return label;
        }

        public static JPanel header(String heading, String subheading) {
            JPanel h = new JPanel();
            h.setLayout(new BoxLayout(h, BoxLayout.Y_AXIS));
            h.setBackground(BG);
            h.setBorder(new EmptyBorder(20, 25, 18, 25));
            JLabel title = new JLabel(heading);
            title.setFont(new Font("Arial", Font.BOLD, 21));
            title.setForeground(PINK_DARK);
            title.setAlignmentX(Component.LEFT_ALIGNMENT);
            JLabel sub = new JLabel(subheading);
            sub.setFont(new Font("Arial", Font.PLAIN, 12));
            sub.setForeground(new Color(105, 107, 118));
            sub.setAlignmentX(Component.LEFT_ALIGNMENT);
            h.add(title);
            h.add(Box.createVerticalStrut(6));
            h.add(sub);
            return h;
        }

        public static boolean confirm(Component parent, String title, String message) {
            return showMessage(parent, title, message, true, false);
        }

        public static void info(Component parent, String title, String message) {
            showMessage(parent, title, message, false, false);
        }

        public static void error(Component parent, String title, String message) {
            showMessage(parent, title, message, false, true);
        }

        private static boolean showMessage(Component parent, String title, String message,
                boolean ask, boolean isError) {
            Window owner = SwingUtilities.getWindowAncestor(parent);
            JDialog dialog = new JDialog(owner, title, Dialog.ModalityType.APPLICATION_MODAL);
            dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            dialog.setLayout(new BorderLayout());
            dialog.getContentPane().setBackground(Color.WHITE);

            dialog.add(header(title, ask ? "Vui lòng xác nhận thao tác" : "Thông báo từ PetPlus"), BorderLayout.NORTH);
            JTextArea content = new JTextArea(message);
            content.setEditable(false);
            content.setOpaque(false);
            content.setLineWrap(true);
            content.setWrapStyleWord(true);
            content.setFont(new Font("Arial", Font.PLAIN, 14));
            content.setForeground(isError ? new Color(170, 38, 45) : TEXT);
            content.setBorder(new EmptyBorder(12, 25, 18, 25));
            dialog.add(content, BorderLayout.CENTER);

            final boolean[] accepted = { false };
            JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
            footer.setBackground(BG);
            if (ask) {
                JButton cancel = button("Hủy", false);
                cancel.addActionListener(e -> dialog.dispose());
                footer.add(cancel);
            }
            JButton ok = button(ask ? "Xác nhận" : "Đóng", true);
            ok.addActionListener(e -> {
                accepted[0] = true;
                dialog.dispose();
            });
            footer.add(ok);
            dialog.add(footer, BorderLayout.SOUTH);
            dialog.getRootPane().setDefaultButton(ok);
            dialog.setSize(475, 260);
            dialog.setMinimumSize(new Dimension(430, 240));
            dialog.setLocationRelativeTo(parent);
            dialog.setVisible(true);
            return accepted[0];
        }
    }
}
