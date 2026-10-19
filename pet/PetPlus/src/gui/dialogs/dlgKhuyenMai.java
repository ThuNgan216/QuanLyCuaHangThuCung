package gui.dialogs;

import entity.KhuyenMai;
import java.awt.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import javax.swing.*;
import service.KhuyenMaiService;
import utils.PetPlusTheme;

public class dlgKhuyenMai extends JDialog {
    private final JTextField txtMaKM = new JTextField(15);
    private final JTextField txtTenKM = new JTextField(15);
    private final JTextField txtPhanTram = new JTextField(15);
    private final JTextField txtDieuKien = new JTextField(15);
    
    // Thay JTextField bằng JSpinner định dạng ngày tháng
    private final JSpinner spnNgayBatDau = createDateSpinner();
    private final JSpinner spnNgayKetThuc = createDateSpinner();

    private final KhuyenMaiService service = new KhuyenMaiService();
    private final KhuyenMai khuyenMaiHienTai;
    private boolean daLuu = false;

    public dlgKhuyenMai(Window owner, KhuyenMai km) {
        super(owner, km == null ? "Thêm Chương Trình Khuyến Mãi" : "Cập Nhật Khuyến Mãi", ModalityType.APPLICATION_MODAL);
        this.khuyenMaiHienTai = km;
        khoiTaoGiaoDien();
    }

    private JSpinner createDateSpinner() {
        JSpinner spinner = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor editor = new JSpinner.DateEditor(spinner, "yyyy-MM-dd");
        spinner.setEditor(editor);
        return spinner;
    }

    private void khoiTaoGiaoDien() {
        setLayout(new BorderLayout(10, 10));

        JPanel pnlForm = new JPanel(new GridBagLayout());
        pnlForm.setBorder(BorderFactory.createEmptyBorder(15, 18, 10, 18));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6, 6, 6, 6);
        g.fill = GridBagConstraints.HORIZONTAL;
        g.weightx = 1;

        int y = 0;
        themDong(pnlForm, g, y++, "Mã Khuyến Mãi (*):", txtMaKM);
        themDong(pnlForm, g, y++, "Tên Khuyến Mãi (*):", txtTenKM);
        themDong(pnlForm, g, y++, "Phần Trăm Giảm (%):", txtPhanTram);
        themDong(pnlForm, g, y++, "Điều Kiện Áp Dụng:", txtDieuKien);
        themDong(pnlForm, g, y++, "Ngày Bắt Đầu:", spnNgayBatDau);
        themDong(pnlForm, g, y++, "Ngày Kết Thúc:", spnNgayKetThuc);

        // Nút thao tác
        JButton btnLuu = new JButton("Lưu");
        JButton btnHuy = new JButton("Hủy");
        JPanel pnlButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        pnlButtons.add(btnLuu);
        pnlButtons.add(btnHuy);

        add(pnlForm, BorderLayout.CENTER);
        add(pnlButtons, BorderLayout.SOUTH);

        // Sự kiện nút bấm
        btnLuu.addActionListener(e -> xuLyLuu());
        btnHuy.addActionListener(e -> dispose());

        // Nếu là chế độ cập nhật, đổ dữ liệu lên form
        if (khuyenMaiHienTai != null) {
            txtMaKM.setText(khuyenMaiHienTai.getMaKhuyenMai());
            txtMaKM.setEditable(false);
            txtTenKM.setText(khuyenMaiHienTai.getTenKhuyenMai());
            txtPhanTram.setText(String.valueOf(khuyenMaiHienTai.getPhanTramGiam()));
            txtDieuKien.setText(khuyenMaiHienTai.getDieuKienApDung());
            
            if (khuyenMaiHienTai.getNgayBatDau() != null) {
                spnNgayBatDau.setValue(Date.from(khuyenMaiHienTai.getNgayBatDau().atStartOfDay(ZoneId.systemDefault()).toInstant()));
            }
            if (khuyenMaiHienTai.getNgayKetThuc() != null) {
                spnNgayKetThuc.setValue(Date.from(khuyenMaiHienTai.getNgayKetThuc().atStartOfDay(ZoneId.systemDefault()).toInstant()));
            }
        }

        PetPlusTheme.apply(getContentPane());
        pack();
        setLocationRelativeTo(getOwner());
    }

    private void themDong(JPanel p, GridBagConstraints g, int y, String label, JComponent c) {
        g.gridx = 0; g.gridy = y; g.weightx = 0;
        p.add(new JLabel(label), g);
        g.gridx = 1; g.weightx = 1;
        p.add(c, g);
    }

    private void xuLyLuu() {
        try {
            String ma = txtMaKM.getText().trim();
            String ten = txtTenKM.getText().trim();
            if (ma.isEmpty() || ten.isEmpty()) {
                throw new IllegalArgumentException("Mã và tên khuyến mãi không được để trống.");
            }

            float phanTram = Float.parseFloat(txtPhanTram.getText().trim().replace("%", ""));
            String dieuKien = txtDieuKien.getText().trim();

            // Chuyển đổi từ JSpinner sang LocalDate
            Date dateBD = (Date) spnNgayBatDau.getValue();
            Date dateKT = (Date) spnNgayKetThuc.getValue();
            LocalDate ngayBD = dateBD.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            LocalDate ngayKT = dateKT.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

            if (ngayKT.isBefore(ngayBD)) {
                throw new IllegalArgumentException("Ngày kết thúc không được trước ngày bắt đầu.");
            }

            KhuyenMai km = new KhuyenMai(ma, ten, phanTram, dieuKien, ngayBD, ngayKT);

            if (khuyenMaiHienTai == null) {
                service.them(km);
                JOptionPane.showMessageDialog(this, "Thêm chương trình khuyến mãi thành công!");
            } else {
                service.capNhat(km);
                JOptionPane.showMessageDialog(this, "Cập nhật khuyến mãi thành công!");
            }
            daLuu = true;
            dispose();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Phần trăm giảm phải là định dạng số hợp lệ.", "Lỗi định dạng", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi nghiệp vụ", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isDaLuu() {
        return daLuu;
    }
}