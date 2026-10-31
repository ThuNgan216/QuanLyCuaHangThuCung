package gui.panel;

import dao.BaoCaoLoiNhuanDAO;
import dao.BaoCaoLoiNhuanDAO.DongTheoNgay;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerDateModel;
import javax.swing.SwingConstants;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import utils.PetPlusTheme;

/** Trang thống kê lợi nhuận gộp ước tính từ các sản phẩm đã bán. */
public class pnlBaoBieuLoiNhuan extends JPanel {
    private static final Color PRIMARY = new Color(250, 48, 115);
    private static final Color MUTED = new Color(89, 111, 140);
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final BaoCaoLoiNhuanDAO baoCaoDAO = new BaoCaoLoiNhuanDAO();
    private final JSpinner spnTuNgay = taoSpinnerNgay(LocalDate.now().withDayOfMonth(1));
    private final JSpinner spnDenNgay = taoSpinnerNgay(LocalDate.now());
    private final JLabel lblDoanhThu = new JLabel("0 đ");
    private final JLabel lblGiaVon = new JLabel("0 đ");
    private final JLabel lblLoiNhuan = new JLabel("0 đ");
    private final DefaultTableModel model = new DefaultTableModel(new String[] {
            "Ngày", "Số hóa đơn", "Doanh thu", "Vốn", "Lợi nhuận"
    }, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    public pnlBaoBieuLoiNhuan() {
        setLayout(new BorderLayout(0, 12));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        taoGiaoDien();
        taiDuLieuBaoCao();
        PetPlusTheme.apply(this);
    }

    private void taoGiaoDien() {
        JPanel pnlTren = new JPanel(new BorderLayout(0, 12));
        pnlTren.setOpaque(false);

        JLabel lblTieuDe = new JLabel("THỐNG KÊ LỢI NHUẬN");
        lblTieuDe.setFont(new Font("Arial", Font.BOLD, 22));
        lblTieuDe.setForeground(new Color(224, 25, 91));
        pnlTren.add(lblTieuDe, BorderLayout.NORTH);

        JPanel pnlBoLoc = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        pnlBoLoc.setOpaque(false);
        pnlBoLoc.add(new JLabel("Từ ngày:"));
        pnlBoLoc.add(spnTuNgay);
        pnlBoLoc.add(new JLabel("Đến ngày:"));
        pnlBoLoc.add(spnDenNgay);
        JButton btnXuat = new JButton("Xuất báo biểu");
        btnXuat.setBackground(PRIMARY);
        btnXuat.setForeground(Color.WHITE);
        btnXuat.addActionListener(e -> {
            if (taiDuLieuBaoCao()) {
                xuatBaoBieuCsv();
            }
        });
        pnlBoLoc.add(btnXuat);
        pnlTren.add(pnlBoLoc, BorderLayout.CENTER);

        JPanel pnlTongQuan = new JPanel(new GridLayout(1, 3, 12, 0));
        pnlTongQuan.setOpaque(false);
        pnlTongQuan.add(taoTheThongKe("Doanh thu", lblDoanhThu,
                new Color(239, 247, 255)));
        pnlTongQuan.add(taoTheThongKe("Vốn", lblGiaVon,
                new Color(255, 249, 235)));
        pnlTongQuan.add(taoTheThongKe("Lợi nhuận", lblLoiNhuan,
                new Color(253, 236, 241)));
        pnlTren.add(pnlTongQuan, BorderLayout.SOUTH);
        add(pnlTren, BorderLayout.NORTH);

        JTable tblLoiNhuan = new JTable(model);
        tblLoiNhuan.setRowHeight(30);
        tblLoiNhuan.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblLoiNhuan.setFillsViewportHeight(true);
        DefaultTableCellRenderer canPhai = new DefaultTableCellRenderer();
        canPhai.setHorizontalAlignment(SwingConstants.RIGHT);
        for (int column = 1; column < model.getColumnCount(); column++) {
            tblLoiNhuan.getColumnModel().getColumn(column).setCellRenderer(canPhai);
        }
        add(new JScrollPane(tblLoiNhuan), BorderLayout.CENTER);
    }

    private JPanel taoTheThongKe(String tieuDe, JLabel lblGiaTri, Color mauNen) {
        JPanel the = new JPanel(new BorderLayout(5, 5));
        the.setBackground(mauNen);
        the.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(218, 225, 233)),
                BorderFactory.createEmptyBorder(10, 14, 10, 14)));
        JLabel lblTieuDe = new JLabel(tieuDe);
        lblTieuDe.setFont(new Font("Arial", Font.PLAIN, 13));
        lblTieuDe.setForeground(MUTED);
        lblGiaTri.setFont(new Font("Arial", Font.BOLD, 20));
        lblGiaTri.setForeground(new Color(33, 37, 41));
        the.add(lblTieuDe, BorderLayout.NORTH);
        the.add(lblGiaTri, BorderLayout.CENTER);
        return the;
    }

    private boolean taiDuLieuBaoCao() {
        LocalDate tuNgay = layNgay(spnTuNgay);
        LocalDate denNgay = layNgay(spnDenNgay);
        if (tuNgay.isAfter(denNgay)) {
            JOptionPane.showMessageDialog(this, "Ngày bắt đầu phải trước hoặc bằng ngày kết thúc.",
                    "Khoảng thời gian không hợp lệ", JOptionPane.WARNING_MESSAGE);
            return false;
        }

        try {
            List<DongTheoNgay> duLieu = baoCaoDAO.layBaoCao(tuNgay, denNgay);
            model.setRowCount(0);
            BigDecimal tongDoanhThu = BigDecimal.ZERO;
            BigDecimal tongGiaVon = BigDecimal.ZERO;
            BigDecimal tongLoiNhuan = BigDecimal.ZERO;

            for (DongTheoNgay dong : duLieu) {
                tongDoanhThu = tongDoanhThu.add(dong.doanhThuDaCoGiaVon);
                tongGiaVon = tongGiaVon.add(dong.giaVonUocTinh);
                tongLoiNhuan = tongLoiNhuan.add(dong.loiNhuanGopUocTinh);
                model.addRow(new Object[] {
                        dong.ngay == null ? "" : dong.ngay.format(DATE_FORMAT),
                        String.format("%,d", dong.soHoaDon),
                        dinhDangTien(dong.doanhThuDaCoGiaVon),
                        dinhDangTien(dong.giaVonUocTinh),
                        dinhDangTien(dong.loiNhuanGopUocTinh)
                });
            }

            lblDoanhThu.setText(dinhDangTien(tongDoanhThu));
            lblGiaVon.setText(dinhDangTien(tongGiaVon));
            lblLoiNhuan.setText(dinhDangTien(tongLoiNhuan));
            return true;
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Không thể tải thống kê lợi nhuận: " + ex.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    private static String dinhDangTien(BigDecimal soTien) {
        return String.format("%,.0f đ", soTien.doubleValue());
    }

    private void xuatBaoBieuCsv() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Lưu báo biểu lợi nhuận");
        chooser.setFileFilter(new FileNameExtensionFilter("Tệp CSV (*.csv)", "csv"));
        chooser.setSelectedFile(new File("BaoCaoLoiNhuan_" + LocalDate.now().format(
                DateTimeFormatter.ofPattern("yyyyMMdd")) + ".csv"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File tep = chooser.getSelectedFile();
        if (!tep.getName().toLowerCase(Locale.ROOT).endsWith(".csv")) {
            tep = new File(tep.getAbsoluteFile().getParentFile(), tep.getName() + ".csv");
        }
        if (tep.exists()) {
            int luaChon = JOptionPane.showConfirmDialog(this,
                    "Tệp đã tồn tại. Bạn có muốn ghi đè không?", "Xác nhận ghi đè",
                    JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (luaChon != JOptionPane.YES_OPTION) {
                return;
            }
        }

        try (BufferedWriter writer = Files.newBufferedWriter(tep.toPath(), StandardCharsets.UTF_8)) {
            writer.write('\uFEFF');
            ghiDongCsv(writer, "Thống kê lợi nhuận");
            ghiDongCsv(writer, "Từ ngày", layNgay(spnTuNgay).format(DATE_FORMAT));
            ghiDongCsv(writer, "Đến ngày", layNgay(spnDenNgay).format(DATE_FORMAT));
            ghiDongCsv(writer, "Doanh thu", lblDoanhThu.getText());
            ghiDongCsv(writer, "Vốn", lblGiaVon.getText());
            ghiDongCsv(writer, "Lợi nhuận", lblLoiNhuan.getText());
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

    private LocalDate layNgay(JSpinner spinner) {
        Date ngay = (Date) spinner.getValue();
        return ngay.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    private static JSpinner taoSpinnerNgay(LocalDate ngay) {
        Date giaTri = Date.from(ngay.atStartOfDay(ZoneId.systemDefault()).toInstant());
        JSpinner spinner = new JSpinner(new SpinnerDateModel(giaTri, null, null, Calendar.DAY_OF_MONTH));
        spinner.setEditor(new JSpinner.DateEditor(spinner, "dd/MM/yyyy"));
        return spinner;
    }
}
