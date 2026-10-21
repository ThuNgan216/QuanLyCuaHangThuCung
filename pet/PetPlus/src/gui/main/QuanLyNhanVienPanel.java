
package gui.main;

import appColor.AppColors;
import dao.NhanVienDAO;
import entity.NhanVien;
import entity.TaiKhoan;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class QuanLyNhanVienPanel extends JPanel {

    private JTextField txtTimKiem;
    private JTable tblNhanVien;
    private DefaultTableModel model;

    private final NhanVienDAO nhanVienDAO = new NhanVienDAO();

    private final String[] COLUMNS = {
            "Mã NV", "Họ tên", "Số điện thoại", "CCCD",
            "Địa chỉ", "Email", "Chức vụ", "Trạng thái"
    };

    public QuanLyNhanVienPanel() {
        setLayout(new BorderLayout(15, 15));
        setBackground(AppColors.BACKGROUND);
        setBorder(new EmptyBorder(25, 30, 25, 30));

        JLabel lblTitle = new JLabel("QUẢN LÝ NHÂN VIÊN");
        lblTitle.setFont(new Font("Arial", Font.BOLD, 26));
        lblTitle.setForeground(AppColors.PRIMARY_DARK);

        JLabel lblSubTitle = new JLabel(
                "Quản lý thông tin và tài khoản nhân viên");
        lblSubTitle.setFont(new Font("Arial", Font.PLAIN, 14));
        lblSubTitle.setForeground(AppColors.TEXT);

        JPanel pnlTitle = new JPanel();
        pnlTitle.setLayout(new BoxLayout(pnlTitle, BoxLayout.Y_AXIS));
        pnlTitle.setBackground(AppColors.BACKGROUND);
        pnlTitle.add(lblTitle);
        pnlTitle.add(Box.createVerticalStrut(8));
        pnlTitle.add(lblSubTitle);

        // Thanh tìm kiếm
        JPanel pnlSearch = new JPanel(new BorderLayout(10, 0));
        pnlSearch.setBackground(AppColors.WHITE);
        pnlSearch.setBorder(new EmptyBorder(15, 15, 15, 15));

        txtTimKiem = new JTextField();
        txtTimKiem.setFont(new Font("Arial", Font.PLAIN, 14));
        txtTimKiem.setPreferredSize(new Dimension(300, 38));

        JButton btnTim = taoNut("Tìm kiếm");
        JButton btnLamMoi = taoNut("Làm mới");

        JPanel pnlSearchButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        pnlSearchButtons.setBackground(AppColors.WHITE);
        pnlSearchButtons.add(btnTim);
        pnlSearchButtons.add(btnLamMoi);

        pnlSearch.add(new JLabel("Tìm nhân viên: "), BorderLayout.WEST);
        pnlSearch.add(txtTimKiem, BorderLayout.CENTER);
        pnlSearch.add(pnlSearchButtons, BorderLayout.EAST);

        JPanel pnlTop = new JPanel(new BorderLayout(0, 18));
        pnlTop.setBackground(AppColors.BACKGROUND);
        pnlTop.add(pnlTitle, BorderLayout.NORTH);
        pnlTop.add(pnlSearch, BorderLayout.CENTER);

        // Bảng dữ liệu
        model = new DefaultTableModel(COLUMNS, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tblNhanVien = new JTable(model);
        tblNhanVien.setFont(new Font("Arial", Font.PLAIN, 13));
        tblNhanVien.setRowHeight(32);
        tblNhanVien.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION);

        tblNhanVien.getTableHeader().setFont(
                new Font("Arial", Font.BOLD, 13));
        tblNhanVien.getTableHeader().setBackground(AppColors.SIDEBAR);
        tblNhanVien.getTableHeader().setForeground(AppColors.TEXT);

        JScrollPane scrollPane = new JScrollPane(tblNhanVien);
        scrollPane.setBorder(
                BorderFactory.createLineBorder(AppColors.BORDER));

        // Các nút chức năng
        JButton btnThem = taoNut("+ Thêm nhân viên");
        JButton btnSua = taoNut("Sửa thông tin");
        JButton btnXoa = taoNut("Ngừng hoạt động");

        JPanel pnlActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        pnlActions.setBackground(AppColors.BACKGROUND);
        pnlActions.add(btnThem);
        pnlActions.add(btnSua);
        pnlActions.add(btnXoa);

        JPanel pnlTable = new JPanel(new BorderLayout(0, 10));
        pnlTable.setBackground(AppColors.BACKGROUND);
        pnlTable.add(scrollPane, BorderLayout.CENTER);
        pnlTable.add(pnlActions, BorderLayout.SOUTH);

        add(pnlTop, BorderLayout.NORTH);
        add(pnlTable, BorderLayout.CENTER);

        // Sự kiện
        btnTim.addActionListener(e -> timNhanVien());
        txtTimKiem.addActionListener(e -> timNhanVien());

        btnLamMoi.addActionListener(e -> {
            txtTimKiem.setText("");
            taiDanhSach();
        });

        btnThem.addActionListener(e -> themNhanVien());
        btnSua.addActionListener(e -> suaNhanVien());
        btnXoa.addActionListener(e -> ngungNhanVien());

        // Không thêm dữ liệu mẫu.
        // Đọc danh sách trực tiếp từ SQL Server.
        taiDanhSach();
    }

    private JButton taoNut(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Arial", Font.BOLD, 13));
        btn.setForeground(AppColors.WHITE);
        btn.setBackground(AppColors.PRIMARY);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(155, 38));
        return btn;
    }

    // Tải lại toàn bộ danh sách từ database
    private void taiDanhSach() {
        try {
            hienThiDanhSach(nhanVienDAO.layDanhSach());
        } catch (SQLException e) {
            baoLoi("Không thể tải danh sách nhân viên.", e);
        }
    }

    private void hienThiDanhSach(List<NhanVien> danhSach) {
        model.setRowCount(0);

        for (NhanVien nv : danhSach) {
            TaiKhoan tk = nv.getMaTK();

            model.addRow(new Object[] {
                    nv.getMaNV(),
                    nv.getTenNV(),
                    nv.getSdt(),
                    nv.getCccd(),
                    nv.getDiaChi(),
                    nv.getEmail(),
                    nv.getChucVu(),
                    tk == null ? "" : tk.getTrangThai()
            });
        }
    }

    // Tìm kiếm trên SQL Server
    private void timNhanVien() {
        try {
            List<NhanVien> ketQua = nhanVienDAO.timKiem(txtTimKiem.getText().trim());

            hienThiDanhSach(ketQua);

            if (ketQua.isEmpty()) {
                JOptionPane.showMessageDialog(
                        this,
                        "Không tìm thấy nhân viên phù hợp.",
                        "Thông báo",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (SQLException e) {
            baoLoi("Không thể tìm kiếm nhân viên.", e);
        }
    }

    // Thêm nhân viên và tài khoản
    private void themNhanVien() {
        hienThiForm(null);
    }

    // Sửa nhân viên: lấy dữ liệu mới nhất từ database
    private void suaNhanVien() {
        int row = tblNhanVien.getSelectedRow();

        if (row == -1) {
            JOptionPane.showMessageDialog(
                    this, "Vui lòng chọn nhân viên cần sửa!");
            return;
        }

        String maNV = String.valueOf(model.getValueAt(row, 0));

        try {
            List<NhanVien> danhSach = nhanVienDAO.layDanhSach();
            NhanVien nvCanSua = null;

            for (NhanVien nv : danhSach) {
                if (nv.getMaNV().equalsIgnoreCase(maNV)) {
                    nvCanSua = nv;
                    break;
                }
            }

            if (nvCanSua == null) {
                JOptionPane.showMessageDialog(
                        this, "Không tìm thấy nhân viên trong database.");
                taiDanhSach();
                return;
            }

            hienThiForm(nvCanSua);

        } catch (SQLException e) {
            baoLoi("Không thể đọc thông tin nhân viên.", e);
        }
    }

    // Ngừng hoạt động tài khoản, không xóa vật lý
    private void ngungNhanVien() {
        int row = tblNhanVien.getSelectedRow();

        if (row == -1) {
            JOptionPane.showMessageDialog(
                    this, "Vui lòng chọn nhân viên cần ngừng hoạt động!");
            return;
        }

        String maNV = String.valueOf(model.getValueAt(row, 0));

        int chon = JOptionPane.showConfirmDialog(
                this,
                "Ngừng hoạt động tài khoản nhân viên " + maNV + "?",
                "Xác nhận",
                JOptionPane.YES_NO_OPTION);

        if (chon != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            boolean thanhCong = nhanVienDAO.xoa(maNV);

            if (thanhCong) {
                JOptionPane.showMessageDialog(
                        this, "Đã ngừng hoạt động tài khoản nhân viên.");
                taiDanhSach();
            } else {
                JOptionPane.showMessageDialog(
                        this, "Không tìm thấy tài khoản nhân viên.");
            }
        } catch (SQLException e) {
            baoLoi("Không thể cập nhật trạng thái nhân viên.", e);
        }
    }

    private void hienThiForm(NhanVien nvCu) {
        boolean dangSua = nvCu != null;

        JTextField txtMa = new JTextField(
                dangSua ? nvCu.getMaNV() : "");
        JTextField txtTen = new JTextField(
                dangSua ? nvCu.getTenNV() : "");
        JTextField txtSDT = new JTextField(
                dangSua ? giaTri(nvCu.getSdt()) : "");
        JTextField txtCCCD = new JTextField(
                dangSua ? giaTri(nvCu.getCccd()) : "");
        JTextField txtDiaChi = new JTextField(
                dangSua ? giaTri(nvCu.getDiaChi()) : "");
        JTextField txtEmail = new JTextField(
                dangSua ? giaTri(nvCu.getEmail()) : "");

        JTextField txtTenDangNhap = new JTextField();
        JPasswordField txtMatKhau = new JPasswordField();

        if (!dangSua) {
            // Mặc định tên đăng nhập và mật khẩu
            txtTenDangNhap.setText(
                    txtMa.getText().trim());
            txtMatKhau.setText("123");

            // Tự động cập nhật tên đăng nhập khi nhập mã nhân viên
            txtMa.getDocument().addDocumentListener(
                    new javax.swing.event.DocumentListener() {
                        private void capNhat() {
                            txtTenDangNhap.setText(
                                    txtMa.getText().trim());
                        }

                        @Override
                        public void insertUpdate(
                                javax.swing.event.DocumentEvent e) {
                            capNhat();
                        }

                        @Override
                        public void removeUpdate(
                                javax.swing.event.DocumentEvent e) {
                            capNhat();
                        }

                        @Override
                        public void changedUpdate(
                                javax.swing.event.DocumentEvent e) {
                            capNhat();
                        }
                    });
        }

        String[] chucVu = {
                "Nhân viên bán hàng",
                "Nhân viên kho",
                "Nhân viên chăm sóc"
        };

        JComboBox<String> cboChucVu = new JComboBox<>(chucVu);
        JComboBox<String> cboTrangThai = new JComboBox<>(
                new String[] { "Hoạt động", "Ngừng hoạt động" });

        if (dangSua) {
            TaiKhoan tk = nvCu.getMaTK();

            txtMa.setEditable(false);
            txtTenDangNhap.setText(tk.getTenDangNhap());
            txtTenDangNhap.setEditable(false);

            // Không hiển thị mật khẩu hiện có.
            // Không cho chỉnh sửa mật khẩu ở form sửa này.
            txtMatKhau.setEnabled(false);

            cboChucVu.setSelectedItem(nvCu.getChucVu());
            cboTrangThai.setSelectedItem(tk.getTrangThai());
        }

        JPanel form = new JPanel(new GridLayout(0, 2, 10, 10));
        form.setBorder(new EmptyBorder(10, 10, 10, 10));

        form.add(new JLabel("Mã nhân viên:"));
        form.add(txtMa);
        form.add(new JLabel("Họ và tên:"));
        form.add(txtTen);
        form.add(new JLabel("Số điện thoại:"));
        form.add(txtSDT);
        form.add(new JLabel("CCCD:"));
        form.add(txtCCCD);
        form.add(new JLabel("Địa chỉ:"));
        form.add(txtDiaChi);
        form.add(new JLabel("Email:"));
        form.add(txtEmail);
        form.add(new JLabel("Chức vụ:"));
        form.add(cboChucVu);
        form.add(new JLabel("Trạng thái:"));
        form.add(cboTrangThai);
        form.add(new JLabel("Tên đăng nhập:"));
        form.add(txtTenDangNhap);
        form.add(new JLabel("Mật khẩu:"));
        form.add(txtMatKhau);

        JScrollPane formScroll = new JScrollPane(form);
        formScroll.setPreferredSize(new Dimension(460, 390));
        formScroll.setBorder(null);

        int luaChon = JOptionPane.showConfirmDialog(
                this,
                formScroll,
                dangSua ? "Sửa thông tin nhân viên"
                        : "Thêm nhân viên và tài khoản",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);

        if (luaChon != JOptionPane.OK_OPTION) {
            return;
        }

        String ma = txtMa.getText().trim();
        String ten = txtTen.getText().trim();
        String sdt = txtSDT.getText().trim();
        String cccd = txtCCCD.getText().trim();
        String diaChi = txtDiaChi.getText().trim();
        String email = txtEmail.getText().trim();
        String tenDN = txtTenDangNhap.getText().trim();
        String matKhau = new String(txtMatKhau.getPassword());

        String cv = String.valueOf(cboChucVu.getSelectedItem());
        String tt = String.valueOf(cboTrangThai.getSelectedItem());

        if (ma.isEmpty() || ten.isEmpty() || sdt.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Mã nhân viên, họ tên và số điện thoại không được để trống!");
            return;
        }

        if (!dangSua) {
            tenDN = ma;
            matKhau = "123";
        }

        if (!sdt.matches("[0-9]{9,15}")) {
            JOptionPane.showMessageDialog(
                    this,
                    "Số điện thoại phải gồm 9 đến 15 chữ số!");
            return;
        }

        // Vai trò lưu trong TaiKhoan phải khớp với MainFrame.checkRole()
        String vaiTro = layVaiTro(cv);

        try {
            if (!dangSua) {
                TaiKhoan tk = new TaiKhoan(
                        taoMaTaiKhoan(ma),
                        tenDN,
                        matKhau,
                        vaiTro,
                        tt);

                NhanVien nv = new NhanVien(
                        ma,
                        ten,
                        sdt,
                        rongThanhNull(cccd),
                        rongThanhNull(diaChi),
                        rongThanhNull(email),
                        cv,
                        tk);

                nhanVienDAO.them(nv);

                JOptionPane.showMessageDialog(
                        this,
                        "Thêm nhân viên thành công!\n"
                                + "Mã nhân viên: " + ma
                                + "\nTên đăng nhập: " + tenDN
                                + "\nMật khẩu mặc định: 123");
            } else {
                TaiKhoan tkCu = nvCu.getMaTK();

                tkCu.setVaiTro(vaiTro);
                tkCu.setTrangThai(tt);

                NhanVien nv = new NhanVien(
                        ma, ten, sdt, rongThanhNull(cccd),
                        rongThanhNull(diaChi), rongThanhNull(email),
                        cv, tkCu);

                if (nhanVienDAO.capNhat(nv)) {
                    JOptionPane.showMessageDialog(
                            this, "Cập nhật nhân viên thành công!");
                } else {
                    JOptionPane.showMessageDialog(
                            this, "Không tìm thấy nhân viên để cập nhật.");
                }
            }

            taiDanhSach();

        } catch (SQLException e) {
            baoLoi(
                    dangSua
                            ? "Không thể cập nhật nhân viên."
                            : "Không thể thêm nhân viên. Hãy kiểm tra mã nhân viên, mã tài khoản, tên đăng nhập và CCCD có bị trùng không.",
                    e);
        }
    }

    private String layVaiTro(String chucVu) {
        switch (chucVu) {
            case "Quản lý":
                return "QUAN_LY";
            case "Nhân viên bán hàng":
                return "BAN_HANG";
            case "Nhân viên kho":
                return "KHO";
            case "Nhân viên chăm sóc":
                return "CHAM_SOC";
            default:
                throw new IllegalArgumentException("Chức vụ không hợp lệ.");
        }
    }

    private String taoMaTaiKhoan(String maNV) {
        // Tạo mã tài khoản từ mã nhân viên.
        // Ví dụ NV05 -> TKNV05.
        return "TK" + maNV;
    }

    private String giaTri(String value) {
        return value == null ? "" : value;
    }

    private String rongThanhNull(String value) {
        return value == null || value.trim().isEmpty()
                ? null
                : value.trim();
    }

    private void baoLoi(String thongBao, SQLException e) {
        e.printStackTrace();

        JOptionPane.showMessageDialog(
                this,
                thongBao + "\nChi tiết: " + e.getMessage(),
                "Lỗi SQL Server",
                JOptionPane.ERROR_MESSAGE);
    }
}
