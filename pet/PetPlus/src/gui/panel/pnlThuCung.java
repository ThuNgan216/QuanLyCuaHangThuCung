package gui.panel;

import entity.ThuCung;
import gui.dialogs.dlgThuCung;
import service.ThuCungService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.util.List;

/** Giao diện CRUD quản lý thú cưng. */
public class pnlThuCung extends JPanel {
    private final ThuCungService service = new ThuCungService();
    private final JTextField txtTimKiem = new JTextField(22);
    private final DefaultTableModel model = new DefaultTableModel(new Object[]{"Mã thú cưng", "Tên", "Loài", "Giống", "Giới tính", "Cân nặng (kg)", "Ngày sinh", "Sức khỏe", "Tiêm chủng", "Mã khách hàng"}, 0) {
        @Override public boolean isCellEditable(int row, int column) { return false; }
    };
    private final JTable table = new JTable(model);

    public pnlThuCung() {
        setLayout(new BorderLayout(10, 10)); setBackground(Color.WHITE); setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        JLabel title = new JLabel("QUẢN LÝ THÚ CƯNG"); title.setFont(new Font("Arial", Font.BOLD, 23)); title.setForeground(new Color(224, 25, 91));
        JPanel top = new JPanel(new BorderLayout(8, 8)); top.setBackground(Color.WHITE); top.add(title, BorderLayout.NORTH);
        JPanel search = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8)); search.setBackground(Color.WHITE);
        JButton btnTim = new JButton("Tìm kiếm"), btnLamMoi = new JButton("Làm mới"); search.add(new JLabel("SĐT khách hàng:")); search.add(txtTimKiem); search.add(btnTim); search.add(btnLamMoi); top.add(search, BorderLayout.CENTER); add(top, BorderLayout.NORTH);
        table.setFont(new Font("Arial", Font.PLAIN, 13)); table.setForeground(new Color(32,44,64)); table.setBackground(Color.WHITE); table.setRowHeight(36); table.setGridColor(new Color(237,217,225)); table.setShowGrid(true); table.setIntercellSpacing(new Dimension(1,1)); table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); table.setAutoCreateRowSorter(true); table.setFillsViewportHeight(true); table.setSelectionBackground(new Color(255,236,243)); table.setSelectionForeground(new Color(32,44,64)); JTableHeader th=table.getTableHeader(); th.setFont(new Font("Arial",Font.BOLD,13)); th.setForeground(new Color(32,44,64)); th.setBackground(new Color(255,236,243)); th.setPreferredSize(new Dimension(0,42)); th.setReorderingAllowed(false); DefaultTableCellRenderer rr=new DefaultTableCellRenderer(); rr.setBackground(Color.WHITE); rr.setForeground(new Color(32,44,64)); rr.setBorder(BorderFactory.createMatteBorder(0,0,1,1,new Color(237,217,225))); rr.setVerticalAlignment(SwingConstants.CENTER); for(int i=0;i<table.getColumnCount();i++) table.getColumnModel().getColumn(i).setCellRenderer(rr); JScrollPane sc=new JScrollPane(table); sc.setBorder(BorderFactory.createLineBorder(new Color(237,217,225))); sc.getViewport().setBackground(Color.WHITE); add(sc,BorderLayout.CENTER);
        JButton btnThem = new JButton("Thêm thú cưng"), btnSua = new JButton("Sửa"), btnXoa = new JButton("Xóa"), btnTai = new JButton("Tải danh sách");
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4)); bottom.setBackground(Color.WHITE); bottom.add(btnTai); bottom.add(btnThem); bottom.add(btnSua); bottom.add(btnXoa); add(bottom, BorderLayout.SOUTH);
        btnTim.addActionListener(e -> timKiem()); txtTimKiem.addActionListener(e -> timKiem()); txtTimKiem.getDocument().addDocumentListener(new javax.swing.event.DocumentListener(){private void capNhat(){SwingUtilities.invokeLater(()->timKiem());} public void insertUpdate(javax.swing.event.DocumentEvent e){capNhat();} public void removeUpdate(javax.swing.event.DocumentEvent e){capNhat();} public void changedUpdate(javax.swing.event.DocumentEvent e){capNhat();}}); btnLamMoi.addActionListener(e -> { txtTimKiem.setText(""); taiDanhSach(); }); btnTai.addActionListener(e -> taiDanhSach());
        btnThem.addActionListener(e -> them()); btnSua.addActionListener(e -> sua()); btnXoa.addActionListener(e -> xoa()); taiDanhSach();
        utils.PetPlusTheme.apply(this);
    }
    private void taiDanhSach() { try { hienThi(service.layDanhSach()); } catch (Exception ex) { loi("Không thể tải danh sách thú cưng.", ex); } }
    private void timKiem() { try { hienThi(service.timKiem(txtTimKiem.getText())); } catch (Exception ex) { loi("Tìm kiếm thất bại.", ex); } }
    private void hienThi(List<ThuCung> ds) {
        model.setRowCount(0);
        for (ThuCung t : ds) model.addRow(new Object[]{t.getMaThuCung(), t.getTenThuCung(), t.getLoai(), t.getGiong(), t.getGioiTinh(), t.getCanNang() < 0 ? "" : t.getCanNang(), t.getNgaySinh(), t.getTinhTrangSK(), t.getTiemChung(), t.getMaKH() == null ? "" : t.getMaKH().getMaKH()});
    }
    private void them() {
        dlgThuCung dlg = new dlgThuCung(SwingUtilities.getWindowAncestor(this), null); dlg.setVisible(true);
        if (dlg.isSaved()) taiDanhSach();
    }
    private void sua() {
        ThuCung selected = layDongChon(); if (selected == null) { thongBao("Vui lòng chọn thú cưng cần sửa."); return; }
        try { ThuCung full = service.timTheoMa(selected.getMaThuCung()); if (full == null) { thongBao("Không tìm thấy thú cưng."); taiDanhSach(); return; }
            dlgThuCung dlg = new dlgThuCung(SwingUtilities.getWindowAncestor(this), full); dlg.setVisible(true);
            if (dlg.isSaved()) taiDanhSach();
        } catch (Exception ex) { loi("Không đọc được thông tin thú cưng.", ex); }
    }
    private void xoa() {
        ThuCung selected = layDongChon(); if (selected == null) { thongBao("Vui lòng chọn thú cưng cần xóa."); return; }
        if (JOptionPane.showConfirmDialog(this, "Xóa thú cưng " + selected.getMaThuCung() + " - " + selected.getTenThuCung() + "?", "Xác nhận", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
        try { if (service.xoa(selected.getMaThuCung())) { thongBao("Đã xóa thú cưng."); taiDanhSach(); } else thongBao("Không xóa được thú cưng."); } catch (Exception ex) { loi("Xóa thú cưng thất bại.", ex); }
    }
    private ThuCung layDongChon() {
        int row = table.getSelectedRow(); if (row < 0) return null; row = table.convertRowIndexToModel(row);
        ThuCung t = new ThuCung(); t.setMaThuCung(String.valueOf(model.getValueAt(row, 0))); return t;
    }
    private void thongBao(String msg) { JOptionPane.showMessageDialog(this, msg); }
    private void loi(String msg, Exception ex) { JOptionPane.showMessageDialog(this, msg + "\n" + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE); }
}
