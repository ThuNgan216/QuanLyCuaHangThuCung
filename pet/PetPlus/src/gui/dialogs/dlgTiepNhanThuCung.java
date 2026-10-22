package gui.dialogs;

import entity.DichVu;
import entity.KhachHang;
import entity.LichHen;
import entity.NhanVien;
import entity.PhieuTiepNhanThuCung;
import entity.ThuCung;
import service.TiepNhanThuCungService;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;
import java.util.Set;

/** Form check-in cho lịch hẹn đã đặt hoặc khách đến trực tiếp. */
public class dlgTiepNhanThuCung extends JDialog {
    private static final DateTimeFormatter THOI_GIAN = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final boolean theoLichHen;
    private final TiepNhanThuCungService service = new TiepNhanThuCungService();
    private final JComboBox<LichHen> cboLichHen = new JComboBox<>();
    private final JComboBox<KhachHang> cboKhachHang = new JComboBox<>();
    private final JComboBox<ThuCung> cboThuCung = new JComboBox<>();
    private final JComboBox<NhanVien> cboNhanVien = new JComboBox<>();
    private final JComboBox<DichVu> cboDichVu = new JComboBox<>();
    private final DefaultTableModel modelDichVuChon = new DefaultTableModel(
            new Object[] { "Dịch vụ đã chọn", "Đơn giá", "Mã dịch vụ" }, 0) {
        @Override public boolean isCellEditable(int row, int column) { return false; }
        @Override public Class<?> getColumnClass(int column) { return column == 1 ? BigDecimal.class : String.class; }
    };
    private final JTable tblDichVuChon = new JTable(modelDichVuChon);
    private final List<DichVu> danhSachDichVuChon = new ArrayList<>();
    private final Set<String> dichVuDaCoTrongLich = new HashSet<>();
    private final JLabel lblSoDichVu = new JLabel("Chưa có dịch vụ nào được chọn.");
    private final JLabel lblThongTinLichHen = new JLabel(" ");
    private final JLabel lblKetQuaTraCuu = new JLabel("Nhập số điện thoại để tra cứu khách hàng.");
    private final JLabel lblTinhTrangNhanVien = new JLabel(" ");
    private final JTextField txtSdtKhach = new JTextField(20);
    private final JTextField txtThoiGian = new JTextField(20);
    private final JTextField txtHenTra = new JTextField(20);
    private final JTextField txtPhiDuKien = new JTextField(20);
    private final JTextArea txtTinhTrang = new JTextArea(3, 30);
    private final JTextArea txtGhiChu = new JTextArea(3, 30);
    private final List<ThuCung> tatCaThuCung = new ArrayList<>();
    private boolean dangNapKhachHang;
    private boolean daLuu;

    public dlgTiepNhanThuCung(Window owner, boolean theoLichHen) {
        super(owner, theoLichHen ? "Tiếp nhận theo lịch hẹn" : "Tiếp nhận khách trực tiếp",
                Dialog.ModalityType.APPLICATION_MODAL);
        this.theoLichHen = theoLichHen;
        khoiTao();
    }

    private void khoiTao() {
        setLayout(new BorderLayout(10, 10));
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(14, 18, 10, 18));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5, 5, 5, 5);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        int row = 0;
        taoRendererDichVu();
        taoBangDichVuChon();

        JLabel moTa = new JLabel(theoLichHen
                ? "Chọn lịch chưa tiếp nhận; thông tin khách và thú cưng sẽ lấy từ lịch."
                : "Tra số điện thoại, chọn khách hàng, sau đó chọn thú cưng đến cửa hàng.");
        moTa.setHorizontalAlignment(SwingConstants.LEFT);
        moTa.setBorder(BorderFactory.createEmptyBorder(0, 0, 7, 0));
        themDong(form, c, row++, "", moTa);

        if (theoLichHen) {
            taoRendererLichHen();
            themDong(form, c, row++, "Lịch hẹn:", cboLichHen);
            themDong(form, c, row++, "Thông tin:", lblThongTinLichHen);
            cboLichHen.addActionListener(e -> capNhatTheoLichHen());
        } else {
            taiDuLieuKhachTrucTiep();
            taoRendererKhachHang();
            taoRendererThuCung();
            taoRendererNhanVien();
            cboKhachHang.setEnabled(false);
            cboThuCung.setEnabled(false);
            cboDichVu.setSelectedIndex(-1);
            cboKhachHang.addActionListener(e -> {
                if (!dangNapKhachHang) locThuCungTheoKhach();
            });
            JButton btnTraCuuKhach = new JButton("Tra cứu");
            JPanel dongTraCuu = new JPanel(new BorderLayout(6, 0));
            dongTraCuu.add(txtSdtKhach, BorderLayout.CENTER);
            dongTraCuu.add(btnTraCuuKhach, BorderLayout.EAST);
            themDong(form, c, row++, "Số điện thoại:", dongTraCuu);
            themDong(form, c, row++, "Kết quả:", lblKetQuaTraCuu);
            themDong(form, c, row++, "Khách hàng:", cboKhachHang);
            themDong(form, c, row++, "Thú cưng:", cboThuCung);
            themDong(form, c, row++, "Nhân viên chăm sóc:", cboNhanVien);
            themDong(form, c, row++, "Tình trạng:", lblTinhTrangNhanVien);
            btnTraCuuKhach.addActionListener(e -> traCuuKhachHang());
            txtSdtKhach.addActionListener(e -> traCuuKhachHang());
            txtSdtKhach.getDocument().addDocumentListener(new DocumentListener() {
                @Override public void insertUpdate(DocumentEvent e) { xoaKetQuaTraCuu(); }
                @Override public void removeUpdate(DocumentEvent e) { xoaKetQuaTraCuu(); }
                @Override public void changedUpdate(DocumentEvent e) { xoaKetQuaTraCuu(); }
            });
        }

        LocalDateTime hienTai = LocalDateTime.now().withSecond(0).withNano(0);
        txtThoiGian.setEditable(false);
        txtThoiGian.setText(hienTai.format(THOI_GIAN));
        txtHenTra.setText(hienTai.plusHours(2).format(THOI_GIAN));
        txtPhiDuKien.setEditable(false);
        txtPhiDuKien.setToolTipText("Giá được lấy tự động từ dịch vụ đã chọn trong database.");
        themDong(form, c, row++, "Giờ tiếp nhận:", txtThoiGian);
        themDong(form, c, row++, "Giờ hẹn trả:", txtHenTra);
        themDong(form, c, row++, "Dịch vụ:", taoPanelDichVu());
        themDong(form, c, row++, "Tổng phí dự kiến (đ):", txtPhiDuKien);
        themDong(form, c, row++, "Tình trạng bàn giao:", taoScroll(txtTinhTrang));
        themDong(form, c, row++, "Lưu ý của chủ:", taoScroll(txtGhiChu));

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnXacNhan = new JButton("Xác nhận tiếp nhận");
        JButton btnHuy = new JButton("Hủy");
        if (!theoLichHen && cboNhanVien.getItemCount() == 0) btnXacNhan.setEnabled(false);
        buttons.add(btnXacNhan);
        buttons.add(btnHuy);
        btnXacNhan.addActionListener(e -> luu());
        btnHuy.addActionListener(e -> dispose());

        add(new JScrollPane(form), BorderLayout.CENTER);
        add(buttons, BorderLayout.SOUTH);
        utils.PetPlusTheme.apply(getContentPane());
        setMinimumSize(new java.awt.Dimension(760, 720));
        pack();
        setLocationRelativeTo(getOwner());

        if (theoLichHen) {
            try {
                for (LichHen lichHen : service.layLichHenChoTiepNhan()) cboLichHen.addItem(lichHen);
                if (cboLichHen.getItemCount() == 0) {
                    btnXacNhan.setEnabled(false);
                    lblThongTinLichHen.setText("Không có lịch hẹn nào đang chờ tiếp nhận.");
                } else {
                    capNhatTheoLichHen();
                }
            } catch (RuntimeException ex) {
                btnXacNhan.setEnabled(false);
                baoLoi(ex);
            }
        }
    }

    private void taiDuLieuKhachTrucTiep() {
        try {
            for (ThuCung thuCung : service.layThuCung()) tatCaThuCung.add(thuCung);
            List<NhanVien> nhanVienConTrong = service.layNhanVienChamSocConTrong();
            for (NhanVien nhanVien : nhanVienConTrong) cboNhanVien.addItem(nhanVien);
            lblTinhTrangNhanVien.setText(nhanVienConTrong.isEmpty()
                    ? "Hiện không có nhân viên chăm sóc đang trống."
                    : "Có " + nhanVienConTrong.size() + " nhân viên chăm sóc đang trống.");
            for (DichVu dichVu : service.layDichVuChoTiepNhan()) cboDichVu.addItem(dichVu);
            cboDichVu.setSelectedIndex(-1);
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi tải dữ liệu", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void traCuuKhachHang() {
        try {
            List<KhachHang> ketQua = service.timKhachHangTheoSdt(txtSdtKhach.getText());
            dangNapKhachHang = true;
            cboKhachHang.removeAllItems();
            cboThuCung.removeAllItems();
            cboThuCung.setEnabled(false);
            for (KhachHang khachHang : ketQua) cboKhachHang.addItem(khachHang);
            cboKhachHang.setSelectedIndex(-1);
            dangNapKhachHang = false;
            cboKhachHang.setEnabled(!ketQua.isEmpty());
            if (ketQua.isEmpty()) {
                lblKetQuaTraCuu.setText("Không tìm thấy khách hàng có số điện thoại phù hợp.");
            } else {
                lblKetQuaTraCuu.setText("Tìm thấy " + ketQua.size() + " khách hàng. Hãy chọn đúng hồ sơ.");
                cboKhachHang.requestFocusInWindow();
            }
        } catch (RuntimeException ex) {
            dangNapKhachHang = false;
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Không thể tra cứu khách hàng",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void xoaKetQuaTraCuu() {
        if (dangNapKhachHang) return;
        dangNapKhachHang = true;
        cboKhachHang.removeAllItems();
        cboThuCung.removeAllItems();
        dangNapKhachHang = false;
        cboKhachHang.setEnabled(false);
        cboThuCung.setEnabled(false);
        lblKetQuaTraCuu.setText("Nhập số điện thoại rồi nhấn Tra cứu.");
    }

    private void capNhatTheoLichHen() {
        LichHen lichHen = (LichHen) cboLichHen.getSelectedItem();
        if (lichHen == null) return;
        String khach = lichHen.getMaKH() == null ? "" : lichHen.getMaKH().getTenKH();
        String thuCung = lichHen.getMaThuCung() == null ? "" : lichHen.getMaThuCung().getTenThuCung();
        String nhanVien = lichHen.getMaNV() == null ? "" : lichHen.getMaNV().getTenNV();
        lblThongTinLichHen.setText("Khách: " + khach + "  |  Thú cưng: " + thuCung + "  |  Nhân viên: " + nhanVien);

        cboDichVu.removeAllItems();
        danhSachDichVuChon.clear();
        dichVuDaCoTrongLich.clear();
        modelDichVuChon.setRowCount(0);
        capNhatSoDichVu();
        capNhatTongPhiDuKien();
        try {
            List<DichVu> dichVuTheoLich = service.layDichVuTheoLichHen(lichHen.getMaLichHen());
            List<DichVu> tatCaDichVu = service.layDichVuChoTiepNhan();
            for (DichVu item : tatCaDichVu) cboDichVu.addItem(item);
            for (DichVu item : dichVuTheoLich) {
                dichVuDaCoTrongLich.add(item.getMaDichVu());
                themDichVuDaChon(item, false);
            }
            if (cboDichVu.getItemCount() > 0) cboDichVu.setSelectedIndex(0);
            capNhatTongPhiDuKien();
        } catch (RuntimeException ex) {
            baoLoi(ex);
        }
    }

    private void capNhatTongPhiDuKien() {
        BigDecimal tong = BigDecimal.ZERO;
        for (DichVu dichVu : danhSachDichVuChon) {
            if (dichVu.getDonGia() != null) tong = tong.add(dichVu.getDonGia());
        }
        txtPhiDuKien.setText(tong.stripTrailingZeros().toPlainString());
    }

    private void taoBangDichVuChon() {
        tblDichVuChon.setFont(new java.awt.Font("Arial", java.awt.Font.PLAIN, 12));
        tblDichVuChon.setRowHeight(25);
        tblDichVuChon.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblDichVuChon.setFillsViewportHeight(true);
        tblDichVuChon.getTableHeader().setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 12));
        tblDichVuChon.getColumnModel().getColumn(1).setCellRenderer(new DefaultTableCellRenderer() {
            @Override protected void setValue(Object value) {
                if (value instanceof BigDecimal) {
                    setText(java.text.NumberFormat.getNumberInstance(new java.util.Locale("vi", "VN"))
                            .format(value) + " đ");
                } else {
                    setText("");
                }
                setHorizontalAlignment(SwingConstants.RIGHT);
            }
        });
        tblDichVuChon.getColumnModel().getColumn(0).setPreferredWidth(220);
        tblDichVuChon.getColumnModel().getColumn(1).setPreferredWidth(110);
        tblDichVuChon.removeColumn(tblDichVuChon.getColumnModel().getColumn(2));
        lblSoDichVu.setFont(new java.awt.Font("Arial", java.awt.Font.PLAIN, 11));
    }

    private JPanel taoPanelDichVu() {
        JPanel panel = new JPanel(new BorderLayout(0, 5));
        JPanel chon = new JPanel(new BorderLayout(6, 0));
        JButton btnThem = new JButton("+ Thêm dịch vụ");
        chon.add(cboDichVu, BorderLayout.CENTER);
        chon.add(btnThem, BorderLayout.EAST);
        btnThem.addActionListener(e -> themDichVuDangChon());
        panel.add(chon, BorderLayout.NORTH);

        JScrollPane scroll = new JScrollPane(tblDichVuChon);
        scroll.setPreferredSize(new Dimension(0, 105));
        panel.add(scroll, BorderLayout.CENTER);

        JButton btnXoa = new JButton("Xóa dịch vụ đã chọn");
        JPanel bottom = new JPanel(new BorderLayout(6, 0));
        bottom.add(lblSoDichVu, BorderLayout.CENTER);
        bottom.add(btnXoa, BorderLayout.EAST);
        btnXoa.addActionListener(e -> {
            int row = tblDichVuChon.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Chọn một dịch vụ trong bảng cần xóa.");
                return;
            }
            String maDichVu = String.valueOf(modelDichVuChon.getValueAt(row, 2));
            if (theoLichHen && dichVuDaCoTrongLich.contains(maDichVu)) {
                JOptionPane.showMessageDialog(this,
                        "Dịch vụ đã có trong lịch hẹn được giữ nguyên. Chỉ xóa được dịch vụ thêm tại quầy.");
                return;
            }
            danhSachDichVuChon.removeIf(d -> maDichVu.equals(d.getMaDichVu()));
            modelDichVuChon.removeRow(row);
            capNhatTongPhiDuKien();
            capNhatSoDichVu();
        });
        panel.add(bottom, BorderLayout.SOUTH);
        return panel;
    }

    private void themDichVuDangChon() {
        Object selected = cboDichVu.getSelectedItem();
        if (!(selected instanceof DichVu)) {
            JOptionPane.showMessageDialog(this, "Chọn dịch vụ muốn thêm trước.");
            return;
        }
        themDichVuDaChon((DichVu) selected, true);
    }

    private void themDichVuDaChon(DichVu dichVu, boolean thongBao) {
        if (dichVu == null || dichVu.getMaDichVu() == null || dichVu.getDonGia() == null) {
            if (thongBao) JOptionPane.showMessageDialog(this, "Dịch vụ chưa có mã hoặc đơn giá hợp lệ trong database.");
            return;
        }
        for (DichVu daChon : danhSachDichVuChon) {
            if (dichVu.getMaDichVu().equals(daChon.getMaDichVu())
                    || (dichVu.getTenDichVu() != null && daChon.getTenDichVu() != null
                            && dichVu.getTenDichVu().trim().equalsIgnoreCase(daChon.getTenDichVu().trim()))) {
                if (thongBao) JOptionPane.showMessageDialog(this, "Dịch vụ này đã có trong phiếu.");
                return;
            }
        }
        danhSachDichVuChon.add(dichVu);
        modelDichVuChon.addRow(new Object[] { dichVu.getTenDichVu(), dichVu.getDonGia(), dichVu.getMaDichVu() });
        capNhatTongPhiDuKien();
        capNhatSoDichVu();
    }

    private void capNhatSoDichVu() {
        lblSoDichVu.setText(danhSachDichVuChon.isEmpty()
                ? "Chưa có dịch vụ nào được chọn."
                : danhSachDichVuChon.size() + " dịch vụ đã chọn.");
    }

    private void locThuCungTheoKhach() {
        Object selected = cboKhachHang.getSelectedItem();
        cboThuCung.removeAllItems();
        if (!(selected instanceof KhachHang)) {
            cboThuCung.setEnabled(false);
            return;
        }
        String maKH = ((KhachHang) selected).getMaKH();
        for (ThuCung thuCung : tatCaThuCung) {
            if (maKH != null && thuCung.getMaKH() != null && maKH.equals(thuCung.getMaKH().getMaKH())) {
                cboThuCung.addItem(thuCung);
            }
        }
        cboThuCung.setEnabled(cboThuCung.getItemCount() > 0);
        if (cboThuCung.getItemCount() == 0) {
            lblKetQuaTraCuu.setText("Khách đã chọn chưa có hồ sơ thú cưng.");
        } else {
            lblKetQuaTraCuu.setText("Đã chọn khách hàng. Chọn thú cưng cần tiếp nhận.");
        }
    }

    private void taoRendererLichHen() {
        cboLichHen.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(javax.swing.JList<?> list, Object value, int index,
                    boolean selected, boolean focus) {
                super.getListCellRendererComponent(list, value, index, selected, focus);
                if (value instanceof LichHen) {
                    LichHen lichHen = (LichHen) value;
                    String ngay = lichHen.getThoiGianHen() == null ? "" : lichHen.getThoiGianHen().format(THOI_GIAN);
                    String tenThuCung = lichHen.getMaThuCung() == null ? "" : lichHen.getMaThuCung().getTenThuCung();
                    String tenKhach = lichHen.getMaKH() == null ? "" : lichHen.getMaKH().getTenKH();
                    setText(lichHen.getMaLichHen() + " | " + ngay + " | " + tenThuCung + " - " + tenKhach);
                }
                return this;
            }
        });
    }

    private void taoRendererKhachHang() {
        cboKhachHang.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(javax.swing.JList<?> list, Object value, int index,
                    boolean selected, boolean focus) {
                super.getListCellRendererComponent(list, value, index, selected, focus);
                if (value instanceof KhachHang) {
                    KhachHang kh = (KhachHang) value;
                    setText(kh.getMaKH() + " - " + kh.getTenKH() + (kh.getSdt() == null ? "" : " - " + kh.getSdt()));
                }
                return this;
            }
        });
    }

    private void taoRendererThuCung() {
        cboThuCung.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(javax.swing.JList<?> list, Object value, int index,
                    boolean selected, boolean focus) {
                super.getListCellRendererComponent(list, value, index, selected, focus);
                if (value instanceof ThuCung) {
                    ThuCung tc = (ThuCung) value;
                    setText(tc.getMaThuCung() + " - " + tc.getTenThuCung() + " (" + tc.getLoai() + ")");
                }
                return this;
            }
        });
    }

    private void taoRendererNhanVien() {
        cboNhanVien.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(javax.swing.JList<?> list, Object value, int index,
                    boolean selected, boolean focus) {
                super.getListCellRendererComponent(list, value, index, selected, focus);
                if (value instanceof NhanVien) {
                    NhanVien nv = (NhanVien) value;
                    setText(nv.getMaNV() + " - " + nv.getTenNV());
                }
                return this;
            }
        });
    }

    private void taoRendererDichVu() {
        cboDichVu.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(javax.swing.JList<?> list, Object value, int index,
                    boolean selected, boolean focus) {
                super.getListCellRendererComponent(list, value, index, selected, focus);
                if (value instanceof DichVu) {
                    DichVu dichVu = (DichVu) value;
                    String gia = dichVu.getDonGia() == null ? "Chưa có giá"
                            : java.text.NumberFormat.getNumberInstance(new java.util.Locale("vi", "VN"))
                                    .format(dichVu.getDonGia()) + " đ";
                    setText(dichVu.getMaDichVu() + " - " + dichVu.getTenDichVu() + " - " + gia);
                }
                return this;
            }
        });
    }

    private JScrollPane taoScroll(JTextArea area) {
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        return new JScrollPane(area);
    }

    private void themDong(JPanel panel, GridBagConstraints c, int row, String label, Component input) {
        c.gridy = row;
        c.gridx = 0;
        c.weightx = 0;
        panel.add(new JLabel(label), c);
        c.gridx = 1;
        c.weightx = 1;
        panel.add(input, c);
    }

    private void luu() {
        try {
            PhieuTiepNhanThuCung phieu = new PhieuTiepNhanThuCung();
            phieu.setThoiGianTiepNhan(LocalDateTime.parse(txtThoiGian.getText(), THOI_GIAN));
            phieu.setGioHenTra(LocalDateTime.parse(txtHenTra.getText().trim(), THOI_GIAN));
            if (danhSachDichVuChon.isEmpty())
                throw new IllegalArgumentException("Vui lòng thêm ít nhất một dịch vụ vào phiếu.");
            phieu.setDanhSachDichVu(danhSachDichVuChon);
            phieu.setTinhTrangBanGiao(txtTinhTrang.getText().trim());
            phieu.setGhiChu(txtGhiChu.getText().trim());

            if (theoLichHen) {
                LichHen lichHen = (LichHen) cboLichHen.getSelectedItem();
                if (lichHen == null) throw new IllegalArgumentException("Vui lòng chọn lịch hẹn.");
                phieu.setMaLichHen(lichHen.getMaLichHen());
                phieu.setMaKH(lichHen.getMaKH().getMaKH());
                phieu.setMaThuCung(lichHen.getMaThuCung().getMaThuCung());
                phieu.setMaNV(lichHen.getMaNV().getMaNV());
            } else {
                KhachHang kh = (KhachHang) cboKhachHang.getSelectedItem();
                ThuCung tc = (ThuCung) cboThuCung.getSelectedItem();
                NhanVien nv = (NhanVien) cboNhanVien.getSelectedItem();
                if (kh == null) throw new IllegalArgumentException("Chưa có khách hàng. Hãy tạo hồ sơ khách hàng trước.");
                if (tc == null) throw new IllegalArgumentException("Khách hàng chưa có hồ sơ thú cưng.");
                if (nv == null) throw new IllegalArgumentException("Vui lòng chọn nhân viên tiếp nhận.");
                phieu.setMaKH(kh.getMaKH());
                phieu.setMaThuCung(tc.getMaThuCung());
                phieu.setMaNV(nv.getMaNV());
            }

            service.tiepNhan(phieu);
            daLuu = true;
            dispose();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Không thể tiếp nhận", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void baoLoi(RuntimeException ex) {
        JOptionPane.showMessageDialog(this, ex.getMessage(), "Không thể tải dữ liệu", JOptionPane.ERROR_MESSAGE);
    }

    public boolean isDaLuu() { return daLuu; }
}
