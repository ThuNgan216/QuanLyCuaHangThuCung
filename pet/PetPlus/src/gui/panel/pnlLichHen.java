package gui.panel;

import entity.LichHen;
import entity.TrangThaiLichHen;
import gui.dialogs.dlgLichHen;
import service.LichHenService;
import utils.Auth;
import utils.ThongBao;

import javax.swing.*;
import java.awt.*;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;

/**
 * Quản lý lịch hẹn. Khung màn hình dùng chung ở pnlQuanLyChung; luật nghiệp vụ (máy trạng thái, ca làm việc,
 * trùng lịch) ở LichHenService. Ngoài tìm kiếm còn lọc theo ngày / khung giờ và các nút đổi trạng thái nhanh.
 */
public class pnlLichHen extends pnlQuanLyChung<LichHen> {
    private static final String TAT_CA_GIO = "Tất cả giờ";
    private static final DateTimeFormatter F = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter DATE_F = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final LichHenService service = new LichHenService();
    private final JTextField txtNgay = new JTextField(9);
    private final JButton btnNgay = new JButton("Chọn");
    private final JButton btnLoc = new JButton("Lọc");
    private final JComboBox<String> cboKhungGio = new JComboBox<>();

    private LocalDate ngayChon = LocalDate.now();
    /** Chỉ lọc theo ngày / khung giờ sau khi người dùng chọn ngày hoặc bấm Lọc; mặc định xem tất cả lịch. */
    private boolean locTheoNgay = false;

    public pnlLichHen() {
        super("QUẢN LÝ LỊCH HẸN", "lịch hẹn", "Mã lịch, tên / SĐT khách hàng, tên thú cưng, tên nhân viên hoặc trạng thái",
                new String[] { "Mã lịch hẹn", "Thời gian", "Trạng thái", "Nhân viên", "Mã KH", "Khách hàng",
                        "Mã thú cưng", "Tên thú cưng" });

        txtNgay.setEditable(false);
        txtNgay.setText(ngayChon.format(DATE_F));
        txtNgay.setFont(new Font("Arial", Font.PLAIN, 13));
        txtNgay.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnNgay.setToolTipText("Chọn ngày trên lịch");
        cboKhungGio.setFont(new Font("Arial", Font.PLAIN, 13));
        cboKhungGio.addItem(TAT_CA_GIO);
        for (int h = 0; h < 24; h++) {
            cboKhungGio.addItem(String.format(Locale.ROOT, "%02d:00–%02d:00", h, (h + 1) % 24));
            cboKhungGio.addItem(String.format(Locale.ROOT, "%02d:00–%02d:30", h, h));
            cboKhungGio.addItem(String.format(Locale.ROOT, "%02d:30–%02d:00", h, (h + 1) % 24));
        }
        pnlBoLoc.add(new JLabel("Khung giờ:"));
        pnlBoLoc.add(cboKhungGio);
        pnlBoLoc.add(new JLabel("Ngày:"));
        pnlBoLoc.add(txtNgay);
        pnlBoLoc.add(btnNgay);
        pnlBoLoc.add(btnLoc);

        btnLoc.addActionListener(e -> {
            locTheoNgay = true;
            taiLai();
        });
        btnNgay.addActionListener(e -> moLichChonNgay());
        txtNgay.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                moLichChonNgay();
            }
        });

        // Các nút đổi trạng thái đi theo máy trạng thái của lịch hẹn (xem TrangThaiLichHen.coTheChuyenSang)
        JButton bXacNhan = new JButton("Xác nhận");
        JButton bHoanThanh = new JButton("Hoàn thành");
        JButton bHuy = new JButton("Hủy lịch");
        boolean quyen = coQuyenCapNhat();
        bXacNhan.setVisible(quyen);
        bHoanThanh.setVisible(quyen);
        bHuy.setVisible(quyen);
        pnlNut.add(bXacNhan, 0);
        pnlNut.add(bHoanThanh, 1);
        pnlNut.add(bHuy, 2);
        bXacNhan.addActionListener(e -> doiTrangThai(TrangThaiLichHen.DA_XAC_NHAN, "xác nhận"));
        bHoanThanh.addActionListener(e -> doiTrangThai(TrangThaiLichHen.HOAN_THANH, "đánh dấu hoàn thành"));
        bHuy.addActionListener(e -> doiTrangThai(TrangThaiLichHen.DA_HUY, "hủy"));

        khoiTao();
    }

    /** Lọc theo từ khóa (mọi ngày) hoặc, khi không có từ khóa và đã bật lọc, theo ngày và khung giờ. */
    @Override
    protected List<LichHen> layDuLieu(String tuKhoa) {
        List<LichHen> ds = service.timKiem(tuKhoa);
        if (!locTheoNgay || !tuKhoa.isEmpty())
            return ds;

        LocalDateTime tu = null;
        LocalDateTime den = null;
        String khung = (String) cboKhungGio.getSelectedItem();
        if (khung != null && !TAT_CA_GIO.equals(khung)) {
            String[] parts = khung.split("[–-]");
            tu = ngayChon.atTime(Integer.parseInt(parts[0].substring(0, 2)), Integer.parseInt(parts[0].substring(3, 5)));
            den = ngayChon.atTime(Integer.parseInt(parts[1].substring(0, 2)), Integer.parseInt(parts[1].substring(3, 5)));
            if (!den.isAfter(tu))
                den = den.plusDays(1); // khung giờ đi qua nửa đêm
        }
        final LocalDateTime start = tu;
        final LocalDateTime end = den;
        ds.removeIf(l -> {
            LocalDateTime t = l.getThoiGianHen();
            if (t == null || !t.toLocalDate().equals(ngayChon))
                return true;
            return start != null && (t.isBefore(start) || !t.isBefore(end));
        });
        return ds;
    }

    @Override
    protected Object[] taoDong(LichHen l) {
        return new Object[] { l.getMaLichHen(),
                l.getThoiGianHen() == null ? "" : l.getThoiGianHen().format(F),
                l.getTrangThai(),
                l.getMaNV() == null ? "" : l.getMaNV().getTenNV(),
                l.getMaKH() == null ? "" : l.getMaKH().getMaKH(),
                l.getMaKH() == null ? "" : l.getMaKH().getTenKH(),
                l.getMaThuCung() == null ? "" : l.getMaThuCung().getMaThuCung(),
                l.getMaThuCung() == null ? "" : l.getMaThuCung().getTenThuCung() };
    }

    @Override
    protected LichHen timTheoMa(String ma) {
        return service.timTheoMa(ma);
    }

    @Override
    protected boolean moDialog(LichHen cu) {
        dlgLichHen dlg = new dlgLichHen(cuaSoCha(), cu);
        dlg.setVisible(true);
        return dlg.isSaved();
    }

    /** Lịch đã tiếp nhận / hoàn thành / hủy không được sửa: báo ngay thay vì mở form rồi mới lỗi. */
    @Override
    protected void kiemTraTruocKhiSua(LichHen l) {
        service.kiemTraChoPhepSua(l);
    }

    @Override
    protected boolean xoaTheoMa(String ma) {
        return service.xoa(ma);
    }

    @Override
    protected boolean coQuyenCapNhat() {
        return Auth.hasFullAccess() || Auth.hasRole("BAN_HANG");
    }

    @Override
    protected String canhBaoKhiXoa() {
        return "(Chỉ xóa được lịch \"Chờ xác nhận\" hoặc \"Đã hủy\"; lịch đã xác nhận hãy dùng Hủy lịch.)";
    }

    @Override
    protected void datLaiBoLoc() {
        locTheoNgay = false;
        ngayChon = LocalDate.now();
        txtNgay.setText(ngayChon.format(DATE_F));
        cboKhungGio.setSelectedIndex(0);
    }

    /**
     * Đổi nhanh trạng thái lịch hẹn đang chọn. Service kiểm tra máy trạng thái nên thao tác sai
     * (ví dụ hoàn thành lịch chưa tới giờ, hủy lịch đã tiếp nhận) sẽ được báo lỗi rõ ràng.
     */
    private void doiTrangThai(TrangThaiLichHen moi, String tenThaoTac) {
        LichHen l = docDongChon("xử lý");
        if (l == null)
            return;
        if (!ThongBao.xacNhan(this, "Bạn có chắc muốn " + tenThaoTac + " lịch hẹn " + l.getMaLichHen() + "?"))
            return;
        try {
            service.doiTrangThai(l.getMaLichHen(), moi);
            taiLai();
        } catch (RuntimeException e) {
            ThongBao.xuLy(this, "Không thể đổi trạng thái lịch hẹn", e);
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
                            Locale.forLanguageTag("vi-VN")) + " " + shown[0].getYear());

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

            locTheoNgay = true;
            taiLai();
        });

        cancel.addActionListener(e -> dlg.dispose());

        render[0].run();

        dlg.pack();

        dlg.setMinimumSize(
                new Dimension(310, 300));

        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);

    }
}
