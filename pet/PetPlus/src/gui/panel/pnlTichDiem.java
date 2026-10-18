package gui.panel;

import entity.TichDiem;
import service.TichDiemService;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.List;

public class pnlTichDiem extends JPanel {
    private final TichDiemService service = new TichDiemService();
    private final JTextField txtTimKiem = new JTextField(24);
    private final DefaultTableModel model = new DefaultTableModel(
            new Object[]{"Mã tích điểm", "Mã khách hàng", "Tên khách hàng", "Điểm ghi nhận", "Ngày cập nhật", "Mã hóa đơn"}, 0) {
        @Override public boolean isCellEditable(int row, int column) { return false; }
    };
    private final JTable table = new JTable(model);

    public pnlTichDiem() {
        setLayout(new BorderLayout(12, 12));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));

        JLabel title = new JLabel("QUẢN LÝ TÍCH ĐIỂM KHÁCH HÀNG");
        title.setFont(new Font("Arial", Font.BOLD, 22));
        title.setForeground(new Color(224, 25, 91));
        add(title, BorderLayout.NORTH);

        JPanel top = new JPanel(new BorderLayout(8, 8));
        top.setOpaque(false);
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        searchPanel.setOpaque(false);
        searchPanel.add(new JLabel("Tìm theo mã tích điểm, khách hàng hoặc hóa đơn:"));
        searchPanel.add(txtTimKiem);
        JButton btnTim = new JButton("Tìm kiếm");
        JButton btnTaiLai = new JButton("Tải lại");
        searchPanel.add(btnTim);
        searchPanel.add(btnTaiLai);
        top.add(searchPanel, BorderLayout.CENTER);
        add(top, BorderLayout.BEFORE_FIRST_LINE);

        table.setRowHeight(28);
        table.setFont(new Font("Arial", Font.PLAIN, 13));
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 13));
        table.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JLabel note = new JLabel("Lưu ý: Đây là lịch sử điểm được ghi nhận theo hóa đơn; mỗi hóa đơn chỉ có một bản ghi tích điểm.", SwingConstants.LEFT);
        note.setForeground(new Color(89, 111, 140));
        add(note, BorderLayout.SOUTH);

        btnTim.addActionListener(e -> timKiem());
        txtTimKiem.addActionListener(e -> timKiem());
        btnTaiLai.addActionListener(e -> { txtTimKiem.setText(""); taiDanhSach(); });
        taiDanhSach();
        utils.PetPlusTheme.apply(this);
    }

    private void taiDanhSach() {
        try { hienThi(service.layDanhSach()); }
        catch (Exception ex) { baoLoi("Không thể tải lịch sử tích điểm.", ex); }
    }

    private void timKiem() {
        try { hienThi(service.timKiem(txtTimKiem.getText())); }
        catch (Exception ex) { baoLoi("Tìm kiếm lịch sử tích điểm thất bại.", ex); }
    }

    private void hienThi(List<TichDiem> ds) {
        model.setRowCount(0);
        for (TichDiem td : ds) {
            String maKH = td.getMaKH() == null ? "" : td.getMaKH().getMaKH();
            String tenKH = td.getMaKH() == null ? "" : td.getMaKH().getTenKH();
            String maHD = td.getMaHoaDon() == null ? "" : td.getMaHoaDon().getMaHoaDon();
            model.addRow(new Object[]{td.getMaTichDiem(), maKH, tenKH, td.getDiemTichLuy(), td.getNgayCapNhat(), maHD});
        }
    }

    private void baoLoi(String noiDung, Exception ex) {
        JOptionPane.showMessageDialog(this, noiDung + "\n" + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
    }
}
