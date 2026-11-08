package gui.dialogs;

import entity.KhachHang;
import service.KhachHangService;
import utils.ThongBao;

import javax.swing.*;
import java.awt.*;

/** Hộp thoại thêm / sửa khách hàng. Luật kiểm tra dữ liệu nằm ở KhachHangService. */
public class dlgKhachHang extends JDialog {
    private final JTextField txtMa = new JTextField(20);
    private final JTextField txtTen = new JTextField(20);
    private final JTextField txtSdt = new JTextField(20);
    private final JTextField txtDiaChi = new JTextField(20);
    private final JTextField txtHang = new JTextField(20);
    private final JTextField txtDiem = new JTextField(20);
    private final KhachHangService service = new KhachHangService();
    private final KhachHang khachHangCu;
    private boolean saved;

    /** @param khachHang null = thêm mới, khác null = sửa khách hàng đó. */
    public dlgKhachHang(Window owner, KhachHang khachHang) {
        super(owner, khachHang == null ? "Thêm khách hàng" : "Cập nhật khách hàng", ModalityType.APPLICATION_MODAL);
        this.khachHangCu = khachHang;
        setLayout(new BorderLayout(10, 10));

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(15, 18, 8, 18));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5, 5, 5, 5);
        c.fill = GridBagConstraints.HORIZONTAL;
        addRow(form, c, 0, "Mã khách hàng:", txtMa);
        addRow(form, c, 1, "Tên khách hàng:", txtTen);
        addRow(form, c, 2, "Số điện thoại:", txtSdt);
        addRow(form, c, 3, "Địa chỉ:", txtDiaChi);
        addRow(form, c, 4, "Hạng thành viên:", txtHang);
        addRow(form, c, 5, "Điểm tích lũy:", txtDiem);
        add(form, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnLuu = new JButton("Lưu");
        JButton btnHuy = new JButton("Hủy");
        buttons.add(btnLuu);
        buttons.add(btnHuy);
        add(buttons, BorderLayout.SOUTH);

        // Mã, hạng, điểm do hệ thống quản lý, không cho nhập tay.
        txtMa.setEditable(false);
        txtHang.setEditable(false);
        txtDiem.setEditable(false);
        txtHang.setFocusable(false);
        txtDiem.setFocusable(false);
        txtDiaChi.setToolTipText("Ví dụ: 650 Lê Văn Thọ, Gò Vấp");

        if (khachHang != null) {
            txtMa.setText(khachHang.getMaKH());
            txtTen.setText(khachHang.getTenKH());
            txtSdt.setText(giaTri(khachHang.getSdt()));
            txtDiaChi.setText(giaTri(khachHang.getDiaChi()));
            txtHang.setText(KhachHangService.moTaHang(khachHang.getHangThanhVien(), khachHang.getDiemTichLuy()));
            txtDiem.setText(String.valueOf(khachHang.getDiemTichLuy()));
        } else {
            try {
                txtMa.setText(service.taoMaMoi());
            } catch (RuntimeException ex) {
                btnLuu.setEnabled(false);
                ThongBao.loi(owner, "Không thể tạo mã khách hàng", ex);
            }
            txtHang.setText(KhachHangService.moTaHang(entity.HangThanhVien.HANG_1.getMa(), 0));
            txtDiem.setText("0");
        }

        btnLuu.addActionListener(e -> luu());
        btnHuy.addActionListener(e -> dispose());
        getRootPane().setDefaultButton(btnLuu);

        utils.PetPlusTheme.apply(getContentPane());
        pack();
        setResizable(false);
        setLocationRelativeTo(owner);
    }

    private void addRow(JPanel p, GridBagConstraints c, int row, String label, Component input) {
        c.gridx = 0; c.gridy = row; c.weightx = 0; p.add(new JLabel(label), c);
        c.gridx = 1; c.weightx = 1; p.add(input, c);
    }

    /**
     * Lưu ngay trong dialog (giống các dialog thú cưng, lịch hẹn, dịch vụ): nếu service từ chối thì báo lỗi
     * và giữ dialog mở để người dùng sửa lại, không mất dữ liệu đã nhập.
     */
    private void luu() {
        try {
            KhachHang kh = layKhachHang();
            boolean ok = khachHangCu == null ? service.them(kh) : service.capNhat(kh);
            if (!ok) {
                ThongBao.khongThayDoi(this);
                return;
            }
            saved = true;
            if (khachHangCu == null)
                ThongBao.daThem(this, "khách hàng", kh.getMaKH());
            else
                ThongBao.daCapNhat(this, "khách hàng");
            dispose();
        } catch (RuntimeException ex) {
            ThongBao.xuLy(this, "Không thể lưu khách hàng", ex);
        }
    }

    private static String giaTri(String s) {
        return s == null ? "" : s;
    }

    public boolean isSaved() {
        return saved;
    }

    private KhachHang layKhachHang() {
        // Hạng và điểm không sửa tay; service luôn lấy lại từ dữ liệu đang lưu.
        int hang = khachHangCu == null ? 1 : khachHangCu.getHangThanhVien();
        int diem = khachHangCu == null ? 0 : khachHangCu.getDiemTichLuy();
        return new KhachHang(txtMa.getText().trim(), txtTen.getText().trim(), txtSdt.getText().trim(),
                txtDiaChi.getText().trim(), hang, diem);
    }
}
