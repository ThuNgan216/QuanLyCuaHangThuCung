package gui.panel;

import java.awt.CardLayout;
import java.sql.SQLException;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

import dao.ChiTietHoaDonDAO;

public class pnlQuanLyHoaDon extends JPanel {

    private static final String CARD_DS = "DS";
    private static final String CARD_CT = "CT";

    private final CardLayout cards = new CardLayout();
    private final pnlHoaDon pnlDanhSach = new pnlHoaDon();
    private final pnlChiTietHoaDon pnlChiTiet = new pnlChiTietHoaDon();
    private final ChiTietHoaDonDAO chiTietDAO = new ChiTietHoaDonDAO();

    public pnlQuanLyHoaDon() {
        setLayout(cards);
        add(pnlDanhSach, CARD_DS);
        add(pnlChiTiet, CARD_CT);

        // Chọn 1 hóa đơn trong danh sách -> sang trang chi tiết
        pnlDanhSach.setOnChonHoaDon(this::moChiTiet);

        // Nút "Quay lại" -> về danh sách
        pnlChiTiet.setOnBack(() -> cards.show(this, CARD_DS));

        cards.show(this, CARD_DS);
    }

    private void moChiTiet(String maHD) {
        try {
            ChiTietHoaDonDAO.ChiTietView v = chiTietDAO.getChiTiet(maHD);
            if (v == null) {
                JOptionPane.showMessageDialog(this, "Không tìm thấy hóa đơn " + maHD, "Thông báo",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            pnlChiTiet.hienThi(v);
            cards.show(this, CARD_CT);
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi tải chi tiết hóa đơn: " + ex.getMessage(), "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}