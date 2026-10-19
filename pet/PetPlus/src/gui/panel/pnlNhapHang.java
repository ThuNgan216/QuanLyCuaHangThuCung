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

    private JButton btnLapPhieu, btnXemChiTiet, btnLamMoi;

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
        // Tiêu đề và cụm nút thao tác phía trên
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

        pnlButtons.add(btnLapPhieu);
        pnlButtons.add(btnXemChiTiet);
        pnlButtons.add(btnLamMoi);

        pnlTop.add(lblTitle, BorderLayout.WEST);
        pnlTop.add(pnlButtons, BorderLayout.EAST);
        add(pnlTop, BorderLayout.NORTH);

        // Bảng danh sách phiếu nhập hiển thị Tên NV và Tên NCC
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
            taiDanhSach(); // Làm mới lại bảng sau khi lập phiếu xong
        });

        btnXemChiTiet.addActionListener(e -> hienThiChiTietPhieuNhap());

        // Hỗ trợ nhấp đúp (double-click) vào bảng để xem chi tiết sản phẩm nhập
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
                
                // Lấy tên Nhân viên và tên Nhà cung cấp an toàn
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
        String tenNV = model.getValueAt(selectedRow, 2).toString();   // Lấy cột 3: Tên nhân viên
        String tenNCC = model.getValueAt(selectedRow, 3).toString(); // Lấy cột 4: Tên nhà cung cấp

        String thongTinChung = "Mã Phiếu Nhập: " + maPN + "\n" +
                               "Ngày Lập: " + ngayLap + "\n" +
                               "Nhân Viên Nhập: " + tenNV + "\n" +
                               "Nhà Cung Cấp: " + tenNCC + "\n\n" +
                               "DANH SÁCH SẢN PHẨM TRONG PHIẾU:\n";

        try {
            // Lấy danh sách sản phẩm chi tiết của phiếu nhập này từ DAO
            List<ChiTietPhieuNhap> chiTietList = ctDao.layChiTietTheoMaPN(maPN);
            if (chiTietList == null || chiTietList.isEmpty()) {
                JOptionPane.showMessageDialog(this, thongTinChung + "(Không có sản phẩm chi tiết)", "Chi tiết phiếu nhập", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            // Tạo bảng con hiển thị chi tiết sản phẩm trong Dialog popup
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

            JLabel lblTong = new JLabel("Tổng Tiền Nhập Hàng: " + String.format("%,.0f đ", tongTien), JLabel.RIGHT);
            lblTong.setFont(new Font("Times New Roman", Font.BOLD, 15));
            lblTong.setForeground(new Color(224, 25, 91));
            pnlDialog.add(lblTong, BorderLayout.SOUTH);

            JOptionPane.showMessageDialog(this, pnlDialog, "Chi Tiết Phiếu Nhập - " + maPN, JOptionPane.PLAIN_MESSAGE);

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Không thể tải chi tiết sản phẩm: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
}