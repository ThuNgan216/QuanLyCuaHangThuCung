package gui.panel;

import entity.PhieuTiepNhanThuCung;
import gui.dialogs.dlgTiepNhanThuCung;
import service.TiepNhanThuCungService;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/** Danh sách check-in thực tế của thú cưng tại cửa hàng. */
public class pnlTiepNhanThuCung extends JPanel {
    private static final DateTimeFormatter THOI_GIAN = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private final TiepNhanThuCungService service = new TiepNhanThuCungService();
    private final JTextField txtTim = new JTextField(22);
    private final DefaultTableModel model = new DefaultTableModel(new Object[] {
            "Mã phiếu", "Hình thức", "Giờ tiếp nhận", "Lịch hẹn", "Khách hàng", "Số điện thoại",
            "Thú cưng", "Dịch vụ / yêu cầu", "Nhân viên", "Hẹn trả", "Phí dự kiến", "Trạng thái"
    }, 0) {
        @Override public boolean isCellEditable(int row, int column) { return false; }
    };
    private final JTable table = new JTable(model);
    private List<PhieuTiepNhanThuCung> danhSachDangHienThi = new ArrayList<>();

    public pnlTiepNhanThuCung() {
        setLayout(new BorderLayout(10, 10));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JLabel title = new JLabel("TIẾP NHẬN THÚ CƯNG");
        title.setFont(new Font("Arial", Font.BOLD, 23));
        title.setForeground(new Color(224, 25, 91));

        JPanel top = new JPanel(new BorderLayout(8, 8));
        top.setBackground(Color.WHITE);
        top.add(title, BorderLayout.NORTH);
        JPanel search = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        search.setBackground(Color.WHITE);
        JButton btnTim = new JButton("Tìm kiếm");
        JButton btnLamMoi = new JButton("Làm mới");
        search.add(new JLabel("Mã phiếu / khách / thú cưng / lịch hẹn:"));
        search.add(txtTim);
        search.add(btnTim);
        search.add(btnLamMoi);
        top.add(search, BorderLayout.CENTER);
        add(top, BorderLayout.NORTH);

        table.setRowHeight(27);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoCreateRowSorter(true);
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        add(new JScrollPane(table), BorderLayout.CENTER);

        JButton btnTheoLich = new JButton("Tiếp nhận theo lịch hẹn");
        JButton btnKhachTrucTiep = new JButton("Tiếp nhận khách trực tiếp");
        JButton btnTrangThai = new JButton("Cập nhật trạng thái");
        JButton btnChiTiet = new JButton("Xem phiếu");
        JButton btnTai = new JButton("Tải danh sách");
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 5));
        bottom.setBackground(Color.WHITE);
        bottom.add(btnTai);
        bottom.add(btnChiTiet);
        bottom.add(btnTrangThai);
        bottom.add(btnTheoLich);
        bottom.add(btnKhachTrucTiep);
        add(bottom, BorderLayout.SOUTH);

        btnTim.addActionListener(e -> taiDanhSach(txtTim.getText()));
        txtTim.addActionListener(e -> taiDanhSach(txtTim.getText()));
        btnLamMoi.addActionListener(e -> { txtTim.setText(""); taiDanhSach(""); });
        btnTai.addActionListener(e -> taiDanhSach(txtTim.getText()));
        btnTheoLich.addActionListener(e -> moTiepNhan(true));
        btnKhachTrucTiep.addActionListener(e -> moTiepNhan(false));
        btnTrangThai.addActionListener(e -> capNhatTrangThai());
        btnChiTiet.addActionListener(e -> xemPhieu());
        table.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) xemPhieu();
            }
        });

        taiDanhSach("");
        utils.PetPlusTheme.apply(this);
    }

    private void taiDanhSach(String tuKhoa) {
        try {
            danhSachDangHienThi = service.layDanhSach(tuKhoa);
            model.setRowCount(0);
            for (PhieuTiepNhanThuCung phieu : danhSachDangHienThi) {
                String hinhThuc = phieu.getMaLichHen() == null ? "Khách trực tiếp" : "Theo lịch hẹn";
                model.addRow(new Object[] {
                        phieu.getMaTiepNhan(), hinhThuc, dinhDang(phieu.getThoiGianTiepNhan()),
                        phieu.getMaLichHen() == null ? "—" : phieu.getMaLichHen(),
                        phieu.getTenKH(), phieu.getSdtKH(), phieu.getTenThuCung(), phieu.getDichVuYeuCau(),
                        phieu.getTenNV(), dinhDang(phieu.getGioHenTra()), tien(phieu.getPhiDuKien()), phieu.getTrangThai()
                });
            }
        } catch (RuntimeException ex) {
            model.setRowCount(0);
            danhSachDangHienThi.clear();
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Không thể tải phiếu tiếp nhận",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void moTiepNhan(boolean theoLichHen) {
        dlgTiepNhanThuCung dialog = new dlgTiepNhanThuCung(
                SwingUtilities.getWindowAncestor(this), theoLichHen);
        dialog.setVisible(true);
        if (dialog.isDaLuu()) taiDanhSach(txtTim.getText());
    }

    private void capNhatTrangThai() {
        PhieuTiepNhanThuCung phieu = layDongChon();
        if (phieu == null) {
            JOptionPane.showMessageDialog(this, "Chọn phiếu cần cập nhật trạng thái.");
            return;
        }
        String[] trangThai = service.TRANG_THAI.toArray(new String[0]);
        Object chon = JOptionPane.showInputDialog(this, "Trạng thái mới cho " + phieu.getMaTiepNhan() + ":",
                "Cập nhật trạng thái", JOptionPane.QUESTION_MESSAGE, null, trangThai, phieu.getTrangThai());
        if (chon == null) return;
        try {
            service.capNhatTrangThai(phieu.getMaTiepNhan(), chon.toString());
            taiDanhSach(txtTim.getText());
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Không thể cập nhật trạng thái",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void xemPhieu() {
        PhieuTiepNhanThuCung phieu = layDongChon();
        if (phieu == null) {
            JOptionPane.showMessageDialog(this, "Chọn phiếu cần xem.");
            return;
        }
        String noiDung = "Mã phiếu: " + phieu.getMaTiepNhan()
                + "\nHình thức: " + (phieu.getMaLichHen() == null ? "Khách trực tiếp" : "Theo lịch hẹn " + phieu.getMaLichHen())
                + "\nThời gian tiếp nhận: " + dinhDang(phieu.getThoiGianTiepNhan())
                + "\nKhách hàng: " + phieu.getTenKH() + " (" + phieu.getSdtKH() + ")"
                + "\nThú cưng: " + phieu.getTenThuCung() + " - " + phieu.getLoaiThuCung()
                + "\nDịch vụ/yêu cầu: " + phieu.getDichVuYeuCau()
                + "\nTình trạng bàn giao: " + giaTri(phieu.getTinhTrangBanGiao())
                + "\nLưu ý của chủ: " + giaTri(phieu.getGhiChu())
                + "\nNhân viên: " + phieu.getTenNV()
                + "\nGiờ hẹn trả: " + dinhDang(phieu.getGioHenTra())
                + "\nPhí dự kiến: " + tien(phieu.getPhiDuKien())
                + "\nTrạng thái: " + phieu.getTrangThai();
        JTextArea area = new JTextArea(noiDung, 15, 44);
        area.setEditable(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        JOptionPane.showMessageDialog(this, new JScrollPane(area), "Phiếu tiếp nhận", JOptionPane.INFORMATION_MESSAGE);
    }

    private PhieuTiepNhanThuCung layDongChon() {
        int row = table.getSelectedRow();
        if (row < 0) return null;
        int modelRow = table.convertRowIndexToModel(row);
        if (modelRow < 0 || modelRow >= danhSachDangHienThi.size()) return null;
        return danhSachDangHienThi.get(modelRow);
    }

    private String dinhDang(LocalDateTime thoiGian) {
        return thoiGian == null ? "—" : thoiGian.format(THOI_GIAN);
    }

    private String tien(BigDecimal gia) {
        if (gia == null) return "—";
        return java.text.NumberFormat.getNumberInstance(new java.util.Locale("vi", "VN")).format(gia) + " đ";
    }

    private String giaTri(String giaTri) {
        return giaTri == null || giaTri.trim().isEmpty() ? "—" : giaTri;
    }
}
