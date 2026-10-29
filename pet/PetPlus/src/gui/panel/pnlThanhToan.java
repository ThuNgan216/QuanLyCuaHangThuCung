package gui.panel;

import dao.ThanhToanDAO;
import dao.TiepNhanThuCungDAO;
import dao.MaVachDAO;
import dao.SanPhamDAO;
import database.ConnectDB;
import entity.KhuyenMai;
import entity.SanPham;
import gui.dialogs.dlgQuetMaVach;
import utils.Auth;

import java.awt.*;
import java.awt.event.*;
import java.text.NumberFormat;
import java.text.Normalizer;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;
import java.sql.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import javax.imageio.ImageIO;

/**
 * Giao diện bán hàng và dịch vụ PETPLUS.
 * Truy vấn danh mục/khách hàng/khuyến mãi và ghi HoaDon/ChiTietHoaDon được gọi
 * thông qua ThanhToanDAO.
 */
public class pnlThanhToan extends JPanel {
    private static final Color PINK = new Color(246, 65, 125);
    private static final Color DARK_PINK = new Color(217, 37, 96);
    private static final Color LIGHT_PINK = new Color(255, 241, 246);
    private static final Color BORDER = new Color(238, 222, 229);
    private static final Color TEXT = new Color(40, 46, 58);
    private static final Color MUTED = new Color(120, 126, 138);
    private static final Color READONLY_BG = new Color(246, 247, 249);
    private static final Font FONT = new Font("Times New Roman", Font.PLAIN, 13);
    private static final NumberFormat MONEY = NumberFormat.getIntegerInstance(new Locale("vi", "VN"));
    private static final Pattern SDT_HOP_LE = Pattern.compile("^0\\d{9}$");
    private static final long GIA_TRI_MOI_DIEM = 100L;

    private static final String LOAI_SP = "Sản phẩm";
    private static final String LOAI_DV = "Dịch vụ";
    private static final int C_LOAI = 0, C_TEN = 1, C_SL = 2, C_GIA = 3, C_TIEN = 4, C_GIAM = 5, C_MA = 6;

    private final ThanhToanDAO thanhToanDAO = new ThanhToanDAO();
    private final TiepNhanThuCungDAO tiepNhanThuCungDAO = new TiepNhanThuCungDAO();
    private final MaVachDAO maVachDAO = new MaVachDAO();

    private final DefaultTableModel modelDon = new DefaultTableModel(
            new String[] { "Loại", "Tên", "SL", "Đơn giá", "Thành tiền", "Giảm KM", "Mã" }, 0) {
        @Override
        public boolean isCellEditable(int r, int c) {
            return false;
        }
    };
    private final JTable tblDon = new JTable(modelDon);

    private final DanhMuc dsSanPham = new DanhMuc(LOAI_SP, "Tìm sản phẩm theo tên hoặc mã...");
    private final DanhMuc dsDichVu = new DanhMuc(LOAI_DV, "Tìm dịch vụ theo tên hoặc mã...");

    // Thông tin đơn hàng
    private final JTextField txtOrderCode = field("", false);
    private final JTextField txtOrderDate = field("", false);
    private final JTextField txtEmployee = field("", false);

    // Thông tin khách hàng
    private final JRadioButton rbKhachCu = new JRadioButton("Khách hàng cũ", true);
    private final JRadioButton rbKhachMoi = new JRadioButton("Khách hàng mới");
    private final JTextField txtPhone = field("", true);
    private final JTextField txtCustomer = field("", false);
    private final JTextField txtAddress = field("", false);
    private final JTextField txtMemberType = field("", false);
    private final JTextField txtPoints = field("0", false);
    private final JLabel lblGoiY = label("", 12, MUTED, false);
    private boolean khachMoi = false;
    private KhachHangItem selectedKh = null;

    private final JPopupMenu popupKhachHang = new JPopupMenu();
    private final DefaultListModel<KhachHangItem> modelKhachHang = new DefaultListModel<>();
    private final JList<KhachHangItem> listKhachHang = new JList<>(modelKhachHang);
    private boolean dangChonKhachHang = false;
    private int lanTimKhachHang = 0;

    // Thanh toán
    private final JLabel lblSubtotal = valueLabel("0 đ");
    private final JLabel lblDiscount = valueLabel("0 đ");
    private final JLabel lblTenKhuyenMaiSanPham = valueLabel("Không áp dụng");
    private final JLabel lblGiamKhuyenMaiSanPham = valueLabel("0 đ");
    private final JCheckBox chkSuDungDiem = new JCheckBox("Dùng điểm tích lũy");
    private final JLabel lblGiamDiem = valueLabel("0 đ");
    private final JLabel lblTotal = valueLabel("0 đ");
    private final JLabel lblChange = valueLabel("0 đ");
    private final JTextField txtPaid = new JTextField("0");
    private final JComboBox<KhuyenMai> cboKhuyenMai = new JComboBox<>();
    private List<KhuyenMai> khuyenMaiSanPhamHieuLuc = new ArrayList<>();
    private KhuyenMai khuyenMaiSanPhamDangApDung;
    private long tienGiamKhuyenMaiSanPham;
    private boolean dangNapKhuyenMai = false;
    private final JRadioButton rbCash = new JRadioButton("Tiền mặt", true);
    private final JRadioButton rbQR = new JRadioButton("QR Code");
    private JButton[] nutTienNhanh = new JButton[0];
    private JButton btnConfirm;
    private long total = 0;
    private int soDiemSuDung = 0;
    private String maTiepNhanDangLap;
    private final Set<String> maDichVuPhieuDangLap = new HashSet<>();
    private static final String DUONG_DAN_QR = "pet/PetPlus/resources/QR/QR.jpg";
    private static final String THU_MUC_HOA_DON = "pet/PetPlus/data/hoadon";

    private JDialog dlgQR;
    private final JLabel lblAnhQR = new JLabel("", SwingConstants.CENTER);
    private final JEditorPane txtThongTinQR = new JEditorPane("text/html", "");

    private static class KhachHangItem {
        final String maKH, ten, sdt, diaChi;
        final int hangThanhVien, diemTichLuy;

        KhachHangItem(String maKH, String ten, String sdt, String diaChi,
                int hangThanhVien, int diemTichLuy) {
            this.maKH = maKH;
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

    public pnlThanhToan() {
        setLayout(new BorderLayout(0, 12));
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(12, 14, 14, 14));
        add(createTitle(), BorderLayout.NORTH);

        JPanel body = new JPanel(new BorderLayout(12, 0));
        body.setOpaque(false);
        body.add(createLeft(), BorderLayout.CENTER);
        body.add(createRight(), BorderLayout.EAST);
        add(body, BorderLayout.CENTER);

        txtPaid.getDocument().addDocumentListener(new SimpleDocumentListener(this::updateTotals));
        chkSuDungDiem.addActionListener(e -> updateTotals());
        cauHinhComboKhuyenMai();
        setupTimKhachHang();
        setCustomerMode(false);
        lamMoiThongTinDon();
        capNhatPhuongThuc();

        addHierarchyListener(e -> {
            if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0 && isShowing()) {
                capNhatNhanVien();
                dsSanPham.taiDuLieu();
                dsDichVu.taiDuLieu();
                taiKhuyenMai();
            }
        });
    }

    // ================== DANH MỤC SẢN PHẨM / DỊCH VỤ ==================
    private class DanhMuc {
        final String loai;
        final DefaultTableModel model = new DefaultTableModel(new String[] { "Tên", "Đơn giá", "Mã" }, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }

            @Override
            public Class<?> getColumnClass(int c) {
                return c == 1 ? Long.class : String.class;
            }
        };
        final JTable table = new JTable(model);
        final TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
        final JTextField txtSearch;
        final JLabel lblTrangThai = label("", 12, MUTED, false);
        boolean dangTai = false;

        DanhMuc(String loai, String goiY) {
            this.loai = loai;
            this.txtSearch = timKiemField(goiY);
            cauHinhBang();
            txtSearch.getDocument().addDocumentListener(new SimpleDocumentListener(this::loc));
            txtSearch.addActionListener(e -> {
                if (table.getRowCount() > 0) {
                    if (table.getSelectedRow() < 0)
                        table.setRowSelectionInterval(0, 0);
                    themDongDangChon();
                    txtSearch.selectAll();
                }
            });
        }

        private void cauHinhBang() {
            table.setFont(FONT);
            table.setRowHeight(26);
            table.setSelectionBackground(new Color(255, 222, 233));
            table.setGridColor(BORDER);
            table.setShowGrid(true);
            table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            table.setRowSorter(sorter);
            JTableHeader header = table.getTableHeader();
            header.setFont(new Font("Times New Roman", Font.BOLD, 12));
            header.setBackground(LIGHT_PINK);
            header.setPreferredSize(new Dimension(0, 28));
            table.getColumnModel().getColumn(0).setPreferredWidth(190);
            table.getColumnModel().getColumn(1).setPreferredWidth(85);
            table.getColumnModel().getColumn(1).setCellRenderer(taoCurrencyRenderer());
            table.removeColumn(table.getColumnModel().getColumn(2));

            table.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() == 2)
                        themDongDangChon();
                }
            });
            table.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke("ENTER"), "themVaoDon");
            table.getActionMap().put("themVaoDon", new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    themDongDangChon();
                }
            });
        }

        JPanel taoGiaoDien() {
            JPanel content = new JPanel(new BorderLayout(0, 6));
            content.setOpaque(false);
            content.add(txtSearch, BorderLayout.NORTH);
            JScrollPane scroll = new JScrollPane(table);
            scroll.setBorder(new LineBorder(BORDER));
            content.add(scroll, BorderLayout.CENTER);
            content.add(lblTrangThai, BorderLayout.SOUTH);
            return card(LOAI_SP.equals(loai) ? "Danh sách sản phẩm" : "Danh sách dịch vụ", content);
        }

        void loc() {
            final String q = boDau(txtSearch.getText().trim());
            if (q.isEmpty()) {
                sorter.setRowFilter(null);
                return;
            }
            sorter.setRowFilter(new RowFilter<DefaultTableModel, Integer>() {
                @Override
                public boolean include(Entry<? extends DefaultTableModel, ? extends Integer> e) {
                    return boDau(String.valueOf(e.getValue(0))).contains(q)
                            || boDau(String.valueOf(e.getValue(2))).contains(q);
                }
            });
        }

        void themDongDangChon() {
            int r = table.getSelectedRow();
            if (r < 0)
                return;
            int m = table.convertRowIndexToModel(r);
            themVaoDon(loai, String.valueOf(model.getValueAt(m, 2)),
                    String.valueOf(model.getValueAt(m, 0)), ((Number) model.getValueAt(m, 1)).longValue());
        }

        void taiDuLieu() {
            if (dangTai)
                return;
            dangTai = true;
            lblTrangThai.setForeground(MUTED);
            lblTrangThai.setText("Đang tải...");

            new SwingWorker<List<Object[]>, Void>() {
                @Override
                protected List<Object[]> doInBackground() throws Exception {
                    return LOAI_SP.equals(loai)
                            ? thanhToanDAO.laySanPham()
                            : thanhToanDAO.layDichVu();
                }

                @Override
                protected void done() {
                    dangTai = false;
                    try {
                        List<Object[]> ds = get();
                        model.setRowCount(0);
                        for (Object[] row : ds)
                            model.addRow(row);
                        loc();
                        lblTrangThai.setForeground(MUTED);
                        lblTrangThai.setText(ds.size() + " mục • Nhấp đúp hoặc Enter để thêm vào đơn");
                    } catch (Exception ex) {
                        Throwable loi = nguyenNhan(ex);
                        lblTrangThai.setForeground(DARK_PINK);
                        lblTrangThai.setText("Lỗi tải dữ liệu: " + loi.getMessage());
                        lblTrangThai.setToolTipText(loi.toString());
                        JOptionPane.showMessageDialog(pnlThanhToan.this,
                                "Không thể tải " + loai + ":\n" + loi.getMessage(),
                                "Lỗi cơ sở dữ liệu", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }.execute();
        }
    }

    private void themVaoDon(String loai, String ma, String ten, long gia) {
        int dong = -1;
        for (int r = 0; r < modelDon.getRowCount(); r++) {
            if (loai.equals(modelDon.getValueAt(r, C_LOAI))
                    && ma.equals(modelDon.getValueAt(r, C_MA))) {
                int existing = ((Number) modelDon.getValueAt(r, C_SL)).intValue();
                modelDon.setValueAt(Math.addExact(existing, 1), r, C_SL);
                dong = r;
                break;
            }
        }
        if (dong < 0) {
            modelDon.addRow(new Object[] { loai, ten, 1, gia, gia, 0L, ma });
            dong = modelDon.getRowCount() - 1;
        }
        updateTotals();
        tblDon.setRowSelectionInterval(dong, dong);
        tblDon.scrollRectToVisible(tblDon.getCellRect(dong, 0, true));
    }

    private static String boDau(String s) {
        String n = Normalizer.normalize(s == null ? "" : s, Normalizer.Form.NFD);
        return n.replaceAll("\\p{M}+", "").replace('đ', 'd').replace('Đ', 'D').toLowerCase(Locale.ROOT);
    }

    // ================== KHUYẾN MÃI ==================
    private static String dinhDangPhanTram(float p) {
        return p == (int) p ? String.valueOf((int) p) : String.valueOf(p);
    }

    private void cauHinhComboKhuyenMai() {
        cboKhuyenMai.setFont(FONT);
        cboKhuyenMai.setBackground(Color.WHITE);
        cboKhuyenMai.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                    boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                KhuyenMai km = (KhuyenMai) value;
                setText(km.getTenKhuyenMai() + " (-" + dinhDangPhanTram(km.getPhanTramGiam()) + "%)");
                setToolTipText(km.getDieuKienApDung());

                return this;
            }
        });
        cboKhuyenMai.addActionListener(e -> {
            if (!dangNapKhuyenMai)
                updateTotals();
        });
    }

    private void taiKhuyenMai() {
        new SwingWorker<List<KhuyenMai>, Void>() {
            @Override
            protected List<KhuyenMai> doInBackground() throws Exception {
                return thanhToanDAO.layKhuyenMaiHieuLuc();
            }

            @Override
            protected void done() {
                try {
                    List<KhuyenMai> ds = get();
                    khuyenMaiSanPhamHieuLuc = new ArrayList<>();
                    List<KhuyenMai> khuyenMaiHoaDon = new ArrayList<>();
                    for (KhuyenMai km : ds) {
                        if (KhuyenMai.PHAM_VI_SAN_PHAM.equals(km.getPhamViApDung())) {
                            khuyenMaiSanPhamHieuLuc.add(km);
                        } else {
                            khuyenMaiHoaDon.add(km);
                        }
                    }
                    KhuyenMai dangChon = (KhuyenMai) cboKhuyenMai.getSelectedItem();
                    dangNapKhuyenMai = true;
                    try {
                        cboKhuyenMai.removeAllItems();
                        for (KhuyenMai km : khuyenMaiHoaDon)
                            cboKhuyenMai.addItem(km);

                        boolean daChon = false;
                        if (dangChon != null && dangChon.getMaKhuyenMai() != null) {
                            for (int i = 0; i < cboKhuyenMai.getItemCount(); i++) {
                                if (dangChon.equals(cboKhuyenMai.getItemAt(i))) {
                                    cboKhuyenMai.setSelectedIndex(i);
                                    daChon = true;
                                    break;
                                }
                            }
                        }
                        if (!daChon)
                            chonKhongKhuyenMai();
                    } finally {
                        dangNapKhuyenMai = false;
                    }
                    updateTotals();
                } catch (Exception ex) {
                    Throwable loi = nguyenNhan(ex);
                    JOptionPane.showMessageDialog(pnlThanhToan.this,
                            "Không thể tải khuyến mãi:\n" + loi.getMessage(),
                            "Lỗi cơ sở dữ liệu", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private KhuyenMai layKhuyenMaiDangChon() {
        KhuyenMai km = (KhuyenMai) cboKhuyenMai.getSelectedItem();
        return (km == null || km.getMaKhuyenMai() == null) ? null : km;
    }

    private void chonKhongKhuyenMai() {
        for (int i = 0; i < cboKhuyenMai.getItemCount(); i++) {
            KhuyenMai km = cboKhuyenMai.getItemAt(i);
            if (km.getPhanTramGiam() <= 0) {
                cboKhuyenMai.setSelectedIndex(i);
                return;
            }
        }
        if (cboKhuyenMai.getItemCount() > 0)
            cboKhuyenMai.setSelectedIndex(0);
    }

    private double layTyLeGiamGia() {
        KhuyenMai km = layKhuyenMaiDangChon();
        return km == null ? 0 : Math.max(0, Math.min(100, km.getPhanTramGiam()));
    }

    private KhuyenMai layKhuyenMaiSanPhamApDung() {
        int hangKhach = selectedKh == null ? 0 : selectedKh.hangThanhVien;
        KhuyenMai totNhat = null;
        for (KhuyenMai km : khuyenMaiSanPhamHieuLuc) {
            int hangApDung = km.getHangKhachHangApDung();
            // Hạng cao hơn được hưởng ưu đãi của hạng thấp hơn; khách mới chỉ dùng được loại "Tất cả".
            if (hangApDung != 0 && hangKhach < hangApDung) continue;
            if (layMaSanPhamDatNguong(km).isEmpty()) continue;
            if (totNhat == null || km.getPhanTramGiam() > totNhat.getPhanTramGiam()) {
                totNhat = km;
            }
        }
        return totNhat;
    }

    /** Trả về các mã sản phẩm có số lượng đạt ngưỡng riêng cho từng mã. */
    private java.util.Set<String> layMaSanPhamDatNguong(KhuyenMai km) {
        java.util.Set<String> maSanPhamApDung = new java.util.HashSet<>(km.getMaSanPhamApDung());
        java.util.Map<String, Long> soLuongTheoMa = new java.util.HashMap<>();
        for (int row = 0; row < modelDon.getRowCount(); row++) {
            if (!LOAI_SP.equals(modelDon.getValueAt(row, C_LOAI))) continue;
            String maSP = String.valueOf(modelDon.getValueAt(row, C_MA));
            if (!maSanPhamApDung.contains(maSP)) continue;
            long soLuong = ((Number) modelDon.getValueAt(row, C_SL)).longValue();
            soLuongTheoMa.put(maSP, Math.addExact(soLuongTheoMa.getOrDefault(maSP, 0L), soLuong));
        }
        java.util.Set<String> maDatNguong = new java.util.HashSet<>();
        for (java.util.Map.Entry<String, Long> entry : soLuongTheoMa.entrySet()) {
            if (entry.getValue() >= km.getSoLuongToiThieu()) maDatNguong.add(entry.getKey());
        }
        return maDatNguong;
    }

    // ================== NHÂN VIÊN ==================
    private void capNhatNhanVien() {
        txtEmployee.setText(Auth.user == null ? "Chưa đăng nhập" : Auth.user.getTenNV());
    }

    private void lamMoiThongTinDon() {
        LocalDateTime now = LocalDateTime.now();
        txtOrderCode.setText("DH-" + now.format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss")));
        txtOrderDate.setText(now.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        capNhatNhanVien();
    }

    // ================== KHÁCH HÀNG ==================
    private void setCustomerMode(boolean moi) {
        khachMoi = moi;
        dangChonKhachHang = true;
        lanTimKhachHang++;
        try {
            popupKhachHang.setVisible(false);
            selectedKh = null;
            txtPhone.setText("");
            txtCustomer.setText("");
            txtAddress.setText("");
        } finally {
            dangChonKhachHang = false;
        }
        datChoPhepNhap(txtCustomer, moi);
        datChoPhepNhap(txtAddress, moi);
        txtMemberType.setText(moi ? "Khách hàng mới" : "");
        txtPoints.setText("0");
        chkSuDungDiem.setSelected(false);
        capNhatTrangThaiDiem();
        lblGoiY.setText(moi
                ? "Thông tin khách hàng sẽ được lưu khi tạo đơn."
                : "Nhập số điện thoại để tìm khách hàng đã có.");
        if (moi)
            txtPhone.requestFocusInWindow();
    }

    private void setupTimKhachHang() {
        listKhachHang.setFont(FONT);
        listKhachHang.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        popupKhachHang.setFocusable(false);
        JScrollPane scroll = new JScrollPane(listKhachHang);
        scroll.setPreferredSize(new Dimension(320, 140));
        popupKhachHang.add(scroll);

        Timer timer = new Timer(300, e -> timKhachHang(txtPhone.getText().trim()));
        timer.setRepeats(false);
        txtPhone.getDocument().addDocumentListener(new DocumentListener() {
            private void capNhat() {
                if (dangChonKhachHang)
                    return;
                lanTimKhachHang++;
                selectedKh = null;
                chkSuDungDiem.setSelected(false);
                capNhatTrangThaiDiem();
                if (khachMoi)
                    return;
                txtCustomer.setText("");
                txtAddress.setText("");
                txtMemberType.setText("");
                txtPoints.setText("0");
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

        txtPhone.addActionListener(e -> {
            if (!khachMoi && popupKhachHang.isVisible() && !modelKhachHang.isEmpty()) {
                if (listKhachHang.getSelectedIndex() < 0)
                    listKhachHang.setSelectedIndex(0);
                chonKhachHang();
            }
        });
        listKhachHang.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() >= 1)
                    chonKhachHang();
            }
        });
    }

    private void timKhachHang(String sdt) {
        final int lanTim = ++lanTimKhachHang;
        modelKhachHang.clear();
        if (khachMoi || sdt.length() < 2) {
            popupKhachHang.setVisible(false);
            return;
        }

        new SwingWorker<List<ThanhToanDAO.KhachHangDTO>, Void>() {
            @Override
            protected List<ThanhToanDAO.KhachHangDTO> doInBackground() throws Exception {
                return thanhToanDAO.timKhachHang(sdt);
            }

            @Override
            protected void done() {
                if (khachMoi || lanTim != lanTimKhachHang
                        || !sdt.equals(txtPhone.getText().trim()))
                    return;
                try {
                    for (ThanhToanDAO.KhachHangDTO kh : get()) {
                        modelKhachHang.addElement(new KhachHangItem(kh.maKH, kh.tenKH, kh.sdt,
                                kh.diaChi, kh.hangThanhVien, kh.diemTichLuy));
                    }
                    if (!modelKhachHang.isEmpty() && txtPhone.isShowing()) {
                        popupKhachHang.show(txtPhone, 0, txtPhone.getHeight());
                        lblGoiY.setText("Chọn khách hàng trong danh sách hoặc nhấn Enter.");
                    } else {
                        popupKhachHang.setVisible(false);
                        lblGoiY.setText("Không tìm thấy số điện thoại. Chọn Khách hàng mới để thêm.");
                    }
                } catch (Exception ex) {
                    popupKhachHang.setVisible(false);
                    Throwable loi = nguyenNhan(ex);
                    JOptionPane.showMessageDialog(pnlThanhToan.this,
                            "Không thể tìm khách hàng: " + loi.getMessage(),
                            "Lỗi cơ sở dữ liệu", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

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
            txtMemberType.setText(String.valueOf(kh.hangThanhVien));
            txtPoints.setText(String.valueOf(kh.diemTichLuy));
            selectedKh = kh;
        } finally {
            dangChonKhachHang = false;
        }
        capNhatTrangThaiDiem();
        lblGoiY.setText("Đã chọn khách hàng: " + kh.ten);
        popupKhachHang.setVisible(false);
    }

    // ================== GIAO DIỆN ==================
    private JPanel createTitle() {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.add(label("Lập hóa đơn", 22, DARK_PINK, true), BorderLayout.WEST);
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);
        JButton btnQuetMaVach = outlineButton("Quét mã vạch");
        btnQuetMaVach.setToolTipText("Quét sản phẩm bằng webcam và thêm vào đơn hàng đang lập.");
        btnQuetMaVach.addActionListener(e -> moQuetMaVach(
                SwingUtilities.getWindowAncestor(this) instanceof Frame
                        ? (Frame) SwingUtilities.getWindowAncestor(this) : null));
        JButton btnPhieuChoThanhToan = outlineButton("Chọn phiếu chờ thanh toán");
        btnPhieuChoThanhToan
                .setToolTipText("Nạp thông tin khách hàng và dịch vụ từ phiếu tiếp nhận đang chờ thanh toán.");
        btnPhieuChoThanhToan.addActionListener(e -> moChonPhieuChoThanhToan());
        actions.add(btnQuetMaVach);
        actions.add(btnPhieuChoThanhToan);
        p.add(actions, BorderLayout.EAST);
        p.setBorder(new CompoundBorder(new MatteBorder(0, 0, 1, 0, PINK),
                new EmptyBorder(0, 6, 9, 0)));
        return p;
    }

    public void moQuetMaVach(Frame parent) {
        new dlgQuetMaVach(parent, this::themSanPhamTheoMaVach).setVisible(true);
    }

    private void themSanPhamTheoMaVach(String maVach) {
        new SwingWorker<MaVachDAO.SanPhamQuet, Void>() {
            @Override
            protected MaVachDAO.SanPhamQuet doInBackground() throws Exception {
                try (Connection con = ConnectDB.getConnection()) {
                    return maVachDAO.timSanPhamTheoMaVach(con, maVach);
                }
            }

            @Override
            protected void done() {
                try {
                    MaVachDAO.SanPhamQuet sanPham = get();
                    if (sanPham == null) {
                        chonSanPhamGanMaVach(maVach);
                        return;
                    }
                    themSanPhamVaoDon(sanPham, maVach);
                } catch (Exception ex) {
                    Throwable loi = nguyenNhan(ex);
                    JOptionPane.showMessageDialog(pnlThanhToan.this,
                            "Không thể tra sản phẩm theo mã vạch: " + loi.getMessage(),
                            "Lỗi mã vạch", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private void chonSanPhamGanMaVach(String maVach) {
        new SwingWorker<List<SanPham>, Void>() {
            @Override
            protected List<SanPham> doInBackground() throws Exception {
                return new SanPhamDAO().layDanhSachSanPham();
            }

            @Override
            protected void done() {
                try {
                    List<SanPham> sanPham = get();
                    if (sanPham.isEmpty()) {
                        JOptionPane.showMessageDialog(pnlThanhToan.this,
                                "Chưa có sản phẩm để gán mã vạch " + maVach + ".",
                                "Không có sản phẩm", JOptionPane.WARNING_MESSAGE);
                        return;
                    }

                    JComboBox<SanPham> cboSanPham = new JComboBox<>(sanPham.toArray(new SanPham[0]));
                    cboSanPham.setSelectedIndex(-1);
                    cboSanPham.setRenderer(new DefaultListCellRenderer() {
                        @Override
                        public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                boolean isSelected, boolean cellHasFocus) {
                            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                            if (value instanceof SanPham) {
                                SanPham item = (SanPham) value;
                                setText(item.getMaSP() + " - " + item.getTenSP()
                                        + " • Tồn: " + item.getSoLuongTon());
                            } else {
                                setText("— Chọn sản phẩm —");
                            }
                            return this;
                        }
                    });
                    JPanel form = new JPanel(new BorderLayout(0, 8));
                    form.add(new JLabel("Mã vạch " + maVach + " chưa được gán. Chọn sản phẩm cần liên kết:"),
                            BorderLayout.NORTH);
                    form.add(cboSanPham, BorderLayout.CENTER);
                    form.add(new JLabel("Nếu sản phẩm đã có mã vạch, mã cũ sẽ được thay thế."), BorderLayout.SOUTH);
                    int chon = JOptionPane.showConfirmDialog(pnlThanhToan.this, form,
                            "Gán mã vạch cho sản phẩm", JOptionPane.OK_CANCEL_OPTION,
                            JOptionPane.QUESTION_MESSAGE);
                    if (chon != JOptionPane.OK_OPTION)
                        return;
                    if (!(cboSanPham.getSelectedItem() instanceof SanPham)) {
                        JOptionPane.showMessageDialog(pnlThanhToan.this, "Hãy chọn sản phẩm cần gán mã vạch.",
                                "Chưa chọn sản phẩm", JOptionPane.WARNING_MESSAGE);
                        return;
                    }

                    SanPham daChon = (SanPham) cboSanPham.getSelectedItem();
                    new SwingWorker<MaVachDAO.SanPhamQuet, Void>() {
                        @Override
                        protected MaVachDAO.SanPhamQuet doInBackground() throws Exception {
                            return maVachDAO.ganMaVachChoSanPham(maVach, daChon.getMaSP());
                        }

                        @Override
                        protected void done() {
                            try {
                                MaVachDAO.SanPhamQuet daGan = get();
                                lblGoiY.setText("Đã gán mã " + maVach + " cho " + daGan.getTenSP() + ".");
                                themSanPhamVaoDon(daGan, maVach);
                            } catch (Exception ex) {
                                Throwable loi = nguyenNhan(ex);
                                JOptionPane.showMessageDialog(pnlThanhToan.this,
                                        "Không thể gán mã vạch: " + loi.getMessage(),
                                        "Lỗi mã vạch", JOptionPane.ERROR_MESSAGE);
                            }
                        }
                    }.execute();
                } catch (Exception ex) {
                    Throwable loi = nguyenNhan(ex);
                    JOptionPane.showMessageDialog(pnlThanhToan.this,
                            "Không tải được danh sách sản phẩm: " + loi.getMessage(),
                            "Lỗi dữ liệu", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private void themSanPhamVaoDon(MaVachDAO.SanPhamQuet sanPham, String maVach) {
        int soLuongTrongDon = 0;
        for (int row = 0; row < modelDon.getRowCount(); row++) {
            if (LOAI_SP.equals(modelDon.getValueAt(row, C_LOAI))
                    && sanPham.getMaSP().equals(String.valueOf(modelDon.getValueAt(row, C_MA)))) {
                soLuongTrongDon = ((Number) modelDon.getValueAt(row, C_SL)).intValue();
                break;
            }
        }
        if (sanPham.getSoLuongTon() <= soLuongTrongDon) {
            JOptionPane.showMessageDialog(pnlThanhToan.this,
                    sanPham.getSoLuongTon() <= 0
                            ? "Sản phẩm " + sanPham.getTenSP() + " hiện đã hết hàng."
                            : "Chỉ còn " + sanPham.getSoLuongTon() + " sản phẩm trong kho.",
                    "Không đủ tồn kho", JOptionPane.WARNING_MESSAGE);
            return;
        }
        themVaoDon(LOAI_SP, sanPham.getMaSP(), sanPham.getTenSP(), Math.round(sanPham.getGiaBan()));
        lblGoiY.setText("Đã thêm " + sanPham.getTenSP() + " vào đơn hàng • Mã vạch: " + maVach);
    }

    private void moChonPhieuChoThanhToan() {
        Window owner = SwingUtilities.getWindowAncestor(this);
        JDialog dialog = new JDialog(owner, "Chọn phiếu chờ thanh toán", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setLayout(new BorderLayout(8, 8));
        JPanel content = new JPanel(new BorderLayout(8, 8));
        content.setBorder(new EmptyBorder(12, 12, 10, 12));
        content.setBackground(Color.WHITE);

        JPanel top = new JPanel(new BorderLayout(8, 0));
        top.setOpaque(false);
        top.add(label("Chọn phiếu để tự động nạp khách hàng và dịch vụ vào hóa đơn.", 13, TEXT, false),
                BorderLayout.NORTH);
        JTextField search = timKiemField("Tìm mã phiếu, khách hàng, số điện thoại hoặc thú cưng...");
        top.add(search, BorderLayout.SOUTH);
        content.add(top, BorderLayout.NORTH);

        DefaultTableModel model = new DefaultTableModel(new Object[] {
                "Mã phiếu", "Giờ tiếp nhận", "Khách hàng", "Số điện thoại", "Thú cưng", "Dịch vụ", "Phí dự kiến"
        }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable table = new JTable(model);
        table.setFont(FONT);
        table.setRowHeight(28);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setSelectionBackground(new Color(255, 222, 233));
        table.setGridColor(BORDER);
        table.setShowGrid(true);
        table.getTableHeader().setFont(new Font("Times New Roman", Font.BOLD, 12));
        table.getTableHeader().setBackground(LIGHT_PINK);
        table.getTableHeader().setPreferredSize(new Dimension(0, 30));
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);
        table.getColumnModel().getColumn(0).setPreferredWidth(145);
        table.getColumnModel().getColumn(2).setPreferredWidth(145);
        table.getColumnModel().getColumn(5).setPreferredWidth(160);
        table.getColumnModel().getColumn(6).setCellRenderer(taoCurrencyRenderer());
        content.add(new JScrollPane(table), BorderLayout.CENTER);

        JLabel status = label("Đang tải danh sách phiếu...", 12, MUTED, false);
        JButton choose = primaryButton("Chọn phiếu");
        JButton reload = outlineButton("Tải lại");
        JButton close = outlineButton("Đóng");
        JPanel actions = new JPanel(new BorderLayout(8, 0));
        actions.setOpaque(false);
        actions.add(status, BorderLayout.CENTER);
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 7, 0));
        buttons.setOpaque(false);
        buttons.add(reload);
        buttons.add(close);
        buttons.add(choose);
        actions.add(buttons, BorderLayout.EAST);
        content.add(actions, BorderLayout.SOUTH);
        dialog.add(content, BorderLayout.CENTER);

        List<ThanhToanDAO.PhieuChoThanhToanDTO> phieuData = new ArrayList<>();
        Runnable napPhieu = () -> {
            status.setText("Đang tải danh sách phiếu...");
            choose.setEnabled(false);
            reload.setEnabled(false);
            new SwingWorker<List<ThanhToanDAO.PhieuChoThanhToanDTO>, Void>() {
                @Override
                protected List<ThanhToanDAO.PhieuChoThanhToanDTO> doInBackground() throws Exception {
                    return thanhToanDAO.layPhieuChoThanhToan();
                }

                @Override
                protected void done() {
                    try {
                        phieuData.clear();
                        phieuData.addAll(get());
                        model.setRowCount(0);
                        for (ThanhToanDAO.PhieuChoThanhToanDTO phieu : phieuData) {
                            model.addRow(new Object[] { phieu.maHoaDon,
                                    phieu.thoiGianTiepNhan == null ? "—"
                                            : phieu.thoiGianTiepNhan.format(
                                                    DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")),
                                    phieu.tenKH, phieu.sdt, phieu.tenThuCung, tenDichVuTrongPhieu(phieu),
                                    phieu.danhSachDichVu.isEmpty() ? phieu.phiDuKien : phieu.tongPhiDichVu() });
                        }
                        status.setText(phieuData.size()
                                + " phiếu đang chờ thanh toán. Nhấp đúp hoặc chọn rồi bấm Chọn phiếu.");
                        if (phieuData.isEmpty())
                            status.setText("Hiện không có phiếu nào chờ thanh toán.");
                        choose.setEnabled(!phieuData.isEmpty());
                    } catch (Exception ex) {
                        Throwable loi = nguyenNhan(ex);
                        status.setText("Không tải được danh sách: " + loi.getMessage());
                        JOptionPane.showMessageDialog(dialog,
                                "Không thể tải phiếu chờ thanh toán:\n" + loi.getMessage(),
                                "Lỗi cơ sở dữ liệu", JOptionPane.ERROR_MESSAGE);
                    } finally {
                        reload.setEnabled(true);
                    }
                }
            }.execute();
        };
        Runnable chonPhieu = () -> {
            int selected = table.getSelectedRow();
            if (selected < 0) {
                JOptionPane.showMessageDialog(dialog, "Hãy chọn một phiếu trong danh sách.");
                return;
            }
            int modelRow = table.convertRowIndexToModel(selected);
            if (modelRow < 0 || modelRow >= phieuData.size())
                return;
            if (napPhieuChoThanhToan(phieuData.get(modelRow)))
                dialog.dispose();
        };
        choose.addActionListener(e -> chonPhieu.run());
        reload.addActionListener(e -> napPhieu.run());
        close.addActionListener(e -> dialog.dispose());
        search.getDocument().addDocumentListener(new SimpleDocumentListener(() -> {
            String key = search.getText().trim();
            sorter.setRowFilter(key.isEmpty() ? null : RowFilter.regexFilter("(?i)" + Pattern.quote(key)));
        }));
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2)
                    chonPhieu.run();
            }
        });
        table.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke("ENTER"), "chonPhieu");
        table.getActionMap().put("chonPhieu", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                chonPhieu.run();
            }
        });
        dialog.setSize(1050, 520);
        dialog.setMinimumSize(new Dimension(820, 420));
        dialog.setLocationRelativeTo(this);
        napPhieu.run();
        dialog.setVisible(true);
    }

    private boolean napPhieuChoThanhToan(ThanhToanDAO.PhieuChoThanhToanDTO phieu) {
        if (phieu.danhSachDichVu.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Phiếu chưa có dịch vụ gắn với lịch hẹn nên chưa thể nạp vào hóa đơn. "
                            + "Hãy tạo lại phiếu tiếp nhận để hệ thống tạo đơn dịch vụ tương ứng.",
                    "Chưa có dữ liệu dịch vụ", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        for (ThanhToanDAO.DichVuHoaDonDTO dichVu : phieu.danhSachDichVu) {
            if (dichVu.maDichVu == null || dichVu.tenDichVu == null || dichVu.donGia < 0) {
                JOptionPane.showMessageDialog(this, "Dịch vụ trong phiếu chưa có mã, tên hoặc đơn giá hợp lệ.",
                        "Thiếu thông tin dịch vụ", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        }
        boolean donDangNhap = modelDon.getRowCount() > 0 || selectedKh != null || khachMoi
                || !txtPhone.getText().trim().isEmpty() || !txtCustomer.getText().trim().isEmpty();
        if (donDangNhap) {
            int chon = JOptionPane.showConfirmDialog(this,
                    "Nạp phiếu " + phieu.maHoaDon + " sẽ thay thế nội dung hóa đơn đang nhập. Tiếp tục?",
                    "Nạp phiếu chờ thanh toán", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (chon != JOptionPane.YES_OPTION)
                return false;
        }

        modelDon.setRowCount(0);
        cboKhuyenMai.setSelectedIndex(0);
        txtPaid.setText("0");
        chkSuDungDiem.setSelected(false);
        rbCash.setSelected(true);
        rbKhachCu.setSelected(true);
        setCustomerMode(false);
        rbKhachCu.setEnabled(false);
        rbKhachMoi.setEnabled(false);
        datChoPhepNhap(txtPhone, false);
        dangChonKhachHang = true;
        try {
            txtPhone.setText(phieu.sdt == null ? "" : phieu.sdt);
            txtCustomer.setText(phieu.tenKH == null ? "" : phieu.tenKH);
            txtAddress.setText(phieu.diaChi == null ? "" : phieu.diaChi);
            txtMemberType.setText(String.valueOf(phieu.hangThanhVien));
            txtPoints.setText(String.valueOf(phieu.diemTichLuy));
            selectedKh = new KhachHangItem(phieu.maKH, phieu.tenKH, phieu.sdt,
                    phieu.diaChi, phieu.hangThanhVien, phieu.diemTichLuy);
        } finally {
            dangChonKhachHang = false;
        }
        maTiepNhanDangLap = phieu.maHoaDon;
        maDichVuPhieuDangLap.clear();
        lamMoiThongTinDon();
        for (ThanhToanDAO.DichVuHoaDonDTO dichVu : phieu.danhSachDichVu) {
            maDichVuPhieuDangLap.add(dichVu.maDichVu);
            themVaoDon(LOAI_DV, dichVu.maDichVu, dichVu.tenDichVu, dichVu.donGia);
        }
        capNhatTrangThaiDiem();
        lblGoiY.setText("Đang lập hóa đơn cho phiếu " + phieu.maHoaDon + " • Thú cưng: " + phieu.tenThuCung);
        return true;
    }

    private String tenDichVuTrongPhieu(ThanhToanDAO.PhieuChoThanhToanDTO phieu) {
        if (phieu.danhSachDichVu.isEmpty())
            return phieu.dichVuYeuCau;
        List<String> ten = new ArrayList<>();
        for (ThanhToanDAO.DichVuHoaDonDTO dichVu : phieu.danhSachDichVu)
            ten.add(dichVu.tenDichVu);
        return String.join("; ", ten);
    }

    private JPanel createLeft() {
        JPanel left = new JPanel(new BorderLayout(0, 10));
        left.setOpaque(false);
        JPanel top = new JPanel(new BorderLayout(10, 0));
        top.setOpaque(false);
        JPanel orderCard = createOrderCard();
        orderCard.setPreferredSize(new Dimension(270, 0));
        top.add(orderCard, BorderLayout.WEST);
        top.add(createCustomerCard(), BorderLayout.CENTER);
        top.setPreferredSize(new Dimension(0, 236));

        JPanel catalogs = new JPanel(new GridLayout(1, 2, 10, 0));
        catalogs.setOpaque(false);
        catalogs.add(dsSanPham.taoGiaoDien());
        catalogs.add(dsDichVu.taoGiaoDien());
        left.add(top, BorderLayout.NORTH);
        left.add(catalogs, BorderLayout.CENTER);
        return left;
    }

    private JPanel createOrderCard() {
        JPanel content = new JPanel(new BorderLayout());
        content.setOpaque(false);
        content.add(rows(new String[] { "Mã đơn hàng:", "Ngày tạo:", "Nhân viên:" },
                new JComponent[] { txtOrderCode, txtOrderDate, txtEmployee }), BorderLayout.NORTH);
        return card("Thông tin đơn hàng", content);
    }

    private JPanel createCustomerCard() {
        JPanel content = new JPanel(new BorderLayout(0, 4));
        content.setOpaque(false);
        JPanel mode = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        mode.setOpaque(false);
        ButtonGroup group = new ButtonGroup();
        for (JRadioButton rb : new JRadioButton[] { rbKhachCu, rbKhachMoi }) {
            rb.setOpaque(false);
            rb.setFont(new Font("Times New Roman", Font.BOLD, 13));
            rb.setForeground(TEXT);
            rb.setFocusPainted(false);
            group.add(rb);
            mode.add(rb);
        }
        rbKhachCu.addActionListener(e -> setCustomerMode(false));
        rbKhachMoi.addActionListener(e -> setCustomerMode(true));
        content.add(mode, BorderLayout.NORTH);

        JPanel form = rows(new String[] { "Số điện thoại:", "Họ và tên:", "Địa chỉ:",
                "Hạng thành viên:", "Điểm tích lũy:" },
                new JComponent[] { txtPhone, txtCustomer, txtAddress, txtMemberType, txtPoints });
        JPanel formWrap = new JPanel(new BorderLayout());
        formWrap.setOpaque(false);
        formWrap.add(form, BorderLayout.NORTH);
        content.add(formWrap, BorderLayout.CENTER);
        lblGoiY.setBorder(new EmptyBorder(0, 5, 0, 0));
        content.add(lblGoiY, BorderLayout.SOUTH);
        return card("Thông tin khách hàng", content);
    }

    private JPanel createRight() {
        JPanel right = new JPanel(new BorderLayout(0, 10));
        right.setOpaque(false);
        right.setPreferredSize(new Dimension(430, 0));
        right.add(createOrderListCard(), BorderLayout.CENTER);
        right.add(createPaymentCard(), BorderLayout.SOUTH);
        return right;
    }

    private JPanel createOrderListCard() {
        configureOrderTable();
        JPanel content = new JPanel(new BorderLayout(0, 8));
        content.setOpaque(false);
        JScrollPane scroll = new JScrollPane(tblDon);
        scroll.setBorder(new LineBorder(BORDER));
        content.add(scroll, BorderLayout.CENTER);

        JPanel actions = new JPanel(new GridLayout(1, 2, 8, 0));
        actions.setOpaque(false);
        JButton edit = outlineButton("Sửa số lượng");
        JButton remove = outlineButton("-   Xóa dòng");
        edit.addActionListener(e -> suaSoLuong());
        remove.addActionListener(e -> removeSelected());
        actions.add(edit);
        actions.add(remove);
        actions.setPreferredSize(new Dimension(0, 28));
        content.add(actions, BorderLayout.SOUTH);
        return card("Đơn hàng đã chọn", content);
    }

    private JPanel createPaymentCard() {
        JPanel pay = new JPanel();
        pay.setOpaque(false);
        pay.setLayout(new BoxLayout(pay, BoxLayout.Y_AXIS));

        JPanel sumGrid = new JPanel(new GridLayout(5, 2, 6, 4));
        sumGrid.setOpaque(false);
        sumGrid.add(label("Tạm tính:", 13, TEXT, false));
        sumGrid.add(lblSubtotal);

        JPanel disc = new JPanel(new BorderLayout(4, 0));
        disc.setOpaque(false);
        disc.add(label("Khuyến mãi", 13, TEXT, false), BorderLayout.WEST);
        cboKhuyenMai.setPreferredSize(new Dimension(150, 24));
        disc.add(cboKhuyenMai, BorderLayout.CENTER);
        sumGrid.add(disc);
        sumGrid.add(lblDiscount);

        JPanel productDiscount = new JPanel(new BorderLayout(6, 0));
        productDiscount.setOpaque(false);
        productDiscount.add(lblTenKhuyenMaiSanPham, BorderLayout.CENTER);
        productDiscount.add(lblGiamKhuyenMaiSanPham, BorderLayout.EAST);
        sumGrid.add(label("KM sản phẩm tự động:", 13, TEXT, false));
        sumGrid.add(productDiscount);

        chkSuDungDiem.setOpaque(false);
        chkSuDungDiem.setFont(FONT);
        chkSuDungDiem.setForeground(TEXT);
        chkSuDungDiem.setFocusPainted(false);
        chkSuDungDiem
                .setToolTipText("Quy đổi 1 điểm tích lũy thành 100 đ; mức giảm không vượt quá số tiền đơn hàng.");
        sumGrid.add(chkSuDungDiem);
        sumGrid.add(lblGiamDiem);

        sumGrid.add(label("Tổng thanh toán:", 14, TEXT, true));
        lblTotal.setForeground(DARK_PINK);
        lblTotal.setFont(new Font("Times New Roman", Font.BOLD, 18));
        sumGrid.add(lblTotal);
        pay.add(sumGrid);
        pay.add(Box.createVerticalStrut(6));
        pay.add(new JSeparator());
        pay.add(Box.createVerticalStrut(4));

        JPanel radio = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 0));
        radio.setOpaque(false);
        ButtonGroup group = new ButtonGroup();
        for (JRadioButton rb : new JRadioButton[] { rbCash, rbQR }) {
            rb.setOpaque(false);
            rb.setFont(FONT);
            rb.setForeground(TEXT);
            rb.setFocusPainted(false);
            rb.addActionListener(e -> capNhatPhuongThuc());
            group.add(rb);
            radio.add(rb);
        }
        pay.add(radio);
        pay.add(Box.createVerticalStrut(4));

        JPanel line = new JPanel(new BorderLayout(8, 0));
        line.setOpaque(false);
        line.add(label("Khách đưa:", 13, TEXT, false), BorderLayout.WEST);
        txtPaid.setFont(FONT);
        txtPaid.setHorizontalAlignment(JTextField.RIGHT);
        line.add(txtPaid, BorderLayout.CENTER);
        line.add(label("đ", 13, TEXT, false), BorderLayout.EAST);
        pay.add(line);
        pay.add(Box.createVerticalStrut(6));

        JPanel amounts = new JPanel(new GridLayout(1, 4, 5, 0));
        amounts.setOpaque(false);
        List<JButton> quick = new ArrayList<>();
        JButton vuaDu = outlineButton("Vừa đủ");
        vuaDu.addActionListener(e -> txtPaid.setText(MONEY.format(total)));
        amounts.add(vuaDu);
        quick.add(vuaDu);
        for (long amount : new long[] { 500000, 1000000, 2000000 }) {
            JButton b = outlineButton(MONEY.format(amount));
            b.addActionListener(e -> txtPaid.setText(MONEY.format(amount)));
            amounts.add(b);
            quick.add(b);
        }
        nutTienNhanh = quick.toArray(new JButton[0]);
        amounts.setPreferredSize(new Dimension(0, 26));
        pay.add(amounts);
        pay.add(Box.createVerticalStrut(8));

        JPanel changeLine = new JPanel(new BorderLayout());
        changeLine.setOpaque(false);
        changeLine.add(label("Tiền thừa trả khách:", 13, TEXT, false), BorderLayout.WEST);
        lblChange.setForeground(DARK_PINK);
        changeLine.add(lblChange, BorderLayout.EAST);
        pay.add(changeLine);
        pay.add(Box.createVerticalStrut(8));

        JPanel footer = new JPanel(new GridLayout(1, 2, 8, 0));
        footer.setOpaque(false);
        btnConfirm = primaryButton("Xác nhận thanh toán");
        JButton cancel = outlineButton("Làm mới đơn");
        btnConfirm.addActionListener(e -> confirmPayment());
        cancel.addActionListener(e -> cancelPayment());
        footer.add(btnConfirm);
        footer.add(cancel);
        footer.setPreferredSize(new Dimension(0, 34));
        pay.add(footer);

        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);
        wrap.add(pay, BorderLayout.NORTH);
        return card("Thanh toán", wrap);
    }

    private JPanel card(String title, JComponent content) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Color.WHITE);
        p.setBorder(new LineBorder(BORDER));
        JPanel heading = new JPanel(new BorderLayout());
        heading.setBackground(LIGHT_PINK);
        heading.setBorder(new EmptyBorder(7, 10, 7, 10));
        heading.add(label(title, 14, DARK_PINK, true));
        p.add(heading, BorderLayout.NORTH);
        JPanel inner = new JPanel(new BorderLayout());
        inner.setOpaque(false);
        inner.setBorder(new EmptyBorder(8, 9, 9, 9));
        inner.add(content, BorderLayout.CENTER);
        p.add(inner, BorderLayout.CENTER);
        return p;
    }

    private JPanel rows(String[] names, JComponent[] controls) {
        JPanel grid = new JPanel(new GridBagLayout());
        grid.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(2, 5, 2, 5);
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
        return grid;
    }

    private void configureOrderTable() {
        tblDon.setFont(FONT);
        tblDon.setRowHeight(28);
        tblDon.setSelectionBackground(new Color(255, 222, 233));
        tblDon.setGridColor(BORDER);
        tblDon.setShowGrid(true);
        tblDon.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JTableHeader header = tblDon.getTableHeader();
        header.setFont(new Font("Times New Roman", Font.BOLD, 12));
        header.setBackground(LIGHT_PINK);
        header.setPreferredSize(new Dimension(0, 30));
        int[] widths = { 58, 120, 34, 72, 78, 68 };
        for (int i = 0; i < widths.length; i++)
            tblDon.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);

        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(SwingConstants.CENTER);
        tblDon.getColumnModel().getColumn(C_LOAI).setCellRenderer(center);
        tblDon.getColumnModel().getColumn(C_SL).setCellRenderer(center);
        tblDon.getColumnModel().getColumn(C_GIA).setCellRenderer(taoCurrencyRenderer());
        tblDon.getColumnModel().getColumn(C_TIEN).setCellRenderer(taoCurrencyRenderer());
        tblDon.getColumnModel().getColumn(C_GIAM).setCellRenderer(taoCurrencyRenderer());
        tblDon.removeColumn(tblDon.getColumnModel().getColumn(C_MA));

        tblDon.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2)
                    suaSoLuong();
            }
        });
        tblDon.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke("DELETE"), "xoaDong");
        tblDon.getActionMap().put("xoaDong", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                removeSelected();
            }
        });
    }

    private static DefaultTableCellRenderer taoCurrencyRenderer() {
        return new DefaultTableCellRenderer() {
            @Override
            protected void setValue(Object value) {
                setText(value instanceof Number ? MONEY.format(((Number) value).longValue()) + " đ" : "");
                setHorizontalAlignment(SwingConstants.RIGHT);
            }
        };
    }

    // ================== SỬA ĐƠN HÀNG ==================
    private void suaSoLuong() {
        int r = tblDon.getSelectedRow();
        if (r < 0) {
            JOptionPane.showMessageDialog(this, "Hãy chọn một dòng cần sửa.");
            return;
        }
        if (laDichVuGocCuaPhieu(r)) {
            JOptionPane.showMessageDialog(this, "Các dịch vụ đã tiếp nhận được giữ nguyên trên hóa đơn.");
            return;
        }
        Object nhap = JOptionPane.showInputDialog(this,
                "Số lượng cho \"" + modelDon.getValueAt(r, C_TEN) + "\":",
                "Sửa số lượng", JOptionPane.PLAIN_MESSAGE, null, null,
                String.valueOf(modelDon.getValueAt(r, C_SL)));
        if (nhap == null)
            return;
        try {
            int count = Integer.parseInt(nhap.toString().trim());
            if (count <= 0)
                throw new IllegalArgumentException();
            modelDon.setValueAt(count, r, C_SL);
            updateTotals();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Số lượng phải là số nguyên lớn hơn 0.");
        }
    }

    private void removeSelected() {
        int r = tblDon.getSelectedRow();
        if (r < 0) {
            JOptionPane.showMessageDialog(this, "Hãy chọn một dòng cần xóa.");
            return;
        }
        if (laDichVuGocCuaPhieu(r)) {
            JOptionPane.showMessageDialog(this, "Các dịch vụ đã tiếp nhận được giữ nguyên trên hóa đơn.");
            return;
        }
        modelDon.removeRow(r);
        updateTotals();
    }

    private boolean laDichVuGocCuaPhieu(int row) {
        return !maDichVuPhieuDangLap.isEmpty() && row >= 0 && row < modelDon.getRowCount()
                && LOAI_DV.equals(modelDon.getValueAt(row, C_LOAI))
                && maDichVuPhieuDangLap.contains(String.valueOf(modelDon.getValueAt(row, C_MA)));
    }

    // ================== TÍNH TIỀN ==================
    private void capNhatPhuongThuc() {
        boolean tienMat = rbCash.isSelected();
        txtPaid.setEnabled(tienMat);
        for (JButton b : nutTienNhanh)
            b.setEnabled(tienMat);
        updateTotals();
        if (!tienMat) {
            hienThiQR();
        } else if (dlgQR != null) {
            dlgQR.setVisible(false);
        }
    }

    /** Chỉ cho dùng điểm khi đã chọn khách hàng cũ có số dư điểm lớn hơn 0. */
    private void capNhatTrangThaiDiem() {
        boolean coTheDungDiem = !khachMoi
                && selectedKh != null
                && selectedKh.diemTichLuy > 0;
        chkSuDungDiem.setEnabled(coTheDungDiem);
        if (!coTheDungDiem) {
            chkSuDungDiem.setSelected(false);
            soDiemSuDung = 0;
        }
        updateTotals();
    }

    private void updateTotals() {
        long subtotal = 0;
        try {
            for (int i = 0; i < modelDon.getRowCount(); i++) {
                int qty = ((Number) modelDon.getValueAt(i, C_SL)).intValue();
                long price = ((Number) modelDon.getValueAt(i, C_GIA)).longValue();
                long line = Math.multiplyExact((long) qty, price);
                modelDon.setValueAt(line, i, C_TIEN);
                modelDon.setValueAt(0L, i, C_GIAM);
                subtotal = Math.addExact(subtotal, line);
            }
        } catch (ArithmeticException ex) {
            JOptionPane.showMessageDialog(this, "Giá trị đơn hàng vượt giới hạn tính toán.");
            return;
        }

        double discountRate = layTyLeGiamGia();
        long discountInvoice = Math.round(subtotal * discountRate / 100.0);
        khuyenMaiSanPhamDangApDung = layKhuyenMaiSanPhamApDung();
        tienGiamKhuyenMaiSanPham = 0;
        if (khuyenMaiSanPhamDangApDung != null) {
            double rate = Math.max(0, Math.min(100, khuyenMaiSanPhamDangApDung.getPhanTramGiam()));
            java.util.Set<String> maSanPhamDatNguong = layMaSanPhamDatNguong(khuyenMaiSanPhamDangApDung);
            try {
                for (int i = 0; i < modelDon.getRowCount(); i++) {
                    if (!LOAI_SP.equals(modelDon.getValueAt(i, C_LOAI))) continue;
                    String maSP = String.valueOf(modelDon.getValueAt(i, C_MA));
                    if (!maSanPhamDatNguong.contains(maSP)) continue;
                    long line = ((Number) modelDon.getValueAt(i, C_TIEN)).longValue();
                    long giamDong = Math.round(line * rate / 100.0);
                    modelDon.setValueAt(giamDong, i, C_GIAM);
                    tienGiamKhuyenMaiSanPham = Math.addExact(tienGiamKhuyenMaiSanPham, giamDong);
                }
            } catch (ArithmeticException ex) {
                JOptionPane.showMessageDialog(this, "Giá trị khuyến mãi sản phẩm vượt giới hạn tính toán.");
                return;
            }
        }
        long discount = discountInvoice + tienGiamKhuyenMaiSanPham;
        long afterPercentDiscount = Math.max(0L, subtotal - discount);

        // Chỉ trừ điểm khi người dùng chủ động chọn checkbox và có khách hàng hợp lệ.
        soDiemSuDung = 0;
        long giamBangDiem = 0;
        if (chkSuDungDiem.isSelected()
                && !khachMoi
                && selectedKh != null
                && selectedKh.diemTichLuy > 0) {
            long diemToiDaTheoDon = afterPercentDiscount / GIA_TRI_MOI_DIEM;
            soDiemSuDung = (int) Math.min((long) selectedKh.diemTichLuy, diemToiDaTheoDon);
            giamBangDiem = soDiemSuDung * GIA_TRI_MOI_DIEM;
        }

        total = Math.max(0L, afterPercentDiscount - giamBangDiem);
        lblSubtotal.setText(MONEY.format(subtotal) + " đ");
        lblDiscount.setText("-" + MONEY.format(discountInvoice) + " đ");
        lblTenKhuyenMaiSanPham.setText(khuyenMaiSanPhamDangApDung == null
                ? "Không áp dụng" : khuyenMaiSanPhamDangApDung.getTenKhuyenMai());
        lblTenKhuyenMaiSanPham.setToolTipText(khuyenMaiSanPhamDangApDung == null
                ? null : "Sản phẩm: " + String.join(", ", khuyenMaiSanPhamDangApDung.getMaSanPhamApDung())
                        + " | Số lượng tối thiểu mỗi loại: " + khuyenMaiSanPhamDangApDung.getSoLuongToiThieu()
                        + (khuyenMaiSanPhamDangApDung.getDieuKienApDung().isEmpty() ? ""
                                : " | Điều kiện: " + khuyenMaiSanPhamDangApDung.getDieuKienApDung()));
        lblGiamKhuyenMaiSanPham.setText("-" + MONEY.format(tienGiamKhuyenMaiSanPham) + " đ");
        lblGiamDiem.setText("-" + MONEY.format(giamBangDiem) + " đ");
        lblTotal.setText(MONEY.format(total) + " đ");

        long paid;
        try {
            paid = parseMoney(txtPaid.getText());
        } catch (Exception e) {
            paid = 0;
        }
        long change = rbCash.isSelected() ? Math.max(0, paid - total) : 0;
        lblChange.setText(MONEY.format(change) + " đ");
        if (dlgQR != null && dlgQR.isVisible())
            capNhatNoiDungQR();
    }

    // ================== XÁC NHẬN / LƯU ĐƠN ==================
    private void confirmPayment() {
        if (Auth.user == null) {
            JOptionPane.showMessageDialog(this, "Hãy đăng nhập để lập đơn hàng.");
            return;
        }
        if (modelDon.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Chưa chọn sản phẩm hoặc dịch vụ cho đơn hàng.");
            return;
        }
        if (khachMoi) {
            if (!SDT_HOP_LE.matcher(txtPhone.getText().trim()).matches()) {
                JOptionPane.showMessageDialog(this,
                        "Số điện thoại không hợp lệ (phải bắt đầu bằng 0 và gồm đúng 10 chữ số).");
                txtPhone.requestFocusInWindow();
                return;
            }
            if (txtCustomer.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Hãy nhập họ và tên khách hàng.");
                txtCustomer.requestFocusInWindow();
                return;
            }
            if (!txtCustomer.getText().trim().matches("[\\p{L}]+(?:\\s+[\\p{L}]+)*")) {
                JOptionPane.showMessageDialog(this, "Họ tên chỉ được chứa chữ cái và khoảng trắng.");
                txtCustomer.requestFocusInWindow();
                return;
            }
        } else if (selectedKh == null) {
            JOptionPane.showMessageDialog(this,
                    "Hãy tìm và chọn khách hàng theo số điện thoại, hoặc chuyển sang Khách hàng mới.");
            txtPhone.requestFocusInWindow();
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
        luuDonHang();
    }

    private List<Object[]> chupDongHang() {
        List<Object[]> ds = new ArrayList<>();
        for (int i = 0; i < modelDon.getRowCount(); i++) {
            ds.add(new Object[] {
                    modelDon.getValueAt(i, C_LOAI), modelDon.getValueAt(i, C_MA),
                    modelDon.getValueAt(i, C_TEN), modelDon.getValueAt(i, C_SL),
                    modelDon.getValueAt(i, C_GIA), modelDon.getValueAt(i, C_TIEN)
            });
        }
        return ds;
    }

    /**
     * Lưu khách hàng mới (nếu có) và hóa đơn trong cùng một transaction.
     * Lỗi ở bất kỳ bước nào sẽ rollback toàn bộ.
     */
    private void luuDonHang() {
        final boolean themKhach = khachMoi;
        final String ten = txtCustomer.getText().trim();
        final String sdt = txtPhone.getText().trim();
        final String diaChi = txtAddress.getText().trim();
        final KhachHangItem khachCu = selectedKh;
        final String maNV = Auth.user.getMaNV();
        final List<Object[]> dong = chupDongHang();
        final long tongTien = total;
        final String phuongThuc = rbCash.isSelected() ? "Tiền mặt" : "QR Code";
        final KhuyenMai khuyenMai = layKhuyenMaiDangChon();
        final String maKhuyenMai = khuyenMai == null ? null : khuyenMai.getMaKhuyenMai();
        final KhuyenMai khuyenMaiSanPham = khuyenMaiSanPhamDangApDung;
        final int diemSuDung = soDiemSuDung;
        final String maTiepNhan = maTiepNhanDangLap;

        // MỚI: chụp lại dữ liệu hiển thị trước khi form bị reset
        final String ngayLap = txtOrderDate.getText();
        final String tenNV = txtEmployee.getText();
        final String tamTinhTxt = lblSubtotal.getText();
        final String giamKMTxt = lblDiscount.getText();
        final String tenKMSanPhamTxt = lblTenKhuyenMaiSanPham.getText();
        final String giamKMSanPhamTxt = lblGiamKhuyenMaiSanPham.getText();
        final String giamDiemTxt = lblGiamDiem.getText();

        long khachDua;
        try {
            khachDua = rbCash.isSelected() ? parseMoney(txtPaid.getText()) : total;
        } catch (Exception ex) {
            khachDua = total;
        }
        final long tienKhachDua = khachDua;

        btnConfirm.setEnabled(false);
        new SwingWorker<String[], Void>() { // ĐỔI: String -> String[]
            @Override
            protected String[] doInBackground() throws Exception {
                try (Connection con = ConnectDB.getConnection()) {
                    con.setAutoCommit(false);
                    try {
                        String maKH;
                        if (themKhach) {
                            if (thanhToanDAO.tonTaiSDT(con, sdt)) {
                                throw new SQLException("Số điện thoại " + sdt
                                        + " đã tồn tại. Hãy chọn khách hàng cũ.");
                            }
                            maKH = thanhToanDAO.taoMaKhachHang(con);
                            thanhToanDAO.themKhachHang(con, maKH, ten, sdt, diaChi);
                        } else {
                            maKH = khachCu.maKH;
                        }

                        // ĐỔI: nhận về maHD
                        String maHD = luuHoaDon(con, maKH, maNV, dong, tongTien, phuongThuc,
                                maKhuyenMai, khuyenMaiSanPham, diemSuDung, tienKhachDua);
                        if (maTiepNhan != null)
                            tiepNhanThuCungDAO.danhDauDaThanhToan(con, maTiepNhan);
                        con.commit();
                        return new String[] { maKH, maHD }; // ĐỔI
                    } catch (Exception ex) {
                        try {
                            con.rollback();
                        } catch (SQLException rollbackEx) {
                            ex.addSuppressed(rollbackEx);
                        }
                        throw ex;
                    } finally {
                        try {
                            con.setAutoCommit(true);
                        } catch (SQLException ignored) {
                        }
                    }
                }
            }

            @Override
            protected void done() {
                btnConfirm.setEnabled(true);
                try {
                    String[] kq = get(); // ĐỔI
                    String maKH = kq[0];
                    String maHD = kq[1];
                    String ketQua = maTiepNhan == null
                            ? "Đã lập hóa đơn " + maHD + " cho khách " + ten + " (mã KH: " + maKH + ")."
                            : "Đã thanh toán phiếu " + maTiepNhan + " và lập hóa đơn " + maHD
                                    + " cho khách " + ten + ".";
                    JOptionPane.showMessageDialog(pnlThanhToan.this,
                            ketQua, "PETPLUS", JOptionPane.INFORMATION_MESSAGE);

                    // MỚI: xuất file txt sau khi đã lưu DB thành công
                    xuatHoaDon(maHD, maKH, ten, sdt, diaChi, ngayLap, tenNV, phuongThuc, dong,
                            tamTinhTxt, giamKMTxt, tenKMSanPhamTxt, giamKMSanPhamTxt,
                            giamDiemTxt, tongTien, tienKhachDua);

                    lamMoiDonHang();
                } catch (Exception ex) {
                    Throwable loi = nguyenNhan(ex);
                    JOptionPane.showMessageDialog(pnlThanhToan.this,
                            "Không thể lưu đơn hàng: " + loi.getMessage(),
                            "Lỗi cơ sở dữ liệu", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    /**
     * Ghi HoaDon, ChiTietHoaDon, trừ tồn kho và cập nhật điểm tích lũy.
     * Dùng chung Connection (transaction) do luuDonHang() quản lý.
     */
    private String luuHoaDon(Connection con, String maKH, String maNV,
            List<Object[]> dong, long tongTien,
            String phuongThuc, String maKhuyenMai,
            KhuyenMai khuyenMaiSanPham, int diemSuDung, long tienKhachDua) throws SQLException {
        String maHD = thanhToanDAO.taoMaHoaDon(con);
        String maPTTT = thanhToanDAO.layMaPTTT(con, phuongThuc);
        thanhToanDAO.themHoaDon(con, maHD, maNV, maKH, maKhuyenMai, maPTTT);
        thanhToanDAO.themChiTiet(con, maHD, dong, tienKhachDua);
        if (khuyenMaiSanPham != null) {
            thanhToanDAO.themKhuyenMaiSanPham(con, maHD, dong, khuyenMaiSanPham);
        }
        thanhToanDAO.truTonKho(con, dong);
        thanhToanDAO.capNhatDiem(con, maKH, diemSuDung, tongTien);
        return maHD;
    }

    private void cancelPayment() {
        int chon = JOptionPane.showConfirmDialog(this,
                maTiepNhanDangLap == null
                        ? "Xóa các dòng đã chọn và thông tin đang nhập?"
                        : "Bỏ hóa đơn đang lập cho " + maTiepNhanDangLap
                                + "? Phiếu vẫn ở trạng thái Chờ thanh toán.",
                "Hủy thanh toán", JOptionPane.YES_NO_OPTION);
        if (chon == JOptionPane.YES_OPTION)
            lamMoiDonHang();
    }

    private void lamMoiDonHang() {
        modelDon.setRowCount(0);
        maTiepNhanDangLap = null;
        maDichVuPhieuDangLap.clear();
        cboKhuyenMai.setSelectedIndex(0);
        chkSuDungDiem.setSelected(false);
        txtPaid.setText("0");
        rbCash.setSelected(true);
        rbKhachCu.setSelected(true);
        rbKhachCu.setEnabled(true);
        rbKhachMoi.setEnabled(true);
        datChoPhepNhap(txtPhone, true);
        setCustomerMode(false);
        lamMoiThongTinDon();
        capNhatPhuongThuc();
        updateTotals();
    }

    // ================== TIỆN ÍCH ==================
    private static long parseMoney(String value) {
        String cleaned = value.replace(".", "").replace(",", "")
                .replace(" ", "").replace("đ", "").replace("Đ", "").trim();
        if (cleaned.isEmpty())
            return 0;
        return Long.parseLong(cleaned);
    }

    private static Throwable nguyenNhan(Throwable ex) {
        Throwable loi = ex;
        while (loi.getCause() != null && loi.getCause() != loi)
            loi = loi.getCause();
        return loi;
    }

    private static void datChoPhepNhap(JTextField t, boolean editable) {
        t.setEditable(editable);
        t.setBackground(editable ? Color.WHITE : READONLY_BG);
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
        t.setBackground(editable ? Color.WHITE : READONLY_BG);
        t.setBorder(new CompoundBorder(new LineBorder(new Color(225, 229, 234)),
                new EmptyBorder(4, 7, 4, 7)));
        return t;
    }

    private static JTextField timKiemField(String hint) {
        JTextField t = new JTextField() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (getText().isEmpty()) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                            RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                    g2.setFont(getFont());
                    g2.setColor(new Color(160, 165, 175));
                    FontMetrics fm = g2.getFontMetrics();
                    g2.drawString(hint, getInsets().left,
                            (getHeight() - fm.getHeight()) / 2 + fm.getAscent());
                    g2.dispose();
                }
            }
        };
        t.setFont(FONT);
        t.setBorder(new CompoundBorder(new LineBorder(new Color(225, 229, 234)),
                new EmptyBorder(5, 8, 5, 8)));
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

    private static class SimpleDocumentListener implements DocumentListener {
        private final Callback callback;

        SimpleDocumentListener(Callback callback) {
            this.callback = callback;
        }

        @Override
        public void insertUpdate(DocumentEvent e) {
            callback.run();
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            callback.run();
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            callback.run();
        }
    }

    // ================== QR CODE ==================
    private void hienThiQR() {
        if (dlgQR == null) {
            Window owner = SwingUtilities.getWindowAncestor(this);
            dlgQR = new JDialog(owner, "Thanh toán QR Code", Dialog.ModalityType.MODELESS);

            JPanel content = new JPanel(new BorderLayout(14, 0));
            content.setBackground(Color.WHITE);
            content.setBorder(new EmptyBorder(14, 14, 14, 14));

            lblAnhQR.setPreferredSize(new Dimension(320, 320));
            lblAnhQR.setBorder(new LineBorder(BORDER));
            lblAnhQR.setFont(FONT);
            content.add(lblAnhQR, BorderLayout.WEST);

            txtThongTinQR.setEditable(false);
            JScrollPane sp = new JScrollPane(txtThongTinQR);
            sp.setPreferredSize(new Dimension(320, 320));
            sp.setBorder(new LineBorder(BORDER));
            content.add(sp, BorderLayout.CENTER);

            dlgQR.setContentPane(content);
            dlgQR.pack();
            dlgQR.setLocationRelativeTo(this);

        }
        napAnhQR();
        capNhatNoiDungQR();
        dlgQR.setVisible(true);
    }

    private void napAnhQR() {
        File f = new File(DUONG_DAN_QR);
        String lyDo;
        try {
            if (!f.exists()) {
                lyDo = "File không tồn tại tại đường dẫn trên.";
            } else {
                BufferedImage img = ImageIO.read(f);
                if (img == null) {
                    lyDo = "File có tồn tại nhưng Java không đọc được định dạng "
                            + "(có thể là webp/CMYK hoặc đổi đuôi). Hãy mở bằng Paint rồi Save as JPEG/PNG.";
                } else {
                    int max = 310;
                    double tiLe = Math.min((double) max / img.getWidth(), (double) max / img.getHeight());
                    int w = Math.max(1, (int) (img.getWidth() * tiLe));
                    int h = Math.max(1, (int) (img.getHeight() * tiLe));
                    lblAnhQR.setText("");
                    lblAnhQR.setIcon(new ImageIcon(img.getScaledInstance(w, h, Image.SCALE_SMOOTH)));
                    return;
                }
            }
        } catch (Exception ex) {
            lyDo = "Lỗi khi đọc: " + ex;
        }
        lblAnhQR.setIcon(null);
        lblAnhQR.setText("<html><div style='width:290px;text-align:center'>Không tải được ảnh QR<br>"
                + f.getAbsolutePath() + "<br><b>" + lyDo + "</b></div></html>");
    }

    private void capNhatNoiDungQR() {
        StringBuilder sb = new StringBuilder();
        sb.append("<html><body style='font-family:Times New Roman;font-size:12px;margin:8px'>");
        sb.append("<h3 style='color:#d92560;margin:0 0 6px 0'>THÔNG TIN HÓA ĐƠN</h3>");
        sb.append("Mã đơn: <b>").append(esc(txtOrderCode.getText())).append("</b><br>");
        sb.append("Ngày: ").append(esc(txtOrderDate.getText())).append("<br>");
        sb.append("Nhân viên: ").append(esc(txtEmployee.getText())).append("<br>");
        sb.append("Khách hàng: ").append(esc(txtCustomer.getText())).append("<br>");
        sb.append("SĐT: ").append(esc(txtPhone.getText())).append("<hr>");
        sb.append("<table width='100%' cellspacing='0' cellpadding='2'>");
        for (int i = 0; i < modelDon.getRowCount(); i++) {
            sb.append("<tr><td>").append(esc(String.valueOf(modelDon.getValueAt(i, C_TEN))))
                    .append(" x").append(modelDon.getValueAt(i, C_SL))
                    .append("</td><td align='right'>")
                    .append(MONEY.format(((Number) modelDon.getValueAt(i, C_TIEN)).longValue()))
                    .append(" đ</td></tr>");
        }
        sb.append("</table><hr>");
        sb.append("Tạm tính: ").append(lblSubtotal.getText()).append("<br>");
        sb.append("Khuyến mãi hóa đơn: ").append(lblDiscount.getText()).append("<br>");
        sb.append("Khuyến mãi sản phẩm: ").append(esc(lblTenKhuyenMaiSanPham.getText()))
                .append(" ").append(lblGiamKhuyenMaiSanPham.getText()).append("<br>");
        sb.append("Giảm điểm: ").append(lblGiamDiem.getText()).append("<br>");
        sb.append("<p style='font-size:15px;color:#d92560'><b>TỔNG: ")
                .append(MONEY.format(total)).append(" đ</b></p>");
        sb.append("<i>Nội dung CK: ").append(esc(txtOrderCode.getText())).append("</i>");
        sb.append("</body></html>");
        txtThongTinQR.setText(sb.toString());
        txtThongTinQR.setCaretPosition(0);
    }

    private static String esc(String s) {
        if (s == null)
            return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    // ================== XUẤT HÓA ĐƠN ==================
    private static final int RONG_HD = 48;

    private static String canHaiBen(String trai, String phai) {
        int khoang = Math.max(1, RONG_HD - trai.length() - phai.length());
        return trai + " ".repeat(khoang) + phai;
    }

    private static String canGiua(String s) {
        int le = Math.max(0, (RONG_HD - s.length()) / 2);
        return " ".repeat(le) + s;
    }

    private void xuatHoaDon(String maHD, String maKH, String tenKH, String sdt, String diaChi,
            String ngay, String nhanVien, String phuongThuc, List<Object[]> dong,
            String tamTinh, String giamKM, String tenKMSanPham, String giamKMSanPham,
            String giamDiem, long tongTien, long khachDua) {
        try {
            String gach = "=".repeat(RONG_HD);
            String nhat = "-".repeat(RONG_HD);
            StringBuilder sb = new StringBuilder();
            sb.append(gach).append("\r\n");
            sb.append(canGiua("PETPLUS")).append("\r\n");
            sb.append(canGiua("HÓA ĐƠN BÁN HÀNG")).append("\r\n");
            sb.append(gach).append("\r\n");
            sb.append("Mã hóa đơn : ").append(maHD).append("\r\n");
            sb.append("Ngày lập   : ").append(ngay).append("\r\n");
            sb.append("Nhân viên  : ").append(nhanVien).append("\r\n");
            sb.append("Khách hàng : ").append(tenKH).append(" (").append(maKH).append(")\r\n");
            sb.append("SĐT        : ").append(sdt).append("\r\n");
            if (diaChi != null && !diaChi.isEmpty())
                sb.append("Địa chỉ    : ").append(diaChi).append("\r\n");
            sb.append("Thanh toán : ").append(phuongThuc).append("\r\n");
            sb.append(nhat).append("\r\n");

            int stt = 1;
            for (Object[] r : dong) {
                long sl = ((Number) r[3]).longValue();
                long gia = ((Number) r[4]).longValue();
                long tien = ((Number) r[5]).longValue();
                sb.append(stt++).append(". ").append(r[2]).append(" [").append(r[0]).append("]\r\n");
                sb.append(canHaiBen("   " + sl + " x " + MONEY.format(gia) + " đ",
                        MONEY.format(tien) + " đ")).append("\r\n");
            }
            sb.append(nhat).append("\r\n");
            sb.append(canHaiBen("Tạm tính:", tamTinh)).append("\r\n");
            sb.append(canHaiBen("KM hóa đơn:", giamKM)).append("\r\n");
            sb.append(canHaiBen("KM sản phẩm:", giamKMSanPham)).append("\r\n");
            if (!"Không áp dụng".equals(tenKMSanPham))
                sb.append("  ").append(tenKMSanPham).append("\r\n");
            sb.append(canHaiBen("Giảm bằng điểm:", giamDiem)).append("\r\n");
            sb.append(nhat).append("\r\n");
            sb.append(canHaiBen("TỔNG THANH TOÁN:", MONEY.format(tongTien) + " đ")).append("\r\n");
            sb.append(canHaiBen("Khách đưa:", MONEY.format(khachDua) + " đ")).append("\r\n");
            sb.append(canHaiBen("Tiền thừa:", MONEY.format(Math.max(0, khachDua - tongTien)) + " đ"))
                    .append("\r\n");
            sb.append(gach).append("\r\n");
            sb.append(canGiua("Cảm ơn quý khách!")).append("\r\n");
            sb.append(gach).append("\r\n");

            Path thuMuc = Paths.get(THU_MUC_HOA_DON);
            Files.createDirectories(thuMuc);
            Path file = thuMuc.resolve(maHD + ".txt");

            byte[] noiDung = sb.toString().getBytes(StandardCharsets.UTF_8);
            byte[] bom = { (byte) 0xEF, (byte) 0xBB, (byte) 0xBF };
            byte[] out = new byte[bom.length + noiDung.length];
            System.arraycopy(bom, 0, out, 0, bom.length);
            System.arraycopy(noiDung, 0, out, bom.length, noiDung.length);
            Files.write(file, out);

            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.OPEN))
                Desktop.getDesktop().open(file.toFile());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Đơn đã lưu nhưng không xuất được hóa đơn: " + ex.getMessage(),
                    "Xuất hóa đơn", JOptionPane.WARNING_MESSAGE);
        }
    }

}
