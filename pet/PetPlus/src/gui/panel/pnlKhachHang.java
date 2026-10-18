
package gui.panel;

import service.KhachHangService;
import entity.KhachHang;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class pnlKhachHang extends JPanel {

    private JTextField txtTimKiem;
    private JTable tblKhachHang;
    private DefaultTableModel model;

    private JButton btnTimKiem;
    private JButton btnThem;
    private JButton btnSua;
    private JButton btnXoa;
    private JButton btnLamMoi;

    private final KhachHangService khachHangService = new KhachHangService();

    public pnlKhachHang() {
        setLayout(new BorderLayout(10, 10));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        khoiTaoGiaoDien();
        ganSuKien();
        taiDanhSach();
        utils.PetPlusTheme.apply(this);
    }

    private void khoiTaoGiaoDien() {
        // Tiêu đề
        JLabel lblTieuDe = new JLabel("QUẢN LÝ KHÁCH HÀNG");
        lblTieuDe.setFont(new Font("Arial", Font.BOLD, 24));
        lblTieuDe.setForeground(new Color(40, 100, 70));

        JPanel pnlTieuDe = new JPanel(new BorderLayout());
        pnlTieuDe.setBackground(Color.WHITE);
        pnlTieuDe.add(lblTieuDe, BorderLayout.WEST);

        // Thanh tìm kiếm
        txtTimKiem = new JTextField(25);
        txtTimKiem.setFont(new Font("Arial", Font.PLAIN, 14));

        btnTimKiem = new JButton("Tìm kiếm");
        btnLamMoi = new JButton("Làm mới");

        JPanel pnlTimKiem = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        pnlTimKiem.setBackground(Color.WHITE);
        pnlTimKiem.add(new JLabel("Mã / Tên / SĐT:"));
        pnlTimKiem.add(txtTimKiem);
        pnlTimKiem.add(btnTimKiem);
        pnlTimKiem.add(btnLamMoi);

        JPanel pnlTren = new JPanel(new BorderLayout());
        pnlTren.setBackground(Color.WHITE);
        pnlTren.add(pnlTieuDe, BorderLayout.NORTH);
        pnlTren.add(pnlTimKiem, BorderLayout.CENTER);

        add(pnlTren, BorderLayout.NORTH);

        // Bảng khách hàng
        String[] cot = {
                "Mã khách hàng",
                "Tên khách hàng",
                "Số điện thoại",
                "Địa chỉ",
                "Hạng thành viên",
                "Điểm tích lũy"
        };

        model = new DefaultTableModel(cot, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tblKhachHang = new JTable(model);
        tblKhachHang.setFont(new Font("Arial", Font.PLAIN, 13));
        tblKhachHang.setRowHeight(28);
        tblKhachHang.getTableHeader().setFont(
                new Font("Arial", Font.BOLD, 13));
        tblKhachHang.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION);
        tblKhachHang.setAutoCreateRowSorter(true);

        JScrollPane scrBang = new JScrollPane(tblKhachHang);
        add(scrBang, BorderLayout.CENTER);

        // Các nút chức năng
        btnThem = new JButton("Thêm");
        btnSua = new JButton("Sửa");
        btnXoa = new JButton("Xóa");

        JPanel pnlNut = new JPanel(
                new FlowLayout(FlowLayout.RIGHT, 10, 5));
        pnlNut.setBackground(Color.WHITE);
        pnlNut.add(btnThem);
        pnlNut.add(btnSua);
        pnlNut.add(btnXoa);

        add(pnlNut, BorderLayout.SOUTH);
    }

    private void ganSuKien() {
        btnTimKiem.addActionListener(e -> timKiem());

        btnLamMoi.addActionListener(e -> {
            txtTimKiem.setText("");
            taiDanhSach();
        });

        btnThem.addActionListener(e -> themKhachHang());
        btnSua.addActionListener(e -> suaKhachHang());
        btnXoa.addActionListener(e -> xoaKhachHang());

        txtTimKiem.addActionListener(e -> timKiem());
    }

    private void taiDanhSach() {
        try {
            hienThiDanhSach(khachHangService.layDanhSach());
        } catch (Exception e) {
            hienThiLoi("Không thể tải danh sách khách hàng.", e);
        }
    }

    private void hienThiDanhSach(List<KhachHang> ds) {
        model.setRowCount(0);

        for (KhachHang kh : ds) {
            model.addRow(new Object[] {
                    kh.getMaKH(),
                    kh.getTenKH(),
                    kh.getSdt(),
                    kh.getDiaChi(),
                    kh.getHangThanhVien(),
                    kh.getDiemTichLuy()
            });
        }
    }

    private void timKiem() {
        String tuKhoa = txtTimKiem.getText().trim();

        try {
            if (tuKhoa.isEmpty()) {
                taiDanhSach();
            } else {
                hienThiDanhSach(khachHangService.timKiem(tuKhoa));
            }
        } catch (Exception e) {
            hienThiLoi("Tìm kiếm khách hàng thất bại.", e);
        }
    }

    private void themKhachHang() {
        KhachHang kh = hienThiForm(null);

        if (kh == null) {
            return;
        }

        try {
            if (khachHangService.them(kh)) {
                JOptionPane.showMessageDialog(
                        this, "Thêm khách hàng thành công!");
                taiDanhSach();
            } else {
                JOptionPane.showMessageDialog(
                        this, "Không thể thêm khách hàng.");
            }
        } catch (Exception e) {
            hienThiLoi("Thêm khách hàng thất bại.", e);
        }
    }

    private void suaKhachHang() {
        int dong = tblKhachHang.getSelectedRow();

        if (dong < 0) {
            JOptionPane.showMessageDialog(
                    this, "Vui lòng chọn khách hàng cần sửa.");
            return;
        }

        int dongModel = tblKhachHang.convertRowIndexToModel(dong);
        String maKH = model.getValueAt(dongModel, 0).toString();

        try {
            KhachHang khCu = khachHangService.timTheoMa(maKH);

            if (khCu == null) {
                JOptionPane.showMessageDialog(
                        this, "Không tìm thấy khách hàng.");
                taiDanhSach();
                return;
            }

            KhachHang khMoi = hienThiForm(khCu);

            if (khMoi != null) {
                if (khachHangService.capNhat(khMoi)) {
                    JOptionPane.showMessageDialog(
                            this, "Cập nhật khách hàng thành công!");
                    taiDanhSach();
                } else {
                    JOptionPane.showMessageDialog(
                            this, "Không thể cập nhật khách hàng.");
                }
            }
        } catch (Exception e) {
            hienThiLoi("Cập nhật khách hàng thất bại.", e);
        }
    }

    private void xoaKhachHang() {
        int dong = tblKhachHang.getSelectedRow();

        if (dong < 0) {
            JOptionPane.showMessageDialog(
                    this, "Vui lòng chọn khách hàng cần xóa.");
            return;
        }

        int dongModel = tblKhachHang.convertRowIndexToModel(dong);
        String maKH = model.getValueAt(dongModel, 0).toString();

        int luaChon = JOptionPane.showConfirmDialog(
                this,
                "Bạn có chắc muốn xóa khách hàng " + maKH + "?",
                "Xác nhận xóa",
                JOptionPane.YES_NO_OPTION);

        if (luaChon != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            if (khachHangService.xoa(maKH)) {
                JOptionPane.showMessageDialog(
                        this, "Xóa khách hàng thành công!");
                taiDanhSach();
            } else {
                JOptionPane.showMessageDialog(
                        this, "Không thể xóa khách hàng.");
            }
        } catch (Exception e) {
            hienThiLoi(
                    "Xóa thất bại. Khách hàng có thể đang được tham chiếu bởi hóa đơn, thú cưng hoặc lịch hẹn.",
                    e);
        }
    }

    private KhachHang hienThiForm(KhachHang khCu) {
        boolean dangSua = khCu != null;

        JTextField txtMa = new JTextField(
                dangSua ? khCu.getMaKH() : "");
        JTextField txtTen = new JTextField(
                dangSua ? khCu.getTenKH() : "");
        JTextField txtSDT = new JTextField(
                dangSua ? giaTri(khCu.getSdt()) : "");
        JTextField txtDiaChi = new JTextField(
                dangSua ? giaTri(khCu.getDiaChi()) : "");
        JTextField txtHang = new JTextField(
                dangSua ? String.valueOf(khCu.getHangThanhVien()) : "0");
        JTextField txtDiem = new JTextField(
                dangSua ? String.valueOf(khCu.getDiemTichLuy()) : "0");

        txtMa.setEditable(!dangSua);

        JPanel pnl = new JPanel(new GridLayout(0, 2, 8, 8));
        pnl.add(new JLabel("Mã khách hàng:"));
        pnl.add(txtMa);
        pnl.add(new JLabel("Tên khách hàng:"));
        pnl.add(txtTen);
        pnl.add(new JLabel("Số điện thoại:"));
        pnl.add(txtSDT);
        pnl.add(new JLabel("Địa chỉ:"));
        pnl.add(txtDiaChi);
        pnl.add(new JLabel("Hạng thành viên:"));
        pnl.add(txtHang);
        pnl.add(new JLabel("Điểm tích lũy:"));
        pnl.add(txtDiem);

        int ketQua = JOptionPane.showConfirmDialog(
                this,
                pnl,
                dangSua ? "Sửa khách hàng" : "Thêm khách hàng",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);

        if (ketQua != JOptionPane.OK_OPTION) {
            return null;
        }

        String ma = txtMa.getText().trim();
        String ten = txtTen.getText().trim();
        String sdt = txtSDT.getText().trim();
        String diaChi = txtDiaChi.getText().trim();

        if (ma.isEmpty() || ten.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this, "Mã và tên khách hàng không được để trống.");
            return null;
        }

        if (!sdt.isEmpty() && !sdt.matches("\\d{9,15}")) {
            JOptionPane.showMessageDialog(
                    this, "Số điện thoại phải gồm 9–15 chữ số.");
            return null;
        }

        try {
            int hang = Integer.parseInt(txtHang.getText().trim());
            int diem = Integer.parseInt(txtDiem.getText().trim());

            if (hang < 0 || diem < 0) {
                JOptionPane.showMessageDialog(
                        this, "Hạng thành viên và điểm không được âm.");
                return null;
            }

            return new KhachHang(
                    ma,
                    ten,
                    sdt.isEmpty() ? null : sdt,
                    diaChi.isEmpty() ? null : diaChi,
                    hang,
                    diem);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(
                    this, "Hạng thành viên và điểm phải là số nguyên.");
            return null;
        }
    }

    private String giaTri(String s) {
        return s == null ? "" : s;
    }

    private void hienThiLoi(String thongBao, Exception e) {
        JOptionPane.showMessageDialog(
                this,
                thongBao + "\nChi tiết: " + e.getMessage(),
                "Lỗi",
                JOptionPane.ERROR_MESSAGE);
        e.printStackTrace();
    }
}
