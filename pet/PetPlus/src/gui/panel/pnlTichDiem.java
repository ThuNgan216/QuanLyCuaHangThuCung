
package gui.panel;

import entity.TichDiem;
import service.TichDiemService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

import java.awt.*;
import java.util.List;

public class pnlTichDiem extends JPanel {

    private final TichDiemService service = new TichDiemService();

    private final JTextField txtTimKiem = new JTextField(24);

    private final DefaultTableModel model = new DefaultTableModel(
            new Object[] {
                    "Mã tích điểm",
                    "Mã khách hàng",
                    "Tên khách hàng",
                    "Điểm ghi nhận",
                    "Ngày cập nhật",
                    "Mã hóa đơn"
            }, 0) {

        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    private final JTable table = new JTable(model);

    private static final Color PRIMARY = new Color(224, 25, 91);
    private static final Color TEXT = new Color(32, 44, 64);
    private static final Color MUTED = new Color(89, 111, 140);
    private static final Color BORDER = new Color(237, 217, 225);

    public pnlTichDiem() {

        setLayout(new BorderLayout(12, 12));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));

        // 1. TIÊU ĐỀ VÀ THANH TÌM KIẾM
        JPanel header = new JPanel();

        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBackground(Color.WHITE);

        JLabel title = new JLabel("QUẢN LÝ TÍCH ĐIỂM KHÁCH HÀNG");
        title.setFont(new Font("Arial", Font.BOLD, 22));
        title.setForeground(PRIMARY);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        header.add(title);
        header.add(Box.createVerticalStrut(12));

        // Thanh tìm kiếm
        JPanel searchPanel = new JPanel(
                new FlowLayout(FlowLayout.LEFT, 8, 8));

        searchPanel.setOpaque(false);
        searchPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblTimKiem = new JLabel(
                "Tìm theo mã tích điểm, khách hàng hoặc hóa đơn:");

        lblTimKiem.setFont(new Font("Arial", Font.PLAIN, 13));
        lblTimKiem.setForeground(TEXT);

        txtTimKiem.setFont(new Font("Arial", Font.PLAIN, 14));

        JButton btnTim = new JButton("Tìm kiếm");
        JButton btnTaiLai = new JButton("Tải lại");

        searchPanel.add(lblTimKiem);
        searchPanel.add(txtTimKiem);
        searchPanel.add(btnTim);
        searchPanel.add(btnTaiLai);

        header.add(searchPanel);

        add(header, BorderLayout.NORTH);

        // 2. ĐỊNH DẠNG BẢNG DỮ LIỆU
        table.setFont(new Font("Arial", Font.PLAIN, 13));
        table.setForeground(TEXT);
        table.setBackground(Color.WHITE);
        table.setRowHeight(36);

        table.setGridColor(BORDER);
        table.setShowGrid(true);
        table.setIntercellSpacing(new Dimension(1, 1));

        table.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION);

        table.setSelectionBackground(new Color(255, 236, 243));
        table.setSelectionForeground(TEXT);

        table.setAutoCreateRowSorter(true);
        table.setFillsViewportHeight(true);

        // Định dạng tiêu đề cột
        JTableHeader tableHeader = table.getTableHeader();

        tableHeader.setFont(new Font("Arial", Font.BOLD, 13));
        tableHeader.setForeground(TEXT);
        tableHeader.setBackground(new Color(255, 236, 243));
        tableHeader.setPreferredSize(new Dimension(0, 40));
        tableHeader.setReorderingAllowed(false);

        // Đưa bảng vào ScrollPane
        JScrollPane scrollPane = new JScrollPane(table);

        scrollPane.setBorder(
                BorderFactory.createLineBorder(BORDER));

        scrollPane.getViewport().setBackground(Color.WHITE);

        add(scrollPane, BorderLayout.CENTER);

        // 3. GHI CHÚ CUỐI BẢNG
        JLabel note = new JLabel(
                "Lưu ý: Đây là lịch sử điểm được ghi nhận theo hóa đơn; "
                        + "mỗi hóa đơn chỉ có một bản ghi tích điểm.",
                SwingConstants.LEFT);

        note.setFont(new Font("Arial", Font.PLAIN, 12));
        note.setForeground(MUTED);
        note.setBorder(
                BorderFactory.createEmptyBorder(5, 0, 0, 0));

        add(note, BorderLayout.SOUTH);

        // 4. XỬ LÝ SỰ KIỆN TÌM KIẾM
        btnTim.addActionListener(e -> timKiem());

        txtTimKiem.addActionListener(e -> timKiem());

        btnTaiLai.addActionListener(e -> {
            txtTimKiem.setText("");
            taiDanhSach();
        });

        // 5. TẢI DỮ LIỆU BAN ĐẦU
        taiDanhSach();

        // Áp dụng giao diện PetPlus
        utils.PetPlusTheme.apply(this);
    }

    /**
     * Tải danh sách tích điểm.
     */
    private void taiDanhSach() {

        try {

            hienThi(service.layDanhSach());

        } catch (Exception ex) {

            baoLoi("Không thể tải lịch sử tích điểm.", ex);
        }
    }

    /**
     * Tìm kiếm lịch sử tích điểm.
     */
    private void timKiem() {

        try {

            hienThi(service.timKiem(txtTimKiem.getText()));

        } catch (Exception ex) {

            baoLoi("Tìm kiếm lịch sử tích điểm thất bại.", ex);
        }
    }

    /**
     * Hiển thị danh sách tích điểm lên bảng.
     */
    private void hienThi(List<TichDiem> ds) {

        model.setRowCount(0);

        for (TichDiem td : ds) {

            String maKH = td.getMaKH() == null
                    ? ""
                    : td.getMaKH().getMaKH();

            String tenKH = td.getMaKH() == null
                    ? ""
                    : td.getMaKH().getTenKH();

            String maHD = td.getMaHoaDon() == null
                    ? ""
                    : td.getMaHoaDon().getMaHoaDon();

            model.addRow(new Object[] {
                    td.getMaTichDiem(),
                    maKH,
                    tenKH,
                    td.getDiemTichLuy(),
                    td.getNgayCapNhat(),
                    maHD
            });
        }
    }

    /**
     * Hiển thị thông báo lỗi.
     */
    private void baoLoi(String noiDung, Exception ex) {

        JOptionPane.showMessageDialog(
                this,
                noiDung + "\n" + ex.getMessage(),
                "Lỗi",
                JOptionPane.ERROR_MESSAGE);
    }
}
