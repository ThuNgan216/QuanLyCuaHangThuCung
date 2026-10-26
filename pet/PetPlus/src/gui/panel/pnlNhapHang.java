package gui.panel;

import dao.ChiTietPhieuNhapDAO;
import entity.ChiTietPhieuNhap;
import entity.PhieuNhap;
import gui.dialogs.dlgPhieuNhap;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import service.NhapHangService;
import utils.PetPlusTheme;

public class pnlNhapHang extends JPanel {
    private DefaultTableModel model;
    private JTable table;
    private final NhapHangService service = new NhapHangService();
    private final ChiTietPhieuNhapDAO ctDao = new ChiTietPhieuNhapDAO();

    // Đã thêm btnGoiY vào khai báo biến toàn cục
    private JButton btnLapPhieu, btnXemChiTiet, btnLamMoi, btnGoiY;

    public pnlNhapHang() {
        setLayout(new BorderLayout(12, 12));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));

        khoiTaoGiaoDien();
        ganSuKien();
        taiDanhSach();
        PetPlusTheme.apply(this);
    }

    private void khoiTaoGiaoDien() {
        JPanel pnlTop = new JPanel(new BorderLayout());
        pnlTop.setOpaque(false);

        JLabel lblTitle = new JLabel("QUẢN LÝ NHẬP HÀNG & PHIẾU NHẬP");
        lblTitle.setFont(new Font("Arial", Font.BOLD, 22));
        lblTitle.setForeground(new Color(224, 25, 91));

        JPanel pnlButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        pnlButtons.setOpaque(false);

        btnLapPhieu = new JButton("+ Lập Phiếu Nhập Mới");
        btnXemChiTiet = new JButton("Xem Chi Tiết");
        btnLamMoi = new JButton("Làm Mới");

        // Khởi tạo nút Gợi Ý
        btnGoiY = new JButton("Gợi Ý Nhập Hàng");
        btnGoiY.setBackground(new Color(255, 193, 7));
        btnGoiY.setForeground(Color.BLACK);

        pnlButtons.add(btnLapPhieu);
        pnlButtons.add(btnGoiY);
        pnlButtons.add(btnXemChiTiet);
        pnlButtons.add(btnLamMoi);

        pnlTop.add(lblTitle, BorderLayout.WEST);
        pnlTop.add(pnlButtons, BorderLayout.EAST);
        add(pnlTop, BorderLayout.NORTH);

        String[] columns = {"Mã Phiếu Nhập", "Ngày Lập", "Nhân Viên Nhập", "Nhà Cung Cấp"};
        model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        table = new JTable(model);
        table.setRowHeight(28);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        add(new JScrollPane(table), BorderLayout.CENTER);
    }

    private void ganSuKien() {
        btnLapPhieu.addActionListener(e -> {
            dlgPhieuNhap dlg = new dlgPhieuNhap(SwingUtilities.getWindowAncestor(this));
            dlg.setVisible(true);
            if (dlg.isDaLuu()) {
                taiDanhSach(); 
            }
        });

        // Đã có thể gọi btnGoiY bình thường
        btnGoiY.addActionListener(e -> hienThiGoiYNhapHang());

        btnXemChiTiet.addActionListener(e -> hienThiChiTietPhieuNhap());

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    hienThiChiTietPhieuNhap();
                }
            }
        });

        btnLamMoi.addActionListener(e -> taiDanhSach());
    }

    private void taiDanhSach() {
        try {
            model.setRowCount(0);
            List<PhieuNhap> danhSach = service.layDanhSachPhieuNhap();
            
            for (PhieuNhap pn : danhSach) {
                String maPN = pn.getMaPN() != null ? pn.getMaPN() : "";
                String ngayLap = pn.getNgayNhap() != null ? pn.getNgayNhap().toString() : "";
                
                String tenNV = (pn.getMaNV() != null && pn.getMaNV().getTenNV() != null) 
                        ? pn.getMaNV().getTenNV() : (pn.getMaNV() != null ? pn.getMaNV().getMaNV() : "");
                
                String tenNCC = (pn.getMaNCC() != null && pn.getMaNCC().getTenNCC() != null) 
                        ? pn.getMaNCC().getTenNCC() : (pn.getMaNCC() != null ? pn.getMaNCC().getMaNCC() : "");

                model.addRow(new Object[]{ maPN, ngayLap, tenNV, tenNCC });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Không thể tải danh sách phiếu nhập: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void hienThiChiTietPhieuNhap() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn phiếu nhập cần xem chi tiết trên bảng.", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String maPN = model.getValueAt(selectedRow, 0).toString();
        String ngayLap = model.getValueAt(selectedRow, 1).toString();
        String tenNV = model.getValueAt(selectedRow, 2).toString();   
        String tenNCC = model.getValueAt(selectedRow, 3).toString(); 

        String thongTinChung = "Mã Phiếu Nhập: " + maPN + "\n" +
                               "Ngày Lập: " + ngayLap + "\n" +
                               "Nhân Viên Nhập: " + tenNV + "\n" +
                               "Nhà Cung Cấp: " + tenNCC + "\n\n" +
                               "DANH SÁCH SẢN PHẨM TRONG PHIẾU:\n";

        try {
            List<ChiTietPhieuNhap> chiTietList = ctDao.layChiTietTheoMaPN(maPN);
            if (chiTietList == null || chiTietList.isEmpty()) {
                JOptionPane.showMessageDialog(this, thongTinChung + "(Không có sản phẩm chi tiết)", "Chi tiết phiếu nhập", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            String[] colNames = {"Mã SP", "Tên Sản Phẩm", "Số Lượng", "Đơn Giá Nhập", "Thành Tiền"};
            DefaultTableModel ctModel = new DefaultTableModel(colNames, 0);
            double tongTien = 0;

            for (ChiTietPhieuNhap ct : chiTietList) {
                String tenSP = (ct.getMaSP() != null && ct.getMaSP().getTenSP() != null) ? ct.getMaSP().getTenSP() : ct.getMaSP().getMaSP();
                double donGia = ct.getDonGiaNhap() != null ? ct.getDonGiaNhap().doubleValue() : 0.0;
                int soLuong = ct.getSoLuongNhap();
                double thanhTien = soLuong * donGia;
                tongTien += thanhTien;

                ctModel.addRow(new Object[]{
                        ct.getMaSP().getMaSP(),
                        tenSP,
                        soLuong,
                        String.format("%,.0f đ", donGia),
                        String.format("%,.0f đ", thanhTien)
                });
            }

            JTable ctTable = new JTable(ctModel);
            ctTable.setRowHeight(26);
            JScrollPane scrollPane = new JScrollPane(ctTable);
            scrollPane.setPreferredSize(new Dimension(620, 260));

            JPanel pnlDialog = new JPanel(new BorderLayout(10, 10));
            pnlDialog.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            
            JTextArea txtInfo = new JTextArea(thongTinChung);
            txtInfo.setEditable(false);
            txtInfo.setFont(new Font("Times New Roman", Font.PLAIN, 14));
            
            pnlDialog.add(txtInfo, BorderLayout.NORTH);
            pnlDialog.add(scrollPane, BorderLayout.CENTER);

            JPanel pnlBottomDialog = new JPanel(new BorderLayout());
            JLabel lblTong = new JLabel("Tổng Tiền Nhập Hàng: " + String.format("%,.0f đ", tongTien), JLabel.RIGHT);
            lblTong.setFont(new Font("Times New Roman", Font.BOLD, 15));
            lblTong.setForeground(new Color(224, 25, 91));
            
            JButton btnInPhieu = new JButton("🖨️ In Phiếu Nhập");
            final double finalTongTien = tongTien; 
            
            btnInPhieu.addActionListener(ev -> {
                try {
                    StringBuilder html = new StringBuilder();
                    html.append("<html><body style='font-family: Arial, sans-serif; padding: 20px;'>");
                    html.append("<h2 style='text-align: center;'>PHIẾU NHẬP KHO</h2>");
                    html.append("<p><b>Mã PN:</b> ").append(maPN).append(" &nbsp;&nbsp;&nbsp; <b>Ngày lập:</b> ").append(ngayLap).append("</p>");
                    html.append("<p><b>Nhân viên:</b> ").append(tenNV).append("</p>");
                    html.append("<p><b>Nhà cung cấp:</b> ").append(tenNCC).append("</p>");
                    html.append("<hr>");
                    html.append("<table style='width: 100%; border-collapse: collapse;' border='1' cellpadding='5'>");
                    html.append("<tr style='background-color: #f2f2f2;'><th>Mã SP</th><th>Tên sản phẩm</th><th>SL</th><th>Đơn giá</th><th>Thành tiền</th></tr>");
                    
                    for (ChiTietPhieuNhap ct : chiTietList) {
                        String tsp = (ct.getMaSP() != null && ct.getMaSP().getTenSP() != null) ? ct.getMaSP().getTenSP() : ct.getMaSP().getMaSP();
                        double dg = ct.getDonGiaNhap() != null ? ct.getDonGiaNhap().doubleValue() : 0.0;
                        double tt = ct.getSoLuongNhap() * dg;
                        html.append("<tr>");
                        html.append("<td>").append(ct.getMaSP().getMaSP()).append("</td>");
                        html.append("<td>").append(tsp).append("</td>");
                        html.append("<td style='text-align:center;'>").append(ct.getSoLuongNhap()).append("</td>");
                        html.append("<td style='text-align:right;'>").append(String.format("%,.0f", dg)).append("</td>");
                        html.append("<td style='text-align:right;'>").append(String.format("%,.0f", tt)).append("</td>");
                        html.append("</tr>");
                    }
                    html.append("</table>");
                    html.append("<h3 style='text-align: right;'>Tổng tiền: ").append(String.format("%,.0f VNĐ", finalTongTien)).append("</h3>");
                    html.append("</body></html>");

                    JEditorPane editorPane = new JEditorPane("text/html", html.toString());
                    boolean printed = editorPane.print(null, null, true, null, null, true);
                    if (printed) {
                        JOptionPane.showMessageDialog(this, "Đã gửi lệnh in thành công!");
                    }
                } catch (Exception exPrint) {
                    JOptionPane.showMessageDialog(this, "Lỗi in phiếu: " + exPrint.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            });

            pnlBottomDialog.add(btnInPhieu, BorderLayout.WEST);
            pnlBottomDialog.add(lblTong, BorderLayout.EAST);
            
            pnlDialog.add(pnlBottomDialog, BorderLayout.SOUTH);

            JOptionPane.showOptionDialog(this, pnlDialog, "Chi Tiết Phiếu Nhập - " + maPN, 
                    JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, new Object[]{"Đóng"}, "Đóng");

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Không thể tải chi tiết sản phẩm: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void hienThiGoiYNhapHang() {
        try {
            List<Object[]> dsGoiY = new dao.SanPhamDAO().layDanhSachGoiYNhapHang(10); 
            if (dsGoiY.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Kho hàng đang đầy đủ, chưa có sản phẩm nào cần nhập thêm!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            String[] cols = {"Mã SP", "Tên Sản Phẩm", "Tồn Kho Hiện Tại", "Gợi ý NCC", "Giá Nhập Lần Cuối"};
            DefaultTableModel modelGoiY = new DefaultTableModel(cols, 0) {
                @Override
                public boolean isCellEditable(int row, int col) { return false; }
            };

            for (Object[] row : dsGoiY) {
                float giaGoiY = (float) row[4];
                row[4] = giaGoiY > 0 ? String.format("%,.0f đ", giaGoiY) : "-";
                modelGoiY.addRow(row);
            }

            JTable tbl = new JTable(modelGoiY);
            tbl.setRowHeight(28);
            JScrollPane scroll = new JScrollPane(tbl);
            scroll.setPreferredSize(new Dimension(750, 300));

            JOptionPane.showMessageDialog(this, scroll, "Danh Sách Hàng Sắp Hết (Cần Nhập)", JOptionPane.PLAIN_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi tải gợi ý: " + ex.getMessage());
        }
    }
}