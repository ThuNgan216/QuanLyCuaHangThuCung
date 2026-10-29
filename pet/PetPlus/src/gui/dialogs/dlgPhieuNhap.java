package gui.dialogs;

import dao.NhaCungCapDAO;
import dao.NhanVienDAO;
import dao.SanPhamDAO;
import entity.ChiTietPhieuNhap;
import entity.NhaCungCap;
import entity.NhanVien;
import entity.PhieuNhap;
import entity.SanPham;
import java.awt.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import service.NhapHangService;
import utils.Auth;
import utils.PetPlusTheme;

public class dlgPhieuNhap extends JDialog {
    private final JTextField txtMaPN = new JTextField(12);
    private final JComboBox<NhaCungCap> cboNhaCungCap = new JComboBox<>();
    private final JComboBox<NhanVien> cboNhanVien = new JComboBox<>();
    private final JTextField txtNgayLap = new JTextField(12);

    // Khu vực nhập chi tiết sản phẩm
    private final JComboBox<SanPham> cboSanPham = new JComboBox<>();
    private final JTextField txtSoLuong = new JTextField(5);
    private final JTextField txtDonGia = new JTextField(8);
    private final JSpinner spnTrangThaiHSD = createDateSpinner();
    private final JButton btnThemSP = new JButton("Thêm SP");

    private DefaultTableModel modelChiTiet;
    private JTable tableChiTiet;
    private JButton btnHoanTat;
    private final List<ChiTietPhieuNhap> danhSachChiTiet = new ArrayList<>();

    private final NhapHangService nhapHangService = new NhapHangService();
    private final SanPhamDAO sanPhamDAO = new SanPhamDAO();
    private final NhanVienDAO nhanVienDAO = new NhanVienDAO();
    private final NhaCungCapDAO nhaCungCapDAO = new NhaCungCapDAO();

    private boolean daLuu = false;

    public dlgPhieuNhap(Window owner) {
        super(owner, "Tập Lập Phiếu Nhập Kho", ModalityType.APPLICATION_MODAL);
        setSize(880, 620);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(12, 12));

        khoiTaoGiaoDien();
        taiDuLieuComboBox();
        tuDongSinhMaPhieuNhap();
        PetPlusTheme.apply(getContentPane());
    }

    private JSpinner createDateSpinner() {
        JSpinner spinner = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor editor = new JSpinner.DateEditor(spinner, "yyyy-MM-dd");
        spinner.setEditor(editor);
        return spinner;
    }

    private void khoiTaoGiaoDien() {
        // --- 1. PHẦN THÔNG TIN PHIẾU NHẬP (NORTH) ---
        JPanel pnlTop = new JPanel(new GridBagLayout());
        pnlTop.setBorder(BorderFactory.createTitledBorder("Thông tin phiếu nhập"));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(8, 12, 8, 12);
        g.fill = GridBagConstraints.HORIZONTAL;

        txtMaPN.setEditable(false);
        txtNgayLap.setText(LocalDate.now().toString());
        txtNgayLap.setEditable(false);

        JButton btnThemNhanhSP = new JButton("+");
        btnThemNhanhSP.setToolTipText("Thêm mới sản phẩm nhanh vào hệ thống");
        btnThemNhanhSP.setPreferredSize(new Dimension(45, 28));
        btnThemNhanhSP.setBackground(new Color(250, 48, 115));
        btnThemNhanhSP.setForeground(Color.WHITE);

        // Sự kiện click nút dấu cộng (+) để tạo nhanh sản phẩm
        btnThemNhanhSP.addActionListener(e -> {
            gui.dialogs.dlgSanPham dlg = new gui.dialogs.dlgSanPham(SwingUtilities.getWindowAncestor(this), null);
            dlg.setVisible(true);
            if (dlg.isDaLuu()) {
                NhaCungCap ncc = (NhaCungCap) cboNhaCungCap.getSelectedItem();
                if (ncc != null) {
                    capNhatDanhSachSanPhamTheoNCC(ncc.getMaNCC());
                    if (cboSanPham.getItemCount() > 0) {
                        cboSanPham.setSelectedIndex(cboSanPham.getItemCount() - 1);
                    }
                }
            }
        });

        // Custom Renderer cho ComboBox Nhà Cung Cấp
        cboNhaCungCap.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof NhaCungCap) {
                    NhaCungCap ncc = (NhaCungCap) value;
                    setText(ncc.getMaNCC() + " - " + ncc.getTenNCC());
                }
                return this;
            }
        });

        cboNhaCungCap.addActionListener(e -> {
            NhaCungCap nccSelected = (NhaCungCap) cboNhaCungCap.getSelectedItem();
            if (nccSelected != null) {
                capNhatDanhSachSanPhamTheoNCC(nccSelected.getMaNCC());
            }
        });

        // Custom Renderer cho ComboBox Nhân Viên
        cboNhanVien.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof NhanVien) {
                    NhanVien nv = (NhanVien) value;
                    setText(nv.getMaNV() + " - " + nv.getTenNV());
                }
                return this;
            }
        });

        int row = 0;
        g.gridx = 0; g.gridy = row; g.weightx = 0.1; pnlTop.add(new JLabel("Mã Phiếu Nhập:"), g);
        g.gridx = 1; g.weightx = 0.4; pnlTop.add(txtMaPN, g);
        g.gridx = 2; g.weightx = 0.1; pnlTop.add(new JLabel("Nhà Cung Cấp:"), g);
        g.gridx = 3; g.weightx = 0.4; pnlTop.add(cboNhaCungCap, g);

        row++;
        g.gridx = 0; g.gridy = row; g.weightx = 0.1; pnlTop.add(new JLabel("Nhân Viên Nhập:"), g);
        g.gridx = 1; g.weightx = 0.4; pnlTop.add(cboNhanVien, g);
        g.gridx = 2; g.weightx = 0.1; pnlTop.add(new JLabel("Ngày Lập:"), g);
        g.gridx = 3; g.weightx = 0.4; pnlTop.add(txtNgayLap, g);

        add(pnlTop, BorderLayout.NORTH);

        // --- 2. PHẦN CHI TIẾT SẢN PHẨM & BẢNG (CENTER) ---
        JPanel pnlCenter = new JPanel(new BorderLayout(8, 8));
        pnlCenter.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
        
        // Chia 2 dòng rõ rệt cho khu vực thêm sản phẩm
        JPanel pnlAddSP = new JPanel(new GridLayout(2, 1, 5, 8));
        pnlAddSP.setBorder(BorderFactory.createTitledBorder("Thêm sản phẩm vào phiếu nhập"));

        // === DÒNG 1: Chọn sản phẩm + Nút thêm nhanh (+) ===
        JPanel pnlRow1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        pnlRow1.setOpaque(false);
        cboSanPham.setPreferredSize(new Dimension(400, 28));
        cboSanPham.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof SanPham) {
                    SanPham sp = (SanPham) value;
                    setText(sp.getMaSP() + " - " + sp.getTenSP());
                }
                return this;
            }
        });
        pnlRow1.add(new JLabel("Sản phẩm:"));
        pnlRow1.add(cboSanPham);
        pnlRow1.add(btnThemNhanhSP);

        // === DÒNG 2: Số lượng, Đơn giá, HSD, Nút Thêm SP ===
        JPanel pnlRow2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        pnlRow2.setOpaque(false);
        txtSoLuong.setPreferredSize(new Dimension(70, 28));
        txtDonGia.setPreferredSize(new Dimension(120, 28));
        spnTrangThaiHSD.setPreferredSize(new Dimension(120, 28));

        pnlRow2.add(new JLabel("Số lượng:"));
        pnlRow2.add(txtSoLuong);
        pnlRow2.add(new JLabel("Đơn giá:"));
        pnlRow2.add(txtDonGia);
        pnlRow2.add(new JLabel("Hạn sử dụng:"));
        pnlRow2.add(spnTrangThaiHSD);
        pnlRow2.add(btnThemSP);

        pnlAddSP.add(pnlRow1);
        pnlAddSP.add(pnlRow2);
        pnlCenter.add(pnlAddSP, BorderLayout.NORTH);

        // Sự kiện: Khi chọn Sản phẩm, tự động gợi ý giá nhập gần nhất
        cboSanPham.addActionListener(e -> {
            SanPham sp = (SanPham) cboSanPham.getSelectedItem();
            NhaCungCap ncc = (NhaCungCap) cboNhaCungCap.getSelectedItem();
            if (sp != null && ncc != null) {
                try {
                    float giaCu = sanPhamDAO.layGiaNhapGanNhat(sp.getMaSP(), ncc.getMaNCC());
                    if (giaCu > 0) {
                        txtDonGia.setText(String.format("%.0f", giaCu));
                    } else {
                        txtDonGia.setText("");
                    }
                } catch (Exception ignored) {}
            }
        });

        // Bảng chi tiết sản phẩm nhập
        String[] columns = {"Mã SP", "Tên Sản Phẩm", "Số Lượng", "Đơn Giá Nhập", "Hạn Sử Dụng", "Thành Tiền"};
        modelChiTiet = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tableChiTiet = new JTable(modelChiTiet);
        tableChiTiet.setRowHeight(28);
        tableChiTiet.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        pnlCenter.add(new JScrollPane(tableChiTiet), BorderLayout.CENTER);
        JButton btnXoaDong = new JButton("Xóa dòng đã chọn");
        JPanel pnlXoaDong = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
        pnlXoaDong.add(btnXoaDong);
        pnlCenter.add(pnlXoaDong, BorderLayout.SOUTH);
        btnXoaDong.addActionListener(e -> xoaDongChiTiet());

        add(pnlCenter, BorderLayout.CENTER);

        // --- 3. PHẦN NÚT HOÀN TẤT & HỦY (SOUTH) ---
        btnHoanTat = new JButton("Hoàn Tất & Nhập Kho");
        btnHoanTat.setEnabled(false);
        JButton btnHuy = new JButton("Hủy Bỏ");
        btnHoanTat.setBackground(new Color(250, 48, 115));
        btnHoanTat.setForeground(Color.WHITE);

        JPanel pnlBottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        pnlBottom.add(btnHoanTat);
        pnlBottom.add(btnHuy);
        add(pnlBottom, BorderLayout.SOUTH);

        // Sự kiện nút bấm
        btnThemSP.addActionListener(e -> xuLyThemSanPham());
        btnHoanTat.addActionListener(e -> xuLyHoanTat());
        btnHuy.addActionListener(e -> dispose());
    }

    private void taiDuLieuComboBox() {
        try {
            List<NhaCungCap> dsNCC = nhaCungCapDAO.layDanhSach();
            for (NhaCungCap ncc : dsNCC) {
                cboNhaCungCap.addItem(ncc);
            }

            cboNhanVien.setEnabled(false);
            NhanVien taiKhoanDangNhap = Auth.user == null
                    ? null : nhanVienDAO.timTheoMa(Auth.user.getMaNV());
            if (taiKhoanDangNhap == null) {
                throw new IllegalStateException("Không tìm thấy nhân viên đang đăng nhập trong database.");
            }
            cboNhanVien.addItem(taiKhoanDangNhap);
            cboNhanVien.setSelectedIndex(0);
            btnHoanTat.setEnabled(true);
        } catch (Exception ex) {
            btnHoanTat.setEnabled(false);
            JOptionPane.showMessageDialog(this, "Lỗi tải dữ liệu danh mục: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void xoaDongChiTiet() {
        int selected = tableChiTiet.getSelectedRow();
        if (selected < 0) {
            JOptionPane.showMessageDialog(this, "Chọn dòng sản phẩm cần xóa.", "Chưa chọn dòng",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        int row = tableChiTiet.convertRowIndexToModel(selected);
        if (row >= 0 && row < danhSachChiTiet.size()) {
            danhSachChiTiet.remove(row);
            modelChiTiet.removeRow(row);
        }
    }

    private void tuDongSinhMaPhieuNhap() {
        try {
            dao.PhieuNhapDAO dao = new dao.PhieuNhapDAO();
            List<PhieuNhap> ds = dao.layDanhSach();
            int stt = ds.size() + 1;
            String maMoi;
            
            while (true) {
                maMoi = String.format("PN%03d", stt);
                boolean trung = false;
                for (PhieuNhap pn : ds) {
                    if (pn.getMaPN().equalsIgnoreCase(maMoi)) {
                        trung = true;
                        break;
                    }
                }
                if (!trung) break;
                stt++;
            }
            txtMaPN.setText(maMoi);
        } catch (Exception e) {
            txtMaPN.setText("PN001");
        }
    }

    private void xuLyThemSanPham() {
        try {
            SanPham sp = (SanPham) cboSanPham.getSelectedItem();
            if (sp == null) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn sản phẩm.", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String slText = txtSoLuong.getText().trim();
            String giaText = txtDonGia.getText().trim();
            if (slText.isEmpty() || giaText.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Vui lòng nhập số lượng và đơn giá.", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
                return;
            }

            int soLuong = Integer.parseInt(slText);
            if (soLuong <= 0) {
                throw new IllegalArgumentException("Số lượng nhập phải lớn hơn 0.");
            }

            BigDecimal donGia = new BigDecimal(giaText);
            if (donGia.compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("Đơn giá không được âm.");
            }

            Date dateHSD = (Date) spnTrangThaiHSD.getValue();
            LocalDate hsd = dateHSD.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

            PhieuNhap pnTemp = new PhieuNhap(txtMaPN.getText().trim());
            ChiTietPhieuNhap ct = new ChiTietPhieuNhap(soLuong, donGia, hsd, pnTemp, sp);

            danhSachChiTiet.add(ct);
            BigDecimal thanhTien = donGia.multiply(new BigDecimal(soLuong));

            modelChiTiet.addRow(new Object[]{
                    sp.getMaSP(),
                    sp.getTenSP(),
                    soLuong,
                    String.format("%,.0f đ", donGia),
                    hsd.toString(),
                    String.format("%,.0f đ", thanhTien)
            });

            txtSoLuong.setText("");
            txtDonGia.setText("");
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Số lượng và đơn giá phải là định dạng số hợp lệ.", "Lỗi định dạng", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void xuLyHoanTat() {
        try {
            if (danhSachChiTiet.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Phiếu nhập chưa có sản phẩm nào.", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String maPN = txtMaPN.getText().trim();
            LocalDate ngayNhap = LocalDate.now();
            NhanVien nv = (NhanVien) cboNhanVien.getSelectedItem();
            NhaCungCap ncc = (NhaCungCap) cboNhaCungCap.getSelectedItem();

            if (nv == null || ncc == null) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn nhân viên lập phiếu và nhà cung cấp.", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
                return;
            }

            PhieuNhap pn = new PhieuNhap();
            pn.setMaPN(maPN);
            pn.setNgayNhap(ngayNhap);
            pn.setMaNV(nv);
            pn.setMaNCC(ncc);

            try (java.sql.Connection con = database.ConnectDB.getConnection()) {
                con.setAutoCommit(false);
                try {
                    dao.PhieuNhapDAO pnDao = new dao.PhieuNhapDAO();
                    pnDao.them(pn, con);

                    dao.ChiTietPhieuNhapDAO ctDao = new dao.ChiTietPhieuNhapDAO();
                    dao.SanPhamDAO spDao = new dao.SanPhamDAO();

                    for (ChiTietPhieuNhap ct : danhSachChiTiet) {
                        ctDao.them(ct, con);

                        SanPham spCu = spDao.timTheoMa(ct.getMaSP().getMaSP());
                        if (spCu != null) {
                            int tonKhoMoi = spCu.getSoLuongTon() + ct.getSoLuongNhap();
                            String sqlUpdate = "UPDATE SanPham SET soLuongTon = ? WHERE maSP = ?";
                            try (java.sql.PreparedStatement ps = con.prepareStatement(sqlUpdate)) {
                                ps.setInt(1, tonKhoMoi);
                                ps.setString(2, spCu.getMaSP());
                                ps.executeUpdate();
                            }
                        }
                    }

                    con.commit();
                } catch (Exception exSQL) {
                    con.rollback();
                    throw exSQL;
                }
            }

            JOptionPane.showMessageDialog(this, "Lập phiếu nhập kho thành công!");
            daLuu = true;
            dispose();

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi khi lưu phiếu nhập: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isDaLuu() {
        return daLuu;
    }

    private void capNhatDanhSachSanPhamTheoNCC(String maNCC) {
        try {
            cboSanPham.removeAllItems();
            List<SanPham> dsSP = sanPhamDAO.laySanPhamTheoNhaCungCapTuLichSu(maNCC);
            for (SanPham sp : dsSP) {
                cboSanPham.addItem(sp);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi tải sản phẩm theo NCC: " + ex.getMessage());
        }
    }
}
