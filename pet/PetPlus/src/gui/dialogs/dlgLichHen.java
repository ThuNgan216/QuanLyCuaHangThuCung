package gui.dialogs;

import entity.*;
import service.LichHenService;
import javax.swing.*;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.YearMonth;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;
import java.util.List;

public class dlgLichHen extends JDialog {
    private final JTextField txtMa = new JTextField(18), txtThoiGian = new JTextField(18);
    private final JTextField txtTimKH = new JTextField(18);
    private final JButton btnChonThoiGian = new JButton("Chọn ngày, giờ...");
    private final JComboBox<TrangThaiLichHen> cboTrangThai = new JComboBox<>(TrangThaiLichHen.chonDuoc());
    private final JComboBox<String> cboNV = new JComboBox<>(), cboKH = new JComboBox<>(),
            cboThuCung = new JComboBox<>();
    private final Map<String, NhanVien> nhanVienMap = new LinkedHashMap<>();
    private final Map<String, KhachHang> khachHangMap = new LinkedHashMap<>();
    private final Map<String, ThuCung> thuCungMap = new LinkedHashMap<>();
    private final LichHenService service = new LichHenService();
    private final LichHen duLieu;
    private boolean saved = false;
    private boolean dangNap = false;
    private static final DateTimeFormatter F = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public dlgLichHen(Window owner, LichHen lichHen) {
        super(owner, lichHen == null ? "Thêm lịch hẹn" : "Cập nhật lịch hẹn", ModalityType.APPLICATION_MODAL);
        this.duLieu = lichHen;
        khoiTao();
    }

    private void khoiTao() {
        setLayout(new BorderLayout(10, 10));
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(15, 18, 8, 18));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(5, 5, 5, 5);
        g.fill = GridBagConstraints.HORIZONTAL;
        g.weightx = 1;
        int y = 0;
        txtMa.setEditable(false);
        txtMa.setText(duLieu == null ? service.taoMaMoi() : duLieu.getMaLichHen());
        themDong(form, g, y++, "Mã lịch hẹn:", txtMa);
        txtThoiGian.setEditable(false);
        JPanel pt = new JPanel(new BorderLayout(6, 0));
        pt.add(txtThoiGian, BorderLayout.CENTER);
        pt.add(btnChonThoiGian, BorderLayout.EAST);
        themDong(form, g, y++, "Thời gian hẹn:", pt);
        btnChonThoiGian.addActionListener(e -> moChonNgayGio());
        themDong(form, g, y++, "Trạng thái:", cboTrangThai);
        themDong(form, g, y++, "Nhân viên:", cboNV);
        JPanel pk = new JPanel(new BorderLayout(4, 4));
        pk.add(txtTimKH, BorderLayout.NORTH);
        pk.add(cboKH, BorderLayout.CENTER);
        themDong(form, g, y++, "Tìm khách (tên/SĐT):", pk);
        themDong(form, g, y++, "Thú cưng của khách:", cboThuCung);
        try {
            for (NhanVien nv : service.layNhanVien()) {
                String label = nv.getMaNV() + " - " + nv.getTenNV();
                nhanVienMap.put(label, nv);
                cboNV.addItem(label);
            }
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi tải nhân viên", JOptionPane.ERROR_MESSAGE);
        }
        txtTimKH.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                napKhachHang();
            }

            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                napKhachHang();
            }

            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                napKhachHang();
            }
        });
        cboKH.addActionListener(e -> {
            if (!dangNap)
                napThuCung();
        });
        napKhachHang();
        if (duLieu != null) {
            if (duLieu.getThoiGianHen() != null)
                txtThoiGian.setText(duLieu.getThoiGianHen().format(F));
            TrangThaiLichHen ttCu = TrangThaiLichHen.tuTen(duLieu.getTrangThai());
            if (ttCu != null && ttCu.doTiepNhanQuanLy()) {
                // Do module tiếp nhận quản lý: hiển thị đúng giá trị, không cho đổi tay
                cboTrangThai.addItem(ttCu);
                cboTrangThai.setSelectedItem(ttCu);
                cboTrangThai.setEnabled(false);
            } else {
                cboTrangThai.setSelectedItem(ttCu);
            }
            if (duLieu.getMaNV() != null) {
                for (String k : nhanVienMap.keySet())
                    if (nhanVienMap.get(k).getMaNV().equals(duLieu.getMaNV().getMaNV()))
                        cboNV.setSelectedItem(k);
            }
            if (duLieu.getMaKH() != null) {
                txtTimKH.setText(duLieu.getMaKH().getTenKH() == null ? "" : duLieu.getMaKH().getTenKH());
                napKhachHang();
                for (String k : khachHangMap.keySet())
                    if (khachHangMap.get(k).getMaKH().equals(duLieu.getMaKH().getMaKH()))
                        cboKH.setSelectedItem(k);
                napThuCung();
                if (duLieu.getMaThuCung() != null)
                    for (String k : thuCungMap.keySet())
                        if (thuCungMap.get(k).getMaThuCung().equals(duLieu.getMaThuCung().getMaThuCung()))
                            cboThuCung.setSelectedItem(k);
            }
        } else
            txtThoiGian.setText(LocalDateTime.now().plusDays(1).withSecond(0).withNano(0).format(F));
        JButton save = new JButton("Lưu"), cancel = new JButton("Hủy");
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.add(save);
        buttons.add(cancel);
        save.addActionListener(e -> luu());
        cancel.addActionListener(e -> dispose());
        add(form, BorderLayout.CENTER);
        add(buttons, BorderLayout.SOUTH);
        utils.PetPlusTheme.apply(getContentPane());
        pack();
        setMinimumSize(new Dimension(520, 340));
        setLocationRelativeTo(getOwner());
    }

    private void themDong(JPanel p, GridBagConstraints g, int y, String label, JComponent c) {
        g.gridx = 0;
        g.gridy = y;
        g.weightx = 0;
        p.add(new JLabel(label), g);
        g.gridx = 1;
        g.weightx = 1;
        p.add(c, g);
    }

    private void napKhachHang() {
        String old = (String) cboKH.getSelectedItem();
        dangNap = true;
        try {
            cboKH.removeAllItems();
            khachHangMap.clear();
            for (KhachHang kh : service.timKhachHang(txtTimKH.getText())) {
                String label = kh.getTenKH() + " - " + (kh.getSdt() == null ? "Chưa có SĐT" : kh.getSdt()) + " ["
                        + kh.getMaKH() + "]";
                khachHangMap.put(label, kh);
                cboKH.addItem(label);
            }
            if (old != null && khachHangMap.containsKey(old))
                cboKH.setSelectedItem(old);
        } finally {
            dangNap = false;
        }
        napThuCung();
    }

    private void napThuCung() {
        String old = (String) cboThuCung.getSelectedItem();
        cboThuCung.removeAllItems();
        thuCungMap.clear();
        String khLabel = (String) cboKH.getSelectedItem();
        KhachHang kh = khachHangMap.get(khLabel);
        if (kh == null)
            return;
        for (ThuCung tc : service.layThuCungTheoKhach(kh.getMaKH(), "")) {
            String label = tc.getTenThuCung() + " - " + tc.getLoai() + " - " + tc.getGiong() + " [" + tc.getMaThuCung()
                    + "]";
            thuCungMap.put(label, tc);
            cboThuCung.addItem(label);
        }
        if (old != null && thuCungMap.containsKey(old))
            cboThuCung.setSelectedItem(old);
    }

    /** Mở lịch chọn ngày và giờ; người dùng không cần gõ thời gian bằng tay. */
    private void moChonNgayGio() {
        LocalDateTime hienTai;
        try {
            hienTai = txtThoiGian.getText().trim().isEmpty()
                    ? LocalDateTime.now().plusDays(1).withSecond(0).withNano(0)
                    : LocalDateTime.parse(txtThoiGian.getText().trim(), F);
        } catch (Exception ex) {
            hienTai = LocalDateTime.now().plusDays(1).withSecond(0).withNano(0);
        }
        final LocalDateTime banDau = hienTai;
        JDialog dlg = new JDialog(this, "Chọn ngày và giờ hẹn", Dialog.ModalityType.APPLICATION_MODAL);
        dlg.setLayout(new BorderLayout(8, 8));
        JPanel calendar = new JPanel(new BorderLayout(5, 5));
        calendar.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));
        JPanel header = new JPanel(new BorderLayout(8, 0));
        JButton prev = new JButton("◀");
        JButton next = new JButton("▶");
        JLabel monthLabel = new JLabel("", SwingConstants.CENTER);
        monthLabel.setFont(monthLabel.getFont().deriveFont(Font.BOLD, 15f));
        header.add(prev, BorderLayout.WEST);
        header.add(monthLabel, BorderLayout.CENTER);
        header.add(next, BorderLayout.EAST);
        calendar.add(header, BorderLayout.NORTH);
        JPanel daysPanel = new JPanel(new GridLayout(0, 7, 3, 3));
        calendar.add(daysPanel, BorderLayout.CENTER);
        LocalDate homNay = LocalDate.now();
        LocalDateTime gioHienTai = LocalDateTime.now();
        LocalDate[] selectedDate = { banDau.toLocalDate().isBefore(homNay) ? homNay : banDau.toLocalDate() };
        YearMonth[] shownMonth = { YearMonth.from(selectedDate[0]) };
        JPanel timePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 4));
        timePanel.add(new JLabel("Giờ:"));
        JSpinner hour = new JSpinner(new SpinnerNumberModel(banDau.getHour(), 0, 23, 1));
        hour.setEditor(new JSpinner.NumberEditor(hour, "00"));
        timePanel.add(hour);
        timePanel.add(new JLabel("Phút:"));
        JSpinner minute = new JSpinner(new SpinnerNumberModel(banDau.getMinute(), 0, 59, 1));
        minute.setEditor(new JSpinner.NumberEditor(minute, "00"));
        timePanel.add(minute);
        JButton ok = new JButton("Chọn");
        JButton cancel = new JButton("Hủy");
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actions.add(ok);
        actions.add(cancel);
        JPanel south = new JPanel(new BorderLayout());
        south.add(timePanel, BorderLayout.CENTER);
        south.add(actions, BorderLayout.SOUTH);
        dlg.add(calendar, BorderLayout.CENTER);
        dlg.add(south, BorderLayout.SOUTH);

        Runnable[] renderCalendar = new Runnable[1];
        renderCalendar[0] = new Runnable() {
            @Override
            public void run() {
                monthLabel.setText(
                        shownMonth[0].getMonth().getDisplayName(java.time.format.TextStyle.FULL, new Locale("vi", "VN"))
                                + " " + shownMonth[0].getYear());
                daysPanel.removeAll();
                prev.setEnabled(shownMonth[0].isAfter(YearMonth.from(LocalDate.now())));
                String[] weekdays = { "T2", "T3", "T4", "T5", "T6", "T7", "CN" };
                for (String day : weekdays)
                    daysPanel.add(new JLabel(day, SwingConstants.CENTER));
                LocalDate first = shownMonth[0].atDay(1);
                int offset = first.getDayOfWeek().getValue() - DayOfWeek.MONDAY.getValue();
                for (int i = 0; i < offset; i++)
                    daysPanel.add(new JLabel(""));
                for (int d = 1; d <= shownMonth[0].lengthOfMonth(); d++) {
                    final LocalDate date = shownMonth[0].atDay(d);
                    JButton b = new JButton(Integer.toString(d));
                    b.setMargin(new Insets(4, 4, 4, 4));
                    if (date.equals(selectedDate[0])) {
                        b.setBackground(utils.PetPlusTheme.PRIMARY);
                        b.setForeground(Color.WHITE);
                    }
                    // Không cho chọn ngày đã qua.
                    b.setEnabled(!date.isBefore(LocalDate.now()));
                    b.addActionListener(e -> {
                        selectedDate[0] = date;
                        renderCalendar[0].run();
                    });
                    daysPanel.add(b);
                }
                daysPanel.revalidate();
                daysPanel.repaint();
            }
        };
        prev.addActionListener(e -> {
            YearMonth thangTruoc = shownMonth[0].minusMonths(1);
            if (!thangTruoc.isBefore(YearMonth.from(LocalDate.now()))) {
                shownMonth[0] = thangTruoc;
                renderCalendar[0].run();
            }
        });
        next.addActionListener(e -> {
            shownMonth[0] = shownMonth[0].plusMonths(1);
            renderCalendar[0].run();
        });
        ok.addActionListener(e -> {
            LocalDateTime value = selectedDate[0].atTime((Integer) hour.getValue(), (Integer) minute.getValue());
            if (!value.isAfter(LocalDateTime.now())) {
                JOptionPane.showMessageDialog(dlg,
                        "Thời gian hẹn phải ở tương lai. Vui lòng chọn giờ sau thời điểm hiện tại.",
                        "Thời gian hẹn không hợp lệ", JOptionPane.WARNING_MESSAGE);
                return;
            }
            txtThoiGian.setText(value.format(F));
            dlg.dispose();
        });
        cancel.addActionListener(e -> dlg.dispose());
        utils.PetPlusTheme.apply(dlg.getContentPane());
        renderCalendar[0].run();
        dlg.pack();
        dlg.setMinimumSize(new Dimension(330, 330));
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);
    }

    private void luu() {
        try {
            String nvLabel = (String) cboNV.getSelectedItem();
            String khLabel = (String) cboKH.getSelectedItem();
            String tcLabel = (String) cboThuCung.getSelectedItem();

            if (nvLabel == null)
                throw new IllegalArgumentException("Vui lòng chọn nhân viên.");

            if (khLabel == null)
                throw new IllegalArgumentException("Vui lòng tìm và chọn khách hàng.");

            if (tcLabel == null)
                throw new IllegalArgumentException(
                        "Vui lòng chọn thú cưng của khách hàng.");

            if (txtThoiGian.getText().trim().isEmpty())
                throw new IllegalArgumentException(
                        "Vui lòng chọn ngày và giờ hẹn.");

            LocalDateTime time = LocalDateTime.parse(txtThoiGian.getText().trim(), F);

            boolean doiGio = duLieu == null || duLieu.getThoiGianHen() == null
                    || !duLieu.getThoiGianHen().equals(time);
            if (doiGio && !time.isAfter(LocalDateTime.now()))
                throw new IllegalArgumentException(
                        "Thời gian hẹn phải ở tương lai.");

            NhanVien nv = nhanVienMap.get(nvLabel);
            KhachHang kh = khachHangMap.get(khLabel);
            ThuCung tc = thuCungMap.get(tcLabel);

            if (nv == null || kh == null || tc == null)
                throw new IllegalArgumentException(
                        "Vui lòng chọn lại nhân viên, khách hàng và thú cưng.");

            if (cboTrangThai.getSelectedItem() == null)
                throw new IllegalArgumentException("Vui lòng chọn trạng thái lịch hẹn.");

            LichHen l = new LichHen();
            l.setMaLichHen(txtMa.getText().trim());
            l.setThoiGianHen(time);
            l.setTrangThai(
                    ((TrangThaiLichHen) cboTrangThai.getSelectedItem())
                            .getTenHienThi());
            l.setMaNV(new NhanVien(nv.getMaNV()));
            l.setMaKH(new KhachHang(kh.getMaKH()));

            ThuCung thuCung = new ThuCung();
            thuCung.setMaThuCung(tc.getMaThuCung());
            l.setMaThuCung(thuCung);

            if (duLieu == null) {
                service.them(l);

                JOptionPane.showMessageDialog(
                        this,
                        "Thêm lịch hẹn thành công!",
                        "Thông báo",
                        JOptionPane.INFORMATION_MESSAGE);
            } else {
                service.capNhat(l);

                JOptionPane.showMessageDialog(
                        this,
                        "Cập nhật lịch hẹn thành công!",
                        "Thông báo",
                        JOptionPane.INFORMATION_MESSAGE);
            }

            saved = true;
            dispose();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                    this,
                    ex.getMessage() == null
                            ? "Không thể lưu lịch hẹn."
                            : ex.getMessage(),
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isSaved() {
        return saved;
    }
}
