package gui.panel;

import dao.ChiTietPhieuNhapDAO;
import dao.PhieuNhapDAO;
import entity.ChiTietPhieuNhap;
import entity.PhieuNhap;
import java.awt.*;
import java.math.BigDecimal;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import utils.PetPlusTheme;

public class pnlBaoBieuNhapHang extends JPanel {
    private DefaultTableModel model;
    private JTable table;
    private final PhieuNhapDAO phieuNhapDAO = new PhieuNhapDAO();
    private final ChiTietPhieuNhapDAO chiTietDAO = new ChiTietPhieuNhapDAO();

    private final JLabel lblTongPhieu = new JLabel("0");
    private final JLabel lblTongChiPhi = new JLabel("0 đ");
    private final JButton btnLamMoi = new JButton("Làm Mới Thống Kê");

    public pnlBaoBieuNhapHang() {
        setLayout(new BorderLayout(15, 15));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        khoiTaoGiaoDien();
        taiDuLieuBaoBieu();
        PetPlusTheme.apply(this);
    }

    private void khoiTaoGiaoDien() {
        // --- 1. TIÊU ĐỀ VÀ THÔNG KÊ TỔNG QUAN (NORTH) ---
        JPanel pnlNorth = new JPanel(new BorderLayout(10, 10));
        pnlNorth.setOpaque(false);

        JLabel lblTitle = new JLabel("BÁO BIỂU & THỐNG KÊ NHẬP HÀNG");
        lblTitle.setFont(new Font("Arial", Font.BOLD, 22));
        lblTitle.setForeground(new Color(224, 25, 91));
        pnlNorth.add(lblTitle, BorderLayout.WEST);

        // Panel các thẻ card thống kê nhanh
        JPanel pnlCards = new JPanel(new GridLayout(1, 2, 15, 0));
        pnlCards.setOpaque(false);

        pnlCards.add(createCard("Tổng số phiếu nhập", lblTongPhieu, new Color(240, 244, 248)));
        pnlCards.add(createCard("Tổng chi phí nhập kho", lblTongChiPhi, new Color(253, 236, 241)));

        JPanel pnlTopWrapper = new JPanel(new BorderLayout(0, 12));
        pnlTopWrapper.setOpaque(false);
        pnlTopWrapper.add(pnlNorth, BorderLayout.NORTH);
        pnlTopWrapper.add(pnlCards, BorderLayout.CENTER);

        add(pnlTopWrapper, BorderLayout.NORTH);

        // --- 2. BẢNG CHI TIẾT THỐNG KÊ (CENTER) ---
        String[] columns = {"Mã Phiếu Nhập", "Ngày Lập", "Nhân Viên Nhập", "Nhà Cung Cấp", "Tổng Tiền Nhập"};
        model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        table = new JTable(model);
        table.setRowHeight(28);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        add(new JScrollPane(table), BorderLayout.CENTER);

        // --- 3. THANH TÁC VỤ (SOUTH) ---
        JPanel pnlSouth = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        pnlSouth.setOpaque(false);
        btnLamMoi.setBackground(new Color(250, 48, 115));
        btnLamMoi.setForeground(Color.WHITE);
        pnlSouth.add(btnLamMoi);

        add(pnlSouth, BorderLayout.SOUTH);

        // Sự kiện
        btnLamMoi.addActionListener(e -> taiDuLieuBaoBieu());
    }

    private JPanel createCard(String title, JLabel lblValue, Color bg) {
        JPanel card = new JPanel(new BorderLayout(5, 5));
        card.setBackground(bg);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(218, 225, 233)),
                BorderFactory.createEmptyBorder(12, 15, 12, 15)
        ));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Arial", Font.PLAIN, 13));
        lblTitle.setForeground(new Color(89, 111, 140));

        lblValue.setFont(new Font("Arial", Font.BOLD, 20));
        lblValue.setForeground(new Color(33, 37, 41));

        card.add(lblTitle, BorderLayout.NORTH);
        card.add(lblValue, BorderLayout.CENTER);
        return card;
    }

    private void taiDuLieuBaoBieu() {
        try {
            model.setRowCount(0);
            List<PhieuNhap> ds = phieuNhapDAO.layDanhSach();
            
            int tongSoPhieu = ds.size();
            BigDecimal tongChiPhiAll = BigDecimal.ZERO;

            for (PhieuNhap pn : ds) {
                String maPN = pn.getMaPN() != null ? pn.getMaPN() : "";
                String ngayLap = pn.getNgayNhap() != null ? pn.getNgayNhap().toString() : "";
                
                String tenNV = (pn.getMaNV() != null && pn.getMaNV().getTenNV() != null) 
                        ? pn.getMaNV().getTenNV() : (pn.getMaNV() != null ? pn.getMaNV().getMaNV() : "");
                
                String tenNCC = (pn.getMaNCC() != null && pn.getMaNCC().getTenNCC() != null) 
                        ? pn.getMaNCC().getTenNCC() : (pn.getMaNCC() != null ? pn.getMaNCC().getMaNCC() : "");

                // Tính tổng tiền của từng phiếu nhập dựa vào chi tiết sản phẩm
                List<ChiTietPhieuNhap> dsChiTiet = chiTietDAO.layChiTietTheoMaPN(maPN);
                BigDecimal tongTienPhieu = BigDecimal.ZERO;
                if (dsChiTiet != null) {
                    for (ChiTietPhieuNhap ct : dsChiTiet) {
                        BigDecimal donGia = ct.getDonGiaNhap() != null ? ct.getDonGiaNhap() : BigDecimal.ZERO;
                        BigDecimal thanhTien = donGia.multiply(new BigDecimal(ct.getSoLuongNhap()));
                        tongTienPhieu = tongTienPhieu.add(thanhTien);
                    }
                }
                tongChiPhiAll = tongChiPhiAll.add(tongTienPhieu);

                model.addRow(new Object[]{
                        maPN,
                        ngayLap,
                        tenNV,
                        tenNCC,
                        String.format("%,.0f đ", tongTienPhieu.doubleValue())
                });
            }

            // Hiển thị lên các thẻ card tổng quan
            lblTongPhieu.setText(String.valueOf(tongSoPhieu));
            lblTongChiPhi.setText(String.format("%,.0f đ", tongChiPhiAll.doubleValue()));

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Không thể tải dữ liệu báo biểu: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
}