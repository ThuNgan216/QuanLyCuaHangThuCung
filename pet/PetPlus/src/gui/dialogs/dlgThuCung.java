package gui.dialogs;

import entity.KhachHang;
import entity.ThuCung;
import service.ThuCungService;
import utils.ThongBao;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.time.YearMonth;
import java.time.DayOfWeek;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.List;

/** Form thêm/sửa thông tin thú cưng. Ngày sinh nhập theo yyyy-MM-dd. */
public class dlgThuCung extends JDialog {
    private final JTextField txtMa = new JTextField(18);
    private final JTextField txtTen = new JTextField(18);
    private final JComboBox<String> cboLoai = new JComboBox<>(new String[]{"Chó", "Mèo", "Chim", "Thỏ", "Hamster", "Khác"});
    private final JComboBox<String> cboGiong = new JComboBox<>();
    private final JRadioButton radDuc = new JRadioButton("Đực");
    private final JRadioButton radCai = new JRadioButton("Cái");
    private final JTextField txtCanNang = new JTextField(18);
    private final JTextField txtNgaySinh = new JTextField(18);
    private final JButton btnChonNgaySinh = new JButton("Chọn ngày...");
    private final JTextField txtSucKhoe = new JTextField(18);
    private final JTextField txtTiemChung = new JTextField(18);
    private final JComboBox<KhachHang> cboKhachHang = new JComboBox<>();
    private final JTextField txtTimKhachHang = new JTextField(18);
    private List<KhachHang> danhSachKhachHang = new java.util.ArrayList<>();
    private final ThuCungService service = new ThuCungService();
    private boolean saved;
    private final boolean dangSua;

    public dlgThuCung(Window parent, ThuCung thuCung) {
        super(parent, thuCung == null ? "Thêm thú cưng" : "Cập nhật thú cưng", ModalityType.APPLICATION_MODAL);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        this.dangSua = thuCung != null;
        setLayout(new BorderLayout(12, 12));
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(16, 18, 8, 18));
        GridBagConstraints c = new GridBagConstraints(); c.insets = new Insets(5, 6, 5, 6); c.fill = GridBagConstraints.HORIZONTAL;
        int r = 0;
        addRow(form, c, r++, "Mã thú cưng *", txtMa);
        addRow(form, c, r++, "Tên thú cưng *", txtTen);
        addRow(form, c, r++, "Loài *", cboLoai);
        addRow(form, c, r++, "Giống", cboGiong);
        ButtonGroup nhomGioiTinh = new ButtonGroup();
        nhomGioiTinh.add(radDuc); nhomGioiTinh.add(radCai);
        JPanel pnlGioiTinh = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        pnlGioiTinh.add(radDuc); pnlGioiTinh.add(radCai);
        addRow(form, c, r++, "Giới tính", pnlGioiTinh);
        addRow(form, c, r++, "Cân nặng (kg)", txtCanNang);
        txtNgaySinh.setEditable(false);
        JPanel pnlNgaySinh = new JPanel(new BorderLayout(6, 0));
        pnlNgaySinh.add(txtNgaySinh, BorderLayout.CENTER);
        pnlNgaySinh.add(btnChonNgaySinh, BorderLayout.EAST);
        addRow(form, c, r++, "Ngày sinh", pnlNgaySinh);
        btnChonNgaySinh.addActionListener(e -> moChonNgaySinh());
        cboLoai.addActionListener(e -> capNhatGiong());
        addRow(form, c, r++, "Tình trạng sức khỏe", txtSucKhoe);
        addRow(form, c, r++, "Tiêm chủng", txtTiemChung);
        JPanel pnlChonKhachHang = new JPanel(new BorderLayout(5, 5));
        pnlChonKhachHang.add(txtTimKhachHang, BorderLayout.NORTH);
        pnlChonKhachHang.add(cboKhachHang, BorderLayout.CENTER);
        txtTimKhachHang.setToolTipText("Nhập mã, tên hoặc số điện thoại để tìm khách hàng");
        addRow(form, c, r++, "Tìm khách hàng sở hữu *", pnlChonKhachHang);
        txtTimKhachHang.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { locKhachHang(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { locKhachHang(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { locKhachHang(); }
        });
        add(form, BorderLayout.CENTER);
        JButton btnLuu = new JButton("Lưu"); JButton btnHuy = new JButton("Hủy");
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT)); actions.add(btnLuu); actions.add(btnHuy); actions.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 14));
        add(actions, BorderLayout.SOUTH);
        taiKhachHang();
        capNhatGiong();
        if (thuCung != null) napDuLieu(thuCung); else txtMa.setText(taoMaTuDong());
        txtMa.setEditable(thuCung == null);
        btnLuu.addActionListener(e -> luu()); btnHuy.addActionListener(e -> dispose());
        getRootPane().setDefaultButton(btnLuu);
        utils.PetPlusTheme.apply(getContentPane());
        pack(); setMinimumSize(new Dimension(470, 570)); setLocationRelativeTo(parent);
    }

    private void addRow(JPanel panel, GridBagConstraints c, int row, String label, Component input) {
        c.gridx = 0; c.gridy = row; c.weightx = 0; panel.add(new JLabel(label), c);
        c.gridx = 1; c.weightx = 1; panel.add(input, c);
    }
    private void taiKhachHang() {
        try {
            List<KhachHang> ds = service.layDanhSachKhachHang();
            danhSachKhachHang.clear(); danhSachKhachHang.addAll(ds);
            for (KhachHang kh : ds) cboKhachHang.addItem(kh);
            cboKhachHang.setRenderer(new DefaultListCellRenderer() {
                @Override public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean selected, boolean focus) {
                    super.getListCellRendererComponent(list, value, index, selected, focus);
                    if (value instanceof KhachHang) { KhachHang kh = (KhachHang) value; setText(kh.getMaKH() + " - " + (kh.getTenKH() == null ? "" : kh.getTenKH())); }
                    return this;
                }
            });
        } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Không tải được danh sách khách hàng: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE); }
    }
    private void locKhachHang() {
        String key = txtTimKhachHang.getText().trim().toLowerCase(Locale.forLanguageTag("vi-VN"));
        KhachHang selected = (KhachHang) cboKhachHang.getSelectedItem();
        cboKhachHang.removeAllItems();
        for (KhachHang kh : danhSachKhachHang) {
            String haystack = (kh.getMaKH() + " " + kh.getTenKH() + " " + kh.getSdt()).toLowerCase(Locale.forLanguageTag("vi-VN"));
            if (key.isEmpty() || haystack.contains(key)) cboKhachHang.addItem(kh);
        }
        if (selected != null) for (int i = 0; i < cboKhachHang.getItemCount(); i++) {
            if (selected.getMaKH().equals(cboKhachHang.getItemAt(i).getMaKH())) { cboKhachHang.setSelectedIndex(i); break; }
        }
    }

    private void napDuLieu(ThuCung t) {
        txtMa.setText(t.getMaThuCung()); txtTen.setText(t.getTenThuCung());
        cboLoai.setSelectedItem(t.getLoai());
        capNhatGiong();
        if (t.getGiong() != null && !t.getGiong().trim().isEmpty()) cboGiong.setSelectedItem(t.getGiong());
        String gioiTinh = v(t.getGioiTinh());
        radDuc.setSelected("Đực".equalsIgnoreCase(gioiTinh) || "Duc".equalsIgnoreCase(gioiTinh) || "Male".equalsIgnoreCase(gioiTinh));
        radCai.setSelected("Cái".equalsIgnoreCase(gioiTinh) || "Cai".equalsIgnoreCase(gioiTinh) || "Female".equalsIgnoreCase(gioiTinh));
        txtCanNang.setText(t.getCanNang() < 0 ? "" : String.valueOf(t.getCanNang()));
        txtNgaySinh.setText(t.getNgaySinh() == null ? "" : t.getNgaySinh().toString());
        txtSucKhoe.setText(v(t.getTinhTrangSK())); txtTiemChung.setText(v(t.getTiemChung()));
        if (t.getMaKH() != null) for (int i=0; i<cboKhachHang.getItemCount(); i++) if (cboKhachHang.getItemAt(i).getMaKH().equals(t.getMaKH().getMaKH())) cboKhachHang.setSelectedIndex(i);
    }
    private String v(String s) { return s == null ? "" : s; }

    private void capNhatGiong() {
        String loai = String.valueOf(cboLoai.getSelectedItem());
        String[] giong;
        switch (loai) {
            case "Mèo": giong = new String[]{"Mèo Anh lông ngắn", "Mèo Anh lông dài", "Mèo Ba Tư", "Mèo Xiêm", "Mèo Bengal", "Mèo ta", "Mèo Scottish Fold", "Mèo Ragdoll", "Mèo Sphynx", "Khác"}; break;
            case "Chó": giong = new String[]{"Poodle", "Corgi", "Husky", "Golden Retriever", "Labrador", "Pomeranian", "Shiba Inu", "Chihuahua", "Phốc sóc", "Chó ta", "Khác"}; break;
            case "Chim": giong = new String[]{"Vẹt", "Chào mào", "Yến phụng", "Sáo", "Khác"}; break;
            case "Thỏ": giong = new String[]{"Thỏ Hà Lan", "Thỏ Lionhead", "Thỏ Mini Rex", "Thỏ ta", "Khác"}; break;
            case "Hamster": giong = new String[]{"Bear", "Winter White", "Roborovski", "Campbell", "Khác"}; break;
            default: giong = new String[]{"Khác"};
        }
        Object selected = cboGiong.getSelectedItem();
        String old = selected == null ? "" : selected.toString();
        cboGiong.removeAllItems();
        for (String item : giong) cboGiong.addItem(item);
        if (!old.isEmpty()) cboGiong.setSelectedItem(old);
        if (cboGiong.getSelectedIndex() < 0 && cboGiong.getItemCount() > 0) cboGiong.setSelectedIndex(0);
    }

    private String taoMaTuDong() { return service.taoMaMoi(); }
    /** Mở lịch chọn ngày sinh, không cho nhập ngày bằng tay. */
    private void moChonNgaySinh() {
        LocalDate current;
        try { current = txtNgaySinh.getText().trim().isEmpty() ? LocalDate.now() : LocalDate.parse(txtNgaySinh.getText().trim()); }
        catch (Exception ex) { current = LocalDate.now(); }
        final LocalDate[] selectedDate = { current };
        final YearMonth[] shownMonth = { YearMonth.from(current) };
        JDialog dlg = new JDialog(this, "Chọn ngày sinh", Dialog.ModalityType.APPLICATION_MODAL);
        dlg.setLayout(new BorderLayout(8, 8));
        JPanel calendar = new JPanel(new BorderLayout(5, 5));
        calendar.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));
        JPanel header = new JPanel(new BorderLayout(8, 0));
        JButton prev = new JButton("◀"), next = new JButton("▶");
        JLabel monthLabel = new JLabel("", SwingConstants.CENTER);
        monthLabel.setFont(monthLabel.getFont().deriveFont(Font.BOLD, 15f));
        header.add(prev, BorderLayout.WEST); header.add(monthLabel, BorderLayout.CENTER); header.add(next, BorderLayout.EAST);
        calendar.add(header, BorderLayout.NORTH);
        JPanel daysPanel = new JPanel(new GridLayout(0, 7, 3, 3));
        calendar.add(daysPanel, BorderLayout.CENTER);
        JButton ok = new JButton("Chọn"), cancel = new JButton("Hủy");
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT)); actions.add(ok); actions.add(cancel);
        dlg.add(calendar, BorderLayout.CENTER); dlg.add(actions, BorderLayout.SOUTH);
        Runnable[] render = new Runnable[1];
        render[0] = () -> {
            monthLabel.setText(shownMonth[0].getMonth().getDisplayName(TextStyle.FULL, Locale.forLanguageTag("vi-VN")) + " " + shownMonth[0].getYear());
            daysPanel.removeAll();
            for (String day : new String[]{"T2", "T3", "T4", "T5", "T6", "T7", "CN"}) daysPanel.add(new JLabel(day, SwingConstants.CENTER));
            LocalDate first = shownMonth[0].atDay(1);
            int offset = first.getDayOfWeek().getValue() - DayOfWeek.MONDAY.getValue();
            for (int i = 0; i < offset; i++) daysPanel.add(new JLabel(""));
            for (int d = 1; d <= shownMonth[0].lengthOfMonth(); d++) {
                final LocalDate date = shownMonth[0].atDay(d);
                JButton b = new JButton(Integer.toString(d)); b.setMargin(new Insets(4, 4, 4, 4));
                if (date.equals(selectedDate[0])) { b.setBackground(utils.PetPlusTheme.PRIMARY); b.setForeground(Color.WHITE); }
                b.addActionListener(e -> { selectedDate[0] = date; render[0].run(); }); daysPanel.add(b);
            }
            daysPanel.revalidate(); daysPanel.repaint();
        };
        prev.addActionListener(e -> { shownMonth[0] = shownMonth[0].minusMonths(1); render[0].run(); });
        next.addActionListener(e -> { shownMonth[0] = shownMonth[0].plusMonths(1); render[0].run(); });
        ok.addActionListener(e -> { txtNgaySinh.setText(selectedDate[0].toString()); dlg.dispose(); });
        cancel.addActionListener(e -> dlg.dispose());
        utils.PetPlusTheme.apply(dlg.getContentPane()); render[0].run();
        dlg.pack(); dlg.setMinimumSize(new Dimension(320, 300)); dlg.setLocationRelativeTo(this); dlg.setVisible(true);
    }

    /**
     * Lưu ngay trong dialog (giống các dialog khách hàng, lịch hẹn, dịch vụ): nếu service từ chối thì báo lỗi
     * và giữ dialog mở để người dùng sửa lại, không mất dữ liệu đã nhập. Luật kiểm tra nằm ở ThuCungService.
     */
    private void luu() {
        try {
            String canNangText = txtCanNang.getText().trim();
            float canNang;
            try {
                canNang = canNangText.isEmpty() ? ThuCung.CAN_NANG_TRONG : Float.parseFloat(canNangText.replace(',', '.'));
            } catch (NumberFormatException ex) {
                throw new IllegalArgumentException("Cân nặng phải là số, ví dụ 4.5.");
            }
            String ngayText = txtNgaySinh.getText().trim();
            LocalDate ngay;
            try {
                ngay = ngayText.isEmpty() ? null : LocalDate.parse(ngayText);
            } catch (DateTimeParseException ex) {
                throw new IllegalArgumentException("Ngày sinh nhập theo yyyy-MM-dd, ví dụ 2022-05-20.");
            }
            KhachHang kh = (KhachHang) cboKhachHang.getSelectedItem();
            if (kh == null)
                throw new IllegalArgumentException("Vui lòng chọn khách hàng sở hữu thú cưng.");
            String gioiTinh = radDuc.isSelected() ? "Đực" : (radCai.isSelected() ? "Cái" : "");
            ThuCung t = new ThuCung(txtMa.getText().trim(), txtTen.getText().trim(), String.valueOf(cboLoai.getSelectedItem()),
                    cboGiong.getSelectedItem() == null ? "" : String.valueOf(cboGiong.getSelectedItem()), gioiTinh,
                    canNang, ngay, txtSucKhoe.getText().trim(), txtTiemChung.getText().trim(), kh);

            boolean thanhCong = dangSua ? service.capNhat(t) : service.them(t);
            if (!thanhCong) {
                ThongBao.khongThayDoi(this);
                return;
            }
            duLieu = t;
            saved = true;
            if (dangSua)
                ThongBao.daCapNhat(this, "thú cưng");
            else
                ThongBao.daThem(this, "thú cưng", t.getMaThuCung());
            dispose();
        } catch (RuntimeException ex) {
            ThongBao.xuLy(this, "Không thể lưu thú cưng", ex);
        }
    }
    private ThuCung duLieu;
    public boolean isSaved() { return saved; }
    public ThuCung getThuCung() { return duLieu; }
}