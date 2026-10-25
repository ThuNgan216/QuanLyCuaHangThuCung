package gui.panel;

import java.awt.BorderLayout;
import java.awt.Window;
import java.sql.SQLException;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import dao.ChiTietHoaDonDAO;
import gui.dialogs.dlgChiTietHoaDon;

public class pnlQuanLyHoaDon extends JPanel {

    private final pnlHoaDon pnlDanhSach = new pnlHoaDon();
    private final ChiTietHoaDonDAO chiTietDAO = new ChiTietHoaDonDAO();

    public pnlQuanLyHoaDon() {
        setLayout(new BorderLayout());
        add(pnlDanhSach, BorderLayout.CENTER);

        // Chọn 1 hóa đơn trong danh sách -> mở dialog chi tiết
        pnlDanhSach.setOnChonHoaDon(this::moChiTiet);
    }

    private void moChiTiet(String maHD) {
        try {
            ChiTietHoaDonDAO.ChiTietView v = chiTietDAO.getChiTiet(maHD);
            if (v == null) {
                JOptionPane.showMessageDialog(this, "Không tìm thấy hóa đơn " + maHD, "Thông báo",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            Window owner = SwingUtilities.getWindowAncestor(this);
            new dlgChiTietHoaDon(owner, v).setVisible(true); // modal: dừng tới khi đóng
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi tải chi tiết hóa đơn: " + ex.getMessage(), "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}