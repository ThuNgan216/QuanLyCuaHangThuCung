package gui.panel;

import dao.ChiTietPhieuNhapDAO;
import dao.PhieuNhapDAO;
import entity.ChiTietPhieuNhap;
import entity.PhieuNhap;
import java.awt.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import utils.PetPlusTheme;

public class pnlBaoBieuNhapHang extends JPanel {
    private DefaultTableModel model;
    private JTable table;
    private final PhieuNhapDAO phieuNhapDAO = new PhieuNhapDAO();
    private final ChiTietPhieuNhapDAO chiTietDAO = new ChiTietPhieuNhapDAO();

    private final JLabel lblTongPhieu = new JLabel("0");
    private final JLabel lblTongChiPhi = new JLabel("0 đ");
    private final JButton btnXuatBaoBieu = new JButton("Xuất báo biểu");

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

        JLabel lblTitle = new JLabel("THỐNG KÊ NHẬP HÀNG");
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
        btnXuatBaoBieu.setBackground(new Color(250, 48, 115));
        btnXuatBaoBieu.setForeground(Color.WHITE);
        pnlSouth.add(btnXuatBaoBieu);

        add(pnlSouth, BorderLayout.SOUTH);

        // Sự kiện
        btnXuatBaoBieu.addActionListener(e -> {
            if (taiDuLieuBaoBieu()) {
                xuatBaoBieuCsv();
            }
        });
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

    private boolean taiDuLieuBaoBieu() {
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
            return true;

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Không thể tải dữ liệu báo biểu: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    private void xuatBaoBieuCsv() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Lưu báo biểu nhập hàng");
        chooser.setFileFilter(new FileNameExtensionFilter("Tệp CSV (*.csv)", "csv"));
        chooser.setSelectedFile(new File("BaoCaoNhapHang_" + LocalDate.now().format(
                DateTimeFormatter.ofPattern("yyyyMMdd")) + ".csv"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;

        File tep = chooser.getSelectedFile();
        if (!tep.getName().toLowerCase(Locale.ROOT).endsWith(".csv")) {
            tep = new File(tep.getAbsoluteFile().getParentFile(), tep.getName() + ".csv");
        }
        if (tep.exists()) {
            int luaChon = JOptionPane.showConfirmDialog(this,
                    "Tệp đã tồn tại. Bạn có muốn ghi đè không?", "Xác nhận ghi đè",
                    JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (luaChon != JOptionPane.YES_OPTION) return;
        }

        try (BufferedWriter writer = Files.newBufferedWriter(tep.toPath(), StandardCharsets.UTF_8)) {
            writer.write('\uFEFF');
            ghiDongCsv(writer, "Thống kê nhập hàng");
            ghiDongCsv(writer, "Tổng số phiếu nhập", lblTongPhieu.getText());
            ghiDongCsv(writer, "Tổng chi phí nhập kho", lblTongChiPhi.getText());
            writer.newLine();

            Object[] cot = new Object[model.getColumnCount()];
            for (int i = 0; i < model.getColumnCount(); i++) {
                cot[i] = model.getColumnName(i);
            }
            ghiDongCsv(writer, cot);
            for (int row = 0; row < model.getRowCount(); row++) {
                Object[] dong = new Object[model.getColumnCount()];
                for (int column = 0; column < model.getColumnCount(); column++) {
                    dong[column] = model.getValueAt(row, column);
                }
                ghiDongCsv(writer, dong);
            }
            JOptionPane.showMessageDialog(this, "Đã xuất báo biểu đến:\n" + tep.getAbsolutePath(),
                    "Xuất báo biểu thành công", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Không thể lưu tệp báo biểu: " + ex.getMessage(),
                    "Lỗi xuất báo biểu", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void ghiDongCsv(BufferedWriter writer, Object... giaTri) throws IOException {
        for (int i = 0; i < giaTri.length; i++) {
            if (i > 0) writer.write(';');
            String chuoi = String.valueOf(giaTri[i]).replace("\"", "\"\"");
            writer.write('"');
            writer.write(chuoi);
            writer.write('"');
        }
        writer.newLine();
    }
}
