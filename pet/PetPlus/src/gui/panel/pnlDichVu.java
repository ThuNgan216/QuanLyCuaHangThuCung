
package gui.panel;

import entity.DichVu;
import gui.dialogs.dlgDichVu;
import service.DichVuService;
import utils.Auth;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class pnlDichVu extends JPanel {

    private final DichVuService service = new DichVuService();

    private final JTextField txtTim = new JTextField(22);

    private final DefaultTableModel model = new DefaultTableModel(
            new Object[] {
                    "Mã dịch vụ",
                    "Tên dịch vụ",
                    "Đơn giá",
                    "Mô tả",
                    "Trạng thái"
            }, 0) {

        @Override
        public boolean isCellEditable(int row, int col) {
            return false;
        }
    };

    private final JTable table = new JTable(model);

    private static final Color PRIMARY = new Color(224, 25, 91);
    private static final Color TEXT = new Color(32, 44, 64);
    private static final Color BORDER = new Color(237, 217, 225);

    public pnlDichVu() {

        setLayout(new BorderLayout(10, 10));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));

        // 1. TIÊU ĐỀ VÀ THANH CHỨC NĂNG
        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBackground(Color.WHITE);

        JLabel title = new JLabel("QUẢN LÝ DỊCH VỤ");
        title.setFont(new Font("Arial", Font.BOLD, 22));
        title.setForeground(PRIMARY);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        header.add(title);
        header.add(Box.createVerticalStrut(12));

        // Thanh tìm kiếm và các nút chức năng
        JPanel top = new JPanel(
                new FlowLayout(FlowLayout.LEFT, 8, 8));

        top.setOpaque(false);
        top.setAlignmentX(Component.LEFT_ALIGNMENT);

        txtTim.setFont(new Font("Arial", Font.PLAIN, 14));

        top.add(new JLabel("Tìm kiếm:"));
        top.add(txtTim);

        JButton btnTim = new JButton("Tìm");
        JButton btnTai = new JButton("Tải lại");
        JButton btnThem = new JButton("Thêm");
        JButton btnSua = new JButton("Sửa");
        JButton btnXoa = new JButton("Xóa");

        // Kiểm tra quyền cập nhật
        boolean coQuyenCapNhat = Auth.hasFullAccess() || Auth.hasRole("CHAM_SOC");

        btnThem.setVisible(coQuyenCapNhat);
        btnSua.setVisible(coQuyenCapNhat);
        btnXoa.setVisible(coQuyenCapNhat);

        top.add(btnTim);
        top.add(btnTai);
        top.add(btnThem);
        top.add(btnSua);
        top.add(btnXoa);

        header.add(top);

        add(header, BorderLayout.NORTH);

        // 2. ĐỊNH DẠNG BẢNG DỮ LIỆU
        table.setFont(new Font("Arial", Font.PLAIN, 13));
        table.setForeground(TEXT);
        table.setBackground(Color.WHITE);
        table.setRowHeight(36);

        table.setGridColor(BORDER);
        table.setShowGrid(true);
        table.setIntercellSpacing(new Dimension(1, 1));

        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setSelectionBackground(new Color(255, 236, 243));
        table.setSelectionForeground(TEXT);

        // Cho phép sắp xếp dữ liệu
        table.setAutoCreateRowSorter(true);
        table.setFillsViewportHeight(true);

        // Tiêu đề cột
        table.getTableHeader().setFont(
                new Font("Arial", Font.BOLD, 13));

        table.getTableHeader().setForeground(TEXT);
        table.getTableHeader().setBackground(
                new Color(255, 236, 243));

        table.getTableHeader().setPreferredSize(
                new Dimension(0, 40));

        table.getTableHeader().setReorderingAllowed(false);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(
                BorderFactory.createLineBorder(BORDER));

        scrollPane.getViewport().setBackground(Color.WHITE);

        add(scrollPane, BorderLayout.CENTER);

        // 3. XỬ LÝ SỰ KIỆN TÌM KIẾM VÀ TẢI LẠI
        btnTim.addActionListener(e -> tim());

        txtTim.addActionListener(e -> tim());

        btnTai.addActionListener(e -> {
            txtTim.setText("");
            tai();
        });

        // 4. XỬ LÝ SỰ KIỆN THÊM, SỬA, XÓA
        btnThem.addActionListener(e -> moDialog(null));

        btnSua.addActionListener(e -> {
            DichVu d = chon();

            if (d != null) {
                moDialog(d);
            }
        });

        btnXoa.addActionListener(e -> xoa());

        // 5. TẢI DỮ LIỆU BAN ĐẦU
        tai();

        // Áp dụng giao diện PetPlus
        utils.PetPlusTheme.apply(this);
    }

    /**
     * Tải danh sách dịch vụ.
     */
    private void tai() {

        try {
            hienThi(service.layDanhSach());

        } catch (Exception ex) {
            loi("Không thể tải danh sách dịch vụ", ex);
        }
    }

    /**
     * Tìm kiếm dịch vụ.
     */
    private void tim() {

        try {
            hienThi(service.timKiem(txtTim.getText()));

        } catch (Exception ex) {
            loi("Không thể tìm dịch vụ", ex);
        }
    }

    /**
     * Hiển thị danh sách dịch vụ lên bảng.
     */
    private void hienThi(List<DichVu> ds) {

        model.setRowCount(0);

        for (DichVu d : ds) {

            model.addRow(new Object[] {
                    d.getMaDichVu(),
                    d.getTenDichVu(),
                    d.getDonGia(),
                    d.getMoTa(),
                    d.getTrangThai()
            });
        }
    }

    /**
     * Lấy dịch vụ đang được chọn.
     */
    private DichVu chon() {

        int row = table.getSelectedRow();

        if (row < 0) {
            JOptionPane.showMessageDialog(
                    this,
                    "Vui lòng chọn một dịch vụ.");

            return null;
        }

        int modelRow = table.convertRowIndexToModel(row);

        String ma = String.valueOf(
                model.getValueAt(modelRow, 0));

        try {

            for (DichVu d : service.layDanhSach()) {

                if (d.getMaDichVu().equals(ma)) {
                    return d;
                }
            }

        } catch (Exception ex) {
            loi("Không thể lấy thông tin dịch vụ", ex);
        }

        return null;
    }

    /**
     * Mở dialog thêm hoặc sửa dịch vụ.
     */
    private void moDialog(DichVu cu) {

        try {

            dlgDichVu dlg = new dlgDichVu(
                    SwingUtilities.getWindowAncestor(this), cu);

            dlg.setVisible(true);

            if (dlg.isConfirmed()) {

                DichVu d = dlg.getDichVu();

                boolean ok = cu == null
                        ? service.them(d)
                        : service.capNhat(d);

                if (ok) {

                    JOptionPane.showMessageDialog(
                            this,
                            "Lưu dịch vụ thành công.");

                    tai();

                } else {

                    JOptionPane.showMessageDialog(
                            this,
                            "Không có dữ liệu nào được thay đổi.");
                }
            }

        } catch (Exception ex) {
            loi("Không thể lưu dịch vụ", ex);
        }
    }

    /**
     * Xóa dịch vụ.
     */
    private void xoa() {

        int row = table.getSelectedRow();

        if (row < 0) {

            JOptionPane.showMessageDialog(
                    this,
                    "Vui lòng chọn dịch vụ cần xóa.");

            return;
        }

        String ma = String.valueOf(
                model.getValueAt(
                        table.convertRowIndexToModel(row), 0));

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Bạn có chắc muốn xóa dịch vụ " + ma + "?",
                "Xác nhận",
                JOptionPane.YES_NO_OPTION);

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        try {

            if (service.xoa(ma)) {

                JOptionPane.showMessageDialog(
                        this,
                        "Đã xóa dịch vụ.");

                tai();

            } else {

                JOptionPane.showMessageDialog(
                        this,
                        "Không tìm thấy dịch vụ.");
            }

        } catch (Exception ex) {
            loi("Không thể xử lý yêu cầu xóa dịch vụ", ex);
        }
    }

    /**
     * Hiển thị thông báo lỗi.
     */
    private void loi(String msg, Exception ex) {

        JOptionPane.showMessageDialog(
                this,
                msg + ":\n" + ex.getMessage(),
                "Lỗi",
                JOptionPane.ERROR_MESSAGE);
    }
}
