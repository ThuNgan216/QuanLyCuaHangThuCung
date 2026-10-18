package gui.panel;

import dao.NhanVienDAO;
import dao.TaiKhoanDAO;
import entity.NhanVien;
import entity.TaiKhoan;
import gui.dialogs.dlgNhanVien.PetPlusUI;
import gui.dialogs.dlgNhanVien.VaiTroHelper;
import gui.dialogs.dlgNhanVien;
import gui.dialogs.dlgNhanVien.PetPlusDialogs;
import gui.dialogs.dlgPhanQuyen;
import gui.dialogs.dlgTaiKhoan;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class pnlNhanVien extends JPanel {
    private final NhanVienDAO nhanVienDAO = new NhanVienDAO();
    private final TaiKhoanDAO taiKhoanDAO = new TaiKhoanDAO();
    private List<NhanVien> danhSach = new ArrayList<>();

    private final JTextField txtTimNV = PetPlusUI.textField();
    private final JTextField txtTimTK = PetPlusUI.textField();
    private final JTable tblNV;
    private final JTable tblTK;
    private final JTable tblQuyen;
    private final DefaultTableModel modelNV;
    private final DefaultTableModel modelTK;
    private final DefaultTableModel modelQuyen;
    private final JTabbedPane tabs = new JTabbedPane();
    private final JLabel lblCountNV = countLabel();
    private final JLabel lblCountTK = countLabel();
    private final JLabel lblCountQuyen = countLabel();

    public pnlNhanVien() {
        setLayout(new BorderLayout(0, 22));
        setBackground(PetPlusUI.BACKGROUND);
        setBorder(new EmptyBorder(22, 25, 22, 25));

        JPanel titlePanel = PetPlusUI.panel(new GridLayout(2, 1, 0, 7), PetPlusUI.BACKGROUND);
        JLabel title = new JLabel("QUẢN LÝ NHÂN VIÊN");
        title.setFont(PetPlusUI.font(Font.BOLD, 27));
        title.setForeground(PetPlusUI.PRIMARY_DARK);
        JLabel subtitle = new JLabel("Quản lý thông tin, tài khoản và phân quyền nhân viên PetPlus");
        subtitle.setFont(PetPlusUI.font(Font.PLAIN, 14));
        subtitle.setForeground(PetPlusUI.TEXT);
        titlePanel.add(title);
        titlePanel.add(subtitle);
        add(titlePanel, BorderLayout.NORTH);

        modelNV = model("Mã NV", "Họ tên", "Số điện thoại", "CCCD", "Địa chỉ", "Email", "Chức vụ", "Trạng thái");
        modelTK = model("Mã TK", "Mã NV", "Tên nhân viên", "Tên đăng nhập", "Chức vụ", "Vai trò", "Trạng thái");
        modelQuyen = model("Mã TK", "Tên nhân viên", "Chức vụ", "Mã quyền", "Quyền sử dụng", "Trạng thái");
        tblNV = new JTable(modelNV);
        tblTK = new JTable(modelTK);
        tblQuyen = new JTable(modelQuyen);
        PetPlusUI.styleTable(tblNV);
        PetPlusUI.styleTable(tblTK);
        PetPlusUI.styleTable(tblQuyen);

        tabs.setFont(PetPlusUI.font(Font.BOLD, 14));
        tabs.setForeground(PetPlusUI.PRIMARY_DARK);
        tabs.setBackground(PetPlusUI.WHITE);
        tabs.addTab("Nhân viên", trangNhanVien());
        tabs.addTab("Quản lý tài khoản", trangTaiKhoan());
        tabs.addTab("Phân quyền", trangPhanQuyen());
        add(tabs, BorderLayout.CENTER);
        taiDanhSach();
    }

    public void chonTab(String tenTab) {
        for (int i = 0; i < tabs.getTabCount(); i++) {
            if (tenTab.equals(tabs.getTitleAt(i))) {
                tabs.setSelectedIndex(i);
                return;
            }
        }
    }

    private static DefaultTableModel model(String... titles) {
        return new DefaultTableModel(titles, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
    }

    private static JLabel countLabel() {
        JLabel l = new JLabel("0 bản ghi");
        l.setFont(PetPlusUI.font(Font.ITALIC, 12));
        l.setForeground(PetPlusUI.MUTED);
        return l;
    }

    private JPanel trangNhanVien() {
        JPanel p = basePage();
        JButton btnTim = PetPlusUI.button("Tìm kiếm", true);
        JButton btnReset = PetPlusUI.button("Làm mới", false);
        JPanel search = createSearchBar("Tìm nhân viên:", txtTimNV, btnTim, btnReset);
        btnTim.addActionListener(e -> timNhanVien());
        txtTimNV.addActionListener(e -> timNhanVien());
        btnReset.addActionListener(e -> { txtTimNV.setText(""); taiDanhSach(); });
        p.add(search, BorderLayout.NORTH);
        p.add(PetPlusUI.scrollTable(tblNV), BorderLayout.CENTER);

        JButton btnThem = PetPlusUI.button("+ Thêm nhân viên", true);
        JButton btnSua = PetPlusUI.button("Sửa thông tin", true);
        JButton btnNgung = PetPlusUI.button("Ngừng hoạt động", true);
        btnThem.addActionListener(e -> themNhanVien());
        btnSua.addActionListener(e -> suaNhanVien());
        btnNgung.addActionListener(e -> ngungHoatDong());
        p.add(createFooter(lblCountNV, btnThem, btnSua, btnNgung), BorderLayout.SOUTH);
        tblNV.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) suaNhanVien();
            }
        });
        return p;
    }

    private JPanel trangTaiKhoan() {
        JPanel p = basePage();
        JButton btnTim = PetPlusUI.button("Tìm tài khoản", true);
        JButton btnReset = PetPlusUI.button("Làm mới", false);
        p.add(createSearchBar("Tìm tài khoản:", txtTimTK, btnTim, btnReset), BorderLayout.NORTH);
        btnTim.addActionListener(e -> locTaiKhoan());
        txtTimTK.addActionListener(e -> locTaiKhoan());
        btnReset.addActionListener(e -> { txtTimTK.setText(""); taiDanhSach(); });
        p.add(PetPlusUI.scrollTable(tblTK), BorderLayout.CENTER);

        JButton add = PetPlusUI.button("+ Thêm tài khoản", true);
        JButton edit = PetPlusUI.button("Cập nhật", true);
    
        JButton lock = PetPlusUI.button("Khóa tài khoản", false);
        JButton unlock = PetPlusUI.button("Mở khóa", false);
        add.setToolTipText("Tạo hồ sơ nhân viên và tài khoản gắn với hồ sơ");
       
        add.addActionListener(e -> themNhanVien());
        edit.addActionListener(e -> capNhatTaiKhoan());
        lock.addActionListener(e -> doiTrangThai("Ngừng hoạt động"));
        unlock.addActionListener(e -> doiTrangThai("Hoạt động"));
     
        p.add(createFooter(lblCountTK, add, edit, lock, unlock), BorderLayout.SOUTH);
        tblTK.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) capNhatTaiKhoan();
            }
        });
        return p;
    }

    private JPanel trangPhanQuyen() {
        JPanel p = basePage();
        
        JPanel heading = PetPlusUI.panel(new BorderLayout(), PetPlusUI.WHITE);
        heading.setBorder(new EmptyBorder(18, 20, 18, 20));
        
        p.add(heading, BorderLayout.NORTH);
        p.add(PetPlusUI.scrollTable(tblQuyen), BorderLayout.CENTER);
        JButton edit = PetPlusUI.button("Cập nhật phân quyền", true);
        edit.setPreferredSize(new Dimension(190, 40));
        edit.addActionListener(e -> phanQuyen());
        p.add(createFooter(lblCountQuyen, edit), BorderLayout.SOUTH);
        return p;
    }

    private JPanel basePage() {
        JPanel p = PetPlusUI.panel(new BorderLayout(0, 17), PetPlusUI.BACKGROUND);
        p.setBorder(new EmptyBorder(16, 0, 0, 0));
        return p;
    }

    private JPanel createSearchBar(String title, JTextField field, JButton tim, JButton reset) {
        JPanel bar = PetPlusUI.panel(new BorderLayout(14, 0), PetPlusUI.WHITE);
        bar.setBorder(new EmptyBorder(15, 18, 15, 18));
        JPanel actions = PetPlusUI.panel(new FlowLayout(FlowLayout.RIGHT, 9, 0), PetPlusUI.WHITE);
        actions.add(tim);
        actions.add(reset);
        bar.add(PetPlusUI.label(title), BorderLayout.WEST);
        bar.add(field, BorderLayout.CENTER);
        bar.add(actions, BorderLayout.EAST);
        return bar;
    }

    private JPanel createFooter(JLabel count, JButton... buttons) {
        JPanel footer = PetPlusUI.panel(new BorderLayout(7, 0), PetPlusUI.BACKGROUND);
        footer.setBorder(new EmptyBorder(4, 0, 5, 0));
        footer.add(count, BorderLayout.WEST);
        JPanel actions = PetPlusUI.panel(new FlowLayout(FlowLayout.RIGHT, 9, 0), PetPlusUI.BACKGROUND);
        for (JButton b : buttons) actions.add(b);
        footer.add(actions, BorderLayout.CENTER);
        return footer;
    }

   
    private void taiDanhSach() {
        try {
            danhSach = nhanVienDAO.layDanhSach();
            hienThiNhanVien(danhSach);
            locTaiKhoan();
            hienThiQuyen(danhSach);
        } catch (SQLException e) { sqlError("Không thể tải danh sách", e); }
    }

    private void hienThiNhanVien(List<NhanVien> ds) {
        modelNV.setRowCount(0);
        for (NhanVien nv : ds) {
            TaiKhoan tk = nv.getMaTK();
            modelNV.addRow(new Object[]{nv.getMaNV(), nv.getTenNV(), nv.getSdt(), nv.getCccd(),
                nv.getDiaChi(), nv.getEmail(), nv.getChucVu(), tk == null ? "" : tk.getTrangThai()});
        }
        lblCountNV.setText(ds.size() + " nhân viên");
    }

    private void hienThiTaiKhoan(List<NhanVien> ds) {
        modelTK.setRowCount(0);
        for (NhanVien nv : ds) {
            TaiKhoan tk = nv.getMaTK();
            if (tk == null) continue;
            modelTK.addRow(new Object[]{tk.getMaTK(), nv.getMaNV(), nv.getTenNV(),
                tk.getTenDangNhap(), nv.getChucVu(), VaiTroHelper.tenVaiTro(tk.getVaiTro()), tk.getTrangThai()});
        }
        lblCountTK.setText(modelTK.getRowCount() + " tài khoản");
    }

    private void hienThiQuyen(List<NhanVien> ds) {
        modelQuyen.setRowCount(0);
        for (NhanVien nv : ds) {
            TaiKhoan tk = nv.getMaTK();
            if (tk == null) continue;
            modelQuyen.addRow(new Object[]{tk.getMaTK(), nv.getTenNV(), nv.getChucVu(),
                tk.getVaiTro(), VaiTroHelper.tenVaiTro(tk.getVaiTro()), tk.getTrangThai()});
        }
        lblCountQuyen.setText(modelQuyen.getRowCount() + " quyền tài khoản");
    }

    private void timNhanVien() {
        String tuKhoa = txtTimNV.getText().trim();
        try {
            List<NhanVien> ds = nhanVienDAO.timKiem(tuKhoa);
            hienThiNhanVien(ds);
            if (ds.isEmpty()) PetPlusDialogs.info(this, "Kết quả tìm kiếm", "Không tìm thấy nhân viên phù hợp.");
        } catch (SQLException e) { sqlError("Không thể tìm nhân viên", e); }
    }

    private void locTaiKhoan() {
        String k = txtTimTK.getText().trim().toLowerCase(Locale.ROOT);
        if (k.isEmpty()) { hienThiTaiKhoan(danhSach); return; }
        List<NhanVien> filtered = new ArrayList<>();
        for (NhanVien nv : danhSach) {
            TaiKhoan tk = nv.getMaTK();
            if (tk == null) continue;
            String joined = (nv.getMaNV() + " " + nv.getTenNV() + " " + tk.getMaTK() +
                " " + tk.getTenDangNhap() + " " + tk.getVaiTro()).toLowerCase(Locale.ROOT);
            if (joined.contains(k)) filtered.add(nv);
        }
        hienThiTaiKhoan(filtered);
    }

    
    private NhanVien selected(JTable table, DefaultTableModel model, int col, boolean keyIsAccount) {
        int row = table.getSelectedRow();
        if (row < 0) {
            PetPlusDialogs.info(this, "Chưa chọn dữ liệu", "Hãy nhấp chọn một dòng trong bảng trước khi thao tác.");
            return null;
        }
        int mRow = table.convertRowIndexToModel(row);
        String key = String.valueOf(model.getValueAt(mRow, col));
        for (NhanVien nv : danhSach) {
            if (!keyIsAccount && key.equalsIgnoreCase(nv.getMaNV())) return nv;
            if (keyIsAccount && nv.getMaTK() != null && key.equalsIgnoreCase(nv.getMaTK().getMaTK())) return nv;
        }
        PetPlusDialogs.error(this, "Dữ liệu cũ", "Không tìm thấy hồ sơ tương ứng. Hãy làm mới danh sách.");
        return null;
    }

    private NhanVien getNV() { return selected(tblNV, modelNV, 0, false); }
    private NhanVien getTK() { return selected(tblTK, modelTK, 0, true); }
    private NhanVien getQuyen() { return selected(tblQuyen, modelQuyen, 0, true); }

    private NhanVien layMoiNhat(String maNV) throws SQLException {
        for (NhanVien nv : nhanVienDAO.layDanhSach())
            if (nv.getMaNV().equalsIgnoreCase(maNV)) return nv;
        return null;
    }

    private void themNhanVien() {
        dlgNhanVien d = new dlgNhanVien(SwingUtilities.getWindowAncestor(this), null);
        NhanVien nv = d.open();
        if (nv == null) return;
        try {
            if (nhanVienDAO.them(nv)) {
                taiDanhSach();
                PetPlusDialogs.info(this, "Tạo thành công", "Đã tạo hồ sơ " + nv.getMaNV()
                    + " và tài khoản: " + nv.getMaTK().getTenDangNhap());
            }
        } catch (SQLException e) { sqlError("Không thể thêm nhân viên. Kiểm tra mã NV, mã TK, tên đăng nhập, CCCD trùng.", e); }
    }

    private void suaNhanVien() {
        NhanVien nv = getNV();
        if (nv == null) return;
        try {
            NhanVien latest = layMoiNhat(nv.getMaNV());
            if (latest == null) {
                PetPlusDialogs.error(this, "Nhân viên không còn tồn tại", "Vui lòng làm mới bảng.");
                taiDanhSach();
                return;
            }
            dlgNhanVien d = new dlgNhanVien(SwingUtilities.getWindowAncestor(this), latest);
            NhanVien update = d.open();
            if (update == null) return;
            if (nhanVienDAO.capNhat(update)) {
                taiDanhSach();
                PetPlusDialogs.info(this, "Cập nhật thành công", "Thông tin nhân viên đã được lưu.");
            } else PetPlusDialogs.error(this, "Không thành công", "Không tìm thấy nhân viên để sửa.");
        } catch (SQLException e) { sqlError("Không thể sửa nhân viên", e); }
    }

    
private void ngungHoatDong() {
    NhanVien nv = getNV();

    if (nv == null) {
        return;
    }

    TaiKhoan tk = nv.getMaTK();

    if (tk == null) {
        PetPlusDialogs.error(
            this,
            "Lỗi",
            "Nhân viên chưa có tài khoản."
        );
        return;
    }

    if ("Ngừng hoạt động".equals(tk.getTrangThai())) {
        PetPlusDialogs.info(
            this,
            "Thông báo",
            "Tài khoản này đã ngừng hoạt động."
        );
        return;
    }

    if (!PetPlusDialogs.confirm(
            this,
            "Ngừng hoạt động",
            "Bạn có muốn ngừng hoạt động tài khoản của "
                + nv.getTenNV() + " (" + nv.getMaNV() + ")?"
    )) {
        return;
    }

    try {
        boolean ketQua = taiKhoanDAO.doiTrangThai(
            tk.getMaTK(),
            "Ngừng hoạt động"
        );

        if (ketQua) {
            taiDanhSach();

            PetPlusDialogs.info(
                this,
                "Thành công",
                "Đã ngừng hoạt động tài khoản.\n"
                    + "Hồ sơ nhân viên vẫn được giữ."
            );
        } else {
            PetPlusDialogs.error(
                this,
                "Thất bại",
                "Không tìm thấy tài khoản cần cập nhật."
            );
        }

    } catch (SQLException e) {
        sqlError("Không thể ngừng hoạt động", e);
    }
}


    private void capNhatTaiKhoan() {
        NhanVien selected = getTK();
        if (selected == null) return;
        try {
            NhanVien nv = layMoiNhat(selected.getMaNV());
            if (nv == null) { taiDanhSach(); return; }
            dlgTaiKhoan d = new dlgTaiKhoan(SwingUtilities.getWindowAncestor(this), nv);
            if (!d.open()) return;
            boolean ok = taiKhoanDAO.capNhatTaiKhoan(nv.getMaTK().getMaTK(), d.getTenDangNhap(),
                d.getMatKhauMoi(), d.getVaiTro(), d.getTrangThai());
            if (ok) {
                taiDanhSach();
                PetPlusDialogs.info(this, "Cập nhật thành công", "Tài khoản đã được cập nhật.");
            } else PetPlusDialogs.error(this, "Không thành công", "Không tìm thấy tài khoản.");
        } catch (SQLException e) { sqlError("Không thể cập nhật tài khoản", e); }
    }

    private void doiTrangThai(String trangThai) {
        NhanVien nv = getTK();
        if (nv == null) return;
        String now = nv.getMaTK().getTrangThai();
        if (trangThai.equals(now)) {
            PetPlusDialogs.info(this, "Trạng thái không đổi", "Tài khoản đã ở trạng thái: " + trangThai);
            return;
        }
        if (!PetPlusDialogs.confirm(this, trangThai.equals("Hoạt động") ? "Mở khóa tài khoản" : "Khóa tài khoản",
                "Đổi trạng thái tài khoản " + nv.getMaTK().getTenDangNhap() + " thành '" + trangThai + "'?")) return;
        try {
            if (taiKhoanDAO.doiTrangThai(nv.getMaTK().getMaTK(), trangThai)) {
                taiDanhSach();
                PetPlusDialogs.info(this, "Thành công", "Đã cập nhật trạng thái tài khoản.");
            } else PetPlusDialogs.error(this, "Không thành công", "Không tìm thấy tài khoản.");
        } catch (SQLException e) { sqlError("Không thể thay đổi trạng thái", e); }
    }

    /** Xóa thật là xóa cả nhân viên và tài khoản; ngăn thao tác nhầm bằng hai xác nhận. */
    private void xoaTaiKhoan() {
        NhanVien nv = getTK();
        if (nv == null) return;
        String username = nv.getMaTK().getTenDangNhap();
        if (!PetPlusDialogs.confirm(this, "CẢNH BÁO XÓA VĨNH VIỄN",
                "Tài khoản '" + username + "' đang thuộc nhân viên '" + nv.getTenNV() +
                "'. Nếu xóa, cả HỒ SƠ NHÂN VIÊN sẽ bị xóa khỏi SQL Server. Bạn vẫn muốn tiếp tục?")) return;
        if (!PetPlusDialogs.confirm(this, "XÁC NHẬN LẦN CUỐI",
                "XÓA VĨNH VIỄN tài khoản '" + username + "' và nhân viên mã " + nv.getMaNV() +
                "? Nếu nhân viên có hóa đơn hoặc dữ liệu liên quan, cơ sở dữ liệu có thể từ chối thao tác.")) return;
        try {
            if (taiKhoanDAO.xoa(nv.getMaTK().getMaTK())) {
                taiDanhSach();
                PetPlusDialogs.info(this, "Đã xóa", "Đã xóa tài khoản và hồ sơ nhân viên.");
            } else PetPlusDialogs.error(this, "Không thể xóa", "Tài khoản không tồn tại hoặc không được phép xóa.");
        } catch (SQLException e) {
            sqlError("Không thể xóa. Dữ liệu liên quan có thể đang tham chiếu nhân viên; giao dịch đã rollback.", e);
        }
    }

    private void phanQuyen() {
        NhanVien selected = getQuyen();
        if (selected == null) return;
        try {
            NhanVien nv = layMoiNhat(selected.getMaNV());
            if (nv == null) { taiDanhSach(); return; }
            dlgPhanQuyen d = new dlgPhanQuyen(SwingUtilities.getWindowAncestor(this), nv);
            if (!d.open()) return;
            if (taiKhoanDAO.doiVaiTro(nv.getMaTK().getMaTK(), d.getVaiTro())) {
                taiDanhSach();
                PetPlusDialogs.info(this, "Đã phân quyền", "Quyền mới: " + VaiTroHelper.tenVaiTro(d.getVaiTro()));
            } else PetPlusDialogs.error(this, "Không thành công", "Không tìm thấy tài khoản.");
        } catch (SQLException e) { sqlError("Không thể phân quyền", e); }
    }

    private void sqlError(String message, SQLException e) {
        e.printStackTrace();
        PetPlusDialogs.error(this, "Lỗi SQL Server", message + "\nChi tiết: " + e.getMessage());
    }
}
